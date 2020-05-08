 /*
 Copyright (c) 2013 Robby, Kansas State University.        
 All rights reserved. This program and the accompanying materials      
 are made available under the terms of the Eclipse Public License v1.0 
 which accompanies this distribution, and is available at              
 http://www.eclipse.org/legal/epl-v10.html                             
 */
 
 package edu.ksu.cis.santos.mdcf.dml.ast;
 
 import java.util.List;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.AccessExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.ApplyExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.BinaryBasicOpExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.DefaultMatchCaseBind;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.Exp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.FunExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.InstanceOfExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.LiteralExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.MapOpExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.MatchCase;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.MatchCaseBind;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.MatchExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.NamedMatchCaseBind;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.Param;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.SeqOpExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.SetOpExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.TupleOpExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.UnknownExp;
 import edu.ksu.cis.santos.mdcf.dml.ast.exp.VarRefExp;
 
 /**
  * Provides AST construction methods.
  * 
  * @author <a href="mailto:robby@k-state.edu">Robby</a>
  */
 public class Ast {
 
   /**
    * Provides AST construction methods with weaker type parameter constraints.
    * 
    * @author <a href="mailto:robby@k-state.edu">Robby</a>
    */
   public static class Weak {
 
     public static AccessExp accessExp(final Exp exp, final String id) {
       return Ast.accessExp(exp, id);
     }
 
     private static <T, O> boolean allInstanceOf(final Iterable<O> os,
         final Class<T> clazz) {
       for (final O o : os) {
         if (!clazz.isAssignableFrom(o.getClass())) {
           return false;
         }
       }
       return true;
     }
 
     public static ApplyExp applyExp(final Exp fun, final Exp arg) {
       return Ast.applyExp(fun, arg);
     }
 
     public static Attribute attribute(final Iterable<?> annotations,
         final Type type, final String name, final Optional<?> init) {
       return Ast.attribute(
           list(annotations, AttributeAnnotation.class),
           type,
           name,
           opt(init, Initialization.class));
     }
 
     public static BasicInit basicInit(final String value) {
       return Ast.basicInit(value);
     }
 
     public static BasicType basicType(final String name,
         final Iterable<?> supers) {
       return Ast.basicType(name, list(supers, NamedType.class));
     }
 
     public static BinaryBasicOpExp binaryBasicOpExp(final Exp left,
         final String op, final Exp right) {
       return Ast.binaryBasicOpExp(left, op, right);
     }
 
     public static ConstAnnotation constAnnotation(final FeatureLevel level,
         final String qualifier) {
       return Ast.constAnnotation(level, qualifier);
     }
 
     public static DataAnnotation dataAnnotation() {
       return Ast.dataAnnotation();
     }
 
     public static DefaultMatchCaseBind defaultMatchCaseBind() {
       return Ast.defaultMatchCaseBind();
     }
 
     public static EitherInit eitherInit(final int index,
         final Initialization init) {
       return Ast.eitherInit(index, init);
     }
 
     public static EitherType eitherType(final Iterable<?> choiceTypes) {
       return Ast.eitherType(list(choiceTypes, Type.class));
     }
 
     public static Feature feature(final Iterable<?> annotations,
         final String name, final Iterable<?> supers, final Iterable<?> members) {
       return Ast.feature(
           list(annotations, FeatureAnnotation.class),
           name,
           list(supers, NamedType.class),
           list(members, Member.class));
     }
 
     public static FeatureInit featureInit(final Iterable<?> types,
         final Iterable<?> attributes) {
       return Ast.featureInit(
           list(types, NamedType.class),
           list(attributes, Attribute.class));
     }
 
     public static FeatureLevelAnnotation featureLevelAnnotation(
         final FeatureLevel level, final String qualifier) {
       return Ast.featureLevelAnnotation(level, qualifier);
     }
 
     public static FunExp funExp(final Param param, final Exp body) {
       return Ast.funExp(param, body);
     }
 
     public static InstanceOfExp instanceOfExp(final Exp exp, final Type testType) {
       return Ast.instanceOfExp(exp, testType);
     }
 
     public static Invariant invariant(final String name,
         final PredicateType predicateType, final FunExp predicate) {
       return Ast.invariant(name, predicateType, predicate);
     }
 
     @SuppressWarnings("unchecked")
     private static <T, O> List<T> list(final Iterable<O> os,
         final Class<T> clazz) {
       if (!allInstanceOf(os, clazz)) {
         throw new IllegalArgumentException(
             "Expecting all elements to be of type " + clazz.getName());
       }
       if (os instanceof ImmutableList) {
         return (ImmutableList<T>) os;
       } else {
         return ImmutableList.<T> builder().addAll((Iterable<T>) os).build();
       }
     }
 
     public static <T> List<T> list(final Iterable<T> ts) {
       return Ast.list(ts);
     }
 
     @SafeVarargs
     public static <T> List<T> list(final T... ts) {
       return Ast.list(ts);
     }
 
     public static LiteralExp literalExp(final String value) {
       return Ast.literalExp(value);
     }
 
     public static MapInit mapInit(final Iterable<?> keyInits,
         final Iterable<?> valueInits) {
       return Ast.mapInit(
           list(keyInits, Initialization.class),
           list(valueInits, Initialization.class));
     }
 
     public static MapOpExp mapOpExp(final String id, final Exp exp) {
       return Ast.mapOpExp(id, exp);
     }
 
     public static MapType mapType(final Type keyType, final Type valueType) {
       return Ast.mapType(keyType, valueType);
     }
 
     public static MatchCase matchCase(final MatchCaseBind bind,
         final Optional<Exp> cond, final Exp body) {
       return Ast.matchCase(bind, cond, body);
     }
 
     public static MatchExp matchExp(final Exp exp, final List<?> cases) {
       return Ast.matchExp(exp, list(cases, MatchCase.class));
     }
 
     public static Model model(final Iterable<?> declarations) {
       return Ast.model(list(declarations, Declaration.class));
     }
 
     public static MultiplicityAnnotation multiplicityAnnotation(final int lo,
         final int hi, final Optional<String> typeName) {
       return Ast.multiplicityAnnotation(lo, hi, typeName);
     }
 
     public static NamedMatchCaseBind namedMatchCaseBind(final String id,
         final Optional<?> type) {
       return Ast.namedMatchCaseBind(id, opt(type, Type.class));
     }
 
     public static NamedType namedType(final String name) {
       return Ast.namedType(name);
     }
 
     public static <T> Optional<T> none() {
       return Ast.none();
     }
 
     public static NoneInit noneInit() {
       return Ast.noneInit();
     }
 
     @SuppressWarnings("unchecked")
     private static <T, O> Optional<T> opt(final Optional<O> opt,
         final Class<T> clazz) {
       if (!optInstanceOf(opt, clazz)) {
         throw new IllegalArgumentException(
             "Expecting optional referent to be of type " + clazz.getName());
       }
       return (Optional<T>) opt;
     }
 
     private static <T, O> boolean optInstanceOf(final Optional<O> opt,
         final Class<T> clazz) {
       if (opt.isPresent()) {
         return clazz.isAssignableFrom(opt.get().getClass());
       } else {
         return true;
       }
     }
 
     public static OptionType optionType(final Type elementType) {
       return Ast.optionType(elementType);
     }
 
     public static OverrideAnnotation overrideAnnotation() {
       return Ast.overrideAnnotation();
     }
 
     public static Param param(final Optional<?> type, final String name) {
       return Ast.param(opt(type, Type.class), name);
     }
 
     public static RefinedType refinedType(final Iterable<?> types,
         final Iterable<?> attributes) {
       return Ast.refinedType(
           list(types, NamedType.class),
           list(attributes, Attribute.class));
     }
 
     public static Requirement requirement(final String name,
         final Iterable<?> members) {
       return Ast.requirement(name, list(members, Member.class));
     }
 
     public static SeqInit seqInit(final Iterable<?> inits) {
       return Ast.seqInit(list(inits, Initialization.class));
     }
 
     public static SeqOpExp seqOpExp(final String id, final Exp exp) {
       return Ast.seqOpExp(id, exp);
     }
 
     public static SeqType seqType(final Type elementType) {
       return Ast.seqType(elementType);
     }
 
     public static SetInit setInit(final Iterable<?> inits) {
       return Ast.setInit(list(inits, Initialization.class));
     }
 
     public static SetOpExp setOpExp(final String id, final Exp exp) {
       return Ast.setOpExp(id, exp);
     }
 
     public static SettableAnnotation settableAnnotation() {
       return Ast.settableAnnotation();
     }
 
     public static SetType setType(final Type elementType) {
       return Ast.setType(elementType);
     }
 
     public static <T> Optional<T> some(final T t) {
       return Ast.some(t);
     }
 
     public static SomeInit someInit(final Initialization init) {
       return Ast.someInit(init);
     }
 
     public static TupleInit tupleInit(final Iterable<?> inits) {
       return Ast.tupleInit(list(inits, Initialization.class));
     }
 
     public static TupleOpExp tupleOpExp(final String id, final Exp exp) {
       return Ast.tupleOpExp(id, exp);
     }
 
     public static TupleType tupleType(final Iterable<?> elementTypes) {
       return Ast.tupleType(list(elementTypes, Type.class));
     }
 
     public static UnknownExp unknownExp() {
       return Ast.unknownExp();
     }
 
     private Weak() {
     }
 
     public VarRefExp varRefExp(final String id) {
       return Ast.varRefExp(id);
     }
   }
 
   public static AccessExp accessExp(final Exp exp, final String id) {
     return new AccessExp(exp, id);
   }
 
   public static ApplyExp applyExp(final Exp fun, final Exp arg) {
     return new ApplyExp(fun, arg);
   }
 
   public static Attribute attribute(
       final Iterable<AttributeAnnotation> annotations, final Type type,
       final String name, final Optional<Initialization> init) {
     return new Attribute(annotations, type, name, init);
   }
 
   public static BasicInit basicInit(final String value) {
     return new BasicInit(value);
   }
 
   public static BasicType basicType(final String name,
       final Iterable<NamedType> supers) {
     return new BasicType(name, supers);
   }
 
   public static BinaryBasicOpExp binaryBasicOpExp(final Exp left,
       final String op, final Exp right) {
     return new BinaryBasicOpExp(left, op, right);
   }
 
   public static ConstAnnotation constAnnotation(final FeatureLevel level,
       final String qualifier) {
     return new ConstAnnotation(level, qualifier);
   }
 
   public static DataAnnotation dataAnnotation() {
     return new DataAnnotation();
   }
 
   public static DefaultMatchCaseBind defaultMatchCaseBind() {
     return new DefaultMatchCaseBind();
   }
 
   public static EitherInit eitherInit(final int index, final Initialization init) {
     return new EitherInit(index, init);
   }
 
   public static EitherType eitherType(final Iterable<Type> choiceTypes) {
     return new EitherType(choiceTypes);
   }
 
   public static Feature feature(final Iterable<FeatureAnnotation> annotations,
       final String name, final Iterable<NamedType> supers,
       final Iterable<Member> members) {
     return new Feature(annotations, name, supers, members);
   }
 
   public static FeatureInit featureInit(final Iterable<NamedType> types,
       final Iterable<Attribute> attributes) {
     return new FeatureInit(types, attributes);
   }
 
   public static FeatureLevelAnnotation featureLevelAnnotation(
       final FeatureLevel level, final String qualifier) {
     return new FeatureLevelAnnotation(level, qualifier);
   }
 
   public static FunExp funExp(final Param param, final Exp body) {
     return new FunExp(param, body);
   }
 
   public static InstanceOfExp instanceOfExp(final Exp exp, final Type testType) {
     return new InstanceOfExp(exp, testType);
   }
 
   public static Invariant invariant(final String name,
       final PredicateType predicateType, final FunExp predicate) {
     return new Invariant(name, predicateType, predicate);
   }
 
   public static <T> List<T> list(final Iterable<T> ts) {
     if (ts instanceof ImmutableList) {
       return (ImmutableList<T>) ts;
     } else {
       return ImmutableList.<T> builder().addAll(ts).build();
     }
   }
 
   @SafeVarargs
   public static <T> List<T> list(final T... ts) {
    return ImmutableList.<T> builder().add(ts).build();
   }
 
   public static LiteralExp literalExp(final String value) {
     return new LiteralExp(value);
   }
 
   public static MapInit mapInit(final Iterable<Initialization> keyInits,
       final Iterable<Initialization> valueInits) {
     return new MapInit(list(keyInits), list(valueInits));
   }
 
   public static MapOpExp mapOpExp(final String id, final Exp exp) {
     return new MapOpExp(id, exp);
   }
 
   public static MapType mapType(final Type keyType, final Type valueType) {
     return new MapType(keyType, valueType);
   }
 
   public static MatchCase matchCase(final MatchCaseBind bind,
       final Optional<Exp> cond, final Exp body) {
     return new MatchCase(bind, cond, body);
   }
 
   public static MatchExp matchExp(final Exp exp, final List<MatchCase> cases) {
     return new MatchExp(exp, cases);
   }
 
   public static Model model(final Iterable<Declaration> declarations) {
     return new Model(declarations);
   }
 
   public static MultiplicityAnnotation multiplicityAnnotation(final int lo,
       final int hi, final Optional<String> typeName) {
     return new MultiplicityAnnotation(lo, hi, typeName);
   }
 
   public static NamedMatchCaseBind namedMatchCaseBind(final String id,
       final Optional<Type> type) {
     return new NamedMatchCaseBind(id, type);
   }
 
   public static NamedType namedType(final String name) {
     return new NamedType(name);
   }
 
   public static <T> Optional<T> none() {
     return Optional.<T> absent();
   }
 
   public static NoneInit noneInit() {
     return new NoneInit();
   }
 
   public static OptionType optionType(final Type elementType) {
     return new OptionType(elementType);
   }
 
   public static OverrideAnnotation overrideAnnotation() {
     return new OverrideAnnotation();
   }
 
   public static Param param(final Optional<Type> type, final String name) {
     return new Param(type, name);
   }
 
   public static RefinedType refinedType(final Iterable<NamedType> types,
       final Iterable<Attribute> attributes) {
     return new RefinedType(types, attributes);
   }
 
   public static Requirement requirement(final String name,
       final Iterable<Member> members) {
     return new Requirement(name, members);
   }
 
   public static SeqInit seqInit(final Iterable<Initialization> inits) {
     return new SeqInit(inits);
   }
 
   public static SeqOpExp seqOpExp(final String id, final Exp exp) {
     return new SeqOpExp(id, exp);
   }
 
   public static SeqType seqType(final Type elementType) {
     return new SeqType(elementType);
   }
 
   public static SetInit setInit(final Iterable<Initialization> inits) {
     return new SetInit(inits);
   }
 
   public static SetOpExp setOpExp(final String id, final Exp exp) {
     return new SetOpExp(id, exp);
   }
 
   public static SettableAnnotation settableAnnotation() {
     return new SettableAnnotation();
   }
 
   public static SetType setType(final Type elementType) {
     return new SetType(elementType);
   }
 
   public static <T> Optional<T> some(final T t) {
     return Optional.of(t);
   }
 
   public static SomeInit someInit(final Initialization init) {
     return new SomeInit(init);
   }
 
   public static TupleInit tupleInit(final Iterable<Initialization> inits) {
     return new TupleInit(inits);
   }
 
   public static TupleOpExp tupleOpExp(final String id, final Exp exp) {
     return new TupleOpExp(id, exp);
   }
 
   public static TupleType tupleType(final Iterable<Type> elementTypes) {
     return new TupleType(elementTypes);
   }
 
   public static UnknownExp unknownExp() {
     return new UnknownExp();
   }
 
   public static VarRefExp varRefExp(final String id) {
     return new VarRefExp(id);
   }
 
   private Ast() {
   }
 }
