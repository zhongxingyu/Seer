 /*
  * Copyright (c) 2013, acbelter
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice, this
  * list of conditions and the following disclaimer in the documentation and/or
  * other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.acbelter.hhtest;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import com.acbelter.hhtest.R.id;
 import com.acbelter.hhtest.R.layout;
 import com.acbelter.hhtest.R.string;
 
 import java.lang.reflect.Method;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class CvActivity extends Activity {
     public static final String NAME = "com.acbelter.hhtest.NAME";
     public static final String BD_YEAR = "com.acbelter.hhtest.BD_YEAR";
     public static final String BD_MONTH = "com.acbelter.hhtest.BD_MONTH";
     public static final String BD_DAY = "com.acbelter.hhtest.DD_DAY";
     public static final String SEX = "com.acbelter.hhtest.SEX";
     public static final String OFFICE = "com.acbelter.hhtest.OFFICE";
     public static final String SALARY = "com.acbelter.hhtest.SALARY";
     public static final String PHONE = "com.acbelter.hhtest.PHONE";
     public static final String EMAIL = "com.acbelter.hhtest.EMAIL";
 
     private static final int RQ_SEND_CV = 1;
 
     private EditText mName;
     private DatePicker mBirthDatePicker;
     private Spinner mSexSpinner;
     private EditText mOffice;
     private EditText mSalary;
     private EditText mPhone;
     private EditText mEmail;
 
     /**
      * Fields for saving replay dialog's state after screen rotation.
      */
     private static String sReplyStr;
     private static boolean sDialogShow;
 
     /**
      * True, if the date of birth was changed and now it's less than the current date.
      */
     private boolean mDateReduced;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_cv);
 
         mName = (EditText) findViewById(id.name);
         mBirthDatePicker = (DatePicker) findViewById(id.birth_date_picker);
         mSexSpinner = (Spinner) findViewById(id.sex_spinner);
         mOffice = (EditText) findViewById(id.office);
         mSalary = (EditText) findViewById(id.salary);
         mPhone = (EditText) findViewById(id.phone);
         mEmail = (EditText) findViewById(id.email);
         // Hide calendar if SDK version is more than 11.
         if (Build.VERSION.SDK_INT >= 11) {
             try {
                 String methodName = "setCalendarViewShown";
                 Method m = mBirthDatePicker.getClass().getMethod(methodName, boolean.class);
                 m.invoke(mBirthDatePicker, false);
             } catch (Exception e) {
                 // Do nothing because old API versions don't include calendar in the DatePicker.
             }
         }
         /*
         InputFilter for applying strings consists of only letters,
         spaces, points and dashes as name.
          */
         InputFilter nameFilter = new InputFilter() {
             @Override
             public CharSequence filter(CharSequence source, int start, int end,
                                        Spanned dest, int dstart, int dend) {
                 for (int i = start; i < end; i++) {
                     char c = source.charAt(i);
                     if (!Character.isLetter(c)
                             && !(c == ' ')
                             && !(c == '.')
                             && !(c == '-')) {
                         return "";
                     }
                 }
 
                 return null;
             }
         };
 
         mName.setFilters(new InputFilter[]{nameFilter});
         mName.requestFocus();
     }
 
     private int[] getBirthDate() {
         int date[] = new int[3];
         date[0] = mBirthDatePicker.getYear();
         date[1] = mBirthDatePicker.getMonth();
         date[2] = mBirthDatePicker.getDayOfMonth();
         return date;
     }
 
     /**
      * Checks the correctness of the entered data.
      * @return True, if the entered data correct.
      */
     private boolean checkInputData() {
         // It's assumed that the date of birth is correct if it isn't empty.
         if (isEmpty(mName)) {
             Toast.makeText(this, getString(string.toast_name), Toast.LENGTH_SHORT).show();
             return false;
         }
 
         Calendar c = Calendar.getInstance();
         Date currentDate = c.getTime();
 
         int[] date = getBirthDate();
         c.set(Calendar.YEAR, date[0]);
         c.set(Calendar.MONTH, date[1]);
         c.set(Calendar.DAY_OF_MONTH, date[2]);
         Date birthDate = c.getTime();
 
         mDateReduced = false;
         // It's assumed that the date of birth is correct if it's less than the current date.
         if (birthDate.compareTo(currentDate) > 0) {
             Toast.makeText(this, getString(string.toast_birth_date), Toast.LENGTH_SHORT).show();
             return false;
         } else if (birthDate.compareTo(currentDate) < 0) {
             mDateReduced = true;
         }
         // It's assumed that the salary is correct if isn't zero and the first digit isn't zero.
         String strSalary = mSalary.getText().toString();
         if (strSalary.length() > 1 && strSalary.startsWith("0")) {
            Toast.makeText(this, getString(string.toast_name), Toast.LENGTH_SHORT).show();
             return false;
         }
         // It's assumed that the email is correct if it matches with regexp.
         String strPhone = mPhone.getText().toString();
         final String phonePattern = "^\\+?\\d+$";
         Pattern pp = Pattern.compile(phonePattern);
         Matcher pm = pp.matcher(strPhone);
         if (strPhone.length() > 0 && !pm.matches()) {
             Toast.makeText(this, getString(string.toast_phone), Toast.LENGTH_SHORT).show();
             return false;
         }
         /* It's assumed that the email is correct if it matches with regexp.
          The regexp description:
          www.mkyong.com/regular-expressions/how-to-validate-email-address-with-regular-expression
 
          Using this regexp gives more accurate results than using Patterns.EMAIL_ADDRESS.
          */
         String strEmail = mEmail.getText().toString();
         final String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                 + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
         Pattern ep = Pattern.compile(emailPattern);
         Matcher em = ep.matcher(strEmail);
         if (strEmail.length() > 0 && !em.matches()) {
             Toast.makeText(this, getString(string.toast_email), Toast.LENGTH_SHORT).show();
             return false;
         }
 
         return true;
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         if (sDialogShow) buildDialog(this, sReplyStr).show();
     }
 
     private boolean isEmpty(EditText editText) {
         return editText.getText().toString().trim().length() < 1;
     }
 
     /**
      * Called when user press the button for sending the curriculum vitae.
      * @param view Clicked button view.
      */
     public void sendCv(View view) {
         Intent cvIntent = new Intent(this, ReplyActivity.class);
         if (checkInputData()) {
             cvIntent.putExtra(NAME, mName.getText().toString().trim());
 
             int[] date = getBirthDate();
             if (mDateReduced) {
                 cvIntent.putExtra(BD_YEAR, date[0]);
                 cvIntent.putExtra(BD_MONTH, date[1]);
                 cvIntent.putExtra(BD_DAY, date[2]);
             }
 
             if (mSexSpinner.getSelectedItemPosition() > 0) {
                 cvIntent.putExtra(SEX, mSexSpinner.getSelectedItem().toString());
             }
             if (!isEmpty(mOffice)) {
                 cvIntent.putExtra(OFFICE, mOffice.getText().toString().trim());
             }
             if (!isEmpty(mSalary)) {
                 cvIntent.putExtra(SALARY, mSalary.getText().toString());
             }
             if (!isEmpty(mPhone)) {
                 cvIntent.putExtra(PHONE, mPhone.getText().toString());
             }
             if (!isEmpty(mEmail)) {
                 cvIntent.putExtra(EMAIL, mEmail.getText().toString().trim());
             }
            
             startActivityForResult(cvIntent, RQ_SEND_CV);
         }
     }
 
     /**
      * Creates the dialog with employer's reply.
      * @param context The Context the Dialog is to run it.
      * @param replyText Employer's reply text.
      * @return New instance of the dialog.
      */
     private static Dialog buildDialog(Context context, String replyText) {
         final Dialog d = new Dialog(context);
         d.setContentView(layout.reply);
         d.setTitle(string.employer_reply);
 
         TextView replyTextView = (TextView) d.findViewById(id.reply_text);
         if (replyText != null) replyTextView.setText(replyText);
 
         Button closeButton = (Button) d.findViewById(id.close_button);
         closeButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 d.dismiss();
                 sDialogShow = false;
                 sReplyStr = null;
             }
         });
 
         return d;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             if (requestCode == RQ_SEND_CV) {
                 sReplyStr = data.getStringExtra(ReplyActivity.REPLY);
                 buildDialog(this, sReplyStr).show();
                 sDialogShow = true;
             }
         }
     }
 }
