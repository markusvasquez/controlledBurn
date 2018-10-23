package controlledBurn;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import com.controlledBurn.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by markusvasquez on 3/13/17.
 */

public class Help extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_help);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolbar);
        myToolBar.setNavigationIcon(getDrawable(R.drawable.ic_arrow_back_white_24dp));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    protected void onResume() {
        super.onResume();

        expListView = (ExpandableListView) findViewById(R.id.expandable_faq);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }


    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add(getString(R.string.question_1));
        List<String> question1 = new ArrayList<String>();
        question1.add(getString(R.string.answer_1));
        listDataChild.put(listDataHeader.get(0), question1);

        listDataHeader.add(getString(R.string.question_2));
        List<String> question2 = new ArrayList<String>();
        question2.add(getString(R.string.answer_2));
        listDataChild.put(listDataHeader.get(1), question2);

        listDataHeader.add(getString(R.string.question_3));
        List<String> question3 = new ArrayList<String>();
        question3.add(getString(R.string.answer_3));
        listDataChild.put(listDataHeader.get(2), question3);

        listDataHeader.add(getString(R.string.question_4));
        List<String> question4 = new ArrayList<String>();
        question4.add(getString(R.string.answer_4));
        listDataChild.put(listDataHeader.get(3), question4);

        listDataHeader.add(getString(R.string.question_5));
        List<String> question5 = new ArrayList<String>();
        question5.add(getString(R.string.answer_5));
        listDataChild.put(listDataHeader.get(4), question5);

        listDataHeader.add(getString(R.string.question_6));
        List<String> question6 = new ArrayList<String>();
        question6.add(getString(R.string.answer_6));
        listDataChild.put(listDataHeader.get(5), question6);

        listDataHeader.add(getString(R.string.question_7));
        List<String> question7 = new ArrayList<String>();
        question7.add(getString(R.string.answer_7));
        listDataChild.put(listDataHeader.get(6), question7);

        listDataHeader.add(getString(R.string.question_8));
        List<String> question8 = new ArrayList<String>();
        question8.add(getString(R.string.answer_8));
        listDataChild.put(listDataHeader.get(7), question8);

        listDataHeader.add(getString(R.string.question_9));
        List<String> question9 = new ArrayList<String>();
        question9.add(getString(R.string.answer_9));
        listDataChild.put(listDataHeader.get(8), question9);

        listDataHeader.add(getString(R.string.question_10));
        List<String> question10 = new ArrayList<String>();
        question10.add(getString(R.string.answer_10));
        listDataChild.put(listDataHeader.get(9), question10);

        listDataHeader.add(getString(R.string.question_11));
        List<String> question11 = new ArrayList<String>();
        question11.add(getString(R.string.answer_11));
        listDataChild.put(listDataHeader.get(10), question11);

    }
}