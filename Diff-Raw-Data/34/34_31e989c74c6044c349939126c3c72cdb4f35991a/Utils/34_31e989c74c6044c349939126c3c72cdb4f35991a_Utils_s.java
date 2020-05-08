 package squared.utils;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.db4o.ObjectContainer;
 import com.db4o.ext.StoredClass;
 import com.db4o.reflect.ReflectClass;
 import com.db4o.reflect.Reflector;
 
 public class Utils {
 	
 	private static String[] ignore = new String[]{
 		"java.lang.",
 		"java.util.",
 		"java.math.",
 		"System.",
 		"j4o.lang.AssemblyNameHint",
 		"com.db4o.",
 		/*"com.db4o.ext",
 		"com.db4o.config",
 		"com.db4o.StaticClass",
 		"com.db4o.StaticField",*/
 //		"Db4objects.Db4o",
 	};
 	
 	public static boolean isIgnored(String className) {
 		for (int j = 0; j < ignore.length; j++) {
 			if (className.startsWith(ignore[j])) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Method taken from db4o Object Manager source (objectmanager-api/src/com/db4o/objectmanager/api/helpers/ReflectHelper2.java)
 	 */
 	public static List<ReflectClass> getStoredClasses(ObjectContainer container) {
 		// Get the known classes
 		StoredClass[] knownClasses = container.ext().storedClasses();
 		Reflector reflector = container.ext().reflector();
 
 		// Filter them
 		List<ReflectClass> filteredList = new ArrayList<ReflectClass>();
 		for (int i = 0; i < knownClasses.length; i++) {
 			StoredClass sc = knownClasses[i];
 			ReflectClass knownClass = reflector.forName(sc.getName());
 			/*
 			this is null for someone in the forums.
 			if(knownClass == null){
 				// STRANGE???
 				System.out.println("knownClass: " + knownClass + " for " + sc.getName());
 				continue;
 			}*/
 			if (knownClass.isArray() || knownClass.isPrimitive()) {
 				continue;
 			}
 			if (isIgnored(knownClass.getName())) {
 				continue;
 			}
 			filteredList.add(knownClass);
 		}
 
 		// Sort them
 		Collections.sort(filteredList, new Comparator() {
 			private Collator comparator = Collator.getInstance();
 
 			public int compare(Object arg0, Object arg1) {
 				ReflectClass class0 = (ReflectClass) arg0;
 				ReflectClass class1 = (ReflectClass) arg1;
 
 				return comparator.compare(class0.getName(), class1.getName());
 			}
 		});
 
 		return filteredList;
 	}
 
 	public static String capitalize(String s) {
 		if (s.length() > 0) {
 			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
 		}
 		return s;
 	}
 
 }
