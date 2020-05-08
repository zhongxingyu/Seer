 package com.bignerdranch.android.remotecontrol;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 /**
  * Created by panda on 7/8/13.
  */
 public class RemoteControlFragment extends Fragment {
     private TextView mSelectedTextView;
     private TextView mWorkingTextView;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                              Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_remote_control, parent, false);
 
         mSelectedTextView = (TextView)v
                 .findViewById(R.id.fragment_remote_control_selectedTextView);
         mWorkingTextView = (TextView)v
                 .findViewById(R.id.fragment_remote_control_workingTextView);
 
         View.OnClickListener numberButtonListener = new View.OnClickListener() {
             @Override
            public void onClick(View v) {
                 TextView textView = (TextView)v;
                 String working = mWorkingTextView.getText().toString();
                 String text    = textView.getText().toString();
                 if (working.equals("0")) {
                     mWorkingTextView.setText(text);
                 } else {
                     mWorkingTextView.setText(working + text);
                 }
             }
         };
 
         TableLayout tableLayout = (TableLayout)v
                 .findViewById(R.id.fragment_remote_control_tableLayout);
         int number = 1;
         for (int i = 2; i < tableLayout.getChildCount() - 1; i++) {
             TableRow row = (TableRow)tableLayout.getChildAt(i);
             for (int j = 0; j < row.getChildCount(); j++) {
                 Button button = (Button)row.getChildAt(j);
                 button.setText("" + number);
                 button.setOnClickListener(numberButtonListener);
                 number++;
             }
         }
 
         TableRow bottomRow = (TableRow)tableLayout
                 .getChildAt(tableLayout.getChildCount() - 1);
 
         Button deleteButton = (Button)bottomRow.getChildAt(0);
         deleteButton.setText("Delete");
         deleteButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 mWorkingTextView.setText("0");
             }
         });
 
         Button zeroButton = (Button)bottomRow.getChildAt(1);
         zeroButton.setText("0");
         zeroButton.setOnClickListener(numberButtonListener);
 
         Button enterButton = (Button)bottomRow.getChildAt(2);
         enterButton.setText("Enter");
         enterButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 CharSequence working = mWorkingTextView.getText();
                 if (working.length() > 0)
                     mSelectedTextView.setText(working);
                 mWorkingTextView.setText("0");
             }
         });
 
         return v;
     }
 }
