 package com.crayonio.mediacodecquery;
 
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.media.MediaCodecInfo.CodecCapabilities;
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
 
public class ColorFragment extends Fragment implements
 		OnItemClickListener {
 
 	/**
 	 * The fragment argument representing the section number for this
 	 * fragment.
 	 */
 	private int[] colorFormats;
 	private int cachedCodecIndex = -1;
 
 	private ArrayList<Boolean> colorVerbosity;
 
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
 				colorFormats = capabilities.colorFormats;
 				cachedCodecIndex = codecIndex;
 
 				Boolean[] array = new Boolean[colorFormats.length];
 				Arrays.fill(array, Boolean.FALSE);
 				colorVerbosity = new ArrayList<Boolean>(Arrays.asList(array));
 			}
 
 			CodecColorFormatTranslator colorTranslator = CodecColorFormatTranslator
 					.getInstance();
 
 			final ArrayList<String> colorFormatList = new ArrayList<String>();
 			for (int colorFormat : colorFormats) {
 				String translatedProfile = colorTranslator
 						.getColorFormat(colorFormat);
 				if (translatedProfile != null)
 					colorFormatList.add(translatedProfile);
 				else
 					colorFormatList.add("0x" + Integer.toHexString(colorFormat));
 			}
 
 			myListView.setAdapter(new ArrayAdapter<String>(getActivity()
 					.getApplicationContext(), R.layout.codec_color_row,
 					R.id.colorName, colorFormatList) {
 
 				public View getView(int position, View convertView,
 				                    ViewGroup parent) {
 
 					// Must always return just a View.
 					View rowView = super.getView(position, convertView,
 							parent);
 
 					if (rowView != null) {
 						String thisColorFormat = colorFormatList.get(position);
 
 						TextView colorField;
 
 						colorField = (TextView) rowView
 								.findViewById(R.id.colorName);
 
 						if (!thisColorFormat.startsWith("0x")
 								|| colorVerbosity.get(position)) {
 							colorField.setText(thisColorFormat);
 							rowView.findViewById(R.id.tapPrompt).setVisibility(View.INVISIBLE);
 						} else {
 							colorField.setText(R.string.undefined);
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
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 		toggleVerbosity(position);
 		((ArrayAdapter<String>) myListView.getAdapter()).notifyDataSetChanged();
 	}
 
 
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle("Options..");
 
 		menu.add(ContextMenu.NONE, ((AdapterContextMenuInfo) menuInfo).position, 0, getString(R.string.copy) + getString(R.string.copy_color_format));
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		if (item.getOrder() == 0) {
 			Context myContext = this.getActivity().getBaseContext();
 			ClipboardManager clipboard = (ClipboardManager) myContext.getSystemService(Context.CLIPBOARD_SERVICE);
 
 			CodecColorFormatTranslator colorTranslator = CodecColorFormatTranslator.getInstance();
 			String translatedColor = colorTranslator.getColorFormat(colorFormats[item.getItemId()]);
 			ClipData clip = ClipData.newPlainText("label", translatedColor);
 			clipboard.setPrimaryClip(clip);
 			Toast.makeText(myContext, getString(R.string.copy_color_format) + getString(R.string.copied), Toast.LENGTH_SHORT).show();
 			return true;
 		} else
 			return super.onContextItemSelected(item);
 	}
 
 	private void toggleVerbosity(int position) {
 		if (colorVerbosity.get(position))
 			colorVerbosity.set(position, Boolean.FALSE);
 		else
 			colorVerbosity.set(position, Boolean.TRUE);
 	}
 }
