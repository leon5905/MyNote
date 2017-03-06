package yeohweizhu.mynote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

/**
 * Created by yeohw on 3/3/2017.
 */

public class NoteWidgetFragment extends NoteFragment {
    //Override this to respond to Note summary on touch
    private Note targetNote;

    @Override
    protected void onSummaryViewTouch(Note targetNote){
        this.targetNote = targetNote;

        //Open dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick This Note To Display On Home?");
        builder.setMessage("Note Content:\n"+ targetNote.getContentText() +"\n\nPress OK to confirm your choices.")
                .setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        completeConfiguration();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        builder.create().show(); //Show dialog
    }
    private void completeConfiguration(){
        ((NoteWidgetConfigurationActivity) getActivity()).updateWidget(targetNote);
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

    //Override this to display different toolbar
    protected void setupToolbar(View v){
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_main);
        activity.setSupportActionBar(toolbar);
        ActionBar bar = activity.getSupportActionBar();
        bar.setTitle("Pick A Note To Display");
    }

    //region Action Bar/Toolbar Menu Handling
    @Override
    protected int getToolbarMenuID(){
        return (R.menu.toolbar_empty);
    }


    public static NoteWidgetFragment newInstance(NoteService paramNoteService) {
        NoteWidgetFragment fragment = new NoteWidgetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
}
