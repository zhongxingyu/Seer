 /*
  * Software License Agreement (BSD License)
  *
  * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
  *
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  *
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  *
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  *
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package es.csic.iiia.maxsum.factors;
 
 import es.csic.iiia.maxsum.Factor;
 import es.csic.iiia.maxsum.CommunicationAdapter;
 import es.csic.iiia.maxsum.MaxOperator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This factor composes (sums) an independent cost/utility factor with some
  * other <em>inner</em> (non-idependent) factor.
  * <p/>
  * The resulting complexity is the same as that of the inner factor.
  *
  * @param <T> Type of the factor's identity.
  * @author Marc Pujol <mpujol@iiia.csic.es>
  * @deprecated
  */
 public class CompositeIndependentFactor<T> extends AbstractFactor<T>
     implements CommunicationAdapter<T>
 {
 
     private Factor<T> innerFactor;
     private IndependentFactor<T> independentFactor;
 
     /**
      * Get the inner (non-independent) factor of this composition.
      *
      * @return inner factor of this composition.
      */
     public Factor<T> getInnerFactor() {
         return innerFactor;
     }
 
     /**
      * Set the inner (non-independent) factor of this composition.
      * @param innerFactor inner factor for this composition.
      */
     public void setInnerFactor(Factor<T> innerFactor) {
         this.innerFactor = innerFactor;
         innerFactor.setCommunicationAdapter(this);
         innerFactor.setMaxOperator(getMaxOperator());
         innerFactor.setIdentity(getIdentity());
     }
 
     /**
      * Get the independent factor of this composition.
      * @return independent factor of this composition.
      */
     public IndependentFactor<T> getIndependentFactor() {
         return independentFactor;
     }
 
     /**
      * Set the independent factor of this composition.
      * @param independentFactor independent factor for this composition.
      */
     public void setIndependentFactor(IndependentFactor<T> independentFactor) {
         this.independentFactor = independentFactor;
     }
 
     @Override
     public void receive(double message, T sender) {
         double value = message + independentFactor.getPotential(sender);
         innerFactor.receive(value, sender);
     }
 
     /**
      * This is the method called by the inner factor when trying to send
      * messages out.
      *
      * @param message
      * @param sender
      * @param recipient
      */
     @Override
     public void send(double message, T sender, T recipient) {
         double value = message + independentFactor.getPotential(recipient);
         getCommunicationAdapter().send(value, sender, recipient);
     }
 
     @Override
     public void setIdentity(T identity) {
         super.setIdentity(identity);
         if (innerFactor != null) {
             innerFactor.setIdentity(identity);
         }
     }
 
     @Override
     public void setMaxOperator(MaxOperator maxOperator) {
         super.setMaxOperator(maxOperator);
         if (innerFactor != null) {
             innerFactor.setMaxOperator(maxOperator);
         }
     }
 
     @Override
     public void addNeighbor(T factor) {
        innerFactor.addNeighbor(factor);
     }
 
     @Override
     public List<T> getNeighbors() {
        return innerFactor.getNeighbors();
     }
 
     @Override
     public void send(double message, T recipient) {
         throw new UnsupportedOperationException("This method should never be called.");
     }
 
     @Override
     protected double eval(Map<T, Boolean> values) {
         return independentFactor.evaluate(values) + innerFactor.evaluate(values);
     }
 
     @Override
     public long iter() {
         return getNeighbors().size() + innerFactor.run();
     }
 
 }
