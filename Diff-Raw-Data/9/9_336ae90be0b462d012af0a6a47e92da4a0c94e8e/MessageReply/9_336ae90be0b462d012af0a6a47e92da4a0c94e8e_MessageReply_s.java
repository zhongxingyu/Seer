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
 import android.widget.Toast;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MessageReply extends Activity
 {
 	
 	private String threadID;
 	private String body;
 	private String sendBody;
 	
 	/** called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.replymessage);
 		
 		String jsonResult = Globals.currentMessage;
 		int messageLoc = Globals.currentMessageLoc;
 		
 		MessagesMap messages = new MessagesMap(jsonResult);
 		ArrayList<MessageData> messageList = messages.getMessageData();
 		
 		String subject = messageList.get(messageLoc).getSubject();
 		String sender = messageList.get(messageLoc).getLastSender();
 		String date = messageList.get(messageLoc).getDate();
 		body = messageList.get(messageLoc).getMessageBody();
 		threadID = messageList.get(messageLoc).getThreadID();
 		
 		
 		final TextView subjectText = (TextView)findViewById(R.id.subjectText);
 		subjectText.setText("This is the Subject.");
 		subjectText.setText(subject);
 		
 		final TextView senderText = (TextView)findViewById(R.id.senderText);
 		senderText.setText("This is the Sender's Name.");
 		senderText.setText("From: " + sender);
 		
 		final TextView dateText = (TextView)findViewById(R.id.dateText);
 		dateText.setText("This is the Date last sent.");
		dateText.setText("Last Message: " + new Date(Integer.parseInt(date) * 1000));
 		
 		
 		final EditText bodyField = (EditText)findViewById(R.id.bodyField);
 		bodyField.setText("This is the Body text.");
 		bodyField.setText("\n"+
 							"\n" +
 							"------------" +
 							"\n" +
 							body);
 
 		final Button sendButton = (Button)findViewById(R.id.sendButton);
 		
         sendButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
             	sendBody = bodyField.getText().toString();
             	
             	APCI_RestServices.postMessage(Integer.parseInt(threadID), sendBody);
             	
             	Toast toast = Toast.makeText(getBaseContext(), "Message Sent!", Toast.LENGTH_LONG);
             	toast.show();
             	
             	finish();
             }
         });
 	}
 }
