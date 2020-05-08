 /*
  * JAHM - Java Advanced Hierarchical Model 
  * 
  * AbstractVariableScope.java
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
 package org.xmodel.xpath.variable;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.xmodel.IModel;
 import org.xmodel.IModelObject;
 import org.xmodel.ModelRegistry;
 import org.xmodel.Update;
 import org.xmodel.memento.IMemento;
 import org.xmodel.memento.VariableMemento;
 import org.xmodel.xpath.expression.ExpressionException;
 import org.xmodel.xpath.expression.ExpressionListener;
 import org.xmodel.xpath.expression.IContext;
 import org.xmodel.xpath.expression.IExpression;
 import org.xmodel.xpath.expression.IExpression.ResultType;
 import org.xmodel.xpath.expression.IExpressionListener;
 import org.xmodel.xpath.expression.RootExpression;
 
 /**
  * A base implementation of IVariableScope which does not define the scope name or precedence.
  */
 @SuppressWarnings("unchecked")
 public abstract class AbstractVariableScope implements IVariableScope
 {
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#internal_setSource(org.xmodel.xpath.variable.IVariableSource)
    */
   public void internal_setSource( IVariableSource source)
   {
     this.source = source;
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#getSource()
    */
   public IVariableSource getSource()
   {
     return source;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#getVariables()
    */
   @Override
   public Collection<String> getVariables()
   {
     if ( variables == null) return Collections.emptyList();
     return variables.keySet();
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.Object)
    */
   @Override
   public Object set( String name, Object value)
   {
     return internal_set( name, value);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#insert(java.lang.String, java.lang.Object)
    */
   @Override
   public void insert( String name, Object object)
   {
     insert( name, object, Integer.MAX_VALUE);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#insert(java.lang.String, java.lang.Object, int)
    */
   @Override
   public void insert( String name, Object object, int index)
   {
     Variable variable = getCreateVariable( name);
     if ( variable.value == null) variable.value = new ArrayList<IModelObject>();
     if ( !(variable.value instanceof List)) throw new IllegalStateException( "Variable does not contain a sequence.");
     
     List<Object> list = (List<Object>)variable.value;
     if ( index == Integer.MAX_VALUE) index = list.size();
     
     if ( !(list instanceof ArrayList))
     {
       list = new ArrayList<Object>();
       variable.value = list;
     }
     
     list.add( index, object);
 
     List<IModelObject> inserted = Collections.singletonList( (IModelObject)object);
     if ( variable.bindings != null)
     {
       Binding[] bindings = variable.bindings.toArray( new Binding[ 0]);
       for( Binding binding: bindings)
       {
         binding.listener.notifyAdd( name, this, binding.context, inserted);
       }
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#remove(java.lang.String, java.lang.Object)
    */
   @Override
   public void remove( String name, Object object)
   {
     Variable variable = variables.get( name);
     if ( variable == null) return;
     if ( !(variable.value instanceof List)) throw new IllegalStateException( "Variable does not contain a sequence.");
     
     List<IModelObject> removed = Collections.singletonList( (IModelObject)object);
     if ( variable.bindings != null)
     {
       Binding[] bindings = variable.bindings.toArray( new Binding[ 0]);
       for( Binding binding: bindings)
       {
         binding.listener.notifyRemove( name, this, binding.context, removed);
       }
     }
     
     List<Object> list = (List<Object>)variable.value;
     list.remove( object);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#remove(java.lang.String, int)
    */
   @Override
   public void remove( String name, int index)
   {
     Variable variable = variables.get( name);
     if ( variable == null) return;
     if ( !(variable.value instanceof List)) throw new IllegalStateException( "Variable does not contain a sequence.");
     
     List<Object> list = (List<Object>)variable.value;
     Object object = list.remove( index);
 
     List<IModelObject> removed = Collections.singletonList( (IModelObject)object);
     if ( variable.bindings != null)
     {
       Binding[] bindings = variable.bindings.toArray( new Binding[ 0]);
       for( Binding binding: bindings)
       {
         binding.listener.notifyRemove( name, this, binding.context, removed);
       }
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, org.xmodel.IModelObject)
    */
   public List<IModelObject> set( String name, IModelObject value)
   {
     List<? extends Object> list = (value != null)? 
       Collections.singletonList( value):
       Collections.emptyList();
       
     Object old = internal_set( name, list);
     if ( old instanceof List) return (List<IModelObject>)old;
     return Collections.emptyList();
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.util.List)
    */
   public List<IModelObject> set( String name, List<IModelObject> value)
   {
     Object old = internal_set( name, value);
     if ( old instanceof List) return (List<IModelObject>)old;
     return Collections.emptyList();
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.Boolean)
    */
   public Boolean set( String name, Boolean value)
   {
     Object old = internal_set( name, value);
     if ( old instanceof Boolean) return (Boolean)old;
     return Boolean.FALSE;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.Number)
    */
   public Number set( String name, Number value)
   {
     Object old = internal_set( name, value);
     if ( old instanceof Number) return (Number)old;
     return 0;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#set(java.lang.String, java.lang.String)
    */
   public String set( String name, String value)
   {
     Object old = internal_set( name, value);
     if ( old instanceof String) return (String)old;
     return "";
   }
 
   /**
    * Generic variable assignment.
    * @param name The name of the variable.
    * @param value The value of the variable.
    * @return Returns the old value.
    */
   protected Object internal_set( String name, Object value)
   {
     Variable variable = getCreateVariable( name);
     if ( variable.value == null)
     {
       variable.value = value;
       return null;
     }
     else if ( variable.value != value)
     {
       Object oldValue = variable.value;
       variable.value = value;
       performNotification( variable, name, value, oldValue);
       return oldValue;
     }
     return value;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#define(java.lang.String, 
    * org.xmodel.xpath.expression.IExpression)
    */
   public void define( String name, IExpression expression)
   {
     if ( variables != null)
     {
       // variable cannot already be defined
       Variable variable = variables.get( name);
       if ( variable != null && variable.bindings != null && variable.bindings.size() > 0)
         throw new IllegalArgumentException( 
           "$"+name+" already defined in scope:\n"+toString());
       
       // expression cannot be assigned to another variable in this scope
       for( Map.Entry<String, Variable> entry: variables.entrySet())
         if ( entry.getValue() == expression)
           throw new IllegalArgumentException(
             "Expression is referenced by another variable ($"+entry.getKey()+") in scope:\n"+toString());
     }
       
     // must be RootExpression
     if ( !(expression instanceof RootExpression))
       throw new IllegalArgumentException( "Assigned expression must be a RootExpression instance.");
 
     // set the parent of the IVariableSource of the assigned expression
     IVariableSource assignedSource = expression.getVariableSource();
     if ( assignedSource != source) assignedSource.setParent( source);
     
     // define
     Variable variable = getCreateVariable( name);
     variable.value = expression;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#copyFrom(org.xmodel.xpath.variable.IVariableScope)
    */
   public void copyFrom( IVariableScope scope)
   {
     for( String name: scope.getAll())
       internal_set( name, scope.get( name));
   }
 
   /**
    * Start update and perform notification.
    * @param variable A variable assignment.
    * @param newValue The new value.
    * @param oldValue The old value.
    */
   private void performNotification( Variable variable, String name, Object newValue, Object oldValue)
   {
     if ( variable.bindings == null) return;
     
     // clone binding list for iteration
     Binding[] bindings = new Binding[ variable.bindings.size()];
     for( int i=0; i<variable.bindings.size(); i++)
       bindings[ i] = variable.bindings.get( i);
 
     // update and notify
     IModel model = ModelRegistry.getInstance().getModel();
     Update update = model.startUpdate();
     update.setVariable( this, name, newValue, oldValue);
     try
     {
       if ( newValue instanceof List)
       {
         List<IModelObject> newNodes = (List<IModelObject>)newValue;
         List<IModelObject> oldNodes = (List<IModelObject>)oldValue;
         
         // notify nodes removed
         List<IModelObject> removedSet = new ArrayList<IModelObject>( newNodes.size());
         for( IModelObject node: oldNodes) if ( !newNodes.contains( node)) removedSet.add( node);
         if ( removedSet.size() > 0)
           for( Binding binding: bindings)
             binding.listener.notifyRemove( name, this, binding.context, removedSet);
         
         // notify nodes added
         List<IModelObject> addedSet = new ArrayList<IModelObject>( newNodes.size());
         for( IModelObject node: newNodes) if ( !oldNodes.contains( node)) addedSet.add( node);
         if ( addedSet.size() > 0)
           for( Binding binding: bindings)
             binding.listener.notifyAdd( name, this, binding.context, addedSet);
       }
       else if ( newValue instanceof String)
       {
         for( Binding binding: bindings)
           binding.listener.notifyChange( name, this, binding.context, (String)newValue, (String)oldValue);
       } 
       else if ( newValue instanceof Number)
       {
         for( Binding binding: bindings)
           binding.listener.notifyChange( name, this, binding.context, (Number)newValue, (Number)oldValue);
       }
       else if ( newValue instanceof Boolean)
       {
         for( Binding binding: bindings)
           binding.listener.notifyChange( name, this, binding.context, (Boolean)newValue);
       }
     }
     finally
     {
       model.endUpdate();
     }
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#get(java.lang.String)
    */
   public Object get( String name)
   {
     if ( variables == null) return null;
     Variable variable = variables.get( name);
     if ( variable == null) return null;
     return variable.value;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#get(java.lang.String, 
    * org.xmodel.xpath.expression.IContext)
    */
   public Object get( String name, IContext context) throws ExpressionException
   {
     if ( variables == null) return null;
     
     Variable variable = variables.get( name);
     if ( variable == null) return null;
     
     if ( variable.value instanceof IExpression)
     {
       IExpression expression = (IExpression)variable.value;
       if ( expression != null)
       {
         switch( expression.getType( context))
         {
           case NODES:   return expression.evaluateNodes( context);
           case NUMBER:  return expression.evaluateNumber( context);
           case STRING:  return expression.evaluateString( context);
           case BOOLEAN: return expression.evaluateBoolean( context);
         }
       }
     }
     else
     {
       return variable.value;
     }
     
     return null;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#getAll()
    */
   public Collection<String> getAll()
   {
     if ( variables == null) return Collections.emptyList();
     return variables.keySet();
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#isDefined(java.lang.String)
    */
   public boolean isDefined( String name)
   {
     if ( variables == null) return false;
     Variable variable = variables.get( name);
     return (variable != null);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#isBound(java.lang.String)
    */
   public boolean isBound( String name)
   {
     if ( variables == null) return false;
     Variable variable = variables.get( name);
     if ( variable != null) return (variable.bindings != null && variable.bindings.size() > 0);
     return false;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#getType(java.lang.String)
    */
   public ResultType getType( String name)
   {
     return getType( name, null);
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#getType(java.lang.String, org.xmodel.xpath.expression.IContext)
    */
   public ResultType getType( String name, IContext context)
   {
     if ( variables == null) return ResultType.UNDEFINED;
     
     Variable variable = variables.get( name);
     if ( variable == null) return ResultType.UNDEFINED;
     
     Object value = variable.value;
     return getType( value, context);
   }
 
   /**
    * Returns the ResultType of the specified object.
    * @param object The object.
    * @param context An optional context for resolving expression variables.
    * @return Returns the ResultType of the specified object.
    */
   public static ResultType getType( Object value, IContext context)
   {
     if ( value instanceof List) return ResultType.NODES;
     if ( value instanceof Number) return ResultType.NUMBER;
     if ( value instanceof Boolean) return ResultType.BOOLEAN;
     if ( value instanceof IExpression) 
     {
       if ( context != null) return ((IExpression)value).getType( context);
       return ((IExpression)value).getType();
     }
     return ResultType.STRING;
   }
 
   /**
    * Get and/or create the specified variable.
    * @param name The name of the variable.
    * @return Returns the Literal object.
    */
   protected Variable getCreateVariable( String name)
   {
     if ( variables == null) variables = new HashMap<String, Variable>( 3);
     Variable variable = variables.get( name);
     if ( variable == null)
     {
       variable = new Variable();
       variable.name = name;
       variables.put( name, variable);
     }
     return variable;
   }
 
   /**
    * Returns the variable entry assigned with the specified expression.
    * @param expression The expression.
    * @return Returns the variable entry assigned with the specified expression.
    */
   protected Variable getVariable( IExpression expression)
   {
     for( Map.Entry<String, Variable> entry: variables.entrySet())
       if ( entry.getValue().value == expression)
         return entry.getValue();
     return null;
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#addListener(java.lang.String, 
    * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.variable.IVariableListener)
    */
   public void addListener( String name, IContext context, IVariableListener listener)
   {
     Variable variable = (variables != null)? variables.get( name): null;
     if ( variable == null) 
       throw new IllegalArgumentException( "$"+name+" is not defined in scope:\n"+toString());
     
     // add variable binding
     variable.addBinding( new Binding( context, listener));
     
     // bind expression
     if ( variable.value instanceof IExpression)
     {
       IExpression expression = (IExpression)variable.value;
       expression.addListener( context, expressionListener);
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#removeListener(java.lang.String, 
    * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.variable.IVariableListener)
    */
   public void removeListener( String name, IContext context, IVariableListener listener)
   {
     Variable variable = variables.get( name);
     if ( variable == null) 
       throw new IllegalArgumentException( "$"+name+" is not defined in scope:\n"+toString());
     
     // remove variable binding
     if ( variable.removeBinding( context, listener))
     {
       // unbind expression
       if ( variable.value instanceof IExpression)
       {
         IExpression expression = (IExpression)variable.value;
         expression.removeListener( context, expressionListener);
       }
     }
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#revert(org.xmodel.memento.IMemento)
    */
   public void revert( IMemento iMemento)
   {
     VariableMemento memento = (VariableMemento)iMemento;
     Variable variable = variables.get( memento.varName);
     if ( variable != null) variable.value = memento.oldValue;
   }
 
   /* (non-Javadoc)
    * @see org.xmodel.xpath.variable.IVariableScope#restore(org.xmodel.memento.IMemento)
    */
   public void restore( IMemento iMemento)
   {
     VariableMemento memento = (VariableMemento)iMemento;
     Variable variable = variables.get( memento.varName);
     if ( variable != null) variable.value = memento.newValue;
   }
   
   /**
    * Find the bindings associated with the specified context.
    * @param variable The variable whose bindings will be searched.
    * @param context The context.
    * @return Returns the bindings associated with the specified context.
    */
   private List<Binding> findBindings( Variable variable, IContext context)
   {
     List<Binding> result = new ArrayList<Binding>();
     for( Binding binding: variable.bindings)
       if ( binding.context == context)
         result.add( binding);
     return result;
   }
 
   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
     StringBuilder sb = new StringBuilder();
     if ( variables != null)
     {
       List<String> names = new ArrayList<String>( getAll());
       Collections.sort( names);
       for( String name: names)
       {
         sb.append( name);
         sb.append( "=");
         Object value = get( name);
         if ( value instanceof IExpression)
         {
           sb.append( "{");
           sb.append( value);
           sb.append( "}");
         }
         else if ( value instanceof List)
         {
           sb.append( value);
         }
         else if ( value != null)
         {
           sb.append( "'");
           sb.append( value);
           sb.append( "'");
         }
         else
         {
           sb.append( "null, ");
         }
         sb.append( "\n");
       }
     }
     return sb.toString();
   }
 
   final IExpressionListener expressionListener = new ExpressionListener() {
     public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
     {
       Variable variable = getVariable( expression);
       for( Binding binding: findBindings( variable, context))
         binding.listener.notifyAdd( variable.name, AbstractVariableScope.this, context, nodes);
     }
     public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
     {
       Variable variable = getVariable( expression);
       for( Binding binding: findBindings( variable, context))
         binding.listener.notifyRemove( variable.name, AbstractVariableScope.this, context, nodes);
     }
     public void notifyChange( IExpression expression, IContext context, boolean newValue)
     {
       Variable variable = getVariable( expression);
       for( Binding binding: findBindings( variable, context))
         binding.listener.notifyChange( variable.name, AbstractVariableScope.this, context, newValue);
     }
     public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
     {
       Variable variable = getVariable( expression);
       for( Binding binding: findBindings( variable, context))
         binding.listener.notifyChange( variable.name, AbstractVariableScope.this, context, newValue, oldValue);
     }
     public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
     {
       Variable variable = getVariable( expression);
       for( Binding binding: findBindings( variable, context))
         binding.listener.notifyChange( variable.name, AbstractVariableScope.this, context, newValue, oldValue);
     }
     public boolean requiresValueNotification()
     {
       return false;
     }
   };
   
   protected class Variable
   {
     public void addBinding( Binding binding)
     {
       if ( bindings == null) bindings = new ArrayList<Binding>( 1);
       bindings.add( binding);
     }
     
     public boolean removeBinding( IContext context, IVariableListener listener)
     {
       // remove should be quiet when there is nothing to remove even though this means
       // that framework errors having to do with the installation and removal of scopes
       // will not be caught.
       if ( bindings != null)
       {
         for( Binding binding: bindings)
           if ( binding.context.equals( context) && binding.listener.equals( listener))
           {
             bindings.remove( binding);
             return true;
           }
       }
       return false;
     }
     
     public String name;
     public Object value;
     public List<Binding> bindings;
   }
   
   protected class Binding
   {
     public Binding( IContext context, IVariableListener listener)
     {
       this.context = context;
       this.listener = listener;
     }
     
     public IContext context;
     public IVariableListener listener;
   }
 
   private IVariableSource source;
   protected Map<String, Variable> variables;
 }
