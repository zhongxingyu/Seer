 package arranger.arrengerPkg;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // Class extending the Activity Class from Android
 public class ArrengeNumber extends Activity {
 	
 	// Array List size
 	private Integer elementsLength = 9;
 	
 	// button Array that is the elements to arrange
 	private ArrayList<TextView> elements= new ArrayList<TextView>();
 	// GridView where buttons are going to be draw
 	private GridView gridview;
 		 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate( savedInstanceState );
         // Retrieve the layout file
         setContentView(R.layout.easy);
         // instance of class MyOnClickListener that handle the events
         MyOnClickListener cl = new MyOnClickListener();
         
         // Construct the button elements
         for( Integer i=0; i<elementsLength ;i++ ){
     		Button newButton = new Button(this);
         	// elements[i] = new Button(this);
     		newButton.setLayoutParams(new GridView.LayoutParams(150, 150));
     		newButton.setPadding(8, 8, 8, 8);
     		newButton.setText( ((Integer)(i+1)).toString() );
     		newButton.setTextSize(50); 
     		newButton.setId(i+1);
     		newButton.setOnClickListener( cl );
     		newButton.setEnabled( true );
     		// The last button is not visible because it is the switch button
      	   	if( i == elementsLength-1 )
      	   		newButton.setVisibility(4);
      	   	
      	   	elements.add( newButton );
         }
         
         // shuffle the elements
         shuffle();
         // Find the GridView element on Layout
         gridview = (GridView) findViewById(R.id.gridview);
         // Set the generic adapter that control the drawing of buttons
         gridview.setAdapter(new ButtonGridAdapter());
         
     }
     /*
      * Function that handles the decision of switching elements when clicked
      * @author : Gustavo Pantuza
      * @since : 28.08.2011
      * @param v : Object of View (button) clicked
      */
     private void arrange( View v ){

     	Integer vIndex = indexOf( v );
     	if( vIndex > -1 ){
     		// Treat the begin of the elements
     		if( vIndex-1 < 0 || vIndex-3 < 0 ){
     			testHeaderList( vIndex );
     		// Treat the end of the elements
     		} else if( vIndex+1 > 8 || vIndex+3 > 8 ){
     			testTailList( vIndex );
     		// Treat the meddle elements
     		} else{
     			testHeaderList( vIndex );
     			testTailList( vIndex );
     		}
     	// Show error
     	} else{
     		Toast.makeText(ArrengeNumber.this, "sory, we got an error!",
     				Toast.LENGTH_SHORT).show();
     	}
     	// If elements are sorted, ends the game
     	if( isSorted() )
     		Toast.makeText(ArrengeNumber.this, "Congratulation o/",
    				Toast.LENGTH_SHORT).show();
     }
     /*
      * Verify if the previous elements could be switched with the current 
      * element.
      * @author : Gustavo Pantuza
      * @since : 28.08.2011
      * @param vIndex : Index of the clicked button on elements  
      */
     private void testHeaderList( Integer vIndex ){
     	
		if( elements.get( vIndex+1 ).getId() == 9 ){
			if( vIndex != 2 && vIndex != 5 )
				switchPosition( vIndex, vIndex+1 );
        	
    	} else if( elements.get( vIndex+3 ).getId() == 9 ){
     		switchPosition( vIndex, vIndex+3 );
     	
    	} else if(vIndex!=0 ){
    		if( elements.get(vIndex-1 ).getId() == 9 && vIndex != 3 )
    			switchPosition( vIndex, vIndex-1 );
     	}
     }
     /*
      * Verify if the next elements could be switched with the current 
      * element.
      * @author : Gustavo Pantuza
      * @since : 28.08.2011
      * @param vIndex : Index of the clicked button on elements  
      */
     private void testTailList( Integer vIndex ){

     	if( elements.get( vIndex-1 ).getId() == 9 ){
    		if( vIndex != 6 && vIndex != 3 )	
    			switchPosition( vIndex, vIndex-1 );
     		
     	}else if( elements.get( vIndex-3 ).getId() == 9 ){
     		switchPosition( vIndex, vIndex-3 );
     	
    	}else if( vIndex != elementsLength-1 ){
    		if( elements.get( vIndex+1 ).getId() == 9 && vIndex != 5 )
    			switchPosition( vIndex, vIndex+1 );
     	}
     }
     /*
      * Find an element (button) position on elements array
      * @author : Gustavo Pantuza
      * @since : 30.08.2011
      * @param v : View object to search 
      * @return i : Integer value representing the index on array
      */
     private Integer indexOf( View v ){
     	for( int i=0; i<elementsLength; i++ )
     		if( elements.get( i ) == v )
     			return i;
     	return -1;
     }
     /*
      * Verify if the array is sorted
      * @author : Gustavo Pantuza
      * @since : 30.08.2011
      * @return : true if the array is sorted. Otherwise, false.
      */
     private Boolean isSorted( ){
     	
     	for( int i=0; i < elementsLength - 1; i++ ){
     		
     		if( elements.get( i ).getId() < elements.get( i+1 ).getId() )
     			continue;
     		else
     			return false;
     	}
     	return true;
     }
     /*
      * Shuffle the elements to start/Restart the game
      * @author : Gustavo Pantuza
      * @since : 31.08.2011
      */
     private void shuffle(){
     	Collections.shuffle( elements );
     }
     /*
      * Switch positions of two elements from the array
      * @author : Gustavo Pantuza
      * @since : 31.08.2011
      * @param currentPos: Index of the current clicked button
      * @param toChangePos : Index of the element to switch position 
      */
     private void switchPosition( Integer currentPos, Integer toChangePos){
 		
     	TextView tmp = elements.get( toChangePos );
 		elements.set( toChangePos, elements.get( currentPos ) );
 		elements.set( currentPos, tmp );
 		// Refresh the game view
 		gridview.invalidateViews();
     }
     /*
      * Click event handler class for the buttons in the array 'elements'
      * @author : Gustavo Pantuza
      * @since : 27.08.2011
      */
 	class MyOnClickListener implements OnClickListener {
   		// On Click function
 		public void onClick(View v) {
 			// Call to the arrange function that try to switch positions
 			arrange( v );
 		}
 	}
 	// Generic class to handle the contruction of the buttons grid view
 	public class ButtonGridAdapter extends BaseAdapter {
    	 
     	 // Total number of things contained within the adapter
     	 public int getCount() {
     		 return elementsLength;
     	 }
 
     	  // Require for structure, not used on code
     	 public Object getItem(int position) {
     	  return null;
     	 }
 
     	 public long getItemId(int position) {
     	  return position;
     	 }
     	 
     	 public boolean areAllItemsEnabled(){
     		 return false;
     	 }
     	 
     	 public int getItemViewType( int pos ){
    			 return 0;
     	 }
     	 
     	 public int getViewTypeCount(){
     		 return 1;
     	 }
     	 
     	 public boolean isEnabled( int pos ){
     		 return pos!=5;
     	 }
     	 // make the view based on the adapter
     	 public View getView(int position, 
     	                           View convertView, ViewGroup parent) {
     		 return elements.get( position );
     	 }
 
     }
 	
 	class ArrayListMatrix extends ArrayList<TextView>{
 		
 		public ArrayListMatrix(int lines, int colums){
 			
			super();
 		}
 		
 	}
 }
