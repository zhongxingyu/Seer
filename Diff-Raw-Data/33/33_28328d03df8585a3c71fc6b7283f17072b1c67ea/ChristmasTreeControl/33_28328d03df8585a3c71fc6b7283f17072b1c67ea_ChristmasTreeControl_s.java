 package ca.sergiomartins.christmastree1887;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Pattern;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class ChristmasTreeControl extends Activity {
 	
 	private TextView consoleView = null; 
 	private Socket nsocket = new Socket();
 	private SocketAddress sockaddr = new InetSocketAddress("192.168.69.8", 23);
 	private PrintWriter output = null;
 	private BufferedReader input = null;
 	private Thread networkThread = null;
 	private String serverMessage = "";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_christmas_tree_control);
 		
 		/* Button handlers */
 		final Button btnquit = (Button) findViewById(R.id.btnquit);
 		btnquit.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 finish();
             }
         });
 		final Button btnon = (Button) findViewById(R.id.btnon);
 		btnon.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	treeMessage("1");
             }
         });
 		final Button btnoff = (Button) findViewById(R.id.btnoff);
 		btnoff.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	treeMessage("0");
             }
         });
 		final Button btnclapon = (Button) findViewById(R.id.btnclapon);
 		btnclapon.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	treeMessage("{");
             }
         });
 		final Button btnclapoff = (Button) findViewById(R.id.btnclapoff);
 		btnclapoff.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	treeMessage("}");
             }
         });
 		final Button btnstatus = (Button) findViewById(R.id.btnstatus);
 		btnstatus.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	treeMessage("~");
             }
         });
 		final EditText edtsend = (EditText) findViewById(R.id.edtsend);
 		edtsend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_NULL ) { // this means enter key. one of 2 ways to do this
 					String text = v.getText().toString();
 					treeMessage(text);
 					v.setText("");
 				}
 				return true;
 			}
 		});
         
         /* Actual start-up code */
 		consoleView = (TextView) findViewById(R.id.console);
 		
 		networkThread = new Thread(new Runnable() {
 			public void run() {
 				treeConnect();
 				
 				while(true) {
 					if ((input != null) && (serverMessage == "")){
 						try {
 							serverMessage = input.readLine();
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						if ((serverMessage != null) && (serverMessage != "")) {
 							serverMessage.replaceAll("\n", "");
 							runOnUiThread(new Runnable(){
 								public void run() {
 									printServerMessage();
 								}
 							});
 						}
 					}
 				}
 			}
 		});
 		networkThread.start();
 	}
 
 	/* disable menu */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		return false;
 	}
 
 	
 	/* All the good stuff is down here */
 	private void printToConsole(String str) {
 		if (this.consoleView != null) {
 			this.consoleView.setText(this.consoleView.getText() + "\n" + str);
 			// scroll to bottom
 			try {
				int scrollAmount = this.consoleView.getLayout().getLineTop(this.consoleView.getLineCount())
			            -this.consoleView.getHeight();
				if(scrollAmount>0)
					this.consoleView.scrollTo(0, scrollAmount);
			}
			catch (NullPointerException e) {
 				// I just don't care
 			}
 		}
 	}
 	
 	private void printServerMessage(){
 		if (serverMessage != "") {
 			printToConsole(serverMessage);
 			serverMessage = "";
 		}
 	}
 	
 	private void treeConnect() {
 		printToConsole("Connecting to Christmas Tree...");
 		try {
 			this.nsocket.connect(this.sockaddr, 4000); //address object (with port), timeout (in ms)
 			OutputStream out = nsocket.getOutputStream();
 		    this.output = new PrintWriter(out);
 		    InputStream in = nsocket.getInputStream(); 
 		    this.input = new BufferedReader(new InputStreamReader(in));
 			if (this.nsocket.isConnected()) { 
 				printToConsole("Connected!");
 				this.treeMessage("An Android has connected!");
 			} else
 				printToConsole("Failed to connect :(");
 		} catch (final IOException e) {
 			runOnUiThread(new Runnable(){
 				public void run() {
 					printToConsole("ERROR: " + e.toString());
 				}
 			});
 			e.printStackTrace();
 		}
 	}
 	
 	private void treeMessage(String str) {
 		if (this.output != null) {
 		    this.output.println(str);
 		    this.output.flush();
 		} else
 			this.printToConsole("Not Connected");
 	}
 	
 }
 
 
 
 
 
 
 
 
