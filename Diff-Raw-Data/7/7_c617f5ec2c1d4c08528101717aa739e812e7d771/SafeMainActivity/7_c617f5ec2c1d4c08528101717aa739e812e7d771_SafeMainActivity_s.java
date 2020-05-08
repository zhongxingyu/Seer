 package com.pnegre.safe;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import com.pnegre.safe.database.Database;
 import com.pnegre.safe.database.EncryptedDatabase;
 import com.pnegre.safe.database.SQLDatabase;
 import com.pnegre.safe.database.Secret;
 
 import java.util.List;
 
 
 public class SafeMainActivity extends ListActivity {
     private SafeApp mApp;
     private boolean mShowingDialog = false;
     private ViewGroup mHeader;
 
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mApp = (SafeApp) getApplication();
         ListView lv = getListView();
         LayoutInflater inflater = getLayoutInflater();
         setAdapter(mApp.getDatabase());
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mApp.setMenuVisibility(false);
         Database db = mApp.getDatabase();
         if (db != null)
             db.destroy();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (mShowingDialog) return;
 
         Database db = mApp.getDatabase();
        if (db == null) return;
 
         if (db.ready() == true)
             setAdapter(db);
     }
 
 
     // Inflate res/menu/mainmenu.xml
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.mainmenu, menu);
 
         if (mApp.menuVisible()) {
             menu.findItem(R.id.newsecret).setVisible(true);
             menu.findItem(R.id.importsecrets).setVisible(true);
             menu.findItem(R.id.exportsecrets).setVisible(true);
             menu.findItem(R.id.changepw).setVisible(true);
         }
 
         return true;
     }
 
 
     // Respond to user click on menu
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.changepw:
                 showChangePasswordDialog();
                 return true;
 
             case R.id.newsecret:
                 newSecret();
                 return true;
 
             case R.id.exportsecrets:
                 exportSecrets();
                 return true;
 
             case R.id.importsecrets:
                 importSecrets();
                 return true;
 
             case R.id.newpass:
                 newPassword();
 
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void newPassword() {
         Intent i = new Intent(this, NewPasswordActivity.class);
         startActivity(i);
     }
 
     private void showChangePasswordDialog() {
         final Dialog dialog = new Dialog(this);
         dialog.setTitle("Change Password");
         dialog.setContentView(R.layout.dialogchangepassword);
         dialog.show();
 
         Button dialogButton = (Button) dialog.findViewById(R.id.butokchpass);
         final EditText et1 = (EditText) dialog.findViewById(R.id.pw1);
         final EditText et2 = (EditText) dialog.findViewById(R.id.pw2);
         dialogButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String pw1 = et1.getText().toString();
                 String pw2 = et2.getText().toString();
                 if (!pw1.equals(pw2))
                     showToast("Passwords don't match");
                 else {
                     changePassword(pw1);
                     dialog.dismiss();
                 }
             }
         });
     }
 
     private void changePassword(String newPass) {
         // TODO: fer més proves. Sembla que ja funciona el canvi de pass, però fer més proves...
         Database db = mApp.getDatabase();
         List<Secret> secrets = db.getSecrets();
         db.wipe();
 
         // TODO: millorar això: no m'agrada gaire ignorar excepcions...
         try {
             db = new EncryptedDatabase(new SQLDatabase(this), this, newPass, true);
             for (Secret s : secrets) {
                 db.newSecret(s);
             }
 
             mApp.setDatabase(db);
             mApp.masterPassword = newPass;
             setAdapter(db);
             showToast("Password updated!");
         } catch (Exception e) { }
     }
 
     private void importSecrets() {
         try {
             final Backup backup = new Backup(mApp.getDatabase());
             AlertDialog.Builder alert = new AlertDialog.Builder(this);
             alert.setTitle("Choose");
             final CharSequence[] items = backup.enumerateFiles();
             if (items.length == 0) {
                 showToast("No backups detected");
                 return;
             }
 
             alert.setItems(items, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialogInterface, int item) {
                     try {
                         String chosen = (String) items[item];
                         backup.doImport(chosen, mApp.masterPassword);
                     } catch (Exception e) {
                         showToast("Can't import!");
                         Log.v("SAFE", "Exception importing: " + e.toString());
                     }
                     setAdapter(mApp.getDatabase());
                 }
             });
             alert.show();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     // Exportar secrets mitjançant un XML (segurament també encriptat)
     private void exportSecrets() {
         try {
             Backup backup = new Backup(mApp.getDatabase());
             String filename = backup.doExport(mApp.masterPassword);
             showToast(filename + " saved.");
 
         } catch (Exception e) {
             e.printStackTrace();
             showToast("Can't save file");
         }
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         Secret item = (Secret) getListAdapter().getItem(position);
         int secretId = item.id;
         Intent i = new Intent(this, ShowSecretActivity.class);
         i.putExtra("secretid", secretId);
         startActivity(i);
     }
 
     private void showToast(String message) {
         Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
     }
 
 
     private void setAdapter(Database db) {
         try {
             if (!db.ready()) return;
             List<Secret> secrets = db.getSecrets();
             Secret[] secretsArray = new Secret[secrets.size()];
             secrets.toArray(secretsArray);
             ArrayAdapter<Secret> adapter = new ArrayAdapter<Secret>(this, android.R.layout.simple_list_item_1, secretsArray);
             setListAdapter(adapter);
             ListView lv = getListView();
             lv.removeHeaderView(mHeader);
 
         } catch (Exception e) {
             Log.d(SafeApp.LOG_TAG, "Problem in setAdapter (SafeMainActivity class)");
             e.printStackTrace();
         }
     }
 
     private void newSecret() {
         Intent i = new Intent(this, NewSecretActivity.class);
         startActivity(i);
     }
 
 }
