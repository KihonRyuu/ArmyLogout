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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by kihon on 2016/06/08.
 */
public class ServiceUtil {

    private final long mStartTimeInMillis;
    private final ServiceTime mServiceTime;
    private final DateTime mStartDateTime;
    private final int mCounterTextPeriodType;
    private final MilitaryInfo mMilitaryInfo;
    private int mDiscountDays;
    private DateTime mStandLogoutDateTime;
    private DateTime mRealLogoutDateTime;
    private Period mPeriod;

    public ServiceUtil(MilitaryInfo militaryInfo) {
        mMilitaryInfo = militaryInfo;
        mStartTimeInMillis = militaryInfo.getBegin();
        mServiceTime = ServiceTime.values()[militaryInfo.getPeriod()];
        mDiscountDays = militaryInfo.getDiscount();
        mCounterTextPeriodType = militaryInfo.getPeriodType();
        mStartDateTime = new DateTime(militaryInfo.getBegin());
        setEndTime();
    }

    public long getStartTimeInMillis() {
        return mStartTimeInMillis;
    }

    public ServiceTime getServiceTime() {
        return mServiceTime;
    }

    public String getLoginDateString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy年M月d日");
        return mStartDateTime.toString(fmt) + "(" + getDayOfWeekShortest(mStartDateTime.getMillis()) + ")";
    }

    private String getDayOfWeekShortest(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE", Locale.TAIWAN);
        return sdf.format(new Date(millis));
    }

    public String getRealLogoutDateString() {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy年M月d日");
        return mRealLogoutDateTime.toString(fmt) + "(" + getDayOfWeekShortest(mRealLogoutDateTime.getMillis()) + ")";
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
            case CUSTOM:
                CustomPeriod customPeriod = mMilitaryInfo.getCustomPeriod();
                int year = customPeriod.getYear();
                int monthOfYear = customPeriod.getMonthOfYear();
                int dayOfMonth = customPeriod.getDayOfMonth();
                mStandLogoutDateTime = mStartDateTime.plusYears(year)
                                .plusMonths(monthOfYear)
                                .plusDays(dayOfMonth);
                String stringBuilder = (year == 0 ? "" : year + "年") +
                        (monthOfYear == 0 ? "" : monthOfYear + "個月") +
                        (dayOfMonth == 0 ? "" : dayOfMonth + "天");
                mServiceTime.setDisplayText(stringBuilder);
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

    public Period getRemainingPeriod(PeriodType periodType) {
        return mPeriod = new Period(DateTime.now(), mRealLogoutDateTime, periodType);
    }

    public String getRemainingDayWithString() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("年")
                .appendMonths().appendSuffix("個月")
                .printZeroAlways().appendDays().appendSuffix("天").appendSeparator(" ")
                .printZeroAlways().minimumPrintedDigits(2).appendHours().appendSeparator(":")
                .printZeroAlways().minimumPrintedDigits(2).appendMinutes().appendSeparator(":")
                .printZeroAlways().minimumPrintedDigits(2).appendSeconds()
                .toFormatter();
        return getRemainingDayWithString(formatter, getCounterTextPeriodType());
    }

    public String getRemainingYearDays() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendYears().appendSuffix("年").appendSeparator(" ")
                .printZeroAlways().appendDays()
                .toFormatter();
        return formatter.print(getRemainingPeriod(PeriodType.yearDayTime()).toPeriod());
    }

    private PeriodType getCounterTextPeriodType() {
        return mCounterTextPeriodType == MilitaryInfo.DayTime ? PeriodType.dayTime() : PeriodType.yearMonthDayTime();
    }

    private String getRemainingDayWithString(PeriodFormatter formatter, PeriodType periodType) {
        if (isLoggedIn())
            if (getRemainingPeriod(periodType).getMillis() < 0)
                return "報告學長(`・ω・́)ゝ 您已經返陽惹!";
            else
                return formatter.print(getRemainingPeriod(periodType));
        else
            return formatter.print(new Period(DateTime.now(), mStartDateTime, PeriodType.dayTime()));
    }

    private int getDiscount() {
        return mDiscountDays;
    }

    public float getPercentage() {
        long pass = new Period(mStartDateTime, DateTime.now(), PeriodType.dayTime()).toPeriod().toStandardDuration().getMillis();
        long total = new Period(mStartDateTime, mRealLogoutDateTime, PeriodType.dayTime()).toPeriod().toStandardDuration().getMillis();
        float percent = pass * 100.0f / total;
        Range<Float> floatRange = Range.open(0.0f, 100.0f);
        return floatRange.contains(percent) ? percent : (percent < 0.0f ? 0.0f : 100.0f);
    }

    public String getUntilHundredDaysRemainingDaysWithString() {
        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .appendDays().appendSuffix("天").toFormatter();
        return formatter.print(getRemainingPeriod(PeriodType.dayTime()).minusDays(30).toPeriod());
    }

    public boolean isHundredDays() {
        return isLoggedIn() && Range.open(30, 100).contains(getRemainingPeriod(PeriodType.dayTime()).getDays());
    }

    public int getDiscountDays() {
        return mDiscountDays;
    }

    public void setDiscountDays(int value) {
        mDiscountDays = value;
        setEndTime();
    }

    public boolean isIllegalDiscountValue() {
        return isIllegalDiscountValue(getDiscount());
    }

    public boolean isIllegalDiscountValue(Integer discountDays) {
        if (mStandLogoutDateTime.minusDays(discountDays).isBefore(mStartDateTime.toInstant())){
            return false;
        }
        return true;
    }
}
