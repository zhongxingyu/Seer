 package edu.bonn.mobilegaming.geoquest.ui;
 
import com.qeevee.ui.ZoomImageView;

 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.mission.NPCTalk;
 
 public class NPCTalkUIDefault extends NPCTalkUI {
 
     private ZoomImageView charImage;
 
     public NPCTalkUIDefault(NPCTalk activity) {
 	super(activity);
     }
 
     @Override
     View createView() {
	LayoutInflater inflater = (LayoutInflater) GeoQuestApp.getContext()
 		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 	view = inflater.inflate(R.layout.npctalk,
 				null);
 	charImage = (ZoomImageView) view.findViewById(R.id.npcimage);
	return view;         
     }
 
     @Override
     public boolean setImage(String pathToImageFile) {
 	try {
 	    charImage.setRelativePathToImageBitmap(pathToImageFile);
 	    return true;
 	} catch (IllegalArgumentException iae) {
 	    charImage.setVisibility(View.GONE);
 	    return false;
 	}
     }
 
 }
