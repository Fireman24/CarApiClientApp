package kz.fire24.andreygolubkow.fire24apiclient.Services;

import java.util.List;

import kz.fire24.andreygolubkow.fire24apiclient.Models.FireImage;
import kz.fire24.andreygolubkow.fire24apiclient.Models.GeoobjectModel;
import kz.fire24.andreygolubkow.fire24apiclient.Models.Departure;
import kz.fire24.andreygolubkow.fire24apiclient.Models.Fire;
import kz.fire24.andreygolubkow.fire24apiclient.Models.GpsPoint;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by andreygolubkow on 12.09.2017.
 */

public interface FiremanService {

    @GET("firecarapi/{carId}/departure")
    Call<Departure> GetDeparture(@Path("carId") String carId);

    @GET("firecarapi/{carId}/fire")
    Call<Fire> GetFire(@Path("carId") String carId);

    @POST("firecarapi/{carId}/location")
    Call<String> PostLocation(@Path("carId") String carId, @Body GpsPoint gpsPoint);

    @GET("firecarapi/{carId}/geoobjects")
    Call<List<GeoobjectModel>> GetGeoObjects(@Path("carId") String carId);

    @GET("firecarapi/{carId}/images")
    Call<List<FireImage>> GetImages(@Path("carId") String carId);

}
