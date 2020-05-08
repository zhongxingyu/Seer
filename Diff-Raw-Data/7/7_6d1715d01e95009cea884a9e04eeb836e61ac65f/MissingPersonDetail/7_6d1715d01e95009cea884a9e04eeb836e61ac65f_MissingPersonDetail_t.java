 package com.shepherd;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Fragment;
import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 /**
  * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
  * contain this fragment must implement the
  * {@link MissingPersonDetail.OnFragmentInteractionListener} interface to handle
  * interaction events. Use the {@link MissingPersonDetail#newInstance} factory
  * method to create an instance of this fragment.
  * 
  */
 public class MissingPersonDetail extends Fragment implements OnClickListener {
 
 	private static final String PERSON_DETAIL_FIRST_NAME = "first_name";
 	private static final String PERSON_DETAIL_MIDDLE_NAME = "middle_name";
 	private static final String PERSON_DETAIL_LAST_NAME = "last_name";
 
 	/**
 	 * Use this factory method to create a new instance of this fragment using
 	 * the provided parameters.
 	 * 
 	 * @param personDetail
 	 *            the detail info to be displayed.
 	 * @return A new instance of fragment MissingPersonDetail.
 	 */
 	public static MissingPersonDetail newInstance(JSONObject personDetail) {
 		MissingPersonDetail fragment = new MissingPersonDetail();
 		Bundle args = new Bundle();
 		try {
 			args.putString(PERSON_DETAIL_FIRST_NAME,
 					personDetail.getString(PERSON_DETAIL_FIRST_NAME));
 			args.putString(PERSON_DETAIL_MIDDLE_NAME,
 					personDetail.getString(PERSON_DETAIL_MIDDLE_NAME));
 			args.putString(PERSON_DETAIL_LAST_NAME,
 					personDetail.getString(PERSON_DETAIL_LAST_NAME));
 		} catch (JSONException e) {
 			throw new IllegalArgumentException(e);
 		}
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	public MissingPersonDetail() {
 		// Required empty public constructor
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 		View view = inflater.inflate(R.layout.fragment_missing_person_detail,
 				container, false);
 		if (getArguments() != null) {
 			((TextView)(view.findViewById(R.id.personName))).setText(
 					String.format("%s %s %s",
 							getArguments().getString(PERSON_DETAIL_FIRST_NAME),
 							getArguments().getString(PERSON_DETAIL_MIDDLE_NAME),
 							getArguments().getString(PERSON_DETAIL_LAST_NAME)
 							));
 		}
 		view.findViewById(R.id.markFound).setOnClickListener(this);
 		return view;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 	}
 
 	// Mark Found.
 	@Override
 	public void onClick(View v) {
 		getActivity().onBackPressed();
 	}
 
 }
