package tw.kihon.armylogout;

import com.google.gson.Gson;

import org.joda.time.DateTime;

/**
 * Created by kihon on 2016/06/18.
 */
public class MilitaryInfo {

    private long begin;
    private int period;
    private int discount;
    private int periodType;
    private CustomPeriod customPeriod;

    static final int DayTime = 0;
    static final int YearMonthDayTime = 1;

    public static MilitaryInfo parse(String jsonString) {
        if (jsonString == null) {
            return new Gson().fromJson(new MilitaryInfo(DateTime.now().getMillis(), ServiceTime.ONE_YEAR, 30, DayTime).getJsonString(), MilitaryInfo.class);
        } else {
            return new Gson().fromJson(jsonString, MilitaryInfo.class);
        }
    }

    public MilitaryInfo(long loginMillis, ServiceTime serviceTime, int deleteDays, int periodType) {
        begin = loginMillis;
        period = serviceTime.ordinal();
        discount = deleteDays;
        this.periodType = periodType;
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

    public int getPeriodType() {
        return periodType;
    }

    public int getDiscount() {
        return discount;
    }

    public MilitaryInfo setBegin(long begin) {
        this.begin = begin;
        return this;
    }

    public MilitaryInfo setPeriod(ServiceTime period) {
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

    public void switchPeriodType() {
        if (periodType == DayTime)
            periodType = YearMonthDayTime;
        else
            periodType = DayTime;
    }

    public void setCustomPeriod(CustomPeriod customPeriod) {
        this.customPeriod = customPeriod;
    }

    public CustomPeriod getCustomPeriod() {
        return customPeriod;
    }
}
