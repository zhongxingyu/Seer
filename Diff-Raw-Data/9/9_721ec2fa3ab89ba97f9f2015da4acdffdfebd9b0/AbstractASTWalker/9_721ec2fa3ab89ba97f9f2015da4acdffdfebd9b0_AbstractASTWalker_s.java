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
 import ru.histone.evaluator.nodes.AstNode;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.parser.AstNodeType;
 import ru.histone.utils.Assert;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Provides basic functionality for optimization units:
  * <ul compact>
  * <li>Recursive traversing of AST tree</li>
  * <li>Lists of types of AST operations ({@link #BINARY_OPERATIONS}, {@link #UNARY_OPERATIONS}, {@link #TERNARY_OPERATIONS}, {@link #CONSTANTS})</li>
  * <li>Some help functions</li>
  * </ul>
  * <p/>
  * <p>Subclasses have to implement {@link #pushContext()} and {@link #popContext()} for context operations support.
  * Subclasses have to define their own logic of AST nodes processing by overwriting several of following methods:
  * <ul compact>
  * <li>{@link #processCall(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processFor(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processIf(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processImport(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processMacro(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processMap(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processOperationOverArguments(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processSelector(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processStatements(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * <li>{@link #processVariable(com.fasterxml.jackson.databind.node.ArrayNode)}</li>
  * </ul>
  * and then start AST processing by invoking (once or several times) {@link #process(com.fasterxml.jackson.databind.node.ArrayNode)} method.
  * <p/>
  * <p>The most controversial method is {@link #node2Ast(ru.histone.evaluator.nodes.Node)}, that is used for backward translation from Histone {@link Node} entity
  * to AST {@code JsonNode}. This capability is used in evaluator of constant AST branches ({@link SafeASTEvaluationOptimizer}).
  * <p/>
  * User: sazonovkirill@gmail.com
  * Date: 08.01.13
  */
 public abstract class AbstractASTWalker {
     protected final NodeFactory nodeFactory;
 
     /**
      * Removes HISTONE signature and returns AST tree:
      * {@code [[Histone signature here][Actual AST tree here]]}
      */
     protected static ArrayNode removeHistoneAstSignature(ArrayNode ast) {
         if (ast.isArray() &&
                 ast.size() == 2 &&
                 ast.get(0).isArray() &&
                 ast.get(1).isArray() &&
                 ast.get(0).size() > 0 &&
                 ast.get(0).get(0).isTextual() &&
                 "HISTONE".equals(ast.get(0).get(0).asText())) {
             // If signature exists
             return (ArrayNode) ast.get(1);
         } else {
             // Otherwise there is no signature, and it's an actual AST
             return ast;
         }
     }
 
     /**
      * Base function for start recursive processing of AST nodes.
      *
      * @param ast input array of AST nodes
      * @return processed AST
      */
     protected ArrayNode processAST(ArrayNode ast) throws HistoneException {
         ast = removeHistoneAstSignature(ast);
 
         ArrayNode result = nodeFactory.jsonArray();
         for (JsonNode node : ast) {
             JsonNode processedNode = processAstNode(node);
             result.add(processedNode);
         }
         return result;
     }
 
     /**
      * Process node as array of AST nodes if it's an array. Otherwise call {@link #processAstNode(com.fasterxml.jackson.databind.JsonNode)}.
      *
      * @return an {@link ArrayNode}, if was processed as array.
      */
     protected JsonNode processArrayOfAstNodes(JsonNode node) throws HistoneException {
         if (node.isArray()) {
             JsonNode[] result = new JsonNode[node.size()];
             for (int i = 0; i < result.length; i++) {
                 result[i] = processAstNode(node.get(i));
             }
             return nodeFactory.jsonArray(result);
         } else {
             return processAstNode(node);
         }
     }
 
     /**
      * Process AST node.
      * <p/>
      * <p>Node can be:
      * <ul>
      * <li>Test nodes (e.g. fragments)</li>
      * <li>AST node, a node that have following structure: [[<operation code from {@link AstNodeType}>], <AST node params> ]</li>
      * </ul>
      *
      * @param node
      * @return
      * @throws HistoneException
      */
     protected JsonNode processAstNode(JsonNode node) throws HistoneException {
         // All text nodes are returned 'as it'
         if (node.isTextual()) {
             return node;
         }
 
         // If this node is not array, so it NOT AST node and we return it 'as is'
         if (!node.isArray()) {
             return node;
         }
         ArrayNode arr = (ArrayNode) node;
 
         // We also skip empty arrays
         if (arr.size() == 0) {
             return arr;
         }
 
         int nodeType = Math.abs(getNodeType(arr));
 
         // Processing of AST node types, that are operations over number of arguments
         if (getOperationsOverArguments().contains(nodeType)) {
             return processOperationOverArguments(arr);
         }
 
         // Processing of AST node types, that are control statements
         switch (nodeType) {
             case AstNodeType.SELECTOR:
                 return processSelector(arr);
             case AstNodeType.STATEMENTS:
                 return processStatements(arr);
             case AstNodeType.IMPORT:
                 return processImport(arr);
 
             case AstNodeType.VAR:
                 return processVariable(arr);
             case AstNodeType.IF:
                 return processIf(arr);
             case AstNodeType.FOR:
                 return processFor(arr);
 
             case AstNodeType.MACRO:
                 return processMacro(arr);
             case AstNodeType.CALL:
                 return processCall(arr);
             case AstNodeType.MAP:
                 return processMap(arr);
             default:
                 return node;
         }
     }
 
 
     //<editor-fold desc="Functions for context support">
 
     /**
      * If a subclass wants to deal with context it's important for handle frame push/pop events (when entering and leaving
      * loops, macros definitions, etc; for this case a subclass has to implement this function for handling push context frame event.
      */
     public abstract void pushContext();
 
     /**
      * If a subclass wants to deal with context it's important for handle frame push/pop events (when entering and leaving
      * loops, macros definitions, etc; for this case a subclass has to implement this function for handling pop context frame event.
      */
     public abstract void popContext();
 
     public ArrayNode process(ArrayNode ast) throws HistoneException{
         return processAST(ast);
     }
     //</editor-fold>
 
     //<editor-fold desc="Recursive processing different types of nodes">
 
     protected JsonNode processCall(ArrayNode call) throws HistoneException {
         int type = call.get(0).asInt();
         JsonNode target = call.get(1);
         JsonNode functionName = call.get(2);
         JsonNode args = call.get(3);
 
         target = processAstNode(target);
         if (args.isArray() && args.size() > 0) {
             args = processArrayOfAstNodes(args);
         }
 
         return nodeFactory.jsonArray(type, target, functionName, args);
     }
 
     protected JsonNode processMap(ArrayNode map) throws HistoneException {
         int type = map.get(0).asInt();
         ArrayNode items = (ArrayNode) map.get(1);
 
         ArrayNode processedItems = nodeFactory.jsonArray();
         for (JsonNode item : items) {
             if (item.isArray()) {
                 ArrayNode arr = (ArrayNode) item;
                 JsonNode key = arr.get(0);
                 JsonNode value = arr.get(1);
 
                 value = processAstNode(value);
                 processedItems.add(nodeFactory.jsonArray(key, value));
             }
         }
 
         return nodeFactory.jsonArray(type, processedItems);
     }
 
     protected JsonNode processOperationOverArguments(ArrayNode ast) throws HistoneException {
         Assert.notNull(ast);
         Assert.notNull(ast.size() > 1);
         Assert.isTrue(ast.get(0).isNumber());
         for (int i = 1; i < ast.size(); i++) Assert.isTrue(ast.get(i).isArray());
         Assert.isTrue(getOperationsOverArguments().contains(Math.abs(ast.get(0).asInt())));
 
         int operationType = ast.get(0).asInt();
 
         JsonNode[] processedArguments = new JsonNode[ast.size() - 1];
         for (int i = 1; i < ast.size(); i++) {
             JsonNode processedArg = processAstNode(ast.get(i));
             Assert.isTrue(processedArg.isArray());
             processedArguments[i - 1] = processedArg;
         }
 
         return nodeFactory.jsonArray(operationType, processedArguments);
     }
 
     protected JsonNode processStatements(ArrayNode statements) throws HistoneException {
         int type = statements.get(0).asInt();
         statements = (ArrayNode) statements.get(1);
 
         JsonNode[] statementsOut = new JsonNode[statements.size()];
         for (int i = 0; i < statements.size(); i++) {
             statementsOut[i] = processAstNode(statements.get(i));
         }
 
         return nodeFactory.jsonArray(type, nodeFactory.jsonArray(statementsOut));
     }
 
     protected JsonNode processImport(ArrayNode import_) throws HistoneException {
         String resource = import_.get(1).asText();
         return import_;
     }
 
     protected JsonNode processVariable(ArrayNode variable) throws HistoneException {
         int type = variable.get(0).asInt();
         Assert.isTrue(variable.size() == 3);
 
         JsonNode var = variable.get(1);
         JsonNode value = variable.get(2);
         JsonNode processedValue = processAstNode(value);
 
         return nodeFactory.jsonArray(type, var, processedValue);
     }
 
     protected JsonNode processMacro(ArrayNode macro) throws HistoneException {
         int type = macro.get(0).asInt();
         JsonNode name = macro.get(1);
         ArrayNode args = (ArrayNode) macro.get(2);
         ArrayNode statements = (ArrayNode) macro.get(3);
 
         pushContext();
         args = (ArrayNode) processAstNode(args);
         statements = (ArrayNode) processArrayOfAstNodes(statements);
         popContext();
 
         return nodeFactory.jsonArray(type, name, args, statements);
     }
 
     protected JsonNode processIf(ArrayNode if_) throws HistoneException {
         int type = if_.get(0).asInt();
         ArrayNode conditions = (ArrayNode) if_.get(1);
 
         ArrayNode conditionsOut = nodeFactory.jsonArray();
 
         for (JsonNode condition : conditions) {
             JsonNode expression = condition.get(0);
             JsonNode statements = condition.get(1);
 
             expression = processAstNode(expression);
 
             pushContext();
             JsonNode[] statementsOut = new JsonNode[statements.size()];
             for (int i = 0; i < statements.size(); i++) {
                 statementsOut[i] = processAstNode(statements.get(i));
             }
             popContext();
 
             ArrayNode conditionOut = nodeFactory.jsonArray();
             conditionOut.add(expression);
             conditionOut.add(nodeFactory.jsonArray(statementsOut));
             conditionsOut.add(conditionOut);
         }
 
         return nodeFactory.jsonArray(type, conditionsOut);
     }
 
     protected JsonNode processFor(ArrayNode for_) throws HistoneException {
         int type = for_.get(0).asInt();
         ArrayNode var = (ArrayNode) for_.get(1);
         ArrayNode collection = (ArrayNode) for_.get(2);
         ArrayNode statements = (ArrayNode) for_.get(3).get(0);
 
         ArrayNode elseStatements = null;
         if (for_.get(3).size() > 1) {
             elseStatements = (ArrayNode) for_.get(3).get(1);
         }
 
         String iterVal = var.get(0).asText();
         String iterKey = (var.size() > 1) ? var.get(1).asText() : null;
 
         collection = (ArrayNode) processAstNode(collection);
 
         pushContext();
         JsonNode[] statementsOut = new JsonNode[statements.size()];
         for (int i = 0; i < statements.size(); i++) {
             statementsOut[i] = processAstNode(statements.get(i));
         }
         popContext();
 
         JsonNode[] elseStatementsOut = null;
         if (elseStatements != null) {
             elseStatementsOut = new JsonNode[elseStatements.size()];
             for (int i = 0; i < elseStatements.size(); i++) {
                 elseStatementsOut[i] = processAstNode(elseStatements.get(i));
             }
         }
 
         ArrayNode statementsContainer = elseStatementsOut == null ?
                 nodeFactory.jsonArray(nodeFactory.jsonArray(statementsOut)) :
                 nodeFactory.jsonArray(nodeFactory.jsonArray(statementsOut), nodeFactory.jsonArray(elseStatementsOut));
 
         return nodeFactory.jsonArray(type, var, collection, statementsContainer);
     }
 
     protected JsonNode processSelector(ArrayNode selector) throws HistoneException {
         int type = selector.get(0).asInt();
         JsonNode fullVariable = selector.get(1);
 
         JsonNode[] tokensOut = new JsonNode[fullVariable.size()];
         for (int i = 0; i < fullVariable.size(); i++) {
             JsonNode token = fullVariable.get(i);
             if (token.isArray()) {
                 token = processAstNode(token);
             }
             tokensOut[i] = token;
         }
 
         return nodeFactory.jsonArray(type, nodeFactory.jsonArray(tokensOut));
     }
 
     //</editor-fold>
 
     /**
      * AST node types, that represent constants.
      */
     protected final static Set<Integer> CONSTANTS = new HashSet<Integer>(Arrays.asList(
             AstNodeType.TRUE,
             AstNodeType.FALSE,
             AstNodeType.NULL,
             AstNodeType.INT,
             AstNodeType.DOUBLE,
             AstNodeType.STRING
     ));
 
     /**
      * AST node types, that represent binary operations.
      */
     protected final static Set<Integer> BINARY_OPERATIONS = new HashSet<Integer>(Arrays.asList(
             AstNodeType.ADD,
             AstNodeType.SUB,
             AstNodeType.MUL,
             AstNodeType.DIV,
             AstNodeType.MOD,
             AstNodeType.OR,
             AstNodeType.AND,
             AstNodeType.NOT,
             AstNodeType.EQUAL,
             AstNodeType.NOT_EQUAL,
             AstNodeType.LESS_OR_EQUAL,
             AstNodeType.LESS_THAN,
             AstNodeType.GREATER_OR_EQUAL,
             AstNodeType.GREATER_THAN
     ));
 
     /**
      * AST node types, that represent unary operations.
      */
     protected final static Set<Integer> UNARY_OPERATIONS = new HashSet<Integer>(Arrays.asList(
             AstNodeType.NEGATE,
             AstNodeType.NOT
     ));
 
     /**
      * AST node types, that represent ternary operations.
      */
     protected final static Set<Integer> TERNARY_OPERATIONS = new HashSet<Integer>(Arrays.asList(
             AstNodeType.NEGATE
     ));
 
     /**
      * @return Codes of binary operations.
      */
     protected Set<Integer> getBinaryOperations() {
         return BINARY_OPERATIONS;
     }
 
     /**
      * @return Codes of unary operations.
      */
     protected Set<Integer> getUnaryOperations() {
         return UNARY_OPERATIONS;
     }
 
     /**
      * @return Codes of ternary operations.
      */
     protected Set<Integer> getTernaryOperations() {
         return TERNARY_OPERATIONS;
     }
 
     /**
      * @return Codes of binary, ternary and unary operations.
      */
     protected Set<Integer> getOperationsOverArguments() {
         final Set<Integer> result = new HashSet<Integer>();
         result.addAll(getBinaryOperations());
         result.addAll(getUnaryOperations());
         result.addAll(getTernaryOperations());
         return result;
     }
 
 
     //<editor-fold desc="Helper functions">
 
     public static boolean isStatements(ArrayNode arr) {
         int nodeType = getNodeType(arr);
         return nodeType == AstNodeType.STATEMENTS;
     }
 
     public static boolean isConstant(ArrayNode arr) {
         int nodeType = getNodeType(arr);
         return nodeType == AstNodeType.TRUE ||
                 nodeType == AstNodeType.FALSE ||
                 nodeType == AstNodeType.NULL ||
                 nodeType == AstNodeType.INT ||
                 nodeType == AstNodeType.DOUBLE ||
                 nodeType == AstNodeType.STRING;
     }
 
     public static boolean isMap(ArrayNode arr) {
         int nodeType = getNodeType(arr);
         return nodeType == AstNodeType.MAP;
     }
 
     /**
      * Returns true if all items in this map are constants (recursive). Accepts only map AST nodes, otherwise returns false.
      *
      * @see {@link #getNodeType(com.fasterxml.jackson.databind.node.ArrayNode)}
      */
     public static boolean isMapOfConstants(ArrayNode arr) {
         int nodeType = getNodeType(arr);
         if (nodeType != AstNodeType.MAP) return false;
 
         boolean result = true;
         ArrayNode mapEntries = (ArrayNode) arr.get(1);
         for (JsonNode mapEntry : mapEntries) {
             JsonNode entryKey = mapEntry.get(0);
             ArrayNode entryValue = (ArrayNode) mapEntry.get(1);
 
             result = result && (isConstant(entryValue) || isMapOfConstants(entryValue));
         }
         return result;
     }
 
     /**
      * Returns true if all AST nodes in input collection are constant nodes.
      *
      * @see {@link #isConstant(com.fasterxml.jackson.databind.node.ArrayNode)}
      */
     protected boolean isConstants(Collection<? extends ArrayNode> astArrays) {
         for (ArrayNode node : astArrays) {
             if (!isConstant(node)) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Returns {@link AstNodeType} value for the input node.
      */
     public static int getNodeType(ArrayNode astArray) {
        return astArray.get(0).asInt();
     }
 
     /**
      * The most problematic function of this class; After evaluating of constant expression (expression, that contains
      * only operations over arguments and all arguments are constants, Histone returns evaluated value as {@link Node}
      * (so it translates from AST node form to Histone node form). In optimizer, we need to translate it back to AST form.
      * <p/>
      * <p>But we definitely know, that constant expression can be evaluated only to one of following types:
      * <ul compact>
      * <li>Constant ({@link #CONSTANTS})</li>
      * <li>NULL</li>
      * <li>undefined (specific json value type)</li>
      * </ul>
      *
      * @see {@link #isConstant(com.fasterxml.jackson.databind.node.ArrayNode)}
      * @see {@link #getOperationsOverArguments()}
      */
     protected JsonNode node2Ast(Node node) {
         if (node.isBoolean()) {
             return node.getAsBoolean().getValue() ? nodeFactory.jsonArray(AstNodeType.TRUE) : nodeFactory.jsonArray(AstNodeType.FALSE);
         } else if (node.isInteger()) {
             return nodeFactory.jsonArray(AstNodeType.INT, nodeFactory.jsonNumber(node.getAsNumber().getValue()));
         } else if (node.isFloat()) {
             return nodeFactory.jsonArray(AstNodeType.DOUBLE, nodeFactory.jsonNumber(node.getAsNumber().getValue()));
         } else if (node.isString()) {
             return nodeFactory.jsonArray(AstNodeType.STRING, nodeFactory.jsonString(node.getAsString().getValue()));
         } else if (node.isNull()) {
             return nodeFactory.jsonArray(AstNodeType.NULL);
         } else if (node.isUndefined()) {
             return nodeFactory.jsonString("");
         } else if (node.isAst()) {
             return ((AstNode)node).getValue();
         }
 
         throw new IllegalStateException(String.format("Can't convert node %s to AST element", node));
     }
 
     protected AbstractASTWalker(NodeFactory nodeFactory) {
         this.nodeFactory = nodeFactory;
     }
 
     /**
      * Computes hash value for the input AST node. That value is used only for comparing two AST nodes (if they are equal or not).
      */
     public static long hash(JsonNode arr) {
         long result = 0;
 
         if (arr.isContainerNode()) {
             for (JsonNode node : arr) {
                 result += hash(node);
             }
         }
 
         if (arr.isValueNode()) {
             result += arr.asText().hashCode();
         }
 
         return result;
     }
 
     /**
      * Counts child nodes of the input one (recursive).
      */
     public static long countNodes(JsonNode arr) {
         long result = 0;
 
         if (arr.isContainerNode()) {
             for (JsonNode node : arr) {
                 result += countNodes(node) + 1;
             }
         }
 
         if (arr.isValueNode()) {
             result += 1;
         }
 
         return result;
     }
     //</editor-fold>
 
     public static final String KEYWORD_SELF = "self";
 }
