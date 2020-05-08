 package de.wak_sh.client.fragments;
 
 import java.io.IOException;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 import de.wak_sh.client.R;
 import de.wak_sh.client.backend.MessageArrayAdapter;
 import de.wak_sh.client.backend.ProgressDialogTask;
 import de.wak_sh.client.backend.model.Message;
 import de.wak_sh.client.backend.service.MessageService;
 
 public class NachrichtenFragment extends Fragment {
 	protected List<Message> messages;
 	protected ListView listView;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_nachrichten,
 				container, false);
		View header = inflater.inflate(R.layout.messages_list_item, container,
				false);
 
 		((TextView) header.findViewById(R.id.msg_date)).setText(R.string.datum);
 		((TextView) header.findViewById(R.id.msg_from)).setText(R.string.von);
 		((TextView) header.findViewById(R.id.msg_subject))
 				.setText(R.string.betreff);
 
 		listView = (ListView) rootView;
		// listView.addHeaderView(header);
 
 		return rootView;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		if (messages == null) {
 			new MessageTask(activity).execute();
 		}
 	}
 
 	private class MessageTask extends ProgressDialogTask<Void, Void> {
 		private Activity activity;
 
 		public MessageTask(Activity activity) {
 			super(activity, activity.getString(R.string.fetching_messages));
 			this.activity = activity;
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			MessageService service = new MessageService();
 			try {
 				service.fetchMessages();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}
 
 			messages = service.getMessages();
 
 			activity.runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					MessageArrayAdapter adapter = new MessageArrayAdapter(
 							activity, messages);
 					listView.setAdapter(adapter);
 				}
 			});
 
 			return null;
 
 		}
 
 	}
 }
