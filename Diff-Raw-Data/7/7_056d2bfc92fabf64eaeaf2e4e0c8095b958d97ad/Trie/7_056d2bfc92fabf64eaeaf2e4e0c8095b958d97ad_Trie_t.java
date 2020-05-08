 package data.structures;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.atomic.AtomicReferenceArray;
 
 public class Trie implements Collection<HashBitmapPair> {
 	
 	private final AtomicReferenceArray<Trie> children = new AtomicReferenceArray<Trie>(new Trie[256]);
 	private final AtomicReference<Integer> word = new AtomicReference<Integer>();
 	private boolean isWord = false;
 
 	@Override
 	public int size() {
 		int size = 0;
 		
 		if (this.isWord) {
 			size += 1;
 		}
 		
 		for (int i = 0; i < this.children.length(); i++) {
 			Trie child = this.children.get(i);
 			if (child != null) {
 				size += child.size();
 			}
 		}
 		
 		return size;
 	}
 	
 	@Override
 	public void clear() {
 		for (int i = 0; i < this.children.length(); i++) {
 			this.children.set(i, null);
 		}
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return this.size() == 0;
 	}
 	
 	@Override
 	public boolean add(HashBitmapPair pair) {
 		return this.addByte(pair, 0, true) != null;
 	}
 	
 	private Integer addByte(HashBitmapPair pair, int i, boolean createChildren) {
 		byte x = pair.getHash()[i];
 		Trie branch = this.children.get(x + 128);
 		if (branch == null) {
 			if (createChildren) {
 				branch = new Trie();
 				this.children.compareAndSet(x + 128, null, branch);
 				branch = this.children.get(x + 128);
 			} else {
 				return null;
 			}
 		}
 		
 		if (pair.getHash().length > i + 1) {
 			return branch.addByte(pair, i + 1, createChildren);
 		} else {
 			if (createChildren) {
				branch.isWord = true;
				branch.word.set(pair.getBitmap());
 			}
 			
			return branch.word.get();
 		}
 	}
 
 	@Override
 	public boolean contains(Object o) {
 		try {
 			HashBitmapPair pair = (HashBitmapPair) o;
 			return this.addByte(pair, 0, false) != null;
 		} catch (ClassCastException e) {
 			return false;
 		}
 	}
 	
 	public Integer getBitmap(HashBitmapPair pair) {
 		return this.addByte(pair, 0, false);
 	}
 
 	@Override
 	public Iterator<HashBitmapPair> iterator() {
 		throw new UnsupportedOperationException("iterator is not supported");
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c) {
 		throw new UnsupportedOperationException("containsAll is not supported");
 	}
 
 	@Override
 	public boolean addAll(Collection<? extends HashBitmapPair> c) {
 		throw new UnsupportedOperationException("addAll is not supported");
 	}
 
 	@Override
 	public boolean remove(Object o) {
 		throw new UnsupportedOperationException("remove is not supported");
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> c) {
 		throw new UnsupportedOperationException("removeAll is not supported");
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> c) {
 		throw new UnsupportedOperationException("retainAll is not supported");
 	}
 	
 	@Override
 	public Object[] toArray() {
 		throw new UnsupportedOperationException("toArray is not supported");
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a) {
 		throw new UnsupportedOperationException("toArray is not supported");
 	}
 }
