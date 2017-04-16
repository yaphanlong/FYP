package zoo.mandai.fyp.POJO.footfall;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SummaryStat {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("numVisitors")
    @Expose
    private Integer numVisitors;
    @SerializedName("numReturningVisitors")
    @Expose
    private Integer numReturningVisitors;
    @SerializedName("avgDuration")
    @Expose
    private Integer avgDuration;

    /**
     *
     * @return
     * The date
     */
    public String getDate() {
        return date;
    }

    /**
     *
     * @param date
     * The date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     *
     * @return
     * The numVisitors
     */
    public Integer getNumVisitors() {
        return numVisitors;
    }

    /**
     *
     * @param numVisitors
     * The numVisitors
     */
    public void setNumVisitors(Integer numVisitors) {
        this.numVisitors = numVisitors;
    }

    /**
     *
     * @return
     * The numReturningVisitors
     */
    public Integer getNumReturningVisitors() {
        return numReturningVisitors;
    }

    /**
     *
     * @param numReturningVisitors
     * The numReturningVisitors
     */
    public void setNumReturningVisitors(Integer numReturningVisitors) {
        this.numReturningVisitors = numReturningVisitors;
    }

    /**
     *
     * @return
     * The avgDuration
     */
    public Integer getAvgDuration() {
        return avgDuration;
    }

    /**
     *
     * @param avgDuration
     * The avgDuration
     */
    public void setAvgDuration(Integer avgDuration) {
        this.avgDuration = avgDuration;
    }

}