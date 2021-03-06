 package com.mysema.rdfbean.model;
 
 import java.util.List;
 import java.util.Stack;
 
 import javax.annotation.Nullable;
 
 import com.mysema.query.QueryMetadata;
 import com.mysema.query.QueryModifiers;
 import com.mysema.query.support.SerializerBase;
 import com.mysema.query.types.Constant;
 import com.mysema.query.types.Expression;
 import com.mysema.query.types.Operator;
 import com.mysema.query.types.Ops;
 import com.mysema.query.types.OrderSpecifier;
 import com.mysema.query.types.Predicate;
 import com.mysema.query.types.SubQueryExpression;
 
 /**
  * @author tiwe
  *
  */
 public class SPARQLVisitor extends SerializerBase<SPARQLVisitor> implements RDFVisitor<Void,Void>{
     
     @Nullable
     private PatternBlock lastPattern;
 
     private final String prefix;
     
     private final Stack<Operator<?>> operators = new Stack<Operator<?>>();
     
     public SPARQLVisitor() {
         this(SPARQLTemplates.DEFAULT, "");
     }
     
     public SPARQLVisitor(SPARQLTemplates templates, String prefix) {
         super(templates);
         this.prefix = prefix;
         setParamPrefix("?");
     }    
     
    @Nullable
     public Void visit(QueryMetadata md, QueryLanguage<?,?> queryType) {
         QueryModifiers mod = md.getModifiers();
         append(prefix);
         // select
         if (queryType == QueryLanguage.TUPLE){
             append("SELECT ");
             if (md.isDistinct()){
                 append("DISTINCT ");
             }
             handle(" ", md.getProjection());
             append("\n");            
             
         // ask
         }else if (queryType == QueryLanguage.BOOLEAN){
             append("ASK ");
             
         // construct
         }else if (queryType == QueryLanguage.GRAPH){
             if (md.getProjection().size() == 1 && md.getProjection().get(0) instanceof GroupBlock){
                 append("CONSTRUCT ").handle("", md.getProjection()).append("\n");
             }else{
                 append("CONSTRUCT { ").handle("", md.getProjection()).append("}\n");    
             }            
         }
         lastPattern = null;
         
         // where
         if (md.getWhere() != null){
             if (queryType != QueryLanguage.BOOLEAN){
                 append("WHERE \n  ");    
             }            
             if (md.getWhere() instanceof GroupBlock){
                 handle(md.getWhere());                
             }else{
                 append("{ ").handle(md.getWhere()).append("}");    
             }
             append("\n");                
         }      
         
         // order
         if (!md.getOrderBy().isEmpty()){
             append("ORDER BY ");
             boolean first = true;            
             for (OrderSpecifier<?> order : md.getOrderBy()){
                 if (!first){
                     append(" ");                    
                 }
                 if (order.isAscending()){
                     handle(order.getTarget());
                 }else{
                     append("DESC(").handle(order.getTarget()).append(")");
                 }
                 first = false;
             }
             append("\n");
         }
         
         // group by
         if (!md.getGroupBy().isEmpty()){
             append("GROUP BY ").handle(" ", md.getGroupBy()).append("\n");
         }
         
         // having
         if (md.getHaving() != null){
             append("HAVING (").handle(md.getHaving()).append(")\n");            
         }    
         
         // limit        
         if (mod.getLimit() != null){
             append("LIMIT ").append(mod.getLimit().toString()).append("\n");
         }
         
         // offset
         if (mod.getOffset() != null){
             append("OFFSET ").append(mod.getOffset().toString()).append("\n");
         }
         
         return null;
 
     }
     
     @Override
     public Void visit(SubQueryExpression<?> expr, Void context) {
         if (!operators.isEmpty() && operators.peek() == Ops.EXISTS){
             handle(expr.getMetadata().getWhere());
         }else{
             visit(expr.getMetadata(), QueryLanguage.TUPLE);    
         }
         
         return null;
     }
 
     @Nullable
     public Void visit(UnionBlock expr, @Nullable Void context) {
         lastPattern = null;
         boolean first = true;
         for (Block block : expr.getBlocks()){
             if (!first){
                 append("UNION ");
             }
             if (block instanceof PatternBlock){
                 append("{ ").handle(block).append("} ");
             }else{
                 handle(block);
             }
             lastPattern = null;
             first = false;
         }
         return null;
     }
 
     @Nullable
     public Void visit(GroupBlock expr, @Nullable Void context) {
         lastPattern = null;
         append("{ ");
         visitBlocks(expr.getBlocks());
         visitFilter(expr.getFilters());
         append("} ");
         lastPattern = null;
         return null;
     }
 
     @Nullable
     public Void visit(GraphBlock expr, @Nullable Void context) {
         lastPattern = null;
         append("GRAPH ").handle(expr.getContext()).append(" { ");
         visitBlocks(expr.getBlocks());
         visitFilter(expr.getFilters());
         append("} ");
         lastPattern = null;
         return null;
     }
 
     @Nullable
     public Void visit(OptionalBlock expr, @Nullable Void context) {
         lastPattern = null;
         append("OPTIONAL {");
         visitBlocks(expr.getBlocks());
         visitFilter(expr.getFilters());
         append("} ");
         lastPattern = null;
         return null;
     }
     
     private void visitBlocks(List<Block> blocks){
         for (Block block : blocks){
             if (lastPattern != null && !(block instanceof PatternBlock)){
                 append(".\n  ");
                 lastPattern = null;
             }
             handle(block);
         }
     }
     
     private void visitFilter(@Nullable Predicate filter){        
         if (filter != null){
             if (lastPattern != null){
                 append(". ");
             }
             lastPattern = null;
             append("FILTER(").handle(filter).append(") ");    
         }        
         lastPattern = null;
     }
     
     @Override
     public Void visit(Constant<?> expr, Void context) {
         if (expr.getConstant() instanceof QueryMetadata){
             QueryMetadata md = (QueryMetadata)expr.getConstant();
             handle(md.getWhere());
         }else if (expr.getConstant() instanceof Block){
             handle((Expression<?>)expr.getConstant());
         }else if (!getConstantToLabel().containsKey(expr.getConstant())) {
             String constLabel = "_c" + (getConstantToLabel().size() + 1);
             getConstantToLabel().put(expr.getConstant(), constLabel);
             append("?" + constLabel);
         } else {
             append("?" + getConstantToLabel().get(expr.getConstant()));
         }
         return null;
     }
 
     @Nullable
     public Void visit(PatternBlock expr, @Nullable Void context) {
         if (lastPattern == null || !lastPattern.getSubject().equals(expr.getSubject())){
             if (lastPattern != null){
                 append(".\n  ");
             }
             handle(expr.getSubject()).append(" ");
             handle(expr.getPredicate()).append(" ");
             
         }else if (!lastPattern.getPredicate().equals(expr.getPredicate())){
             append("; ");
             handle(expr.getPredicate()).append(" ");
             
         }else{
             append(", ");
         }
         
         handle(expr.getObject()).append(" ");
         lastPattern = expr;
         return null;
     }
     
     @Override
     protected void visitOperation(Class<?> type, Operator<?> operator, List<Expression<?>> args) {
         operators.push(operator);
         try{
             super.visitOperation(type, operator, args);
         }finally{
             operators.pop();
         }
     }
         
 }
