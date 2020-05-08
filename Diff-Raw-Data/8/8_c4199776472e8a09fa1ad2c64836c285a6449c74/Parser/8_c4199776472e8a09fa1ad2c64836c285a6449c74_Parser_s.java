 package parser;
 import java.io.*; import lexer.*; import symbols.*; import inter.*; import java.util.*;
 
 public class Parser {
 
    private Lexer lex;    // lexical analyzer for this parser
    private Token look;   // lookahead tagen
    private Function curF = null;    // processing function
    private Function main = null;    // main function
    private LinkedList<Call> calls;
    Env top = null;       // current or top symbol table
    int used = 0;         // storage used for declarations
 
    public Parser(Lexer l) throws IOException { lex = l; move(); }
 
    void move() throws IOException { look = lex.scan(); }
 
    void error(String s) { throw new Error("near line "+lex.line+": "+s); }
 
    void match(int t) throws IOException {
       if( look.tag == t ) move();
       else if(t<256) error("Syntax error. Expected "+(char)t+", got "+(char)look.tag);
       else error("Syntax error. Expected "+t+", got "+look.tag);
    }
 
    public void program() throws IOException {  // program -> block
       top = new Env(top);
       Stmt s = stmts();
       if(main==null) throw new Error("No main()");
       
       int begin = s.newlabel();  int after = s.newlabel();
 		s.emitlabel(begin);
 		s.emit("push " + after); // return point
 		s.emit("goto " + main.label);
 		s.gen(begin, after);
 		s.emitlabel(after);
    }
 
    Stmt block() throws IOException {  // block -> { decls stmts }
       match('{');  Env savedEnv = top;  top = new Env(top);
       while( look.tag == Tag.BASIC ) decl();
       Stmt s = stmts();
       match('}');  top = savedEnv;
       return s;
    }
 
    Stmt decl() throws IOException {
       Type p = type(); Token tok = look; match(Tag.ID);
       
       if( look.tag == '(' ) {
             Function f = new Function((Word)tok, p);
		  	curF = f;
             if( f.name.equals("main") ) main = f;
 
		  	top.put( tok, f );
             Env savedEnv = top;
             top = new Env(top);
          
             match('(');
             while( look.tag != ')' ) {
                   if( f.name.equals("main") ) throw new Error("Main should not have arguments()");
                   if(look.tag==',') match(',');
                   Type argt = type();
                   Token arg = look;
                   match(Tag.ID);
				  Id id = new Id((Word)arg, argt, used);
                   f.addArgument((Word)arg, argt);
                   top.put((Word)arg, id);
                   used = used + p.width;
             }
             match(')');
             
             match('{');
             Stmt s = stmts();
             f.setBody(s, top);
             match('}');
             
             top = savedEnv;
             return f;
       } else {
             Id id = new Id((Word)tok, p, used);
             top.put( tok, id );
             used = used + p.width;
             match(';');
 
             return Stmt.Null;
       }
    }
 
    Type type() throws IOException {
       Type p = (Type)look;            // expect look.tag == Tag.BASIC 
       match(Tag.BASIC);
       if( look.tag != '[' ) return p; // T -> basic
       else return dims(p);            // return array type
    }
 
    Type dims(Type p) throws IOException {
       match('[');  Token tok = look;  match(Tag.NUM);  match(']');
       if( look.tag == '[' )
             p = dims(p);
       return new Array(((Num)tok).value, p);
    }
 
    Stmt stmts() throws IOException {
       if ( look.tag == '}' || look.tag==Tag.EOF ) return Stmt.Null;
       else return new Seq(stmt(), stmts());
    }
 
    Stmt stmt() throws IOException {
       Expr x;  Stmt s, s1, s2;
       Stmt savedStmt;         // save enclosing loop for breaks
 
       switch( look.tag ) {
 
       case Tag.BASIC:
          return decl();
 
       case ';':
          move();
          return Stmt.Null;
 
       case Tag.IF:
          match(Tag.IF); match('('); x = bool(); match(')');
          s1 = stmt();
          if( look.tag != Tag.ELSE ) return new If(x, s1);
          match(Tag.ELSE);
          s2 = stmt();
          return new Else(x, s1, s2);
 
       case Tag.WHILE:
          While whilenode = new While();
          savedStmt = Stmt.Enclosing; Stmt.Enclosing = whilenode;
          match(Tag.WHILE); match('('); x = bool(); match(')');
          s1 = stmt();
          whilenode.init(x, s1);
          Stmt.Enclosing = savedStmt;  // reset Stmt.Enclosing
          return whilenode;
 
       case Tag.DO:
          Do donode = new Do();
          savedStmt = Stmt.Enclosing; Stmt.Enclosing = donode;
          match(Tag.DO);
          s1 = stmt();
          match(Tag.WHILE); match('('); x = bool(); match(')'); match(';');
          donode.init(s1, x);
          Stmt.Enclosing = savedStmt;  // reset Stmt.Enclosing
          return donode;
 
 	  case Tag.PUTS:
 			match(Tag.PUTS);
 			x = bool();
 			match(';');
 			return new Puts(x);
       case Tag.BREAK:
          match(Tag.BREAK); match(';');
          return new Break();
 
       case Tag.RETURN:
             match(Tag.RETURN);
             x = bool();
             match(';');
             return new Return(x, curF);
 
       case '{':
          return block();
 
       default:
          return assign();
       }
    }
 
    Stmt assign() throws IOException {
       Stmt stmt;  Token t = look;
       match(Tag.ID);
       Id id = top.get(t);
       if( id == null ) error(t.toString() + " undeclared");
 
       if( look.tag == '=' ) {       // S -> id = E ;
          move();  stmt = new inter.Set(id, bool());
       }
       else {                        // S -> L = E ;
          Access x = offset(id);
          match('=');  stmt = new SetElem(x, bool());
       }
       match(';');
       return stmt;
    }
 
    Expr bool() throws IOException {
 	  LinkedList<Call> savedCalls = calls;
       calls = new LinkedList();
 	   
       Expr x = join();
       while( look.tag == Tag.OR ) {
          Token tok = look;  move();  x = new Or(tok, x, join());
       }
 	  
 	  if(savedCalls==null) { // that was a root of the expression
 		x.addBeforeStmts(calls);
 		calls = null;
       } else {
 		calls.addAll(savedCalls);
       }
       return x;
    }
 
    Expr join() throws IOException {
       Expr x = equality();
       while( look.tag == Tag.AND ) {
          Token tok = look;  move();  x = new And(tok, x, equality());
       }
       return x;
    }
 
    Expr equality() throws IOException {
       Expr x = rel();
       while( look.tag == Tag.EQ || look.tag == Tag.NE ) {
          Token tok = look;  move();  x = new Rel(tok, x, rel());
       }
       return x;
    }
 
    Expr rel() throws IOException {
       Expr x = expr();
       switch( look.tag ) {
       case '<': case Tag.LE: case Tag.GE: case '>':
          Token tok = look;  move();  return new Rel(tok, x, expr());
       default:
          return x;
       }
    }
 
    Expr expr() throws IOException {
       Expr x = term();
       while( look.tag == '+' || look.tag == '-' ) {
          Token tok = look;  move();  x = new Arith(tok, x, term());
       }
       return x;
    }
 
    Expr term() throws IOException {
       Expr x = unary();
       while(look.tag == '*' || look.tag == '/' ) {
          Token tok = look;  move();   x = new Arith(tok, x, unary());
       }
       return x;
    }
 
    Expr unary() throws IOException {
       if( look.tag == '-' ) {
          move();  return new Unary(Word.minus, unary());
       }
       else if( look.tag == '!' ) {
          Token tok = look;  move();  return new Not(tok, unary());
       }
       else return factor();
    }
 
    Expr factor() throws IOException {
       Expr x = null;
       switch( look.tag ) {
       case '(':
          move(); x = bool(); match(')');
          return x;
       case Tag.NUM:
          x = new Constant(look, Type.Int);    move(); return x;
       case Tag.REAL:
          x = new Constant(look, Type.Float);  move(); return x;
       case Tag.TRUE:
          x = Constant.True;                   move(); return x;
       case Tag.FALSE:
          x = Constant.False;                  move(); return x;
       default:
          error("syntax error");
          return x;
       case Tag.ID:
          Token tok = look;
          Id id;
          match(Tag.ID);
 		
          if(look.tag=='(') { // fuction call
             match('(');
 
             Function f = top.getFunc(tok);
             if( f == null ) error(tok.toString() + " undeclared");
             
             id = new Temp(f.type); // var for returned value
             top.put(new Word(id.toString(), Tag.TEMP), id); // add to local vars
             
             Call call = new Call(f, id, curF);
             
             while( look.tag != ')' ) {
                if(look.tag==',') match(',');
                x = bool();
                if( !call.addArg(x) ) error("Wrong argumnet type or too many args");
             }
 			
             match(')');
 			if(!call.checkArg()) error("Wrong argumnet count");
             calls.add(call);
          } else {
             id = top.get(tok);
             if( id == null ) error(tok.toString() + " undeclared");
          }
          if( look.tag != '[' ) return id;
          else return offset(id);
       }
    }
 
    Access offset(Id a) throws IOException {   // I -> [E] | [E] I
       Expr i; Expr w; Expr t1, t2; Expr loc;  // inherit id
 
       Type type = a.type;
       match('['); i = bool(); match(']');     // first index, I -> [ E ]
       type = ((Array)type).of;
       w = new Constant(type.width);
       t1 = new Arith(new Token('*'), i, w);
       loc = t1;
       while( look.tag == '[' ) {      // multi-dimensional I -> [ E ] I
          match('['); i = bool(); match(']');
          type = ((Array)type).of;
          w = new Constant(type.width);
          t1 = new Arith(new Token('*'), i, w);
          t2 = new Arith(new Token('+'), loc, t1);
          loc = t2;
       }
 
       return new Access(a, loc, type);
    }
 }
