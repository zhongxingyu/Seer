 package net.sourceforge.eclipseccase.ui;
 
 import java.util.Comparator;
 import org.eclipse.core.resources.IResource;
 
public class DirectoryLastComparator implements Comparator {
 
 	public DirectoryLastComparator() {
 		super();
 	}
 
 	public int compare(Object o1, Object o2) {
 		IResource r1 = (IResource) o1;
 		IResource r2 = (IResource) o2;
 		boolean isDir1 = r1.getType() != IResource.FILE;
 		boolean isDir2 = r2.getType() != IResource.FILE;
 		if (isDir1 && !isDir2)
 			return 1;
 		else if (!isDir1 && isDir2)
 			return -1;
 		else
 			return (r2.getFullPath().toString()).compareTo(r1.getFullPath().toString());
 	}
 
 }
