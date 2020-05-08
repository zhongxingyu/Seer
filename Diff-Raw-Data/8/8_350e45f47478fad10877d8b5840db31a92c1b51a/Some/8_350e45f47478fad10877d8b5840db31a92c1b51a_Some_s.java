 /******************************************************************************
  * Copyright (c) David Orme and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    David Orme - initial API and implementation
  ******************************************************************************/
 package com.coconut_palm_software.possible;
 
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 
 /**
  * An Option instance that contains a value.
  * 
  * @param <T> The type that this Option is encapsulating.
  */
 final class Some<T> extends Possible<T> {
     private final T value;
     private IStatus status = null;
  
     Some(T value) {
         this.value = value;
     }
  
     Some(T value, IStatus status) {
         this.value = value;
         this.status = status;
     }
  
     /* (non-Javadoc)
      * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#get()
      */
     public T get() {
         return value;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#getOrSubstitute(java.lang.Object)
 	 */
 	public T getOrSubstitute(T defaultValue) {
 		return value;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#getOrThrow(java.lang.Throwable)
 	 */
 	public <E extends Throwable> T getOrThrow(E exception) {
 		return value;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#hasValue()
 	 */
 	public boolean hasValue() {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#getStatus()
 	 */
 	public IStatus getStatus() {
 		return Nulls.valueOrSubstitute(status, Status.OK_STATUS);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#contains(java.lang.Object)
 	 */
 	@Override
 	boolean contains(Object o) {
 		return value.equals(o);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#isEmpty()
 	 */
 	@Override
 	boolean isEmpty() {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#size()
 	 */
 	@Override
 	int size() {
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#toArray()
 	 */
 	@Override
 	Object[] toArray() {
 		return new Object[] {value};
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#toArray(A[])
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	<A> A[] toArray(A[] a) {
 		a[0] = (A) value;
 		return a;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Iterable#iterator()
 	 */
 	@Override
 	public Iterator<T> iterator() {
 		return new Iterator<T>() {
 			boolean returnedValue = false;
 
 			@Override
 			public boolean hasNext() {
 				return !returnedValue;
 			}
 
 			@Override
 			public T next() {
 				if (hasNext()) {
 					returnedValue = true;
 					return value;
 				}
 				throw new IndexOutOfBoundsException("Past end of collection.");
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}};
 	}
 }
  
