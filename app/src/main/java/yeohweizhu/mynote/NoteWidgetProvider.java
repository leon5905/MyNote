package yeohweizhu.mynote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * Created by yeohw on 3/3/2017.
 */

public class NoteWidgetProvider extends AppWidgetProvider{
    private static final String ACTION_CLICK = "ACTION_CLICK";

    //Called on startup if no configuration activity is defined (optional), or called at specified time internal (always),
    // after boot complete, this method is automatically called
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                NoteWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key_widget),Context.MODE_PRIVATE);
            int noteID = sharedPref.getInt(String.valueOf(widgetId),-1);

            Note note = null;
            if (noteID>-1) {
                Cursor cursor = context.getContentResolver().query(Uri.parse(NoteProvider.URL + "/" + noteID), NoteProvider.allSupportedColumns, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    if (!cursor.isAfterLast()) {
                        note = new Note();
                        NoteService.parseCursorDataToNote(cursor, note);
                    }
                    cursor.close();
                }
            }

            RemoteViews remoteViews=null;
            if (note==null){
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(String.valueOf(widgetId),-1);
                editor.commit();

                remoteViews= bindEmpty(context,widgetId);
            }
            else{
                remoteViews =bindData(context,note);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    public static RemoteViews bindData(Context context, Note note){
        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
//        view.setTextViewText(R.id.widget_note_id,"??");
//        view.setTextViewText(R.id.widget_note_id_2,note.getContentText());

        if (note.getTitleText()!=null && !note.getTitleText().isEmpty()) {
            view.setViewVisibility(R.id.widget_summary_title, View.VISIBLE);
            view.setTextViewText(R.id.widget_summary_title, note.getTitleText());
        }
        else{
            view.setViewVisibility(R.id.widget_summary_title, View.GONE);
            view.setTextViewText(R.id.widget_summary_title, "");
        }

        if (note.getContentText()!=null && !note.getContentText().isEmpty()) {
            view.setViewVisibility(R.id.widget_summary_content, View.VISIBLE);
            view.setTextViewText(R.id.widget_summary_content, note.getContentText());
        }
        else{
            view.setViewVisibility(R.id.widget_summary_content, View.GONE);
            view.setTextViewText(R.id.widget_summary_content, "");
        }

        if (note.getReminder().getReminderTime()!=null){

            Date date = new Date(note.getReminder().getReminderTime().getTime());
            SimpleDateFormat formatter=new SimpleDateFormat("dd MMM yy, h:mm a ");
            String str = formatter.format(date);
            view.setTextViewText(R.id.widget_summary_reminder,"Reminder: " + str);
        }

        view.setInt(R.id.widget_summary_layout, "setBackgroundColor", note.getBackgroundColor());
        view.removeAllViews(R.id.widget_summary_checklist_layout);

        if (note.getCheckList().size()>0) {
            view.setViewVisibility(R.id.widget_summary_checklist_layout,View.VISIBLE);

            for (Note.CheckListItem item : note.getCheckList()) {
                RemoteViews checkListItemView = new RemoteViews(context.getPackageName(), R.layout.widget_checklist_item);
                checkListItemView.setTextViewText(R.id.widget_checklist_item_text, item.getItemText());

                if (item.isChecked())
                    checkListItemView.setImageViewResource(R.id.widget_checklist_item_checkbox, R.drawable.checkbox_checked);
                else
                    checkListItemView.setImageViewResource(R.id.widget_checklist_item_checkbox, R.drawable.checkbox_unchecked);

                view.addView(R.id.widget_summary_checklist_layout, checkListItemView);
            }
        }
        else{
            view.setViewVisibility(R.id.widget_summary_checklist_layout,View.GONE);
        }

        Intent configIntent = new Intent(context, FullDetailNoteActivity.class);
        configIntent.putExtra(FullDetailNoteActivity.NOTE_ID,note.getID());
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, note.getID(), configIntent, 0);
        view.setOnClickPendingIntent(R.id.widget_summary_layout, configPendingIntent);

        return view;
    }

    public static RemoteViews bindEmpty(Context context,int appWidgetID){
//        Note note = new Note();
//        note.setBackgroundColor(ContextCompat.getColor(context,R.color.colorWhite));
//        ContentValues values = NoteProvider.noteToContentValues(note,true);
//        note.setID(Integer.valueOf(context.getContentResolver().insert(
//                NoteProvider.CONTENT_URI, values).getLastPathSegment())); BUGGY, activity binding error

        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
//        view.setTextViewText(R.id.widget_note_id,"??");
//        view.setTextViewText(R.id.widget_note_id_2,note.getContentText());

        view.setTextViewText(R.id.widget_summary_title,"Note Deleted");
        view.setViewVisibility(R.id.widget_summary_title, View.VISIBLE);
        view.setViewVisibility(R.id.widget_summary_content, View.VISIBLE);
        view.setTextViewText(R.id.widget_summary_content,"Click Here to select a new one");
        view.removeAllViews(R.id.widget_summary_checklist_layout);
        view.setTextViewText(R.id.widget_summary_reminder,"");

        Intent configIntent = new Intent(context, NoteWidgetConfigurationActivity.class);
        Bundle b = new Bundle();
        b.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetID);
        configIntent.putExtras(b);
//        configIntent.putExtra(FullDetailNoteActivity.NOTE_ID,note.getID());//-1 request code mean no note, noteID too buggy, no enough time to implement
        PendingIntent configPendingIntent = PendingIntent.getActivity(context,appWidgetID, configIntent, 0);
        view.setOnClickPendingIntent(R.id.widget_summary_layout, configPendingIntent);

        return view;
    }

    public static void updateWidgets(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), NoteWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, NoteWidgetProvider.class));

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
//            widgetManager.notifyAppWidgetViewDataChanged(ids, android.R.id.list);

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }


}
