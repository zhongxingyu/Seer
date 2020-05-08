 
 /**********************************************************************
  $Id: PhysicalConstructOperator.java,v 1.1 2000/05/30 21:03:26 tufte Exp $
 
 
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
 
 
 package niagara.query_engine;
 
 import java.util.ArrayList;
 import java.util.Vector;
 import com.ibm.xml.parser.TXElement;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import niagara.utils.*;
 import niagara.xmlql_parser.op_tree.*;
 import niagara.xmlql_parser.syntax_tree.*;
 
 /**
  * This is the <code>PhysicalConstructOperator</code> that extends
  * the basic PhysicalOperator with the implementation of the Construct
  * operator.
  *
  * @version 1.0
  *
  */
 
 public class PhysicalConstructOperator extends PhysicalOperator {
 
     /////////////////////////////////////////////////////////////////////////
     // These are the private data members of the PhysicalConstructOperator //
     // class                                                               //
     /////////////////////////////////////////////////////////////////////////
 
     // This is the array having information about blocking and non-blocking
     // streams
     //
     private static final boolean[] blockingSourceStreams = { false };
 
     // The result template to construct from
     //
     private constructBaseNode resultTemplate;
 
 
     ///////////////////////////////////////////////////////////////////////////
     // These are the methods of the PhysicalConstructOperator class          //
     ///////////////////////////////////////////////////////////////////////////
 
     /**
      * This is the constructor for the PhysicalConstructOperator class that
      * initializes it with the appropriate logical operator, source streams,
      * destination streams, and the responsiveness to control information.
      *
      * @param logicalOperator The logical operator that this operator implements
      * @param sourceStreams The Source Streams associated with the operator
      * @param destinationStreams The Destination Streams associated with the
      *                           operator
      * @param responsiveness The responsiveness to control messages, in milli
      *                       seconds
      */
 
     public PhysicalConstructOperator(op logicalOperator,
 				     Stream[] sourceStreams,
 				     Stream[] destinationStreams,
 				     Integer responsiveness) {
 
 
 	// Call the constructor of the super class
 	//
 	super(sourceStreams,
 	      destinationStreams,
 	      blockingSourceStreams,
 	      responsiveness);
 
 	// Typecast to a construct logical operator
 	//
 	constructOp constructLogicalOp = (constructOp) logicalOperator;
 
 	// Initialize the result template
 	//
 	this.resultTemplate = constructLogicalOp.getResTemp();
     }
     
 
     /**
      * This function processes a tuple element read from a source stream
      * when the operator is in a non-blocking state. This over-rides the
      * corresponding function in the base class.
      *
      * @param tupleElement The tuple element read from a source stream
      * @param streamId The source stream from which the tuple was read
      * @param result The result is to be filled with tuples to be sent
      *               to destination streams
      *
      * @return True if the operator is to continue and false otherwise
      */
 
     protected boolean nonblockingProcessSourceTupleElement (
 						 StreamTupleElement tupleElement,
 						 int streamId,
 						 ResultTuples result) {
 
 	// Recurse down the result template to construct result
 	//
 	ArrayList resultList = constructResult(tupleElement, resultTemplate);
 
 	// Add all the results in the result list as result tuples
 	//
 	int numResults = resultList.size();
 
 	for (int res = 0; res < numResults; ++res) {
 
 	    // Clone the input tuple element to create a new output tuple
 	    //element
 	    //
 	    StreamTupleElement outputTuple = 
 		              (StreamTupleElement) tupleElement.clone();
 
 	    // Append the constructed result to end of newly created tuple
 	    //
 	    outputTuple.appendAttribute(resultList.get(res));
 
 	    // Add the new tuple to the result
 	    //
             
 	    result.add(outputTuple, 0);
 	}
 	// The operator can continue
 	//
 	return true;
     }
 
 
     /**
      * This function constructs results given a tuple and a result template
      *
      * @param tupleElement the tuple to construct results from
      * @param templateRoot the root of the result template
      *
      * @return a list of nodes constructed as per the template
      */
 
     public static ArrayList constructResult (
 				       StreamTupleElement tupleElement,
 				       constructBaseNode templateRoot) {
 
 	// Check if the template root is an internal node or a leaf node
 	// and process accordingly
 	//
 	if (templateRoot instanceof constructLeafNode) {
 
 	    return processLeafNode(tupleElement,
 				   (constructLeafNode) templateRoot);
 	}
 	else if (templateRoot instanceof constructInternalNode) {
 
 	    return processInternalNode(tupleElement,
 				       (constructInternalNode) templateRoot);
 	}
 	else {
 	    System.err.println("Error: Unknown construct node type!");
 	    return null;
 	}
     }
 
 
     /**
      * This function processes a leaf node during the construction process
      *
      * @param tupleElement The tuple to construct the result from
      * @param leafConstructNode The leaf node having details of construction
      *
      * @return The list of results constructed
      */
 
     private static ArrayList processLeafNode (StreamTupleElement tupleElement,
 					      constructLeafNode leafNode) {
 
 	// Create a place holder for the result
 	//
 	ArrayList result = new ArrayList();
 
 	// Get the data of the leaf node
 	//
 	data leafData = leafNode.getData();
 
 	// Check the type of the data
 	//
 	int type = leafData.getType();
 
 	if (type == dataType.STRING) {
 
 	    // Add the string value to the result
 	    //
	    result.add(leafData.getValue());
 	}
 	else if (type == dataType.ATTR) {
 
 	    // First get the schema attribute
 	    //
 	    schemaAttribute schema = (schemaAttribute) leafData.getValue();
 
 	    // Now construct result based on whether it is to be interpreted
 	    // as an element or a parent
 	    //
 	    if (schema.getType() == varType.ELEMENT_VAR) {
 
 		// The value of the leafData is a schema attribute - from it
 		// get the attribute id in the tuple to construct from
 		//
 		int attributeId = ((schemaAttribute) leafData.getValue()).getAttrId();
 
 		// Add the attribute as the result
 		//
 		result.add(tupleElement.getAttribute(attributeId));
 	    }
 	    else if (schema.getType() == varType.CONTENT_VAR) {
 
 		// The value of the leafData is a schema attribute - from it
 		// get the attribute id in the tuple to construct from
 		//
 		int attributeId = ((schemaAttribute) leafData.getValue()).getAttrId();
 
 		// Get the children of the attribute
 		//
 		NodeList nodeList =
 		    ((Node) tupleElement.getAttribute(attributeId)).getChildNodes();
 
 		// Add all the children to the result
 		//
 		int numChildren = nodeList.getLength();
 
 		for (int child = 0; child < numChildren; ++child) {
 
 		    result.add(nodeList.item(child));
 		}
 	    }
 	    else {
 
 		System.err.println("Unknown schema attribute type in construct leaf node");
 	    }
 	}
 	else {
 
 	    System.err.println("Unknown type in construct leaf node");
 	}
 
 	// Return the constructed result
 	//
 	return result;
     }
 
     
     /**
      * This function processes a internal node during the construction process
      *
      * @param tupleElement The tuple to construct the result from
      * @param interalNode The internal node having details of construction
      *
      * @return The list of results constructed
      */
 
     private static ArrayList processInternalNode (
 					 StreamTupleElement tupleElement,
 					 constructInternalNode internalNode) {
 
 	// Create a new element node with the required tag name
 	// taking care of tagvariables
 
 	data tagData = internalNode.getStartTag().getSdata();
 	String tagName;
 
 	if(tagData.getType() == dataType.ATTR) {
 	   schemaAttribute sattr = (schemaAttribute)tagData.getValue();
 	   int attrId = sattr.getAttrId();
 	   tagName = ((Node)tupleElement.getAttribute(attrId)).getNodeName();
 	}
 	else
 	   tagName = (String)tagData.getValue();
 
 	TXElement resultElement = 
 	    new TXElement(tagName);
 
 	// Recurse on all children and construct result
 	//
 	Vector children = internalNode.getChildren();
 
 	int numChildren = children.size();
 
 	for (int child = 0; child < numChildren; ++child) {
 
 	    // Get constructed results from child
 	    //
 	    ArrayList childResult = 
 		constructResult(tupleElement,
 				(constructBaseNode) children.get(child));
 
 	    // Add each constructed result to the result element
 	    //
 	    int numResults = childResult.size();
 
 	    for (int res = 0; res < numResults; ++res) {
 
 		resultElement.appendChild(((Node) childResult.get(res)).cloneNode(true));
 	    }
 	}
 
 	// Construct the result array list
 	//
 	ArrayList result = new ArrayList(1);
 
 	result.add(resultElement);
 
 	// Return the result
 	//
 	return result;
     }
 }
