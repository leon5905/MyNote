package yeohweizhu.mynote;

import android.view.View;

/**
 * Created by yeohw on 2/19/2017.
 */

public interface ITouchListener {
    public void onTouch(View view, int position);
    public void onLongTouch(View view,int position);
}
