package com.example.mystuff.lick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class LoginOrSignupActivity extends Activity {

    public static final String TYPE = "type";
    public static final String LOGIN = "Log In";
    public static final String SIGNUP = "Sign Up";

    protected Button mLoginButton;
    protected Button mSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_signup);

        mLoginButton = (Button) findViewById(R.id.button1);
        mSignupButton = (Button) findViewById(R.id.button2);
		
		/*
		 * Check for cached user using ParseUser.getCurrentUser()
		 */
        // Start Parse Init
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, //////////////////KEYS\\\\\\\\\\\\\\\\\\\\\);

        ParseACL defaultACL = new ParseACL();

        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();

        // End Parse Init
        if (ParseUser.getCurrentUser() != null) {
            Intent intent = new Intent(this, Record.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else {
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginOrSignupActivity.this, AuthenticateActivity.class);
                    intent.putExtra(TYPE, LOGIN);
                    startActivity(intent);
                }
            });

            mSignupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LoginOrSignupActivity.this, AuthenticateActivity.class);
                    intent.putExtra(TYPE, SIGNUP);
                    startActivity(intent);
                }
            });
        }
    }
}