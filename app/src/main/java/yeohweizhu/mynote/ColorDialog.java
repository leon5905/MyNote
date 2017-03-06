package yeohweizhu.mynote;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.flexbox.FlexboxLayout;

import java.util.HashMap;

/**
 * Created by yeohw on 3/5/2017.
 */

public class ColorDialog implements View.OnClickListener {
    public static final String TAG = "ColorDialog";

    private Context mContext;
    private IDialogCompleteListener mIDialogCompleteListener;
    private int mSelectedColor;
    private BitmapDrawable mSelectedButtonTick;
    private ImageButton mSelectedButton;

    private AlertDialog mAlertDialog;

    private int[] colorArray;
    private int[] notificationArray;

    HashMap<Integer,Integer> colorMap = new HashMap<>();

    public static int getNotificationColor(Context context, int color){
        HashMap<Integer,Integer> colorMap = new HashMap<>();

        int[] colorArray={ContextCompat.getColor(context,R.color.colorWhite),
                ContextCompat.getColor(context,R.color.colorLightYellow),
                ContextCompat.getColor(context,R.color.colorLightRed),
                ContextCompat.getColor(context,R.color.colorLightGreen),
                ContextCompat.getColor(context,R.color.colorLightCyan),
                ContextCompat.getColor(context,R.color.colorLightBlue),
                ContextCompat.getColor(context,R.color.colorLightBlueGray)
        };

        int[] notificationArray={ContextCompat.getColor(context,R.color.colorDarkWhite),
                ContextCompat.getColor(context,R.color.colorDarkYellow),
                ContextCompat.getColor(context,R.color.colorDarkRed),
                ContextCompat.getColor(context,R.color.colorDarkGreen),
                ContextCompat.getColor(context,R.color.colorDarkCyan),
                ContextCompat.getColor(context,R.color.colorDarkBlue),
                ContextCompat.getColor(context,R.color.colorDarkBlueGray)
        };

        for (int i=0;i<colorArray.length;i++){
            colorMap.put(colorArray[i],notificationArray[i]);
        }

        return colorMap.get(color);
    }

    private ColorDialog(){}

    public ColorDialog(Context context,IDialogCompleteListener listener,int color){
        mSelectedColor=color;
        mContext = context;
        mIDialogCompleteListener=listener;

        colorArray= new int[]{ContextCompat.getColor(mContext, R.color.colorWhite),
                ContextCompat.getColor(mContext, R.color.colorLightYellow),
                ContextCompat.getColor(mContext, R.color.colorLightRed),
                ContextCompat.getColor(mContext, R.color.colorLightGreen),
                ContextCompat.getColor(mContext, R.color.colorLightCyan),
                ContextCompat.getColor(mContext, R.color.colorLightBlue),
                ContextCompat.getColor(mContext, R.color.colorLightBlueGray)
        };

        notificationArray= new int[]{ContextCompat.getColor(mContext,R.color.colorDarkWhite),
                ContextCompat.getColor(mContext,R.color.colorDarkYellow),
                ContextCompat.getColor(mContext,R.color.colorDarkRed),
                ContextCompat.getColor(mContext,R.color.colorDarkGreen),
                ContextCompat.getColor(mContext,R.color.colorDarkCyan),
                ContextCompat.getColor(mContext,R.color.colorDarkBlue),
                ContextCompat.getColor(mContext,R.color.colorDarkBlueGray)
        };

        for (int i=0;i<colorArray.length;i++){
            colorMap.put(colorArray[i],notificationArray[i]);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onColorDialogColorChange(IDialogCompleteListener.DialogResponse.POSITIVE);
            }
        });

        builder.setTitle("Select Note Color");

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_color, null);
        FlexboxLayout layout = (FlexboxLayout) dialogView.findViewById(R.id.dialog_color_layout);
        Button button = (Button) dialogView.findViewById(R.id.dialog_color_circle_button);
        layout.removeAllViews();

//        int[] colorArrayID={R.color.colorWhite,
//                R.color.colorLightYellow,
//                R.color.colorLightRed,
//                R.color.colorLightGreen,
//                R.color.colorLightCyan,
//                R.color.colorLightBlue,
//                R.color.colorLightBlueGray
//        };

        int index=0;
        for (int colorInArray:colorArray){
            ImageButton newButton = new ImageButton(mContext);
            newButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            newButton.setId(colorInArray);
            newButton.setClickable(true);
            newButton.setImageResource(R.drawable.tick_oval);
            newButton.setOnClickListener((View.OnClickListener) this);
            newButton.setBackground(null);
            LayerDrawable drawable = (LayerDrawable)newButton.getDrawable();
            GradientDrawable oval = (GradientDrawable)drawable.getDrawable(0);
            oval.setColor(colorArray[index]);
            BitmapDrawable tick = (BitmapDrawable)drawable.getDrawable(1);
            if (colorInArray == color){
                mSelectedButton=newButton;
                mSelectedButtonTick = tick;
                mSelectedColor = colorInArray;
                tick.mutate().setAlpha(255);
            }
            else{
                tick.mutate().setAlpha(0);
            }
            layout.addView(newButton);
            index++;
        }

        float dpi = mContext.getResources().getDisplayMetrics().density;
        dialogView.setPadding((int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi));
        builder.setView(dialogView);

        mAlertDialog =builder.create();
    }

    public void showDialog(){
        if (mAlertDialog!=null){
            mAlertDialog.show();
        }
    }

    private void onColorDialogColorChange(IDialogCompleteListener.DialogResponse response){
        int notificationColor = colorMap.get(mSelectedColor);

        mIDialogCompleteListener.onColorDialogColorChange(mSelectedColor,notificationColor);
    }

    @Override
    public void onClick(View view) {
        ImageButton clickedbutton = (ImageButton)view;

        if (clickedbutton == mSelectedButton){
            return;
        }

        mSelectedColor= clickedbutton.getId();

        LayerDrawable drawable = (LayerDrawable)clickedbutton.getDrawable();
        BitmapDrawable tick = (BitmapDrawable)drawable.getDrawable(1);
        tick.setAlpha(255);//Change tick
        if (mSelectedButtonTick!=null)
            mSelectedButtonTick.setAlpha(0);//Change tick

        mSelectedButtonTick=tick;
        mSelectedButton = clickedbutton;

        onColorDialogColorChange(IDialogCompleteListener.DialogResponse.POSITIVE);//Change color
    }

    private int toggleAlpha(int color){
        String str= String.valueOf(Color.red(color))+String.valueOf(Color.green(color))+String.valueOf(Color.blue(color));
        Log.d(TAG,str);
        return Color.argb(255,Color.red(color),Color.green(color),Color.blue(color));
    }
}
