 package org.jtrace.shader;
 
 import org.jtrace.Jay;
 import org.jtrace.Material;
 import org.jtrace.geometry.Sphere;
 import org.jtrace.primitives.ColorRGB;
 import org.jtrace.primitives.Point3D;
 import org.jtrace.primitives.ReflectanceCoefficient;
 import org.jtrace.primitives.Vector3D;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class AmbientShaderUnitTest {
 	
 	private static Point3D CENTER = new Point3D(0, 0, -10);
 	private static float RADIUS = 2.0f;
 	private static ReflectanceCoefficient K_AMBIENT = new ReflectanceCoefficient(0.2, 0.2, 0.2);
 	private static ReflectanceCoefficient K_DIFFUSE = new ReflectanceCoefficient(0.3, 0.3, 0.3);
 	private static Material MATERIAL = new Material(ColorRGB.BLUE, K_AMBIENT, K_DIFFUSE);
 	
 	private static Sphere SPHERE = new Sphere(CENTER, RADIUS, MATERIAL);
 	
 	private static Vector3D DIRECTION = new Vector3D(0, 0, -1);
 	private static Point3D ORIGIN = new Point3D(0, 0, 0);
 	
 	private static Jay JAY = new Jay(ORIGIN, DIRECTION);
 	
 	@Test
 	public void testShade() {
 		double red = ColorRGB.BLUE.getRed() * K_AMBIENT.getRed();
 		double green = ColorRGB.BLUE.getGreen() * K_AMBIENT.getGreen();
 		double blue = ColorRGB.BLUE.getBlue() * K_AMBIENT.getBlue();
 		ColorRGB expectedColor = new ColorRGB(red, green, blue);
 		
		Assert.assertEquals(expectedColor, new AmbientShader().shade(null, null, JAY, SPHERE));
 	}
 }
