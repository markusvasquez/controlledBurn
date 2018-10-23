package controlledBurn;

import Jama.Matrix;


/**
 * Created by mv on 10/22/16.
 * This is a class that just holds constants for Day.
 *
 */

public abstract class Day2Helper {

    //Set parameter values

    private final static double[][] F_VALS = {{1, -1.0/3500.0},{0,1}};
    private final static Matrix DEFAULT_F = new Matrix(F_VALS);
    static Matrix F = DEFAULT_F;

    private final static double[][] B_VALS = {{1.0/3500, -1.0/3500},
            {0,0},
            {0,0},
            {0,0},
            {0,0},
            {0,0},
            {0,0},
            {0,0},
            {0,0},
            {0,0}};
    private final static Matrix DEFAULT_B = new Matrix(B_VALS);
    static Matrix B = DEFAULT_B;

    private final static double[][] Q_SQRT_VALS =
            {{0.1,0,0,0,0,0,0,0,0,0,0},
            {0,1.25,0,0,0,0,0,0,0,0,0},
            {0,0,0.05,0,0,0,0,0,0,0,0},
            {0,0,0,0.15,0,0,0,0,0,0,0},
            {0,0,0,0,0.3,0,0,0,0,0,0},
            {0,0,0,0,0,0.4,0,0,0,0,0},
            {0,0,0,0,0,0,0.44,0,0,0,0},
            {0,0,0,0,0,0,0,.09,0,0,0},
            {0,0,0,0,0,0,0,0,0.012,0.016,0},
            {0,0,0,0,0,0,0,0,0.012,0,0.016}};

    private final static double[][] Q_NO_TRACKING_SQRT_VALS =
            {{0.2,0,0,0,0,0,0,0,0,0,0},
            {0,1.25,0,0,0,0,0,0,0,0,0},
            {0,0,0.05,0,0,0,0,0,0,0,0},
            {0,0,0,0.15,0,0,0,0,0,0,0},
            {0,0,0,0,0.3,0,0,0,0,0,0},
            {0,0,0,0,0,0.4,0,0,0,0,0},
            {0,0,0,0,0,0,0.44,0,0,0,0},
            {0,0,0,0,0,0,0,.09,0,0,0},
            {0,0,0,0,0,0,0,0,0.012,0.016,0},
            {0,0,0,0,0,0,0,0,0.012,0,0.016}};


    private final static Matrix Q_SQRT = new Matrix(Q_SQRT_VALS);
    private final static Matrix Q_NO_TRACKING_SQRT = new Matrix(Q_NO_TRACKING_SQRT_VALS);

    //Need to multiply var cov matrices by tdee.
    public final static Matrix Q = Q_SQRT.times(Q_SQRT.transpose()).times(1./(2500.*2500.));
    public final static Matrix Q_NO_TRACKING = Q_NO_TRACKING_SQRT.times(Q_NO_TRACKING_SQRT.transpose()).times(1./(2500.*2500.));

    private final static double[][] R_VALS = {{1.5*1.5}};
    private final static Matrix DEFAULT_R = new Matrix(R_VALS);
    public static Matrix R = DEFAULT_R;

    private final static double[][] H_VALS = {{1,0,0,0,0,0,0,0,0,0}};
    private final static Matrix DEFAULT_H = new Matrix(H_VALS);
    static Matrix H = DEFAULT_H;




    //Set values used in calculation of first day NEEE
    final static double NEEE_MASS_MULTIPLIER = 4.53138408;
    final static double NEEE_HEIGHT_MULTIPLIER = 15.875;
    final static double NEEE_AGE_MULTIPLIER = -4.92;
    final static double NEEE_FROM_MALE = 5.0;
    final static double NEEE_FROM_FEMALE = -161.0;
    final static double NEEE_FROM_NO_GENDER = 0.5*(NEEE_FROM_FEMALE+NEEE_FROM_MALE);
    final static double DEFAULT_NEEE_ACTIVITY_LEVEL = 1.5;

    //Numbers to multiply by weight to get estimates of exercise expenditure.
    final static double MOD_EXERCISE_MULTIPLIER = 0.0175*7./2.2;
    final static double VIG_EXERCISE_MULTIPLIER = 0.0175*14./2.2;

    //Numbers to multiply by neee to get estimates of calories.
    final static double SMALL_SNACK_MULTIPLIER = 1./24.;
    final static double SNACK_MULTIPLIER = 1./8.;
    final static double LIGHT_MEAL_MULTIPLIER = 1./4.;
    final static double MEAL_MULTIPLIER = 1./3.;
    final static double HEAVY_MEAL_MULTIPLIER = 5./12.;
    final static double BEVERAGE_MULTIPLIER = 1./16.;

    final static double CALORIES_PER_POUND = 3500.;

    //Integer tags for gender
    public final static int MALE = 0;
    public final static int FEMALE = 1;
    public final static int NO_GENDER = 2;

    final static double INITIAL_WEIGHT_SD = 200;
    final static double INITIAL_NEEE_SD = 400.;
    final static double INITIAL_FOOD_SD = 600;

    //set first day variance covariance value.
           private static double[][] defaultInitialPValsSQRT = {{INITIAL_WEIGHT_SD,0,0,0,0,0,0,0,0,0},
            {NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_SD,INITIAL_NEEE_SD,0,0,0,0,0,0,0,0},
            {NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_SD*SMALL_SNACK_MULTIPLIER,INITIAL_NEEE_SD*SMALL_SNACK_MULTIPLIER, INITIAL_FOOD_SD*SMALL_SNACK_MULTIPLIER,0,0,0,0,0,0,0},
            {NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_SD*SNACK_MULTIPLIER,INITIAL_NEEE_SD*SNACK_MULTIPLIER,0,INITIAL_FOOD_SD*SNACK_MULTIPLIER,0,0,0,0,0,0},
            {NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_SD*LIGHT_MEAL_MULTIPLIER,INITIAL_NEEE_SD*LIGHT_MEAL_MULTIPLIER,0,0,INITIAL_FOOD_SD*LIGHT_MEAL_MULTIPLIER,0,0,0,0,0},
            {NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_SD*MEAL_MULTIPLIER,INITIAL_NEEE_SD*MEAL_MULTIPLIER,0,0,0,INITIAL_FOOD_SD*MEAL_MULTIPLIER,0,0,0,0},
            {NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_SD*HEAVY_MEAL_MULTIPLIER,INITIAL_NEEE_SD*HEAVY_MEAL_MULTIPLIER,0,0,0,0,INITIAL_FOOD_SD*HEAVY_MEAL_MULTIPLIER,0,0,0},
            {NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_SD*BEVERAGE_MULTIPLIER,INITIAL_NEEE_SD*BEVERAGE_MULTIPLIER,0,0,0,0,0,INITIAL_FOOD_SD*BEVERAGE_MULTIPLIER,0,0},
            {MOD_EXERCISE_MULTIPLIER*INITIAL_WEIGHT_SD,0,0,0,0,0,0,0,1.6,0},
            {VIG_EXERCISE_MULTIPLIER*INITIAL_WEIGHT_SD,0,0,0,0,0,0,0,0,3.2}};


    private static Matrix defaultInitialPSQRT = new Matrix(defaultInitialPValsSQRT);

    final static Matrix DEFAULT_INITIAL_P = defaultInitialPSQRT.times(defaultInitialPSQRT.transpose()).times(1./(2500.*2500.));

    public final static int SMALL_SNACK_INDEX = 0;
    public final static int SNACK_INDEX = 1;
    public final static int LIGHT_MEAL_INDEX = 2;
    public final static int REG_MEAL_INDEX = 3;
    public final static int HEAVY_MEAL_INDEX = 4;
    public final static int BEVERAGE_INDEX = 5;
    public final static int MODERATE_EXERCISE_INDEX = 6;
    public final static int VIGOROUS_EXERCISE_INDEX = 7;

    final static int DIARY_INFO_NUM_INDEX = 0;
    final static int DIARY_INFO_CALS_INDEX = 1;

    final static double DECAY_RATE = 0.8;
    final static double DEFAULT_PREDICTED_SURPLUS_VARIANCE = 100.*100./(2500.*2500.);

    final static double SCALE_DEVIATION = 0.0175; //Percentage of body weight that is expected for scale sd

    //Parameters used to compute R to weed out large positive scale deviations.
    //was 3.8, 3.4, 5.8
    final static double A_PARAM = 3.8;
    final static double B_PARAM = 3.4;
    final static double C_PARAM = 5.8;

    //Estimates for initial weight, height, sex, age
    final static double DEFAULT_WEIGHT_EST = 175.;
    final static int DEFAULT_AGE_EST = 40;
    final static int DEFAULT_HEIGHT_EST = 70;
    final static int DEFAULT_SEX_EST = 2;




}
