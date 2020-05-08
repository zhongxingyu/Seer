 package feynstein.geometry;
 import feynstein.utilities.Vector3d;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Mesh {
 	private List<Particle> particles;
 	private List<Edge> edges;
 	private List<Triangle> triangles;
 	
 	public Mesh() {
		this.particles = new ArrayList<Particle>();
		this.edges = new ArrayList<Edge>();
		this.triangles = new ArrayList<Triangle>();
 	}
 	
 	public Mesh(List<Particle> particles, List<Edge> edges, 
 				List<Triangle> triangles) {
 		this.particles = particles;
 		this.edges = edges;
 		this.triangles = triangles;
 	}
 	
 	public List<Particle> getParticles() {
 		return particles;
 	}
 	
 	public List<Edge> getEdges() {
 		return edges;
 	}
 	
 	public List<Triangle> getTriangles() {
 		return triangles;
 	}
 	
 	public Vector3d getVert(int index) {
 		return particles.get(index).getPos();
 	}
 	
 	public void append(Mesh localMesh) {
 		// note: this will not work if we have mutliple shapes 
 		// (which we obviously will, because the indexes 
 		// need to be adjusted
 		// TODO: fix this
 		int shift = particles.size();
 		for (Particle p : localMesh.getParticles())
 			particles.add(p);
 		for (Edge e : localMesh.getEdges())
 			edges.add(new Edge(e.getIdx(0)+shift, e.getIdx(1)+shift));
 		for (Triangle t : localMesh.getTriangles())
 			triangles.add(new Triangle(t.getIdx(0)+shift, t.getIdx(1)+shift, t.getIdx(2)+shift));
 	}
 	
 }
