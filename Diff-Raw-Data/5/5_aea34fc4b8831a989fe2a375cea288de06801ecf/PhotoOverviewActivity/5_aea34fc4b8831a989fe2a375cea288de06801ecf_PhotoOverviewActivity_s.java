 package ch.bergturbenthal.raoa.client.photo;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.ContentObserver;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Pair;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.SubMenu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.SearchView;
 import android.widget.SearchView.OnQueryTextListener;
 import android.widget.ShareActionProvider;
 import ch.bergturbenthal.raoa.R;
 import ch.bergturbenthal.raoa.client.album.AlbumOverviewActivity;
 import ch.bergturbenthal.raoa.client.binding.AbstractViewHandler;
 import ch.bergturbenthal.raoa.client.binding.ComplexCursorAdapter;
 import ch.bergturbenthal.raoa.client.binding.PhotoViewHandler;
 import ch.bergturbenthal.raoa.client.binding.TextViewHandler;
 import ch.bergturbenthal.raoa.client.binding.ViewHandler;
 import ch.bergturbenthal.raoa.client.util.KeywordUtil;
 import ch.bergturbenthal.raoa.client.util.SimpleAsync;
 import ch.bergturbenthal.raoa.provider.Client;
 import ch.bergturbenthal.raoa.provider.criterium.Compare;
 import ch.bergturbenthal.raoa.provider.criterium.Compare.Operator;
 import ch.bergturbenthal.raoa.provider.criterium.Constant;
 import ch.bergturbenthal.raoa.provider.criterium.Criterium;
 import ch.bergturbenthal.raoa.provider.criterium.Field;
 import ch.bergturbenthal.raoa.provider.criterium.Value;
 
 public class PhotoOverviewActivity extends Activity {
 	private static class EntryValues {
 		Collection<String> keywords = new HashSet<String>();
 		Uri thumbnailUri;
 	}
 
 	private static interface KeywordsHandler {
 		void handleKeywords(final Collection<String> keywords);
 	}
 
 	private enum UiMode {
 		NAVIGATION, SELECTION
 	}
 
 	private static final String CURR_ITEM_INDEX = "currentItemIndex";
 	private static final String MODE_KEY = PhotoOverviewActivity.class.getName() + "-mode";
 	private static final String SELECTION_KEY = PhotoOverviewActivity.class.getName() + "-selection";
 
 	private Uri albumEntriesUri;
 	private String albumTitle = null;
 	private Uri albumUri;
 	private String currentFilter;
 	private int currentItemIndex;
 
 	private UiMode currentMode = UiMode.NAVIGATION;
 
 	private ComplexCursorAdapter cursorAdapter;
 
 	private Collection<String> enabledStorages = Collections.emptyList();
 
 	private GridView gridview;
 
 	private List<String> knownKeywords;
 
 	private int lastLongClickposition = -1;
 
 	private final Map<String, EntryValues> selectedEntries = new HashMap<String, EntryValues>();
 
 	@Override
 	public boolean onCreateOptionsMenu(final Menu menu) {
 		switch (currentMode) {
 		case SELECTION:
 			getMenuInflater().inflate(R.menu.photo_overview_selection_menu, menu);
 
 			final ShareActionProvider shareActionProvider = (ShareActionProvider) menu.findItem(R.id.photo_overview_menu_share).getActionProvider();
 			final Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
 			shareIntent.setType("image/jpeg");
 			shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, makeCurrentSelectedUris());
 			shareActionProvider.setShareIntent(shareIntent);
 
 			final MenuItem addTagsMenu = menu.findItem(R.id.photo_overview_menu_add_tag_menu);
 			final SubMenu tagsSubmenu = addTagsMenu.getSubMenu();
 			tagsSubmenu.removeGroup(R.id.photo_overview_menu_existing_tag);
 			final Map<String, Integer> selectedKeywordCounts = new HashMap<String, Integer>();
 			for (final EntryValues entryValues : selectedEntries.values()) {
 				for (final String keyword : entryValues.keywords) {
 					final Integer existingKeyword = selectedKeywordCounts.get(keyword);
 					if (existingKeyword != null) {
 						selectedKeywordCounts.put(keyword, Integer.valueOf(existingKeyword.intValue() + 1));
 					} else {
 						selectedKeywordCounts.put(keyword, Integer.valueOf(1));
 					}
 					if (!knownKeywords.contains(keyword)) {
 						knownKeywords.add(keyword);
 					}
 				}
 			}
 			for (final String keyword : knownKeywords) {
 				final Integer count = selectedKeywordCounts.get(keyword);
 				final String keywordDisplay = count != null ? keyword + " (" + count + ")" : keyword;
 				final MenuItem item = tagsSubmenu.add(R.id.photo_overview_menu_existing_tag, Menu.NONE, Menu.NONE, keywordDisplay);
 				item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 					@Override
 					public boolean onMenuItemClick(final MenuItem item) {
 						setTagToSelectedEntries(keyword);
 						return true;
 					}
 				});
 			}
 
 			final MenuItem removeTagsMenu = menu.findItem(R.id.photo_overview_menu_remove_tag_menu);
 			final SubMenu removeTagsSubmenu = removeTagsMenu.getSubMenu();
 			removeTagsSubmenu.clear();
 			final ArrayList<String> keywordsByCount = KeywordUtil.orderKeywordsByFrequent(selectedKeywordCounts);
 			for (final String keyword : keywordsByCount) {
 				final String keywordDisplay = keyword + " (" + selectedKeywordCounts.get(keyword) + ")";
 				final MenuItem removeTagItem = removeTagsSubmenu.add(keywordDisplay);
 				removeTagItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 					@Override
 					public boolean onMenuItemClick(final MenuItem item) {
 						removeTagFromSelectedEntries(keyword);
 						return true;
 					}
 
 				});
 			}
 
 			// final SubMenu subMenu = findItem.getSubMenu();
 			// subMenu.clear();
 			// subMenu.add("Hello Tag");
 			break;
 		case NAVIGATION:
 			getMenuInflater().inflate(R.menu.photo_overview_navigation_menu, menu);
 			final MenuItem shareItem = menu.findItem(R.id.photo_overview_share_album);
 			final SubMenu subMenu = shareItem.getSubMenu();
 			subMenu.clear();
 			final Handler handler = new Handler();
 			new SimpleAsync() {
 
 				private final List<Pair<String, String>> menuEntries = new ArrayList<Pair<String, String>>();
 
 				@Override
 				protected void doInBackground() {
 					final Cursor storagesCursor = getContentResolver().query(	Client.STORAGE_URI,
 																																		new String[] { Client.Storage.STORAGE_ID,
 																																									Client.Storage.STORAGE_NAME,
 																																									Client.Storage.TAKE_ALL_REPOSITORIES },
 																																		null,
 																																		null,
 																																		null);
 
 					if (storagesCursor.moveToFirst()) {
 						final int idColumn = storagesCursor.getColumnIndexOrThrow(Client.Storage.STORAGE_ID);
 						final int nameColumn = storagesCursor.getColumnIndexOrThrow(Client.Storage.STORAGE_NAME);
 						final int allRepositoriesColumn = storagesCursor.getColumnIndexOrThrow(Client.Storage.TAKE_ALL_REPOSITORIES);
 						do {
 							final String storageId = storagesCursor.getString(idColumn);
 							final String storageName = storagesCursor.getString(nameColumn);
 							if (storagesCursor.getInt(allRepositoriesColumn) != 0) {
 								continue;
 							}
 							menuEntries.add(new Pair<String, String>(storageId, storageName));
 						} while (storagesCursor.moveToNext());
 					}
 					storagesCursor.registerContentObserver(new ContentObserver(handler) {
 
 						@Override
 						public void onChange(final boolean selfChange) {
 							super.onChange(selfChange);
 							invalidateOptionsMenu();
 						}
 					});
 					Collections.sort(menuEntries, new Comparator<Pair<String, String>>() {
 						@Override
 						public int compare(final Pair<String, String> lhs, final Pair<String, String> rhs) {
 							return lhs.second.compareTo(rhs.second);
 						}
 					});
 				}
 
 				@Override
 				protected void onPostExecute() {
 					for (final Pair<String, String> entry : menuEntries) {
 						final String entryId = entry.first;
 						final String entryName = entry.second;
 						final MenuItem storageItem = subMenu.add(entryName);
 						storageItem.setCheckable(true);
 						final boolean enabled = enabledStorages.contains(entryId);
 						storageItem.setChecked(enabled);
 						storageItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 							@Override
 							public boolean onMenuItemClick(final MenuItem item) {
 								enableStorage(entryId, !enabled);
 								return true;
 							}
 
 						});
 					}
 				}
 
 			}.execute();
 			final MenuItem searchClearItem = menu.findItem(R.id.photo_overview_clear_search_album);
 			searchClearItem.setVisible(currentFilter != null);
 			searchClearItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
 
 				@Override
 				public boolean onMenuItemClick(final MenuItem item) {
 					initLoaderWithFilter(null);
 					return true;
 				}
 			});
			final SearchView searchView = (SearchView) menu.findItem(R.id.photo_overview_search_album).getActionView();
 			if (currentFilter != null) {
 				final Criterium filter = Criterium.decodeString(currentFilter);
 				if (filter instanceof Compare) {
 					final Compare comp = (Compare) filter;
 					final Value op1 = comp.getOp1();
 					final Value op2 = comp.getOp2();
 					if (op1 instanceof Field && ((Field) op1).getFieldName().equals(Client.AlbumEntry.META_KEYWORDS)
 							&& comp.getOperator() == Operator.CONTAINS
 							&& op2 instanceof Constant) {
 						final String value = (String) ((Constant) op2).getValue();
 						searchView.setQuery(value, false);
 						searchView.setIconified(false);
 					}
 				}
 			}
 			searchView.setOnQueryTextListener(new OnQueryTextListener() {
 
 				@Override
 				public boolean onQueryTextChange(final String newText) {
 					return false;
 				}
 
 				@Override
 				public boolean onQueryTextSubmit(final String query) {
 					initLoaderWithFilter(Criterium.contains(new Field(Client.AlbumEntry.META_KEYWORDS), new Constant(query.trim())).makeString());
 					return true;
 				}
 			});
 			break;
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			final Intent upIntent = new Intent(this, AlbumOverviewActivity.class);
 			upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(upIntent);
 			finish();
 			return true;
 		case R.id.photo_overview_menu_add_new_tag:
 			setNewTag();
 			return true;
 		case R.id.photo_overview_menu_close_selection:
 			activateNavigationMode();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (data != null) {
 			final Bundle bundle = data.getExtras();
 			currentItemIndex = bundle.getInt(CURR_ITEM_INDEX);
 			gridview.setSelection(currentItemIndex);
 		}
 	}
 
 	@Override
 	protected void onCreate(final Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		// get album id out of intent
 		final Bundle bundle = getIntent().getExtras();
 		albumEntriesUri = Uri.parse(bundle.getString("album_entries_uri"));
 		albumUri = Uri.parse(bundle.getString("album_uri"));
 		setContentView(R.layout.photo_overview);
 
 		if (savedInstanceState != null) {
 			final String[] savedSelection = savedInstanceState.getStringArray(SELECTION_KEY);
 			if (savedSelection != null) {
 				for (final String selectedEntry : savedSelection) {
 					selectedEntries.put(selectedEntry, new EntryValues());
 				}
 			}
 		}
 
 		cursorAdapter = new ComplexCursorAdapter(this, R.layout.photo_overview_item, makeHandlers(), new String[] { Client.AlbumEntry.ENTRY_URI,
 																																																								Client.AlbumEntry.META_KEYWORDS,
 																																																								Client.AlbumEntry.THUMBNAIL_ALIAS });
 		initLoaderWithFilter(null);
 
 		gridview = (GridView) findViewById(R.id.photo_overview);
 		gridview.setAdapter(cursorAdapter);
 
 		// Handle click on photo
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(final AdapterView<?> parent, final View v, final int position, final long id) {
 				shortClick(position);
 			}
 
 		});
 		gridview.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
 				return longClick(position);
 			}
 		});
 		gridview.setWillNotCacheDrawing(false);
 		loadAlbumEntry(albumUri);
 
 		final UiMode mode = savedInstanceState == null ? UiMode.NAVIGATION : UiMode.valueOf(savedInstanceState.getString(MODE_KEY, UiMode.NAVIGATION.name()));
 		switch (mode) {
 		case NAVIGATION:
 			activateNavigationMode();
 			break;
 		case SELECTION:
 			activateSelectionMode();
 			break;
 		}
 	}
 
 	private void activateNavigationMode() {
 		currentMode = UiMode.NAVIGATION;
 		if (albumTitle != null) {
 			getActionBar().setTitle(albumTitle);
 		}
 		selectedEntries.clear();
 		lastLongClickposition = -1;
 		redraw();
 		invalidateOptionsMenu();
 	}
 
 	private void activateSelectionMode() {
 		currentMode = UiMode.SELECTION;
 		invalidateOptionsMenu();
 	}
 
 	private void addEntryToSelection(final Pair<String, EntryValues> pair) {
 		selectedEntries.put(pair.first, pair.second);
 		invalidateOptionsMenu();
 	}
 
 	private void enableStorage(final String entryId, final boolean enabled) {
 		new SimpleAsync() {
 
 			@Override
 			protected void doInBackground() {
 				if (albumUri == null) {
 					return;
 				}
 				final ContentResolver contentResolver = getContentResolver();
 				final Cursor cursor = contentResolver.query(albumUri, new String[] { Client.Album.STORAGES }, null, null, null);
 				try {
 					if (cursor == null || !cursor.moveToFirst()) {
 						return;
 					}
 					final Collection<String> storages = new LinkedHashSet<String>();
 					final String storagesRaw = cursor.getString(cursor.getColumnIndexOrThrow(Client.Album.STORAGES));
 					if (storagesRaw != null) {
 						storages.addAll(Client.Album.decodeStorages(storagesRaw));
 					}
 					if (enabled) {
 						storages.add(entryId);
 					} else {
 						storages.remove(entryId);
 					}
 					final ContentValues values = new ContentValues();
 					values.put(Client.Album.STORAGES, Client.Album.encodeStorages(storages));
 					contentResolver.update(albumUri, values, null, null);
 				} finally {
 					if (cursor != null) {
 						cursor.close();
 					}
 				}
 			}
 		}.execute();
 	}
 
 	private void initLoaderWithFilter(final String filter) {
 		currentFilter = filter;
 		getLoaderManager().restartLoader(0, null, new LoaderCallbacks<Cursor>() {
 
 			@Override
 			public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
 				return new CursorLoader(PhotoOverviewActivity.this, albumEntriesUri, cursorAdapter.requiredFields(), currentFilter, null, null);
 			}
 
 			@Override
 			public void onLoaderReset(final Loader<Cursor> loader) {
 				cursorAdapter.swapCursor(null);
 			}
 
 			@Override
 			public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
 				final Collection<String> oldSelectedEntries = new HashSet<String>(selectedEntries.keySet());
 				selectedEntries.clear();
 				try {
 					if (data == null || !data.moveToFirst()) {
 						return;
 					}
 					final int entryColumn = data.getColumnIndex(Client.AlbumEntry.ENTRY_URI);
 					final int keywordsColumn = data.getColumnIndex(Client.AlbumEntry.META_KEYWORDS);
 					final int thumbnailColumn = data.getColumnIndex(Client.AlbumEntry.THUMBNAIL_ALIAS);
 					do {
 						final String uri = data.getString(entryColumn);
 						if (oldSelectedEntries.contains(uri)) {
 							selectedEntries.put(uri, makeEntry(data.getString(keywordsColumn), data.getString(thumbnailColumn)));
 						}
 					} while (data.moveToNext());
 				} finally {
 					cursorAdapter.swapCursor(data);
 					invalidateOptionsMenu();
 				}
 			}
 		});
 	}
 
 	private void loadAlbumEntry(final Uri albumUri) {
 		final Handler handler = new Handler();
 		new SimpleAsync() {
 
 			@Override
 			protected void doInBackground() {
 				final Cursor cursor = getContentResolver().query(albumUri, new String[] { Client.Album.TITLE, Client.Album.STORAGES }, null, null, null);
 				try {
 					if (cursor.moveToFirst()) {
 						albumTitle = cursor.getString(cursor.getColumnIndexOrThrow(Client.Album.TITLE));
 						enabledStorages = Client.Album.decodeStorages(cursor.getString(cursor.getColumnIndexOrThrow(Client.Album.STORAGES)));
 					}
 					cursor.registerContentObserver(new ContentObserver(handler) {
 						@Override
 						public boolean deliverSelfNotifications() {
 							return true;
 						}
 
 						@Override
 						public void onChange(final boolean selfChange) {
 							loadAlbumEntry(albumUri);
 						}
 
 					});
 				} finally {
 					cursor.close();
 				}
 				knownKeywords = KeywordUtil.getKnownKeywords(getContentResolver());
 			}
 
 			@Override
 			protected void onPostExecute() {
 				invalidateOptionsMenu();
 			}
 		}.execute();
 	}
 
 	private boolean longClick(final int position) {
 		if (lastLongClickposition >= 0) {
 			final int lower = Math.min(position, lastLongClickposition);
 			final int upper = Math.max(position, lastLongClickposition);
 			for (int i = lower; i <= upper; i++) {
 				addEntryToSelection(readCurrentEntry(i));
 			}
 			lastLongClickposition = -1;
 		} else {
 			lastLongClickposition = position;
 			addEntryToSelection(readCurrentEntry(position));
 		}
 		if (currentMode != UiMode.SELECTION) {
 			activateSelectionMode();
 		}
 		redraw();
 		return true;
 	}
 
 	/**
 	 * @return
 	 */
 	private ArrayList<Uri> makeCurrentSelectedUris() {
 		final ArrayList<Uri> ret = new ArrayList<Uri>();
 		for (final EntryValues value : selectedEntries.values()) {
 			ret.add(value.thumbnailUri);
 		}
 		return ret;
 	}
 
 	private EntryValues makeEntry(final String keywordValue, final String uriString) {
 		final EntryValues values = new EntryValues();
 		final Collection<String> keywords = Client.AlbumEntry.decodeKeywords(keywordValue);
 		values.keywords = keywords;
 		values.thumbnailUri = Uri.parse(uriString);
 		return values;
 	}
 
 	/**
 	 * @return
 	 */
 	private Collection<ViewHandler<? extends View>> makeHandlers() {
 		final ArrayList<ViewHandler<? extends View>> ret = new ArrayList<ViewHandler<? extends View>>();
 		ret.add(new PhotoViewHandler(R.id.photos_item_image, Client.AlbumEntry.THUMBNAIL_ALIAS, new PhotoViewHandler.DimensionCalculator(R.dimen.image_width)));
 		ret.add(new TextViewHandler(R.id.photo_name, Client.AlbumEntry.NAME));
 		ret.add(new AbstractViewHandler<View>(R.id.photos_overview_grid_item) {
 
 			@Override
 			public void bindView(final View view, final Context context, final Map<String, Object> values) {
 				final String entryUri = (String) values.get(Client.AlbumEntry.ENTRY_URI);
 				if (selectedEntries.containsKey(entryUri)) {
 					view.setBackgroundResource(R.drawable.layout_border);
 				} else {
 					view.setBackgroundResource(R.drawable.layout_no_border);
 				}
 			}
 
 			@Override
 			public String[] usedFields() {
 				return new String[] { Client.AlbumEntry.ENTRY_URI };
 			}
 		});
 		return ret;
 	}
 
 	private void openDetailView(final int position) {
 		final Intent intent = new Intent(PhotoOverviewActivity.this, PhotoDetailViewActivity.class);
 		intent.putExtra(PhotoDetailViewActivity.ALBUM_URI, albumEntriesUri.toString());
 		intent.putExtra(PhotoDetailViewActivity.ACTUAL_POS, position);
 		intent.putExtra(PhotoDetailViewActivity.CURRENT_FILTER, currentFilter);
 		startActivityForResult(intent, 1);
 	}
 
 	private Pair<String, EntryValues> readCurrentEntry(final int position) {
 		final Object[] additionalValues = cursorAdapter.getAdditionalValues(position);
 		final String uri = (String) additionalValues[0];
 		final String keywordValue = (String) additionalValues[1];
 		final String uriString = (String) additionalValues[2];
 		return new Pair<String, EntryValues>(uri, makeEntry(keywordValue, uriString));
 	}
 
 	private void redraw() {
 		gridview.requestLayout();
 		gridview.invalidateViews();
 	}
 
 	private void removeTagFromSelectedEntries(final String keyword) {
 		updateSelectedEntries(new KeywordsHandler() {
 			@Override
 			public void handleKeywords(final Collection<String> keywords) {
 				keywords.remove(keyword);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 */
 	private void setNewTag() {
 		final EditText newTagValue = new EditText(this);
 		new AlertDialog.Builder(this).setTitle("Input new Tag").setView(newTagValue).setPositiveButton("Ok", new OnClickListener() {
 
 			@Override
 			public void onClick(final DialogInterface dialog, final int which) {
 				setTagToSelectedEntries(newTagValue.getText().toString());
 			}
 		}).setNegativeButton("Cancel", new OnClickListener() {
 			@Override
 			public void onClick(final DialogInterface dialog, final int which) {
 			}
 		}).show();
 	}
 
 	private void setTagToSelectedEntries(final String tagValue) {
 		updateSelectedEntries(new KeywordsHandler() {
 			@Override
 			public void handleKeywords(final Collection<String> keywords) {
 				keywords.add(tagValue);
 			}
 		});
 	}
 
 	private void shortClick(final int position) {
 		switch (currentMode) {
 		case NAVIGATION:
 			openDetailView(position);
 			break;
 		case SELECTION:
 			toggleSelection(position);
 			break;
 		}
 	}
 
 	/**
 	 * @param position
 	 */
 	private void toggleSelection(final int position) {
 		final Pair<String, EntryValues> currentEntry = readCurrentEntry(position);
 		if (selectedEntries.remove(currentEntry.first) == null) {
 			selectedEntries.put(currentEntry.first, currentEntry.second);
 		}
 		redraw();
 		invalidateOptionsMenu();
 	}
 
 	private void updateSelectedEntries(final KeywordsHandler handler) {
 		for (final String entryUri : selectedEntries.keySet()) {
 			final ContentResolver resolver = getContentResolver();
 			final Uri uri = Uri.parse(entryUri);
 			final Cursor queryCursor = resolver.query(uri, new String[] { Client.AlbumEntry.META_KEYWORDS }, null, null, null);
 			try {
 				if (!queryCursor.moveToFirst()) {
 					continue;
 				}
 				final Collection<String> keywords = new HashSet<String>(Client.AlbumEntry.decodeKeywords(queryCursor.getString(0)));
 				handler.handleKeywords(keywords);
 				final ContentValues values = new ContentValues();
 				values.put(Client.AlbumEntry.META_KEYWORDS, Client.AlbumEntry.encodeKeywords(keywords));
 				resolver.update(uri, values, null, null);
 			} finally {
 				queryCursor.close();
 			}
 		}
 		invalidateOptionsMenu();
 	}
 }
