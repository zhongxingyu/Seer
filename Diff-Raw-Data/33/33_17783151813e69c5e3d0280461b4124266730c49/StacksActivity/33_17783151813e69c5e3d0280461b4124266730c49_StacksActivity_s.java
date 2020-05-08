 package uk.co.ataulmunim.android.stacks.activity;
 
 import uk.co.ataulmunim.android.stacks.stack.Stack;
 import uk.co.ataulmunim.android.stacks.adapter.StacksPagerAdapter;
 import uk.co.ataulmunim.android.stacks.fragment.StackEditFragment;
 import uk.co.ataulmunim.android.view.FreezableViewPager;
 import uk.co.ataulmunim.android.widget.CroutonEx;
 
 import com.nicedistractions.shortstacks.R;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 
 
 public class StacksActivity extends Activity {
     public static final String TAG = StacksActivity.class.getSimpleName();
 
     /* "Press back to lose unsaved changes" */
     public enum UserWarnedAboutBack {
         YES, NO, UNSET
     }
 
 
     public static final int STACKS_LOADER = 0;
 
     private StacksPagerAdapter adapter;
     private FreezableViewPager pager;
     private Stack stack;
     private Menu menu;
 
     private UserWarnedAboutBack userWarned = UserWarnedAboutBack.UNSET;
     private int stackId = Stack.DEFAULT_STACK_ID;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_stacks);
 
         final Intent intent = getIntent();
         final Uri stackUri = intent.getData();
 
         // TODO: differentiate between INTENTs by performing different actions
         // final String action = intent.getAction();
 
         if (stackUri != null) {
             try {
                 stackId = Integer.parseInt(stackUri.getLastPathSegment());
             } catch (NumberFormatException e) {
                 Log.w(TAG, "Invalid stack Uri passed (" + stackUri.getLastPathSegment() + ").");
             }
         }
         stack = Stack.getStack(getContentResolver(), stackId);
 
         adapter = new StacksPagerAdapter(getFragmentManager());
         pager = (FreezableViewPager) findViewById(R.id.pager);
         pager.setFrozen(true);
         pager.setAdapter(adapter);
     }
 
     @Override
     public void onDestroy() {
         Crouton.clearCroutonsForActivity(this);
         super.onDestroy();
     }
 
     /**
      * Inflates the menu items for the action bar
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         this.menu = menu;
         super.onCreateOptionsMenu(menu);
         getActionBar().setDisplayOptions(
                 ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE,
                 ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                         | ActionBar.DISPLAY_SHOW_TITLE
         );
         getMenuInflater().inflate(R.menu.menu_activity_stacks, menu);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.ab_menu_edit:
                 onEditActionSelected();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     /**
      * Fires a warning if there are unsaved changes, but returns on second press.
      * <p/>
      * If the user is editing the stack name or notes properties of the stack and presses back, a
      * Crouton is displayed warning the user. The user can press back again to return to the
      * StackViewFragment. If the user makes subsequent changes, the warning flag is reset, and the
      * user will be warned again.
      * <p/>
      * If the user is in a StackEditFragment, the return destination is set as StackViewFragment,
      * otherwise it behaves as a regular back button (finishes the current Activity).
      */
     @Override
     public void onBackPressed() {
         if (pager.getCurrentItem() == StacksPagerAdapter.EDIT_PAGE
                 && userWarned == UserWarnedAboutBack.YES) {
 
             Crouton.clearCroutonsForActivity(this);
             CroutonEx.makeText(this, R.string.unsaved_changes, CroutonEx.INFO).show();
             softDiscardChanges();
             userWarned = UserWarnedAboutBack.NO;
 
         } else if (pager.getCurrentItem() == StacksPagerAdapter.EDIT_PAGE
                 && userWarned == UserWarnedAboutBack.NO) {
 
             Crouton.clearCroutonsForActivity(this);
             CroutonEx.makeText(this, R.string.warn_unsaved_changes, CroutonEx.WARN).show();
             userWarned = UserWarnedAboutBack.YES;
 
         } else if (pager.getCurrentItem() == StacksPagerAdapter.EDIT_PAGE
                 && userWarned == UserWarnedAboutBack.UNSET) {
 
             softDiscardChanges();
 
         } else super.onBackPressed();
     }
 
     /**
      * Get a representation of the current stack in a {@link Stack} object.
      * <p/>
      * The returned Stack isn't guaranteed to stay up-to-date, so don't keep a long-lived reference.
      *
      * @return stack the representation of the current stack as a Stack object
      */
     public Stack getStack() {
         return stack;
     }
 
     private void onEditActionSelected() {
        CroutonEx.makeText(this, "centerplease", CroutonEx.INFO).show();
//        pager.setCurrentItem(StacksPagerAdapter.EDIT_PAGE);
//        menu.clear();
//        setDoneDiscardBar();
//        ((StackEditFragment) adapter.getItem(StacksPagerAdapter.EDIT_PAGE)).updateInputFields();
     }
 
     /**
      * Inflates the DISCARD/DONE action bar, setting listeners for the buttons.
      */
     private void setDoneDiscardBar() {
         ActionBar actionBar = getActionBar();
 
         LayoutInflater inflater;
 
         if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
             inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
         } else {
             inflater = (LayoutInflater) actionBar.getThemedContext().getSystemService(LAYOUT_INFLATER_SERVICE);
         }
 
         final View doneDiscard = inflater.inflate(R.layout.actionbar_custom_view_done_discard, null);
 
         doneDiscard.findViewById(R.id.actionbar_done).setOnClickListener(
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         acceptEdits();
                     }
                 }
         );
 
         doneDiscard.findViewById(R.id.actionbar_discard).setOnClickListener(
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         softDiscardChanges();
                     }
                 }
         );
 
         actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                 ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
 
         final int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
         actionBar.setCustomView(doneDiscard, new ActionBar.LayoutParams(
                 matchParent, matchParent));
     }
 
     private void acceptEdits() {
         CroutonEx.makeText(this, "Changes saved", CroutonEx.CONFIRM).show();
 
         ((StackEditFragment) adapter.getItem(StacksPagerAdapter.EDIT_PAGE)).commitChanges(stack);
 
         pager.setCurrentItem(StacksPagerAdapter.STACKS_PAGE);
         invalidateOptionsMenu();
     }
 
     private void softDiscardChanges() {
         pager.setCurrentItem(StacksPagerAdapter.STACKS_PAGE);
         invalidateOptionsMenu();
     }
 
     public void setUserWarnedAboutBack(UserWarnedAboutBack flag) {
         userWarned = flag;
     }
 
     /**
      * Gets the local (SQL) ID of the current stack.
      *
      * @return
      */
     public int getStackId() {
         return stack.getId();
     }
 }
