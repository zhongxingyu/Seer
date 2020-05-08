 /*
 FragmentList Class for CreateYourOwnAdventure App.
 The view for the ReadFragmentActivity. Constructs the required view
 to display the media of the story fragment properly.
     
     License GPLv3: GNU GPL Version 3
     <http://gnu.org/licenses/gpl.html>.
     
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cmput301.f13t01.createyourownadventure;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.TextView.BufferType;
 
 /**
  * @author Eddie Tai <eddie@ualberta.ca>
  * 
  *         The view for the ReadFragmentActivity. Constructs the required
  *         fragment to display the media of the story fragment properly. Uses
  *         the story manager to access parts of the story for display.
  * 
  */
 public class ReadFragmentView extends Fragment {
 
 	ReadStoryManager storyManager;
 
 	// get the storyManager on attachment to the activity
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		GlobalManager app = (GlobalManager) activity.getApplication();
 		ReadStoryManager storyManager = app.getStoryManager();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		// Inflate the view
 		View scrollable = inflater.inflate(R.layout.view_fragment, container,
 				false);
 
 		// Setup fragment's outer container-layout
 		LinearLayout layout = (LinearLayout) scrollable
 				.findViewById(R.id.view_fragment_linear);
 		// LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
 		// LinearLayout.LayoutParams.MATCH_PARENT,
 		// LinearLayout.LayoutParams.WRAP_CONTENT);
 		
 		// fetch story fragment Id
 		String resourceString = getResources().getString(R.string.destination_id);
		Integer fragmentId = getArguments().getInt(resourceString);
 		
 		ArrayList<Media> mediaList = storyManager.getMediaList(fragmentId);
 
 		// cycle through the media list
 		for (int i = 0; i < mediaList.size(); i++) {
 			@SuppressWarnings("rawtypes")
 			Media media = mediaList.get(i);
 
 			// get media file's type and do class comparisons
 			
 			// for Text type of media
 			if (media.getClass().equals(Text.class)) {
 				Text text = (Text) media;
 
 				// get media content's SpannableString as s
 				SpannableString s = text.getContent();
 				TextView tv = new TextView(getActivity());
 				tv.setText("s", BufferType.SPANNABLE);
 				layout.addView(tv);
 			}
 
 			// the rest are implemented later for iteration 3/4
 
 			// for Image type of media
 			if (media.getClass().equals(Image.class)) {
 				// TO-DO
 			}
 
 			// for Sound type of media
 			if (media.getClass().equals(Sound.class)) {
 				// TO-DO
 			}
 
 			// for Video type of media
 			if (media.getClass().equals(Video.class)) {
 				// TO-DO
 			}
 		}
 
 		// from story level with fragment id, get the array list of choice
 		// objects
 		ArrayList<Choice> choices = storyManager.getChoices(fragmentId);
 
 		// if there are choices, cycle through them and extract the flavour
 		// texts
 		if (choices != null) {
 			ArrayList<String> flavourText = new ArrayList<String>();
 
 			for (int i = 0; i < choices.size(); i++) {
 				String s = choices.get(i).getFlavourText();
 				flavourText.add(s);
 			}
 			
 			// use for loop to make series of buttons consisting of the
 			// flavour texts
 
 			for (Integer i = 0; i < flavourText.size(); i++) {
 				Button choiceButton = new Button(getActivity());
 				choiceButton.setText(flavourText.get(i));
 				choiceButton.setId(i+1);
 				choiceButton.setTextSize(12);
 				layout.addView(choiceButton);
 				
 				// set each button's controller to use Activity's function
 				choiceButton.setOnClickListener(new View.OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						((ReadFragmentActivity)getActivity()).onFragmentListClick(v, fragmentId);
 					}
 				});
 			}
 		}
 
 		return scrollable;
 	}
 }
