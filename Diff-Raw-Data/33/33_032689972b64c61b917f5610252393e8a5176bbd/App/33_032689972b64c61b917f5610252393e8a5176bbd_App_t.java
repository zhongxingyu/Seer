 package de.echox.hacklace.pix0lat0r.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.echox.hacklace.pix0lat0r.data.Matrix;
 import de.echox.hacklace.pix0lat0r.data.MatrixImpl;
 
 public class App {
 	
 	List<Matrix> pages = new ArrayList<Matrix>();
 	
	private int pageIdx = 0;
 
 	public void quit() {
 		System.exit(0);
 	}
 
 	App() {
 		pages.add(new MatrixImpl(5,7));
 	}
 	
 	public Matrix nextPage() {
		pageIdx++;
		if(pageIdx == pages.size()) {
 			pages.add(new MatrixImpl(5,7));
 		}
		return pages.get(pageIdx);
 	}
 
 	public Matrix prevPage() {
		if (pageIdx > 0) {
			pageIdx--;
 		}
		return pages.get(pageIdx);
 	}
 
 	public int getCurrentPage() {
		return pageIdx + 1;
 	}
 
 	public int getPageSize() {
 		return this.pages.size();
 	}
 }
