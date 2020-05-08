 /*
  * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
  *
  * This software is distributable under the BSD license. See the terms of the
  * BSD license in the documentation provided with this software.
  */
 package de.hunsicker.jalopy.printer;
 
 import java.io.IOException;
 
 import de.hunsicker.antlr.collections.AST;
 import de.hunsicker.jalopy.language.JavaNode;
 import de.hunsicker.jalopy.language.JavaTokenTypes;
 import de.hunsicker.jalopy.storage.ConventionDefaults;
 import de.hunsicker.jalopy.storage.ConventionKeys;
 
 
 /**
  * Printer for <code>for</code> loops [<code>LITERAL_for</code>].
  * <pre class="snippet">
  * <strong>for </strong>(<em>initialization</em>; <em>Boolean-expression</em>; <em>step</em>;)
  * {
  *    <em>statement</em>
  * }
  * </pre>
  *
  * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
  * @version $Revision$
  */
 final class ForPrinter
     extends BlockStatementPrinter
 {
     //~ Static variables/initializers ----------------------------------------------------
 
     /** Singleton. */
     private static final Printer INSTANCE = new ForPrinter();
 
     //~ Constructors ---------------------------------------------------------------------
 
     /**
      * Creates a new ForPrinter object.
      */
     protected ForPrinter()
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
         super.print(node, out);
 
         int offset = 1;
 
         if (
             this.settings.getBoolean(
                 ConventionKeys.SPACE_BEFORE_STATEMENT_PAREN,
                 ConventionDefaults.SPACE_BEFORE_STATEMENT_PAREN))
         {
             offset = out.print(FOR_SPACE, JavaTokenTypes.LITERAL_for);
         }
         else
         {
             offset = out.print(FOR, JavaTokenTypes.LITERAL_for);
         }
 
         trackPosition((JavaNode) node, out.line, offset, out);
 
         AST lparen = node.getFirstChild();
         PrinterFactory.create(lparen).print(lparen, out);
 
         Marker marker = out.state.markers.add();
 
        /**
         * @todo print comments after semis
         */
         AST forInit = lparen.getNextSibling();
         AST firstSemi = forInit.getNextSibling();
         AST forCond = firstSemi.getNextSibling();
         AST secondSemi = forCond.getNextSibling();
         AST forIter = secondSemi.getNextSibling();
 
         int lineLength =
             this.settings.getInt(
                 ConventionKeys.LINE_LENGTH, ConventionDefaults.LINE_LENGTH);
         boolean indentDeep =
             this.settings.getBoolean(
                 ConventionKeys.INDENT_DEEP, ConventionDefaults.INDENT_DEEP);
         boolean firstWrap = false;
 
         if (out.mode == NodeWriter.MODE_DEFAULT)
         {
             out.state.paramLevel++;
             out.state.parenScope.addFirst(new ParenthesesScope(out.state.paramLevel));
 
             if (
                 this.settings.getBoolean(
                     ConventionKeys.LINE_WRAP_AFTER_LEFT_PAREN,
                     ConventionDefaults.LINE_WRAP_AFTER_LEFT_PAREN))
             {
                 TestNodeWriter tester = out.testers.get();
 
                 AST child = forInit.getFirstChild();
 
                 for (AST c = child; c != null; c = c.getNextSibling())
                 {
                     PrinterFactory.create(c).print(c, tester);
                 }
 
                 child = forCond.getFirstChild();
 
                 if (child != null)
                 {
                     PrinterFactory.create(child).print(child, tester);
                 }
 
                 child = forIter.getFirstChild();
 
                 if (child != null)
                 {
                     PrinterFactory.create(child).print(child, tester);
                 }
 
                 if ((out.column + tester.length) > lineLength)
                 {
                     firstWrap = true;
                 }
 
                 out.testers.release(tester);
             }
         }
 
         printForInit(forInit, firstWrap, out);
 
         boolean wrapAll =
             this.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_PARAMS_EXCEED,
                 ConventionDefaults.LINE_WRAP_PARAMS_EXCEED);
         boolean spaceAfterSemi =
             this.settings.getBoolean(
                 ConventionKeys.SPACE_AFTER_SEMICOLON,
                 ConventionDefaults.SPACE_AFTER_SEMICOLON);
 
         out.continuation =
             this.settings.getBoolean(
                 ConventionKeys.INDENT_CONTINUATION_BLOCK,
                 ConventionDefaults.INDENT_CONTINUATION_BLOCK);
 
         boolean secondWrap = false;
 
         if (out.mode == NodeWriter.MODE_DEFAULT)
         {
             if (firstWrap && wrapAll)
             {
                 secondWrap = true;
             }
             else
             {
                 TestNodeWriter tester = out.testers.get();
 
                 AST child = forCond.getFirstChild();
 
                 if (child != null)
                 {
                     PrinterFactory.create(child).print(child, tester);
 
                     // add enough space for semis before and after
                     tester.length += (spaceAfterSemi ? 3
                                                      : 2);
                 }
 
                 if ((out.column + tester.length) > lineLength)
                 {
                     secondWrap = true;
                 }
 
                 out.testers.release(tester);
             }
         }
 
         printForCond(forCond, secondWrap, out);
 
         boolean thirdWrap = false;
 
         if (out.mode == NodeWriter.MODE_DEFAULT)
         {
             if (firstWrap && wrapAll)
             {
                 thirdWrap = true;
             }
             else
             {
                 TestNodeWriter tester = out.testers.get();
 
                 AST child = forIter.getFirstChild();
 
                 if (child != null)
                 {
                     PrinterFactory.create(child).print(child, tester);
 
                     // add enough space for semis before and parenthesis after
                     tester.length += (spaceAfterSemi ? 5
                                                      : 3);
                 }
 
                 if ((out.column + tester.length) > lineLength)
                 {
                     thirdWrap = true;
                 }
 
                 out.testers.release(tester);
             }
         }
 
         printForIter(forIter, thirdWrap, out);
 
         out.continuation = false;
 
         if (
             (firstWrap || secondWrap || thirdWrap)
             && this.settings.getBoolean(
                 ConventionKeys.LINE_WRAP_BEFORE_RIGHT_PAREN,
                 ConventionDefaults.LINE_WRAP_BEFORE_RIGHT_PAREN))
         {
             if (!out.newline)
             {
                 out.printNewline();
 
                 if (indentDeep)
                 {
                     out.print(
                         out.getString(marker.column - 1 - out.getIndentLength()),
                         JavaTokenTypes.WS);
                 }
             }
 
             out.print(EMPTY_STRING, JavaTokenTypes.WS);
         }
 
         AST rparen = forIter.getNextSibling();
 
         PrinterFactory.create(rparen).print(rparen, out);
 
         out.state.markers.remove(marker);
 
         if (out.mode == out.MODE_DEFAULT)
         {
             out.state.paramLevel--;
             out.state.parenScope.removeFirst();
         }
 
         out.last = JavaTokenTypes.LITERAL_for;
 
         AST body = rparen.getNextSibling();
 
         switch (body.getType())
         {
             case JavaTokenTypes.SLIST :
                 PrinterFactory.create(body).print(body, out);
 
                 break;
 
             default :
 
                 // insert braces manually
                 if (
                     this.settings.getBoolean(
                         ConventionKeys.BRACE_INSERT_FOR,
                         ConventionDefaults.BRACE_INSERT_FOR))
                 {
                     out.printLeftBrace(
                         this.settings.getBoolean(
                             ConventionKeys.BRACE_NEWLINE_LEFT,
                             ConventionDefaults.BRACE_NEWLINE_LEFT), NodeWriter.NEWLINE_YES);
                     PrinterFactory.create(body).print(body, out);
                     out.printRightBrace();
                 }
                 else
                 {
                     out.printNewline();
                     out.indent();
                     PrinterFactory.create(body).print(body, out);
                     out.unindent();
                 }
         }
 
         // to simplify line wrapping we always indicate braces
         out.last = JavaTokenTypes.RCURLY;
     }
 
 
     /**
      * Prints the condition part of the for loop.
      *
      * @param node the condition part node of the loop.
      * @param wrap if <code>true</code> print a newline before the part.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printForCond(
         AST        node,
         boolean    wrap,
         NodeWriter out)
       throws IOException
     {
        out.print(SEMI, JavaTokenTypes.FOR_INIT);

         if (node.getFirstChild() == null)
         {
             return;
         }
 
         if (wrap)
         {
             out.printNewline();
             printIndentation(out);
         }
         else if (
             this.settings.getBoolean(
                 ConventionKeys.SPACE_AFTER_SEMICOLON,
                 ConventionDefaults.SPACE_AFTER_SEMICOLON))
         {
             out.print(SPACE, JavaTokenTypes.FOR_INIT);
         }
 
         for (AST child = node.getFirstChild(); child != null;
             child = child.getNextSibling())
         {
             PrinterFactory.create(child).print(child, out);
         }
     }
 
 
     /**
      * Prints the initialization part of the for loop.
      *
      * @param node the initialization part node of the loop.
      * @param wrap DOCUMENT ME!
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printForInit(
         AST        node,
         boolean    wrap,
         NodeWriter out)
       throws IOException
     {
         if (wrap)
         {
             out.printNewline();
             printIndentation(out);
         }
 
         AST child = node.getFirstChild();
 
         if (child != null)
         {
             printVariableDefs(node, out);
         }
     }
 
 
     /**
      * Prints the iteration part of the for loop.
      *
      * @param node the iteration part node of the loop.
      * @param wrap if <code>true</code> print a newline before the part.
      * @param out stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printForIter(
         AST        node,
         boolean    wrap,
         NodeWriter out)
       throws IOException
     {
        out.print(SEMI, JavaTokenTypes.FOR_INIT);

         AST elist = node.getFirstChild();
 
         if (elist == null)
         {
             return;
         }
 
         if (wrap)
         {
             out.printNewline();
             printIndentation(out);
         }
         else if (
             this.settings.getBoolean(
                 ConventionKeys.SPACE_AFTER_SEMICOLON,
                 ConventionDefaults.SPACE_AFTER_SEMICOLON))
         {
             out.print(SPACE, JavaTokenTypes.FOR_INIT);
         }
 
         boolean spaceAfterComma =
             this.settings.getBoolean(
                 ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_SEMICOLON);
         String comma = spaceAfterComma ? COMMA_SPACE
                                        : COMMA;
 
         // we don't use ParametersPrinter.java because of the
         // automated added parentheses, which would lead to compile
         // errors here
         for (
             AST element = elist.getFirstChild(); element != null;
             element = element.getNextSibling())
         {
             switch (element.getType())
             {
                 case JavaTokenTypes.COMMA :
                     out.print(comma, JavaTokenTypes.COMMA);
 
                     break;
 
                 default :
                     PrinterFactory.create(element).print(element, out);
 
                     break;
             }
         }
     }
 
 
     /**
      * Outputs a single variable definition of the initialization part of the for loop.
      *
      * @param node a node.
      * @param out the output stream to write to.
      * @param printType
      *
      * @throws IOException if an I/O error occured.
      */
     private void printVariableDef(
         AST        node,
         NodeWriter out,
         boolean    printType)
       throws IOException
     {
         // the initialization part of the loop can either consist of variable
         // definitions or assignment expressions
         // for (int i = 0;;){...}  vs.  for (i = 0;;){...}
         switch (node.getType())
         {
             // one or more assignment expressions
             case JavaTokenTypes.ELIST :
 
                 boolean spaceAfterComma =
                     this.settings.getBoolean(
                         ConventionKeys.SPACE_AFTER_COMMA,
                         ConventionDefaults.SPACE_AFTER_COMMA);
                 String comma = spaceAfterComma ? COMMA_SPACE
                                                : COMMA;
 
                 /**
                  * @todo is this still valid?
                  */
 
                 // don't use ParametersPrinter.java because of the added
                 // parenthesis
                 for (
                     AST param = node.getFirstChild(); param != null;
                     param = param.getNextSibling())
                 {
                     PrinterFactory.create(param).print(param, out);
 
                     /**
                      * @todo space after comma
                      */
                     if (param.getNextSibling() != null)
                     {
                         out.print(comma, JavaTokenTypes.COMMA);
                     }
                 }
 
                 return;
         }
 
        AST mod = node.getFirstChild();
        AST type = mod.getNextSibling();
 
         if (printType)
         {
             PrinterFactory.create(type).print(type, out);
         }
 
        AST ident = type.getNextSibling();
 
         // only insert whitespace if we haven't multiple variable defs in the
         // initialization part
         if (out.last != JavaTokenTypes.COMMA)
         {
             out.print(SPACE, out.last);
         }
 
        PrinterFactory.create(ident).print(ident, out);
 
        AST assign = ident.getNextSibling();
 
         if (assign != null)
         {
             // if we're printing multiple variable defs but the last one
             // has no assignment then a semi appears which is bad as we always
             // output one in printForCond(), so ignore it
             switch (assign.getType())
             {
                 case JavaTokenTypes.SEMI :
                     break;
 
                 default :
                     PrinterFactory.create(assign).print(assign, out);
 
                     break;
             }
         }
     }
 
 
     /**
      * Outputs all variable definitions/initializations of the initialization part of the
      * for loop.
      *
      * @param node a VARIABLE_DEF node.
      * @param out the output stream to write to.
      *
      * @throws IOException if an I/O error occured.
      */
     private void printVariableDefs(
         AST        node,
         NodeWriter out)
       throws IOException
     {
         AST child = node.getFirstChild();
         boolean spaceAfterComma =
             this.settings.getBoolean(
                 ConventionKeys.SPACE_AFTER_COMMA, ConventionDefaults.SPACE_AFTER_COMMA);
         String comma = spaceAfterComma ? COMMA_SPACE
                                        : COMMA;
 
         switch (child.getType())
         {
             case JavaTokenTypes.VARIABLE_DEF :
                 printVariableDef(child, out, true);
 
                 for (
                     child = child.getNextSibling(); child != null;
                     child = child.getNextSibling())
                 {
                     out.print(comma, JavaTokenTypes.COMMA);
                     printVariableDef(child, out, false);
                 }
 
                 break;
 
             case JavaTokenTypes.ELIST :
 
                 for (AST var = child.getFirstChild(); var != null;
                     var = var.getNextSibling())
                 {
                     switch (child.getType())
                     {
                         case JavaTokenTypes.COMMA :
                             out.print(comma, JavaTokenTypes.COMMA);
 
                             break;
 
                         default :
                             PrinterFactory.create(var).print(var, out);
 
                             break;
                     }
                 }
 
                 break;
         }
     }
 }
