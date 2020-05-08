 package confdb.data;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 
 /**
  * PrimaryDataset
  * --------------
  * @author Philipp Schieferdecker
  *
  * PrimaryDatasets are a set (!) of paths assigned with the parent
  * stream. Each path can only be assigned to one of the PDs assigned
  * with a single stream.
  */
 public class PrimaryDataset extends DatabaseEntry
                             implements Comparable<PrimaryDataset>
 {
     //
     // member data
     //
     
     /** name of the stream */ 
     private String name;
     
     /** collection of assigned paths */
     private ArrayList<Path> paths = new ArrayList<Path>();
     
     /** parent stream */
     private Stream parentStream = null;
     
 
     //
     // construction
     //
     
     /** standard constructor */
     public PrimaryDataset(String name,Stream parentStream)
     {
 	this.name        = name;
 	this.parentStream = parentStream;
     }
     
     
     //
     // member functions
     //
     
     /** name of this stream */
     public String name() { return name; }
 

    public int databaseId(){
	hasChanged();
	return super.databaseId();
    }

    public boolean hasChanged(){
 	for (Path p : paths){
 	    if(p.hasChanged()){
 		setHasChanged();
 		break;
 	    }
 	}
 	return super.hasChanged();
     }
     
     /** get the parent stream */
     public Stream parentStream() { return parentStream; }
 
     /** set name of this stream */
     public void setName(String name) { this.name = name;
 	setHasChanged();
 
     }
     
     /** overload 'toString()' */
     public String toString() { return name(); }
     
     /** Comparable: compareTo() */
     public int compareTo(PrimaryDataset s)
     {
 	return toString().compareTo(s.toString());
     }
 
     /** number of paths */
     public int pathCount() { return paths.size(); }
 
     /** retrieve i-th path */
     public Path path(int i) { return paths.get(i); }
     
     /** retrieve path by name */
     public Path path(String pathName)
     {
 	for (Path p : paths) if (p.name().equals(pathName)) return p;
 	return null;
     }
     
     /** retrieve iterator over paths */
     public Iterator<Path> pathIterator() { return paths.iterator(); }
     
     /** index of a certain path */
     public int indexOfPath(Path path) { return paths.indexOf(path); }
 
     /** insert and associate a path with this stream */
     public boolean insertPath(Path path)
     {
 	if (paths.indexOf(path)>=0) {
 	    System.err.println("PrimaryDataset.insertPath() ERROR: "+
 			       "path already associated!");
 	    return false;
 	}
 	if (parentStream.listOfAssignedPaths().indexOf(path)>=0) {
 	    System.err.println("PrimaryDataset.insertPath() ERROR: "+
 			       "path already associated with another dataset "+
 			       "in the parent stream!");
 	    return false;
 	}
 	paths.add(path);
 	setHasChanged();
 	parentStream.setHasChanged();
 	return true;
     }
     
     /** remove a path from this stream */
     public boolean removePath(Path path)
     {
 	int index = paths.indexOf(path);
 	if (index<0) return false;
 	paths.remove(index);
 	setHasChanged();
 	return true;
     }
     
 }
