package yeohweizhu.mynote;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static yeohweizhu.mynote.IDialogCompleteListener.DialogResponse.POSITIVE;

/**
 * Full Detail Note
 * Full Edit
 * Enter after user click on the summary note
 * If intent note == null, add new note
 */
public class FullDetailNoteActivity extends AppCompatActivity implements IDialogCompleteListener{
    public static final String TAG = "FullDetailNoteActivity";
    public static final String NOTEOBJ = "NOTEOBJ";
    public static final String NOTE_ID="FullDetailNoteActivity_Note_ID";
    private static final String IS_REMINDER_DIRTY="Is_Reminder_Dirty";

    NoteService mNoteService;
    Note mNote;
    Boolean isNewNote;
    Boolean isDelete;

    //All GUI references
    EditText mContentEditText;
    EditText mTitleEditText;
    LinearLayout mReminderLayout;
    ImageView mReminderImageView;
    TextView mReminderTextView;
    ImageButton mReminderToolbarImageButton;
    RecyclerView mUncheckedListRecyclerView;
    NoteChecklistAdapter mUncheckedAdapter;
    RecyclerView mCheckedListRecyclerView;
    NoteChecklistAdapter mCheckedAdapter;
    View mLineSeparator;
    Button mCheckedListAddItemButton;
    NestedScrollView mNestedScrollView;
    LinearLayout mLinearLayoutBottomBar;
    Toolbar mToolbar;

    //Dialog UI Dirty Flag
    private boolean isReminderDialogDirty;
    private DialogResponse mDialogResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_detail_note);

        //Instantiate Note Object
        if (savedInstanceState==null) {
            Intent tempIntent = getIntent();
            if (tempIntent!=null){
                Bundle tempBundle = tempIntent.getExtras();
                if (tempBundle!=null){
                    mNote = (Note) tempBundle.getParcelable(NOTEOBJ);
                }
            }
        }
        else{
            mNote = (Note) savedInstanceState.getParcelable(NOTEOBJ);
            isReminderDialogDirty = savedInstanceState.getBoolean(IS_REMINDER_DIRTY);
        }

        if (mNote==null){
            //Check for note id
            Intent tempIntent = getIntent();
            if (tempIntent!=null) {
                int noteID = tempIntent.getIntExtra(NOTE_ID, -1);
                Log.d(TAG, String.valueOf(noteID));
                if (noteID!=-1){
                    //Need to retrieve note myself
                    mNote = NoteService.getNote(this,noteID);

                    //Note already deleted.
                    if (mNote==null){
                        isNewNote = true;
                        mNote = new Note();
                        mNote.setBackgroundColor(ContextCompat.getColor(this,R.color.colorWhite));
                    }

                    isNewNote=false;
                }
                else{
                    isNewNote = true;
                    mNote = new Note();
                    mNote.setBackgroundColor(ContextCompat.getColor(this,R.color.colorWhite));
                    //New Note confirmed!
                }
            }
        }
        else{
            isNewNote = false; //Note Loaded from intent
        }

        isDelete=false;

        mNoteService = NoteService.getInstance(getApplicationContext());

        //Toolbar setting
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        mToolbar = toolbar;
        setSupportActionBar(toolbar);
        ActionBar tempBar = getSupportActionBar();
        tempBar.setDisplayHomeAsUpEnabled(true);
        tempBar.setDisplayShowHomeEnabled(true);
        tempBar.setTitle("");

        //Setting up GUI and binding its data
        mCheckedListAddItemButton = (Button) findViewById(R.id.activity_full_detail_note_checklist_add_item);
        mLineSeparator = (View) findViewById(R.id.activity_full_detail_note_checklist_line_separator);
        if (mNote.getCheckList().size() == 0) {
            mLineSeparator.setVisibility(View.GONE);
            mCheckedListAddItemButton.setVisibility(View.GONE);
        }

        mContentEditText= (EditText) findViewById(R.id.activity_full_detail_note_content);
        mTitleEditText = (EditText) findViewById(R.id.activity_full_detail_note_title);
        mNestedScrollView = (NestedScrollView) findViewById(R.id.activity_full_detail_note_nested_scroll_view);
        mLinearLayoutBottomBar= (LinearLayout) findViewById(R.id.activity_full_detail_note_toolbar_bottom_layout);

        mReminderImageView= (ImageView) findViewById(R.id.activity_full_detail_note_reminder_image);
        mReminderLayout = (LinearLayout) findViewById(R.id.activity_full_detail_note_reminder_layout);
        mReminderTextView = (TextView) findViewById(R.id.activity_full_detail_note_reminder_text);
        mReminderToolbarImageButton = (ImageButton) findViewById(R.id.activity_full_detail_note_toolbar_reminder);
        mReminderLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick");
                FullDetailNoteActivity.this.onClick(mReminderToolbarImageButton);
            }
        });


        mUncheckedListRecyclerView = (RecyclerView) findViewById(R.id.activity_full_detail_note_checklist_unchecked);
        mUncheckedListRecyclerView.setNestedScrollingEnabled(false);

        mCheckedListRecyclerView = (RecyclerView) findViewById(R.id.activity_full_detail_note_checklist_checked);
        mCheckedListRecyclerView.setNestedScrollingEnabled(false);

        List<View> viewList = new ArrayList<>();
        viewList.add(mCheckedListAddItemButton);
        viewList.add(mLineSeparator);
        mUncheckedAdapter= new NoteChecklistAdapter(this, mNote.getCheckList(), NoteChecklistAdapter.RenderOption.RENDER_UNCHECKED,viewList);
        mCheckedAdapter= new NoteChecklistAdapter(this,mNote.getCheckList(), NoteChecklistAdapter.RenderOption.RENDER_CHECKED,viewList);
        mUncheckedAdapter.setNoteChecklistAdapterCounterpart(mCheckedAdapter);
        mCheckedAdapter.setNoteChecklistAdapterCounterpart(mUncheckedAdapter);

        //Drag and hold and swipe
        ItemTouchHelper.Callback callback =
                new ItemTouchHelperCallback(mUncheckedAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mUncheckedListRecyclerView);
        callback =
                new ItemTouchHelperCallback(mCheckedAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mCheckedListRecyclerView);

        mUncheckedListRecyclerView.setAdapter(mUncheckedAdapter);
        mCheckedListRecyclerView.setAdapter(mCheckedAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mUncheckedListRecyclerView.setLayoutManager(mLayoutManager);
        mUncheckedListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLayoutManager = new LinearLayoutManager(this);
        mCheckedListRecyclerView.setLayoutManager(mLayoutManager);
        mCheckedListRecyclerView.setItemAnimator(new DefaultItemAnimator());

        bindNote(mNote);
        setViewColor(mNote.getBackgroundColor(),ColorDialog.getNotificationColor(this,mNote.getBackgroundColor()));
    }

    //region Action Bar/Toolbar Menu Handling
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if (!mNote.isArchive()) {
            inflater.inflate(R.menu.toolbar_full_detail_note_items, menu);
        }
        else{
            inflater.inflate(R.menu.toolbar_full_detail_note_items_archived, menu);
        }

        pinIconColorChange(menu.findItem(R.id.toolbar_full_detail_note_items_pin),mNote.isPinned());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                closeOptionsMenu();
                finish();
                break;
            case R.id.toolbar_full_detail_note_items_pin:
                mNote.setPinned(!mNote.isPinned());
                pinIconColorChange(item,mNote.isPinned());
                break;
            case R.id.toolbar_full_detail_note_items_archive_toggle:
                mNote.setArchive(!mNote.isArchive());
                closeOptionsMenu();
                finish();
//                NavUtils.navigateUpFromSameTask(this);
                break;
            case R.id.toolbar_full_detail_note_items_delete:
                isDelete=true;
                closeOptionsMenu();
                finish();
//                NavUtils.navigateUpFromSameTask(this);
                break;
        }

        return false;
    }
    private void pinIconColorChange(MenuItem item,boolean isPinned){
        if (!isPinned){
            item.getIcon().clearColorFilter();
        }
        else{
            int iColor = ContextCompat.getColor(getBaseContext(),R.color.colorAccentHighlight);

            int red   = (iColor & 0xFF0000) / 0xFFFF;
            int green = (iColor & 0xFF00) / 0xFF;
            int blue  = iColor & 0xFF;

            float[] matrix = { 0, 0, 0, 0, red,
                    0, 0, 0, 0, green,
                    0, 0, 0, 0, blue,
                    0, 0, 0, 1, 0 };

            ColorFilter colorFilter = new ColorMatrixColorFilter(matrix);
            item.getIcon().mutate().setColorFilter(colorFilter);
        }
    }
    //endregion

    //OnStop save relevant information
    @Override
    public void onStop(){
        updateDataSource(false);

        super.onStop();
    }
    private void updateDataSource(boolean isUpdateFromReminderDialog){
        NoteChecklistAdapter.CheckListItemViewHolder viewHolder=mCheckedAdapter.getFocusedViewHolder();
        if (viewHolder!=null){
            viewHolder.parseData(); //Update original source
        }
        viewHolder=mUncheckedAdapter.getFocusedViewHolder();
        if(viewHolder!=null){
            viewHolder.parseData(); //Update original source
        }

//        if (mNote.getReminder().getReminderTime()!=null &&
//                mNote.getReminder().getReminderTime().getTime() > Calendar.getInstance().getTimeInMillis()){
//            mNote.setNotified(true);//Assume notification already fly out :D. since the user can spent eternity inside the edit note to write essay ;)
//            //Extremely rare case where the saving of this note happen just write before
//        }

        if (isNewNote) {
            if (!isFullDetailNoteActivityEmpty() && !isDelete) { //Only add new note if there is anything there
                mNoteService.addNote(parseToNote());
                isNewNote=false; //Indicate the note already added
            }
        }
        else {
            if (isDelete && !isNewNote){
                mNoteService.deleteNote(parseToNote());
                updateAlarm(DialogResponse.EXTRA); //Delete alarm
            }
            else{
                if (isUpdateFromReminderDialog){
                    mNoteService.editNote(parseToNote());
                }
                else{
                    //Not from reminder dialog
                    //Do not update reminder isnotified flag cuz notification already fly out already
                    mNoteService.editNoteWithoutUpdatingNotified(parseToNote());
                }
            }
        }

        NoteWidgetProvider.updateWidgets(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(NOTEOBJ,mNote);
        savedInstanceState.putBoolean(IS_REMINDER_DIRTY,isReminderDialogDirty);
    }


    //TODO parse to note more property
    //Parse information to Note Class
    private Note parseToNote(){
        //Make necessary changes to existing mNote references
        mNote.setTitleText(mTitleEditText.getText().toString());
        mNote.setContentText(mContentEditText.getText().toString());

        //CHECKList already auto update itself
        //Reminder auto update itself
        return mNote;
    }
    private boolean isFullDetailNoteActivityEmpty(){
        if (mTitleEditText.getText().toString().isEmpty() && mContentEditText.getText().toString().isEmpty() &&
                mNote.getCheckList().size()<=0 &&
                (mNote.getReminder().getReminderTime()==null || mNote.getReminder().getReminderTime().getTime()==0)
                ){
            return true; //It is empty as all content is empty
        }
        else
            return false;
    }

    //TODO bind note more property
    //Bind note information to GUI
    private void bindNote(Note note){
        mTitleEditText.setText(note.getTitleText());
        mContentEditText.setText(note.getContentText());
        updateReminderGraphic(note);
    }
    private void updateReminderGraphic(Note note){
        if (note.getReminder().getReminderTime()!=null){
            mReminderLayout.setVisibility(View.VISIBLE);
            mReminderImageView.setVisibility(View.VISIBLE);
            mReminderTextView.setVisibility(View.VISIBLE);

            Date date = new Date(note.getReminder().getReminderTime().getTime());
            SimpleDateFormat formatter=new SimpleDateFormat("dd MMM yy, h:mm a ");
            String str = formatter.format(date);

            mReminderTextView.setText(str);

        }
        else{
            mReminderLayout.setVisibility(View.GONE);
            mReminderImageView.setVisibility(View.GONE);
            mReminderTextView.setVisibility(View.GONE);
        }

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.activity_full_detail_note_toolbar_add: //Toolbar checklist add button
                Note.CheckListItem item = new Note.CheckListItem();
                item.setChecked(false);
                item.setItemText("");
                mNote.getCheckList().add(item);
                int index =mNote.getCheckList().size()-1;
                mUncheckedAdapter.notifyItemChanged(index);
                mLineSeparator.setVisibility(View.VISIBLE);
                mCheckedListAddItemButton.setVisibility(View.VISIBLE);
                break;
            case R.id.activity_full_detail_note_checklist_add_item: //CheckList self add item button
                Note.CheckListItem new_item = new Note.CheckListItem();
                new_item.setChecked(false);
                new_item.setItemText("");
                mNote.getCheckList().add(new_item);
                int item_index =mNote.getCheckList().size()-1;
                mUncheckedAdapter.notifyItemChanged(item_index);
                mLineSeparator.setVisibility(View.VISIBLE);
                mCheckedListAddItemButton.setVisibility(View.VISIBLE);
                //TODO scroll to correct position
                //myRecyclerViewAdapter.setSelectedItem(myRecyclerViewAdapter.getItemCount() - 1);
                //myRecyclerView.scrollToPosition(myRecyclerViewAdapter.getItemCount() - 1);
                break;
            case R.id.activity_full_detail_note_toolbar_reminder: //Add/Edit Reminder
                //TODO change reminder click logic location
                Timestamp timestamp = mNote.getReminder().getReminderTime();
                long time;
                if (timestamp == null) {
                    time = 0;
                }
                else{
                    time = timestamp.getTime();
                }

                DateTimeReminderDialog dialog = new DateTimeReminderDialog(FullDetailNoteActivity.this,FullDetailNoteActivity.this,time); //context,listener,time in long
                dialog.showReminderDialog();
                break;
            case R.id.activity_full_detail_note_toolbar_paint: //Change Color
                int color = mNote.getBackgroundColor();

                ColorDialog colorDialog = new ColorDialog(FullDetailNoteActivity.this,FullDetailNoteActivity.this,color);
                colorDialog.showDialog();

                break;
        }

    }

    //Called after reminder dialog complete, try to set alarm if targetTime > currentTime.
    //Will cancel alarm if reminder deleted.
    @Override
    public void onReminderDialogComplete(DialogResponse responseType, long timeInMillis) {
        if (responseType==DialogResponse.NEGATIVE) return;

        switch(responseType){
            case POSITIVE:
                //ADD/Modify - Confirm
                mDialogResponse = DialogResponse.POSITIVE;
                Timestamp ts = new Timestamp(timeInMillis);
                mNote.getReminder().setReminderTime(ts);
                mNote.setNotified(false); //Need to reset to allow notification to goes through
                break;
            case EXTRA:
                //Delete
                mNote.getReminder().setReminderTime(null);
                mNote.setNotified(false); //Need to reset to allow notification to goes through
                mDialogResponse = DialogResponse.EXTRA;
                break;
        }

        //TODO As below
        //Currently if user does not press back (to save) the isNotified will still display old value in database
        //Notification therefore will not be triggered...
        //Simple fix is to update note when this dialog is complete.

        updateDataSource(true);
        updateAlarm(mDialogResponse);
        updateReminderGraphic(mNote);
    }

    private void updateAlarm(DialogResponse response){
        Intent intent = new Intent(this, NotificationAlarmReceiver.class);
        intent.putExtra(NotificationAlarmReceiver.NOTE_ID, mNote.getID());
        PendingIntent sender = PendingIntent.getBroadcast(this, mNote.getID(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (response == DialogResponse.POSITIVE) {
            //Add
            long targetTime = mNote.getReminder().getReminderTime().getTime();
            long currentTime = Calendar.getInstance().getTimeInMillis();
            alarmManager.set(AlarmManager.RTC_WAKEUP, targetTime, sender);
//            //Only set alarm for future time not passed one
//            if (targetTime>currentTime)
//                alarmManager.set(AlarmManager.RTC_WAKEUP, targetTime, sender);
//            else{
//                alarmManager.cancel(sender); //Needed to remove any future alarm already set
//            }
        } else if (response == DialogResponse.EXTRA) {
            //Delete
            alarmManager.cancel(sender);
        }
    }

    @Override
    public void onColorDialogColorChange(int color,int notificationColor){
            mNote.setBackgroundColor(color);
            setViewColor(color,notificationColor);
    }

    private void setViewColor(int color,int notificationColor){
        //Change toolbar,change bottom bar, change middle part, and notification bar color (>21)
        mToolbar.setBackgroundColor(color);
        mNestedScrollView.setBackgroundColor(color);
        mLinearLayoutBottomBar.setBackgroundColor(color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(notificationColor);
        }
    }
}
