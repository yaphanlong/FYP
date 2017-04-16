package zoo.mandai.fyp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.tooltip.Tooltip;

import java.text.DecimalFormat;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMOreg;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class PredictionActivity extends AppCompatActivity {

    //Monthly Prediction Card
    @BindView(R.id.monthlyPredictionChart) CombinedChart monthlyPredictionChart;
    @BindView(R.id.checkBoxMonthlyPR) CheckBox checkBoxMonthlyPR;
    @BindView(R.id.checkBoxMonthlyPRE) CheckBox checkBoxMonthlyPRE;
    @BindView(R.id.checkBoxMonthlyPREW) CheckBox checkBoxMonthlyPREW;
    @BindView(R.id.checkBoxMonthlyPREWO) CheckBox checkBoxMonthlyPREWO;
    @BindView(R.id.checkBoxMonthlyPREWOL) CheckBox checkBoxMonthlyPREWOL;
    @BindView(R.id.textCCValuePR) TextView textCCValuePR;
    @BindView(R.id.textCCValuePRE) TextView textCCValuePRE;
    @BindView(R.id.textCCValuePREW) TextView textCCValuePREW;
    @BindView(R.id.textCCValuePREWO) TextView textCCValuePREWO;
    @BindView(R.id.textCCValuePREWOL) TextView textCCValuePREWOL;
    @BindView(R.id.textMAEValuePR) TextView textMAEValuePR;
    @BindView(R.id.textMAEValuePRE) TextView textMAEValuePRE;
    @BindView(R.id.textMAEValuePREW) TextView textMAEValuePREW;
    @BindView(R.id.textMAEValuePREWO) TextView textMAEValuePREWO;
    @BindView(R.id.textMAEValuePREWOL) TextView textMAEValuePREWOL;

    //Daily Prediction Card
    @BindView(R.id.dailyPredictionChart) CombinedChart dailyPredictionChart;
    @BindView(R.id.dailyPredictionTitle) TextView dailyPredictionTitle;
    @BindView(R.id.checkBoxDailyPR) CheckBox checkBoxDailyPR;
    @BindView(R.id.checkBoxDailyPRE) CheckBox checkBoxDailyPRE;
    @BindView(R.id.checkBoxDailyPREW) CheckBox checkBoxDailyPREW;
    @BindView(R.id.checkBoxDailyPREWO) CheckBox checkBoxDailyPREWO;
    @BindView(R.id.checkBoxDailyPREWOL) CheckBox checkBoxDailyPREWOL;

    @BindView(R.id.toolbar) Toolbar toolbar;

    private String[] mMonths = new String[]{
            "Nov", "Dec", "Jan", "Feb"
    };
    private List<BarEntry> barEntriesFootFallMonth = new ArrayList<>();
    private List<Entry> lineEntriesPR = new ArrayList<>();
    private List<Entry> lineEntriesPRE = new ArrayList<>();
    private List<Entry> lineEntriesPREW = new ArrayList<>();
    private List<Entry> lineEntriesPREWO = new ArrayList<>();
    private List<Entry> lineEntriesPREWOL = new ArrayList<>();
    private LineDataSet setMonthlyPR, setMonthlyPRE, setMonthlyPREW, setMonthlyPREWO, setMonthlyPREWOL,
            setDailyPR, setDailyPRE, setDailyPREW, setDailyPREWO, setDailyPREWOL;
    private CombinedData combinedDataMonthly = new CombinedData();
    private CombinedData combinedDataDaily = new CombinedData();

    private String[] dateString;

    private Map<Integer, Map<String, Integer>> actualMonthMap = new LinkedHashMap<>();
    private Map<Integer, Map<String, Integer>> predictPRMonthMap = new LinkedHashMap<>();
    private Map<Integer, Map<String, Integer>> predictPREMonthMap = new LinkedHashMap<>();
    private Map<Integer, Map<String, Integer>> predictPREWMonthMap = new LinkedHashMap<>();
    private Map<Integer, Map<String, Integer>> predictPREWOMonthMap = new LinkedHashMap<>();
    private Map<Integer, Map<String, Integer>> predictPREWOLMonthMap = new LinkedHashMap<>();

    private int[] prediction(Instances trainDataset, Instances testDataset, String[] options, Map<Integer, Map<String, Integer>> predictMap, int model) {
        int[] predictCount = new int[]{0, 0, 0, 0};
        try {

            Remove remove = new Remove();
            remove.setOptions(options);
            remove.setInputFormat(trainDataset);
            //filter both training and test set based on different heuristics
            Instances trainDatasetFiltered = Filter.useFilter(trainDataset, remove);
            trainDatasetFiltered.setClassIndex(trainDatasetFiltered.numAttributes() - 1);

            Instances testDatasetFiltered = Filter.useFilter(testDataset, remove);
            testDatasetFiltered.setClassIndex(testDatasetFiltered.numAttributes() - 1);

            //train filtered training set using SMOreg
            SMOreg smo = new SMOreg();
            smo.buildClassifier(trainDatasetFiltered);

            //evaluate each model trained
            DecimalFormat df = new DecimalFormat("0.000");
            Evaluation eval = new Evaluation(trainDatasetFiltered);
            eval.crossValidateModel(smo, testDatasetFiltered, 10, new Random(1));
            if (model == 1) {
                textCCValuePR.setText(String.valueOf(df.format(eval.correlationCoefficient())));
                textMAEValuePR.setText(String.valueOf(df.format(eval.meanAbsoluteError())));
            } else if (model == 2) {
                textCCValuePRE.setText(String.valueOf(df.format(eval.correlationCoefficient())));
                textMAEValuePRE.setText(String.valueOf(df.format(eval.meanAbsoluteError())));
            } else if (model == 3) {
                textCCValuePREW.setText(String.valueOf(df.format(eval.correlationCoefficient())));
                textMAEValuePREW.setText(String.valueOf(df.format(eval.meanAbsoluteError())));
            } else if (model == 4) {
                textCCValuePREWO.setText(String.valueOf(df.format(eval.correlationCoefficient())));
                textMAEValuePREWO.setText(String.valueOf(df.format(eval.meanAbsoluteError())));
            } else if (model == 5) {
                textCCValuePREWOL.setText(String.valueOf(df.format(eval.correlationCoefficient())));
                textMAEValuePREWOL.setText(String.valueOf(df.format(eval.meanAbsoluteError())));
            }

            //store predicted monthly and daily footfall for each model trained
            for (int i = 0; i < testDatasetFiltered.numInstances(); i++) {
                if (testDataset.instance(i).stringValue(0).contains("Nov")) {
                    Map<String, Integer> predictDayMap = predictMap.get(0);
                    predictDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i))));
                    predictMap.put(0, predictDayMap);
                    predictCount[0] = predictCount[0] + (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i)));

                } else if (testDataset.instance(i).stringValue(0).contains("Dec")) {
                    Map<String, Integer> predictDayMap = predictMap.get(1);
                    predictDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i))));
                    predictMap.put(1, predictDayMap);
                    predictCount[1] = predictCount[1] + (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i)));

                } else if (testDataset.instance(i).stringValue(0).contains("Jan")) {
                    Map<String, Integer> predictDayMap = predictMap.get(2);
                    predictDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i))));
                    predictMap.put(2, predictDayMap);
                    predictCount[2] = predictCount[2] + (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i)));

                } else if (testDataset.instance(i).stringValue(0).contains("Feb")) {
                    Map<String, Integer> predictDayMap = predictMap.get(3);
                    predictDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i))));
                    predictMap.put(3, predictDayMap);
                    predictCount[3] = predictCount[3] + (int) Math.round(smo.classifyInstance(testDatasetFiltered.instance(i)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return predictCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        checkBoxMonthlyPR.setChecked(false);
        checkBoxMonthlyPRE.setChecked(false);
        checkBoxMonthlyPREW.setChecked(false);
        checkBoxMonthlyPREWO.setChecked(false);
        checkBoxMonthlyPREWOL.setChecked(false);

        for (int i = 0; i < 4; i++) {
            actualMonthMap.put(i, new LinkedHashMap<>());
            predictPRMonthMap.put(i, new LinkedHashMap<>());
            predictPREMonthMap.put(i, new LinkedHashMap<>());
            predictPREWMonthMap.put(i, new LinkedHashMap<>());
            predictPREWOMonthMap.put(i, new LinkedHashMap<>());
            predictPREWOLMonthMap.put(i, new LinkedHashMap<>());
        }

        int[] actualCount = new int[]{0, 0, 0, 0};
        int[] predictPRCount = new int[]{0, 0, 0, 0};
        int[] predictPRECount = new int[]{0, 0, 0, 0};
        int[] predictPREWCount = new int[]{0, 0, 0, 0};
        int[] predictPREWOCount = new int[]{0, 0, 0, 0};
        int[] predictPREWOLCount = new int[]{0, 0, 0, 0};

        try {
            ConverterUtils.DataSource trainSource = new ConverterUtils.DataSource(PredictionActivity.this.getAssets().open("training.arff"));
            Instances trainDataset = trainSource.getDataSet();
            //read training set from assets folder
            ConverterUtils.DataSource testSource = new ConverterUtils.DataSource(PredictionActivity.this.getAssets().open("test.arff"));
            Instances testDataset = testSource.getDataSet();
            testDataset.setClassIndex(testDataset.numAttributes() - 1);
            //read test set from assets folder
            //train and test model using different heuristics
            predictPRCount = prediction(trainDataset, testDataset, new String[]{"-R", "1,4,5,6,7"}, predictPRMonthMap, 1);
            predictPRECount = prediction(trainDataset, testDataset, new String[]{"-R", "1,5,6,7"}, predictPREMonthMap, 2);
            predictPREWCount = prediction(trainDataset, testDataset, new String[]{"-R", "1,6,7"}, predictPREWMonthMap, 3);
            predictPREWOCount = prediction(trainDataset, testDataset, new String[]{"-R", "1,7"}, predictPREWOMonthMap, 4);
            predictPREWOLCount = prediction(trainDataset, testDataset, new String[]{"-R", "1"}, predictPREWOLMonthMap, 5);

            //store actual monthly and daily footfall
            for (int i = 0; i < testDataset.numInstances(); i++) {
                if (testDataset.instance(i).stringValue(0).contains("Nov")) {
                    Map<String, Integer> actualDayMap = actualMonthMap.get(0);
                    actualDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(testDataset.instance(i).classValue()));
                    actualMonthMap.put(0, actualDayMap);
                    actualCount[0] = actualCount[0] + (int) Math.round(testDataset.instance(i).classValue());

                } else if (testDataset.instance(i).stringValue(0).contains("Dec")) {
                    Map<String, Integer> actualDayMap = actualMonthMap.get(1);
                    actualDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(testDataset.instance(i).classValue()));
                    actualMonthMap.put(1, actualDayMap);
                    actualCount[1] = actualCount[1] + (int) Math.round(testDataset.instance(i).classValue());

                } else if (testDataset.instance(i).stringValue(0).contains("Jan")) {
                    Map<String, Integer> actualDayMap = actualMonthMap.get(2);
                    actualDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(testDataset.instance(i).classValue()));
                    actualMonthMap.put(2, actualDayMap);
                    actualCount[2] = actualCount[2] + (int) Math.round(testDataset.instance(i).classValue());

                } else if (testDataset.instance(i).stringValue(0).contains("Feb")) {
                    Map<String, Integer> actualDayMap = actualMonthMap.get(3);
                    actualDayMap.put(testDataset.instance(i).stringValue(0), (int) Math.round(testDataset.instance(i).classValue()));
                    actualMonthMap.put(3, actualDayMap);
                    actualCount[3] = actualCount[3] + (int) Math.round(testDataset.instance(i).classValue());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        monthlyPredictionChart.setDrawBarShadow(false);
        monthlyPredictionChart.getDescription().setEnabled(false);
        monthlyPredictionChart.setPinchZoom(false);
        monthlyPredictionChart.setDoubleTapToZoomEnabled(false);
        monthlyPredictionChart.setScaleEnabled(false);
        monthlyPredictionChart.setDrawGridBackground(false);

        XAxis xAxis = monthlyPredictionChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);
        xAxis.setValueFormatter((value, axis) -> mMonths[(int) value % mMonths.length]);

        YAxis leftAxis = monthlyPredictionChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new LargeValueFormatter());
        YAxis rightAxis = monthlyPredictionChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setValueFormatter(new LargeValueFormatter());

        Legend l = monthlyPredictionChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        for (int i = 0; i < 4; i++) {
            barEntriesFootFallMonth.add(new BarEntry(i, actualCount[i]));
            lineEntriesPR.add(new Entry(i, predictPRCount[i]));
            lineEntriesPRE.add(new Entry(i, predictPRECount[i]));
            lineEntriesPREW.add(new Entry(i, predictPREWCount[i]));
            lineEntriesPREWO.add(new Entry(i, predictPREWOCount[i]));
            lineEntriesPREWOL.add(new Entry(i, predictPREWOLCount[i]));
        }

        setMonthlyPR = new LineDataSet(lineEntriesPR, getResources().getString(R.string.psi_rainfall));
        setLineAttributes(setMonthlyPR, R.color.purple, false, "#9C27B0");

        setMonthlyPRE = new LineDataSet(lineEntriesPRE, getResources().getString(R.string.psi_rainfall_event));
        setLineAttributes(setMonthlyPRE, R.color.lightblue, false, "#03A9F4");

        setMonthlyPREW = new LineDataSet(lineEntriesPREW, getResources().getString(R.string.psi_rainfall_event_weekend));
        setLineAttributes(setMonthlyPREW, R.color.pink, false, "#E91E63");

        setMonthlyPREWO = new LineDataSet(lineEntriesPREWO, getResources().getString(R.string.psi_rainfall_event_weekend_oversea_holiday));
        setLineAttributes(setMonthlyPREWO, R.color.teal, false, "#009688");

        setMonthlyPREWOL = new LineDataSet(lineEntriesPREWOL, getResources().getString(R.string.psi_rainfall_event_weekend_oversea_holiday_local_holiday));
        setLineAttributes(setMonthlyPREWOL, R.color.orange, false, "#FF9800");

        BarDataSet setFootFall = new BarDataSet(barEntriesFootFallMonth, "Actual Footfall");
        setFootFall.setColors(new int[]{R.color.indigo}, PredictionActivity.this);
        setFootFall.setDrawValues(false);
        BarData barDataFootFall = new BarData(setFootFall);
        barDataFootFall.setDrawValues(true);
        barDataFootFall.setValueTextSize(10f);

        combinedDataMonthly.setData(barDataFootFall);
        monthlyPredictionChart.setData(combinedDataMonthly);
        monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);

        monthlyPredictionChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                plotDailyChart((int) h.getX());
                checkBoxDailyPR.setChecked(false);
                checkBoxDailyPRE.setChecked(false);
                checkBoxDailyPREW.setChecked(false);
                checkBoxDailyPREWO.setChecked(false);
                checkBoxDailyPREWOL.setChecked(false);
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    @OnClick(R.id.imageMonthlyPredictionInfo)
    public void imageMonthlyPredictionInfo(View v) {
        Tooltip.Builder builder = new Tooltip.Builder(v)
                .setCancelable(true)
                .setDismissOnClick(false)
                .setCornerRadius(20f)
                .setGravity(Gravity.BOTTOM)
                .setTextColor(Color.parseColor("#FFFFFF"))
                .setBackgroundColor(getColor(R.color.colorPrimary))
                .setText("• Compare prediction using different factors\n• Both Axis - FootFall numbers");
        builder.show();
    }

    @OnCheckedChanged(R.id.checkBoxMonthlyPR)
    public void checkBoxMonthlyPR(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setMonthlyPR);
            if (checkBoxMonthlyPRE.isChecked()) {
                lineDataSets.add(setMonthlyPRE);
            }
            if (checkBoxMonthlyPREW.isChecked()) {
                lineDataSets.add(setMonthlyPREW);
            }
            if (checkBoxMonthlyPREWO.isChecked()) {
                lineDataSets.add(setMonthlyPREWO);
            }
            if (checkBoxMonthlyPREWOL.isChecked()) {
                lineDataSets.add(setMonthlyPREWOL);
            }
            combinedDataMonthly.setData(lineData);
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxMonthlyPREW.isChecked() && !checkBoxMonthlyPREWO.isChecked()
                    && !checkBoxMonthlyPREWOL.isChecked() && !checkBoxMonthlyPRE.isChecked()) {
                combinedDataMonthly.setData(new LineData());
            } else {
                if (checkBoxMonthlyPRE.isChecked()) {
                    lineDataSets.add(setMonthlyPRE);
                }
                if (checkBoxMonthlyPREW.isChecked()) {
                    lineDataSets.add(setMonthlyPREW);
                }
                if (checkBoxMonthlyPREWO.isChecked()) {
                    lineDataSets.add(setMonthlyPREWO);
                }
                if (checkBoxMonthlyPREWOL.isChecked()) {
                    lineDataSets.add(setMonthlyPREWOL);
                }
                combinedDataMonthly.setData(lineData);
            }
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxMonthlyPRE)
    public void checkBoxMonthlyPRE(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setMonthlyPRE);
            if (checkBoxMonthlyPR.isChecked()) {
                lineDataSets.add(setMonthlyPR);
            }
            if (checkBoxMonthlyPREW.isChecked()) {
                lineDataSets.add(setMonthlyPREW);
            }
            if (checkBoxMonthlyPREWO.isChecked()) {
                lineDataSets.add(setMonthlyPREWO);
            }
            if (checkBoxMonthlyPREWOL.isChecked()) {
                lineDataSets.add(setMonthlyPREWOL);
            }
            combinedDataMonthly.setData(lineData);
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxMonthlyPREW.isChecked() && !checkBoxMonthlyPREWO.isChecked() &&
                    !checkBoxMonthlyPREWOL.isChecked() && !checkBoxMonthlyPR.isChecked()) {
                combinedDataMonthly.setData(new LineData());
            } else {
                if (checkBoxMonthlyPR.isChecked()) {
                    lineDataSets.add(setMonthlyPR);
                }
                if (checkBoxMonthlyPREW.isChecked()) {
                    lineDataSets.add(setMonthlyPREW);
                }
                if (checkBoxMonthlyPREWO.isChecked()) {
                    lineDataSets.add(setMonthlyPREWO);
                }
                if (checkBoxMonthlyPREWOL.isChecked()) {
                    lineDataSets.add(setMonthlyPREWOL);
                }
                combinedDataMonthly.setData(lineData);
            }
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxMonthlyPREW)
    public void checkBoxMonthlyPREW(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setMonthlyPREW);
            if (checkBoxMonthlyPR.isChecked()) {
                lineDataSets.add(setMonthlyPR);
            }
            if (checkBoxMonthlyPRE.isChecked()) {
                lineDataSets.add(setMonthlyPRE);
            }
            if (checkBoxMonthlyPREWO.isChecked()) {
                lineDataSets.add(setMonthlyPREWO);
            }
            if (checkBoxMonthlyPREWOL.isChecked()) {
                lineDataSets.add(setMonthlyPREWOL);
            }
            combinedDataMonthly.setData(lineData);
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxMonthlyPR.isChecked() && !checkBoxMonthlyPREWO.isChecked() &&
                    !checkBoxMonthlyPREWOL.isChecked() && !checkBoxMonthlyPRE.isChecked()) {
                combinedDataMonthly.setData(new LineData());
            } else {
                if (checkBoxMonthlyPR.isChecked()) {
                    lineDataSets.add(setMonthlyPR);
                }
                if (checkBoxMonthlyPRE.isChecked()) {
                    lineDataSets.add(setMonthlyPRE);
                }
                if (checkBoxMonthlyPREWO.isChecked()) {
                    lineDataSets.add(setMonthlyPREWO);
                }
                if (checkBoxMonthlyPREWOL.isChecked()) {
                    lineDataSets.add(setMonthlyPREWOL);
                }
                combinedDataMonthly.setData(lineData);
            }
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxMonthlyPREWO)
    public void checkBoxMonthlyPREWO(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setMonthlyPREWO);
            if (checkBoxMonthlyPRE.isChecked()) {
                lineDataSets.add(setMonthlyPRE);
            }
            if (checkBoxMonthlyPREW.isChecked()) {
                lineDataSets.add(setMonthlyPREW);
            }
            if (checkBoxMonthlyPR.isChecked()) {
                lineDataSets.add(setMonthlyPR);
            }
            if (checkBoxMonthlyPREWOL.isChecked()) {
                lineDataSets.add(setMonthlyPREWOL);
            }
            combinedDataMonthly.setData(lineData);
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxMonthlyPREW.isChecked() && !checkBoxMonthlyPR.isChecked() &&
                    !checkBoxMonthlyPREWOL.isChecked() && !checkBoxMonthlyPRE.isChecked()) {
                combinedDataMonthly.setData(new LineData());
            } else {
                if (checkBoxMonthlyPREW.isChecked()) {
                    lineDataSets.add(setMonthlyPREW);
                }
                if (checkBoxMonthlyPRE.isChecked()) {
                    lineDataSets.add(setMonthlyPRE);
                }
                if (checkBoxMonthlyPR.isChecked()) {
                    lineDataSets.add(setMonthlyPR);
                }
                if (checkBoxMonthlyPREWOL.isChecked()) {
                    lineDataSets.add(setMonthlyPREWOL);
                }
                combinedDataMonthly.setData(lineData);
            }
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxMonthlyPREWOL)
    public void checkBoxMonthlyPREWOL(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setMonthlyPREWOL);
            if (checkBoxMonthlyPREW.isChecked()) {
                lineDataSets.add(setMonthlyPREW);
            }
            if (checkBoxMonthlyPRE.isChecked()) {
                lineDataSets.add(setMonthlyPRE);
            }
            if (checkBoxMonthlyPREWO.isChecked()) {
                lineDataSets.add(setMonthlyPREWO);
            }
            if (checkBoxMonthlyPR.isChecked()) {
                lineDataSets.add(setMonthlyPR);
            }
            combinedDataMonthly.setData(lineData);
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxMonthlyPREW.isChecked() && !checkBoxMonthlyPREWO.isChecked()
                    && !checkBoxMonthlyPR.isChecked() && !checkBoxMonthlyPRE.isChecked()) {
                combinedDataMonthly.setData(new LineData());
            } else {
                if (checkBoxMonthlyPREW.isChecked()) {
                    lineDataSets.add(setMonthlyPREW);
                }
                if (checkBoxMonthlyPRE.isChecked()) {
                    lineDataSets.add(setMonthlyPRE);
                }
                if (checkBoxMonthlyPREWO.isChecked()) {
                    lineDataSets.add(setMonthlyPREWO);
                }
                if (checkBoxMonthlyPR.isChecked()) {
                    lineDataSets.add(setMonthlyPR);
                }
                combinedDataMonthly.setData(lineData);
            }
            monthlyPredictionChart.setData(combinedDataMonthly);
            monthlyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    //plot daily actual vs prediction chart based on selected month
    private void plotDailyChart(int index) {
        dailyPredictionChart.setDrawBarShadow(false);
        dailyPredictionChart.getDescription().setEnabled(false);
        dailyPredictionChart.setPinchZoom(false);
        dailyPredictionChart.setDoubleTapToZoomEnabled(false);
        dailyPredictionChart.setScaleEnabled(false);
        dailyPredictionChart.setDrawGridBackground(false);

        YAxis leftAxis = dailyPredictionChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new LargeValueFormatter());
        YAxis rightAxis = dailyPredictionChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setValueFormatter(new LargeValueFormatter());

        Legend l = dailyPredictionChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        XAxis xAxis = dailyPredictionChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);

        List<BarEntry> barEntriesFootFallDay = new ArrayList<>();
        List<Entry> lineEntriesDailyPR = new ArrayList<>();
        List<Entry> lineEntriesDailyPRE = new ArrayList<>();
        List<Entry> lineEntriesDailyPREW = new ArrayList<>();
        List<Entry> lineEntriesDailyPREWO = new ArrayList<>();
        List<Entry> lineEntriesDailyPREWOL = new ArrayList<>();

        dateString = new String[actualMonthMap.get(index).size()];

        int actualIndex = 0;

        for (Map.Entry<String, Integer> entry : actualMonthMap.get(index).entrySet()) {
            dateString[actualIndex] = entry.getKey();
            barEntriesFootFallDay.add(new BarEntry(actualIndex, entry.getValue()));
            actualIndex++;
        }

        dailyPredictionTitle.setText(String.format("%s Actual vs Prediction", dateString[0].split("-")[1]));
        actualIndex = 0;

        for (Map.Entry<String, Integer> entry : predictPRMonthMap.get(index).entrySet()) {
            lineEntriesDailyPR.add(new Entry(actualIndex, entry.getValue()));
            actualIndex++;
        }

        actualIndex = 0;

        for (Map.Entry<String, Integer> entry : predictPREMonthMap.get(index).entrySet()) {
            lineEntriesDailyPRE.add(new Entry(actualIndex, entry.getValue()));
            actualIndex++;
        }

        actualIndex = 0;

        for (Map.Entry<String, Integer> entry : predictPREWMonthMap.get(index).entrySet()) {
            lineEntriesDailyPREW.add(new Entry(actualIndex, entry.getValue()));
            actualIndex++;
        }
        actualIndex = 0;

        for (Map.Entry<String, Integer> entry : predictPREWOMonthMap.get(index).entrySet()) {
            lineEntriesDailyPREWO.add(new Entry(actualIndex, entry.getValue()));
            actualIndex++;
        }

        actualIndex = 0;

        for (Map.Entry<String, Integer> entry : predictPREWOLMonthMap.get(index).entrySet()) {
            lineEntriesDailyPREWOL.add(new Entry(actualIndex, entry.getValue()));
            actualIndex++;
        }

        xAxis.setValueFormatter((value, axis) -> dateString[(int) value % dateString.length]);

        setDailyPR = new LineDataSet(lineEntriesDailyPR, getResources().getString(R.string.psi_rainfall));
        setLineAttributes(setDailyPR, R.color.purple, false, "#9C27B0");

        setDailyPRE = new LineDataSet(lineEntriesDailyPRE, getResources().getString(R.string.psi_rainfall_event));
        setLineAttributes(setDailyPRE, R.color.lightblue, false, "#03A9F4");

        setDailyPREW = new LineDataSet(lineEntriesDailyPREW, getResources().getString(R.string.psi_rainfall_event_weekend));
        setLineAttributes(setDailyPREW, R.color.pink, false, "#E91E63");

        setDailyPREWO = new LineDataSet(lineEntriesDailyPREWO, getResources().getString(R.string.psi_rainfall_event_weekend_oversea_holiday));
        setLineAttributes(setDailyPREWO, R.color.teal, false, "#009688");

        setDailyPREWOL = new LineDataSet(lineEntriesDailyPREWOL, getResources().getString(R.string.psi_rainfall_event_weekend_oversea_holiday_local_holiday));
        setLineAttributes(setDailyPREWOL, R.color.orange, false, "#FF9800");

        BarDataSet setFootFallDay = new BarDataSet(barEntriesFootFallDay, "Actual Footfall");
        setFootFallDay.setColors(new int[]{R.color.indigo}, PredictionActivity.this);
        setFootFallDay.setDrawValues(false);
        BarData barData = new BarData(setFootFallDay);
        barData.setDrawValues(true);
        barData.setValueTextSize(10f);

        combinedDataDaily.setData(barData);

        dailyPredictionChart.setData(combinedDataDaily);
        dailyPredictionChart.setVisibleXRangeMaximum(5);
        dailyPredictionChart.setVisibleXRangeMinimum(5);
        dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);

    }

    @OnCheckedChanged(R.id.checkBoxDailyPR)
    public void checkBoxDailyPR(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setDailyPR);
            if (checkBoxDailyPRE.isChecked()) {
                lineDataSets.add(setDailyPRE);
            }
            if (checkBoxDailyPREW.isChecked()) {
                lineDataSets.add(setDailyPREW);
            }
            if (checkBoxDailyPREWO.isChecked()) {
                lineDataSets.add(setDailyPREWO);
            }
            if (checkBoxDailyPREWOL.isChecked()) {
                lineDataSets.add(setDailyPREWOL);
            }
            combinedDataDaily.setData(lineData);
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxDailyPREW.isChecked() && !checkBoxDailyPREWO.isChecked()
                    && !checkBoxDailyPREWOL.isChecked() && !checkBoxDailyPRE.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxDailyPRE.isChecked()) {
                    lineDataSets.add(setDailyPRE);
                }
                if (checkBoxDailyPREW.isChecked()) {
                    lineDataSets.add(setDailyPREW);
                }
                if (checkBoxDailyPREWO.isChecked()) {
                    lineDataSets.add(setDailyPREWO);
                }
                if (checkBoxDailyPREWOL.isChecked()) {
                    lineDataSets.add(setDailyPREWOL);
                }
                combinedDataDaily.setData(lineData);
            }
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxDailyPRE)
    public void checkBoxDailyPRE(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setDailyPRE);
            if (checkBoxDailyPR.isChecked()) {
                lineDataSets.add(setDailyPR);
            }
            if (checkBoxDailyPREW.isChecked()) {
                lineDataSets.add(setDailyPREW);
            }
            if (checkBoxDailyPREWO.isChecked()) {
                lineDataSets.add(setDailyPREWO);
            }
            if (checkBoxDailyPREWOL.isChecked()) {
                lineDataSets.add(setDailyPREWOL);
            }
            combinedDataDaily.setData(lineData);
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxDailyPREW.isChecked() && !checkBoxDailyPREWO.isChecked()
                    && !checkBoxDailyPREWOL.isChecked() && !checkBoxDailyPR.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxDailyPR.isChecked()) {
                    lineDataSets.add(setDailyPR);
                }
                if (checkBoxDailyPREW.isChecked()) {
                    lineDataSets.add(setDailyPREW);
                }
                if (checkBoxDailyPREWO.isChecked()) {
                    lineDataSets.add(setDailyPREWO);
                }
                if (checkBoxDailyPREWOL.isChecked()) {
                    lineDataSets.add(setDailyPREWOL);
                }
                combinedDataDaily.setData(lineData);
            }
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxDailyPREW)
    public void checkBoxDailyPREW(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setDailyPREW);
            if (checkBoxDailyPRE.isChecked()) {
                lineDataSets.add(setDailyPRE);
            }
            if (checkBoxDailyPR.isChecked()) {
                lineDataSets.add(setDailyPR);
            }
            if (checkBoxDailyPREWO.isChecked()) {
                lineDataSets.add(setDailyPREWO);
            }
            if (checkBoxDailyPREWOL.isChecked()) {
                lineDataSets.add(setDailyPREWOL);
            }
            combinedDataDaily.setData(lineData);
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxDailyPR.isChecked() && !checkBoxDailyPREWO.isChecked()
                    && !checkBoxDailyPREWOL.isChecked() && !checkBoxDailyPRE.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxDailyPRE.isChecked()) {
                    lineDataSets.add(setDailyPRE);
                }
                if (checkBoxDailyPR.isChecked()) {
                    lineDataSets.add(setDailyPR);
                }
                if (checkBoxDailyPREWO.isChecked()) {
                    lineDataSets.add(setDailyPREWO);
                }
                if (checkBoxDailyPREWOL.isChecked()) {
                    lineDataSets.add(setDailyPREWOL);
                }
                combinedDataDaily.setData(lineData);
            }
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxDailyPREWO)
    public void checkBoxDailyPREWO(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setDailyPREWO);
            if (checkBoxDailyPRE.isChecked()) {
                lineDataSets.add(setDailyPRE);
            }
            if (checkBoxDailyPREW.isChecked()) {
                lineDataSets.add(setDailyPREW);
            }
            if (checkBoxDailyPR.isChecked()) {
                lineDataSets.add(setDailyPR);
            }
            if (checkBoxDailyPREWOL.isChecked()) {
                lineDataSets.add(setDailyPREWOL);
            }
            combinedDataDaily.setData(lineData);
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxDailyPREW.isChecked() && !checkBoxDailyPR.isChecked()
                    && !checkBoxDailyPREWOL.isChecked() && !checkBoxDailyPRE.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxDailyPRE.isChecked()) {
                    lineDataSets.add(setDailyPRE);
                }
                if (checkBoxDailyPREW.isChecked()) {
                    lineDataSets.add(setDailyPREW);
                }
                if (checkBoxDailyPR.isChecked()) {
                    lineDataSets.add(setDailyPR);
                }
                if (checkBoxDailyPREWOL.isChecked()) {
                    lineDataSets.add(setDailyPREWOL);
                }
                combinedDataDaily.setData(lineData);
            }
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxDailyPREWOL)
    public void checkBoxDailyPREWOL(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setDailyPREWOL);
            if (checkBoxDailyPRE.isChecked()) {
                lineDataSets.add(setDailyPRE);
            }
            if (checkBoxDailyPREW.isChecked()) {
                lineDataSets.add(setDailyPREW);
            }
            if (checkBoxDailyPREWO.isChecked()) {
                lineDataSets.add(setDailyPREWO);
            }
            if (checkBoxDailyPR.isChecked()) {
                lineDataSets.add(setDailyPR);
            }
            combinedDataDaily.setData(lineData);
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxDailyPREW.isChecked() && !checkBoxDailyPREWO.isChecked()
                    && !checkBoxDailyPR.isChecked() && !checkBoxDailyPRE.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxDailyPRE.isChecked()) {
                    lineDataSets.add(setDailyPRE);
                }
                if (checkBoxDailyPREW.isChecked()) {
                    lineDataSets.add(setDailyPREW);
                }
                if (checkBoxDailyPREWO.isChecked()) {
                    lineDataSets.add(setDailyPREWO);
                }
                if (checkBoxDailyPR.isChecked()) {
                    lineDataSets.add(setDailyPR);
                }
                combinedDataDaily.setData(lineData);
            }
            dailyPredictionChart.setData(combinedDataDaily);
            dailyPredictionChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    //set line attributes
    private void setLineAttributes(LineDataSet lineDataSet, int color, boolean rightYDependency, String colorHex) {
        lineDataSet.setColors(new int[]{color}, PredictionActivity.this);
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(2.5f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setCircleColor(Color.parseColor(colorHex));
        if (rightYDependency) {
            lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
        }
    }
}
