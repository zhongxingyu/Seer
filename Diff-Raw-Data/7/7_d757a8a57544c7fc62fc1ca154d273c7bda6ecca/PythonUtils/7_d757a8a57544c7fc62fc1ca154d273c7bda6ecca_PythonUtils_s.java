 /*-
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
 
 package uk.ac.diamond.scisoft.python;
 
 import java.lang.reflect.Array;
 import java.util.List;
 
 import org.apache.commons.math3.complex.Complex;
 import org.python.core.Py;
 import org.python.core.PyArray;
 import org.python.core.PyComplex;
 import org.python.core.PyEllipsis;
 import org.python.core.PyException;
 import org.python.core.PyInteger;
 import org.python.core.PyNone;
 import org.python.core.PyObject;
 import org.python.core.PySequence;
 import org.python.core.PySequenceList;
 import org.python.core.PySlice;
 import org.python.core.PyString;
 import org.python.core.PyTuple;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Slice;
 
 /**
  * Class of utilities for interfacing with Jython
  */
 public class PythonUtils {
 
 	/**
 	 * Convert tuples/lists of tuples/lists to Java lists of lists. Also convert complex numbers to Apache Commons
 	 * version and Jython strings to strings
 	 * 
 	 * @param obj
 	 * @return converted object
 	 */
 	public static Object convertToJava(Object obj) {
 		if (obj == null || obj instanceof PyNone)
 			return null;
 
 		if (obj instanceof PySequenceList) {
 			obj = ((PySequenceList) obj).toArray();
 		}
 
 		if (obj instanceof PyArray) {
 			obj = ((PyArray) obj).getArray();
 		}
 
 		if (obj instanceof List<?>) {
 			@SuppressWarnings("unchecked")
 			List<Object> jl = (List<Object>) obj;
 			int l = jl.size();
 			for (int i = 0; i < l; i++) {
 				Object lo = jl.get(i);
 				if (lo instanceof PyObject) {
 					jl.set(i, convertToJava(lo));
 				}
 			}
 			return obj;
 		}
 
 		if (obj.getClass().isArray()) {
 			int l = Array.getLength(obj);
 			for (int i = 0; i < l; i++) {
 				Object lo = Array.get(obj, i);
 				if (lo instanceof PyObject) {
 					Array.set(obj, i, convertToJava(lo));
 				}
 			}
 			return obj;
 		}
 
 		if (!(obj instanceof PyObject))
 			return obj;
 
 		if (obj instanceof PyComplex) {
 			PyComplex z = (PyComplex) obj;
 			return new Complex(z.real, z.imag);
 		} else if (obj instanceof PyString) {
 			return obj.toString();
 		}
 
 		return ((PyObject) obj).__tojava__(Object.class);
 	}
 
 	static class SliceData {
 		Slice[] slice;  // slices
 		boolean[] sdim; // flag which dimensions are sliced
 		int[] axes;  // new axes
 	}
 
 	/**
 	 * Convert an array of python slice objects to a slice array
 	 * 
 	 * @param indexes
 	 * @param shape
 	 * @return slice array
 	 */
 	private static SliceData convertPySlicesToSlice(final PyObject indexes, final int[] shape) {
 		PyObject indices[] = (indexes instanceof PyTuple) ? ((PyTuple) indexes).getArray() : new PyObject[] { indexes };
 
 		int orank = shape.length;
 		SliceData slice = new SliceData();
 
 		int na = 0; // count new axes
 		int nc = 0; // count collapsed dimensions
 		int ns = 0; // count slices or ellipses
 		for (int j = 0; j < indices.length; j++) {
 			PyObject index = indices[j];
 			if (index instanceof PyNone)
 				na++;
 			else if (index instanceof PyInteger)
 				nc++;
 			else if (index instanceof PySlice)
 				ns++;
 			else if (index instanceof PyEllipsis)
 				ns++;
 		}
 
 		int spare = orank - nc - ns; // number of spare dimensions
 		slice.slice = new Slice[orank];
 		slice.axes = new int[na];
 		slice.sdim = new boolean[orank]; // flag which dimensions are sliced
 
 		boolean hasEllipse = false;
 		int i = 0;
 		int a = 0; // new axes
 		int c = 0; // collapsed dimensions
 		for (int j = 0; i < orank && j < indices.length; j++) {
 			PyObject index = indices[j];
 			if (index instanceof PyEllipsis) {
 				slice.sdim[i] = true;
 				slice.slice[i++] = null;
 				if (!hasEllipse) { // pad out with full slices on first ellipse
 					hasEllipse = true;
 					for (int k = 0; k < spare; k++) {
 						slice.sdim[i] = true;
 						slice.slice[i++] = null;
 					}
 				}
 			} else if (index instanceof PyInteger) {
 				int n = ((PyInteger) index).getValue();
 				if (n < -shape[i] || n >= shape[i]) {
 					throw new PyException(Py.IndexError);
 				}
 				if (n < 0) {
 					n += shape[i];
 				}
 				slice.sdim[i] = false; // nb specifying indexes whilst using slices will reduce rank
 				slice.slice[i++] = new Slice(n, n + 1);
 				c++;
 			} else if (index instanceof PySlice) {
 				PySlice pyslice = (PySlice) index;
 				slice.sdim[i] = true;
 				slice.slice[i++] = new Slice(pyslice.start instanceof PyNone ? null : ((PyInteger) pyslice.start).getValue(),
 						pyslice.stop instanceof PyNone ? null : ((PyInteger) pyslice.stop).getValue(),
 						pyslice.step instanceof PyNone ? null : ((PyInteger) pyslice.step).getValue());
 			} else if (index instanceof PyNone) { // newaxis
 				slice.axes[a++] = (hasEllipse ? j + spare : j) - c;
 			} else {
 				throw new IllegalArgumentException("Unexpected item in indexing");
 			}
 		}
 
 		assert nc == c;
 		while (i < orank) {
 			slice.sdim[i++] = true;
 		}
 		while (a < na) {
 			slice.axes[a] = i - c + a;
 			a++;
 		}
 
 		return slice;
 	}
 
 	/**
 	 * Jython method to get slice within a dataset
 	 * 
 	 * @param a
 	 *            dataset
 	 * @param indexes
 	 *            can be a mixed array of integers or slices
 	 * @return dataset of specified sub-dataset
 	 */
 	public static IDataset getSlice(final ILazyDataset a, final PyObject indexes) {
 		int[] shape = a.getShape();
 		int orank = shape.length;
 
 		SliceData slice = convertPySlicesToSlice(indexes, shape);
		IDataset dataSlice = DatasetUtils.convertToAbstractDataset(a).getSliceView(slice.slice);
 
 		// removed dimensions that were not sliced (i.e. that were indexed with an integer)
 		int rank = 0;
 		for (int i = 0; i < orank; i++) {
 			if (slice.sdim[i])
 				rank++;
 		}
 
 		if (rank < orank) {
 			int[] oldShape = dataSlice.getShape();
 			int[] newShape = new int[rank];
 			int j = 0;
 			for (int i = 0; i < orank; i++) {
 				if (slice.sdim[i]) {
 					newShape[j++] = oldShape[i];
 				}
 			}
 			dataSlice.setShape(newShape);
 		}
 
 		int n = slice.axes.length;
 		if (n > 0) {
 			int[] oldShape = dataSlice.getShape();
 			int[] newShape = new int[rank + n];
 			int j = 0;
 			int k = 0;
 			for (int i = 0; i < newShape.length; i++) {
 				if (k < n && i == slice.axes[k]) {
 					k++;
 					newShape[i] = 1;
 				} else {
 					newShape[i] = oldShape[j++];
 				}
 			}
 			dataSlice.setShape(newShape);
 		}
 		return dataSlice;
 	}
 
 	/**
 	 * Jython method to set slice within a dataset
 	 * 
 	 * @param a
 	 *            dataset
 	 * @param object
 	 *            can an item or a dataset
 	 * @param indexes
 	 *            can be a mixed array of integers or slices
 	 */
 	public static void setSlice(AbstractDataset a, Object object, final PyObject indexes) {
 		if (a.isComplex() || a.getElementsPerItem() == 1) {
 			if (object instanceof PySequence) {
 				object = AbstractDataset.array(object, a.getDtype());
 			}
 		}
 
 		int[] shape = a.getShape();
 		SliceData slice = convertPySlicesToSlice(indexes, shape);
 		a.setSlice(object, slice.slice);
 	}
 }
