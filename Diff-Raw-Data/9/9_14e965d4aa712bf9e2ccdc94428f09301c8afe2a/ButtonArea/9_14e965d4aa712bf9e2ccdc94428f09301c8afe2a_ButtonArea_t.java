 package com.github.scratch_android;
 
 import java.util.ArrayList;
 
 import com.github.scratch_android.ButtonArea.OnToggleListener;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 
 
 public class ButtonArea extends Fragment {
 	
 	private OnToggleListener toggle_listener;
 	
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		toggle_listener = (OnToggleListener) activity;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_switcher_layout, container, false);
 		new ButtonAreaManager(v, toggle_listener);
 		return v;
 	}
 	
 	public interface OnToggleListener {
 		public void onToggle(int fragment);
 	}
 }
 
 class ButtonAreaManager implements View.OnClickListener {
 	private View v;
 	private ArrayList<ToggleFragmentButton> listeners = new ArrayList<ToggleFragmentButton>();
 	OnToggleListener toggle_listener;
 	
 	public ButtonAreaManager(View v, OnToggleListener toggle_listener) {
 		this.v = v;
 		this.toggle_listener = toggle_listener;
 		construct_listeners();
 		listeners.get(0).toggle();
 	}
 	
 	public void construct_listeners() {
 		ViewGroup container = (ViewGroup) this.v.findViewById(R.id.button_container);
 		int child_count =  container.getChildCount();
 		for (int i = 0; i < child_count; i++) {
 			ToggleFragmentButton tfb = (ToggleFragmentButton) container.getChildAt(i);
 			tfb.setOnClickListener(this);
 			listeners.add(tfb);
 		}
 	}
 	@Override
 	public void onClick(View button) {
 		for (ToggleFragmentButton bt : listeners) {
 			if (bt == button) {
				if (bt.is_toggled())
					return;
 				bt.toggle();
 				toggle_listener.onToggle(bt.get_type());
 				continue;
 			}
 			if (bt.is_toggled())
 				bt.untoggle();
 		}	
 	}
 }
 
 class ToggleFragmentButton extends ImageView{
 	private String selected_image_name;
 	private String unselected_image_name;
 	private int type;
 	private boolean is_selected = false;
 	
 	public ToggleFragmentButton(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context, attrs);
 	}
 	
 	public int get_type() {
 		return this.type;
 	}
 	
 	public boolean is_toggled() {
 		return this.is_selected;
 	}
 	
 	public void untoggle() {
 		this.setImageResource(getResources().getIdentifier(this.unselected_image_name, "drawable", "com.github.scratch_android"));	
 		is_selected = false;
 	}
 	
 	public void toggle() {
 		this.setImageResource(getResources().getIdentifier(this.selected_image_name, "drawable", "com.github.scratch_android"));	
 		is_selected = true;
 	}
 	
 	private void init(Context context, AttributeSet attrs) {
 		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleFragmentButton);
 		CharSequence s = a.getString(R.styleable.ToggleFragmentButton_selected_image_name);
         this.selected_image_name = s.toString();
 
         s = a.getString(R.styleable.ToggleFragmentButton_unselected_image_name);
         this.unselected_image_name = s.toString();
         
         this.type = a.getInteger(R.styleable.ToggleFragmentButton_button_type, 1);
         a.recycle();
 	}
 }
