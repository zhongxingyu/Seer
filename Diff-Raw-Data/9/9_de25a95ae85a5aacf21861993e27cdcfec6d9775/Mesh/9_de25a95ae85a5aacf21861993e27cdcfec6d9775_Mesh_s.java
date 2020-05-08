 package engine.render.model_pieces;
 
 import java.util.ArrayList;
 
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.opengl.GL11;
 
 import engine.render.ubos.Material;
 
 public class Mesh implements Cloneable {
 	private ArrayList<Face> faces;
 	private Material mat;
 	private Vector3f location;
 	private Vector3f forward;
 	private Vector3f up;
 
 	public Mesh() {
 		meshInit();
 	}
 
 	public Mesh(ArrayList<Face> _faces) {
 		meshInit();
 		for (Face f : _faces) {
 			this.faces.add(new Face(f));
 		}
 	}
 
 	public Mesh(Material m) {
 		meshInit();
 		this.mat = new Material(m);
 	}
 
 	public Mesh(ArrayList<Face> _faces, Material m) {
 		meshInit();
 		for (Face f : _faces) {
 			this.faces.add(new Face(f));
 		}
 		this.mat = new Material(m);
 	}
 
 	public Mesh(Vector3f loc) {
 		meshInit();
 		location.x = loc.x;
 		location.y = loc.y;
 		location.z = loc.z;
 	}
 
 	public Mesh(ArrayList<Face> _faces, Vector3f loc) {
 		meshInit();
 		for (Face f : _faces) {
 			this.faces.add(new Face(f));
 		}
 		location.x = loc.x;
 		location.y = loc.y;
 		location.z = loc.z;
 	}
 
 	public Mesh(Material m, Vector3f loc) {
 		meshInit();
 
 		mat = new Material(m);
 		location.x = loc.x;
 		location.y = loc.y;
 		location.z = loc.z;
 	}
 
 	public Mesh(ArrayList<Face> _faces, Material m, Vector3f loc) {
 		meshInit();
 
 		for (Face f : _faces) {
 			this.faces.add(new Face(f));
 		}
 		mat = new Material(m);
 		location = new Vector3f(loc.x, loc.y, loc.z);
 	}
 
 	public Mesh clone() {
 		try {
 			return (Mesh) super.clone();
 		} catch (CloneNotSupportedException e) {
 			// TODO Auto-generated catch block
 			throw new InternalError(e.toString());
 		}
 	}
 
 	// Copy Constructor
 	public Mesh(Mesh m) {
 		this.meshInit();
 
 		for (Face f : m.faces) {
 			this.faces.add(new Face(f));
 		}
 
 		this.location = new Vector3f(m.location.x, m.location.y, m.location.z);
 		this.forward = new Vector3f(m.forward.x, m.forward.y, m.forward.z);
 		this.up = new Vector3f(m.up.x, m.up.y, m.up.z);
 
 		this.mat = new Material(m.mat);
 	}
 
 	private void meshInit() {
 		faces = new ArrayList<Face>();
 		mat = new Material();
 		location = new Vector3f(0.0f, 0.0f, 0.0f);
 		forward = new Vector3f(0.0f, 0.0f, 1.0f);
 		up = new Vector3f(0.0f, 1.0f, 0.0f);
 	}
 
 	/* Setters */
 	public void setMaterial(Material m) {
 		mat = m;
 	}
 
 	public void addFace(Face f) {
 		faces.add(f);
 	}
 	
 	public void transform(Vector3f loc, Vector3f forw, Vector3f up)
 		throws Exception {
 		this.location = new Vector3f(loc.x, loc.y, loc.z);
 		this.forward = new Vector3f(forw.x, forw.y, forw.z);
 		this.up = new Vector3f(up.x, up.y, up.z);
 	}
 
 	/* Getters */
 	public Material getMaterial() {
 		return mat;
 	}
 
 	public Face getFace(int i) {
 		return faces.get(i);
 	}
 
 	public ArrayList<Face> getFaces() {
 		return faces;
 	}
 
 	public Vector3f getTransform() {
 		return location;
 	}
 	/*
 	public void drawMaterial() {
 		GL11.glMaterial(
 			GL11.GL_FRONT_AND_BACK, 
 			GL11.GL_AMBIENT,
 			mat.getAmbientAsBuffer()
 		);
 		GL11.glMaterial(
 			GL11.GL_FRONT_AND_BACK, 
 			GL11.GL_DIFFUSE,
 			mat.getDiffuseAsBuffer()
 		);
 		GL11.glMaterial(
 			GL11.GL_FRONT_AND_BACK, 
 			GL11.GL_SPECULAR,
 			mat.getSpecularAsBuffer()
 		);
 		GL11.glMaterial(
 			GL11.GL_FRONT_AND_BACK, 
 			GL11.GL_EMISSION,
 			mat.getEmissionAsBuffer()
 		);
 		GL11.glMaterialf(
 			GL11.GL_FRONT_AND_BACK, 
 			GL11.GL_SHININESS,
 			mat.getShine()
 		);
 	}
 	*/
 	public void draw() {
 		// Set Material
 		//drawMaterial();
 
 		// Transform
 		GL11.glTranslatef(location.x, location.y, location.z);
 
 		GL11.glBegin(GL11.GL_TRIANGLES);
 		// Draw faces
 		for (Face f : faces) {
 			f.draw();
 		}
 		GL11.glEnd();
 	}
 
 	/* Debug */
 	public String toString() {
 		String ret = "";
 		ret += "		location: (" + location.x + "," + location.y + ","	+ location.z + ")\n";
 		ret += "		forward: (" + forward.x + "," + forward.y + "," + forward.z	+ ")\n";
 		ret += "		up: (" + up.x + "," + up.y + "," + up.z + ")\n";
 		ret += "		Material{\n";
 		ret += mat.toString();
 		ret += "		}\n";
 		ret += "		# of Faces: " + faces.size() + "\n";
 		for (Integer i = 0; i < faces.size(); i++) {
 			ret += "		Face" + i.toString() + "{\n";
 			ret += faces.get(i).toString();
 			ret += "		}\n";
 		}
 		return ret;
 	}
 
 	public int getFaceCount() {
 		return faces.size();
 	}
 
 	public StringBuffer toXGLString(int ref) {
 		StringBuffer data = new StringBuffer();
 		data.append(mat.toXGLString(ref));
 		data.append("<MESH ID=\"" + ref + "\">");
 		for (int i = 0; i < faces.size(); i++) {
 			data.append(faces.get(i).toXGLString(i * 3, ref));
 		}
 		data.append("</MESH>");
 		data.append("<OBJECT>\n");
 		data.append("<TRANSFORM>\n");
 		data.append(
 			"<FORWARD>" + forward.x + ", " + forward.y + ", "
 			+ forward.z + "</FORWARD>\n"
 		);
 		data.append("<UP>" + up.x + ", " + up.y + ", " + up.z + "</UP>\n");
 		data.append(
 			"<POSITION>" + location.x + ", " + location.y + ", "
 			+ location.z + "</POSITION>\n"
 		);
 		data.append("</TRANSFORM>\n");
 		data.append("<MESHREF>" + ref + "</MESHREF>");
 		data.append("</OBJECT>\n");
 		return data;
 	}
 
 	public Vector3f getMaximums() {
 		Vector3f max = new Vector3f();
 		for (Face f : this.faces) {
 			if (f.getVertices().size() > 0) {
 				// find the max and min vertices for each dimension
 				if (f.getVertex(0).x > max.x) max.x = f.getVertex(0).x;
 				if (f.getVertex(1).x > max.x) max.x = f.getVertex(1).x;
 				if (f.getVertex(2).x > max.x) max.x = f.getVertex(2).x;
 
 				if (f.getVertex(0).y > max.y) max.y = f.getVertex(0).y;
 				if (f.getVertex(1).y > max.y) max.y = f.getVertex(1).y;
 				if (f.getVertex(2).y > max.y) max.y = f.getVertex(2).y;
 
 				if (f.getVertex(0).z > max.z) max.z = f.getVertex(0).z;
 				if (f.getVertex(1).z > max.z) max.z = f.getVertex(1).z;
 				if (f.getVertex(2).x > max.x) max.z = f.getVertex(2).z;
 			}
 		}
 		return max;
 	}
 
 	public Vector3f getMinimums() {
 		Vector3f min = new Vector3f();
 		for (Face f : this.faces) {
 			if (f.getVertices().size() > 0) {
 				if (f.getVertex(0).x < min.x) min.x = f.getVertex(0).x;
 				if (f.getVertex(1).x < min.x) min.x = f.getVertex(1).x;
 				if (f.getVertex(2).x < min.x) min.x = f.getVertex(2).x;
 
 				if (f.getVertex(0).y < min.y) min.y = f.getVertex(0).y;
 				if (f.getVertex(1).y < min.y) min.y = f.getVertex(1).y;
 				if (f.getVertex(2).y < min.x) min.y = f.getVertex(2).y;
 
 				if (f.getVertex(0).z < min.z) min.z = f.getVertex(0).z;
 				if (f.getVertex(1).z < min.z) min.z = f.getVertex(1).z;
 				if (f.getVertex(2).z < min.z) min.z = f.getVertex(2).z;
 			}
 		}
 		return min;
 	}
 
 	public void calcNormals(boolean overwrite_existing) {
 
 		//Clear the existing normals if overwriting is enabled
 		//other wise bailout
 		if(overwrite_existing) {
 			for(Face f : faces) {
				for(int i=0; i < (f.getVertices().size()-f.getNormals().size()) ; i++) {
 					f.addVertexNorm(new Vector3f(0,0,0));
 				}
 				for(Vector3f normal: f.getNormals()) {
 					normal.set(0,0,0);
 				}					
 			}
 			
 			for (Face f : faces) {
 				if (f.getNormals().size() <= 0 || overwrite_existing) {
 					// Copy the verts so we don't scrub the originals
 					// with our math
 					Vector3f 
 						vertex0 = new Vector3f(f.getVertex(0)), 
 						vertex1 = new Vector3f(f.getVertex(1)), 
 						vertex2 = new Vector3f(f.getVertex(2)), 
 						line1 = new Vector3f(), 
 						line2 = new Vector3f(), 
 						normal_vert = new Vector3f();
 
 					// Find two vectors so we can get the orientation
 					// of the face
 					line1.sub(vertex0, vertex2);
 					line2.sub(vertex0, vertex1);
 					normal_vert.cross(line1, line2);
 	
 					for (Vector3f normal: f.getNormals()) {
 						normal.add(normal_vert);
 					}
 				}
 			}
 
 			//Post normalization of all normals
 			for(Face f : faces) {
 				for(Vector3f normal: f.getNormals()) {
 					normal.normalize();
 				}
 			}			
 		}
 	}
 }
