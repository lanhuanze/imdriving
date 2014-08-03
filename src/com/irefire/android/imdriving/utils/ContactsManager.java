package com.irefire.android.imdriving.utils;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.irefire.android.imdriving.App;
import static android.provider.ContactsContract.CommonDataKinds.Phone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lan on 8/2/14.
 */
public class ContactsManager {
    public static final ContactsManager getInstance() {
        return Holder._INST;
    }

    private static final class Holder {
        public static final ContactsManager _INST = new ContactsManager();
    }

    private Map<String, String> contacts = new ConcurrentHashMap<String, String>();

    private ContactsManager() {
        updateContacts();
        App.getStaticContext().getContentResolver().registerContentObserver(Phone.CONTENT_URI, true, mContactsObserver);
    }

    private ContentObserver mContactsObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            //super.onChange(selfChange, uri);
            updateContacts();
        }
    };

    private void updateContacts() {
        new Thread() {
            public void run() {
                contacts.clear();
                Context c = App.getStaticContext();
                Cursor cursor = c.getContentResolver().query(Phone.CONTENT_URI,
                        new String[] {Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER}, null, null,  ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                if(cursor != null) {
                    int idIndex = cursor.getColumnIndex(Phone._ID);
                    int nameIndex = cursor.getColumnIndex(Phone.DISPLAY_NAME);
                    int numberIndex = cursor.getColumnIndex(Phone.NUMBER);
                    while(cursor.moveToNext()) {
                        contacts.put(cursor.getString(numberIndex), cursor.getString(nameIndex));
                    }
                }
            }
        }.start();
    }


    public String getName(String phoneNumber) {
        if(TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        for(String s: contacts.keySet()) {
            if(PhoneNumberUtils.compare(s, phoneNumber)) {
                return contacts.get(s);
            }
        }
        return "";
    }
}
