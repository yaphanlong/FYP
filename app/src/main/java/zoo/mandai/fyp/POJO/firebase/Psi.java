package zoo.mandai.fyp.POJO.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Psi {

    @SerializedName("goodmoderate")
    @Expose
    private Integer goodmoderate;
    @SerializedName("hazardous")
    @Expose
    private Integer hazardous;
    @SerializedName("unhealthyveryunhealthy")
    @Expose
    private Integer unhealthyveryunhealthy;

    public Integer getGoodmoderate() {
        return goodmoderate;
    }

    public void setGoodmoderate(Integer goodmoderate) {
        this.goodmoderate = goodmoderate;
    }

    public Integer getHazardous() {
        return hazardous;
    }

    public void setHazardous(Integer hazardous) {
        this.hazardous = hazardous;
    }

    public Integer getUnhealthyveryunhealthy() {
        return unhealthyveryunhealthy;
    }

    public void setUnhealthyveryunhealthy(Integer unhealthyveryunhealthy) {
        this.unhealthyveryunhealthy = unhealthyveryunhealthy;
    }

}