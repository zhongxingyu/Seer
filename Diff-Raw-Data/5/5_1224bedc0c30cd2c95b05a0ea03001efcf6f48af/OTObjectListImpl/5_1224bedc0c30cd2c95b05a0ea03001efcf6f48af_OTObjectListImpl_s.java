 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.16 $
  * $Date: 2007-10-10 03:09:05 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk;
 
 import java.lang.reflect.Array;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.otrunk.datamodel.OTDataList;
 
 /**
  * OTObjectList
  * Class name and description
  *
  * Date created: Nov 9, 2004
  *
  * @author scott<p>
  *
  */
 public class OTObjectListImpl extends OTCollectionImpl
 	implements OTObjectList
 {
 	protected OTDataList list;
 	
     /**
      * This is used to store references to the OTObjects, this prevents them from 
      * getting garbage collected as long as this collection is referenced. <p>
      * 
      * Using an ArrayList seemed like the natural thing to do, but it is hard to keep
      * that synchronized with the data list.  So instead a map is used.
      */
 	protected HashMap<OTID, OTObject> referenceMap;
 	
 	public OTObjectListImpl(String property, OTDataList resList, OTObjectInternal objectInternal)
 	{
 		super(property, objectInternal);
 		this.list = resList;
 	}
 
 	protected OTID getId(int index)
 	{
 		if (index < 0){
 			return null;
 		}
 		
 		OTID id = (OTID)list.get(index);
 		if(id == null) {
 			System.err.println("Null item in object list: \n" + 
 					"   " + objectInternal.getGlobalId() + "." +
 					property + "[" + index + "]");
 			
 			return null;
 		}
 		
 		return id;
 	}
 	
 	public OTObject get(int index)
 	{
 		try {
 			OTID id = getId(index);
 			if(id == null) {
 				return null;
 			}
 			OTObject otObject = objectInternal.getOTObject(id);
 			
 			if(referenceMap == null){
 				referenceMap = new HashMap<OTID, OTObject>();
 			}
 			referenceMap.put(id, otObject);
 			return otObject;
 		} catch (Exception e) {
 			e.printStackTrace();			
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @see org.concord.framework.otrunk.OTObjectList#getVector()
 	 */
 	public Vector<OTObject> getVector()
 	{
 		Vector<OTObject> childVector = new Vector<OTObject>();
 
 		for(int i=0; i<list.size(); i++) {
 			try {	
 				OTID childID = getId(i);
 				if(childID == null){
 					childVector.add(null);
 				} else {
 					childVector.add(objectInternal.getOTObject(childID));					
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return childVector;
 	}
 			
 	public boolean add(OTObject obj)
 	{
 		OTID id = obj.getGlobalId();
 		if(id == null) {
 			throw new RuntimeException("adding null id object list");
 		}
 
 		list.add(id);
 		
 		if(referenceMap == null){
 			referenceMap = new HashMap<OTID, OTObject>();
 		}
 		referenceMap.put(id, obj);
 		
 		notifyOTChange(OTChangeEvent.OP_ADD, obj, null);
 		
 		return true;
 	}
 	
 	public void add(int index, OTObject obj)
 	{
 		OTID id = obj.getGlobalId();
 		if(id == null) {
 			throw new RuntimeException("adding null id object list");
 		}
 
 		list.add(index, id);
 		
 		if(referenceMap == null){
 			referenceMap = new HashMap<OTID, OTObject>();
 		}
 		referenceMap.put(id, obj);
 		
 		notifyOTChange(OTChangeEvent.OP_ADD, obj, null);
 	}
 
 	/*
 	 * This is a hack until we can sort this out
 	 * it would be best if the users of this list could have all ids hidden from
 	 * them.
 	 */
 	public void add(OTID id)
 	{
 		list.add(id);
 
 		// FIXME will screw up some listeners which expect an object not an 
 		//  id.  But the reason this call is here is for efficiency so the actual
 		//  OTObject doesn't need to be created.  So it isn't clear what to do  		
 		notifyOTChange(OTChangeEvent.OP_ADD, id, null);
 	}
 	
 	public void set(int index, OTObject obj)
 	{
 		OTID id = obj.getGlobalId();
 		if(id == null) {
 			throw new RuntimeException("adding null id object list");
 		}
 		
 		Object previousObject = list.set(index, id);
 		
 		if(previousObject instanceof OTID){
 	        try {
 		        previousObject = objectInternal.getOTObject((OTID) previousObject);
 	        } catch (Exception e) {
 		        // TODO Auto-generated catch block
 		        e.printStackTrace();
 	        }			
 		}
 
 		if(referenceMap == null){
 			referenceMap = new HashMap<OTID, OTObject>();
 		}
 		referenceMap.put(id, obj);
 		
 		if(previousObject != null){
 			// FIXME we should remove the reference from this list only if it hasn't
 			// be set into 2 different places.  We'd need to track where each object 
 			// was inserted to do this correctly.
 		}
 		
 		notifyOTChange(OTChangeEvent.OP_SET, obj, previousObject);
 	}
 
 	public int size()
 	{
 		return list.size();
 	}
 	
 	public void clear()
 	{
 		list.removeAll();
 		
 		referenceMap = null;
 		
 		notifyOTChange(OTChangeEvent.OP_REMOVE_ALL, null, null);		
 	}
 	
 	/**
 	 * @see org.concord.framework.otrunk.OTObjectList#remove(org.concord.framework.otrunk.OTObject)
 	 */
 	public boolean remove(Object genericObj)
 	{
 		if(!(genericObj instanceof OTObject)){
 			throw new RuntimeException("can't remove object that isn't a OTObject");			
 		}
 		
 		OTObject obj = (OTObject) genericObj;
 		OTID id = obj.getGlobalId();
 		if(id == null) {
 			throw new RuntimeException("adding null id object list");
 		}
 
 		list.remove(id);
 		if(referenceMap != null){
 			referenceMap.put(id, null);
 		}
 		
 		notifyOTChange(OTChangeEvent.OP_REMOVE, obj, null);
 		
 		return true;
 	}
 
 	/**
 	 * @see org.concord.framework.otrunk.OTObjectList#remove(int)
 	 */
 	public void remove(int index)
 	{
 		OTID id = getId(index);
 		list.remove(index);
 
 		
 		OTObject obj = null;
 		if(id != null){
 			try {
 				obj = objectInternal.getOTObject(id);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			if(referenceMap != null){
 				referenceMap.put(id, null);
 			}
 		}		
 
 		notifyOTChange(OTChangeEvent.OP_REMOVE, obj, null);		
 	}
 		
 	/**
 	 * This is package protected.  It should not be used outside of this package,
 	 * because it will be removed at some point.
 	 * 
 	 * @return
 	 */
 	OTDataList getDataList()
 	{
 		return list;
 	}
 
 	public Iterator<OTObject> iterator()
     {
 		return new Iterator<OTObject>(){
 			/**
 			 * This points to the current object index;
 			 */
 			int index = -1;
 			
 			public boolean hasNext()            
 			{
 				return index < (size() - 1);
             }
 
 			public OTObject next()
             {
 				index++;
 				return get(index);
             }
 
 			public void remove()
             {
 				OTObjectListImpl.this.remove(index);	            
             }			
 		};
     }
 
 	public boolean contains(Object obj)
 	{
 		if(!(obj instanceof OTObject)){
 			throw new IllegalArgumentException("not an OTObject");
 		}
 		
 		OTID id = ((OTObject)obj).getGlobalId();
 		if(id == null) {
 			throw new IllegalArgumentException("null object id");
 		}
 
 		return list.contains(obj);
 	}
 	
 	public Object[] toArray()
     {
 		return toArray(new OTObject[list.size()]);
     }
 
 	@SuppressWarnings("unchecked")
     public <T> T[] toArray(T[] array)
     {
 		int size = list.size();
 		Class<?> componentType = array.getClass().getComponentType();
 		if(array.length < size){
             array = (T[])Array.newInstance(componentType, size);		
 		}
 
 		Object objToBeStored = null;
 		for(int i=0; i<size; i++) {
 			try {	
 				OTID childID = getId(i);
 				if(childID == null){
 					array[i] = null;
 				} else {
 					objToBeStored = objectInternal.getOTObject(childID); 
 					array[i] = (T) objToBeStored;										
 				}
			} catch (ClassCastException cce) {
 				throw new ArrayStoreException("Can't store " + objToBeStored + " at index: " + i +
					 " in array of type: " + componentType + ": " + cce.getMessage());
 			} catch (Exception e)  {
 				e.printStackTrace();
 			}
 		}		
 		
         if (array.length > size) {
             array[size] = null;
         }
         
         return array;
     }	
 
 	public boolean containsAll(Collection<?> c)
     {
 		Iterator<?> iterator = c.iterator();
 		while(iterator.hasNext()){
 			if(!contains(iterator.next())){
 				return false;
 			}
 		}
 		
 		return true;
     }
 
 	/**
 	 * @see java.util.Collection#addAll(java.util.Collection)
 	 */
 	public boolean addAll(Collection<? extends OTObject> c)
 	{
 		for (OTObject object : c) {
 	        add(object);
         }
 		
 		return true;
 	}
 	
 	/** 
 	 * Unsupported 
 	 * @see java.util.Collection#removeAll(java.util.Collection)
 	 */
 	public boolean removeAll(Collection<?> c)
     {
 		throw new UnsupportedOperationException();
     }
 
 	/**
 	 * Unsupported
 	 * @see java.util.Collection#retainAll(java.util.Collection)
 	 */
 	public boolean retainAll(Collection<?> c)
     {
 		throw new UnsupportedOperationException();
     }
 
 }
