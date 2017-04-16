package zoo.mandai.fyp;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
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
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tooltip.Tooltip;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import mobi.gspd.segmentedbarview.Segment;
import mobi.gspd.segmentedbarview.SegmentedBarView;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import zoo.mandai.fyp.api.ApiService;
import zoo.mandai.fyp.model.event.Event;
import zoo.mandai.fyp.model.event.Events;
import zoo.mandai.fyp.model.firebase.Data;
import zoo.mandai.fyp.model.firebase.Holiday;
import zoo.mandai.fyp.model.footfall.FootFall;
import zoo.mandai.fyp.model.footfall.SapInformation;
import zoo.mandai.fyp.model.psi.PSI;
import zoo.mandai.fyp.model.psi.Pm25SubIndex;
import zoo.mandai.fyp.model.weather.Currently;
import zoo.mandai.fyp.model.weather.Hourly;
import zoo.mandai.fyp.model.weather.Weather;

public class MainActivity extends AppCompatActivity {

    //Weather Card
    @BindView(R.id.currentTemperature) TextView currentTemperature;
    @BindView(R.id.hourlyWeather) TextView hourlyWeather;
    @BindView(R.id.dailyWeather) TextView dailyWeather;
    @BindView(R.id.hourlyWeatherImg) ImageView hourlyWeatherImg;
    @BindView(R.id.weatherChart) CombinedChart weatherChart;
    @BindView(R.id.psiView) SegmentedBarView psiView;

    //Footfall Card
    @BindView(R.id.currentFootfall) TextView currentFootfall;
    @BindView(R.id.footfallChart) PieChart footfallChart;

    //Event Card
    @BindView(R.id.eventCount) TextView eventCount;
    @BindView(R.id.eventRecyclerView) RecyclerView eventRecyclerView;

    //Holiday Card
    @BindView(R.id.holidayCount) TextView holidayCount;
    @BindView(R.id.holidayRecyclerView) RecyclerView holidayRecyclerView;

    //Prediction Card
    @BindView(R.id.footfallPrediction) TextView footfallPrediction;
    @BindView(R.id.rainfallInput) EditText rainfallInput;
    @BindView(R.id.psiInput) EditText psiInput;
    @BindView(R.id.eventInput) EditText eventInput;
    @BindView(R.id.weekendInput) SwitchCompat weekendInput;
    @BindView(R.id.localHolidayInput) SwitchCompat localHolidayInput;
    @BindView(R.id.overseaHolidayInput) SwitchCompat overseaHolidayInput;

    @BindArray(R.array.regions) String[] regionName;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private List<PieEntry> pieEntriesFootFall = new ArrayList<>();
    private List<BarEntry> barEntriesRain = new ArrayList<>();
    private List<Entry> lineEntriesTemp = new ArrayList<>();
    private List<Segment> psiSegments = new ArrayList<>();
    private PieData pieDataFootFall;
    private DecimalFormat df = new DecimalFormat("0.0");
    private String model_string;
    private SMOreg smo;
    private String[] attributesName = {"RAINFALL", "PSI", "EVENT", "WEEKEND", "OVERSEA_HOLIDAY", "LOCAL_HOLIDAY", "FOOTFALL"};
    private CompositeDisposable mCompositeDisposable;
    private XAxis weatherXAxis;
    private DatabaseReference mDatabase;
    private int counter = 0;
    private int maxCounter = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mCompositeDisposable = new CompositeDisposable();
        mDatabase = FirebaseDatabase.getInstance().getReference("data");

        trainModel();
        loadData();
        initWeatherChart();
        initFootfallChart();
        initEvent();
        initHoliday();

        swipeContainer.setColorSchemeResources(R.color.indigo, R.color.pink);
        swipeContainer.setOnRefreshListener(this::loadData);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    @OnClick(R.id.modelBtn)
    public void modelBtn() {
        new MaterialDialog.Builder(this).title("Regression Model")
                .content(model_string)
                .positiveText("CLOSE")
                .show();
    }

    @OnClick(R.id.predictBtn)
    public void predictBtn() {
        try {
            ArrayList<Attribute> attributeList = new ArrayList<>();
            for (String AttributesName : attributesName) {
                attributeList.add(new Attribute(AttributesName));
            }
            Instances testDataset = new Instances("Test", attributeList, attributesName.length);
            testDataset.setClassIndex(testDataset.numAttributes() - 1);

            Instance testInstance = new DenseInstance(attributesName.length);
            testInstance.setValue(attributeList.get(0), Double.parseDouble(rainfallInput.getText().toString()));
            testInstance.setValue(attributeList.get(1), Double.parseDouble(psiInput.getText().toString()));
            testInstance.setValue(attributeList.get(2), Double.parseDouble(eventInput.getText().toString()));
            if (weekendInput.isChecked()) {
                testInstance.setValue(attributeList.get(3), 1);
            } else {
                testInstance.setValue(attributeList.get(3), 0);
            }
            if (overseaHolidayInput.isChecked()) {
                testInstance.setValue(attributeList.get(4), 1);
            } else {
                testInstance.setValue(attributeList.get(4), 0);
            }
            if (localHolidayInput.isChecked()) {
                testInstance.setValue(attributeList.get(5), 1);
            } else {
                testInstance.setValue(attributeList.get(5), 0);
            }
            testInstance.setValue(attributeList.get(6), '?');
            testDataset.add(testInstance);

            double predictedFootfall = smo.classifyInstance(testDataset.instance(0));
            footfallPrediction.setText(String.format("Predicted FootFall: %s", String.valueOf((int) predictedFootfall)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.footfallCard)
    public void footfallCard() {
        Intent i = new Intent(MainActivity.this, FootFallActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.trendCard)
    public void trendCard() {
        Intent i = new Intent(MainActivity.this, TrendActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.regressionCard)
    public void regressionCard() {
        Intent i = new Intent(MainActivity.this, PredictionActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.imageWeatherInfo)
    public void weatherInfoImg(View v) {
        tooltipBuilder(v, "• Real-Time Weather and PSI information\n"
                + "• Click on each bar chart to get more info\n• Left Axis - Chance of Raining in %\n• Right Axis - Temperature in °C");
    }

    @OnClick(R.id.imageFootfallInfo)
    public void footfallInfoImg(View v) {
        tooltipBuilder(v, "• FootFall distribution across all Mandai Regions\n• Click on card to set threshold for each region\n• Click on each slice of chart to get more info");
    }

    @OnClick(R.id.imageEventInfo)
    public void eventInfoImg(View v) {
        tooltipBuilder(v, "• On-Going Events happening today");
    }

    @OnClick(R.id.imageHolidayInfo)
    public void holidayInfoImg(View v) {
        tooltipBuilder(v, "• Holidays happening today\n• Countries - SG, MY, PH, CN, ID, TH, AU");
    }

    @OnClick(R.id.imageTrendInfo)
    public void trendInfoImg(View v) {
        tooltipBuilder(v, "• Discover patterns and trends\nfrom historical data");
    }

    @OnClick(R.id.imagePredictionInfo)
    public void regressionInfoImg(View v) {
        tooltipBuilder(v, "• Predict footfall by setting the 6 factors\n• Click on card for more info");
    }

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

    //read training set and train model
    private void trainModel() {
        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(MainActivity.this.getAssets().open("training.arff"));
            Instances trainDataset = source.getDataSet();

            String[] opts = new String[]{"-R", "1"};
            Remove remove = new Remove();
            remove.setOptions(opts);
            remove.setInputFormat(trainDataset);

            Instances trainDatasetFiltered = Filter.useFilter(trainDataset, remove);

            trainDatasetFiltered.setClassIndex(trainDatasetFiltered.numAttributes() - 1);
            smo = new SMOreg();
            smo.buildClassifier(trainDatasetFiltered);
            model_string = smo.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //initialise footfall chart
    private void initFootfallChart() {
        currentFootfall.setText("0");
        footfallChart.setDrawEntryLabels(false);
        footfallChart.getDescription().setEnabled(false);
        footfallChart.setDragDecelerationFrictionCoef(0.95f);

        footfallChart.setDrawHoleEnabled(true);
        footfallChart.setHoleColor(Color.WHITE);

        footfallChart.setTransparentCircleColor(Color.WHITE);
        footfallChart.setTransparentCircleAlpha(110);

        footfallChart.setHoleRadius(58f);
        footfallChart.setTransparentCircleRadius(61f);

        footfallChart.setRotationAngle(0);
        footfallChart.setRotationEnabled(false);
        footfallChart.setHighlightPerTapEnabled(true);

        Legend l = footfallChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }

    //initialise weather chart and psi segments
    private void initWeatherChart() {
        weatherChart.setDrawBarShadow(false);
        weatherChart.getDescription().setEnabled(false);
        weatherChart.setPinchZoom(false);
        weatherChart.setDoubleTapToZoomEnabled(false);
        weatherChart.setScaleEnabled(false);
        weatherChart.setDrawGridBackground(false);

        weatherXAxis = weatherChart.getXAxis();
        weatherXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        weatherXAxis.setDrawGridLines(false);
        weatherXAxis.setGranularity(1f);
        weatherXAxis.setSpaceMin(0.5f);
        weatherXAxis.setSpaceMax(0.5f);

        YAxis leftAxis = weatherChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter((value, axis) -> (int) value + "%");
        YAxis rightAxis = weatherChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);
        rightAxis.setValueFormatter((value, axis) -> (int) value + "°C");

        Legend l = weatherChart.getLegend();
        l.setWordWrapEnabled(true);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);

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
        psiView.setShowDescriptionText(true);
        psiView.setDescriptionTextSize(30);
        psiView.setSegmentTextSize(35);
        psiView.setValueSignSize(120, 90);
        psiView.setValueSignColor(Color.BLACK);
        psiView.setSegments(psiSegments);
    }

    //initialise holiday recycler view
    private void initHoliday() {
        holidayRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        holidayRecyclerView.setItemAnimator(new DefaultItemAnimator());
        holidayRecyclerView.setHasFixedSize(true);
        holidayRecyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
    }

    //initialise event recycler view
    private void initEvent() {
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        eventRecyclerView.setItemAnimator(new DefaultItemAnimator());
        eventRecyclerView.setHasFixedSize(true);
        eventRecyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, LinearLayoutManager.VERTICAL));
    }

    //load all data from api calls and database
    private void loadData() {
        counter = 0;
        swipeContainer.setRefreshing(true);

        ApiService apiService = new ApiService();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar todayDate = Calendar.getInstance();
        Calendar tmlDate = Calendar.getInstance();
        tmlDate.add(Calendar.DATE, 1);

        mCompositeDisposable.add(apiService.serviceEvent().getCurrentEvent(0, sdf.format(todayDate.getTime()), sdf.format(tmlDate.getTime()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::eventResponse, this::eventError));

        mCompositeDisposable.add(apiService.serviceWeather().getWeather()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::weatherResponse, this::weatherError));

        mCompositeDisposable.add(apiService.servicePSI().getCurrentPSI()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::psiResponse, this::psiError));

        mCompositeDisposable.add(apiService.serviceFootfall().getCurrentFootFall()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::footfallResponse, this::footfallError));

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.US);
                SimpleDateFormat sdfToday = new SimpleDateFormat("d MMM", Locale.US);
                SimpleDateFormat sdfTodayYear = new SimpleDateFormat("d MMM yyyy", Locale.US);
                Calendar todayDate = Calendar.getInstance();
                int currentMonth = Integer.parseInt(sdf.format(todayDate.getTime()));

                Data firebaseData = dataSnapshot.getValue(Data.class);
                List<Holiday> holidayList = firebaseData.get2016().get(currentMonth - 1).getHolidays();
                List<Holiday> todayHolidayList = new ArrayList<>();

                for (int i = 0; i < holidayList.size(); i++) {
                    if (!holidayList.get(i).getDate().contains("-")) {
                        if (holidayList.get(i).getDate().equals(sdfToday.format(todayDate.getTime()))) {
                            todayHolidayList.add(holidayList.get(i));
                        }
                    } else {
                        try {
                            String date1 = holidayList.get(i).getDate().split(" - ")[0] + " 2016";
                            String date2 = holidayList.get(i).getDate().split(" - ")[1] + " 2016";
                            Date firstDate = sdfTodayYear.parse(date1);
                            Date secondDate = sdfTodayYear.parse(date2);
                            if (todayDate.getTime().after(firstDate) && todayDate.getTime().before(secondDate)) {
                                todayHolidayList.add(holidayList.get(i));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
                holidayCount.setText(String.format("Holiday Count - %s", String.valueOf(todayHolidayList.size())));
                holidayRecyclerView.setAdapter(new HolidayAdapter(todayHolidayList));

                ViewGroup.LayoutParams params = holidayRecyclerView.getLayoutParams();
                if (todayHolidayList.size() > 4) {
                    params.height = 820;
                    holidayRecyclerView.setLayoutParams(params);
                } else {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                counter++;
                if (counter == maxCounter) {
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar snackbar =
                        Snackbar.make(coordinatorLayout, "Holiday Error: " + databaseError.toString(),
                                Snackbar.LENGTH_LONG);
                snackbar.show();
                counter++;
                if (counter == maxCounter) {
                    swipeContainer.setRefreshing(false);
                }
            }
        });
    }

    //response from PSI api call
    private void psiResponse(PSI currentPSI) {
        Pm25SubIndex psi = currentPSI.getItems().get(0).getReadings().getPm25SubIndex();
        psiView.setValue((float) psi.getNorth());
        psiInput.setText(String.valueOf(psi.getNorth()));
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }

    //error from PSI api call
    private void psiError(Throwable error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "PSI Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
        snackbar.show();
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }

    //response from weather api call
    private void weatherResponse(Weather weather) {
        Currently current = weather.getCurrently();
        Hourly hourly = weather.getHourly();
        currentTemperature.setText(String.format("%s°C", String.format("Now - %s", current.getTemperature())));
        int roundedUp = (int) Math.round(current.getPrecipProbability() * 100);
        String rainProb = String.format("%s%%", Integer.toString(roundedUp));
        hourlyWeather.setText(String.format("%s - %s Rain", current.getSummary(), rainProb));
        dailyWeather.setText(hourly.getSummary());
        String icon = current.getIcon();
        switch (icon) {
            case "partly-cloudy-day":
                hourlyWeatherImg.setImageResource(R.drawable.partly_cloudy_day);
                break;
            case "partly-cloudy-night":
                hourlyWeatherImg.setImageResource(R.drawable.partly_cloudy_night);
                break;
            case "cloudy":
                hourlyWeatherImg.setImageResource(R.drawable.clouds);
                break;
            case "rain":
                hourlyWeatherImg.setImageResource(R.drawable.rain);
                break;
            case "clear-day":
                hourlyWeatherImg.setImageResource(R.drawable.clear);
                break;
            case "clear-night":
                hourlyWeatherImg.setImageResource(R.drawable.clear_night);
                break;
            case "wind":
                hourlyWeatherImg.setImageResource(R.drawable.wind);
                break;
            case "fog":
                hourlyWeatherImg.setImageResource(R.drawable.fog);
                break;
        }

        barEntriesRain.clear();
        lineEntriesTemp.clear();
        String[] xTimeShort = new String[hourly.getData().size()];
        String[] xTimeLong = new String[hourly.getData().size()];
        String[] weatherCondition = new String[hourly.getData().size()];
        double[] weatherTemp = new double[hourly.getData().size()];

        for (int i = 0; i < hourly.getData().size(); i++) {
            barEntriesRain.add(i, new BarEntry(i, (int) Math.round(hourly.getData().get(i).getPrecipProbability() * 100)));
            lineEntriesTemp.add(i, new Entry(i, Math.round(hourly.getData().get(i).getTemperature())));
            Date date = new Date(hourly.getData().get(i).getTime() * 1000L);
            SimpleDateFormat sdfShort = new SimpleDateFormat("HH:mm", Locale.US);
            SimpleDateFormat sdfLong = new SimpleDateFormat("EEE, HH:mm", Locale.US);
            sdfShort.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            xTimeShort[i] = sdfShort.format(date);
            xTimeLong[i] = sdfLong.format(date);
            weatherCondition[i] = hourly.getData().get(i).getSummary();
            weatherTemp[i] = hourly.getData().get(i).getTemperature();
        }

        weatherXAxis.setValueFormatter((value, axis) -> xTimeShort[(int) value % xTimeShort.length]);
        BarDataSet setRain = new BarDataSet(barEntriesRain, "Rain Chance");
        setRain.setColors(new int[]{R.color.indigo}, MainActivity.this);
        setRain.setDrawValues(false);
        BarData barDataRain = new BarData(setRain);

        LineDataSet setTemp = new LineDataSet(lineEntriesTemp, "Temperature");
        setTemp.setColors(new int[]{R.color.pink}, MainActivity.this);
        setTemp.setDrawValues(false);
        setTemp.setLineWidth(2.5f);
        setTemp.setCircleRadius(3f);
        setTemp.setCircleColor(Color.parseColor("#E91E63"));
        setTemp.setDrawCircleHole(false);
        setTemp.setHighlightEnabled(false);
        setTemp.setAxisDependency(YAxis.AxisDependency.RIGHT);
        LineData lineDataTemp = new LineData(setTemp);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(barDataRain);
        combinedData.setData(lineDataTemp);
        weatherChart.setData(combinedData);
        weatherChart.setVisibleXRangeMaximum(12);
        weatherChart.animateY(800, Easing.EasingOption.EaseInOutQuad);

        weatherChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) e.getX();
                int barValue = (int) e.getY();
                Snackbar snackbar = Snackbar.make(coordinatorLayout, xTimeLong[index] + ":  " + weatherCondition[index] + ",  "
                        + barValue + "% Rain" + ",  " + weatherTemp[index] + "°C", Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            @Override
            public void onNothingSelected() {
            }
        });
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }

    //error from weather api call
    private void weatherError(Throwable error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Weather Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
        snackbar.show();
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }

    //response from event api call
    private void eventResponse(Events events) {
        List<Event> eventList = events.getEvents();
        eventCount.setText(String.format("Event Count - %s", String.valueOf(events.getAttributes().getCount())));
        eventInput.setText(String.valueOf(events.getAttributes().getCount()));
        eventRecyclerView.setAdapter(new EventAdapter(eventList));

        ViewGroup.LayoutParams params = eventRecyclerView.getLayoutParams();
        if (eventList.size() > 4) {
            params.height = 820;
            eventRecyclerView.setLayoutParams(params);
        } else {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }

    //error from event api call
    private void eventError(Throwable error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Event Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
        snackbar.show();
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }

    //response from footfall api call
    private void footfallResponse(FootFall footfall) {
        List<SapInformation> regions = footfall.getSapInformation();
        pieEntriesFootFall.clear();
        int total = 0;
        for (int i = 0; i < regions.size(); i++) {
            total += regions.get(i).getNumVisitors();
            pieEntriesFootFall.add(i, new PieEntry(regions.get(i).getNumVisitors(), regionName[i]));
        }

        currentFootfall.setText(String.valueOf(total));
        PieDataSet setPieFootFall = new PieDataSet(pieEntriesFootFall, null);
        setPieFootFall.setColors(new int[]{R.color.lightblue, R.color.grey, R.color.pink, R.color.lightgreen, R.color.purple,
                R.color.yellow, R.color.orange, R.color.teal, R.color.indigo}, MainActivity.this);
        pieDataFootFall = new PieData(setPieFootFall);
        pieDataFootFall.setDrawValues(false);
        footfallChart.setData(pieDataFootFall);
        footfallChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
        SpannableString s = new SpannableString("Current\n" + Integer.toString((int) pieDataFootFall.getYValueSum()));
        footfallChart.setCenterText(s);

        footfallChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index = (int) h.getX();
                String label = pieEntriesFootFall.get(index).getLabel();
                String value = (int) pieEntriesFootFall.get(index).getValue() + " (" + df.format(pieEntriesFootFall.get(index).getValue() / pieDataFootFall.getYValueSum() * 100) + "%)";
                Snackbar snackbar = Snackbar.make(coordinatorLayout, label + ": " + value, Snackbar.LENGTH_LONG);
                snackbar.show();
            }

            @Override
            public void onNothingSelected() {
            }
        });
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }

    //error from footfall api call
    private void footfallError(Throwable error) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Footfall Error: " + error.getLocalizedMessage(), Snackbar.LENGTH_LONG);
        snackbar.show();
        counter++;
        if (counter == maxCounter) {
            swipeContainer.setRefreshing(false);
        }
    }
}


