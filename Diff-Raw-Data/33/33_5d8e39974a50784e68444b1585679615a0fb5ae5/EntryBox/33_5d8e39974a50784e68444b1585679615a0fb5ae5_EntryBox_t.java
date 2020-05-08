 package com.scrollwin.client;
 
import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.Timer;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.widgets.Img;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.TextAreaItem;
 import com.smartgwt.client.widgets.form.fields.TextItem;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.HStack;
 import com.smartgwt.client.widgets.layout.VStack;
 import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
 import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
 import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
 import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
 
 public class EntryBox extends HLayout {
 
 	private TextAreaItem messageItem = new TextAreaItem();
 	private Label infoItem = new Label();
 	private HStack imageStack = new HStack();
 	private VStack editStack = new VStack();
 	private DynamicForm form = new DynamicForm(); 
     private ioCallbackInterface myCallbackInterface;
     private userCallbackInterface myUserCallbackInterface;
     private Timer focusTimer;
     private boolean myIsShiftDown = false;
     private boolean myIsEditing = false;
     private int myEditingMessageSeq = 0;
     
     
     public EntryBox(ioCallbackInterface callbackInterface, userCallbackInterface userCB) {
     	myCallbackInterface = callbackInterface;
     	myUserCallbackInterface = userCB;
     	
     	setShowEdges(true);
     	setMargin(5);
     	setWidth(800);
     	setHeight(75);
     	setBackgroundColor("#ffffff");
     	setEdgeImage("borders/sharpframe_10.png");
         setEdgeSize(6);
         
     	imageStack.setWidth(40);
     	imageStack.setAlign(Alignment.CENTER);
     	imageStack.setPadding(8);
     	imageStack.setBackgroundColor("#E0E0E0");
     	
     	infoItem.setContents("Editing message 222");
     	infoItem.setAutoHeight();
     	infoItem.setPadding(2);
     	infoItem.hide();
     	messageItem.setShowTitle(false);  
         messageItem.setLength(1000);  
         messageItem.setWidth("*");
         messageItem.setHeight("*");
         
         form.setNumCols(1);
         form.setCanDragResize(true);
         form.setAutoFocus(true);
         form.setWidth100();
         form.setHeight100();
         form.setFields(messageItem);
         form.focusInItem(messageItem);
         form.setStyleName("blueYellow");
         
         addClickHandler(new ClickHandler(){
 			@Override
 			public void onClick(ClickEvent event) {
 				myUserCallbackInterface.userEntry();
 			}
         });
         
         messageItem.addKeyUpHandler(new KeyUpHandler() {
         	@Override
         	public void onKeyUp(KeyUpEvent event) {
         		if(event.getKeyName().compareTo("Shift") == 0)
 					myIsShiftDown= false;
         		
 	        	if (event.getKeyName().compareTo("Enter") == 0){
 	        		if(!myIsShiftDown){
 						event.cancel();
 						myCallbackInterface.messageToSendCallback(filterMessage(messageItem.getValueAsString()), myIsEditing, myEditingMessageSeq);
 						myIsEditing = false;
 						infoItem.hide();
 						myEditingMessageSeq = 0;
 						messageItem.clearValue();
 	        		}
 				}
 	        	
 	        	if (event.getKeyName().compareTo("Escape") == 0){
 	        		if(myIsEditing){
 	        			myIsEditing = false;
 						infoItem.hide();
 						myEditingMessageSeq = 0;
 						messageItem.clearValue();
 	        		}
 	        	}
         	}
         });
         
         messageItem.addKeyPressHandler(new KeyPressHandler() {
 			@Override
 			public void onKeyPress(KeyPressEvent event) {
 				if(event.getKeyName().compareTo("Shift") == 0)
 					myIsShiftDown = true;
 				myUserCallbackInterface.userEntry();
 			}
           });
         
         focusTimer = new Timer() {
 			@Override
 			public void run() {
 				form.focusInItem(messageItem);
 				messageItem.setSelectionRange(messageItem.getLength(), messageItem.getLength());
 			}
         };
         
         editStack.addMember(infoItem);
         editStack.addMember(form);
         
 		addMember(imageStack);
 		addMember(editStack);
     }
     
 	public void setUser(UserContainer user)
     {
     	Img userImage = new Img(user.getAvatarURL(), 36, 36);
     	userImage.setBorder("2px groove #808080");
     	imageStack.addMember(userImage, 0);
     }
     
     public void addAddressee(String userNick)
     {
     	String Message;
     	
     	// Add a space before only if we're not beginning the message
     	if(messageItem.getValueAsString() != null)
     		Message = messageItem.getValueAsString() + " @" + userNick + " ";
     	else 
     		Message = "@" + userNick + " ";
     	
     	messageItem.setValue(Message);
 		setFocus();
     }
 
     public void setFocus()
     {
     	focusTimer.schedule(100);
     }
     
     public String filterMessage(String Message)
     {
     	String outputMessage = "";
     	int token = 0;
     	
     	// remove trailing crlf "submit", it's not part of the message
     	if(Message.endsWith("\n"))
     		Message = Message.substring(0, Message.length() - 1);
     	
     	// Split the message in tokens (separator is space) an try to locate URLs
     	// Convert CrLf to HTML linefeeds before the split because \n\r are considered 
     	// as whitespace by the regex.
     	String [] parts = convertCrLf(Message).split("\\s+");
     	
     	// Check if the message is targeted at someone (
     	for(int tok = 0; tok < parts.length; tok++) {
     		if(parts[token].startsWith("@")){
     			if(token > 0)
     				outputMessage +=", ";
 
     			outputMessage += "<b>" + parts[token] + "</b>";
     			token++;
     		} else {
     			if(token > 0)
     				outputMessage += "<b> > </b>";
     			break;
     		}
     	}
     	
     	// Look for URLs and encapsulate them to img or href
     	for(int tok = token; tok < parts.length; tok++)
     	{
     		String item = parts[tok];
     		if ((item.startsWith("http://")) || (item.startsWith("https://")) ){
     			if( (item.endsWith(".jpg")) || (item.endsWith(".gif")) || (item.endsWith(".png")) ||
     				(item.endsWith(".JPG")) || (item.endsWith(".GIF")) || (item.endsWith(".PNG")))
     				item = "<br><img src=\"" + item + "\" height=200/><br>";
     			else
     				item = "<a href=\"" + item + "\" target=\"_blank\">lien</a>";
     		}
     		outputMessage += item + " ";
     	}
    	
     	return escapeJson(outputMessage);
     }
     
     public void editMessage(MessageContainer message)
     {
    	String decodedMessage = URL.decode(message.getMessage());
    	messageItem.setValue(deconvertCrLf(decodedMessage));
     	myIsEditing = true;
     	myEditingMessageSeq = message.getMessageSeqId();
     	infoItem.setContents("Édition du message <b>" + myEditingMessageSeq + "</b>. ESC pour annuler");
     	infoItem.show();
     	setFocus();
     }
     
     public String escapeJson(String str) {
 	    str = str.replace("\\", "\\\\");
 	    str = str.replace("\"", "\\\"");
 	    str = str.replace("/", "\\/");
 	    str = str.replace("\b", "\\b");
 	    str = str.replace("\f", "\\f");
 	    str = str.replace("\t", "\\t");
 	    return str;
 	}
     
     public String convertCrLf(String str) {
     	// Add space before so the <br> is processed as a separate token
     	str = str.replace("\n", " <br /> "); 
 	    str = str.replace("\r", " <br /> ");
 	    return str;
     }
     
     public String deconvertCrLf(String str) {
     	// Put back the \n where <br> where inserted
     	str = str.replace(" <br /> ", "\n"); 
     	str = str.replace("<br />", "\n");
 	    return str;
     }
 }
