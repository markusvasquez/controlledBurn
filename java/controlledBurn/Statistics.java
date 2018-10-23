package controlledBurn;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.controlledBurn.R;
import com.github.mikephil.charting.charts.LineChart;

import org.joda.time.LocalDate;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;

/**
 * Created by markusvasquez on 3/13/17.
 */

public class Statistics extends AppCompatActivity {

    Repository repo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);


        setContentView(R.layout.statistics_layout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        myToolBar.setNavigationIcon(getDrawable(R.drawable.ic_arrow_back_white_24dp));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        repo = MainActivity.getRepository();

        if(repo==null) {
            repo = new Repository(getFilesDir().toURI());
            repo.setupRepository();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected void onResume() {
        super.onResume();

        if(repo == null){
            repo = new Repository(getFilesDir().toURI());
            repo.setupRepository();
        }

        DecimalFormat format = new DecimalFormat("#0");
        LocalDate today = Repository.getToday();


        TextView baseCals = (TextView) findViewById(R.id.base_energy_expenditure);
        baseCals.setText(format.format(repo.getNEEE(today)));

        TextView exercise = (TextView) findViewById(R.id.exercise_cal_min);
        exercise.setText(format.format(repo.getExerciseCals(today)));

        TextView smallSnack = (TextView) findViewById(R.id.small_snack_cal);
        smallSnack.setText(format.format(repo.getSmallSnackCals(today)));

        TextView largeSnack = (TextView) findViewById(R.id.large_snack_cal);
        largeSnack.setText(format.format(repo.getLargeSnackCals(today)));

        TextView smallMeal = (TextView) findViewById(R.id.small_meal_cal);
        smallMeal.setText(format.format(repo.getSmallMealCals(today)));

        TextView regMeal = (TextView) findViewById(R.id.regular_meal_cal);
        regMeal.setText(format.format(repo.getRegularMealCals(today)));

        TextView largeMeal = (TextView) findViewById(R.id.large_meal_cal);
        largeMeal.setText(format.format(repo.getLargeMealCals(today)));

        TextView bev = (TextView) findViewById(R.id.beverage_cal);
        bev.setText(format.format(repo.getBeverageCals(today)));
    }

}