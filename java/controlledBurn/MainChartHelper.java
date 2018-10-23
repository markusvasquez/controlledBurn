package controlledBurn;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.controlledBurn.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import org.joda.time.LocalDate;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by markusvasquez on 4/4/17.
 */

public class MainChartHelper {

    public static final int NUM_CHART_DAYS = 6;
    final int DAYS_IN_FUTURE =2;

    final double MIN_ENTRY_MARGIN = 0.9965;

    final double MAX_ENTRY_MARGIN = 1.0005;


    LineChart chart;

    ArrayList<Entry> trueEntries;
    ArrayList<Entry> lastEntries;
    ArrayList<Entry> maxEntries;
    ArrayList<Entry> minEntries;
    ArrayList<Entry> goalEntries;
    ArrayList<String> xAxis;

    Repository repo;
    Context context;

    private int tomorrowIndex;


    MainChartHelper(LineChart chartPar, Repository repoPar, Context contextPar){

        chart = chartPar;
        repo = repoPar;
        context = contextPar;

        trueEntries = new ArrayList<>();
        lastEntries = new ArrayList<>();
        maxEntries = new ArrayList<>();
        minEntries = new ArrayList<>();
        goalEntries = new ArrayList<>();
        xAxis = new ArrayList<>();

        updateDataSets();


        LineDataSet trueDataSet = new LineDataSet(trueEntries, "Weight");
        LineDataSet minDataSet = new LineDataSet(minEntries, "minWeights");
        LineDataSet maxDataSet = new LineDataSet(maxEntries, "maxWeights");
        LineDataSet lastDataSet = new LineDataSet(lastEntries, "Weight");
        LineDataSet goalDataSet = new LineDataSet(goalEntries, "goalEntries");
        int graphColor = ContextCompat.getColor(context, R.color.primary_material_light);
        int lastDayColor = ContextCompat.getColor(context, R.color.primary_material_light_2);
        int transGoalColor = ContextCompat.getColor(context, R.color.accent_material_light_2);
        int goalColor = ContextCompat.getColor(context, R.color.accent_material_light);


        lastDataSet.setCircleColorHole(graphColor);
        lastDataSet.setColor(lastDayColor);
        lastDataSet.setCircleColor(graphColor);


        lastDataSet.setCircleRadius(4);
        lastDataSet.setCircleHoleRadius(3);

        lastDataSet.setLineWidth(3);

        minDataSet.setColor(android.R.color.transparent);
        minDataSet.setCircleColor(android.R.color.transparent);
        minDataSet.setCircleColorHole(android.R.color.transparent);

        maxDataSet.setColor(android.R.color.transparent);
        maxDataSet.setCircleColor(android.R.color.transparent);
        maxDataSet.setCircleColorHole(android.R.color.transparent);

        goalDataSet.setColor(transGoalColor);
        goalDataSet.setCircleColorHole(goalColor);
        goalDataSet.setCircleColor(goalColor);
        //goalDataSet.enableDashedLine(dashedLineLength, dashedLineGapLength,2);
        goalDataSet.setCircleRadius(4);
        goalDataSet.setCircleHoleRadius(1);
        goalDataSet.setLineWidth(3);

        trueDataSet.setColor(graphColor);
        trueDataSet.setLineWidth(3);
        trueDataSet.setDrawCircles(true);
        trueDataSet.setCircleColor(graphColor);
        trueDataSet.setCircleColorHole(graphColor);


        trueDataSet.setCircleRadius(4);


        /*
        currentDayDataSet.setColor(graphColor);
        currentDayDataSet.setCircleRadius(4);
        currentDayDataSet.setCircleColor(graphColor);
        currentDayDataSet.setCircleColorHole(graphColor);
        */

        LineData data = new LineData();

        data.addDataSet(minDataSet);
        data.addDataSet(maxDataSet);

        data.addDataSet(goalDataSet);
        data.addDataSet(lastDataSet);
        //data.addDataSet(currentDayDataSet);


        data.addDataSet(trueDataSet);


        data.setXVals(xAxis);
        data.setDrawValues(false);

        chart.setData(data);
        chart.setDrawGridBackground(false);
        chart.getAxisLeft().setDrawGridLines(true);

        chart.getXAxis().setDrawGridLines(true);

        chart.getAxisRight().setDrawGridLines(false);
        chart.setDrawBorders(false);
        //chart.setBorderWidth(0);
        chart.setDescription("");
        chart.setPinchZoom(false);
        chart.setTouchEnabled(false);
        chart.notifyDataSetChanged();
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.TOP);
        chart.getXAxis().setLabelsToSkip(0);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setAvoidFirstLastClipping(true);
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisRight().setDrawAxisLine(false);


        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setValueFormatter(new MyYAxisValueFormatter(repo.getUsesLbs()));
        chart.getAxisLeft().setTextSize(14);
        chart.getAxisLeft().setDrawLabels(true);
        chart.getAxisLeft().setMinWidth(48);
        chart.getAxisLeft().setLabelCount(4,false );



        chart.getXAxis().setTextSize(14);
        chart.setExtraTopOffset(3);

        chart.getLegend().setEnabled(true);


        chart.getLegend().setTextSize(14);
        chart.getLegend().setDrawInside(true);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        chart.getLegend().setFormToTextSpace(3);
        chart.getLegend().setXEntrySpace(8);


        int[] colorArray = {graphColor, goalColor};
        String[] lbsStringArray = {chart.getResources().getString(R.string.weight), chart.getResources().getString(R.string.graph_goal_weight)};
        String[] kgsStringArray = {chart.getResources().getString(R.string.weight), chart.getResources().getString(R.string.graph_goal_weight)};

        if(repo.getUsesLbs()) {
            chart.getLegend().setCustom(colorArray,lbsStringArray);
        }else{
            chart.getLegend().setCustom(colorArray, kgsStringArray);
        }



        chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        chart.getLegend().setFormSize(10);


        chart.notifyDataSetChanged();

        chart.animateY(1);
    }

    void updateLastEntries(){

        if(lastEntries.size() == 2) {
            lastEntries.remove(1);
        }

        if(repo.getCaloriesRecorded(Repository.getToday())) {
            lastEntries.add(new Entry((float) repo.getWeightEst(Repository.getToday().plusDays(1),false), tomorrowIndex));
        }

        chart.notifyDataSetChanged();
        chart.invalidate();

    }

    void updateChart(){

        trueEntries.clear();
        lastEntries.clear();
        //currentDayEntries.clear();
        maxEntries.clear();
        minEntries.clear();
        goalEntries.clear();
        xAxis.clear();

        updateDataSets();


        chart.notifyDataSetChanged();

        chart.invalidate();
    }


    private void updateDataSets(){

        LocalDate startDate, today, tomorrow;

        today = Repository.getToday();

        tomorrow = today.plusDays(1);


        startDate = today.minusDays(NUM_CHART_DAYS);

        if(startDate.isBefore(repo.getFirstRecordDate())){
            startDate = repo.getFirstRecordDate();
        }

        repo.smooth(today,startDate);
        ArrayList<Entry> trueEntriesTemp = repo.getWeights(today,startDate,false);

        int trueEntriesSize = trueEntriesTemp.size();
        tomorrowIndex = trueEntriesSize+1;

        for(int j=0; j< trueEntriesSize;j++){
            float entryTemp = trueEntriesTemp.get(j).getVal();
            int indexTemp = trueEntriesSize-j;
            minEntries.add(new Entry((float) (MIN_ENTRY_MARGIN * entryTemp), indexTemp));
            maxEntries.add(new Entry((float) (MAX_ENTRY_MARGIN * entryTemp),indexTemp));
            trueEntries.add(new Entry((float) entryTemp,indexTemp));
        }

        lastEntries.add(new Entry(trueEntries.get(0).getVal(),trueEntriesSize));

        if(repo.getCaloriesRecorded(today)) {
            lastEntries.add(new Entry((float) repo.getWeightEst(tomorrow,false), tomorrowIndex));
        }

        goalEntries.add(new Entry(trueEntries.get(0).getVal(),trueEntriesSize));
        goalEntries.add(new Entry((float) repo.getImmediateGoalWeight(today,false), tomorrowIndex));



        if(trueEntriesSize <2){
            xAxis.add("");
            xAxis.add(context.getString(R.string.today));
            xAxis.add(context.getString(R.string.graph_tomorrow));
            xAxis.add("");
        }else if(trueEntriesSize==2) {
            xAxis.add("");
            xAxis.add("");
            xAxis.add(context.getString(R.string.graph_today));
            xAxis.add(context.getString(R.string.graph_tomorrow));
            xAxis.add("");
        }else{
            for(int j=0; j<=trueEntriesSize+2;j ++){

                if(j ==trueEntriesSize) {
                    xAxis.add(context.getString(R.string.graph_today));
                } else if (j == 1 && startDate.isBefore(today.minusDays(1))) {
                    if(startDate.isAfter(today.minusMonths(5))) {
                        xAxis.add(android.text.format.DateFormat.format("MMM dd", startDate.toDate()).toString());
                    }else{
                        xAxis.add(android.text.format.DateFormat.format("MM/dd/yy", startDate.toDate()).toString());
                    }
                } else{
                    xAxis.add("");
                }

            }

        }


    }

    private static class MyYAxisValueFormatter implements YAxisValueFormatter {

        private DecimalFormat mFormat;

        private MyYAxisValueFormatter (boolean usesLbs) {
            if(usesLbs) {
                mFormat = new DecimalFormat("##0.0"); // use one decimal
            }else{
                mFormat = new DecimalFormat("##0.00"); // use two decimal

            }
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            // write your logic here
            // access the YAxis object to get more information
            return mFormat.format(value); // e.g. append a dollar-sign
        }
    }
}
