package com.mover.vehicle;

import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LUKE on 2016/06/19.
 * Class for posting accidents to server
 */
public class postRequest extends AsyncTask<String, String, String> {

    private String urlParameters;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public postRequest(AsyncResponse delegate, String urlParameters) {
        this.delegate = delegate;
        this.urlParameters = urlParameters;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected String doInBackground(String... params) {

        String urlString = params[0]; // URL to call

        String resultToDisplay = "";

        InputStream in = null;
        try {

            byte[] postData       = urlParameters.getBytes();
            int postDataLength = postData.length;
            URL url            = new URL(urlString);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput( true );
            urlConnection.setInstanceFollowRedirects( false );
            urlConnection.setRequestMethod( "POST" );
            urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty( "charset", "utf-8");
            urlConnection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
            urlConnection.setUseCaches( false );
            urlConnection.getOutputStream().write(postData);

            in = new BufferedInputStream(urlConnection.getInputStream());


        } catch (Exception e) {

            System.out.println(e);

            return "" + e;

        }

        try {
            resultToDisplay = IOUtils.toString(in, "UTF-8");
            //to [convert][1] byte stream to a string
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(resultToDisplay);
        return resultToDisplay;
    }


    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

}