 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 import java.util.ArrayList;
 
 public class Triangle extends GraphicObject {
 
 	ArrayList<Point3d> vertices;
 	Shader shader = null;
 
 	public Triangle(ArrayList<Point3d> vertices, Color ambc)
 	{
        this(vertices, ambc, 0.2, 0.4, 0.6, 20, 0.0, 0.0, 0);
     }
     
     public Triangle(ArrayList<Point3d> vertices, Color ambc, int shaderIndex)
     {
        this(vertices, ambc, 0.2, 0.4, 0.6, 20, 0.0, 0.0, shaderIndex);
     }
 
     /*
 	    Creates a Sphere object with the given vertices, surface normal, ambient color,
 	    ambient light, diffuse light, specular light, the exponent, reflectivity,
 	    transparency, and a shaderIndex.
 	    
 	    ka - Ambient Lighting (0-1). Amount of background light
 	    kd - Diffuse Lighting (0-1). Lambertian Reflection
 	    ks - Specular Lighting (0-1). Mirror-like Reflection
 	    ke - Exponent (20-100. Needs to be EVEN) - Controls size of Specular Highlight
 	    kr - Reflectivity (0-1). 0=Non-Reflective 1=Reflective
 	    kt - Transparency (0-1). 0=Non-Transparent 1=Transparent
 	    shaderIndex - 0=No Shader 1=Checkerboard 2=PolkaDot 3=Ripple
 	*/
     public Triangle(ArrayList<Point3d> vertices, Color ambc, double ka, double kd, double ks, int ke, double kr, double kt, int shaderIndex)
 	{
 		this.vertices = vertices;
 		this.ambcolor = ambc;
 		this.ka = ka;
 		this.kd = kd;
 		this.ks = ks;
         if ( (ke & 1) == 0 ) {
             this.ke = (double)ke;
         } else {
             this.ke = (double)ke+1.0;
         }
 		this.kr = kr;
 		this.kt = kt;
 		
         Vector3d p3p2 = new Vector3d(vertices.get(2).x-vertices.get(1).x, vertices.get(2).y-vertices.get(1).y, vertices.get(2).z-vertices.get(1).z);
         Vector3d p1p2 = new Vector3d(vertices.get(0).x-vertices.get(1).x, vertices.get(0).y-vertices.get(1).y, vertices.get(0).z-vertices.get(1).z);
         this.normal = new Vector3d();
 		this.normal.cross(p3p2,p1p2);
 		this.normal.normalize();
 		
 		if ( shaderIndex == 1 ) {
     		//Checker Board Shader
             this.shader = new CheckBoardShad(this.vertices.get(1), new Color(0.0, 0.0, 0.0), 
                                                     new Color(500.0, 500.0, 500.0), 1.5);
 		} else if ( shaderIndex == 2 ) {
     		//Polka Dot Shader
             this.shader = new PolkaDotShad(new Point3d(-2.25, 0.0, 0.0), new Point3d(1.25, 0.0, -10.0), 
                                         new Color(50.0, 100.0, 200.0), new Color(255.0, 255.0, 0.0),
                                         new Color(200.0, 0.0, 0.0), 0.6, 0.25);
         } else if ( shaderIndex == 3 ) {
     		//Ripple Shader
             this.shader = new RippleShad(new Point3d(0.0, 0.0, -5.0));
         }
     }
 
 
 	public Point3d intersect(Ray r) 
 	{
 	    double ntd = normal.dot(r.direction);
         if ( ntd > 0 ) {
             return null;
         } else {
             Vector3d rOriginVec = new Vector3d(r.origin.x, r.origin.y, r.origin.z);
             Vector3d p1 = new Vector3d(vertices.get(0).x, vertices.get(0).y, vertices.get(0).z);
             double t = -(normal.dot(rOriginVec) - normal.dot(p1)) / ntd;
             Point3d poi = new Point3d(r.origin.x+t*r.direction.x, r.origin.y+t*r.direction.y, r.origin.z+t*r.direction.z);
             Vector3d l1 = new Vector3d(vertices.get(0).x-poi.x, vertices.get(0).y-poi.y, vertices.get(0).z-poi.z);
             l1.normalize();
             Vector3d l2 = new Vector3d(vertices.get(1).x-poi.x, vertices.get(1).y-poi.y, vertices.get(1).z-poi.z);
             l2.normalize();
             Vector3d l3 = new Vector3d(vertices.get(2).x-poi.x, vertices.get(2).y-poi.y, vertices.get(2).z-poi.z);
             l3.normalize();
             double total = Math.acos(l1.dot(l2)) + Math.acos(l2.dot(l3)) + Math.acos(l3.dot(l1));
             double absPiTotal = 2*Math.PI - total;
             if (absPiTotal < 0) {
                 absPiTotal *= -1;
             }
             if(absPiTotal < .0001){ //point is inside plane
                 return poi;
             } else { //point is outside plane
                 return null;
             }
         }
 	}
 	
 	public Color getColor(Point3d point) {
 		Color result;
 		if(shader != null) {
 			result = shader.shade(point);
 		}
 		else
 			result = this.ambcolor;
 		return result;
 	}
 
 } 
