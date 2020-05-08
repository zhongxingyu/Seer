 package rpisdd.rpgme.activities;
 
 import java.util.ArrayList;
 
 import rpisdd.rpgme.R;
 import rpisdd.rpgme.gamelogic.player.Player;
 import rpisdd.rpgme.gamelogic.player.Reward;
 import rpisdd.rpgme.gamelogic.quests.DateHelper;
 import rpisdd.rpgme.gamelogic.quests.Quest;
 import rpisdd.rpgme.gamelogic.quests.QuestManager;
 import rpisdd.rpgme.popups.RewardPopup;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 //The quest menu is the "hub" for all quest-related activities.
 //Players can create, update, delete or view quests here.
 //CreateQuest and UpdateQuest (for later iteration) are their own menus,
 //but viewing, deleting and completing can all be done in this menu.
 public class QuestMenu extends ListFragment implements OnClickListener {
 
 	public QuestMenu() {
 	}
 
 	private Button createQuest;
 	private ImageButton deleteQuest;
 	private ImageButton completeQuest;
 	private ImageButton viewQuest;
 
 	private View selectedQuest;
 	private Quest currentQuest;
 
 	private QuestAdapter adapter;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 
 		selectedQuest = null;
 
 		View v = inflater.inflate(R.layout.quest_menu, container, false);
 
 		createQuest = (Button) v.findViewById(R.id.createQuestButton);
 		createQuest.setOnClickListener(this);
 		deleteQuest = (ImageButton) v.findViewById(R.id.deleteQuestButton);
 		deleteQuest.setOnClickListener(this);
 		completeQuest = (ImageButton) v.findViewById(R.id.completeQuestButton);
 		completeQuest.setOnClickListener(this);
 		viewQuest = (ImageButton) v.findViewById(R.id.viewQuestButton);
 		viewQuest.setOnClickListener(this);
 		updateButtons();
 
 		return v;
 	}
 
 	// Will enable or disable buttons, depending on the situation.
 	public void updateButtons() {
 		if (Player.getPlayer().getQuestManager().atMaxNumQuests()) {
 			createQuest.setEnabled(false);
 		} else {
 			createQuest.setEnabled(true);
 		}
 		if (selectedQuest != null) {
 			deleteQuest.setEnabled(true);
 			viewQuest.setEnabled(true);
 			deleteQuest.setVisibility(View.VISIBLE);
 			completeQuest.setVisibility(View.VISIBLE);
 			viewQuest.setVisibility(View.VISIBLE);
 			if (currentQuest != null && currentQuest.isFailed()) {
 				completeQuest.setEnabled(false);
 			} else {
 				completeQuest.setEnabled(true);
 			}
 		} else {
 			deleteQuest.setVisibility(View.INVISIBLE);
 			completeQuest.setVisibility(View.INVISIBLE);
 			viewQuest.setVisibility(View.INVISIBLE);
 			deleteQuest.setEnabled(false);
 			completeQuest.setEnabled(false);
 			viewQuest.setEnabled(false);
 		}
 
 		if (adapter != null) {
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	@Override
 	public void onResume() {
 
		fillListView(Player.getPlayer().getQuestManager(), getView());
 
 		super.onResume();
 	}
 
 	// Take a list of quests, and fill up the list view with those entries
 	public void fillListView(QuestManager quests, View v) {
 
 		quests.reRecurQuests();
 
 		adapter = new QuestAdapter(getActivity(), R.layout.quest_list_item,
 				quests.getIncompleteQuests());
 
 		setListAdapter(adapter);
 
 		updateButtons();
 	}
 
 	private class QuestAdapter extends ArrayAdapter<Quest> {
 
 		ArrayList<Quest> quests;
 
 		public QuestAdapter(Context context, int textViewResourceId,
 				ArrayList<Quest> quests) {
 			super(context, textViewResourceId, quests);
 			this.quests = quests;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				v = LayoutInflater.from(getActivity()).inflate(
 						R.layout.quest_list_item, null);
 			}
 			Quest q = quests.get(position);
 			if (q != null) {
 				TextView name = (TextView) v
 						.findViewById(R.id.questListItemName);
 				ImageView statImg = (ImageView) v
 						.findViewById(R.id.statTypeImage);
 				statImg.setImageResource(q.getStatType().getImgPath());
 				if (q.isFailed()) {
 					name.setText(q.getName() + " (FAILED)");
 					name.setTextColor(Color.RED);
 				} else {
 					name.setText(q.getName());
 					name.setTextColor(Color.BLACK);
 				}
 			}
 			return v;
 		}
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 
 		super.onListItemClick(l, v, position, id);
 
 		if (selectedQuest != null) {
 			selectedQuest.setBackgroundColor(Color.TRANSPARENT);
 		}
 
 		v.setBackgroundColor(Color.GRAY);
 
 		v.setSelected(true);
 		selectedQuest = v;
 		currentQuest = Player.getPlayer().getQuestManager()
 				.getIncompleteQuests().get(position);
 
 		updateButtons();
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		switch (v.getId()) {
 		case R.id.createQuestButton: {
 			Log.i("Debug:", "created");
 			// Make the parent activity change the fragment
 			((MainActivity) getActivity())
 					.changeFragment(new CreateQuestMenu());
 			break;
 		}
 		case R.id.deleteQuestButton: {
 			Log.i("Debug:", "destroy");
 
 			/*
 			 * confirmDelete = new PopupWindow(
 			 * LayoutInflater.from(getActivity()).inflate(R.layout.confirmation,
 			 * null, false), ViewGroup.LayoutParams.WRAP_CONTENT,
 			 * ViewGroup.LayoutParams.WRAP_CONTENT);
 			 * confirmDelete.showAtLocation(getView(), Gravity.CENTER, 0, 0);
 			 * confirmDelete
 			 * .getContentView().findViewById(R.id.yesButton).setOnClickListener
 			 * (this);
 			 * confirmDelete.getContentView().findViewById(R.id.noButton)
 			 * .setOnClickListener(this); confirmDelete.setFocusable(true);
 			 * //confirmDelete.setOutsideTouchable(true);
 			 * confirmDelete.update();
 			 */
 
 			AlertDialog.Builder builder1 = new AlertDialog.Builder(
 					getActivity());
 			builder1.setMessage("Are you sure you want to delete the selected quest?");
 			builder1.setCancelable(true);
 			builder1.setPositiveButton("Yes",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 							removeQuest();
 						}
 					});
 
 			builder1.setNegativeButton("No",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 
 			AlertDialog alert11 = builder1.create();
 			alert11.show();
 
 			break;
 		}
 		case R.id.completeQuestButton: {
 			Log.i("Debug:", "complete");
 			if (currentQuest.isFailed()) {
 
 				AlertDialog.Builder builder1 = new AlertDialog.Builder(
 						getActivity());
 				builder1.setMessage("You cannot complete that quest since the deadline has been reached.");
 				builder1.setCancelable(true);
 				builder1.setPositiveButton("OK",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 
 				AlertDialog alert11 = builder1.create();
 				alert11.show();
 
 			} else {
 				completeQuestPopup();
 			}
 			break;
 		}
 		case R.id.viewQuestButton: {
 			AlertDialog.Builder builder1 = new AlertDialog.Builder(
 					getActivity());
 			builder1.setMessage("View Quest");
 			builder1.setCancelable(true);
 			View viewQuest = LayoutInflater.from(getActivity()).inflate(
 					R.layout.view_quest, null);
 
 			((TextView) viewQuest.findViewById(R.id.viewQuestName))
 					.setText(currentQuest.getName());
 
 			if (currentQuest.isFailed()) {
 				((TextView) viewQuest.findViewById(R.id.viewQuestName))
 						.append(" (FAILED)");
 				((TextView) viewQuest.findViewById(R.id.viewDeadlineTitle))
 						.setTextColor(Color.RED);
 				((TextView) viewQuest.findViewById(R.id.viewQuestDeadline))
 						.setTextColor(Color.RED);
 			}
 
 			((TextView) viewQuest.findViewById(R.id.viewQuestDesc))
 					.setText(currentQuest.getDescription());
 			((TextView) viewQuest.findViewById(R.id.viewQuestDifficulty))
 					.setText(currentQuest.getDifficulty().toString());
 			((TextView) viewQuest.findViewById(R.id.viewQuestStatTag))
 					.setText(currentQuest.getStatType().toString());
 
 			if (currentQuest.isTimed()) {
 
 				viewQuest.findViewById(R.id.viewQuestDueDate).setVisibility(
 						View.VISIBLE);
 				((TextView) viewQuest.findViewById(R.id.viewQuestDeadline))
 						.setText(DateHelper.formatDate(currentQuest
 								.getDeadline()));
 
 			}
 
 			if (currentQuest.isRecurring()) {
 
 				viewQuest.findViewById(R.id.viewRecLayout).setVisibility(
 						View.VISIBLE);
 				((TextView) viewQuest.findViewById(R.id.viewQuestRecurrence))
 						.setText("Recurs "
 								+ currentQuest.getRecurrence().toString());
 
 			}
 
 			builder1.setView(viewQuest);
 			builder1.setPositiveButton("OK",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dialog, int id) {
 							dialog.cancel();
 						}
 					});
 
 			AlertDialog alert11 = builder1.create();
 			alert11.show();
 
 		}
 		default:
 			break;
 		}
 	}
 
 	public void completeQuestPopup() {
 
 		// Bring up a confirmation prompt first
 		AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
 		builder1.setMessage("Are you sure you want to complete the selected quest?");
 		builder1.setCancelable(true);
 		builder1.setPositiveButton("Yes",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int id) {
 
 						dialog.cancel();
 						completeQuest();
 
 					}
 				});
 
 		builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			}
 		});
 
 		AlertDialog alert11 = builder1.create();
 		alert11.show();
 
 	}
 
 	public void completeQuest() {
 
 		Reward reward = Player.getPlayer().getQuestManager()
 				.completeQuest(Player.getPlayer(), currentQuest);
 
 		RewardPopup.show("Quest Complete!", reward, getActivity(), null);
 
 		Player player = Player.getPlayer();
 		selectedQuest = null;
 		currentQuest = null;
 		fillListView(player.getQuestManager(), getView());
 	}
 
 	public void removeQuest() {
 		Player player = Player.getPlayer();
 		player.getQuestManager().removeQuest(currentQuest);
 		selectedQuest = null;
 		currentQuest = null;
 		fillListView(Player.getPlayer().getQuestManager(), getView());
 
 	}
 
 }
