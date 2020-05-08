 package brown.puzzles.liarliar;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Matt Brown
  * @date Feb 22, 2010
  */
 public class Partition<T> {
 
 	private String name;
 
 	private Set<T> set;
 
 	public Partition(String name) {
 		this.name = name;
 		this.set = new HashSet<T>();
 	}
 
 	public void add(T node) {
 		this.set.add(node);
 	}
 
 	public void addAll(Collection<? extends T> col) {
 		this.set.addAll(col);
 	}
 
 	public boolean contains(T node) {
 		return this.set.contains(node);
 	}
 
 	public int size() {
 		return this.set.size();
 	}
 
 	@Override
 	public String toString() {
 		return "Parition [" + name + "]";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((set == null) ? 0 : set.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) return true;
 		if (obj == null) return false;
 		if (getClass() != obj.getClass()) return false;
		Partition other = (Partition) obj;
 		if (name == null) {
 			if (other.name != null) return false;
 		}
 		else if (!name.equals(other.name)) return false;
 		if (set == null) {
 			if (other.set != null) return false;
 		}
 		else if (!set.equals(other.set)) return false;
 		return true;
 	}
 
 }
