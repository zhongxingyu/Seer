 package org.yi.happy.archive.crypto;
 
 /**
  * A pseudo value object representing a cipher algorithm name. If get() is not
  * called then this is a value object, and implementations should honor this
  * property.
  */
 public abstract class CipherProvider {
     /**
      * get the implementation of this cipher.
      * 
      * @return a cipher implementation.
      */
     public abstract Cipher get();
 
     protected final String algorithm;
 
     public CipherProvider(String algorithm) {
 	this.algorithm = algorithm;
     }
 
     public final String getAlgorithm() {
 	return algorithm;
     }
 
     @Override
     public final int hashCode() {
 	final int prime = 31;
 	int result = 1;
 	result = prime * result
 		+ ((algorithm == null) ? 0 : algorithm.hashCode());
 	return result;
     }
 
     @Override
     public final boolean equals(Object obj) {
 	if (this == obj)
 	    return true;
 	if (obj == null)
 	    return false;
	if (getClass() != obj.getClass())
 	    return false;
 	CipherProvider other = (CipherProvider) obj;
 	if (algorithm == null) {
 	    if (other.algorithm != null)
 		return false;
 	} else if (!algorithm.equals(other.algorithm))
 	    return false;
 	return true;
     }
 
     @Override
     public final String toString() {
 	return algorithm;
     }
 }
