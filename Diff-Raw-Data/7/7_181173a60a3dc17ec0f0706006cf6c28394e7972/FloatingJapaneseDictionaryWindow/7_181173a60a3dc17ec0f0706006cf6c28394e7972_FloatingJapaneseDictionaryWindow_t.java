 package pedals.is.floatingjapanesedictionary;
 
 import pedals.is.floatingjapanesedictionary.dictionarysearcher.DictionaryEntries;
 import pedals.is.floatingjapanesedictionary.dictionarysearcher.DictionaryEntry;
 import android.app.SearchManager;
 import android.content.ComponentName;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import wei.mark.standout.StandOutWindow;
 import wei.mark.standout.constants.StandOutFlags;
 import wei.mark.standout.ui.Window;
 
 public class FloatingJapaneseDictionaryWindow extends StandOutWindow {
 
 	public static final int DISPLAY_TEXT = 0, DISPLAY_DEFINITION = 1,
 			DISPLAY_ERROR = 2, DISPLAY_SEARCH = 3;
 
 	private static boolean CLOSED = true;
 
 	private StandOutLayoutParams closedParams;
 	private StandOutLayoutParams openedParams;
 
 	@Override
 	public void createAndAttachView(final int id, FrameLayout frame) {
 
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		View view = inflater.inflate(R.layout.floatingdictionary, frame, true);
 
 		SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
 		SearchView searchView = (SearchView) view.findViewById(R.id.search);
 		searchView
 				.setSearchableInfo(searchManager
 						.getSearchableInfo(new ComponentName(
 								"pedals.is.floatingjapanesedictionary",
 								"pedals.is.floatingjapanesedictionary.dictionarysearcher.DictionarySearcherActivity")));
 		searchView.setSubmitButtonEnabled(true);
 		searchView.setIconified(true);
 		FloatingJapaneseDictionaryWindow.CLOSED = true;
 
 		final FloatingJapaneseDictionaryWindow thisWindow = this;
 		searchView.setOnCloseListener(new SearchView.OnCloseListener() {
 
 			public boolean onClose() {
 
 				setClosedState(thisWindow, id);
 				return false;
 			}
 		});
 
 		searchView.setOnSearchClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(final View searchView) {
 
 				thisWindow.getWindow(id).addOnLayoutChangeListener(
 						new View.OnLayoutChangeListener() {
 
 							@Override
 							public void onLayoutChange(View v, int left,
 									int top, int right, int bottom,
 									int oldLeft, int oldTop, int oldRight,
 									int oldBottom) {
 
 								searchView.requestFocus();
 								v.removeOnLayoutChangeListener(this);
 							}
 						});
 
 				setOpenedState(thisWindow, id);
 
 			}
 		});
 	}
 
 	private void setClosedState(FloatingJapaneseDictionaryWindow thisWindow,
 			int id) {
 
 		FloatingJapaneseDictionaryWindow.CLOSED = true;
 		Window window = thisWindow.getWindow(id);
 		thisWindow.clearText(window);
 		thisWindow.updateViewLayout(id, thisWindow.getParams(id));
 	}
 
 	private void setOpenedState(FloatingJapaneseDictionaryWindow thisWindow,
 			int id) {
 
 		FloatingJapaneseDictionaryWindow.CLOSED = false;
 		Window window = thisWindow.getWindow(id);
 		thisWindow.clearText(window);
 		thisWindow.updateViewLayout(id, thisWindow.getParams(id));
 
 	}
 
 	@Override
	public boolean onClose(int id, Window window) {

		setClosedState(this, id);
		return false;
	}

	@Override
 	public StandOutLayoutParams getParams(int id, Window window) {
 
 		return getParams(id);
 	}
 
 	public StandOutLayoutParams getParams(int id) {
 
 		if (CLOSED) {
 			return getClosedParams(id);
 		}
 		return getOpenedParams(id);
 	}
 
 	@Override
 	public int getFlags(int id) {
 
 		return super.getFlags(id) | StandOutFlags.FLAG_DECORATION_SYSTEM
 				| StandOutFlags.FLAG_BODY_MOVE_ENABLE
 				| StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE;
 	}
 
 	@Override
 	public void onReceiveData(int id, int requestCode, Bundle data,
 			Class<? extends StandOutWindow> fromCls, int fromId) {
 
 		Window window = getWindow(id);
 
 		clearText(window);
 
 		switch (requestCode) {
 			case DISPLAY_DEFINITION:
 				displayDefinition(window, data.getString("TEXT"));
 				break;
 			case DISPLAY_SEARCH:
 				displaySearch(window, data.getString("TEXT"));
 			case DISPLAY_TEXT:
 			default:
 				displayText(window, data.getString("TEXT"));
 		}
 
 	}
 
 	private void clearText(Window window) {
 
 		TextView status = (TextView) window.findViewById(R.id.status);
 		ListView listView = (ListView) window.findViewById(R.id.results);
 		status.setText("");
 		listView.setAdapter(new ArrayAdapter<Object>(window.getContext(),
 				R.layout.dictionaryentry));
 
 	}
 
 	private void displayDefinition(Window window, String text) {
 
 		DictionaryEntries entries = null;
 		try {
 			entries = DictionaryEntries.fromJSON(text);
 		}
 		catch (Exception e) {
 			displayError(window, "definitions lost inflight");
 		}
 		ArrayAdapter<DictionaryEntry> adapter = new ArrayAdapter<DictionaryEntry>(
 				window.getContext(), R.layout.dictionaryentry, entries);
 		ListView listView = (ListView) window.findViewById(R.id.results);
 		listView.setAdapter(adapter);
 	}
 
 	private void displayError(Window window, String error) {
 
 		TextView status = (TextView) window.findViewById(R.id.status);
 		status.setTextSize(20);
 		status.setText("Error: " + error);
 	}
 
 	private void displayText(Window window, String text) {
 
 		TextView status = (TextView) window.findViewById(R.id.status);
 		status.setTextSize(20);
 		status.setText(text);
 	}
 
 	private void displaySearch(Window window, String text) {
 
 		SearchView searchView = (SearchView) window.findViewById(R.id.search);
 		searchView.setQuery(text, false);
 	}
 
 	private StandOutLayoutParams getClosedParams(int id) {
 
 		final int CLOSED_WIDTH = 65;
 		final int CLOSED_HEIGHT = 128;
 
 		if (closedParams == null) {
 			closedParams = new StandOutLayoutParams(id, CLOSED_WIDTH,
 					CLOSED_HEIGHT);
 		}
 		return closedParams;
 	}
 
 	private StandOutLayoutParams getOpenedParams(int id) {
 
 		final int OPENED_WIDTH = 400;
 		final int OPENED_HEIGHT = 400;
 
 		if (openedParams == null) {
 			openedParams = new StandOutLayoutParams(id, OPENED_WIDTH,
 					OPENED_HEIGHT);
 		}
 		return openedParams;
 	}
 
 	@Override
 	public String getAppName() {
 
 		return "Floating Japanese Dictionary";
 	}
 
 	@Override
 	public int getAppIcon() {
 
 		return android.R.drawable.ic_menu_add;
 	}
 
 }
