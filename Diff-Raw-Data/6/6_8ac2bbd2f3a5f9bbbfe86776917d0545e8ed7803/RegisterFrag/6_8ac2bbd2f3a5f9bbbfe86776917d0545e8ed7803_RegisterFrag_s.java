 package com.chess.genesis;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Toast;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class RegisterFrag extends BaseContentFrag implements OnClickListener
 {
 	public final static String TAG = "REGISTER";
 
 	private NetworkClient net;
 	private ProgressMsg progress;
 
 	private final Handler handle = new Handler()
 	{
 		public void handleMessage(final Message msg)
 		{
 			switch (msg.what) {
 			case NetworkClient.REGISTER:
 				final JSONObject json = (JSONObject) msg.obj;
 
 				try {
 					if (json.getString("result").equals("error")) {
 						progress.remove();
 						Toast.makeText(act, "ERROR:\n" + json.getString("reason"), Toast.LENGTH_LONG).show();
 						return;
 					}
 					progress.setText("Registration Successfull");
 					(new RegisterActivation(act, handle)).show();
 				} catch (JSONException e) {
 					e.printStackTrace();
 					throw new RuntimeException();
 				}
 				break;
 			case RegisterConfirm.MSG:
 				progress.setText("Sending Registration");
 				final Bundle data = (Bundle) msg.obj;
 
 				net.register(data.getString("username"), data.getString("password"), data.getString("email"));
 				(new Thread(net)).start();
 				break;
 			case RegisterActivation.MSG:
 				if (isTablet)
 					getFragmentManager().popBackStack();
 				else
 					act.finish();
 				break;
 			}
 		}
 	};
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		initBaseContentFrag();
 
 		final View view = inflater.inflate(R.layout.fragment_register, container, false);
 
 		// create network client instance
 		net = new NetworkClient(act, handle);
 		progress = new ProgressMsg(act);
 
 		// setup click listeners
 		final View image = view.findViewById(R.id.register);
 		image.setOnClickListener(this);
 
 		return view;
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		NetActive.inc();
 	}
 
 	@Override
 	public void onPause()
 	{
 		NetActive.dec();
 		super.onPause();
 	}
 
 	public void onClick(final View v)
 	{
 		if (v.getId() == R.id.register)
 			register_validate();
 	}
 
 	private void register_validate()
 	{
 		EditText txt = (EditText) act.findViewById(R.id.username);
 		final String username = txt.getText().toString().trim();
 
 		txt = (EditText) act.findViewById(R.id.password);
 		final String password = txt.getText().toString();
 
 		txt = (EditText) act.findViewById(R.id.password2);
 		final String password2 = txt.getText().toString();
 
 		txt = (EditText) act.findViewById(R.id.email);
 		final String email = txt.getText().toString().trim();
 
 		if (!valid_username(username))
 			return;
 		if (!valid_password(password, password2))
 			return;
 		if (!valid_email(email))
 			return;
 
 		final Bundle bundle = new Bundle();
 		bundle.putString("username", username);
 		bundle.putString("password", password);
 		bundle.putString("email", email);
 
 		(new RegisterConfirm(act, handle, bundle)).show();
 	}
 
 	private boolean valid_username(final String name)
 	{
 		if (name.length() < 3) {
 			Toast.makeText(act, "Username too short", Toast.LENGTH_LONG).show();
 			return false;
 		} else if (!name.matches("[a-zA-Z0-9]+")) {
 			Toast.makeText(act, "Username can only contain letters or numbers", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		return true;
 	}
 
 	private boolean valid_password(final String pass1, final String pass2)
 	{
 		if (!pass1.equals(pass2)) {
 			Toast.makeText(act, "Passwords don't match", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		if (pass1.length() < 4) {
 			Toast.makeText(act, "Password too short", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		for (int i = 0; i < pass1.length(); i++) {
 			if (pass1.charAt(i) < 32 || pass1.charAt(i) > 126) {
 				Toast.makeText(act, "Password can only contain ASCII characters", Toast.LENGTH_LONG).show();
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean valid_email(final String email)
 	{
 		// regex:  \w+[\w\._+-]*\w+@\w+[\w\.-]*\w+\.\w+[\w\.-]*\w+
 		if (!email.matches("\\w+[\\w\\._+-]*\\w+@\\w+[\\w\\.-]*\\w+\\.\\w+[\\w\\.-]*\\w+")) {
 			Toast.makeText(act, "Invalid email address", Toast.LENGTH_LONG).show();
 			return false;
 		}
 		return true;
 	}
 }
