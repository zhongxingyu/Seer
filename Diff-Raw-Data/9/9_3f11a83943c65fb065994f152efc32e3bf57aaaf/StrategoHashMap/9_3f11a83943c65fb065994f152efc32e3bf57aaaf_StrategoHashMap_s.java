 package org.spoofax.interpreter.library.ssl;
 
 import java.io.IOException;
 import java.util.LinkedHashMap;
 
 import org.spoofax.NotImplementedException;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermPrinter;
 import org.spoofax.terms.AbstractTermFactory;
 import org.spoofax.terms.attachments.ITermAttachment;
 import org.spoofax.terms.attachments.TermAttachmentType;
 
 public class StrategoHashMap extends LinkedHashMap<IStrategoTerm, IStrategoTerm> implements IStrategoTerm {
 
     private static final long serialVersionUID = -8193582031891397734L;
 
     public StrategoHashMap(int initialSize, int maxLoad) {
         super(initialSize, 1.0f * maxLoad / 100);
     }
 
     public IStrategoTerm[] getAllSubterms() {
         return AbstractTermFactory.EMPTY;
     }
 
     public IStrategoList getAnnotations() {
         return null;
     }
 
     public int getStorageType() {
         return MUTABLE;
     }
 
     public IStrategoTerm getSubterm(int index) {
         throw new UnsupportedOperationException();
     }
 
     public int getSubtermCount() {
         return 0;
     }
 
     public int getTermType() {
         return BLOB;
     }
 
     public boolean match(IStrategoTerm second) {
         return second == this;
     }
     
     public int hashCode() {
         return System.identityHashCode(this);
     }
 
     public void prettyPrint(ITermPrinter pp) {
         pp.print(toString());
     }
     
     @Override
     public String toString() {
         return String.valueOf(hashCode());
     }
     
     public String toString(int maxDepth) {
         return toString();
     }
     
     public void writeAsString(Appendable output, int maxDepth)
             throws IOException {
         output.append(toString());
     }
     
     public<T extends ITermAttachment> T getAttachment(TermAttachmentType<T> attachmentType) {
        throw new UnsupportedOperationException();
     }
 
     public void putAttachment(ITermAttachment attachment) {
         throw new NotImplementedException();
     }
     
     public void removeAttachment(TermAttachmentType<?> attachmentType) {
         throw new NotImplementedException();
     }
     
     public boolean isList() {
         return false;
     }
 
 }
