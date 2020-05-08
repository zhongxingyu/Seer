 /*
  * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
  *
  * This software is distributable under the BSD license. See the terms of the
  * BSD license in the documentation provided with this software.
  */
 package de.hunsicker.jalopy.printer;
 
 import java.io.IOException;
 
 import antlr.collections.AST;
 import de.hunsicker.jalopy.language.JavaNode;
 import de.hunsicker.jalopy.language.JavaNodeHelper;
 import de.hunsicker.jalopy.language.antlr.JavaTokenTypes;
 import de.hunsicker.jalopy.storage.ConventionDefaults;
 import de.hunsicker.jalopy.storage.ConventionKeys;
 
 
 /**
  * Printer for parameter lists [<code>ELIST</code>, <code>PARAMETERS</code>].
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 final class ParametersPrinter
     extends AbstractPrinter
 {
     //~ Static variables/initializers ----------------------------------------------------
 
     /** No alignment offset set. */
     static final int OFFSET_NONE = -1;
 
     /** The index of the first parameter in the list. */
     private static final int FIRST_PARAM = 0;
 
     /** Singleton. */
     private static final Printer INSTANCE = new ParametersPrinter();
 
     /** Always wrap/align all parameters. */
     private static final int MODE_ALWAYS = 1;
 
     /** Perform line wrapping/alignment only if necessary. */
     private static final int MODE_AS_NEEDED = 2;
 
     //~ Constructors ---------------------------------------------------------------------
 
     /**
      * Creates a new ParametersPrinter object.
      */
     public ParametersPrinter()
     {
     }
 
     //~ Methods --------------------------------------------------------------------------
 
     /**
      * Returns the sole instance of this class.
      *
      * @return the sole instance of this class.
      */
     public static final Printer getInstance()
     {
         return INSTANCE;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void print(
         AST        node,
         NodeWriter out)
       throws IOException
     {
         int line = out.line;
         boolean wrapped = false;
 
         Marker marker = out.state.markers.add();
 
         switch (node.getType())
         {
             // a method or ctor declaration
             case JavaTokenTypes.PARAMETERS :
 
                 boolean newlineAfter =
                     AbstractPrinter.settings.getBoolean(
                         ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_DEF,
                         ConventionDefaults.LINE_WRAP_AFTER_PARAMS_METHOD_DEF);
 
                 /**
                  * @todo move the whole test into printImpl()?
                  */
                 if (out.mode == NodeWriter.MODE_DEFAULT)
                 {
                     boolean align =
                         AbstractPrinter.settings.getBoolean(
                             ConventionKeys.ALIGN_PARAMS_METHOD_DEF,
                             ConventionDefaults.ALIGN_PARAMS_METHOD_DEF);
 
                     // determine if all parameters will be wrapped:
                     if (align)
                     {
                         // either wrapping is forced...
                         if (newlineAfter)
                         {
                             setAlignOffset(node, out);
                         }
                         else
                         {
                             AST expr = node.getFirstChild();
 
                             if (expr != null)
                             {
                                 TestNodeWriter tester = out.testers.get();
                                 PrinterFactory.create(expr, out).print(expr, tester);
 
                                 int lineLength =
                                     AbstractPrinter.settings.getInt(
                                         ConventionKeys.LINE_LENGTH,
                                         ConventionDefaults.LINE_LENGTH);
 
                                 // ... or necessary
                                 if ((out.column + tester.length) > lineLength)
                                 {
                                     setAlignOffset(node, out);
                                 }
 
                                 out.testers.release(tester);
                             }
                         }
                     }
                 }
 
                 wrapped =
                     out.state.parametersWrapped =
                         printImpl(
                             node, newlineAfter ? MODE_ALWAYS
                                                : MODE_AS_NEEDED, JavaTokenTypes.PARAMETERS,
                             out);
                 out.state.paramOffset = OFFSET_NONE;
 
                 break;
 
             // a method call or creator
             case JavaTokenTypes.ELIST :
                 wrapped = printImpl(node, MODE_AS_NEEDED, JavaTokenTypes.ELIST, out);
 
                 break;
 
             default :
                 throw new IllegalArgumentException("unexpected node type -- " + node);
         }
 
         // printImpl() returns 'false' if only one parameter was found, but we
         // want to let the right parenthesis stand out if the parameter took
         // more than one line to print anyway
         if (out.line > line)
         {
             wrapped = true;
         }
 
         // wrap and align, if necessary
         if (
             wrapped
             && AbstractPrinter.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_BEFORE_RIGHT_PAREN,
                 ConventionDefaults.LINE_WRAP_BEFORE_RIGHT_PAREN))
         {
             if (!out.newline)
             {
                 out.printNewline();
             }
 
             if (
                 AbstractPrinter.settings.getBoolean(
                     ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP))
             {
                 printIndentation(-1, out);
             }
             else
             {
                 printIndentation(out);
             }
         }
 
         out.state.markers.remove(marker,out);
     }
 
 
     /**
      * Sets the offset to align parameters of method declarations.
      *
      * @param node PARAMETER node.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O exception occured.
      */
     private void setAlignOffset(
         AST        node,
         NodeWriter out)
       throws IOException
     {
         int result = 0;
         TestNodeWriter tester = out.testers.get();
 
         for (AST param = node.getFirstChild(); param != null;
             param = param.getNextSibling())
         {
             switch (param.getType())
             {
                 case JavaTokenTypes.COMMA :
                     break;
 
                 default :
 
                     AST modifier = param.getFirstChild();
                     PrinterFactory.create(modifier, out).print(modifier, tester);
 
                     AST type = modifier.getNextSibling();
                     PrinterFactory.create(type, out).print(type, tester);
 
                     // +1 for the space between modifiers and name
                     int length = tester.length + 1;
 
                     if (length > result)
                     {
                         result = length;
                     }
 
                     tester.reset();
 
                     break;
             }
         }
 
         out.testers.release(tester);
 
         out.state.paramOffset = out.column + result;
     }
 
 
     /**
      * Determines whether the given EXPR node denotes a concatenated expression (either a
      * string concatenation or addititive expression using the + operator).
      *
      * @param node an EXPR node.
      *
      * @return <code>true</code> if the given node denotes a concatenated expression.
      *
      * @since 1.0b8
      */
 //    TODO private boolean isConcat(AST node)
 //    {
 //        for (AST child = node.getFirstChild(); child != null;
 //            child = child.getFirstChild())
 //        {
 //            switch (child.getType())
 //            {
 //                case JavaTokenTypes.PLUS :
 //                    return true;
 //
 //                default :
 //                    return isConcat(child);
 //            }
 //        }
 //
 //        return false;
 //    }
 
 
     private AST getFirstStringConcat(AST node)
     {
         AST expr = node.getFirstChild();
 
         switch (expr.getType())
         {
             case JavaTokenTypes.PLUS :
 
                 AST first = null;
 SEARCH: 
                 for (
                     AST next = expr.getFirstChild(); next != null;
                     next = next.getFirstChild())
                 {
                     switch (next.getType())
                     {
                         case JavaTokenTypes.PLUS :
                             break;
 
                         default :
                             first = next;
 
                             break SEARCH;
                     }
                 }
 
                 switch (first.getType())
                 {
                     case JavaTokenTypes.STRING_LITERAL :
                         return first;
                 }
 
                 break;
         }
 
         return null;
     }
 
 
     /**
      * Returns the last (deepest nested) child of the given node.
      *
      * @param node an EXPR or PARAMETER_DEF node.
      *
      * @return the last child of the given node.
      *
      * @throws IllegalArgumentException DOCUMENT ME!
      *
      * @todo fully implement the method in AbstractPrinter.java and use that method
      * @since 1.0b8
      */
 //    TODO private AST getLastChild(AST node)
 //    {
 //        AST result = null;
 //
 //        switch (node.getType())
 //        {
 //            case JavaTokenTypes.EXPR :
 //                result = node.getFirstChild();
 //
 //                for (AST child = result; child != null; child = child.getFirstChild())
 //                {
 //                    result = child;
 //                }
 //
 //                return result;
 //
 //            case JavaTokenTypes.PARAMETER_DEF :
 //
 //                for (
 //                    AST child = node.getFirstChild(); child != null;
 //                    child = child.getNextSibling())
 //                {
 //                    result = child;
 //                }
 //
 //                return result;
 //
 //            default :
 //                throw new IllegalArgumentException("invalid type -- " + node);
 //        }
 //    }
 
 
     /**
      * Determines wether the given EXPR node denotes a single literal string.
      *
      * @param node an EXPR node.
      *
      * @return <code>true</code> if the given nodes denotes a single literal string.
      *
      * @since 1.0b8
      */
     private boolean isSingleLiteral(AST node)
     {
         if ((node.getNextSibling() == null) && (node.getType() != JavaTokenTypes.PLUS))
         {
             switch (node.getFirstChild().getType())
             {
                 case JavaTokenTypes.STRING_LITERAL :
                     return true;
             }
         }
 
         return false;
     }
 
 
     /**
      * Adjusts the aligment offset. Called from {@link #wrapFirst} if the first parameter
      * was actually wrapped.
      *
      * @param column column offset before the newline was issued.
      * @param out stream to write to.
      *
      * @since 1.0b9
      */
     private void adjustAlignmentOffset(
         int        column,
         NodeWriter out)
     {
         if (out.state.paramOffset != OFFSET_NONE)
         {
             out.state.paramOffset = out.state.paramOffset - column + out.column;
         }
     }
 
 
     /**
      * Indicates whether the given parameters node contains a METHOD_CALL node as a
      * parameter.
      *
      * @param node a parameters node (either PARAMETERS or ELIST).
      *
      * @return <code>true</code> if the given parameters node does contain a METHOD_CALL
      *         node as a parameter.
      */
     private boolean containsMethodCall(AST node)
     {
         for (AST child = node.getFirstChild(); child != null;
             child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 case JavaTokenTypes.COMMA :
                     break;
 
                 default :
 
                     switch (child.getFirstChild().getType())
                     {
                         case JavaTokenTypes.METHOD_CALL :
                         case JavaTokenTypes.LITERAL_new :
                             return true;
                     }
 
                     break;
             }
         }
 
         return false;
     }
 
 
     /**
      * Prints the parameters of the given node.
      *
      * @param node parameter list.
      * @param action action to perform.
      * @param type the node type we print parameters for. Either PARAMETERS or ELIST
      * @param out stream to write to.
      *
      * @return <code>true</code> if line wrap.
      *
      * @throws IOException if an I/O error occured.
      */
     private boolean printImpl(
         AST        node,
         int        action,
         int        type,
         NodeWriter out)
       throws IOException
     {
         JavaNode parameter = (JavaNode) node.getFirstChild();
 
         if (parameter == null)
         {
             // no parameters found, nothing to do
             return false;
         }
 
         boolean wrapLines =
             AbstractPrinter.settings.getBoolean(
                 ConventionKeys.LINE_WRAP, ConventionDefaults.LINE_WRAP)
             && (out.mode == NodeWriter.MODE_DEFAULT);
         int lineLength =
             AbstractPrinter.settings.getInt(
                 ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);
         boolean indentDeep =
             AbstractPrinter.settings.getBoolean(
                 ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP);
         int deepIndentSize =
             AbstractPrinter.settings.getInt(
                 ConventionKeys.INDENT_SIZE_DEEP, ConventionDefaults.INDENT_SIZE_DEEP);
         boolean alignMethodCall =
             AbstractPrinter.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL,
                 ConventionDefaults.LINE_WRAP_AFTER_PARAMS_METHOD_CALL);
         boolean alignMethodCallIfNested =
             AbstractPrinter.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED,
                 ConventionDefaults.LINE_WRAP_AFTER_PARAMS_METHOD_CALL_IF_NESTED);
         boolean spaceAfterComma =
             AbstractPrinter.settings.getBoolean(
                 ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_COMMA);
         boolean preferWrapAfterLeftParen =
             AbstractPrinter.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_AFTER_LEFT_PAREN,
                 ConventionDefaults.LINE_WRAP_AFTER_LEFT_PAREN);
         boolean wrapIfFirst =
             AbstractPrinter.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_PARAMS_EXCEED,
                 ConventionDefaults.LINE_WRAP_PARAMS_EXCEED);
 
         boolean result = false;
         int paramIndex = 0;
         boolean restoreAction = false;
         int userAction = action;
         boolean firstWrapped = false; // was the first parameter wrapped?
 
         boolean[] data = shouldHaveSmallIndent(parameter,
             action,
             type,
             out, spaceAfterComma, lineLength,alignMethodCall);
         boolean smallIndent = data[0];
         alignMethodCall = data[1];
         
         if (out.mode == NodeWriter.MODE_DEFAULT)
         {
             out.state.paramList = true;
             out.state.paramLevel++;
             out.state.parenScope.addFirst(new ParenthesesScope(out.state.paramLevel));
         }
 
         while (parameter != null)
         {
             JavaNode next = (JavaNode) parameter.getNextSibling();
 
             switch (parameter.getType())
             {
                 case JavaTokenTypes.COMMA :
 
                     if (parameter.hasCommentsAfter())
                     {
                         out.print(COMMA, JavaTokenTypes.COMMA);
                         printCommentsAfter(
                             parameter, NodeWriter.NEWLINE_NO, action != MODE_ALWAYS, out);
                     }
                     else
                     {
                         out.print(COMMA, JavaTokenTypes.COMMA);
                     }
 
                     break;
 
                 default :
                     if (paramIndex == FIRST_PARAM && alignMethodCall){
                             if (smallIndent){
                                     
                                 out.printNewline();
                                 //add a new marker with an
                                 // appropiate indent level
                                 // on a new line
                                 Marker lastMarker = out.state.markers.getLast();
                                 // add only a 0 marker
                                 // Add a new marker with 0 and indent
                                 // the line was 0
                                 Marker current=out.state.markers.add(
                                     out.line,
                                     0,true,out
                                     );
                             }                        
                     }
 
                     //if (out.mode == NodeWriter.MODE_DEFAULT)
                     {
                         // overwrite the mode for method calls
                         if (
                             (type == JavaTokenTypes.ELIST)
                             && (alignMethodCall
                             || (alignMethodCallIfNested && containsMethodCall(node))))
                         {
                             
                             if (restoreAction)
                             {
                                 action = userAction;
                             }
 
                             switch (parameter.getFirstChild().getType())
                             {
                                 case JavaTokenTypes.METHOD_CALL :
                                     userAction = action;
                                     action = MODE_ALWAYS;
                                     restoreAction = true;
 
                                     break;
 
                                 default :
 
                                     /**
                                      * @todo use out.state.paramLevel?
                                      */
                                     switch (out.state.markers.count)
                                     {
                                         case 0 :
                                         case 1 : // first level parentheses restoreAction = false;
                                             break;
 
                                         default : // secondary, tertiary, ... parentheses
                                             action = MODE_ALWAYS;
 
                                             break;
                                     }
                             }
                         }
 
                         switch (action)
                         {
                             case MODE_AS_NEEDED :
 
                                 if (out.newline)
                                 {
                                     printIndentation(out);
 
                                     if (
                                         preferWrapAfterLeftParen
                                        && (paramIndex == FIRST_PARAM))
                                     {
                                         TestNodeWriter tester = out.testers.get();
 
                                         // determine the exact space all
                                         // parameters would need
                                         PrinterFactory.create(node, out).print(node, tester);
 
                                         // +1 for the right parenthesis
                                         if ((out.column + tester.length + 1) > lineLength)
                                         {
                                             firstWrapped = true;
                                         }
 
                                         out.testers.release(tester);
                                     }
                                 }
                                else if (wrapLines)
                                 {
                                     TestNodeWriter tester = out.testers.get();
 
                                     if (
                                         preferWrapAfterLeftParen
                                         && (paramIndex == FIRST_PARAM))
                                     {
                                         // determine the exact space all
                                         // parameters would need
                                         PrinterFactory.create(node, out).print(node, tester);
 
                                         // (+1 for the right parenthesis)
                                         tester.length += 1;
                                     }
                                     else
                                     {
                                         // determine the exact space this
                                         // parameter would need
                                         PrinterFactory.create(parameter, out).print(
                                             parameter, tester);
                                     }
 
                                     if (!preferWrapAfterLeftParen && (next != null))
                                     {
                                         if (spaceAfterComma)
                                         {
                                             tester.length += 2;
                                         }
                                         else
                                         {
                                             tester.length += 1;
                                         }
                                     }
 
                                     // space exceeds the line length but we
                                     // have to apply further checks
                                     if ((out.column + tester.length) > lineLength)
                                     {
                                         // for the first parameter we need to determine
                                         // whether we should print it directly after
                                         // parenthesis or wrap and indent
                                         if (paramIndex == FIRST_PARAM)
                                         {
                                             if (preferWrapAfterLeftParen)
                                             {
                                                 result =
                                                     wrapFirst(
                                                         type, true, next == null, out);
                                                 firstWrapped = result;
                                             }
                                             else if (
                                                 !indentDeep
                                                 && !shouldWrapAtLowerLevel(
                                                     parameter.getFirstChild(), lineLength,
                                                     deepIndentSize, out))
                                             {
                                                 if ((type == JavaTokenTypes.PARAMETERS))
                                                 {
                                                     result =
                                                         wrapFirst(
                                                             type, true, next == null, out);
                                                     firstWrapped = result;
                                                 }
 
                                                 // for chained method calls prefer wrapping
                                                 // along the dots
                                                 else if (
                                                     !JavaNodeHelper.isChained(
                                                         ((JavaNode) node)
                                                         .getPreviousSibling()))
                                                 {
                                                     if (isSingleLiteral(parameter))
                                                     {
                                                         /**
                                                          * @todo add StringBreaker
                                                          *       feature
                                                          */
                                                         result =
                                                             wrapFirst(
                                                                 type, true, next == null,
                                                                 out);
                                                         firstWrapped = result;
                                                     }
                                                     else
                                                     {
                                                         AST first =
                                                             getFirstStringConcat(
                                                                 parameter);
 
                                                         if (first != null)
                                                         {
                                                             tester.reset();
                                                             PrinterFactory.create(first, out)
                                                                           .print(
                                                                 first, tester);
 
                                                             if (
                                                                 (out.column
                                                                 + tester.length) > lineLength)
                                                             {
                                                                 result =
                                                                     wrapFirst(
                                                                         type, true,
                                                                         next == null, out);
                                                                 firstWrapped = result;
                                                             }
                                                             else
                                                             {
                                                                 result =
                                                                     wrapFirst(
                                                                         type, next == null,
                                                                         out);
                                                                 firstWrapped = result;
                                                             }
                                                         }
                                                         else if (
                                                             parameter.getFirstChild()
                                                                      .getType() == JavaTokenTypes.STRING_LITERAL)
                                                         {
                                                             result =
                                                                 wrapFirst(
                                                                     type, next == null,
                                                                     true, out);
                                                             firstWrapped = result;
                                                         }
                                                         else
                                                         {
                                                             result =
                                                                 wrapFirst(
                                                                     type, next == null,
                                                                     out);
                                                             firstWrapped = result;
                                                         }
                                                     }
                                                 }
                                             }
                                         }
                                         else // for successive params wrap/align
                                         {
                                             out.printNewline();
                                             printIndentation(out);
                                             result = true;
 
                                             /*int indentLength = out.getIndentLength();
                                             Marker m = out.state.markers.getLast();
                                             int length = (m.column > indentLength)
                                                              ? (m.column -
                                                              indentLength)
                                                              : m.column;
 
                                             // line break only needed if no endline
                                             // comment forced one
                                             if ((length + indentLength + 1) != out.column)
                                             {
                                                 out.printNewline();
                                                 printIndentation(out);
                                                 result = true;
                                             }
                                             else if (spaceAfterComma)
                                             {
                                                 out.print(SPACE,
                                                           JavaTokenTypes.WS);
                                             }*/
                                         }
 
                                         /*
                                         else // force specified indentation
                                         {
                                             //int length = indentation * out.state.paramLevel;
                                             int length = out.indentSize * out.state.paramLevel;
 
                                             // line break only needed if no endline
                                             // comment forced one
                                             if ((length + indentLength + 1) != out.column)
 
                                             {
                                                 out.printNewline();
                                                 printIndentation(out);
                                                 //out.print(out.getString(
                                                 //                  indentation * out.state.paramLevel),
                                                 //          JavaTokenTypes.WS);
 
                                                 result = true;
                                             }
                                             else if (spaceAfterComma)
                                                 out.print(SPACE, JavaTokenTypes.WS);
                                         }
                                         }*/
                                     }
                                     else if (wrapIfFirst && firstWrapped)
                                     {
                                         /**
                                          * @todo implement custom indentation
                                          */
                                         int indentLength = out.getIndentLength();
                                         Marker m = out.state.markers.getLast();
                                         int length =
                                             (m.column > indentLength)
                                             ? (m.column - indentLength)
                                             : m.column;
 
                                         if ((length + indentLength + 1) != out.column)
                                         {
                                             out.printNewline();
                                             printIndentation(out);
 
                                             result = true;
                                         }
                                         else if (spaceAfterComma)
                                         {
                                             out.print(SPACE, JavaTokenTypes.WS);
                                         }
                                     }
                                     else if (
                                         spaceAfterComma && (paramIndex != FIRST_PARAM))
                                     {
                                         out.print(SPACE, JavaTokenTypes.WS);
                                     }
 
                                     out.testers.release(tester);
                                 }
                                 else if (spaceAfterComma && (paramIndex != FIRST_PARAM))
                                 {
                                     out.print(SPACE, JavaTokenTypes.WS);
                                 }
 
                                 break;
 
                             case MODE_ALWAYS :
 
                                 if (paramIndex != FIRST_PARAM)
                                 {
                                     if (!out.newline)
                                     {
                                         out.printNewline();
                                         result = true;
                                     }
 
                                     printIndentation(out);
                                 }
                                 else
                                 {
                                     if (out.newline)
                                     {
                                         printIndentation(out);
                                         firstWrapped = true;
                                     }
                                     else if (preferWrapAfterLeftParen || (!indentDeep))
                                     {
                                         if (next == null)
                                         {
                                             TestNodeWriter tester = out.testers.get();
                                             PrinterFactory.create(parameter, out).print(
                                                 parameter, tester);
 
                                             // +1 for the right parenthesis
                                             if (
                                                 (out.column + tester.length + 1) > lineLength)
                                             {
                                                 result =
                                                     wrapFirst(
                                                         type, true, next == null, out);
                                                 firstWrapped = true;
                                             }
 
                                             out.testers.release(tester);
                                         }
                                         else
                                         {
                                             result =
                                                 wrapFirst(
                                                     type,
                                                     (preferWrapAfterLeftParen
                                                     || !indentDeep), next == null, out);
                                             firstWrapped = result;
                                         }
                                     }
                                     else if (out.column > deepIndentSize)
                                     {
                                         result =
                                             wrapFirst(
                                                 type, preferWrapAfterLeftParen,
                                                 next == null, out);
                                         firstWrapped = result;
                                     }
                                 }
 
                                 break;
                         }
                     }
                     /*
                     else if (spaceAfterComma && (paramIndex != FIRST_PARAM))
                     {
                         out.print(SPACE, JavaTokenTypes.WS);
                     }
 */
                     PrinterFactory.create(parameter, out).print(parameter, out);
 
                     paramIndex++;
 
                     break;
             }
 
             parameter = next;
         }
 
         if (out.mode == NodeWriter.MODE_DEFAULT)
         {
             out.state.paramList = false;
             out.state.paramLevel--;
             out.state.parenScope.removeFirst();
         }
 
         return result;
     }
 
 
     /**
      * Determines whether line wrapping should occur at a lower level (i.e. one of the
      * children will be wrapped).
      *
      * @param node first child of an EXPR node (via <code>expr.getFirstChild()</code>) to
      *        determine line wrapping policy for.
      * @param lineLength the maximum line length setting.
      * @param deepIndent the deep indent setting.
      * @param out stream to write to.
      *
      * @return <code>true</code> if line wrapping should be performed at a lower level.
      *
      * @throws IOException if an I/O error occured.
      */
     private boolean shouldWrapAtLowerLevel(
         AST        node,
         int        lineLength,
         int        deepIndent,
         NodeWriter out)
       throws IOException
     {
         for (AST child = node; child != null; child = child.getNextSibling())
         {
             switch (child.getType())
             {
                 case JavaTokenTypes.ELIST :
                 case JavaTokenTypes.QUESTION :
                 case JavaTokenTypes.PLUS :
 
                     AST first = child;
 SEARCH: 
                     for (
                         AST next = node.getFirstChild(); next != null;
                         next = next.getFirstChild())
                     {
                         switch (next.getType())
                         {
                             case JavaTokenTypes.PLUS :
                                 break;
 
                             default :
                                 first = next;
 
                                 break SEARCH;
                         }
                     }
 
                     switch (first.getType())
                     {
                         case JavaTokenTypes.STRING_LITERAL :
 
                             TestNodeWriter tester = out.testers.get();
                             PrinterFactory.create(first, out).print(first, tester);
 
                             if ((out.column + tester.length) > lineLength)
                             {
                                 out.testers.release(tester);
 
                                 return false;
                             }
 
                             out.testers.release(tester);
 
                             break;
                     }
 
                     return true;
 
                 case JavaTokenTypes.METHOD_CALL :
                 {
                     AST name = child.getFirstChild();
 
                     if ((out.column < deepIndent) && JavaNodeHelper.isChained(name))
                     {
                         return true;
                     }
 
                     String text = JavaNodeHelper.getDottedName(name);
 
                     if ((out.column + text.length()) > lineLength)
                     {
                         return false;
                     }
 
                     AST elist = name.getNextSibling();
                     int paramNum = 0;
 
                     for (
                         AST param = elist.getFirstChild(); param != null;
                         param = param.getNextSibling())
                     {
                         paramNum++;
                     }
 
                     if (paramNum > 0)
                     {
                         return true;
                     }
                     return false;
                 }
 
                 case JavaTokenTypes.LITERAL_new :
                 {
                     AST name = child.getFirstChild();
 
                     if ((out.column + 4 + name.getText().length()) > lineLength)
                     {
                         return false;
                     }
 
                     for (AST c = child.getFirstChild(); c != null;
                         c = c.getNextSibling())
                     {
                         switch (c.getType())
                         {
                             case JavaTokenTypes.ARRAY_INIT :
                                 return false;
 
                             case JavaTokenTypes.OBJBLOCK :
                                 return true;
                         }
                     }
 
                     AST elist = name.getNextSibling();
                     int paramNum = 0;
 
                     for (
                         AST param = elist.getFirstChild(); param != null;
                         param = param.getNextSibling())
                     {
                         if (paramNum == 0)
                         {
                             ;
                         }
                         else
                         {
                             return true;
                         }
                     }
 
                     return false;
                 }
 
                 /**
                  * @todo maybe this is debatable?
                  */
                 case JavaTokenTypes.LAND :
                 case JavaTokenTypes.LOR :
                 case JavaTokenTypes.BAND :
                 case JavaTokenTypes.BOR :
                 case JavaTokenTypes.BXOR :
                 case JavaTokenTypes.BNOT :
                 case JavaTokenTypes.MINUS :
                     return true;
 
                 case JavaTokenTypes.LPAREN :
 
                     /**
                      * @todo this advancing stuff should no longer be necessary
                      */
                     return shouldWrapAtLowerLevel(
                         PrinterHelper.advanceToFirstNonParen(child), deepIndent,
                         lineLength, out);
             }
         }
 
         return false;
     }
 
 
     /**
      * Prints a newline before the first parameter of a parameter list, if necessary.
      *
      * @param type the type of the parameter list. Either ELIST or PARAMETER.
      * @param last the amount of whitespace that is to print before the parameter.
      * @param out stream to write to.
      *
      * @return <code>true</code> if a newline was actually printed.
      *
      * @throws IOException DOCUMENT ME!
      */
     private boolean wrapFirst(
         int        type,
         boolean    last,
         NodeWriter out)
       throws IOException
     {
         return wrapFirst(type, false, last, out);
     }
 
 
     /**
      * Prints a newline before the first parameter of a parameter list.
      *
      * @param type the type of the parameter list. Either ELIST or PARAMETER.
      * @param force if <code>true</code> a newline will be forced.
      * @param last <code>true</code> indicates that this parameter is the last parameter
      *        of the list.
      * @param out stream to write to.
      *
      * @return <code>true</code> if a newline was actually printed.
      *
      * @throws IOException DOCUMENT ME!
      * @throws IllegalStateException DOCUMENT ME!
      */
     private boolean wrapFirst(
         int        type,
         boolean    force,
         boolean    last,
         NodeWriter out)
       throws IOException
     {
         boolean result = false;
 
         if (
             !AbstractPrinter.settings.getBoolean(
                 ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP) || !last)
         {
             switch (out.state.paramLevel)
             {
                 case 0 :
 
                     if (!out.state.markers.isMarked())
                     {
                         throw new IllegalStateException(
                             "not inside parentheses and no marker found");
                     }
 
                 // parameters of the outmost parentheses are aligned relative
                 // to the current indentation level
                 case 1 :
                 {
                     int length = out.indentSize;
 
                     /*if (!force && (indentation == -1))
                     {
                         length += out.continuationIndentSize;
                     }*/
 
                     // only wrap if the new column offset would be smaller than
                     // the current one and is between a certain tolerance area
                     if (
                         force
                         || (((length + out.getIndentLength()) < out.column)
                         && (length > (out.column - out.indentSize))))
                     {
                         int column = out.column;
 
                         out.printNewline();
                         printIndentation(out);
 
                         result = true;
 
                         adjustAlignmentOffset(column, out);
                     }
 
                     out.state.markers.add();
 
                     break;
                 }
 
                 // level 2 or deeper
                 default :
                 {
                     Marker marker = out.state.markers.get(out.state.markers.count - 2);
                     int indentLength = out.getIndentLength();
                     int offset =
                         ((marker.column > indentLength) ? (marker.column - indentLength)
                                                         : marker.column)
                         + (out.indentSize);
 
                     /*if (!force && (indentation == -1))
                     {
                         offset += out.continuationIndentSize;
                     }*/
 
                     // only wrap if the new column offset would be smaller than
                     // the current one and is between a certain tolerance area
                     if (
                         ((offset + indentLength) < out.column)
                         && (((offset + indentLength) < (out.column - out.indentSize))
                         || ((offset + indentLength) > (out.column + out.indentSize))))
                     {
                         int column = out.column;
 
                         out.printNewline();
                         printIndentation(out);
 
                         result = true;
 
                         adjustAlignmentOffset(column, out);
                     }
 
                     out.state.markers.add();
 
                     break;
                 }
             }
         }
 
         /*}else
         {
             int column = out.column;
             out.printNewline();
             //out.print(out.getString(indentation * out.state.paramLevel), JavaTokenTypes.WS);
             printIndentation(out);
             result = true;
             adjustAlignmentOffset(column, out);
         }*/
         return result;
     }
     private boolean[] shouldHaveSmallIndent(
         JavaNode        parameter,
         int        action,
         int        type,
         NodeWriter out, boolean spaceAfterComma, int lineLength,boolean alignMethodCall)
     throws IOException{
         
         boolean results[] = new boolean[]{false,alignMethodCall};
         int totalParameterWidth = 0;
         int totalParameters = 0;
 //        Point paramsSizes[] = null;
         boolean smallindent = false;
         
         boolean debugmode=false;
         JavaNode littleParam = null;
             
         
         if (AbstractPrinter.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_PARAMS_HARD,
                 ConventionDefaults.LINE_WRAP_PARAMS_HARD)) {
         if (debugmode)
             System.out.println("**Scaning line "+out.line+","+out.column+","+totalParameterWidth+","+type);
         
         // ignore method parameter calls for wrapping       
         // If we want we could change this but we need to adjust using
         //  setAlignOffset(node, out);
         if (type!=JavaTokenTypes.PARAMETERS) {
             TestNodeWriter paramTester = out.testers.get();
             paramTester.reset(out,false);
             //paramTester.column = paramTester.maxColumn = paramTester.length = out.column;
     
             // determine the exact space all
             // parameters would need
             littleParam = parameter;
             
             // Iterate through each parameter to get the length of the
             // line
             while(littleParam!=null){
                 PrinterFactory.create(littleParam, out).print(littleParam, paramTester);
                 if (spaceAfterComma && (totalParameters != FIRST_PARAM))
                 {
                     paramTester.print(SPACE, JavaTokenTypes.WS);
                 }
                 littleParam = (JavaNode) littleParam.getNextSibling();
                 if (paramTester.state.smallIndent) {
                     smallindent = true;
                 }
                 totalParameters ++;
     //            System.out.println("has indent is a "+paramTester.state.smallIndent+","+ totalParameters +"," + out.line+","+out.column);
             }
             // +1 for braces
             totalParameterWidth = paramTester.maxColumn + 1;
             if (debugmode)
             System.out.println("initial result "+paramTester.line+","+paramTester.column+","+paramTester.maxColumn);
             // new
             // If doing a mall indent
             // or we need to wrap
             // or we have multiple lines
             if (smallindent || totalParameterWidth > lineLength ||
                 paramTester.line>1) {
                     
                 // Attempt to wrap parameters see if that was sucesful in
                 // reducing the size to prevent the smallindent
                 TestNodeWriter paramTester2 = out.testers.get(); 
                 // A new line has occured , we need to test to see
                 // if it would be more efficient for us to make 
                 // the wrap or the child. 
                 paramTester2.reset(out,false);
                 //paramTester2.indent();
                 if (debugmode)
                 System.out.println("***Choosing to perform second scan on line"+out.line+","+out.column);
                 littleParam = parameter;
             
                 // Iterate through each parameter to get the length of the
                 // line (This is a cheap and dirty alignment of method parameters call
     //            System.out.println("Scanning line "+out.line+","+out.column);
                 while(littleParam!=null){
                     PrinterFactory.create(littleParam, out).print(littleParam, paramTester2);
                     littleParam = (JavaNode) littleParam.getNextSibling();
                     //increment twice to ignore the commas
                     if (littleParam !=null) {
                         littleParam = (JavaNode) littleParam.getNextSibling();
                         if (littleParam !=null) {
     //                        System.out.println("new line - "+paramTester2.line+","+littleParam+","+paramTester2.column); 
                             paramTester2.printNewline();
                             printIndentation(paramTester2);
                         }
                     }
                 }
                 // Results
                 // Changed to +3 incase another wrap has occured at the correct 
                 // position this will give a bit of flexibility
                 boolean pt1 = paramTester.maxColumn>lineLength+3;
                 if (debugmode)
                 System.out.println(pt1 + "Paramtesters 1,"+paramTester.line+","+paramTester.column+","+paramTester.maxColumn+":"+paramTester.state.smallIndent);
                 if (debugmode)
                 System.out.println("Paramtesters 2,"+paramTester2.line+","+paramTester2.column+","+paramTester2.maxColumn+":"+paramTester2.state.smallIndent);
     //            boolean pt2 = paramTester2.maxColumn>lineLength;
                 // Test if parameters in tester 1 exceed the line length
                 // or
                 // paramTester 1 is greater then line length 
                 // or
                 // paramtester 2.state.smallindent is false
                 // and
                 // paramtester 1.state.smallindent is true
                 if (paramTester.line>=paramTester2.line || pt1 ||
                     (!paramTester2.state.smallIndent && paramTester.state.smallIndent))
                 //if (true) 
                 {
                     if (debugmode)
                     System.out.println("Opting with 2");
                     out.testers.release(paramTester);
                     paramTester = paramTester2;
                     alignMethodCall=true;
                 }
                 else {
                     if (debugmode)
                     System.out.println("Opting with 1");
                     out.testers.release(paramTester2);
                 }
                 // Perform a small indent if tester wrapped small or 
                 // if specified in options
                 smallindent = paramTester.state.smallIndent || 
                     AbstractPrinter.settings.getBoolean(
                         ConventionKeys.LINE_WRAP_PARAMS_DEEP,
                         ConventionDefaults.LINE_WRAP_PARAMS_DEEP);
                 if (!smallindent){
                     // Changed to +3 incase another wrap has occured at the correct 
                     // position this will give a bit of flexibility
                     smallindent=paramTester.maxColumn>lineLength+3;
                 }
                 
                 // || paramTester.line>1 if we want to break
                 // whenever a line is wrapped
                 if ((smallindent ) && !alignMethodCall){
                     alignMethodCall = true;
                 }
             }
             //end
             // If we are using a new param tester we need to 
             // re initialize the total width
             // +1 for braces
             totalParameterWidth = paramTester.maxColumn + 1;
             if (debugmode) {
           System.out.println("Param tester status"+paramTester.line+","+paramTester.column);
             }
           out.testers.release(paramTester);
         }
         if (debugmode) {
       System.out.println("FLAGS "+smallindent+","+alignMethodCall);
       System.out.println("**Scanned line "+out.line+","+out.column+","+totalParameterWidth);
         }
       
       // Small indent state only applicable to TestNodeWriter's
       if (out instanceof TestNodeWriter){
           // Only change the state if the current state is not true
           // On release of the node writers the state will be changed
           if (!out.state.smallIndent){
             out.state.smallIndent=smallindent;
           }
           
       }
       else {
           // Small indent state only applicable to tester 
       }
       results[0] = smallindent;
       results[1] = alignMethodCall;
         }
       return results;
     }
 }
