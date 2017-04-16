package zoo.mandai.fyp.POJO.psi;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Readings {

    @SerializedName("pm25_sub_index")
    @Expose
    private Pm25SubIndex pm25SubIndex;

    public Pm25SubIndex getPm25SubIndex() {
        return pm25SubIndex;
    }

    public void setPm25SubIndex(Pm25SubIndex pm25SubIndex) {
        this.pm25SubIndex = pm25SubIndex;
    }
}