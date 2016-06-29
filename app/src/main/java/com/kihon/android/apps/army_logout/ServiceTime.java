package com.kihon.android.apps.army_logout;

/**
 * Created by kihon on 2016/06/23.
 */
public enum ServiceTime {
    ONE_YEAR("1年"),
    FOUR_YEARS("4年"),
    ONE_YEAR_FIFTH_DAYS("1年15天"),
    FOUR_MONTHS("4個月"),
    FOUR_MONTHS_FIVE_DAYS("4個月5天"),
    SIX_MONTHS("6個月"),
    THREE_YEARS("3年"),
    ONE_YEAR_SIX_MONTHS("1年6個月"),
    TEN_MONTHS("10個月"),
    CUSTOM("自訂");

    private String mDisplayText;

    ServiceTime(String displayText) {
        mDisplayText = displayText;
    }

    public String getDisplayText() {
        return mDisplayText;
    }

    public ServiceTime setDisplayText(String displayText) {
        mDisplayText = displayText;
        return this;
    }
}
