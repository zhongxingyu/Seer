 package fr.utc.assos.uvweb;
 
 import android.content.res.Configuration;
import android.os.Build;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewStub;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import fr.utc.assos.uvweb.adapters.UVCommentAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 
 /**
  * A fragment representing a single UV detail screen. This fragment is either
  * contained in a {@link fr.utc.assos.uvweb.activities.UVListActivity} in two-pane mode (on tablets) or a
  * {@link fr.utc.assos.uvweb.activities.UVDetailActivity} on handsets.
  */
 public class UVDetailFragment extends SherlockFragment {
 	private static final String TAG = "UVDetailFragment";
 
 	/**
 	 * The fragment argument representing the UV ID that this fragment
 	 * represents.
 	 */
 	public static final String ARG_UV_ID = "item_id";
 	/**
 	 * The UV this fragment is presenting.
 	 */
 	private UVwebContent.UV mUV;
 	/**
 	 * The ListView containing all comment items.
 	 */
 	private ListView mListView;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public UVDetailFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Fragment configuration
 		setHasOptionsMenu(true);
 
 		final Configuration config = getResources().getConfiguration();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2 || config.smallestScreenWidthDp != 600 || config.orientation != Configuration.ORIENTATION_LANDSCAPE) {
 			// Workaround, we do not want to retain instance for a Nexus 7 for instance,
 			// since in portrait mode only the list is displayed
 			setRetainInstance(true);
 		}
 
 		if (getArguments().containsKey(ARG_UV_ID)) {
 			// Load the UV specified by the fragment
 			// arguments. In a real-world scenario, use a Loader
 			// to load content from a content provider.
 			mUV = UVwebContent.UV_MAP.get(getArguments().getString(
 					ARG_UV_ID));
 
 			getSherlockActivity().getSupportActionBar().setTitle(mUV.toString());
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_uv_detail,
 				container, false);
 
 		mListView = (ListView) rootView.findViewById(android.R.id.list);
 
 		UVCommentAdapter adapter = new UVCommentAdapter(getSherlockActivity());
 		adapter.updateComments(UVwebContent.COMMENTS);
 
 		// Show the UV as text in a TextView.
 		if (mUV != null) {
 			View headerView = rootView.findViewById(android.R.id.empty);
 			if (adapter.isEmpty()) {
 				ViewStub headerViewStub = (ViewStub) headerView;
 				headerViewStub.setOnInflateListener(new ViewStub.OnInflateListener() {
 					@Override
 					public void onInflate(ViewStub stub, View inflated) {
 						mListView.setEmptyView(inflated);
 						setHeaderData(inflated);
 					}
 				});
 				headerViewStub.inflate();
 				headerViewStub = null;
 			} else {
 				headerView = inflater.inflate(R.layout.uv_detail_header, null);
 				setHeaderData(headerView);
 				mListView.addHeaderView(headerView);
 			}
 		}
 
 		mListView.setAdapter(adapter);
 
 		return rootView;
 	}
 
 	private void setHeaderData(View inflatedHeader) {
 		((TextView) inflatedHeader.findViewById(R.id.uvcode)).setText(Html.fromHtml(String.format(
 				UVwebContent.UV_TITLE_FORMAT, mUV.getLetterCode(), mUV.getNumberCode())));
 		((TextView) inflatedHeader.findViewById(R.id.desc)).setText(mUV.getDescription());
 		((TextView) inflatedHeader.findViewById(R.id.rate)).setText(mUV.getFormattedRate());
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.fragment_uv_detail, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_refresh:
 				Toast.makeText(getActivity(), "Refresh clicked", Toast.LENGTH_SHORT).show();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 }
