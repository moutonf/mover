package com.mover.vehicle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * A login screen that offers login via email/password.
 */
//TODO: Add sign up functionality
public class LoginActivity extends AppCompatActivity {

    private EditText userNameText;
    private EditText passwordText;
    String postResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mover.vehicle.R.layout.activity_login);

        userNameText = (EditText) findViewById(com.mover.vehicle.R.id.userNameText);
        passwordText = (EditText) findViewById(com.mover.vehicle.R.id.passwordText);

    }

    //TODO: Authenticate User
    public void attemptLogin(View view) {

        // Store values at the time of the login attempt.
        String email = userNameText.getText().toString();
        String password = passwordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            passwordText.setError(Html.fromHtml("<font color='red'>Invalid Password</font>"));
            focusView = passwordText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
           userNameText.setError(Html.fromHtml("<font color='red'>This Field is Required</font>"));
            focusView = userNameText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            userNameText.setError(Html.fromHtml("<font color='red'>Invalid Email</font>"));
            focusView = userNameText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //All fields filled in correctly, try login

            //All fields filled out fine, try sign up
            try {
                if(loginRequest(email, password)){
                    //Go to main activity
                    Intent k = new Intent(this, MainActivity.class);
                    startActivity(k);
                }
            } catch (JSONException e) {
                Log.e("LoginActivity",  "Message: " + e.getMessage() +"\nCause: " + e.getCause());
            }
        }
    }

    public Boolean loginRequest(String user, String password) throws JSONException {
        JSONObject jsonResponse;

        postRequest asyncTask = (postRequest) new postRequest(new postRequest.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                Context context = getApplicationContext();

                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, output, duration);
                toast.show();
            }
        }, "email=" + user + "&password=" + password).execute("http://139.162.178.79:4000/login");

        try {
            postResponse = asyncTask.get();
            jsonResponse = new JSONObject(postResponse);
        } catch (InterruptedException e) {
            Log.e("LoginActivity",  "Message: " + e.getMessage() +"\nCause: " + e.getCause());
            return false;
        } catch (ExecutionException e) {
            Log.e("LoginActivity",  "Message: " + e.getMessage() +"\nCause: " + e.getCause());
            return false;
        }

        if(jsonResponse.getString("auth").equals("fail")){
            return false;
        }
        else {
            Mover.setUser(jsonResponse.getInt("id"));
            return true;
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    public void signup(View view) {
        Intent k = new Intent(this, signupActivity.class);
        startActivity(k);
    }
}

