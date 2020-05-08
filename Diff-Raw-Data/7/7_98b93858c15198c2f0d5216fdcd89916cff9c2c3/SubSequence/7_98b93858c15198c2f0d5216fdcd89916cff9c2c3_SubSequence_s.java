 package de.tum.in.lrr.hmm;
 
 
 
 /**
  * @author <a href="bauerb@in.tum.de">Bernhard Bauer</a>
  *
  */
 public class SubSequence extends AbstractSequence {
 
     protected final ISequence containingSequence;
     protected final int start;
     protected final int end;
     protected final boolean complement;
 
     public SubSequence(ISequence containingSequence, int start, int end, boolean complement) {
         if (complement && containingSequence.getAlphabet() != Alphabet.DNA) {
             throw new IllegalArgumentException("Only DNA sequences support complements");
         }
 
         final int length = containingSequence.length();
         if (start < 0) {
             throw new IndexOutOfBoundsException("start: "+start);
         }
         if (end > length) {
             throw new IndexOutOfBoundsException("end: "+end+" length: "+length);
         }
         if (start > end) {
             throw new IllegalArgumentException("start: "+start+" end: "+end);
         }
         //        if (containingSequence instanceof SubSequence) {
         //            SubSequence subSequence = (SubSequence)containingSequence;
         //            this.containingSequence = subSequence.containingSequence;
         //            this.start = start + subSequence.start;
         //            this.end = end + subSequence.start;
         //        } else {
         this.containingSequence = containingSequence;
         this.start = start;
         this.end = end;
         this.complement = complement;
         //        }
     }
 
     /**
      * @return the containingSequence
      */
     public ISequence getContainingSequence() {
         return containingSequence;
     }
 
 
     /**
      * @return the start
      */
     public int getStartIndex() {
         return start;
     }
 
     /**
      * @return the end
      */
     public int getEndIndex() {
         return end;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getIdentifier() {
         return containingSequence.getIdentifier();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return getIdentifier()+"["+getRange()+"]";
     }
 
     public String getRange() {
         return complement ? "complement("+(start+1)+".."+end+")" : (start+1)+".."+end;
     }
 
     /**
      * {@inheritDoc}
      */
     public int length() {
         return end-start;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Byte get(int index) {
         final int size = size();
         if (index<0 || index>=size) {
             throw new IndexOutOfBoundsException("Index: "+index+",Size: "+size);
         }
        return complement ? (byte)(5 - containingSequence.get(end - index - 1)) : containingSequence.get(index + start);
 
     }
 
     /**
      * {@inheritDoc}
      */
     public Alphabet getAlphabet() {
         return containingSequence.getAlphabet();
     }
 
     /**
      * @return
      */
     public boolean isComplement() {
         return complement;
     }
 
 }
