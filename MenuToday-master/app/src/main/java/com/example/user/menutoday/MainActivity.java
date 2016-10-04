package com.example.user.menutoday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Bundle;

import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.SAXParserFactory;


class CafeteriaItem {

    String cafeterianame;
    ArrayList<Meal> meals;
    String opentime;

    public CafeteriaItem(String cafeterianame, String opentime, ArrayList<Meal> meals) {
        this.cafeterianame = cafeterianame;
        this.opentime = opentime;
        this.meals = meals;
    }

}

public class MainActivity extends ActionBarActivity {//{//AppCompatActivity {

    TextView myTextView;
    Button cafeteriaSelector;
    ImageView openOptions;

    TextView appName;

    String[] names;
    String[] links;

    Meal[] meals;
    String[] dishes;
    int[] prices;

    Typeface typeface;
    Typeface boldtypeface;

    static boolean Rendered = false;
    static HashMap<String, Boolean> hasAdded;

    HashMap<String, String> namelink = new HashMap<String, String>();
    boolean open = true;

    String originallink;//"http://www.hanyang.ac.kr/web/www/-248?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=13&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8";
    String cafeterianame;

    SharedPreferences pref;
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

        openOptions = (ImageView) findViewById(R.id.openoptions);
        openOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenDialog();
            }
        });
//        cafeteriaSelector.setVisibility(View.GONE);

        hasAdded = new HashMap<String, Boolean>();

        originallink  = getCafeteria(this.getApplicationContext());

        AssetManager am = getAssets();

        typeface = Typeface.createFromAsset(am,
                String.format(Locale.KOREAN, "fonts/malgun.ttf", "fonts/malgun.ttf"));

        boldtypeface = Typeface.createFromAsset(am,
                String.format(Locale.KOREAN, "fonts/malgunbd.ttf", "fonts/malgunbd.tff"));

        pref = this.getApplicationContext().getSharedPreferences("meals", MODE_PRIVATE);

        appName = (TextView) findViewById(R.id.actionBarTitle);
        appName.setTypeface(boldtypeface);

        long mils = pref.getLong("TIME", 0);
        Date today = new Date();
        long curtime = today.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(curtime - mils);
        System.out.println("Saved time: " + mils + " " + curtime);
        System.out.println("Minutes: " + minutes);
        if (minutes > 360) {
            new RetrieveURL().execute();
        } else {
            System.out.println("LOADING FROM JSON FILE!!!");
            loadFromJson();
        }

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.LEFT | Gravity.CENTER_VERTICAL
        );

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        pref = this.getApplicationContext().getSharedPreferences("meals", MODE_PRIVATE);

        if(!pref.getBoolean("suggested", false)) {
            SuggestWidget();

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("suggested", true);
            editor.commit();
        }

        //getSupportActionBar().setDisplayShowTitleEnabled(false);

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

    private void loadFromJson() {

        pref = getApplicationContext().getSharedPreferences("meals", MODE_PRIVATE);

        String wholewebsite = pref.getString("allitems", "{}");
        System.out.println(wholewebsite);
        Gson gson = new Gson();
        CafeteriaItem[] cafeterialist = gson.fromJson(wholewebsite, CafeteriaItem[].class);

        names = new String[cafeterialist.length];

        for(int i = 0; i < cafeterialist.length; ++i) {
            System.out.println("From JSON OBJECT: " +  cafeterialist[i].cafeterianame);
            for(int j = 0; j < cafeterialist[i].meals.size(); ++j) {
                System.out.println(cafeterialist[i].meals.get(j).name);
            }
            names[i] = cafeterialist[i].cafeterianame;
        }

        SharedPreferences.Editor editor = pref.edit();
        String currentname = pref.getString("cafeterianame", "학생식당");
        //updateCafeteria(currentname);
        for(CafeteriaItem item: cafeterialist) {
            if(item.cafeterianame.equals(currentname)) {
                loadMeals(item.meals, item.opentime);

                break;
            }
        }

    }

    private void SuggestWidget() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.suggestwidget, null);
        dialog.setContentView(convertView);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
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


                cafeterianame = parent.getItemAtPosition(position).toString();
                saveCafeteria(getApplicationContext(), cafeterianame);
                dialog.dismiss();

                MainActivity.this.recreate();

                pref = getApplicationContext().getSharedPreferences("meals", MODE_PRIVATE);

            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private String getCafeteria(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("meals", MODE_PRIVATE);
        System.out.println("Link saved" + pref.getString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-248"));
        return pref.getString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-248");
    }

    private void saveCafeteria(Context ctx, String cafeterianame) {
        SharedPreferences pref = ctx.getSharedPreferences("meals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();


        editor.putString("cafeterianame", cafeterianame);
        editor.commit();
    }

    private String simpleDish(String dishname) {
        String ret;
        System.out.println("Dishname at simpleDish: " + dishname);
        if(dishname.length() == 0) {
            return "";
        }
        if(dishname.indexOf("(") == 0 && dishname.indexOf(")") >= dishname.indexOf("(")) {
            ret = dishname.substring(dishname.indexOf("("), dishname.indexOf(")") + 1);
            if(ret.matches(".*[a-zA-Z]+.*")) {
                ret = ret.trim();
                ret = dishname.substring(dishname.indexOf(")") + 1);

                return simpleDish(commaspacing(removeheader(ret)));
            }

        } else if(dishname.indexOf("(") > 0 && dishname.indexOf(")") > dishname.indexOf("(")) {
            ret = dishname.substring(dishname.indexOf("("), dishname.indexOf(")") + 1);
            if(ret.matches(".*[a-zA-Z]+.*")) {
                ret = ret.trim();
                ret = dishname.substring(dishname.indexOf(")") + 1);

                return simpleDish(commaspacing(removeheader(ret)));
            }
        }
        return commaspacing(removeheader(dishname));
    }

    private String stripWon(String price) {
        String ret;
        System.out.println("Price at stripWon: " + price);

        String testval = price;
        int index = testval.indexOf("원");
        int count = 0;
        while(index != -1) {
            count++;
            testval = testval.substring(index + 1);
            index = testval.indexOf("원");
        }

        if(count > 2) {
            return price;
        }

        if(price.length() == 0) {
            return "-";
        } else if(price.contains("원")) {
            ret = price.substring(0, price.indexOf("원"));
            ret = ret.trim();
            return ret;
        }
        return price;
    }

    private void loadMeals(ArrayList<Meal> mealmenus, String opentime) {
        ArrayList<Meal> meallist = mealmenus;
        /*
        for (int i = 0; i < meals.length; ++i) {
            if (!meals[i].name.trim().equals("공통찬")) {
                System.out.println('"' + meals[i].name.trim() + '"');
                meallist.add(meals[i]);
            }
        }
        */
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


        LinearLayout cafeteriaLayout = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.restaurantheader, null, false);

        pref = getApplicationContext().getSharedPreferences("meals", MODE_PRIVATE);
        cafeteriaSelector = (Button) cafeteriaLayout.findViewById(R.id.selector);
        cafeteriaSelector.setText(pref.getString("cafeterianame", "학생식당"));
        cafeteriaSelector.setTypeface(boldtypeface);


        TextView opentimeView = (TextView) cafeteriaLayout.findViewById(R.id.opentime);
        opentimeView.setText(opentime);
        opentimeView.setTypeface(typeface);

        lv.addHeaderView(cafeteriaLayout);

        if(mealmenus.size() == 0) {
            LinearLayout closedLayout = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.closed, null);
            TextView closedMessage = (TextView) closedLayout.findViewById(R.id.closed);
            closedMessage.setTypeface(typeface);
            lv.addFooterView(closedLayout);
        }

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

                    //populating HashMap
                    hasAdded.put(dishname.text(), false);
                    System.out.println(dishname.text());
                }
                ArrayList<String> prices = new ArrayList<String>();
                Elements pricelist = elements.get(i).select(".price");
                for(Element price: pricelist) {
                    prices.add(stripWon(price.text()) + " 원");
                    System.out.println(stripWon(price.text()));
                }
                meals[i] = new Meal(elements.get(i).select(".d-title2").text(), dishes, prices);

            }


        }

    }

    private String removeheader(String text) {
        String ret = text.trim();
        if(ret.charAt(0) == '[') {
            int last = 0;
            for(int i = 0; i < ret.length(); ++i) {
                if(ret.charAt(i) == ']') {
                    last = i;
                    return removeheader(ret.substring(i + 1));
                }
            }
            return ret.substring(last + 1);

        }
        return ret;
    }

    private String commaspacing(String text) {
        if(!text.contains(",")) {
            return text.trim();
        } else {
            int index = 0;
            text = text.trim();
            while(index < text.length()) {
                if (text.charAt(index) ==',' && index + 1 < text.length() && text.charAt(index + 1) != ' ') {
                    text = text.substring(0, index + 1) + ' ' + text.substring(index + 1);
                }
                index++;
            }
        }
        return text.trim();
    }

    private void GetCafeterias(Elements elements) {
        //    cafeteriaSelector.setAlpha(1);
        //    cafeteriaSelector.setText(elements.select(".active").text());
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

    private void updateCafeteria(String cafeterianame) {
        cafeteriaSelector.setAlpha(1);
        cafeteriaSelector.setText(cafeterianame);
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

        Elements cafeterias;
        Elements menus;


        @Override
        protected Void doInBackground(String... urls) {

            Document doc = null;
            try {
                doc = Jsoup.connect(originallink).get();

                cafeterias = doc.select(".tab-7 > li");
                //   GetCafeterias(cafeterias);

                menus = doc.select(".in-box");
                //   GetMenus(menus);

            } catch (IOException e) {
                doc = Jsoup.parse("");

                cafeterias = doc.select(".tab-7 > li");

                menus = doc.select(".in-box");

                e.printStackTrace();
            }

            names = new String[cafeterias.size()];
            links = new String[cafeterias.size()];

            for(int i = 0; i < cafeterias.size(); ++i) {
                String buffer = cafeterias.get(i).html();
                Document bufferdoc = Jsoup.parse(buffer);
                names[i] = bufferdoc.text();
                links[i] = bufferdoc.select("a").attr("href");
                namelink.put(names[i], links[i]);
            }

            ArrayList<CafeteriaItem> cafeterialist = new ArrayList<CafeteriaItem>();

            try {
                for (int i = 0; i < links.length; ++i) {
                    String cafeterianame = names[i];
                    doc = Jsoup.connect(links[i]).get();

                    menus = doc.select(".in-box");
                    GetMenus(menus);
                    String opentime = doc.select("pre").text();

                    System.out.println("Opentime: " + opentime);

                    ArrayList<Meal> CafeteriaMeals = new ArrayList<Meal>();
                    for(int x = 0; x < meals.length; ++x) {
                        if(!meals[x].name.equals("공통찬"))
                        CafeteriaMeals.add(meals[x]);

                        /*ArrayList<String> dishlist = new ArrayList<String>();
                        ArrayList<String> pricelist = new ArrayList<String>();
                        for(int y = 0; y < meals[x].dishes.size(); ++y) {
                            dishlist.add(meals[x].dishes.get(y));
                            pricelist.add(meals[x].prices.get(y));
                        }*/

                    }
                    cafeterialist.add(new CafeteriaItem(cafeterianame, opentime, CafeteriaMeals));

                }

                Gson gson = new Gson();

                String prettyJson = gson.toJson(cafeterialist);
                pref = getApplicationContext().getSharedPreferences("meals", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putLong("TIME", new Date().getTime());
                editor.commit();

                editor.putString("allitems", prettyJson);
                editor.commit();

            } catch(IOException e) {

            }


            return null;

        }

        @Override
        protected void onPostExecute(Void something) {
            loadFromJson();


            //    GetCafeterias(cafeterias);
            //    GetMenus(menus);
            //UpdateText(retvalue);
        }
    }

}