package com.example.user.menutoday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

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
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends ActionBarActivity {

    TextView myTextView;
    Button cafeteriaSelector;
    String[] names;
    String[] links;

    Meal[] meals;
    String[] dishes;
    int[] prices;



    HashMap<String, String> namelink = new HashMap<String, String>();
    boolean open = true;

    String originallink;//"http://www.hanyang.ac.kr/web/www/-248?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=13&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8";
    String cafeterianame;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


//in your OnCreate() method


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cafeteriaSelector = (Button) findViewById(R.id.selector);
        cafeteriaSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDialog();
            }
        });

        originallink  = getCafeteria(this.getApplicationContext());

        new RetrieveURL().execute();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.customdialog, null);
        dialog.setContentView(convertView);
        ListView lv = (ListView) convertView.findViewById(R.id.listView1);
        lv.setBackgroundResource(R.drawable.customshape);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cafeterias, names);
        ArrayList<String> namelist = new ArrayList<String>();
        for (int i = 0; i < names.length; ++i) {
            namelist.add(names[i]);
        }
        CafeteriaAdapter adapter = new CafeteriaAdapter(this, namelist);
        lv.setAdapter(adapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, namelink.get(parent.getItemAtPosition(position).toString()), Toast.LENGTH_LONG).show();
                System.out.println(namelink.get(parent.getItemAtPosition(position).toString()));
                originallink = namelink.get(parent.getItemAtPosition(position).toString());
                cafeterianame = parent.getItemAtPosition(position).toString();
                saveCafeteria(getApplicationContext(), originallink, cafeterianame);
                dialog.dismiss();

                MainActivity.this.recreate();

                new RetrieveURL().execute();

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private String getCafeteria(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("meals", MODE_PRIVATE);
        System.out.println("Link saved" + pref.getString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-248?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=13&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8"));//"http://www.hanyang.ac.kr/web/www/-248");)
        return pref.getString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-248?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=13&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8");//"http://www.hanyang.ac.kr/web/www/-248");
    }

    private void saveCafeteria(Context ctx, String cafeterialink, String cafeterianame) {
        SharedPreferences pref = ctx.getSharedPreferences("meals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if(cafeterialink.equals("http://www.hanyang.ac.kr/web/www/-248")) {
            editor.putString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-248?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=13&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8");
        } else if(cafeterialink.equals("http://www.hanyang.ac.kr/web/www/-2-")) {
            editor.putString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-2-?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=12&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8");
        } else {
            editor.putString("cafeterialink", cafeterialink);

        }
        editor.putString("cafeterianame", cafeterianame);
        editor.commit();
    }

    private String simpleDish(String dishname) {
        String ret;
        ret = dishname.substring(0, dishname.indexOf("("));
        ret = ret.trim();
        return ret;
    }

    private void loadMeals() {
        ArrayList<Meal> meallist = new ArrayList<Meal>();
        for (int i = 0; i < meals.length; ++i) {
            if (!meals[i].name.trim().equals("공통찬")) {
                System.out.println('"' + meals[i].name.trim() + '"');
                meallist.add(meals[i]);

            }

        }
        MealAdapter adapter = new MealAdapter(this, meallist);

        LayoutInflater inflater = getLayoutInflater();

        ListView lv = (ListView) findViewById(R.id.meallist);

        lv.setAdapter(adapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                System.out.println("Something clicked here!");
                System.out.println(view.getMeasuredHeight());
            }
        });
    }

    private void GetMenus(Elements elements) {
        if(elements.size() == 0) {
            open = false;
            return;
        } else {
            meals = new Meal[elements.size()];
            dishes = new String[elements.size()];
            prices = new int[elements.size()];

            for(int i = 0; i < elements.size(); ++i) {
                ArrayList<String> dishes = new ArrayList<String>();

                Elements dishlist = elements.get(i).select("h3");
                for(Element dishname: dishlist) {
                    dishes.add(simpleDish(dishname.text()));
                    System.out.println(dishname.text());
                }
                ArrayList<String> prices = new ArrayList<String>();
                Elements pricelist = elements.get(i).select(".price");
                for(Element price: pricelist) {
                    prices.add(price.text() + " 원");
                    System.out.println(price.text());
                }
                meals[i] = new Meal(elements.get(i).select(".d-title2").text(), dishes, prices);

            }
            loadMeals();

        }

    }

    private void GetCafeterias(Elements elements) {
        cafeteriaSelector.setAlpha(1);
        cafeteriaSelector.setText(elements.select(".active").text());
        //cafeteriaSelector.setText(elements.first().text());
        names = new String[elements.size()];
        links = new String[elements.size()];
        for (int i = 0; i < elements.size(); ++i) {
            String buffer = elements.get(i).html();
            Document doc = Jsoup.parse(buffer);
            names[i] = doc.text();
            links[i] = doc.select("a").attr("href");
            namelink.put(names[i], links[i]);
            System.out.println(links[i]);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.user.menutoday/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.user.menutoday/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class RetrieveURL extends AsyncTask<String, Void, Void> {

        private Exception exception;

        String retvalue;
        Elements cafeterias;
        Elements menus;


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
                while ((line = reader.readLine()) != null) {
                    result.append('\n' + line);

                }

                urlConnection.disconnect();
            } catch (IOException e) {
                System.out.println("Error Found!");
            } finally {

            }


            Document doc = null;
            try {
                doc = Jsoup.connect(originallink).get();

                cafeterias = doc.select(".tab-7 > li");

                menus = doc.select(".in-box");

                retvalue = cafeterias.html();


            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;

        }

        @Override
        protected void onPostExecute(Void something) {
            GetCafeterias(cafeterias);
            GetMenus(menus);
            //UpdateText(retvalue);
        }
    }

}
