package zoo.mandai.fyp.POJO.footfall;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")

public class FootFall {
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("sapInformation")
    @Expose
    private List<SapInformation> sapInformation = new ArrayList<SapInformation>();

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<SapInformation> getSapInformation() {
        return sapInformation;
    }

    public void setSapInformation(List<SapInformation> sapInformation) {
        this.sapInformation = sapInformation;
    }
}
