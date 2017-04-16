package zoo.mandai.fyp.model.footfall;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")

public class SapInformation {
    @SerializedName("regionID")
    @Expose
    private Integer regionID;
    @SerializedName("changeRate")
    @Expose
    private Integer changeRate;
    @SerializedName("numVisitors")
    @Expose
    private Integer numVisitors;

    public Integer getRegionID() {
        return regionID;
    }

    public void setRegionID(Integer regionID) {
        this.regionID = regionID;
    }

    public Integer getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(Integer changeRate) {
        this.changeRate = changeRate;
    }

    public Integer getNumVisitors() {
        return numVisitors;
    }

    public void setNumVisitors(Integer numVisitors) {
        this.numVisitors = numVisitors;
    }
}