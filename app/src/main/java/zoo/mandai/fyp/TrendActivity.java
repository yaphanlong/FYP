package zoo.mandai.fyp;

import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.PieChart;
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
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import mobi.gspd.segmentedbarview.Segment;
import mobi.gspd.segmentedbarview.SegmentedBarView;
import zoo.mandai.fyp.api.ApiService;
import zoo.mandai.fyp.POJO.event.Event;
import zoo.mandai.fyp.POJO.event.Events;
import zoo.mandai.fyp.POJO.firebase.Data;
import zoo.mandai.fyp.POJO.firebase.Day;
import zoo.mandai.fyp.POJO.firebase.Holiday;

public class TrendActivity extends AppCompatActivity {

    //Monthly Card
    @BindView(R.id.textMonthlyTitle) TextView textMonthlyTitle;
    @BindView(R.id.checkBoxMonthlyPsi) CheckBox checkBoxMonthlyPsi;
    @BindView(R.id.checkBoxMonthlyTourist) CheckBox checkBoxMonthlyTourist;
    @BindView(R.id.combinedChartMonthly) CombinedChart combinedChartMonthly;

    //Selected Region Card
    @BindView(R.id.textRegionTitle) TextView textRegionTitle;
    @BindView(R.id.pieChartRegion) PieChart pieChartRegion;

    //Daily Card
    @BindView(R.id.textDailyTitle) TextView textDailyTitle;
    @BindView(R.id.checkBoxPsi) CheckBox checkBoxPsi;
    @BindView(R.id.checkBoxRainfall) CheckBox checkBoxRainfall;
    @BindView(R.id.checkBoxEvent) CheckBox checkBoxEvent;
    @BindView(R.id.combinedChartDaily) CombinedChart combinedChartDaily;
    @BindView(R.id.combinedChartHoliday) CombinedChart combinedChartHoliday;

    //Daily Summary Card
    @BindView(R.id.textDailySummaryTitle) TextView textDailySummaryTitle;
    @BindView(R.id.barDailyPsi) SegmentedBarView barDailyPsi;
    @BindView(R.id.recyclerViewDailyEvent) RecyclerView recyclerViewDailyEvent;
    @BindView(R.id.dailyEventCount) TextView dailyEventCount;
    @BindView(R.id.dailyFootfall) TextView dailyFootfall;
    @BindView(R.id.dailyRainfall) TextView dailyRainfall;

    //Monthly Summary Card
    @BindView(R.id.textMonthlySummaryTitle) TextView textMonthlySummaryTitle;
    @BindView(R.id.recyclerViewMonthlyHoliday) RecyclerView recyclerViewMonthlyHoliday;
    @BindView(R.id.recyclerViewMonthlyEvent) RecyclerView recyclerViewMonthlyEvent;
    @BindView(R.id.mandaiFootfall) TextView mandaiFootfall;
    @BindView(R.id.touristArrival) TextView touristArrival;
    @BindView(R.id.rainyDay) TextView rainyDay;
    @BindView(R.id.nonRainy) TextView nonRainy;
    @BindView(R.id.psiGoodModerate) TextView psiGoodModerate;
    @BindView(R.id.psiUnhealthyVeryunhealthy) TextView psiUnhealthyVeryunhealthy;
    @BindView(R.id.psiHazardous) TextView psiHazardous;
    @BindView(R.id.monthlyHolidayCount) TextView monthlyHolidayCount;
    @BindView(R.id.monthlyEventCount) TextView monthlyEventCount;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_trend) CoordinatorLayout coordinatorLayout;
    @BindArray(R.array.regions) String[] regionName;

    private Data firebaseData;
    private String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private DecimalFormat df = new DecimalFormat("#.0");

    private List<BarEntry> barEntriesFootFallMonth = new ArrayList<>();
    private List<Entry> lineEntriesTourist = new ArrayList<>();
    private List<Entry> lineEntriesGoodModerate = new ArrayList<>();
    private List<Entry> lineEntriesUnhealthyVeryUnhealthy = new ArrayList<>();
    private List<Entry> lineEntriesHazardous = new ArrayList<>();
    private List<PieEntry> pieEntriesRegion = new ArrayList<>();
    private LineDataSet setTourist, setGoodModerate, setUnhealthyVeryUnhealthy, setHazardous, setPSI, setEvent, setRainFall;
    private BarDataSet setFootFall;
    private CombinedData combinedDataMonthly = new CombinedData();
    private CombinedData combinedDataDaily = new CombinedData();
    private CombinedData combinedDataHoliday = new CombinedData();
    private List<Segment> psiSegments = new ArrayList<>();
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mCompositeDisposable = new CompositeDisposable();

        initMonthlyChart();
        initRegionChart();
        initDailyChart();
        initDailySummary();
        initMonthlySummary();
    }

    @OnClick(R.id.imageMonthlyInfo)
    public void imageMonthlyInfo(View v) {
        tooltipBuilder(v, "• Toggle Tourist and PSI to identify patterns with FootFall\n• Left Axis - FootFall/Tourist numbers\n• Right Axis - No. of days with certain PSI condition");
    }

    @OnClick(R.id.imageRegionInfo)
    public void imageRegionInfo(View v) {
        tooltipBuilder(v, "• FootFall distribution across all Mandai Regions\n• Click on each slice of chart to get more info");
    }

    @OnClick(R.id.imageDailyInfo)
    public void imageDailyInfo(View v) {
        tooltipBuilder(v, "• 1st Graph represents daily FootFall sorted in descending order" +
                "\n• Toggle PSI, RainFall and Events to identify patterns with FootFall for the month" +
                "\n• Click on each bar chart to get more info from the card below" +
                "\n• Left Axis - FootFall numbers\n• Right Axis - PSI, RainFall(mm), Event Count" +
                "\n• 2nd Graph represents FootFall during Local/Oversea Holidays sorted in descending order" +
                "\n• Click on each bar chart to get more info about the holiday");
    }

    @OnClick(R.id.imageDailySummaryInfo)
    public void imageDailySummaryInfo(View v) {
        tooltipBuilder(v, "• Summary for the day");
    }

    @OnClick(R.id.imageMonthlySummaryInfo)
    public void imageMonthlySummaryInfo(View v) {
        tooltipBuilder(v, "• Summary for the month");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    //tool tip builder
    private void tooltipBuilder(View v, String text) {
        Tooltip.Builder builder = new Tooltip.Builder(v).setCancelable(true)
                .setDismissOnClick(false)
                .setCornerRadius(20f)
                .setGravity(Gravity.BOTTOM)
                .setTextColor(Color.parseColor("#FFFFFF"))
                .setBackgroundColor(getColor(R.color.colorPrimary))
                .setText(text);
        builder.show();
    }

    //set line attributes
    private void setLineAttributes(LineDataSet lineDataSet, int color, boolean rightYDependency, String colorHex) {
        lineDataSet.setColors(new int[]{color}, TrendActivity.this);
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

    //initialise monthly chart
    private void initMonthlyChart() {
        textMonthlyTitle.setText("2016 FootFall");
        combinedChartMonthly.setDrawBarShadow(false);
        combinedChartMonthly.getDescription().setEnabled(false);
        combinedChartMonthly.setPinchZoom(false);
        combinedChartMonthly.setDoubleTapToZoomEnabled(false);
        combinedChartMonthly.setScaleEnabled(false);
        combinedChartMonthly.setDrawGridBackground(false);

        XAxis xAxis = combinedChartMonthly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);
        xAxis.setValueFormatter((value, axis) -> mMonths[(int) value % mMonths.length]);

        YAxis leftAxis = combinedChartMonthly.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new LargeValueFormatter());
        YAxis rightAxis = combinedChartMonthly.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(35f);
        rightAxis.setValueFormatter(new LargeValueFormatter());

        Legend l = combinedChartMonthly.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        lineEntriesTourist.clear();
        lineEntriesGoodModerate.clear();
        lineEntriesUnhealthyVeryUnhealthy.clear();
        lineEntriesHazardous.clear();
        barEntriesFootFallMonth.clear();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("data");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebaseData = dataSnapshot.getValue(Data.class);

                for (int i = 0; i < 12; i++) {
                    lineEntriesTourist.add(i, new Entry(i, firebaseData.get2016().get(i).getTourist()));
                    lineEntriesGoodModerate.add(i, new Entry(i, firebaseData.get2016().get(i).getPsi().getGoodmoderate()));
                    lineEntriesUnhealthyVeryUnhealthy.add(i, new Entry(i, firebaseData.get2016().get(i).getPsi().getUnhealthyveryunhealthy()));
                    lineEntriesHazardous.add(i, new Entry(i, firebaseData.get2016().get(i).getPsi().getHazardous()));
                    barEntriesFootFallMonth.add(i, new BarEntry(i, firebaseData.get2016().get(i).getTotal()));
                }

                setTourist = new LineDataSet(lineEntriesTourist, getResources().getString(R.string.tourist_arrivals));
                setGoodModerate = new LineDataSet(lineEntriesGoodModerate, getResources().getString(R.string.good_moderate));
                setUnhealthyVeryUnhealthy = new LineDataSet(lineEntriesUnhealthyVeryUnhealthy, getResources().getString(R.string.unhealthy_vunhealthy));
                setHazardous = new LineDataSet(lineEntriesHazardous, getResources().getString(R.string.hazardous));

                setLineAttributes(setTourist, R.color.purple, false, "#9C27B0");
                setLineAttributes(setGoodModerate, R.color.teal, true, "#009688");
                setLineAttributes(setUnhealthyVeryUnhealthy, R.color.yellow, true, "#FFEB3B");
                setLineAttributes(setHazardous, R.color.pink, true, "#E91E63");

                setFootFall = new BarDataSet(barEntriesFootFallMonth, getResources().getString(R.string.footfall));
                setFootFall.setColors(new int[]{R.color.indigo}, TrendActivity.this);
                setFootFall.setDrawValues(false);
                BarData barDataFootFall = new BarData(setFootFall);

                combinedDataMonthly.setData(barDataFootFall);
                combinedChartMonthly.setData(combinedDataMonthly);
                combinedChartMonthly.animateY(800, Easing.EasingOption.EaseInOutQuad);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        combinedChartMonthly.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) h.getX();

                plotRegionChart(index);
                plotDailyChart(index);
                plotMonthlySummary(index);

                checkBoxEvent.setChecked(false);
                checkBoxPsi.setChecked(false);
                checkBoxRainfall.setChecked(false);
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    @OnCheckedChanged(R.id.checkBoxMonthlyTourist)
    public void checkBoxMonthlyTourist(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setTourist);
            if (checkBoxMonthlyPsi.isChecked()) {
                lineDataSets.add(setGoodModerate);
                lineDataSets.add(setUnhealthyVeryUnhealthy);
                lineDataSets.add(setHazardous);
            }
            combinedDataMonthly.setData(lineData);
            combinedChartMonthly.setData(combinedDataMonthly);
            combinedChartMonthly.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (checkBoxMonthlyPsi.isChecked()) {
                lineDataSets.add(setGoodModerate);
                lineDataSets.add(setUnhealthyVeryUnhealthy);
                lineDataSets.add(setHazardous);
                combinedDataMonthly.setData(lineData);
            } else {
                combinedDataMonthly.setData(new LineData());
            }
            combinedChartMonthly.setData(combinedDataMonthly);
            combinedChartMonthly.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxMonthlyPsi)
    public void checkBoxMonthlyPsi(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setGoodModerate);
            lineDataSets.add(setUnhealthyVeryUnhealthy);
            lineDataSets.add(setHazardous);
            if (checkBoxMonthlyTourist.isChecked()) {
                lineDataSets.add(setTourist);
            }
            combinedDataMonthly.setData(lineData);
            combinedChartMonthly.setData(combinedDataMonthly);
            combinedChartMonthly.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (checkBoxMonthlyTourist.isChecked()) {
                lineDataSets.add(setTourist);
                combinedDataMonthly.setData(lineData);
            } else {
                combinedDataMonthly.setData(new LineData());
            }
            combinedChartMonthly.setData(combinedDataMonthly);
            combinedChartMonthly.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    //initialise region chart
    private void initRegionChart() {
        textRegionTitle.setText(R.string.select_month);
        pieChartRegion.setDrawEntryLabels(false);
        pieChartRegion.getDescription().setEnabled(false);
        pieChartRegion.setDragDecelerationFrictionCoef(0.95f);

        pieChartRegion.setDrawHoleEnabled(true);
        pieChartRegion.setHoleColor(Color.WHITE);

        pieChartRegion.setTransparentCircleColor(Color.WHITE);
        pieChartRegion.setTransparentCircleAlpha(110);

        pieChartRegion.setHoleRadius(58f);
        pieChartRegion.setTransparentCircleRadius(61f);

        pieChartRegion.setRotationAngle(0);
        pieChartRegion.setRotationEnabled(false);
        pieChartRegion.setHighlightPerTapEnabled(true);

        Legend l = pieChartRegion.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }

    //plot region chart based on index
    private void plotRegionChart(int index) {
        textRegionTitle.setText(String.format("%s Region Summary", firebaseData.get2016().get(index).getMonth()));
        pieEntriesRegion.clear();
        for (int i = 0; i < regionName.length; i++) {
            pieEntriesRegion.add(new PieEntry(firebaseData.get2016().get(index).getRegions().get(i).getVisitors(), regionName[i]));
        }

        PieDataSet pieDataSet = new PieDataSet(pieEntriesRegion, null);
        pieDataSet.setColors(new int[]{R.color.lightblue, R.color.grey, R.color.pink, R.color.lightgreen, R.color.purple,
                R.color.yellow, R.color.orange, R.color.teal, R.color.indigo}, TrendActivity.this);
        final PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(false);
        pieChartRegion.setData(pieData);
        pieChartRegion.animateY(800, Easing.EasingOption.EaseInOutQuad);
        SpannableString s = new SpannableString("Total Visitors\n" + Integer.toString((int) pieData.getYValueSum()));
        pieChartRegion.setCenterText(s);

        pieChartRegion.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int pIndex = (int) h.getX();
                String pLabel = pieEntriesRegion.get(pIndex).getLabel();
                String pValue = (int) pieEntriesRegion.get(pIndex).getValue() + " (" + df.format(pieEntriesRegion.get(pIndex).getValue() / pieData.getYValueSum() * 100) + "%)";
                Snackbar snackbar = Snackbar.make(coordinatorLayout, pLabel + ": " + pValue, Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    //initialise daily chart
    private void initDailyChart() {
        textDailyTitle.setText(R.string.select_month);
        combinedChartDaily.setDrawBarShadow(false);
        combinedChartDaily.getDescription().setEnabled(false);
        combinedChartDaily.setPinchZoom(false);
        combinedChartDaily.setDoubleTapToZoomEnabled(false);
        combinedChartDaily.setScaleEnabled(false);
        combinedChartDaily.setDrawGridBackground(false);

        YAxis leftAxis = combinedChartDaily.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);
        YAxis rightAxis = combinedChartDaily.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setAxisMaximum(150f);

        Legend l = combinedChartDaily.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

        combinedChartHoliday.setDrawBarShadow(false);
        combinedChartHoliday.getDescription().setEnabled(false);
        combinedChartHoliday.setPinchZoom(false);
        combinedChartHoliday.setDoubleTapToZoomEnabled(false);
        combinedChartHoliday.setScaleEnabled(false);
        combinedChartHoliday.setDrawGridBackground(false);

        YAxis leftHolidayAxis = combinedChartHoliday.getAxisLeft();
        leftHolidayAxis.setDrawGridLines(false);
        leftHolidayAxis.setAxisMinimum(0f);
        YAxis rightHolidayAxis = combinedChartHoliday.getAxisRight();
        rightHolidayAxis.setDrawGridLines(false);
        rightHolidayAxis.setAxisMinimum(0f);
    }

    //plot daily chart based on selected month
    private void plotDailyChart(int index) {
        textDailyTitle.setText(String.format("%s FootFall vs Holiday", firebaseData.get2016().get(index).getMonth()));

        List<Day> dayList = firebaseData.get2016().get(index).getDays();
        List<BarEntry> barEntriesFootFallDay = new ArrayList<>();
        List<Entry> lineEntriesPSI = new ArrayList<>();
        List<Entry> lineEntriesEvent = new ArrayList<>();
        List<Entry> lineEntriesRainFall = new ArrayList<>();

        Collections.sort(dayList, (o1, o2) -> (o2.getTotal().compareTo(o1.getTotal())));

        String[] xDate = new String[dayList.size()];
        for (int i = 0; i < dayList.size(); i++) {
            xDate[i] = dayList.get(i).getDate();
            barEntriesFootFallDay.add(new BarEntry(i, dayList.get(i).getTotal()));
            lineEntriesPSI.add(new Entry(i, dayList.get(i).getPsi()));
            lineEntriesRainFall.add(new Entry(i, dayList.get(i).getRainfall()));
            lineEntriesEvent.add(new Entry(i, dayList.get(i).getEvent()));
        }

        XAxis xAxis = combinedChartDaily.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setSpaceMin(0.5f);
        xAxis.setSpaceMax(0.5f);
        xAxis.setValueFormatter((value, axis) -> xDate[(int) value % xDate.length]);

        BarDataSet set = new BarDataSet(barEntriesFootFallDay, "Descending Order FootFall (Days)");
        set.setColors(new int[]{R.color.indigo}, TrendActivity.this);
        set.setDrawValues(false);
        BarData barData = new BarData(set);
        barData.setDrawValues(true);
        barData.setValueTextSize(10f);

        setPSI = new LineDataSet(lineEntriesPSI, "PSI");
        setLineAttributes(setPSI, R.color.pink, true, "#E91E63");

        setEvent = new LineDataSet(lineEntriesEvent, "Event");
        setLineAttributes(setEvent, R.color.purple, true, "#9C27B0");

        setRainFall = new LineDataSet(lineEntriesRainFall, "RainFall (mm)");
        setLineAttributes(setRainFall, R.color.teal, true, "#009688");

        combinedDataDaily.setData(barData);
        combinedChartDaily.setData(combinedDataDaily);
        combinedChartDaily.setVisibleXRangeMaximum(5);
        combinedChartDaily.setVisibleXRangeMinimum(5);
        combinedChartDaily.animateY(800, Easing.EasingOption.EaseInOutQuad);

        combinedChartDaily.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) h.getX();
                plotDailySummary(index, dayList);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        Map<String, Integer> footfallMap = new HashMap<>();
        for (Day day : firebaseData.get2016().get(index).getDays()) {
            footfallMap.put(day.getDate(), day.getTotal());
        }

        Map<Holiday, Integer> holidayMap = new HashMap<>();
        for (Holiday holiday : firebaseData.get2016().get(index).getHolidays()) {
            String holidayDate = holiday.getDate();
            if (!holidayDate.contains("-")) {
                for (Map.Entry<String, Integer> entry : footfallMap.entrySet()) {
                    if (entry.getKey().equals(holidayDate)) {
                        holidayMap.put(holiday, entry.getValue());
                    }
                }
            }
        }

        List<Map.Entry<Holiday, Integer>> sortHolidayList = new ArrayList<>(holidayMap.entrySet());
        Collections.sort(sortHolidayList, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        String[] holidayLabel = new String[holidayMap.size()];
        List<BarEntry> barEntryList = new ArrayList<>();

        for (int i = 0; i < sortHolidayList.size(); i++) {
            barEntryList.add(new BarEntry(i, sortHolidayList.get(i).getValue()));
            if (sortHolidayList.get(i).getKey().getCountry().contains("SG")) {
                holidayLabel[i] = "L";
            } else {
                holidayLabel[i] = "O";
            }
        }

        XAxis xHolidayAxis = combinedChartHoliday.getXAxis();
        xHolidayAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xHolidayAxis.setDrawGridLines(false);
        xHolidayAxis.setGranularity(1f);
        xHolidayAxis.setSpaceMin(0.5f);
        xHolidayAxis.setSpaceMax(0.5f);
        xHolidayAxis.setValueFormatter((value, axis) -> holidayLabel[(int) value % holidayLabel.length]);

        BarDataSet setHoliday = new BarDataSet(barEntryList, "Descending Order FootFall Holidays (Local/Oversea)");
        setHoliday.setColors(new int[]{R.color.indigo}, TrendActivity.this);
        setHoliday.setDrawValues(false);
        BarData barDataHoliday = new BarData(set);
        barDataHoliday.setDrawValues(true);
        barDataHoliday.setValueTextSize(10f);

        combinedDataHoliday.setData(barDataHoliday);
        combinedChartHoliday.setData(combinedDataHoliday);
        combinedChartHoliday.setVisibleXRangeMaximum(5);
        combinedChartHoliday.setVisibleXRangeMinimum(5);
        combinedChartHoliday.animateY(800, Easing.EasingOption.EaseInOutQuad);

        combinedChartHoliday.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) h.getX();
                Snackbar snackbar = Snackbar.make(coordinatorLayout, sortHolidayList.get(index).getKey().getDate() + ": "
                        + sortHolidayList.get(index).getKey().getCountry() + "\n" + sortHolidayList.get(index).getKey().getTitle(), Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            @Override
            public void onNothingSelected() {
            }
        });
    }

    @OnCheckedChanged(R.id.checkBoxPsi)
    public void checkBoxPsi(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setPSI);
            if (checkBoxEvent.isChecked()) {
                lineDataSets.add(setEvent);
            }
            if (checkBoxRainfall.isChecked()) {
                lineDataSets.add(setRainFall);
            }
            combinedDataDaily.setData(lineData);
            combinedChartDaily.setData(combinedDataDaily);
            combinedChartDaily.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxEvent.isChecked() && !checkBoxRainfall.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxEvent.isChecked()) {
                    lineDataSets.add(setEvent);
                }
                if (checkBoxRainfall.isChecked()) {
                    lineDataSets.add(setRainFall);
                }
                combinedDataDaily.setData(lineData);
            }
            combinedChartDaily.setData(combinedDataDaily);
            combinedChartDaily.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxEvent)
    public void checkBoxEvent(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setEvent);
            if (checkBoxPsi.isChecked()) {
                lineDataSets.add(setPSI);
            }
            if (checkBoxRainfall.isChecked()) {
                lineDataSets.add(setRainFall);
            }
            combinedDataDaily.setData(lineData);
            combinedChartDaily.setData(combinedDataDaily);
            combinedChartDaily.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxRainfall.isChecked() && !checkBoxPsi.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxRainfall.isChecked()) {
                    lineDataSets.add(setRainFall);
                }
                if (checkBoxPsi.isChecked()) {
                    lineDataSets.add(setPSI);
                }
                combinedDataDaily.setData(lineData);
            }
            combinedChartDaily.setData(combinedDataDaily);
            combinedChartDaily.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    @OnCheckedChanged(R.id.checkBoxRainfall)
    public void checkBoxRainfall(boolean isChecked) {
        ArrayList<ILineDataSet> lineDataSets = new ArrayList<>();
        LineData lineData = new LineData(lineDataSets);
        if (isChecked) {
            lineDataSets.add(setRainFall);
            if (checkBoxPsi.isChecked()) {
                lineDataSets.add(setPSI);
            }
            if (checkBoxEvent.isChecked()) {
                lineDataSets.add(setEvent);
            }
            combinedDataDaily.setData(lineData);
            combinedChartDaily.setData(combinedDataDaily);
            combinedChartDaily.animateY(800, Easing.EasingOption.EaseInOutQuad);
        } else {
            if (!checkBoxEvent.isChecked() && !checkBoxPsi.isChecked()) {
                combinedDataDaily.setData(new LineData());
            } else {
                if (checkBoxEvent.isChecked()) {
                    lineDataSets.add(setEvent);
                }
                if (checkBoxPsi.isChecked()) {
                    lineDataSets.add(setPSI);
                }
                combinedDataDaily.setData(lineData);
            }
            combinedChartDaily.setData(combinedDataDaily);
            combinedChartDaily.animateY(800, Easing.EasingOption.EaseInOutQuad);
        }
    }

    //initialise daily summary
    private void initDailySummary() {
        textDailySummaryTitle.setText(R.string.select_day);
        Segment segmentGood = new Segment(0, 50, "Good", Color.parseColor("#4CAF50"));
        psiSegments.add(segmentGood);
        Segment segmentModerate = new Segment(50, 100, "Moderate", Color.parseColor("#3F51B5"));
        psiSegments.add(segmentModerate);
        Segment segmentUnhealthy = new Segment(100, 200, "Unhealthy", Color.parseColor("#FFC107"));
        psiSegments.add(segmentUnhealthy);
        Segment segmentVeryUnhealthy = new Segment(200, 300, "V. Unhealthy", Color.parseColor("#FF5722"));
        psiSegments.add(segmentVeryUnhealthy);
        Segment segmentHazardous = new Segment(300, 500, "Hazardous", Color.parseColor("#E91E63"));
        psiSegments.add(segmentHazardous);
        barDailyPsi.setShowDescriptionText(true);
        barDailyPsi.setDescriptionTextSize(30);
        barDailyPsi.setSegmentTextSize(35);
        barDailyPsi.setValueSignSize(120, 90);
        barDailyPsi.setValueSignColor(Color.BLACK);
        barDailyPsi.setSegments(psiSegments);

        recyclerViewDailyEvent.setLayoutManager(new LinearLayoutManager(TrendActivity.this));
        recyclerViewDailyEvent.setItemAnimator(new DefaultItemAnimator());
        recyclerViewDailyEvent.setHasFixedSize(true);
        recyclerViewDailyEvent.addItemDecoration(new DividerItemDecoration(TrendActivity.this, LinearLayoutManager.VERTICAL));
    }

    //plot daily summary based on selected day
    private void plotDailySummary(int index, List<Day> dayList) {
        textDailySummaryTitle.setText(String.format("%s Summary", dayList.get(index).getDate()));
        dailyFootfall.setText(String.valueOf(dayList.get(index).getTotal()));
        dailyRainfall.setText(String.format("%s mm", String.valueOf(dayList.get(index).getRainfall())));
        barDailyPsi.setValue((float) dayList.get(index).getPsi());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat sdf2 = new SimpleDateFormat("d MMM yyyy", Locale.US);

        Calendar todayDate = Calendar.getInstance();
        Calendar tmlDate = Calendar.getInstance();
        try {
            todayDate.setTime(sdf2.parse(dayList.get(index).getDate() + " 2016"));
            tmlDate.setTime(sdf2.parse(dayList.get(index).getDate() + " 2016"));
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        tmlDate.add(Calendar.DATE, 1);
        mCompositeDisposable.add(new ApiService().serviceEvent().getCurrentEvent(0, sdf.format(todayDate.getTime()), sdf.format(tmlDate.getTime()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::eventDailyResponse, this::eventDailyError));
    }

    //response from daily event api call
    private void eventDailyResponse(Events events) {
        List<Event> eventList = events.getEvents();
        dailyEventCount.setText(String.format("Event Count - %s", String.valueOf(events.getAttributes().getCount())));
        recyclerViewDailyEvent.setAdapter(new EventAdapter(eventList));
        ViewGroup.LayoutParams params = recyclerViewDailyEvent.getLayoutParams();
        if (eventList.size() > 4) {
            params.height = 820;
            recyclerViewDailyEvent.setLayoutParams(params);
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    //error from daily event api call
    private void eventDailyError(Throwable error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Event Daily Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    //initialise monthly summary
    private void initMonthlySummary() {
        textMonthlySummaryTitle.setText(R.string.select_month);
        recyclerViewMonthlyHoliday.setLayoutManager(new LinearLayoutManager(TrendActivity.this));
        recyclerViewMonthlyHoliday.setItemAnimator(new DefaultItemAnimator());
        recyclerViewMonthlyHoliday.setHasFixedSize(true);
        recyclerViewMonthlyHoliday.addItemDecoration(new DividerItemDecoration(TrendActivity.this, LinearLayoutManager.VERTICAL));

        recyclerViewMonthlyEvent.setLayoutManager(new LinearLayoutManager(TrendActivity.this));
        recyclerViewMonthlyEvent.setItemAnimator(new DefaultItemAnimator());
        recyclerViewMonthlyEvent.setHasFixedSize(true);
        recyclerViewMonthlyEvent.addItemDecoration(new DividerItemDecoration(TrendActivity.this, LinearLayoutManager.VERTICAL));
    }

    //plot monthly summary based on index
    private void plotMonthlySummary(int index) {
        textMonthlySummaryTitle.setText(String.format("%s Summary", firebaseData.get2016().get(index).getMonth()));
        mandaiFootfall.setText(String.valueOf(firebaseData.get2016().get(index).getTotal()));
        touristArrival.setText(String.valueOf(firebaseData.get2016().get(index).getTourist()));
        rainyDay.setText(String.valueOf(firebaseData.get2016().get(index).getRainyday()));
        nonRainy.setText(String.valueOf(firebaseData.get2016().get(index).getNonrainyday()));
        psiGoodModerate.setText(String.valueOf(firebaseData.get2016().get(index).getPsi().getGoodmoderate()));
        psiUnhealthyVeryunhealthy.setText(String.valueOf(firebaseData.get2016().get(index).getPsi().getUnhealthyveryunhealthy()));
        psiHazardous.setText(String.valueOf(firebaseData.get2016().get(index).getPsi().getHazardous()));

        List<Holiday> holidayList = firebaseData.get2016().get(index).getHolidays();
        recyclerViewMonthlyHoliday.setAdapter(new HolidayAdapter(holidayList));

        monthlyHolidayCount.setText(String.format("Holiday Count - %s", String.valueOf(holidayList.size())));
        ViewGroup.LayoutParams params = recyclerViewMonthlyHoliday.getLayoutParams();
        if (holidayList.size() > 4) {
            params.height = 820;
            recyclerViewMonthlyHoliday.setLayoutParams(params);
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }

        NumberFormat formatter = new DecimalFormat("00");
        String cMonth = formatter.format(index + 1);
        String nMonth = formatter.format(index + 2);
        String cMonthInput, nMonthInput;

        if (index == 11) {
            cMonthInput = "01-12-2016";
            nMonthInput = "01-01-2017";
        } else {
            cMonthInput = "01-" + cMonth + "-2016";
            nMonthInput = "01-" + nMonth + "-2016";
        }

        mCompositeDisposable.add(new ApiService().serviceEvent().getCurrentEvent(0, cMonthInput, nMonthInput)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::eventMonthlyResponse, this::eventMonthlyError));
    }

    //response from monthly event api call
    private void eventMonthlyResponse(Events events) {
        List<Event> eventList = events.getEvents();
        monthlyEventCount.setText(String.format("Event Count - %s", String.valueOf(events.getAttributes().getCount())));
        recyclerViewMonthlyEvent.setAdapter(new EventAdapter(eventList));
        ViewGroup.LayoutParams params = recyclerViewMonthlyEvent.getLayoutParams();
        if (eventList.size() > 4) {
            params.height = 820;
            recyclerViewMonthlyEvent.setLayoutParams(params);
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    //error from monthly event api call
    private void eventMonthlyError(Throwable error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Event Monthly Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}
