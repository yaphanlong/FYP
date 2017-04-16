package zoo.mandai.fyp.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import zoo.mandai.fyp.POJO.weather.Weather;

public interface InterfaceWeather {

    @GET("8ee94eebbb24ea81e40c962f976499c6/1.4040668,103.7917272?units=si")
    Observable<Weather> getWeather();
}
