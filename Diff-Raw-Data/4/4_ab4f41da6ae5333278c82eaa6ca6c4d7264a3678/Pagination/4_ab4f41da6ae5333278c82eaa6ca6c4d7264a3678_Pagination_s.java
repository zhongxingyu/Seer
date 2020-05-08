 package fi.mikuz.boarder.util.editor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.util.Log;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.util.Handlers.ToastHandler;
 
 /**
  * Knows current status of pages. Provides pagination functionality.
  * 
  * @author Jan Mikael Lindlf
  */
 public class Pagination {
 	public static final String TAG = Pagination.class.getSimpleName();
 	
 	GraphicalSoundboardProvider gsbp;
 	
 	private boolean movePageMode;
 	private int moveFromPageNumber;
 	private int movePageOrientation;
 	
 	private int pageNumberPortrait;
 	private int pageNumberLandscape;
 	
 	public Pagination(GraphicalSoundboardProvider gsbp) {
 		this.gsbp = gsbp;
 		resetMove();
 		pageNumberPortrait = 0;
 		pageNumberLandscape = 0;
 	}
 	
 	public void initMove(GraphicalSoundboard gsb) {
 		this.movePageMode = true;
 		this.movePageOrientation = gsb.getScreenOrientation();
 		this.moveFromPageNumber = gsb.getPageNumber();
 	}
 	
 	private void resetMove() {
 		this.movePageMode = false;
 		this.moveFromPageNumber = -1;
 		this.movePageOrientation = -1;
 	}
 	
 	public GraphicalSoundboard getBoard(int orientation) {
 		
 		int pageNumber = getPageNumberForOrientation(orientation);
 		GraphicalSoundboard gsb = gsbp.getPage(orientation, pageNumber);
 		if (gsb != null) return gsb;
 		
 		Log.v(TAG, "No pages in this orientation. Adding page.");
 		gsb = gsbp.addBoardPage(orientation);
 		return gsb;
 	}
 	
 	public void movePage(ToastHandler toastHandler, GraphicalSoundboard toGsb) {
 		int orientation = this.movePageOrientation;
 		int fromPageNumber = this.moveFromPageNumber;
 		int toPageNumber = toGsb.getPageNumber();
 		
 		if (toGsb.getScreenOrientation() != orientation) {
 			toastHandler.toast("Wrong orientation!");
     		return;
     	}
 		resetMove();
 		
 		List<GraphicalSoundboard> pages = new ArrayList<GraphicalSoundboard>();
 		
 		int beginPageNumber = (fromPageNumber < toPageNumber) ? fromPageNumber : toPageNumber;
 		int endPageNumber = beginPageNumber + Math.abs(fromPageNumber-toPageNumber);
 		
 		for (int i = beginPageNumber; i <= endPageNumber; i++) {
 			GraphicalSoundboard gsb = gsbp.getPage(orientation, i);
 			pages.add(gsb);
 		}
 		
 		for (GraphicalSoundboard gsb : pages) {
 			int pageNumber = gsb.getPageNumber();
 
 			if (pageNumber == fromPageNumber) {
 				gsb.setPageNumber(toPageNumber);
 				gsbp.overrideBoard(gsb);
 			} else if (fromPageNumber > toPageNumber) {
 				gsb.setPageNumber(pageNumber + 1);
 				gsbp.overrideBoard(gsb);
 			} else if (fromPageNumber < toPageNumber) {
 				gsb.setPageNumber(pageNumber - 1);
 				gsbp.overrideBoard(gsb);
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param current gsb
 	 * @return next board page or null
 	 */
 	public GraphicalSoundboard getNextBoardPage(GraphicalSoundboard lastGsb) {
 		int orientation = lastGsb.getScreenOrientation();
 		GraphicalSoundboard selectedBoard = null;
 		
 		selectedBoard = gsbp.getPage(orientation, lastGsb.getPageNumber() + 1);
 		
 		if (selectedBoard == null) selectedBoard = gsbp.getPage(orientation, 0); // Last page, go to first page.
 		
 		updatePageNumber(selectedBoard);
 		return selectedBoard;
 	}
 	
 	/**
 	 * 
 	 * @param current gsb
 	 * @return next board page or null
 	 */
 	public GraphicalSoundboard getPreviousPage(GraphicalSoundboard lastGsb) {
 		int orientation = lastGsb.getScreenOrientation();
 		GraphicalSoundboard selectedBoard = null;
 		
 		selectedBoard = gsbp.getPage(orientation, lastGsb.getPageNumber() - 1);
 		
 		if (selectedBoard == null) {
 			int lastPage = getLastPageNumber(orientation); // First page, go to last page.
 			selectedBoard = gsbp.getPage(orientation, lastPage);
 		}
 		
 		updatePageNumber(selectedBoard);
 		return selectedBoard;
 	}
 	
 	private int getLastPageNumber(int orientation) {
 		int lastPage = -1;
 		
 		while (gsbp.getPage(orientation, lastPage + 1) != null) {
 			lastPage++;
 		}
 		
 		return lastPage;
 	}
 	
 	private void updatePageNumber(GraphicalSoundboard gsb) {
 		if (gsb != null) {
 			int pageNumber = gsb.getPageNumber();
 			int orientation = gsb.getScreenOrientation();
 			if (orientation == GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT) {
 				setPageNumberPortrait(pageNumber);
 			} else if (orientation == GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE) {
 				setPageNumberLandscape(pageNumber);
 			}
 		}
 	}
 	
 	public boolean isMovePageMode() {
 		return movePageMode;
 	}
 	public int getPageNumberForOrientation(int orientation) {
 		if (orientation == GraphicalSoundboard.SCREEN_ORIENTATION_PORTRAIT) {
 			return pageNumberPortrait;
 		} else if (orientation == GraphicalSoundboard.SCREEN_ORIENTATION_LANDSCAPE) {
 			return pageNumberLandscape;
 		} else {
 			return -1;
 		}
 	}
 	public void setPageNumberPortrait(int pageNumberPortrait) {
 		this.pageNumberPortrait = pageNumberPortrait;
 		if (gsbp.isPaginationSynchronizedBetweenOrientations()) {
 			this.pageNumberLandscape = pageNumberPortrait;
 		}
 	}
 	public void setPageNumberLandscape(int pageNumberLandscape) {
 		this.pageNumberLandscape = pageNumberLandscape;
 		if (gsbp.isPaginationSynchronizedBetweenOrientations()) {
 			this.pageNumberPortrait = pageNumberLandscape;
 		}
 	}
 }
