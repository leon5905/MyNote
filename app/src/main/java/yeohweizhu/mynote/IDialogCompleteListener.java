package yeohweizhu.mynote;

/**
 * Created by yeohw on 3/4/2017.
 */

public interface IDialogCompleteListener {
    public enum DialogResponse{POSITIVE,NEGATIVE,EXTRA}

    public void onReminderDialogComplete(DialogResponse repsonseType,long timeInMillis);

    public void onColorDialogColorChange(int color, int notificationColor);
}
