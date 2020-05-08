 package edu.mines.alterego;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * Description: This file defines the fragment showing characters and stats
  * @author Matt Buland, Maria Deslis, Eric Young
  *
  */
 
 public class CharacterFragment extends Fragment {
 
     // Local data object to hold the character name and description
     CharacterData mChar;
     RefreshInterface mActRefresher;
     View mainView;
     private SimpleCursorAdapter mCharStatAdapterC;
     private Cursor statsCursor;
 
     CharacterFragment(RefreshInterface refresher) {
         super();
         mActRefresher = refresher;
     }
 
 
     /**
      * This function creates the fragment view and decides if it needs to show new character creation
      * or existing characters.
      */
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         CharacterDBHelper dbHelper = new CharacterDBHelper(getActivity());
         if (GameActivity.mCharId < 0) {
             GameActivity.mCharId = dbHelper.getCharacterIdForGame(GameActivity.mGameId);
         }
 
         // Inflate the layout for this fragment
         View characterView = inflater.inflate(R.layout.character_view, container, false);
         mainView = characterView;
 
 
         if (GameActivity.mCharId >= 0) {
             mChar = dbHelper.getCharacter(GameActivity.mCharId);
             showCharacter();
         } else {
             // Make the no-char layout visible
             LinearLayout nochar11 = (LinearLayout) characterView.findViewById(R.id.nochar_layout);
             nochar11.setVisibility(0);
 
             // Bind the new-character button to it's appropriate action
             Button newChar = (Button) characterView.findViewById(R.id.nochar_button);
             newChar.setOnClickListener( new Button.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     // Spawn the create-character dialog
 
                     AlertDialog.Builder charBuilder = new AlertDialog.Builder(v.getContext());
                     LayoutInflater inflater = getActivity().getLayoutInflater();
 
                     charBuilder.setView(inflater.inflate(R.layout.new_char_dialog, null))
                         .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int id) {
                                 AlertDialog thisDialog = (AlertDialog) dialog;
 
                                 EditText nameInput = (EditText) thisDialog.findViewById(R.id.char_name);
                                 EditText descInput = (EditText) thisDialog.findViewById(R.id.char_desc);
 
                                 String name = nameInput.getText().toString();
                                 String desc = descInput.getText().toString();
 
                                 CharacterDBHelper dbHelper = new CharacterDBHelper(getActivity());
                                 CharacterData newChar = dbHelper.addCharacter(GameActivity.mGameId, name, desc);
 
                                 mChar = newChar;
                                 GameActivity.mCharId = newChar.id;
 
                                 showCharacter();
 
                                 mActRefresher.refresh();
                             }
                         })
                         .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                             // Negative button just closes the dialog
                             @Override
                             public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
                         });
                     charBuilder.create().show();
                 }
             });
         }
 
         return characterView;
     }
 
     /**
      * This method shows character the current, or newly created character and applicable stats.
      */
     public void showCharacter() {
         // Make the no-char layout invisible
         LinearLayout nochar11 = (LinearLayout) mainView.findViewById(R.id.nochar_layout);
         nochar11.setVisibility(View.GONE);
 
         // Make the character-viewing area visible
         LinearLayout charLayout = (LinearLayout) mainView.findViewById(R.id.haschar_layout);
         charLayout.setVisibility(View.VISIBLE);
 
         // Show the character name and description
         TextView cName = (TextView) mainView.findViewById(R.id.char_name);
         TextView cDesc = (TextView) mainView.findViewById(R.id.char_desc);
 
         cName.setText(mChar.name);
         cDesc.setText(mChar.description);
 
         // Show all the character's attributes/skills/complications
         CharacterDBHelper dbHelper = new CharacterDBHelper(getActivity());
         int[] ctrlIds = new int[] {android.R.id.text1, android.R.id.text2};
         statsCursor = dbHelper.getStatsForCharCursor(mChar.id);
         mCharStatAdapterC = new SimpleCursorAdapter(this.getActivity(), android.R.layout.simple_list_item_2 , statsCursor, new String[] {"stat_name", "stat_value"} , ctrlIds, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 
         ListView statView = (ListView) mainView.findViewById(R.id.char_stats);
         statView.setAdapter(mCharStatAdapterC);
 
 
         Button newStat = (Button) mainView.findViewById(R.id.new_stat_button);
         newStat.setOnClickListener( new Button.OnClickListener() {
 
          @Override
             public void onClick(View v) {
                 // Spawn the create-character dialog
 
                 AlertDialog.Builder statBuilder = new AlertDialog.Builder(v.getContext());
                 LayoutInflater inflater = getActivity().getLayoutInflater();
 
                 statBuilder.setView(inflater.inflate(R.layout.new_stat_dialog, null))
                     .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int id) {
                             AlertDialog thisDialog = (AlertDialog) dialog;
 
                             EditText nameInput = (EditText) thisDialog.findViewById(R.id.char_stat_name);
                             EditText descInput = (EditText) thisDialog.findViewById(R.id.char_stat_val);
 
                             String name = nameInput.getText().toString();
                             String val = descInput.getText().toString();
 
                             CharacterDBHelper dbHelper = new CharacterDBHelper(getActivity());
                             dbHelper.insertCharStat(mChar.id, Integer.parseInt(val) , name, 0);
                             mCharStatAdapterC.changeCursor(dbHelper.getStatsForCharCursor(mChar.id));
                         }
                     })
                     .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                         // Negative button just closes the dialog
                         @Override
                         public void onClick(DialogInterface dialog, int id) { dialog.dismiss(); }
                     });
                 statBuilder.create().show();
             }
         });
     }
 
 }
