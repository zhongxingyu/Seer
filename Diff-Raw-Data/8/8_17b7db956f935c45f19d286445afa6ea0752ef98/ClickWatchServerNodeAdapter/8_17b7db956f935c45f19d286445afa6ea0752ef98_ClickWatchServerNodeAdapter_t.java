 package edu.hu.clickwatch.server.node;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.FeatureMap;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
//import org.osgi.service.log.LogService;
 
 import click.ControlSocket;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 import com.google.inject.Inject;
 
 import edu.hu.clickcontrol.IClickSocket;
 import edu.hu.clickwatch.model.ClickWatchModelFactory;
 import edu.hu.clickwatch.model.Element;
 import edu.hu.clickwatch.model.Handler;
 import edu.hu.clickwatch.model.Node;
 
 public class ClickWatchServerNodeAdapter implements INodeAdapter {
 	/** */
 	@Inject
 	private IClickSocket mClickSocket;
 	/** */
 	private String mHost = null;
 	/** */
 	private String mPort = null;
 	/** */
 	private boolean isConnected = false;
 	/** */
 	private Node mInternalNodeCopy = null;
 
 	private static final ClickWatchModelFactory modelFactory = ClickWatchModelFactory.eINSTANCE;
 
 	/** Access to the OSGi log service */
//	private LogService mLogService = null;
 	
 	public ClickWatchServerNodeAdapter(){
 		//mLogService = ClickWatchServerPluginActivator.getInstance().getLogService();
 	}
 	
 	/** 
 	 * 
 	 */
 	public synchronized void setUp(String pHost, String pPort) {
 		this.mHost = pHost;
 		this.mPort = pPort;
 	}
 	
 	public void connect() {
 		/** */
 		Preconditions.checkState(!isConnected);
 		/** */
 		Preconditions.checkNotNull(mHost);
 		/** */
 		Preconditions.checkNotNull(mPort);
 
 		try {
 			mClickSocket.connect(InetAddress.getByName(mHost.trim()), new Integer(mPort.trim()));
 			isConnected = true;
 		} catch (IOException e) {
 			Throwables.propagate(e);
 		}	
 	}
 
 	public boolean isConnected() {
 		return isConnected;
 	}
 	
 	/**
 	 * Allows extending classes to ignore elements. Currently elements with "@"
 	 * in their names are ignored.
 	 * 
 	 * @param name
 	 *            , name of the element.
 	 * @return true, iff the element should be ignored.
 	 */
 	protected boolean ignoreElement(String name) {
 		return name.contains("@");
 	}
 
 	protected boolean ignoreHandler(String name) {
 		return false;
 	}
 
 	public void disconnect() {
 		if (mClickSocket == null || !isConnected) {
 			return;
 		}
 		mClickSocket.close();
 		isConnected = false;
 		if (mInternalNodeCopy != null) {
 			EcoreUtil.delete(mInternalNodeCopy, true);
 		}
 		mInternalNodeCopy = null;		
 	}
 
 	private void ensureConnected() {
 		Preconditions.checkState(isConnected, getClass().getName()
 				+ " needs to be connected first");
 	}
 	
 	public Node retrieve(String pElememtFilter, String pHandlerFilter) {
 		ensureConnected();
 		mInternalNodeCopy = modelFactory.createNode();
 		mInternalNodeCopy.setINetAdress(mHost);
 		mInternalNodeCopy.setPort(mPort);
 		
 		Map<String, Element> elementMap = new HashMap<String, Element>();
 		List<String> configElementNames = null;
 		try {
 			configElementNames = mClickSocket.getConfigElementNames();
 			for (Object elementNameObject : configElementNames) {
 				String elementName = elementNameObject.toString();
 				if (!ignoreElement(elementName)) {
 					String[] elementPath = elementName.split("/");
 					String elementNamePrefix = null;
 					Element parent = null;
 					for (String elementPathItem : elementPath) {
 						elementNamePrefix = elementNamePrefix == null ? elementPathItem
 								: elementNamePrefix + "/" + elementPathItem;
 						Element element = elementMap.get(elementNamePrefix);
 						if (element == null) {
 							element = modelFactory.createElement();
 							element.setName(elementPathItem);
 							if (parent == null) {
 								mInternalNodeCopy.getElements().add(element);
 							} else {
 								parent.getChildren().add(element);
 							}
 							elementMap.put(elementNamePrefix, element);
 						}
 						parent = element;
 					}
 				}
 			}
 		} catch (Throwable e) {
 			Throwables.propagate(e);
 		}
 
 		for (String elementPath : configElementNames) {
 			if (!ignoreElement(elementPath)) {
 				List<ControlSocket.HandlerInfo> handlerInfos = null;
 				Element element = elementMap.get(elementPath);
 				Preconditions.checkState(element != null);
 				try {
 					handlerInfos = mClickSocket.getElementHandlers(elementPath);
 				} catch (Throwable e) {
 					Throwables.propagate(e);
 				}
 
 				for (ControlSocket.HandlerInfo handlerInfo : handlerInfos) {
 					Handler newHandler = ClickWatchModelFactory.eINSTANCE
 							.createHandler();
 					newHandler.setName(handlerInfo.getHandlerName());
 					newHandler.setCanRead(handlerInfo.isCanRead());
 					newHandler.setCanWrite(handlerInfo.isCanWrite());
 					if (newHandler.isCanRead()) {
 						element.getHandlers().add(newHandler);
 					}
 				}
 			}
 		}
 		
 		filterInternalNodeCopy(pElememtFilter, pHandlerFilter);
 
 		Iterator<EObject> it = mInternalNodeCopy.eAllContents();
 		while (it.hasNext()) {
 			EObject next = it.next();
 			if (next instanceof Handler) {
 				Handler handler = (Handler)next;
 				if (handler.isCanRead()
 						&& !ignoreHandler(handler.getName())) {
 					char data[] = null;
 					try {
 						data = mClickSocket.read(((Element)handler.eContainer()).getElementPath(),
 								handler.getName());
 					} catch (Throwable e) {
 						Throwables.propagate(e);
 					}
 
 					try {
 						createAndSetModelValue(handler, new String(data));
 					} catch (Throwable e) {
						//mLogService.log(LogService.LOG_ERROR,"Exception: Exception while creating model for " + handler + " from " + new String(data), e);
 					}
 				}
 			}
 		}
 		//return EcoreUtil.copy(internalNodeCopy); //necessary??
 		return mInternalNodeCopy;
 	}
 
 	private void filterInternalNodeCopy(String elemFilters, String handFilter) {
 		// filter internal node copy
 		if ((elemFilters == null || elemFilters.trim().equals(""))
 				&& (handFilter == null || handFilter.trim().equals(""))) {
 			return;
 		}
 
 		if (elemFilters == null) {
 			elemFilters = "";
 		}
 		if (handFilter == null) {
 			handFilter = "";
 		}
 
 		String[] elemFilter = elemFilters.split("/");
 		Iterator<Element> elem_it = mInternalNodeCopy.getElements().iterator();
 		// recursive checker
 		filterMatched(elem_it, elemFilter, 0, handFilter);
 
 	}
 	
 	public final synchronized void updateHandlerValue(Handler pHandler) {
 		Preconditions.checkArgument(pHandler.isCanWrite());
 
 		ensureConnected();
 		Element element = (Element) pHandler.eContainer();
 
 		try {
 			mClickSocket.write(element.getElementPath(), pHandler.getName(),
 					createPlainRealValue(pHandler).toCharArray());
 		} catch (Throwable e) {
 			Throwables.propagate(e);
 		}		
 	}
 	
 	// recursive filter
 	private void filterMatched(Iterator<Element> elem_it, String[] elemFilter,
 			int idx, String handFilter) {
 		while (elem_it.hasNext()) {
 			Element elem = elem_it.next();
 			if ((elemFilter.length > idx)
 					&& !java.util.regex.Pattern.compile(elemFilter[idx])
 							.matcher(elem.getName()).find()) {
 				// does not match; remove it
 				elem_it.remove();
 				continue; // no need to check the children nodes
 			}
 			Iterator<Handler> hand_it = elem.getHandlers().iterator();
 
 			while (hand_it.hasNext()) {
 				Handler hand = hand_it.next();
 				if (!java.util.regex.Pattern.compile(handFilter)
 						.matcher(hand.getName()).find()) {
 					// does not match; remove it
 					hand_it.remove();
 				}
 			}
 
 			// recursive call
 			if (elem.getChildren().size() > 0) {
 				filterMatched(elem.getChildren().iterator(), elemFilter,
 						idx + 1, handFilter);
 			}
 		}
 	}
 	
 	
 	protected String createPlainRealValue(Handler handler) {
 		if (handler.getMixed().isEmpty()) {
 			return null;
 		} else {
 			Object value = handler.getMixed().getValue(0);
 			Preconditions.checkState(value instanceof String);
 			return (String)value;
 		}
 	}
 	
 	protected void createAndSetModelValue(Handler handler, String string) {
 		if (string == null) {
 			return;
 		}
 		
 		FeatureMap.Entry entry = FeatureMapUtil.createTextEntry(string);
 		if (!handler.getMixed().isEmpty()) {
 			handler.getMixed().set(0, entry);
 		} else {
 			handler.getMixed().add(entry);
 		}
 	}
 }
