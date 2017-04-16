package zoo.mandai.fyp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import zoo.mandai.fyp.model.firebase.Holiday;


public class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.ViewHolder> {
    private List<Holiday> holidayList;

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.holiday_title) TextView textTitle;
        @BindView(R.id.holiday_date) TextView textDate;
        @BindView(R.id.holiday_country) TextView textCountry;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    HolidayAdapter(List<Holiday> holidayList) {
        this.holidayList = holidayList;
    }

    @Override
    public HolidayAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.holiday_list_row, viewGroup, false);
        return new HolidayAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HolidayAdapter.ViewHolder viewHolder, int i) {

        viewHolder.textTitle.setText(holidayList.get(i).getTitle());
        viewHolder.textDate.setText(holidayList.get(i).getDate());
        viewHolder.textCountry.setText(holidayList.get(i).getCountry());
    }

    @Override
    public int getItemCount() {
        return holidayList.size();
    }
}