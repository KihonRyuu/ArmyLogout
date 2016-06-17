package com.kihon.android.apps.army_logout;

import com.google.common.collect.Range;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * Created by kihon on 2016/06/08.
 */
public class ServiceUtil {

    private final long mStartTime;
    private final MainActivity.ServiceTime mServiceTime;
    private final DateTime mStartDateTime;
    private int mDiscountDays;
    private DateTime mStandLogoutDateTime;
    private DateTime mRealLogoutDateTime;
    private Period mPeriod;

    public ServiceUtil(long startTimeInMillis, MainActivity.ServiceTime serviceTime, int discountDays) {
        mStartTime = startTimeInMillis;
        mServiceTime = serviceTime;
        mDiscountDays = discountDays;
        mStartDateTime = new DateTime(startTimeInMillis);
        setEndTime();
    }

    public MainActivity.ServiceTime getServiceTime() {
        return mServiceTime;
    }

    public ServiceUtil(MilitaryInfo militaryInfo) {
        this(militaryInfo.getBegin(), MainActivity.ServiceTime.values()[militaryInfo.getPeriod()], militaryInfo.getDiscount());
    }

    public String getLoginDateString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy年M月d日");
        return mStartDateTime.toString(fmt);
    }

    public String getRealLogoutDateString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy年M月d日");
        return mRealLogoutDateTime.toString(fmt);
    }

    private void setEndTime() {
        switch (mServiceTime) {
            case ONE_YEAR:
                mStandLogoutDateTime = mStartDateTime.plusYears(1);
                break;
            case FOUR_YEARS:
                mStandLogoutDateTime = mStartDateTime.plusYears(4);
                break;
            case ONE_YEAR_FIFTH_DAYS:
                mStandLogoutDateTime = mStartDateTime.plusYears(1).plusDays(15);
                break;
            case FOUR_MONTHS:
                mStandLogoutDateTime = mStartDateTime.plusMonths(4);
                break;
            case FOUR_MONTHS_FIVE_DAYS:
                mStandLogoutDateTime = mStartDateTime.plusMonths(4).plusDays(5);
                break;
            case SIX_MONTHS:
                mStandLogoutDateTime = mStartDateTime.plusMonths(6);
                break;
            case THREE_YEARS:
                mStandLogoutDateTime = mStartDateTime.plusYears(3);
                break;
            case ONE_YEAR_SIX_MONTHS:
                mStandLogoutDateTime = mStartDateTime.plusYears(1).plusMonths(6);
                break;
            case TEN_MONTHS:
                mStandLogoutDateTime = mStartDateTime.plusMonths(10);
                break;
        }
        mRealLogoutDateTime = mStandLogoutDateTime.minusDays(getDiscount());
    }


    public boolean isLoggedIn() {
        return mStartDateTime.isBeforeNow();
    }

    /**
     * - -> not login
     * 0 -> tomorrow or today login
     *
     * @return Number of days.
     */
    public String getPassedDay() {
        return String.valueOf(Days.daysBetween(mStartDateTime.toInstant(), DateTime.now().toInstant()).getDays());
    }

    public Period getRemainingPeriod() {
        return mPeriod = new Period(DateTime.now(), mRealLogoutDateTime, PeriodType.dayTime());
    }

    public String getRemainingDayWithString() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .printZeroAlways().appendDays().appendSuffix("天").appendSeparator(" ")
                .printZeroAlways().minimumPrintedDigits(2).appendHours().appendSeparator(":")
                .printZeroAlways().minimumPrintedDigits(2).appendMinutes().appendSeparator(":")
                .printZeroAlways().minimumPrintedDigits(2).appendSeconds()
                .toFormatter();
        return getRemainingDayWithString(formatter);
    }

    public String getRemainingDayWithString(PeriodFormatter formatter) {
        return formatter.print(isLoggedIn() ? getRemainingPeriod() : new Period(DateTime.now(), mStartDateTime, PeriodType.dayTime()));
    }

    private int getDiscount() {
        return mDiscountDays;
    }

    public float getPercentage() {
        long pass = new Period(mStartDateTime, DateTime.now(), PeriodType.dayTime()).toPeriod().toStandardDuration().getMillis();
        long total = new Period(mStartDateTime, mRealLogoutDateTime, PeriodType.dayTime()).toPeriod().toStandardDuration().getMillis();
        float percent = pass * 100.0f / total;
        return percent >= 100.0f ? 100.0f : percent;
    }

    public String getUntilHundredDaysRemainingDaysWithString() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix("天").toFormatter();
        return formatter.print(getRemainingPeriod().minusDays(30).toPeriod());
    }

    public boolean isHundredDays() {
        return Range.open(30, 100).contains(getRemainingPeriod().getDays());
    }

    public void setDiscountDays(int value) {
        mDiscountDays = value;
        setEndTime();
    }

    public int getDiscountDays() {
        return mDiscountDays;
    }
}
