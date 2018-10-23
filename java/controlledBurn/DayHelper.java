package controlledBurn;

import Jama.Matrix;


/**
 * Created by mv on 10/22/16.
 * This is a class that just holds constants for Day.
 *
 */

public abstract class DayHelper {

    //Set parameter values

    private final static double[][] F_VALS = {{1, -1.0/3500.0},{0,1}};
    private final static Matrix DEFAULT_F = new Matrix(F_VALS);
    static Matrix F = DEFAULT_F;

    private final static double[][] B_VALS = {{1.0/3500, -1.0/3500},{0,0}};
    private final static Matrix DEFAULT_B = new Matrix(B_VALS);
    static Matrix B = DEFAULT_B;

    private final static double[][] Q_VALS = {{0.0075,0},{0,144.0}};
    private final static Matrix DEFAULT_Q = new Matrix(Q_VALS);
    static Matrix Q = DEFAULT_Q;

    private final static double[][] Q_NO_TRACKING_VALS = {{(1.0/7.0)*(1.0/7.0),0},{0,12.0*12.0}};
    private final static Matrix DEFAULT_Q_NO_TRACKING = new Matrix(Q_NO_TRACKING_VALS);
    static Matrix QNoTracking = DEFAULT_Q_NO_TRACKING;

    private final static double[][] R_VALS = {{1.5*1.5}};
    private final static Matrix DEFAULT_R = new Matrix(R_VALS);
    public static Matrix R = DEFAULT_R;

    private final static double[][] H_VALS = {{1,0}};
    private final static Matrix DEFAULT_H = new Matrix(H_VALS);
    static Matrix H = DEFAULT_H;

    private final static double[][] I_VALS = {{1,0},{0,1}};
    final static Matrix I = new Matrix(I_VALS);



    //Set values used in calculation of first day NEEE
    final static double NEEE_MASS_MULTIPLIER = 4.53138408;
    final static double NEEE_HEIGHT_MULTIPLIER = 15.875;
    final static double NEEE_AGE_MULTIPLIER = -4.92;
    final static double NEEE_FROM_MALE = 5.0;
    final static double NEEE_FROM_FEMALE = -161.0;
    final static double NEEE_FROM_NO_GENDER = 0.5*(NEEE_FROM_FEMALE+NEEE_FROM_MALE);
    final static double DEFAULT_NEEE_ACTIVITY_LEVEL = 1.5;

    //Integer tags for gender
    public final static int MALE = 0;
    public final static int FEMALE = 1;
    public final static int NO_GENDER = 2;

    //set first day variance covariance value.
    private final static double INITIAL_WEIGHT_VAR = 2.5*2.5;
    final static double INITIAL_NEEE_VAR = 350.0*350.0;
    private final static double INITIAL_COV = 0;//DEFAULT_NEEE_ACTIVITY_LEVEL*NEEE_MASS_MULTIPLIER*INITIAL_WEIGHT_VAR;
    private final static double[][] DEFAULT_INITIAL_P_VALS = {{INITIAL_WEIGHT_VAR,INITIAL_COV},{INITIAL_COV,INITIAL_NEEE_VAR}};
    static double[][] initialPVals = DEFAULT_INITIAL_P_VALS;

}
