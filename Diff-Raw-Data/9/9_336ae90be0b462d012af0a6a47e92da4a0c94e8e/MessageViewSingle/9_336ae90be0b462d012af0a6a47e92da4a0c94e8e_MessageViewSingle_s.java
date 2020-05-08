 package com.allplayers.android;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.method.LinkMovementMethod;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MessageViewSingle extends Activity
 {
 	/** called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.viewsinglemessage);
 		
 		String jsonResult = Globals.currentMessage;
 		int messageLoc = Globals.currentMessageLoc;
 		
 		MessagesMap messages = new MessagesMap(jsonResult);
 		ArrayList<MessageData> messageList = messages.getMessageData();
 		
 		String subject = messageList.get(messageLoc).getSubject();
 		String sender = messageList.get(messageLoc).getLastSender();
 		String date = messageList.get(messageLoc).getDate();
 		String body = messageList.get(messageLoc).getMessageBody();
 		int threadID = Integer.parseInt(messageList.get(messageLoc).getThreadID());
 		int isNew = Integer.parseInt(messageList.get(messageLoc).getNew());
 		
 		if(isNew == 1)
 		{
 			APCI_RestServices.putMessage(threadID, 0, "");
 		}
 		
 		final TextView subjectText = (TextView)findViewById(R.id.subjectText);
 		subjectText.setText("This is the Subject.");
 		subjectText.setText(subject);
 		
 		final TextView senderText = (TextView)findViewById(R.id.senderText);
 		senderText.setText("This is the Sender's Name.");
 		senderText.setText("From: " + sender);
 		
 		final TextView dateText = (TextView)findViewById(R.id.dateText);
 		dateText.setText("This is the Date last sent.");
		dateText.setText("Last Message: " + new Date(Integer.parseInt(date)));
 		
 		final TextView bodyText = (TextView)findViewById(R.id.bodyText);
 		bodyText.setText("This is the body text.");
 		bodyText.setText(body);
 		bodyText.setMovementMethod(LinkMovementMethod.getInstance());
 		
 		final Button replyButton = (Button)findViewById(R.id.replyButton);
 		
         replyButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
             	startActivity(new Intent(MessageViewSingle.this, MessageReply.class));
             }
         });
 	}
 }
