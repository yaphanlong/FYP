package zoo.mandai.fyp.api;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    public InterfaceWeather serviceWeather() {
        return new Retrofit.Builder().baseUrl("https://api.darksky.net/forecast/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InterfaceWeather.class);
    }

    public InterfacePSI servicePSI() {
        return new Retrofit.Builder().baseUrl("https://api.data.gov.sg/v1/environment/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InterfacePSI.class);
    }

    public InterfaceFootfall serviceFootfall() {
        return new Retrofit.Builder().baseUrl("https://api.sap.lbasense.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(InterfaceFootfall.class);
    }

    public InterfaceEvent serviceEvent() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", Credentials.basic("project", "kyf3vqcqctsb"))
                            .header("Accept", "application/json")
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                });

        return new Retrofit.Builder().baseUrl("http://api.eventfinda.sg/v2/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build()
                .create(InterfaceEvent.class);
    }
}
