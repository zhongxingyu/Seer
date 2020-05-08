 package nz.gen.wellington.guardian.android.model;
 
public abstract class AbstractArticleSet {
 	
 	private int pageSize;
 
 	public AbstractArticleSet(int pageSize) {
 		this.pageSize = pageSize;
 	}
 
 	public int getPageSize() {
 		return pageSize;
 	}
 	
 }
