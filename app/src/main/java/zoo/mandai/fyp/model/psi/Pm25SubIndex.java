package zoo.mandai.fyp.model.psi;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Pm25SubIndex {

    @SerializedName("north")
    @Expose
    private Integer north;

    public Integer getNorth() {
        return north;
    }

    public void setNorth(Integer north) {
        this.north = north;
    }
}