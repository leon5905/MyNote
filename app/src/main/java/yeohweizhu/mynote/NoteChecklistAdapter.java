package yeohweizhu.mynote;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.Collections;
import java.util.List;

/**
 * Created by yeohw on 3/2/2017.
 */

public class NoteChecklistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IItemTouchHelperAdapter{
    //InternalNoteList References
    private List<Note.CheckListItem> mCheckList;
    public List<Note.CheckListItem> getCheckList() {return mCheckList;}
    //public void setNoteList(List<Note.CheckListItem> checkList) {mCheckList = checkList;}

    private NoteChecklistAdapter mNoteChecklistAdapterCounterpart=null;
    public void setNoteChecklistAdapterCounterpart(NoteChecklistAdapter noteChecklistAdapterCounterpart) {
        mNoteChecklistAdapterCounterpart = noteChecklistAdapterCounterpart;
    }

    private CheckListItemViewHolder mFocusedViewHolder=null;
    public CheckListItemViewHolder getFocusedViewHolder(){
        return mFocusedViewHolder;
    }

    private Context mContext;
    private RenderOption mRenderOption;
    private List<View> viewArray; //View to be toggle visibility

    public enum RenderOption{RENDER_CHECKED,RENDER_UNCHECKED,RENDER_ALL}

    //Constructor
    public NoteChecklistAdapter(Context context,List<Note.CheckListItem> checkList, RenderOption render) {
        init(context,checkList,render,null,null);
    }

    public NoteChecklistAdapter(Context context,List<Note.CheckListItem> checkList, RenderOption render, List<View> viewArray) {
        init(context,checkList,render,null,viewArray);
    }
    private void init(Context context,List<Note.CheckListItem> checkList, RenderOption render, NoteChecklistAdapter noteChecklistAdapterCounterpart,List<View> viewArray){
        this.mCheckList = checkList;
        this.mContext = context;
        this.mRenderOption = render;
        this.mNoteChecklistAdapterCounterpart = noteChecklistAdapterCounterpart;
        this.viewArray = viewArray;
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mCheckList.size();
    }

    //Data binding process
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Note.CheckListItem item = mCheckList.get(position);
        NoteChecklistAdapter.CheckListItemViewHolder tempViewHolder = (CheckListItemViewHolder) holder;

        switch (holder.getItemViewType()) {
            case 0: //Uncheck
                if (mRenderOption==RenderOption.RENDER_UNCHECKED)
                    tempViewHolder.bindData(item);
                else
                    tempViewHolder.hideData(); //unchecked tickbox which not be rendered here
                break;
            case 1: //Checked
                if (mRenderOption==RenderOption.RENDER_CHECKED)
                    tempViewHolder.bindData(item);
                else
                    tempViewHolder.hideData();
                break;
        }

    }

    //Different Type to classify different view
    @Override
    public int getItemViewType(int position) {
        if (mCheckList.get(position).isChecked()){
            return 1;
        }
        else{
            return 0;
        }
    }

    //Decide which view to inflate
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView=null;

        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_checklist_item, parent, false);

        return new CheckListItemViewHolder(itemView);
    }

    //Swiped to right
    @Override
    public void onItemDismiss(int position) {
        //Instead of dismissing the item, the item is isChecked attribute it toggled.
        Note.CheckListItem item = mCheckList.get(position);
        mCheckList.get(position).setChecked(!item.isChecked());
        notifyItemChanged(position);
        if (mNoteChecklistAdapterCounterpart!=null){
            mNoteChecklistAdapterCounterpart.notifyItemChanged(position);
        }
    }

    //OnDrag
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mCheckList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mCheckList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        if (mNoteChecklistAdapterCounterpart!=null){
            mNoteChecklistAdapterCounterpart.notifyItemMoved(fromPosition, toPosition);
        }
    }

    //region View Holder
    //Normal ViewHolder Class
    //, View.OnLongClickListener
    public class CheckListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private View view;
        private RelativeLayout relativeLayout;
        private EditText text;
        private AppCompatCheckBox checkBox;
        private ImageButton removeButton;
        private Note.CheckListItem item;

        public CheckListItemViewHolder(View view) {
            super(view);
            this.view= view;
            removeButton = (ImageButton) view.findViewById(R.id.note_checklist_item_remove_image_button);
            removeButton.setOnClickListener(this);
            text = (EditText) view.findViewById(R.id.note_checklist_item_text);
            checkBox = (AppCompatCheckBox) view.findViewById(R.id.note_checklist_item_checkbox);
            relativeLayout = (RelativeLayout) view.findViewById(R.id.note_checklist_item_layout);
            checkBox.setOnClickListener(this);
            text.setOnFocusChangeListener(new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange(View v, boolean hasFocus)
                {
                    if(!hasFocus)//LostFocus
                    {
                        mFocusedViewHolder=null;
                        item.setItemText(text.getText().toString()); //Update it
                    }
                    else{
                        mFocusedViewHolder = CheckListItemViewHolder.this;
                    }
                }
            });
        }

        public void bindData(Note.CheckListItem item){
            this.item=item;
            text.setText(item.getItemText());

            boolean isChecked = item.isChecked();
            checkBox.setChecked(isChecked);
            if (isChecked){
                text.setTextColor(ContextCompat.getColor(mContext,R.color.colorDarkGray));
                text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); //strikethrough text
                checkBox.setSupportButtonTintList(ContextCompat.getColorStateList(mContext, R.color.colorDarkGray));
            }

            relativeLayout.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }

        public void parseData(){
            item.setChecked(checkBox.isChecked());
            item.setItemText(text.getText().toString());
        }

        public void hideData(){
            relativeLayout.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
        }

        // onClick Listener for view
        @Override
        public void onClick(View view) {
            int position = mCheckList.indexOf(item);
            if (position<0) return; //Might happen if user click too fast

            switch (view.getId()){
                case R.id.note_checklist_item_checkbox:
                    parseData();

                    NoteChecklistAdapter.this.notifyItemChanged(position);
                    if (NoteChecklistAdapter.this.mNoteChecklistAdapterCounterpart!=null)
                        NoteChecklistAdapter.this.mNoteChecklistAdapterCounterpart.notifyItemChanged(position);

                    break;
                case R.id.note_checklist_item_remove_image_button:
                    NoteChecklistAdapter.this.mCheckList.remove(position);
                    NoteChecklistAdapter.this.notifyItemRemoved(position);

                    int itemCount = NoteChecklistAdapter.this.getItemCount();

                    NoteChecklistAdapter.this.notifyItemRangeChanged(position,itemCount);
                    if (NoteChecklistAdapter.this.mNoteChecklistAdapterCounterpart!=null) {
                        NoteChecklistAdapter.this.mNoteChecklistAdapterCounterpart.notifyItemRemoved(position);
                        NoteChecklistAdapter.this.mNoteChecklistAdapterCounterpart.notifyItemRangeChanged(position,itemCount);
                    }

                    //Remove visibility if none
                    if (itemCount == 0) {
                        if (viewArray!=null){
                            for (View v:viewArray){
                                v.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
            }
        }

        //onLongClickListener for view
//        @Override
//        public boolean onLongClick(View v) {
//
//            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//            builder.setTitle ("Hello Dialog")
//                    .setMessage ("LONG CLICK DIALOG WINDOW FOR ICON " + String.valueOf(getAdapterPosition()))
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//
//            builder.create().show();
//            return true;
//        }
    }
    //endregion
}
