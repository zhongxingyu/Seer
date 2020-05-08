 package org.pentaho.di.palo.core;
 
import java.util.Iterator;

 /*
  *   This file is part of PaloKettlePlugin.
  *
  *   PaloKettlePlugin is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   PaloKettlePlugin is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   
  *   Copyright 2008 Stratebi Business Solutions, S.L.
  *   Copyright 2010 Pentaho
  */
 
 public final class ReadOnlyDimensionGroupingCollection extends DimensionGroupingCollection {
 
 	private static final long			serialVersionUID	= 5707708899733173613L;
 
 	private DimensionGroupingCollection	list				= null;
 
 	public DimensionGrouping get(int index) {
 		return (DimensionGrouping) list.get(index);
 	}
 
 	public int size() {
 		return list.size();
 	}
 
 	public ReadOnlyDimensionGroupingCollection(DimensionGroupingCollection collection) {
 		this.list = collection;
 	}
 
 	public boolean add(DimensionGrouping g) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void clear() {
 		throw new UnsupportedOperationException();
 	}
 
 	public void remove(DimensionGrouping d) {
 		throw new UnsupportedOperationException();
 	}
 
 	public void removeAll() {
 		throw new UnsupportedOperationException();
 	}
 
 	public DimensionGrouping remove(int i) {
 		throw new UnsupportedOperationException();
 	}
	
	public Iterator<DimensionGrouping> iterator() {
        return list.iterator();
    }
 }
