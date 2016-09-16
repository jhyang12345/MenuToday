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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends ActionBarActivity {

    TextView myTextView;
    Button cafeteriaSelector;
    String[] names;
    String[] links;
    HashMap<String, String> namelink = new HashMap<String, String>();
    String originallink = "http://www.hanyang.ac.kr/web/www/-248?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=13&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8";


//in your OnCreate() method


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myTextView = (TextView)findViewById(R.id.textView1);

        myTextView.setText("");

        cafeteriaSelector = (Button) findViewById(R.id.selector);
        cafeteriaSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDialog();
            }
        });


        new RetrieveURL().execute();

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

    private void OpenDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.customdialog, null);
        dialog.setContentView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
        lv.setBackgroundResource(R.drawable.customshape);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cafeterias,names);
        lv.setAdapter(adapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Toast.makeText(MainActivity.this, "this is my Toast message!!! =)",
                        Toast.LENGTH_LONG).show();
                System.out.println("Something clicked!");
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void GetCafeterias(Elements elements) {
        cafeteriaSelector.setAlpha(1);
        cafeteriaSelector.setText(elements.select(".active").text());
        //cafeteriaSelector.setText(elements.first().text());
        names = new String[elements.size()];
        links = new String[elements.size()];
        for(int i = 0; i < elements.size(); ++i) {
            String buffer = elements.get(i).html();
            Document doc = Jsoup.parse(buffer);
            names[i] = doc.text();
            links[i] = doc.select("a").attr("href");
            namelink.put(names[i], links[i]);
            System.out.println(links[i]);
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
                doc = Jsoup.connect(originallink).get();

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
