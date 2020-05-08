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
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 
 import java.util.Arrays;
 
 /**
  * Class to run over a slice of a dataset
  */
 public class SliceIterator extends IndexIterator {
 	final int[] shape;
 	final int isize;
 	final int endrank; // last shape index
 	final int[] gap; // gaps in dataset
 	final int imax; // maximum index in array
 	final int[] start;
 	final int[] stop;
 	final int[] step;
 	final int[] sshape; // slice shape
 	final int[] pos; // position in dataset
 	final int istep; // step in last index
 
 	/**
 	 * Constructor for an iterator over the elements of a sliced dataset
 	 * 
 	 * @param shape or dataShape
 	 * @param length of entire data array
 	 * @param sshape shape of new dataset, i.e. slice
 	 */
 	public SliceIterator(final int[] shape, final int length, final int[] sshape) {
 		this(shape, length, null, null, sshape, 1);
 	}
 
 	/**
 	 * Constructor for an iterator over the elements of a sliced dataset
 	 * 
 	 * @param shape or dataShape
 	 * @param length of entire data array
 	 * @param start (if null then equivalent to the origin)
 	 * @param sshape shape of new dataset, i.e. slice
 	 */
 	public SliceIterator(final int[] shape, final int length, final int[] start, final int[] sshape) {
 		this(shape, length, start, null, sshape, 1);
 	}
 
 	/**
 	 * Constructor for an iterator over the elements of a sliced dataset
 	 * 
 	 * @param shape or dataShape
 	 * @param length of entire data array
 	 * @param sshape shape of new dataset, i.e. slice
 	 * @param isize number of elements in an item
 	 */
 	public SliceIterator(final int[] shape, final int length, final int[] sshape, final int isize) {
 		this(shape, length, null, null, sshape, isize);
 	}
 
 	/**
 	 * Constructor for an iterator over the elements of a sliced dataset
 	 * 
 	 * @param shape or dataShape
 	 * @param length of entire data array
 	 * @param start (if null then equivalent to the origin)
 	 * @param sshape shape of new dataset, i.e. slice
 	 * @param isize number of elements in an item
 	 */
 	public SliceIterator(final int[] shape, final int length, final int[] start, final int[] sshape, final int isize) {
 		this(shape, length, start, null, sshape, isize);
 	}
 
 	/**
 	 * Constructor for an iterator over the elements of a sliced dataset
 	 * 
 	 * @param shape or dataShape
 	 * @param length of entire data array
 	 * @param start (if null then equivalent to the origin)
 	 * @param step (cannot contain zeros, if null then equivalent to ones)
 	 * @param sshape shape of new dataset, i.e. slice
 	 */
 	public SliceIterator(final int[] shape, final int length, final int[] start, final int[] step, final int[] sshape) {
 		this(shape, length, start, step, sshape, 1);
 	}
 
 	/**
 	 * Constructor for an iterator over the elements of a sliced dataset
 	 * 
 	 * @param shape or dataShape
 	 * @param length of entire data array
 	 * @param start (if null then equivalent to the origin)
 	 * @param step (cannot contain zeros, if null then equivalent to ones)
 	 * @param sshape shape of new dataset, i.e. slice
 	 * @param isize number of elements in an item
 	 */
 	public SliceIterator(final int[] shape, final int length, final int[] start, final int[] step, final int[] sshape, final int isize) {
 		this.isize = isize;
 		int rank = shape.length;
 		endrank = rank - 1;
 		this.shape = shape;
 		this.start = new int[rank];
 		this.sshape = sshape;
 		if (step == null) {
 			this.step = new int[rank];
 			Arrays.fill(this.step, 1);
 		} else {
 			this.step = step;
 		}
 
 		if (rank == 0) {
 			istep = isize;
 			imax = length*isize;
 			stop = new int[0];
 			pos = new int[0];
 			gap = null;
 		} else {
 			istep = this.step[endrank] * isize;
 			imax = length * isize;
 			stop = new int[rank];
 			gap = new int[endrank + 1];
 			pos = new int[rank];
 			calcGap();
 		}
 
 		setStart(start);
 	}
 
 	private void calcGap() {
 		int chunk = isize;
 		for (int i = endrank; i >= 0; i--) {
 			stop[i] = start[i] + sshape[i] * step[i];
 	
 			if (step[i] < 0)
 				stop[i]++; // adjust for -ve steps so later code has more succinct test
 	
 			if (i > 0) {
 				gap[i] = (shape[i] * step[i - 1] - sshape[i] * step[i]) * chunk;
 				chunk *= shape[i];
 			}
 		}
 	}
 
 	/**
 	 * Set start (prefix with zeros if necessary)
 	 * @param nstart if null, then treat as origin
 	 */
 	public void setStart(int... nstart) {
 		final int rank = shape.length;
		if (rank == 0) {
			index = -istep;
 			return;
		}
 
 		if (nstart == null) {
 			for (int i = 0; i < rank; i++) {
 				start[i] = 0;
 			}
 		} else if (nstart.length > rank) {
 			throw new IllegalArgumentException("Length of start array greater than rank");
 		} else {
 			int extra = rank - nstart.length;
 			for (int i = 0; i < extra; i++) {
 				start[i] = 0;
 			}
 			for (int i = 0; i < nstart.length; i++) {
 				start[i+extra] = nstart[i];
 			}
 		}
 
 		reset();
 		calcGap();
 	}
 
 	@Override
 	public void reset() {
 		if (shape.length == 0) {
 			index = -istep;
 		} else {
 			// work out index of first position
 			for (int i = 0; i < shape.length; i++)
 				pos[i] = start[i];
 			pos[endrank] -= step[endrank];
 	
 			index = pos[0];
 			for (int j = 1; j <= endrank; j++)
 				index = index * shape[j] + pos[j];
 			index *= isize;
 		}
 	}
 
 	@Override
 	public boolean hasNext() {
 		// now move on one position in slice
 		int j = endrank;
 		for (; j >= 0; j--) {
 			pos[j] += step[j];
 
 			if ((pos[j] >= stop[j]) == (step[j] > 0)) { // stop index has been adjusted in code for -ve steps
 				pos[j] = start[j];
 				index += gap[j];
 			} else {
 				break;
 			}
 		}
 		if (j == -1 && endrank >= 0) {
 			index = imax;
 			return false;
 		}
 
 		index += istep;
 		return index < imax;
 	}
 
 	public int[] getStart() {
 		return start;
 	}
 
 	@Override
 	public int[] getPos() {
 		return pos;
 	}
 
 	public int[] getStop() {
 		return stop;
 	}
 
 	public int[] getStep() {
 		return step;
 	}
 
 	public int[] getGap() {
 		return gap;
 	}
 
 	public int[] getShape() {
 		return shape;
 	}
 
 	public int[] getSliceShape() {
 		return sshape;
 	}
 }
