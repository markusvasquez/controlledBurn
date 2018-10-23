package controlledBurn;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.controlledBurn.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;


/**
 * Created by markusvasquez on 3/13/17.
 */

public class Settings extends AppCompatActivity {

    final static int LOSE_INDEX = 0;
    final static int MAINTAIN_INDEX = 1;
    final static int GAIN_INDEX = 2;

    final static int LBS_INDEX = 0;
    final static int KGS_INDEX = 1;

    final static int INTERNAL_INDEX = 0;
    final static int EXTERNAL_INDEX = 1;


    Repository repo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);


        setContentView(R.layout.settings_layout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        myToolBar.setNavigationIcon(getDrawable(R.drawable.ic_arrow_back_white_24dp));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        repo = MainActivity.getRepository();

        if(repo==null) {
            repo = new Repository(getFilesDir().toURI());
            repo.setupRepository();
        }

        //Setup the unit spinner.
        Spinner unitSpinner = (Spinner) findViewById(R.id.weight_unit_spinner);
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(this,
                R.array.unit_array, android.R.layout.simple_spinner_item);

        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        if(repo.getUsesLbs()){
            unitSpinner.setSelection(LBS_INDEX);
        }else{
            unitSpinner.setSelection(KGS_INDEX);
        }

        final EditText goalEntry = (EditText) findViewById(R.id.goal_edit_text);
        final NumberFormat formatter = new DecimalFormat("#0");

        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {

                if(spinnerPosition == LBS_INDEX){
                    repo.setWeightUnits(true);
                }else{
                    repo.setWeightUnits(false);
                }

                String unit;
                if(repo.getUsesLbs()){
                    unit = getString(R.string.lbs);
                }else{
                    unit = getString(R.string.kgs);
                }

                if(repo.getGoalWeight(false)>0) {
                    goalEntry.setHint(formatter.format(repo.getGoalWeight(false)) +" "+unit);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String unit;

        if(repo.getGoalWeight(false)>0.0){

            if(repo.getUsesLbs()){
                unit = getString(R.string.lbs);
            }else{
                unit = getString(R.string.kgs);
            }

            goalEntry.setHint(formatter.format(repo.getGoalWeight(false))+" "+unit);
        }

        goalEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED){
                    String entry = goalEntry.getText().toString();
                    if(!entry.equals("")) {
                        repo.setGoalWeight(Double.parseDouble(entry));
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        goalEntry.setText("");
                        String inUnit;
                        if(repo.getUsesLbs()){
                            inUnit = getString(R.string.lbs);
                        }else{
                            inUnit = getString(R.string.kgs);
                        }
                        goalEntry.setHint(formatter.format(repo.getGoalWeight(false))+ " "+inUnit);
                        goalEntry.clearFocus();
                        repo.updateGoal();
                    }else{
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        goalEntry.clearFocus();
                    }
                }
                return false;
            }
        });

        ImageButton goalAcceptButton = (ImageButton) findViewById(R.id.goal_weight_accept);

        goalAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String entry = goalEntry.getText().toString();
                if(!entry.equals("")) {
                    repo.setGoalWeight(Double.parseDouble(entry));
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    goalEntry.setText("");
                    String inUnit;
                    if(repo.getUsesLbs()){
                        inUnit = getString(R.string.lbs);
                    }else{
                        inUnit = getString(R.string.kgs);
                    }
                    goalEntry.setHint(formatter.format(repo.getGoalWeight(false))+ " "+inUnit);
                    goalEntry.clearFocus();
                    repo.updateGoal();
                }else{
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    goalEntry.clearFocus();
                }

            }
        });



        //Setup the tracking spinner.
        Spinner trackingSpinner = (Spinner) findViewById(R.id.tracking_spinner);
        ArrayAdapter<CharSequence> trackingAdapter = ArrayAdapter.createFromResource(this,
                R.array.tracking_array, android.R.layout.simple_spinner_item);

        trackingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trackingSpinner.setAdapter(trackingAdapter);

        if(repo.getInternalTracking()){
            trackingSpinner.setSelection(INTERNAL_INDEX);
        }else{
            trackingSpinner.setSelection(EXTERNAL_INDEX);
        }

        trackingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {

                if(spinnerPosition == INTERNAL_INDEX){
                    repo.setInternalTracking(true);
                }else{
                    repo.setInternalTracking(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final TextView resetText = (TextView) findViewById(R.id.reset_button);

        resetText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                repo.clearRepository();
                                System.exit(0);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(resetText.getContext());
                builder.setMessage(R.string.reset_warning).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
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