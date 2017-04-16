package zoo.mandai.fyp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gc.materialdesign.views.Button;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import zoo.mandai.fyp.model.event.Event;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<Event> eventList;

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.event_title) TextView textTitle;
        @BindView(R.id.event_date) TextView textDate;
        @BindView(R.id.event_url) Button buttonUrl;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_list_row, viewGroup, false);
        return new EventAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventAdapter.ViewHolder viewHolder, int i) {

        viewHolder.textTitle.setText(eventList.get(i).getName());
        viewHolder.textDate.setText(eventList.get(i).getDatetimeSummary());
        viewHolder.buttonUrl.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(eventList.get(i).getUrl()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}