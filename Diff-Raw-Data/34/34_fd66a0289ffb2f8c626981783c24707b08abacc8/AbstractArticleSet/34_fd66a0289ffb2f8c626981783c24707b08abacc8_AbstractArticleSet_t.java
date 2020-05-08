 package nz.gen.wellington.guardian.android.model;
 
import java.io.Serializable;

public abstract class AbstractArticleSet implements Serializable {
 	
	private static final long serialVersionUID = 1L;
 	private int pageSize;
 
 	public AbstractArticleSet(int pageSize) {
 		this.pageSize = pageSize;
 	}
 
 	public int getPageSize() {
 		return pageSize;
 	}
 	
 }
