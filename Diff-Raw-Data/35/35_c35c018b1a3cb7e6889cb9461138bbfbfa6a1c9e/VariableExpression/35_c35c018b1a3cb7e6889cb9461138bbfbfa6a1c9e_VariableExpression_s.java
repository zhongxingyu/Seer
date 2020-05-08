 /*
  * JAHM - Java Advanced Hierarchical Model 
  * 
  * VariableExpression.java
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
 package org.xmodel.xpath.expression;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.xmodel.IChangeSet;
 import org.xmodel.IModelObject;
 import org.xmodel.IModelObjectFactory;
import org.xmodel.xpath.XPath;
 import org.xmodel.xpath.variable.IVariableListener;
 import org.xmodel.xpath.variable.IVariableScope;
 import org.xmodel.xpath.variable.IVariableSource;
import org.xmodel.xpath.variable.VariableScope;
 
 
 /**
  * An IExpression which implements a variable reference in an expression tree.
  */
 @SuppressWarnings("unchecked")
 public class VariableExpression extends Expression
 {
   /**
    * Create a VariableExpression for the specified variable.
    * @param variable The name of the variable.
    */
   public VariableExpression( String variable)
   {
     this.variable = variable;
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.IExpression#getName()
    */
   public String getName()
   {
     return "variable";
   }
 
   /**
    * Returns the name of the variable.
    * @return Returns the name of the variable.
    */
   public String getVariableName()
   {
     return variable;
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.IExpression#getType()
    */
   public ResultType getType()
   {
     // assumes that the variable is in a non-context scope
     IVariableSource source = getVariableSource();
     if ( source == null) return ResultType.UNDEFINED;
     return source.getVariableType( variable);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.IExpression#getType(org.xmodel.xpath.expression.IContext)
    */
   public ResultType getType( IContext context)
   {
     IVariableSource source = getVariableSource();
     try
     {
       source.addScope( context.getScope());
       return source.getVariableType( variable, context);
     }
     finally
     {
       source.removeScope( context.getScope());
     }
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
    */
   @Override
   public boolean evaluateBoolean( IContext context) throws ExpressionException
   {
     ResultType type = getType( context);
     if ( type == ResultType.UNDEFINED) return false;
     
     IVariableSource source = getVariableSource();
     try
     {
       if ( type == ResultType.BOOLEAN) 
       {
         source.addScope( context.getScope());
         return (Boolean)source.getVariable( variable, context);
       }
       return super.evaluateBoolean( context);
     }
     finally
     {
       source.removeScope( context.getScope());
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
    */
   @Override
   public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
   {
     ResultType type = getType( context);
     if ( type == ResultType.UNDEFINED) return Collections.emptyList();
     
     IVariableSource source = getVariableSource();
     try
     {
       source.addScope( context.getScope());
       if ( type == ResultType.NODES)
       {
         return new ArrayList<IModelObject>( (List<IModelObject>)source.getVariable( variable, context));
       }
       return super.evaluateNodes( context);
     }
     finally
     {
       source.removeScope( context.getScope());
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
    */
   @Override
   public double evaluateNumber( IContext context) throws ExpressionException
   {
     ResultType type = getType( context);
     if ( type == ResultType.UNDEFINED) return 0;
     
     IVariableSource source = getVariableSource();
     try
     {
       source.addScope( context.getScope());
       if ( type == ResultType.NUMBER) 
       {
         Number number = (Number)source.getVariable( variable, context);
         return number.doubleValue();
       }
       return super.evaluateNumber( context);
     }
     finally
     {
       source.removeScope( context.getScope());
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
    */
   @Override
   public String evaluateString( IContext context) throws ExpressionException
   {
     ResultType type = getType( context);
     if ( type == ResultType.UNDEFINED) return "";
     
     IVariableSource source = getVariableSource();
     try
     {
       source.addScope( context.getScope());
       if ( type == ResultType.STRING) 
       {
         Object value = source.getVariable( variable, context);
         if ( value == null) return "";
         return value.toString();
       }
       return super.evaluateString( context);
     }
     finally
     {
       source.removeScope( context.getScope());
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#createSubtree(org.xmodel.xpath.expression.IContext, 
    * org.xmodel.IModelObjectFactory, org.xmodel.IChangeSet)
    */
   @Override
   public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
   {
     if ( context instanceof StatefulContext)
     {
       List<IModelObject> emptyList = Collections.emptyList();
       ((StatefulContext)context).set( variable, emptyList);
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#isAbsolute()
    */
   @Override
   public boolean isAbsolute( IContext context)
   {
     if ( context == null) return true;
     return !context.getScope().isDefined( variable);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#requiresContext()
    */
   @Override
   public boolean requiresOrdinalContext()
   {
     return true;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#bind(org.xmodel.xpath.expression.IContext)
    */
   @Override
   public void bind( IContext context)
   {
     // since scopes are dynamic we must get the scope immediately before binding
     IVariableSource source = getVariableSource();
     try
     {
       source.addScope( context.getScope());
      IVariableScope scope = source.getVariableScope( variable);
      if ( scope == null) throw new IllegalStateException( "Undefined variable: "+variable);
       scope.addListener( variable, context, listener);
       Object value = scope.get( variable, context);
       if ( value instanceof List) installValueListener( context, (List<IModelObject>)value);
     }
     catch( ExpressionException e)
     {
       handleException( this, context, e);
     }
     finally
     {
       source.removeScope( context.getScope());
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#unbind(org.xmodel.xpath.expression.IContext)
    */
   @Override
   public void unbind( IContext context)
   {
     // warning: assuming that the variable scope is the same as when the variable was bound.
     // the variable may be legitimately bound in other scopes so the client must take care
     // to keep the variable assignments sane.
     IVariableSource source = getVariableSource();
     try
     {
       source.addScope( context.getScope());
       IVariableScope scope = source.getVariableScope( variable);
      if ( scope == null) throw new IllegalStateException( "Undefined variable: "+variable);
      scope.removeListener( variable, context, listener);
      Object value = scope.get( variable, context);
      if ( value instanceof List) removeValueListener( context, (List<IModelObject>)value);
     }
     catch( ExpressionException e)
     {
       handleException( this, context, e);
     }
     finally
     {
       source.removeScope( context.getScope());
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.expression.Expression#cloneOne()
    */
   @Override
   protected IExpression cloneOne()
   {
     return new VariableExpression( variable);
   }
 
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
     return "$"+variable;
   }
   
   /**
    * Install a LeafValueListener on the specified nodes.
    * @param context The context.
    * @param nodes The nodes.
    */
   private void installValueListener( IContext context, List<IModelObject> nodes)
   {
     if ( parent.requiresValueNotification( this))
     {
       LeafValueListener listener = new LeafValueListener( this, context);
       for( IModelObject node: nodes) node.addModelListener( listener);    
     }
   }
   
   /**
    * remove the LeafValueListener from the specified nodes.
    * @param context The context.
    * @param nodes The nodes.
    */
   private void removeValueListener( IContext context, List<IModelObject> nodes)
   {
     if ( parent.requiresValueNotification( this))
     {
       for( IModelObject node: nodes) 
       {
         LeafValueListener listener = LeafValueListener.findListener( node, this, context);
         if ( listener != null) node.removeModelListener( listener);
       }
     }
   }
   
   final IVariableListener listener = new IVariableListener() {
     public void notifyAdd( String name, IVariableScope scope, IContext context, List<IModelObject> nodes)
     {
       installValueListener( context, nodes);
       parent.notifyAdd( VariableExpression.this, context, nodes);
     }
     public void notifyRemove( String name, IVariableScope scope, IContext context, List<IModelObject> nodes)
     {
       removeValueListener( context, nodes);
       parent.notifyRemove( VariableExpression.this, context, nodes);
     }
     public void notifyChange( String name, IVariableScope scope, IContext context, Boolean newValue)
     {
       parent.notifyChange( VariableExpression.this, context, newValue);
     }
     public void notifyChange( String name, IVariableScope scope, IContext context, Number newValue, Number oldValue)
     {
       parent.notifyChange( VariableExpression.this, context, newValue.doubleValue(), oldValue.doubleValue());
     }
     public void notifyChange( String name, IVariableScope scope, IContext context, String newValue, String oldValue)
     {
       parent.notifyChange( VariableExpression.this, context, newValue, oldValue);
     }
   };
   
   private String variable;
 }
