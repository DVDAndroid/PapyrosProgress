package com.teinproductions.tein.papyrosprogress;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import java.util.Map;
import java.util.NoSuchElementException;

public abstract class AbstractProgressWidget extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        AlarmUtils.setAlarm(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateFromCache(context);
        context.sendBroadcast(new Intent(context, UpdateCheckReceiver.class));
    }

    @Override
    public void onDisabled(Context context) {
        AlarmUtils.reconsiderSettingAlarm(context);
    }

    public static void updateAppWidgets(Context context, Map<String, Integer> progressMap) {
        AppWidgetManager awManager = AppWidgetManager.getInstance(context);
        int[] appWidgetLargeIds = getAppWidgetLargeIds(context, awManager);
        int[] appWidgetSmallIds = getAppWidgetSmallIds(context, awManager);

        SharedPreferences pref = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String firstKey = null; // Default milestone to track with new widget
        try {
            firstKey = progressMap.keySet().iterator().next();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }

        for (int appWidgetId : appWidgetLargeIds) {
            String milestoneTitle = pref.getString(MainActivity.MILESTONE_WIDGET_PREFERENCE + appWidgetId, firstKey);
            Integer progress = progressMap.get(milestoneTitle);
            if (progress == null) continue;
            RemoteViews views = ProgressWidgetLarge.getRemoteViews(context, appWidgetId, progress);
            if (views != null) {
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
            }
        }
        for (int appWidgetSmallId : appWidgetSmallIds) {
            String milestoneTitle = pref.getString(MainActivity.MILESTONE_WIDGET_PREFERENCE + appWidgetSmallId, firstKey);
            Integer progress = progressMap.get(milestoneTitle);
            if (progress == null) continue;
            RemoteViews views = ProgressWidgetSmall.getRemoteViews(context, appWidgetSmallId, progress);
            if (views != null) {
                AppWidgetManager.getInstance(context).updateAppWidget(appWidgetSmallId, views);
            }
        }
    }

    private static int[] getAppWidgetLargeIds(Context context, AppWidgetManager awManager) {
        ComponentName componentName = new ComponentName(context, ProgressWidgetLarge.class);
        return awManager.getAppWidgetIds(componentName);
    }

    private static int[] getAppWidgetSmallIds(Context context, AppWidgetManager awManager) {
        ComponentName componentName = new ComponentName(context, ProgressWidgetSmall.class);
        return awManager.getAppWidgetIds(componentName);
    }

    public static boolean areAppWidgetsEnabled(Context context) {
        AppWidgetManager awManager = AppWidgetManager.getInstance(context);
        int[] appWidgetLargeIds = getAppWidgetLargeIds(context, awManager);
        int[] appWidgetSmallIds = getAppWidgetSmallIds(context, awManager);

        return !(appWidgetLargeIds.length == 0 && appWidgetSmallIds.length == 0);
    }

    /**
     * Update all app widgets without loading the progress from the Internet.
     * Used for example when the text size of a widget is updated, or when a widget
     * is first instantiated.
     */
    public static void updateFromCache(Context context) {
        String cache = MainActivity.getCache(context);
        if (cache == null) return;
        Map<String, Integer> progress = JSONUtils.getProgressMap(cache);
        updateAppWidgets(context, progress);
    }
}
