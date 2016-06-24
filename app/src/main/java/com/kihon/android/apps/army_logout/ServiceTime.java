package com.kihon.android.apps.army_logout;

/**
 * Created by kihon on 2016/06/23.
 */
public enum ServiceTime {
    ONE_YEAR("一年"),
    FOUR_YEARS("四年"),
    ONE_YEAR_FIFTH_DAYS("一年十五天"),
    FOUR_MONTHS("四個月"),
    FOUR_MONTHS_FIVE_DAYS("四個月五天"),
    SIX_MONTHS("六個月"),
    THREE_YEARS("三年"),
    ONE_YEAR_SIX_MONTHS("一年六個月"),
    TEN_MONTHS("十個月");
//        ,CUSTOM("自訂");

    private final String mDisplayText;

    ServiceTime(String displayText) {
        mDisplayText = displayText;
    }

    public String getDisplayText() {
        return mDisplayText;
    }
}
