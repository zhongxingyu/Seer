 package com.hyperactivity.android_app.activities;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import com.hyperactivity.android_app.R;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.core.ScrollPicker;
 import com.hyperactivity.android_app.forum.models.Category;
 
 import java.util.Iterator;
 
 public class CreateThreadFragment extends Fragment {
     private ScrollPicker scrollPicker;
     private EditText headlineEditText;
     private EditText textEditText;
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.create_thread_fragment, null);
 
         headlineEditText = (EditText) view.findViewById(R.id.headline_textfield);
         textEditText = (EditText) view.findViewById(R.id.text_textfield);
 
         scrollPicker = (ScrollPicker) view.findViewById(R.id.forum_categories_surface_view);
         scrollPicker.getThread().setState(ScrollPicker.ScrollPickerThread.STATE_READY);
 
         if (((Engine) getActivity().getApplication()).getPublicForum().getCategories().size() > 0) {
             //Categories already loaded.
 
             Iterator<Category> it = ((Engine) getActivity().getApplication()).getPublicForum().getCategories().iterator();
 
             while (it.hasNext()) {
                 Category category = it.next();
 
                 scrollPicker.getItemManager().addItem(category.getImage(getActivity()), category.getHeadLine(), Color.BLACK, category);
             }
         } else {
             //not loaded, tell forum to load.
             ((Engine) getActivity().getApplication()).getPublicForum().loadCategories(getActivity(), false);
         }
 
         final EditText headline = headlineEditText;
         final EditText text = textEditText;
         final Button button = (Button) view.findViewById(R.id.create_button);
         button.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 String h = headline.getText().toString().trim();
                 String t = text.getText().toString().trim();
                 ((Engine) getActivity().getApplication()).getPublicForum().createThread(getActivity(), scrollPicker.getThread().getItemManager().getSelectedItem().getCategory().getId(), headlineEditText.getText().toString().trim(), textEditText.getText().toString().trim(), true);
             }
         });
 
         return view;
     }
 
     public void updateCategories() {
         if (scrollPicker != null && scrollPicker.getThread().isReady()) {
             scrollPicker.reset();
 
             Iterator<Category> it = ((Engine) getActivity().getApplication()).getPublicForum().getCategories().iterator();
 
             while (it.hasNext()) {
                 Category category = it.next();
 
                 scrollPicker.getItemManager().addItem(category.getImage(getActivity()), category.getHeadLine(), Color.BLACK, category);
             }
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         scrollPicker.getThread().pause();
 
         InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(textEditText.getWindowToken(), 0);
         imm.hideSoftInputFromWindow(headlineEditText.getWindowToken(), 0);
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         scrollPicker.getThread().unpause();
     }
 }
