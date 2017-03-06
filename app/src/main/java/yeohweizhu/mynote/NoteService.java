package yeohweizhu.mynote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yeohw on 2/19/2017.
 * NoteService store global note list
 * Will query content provider for all related operation
 */

public class NoteService{
    public final static String TAG ="NOTESERVICE";

    private static NoteService sNoteService;

    //Make sure to pass in proper context for context resolver lookup
    public static NoteService getInstance(Context context){
        if (sNoteService==null){
            sNoteService = new NoteService();
            sNoteService.mContext = context;
        }

        if (!sNoteService.isInit) {
            sNoteService.init();
            sNoteService.isInit=true;
        }
        return sNoteService;
    }

//    public static NoteService getInstanceWithoutInit(Context context){
//        if (sNoteService==null){
//            sNoteService = new NoteService();
//            sNoteService.mContext = context;
//        }
//
//        return sNoteService;
//    }

    private List<Note> mNoteList;
    private List<Note> mArchiveNoteList;
    private Context mContext;
    private boolean isInit=false;

    private NoteService(){

    }

    private void init(){
        HashMap<Integer, Note> tempNoteHashMap = new HashMap<>();
        mNoteList = new ArrayList<>();

        String[] projection = NoteProvider.allSupportedColumns;
        Cursor cursor = mContext.getContentResolver().query(NoteProvider.CONTENT_URI, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Note newNote = new Note();
                parseCursorDataToNote(cursor,newNote);
                tempNoteHashMap.put(newNote.getID(), newNote);
                cursor.moveToNext();
            }

            //Reorder NoteList
            for (int id : this.getNoteOrder()) {
                Note note = tempNoteHashMap.remove(id);
                if (note != null) {
                    mNoteList.add(note);
                }
            }

            for (Map.Entry<Integer,Note> entry:tempNoteHashMap.entrySet()){
                mNoteList.add(entry.getValue());
            }

            cursor.close();
        }

        Log.d(TAG, String.valueOf(mNoteList.size()));
    }

    //Special static method to get only a single note information. Useful for notification, widget etc to retrieve single information
    public static Note getNote(Context context,int noteID){
        Note newNote = null;

        String[] projection = NoteProvider.allSupportedColumns;
        Cursor cursor = context.getContentResolver().query(Uri.parse(NoteProvider.URL+"/"+String.valueOf(noteID))
                , projection, null, null,
                null);
        if (cursor != null) {

            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                newNote = new Note();

                parseCursorDataToNote(cursor,newNote);
                //cursor.moveToNext();
            }

            cursor.close();
        }

        return newNote;
    }

    //TODO add more columns
    public static void parseCursorDataToNote(Cursor cursor,Note newNote){
        newNote.setID(cursor.getInt(cursor
                .getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_ID)));
        newNote.setContentText(cursor.getString
                (cursor.getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_CONTENT)));
        newNote.setTitleText(cursor.getString(cursor
                .getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_TITLE)));

        int pinnedInt = cursor.getInt(cursor
                .getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_PINNED));
        newNote.setPinned(pinnedInt == 1 ? true : false);

        int archiveInt = cursor.getInt(cursor
                .getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_ARCHIVE));
        newNote.setArchive(archiveInt == 1 ? true : false);

        int notifiedInt = cursor.getInt(cursor
                .getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_REMINDER_BOOLEAN));
        newNote.setNotified(notifiedInt == 1 ? true : false);

        newNote.setBackgroundColor(cursor.getInt(cursor
                .getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_COLOR)));

        //Reminder
        long reminderLong = cursor.getLong(cursor
                .getColumnIndexOrThrow(NoteDbHelper.NOTE_COLUMN_NAME_REMINDER_TIME));
        Note.Reminder reminder = new Note.Reminder();
        Timestamp timestamp;
        if (reminderLong == 0) {
            timestamp = null;
        } else {
            timestamp = new Timestamp(reminderLong);
        }
        reminder.setReminderTime(timestamp);
        newNote.setReminder(reminder);

        //Checklist
        JSONArray jsonArray = null;
        int columnIndex = cursor.getColumnIndex(NoteDbHelper.NOTE_COLUMN_NAME_CHECKLIST);
        if (columnIndex != -1) {
            String str = cursor.getString(columnIndex);

            if (str != null && !str.isEmpty()) {
                try {
                    Log.d(TAG,str);
                    jsonArray = new JSONArray(str);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                newNote.setCheckList(Note.CheckListItem.deserializeFromJsonArray(jsonArray));
            }
        }
    }


    public List<Note> getNoteList() {
        return mNoteList;
    }

    //Get note list without any header
    public List<Note> getPureNoteList(){
        return mNoteList;
    }

    public void addNote(Note newNote){
        ContentValues values= NoteProvider.noteToContentValues(newNote,true);

        newNote.setID(Integer.valueOf(mContext.getContentResolver().insert(
                NoteProvider.CONTENT_URI, values).getLastPathSegment()));
        mNoteList.add(newNote);

        onNoteListChanged(mNoteList.size()-1, ListItemChangedArgs.Status.INSERTED);
    }

    public void editNote(Note editNote){
        ContentValues values= NoteProvider.noteToContentValues(editNote,false);

        mContext.getContentResolver().update(
                Uri.parse(NoteProvider.URL+"/"+String.valueOf(editNote.getID())), values,null,null);

        int noteIndex = findNoteIndexById(editNote.getID());
        if (noteIndex>-1) {
            mNoteList.set(noteIndex, editNote);
            onNoteListChanged(noteIndex, ListItemChangedArgs.Status.CHANGED);
        }
    }
    public void editNoteWithoutUpdatingNotified(Note editNote) {
        ContentValues values= NoteProvider.noteToContentValuesWithoutNotified(editNote,false);

        mContext.getContentResolver().update(
                Uri.parse(NoteProvider.URL+"/"+String.valueOf(editNote.getID())), values,null,null);

        int noteIndex = findNoteIndexById(editNote.getID());
        if (noteIndex>-1) {
            mNoteList.set(noteIndex, editNote);
            onNoteListChanged(noteIndex, ListItemChangedArgs.Status.CHANGED);
        }
    }

    public void deleteNote(Note deleteNote){
        int noteIndex = findNoteIndexById(deleteNote.getID());

        ContentValues values= NoteProvider.noteToContentValues(deleteNote,false);
        mContext.getContentResolver().delete(
                Uri.parse(NoteProvider.URL+"/"+String.valueOf(deleteNote.getID())),null,null);

        if (noteIndex>-1) { //Widget might not initialize the note list
            mNoteList.remove(noteIndex);
            onNoteListChanged(noteIndex, ListItemChangedArgs.Status.DELETED);
        }
    }

    private int findNoteIndexById(int id){
        for (int i=0;i<mNoteList.size();i++){
            if (mNoteList.get(i).getID()==id)
                return i;
        }

        return -1; //Not Found
    }

    public void saveNoteOrder(){
        //Save ordering to single XML file instead of updating the position value to each individual element because it should be faster operation than a database
        ContentValues values = new ContentValues();
        String concatString="";
        for (Note n:mNoteList){
            concatString+=String.valueOf(n.getID()) + " ";
        }

        if (concatString.isEmpty()) concatString="1"; //Fix conversion error, does not matter because it will be simply ignored if note is empty
        values.put(NoteProvider.CONCAT_STRING_ORDER,concatString);

        mContext.getContentResolver().insert(NoteProvider.CONTENT_ORDER_URI,values);
    }
    public List<Integer> getNoteOrder(){
        List<Integer> intList = new ArrayList<>();
        Cursor cursor = mContext.getContentResolver().query(NoteProvider.CONTENT_ORDER_URI, null, null, null, null);

        if (cursor!=null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                intList.add(cursor.getInt(i));
            }
        }

        return intList;
    }

    //region EventHandling
    private List<INoteServiceListChangedEventListener> listenerList = new ArrayList<INoteServiceListChangedEventListener>();
    private void onNoteListChanged(int position, ListItemChangedArgs.Status status){
        for(INoteServiceListChangedEventListener listener :listenerList){
            if (listener!=null)
                listener.NoteListItemChangedEvent(this,new ListItemChangedArgs(position,status));
        }
    }

    //Keep track of listener
    public void addOnNoteListChangedListener(INoteServiceListChangedEventListener listener){
        listenerList.add(listener);
    }

    public void unsubscribeOnNoteListChangedListener(INoteServiceListChangedEventListener listener){
        listenerList.remove(listener);
    }

    //Event Handling (Broadcasting)
    public interface INoteServiceListChangedEventListener { //Observer Pattern
        void NoteListItemChangedEvent(Object sender, ListItemChangedArgs eventArgs);
    }

    public static class ListItemChangedArgs { //Event Argument
        public int position;
        public enum Status{NONE,INSERTED,CHANGED,DELETED};
        public Status status;

        public ListItemChangedArgs(int position, Status status){
            this.position = position;
            this.status =status;
        }
    }
    //endregion

    //region Parcelable
//    protected NoteService(Parcel in) {
//        mNoteList = in.createTypedArrayList(Note.CREATOR);
//
//    }
//
//    public static final Creator<NoteService> CREATOR = new Creator<NoteService>() {
//        @Override
//        public NoteService createFromParcel(Parcel in) {
//            return new NoteService(in);
//        }
//
//        @Override
//        public NoteService[] newArray(int size) {
//            return new NoteService[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel parcel, int i) {
//        parcel.writeTypedList(mNoteList);
//    }
//    //endregion
}
