 /*
  * @(#)PageNode.java
  *
  * Copyright 2009 Instituto Superior Tecnico, João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Contents Module for the MyOrg web application.
  *
  *   The Contents Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Contents Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Contents Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package module.contents.domain;
 
 import myorg.domain.VirtualHost;
 import myorg.domain.contents.Node;
 import myorg.domain.groups.AnyoneGroup;
 import myorg.presentationTier.Context;
 import myorg.presentationTier.actions.ContextBaseAction;
 import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;
 
 public class PageNode extends PageNode_Base {
     
     public PageNode() {
         super();
         setAccessibilityGroup(AnyoneGroup.getInstance());
     }
 
     public PageNode(final VirtualHost virtualHost, final Node parentNode, final Page page, final Integer order) {
 	this();
 	init(virtualHost, parentNode, order);
 	setPage(page);
     }
 
     @Override
     public Object getElement() {
 	return getPage();
     }
 
     @Override
     public void delete() {
 	removePage();
 	super.delete();
     }
 
     @Override
     public MultiLanguageString getLink() {
 	final Page page = getPage();
 	return page.getLink();
     }
 
     @Override
    protected void appendUrlPrefix(final StringBuilder stringBuilder) {
	stringBuilder.append("/content.do?method=viewPage");
     }
 
 }
