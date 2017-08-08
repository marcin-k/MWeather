package com.mvk.mweather.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by marcin on 05/02/2017.
 */

public class Weather {
    private String mIcon;
    private long mTime;
    private double mTemperature;
    private double mRainChances;
    private String mSummary;
    private String mTimeZone;

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }




    public int getPrecipChance() {
        double precipProcentage = mRainChances *100;
        return (int) Math.round(precipProcentage);
    }

    public void setPrecipChance(double precipChance) {
        mRainChances = precipChance;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public int getTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        //Converts fahrenheits to Celcius
        temperature = ((temperature - 32) / 1.8);
        mTemperature = temperature;
    }

    public long getTime() {
        return mTime;
    }

    public String getFormattedTime(){
        //contains only one h to avoid leading zero in hours 1-9 a for am/pm
        // docs: https://developer.android.com/reference/java/text/SimpleDateFormat.html
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        //Java date class take time milli secs, time from online api is in secs
        Date dateTime = new Date(getTime()*1000);
        String timeString = formatter.format(dateTime);
        return timeString;
    }

    public void setTime(long time) {
        mTime = time;
    }

}
