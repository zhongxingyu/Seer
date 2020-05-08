 /*******************************************************************
  * Copyright (c) 2010 Rohit Kumbhar
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  *******************************************************************/
 package net.yama.android.views.components;
 
 import net.yama.android.R;
 import net.yama.android.managers.DataManager;
 import net.yama.android.managers.connection.ApplicationException;
 import net.yama.android.requests.write.WriteRsvp;
 import net.yama.android.response.Event;
 import net.yama.android.response.Rsvp;
 import net.yama.android.response.Event.Rsvpable;
 import net.yama.android.response.Rsvp.RsvpResponse;
 import net.yama.android.util.Constants;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EventRsvpView extends TableLayout {
 
 	private static final int GUESTS_ID = 0x5478;
 	private static final int COMMENTS_ID = 0x5479;
 	Context context;
 	Event event;
 	Rsvp rsvp;
 	RsvpButtonListener listener;
 	private TextView curRsvp;
 	public EventRsvpView(Context context, Event event) throws ApplicationException {
 		super(context);
 		this.context = context;
 		this.event = event;
 		this.rsvp = DataManager.getMyRsvp(event.getId());;
 		
 		// Title
 		TextView titleView = new TextView(context);
 		titleView.setText(event.getName());
 		titleView.setTypeface(Typeface.DEFAULT_BOLD);
 		titleView.setTextSize(19.0F);
 		titleView.setPadding(2, 2, 2, 2);
 		titleView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 		this.addView(titleView);
 		
 		// Event Information
 		String rsvpString = getRsvpString(event);
 		String feeString = getFeeString(event);
 		TextView rsvpStatsView = new TextView(context);
 		rsvpStatsView.setText(rsvpString + feeString);
 		rsvpStatsView.setPadding(1,1,1,3);
//		rsvpStatsView.setTextSize(12.0F);
 		rsvpStatsView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 		this.addView(rsvpStatsView);
 		
 		// RSVP Pre
 		TextView rsvpPre = new TextView(context);
 		rsvpPre.setText(R.string.youRsvped);
 		rsvpPre.setTypeface(Typeface.DEFAULT_BOLD);
 		
 		curRsvp = new TextView(context);
 		curRsvp.setText(getCurrentRSVPString(this.rsvp));
 		if(this.rsvp != null){
 			curRsvp.setText(getCurrentRSVPString(this.rsvp));
 			curRsvp.setTextColor((this.rsvp.getResponse() != null ? this.rsvp.getResponse().getColor() : Color.WHITE));
 			curRsvp.setTypeface(Typeface.DEFAULT_BOLD);
 		}
 		
 		TableLayout temp = new TableLayout(context);
 		TableRow row = new TableRow(context);
 		row.addView(rsvpPre);
 		row.addView(curRsvp);
 		row.addView(new TextView(context));
 		temp.addView(row);
 		this.addView(temp);
 		
 		
 		// Comment
 		TextView guestsLabel = new TextView(context);
 		guestsLabel.setText(R.string.guestsLabel);
 		guestsLabel.setTypeface(Typeface.DEFAULT_BOLD);
 		this.addView(guestsLabel);
 		EditText guestsText = new EditText(context);
 		guestsText.setId(GUESTS_ID);
 		guestsText.setInputType(InputType.TYPE_CLASS_NUMBER);
 		this.addView(guestsText);
 		
 		
 		// Comment
 		TextView haveAComment = new TextView(context);
 		haveAComment.setText(R.string.haveAComment);
 		haveAComment.setTypeface(Typeface.DEFAULT_BOLD);
 		this.addView(haveAComment);
 		EditText rsvpComment = new EditText(context);
 		rsvpComment.setId(COMMENTS_ID);
 		rsvpComment.setLines(2);
 		this.addView(rsvpComment);
 	
 		
 		addRsvpButtonsView(context, event, this.rsvp);
 		
 	}
 
 	private void addRsvpButtonsView(Context context, Event event, Rsvp rsvp) {
 		
 		if(event.isRsvpClosed() || Rsvpable.CLOSED.equals(event.getRsvpable()) || (rsvp != null && !rsvp.getResponse().equals(RsvpResponse.YES) && Rsvpable.FULL.equals(event.getRsvpable()))){
 			TextView rsvpClosed = new TextView(context);
 			rsvpClosed.setText(R.string.rsvpClosed);
 			rsvpClosed.setGravity(Gravity.CENTER);
 			rsvpClosed.setTextColor(Color.RED);
 			this.addView(rsvpClosed);
 			return;
 		}
 		
 		listener = new RsvpButtonListener();
 		Button yesBtn = new Button(context);
 		yesBtn.setId(R.string.yes);
 		yesBtn.setText(R.string.yes);
 		yesBtn.setPadding(2, 2, 2, 2);
 		yesBtn.setGravity(Gravity.CENTER);
 		yesBtn.setOnClickListener(listener);
 		yesBtn.setMinWidth(100);
 		yesBtn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 		
 		Button noBtn = new Button(context);
 		noBtn.setId(R.string.no);
 		noBtn.setText(R.string.no);
 		noBtn.setPadding(2, 2, 2, 2);
 		noBtn.setGravity(Gravity.CENTER);
 		noBtn.setOnClickListener(listener);
 		noBtn.setMinWidth(100);
 		noBtn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 		
 		Button maybeBtn = new Button(context);
 		maybeBtn.setId(R.string.maybe);
 		maybeBtn.setText(R.string.maybe);
 		maybeBtn.setPadding(2, 2, 2, 2);
 		maybeBtn.setGravity(Gravity.CENTER);
 		maybeBtn.setOnClickListener(listener);
 		maybeBtn.setMinWidth(100);
 		maybeBtn.setEnabled(event.isAllowMaybeRsvp());
 		maybeBtn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 		
 		
 		RelativeLayout rel = new RelativeLayout(context);
 		rel.setPadding(2, 2, 2, 2);
 		RelativeLayout.LayoutParams noBtnParams = new RelativeLayout.LayoutParams(android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
 		noBtnParams.addRule(RelativeLayout.RIGHT_OF,R.string.maybe);
 		rel.addView(noBtn, noBtnParams);
 		
 		RelativeLayout.LayoutParams maybeBtnParams = new RelativeLayout.LayoutParams(android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
 		maybeBtnParams.addRule(RelativeLayout.CENTER_IN_PARENT);
 		rel.addView(maybeBtn, maybeBtnParams);
 		
 		
 		
 		RelativeLayout.LayoutParams yesBtnParams = new RelativeLayout.LayoutParams(android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
 		yesBtnParams.addRule(RelativeLayout.LEFT_OF,R.string.maybe);
 		rel.addView(yesBtn, yesBtnParams);
 		
 		this.addView(rel);
 		
 	}
 
 	private String getCurrentRSVPString(Rsvp rsvp) {
 		return (rsvp != null ? " " + rsvp.getResponse().getIdString() : " You have not RSVPed yet!" );
 	}
 
 	private String getFeeString(Event event) {
 		
 		StringBuilder feeStr = new StringBuilder();
 		feeStr.append("Fee: " + event.getFee()).append(" ").append(event.getFeeCurrency()).append("\n");
 		feeStr.append("Fee Description: ").append(event.getFeeDescription());
 		return feeStr.toString();
 	}
 
 	private String getRsvpString(Event event) {
		
 		StringBuilder rsvpStr = new StringBuilder();
		rsvpStr.append("RSVP Limit: ").append(event.getRsvpLimit()).append("\n");
		rsvpStr.append("Open spots: ").append(event.getRsvpCount() - event.getNoRsvpCount()).append("\n");
 		return rsvpStr.toString();
 	}
 
 	
 	public class RsvpButtonListener implements View.OnClickListener{
 		
 		public void onClick(View v) {
 			
 			Toast t = Toast.makeText(context,"Saving your rsvp..." , Toast.LENGTH_LONG);
 			t.show();
 			int id = v.getId();
 			String rsvp = null;
 			if(R.string.yes == id)
 				rsvp = "yes";
 			else if(R.string.no == id)
 				rsvp = "no";
 			else if(R.string.maybe == id)
 				rsvp = "maybe";
 			
 			
 			EditText guestsView = (EditText) findViewById(GUESTS_ID);
 			String numOfGuests = guestsView.getText().toString();
 			
 			EditText commentsView = (EditText) findViewById(COMMENTS_ID);
 			String comments = commentsView.getText().toString();
 			
 			WriteRsvp request = new WriteRsvp();
 			request.addParameter(Constants.EVENT_ID_KEY, event.getId());
 			request.addParameter(Constants.RSVP_RESPONSE, rsvp);
 			
 			if(numOfGuests != null && numOfGuests.length() > 0 )
 				request.addParameter(Constants.RSVP_GUESTS, numOfGuests);
 			
 			if(comments != null && comments.length() > 0 )
 				request.addParameter(Constants.RSVP_COMMENTS, comments);
 			
 			String response = null;
 			try {
 				DataManager.updateRsvp(request);
 				
 				curRsvp.setText(RsvpResponse.valueOf(rsvp.toUpperCase()).getIdString());
 				response = "Your RSVP has been updated";
 				
 			} catch (ApplicationException e) {
 				Log.e("RsvpButtonListener", "Exception occured", e);
 				response = e.getMessage();
 			}
 			t.cancel();
 			t = Toast.makeText(context,response, Toast.LENGTH_LONG);
 			postInvalidate();
 			t.show();
 		}
 		
 	}
 
 }
 
