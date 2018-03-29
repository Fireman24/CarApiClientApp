package kz.fire24.andreygolubkow.fire24apiclient.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.List;

public class Fire {

    @SerializedName("id")
    @Expose
    public int Id;

    @SerializedName("address")
    @Expose
    public String Address;

    @SerializedName("rank")
    @Expose
    public int Rank;

    @SerializedName("gpsPoint")
    @Expose
    public GpsPoint GpsPoint;

    @SerializedName("startDateTime")
    @Expose
    public String StartDateTime;

    @SerializedName("finishDateTime")
    @Expose
    public String FinishDateTime;

    @SerializedName("comments")
    @Expose
    public String Comments;

    @SerializedName("receiver")
    @Expose
    public String Receiver;

    @SerializedName("active")
    @Expose
    public boolean Active;

    @SerializedName("manager")
    @Expose
    public String Manager;

    @SerializedName("history")
    @Expose
    public List<HistoryRecord> History;

    @SerializedName("images")
    @Expose
    public List<FireImage> Images;


}
