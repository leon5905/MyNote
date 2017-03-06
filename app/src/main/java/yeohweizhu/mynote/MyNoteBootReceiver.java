package yeohweizhu.mynote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by yeohw on 3/6/2017.
 */

public class MyNoteBootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String[] args = new String[]{"0"};
        Cursor cursor = context.getContentResolver().query(NoteProvider.CONTENT_URI,NoteProvider.allSupportedColumns,
                NoteDbHelper.NOTE_COLUMN_NAME_REMINDER_TIME+" > ?",args,null);

        if (cursor!=null){
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                Note note = new Note();
                NoteService.parseCursorDataToNote(cursor,note);

                if (!note.isNotified()){
                    NotificationAlarmReceiver.scheduleAlarm(context,note);
                }

                cursor.moveToNext();
            }
        }

        cursor.close();
    }
}
