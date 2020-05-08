 package ltg.heliotablet_android.view.observation;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 import org.apache.commons.lang3.StringUtils;
 
 import ltg.heliotablet_android.MainActivity;
 import ltg.heliotablet_android.R;
 import ltg.heliotablet_android.R.color;
 import ltg.heliotablet_android.data.Reason;
 import ltg.heliotablet_android.view.ICircleView;
 import ltg.heliotablet_android.view.PopoverView;
 import ltg.heliotablet_android.view.PopoverViewAdapter;
 import ltg.heliotablet_android.view.PopoverView.PopoverViewDelegate;
 import ltg.heliotablet_android.view.controller.ObservationReasonController;
 import ltg.heliotablet_android.view.controller.TheoryReasonController;
 import ltg.heliotablet_android.view.theory.CircleView;
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.support.v4.view.ViewPager;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Multiset;
 
 public class ObservationCircleView extends RelativeLayout implements
 		ICircleView, PopoverViewDelegate {
 
 	private String flag;
 	private int textColor;
 	private TextView reasonTextView;
 	private ImmutableSortedSet<Reason> imReasons;
 	private String anchor;
 	private GestureDetector gestureDetector;
 
 	private RelativeLayout viewPagerLayout;
 	private Reason reasonNeedsUpdate;
 	private Multiset<String> reasonTextMultiSet;
 	private boolean isDelete = false;
 	private ObservationReasonController observationReasonController;
 	private PopoverView cachedPopoverView;
 	private OnTouchListener popoverTouchListener;
 
 	public ObservationCircleView(Context context) {
 		super(context);
 	}
 
 	public ObservationCircleView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.circle_view, this, true);
 		reasonTextView = (TextView) getChildAt(0);
 
 		TypedArray a = context.obtainStyledAttributes(attrs,
 				R.styleable.CircleView);
 		this.flag = a.getString(R.styleable.CircleView_flag);
 		this.setTextColor(a.getColor(R.styleable.CircleView_textColor,
 				color.White));
 		a.recycle();
 
 		observationReasonController = ObservationReasonController
 				.getInstance(context);
 
 		viewPagerLayout = (RelativeLayout) inflater.inflate(
 				R.layout.activity_screen_slide, this, false);
 		reasonTextView.setTextColor(this.getTextColor());
 		this.enableDoubleTap();
 	}
 	
 	public void showPopover() {
 		showPopover(imReasons);
 	}
 
 	public void showPopover(ImmutableSortedSet<Reason> popOverReasonSet) {
 		Resources resources = getResources();
 		ArrayList<View> pages = new ArrayList<View>();
 		ViewPager pager = new ViewPager(getContext());
 		ViewPager vPager = (ViewPager) viewPagerLayout
 				.findViewById(R.id.popover_pager);
 
 		// get the editable reason
 		ImmutableSortedSet<Reason> editableReasons = ImmutableSortedSet
 				.copyOf(Iterables.filter(popOverReasonSet,
 						Reason.getIsReadOnlyFalsePredicate()));
 
 		ImmutableSortedSet<Reason> nonEditableReasons = ImmutableSortedSet
 				.copyOf(Iterables.filter(popOverReasonSet,
 						Reason.getIsReadOnlyTruePredicate()));
 
 		vPager.setAdapter(new PopoverViewAdapter(pages));
 		vPager.setOffscreenPageLimit(5);
 		vPager.setCurrentItem(0);
 		ViewGroup parent = (ViewGroup) this.getParent().getParent().getParent();
 
 		PopoverView popoverView = new PopoverView(getContext(), viewPagerLayout);
 		
 		cachedPopoverView = popoverView;
 		
 		popoverTouchListener = popoverView;
 		
		popoverView.setContentSizeForViewInPopover(new Point(520, 290));
 		popoverView.setDelegate(this);
 		
 	
 		
 		popoverView.showPopoverFromRectInViewGroup(parent,
 				PopoverView.getFrameForView(this),
 				PopoverView.PopoverArrowDirectionAny, true);
 		
 		if (!editableReasons.isEmpty()) {
 			Reason reason = editableReasons.first();
 
 			View layout = View.inflate(getContext(),
 					R.layout.popover_view_obs_choose_delete, null);
 			pages.add(layout);
 
 			String flag = StringUtils.capitalize(reason.getFlag());
 			String anchor = StringUtils.capitalize(reason.getAnchor());
 
 			TextView titleText = (TextView) layout
 					.findViewById(R.id.reasonTitle);
 			String text = String
 					.format(resources.getString(R.string.obs_reason_text),
 							flag, anchor);
 			titleText.setText(text);
 
 			RadioGroup radioGroup = (RadioGroup) layout
 					.findViewById(R.id.radioGroup);
 			radioGroup.setTag(reason);
 
 			String reasonText = StringUtils.stripToNull(reason.getReasonText());
 
 			
 			boolean shouldBeEnabled = true;
 			radioGroup.setEnabled(shouldBeEnabled);
 			
 			for (int i = 0; i < radioGroup.getChildCount(); i++) {
 				RadioButton radioButton = (RadioButton) radioGroup
 						.getChildAt(i);
 
 				updateRadioButton(radioButton, flag, anchor);
 
 				if (reasonText != null
 						& radioButton.getTag().equals(reasonText)) {
 					
 					radioButton.setChecked(true);
 					radioButton.setEnabled(false);
 					shouldBeEnabled = false;
 				} 
 				
 			}
 
 			if( shouldBeEnabled == false ) {
 				for (int i = 0; i < radioGroup.getChildCount(); i++) {
 					RadioButton radioButton = (RadioButton) radioGroup
 							.getChildAt(i);
 
 					
 						radioButton.setEnabled(false);
 
 				}
 				popoverView.setOnTouchListener(popoverTouchListener);
 			} else {
 				popoverView.setOnTouchListener(null);
 			}
 			
 			radioGroup
 					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 						@Override
 						public void onCheckedChanged(RadioGroup radioGroup,
 								int checkedId) {
 							
 							Reason reason = (Reason) radioGroup.getTag();
 
 							String flag = StringUtils.capitalize(reason
 									.getFlag());
 							String anchor = StringUtils.capitalize(reason
 									.getAnchor());
 
 							RadioButton selectedRadioButton = (RadioButton) radioGroup
 									.findViewById(checkedId);
 							String text = (String) selectedRadioButton.getTag();
 							updateRadioButton(selectedRadioButton, flag, anchor);
 							cachedPopoverView.dissmissPopover(true);
 						}
 					});
 
 			Button deleteButton = (Button) layout
 					.findViewById(R.id.deleteButton);
 
 			deleteButton.setVisibility(View.VISIBLE);
 			deleteButton.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 
 					ViewGroup parent = (ViewGroup) v.getParent().getParent();
 
 					ObservationCircleView.this.isDelete = true;
 					ObservationCircleView.this.cachedPopoverView
 							.dissmissPopover(true);
 
 					
 					ImmutableSortedSet<Reason> editableReasons = ImmutableSortedSet
 							.copyOf(Iterables.filter(ObservationCircleView.this.imReasons,
 									Reason.getIsReadOnlyFalsePredicate()));
 					
 					
 					
 					if( editableReasons.size() > 0 ) {
 						MainActivity ma = (MainActivity) ObservationCircleView.this.getContext();
 						Reason reasonToDelete = editableReasons.first();
 						ma.operationObservation(reasonToDelete, reasonToDelete.getAnchor(), "remove",true);
 					}
 	
 				}
 			});
 		}
 		if (!nonEditableReasons.isEmpty()) {
 
 			StringBuilder sb = new StringBuilder();
 
 			String flag = StringUtils.capitalize(getFlag());
 			String anchor = StringUtils.capitalize(getAnchor());
 
 			String text = String.format(
 					resources.getString(R.string.obs_reason_origin_text), flag,
 					anchor);
 
 			sb.append(text);
 			sb.append("\n\n");
 			for (Reason nonEditableReason : nonEditableReasons) {
 				sb.append(nonEditableReason.getOrigin());
 				sb.append("\n");
 			}
 
 			View originLayout = View.inflate(getContext(),
 					R.layout.popover_view_obs_origin_list, null);
 			pages.add(originLayout);
 
 			EditText editText = (EditText) originLayout
 					.findViewById(R.id.originEditText);
 			editText.setText(sb.toString());
 			editText.setKeyListener(null);
 
 		}
 
 
 	}
 
 	private void updateRadioButton(RadioButton radioButton, String flag,
 			String anchor) {
 		String text = null;
 		switch (radioButton.getId()) {
 		case R.id.reasonButton1:
 			text = (String) getResources().getString(R.string.reasonButton1,
 					flag, anchor);
 			break;
 		case R.id.reasonButton2:
 			text = (String) getResources().getString(R.string.reasonButton2,
 					flag, anchor);
 			break;
 		case R.id.reasonButton3:
 			text = (String) getResources().getString(R.string.reasonButton3,
 					flag, anchor);
 			break;
 		}
 
 		radioButton.setTag(text);
 		radioButton.setText(text);
 	}
 
 	@Override
 	public void popoverViewWillShow(PopoverView view) {
 	}
 
 	@Override
 	public void popoverViewDidShow(PopoverView view) {
 
 	}
 
 	public void popoverViewWillDismiss(PopoverView view) {
 
 		if (isDelete == false) {
 			ViewPager vPager = (ViewPager) view
 					.findViewById(R.id.popover_pager);
 			PopoverViewAdapter adapter = (PopoverViewAdapter) vPager
 					.getAdapter();
 			ViewGroup radioView = (ViewGroup) adapter.getView(0);
 
 			if (radioView != null) {
 				RadioGroup radioGroup = (RadioGroup) radioView
 						.findViewById(R.id.radioGroup);
 
 				if (radioGroup != null) {
 
 					for (int i = 0; i < radioGroup.getChildCount(); i++) {
 						RadioButton radioButton = (RadioButton) radioGroup
 								.getChildAt(i);
 
 						if (radioButton.isEnabled() && radioButton.isChecked()) {
 							String selectedText = (String) radioButton.getTag();
 
 							reasonNeedsUpdate = null;
 							Reason newReason = Reason
 									.newInstance((Reason) radioGroup.getTag());
 							newReason.setReasonText(selectedText);
 							reasonNeedsUpdate = newReason;
 							MainActivity ma = (MainActivity) ObservationCircleView.this.getContext();
 							ma.operationObservation(reasonNeedsUpdate, reasonNeedsUpdate.getAnchor(), "update", true);
 							this.makeToast("Reason Updated");
 							reasonNeedsUpdate = null;
 						}
 
 					}
 				}
 
 			}
 		}
 
 	}
 
 	@Override
 	public void popoverViewDidDismiss(PopoverView view) {
 		// lets update
 		if (reasonNeedsUpdate != null && isDelete == false) {
 
 			Reason newInstance = Reason.newInstance(reasonNeedsUpdate);
 
 //			MainActivity ma = (MainActivity) ObservationCircleView.this.getContext();
 //			ma.operationObservation(reasonNeedsUpdate, reasonNeedsUpdate.getAnchor(), "update", true);
 //			this.makeToast("Reason Updated");
 //			reasonNeedsUpdate = null;
 		} else {
 			// just deleted reset the flag
 			isDelete = false;
 
 		}
 
 	}
 
 	public int getReasonsSize() {
 		if (imReasons != null) {
 			imReasons.size();
 		}
 		return 0;
 	}
 
 	@Override
 	public void setTag(Object tag) {
 		super.setTag(tag);
 
 		imReasons = (ImmutableSortedSet<Reason>) tag;
 
 		if (imReasons != null) {
 			this.reasonTextView.setText("" + imReasons.size());
 			// also make a multiset
 		}
 
 	}
 
 	public void makeToast(String toastText) {
 		Toast toast = Toast.makeText(getContext(), toastText,
 				Toast.LENGTH_SHORT);
 		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 50);
 		toast.show();
 	}
 
 	public String getFlag() {
 		return flag;
 	}
 
 	public void setFlag(String flag) {
 		this.flag = flag;
 	}
 
 	public void setReasonText(String reasonText) {
 		this.reasonTextView.setText(reasonText);
 	}
 
 	public String getReasonText() {
 		return this.reasonTextView.getText().toString();
 	}
 
 	public int getTextColor() {
 		return textColor;
 	}
 
 	public void setTextColor(int textColor) {
 		this.textColor = textColor;
 		reasonTextView.setTextColor(textColor);
 	}
 
 	public void setReducedAlpha(float circleViewAlpha) {
 		Drawable background = getBackground();
 		background.setAlpha((int) circleViewAlpha);
 		setBackground(background);
 		reasonTextView.setAlpha(circleViewAlpha);
 	}
 
 	@Override
 	public String toString() {
 		return "Flag: " + this.getFlag() + " Reasons: " + imReasons.toString();
 	}
 
 	public String getAnchor() {
 		return anchor;
 	}
 
 	public void setAnchor(String anchor) {
 		this.anchor = anchor;
 	}
 
 	public void enableDoubleTap() {
 		gestureDetector = new GestureDetector(getContext(),
 				new CircleDoubleTapGestureListener());
 		this.setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				gestureDetector.onTouchEvent(event);
 				return true;
 			}
 		});
 	}
 
 	public Multiset<String> getReasonTextMultiSet() {
 		return reasonTextMultiSet;
 	}
 
 	public void setReasonTextMultiSet(Multiset<String> reasonTextMultiSet) {
 		this.reasonTextMultiSet = reasonTextMultiSet;
 	}
 
 	class CircleDoubleTapGestureListener extends
 			GestureDetector.SimpleOnGestureListener {
 		// event when double tap occurs
 		@Override
 		public boolean onDoubleTap(MotionEvent e) {
 
 			showPopover(imReasons);
 			return super.onDoubleTap(e);
 		}
 	}
 
 }
