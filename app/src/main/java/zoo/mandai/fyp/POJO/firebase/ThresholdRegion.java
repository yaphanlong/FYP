package zoo.mandai.fyp.POJO.firebase;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ThresholdRegion {

    @SerializedName("limit")
    @Expose
    private Integer limit;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

}