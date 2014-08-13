package com.irefire.android.imdriving.parse;

import android.accounts.Account;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Systems;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by lan on 8/13/14.
 */
public class UpdateParseService extends IntentService {

    private static final Logger l = LoggerFactory.getLogger(UpdateParseService.class.getSimpleName());

    public static final String ACTION_GET_ACCOUNT_INFO = "ACTION_GET_ACCOUNT_INFO";
    public static final String ACTION_UPDATE_ACCOUNT_INFO = "ACTION_UPDATE_ACCOUNT_INFO";
    public static final String ACTION_QUERY_ACCOUNT_INFO = "ACTION_QUERY_ACCOUNT_INFO";

    private HttpClient mHttpClient = null;
    private AppSettings mAppSettings = null;
    private AccountInfo mAccountInfo = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mHttpClient = new DefaultHttpClient();
        mAppSettings = AppSettings.getInstance();
        List<Account> accounts = Systems.getAccountsByType(this, null);
        if(accounts == null || accounts.size() < 1) {
            l.warn("We need Google account to TTS.");
            return;
        }
        String emailAddress = "";
        for(Account a: accounts) {
            if(a.type.equals("com.google")) {
                emailAddress = a.name;
                break;
            }
        }
        String deviceId = Systems.getUniqueIdentifier(this);
        l.debug("emailAddress:" + emailAddress);
        l.debug("deviceId:" + deviceId);
        mAccountInfo = new AccountInfo(emailAddress, deviceId, accounts);
    }

    public UpdateParseService() {
        super(UpdateParseService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent == null) {
            l.warn("Intent is null");
        }
        String action = intent.getAction();
        if(ACTION_GET_ACCOUNT_INFO.equals(action)) {

        }else if(ACTION_UPDATE_ACCOUNT_INFO.equals(action)) {

        }else if(ACTION_QUERY_ACCOUNT_INFO.equals(action)) {
            HttpUriRequest get = mAccountInfo.getHttpGetWithQuery();
            try {
                HttpResponse resp = mHttpClient.execute(get);
                l.debug("Get response status code:" + resp.getStatusLine().getStatusCode());
                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String body = EntityUtils.toString(resp.getEntity());
                    l.debug("Get response body:" + body);
                    try {
                        JSONObject result = new JSONObject(body);
                        JSONArray arrays = result.getJSONArray("results");
                        if (arrays == null || arrays.length() < 1) {
                            HttpPost post = mAccountInfo.getHttpPost();

                            try {
                                resp = mHttpClient.execute(post);
                                l.debug("Post response status code:" + resp.getStatusLine().getStatusCode());
                                if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                                    body = EntityUtils.toString(resp.getEntity());
                                    l.debug("Post response body:" + body);
                                    JSONObject created = new JSONObject(body);
                                    mAppSettings.setAccountInfoId(created.optString("objectId"));
                                    mAppSettings.setFirstUseTime(System.currentTimeMillis());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            JSONObject obj = arrays.getJSONObject(0);
                            String accountInfoId = obj.optString("objectId");
                            if (!TextUtils.isEmpty(accountInfoId)) {
                                mAppSettings.setAccountInfoId(accountInfoId);
                            }
                            AccountInfo info = new Gson().fromJson(obj.toString(), AccountInfo.class);
                            l.debug("Retrieve account info from net:" + info.toString());
                            mAppSettings.setFirstUseTime(info.getFistUsedTime());
                        }
                    } catch (JSONException e) {
                        l.error("JSON error:" + e.getMessage());
                    }
                }
            }catch(Exception e) {
                l.warn("Error got exception:" + e.getMessage());
            }
        }
    }
}
