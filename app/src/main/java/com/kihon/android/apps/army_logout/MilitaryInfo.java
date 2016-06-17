package com.kihon.android.apps.army_logout;

import com.google.gson.Gson;

import org.joda.time.DateTime;

/**
 * Created by kihon on 2016/06/18.
 */
public class MilitaryInfo {

    private long begin;
    private int period;
    private int discount;

    public static MilitaryInfo parse(String jsonString) {
        if (jsonString == null) {
            return new Gson().fromJson(new MilitaryInfo(DateTime.now().getMillis(), MainActivity.ServiceTime.ONE_YEAR, 30).getJsonString(), MilitaryInfo.class);
        } else {
            return new Gson().fromJson(jsonString, MilitaryInfo.class);
        }
    }

    public MilitaryInfo(long loginMillis, MainActivity.ServiceTime serviceTime, int deleteDays) {
        begin = loginMillis;
        period = serviceTime.ordinal();
        discount = deleteDays;
    }

    public String getJsonString() {
        return new Gson().toJson(this);
    }

    public long getBegin() {
        return begin;
    }

    public int getPeriod() {
        return period;
    }

    public int getDiscount() {
        return discount;
    }

    public MilitaryInfo setBegin(long begin) {
        this.begin = begin;
        return this;
    }

    public MilitaryInfo setPeriod(MainActivity.ServiceTime period) {
        this.period = period.ordinal();
        return this;
    }

    public MilitaryInfo setPeriod(int period) {
        this.period = period;
        return this;
    }

    public MilitaryInfo setDiscount(int discount) {
        this.discount = discount;
        return this;
    }
}
