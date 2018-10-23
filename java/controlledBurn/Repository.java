package controlledBurn;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import com.github.mikephil.charting.data.Entry;


public class Repository{
    private final String TAG = "Repository";

    //Backup and key stuff
    private final static String BACKUP = "backup";
    private final static String WEIGHT_KEY = "weight";
    private final static String SMALL_SNACK_KEY = "smallSnack";
    private final static String SNACK_KEY = "snack";
    private final static String SMALL_MEAL_KEY = "smallMeal";
    private final static String MEAL_KEY = "meal";
    private final static String LARGE_MEAL_KEY = "heavyMeal";
    private final static String BEVERAGE_KEY = "beverage";
    private final static String EXERCISE_KEY = "modEx";
    private final static String VIGOROUS_EXERCISE_KEY ="vigEx";
    private final static String OTHER_EXERCISE_KEY = "otherEx";
    private final static String OTHER_CALS_KEY = "otherCals";

    //Backup stuff for settings
    private final static String SETTINGS = "settings";
    private final static String FIRST_RECORD_DATE_KEY = "firstRecordDate";
    private final static String USES_LBS_KEY = "usesLbs";
    private final static String GOAL_KEY = "goal";
    private final static String SEEN_TUTORIAL_KEY = "seenTutorial";
    private final static String VERSION_KEY = "version";
    private final static String INTERNAL_TRACKING_KEY = "internalTracking"; //This will be true if they want to use cb for tracking calories.
    private final static String GOAL_WEIGHT_KEY = "goalWeight";

    private final static int CURRENT_VERSION = 3;

    private final static int NUM_YEARS_AFTER_EPOCH = 46;
    private final static int NUM_MONTHS_AFTER_EPOCH = 6;
    public final static LocalDate BEGINNING_OF_TIME
            = LocalDate.fromDateFields(new Date(0)).plusYears(NUM_YEARS_AFTER_EPOCH)
                .plusMonths(NUM_MONTHS_AFTER_EPOCH);



    private final static double LBS_TO_KGS_MULTIPLIER = 0.453592;
    private final static double KGS_TO_LBS_MULTIPLIER = 1./LBS_TO_KGS_MULTIPLIER;
    private final static double MINIMUM_DEFICIT = 125./2500.;
    private final static double GOAL_SD_MARGIN = 1.0;
    private final static double CALORIES_PER_POUND = 3500.;

    final static int LOSE_WEIGHT = 0;
    final static int MAINTAIN = 1;
    final static int GAIN_WEIGHT = 2;

    public final static int SMALL_SNACK_INDEX = 0;
    public final static int LARGE_SNACK_INDEX = 1;
    public final static int SMALL_MEAL_INDEX = 2;
    public final static int REGULAR_MEAL_INDEX = 3;
    public final static int LARGE_MEAL_INDEX = 4;
    public final static int BEVERAGE_INDEX = 5;



    final static int DAY_SWITCH_HOUR = 3;



    private URI path;
    //private LocalDate firstDate;
    private LocalDate firstRecordDate; //The first date for which a weight is recorded. This will be
                                        //the first date that is graphed.
    //private LocalDate lastDate;
    private boolean usesLbs;
    //private int graphLength;
    private int goal; //0 is lose weight. 1 is maintain. 2 is gain weight.
    private double goalWeight;
    //private boolean showFoodCals;
    //private boolean showExerciseCals;
    private boolean seenTutorial;
    private int version;
    private boolean internalTracking;


   /**
    *The main constructor. Likely, you will want to make a new one each time the app is pulled up.
    *The Path that it gets fed is the Path to the directory that 
    *stores the users information. The directory shouldn't change.
    */

   public Repository(URI pathPar){
       path = pathPar;
   }

   /**
    *set the scale weight on the given date. Weight is in lbs.
    */

   public void setScaleWeight(LocalDate date, double weight){
      Day3 day = readDay(date);
       Double weightToRecord = getWeightToRecord(weight);
      day.setScaleWeight(weightToRecord);
      writeDay(day);
       writeStringToBackupFile(date, WEIGHT_KEY, Double.toString(weightToRecord));

       if(date.isBefore(firstRecordDate)){
           setFirstRecordDate(date);
       }
   }

   /**Set the calories on the given date. 
    */
   public void setCustomCalories(LocalDate date, double calories){
      
      Day3 day = readDay(date);
      day.setCustomCalories(calories);
      writeDay(day);
       if(calories > 0) {
           writeStringToBackupFile(date, OTHER_CALS_KEY, Double.toString(calories));
       }else{
           removeBackupFile(date, OTHER_CALS_KEY);
       }
   }


   /**Set the exercise calories on a given date.
    */
   public void setCustomExercise(LocalDate date, double exercise){
   

      Day3 day = readDay(date);
      day.setCustomExercise(exercise);
      writeDay(day);
       if(exercise > 0) {
           writeStringToBackupFile(date, OTHER_EXERCISE_KEY, Double.toString(exercise));
       }else{
           removeBackupFile(date, OTHER_EXERCISE_KEY);
       }

   }


   /**Get the recorded scale weight in lbs on the given date.
    * the argument internal is true if this is being called from within repository and false otherwise
    */
   public double getScaleWeight(LocalDate date, boolean internal){
       return getWeightToReport(readDay(date).getScaleWeight(),internal);

   }


   /**Gets the calories on the given date.
    */
   public double getCustomCalories(LocalDate date){
       return readDay(date).getCustomCalories();
   }


    //Returns user's additional recorded exercise.
    public double getCustomExercise(LocalDate date){
        return readDay(date).getCustomExercise();
    }

   /**Returns true if the scale weight was recorded on this date.
    */
   
   public boolean scaleWeightRecorded(LocalDate date){
      return (readDay(date).getScaleWeight() >0.);
   }

   /**Returns true if calories were recorded on that day.
    */

   public boolean customCaloriesRecorded(LocalDate date){
      return (readDay(date).getCustomCalories() !=0);
   }

   /**Returns the best weight estimate that we have for the given date.
    *Does not require any information about the day to be recorded.
    * internal is true if this is called from within Repository and false otherwise.
    */
   public double getWeightEst(LocalDate date, Boolean internal){

      return getWeightToReport(readDay(date).getWeightEst(), internal);
   }
   
   /**Returns our estimate of the non-exercise energy expenditure.
    */
   public double getNEEE(LocalDate date){

      return readDay(date).getNEEE();
    }

    public boolean isCustomExerciseRecorded(LocalDate date){
        return readDay(date).isCustomExerciseRecorded();
    }



   //A method that saves a day to the file whose name is the day.getDate().toString().

   private void writeDay(Day3 day){

      URI newPath = path.resolve(day.getDate().toString());

      //First, delete the old file if it's there.
       if((new File(newPath)).exists()){
           new File(newPath).delete();
       }

      //Then save the new one.
      try{
          File file = new File(newPath);
          FileOutputStream fileOut = new FileOutputStream(file);
          ObjectOutputStream outputStream = new ObjectOutputStream(fileOut);
          outputStream.writeObject(day);
          fileOut.close();
          outputStream.close();
      } catch(IOException e){
          throw new RuntimeException(e);
      }

   }

   
   //A method that reads a day from its stored file.

   private Day3 readDay(LocalDate date) {

       FileInputStream fileIn;
       ObjectInputStream in;
       File file;

       try {
           file = new File(path.resolve(date.toString()));
           fileIn = new FileInputStream(file);

           in = new ObjectInputStream(fileIn);
           Object obj = in.readObject();
           in.close();
           fileIn.close();
           return (Day3) obj;

       } catch (Exception e) {
           return makeDay(date);
       }

   }


   
   //Method that creates and writes a new Day3 object at the given date. Overwrites any file that is there.
   //Also restores from backup.

   private Day3 makeDay(LocalDate date){

       Day3 newDay = new Day3(date);


       if(date.isAfter(BEGINNING_OF_TIME)){
           newDay.updateFromPrevDay(readDay(date.minusDays(1)));
       }
       newDay = restoreDayFromBackup(newDay);
       writeDay(newDay);

       return newDay;


   }

   //Method that propagates changes from the date given to the lastDate.

   public void propagate(LocalDate date, LocalDate toDate){

      LocalDate tempDate = date.plusDays(0);
      Day3 tempDay = readDay(tempDate);

      Day3 nextDay;

      while(toDate.isAfter(tempDate)){

          nextDay = readDay(tempDate.plusDays(1));
          nextDay.updateFromPrevDay(tempDay);

          //Now, tempDay3 is the correct follow-up day for tempDay, so write it to disk.
          writeDay(nextDay);

          //Then increment both tempDay and tempDate.
          tempDate = tempDate.plusDays(1);
          tempDay = nextDay;
      }
   }

    //Method that propagates changes from the date given to tomorrow.

    public void propagate(LocalDate date){

        propagate(date, getToday().plusDays(1));
    }

   //Method that indicates whether or not there is a saved Day for the corresponding date.

   private boolean dayExists(LocalDate date){
       File file = new File(path.resolve(date.toString()));
       return (file.exists() && file.length() !=0);
   }


   /** Returns true if the first day constructor for Repository has been called at some point
    *in time.
    */

    public boolean repositoryInitialized(){
       return (new File(path).listFiles().length >1);
   }

    public boolean backupExists(){
        return (new File(path.resolve(BACKUP)).exists());
    }


    //Returns true if the user has seen the tutorial once.
    public boolean getSeenTutorial(){
        return seenTutorial;
    }

    //records the fact that the user has seen the tutorial.
    public void setSeenTutorial(boolean seenTutorialPar){
       seenTutorial = seenTutorialPar;
        writeSettingToBackupFile(SEEN_TUTORIAL_KEY, Boolean.toString(seenTutorial));
    }

   /**
    * Returns the system to the state in which the scale weight for the given date was never set.
    */
   
   public void unsetScaleWeight(LocalDate date){


      Day3 day = readDay(date);
      
      //Make it look like scale weight was never recorded.
      day.setScaleWeight(0.0);

      //Write that date.
      writeDay(day);

       removeBackupFile(date, WEIGHT_KEY);

       if(date.equals(firstRecordDate)){
           setFirstRecordDate(findFirstRecordDate());
       }
   }

    public void unsetExercise(LocalDate date){
        setExercise(date,0);
        removeBackupFile(date, EXERCISE_KEY);
    }


    //Method that will smooth data starting with the smoothFromDate and going back to the smoothToDate
   public void smooth(LocalDate smoothFromDate, LocalDate smoothToDate) {

       //If for some reason we're trying to smooth the wrong way, do nothing.
       if (smoothFromDate.isAfter(smoothToDate)) {

           LocalDate tempDate = smoothFromDate.minusDays(1);
           Day3 firstTempDay = readDay(tempDate);
           Day3 secondTempDay = readDay(tempDate.plusDays(1));

           //smooth the firstTempDay calling the secondTempDay
           while(tempDate.isAfter(smoothToDate)){
               firstTempDay.smooth(secondTempDay);
               writeDay(firstTempDay);

               tempDate = tempDate.minusDays(1);
               secondTempDay = firstTempDay;
               firstTempDay = readDay(tempDate);
           }
           firstTempDay.smooth(secondTempDay);
           writeDay(firstTempDay);


       }
   }

    //Returns an array with the (hopefully) smoothed weights IN REVERSE ORDER!
    //internal argument is true if this is called from within Repository and false otherwise.

    public ArrayList<Entry> getWeights(LocalDate laterDate, LocalDate priorDate,boolean internal) {

        ArrayList<Entry> smoothedWeights = new ArrayList<>();
        int n = Days.daysBetween(priorDate,laterDate).getDays()+1;

        //If for some reason we're trying to smooth the wrong way, do nothing.
        if (laterDate.isAfter(priorDate)) {

            LocalDate tempDate = laterDate.minusDays(1);
            Day3 firstTempDay = readDay(tempDate);
            Day3 secondTempDay = readDay(tempDate.plusDays(1));

            smoothedWeights.add(new Entry((float) getWeightToReport(secondTempDay.getWeightEst(),internal),n));
            n--;

            //get the rest of the weights
            while(tempDate.isAfter(priorDate)){
                smoothedWeights.add(new Entry((float) getWeightToReport(firstTempDay.getWeightEst(),internal),n));
                n--;
                tempDate = tempDate.minusDays(1);
                firstTempDay = readDay(tempDate);
            }
            smoothedWeights.add(new Entry((float) getWeightToReport(firstTempDay.getWeightEst(),internal),n));

        }else if(laterDate.equals(priorDate)){
            Day3 day = readDay(laterDate);
            smoothedWeights.add(new Entry((float) getWeightToReport(day.getWeightEst(),internal),n));
        }else{
            return null;
        }

        return smoothedWeights;
    }
   /**
    * Returns the system to the state in which the calories for the given day was never set.
    */

   public void unsetCustomCalories(LocalDate date){

       Day3 day = readDay(date);
       day.setCustomCalories(0);
       writeDay(day);

       removeBackupFile(date, OTHER_CALS_KEY);
   }

    //A method for deleting all of the files.
    public void clearRepository(){

        try {
            FileUtils.cleanDirectory(new File(path));
        }catch(Exception ignored){}

    }

    //A method that decides what the right weight number to record is.
    private double getWeightToRecord(double weight){
        if(usesLbs){
            return weight;
        }else{
            return weight*KGS_TO_LBS_MULTIPLIER;
        }
    }

    //A method that decides what the right weight number to return is.
    //internal argument is true if this is called from within Repository and false otherwise.
    private double getWeightToReport(double weight, boolean internal){
        if(internal || usesLbs){
            return weight;
        }else{
            return weight*LBS_TO_KGS_MULTIPLIER;
        }
    }

    //A method that changes whether the repository accepts and outputs numbers in lbs or kgs.
    public void setWeightUnits(Boolean usesLbsPar){

        usesLbs = usesLbsPar;

        writeSettingToBackupFile(USES_LBS_KEY, Boolean.toString(usesLbs));

    }

    //A method that indicates whether or not the current Repo is using lbs
    public boolean getUsesLbs(){
        return usesLbs;
    }


    public void unsetCustomExercise(LocalDate date) {

        Day3 day = readDay(date);
        day.setCustomExercise(0.);
        writeDay(day);

        removeBackupFile(date, OTHER_EXERCISE_KEY);
    }


    public int getGoal() {
        return goal;
    }

    public void setGoal(int goalPar) {

        goal = goalPar;

        writeSettingToBackupFile(GOAL_KEY, Integer.toString(goal));
    }

    //internal argument is true if this is called from within Repository and false otherwise.
    public double getGoalWeight(boolean internal) {return getWeightToReport(goalWeight,internal);}

    public void setGoalWeight(double goalWeightPar){
        goalWeight = getWeightToRecord(goalWeightPar);
        writeSettingToBackupFile(GOAL_WEIGHT_KEY, Double.toString(goalWeight));
        updateGoal();

    }

    public boolean getInternalTracking(){
        return internalTracking;
    }

    //internal argument is true if this is called from within Repository and false otherwise.
    public void setInternalTracking(boolean internalTrackingPar){
        internalTracking = internalTrackingPar;
        writeSettingToBackupFile(INTERNAL_TRACKING_KEY, Boolean.toString(internalTracking));
    }


    public int getExercise(LocalDate date){
        return readDay(date).getExercise();
    }


    public void setExercise(LocalDate date, int exercise){

        Day3 day = readDay(date);
        day.setExercise(exercise);
        writeDay(day);

        if(exercise >0) {
            writeStringToBackupFile(date, EXERCISE_KEY, Integer.toString(exercise));
        }else{
            removeBackupFile(date, EXERCISE_KEY);
        }
    }



    public double getBaseGoalCalories(LocalDate date){
        Day3 day = readDay(date);
        double neee = day.getNEEE();

        if(goal == LOSE_WEIGHT){
            double desiredGoal = day.getPredictedNetIntake() + GOAL_SD_MARGIN*Math.sqrt(day.getPredictedNetIntakeVar());
            return Math.min(neee-MINIMUM_DEFICIT*neee,desiredGoal);

        }else if(goal == MAINTAIN){
            return neee;
        }else{
            double desiredGoal = day.getPredictedNetIntake() - GOAL_SD_MARGIN*Math.sqrt(day.getPredictedNetIntakeVar());
            return Math.max(neee+MINIMUM_DEFICIT*neee, desiredGoal);
        }
    }

    public double getTotalCalories(LocalDate date){

        return readDay(date).getTotalCalories();
    }

    public double getTotalExerciseCalories(LocalDate date){
        return readDay(date).getTotalExerciseCalories();
    }

    public double getExerciseCals(LocalDate date){

        return readDay(date).getExerciseCals();
    }

    public double getSmallSnackCals(LocalDate date){
        return readDay(date).getSmallSnackCals();
    }

    public double getLargeSnackCals(LocalDate date){

        return readDay(date).getLargeSnackCals();
    }

    public double getSmallMealCals(LocalDate date){
        return readDay(date).getSmallMealCals();
    }

    public double getRegularMealCals(LocalDate date){
        return readDay(date).getRegularMealCals();
    }

    public double getLargeMealCals(LocalDate date){
        return readDay(date).getLargeMealCals();
    }

    public double getBeverageCals(LocalDate date){
        return readDay(date).getBeverageCals();
    }



    public boolean getCaloriesRecorded(LocalDate date){
        return (readDay(date).getTotalCalories() > 0);
    }


    public LocalDate findFirstRecordDate(){
        LocalDate tempDate = BEGINNING_OF_TIME;

        while(tempDate.isBefore(getToday())){
            if(scaleWeightRecorded(tempDate)){
                return tempDate;
            }
            tempDate = tempDate.plusDays(1);
        }
        return tempDate;
    }

    //Method for writing a user's daily input data. NOT FOR SETTINGS.
    private void writeStringToBackupFile(LocalDate date, String key, String toWrite){

        File file = new File(path.resolve(BACKUP + "/" + date.toString() + "/" + key));
        file.getParentFile().mkdirs();

        if(file.exists()){
            file.delete();
        }

        try {
            //Files.write(path.resolve(LAST_DATE), lines);
            PrintWriter writer = new PrintWriter(file);
            writer.println(toWrite);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void removeBackupFile(LocalDate date, String key){

        File file = new File(path.resolve(BACKUP +"/" + date.toString() + "/" + key));
        file.getParentFile().mkdirs();

        if(file.exists()){
            file.delete();
        }

    }

    private String readStringFromBackupFile(LocalDate date, String key) throws IOException{

        String fromFile;
        File file = new File(path.resolve(BACKUP + "/" + date.toString() + "/" + key));
        BufferedReader reader = new BufferedReader(new FileReader(file));
        fromFile = reader.readLine();
        reader.close();

        return fromFile;
    }



    //A method for backing up user's settings. NOT FOR DAILY RECORDS.
    private void writeSettingToBackupFile(String key, String toWrite){

        File file = new File(path.resolve(BACKUP + "/" + SETTINGS + "/" + key));
        file.getParentFile().mkdirs();

        if(file.exists()){
            file.delete();
        }

        try {
            PrintWriter writer = new PrintWriter(file);
            writer.println(toWrite);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readSettingFromBackupFile(String key) throws IOException{

        String fromFile;
        File file = new File(path.resolve(BACKUP + "/" + SETTINGS + "/" + key));
        BufferedReader reader = new BufferedReader(new FileReader(file));
        fromFile = reader.readLine();
        reader.close();

        return fromFile;
    }

    public LocalDate getFirstRecordDate(){
        return firstRecordDate;
    }

    //internal argument is true if this is called from within Repository and false otherwise.
    public double getImmediateGoalWeight(LocalDate date, boolean internal){

        double immediateGoalWeight = getWeightEst(date,true)+(getBaseGoalCalories(date) - getNEEE(date))/CALORIES_PER_POUND;

        if(goal==LOSE_WEIGHT){
            immediateGoalWeight = Math.max(immediateGoalWeight, goalWeight);
        }else if(goal==GAIN_WEIGHT){
            immediateGoalWeight = Math.min(immediateGoalWeight,goalWeight);
        }
        return getWeightToReport(immediateGoalWeight,internal);
    }

    public double getGoalCalories(LocalDate date){
        System.out.println("immediateGoalWeight "+getImmediateGoalWeight(date,true));
        return getNEEE(date)+(getImmediateGoalWeight(date,true)-getWeightEst(date,true))*CALORIES_PER_POUND;
    }




    //Restores a single day from backup.
    private Day3 restoreDayFromBackup(Day3 tempDay){

        LocalDate tempDate = tempDay.getDate();

        if(backupDayExists(tempDate)) {

            //try reading scale weight from backup
            try {
                tempDay.setScaleWeight(Double.parseDouble(readStringFromBackupFile(tempDate, WEIGHT_KEY)));
            } catch (Exception ignored) {
            }

            //try reading  exercise from backup
            try {
                tempDay.setExercise(Integer.parseInt(readStringFromBackupFile(tempDate, EXERCISE_KEY)));
            } catch (Exception ignored) {
            }


            //try reading custom exercise from backup
            try {
                tempDay.setCustomExercise(Double.parseDouble(readStringFromBackupFile(tempDate, OTHER_EXERCISE_KEY)));
            } catch (Exception ignored) {
            }

            //try reading small snacks from backup
            try {
                tempDay.setSmallSnacks(Integer.parseInt(readStringFromBackupFile(tempDate, SMALL_SNACK_KEY)));
            } catch (Exception ignored) {
            }

            //try reading large snacks from backup
            try {
                tempDay.setLargeSnacks(Integer.parseInt(readStringFromBackupFile(tempDate, SNACK_KEY)));
            } catch (Exception ignored) {
            }

            //try reading small meals from backup
            try {
                tempDay.setSmallMeals(Integer.parseInt(readStringFromBackupFile(tempDate, SMALL_MEAL_KEY)));
            } catch (Exception ignored) {
            }

            //try reading regular meals from backup
            try {
                tempDay.setRegularMeals(Integer.parseInt(readStringFromBackupFile(tempDate, MEAL_KEY)));
            } catch (Exception ignored) {
            }

            //try reading large meals from backup
            try {
                tempDay.setLargeMeals(Integer.parseInt(readStringFromBackupFile(tempDate, LARGE_MEAL_KEY)));
            } catch (Exception ignored) {
            }

            //try reading beverages from backup
            try {
                tempDay.setBeverages(Integer.parseInt(readStringFromBackupFile(tempDate, BEVERAGE_KEY)));
            } catch (Exception ignored) {
            }

            //try reading custom calories from backup
            try {
                tempDay.setCustomCalories(Double.parseDouble(readStringFromBackupFile(tempDate, OTHER_CALS_KEY)));
            } catch (Exception ignored) {
            }
        }

        writeDay(tempDay);


        return tempDay;

    }


    public static LocalDate getToday(){
        LocalDate today;
        if(LocalDateTime.now().getHourOfDay() >=DAY_SWITCH_HOUR) {
            today = new LocalDate();
        }else{
            today = new LocalDate().minusDays(1);
        }

        return today;
    }

    public int getSmallSnacks(LocalDate date){
        return readDay(date).getSmallSnacks();
    }

    public void setSmallSnacks(LocalDate date, int smallSnacks){
        Day3 day = readDay(date);
        day.setSmallSnacks(smallSnacks);
        writeDay(day);
        if(smallSnacks >0) {
            writeStringToBackupFile(date, SMALL_SNACK_KEY, Integer.toString(smallSnacks));
        }else{
            removeBackupFile(date, SMALL_SNACK_KEY);
        }

    }


    public int getLargeSnacks(LocalDate date){
        return readDay(date).getLargeSnacks();
    }

    public void setSnacks(LocalDate date, int snacks){
        Day3 day = readDay(date);
        day.setLargeSnacks(snacks);
        writeDay(day);
        if(snacks > 0) {
            writeStringToBackupFile(date, SNACK_KEY, Integer.toString(snacks));
        }else{
            removeBackupFile(date, SNACK_KEY);
        }
    }

    public int getSmallMeals(LocalDate date){
        return readDay(date).getSmallMeals();
    }

    public void setSmallMeals(LocalDate date, int smallMeals){
        Day3 day = readDay(date);
        day.setSmallMeals(smallMeals);
        writeDay(day);
        if(smallMeals > 0) {
            writeStringToBackupFile(date, SMALL_MEAL_KEY, Integer.toString(smallMeals));
        }else{
            removeBackupFile(date, SMALL_MEAL_KEY);
        }

    }


    public int getRegMeals(LocalDate date){
        return readDay(date).getRegularMeals();
    }

    public void setRegMeals(LocalDate date, int regularMeals){
        Day3 day = readDay(date);
        day.setRegularMeals(regularMeals);
        writeDay(day);
        if(regularMeals > 0) {
            writeStringToBackupFile(date, MEAL_KEY, Integer.toString(regularMeals));
        }else{
            removeBackupFile(date, MEAL_KEY);
        }

    }

    public int getLargeMeals(LocalDate date){
        return readDay(date).getLargeMeals();
    }

    public void setLargeMeals(LocalDate date, int largeMeals){
        Day3 day = readDay(date);
        day.setLargeMeals(largeMeals);
        writeDay(day);
        if(largeMeals > 0) {
            writeStringToBackupFile(date, LARGE_MEAL_KEY, Integer.toString(largeMeals));
        }else{
            removeBackupFile(date, LARGE_MEAL_KEY);
        }

    }

    public int getBeverages(LocalDate date){
        return readDay(date).getBeverages();
    }

    public void setBeverages(LocalDate date, int beverages){
        Day3 day = readDay(date);
        day.setBeverages(beverages);
        writeDay(day);
        if(beverages > 0) {
            writeStringToBackupFile(date, BEVERAGE_KEY, Integer.toString(beverages));
        }else{
            removeBackupFile(date, BEVERAGE_KEY);
        }

    }

    private void setFirstRecordDate(LocalDate date){
        firstRecordDate = date;
        writeSettingToBackupFile(FIRST_RECORD_DATE_KEY, firstRecordDate.toString());
    }

    public void setupRepository(){

        firstRecordDate = getToday();
        try{
            version = Integer.parseInt(readSettingFromBackupFile(VERSION_KEY));
        }catch (Exception e){
            e.printStackTrace();
            if(backupExists()) {
                removeVigorousExercise();
                readAll();
                smooth(getToday(), getToday().minusDays(MainActivity.DEFAULT_CHART_DAYS));
            }else{
                readAll();
            }
            writeSettingToBackupFile(VERSION_KEY, Integer.toString(CURRENT_VERSION));
        }


        //Read the first record date from file
        String firstRecordDateFromFile;

        try{
            firstRecordDateFromFile = readSettingFromBackupFile(FIRST_RECORD_DATE_KEY);
            firstRecordDate = LocalDate.parse(firstRecordDateFromFile);
        } catch(IOException | NullPointerException e){
            firstRecordDate = findFirstRecordDate();
            writeSettingToBackupFile(FIRST_RECORD_DATE_KEY, firstRecordDate.toString());
        }

        //Read whether or not weights are in lbs from file
        String usesLbsFromFile;

        try{
            usesLbsFromFile = readSettingFromBackupFile(USES_LBS_KEY);
            usesLbs = Boolean.parseBoolean(usesLbsFromFile);
        }catch (IOException e){
            usesLbs = true;
            writeSettingToBackupFile(USES_LBS_KEY, Boolean.toString(usesLbs));
        }

        //Read the goal from file.
        String goalFromFile;

        try{
            goalFromFile = readSettingFromBackupFile(GOAL_KEY);
            goal = Integer.parseInt(goalFromFile);
        }catch (IOException e){
            goal = MAINTAIN;
            writeSettingToBackupFile(GOAL_KEY, Integer.toString(goal));
        }

        //Read the goalWeight from file.
        String goalWeightFromFile;

        try{
            goalWeightFromFile = readSettingFromBackupFile(GOAL_WEIGHT_KEY);
            goalWeight = Double.parseDouble(goalWeightFromFile);
        }catch(IOException e){
            goalWeight = -1.0;
        }

        //Read seenTutorial from file
        String seenTutorialFromFile;

        try{
            seenTutorialFromFile = readSettingFromBackupFile(SEEN_TUTORIAL_KEY);
            seenTutorial = Boolean.parseBoolean(seenTutorialFromFile);
        }catch (IOException e){
            seenTutorial = false;
            writeSettingToBackupFile(SEEN_TUTORIAL_KEY, Boolean.toString(seenTutorial));
        }

        //Read internalTracking from file
        String internalTrackingFromFile;

        try{
            internalTrackingFromFile = readSettingFromBackupFile(INTERNAL_TRACKING_KEY);
            internalTracking = Boolean.parseBoolean(internalTrackingFromFile);
        }catch (IOException e){
            internalTracking = true;
            writeSettingToBackupFile(SEEN_TUTORIAL_KEY, Boolean.toString(internalTracking));
        }
    }

    public void removeVigorousExercise(){

        LocalDate tempDate = BEGINNING_OF_TIME;
        int tempVigorousExercise;
        int tempModerateExercise;

        while(!tempDate.isAfter(getToday())){
            try {
                tempVigorousExercise = Integer.parseInt(readStringFromBackupFile(tempDate, VIGOROUS_EXERCISE_KEY));
            }catch(IOException e){
                tempVigorousExercise = 0;
            }
            try {
                tempModerateExercise = Integer.parseInt(readStringFromBackupFile(tempDate, EXERCISE_KEY));
            }catch(IOException e){
                tempModerateExercise = 0;
            }

            tempModerateExercise = tempModerateExercise + tempVigorousExercise;

            if(tempModerateExercise>0){
                writeStringToBackupFile(tempDate, EXERCISE_KEY, Integer.toString(tempModerateExercise));
            }

            tempDate = tempDate.plusDays(1);
        }

        writeSettingToBackupFile(VERSION_KEY, Boolean.toString(true));
    }


    private  void readAll(){

        LocalDate tempDate = BEGINNING_OF_TIME;
        LocalDate today = getToday();


        while(!tempDate.isAfter(today)){
            readDay(tempDate);
            tempDate = tempDate.plusDays(1);
        }
    }

    private boolean backupDayExists(LocalDate date){

        return new File(path.resolve(BACKUP + "/" + date.toString())).exists();
    }

    public double[] getMealCategoryCalories(LocalDate date){
        Day3 day = readDay(date);

        double[] array = { day.getSmallSnackCals(), day.getLargeSnackCals(), day.getSmallMealCals(),
                            day.getRegularMealCals(), day.getLargeMealCals(), day.getBeverageCals()};

        return array;
    }

    public void updateGoal(){

        if(goalWeight>0){
            if(goalWeight <= getWeightEst(getToday(),true)){
                setGoal(Repository.LOSE_WEIGHT);
            }else{
                setGoal(Repository.GAIN_WEIGHT);
            }
        }
    }

    //internal argument is true if this is called from within Repository and false otherwise.
    public double getStartWeight(boolean internal){
        return getWeightEst(firstRecordDate, internal);
    }
}




