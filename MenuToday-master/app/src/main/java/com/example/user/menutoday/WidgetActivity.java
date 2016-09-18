package com.example.user.menutoday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by user on 2016-09-17.
 */
public class WidgetActivity extends AppWidgetProvider {

    public static ArrayList<String> cafeterialist = new ArrayList<String>();

    public static String cafeteriaClick = "CafeteriaWidgetClicked";

    public static SharedPreferences pref;

    String retvalue;
    static Elements cafeterias;
    static Elements menus;
    static String[] names;
    static String[] links;
    static boolean open = true;

    static Meal[] meals;
    static String[] dishes;
    static int[] prices;

    public static final String ACTION_UPDATE_CLICK = "android.appwidget.action.ACTION_WIDGET_CLICK";

    static HashMap<String, String> namelink = new HashMap<String, String>();

    String originallink = "http://www.hanyang.ac.kr/web/www/-2-?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=12&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int i=0; i<appWidgetIds.length; i++){
            int currentWidgetId = appWidgetIds[i];

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pending = PendingIntent.getActivity(context, 0,intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.mealwidget);

            //views.setOnClickPendingIntent(R.id.button, pending);
            appWidgetManager.updateAppWidget(currentWidgetId,views);
            //Toast.makeText(context, "widget added", Toast.LENGTH_SHORT).show();

            pref =  context.getSharedPreferences("meals", Context.MODE_PRIVATE);

            Calendar c = Calendar.getInstance();
            int minutes = c.get(Calendar.MINUTE);
            int hours = c.get(Calendar.HOUR_OF_DAY);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);

            views.setTextViewText(R.id.currentTime, String.format("%02d", Integer.parseInt(String.valueOf(month))) + "/" + String.format("%02d", Integer.parseInt(String.valueOf(day))) +
                    " " + String.format("%02d", Integer.parseInt(String.valueOf(hours))) + ":" + String.format("%02d", Integer.parseInt(String.valueOf(minutes))));

            views.setTextViewText(R.id.widgetcafeteria, pref.getString("cafeterianame", "학생식당"));

            //setting up an intent and assigning it to cafeteriaClick
            Intent clickintent = new Intent(context, getClass());
            clickintent.setAction(ACTION_UPDATE_CLICK);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, currentWidgetId, clickintent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.widgetcafeteria, pendingIntent);

            ComponentName thisWidget = new ComponentName(context,WidgetActivity.class);

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, views);

            originallink = pref.getString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-2-?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=12&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8");

            System.out.println("Original link from namelink retrieval: " + originallink);

            new RetrieveURL(views, context, manager).execute();
/*
            final Intent newintent = new Intent(context, UpdateService.class);
            final PendingIntent newpending = PendingIntent.getActivity(context, 0, newintent, 0);
            final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(newpending);
            long interval = 1000*10;
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),interval, newpending);*/


        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context,intent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mealwidget);

        pref =  context.getSharedPreferences("meals", Context.MODE_PRIVATE);

        System.out.println(intent.getAction());

        if(intent.getAction().equals(ACTION_UPDATE_CLICK)) {
            if(!cafeterialist.isEmpty()) {
                for(int i = 0; i < cafeterialist.size(); ++i) {
                    System.out.println("Cafeterialist: " + cafeterialist.get(i));
                }
                int curindex = cafeterialist.indexOf(pref.getString("cafeterianame", cafeterialist.get(0)));
                System.out.println("Current index is: " + curindex + ", " + pref.getString("cafeterianame", cafeterialist.get(0)));
                if(curindex < cafeterialist.size() - 1) {
                    System.out.println("Setting value to " + cafeterialist.get(curindex + 1));
                    views.setTextViewText(R.id.widgetcafeteria, cafeterialist.get(curindex + 1));
                    saveCafeteria(context, links[curindex + 1], names[curindex + 1]);
                    System.out.println(cafeterialist.get(curindex + 1));
                    System.out.println("New link: " + namelink.get(names[curindex + 1]));
                    originallink = namelink.get(names[curindex + 1]);
                } else {
                    views.setTextViewText(R.id.widgetcafeteria, cafeterialist.get(0));
                    saveCafeteria(context, links[0], names[0]);
                    System.out.println(cafeterialist.get(0));
                    System.out.println("New link: " + namelink.get(names[0]));
                    originallink = namelink.get(names[0]);
                }



                AppWidgetManager manager = AppWidgetManager.getInstance(context);



                AppWidgetManager.getInstance(context).updateAppWidget(
                        new ComponentName(context, WidgetActivity.class),views);

                ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetActivity.class.getName());
                int[] appWidgetIds = manager.getAppWidgetIds(thisAppWidget);

                onUpdate(context, manager, appWidgetIds);

                new RetrieveURL(views, context, manager).execute();

                /*

                for(int i = 0; i <5; ++i) {
                    RemoteViews headerText = new RemoteViews(context.getPackageName(), R.layout.widgetheader);

                    views.addView(R.id.headermeal, headerText);
                    //System.out.println("Meals found from widget " + meals[i].name);
                    AppWidgetManager.getInstance(context).updateAppWidget(
                            new ComponentName(context, WidgetActivity.class),views);
                }*/

            } else {
                //GetCafeterias(cafeterias);
            }

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);
        }
    }

    private void GetCafeterias(Elements elements) {
        System.out.println("Get Cafeteria Called!");
        cafeterialist = new ArrayList<String>();
        names = new String[elements.size()];
        links = new String[elements.size()];

        for (int i = 0; i < elements.size(); ++i) {
            String buffer = elements.get(i).html();
            Document doc = Jsoup.parse(buffer);
            cafeterialist.add(doc.text().trim());
            //System.out.println("Adding " + doc.text().trim() + doc.select("a").attr("href"));
            names[i] = doc.text();
            links[i] = doc.select("a").attr("href");
            namelink.put(names[i], links[i]);
            //System.out.println(links[i]);
        }

    }

    private String simpleDish(String dishname) {
        String ret;
        ret = dishname.substring(0, dishname.indexOf("("));
        ret = ret.trim();
        return ret;
    }

    private Meal[] GetMenus(Elements elements) {
        Meal[] meals = new Meal[elements.size()];
        String[] dishes = new String[elements.size()];
        int[] prices = new int[elements.size()];
        if(elements.size() == 0) {
            open = false;

            return meals;
        } else {

            for(int i = 0; i < elements.size(); ++i) {
                ArrayList<String> dishesarray = new ArrayList<String>();

                Elements dishlist = elements.get(i).select("h3");
                for(Element dishname: dishlist) {
                    dishesarray.add(simpleDish(dishname.text()));

                    //populating HashMap

                    System.out.println("Adding dishname: " + dishname.text());
                }
                ArrayList<String> pricesarray = new ArrayList<String>();
                Elements pricelist = elements.get(i).select(".price");
                for(Element price: pricelist) {
                    pricesarray.add(price.text() + " 원");
                    System.out.println(price.text());
                }
                meals[i] = new Meal(elements.get(i).select(".d-title2").text(), dishesarray, pricesarray);

            }

            return meals;
        }

    }

    private void saveCafeteria(Context ctx, String cafeterialink, String cafeterianame) {

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

    private class RetrieveURL extends AsyncTask<String, Void, RemoteViews> {

        private Exception exception;

        private RemoteViews views;
        private Context context;
        private AppWidgetManager WidgetManager;

        public RetrieveURL(RemoteViews views, Context context, AppWidgetManager appWidgetManager) {
            this.views = views;
            this.context = context;
            this.WidgetManager = appWidgetManager;
        }

        @Override
        protected RemoteViews doInBackground(String... urls) {
            Document doc = null;
            try {
                if(originallink.equals("http://www.hanyang.ac.kr/web/www/-2-")) {
                    originallink = "http://www.hanyang.ac.kr/web/www/-2-?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=12&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8";
                } else if (originallink.equals("http://www.hanyang.ac.kr/web/www/-248")) {
                    originallink = "http://www.hanyang.ac.kr/web/www/-248?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=13&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8";
                }

                System.out.println("Original link before retrieval: " + originallink);

                doc = Jsoup.connect(originallink).get();

                cafeterias = doc.select(".tab-7 > li");

                menus = doc.select(".in-box");

                System.out.println("Current menu status at retrieve: " + menus.html());

                retvalue = cafeterias.html();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(RemoteViews views) {
            System.out.println("Post Execute Called!!");
            GetCafeterias(cafeterias);

            if(cafeterialist.isEmpty()) {

                String lastchoice = pref.getString("cafeterialink", cafeterialist.get(0));
                System.out.println("Last Choice found!! " + lastchoice);
                cafeterialist.indexOf(lastchoice);

            }

            System.out.println("Current menu status: " + menus.html());
            Meal[] meals = GetMenus(menus);

            RemoteViews headerView = new RemoteViews(context.getPackageName(), R.id.header);

            this.views.removeAllViews(R.id.header);

            for(int i = 0; i < meals.length; i++) {

                RemoteViews textView = new RemoteViews(context.getPackageName(), R.layout.widgetheader);
                if(meals[i].name.equals("공통찬")) continue;

                textView.setTextViewText(R.id.headermeal, meals[i].name);
                //textView.setTextViewText(R.id.headermeal, "TextView number " + String.valueOf(i));
                this.views.addView(R.id.header, textView);
            }

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class), this.views);


            //UpdateText(retvalue);
        }
    }

}
