 package ru.noisefm.orders.fragment;
 
import android.app.Activity;
 import com.actionbarsherlock.app.SherlockFragment;
 import ru.noisefm.orders.activity.MainActivity;
 
 public abstract class BaseFragment extends SherlockFragment {
     public abstract void updateActivityTitle();
 
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update title after getting fragment from navigation stack
        updateActivityTitle();
    }

     public boolean onBackPressed() {
         // false means that Fragment hasn't processed event and vice versa
         return false;
     }
 
     protected final MainActivity getMainActivity() {
         return (MainActivity)getSherlockActivity();
     }
 }
