 package yuku.infinitepassgen.fr;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.AsyncTaskLoader;
 import android.support.v4.content.Loader;
 import android.text.TextUtils;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import yuku.afw.App;
 import yuku.afw.V;
 import yuku.afw.widget.EasyAdapter;
 import yuku.filechooser.FileChooserActivity;
 import yuku.filechooser.FileChooserConfig;
 import yuku.filechooser.FileChooserConfig.Mode;
 import yuku.filechooser.FileChooserResult;
 import yuku.infinitepassgen.S;
 import yuku.infinitepassgen.ac.BookmarksActivity;
 import yuku.infinitepassgen.app.R;
 import yuku.infinitepassgen.fr.base.BaseFragment;
 import yuku.infinitepassgen.model.Bookmark;
 import yuku.infinitepassgen.util.IniFileImport;
 
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class BookmarksFragment extends BaseFragment {
 	public static final String TAG = BookmarksFragment.class.getSimpleName();
 
 	private static final int REQCODE_import = 1;
 	
 	public BookmarksFragment() {
 	}
 	
 	public static BookmarksFragment create() {
 		return new BookmarksFragment();
 	}
 	
 	ListView lsBookmarks;
 	BookmarkAdapter adapter;
 
 	private OnItemClickListener lsBookmarks_itemClick = new OnItemClickListener() {
 		@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			((BookmarksActivity) getActivity()).finishOk(adapter.getItem(position));
 		}
 	};
 	
 	@Override public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 	
 	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.fragment_bookmarks, menu);
 	}
 	
 	@Override public boolean onOptionsItemSelected(MenuItem item) {
 		int itemId = item.getItemId();
 		if (itemId == R.id.menuImport) {
 			FileChooserConfig config = new FileChooserConfig();
 			config.title = "Where is \"passgen3.ini\"?";
 			config.mode = Mode.Open;
 			config.initialDir = Environment.getExternalStorageDirectory().getAbsolutePath();
 			config.pattern = "passgen3\\.ini";
 			startActivityForResult(FileChooserActivity.createIntent(App.context, config), REQCODE_import);
 			return true;
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View res = inflater.inflate(R.layout.fragment_bookmarks, container, false);
 		lsBookmarks = V.get(res, R.id.lsBookmarks);
 		lsBookmarks.setOnItemClickListener(lsBookmarks_itemClick);
 		return res;
 	}
 	
 	@Override public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		lsBookmarks.setAdapter(adapter = new BookmarkAdapter());
 		getLoaderManager().initLoader(1, null, new LoaderCallbacks<List<Bookmark>>() {
 			@Override public Loader<List<Bookmark>> onCreateLoader(int id, Bundle args) {
 				return new AsyncTaskLoader<List<Bookmark>>(getActivity()) {
 					@Override public List<Bookmark> loadInBackground() {
 						return S.getDb().getAllBookmarks();
 					}
 				};
 			}
 
 			@Override public void onLoadFinished(Loader<List<Bookmark>> loader, List<Bookmark> data) {
 				adapter.setData(data);
 			}
 
 			@Override public void onLoaderReset(Loader<List<Bookmark>> loader) {}
		});
 	}
 	
 	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQCODE_import) {
 			FileChooserResult result = FileChooserActivity.obtainResult(data);
 			if (result != null) {
 				if (result.firstFilename != null) {
 					importFromIniFile(result.firstFilename);
 				}
 			}
 		}
 		
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	private void importFromIniFile(String inifile) {
 		try {
 			Scanner sc = new Scanner(new File(inifile));
 			List<String> lines = new ArrayList<String>();
 			while (sc.hasNextLine()) {
 				String line = sc.nextLine();
 				if (line.length() > 0) {
 					lines.add(line);
 				}
 			}
 			List<Bookmark> bookmarks = IniFileImport.parseIniFileLines(lines);
 			if (bookmarks.size() > 0) {
 				List<String> existings = new ArrayList<String>();
 				List<String> createds = new ArrayList<String>();
 				for (Bookmark bookmark: bookmarks) {
 					Bookmark old = S.getDb().getBookmarkByKeyword(bookmark.keyword);
 					if (old == null) {
 						S.getDb().putBookmark(bookmark);
 						createds.add(bookmark.keyword);
 					} else {
 						existings.add(bookmark.keyword);
 					}
 				}
 				S.msgDialog(getActivity(), 
 					(createds.size() == 0? "": ("New keywords imported: " + TextUtils.join(", ", createds) + "\n\n")) 
 					+ (existings.size() == 0? "": ("The following keywords already exist so it's not imported: " + TextUtils.join(", ", existings)))
 				); 
 			}
 			sc.close();
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, "opening ini file", e);
 			S.msgDialog(getActivity(), "Failed to read " + inifile);
 		}
 	}
 	
 	class BookmarkAdapter extends EasyAdapter {
 		List<Bookmark> data;
 		java.text.DateFormat dateFormat = DateFormat.getDateFormat(getActivity());
 		
 		@Override public Bookmark getItem(int position) {
 			return data == null? null: data.get(position);
 		}
 		
 		@Override public void bindView(View view, int position, ViewGroup parent) {
 			TextView lKeyword = V.get(view, R.id.lKeyword);
 			TextView lUpdatedTime = V.get(view, R.id.lUpdatedTime);
 			
 			Bookmark bookmark = getItem(position);
 			lKeyword.setText(bookmark.keyword);
 			lUpdatedTime.setText(dateFormat.format(bookmark.updatedTime));
 		}
 		
 		public void setData(List<Bookmark> data) {
 			notifyDataSetChanged();
 		}
 
 		@Override public View newView(int position, ViewGroup parent) {
			return LayoutInflater.from(getActivity()).inflate(R.layout.item_bookmark, parent);
 		}
 
 		@Override public int getCount() {
 			return data == null? 0: data.size();
 		}
 	}
 }
