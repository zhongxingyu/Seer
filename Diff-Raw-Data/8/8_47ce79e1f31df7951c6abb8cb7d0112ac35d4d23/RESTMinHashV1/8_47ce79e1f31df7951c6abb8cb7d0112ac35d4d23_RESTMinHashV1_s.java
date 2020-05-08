 package org.jboss.pressgang.ccms.rest.v1.entities;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTMinHashCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTProjectCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTMinHashCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBasePrimaryEntityV1;
 
 
public class RESTMinHashV1 extends RESTBasePrimaryEntityV1<RESTMinHashV1, RESTMinHashCollectionV1, RESTMinHashCollectionItemV1> {
 
     private Integer minHash;
     private Integer minHashFuncId;
     /**
      * A list of the Envers revision numbers
      */
     private RESTMinHashCollectionV1 revisions = null;
 
     @Override
     public RESTMinHashCollectionV1 getRevisions() {
         return revisions;
     }
 
     @Override
     public void setRevisions(final RESTMinHashCollectionV1 revisions) {
         this.revisions = revisions;
     }
 
     @Override
     public RESTMinHashV1 clone(boolean deepCopy) {
         final RESTMinHashV1 retValue = new RESTMinHashV1();
 
         cloneInto(retValue, deepCopy);
 
         return retValue;
     }
 
     public void cloneInto(final RESTMinHashV1 clone, final boolean deepCopy) {
         super.cloneInto(clone, deepCopy);
 
         clone.minHash = minHash;
         clone.minHashFuncId = minHashFuncId;
 
         if (deepCopy) {
 
             if (revisions != null) {
                 clone.revisions = new RESTMinHashCollectionV1();
                 revisions.cloneInto(clone.revisions, deepCopy);
             }  else {
                 clone.revisions = null;
             }
         } else {
             clone.revisions = revisions;
         }
     }
 
     public Integer getMinHash() {
         return minHash;
     }
 
     public void setMinHash(final Integer minHash) {
         this.minHash = minHash;
     }
 
     public Integer getMinHashFuncId() {
         return minHashFuncId;
     }
 
     public void setMinHashFuncId(final Integer minHashFuncId) {
         this.minHashFuncId = minHashFuncId;
     }
 
     @Override
     public boolean equals(final Object other) {
         if (other == null) return false;
         if (this == other) return true;
         if (!(other instanceof RESTMinHashV1)) return false;
 
         return super.equals(other);
     }
 }
