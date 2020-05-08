 package jp.oreore.hk.viewer.book;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import jp.oreore.hk.iface.IPageShower;
 import jp.oreore.hk.iface.IPagesMaker;
 import jp.oreore.hk.json.obj.Book;
 import jp.oreore.hk.json.obj.Mark;
 import jp.oreore.hk.json.obj.Note;
 import jp.oreore.hk.screen.RawScreenSize;
 import jp.oreore.hk.task.BookPageReader;
 import jp.oreore.hk.task.ImageCache;
 import jp.oreore.hk.task.ImageFetcher;
 import jp.oreore.hk.viewer.R;
 import jp.oreore.hk.viewer.ViewerUtil;
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class LogicPage implements IPagesMaker {
 	private static final String TAG = "LogicPage";
 	
 	private Activity activity;
 	private Book book;
 	private Note note;
 	private RawScreenSize rawSize;
 	private int pageIdx;
 	private List<String> pageList;
 	protected ImageFetcher imageFetcher;
 	private BookPageReader task;
 	private boolean showPage;
 	private boolean firstShowDone;
 	private IPageShower shower;
 	private String blankExtension;
 
 	public LogicPage(Activity act, Book b, Note n, RawScreenSize r) {
 		activity = act;
 		book = b;
 		note = n;
 		rawSize = r;
 		task = null;
 		pageIdx = -1;
 		showPage = false;
 		blankExtension = activity.getString(R.string.blank_page_extension);
 	}
 	
 	public void startReadPages() {
 		if(task != null) {
 			task.setExitTasksEarly(true);
 		}
 		task = null;
 		pageIdx = -1;
 		showPage = false;
 		firstShowDone = false;
 
 		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(activity);
 		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
 		
         // The ImageFetcher takes care of loading images into our ImageView children asynchronously
         imageFetcher = new ImageFetcher(activity);
         imageFetcher.addImageCache(activity.getFragmentManager(), cacheParams);
         imageFetcher.setImageFadeIn(false);
 
 		pageList = new ArrayList<String>();
 		task = new BookPageReader(this, activity, book, pageList);
 		task.exec();
 	}
 
 	public void setExitTasksEarly(boolean exit) {
 		if(task != null) {
 			task.setExitTasksEarly(exit);
 		}
 		if(imageFetcher != null) {
 			imageFetcher.setExitTasksEarly(exit);
 		}
 	}
 
 	private void setInitialPageIndex() {
 		pageIdx = 0;
 		Mark m = note.getMarkOfTemporary();
 		if(m == null) {
 			return;
 		}
 		String pnm = book.getPath() + m.getPageName();
 		if(m.getPageName().endsWith(blankExtension)) {
 			pnm = m.getPageName();
 		}
 		for(int i = 0; i < pageList.size(); i ++) {
 			if(pnm.equals(pageList.get(i))) {
 				pageIdx = i;
 				break;
 			}
 		}
 	}
 	
 	private void setMarkOfTemporary(String pnm) {
 		String fnm = pnm.replace(book.getPath(), "");
 		Mark m = Mark.getNewInstanceOfTemporary(fnm, "");
 		note.addMark(m);
 	}
 	
 	private void showPage() {
 		shower.clearView();
 		boolean memoryDone = false;
 		Iterator<IPageShower.ImageInfo> itr = shower.iterator();
 		while(itr.hasNext()) {
 			IPageShower.ImageInfo info = itr.next();
 			String pnm = info.path;
 			if(!memoryDone) {
 				memoryDone = true;
 				setMarkOfTemporary(pnm);
 			}
 			ImageView v = info.iview;
 			if(pnm.endsWith(blankExtension)) {
 				Drawable image = activity.getResources().getDrawable(R.drawable.blank_page);
 				v.setImageDrawable(image);
 				continue;
 			}
 			imageFetcher.loadImage(pnm, v);
 			
 			shower.additionalAction(info);
 		}
 		String prefetchpnm = shower.getNextPpath();
 		imageFetcher.loadImage(prefetchpnm, null);
 	}
 	
 	private void showFirstPage() {
 		boolean isLandscape = (ViewerUtil.OrientationMode.Landscape == ViewerUtil.getOrientationMode(activity));
 		if(book.isGtxt()) {
 			makeGtxtShower(isLandscape);
 		} else {
 			if(isLandscape && book.isTwin()) {
 				makeTwinShower();
 			} else {
				makeSoloShower(isLandscape);
 			}
 		}
 		shower.setFirst(pageIdx);
 		showPage();
 		firstShowDone = true;
 	}
 	
 	public void turnToForward() {
 		shower.turnToForward();
 		showPage();
 	}
 	
 	public void turnToBackward() {
 		shower.turnToBackward();
 		showPage();
 	}
 	
 	public void turnToDirect(int idx) {
 		shower.setFirst(idx);
 		showPage();
 	}
 	
 	private void makeGtxtShower(boolean isLandscape) {
 		ImageView v = (isLandscape
 						? (ImageView)activity.findViewById(R.id.imageViewBookPageGtxtLandscape)
 						: (ImageView)activity.findViewById(R.id.imageViewBookPageGtxtPortrait));
 		TextView t = (isLandscape
 						? (TextView)activity.findViewById(R.id.textViewBookPageGtxtLandscape)
 						: (TextView)activity.findViewById(R.id.textViewBookPageGtxtPortrait));
 		String ext = activity.getString(R.string.fixed_expression_text_extension);
 		shower = new ShowGtxt(book, pageList, rawSize, v, blankExtension, t, ext, isLandscape);
 	}
 	
	private void makeSoloShower(boolean isLandscape) {
		ImageView v = (ImageView)activity.findViewById(isLandscape ? R.id.imageViewBookPageLandCenter : R.id.imageViewBookPagePortrait);
 		shower = new ShowSolo(book, pageList, rawSize, v, blankExtension);
 	}
 	
 	private void makeTwinShower() {
 		ImageView vLeft = (ImageView)activity.findViewById(R.id.imageViewBookPageLandLeft);
 		ImageView vRight = (ImageView)activity.findViewById(R.id.imageViewBookPageLandRight);
 		ImageView vCenter = (ImageView)activity.findViewById(R.id.imageViewBookPageLandCenter);
 		float limit = Float.valueOf(activity.getString(R.string.limit_ratio_show_solo_in_landscape));
 		float shorter = Float.valueOf(activity.getString(R.string.shorter_ratio_of_width_in_landscape));
 		shower = new ShowTwin(book, pageList, rawSize, vLeft, vRight, vCenter, limit, shorter, blankExtension);
 	}
 	
 	public void startShowPage() {
 		showPage = true;
 		if(pageIdx < 0) {
 			return;
 		}
 		showFirstPage();
 	}
 	
 	//
 	// getter
 	//
 	
 	public boolean isFirstShowDone() {
 		return firstShowDone;
 	}
 	
 	public int getPageCount() {
 		return pageList.size();
 	}
 	
 	public int getCurrentIdx() {
 		return shower.getCurrentIdx();
 	}
 	
 	public String getPageInfo(int i) {
 		int idx = i;
 		int max = pageList.size();
 		if(idx < 0 || idx >= max) {
 			idx = 0;
 		}
 		String pnm = pageList.get(idx);
 		String info = pnm.replace(book.getPath(), "");
 		return info;
 	}
 	
 	public int getPageIdx(String pnm) {
 		int idx = 0;
 		for(String s : pageList) {
 			if(s.equals(pnm)) {
 				break;
 			}
 			idx ++;
 		}
 		return (idx >= pageList.size() ? 0 : idx);
 	}
 	
 	public int getAllowFlingLimit() {
 		return shower.getAllowFlingLimit();
 	}
 	
 	//
 	// interface
 	//
 
     // IPagesMaker
     public void notifyComplete() {
     	Log.d(TAG, "notifyComplete.");
     	
     	setInitialPageIndex();
     	if(showPage) {
     		showFirstPage();
     	}
     }
 }
