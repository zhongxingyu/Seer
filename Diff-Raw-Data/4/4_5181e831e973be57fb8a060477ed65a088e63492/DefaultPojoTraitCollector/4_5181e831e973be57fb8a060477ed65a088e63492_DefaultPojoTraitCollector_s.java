 package com.github.t1.webresource.meta;
 
 import java.lang.reflect.AnnotatedElement;
 
import lombok.extern.slf4j.Slf4j;

 import com.google.common.base.Predicate;
 
@Slf4j
 public class DefaultPojoTraitCollector extends AbstractPojoTraitCollector {
     public DefaultPojoTraitCollector(Class<?> type, PojoTraits target, AnnotatedElement annotations) {
         super(type, target, annotations);
     }
 
     @Override
     protected Predicate<PojoTrait> fieldVisiblePredicate() {
         return new Predicate<PojoTrait>() {
             @Override
             public boolean apply(PojoTrait input) {
                 return isPublic(input.member());
             }
         };
     }
 
     @Override
     protected Predicate<PojoTrait> accessorVisiblePredicate() {
         return new Predicate<PojoTrait>() {
             @Override
             public boolean apply(PojoTrait input) {
                 return isPublic(input.member());
             }
         };
     }
 }
