 /**********************************************************************
  $Id: logNode.java,v 1.9 2002/10/31 04:17:05 vpapad Exp $
 
 
   NIAGARA -- Net Data Management System                                 
                                                                         
   Copyright (c)    Computer Sciences Department, University of          
                        Wisconsin -- Madison                             
   All Rights Reserved.                                                  
                                                                         
   Permission to use, copy, modify and distribute this software and      
   its documentation is hereby granted, provided that both the           
   copyright notice and this permission notice appear in all copies      
   of the software, derivative works or modified versions, and any       
   portions thereof, and that both notices appear in supporting          
   documentation.                                                        
                                                                         
   THE AUTHORS AND THE COMPUTER SCIENCES DEPARTMENT OF THE UNIVERSITY    
   OF WISCONSIN - MADISON ALLOW FREE USE OF THIS SOFTWARE IN ITS "        
   AS IS" CONDITION, AND THEY DISCLAIM ANY LIABILITY OF ANY KIND         
   FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE USE OF THIS SOFTWARE.   
                                                                         
   This software was developed with support by DARPA through             
    Rome Research Laboratory Contract No. F30602-97-2-0247.  
 **********************************************************************/
 
 
 /**
  * This class is used to represent a node in the logical operator tree. Has
  * data members that helps in book-keeping when generation the plan.
  *
  */
 package niagara.xmlql_parser.op_tree;
 
 import java.util.*;
 import java.io.*;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 import niagara.xmlql_parser.syntax_tree.*;
 import niagara.connection_server.NiagraServer;
 import niagara.data_manager.*;
 import niagara.optimizer.colombia.Op;
 import niagara.query_engine.PhysicalOperator;
 import niagara.query_engine.SchedulablePlan;
 import niagara.query_engine.TupleSchema;
 import niagara.utils.*;
 
 public class logNode implements SchedulablePlan, java.io.Serializable {
    protected op operator;	// operator
 
    protected Schema tupleDes;	// describes the tuple
 				// captures the parent-child relationship
 				// among the different elements scanned so far
    protected varTbl varList;	// list of var used in the subtree rooted at
 				// this node. Maps variables to their 
 				// schemaAttributes
 
    protected logNode[] inputs;	// array of inputs or logNode
    
    protected int[] inputsId;    // For Trigger ONLY!
    protected int Id;            // for trig use.  Not trig system Id = -1
 
     protected boolean isHead;
 
    /**
     * Constructor without any parameter
     */
    public logNode() {
        operator = null;
        inputs = new logNode[1];
        Id = -1;
    }
 
    /**
     * Constructor
     *
     * @param the unary operator
     * @param the only input to this operator
     */
    public logNode(op _op, logNode in) {
 	inputs = new logNode[1];
 	inputs[0] = in;
 	operator = _op;
    }
    
    /**
     * Constructor
     *
     * @param the operator
     */
    public logNode(op _op) {
 	operator = _op;
         Id = -1;
 	inputs = new logNode[] {};
 	tupleDes = null;
 	varList = null;
    }
 
    /**
     * Constructor
     *
     * @param the binary operator
     * @param the left subtree
     * @param the right subtree
     */
    public logNode(op _op, logNode leftin, logNode rightin) {
 	inputs = new logNode[2];
         Id = -1;
 	inputs[0] = leftin;
 	inputs[1] = rightin;
 	operator = _op;
    }
 
     public logNode(op operator, logNode[] inputs) {
 	this.operator = operator;
 	Id = -1;
 	this.inputs = inputs;
     }
 
    public int getId() {
         return Id;
    }
 
    public void setId(int id) {
        Id = id;
    }
 
    /**
     * @return the operator
     */
    public op getOperator() {
 	return operator;
    }
 
    public void setOperator(op operator) {
        this.operator = operator;
    }
 
     public logNode[] getInputs() {
         return inputs;
     }
 
    /**
     * This function returns the number of inputs to this logical node
     *
     * @return The number of inputs to this logical node
     */
 
    public int getArity() {
         return inputs.length;
    }
 
    /**
     * @return the left subtree
     */
    public logNode leftInput() {
 	return input(0);
    }
 
    /**
     * @return the right subtree
     */
    public logNode rightInput() {
 	return input(1);
    }
 
    public logNode input(int i) {
         // OK.  Trigger trick comes in
         if(inputs==null) return null;
         if(i>=inputs.length) return null; // should throw sth?
         return inputs[i];
         // if(trig==null) return null; // should not happen
         // inputs[i] = trig.findLogNode(inputsId[i]);
         // return inputs[i];
    }
 
    /**
     * @return the first subtree or the only subtree in case of unary operators
     */
    public logNode input() {
 	return input(0);
    }
 
    /**
     * to set the Nth child
     *
     * @param the root of the subtree
     * @param the position of this child
     */
    public void setInput(logNode newChild, int index) {
 	if (index >= inputs.length)
 		System.err.println("index out of range");
 	inputs[index]=newChild;
         if(inputsId==null) return;
         inputsId[index] = newChild.Id;
    }
 
     public void setInputs(logNode[] inputs) {
         this.inputs = inputs;
     }
 
    /**
     * @param the Schema of the tuples at this node
     */
    public void setSchema(Schema _td) {
 	tupleDes = _td;
    }
 
    /**
     * @param variable table with the variables encountered so far
     */
    public void setVarTbl(varTbl _vt) {
 	varList = _vt;
    }
 
    /**
     * @return true if the given set of variables is contained in the variables
     *         encountered in the subtree rooted at this node, false otherwise
     */
    public boolean contains(Vector variables) {
 	if(varList == null)
 		return false;
 	return varList.contains(variables);
    }
 
    /**
     * @return the variable table
     */
    public varTbl getVarTbl() {
 	return varList;
    }
 
    /**
     * @return the Schema created from the elements encountered so far
     */
    public Schema getSchema() {
 	return tupleDes;
    }
 
    /**
     * used for creating a postscript representation of this logical plan
     * tree using the 'dot' command. called recursively on the child nodes.
     *
     * @return String representation for the 'dot' command
     */
    public String makeDot() {
       String dot="";
       String thisNode = "\""+operator.toString()+"\"";
       dot+=operator.hashCode()+" [label="+thisNode+"];\n";
       if(inputs != null)
          for(int i=0;i<inputs.length;i++) {
 	    dot+=operator.hashCode()+"->"+inputs[i].getOperator().hashCode()+";\n";
 	    dot+=inputs[i].makeDot();
          }
       return dot;
    }
 
    /**
     * saves the String representation of this tree for the dot command into a
     * file that can be used to generate a postscript file with the graph.
     *
     * @param the String
     * @param the output 
     */
    public static void writeDot(String dot, Writer writer) {
       PrintWriter pw = null;
       try {
 	 pw = new PrintWriter(writer);
 	 pw.println("digraph QueryPlan {");
 	 pw.println(dot);
 	 pw.println("}");
 	 pw.close();
       } catch (Exception e) {
 	 e.printStackTrace();
       } finally {
 	 try {
 	   if (pw != null)
 	      pw.close();
          } catch (Exception e) {}
       }
    }
 
    /**
     * prints this node to the standard output
     */
    public void dump() {
        dump(new Hashtable());
    }
 
     protected void dump(Hashtable nodesDumped) {
 	if (nodesDumped.containsKey(this))
 	    return;
 	nodesDumped.put(this, this);
 
 	operator.dump();
 
 	if(inputs != null)
 	   for(int i=0; i<inputs.length; i++)
 		input(i).dump(nodesDumped);
     }
 
 
     public boolean isSchedulable() { return true; }
 
     public String getName() {
         // return an artificial name for this operator instance
         return operator.getName() + "#" + operator.hashCode();
     }
 
     public boolean isAccumulateOp() {
 	return (operator instanceof AccumulateOp);
     }
 
     public String getAccumFileName() {
 	if(!isAccumulateOp()) {
 	    throw new PEException("Can't get AccumFile name from non-accumulate operator");
 	}
 	return ((AccumulateOp)operator).getAccumFileName();
     }
 
 
     public boolean isHead() {
 	return isHead;
     }
 
     public void setIsHead() {
 	isHead = true;
     }
 
     /** Instantiate the selected physical operator algorithm */
     public PhysicalOperator getPhysicalOperator() {
         Class physicalOperatorClass = operator.getSelectedAlgo();
         // If there is no selected algo, error
         if (physicalOperatorClass == null) {
             throw new PEException("No algorithm selected");
         }
 
         PhysicalOperator physicalOperator;
         try {
         // Get the zero-argument constructor        
         Constructor constructor =
             physicalOperatorClass.getConstructor(new Class[] {
         });
 
         // Create a new physical operator object
         physicalOperator =
             (PhysicalOperator) constructor.newInstance(new Object[] {
         });
         } catch (NoSuchMethodException nsme) {
             throw new PEException("Could not find a zero-argument constructor for: " + physicalOperatorClass);
         } catch (InstantiationException e) {
             throw new PEException(
                 "Error in Instantiating Physical Operator" + e.getMessage());
         } catch (IllegalAccessException e) {
             throw new PEException(
                 "Error in Instantiating Physical Operator" + e.getMessage());
         } catch (InvocationTargetException e) {
             throw new PEException(
                 "Error in Instantiating Physical Operator" + e.getMessage());
         }
 
         physicalOperator.initFrom(operator);
         // XXX vpapad: tuple schema construction is useless
         // for legacy query plans, but we still have to do it 
         // even with empty tuple schemas
         TupleSchema[] ts = new TupleSchema[getArity()];
         for (int i = 0; i < getArity(); i++)
             ts[i] = new TupleSchema();
        physicalOperator.constructTupleSchema(ts);
         return physicalOperator;
     }
     
     public SchedulablePlan getInput(int i) {
         return input(i);
     }
     
     public  boolean isSource() {
         return operator.isSourceOp();
     }
     
     public void setInputs(SchedulablePlan[] inputs) {
         setInputs((logNode[]) inputs);
     }
     
     public int getNumberOfOutputs() {
         return operator.getNumberOfOutputs();
     }
 
     public void processSource(SinkTupleStream sinkStream, DataManager dm) 
     throws ShutdownException {
         if (!isSource())
             throw new PEException("Not a source op");
         op sourceOp = operator;
 
         if (sourceOp instanceof dtdScanOp) {
             dtdScanOp dop = (dtdScanOp) sourceOp;
             // Ask the data manager to start filling the output stream with
             // the parsed XML documents
             boolean scan = dm.getDocuments(dop.getDocs(), null, sinkStream);
             if (!scan)
                 System.err.println(
                     "dtdScan FAILURE! " + dop.getDocs().elementAt(0));
         } else { 
             // XXX vpapad: stopgap
             // After merge is complete, logNodes will only handle
             // XML-QL plans, that use only dtdscan as a source op
             StreamThread st = new StreamThread();
             st.initFrom(sourceOp);
             st.plugIn(sinkStream, dm);
             (new Thread(st)).start();
         }
     }
     
     public void setOperator(Op operator) {
         this.operator = (op) operator;
     }
     
     // XXX vpapad: temporary hack to get CVS to compile
     public String getLocation() {return null;}
     public void setLocation(String location) {}
     public String planToXML() {return null;}
     public void setName(String name) {}
 }
 
 
 
