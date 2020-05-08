 /*
  * Copyright (C) 2008 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.xml.saxon;
 
 import org.trancecode.core.collection.TubularIterables;
 
 import com.google.common.collect.Iterables;
 
 import net.sf.saxon.s9api.Axis;
 import net.sf.saxon.s9api.XdmItem;
 import net.sf.saxon.s9api.XdmNode;
 
 
 /**
  * Utility methods related to {@link Iterable} and Saxon.
  * 
  * @author Herve Quiroz
  * @version $Revision$
  */
 public final class SaxonIterables
 {
 	private SaxonIterables()
 	{
 		// No instantiation
 	}
 
 
 	public static Iterable<XdmItem> childXdmItems(final XdmNode node)
 	{
		return Iterables.concat(
			TubularIterables.newIterable(SaxonSuppliers.axisIterator(node, Axis.ATTRIBUTE)), TubularIterables
				.newIterable(SaxonSuppliers.axisIterator(node, Axis.CHILD)));
 	}
 
 
 	public static Iterable<XdmNode> childNodes(final XdmNode node)
 	{
 		return Iterables.filter(childXdmItems(node), XdmNode.class);
 	}
 
 
 	public static Iterable<XdmNode> childElements(final XdmNode node)
 	{
 		return Iterables.filter(childNodes(node), SaxonPredicates.isElement());
 	}
 
 
 	public static Iterable<XdmNode> attributes(final XdmNode node)
 	{
 		return Iterables.filter(childNodes(node), SaxonPredicates.isAttribute());
 	}
 }
