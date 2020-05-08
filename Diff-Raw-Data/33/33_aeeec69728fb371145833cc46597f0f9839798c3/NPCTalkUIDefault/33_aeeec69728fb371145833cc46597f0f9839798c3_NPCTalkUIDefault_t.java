 package edu.bonn.mobilegaming.geoquest.ui;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.qeevee.ui.ZoomImageView;

 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.mission.NPCTalk;
 
 public class NPCTalkUIDefault extends NPCTalkUI {
 
     private ZoomImageView charImage;
    private Button proceedButton;
    private TextView dialogText;
    private ScrollView scrollView;
 
     public NPCTalkUIDefault(NPCTalk activity) {
 	super(activity);
     }
 
     @Override
     View createView() {
	LayoutInflater inflater = (LayoutInflater) activity
 		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 	view = inflater.inflate(R.layout.npctalk,
 				null);
 	charImage = (ZoomImageView) view.findViewById(R.id.npcimage);
	return view;
     }
 
    /**
     * @param pathToImageFile
     *            the relative path to the bitmap file that should be set as
     *            image or null if no image should be shown.
     * 
     * @see edu.bonn.mobilegaming.geoquest.ui.NPCTalkUI#setImage(java.lang.String)
     */
     @Override
     public boolean setImage(String pathToImageFile) {
	if (pathToImageFile == null) {
	    charImage.setVisibility(View.GONE);
	    return false;
	}
 	try {
 	    charImage.setRelativePathToImageBitmap(pathToImageFile);
 	    return true;
 	} catch (IllegalArgumentException iae) {
 	    charImage.setVisibility(View.GONE);
 	    return false;
 	}
     }
 
 }
