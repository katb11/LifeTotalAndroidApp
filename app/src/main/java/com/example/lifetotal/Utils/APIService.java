package com.example.lifetotal.Utils;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class APIService extends AsyncTask<String, String, String> {

    private AsyncResponse delegate;
    private String method;

    public APIService(AsyncResponse delegate){
        this.delegate = delegate;
    }

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        String data = params[1];
        String responseStorage = "connection failed";
        OutputStream out;

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setConnectTimeout(5000);

            if (method.equals("POST")) {
                out = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(data);
                writer.flush();
                writer.close();
                out.close();
            }

            //getting the response from the server
            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            ByteArrayOutputStream byteArrayBuffer = new ByteArrayOutputStream(50);

            int intResponse = urlConnection.getResponseCode();

            while ((intResponse = bufferedReader.read()) != -1) {
                byteArrayBuffer.write(intResponse);
            }

            responseStorage = byteArrayBuffer.toString();

        } catch (Exception aException) {
            aException.printStackTrace();
        } finally {
            return responseStorage;
        }
    }
}