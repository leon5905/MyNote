package yeohweizhu.mynote;

import android.content.Context;
import android.graphics.Paint;
import android.support.transition.Visibility;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by yeohw on 2/15/2017.
 * Adapter for displaying summary note
 * Capable of synchronizing two adapter sharing the same list (for removal)
 */

public class NoteSummaryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  implements IItemTouchHelperAdapter{
    public enum RenderOption{RENDER_PINNED,RENDER_UNPINNED,RENDER_ALL}
    public enum ArchiveOption{RENDER_NORMAL,RENDER_ARCHIVE,RENDER_ALL}

    //InternalNoteList References
    private List<Note> mNoteList;
    public List<Note> getNoteList() {return mNoteList;}
    //public void setNoteList(List<Note> noteList) {mNoteList = noteList;}

    private NoteSummaryAdapter mSynchroAdapter; //Sync both adapter if present.
    private Context mContext;
    private RenderOption mRenderOption;
    private ArchiveOption mArchiveOption;

    public NoteSummaryAdapter(Context context, List<Note> noteList, RenderOption render, ArchiveOption renderArchive) {
        this.mNoteList = noteList;
        this.mContext = context;
        this.mRenderOption =render;
        this.mArchiveOption = renderArchive;
        this.notifyDataSetChanged();
    }
    public void setSynchronizedAdapter(NoteSummaryAdapter adapter){
        mSynchroAdapter = adapter;
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    //Data binding process
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Note note = mNoteList.get(position);

        switch(holder.getItemViewType()){
            case 0:
                NoteViewHolderSummary unpinnedViewHolder = (NoteViewHolderSummary) holder;
                if (mRenderOption==RenderOption.RENDER_UNPINNED || mRenderOption==RenderOption.RENDER_ALL) {
                    if (mArchiveOption==ArchiveOption.RENDER_ALL)
                        unpinnedViewHolder.bindData(note);
                    else if ( (mArchiveOption==ArchiveOption.RENDER_ARCHIVE && note.isArchive()) ||
                            (mArchiveOption==ArchiveOption.RENDER_NORMAL && !note.isArchive())
                            ){
                        unpinnedViewHolder.bindData(note);
                    }
                    else{
                        unpinnedViewHolder.hideData();
                    }
                }
                else{
                    unpinnedViewHolder.hideData();
                }
                break;
            case 1:
                NoteViewHolderSummary pinnedViewHolder = (NoteViewHolderSummary) holder;
                if (mRenderOption==RenderOption.RENDER_PINNED || mRenderOption==RenderOption.RENDER_ALL) {
                    if (mArchiveOption==ArchiveOption.RENDER_ALL)
                        pinnedViewHolder.bindData(note);
                    else if ( (mArchiveOption==ArchiveOption.RENDER_ARCHIVE && note.isArchive()) ||
                            (mArchiveOption==ArchiveOption.RENDER_NORMAL && !note.isArchive())
                            ){
                        pinnedViewHolder.bindData(note);
                    }
                    else{
                        pinnedViewHolder.hideData();
                    }
                }
                else{
                    pinnedViewHolder.hideData();
                }
                break;
//            case 1:
                  //LEGACY Code..Using header approach - problem when moving item around. Thus scrapped.
//                //Header Layout
//                NoteViewHolderHeader tempHeaderViewHolder = (NoteViewHolderHeader) holder;
//                StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                layoutParams.setFullSpan(true);
//                holder.itemView.setLayoutParams(layoutParams);
//                tempHeaderViewHolder.bindData(note);
//                break;
        }

    }

    //Different Type to classify different view
    @Override
    public int getItemViewType(int position) {
        if (!mNoteList.get(position).isPinned())
            return 0; //not pinned
        else
            return 1; //pinned
    }

    //Decide which view to inflate
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView=null;

        switch(viewType){
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_summary, parent, false);
                return new NoteViewHolderSummary(itemView);
        }
    }

    //Swiped to right
    @Override
    public void onItemDismiss(int position) {
        //Instead of dismissing the item, the item is archived..
        Note note = mNoteList.get(position);
        note.setArchive(!note.isArchive());

        notifyItemChanged(position);
        if (mSynchroAdapter!=null){
            mSynchroAdapter.notifyItemChanged(position);
        }
    }

    //OnDrag
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mNoteList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mNoteList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        if (mSynchroAdapter!=null){
            mSynchroAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    public class NoteViewHolderGeneral extends RecyclerView.ViewHolder{
        protected Note mNote;
        public Note getNote(){return mNote;};

        public NoteViewHolderGeneral(View itemView) {
            super(itemView);
        }
    }

    //region View Holder
    //Normal ViewHolder Class
    public class NoteViewHolderSummary extends NoteViewHolderGeneral {
        private TextView mText;
        private TextView mTitle;
        private LinearLayout mCheckListLayout;
        private ImageView mPinImageView;
        private CardView mCardView;
        private ImageView mReminderImageView;
        private LinearLayout mReminderLayout;
        private TextView mReminderTextView;

        public NoteViewHolderSummary(View view) {
            super(view);

            mCardView = (CardView) view.findViewById(R.id.note_summary_card_view);
            mText = (TextView) view.findViewById(R.id.note_summary_text);
            mTitle = (TextView) view.findViewById(R.id.note_summary_title);
            mCheckListLayout = (LinearLayout) view.findViewById(R.id.note_summary_checklist_layout);
            mPinImageView = (ImageView) view.findViewById(R.id.note_summary_pin);
            mReminderImageView = (ImageView) view.findViewById(R.id.note_summary_reminder_image);
            mReminderLayout = (LinearLayout) view.findViewById(R.id.note_summary_reminder_layout);
            mReminderTextView = (TextView) view.findViewById(R.id.note_summary_reminder_text);
        }

        public void bindData(Note note){
            super.mNote = note;

            mCardView.setCardBackgroundColor(note.getBackgroundColor());
            mCardView.setVisibility(View.VISIBLE);

            //Pin
            if (note.isPinned())
                mPinImageView.setVisibility(View.VISIBLE);
            else
                mPinImageView.setVisibility(View.GONE);

            mText.setText(note.getContentText());

            //Title
            String title = note.getTitleText();
            if (title!=null && !title.isEmpty()){
                mTitle.setText(title);
                mTitle.setVisibility(View.VISIBLE);
            }
            else{
                mTitle.setVisibility(View.GONE);
            }

            //Reminder
            if (mNote.getReminder().getReminderTime()!=null){
                mReminderImageView.setVisibility(View.VISIBLE);
                mReminderTextView.setVisibility(View.VISIBLE);
                mReminderLayout.setVisibility(View.VISIBLE);

                Date date = new Date(note.getReminder().getReminderTime().getTime());
                SimpleDateFormat formatter=new SimpleDateFormat("dd MMM yy, h:mm a ");
                String str = formatter.format(date);
                mReminderTextView.setText(str);
            }
            else{
                mReminderImageView.setVisibility(View.GONE);
                mReminderTextView.setVisibility(View.GONE);
                mReminderLayout.setVisibility(View.GONE);
            }

            //CheckList
            mCheckListLayout.removeAllViews();//Discard all view
            if (note.getCheckList()==null || note.getCheckList().size()==0){
                mCheckListLayout.setVisibility(View.GONE);
            }
            else{
                mCheckListLayout.setVisibility(View.VISIBLE);
            }

            Queue<Note.CheckListItem> itemQueue = new LinkedList<>();
            for (Note.CheckListItem item :note.getCheckList()){
                if (item.isChecked()){
                    itemQueue.add(item);
                }
                else{
                    View view = LayoutInflater.from(mContext).inflate(R.layout.note_checklist_item, null);
                    AppCompatCheckBox checkBox = ((AppCompatCheckBox)view.findViewById(R.id.note_checklist_item_checkbox));
                    checkBox.setChecked(item.isChecked());
                    checkBox.setFocusable(false);
                    checkBox.setClickable(false);
                    TextView text = ((TextView)view.findViewById(R.id.note_checklist_item_text));
                    text.setFocusable(false);
                    text.setClickable(false);
                    text.setText(item.getItemText());
                    text.setHint("");

                    view.findViewById(R.id.note_checklist_item_remove_image_button).setVisibility(View.GONE);

                    mCheckListLayout.addView(view);
                }
            }
            for(Note.CheckListItem item:itemQueue){
                View view = LayoutInflater.from(mContext).inflate(R.layout.note_checklist_item, null);
                AppCompatCheckBox checkBox = ((AppCompatCheckBox)view.findViewById(R.id.note_checklist_item_checkbox));
                checkBox.setChecked(item.isChecked());
                checkBox.setFocusable(false);
                checkBox.setClickable(false);

                EditText text = ((EditText)view.findViewById(R.id.note_checklist_item_text));
                text.setFocusable(false);
                text.setClickable(false);
                text.setText(item.getItemText());
                text.setHint("");

                view.findViewById(R.id.note_checklist_item_remove_image_button).setVisibility(View.GONE);

                text.setTextColor(ContextCompat.getColor(mContext,R.color.colorDarkGray));
                text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //strikethrough text
                checkBox.setSupportButtonTintList(ContextCompat.getColorStateList(mContext, R.color.colorDarkGray));

                mCheckListLayout.addView(view);
            }
        }
        private void hideData(){
            mCardView.setVisibility(View.GONE);
        }
    }
    //endregion
}
