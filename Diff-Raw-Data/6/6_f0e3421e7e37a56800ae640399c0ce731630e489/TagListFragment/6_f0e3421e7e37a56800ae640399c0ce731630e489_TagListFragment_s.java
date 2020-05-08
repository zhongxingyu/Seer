 package com.evernote.android.sample.tageditor.ui;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.ListFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.evernote.android.sample.tageditor.R;
 import com.evernote.android.sample.tageditor.data.DatabaseHelper;
 import com.evernote.android.sample.tageditor.data.TagListAdapter;
 import com.evernote.android.sample.tageditor.data.TagsDb;
 import com.evernote.android.sample.tageditor.service.TagSyncService;
 import com.evernote.edam.type.Tag;
 
 /**
  * List Fragment that displays the list of Tags
  * 
  * <p>We extend a ListFragment which gives us all the functionality we need 
  * to present a list of items through a listview and its corresponding adapter.</p>
  * 
  * @author Juan Gomez
  * @version 1.0.0
  * @since December 3, 2012
  * 
  */
 public class TagListFragment extends ListFragment {
 
 	private Cursor cursor;
 	private String currentTagGuid;
 	private boolean isTopLevel;
 	private LinearLayout header;
 
 	@Override
 	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		View view = inflater.inflate(R.layout.fragment_tag_list, null);
 		// we save a reference to the header item to be able to manipulate it later
 		header = (LinearLayout) view.findViewById(R.id.header);
 		return view;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// When the fragment gets created we're loading the list of top lever tags.
 		// so we set isTopLevel to true and execute the loader task.
 		isTopLevel = true;
 		new TagLoaderTask().execute();
 	}
 
 	@Override
 	public void onPause() {
 		// close a cursor if it's still open
 		if(cursor != null && !cursor.isClosed())
 			cursor.close();
 		super.onPause();
 	}
 
 	/**
 	 * Get the current Guid of the Parent tag being shown on the fragment
 	 * or "" if we're showing the top level tags
 	 * 
 	 */
 	public String getCurrentTagGuid() {
 		return currentTagGuid;
 	}
 
 	/**
 	 * Is the fragment showing the top level tags
 	 * 
 	 */
 	public boolean isTopLevelList() {
 		return isTopLevel;
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		cursor.moveToPosition(position);
 		// if an item is cisked we determine if it has any children
 		final String selectedTagGuid = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_GUID));
 		final Cursor childCursor = TagsDb.INSTANCE.getChildTags(selectedTagGuid);
 		boolean hasChildren = false;
 		if(childCursor != null) {
 			// if it does, we set hasChildren to true
 			if(childCursor.getCount() > 0) {
 				hasChildren = true;
 			}
 		}
 		// we create a Dialog Fragment to display the possible actions on the selected tag
 		// and send information about the tag and the state of the list fragment using
 		// and arguments bundle.
 		DialogFragment newFragment = new ModifyTagDialogFragment();
 		Bundle argBundle = new Bundle();
 		argBundle.putString(AddEditTagDialogFragment.NAME_BUNDLE_KEY, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)));
 		argBundle.putString(AddEditTagDialogFragment.GUID_BUNDLE_KEY, selectedTagGuid);
 		argBundle.putString(AddEditTagDialogFragment.PARENT_GUID_BUNDLE_KEY, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PARENT_GUID)));
 		argBundle.putBoolean(ModifyTagDialogFragment.HAS_CHILDREN_BUNDLE_KEY, hasChildren);
 
 		newFragment.setArguments(argBundle);
 		// Create the new dialog through the FragmentManager
 		newFragment.show(getFragmentManager(), "TagAction");
 	}
 
 	public void notifyDataSetChanged() {
 		new TagLoaderTask().execute();
 	}
 
 	public void onHeaderClick() {
 		// if the header is clicked (or the back button pressed) we retrieve the parent
 		// tag of the current tag and then reset the lst fragment to display a list
 		// of its children
 		Tag currentTag = TagsDb.INSTANCE.getTagByGuid(currentTagGuid);
 		TextView title = (TextView) header.findViewById(R.id.name_title);
 		if(currentTag.getParentGuid() != null) {
 			currentTagGuid = currentTag.getParentGuid();
 			Tag parentTag = TagsDb.INSTANCE.getTagByGuid(currentTagGuid);
 			title.setText("Tags under " + parentTag.getName());
 		} else {
 			// if the parent Tag is blank, we simply reconfoigure the list view for the
 			// list of top level tags.
 			isTopLevel = true;
 			currentTagGuid =  "";
 			header.setVisibility(View.GONE);
 			header.setOnClickListener(null);
 			title.setText("");
 		}
 		new TagLoaderTask().execute();
 	}
 
 	/**
 	 * This a loader task that performs the query to the database on an AsyncTask to 
 	 * avoid locking the UI thread 
 	 * 
 	 */
 	private class TagLoaderTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected void onPreExecute() {
 			// detach the lsit adapter from the cursor we're about to re-create
 			setListAdapter(null);
 		}
 
 		@Override
 		protected Void doInBackground(Void... v) {
 			// close any open cursors
 			if(cursor != null) {
 				cursor.close();
 				cursor = null;
 			}
 			TagsDb.INSTANCE.open(getActivity());
 
 			// if isTopLevel equals false we send the Guid of the current task to get its children
 			if(isTopLevel) {
 				cursor = TagsDb.INSTANCE.getTopLevelTags();
 			} else {
 				cursor = TagsDb.INSTANCE.getChildTags(TagListFragment.this.currentTagGuid);
 			}
 
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void v) {
 			if(cursor != null && !cursor.isClosed() && cursor.getCount() > 0) {
 				setListAdapter(new TagListAdapter(TagListFragment.this.getActivity(), cursor));
 			}
 		}
 	}
 
 	/**
 	 * Dialog Fragment used to display an list of options to be performed on the selected tag
 	 * 
 	 */
 	public static class ModifyTagDialogFragment extends DialogFragment {
 
 		
 		private static final int ACTION_VIEW = 2;
 		private static final int ACTION_DELETE = 1;
 		private static final int ACTION_EDIT = 0;
 		public static final String HAS_CHILDREN_BUNDLE_KEY = "hasChildren";
 
 		@Override
 		public Dialog onCreateDialog(Bundle savedInstanceState) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			// We use an arguments bundle to set the data for this dialog
 			final Bundle argBundle = getArguments();
 			// we build an array with the possible options to present to the user
 			StringBuilder options = new StringBuilder();
 			getResources().getString(R.string.action_edit);
 			options.append(getResources().getString(R.string.action_edit));
 			options.append(",");
 			options.append(getResources().getString(R.string.action_delete));
			if(argBundle.getBoolean(HAS_CHILDREN_BUNDLE_KEY))
 				options.append(",");
			options.append(getResources().getString(R.string.action_view));
 			// and set the main attributes of the dialog
 			builder.setTitle(getResources().getString(R.string.action_prompt))
 			.setItems(options.toString().split(","), new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					//the which argument tells us, which option was selected
 					switch(which) {
 					case ACTION_EDIT:
 						// If it's Edit, we ahow the Add/Edit dialog, but we set the 
 						// EDIT_BUNDLE_KEY to true on the arguments bundle.
 						AddEditTagDialogFragment newFragment = new AddEditTagDialogFragment();
 						argBundle.putBoolean(AddEditTagDialogFragment.EDIT_BUNDLE_KEY, true);
 						newFragment.setArguments(argBundle);
 						dismiss();
 						newFragment.show(getFragmentManager(), AddEditTagDialogFragment.ADD_EDIT_FRAGMENT_TAG);
 						break;
 					case ACTION_DELETE:
 						// If it's delete we can call the sync service directly with the Guid
 						// and the Delete task
 						dismiss();
 						Intent i = new Intent(getActivity().getApplicationContext(), TagSyncService.class);
 						i.putExtra(TagSyncService.EXTRA_CURRENT_TASK, TagSyncService.Task.DELETE);
 						i.putExtra(TagSyncService.EXTRA_TAG_GUID, argBundle.getString(AddEditTagDialogFragment.GUID_BUNDLE_KEY));
 						getActivity().startService(i);
 						break;
 					case ACTION_VIEW:
 						// If we're viewing the embedded tasks, we re-set the parameters of the 
 						// list dialog and triger a re-query
 						final TagListFragment listFragment = (TagListFragment) getFragmentManager().findFragmentByTag(TagEditorActivity.LIST_FRAGMENT_TAG);
 						listFragment.isTopLevel = false;
 						listFragment.currentTagGuid = argBundle.getString(AddEditTagDialogFragment.GUID_BUNDLE_KEY);
 						// we also activate the header view, so that the user can get back to the previous screen
 						listFragment.header.setVisibility(View.VISIBLE);
 						listFragment.header.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 								listFragment.onHeaderClick();
 							}
 						});
 						TextView title = (TextView) listFragment.header.findViewById(R.id.name_title);
 						title.setText(argBundle.getString(AddEditTagDialogFragment.NAME_BUNDLE_KEY));
 						listFragment.notifyDataSetChanged();
 					}
 				}
 			});
 			return builder.create();
 		}
 	}
 }
