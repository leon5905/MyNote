package yeohweizhu.mynote;

/**
 * Created by yeohw on 3/3/2017.
 */
public interface IItemTouchHelperAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
