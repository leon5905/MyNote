package yeohweizhu.mynote;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

import java.util.List;

/**
 * Created by yeohw on 3/3/2017.
 */

/*
Special coded to provide canHoldOver with NoteViewHolder
 */
public class ItemTouchHelperCallBackPinGrid extends ItemTouchHelper.Callback {
    private final IItemTouchHelperAdapter mAdapter;

    public ItemTouchHelperCallBackPinGrid(IItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = (ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) ;
        int swipeFlags = ItemTouchHelper.END | ItemTouchHelper.START;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

//    @Override
//    public boolean canDropOver (RecyclerView recyclerView,
//                         RecyclerView.ViewHolder current,
//                         RecyclerView.ViewHolder target){
//        NoteSummaryAdapter.NoteViewHolderGeneral currentViewHolder= (NoteSummaryAdapter.NoteViewHolderGeneral) current;
//        NoteSummaryAdapter.NoteViewHolderGeneral targetViewHolder = (NoteSummaryAdapter.NoteViewHolderGeneral) target;
//
//        Note currentNote = currentViewHolder.getNote();
//        Note targetNote = targetViewHolder.getNote();
//
//        if (currentNote.isNoteHeader())
//            return false;
//        else if (currentNote.isPinned() == targetNote.isPinned()){
//            return true;
//        }
//        else{
//            return false; //If both is not same status, dun let them swap place
//        }
//    }
}
