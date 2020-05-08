 /*
 DialerField.java
 Copyright (C) 2010  Belledonne Communications, Grenoble, France
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package org.linphone.jlinphone.gui;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.pim.Contact;
 import javax.microedition.pim.PIMException;
 
 import org.linphone.core.CallDirection;
 import org.linphone.core.LinphoneAddress;
 import org.linphone.core.LinphoneCore;
 import org.linphone.core.LinphoneCoreException;
 import org.linphone.jortp.JOrtpFactory;
 import org.linphone.jortp.Logger;
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.Characters;
 import net.rim.device.api.system.Display;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.CheckboxField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.KeywordFilterField;
 import net.rim.device.api.ui.component.RadioButtonField;
 import net.rim.device.api.ui.component.RadioButtonGroup;
 import net.rim.device.api.ui.component.RichTextField;
 import net.rim.device.api.ui.component.SeparatorField;
 import net.rim.device.api.ui.component.TextField;
 import net.rim.device.api.ui.container.HorizontalFieldManager;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.text.PhoneTextFilter;
 
 public class DialerField extends VerticalFieldManager implements TabFieldItem, LinphoneResource{
 	private VerticalFieldManager mOutcallFields = new VerticalFieldManager();
 	private TextField  mInputAddress;
 	private KeywordFilterField mkeyWordField;
 	
 	private VerticalFieldManager mIncallFields = new VerticalFieldManager(Field.USE_ALL_WIDTH);
 	RichTextField mDisplayNameField;
 	TextField mPhoneNumberField;
 	TextField mDurationField;
 	CheckboxField mMute;
 	CheckboxField mSpeaker;
 	static final private Timer mTimer = new Timer();
 	TimerTask mCallDurationTask;
 	private static Logger sLogger=JOrtpFactory.instance().createLogger("Linphone");
 	String mDisplayName;
 	LinphoneCore mCore;
 	final static int GREEN_BUTTON_KEY=1114112;
 	PhoneTextFilter mPhoneTextFilter = new PhoneTextFilter();
 	private static ResourceBundle mRes = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
 	public DialerField(LinphoneCore aCore) {
 		mCore=aCore;
 		//outcall fields
 		try {
 		    
 			mkeyWordField = new SearchableContactList(new SearchableContactList.Listener() {
 
 				public void onSelected(Contact selected) {
 					setAddressAndDisplay(selected);
 				}
 			}).getKeywordFilterField();
 		  } catch (PIMException e) {
 			  sLogger.error("Cannot open contact list",e);
 		  }
 		  mkeyWordField.setChangeListener(new FieldChangeListener() {
 
 			public void fieldChanged(Field field, int context) {
 				if (mkeyWordField.getKeyword().length() == 0) {
 					mkeyWordField.setKeyword(null);
 					 mInputAddress.setLabel("sip:");
 				}
 			}
 			  
 		  });
 		  mkeyWordField.setKeywordField(new TextField(Field.NON_FOCUSABLE));
 		  mkeyWordField.getKeywordField().setLabel(mRes.getString(FIND));
 		  mkeyWordField.getKeywordField().setEditable(false);
 		  mInputAddress = new TextField(Field.FOCUSABLE) {
 	    	boolean mInDigitMode=true;
 	    	protected boolean insert(char charater, int arg1) {
 				char lNumber = mPhoneTextFilter.convert(charater, 0);
 				StringBuffer lnewKey = new StringBuffer(mkeyWordField.getKeywordField().getText());
 				mkeyWordField.setKeyword(lnewKey.insert(getCursorPosition(), charater).toString());
 				
 				if (mInDigitMode ==true && 0<=Character.digit(lNumber,10) && Character.digit(lNumber,10)<10) {
 					 return super.insert(lNumber, arg1);
 				} else {
 					if (mInDigitMode==true) {
 						setText(mkeyWordField.getKeyword());
 						mInDigitMode=false;
 						return true;
 					}
 					return super.insert(charater, arg1);
 				}
 				
 			}
 			protected synchronized boolean backspace() {
 				if(getCursorPosition()<=mkeyWordField.getKeyword().length()){
 					StringBuffer lnewKey = new StringBuffer(mkeyWordField.getKeyword());
 					mkeyWordField.setKeyword(lnewKey.delete(getCursorPosition()-1,getCursorPosition()).toString());
 				}
 				return super.backspace();
 			}
 			protected boolean keyChar(char key, int status, int time) {
 				mDisplayName=null; //Erase display name if any key is manually entered
 				if (getTextLength()!=0) {
 					mInputAddress.setLabel("");
 				} else {
 					mInDigitMode=true;
 				}
 				if (key == Characters.BACKSPACE && getCursorPosition()==0 && getTextLength() !=0) {
 					StringBuffer lnewKey = new StringBuffer(mkeyWordField.getKeywordField().getText());
 					mkeyWordField.setKeyword(lnewKey.delete(0,1).toString());
 				}
 				if (getTextLength() == 0 || (key == Characters.BACKSPACE && getTextLength()==1)) {
 					setLabel("sip:");
 				}
 				return super.keyChar(key, status, time);
 			}
 			
 
 	    };
 	    mInputAddress.setLabel("sip:");
 	    mInputAddress.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,50));
 	    mOutcallFields.add(mInputAddress);
 	    mOutcallFields.add(new SeparatorField());
 	    mOutcallFields.add(mkeyWordField.getKeywordField());
 	    mOutcallFields.add(new SeparatorField());
 	    mOutcallFields.add(mkeyWordField);
 	    
 	    //incall fields
 	    mDisplayNameField = new RichTextField(Field.NON_FOCUSABLE|RichTextField.TEXT_ALIGN_HCENTER);
 	    mDisplayNameField.setFont(Font.getDefault().derive(Font.ANTIALIAS_STANDARD,50));
 	    mIncallFields.add(mDisplayNameField);
 	    mIncallFields.add(new SeparatorField());
 	    
 	    mPhoneNumberField = new TextField(Field.NON_FOCUSABLE);
 	    mDurationField = new TextField(Field.NON_FOCUSABLE);
 	    
 	    HorizontalFieldManager lNumAndDuration = new HorizontalFieldManager(Field.USE_ALL_WIDTH) {
 	    	{
 	    	    add(mPhoneNumberField);
 	    	    add(mDurationField);	
 	    	}
 			protected void sublayout(int maxWidth, int maxHeight) {
 				layoutChild(mPhoneNumberField, 2*maxWidth/3, maxHeight);   
 				layoutChild(mDurationField, maxWidth/3, maxHeight);   
 				setPositionChild(mPhoneNumberField, 10, 0);    
 				setPositionChild(mDurationField, maxWidth - maxWidth/3, 0);    
 				setExtent(maxWidth, mPhoneNumberField.getHeight());  
 			}
 	    	
 	    };
 	    
 
 
 	    mIncallFields.add(lNumAndDuration);
 	    mMute =  new CheckboxField(mRes.getString(MUTE),false);
 	    mMute.setChangeListener(new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				mCore.muteMic(((CheckboxField)field).getChecked());
 			}
 		});
 
 	    mSpeaker =  new CheckboxField(mRes.getString(SPEAKER),false);
 	    mSpeaker.setChangeListener(new FieldChangeListener() {
 			public void fieldChanged(Field field, int context) {
 				mCore.enableSpeaker(((CheckboxField)field).getChecked());
 			}
 		});
 	   
 	    
 	    HorizontalFieldManager lMuteAndSpeaker = new HorizontalFieldManager(Field.USE_ALL_WIDTH) {
 	    	{
 	    	    add(mMute);
 	    	    add(mSpeaker);	
 	    	}
 			protected void sublayout(int maxWidth, int maxHeight) {
 				layoutChild(mMute, 2*maxWidth/3, maxHeight);   
 				layoutChild(mSpeaker, maxWidth/3, maxHeight);   
 				int lYPosition = Display.getHeight() - 2*TabField.SIZE -mDisplayNameField.getContentHeight() - mPhoneNumberField.getHeight()-60;
 				setPositionChild(mMute, 10, lYPosition);    
 				setPositionChild(mSpeaker, maxWidth - mSpeaker.getWidth()-10, lYPosition);    
 				setExtent(maxWidth, lYPosition+mSpeaker.getHeight());  
 			}
 	    	
 	    };
 	
 
 	    mIncallFields.add(lMuteAndSpeaker);
 	    
 	    enableOutOfCallFields();
 	}
 	
 	
 	protected boolean keyDown(int keycode, int time) {
 		if (keycode==GREEN_BUTTON_KEY && (mkeyWordField.getSelectedElement() != null ||mInputAddress.getText().length()>0)) {
 			try {
 				if (mCore.isInComingInvitePending()){
 					throw new LinphoneCoreException("Already in call");
 				}else{
 					if (mkeyWordField.getSelectedElement() != null && ((VerticalFieldManager)getFieldWithFocus()).getFieldWithFocus()==mkeyWordField) {
 						DialerField.this.setAddressAndDisplay((Contact) mkeyWordField.getSelectedElement());
 					}
 					if (getAddress().length() >0 ) {
 						LinphoneAddress lTo = mCore.interpretUrl(getAddress());
 						lTo.setDisplayName(getDisplayName());
 						mCore.invite(lTo);
 					}
 					return true;
 				}
 			} catch (final LinphoneCoreException e) {
 				sLogger.error("call error",e);
 				UiApplication.getUiApplication().invokeLater(new Runnable() {
 					public void run() {
 						Dialog.alert(e.getMessage());
 
 					}
 				});
 			}
 		}
 		return super.keyDown(keycode, time);
 	}
 
 	public boolean keyChar(char ch, int status, int time) {
 		char lNumber = mPhoneTextFilter.convert(ch, 0);
		if (mCore.isIncall() && 0<=Character.digit(lNumber,10) && Character.digit(lNumber,10)<10) {
 			 mCore.sendDtmf(lNumber);
 			return true;
 		} else {
 			return super.keyChar(ch, status, time);
 		}
 	}
 
 
 	private void setAddressAndDisplay (Contact aContact) {
 		setAddress( aContact.getString(Contact.TEL, 0));
 		String[] lContactNames = aContact.getStringArray(Contact.NAME, 0);
 		StringBuffer lDisplayName = new StringBuffer();
 
 		if (lContactNames[Contact.NAME_GIVEN] != null ) {
 			lDisplayName.append(lContactNames[Contact.NAME_GIVEN]);
 		}
 		if (lContactNames[Contact.NAME_FAMILY] != null ) {
 			if (lDisplayName.length()!= 0) lDisplayName.append(' ');
 			lDisplayName.append(lContactNames[Contact.NAME_FAMILY]);
 		}
 		if (lDisplayName.length()!=0) {
 			setDisplayName(lDisplayName.toString());
 		} else {
 			setDisplayName(null);
 		}
 	}
 	public void setAddress(String aValue) {
 		if (aValue.length()>0) mInputAddress.setLabel(null);
 		mInputAddress.setText(aValue);
 	}
 	public String getAddress() {
 		return mInputAddress.getText();
 	}
 	public String getDisplayName() {
 		return mDisplayName;
 	}
 	public void setDisplayName(String aDisplayName) {
 		mDisplayName=aDisplayName;
 	}
 	public void enableIncallFields() {
 		if (mOutcallFields.getManager() == this ) delete (mOutcallFields);
 		add(mIncallFields);
 		String lDisplay=null;
 		String lNumber="";
 		if (mCore.getCurrentCall().getDirection() == CallDirection.Incoming) {
 			LinphoneAddress lIncallAddress = mCore.getCurrentCall().getRemoteAddress();
 			lDisplay = lIncallAddress.getDisplayName();
 			lNumber = lIncallAddress.getUserName();
 		} else {
 			lDisplay = mDisplayName;
 			lNumber = getAddress();
 		
 		}
 		if (lDisplay !=null) {
 			mDisplayNameField.setText(lDisplay);
 			mPhoneNumberField.setText(lNumber);
 		} else {
 			mDisplayNameField.setText(lNumber);
 			mPhoneNumberField.setText("");
 		}
 		mSpeaker.setChecked(mCore.isSpeakerEnabled());
 		mMute.setChecked(mCore.isMicMuted());
 		mCallDurationTask = new TimerTask() {
 			int mDuration=0;
 			{
 				mDurationField.setText("0s");
 			}
 			public void run() {
 				mDuration++;
 				if (mDuration <=60) {
 					mDurationField.setText(mDuration+"s");
 				} else {
 					mDurationField.setText(mDuration/60+":"+(mDuration - mDuration/60)+"s" );
 				}
 			}
 		};
 		mTimer.scheduleAtFixedRate(mCallDurationTask, 0, 1000);
 	}
 	public void enableOutOfCallFields() {
 		if (mIncallFields.getManager() == this ) {
 			mCallDurationTask.cancel();
 			delete (mIncallFields);
 		}
 		add(mOutcallFields);
 	}
 
 
 	public void onSelected() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public void onUnSelected() {
 		// TODO Auto-generated method stub
 		
 	}
 }
