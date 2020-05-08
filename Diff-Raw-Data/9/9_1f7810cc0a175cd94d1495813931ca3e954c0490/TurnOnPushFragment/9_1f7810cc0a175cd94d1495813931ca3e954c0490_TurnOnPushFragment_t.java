 package pj.rozkladWKD.pj.rozkladWKD.ui;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import pj.rozkladWKD.R;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pawel
  * Date: 20.05.12
  * Time: 16:39
  * To change this template use File | Settings | File Templates.
  */
 public class TurnOnPushFragment extends SherlockDialogFragment{
     public interface TurnOnPushListener {
         void onTurnPushOn();
     }
 
    public TurnOnPushFragment() {};

     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setMessage(R.string.turn_on_push_message)
                 .setTitle(R.string.push_notifications)
                 .setIcon(R.drawable.ic_launcher_wkd)
                 .setCancelable(true)
                 .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         TurnOnPushListener listener = (TurnOnPushListener) getActivity();
                         listener.onTurnPushOn();
                         dialog.dismiss();
                     }
                 })
                 .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                     }
                 });
         return  builder.create();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 }
