 package org.codehaus.xfire.aegis;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.ServiceInfo;
 import org.codehaus.xfire.service.binding.DefaultServiceConfiguration;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.jdom.Element;
 
 /**
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * 
  */
 public class AegisServiceConfiguration extends DefaultServiceConfiguration {
 
 	private final XMLClassMetaInfoManager manager = new XMLClassMetaInfoManager();
 
 	private Map classes = new HashMap();
 
 	public AegisServiceConfiguration() {
 		super();
 	}
 
 	public AegisServiceConfiguration(ObjectServiceFactory serviceFactory) {
 		super();
 		setServiceFactory(serviceFactory);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.codehaus.xfire.service.binding.DefaultServiceConfiguration#getOperationName(org.codehaus.xfire.service.ServiceInfo,
 	 *      java.lang.reflect.Method)
 	 */
 	public String getOperationName(ServiceInfo service, Method method) {
 
 		String opName = null;
 		MethodInfo methodInfo = getMethodInfo(service.getServiceClass(), method);
 
 		if (methodInfo != null) {
 			opName = methodInfo.getMappedName();
 		}
 
 		return (opName != null ? opName : super.getOperationName(service,
 				method));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.codehaus.xfire.service.binding.DefaultServiceConfiguration#getAction(org.codehaus.xfire.service.OperationInfo)
 	 */
 	public String getAction(OperationInfo op) {
 		String action = null;
 		MethodInfo methodInfo = getMethodInfo(
 				op.getService().getServiceClass(), op.getMethod());
 		if (methodInfo != null) {
 			action = methodInfo.getAction();
 		}
 
 		return (action != null ? action : super.getAction(op));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.codehaus.xfire.service.binding.DefaultServiceConfiguration#isHeader(java.lang.reflect.Method,
 	 *      int)
 	 */
 	public Boolean isHeader(Method method, int j) {
 
		MethodInfo methodInfo = getMethodInfo(method.getDeclaringClass(),
				method);
 		ParamInfo param = methodInfo.getParam(j);
 		if (param == null) {
 			return super.isHeader(method, j);
 		}
 		return Boolean.valueOf(param.isHeader());
 	}
 
 	/**
 	 * @param service
 	 * @return
 	 */
 	private BeanInfo parseBeanElement(Class clazz) {
 		BeanInfo beanInfo = new BeanInfo();
 		beanInfo.setClazz(clazz);
 
 		Element mapping = manager.findMapping(clazz, null);
 		if (mapping == null) {
 			return beanInfo;
 		}
 		List methods = mapping.getChildren("method");
 
 		for (int i = 0; i < methods.size(); i++) {
 			MethodInfo methodInfo = new MethodInfo();
 			Element methodEl = (Element) methods.get(i);
 			methodInfo.setName(methodEl.getAttributeValue("name"));
 			methodInfo.setMappedName(methodEl.getAttributeValue("mappedName"));
 			methodInfo.setAction(methodEl.getAttributeValue("action"));
 			beanInfo.addMethod(methodInfo);
 			List params = methodEl.getChildren("parameter");
 			for (Iterator iter = params.iterator(); iter.hasNext();) {
 				Element paramEl = (Element) iter.next();
 				ParamInfo param = new ParamInfo();
 				param.setHeader("true".equals(paramEl
 						.getAttributeValue("header")));
 				param.setIndex(Integer.parseInt(paramEl
 						.getAttributeValue("index")));
 				methodInfo.addParam(param);
 
 			}
 
 		}
 		return beanInfo;
 	}
 
 	
 
 	/**
 	 * @param clazz
 	 * @return
 	 */
 	private BeanInfo getBeanInfo(Class clazz) {
 
 		BeanInfo info = (BeanInfo) classes.get(clazz);
 		if (info == null) {
 			info = parseBeanElement(clazz);
 		}
 
 		return info;
 	}
 
 	/**
 	 * @param service
 	 * @param method
 	 * @return
 	 */
 	private MethodInfo getMethodInfo(Class clazz, Method method) {
 
 		BeanInfo beanInfo = getBeanInfo(clazz);
 		return beanInfo.getMethod(method.getName());
 	}
 
 	/**
 	 * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
 	 * 
 	 */
 	private class BeanInfo {
 		private Map methods = new HashMap();
 
 		private Class clazz;
 
 		public MethodInfo getMethod(String name) {
 			return (MethodInfo) methods.get(name);
 
 		}
 
 		public void addMethod(MethodInfo method) {
 			methods.put(method.getName(), method);
 		}
 
 		public Class getClazz() {
 			return clazz;
 		}
 
 		public void setClazz(Class clazz) {
 			this.clazz = clazz;
 		}
 
 	}
 
 	/**
 	 * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
 	 * 
 	 */
 	private class MethodInfo {
 
 		private String name;
 
 		private String mappedName;
 
 		private String action;
 
 		private Map params = new HashMap();
 
 		public String getAction() {
 			return action;
 		}
 
 		public ParamInfo getParam(int j) {
 
 			return (ParamInfo) params.get(new Integer(j));
 
 		}
 
 		public void addParam(ParamInfo param) {
 
 			params.put(new Integer(param.getIndex()), param);
 
 		}
 
 		public void setAction(String action) {
 			this.action = action;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public String getMappedName() {
 			return mappedName;
 		}
 
 		public void setMappedName(String mappedName) {
 			this.mappedName = mappedName;
 		}
 
 	}
 
 	/**
 	 * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
 	 * 
 	 */
 	private class ParamInfo {
 
 		private int index;
 
 		private boolean isHeader;
 
 		public int getIndex() {
 			return index;
 		}
 
 		public void setIndex(int index) {
 			this.index = index;
 		}
 
 		public boolean isHeader() {
 			return isHeader;
 		}
 
 		public void setHeader(boolean isHeader) {
 			this.isHeader = isHeader;
 		}
 
 	}
 }
