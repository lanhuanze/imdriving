package com.irefire.android.imdriving.parse;

import android.accounts.Account;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.irefire.android.imdriving.utils.AppSettings;
import com.irefire.android.imdriving.utils.Systems;
import com.irefire.http.HttpGetWithEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 8/8/14.
 */
public class AccountInfo implements ParseOp {
    private static final Logger l = LoggerFactory.getLogger(AccountInfo.class.getSimpleName());
    public String id;
    public String emailAddress;
    public String deviceId;
    public long fistUsedTime;
    public List<Account> accountList = new ArrayList<Account>();

    public AccountInfo(String email, String devid, List<Account> accounts) {
        emailAddress = email;
        deviceId = devid;
        try {
            id = Systems.sha1(emailAddress + deviceId);
        } catch (NoSuchAlgorithmException e) {
            l.error("AccountInfo throw error:" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            l.error("AccountInfo throw error:" + e.getMessage());
        } finally {
            if (TextUtils.isEmpty(id)) {
                try {
                    id = URLEncoder.encode(deviceId, "ISO-8859-1");
                } catch (UnsupportedEncodingException e) {
                    l.error("AccountInfo throw error:" + e.getMessage());
                }
            }
        }
        accountList.addAll(accounts);
        AppSettings settings = AppSettings.getInstance();
        this.fistUsedTime = settings.getFirstUseTime();
    }

    @Override
    public String getClassUrl() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(CLASS_URL);
        buffer.append(AccountInfo.class.getSimpleName().toLowerCase());
        l.debug("ClassUrl:" + buffer.toString());
        return buffer.toString();
    }


    @Override
    public String getObjectUrl() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(CLASS_URL);
        buffer.append(AccountInfo.class.getSimpleName().toLowerCase());
        buffer.append("/");
        buffer.append(id);
        return buffer.toString();
    }

    public HttpGet getHttpGet() {
        HttpGet get = new HttpGet(getClassUrl());
        get.setHeader(APP_ID_HEADER_NAME, APP_ID);
        get.setHeader(REST_API_HEADER_NAME, REST_API_KEY);
        return get;
    }

    public HttpPut getHttpPut(String objectId) {
        HttpPut put = new HttpPut(getClassUrl() + "/" + objectId);
        put.setHeader(REST_API_HEADER_NAME, REST_API_KEY);
        put.setHeader("Content-Type", "application/json");
        String body = new Gson().toJson(this);
        l.debug("post body:" + body);
        try {
            put.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException e) {
            l.error("getHttpPost setEntity error:" + e.getMessage());
        }
        return put;
    }

    public HttpGetWithEntity getHttpGetWithQuery() {
        HttpGetWithEntity get = new HttpGetWithEntity(getClassUrl());
        get.setHeader(APP_ID_HEADER_NAME, APP_ID);
        get.setHeader(REST_API_HEADER_NAME, REST_API_KEY);
        try {
            get.setEntity(new StringEntity(getQueryString()));
        } catch (UnsupportedEncodingException e) {
            l.error("getHttpGetWithQuery error:" + e.getMessage());
        }
        return get;
    }

    public HttpPost getHttpPost() {
        HttpPost post = new HttpPost(getClassUrl());
        post.setHeader(APP_ID_HEADER_NAME, APP_ID);
        post.setHeader(REST_API_HEADER_NAME, REST_API_KEY);
        post.setHeader("Content-Type", "application/json");
        String body = new Gson().toJson(this);
        l.debug("post body:" + body);
        try {
            post.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException e) {
            l.error("getHttpPost setEntity error:" + e.getMessage());
        }
        return post;
    }

    public String getQueryString() {
        StringBuilder buffer = new StringBuilder("where={\"id\":\"");
        buffer.append(id);
        buffer.append("\"}");
        l.debug("getQueryString:" + buffer.toString());
        String encodedString = "";
        try {
            encodedString = URLEncoder.encode(buffer.toString(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedString;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getFistUsedTime() {
        return fistUsedTime;
    }

    public void setFistUsedTime(long fistUsedTime) {
        this.fistUsedTime = fistUsedTime;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }
}
