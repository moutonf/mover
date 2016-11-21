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
 * Created by LUKE on 2016/07/10.
 */
public class signupActivity extends AppCompatActivity {

    EditText nameText;
    EditText surnameText;
    EditText emailText;
    EditText passwordText;
    EditText confirmPasswordText;
    String name;
    String surname;
    String email;
    String password;
    String confirmPassword;
    String postResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mover.vehicle.R.layout.activity_signup);

        nameText = (EditText) findViewById(com.mover.vehicle.R.id.nameText);
        surnameText = (EditText) findViewById(com.mover.vehicle.R.id.surnameText);
        emailText = (EditText) findViewById(com.mover.vehicle.R.id.emailText);
        passwordText = (EditText) findViewById(com.mover.vehicle.R.id.passwordText);
        confirmPasswordText = (EditText) findViewById(com.mover.vehicle.R.id.confirmPasswordText);


    }

    //TODO: validate fields and add user to db
    public void attemptSignup(View view) {

        name = nameText.getText().toString();
        surname = surnameText.getText().toString();
        email = emailText.getText().toString();
        password = passwordText.getText().toString();
        confirmPassword = confirmPasswordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check if passwords match
        if (!password.equals(confirmPassword)) {
            confirmPasswordText.setError(Html.fromHtml("<font color='red'>Non matching passwords</font>"));
            focusView = passwordText;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password)) {
            passwordText.setError(Html.fromHtml("<font color='red'>Invalid Password</font>"));
            focusView = passwordText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailText.setError(Html.fromHtml("<font color='red'>This Field is Required</font>"));
            focusView = emailText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailText.setError(Html.fromHtml("<font color='red'>Invalid Email</font>"));
            focusView = emailText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //All fields filled out fine, try sign up
            try {
                if(signupRequest(name, surname, email, password)){
                    //Go to main activity
                    Intent k = new Intent(this, MainActivity.class);
                    startActivity(k);
                }
            } catch (JSONException e) {
                Log.e("SignUpActivity",  "Message: " + e.getMessage() +"\nCause: " + e.getCause());
            }
        }
    }


    public Boolean signupRequest(String name, String surname, String user, String password) throws JSONException {
        JSONObject jsonResponse;
        postRequest asyncTask = (postRequest) new postRequest(new postRequest.AsyncResponse() {

            @Override
            public void processFinish(String output) {

                Context context = getApplicationContext();

                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, output, duration);
                toast.show();
            }
        }, "name=" + name + "&surname=" + surname + "&email=" + user + "&password=" + password + "&password_confirm=" + password ).execute("http://139.162.178.79:4000/register");

        try {
            postResponse = asyncTask.get();
            jsonResponse = new JSONObject(postResponse);
        } catch (InterruptedException e) {
            Log.e("SignUpActivity",  "Message: " + e.getMessage() +"\nCause: " + e.getCause());
            return false;
        } catch (ExecutionException e) {
            Log.e("SignUpActivity",  "Message: " + e.getMessage() +"\nCause: " + e.getCause());
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

}
