package tw.kihon.armylogout;

import com.google.android.gms.analytics.HitBuilders;

import com.afollestad.materialdialogs.color.ColorChooserDialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import tw.kihon.armylogout.settings.SettingsUtils;

/**
 * Created by kihon on 2016/06/19.
 */
public class WidgetSettingsActivity extends BaseAppCompatActivity
        implements ItemClickSupport.OnItemClickListener, ColorChooserDialog.ColorCallback {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.until_logout_title)
    TextView mUntilLogoutTitle;
    @BindView(R.id.until_logout_text)
    TextView mUntilLogoutText;
    @BindView(R.id.logout_percent_title)
    TextView mLogoutPercentTitle;
    @BindView(R.id.login_percent_text)
    TextView mLoginPercentText;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.widgetMainLayout)
    FrameLayout mWidgetMainLayout;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private RecyclerView.Adapter mSettingsAdapter;
    private Item mSelectedItem;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        final Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.ic_close_black_24dp);
        upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        MilitaryInfo militaryInfo = MilitaryInfo.parse(SettingsUtils.getMilitaryInfo());
        ServiceUtil serviceUtil = new ServiceUtil(militaryInfo);

        mUntilLogoutTitle.setText(serviceUtil.isLoggedIn() ? "距離返陽還剩下" : "距離入伍還剩下");
        mUntilLogoutText.setText(serviceUtil.getRemainingDayWithString());
        mLoginPercentText.setText(String.format(Locale.TAIWAN, "%.2f%%", serviceUtil.getPercentage()));
        mProgressBar.setProgress((int) serviceUtil.getPercentage());

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(this);
        mSettingsAdapter = new SettingsAdapter(this, Item.values());
        mRecyclerView.setAdapter(mSettingsAdapter);

        initColors();

        AppApplication.getInstance().getDefaultTracker().setScreenName("widget_settings");
        AppApplication.getInstance().getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void initColors() {
        mWidgetMainLayout.setBackgroundColor(SettingsUtils.getWidgetBackgroundColor());
        mUntilLogoutTitle.setTextColor(SettingsUtils.getWidgetTitleColor());
        mLogoutPercentTitle.setTextColor(SettingsUtils.getWidgetTitleColor());
        mUntilLogoutText.setTextColor(SettingsUtils.getWidgetContentColor());
        mLoginPercentText.setTextColor(SettingsUtils.getWidgetContentColor());
    }

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        mSelectedItem = Item.values()[position];
        ColorChooserDialog.Builder builder = new ColorChooserDialog.Builder(this, R.string.color_palette);
        switch (mSelectedItem) {
            case Background:
                builder.preselect(SettingsUtils.getWidgetBackgroundColor());
                break;
            case Title:
                builder.preselect(SettingsUtils.getWidgetTitleColor());
                break;
            case Content:
                builder.preselect(SettingsUtils.getWidgetContentColor());
                break;
        }
        builder.accentMode(true)
                .dynamicButtonColor(true)
                .show();
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (mSelectedItem) {
            case Background:
                SettingsUtils.setWidgetBackgroundColor(selectedColor);
                break;
            case Title:
                SettingsUtils.setWidgetTitleColor(selectedColor);
                break;
            case Content:
                SettingsUtils.setWidgetContentColor(selectedColor);
                break;
        }
        initColors();
        mSettingsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }

    private enum Item {

        Background("背景"),
        Title("標題"),
        Content("內容文字");

        private final String text;

        Item(String text) {
            this.text = text;
        }
    }

    class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ItemViewHolder> {

        private final Item[] mItems;
        private final LayoutInflater mLayoutInflater;

        SettingsAdapter(Context context, Item[] items) {
            mLayoutInflater = LayoutInflater.from(context);
            mItems = items;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ItemViewHolder(mLayoutInflater.inflate(R.layout.list_item_main_two_line, parent, false));
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.title.setText(mItems[position].text);
            switch (mItems[position]) {
                case Background:
                    holder.icon.setBackgroundColor(SettingsUtils.getWidgetBackgroundColor());
                    break;
                case Title:
                    holder.icon.setBackgroundColor(SettingsUtils.getWidgetTitleColor());
                    break;
                case Content:
                    holder.icon.setBackgroundColor(SettingsUtils.getWidgetContentColor());
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mItems.length;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.imageView)
            ImageView icon;
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.subtitle)
            TextView subtitle;
            @BindView(R.id.handle)
            ImageView handleView;

            ItemViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                subtitle.setVisibility(View.GONE);
                handleView.setVisibility(View.GONE);
            }
        }
    }
}
