package com.irefire.android.imdriving;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.view.View;
import android.widget.Button;

/**
 * Created by lan on 8/21/14.
 */
public class TranslateActivity extends Activity implements View.OnClickListener{
    private Button mContactUsButton = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        mContactUsButton = (Button)findViewById(R.id.contact_us_button);
        mContactUsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.contact_us_button) {
            ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this);
            builder.setType("message/rfc822");
            builder.addEmailTo("rorbbin@gmail.com");
            builder.setSubject("I want to help to translate");
            builder.setChooserTitle("Contact developer");
            builder.startChooser();
        }
    }
}