 package kcsaba.vision.data.mesh;
 
 import java.util.List;
 import java.util.NoSuchElementException;
 import kcsaba.math.matrix.Vector3;
 
 /**
  * An implementation of {@code IndexedTriangleStrip} which stores the points in a list, and stores the
  * strips as lists of point indices.
  * @author Kazó Csaba
  */
 public class IndexedTriangleStripImpl extends AbstractIndexedTriangleStrip implements IndexedTriangleStrip {
 
 	private List<int[]> indices;
 	
 	private class StripPoints implements PointList {
 		private final int[] stripIndices;
 		public StripPoints(int index) {
 			stripIndices=indices.get(index);
 		}
 
 		@Override
 		public Vector3 getPoint(int index) {
 			return IndexedTriangleStripImpl.this.getPoint(stripIndices[index]);
 		}
 
 		@Override
 		public int getPointCount() {
 			return stripIndices.length;
 		}
 	}
 
 	/**
 	 * Creates a new instance with the specified data. Both lists will be stored
 	 * by reference. No further operations on these objects should be performed.
 	 * @param points the points of the mesh
 	 * @param strips the triangle strips specified by point index arrays
 	 * @throws NullPointerException if either argument or an element of either list is {@code null}
 	 * @throws IllegalArgumentException if an array in the triangle list has a length less than 3,
 	 * or if the strip list is empty
 	 * @throws IndexOutOfBoundsException if a point index is out of range
 	 */
 	public IndexedTriangleStripImpl(List<Vector3> points, List<int[]> strips) {
 		super(points);
 		if (strips.isEmpty()) throw new IllegalArgumentException();
 		for (int[] strip: strips) {
 			if (strip.length<3) throw new IllegalArgumentException();
 			for (int index: strip)
 				if (index<0 || index>=points.size()) throw new IndexOutOfBoundsException();
 		}
 		this.indices=strips;
 	}
 	
 	
 	
 	@Override
 	public int getStripPointIndex(int strip, int point) {
 		return indices.get(strip)[point];
 	}
 
 	@Override
 	public int getStripCount() {
 		return indices.size();
 	}
 
 	@Override
 	public PointList getStrip(int index) {
 		return new StripPoints(index);
 	}
 
 	@Override
 	public int getStripLength(int index) {
 		return indices.get(index).length;
 	}
 
 	@Override
 	public TriangleListIterator iterateTriangles() {
 		return new Iterator();
 	}
 	
 	private class Iterator implements TriangleListIterator {
 		private int stripCursor=-1;
 		private int pointCursor;
 		@Override
 		public boolean hasNext() {
 			return stripCursor<getStripCount()-1 || pointCursor+3<getStripLength(stripCursor);
 		}
 
 		@Override
 		public void next() {
 			if (!hasNext()) throw new NoSuchElementException();
 			if (stripCursor==-1 || pointCursor+3==getStripLength(stripCursor)) {
 				stripCursor++;
 				pointCursor=0;
			} else
 				pointCursor++;
 			
 		}
 
 		@Override
 		public Vector3 getV1() {
 			return getPoint(indices.get(stripCursor)[pointCursor]);
 		}
 
 		@Override
 		public Vector3 getV2() {
			return getPoint(indices.get(stripCursor)[pointCursor+1]);
 		}
 
 		@Override
 		public Vector3 getV3() {
			return getPoint(indices.get(stripCursor)[pointCursor+2]);
 		}
 		
 	}
 }
