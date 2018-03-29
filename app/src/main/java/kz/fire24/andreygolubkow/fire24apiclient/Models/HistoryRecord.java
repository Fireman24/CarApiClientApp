package kz.fire24.andreygolubkow.fire24apiclient.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class HistoryRecord {

    @SerializedName("id")
    @Expose
    public int Id;

    @SerializedName("dateTime")
    @Expose
    public DateTime DateTime;

    @SerializedName("record")
    @Expose
    public String Record;


}
