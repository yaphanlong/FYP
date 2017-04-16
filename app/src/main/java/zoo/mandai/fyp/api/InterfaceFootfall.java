package zoo.mandai.fyp.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import zoo.mandai.fyp.POJO.footfall.FootFall;

public interface InterfaceFootfall {

    @GET("CurrentSAPValuePerRegion?user=sitstudents&pass=aiurldd952jeu49r&siteId=282")
    Observable<FootFall> getCurrentFootFall();
}
