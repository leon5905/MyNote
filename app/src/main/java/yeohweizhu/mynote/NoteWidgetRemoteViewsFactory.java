package yeohweizhu.mynote;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

/**
 * Created by yeohw on 3/5/2017.
 * Not complete, no enough time to finish
 * Thus it is not used
 */

public class NoteWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String TAG="ViewFactory";
    public static final String NOTE="NoteObject";

    private ArrayList<Note> mNoteList = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;

    public NoteWidgetRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        Note note = (Note)intent.getParcelableExtra(NOTE);

        if (note!=null)
            mNoteList.add(note);
    }

    @Override
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.


    }

    @Override
    public void onDataSetChanged() {   }

    @Override
    public void onDestroy(){   }

    @Override
    public int getCount() {
        return mNoteList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Note note = mNoteList.get(0);
        RemoteViews view = NoteWidgetProvider.bindData(context,note);
        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.simple_text_view);
        view.setTextViewText(R.id.simple_text,"Loading...");

        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }
}