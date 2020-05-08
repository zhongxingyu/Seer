 package de.htwg.se.battleship.util.observer;
 
import java.io.PrintWriter;
import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 /**
  * Observsable Class
  * 
  * @author aullik
  */
 public class Observable implements IObservable {
 
     private final Logger logger = Logger.getLogger("de.htwg.se.battleship.util.observer");
 
     /**
      * List of Observers
      */
     private final List<IObserver> subscribers = new ArrayList<IObserver>(1);
 
     @Override
     public void addObserver(IObserver s) {
         subscribers.add(s);
     }
 
     @Override
     public void removeObserver(IObserver s) {
         subscribers.remove(s);
     }
 
     @Override
     public void removeAllObservers() {
         subscribers.clear();
     }
 
     @Override
     public void notifyObservers() {
         notifyObservers(null);
     }
 
     @Override
     public void notifyObservers(Event e) {
         for (Iterator<IObserver> iter = subscribers.iterator(); iter.hasNext();) {
             IObserver observer = iter.next();
 
             if (e == null) {
                 observer.update(e);
                 continue;
             }
 
             try {
                 /* This method is not the best solution, but avoids many instanceofs within the update method */
                 Method update = observer.getClass().getMethod("update", e.getClass());
                 update.invoke(observer, e);
             } catch (InvocationTargetException e1) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e1.printStackTrace(pw);
                logger.error(sw.toString());
             } catch (Exception e1) {
                 observer.update(e);
             }
         }
     }
 }
