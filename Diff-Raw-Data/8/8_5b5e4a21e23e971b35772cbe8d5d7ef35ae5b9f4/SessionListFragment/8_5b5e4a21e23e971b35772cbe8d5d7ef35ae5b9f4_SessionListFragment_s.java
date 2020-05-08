 package albin.oredev2012;
 
 import java.util.List;
 
 import albin.oredev2012.model.Session;
 import albin.oredev2012.ui.SessionAdapter;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 
 public class SessionListFragment extends Fragment {
 
 	protected SessionAdapter adapter;
	private final List<Session> sessions;
 
 	public SessionListFragment(String date, List<Session> sessions) {
 		this.sessions = sessions;
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		adapter = new SessionAdapter(getActivity(), sessions);
 		View view = inflater.inflate(R.layout.fragment_session_list, null);
 		ExpandableListView listView = (ExpandableListView) view.findViewById(R.id.list);
 		listView.setAdapter(adapter);
 		listView.setOnChildClickListener(new SessionClickListener());
 		return view;
 	}
 
 	private class SessionClickListener implements OnChildClickListener {
 	
 		@Override
 		public boolean onChildClick(ExpandableListView parent, View v,
 				int groupPosition, int childPosition, long id) {
 			Session session = adapter.getSession(groupPosition, childPosition);
 			SessionDetailActivity_.intent(getActivity()).sessionId(session.getId()).start();
 			return true;
 		}
 	
 	}
 	
 }
