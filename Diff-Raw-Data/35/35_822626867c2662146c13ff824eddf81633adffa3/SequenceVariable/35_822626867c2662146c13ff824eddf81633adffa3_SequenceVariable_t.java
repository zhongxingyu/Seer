 package com.sun.javafx.runtime.location;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 import com.sun.javafx.runtime.AssignToBoundException;
 import com.sun.javafx.runtime.ErrorHandler;
 import com.sun.javafx.runtime.sequence.Sequence;
 import com.sun.javafx.runtime.sequence.SequenceMutator;
 import com.sun.javafx.runtime.sequence.SequencePredicate;
 import com.sun.javafx.runtime.sequence.Sequences;
 
 /**
  * SequenceVariable
  *
  * @author Brian Goetz
  */
 public class SequenceVariable<T>
         extends AbstractVariable<Sequence<T>, SequenceLocation<T>, SequenceBindingExpression<T>>
         implements SequenceLocation<T> {
 
     private final Class<T> clazz;
     private final SequenceMutator.Listener<T> mutationListener;
     private List<SequenceChangeListener<T>> changeListeners;
     private Sequence<T> value;
    private SequenceLocation<T> boundLocation;
 
 
     public static <T> SequenceVariable<T> make(Class clazz) {
         return new SequenceVariable<T>(clazz);
     }
 
     public static <T> SequenceVariable<T> make(Sequence<T> value) {
         return new SequenceVariable<T>(value);
     }
 
     public static <T> SequenceVariable<T> make(Class clazz, Sequence<? extends T> value) {
         return new SequenceVariable<T>(clazz, value);
     }
 
     public static<T> SequenceVariable<T> make(Class clazz, boolean lazy, SequenceBindingExpression<T> binding, Location... dependencies) {
         return new SequenceVariable<T>(clazz, lazy, binding, dependencies);
     }
 
     public static<T> SequenceVariable<T> make(Class clazz, SequenceBindingExpression<T> binding, Location... dependencies) {
         return new SequenceVariable<T>(clazz, false, binding, dependencies);
     }
 
     public static<T> SequenceVariable<T> make(Class clazz, SequenceLocation<T> otherLocation) {
         SequenceVariable<T> me = make(clazz);
         me.bind(otherLocation);
         return me;
     }
 
     /** Create a bijectively bound variable */
     public static<T> SequenceVariable<T> makeBijective(Class clazz, SequenceVariable<T> other) {
         SequenceVariable<T> me = SequenceVariable.<T>make(clazz);
         me.bijectiveBind(other);
         return me;
     }
 
     protected SequenceVariable(Class clazz) {
         this.clazz = clazz;
         this.value = Sequences.emptySequence(clazz);
         this.mutationListener = new SequenceMutator.Listener<T>() {
             public void onReplaceSlice(int startPos, int endPos, Sequence<? extends T> newElements, Sequence<T> oldValue, Sequence<T> newValue) {
                 replaceSlice(startPos, endPos, newElements, newValue);
             }
         };
     }
 
     protected SequenceVariable(Sequence<T> value) {
         this(value.getElementType());
         replaceValue(value);
     }
 
     protected SequenceVariable(Class clazz, Sequence<? extends T> value) {
         this(clazz);
         if (value == null)
             value = Sequences.emptySequence(clazz);
         replaceValue(Sequences.upcast(clazz, value));
     }
 
     protected SequenceVariable(Class clazz, boolean lazy, SequenceBindingExpression<T> binding, Location... dependencies) {
         this(clazz);
         bind(lazy, binding);
         addDependencies(dependencies);
     }
 
     private void ensureValid() {
         if (isBound() && !isValid())
             update();
     }
 
     /** Update the held value, notifying change listeners */
     private void replaceValue(Sequence<T> newValue) {
        assert(boundLocation == null);
         if (newValue == null)
             newValue = Sequences.emptySequence(clazz);
         replaceSlice(0, Sequences.size(value)-1, newValue, newValue);
     }
 
     /** Update the held value, notifying change listeners */
     private void replaceSlice(int startPos, int endPos, Sequence<? extends T> newElements, Sequence<T> newValue) {
        assert(boundLocation == null);
         Sequence<T> oldValue = value;
         if (!Sequences.isEqual(oldValue, newValue) || !isInitialized() || !isEverValid()) {
             value = newValue;
             setValid();
             valueChanged();
             notifyListeners(startPos, endPos, newElements, oldValue, newValue);
         }
         else
             setValid();
     }
 
 
     private Sequence<T> getRawValue() {
         return value;
     }
 
     public Sequence<T> get() {
         return getAsSequence();
     }
 
     public T get(int position) {
         return getAsSequence().get(position);
     }
 
     public Sequence<T> getAsSequence() {
         ensureValid();
         return value;
     }
 
     public Sequence<T> getSlice(int startPos, int endPos) {
         return getAsSequence().getSlice(startPos, endPos);
     }
 
     public boolean isNull() {
         return Sequences.size(getAsSequence()) == 0;
     }
 
     protected SequenceBindingExpression<T> makeBindingExpression(final SequenceLocation<T> otherLocation) {
         return new SequenceBindingExpression<T>() {
             public Sequence<T> computeValue() {
                 return otherLocation.getAsSequence();
             }
         };
     }
 
     public void addChangeListener(final ObjectChangeListener<Sequence<T>> listener) {
         addChangeListener(new SequenceChangeListener<T>() {
             public void onChange(int startPos, int endPos, Sequence<? extends T> newElements, Sequence<T> oldValue, Sequence<T> newValue) {
                 listener.onChange(oldValue, newValue);
             }
         });
     }
 
     public void addChangeListener(SequenceChangeListener<T> listener) {
         if (changeListeners == null)
             changeListeners = new ArrayList<SequenceChangeListener<T>>();
         changeListeners.add(listener);
     }
 
     public void removeChangeListener(SequenceChangeListener<T> listener) {
         if (changeListeners != null)
             changeListeners.remove(listener);
     }
 
     private void notifyListeners(final int startPos, final int endPos,
                                  final Sequence<? extends T> newElements,
                                  final Sequence<T> oldValue, final Sequence<T> newValue) {
         if (changeListeners != null) {
             for (SequenceChangeListener<T> listener : changeListeners)
                 listener.onChange(startPos, endPos, newElements, oldValue, newValue);
         }
     }
 
    public void bind(SequenceLocation<T> otherLocation) {
        ensureBindable();
        boundLocation = otherLocation;
        otherLocation.addChangeListener(new ChangeListener() {
            public boolean onChange(Location location) {
                valueChanged();
                return true;
            }
        });
        otherLocation.addChangeListener(new SequenceChangeListener<T>() {
            public void onChange(int startPos, int endPos, Sequence<? extends T> newElements, Sequence<T> oldValue, Sequence<T> newValue) {
                value = newValue;
                notifyListeners(startPos, endPos, newElements, oldValue, newValue);
            }
        });
        Sequence<T> oldValue = value;
        value = otherLocation.get();
        notifyListeners(0, Sequences.size(oldValue)-1, value, oldValue, value);
    }

    public boolean isBound() {
        return super.isBound() || (boundLocation != null);
    }

     @Override
     public String toString() {
         return getAsSequence().toString();
     }
 
     public Iterator<T> iterator() {
         return getAsSequence().iterator();
     }
 
     @Override
     public void update() {
         try {
            if (isBound() && !isValid() && boundLocation == null) {
                 replaceValue(Sequences.upcast(clazz, binding.computeValue()));
            }
         }
         catch (RuntimeException e) {
             ErrorHandler.bindException(e);
             if (isInitialized())
                 replaceValue(Sequences.emptySequence(clazz));
         }
     }
 
     private void ensureNotBound() {
         if (isBound())
             throw new AssignToBoundException("Cannot mutate bound sequence");
     }
 
     public Sequence<T> set(Sequence<T> value) {
         return setAsSequence(value);
     }
 
     public void setDefault() {
         setAsSequence(Sequences.emptySequence(clazz));
     }
 
     public Sequence<T> setAsSequence(Sequence<? extends T> newValue) {
         Sequence<T> result;
         ensureNotBound();
         Sequence<T> oldValue = getRawValue();
         // Workaround for JFXC-885
         if (!isInitialized() && !isEverValid() && Sequences.size(newValue) == 0 && Sequences.size(value) == 0) {
             result = Sequences.emptySequence(clazz);
             replaceValue(result);
         }
         else if (!Sequences.isEqual(oldValue, newValue) || !isInitialized() || !isEverValid()) {
             result = SequenceMutator.replaceSlice(oldValue, mutationListener, 0, Sequences.size(oldValue) - 1, newValue);
         }
         else
             result = oldValue;
         return result;
     }
 
     public Sequence<T> setAsSequenceFromLiteral(final Sequence<? extends T> value) {
         deferredLiteral = new DeferredInitializer() {
             public void apply() {
                 setAsSequence(value);
             }
         };
         return Sequences.upcast(clazz, value);
     }
 
     @Override
     public T set(int position, T newValue) {
         ensureNotBound();
         SequenceMutator.set(getRawValue(), mutationListener, position, newValue);
         return newValue;
     }
 
     @Override
     public Sequence<? extends T> replaceSlice(int startPos, int endPos, Sequence<? extends T> newValues) {
         ensureNotBound();
         SequenceMutator.replaceSlice(getRawValue(), mutationListener, startPos, endPos, newValues);
         return newValues;
     }
 
     @Override
     public void delete(int position) {
         ensureNotBound();
         SequenceMutator.delete(getRawValue(), mutationListener, position);
     }
 
     @Override
     public void deleteSlice(int startPos, int endPos) {
         replaceSlice(startPos, endPos, null);
     }
 
     @Override
     public void delete(SequencePredicate<T> sequencePredicate) {
         ensureNotBound();
         SequenceMutator.delete(getRawValue(), mutationListener, sequencePredicate);
     }
 
     @Override
     public void deleteAll() {
         ensureNotBound();
         setAsSequence(Sequences.emptySequence(clazz));
     }
 
     @Override
     public void deleteValue(final T targetValue) {
         ensureNotBound();
         delete(new SequencePredicate<T>() {
             public boolean matches(Sequence<? extends T> sequence, int index, T value) {
                 if (value == null)
                     return targetValue == null;
                 return value.equals(targetValue);
             }
         });
     }
 
     @Override
     public void insert(T value) {
         ensureNotBound();
         SequenceMutator.insert(getRawValue(), mutationListener, value);
     }
 
     @Override
     public void insert(Sequence<? extends T> values) {
         ensureNotBound();
         SequenceMutator.insert(getRawValue(), mutationListener, values);
     }
 
     public void insertFirst(T value) {
         ensureNotBound();
         SequenceMutator.insertFirst(getRawValue(), mutationListener, value);
     }
 
     @Override
     public void insertFirst(Sequence<? extends T> values) {
         ensureNotBound();
         SequenceMutator.insertFirst(getRawValue(), mutationListener, values);
     }
 
     @Override
     public void insertBefore(T value, int position) {
         ensureNotBound();
         SequenceMutator.insertBefore(getRawValue(), mutationListener, value, position);
     }
 
     @Override
     public void insertBefore(T value, SequencePredicate<T> sequencePredicate) {
         ensureNotBound();
         SequenceMutator.insertBefore(getRawValue(), mutationListener, value, sequencePredicate);
     }
 
     @Override
     public void insertBefore(Sequence<? extends T> values, int position) {
         ensureNotBound();
         SequenceMutator.insertBefore(getRawValue(), mutationListener, values, position);
     }
 
     @Override
     public void insertBefore(Sequence<? extends T> values, SequencePredicate<T> sequencePredicate) {
         ensureNotBound();
         SequenceMutator.insertBefore(getRawValue(), mutationListener, values, sequencePredicate);
     }
 
     @Override
     public void insertAfter(T value, int position) {
         ensureNotBound();
         SequenceMutator.insertAfter(this.getRawValue(), mutationListener, value, position);
     }
 
     @Override
     public void insertAfter(T value, SequencePredicate<T> sequencePredicate) {
         ensureNotBound();
         SequenceMutator.insertAfter(getRawValue(), mutationListener, value, sequencePredicate);
     }
 
     @Override
     public void insertAfter(Sequence<? extends T> values, int position) {
         ensureNotBound();
         SequenceMutator.insertAfter(getRawValue(), mutationListener, values, position);
     }
 
     @Override
     public void insertAfter(Sequence<? extends T> values, SequencePredicate<T> sequencePredicate) {
         ensureNotBound();
         SequenceMutator.insertAfter(getRawValue(), mutationListener, values, sequencePredicate);
     }
 }
