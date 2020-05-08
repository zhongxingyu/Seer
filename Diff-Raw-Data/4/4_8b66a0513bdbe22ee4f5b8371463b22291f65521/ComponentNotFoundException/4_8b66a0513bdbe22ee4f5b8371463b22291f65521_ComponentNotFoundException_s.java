 package frankversnel.processing.component;
 
 import frankversnel.processing.GameObject;
 
 public class ComponentNotFoundException extends Exception {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8863053240768908484L;
 
 	public <T extends Component>  ComponentNotFoundException(Class<T> componentType, 
 			GameObject gameObject) {
		super("Component of type " + componentType.getClass() + 
				" could not be fournd for " + gameObject);
 	}
 
 }
