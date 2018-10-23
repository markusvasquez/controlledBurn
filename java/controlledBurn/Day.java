package controlledBurn;


import org.joda.time.LocalDate;

import java.io.Serializable;

import Jama.Matrix;

class Day implements Serializable{




   //Declare class variables.

   private LocalDate date;

   private Matrix priorX;   //prior mean of state.
                            //State is [weight, neee]
   private Matrix priorP;   //prior variance-covariance of morning state
   private Matrix postX;    //Posteriors of the above
   private Matrix postP;
   private Matrix smoothX;
   private Matrix smoothP;
   private Matrix u;        //The controls. u = [calories, exercise].
   private Matrix z;        // The observation. z = [scaleWeight]
   
   private boolean calsRecorded; //Says whether or not the number of calories was recorded.
   private boolean zRecorded; //Says whether or not scale weight was recorded.
    private boolean exerciseRecorded; //Says whether or not exercise was recorded.

   //The main constructor that takes the previous day as its only argument.

   Day(Day prevDay){

      //Set the new date.
   
      LocalDate prevDate = prevDay.getDate();
      date = prevDate.plusDays(1);
   
      //Indicate that nothing has been recorded yet.
      calsRecorded = false;
      zRecorded = false;
       exerciseRecorded = false;

      //Get previous postX and postP.

      Matrix prevPostX = prevDay.getPostX();
      Matrix prevPostP = prevDay.getPostP();
      Matrix prevU = prevDay.getU();
       Boolean prevURecorded = prevDay.caloriesRecorded();

      //Set priorX and priorP
 
      priorX = computePriorX(prevPostX, prevU, prevURecorded);
      priorP = computePriorP(prevPostP, prevURecorded);
   
      //Set default value of u.
      double[][] uVals = {{prevDay.getNEEE()},{0}};
      u = new Matrix(uVals);

      //instantiate the z as matrix of 0s
      double[][] zVals = {{0}};
      z = new Matrix(zVals);

      //Now that we've set the priors, we can update the posteriors. 
      //This will automatically update everything after it.

      updatePosteriors();   

   }

   //The constructor for the first day.
   //Weight is in pounds.
   //Height is in inches.
   //Age is in years.
   
   Day(LocalDate newDate, double weight, double height, int age, int gender){

      date = newDate;


      //Indicate that nothing has been recorded yet.
      calsRecorded = false;
      zRecorded = false;
       exerciseRecorded = false;

      //Compute the "stupid" estimate of the user's neee using cross sectional formula.

      double neeeEstimate = computeNEEE(weight, height, age, gender);

      //We'll act as if their initial weight recording was taken on the morning of the first day. 
      //The initial variance should be high enough that it doesn't matter.
   
      double[][] priorXVals = {{weight},{neeeEstimate}};
      priorX = new Matrix(priorXVals);
      priorP = new Matrix(DayHelper.initialPVals);

      //Set default value of u.
      double[][] uVals = {{neeeEstimate},{0}};
      u = new Matrix(uVals);


      //Record weight as z
      double[][] zVals = {{weight}};
      z = new Matrix(zVals);
       //zRecorded=true;

      //Compute posteriors
      updatePosteriors();

   }

   //Method to multiply two matrices

   private Matrix times(Matrix A, Matrix B){

       return A.times(B);
   }

   
   //Method to add two matrices
   private Matrix plus(Matrix A, Matrix B){

       return A.plus(B);
   }

   //Method to subtract one matrix from another (A,B) -> A-B
   private Matrix minus(Matrix A, Matrix B){
      return A.minus(B);
   }

   //Method to compute priorX from previous posterior X
   private Matrix computePriorX(Matrix prevPostX, Matrix prevU, Boolean prevURecorded){

        if(prevURecorded) {
            return plus(times(DayHelper.F, prevPostX), times(DayHelper.B, prevU));
        }else{
            return prevPostX;
        }
   }

   //Method to compute priorP from previous posterior P

   private Matrix computePriorP(Matrix prevPostP, Boolean prevURecorded){

       Matrix newPriorP;
       if(prevURecorded) {
           newPriorP = plus(times(DayHelper.F, times(prevPostP, DayHelper.F.transpose())), DayHelper.Q);
       }else{
           newPriorP = plus(times(DayHelper.F, times(prevPostP, DayHelper.F.transpose())), DayHelper.QNoTracking);
       }

      //Make sure that P hasn't gotten too big.
      if(newPriorP.get(0,0) > 10*DayHelper.initialPVals[0][0] || newPriorP.get(1,1) > 10*DayHelper.initialPVals[1][1]){
      newPriorP = new Matrix(DayHelper.initialPVals);
      }
 
      return newPriorP;
   }

 
   //Method that returns today's estimate of neee. Should pull from posterior.
   double getNEEE(){
      return smoothX.get(1,0);

   }


   //Method for setting scale weight.

   void setScaleWeight(double weight){

      z.set(0,0,weight);
      zRecorded = true;

      //Update posterior
      updatePosteriors();

   }


   //This method computes posteriors.

   void updatePosteriors(){

       //If the scale weight has been recorded, then do the full update
      //Otherwise, just copy the priors to the posteriors.   
      if(zRecorded){
         //Compute innovation
         Matrix y = minus(z, times(DayHelper.H,priorX));

         //Compute innovation covariance
         Matrix S = plus(times(DayHelper.H, times(priorP,DayHelper.H.transpose())),DayHelper.R);

         //Compute optimal Kalman gain
         Matrix K = times(priorP,times(DayHelper.H.transpose(),S.inverse()));

         //compute state posterior
         postX = plus(priorX,times(K,y));

         //compute covariance posterior
         postP = times(minus(DayHelper.I,times(K,DayHelper.H)),priorP);

         //Copy the posteriors as the smoothed versions

         smoothX = postX.copy();
         smoothP = postP.copy();

      }
      else{
         postX = priorX.copy();
         postP = priorP.copy();
         smoothX = postX.copy();
         smoothP = postP.copy();
      }


   }


   //Computes an estimate of NEEE given weight, height, activity level, age and gender.
   // weight in lbs, height in inches, activity level from 
   private double computeNEEE(double weight, double height, int age, int gender){

         double neee = 0;

         //contribution from mass.
         neee = neee + DayHelper.NEEE_MASS_MULTIPLIER*weight;
     
         //contribution from height
         neee = neee + DayHelper.NEEE_HEIGHT_MULTIPLIER*height;
       
         //contribution from age
         neee = neee + DayHelper.NEEE_AGE_MULTIPLIER*age;
  
         //contribution from gender
 
         if(gender == DayHelper.MALE){
            neee = neee + DayHelper.NEEE_FROM_MALE;
         }
         else if(gender== DayHelper.FEMALE){
            neee = neee + DayHelper.NEEE_FROM_FEMALE;
         }else{
             neee = neee + DayHelper.NEEE_FROM_NO_GENDER;
         }

         //Switch from Basal to ``NEEE"
         neee = neee * DayHelper.DEFAULT_NEEE_ACTIVITY_LEVEL;
  
         return neee;

         
      }



   double getScaleWeight(){
      if(zRecorded){
         return z.get(0,0);
      }
      else{
         throw new IllegalStateException("Scale weight not set!");
      }
   }


   double getCalories(){
   
      if(calsRecorded){
         return u.get(0,0);
      }
      else{
         throw new IllegalStateException("Calories not set!");
      }
   }

   double getExercise(){
    
      return u.get(1,0);
   }

   
   void setCalories(double cals){

      u.set(0,0,cals);
      calsRecorded = true;
     
   }

   void setExercise(double exercise){
       exerciseRecorded = true;
      u.set(1,0,exercise);
      
   }

    boolean isExerciseRecorded(){ return exerciseRecorded;}

   
   
   double getWeightEst(){

      return smoothX.get(0,0);
   }


   LocalDate getDate(){
      return date;
   }


   public double getWeightEstSD(){

     return Math.sqrt(smoothP.get(0,0));
   }

   double getNEEESD(){
      return Math.sqrt(smoothP.get(1,1));
   }


   private Matrix getPostX(){
      return postX;
   }

   private Matrix getPostP(){
      return postP;
   }


   private Matrix getU(){
      return u;
   }


   boolean caloriesRecorded(){
      return calsRecorded;
   }


   boolean scaleWeightRecorded(){
      return zRecorded;
   }

    void setCaloriesRecorded(boolean wasRecorded){
      calsRecorded = wasRecorded;
   }

    void setScaleWeightRecorded(boolean wasRecorded){
      zRecorded = wasRecorded;
    }

    private Matrix getSmoothX(){
        return smoothX;
    }

    private Matrix getSmoothP(){
        return smoothP;
    }

    private Matrix getPriorX(){
        return priorX;
    }

    private Matrix getPriorP(){
        return priorP;
    }

    //Method that computes the smoothed estimates of state given the following day.
    void smooth(Day nextDay){
        Matrix nextSmoothX = nextDay.getSmoothX();
        Matrix nextSmoothP = nextDay.getSmoothP();
        Matrix nextPriorX = nextDay.getPriorX();
        Matrix nextPriorP = nextDay.getPriorP();

        Matrix C = postP.times(DayHelper.F.transpose()).times(nextPriorP.inverse());
        smoothX = plus(postX ,C.times(nextSmoothX.minus(nextPriorX)));
        smoothP = postP.plus(C.times(nextSmoothP.minus(nextPriorP)).times(C.transpose()));
    }

    void setExerciseRecorded(boolean exerciseRecorded) {
        this.exerciseRecorded = exerciseRecorded;
    }
}




 
