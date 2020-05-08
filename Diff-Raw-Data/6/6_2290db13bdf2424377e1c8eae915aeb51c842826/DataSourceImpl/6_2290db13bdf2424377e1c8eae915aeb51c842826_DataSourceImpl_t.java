 package com.iq4j.faces.impl;
 
 import java.io.Serializable;
 import java.util.List;
 
 import com.iq4j.faces.api.DataSource;
 import com.iq4j.utils.impl.ObjectListImpl;
 
 public abstract class DataSourceImpl<T> extends ObjectListImpl<T> implements DataSource<T>, Serializable {
 
 	private static final long serialVersionUID = 3917145886233868937L;
 	
 	// --- fields
 	private boolean selectable;
 	private List<T> filteredList;
 	private T selection;
 	
 	// --- constructors
 	public DataSourceImpl() {
 		this(true);
 	}
 	
 	public DataSourceImpl(boolean selectable) {
 		this.selectable = selectable;
 	}
 	
 	// --- methods
 	protected abstract List<T> load();
 	
	@Override
	public void refresh() {
		filteredList = null;
		super.refresh();
	}
	
 	// --- properties
 	@Override
 	public boolean isSelected() {
 		return getSelection() != null;
 	}
 	
 	@Override
 	public List<T> getFilteredList() {
 		return filteredList;
 	}
 
 	@Override
 	public void setFilteredList(List<T> filteredList) {
 		this.filteredList = filteredList;
 	}
 
 	@Override
 	public boolean isSelectable() {
 		return selectable;
 	}
 
 	@Override
 	public T getSelection() {
 		return selection;
 	}
 
 	@Override
 	public void setSelection(T instance) {
 		this.selection = instance;
 	}
 
 }
