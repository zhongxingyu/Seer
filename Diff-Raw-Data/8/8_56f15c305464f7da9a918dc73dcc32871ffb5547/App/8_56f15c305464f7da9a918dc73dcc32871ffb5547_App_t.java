 package finance.candlePatterns;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import finance.candlePatterns.Core.Bar;
 import finance.candlePatterns.Core.BarFilter;
 import finance.candlePatterns.Core.FilterApplier;
 import finance.candlePatterns.IO.DataLoader;
 import finance.candlePatterns.IO.FinamDataParser;
 import finance.candlePatterns.IO.FinamParserFactory;
 import finance.candlePatterns.IO.ParserFactory;
 import finance.candlePatterns.Utils.Pair;
 
 /**
  *
  */
 public class App {
     private static final Logger log = Logger.getLogger(App.class);
     
     public static void main( String[] args ) {
         FilterCostructor ctor = new FilterCostructor(
             args[0],
             (args.length > 2)
                 ? Arrays.copyOfRange(args, 2, args.length)
                 : null
         );
         
         Pair<Constructor<?>, List<Object>> costructorInfo = ctor.getConstructor();
         
         if (null == costructorInfo) {
             log.info("Exit");
             return;
         }
         
         ParserFactory<FinamDataParser> pFactory =
             new FinamParserFactory<FinamDataParser>();
         
         DataLoader<FinamDataParser> loader = new DataLoader<FinamDataParser>(
             new File(args[1]),
             pFactory
         );
         
         List<Bar> bars = loader.load();
         
         BarFilter filter = null;
         
         System.out.println(costructorInfo.first);
         
         // create filter
         try {
             filter = (BarFilter) costructorInfo.first.newInstance(
                 costructorInfo.second.toArray()
             );
         } catch (IllegalArgumentException e) {
             log.fatal(e);
             return;
         } catch (InstantiationException e) {
             log.fatal(e);
             return;
         } catch (IllegalAccessException e) {
             log.fatal(e);
             return;
         } catch (InvocationTargetException e) {
             log.fatal(e);
             return;
         }
         
         FilterApplier applier = new FilterApplier(filter);
         applier.filtrate(bars);
     }
     
     private static class FilterCostructor {
         private final String filterClassName;
         private final String[] filterArgs;
         
         public FilterCostructor(String filterClassName, String[] filterArgs) {
             this.filterClassName = filterClassName;
             this.filterArgs = filterArgs;
         }
         
         /**
          * @return Constructor and typed args
          */
         public Pair<Constructor<?>, List<Object>> getConstructor() {
             log.debug("Filter class:"  + filterClassName);
             log.debug("Filter args:"  + filterArgs);
             
             Class<?> filterClass = null;
             // check class
             try {
                 filterClass = Class.forName(filterClassName);
             } catch (ClassNotFoundException e) {
                 log.fatal(e);
                 return null;
             }
             
            int filterArgsLength = (null != filterArgs) ? filterArgs.length : 0;
            
             // trying to create class
            Class<?>[] classParams = new Class<?>[filterArgsLength];
             
             List<Object> typedArgs = new ArrayList<Object>();
             
            for (int i = 0; i < filterArgsLength; i++) {
                 Integer intValue = getInt(filterArgs[i]);
                 
                 if (null != intValue) {
                     classParams[i] = intValue.getClass();
                     typedArgs.add(intValue);
                     continue;
                 }
                 
                 Double doubleValue = getDouble(filterArgs[i]);
                 
                 if (null != doubleValue) {
                     classParams[i] = doubleValue.getClass();
                     typedArgs.add(doubleValue);
                     continue;
                 }
                 
                 // string
                 classParams[i] = filterArgs[i].getClass();
                 typedArgs.add(filterArgs[i]);
             }
             
             Constructor<?> ctor;
             
             try {
                 log.debug(
                     String.format(
                         "Costructor params (%d): %s",
                         classParams.length,
                         classParams
                     )
                 );
                 
                 ctor = filterClass.getConstructor(classParams);
             } catch (SecurityException e) {
                 log.fatal(e);
                 return null;
             } catch (NoSuchMethodException e) {
                 log.fatal(e);
                 return null;
             }
             
             return new Pair<Constructor<?>, List<Object>>(ctor, typedArgs);
         }
         
         private Integer getInt(String arg) {
             try {
                 return Integer.parseInt(arg);
             } catch (NumberFormatException e) {
                 log.debug("Arg " + arg + " isn't Integer");
                 return null;
             }
         }
         
         private Double getDouble(String arg) {
             try {
                 return Double.parseDouble(arg);
             } catch (NumberFormatException e) {
                 log.debug("Arg " + arg + " isn't Double");
                 return null;
             }
         }
     }
 }
