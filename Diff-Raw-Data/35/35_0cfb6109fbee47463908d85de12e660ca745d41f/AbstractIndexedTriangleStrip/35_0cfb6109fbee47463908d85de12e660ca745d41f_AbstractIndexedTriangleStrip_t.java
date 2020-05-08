 package kcsaba.vision.data.mesh;
 
 import java.util.List;
 import kcsaba.math.matrix.Vector3;
 
 /**
  * Abstract superclass for {@code IndexedTriangleStrip} objects implementing the {@code IndexedTriangleMesh}
  * and the {@code PointList} aspects.
  * @author Kazó Csaba
  */
 public abstract class AbstractIndexedTriangleStrip extends PointListImpl implements IndexedTriangleStrip {
 
 	/**
 	 * Creates a new instance backed by the specified list of vectors. The list is stored by reference and should not
 	 * be modified.
 	 * @param points the list of points
 	 * @throws NullPointerException if either the argument or an element of the list is {@code null}
 	 */
 	public AbstractIndexedTriangleStrip(List<Vector3> points) {
 		super(points);
 	}
 
 	@Override
 	public int getTriangleCount() {
 		int count=0;
 		for (int strip=0; strip<getStripCount(); strip++) {
 			count+=getStripLength(strip)-2;
 		}
 		return count;
 	}
 
 	@Override
 	public int getTrianglePointIndex(int triangle, int point) {
 		if (triangle<0) throw new IndexOutOfBoundsException();
 		if (point<0 || point>=3) throw new IndexOutOfBoundsException();
 		int trianglesInPreviousStrips=0;
 		for (int currentStrip=0; currentStrip<getStripCount(); currentStrip++) {
 			int trianglesInCurrentStrip=getStripLength(currentStrip)-2;
			if (triangle<trianglesInPreviousStrips+trianglesInCurrentStrip) {
				triangle=triangle-trianglesInPreviousStrips;
				boolean flip=(triangle % 2) == 0;
				if (flip) {
					if (point==2) point=1; else if (point==1) point=2;
				}
				return getStripPointIndex(currentStrip, triangle+point);
			}
 			trianglesInPreviousStrips+=trianglesInCurrentStrip;
 		}
 		throw new IndexOutOfBoundsException();
 	}
 
 	@Override
 	public Vector3 getTrianglePoint(int triangle, int point) {
 		return getPoint(getTrianglePointIndex(triangle, point));
 	}
 
 }
