 package net.fobel.android.remoteduino;
 
 import android.widget.Toast;
 import android.content.Context;
 import android.widget.Button;
 import android.view.View;
 
 public class RemoteButton extends Button {
 	RemoteCommand cmd;
 	Context context;
 
 	public RemoteButton(Context context, RemoteCommand cmd) {
 		super(context);
 		this.cmd = cmd;
 		this.context = context;
 		this.setText(cmd.label);
 		final RemoteButton here = this;
 		this.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 here.send();
             }
         });
 	}
 	
 	public void send() {
         String result = cmd.send();
         /*
 		Toast.makeText(context, String.format("CODE: c=%s, p=%s [%s]\nResult: %s",
 				cmd.code, cmd.protocol, cmd.label, result), Toast.LENGTH_SHORT).show();
		*/
 	}
 }
