 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.gda.extensions.loaders;
 
 import ncsa.hdf.object.Dataset;
 import ncsa.hdf.object.Datatype;
 import ncsa.hdf.object.Group;
 import ncsa.hdf.object.h5.H5Datatype;
 
 import org.dawb.hdf5.IHierarchicalDataFile;
 import org.dawb.hdf5.Nexus;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 
 public class H5Utils {
 
 	public static int getDataType(Datatype datatype) throws Exception {
 		
 
 		final int type = datatype.getDatatypeClass();
 		if (type == Datatype.CLASS_ARRAY) throw new Exception("Cannot read array type data sets!");
 			
 		final int size = datatype.getDatatypeSize()*4;
 		
 		switch (size) {
 		case 8:
 			return AbstractDataset.INT8;
 			
 		case 16:
 			return AbstractDataset.INT16;
 			
 		case 32:
 			if (type==Datatype.CLASS_INTEGER) return AbstractDataset.INT32;
 			if (type==Datatype.CLASS_FLOAT)   return AbstractDataset.FLOAT32;
			//$FALL-THROUGH$
 		case 64:
 			if (type==Datatype.CLASS_INTEGER) return AbstractDataset.INT64;
 			if (type==Datatype.CLASS_FLOAT)   return AbstractDataset.FLOAT64;
 		}
 		
 		return AbstractDataset.FLOAT;
 	}
 	
 	/**
	 * Gets a dataset from the complete dims.
 	 * @param val
 	 * @param set
	 * @return dataset
 	 * @throws Exception
 	 */
 	public static AbstractDataset getSet(final Object  val, final Dataset set) throws Exception {
 	    return H5Utils.getSet(val, set.getDims(), set);
 	}
 
 	/**
 	 * Used when dims are not the same as the entire set, for instance when doing a slice.
 	 * @param val
 	 * @param longShape
 	 * @param set
	 * @return dataset
 	 * @throws Exception
 	 */
 	public static AbstractDataset getSet(final Object  val, final long[] longShape, final Dataset set) throws Exception {
 
 		final int[] intShape  = getInt(longShape);
          
 		AbstractDataset ret = null;
         if (val instanceof byte[]) {
         	ret = new ByteDataset((byte[])val, intShape);
         } else if (val instanceof short[]) {
         	ret = new ShortDataset((short[])val, intShape);
         } else if (val instanceof int[]) {
         	ret = new IntegerDataset((int[])val, intShape);
         } else if (val instanceof long[]) {
         	ret = new LongDataset((long[])val, intShape);
         } else if (val instanceof float[]) {
         	ret = new FloatDataset((float[])val, intShape);
         } else if (val instanceof double[]) {
         	ret = new DoubleDataset((double[])val, intShape);
         } else {
         	throw new Exception("Cannot deal with data type "+set.getDatatype().getDatatypeDescription());
         }
         
 		if (set.getDatatype().isUnsigned()) {
 			switch (ret.getDtype()) {
 			case AbstractDataset.INT32:
 				ret = new LongDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 32);
 				break;
 			case AbstractDataset.INT16:
 				ret = new IntegerDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 16);
 				break;
 			case AbstractDataset.INT8:
 				ret = new ShortDataset(ret);
 				DatasetUtils.unwrapUnsigned(ret, 8);
 				break;
 			}
 		}
 
         return ret;
        
 	}
 
 	/**
 	 * Get a int[] from a long[]
 	 * @param longShape
	 * @return int shape
 	 */
 	public static int[] getInt(long[] longShape) {
 		final int[] intShape  = new int[longShape.length];
 		for (int i = 0; i < intShape.length; i++) intShape[i] = (int)longShape[i];
 		return intShape;
 	}
 
 	/**
 	 * Get a long[] from a int[]
	 * @param intShape
	 * @return long shape
 	 */
 	public static long[] getLong(int[] intShape) {
 		final long[] longShape  = new long[intShape.length];
		for (int i = 0; i < intShape.length; i++) longShape[i] = intShape[i];
 		return longShape;
 	}
 	/**
 	 * Determines the HDF5 Datatype for an abstract dataset.
 	 * @param a
	 * @return data type
 	 */
 	public static Datatype getDatatype(IDataset a) throws Exception {
 		
 		// There is a smarter way of doing this, but am in a hurry...
 		if (a instanceof ByteDataset || a instanceof BooleanDataset) {
          	return new H5Datatype(Datatype.CLASS_INTEGER, 8/8, Datatype.NATIVE, Datatype.SIGN_NONE);
          	
         } else if (a instanceof ShortDataset) {
         	return new H5Datatype(Datatype.CLASS_INTEGER, 16/8, Datatype.NATIVE, Datatype.NATIVE); 
         	
         } else if (a instanceof IntegerDataset) {
         	return new H5Datatype(Datatype.CLASS_INTEGER, 32/8, Datatype.NATIVE, Datatype.NATIVE); 
        	
         } else if (a instanceof LongDataset) {
         	return new H5Datatype(Datatype.CLASS_INTEGER, 64/8, Datatype.NATIVE, Datatype.NATIVE); 
         	
         } else if (a instanceof FloatDataset) {
         	return new H5Datatype(Datatype.CLASS_FLOAT, 32/8, Datatype.NATIVE, Datatype.NATIVE); 
         	
         } else if (a instanceof DoubleDataset) {
         	return new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE); 
       	    
         }
         
         throw new Exception("Cannot deal with data type "+a.getClass().getName());
 	}
 
 	/**
 	 * Appends a to a dataset of the same name as a and parent Group of parent.
 	 * 
 	 * @param file
 	 * @param parent
 	 * @param a
 	 * @throws Exception
 	 */
 	public static void appendDataset(IHierarchicalDataFile file, Group parent, AbstractDataset a) throws Exception {
 
 		long[] shape = H5Utils.getLong(a.getShape());
 		Dataset s = file.appendDataset(a.getName(),  H5Utils.getDatatype(a), shape, a.getBuffer(), parent);
 		file.setNexusAttribute(s, Nexus.SDS);			
 	}
 
 }
