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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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

    static CafeteriaItem[] jsonlist;

    public static final String ACTION_UPDATE_CLICK = "android.appwidget.action.ACTION_WIDGET_CLICK";
    public static final String HEADER_CLICK = "android.appwidget.action.HEADER_CLICK";

    public static final String HEADER_CLICK1 = "android.appwidget.action.HEADER_CLICK1";
    public static final String HEADER_CLICK2 = "android.appwidget.action.HEADER_CLICK2";
    public static final String HEADER_CLICK3 = "android.appwidget.action.HEADER_CLICK3";
    public static final String HEADER_CLICK4 = "android.appwidget.action.HEADER_CLICK4";
    public static final String HEADER_CLICK5 = "android.appwidget.action.HEADER_CLICK5";

    public static final String CAFETERIA_CLICK1 = "android.appwidget.action.CAFETERIA_CLICK1";
    public static final String CAFETERIA_CLICK2 = "android.appwidget.action.CAFETERIA_CLICK2";
    public static final String CAFETERIA_CLICK3 = "android.appwidget.action.CAFETERIA_CLICK3";
    public static final String CAFETERIA_CLICK4 = "android.appwidget.action.CAFETERIA_CLICK4";
    public static final String CAFETERIA_CLICK5 = "android.appwidget.action.CAFETERIA_CLICK5";
    public static final String CAFETERIA_CLICK6 = "android.appwidget.action.CAFETERIA_CLICK6";
    public static final String CAFETERIA_CLICK7 = "android.appwidget.action.CAFETERIA_CLICK7";
    public static final String CAFETERIA_CLICK8 = "android.appwidget.action.CAFETERIA_CLICK8";

    public static final String NEXT_MENU = "android.appwidget.action.NEXT_MENU";
    public static final String PREV_MENU = "android.appwidget.action.PREV_MENU";

    public static final String MENU_CLICKED = "android.appwidget.action.MENU_CLICKED";

    public static final String MANUAL_UPDATE = "android.appwidget.action.MANUAL_UPDATE";

    static int cafeteriaindex = 0;
    static boolean settingRestaurant = false;
    static boolean cafeterialistopen = false;

    static HashMap<String, String> namelink = new HashMap<String, String>();

    static boolean loadMeal = false;
    static int clickedIndex = 0;
    static boolean updateRestaurant = false;

    static boolean nextMenu = false;
    static boolean prevMenu = false;

    static boolean changingMenu = false;

    static boolean manualUpdate = false;

    static String[] weekdays = { "", "일", "월", "화", "수", "목", "금", "토"};

    static String[] restaurants = {"학생식당", "교직원식당", "사랑방", "신교직원식당", "신학생식당", "제2생활관식당", "행원파크"};

    String originallink = "http://www.hanyang.ac.kr/web/www/-2-?p_p_id=foodView_WAR_foodportlet&p_p_lifecycle=0&p_p_state=normal&p_p_mode=view&p_p_col_id=column-1&p_p_col_pos=1&p_p_col_count=2&_foodView_WAR_foodportlet_sFoodDateDay=12&_foodView_WAR_foodportlet_sFoodDateYear=2016&_foodView_WAR_foodportlet_action=view&_foodView_WAR_foodportlet_sFoodDateMonth=8";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for(int i=0; i<appWidgetIds.length; i++){
            int currentWidgetId = appWidgetIds[i];

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.mealwidget);

            appWidgetManager.updateAppWidget(currentWidgetId,views);

            pref =  context.getSharedPreferences("meals", Context.MODE_PRIVATE);

            Calendar c = Calendar.getInstance();
            int minutes = c.get(Calendar.MINUTE);
            int hours = c.get(Calendar.HOUR_OF_DAY);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int weekday = c.get(Calendar.DAY_OF_WEEK);

            views.setTextViewText(R.id.currentTime, Integer.parseInt(String.valueOf(month)) + "월 " + Integer.parseInt(String.valueOf(day)) +
                    "일 " + weekdays[weekday] + "요일");

            if(!updateRestaurant) {
                views.setTextViewText(R.id.widgetcafeteria, pref.getString("cafeterianame", "학생식당"));
            }


            //setting up an intent and assigning it to cafeteriaClick
            Intent clickintent = new Intent(context, getClass());
            clickintent.setAction(ACTION_UPDATE_CLICK);

            Intent headerintent = new Intent(context, getClass());
            headerintent.setAction(HEADER_CLICK);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, currentWidgetId, clickintent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent headerpendingIntent = PendingIntent.getBroadcast(context, currentWidgetId, headerintent, PendingIntent.FLAG_UPDATE_CURRENT);

            Intent headerintent1 = new Intent(context, getClass());
            Intent headerintent2 = new Intent(context, getClass());
            Intent headerintent3 = new Intent(context, getClass());
            Intent headerintent4 = new Intent(context, getClass());
            Intent headerintent5 = new Intent(context, getClass());

            headerintent1.setAction(HEADER_CLICK1);
            headerintent2.setAction(HEADER_CLICK2);
            headerintent3.setAction(HEADER_CLICK3);
            headerintent4.setAction(HEADER_CLICK4);
            headerintent5.setAction(HEADER_CLICK5);

            PendingIntent headerpendingintent1 = PendingIntent.getBroadcast(context, currentWidgetId, headerintent1, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent headerpendingintent2 = PendingIntent.getBroadcast(context, currentWidgetId, headerintent2, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent headerpendingintent3 = PendingIntent.getBroadcast(context, currentWidgetId, headerintent3, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent headerpendingintent4 = PendingIntent.getBroadcast(context, currentWidgetId, headerintent4, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent headerpendingintent5 = PendingIntent.getBroadcast(context, currentWidgetId, headerintent5, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.headermeal1, headerpendingintent1);
            views.setOnClickPendingIntent(R.id.headermeal2, headerpendingintent2);
            views.setOnClickPendingIntent(R.id.headermeal3, headerpendingintent3);
            views.setOnClickPendingIntent(R.id.headermeal4, headerpendingintent4);
            views.setOnClickPendingIntent(R.id.headermeal5, headerpendingintent5);

            views.setOnClickPendingIntent(R.id.widgetcafeteria, pendingIntent);
            views.setOnClickPendingIntent(R.id.header, headerpendingIntent);

            //setting listeners for widget cafeterialist items
            Intent cafeteriaintent1 = new Intent(context, getClass());
            Intent cafeteriaintent2 = new Intent(context, getClass());
            Intent cafeteriaintent3 = new Intent(context, getClass());
            Intent cafeteriaintent4 = new Intent(context, getClass());
            Intent cafeteriaintent5 = new Intent(context, getClass());
            Intent cafeteriaintent6 = new Intent(context, getClass());
            Intent cafeteriaintent7 = new Intent(context, getClass());
            Intent cafeteriaintent8 = new Intent(context, getClass());

            cafeteriaintent1.setAction(CAFETERIA_CLICK1);
            cafeteriaintent2.setAction(CAFETERIA_CLICK2);
            cafeteriaintent3.setAction(CAFETERIA_CLICK3);
            cafeteriaintent4.setAction(CAFETERIA_CLICK4);
            cafeteriaintent5.setAction(CAFETERIA_CLICK5);
            cafeteriaintent6.setAction(CAFETERIA_CLICK6);
            cafeteriaintent7.setAction(CAFETERIA_CLICK7);
            cafeteriaintent8.setAction(CAFETERIA_CLICK8);

            PendingIntent cafeteriapendingintent1 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent1, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cafeteriapendingintent2 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent2, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cafeteriapendingintent3 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent3, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cafeteriapendingintent4 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent4, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cafeteriapendingintent5 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent5, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cafeteriapendingintent6 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent6, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cafeteriapendingintent7 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent7, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent cafeteriapendingintent8 = PendingIntent.getBroadcast(context, currentWidgetId, cafeteriaintent8, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.cafeteriachoice1, cafeteriapendingintent1);
            views.setOnClickPendingIntent(R.id.cafeteriachoice2, cafeteriapendingintent2);
            views.setOnClickPendingIntent(R.id.cafeteriachoice3, cafeteriapendingintent3);
            views.setOnClickPendingIntent(R.id.cafeteriachoice4, cafeteriapendingintent4);
            views.setOnClickPendingIntent(R.id.cafeteriachoice5, cafeteriapendingintent5);
            views.setOnClickPendingIntent(R.id.cafeteriachoice6, cafeteriapendingintent6);
            views.setOnClickPendingIntent(R.id.cafeteriachoice7, cafeteriapendingintent7);
            views.setOnClickPendingIntent(R.id.cafeteriachoice8, cafeteriapendingintent8);

            Intent nextIntent = new Intent(context, getClass());
            Intent prevIntent = new Intent(context, getClass());

            nextIntent.setAction(NEXT_MENU);
            prevIntent.setAction(PREV_MENU);

            PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, currentWidgetId, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent prevPendingIntent = PendingIntent.getBroadcast(context, currentWidgetId, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.rightclick, nextPendingIntent);
            views.setOnClickPendingIntent(R.id.leftclick, prevPendingIntent);

            Intent menuIntent = new Intent(context, getClass());
            menuIntent.setAction(MENU_CLICKED);

            PendingIntent menuPendingIntent = PendingIntent.getBroadcast(context, currentWidgetId, menuIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.menu1, menuPendingIntent);

            Intent manualIntent = new Intent(context, getClass());
            manualIntent.setAction(MANUAL_UPDATE);

            PendingIntent manualPendingIntent = PendingIntent.getBroadcast(context, currentWidgetId, manualIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.currentTime, manualPendingIntent);

            ComponentName thisWidget = new ComponentName(context,WidgetActivity.class);

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, views);

            if(!loadMeal) {
                originallink = pref.getString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-2-");
            }

            originallink = pref.getString("cafeterialink", "http://www.hanyang.ac.kr/web/www/-2-");

            System.out.println("Original link from namelink retrieval: " + originallink);

            new RetrieveURL(views, context, manager, currentWidgetId).execute();
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

        //pref = context.getSharedPreferences("meals", Context.MODE_PRIVATE);

        System.out.println(intent.getAction());

        RemoteViews textView1 = new RemoteViews(context.getPackageName(), R.id.headermeal1);
        RemoteViews textView2 = new RemoteViews(context.getPackageName(), R.id.headermeal2);
        RemoteViews textView3 = new RemoteViews(context.getPackageName(), R.id.headermeal3);
        RemoteViews textView4 = new RemoteViews(context.getPackageName(), R.id.headermeal4);
        RemoteViews textView5 = new RemoteViews(context.getPackageName(), R.id.headermeal5);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetActivity.class),views);

        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetActivity.class.getName());
        int[] appWidgetIds = manager.getAppWidgetIds(thisAppWidget);

        System.out.println("Current settingRestaurant: " + settingRestaurant + " Current updateRestaurant: " + updateRestaurant + " " + intent.getAction());

        if(intent.getAction().equals(ACTION_UPDATE_CLICK)) {
            if(!cafeterialist.isEmpty()) {

            } else {
                //GetCafeterias(cafeterias);
            }

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);

            updateRestaurant = true;


            System.out.println("Update Restaurant set as: " + updateRestaurant);

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(HEADER_CLICK)) {
            System.out.println("Textview clicked!");

        } else if(intent.getAction().equals(HEADER_CLICK1)) {
            views.setTextColor(R.id.headermeal1, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal2, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal3, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal4, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal5, context.getResources().getColor(R.color.LightSlateGray));

            views.setTextColor(R.id.headermeal1, context.getResources().getColor(R.color.White));

            clickedIndex = 0;
            saveMealIndex(context, clickedIndex);
            loadMeal = true;
            System.out.println("Set loadMeal as: " + loadMeal);
            System.out.println("Set clickedIndex as: " + clickedIndex);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);

            onUpdate(context, manager, appWidgetIds);


        } else if(intent.getAction().equals(HEADER_CLICK2)) {
            views.setTextColor(R.id.headermeal1, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal2, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal3, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal4, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal5, context.getResources().getColor(R.color.LightSlateGray));

            views.setTextColor(R.id.headermeal2, context.getResources().getColor(R.color.White));

            clickedIndex = 1;
            saveMealIndex(context, clickedIndex);
            loadMeal = true;
            System.out.println("Set loadMeal as: " + loadMeal);
            System.out.println("Set clickedIndex as: " + clickedIndex);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);

            onUpdate(context, manager, appWidgetIds);

        } else if(intent.getAction().equals(HEADER_CLICK3)) {
            views.setTextColor(R.id.headermeal1, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal2, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal3, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal4, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal5, context.getResources().getColor(R.color.LightSlateGray));

            views.setTextColor(R.id.headermeal3, context.getResources().getColor(R.color.White));

            clickedIndex = 2;
            saveMealIndex(context, clickedIndex);
            loadMeal = true;
            System.out.println("Set loadMeal as: " + loadMeal);
            System.out.println("Set clickedIndex as: " + clickedIndex);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);

            onUpdate(context, manager, appWidgetIds);

        } else if(intent.getAction().equals(HEADER_CLICK4)) {
            views.setTextColor(R.id.headermeal1, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal2, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal3, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal4, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal5, context.getResources().getColor(R.color.LightSlateGray));

            views.setTextColor(R.id.headermeal4, context.getResources().getColor(R.color.White));

            clickedIndex = 3;
            saveMealIndex(context, clickedIndex);
            loadMeal = true;
            System.out.println("Set loadMeal as: " + loadMeal);
            System.out.println("Set clickedIndex as: " + clickedIndex);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);

            onUpdate(context, manager, appWidgetIds);

        } else if(intent.getAction().equals(HEADER_CLICK5)) {
            views.setTextColor(R.id.headermeal1, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal2, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal3, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal4, context.getResources().getColor(R.color.LightSlateGray));
            views.setTextColor(R.id.headermeal5, context.getResources().getColor(R.color.LightSlateGray));

            views.setTextColor(R.id.headermeal5, context.getResources().getColor(R.color.White));

            clickedIndex = 4;
            saveMealIndex(context, clickedIndex);
            loadMeal = true;
            System.out.println("Set loadMeal as: " + loadMeal);
            System.out.println("Set clickedIndex as: " + clickedIndex);

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class),views);

            onUpdate(context, manager, appWidgetIds);

        } else if(intent.getAction().equals(CAFETERIA_CLICK1)) {
            cafeteriaindex = 0;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(CAFETERIA_CLICK2)) {
            cafeteriaindex = 1;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(CAFETERIA_CLICK3)) {
            cafeteriaindex = 2;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(CAFETERIA_CLICK4)) {
            cafeteriaindex = 3;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(CAFETERIA_CLICK5)) {
            cafeteriaindex = 4;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(CAFETERIA_CLICK6)) {
            cafeteriaindex = 5;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(CAFETERIA_CLICK7)) {
            cafeteriaindex = 6;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(CAFETERIA_CLICK8)) {
            cafeteriaindex = 7;

            settingRestaurant = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(cafeterialistopen) {
            views.setViewVisibility(R.id.widgetcafeteria, View.VISIBLE);

            views.setViewVisibility(R.id.cafeteriachoices, View.GONE);

            System.out.println("Setting restaurant and non update!!!");

            cafeterialistopen = false;

        } else if(intent.getAction().equals(NEXT_MENU)) {
            nextMenu = true;

            changingMenu = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(PREV_MENU)) {
            prevMenu = true;

            changingMenu = true;

            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(MENU_CLICKED)) {
            onUpdate(context, manager, appWidgetIds);
        } else if(intent.getAction().equals(MANUAL_UPDATE)) {
            manualUpdate = true;

            onUpdate(context, manager, appWidgetIds);
        }

        float scale = context.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (4 * scale + 0.5f);

        if(!changingMenu) {
            views.setInt(R.id.headermeal1, "setBackgroundResource", R.drawable.nounderline);
            views.setInt(R.id.headermeal2, "setBackgroundResource", R.drawable.nounderline);
            views.setInt(R.id.headermeal3, "setBackgroundResource", R.drawable.nounderline);
            views.setInt(R.id.headermeal4, "setBackgroundResource", R.drawable.nounderline);
            views.setInt(R.id.headermeal5, "setBackgroundResource", R.drawable.nounderline);

        } else {
            changingMenu = false;
        }



        views.setViewPadding(R.id.headermeal1, 0, dpAsPixels, 0, dpAsPixels);
        views.setViewPadding(R.id.headermeal2, 0, dpAsPixels, 0, dpAsPixels);
        views.setViewPadding(R.id.headermeal3, 0, dpAsPixels, 0, dpAsPixels);
        views.setViewPadding(R.id.headermeal4, 0, dpAsPixels, 0, dpAsPixels);
        views.setViewPadding(R.id.headermeal5, 0, dpAsPixels, 0, dpAsPixels);

        //   onUpdate(context, manager, appWidgetIds);

        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, WidgetActivity.class),views);
    }

    private void loadFromJson() {
        String wholewebsite = pref.getString("allitems", "{}");
        System.out.println(wholewebsite);
        Gson gson = new Gson();
        jsonlist = gson.fromJson(wholewebsite, CafeteriaItem[].class);

        names = new String[jsonlist.length];

        //reinitializing to empty arraylist
        cafeterialist = new ArrayList<String>();

        for(int i = 0; i < jsonlist.length; ++i) {
            System.out.println("From JSON OBJECT: " +  jsonlist[i].cafeterianame);
            for(int j = 0; j < jsonlist[i].meals.size(); ++j) {
                System.out.println(jsonlist[i].meals.get(j).name);
            }
            cafeterialist.add(jsonlist[i].cafeterianame);
            names[i] = jsonlist[i].cafeterianame;
        }
        //pref = this.getContext().getSharedPreferences("meals", MODE_PRIVATE);

        String currentname = pref.getString("cafeterianame", "학생식당");

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

    public String removeheader(String text) {
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

    public String commaspacing(String text) {
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

    private Meal[] GetMenus(Elements elements) {
        Meal[] meals = new Meal[elements.size()];
        String[] dishes = new String[elements.size()];
        int[] prices = new int[elements.size()];
        if(elements.size() == 0) {
            open = false;
            System.out.println("Cafeterias closed!!!");
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
                    pricesarray.add(stripWon(price.text()) + " 원");
                    System.out.println(stripWon(price.text()));
                }
                meals[i] = new Meal(elements.get(i).select(".d-title2").text(), dishesarray, pricesarray);

            }

            return meals;
        }

    }

    private void saveMealIndex(Context ctx, int index) {
        pref= ctx.getSharedPreferences("meals", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt("mealindex", index);
        editor.commit();
    }

    private void saveMenuIndex(Context ctx, int index) {
        pref = ctx.getSharedPreferences("meals", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt("menuindex", index);
        editor.commit();
    }

    private void saveCafeteria(Context ctx,  String cafeterianame) {
        pref = ctx.getSharedPreferences("meals", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("cafeterianame", cafeterianame);
        editor.commit();
    }

    private void saveMealOfDay(Context ctx, String mealname) {
        pref = ctx.getSharedPreferences("meals", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("mealname", mealname);
        editor.commit();

    }

    private class RetrieveURL extends AsyncTask<String, Void, Void> {

        private Exception exception;

        private RemoteViews views;
        private Context context;
        private AppWidgetManager WidgetManager;
        private int currentWidgetId;

        public RetrieveURL(RemoteViews views, Context context, AppWidgetManager appWidgetManager, int currentWidgetId) {
            this.views = views;
            this.context = context;
            this.WidgetManager = appWidgetManager;
            this.currentWidgetId = currentWidgetId;
        }

        @Override
        protected Void doInBackground(String... urls) {
            Document doc = null;

            long mils = pref.getLong("TIME", 0);
            Date today = new Date();
            long curtime = today.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(curtime - mils);
            System.out.println("Saved time: " + mils + " " + curtime);
            System.out.println("Minutes: " + minutes);
            if(minutes > 360 || manualUpdate) {
                manualUpdate = false;
                try {

                    System.out.println("Original link before retrieval: " + originallink);

                    doc = Jsoup.connect(originallink).get();

                    cafeterias = doc.select(".tab-7 > li");

                    menus = doc.select(".in-box");

                    //System.out.println("Current menu status at retrieve: " + menus.html());

                    retvalue = cafeterias.html();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                names = new String[cafeterias.size()];
                links = new String[cafeterias.size()];

                for (int i = 0; i < cafeterias.size(); ++i) {
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
                        meals = GetMenus(menus);

                        ArrayList<Meal> CafeteriaMeals = new ArrayList<Meal>();
                        for (int x = 0; x < meals.length; ++x) {
                            CafeteriaMeals.add(meals[x]);

                            /*ArrayList<String> dishlist = new ArrayList<String>();
                            ArrayList<String> pricelist = new ArrayList<String>();
                            for(int y = 0; y < meals[x].dishes.size(); ++y) {
                                dishlist.add(meals[x].dishes.get(y));
                                pricelist.add(meals[x].prices.get(y));
                            }*/

                        }
                        cafeterialist.add(new CafeteriaItem(cafeterianame, CafeteriaMeals));

                    }

                    Gson gson = new Gson();

                    String prettyJson = gson.toJson(cafeterialist);

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("allitems", prettyJson);
                    editor.commit();

                    editor.putLong("TIME", new Date().getTime());
                    editor.commit();

                } catch (IOException e) {

                }
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void something) {
            System.out.println("Post Execute Called!!");
            //GetCafeterias(cafeterias);

            loadFromJson();


            System.out.println("LoadMeal in PostExecute: " + loadMeal);

            //System.out.println("Current menu status: " + menus.html());

            ComponentName thisWidget = new ComponentName(this.context.getPackageName(), WidgetActivity.class.getName());

            AppWidgetManager manager = AppWidgetManager.getInstance(this.context);

            if(updateRestaurant) {

                this.views.setViewVisibility(R.id.widgetcafeteria, View.GONE);

                views.setViewVisibility(R.id.cafeteriachoices, View.VISIBLE);

                cafeterialistopen = true;

                manager.updateAppWidget(thisWidget, views);

                updateRestaurant = false;
            }


            ArrayList<RemoteViews> mealholder = new ArrayList<RemoteViews>();
            RemoteViews textView1 = new RemoteViews(context.getPackageName(), R.id.headermeal1);
            RemoteViews textView2 = new RemoteViews(context.getPackageName(), R.id.headermeal2);
            RemoteViews textView3 = new RemoteViews(context.getPackageName(), R.id.headermeal3);
            RemoteViews textView4 = new RemoteViews(context.getPackageName(), R.id.headermeal4);
            RemoteViews textView5 = new RemoteViews(context.getPackageName(), R.id.headermeal5);
            mealholder.add(textView1);
            mealholder.add(textView2);
            mealholder.add(textView3);
            mealholder.add(textView4);
            mealholder.add(textView5);

            HashMap<RemoteViews, Integer> mealresource = new HashMap<RemoteViews, Integer>();
            mealresource.put(textView1, R.id.headermeal1);
            mealresource.put(textView2, R.id.headermeal2);
            mealresource.put(textView3, R.id.headermeal3);
            mealresource.put(textView4, R.id.headermeal4);
            mealresource.put(textView5, R.id.headermeal5);

            ArrayList<RemoteViews> menuholder = new ArrayList<RemoteViews>();
            RemoteViews menuTextView1 = new RemoteViews(context.getPackageName(), R.id.menu1);
            RemoteViews menuTextView2 = new RemoteViews(context.getPackageName(), R.id.menu2);
            RemoteViews menuTextView3 = new RemoteViews(context.getPackageName(), R.id.menu3);
            RemoteViews menuTextView4 = new RemoteViews(context.getPackageName(), R.id.menu4);
            RemoteViews menuTextView5 = new RemoteViews(context.getPackageName(), R.id.menu5);
            menuholder.add(menuTextView1);
            menuholder.add(menuTextView2);
            menuholder.add(menuTextView3);
            menuholder.add(menuTextView4);
            menuholder.add(menuTextView5);

            HashMap<RemoteViews, Integer> menuresource = new HashMap<RemoteViews, Integer>();
            menuresource.put(menuTextView1, R.id.menu1);
            menuresource.put(menuTextView2, R.id.menu2);
            menuresource.put(menuTextView3, R.id.menu3);
            menuresource.put(menuTextView4, R.id.menu4);
            menuresource.put(menuTextView5, R.id.menu5);

            this.views.setViewVisibility(menuresource.get(menuTextView1), View.GONE);
            this.views.setViewVisibility(menuresource.get(menuTextView2), View.GONE);
            this.views.setViewVisibility(menuresource.get(menuTextView3), View.GONE);
            this.views.setViewVisibility(menuresource.get(menuTextView5), View.GONE);
            this.views.setViewVisibility(menuresource.get(menuTextView4), View.GONE);


            for(int i = 0; i < mealholder.size(); ++i) {
                this.views.setTextColor(mealresource.get(mealholder.get(i)), context.getResources().getColor(R.color.LightSlateGray));
            }
            //this.views.removeAllViews(R.id.header);

            ArrayList<RemoteViews> cafeterianameholder = new ArrayList<RemoteViews>();
            RemoteViews cafeteriaTextView1 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice1);
            RemoteViews cafeteriaTextView2 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice2);
            RemoteViews cafeteriaTextView3 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice3);
            RemoteViews cafeteriaTextView4 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice4);
            RemoteViews cafeteriaTextView5 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice5);
            RemoteViews cafeteriaTextView6 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice6);
            RemoteViews cafeteriaTextView7 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice7);
            RemoteViews cafeteriaTextView8 = new RemoteViews(context.getPackageName(), R.id.cafeteriachoice8);

            cafeterianameholder.add(cafeteriaTextView8);
            cafeterianameholder.add(cafeteriaTextView7);
            cafeterianameholder.add(cafeteriaTextView6);
            cafeterianameholder.add(cafeteriaTextView5);
            cafeterianameholder.add(cafeteriaTextView4);
            cafeterianameholder.add(cafeteriaTextView3);
            cafeterianameholder.add(cafeteriaTextView2);
            cafeterianameholder.add(cafeteriaTextView1);

            HashMap<RemoteViews, Integer> cafeteriaresource = new HashMap<RemoteViews, Integer>();
            cafeteriaresource.put(cafeteriaTextView8, R.id.cafeteriachoice8);
            cafeteriaresource.put(cafeteriaTextView7, R.id.cafeteriachoice7);
            cafeteriaresource.put(cafeteriaTextView6, R.id.cafeteriachoice6);
            cafeteriaresource.put(cafeteriaTextView5, R.id.cafeteriachoice5);
            cafeteriaresource.put(cafeteriaTextView4, R.id.cafeteriachoice4);
            cafeteriaresource.put(cafeteriaTextView3, R.id.cafeteriachoice3);
            cafeteriaresource.put(cafeteriaTextView2, R.id.cafeteriachoice2);
            cafeteriaresource.put(cafeteriaTextView1, R.id.cafeteriachoice1);

            for(int i = 0; i < cafeterianameholder.size(); ++i) {
                RemoteViews textView = cafeterianameholder.get(i);

                if(i < cafeterialist.size()) {
                    this.views.setTextViewText(cafeteriaresource.get(textView), cafeterialist.get(i));
                } else {
                    this.views.setViewVisibility(cafeteriaresource.get(textView), View.GONE);
                }

            }

            if(settingRestaurant) {
                this.views.setViewVisibility(R.id.cafeteriachoices, View.GONE);

                RemoteViews textView = cafeterianameholder.get(cafeterialist.size() - cafeteriaindex);
                this.views.setTextViewText(R.id.widgetcafeteria, cafeterialist.get(cafeterialist.size() - cafeteriaindex));
                saveCafeteria(context, cafeterialist.get(cafeterialist.size() - cafeteriaindex));

                this.views.setViewVisibility(R.id.widgetcafeteria, View.VISIBLE);

                settingRestaurant = false;
                updateRestaurant = false;
                cafeterialistopen = false;
                cafeteriaindex = 0;

                SharedPreferences.Editor editor = pref.edit();

                editor.putInt("mealindex", 0);
                editor.commit();

            }

            String lastchoice = pref.getString("cafeterianame", cafeterialist.get(0));
            if(cafeterialist.isEmpty()) {

                System.out.println("Last Choice found!! " + lastchoice);
                cafeterialist.indexOf(lastchoice);

            }

            Meal[] meals = new Meal[0];
            for(CafeteriaItem item: jsonlist) {
                if(item.cafeterianame.equals(lastchoice)) {
                    meals = new Meal[item.meals.size()];
                    for(int i = 0; i < item.meals.size(); ++i) {
                        meals[i] = item.meals.get(i);
                    }
                }
            }

            ArrayList<Integer> indexlist = new ArrayList<Integer>();

            System.out.println("Meals found!: " + meals.length);

            for(int i = 0; i < meals.length; ++i) {
                if(meals[i].name.equals("공통찬")) continue;
                indexlist.add(i);
            }

            //Update widget issue starting here

            if(!nextMenu && !prevMenu) {
                SharedPreferences.Editor editor = pref.edit();

                editor.putInt("menuindex", 0);
                editor.commit();
            }

            for(int index = 0; index < indexlist.size(); index++) {
                //for(int i = 0; i < meals.length; i++) {
                int i = indexlist.get(index);
                RemoteViews textView = mealholder.get(index);//new RemoteViews(context.getPackageName(), R.layout.widgetheader);
                if(meals[i].name.equals("공통찬")) continue;

                this.views.setTextViewText(mealresource.get(textView), meals[i].name);
                this.views.setViewVisibility(mealresource.get(textView), View.VISIBLE);

                System.out.println("Updating name for meals: " + meals[i].name);

                System.out.println("Clicked Index here: " + clickedIndex);

                float scale = context.getResources().getDisplayMetrics().density;
                int dpAsPixels = (int) (4 * scale + 0.5f);

                textView.setViewPadding(R.id.headermeal, 0, dpAsPixels, 0, dpAsPixels);


                if(loadMeal) {

                    if(index == clickedIndex) {
                        System.out.println("CURRENT INDEX AST LOADMEAL IS: " + i);
                        this.views.setTextColor(mealresource.get(textView), context.getResources().getColor(R.color.White));
                        this.views.setInt(mealresource.get(textView), "setBackgroundResource", R.drawable.underline);
                        this.views.setViewPadding(mealresource.get(textView), 0, dpAsPixels, 0, dpAsPixels);
                        this.views.setTextViewText(R.id.pagercount, (pref.getInt("menuindex", 0) + 1) + " / " + meals[i].dishes.size());
                        System.out.println("Coloring for meal: " + meals[i].name);

                        if(nextMenu) {
                            RemoteViews menuTextView = menuholder.get(0);

                            int curindex = pref.getInt("menuindex", 1);
                            System.out.println("Current Menu Index: " + curindex);
                            int nextindex = 0;
                            if(curindex < meals[i].dishes.size() - 1) nextindex = curindex + 1;
                            if(curindex == meals[i].dishes.size() - 1) nextindex = 0;

                            saveMenuIndex(context, nextindex);

                            this.views.setTextViewText(menuresource.get(menuTextView), meals[i].dishes.get(nextindex));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);

                            this.views.setTextViewText(R.id.pagercount, (pref.getInt("menuindex", 0) + 1) + " / " + meals[i].dishes.size());

                            nextMenu = false;
                        } else if(prevMenu) {
                            RemoteViews menuTextView = menuholder.get(0);

                            int curindex = pref.getInt("menuindex", 1);
                            System.out.println("Current Menu Index: " + curindex);
                            int nextindex = 0;
                            if(curindex > 0) nextindex = curindex - 1;
                            if(curindex == 0) nextindex = meals[i].dishes.size() - 1;

                            saveMenuIndex(context, nextindex);

                            this.views.setTextViewText(menuresource.get(menuTextView), meals[i].dishes.get(nextindex));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);

                            this.views.setTextViewText(R.id.pagercount, (pref.getInt("menuindex", 0) + 1) + " / " + meals[i].dishes.size());

                            prevMenu = false;
                        } else {
                            RemoteViews menuTextView = menuholder.get(0);

                            int curindex = pref.getInt("menuindex", 0);
                            this.views.setTextViewText(menuresource.get(menuTextView), meals[i].dishes.get(curindex));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);
                        }

                        /*
                        for(int j = 0; j < meals[i].dishes.size() && j < 5; ++j) {
                            System.out.println("Setting meal name as: " + meals[i].dishes.get(j));
                            RemoteViews menuTextView = menuholder.get(j);
                            this.views.setTextViewText(menuresource.get(menuTextView), simpleDish(meals[i].dishes.get(j)));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);
                        }
                        for(int j = meals[i].dishes.size(); j < 5 && j < menuholder.size(); ++j) {
                            RemoteViews menuTextView = menuholder.get(j);
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.GONE);
                        }*/


                    }


                    //loadMeal = false;
                } else if(!loadMeal) {
                    int mealindex = pref.getInt("mealindex", 0);
                    clickedIndex = mealindex;

                    if(index == clickedIndex) {
                        System.out.println("CURRENT INDEX BEFORE LOADMEAL IS: " + i);
                        this.views.setTextColor(mealresource.get(textView), context.getResources().getColor(R.color.White));
                        this.views.setInt(mealresource.get(textView), "setBackgroundResource", R.drawable.underline);
                        this.views.setViewPadding(mealresource.get(textView), 0, dpAsPixels, 0, dpAsPixels);
                        this.views.setTextViewText(R.id.pagercount, (pref.getInt("menuindex", 0) + 1) + " / " + meals[i].dishes.size());
                        System.out.println("Coloring for meal: " + meals[i].name);


                        if (nextMenu) {
                            RemoteViews menuTextView = menuholder.get(0);

                            int curindex = pref.getInt("menuindex", 1);
                            System.out.println("Current Menu Index: " + curindex);
                            int nextindex = 0;
                            if (curindex < meals[i].dishes.size() - 1) nextindex = curindex + 1;
                            if (curindex == meals[i].dishes.size() - 1) nextindex = 0;

                            saveMenuIndex(context, nextindex);

                            this.views.setTextViewText(menuresource.get(menuTextView), meals[i].dishes.get(nextindex));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);

                            this.views.setTextViewText(R.id.pagercount, (pref.getInt("menuindex", 0) + 1) + " / " + meals[i].dishes.size());

                            nextMenu = false;
                        } else if (prevMenu) {
                            RemoteViews menuTextView = menuholder.get(0);

                            int curindex = pref.getInt("menuindex", 1);
                            System.out.println("Current Menu Index: " + curindex);
                            int nextindex = 0;
                            if (curindex > 0) nextindex = curindex - 1;
                            if (curindex == 0) nextindex = meals[i].dishes.size() - 1;

                            saveMenuIndex(context, nextindex);

                            this.views.setTextViewText(menuresource.get(menuTextView), meals[i].dishes.get(nextindex));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);

                            this.views.setTextViewText(R.id.pagercount, (pref.getInt("menuindex", 0) + 1) + " / " + meals[i].dishes.size());

                            prevMenu = false;
                        } else {
                            RemoteViews menuTextView = menuholder.get(0);

                            int curindex = pref.getInt("menuindex", 0);
                            this.views.setTextViewText(menuresource.get(menuTextView), meals[i].dishes.get(curindex));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);
                        }
                        /*
                        for(int j = 0; j < meals[i].dishes.size() && j < 5; ++j) {
                            System.out.println(meals[i].dishes.get(j));
                            RemoteViews menuTextView = menuholder.get(j);
                            this.views.setTextViewText(menuresource.get(menuTextView), meals[i].dishes.get(j));
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.VISIBLE);
                        }
                        for(int j = meals[i].dishes.size(); j < 5 && j < menuholder.size(); ++j) {
                            RemoteViews menuTextView = menuholder.get(j);
                            this.views.setViewVisibility(menuresource.get(menuTextView), View.GONE);
                        }*/

                    }
                }


                //manager.updateAppWidget(thisWidget, this.views);

                //manager.updateAppWidget(thisWidget, textView);


                //             AppWidgetManager.getInstance(this.context).updateAppWidget(
                //                     new ComponentName(this.context.getPackageName(), WidgetActivity.class.getName()), textView);

                //textView.setTextViewText(R.id.headermeal, "TextView number " + String.valueOf(i));


//                if(index == 0) {
//                    textView.setInt(R.id.headermeal, "setBackgroundResource", R.drawable.mealtimebackgroundleft);
//
//                    textView.setViewPadding(R.id.headermeal, 0, dpAsPixels, 0, dpAsPixels);
//                }

                //this.views.addView(R.id.header, textView);
            }

            for(int i = 0; i < mealholder.size(); ++i) {
                if(!indexlist.contains(i)) {
                    RemoteViews textView = mealholder.get(i);
                    this.views.setViewVisibility(mealresource.get(textView), View.GONE);
                }
            }


            if(meals.length == 0) {
                this.views.setViewVisibility(R.id.header, View.INVISIBLE);
                System.out.println("Information is currently not ready!");
                System.out.println("Setting header as invisible");
            } else {
                this.views.setViewVisibility(R.id.header, View.VISIBLE);
                System.out.println("Setting header as visible");
            }

            AppWidgetManager.getInstance(context).updateAppWidget(
                    new ComponentName(context, WidgetActivity.class), this.views);

            loadMeal = false;
            clickedIndex = 0;

            //Update widget issue ending here
        }
    }


}