 /*
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an 
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language 
  * governing permissions and limitations under the License.
  */
 package fuzzy.mf;
 
 import org.apache.commons.functor.UnaryFunction;
 
 /**
  * Membership Function marker interface. All provided membership functions 
  * implement this interface.
  * <p>
  * Implementors are encouraged but not required to make their membership 
  * functions {@link java.io.Serializable Serializable}.
  * </p>
  * 
  * @author Bruno P. Kinoshita - http://www.kinoshita.eti.br
  * @since 0.1
  */
public interface MembershipFunction extends UnaryFunction<Double, Double> {
 
 	/**
      * Returns a human readable description of this membership function.
      * Implementators are strongly encouraged but not strictly required to 
      * override the default {@link Object} implementation of this method.
      *
      * @return a human readable description of this membership function
      */
 	String toString();
 	
 	/**
      * Indicates whether some other object is &quot;equal to&quot;
      * this membership function.  This method must adhere to
      * general {@link Object#equals Object.equals} contract.
      * Additionally, this method can return
      * <tt>true</tt> <i>only</i> if the specified Object implements
      * the same membership function interface and is known to produce the same
      * results and/or side-effects for the same arguments (if any).
      * <p>
      * While implementators are strongly encouraged to override
      * the default Object implementation of this method,
      * note that the default Object implementation
      * does in fact adhere to the functor <code>equals</code> contract.
      * </p>
     * @param that the object to compare this membership function to
      * @see #hashCode
      * @return <code>true</code> iff the given object implements
      *         this same membership function interface, and is known to produce 
      *         the same results and/or side-effects for the same arguments
      *         (if any).
      */
 	boolean equals(Object obj);
 	
 	/**
      * Returns a hash code for this functor adhering to the
      * general {@link Object#hashCode Object.hashCode} contract.
      * Implementators are strongly encouraged but not
      * strictly required to override the default {@link Object}
      * implementation of this method.
      *
      * @see #equals
      * @return a hash code for this membership function
      */
 	int hashCode();
 	
 }
