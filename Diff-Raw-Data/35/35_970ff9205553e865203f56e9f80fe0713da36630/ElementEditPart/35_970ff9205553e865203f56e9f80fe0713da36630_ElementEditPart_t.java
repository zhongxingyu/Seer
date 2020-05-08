 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.parts;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.DragTracker;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.EditPolicy;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.requests.LocationRequest;
 import org.eclipse.gef.tools.DragEditPartsTracker;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsf.core.internal.tld.CMUtil;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.converter.ConvertPosition;
 import org.eclipse.jst.pagedesigner.converter.IConverterFactory;
 import org.eclipse.jst.pagedesigner.converter.ITagConverter;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.layout.CSSFigure;
 import org.eclipse.jst.pagedesigner.css2.layout.CSSWidgetLayout;
 import org.eclipse.jst.pagedesigner.css2.style.AbstractStyle;
 import org.eclipse.jst.pagedesigner.css2.widget.HiddenProvider;
 import org.eclipse.jst.pagedesigner.dtmanager.DTManager;
 import org.eclipse.jst.pagedesigner.editpolicies.ElementMenuBar;
 import org.eclipse.jst.pagedesigner.editpolicies.ElementResizableEditPolicy;
 import org.eclipse.jst.pagedesigner.editpolicies.IEnhancedSelectionEditPolicy;
 import org.eclipse.jst.pagedesigner.elementedit.ElementEditFactoryRegistry;
 import org.eclipse.jst.pagedesigner.elementedit.IElementEdit;
 import org.eclipse.jst.pagedesigner.figurehandler.FigureFactory;
 import org.eclipse.jst.pagedesigner.figurehandler.IFigureHandler;
 import org.eclipse.jst.pagedesigner.jsp.core.IJSPCoreConstants;
 import org.eclipse.jst.pagedesigner.range.RangeUtil;
 import org.eclipse.jst.pagedesigner.requests.PageDesignerRequestConstants;
 import org.eclipse.jst.pagedesigner.viewer.DesignRange;
 import org.eclipse.jst.pagedesigner.viewer.IHTMLGraphicalViewer;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMText;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 /**
  * @author mengbo
  * @version 1.5
  */
 public class ElementEditPart extends SubNodeEditPart {
 	private static Logger _log = PDPlugin.getLogger(ElementEditPart.class);
 
 	private Element _elementNode;
 
 	private ITagConverter _tagConverter;
     
     private ElementMenuBar  _nonVisualElementBar;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.EditPart#setModel(java.lang.Object)
 	 */
 	public void setModel(Object model) {
 		super.setModel(model);
 		_elementNode = (Element) model;
 		_tagConverter = getTagConverter(_elementNode);
 		_tagConverter.convertRefresh(null);
 		adaptEditProxy();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.parts.NodeEditPart#getDragTracker(org.eclipse.gef.Request)
 	 */
 	public DragTracker getDragTracker(Request request) 
     {
         EditPolicy policy = this
             .getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
 
         if (PageDesignerRequestConstants.REQ_SELECTION_TRACKER.equals(request.getType())
                 || org.eclipse.gef.RequestConstants.REQ_SELECTION.equals(request.getType()))
         {
             if (policy instanceof IEnhancedSelectionEditPolicy
                     && request instanceof LocationRequest)
             {
                 return ((IEnhancedSelectionEditPolicy)policy).getSelectionDragTracker((LocationRequest)request);
             }
             
             return null;
         }
         
         // should not happen
         return new DragEditPartsTracker(this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.parts.NodeEditPart#createEditPolicies()
 	 */
 	protected void createEditPolicies() {
 		super.createEditPolicies();
 		IElementEdit support = getElementEdit();
 		if (support != null) {
 			support.createEditPolicies(this);
 		}
 
 		// if ElementEdit didn't install special SELECTION_FEEDBACK_ROLE policy,
 		// then default
 		if (this.getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE) == null) {
 			this.installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE,
 					new ElementResizableEditPolicy());
 		}
 	}
 
 	/**
	 * @return the associated element edit
 	 */
 	public IElementEdit getElementEdit() {
 		// XXX: should we cache it?
 		return ElementEditFactoryRegistry.getInstance().createElementEdit(
 				_elementNode);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.parts.NodeEditPart#addNotify()
 	 */
 	public void addNotify() {
 		if (_tagConverter == null) {
 			_tagConverter = getTagConverter(_elementNode);
 			_tagConverter.convertRefresh(null);
 			adaptEditProxy();
 		}
 		super.addNotify();
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	private ITagConverter getTagConverter(Element node) {
 		return DTManager.getInstance().getTagConverter(node,
 				IConverterFactory.MODE_DESIGNER,
 				this.getDestDocumentForDesign());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#removeNotify()
 	 */
 	public void removeNotify() {
 		super.removeNotify();
 		// if (_tagConverter != null)
 		// {
 		// _tagConverter.dispose();
 		// _tagConverter = null;
 		// }
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
 	 */
 	protected List getModelChildren() {
		List children_ = new ArrayList(_tagConverter.getChildModeList());
         
         for (Iterator it = _tagConverter.getNonVisualChildren().iterator(); it.hasNext();)
         {
             Element nonVisualChild = (Element) it.next();
            children_.add(DTManager.getInstance().getTagConverter(nonVisualChild,
                 IConverterFactory.MODE_DESIGNER,
                 this.getDestDocumentForDesign()));
         }
        return children_;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
 	 */
 	protected IFigure createFigure() {
 		// if (_tagConverter.isVisualByHTML())
 		// {
 		// Element result = _tagConverter.getResultElement();
 		// return FigureFactory.createFigure(result,
 		// true);//_tagConverter.isMultiLevel());
 		// }
 		// else
 		// {
 		// CSSWidgetFigure figure = new CSSWidgetFigure(this._elementNode,
 		// createHiddenProvider());
 		// return figure;
 		// }
 		return new CSSFigure();
 	}
 
 	/**
 	 * @return
 	 */
 	private HiddenProvider createHiddenProvider() {
 		Element result = _tagConverter.getHostElement();
 		String localName = result.getLocalName();
 		String appendString = localName;
 		if (localName.equalsIgnoreCase(IJSPCoreConstants.TAG_DIRECTIVE_TAGLIB)) {
 			appendString = ((IDOMElement) result)
 					.getAttribute(IJSPCoreConstants.ATTR_URI);
 			if (appendString == null) {
 				appendString = "";
 			}
 		}
 		Image image = _tagConverter.getVisualImage();
 		HiddenProvider provider = new HiddenProvider(image, this);
 		((CSSFigure) getFigure()).setCSSStyle(provider.getCSSStyle());
 		provider.setLabel(appendString);
 		return provider;
 	}
 
 	/**
 	 * called by the
	 * @param recursive
 	 * 
 	 */
 	public void refreshModelChange(boolean recursive) {
 		IElementEdit support = getElementEdit();
 		if (support == null
 				|| !support.handleModelChange(_elementNode, this, recursive)) {
 			this.refresh(recursive);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#refresh()
 	 */
 	public void refresh() {
 		refresh(false);
 	}
 
	/**
	 * @param recursive
	 */
 	public void refresh(boolean recursive) {
 		if (!_tagConverter.isVisualByHTML()) {
 			_tagConverter.convertRefresh(null);
 			((CSSFigure) getFigure())
 					.setFixedLayoutManager(new CSSWidgetLayout(
 							(CSSFigure) getFigure(), createHiddenProvider()));
 			// nothing to refresh
 			// ((CSSWidgetFigure)
 			// getFigure()).setProvider(createHiddenProvider());
 			return;
 		}
 		EditPart editPart;
 		Object model;
 
 		Map modelToEditPart = new HashMap();
 		List children1 = getChildren();
 
 		for (int i = 0, n = children1.size(); i < n; i++) {
 			editPart = (EditPart) children1.get(i);
 			modelToEditPart.put(editPart.getModel(), editPart);
 			// remove child visual, since we may reconstruct the figure
 			// structure of this edit part
 			removeChildVisual(editPart);
 		}
 
 		Element oldEle = _tagConverter.getResultElement();
 
 		// link parent node.
 		Node parent = oldEle.getParentNode();
 		_tagConverter.convertRefresh(null);
 		if (parent != null) {
 			// a new element is generated. replace the old one.
 			parent.replaceChild(_tagConverter.getResultElement(), oldEle);
 		}
 
 		adaptEditProxy();
 
 		// XXX: comment out the if-else for always deep update.
 		// this is for the case when a empty container generate child
 		// text node, and then when user input data into the container,
 		// the node change from "multiLevel" state to "non-multilevel"
 		// state. We don't handle this very well yet, so always to deep
 		// update for now. (lium)
 		// if (_tagConverter.isMultiLevel())
 		// {
 		FigureFactory.updateDeepFigure(_tagConverter.getResultElement(),
 				oldEle, (CSSFigure) this.getFigure());
 		// }
 		// else
 		// {
 		// FigureFactory.updateNonDeepFigure(_tagConverter.getResultElement(),
 		// this.getFigure());
 		// }
 
 		List modelObjects = getModelChildren();
 		if (!recursive) {
 			for (int i = 0, n = modelObjects.size(); i < n; i++) {
 				model = modelObjects.get(i);
 
 				// Look to see if the EditPart is already around but in the
 				// wrong location
 				editPart = (EditPart) modelToEditPart.remove(model);
 
 				if (editPart != null) {
 					addChildVisual(editPart, i);
 				} else {
 					// An editpart for this model doesn't exist yet. Create and
 					// insert one.
 					editPart = createChild(model);
 					addChild(editPart, i);
 				}
 			}
 			for (Iterator iter = modelToEditPart.values().iterator(); iter
 					.hasNext();) {
 				EditPart part = (EditPart) iter.next();
 				removeChild(part);
 			}
 		} else {
 			// remove all child, and recreate them.
 			for (Iterator iter = modelToEditPart.values().iterator(); iter
 					.hasNext();) {
 				EditPart part = (EditPart) iter.next();
 				removeChild(part);
 			}
 			for (int i = 0, n = modelObjects.size(); i < n; i++) {
 				model = modelObjects.get(i);
 
 				// Look to see if the EditPart is already around but in the
 				// wrong location
 				// An editpart for this model doesn't exist yet. Create and
 				// insert one.
 				editPart = createChild(model);
 				addChild(editPart, i);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void adaptEditProxy() {
 		Element resultEle = _tagConverter.getResultElement();
 		if (resultEle instanceof IDOMElement) {
 			INodeAdapter adapter = ((IDOMElement) resultEle)
 					.getAdapterFor(EditProxyAdapter.class);
 			if (adapter != null) {
 				((IDOMElement) resultEle).removeAdapter(adapter);
 			}
 			((IDOMElement) resultEle).addAdapter(new EditProxyAdapter(this));
 		}
 	}
 
 	/**
	 * @return true if we are in range mode and this is in 
	 * the selection range
 	 */
 	public boolean isRangeSelected() {
 		IHTMLGraphicalViewer viewer = (IHTMLGraphicalViewer) this.getViewer();
 		if (viewer == null || !viewer.isInRangeMode()) {
 			return false;
 		}
 		DesignRange range = viewer.getRangeSelection();
 		if (range == null || !range.isValid()) {
 			return false;
 		}
 		return RangeUtil.intersect(range, this);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.parts.NodeEditPart#isWidget()
 	 */
 	public boolean isWidget() {
 		return _tagConverter.isWidget();
 	}
 
 	/**
	 * @return true if our model node can have direct text children
 	 */
 	public boolean canHaveDirectTextChild() {
 		return CMUtil.canHaveDirectTextChild(this._elementNode);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.parts.NodeEditPart#isResizable()
 	 */
 	public boolean isResizable() {
 		if (!_tagConverter.isVisualByHTML()) {
 			return false;
 		}
 		IElementEdit edit = this.getElementEdit();
 		if (edit != null) {
 			return edit.isResizable(this._elementNode);
 		}
         CMElementDeclaration decl = CMUtil
         		.getElementDeclaration(this._elementNode);
         if (decl != null) {
         	// XXX: default implementation, if this element support "style"
         	// attribute,
         	// then we think it support resize.
         	return decl.getAttributes().getNamedItem("style") != null;
         }
         return true;
 	}
 
 	/**
 	 * @param parent
 	 * @return
 	 */
 	private IFigure getFigure(Node parent) {
 		if (parent instanceof INodeNotifier) {
 			IFigureHandler handler = (IFigureHandler) ((INodeNotifier) parent)
 					.getAdapterFor(IFigureHandler.class);
 			if (handler != null) {
 				return handler.getFigure();
 			}
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#addChildVisual(org.eclipse.gef.EditPart,
 	 *      int)
 	 */
 	protected void addChildVisual(EditPart childEditPart, int index) {
         
         boolean figureAdded = false;
 
         if (childEditPart instanceof NonVisualComponentEditPart)
         {
             getNonVisualElementBar().addNonVisualChild(((NonVisualComponentEditPart) childEditPart));
             figureAdded = true;
             //TODO: need better flow of control.
             return;
         }
         
 		Node childNode = (Node) childEditPart.getModel();
 		IFigure childFigure = ((GraphicalEditPart) childEditPart).getFigure();
 		ConvertPosition position = _tagConverter
 				.getChildVisualPosition(childNode);
 		if (position != null) {
 			Node parent = position.getParentNode();
 			// link up figure.
 			IFigure parentFigure = getFigure(parent);
 			if (parentFigure != null) {
 				parentFigure.add(childFigure, position.getIndex());
 				figureAdded = true;
 			}
 			// link up style
 			if (parent instanceof INodeNotifier) {
 				ICSSStyle parentStyle = (ICSSStyle) ((INodeNotifier) parent)
 						.getAdapterFor(ICSSStyle.class);
 				if (parentStyle != null) {
 					ICSSStyle childStyle = (ICSSStyle) ((INodeNotifier) childNode)
 							.getAdapterFor(ICSSStyle.class);
 					if (childStyle instanceof AbstractStyle) {
 						((AbstractStyle) childStyle)
 								.setParentStyle(parentStyle);
 					}
 				}
 			}
 			// link up the nodeForFigure
 			if (childEditPart instanceof SubNodeEditPart) {
 				Node nodeForFigure = ((SubNodeEditPart) childEditPart)
 						.getNodeForFigure();
 				if (nodeForFigure != null /*
 											 * && !(nodeForFigure instanceof
 											 * PseudoElement)
 											 */) {
 					parent.appendChild(nodeForFigure);
 				}
 			}
 		} else {
 		    _log.error("getChildVisualPosition() return null");
 		}
 
 		if (!figureAdded) {
 			super.addChildVisual(childEditPart, index);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#removeChildVisual(org.eclipse.gef.EditPart)
 	 */
 	protected void removeChildVisual(EditPart childEditPart) {
         // remove figure
         IFigure childFigure = ((GraphicalEditPart) childEditPart).getFigure();
         IFigure parent = childFigure.getParent();
 
 		if (parent != null) {
 			parent.remove(childFigure);
 		}
         
         if (childEditPart instanceof NonVisualComponentEditPart)
         {
             _nonVisualElementBar.removeNonVisualChild((NonVisualComponentEditPart) childEditPart);
         }
         // this only applies to visual edit parts
         else
         {
     		// de-link style
     		Node childNode = (Node) childEditPart.getModel();
     		ICSSStyle childStyle = (ICSSStyle) ((INodeNotifier) childNode)
     				.getAdapterFor(ICSSStyle.class);
     		if (childStyle instanceof AbstractStyle) {
     			((AbstractStyle) childStyle).setParentStyle(null);
     		}
     		// de-link nodeForFigure
     		if (childEditPart instanceof SubNodeEditPart) {
     			Node nodeForFigure = ((SubNodeEditPart) childEditPart)
     					.getNodeForFigure();
     			if (nodeForFigure != null && nodeForFigure.getParentNode() != null) {
     				nodeForFigure.getParentNode().removeChild(nodeForFigure);
     			}
     		}
         }
 	}
 
 	/**
	 * @return the associated tag converter
 	 */
 	public ITagConverter getTagConvert() {
 		return _tagConverter;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.sse.core.internal.provisional.INodeAdapter#notifyChanged(org.eclipse.wst.sse.core.internal.provisional.INodeNotifier,
 	 *      int, java.lang.Object, java.lang.Object, java.lang.Object, int)
 	 */
 	public void notifyChanged(INodeNotifier notifier, int eventType,
 			Object changedFeature, Object oldValue, Object newValue, int pos) {
         // XXX: this can cause multiple refreshes on the same edit part for the 
         // same change.  I can also cause incorrect child refreshes...
 		refresh();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.parts.SubNodeEditPart#getNodeForFigure()
 	 */
 	public Node getNodeForFigure() {
 		return _tagConverter.getResultElement();
 	}
 
 	/**
	 * @return true this part's node has non whitespace child nodes
 	 */
 	public boolean haveNonWhitespaceTextChild() {
 		List children1 = this.getChildren();
 		for (int i = 0, size = children1.size(); i < size; i++) {
 			if (children1.get(i) instanceof TextEditPart) {
 				IDOMText xmltext = (IDOMText) ((TextEditPart) children1.get(i))
 						.getIDOMNode();
 				if (!xmltext.isElementContentWhitespace()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
     private ElementMenuBar getNonVisualElementBar()
     {
         if (_nonVisualElementBar == null)
         {
             _nonVisualElementBar = new ElementMenuBar(this);
         }
         return _nonVisualElementBar;
     }
 
    /**
     * @return the element menu bar for this element
     */
     public ElementMenuBar getElementMenuBar() {
         return getNonVisualElementBar();
     }
 
     public void deactivate() {
         super.deactivate();
         if (_nonVisualElementBar != null)
         {
             _nonVisualElementBar.dispose();
             _nonVisualElementBar = null;
         }
         if (_tagConverter != null) {
         	_tagConverter.dispose();
         }
     }
 
     public Cursor getCursor(Point mouseLocation) {
         // let the selection edit policy dictate
         EditPolicy  editPolicy = getEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE);
         
         if (editPolicy instanceof IEnhancedSelectionEditPolicy)
         {
             return  ((IEnhancedSelectionEditPolicy)editPolicy).getSelectionToolCursor(mouseLocation);
         }
         return super.getCursor(mouseLocation);
     }
     
     
 }
