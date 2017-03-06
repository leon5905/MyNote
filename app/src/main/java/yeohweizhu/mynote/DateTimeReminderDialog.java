package yeohweizhu.mynote;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * Created by yeohw on 3/4/2017.
 */

public class DateTimeReminderDialog {
    private Context mContext;
    private Calendar mCalendarTemp;
    private IDialogCompleteListener mListener;

    private EditText dateView;
    private EditText timeView;

    private AlertDialog mReminderDialog;

    private void DateTimeReminderDialog(){}

    public static DateTimeReminderDialog getInstance(Context context, IDialogCompleteListener listener ,long timeInMillis){
        return new DateTimeReminderDialog(context,listener,timeInMillis);
    }

    public DateTimeReminderDialog(Context context, IDialogCompleteListener listener ,long timeInMillis){
        mContext = context;
        mListener = listener;

        mCalendarTemp = Calendar.getInstance(); //new temp instance
        if (timeInMillis!=0) {
            mCalendarTemp.setTimeInMillis(timeInMillis);
        }

        int year = mCalendarTemp.get(Calendar.YEAR);
        int day = mCalendarTemp.get(Calendar.DAY_OF_MONTH);
        int month = mCalendarTemp.get(Calendar.MONTH);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishReminderDialog(IDialogCompleteListener.DialogResponse.POSITIVE);
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishReminderDialog(IDialogCompleteListener.DialogResponse.NEGATIVE);
            }
        });
        builder.setNeutralButton(R.string.dialog_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishReminderDialog(IDialogCompleteListener.DialogResponse.EXTRA);
            }
        });

        if (timeInMillis!=0)
            builder.setTitle("Edit Reminder");
        else
            builder.setTitle("Add Reminder");

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View dialogView = inflater.inflate(R.layout.dialog_reminder, null);
        dateView = (EditText) dialogView.findViewById(R.id.dialog_reminder_date);
        dateView.setInputType(0);
        setDateView(year,month,day);

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = mCalendarTemp;

                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(mContext,
                        new mDateSetListener(), mYear, mMonth, mDay);
                dialog.show();
            }
        });

        int hoursOfDay = mCalendarTemp.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendarTemp.get(Calendar.MINUTE);

        timeView = (EditText) dialogView.findViewById(R.id.dialog_reminder_time);
        timeView.setInputType(0);
        setTimeView(hoursOfDay,minute);

        timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = mCalendarTemp;

                int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(mContext,
                        new TimeSetListener(), hourOfDay, minute, false);
                dialog.show();
            }
        });

        float dpi = mContext.getResources().getDisplayMetrics().density;
        dialogView.setPadding((int)(19*dpi), (int)(5*dpi), (int)(14*dpi), (int)(5*dpi));
        builder.setView(dialogView);

        // Create the AlertDialog
        mReminderDialog = builder.create();
    }

    public void showReminderDialog(){
        mReminderDialog.show();
    }
    
    class mDateSetListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            int mYear = year;
            int mMonth = monthOfYear;
            int mDay = dayOfMonth;

            DateTimeReminderDialog.this.setDateView(mYear,mMonth,mDay);
            //Update Cache
            mCalendarTemp.set(Calendar.YEAR,mYear);
            mCalendarTemp.set(Calendar.MONTH,mMonth);
            mCalendarTemp.set(Calendar.DAY_OF_MONTH,mDay);
        }
    }

    class TimeSetListener implements TimePickerDialog.OnTimeSetListener{
        @Override
        public void onTimeSet(TimePicker timePicker, int hoursOfDay, int minute) {
            DateTimeReminderDialog.this.setTimeView(hoursOfDay,minute);

            //Update Cache
            mCalendarTemp.set(Calendar.HOUR_OF_DAY,hoursOfDay);
            mCalendarTemp.set(Calendar.MINUTE,minute);
        }
    }


    private void finishReminderDialog(IDialogCompleteListener.DialogResponse response_type){
        if (response_type == IDialogCompleteListener.DialogResponse.POSITIVE){
            mListener.onReminderDialogComplete(IDialogCompleteListener.DialogResponse.POSITIVE,mCalendarTemp.getTimeInMillis());
        }
        else if (response_type==IDialogCompleteListener.DialogResponse.NEGATIVE){
            mListener.onReminderDialogComplete(IDialogCompleteListener.DialogResponse.NEGATIVE,0);
        }
        else{
            //Delete
            mListener.onReminderDialogComplete(IDialogCompleteListener.DialogResponse.EXTRA,0);
        }

        //  Note.Reminder reminder=mNote.getReminder();
        //  if(makeReminderDialog(reminder)){
        //      Timestamp reminderTimeStamp  = reminder.getReminderTime();
        //      if (reminderTimeStamp==null){
        //          //Delete alarm from alarmManager
        //
        //      }
        //      else{
        //          //Add alarmMaanger
        //
        //      }
        //  }
    }

    private void setDateView(int year,int month,int day){
        //Display Month
        String monthStr = new DateFormatSymbols().getMonths()[month]; //mMonth is zero based
        dateView.setText(new StringBuilder()
                .append(day).append(" ").append(monthStr).append(" ")
                .append(year).append(" "));
    }

    private void setTimeView(int hoursOfDay,int minute){
        boolean isNight = (hoursOfDay/12)>0?true:false;
        String minuteStr = minute/10>0?String.valueOf(minute):("0"+(String.valueOf(minute)));
        timeView.setText( (hoursOfDay%12==0?12:hoursOfDay%12)+ " : " + minuteStr + (isNight?" PM":" AM") );
    }
}
