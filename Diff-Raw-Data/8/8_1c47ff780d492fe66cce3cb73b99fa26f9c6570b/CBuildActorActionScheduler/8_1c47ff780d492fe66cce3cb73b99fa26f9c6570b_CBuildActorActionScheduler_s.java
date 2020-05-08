 /* 
  * Copyright (c) Ericsson AB, 2013
  * All rights reserved.
  *
  * License terms:
  *
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  *     * Redistributions of source code must retain the above 
  *       copyright notice, this list of conditions and the 
  *       following disclaimer.
  *     * Redistributions in binary form must reproduce the 
  *       above copyright notice, this list of conditions and 
  *       the following disclaimer in the documentation and/or 
  *       other materials provided with the distribution.
  *     * Neither the name of the copyright holder nor the names 
  *       of its contributors may be used to endorse or promote 
  *       products derived from this software without specific 
  *       prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
  * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.caltoopia.codegen.printer;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.caltoopia.ast2ir.PriorityGraph;
 import org.caltoopia.ast2ir.Stream;
 import org.caltoopia.ast2ir.Util;
 import org.caltoopia.ast2ir.Stream.Indent;
 import org.caltoopia.codegen.CEnvironment;
 import org.caltoopia.codegen.CodegenError;
 import org.caltoopia.codegen.UtilIR;
 import org.caltoopia.codegen.printer.CBuildVarDeclaration.varCB;
 import org.caltoopia.codegen.transformer.IrTransformer;
 import org.caltoopia.codegen.transformer.TransUtil;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarAccess;
 import org.caltoopia.codegen.transformer.analysis.IrVariableAnnotation.VarType;
 import org.caltoopia.ir.Action;
 import org.caltoopia.ir.Actor;
 import org.caltoopia.ir.Declaration;
 import org.caltoopia.ir.Block;
 import org.caltoopia.ir.Guard;
 import org.caltoopia.ir.LambdaExpression;
 import org.caltoopia.ir.PortRead;
 import org.caltoopia.ir.PortWrite;
 import org.caltoopia.ir.ProcExpression;
 import org.caltoopia.ir.ReturnValue;
 import org.caltoopia.ir.State;
 import org.caltoopia.ir.Statement;
 import org.caltoopia.ir.Type;
 import org.caltoopia.ir.TypeActor;
 import org.caltoopia.ir.TypeLambda;
 import org.caltoopia.ir.TypeList;
 import org.caltoopia.ir.Variable;
 import org.caltoopia.ir.VariableReference;
 import org.caltoopia.ir.util.IrSwitch;
 import org.eclipse.emf.ecore.EObject;
 
 public class CBuildActorActionScheduler extends IrSwitch<Boolean> {
     String schedStr="";
     Actor actor;
     CEnvironment cenv = null;
     private IndentStr ind = null;
     String thisStr;
     String actorId;
     boolean initialToken = false;
     boolean debugPrint=false;
 
     public CBuildActorActionScheduler(Actor actor, CEnvironment cenv, IndentStr ind, boolean debugPrint) {
         schedStr="";
         this.actor = actor;
         this.cenv = cenv;
         if(ind == null) {
             this.ind = new IndentStr();
         } else {
             this.ind = ind;
         }
         this.debugPrint=debugPrint;
     }
     
     public String toStr() {
         Boolean res = doSwitch(actor);
         if(!res) {
             CodegenError.err("Actor action scheduler builder", schedStr);
         }
         return schedStr;
     }
     
     private void enter(EObject obj) {}
     private void leave() {}
 
     @Override
     public Boolean caseActor(Actor actor) {
         thisStr = Util.marshallQualifiedName(actor.getType().getNamespace()) + "__" + TransUtil.getAnnotationArg(actor, "Instance", "name");
         actorId = "ActorInstance_" + thisStr;
         if(!actor.getInitializers().isEmpty()) {
             for(Action a : actor.getInitializers()) {
                 if(!a.getOutputs().isEmpty()) {
                     initialToken=true;
                     doSwitch(a);
                 }
             }
         }
     
         if(initialToken)
             schedStr += ind.ind() + ("static int initializers_have_run=0;") + ind.nl();
         //TODO This should be one per port and we should point to the correct one when blocking instead of this Any
         schedStr += ind.ind() + ("static const int exitcode_block_Any[3]={1,0,1};") + ind.nl();
         schedStr += ind.ind() + ("ART_ACTION_SCHEDULER(" + thisStr + "_action_scheduler) {") + ind.nl();
         ind.inc();
         schedStr += ind.ind() + ("const int *result = EXIT_CODE_YIELD;") + ind.nl();
         schedStr += ind.ind() + ("ActorInstance_" + thisStr + " *thisActor=(ActorInstance_" + thisStr + "*) pBase;") + ind.nl();
         schedStr += ind.ind() + ("ART_ACTION_SCHEDULER_ENTER(" + actor.getInputPorts().size() + ", " + actor.getOutputPorts().size() + ")") + ind.nl();
         if(initialToken) {
             schedStr += ind.ind() + ("if(!initializers_have_run) {") + ind.nl();
             ind.inc();
             schedStr += ind.ind() + ("initializers_have_run=1;") + ind.nl();
             for(Action a : actor.getInitializers()) {
                 if(!a.getOutputs().isEmpty())
                     schedStr += ind.ind() + ("ART_FIRE_ACTION(" + a.getId() + ");") + ind.nl();
             }
             ind.dec();
             schedStr += ind.ind() + ("}") + ind.nl();
         }
         schedStr += ind.ind() + ("while(1) {") + ind.nl();
         ind.inc();
         if (!actor.getSchedule().getFreeRunners().isEmpty()) {
           printFiringTests(actor.getSchedule().getFreeRunners(), null);
         }
         schedStr += ind.ind() + ("switch(thisActor->_fsmState) {") + ind.nl();
         for (State state : actor.getSchedule().getStates()) {
             schedStr += ind.ind() + ("case " + actorId + "__" + state.getName() + "_ID:") + ind.nl();
             ind.inc();
             PriorityGraph priorityGraph = (PriorityGraph) state.getPriorityGraph();
             Map<Action, String> action2TargetMap = (Map<Action, String>) state.getAction2TargetMap();
             printFiringTests(priorityGraph.getOneTopologicalOrder(), action2TargetMap);     
             schedStr += ind.ind() + ("result = exitcode_block_Any;") + ind.nl();
             schedStr += ind.ind() + ("goto out;") + ind.nl();
             schedStr += ind.ind() + ("break;") + ind.nl();
             ind.dec();
         }
         schedStr += ind.ind() + ("}") + ind.nl();
         ind.dec();
 
         schedStr += ind.ind() + ("}") + ind.nl();
         schedStr += ind.ind() + ("out:") + ind.nl();
         schedStr += ind.ind() + ("ART_ACTION_SCHEDULER_EXIT(" + actor.getInputPorts().size() + ", " + actor.getOutputPorts().size() + ")") + ind.nl();
         schedStr += ind.ind() + ("return result;") + ind.nl();
         ind.dec();
         schedStr += ind.ind() + ("}") + ind.nl();
         return true;
     }
     
     private void printFiringTests(List<Action> actions, Map<Action, String> action2TargetMap) {
         
         for(Iterator<Action> a = actions.iterator();a.hasNext();) {
             Action action = a.next();
             // First check for availability of tokens
             if (!action.getInputs().isEmpty()) {
                 schedStr += ind.ind() + ("if (");
                 for (Iterator<PortRead> i = action.getInputs().iterator(); i.hasNext();) {
                     PortRead read = i.next();
                     Type t = read.getPort().getType();
                     if (UtilIR.isList(t)) {
                         t = ((TypeList) t).getType();
                     }
                     schedStr += ("(pinAvailIn_" + new CBuildTypeName(t, new CPrintUtil.dummyCB(), false).toStr());
                     String portNbr = TransUtil.getAnnotationArg(read, "Port", "index");
                     String portStr = "IN" + portNbr+ "_" + TransUtil.getAnnotationArg(read, "Port", "name");
                     schedStr += ("(" + portStr + ") >= ");
                     if (UtilIR.isList(read.getPort().getType())) {
                         new CBuildExpression(((TypeList) t).getSize(),cenv).toStr();
                         schedStr += ("*");
                     } 
                     if(read.getRepeat()!=null)
                         schedStr += new CBuildExpression(read.getRepeat(),cenv).toStr();
                     else
                         schedStr += ("1");
 
                     //When many variables each variable need to exist in fifo
                     if(read.getVariables().size()>1)
                         schedStr += ("*"+read.getVariables().size());
 
                     schedStr += (")");
                     if (i.hasNext()) schedStr += (" && ");
                 }
                 schedStr += (") {") + ind.nl();
                 ind.inc();
             }
             
             if (!action.getGuards().isEmpty()) {
                 schedStr += ind.ind() + ("if (");
                 String actionId = thisStr + CPrintUtil.getNamespace(action.getTag());
                 schedStr += "Guard_" + actionId + "__" + action.getId()+ "(context, thisActor)";
                 schedStr += (") {") + ind.nl();
                 ind.inc();
             }
     
             if (!action.getOutputs().isEmpty()) {
                 schedStr += ind.ind() + ("if (");
                 for (Iterator<PortWrite> i = action.getOutputs().iterator(); i.hasNext();) {
                     PortWrite write = i.next();
                     Type t = write.getPort().getType();
                     if (UtilIR.isList(t)) {
                         t = ((TypeList) t).getType();
                     } 
                     schedStr += ("(pinAvailOut_" + new CBuildTypeName(t, new CPrintUtil.dummyCB(), false).toStr());
                     String portNbr = TransUtil.getAnnotationArg(write, "Port", "index");
                     String portStr = "OUT" + portNbr+ "_" + TransUtil.getAnnotationArg(write, "Port", "name");
                     schedStr += ("(" + portStr + ") >= ");  
                     if (UtilIR.isList(write.getPort().getType())) {
                         new CBuildExpression(((TypeList) t).getSize(),cenv).toStr();
                         schedStr += ("*");
                     } 
                     if(write.getRepeat()!=null)
                         schedStr += new CBuildExpression(write.getRepeat(),cenv).toStr();
                     else
                         schedStr += ("1");
                     
                     //When many expressions each expression need to have room
                     if(write.getExpressions().size()>1)
                         schedStr += ("*"+write.getExpressions().size());
                         
                     schedStr += (")");
                     if (i.hasNext()) schedStr += (" && ");
                 }
                 schedStr += (") {") + ind.nl();
                 ind.inc();
             }
             schedStr += ind.ind() + ("ART_FIRE_ACTION(" + action.getId() + ");") + ind.nl();
             if (action2TargetMap != null) {
                 if(debugPrint) {
                     schedStr += ind.ind() + ("if(thisActor->_fsmState != " + actorId + "__" + action2TargetMap.get(action) + "_ID) {") + ind.nl();
                     ind.inc();
                     schedStr += ind.ind() + ("dprint2(\"%i --> %i " + actorId + "__" + action2TargetMap.get(action) +
                             "\\n\", thisActor->_fsmState, " + actorId + "__" + action2TargetMap.get(action) + "_ID" + ");") + ind.nl();
                     ind.dec();
                     schedStr += ind.ind() + ("}") + ind.nl();
                 }
                 schedStr += ind.ind() + ("thisActor->_fsmState = ");
                 schedStr += (actorId + "__" + action2TargetMap.get(action) + "_ID");
                 schedStr += (";") + ind.nl();
             }
             schedStr += ind.ind() + ("continue;") + ind.nl();
             if (!action.getOutputs().isEmpty()) {
                 ind.dec();
                 schedStr += ind.ind() + ("} else {") + ind.nl();
                 ind.inc();
                 schedStr += ind.ind() + ("result = exitcode_block_Any;") + ind.nl();
                 schedStr += ind.ind() + ("goto out;") + ind.nl();
                 ind.dec();
                 schedStr += ind.ind() + ("}") + ind.nl();
                ind.dec();
             }
             if (!action.getGuards().isEmpty()) {
                    schedStr += ind.ind() + ("}") + ind.nl();
                    ind.dec();
             }
             
             if (!action.getInputs().isEmpty()) {
                 schedStr += ind.ind() + ("}") + ind.nl();
             }
         }
     }
 }
