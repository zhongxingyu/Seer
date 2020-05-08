 package ${package}.pages.list;
 
 /**
  * Abstract parent class for customized List pages.
  *
  * @param <T>
  */
 public abstract class CustomList<T> extends ${package}.pages.List {
 
 	public abstract Class<T> getType();
 
 	final protected void onActivate() throws Exception {
 		super.onActivate(getType());
 	}

	@Override
	final protected Object[] onPassivate() {
		return new Object[0];
	}
 }
