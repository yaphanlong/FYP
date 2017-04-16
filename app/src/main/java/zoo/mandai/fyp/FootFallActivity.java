package zoo.mandai.fyp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import zoo.mandai.fyp.api.ApiService;
import zoo.mandai.fyp.api.InterfaceFootfall;
import zoo.mandai.fyp.model.firebase.Threshold;
import zoo.mandai.fyp.model.footfall.FootFall;
import zoo.mandai.fyp.model.footfall.SapInformation;


public class FootFallActivity extends AppCompatActivity {

    @BindViews({R.id.kfcText, R.id.australianText, R.id.entranceText, R.id.ahmengText, R.id.sphText, R.id.whrcText, R.id.ampText, R.id.elepantText, R.id.taxiText})
    List<TextView> regionTextList;
    @BindViews({R.id.kfcMax, R.id.australianMax, R.id.entranceMax, R.id.ahmengMax, R.id.sphMax, R.id.whrcMax, R.id.ampMax, R.id.elephantMax, R.id.taxiMax})
    List<TextView> regionMaxList;
    @BindViews({R.id.kfcCurrent, R.id.australianCurrent, R.id.entranceCurrent, R.id.ahmengCurrent, R.id.sphCurrent, R.id.whrcCurrent, R.id.ampCurrent, R.id.elephantCurrent, R.id.taxiCurrent})
    List<TextView> regionCurrentList;
    @BindViews({R.id.kfcBar, R.id.australianBar, R.id.entranceBar, R.id.ahmengBar, R.id.sphBar, R.id.whrcBar, R.id.ampBar, R.id.elephantBar, R.id.taxiBar})
    List<ProgressBar> regionBarList;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.swipeContainer) SwipeRefreshLayout swipeContainer;
    @BindView(R.id.coordinatorLayout) CoordinatorLayout coordinatorLayout;
    @BindArray(R.array.regions) String[] regionName;

    private DatabaseReference mDatabase;
    private Context context;
    private int notificationID = 0;
    private CompositeDisposable mCompositeDisposable;
    private int counter = 0;
    private int maxCounter = 2;
    private Threshold threshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_footfall);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mCompositeDisposable = new CompositeDisposable();
        context = this.getApplicationContext();
        mDatabase = FirebaseDatabase.getInstance().getReference("threshold");

        for (int i = 0; i < regionName.length; i++) {
            regionTextList.get(i).setText(regionName[i]);
            regionCurrentList.get(i).setText("0");
            regionMaxList.get(i).setText("0");
        }

        swipeContainer.setColorSchemeResources(R.color.indigo, R.color.pink);
        swipeContainer.setOnRefreshListener(this::loadData);
        loadData();
    }

    @OnClick(R.id.kfcRow)
    public void kfcRow() {
        showDialog(regionName[0], regionMaxList.get(0).getText().toString(), 0);
    }

    @OnClick(R.id.australianRow)
    public void australianRow() {
        showDialog(regionName[1], regionMaxList.get(1).getText().toString(), 1);
    }

    @OnClick(R.id.entranceRow)
    public void entranceRow() {
        showDialog(regionName[2], regionMaxList.get(2).getText().toString(), 2);
    }

    @OnClick(R.id.ahmengRow)
    public void ahmengRow() {
        showDialog(regionName[3], regionMaxList.get(3).getText().toString(), 3);
    }

    @OnClick(R.id.sphRow)
    public void sphRow() {
        showDialog(regionName[4], regionMaxList.get(4).getText().toString(), 4);
    }

    @OnClick(R.id.whrcRow)
    public void whrcRow() {
        showDialog(regionName[5], regionMaxList.get(5).getText().toString(), 5);
    }

    @OnClick(R.id.ampRow)
    public void ampRow() {
        showDialog(regionName[6], regionMaxList.get(6).getText().toString(), 6);
    }

    @OnClick(R.id.elephantRow)
    public void elephantRow() {
        showDialog(regionName[7], regionMaxList.get(7).getText().toString(), 7);
    }

    @OnClick(R.id.taxiRow)
    public void taxiRow() {
        showDialog(regionName[8], regionMaxList.get(8).getText().toString(), 8);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    //load data from api call and database
    private void loadData() {
        counter = 0;
        swipeContainer.setRefreshing(true);

        mCompositeDisposable.add(new ApiService().serviceFootfall().getCurrentFootFall()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::footfallResponse, this::footfallError));

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                threshold = dataSnapshot.getValue(Threshold.class);

                for (int i = 0; i < regionName.length; i++) {
                    regionMaxList.get(i).setText(String.valueOf(threshold.getRegions().get(i).getLimit()));
                    regionBarList.get(i).setMax(threshold.getRegions().get(i).getLimit());
                }
                counter++;
                if (counter == maxCounter) {
                    for (int i = 0; i < regionName.length; i++) {
                        progressStatus(Integer.parseInt(regionCurrentList.get(i).getText().toString()),
                                Integer.parseInt(regionMaxList.get(i).getText().toString()), regionBarList.get(i), regionName[i]);
                    }
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Database Error: " + databaseError.toString(), Snackbar.LENGTH_LONG);
                snackbar.show();
                counter++;
                if (counter == maxCounter) {
                    swipeContainer.setRefreshing(false);
                }
            }
        });
    }

    //response from footfall api call
    private void footfallResponse(FootFall footfall) {
        List<SapInformation> regions = footfall.getSapInformation();

        for (int i = 0; i < regions.size(); i++) {
            regionCurrentList.get(i).setText(String.valueOf(regions.get(i).getNumVisitors()));
            regionBarList.get(i).setProgress(regions.get(i).getNumVisitors());
        }
        counter++;
        if (counter == maxCounter) {
            for (int i = 0; i < regions.size(); i++) {
                progressStatus(regions.get(i).getNumVisitors(), Integer.parseInt(regionMaxList.get(i).getText().toString()), regionBarList.get(i), regionName[i]);
            }
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

    //display dialogs
    private void showDialog(String title, String value, int index) {
        new MaterialDialog.Builder(this)
                .title(title)
                .positiveText("OK")
                .negativeText("CANCEL")
                .inputRange(1, 4)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input(null, value, (dialog, input) -> {
                    threshold.getRegions().get(index).setLimit(Integer.parseInt(input.toString()));
                    mDatabase.setValue(threshold);
                    loadData();
                }).show();
    }

    //set progress bar color if threshold exceeds current footfall and trigger notification
    private void progressStatus(int current, int max, ProgressBar barStatus, String region) {
        if (current >= max) {
            barStatus.setProgressTintList(getColorStateList(R.color.colorAccent));
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(R.drawable.clear);
            mBuilder.setContentTitle("Mandai Zoo - Overcrowded");
            mBuilder.setContentText(region + ": " + current + "/" + max);
            mBuilder.setDefaults(-1);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(notificationID++, mBuilder.build());
        } else {
            barStatus.setProgressTintList(getColorStateList(R.color.colorPrimary));
        }
        barStatus.setProgress(current);
    }
}
