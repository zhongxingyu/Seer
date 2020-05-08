/**--------------------------------------------------------------------
 
 Copyright Jonathan Cozzo and Patrick Rannou (22/03/2013)
 
 This software is an Android application whose purpose is to select 
 and characterize zones on a photography (type, material, color...).
 
 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 
 
 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 
 
 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 
 
 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.
 
-----------------------------------------------------------------------*/
 
 package com.ecn.urbapp.dialogs;
 
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.activities.MainActivity;
 import com.ecn.urbapp.fragments.ZoneFragment;
 
/**
  * This class creates the dialog that indicate which pixelgeoms is not defined
  * 
  * @author Jules Party
  * 
 */
 public class AddZoneDialogFragment extends DialogFragment {
 
	/**
 	 * The Dialog instance that allows the user to choose how to insert new PixelGeoms.
	 */
 	private Dialog box;
	/** 
 	 * Button to add the zone by intersecting it with older ones
	 */
 	private Button addIntersect = null;
	/**
 	 * Button to add the zone directly without intersecting it with older ones
	 */
 	private Button addDirect = null;
 	
 	private static ZoneFragment zoneFrag;
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		box = new Dialog(getActivity());
 		box.setContentView(R.layout.layout_zone_dialog_create);
 		box.setTitle(R.string.zone_create_dialog);
 		addIntersect = (Button) box.findViewById(R.id.zone_button_create_intersect);
 		addDirect = (Button) box.findViewById(R.id.zone_button_create_add);
 		addIntersect.setOnClickListener(intersectAdd);
 		addDirect.setOnClickListener(directAdd);
 		zoneFrag = MainActivity.getZoneFragment();
 		return box;
 	}
 
 	private OnClickListener intersectAdd = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			zoneFrag.addZone(true);
 			box.dismiss();
 		}
 	};
 
 	private OnClickListener directAdd = new OnClickListener() {
 		
 		@Override
 		public void onClick(View v) {
 			zoneFrag.addZone(false);
 			box.dismiss();
 		}
 	};
 
 	@Override
 	public void onCancel(DialogInterface dialog) {
 	}
}
