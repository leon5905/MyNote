package yeohweizhu.mynote;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by yeohw on 2/12/2017.
 * Model Class for note.
 */

//TODO make sure to update parcelable method
public class Note implements Parcelable {
    public enum NoteStatus{NORMAL,ARCHIVE}; //Not using
    private NoteStatus mNoteStatus = NoteStatus.NORMAL; //Not using

    private String mContentText="";
    private String mTitleText="";
    private int mID;
    private boolean isPinned=false;
    private boolean isArchive=false;

    private boolean isNotified=false;
    private int mBackgroundColor;
    private Reminder mReminder;
    private ArrayList<CheckListItem> mCheckList;

    //TODO TAGGING, Drawings, Photos - Not enough time

    public Note(){
        mCheckList = new ArrayList<>();
        mReminder = new Reminder();
    }

    //region Parcelable implementation
    protected Note(Parcel in) {
        mCheckList= new ArrayList<>();

        //The parcelable object had to be the first one
        mReminder = (Reminder) in.readParcelable(Reminder.class.getClassLoader());
        mTitleText=in.readString();
        mContentText = in.readString();
        mID = in.readInt();
        mNoteStatus = NoteStatus.valueOf(in.readString());
        isPinned = in.readInt()==1?true:false;
        isArchive = in.readInt()==1?true:false;
        isNotified=  in.readInt()==1?true:false;
        mBackgroundColor = in.readInt();
        in.readTypedList(mCheckList, CheckListItem.CREATOR);
    }
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        //The parcelable object had to be the first one
        parcel.writeParcelable(mReminder,i);
        parcel.writeString(mTitleText);
        parcel.writeString(mContentText);
        parcel.writeInt(mID);
        parcel.writeString(mNoteStatus.toString());
        parcel.writeInt(isPinned ?1:0);
        parcel.writeInt(isArchive?1:0);
        parcel.writeInt(isNotified?1:0);
        parcel.writeInt(mBackgroundColor);
        parcel.writeTypedList(mCheckList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
    //endregion

    //region Getter and Setter
    public String getContentText() {
        return mContentText;
    }
    public void setContentText(String mText) {
        this.mContentText = mText;
    }

    public NoteStatus getNoteStatus() {
        return mNoteStatus;
    }
    public void setNoteStatus(NoteStatus noteStatus) {
        mNoteStatus = noteStatus;
    }

    public int getID() {
        return mID;
    }
    public void setID(int ID) {
        mID = ID;
    }

    public String getTitleText() {return mTitleText;}
    public void setTitleText(String titleText) {mTitleText = titleText;}

    public boolean isPinned() {return isPinned;}
    public void setPinned(boolean pinned) {
        isPinned = pinned;}

    public boolean isArchive() {return isArchive;}
    public void setArchive(boolean archive) {isArchive = archive;}

    public ArrayList<CheckListItem> getCheckList() {return mCheckList;}
    public void setCheckList(ArrayList<CheckListItem> checkList) {mCheckList = checkList;}

    public int getBackgroundColor() {return mBackgroundColor;}
    public void setBackgroundColor(int backgroundColor) {mBackgroundColor = backgroundColor;}

    public Reminder getReminder() {return mReminder;}
    public void setReminder(Reminder reminder) {mReminder = reminder;}

    public boolean isNotified() {return isNotified;}
    public void setNotified(boolean notified) {isNotified = notified;}
    //endregion

    public static class CheckListItem implements Parcelable{
        private String mItemText;
        private boolean isChecked;

        public static JSONObject serializeToJsonObject(CheckListItem item){
            JSONObject object = new JSONObject();

            try {
                object.put("mItemText",item.getItemText());
                object.put("isChecked",item.isChecked());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return object;
        }
        public static CheckListItem deserializeFromJsonObject(JSONObject jsonObject){
            CheckListItem item = new CheckListItem();
            try {
                item.setItemText((String)jsonObject.get("mItemText"));
                item.setChecked(jsonObject.getBoolean("isChecked"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return item;
        }
        public static JSONArray serializeToJsonArray(ArrayList<CheckListItem> itemList){
            JSONArray jsonArray= new JSONArray();

            for (CheckListItem item:itemList){
                jsonArray.put(CheckListItem.serializeToJsonObject(item));
            }

            return jsonArray;
        }
        public static ArrayList<CheckListItem> deserializeFromJsonArray(JSONArray jsonArray){
            ArrayList<CheckListItem> itemList = new ArrayList<>();

            try {
                for (int i = 0; i < jsonArray.length(); i++) {
                    itemList.add(CheckListItem.deserializeFromJsonObject(jsonArray.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return itemList;
        }

        public CheckListItem(){}

        protected CheckListItem(Parcel in) {
            mItemText = in.readString();
            isChecked = in.readByte() != 0;
        }

        public static final Creator<CheckListItem> CREATOR = new Creator<CheckListItem>() {
            @Override
            public CheckListItem createFromParcel(Parcel in) {
                return new CheckListItem(in);
            }

            @Override
            public CheckListItem[] newArray(int size) {
                return new CheckListItem[size];
            }
        };

        public String getItemText() {
            return mItemText;
        }

        public void setItemText(String itemText) {
            mItemText = itemText;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mItemText);
            parcel.writeByte((byte) (isChecked ? 1 : 0));
        }
    }

    public static class Reminder implements Parcelable{
        private Timestamp reminderTime; //Null if no reminder time

        //Other relevant information can be extended here like repeating daily,yearly etc.

        public Reminder(){
            reminderTime = null;
        }

        protected Reminder(Parcel in) {
            long time = in.readLong();
            if (time==0){
                reminderTime=null;
            }
            else{
                reminderTime = new Timestamp(time);
            }
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            if (reminderTime==null){
                parcel.writeLong(0);
            }
            else {
                parcel.writeLong(reminderTime.getTime());
            }
        }

        public static final Creator<Reminder> CREATOR = new Creator<Reminder>() {
            @Override
            public Reminder createFromParcel(Parcel in) {
                return new Reminder(in);
            }

            @Override
            public Reminder[] newArray(int size) {
                return new Reminder[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        public Timestamp getReminderTime() {
            return reminderTime;
        }
        public void setReminderTime(Timestamp reminderTime) {
            this.reminderTime = reminderTime;
        }
    }
}
