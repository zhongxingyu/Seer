 package com.crayonio.mediacodecquery;
 
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.media.MediaCodecInfo.CodecCapabilities;
 import android.media.MediaCodecInfo.CodecProfileLevel;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
class LevelFragment extends Fragment implements
 		OnItemClickListener {
 
 	/**
 	 * The fragment argument representing the section number for this
 	 * fragment.
 	 */
 	private CodecProfileLevel[] profileLevels;
 	private int cachedCodecIndex = -1;
 
 	private ArrayList<Boolean> profileVerbosity;
 	private ListView myListView;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 	                         Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_codec_profile,
 				container, false);
 
 		if (rootView != null) {
 			myListView = (ListView) rootView.findViewById(R.id.profile_list);
 			myListView.setOnItemClickListener(this);
 			registerForContextMenu(myListView);
 
 			String selectedType = getArguments().getString(CodecProfileActivity.SELECTED_TYPE);
 			int codecIndex = getArguments().getInt(CodecProfileActivity.CODEC_INDEX);
 
 		/*
 		 * Log.d("Codec Profile Fragment", " Showing " + selectedType +
 		 * " of codec " + codecIndex + " in " + sectionNum);
 		 */
 
 			if (cachedCodecIndex != codecIndex) {
 
 				CodecInfo thisCodecInfo = CodecInfoList.getCodecInfoList().get(codecIndex);
 				CodecCapabilities capabilities = thisCodecInfo.getCapabilitiesForType(selectedType);
 				profileLevels = capabilities.profileLevels;
 				cachedCodecIndex = codecIndex;
 
 				Boolean[] array = new Boolean[profileLevels.length];
 				Arrays.fill(array, Boolean.FALSE);
 				profileVerbosity = new ArrayList<Boolean>(Arrays.asList(array));
 			}
 
 			CodecProfileLevelTranslator profileTranslator = CodecProfileLevelTranslator.getInstance();
 
 			final ArrayList<CodecProfileStrings> codecProfileStrings = new ArrayList<CodecProfileStrings>();
 
 			for (CodecProfileLevel thisProfile : profileLevels) {
 				String translatedProfile = profileTranslator.getProfile(thisProfile.profile);
 				if (translatedProfile == null)
 					translatedProfile = (String.valueOf(thisProfile.profile));
 
 				String translatedLevel = profileTranslator
 						.getLevel(thisProfile.level);
 				if (translatedLevel == null)
 					translatedLevel = ("0x" + Integer.toHexString(thisProfile.level));
 
 				codecProfileStrings.add(new CodecProfileStrings(
 						translatedProfile, translatedLevel));
 			}
 
 			myListView.setAdapter(new ArrayAdapter<CodecProfileStrings>(
 					getActivity().getApplicationContext(),
 					R.layout.codec_profile_row, R.id.profileName,
 					codecProfileStrings) {
 
 				@Override
 				public View getView(int position, View convertView,
 				                    ViewGroup parent) {
 
 					// Must always return just a View.
 					View rowView = super.getView(position, convertView,
 							parent);
 
 					CodecProfileStrings thisProfile = codecProfileStrings.get(position);
 
 					if (rowView != null) {
 						TextView profile = (TextView) rowView.findViewById(R.id.profileName);
 						TextView level = (TextView) rowView.findViewById(R.id.levelName);
 
 						if (!thisProfile.getProfileName().startsWith("0x") || profileVerbosity.get(position))
 							profile.setText(thisProfile.getProfileName());
 						else
 							profile.setText(R.string.undefined);
 
 						if (!thisProfile.getLevelName().startsWith("0x")
 								|| profileVerbosity.get(position)) {
 							level.setText(getResources().getString(R.string.level) + thisProfile.getLevelName());
 							rowView.findViewById(R.id.tapPrompt).setVisibility(View.INVISIBLE);
 						} else {
 							level.setText(getResources().getString(R.string.level) + getResources().getString(R.string.undefined));
 							rowView.findViewById(R.id.tapPrompt).setVisibility(View.VISIBLE);
 						}
 
 					}
 					return rowView;
 				}
 			});
 
 		}
 		return rootView;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 	                        long id) {
 		toggleVerbosity(position);
 
 		((ArrayAdapter<CodecProfileLevel>) myListView.getAdapter())
 				.notifyDataSetChanged();
 
 	}
 
 
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Options..");
 		menu.add(ContextMenu.NONE, ((AdapterContextMenuInfo) menuInfo).position, 0, getString(R.string.copy) + getString(R.string.copy_profile_name));
 		menu.add(ContextMenu.NONE, ((AdapterContextMenuInfo) menuInfo).position, 1, getString(R.string.copy) + getString(R.string.copy_profile_level));
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		if (item.getOrder() == 0) {
 			Context myContext = this.getActivity().getBaseContext();
 			ClipboardManager clipboard = (ClipboardManager) myContext.getSystemService(Context.CLIPBOARD_SERVICE);
 			CodecProfileLevelTranslator profileTranslator = CodecProfileLevelTranslator.getInstance();
 			String translatedProfile = profileTranslator.getProfile(profileLevels[item.getItemId()].profile);
 			ClipData clip = ClipData.newPlainText("label", translatedProfile);
 			clipboard.setPrimaryClip(clip);
 			Toast.makeText(myContext, getString(R.string.copy_profile_name) + getString(R.string.copied), Toast.LENGTH_SHORT).show();
 			return true;
 		} else if (item.getOrder() == 1) {
 			Context myContext = this.getActivity().getBaseContext();
 			ClipboardManager clipboard = (ClipboardManager) myContext.getSystemService(Context.CLIPBOARD_SERVICE);
 			CodecProfileLevelTranslator profileTranslator = CodecProfileLevelTranslator.getInstance();
 			String translatedProfile = profileTranslator.getLevel(profileLevels[item.getItemId()].level);
 			ClipData clip = ClipData.newPlainText("label", translatedProfile);
 			clipboard.setPrimaryClip(clip);
 			Toast.makeText(myContext, getString(R.string.copy_profile_level) + getString(R.string.copied), Toast.LENGTH_SHORT).show();
 			return true;
 		} else
 
 			return super.onContextItemSelected(item);
 	}
 
 	private void toggleVerbosity(int position) {
 
 		if (profileVerbosity.get(position))
 			profileVerbosity.set(position, Boolean.FALSE);
 		else
 			profileVerbosity.set(position, Boolean.TRUE);
 	}
 }
