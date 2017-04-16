package zoo.mandai.fyp.POJO.firebase;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class _2016 {

    @SerializedName("days")
    @Expose
    private List<Day> days = null;
    @SerializedName("holidays")
    @Expose
    private List<Holiday> holidays = null;
    @SerializedName("month")
    @Expose
    private String month;
    @SerializedName("psi")
    @Expose
    private Psi psi;
    @SerializedName("rainyday")
    @Expose
    private Integer rainyday;
    @SerializedName("nonrainyday")
    @Expose
    private Integer nonrainyday;
    @SerializedName("regions")
    @Expose
    private List<Region> regions = null;
    @SerializedName("total")
    @Expose
    private Integer total;
    @SerializedName("tourist")
    @Expose
    private Integer tourist;

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }

    public List<Holiday> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<Holiday> holidays) {
        this.holidays = holidays;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Psi getPsi() {
        return psi;
    }

    public void setPsi(Psi psi) {
        this.psi = psi;
    }

    public Integer getRainyday() {
        return rainyday;
    }

    public void setRainyday(Integer rainyday) {
        this.rainyday = rainyday;
    }

    public Integer getNonrainyday() {
        return nonrainyday;
    }

    public void setNonrainyday(Integer nonrainyday) {
        this.nonrainyday = nonrainyday;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getTourist() {
        return tourist;
    }

    public void setTourist(Integer tourist) {
        this.tourist = tourist;
    }

}