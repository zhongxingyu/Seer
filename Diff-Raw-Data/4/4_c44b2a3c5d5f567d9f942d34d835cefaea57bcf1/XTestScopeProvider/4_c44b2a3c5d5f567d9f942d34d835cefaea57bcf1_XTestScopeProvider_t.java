 package org.xtest.scoping;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.xtext.common.types.JvmEnumerationLiteral;
 import org.eclipse.xtext.common.types.JvmEnumerationType;
 import org.eclipse.xtext.common.types.JvmTypeReference;
 import org.eclipse.xtext.common.types.util.TypeReferences;
 import org.eclipse.xtext.resource.EObjectDescription;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 import org.eclipse.xtext.scoping.impl.MapBasedScope;
 import org.eclipse.xtext.xbase.scoping.XbaseScopeProvider;
 import org.xtest.preferences.RuntimePref;
 import org.xtest.xTest.XTestPackage;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 
 /**
  * Custom scope provider for Xtest
  * 
  * @author Michael Barry
  */
 @SuppressWarnings("restriction")
 public class XTestScopeProvider extends XbaseScopeProvider {
     @Inject
     private TypeReferences typeRefs;
 
     @Override
     public IScope getScope(EObject context, EReference reference) {
         IScope scope;
        if (reference == XTestPackage.Literals.FILE_PARAM__FEATURE) {
             List<IEObjectDescription> descriptions = Lists.newArrayList();
             Map<String, RuntimePref> map = Maps.newTreeMap();
             for (RuntimePref pref : RuntimePref.values()) {
                 map.put(pref.toString(), pref);
             }
             JvmTypeReference typeForName = typeRefs.getTypeForName(RuntimePref.class, context);
             if (typeForName != null && typeForName.getType() instanceof JvmEnumerationType) {
                 EList<JvmEnumerationLiteral> literals = ((JvmEnumerationType) typeForName.getType())
                         .getLiterals();
                 for (JvmEnumerationLiteral literal : literals) {
                     String simpleName = literal.getSimpleName();
                     RuntimePref runtimePref = map.get(simpleName);
                     if (runtimePref != null) {
                         String id = runtimePref.getId();
                         IEObjectDescription create = EObjectDescription.create(id, literal);
                         descriptions.add(create);
                     }
                 }
             }
 
             scope = MapBasedScope.createScope(MapBasedScope.NULLSCOPE, descriptions);
         } else {
             scope = super.getScope(context, reference);
         }
         return scope;
     }
 }
