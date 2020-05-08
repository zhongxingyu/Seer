 package be.quentinloos.manille.gui.dialogs;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import be.quentinloos.manille.R;
 import be.quentinloos.manille.core.Manille;
 import be.quentinloos.manille.core.Turn;
 import be.quentinloos.manille.gui.activities.MainActivity;
 
 /**
  * A dialog to add a turn
  *
  * @author Quentin Loos <contact@quentinloos.be>
  */
 public class AddTurnDialog extends DialogFragment {
 
     SharedPreferences preferences;
 
     private RadioGroup rg;
     private EditText pointsTeam1, pointsTeam2;
     private Spinner spinner;
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         LayoutInflater inflater = getActivity().getLayoutInflater();
         final View view = inflater.inflate(R.layout.dialog_add_turn, null);
 
         // RadioButton for teams
         rg = (RadioGroup) view.findViewById(R.id.radio_group_team);
         RadioButton r1 = (RadioButton) rg.findViewById(R.id.radio_team1);
         RadioButton r2 = (RadioButton) rg.findViewById(R.id.radio_team2);
 
         final Manille manille = ((MainActivity) getActivity()).getManille();
 
         // Check the good radiobutton
         if (manille.getNbrTurns() > 0) {
             if (manille.getTurns().get(manille.getNbrTurns() - 1).getTeam() == 1)
                 rg.check(R.id.radio_team2);
             else if (manille.getTurns().get(manille.getNbrTurns() - 1).getTeam() == 2)
                 rg.check(R.id.radio_team1);
         }
 
         preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
         r1.setText(preferences.getString("team1", getString(R.string.default_team1)));
         r2.setText(preferences.getString("team2", getString(R.string.default_team2)));
 
         // EditText for points of the turn
         pointsTeam1 = (EditText) view.findViewById(R.id.points1);
         pointsTeam2 = (EditText) view.findViewById(R.id.points2);
 
         // Listener to autocomplete points
         pointsTeam1.addTextChangedListener(getTextWatcher(pointsTeam1, pointsTeam2));
         pointsTeam2.addTextChangedListener(getTextWatcher(pointsTeam2, pointsTeam1));
 
         // Spinner to choose the trump suit
         spinner = (Spinner) view.findViewById(R.id.spinner);
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.trump, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(adapter);
 
         return new AlertDialog.Builder(this.getActivity())
                 .setTitle(R.string.action_add)
                 .setView(view)
 
                 // Listener for the 'OK' button
                 .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         int score1 = 0, score2 = 0;
 
                         if (rg.getCheckedRadioButtonId() == -1) {
                             Toast.makeText(view.getContext(), getString(R.string.no_team_selected), Toast.LENGTH_SHORT).show();
                             return;
                         }
 
                         try {
                             score1 = Integer.parseInt(pointsTeam1.getText().toString());
                             score2 = Integer.parseInt(pointsTeam2.getText().toString());
                         } catch (NumberFormatException e) {
                             AddTurnDialog.this.getDialog().cancel();
                             Toast.makeText(view.getContext(), getString(R.string.exception_score), Toast.LENGTH_SHORT).show();
                         }
 
                         boolean double1 = ((CheckBox) view.findViewById(R.id.turn_double1)).isChecked();
                         boolean double2 = ((CheckBox) view.findViewById(R.id.turn_double2)).isChecked();
 
                         // Wich team have chosen the trump suit
                         int team = 0;
                         if (rg.getCheckedRadioButtonId() == R.id.radio_team1)
                             team = 1;
                         else if (rg.getCheckedRadioButtonId() == R.id.radio_team2)
                             team = 2;
                         Turn.Trump trump = Turn.Trump.values()[spinner.getSelectedItemPosition()];
 
                         // How many draw play before ?
                         int reportedMult= 0;
                         if (manille.getNbrTurns() > 0 && manille.getTurns().get(manille.getNbrTurns() - 1).isDraw())
                             reportedMult += manille.getTurns().get(manille.getNbrTurns() - 1).getMult();
 
                         // multiplication factor
                         String multStr = preferences.getString("mult", "Addition");
                         boolean mult = false;
                         if (multStr.equals("Addition"))
                             mult = false;
                         else if(multStr.equals("Multiplication"))
                             mult = true;
 
                         try {
                             manille.addTurn(new Turn(score1, score2, team, trump, double1, double2, reportedMult, mult));
                             ((MainActivity) getActivity()).refreshMainFragment();
                         } catch (IllegalArgumentException e) {
                             Toast.makeText(getActivity(), getString(R.string.exception_score), Toast.LENGTH_SHORT).show();
                         }
                     }
                 })
 
                 // Listener for the 'Cancel' button
                 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         AddTurnDialog.this.getDialog().cancel();
                     }
                 })
 
                 .create();
     }
 
     /**
      * This method returns a TextWatcher, a listener for EditText, who listens the first EditText,
      * computes the points of the other team and autocomplete it in the second EditText.
      *
      * @param points1EditText The EditText who have the focus
      * @param points2EditText The EditText to autocomplete
      * @return a configured TextWatcher
      */
     public TextWatcher getTextWatcher(final EditText points1EditText, final EditText points2EditText) {
         return new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 if(points1EditText.isFocused()) {
                     try {
                         int points1 = Integer.parseInt(s.toString()), points2;
                         if (points1 < 61) {
                             points2 = 60 - points1;
                             points2EditText.setText(String.valueOf(points2));
                         }
                     } catch (NumberFormatException e) {
                         points2EditText.setText("");
                     }
                 }
             }
 
             @Override
             public void afterTextChanged(Editable s) {}
         };
     }
 }
