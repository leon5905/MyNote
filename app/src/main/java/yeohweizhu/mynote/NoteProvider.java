package yeohweizhu.mynote;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by yeohw on 2/28/2017.
 */

public class NoteProvider extends ContentProvider {
    static final String AUTHORITY = "yeohweizhu.mynote.NoteProvider";
    static final String BASE_PATH = "note";
    static final String URL = "content://" + AUTHORITY + "/" + BASE_PATH;
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String CONCAT_STRING_ORDER = "noteOrderSeperatedBySpace";
    static final String JSON_FILE_NAME = "noteOrder.JSON";
    static final String BASE_ORDER_PATH = "noteOrder";
    static final String ORDER_URL = "content://" + AUTHORITY + "/" + BASE_ORDER_PATH;
    static final Uri CONTENT_ORDER_URI = Uri.parse(ORDER_URL);

    static final int NOTE = 1;
    static final int NOTE_ID = 2;
    static final int NOTE_ORDER_ID=3;

    //TODO add more columns
    static final String[] allSupportedColumns ={ NoteDbHelper.NOTE_COLUMN_NAME_ID,
            NoteDbHelper.NOTE_COLUMN_NAME_CONTENT, NoteDbHelper.NOTE_COLUMN_NAME_TITLE,
            NoteDbHelper.NOTE_COLUMN_NAME_PINNED,NoteDbHelper.NOTE_COLUMN_NAME_CHECKLIST,
            NoteDbHelper.NOTE_COLUMN_NAME_ARCHIVE, NoteDbHelper.NOTE_COLUMN_NAME_COLOR,
            NoteDbHelper.NOTE_COLUMN_NAME_REMINDER_TIME,NoteDbHelper.NOTE_COLUMN_NAME_REMINDER_BOOLEAN};

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/note";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/note_id";

    static final UriMatcher sURIMatcher;
    static{
        sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, NOTE);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH+"/#", NOTE_ID);
        sURIMatcher.addURI(AUTHORITY,BASE_ORDER_PATH,NOTE_ORDER_ID);
    }

    NoteDbHelper mNoteDbHelper;

    @Override
    public boolean onCreate() {
        mNoteDbHelper = new NoteDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        if (NOTE_ORDER_ID == sURIMatcher.match(uri)){
            Cursor cursor = loadOrderFromFile();
            if (cursor!=null)
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(NoteDbHelper.NOTE_TABLE_NAME);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case NOTE:
                break;
            case NOTE_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(NoteDbHelper.NOTE_COLUMN_NAME_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        //Database it cached no need to close
        SQLiteDatabase db = mNoteDbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        if (cursor!=null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }
    private void checkColumns(String[] projection) {
        String[] available = allSupportedColumns;
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(
                    Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(
                    Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = mNoteDbHelper.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case NOTE:
                id = db.insert(NoteDbHelper.NOTE_TABLE_NAME, null, values);
                break;
            case NOTE_ORDER_ID:
                saveOrderToFile(values);
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_ORDER_PATH );
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = mNoteDbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case NOTE:
                rowsDeleted = db.delete(NoteDbHelper.NOTE_TABLE_NAME, whereClause,
                        whereArgs);
                break;
            case NOTE_ID:
                //TODO Check if any problem here
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(whereClause)) {
                    rowsDeleted = db.delete(
                            NoteDbHelper.NOTE_TABLE_NAME,
                            NoteDbHelper.NOTE_COLUMN_NAME_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = db.delete(
                            NoteDbHelper.NOTE_TABLE_NAME,
                            NoteDbHelper.NOTE_COLUMN_NAME_ID + "=" + id
                                    + " and " + whereClause, whereArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = mNoteDbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case NOTE:
                rowsUpdated = db.update(mNoteDbHelper.NOTE_TABLE_NAME,
                        values,
                        whereClause,
                        whereArgs);
                break;
            case NOTE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(whereClause)) {
                    rowsUpdated = db.update(mNoteDbHelper.NOTE_TABLE_NAME,
                            values,
                            mNoteDbHelper.NOTE_COLUMN_NAME_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = db.update(mNoteDbHelper.NOTE_TABLE_NAME,
                            values,
                            mNoteDbHelper.NOTE_COLUMN_NAME_ID + "=" + id
                                    + " and "
                                    + whereClause, whereArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    public static ContentValues noteToContentValuesWithoutNotified(Note note,boolean isNew){
        return noteToContentValuBaseMethod(note,isNew,false);
    }
    public static ContentValues noteToContentValues(Note note, boolean isNew){
        return noteToContentValuBaseMethod(note,isNew,true);
    }
    private static ContentValues noteToContentValuBaseMethod(Note note,boolean isNew, boolean includeNotified){
        ContentValues values = new ContentValues();
        if (!isNew)
            values.put(NoteDbHelper.NOTE_COLUMN_NAME_ID,note.getID()); //Allow new id to be auto selected by db

        //TODO add  more columns
        values.put(NoteDbHelper.NOTE_COLUMN_NAME_TITLE, note.getTitleText());
        values.put(NoteDbHelper.NOTE_COLUMN_NAME_CONTENT, note.getContentText());
        values.put(NoteDbHelper.NOTE_COLUMN_NAME_PINNED,note.isPinned()?1:0);
        values.put(NoteDbHelper.NOTE_COLUMN_NAME_ARCHIVE,note.isArchive()?1:0);
        values.put(NoteDbHelper.NOTE_COLUMN_NAME_COLOR,note.getBackgroundColor());
        if (includeNotified)
            values.put(NoteDbHelper.NOTE_COLUMN_NAME_REMINDER_BOOLEAN,note.isNotified()?1:0);
        //Reminder time and checklist already supported below.

        long timestamp;
        if (note.getReminder().getReminderTime()==null){
            timestamp=0;
        }
        else{
            timestamp = note.getReminder().getReminderTime().getTime();
        }
        values.put(NoteDbHelper.NOTE_COLUMN_NAME_REMINDER_TIME,timestamp);

        ArrayList<Note.CheckListItem> checkList = note.getCheckList();
        if (checkList!=null && checkList.size()!=0){ //Check whether or not to put in string value
            values.put(NoteDbHelper.NOTE_COLUMN_NAME_CHECKLIST,Note.CheckListItem.serializeToJsonArray(checkList).toString());
        }
        else{
            values.putNull(NoteDbHelper.NOTE_COLUMN_NAME_CHECKLIST);
        }

        return values;
    }

    private void saveOrderToFile(ContentValues values){
        List<Integer> intArray = new ArrayList<>();
        String concatStr = values.getAsString(NoteProvider.CONCAT_STRING_ORDER);
        String[] individualStr  = concatStr.split(" ");
        for (String str:individualStr){
            intArray.add(Integer.valueOf(str));
        }

        createAndSaveFile(JSON_FILE_NAME,serializeIntegerListToJsonArray(intArray).toString());
    }
    private Cursor loadOrderFromFile(){
        MatrixCursor cursor=null;

        JSONArray jsonArray = readJsonFile(JSON_FILE_NAME);
        if (jsonArray==null)
            return null;

        else {
            List<Integer> intList = deserializeFromJsonArrayToListInteger(jsonArray);
            cursor = new MatrixCursor(new String[intList.size()]);
            cursor.addRow(intList);
        }

        return cursor;
    }

    public static JSONArray serializeIntegerListToJsonArray(List<Integer> intArray){
        JSONArray jsonArray= new JSONArray();

        for (Integer intValue:intArray){
            jsonArray.put(intValue);
        }

        return jsonArray;
    }
    public static List<Integer> deserializeFromJsonArrayToListInteger(JSONArray jsonArray){
        ArrayList<Integer> intList = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                intList.add(jsonArray.getInt(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return intList;
    }
    private void createAndSaveFile(String fileName, String jSon) {
        try {
            FileWriter file = new FileWriter("/data/data/" + getContext().getPackageName() + "/" + fileName);
            file.write(jSon);
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private JSONArray readJsonFile(String fileName) {
        try {
            File f = new File("/data/data/" + getContext().getPackageName() + "/" + fileName);
            if (f.exists()) {
                FileInputStream is = new FileInputStream(f);
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String JSONString = new String(buffer);
                JSONArray jsonArray = new JSONArray(JSONString);
                return jsonArray;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
