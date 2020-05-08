 package com.chethan.contact;
 
 import java.util.ArrayList;
 
 import com.chethan.services.ContactService;
 import com.chethan.utils.Utils;
 
 import android.R.integer;
 import android.graphics.AvoidXfermode.Mode;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.view.DragEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnDragListener;
 import android.view.View.OnGenericMotionListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.MarginLayoutParams;
 import android.widget.AbsListView;
 import android.widget.AbsoluteLayout;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class PeopleFragment extends Fragment {
 
 	public static ContactService contactService;
 	private ArrayList<String> contactNameList = new ArrayList<String>();
 	private int contactListPosition = 0;
 	
 	private TextView contact_2;
 	private TextView contact_1;
 	private TextView contact;
 	private TextView contact1;
 	private TextView contact2;
 	private TextView alphabetTextView;
 	
 	public static final PeopleFragment newInstance(ContactService service)
 	 {
 		PeopleFragment f = new PeopleFragment();
 	   contactService=service;
 	   return f;
 	 }
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.people_all, container, false);
 		
 		final View singleContact = (View)view.findViewById(R.id.single_contact);
 		android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Utils.getWidthForContact(getActivity()), android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
 		singleContact.setLayoutParams(params);
 		final TextView textView = (TextView)view.findViewById(R.id.alphabets);
 		android.widget.LinearLayout.LayoutParams alphabets_params = new LinearLayout.LayoutParams(Utils.getWidthForAlphabets(getActivity()), android.widget.FrameLayout.LayoutParams.MATCH_PARENT);
 		textView.setLayoutParams(alphabets_params);
 		
 		contact_2 = (TextView)view.findViewById(R.id.SingleContactName_2);
 		contact_1 = (TextView)view.findViewById(R.id.SingleContactName_1);
 		contact = (TextView)view.findViewById(R.id.SingleContactName);
 		contact1 = (TextView)view.findViewById(R.id.SingleContactName1);
 		contact2 = (TextView)view.findViewById(R.id.SingleContactName2);
 		alphabetTextView = (TextView)view.findViewById(R.id.alphabets);
 		
 		contactNameList = contactService.getContactNameList();
 		scroll();
 		
 		view.setOnTouchListener(new OnTouchListener() {
 			
 			@Override
 			public boolean onTouch(View arg0, MotionEvent arg1) {
 				switch (arg1.getAction()) {
 				case MotionEvent.ACTION_MOVE:
 					if(arg1.getY()>(singleContact.getY()+singleContact.getHeight())){
						singleContact.setTranslationY(singleContact.getY()+10);
 						contactListPosition++;
 						scroll();
 					}else if(arg1.getY()<(singleContact.getY())){
						singleContact.setTranslationY(singleContact.getY()-10);
 						contactListPosition--;
 						scroll();
 					}
 					else{
 						singleContact.setTranslationY(arg1.getY()-singleContact.getHeight()/2);
						float step = (Utils.getScreenHeight(getActivity())/contactNameList.size());
 						contactListPosition = (int)(arg1.getY()/step);
 						scroll();
 					}
 					break;
 
 				default:
 					break;
 				}
 				
 				return true;
 			}
 		});
 		
 		return view;
 	}
 	
 	private void scroll(){
 		
 		if(contactListPosition>=contactNameList.size())
 			contactListPosition=contactNameList.size()-1;
 		
 		if(contactListPosition<0)
 			contactListPosition=0;
 		int position = contactListPosition;
 	//	contactListPosition=position;
 		
 		if(position>2 && position<contactNameList.size()){
 			contact_2.setText(contactNameList.get(position-2));
 		}else{
 			contact_2.setText("");
 		}
 		if(position>1 && position<contactNameList.size()){
 			contact_1.setText(contactNameList.get(position-1));
 		}else{
 			contact_1.setText("");
 		}
 		if(position>=0 && position<contactNameList.size()){
 			contact.setText(contactNameList.get(position));
 		}
 		if(position+1<contactNameList.size()){
 			contact1.setText(contactNameList.get(position+1));
 		}else{
 			contact1.setText("");
 		}
 		if(position+2<contactNameList.size()){
 			contact2.setText(contactNameList.get(position+2));
 		}else {
 			contact2.setText("");
 		}
 		
 		highlightAlphabets();
 	}
 	
 	private void highlightAlphabets(){
 		String textToHighlight = contactNameList.get(contactListPosition).substring(0, 1);
 //		String.replaceAll(textToHighlight,<font color="red">textToHighlight</font>);
 		Spannable WordtoSpan = new SpannableString("A\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nM\nN\nO\nP\nQ\nR\nS\nT\nU\nV\nW\nX\nY\nZ");        
 		WordtoSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#00B4FF")), WordtoSpan.toString().indexOf(textToHighlight), WordtoSpan.toString().indexOf(textToHighlight)+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 		alphabetTextView.setText(WordtoSpan);
 //		Textview.setText(Html.fromHtml(String));
 	}
 	
 }
