 package xtraitj.util;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Iterables;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.EcoreUtil2;
 import org.eclipse.xtext.common.types.JvmFormalParameter;
 import org.eclipse.xtext.common.types.JvmTypeReference;
 import org.eclipse.xtext.xbase.lib.CollectionLiterals;
 import org.eclipse.xtext.xbase.lib.Functions.Function1;
 import org.eclipse.xtext.xbase.lib.IterableExtensions;
 import org.eclipse.xtext.xbase.lib.ListExtensions;
 import org.eclipse.xtext.xbase.lib.ObjectExtensions;
 import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
 import xtraitj.xtraitj.TJClass;
 import xtraitj.xtraitj.TJConstructor;
 import xtraitj.xtraitj.TJDeclaration;
 import xtraitj.xtraitj.TJField;
 import xtraitj.xtraitj.TJMember;
 import xtraitj.xtraitj.TJMethod;
 import xtraitj.xtraitj.TJMethodDeclaration;
 import xtraitj.xtraitj.TJProgram;
 import xtraitj.xtraitj.TJRequiredMethod;
 import xtraitj.xtraitj.TJTrait;
 import xtraitj.xtraitj.TJTraitExpression;
 import xtraitj.xtraitj.TJTraitOperation;
 import xtraitj.xtraitj.TJTraitReference;
 
 @SuppressWarnings("all")
 public class TraitJModelUtil {
   public static Iterable<TJTrait> traits(final TJProgram p) {
     EList<TJDeclaration> _elements = p.getElements();
     Iterable<TJTrait> _filter = Iterables.<TJTrait>filter(_elements, TJTrait.class);
     return _filter;
   }
   
   public static Iterable<TJClass> classes(final TJProgram p) {
     EList<TJDeclaration> _elements = p.getElements();
     Iterable<TJClass> _filter = Iterables.<TJClass>filter(_elements, TJClass.class);
     return _filter;
   }
   
   public static Iterable<TJField> fields(final TJTrait t) {
     EList<TJMember> _members = t.getMembers();
     Iterable<TJField> _filter = Iterables.<TJField>filter(_members, TJField.class);
     return _filter;
   }
   
   public static Iterable<TJMethod> methods(final TJTrait t) {
     EList<TJMember> _members = t.getMembers();
     Iterable<TJMethod> _filter = Iterables.<TJMethod>filter(_members, TJMethod.class);
     return _filter;
   }
   
   public static Iterable<TJField> fields(final TJTraitReference e) {
     Iterable<TJField> _fields = null;
     TJTrait _trait = e.getTrait();
     if (_trait!=null) {
       _fields=TraitJModelUtil.fields(_trait);
     }
     return _fields;
   }
   
   public static Iterable<TJMethod> methods(final TJTraitReference e) {
     Iterable<TJMethod> _methods = null;
     TJTrait _trait = e.getTrait();
     if (_trait!=null) {
       _methods=TraitJModelUtil.methods(_trait);
     }
     return _methods;
   }
   
   public static EList<? extends TJMember> members(final TJDeclaration d) {
     EList<? extends TJMember> _switchResult = null;
     boolean _matched = false;
     if (!_matched) {
       if (d instanceof TJTrait) {
         final TJTrait _tJTrait = (TJTrait)d;
         _matched=true;
         EList<TJMember> _members = _tJTrait.getMembers();
         _switchResult = _members;
       }
     }
     if (!_matched) {
       if (d instanceof TJClass) {
         final TJClass _tJClass = (TJClass)d;
         _matched=true;
         EList<TJField> _fields = _tJClass.getFields();
         _switchResult = _fields;
       }
     }
     return _switchResult;
   }
   
   public static Iterable<TJRequiredMethod> requiredMethods(final TJTrait t) {
     EList<TJMember> _members = t.getMembers();
     Iterable<TJRequiredMethod> _filter = Iterables.<TJRequiredMethod>filter(_members, TJRequiredMethod.class);
     return _filter;
   }
   
   public static Iterable<TJRequiredMethod> requiredMethods(final TJTraitReference e) {
     Iterable<TJRequiredMethod> _requiredMethods = null;
     TJTrait _trait = e.getTrait();
     if (_trait!=null) {
       _requiredMethods=TraitJModelUtil.requiredMethods(_trait);
     }
     return _requiredMethods;
   }
   
   public static TJDeclaration containingDeclaration(final EObject e) {
     TJDeclaration _containerOfType = EcoreUtil2.<TJDeclaration>getContainerOfType(e, TJDeclaration.class);
     return _containerOfType;
   }
   
   public static TJTrait containingTrait(final TJTraitReference e) {
     TJTrait _containerOfType = EcoreUtil2.<TJTrait>getContainerOfType(e, TJTrait.class);
     return _containerOfType;
   }
   
   public static TJTrait containingTrait(final TJMember e) {
     TJTrait _containerOfType = EcoreUtil2.<TJTrait>getContainerOfType(e, TJTrait.class);
     return _containerOfType;
   }
   
   public static TJTraitReference containingTraitOperationExpression(final EObject e) {
     TJTraitReference _containerOfType = EcoreUtil2.<TJTraitReference>getContainerOfType(e, TJTraitReference.class);
     return _containerOfType;
   }
   
   public static TJClass containingClass(final TJConstructor e) {
     TJClass _containerOfType = EcoreUtil2.<TJClass>getContainerOfType(e, TJClass.class);
     return _containerOfType;
   }
   
   public static TJProgram containingProgram(final TJDeclaration e) {
     TJProgram _containerOfType = EcoreUtil2.<TJProgram>getContainerOfType(e, TJProgram.class);
     return _containerOfType;
   }
   
   public static String getOriginalName(final TJMember m) {
     String _name = m.getName();
     return _name;
   }
   
   public static String getCurrentName(final TJMember m) {
     String _name = m.getName();
     return _name;
   }
   
   public static List<TJTraitReference> traitReferences(final TJDeclaration t) {
     TJTraitExpression _traitExpression = t.getTraitExpression();
     List<TJTraitReference> _traitReferences = TraitJModelUtil.traitReferences(_traitExpression);
     return _traitReferences;
   }
   
   public static List<TJTraitReference> traitReferences(final TJTraitExpression e) {
     EList<TJTraitReference> _xblockexpression = null;
     {
       boolean _equals = Objects.equal(e, null);
       if (_equals) {
         return CollectionLiterals.<TJTraitReference>emptyList();
       }
       EList<TJTraitReference> _references = e.getReferences();
       _xblockexpression = (_references);
     }
     return _xblockexpression;
   }
   
   public static List<TJTraitReference> traitOperationExpressions(final TJDeclaration t) {
     TJTraitExpression _traitExpression = t.getTraitExpression();
     List<TJTraitReference> _traitReferences = TraitJModelUtil.traitReferences(_traitExpression);
     final Function1<TJTraitReference,Boolean> _function = new Function1<TJTraitReference,Boolean>() {
       public Boolean apply(final TJTraitReference it) {
         EList<TJTraitOperation> _operations = it.getOperations();
         boolean _isEmpty = _operations.isEmpty();
         boolean _not = (!_isEmpty);
         return Boolean.valueOf(_not);
       }
     };
     Iterable<TJTraitReference> _filter = IterableExtensions.<TJTraitReference>filter(_traitReferences, _function);
     List<TJTraitReference> _list = IterableExtensions.<TJTraitReference>toList(_filter);
     return _list;
   }
   
   /**
    * Recursively collects all TJTrait occurrences in the TJTraitExpression
    * of the passed trait,
    * avoiding possible cycles
    */
   public static Iterable<TJTrait> allTraitsDependency(final TJTrait t) {
    Iterable<TJTrait> _xblockexpression = null;
    {
      TJTraitExpression _traitExpression = t.getTraitExpression();
      boolean _equals = Objects.equal(_traitExpression, null);
      if (_equals) {
        return CollectionLiterals.<TJTrait>emptyList();
       }
      TJTraitExpression _traitExpression_1 = t.getTraitExpression();
      ArrayList<TJTraitReference> _allTraitReferences = TraitJModelUtil.allTraitReferences(_traitExpression_1);
      Iterable<TJTraitReference> _filter = Iterables.<TJTraitReference>filter(_allTraitReferences, TJTraitReference.class);
      final Function1<TJTraitReference,TJTrait> _function = new Function1<TJTraitReference,TJTrait>() {
        public TJTrait apply(final TJTraitReference it) {
          TJTrait _trait = it.getTrait();
          return _trait;
        }
      };
      Iterable<TJTrait> _map = IterableExtensions.<TJTraitReference, TJTrait>map(_filter, _function);
      _xblockexpression = (_map);
    }
    return _xblockexpression;
   }
   
   public static ArrayList<TJTraitReference> allTraitReferences(final TJTraitExpression e) {
     ArrayList<TJTraitReference> _newArrayList = CollectionLiterals.<TJTraitReference>newArrayList();
     final Procedure1<ArrayList<TJTraitReference>> _function = new Procedure1<ArrayList<TJTraitReference>>() {
       public void apply(final ArrayList<TJTraitReference> it) {
         HashSet<TJTrait> _newHashSet = CollectionLiterals.<TJTrait>newHashSet();
         TraitJModelUtil.allTraitReferences(e, it, _newHashSet);
       }
     };
     ArrayList<TJTraitReference> _doubleArrow = ObjectExtensions.<ArrayList<TJTraitReference>>operator_doubleArrow(_newArrayList, _function);
     return _doubleArrow;
   }
   
   private static void allTraitReferences(final TJTraitExpression e, final List<TJTraitReference> traitExpressions, final Set<TJTrait> visited) {
     List<TJTraitReference> _traitReferences = TraitJModelUtil.traitReferences(e);
     for (final TJTraitReference t : _traitReferences) {
       TJTrait _trait = t.getTrait();
       boolean _contains = visited.contains(_trait);
       boolean _not = (!_contains);
       if (_not) {
         TJTrait _trait_1 = t.getTrait();
         visited.add(_trait_1);
         traitExpressions.add(t);
         TJTrait _trait_2 = t.getTrait();
         TJTraitExpression _traitExpression = _trait_2.getTraitExpression();
         if (_traitExpression!=null) {
           TraitJModelUtil.allTraitReferences(_traitExpression, traitExpressions, visited);
         }
       }
     }
   }
   
   public static String representationWithTypes(final TJField f) {
     JvmTypeReference _type = f.getType();
     String _simpleName = null;
     if (_type!=null) {
       _simpleName=_type.getSimpleName();
     }
     String _plus = (_simpleName + " ");
     String _name = f.getName();
     String _plus_1 = (_plus + _name);
     return _plus_1;
   }
   
   public static String representationWithTypes(final TJMethodDeclaration f) {
     JvmTypeReference _type = f.getType();
     String _simpleName = null;
     if (_type!=null) {
       _simpleName=_type.getSimpleName();
     }
     String _plus = (_simpleName + " ");
     String _name = f.getName();
     String _plus_1 = (_plus + _name);
     String _parameterRepresentation = TraitJModelUtil.parameterRepresentation(f);
     String _plus_2 = (_plus_1 + _parameterRepresentation);
     return _plus_2;
   }
   
   public static String parameterRepresentation(final TJMethodDeclaration f) {
     EList<JvmFormalParameter> _params = f.getParams();
     final Function1<JvmFormalParameter,String> _function = new Function1<JvmFormalParameter,String>() {
       public String apply(final JvmFormalParameter it) {
         JvmTypeReference _parameterType = it.getParameterType();
         String _simpleName = null;
         if (_parameterType!=null) {
           _simpleName=_parameterType.getSimpleName();
         }
         return _simpleName;
       }
     };
     List<String> _map = ListExtensions.<JvmFormalParameter, String>map(_params, _function);
     String _join = IterableExtensions.join(_map, ", ");
     String _plus = ("(" + _join);
     String _plus_1 = (_plus + ")");
     return _plus_1;
   }
   
   public static String constructorRepresentation(final TJConstructor c) {
     String _name = c.getName();
     String _plus = (_name + 
       "(");
     EList<JvmFormalParameter> _params = c.getParams();
     final Function1<JvmFormalParameter,String> _function = new Function1<JvmFormalParameter,String>() {
       public String apply(final JvmFormalParameter it) {
         JvmTypeReference _parameterType = it.getParameterType();
         String _simpleName = null;
         if (_parameterType!=null) {
           _simpleName=_parameterType.getSimpleName();
         }
         return _simpleName;
       }
     };
     List<String> _map = ListExtensions.<JvmFormalParameter, String>map(_params, _function);
     String _join = IterableExtensions.join(_map, ", ");
     String _plus_1 = (_plus + _join);
     String _plus_2 = (_plus_1 + ")");
     return _plus_2;
   }
 }
