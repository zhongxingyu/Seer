 package com.joewoo.ontime.support.dialog;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.Toast;
 
 import com.joewoo.ontime.R;
 import com.joewoo.ontime.support.adapter.gridview.UserChooserAdapter;
 import com.joewoo.ontime.support.sql.MyMaidSQLHelper;
 import com.joewoo.ontime.support.util.GlobalContext;
 import com.joewoo.ontime.ui.Login;
 import com.joewoo.ontime.ui.maintimeline.MainTimelineActivity;
 
 import static com.joewoo.ontime.support.info.Defines.TAG;
 
 /**
  * Created by JoeWoo on 14-1-4.
  */
 public class UserChooserDialog {
 
     public static void show(final Activity act) {
 
         final Dialog dialog = new Dialog(act);
 
         dialog.setTitle(R.string.frag_ftl_dialog_choose_account_title);
         dialog.setContentView(R.layout.user_chooser_dialog);
 
         GridView gv = (GridView) dialog.findViewById(R.id.user_chooser_dialog_gv);
 
         final UserChooserAdapter uAdapter = new UserChooserAdapter(act);
         final int usersCount = uAdapter.getUsersCount();
 
         gv.setAdapter(uAdapter);
 
         gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                 Log.e(TAG, "User Chose " + String.valueOf(position));
 
                 if (usersCount - position > 0) {
 
                     dialog.cancel();
 
                     Cursor c = uAdapter.getCursor();
 
                     c.move(position);
 
                     if (!c.getString(c.getColumnIndex(MyMaidSQLHelper.UID)).equals(GlobalContext.getUID())) {
                         GlobalContext.setProfileImg(c.getBlob(c.getColumnIndex(MyMaidSQLHelper.PROFILE_IMG)));
                         GlobalContext.setUID(c.getString(c.getColumnIndex(MyMaidSQLHelper.UID)));
                         GlobalContext.setScreenName(c.getString(c.getColumnIndex(MyMaidSQLHelper.SCREEN_NAME)));
                         GlobalContext.setAccessToken(c.getString(c.getColumnIndex(MyMaidSQLHelper.ACCESS_TOKEN)));
                         try {
                             GlobalContext.setDraft(c.getString(c.getColumnIndex(MyMaidSQLHelper.DRAFT)));
                             GlobalContext.setPicPath(c.getString(c.getColumnIndex(MyMaidSQLHelper.PIC_FILE_PATH)));
                         } catch (Exception e) {
                             Log.e(TAG, "No Draft of Pic Path");
                         }
 
                         MyMaidSQLHelper.setLastLogin(GlobalContext.getUID());
 
                         act.finish();
                         act.startActivity(new Intent(act, MainTimelineActivity.class));
 
                     } else {
                         Toast.makeText(act, R.string.user_chooser_dialog_choosed, Toast.LENGTH_SHORT).show();
                     }
 
                } else if(usersCount - position == 0) {
                    dialog.cancel();
                    act.finish();
                    act.startActivity(new Intent(act, Login.class));

                } else if (usersCount - position == -1) {
                     AlertDialog.Builder builder = new AlertDialog.Builder(act);
 
                     builder.setTitle(R.string.frag_ftl_dialog_confirm_logout_title);
                     builder.setPositiveButton(R.string.frag_ftl_dialog_confirm_logout_btn_ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog2, int which) {
 
                            if (GlobalContext.getSQL().delete(MyMaidSQLHelper.USER_TABLE, MyMaidSQLHelper.UID + "=?",
                                    new String[]{GlobalContext.getUID()}) > 0) {
                                 Log.e(MyMaidSQLHelper.TAG_SQL, "LOGOUT - Cleared user info");
                                 Toast.makeText(act, "<(￣︶￣)>", Toast.LENGTH_SHORT).show();
                                 GlobalContext.clear();
                                 dialog.cancel();
                             }
                             act.finish();
                             act.startActivity(new Intent(act, Login.class));
                         }
                     });
                     builder.setNegativeButton(R.string.frag_ftl_dialog_confirm_logout_btn_cancle, null);
                     builder.show();
                 }
             }
         }
         );
 
         dialog.show();
     }
 
 
 }
