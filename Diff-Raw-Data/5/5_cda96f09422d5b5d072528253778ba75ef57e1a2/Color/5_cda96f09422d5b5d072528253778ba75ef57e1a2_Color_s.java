 package frankversnel.processing.rendering.component;
 
 import frankversnel.processing.component.Component;
 import frankversnel.processing.gameobject.GameObject;
 
 public class Color extends Component {
 	private int r;
 	private int g;
 	private int b;
 
 	public Color(GameObject gameObject, int r, int g, int b) {
 		super(gameObject);
 		
 		assertColorValue(r);
 		assertColorValue(g);
 		assertColorValue(b);
 		
 		this.r = r;
 		this.g = g;
 		this.b = b;
 	}
 	
 	public int r() {
 		return this.r;
 	}
 	
 	public int g() {
 		return this.g;
 	}
 	
 	public int b() {
 		return this.b;
 	}
 	
	private void assertColorValue(int colorValue) {
		assert(colorValue >= 0 && colorValue <= 255);
 	}
 	
 	public static Color red(GameObject gameObject) {
 		return new Color(gameObject, 255, 0, 0);
 	}
 	public static Color green(GameObject gameObject) {
 		return new Color(gameObject, 0, 255, 0);
 	}
 	public static Color blue(GameObject gameObject) {
 		return new Color(gameObject, 0, 0, 255);
 	}
 
 }
