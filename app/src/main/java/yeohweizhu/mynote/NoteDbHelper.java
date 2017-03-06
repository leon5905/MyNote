package yeohweizhu.mynote;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yeohw on 2/19/2017.
 */

public class NoteDbHelper extends SQLiteOpenHelper{
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 11;
    public static final String DATABASE_NAME = "Note.db";

    //TODO add more columns
    public static final String NOTE_TABLE_NAME = "Note";
    public static final String NOTE_COLUMN_NAME_ID = "id";
    public static final String NOTE_COLUMN_NAME_TITLE = "title";
    public static final String NOTE_COLUMN_NAME_CONTENT = "content";
    public static final String NOTE_COLUMN_NAME_PINNED = "pinned";
    public static final String NOTE_COLUMN_NAME_CHECKLIST = "checklist";
    public static final String NOTE_COLUMN_NAME_ARCHIVE = "archive";
    public static final String NOTE_COLUMN_NAME_COLOR = "backgroundcolor";
    public static final String NOTE_COLUMN_NAME_REMINDER_TIME= "reminderTime";
    public static final String NOTE_COLUMN_NAME_REMINDER_BOOLEAN = "reminderBoolean";

    //TODO add more columns
    private static final String NOTE_SQL_CREATE_TABLE =
            "CREATE TABLE " + NOTE_TABLE_NAME + " (" +
                    NOTE_COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    NOTE_COLUMN_NAME_TITLE + " TEXT," +
                    NOTE_COLUMN_NAME_CONTENT + " TEXT," +
                    NOTE_COLUMN_NAME_PINNED + " INTEGER," +
                    NOTE_COLUMN_NAME_ARCHIVE + " INTEGER," +
                    NOTE_COLUMN_NAME_COLOR + " INTEGER," +
                    NOTE_COLUMN_NAME_REMINDER_TIME + " INTEGER,"+
                    NOTE_COLUMN_NAME_REMINDER_BOOLEAN + " INTEGER,"+
                    NOTE_COLUMN_NAME_CHECKLIST + " TEXT)";

    private static final String NOTE_SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + NOTE_TABLE_NAME;

    private SQLiteDatabase mSQLiteDatabase;

    public NoteDbHelper(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NOTE_SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //For development Purposes, simply drop the table and create new one
        db.execSQL(NOTE_SQL_DELETE_TABLE);
        db.execSQL(NOTE_SQL_CREATE_TABLE);

//          //Incremental Upgrade logic (for future use, after apps have been releases)
//          switch(oldVersion) {
//              case 1:
//                  //upgrade logic from version 1 to 2
//              case 2:
//                  //upgrade logic from version 2 to 3
//              case 3:
//                  //upgrade logic from version 3 to 4
//                  break;
//              default:
//                  throw new IllegalStateException(
//                          "onUpgrade() with unknown oldVersion " + oldVersion);
//          }
    }

    public SQLiteDatabase open() throws SQLException{
        if (mSQLiteDatabase !=null && mSQLiteDatabase.isOpen()){
        }
        else{
            mSQLiteDatabase = this.getWritableDatabase();
        }

        return mSQLiteDatabase;
    }

    //Android already close the database connection if process is terminated. Call this if the operation is one-time only to prevent multiple opnening database overhead in frequent database operation.
    //TODO check with widget database retrieval and writing to comfirm this information
    public void close(){
        mSQLiteDatabase.close();
        mSQLiteDatabase=null;
    }
}

