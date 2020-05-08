 package batch.syntax;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import batch.Op;
 import batch.util.BatchFactory;
 
 public class Parser<E> {
   BatchFactory<E> f;
   Lexer in;
   Symbol kind;
   Object data;
 
   private static int numSymbol;
   private static Map<String, Symbol> special = new HashMap<String, Symbol>();
 
   public static enum Symbol {
     // from lexer
     EOF(null), ID(null), NUMBER(null), STRING(null),
     // keywords
     VAR("var"), FOR("for"), IN("in"), FUNCTION("function"), IF("if"), ELSE(
         "else"), DATE("date"), TRUE("true"), FALSE("false"), OUTPUT("OUTPUT"), OR(
         "||"), AND("&&"), EQ("=="), NE("!="), LE("<="), LT("<"), GT(">"), GE(
         ">="), SEMI(";"), COMMA(","), ASSIGN("="), LCURLY("{"), RCURLY("}"), LPAREN(
         "("), RPAREN(")"), ADD("+"), SUB("-"), MUL("*"), DIV("/"), DOT("."), NOT(
         "!"), INPUT("INPUT");
 
     public final String text;
     public final int index;
 
     private Symbol(String text) {
       this.text = text;
       this.index = numSymbol++;
       special.put(text, this);
     }
   }
 
   static public <E> E parse(String script, BatchFactory<E> f) {
     Parser<E> p = new Parser<E>(script, f);
     return p.main();
   }
 
   public Parser(String script, BatchFactory<E> f) {
     Symbol.values(); // required to create the enum!
     this.f = f;
     in = new Lexer(script);
     next();
   }
 
   void next() {
     Lexer.Token current = in.next();
     switch (current.type) {
     case EOF:
       kind = Symbol.EOF;
       data = null;
       break;
     case STRING:
       kind = Symbol.STRING;
       data = current.data;
       break;
     case NUMBER:
       kind = Symbol.NUMBER;
       String str = (String) current.data;
       if (str.indexOf('.') != -1 || str.indexOf('e') != -1)
         data = new Float(str);
       else
         data = new Integer(str);
       break;
     case SYMBOL:
       kind = special.get(current.data);
       if (kind == null) {
         kind = Symbol.ID;
         data = current.data;
       }
     }
   }
 
   boolean match(Symbol c) {
     if (kind == c) {
       next();
       return true;
     }
     return false;
   }
 
   String ID() {
     String val = null;
     if (kind == Symbol.ID)
       val = (String) data;
     else
       throw new Error("Expected identifier");
     next();
     return val;
   }
 
   private String String() {
     String val = null;
     if (kind == Symbol.STRING)
       val = (String) data;
     else
       throw new Error("Expected string");
     next();
     return val;
   }
 
   private void require(Symbol c) {
     if (!match(c))
       throw new Error("Expected " + c);
   }
 
   /*
    * 
         if(tokenizer.ttype == StreamTokenizer.TT_WORD) {
             System.out.println(tokenizer.sval);
         } else if(tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
             System.out.println(tokenizer.nval);
         } else if(tokenizer.ttype == StreamTokenizer.TT_EOL) {
             System.out.println();
         }
 
    */
   E main() {
     E value = statements();
     if (kind != Symbol.EOF)
       throw new Error("Expected EOF");
     return value;
   }
 
   E statements() {
     E value = statement();
     if (match(Symbol.SEMI)) {
       E es = statements();
       if (es != null)
         value = f.Prim(batch.Op.SEQ, value, es);
     }
     return value;
   }
 
   E block() {
     require(Symbol.LCURLY);
     E e = statements();
     require(Symbol.RCURLY);
     return e;
   }
 
   E statement() {
     if (match(Symbol.FOR)) {
      match(Symbol.LPAREN);
       String v = ID();
      match(Symbol.IN);
       E e = expr();
      match(Symbol.RPAREN);
       E b = block();
       return f.Loop(v, e, b);
     } else if (match(Symbol.VAR)) {
       String v = ID();
       require(Symbol.ASSIGN);
       E e = expr();
       require(Symbol.SEMI);
       E b = statements();
       return f.Let(v, e, b);
     } else
       return expr();
   }
 
   E expr() {
     E a = and();
     if (match(Symbol.OR)) {
       E b = expr();
       return f.Prim(batch.Op.OR, a, b);
     }
     return a;
   }
 
   E and() {
     E a = comp();
     if (match(Symbol.AND)) {
       E b = and();
       return f.Prim(batch.Op.AND, a, b);
     }
     return a;
   }
 
   E comp() {
     E a = term();
     batch.Op op = compop();
     if (op != null) {
       E b = comp();
       return f.Prim(op, a, b);
     }
     return a;
   }
 
   batch.Op compop() {
     if (match(Symbol.EQ))
       return batch.Op.EQ;
     if (match(Symbol.NE))
       return batch.Op.NE;
     if (match(Symbol.LE))
       return batch.Op.LE;
     if (match(Symbol.LT))
       return batch.Op.LT;
     if (match(Symbol.GT))
       return batch.Op.GT;
     if (match(Symbol.GE))
       return batch.Op.GE;
     return null;
   }
 
   E term() {
     E a = factor();
     batch.Op op = addop();
     if (op != null) {
       E b = term();
       return f.Prim(op, a, b);
     }
     return a;
   }
 
   batch.Op addop() {
     if (match(Symbol.ADD))
       return batch.Op.ADD;
     if (match(Symbol.SUB))
       return batch.Op.SUB;
     return null;
   }
 
   E factor() {
     E a = base();
     batch.Op op = mulop();
     if (op != null) {
       E b = factor();
       return f.Prim(op, a, b);
     }
     return a;
   }
 
   batch.Op mulop() {
     if (match(Symbol.MUL))
       return batch.Op.MUL;
     if (match(Symbol.DIV))
       return batch.Op.DIV;
     return null;
   }
 
   E base() {
     E e;
     switch (kind) {
     case NUMBER:
       Object d = data;
       next();
       return f.Data(d);
     case NOT: {
       next();
       e = base();
       return f.Prim(batch.Op.NOT, e);
     }
     case LPAREN: {
       next();
       e = expr();
      match(Symbol.RPAREN);
       return e;
     }
     case STRING: {
       String str = (String) data;
       str = str.replace("\\\"", "\"");
       str = str.replace("\\\\", "\\");
       // TODO: handle unicode characters!???
       next();
       return f.Data(str);
     }
     case FUNCTION: {
       next();
      match(Symbol.LPAREN);
       String v = ID();
      match(Symbol.RPAREN);
       e = block();
       return f.Fun(v, e);
     }
     case IF: {
       next();
      match(Symbol.LPAREN);
       E a = expr();
      match(Symbol.RPAREN);
       E b = block();
       if (match(Symbol.ELSE)) {
         E c = block();
         return f.If(a, b, c);
       }
       return f.If(a, b, f.Skip());
     }
     case DATE: {
       next();
      match(Symbol.LPAREN);
       String str = String();
      match(Symbol.RPAREN);
       return f.Data(java.sql.Date.valueOf(str));
     }
     case TRUE:
       next();
       return f.Data(true);
     case FALSE:
       next();
       return f.Data(false);
     default: {
       E a = prim();
       if (match(Symbol.ASSIGN)) {
         E b = expr();
         return f.Assign(a, b);
       }
       return a;
     }
     }
   }
 
   E prim() {
     if (match(Symbol.OUTPUT)) {
       require(Symbol.LPAREN);
       String str = String();
       require(Symbol.COMMA);
       E e = expr();
       require(Symbol.RPAREN);
       return f.Out(str, e);
     } else if (match(Symbol.INPUT)) {
       require(Symbol.LPAREN);
       String str = String();
       require(Symbol.RPAREN);
       return f.In(str);
     } else {
       E value = f.Var(ID());
       return access(value);
     }
   }
 
   // Prop and Call
   E access(E value) {
     String field = null;
     if (match(Symbol.DOT)) {
       field = ID();
       if (match(Symbol.LPAREN)) {
         List<E> args = new ArrayList<E>();
         if (!match(Symbol.RPAREN)) {
           E e = expr();
           args.add(e);
           while (match(Symbol.COMMA)) {
             e = expr();
             args.add(e);
           }
         }
        match(Symbol.RPAREN);
         value = f.Call(value, field, args);
       } else
         value = f.Prop(value, field);
     }
     return field == null ? value : access(value);
   }
 
   public static <E> E binary(BatchFactory<E> factory, Op op, E c1, E c2) {
     if (c1 == null)
       return c2;
     if (c2 == null)
       return c1;
     List<E> args = new ArrayList<E>(2);
     args.add(c1);
     args.add(c2);
     return factory.Prim(op, args);
   }
 
 }
