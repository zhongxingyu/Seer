 package org.vaadin.vol.client.ui;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.vaadin.vol.client.Costants;
 import org.vaadin.vol.client.wrappers.GwtOlHandler;
 import org.vaadin.vol.client.wrappers.JsObject;
 import org.vaadin.vol.client.wrappers.Map;
 import org.vaadin.vol.client.wrappers.Projection;
 import org.vaadin.vol.client.wrappers.Style;
 import org.vaadin.vol.client.wrappers.StyleMap;
 import org.vaadin.vol.client.wrappers.Vector;
 import org.vaadin.vol.client.wrappers.control.Control;
 import org.vaadin.vol.client.wrappers.control.DrawFeature;
 import org.vaadin.vol.client.wrappers.control.ModifyFeature;
 import org.vaadin.vol.client.wrappers.control.SelectFeature;
 import org.vaadin.vol.client.wrappers.geometry.Geometry;
 import org.vaadin.vol.client.wrappers.geometry.LineString;
 import org.vaadin.vol.client.wrappers.geometry.Point;
 import org.vaadin.vol.client.wrappers.handler.PathHandler;
 import org.vaadin.vol.client.wrappers.handler.PointHandler;
 import org.vaadin.vol.client.wrappers.handler.PolygonHandler;
 import org.vaadin.vol.client.wrappers.layer.VectorLayer;
 
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.Container;
 import com.vaadin.terminal.gwt.client.Paintable;
 import com.vaadin.terminal.gwt.client.RenderSpace;
 import com.vaadin.terminal.gwt.client.UIDL;
 import com.vaadin.terminal.gwt.client.ValueMap;
 
 public class VVectorLayer extends FlowPanel implements VLayer, Container {
 
     private VectorLayer vectors;
     private String drawingMode = "NONE";
     private Control df;
     private GwtOlHandler _fAddedListener;
     private boolean updating;
     private ApplicationConnection client;
     private String displayName;
     private GwtOlHandler _fModifiedListener;
 
     private boolean immediate;
 
     public VectorLayer getLayer() {
         if (vectors == null) {
             vectors = VectorLayer.create(displayName);
             vectors.registerHandler("featureadded", getFeatureAddedListener());
             vectors.registerHandler("featuremodified",
                     getFeatureModifiedListener());
             vectors.registerHandler("afterfeaturemodified", new GwtOlHandler() {
                 @SuppressWarnings("rawtypes")
                 public void onEvent(JsArray arguments) {
                     if (updating) {
                         // ignore selections that happend during update, those
                         // should be already known and notified by the server
                         // side
                         return;
                     }
                     client.sendPendingVariableChanges();
                 }
             });
             vectors.registerHandler("featureselected", new GwtOlHandler() {
                 @SuppressWarnings("rawtypes")
                 public void onEvent(JsArray arguments) {
                     if (updating) {
                         // ignore selections that happend during update, those
                         // should be already known and notified by the server
                         // side
                         return;
                     }
                     if (client.hasEventListeners(VVectorLayer.this, "vsel")) {
                         ValueMap javaScriptObject = arguments.get(0).cast();
                         Vector vector = javaScriptObject.getValueMap("feature")
                                 .cast();
                         for (Widget w : getChildren()) {
                             VAbstractVector v = (VAbstractVector) w;
                             if (v.getVector() == vector) {
                                 client.updateVariable(paintableId, "vsel", v,
                                         true);
                             }
                         }
                     }
                 }
             });
 
             vectors.registerHandler("featureunselected", new GwtOlHandler() {
                 @SuppressWarnings("rawtypes")
                 public void onEvent(JsArray arguments) {
                     if (updating) {
                         // ignore selections that happend during update, those
                         // should be already known and notified by the server
                         // side
                         return;
                     }
                     ValueMap javaScriptObject = arguments.get(0).cast();
                     Vector vector = javaScriptObject.getValueMap("feature")
                             .cast();
                     for (Widget w : getChildren()) {
                         VAbstractVector v = (VAbstractVector) w;
                         if (v.getVector() == vector) {
                             v.revertDefaultIntent();
                             if (client.hasEventListeners(VVectorLayer.this,
                                     "vusel")) {
                                 client.updateVariable(paintableId, "vusel", v,
                                         true);
                                 break;
                             }
                         }
                     }
                 }
             });
 
         }
         return vectors;
     }
 
     private GwtOlHandler getFeatureModifiedListener() {
         if (_fModifiedListener == null) {
             _fModifiedListener = new GwtOlHandler() {
 
                 @SuppressWarnings("rawtypes")
                 public void onEvent(JsArray arguments) {
                     if (!updating && drawingMode != "NONE") {
                         // use vaadin js object helper to get the actual feature
                         // from ol event
                         // TODO improve typing of OL events
                         JsObject event = arguments.get(0).cast();
                         Vector feature = event.getFieldByName("feature").cast();
                         Geometry geometry = feature.getGeometry();
 
                         boolean isLineString = true; // TODO
                         if (isLineString) {
                             LineString ls = geometry.cast();
                             JsArray<Point> allVertices = ls.getAllVertices();
                             client.updateVariable(paintableId,
                                     "newVerticesProj", getMap().getProjection()
                                             .toString(), false);
                             String[] points = new String[allVertices.length()];
                             for (int i = 0; i < allVertices.length(); i++) {
                                 Point point = allVertices.get(i);
                                 point = point.nativeClone();
                                 point.transform(getMap().getProjection(),
                                         getProjection());
                                 points[i] = point.toString();
                             }
                             // VConsole.log("modified");
                             // communicate points to server and mark the
                             // new geometry to be removed on next update.
                             client.updateVariable(paintableId, "vertices",
                                     points, false);
 
                             Vector modifiedFeature = ((ModifyFeature) df.cast())
                                     .getModifiedFeature();
                             Iterator<Widget> iterator = iterator();
                             while (iterator.hasNext()) {
                                 VAbstractVector next = (VAbstractVector) iterator
                                         .next();
                                 Vector vector = next.getVector();
                                 if (vector == modifiedFeature) {
                                     client.updateVariable(paintableId,
                                             "modifiedVector", next, immediate);
                                     break;
                                 }
                             }
 
                             // client.sendPendingVariableChanges();
                             // lastNewDrawing = feature;
                         }
                     }
                 }
             };
         }
         return _fModifiedListener;
     }
 
     private String paintableId;
     private Vector lastNewDrawing;
     private boolean added = false;
     private String currentSelectionMode;
     private SelectFeature selectFeature;
 
     private GwtOlHandler getFeatureAddedListener() {
         if (_fAddedListener == null) {
             _fAddedListener = new GwtOlHandler() {
 
                 @SuppressWarnings("rawtypes")
                 public void onEvent(JsArray arguments) {
                     if (!updating && drawingMode != "NONE") {
                         // use vaadin js object helper to get the actual feature
                         // from ol event
                         // TODO improve typing of OL events
                         JsObject event = arguments.get(0).cast();
                         Vector feature = event.getFieldByName("feature").cast();
                         Geometry geometry = feature.getGeometry();
 
                         if (drawingMode == "AREA" || drawingMode == "LINE") {
                             LineString ls = geometry.cast();
                             JsArray<Point> allVertices = ls.getAllVertices();
                             // TODO this can be removed??
                             client.updateVariable(paintableId,
                                     "newVerticesProj", getMap().getProjection()
                                             .toString(), false);
                             String[] points = new String[allVertices.length()];
                             for (int i = 0; i < allVertices.length(); i++) {
                                 Point point = allVertices.get(i);
                                 point.transform(getMap().getProjection(),
                                         getProjection());
                                 points[i] = point.toString();
                             }
                             client.updateVariable(paintableId, "vertices",
                                     points, false);
                         } else if (drawingMode == "POINT") {
                             // point
                             Point point = geometry.cast();
                             point.transform(getMap().getProjection(),
                                     getProjection());
                             double x = point.getX();
                             double y = point.getY();
                             client.updateVariable(paintableId, "x", x, false);
                             client.updateVariable(paintableId, "y", y, false);
                         }
                         // VConsole.log("drawing done");
                         // communicate points to server and mark the
                         // new geometry to be removed on next update.
                         client.sendPendingVariableChanges();
                         if (drawingMode != "MODIFY") {
                             lastNewDrawing = feature;
                         }
                     }
                 }
             };
         }
         return _fAddedListener;
     }
 
     public void updateFromUIDL(UIDL layer, ApplicationConnection client) {
         if (client.updateComponent(this, layer, false)) {
             return;
         }
         this.client = client;
         paintableId = layer.getId();
         updating = true;
         displayName = layer.getStringAttribute("name");
         immediate = layer.getBooleanAttribute("immediate");
         if (!added) {
             getMap().addLayer(getLayer());
             added = true;
         }
 
         // Last new drawing only visible to next update. If used by server side
         // handler, we probably have it as a child component
         if (lastNewDrawing != null) {
             getLayer().removeFeature(lastNewDrawing);
             lastNewDrawing = null;
         }
 
         updateStyleMap(layer);
         setDrawingMode(layer.getStringAttribute("dmode"));
         setSelectionMode(layer);
 
         HashSet<Widget> orphaned = new HashSet<Widget>();
         for (Iterator<Widget> iterator = iterator(); iterator.hasNext();) {
             orphaned.add(iterator.next());
         }
 
         int childCount = layer.getChildCount();
         for (int i = 0; i < childCount; i++) {
             UIDL childUIDL = layer.getChildUIDL(i);
             VAbstractVector vector = (VAbstractVector) client
                     .getPaintable(childUIDL);
             boolean isNew = !hasChildComponent(vector);
             if (isNew) {
                 add(vector);
             }
             vector.updateFromUIDL(childUIDL, client);
             orphaned.remove(vector);
         }
         for (Widget widget : orphaned) {
             widget.removeFromParent();
         }
         updating = false;
     }
 
     private void setSelectionMode(UIDL layer) {
         String newSelectionMode = layer.getStringAttribute("smode").intern();
         if (currentSelectionMode != newSelectionMode) {
             if (selectFeature != null) {
                 selectFeature.deActivate();
                 getMap().removeControl(selectFeature);
                 selectFeature = null;
             }
 
             if (newSelectionMode != "NONE") {
                 selectFeature = SelectFeature.create(vectors);
                 getMap().addControl(selectFeature);
                 selectFeature.activate();
             }
 
             currentSelectionMode = newSelectionMode;
         }
         if (currentSelectionMode != "NONE" || drawingMode == "MODIFY") {
             if (layer.hasAttribute("svector")) {
                 VAbstractVector selectedVector = (VAbstractVector) layer
                         .getPaintableAttribute("svector", client);
                 if (selectedVector != null) {
                     // ensure selection
                     if (drawingMode == "MODIFY") {
                         ModifyFeature mf = (ModifyFeature) df.cast();
                         if(mf.getModifiedFeature() != null) {
                             mf.unselect(mf.getModifiedFeature());
                         }
                         mf.select(selectedVector.getVector());
                     } else {
                         selectFeature.select(selectedVector.getVector());
                     }
                 }
             } else {
                 // remove selection
                 if (drawingMode == "MODIFY") {
                     ModifyFeature modifyFeature = (ModifyFeature) df.cast();
                     if (modifyFeature.getModifiedFeature() != null) {
                         modifyFeature.unselect(modifyFeature
                                 .getModifiedFeature());
                     }
                 } else {
                    selectFeature.unselectAll();
                 }
             }
         }
     }
 
     private void setDrawingMode(String newDrawingMode) {
         newDrawingMode = newDrawingMode.intern();
         if (drawingMode != newDrawingMode) {
             if (drawingMode != "NONE") {
                 // remove old drawing feature
                 if(drawingMode == "MODIFY") {
                     ModifyFeature mf = df.cast();
                     if(mf.getModifiedFeature() != null) {
                         mf.unselect(mf.getModifiedFeature());
                     }
                 }
                 df.deActivate();
                 getMap().removeControl(df);
             }
             drawingMode = newDrawingMode;
             df = null;
 
             if (drawingMode == "AREA") {
                 df = DrawFeature.create(getLayer(), PolygonHandler.get());
             } else if (drawingMode == "LINE") {
                 df = DrawFeature.create(getLayer(), PathHandler.get());
             } else if (drawingMode == "MODIFY") {
                 df = ModifyFeature.create(getLayer());
             } else if (drawingMode == "POINT") {
                 df = DrawFeature.create(getLayer(), PointHandler.get());
             }
             if (df != null) {
                 getMap().addControl(df);
                 df.activate();
             }
 
         }
     }
 
     private void updateStyleMap(UIDL childUIDL) {
         StyleMap sm = getStyleMap(childUIDL);
         if (sm == null) {
             sm = StyleMap.create();
         }
 
         getLayer().setStyleMap(sm);
     }
 
     public static StyleMap getStyleMap(UIDL childUIDL) {
         if (!childUIDL.hasAttribute("olStyleMap")) {
             return null;
         }
         String[] renderIntents = childUIDL
                 .getStringArrayAttribute("olStyleMap");
         StyleMap sm;
         if (renderIntents.length == 1 && renderIntents[0].equals("default")) {
             sm = StyleMap.create();
             sm.setStyle(
                     "default",
                     Style.create(childUIDL.getMapAttribute(
                             "olStyle_" + renderIntents[0]).cast()));
         } else {
             sm = StyleMap.create();
             for (String intent : renderIntents) {
                 if (intent.startsWith("__")) {
                     String specialAttribute = intent.replaceAll("__", "");
                     if (specialAttribute.equals("extendDefault")) {
                         sm.setExtendDefault(true);
                     }
                 } else {
                     Style style = Style.create(childUIDL
                             .getMapAttribute("olStyle_" + intent));
                     sm.setStyle(intent, style);
                 }
             }
         }
 
         if (childUIDL.hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES)) {
             addUniqueValueRules(sm, childUIDL);
         }
         return sm;
     }
 
     private static void addUniqueValueRules(StyleMap sm, UIDL childUIDL) {
 
         if (childUIDL.hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_KEYS)) {
             String[] uvrKeysArray = childUIDL
                     .getStringArrayAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_KEYS);
 
             for (String uvrkey : uvrKeysArray) {
 
                 String property = childUIDL
                         .getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                 + uvrkey
                                 + Costants.STYLEMAP_UNIQUEVALUERULES_PROPERTY_SUFFIX);
                 String intent = childUIDL
                         .getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                 + uvrkey
                                 + Costants.STYLEMAP_UNIQUEVALUERULES_INTENT_SUFFIX);
                 // Object context =
                 // childUIDL.getStringAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX+uvrkey+Costants.STYLEMAP_UNIQUEVALUERULES_CONTEXT_SUFFIX);
 
                 if (childUIDL
                         .hasAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                 + uvrkey + "_lookupkeys")) {
                     String[] lookup_keys = childUIDL
                             .getStringArrayAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                     + uvrkey
                                     + Costants.STYLEMAP_UNIQUEVALUERULES_LOOKUPKEYS_SUFFIX);
 
                     JsObject symbolizer_lookup_js = JsObject.createObject();
 
                     for (String key : lookup_keys) {
 
                         ValueMap symbolizer = childUIDL
                                 .getMapAttribute(Costants.STYLEMAP_UNIQUEVALUERULES_PREFIX
                                         + uvrkey
                                         + Costants.STYLEMAP_UNIQUEVALUERULES_LOOKUPITEM_SUFFIX
                                         + key);
                         symbolizer_lookup_js
                                 .setProperty(key, symbolizer.cast());
                     }
 
                     sm.addUniqueValueRules(intent, property,
                             symbolizer_lookup_js.cast(), null);
                 }
             }
 
         }
 
         return;
     }
 
     @Override
     protected void onDetach() {
         super.onDetach();
         getMap().removeLayer(getLayer());
     }
 
     private Map getMap() {
         return getVMap().getMap();
     }
 
     private VOpenLayersMap getVMap() {
         return ((VOpenLayersMap) getParent().getParent());
     }
 
     protected Projection getProjection() {
         return getVMap().getProjection();
     }
 
     public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
         // TODO Auto-generated method stub
 
     }
 
     public boolean hasChildComponent(Widget component) {
         return getWidgetIndex(component) != -1;
     }
 
     public void updateCaption(Paintable component, UIDL uidl) {
         // TODO Auto-generated method stub
 
     }
 
     public boolean requestLayout(Set<Paintable> children) {
         // TODO Auto-generated method stub
         return false;
     }
 
     public RenderSpace getAllocatedSpace(Widget child) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public void vectorUpdated(VAbstractVector vAbstractVector) {
         // redraw
         getLayer().drawFeature(vAbstractVector.getVector());
         if (df != null) {
             String id = df.getId();
             if (id.contains("ModifyFeature")) {
                 ModifyFeature mf = df.cast();
                 Vector modifiedFeature = mf.getModifiedFeature();
                 if (modifiedFeature == vAbstractVector.getVector()) {
                     // VConsole.log("Whoops, modified on the server side " +
                     // "while currently being modified on the client side. " +
                     // "This may cause issues for OL unleass we notify " +
                     // "the ModifyFeature about the change.");
                     mf.resetVertices();
                 }
             }
         }
     }
 
 }
