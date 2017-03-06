package yeohweizhu.mynote;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by yeohw on 3/4/2017.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static String TAG = "NotAlarmReceiver";
    public static String NOTE_ID = "NotificationNoteID";

    @Override
    public void onReceive(Context context, Intent intent) {
        int noteID = intent.getIntExtra(NOTE_ID,-1);

        if(noteID==-1){
            return;
        }
        else{
            String[] projection = NoteProvider.allSupportedColumns;
            Cursor cursor = context.getContentResolver().query(Uri.parse(NoteProvider.URL+"/"+String.valueOf(noteID)),projection,null,null,null);
            String content="Empty Content",title="Empty Title";

            if (!cursor.moveToFirst()){
                return; //Prevent empty data crashing
            }

            Note note = new Note();
            NoteService.parseCursorDataToNote(cursor,note);
            cursor.close();

            String contentText = note.getContentText();
            if (contentText!=null && !contentText.isEmpty()){
                content = contentText;
            }
            String titleText = note.getTitleText();
            if (titleText!=null && !titleText.isEmpty()){
                title = titleText;
            }

            note.setNotified(true);

            ContentValues values= NoteProvider.noteToContentValues(note,false);

            context.getContentResolver().update(
                    Uri.parse(NoteProvider.URL+"/"+String.valueOf(note.getID())), values,null,null);

            sendNotification(context,content,title,noteID);
        }
    }

    public static void sendNotification(Context context,String content,String title,int noteID){
        //Push notification
        NotificationManager mNM;
        mNM = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
        // Set the icon, scrolling text and timestamp
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_note_icon)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(context, FullDetailNoteActivity.class);
        resultIntent.putExtra(FullDetailNoteActivity.NOTE_ID,noteID);
        //Pass in noteID (instead of obj), to ensure the correct Note information is always displayed without need to update the Intent for notification

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(FullDetailNoteActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        noteID,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // noteID allows you to update the notification later on using the id;
        mNotificationManager.notify(noteID, mBuilder.build());
    }

    public static void scheduleAlarm(Context context,Note note){
        Intent intent = new Intent(context, NotificationAlarmReceiver.class);
        intent.putExtra(NotificationAlarmReceiver.NOTE_ID, note.getID());
        PendingIntent sender = PendingIntent.getBroadcast(context, note.getID(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        long targetTime = note.getReminder().getReminderTime().getTime();
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetTime, sender);
    }
}
