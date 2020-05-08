 /* OperationAgentManager.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Mar 20, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
  */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.zkoss.zats.mimic.impl.Util;
 import org.zkoss.zats.mimic.operation.CheckAgent;
 import org.zkoss.zats.mimic.operation.ClickAgent;
 import org.zkoss.zats.mimic.operation.FocusAgent;
 import org.zkoss.zats.mimic.operation.KeyStrokeAgent;
 import org.zkoss.zats.mimic.operation.MultipleSelectAgent;
 import org.zkoss.zats.mimic.operation.OpenAgent;
 import org.zkoss.zats.mimic.operation.OperationAgent;
 import org.zkoss.zats.mimic.operation.RendererAgent;
 import org.zkoss.zats.mimic.operation.SelectAgent;
 import org.zkoss.zats.mimic.operation.TypeAgent;
 import org.zkoss.zhtml.Input;
 import org.zkoss.zk.ui.AbstractComponent;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zul.Checkbox;
 import org.zkoss.zul.Datebox;
 import org.zkoss.zul.Decimalbox;
 import org.zkoss.zul.Doublebox;
 import org.zkoss.zul.Doublespinner;
 import org.zkoss.zul.Grid;
 import org.zkoss.zul.Intbox;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Longbox;
 import org.zkoss.zul.Menuitem;
 import org.zkoss.zul.Spinner;
 import org.zkoss.zul.Timebox;
 import org.zkoss.zul.Toolbarbutton;
 import org.zkoss.zul.impl.InputElement;
 
 public class OperationAgentManager {
 	private static Map<Key, OperationAgentBuilder<? extends OperationAgent>> builders;
 
 	static {
 		builders = new HashMap<OperationAgentManager.Key, OperationAgentBuilder<? extends OperationAgent>>();
 
 		// TODO load default implement
 		registerBuilder("5.0.0","*", AbstractComponent.class, ClickAgent.class,
 				new GenericClickAgentBuilder());
 		registerBuilder("5.0.0","*", AbstractComponent.class, KeyStrokeAgent.class,
 				new GenericKeyStrokeAgentBuilder());
 
 		
 		//the inputs
		registerBuilder("5.0.0","*", InputElement.class, FocusAgent.class,
				new GenericFocusAgentBuilder());
 		registerBuilder("5.0.0","*", InputElement.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.TextTypeAgentBuilder());
 		registerBuilder("5.0.0","*", Intbox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.IntegerTypeAgentBuilder());
 		registerBuilder("5.0.0","*", Longbox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.IntegerStringTypeAgentBuilder());
 		registerBuilder("5.0.0","*", Spinner.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.IntegerTypeAgentBuilder());
 		
 		registerBuilder("5.0.0","*", Decimalbox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DecimalStringTypeAgentBuilder());
 		registerBuilder("5.0.0","*", Doublebox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DecimalTypeAgentBuilder());
 		registerBuilder("5.0.0","*", Doublespinner.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DecimalTypeAgentBuilder());
 		
 		registerBuilder("5.0.0","*", Datebox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DateTypeAgentBuilder());
 		registerBuilder("5.0.0","*", Timebox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.TimeTypeAgentBuilder());
 
 		//the listbox
 		registerBuilder("5.0.0","*", Listbox.class, SelectAgent.class,
 				new ListboxSelectAgentBuilder());
 		registerBuilder("5.0.0","*", Listbox.class, MultipleSelectAgent.class,
 				new ListboxMultipleSelectAgentBuilder());
 		registerBuilder("5.0.0","*", Listbox.class, RendererAgent.class, new ListboxRendererAgentBuilder());
 		
 		registerBuilder("5.0.0","*", Grid.class, RendererAgent.class, new GridRendererAgentBuilder());
 
 		registerBuilder("5.0.0","*", Input.class, CheckAgent.class,
 				new GenericCheckAgentBuilder());
 		registerBuilder("5.0.0","*", Checkbox.class, CheckAgent.class,
 				new GenericCheckAgentBuilder()); // include Radio.class
 		registerBuilder("5.0.0","*", Menuitem.class, CheckAgent.class,
 				new GenericCheckAgentBuilder());
 		registerBuilder("5.0.0","*", Toolbarbutton.class, CheckAgent.class,
 				new GenericCheckAgentBuilder());
 
 		registerBuilder("5.0.0","*", AbstractComponent.class, OpenAgent.class,
 				new GenericOpenAgentBuilder());
 		
 		// TODO load custom implement from configuration
 
 		// TODO
 		// registerBuilder(*, Label.class, Clickable.class, new
 		// GenericClickableBuilder());
 		// registerBuilder(6, Label.class, Clickable.class, new
 		// Generic6ClickableBuilder());
 
 		// TODO Enhancement.
 		// registerBuilder(*,*, Label.class, Clickable.class, new
 		// GenericClickableBuilder());
 		// registerBuilder(*,"6.0.1", Label.class, Clickable.class, new
 		// GenericClickableBuilder());
 		// registerBuilder("5.0.2","*", Label.class, Clickable.class, new
 		// GenericClickableBuilder());
 		// registerBuilder("5.0.2","6.0.1", Label.class, Clickable.class, new
 		// GenericClickableBuilder());
 	}
 
 	/**
 	 * Register a operation builder mapping to component and operation. We can
 	 * specify zk version worked on. The version text could be normal version
 	 * format (e.g 6.0.0 or 5.0.7.1) or "*" sign means no specify. If specify
 	 * version range doesn't include current zk version at runtime, this
 	 * register will be ignored. <p/>
 	 * 
 	 * Use this API if the component is only in a particular zk version only to avoid initial exception. <p/>
 	 * 
 	 * @param startVersion
 	 *            start version (include)
 	 * @param endVersion
 	 *            end version (include)
 	 * @param compClazz
 	 *            the component class that builder maps to ( *notice: it should
 	 *            not specify interface)
 	 * @param opClass
 	 *            the operation class that builder maps to
 	 * @param builder
 	 *            operation builder
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static <T extends OperationAgent> 
 	void registerBuilder(String startVersion, String endVersion, String compClazz,Class<T> opClass, String builderClazz) {
 		if (startVersion == null || endVersion == null || compClazz == null
 				|| opClass == null || builderClazz == null)
 			throw new IllegalArgumentException();
 		
 		if(!Util.checkVersion(startVersion,endVersion)) return;
 		
 		Class clz = null;
 		try {
 			clz = Class.forName(compClazz);
 		} catch (ClassNotFoundException e) {
 			throw new IllegalArgumentException("compClazz "+compClazz+" not found ", e);
 		}
 		OperationAgentBuilder<T> builder = null;
 		try{
 			Class buildClz = Class.forName(builderClazz);
 			builder = (OperationAgentBuilder)buildClz.newInstance();
 		}catch(Exception x){
 			throw new IllegalArgumentException(x.getMessage(), x);
 		}
 		
 		if(Component.class.isAssignableFrom(clz)){
 			registerBuilder(startVersion,endVersion,clz,opClass,builder);
 		}else{
 			throw new IllegalArgumentException("compClazz "+compClazz+" is not a Component");
 		}
 	}
 
 	
 	/**
 	 * register a operation builder mapping to component and operation. We can
 	 * specify zk version worked on. The version text could be normal version
 	 * format (e.g 6.0.0 or 5.0.7.1) or "*" sign means no specify. If specify
 	 * version range doesn't include current zk version at runtime, this
 	 * register will be ignored.
 	 * 
 	 * @param startVersion
 	 *            start version (include)
 	 * @param endVersion
 	 *            end version (include)
 	 * @param compClazz
 	 *            the component class that builder maps to ( *notice: it should
 	 *            not specify interface)
 	 * @param opClass
 	 *            the operation class that builder maps to
 	 * @param builder
 	 *            operation builder
 	 */
 	public static <T extends OperationAgent,C extends Component> 
 		void registerBuilder(String startVersion, String endVersion, Class<C> compClazz,Class<T> opClass, OperationAgentBuilder<T> builder) {
 		
 		if (startVersion == null || endVersion == null || compClazz == null
 				|| opClass == null || builder == null)
 			throw new IllegalArgumentException();
 
 		if(!Util.checkVersion(startVersion,endVersion)) return;
 
 		// component and operation classes mapping to builder
 		// builder would be replace by later register
 		builders.put(new Key(compClazz, opClass), builder);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T extends OperationAgent> OperationAgentBuilder<T> getBuilder(
 			Component component, Class<T> operation) {
 		// search from self class to parent class
 		Class<?> c = component.getClass();
 		while (c != null) {
 			OperationAgentBuilder<? extends OperationAgent> builder = builders
 					.get(new Key(c, operation));
 			if (builder != null)
 				return (OperationAgentBuilder<T>) builder;
 			c = c.getSuperclass();
 		}
 		return null; // not found
 	}
 
 	/**
 	 * for operation builder mapping
 	 * 
 	 * @author pao
 	 */
 	private static class Key {
 		public Class<?> c;
 		public Class<?> t;
 
 		public <T extends OperationAgent, C extends Component> Key(Class<?> c,
 				Class<?> t) {
 			this.c = c;
 			this.t = t;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((c == null) ? 0 : c.hashCode());
 			result = prime * result + ((t == null) ? 0 : t.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj == this)
 				return true;
 			else if (obj instanceof Key) {
 				Key o = (Key) obj;
 				return o.t == this.t && o.c == this.c;
 			}
 			return false;
 		}
 	}
 }
