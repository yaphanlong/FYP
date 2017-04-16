package zoo.mandai.fyp.POJO.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Day {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("rainfall")
    @Expose
    private Float rainfall;
    @SerializedName("event")
    @Expose
    private Integer event;
    @SerializedName("psi")
    @Expose
    private Integer psi;
    @SerializedName("total")
    @Expose
    private Integer total;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getRainfall() {
        return rainfall;
    }

    public void setRainfall(Float rainfall) {
        this.rainfall = rainfall;
    }

    public Integer getEvent() {
        return event;
    }

    public void setEvent(Integer event) {
        this.event = event;
    }

    public Integer getPsi() {
        return psi;
    }

    public void setPsi(Integer psi) {
        this.psi = psi;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

}