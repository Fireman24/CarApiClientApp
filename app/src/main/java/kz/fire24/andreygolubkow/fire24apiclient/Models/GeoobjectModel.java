package kz.fire24.andreygolubkow.fire24apiclient.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import kz.fire24.andreygolubkow.fire24apiclient.Models.GpsPoint;

/**
 * Created by andreygolubkow on 26.09.2017.
 */

public class GeoobjectModel {
    @SerializedName("popupText")
    @Expose
    public String popupText;
    @SerializedName("marker")
    @Expose
    public String marker;
    @SerializedName("gpsPoint")
    @Expose
    public GpsPoint gpsPoint;

}
