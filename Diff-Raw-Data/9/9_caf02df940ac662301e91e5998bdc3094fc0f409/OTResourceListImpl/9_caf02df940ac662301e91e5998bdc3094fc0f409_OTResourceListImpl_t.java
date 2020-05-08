 /**
  * 
  */
 package org.concord.otrunk;
 
 import java.lang.reflect.Array;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.otrunk.datamodel.OTDataList;
 
 /**
  * @author scott
  *
  */
 public class OTResourceListImpl extends OTResourceCollectionImpl
 	implements OTResourceList 
 {
 	protected OTDataList list;
 	
 	/**
 	 * @param handler 
 	 * 
 	 */
 	public OTResourceListImpl(String property, OTDataList list, 
 		OTObjectInternal handler) 
 	{
 		super(property, handler);
 		this.list = list;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTResourceList#add(java.lang.Object)
 	 */
 	public boolean add(Object object) 
 	{
 		Object toBeStored = translateExternalToStored(object);
 		boolean added = list.add(toBeStored);
 		notifyOTChange(OTChangeEvent.OP_ADD, object, null);
 		return added;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTResourceList#add(int, java.lang.Object)
 	 */
 	public void add(int index, Object object) 
 	{
 		Object toBeStored = translateExternalToStored(object);
 
 		list.add(index, toBeStored);
 		notifyOTChange(OTChangeEvent.OP_ADD, object, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTResourceList#get(int)
 	 */
 	public Object get(int index) 
 	{
 		Object stored =  list.get(index);
 		return translateStoredToExternal(stored);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTResourceList#remove(int)
 	 */
 	public void remove(int index) 
 	{
 		Object obj = list.get(index);
 		list.remove(index);
 		notifyOTChange(OTChangeEvent.OP_REMOVE, obj, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTResourceList#remove(java.lang.Object)
 	 * FIXME if this fails we need to search for the byte[] in 
 	 * BlobResources
 	 */
 	public boolean remove(Object obj) 
 	{
 		boolean removed = list.remove(obj);
 		notifyOTChange(OTChangeEvent.OP_REMOVE, obj, null);
 		return removed;
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTResourceList#set(int, java.lang.Object)
 	 */
 	public void set(int index, Object object) 
 	{
 		Object toBeStored = translateExternalToStored(object);
 		Object previousValue = list.set(index, toBeStored);
 		notifyOTChange(OTChangeEvent.OP_SET, object, previousValue);
 	}
 
 	public void clear()
     {
 		list.removeAll();
 		notifyOTChange(OTChangeEvent.OP_REMOVE_ALL, null, null);
     }
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTResourceCollection#size()
 	 */
 	public int size() 
 	{
 		return list.size();
 	}
 	
 	public boolean containsAll(Collection c)
     {
 		Iterator iterator = c.iterator();
 		while(iterator.hasNext()){
 			if(!contains(iterator.next())){
 				return false;
 			}
 		}
 		
 		return true;
     }
 
 	public Object[] toArray()
     {
 		return toArray(new Object[list.size()]);
     }
 
 	public Object[] toArray(Object[] array)
     {
 		int size = list.size();
 		if(array.length < size){
             array = (Object[])Array.newInstance(array.getClass().getComponentType(), size);		
 		}
 
 		for(int i=0; i<size; i++) {
 			array[i] = list.get(i);
 		}		
 		
         if (array.length > size) {
             array[size] = null;
         }
         
         return array;
     }	
 
 	public boolean contains(Object obj)
     {
 		return list.contains(obj);		
     }
 
 	public Iterator iterator()
     {
 		return new Iterator(){
 			/**
 			 * This points to the current object index;
 			 */
 			int index = -1;
 			
 			public boolean hasNext()            
 			{
				return index < (size() - 1);
             }
 
 			public Object next()
             {
 				index++;
 				return get(index);
             }
 
 			public void remove()
             {
 				OTResourceListImpl.this.remove(index);	            
             }			
 		};
     }	
 
 	public boolean addAll(Collection c)
 	{
 		throw new UnsupportedOperationException();
 	}
 	
 	public boolean removeAll(Collection c)
     {
 		throw new UnsupportedOperationException();
     }
 
 	public boolean retainAll(Collection c)
     {
 		throw new UnsupportedOperationException();
     }
 
 
 }
