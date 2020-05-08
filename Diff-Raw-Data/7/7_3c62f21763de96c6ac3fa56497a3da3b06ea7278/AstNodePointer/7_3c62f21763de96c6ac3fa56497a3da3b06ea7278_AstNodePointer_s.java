 /**
  * Copyright 2011 The Open Source Research Group,
  *                University of Erlangen-Nürnberg
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package de.fau.cs.osr.ptk.common.jxpath;
 
 import org.apache.commons.jxpath.ri.Compiler;
 import org.apache.commons.jxpath.ri.QName;
 import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
 import org.apache.commons.jxpath.ri.compiler.NodeTest;
 import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
 import org.apache.commons.jxpath.ri.model.NodeIterator;
 import org.apache.commons.jxpath.ri.model.NodePointer;
 
 import de.fau.cs.osr.ptk.common.ast.AstNode;
 import de.fau.cs.osr.utils.FmtInternalLogicError;
 
 public class AstNodePointer
         extends
             NodePointer
 {
 	private static final long serialVersionUID = 1L;
 	
 	/** The node this pointer points to. */
 	private AstNode node;
 	
 	// =========================================================================
 	
 	public AstNodePointer(AstNode node)
 	{
 		super(null);
 		this.node = node;
 		//debug("AstNodePointer(AstNode node)");
 		
 	}
 	
 	public AstNodePointer(NodePointer parent, AstNode node)
 	{
 		// Usually called by an AstNodeIterator. In this case, `parent' would
 		// point to the node that we iterated over and `node' would be the 
 		// child node we found.
 		
 		super(parent);
 		this.node = node;
 		//debug("AstNodePointer(NodePointer parent, AstNode node)");
 	}
 	
 	// =========================================================================
 	
 	@Override
 	public NodeIterator attributeIterator(QName name)
 	{
 		//debug("attributeIterator");
 		
		if (node.hasAttributes())
			return null;
 		
		return new AstPropertyIterator(this, name);
 	}
 	
 	@Override
 	public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith)
 	{
 		//debug("childIterator");
 		
 		if (isLeaf())
 		{
 			//throw new FormattedInternalLogicError("This should not happen!?");
 			// But it does :(
 			return null;
 		}
 		
 		return new AstNodeFieldIterator(this, test, reverse, startWith);
 	}
 	
 	@Override
 	public boolean isLeaf()
 	{
 		/*
 		boolean isEmpty = node.isEmpty();
 		if (!isEmpty)
 		{
 			isEmpty = true;
 			for (AstNode child : node)
 				if (child != null)
 				{
 					isEmpty = false;
 					break;
 				}
 			
 		}
 		
 		return isEmpty;
 		*/
 
 		return node.isEmpty();
 	}
 	
 	@Override
 	public boolean isActual()
 	{
 		return true;
 	}
 	
 	@Override
 	public boolean isCollection()
 	{
 		// As far as I can tell, collection semantics is not really part of
 		// XPath. Anyway, I don't get how it's supposed to work ...
 		return false;
 		
 		// If it has a variable number of children, then the non-fixed children 
 		// will have to be exposed as a collection
 		//return node.hasVariable() || !node.isEmpty();
 	}
 	
 	@Override
 	public int getLength()
 	{
 		return 1;
 		
 		//if (isCollection())
 		//	return node.size();
 		//else
 		//	return 1;
 	}
 	
 	@Override
 	public QName getName()
 	{
 		// Since we don't handle namespaces, we probably should return 
 		// `null' as namespace prefix in names.
 		return new QName(null, getName2());
 		
 		//Class<? extends AstNode> clazz = node.getClass();
 		//return new QName(
 		//        clazz.getPackage().getName(),
 		//        clazz.getSimpleName());
 	}
 	
 	@Override
 	public Object getBaseValue()
 	{
 		return node;
 	}
 	
 	@Override
 	public Object getImmediateNode()
 	{
 		return node;
 	}
 	
 	@Override
 	public void setValue(Object value)
 	{
 		throw new UnsupportedOperationException("Not supported yet.");
 	}
 	
 	@Override
 	public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2)
 	{
 		//debug("compareChildNodePointers");
 		
 		if (pointer1.getBaseValue() == pointer2.getBaseValue())
 			return 0;
 		
 		boolean p1IsProp = pointer1 instanceof AstPropertyPointer;
 		boolean p2IsProp = pointer2 instanceof AstPropertyPointer;
 		if (p1IsProp && !p2IsProp)
 		{
 			return -1;
 		}
 		else if (!p1IsProp && p2IsProp)
 		{
 			return 1;
 		}
 		else if (p1IsProp && p2IsProp)
 		{
 			AstPropertyPointer p1 = (AstPropertyPointer) pointer1;
 			AstPropertyPointer p2 = (AstPropertyPointer) pointer2;
 			
 			int cmp = p1.getName2().compareTo(p2.getName2());
 			if (cmp == 0)
 				throw new FmtInternalLogicError();
 			
 			return cmp;
 		}
 		else
 		{
 			AstNode node1 = (AstNode) pointer1.getBaseValue();
 			AstNode node2 = (AstNode) pointer2.getBaseValue();
 			
 			for (AstNode child : node)
 			{
 				if (child == node1)
 				{
 					return -1;
 				}
 				else if (child == node2)
 				{
 					return 1;
 				}
 			}
 			
 			throw new FmtInternalLogicError();
 		}
 	}
 	
 	// =========================================================================
 	
 	@Override
 	public String asPath()
 	{
 		StringBuffer buffer = new StringBuffer();
 		if (parent != null)
 		{
 			buffer.append(parent.asPath());
 			
 			// If the parent pointer is not one of our pointer types, it is the 
 			// parent's responsibility to produce the node test part of the path
 			if (parent instanceof AstNodePointer || parent instanceof AstNodeFieldPointer)
 			{
 				if (buffer.length() == 0 || buffer.charAt(buffer.length() - 1) != '/')
 					buffer.append('/');
 				
 				buffer.append(getName2());
 				if (parent != null)
 				{
 					buffer.append('[');
 					buffer.append(getRelativePosition());
 					buffer.append(']');
 				}
 			}
 		}
 		else
 		{
 			buffer.append('/');
 		}
 		
 		return buffer.toString();
 	}
 	
 	private int getRelativePosition()
 	{
 		if (parent != null)
 		{
 			if (parent instanceof AstNodePointer || parent instanceof AstNodeFieldPointer)
 			{
 				NodePointer p = (NodePointer) parent;
 				
 				AstNode thisNode = (AstNode) getImmediateNode();
 				
 				int i = 1;
 				for (AstNode n : (AstNode) p.getImmediateNode())
 				{
 					if (n == thisNode)
 						return i;
 					++i;
 				}
 				
 				throw new FmtInternalLogicError();
 			}
 			/*
 			else if (parent instanceof AstNodeFieldPointer)
 			{
 				AstNodeFieldPointer p = (AstNodeFieldPointer) parent;
 				
 				int i = 1;
 				for (AstNode n : (AstNode) p.getImmediateNode())
 				{
 					if (n == node)
 						return i;
 					++i;
 				}
 				
 				throw new FormattedInternalLogicError();
 			}
 			*/
 			else
 				return 1;
 		}
 		else
 			return 1;
 	}
 	
 	// =========================================================================
 	
 	@Override
 	public int hashCode()
 	{
 		return node.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		AstNodePointer other = (AstNodePointer) obj;
 		if (node != other.node)
 			return false;
 		return true;
 	}
 	
 	// =========================================================================
 	
 	@Override
 	public boolean testNode(NodeTest test)
 	{
 		//debug("testNode", test);
 		
 		if (test == null)
 		{
 			return true;
 		}
 		else if (test instanceof NodeNameTest)
 		{
 			NodeNameTest nodeNameTest = (NodeNameTest) test;
 			
 			QName testName = nodeNameTest.getNodeName();
 			
 			// We don't support prefixes
 			if (testName.getPrefix() != null)
 				return false;
 			
 			// Wildcards don't match when prefixes differ, so check this after 
 			// we made sure that we are not looking for a prefixed name.
 			if (nodeNameTest.isWildcard())
 				return true;
 			
 			// Perform actual name check
 			String s1 = getName2();
 			String s2 = testName.getName();
 			
 			// s1 cannot be `null'
 			return s1 == s2 || s1.equals(s2);
 		}
 		else if (test instanceof NodeTypeTest)
 		{
 			NodeTypeTest nodeTypeTest = (NodeTypeTest) test;
 			switch (nodeTypeTest.getNodeType())
 			{
 				case Compiler.NODE_TYPE_NODE:
 					return true;
 					
 				case Compiler.NODE_TYPE_TEXT:
 				case Compiler.NODE_TYPE_COMMENT:
 				case Compiler.NODE_TYPE_PI:
 				default:
 					return false;
 			}
 		}
 		else
 		{
 			// Unhandled: ProcessingInstructionTest
 			//   An AstNode can never be a PI.
 			
 			return false;
 		}
 	}
 	
 	// =========================================================================
 	
 	private String getName2()
 	{
 		return node.getClass().getSimpleName();
 	}
 	
 	// =========================================================================
 	
 	/*
 	private void debug(String where, Object... params)
 	{
 		StringBuilder b = new StringBuilder();
 		
 		String path = asPath();
 		b.append(String.format(
 		        "@%8x : %s : %s%s.%s ; ",
 		        System.identityHashCode(this),
 		        path,
 		        StringUtils.strrep(' ', 28 - path.length()),
 		        getClass().getSimpleName(),
 		        where));
 		
 		int i = 0;
 		for (Object o : params)
 		{
 			if (i++ != 0)
 				b.append(", ");
 			b.append(o == null ? "null" : o.toString());
 		}
 		
 		System.out.println(b.toString());
 	}
 	*/
 }
