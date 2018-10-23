package controlledBurn;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.controlledBurn.R;
import com.github.mikephil.charting.charts.LineChart;

import java.text.DecimalFormat;


/**
 * Created by markusvasquez on 3/13/17.
 */

public class History extends AppCompatActivity {

    Repository repo;

    final static int TEN_DAYS_INDEX = 0;
    final static int TEN_DAYS_DAYS_TO_GRAPH = 9;

    final static int MONTH_INDEX = 1;
    final static int MONTH_DAYS_TO_GRAPH = 30;

    final static int SIX_MONTHS_INDEX = 2;
    final static int SIX_MONTHS_DAYS_TO_GRAPH = 180;

    final static int YEAR_INDEX = 3;
    final static int YEAR_DAYS_TO_GRAPH = 364;

    final static int TWO_YEARS_INDEX = 4;
    final static int TWO_YEARS_DAYS_TO_GRAPH = 729;

    final static int MAX_INDEX = 5;
    final static int MAX_DAYS_TO_GRAPH = 36400;

    LineChart chart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        if(getResources().getConfiguration().orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setContentView(R.layout.portrait_history_layout);
        }else{
            setContentView(R.layout.landscape_history_layout);
        }

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        myToolBar.setNavigationIcon(getDrawable(R.drawable.ic_arrow_back_white_24dp));
        myToolBar.setTitle(R.string.history);

        setSupportActionBar(myToolBar);

        repo = MainActivity.getRepository();

        if(repo==null) {
            repo = new Repository(getFilesDir().toURI());
            repo.setupRepository();
        }

        chart = (LineChart) findViewById(R.id.chart);

        //Setup the unit spinner.
        final Spinner timeSpinner = (Spinner) findViewById(R.id.time_spinner);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this,
                R.array.time_array, android.R.layout.simple_spinner_item);

        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);


        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {

                if(spinnerPosition == TEN_DAYS_INDEX){
                    HistoryChartHelper.updateChart(timeSpinner.getContext(), chart,repo, TEN_DAYS_DAYS_TO_GRAPH);
                }else if(spinnerPosition == MONTH_INDEX){
                    HistoryChartHelper.updateChart(timeSpinner.getContext(), chart,repo, MONTH_DAYS_TO_GRAPH);
                }else if(spinnerPosition == SIX_MONTHS_INDEX){
                    HistoryChartHelper.updateChart(timeSpinner.getContext(), chart,repo, SIX_MONTHS_DAYS_TO_GRAPH);
                }else if(spinnerPosition == YEAR_INDEX){
                    HistoryChartHelper.updateChart(timeSpinner.getContext(), chart,repo, YEAR_DAYS_TO_GRAPH);
                }else if(spinnerPosition == TWO_YEARS_INDEX){
                    HistoryChartHelper.updateChart(timeSpinner.getContext(), chart,repo, TWO_YEARS_DAYS_TO_GRAPH);
                }else if(spinnerPosition == MAX_INDEX){
                    HistoryChartHelper.updateChart(timeSpinner.getContext(), chart,repo, MAX_DAYS_TO_GRAPH);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        timeSpinner.setSelection(TEN_DAYS_INDEX);

        TextView progressText = (TextView) findViewById(R.id.progress_text);

        double goalWeight = repo.getGoalWeight(false);
        double currentWeight = repo.getWeightEst(Repository.getToday(),false);
        double startWeight = repo.getStartWeight(false);
        String unit;

        if(repo.getUsesLbs()){
            unit = getString(R.string.lbs);
        }else{
            unit = getString(R.string.kgs);
        }
        DecimalFormat formatter = new DecimalFormat("#0.0");
        String progressString;


        if(goalWeight >0 && goalWeight <= startWeight && currentWeight <= startWeight-0.2){
            progressString = getString(R.string.youve_lost)+" " +
                    formatter.format(startWeight-currentWeight) + " " +
                    unit +"!";
            progressText.setText(progressString);
        }else if(goalWeight >= startWeight && currentWeight >= startWeight + 0.2){
            progressString= getString(R.string.youve_gained) + " " +
                    formatter.format(currentWeight-startWeight) + " " + unit + "!";
            progressText.setText(progressString);
        }else{
            CardView progressCard = (CardView) findViewById(R.id.progress_card);
            progressCard.setVisibility(View.GONE);
        }

        ImageButton progressButton = (ImageButton) findViewById(R.id.progress_share);

        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String unit;
                double goalWeight = repo.getGoalWeight(false);
                double startWeight = repo.getStartWeight(false);
                double currentWeight = repo.getWeightEst(Repository.getToday(),false);
                if(repo.getUsesLbs()){
                    unit = getString(R.string.lbs);
                }else{
                    unit = getString(R.string.kgs);
                }

                DecimalFormat formatter = new DecimalFormat("#0.0");
                String progressString;
                if(goalWeight <= startWeight && currentWeight <= startWeight-0.2){
                    progressString = getString(R.string.ive_lost)+" " +
                            formatter.format(startWeight-currentWeight) + " " +
                            unit +" " + getString(R.string.with_cb) +"\n\n"+getString(R.string.cb_url);
                }else{
                    progressString= getString(R.string.youve_gained) + " " +
                            formatter.format(currentWeight-startWeight) + " " + unit +
                            " " + getString(R.string.with_cb)+"\n"+getString(R.string.cb_url);
                }

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, progressString);

                startActivity(Intent.createChooser(share, "Share with"));
            }
        });

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
    }


}