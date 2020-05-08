 package org.ita.testrefactoring.astparser;
 
 import java.util.HashMap;
 
 import org.ita.testrefactoring.metacode.Type;
 
 public class TypeCache extends HashMap<String, Type> {
 	
 	private ASTEnvironment environment;
 
 	public TypeCache(ASTEnvironment environment) {
 		this.environment = environment;
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7973930399495455846L;
 
 	@Override
 	public Type get(Object key) {
 		if (key == null) {
 			key = "java.lang.Object";
 		}
 		
 		Type cachedType = super.get(key);
 		
 		if (cachedType == null) {
 			String fullQualifiedName = key.toString();
 			
 			String packageName = ASTEnvironment.extractPackageName(fullQualifiedName);
 			String typeName = ASTEnvironment.extractTypeName(fullQualifiedName);
 
 			ASTPackage pack = environment.getPackageList().get(packageName);
 			
 			if (pack == null) {
 				pack = environment.createPackage(packageName);
 			}
 			
			environment.createDummyType(typeName, pack);
 			
			cachedType = super.get(key);
 		}
 		
 		return cachedType;
 	}
 }
