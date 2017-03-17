package tw.kihon.armylogout;

import java.util.Locale;

/**
 * Created by kihon on 2016/06/24.
 */
public class CustomPeriod {

    private final int year;
    private final int monthOfYear;
    private final int dayOfMonth;

    public CustomPeriod(int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;
    }

    public int getYear() {
        return year;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    @Override
    public String toString() {
        return String.format(Locale.TAIWAN, "%dY%dM%dD", year, monthOfYear, dayOfMonth);
    }

    public String toDisplayString() {
        return (year == 0 ? "" : year + "年") +
                (monthOfYear == 0 ? "" : monthOfYear + "個月") +
                (dayOfMonth == 0 ? "" : dayOfMonth + "天");
    }
}
