 package edu.washington.cs.games.ktuite.pointcraft;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 /* these primitives built out of pellets...
  * keep a list of pellets and then draw lines or polygons between them.
  */
 public class Primitive {
 	private int gl_type;
 	private List<Pellet> vertices;
 	private float line_width = 5f;
 	private byte[] textureData = null;
 	private Texture texture = null;
 	private Vector3f player_position;
 	private Vector3f player_viewing_direction;
 
 	public Primitive(int _gl_type, List<Pellet> _vertices) {
 		gl_type = _gl_type;
 		vertices = _vertices;
 		if (gl_type == GL_POLYGON) {
 			System.out.println("making new polygon Primitive");
 			System.out.println("number of vertices: " + vertices.size());
 		}
 	}
 
 	public Primitive(int _gl_type, List<Pellet> _vertices, float _line_width) {
 		gl_type = _gl_type;
 		vertices = _vertices;
 		line_width = _line_width;
 	}
 
 	public void setPlayerPositionAndViewingDirection(Vector3f pos, Vector3f view) {
 		player_position = new Vector3f(pos);
 		player_viewing_direction = new Vector3f(view);
 		player_viewing_direction.normalise();
		// startDownloadingTexture();
 	}
 
 	public boolean isPolygon() {
 		if (gl_type == GL_POLYGON)
 			return true;
 		else
 			return false;
 	}
 
 	public int numVertices() {
 		return vertices.size();
 	}
 
 	public void draw() {
 		if (gl_type == GL_LINES) {
 			glColor3f(0, 0, 0);
 			glLineWidth(line_width);
 		} else if (gl_type == GL_POLYGON) {
			glColor4f(.9f, .9f, .4f, .9f);
 			if (texture != null) {
 				glEnable(GL_TEXTURE_2D);
 				texture.bind();
 			} else {
 				glDisable(GL_TEXTURE_2D);
 				if (textureData != null) {
 					try {
 						texture = TextureLoader.getTexture("PNG",
 								new ByteArrayInputStream(textureData));
 						textureData = null;
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 		}
 
 		Vector2f[] tex_coords = new Vector2f[] { new Vector2f(0, 0),
 				new Vector2f(1, 0), new Vector2f(1, 1), new Vector2f(0, 1) };
 
 		glBegin(gl_type);
 		for (int i = 0; i < vertices.size() && i < tex_coords.length; i++) {
 			Pellet pellet = vertices.get(i);
 			Vector3f vertex = pellet.pos;
 			glTexCoord2f(tex_coords[i].x, tex_coords[i].y);
 			glVertex3f(vertex.x, vertex.y, vertex.z);
 		}
 		glEnd();
 	}
 
 	public void startDownloadingTexture() {
 		String url_string = "http://mazagran.cs.washington.edu:8080/texture.png?&v=";
 		for (Pellet p : vertices) {
 			Vector3f v = p.pos;
 			url_string += v.x + "," + v.y + "," + v.z + ",";
 		}
 		url_string += "garbage,&w=128,&h=128,";
 		if (player_position != null && player_viewing_direction != null) {
 			url_string += "&p=" + player_position.x + "," + player_position.y
 					+ "," + player_position.z + ",";
 			url_string += "&e=" + player_viewing_direction.x + ","
 					+ player_viewing_direction.y + ","
 					+ player_viewing_direction.z + ",";
 		}
 		System.out.println(url_string);
 		final String final_url_string = url_string;
 		new Thread() {
 			public void run() {
 
 				try {
 					URL url = new URL(final_url_string);
 					InputStream is = url.openStream();
 					ByteArrayOutputStream baos = new ByteArrayOutputStream();
 					byte[] bytes = new byte[4096];
 					int n;
 					while ((n = is.read(bytes)) != -1) {
 						baos.write(bytes, 0, n);
 					}
 					textureData = baos.toByteArray();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	public String plyFace() {
 		String s = vertices.size() - 2 + "";
 		for (int i = 0; i < vertices.size() - 2; i++) {
 			Pellet pellet = vertices.get(i);
 			s += " " + Main.all_pellets_in_world.indexOf(pellet);
 		}
 		s += "\n";
 		System.out.println("PLYFACE:" + s);
 		return s;
 	}
 
 	public void printTriangleVertices() {
 		for (int i = 0; i < vertices.size() - 2; i++) {
 			Pellet pellet = vertices.get(i);
 			System.out.println(pellet.pos.x + " " + pellet.pos.y + " "
 					+ pellet.pos.z);
 		}
 		System.out.println("");
 	}
 }
