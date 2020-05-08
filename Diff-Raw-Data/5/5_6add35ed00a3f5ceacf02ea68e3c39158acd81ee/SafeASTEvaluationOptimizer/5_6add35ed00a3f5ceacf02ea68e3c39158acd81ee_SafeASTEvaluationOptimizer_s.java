 /**
  *    Copyright 2013 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.optimizer;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import ru.histone.HistoneException;
 import ru.histone.evaluator.Evaluator;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.parser.AstNodeType;
 
 /**
  * This optimization unit evaluates constant AST branches and replaces them by evaluation result (string constant AST node).
  * Constant AST branches are those, that don't have 'side-effects'. These branches can be evaluated only once and later
  * be treated as strings, because:
  * <ul compact>
  * <li>They don't depend on context variables</li>
  * <li>They don't use any calls (functions)</li>
  * </ul>
  *
  * @author sazonovkirill@gmail.com
  * @author peter@salnikov.cc
  */
 public class SafeASTEvaluationOptimizer extends AbstractASTWalker {
     private final Evaluator evaluator;
 
     public SafeASTEvaluationOptimizer(NodeFactory nodeFactory, Evaluator evaluator) {
         super(nodeFactory);
         this.evaluator = evaluator;
     }
 
     @Override
     protected JsonNode processIf(ArrayNode ast) throws HistoneException {
         int type = ast.get(0).asInt();
 
         if (type > 0) {
             JsonNode evaluated = evaluateAstOnCleanContext(ast);
             return nodeFactory.jsonArray(evaluated);
         }
 
         ArrayNode conditions = (ArrayNode) ast.get(1);
 
         ArrayNode conditionsOut = nodeFactory.jsonArray();
 
         for (JsonNode condition : conditions) {
             JsonNode expression = condition.get(0);
             JsonNode statements = condition.get(1);
 
             if (!isTrueNode(expression) && SafeASTNodesMarker.safeAstNode(expression)) {
                 JsonNode evaluated = evaluateAstOnCleanContext(expression);
                 expression = nodeFactory.jsonArray(evaluated);
             } else {
                 expression = clearSafeFlag(super.processAstNode(expression));
             }
 
             pushContext();
             ArrayNode statementsOut = processAST((ArrayNode) statements);
             popContext();
 
             ArrayNode conditionOut = nodeFactory.jsonArray();
             conditionOut.add(expression);
             conditionOut.add(statementsOut);
             conditionsOut.add(conditionOut);
         }
 
         return nodeFactory.jsonArray(AstNodeType.IF, clearSafeFlag(conditionsOut));
     }
 
     private boolean isTrueNode(JsonNode expression) {
         return expression.isArray() && expression.size() == 1 && expression.get(0).asInt() == AstNodeType.TRUE;
     }
 
     @Override
     protected ArrayNode processAST(ArrayNode ast) throws HistoneException {
         ArrayNode result = nodeFactory.jsonArray();
         for (JsonNode node : ast) {
             if (node.isTextual()) {
                 result.add(node);
             } else if (!node.isArray()) {
                 result.add(node);
             } else if (node.size() == 0) {
                 // We also skip empty arrays
                 result.add(node);
             } else {
                 int type = getNodeType((ArrayNode) node);
                 if (type > 0) {
                     JsonNode evaluated = evaluateAstOnCleanContext(node);
                     result.add(nodeFactory.jsonArray(evaluated));
                 } else {
                     JsonNode processedNode = processAstNode(node);
                     result.add(processedNode);
                 }
             }
         }
         return result;
     }
 
     public JsonNode evaluateAstOnCleanContext(JsonNode ast) throws HistoneException {
         Node node = evaluator.evaluate(ast);
         return nodeFactory.jsonString(node.getAsString().getValue());
     }
 
     @Override
     public void pushContext() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void popContext() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     private JsonNode clearSafeFlag(JsonNode ast) {
         if (ast.isArray() && ast.size() > 0 && ast.get(0).isInt()) {
             ArrayNode arr = (ArrayNode) ast;
             int value = arr.get(0).asInt();
             if (value < 0) {
                 arr.remove(0);
                 arr.insert(0, -value);
             }
         }
 
         for (JsonNode node : ast) {
             clearSafeFlag(node);
         }
 
         return ast;
     }
 }
