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



class HistoryChartHelper {


    static void updateChart(Context context, LineChart chart, Repository repo,
                            int daysToGraph){

        ArrayList<Entry> trueEntries = new ArrayList<>();
        //ArrayList<Entry> lastEntries = new ArrayList<>();
        //ArrayList<Entry> currentDayEntries = new ArrayList<>();
        //ArrayList<Entry> maxEntries = new ArrayList<>();
        //ArrayList<Entry> minEntries = new ArrayList<>();
        //ArrayList<Entry> goalEntries = new ArrayList<>();
        ArrayList<String> xAxis = new ArrayList<>();

        LocalDate startDate, today;

        today = Repository.getToday();
        //yesterday = today.minusDays(1);


        startDate = today.minusDays(daysToGraph);

        if(startDate.isBefore(repo.getFirstRecordDate())){
            startDate = repo.getFirstRecordDate();
        }

        if(daysToGraph < 10){
            repo.smooth(today,today.minusDays(9));
        }

        trueEntries= repo.getWeights(today, startDate,false);



        //tempDate = startDate.minusDays(1);

        //graphToDate = today.plusDays(1);

        //double minEntryMargin = 0.9995;

        //double maxEntryMargin = 1.0005;

        //double currentWeight;

        for(int i =0; i<= trueEntries.size()+1;i++){
            if(i == 1){
                if(i+1 == trueEntries.size()){
                    xAxis.add(context.getString(R.string.yesterday));
                } else if(startDate.isAfter(today.minusMonths(5))) {
                    xAxis.add(android.text.format.DateFormat.format("MMM dd", startDate.toDate()).toString());
                }else{
                    xAxis.add(android.text.format.DateFormat.format("MM/dd/yy", startDate.toDate()).toString());
                }
            } else if(i==trueEntries.size()){
                xAxis.add(context.getString(R.string.graph_today)+"  ");
            }else{
                xAxis.add("");
            }
        }



        LineDataSet trueDataSet = new LineDataSet(trueEntries, "Weight");
        //LineDataSet minDataSet = new LineDataSet(minEntries, "minWeights");
        //LineDataSet maxDataSet = new LineDataSet(maxEntries, "maxWeights");
        //LineDataSet lastDataSet = new LineDataSet(lastEntries, "Weight");
        //LineDataSet goalDataSet = new LineDataSet(goalEntries, "goalEntries");
        //LineDataSet currentDayDataSet = new LineDataSet(currentDayEntries, "Weight");
        int graphColor = ContextCompat.getColor(context, R.color.primary_material_light);
        //int lastDayColor = ContextCompat.getColor(context, R.color.primary_material_light_2);
        //int transGoalColor = ContextCompat.getColor(context, R.color.accent_material_light_2);
        //int goalColor = ContextCompat.getColor(context, R.color.accent_material_light);

        //if(!repo.getCaloriesRecorded(today)){
        //    int transparent = ContextCompat.getColor(context, android.R.color.transparent);
        //    lastDataSet.setCircleColorHole(transparent);
        //    lastDataSet.setColor(transparent);
        //    lastDataSet.setCircleColor(transparent);
        //}else{
        //    lastDataSet.setCircleColorHole(graphColor);
        //    lastDataSet.setColor(lastDayColor);
        //    lastDataSet.setCircleColor(graphColor);
        //}

        //int dashedLineLength =1;
        //int dashedLineGapLength = 1;
        //lastDataSet.enableDashedLine(dashedLineLength, dashedLineGapLength, 2);

        //lastDataSet.setCircleRadius(4);
        //lastDataSet.setCircleHoleRadius(3);

        //lastDataSet.setLineWidth(3);

        //minDataSet.setColor(android.R.color.transparent);
        //minDataSet.setCircleColor(android.R.color.transparent);
        //minDataSet.setCircleColorHole(android.R.color.transparent);

        //maxDataSet.setColor(android.R.color.transparent);
        //maxDataSet.setCircleColor(android.R.color.transparent);
        //maxDataSet.setCircleColorHole(android.R.color.transparent);

        /*
        goalDataSet.setColor(transGoalColor);
        goalDataSet.setCircleColorHole(goalColor);
        goalDataSet.setCircleColor(goalColor);
        //goalDataSet.enableDashedLine(dashedLineLength, dashedLineGapLength,2);
        goalDataSet.setCircleRadius(4);
        goalDataSet.setCircleHoleRadius(1);
        goalDataSet.setLineWidth(3);
        */

        trueDataSet.setColor(graphColor);
        trueDataSet.setLineWidth(3);
        trueDataSet.setDrawCircles(true);
        trueDataSet.setCircleColor(graphColor);
        trueDataSet.setCircleColorHole(graphColor);


        trueDataSet.setCircleRadius(1);


/*
        currentDayDataSet.setColor(graphColor);
        currentDayDataSet.setCircleRadius(4);
        currentDayDataSet.setCircleColor(graphColor);
        currentDayDataSet.setCircleColorHole(graphColor);
        */

        LineData data = new LineData();

        /*
        data.addDataSet(minDataSet);
        data.addDataSet(maxDataSet);
        */


        data.addDataSet(trueDataSet);


        data.setXVals(xAxis);
        data.setDrawValues(false);

        chart.setData(data);
        chart.setDrawGridBackground(false);
        chart.getAxisLeft().setDrawGridLines(true);

        if(startDate.isAfter(today.minusWeeks(1).minusDays(2))) {
            chart.getXAxis().setDrawGridLines(true);
        }else{
            chart.getXAxis().setDrawGridLines(false);
        }
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


        //For using left axis.
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setValueFormatter(new MyYAxisValueFormatter(repo.getUsesLbs()));
        chart.getAxisLeft().setTextSize(14);
        chart.getAxisLeft().setDrawLabels(true);
        chart.getAxisLeft().setMinWidth(48);
        chart.getAxisLeft().setLabelCount(4,false );

/*
        //For using right axis
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setValueFormatter(new MyYAxisValueFormatter(repo.getUsesLbs()));
        chart.getAxisRight().setTextSize(14);
        chart.getAxisRight().setDrawLabels(true);
        chart.getAxisRight().setMinWidth(48);
*/



        chart.getXAxis().setTextSize(14);
        chart.setExtraTopOffset(3);

        chart.getLegend().setEnabled(true);


        chart.getLegend().setTextSize(14);
        chart.getLegend().setDrawInside(true);
        chart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        chart.getLegend().setFormToTextSpace(3);
        chart.getLegend().setXEntrySpace(8);



        int[] colorArray = {graphColor};
        String[] lbsStringArray = {chart.getResources().getString(R.string.weight)};
        String[] kgsStringArray = {chart.getResources().getString(R.string.weight)};

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
