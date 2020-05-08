 /**
  * AbstractFunctionNode.java
  * 
  * Copyright 2009 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.gp.impl;
 
 import java.util.List;
 
 import jmona.gp.FunctionNode;
 import jmona.gp.Node;
 
 /**
  * A base class for an inner Node representing an element from the function set.
  * 
  * @param <V>
  *          The type of value to which this Node evaluates.
  * @author jfinkels
  */
 public abstract class AbstractFunctionNode<V> extends AbstractNode<V> implements
     FunctionNode<V> {
 
   /**
    * Children of this Node. The size of this List must equal the "arity" of this
    * Node.
    */
  private List<Node<V>> children = null;
 
   /**
    * {@inheritDoc}
    * 
    * @return {@inheritDoc}
    * @see jmona.gp.Node#children()
    */
   @Override
   public List<Node<V>> children() {
     return this.children;
   }
 
 }
