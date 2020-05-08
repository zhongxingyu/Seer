 /*******************************************************************************
  * Copyright (c) 2012 Emanuele.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Emanuele - initial API and implementation
  ******************************************************************************/
 package game.configuration;
 
 import game.configuration.errorchecks.LengthCheck;
 import game.plugins.Constraint;
 import game.plugins.Implementation;
 import game.plugins.PluginManager;
 import game.utils.Utils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 import java.util.SortedSet;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public abstract class Configurable extends Observable implements Observer {
 	
 	@XStreamOmitField
 	private static final XStream configStream = new XStream();
 	@XStreamOmitField
 	private static Set<BaseConverter> converters = new HashSet<>();
 	
 	private class RequestForBoundOptions {
 		private List<String> boundOptions;
 		private String path;
 		
 		public RequestForBoundOptions(List<String> boundOptions, String path) {
 			this.boundOptions = boundOptions;
 			this.path = path;
 		}
 		
 		public List<String> getBoundOptions() { return boundOptions; }
 		public String getPath() { return path; }
 	}
 	
 	private class RequestForConstraints {
 		private List<Constraint> constraints;
 		private String path;
 
 		public RequestForConstraints(List<Constraint> constraints, String path) {
 			this.constraints = constraints;
 			this.path = path;
 		}
 		
 		public List<Constraint> getConstraints() { return constraints; }
 		public String getPath() { return path; }
 	}
 	
 	private List<OptionBinding> optionBindings = new LinkedList<>();
 	private Map<String, List<ErrorCheck>> optionChecks = new HashMap<>();
 	private List<String> omittedFromErrorCheck = new LinkedList<>();
 	private List<String> internalOptions = new LinkedList<>();
 	
 	protected Map<String, List<Constraint>> optionConstraints = new HashMap<>();
 	
 	//private List<String> omittedFromConfiguration = new LinkedList<>();
 	protected boolean notify = true;
 	
 	static {
 		registerConverter(new ConfigurableConverter());
 		registerConverter(new ConfigurableCollectionConverter());
 	}
 	
 	public Configurable() {
 		try {
 			getClass().getField("name");
 			setOption("name", String.format("%s%03d", getClass().getSimpleName(), hashCode() % 1000));
 		} catch (NoSuchFieldException | SecurityException e) {}
 		
 		setOptionChecks("name", new LengthCheck(1));
 	}
 	
 	public boolean hasOption(String optionName) {
 		return getAllOptionNames().contains(optionName);
 	}
 
 	public static <T> T loadFromConfiguration(File configFile) {
 		return (T)configStream.fromXML(Utils.readFile(configFile));
 	}
 	
 	public static void setClassLoader(ClassLoader loader) {
 		configStream.setClassLoader(loader);
 		for (BaseConverter converter: converters)
 			converter.setClassLoader(loader);
 	}
 	
 	public static void registerConverter(BaseConverter converter) {
 		configStream.registerConverter(converter);
 		converters.add(converter);
 	}
 	
 	public <T> T getOption(String optionPath) {
 		if (optionPath.isEmpty())
 			return null;
 		
 		int dotIndex = optionPath.indexOf('.');
 		int firstOptionIndex = dotIndex < 0 ? optionPath.length() : dotIndex;
 		Object object = getLocalOption(optionPath.substring(0, firstOptionIndex));
 		
 		if (dotIndex < 0 || object == null)
 			return (T)object;
 		else
 			return (T)((Configurable)object).getOption(optionPath.substring(firstOptionIndex+1));
 	}
 	
 	public <T> T getOption(String optionPath, Class<T> type) {
 		return (T)getOption(optionPath);
 	}
 	
 	public void setOption(String optionPath, Object content) {
 		setOption(optionPath, content, true, null);
 	}
 	
 	public void setOption(String optionPath, Object content, boolean notify, Object setter) {
 		if (optionPath.isEmpty())
 			return;
 		
 		this.notify = notify;
 		
 		int dotIndex = optionPath.indexOf('.');
 		int firstOptionIndex = dotIndex < 0 ? optionPath.length() : dotIndex;
 		
 		if (dotIndex < 0) {
 			setLocalOption(optionPath, content, setter);
 		} else {
 			Configurable object = (Configurable)getLocalOption(optionPath.substring(0, firstOptionIndex));
 			if (object != null)
 				object.setOption(optionPath.substring(firstOptionIndex+1), content, notify, setter);
 		}
 		
 		this.notify = true;
 	}
 	
 	public List<String> getBoundOptionNames() {
 		List<String> bound = new LinkedList<>();
 		setChanged();
 		update(null, new RequestForBoundOptions(bound, ""));
 		return bound;
 	}
 	
 	public List<String> getUnboundOptionNames() {
 		List<String> ret = getPublicOptionNames();
 		ret.removeAll(getBoundOptionNames());
 		return ret;
 	}
 	
 	public List<String> getAllOptionNames() {
 		List<String> ret = new LinkedList<>();
 		List<String> perClass = new LinkedList<>();
 		Class currentClass = getClass();
 		int fieldCount = 0;
 		for (Field f: getClass().getFields()) {
 			if (f.getDeclaringClass() != currentClass) {
 				ret.addAll(0, perClass);
 				currentClass = f.getDeclaringClass();
 				perClass.clear();
 			}
 			if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
 				perClass.add(f.getName());
 				fieldCount++;
 			}
 		}
 		if (fieldCount != ret.size())
 			ret.addAll(0, perClass);
 		return ret;
 	}
 	
 	public List<String> getPublicOptionNames() {
 		List<String> ret = getAllOptionNames();
 		ret.removeAll(internalOptions);
 		return ret;
 	}
 	
 	public HashMap<String, Object> getOptionsMap() {
 		HashMap<String, Object> ret = new HashMap<>();
 		
 		for (String name: getAllOptionNames())
 			ret.put(name, getLocalOption(name));
 		
 		return ret;
 	}
 	
 	public LinkedList<String> getConfigurationErrors() {
 		LinkedList<String> ret = getErrors();
 		
 		ret.addAll(getConfigurationErrors(new HashSet<Configurable>()));
 		
 		return ret;
 	}
 	
 	private LinkedList<String> getConfigurationErrors(Set<Configurable> seen) {
 		LinkedList<String> ret = new LinkedList<>();
 		seen.add(this);
 		for (Map.Entry<String, Object> entry: getOptionsMap().entrySet()) {
			if (internalOptions.contains(entry.getKey()))
				continue;
			if (entry.getValue() == null) {
 				ret.add(entry.getKey() + ": is null");
 			} else {
 				if (optionChecks.containsKey(entry.getKey())) {
 					for (ErrorCheck check: optionChecks.get(entry.getKey())) {
 						String error = check.getError(entry.getValue());
 						if (error != null)
 							ret.add(entry.getKey() + ": " + error);
 					}
 				}
 				if (entry.getValue() instanceof Configurable
 						&& !seen.contains(entry.getValue())
 						&& !omittedFromErrorCheck.contains(entry.getKey()))
 					ret.addAll(putPrefix(entry.getKey() + ".",
 							             ((Configurable)entry.getValue()).getConfigurationErrors(seen)));
 			}
 		}
 	
 		return ret;
 	}
 	
 	public <T> SortedSet<Implementation<T>> getCompatibleOptionImplementations(String optionName) {
 		return PluginManager.getCompatibleImplementationsOf(getOptionType(optionName), getOptionConstraints(optionName));
 	}
 	
 	public Class getOptionType(String optionName) {
 		try {
 			return getClass().getField(optionName).getType();
 		} catch (NoSuchFieldException | SecurityException e) {
 			return null;
 		}
 	}
 	
 	public String getOptionNameFromContent(Object content) {
 		try {
 			for (Field field: getClass().getFields()) {
 				if (field.get(this) == content)
 					return field.getName();
 			}
 		} catch (IllegalArgumentException | IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public String getConfiguration() {
 		return configStream.toXML(this);
 	}
 	
 	public boolean loadConfiguration(String fileName) {
 		return loadConfiguration(new File(fileName));
 	}
 	
 	public boolean loadConfiguration(File file) {
 		boolean ret = true;
 		
 		Configurable temporary = Configurable.loadFromConfiguration(file);
 		if (temporary.getClass().equals(this.getClass())) {
 			for(String boundOption: getBoundOptionNames()) {
 				if (!temporary.getOption(boundOption).equals(this.getOption(boundOption))) {
 					ret = false;
 					break;
 				}
 			}
 		} else {
 			ret = false;
 		}
 		temporary = null;
 		
 		if (ret == true) {
 			configStream.fromXML(Utils.readFile(file), this);
 			setChanged();
 			notifyObservers(new Change("", null, new HashSet<Configurable>()));
 		}
 		return ret;
 	}
 	
 	@Override
 	public boolean equals(Object object) {
 		if (object == this) {
 			return true;
 		}
 		if (object == null) {
 			return false;
 		}
 		if (!object.getClass().equals(this.getClass())) {
 			return false;
 		}
 		Configurable other = (Configurable)object;
 		return recursiveEquals(this, other, new HashSet<Configurable>());
 	}
 	
 	private static boolean recursiveEquals(Configurable c1, Configurable c2, Set<Configurable> seen) {
 		seen.add(c1);
 		seen.add(c2);
 		
 		for(String option: c1.getAllOptionNames()) {
 			if (option.equals("name"))
 				continue;
 
 			Object option1 = c1.getOption(option);
 			Object option2 = c2.getOption(option);
 			
 			if (option2 == null && option1 == null)
 				continue;
 			
 			if (option2 == null && option1 != null ||
 				option1 == null && option2 != null) {
 				return false;
 			}
 			
 			if (option1 instanceof Configurable && seen.contains(option1))
 				continue;
 			if (option2 instanceof Configurable && seen.contains(option2))
 				continue;
 			
 			if (option1 instanceof Configurable && option2 instanceof Configurable) {
 				if (!recursiveEquals((Configurable)option1, (Configurable)option2, seen))
 					return false;
 			} else {
 				if (!option2.equals(option1))
 					return false;
 			}
 		}
 		return true;
 	}
 
 	public void saveConfiguration(String fileName) {
 		saveConfiguration(new File(fileName));
 	}
 	
 	public void saveConfiguration(File file) {
 		try {
 			FileOutputStream stream = new FileOutputStream(file);
 			configStream.toXML(this, stream);
 			stream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public <T extends Configurable> T cloneConfiguration() {
 		T clone = (T)configStream.fromXML(this.getConfiguration());
 		return clone;
 	}
 	
 	public <T extends Configurable> T cloneConfiguration(String newName) {
 		T clone = (T)configStream.fromXML(this.getConfiguration());
 		clone.setOption("name", newName);
 		return clone;
 	}
 	
 	public void forceUpdate() {
 		propagateUpdate("", null);
 	}
 	
 	@Override
 	public void update(Observable observedOption, Object message) {
 		if (message instanceof Change) {
 			Change change = (Change)message;
 			String changedOption = getOptionNameFromContent(observedOption);
 			changedOption += change.getPath().isEmpty() ? "" : "." + change.getPath();
 			propagateUpdate(changedOption, change.getSetter(), change.getPropagators());
 		}
 		
 		if (message instanceof RequestForBoundOptions) {
 			RequestForBoundOptions request = (RequestForBoundOptions)message;
 			String pathToParent = observedOption == null ? "" : getOptionNameFromContent(observedOption) + "." + request.getPath();
 			for (OptionBinding binding: optionBindings) {
 				request.getBoundOptions().addAll(binding.getBoundOptions(pathToParent));
 			}
 			
 			if (observedOption != null)
 				observedOption.deleteObserver(this);
 			setChanged();
 			notifyObservers(new RequestForBoundOptions(request.getBoundOptions(), pathToParent));
 			if (observedOption != null)
 				observedOption.addObserver(this);
 		}
 		
 		if (message instanceof RequestForConstraints) {
 			RequestForConstraints request = (RequestForConstraints)message;
 			String pathToParent = getOptionNameFromContent(observedOption) + "." + request.getPath();
 			if (optionConstraints.containsKey(pathToParent))
 				request.getConstraints().addAll(optionConstraints.get(pathToParent));
 			
 			observedOption.deleteObserver(this);
 			setChanged();
 			notifyObservers(new RequestForConstraints(request.getConstraints(), pathToParent));
 			observedOption.addObserver(this);
 		}
 	}
 	/*
 	public boolean isOmittedFromConfiguration(String optionName) {
 		return omittedFromConfiguration.contains(optionName);
 	}
 	*/
 	@Override
 	public String toString() {
 		String name = getOption("name");
 		if (name != null)
 			return name;
 		else
 			return getClass().getSimpleName();
 	}
 	
 	protected void propagateUpdate(String changedOption, Object setter) {
 		propagateUpdate(changedOption, setter, new HashSet<Configurable>());
 	}
 	
 	protected void propagateUpdate(String changedOption, Object setter, Set<Configurable> propagators) {
 		if (notify) {
 			if (!changedOption.isEmpty())
 				updateOptionBindings(changedOption);
 			setChanged();
 			if (!propagators.contains(this)) {
 				propagators.add(this);
 				notifyObservers(new Change(changedOption, setter, propagators));
 			}
 		}
 	}
 	
 	protected void setAsInternalOptions(String... optionNames) {
 		for (String optionName: optionNames)
 			internalOptions.add(optionName);
 	}
 	
 	protected void unsetAsInternalOptions(String... optionNames) {
 		for (String optionName: optionNames)
 			internalOptions.remove(optionName);
 	}
 	
 	protected void omitFromErrorCheck(String... optionNames) {
 		for (String optionName: optionNames)
 			omittedFromErrorCheck.add(optionName);
 	}
 
 	protected void setOptionBinding(String masterPath, String... slaves) {
 		optionBindings.add(new OptionBinding(this, masterPath, slaves));
 	}
 	
 	protected void setOptionChecks(String optionName, ErrorCheck... checks) {
 		if (!optionChecks.containsKey(optionName))
 			optionChecks.put(optionName, new LinkedList<ErrorCheck>());
 		for (ErrorCheck check: checks)
 			optionChecks.get(optionName).add(check);
 	}
 	
 	protected void setOptionConstraints(String optionName, Constraint... constraints) {
 		List<Constraint> list = new ArrayList<>(constraints.length);
 		for(Constraint c: constraints)
 			list.add(c);
 		optionConstraints.put(optionName, list);
 	}
 	/*
 	protected void addOptionConstraints(String optionName, Constraint... constraints) {
 		if (!optionConstraints.containsKey(optionName))
 			setOptionConstraints(optionName, constraints);
 		else {
 			List<Constraint> list = optionConstraints.get(optionName);
 			for(Constraint c: constraints)
 				list.add(c);
 		}
 	}
 	*/
 	protected List<Constraint> getOptionConstraints(String optionName) {
 		List<Constraint> constraints = new LinkedList<>();
 		if (optionConstraints.containsKey(optionName))
 			constraints.addAll(optionConstraints.get(optionName));
 		setChanged();
 		notifyObservers(new RequestForConstraints(constraints, optionName));
 		return constraints;
 	}
 	
 	protected Object getLocalOption(String optionName) {
 		try {
 			Method getter = getMethodByName(getAccessorName("get", optionName));
 			if (getter != null) {
 				return getter.invoke(this);
 			} else {
 				return getClass().getField(optionName).get(this);
 			}
 		} catch (IllegalAccessException | IllegalArgumentException
 				| InvocationTargetException | NoSuchFieldException | SecurityException e) {
 			//e.printStackTrace();
 			return null;
 		}
 	}
 	
 	protected void setLocalOption(String optionName, Object content, Object setter) {
 		try {
 			Object oldContent = getLocalOption(optionName);
 			if (oldContent instanceof Configurable) {
 				((Configurable)oldContent).deleteObserver(this);
 			}
 			
 			Method setterMethod = getMethodByName(getAccessorName("set", optionName));
 			if (setterMethod != null) {
 				setterMethod.invoke(this, content);
 			} else {
 				getClass().getField(optionName).set(this, content);
 			}
 			
 			if (content instanceof Configurable) {
 				((Configurable)content).addObserver(this);
 			}
 			
 			propagateUpdate(optionName, setter);
 		} catch (IllegalAccessException | IllegalArgumentException
 				| InvocationTargetException | NoSuchFieldException | SecurityException e) {
 			//e.printStackTrace();
 		}
 	}
 	
 	protected void updateOptionBindings(String changedOption) {
 		for (OptionBinding binding: optionBindings)
 			binding.updateOnChange(changedOption);
 	}
 	
 	protected LinkedList<String> getErrors() {
 		LinkedList<String> ret = new LinkedList<>();
 		
 		return ret;
 	}
 	
 	private String getAccessorName(String prefix, String optionName) {
 		return prefix + optionName.substring(0,1).toUpperCase() + optionName.substring(1);
 	}
 	
 	private LinkedList<String> putPrefix(String prefix, LinkedList<String> list) {
 		LinkedList<String> ret = new LinkedList<>();
 		
 		for (String s: list)
 			ret.add(prefix + s);
 		
 		return ret;
 	}
 	
 	private Method getMethodByName(String methodName) {
 		for (Method method: getClass().getMethods()) {
 			if (method.getName().equals(methodName))
 				return method;
 		}
 		return null;
 	}
 	
 }
