package yeohweizhu.mynote;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

/**
 * Created by yeohw on 3/5/2017.
 */

public class NoteWidgetRemoteViewsService extends RemoteViewsService {
    public static final String TAG= "RViewService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return (new NoteWidgetRemoteViewsFactory(this.getApplicationContext(), intent));
    }
}
