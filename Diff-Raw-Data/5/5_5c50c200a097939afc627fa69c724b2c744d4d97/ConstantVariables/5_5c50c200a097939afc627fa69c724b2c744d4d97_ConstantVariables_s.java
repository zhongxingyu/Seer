 package de.softwareforge.eyewiki.variable;
 
 import de.softwareforge.eyewiki.WikiContext;
 import de.softwareforge.eyewiki.manager.VariableManager;
 
 /*
  * ========================================================================
  *
  * eyeWiki - a WikiWiki clone written in Java
  *
  * ========================================================================
  *
  * Copyright (C) 2005 Henning Schmiedehausen <henning@software-forge.com>
  *
  * based on
  *
  * JSPWiki - a JSP-based WikiWiki clone.
  * Copyright (C) 2002-2005 Janne Jalkanen (Janne.Jalkanen@iki.fi)
  *
  * ========================================================================
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * ========================================================================
  */
 import org.picocontainer.Startable;
 
 /**
  * DOCUMENT ME!
  *
  * @author $author$
  * @version $Id$
  */
 public class ConstantVariables
         implements Startable
 {
     /** DOCUMENT ME! */
     private final VariableManager variableManager;
 
     /**
      * Creates a new ConstantVariables object.
      *
      * @param variableManager DOCUMENT ME!
      */
     public ConstantVariables(final VariableManager variableManager)
     {
         this.variableManager = variableManager;
     }
 
     /**
      * DOCUMENT ME!
      */
     public synchronized void start()
     {
     }

     /**
      * DOCUMENT ME!
      */
     public synchronized void stop()
     {
         // GNDN
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author $author$
      * @version $Id$
      */
     private static class ConstantVariable
             extends AbstractSimpleVariable
             implements WikiVariable
     {
         /** DOCUMENT ME! */
         private final String value;
 
         /**
          * Creates a new ConstantVariable object.
          *
          * @param value DOCUMENT ME!
          */
         private ConstantVariable(final String value)
         {
             this.value = value;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param context DOCUMENT ME!
          * @param variableName DOCUMENT ME!
          *
          * @return DOCUMENT ME!
          */
         public String getValue(WikiContext context, String variableName)
         {
             return value;
         }
     }
 }
