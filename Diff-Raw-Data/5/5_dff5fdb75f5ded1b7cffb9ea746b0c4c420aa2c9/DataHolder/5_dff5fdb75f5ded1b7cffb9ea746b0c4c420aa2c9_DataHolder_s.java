 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IMetadataProvider;
 
 /**
  * This class is to marshal all the data for the purpose of loading from or saving to a file
  * directly or via a ScanFileHolder.
  * <p>
  * This is designed to take in any dataset obeying the IDataset interface but output an
  * object that is a subclass of AbstractDataset - the dataset will be converted if necessary.
  */
 public class DataHolder implements IMetadataProvider, IDataHolder, Serializable {
 	
 	protected static final Logger logger = LoggerFactory.getLogger(DataHolder.class);
 
 	/**
 	 * List containing all the data to be loaded
 	 */
 	private List<ILazyDataset> data;
 
 	/**
 	 * List containing the names of all the data
 	 */
 	private List<String> names;
 
 	/**
 	 * List containing metadata
 	 */
 	private IMetaData metadata;
 
 	/**
 	 * Loader class string
 	 */
 	private String loaderClass;
 	
 	/**
 	 * The path to the original file loaded (if any)
 	 */
 	private String filePath;
 
 	/**
 	 * This must create the three objects which will be put into the ScanFileHolder
 	 */
 	public DataHolder() {
 		data = new Vector<ILazyDataset>();
 		names = new Vector<String>();
 		metadata = new Metadata();
 	}
 	
 
 	/**
 	 * The current data as a map
 	 * @return map of lazy data
 	 */
 	@Override
 	public Map<String, ILazyDataset> toLazyMap() {
 		final Map<String, ILazyDataset> ret= new LinkedHashMap<String, ILazyDataset>(names.size());
 		for (String name : names) {
 			ret.put(name, getLazyDataset(name));
 		}
 		return ret;
 	}
 
 	
 	/**
	 * 
 	 * @return shallow copy of DataHolder
 	 */
 	@Override
 	public IDataHolder clone() {
 		
 		DataHolder ret = new DataHolder();
 		ret.data.addAll(data);
 		ret.names.addAll(names);
		ret.metadata    = metadata!=null ? metadata.clone() : null;
 		ret.filePath    = filePath;
 		ret.loaderClass = loaderClass;
 		return ret;
 	}
 	
 
 	/**
 	 * Adds a dataset and its name into the two vectors of the Object.
 	 * 
 	 * @param name
 	 *            the name of the dataset which is to be added
 	 * @param dataset
 	 *            the actual data of the dataset
 	 */
 	@Override
 	public boolean addDataset(String name, ILazyDataset dataset) {
 		// Do not allow duplicates
 		boolean ret = remove(name);
 		names.add(name);
 		data.add(dataset);
 		return ret;
 	}
 
 	/**
 	 * Adds a dataset, metadata and its name. This is for Diffraction data
 	 * 
 	 * Replaces any datasets of the same name already existing 
 	 * 
 	 * @param name
 	 *            the name of the dataset which is to be added
 	 * @param dataset
 	 *            the actual data of the dataset.
 	 * @param metadata
 	 *            the metadata that is associated with the dataset
 	 */
 	public boolean addDataset(String name, ILazyDataset dataset, IMetaData metadata) {
 		boolean ret = remove(name);
 		names.add(name);
 		data.add(dataset);
 		this.metadata = metadata;
 		return ret;
 	}
 	
 	private boolean remove(String name) {
 		if (names.contains(name)) {
 			data.remove(names.indexOf(name));
 			names.remove(name);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Add a ImetaData object
 	 * @param metadata which is an object implementing IMetaData
 	 */
 
 	public void setMetadata(IMetaData metadata) {
 		this.metadata = metadata;
 	}
 
 	/**
 	 * @return an object implementing IMetaData
 	 */
 	@Override
 	public IMetaData getMetadata() {
 		return metadata;
 	}
 
 	/**
 	 * This is not guaranteed to work as duplicate names will overwrite in the map.
 	 * @return Read-Only Map of datasets with keys from their corresponding names
 	 */
 	public Map<String, ILazyDataset> getMap() {
 		Map<String, ILazyDataset> hm = new LinkedHashMap<String, ILazyDataset>();
 		int imax = data.size();
 		for (int i = 0; i < imax; i++) {
 			hm.put(names.get(i), data.get(i));
 		}
 		return hm;
 	}
 
 	/**
 	 * @return List of datasets
 	 */
 	public List<ILazyDataset> getList() {
 		int imax = data.size();
 		List<ILazyDataset> al = new ArrayList<ILazyDataset>(imax);
 		for (int i = 0; i < imax; i++) {
 			al.add(data.get(i));
 		}
 		return data;
 	}
 
 	/**
 	 * Set a generic dataset at given index. Ensure the index is in range otherwise an exception
 	 * will occur
 	 * @param index
 	 * @param dataset
 	 */
 	public void setDataset(int index, ILazyDataset dataset) {
 		data.set(index, dataset);
 	}
 
 	/**
 	 * Set a generic dataset with given name
 	 * @param name
 	 * @param dataset
 	 */
 	public void setDataset(String name, ILazyDataset dataset) {
 		if( names.contains(name)){
 			data.set(names.indexOf(name), dataset);
 		} else {
 			addDataset(name, dataset);
 		}
 	}
 
 	/**
 	 * This does not retrieve lazy datasets.
 	 * @param index
 	 * @return Generic dataset with given index in holder
 	 */
 	@Override
 	public AbstractDataset getDataset(int index) {
 		return DatasetUtils.convertToAbstractDataset(data.get(index));
 	}
 
 	/**
 	 * This does not retrieve lazy datasets.
 	 * @param name
 	 * @return Generic dataset with given name (first one if name not unique)
 	 */
 	@Override
 	public AbstractDataset getDataset(String name) {
 		if (names.contains(name))
 			return DatasetUtils.convertToAbstractDataset(data.get(names.indexOf(name)));
 		return null;
 	}
 
 	/**
 	 * This pulls out the dataset which could be lazy, maintaining its laziness.
 	 * @param index
 	 * @return Generic dataset with given index in holder
 	 */
 	@Override
 	public ILazyDataset getLazyDataset(int index) {
 		return data.get(index);
 	}
 
 	/**
 	 * This pulls out the dataset which could be lazy, maintaining its laziness.
 	 * @param name
 	 * @return Generic dataset with given name (first one if name not unique)
 	 */
 	@Override
 	public ILazyDataset getLazyDataset(String name) {
 		if (names.contains(name))
 			return data.get(names.indexOf(name));
 		return null;
 	}
 
 	/**
 	 * @param name
 	 * @return true if data holder contains name 
 	 * @see java.util.List#contains(Object)
 	 */
 	@Override
 	public boolean contains(String name) {
 		return names.contains(name);
 	}
 
 	/**
 	 * @param name
 	 * @return index of first dataset with given name
 	 * @see java.util.List#indexOf(Object)
 	 */
 	public int indexOf(String name) {
 		return names.indexOf(name);
 	}
 
 	/**
 	 * @return Array of dataset names
 	 */
 	@Override
 	public String[] getNames() {
 		return names.toArray(new String[names.size()]);
 	}
 
 	/**
 	 * @param index
 	 * @return Dataset name at given index
 	 */
 	@Override
 	public String getName(final int index) {
 		if (index >= 0 && index < names.size())
 			return names.get(index);
 		return null;
 	}
 
 	/**
 	 * @return Number of datasets
 	 */
 	@Override
 	public int size() {
 		return data.size();
 	}
 
 	/**
 	 * @return Number of unique dataset names
 	 */
 	@Override
 	public int namesSize() {
 		return names.size();
 	}
 
 	/**
 	 * Clear list of names and datasets
 	 * @see java.util.List#clear()
 	 */
 	public void clear() {
 		data.clear();
 		names.clear();
 		metadata = null;
 	}
 
 	/**
 	 * Remove name and dataset at index
 	 * @param index
 	 * @see java.util.List#remove(int)
 	 */
 	public void remove(int index) {
 		data.remove(index);
 		names.remove(index);
 	}
 
 	/**
 	 * @return class
 	 */
 	@SuppressWarnings("unchecked")
 	public Class<? extends AbstractFileLoader> getLoaderClass() {
 		try {
 			return (Class<? extends AbstractFileLoader>) Class.forName(loaderClass);
 		} catch (ClassNotFoundException e) {
 			logger.error("No class found for {}", loaderClass, e);
 		}
 		return null;
 	}
 
 	public void setLoaderClass(Class<? extends AbstractFileLoader> clazz) {
 		loaderClass = clazz.getName();
 	}
 
 	public String getFilePath() {
 		return filePath;
 	}
 
 	public void setFilePath(String filePath) {
 		this.filePath = filePath;
 	}
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((data == null) ? 0 : data.hashCode());
 		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
 		result = prime * result + ((loaderClass == null) ? 0 : loaderClass.hashCode());
 		result = prime * result + ((metadata == null) ? 0 : metadata.hashCode());
 		result = prime * result + ((names == null) ? 0 : names.hashCode());
 		return result;
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		DataHolder other = (DataHolder) obj;
 		if (data == null) {
 			if (other.data != null)
 				return false;
 		} else if (!data.equals(other.data))
 			return false;
 		if (filePath == null) {
 			if (other.filePath != null)
 				return false;
 		} else if (!filePath.equals(other.filePath))
 			return false;
 		if (loaderClass == null) {
 			if (other.loaderClass != null)
 				return false;
 		} else if (!loaderClass.equals(other.loaderClass))
 			return false;
 		if (metadata == null) {
 			if (other.metadata != null)
 				return false;
 		} else if (!metadata.equals(other.metadata))
 			return false;
 		if (names == null) {
 			if (other.names != null)
 				return false;
 		} else if (!names.equals(other.names))
 			return false;
 		return true;
 	}
 
 }
