package com.example.user.menutoday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends ActionBarActivity {

    TextView myTextView;
    Button cafeteriaSelector;

//in your OnCreate() method


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = (TextView)findViewById(R.id.textView1);

        myTextView.setText("");

        cafeteriaSelector = (Button) findViewById(R.id.selector);

        new RetrieveURL().execute();
/*
        String names[] ={"A","B","C","D"};
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.customdialog, null);
        alertDialog.setView(convertView);
       // alertDialog.setTitle("List");
        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
        lv.setBackgroundResource(R.drawable.customshape);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cafeterias,names);
        lv.setAdapter(adapter);
*/
        String names[] ={"A","B","C","D"};
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.customdialog, null);
        dialog.setContentView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
        lv.setBackgroundResource(R.drawable.customshape);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cafeterias,names);
        lv.setAdapter(adapter);


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    //    dialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void UpdateText(String val) {
        myTextView.setText(val);

        cafeteriaSelector.setAlpha(1);
    }

    private void GetCafeterias(Elements elements) {
        cafeteriaSelector.setAlpha(1);
        cafeteriaSelector.setText(elements.first().text());
        for(Element element: elements) {

        }
    }

    private class RetrieveURL extends AsyncTask<String, Void, Void> {

        private Exception exception;

        String retvalue;
        Elements cafeterias;

        @Override
        protected Void doInBackground(String... urls) {
            URL url = null;
            try {
                url = new URL("http://www.hanyang.ac.kr/web/www/-253");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection urlConnection;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder result = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null) {
                    result.append('\n' + line);

                }

                urlConnection.disconnect();
            } catch(IOException e) {
                System.out.println("Error Found!");
            } finally {

            }


            Document doc = null;
            try {
                doc = Jsoup.connect("http://www.hanyang.ac.kr/web/www/-253").get();

                cafeterias = doc.select(".tab-7 > li");


                retvalue = cafeterias.html();


            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;

        }

        @Override
        protected void onPostExecute(Void something) {
            GetCafeterias(cafeterias);
            //UpdateText(retvalue);
        }
    }

}
