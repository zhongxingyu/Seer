 package com.ese2013.mub;
 
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.Toast;
 
 import com.ese2013.mub.model.Mensa;
 
 public class FavoriteButtonListener implements OnClickListener {
 	private Mensa mensa;
 	private ImageButton button;
 	private DailyPlanFragment frag;
 	private boolean isInDailyView;
 
 	public FavoriteButtonListener(Mensa mensa, ImageButton button) {
 		this.mensa = mensa;
 		this.button = button;
 	}
 
 	public FavoriteButtonListener(Mensa mensa, ImageButton button, DailyPlanFragment frag) {
 		this.mensa = mensa;
 		this.button = button;
 		this.frag = frag;
 		this.isInDailyView = true;
 	}
 
 	@Override
 	public void onClick(View viewIn) {
 		mensa.setIsFavorite(!mensa.isFavorite());
		
 		if (!mensa.isFavorite()) {
 			button.setImageResource(R.drawable.ic_fav_grey);
 			if(isInDailyView && !HomeFragment.getShowAllByDay())
 				frag.refreshFavoriteView();
 		} else{
 			button.setImageResource(R.drawable.ic_fav);
 		}
 	}	
 }
