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
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

            Intent clickintent = new Intent(context, WidgetActivity.class);
            clickintent.setAction(cafeteriaClick);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickintent, 0);

            views.setOnClickPendingIntent(R.id.widgetcafeteria, pendingIntent);


            new RetrieveURL().execute();
/*
            final Intent newintent = new Intent(context, UpdateService.class);
            final PendingIntent newpending = PendingIntent.getActivity(context, 0, newintent, 0);
            final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.cancel(newpending);
            long interval = 1000*10;
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),interval, newpending);*/

            ComponentName thisWidget = new ComponentName(context,WidgetActivity.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context,intent);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mealwidget);

        if(intent.getAction().equals(cafeteriaClick)) {
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

                } else {
                    views.setTextViewText(R.id.widgetcafeteria, cafeterialist.get(0));
                    saveCafeteria(context, links[0], names[0]);
                }

            } else {
                GetCafeterias(cafeterias);
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
            System.out.println("Adding " + doc.text().trim());
            names[i] = doc.text();
            links[i] = doc.select("a").attr("href");
            namelink.put(names[i], links[i]);
            System.out.println(links[i]);
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

    private class RetrieveURL extends AsyncTask<String, Void, Void> {

        private Exception exception;


        @Override
        protected Void doInBackground(String... urls) {
            Document doc = null;
            try {
                doc = Jsoup.connect(originallink).get();

                cafeterias = doc.select(".tab-7 > li");

                menus = doc.select(".in-box");

                retvalue = cafeterias.html();

                System.out.println(cafeterias.html());

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void something) {
            System.out.println("Post Execute Called!!");
            GetCafeterias(cafeterias);
            if(cafeterialist.isEmpty()) {

                String lastchoice = pref.getString("cafeterialink", cafeterialist.get(0));
                System.out.println("Last Choice found!! " + lastchoice);
                cafeterialist.indexOf(lastchoice);
            }
        //    GetMenus(menus);
            //UpdateText(retvalue);
        }
    }

}
