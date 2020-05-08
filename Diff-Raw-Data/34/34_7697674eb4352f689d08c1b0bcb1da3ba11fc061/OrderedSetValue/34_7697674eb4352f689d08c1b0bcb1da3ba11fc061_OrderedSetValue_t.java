 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.modelinglab.ocl.core.values;
 
import java.util.*;
 import org.modelinglab.ocl.core.ast.types.Classifier;
 import org.modelinglab.ocl.core.ast.types.OrderedSetType;
import org.modelinglab.ocl.core.ast.types.SetType;
 import org.modelinglab.ocl.core.ast.utils.OrderedSet;
 import org.modelinglab.ocl.core.values.utils.ValueVisitor;
 
 /**
  *
  * @param <E> 
  * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
  */
 public final class OrderedSetValue<E extends OclValue<?>> extends OclValue<List<E>> {
     private static final long serialVersionUID = 1L;
 
     private final OrderedSetType type;
     public OrderedSetValue(Collection<E> value, Classifier elementType) {
         super(new OrderedSet<>(value));
         this.type = new OrderedSetType(elementType);
     }
     
     public OrderedSetValue(List<E> value, Classifier elementType, boolean isInmutable) {
         super(value);
         this.type = new OrderedSetType(elementType);
     }
     
     public OrderedSetValue(SetValue<E> value) {
         super(value.getValue());
         this.type = new OrderedSetType(value.getType().getElementType());
     }
 
    private static <S> List<S> getValueList(List<S> value, boolean isInmutable) {
        if (isInmutable) {
            assert (new HashSet<>(value)).size() == value.size();
            return value;
        }
        return new OrderedSet<>(value);
    }
    
     @Override
     public <Result, Arg> Result accept(ValueVisitor<Result, Arg> visitor, Arg arg) {
         return visitor.visit(this, arg);
     }
 
     @Override
     public OrderedSetType getType() {
         return type;
     }
 
     @Override
     public List<E> getValue() {
         return Collections.unmodifiableList(super.getValue());
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(5 * getValue().size());
         for (final E e : getValue()) {
             sb.append(e).append(", ");
         }
         if (!getValue().isEmpty()) {
             sb.delete(sb.length() - 2, sb.length());
         }
         return "OrderedSet{" + sb.toString() + '}';
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final OrderedSetValue<E> other = (OrderedSetValue<E>) obj;
         if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
             return false;
         }
         return super.equals(obj);
     }
 
     @Override
     public int hashCode() {
         int hash = super.hashCode();
         hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
         return hash;
     }
 }
