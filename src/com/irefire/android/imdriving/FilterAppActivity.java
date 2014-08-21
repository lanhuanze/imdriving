package com.irefire.android.imdriving;

import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Systems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.irefire.android.imdriving.utils.Systems.AppItem;


/**
 * Created by lan on 7/31/14.
 */
public class FilterAppActivity extends ListActivity implements View.OnClickListener {

    private static final Logger l = LoggerFactory.getLogger(FilterAppActivity.class.getSimpleName());

    private List<AppItem> mAppItems = null;
    private Map<String, Boolean> mReadAppMap = null;
    private Map<String, Boolean> mSelectedMap = null;
    private AppSettings mSettings = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.filter_app_main);
        Set<AppItem> apps = Systems.getAllApps(this);
        mAppItems = new ArrayList<AppItem>(apps);
        mReadAppMap = new HashMap<String, Boolean>();
        mSettings = AppSettings.getInstance();

        List<String> saved = mSettings.getAllowedPackages();
        for (String p : saved) {
            mReadAppMap.put(p, Boolean.TRUE);
        }

        mSelectedMap = new HashMap<String, Boolean>();
        mSelectedMap.putAll(mReadAppMap);
        Collections.sort(mAppItems);
        AppItemAdapter adapter = new AppItemAdapter(mAppItems, this, mSelectedMap);
        this.setListAdapter(adapter);
        findViewById(R.id.app_filter_select_all_checkbox).setOnClickListener(this);
        findViewById(R.id.app_filter_save).setOnClickListener(this);
        findViewById(R.id.app_filter_cancel).setOnClickListener(this);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
        ViewTag tag = (ViewTag) v.getTag(R.id.app_filter_item_id);
        if (tag != null) {
            boolean checked = tag.checkbox.isChecked();
            if (!checked) {
                mSelectedMap.put(tag.pkg.toString(), Boolean.TRUE);
            } else {
                mSelectedMap.remove(tag.pkg.toString());
            }
            tag.checkbox.setChecked(!checked);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.app_filter_select_all_checkbox) {
            boolean checked = ((CheckBox) v).isChecked();
            mSelectedMap.clear();
            if (checked) {
                for (AppItem ai : mAppItems) {
                    mSelectedMap.put(ai.pkg.toString(), Boolean.TRUE);
                }
            } else {
                mSelectedMap.putAll(mReadAppMap);
            }
            this.setListAdapter(new AppItemAdapter(mAppItems, this, mSelectedMap));
        } else if (id == R.id.app_filter_cancel) {
            finish();
        } else if (id == R.id.app_filter_save) {
            mSettings.saveReadApps(mSelectedMap);
            mSettings.updateAllowedPackages(mSelectedMap);
            finish();
        }
    }


    private static class AppItemAdapter implements ListAdapter {
        private List<AppItem> mApps;
        private Context mContext;
        private Map<String, Boolean> checkMap;

        public AppItemAdapter(List<AppItem> apps, Context c, Map<String, Boolean> checkMap) {
            mApps = apps;
            mContext = c;
            this.checkMap = checkMap;
        }

        @Override
        public boolean isEmpty() {
            return mApps == null || mApps.isEmpty();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return mApps == null ? 0 : mApps.size();
        }

        @Override
        public Object getItem(int position) {
            return mApps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.filter_app_item, null);

            }
            AppItem ai = mApps.get(position);
            ViewTag tag = (ViewTag) convertView.getTag(R.id.app_filter_item_id);

            if (tag == null) {
                tag = new ViewTag();
                tag.icon = (ImageView) convertView.findViewById(R.id.app_filter_icon);
                tag.checkbox = (CheckBox) convertView.findViewById(R.id.app_filter_checkbox);
                tag.name = (TextView) convertView.findViewById(R.id.app_filter_name);
                convertView.setTag(R.id.app_filter_item_id, tag);
            }

            tag.pkg = ai.pkg;

            tag.fill(ai, checkMap.get(ai.pkg) == null ? false : true);
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }
    }

    private static class ViewTag {
        public CheckBox checkbox;
        public TextView name;
        public ImageView icon;
        public CharSequence pkg;

        public void fill(AppItem ai, boolean checked) {
            name.setText(ai.name);
            icon.setImageDrawable(ai.icon);
            checkbox.setChecked(checked);
        }

    }
}