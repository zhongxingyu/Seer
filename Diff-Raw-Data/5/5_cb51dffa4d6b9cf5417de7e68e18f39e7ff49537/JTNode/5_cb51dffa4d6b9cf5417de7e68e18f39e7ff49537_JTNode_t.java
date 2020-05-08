 /*
  * Software License Agreement (BSD License)
  * 
  * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
  * All rights reserved.
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
 
 package es.csic.iiia.dcop.jt;
 
 import es.csic.iiia.dcop.Variable;
 import es.csic.iiia.dcop.gdl.GdlNode;
 import es.csic.iiia.dcop.mp.AbstractNode;
 import es.csic.iiia.dcop.mp.Edge;
 import es.csic.iiia.dcop.mp.Result;
 import es.csic.iiia.dcop.up.UPNode;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  *
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public class JTNode extends AbstractNode<JTEdge, Result> {
 
     private UPNode node;
     private Set<Variable> previousVariables;
     private Set<Variable> reachableVariables;
 
     public JTNode(UPNode node) {
         this.node = node;
     }
 
     public UPNode getNode() {
         return this.node;
     }
 
     public void initialize() {
        setMode(Modes.GRAPH);

         Set variables = node.getVariables();
         for (Edge e : getEdges()) {
             e.sendMessage(this, new JTMessage(variables));
         }
 
         previousVariables = new HashSet<Variable>();
         reachableVariables = new HashSet<Variable>(variables);
     }
 
     public long run() {
         // CC count
         long cc = 0;
 
         // Keep the list of previously reachable variables
         previousVariables.addAll(reachableVariables);
 
         // Count incoming variable occurrences
         Hashtable<Variable,Integer> count = new Hashtable<Variable,Integer>();
         for (JTEdge e : getEdges()) {
             JTMessage msg = e.getMessage(this);
             for (Variable v : msg.getVariables()) {
                 reachableVariables.add(v);
                 Integer c = count.get(v);
                 if (c==null) {
                     c = 0;
                 }
                 count.put(v, ++c);
             }
         }
 
         // Seek variables with more than 1 ocurrence
         for (Entry<Variable,Integer> e : count.entrySet()) {
             if (e.getValue() > 1) {
                 node.addVariable(e.getKey());
             }
         }
 
         // Send updated messages
         for (JTEdge e : getEdges()) {
             HashSet<Variable> msg = new HashSet<Variable>(reachableVariables);
             msg.removeAll(e.getMessage(this).getVariables());
             msg.addAll(node.getVariables());
             e.sendMessage(this, new JTMessage(msg));
         }
         
         // And finish.
         setUpdated(false);
         return cc;
     }
 
     public boolean isConverged() {
        return previousVariables.equals(reachableVariables);
     }
 
     public Result end() {
         // Finally, update edge variables
         for (JTEdge e : getEdges()) {
             e.updateVariables();
         }
         return new JTResult(node);
     }
 
     public String getName() {
         return node.getName();
     }
 
     @Override
     public String toString() {
         return node.toString();
     }
 
 }
