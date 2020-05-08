 /*
  * Icosahedron.java
  * TCSS 491 Computational Worlds
  * Phillip Cardon
  */
 package tesseract.objects;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.IndexedQuadArray;
 import javax.media.j3d.Material;
 import javax.media.j3d.PointArray;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Transform3D;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import com.sun.j3d.utils.geometry.GeometryInfo;
 import com.sun.j3d.utils.geometry.NormalGenerator;
 
 /**
  * Represents a Toroid, or donut like shape.
  * @author Phillip Cardon
  * @version 0.9a
  */
 public class Toroid extends PhysicalObject {
 	/**
 	 * @param position starting position.
 	 * @param mass of object.
 	 * @param scale mesh scale.
 	 * @param sliceRadius radius of slice "flesh."
 	 * @param sliceDivisions resolution of slice "flesh" circles.
 	 * @param arcRadius Radius of donut circle
 	 * @param arcDivisions resolution of slices on donut.
 	 */
 	public Toroid(final Vector3f position, final float mass, final float scale,
 			final float sliceRadius, final int sliceDivisions,
 			final float arcRadius, final int arcDivisions) {
 		super(position, mass);
 		
 		setShape(buildToroid(scale, sliceRadius, sliceDivisions,
 				arcRadius, arcDivisions));
 	}
 	
 	
 	/**
 	 * Creates donut.
 	 * @param sliceRadius radius of slice "flesh."
 	 * @param sliceDivisions resolution of slice "flesh" circles.
 	 * @param arcRadius Radius of donut circle
 	 * @param arcDivisions resolution of slices on donut.
 	 */
 	public Shape3D buildToroid(final float scale, final float sliceRadius, final int sliceDivisions,
 			final float arcRadius, final int arcDivisions) {
 		Point3f[][] coordinates = new Point3f[arcDivisions][sliceDivisions];
 		final float arcAngle = (float) (Math.PI * 2.0);
 		final float sliceDivisionAngle = 2 * (float) Math.PI / sliceDivisions;
 		
 		for (int i = 0; i < sliceDivisions; i++) {
 			coordinates[0][i] = new Point3f(sliceRadius 
 					* (float) Math.cos(i * sliceDivisionAngle), 0f, sliceRadius 
 					* (float) Math.sin(i * sliceDivisionAngle));	
 		}
 		
 		Transform3D trans3D = new Transform3D();
 		trans3D.setTranslation(new Vector3f(arcRadius, 0, 0));
 		Transform3D tmp = new Transform3D();
 		tmp.rotZ(-arcAngle / (arcDivisions - 1));
 		trans3D.mul(tmp);
 		tmp.setIdentity();
 		tmp.setTranslation(new Vector3f(-arcRadius, 0, 0));
 		trans3D.mul(tmp);
 		
 		for (int j = 1; j < arcDivisions; j++) {
 			for (int i = 0; i < sliceDivisions; i++) {
 				coordinates[j][i] = new Point3f(coordinates[j - 1][i]);
 				trans3D.transform(coordinates[j][i]);
 				coordinates[j][i].scale(scale);
 			}
 		}
 		
 		IndexedQuadArray geometry = new IndexedQuadArray(arcDivisions 
 				* sliceDivisions, PointArray.COORDINATES, 4 * sliceDivisions 
 				* (arcDivisions - 1));
 		for (int j = 0; j < arcDivisions; j++) {
 			geometry.setCoordinates(j * sliceDivisions, coordinates[j]);
 		}
 		
 		int index = 0;
 		int last = 0;
 		for (int j = 0; j < arcDivisions - 2; j++) {
 			for (int i = 0; i < sliceDivisions; i++) {
 				geometry.setCoordinateIndex(index++, j * sliceDivisions + i);
 				geometry.setCoordinateIndex(index++, (j + 1) * sliceDivisions 
 						+ i);
 				geometry.setCoordinateIndex(index++, (j + 1) * sliceDivisions 
 						+ (i + 1) % sliceDivisions);
 				geometry.setCoordinateIndex(index++, j * sliceDivisions 
 						+ (i + 1) % sliceDivisions);
 			}
 			last = j;
 		}
 		last++;
 		for (int i = 0; i < sliceDivisions; i++) {
 			geometry.setCoordinateIndex(index++, last * sliceDivisions + i);
 			geometry.setCoordinateIndex(index++, (0) * sliceDivisions + i);
 			geometry.setCoordinateIndex(index++, (0) * sliceDivisions + (i + 1) 
 					% sliceDivisions);
 			geometry.setCoordinateIndex(index++, last * sliceDivisions 
 					+ (i + 1) % sliceDivisions);
 		}
 		
 		
 		GeometryInfo gInfo = new GeometryInfo(geometry);
		new NormalGenerator().generateNormals(gInfo);
 		gInfo.convertToIndexedTriangles();
 		Shape3D shape = new Shape3D(gInfo.getGeometryArray());
 
 		Appearance app = new Appearance();
 		Material mat = new Material();
 		mat.setDiffuseColor(1, 0, 0);
 		app.setMaterial(mat);
 		shape.setAppearance(app);
 	
 		return shape;
 	}
 }
