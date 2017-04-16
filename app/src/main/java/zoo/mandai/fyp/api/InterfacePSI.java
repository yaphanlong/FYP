package zoo.mandai.fyp.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import zoo.mandai.fyp.model.psi.PSI;

public interface InterfacePSI {

    @Headers("api-key: 2GRGLx8VIm2R8Pnr7M6s5Xet6ZRq7qoM")
    @GET("psi")
    Observable<PSI> getCurrentPSI();
}