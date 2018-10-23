package controlledBurn;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.controlledBurn.R;
import com.github.mikephil.charting.charts.LineChart;

import org.joda.time.LocalDate;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static controlledBurn.Repository.BEVERAGE_INDEX;
import static controlledBurn.Repository.LARGE_MEAL_INDEX;
import static controlledBurn.Repository.LARGE_SNACK_INDEX;
import static controlledBurn.Repository.REGULAR_MEAL_INDEX;
import static controlledBurn.Repository.SMALL_MEAL_INDEX;
import static controlledBurn.Repository.SMALL_SNACK_INDEX;


public class MainActivity extends AppCompatActivity {

    final static int ESTIMATED_WEIGHT_POSITION = 0;
    final static int SCALE_WEIGHT_POSITION = 1;
    final static int DEFAULT_CHART_DAYS = 5;

    final static int EXERCISE_INCREMENT = 10;

    final static int JOB_ID = 1;



    static Repository repo;
    LocalDate date;

    LocalDate today;

    LineChart chart;

    //ImageButton dateLeft;
    //TextView dateText;
    //ImageButton dateRight;

    //Spinner weightSpinner;
    //ImageButton removeWeight;
    //EditText weightEntry;
    //ImageButton acceptWeight;

    //ImageButton minusExercise;
    //EditText exerciseEntry;
    //ImageButton plusExercise;

    //TextView smallSnackText;
    //ImageButton minusSmallSnack;
    //EditText smallSnackEntry;
    //ImageButton plusSmallSnack;

    //TextView snackText;
    //ImageButton minusSnack;
    //EditText snackEntry;
    //ImageButton plusSnack;

   // TextView smallMealText;
    //ImageButton minusSmallMeal;
    //EditText smallMealEntry;
    //ImageButton plusSmallMeal;

    //TextView mealText;
    //ImageButton minusMeal;
    //EditText mealEntry;
    //ImageButton plusMeal;

    //TextView largeMealText;
    //ImageButton minusLargeMeal;
    //EditText largeMealEntry;
    //ImageButton plusLargeMeal;

    //TextView beverageText;
    //ImageButton minusBeverage;
    //EditText beverageEntry;
    //ImageButton plusBeverage;

    //TextView otherCaloriesText;
    //ImageButton removeOtherCalories;
    //EditText otherCaloriesEntry;
    //ImageButton acceptOtherCalories;

    //TextView otherExerciseText;
    //ImageButton removeOtherExercise;
    //EditText otherExerciseEntry;
    //ImageButton acceptOtherExercise;

    //TextView goalEquationText;
    //TextView foodEquationText;
    //TextView exerciseEquationText;
    //TextView remainingEquationText;

    LinearLayout scrollHolder;

    MainChartHelper chartHelper;

    boolean usesCBTracking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {



        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.standard_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setLogo(getDrawable(R.drawable.fire_launcher));
        myToolbar.setTitle(" " +getString(R.string.app_name));
        setSupportActionBar(myToolbar);

        repo = new Repository(getFilesDir().toURI());
        repo.setupRepository();

    }

    protected void onResume(){
        super.onResume();


        if(repo ==null){
            repo = new Repository(getFilesDir().toURI());
            repo.setupRepository();
        }

        if(!repo.getSeenTutorial()){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            repo.setSeenTutorial(true);
                            break;

                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.welcome_message).setPositiveButton(R.string.got_it, dialogClickListener)
                    .setTitle(R.string.welcome).show();
        }


        date = Repository.getToday();

        today = date.plusDays(0);

        usesCBTracking = repo.getInternalTracking();

        scrollHolder = (LinearLayout) findViewById(R.id.scroll_holder);
        scrollHolder.removeAllViewsInLayout();

        repo.updateGoal();
        setupChart();
        
        setupDate();

        if(!usesCBTracking){
            getLayoutInflater().inflate(R.layout.equation_layout,scrollHolder);
            getLayoutInflater().inflate(R.layout.divider,scrollHolder);
        }

        getLayoutInflater().inflate(R.layout.weight_layout,scrollHolder);
        setupWeightView();



        if(usesCBTracking) {
            getLayoutInflater().inflate(R.layout.divider,scrollHolder);


            getLayoutInflater().inflate(R.layout.small_snack_layout, scrollHolder);
            setupSmallSnack();

            getLayoutInflater().inflate(R.layout.snacks_layout, scrollHolder);
            getLayoutInflater().inflate(R.layout.divider, scrollHolder);
            setupSnack();

            getLayoutInflater().inflate(R.layout.small_meal_layout, scrollHolder);
            setupSmallMeal();

            getLayoutInflater().inflate(R.layout.meal_layout, scrollHolder);
            setupMeal();

            getLayoutInflater().inflate(R.layout.large_meal_layout, scrollHolder);
            getLayoutInflater().inflate(R.layout.divider, scrollHolder);
            setupLargeMeal();

            getLayoutInflater().inflate(R.layout.beverage_layout, scrollHolder);
            getLayoutInflater().inflate(R.layout.divider, scrollHolder);
            setupBeverage();

            getLayoutInflater().inflate(R.layout.exercise_entry_layout, scrollHolder);
            getLayoutInflater().inflate(R.layout.divider, scrollHolder);
            setupExerciseView();
        }

        getLayoutInflater().inflate(R.layout.other_calories_layout,scrollHolder);
        setupOtherCals();

        getLayoutInflater().inflate(R.layout.other_exercise_layout,scrollHolder);
        setupOtherExercise();

        setDailyTexts();

    }



    public static Repository getRepository(){

        return repo;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings: {
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            }
            case R.id.help:{
                Intent intent = new Intent(this, Help.class);
                startActivity(intent);
                break;
            }
            case R.id.history:{
                Intent intent = new Intent(this, History.class);
                startActivity(intent);
                break;
            }

        }
        return false;
    }




    private void setupChart(){
        chart = (LineChart) findViewById(R.id.chart);

        chartHelper = new MainChartHelper(chart, repo, this);

    }

    private void setupDate(){

        ImageButton dateLeft = (ImageButton) findViewById(R.id.date_left);
        //dateText = (TextView) findViewById(R.id.date_text);
        ImageButton dateRight = (ImageButton) findViewById(R.id.date_right);

        dateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            date = date.minusDays(1);
            setDailyTexts();

            }
        });

        dateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(date.isBefore(Repository.getToday())){
                    date = date.plusDays(1);
                    setDailyTexts();
                }
                
            }
        });
    }

    private void setDateText(){

        TextView dateText = (TextView) findViewById(R.id.date_text);


        LocalDate today = Repository.getToday();
        
        if(date.equals(today)){
            dateText.setText(getString(R.string.today));
        }else if(date.equals(today.minusDays(1))){
            dateText.setText(getString(R.string.yesterday));
        }else{
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(getBaseContext());
            dateText.setText(dateFormat.format(date.toDate()));
        }
    }

    private void setupWeightView(){

        final Spinner weightSpinner = (Spinner) findViewById(R.id.weight_spinner);
        final ImageButton removeWeight = (ImageButton) findViewById(R.id.weight_remove);
        final EditText weightEntry = (EditText) findViewById(R.id.weight_entry);
        final ImageButton acceptWeight = (ImageButton) findViewById(R.id.weight_accept_button);

        weightEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    weightEntry.setBackgroundTintList(ContextCompat.getColorStateList(weightEntry.getContext(), R.color.accent_material_light));
                }else{
                    weightEntry.setBackgroundTintList(ContextCompat.getColorStateList(weightEntry.getContext(), R.color.secondary_text_material_light_1));
                }
            }
        });


        // Create an ArrayAdapter using the string array and a default timeSpinner layout
        ArrayAdapter<CharSequence> weightAdapter = ArrayAdapter.createFromResource(this,
                R.array.weight_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the timeSpinner
        weightSpinner.setAdapter(weightAdapter);

        weightSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {

                if(spinnerPosition == ESTIMATED_WEIGHT_POSITION){
                    weightEntry.setEnabled(false);
                    weightEntry.setBackgroundTintList(ContextCompat.getColorStateList(weightEntry.getContext(), android.R.color.transparent));
                    removeWeight.setVisibility(View.INVISIBLE);
                    acceptWeight.setVisibility(View.INVISIBLE);
                }else{
                    weightEntry.setEnabled(true);
                    weightEntry.setBackgroundTintList(ContextCompat.getColorStateList(weightEntry.getContext(), R.color.secondary_text_material_light_1));
                    removeWeight.setVisibility(View.VISIBLE);
                    acceptWeight.setVisibility(View.VISIBLE);
                }
                setWeightText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        removeWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.unsetScaleWeight(date);
                setWeightText();
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                weightEntry.clearFocus();
                repo.propagate(date);
                setEquationTexts();
                grayOutTexts();
                blackOutTexts();
                repo.updateGoal();
                chartHelper.updateChart();
                scheduleSmoothing();
            }
        });

        acceptWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!weightEntry.getText().toString().equals("")){
                    repo.setScaleWeight(date, Double.parseDouble(weightEntry.getText().toString()));
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    weightEntry.clearFocus();
                    repo.propagate(date);
                    setEquationTexts();
                    grayOutTexts();
                    blackOutTexts();
                    weightSpinner.setSelection(ESTIMATED_WEIGHT_POSITION);
                    repo.updateGoal();
                    chartHelper.updateChart();
                    scheduleSmoothing();
                }else{
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    weightEntry.clearFocus();
                }
            }
        });

        weightEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
                    String entry = weightEntry.getText().toString();
                    if(!entry.equals("")) {
                        repo.setScaleWeight(date, Double.parseDouble(entry));
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        weightEntry.clearFocus();
                        repo.propagate(date);
                        setEquationTexts();
                        grayOutTexts();
                        blackOutTexts();
                        weightSpinner.setSelection(ESTIMATED_WEIGHT_POSITION);
                        repo.updateGoal();
                        chartHelper.updateChart();
                        scheduleSmoothing();
                    }else{
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        weightEntry.clearFocus();
                    }
                }
                return false;
            }
        });

    }

    private void setWeightText(){

        Spinner weightSpinner = (Spinner) findViewById(R.id.weight_spinner);
        EditText weightEntry = (EditText) findViewById(R.id.weight_entry);

        NumberFormat formatter;
        String units;

        if(repo.getUsesLbs()) {
            formatter = new DecimalFormat("#0.0");
            units = getString(R.string.lbs);
        }else{
            formatter = new DecimalFormat("#0.0");
            units = getString(R.string.kgs);
        }

        if(weightSpinner.getSelectedItemPosition() == ESTIMATED_WEIGHT_POSITION){
            weightEntry.setText(formatter.format(repo.getWeightEst(date,false)));
        }else{
            weightEntry.setText("");
            if(repo.scaleWeightRecorded(date)) {
                weightEntry.setHint(formatter.format(repo.getScaleWeight(date,false)));
            }else{
                weightEntry.setHint(units);
            }
        }
    }

    private void setupExerciseView(){

        ImageButton minusExercise = (ImageButton) findViewById(R.id.exercise_minus);
        final EditText exerciseEntry = (EditText) findViewById(R.id.exercise_entry);
        ImageButton plusExercise = (ImageButton) findViewById(R.id.exercise_add);

        minusExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldExercise = repo.getExercise(date);
                if(oldExercise >0) {
                    repo.setExercise(date, Math.max(oldExercise-EXERCISE_INCREMENT,0));
                    setExerciseText();
                    grayOutTexts();
                    repo.propagate(date);
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    exerciseEntry.clearFocus();
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }
            }
        });

        plusExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setExercise(date, repo.getExercise(date) + EXERCISE_INCREMENT);
                setExerciseText();
                blackOutTexts();
                repo.propagate(date);
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                exerciseEntry.clearFocus();
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }


            }
        });

        exerciseEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
                    String entry = exerciseEntry.getText().toString();

                    if(!entry.equals("")){
                        repo.setExercise(date, Integer.parseInt(entry));
                        setExerciseText();
                        grayOutTexts();
                        blackOutTexts();
                        repo.propagate(date);
                        if(date.equals(today)) {
                            chartHelper.updateLastEntries();
                        }else{
                            chartHelper.updateChart();
                        }
                    }else{
                        setExerciseText();
                    }
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    exerciseEntry.clearFocus();

                }
                return false;
            }
        });

    }

    private void setExerciseText(){

        EditText exerciseEntry = (EditText) findViewById(R.id.exercise_entry);


        exerciseEntry.setText(Integer.toString(repo.getExercise(date)));
    }

    private void setupSmallSnack(){

        ImageButton minusSmallSnack = (ImageButton) findViewById(R.id.small_snack_minus);
        ImageButton plusSmallSnack = (ImageButton) findViewById(R.id.small_snack_plus);

        minusSmallSnack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int oldSmallSnacks = repo.getSmallSnacks(date);
                if(oldSmallSnacks > 0){
                    repo.setSmallSnacks(date, oldSmallSnacks-1);
                    setSmallSnackText();
                    blackOutTexts();
                    repo.propagate(date);
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }
            }
        });

        plusSmallSnack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                repo.setSmallSnacks(date, repo.getSmallSnacks(date)+1);
                setSmallSnackText();
                grayOutTexts();
                repo.propagate(date);
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }
            }

        });
    }

    private void setSmallSnackText(){
        EditText smallSnackEntry = (EditText) findViewById(R.id.small_snack_entry);

        smallSnackEntry.setText(Integer.toString(repo.getSmallSnacks(date)));
    }




    private void setupSnack(){

        ImageButton minusSnack = (ImageButton) findViewById(R.id.snack_minus);
        ImageButton plusSnack = (ImageButton) findViewById(R.id.snack_plus);

        minusSnack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldSnacks = repo.getLargeSnacks(date);
                if(oldSnacks > 0){
                    repo.setSnacks(date, oldSnacks-1);
                    setSnackText();
                    blackOutTexts();
                    repo.propagate(date);
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }
            }
        });

        plusSnack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setSnacks(date, repo.getLargeSnacks(date)+1);
                setSnackText();
                grayOutTexts();
                repo.propagate(date);
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }

            }
        });
    }

    private void setSnackText(){
        EditText snackEntry = (EditText) findViewById(R.id.snack_entry);

        snackEntry.setText(Integer.toString(repo.getLargeSnacks(date)));
    }



    private void setupSmallMeal(){

        ImageButton minusSmallMeal = (ImageButton) findViewById(R.id.small_meal_minus);
        ImageButton plusSmallMeal = (ImageButton) findViewById(R.id.small_meal_plus);

        minusSmallMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldSmallMeals = repo.getSmallMeals(date);
                if(oldSmallMeals > 0){
                    repo.setSmallMeals(date, oldSmallMeals-1);
                    setSmallMealText();
                    blackOutTexts();
                    repo.propagate(date);
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }
            }
        });

        plusSmallMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setSmallMeals(date, repo.getSmallMeals(date)+1);
                setSmallMealText();
                grayOutTexts();
                repo.propagate(date);
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }

            }
        });
    }

    private void setSmallMealText(){
        EditText smallMealEntry = (EditText) findViewById(R.id.small_meal_entry);

        smallMealEntry.setText(Integer.toString(repo.getSmallMeals(date)));
    }

    private void setupMeal(){

        ImageButton minusMeal = (ImageButton) findViewById(R.id.meal_minus);
        ImageButton plusMeal = (ImageButton) findViewById(R.id.meal_plus);

        minusMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldMeals = repo.getRegMeals(date);
                if(oldMeals > 0){
                    repo.setRegMeals(date, oldMeals-1);
                    setMealText();
                    blackOutTexts();
                    repo.propagate(date);
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }
            }
        });

        plusMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setRegMeals(date, repo.getRegMeals(date)+1);
                setMealText();
                grayOutTexts();
                repo.propagate(date);
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }

            }
        });
    }

    private void setMealText(){
        EditText mealEntry = (EditText) findViewById(R.id.meal_entry);

        mealEntry.setText(Integer.toString(repo.getRegMeals(date)));
    }



    private void setupLargeMeal(){

        ImageButton minusLargeMeal = (ImageButton) findViewById(R.id.large_meal_minus);
        ImageButton plusLargeMeal = (ImageButton) findViewById(R.id.large_meal_plus);

        minusLargeMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldLargeMeals = repo.getLargeMeals(date);
                if(oldLargeMeals > 0){
                    repo.setLargeMeals(date, oldLargeMeals-1);
                    setLargeMealText();
                    blackOutTexts();
                    repo.propagate(date);
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }
            }
        });

        plusLargeMeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setLargeMeals(date, repo.getLargeMeals(date)+1);
                setLargeMealText();
                grayOutTexts();
                repo.propagate(date);
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }

            }
        });
    }

    private void setLargeMealText(){
        EditText largeMealEntry = (EditText) findViewById(R.id.large_meal_entry);

        largeMealEntry.setText(Integer.toString(repo.getLargeMeals(date)));
    }

    private void setupBeverage(){

        ImageButton minusBeverage = (ImageButton) findViewById(R.id.beverage_minus);
        ImageButton plusBeverage = (ImageButton) findViewById(R.id.beverage_plus);

        minusBeverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int oldBeverages = repo.getBeverages(date);
                if(oldBeverages > 0){
                    repo.setBeverages(date, oldBeverages-1);
                    setBeverageText();
                    blackOutTexts();
                    repo.propagate(date);
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }
            }
        });

        plusBeverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setBeverages(date, repo.getBeverages(date)+1);
                setBeverageText();
                grayOutTexts();
                repo.propagate(date);
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }

            }
        });
    }

    private void setBeverageText(){
        EditText beverageEntry = (EditText) findViewById(R.id.beverage_entry);

        beverageEntry.setText(Integer.toString(repo.getBeverages(date)));
    }

    private void setupOtherCals(){
        TextView otherCaloriesText = (TextView) findViewById(R.id.other_calories_text);
        ImageButton removeOtherCalories = (ImageButton) findViewById(R.id.other_cals_remove);
        final EditText otherCaloriesEntry = (EditText) findViewById(R.id.other_cals_entry);
        ImageButton acceptOtherCalories = (ImageButton) findViewById(R.id.other_cals_accept_button);

        if(!usesCBTracking){
            otherCaloriesText.setText(R.string.food);
        }

        removeOtherCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setCustomCalories(date,0.);
                setOtherCalsText();
                blackOutTexts();
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                repo.propagate(date);
                setEquationTexts();
                otherCaloriesEntry.clearFocus();
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }

            }
        });

        acceptOtherCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String entry = otherCaloriesEntry.getText().toString();

                if(!entry.equals("")) {
                    repo.setCustomCalories(date, Double.parseDouble(entry));
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    otherCaloriesEntry.clearFocus();
                    grayOutTexts();
                    blackOutTexts();
                    repo.propagate(date);
                    setEquationTexts();
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }else{
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    otherCaloriesEntry.clearFocus();
                }

            }
        });

        otherCaloriesEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
                    String entry = otherCaloriesEntry.getText().toString();

                    if(!entry.equals("")) {
                        repo.setCustomCalories(date, Double.parseDouble(entry));
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        otherCaloriesEntry.clearFocus();
                        grayOutTexts();
                        blackOutTexts();
                        repo.propagate(date);
                        setEquationTexts();
                        if(date.equals(today)) {
                            chartHelper.updateLastEntries();
                        }else{
                            chartHelper.updateChart();
                        }
                    }else{
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        otherCaloriesEntry.clearFocus();
                    }
                }
                return false;
            }
        });

    }

    private void setOtherCalsText(){
        EditText otherCaloriesEntry = (EditText) findViewById(R.id.other_cals_entry);

        if(repo.getCustomCalories(date)>0.0){
            NumberFormat formatter = new DecimalFormat("#0");
            otherCaloriesEntry.setText(formatter.format(repo.getCustomCalories(date)));
        }else{
            otherCaloriesEntry.setText("");
        }
    }

    private void setupOtherExercise(){

        TextView otherExerciseText = (TextView) findViewById(R.id.other_exercise_text);
        ImageButton removeOtherExercise = (ImageButton) findViewById(R.id.other_exercise_remove);
        final EditText otherExerciseEntry  = (EditText) findViewById(R.id.other_exercise_entry);
        ImageButton acceptOtherExercise = (ImageButton) findViewById(R.id.other_exercise_accept_button);

        if(!usesCBTracking){
            otherExerciseText.setText(R.string.exercise);
        }

        removeOtherExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repo.setCustomExercise(date,0.);
                setOtherExerciseText();
                grayOutTexts();
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                otherExerciseEntry.clearFocus();
                repo.propagate(date);
                setEquationTexts();
                if(date.equals(today)) {
                    chartHelper.updateLastEntries();
                }else{
                    chartHelper.updateChart();
                }

            }
        });

        acceptOtherExercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String entry = otherExerciseEntry.getText().toString();

                if(!entry.equals("")) {
                    repo.setCustomExercise(date, Double.parseDouble(otherExerciseEntry.getText().toString()));
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    otherExerciseEntry.clearFocus();
                    grayOutTexts();
                    blackOutTexts();
                    repo.propagate(date);
                    setEquationTexts();
                    if(date.equals(today)) {
                        chartHelper.updateLastEntries();
                    }else{
                        chartHelper.updateChart();
                    }
                }else{
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    otherExerciseEntry.clearFocus();
                }


            }
        });

        otherExerciseEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
                    String entry = otherExerciseEntry.getText().toString();

                    if(!entry.equals("")) {
                        repo.setCustomExercise(date, Double.parseDouble(entry));
                        grayOutTexts();
                        blackOutTexts();
                        repo.propagate(date);
                        setEquationTexts();
                        if(date.equals(today)) {
                            chartHelper.updateLastEntries();
                        }else{
                            chartHelper.updateChart();
                        }
                    }
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    otherExerciseEntry.clearFocus();
                }

                return false;
            }
        });

    }

    private void setOtherExerciseText(){

        final EditText otherExerciseEntry  = (EditText) findViewById(R.id.other_exercise_entry);


        if(repo.getCustomExercise(date)>0.0){
            NumberFormat formatter = new DecimalFormat("#0");
            otherExerciseEntry.setText(formatter.format(repo.getCustomExercise(date)));
        }else{
            otherExerciseEntry.setText("");
        }
    }



    private void setEquationTexts(){

        if(!usesCBTracking) {
            TextView goalEquationText = (TextView) findViewById(R.id.equation_goal);
            TextView foodEquationText = (TextView) findViewById(R.id.equation_food);
            TextView exerciseEquationText = (TextView) findViewById(R.id.equation_exercise);
            TextView remainingEquationText = (TextView) findViewById(R.id.equation_remaining);

            NumberFormat formatter = new DecimalFormat("#0");
            System.out.println(repo.getGoalCalories(date));
            double goalCalories = repo.getGoalCalories(date);
            double foodCalories = repo.getTotalCalories(date);
            double exerciseCalories = repo.getTotalExerciseCalories(date);
            double remainingCalories = goalCalories - foodCalories + exerciseCalories;

            goalEquationText.setText(formatter.format(goalCalories));
            foodEquationText.setText(formatter.format(foodCalories));
            exerciseEquationText.setText(formatter.format(exerciseCalories));
            remainingEquationText.setText(formatter.format(remainingCalories));
        }
    }



    private void setDailyTexts(){

        setDateText();

        Spinner weightSpinner = (Spinner) findViewById(R.id.weight_spinner);

        if(repo.scaleWeightRecorded(date)) {
            weightSpinner.setSelection(ESTIMATED_WEIGHT_POSITION);
        }else{
            weightSpinner.setSelection(SCALE_WEIGHT_POSITION);
        }

        setWeightText();

        if(usesCBTracking) {
            setExerciseText();
            setSmallSnackText();
            setSnackText();
            setSmallMealText();
            setMealText();
            setLargeMealText();
            setBeverageText();
            grayOutTexts();
            blackOutTexts();
        }else{
            setEquationTexts();
        }

        setOtherCalsText();
        setOtherExerciseText();

    }


    private void grayOutTexts(){
        TextView smallSnackText = (TextView) findViewById(R.id.small_snack_text);
        TextView snackText = (TextView) findViewById(R.id.large_snack_text);
        TextView smallMealText = (TextView) findViewById(R.id.small_meal_text);
        TextView mealText = (TextView) findViewById(R.id.regular_meal_text);
        TextView largeMealText = (TextView) findViewById(R.id.large_meal_text);
        TextView beverageText = (TextView) findViewById(R.id.beverage_text);


        if(usesCBTracking) {
            double caloriesRemaining = repo.getBaseGoalCalories(date) - repo.getTotalCalories(date)
                    + repo.getTotalExerciseCalories(date);

            double[] calorieArray = repo.getMealCategoryCalories(date);


            int disabledColor = getColor(R.color.secondary_text_material_light_1);

            if (calorieArray[SMALL_SNACK_INDEX] > caloriesRemaining) {
                smallSnackText.setTextColor(disabledColor);
            }

            if (calorieArray[LARGE_SNACK_INDEX] > caloriesRemaining) {
                snackText.setTextColor(disabledColor);
            }

            if (calorieArray[SMALL_MEAL_INDEX] > caloriesRemaining) {
                smallMealText.setTextColor(disabledColor);
            }

            if (calorieArray[REGULAR_MEAL_INDEX] > caloriesRemaining) {
                mealText.setTextColor(disabledColor);
            }

            if (calorieArray[LARGE_MEAL_INDEX] > caloriesRemaining) {
                largeMealText.setTextColor(disabledColor);
            }

            if (calorieArray[BEVERAGE_INDEX] > caloriesRemaining) {
                beverageText.setTextColor(disabledColor);
            }
        }
    }

    private void blackOutTexts(){

        TextView smallSnackText = (TextView) findViewById(R.id.small_snack_text);
        TextView snackText = (TextView) findViewById(R.id.large_snack_text);
        TextView smallMealText = (TextView) findViewById(R.id.small_meal_text);
        TextView mealText = (TextView) findViewById(R.id.regular_meal_text);
        TextView largeMealText = (TextView) findViewById(R.id.large_meal_text);
        TextView beverageText = (TextView) findViewById(R.id.beverage_text);

        if(usesCBTracking) {
            double caloriesRemaining = repo.getBaseGoalCalories(date) - repo.getTotalCalories(date)
                    + repo.getTotalExerciseCalories(date);

            double[] calorieArray = repo.getMealCategoryCalories(date);

            int enabledColor = getColor(R.color.primary_text_material_dark);

            if (calorieArray[SMALL_SNACK_INDEX] < caloriesRemaining) {
                smallSnackText.setTextColor(enabledColor);
            }

            if (calorieArray[LARGE_SNACK_INDEX] < caloriesRemaining) {
                snackText.setTextColor(enabledColor);
            }

            if (calorieArray[SMALL_MEAL_INDEX] < caloriesRemaining) {
                smallMealText.setTextColor(enabledColor);
            }

            if (calorieArray[REGULAR_MEAL_INDEX] < caloriesRemaining) {
                mealText.setTextColor(enabledColor);
            }

            if (calorieArray[LARGE_MEAL_INDEX] < caloriesRemaining) {
                largeMealText.setTextColor(enabledColor);
            }

            if (calorieArray[BEVERAGE_INDEX] < caloriesRemaining) {
                beverageText.setTextColor(enabledColor);
            }
        }
    }

    private void scheduleSmoothing(){
        ComponentName serviceName = new ComponentName(this, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .build();

        JobScheduler scheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);


    }

}

