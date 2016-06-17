package com.kihon.android.apps.army_logout;

/**
 * Created by kihon on 2016/06/17.
 */
public enum InfoItem {

    LoginDate(R.drawable.ic_date_range_black_24dp, "入伍日期"),
    Period(R.drawable.ic_access_time_black_24dp, "役期"),
    LogoutDate(R.drawable.ic_directions_run_black_24dp, "退伍日期"),
    Discount(R.drawable.ic_all_inclusive_black_24px, "折抵"),
    CounterTimer(R.drawable.ic_time_countdown_black_24dp, "距離退伍剩下"),
    CounterProgressbar(R.drawable.ic_school_black_24dp, "退伍令下載進度");

    private final int mImageRes;
    private final String mTitle;

    InfoItem(int imageRes, String title) {
        mImageRes = imageRes;
        mTitle = title;
    }

    public int getImageRes() {
        return mImageRes;
    }

    public String getTitle() {
        return mTitle;
    }
}
