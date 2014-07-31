package com.irefire.android.imdriving;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.irefire.android.imdriving.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.Inflater;

/**
 * Created by lan on 7/31/14.
 */
public class FilterAppActivity extends ListActivity implements View.OnClickListener{

    private static final Logger l = LoggerFactory.getLogger(FilterAppActivity.class.getSimpleName());

    private List<AppItem> mAppItems = null;
    private Map<String, Boolean> mReadAppMap = null;
    private Map<String, Boolean> mSelectedMap = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.filter_app_main);
        Set<AppItem> apps = this.getAllApps();
        mAppItems = new ArrayList<AppItem>(apps);
        mReadAppMap = new HashMap<String, Boolean>();

        List<String> saved = this.getReadApps();
        for(String p: saved) {
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
        ViewTag tag = (ViewTag)v.getTag(R.id.app_filter_item_id);
        if(tag != null) {
            boolean checked = tag.checkbox.isChecked();
            if(!checked) {
                mSelectedMap.put(tag.pkg.toString(), Boolean.TRUE);
            }else {
                mSelectedMap.remove(tag.pkg.toString());
            }
            tag.checkbox.setChecked(!checked);
        }
    }

    private Set<AppItem> getAllApps() {
        PackageManager pm = this.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);
        Set<AppItem> items = new HashSet<AppItem>();
        for(ResolveInfo ai: list) {
            AppItem item = new AppItem();
            item.pkg = ai.activityInfo.packageName;
            item.name = ai.activityInfo.loadLabel(pm);
            item.icon = ai.activityInfo.loadIcon(pm);
            items.add(item);
        }
        return items;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.app_filter_select_all_checkbox) {
            boolean checked = ((CheckBox)v).isChecked();
            mSelectedMap.clear();
            if(checked) {
                for(AppItem ai: mAppItems) {
                    mSelectedMap.put(ai.pkg.toString(), Boolean.TRUE);
                }
            }else {
                mSelectedMap.putAll(mReadAppMap);
            }
            this.setListAdapter(new AppItemAdapter(mAppItems, this, mSelectedMap));
        }else if(id == R.id.app_filter_cancel) {
            finish();
        }else if(id == R.id.app_filter_save) {
            this.saveReadApps(mSelectedMap);
            finish();
        }
    }

    private List<String> getReadApps() {
        try {
            InputStream in = this.openFileInput(Constants.READ_APP_SAVED_FILE_NAME);
            byte[] data = new byte[in.available()];
            in.read(data);
            String content = new String(data);
            String[] pkgs = content.split(";");
            return Arrays.asList(pkgs);
        }  catch (FileNotFoundException e) {
            l.warn("Error when read read app:" + e.getMessage());
        }catch(IOException e) {
            l.warn("Error when read read app:" + e.getMessage());
        }
        return Collections.emptyList();
    }

    private boolean saveReadApps(Map<String, Boolean> maps) {
        StringBuilder builder = new StringBuilder(2048);
        for(String pkg: maps.keySet()) {
            if(maps.get(pkg)) {
                builder.append(pkg);
                builder.append(";");
            }
        }
        boolean result = true;
        try {
            OutputStream out = this.openFileOutput(Constants.READ_APP_SAVED_FILE_NAME, Context.MODE_PRIVATE);
            out.write(builder.toString().getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            l.warn("Error when save read app:" + e.getMessage());
            result = false;
        }catch(IOException e) {
            l.warn("Error when save read app:" + e.getMessage());
            result = false;
        }
        return result;
    }

    private static final class AppItem implements Comparable<AppItem>{
        public CharSequence pkg;
        public CharSequence name;
        public Drawable icon;

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }else if(o instanceof AppItem) {
                AppItem ai = (AppItem)o;
                return TextUtils.equals(pkg, ai.pkg);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + name.hashCode() + pkg.hashCode();
        }

        @Override
        public int compareTo(AppItem another) {
            return name.toString().compareTo(another.name.toString());
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
            return mApps == null? 0: mApps.size();
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
            if(convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.filter_app_item, null);

            }
            AppItem ai = mApps.get(position);
            ViewTag tag = (ViewTag)convertView.getTag(R.id.app_filter_item_id);

            if(tag == null) {
                tag = new ViewTag();
                tag.icon = (ImageView)convertView.findViewById(R.id.app_filter_icon);
                tag.checkbox = (CheckBox)convertView.findViewById(R.id.app_filter_checkbox);
                tag.name = (TextView)convertView.findViewById(R.id.app_filter_name);
                convertView.setTag(R.id.app_filter_item_id,tag);
            }

            tag.pkg = ai.pkg;

            tag.fill(ai, checkMap.get(ai.pkg) == null? false: true);
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