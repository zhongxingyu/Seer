 /*
  * ImportFinalizer.java
  *
  * Created on 27. Oktober 2003, 11:26
  */
 package de.cismet.jpresso.core.kernel;
 
 import de.cismet.jpresso.core.serviceacceptor.ProgressListener;
 import de.cismet.jpresso.core.serviceprovider.FinalizerController;
 import de.cismet.jpresso.core.serviceprovider.exceptions.FinalizerException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Properties;
 import java.util.Enumeration;
 import javax.swing.table.TableModel;
 
 /**
  * @author  srichter
  * @author  hell
  */
 public class ImportFinalizer implements FinalizerController {
 
     /** Logger */
     //private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private static final org.apache.log4j.Logger sLog = org.apache.log4j.Logger.getLogger(ImportFinalizer.class);
     private final Class<?> finalizerClass;
     private final Object finalizerObject;
     private final Finalizer finalizer;
     private final ProgressListener progressListener;
     private String fullMethodName;
 //    private static final String FINALIZER_PACKAGE = "de.cismet.jpresso.core.finalizer.";
    private static final String FINALIZER_PACKAGE = Finalizer.class.getPackage().getName();
     private static final String SET = "set";
 
     //IntermedTablesContainer intermedTables;
     /** Creates a new instance of ImportFinalizer */
     public ImportFinalizer(String finalizerClassName, IntermedTablesContainer intermedTables, Properties props) throws FinalizerException {
         this(finalizerClassName, intermedTables, props, null);
     }
 
     public ImportFinalizer(final String finalizerClassName, final IntermedTablesContainer intermedTables, final Properties props, final ProgressListener progressListener) throws FinalizerException {
 
         //Abspeichern der intermediate Tables
         //this.intermedTables = intermedTables;
         this.progressListener = progressListener;
         String canonicalFinalizer = FINALIZER_PACKAGE + "." + finalizerClassName;
 
         try {
             //Suchen der Finisher Klasse
             finalizerClass = Class.forName(canonicalFinalizer);
 
             //Erzeugen eines Objektes des Finalizers
             finalizerObject = finalizerClass.newInstance();
             if (finalizerObject != null && finalizerObject instanceof Finalizer) {
                 finalizer = (Finalizer) finalizerObject;
             } else {
                 throw new FinalizerException("Finalizer Object is not an instance of Finalizer!");
             }
             //Suchen und Aufruf der Property-Methoden
             if (props != null) {
                 final Enumeration<?> propsEnum = props.propertyNames();
                 while (propsEnum.hasMoreElements()) {
                     final String name = propsEnum.nextElement().toString();
                     String methodName = name;
                     if (methodName != null) {
                         methodName = name.trim();
                         if (methodName.length() > 0) {
                             methodName = name.substring(0, 1).toUpperCase() + name.substring(1);
                         }
                     }
                     fullMethodName = SET + methodName;
                     try {
                         final Method m = finalizerClass.getMethod(fullMethodName, new Class[]{String.class});
                         m.invoke(finalizerObject, new Object[]{props.getProperty(name)});
                     } catch (NoSuchMethodException nsme) {
                         //find allowed methods via reflection to present them to the user as a hint
                         final StringBuilder allowedMethods = new StringBuilder();
                         final Method[] methods = finalizerClass.getDeclaredMethods();
                         for (final Method method : methods) {
                             final Class[] paramsTypes = method.getParameterTypes();
                             if (method.getName().startsWith(SET)) {
                                 allowedMethods.append("- ");
                                 allowedMethods.append(method.getName().substring(3));
                                 allowedMethods.append("(");
                                 for (int i = 0; i < paramsTypes.length; ++i) {
                                     allowedMethods.append(paramsTypes[i].getSimpleName());
                                     allowedMethods.append(" arg");
                                     allowedMethods.append(i);
                                     allowedMethods.append(", ");
                                 }
                                 allowedMethods.deleteCharAt(allowedMethods.length() - 1);
                                 allowedMethods.deleteCharAt(allowedMethods.length() - 1);
                                 allowedMethods.append(")");
                                 allowedMethods.append("\n");
                             }
                         }
                         if (allowedMethods.length() > 0) {
                             allowedMethods.deleteCharAt(allowedMethods.length() - 1);
                         }
                         throw new FinalizerException("Illegal Finalizer Parameter: Method " + fullMethodName + "(String arg) does not exist!\nPossible parameters:\n" + allowedMethods, nsme);
                     }
                 }
             }
         } catch (InvocationTargetException invEx) {
             throw new FinalizerException("Fehler im Finalizer (Initialisierungsphase)!", invEx.getCause());
         } catch (ClassNotFoundException cnfe) {
             throw new FinalizerException("Could not find Finalizer class named " + canonicalFinalizer, cnfe);
         } catch (IllegalArgumentException iae) {
             throw new FinalizerException("Illegal Finalizer Parameter Argument!", iae);
         } catch (Exception e) {
             throw new FinalizerException("Error in Finalizer!", e);
         }
         //Setzen der IntermedTables
         finalizer.setIntermedTables(intermedTables, progressListener);
 
     }
 
     @Override
     public TableModel getFinalizerOutputTable() {
         return finalizer;
     }
 
     /**
      * 
      * @return
      * @throws de.cismet.jpressocore.serviceprovider.exceptions.FinalizerException
      */
     @Override
     public long finalise() throws FinalizerException {
         if (finalizer != null) {
             try {
                 return finalizer.finalise();
             } catch (Exception ex) {
                 throw new FinalizerException("Fehler bei finalise():\n" + ex, ex);
             } finally {
                 if (progressListener != null) {
                     progressListener.finish();
                 }
             }
         } else {
             throw new FinalizerException("Finalizer is null!");
         }
     }
 
     @Override
     public void cancel() {
         if (finalizer != null) {
             finalizer.cancel();
         }
     }
 
     @Override
     public boolean isCanceled() {
         return finalizer.isCanceled();
     }
 
     @Override
     public String getLogs() {
         return finalizer.getLogs();
     }
 }
