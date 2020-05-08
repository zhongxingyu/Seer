 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.function;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.swows.graph.events.DynamicGraph;
 import org.swows.graph.events.DynamicGraphFromGraph;
 import org.swows.node.Skolemizer;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.QueryBuildException;
 import com.hp.hpl.jena.sparql.ARQInternalErrorException;
 import com.hp.hpl.jena.sparql.engine.binding.Binding;
 import com.hp.hpl.jena.sparql.expr.Expr;
 import com.hp.hpl.jena.sparql.expr.ExprEvalException;
 import com.hp.hpl.jena.sparql.expr.ExprList;
 import com.hp.hpl.jena.sparql.expr.NodeValue;
 import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
 import com.hp.hpl.jena.sparql.function.Function;
 import com.hp.hpl.jena.sparql.function.FunctionEnv;
 import com.hp.hpl.jena.sparql.util.Utils;
 
 public abstract class GraphReturningFunction implements Function {
 
 	public abstract int getMinArgNum();
 	public abstract int getMaxArgNum();
     public abstract Graph exec(List<NodeValue> args) ;
 	
     private String wantedArgNum = null;
     
     public String getWantedArgNum() {
     	if (wantedArgNum == null)
     		wantedArgNum =
     				( getMinArgNum() < 0 )
     					? "less than " + getMaxArgNum()
     					: ( ( getMaxArgNum() < 0 )
     							? "more than " + getMinArgNum()
     							: ( ( getMaxArgNum() == getMinArgNum() )
     									? "" + getMinArgNum()
     									: "between " + getMinArgNum() + " and " + getMaxArgNum() ) );
     	return wantedArgNum;
     }
 
 	public Graph exec(
 			List<NodeValue> params,
 			FunctionEnv env) {
 		return exec(params);
 	}
 
     @Override
 	public NodeValue exec(
 			Binding binding,
             ExprList args,
             String uri,
             FunctionEnv env) {
 
         //this.env = env ;
         
 		System.out.println("Executing a graph returning function...");
 
 		if ( args == null )
             // The contract on the function interface is that this should not happen.
             throw new ARQInternalErrorException(Utils.className(this)+": Null args list") ;
         
         if ( args.size() < getMinArgNum() || ( getMaxArgNum() >=0 && args.size() > getMaxArgNum() ) ) {
             throw new ExprEvalException(Utils.className(this)+": Wrong number of arguments: Wanted " + getWantedArgNum() + ", got "+args.size()) ;
         }
         
         List<NodeValue> evalArgs = new ArrayList<NodeValue>() ;
         for ( Iterator<Expr> iter = args.iterator() ; iter.hasNext() ; )
         {
             Expr e = iter.next() ;
             NodeValue x = e.eval(binding, env) ;
             evalArgs.add(x) ;
         }
         
 //        Graph newGraph = exec(evalArgs) ;
         DynamicGraph newGraph =
         		new DynamicGraphFromGraph( exec(evalArgs, env) );
 		System.out.println("Graph created: " + newGraph);
         Node graphName = Skolemizer.getInstance().getNode(env, evalArgs);
         // TODO find another way to add this graph!!!!
         env.getDataset().addGraph(graphName, newGraph);
         //arguments = null ;
 		System.out.println("Graph returning function executed and returning " + graphName);
        return new NodeValueNode(graphName);
 		
 	}
 	
     public void checkBuild(String uri, ExprList args)
     { 
         if ( args.size() < getMinArgNum() || ( getMaxArgNum() >=0 && args.size() > getMaxArgNum() ) )
             throw new QueryBuildException("Function '"+Utils.className(this)+"' takes " + getWantedArgNum() + " arguments") ;
     }
 
 	@Override
 	public void build(String uri, ExprList args) {
         //this.uri = uri ;
         //arguments = args ;
         checkBuild(uri, args) ;
 	}
 
 }
