 package edu.unlp.fcnym.guiame.ui;
 
 import edu.unlp.fcnym.guiame.R;
 import edu.unlp.fcnym.guiame.R.drawable;
 import edu.unlp.fcnym.guiame.core.GuiaMe;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageSwitcher;
 import android.widget.TextView;
 import android.widget.ViewSwitcher.ViewFactory;
 import android.widget.ImageView;
 import android.app.ActionBar.LayoutParams;
 
 public class ImagesFragment extends Fragment{
 	
 	private ImageButton nextButton;
 	private ImageButton previousButton;
 	private ImageSwitcher imageSwitcher;
 	private TextView exhibitionTitle;
 	private TextView resourceDescription;
 
 	
 	int imageResources[] = { 
 			   drawable.paleontologia_1,
 			   drawable.paleontologia_2,
 			   drawable.paleontologia_3
 			  };
 	
 	String imageDescription[] ={
 			
 			"Glyptodon",
 			"Exaeretodon",
 			"Iguanodon"
 			
 	};
 	
 	int currentIndex=0;
 	
 	
 	@Override
 	
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		
 		   final View view = inflater.inflate(R.layout.images, container, false);
 			
 		   exhibitionTitle = (TextView) view.findViewById(R.id.textView1);
 		   resourceDescription = (TextView) view.findViewById(R.id.textView2);
 		   
 		   
 		   
 			previousButton = (ImageButton) view.findViewById(R.id.imageButton1);
 			nextButton = (ImageButton) view.findViewById(R.id.imageButton2);
 			
 			imageSwitcher = (ImageSwitcher) view.findViewById(R.id.imageSwitcher1);
 			
 			previousButton.setVisibility(View.INVISIBLE); //back button invisible when showing first picture
 	
 			imageSwitcher.setFactory(new ViewFactory() {
 
 				   @Override
 				   public View makeView() {
 					   ImageView myView = new ImageView(getActivity().getApplicationContext());
					  myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
 					// myView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
					 myView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
 			
 					   return myView;
 				       }
 
 				   });
 			
 			imageSwitcher.setImageResource(imageResources[currentIndex]);
 			resourceDescription.setText(imageDescription[currentIndex]);
    
 			
 			nextButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View _view) {
 					
 					_view.startAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.image_click));	
 						
 					  Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.slide_in_right);
 				      Animation out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),R.anim.slide_out_left);  
 				     
 				      
 				      
 				      imageSwitcher.setOutAnimation(out);
 				      imageSwitcher.setInAnimation(in);
 					
 					
 				      
 				      
 				      if (currentIndex < imageResources.length - 1) {
 				    	     imageSwitcher.setImageResource(imageResources[++currentIndex]);
 				    	     resourceDescription.setText(imageDescription[currentIndex]);
 				    	     if(currentIndex==imageResources.length - 1)
 				    	    	 nextButton.setVisibility(View.INVISIBLE);
 				    	     if(currentIndex>0)
 				    	    	 previousButton.setVisibility(View.VISIBLE);
 				    	     
 				      }	   
 				
 				}
 			});
 			
 			previousButton.setOnClickListener(new OnClickListener() {
 				public void onClick(View _view) {
 					
 					_view.startAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.image_click));
 					   
 					  Animation in = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),android.R.anim.slide_in_left);
 				      Animation out = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),android.R.anim.slide_out_right);
 				      
 				      imageSwitcher.setInAnimation(in);
 				      imageSwitcher.setOutAnimation(out);
 				      
 				       if (currentIndex > 0) {
 				    	     imageSwitcher.setImageResource(imageResources[--currentIndex]);
 				    	     resourceDescription.setText(imageDescription[currentIndex]);
 				    	     if(currentIndex==0)
 				    	    	 previousButton.setVisibility(View.INVISIBLE);
 				    	     if(currentIndex<imageResources.length - 1)
 				    	    	 nextButton.setVisibility(View.VISIBLE);
 				    	    	 
 				       }
 				       
 				      
 				}
 			});
 			
 			
 			
 		return view;
     }
 	
 	
 	
 
 }
