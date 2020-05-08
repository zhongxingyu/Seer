 package edu.ucsb.cs290.touch.to.text;
 
 import java.security.SignedObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import edu.ucsb.cs290.touch.to.text.crypto.Contact;
 import edu.ucsb.cs290.touch.to.text.crypto.SealablePublicKey;
 
 public class ConversationListActivity extends KeyActivity implements
 		ConversationListFragment.Callbacks {
 	private static final long NEW_CONTACT_ID = 0;
 	private static final String FRAG_TAG = null;
 	private boolean mTwoPane;
 	private boolean fragmentInit = false;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_conversation_list);
 
 		if (findViewById(R.id.conversation_detail_container) != null) {
 			mTwoPane = true;
 			getListFragment().setActivateOnItemClick(true);
 		}
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.activity_conversation_view, menu);
 		return true;
 	}
 
 	public boolean addContact(MenuItem item) {
 		startActivityForResult(new Intent(getApplicationContext(),
 				NewContactActivity.class), 101);
 		return true;
 	}
 
 	public boolean wipeDB(MenuItem item) {
 		return getInstance().wipeDB();
 	}
 
 	@Override
 	public void onItemSelected(Contact id) {
 		if (mTwoPane) {
 			Bundle arguments = new Bundle();
 			arguments.putSerializable(ConversationDetailFragment.ARG_ITEM_ID,
 					id);
 			final ConversationDetailFragment fragment = new ConversationDetailFragment();
 			fragment.setArguments(arguments);
 			getFragmentManager().beginTransaction()
 					.replace(R.id.conversation_detail_container, fragment, FRAG_TAG)
 					.commit();
 			new Handler().post(new Runnable() {
 
 				@Override
 				public void run() {
 					fragment.onServiceConnected();
 					fragmentInit = true;
 				}
 			});
 
 		} else {
 			Intent detailIntent = new Intent(this,
 					ConversationDetailActivity.class);
 			detailIntent.putExtra(ConversationDetailFragment.ARG_ITEM_ID, id);
 			startActivity(detailIntent);
 		}
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case 101:
 			if (resultCode == Activity.RESULT_CANCELED) {
 				return;
 			}
 			System.out.println("Contact added");
 			// String long SealablePublicKey
 			String name = data.getExtras().getString("name");
 			SealablePublicKey key = (SealablePublicKey) data
 					.getExtras().get("key");
 			// SignedObject token = (SignedObject) data.getExtras().get("token");
 			SignedObject token = key.token();
 			Contact newContact = new Contact(name, key, token, NEW_CONTACT_ID);
 			getInstance().addContact(newContact);
 			return;
 		}
 	}
 
 	@Override
 	protected void onServiceConnected() {
 		getListFragment().onServiceConnected();
 		if (mTwoPane && fragmentInit) {
 			ConversationDetailFragment f = (ConversationDetailFragment) getFragmentManager()
 					.findFragmentByTag(FRAG_TAG);
			f.onServiceConnected();
 		}
 	}
 
 	@Override
 	protected void refresh() {
 		getListFragment().refresh();
 		if (mTwoPane && fragmentInit) {
 			ConversationDetailFragment f = ((ConversationDetailFragment) getFragmentManager()
 					.findFragmentByTag(FRAG_TAG));
 			f.refresh();
 		}
 	}
 
 	private ConversationListFragment getListFragment() {
 		return (ConversationListFragment) getFragmentManager()
 				.findFragmentById(R.id.conversation_list);
 	}
 }
