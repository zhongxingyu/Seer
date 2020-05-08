 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.io.java;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.lexer.recognizer.regexp.RegExpPatternRecognizer;
 import org.modelcc.lexer.recognizer.regexp.RegExps;
 import org.modelcc.csm.language.MemberMappingComponent;
 import org.modelcc.io.ModelReader;
 import org.modelcc.metamodel.*;
 import org.modelcc.probabilistic.InvalidProbabilityValueException;
 import org.modelcc.probabilistic.NumericProbabilityEvaluator;
 import org.modelcc.probabilistic.Probability;
 import org.modelcc.probabilistic.ProbabilityEvaluator;
 import org.modelcc.tools.FieldFinder;
 import org.modelcc.tools.PrimitiveClasses;
 import org.modelcc.tools.RunTimeSubclassFinder;
 import org.modelcc.*;
 import org.modelcc.io.DefaultFilter;
 import org.modelcc.io.WarningExportHandler;
 import org.modelcc.io.WarningLogger;
 
 /**
  * Java to model reader
  * @author elezeta
  * @serial
  */
 public class JavaModelReader extends ModelReader implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Root class.
      */
     Class root;
 
     /**
      * Logger.
      */
     Logger logger;
 
     /**
      * Warning log.
      */
     private List<String> warnings;
     
     /**
      * Cosntructor
      * @param root the root class
      */
     public JavaModelReader(Class root) {
         this.root = root;
         WarningExportHandler weh = new WarningExportHandler();
         warnings = weh.getMessages();
         logger = new WarningLogger();
         logger.addHandler(weh);
         logger.setFilter(new DefaultFilter());
         
     }
 
 
     private void log(Level level, String string, Object[] object) {
         logger.log(level,string,object);
         Logger.getLogger(JavaModelReader.class.getName()).log(level,string,object);
     }
 
     
     /**
      * Reads a model from a root java class.
      * @param root the root class
      * @return the model
      * @throws Exception
      */
     public static Model read(Class root) throws Exception {
         JavaModelReader jmr = new JavaModelReader(root);
         return jmr.read();
     }
 
     /**
      * Reads a model from a root java class.
      * @return the model
      * @throws Exception
      */
     @Override
 	public Model read() throws Exception {
         warnings.clear();
 
         if (!IModel.class.isAssignableFrom(root))
             throw new ClassDoesNotExtendIModelException("Class "+root+" does not extend IModel.");
 
         Map<String,PatternRecognizer> pas = new HashMap<String,PatternRecognizer>();
         Set<ModelElement> elements = new HashSet<ModelElement>();
         Set<Class> relevantClasses = new HashSet<Class>();
         ModelElement start;
         Set<PatternRecognizer> delimiters = new HashSet<PatternRecognizer>();
         Map<Class,ModelElement> classToElement = new HashMap<Class,ModelElement>();
         Map<Class,PreElement> classToPreElement = new HashMap<Class,PreElement>();
         Map<ModelElement,Set<ModelElement>> precedences = new HashMap<ModelElement,Set<ModelElement>>();
         Map<ModelElement,Set<ModelElement>> subclasses = new HashMap<ModelElement,Set<ModelElement>>();
         Map<ModelElement,ModelElement> superclasses = new HashMap<ModelElement,ModelElement>();
 
         Map<PreElement,ModelElement> preElementToElement = new HashMap<PreElement,ModelElement>();
         Set<PreElement> preElements = new HashSet<PreElement>();
         Map<PreElement,Set<PreElement>> prePrecedences = new HashMap<PreElement,Set<PreElement>>();
         Map<PreElement,Set<PreElement>> preSubclasses = new HashMap<PreElement,Set<PreElement>>();
         Map<PreElement,PreElement> preSuperclasses = new HashMap<PreElement,PreElement>();
         Map<PreElement,Integer> priorities = new HashMap<PreElement,Integer>();
 
         Map<ModelElement, Set<ModelElement>> defaultElement = new HashMap<ModelElement, Set<ModelElement>>();
 
         // Detects relevants classes.
         relevantClasses = detectRelevantClasses(root);
 
         // Reads elements and contents.
         readClasses(relevantClasses,pas,preElements,delimiters,prePrecedences,priorities,preSubclasses,preSuperclasses,classToPreElement);
 
         // Inherit members.
         inheritMembers(preElements,preSubclasses);
         
         // Recursively manage attribute inheritance
         inheritAttributes(preElements,preSubclasses,priorities,prePrecedences);
 
         // Converts priority values to precedences.
         priorityToPrecede(preElements,priorities,prePrecedences);
 
         // Checks precedences for any cycles.
         checkPrecedences(preElements,prePrecedences);
 
         // Checks optionals.
         checkOptionals(preElements,classToPreElement);
 
         // Checks references.
         checkReferences(preElements,classToPreElement);
 
         // Convert PreElements to Elements (fixate them in order to encapsulate private members)
         fixateElements(preElements,elements,prePrecedences,precedences,subclasses,preSubclasses,superclasses,preSuperclasses,preElementToElement,classToElement);
 
         readPositions(elements);
         // Find defaultElements.
         findDefaultElements(elements,classToElement,subclasses,defaultElement);
 
         // Check cycles.
         checkCycles(elements,classToElement,subclasses);
 
         start = classToElement.get(root);
 
         return new Model(elements,start,delimiters,precedences,subclasses,superclasses,classToElement,defaultElement);
     }
 
 	private void readPositions(Set<ModelElement> elements) {
 		for (Iterator<ModelElement> ite = elements.iterator();ite.hasNext();) {
 			ModelElement elem = ite.next();
 	        Map<ElementMember,PositionInfo> positions = new HashMap<ElementMember,PositionInfo>();
 			if (ComplexModelElement.class.isAssignableFrom(elem.getClass())) {
 				ComplexModelElement celem = (ComplexModelElement)elem;
 		        Field[] fl = FieldFinder.getAllFields(elem.getElementClass());
 		        for (int i = 0;i < fl.length;i++) {
 		            Field field = fl[i];
 		            ElementMember thisElement = null;
 		            ElementMember otherElement = null;
 		            int indexThis;
 		            int indexOther;
 		        	indexThis = searchField(celem.getContents(),fl[i].getName());    
 		        	if (indexThis != -1)
 		        		thisElement = celem.getContents().get(indexThis);
 		        	
 		            if (field.isAnnotationPresent(Position.class)) {
 		            	Position positionTag = field.getAnnotation(Position.class);
 		            	Field otherField = null;
 			        	for (int j = 0;j < fl.length;j++) {
 			        		if (fl[j].getName().equals(positionTag.element()))
 			        			otherField = fl[j];
 			        	}
 
 		            	indexOther = searchField(celem.getContents(),positionTag.element());    
 		            	if (indexOther != -1)
 		            		otherElement = celem.getContents().get(indexOther);
 		
 		            	if (otherElement==null) {
 		                    log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Position annotation refers to an undefined field.", new Object[]{field.getName(),elem.getClass().getCanonicalName()});
 		            	}
 		            	else if (otherElement==thisElement) {
 		                    log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Position annotation refers to the same field.", new Object[]{field.getName(),elem.getClass().getCanonicalName()});
 		            	}
 		            	else if (MultipleElementMember.class.isAssignableFrom(thisElement.getClass()) &&
 		            			(positionContains(positionTag.position(),Position.BEFORELAST)||positionContains(positionTag.position(),Position.WITHIN))) {
 		                    log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Position annotation cannot be applied to a list and have BEFORELAST or WITHIN values.", new Object[]{field.getName(),elem.getClass().getCanonicalName()});
 		            	}
 		            	else if (!MultipleElementMember.class.isAssignableFrom(otherElement.getClass()) &&
 		            			(positionContains(positionTag.position(),Position.BEFORELAST)||positionContains(positionTag.position(),Position.WITHIN))) {
 		                    log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Position annotation cannot be applied to BEFORELAST or WITHIN a non-list element.", new Object[]{field.getName(),elem.getClass().getCanonicalName()});
 		            	}
 		            	else if (otherField.isAnnotationPresent(Position.class)) {
 	                        log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Position annotation cannot refer to a member annotated with @Position.", new Object[]{field.getName(),elem.getClass().getCanonicalName()});
 		            	}
 		            	else if (otherElement.isOptional()) {
 	                        log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Position annotation cannot refer to a member annotated with @Optional.", new Object[]{field.getName(),elem.getClass().getCanonicalName()});
 		            	}
 		            	else if (!compatible(field,fl)) {
 	                        log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": @Position clashes with another member.", new Object[]{field.getName(),elem.getClass().getCanonicalName()});
 		            	}
 
 		            	else {
 		            		positions.put(thisElement,new PositionInfo(otherElement,positionTag.position(),positionTag.separatorPolicy()));
 		            	}
 		            }
 		        }
 			}
 			elem.setPositions(positions);
 		}
 	}
 
 
 	private boolean positionContains(int[] haystack, int needle) {
 		for (int i = 0;i < haystack.length;i++)
 			if (haystack[i]==needle)
 				return true;
 		return false;
 	}
 
 
 	private boolean compatible(Field field, Field[] fl) {
 		Position positionTag = field.getAnnotation(Position.class);
     	for (int j = 0;j < fl.length;j++) {
     		if (fl[j] != field) {
 	            if (fl[j].isAnnotationPresent(Position.class)) {
 	            	Position otherPositionTag = fl[j].getAnnotation(Position.class);
 	            	if (positionTag.element()==otherPositionTag.element()) {
 
 	            		  if (
 	            				  (positionContains(positionTag.position(),Position.BEFORE) &&  (positionContains(otherPositionTag.position(),Position.BEFORE))) ||
 	            				  (positionContains(positionTag.position(),Position.AFTER) &&  (positionContains(otherPositionTag.position(),Position.AFTER))) ||
 	            				  (positionContains(positionTag.position(),Position.WITHIN) &&  (positionContains(otherPositionTag.position(),Position.WITHIN) || positionContains(otherPositionTag.position(),Position.BEFORELAST))) ||
 	            				  (positionContains(positionTag.position(),Position.BEFORELAST) &&  (positionContains(otherPositionTag.position(),Position.WITHIN) || positionContains(otherPositionTag.position(),Position.BEFORELAST)))
 	            			)
 	            			  return false;
 	            			  
 	            	}
 	            }
     		}
     	}
 		return true;
 	}
 
 
 	/**
      * Detects all the relevant classes from a root one
      * @param root the root class
      * @return the set of relevant classes
      * @throws ClassNotFoundException
      */
     private Set<Class> detectRelevantClasses(Class root) throws ClassNotFoundException {
 
         LinkedList<Class> q = new LinkedList<Class>();
         Set<Class> done = new HashSet<Class>();
 
         Class act;
         Class add;
         Field[] fl;
         Set<Class<?>> extendeds;
         int i;
 
         //Hack for avoiding the processing of String and Object classes, which makes no sense.
         done.add(String.class);
         done.add(Object.class);
 
         if (!root.getName().contains("$"))
         	q.addLast(root);
 
         while (!q.isEmpty()) {
             act = q.removeLast();
             done.add(act);
 
             // Adds precedences as relevant classes.
             if (act.isAnnotationPresent(Priority.class)) {
                 Priority an;
                 an = (Priority) act.getAnnotation(Priority.class);
                 for (int j = 0;j < an.precedes().length;j++) {
                     if (IModel.class.isAssignableFrom(an.precedes()[j]))
                         addClass(an.precedes()[j], q, done);
                 }
             }
 
             // Adds enclosing class as relevant class.
             if (act.getEnclosingClass() != null)
                 if (IModel.class.isAssignableFrom(act.getEnclosingClass()))
                     addClass(act.getEnclosingClass(),q,done);
             
             //Field processing (Containers, vectors and bare fields).
             if (!hasPattern(act)) {
                 fl = act.getDeclaredFields();
                 for (i = 0;i < fl.length;i++) {
                     CollectionType collection = null;
                     boolean avoid = false;
                  if (List.class.isAssignableFrom(fl[i].getType())) {
                         if (ArrayList.class.equals(fl[i].getType()) || (List.class.equals(fl[i].getType())))
                                 collection = CollectionType.LIST;
                         else
                             log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The class of a composite list may only be List or ArrayList.", new Object[]{fl[i].getName(), act.getCanonicalName()});
                     }
                     else if (Set.class.isAssignableFrom(fl[i].getType())) {
                         if (HashSet.class.equals(fl[i].getType()) || (Set.class.equals(fl[i].getType())))
                                 collection = CollectionType.SET;
                         else
                             log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The class of a composite set may only be Set or HashSet.", new Object[]{fl[i].getName(), act.getCanonicalName()});
                     }
                     else if (Map.class.isAssignableFrom(fl[i].getType())) {
                         avoid = true;
                     }
                     else if (fl[i].getType().isArray()) {
                         collection = CollectionType.LANGARRAY;
                     }
 
                     if (!avoid) {
                         add = getComponentType(collection,fl[i]);
                         if (add != null) {
                             if (IModel.class.isAssignableFrom(act))
                                 addClass(add, q, done);
                         }
                     }
                 }
 
             }
 
             // Adds subclasses as relevant classes.
             if (!act.isPrimitive()) {
                 String packageName = "";
                 if (act.getPackage() != null)
                     packageName = act.getPackage().getName();
                 Set<String> packages = new HashSet<String>();
                 packages.add("org.modelcc.types");
                 packages.add(packageName);
                 extendeds = RunTimeSubclassFinder.runTimeFindSubclasses(packageName,act);
                 for (Iterator<Class<?>> ite = extendeds.iterator();ite.hasNext();) {
                     add = ite.next();
                     addClass(add, q, done);
                 }
             }
             
             // Adds superclass as relevant classes.
             if (!act.isPrimitive()) {
                 add = act.getSuperclass();
                 addClass(add, q, done);
             }
 
         }
 
         //Revert hack for avoiding the processing of String and Object classes, which makes no sense.
         done.remove(Object.class);
         done.remove(String.class);
 
         return done;
     }
 
     /**
      * Checks whether a class has a pattern or value annotation
      * @param act the class
      * @return true if it has a pattern or value annotation, false otherwise
      */
     private boolean hasPattern(Class act) {
         if (act.isAnnotationPresent(Pattern.class))
             return true;
         else {
             Field fl[];
             fl = act.getDeclaredFields();
             int i;
             for (i = 0;i < fl.length;i++) {
                 if (fl[i].isAnnotationPresent(Value.class)) {
                    return true;
                 }
             }
             return false;
         }
     }
 
     /**
      * Adds a class to a queue if it hasn't been added before
      * @param add the class to add
      * @param q the queue
      * @param done the already added classes
      * @return true if it has been added now, false if it was added before
      * @throws ClassNotFoundException
      */
     private boolean addClass(Class add,LinkedList<Class> q,Set<Class> done) throws ClassNotFoundException {
         if (!done.contains(add) && !q.contains(add) && IModel.class.isAssignableFrom(add) && !add.getName().contains("$")) {
           q.addLast(add);
           return true;
         }
         return false;
     }
 
     /**
      * Reads all the classes information
      * @param relevantClasses the set of relevant classes
      * @param pas the regExp to pattern map
      * @param elements the set of elements
      * @param delimiters the set of delimiters
      * @param precedences the precedence map
      * @param priorities the priorities map
      * @param subclasses the subclasses map
      * @param superclasses the superclasses map
      * @param classToPreElement the class to preelement map
      * @throws ClassNotFoundException
      * @throws UnsupportedOperationException
      * @throws NoSuchMethodException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws IllegalArgumentException
      * @throws InvocationTargetException
      */
     private void readClasses(Set<Class> relevantClasses,Map<String,PatternRecognizer> pas,Set<PreElement> elements,Set<PatternRecognizer> delimiters,Map<PreElement,Set<PreElement>> precedences,Map<PreElement,Integer> priorities,Map<PreElement,Set<PreElement>> subclasses,Map<PreElement,PreElement> superclasses,Map<Class,PreElement> classToPreElement) throws ClassNotFoundException,UnsupportedOperationException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 
         Class cact;
         PreElement el;
 
         for (Iterator<Class> ite = relevantClasses.iterator();ite.hasNext();) {
             cact = ite.next();
             el = readClass(relevantClasses,cact,pas,delimiters,precedences,priorities,classToPreElement);
             elements.add(el);
         }
 
         for (Iterator<Class> ite = relevantClasses.iterator();ite.hasNext();) {
             cact = ite.next();
             if (!cact.isPrimitive()) {
                 Class sclass = cact.getSuperclass();
                 if (IModel.class.isAssignableFrom(sclass)) {
                     superclasses.put(classToPreElement.get(cact),classToPreElement.get(sclass));
                     Set<PreElement> se = subclasses.get(classToPreElement.get(sclass));
                     if (se == null) {
                         se = new HashSet<PreElement>();
                         subclasses.put(classToPreElement.get(sclass),se);
                     }
                     se.add(classToPreElement.get(cact));
                 }
             }
         }
 
 
         PreElement pe;
         for (Iterator<PreElement> ite = elements.iterator();ite.hasNext();) {
             pe = ite.next();
             Class elementClass = pe.getElementClass();
 
             // Map<PreElement,Set<PreElement>> precedences,Map<PreElement,Integer> priorities
             Integer priority = null;
             Set<PreElement> precede = new HashSet<PreElement>();
             if (elementClass.isAnnotationPresent(Priority.class)) {
                 Priority an;
                 an = (Priority) elementClass.getAnnotation(Priority.class);
                 priority = an.value();
                 for (int i = 0;i < an.precedes().length;i++) {
                     if (!IModel.class.isAssignableFrom(an.precedes()[i]))
                          log(Level.SEVERE, "In class \"{0}\": Preceded by class \"{1}\" but does not implement IModel.", new Object[]{an.precedes()[i].getCanonicalName(),elementClass.getCanonicalName()});
                     else
                         precede.add(classToPreElement.get(an.precedes()[i]));
                 }
             }
             if (!precede.isEmpty())
                 precedences.put(pe, precede);
             priorities.put(pe, priority);
         }
     }
 
     /**
      * Reads a class information
      * @param relevantClasses the set of relevant classes
      * @param elementClass the element class
      * @param pas the regExp to pattern map
      * @param elements the set of elements
      * @param delimiters the set of delimiters
      * @param precedences the precedence map
      * @param priorities the priorities map
      * @param classToPreElement the class to preelement map
      * @throws NoSuchMethodException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws IllegalArgumentException
      * @throws InvocationTargetException
      */
     private PreElement readClass(Set<Class> relevantClasses,Class elementClass,Map<String,PatternRecognizer> pas,Set<PatternRecognizer> delimiters,Map<PreElement,Set<PreElement>> precedences,Map<PreElement,Integer> priorities,Map<Class,PreElement> classToPreElement) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
         List<ElementMember> contents = new ArrayList<ElementMember>();
         List<ElementMember> ids = new ArrayList<ElementMember>();
         Boolean freeOrder = null;
         AssociativityType associativity = null;
         CompositionType composition = null;
         List<PatternRecognizer> prefix = null;
         List<PatternRecognizer> suffix = null;
         List<PatternRecognizer> separator = null;
         PatternRecognizer pattern = null;
         Field valueField = null;
         Method setupMethod = null;
         List<Method> constraintMethods = new ArrayList<Method>();
         ProbabilityEvaluator probabilityEvaluator = null;
 
         Field[] fl = elementClass.getDeclaredFields();
         //log(Level.INFO, "Reading class \"{0}\".", actname);
 
         //Field valueField = null;
         boolean shownValueError = false;
         for (int i = 0;i < fl.length;i++) {
             Field field = fl[i];
             if (field.isAnnotationPresent(Value.class)) {
                 if (valueField == null) {
                     valueField = fl[i];
                 }
                 else {
                     if (!shownValueError) {
                         log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Value annotation can only be assigned to a single field.", new Object[]{valueField.getName(), elementClass.getCanonicalName()});
                         shownValueError = true;
                     }
                     log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Value annotation can only be assigned to a single field.", new Object[]{fl[i].getName(), elementClass.getCanonicalName()});
                 }
             }
         }
         if (shownValueError)
             valueField = null;
 
 
         if (elementClass.isAnnotationPresent(Probability.class)) {
             Probability an;
             an = (Probability) elementClass.getAnnotation(Probability.class);
             if (!an.evaluator().equals(Probability.class)) {
 	            if (!ProbabilityEvaluator.class.isAssignableFrom(an.evaluator()))
 	            	log(Level.SEVERE, "In class \"{0}\": The @Probability class \"{1}\" does not extend ProbabilityEvaluator.", new Object[]{elementClass.getCanonicalName(),an.evaluator().getCanonicalName()});
 	            else {
 	                try {
 						probabilityEvaluator = (ProbabilityEvaluator) an.evaluator().getConstructor(String.class).newInstance(an.args());
 					} catch (Exception e) {
 		            	log(Level.SEVERE, "In class \"{0}\": Exception while instancing @Probability class \"{1}\".", new Object[]{elementClass.getCanonicalName(),an.evaluator().getCanonicalName()});
 					}
 	            }
             } else if (!(an.p() > -31337.1 && an.p() < -31336.9)) {
             	if (an.p() >= 0 && an.p() <= 1) {
             		try {
             			probabilityEvaluator = new NumericProbabilityEvaluator(an.p());
             		} catch (InvalidProbabilityValueException e) {
     	            	log(Level.SEVERE, "In class \"{0}\": Invalid probability p-value.", new Object[]{elementClass.getCanonicalName()});
             		}
             	}
             	else {
 	            	log(Level.SEVERE, "In class \"{0}\": Invalid probability p-value.", new Object[]{elementClass.getCanonicalName()});
             	}
             } else {
             	log(Level.SEVERE, "In class \"{0}\": @Probability annotation with neither evaluator nor p-value.", new Object[]{elementClass.getCanonicalName()});
             }
         }
         
         //PatternRecognizer pattern;
         if (elementClass.isAnnotationPresent(Pattern.class)) {
             Pattern p = (Pattern)elementClass.getAnnotation(Pattern.class);
             if (p.matcher() != Pattern.class && !p.regExp().equals("]]]]]]]]]]]]]")) {
                 log(Level.SEVERE, "In class \"{0}\": The @Pattern annotation cannot specify both a matcher class and a regular expression.", new Object[]{elementClass.getCanonicalName()});
                 if (valueField != null)
                     valueField = null;
             }
             else {
                 Pattern an;
                 an = (Pattern) elementClass.getAnnotation(Pattern.class);
                 if (!an.matcher().equals(Pattern.class)) {
                     if (!PatternRecognizer.class.isAssignableFrom(an.matcher()))
                         log(Level.SEVERE, "In class \"{0}\": The @Pattern class \"{1}\" does not extend PatternRecognizer.", new Object[]{elementClass.getCanonicalName(),an.matcher().getCanonicalName()});
                     else {
                         pattern = (PatternRecognizer) an.matcher().getConstructor(String.class).newInstance(an.args());
                     }
                 }
                 else if (!an.regExp().equals("]]]]]]]]]]]]]")) {
                     if (pas.containsKey(an.regExp()))
                             pattern = pas.get(an.regExp());
                     else {
                         pattern = new RegExpPatternRecognizer(an.regExp());
                         pas.put(an.regExp(),pattern);
                     }
                 }
                 else {
                     log(Level.SEVERE, "In class \"{0}\": The @Pattern annotation has neither regular expression nor matcher class.", new Object[]{elementClass.getCanonicalName()});
                 }
             }
         }
         if (valueField != null && pattern == null) {
                 String pat = RegExps.getPattern(valueField.getType());
                 if (pat != null) {
                     pattern = new RegExpPatternRecognizer(pat);
                     if (pas.containsKey(pat))
                             pattern = pas.get(pat);
                     else {
                         pattern = new RegExpPatternRecognizer(pat);
                         pas.put(pat,pattern);
                     }
                 }
                 else
                     log(Level.SEVERE, "In class \"{0}\": Found non-primitive @Value field and no valid @Pattern annotation.", new Object[]{elementClass.getCanonicalName()});
         }
 
 
         //Method setupMethod;
         {
 	        Method[] ml = elementClass.getDeclaredMethods();
 	        boolean shownMethodError = false;
 	        boolean shownMethodError1 = false;
 	        for (int i = 0;i < ml.length;i++) {
 	            if (ml[i].isAnnotationPresent(Setup.class)) {
 	                if (ml[i].getReturnType()!=void.class) {
 	                    log(Level.SEVERE, "In method \"{0}\" of class \"{1}\": The @Setup annotation can only be assigned to a method that returns void.", new Object[]{ml[i].getName(), elementClass.getCanonicalName()});
 	                    shownMethodError1 = true;
 	                }
 	                if (ml[i].getParameterTypes().length!=0) {
 	                    log(Level.SEVERE, "In method \"{0}\" of class \"{1}\": The @Setup annotation can only be assigned to a method with no parameters.", new Object[]{ml[i].getName(), elementClass.getCanonicalName()});
 	                    shownMethodError1 = true;
 	                }
 	                if (setupMethod == null) {
 	                    setupMethod = ml[i];
 	                }
 	                else {
 	                    if (!shownMethodError) {
 	                        log(Level.SEVERE, "In method \"{0}\" of class \"{1}\": The @Setup annotation can only be assigned to a single method.", new Object[]{setupMethod.getName(), elementClass.getCanonicalName()});
 	                        shownMethodError = true;
 	                    }
 	                    log(Level.SEVERE, "In method \"{0}\" of class \"{1}\": The @Setup annotation can only be assigned to a single method.", new Object[]{ml[i].getName(), elementClass.getCanonicalName()});
 	                }
 	            }
 	        }
 	        if (shownMethodError || shownMethodError1)
 	            setupMethod = null;
         }
         
         //Method constraintMethods;
         {
 	        Method[] ml = elementClass.getDeclaredMethods();
 	        boolean shownMethodError;
 	        for (int i = 0;i < ml.length;i++) {
 	        	shownMethodError = false;
 	            if (ml[i].isAnnotationPresent(Constraint.class)) {
 	                if (ml[i].getReturnType()!=boolean.class &&
 	                    ml[i].getReturnType()!=Boolean.class) {
 	                    log(Level.SEVERE, "In method \"{0}\" of class \"{1}\": The @Constraint annotation can only be assigned to a method that returns boolean or Boolean.", new Object[]{ml[i].getName(), elementClass.getCanonicalName()});
 	                    shownMethodError = true;
 	                }
 	                if (ml[i].getParameterTypes().length!=0) {
 	                    log(Level.SEVERE, "In method \"{0}\" of class \"{1}\": The @Constraint annotation can only be assigned to a method with no parameters.", new Object[]{ml[i].getName(), elementClass.getCanonicalName()});
 	                    shownMethodError = true;
 	                }
 	                if (!shownMethodError)
 	                	constraintMethods.add(ml[i]);
 	            }
 	        }
         }
      
         //List<ElementMember> contents = new ArrayList<ElementMember>();
         for (int i = 0;i < fl.length;i++) {
             Field field = fl[i];
             ElementMember c = readContent(relevantClasses,field,elementClass,pas,delimiters);
             if (c != null) {
                 contents.add(c);
                 if (c.isId())
                     ids.add(c);
             }
         }
 
         //boolean freeOrder;
         if (elementClass.isAnnotationPresent(FreeOrder.class))
             freeOrder = ((FreeOrder) elementClass.getAnnotation(FreeOrder.class)).value();
 
         //AssociativityType associativity;
         if (elementClass.isAnnotationPresent(Associativity.class))
             associativity = ((Associativity) elementClass.getAnnotation(Associativity.class)).value();
 
         //CompositionType composition;
         if (elementClass.isAnnotationPresent(Composition.class))
             composition = ((Composition) elementClass.getAnnotation(Composition.class)).value();
 
         //List<PatternRecognizer> prefix = new ArrayList<PatternRecognizer>();
         if (elementClass.isAnnotationPresent(Prefix.class)) {
             Prefix an;
             an = (Prefix) elementClass.getAnnotation(Prefix.class);
             PatternRecognizer pr;
             int k;
             prefix = new ArrayList<PatternRecognizer>();
             boolean err = false;
             for (k = 0;k < an.value().length;k++) {
                 if (an.value()[k] == null) {
                      log(Level.SEVERE, "In class \"{0}\": The @Prefix value cannot contain null.", new Object[]{elementClass.getCanonicalName()});
                      err = true;
                 }
                 else {
                     if (pas.containsKey(an.value()[k]))
                         pr = pas.get(an.value()[k]);
                     else {
                         pr = new RegExpPatternRecognizer(an.value()[k]);
                         pas.put(an.value()[k],pr);
                     }
                     prefix.add(pr);
                 }
             }
             if (err)
                 prefix = null;
             else
                 delimiters.addAll(prefix);
         }
 
         //List<PatternRecognizer> suffix = new ArrayList<PatternRecognizer>();
         if (elementClass.isAnnotationPresent(Suffix.class)) {
             Suffix an;
             an = (Suffix) elementClass.getAnnotation(Suffix.class);
             PatternRecognizer pr;
             int k;
             suffix = new ArrayList<PatternRecognizer>();
             boolean err = false;
             for (k = 0;k < an.value().length;k++) {
                 if (an.value()[k] == null) {
                      log(Level.SEVERE, "In class \"{0}\": The @Suffix value cannot contain null.", new Object[]{elementClass.getCanonicalName()});
                      err = true;
                 }
                 else {
                     if (pas.containsKey(an.value()[k]))
                         pr = pas.get(an.value()[k]);
                     else {
                         pr = new RegExpPatternRecognizer(an.value()[k]);
                         pas.put(an.value()[k],pr);
                     }
                     suffix.add(pr);
                 }
             }
             if (err)
                 suffix = null;
             else
                 delimiters.addAll(suffix);
         }
 
         //List<PatternRecognizer> separator = new ArrayList<PatternRecognizer>();
         if (elementClass.isAnnotationPresent(Separator.class)) {
             Separator an;
             an = (Separator) elementClass.getAnnotation(Separator.class);
             PatternRecognizer pr;
             int k;
             separator = new ArrayList<PatternRecognizer>();
             boolean err = false;
             for (k = 0;k < an.value().length;k++) {
                 if (an.value()[k] == null) {
                      log(Level.SEVERE, "In class \"{0}\": The @Separator value cannot contain null.", new Object[]{elementClass.getCanonicalName()});
                      err = true;
                 }
                 else {
                     if (pas.containsKey(an.value()[k]))
                         pr = pas.get(an.value()[k]);
                     else {
                         pr = new RegExpPatternRecognizer(an.value()[k]);
                         pas.put(an.value()[k],pr);
                     }
                     separator.add(pr);
                 }
             }
             if (err)
                 separator = null;
             else
                 delimiters.addAll(separator);
         }
 
         PreElement pe = new PreElement(elementClass,contents,ids,freeOrder,associativity,composition,prefix,suffix,separator,pattern,valueField,setupMethod,constraintMethods,probabilityEvaluator);
 
         classToPreElement.put(elementClass,pe);
 
         return pe;
     }
 
     private int searchField(List<ElementMember> contents, String name) {
     	int index = 0;
     	while (index<contents.size()) {
     		if (contents.get(index).getField().equals(name)) {
     			return index;
     		}
     		index++;
     	}
     	return -1;
 	}
 
 
 	/**
      * Reads a field information
      * @param relevantClasses the set of relevant classes
      * @param field the field
      * @param pas the regExp to pattern mapcannot be annotated with @Optional
      * @param delimiters the set of delimiters
      */
     private ElementMember readContent(Set<Class> relevantClasses,Field field,Class elementClass,Map<String,PatternRecognizer> pas,Set<PatternRecognizer> delimiters) {
         CollectionType collection = null;
         int minimumMultiplicity = -1;
         int maximumMultiplicity = -1;
         boolean optional = false;
         boolean id = false;
         boolean reference = false;
         List<PatternRecognizer> prefix = null;
         List<PatternRecognizer> suffix = null;
         List<PatternRecognizer> separator = null;
         Class contentClass;
         ProbabilityEvaluator probabilityEvaluator = null;
 
         //CollectionType collection = CollectionType.NO_COLLECTION;
         if (List.class.isAssignableFrom(field.getType())) {
             collection = CollectionType.LIST;
         }
         else if (Set.class.isAssignableFrom(field.getType())) {
             collection = CollectionType.SET;
         }
         else if (Map.class.isAssignableFrom(field.getType())) {
             return null;
         }
         else if (field.getType().isArray()) {
             collection = CollectionType.LANGARRAY;
         }
 
         contentClass = getComponentType(collection,field);
 
         
         if (!relevantClasses.contains(contentClass)) {
             if (field.isAnnotationPresent(ID.class) || field.isAnnotationPresent(Maximum.class) || field.isAnnotationPresent(Minimum.class) || field.isAnnotationPresent(Optional.class) || field.isAnnotationPresent(Prefix.class) || field.isAnnotationPresent(Suffix.class) || field.isAnnotationPresent(Separator.class) || field.isAnnotationPresent(Reference.class)) {
                 log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": Field class does not implement IModel but has ModelCC annotations.", new Object[]{field.getName(), elementClass.getCanonicalName()});
             }
             return null;
         }
     
         if (field.isAnnotationPresent(Optional.class))
             optional = true;
 
 
         if (field.isAnnotationPresent(ID.class)) {
             id = true;
             if (optional) {
                 id = false;
                 log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": A field annotated with @ID cannot be annotated with @Optional.", new Object[]{field.getName(), elementClass.getCanonicalName()});
             }
             
         }
 
         if (field.isAnnotationPresent(Reference.class)) {
             reference = true;
         }
         
         
         //int minimumMultiplicity = -1;
         //int maximumMultiplicity = -1;
         if (field.isAnnotationPresent(Minimum.class)) {
             minimumMultiplicity = field.getAnnotation(Minimum.class).value();
             if (minimumMultiplicity < 0) {
                 log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Minimum value has to be 0 or higher.", new Object[]{field.getName(), elementClass.getCanonicalName()});
                 minimumMultiplicity = -1;
             }
         }
 
         if (field.isAnnotationPresent(Maximum.class)) {
             maximumMultiplicity = field.getAnnotation(Maximum.class).value();
             if (maximumMultiplicity < 0) {
                 log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Maximum value has to be 0 or higher.", new Object[]{field.getName(), elementClass.getCanonicalName()});
                 maximumMultiplicity = -1;
             }
         }
 
         if (field.isAnnotationPresent(Minimum.class) && field.isAnnotationPresent(Maximum.class)) {
             if (minimumMultiplicity>maximumMultiplicity) {
                 log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Maximum value has to be the same or higher than the @Minimum value.", new Object[]{field.getName(), elementClass.getCanonicalName()});
                 maximumMultiplicity = -1;
             }
         }
 
         if ((field.isAnnotationPresent(Minimum.class) || field.isAnnotationPresent(Maximum.class)) && collection==null) {
                 log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Minimum or @Maximum values can only be set for container contents.", new Object[]{field.getName(), elementClass.getCanonicalName()});
                 maximumMultiplicity = -1;
                 minimumMultiplicity = -1;
         }
 
        
         //List<PatternRecognizer> pre = new ArrayList<PatternRecognizer>();
         if (field.isAnnotationPresent(Prefix.class)) {
             prefix = new ArrayList<PatternRecognizer>();
             Prefix an;
             an = field.getAnnotation(Prefix.class);
             PatternRecognizer pr;
             int k;
             for (k = 0;k < an.value().length;k++) {
                 if (an.value()[k] == null)
                      log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Prefix value cannot contain null.", new Object[]{field.getName(), elementClass.getCanonicalName()});
                 else {
                     if (pas.containsKey(an.value()[k]))
                         pr = pas.get(an.value()[k]);
                     else {
                         pr = new RegExpPatternRecognizer(an.value()[k]);
                         pas.put(an.value()[k],pr);
                     }
                     prefix.add(pr);
                     delimiters.add(pr);
                     //log(Level.INFO,"  Field prefix: RegExp \""+allList(an.value())+".");
                 }
             }
         }
 
         //List<PatternRecognizer> suf = new ArrayList<PatternRecognizer>();
         if (field.isAnnotationPresent(Suffix.class)) {
             suffix = new ArrayList<PatternRecognizer>();
             Suffix an;
             an = field.getAnnotation(Suffix.class);
             PatternRecognizer pr;
             int k;
             for (k = 0;k < an.value().length;k++) {
                 if (an.value()[k] == null)
                      log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Suffix value cannot contain null.", new Object[]{field.getName(), elementClass.getCanonicalName()});
                 else {
                     if (pas.containsKey(an.value()[k]))
                         pr = pas.get(an.value()[k]);
                     else {
                         pr = new RegExpPatternRecognizer(an.value()[k]);
                         pas.put(an.value()[k],pr);
                     }
                     suffix.add(pr);
                     delimiters.add(pr);
                     //log(Level.INFO,"  Field suffix: RegExp \""+allList(an.value())+".");
                 }
             }
         }
 
         //List<PatternRecognizer> sep = new ArrayList<PatternRecognizer>();
         if (field.isAnnotationPresent(Separator.class)) {
             separator = new ArrayList<PatternRecognizer>();
             Separator an;
             an = field.getAnnotation(Separator.class);
             PatternRecognizer pr;
             int k;
             for (k = 0;k < an.value().length;k++) {
                 if (an.value()[k] == null)
                      log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Separator value cannot contain null.", new Object[]{field.getName(), elementClass.getCanonicalName()});
                 else {
                     if (pas.containsKey(an.value()[k]))
                         pr = pas.get(an.value()[k]);
                     else {
                         pr = new RegExpPatternRecognizer(an.value()[k]);
                         pas.put(an.value()[k],pr);
                     }
                     separator.add(pr);
                     delimiters.add(pr);
                     //log(Level.INFO,"  Field separator: RegExp \""+allList(an.value())+".");
                 }
             }
         }
         
         if (field.isAnnotationPresent(Probability.class)) {
             Probability an;
             an = field.getAnnotation(Probability.class);
             if (!an.evaluator().equals(Probability.class)) {
 	            if (!ProbabilityEvaluator.class.isAssignableFrom(an.evaluator()))
 	            	log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": The @Probability class \"{2}\" does not extend ProbabilityEvaluator.", new Object[]{field.getName(), elementClass.getCanonicalName(),an.evaluator().getCanonicalName()});
 	            else {
 	                try {
 						probabilityEvaluator = (ProbabilityEvaluator) an.evaluator().getConstructor(String.class).newInstance(an.args());
 					} catch (Exception e) {
 		            	log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": Exception while instancing @Probability class \"{2}\".", new Object[]{field.getName(), elementClass.getCanonicalName(),an.evaluator().getCanonicalName()});
 					}
 	            }
             } else if (!(an.p() > -31337.1 && an.p() < -31336.9)) {
             	if (an.p() >= 0 && an.p() <= 1) {
             		try {
 						probabilityEvaluator = new NumericProbabilityEvaluator(an.p());
 					} catch (InvalidProbabilityValueException e) {
 		            	log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": Invalid probability p-value.", new Object[]{field.getName(), elementClass.getCanonicalName()});
 					}
             	}
             	else {
 	            	log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": Invalid probability p-value.", new Object[]{field.getName(), elementClass.getCanonicalName()});
             	}
             } else {
             	log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": @Probability annotation with neither evaluator nor p-value.", new Object[]{field.getName(), elementClass.getCanonicalName()});
             }
         }
         
         if (collection == null)
             return new ElementMember(field.getName(),contentClass,optional,id,reference,prefix,suffix,separator,probabilityEvaluator);
         else
             return new MultipleElementMember(field.getName(),contentClass,optional,id,reference,prefix,suffix,separator,collection,minimumMultiplicity,maximumMultiplicity,probabilityEvaluator);
     }
 
     /**
      * Returns a field component type
      * @param collection the collection type (null if no collection)
      * @param field the field
      * @return the component type
      */
     private Class getComponentType(CollectionType collection, Field field) {
 
         if (collection == null)
             return field.getType();
 
         switch (collection) {
             case LANGARRAY:
                 return field.getType().getComponentType();
             case LIST:
             case SET:
                 String name = field.getType().getName();
                 String fname = field.getGenericType().toString();
                 int ini = name.length()+1;
                 int fin = fname.length()-1;
                 if (ini < fin && fname.endsWith(">")) {
                     try {
                         return PrimitiveClasses.getClass(fname.substring(ini, fin));
                     } catch (ClassNotFoundException ex) {
                         return null;
                     }
                 }
         }
 
         return null;
 
     }
 
     /**
      * Manages the inheritance of the element members
      * @param elements the set of elements
      * @param subclasses the subclasses map
      */
     private void inheritMembers(Set<PreElement> elements,Map<PreElement,Set<PreElement>> subclasses) {
         PreElement source;
         PreElement target;
         Iterator<PreElement> it1 = elements.iterator();
         while (it1.hasNext()) {
             source = it1.next();
             if (!hasPattern(source.getElementClass())) {
                 Iterator<PreElement> ite;
                 if (subclasses.get(source) != null) {
                     ite = subclasses.get(source).iterator();
                         //log(Level.INFO,"Extending attributes from \""+source.getClassName()+"\":");
                         while (ite.hasNext()) {
                             target = ite.next();
                             inheritMemberClass(subclasses,source,target);
                         }
                     }
             }
             else {
                 //log(Level.INFO,"Class \""+source.getClassName()+"\" has a defined pattern, not extending attributes.");
             }
         }
     }
 
     /**
      * Inherits members from a source to a target element
      * @param subclasses the subclasses map
      * @param source the source element
      * @param target the target element
      */
     private void inheritMemberClass(Map<PreElement,Set<PreElement>> subclasses,PreElement source,PreElement target) {
         Iterator<PreElement> ite;
         PreElement target2;
 
         int j = 0;
         for (int i = 0;i < source.getContents().size();i++) {
         	ElementMember content = source.getContents().get(i);
         	if (!target.getContents().contains(content)) {
         		target.getContents().add(j,content);
         		j++;
         	}
         }
         if (subclasses.get(target) != null) {
             ite = subclasses.get(target).iterator();
             while (ite.hasNext()) {
                 target2 = ite.next();
                 inheritMemberClass(subclasses,target,target2);
             }
         }
     }
     
     /**
      * Manages the inheritance of the element attributes
      * @param elements the set of elements
      * @param subclasses the subclasses map
      * @param priorities the priorities map
      * @param precedences the precedence map
      */
     private void inheritAttributes(Set<PreElement> elements,Map<PreElement,Set<PreElement>> subclasses,Map<PreElement,Integer> priorities,Map<PreElement,Set<PreElement>> precedences) {
         PreElement source;
         PreElement target;
         //log(Level.INFO,"---");
         //log(Level.INFO,"Cascade extending attributes.");
         Iterator<PreElement> it1 = elements.iterator();
         while (it1.hasNext()) {
             source = it1.next();
             if (!hasPattern(source.getElementClass())) {
                 Iterator<PreElement> ite;
                 if (subclasses.get(source) != null) {
                     ite = subclasses.get(source).iterator();
                         //log(Level.INFO,"Extending attributes from \""+source.getClassName()+"\":");
                         while (ite.hasNext()) {
                             target = ite.next();
                             inheritAttributesClass(subclasses,source,target,priorities,precedences);
                         }
                     }
             }
             else {
                 //log(Level.INFO,"Class \""+source.getClassName()+"\" has a defined pattern, not extending attributes.");
             }
         }
         //log(Level.INFO,"---");
         //log(Level.INFO,"");
     }
 
     /**
      * Inherits attributes from a source to a target element
      * @param subclasses the subclasses map
      * @param source the source element
      * @param target the target element
      * @param priorities the priorities map
      * @param precedences the precedence map
      */
     private void inheritAttributesClass(Map<PreElement,Set<PreElement>> subclasses,PreElement source,PreElement target,Map<PreElement,Integer> priorities,Map<PreElement,Set<PreElement>> precedences) {
         Iterator<PreElement> ite;
         PreElement target2;
         //for (i = 0;i < depth;i++)
         //  deb.debugraw("  ");
         //log(Level.INFO,"  to \""+target.getClassName()+"\".");
 
         // Associativity
         if (target.getAssociativity()==null && source.getAssociativity()!= null) {
             target.setAssociativity(source.getAssociativity());
         }
         // Composition
         if (target.getComposition()==null && source.getComposition()!= null) {
             target.setComposition(source.getComposition());
         }
         // FreeOrder
         if (target.isFreeOrder()==null && source.isFreeOrder()!= null) {
             target.setFreeOrder(source.isFreeOrder());
         }
         // Separator
         if (target.getSeparator()==null && source.getSeparator()!= null) {
             target.setSeparator(source.getSeparator());
         }
         // Value
         if (target.getValueField()==null && source.getValueField()!= null) {
             target.setValueField(source.getValueField());
         }
         // Priority
         if (priorities.get(target) == null && priorities.get(source) != null) {
             priorities.put(target,priorities.get(source));
         }
         if (precedences.get(target) == null && precedences.get(source) != null) {
             precedences.put(target,precedences.get(source));
             if (precedences.get(target).contains(target))
                 precedences.remove(target);
         }
         // Probability
         if (target.getProbabilityEvaluator()==null && source.getProbabilityEvaluator()!= null) {
             target.setProbabilityEvaluator(source.getProbabilityEvaluator());
         }
 
         if (subclasses.get(target) != null) {
             ite = subclasses.get(target).iterator();
             while (ite.hasNext()) {
                 target2 = ite.next();
                 inheritAttributesClass(subclasses,target,target2,priorities,precedences);
             }
         }
 
     }
 
     /**
      * Converts priority values into precedence relationships
      * @param elements the set of elements
      * @param priorities the priorities map
      * @param precedences the precedence map
      */
     private void priorityToPrecede(Set<PreElement> elements,Map<PreElement,Integer> priorities,Map<PreElement,Set<PreElement>> precedences) {
         PreElement el;
         PreElement el2;
         for (Iterator<PreElement> it1 = elements.iterator();it1.hasNext();) {
             el = it1.next();
             Integer prio = priorities.get(el);
             if (prio != null) {
                 for (Iterator<PreElement> it2 = elements.iterator();it2.hasNext();) {
                     el2 = it2.next();
                     Integer prio2 = priorities.get(el2);
                     if (prio2 != null) {
                         if (prio < prio2) {
                             Set<PreElement> se = precedences.get(el);
                             if (se == null) {
                                 se = new HashSet<PreElement>();
                                 precedences.put(el,se);
                             }
                             se.add(el2);
                         }
                         else if (prio2 < prio) {
                             Set<PreElement> se = precedences.get(el2);
                             if (se == null) {
                                 se = new HashSet<PreElement>();
                                 precedences.put(el2,se);
                             }
                             se.add(el);
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Checks for cyclic precedences
      * @param elements the set of elements
      * @param precedences the precedence map
      */
     private void checkPrecedences(Set<PreElement> elements,Map<PreElement, Set<PreElement>> precedences) {
         Set<PreElement> pool = new HashSet<PreElement>();
         pool.addAll(elements);
 
 
         // -------------
         // Generate specification
         // -------------
 
         {
 
             // INITIALIZATION
             // --------------
 
             // Token specifications preceded by any token specification in the pool.
             Set<PreElement> precededs;
 
             // Auxiliar variables.
             Iterator<PreElement> ite;
             Iterator<PreElement> ite2;
             PreElement ts;
             PreElement ts2;
             Set<PreElement> pset;
 
             // Whether if any new token specification has been added to the sorted list.
             boolean found;
 
             // List of conflicting elements.
             String list;
 
             // PROCEDURE
             // --------------
 
             while (!pool.isEmpty()) {
 
                 found = false;
 
                 // Update precededs list.
                 precededs = new HashSet<PreElement>();
                 for (ite = pool.iterator();ite.hasNext();) {
                     ts = ite.next();
                     pset = precedences.get(ts);
                     if (pset != null) {
                         precededs.addAll(pset);
                     }
                 }
 
                 // Adds news unprecededs.
                 for (ite = pool.iterator();ite.hasNext();) {
                     ts = ite.next();
                     if (!precededs.contains(ts)) {
                         ite.remove();
                         found = true;
                     }
                 }
 
                 if (!found) {
                     list = new String();
                     for (ite = pool.iterator();ite.hasNext();)
                         list += " "+ite.next().getElementClass().getCanonicalName();
                     log(Level.SEVERE, "Cyclic precedence exception:{0}.", new Object[]{list.toString()});
                     for (ite = pool.iterator();ite.hasNext();) {
                         ts = ite.next();
                         pset = precedences.get(ts);
                         if (pset != null) {
                             for (ite2 = pool.iterator();ite2.hasNext();) {
                                 ts2 = ite2.next();
                                 pset.remove(ts2);
                             }
                             if (pset.isEmpty())
                                 precedences.remove(ts);
                         }
                     }
                     return;
                 }
             }
         }
     }
 
     /**
      * Fixates elements, converting them from PreElements to Elements
      * @param preElements the set of preelements
      * @param elements the set of elements
      * @param prePrecedences the precedence between preelements map
      * @param precedences the precedence map
      * @param subclasses the subclasses map
      * @param preSubclasses the preelement subclasses map
      * @param superclasses the superclasses map
      * @param preSuperclasses the preelement superclasses map
      * @param preElementToElement the preelement to element map
      * @param classToElement the class to element map
      */
     private void fixateElements(Set<PreElement> preElements, Set<ModelElement> elements, Map<PreElement, Set<PreElement>> prePrecedences, Map<ModelElement, Set<ModelElement>> precedences, Map<ModelElement, Set<ModelElement>> subclasses, Map<PreElement, Set<PreElement>> preSubclasses, Map<ModelElement, ModelElement> superclasses, Map<PreElement, PreElement> preSuperclasses,Map<PreElement,ModelElement> preElementToElement,Map<Class,ModelElement> classToElement) {
         PreElement pe;
         ModelElement e;
         for (Iterator<PreElement> ite = preElements.iterator();ite.hasNext();) {
             pe = ite.next();
             Class elementClass;
             List<ElementMember> contents;
             List<ElementMember> ids;
             boolean freeOrder;
             AssociativityType associativity;
             CompositionType composition;
             List<PatternRecognizer> prefix;
             List<PatternRecognizer> suffix;
             List<PatternRecognizer> separator;
             PatternRecognizer pattern;
             Field valueField;
             Method setupMethod;
             elementClass = pe.getElementClass();
             contents = pe.getContents();
             ids = pe.getIds();
             boolean hasAnyAssociativity;
             ProbabilityEvaluator probabilityEvaluator;
             
             if (pe.isFreeOrder() != null)
                 freeOrder = pe.isFreeOrder();
             else
                 freeOrder = false;
             if (pe.getAssociativity() == null)
                 associativity = AssociativityType.UNDEFINED;
             else
                 associativity = pe.getAssociativity();
             if (pe.getComposition() == null)
                 composition = CompositionType.UNDEFINED;
             else
                 composition = pe.getComposition();
 
             if (pe.getPrefix()==null)
                 prefix = null;
             else
                 prefix = pe.getPrefix();
             if (pe.getSuffix()==null)
                 suffix = null;
             else
                 suffix = pe.getSuffix();
             if (pe.getSeparator()==null)
                 separator = null;
             else
                 separator = pe.getSeparator();
 
             pattern = pe.getPattern();
             valueField = pe.getValueField();
             setupMethod = pe.getSetupMethod();
             hasAnyAssociativity = pe.getHasAnyAssociativity();
             probabilityEvaluator = pe.getProbabilityEvaluator();
 
             String valueFieldName = null;
             if (valueField != null)
                 valueFieldName = valueField.getName();
 
             String setupMethodName = null;
             if (setupMethod != null)
                 setupMethodName = setupMethod.getName();
 
             List<String> constraintMethodNames = new ArrayList<String>();
             for (int i = 0;i < pe.getConstraintMethods().size();i++) {
             	constraintMethodNames.add(pe.getConstraintMethods().get(i).getName());
             }
             
             e = null;
             if (Modifier.isAbstract(elementClass.getModifiers()) && preSubclasses.get(pe) == null) {
             	e = new ChoiceModelElement(elementClass,associativity,prefix,suffix,separator,setupMethodName,constraintMethodNames,hasAnyAssociativity,probabilityEvaluator);
                  log(Level.SEVERE, "In class \"{0}\": Abstract class without subclasses.", new Object[]{elementClass.getCanonicalName()});
             }
             else if (pe.getContents().isEmpty() && preSubclasses.get(pe) != null) {
                 e = new ChoiceModelElement(elementClass,associativity,prefix,suffix,separator,setupMethodName,constraintMethodNames,hasAnyAssociativity,probabilityEvaluator);
             }
             else if (pe.getPattern() != null) {
                 if (!hasConstructor(elementClass))
                     log(Level.SEVERE, "In class \"{0}\": Elements containing @Pattern or @Value need to implement a public parameterless constructor.", new Object[]{elementClass.getCanonicalName()});
                     e = new BasicModelElement(elementClass,associativity,prefix,suffix,separator,setupMethodName,constraintMethodNames,pattern,valueFieldName,hasAnyAssociativity,probabilityEvaluator);
             }
             else {
                 e = new ComplexModelElement(elementClass,associativity,prefix,suffix,separator,setupMethodName,constraintMethodNames,contents,ids,freeOrder,composition,hasAnyAssociativity,probabilityEvaluator);
                 if (hasPattern(elementClass)) {
                     contents = new ArrayList<ElementMember>();
                 }
                 if (!hasConstructor(elementClass))
                     log(Level.SEVERE, "In class \"{0}\": Composite elements need to implement a public parameterless constructor.", new Object[]{elementClass.getCanonicalName()});
                 if (contents.isEmpty() && !hasPattern(elementClass) && preSubclasses.get(pe) == null)
                     log(Level.SEVERE, "In class \"{0}\": Empty composite element.", new Object[]{elementClass.getCanonicalName()});
             }
             elements.add(e);
             preElementToElement.put(pe, e);
             classToElement.put(e.getElementClass(), e);
         }
         Iterator<PreElement> ite;
         Iterator<PreElement> ite2;
         PreElement k;
         for (ite = prePrecedences.keySet().iterator();ite.hasNext();) {
             k = ite.next();
             Set<ModelElement> ns = new HashSet<ModelElement>();
             for (ite2 = prePrecedences.get(k).iterator();ite2.hasNext();)
                 ns.add(preElementToElement.get(ite2.next()));
             if (!ns.isEmpty())
                 precedences.put(preElementToElement.get(k), ns);
         }
         for (ite = preSubclasses.keySet().iterator();ite.hasNext();) {
             k = ite.next();
             Set<ModelElement> ns = new HashSet<ModelElement>();
             for (ite2 = preSubclasses.get(k).iterator();ite2.hasNext();)
                 ns.add(preElementToElement.get(ite2.next()));
             if (!ns.isEmpty())
                 subclasses.put(preElementToElement.get(k), ns);
         }
         Iterator<Entry<PreElement,PreElement>> itee;
         Entry<PreElement,PreElement> entr;
         for (itee = preSuperclasses.entrySet().iterator();itee.hasNext();) {
             entr = itee.next();
             superclasses.put(preElementToElement.get(entr.getKey()), preElementToElement.get(entr.getValue()));
         }
     }
 
     private boolean hasConstructor(Class elementClass) {
         try {
             if (elementClass.getConstructor() != null)
                 return true;
         } catch (Exception e) {
 
         }
         return false;
     }
 
     private void checkOptionals(Set<PreElement> preElements,Map<Class,PreElement> classToPreElement) {
         for (Iterator<PreElement> ite = preElements.iterator();ite.hasNext();) {
             PreElement pe = ite.next();
             for (int i = 0;i < pe.getContents().size();i++) {
                 ElementMember em = pe.getContents().get(i);
                 if (em.isOptional()) {
                     if (!MultipleElementMember.class.isAssignableFrom(em.getClass())) {
                         PreElement pe2 = classToPreElement.get(em.getElementClass());
                         boolean allopt = true;
                         if (pe2.getContents().isEmpty())
                             allopt = false;
                         for (Iterator<ElementMember> itec2 = pe2.getContents().iterator();itec2.hasNext() && allopt;) {
                             ElementMember em2 = itec2.next();
                             if (!em2.isOptional())
                                 allopt = false;
                         }
                         if (allopt) {
                             pe.getContents().set(i,new ElementMember(em.getField(),em.getElementClass(),false,em.isId(),em.isReference(),em.getPrefix(),em.getSuffix(),em.getSeparator(),em.getProbabilityEvaluator()));
                             log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": This field is annotated with @Optional and all its contents are also @Optional, the field @Optional annotation is redundant.", new Object[]{em.getField(), pe.getElementClass().getCanonicalName()});
                         }
                     }
                     else {
                         MultipleElementMember mem = (MultipleElementMember)em;
                         if (mem.getMinimumMultiplicity()==0 && mem.getPrefix() == null && mem.getSuffix() == null) {
                             pe.getContents().set(i,new MultipleElementMember(em.getField(),em.getElementClass(),false,em.isId(),em.isReference(),em.getPrefix(),em.getSuffix(),em.getSeparator(),mem.getCollection(),mem.getMinimumMultiplicity(),mem.getMaximumMultiplicity(),em.getProbabilityEvaluator()));
                             log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": This field has minimum multiplicity 0 and is redundantly optional because it has not prefixes or suffixes.", new Object[]{mem.getField(), pe.getElementClass().getCanonicalName()});
                         }
                     }
                 }
             }
         }
         // Para cada elemento, ver sus contenidos
     }
 
     /**
      * @return the warnings
      */
     public List<String> getWarnings() {
         return Collections.unmodifiableList(warnings);
     }
 
     private void checkReferences(Set<PreElement> preElements, Map<Class, PreElement> classToPreElement) {
         for (Iterator<PreElement> ite = preElements.iterator();ite.hasNext();) {
             PreElement pe = ite.next();
             for (int i = 0;i < pe.getContents().size();i++) {
                 ElementMember em = pe.getContents().get(i);
                 if (em.isReference()) {
                     PreElement peref = classToPreElement.get(em.getElementClass());
                     if (peref.getIds().isEmpty()) {
                         if (!MultipleElementMember.class.isAssignableFrom(em.getClass())) {
                             pe.getContents().set(i,new ElementMember(em.getField(),em.getElementClass(),em.isOptional(),em.isId(),false,em.getPrefix(),em.getSuffix(),em.getSeparator(),em.getProbabilityEvaluator()));
                             log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": This field is annotated with @Reference but its field class has no @ID members.", new Object[]{em.getField(), pe.getElementClass().getCanonicalName()});
                         }
                         else {
                             MultipleElementMember mem = (MultipleElementMember)em;
                             pe.getContents().set(i,new MultipleElementMember(em.getField(),em.getElementClass(),em.isOptional(),em.isId(),false,em.getPrefix(),em.getSuffix(),em.getSeparator(),mem.getCollection(),mem.getMinimumMultiplicity(),mem.getMaximumMultiplicity(),em.getProbabilityEvaluator()));
                             log(Level.SEVERE, "In field \"{0}\" of class \"{1}\": This field is annotated with @Reference but its field class has no @ID members.", new Object[]{em.getField(), pe.getElementClass().getCanonicalName()});
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Find default elements. When an abstract element matches the empty string, one of its subclasses is its default element.
      * @param elements Element list
      * @param defaultElement Output default element list
      */
     private void findDefaultElements(Set<ModelElement> elements,Map<Class,ModelElement> classToElement,Map<ModelElement,Set<ModelElement>> subclasses,Map<ModelElement, Set<ModelElement>> defaultElement) {
     	for (Iterator<ModelElement> ite = elements.iterator();ite.hasNext();) {
     		ModelElement e = ite.next();
     		Set<ModelElement> defaultElements = defaultElement.get(e);
     		if (Modifier.isAbstract(e.getElementClass().getModifiers())) {
     			if (subclasses.containsKey(e)) {
 	    			for (Iterator<ModelElement> ites = subclasses.get(e).iterator();ites.hasNext();) {
 	    	    		ModelElement es = ites.next();
 	    	    		if (canMatchEmptyString(es,subclasses,classToElement,new HashSet<ModelElement>())) {
 	    	    			if (defaultElements == null)
 	    	    				defaultElements = new HashSet<ModelElement>();
 	    	    			defaultElements.add(es);
 	    	    		}
 	    	    	}
     			}
     		}
     		if (defaultElements != null)
     			defaultElement.put(e, defaultElements);
     			defaultElements = null;
     	}
     	for (Iterator<ModelElement> ite = elements.iterator();ite.hasNext();) {
     		ModelElement e = ite.next();
     		Set<ModelElement> defaultElements = defaultElement.get(e);
     		if (defaultElements != null) {
     			if (defaultElements.size()>1) {
     				for (Iterator<ModelElement> iten = defaultElements.iterator();iten.hasNext();) {
                         log(Level.SEVERE, "In class \"{0}\": Multiple empty matching patterns: {1}.", new Object[]{e.getElementClass().getCanonicalName(), iten.next().getElementClass().getCanonicalName()});
     				}
     			}
     		}
     	}
 	}
 
 
 	private boolean canMatchEmptyString(ModelElement es,Map<ModelElement,Set<ModelElement>> subclasses,Map<Class,ModelElement> classToElement,Set<ModelElement> history) {
 		if ((es.getPrefix()!=null)) {
 			for (Iterator<PatternRecognizer> ite = es.getPrefix().iterator();ite.hasNext();) {
				if (ite.next().read("",0) != null) {
 					return false;
 				}
 			}
 		}
 		if ((es.getSuffix()!=null)) {
 			for (Iterator<PatternRecognizer> ite = es.getSuffix().iterator();ite.hasNext();) {
				if (ite.next().read("",0) != null) {
 					return false;
 				}
 			}
 		}
  		if (BasicModelElement.class.isAssignableFrom(es.getClass())) {
 			BasicModelElement bes = (BasicModelElement)es;
 			if (bes.getPattern().read("",0) != null) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		else if (ChoiceModelElement.class.isAssignableFrom(es.getClass())) {
 			ChoiceModelElement ces = (ChoiceModelElement)es;
 			if (subclasses.containsKey(ces)) {
 				for (Iterator<ModelElement> ite = subclasses.get(ces).iterator();ite.hasNext();) {
 					ModelElement me = ite.next();
 					if (!history.contains(me)) {
 						Set<ModelElement> history2 = new HashSet<ModelElement>();
 						history2.addAll(history);
 						history2.add(es);
 						if (canMatchEmptyString(me,subclasses,classToElement,history2)) {
 							return true;
 						}
 					}
 				}
 			}
 			return false;
 		}
 		else if (ComplexModelElement.class.isAssignableFrom(es.getClass())) {
 			ComplexModelElement ces = (ComplexModelElement)es;
 			for (int i = 0;i < ces.getContents().size();i++) {
 				ElementMember em = ces.getContents().get(i);
 				if (!em.isOptional()) {
 					if (em.getPrefix()!=null) {
 						for (Iterator<PatternRecognizer> ite = em.getPrefix().iterator();ite.hasNext();) {
 							if (ite.next().read("",0) == null) {
 								return false;
 							}
 						}
 					}
 					if (em.getSuffix()!=null) {
 						for (Iterator<PatternRecognizer> ite = em.getSuffix().iterator();ite.hasNext();) {
 							if (ite.next().read("",0) == null) {
 								return false;
 							}
 						}
 					}
 					if (MultipleElementMember.class.isAssignableFrom(em.getClass())) {
 						MultipleElementMember mem = (MultipleElementMember)em;
 						if (mem.getMinimumMultiplicity()>0) {
 							if (em.getSeparator()!=null) {
 								for (Iterator<PatternRecognizer> ite = em.getSeparator().iterator();ite.hasNext();) {
 									if (ite.next().read("",0) == null) {
 										return false;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 
 			boolean anything = false;
 			for (int i = 0;i < ces.getContents().size();i++) {
 				ElementMember em = ces.getContents().get(i);
 				if (!em.isOptional()) {
 					Set<ModelElement> history2 = new HashSet<ModelElement>();
 					history2.addAll(history);
 					history2.add(es);
 					if (!canMatchEmptyString(classToElement.get(em.getElementClass()),subclasses,classToElement,history2)) {
 						anything = true;
 					}
 				}
 			}
 
 			for (int i = 0;i < ces.getContents().size();i++) {
 				ElementMember em = ces.getContents().get(i);
 				if (!em.isOptional()) {
 					ModelElement emc = classToElement.get(em.getElementClass());
 					Set<ModelElement> history2 = new HashSet<ModelElement>();
 					history2.addAll(history);
 					history2.add(es);
 					if (!canMatchEmptyString(emc,subclasses,classToElement,history2)) {
 						anything = true;
 					}
 				}
 			}
 			if (!anything)
 				return true;
 
 
 		}
 		return false;
 	}
 	
 	private void checkCycles(Set<ModelElement> elements,
 			Map<Class, ModelElement> classToElement,
 			Map<ModelElement, Set<ModelElement>> subclasses) {
 		for (Iterator<ModelElement> ite = elements.iterator();ite.hasNext();) {
 			ModelElement e = ite.next();
 			checkCycle(e,e,classToElement,subclasses);
 		}
 	}
 
 
 	private void checkCycle(ModelElement es, ModelElement orig,
 			Map<Class, ModelElement> classToElement,
 			Map<ModelElement, Set<ModelElement>> subclasses) {
 		if ((es.getPrefix()!=null)) {
 			for (Iterator<PatternRecognizer> ite = es.getPrefix().iterator();ite.hasNext();) {
 				if (ite.next().read("",0) == null) {
 					return;
 				}
 			}
 		}
 		if ((es.getSuffix()!=null)) {
 			for (Iterator<PatternRecognizer> ite = es.getSuffix().iterator();ite.hasNext();) {
 				if (ite.next().read("",0) == null) {
 					return;
 				}
 			}
 		}
  		if (BasicModelElement.class.isAssignableFrom(es.getClass())) {
  			return;
 		}
 		else if (ChoiceModelElement.class.isAssignableFrom(es.getClass())) {
 			ChoiceModelElement ces = (ChoiceModelElement)es;
 			if (subclasses.containsKey(ces)) {
 				boolean hasOtherThanOrig = false;
 				boolean hasOrig = false;
 				for (Iterator<ModelElement> ite = subclasses.get(ces).iterator();ite.hasNext();) {
 					ModelElement me = ite.next();
 					if (me.equals(orig)) {
 						hasOrig = true;
 						if ((me.getPrefix()!=null)) {
 							for (Iterator<PatternRecognizer> ite2 = me.getPrefix().iterator();ite2.hasNext();) {
 								if (ite2.next().read("",0) == null) {
 									hasOtherThanOrig = true;
 								}
 							}
 						}
 						if ((me.getSuffix()!=null)) {
 							for (Iterator<PatternRecognizer> ite2 = me.getSuffix().iterator();ite2.hasNext();) {
 								if (ite2.next().read("",0) == null) {
 									hasOtherThanOrig = true;
 								}
 							}
 						}
 					}
 					else {
 						hasOtherThanOrig = true;
 					}
 					checkCycle(me,orig,classToElement,subclasses);
 				}
 				if (hasOrig && !hasOtherThanOrig) {
                     log(Level.SEVERE, "Class \"{0}\" cycles with recursive inheritance or composition.", new Object[]{orig.getElementClass().getCanonicalName()});
 				}
 			}
 			return;
 		}
 		else if (ComplexModelElement.class.isAssignableFrom(es.getClass())) {
 			ComplexModelElement ces = (ComplexModelElement)es;
 			for (int i = 0;i < ces.getContents().size();i++) {
 				ElementMember em = ces.getContents().get(i);
 				if (!em.isOptional()) {
 					ModelElement emca = classToElement.get(em.getElementClass());
 					if (emca.getPrefix()!=null) {
 						for (Iterator<PatternRecognizer> ite = emca.getPrefix().iterator();ite.hasNext();) {
 							if (ite.next().read("",0) == null) {
 								return;
 							}
 						}
 					}
 					if (emca.getSuffix()!=null) {
 						for (Iterator<PatternRecognizer> ite = emca.getSuffix().iterator();ite.hasNext();) {
 							if (ite.next().read("",0) == null) {
 								return;
 							}
 						}
 					}
 					if (em.getPrefix()!=null) {
 						for (Iterator<PatternRecognizer> ite = em.getPrefix().iterator();ite.hasNext();) {
 							if (ite.next().read("",0) == null) {
 								return;
 							}
 						}
 					}
 					if (em.getSuffix()!=null) {
 						for (Iterator<PatternRecognizer> ite = em.getSuffix().iterator();ite.hasNext();) {
 							if (ite.next().read("",0) == null) {
 								return;
 							}
 						}
 					}
 					if (MultipleElementMember.class.isAssignableFrom(em.getClass())) {
 						MultipleElementMember mem = (MultipleElementMember)em;
 						if (mem.getMinimumMultiplicity()>0) {
 							if (em.getSeparator()!=null) {
 								for (Iterator<PatternRecognizer> ite = em.getSeparator().iterator();ite.hasNext();) {
 									if (ite.next().read("",0) == null) {
 										return;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 			boolean hasOtherThanOrig = false;
 			boolean hasOrig = false;
 
 			for (int i = 0;i < ces.getContents().size();i++) {
 				ElementMember em = ces.getContents().get(i);
 				ModelElement emm = classToElement.get(em.getElementClass());
 				if (!em.isOptional()) {
 					if (emm.equals(orig)) {
 						hasOrig = true;
 						if ((emm.getPrefix()!=null)) {
 							for (Iterator<PatternRecognizer> ite2 = emm.getPrefix().iterator();ite2.hasNext();) {
 								if (ite2.next().read("",0) == null) {
 									hasOtherThanOrig = true;
 								}
 							}
 						}
 						if ((emm.getSuffix()!=null)) {
 							for (Iterator<PatternRecognizer> ite2 = emm.getSuffix().iterator();ite2.hasNext();) {
 								if (ite2.next().read("",0) == null) {
 									hasOtherThanOrig = true;
 								}
 							}
 						}
 					}
 					else {
 						hasOtherThanOrig = true;
 					}
 				}
 			}
 			
 			if (hasOrig && !hasOtherThanOrig) {
                 log(Level.SEVERE, "Class \"{0}\" cycles with recursive inheritance or composition.", new Object[]{orig.getElementClass().getCanonicalName()});
 			}
 
 			if (hasOrig && !hasOtherThanOrig) {
 				for (int i = 0;i < ces.getContents().size();i++) {
 					ElementMember em = ces.getContents().get(i);
 					ModelElement emm = classToElement.get(em.getElementClass());
 					if (!em.isOptional()) {
 						if (!emm.equals(orig)) {
 							checkCycle(emm,orig,classToElement,subclasses);
 						}
 					}
 				}
 			}
 
 		}
 	}
 }
