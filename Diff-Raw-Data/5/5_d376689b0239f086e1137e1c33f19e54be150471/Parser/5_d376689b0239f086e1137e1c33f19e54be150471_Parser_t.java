 /*
  *  lemonjuice - Java Template Engine.
  *  Copyright (C) 2009 Manuel Tomis support@pagegoblin.com
  *
  *  This library is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.pagegoblin.lemonjuice.engine;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.pagegoblin.lemonjuice.Renderable;
 import com.pagegoblin.lemonjuice.Template;
 import com.pagegoblin.lemonjuice.TemplateContext;
 import com.pagegoblin.lemonjuice.TemplateFunction;
 import com.pagegoblin.lemonjuice.TemplateRenderer;
 import com.pagegoblin.lemonjuice.engine.builtins.AppendFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.EqualsFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.FlattenFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.HeadFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.JoinFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.LengthFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.LookupFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.ReverseFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.ShuffleFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.SortFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.SplitFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.StripFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.TailFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.URLFunction;
 import com.pagegoblin.lemonjuice.engine.builtins.XMLFunction;
 
 public class Parser {
     private Scanner in;
     private Template template;
     
     private enum IfType {ANY, SINGLE, MULTIPLE};
 
     private static final String[] IF_TERMINATORS = {"else", "end"};
     private static final String[] END_TERMINATOR = {"end"};
     private static final String[] FOR_TERMINATORS = {"do", "end"};
 
     public Parser(Reader in, Template template) {
         this.in = new Scanner(new PeekReader(in));
         this.template = template;
     }
 
     public Element parse() throws IOException {
         List<Element> ls = new ArrayList<Element>();
         for (;;) {
             in.read();
             int column = pray();
             
             if (in.isEnd()) {
                 return bless(column, new SequenceElement(ls));
             } else if (in.isText()) {
                 ls.add(bless(column, new TextElement(in.getToken())));
             } else if (in.isSymbol("${")) {
                 ls.add(bless(column, parseTag()));
             }
         }
     }
 
     private Element parseBody(String[] terminators) throws IOException {
         List<Element> ls = new ArrayList<Element>();
         for (;;) {
             in.read();
             int column = pray();
             
             if (in.isEnd()) {
                 error("Unexpected EOF");
             } else if (in.isText()) {
                 ls.add(bless(column, new TextElement(in.getToken())));
             } else if (in.isSymbol("${")) {
                 in.peek();
 
                 for (String key : terminators) {
                     if (in.isSymbol(key)) {
                         return bless(column, new SequenceElement(ls));
                     }
                 }
                 
                 ls.add(bless(column, parseTag()));
             }
         }
     }
 
     private int pray() {
         return in.column();
     }
     
     private Element bless(int column, Element element) {
         if (element != null) {
             element.bless(template.getName(), in.line(), column);
         }
         return element;
     }
     
     private Element error(String text) throws IOException {
         throw new IOException("" + template.getName() + ": line " + in.line() + "," + in.column() + ": " + text);
     }    
     
     private void expect(String text) throws IOException {
         in.peek();
         
         if (in.isSymbol(text)) {
             in.read();
         } else {
             error("Expected `" + text + "' instead of `" + in.getToken() + "'");
         }
     }
 
     private boolean checkEnd() throws IOException {
         in.peek();
         
         return (in.isSymbol("}") || in.isSymbol(")") || in.isSymbol("]") || in.isSymbol(";")
              || in.isSymbol(",") || in.isSymbol("||") || in.isSymbol("&&") || in.isSymbol("..")
              || in.isSymbol("do") || in.isSymbol("else") || in.isSymbol("then")
              || in.isSymbol("in") || in.isSymbol("+"));
     }
 
     private boolean check(String text) throws IOException {
         in.peek();
         
         if (in.isSymbol(text)) {
             in.read();
             return true;
         } else {
             return false;
         }
     }
 
     private boolean peek(String text) throws IOException {
         in.peek();
         return in.isSymbol(text);
     }
     
     private Element parseTag() throws IOException {
         List<Element> elements = new ArrayList<Element>();
         in.peek();
         int column = pray();
         
         for (;;) {
             Element element = parseStatement();
             if (element != null) {
                 elements.add(element);
             }
 
             if (check(";")) {
                 if (check("}")) {
                     break;
                 }
             } else {
                 expect("}");
                 break;
             }
         }
         
         if (elements.size() == 1) {
             return elements.get(0);
         } else {
             return bless(column, new SequenceElement(elements));
         }
     }
     
     private Element parseStatement() throws IOException {
         in.peek();
         int column = pray();
         
         Element element;
         if (in.isSymbol("macro")) {
             element = parseDefine();
         } else {
             element = parseSubStatement();
         }
         
         return bless(column, element);
     }
 
     private Element parseSubStatement() throws IOException {
         in.peek();
         int column = pray();
         
         Element element;
         if (in.isSymbol("if")) {
             element = parseIf(IfType.ANY);
         } else if (in.isSymbol("for")) {
             element = parseFor();
         } else if (in.isSymbol("set")) {
             element = parseSet();
         } else {
             element = parseExpression();
         }
         
         return bless(column, element);
     }
     
     private Element parseList() throws IOException {
         List<Element> ls = new ArrayList<Element>();
 
         expect("[");
         in.peek();
         int column = pray();
 
         if (in.isKeyword()) {
             List<String> keys = new ArrayList<String>();
             
             for (;;) {
                 if (check("]")) {
                     break;
                 }
 
                 in.read();
                 if (in.isKeyword()) {
                     keys.add(in.getToken());
                 } else {
                     error("Expected keyword instead of `" + in.getToken() + "'");
                 }
                 
                 ls.add(parseExpression());
                 if (check("]")) {
                     break;
                 } else {
                     expect(",");
                 }
             }
             
             return bless(column, new MapElement(keys.toArray(new String[ls.size()]), ls.toArray(new Element[ls.size()])));
             
         } else {
             for (;;) {
                 if (check("]")) {
                     break;
                 }
     
                 ls.add(parseExpression());
                 if (check("]")) {
                     break;
                 } else {
                     expect(",");
                 }
             }
             
             return bless(column, new ListElement(ls.toArray(new Element[ls.size()])));
         }
     }
 
     private Element parseSet() throws IOException {
         expect("set");
         in.read();
         int column = pray();
 
         if (!in.isName()) {
             error("Expected local variable name instead of `" + in.getToken() + "'");
         }
         
         String name = in.getToken();
         in.peek();
         
         Element element = null;
         if (!checkEnd()) {
             element = parseExpression();
         }
         
         return bless(column, new SetElement(name, element));
     }
 
     private Element parseIf(IfType type) throws IOException {
         expect("if");
         int column = pray();
         
         Element condition = parseExpression();
         
         boolean then = check("then");
         if (check("}")) {
             Element trueValue = parseBody(IF_TERMINATORS);
             Element falseValue = null;
             
             if (check("else")) {
                 if (peek("if")) {
                     falseValue = parseIf(null);
                     return bless(column, new IfElement(condition, trueValue, falseValue));
                 }
                 
                 if (check("}")) {
                     falseValue = parseBody(IF_TERMINATORS);
                 } else {
                     falseValue = parseExpression();
                 }
                 
                 expect("end");
                 check("if");
             } else {
                 expect("end");
                 check("if");
             }
 
             return bless(column, new IfElement(condition, trueValue, falseValue));
         } else if (then) {
             Element trueValue = parseExpression();
             Element falseValue = null;
             
             if (check("else")) {
                 if (peek("if")) {
                     falseValue = parseIf(null);
                 } else {
                     if (check("}")) {
                         falseValue = parseBody(IF_TERMINATORS);
                     } else {
                         falseValue = parseExpression();
                     }
                 }
             }
             
             if (check("end")) {
                 check("if");
             }
 
             return bless(column, new IfElement(condition, trueValue, falseValue));
             
         } else {
             return error("Unexpected symbol `" + in.getToken() + "'");
         }
     }
 
     private Element parseFor() throws IOException {
         expect("for");
         int column = pray();
 
         List<Element> generators = new ArrayList<Element>();
         for (;;) {
             generators.add(parseExpression());
             if (!check(",")) {
                 break;
             }
         }
         
         if (check("do")) {
             List<Element> functions = new ArrayList<Element>();
             Element otherwise = null;
             for (;;) {
                 functions.add(parseExpression());
                 
                 if (check(",")) {
                 } else {
                     if (check("else")) {
                         otherwise = parseExpression();
                     }
                     
                     return bless(column, new ForElement(generators.toArray(new Element[generators.size()]),
                                                 functions.toArray(new Element[functions.size()]),
                                                 true, otherwise));
                 }
             }
         } else if (check("in")) {
             String[] names = new String[generators.size()];
             for (int i = 0; i < generators.size(); i++) {
                 Element element = generators.get(i);
                 if (element instanceof ValueElement) {
                     names[i] = ((ValueElement)element).key;
                 } else {
                     error("Expected variable name instead of of expression in for");
                 }
             }
             
             List<Element> lists = new ArrayList<Element>();
             for (;;) {
                 lists.add(parseExpression());
                 if (!check(",")) {
                     break;
                 }
             }
             
             expect("do");
             
             List<Element> actions = new ArrayList<Element>();
             for (;;) {
                 actions.add(parseBody(FOR_TERMINATORS));
                 if (!check("do")) {
                     break;
                 }
             }
 
             Element otherwise = null;
             if (check("else")) {
                 otherwise = parseExpression();
             }
             
             expect("end");
             check("for");
             
             return bless(column, new ForInlineElement(names, lists.toArray(new Element[generators.size()]),
                     actions.toArray(new Element[actions.size()]),
                     otherwise));
             
         } else {
             return error("Unexpected symbol `" + in.getToken() + "'");
         }
     }
 
     private Element parseDefine() throws IOException {
         expect("macro");
         in.read();
         int column = pray();
         
         if (!in.isName() && !in.isSymbol("@")) {
             error("Expected macro name instead of `" + in.getToken() + "'");
         }
         boolean isModule = in.isSymbol("@");
         String name = in.getToken();
         
         List<String> parameters = new ArrayList<String>();
         List<Element> defaults = new ArrayList<Element>();
         
         if (!peek("}")) {
             for (;;) {
                 in.read();
                 if (in.isName()) {
                     parameters.add(in.getToken());
                     defaults.add(null);
                 } else if (in.isKeyword()) {
                     parameters.add(in.getToken());
                     defaults.add(parseBasic());
                 } else {
                     error("Expected parameter name instead of `" + in.getToken() + "'");
                 }
                 
                 if (peek("}")) {
                     break;
                 }
             }
         }
 
         String[] array = null;
         if (parameters.size() > 0) {
             array = parameters.toArray(new String[parameters.size()]);
         }
         
         Element[] _defaults = null;
         if (defaults.size() > 0) {
             _defaults = defaults.toArray(new Element[defaults.size()]);
         }
         
         if (isModule) {
             template.setDefaults(array, _defaults);
             return null;
         } else {
             Element body = parseBody(END_TERMINATOR);
             
             expect("end");
             check("macro");
     
             return bless(column, new DefineElement(name, new Template(body, array, _defaults)));
         }
     }
     
     private Element parseBuiltin() throws IOException {
         in.read();
         int column = pray();
             
         if (!in.isBuiltin()) {
             error("Expected builtin function name instead of `" + in.getToken() + "'");
         }
         
         String name = in.getToken();
         List<Element> parameters = new ArrayList<Element>();
 
         if (!checkEnd()) {
             Element value = parseBasic();
             parameters.add(value);
             for (;;) {
                 if (!checkEnd()) {
                     parameters.add(parseBasic());
                 } else {
                     break;
                 }
             }
         }
 
         Element[] elements = parameters.toArray(new Element[parameters.size()]);
         
         if ("tail".equals(name)) {
             return bless(column, new TailFunction(elements));
         } else if ("head".equals(name)) {
             return bless(column, new HeadFunction(elements));
         } else if ("append".equals(name)) {
             return bless(column, new AppendFunction(elements));
         } else if ("equal".equals(name)) {
             return bless(column, new EqualsFunction(elements));
         } else if ("join".equals(name)) {
             return bless(column, new JoinFunction(elements));
         } else if ("lookup".equals(name)) {
             return bless(column, new LookupFunction(elements));
         } else if ("shuffle".equals(name)) {
             return bless(column, new ShuffleFunction(elements));
         } else if ("strip".equals(name)) {
             return bless(column, new StripFunction(elements));
         } else if ("flatten".equals(name)) {
             return bless(column, new FlattenFunction(elements));
         } else if ("length".equals(name)) {
             return bless(column, new LengthFunction(elements));
         } else if ("split".equals(name)) {
             return bless(column, new SplitFunction(elements));
         } else if ("reverse".equals(name)) {
             return bless(column, new ReverseFunction(elements));
         } else if ("sort".equals(name)) {
             return bless(column, new SortFunction(elements));
         } else if ("include".equals(name)) {
             return bless(column, new IncludeElement(template, elements));
         } else if ("xml".equals(name)) {
             return bless(column, new XMLFunction(elements));
         } else if ("url".equals(name)) {
             return bless(column, new URLFunction(elements));
         } else {
             return error("builtin");
         }
     }
 
     private Element parseExpression() throws IOException {
         in.peek();
         int column = pray();
         return parseOperatorsOr();
     }
     
     private Element parseOperatorsOr()  throws IOException {
         in.peek();
         int column = pray();
 
         Element element = parseOperatorsAnd();
         in.peek();
 
         if (in.isSymbol("||")) {
             List<Element> parameters = new ArrayList<Element>();
             parameters.add(element);
             
             while (in.isSymbol("||")) {
                 in.read();
                 parameters.add(parseOperatorsAnd());
                 in.peek();
             }
             
             element = bless(column, new OrElement(parameters.toArray(new Element[parameters.size()])));
         }
         
         return element;
     }
 
     private Element parseOperatorsAnd()  throws IOException {
         in.peek();
         int column = pray();
 
         Element element = parseOperatorsJoin();
         in.peek();
         if (in.isSymbol("&&")) {
             List<Element> parameters = new ArrayList<Element>();
             parameters.add(element);
             
             while (in.isSymbol("&&")) {
                 in.read();
                 parameters.add(parseOperatorsJoin());
                 in.peek();
             }
             
             element = bless(column, new AndElement(parameters.toArray(new Element[parameters.size()])));
         }
         
         return element;
     }
 
     private Element parseOperatorsJoin()  throws IOException {
         in.peek();
         int column = pray();
 
         Element element = parseUnary();
         in.peek();
         if (in.isSymbol("+")) {
             List<Element> parameters = new ArrayList<Element>();
             parameters.add(element);
             
             while (in.isSymbol("+")) {
                 in.read();
                 parameters.add(parseUnary());
                 in.peek();
             }
             
             element = bless(column, new AppendFunction(parameters.toArray(new Element[parameters.size()])));
         }
         
         return element;
     }
     
     private Element parseApply() throws IOException {
         in.peek();
         int column = pray();
 
         Element element = parseBasic();
         
         if (!checkEnd()) {
             List<String> keys;
             List<Element> parameters = new ArrayList<Element>();
             
             in.peek();
             if (in.isKeyword()) {
                 keys = new ArrayList<String>();
                 
                 for (;;) {
                     if (!checkEnd()) {
                         in.read();
                         if (!in.isKeyword()) {
                             error("Expected keyword parameter name instead `" + in.getToken() + "'");
                         }
                         
                         keys.add(in.getToken());
                         parameters.add(parseBasic());
                     } else {
                         break;
                     }
                 }
                 
             } else {
                 keys = null;
                 for (;;) {
                     if (!checkEnd()) {
                         parameters.add(parseBasic());
                     } else {
                         break;
                     }
                 }
             }
                  
             element = bless(column, new ApplyElement(element,
                                              parameters.toArray(new Element[parameters.size()]),
                                              keys == null ? null : keys.toArray(new String[keys.size()])));
        } else if (element instanceof InlineElement) {
            element = bless(column, new ApplyElement(element, new Element[0]));
         }
         
         return element;
     }
 
     private Element parseUnary() throws IOException {
         in.peek();
         int column = pray();
 
         boolean isNot = check("!");
         Element element = parseApply();
 
         if (isNot) {
             element = bless(column, new NotElement(element));
         }
         
         return element;
     }
     
     private Element parseBasic() throws IOException {
         Element element = null;
 
         in.peek();
         int column = pray();
 
         if (in.isSymbol("[")) {
             element = parseList();
         } else if (in.isSymbol("true")) {
             in.read();
             return bless(column, new ConstantElement(true));
         } else if (in.isSymbol("false")) {
             in.read();
             return bless(column, new ConstantElement(false));
         } else if (in.isBuiltin()) {
             return parseBuiltin();
         } else if (in.isSymbol("(")) {
             element = parseParenthesis();
         } else if (in.isSymbol("@")) {
             return parseInline();
         } else if (in.isSymbol("macro")) {
             return parseInlineFunction();
         } else {
             in.read();
             
             if (in.isString()) {
                 element = bless(column, new TextElement(in.getToken()));
                 return element;
             } else if (in.isNumber()) {
                 element = bless(column, new ConstantElement(new Long(in.getToken())));
             } else if (in.isName()) {
                 element = bless(column, new ValueElement(in.getToken()));
             } else {
                 error("Unexpected symbol `" + in.getToken() + "'");
             }
         }
 
         while (check(".")) {
             in.read();
             if (in.isSymbol("(")) {
                 element = bless(column, new IndirectPropertyElement(element, parseExpression()));
                 expect(")");
             } else if (in.isName()) {
                 String name = in.getToken();
                 element = bless(column, new PropertyElement(element, name));
             } else {
                 error("Expected property name instead of `" + in.getToken() + "'");
             }
         }
 
         return element;
     }
     
     private Element parseInline() throws IOException {
         in.peek();
         int column = pray();
 
         check("@");
         StringBuilder out = new StringBuilder();
         
         in.read();
         if (!in.isName()) {
             error("Expected identifier instead of `" + in.getToken() + "'");
         }
         
         out.append(in.getToken());
         
         while (check(".")) {
             in.read();
             if (!in.isName()) {
                 error("Expected identifier instead of `" + in.getToken() + "'");
             } else {
                 out.append("/");
                 out.append(in.getToken());
             }
         }
         
         return bless(column, new InlineElement(template, out.toString()));
     }
 
     private Element parseInlineFunction() throws IOException {
         in.peek();
         int column = pray();
 
         check("macro");
         
         List<String> parameters = new ArrayList<String>();
         List<Element> defaults = new ArrayList<Element>();
         
         if (!check("}")) {
             for (;;) {
                 in.read();
                 if (in.isName()) {
                     parameters.add(in.getToken());
                     defaults.add(null);
                 } else if (in.isKeyword()) {
                     parameters.add(in.getToken());
                     defaults.add(parseBasic());
                 } else {
                     error("Expected parameter name instead of `" + in.getToken() + "'");
                 }
                 
                 if (check("}")) {
                     break;
                 }
             }
         }
 
         String[] array = null;
         if (parameters.size() > 0) {
             array = parameters.toArray(new String[parameters.size()]);
         }
         
         Element[] _defaults = null;
         if (defaults.size() > 0) {
             _defaults = defaults.toArray(new Element[defaults.size()]);
         }
         
         Element body = parseBody(END_TERMINATOR);
         expect("end");
         check("macro");
 
         return bless(column, new ConstantElement(new Template(body, array, _defaults)));
     }
     
     
     private Element parseParenthesis() throws IOException {
         in.peek();
         int column = pray();
 
         check("(");
         
         if (check(")")) {
             return bless(column, new ConstantElement(null));
         }
         
         Element element = parseSubStatement();
             
         if (check(";")) {
             List<Element> ls = new ArrayList<Element>();
             ls.add(element);
           
             for (;;) {
                 if (check(")")) {
                     return new SequenceElement(ls);
                 }
                 
                 ls.add(parseSubStatement());
                 
                 if (check(")")) {
                     return new SequenceElement(ls);
                 } else if (!check(";")) {
                     error("Expected `;' instead of `" + in.getToken() + "'");
                 }
             }
         } else {
             expect(")");
         }
         return element;
     }
     
     public static void main(String[] args) {
         TemplateContext context = new TemplateContext();
         Template template = null;
         try {
            template = context.find("/home/manuel4/test/lemon/test.txt");
             context.set("poop", "Hello");
             context.set("obj", new Lobster());
             
             Map map = new HashMap();
             map.put("person", "Jim");
             
             context.set("map", map);
             
             context.set("jump", new Element() {
                 @Override
                 public Object evaluate(TemplateContext model) throws Exception {
                     return "I jump";
                 }
             });
             
             context.set("flop", new TemplateFunction() {
                 public Object evaluate(Object[] parameters) {
                     if (parameters.length > 0) {
                         return ("" + parameters[0]).toUpperCase();
                     }
                     return null;
                 }
             });
             
             StringWriter out = new StringWriter();
             
             long last = System.currentTimeMillis();
             if (template != null) {
                 template.print(out, context);
             }
             System.out.println("Rendering Time: " + (System.currentTimeMillis() - last));
             
             System.out.println(out.toString());
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
     
     public static class Lobster implements Renderable {
         public Object render(TemplateRenderer context) {
             return new Object() {
                 private String name = "Big lobster";
                 private String jim  = "Small lobster";
             };
         }
     }
 }
