 package dk.aau.cs.giraf.train.opengl;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.DragEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import dk.aau.cs.giraf.train.R;
 /***
  * An abstract class that contains methods for StationLinearLayout and WagonLinearLayout.
  * @author Jacob
  *
  */
 public abstract class GameActivityLinearLayout extends LinearLayout {
 	public GameActivityLinearLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	@SuppressWarnings("deprecation")
     public void addPictoFrames(int numberOfPictoFrames){
 		Drawable normalShape = getResources().getDrawable(R.drawable.shape);
 		int height = 300/(numberOfPictoFrames/2);
 		for (int j = 0; j < (numberOfPictoFrames / 2); j++) {
 			LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(0,height,1.0f);
 			PictoFrameLayout pictoFrameLayout = new PictoFrameLayout(this.getContext());
 			pictoFrameLayout.setLayoutParams(linearLayoutParams);
 			pictoFrameLayout.setBackgroundDrawable(normalShape);
 			pictoFrameLayout.setOnDragListener(new DragListener());
 			
 			this.addView(pictoFrameLayout);
 		}
 	}
 	
 	/**
 	 * A drag listner implementing an onDrag() method that runs when something
 	 * is dragged to it.
 	 */
 	private class DragListener implements OnDragListener {
 		private Drawable enterShape;
 		private Drawable normalShape;
 
 		public DragListener() {
 			Resources resources = getResources();
 
 			this.enterShape = resources.getDrawable(R.drawable.shape_droptarget);
 			this.normalShape = resources.getDrawable(R.drawable.shape);
 		}
 
 		@SuppressWarnings("deprecation")
         @Override
 		public boolean onDrag(View hoverView, DragEvent event) {
				View draggedView = (View) event.getLocalState();
 				
 				switch (event.getAction()) {
 					case DragEvent.ACTION_DRAG_STARTED:
						// makes the draggedview invisible in ownerContainer
 						break;
 	
 					case DragEvent.ACTION_DRAG_ENTERED:
 						// Change the background of droplayout(purely style)
 						hoverView.setBackgroundDrawable(enterShape);
 						hoverView.invalidate();
 						break;
 	
 					case DragEvent.ACTION_DRAG_EXITED:
 						// Change the background back when exiting droplayout(purely
 						// style)
 						hoverView.setBackgroundDrawable(normalShape);
 						hoverView.invalidate();
 						break;
 	
 					case DragEvent.ACTION_DROP:
 						// Dropped, assigns the draggedview to the dropcontainer if
 						// the container does not already contain a view.
 						ViewGroup ownerContainer = (ViewGroup) draggedView.getParent();
 	
 						PictoFrameLayout dropContainer = (PictoFrameLayout) hoverView;
 						Object tag = hoverView.getTag();
 						if (tag == null) {
 							ownerContainer.removeView(draggedView);
 							ownerContainer.setTag(null);
 							ownerContainer.invalidate();
 							dropContainer.addView(draggedView);
 							dropContainer.setTag("filled");
 							dropContainer.invalidate();
 						}
 						draggedView.setVisibility(View.VISIBLE);
 						draggedView.invalidate();
 						break;
 	
 					case DragEvent.ACTION_DRAG_ENDED:
 						// Makes the draggedview visible again after the view has
 						// been moved or the drop wasn't valid.
 						hoverView.setBackgroundDrawable(normalShape);
 						hoverView.invalidate();
 						if(event.getResult() == false){
 							draggedView.setVisibility(View.VISIBLE);
 							draggedView.invalidate();
 						}
 						break;
 				}
 				return true;
 		}
 	}
 }
