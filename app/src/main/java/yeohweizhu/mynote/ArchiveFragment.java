package yeohweizhu.mynote;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

/**
 * Created by yeohw on 3/5/2017.
 */

public class ArchiveFragment extends NoteFragment {
    public static ArchiveFragment newInstance(NoteService paramNoteService) {
        ArchiveFragment fragment = new ArchiveFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    //Override this to display different toolbar
    protected void setupToolbar(View v){
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_main);
        toolbar.setTitle("Archive");
        toolbar.setTitleTextColor(ContextCompat.getColor(getActivity(),R.color.colorWhite));
        toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(),R.color.colorDarkGray));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = ContextCompat.getColor(getActivity(),R.color.colorDarkestGray);
            //int targetColor = Color.argb(100,Color.red(color),Color.red(color),Color.blue(color));
            //int targetColor = Color.argb(0,0,0,0);
            getActivity().getWindow().setStatusBarColor(color);
        }
        activity.setSupportActionBar(toolbar);
        ActionBar bar = activity.getSupportActionBar();
        ((IActivityFragmentHost) getActivity()).showHamburgerIcon();
    }

    @Override
    //Override this to give proper toolbar menu
    protected int getToolbarMenuID(){
        return (R.menu.toolbar_main_items);
    }

    @Override
    //Override this to respond to Note summary on touch
    protected void onSummaryViewTouch(Note targetNote){
        super.StartFullDetailNoteActivity(targetNote);
    }

    @Override
    //Override this to respond to Note summary on long touch
    protected void onSummaryViewLongTouch(Note tagetNote){

    }

    @Override
    //Override this to not show bottom toolbar
    protected boolean showBottomToolbar(){
        return false;
    }

    @Override
    //Override this to render what archive, unarchive, all
    protected NoteSummaryAdapter.ArchiveOption getArchiveRenderOption(){
        return NoteSummaryAdapter.ArchiveOption.RENDER_ARCHIVE;
    }
}
