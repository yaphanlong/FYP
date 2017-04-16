package zoo.mandai.fyp.model.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Region {

    @SerializedName("visitors")
    @Expose
    private Integer visitors;

    public Integer getVisitors() {
        return visitors;
    }

    public void setVisitors(Integer visitors) {
        this.visitors = visitors;
    }

}