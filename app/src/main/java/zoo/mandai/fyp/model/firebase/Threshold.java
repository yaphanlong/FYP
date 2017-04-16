package zoo.mandai.fyp.model.firebase;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Threshold {

    @SerializedName("regions")
    @Expose
    private List<ThresholdRegion> regions = null;

    public List<ThresholdRegion> getRegions() {
        return regions;
    }

    public void setRegions(List<ThresholdRegion> regions) {
        this.regions = regions;
    }

}