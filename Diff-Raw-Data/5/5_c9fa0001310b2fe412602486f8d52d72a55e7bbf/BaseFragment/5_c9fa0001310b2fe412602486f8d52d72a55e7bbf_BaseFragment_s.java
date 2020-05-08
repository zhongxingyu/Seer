 package lv.jug.javaday.androidapp.presentation;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import butterknife.Views;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.squareup.otto.Bus;
 
 import javax.inject.Inject;
 
 public abstract class BaseFragment extends SherlockFragment {
 
     @Inject
     Bus bus;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         BaseApplication.inject(this);
         View view = inflater.inflate(contentViewId(), container, false);
         Views.inject(this, view);
         init(savedInstanceState);
 
         return view;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         bus.register(this);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         bus.unregister(this);
     }
 
     protected void init(Bundle savedInstanceState){}
 
     protected abstract int contentViewId();
 
     public MainActivity getMainActivity() {
         return (MainActivity) getActivity();
     }
 
 }
