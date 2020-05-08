 package vnd.blueararat.smssieve;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Fragment1 extends Fragment {
 
 	public Fragment1() {
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment1, container, false);
 		// CheckBox cb = (CheckBox) rootView.findViewById(R.id.checkBox1);
 		final int[] colors = {
 				getActivity().getResources().getColor(R.color.disabled),
 				getActivity().getResources().getColor(R.color.enabled) };
 		final int[] states = { R.string.disabled, R.string.enabled };
 
 		TextView enabled = (TextView) rootView.findViewById(R.id.enabled);
 		TextView tv = (TextView) rootView.findViewById(R.id.textView1);
 		TextView tv2 = (TextView) rootView.findViewById(R.id.textView2);
 
 		boolean b = MainActivity.preferences.getBoolean(Receiver.KEY_ENABLED,
 				true);
 		int i = MainActivity.preferences.getInt(Receiver.KEY_SKIPPED, 0);
 		String matches = MainActivity.preferences.getString(
 				Receiver.KEY_MATCHES, "");
 		if (matches.length() > 0) {
 			tv2.setText(getString(R.string.recent) + matches);
 		} else {
 			tv2.setText(R.string.section2_summary);
 		}
 
 		tv.setText(getActivity().getString(R.string.skipped) + i);
 		int ind = b ? 1 : 0;
 		enabled.setBackgroundColor(colors[ind]);
 		enabled.setText(states[ind]);
 		enabled.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				boolean b2 = !MainActivity.preferences.getBoolean(
 						Receiver.KEY_ENABLED, true);
 				Editor et = MainActivity.preferences.edit();
 				et.putBoolean(Receiver.KEY_ENABLED, b2);
 				int id = b2 ? 1 : 0;
 				v.setBackgroundColor(colors[id]);
 				((TextView) v).setText(states[id]);
 				et.commit();
 			}
 		});
 		refresh(rootView);
 		return rootView;
 	}
 
 	public void refresh(View rootView) {
 		if (rootView == null)
 			rootView = getView();
 		final List<String> l2 = new ArrayList<String>();
 		l2.addAll(MainActivity.filters.getAll().keySet());
 		final int part1 = l2.size();
 		Set<String> set = MainActivity.regex_filters.getAll().keySet();
 		for (String exp : set) {
 			l2.add(exp);
 		}
 		TextView t = (TextView) rootView.findViewById(R.id.textView2);
 
 		if (l2.isEmpty()) {
 			t.setText(R.string.section2_empty);
 		}
 		ArrayAdapter<String> a2 = new ArrayAdapter<String>(getActivity(),
 				R.layout.list_item2, l2) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup parent) {
 				View view;
 				TextView text1, text2, description;
 				LayoutInflater inflater = (LayoutInflater) getActivity()
 						.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
 
 				if (convertView == null) {
 					view = inflater.inflate(R.layout.list_item2, parent, false);
 				} else {
 					view = convertView;
 				}
 
 				try {
 					text1 = (TextView) view.findViewById(R.id.text1);
 					text2 = (TextView) view.findViewById(R.id.text2);
 					description = (TextView) view
 							.findViewById(R.id.description);
 				} catch (ClassCastException e) {
 					throw new IllegalStateException(e.toString(), e);
 				}
 				String s = l2.get(position);
 				text1.setText(s);
 				if (position < part1) {
 					description.setVisibility(View.GONE);
 					text2.setText("" + MainActivity.filters.getInt(s, 0));
 				} else {
 					description.setVisibility(View.VISIBLE);
 
 					text2.setText("" + MainActivity.regex_filters.getInt(s, 0));
 				}
 				return view;
 			}
 		};
 		ListView list2 = (ListView) rootView.findViewById(R.id.listView2);
 		list2.setAdapter(a2);
 		list2.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 		list2.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				final CharSequence[] items = { getString(R.string.remove),
 						getString(R.string.edit) };
 				final String key = l2.get(position);
 				final int pos = position;
 
 				new AlertDialog.Builder(getActivity()).setItems(items,
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int which) {
 								switch (which) {
 								case 0:
 									new AlertDialog.Builder(getActivity())
 											// .setIcon(android.R.drawable.ic_dialog_alert)
 											// .setTitle("")
 											.setMessage(
 													getActivity().getString(
 															R.string.remove)
 															+ key + " ?")
 											.setPositiveButton(
 													android.R.string.yes,
 													new DialogInterface.OnClickListener() {
 
 														@Override
 														public void onClick(
 																DialogInterface dialog,
 																int which) {
 															Editor et = null;
 															if (pos < part1) {
 																et = MainActivity.filters
 																		.edit();
 															} else {
 																et = MainActivity.regex_filters
 																		.edit();
 															}
 															et.remove(key);
 															et.commit();
 															Fragment2 f3 = (Fragment2) (((MainActivity) getActivity()).fr[1]);
															f3.refresh();
 															refresh(null);
 														}
 
 													})
 											.setNegativeButton(
 													android.R.string.no, null)
 											.show();
 									break;
 								case 1:
 									final EditText input = new EditText(
 											getActivity());
 									input.setText(key);
 									new AlertDialog.Builder(getActivity())
 											// .setTitle(R.string.edit)
 											.setMessage(R.string.edit)
 											.setView(input)
 											.setPositiveButton(
 													android.R.string.yes,
 													new DialogInterface.OnClickListener() {
 														public void onClick(
 																DialogInterface dialog,
 																int whichButton) {
 															String str = input
 																	.getText()
 																	.toString();
 															if (str.equals(key))
 																return;
 
 															Editor et = null;
 															boolean isRegular = pos >= part1;
 															if (isRegular) {
 																et = MainActivity.regex_filters
 																		.edit();
 															} else {
 																et = MainActivity.filters
 																		.edit();
 															}
 															if (isRegular) {
 																if (!MainActivity.regex_filters
 																		.contains(str)) {
 																	Pattern p = null;
 																	try {
 																		p = Pattern
 																				.compile(str);
 																	} catch (PatternSyntaxException e) {
 																		Toast.makeText(
 																				getActivity(),
 																				getString(R.string.regex)
 																						+ " \""
 																						+ str
 																						+ "\" "
 																						+ getString(R.string.wrong_pattern),
 																				Toast.LENGTH_SHORT)
 																				.show();
 																		return;
 																	}
 																}
 															}
 															et.remove(key);
 															if (str.length() != 0)
 																et.putInt(str,
 																		0);
 															et.commit();
 															Fragment2 f3 = (Fragment2) (((MainActivity) getActivity()).fr[1]);
															f3.refresh();
 															refresh(null);
 														}
 													})
 											.setNegativeButton(
 													android.R.string.no, null)
 											.show();
 									break;
 								}
 							}
 						}).show();
 
 				return true;
 			}
 		});
 	}
 }
