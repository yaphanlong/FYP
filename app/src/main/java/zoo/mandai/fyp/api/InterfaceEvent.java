package zoo.mandai.fyp.api;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import zoo.mandai.fyp.model.event.Events;

public interface InterfaceEvent {
    @GET("events.json?rows=20&fields=event:(name,url,datetime_summary)")
    Observable<Events> getCurrentEvent(@Query("offset") int offset, @Query("start_date") String startDate, @Query("end_date") String endDate);
}
