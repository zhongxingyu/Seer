 /*******************************************************************************
  * Copyright 2012 Josh Attenberg. Not for re-use or redistribution.
  ******************************************************************************/
 package com.parallax.ml.vector;
 
 import static com.google.common.base.Preconditions.checkElementIndex;
 import gnu.trove.iterator.TIntDoubleIterator;
 import gnu.trove.map.hash.TIntDoubleHashMap;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import com.google.common.collect.Sets;
 import com.parallax.ml.vector.util.VectorUtils;
 
 /**
  * TrovePrimativeMapLinearVector
  * 
  * @author Josh Attenberg
  */
 class TrovePrimativeMapLinearVector extends AbstractLinearVector {
 	private static final long serialVersionUID = -1808164285864302340L;
 	private TIntDoubleHashMap w;
 
 	TrovePrimativeMapLinearVector(int bins) {
 		super(bins);
 		w = new TIntDoubleHashMap();
 	}
 
 	@Override
 	public void delete(int index) {
 		checkElementIndex(index, numRows);
 		if (w.contains(index))
 			w.remove(index);
 	}
 
 	@Override
 	public Set<Integer> getFeatureIndicies() {
 		Set<Integer> out = Sets.newHashSet();
		for (int i : w.keys())
			out.add(i);
 		return out;
 	}
 
 	@Override
 	public double getValue(int index) {
 		checkElementIndex(index, numRows);
 		return w.contains(index) ? w.get(index) : 0;
 	}
 
 	@Override
 	public void resetValue(int index, double value) {
 		checkElementIndex(index, numRows);
 		if (!VectorUtils.floatingPointEquals(value, 0))
 			w.put(index, value);
 		else
 			w.remove(index);
 	}
 
 	@Override
 	public void updateValue(int index, double value) {
 		checkElementIndex(index, numRows);
 		w.put(index, w.contains(index) ? (value + w.get(index)) : value);
 		if (VectorUtils.floatingPointEquals(w.get(index), 0))
 			w.remove(index);
 	}
 
 	@Override
 	public Iterator<Integer> iterator() {
 		return new TroveMapIterator(w.iterator());
 	}
 
 	private class TroveMapIterator implements Iterator<Integer> {
 		private TIntDoubleIterator iter;
 
 		TroveMapIterator(TIntDoubleIterator iter) {
 			this.iter = iter;
 		}
 
 		@Override
 		public boolean hasNext() {
 			return iter.hasNext();
 		}
 
 		@Override
 		public Integer next() {
 			iter.advance();
 			return iter.key();
 		}
 
 		@Override
 		public void remove() {
 			iter.remove();
 		}
 
 	}
 
 	@Override
 	public void initW(double param) {
 		w = new TIntDoubleHashMap();
 		if (!VectorUtils.floatingPointEquals(0, param))
 			for (int i = 0; i < numRows; i++)
 				w.put(i, param);
 	}
 }
