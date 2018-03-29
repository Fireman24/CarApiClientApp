package kz.fire24.andreygolubkow.fire24apiclient.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Departure {

    @SerializedName("id")
    @Expose
    public int Id;


    @SerializedName("address")
    @Expose
    public String Address;

    @SerializedName("dateTime")
    @Expose
    public String DateTime;

    @SerializedName("intent")
    @Expose
    public String Intent;

    @SerializedName("receiver")
    @Expose
    public String Receiver;

    @SerializedName("active")
    @Expose
    public Boolean Active;

    @SerializedName("manager")
    @Expose
    public String Manager;

    @SerializedName("comments")
    @Expose
    public String Comments;

    @SerializedName("gpsPoint")
    @Expose
    public GpsPoint GpsPoint;

    @SerializedName("history")
    @Expose
    public List<HistoryRecord> History;

    @SerializedName("images")
    @Expose
    public List<FireImage> Images;

}
