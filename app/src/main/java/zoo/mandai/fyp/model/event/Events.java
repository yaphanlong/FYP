package zoo.mandai.fyp.model.event;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Events {

    @SerializedName("@attributes")
    @Expose
    private Attributes attributes;
    @SerializedName("events")
    @Expose
    private List<Event> events = null;

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

}