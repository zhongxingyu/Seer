 package com.ioabsoftware.gameraven.views.rowview;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.text.method.ArrowKeyMovementMethod;
 import android.util.AttributeSet;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.ioabsoftware.gameraven.AllInOneV2;
 import com.ioabsoftware.gameraven.R;
 import com.ioabsoftware.gameraven.views.BaseRowData;
 import com.ioabsoftware.gameraven.views.BaseRowView;
 import com.ioabsoftware.gameraven.views.ClickableLinksTextView;
 import com.ioabsoftware.gameraven.views.RowType;
 import com.ioabsoftware.gameraven.views.StateDrawable;
 import com.ioabsoftware.gameraven.views.rowdata.MessageRowData;
 
 public class MessageRowView extends BaseRowView implements View.OnClickListener {
 	
 	View topWrapper;
 	
 	TextView user;
 	TextView post;
 	
 	LinearLayout pollWrapper;
 	
 	ClickableLinksTextView message;
 	
 	MessageRowData myData;
 	
 	StateDrawable headerSelector;
 	
 	boolean isHighlighted = false;
 	boolean isShowingPoll = false;
 
 	public MessageRowView(Context context) {
 		super(context);
 	}
 
 	public MessageRowView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	public MessageRowView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 	}
 	
 	@Override
 	protected void init(Context context) {
 		myType = RowType.MESSAGE;
 		setOrientation(VERTICAL);
         LayoutInflater.from(context).inflate(R.layout.msgview, this, true);
         
         topWrapper = findViewById(R.id.mvTopWrapper);
         
         user = (TextView) findViewById(R.id.mvUser);
     	post = (TextView) findViewById(R.id.mvPostNumber);
     	
     	pollWrapper = (LinearLayout) findViewById(R.id.mvPollWrapper);
     	
     	message = (ClickableLinksTextView) findViewById(R.id.mvMessage);
     	
     	int px = TypedValue.COMPLEX_UNIT_PX;
     	user.setTextSize(px, user.getTextSize() * AllInOneV2.getTextScale());
 		post.setTextSize(px, post.getTextSize() * AllInOneV2.getTextScale());
 		message.setTextSize(px, message.getTextSize() * AllInOneV2.getTextScale());
 		
 		headerSelector = new StateDrawable(new Drawable[] {getResources().getDrawable(R.drawable.msghead)});
 		headerSelector.setMyColor(AllInOneV2.getAccentColor());
 		topWrapper.setBackgroundDrawable(headerSelector);
 		topWrapper.setOnClickListener(this);
         
         message.setLinkTextColor(AllInOneV2.getAccentColor());
 		
 		post.setTextColor(AllInOneV2.getAccentTextColor());
 		user.setTextColor(AllInOneV2.getAccentTextColor());
 		
 		if (AllInOneV2.isAccentLight())
         	((ImageView) findViewById(R.id.mvMessageMenuIcon)).setImageResource(R.drawable.ic_info_light);
 	}
 	
 	@Override
 	public void showView(BaseRowData data) {
 		if (data.getRowType() != myType)
 			throw new IllegalArgumentException("data RowType does not match myType");
 		
 		myData = (MessageRowData) data;
 
 		topWrapper.setClickable(myData.topClickable());
 		
 		user.setText((myData.hasTitles() ? myData.getUser() + myData.getUserTitles() : myData.getUser()));
 		post.setText((myData.hasMsgID() ? "#" + myData.getPostNum() + ", " + myData.getPostTime() : myData.getPostTime()));
 		
 		if (myData.hasPoll()) {
 			isShowingPoll = true;
			pollWrapper.removeAllViews();
 			pollWrapper.addView(myData.getPoll());
			pollWrapper.setVisibility(View.VISIBLE);
 		}
 		else if (isShowingPoll) {
 			isShowingPoll = false;
 			pollWrapper.setVisibility(View.GONE);
			pollWrapper.removeAllViews();
 		}
 		
 		if (myData.getHLColor() == 0) {
 			if (isHighlighted) {
 				isHighlighted = false;
 				headerSelector.setMyColor(AllInOneV2.getAccentColor());
 			}
 		}
         else {
         	isHighlighted = true;
         	headerSelector.setMyColor(myData.getHLColor());
         }
 		
 		message.setText(myData.getSpannedMessage());
 		
     	message.setMovementMethod(ArrowKeyMovementMethod.getInstance());
     	message.setTextIsSelectable(true);
         // the autoLink attribute must be removed, if you hasn't set it then ok, otherwise call textView.setAutoLink(0);
 
 		headerSelector.invalidateDrawable(headerSelector);
 	}
 	
 	/**
 	 * @return selected text, or null if no text is selected
 	 */
 	public String getSelection() {
 		int start = message.getSelectionStart();
 		int end = message.getSelectionEnd();
 		if (start != end)
 			return message.getText().subSequence(start, end).toString();
 		else
 			return null;
 	}
 	
 	public String getMessageDetailLink() {
 		return myData.getMessageDetailLink();
 	}
 	
 	public String getUserDetailLink() {
 		return myData.getUserDetailLink();
 	}
 	
 	public boolean isEdited() {
 		return myData.isEdited();
 	}
 	
 	public String getMessageID() {
 		return myData.getMessageID();
 	}
 	
 	public String getUser() {
 		return myData.getUser();
 	}
 	
 	public boolean isEditable() {
 		return myData.isEditable();
 	}
 	
 	public String getMessageForQuoting() {
 		return myData.getMessageForQuoting();
 	}
 	
 	public String getMessageForEditing() {
 		return myData.getMessageForEditing();
 	}
 
 	@Override
 	public void onClick(View v) {
 		AllInOneV2.get().messageMenuClicked(this);
 	}
 
 }
