 package ru.noisefm.orders.fragment;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import ru.noisefm.orders.activity.MainActivity;
 
 public abstract class BaseFragment extends SherlockFragment {
     public abstract void updateActivityTitle();
 
     public boolean onBackPressed() {
         // false means that Fragment hasn't processed event and vice versa
         return false;
     }
 
     protected final MainActivity getMainActivity() {
         return (MainActivity)getSherlockActivity();
     }
 }
