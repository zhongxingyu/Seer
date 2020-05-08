 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.render.network;
 
 import java.awt.Point;
 import java.util.HashMap;
 
import org.eclipse.ui.IMemento;

 import net.refractions.udig.project.IPersister;
 
 /**
  * Persister for storing HashMap to IMemento
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class HashMapPersister extends IPersister<HashMap<Long, Point>> {
 
     /** String ID_PARAM field */
     private static final String ID_PARAM = "id";
     /** String Y_PARAM field */
     private static final String Y_PARAM = "y";
     /** String X_PARAM field */
     private static final String X_PARAM = "x";
     /** String POINT_PARAM field */
     private static final String POINT_PARAM = "point";
 
     @Override
     public Class getPersistee() {
         return HashMap.class;
     }
 
     @Override
     public HashMap<Long, Point> load(IMemento memento) {
         HashMap<Long, Point> result = new HashMap<Long, Point>();
         
         for (IMemento pointMemento : memento.getChildren(POINT_PARAM)) {
             int x = pointMemento.getInteger(X_PARAM);
             int y = pointMemento.getInteger(Y_PARAM);
             long id = new Long(pointMemento.getInteger(ID_PARAM));
             
             Point point = new Point(x, y);
             result.put(id, point);
         }
         
         return result;
     }
 
     @Override
     public void save(HashMap<Long, Point> object, IMemento memento) {
         for (Long key : object.keySet()) {
             Point point = object.get(key);
             IMemento pointChild = memento.createChild(POINT_PARAM);
             pointChild.putInteger(X_PARAM, point.x);
             pointChild.putInteger(Y_PARAM, point.y);
             pointChild.putInteger(ID_PARAM, key.intValue());            
         }
     }
 
 }
