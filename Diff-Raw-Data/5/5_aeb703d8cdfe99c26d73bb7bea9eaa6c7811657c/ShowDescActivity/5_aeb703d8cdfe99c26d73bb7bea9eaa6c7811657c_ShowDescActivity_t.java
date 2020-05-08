 package com.hackathon.tvnight.ui;
 
 import java.util.ArrayList;
 
 import org.apache.commons.lang3.CharSequenceUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.hackathon.tvnight.R;
 import com.hackathon.tvnight.model.ShowingResult;
 import com.hackathon.tvnight.model.TVShow;
 import com.hackathon.tvnight.task.GetShowDetailTask;
 import com.hackathon.tvnight.task.Invitation;
 import com.hackathon.tvnight.task.SendInvitationTask;
 import com.hackathon.tvnight.util.Util;
 import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
 import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
 
 public class ShowDescActivity extends Activity implements OnClickListener {
 
 	private GetShowDetailTask getShowDetails = null;
 	private Button createWatchGroup;
 	private TextView showTitle;
 	private TextView showDesc;
 	private TextView mySeasonAndEp;
 	private long startTime;
 	private Button purchaseButt;
 	private String channelId;
 	private ImageView showIcon;
 	private final String roviId = "6718065";
 	private String showName;
 	private Button saveButt;
 	private ArrayList<String> selectedNums;
 	
 	private Handler getDetailsHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			TVShow show = (TVShow) msg.obj;
 			startTime = System.currentTimeMillis()+(3*60*1000);
 			String stringTime = ShowingResult.convertTime(startTime);
 			channelId = "703";
 			if (show != null && show.getShowingResultList() != null && show.getShowingResultList().size() > 0) {
 				channelId = ((TVShow) msg.obj).getShowingResultList().get(0).getChannelId(); //show first showing result
 				if (channelId == null) {
 					channelId = "703";
 				}
 			}
 			mySeasonAndEp.setText(stringTime+" on Ch. "+channelId);
 			findViewById(R.id.progress_spinner).setVisibility(View.GONE);
 			findViewById(R.id.top_layout).setVisibility(View.VISIBLE);
 		};
 	};
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setContentView(R.layout.show_desc_layout);
 		createWatchGroup = (Button) findViewById(R.id.create_watch_group_butt);
 		showIcon = (ImageView) findViewById(R.id.show_icon);
 		showTitle = (TextView) findViewById(R.id.show_title);
 		showDesc = (TextView) findViewById(R.id.show_desc);
 		mySeasonAndEp = (TextView) findViewById(R.id.season_ep_text);
 		showName = getIntent().getStringExtra("name");
 		showTitle.setText(showName);
 		showDesc.setText(getIntent().getStringExtra("desc"));
 		purchaseButt = (Button) findViewById(R.id.purchase_butt);
 		saveButt = (Button) findViewById(R.id.save_butt);
 		getShowDetails = new GetShowDetailTask(getDetailsHandler, 1);
 		getShowDetails.execute(getIntent().getStringExtra("show_id"));
		purchaseButt.setOnClickListener(this);
		if (getIntent().getBooleanExtra("simpaid", false) && selectedNums != null & selectedNums.size() > 0) {
 			purchaseButt.setVisibility(View.VISIBLE);
 		}
 		saveButt.setOnClickListener(this);
 		createWatchGroup.setOnClickListener(this);
 		String imageUrl = getIntent().getStringExtra("imgurl");
 		if (imageUrl != null) {
 			findViewById(R.id.loading).setVisibility(View.VISIBLE);
 			showIcon.setVisibility(View.GONE);
 			UrlImageViewHelper.setUrlDrawable(showIcon, getIntent().getStringExtra("imgurl"), new UrlImageViewCallback() {
 				@Override
 				public void onLoaded(ImageView arg0, Bitmap arg1, String arg2, boolean arg3) {
 					findViewById(R.id.loading).setVisibility(View.GONE);
 					showIcon.setVisibility(View.VISIBLE);
 				}
 			});
 		}
 		else {
 			findViewById(R.id.loading).setVisibility(View.GONE);
 			showIcon.setVisibility(View.VISIBLE);
 		}
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	protected void onResume() {
 		startTime = System.currentTimeMillis()+(3*60*1000);
 		String stringTime = ShowingResult.convertTime(startTime);
 		mySeasonAndEp.setText(stringTime+" on Ch. "+channelId);
 		super.onResume();
 	}
 	
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
 			selectedNums = data.getStringArrayListExtra("selected_nums");
 			createWatchGroup.setText("Edit Watch Group ("+selectedNums.size()+")");
 			findViewById(R.id.save_layout).setVisibility(View.VISIBLE);
 		}
 		else {
 			selectedNums = null;
 			createWatchGroup.setText("Create a Watch Group");
 			findViewById(R.id.save_layout).setVisibility(View.GONE);
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.create_watch_group_butt:
 			Intent startContactSelect = new Intent(this, ContactListSelectorActivity.class);
 			if (selectedNums != null) {
 				startContactSelect.putStringArrayListExtra("selected_nums", selectedNums);
 			}
 			startActivityForResult(startContactSelect, 0);
 			break;
 		case R.id.save_butt:
 			saveButt.setEnabled(false);
 			final ProgressDialog pd = new ProgressDialog(this);
 			pd.setMessage("Sending...");
 			pd.setCancelable(false);
 			pd.show();
 			startTime = System.currentTimeMillis()+(3*60*1000);
 			SendInvitationTask sendTask = new SendInvitationTask(new Handler() {
 				@Override
 				public void handleMessage(Message msg) {
 					pd.cancel();
 					if (msg.obj != null) {
 						Toast.makeText(ShowDescActivity.this, "Invitation Sent!", Toast.LENGTH_SHORT).show();
 					}
 					else {
 						Toast.makeText(ShowDescActivity.this, "Request Failed!", Toast.LENGTH_SHORT).show();						
 					}
 					finish();
 					super.handleMessage(msg);
 				}
 			}, 1);
 			Invitation i = new Invitation(Util.getPhoneNumber(this), channelId, roviId, "", startTime);
 			for (String recip : selectedNums) {
 				i.addRecipient(recip);
 			}
 			sendTask.execute(i);
 			break;
 		case R.id.purchase_butt:
 			Builder builder = new Builder(this);
 			if (selectedNums != null && selectedNums.size() > 0) {
 				builder.setCancelable(true);
 				builder.setTitle("Purchase for yourself or for group");
 				builder.setItems(new CharSequence[] {"Myself", "Group ("+selectedNums.size()+")"}, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						switch (which) {
 						case 0:
 							dialog.cancel();
 							showBuyDialog(false);
 							break;
 						case 1:
 							showBuyDialog(true);
 							break;
 						}
 					}
 				});
 				builder.show();
 			}
 			else {
 				showBuyDialog(false);
 			}
 			break;
 		}
 	}
 	
 	private void showBuyDialog(boolean group) {
 		Builder builder = new Builder(this);
 		builder.setCancelable(false);
 		builder.setTitle(group ? "Purchase for Group ("+selectedNums.size()+")": "Purchase for Yourself");
 		
 		builder.setMessage((group) ? 
 					"Would you like to buy \""+showName+"\" for a group of "+selectedNums.size()+" people? For a total of: $"+selectedNums.size()*2 :
 					"Would you like to buy \""+showName+"\" for $3 for yourself? (If you buy for a group you get a $1 discount)");
 		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Toast.makeText(ShowDescActivity.this, "Thank you for purchasing!", Toast.LENGTH_SHORT).show();
 				dialog.cancel();
 			}
 		});
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 		builder.show();
 	}
 	
 }
