 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.modelinglab.ocl.core.values;
 
 import java.util.*;
 import org.modelinglab.ocl.core.ast.types.Classifier;
 import org.modelinglab.ocl.core.ast.types.SetType;
 import org.modelinglab.ocl.core.values.utils.ValueVisitor;
 
 /**
  *
  * @param <E> 
  * @author Gonzalo Ortiz Jaureguizar (gortiz at software.imdea.org)
  */
 public final class SetValue<E extends OclValue<?>> extends OclValue<List<E>> {
     private static final long serialVersionUID = 1L;
 
     private final SetType type;
     public SetValue(Collection<E> value, Classifier elementType) {
        super(new ArrayList<>(new HashSet<>(value)));
         this.type = new SetType(elementType);
     }
     
     public SetValue(List<E> value, Classifier elementType, boolean isInmutable) {
        super(getValueList(value, isInmutable));
         this.type = new SetType(elementType);
     }
     
     public SetValue(OrderedSetValue<E> ordered) {
         super(ordered.getValue());
         this.type = new SetType(ordered.getType().getElementType());
     }
     
    private static <S> List<S> getValueList(List<S> value, boolean isInmutable) {
        if (isInmutable) {
            assert (new HashSet<>(value)).size() == value.size();
            return value;
        }
        return new ArrayList<>(new HashSet(value));
    }
    
     @Override
     public <Result, Arg> Result accept(ValueVisitor<Result, Arg> visitor, Arg arg) {
         return visitor.visit(this, arg);
     }
 
     @Override
     public SetType getType() {
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
         return "Set{" + sb.toString() + '}';
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final SetValue<?> other = (SetValue<?>) obj;
         if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
             return false;
         }
         
         
         if (this.getValue().size() != other.getValue().size()) {
             return false;
         }
         for (final E e : this.getValue()) {
             if (!other.getValue().contains(e)) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = super.hashCode();
         hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
         return hash;
     }
 }
