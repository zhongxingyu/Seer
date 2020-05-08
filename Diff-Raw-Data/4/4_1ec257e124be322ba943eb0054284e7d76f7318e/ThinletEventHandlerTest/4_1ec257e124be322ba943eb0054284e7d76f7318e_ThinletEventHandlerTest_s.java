 package net.frontlinesms.test.ui;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import thinlet.Thinlet;
 import net.frontlinesms.FrontlineSMSConstants;
 import net.frontlinesms.test.spring.ApplicationContextAwareTestCase;
 import net.frontlinesms.ui.ThinletUiEventHandler;
 
 import static thinlet.Thinlet.*;
 
 public abstract class ThinletEventHandlerTest<E extends ThinletUiEventHandler> extends ApplicationContextAwareTestCase  {
 	protected TestFrontlineUI ui;
 	/** event handler instance under test */
 	protected E h;
 	/** root UI component that this handler is controlling */
 	private Object rootComponent;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		initUiForTests();
 	}
 	
 	/** Initialise fields of this class for testing.  This is called as part of {@link #setUp()},
 	 * but it may be useful to call it again later if e.g. UI needs to be reinitialised after
 	 * data mocking/fixtures are in place. */
 	protected void initUiForTests() {
 		DEFAULT_ENGLISH_BUNDLE = new MostlyKeyReturningMap(
 				FrontlineSMSConstants.DATEFORMAT_YMD, /* -> */ "d/M/yy");
 		ui = new TestFrontlineUI();
 		h = initHandler();
 		rootComponent = getRootComponent();
 		if(Thinlet.getClass(rootComponent).equals(DIALOG)) {
 			ui.add(rootComponent);
 		}
 	}
 
 	protected abstract E initHandler();
 	protected abstract Object getRootComponent();
 
 	protected void waitForUiEvents() {
 		new BlockingFrontlineUiUpdateJob() { public void run() {} }.execute();
 	}
 	
 //> UI INTERACTION METHODS/CLASSES
 	protected ThinletComponent $() {
 		return new RealThinletComponent(getClass().getSimpleName() + ":rootComponent", ui, rootComponent);
 	}
 	
 	/**
 	 * Finds a component with the given name.  Any descendant of {@link #rootComponent} will
 	 * be prioritised, but if none is found within {@link #rootComponent}, the whole UI will
 	 * be searched.
 	 * @param componentName
 	 * @return
 	 */
 	protected ThinletComponent $(String componentName) {
 		Object component = find(rootComponent, componentName);
 		if(component == null) component = ui.find(componentName);
 		if(component == null) return new MissingThinletComponent(componentName);
 		else return new RealThinletComponent(componentName, ui, component);
 	}
 }
 
 class MostlyKeyReturningMap implements Map<String, String> {
 	private final HashMap<String, String> someValues = new HashMap<String, String>();
 	
 	MostlyKeyReturningMap(String... keyValuePairs) {
 		for (int i = 0; i < keyValuePairs.length; i+=2) {
 			someValues.put(keyValuePairs[i], keyValuePairs[i+1]);
 		}
 	}
 	
 	public void clear() {}
 	public boolean containsKey(Object _) { return true; }
 	public boolean containsValue(Object _) { return true; }
 	@SuppressWarnings("unchecked")
 	public Set<java.util.Map.Entry<String, String>> entrySet() { return unsupported(Set.class); }
 	public String get(Object key) {
 		if(someValues.containsKey(key)) return someValues.get(key);
 		else return key.toString();
 	}
 	public boolean isEmpty() { return false; }
 	@SuppressWarnings("unchecked")
 	public Set<String> keySet() { return unsupported(Set.class); }
 	public String put(String key, String value) { return key; }
 	public void putAll(Map<? extends String, ? extends String> arg0) {}
 	public String remove(Object arg0) { return unsupported(String.class); }
 	public int size() { return unsupported(int.class); }
 	@SuppressWarnings("unchecked")
 	public Collection<String> values() { return unsupported(Collection.class); }
 	private <T> T unsupported(Class<T> returnClass) {
 		throw new RuntimeException("This map has everything in it, so we can't really return that.");
 	}
 }
