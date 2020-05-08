 package be.quentinloos.manille.gui.dialogs;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 import be.quentinloos.manille.R;
 
 /**
  * The end dialog to display the winner
  *
  * @author Quentin Loos <contact@quentinloos.be>
  */
 public class GameOverDialog extends DialogFragment {
 
     public static GameOverDialog newInstance(String team, int points) {
         GameOverDialog dialog = new GameOverDialog();
         Bundle args = new Bundle();
         args.putString("team", team);
         args.putInt("points", points);
         dialog.setArguments(args);
         return dialog;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         return new AlertDialog.Builder(this.getActivity())
                 .setTitle(R.string.title_dialog_game_over)
                 .setMessage(String.format(getString(R.string.message_dialog_game_over),
                         getArguments().get("team"), getArguments().get("points")))
                .setInverseBackgroundForced(true)
 
                  // Listener for the 'OK' button
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
 
                     }
                 })
 
                 .create();
     }
 }
