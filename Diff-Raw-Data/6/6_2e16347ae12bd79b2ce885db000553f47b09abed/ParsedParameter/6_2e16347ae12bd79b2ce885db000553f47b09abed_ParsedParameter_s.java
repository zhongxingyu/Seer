 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.gen.parser.parameter.analysis.DepthFirstAdapter;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AAlphanumericFunctionArgToken;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AAlphanumericParamToken;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AAnySequenceParamToken;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AEscapeSequenceFunctionArgToken;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AEscapeSequenceParamToken;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AFunction;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.ALiteral;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AReference;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.AVariable;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.PFunctionArgList;
 import org.eclipse.jubula.client.core.gen.parser.parameter.node.TComma;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 /**
  * Contains information gathered from parsing a Parameter string. The class
  * is designed to be instantiated, used in a call to 
  * {@link Start#apply(org.eclipse.jubula.client.core.parser.parameter.node.Switch)},
  * and then queried for tokens via {@link #getTokens()}.
  * @author BREDEX GmbH
  * @created 12.01.2011
  */
 public class ParsedParameter extends DepthFirstAdapter {
 
     /** parsed value tokens */
     private List<IParamValueToken> m_paramValueTokens;
     
     /** whether the parsed value comes from user input */
     private boolean m_isGuiSource;
 
     /** the parameter node to which the parsed parameter belongs */
     private IParameterInterfacePO m_paramNode;
     
     /** the parameter description to which the parsed parameter belongs */
     private IParamDescriptionPO m_paramDesc;
 
     /**
      * Constructor
      * 
      * @param isGuiSource Whether the parsed value comes from user input.
      * @param paramNode The parameter node to which the 
      *                  parsed parameter belongs.
      * @param desc The parameter description to which the 
      *             parsed parameter belongs.
      */
     public ParsedParameter(boolean isGuiSource, 
             IParameterInterfacePO paramNode, IParamDescriptionPO desc) {
         m_paramValueTokens = new ArrayList<IParamValueToken>();
         m_isGuiSource = isGuiSource;
         m_paramNode = paramNode;
         m_paramDesc = desc;
     }
     
     @Override
     public void caseALiteral(ALiteral literal) {
         super.caseALiteral(literal);
         StringBuilder literalBuilder = new StringBuilder();
         literalBuilder.append(literal.getOpenLiteral().getText());
         if (literal.getLiteralBody() != null) {
             literalBuilder.append(literal.getLiteralBody().getText());
         }
         literalBuilder.append(literal.getCloseLiteral().getText());
         m_paramValueTokens.add(new LiteralToken(
                 literalBuilder.toString(), literal.getOpenLiteral().getPos()));
     }
 
     @Override
     public void caseAEscapeSequenceParamToken(
             AEscapeSequenceParamToken escSeq) {
         super.caseAEscapeSequenceParamToken(escSeq);
         m_paramValueTokens.add(new SimpleValueToken(
                 escSeq.getEscapedSymbol().getText(), 
                 escSeq.getEscapedSymbol().getPos(), m_paramDesc));
     }
     
     @Override
     public void caseAReference(AReference ref) {
         super.caseAReference(ref);
         boolean containsBraces = ref.getOpenBrace() != null;
         StringBuilder refBuilder = 
             new StringBuilder(ref.getReferenceToken().getText());
         if (containsBraces) {
             refBuilder.append(ref.getOpenBrace().getText());
         }
 
         if (ref.getReferenceBody() != null) {
             refBuilder.append(ref.getReferenceBody().getText());
         } else {
             if (containsBraces && ref.getCloseBrace() != null) {
                 throw new SemanticParsingException(
                         I18n.getString(MessageIDs.getMessage(
                                 MessageIDs.E_MISSING_CONTENT)), 
                         MessageIDs.E_MISSING_CONTENT, 
                         ref.getReferenceToken().getPos());
             }
             
             throw new SemanticParsingException(
                     I18n.getString(MessageIDs.getMessage(
                             MessageIDs.E_ONE_CHAR_PARSE_ERROR)), 
                     MessageIDs.E_ONE_CHAR_PARSE_ERROR, 
                     ref.getReferenceToken().getPos());
         }
 
         if (ref.getCloseBrace() != null) {
             if (!containsBraces) {
                 throw new SemanticParsingException(
                         I18n.getString(MessageIDs.getMessage(
                                 MessageIDs.E_GENERAL_PARSE_ERROR)), 
                         MessageIDs.E_GENERAL_PARSE_ERROR,
                         ref.getCloseBrace().getPos());
             }
             refBuilder.append(ref.getCloseBrace().getText());
         } else if (containsBraces) {
             throw new SemanticParsingException(
                     I18n.getString(MessageIDs.getMessage(
                             MessageIDs.E_MISSING_CLOSING_BRACE)), 
                     MessageIDs.E_MISSING_CLOSING_BRACE, 
                     ref.getOpenBrace().getPos());
         }
 
         m_paramValueTokens.add(new RefToken(refBuilder.toString(), 
                 m_isGuiSource, ref.getReferenceToken().getPos(), m_paramNode, 
                 m_paramDesc));
     }
 
     @Override
     public void caseAVariable(AVariable var) {
         super.caseAVariable(var);
         boolean containsBraces = var.getOpenBrace() != null;
         String variableBody = var.getVariableBody() != null 
             ? var.getVariableBody().getText() : StringUtils.EMPTY;
         StringBuilder varBuilder = 
             new StringBuilder(var.getVariableToken().getText());
         if (containsBraces) {
             varBuilder.append(var.getOpenBrace().getText());
         }
         if (!StringUtils.isEmpty(variableBody)) {
             varBuilder.append(variableBody);
         } else {
             if (containsBraces && var.getCloseBrace() != null) {
                 throw new SemanticParsingException(
                         I18n.getString(MessageIDs.getMessage(
                                 MessageIDs.E_MISSING_CONTENT)), 
                         MessageIDs.E_MISSING_CONTENT, 
                         var.getVariableToken().getPos());
             }
             
             throw new SemanticParsingException(
                     I18n.getString(MessageIDs.getMessage(
                             MessageIDs.E_ONE_CHAR_PARSE_ERROR)), 
                     MessageIDs.E_ONE_CHAR_PARSE_ERROR, 
                     var.getVariableToken().getPos());
         }
         if (var.getCloseBrace() != null) {
             if (!containsBraces) {
                 throw new SemanticParsingException(
                         I18n.getString(MessageIDs.getMessage(
                                 MessageIDs.E_GENERAL_PARSE_ERROR)), 
                         MessageIDs.E_GENERAL_PARSE_ERROR,
                         var.getCloseBrace().getPos());
             }
             varBuilder.append(var.getCloseBrace().getText());
         } else if (containsBraces) {
             throw new SemanticParsingException(
                     I18n.getString(MessageIDs.getMessage(
                             MessageIDs.E_MISSING_CLOSING_BRACE)), 
                     MessageIDs.E_MISSING_CLOSING_BRACE, 
                     var.getOpenBrace().getPos());
         }
         
         m_paramValueTokens.add(new VariableToken(
                 varBuilder.toString(), var.getVariableToken().getPos(), 
                 variableBody, m_paramDesc));
 
     }
     
     @Override
     public void caseAAlphanumericParamToken(
             AAlphanumericParamToken alphanumeric) {
         super.caseAAlphanumericParamToken(alphanumeric);
         m_paramValueTokens.add(new SimpleValueToken(
                 alphanumeric.getAlphanumeric().getText(), 
                 alphanumeric.getAlphanumeric().getPos(), m_paramDesc));
     }
     
     @Override
     public void caseAAnySequenceParamToken(AAnySequenceParamToken anySeq) {
         super.caseAAnySequenceParamToken(anySeq);
         m_paramValueTokens.add(new SimpleValueToken(
                 anySeq.getChar().getText(), 
                 anySeq.getChar().getPos(), m_paramDesc));
     }
     
     @Override
     public void caseAFunction(AFunction function) {
         // No call to super() here because we want to traverse the argument list
         // separately. We do not want the argument list productions to appear
         // aside from the function.
         
         if (function.getFunctionName() == null
                 || StringUtils.isEmpty(function.getFunctionName().getText())) {
             throw new SemanticParsingException(
                     I18n.getString(MessageIDs.getMessage(
                             MessageIDs.E_MISSING_FUNCTION_NAME)),
                     MessageIDs.E_MISSING_FUNCTION_NAME, 
                     function.getFunctionToken().getPos());
         }
         
         // collect argument list
         ParsedParameter argumentParser = new ParsedParameter(
                 m_isGuiSource, m_paramNode, m_paramDesc);
         PFunctionArgList functionArgList = function.getFunctionArgList();
         if (functionArgList != null) {
             functionArgList.apply(argumentParser);
         }
 
         IParamValueToken[] argumentTokens = argumentParser.getTokens().toArray(
                 new IParamValueToken[argumentParser.getTokens().size()]);
         StringBuilder functionTextBuilder = new StringBuilder();
         functionTextBuilder.append(function.getFunctionToken().getText())
             .append(function.getFunctionName().getText())
             .append(function.getBeginFunctionArgsToken().getText());
 
         String functionPrefix = functionTextBuilder.toString();
         for (IParamValueToken token : argumentTokens) {
            functionTextBuilder.append(token.getGuiString());
         }
 
         String functionSuffix = function.getEndFunctionArgsToken().getText();
         functionTextBuilder.append(functionSuffix);
         
         m_paramValueTokens.add(new FunctionToken(
                 functionTextBuilder.toString(),
                 functionPrefix, functionSuffix,
                 function.getFunctionToken().getPos(), m_paramDesc, 
                 argumentTokens));
 
     }
     
     @Override
     public void caseTComma(TComma node) {
         super.caseTComma(node);
         m_paramValueTokens.add(new SimpleValueToken(
                 node.getText(), node.getPos(), m_paramDesc));
     }
     
     @Override
     public void caseAEscapeSequenceFunctionArgToken(
             AEscapeSequenceFunctionArgToken escapeSequence) {
         super.caseAEscapeSequenceFunctionArgToken(escapeSequence);
         m_paramValueTokens.add(new SimpleValueToken(
                 escapeSequence.getEscapedSymbolInFunction().getText(), 
                 escapeSequence.getEscapedSymbolInFunction().getPos(), 
                 m_paramDesc));
     }
 
     @Override
     public void caseAAlphanumericFunctionArgToken(
             AAlphanumericFunctionArgToken node) {
         super.caseAAlphanumericFunctionArgToken(node);
         m_paramValueTokens.add(new SimpleValueToken(
                 node.getFunctionAlphanumeric().getText(), 
                 node.getFunctionAlphanumeric().getPos(), 
                 m_paramDesc));
     }
     
     /**
      * 
      * @return the tokens parsed from the AST.
      */
     public List<IParamValueToken> getTokens() {
         return m_paramValueTokens;
     }
 }
