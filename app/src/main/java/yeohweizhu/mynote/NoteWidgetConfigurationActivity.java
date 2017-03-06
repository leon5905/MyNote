package yeohweizhu.mynote;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RemoteViews;

import java.util.List;

/**
 * Created by yeohw on 3/3/2017.
 */

public class NoteWidgetConfigurationActivity extends AppCompatActivity implements NoteFragment.OnFragmentInteractionListener {

    public static final String TAG="WidgetConfigAct";

    private int mAppWidgetId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.widget_configuration_activity);

        //Get widget id for return result
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_CANCELED, resultValue); //Precaution so that if user back out os is notified.

        NoteService noteService = NoteService.getInstance(this);

        //Init Fragment related operation
        NoteFragment mAllNoteFragment = NoteWidgetFragment.newInstance(noteService);
        FragmentManager mFragmentManager = getSupportFragmentManager();

        FragmentTransaction initTransaction = mFragmentManager.beginTransaction();
        initTransaction.add(R.id.widget_activity_home_frame,mAllNoteFragment);
        initTransaction.commit();
        
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void updateWidget(Note note){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.preference_file_key_widget),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(String.valueOf(mAppWidgetId),note.getID());
        editor.commit(); //Associate app widget id with note id

//        RemoteViews view = NoteWidgetProvider.bindData(this,note);
//        RemoteViews view =updateWidgetListView(this,mAppWidgetId,note);
        RemoteViews view = NoteWidgetProvider.bindData(this,note);
        appWidgetManager.updateAppWidget(mAppWidgetId, view);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);

        finish();
    }

    private RemoteViews updateWidgetListView(Context context,
                                             int appWidgetId,
                                             Note note) {
        //which layout to show on widget
        RemoteViews remoteViews = new RemoteViews(
                context.getPackageName(),R.layout.widget_main_layout);

        //RemoteViews Service needed to provide adapter for ListView
        Intent svcIntent = new Intent(context, NoteWidgetRemoteViewsService.class);
        //passing app widget id to that RemoteViews Service
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        svcIntent.setData(Uri.parse(
                svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        svcIntent.putExtra(NoteWidgetRemoteViewsFactory.NOTE,note);

        //setting adapter to listview of the widget
        remoteViews.setRemoteAdapter(R.id.widget_main_list_view, svcIntent);

//        NoteWidgetRemoteViewsService service = new NoteWidgetRemoteViewsService();
//        service.onGetViewFactory(svcIntent);

        //setting an empty view in case of no data
        //remoteViews.setEmptyView(R.id.listViewWidget, R.id.empty_view);
        return remoteViews;
    }
}
