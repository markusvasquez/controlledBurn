package controlledBurn;


import org.joda.time.LocalDate;

import java.io.Serializable;

import Jama.Matrix;

import static controlledBurn.Day2Helper.A_PARAM;
import static controlledBurn.Day2Helper.BEVERAGE_INDEX;
import static controlledBurn.Day2Helper.BEVERAGE_MULTIPLIER;
import static controlledBurn.Day2Helper.B_PARAM;
import static controlledBurn.Day2Helper.CALORIES_PER_POUND;
import static controlledBurn.Day2Helper.C_PARAM;
import static controlledBurn.Day2Helper.DECAY_RATE;
import static controlledBurn.Day2Helper.DEFAULT_AGE_EST;
import static controlledBurn.Day2Helper.DEFAULT_HEIGHT_EST;
import static controlledBurn.Day2Helper.DEFAULT_PREDICTED_SURPLUS_VARIANCE;
import static controlledBurn.Day2Helper.DEFAULT_SEX_EST;
import static controlledBurn.Day2Helper.DEFAULT_WEIGHT_EST;
import static controlledBurn.Day2Helper.HEAVY_MEAL_INDEX;
import static controlledBurn.Day2Helper.HEAVY_MEAL_MULTIPLIER;
import static controlledBurn.Day2Helper.LIGHT_MEAL_INDEX;
import static controlledBurn.Day2Helper.LIGHT_MEAL_MULTIPLIER;
import static controlledBurn.Day2Helper.MEAL_MULTIPLIER;
import static controlledBurn.Day2Helper.MOD_EXERCISE_MULTIPLIER;
import static controlledBurn.Day2Helper.REG_MEAL_INDEX;
import static controlledBurn.Day2Helper.SMALL_SNACK_INDEX;
import static controlledBurn.Day2Helper.SMALL_SNACK_MULTIPLIER;
import static controlledBurn.Day2Helper.SNACK_INDEX;
import static controlledBurn.Day2Helper.SNACK_MULTIPLIER;
import static controlledBurn.Day2Helper.VIG_EXERCISE_MULTIPLIER;

class Day2 implements Serializable{



    //Declare class variables.

    private LocalDate date;

    private Matrix priorX;   //prior mean of state.
    //State is [weight, neee, smallSnackCals, snackCals, lightMealCals, regMealCals, heavyMealCals, beverageCals,
    // lightExCals, modExCals, intenseExCals]
    private Matrix priorP;   //prior variance-covariance of morning state
    private Matrix postX;    //Posteriors of the above
    private Matrix postP;
    private Matrix smoothX;
    private Matrix smoothP;
    private Matrix u;        //The controls. u = [customCalories, customExercise].
    private Matrix z;        // The observation. z = [scaleWeight]
    private int smallSnacks; //Number of smallSnacks eaten today.
    private int snacks; //Number of snacks.
    private int lightMeals; //Number of light meals.
    private int regMeals; //Number of regular sized meals.
    private int heavyMeals; //Number of heavy meals.
    private int beverages; //Number of beverages consumed.
    private int moderateExercise; //Number of minues of moderate exercise.
    private int vigorousExercise; //Number of minutes of intense exercise.
    private double predictedSurplus; //Our estimate for the amount of food -exercise-tdee that user
                                        //user will consume *today*.
    private double predictedSurplusVar; //Our estimate of the var of food-exercise - tdee.



    //The main constructor that takes the previous day as its only argument.

    Day2(Day2 prevDay){

        //Set the new date.

        LocalDate prevDate = prevDay.getDate();
        date = prevDate.plusDays(1);


        smallSnacks = 0;
        snacks=0;
        lightMeals = 0;
        regMeals = 0;
        heavyMeals = 0;
        beverages = 0;
        moderateExercise = 0;
        vigorousExercise = 0;


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
            double prevSurplus = prevDay.getCalories()-prevDay.getExercise()-prevDay.getNEEE();

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

    Day2(LocalDate newDate, double weight, double height, int age, int gender){

        date = newDate;



        smallSnacks = 0;
        snacks=0;
        lightMeals = 0;
        regMeals = 0;
        heavyMeals = 0;
        beverages = 0;
        moderateExercise = 0;
        vigorousExercise = 0;

        //Compute the "stupid" estimate of the user's neee using cross sectional formula.

        double neeeEstimate = computeNEEE(weight, height, age, gender);

        predictedSurplus = 0.;
        predictedSurplusVar = DEFAULT_PREDICTED_SURPLUS_VARIANCE*Math.pow(neeeEstimate,2);

        double modExEst = weight*MOD_EXERCISE_MULTIPLIER;
        double vigExEst = weight*VIG_EXERCISE_MULTIPLIER;

        //We'll act as if their initial weight recording was taken on the morning of the first day.
        //The initial variance should be high enough that it doesn't matter.

        double[][] priorXVals = {{weight},
                {neeeEstimate},
                {neeeEstimate*SMALL_SNACK_MULTIPLIER},
                {neeeEstimate*SNACK_MULTIPLIER},
                {neeeEstimate*LIGHT_MEAL_MULTIPLIER},
                {neeeEstimate*MEAL_MULTIPLIER},
                {neeeEstimate*HEAVY_MEAL_MULTIPLIER},
                {neeeEstimate*BEVERAGE_MULTIPLIER},
                {modExEst},
                {vigExEst}};
        priorX = new Matrix(priorXVals);
        priorP = Day2Helper.DEFAULT_INITIAL_P.times(Math.pow(getPriorNEEE(),2));

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

    //A constructor for Day2 that makes the initial variance very high that only requires a date.

    Day2(LocalDate newDate){
        date = newDate;



        smallSnacks = 0;
        snacks=0;
        lightMeals = 0;
        regMeals = 0;
        heavyMeals = 0;
        beverages = 0;
        moderateExercise = 0;
        vigorousExercise = 0;

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
                {neeeEstimate*SNACK_MULTIPLIER},
                {neeeEstimate*LIGHT_MEAL_MULTIPLIER},
                {neeeEstimate*MEAL_MULTIPLIER},
                {neeeEstimate*HEAVY_MEAL_MULTIPLIER},
                {neeeEstimate*BEVERAGE_MULTIPLIER},
                {weightEstimate*MOD_EXERCISE_MULTIPLIER},
                {weightEstimate*VIG_EXERCISE_MULTIPLIER}};
        priorX = new Matrix(priorXVals);
        priorP = Day2Helper.DEFAULT_INITIAL_P.times(Math.pow(neeeEstimate,2));

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

    //Constructor that uses a Day to produce a Day2.

    Day2(Day day){
        date = day.getDate();


        double weightEstimate = day.getWeightEst();
        double neeeEstimate = day.getNEEE();
        double[][] priorXVals = {{weightEstimate},
                {neeeEstimate},
                {neeeEstimate*SMALL_SNACK_MULTIPLIER},
                {neeeEstimate*SNACK_MULTIPLIER},
                {neeeEstimate*LIGHT_MEAL_MULTIPLIER},
                {neeeEstimate*MEAL_MULTIPLIER},
                {neeeEstimate*HEAVY_MEAL_MULTIPLIER},
                {neeeEstimate*BEVERAGE_MULTIPLIER},
                {weightEstimate*MOD_EXERCISE_MULTIPLIER},
                {weightEstimate*VIG_EXERCISE_MULTIPLIER}};

        priorX = new Matrix(priorXVals);
        priorP = Day2Helper.DEFAULT_INITIAL_P.times(Math.pow(getPriorNEEE(),2));

        postX = priorX.copy();
        postX.set(0,0, weightEstimate);
        postX.set(1,0,neeeEstimate);

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
        snacks = 0;
        lightMeals = 0;
        regMeals = 0;
        heavyMeals = 0;
        beverages = 0;
        moderateExercise = 0;
        vigorousExercise = 0;

        predictedSurplus = 0.;
        predictedSurplusVar = DEFAULT_PREDICTED_SURPLUS_VARIANCE*Math.pow(neeeEstimate,2);

    }

    //A method that corrects the priors and posteriors for the current day when the info from the
    //previous day may have changed.

    void updateFromPrevDay(Day2 prevDay){


        //Update estimates for net consumption and its standard deviation.

        if(prevDay.getFoodRecorded()) {
            double prevSurplus = prevDay.getCalories()-prevDay.getExercise()-prevDay.getNEEE();

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
            newPriorX = plus(times(prevF, prevPostX), times(Day2Helper.B, prevU));
        }else{
            newPriorX = prevPostX;
        }

        return newPriorX;
    }

    //Method to compute priorP from previous posterior P

    private Matrix computePriorP(Matrix prevPostP, Matrix prevF, Boolean prevFoodRecorded){

        Matrix newPriorP;

        if(prevFoodRecorded) {
            newPriorP = plus(times(prevF, times(prevPostP, prevF.transpose())), Day2Helper.Q.times(Math.pow(getPriorNEEE(),2)));
        }else{
            newPriorP = plus(prevPostP, Day2Helper.Q_NO_TRACKING.times(Math.pow(getPriorNEEE(),2)));
        }

        //Make sure that P hasn't gotten too big.
        if(newPriorP.get(0,0) > Day2Helper.DEFAULT_INITIAL_P.get(0,0)*Math.pow(getPriorNEEE(),2) ||
                newPriorP.get(1,1) > 10.*Day2Helper.DEFAULT_INITIAL_P.get(1,1)*Math.pow(getPriorNEEE(),2)){
            newPriorP = Day2Helper.DEFAULT_INITIAL_P.times(Math.pow(getPriorNEEE(),2));
        }

        return newPriorP;
    }


    //Method that returns today's estimate of neee. Should pull from posterior.
    double getNEEE(){
        return smoothX.get(1,0);

    }

    //Method that returns today's number of consumed smallSnacks

    int getSmallSnacks(){
        return smallSnacks;
    }


    //Method that returns today's number of consumed snacks

    int getSnacks(){
        return snacks;
    }

    //Method that returns today's number of light meals.

    int getLightMeals(){
        return lightMeals;
    }

    //Method that returns today's number of regMeals.

    int getRegMeals(){
        return regMeals;
    }

    //Method that returns today's number of heavy meals.

    int getHeavyMeals(){
        return heavyMeals;
    }

    //Method that returns today's number of beverages.

    int getBeverages(){
        return beverages;
    }

    //Method that returns today's minutes of lightExercise.

    //Method that returns today's minutes of moderateExercise.

    int getModerateExercise(){
        return moderateExercise;
    }

    //Method that returns today's minutes of vigorousExercise.

    int getVigorousExercise(){
        return vigorousExercise;
    }

    //Method for setting scale weight.

    void setScaleWeight(double weight){

        z.set(0,0,weight);

        //Update posterior
        updatePosteriors();

    }


    //This method computes posteriors.

    void updatePosteriors(){

        //If the scale weight has been recorded, then do the full update
        //Otherwise, just copy the priors to the posteriors.
        if(scaleWeightRecorded()){
            //Compute innovation
            Matrix y = minus(z, times(Day2Helper.H,priorX));

            //Compute innovation covariance
            Matrix S = plus(times(Day2Helper.H, times(priorP,Day2Helper.H.transpose())),computeR(y));

            //Compute optimal Kalman gain
            Matrix K = times(priorP,times(Day2Helper.H.transpose(),S.inverse()));

            //compute state posterior
            postX = plus(priorX,times(K,y));

            //compute covariance posterior
            postP = times(minus(Matrix.identity(10,10),times(K,Day2Helper.H)),priorP);


        }
        else{
            postX = priorX.copy();
            postP = priorP.copy();
        }

        //Copy the posteriors as the smoothed versions
        smoothX = postX.copy();
        smoothP = postP.copy();

    }


    //Computes an estimate of NEEE given weight, height, activity level, age and gender.
    // weight in lbs, height in inches, activity level from
    private double computeNEEE(double weight, double height, int age, int gender){

        double neee = 0;

        //contribution from mass.
        neee = neee + Day2Helper.NEEE_MASS_MULTIPLIER*weight;

        //contribution from height
        neee = neee + Day2Helper.NEEE_HEIGHT_MULTIPLIER*height;

        //contribution from age
        neee = neee + Day2Helper.NEEE_AGE_MULTIPLIER*age;

        //contribution from gender

        if(gender == Day2Helper.MALE){
            neee = neee + Day2Helper.NEEE_FROM_MALE;
        }
        else if(gender== Day2Helper.FEMALE){
            neee = neee + Day2Helper.NEEE_FROM_FEMALE;
        }else{
            neee = neee + Day2Helper.NEEE_FROM_NO_GENDER;
        }

        //Switch from Basal to ``NEEE"
        neee = neee * Day2Helper.DEFAULT_NEEE_ACTIVITY_LEVEL;

        return neee;

    }


    double getScaleWeight(){
        return z.get(0,0);
    }


    double getCustomCalories(){

            return u.get(0,0);

    }

    double getCalories(){
        Matrix mealNums = getDiaryInfo().getMatrix(0,5,0,0);
        Matrix calNums = getDiaryInfo().getMatrix(0,5,1,1);
        return getCustomCalories() + mealNums.transpose().times(calNums).get(0,0);
    }

    double getExercise(){
        Matrix exNums = getDiaryInfo().getMatrix(6,7,0,0);
        Matrix calNums = getDiaryInfo().getMatrix(6,7,1,1);
        return getCustomExercise() + exNums.transpose().times(calNums).get(0,0);
    }

    double getCustomExercise(){

        return u.get(1,0);
    }


    void setCustomCalories(double cals){

        u.set(0,0,cals);

    }

    void setCustomExercise(double customExercise){
        u.set(1,0,customExercise);

    }

    boolean isCustomExerciseRecorded(){
        return u.get(1,0) >0;}



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
    void smooth(Day2 nextDay){
        Matrix nextSmoothX = nextDay.getSmoothX();
        Matrix nextSmoothP = nextDay.getSmoothP();
        Matrix nextPriorX = nextDay.getPriorX();
        Matrix nextPriorP = nextDay.getPriorP();

        //Matrix C = postP.times(computeF().transpose()).times(nextPriorP.inverse());
        Matrix C = nextPriorP.solve(computeF().times(postP)).transpose();
        smoothX = plus(postX ,C.times(nextSmoothX.minus(nextPriorX)));
        smoothP = postP.plus(C.times(nextSmoothP.minus(nextPriorP)).times(C.transpose()));
    }


    public void setSmallSnacks(int smallSnacks) {

        this.smallSnacks = smallSnacks;
    }

    public void setSnacks(int snacks) {

        this.snacks = snacks;
    }

    public void setLightMeals(int lightMeals) {
        this.lightMeals = lightMeals;
    }

    public void setRegMeals(int regMeals) {
        this.regMeals = regMeals;
    }

    public void setHeavyMeals(int heavyMeals) {
        this.heavyMeals = heavyMeals;
    }

    public void setBeverages(int beverages) {
        this.beverages = beverages;
    }

    public void setModerateExercise(int moderateExercise) {
        this.moderateExercise = moderateExercise;
    }

    public void setVigorousExercise(int vigorousExercise) {
        this.vigorousExercise = vigorousExercise;
    }

    double getSmallSnackCals(){
        return postX.get(2,0);
    }

    double getSnackCals(){
        return postX.get(3,0);
    }

    double getLightMealCals(){
        return postX.get(4,0);
    }

    double getRegMealCals(){
        return postX.get(5,0);
    }

    double getHeavyMealCals(){
        return postX.get(6,0);
    }

    double getBeverageCals(){
        return postX.get(7,0);
    }


    double getModerateExerciseCals(){
        return postX.get(8,0);
    }

    double getVigorousExerciseCals(){
        return postX.get(9,0);
    }

    Matrix computeF(){
        Matrix F = Matrix.identity(10,10);

        F.set(0,1,-1./CALORIES_PER_POUND);
        F.set(0,2,(double) smallSnacks/CALORIES_PER_POUND);
        F.set(0,3,(double) snacks/CALORIES_PER_POUND);
        F.set(0,4,(double) lightMeals/CALORIES_PER_POUND);
        F.set(0,5,(double) regMeals/CALORIES_PER_POUND);
        F.set(0,6,(double) heavyMeals/CALORIES_PER_POUND);
        F.set(0,7,(double) beverages/CALORIES_PER_POUND);
        F.set(0,8,-1.*moderateExercise/CALORIES_PER_POUND);
        F.set(0,9,-1.*vigorousExercise/CALORIES_PER_POUND);

        return F;

    }

    Matrix getDiaryInfo(){
        double[][] matrixVals = {{getSmallSnacks(), getSmallSnackCals()},
                {getSnacks(), getSnackCals()},
                {getLightMeals(), getLightMealCals()},
                {getRegMeals(), getRegMealCals()},
                {getHeavyMeals(), getHeavyMealCals()},
                {getBeverages(), getBeverageCals()},
                {getModerateExercise(), getModerateExerciseCals()},
                {getVigorousExercise(), getVigorousExerciseCals()}};

        return new Matrix(matrixVals);
    }

    public double getPredictedSurplus() {
        return predictedSurplus;
    }

    public double getPredictedNetIntake(){
        return (getNEEE() + predictedSurplus);
    }

    public void setPredictedSurplus(double predictedSurplus) {
        this.predictedSurplus = predictedSurplus;
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

        return (getCalories() > 0);
    }

    public void recordFood(int[] a){
        setSmallSnacks(a[SMALL_SNACK_INDEX]);
        setSnacks(a[SNACK_INDEX]);
        setLightMeals(a[LIGHT_MEAL_INDEX]);
        setRegMeals(a[REG_MEAL_INDEX]);
        setHeavyMeals(a[HEAVY_MEAL_INDEX]);
        setBeverages(a[BEVERAGE_INDEX]);
    }

    public double getPriorNEEE(){
        return priorX.get(1,0);
    }

    public double getPriorWeight(){
        return priorX.get(0,0);
    }

    public Matrix computeR(Matrix y){
        double r = Day2Helper.SCALE_DEVIATION*getPriorWeight();


        double skepticalSD = Math.sqrt(r*r + getPriorWeightVar());

        double[][] rVals = {{Math.pow(r*sigmoidFunction(y.get(0,0)/skepticalSD),2)}};

        return new Matrix(rVals);
    }

    public double sigmoidFunction(double w){
        return 1+A_PARAM/(1+Math.exp(C_PARAM - B_PARAM*w));
    }
}





