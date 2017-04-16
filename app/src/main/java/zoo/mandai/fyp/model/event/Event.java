package zoo.mandai.fyp.model.event;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Event {

    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("datetime_summary")
    @Expose
    private String datetimeSummary;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatetimeSummary() {
        return datetimeSummary;
    }

    public void setDatetimeSummary(String datetimeSummary) {
        this.datetimeSummary = datetimeSummary;
    }

}