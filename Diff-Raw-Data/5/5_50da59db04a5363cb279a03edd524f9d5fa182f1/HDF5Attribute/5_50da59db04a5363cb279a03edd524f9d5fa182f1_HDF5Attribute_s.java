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
 
 package uk.ac.diamond.scisoft.analysis.hdf5;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.StringDataset;
 
 /**
  * Represent an attribute using a dataset
  */
 public class HDF5Attribute {
 	private HDF5File file;
 	private String node;
 	private String name;
 	private String type;
 	private AbstractDataset value;
 
 	/**
 	 * Create an attribute with file, node, name, value and sign
 	 * @param hdf5File
 	 * @param nodeName
 	 * @param attrName
 	 * @param attrValue (usually, this is a Java array)
 	 * @param isUnsigned true if items are unsigned but held in signed primitives
 	 */
 	public HDF5Attribute(final HDF5File hdf5File, final String nodeName, final String attrName, final Object attrValue, final boolean isUnsigned) {
 		file = hdf5File;
 		node = nodeName;
 		name = attrName;
 		value = AbstractDataset.array(attrValue, isUnsigned);
 	}
 
 	/**
 	 * @return hdf5 file of attribute
 	 */
 	public HDF5File getHDF5File() {
 		return file;
 	}
 
 	/**
 	 * @return name of attribute
 	 */
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String toString() {
 		return value.toString();
 	}
 
 	/**
 	 * @return first element as string
 	 */
 	public String getFirstElement() {
 		return value.getString(0);
 	}
 
 	/**
 	 * Set name of attribute type
 	 * @param name
 	 */
 	public void setTypeName(String name) {
 		type = name;
 	}
 
 	/**
 	 * @return name of attribute type
 	 */
 	public String getTypeName() {
 		return type;
 	}
 
 	/**
 	 * Get dataset holding value(s) of attribute
 	 * @return dataset
 	 */
 	public AbstractDataset getValue() {
 		return value;
 	}
 
 	/**
 	 * Set value of attribute from given object
 	 * @param obj
 	 */
 	public void setValue(Object obj) {
 		setValue(obj, false);
 	}
 
 	/**
 	 * Set value of attribute from given object
 	 * @param obj
 	 * @param isUnsigned
 	 *            if true, interpret integer values as unsigned by increasing element bit width
 	 */
 	public void setValue(Object obj, boolean isUnsigned) {
 		value = AbstractDataset.array(obj, isUnsigned);
 	}
 
 	/**
 	 * @return shape of attribute dataset
 	 */
 	public int[] getShape() {
 		return value.getShape();
 	}
 
 	/**
 	 * @return rank of attribute dataset
 	 */
 	public int getRank() {
 		return value.getRank();
 	}
 
 	/**
 	 * Get number of items in attribute dataset
 	 * @return size
 	 */
 	public int getSize() {
 		return value.getSize();
 	}
 
 	/**
 	 * Get node name
 	 * @return name
 	 */
 	public String getNode() {
 		return node;
 	}
 
 	/**
 	 * Get full name of attribute including node and attribute name
 	 * @return name
 	 */
 	public String getFullName() {
 		return node + HDF5Node.ATTRIBUTE + name;
 	}
 
 	/**
 	 * @return true if attribute contains strings
 	 */
 	public boolean isString() {
 		return value != null && (value instanceof StringDataset);
 	}
 }
