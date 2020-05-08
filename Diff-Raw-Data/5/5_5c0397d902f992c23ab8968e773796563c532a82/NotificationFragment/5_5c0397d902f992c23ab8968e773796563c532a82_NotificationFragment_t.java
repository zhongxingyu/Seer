 package com.ese2013.mub;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.ese2013.mub.map.MapButtonListener;
 import com.ese2013.mub.model.Day;
 import com.ese2013.mub.model.Mensa;
 import com.ese2013.mub.model.Menu;
 import com.ese2013.mub.model.Model;
 import com.ese2013.mub.service.Criteria;
 import com.ese2013.mub.service.CriteriaMatcher;
 import com.ese2013.mub.util.Observer;
 import com.ese2013.mub.util.SharedPrefsHandler;
 
 /**
  * Fragment which shows the result of the criteria defined in the settings.
  * 
  * @author Cdric
  * 
  */
 public class NotificationFragment extends Fragment implements Observer {
 	private NotificationAdapter notificationAdapter;
 	private ListView list;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Model.getInstance().addObserver(this);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_notification, container,
 				false);
 		notificationAdapter = new NotificationAdapter();
 		list = (ListView) view.findViewById(R.id.notification_list);
 		list.setAdapter(notificationAdapter);
 		View emptyView = view.findViewById(R.id.no_crit_text);
 		list.setEmptyView(emptyView);
 		notificationAdapter.fill();
 		return view;
 	}
 
 	public void onPause() {
 		super.onPause();
 	};
 
 	@Override
 	public void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 	}
 
 	public void onNotifyChanges(Object... message) {
 		notificationAdapter.notifyDataSetChanged();
 	}
 
 	public void sendListToMenusIntent(Mensa mensa) {
 		((DrawerMenuActivity) getActivity()).launchByMensaAtGivenPage(mensa
 				.getId());
 	}
 
 	/**
 	 * 
 	 * Adapter for filling the ListView of the NotificationFragment
 	 * 
 	 */
 	private class NotificationAdapter extends BaseAdapter implements IAdapter {
 		private LayoutInflater inflater;
 		private List<Criteria> adapterList;
 		private CriteriaMatcher criteriaMatcher = new CriteriaMatcher();
 
 		public NotificationAdapter() {
 			super();
 			adapterList = createList();
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = convertView;
 			if (inflater == null)
 				inflater = (LayoutInflater) getActivity().getSystemService(
 						Context.LAYOUT_INFLATER_SERVICE);
 
 			view = inflater.inflate(R.layout.notification_list_element, null);
 			LinearLayout layout = (LinearLayout) view
 					.findViewById(R.id.notification_list_sublayout);
 
 			Criteria criteria = adapterList.get(position);
 			TextView criteriaTitle = (TextView) view
 					.findViewById(R.id.criteria_title);
 			criteriaTitle.setText(criteria.getName().toUpperCase(
 					Locale.getDefault()));
 
 			for (Menu menu : criteria.getMap().keySet()) {
 				displayMenu(layout, criteria, menu);
 			}
 			return view;
 		}
 
 		private void displayMenu(LinearLayout layout, Criteria criteria,
 				Menu menu) {
 			TextView menuHeader = new TextView(getActivity());
 			menuHeader.setText(R.string.givenMenu);
 			layout.addView(menuHeader);
 			layout.addView(new MenuView(getActivity(), menu, Day.today()));
 			TextView mensaHeader = new TextView(getActivity());
 			mensaHeader.setText(R.string.servedInMensa);
 			layout.addView(mensaHeader);
 			for (Mensa mensa : criteria.getMap().get(menu)) {
 				RelativeLayout rel = (RelativeLayout) inflater.inflate(
 						R.layout.daily_section_title_bar, null);
 				TextView text = (TextView) rel.getChildAt(0);
 				text.setOnClickListener(new AddressTextListener(mensa, this));
 				text.setText(mensa.getName());
 				ImageButton favoriteButton = (ImageButton) rel.getChildAt(1);
 				favoriteButton.setOnClickListener(new FavoriteButtonListener(
 						mensa, favoriteButton));
				favoriteButton.setImageResource(mensa.isFavorite()? R.drawable.ic_fav : R.drawable.ic_fav_grey);
 				ImageButton mapButton = (ImageButton) rel.getChildAt(2);
				mapButton.setImageResource(R.drawable.ic_map);
 				mapButton.setOnClickListener(new MapButtonListener(mensa,
 						NotificationFragment.this));
 				ImageButton inviteButton = (ImageButton) rel.getChildAt(3);
 				inviteButton.setOnClickListener(new InvitationButtonListener(
 						mensa, new Day(new Date()), NotificationFragment.this));
 				layout.addView(rel);
 			}
 		}
 
 		@Override
 		public int getCount() {
 			return adapterList.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			return adapterList.get(position);
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return position;
 		}
 
 		@Override
 		public void notifyDataSetChanged() {
 			adapterList = createList();
 			super.notifyDataSetChanged();
 		}
 
 		private List<Criteria> createList() {
 			SharedPrefsHandler pref = new SharedPrefsHandler(
 					NotificationFragment.this.getActivity());
 
 			Set<String> criteria = pref.getNotificationListItems();
 			boolean allMensas = pref.getNotificationMensas() == 0 ? true
 					: false;
 
 			List<Mensa> mensas = allMensas ? Model.getInstance().getMensas()
 					: Model.getInstance().getFavoriteMensas();
 			return criteriaMatcher.match(criteria, mensas);
 		}
 
 		@Override
 		public void sendListToMenusIntent(Mensa mensa) {
 			((DrawerMenuActivity) getActivity()).launchByMensaAtGivenPage(mensa
 					.getId());
 		}
 
 		public void fill() {
 			adapterList = createList();
 		}
 	}
 }
