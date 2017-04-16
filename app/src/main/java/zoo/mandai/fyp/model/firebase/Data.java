package zoo.mandai.fyp.model.firebase;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("2016")
    @Expose
    private List<_2016> _2016 = null;

    public List<_2016> get2016() {
        return _2016;
    }

    public void set2016(List<_2016> _2016) {
        this._2016 = _2016;
    }
}