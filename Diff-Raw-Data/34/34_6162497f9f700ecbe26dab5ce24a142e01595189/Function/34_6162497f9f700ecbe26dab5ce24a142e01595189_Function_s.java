 package xi.go.cst.prim;
 
 import static xi.go.cst.Thunk.app;
 
 import java.math.BigInteger;
 
 import xi.go.cst.App;
 import xi.go.cst.Node;
 import xi.go.cst.Thunk;
 
 /**
  * Built-in functions of the SK interpreter.
  * 
  * @author Leo
  * @author Joschi
  */
 public final class Function extends Prim {
 
     /**
      * Enumeration of all built-in functions.
      * 
      * @author Leo
      */
     public static enum Def {
 
         /** The substitution combinator. */
         S('S', 3) {
             @Override
             public Node apply(final Thunk... args) {
                 final Thunk f = args[0], g = args[1], x = args[2];
                 return new App(app(f, x), app(g, x));
             }
         },
 
         /** Another substitution combinator. */
         S_PRIME('z', 4) {
             @Override
             public Node apply(final Thunk... args) {
                 final Thunk c = args[0], f = args[1], g = args[2], x = args[3];
                 return new App(app(c, app(f, x)), app(g, x));
             }
         },
 
         /** The right-side substitution combinator. */
         B('B', 3) {
             @Override
             public Node apply(final Thunk... args) {
                 final Thunk f = args[0], g = args[1], x = args[2];
                 return new App(f, app(g, x));
             }
         },
 
         /** The right-side substitution combinator. */
         B_STAR('b', 4) {
             @Override
             public Node apply(final Thunk... args) {
                final Thunk c = args[0], f = args[1], g = args[2], x = args[2];
                 return new App(c, app(f, app(g, x)));
             }
         },
 
         /** The left-side substitution combinator. */
         C('C', 3) {
             @Override
             public Node apply(final Thunk... args) {
                 final Thunk f = args[0], g = args[1], x = args[2];
                 return new App(app(f, x), g);
             }
         },
 
         /** Another left-side substitution combinator. */
         C_PRIME('v', 4) {
             @Override
             public Node apply(final Thunk... args) {
                 final Thunk c = args[0], f = args[1], g = args[2], x = args[3];
                 return new App(app(c, app(f, x)), g);
             }
         },
 
         /** The constant combinator. */
         K('K') {
             @Override
             public Node apply(final Thunk... args) {
                 return indirect(args[0]);
             }
         },
 
         /** The identity combinator. */
         I('I', 1) {
             @Override
             public Node apply(final Thunk... args) {
                 throw new IllegalStateException();
                 // return args[0].getNode();
             }
         },
 
         /** The recursion combinator. */
         Y('Y', 1) {
             @Override
             public Node apply(final Thunk... args) {
                 return App.loop(args[0]);
             }
         },
 
         /** The uncurry combinator. */
         U('U') {
             @Override
             public Node apply(final Thunk... args) {
                 final Thunk f = args[0];
                 final Thunk z = args[1];
                 return new App(app(f, app(HEAD.thunk, z)), app(TAIL.thunk, z));
             }
         },
 
         /** Addition. */
         ADD('+') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger a = args[0].wHNF().getNum();
                 final BigInteger b = args[1].wHNF().getNum();
                 return new Num(a.add(b));
             }
         },
 
         /** Subtraction. */
         SUB('-') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger a = args[0].wHNF().getNum();
                 final BigInteger b = args[1].wHNF().getNum();
                 return new Num(a.subtract(b));
             }
         },
 
         /** Multiplication. */
         MUL('*') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger a = args[0].wHNF().getNum();
                 final BigInteger b = args[1].wHNF().getNum();
                 return new Num(a.multiply(b));
             }
         },
 
         /** Division. */
         DIV('/') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger a = args[0].wHNF().getNum();
                 final BigInteger b = args[1].wHNF().getNum();
                 return new Num(a.divide(b));
             }
         },
 
         /** Modulo. */
         MOD('%') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger a = args[0].wHNF().getNum();
                 final BigInteger b = args[1].wHNF().getNum();
                 return new Num(a.mod(b));
             }
         },
 
         /** Logical negation. */
         NOT('!', 1) {
             @Override
             public Node apply(final Thunk... args) {
                 return Bool.get(!args[0].wHNF().getBool());
             }
         },
 
         /** Numeric negation. */
         NEG('~', 1) {
             @Override
             public Node apply(final Thunk... args) {
                 return new Num(args[0].wHNF().getNum().negate());
             }
         },
 
         /** Conditional operator. */
         COND('?', 3) {
             @Override
             public Node apply(final Thunk... args) {
                 return indirect(args[args[0].wHNF().getBool() ? 1 : 2]);
             }
         },
 
         /** List construction. */
         CONS(':') {
             @Override
             public Node apply(final Thunk... args) {
                 return new List(args[0], args[1]);
             }
         },
 
         /** Head of a list. */
         HEAD('h', 1) {
             @Override
             public Node apply(final Thunk... args) {
                 // get the cons node
                 return indirect(args[0].wHNF().getHead());
             }
         },
 
         /** Tail of a list. */
         TAIL('t', 1) {
             @Override
             public Node apply(final Thunk... args) {
                 // get the cons node
                 return indirect(args[0].wHNF().getTail());
             }
         },
 
         /** Less-than operator. */
         LT('L') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger x = args[0].wHNF().getNum();
                 final BigInteger y = args[1].wHNF().getNum();
                 return Bool.get(x.compareTo(y) < 0);
             }
         },
 
         /** Less-than-or-equal operator. */
         LTE('l') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger x = args[0].wHNF().getNum();
                 final BigInteger y = args[1].wHNF().getNum();
                 return Bool.get(x.compareTo(y) <= 0);
             }
         },
 
         /** Equality operator. */
         EQ('e') {
             @Override
             public Node apply(final Thunk... args) {
                 final Value x = args[0].wHNF();
                 final Value y = args[1].wHNF();
                 return Bool.get(x.eq(y));
             }
         },
 
         /** Non-equality operator. */
         NEQ('n') {
             @Override
             public Node apply(final Thunk... args) {
                 final Value x = args[0].wHNF();
                 final Value y = args[1].wHNF();
                 return Bool.get(!x.eq(y));
             }
         },
 
         /** Greater-than-or-equal operator. */
         GTE('g') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger x = args[0].wHNF().getNum();
                 final BigInteger y = args[1].wHNF().getNum();
                 return Bool.get(x.compareTo(y) >= 0);
             }
         },
 
         /** Greater-than operator. */
         GT('G') {
             @Override
             public Node apply(final Thunk... args) {
                 final BigInteger x = args[0].wHNF().getNum();
                 final BigInteger y = args[1].wHNF().getNum();
                 return Bool.get(x.compareTo(y) > 0);
             }
         },
 
         /** Logical conjunction. */
         AND('&') {
             @Override
             public Node apply(final Thunk... args) {
                 return indirect(args[args[0].wHNF().getBool() ? 1 : 0]);
             }
         },
 
         /** Logical disjunction. */
         OR('|') {
             @Override
             public Node apply(final Thunk... args) {
                 return indirect(args[args[0].wHNF().getBool() ? 0 : 1]);
             }
         },
 
         /** Sequence operator. */
         SEQ('s') {
             @Override
             public Node apply(final Thunk... args) {
                 args[0].wHNF();
                 return indirect(args[1]);
             }
         },
 
         /** Character casting operator. */
         CHAR('c', 1) {
             @Override
             public Node apply(final Thunk... args) {
                 return Thunk.chr(args[0].wHNF().getNum().intValue()).wHNF();
             }
         },
 
         /**
          * Does nothing -- is used to find duplicate symbol definitions in the
          * SK-Code.
          */
         NOP(' ') {
             @Override
             public Node apply(final Thunk... args) {
                 throw new IllegalStateException("nop should not be used!!!");
             }
         },
 
         ;
 
         /** Jump table from character code (ASCII) to definition. */
         private static final Def[] FUN_MAP = new Def[1 << 7];
         static {
             for (final char c : "i=\"<@TF_,'\r\n\t".toCharArray()) {
                 FUN_MAP[c] = NOP;
             }
 
             for (final Def fun : values()) {
                 if (FUN_MAP[fun.name] != null) {
                     throw new IllegalStateException(
                             "duplicate symbol definition! " + fun.name);
                 }
                 FUN_MAP[fun.name] = fun;
             }
         }
 
         /** The function's cardinality. */
         public final int cardinality;
 
         /** the function's name. */
         public final char name;
 
         /** The function's surrounding thunk. */
         public final Thunk thunk;
 
         /**
          * Constructor taking the function's name and cardinality.
          * 
          * @param f
          *            function name
          * @param n
          *            cardinality
          */
         private Def(final char f, final int n) {
             name = f;
             cardinality = n;
             thunk = new Thunk(new Function(this));
         }
 
         /**
          * Constructor taking the function's name, the cardinality is assumed to
          * be 2.
          * 
          * @param f
          *            function name
          */
         private Def(final char f) {
             this(f, 2);
         }
 
         /**
          * Applies this function to the given arguments.
          * 
          * @param args
          *            argument array
          * @return resulting node
          */
         public abstract Node apply(final Thunk... args);
 
         /**
          * Creates an indirection node to the {@link Thunk} {@code th}, i.e.
          * {@code (I @ th)}.
          * 
          * @param th
          *            Thunk to wrap
          * @return the indirection node
          */
         private static Node indirect(final Thunk th) {
             return new App(I.thunk, th);
         }
 
     }
 
     /** Definition of this function. */
     private final Def fun;
 
     /**
      * Constructor taking the definition of this function.
      * 
      * @param f
      *            function definition
      */
     private Function(final Def f) {
         fun = f;
     }
 
     /**
      * Getter for the function definition associated with the given character.
      * 
      * @param name
      *            character from the SK code
      * @return corresponding {@link Function.Def definition} of {@code null}, if
      *         no such function exists
      */
     public static Def forChar(final char name) {
         return Def.FUN_MAP[name];
     }
 
     @Override
     public String toString() {
         return fun.toString();
     }
 
     @Override
     public Def getFunction() {
         return fun;
     }
 
     @Override
     public boolean equals(final Object obj) {
         return obj instanceof Function && fun == ((Function) obj).fun;
     }
 
     @Override
     public int hashCode() {
         return fun.hashCode();
     }
 
 }
