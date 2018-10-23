package controlledBurn;


import org.joda.time.LocalDate;

import java.io.Serializable;

import Jama.Matrix;

import static controlledBurn.Day3Helper.A_PARAM;
import static controlledBurn.Day3Helper.BEVERAGE_CALS_INDEX;
import static controlledBurn.Day3Helper.B_PARAM;
import static controlledBurn.Day3Helper.CUSTOM_EXERCISE_CALS_INDEX;
import static controlledBurn.Day3Helper.CUSTOM_FOOD_CALS_INDEX;
import static controlledBurn.Day3Helper.C_PARAM;
import static controlledBurn.Day3Helper.B;
import static controlledBurn.Day3Helper.BEVERAGE_MULTIPLIER;
import static controlledBurn.Day3Helper.CALORIES_PER_POUND;
import static controlledBurn.Day3Helper.DECAY_RATE;
import static controlledBurn.Day3Helper.DEFAULT_AGE_EST;
import static controlledBurn.Day3Helper.DEFAULT_HEIGHT_EST;
import static controlledBurn.Day3Helper.DEFAULT_INITIAL_P;
import static controlledBurn.Day3Helper.DEFAULT_NEEE_ACTIVITY_LEVEL;
import static controlledBurn.Day3Helper.DEFAULT_PREDICTED_SURPLUS_VARIANCE;
import static controlledBurn.Day3Helper.DEFAULT_SEX_EST;
import static controlledBurn.Day3Helper.DEFAULT_WEIGHT_EST;
import static controlledBurn.Day3Helper.EXERCISE_CALS_INDEX;
import static controlledBurn.Day3Helper.EXERCISE_MULTIPLIER;
import static controlledBurn.Day3Helper.FEMALE;
import static controlledBurn.Day3Helper.H;
import static controlledBurn.Day3Helper.LARGE_MEAL_CALS_INDEX;
import static controlledBurn.Day3Helper.LARGE_MEAL_MULTIPLIER;
import static controlledBurn.Day3Helper.LARGE_SNACK_CALS_INDEX;
import static controlledBurn.Day3Helper.LARGE_SNACK_MULTIPLIER;
import static controlledBurn.Day3Helper.MALE;
import static controlledBurn.Day3Helper.NEEE_AGE_MULTIPLIER;
import static controlledBurn.Day3Helper.NEEE_EST_INDEX;
import static controlledBurn.Day3Helper.NEEE_FROM_FEMALE;
import static controlledBurn.Day3Helper.NEEE_FROM_MALE;
import static controlledBurn.Day3Helper.NEEE_FROM_NO_GENDER;
import static controlledBurn.Day3Helper.NEEE_HEIGHT_MULTIPLIER;
import static controlledBurn.Day3Helper.NEEE_MASS_MULTIPLIER;
import static controlledBurn.Day3Helper.Q;
import static controlledBurn.Day3Helper.Q_NO_TRACKING;
import static controlledBurn.Day3Helper.REGULAR_MEAL_CALS_INDEX;
import static controlledBurn.Day3Helper.REGULAR_MEAL_MULTIPLIER;
import static controlledBurn.Day3Helper.SCALE_DEVIATION;
import static controlledBurn.Day3Helper.SMALL_MEAL_CALS_INDEX;
import static controlledBurn.Day3Helper.SMALL_MEAL_MULTIPLIER;
import static controlledBurn.Day3Helper.SMALL_SNACK_CALS_INDEX;
import static controlledBurn.Day3Helper.SMALL_SNACK_MULTIPLIER;
import static controlledBurn.Day3Helper.STATE_DIMENSION;
import static controlledBurn.Day3Helper.WEIGHT_EST_INDEX;

class Day3 implements Serializable{


    //Declare class variables.

    private LocalDate date;

    private Matrix priorX;   //prior mean of state.
    //State is [weight, neee, smallSnackCals, largeSnackCals, smallMealCals, regularMealCals,
    // largeMealCals, beverageCals, exCals]
    private Matrix priorP;   //prior variance-covariance of morning state
    private Matrix postX;    //Posteriors of the above
    private Matrix postP;
    private Matrix C;
    private Matrix smoothX;
    private Matrix smoothP;
    private Matrix u;        //The controls. u = [customFoodCalories, customExerciseCalories].
    private Matrix z;        // The observation. z = [scaleWeight]
    private int smallSnacks; //Number of smallSnacks eaten today.
    private int largeSnacks; //Number of largeSnacks.
    private int smallMeals; //Number of small meals.
    private int regularMeals; //Number of regular sized meals.
    private int largeMeals; //Number of large meals.
    private int beverages; //Number of beverages consumed.
    private int exercise; //Number of minutes of exercise.
    private double predictedSurplus; //Our estimate for the amount of food -exercise-tdee that user
    //user will consume *today*.
    private double predictedSurplusVar; //Our estimate of the var of food-exercise - tdee.



    //The main constructor that takes the previous day as its only argument.

    Day3(Day3 prevDay){

        //Set the new date.

        LocalDate prevDate = prevDay.getDate();
        date = prevDate.plusDays(1);


        smallSnacks = 0;
        largeSnacks =0;
        smallMeals = 0;
        regularMeals = 0;
        largeMeals = 0;
        beverages = 0;
        exercise = 0;


        //Get previous postX and postP.

        Matrix prevPostX = prevDay.getPostX();
        Matrix prevPostP = prevDay.getPostP();
        Matrix prevU = prevDay.getU();
        Boolean prevFoodRecorded = prevDay.getFoodRecorded();

        Matrix prevF = prevDay.computeF();

        //Set priorX and priorP

        priorX = computePriorX(prevPostX, prevU, prevF, prevFoodRecorded);
        priorP = computePriorP(prevPostP, prevF, prevFoodRecorded);

        //Set default value of u.
        double[][] uVals = {{0},{0}};
        u = new Matrix(uVals);

        //instantiate the z as matrix of 0s
        double[][] zVals = {{0}};
        z = new Matrix(zVals);

        //Now that we've set the priors, we can update the posteriors.
        //This will automatically update everything after it.

        updatePosteriors();

        //Update estimates for net consumption and its standard deviation.

        if(prevDay.getFoodRecorded()) {
            double prevSurplus = prevDay.getTotalCalories()-prevDay.getExercise()-prevDay.getNEEE();

            predictedSurplus = DECAY_RATE * prevDay.getPredictedSurplus() + (1.- DECAY_RATE) * prevSurplus;
            predictedSurplusVar = DECAY_RATE * prevDay.getPredictedSurplusVar() +
                    (1.-DECAY_RATE) * Math.pow(prevSurplus - prevDay.getPredictedSurplus(), 2);
        }else{
            predictedSurplus = DECAY_RATE * prevDay.getPredictedSurplus();
            predictedSurplusVar = Math.min((2.-DECAY_RATE)*prevDay.getPredictedSurplusVar(), DEFAULT_PREDICTED_SURPLUS_VARIANCE*Math.pow(getPriorNEEE(),2));

        }

    }





    //The constructor for the first day.
    //Weight is in pounds.
    //Height is in inches.
    //Age is in years.

    Day3(LocalDate newDate, double weight, double height, int age, int gender){

        date = newDate;



        smallSnacks = 0;
        largeSnacks =0;
        smallMeals = 0;
        regularMeals = 0;
        largeMeals = 0;
        beverages = 0;
        exercise = 0;

        //Compute the "stupid" estimate of the user's neee using cross sectional formula.

        double neeeEstimate = computeNEEE(weight, height, age, gender);

        predictedSurplus = 0.;
        predictedSurplusVar = DEFAULT_PREDICTED_SURPLUS_VARIANCE*Math.pow(neeeEstimate,2);

        double modExEst = weight*EXERCISE_MULTIPLIER;

        //We'll act as if their initial weight recording was taken on the morning of the first day.
        //The initial variance should be high enough that it doesn't matter.

        double[][] priorXVals = {{weight},
                {neeeEstimate},
                {neeeEstimate*SMALL_SNACK_MULTIPLIER},
                {neeeEstimate*LARGE_SNACK_MULTIPLIER},
                {neeeEstimate*SMALL_MEAL_MULTIPLIER},
                {neeeEstimate*REGULAR_MEAL_MULTIPLIER},
                {neeeEstimate*LARGE_MEAL_MULTIPLIER},
                {neeeEstimate*BEVERAGE_MULTIPLIER},
                {modExEst}};
        priorX = new Matrix(priorXVals);
        priorP = DEFAULT_INITIAL_P.times(Math.pow(getPriorNEEE(),2));

        //Set default value of u.
        double[][] uVals = {{0},{0}};
        u = new Matrix(uVals);


        //Record weight as z
        double[][] zVals = {{0}};
        z = new Matrix(zVals);
        //weightRecorded=true;

        //Compute posteriors
        updatePosteriors();

    }

    //A constructor for Day3 that makes the initial variance very high that only requires a date.

    Day3(LocalDate newDate){
        date = newDate;



        smallSnacks = 0;
        largeSnacks =0;
        smallMeals = 0;
        regularMeals = 0;
        largeMeals = 0;
        beverages = 0;
        exercise = 0;

        Double weightEstimate = DEFAULT_WEIGHT_EST;

        //Compute the "stupid" estimate of the user's neee using cross sectional formula.

        double neeeEstimate = computeNEEE(weightEstimate, DEFAULT_HEIGHT_EST, DEFAULT_AGE_EST, DEFAULT_SEX_EST);
        predictedSurplus = 0.;
        predictedSurplusVar = DEFAULT_PREDICTED_SURPLUS_VARIANCE*Math.pow(neeeEstimate,2);

        //We'll act as if their initial weight recording was taken on the morning of the first day.
        //The initial variance should be high enough that it doesn't matter.

        double[][] priorXVals = {{weightEstimate},
                {neeeEstimate},
                {neeeEstimate*SMALL_SNACK_MULTIPLIER},
                {neeeEstimate*LARGE_SNACK_MULTIPLIER},
                {neeeEstimate*SMALL_MEAL_MULTIPLIER},
                {neeeEstimate*REGULAR_MEAL_MULTIPLIER},
                {neeeEstimate*LARGE_MEAL_MULTIPLIER},
                {neeeEstimate*BEVERAGE_MULTIPLIER},
                {weightEstimate*EXERCISE_MULTIPLIER}};

        priorX = new Matrix(priorXVals);
        priorP = DEFAULT_INITIAL_P.times(Math.pow(neeeEstimate,2));

        //Set default value of u.
        double[][] uVals = {{0},{0}};
        u = new Matrix(uVals);


        //Record weight as z
        double[][] zVals = {{0}};
        z = new Matrix(zVals);
        //weightRecorded=true;

        //Compute posteriors
        updatePosteriors();
    }

    //Constructor that uses a Day to produce a Day3.

    Day3(Day day){
        date = day.getDate();


        double weightEstimate = day.getWeightEst();
        double neeeEstimate = day.getNEEE();
        double[][] priorXVals = {{weightEstimate},
                {neeeEstimate},
                {neeeEstimate*SMALL_SNACK_MULTIPLIER},
                {neeeEstimate*LARGE_SNACK_MULTIPLIER},
                {neeeEstimate*SMALL_MEAL_MULTIPLIER},
                {neeeEstimate*REGULAR_MEAL_MULTIPLIER},
                {neeeEstimate*LARGE_MEAL_MULTIPLIER},
                {neeeEstimate*BEVERAGE_MULTIPLIER},
                {weightEstimate*EXERCISE_MULTIPLIER}};

        priorX = new Matrix(priorXVals);
        priorP = DEFAULT_INITIAL_P.times(Math.pow(getPriorNEEE(),2));

        postX = priorX.copy();
        postX.set(WEIGHT_EST_INDEX,0, weightEstimate);
        postX.set(NEEE_EST_INDEX,0,neeeEstimate);

        postP = priorP.copy();

        smoothX = postX.copy();
        smoothP = postP.copy();
        double calories = 0;

        if(day.caloriesRecorded()){
            calories = day.getCalories();
        }
        double[][] uVals = {{calories},{day.getExercise()}};
        u = new Matrix(uVals);

        double scaleWeight;

        if(day.scaleWeightRecorded()) {
            scaleWeight = day.getScaleWeight();
        } else{
            scaleWeight = 0;
        }

        double[][] zVals = {{scaleWeight}};
        z = new Matrix(zVals);

        smallSnacks = 0;
        largeSnacks = 0;
        smallMeals = 0;
        regularMeals = 0;
        largeMeals = 0;
        beverages = 0;
        exercise = 0;

        predictedSurplus = 0.;
        predictedSurplusVar = DEFAULT_PREDICTED_SURPLUS_VARIANCE*Math.pow(neeeEstimate,2);

    }



    //A method that corrects the priors and posteriors for the current day when the info from the
    //previous day may have changed.

    void updateFromPrevDay(Day3 prevDay){


        //Update estimates for net consumption and its standard deviation.

        if(prevDay.getFoodRecorded()) {
            double prevSurplus = prevDay.getTotalCalories()-prevDay.getExercise()-prevDay.getNEEE();

            predictedSurplus = DECAY_RATE * prevDay.getPredictedSurplus() + (1.-DECAY_RATE) * (prevSurplus);
            predictedSurplusVar = DECAY_RATE * prevDay.getPredictedSurplusVar() +
                    (1.-DECAY_RATE) * Math.pow(prevSurplus - prevDay.getPredictedSurplus(), 2);

        }else{
            predictedSurplus = DECAY_RATE*prevDay.getPredictedSurplus();
            predictedSurplusVar = Math.min((2.-DECAY_RATE)*prevDay.getPredictedSurplusVar(),
                    DEFAULT_PREDICTED_SURPLUS_VARIANCE*Math.pow(prevDay.getNEEE(),2));
        }
        //Get previous postX and postP.

        Matrix prevPostX = prevDay.getPostX();
        Matrix prevPostP = prevDay.getPostP();
        Matrix prevU = prevDay.getU();
        Boolean prevFoodRecorded = prevDay.getFoodRecorded();

        Matrix prevF = prevDay.computeF();

        //Set priorX and priorP

        priorX = computePriorX(prevPostX, prevU, prevF, prevFoodRecorded);
        priorP = computePriorP(prevPostP, prevF, prevFoodRecorded);

        //Now that we've set the priors, we can update the posteriors.

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
    private Matrix computePriorX(Matrix prevPostX, Matrix prevU, Matrix prevF, Boolean prevCalsRecorded){

        Matrix newPriorX;

        if(prevCalsRecorded) {
            newPriorX = plus(times(prevF, prevPostX), times(B, prevU));
        }else{
            newPriorX = prevPostX;
        }

        return newPriorX;
    }

    //Method to compute priorP from previous posterior P

    private Matrix computePriorP(Matrix prevPostP, Matrix prevF, Boolean prevFoodRecorded){

        Matrix newPriorP;

        if(prevFoodRecorded) {
            newPriorP = plus(times(prevF, times(prevPostP, prevF.transpose())), Q.times(Math.pow(getPriorNEEE(),2)));
        }else{
            newPriorP = plus(prevPostP, Q_NO_TRACKING.times(Math.pow(getPriorNEEE(),2)));
        }

        //Make sure that P hasn't gotten too big.
        if(newPriorP.get(WEIGHT_EST_INDEX, WEIGHT_EST_INDEX) > DEFAULT_INITIAL_P.get(WEIGHT_EST_INDEX, WEIGHT_EST_INDEX)*Math.pow(getPriorNEEE(),2)) {
            newPriorP = DEFAULT_INITIAL_P.times(Math.pow(getPriorNEEE(),2));
        }

        return newPriorP;
    }


    //Method that returns today's estimate of neee. Should pull from posterior.
    double getNEEE(){
        return smoothX.get(NEEE_EST_INDEX,0);

    }

    //Method that returns today's number of consumed smallSnacks

    int getSmallSnacks(){
        return smallSnacks;
    }


    //Method that returns today's number of consumed largeSnacks

    int getLargeSnacks(){
        return largeSnacks;
    }

    //Method that returns today's number of small meals.

    int getSmallMeals(){
        return smallMeals;
    }

    //Method that returns today's number of regularMeals.

    int getRegularMeals(){
        return regularMeals;
    }

    //Method that returns today's number of large meals.

    int getLargeMeals(){
        return largeMeals;
    }

    //Method that returns today's number of beverages.

    int getBeverages(){
        return beverages;
    }


    //Method that returns today's minutes of exercise.

    int getExercise(){
        return exercise;
    }


    //Method for setting scale weight.

    void setScaleWeight(double weight){

        z.set(0,0,weight);
        updatePosteriors();
    }


    //This method computes posteriors.

    void updatePosteriors(){

        //If the scale weight has been recorded, then do the full update
        //Otherwise, just copy the priors to the posteriors.
        if(scaleWeightRecorded()){
            //Compute innovation
            Matrix y = minus(z, times(H,priorX));

            //Compute innovation covariance
            Matrix S = plus(times(H, times(priorP,H.transpose())),computeR(y));

            //Compute optimal Kalman gain
            Matrix K = times(priorP,times(H.transpose(),S.inverse()));

            //compute state posterior
            postX = plus(priorX,times(K,y));

            //compute covariance posterior
            postP = times(minus(Matrix.identity(STATE_DIMENSION,STATE_DIMENSION),times(K,H)),priorP);
        }
        else{
            postX = priorX.copy();
            postP = priorP.copy();
        }

        Matrix nextPriorP;
        Matrix F = computeF();

        if(getFoodRecorded()) {
            nextPriorP = plus(times(F, times(postP, F.transpose())), Q.times(Math.pow(getPriorNEEE(),2)));
        }else{
            nextPriorP = plus(postP, Q_NO_TRACKING.times(Math.pow(getPriorNEEE(),2)));
        }

        C = nextPriorP.solve(postP.times(computeF().transpose())).transpose();

        //Copy the posteriors as the smoothed versions
        smoothX = postX.copy();
        smoothP = postP.copy();

    }


    //Computes an estimate of NEEE given weight, height, activity level, age and gender.
    // weight in lbs, height in inches, activity level from
    private double computeNEEE(double weight, double height, int age, int gender){

        double neee = 0;

        //contribution from mass.
        neee = neee + NEEE_MASS_MULTIPLIER*weight;

        //contribution from height
        neee = neee + NEEE_HEIGHT_MULTIPLIER*height;

        //contribution from age
        neee = neee + NEEE_AGE_MULTIPLIER*age;

        //contribution from gender

        if(gender == MALE){
            neee = neee + NEEE_FROM_MALE;
        }
        else if(gender== FEMALE){
            neee = neee + NEEE_FROM_FEMALE;
        }else{
            neee = neee + NEEE_FROM_NO_GENDER;
        }

        //Switch from Basal to ``NEEE"
        neee = neee * DEFAULT_NEEE_ACTIVITY_LEVEL;

        return neee;

    }


    double getScaleWeight(){
        return z.get(0,0);
    }


    double getCustomCalories(){ return u.get(CUSTOM_FOOD_CALS_INDEX,0);}

    double getTotalCalories(){
        double cals = 0.;

        cals += smallSnacks*getSmallSnackCals();
        cals += largeSnacks*getLargeSnackCals();
        cals += smallMeals*getSmallMealCals();
        cals += regularMeals*getRegularMealCals();
        cals += largeMeals*getLargeMealCals();
        cals += beverages*getBeverageCals();
        cals += getCustomCalories();

        return cals;
    }

    double getTotalExerciseCalories(){
        return getCustomExercise() + getExercise()*getExerciseCals();
    }

    double getCustomExercise(){

        return u.get(CUSTOM_EXERCISE_CALS_INDEX,0);
    }


    void setCustomCalories(double cals){

        u.set(CUSTOM_FOOD_CALS_INDEX,0,cals);
    }

    void setCustomExercise(double customExercise){

        u.set(CUSTOM_EXERCISE_CALS_INDEX,0,customExercise);
    }

    boolean isCustomExerciseRecorded(){
        return u.get(1,0) >0;
    }



    double getWeightEst(){
        return smoothX.get(0,0);
    }

    LocalDate getDate(){
        return date;
    }


    public double getWeightEstSD(){
        return Math.sqrt(smoothP.get(0,0));
    }

    private double getPriorWeightVar(){
        return priorP.get(0,0);
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

    boolean scaleWeightRecorded(){
        return getScaleWeight()>0;
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
    void smooth(Day3 nextDay){

        Matrix nextSmoothX = nextDay.getSmoothX();
        Matrix nextSmoothP = nextDay.getSmoothP();
        Matrix nextPriorX = nextDay.getPriorX();
        Matrix nextPriorP = nextDay.getPriorP();

        smoothX = plus(postX ,C.times(nextSmoothX.minus(nextPriorX)));
        smoothP = postP.plus(C.times(nextSmoothP.minus(nextPriorP)).times(C.transpose()));
    }


    public void setSmallSnacks(int smallSnacks) {

        this.smallSnacks = smallSnacks;
        updatePosteriors();
    }

    public void setLargeSnacks(int largeSnacks) {

        this.largeSnacks = largeSnacks;
        updatePosteriors();
    }

    public void setSmallMeals(int smallMeals) {

        this.smallMeals = smallMeals;
        updatePosteriors();
    }

    public void setRegularMeals(int regularMeals) {

        this.regularMeals = regularMeals;
        updatePosteriors();
    }

    public void setLargeMeals(int largeMeals) {

        this.largeMeals = largeMeals;
        updatePosteriors();
    }

    public void setBeverages(int beverages) {

        this.beverages = beverages;
        updatePosteriors();
    }

    public void setExercise(int exercisePar) {

        exercise = exercisePar;
        updatePosteriors();
    }

    double getSmallSnackCals(){
        return postX.get(SMALL_SNACK_CALS_INDEX,0);
    }

    double getLargeSnackCals(){
        return postX.get(LARGE_SNACK_CALS_INDEX,0);
    }

    double getSmallMealCals(){
        return postX.get(SMALL_MEAL_CALS_INDEX,0);
    }

    double getRegularMealCals(){
        return postX.get(REGULAR_MEAL_CALS_INDEX,0);
    }

    double getLargeMealCals(){
        return postX.get(LARGE_MEAL_CALS_INDEX,0);
    }

    double getBeverageCals(){
        return postX.get(BEVERAGE_CALS_INDEX,0);
    }


    double getExerciseCals(){
        return postX.get(EXERCISE_CALS_INDEX,0);
    }
    Matrix computeF(){

        Matrix F = Matrix.identity(STATE_DIMENSION,STATE_DIMENSION);

        F.set(WEIGHT_EST_INDEX, NEEE_EST_INDEX,-1./CALORIES_PER_POUND);
        F.set(WEIGHT_EST_INDEX,SMALL_SNACK_CALS_INDEX,(double) smallSnacks/CALORIES_PER_POUND);
        F.set(WEIGHT_EST_INDEX,LARGE_SNACK_CALS_INDEX,(double) largeSnacks /CALORIES_PER_POUND);
        F.set(WEIGHT_EST_INDEX,SMALL_MEAL_CALS_INDEX,(double) smallMeals /CALORIES_PER_POUND);
        F.set(WEIGHT_EST_INDEX,REGULAR_MEAL_CALS_INDEX,(double) regularMeals /CALORIES_PER_POUND);
        F.set(WEIGHT_EST_INDEX,LARGE_MEAL_CALS_INDEX,(double) largeMeals /CALORIES_PER_POUND);
        F.set(WEIGHT_EST_INDEX,BEVERAGE_CALS_INDEX,(double) beverages/CALORIES_PER_POUND);
        F.set(WEIGHT_EST_INDEX,EXERCISE_CALS_INDEX,-1.*exercise/CALORIES_PER_POUND);

        return F;
    }


    public double getPredictedSurplus() {
        return predictedSurplus;
    }

    public double getPredictedNetIntake(){
        return (getNEEE() + predictedSurplus);
    }

    public double getPredictedSurplusVar() {
        return predictedSurplusVar;
    }

    public void setPredictedSurplusVar(double predictedSurplusVar) {
        this.predictedSurplusVar = predictedSurplusVar;
    }

    public double getPredictedNetIntakeVar(){
        return predictedSurplusVar;
    }

    private Boolean getFoodRecorded() {

        return (getTotalCalories() > 0);
    }


    public double getPriorNEEE(){
        return priorX.get(1,0);
    }

    public double getPriorWeight(){
        return priorX.get(0,0);
    }

    public Matrix computeR(Matrix y){
        double r = SCALE_DEVIATION*getPriorWeight();

        double skepticalSD = Math.sqrt(r*r + getPriorWeightVar());

        double[][] rVals = {{Math.pow(r*sigmoidFunction(y.get(0,0)/skepticalSD),2)}};

        return new Matrix(rVals);
    }

    public double sigmoidFunction(double w){
        return 1+A_PARAM/(1+Math.exp(C_PARAM - B_PARAM*w));
    }
}





