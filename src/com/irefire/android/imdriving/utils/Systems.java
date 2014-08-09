package com.irefire.android.imdriving.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Systems {
    private static final Logger l = LoggerFactory.getLogger(Systems.class.getSimpleName());

    public static final String getSystemPath() {
        StringBuilder builder = new StringBuilder(256);
        BufferedReader reader = null;
        try {
            Process p = Runtime.getRuntime().exec("sh echo $PATH");
            p.waitFor();
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            l.warn("getSystemPath get IOException:" + e.getMessage());
        } catch (InterruptedException e) {
            l.warn("getSystemPath get InterruptedException:" + e.getMessage());
        } finally {
            SafeIO.close(reader);
        }

        l.debug("getSystemPath returns " + builder.toString());
        return builder.toString();
    }

    public static List<String> getMessagePackages() {

        return null;
    }

    /**
     * 把电话号码格式化成语音比较好读的格式。
     *
     * @param str
     * @return
     */
    public static String formatPhoneNumber(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }

        while (str.startsWith("+") || str.startsWith("0")) {
            str = str.substring(1);
        }
        StringBuffer buffer = new StringBuffer(str.length() * 2);
        boolean ajointDigital = false;
        char cs[] = str.toCharArray();
        for (char c : cs) {

            // 如果前一个字符是数字，则在它之后加一个点。
            if (ajointDigital) {
                buffer.append(" ");
            }
            buffer.append(c);

            if (Character.isDigit(c)) {
                ajointDigital = true;
            } else {
                ajointDigital = false;
            }
        }
        return buffer.toString();
    }

    public static Set<String> getAccountTypes(Context context) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccounts();
        Set<String> types = new HashSet<String>();
        if (accounts != null) {
            for (Account a : accounts) {
                types.add(a.type);
            }
        }
        return types;
    }

    public static List<Account> getAccountsByType(Context context, String type) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = null;
        if (TextUtils.isEmpty(type)) {
            accounts = am.getAccounts();
        } else {
            accounts = am.getAccountsByType(type);
        }
        Set<Account> typedAccounts = new HashSet<Account>();
        if (accounts != null) {
            typedAccounts.addAll(Arrays.asList(accounts));
        }
        List<Account> list = new ArrayList<Account>(typedAccounts);
        return list;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String sha1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        byte[] sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String szImei = tm.getDeviceId(); // Requires READ_PHONE_STATE
        return szImei;
    }

    public static String getPseudoUUID() {
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits
        return m_szDevIDShort;
    }

    public static String getMacAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wm.getConnectionInfo().getMacAddress();
        return macAddress;
    }

    public static String getAndroidId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    public static String getUniqueIdentifier(Context c) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getImei(c));
        buffer.append(getMacAddress(c));
        buffer.append(getAndroidId(c));
        String m_szLongID = buffer.toString();
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = convertToHex(p_md5Data);
        m_szUniqueID = m_szUniqueID.toUpperCase();
        return m_szUniqueID;
    }

    public boolean isMessageApp(String pkg, Context context) {
        // we will check if an app is message app according to
        // http://android-developers.blogspot.com/2013/10/getting-your-sms-apps-ready-for-kitkat.html
        PackageManager pm = context.getPackageManager();
        boolean hasHeadlessSmsSendService = false;
        boolean hasSmsReceiver = false;
        boolean hasMmsReceive = false;
        boolean hasComposeSmsActivity = false;

        Intent headlessSmsSendServiceIntent = new Intent();
        headlessSmsSendServiceIntent.setAction("android.intent.action.RESPOND_VIA_MESSAGE");
        headlessSmsSendServiceIntent.addCategory("android.intent.category.DEFAULT");
        //headlessSmsSendServiceIntent.


        return true;
    }
}
