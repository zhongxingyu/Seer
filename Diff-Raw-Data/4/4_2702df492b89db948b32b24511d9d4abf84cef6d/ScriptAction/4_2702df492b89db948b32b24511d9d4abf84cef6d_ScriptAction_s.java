 /*
  * JAHM - Java Advanced Hierarchical Model 
  * 
  * ScriptAction.java
  * 
  * Copyright 2009 Robert Arvin Dunnagan
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
 package org.xmodel.xaction;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.xmodel.IModelObject;
 import org.xmodel.xpath.expression.IContext;
 
 
 /**
  * A toplevel action which executes all of its children until one of them fails.
  */
 public class ScriptAction extends GuardedAction
 {
   public ScriptAction()
   {
     ignore = new HashSet<String>();
     ignore( defaultIgnore);
   }
   
   /**
    * Create a ScriptAction which will execute the children of the specified element.
    * @param loader The class loader.
    * @param element The root of the script.
    */
   public ScriptAction( ClassLoader loader, IModelObject element)
   {
     this( loader, element, new String[ 0]);
   }
 
   /**
    * Create a ScriptAction which will execute the children of the specified element except those in the ignore array.
    * @param loader The class loader.
    * @param root The root of the script.
    * @param ignore The element names to ignore.
    */
   public ScriptAction( ClassLoader loader, IModelObject root, String... ignore)
   {
     if ( root != null)
     {
       XActionDocument document = new XActionDocument( loader);
       document.setRoot( root);
       
       this.ignore = new HashSet<String>();
       ignore( defaultIgnore);
       ignore( ignore);
 
       configure( document);
     }
   }
   
   /**
    * Ignore elements with the specified names when creating actions.
    * @param ignore The list of names to ignore.
    */
   public void ignore( String... ignore)
   {
     for( int i=0; i<ignore.length; i++) 
       this.ignore.add( ignore[ i]);
   }
   
   /**
    * Returns the actions in the script.
    * @return Returns the actions in the script.
    */
   public List<IXAction> getActions()
   {
     if ( actions == null) return Collections.emptyList();
     return actions;
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xaction.XAction#configure(org.xmodel.xaction.XActionDocument)
    */
   @Override
   public void configure( XActionDocument document)
   {
     super.configure( document);
     
     actions = new ArrayList<IXAction>();
     for( IModelObject element: document.getRoot().getChildren())
     {
       if ( !ignore.contains( element.getType()))
       {
         IXAction action = document.getAction( element);
         if ( action != null) actions.add( action);
       }
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
    */
   @Override
   public Object[] doAction( IContext context)
   {
     if ( actions == null || actions.size() == 0) return null;
     
     for( IXAction action: actions)
     {
       Object[] result = action.run( context);
       if ( result != null) return result;
     }
     
     return null;
   }
   
  private final static String[] defaultIgnore = {
     "condition",
     "factory",
     "matcher",
     "package",
     "when",
   };
   
   private Set<String> ignore;
   private List<IXAction> actions;
 }
