 package de.thm.ateam.memory;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import de.thm.ateam.memory.game.PlayerList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import de.thm.ateam.memory.game.Card;
 
 /**
  * 
  * BaseAdapter to handle the gridview with the bitmaps of a theme
  *
  */
 public class ImageAdapter extends BaseAdapter {
 	private Context mContext;
 	// Contains the pictures
 	private List<ImageView> listImageView;
 
 	private Theme theme;
 
 	/**
 	 * 
 	 * Constructor which initialize the imageViews with the theme
 	 * 
 	 * @param Context c
 	 * @param int row
 	 * @param int col
 	 * 
 	 */
 	public ImageAdapter(Context c, int row, int col) {
 		mContext = c;
 		listImageView = new ArrayList<ImageView>();
 
 		// Create Theme with the deckNum which is saved in the config file
 		theme = new Theme(c, PlayerList.getInstance().deckNum);
 		
 		// Width of the Grifview that it fit on landscape and the other mode
 		int size = (int) (maxSize() / col)-4;
 
 		// We need row * col / 2 pictures
 		for (int i = 0; i < row*col/2; i++) {
 			ImageView imageView;
 
 			// From every picture we need 2
 			for (int j = 0; j < 2; j++) {
 				imageView = new ImageView(mContext);
 				imageView.setLayoutParams(new GridView.LayoutParams(size, size));
 				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 				imageView.setPadding(2, 2, 2, 2);
 				imageView.setMaxHeight(size);
 				imageView.setMaxWidth(size);
 				imageView.setId(i);
 
 				imageView.setImageBitmap(theme.getBackSide());
 
 				listImageView.add(imageView);
 			}
 		}
 	
 	}
 	
 	public void shuffleImages(){
 	  Collections.shuffle(listImageView);
 	}
 	
 	
	public void buildField(String fieldString, int size){
 	  ImageView[] imageViewNew = new ImageView[size];
	  int colsize = PlayerList.getInstance().col;
 	  String[] pairs = fieldString.split("Ende");
 	  for(int i = 0; i < pairs.length; i++){
 	    String pair[] = pairs[i].split(";");
 	    for(int j = 0; j < pair.length; j++){
 	      String values[] = pair[j].split(",");
 	      imageViewNew[Integer.parseInt(values[1])* colsize + Integer.parseInt(values[0])] = listImageView.get(j);
 	    }
 	    //jetzt die ersten zwei Element aus imageView entfernen, damit die Schleife weiter funktioniert
 	    listImageView.remove(0);
 	    listImageView.remove(0);
 	  }
 	  listImageView.clear();
 	  listImageView = Arrays.asList(imageViewNew);
 	}
 	
 	
	public Card[][] getPositions(){
	  int colsize = PlayerList.getInstance().col;
 	  Card[][] cards = new Card[listImageView.size()/2][2];
 	  for(int j = 0; j < listImageView.size(); j++){
 	    for(int i = 0; i < listImageView.size(); i++){
 	      if(cards[i][0] == null){
 	        cards[i][0] = new Card(listImageView.get(j).getId(), j%colsize, j/colsize);
 	        break;
 	      }
 	      if(cards[i][0] != null && cards[i][0].id == listImageView.get(j).getId()){
 	        cards[i][1] = new Card(listImageView.get(j).getId(), j%colsize, j/colsize);
 	        break;
 	      }
 	    }
 	  }
 	  return cards;
 	}
 
 	/**
 	 * 
 	 * Function which returns the amount of the imageViews
 	 * 
 	 * @return int - count
 	 * 
 	 */
 	public int getCount() {
 		return listImageView.size();
 	}
 
 	/**
 	 * 
 	 * Function which return the imageView at position
 	 * When position invalid then the function will return null
 	 * 
 	 * @param int position
 	 * @return Object - ImageView
 	 * 
 	 */
 	public ImageView getItem(int position) {
 		if(position < listImageView.size())
 			return listImageView.get(position);
 		return null;
 	}
 
 
 	/**
 	 * 
 	 * Function which returns the theme
 	 * 
 	 * @return Theme
 	 * 
 	 */
 	public Theme getTheme() {
 		return theme;
 	}
 
 	/**
 	 * 
 	 * Function which returns the image id of the imageView at position
 	 * 
 	 * @return long - imageId
 	 * @param int position
 	 * 
 	 */
 	public long getItemId(int position) {
 		return listImageView.get(position).getId();
 	}
 
 	/**
 	 * 
 	 * Function for the the gridView to be handled as an Adapter
 	 * 
 	 * @return View - ImageView
 	 * @param int position
 	 * @param View convertView
 	 * @param ViewGroup parent
 	 * 
 	 */
 	public View getView(int position, View convertView, ViewGroup parent) {
 		return listImageView.get(position);
 	}
 
 	/**
 	 * 
 	 * Returns the the pixel size of the little display size
 	 * 
 	 * @return int - max size we should use for width
 	 * 
 	 */
 	public int maxSize() {
 		return Math.min(((Activity) mContext).getWindow().getWindowManager()
 		    .getDefaultDisplay().getHeight(), ((Activity) mContext).getWindow()
 		    .getWindowManager().getDefaultDisplay().getWidth());
 	}
 	
 	@Override
 	public boolean areAllItemsEnabled() {
 		return false;
 	}
 	
 	@Override
 	public boolean isEnabled(int position) {
 		return listImageView.get(position).isEnabled();
 	}
 	
 }
