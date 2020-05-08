 //### This file created by BYACC 1.8(/Java extension  1.15)
 //### Java capabilities added 7 Jan 97, Bob Jamison
 //### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
 //###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
 //###           01 Jun 99  -- Bob Jamison -- added Runnable support
 //###           06 Aug 00  -- Bob Jamison -- made state variables class-global
 //###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
 //###           16 May 01  -- Bob Jamison -- added custom stack sizing
 //###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
 //###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
 //### Please send bug reports to tom@hukatronic.cz
 //### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";
 
 
 
 
 
 
 //#line 2 "grammar.txt"
 package Sintactic;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.StringTokenizer;
 
 import Lexical.LexicalAnalizer;
 import Lexical.Token;
 import Lexical.ErrorHandler;
 import Lexical.Error;
 import Lexical.SymbolTable;
 
 import java.util.ArrayList;
 
 import utils.PathContainer;
 import Lexical.SymbolElement;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 
 //#line 32 "Parser.java"
 
 
 
 
 public class Parser
 {
 
 boolean yydebug;        //do I want debug output?
 int yynerrs;            //number of errors so far
 int yyerrflag;          //was there an error?
 int yychar;             //the current working character
 
 //########## MESSAGES ##########
 //###############################################################
 // method: debug
 //###############################################################
 void debug(String msg)
 {
   if (yydebug)
     System.out.println(msg);
 }
 
 //########## STATE STACK ##########
 final static int YYSTACKSIZE = 500;  //maximum stack size
 int statestk[] = new int[YYSTACKSIZE]; //state stack
 int stateptr;
 int stateptrmax;                     //highest index of stackptr
 int statemax;                        //state when highest index reached
 //###############################################################
 // methods: state stack push,pop,drop,peek
 //###############################################################
 final void state_push(int state)
 {
   try {
 		stateptr++;
 		statestk[stateptr]=state;
 	 }
 	 catch (ArrayIndexOutOfBoundsException e) {
      int oldsize = statestk.length;
      int newsize = oldsize * 2;
      int[] newstack = new int[newsize];
      System.arraycopy(statestk,0,newstack,0,oldsize);
      statestk = newstack;
      statestk[stateptr]=state;
   }
 }
 final int state_pop()
 {
   return statestk[stateptr--];
 }
 final void state_drop(int cnt)
 {
   stateptr -= cnt; 
 }
 final int state_peek(int relative)
 {
   return statestk[stateptr-relative];
 }
 //###############################################################
 // method: init_stacks : allocate and prepare stacks
 //###############################################################
 final boolean init_stacks()
 {
   stateptr = -1;
   val_init();
   return true;
 }
 //###############################################################
 // method: dump_stacks : show n levels of the stacks
 //###############################################################
 void dump_stacks(int count)
 {
 int i;
   System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
   for (i=0;i<count;i++)
     System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
   System.out.println("======================");
 }
 
 
 //########## SEMANTIC VALUES ##########
 //public class ParserVal is defined in ParserVal.java
 
 
 String   yytext;//user variable to return contextual strings
 ParserVal yyval; //used to return semantic vals from action routines
 ParserVal yylval;//the 'lval' (result) I got from yylex()
 ParserVal valstk[];
 int valptr;
 //###############################################################
 // methods: value stack push,pop,drop,peek.
 //###############################################################
 void val_init()
 {
   valstk=new ParserVal[YYSTACKSIZE];
   yyval=new ParserVal();
   yylval=new ParserVal();
   valptr=-1;
 }
 void val_push(ParserVal val)
 {
   if (valptr>=YYSTACKSIZE)
     return;
   valstk[++valptr]=val;
 }
 ParserVal val_pop()
 {
   if (valptr<0)
     return new ParserVal();
   return valstk[valptr--];
 }
 void val_drop(int cnt)
 {
 int ptr;
   ptr=valptr-cnt;
   if (ptr<0)
     return;
   valptr = ptr;
 }
 ParserVal val_peek(int relative)
 {
 int ptr;
   ptr=valptr-relative;
   if (ptr<0)
     return new ParserVal();
   return valstk[ptr];
 }
 final ParserVal dup_yyval(ParserVal val)
 {
   ParserVal dup = new ParserVal();
   dup.ival = val.ival;
   dup.dval = val.dval;
   dup.sval = val.sval;
   dup.obj = val.obj;
   return dup;
 }
 //#### end semantic value section ####
 public final static short VARTYPE=257;
 public final static short PRINT=258;
 public final static short ID=259;
 public final static short CTE=260;
 public final static short CHARCHAIN=261;
 public final static short FUNCTION=262;
 public final static short BEGIN=263;
 public final static short END=264;
 public final static short FOR=265;
 public final static short COMPARATOR=266;
 public final static short ERROR=267;
 public final static short IF=268;
 public final static short THEN=269;
 public final static short ELSE=270;
 public final static short ASSIGN=271;
 public final static short SEMICOLON=272;
 public final static short COMMA=273;
 public final static short OPENPAREN=274;
 public final static short CLOSEPAREN=275;
 public final static short PLUS=276;
 public final static short MINUS=277;
 public final static short PRODUCT=278;
 public final static short DIVISION=279;
 public final static short RETURN=280;
 public final static short IT=281;
 public final static short YYERRCODE=256;
 final static short yylhs[] = {                           -1,
     2,    0,    1,    1,    1,    3,    3,    5,    5,    8,
     8,    9,    6,    6,   10,   10,   12,   11,    7,    4,
     4,   13,   13,   15,   15,   14,   14,   14,   14,   14,
    16,   16,   16,   22,   24,   17,   17,   17,   18,   18,
    18,   26,   27,   19,   20,   20,   20,   20,   20,   20,
    20,   20,   28,   30,   31,   25,   29,   29,   29,   29,
    29,   29,   29,   29,   29,   29,   32,   21,   23,   23,
    23,   33,   33,   33,   34,   34,   34,
 };
 final static short yylen[] = {                            2,
     0,    2,    1,    2,    1,    2,    1,    1,    1,    2,
     1,    3,    3,    1,    3,    3,    0,    6,    6,    2,
     1,    2,    1,    1,    1,    2,    2,    2,    2,    2,
     7,    6,    6,    0,    0,    4,    3,    3,    4,    4,
     4,    0,    0,   10,   11,   10,   10,   10,    8,    8,
     8,    7,    0,    0,    0,    3,    4,    4,    3,    3,
     3,    2,    2,    2,    1,    1,    0,    4,    3,    3,
     1,    3,    3,    1,    1,    1,    1,
 };
 final static short yydefred[] = {                         1,
     0,    0,    0,    0,    0,    0,    0,    0,    2,    0,
     0,    7,    8,    9,   14,    0,   21,    0,    0,    0,
     0,    0,    0,    0,    0,    0,    0,   67,    0,    0,
     0,    0,    0,   75,    0,   77,    0,    0,    0,   74,
     0,    6,   20,    0,   26,   27,   28,   29,   30,   12,
     0,   13,    0,   37,    0,    0,    0,    0,   17,    0,
     0,    0,    0,    0,    0,    0,    0,    0,    0,   11,
     0,   16,   15,   36,   40,    0,   41,   39,    0,   42,
     0,    0,   53,    0,    0,    0,    0,   53,   72,   73,
     0,   10,    0,   24,   23,   25,    0,    0,    0,   53,
    53,    0,   53,    0,    0,   34,    0,    0,   22,    0,
    18,    0,    0,    0,    0,    0,    0,    0,    0,   54,
     0,    0,   54,    0,    0,   19,    0,   55,   55,   64,
     0,    0,    0,   62,    0,    0,    0,   52,    0,    0,
     0,    0,   50,   51,    0,    0,   61,   59,    0,    0,
    49,    0,    0,   35,    0,   35,    0,   57,   58,   55,
     0,   55,   55,   33,   35,   32,    0,   47,   55,   48,
    46,   31,   45,
 };
 final static short yydgoto[] = {                          1,
     9,    2,  116,  117,  118,   13,   14,   71,   15,   24,
    16,   79,   93,  119,   95,   96,   18,   19,   20,   21,
    22,  107,   37,  164,   38,   99,  157,  102,  120,  135,
   138,   57,   39,   40,
 };
 final static short yysindex[] = {                         0,
     0,  -14, -235, -239, -184, -214, -221, -164,    0,  -14,
     2,    0,    0,    0,    0, -227,    0, -211, -173, -170,
  -161, -148, -255, -136, -142, -119, -130,    0,  -93, -117,
   -88, -108, -246,    0, -108,    0, -168,  -89,  -84,    0,
     2,    0,    0,  -78,    0,    0,    0,    0,    0,    0,
   -71,    0,  -69,    0,  -72,  -67, -108, -218,    0,  -57,
   -49,  -53, -231, -108, -108, -108,  -44, -108, -108,    0,
  -238,    0,    0,    0,    0,  -80,    0,    0,  -17,    0,
   -20,  -19,    0,  -11,  -80,  -84,  -84,    0,    0,    0,
   -33,    0, -191,    0,    0,    0,   -4,  -16, -108,    0,
     0,  -50,    0,  -50,  -50,    0, -108,  -15,    0,  -10,
     0,   -9,  -50,  -50,  -38, -176,  -96,    0,    0,    0,
   -50,    0,    0, -108, -154,    0, -176,    0,    0,    0,
   -14,  -30,    5,    0,   -7,    0,    7,    0,   11,    3,
    10, -176,    0,    0,  -12,    8,    0,    0,  -50,   13,
     0,  -50,  -50,    0,   12,    0,    2,    0,    0,    0,
   -50,    0,    0,    0,    0,    0,    2,    0,    0,    0,
     0,    0,    0,
 };
 final static short yyrindex[] = {                         0,
     0,    0,    0,    0,    0,    0,    0,    0,    0,  268,
   273,    0,    0,    0,    0,    0,    0,    0,    0,    0,
     0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
     0,    0, -172,    0,    0,    0,    0,    0, -128,    0,
   276,    0,    0,    0,    0,    0,    0,    0,    0,    0,
     0,    0,    0,    0,   14,    0,    0,    0,    0,    0,
     0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
     0,    0,    0,    0,    0,   15,    0,    0,    0,    0,
     0,    0,    0,  -26, -223, -116, -102,    0,    0,    0,
   -59,    0,    0,    0,    0,    0,    0,    0,    0,    0,
     0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
     0,    0,    0,    0,    0,    0,    0, -145,  -66,    0,
     0,  -90,    0,    0,    0,    0,    0,    0,    0,    0,
     0,  -87, -123,    0,    0,  -90,    0,    0,    0,    0,
     0,    6,    0,    0,    0,  -81,    0,    0,    0,    0,
     0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
     0,    0,    0,    0,    0,    0,   16,    0,    0,    0,
     0,    0,    0,
 };
 final static short yygindex[] = {                         0,
     0,    0,   -1,    1,    4,  -34,    0,    0,  206,    0,
     0,    0,    0,   -2,  196,    0,    0,   -6,    0,    0,
   259,  185,  -52, -122,  -28,    0,    0,  -18,  -73,  -58,
  -113,    0,  145,  148,
 };
 final static int YYTABLESIZE=291;
 static short yytable[];
 static { yytable();}
 static void yytable(){
 yytable = new short[]{                         17,
    10,   36,   11,   62,   76,   12,   63,   17,   43,   70,
    41,   85,   27,   42,  143,  144,   50,   51,    3,    4,
     5,   25,  151,   23,   82,   36,    7,   29,   36,    8,
   122,  123,   56,  166,   26,   44,   92,   83,   43,  128,
   129,   91,  172,   84,   30,   56,  168,  136,  170,  171,
    36,   56,   31,   77,  125,  173,   78,   36,   36,   36,
    45,   36,   36,  137,  139,  104,    4,    5,   94,  105,
   112,  140,  108,    7,   27,  160,    8,  150,  162,  163,
     3,  113,  114,   76,  121,    6,   28,  169,   91,   29,
    94,   32,   36,   76,   33,   34,   76,   64,   46,   76,
    36,   47,   76,   76,   76,   76,   76,   65,   66,   35,
    48,    7,   17,  131,   43,  132,    7,   36,   12,  133,
   141,   65,   66,   49,   65,  142,   65,   71,   17,   43,
    12,  145,   54,    6,  146,   52,   53,   71,    6,   69,
    71,   55,   43,   71,   56,   42,   71,   71,   71,   69,
    33,   34,   69,   70,   17,   69,   59,  167,   69,   69,
    69,    4,    5,   70,   43,   58,   70,  134,    7,   70,
    60,    8,   70,   70,   70,    6,    6,    6,    3,   54,
     6,   55,   63,    6,   63,   67,    6,   72,   60,   73,
    60,   21,   21,   68,   69,   65,   66,   21,   21,   34,
    34,   21,   74,   66,   75,   66,    3,    4,    5,   86,
    87,    6,  115,   28,    7,   89,   90,    8,    3,    4,
     5,   81,   80,    6,   88,  130,    7,    4,    5,    8,
    53,   53,   53,  147,    7,   53,   53,    8,   53,   97,
   106,   53,    3,    4,    5,    4,    5,    6,  100,  101,
     7,  158,    7,    8,  110,    8,  126,  103,  111,    4,
     5,   50,  149,   43,   43,  127,    7,    3,  148,    8,
    43,  159,    5,   43,  154,    4,  152,  155,   65,   66,
   153,  156,  161,  165,   98,   38,   68,   44,  109,   61,
   124,
 };
 }
 static short yycheck[];
 static { yycheck(); }
 static void yycheck() {
 yycheck = new short[] {                          2,
     2,    8,    2,   32,   57,    2,   35,   10,   11,   44,
    10,   64,  259,   10,  128,  129,  272,  273,  257,  258,
   259,  261,  136,  259,  256,   32,  265,  274,   35,  268,
   104,  105,  256,  156,  274,  263,   71,  269,   41,  113,
   114,  280,  165,  275,  259,  269,  160,  121,  162,  163,
    57,  275,  274,  272,  107,  169,  275,   64,   65,   66,
   272,   68,   69,  122,  123,   84,  258,  259,   71,   88,
    99,  124,  264,  265,  259,  149,  268,  136,  152,  153,
   257,  100,  101,  256,  103,  262,  271,  161,  280,  274,
    93,  256,   99,  266,  259,  260,  269,  266,  272,  272,
   107,  272,  275,  276,  277,  278,  279,  276,  277,  274,
   272,  257,  115,  115,  117,  115,  262,  124,  115,  116,
   275,  276,  277,  272,  270,  127,  272,  256,  131,  132,
   127,  131,  275,  257,  131,  272,  273,  266,  262,  256,
   269,  261,  145,  272,  275,  142,  275,  276,  277,  266,
   259,  260,  269,  256,  157,  272,  274,  157,  275,  276,
   277,  258,  259,  266,  167,  259,  269,  264,  265,  272,
   259,  268,  275,  276,  277,  257,  258,  259,  257,  270,
   262,  272,  270,  265,  272,  275,  268,  259,  270,  259,
   272,  258,  259,  278,  279,  276,  277,  264,  265,  259,
   260,  268,  275,  270,  272,  272,  257,  258,  259,   65,
    66,  262,  263,  271,  265,   68,   69,  268,  257,  258,
   259,  275,  272,  262,  269,  264,  265,  258,  259,  268,
   257,  258,  259,  264,  265,  262,  263,  268,  265,  257,
   274,  268,  257,  258,  259,  258,  259,  262,  269,  269,
   265,  264,  265,  268,  259,  268,  272,  269,  275,  258,
   259,  272,  270,  258,  259,  275,  265,    0,  264,  268,
   265,  264,    0,  268,  272,    0,  270,  275,  276,  277,
   270,  272,  270,  272,   79,  272,  272,  272,   93,   31,
   106,
 };
 }
 final static short YYFINAL=1;
 final static short YYMAXTOKEN=281;
 final static String yyname[] = {
 "end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
 null,null,null,"VARTYPE","PRINT","ID","CTE","CHARCHAIN","FUNCTION","BEGIN",
 "END","FOR","COMPARATOR","ERROR","IF","THEN","ELSE","ASSIGN","SEMICOLON",
 "COMMA","OPENPAREN","CLOSEPAREN","PLUS","MINUS","PRODUCT","DIVISION","RETURN",
 "IT",
 };
 final static String yyrule[] = {
 "$accept : start",
 "$$1 :",
 "start : $$1 program",
 "program : declarationList",
 "program : declarationList executions",
 "program : executions",
 "declarationList : declarationList declaration",
 "declarationList : declaration",
 "declaration : varDeclaration",
 "declaration : functionDeclaration",
 "varDeclarationList : varDeclarationList varDeclaration",
 "varDeclarationList : varDeclaration",
 "singleVarDeclaration : VARTYPE ID SEMICOLON",
 "varDeclaration : VARTYPE varList SEMICOLON",
 "varDeclaration : singleVarDeclaration",
 "varList : varList COMMA ID",
 "varList : ID COMMA ID",
 "$$2 :",
 "functionHeader : FUNCTION ID OPENPAREN $$2 singleVarDeclaration CLOSEPAREN",
 "functionDeclaration : functionHeader BEGIN varDeclarationList executionWReturnList END SEMICOLON",
 "executions : executions execution",
 "executions : execution",
 "executionWReturnList : executionWReturnList executionWReturn",
 "executionWReturnList : executionWReturn",
 "executionWReturn : execution",
 "executionWReturn : ret",
 "execution : print SEMICOLON",
 "execution : functionExecution SEMICOLON",
 "execution : iteration SEMICOLON",
 "execution : selection SEMICOLON",
 "execution : assign SEMICOLON",
 "ret : RETURN OPENPAREN retRule1 expression CLOSEPAREN SEMICOLON retRule2",
 "ret : RETURN retRule1 expression CLOSEPAREN SEMICOLON retRule2",
 "ret : RETURN OPENPAREN retRule1 expression SEMICOLON retRule2",
 "retRule1 :",
 "retRule2 :",
 "print : PRINT OPENPAREN CHARCHAIN CLOSEPAREN",
 "print : PRINT CHARCHAIN CLOSEPAREN",
 "print : PRINT OPENPAREN CHARCHAIN",
 "functionExecution : ID OPENPAREN ID CLOSEPAREN",
 "functionExecution : ID ID CLOSEPAREN SEMICOLON",
 "functionExecution : ID OPENPAREN ID SEMICOLON",
 "$$3 :",
 "$$4 :",
 "iteration : FOR OPENPAREN assign SEMICOLON $$3 condition CLOSEPAREN declarationList $$4 executions",
 "selection : IF OPENPAREN condition CLOSEPAREN THEN ifrule1 block ifrule2 ELSE block ifrule3",
 "selection : IF condition CLOSEPAREN THEN ifrule1 block ifrule2 ELSE block ifrule3",
 "selection : IF OPENPAREN condition THEN ifrule1 block ifrule2 ELSE block ifrule3",
 "selection : IF OPENPAREN condition CLOSEPAREN ifrule1 block ifrule2 ELSE block ifrule3",
 "selection : IF OPENPAREN condition CLOSEPAREN THEN ifrule1 block ifrule3",
 "selection : IF error condition CLOSEPAREN THEN ifrule1 block ifrule3",
 "selection : IF OPENPAREN condition error THEN ifrule1 block ifrule3",
 "selection : IF OPENPAREN condition CLOSEPAREN ifrule1 block ifrule3",
 "ifrule1 :",
 "ifrule2 :",
 "ifrule3 :",
 "condition : expression COMPARATOR expression",
 "block : BEGIN declarationList executions END",
 "block : BEGIN declarationList declaration END",
 "block : declarationList declaration END",
 "block : BEGIN declarationList declaration",
 "block : BEGIN executions END",
 "block : executions END",
 "block : BEGIN executions",
 "block : BEGIN END",
 "block : declaration",
 "block : execution",
 "$$5 :",
 "assign : ID ASSIGN $$5 expression",
 "expression : expression PLUS term",
 "expression : expression MINUS term",
 "expression : term",
 "term : term PRODUCT factor",
 "term : term DIVISION factor",
 "term : factor",
 "factor : CTE",
 "factor : ID",
 "factor : functionExecution",
 };
 
 //#line 517 "grammar.txt"
 
 String ins;
 StringTokenizer st;
 
 void yyerror(String s) {
  System.out.println("par:"+s);
 }
 
 boolean newline;
 
 public class ParserUtils {
 	ArrayList<String> variableList = new ArrayList<String>();
 	//there will be one reverse polish vector for each function, and one for the main. Each vector
 	//will be able to be accessed by the context name
 	public HashMap<String,ArrayList<String>> intermediateCode = new HashMap<String,ArrayList<String>>();
 	ErrorHandler errorHandler = ErrorHandler.getInstance();
 	LexicalAnalizer lexical = new LexicalAnalizer(PathContainer.getPath());
 	String context = "main";
 	//variable to handle the intermediate code generation of the FOR loop
 	String forConditionVar;
 	//a stack to handle index handling in IF and FOR statements
 	ArrayList<Integer> indexStack = new ArrayList<Integer>();
 	
 	public String toString(){
 		String stringvar = new String();
 		
 		String newline = System.getProperty("line.separator");
 		
 		Set<String> keys = this.intermediateCode.keySet();
 		for (String key : keys){
 			stringvar += key.toUpperCase() + " -> ";
 			stringvar += this.intermediateCode.get(key).toString();
 			stringvar += newline;
			stringvar += newline;
 		}
 		
 		return stringvar;
 	}
 	
 	public void godSaveThePolish(){
 		PrintWriter out = null;
 		try {
 			out = new PrintWriter("polish.txt");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		out.println(this.toString());
		out.flush();
		
 	}
 }
 
 public ParserUtils parserUtils = new ParserUtils();
 
 int yylex() {
 	Token token = parserUtils.lexical.getToken();
 	yylval = new ParserVal(token);
 
 	switch(token.getTokenValue()) {
 		case "ID":
 			return Parser.ID;
 		case "CTE" :
 			return Parser.CTE;
 		case "CHARCHAIN" :
 			return Parser.CHARCHAIN;
 		case "ULONG" :
 			return Parser.VARTYPE;
 		case "RETURN" :
 			return Parser.RETURN;
 		case ">" :
 			return Parser.COMPARATOR;
 		case ">=" :
 			return Parser.COMPARATOR;
 		case "<" :
 			return Parser.COMPARATOR;
 		case "<=" :
 			return Parser.COMPARATOR;
 		case "==" :
 			return Parser.COMPARATOR;
 		case "!=" :
 			return Parser.COMPARATOR;
 		case "=" :
 			return Parser.ASSIGN;
 		case ";" :
 			return Parser.SEMICOLON;
 		case "," :
 			return Parser.COMMA;
 		case "(" :
 			return Parser.OPENPAREN;
 		case ")" :
 			return Parser.CLOSEPAREN;
 		case "+" :
 			return Parser.PLUS;
 		case "-" :
 			return Parser.MINUS;
 		case "*" :
 			return Parser.PRODUCT;
 		case "/" :
 			return Parser.DIVISION;
 		case  "PRINT":
 			return Parser.PRINT;
 		case  "FUNCTION":
 			return Parser.FUNCTION;
 		case  "BEGIN" :
 			return Parser.BEGIN;
 		case  "END":
 			return Parser.END;
 		case  "FOR":
 			return Parser.FOR;
 		case  "IF":
 			return Parser.IF;
 		case  "THEN":
 			return Parser.THEN;
 		case  "ELSE":
 			return Parser.ELSE;
 		case  "#":
 			return -1;
 	}
 	return -1;
 }
 //#line 490 "Parser.java"
 //###############################################################
 // method: yylexdebug : check lexer state
 //###############################################################
 void yylexdebug(int state,int ch)
 {
 String s=null;
   if (ch < 0) ch=0;
   if (ch <= YYMAXTOKEN) //check index bounds
      s = yyname[ch];    //now get it
   if (s==null)
     s = "illegal-symbol";
   debug("state "+state+", reading "+ch+" ("+s+")");
 }
 
 
 
 
 
 //The following are now global, to aid in error reporting
 int yyn;       //next next thing to do
 int yym;       //
 int yystate;   //current parsing state from state table
 String yys;    //current token string
 
 
 //###############################################################
 // method: yyparse : parse input and execute indicated items
 //###############################################################
 int yyparse()
 {
 boolean doaction;
   init_stacks();
   yynerrs = 0;
   yyerrflag = 0;
   yychar = -1;          //impossible char forces a read
   yystate=0;            //initial state
   state_push(yystate);  //save it
   val_push(yylval);     //save empty value
   while (true) //until parsing is done, either correctly, or w/error
     {
     doaction=true;
     if (yydebug) debug("loop"); 
     //#### NEXT ACTION (from reduction table)
     for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
       {
       if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
       if (yychar < 0)      //we want a char?
         {
         yychar = yylex();  //get next token
         if (yydebug) debug(" next yychar:"+yychar);
         //#### ERROR CHECK ####
         if (yychar < 0)    //it it didn't work/error
           {
           yychar = 0;      //change it to default string (no -1!)
           if (yydebug)
             yylexdebug(yystate,yychar);
           }
         }//yychar<0
       yyn = yysindex[yystate];  //get amount to shift by (shift index)
       if ((yyn != 0) && (yyn += yychar) >= 0 &&
           yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
         {
         if (yydebug)
           debug("state "+yystate+", shifting to state "+yytable[yyn]);
         //#### NEXT STATE ####
         yystate = yytable[yyn];//we are in a new state
         state_push(yystate);   //save it
         val_push(yylval);      //push our lval as the input for next rule
         yychar = -1;           //since we have 'eaten' a token, say we need another
         if (yyerrflag > 0)     //have we recovered an error?
            --yyerrflag;        //give ourselves credit
         doaction=false;        //but don't process yet
         break;   //quit the yyn=0 loop
         }
 
     yyn = yyrindex[yystate];  //reduce
     if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
             yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
       {   //we reduced!
       if (yydebug) debug("reduce");
       yyn = yytable[yyn];
       doaction=true; //get ready to execute
       break;         //drop down to actions
       }
     else //ERROR RECOVERY
       {
       if (yyerrflag==0)
         {
         yyerror("syntax error");
         yynerrs++;
         }
       if (yyerrflag < 3) //low error count?
         {
         yyerrflag = 3;
         while (true)   //do until break
           {
           if (stateptr<0)   //check for under & overflow here
             {
             yyerror("stack underflow. aborting...");  //note lower case 's'
             return 1;
             }
           yyn = yysindex[state_peek(0)];
           if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                     yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
             {
             if (yydebug)
               debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
             yystate = yytable[yyn];
             state_push(yystate);
             val_push(yylval);
             doaction=false;
             break;
             }
           else
             {
             if (yydebug)
               debug("error recovery discarding state "+state_peek(0)+" ");
             if (stateptr<0)   //check for under & overflow here
               {
               yyerror("Stack underflow. aborting...");  //capital 'S'
               return 1;
               }
             state_pop();
             val_pop();
             }
           }
         }
       else            //discard this token
         {
         if (yychar == 0)
           return 1; //yyabort
         if (yydebug)
           {
           yys = null;
           if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
           if (yys == null) yys = "illegal-symbol";
           debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
           }
         yychar = -1;  //read another
         }
       }//end error recovery
     }//yyn=0 loop
     if (!doaction)   //any reason not to proceed?
       continue;      //skip action
     yym = yylen[yyn];          //get count of terminals on rhs
     if (yydebug)
       debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
     if (yym>0)                 //if count of rhs not 'nil'
       yyval = val_peek(yym-1); //get current semantic value
     yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
     switch(yyn)
       {
 //########## USER-SUPPLIED ACTIONS ##########
 case 1:
 //#line 51 "grammar.txt"
 {
 	/*initialize the Main intermediate code vector*/
 	parserUtils.intermediateCode.put("MAIN", new ArrayList<String>());
 }
 break;
 case 12:
 //#line 73 "grammar.txt"
 {
 		Error error;
 		String varName = ((Token)val_peek(1).obj).getLiteralValue();
 		String varNameWithContext = "";
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		SymbolElement element = symbolTable.identify(varName);
 		String literalTypeValue = ((Token)val_peek(2).obj).getLiteralValue();
 
 		/*clear prior defined variables*/
 		parserUtils.variableList.clear();
 		/*update symbole table by applying name mangling*/
 		varNameWithContext = parserUtils.context+"_"+varName;
 		/*check if variable was already declared*/
 		if(symbolTable.contains(varNameWithContext)) {
 			error = new Error(Error.TYPE_WARNING,"Variable redeclartion", parserUtils.lexical.getLine());
 			parserUtils.errorHandler.addError(error);
 		}
 		symbolTable.remove(varName);
 		symbolTable.addSymbol(varNameWithContext,element);
 
 		element.setVarType(literalTypeValue);
 		element.setUse("VAR");
 		/*return variable name*/
 		yyval=val_peek(1);
 	}
 break;
 case 13:
 //#line 99 "grammar.txt"
 {
 		Error error;
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		SymbolElement element = new SymbolElement();
 		String varNameWithContext = "";
 		String literalTypeValue = ((Token)val_peek(2).obj).getLiteralValue();
 
 		/*add type to each symbol table entryof the varList*/
 		for(String varName : parserUtils.variableList) {
 			element = symbolTable.identify(varName);
 			/*update symbole table by applying name mangling*/
 			varNameWithContext = parserUtils.context+"_"+varName;
 			/*check if variable was already declared*/
 			if(symbolTable.contains(varNameWithContext)) {
 				error = new Error(Error.TYPE_WARNING,"Variable redeclartion", parserUtils.lexical.getLine());
 				parserUtils.errorHandler.addError(error);
 			}
 			symbolTable.remove(varName);
 			symbolTable.addSymbol(varNameWithContext,element);
 			element.setVarType(literalTypeValue);
 			element.setUse("VAR");
 		}
 		/*clear prior defined variables*/
 		parserUtils.variableList.clear();
 	}
 break;
 case 15:
 //#line 126 "grammar.txt"
 {
 		/*add variable name to a list that will be consulted later*/
 		/*to add type to each variable in the symbol table*/
 		String varName = ((Token)val_peek(0).obj).getLiteralValue();
 		parserUtils.variableList.add(varName);
 	}
 break;
 case 16:
 //#line 132 "grammar.txt"
 {
 		String varName = ((Token)val_peek(2).obj).getLiteralValue();
 		parserUtils.variableList.add(varName);
 		varName = ((Token)val_peek(0).obj).getLiteralValue();
 		parserUtils.variableList.add(varName);
 	}
 break;
 case 17:
 //#line 139 "grammar.txt"
 {
 		/*Set function name as the current context*/
 		String functionName = ((Token)val_peek(1).obj).getLiteralValue();
 		parserUtils.context = functionName;
 	}
 break;
 case 18:
 //#line 143 "grammar.txt"
 {
 		Error error;
 		SymbolElement element = new SymbolElement();
 		/*newElement will be the auxiliar variable to store de parameter passed to the current function*/
 		SymbolElement newElement = new SymbolElement("ID",null);
 		String varNameWithContext = "";
 		String varName = ((Token)val_peek(1).obj).getLiteralValue();
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		String functionName = ((Token)val_peek(4).obj).getLiteralValue();
 		ArrayList<String> currentIntCodeVector;
 
 		/*add auxiliar parameter variable*/
 		symbolTable.addSymbol(parserUtils.context+"_parameter",newElement);
 		/*add FUNCTION use to the function identifier*/
 		element = symbolTable.identify(functionName);
 		element.setUse("FUNC");
 		/*check for function redeclaration*/
 		if(symbolTable.contains(functionName)) {
 			error = new Error(Error.TYPE_WARNING,"Function redeclartion", parserUtils.lexical.getLine());
 			parserUtils.errorHandler.addError(error);
 		}
 		/*initialize intermediate code vector*/
 		parserUtils.intermediateCode.put(parserUtils.context.toUpperCase(), new ArrayList<String>());
 		varNameWithContext = parserUtils.context+"_"+varName;
 		element = symbolTable.identify(varNameWithContext);
 
 		/*set PARAMETER use to each identifier in the parameters declaration*/
 		element.setUse("PARAM");
 		parserUtils.variableList.clear();
 		/*default to 0 the return value*/
 		currentIntCodeVector = parserUtils.intermediateCode.get(parserUtils.context.toUpperCase());
 		
 		//add direction to function parameter
 		currentIntCodeVector.add(((Token)val_peek(4).obj).getLiteralValue()+"_"+((Token)val_peek(1).obj).getLiteralValue()); //function variable
 		currentIntCodeVector.add(((Token)val_peek(4).obj).getLiteralValue()+"_parameter"); //auxiliar
 		currentIntCodeVector.add("=");
 		
 		currentIntCodeVector.add("rtn");
 		currentIntCodeVector.add("0");
 		currentIntCodeVector.add("=");
 
 	}
 break;
 case 19:
 //#line 179 "grammar.txt"
 {
 		/*once the function declaration is over, set the current context as main*/
 		parserUtils.context = "main";
 	}
 break;
 case 32:
 //#line 205 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing left paretheses in return execution ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 33:
 //#line 209 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing right paretheses in return execution ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 34:
 //#line 214 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		currentIntCodeVector.add("rtn");
 	}
 break;
 case 35:
 //#line 220 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		currentIntCodeVector.add("=");
 		currentIntCodeVector.add("[RET]");
 	}
 break;
 case 36:
 //#line 228 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		String message = ((Token)val_peek(1).obj).getLiteralValue();
 		currentIntCodeVector.add(message);
 		currentIntCodeVector.add("[PRINT]");
 	}
 break;
 case 37:
 //#line 235 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing left paretheses in print execution ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 38:
 //#line 239 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing right paretheses in print execution ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 39:
 //#line 245 "grammar.txt"
 {
 		Error error;
 		String parameterName = ((Token)val_peek(1).obj).getLiteralValue();
 		String functionName = ((Token)val_peek(3).obj).getLiteralValue();
 		String parameterNameWContext = parserUtils.context+"_"+parameterName;
 		SymbolElement element = new SymbolElement();
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		/*check if parameter is declared*/
 		element = symbolTable.identify(parameterNameWContext);
 		if(!(element != null && element.getUse() != "FUNC")) {
 			error = new Error(Error.TYPE_FATAL,"Identifier "+parameterName+" not found.", parserUtils.lexical.getLine());
 			parserUtils.errorHandler.addError(error);
 		}
 		/*assign the parameter value to the auxiliar parameter previously reserved*/
 		currentIntCodeVector.add(functionName+"_parameter");
 		
 			SymbolElement SE = new SymbolElement();
 			SE.setUse("VAR");
 			SE.setType("PARAM");
 			symbolTable.addSymbol(functionName+"_parameter", SE);
 		currentIntCodeVector.add(context+"_"+parameterName);
 		currentIntCodeVector.add("[LEA]");
 		/*add function call to the intermediate code vector*/
 		currentIntCodeVector.add(functionName);
 		currentIntCodeVector.add("[CALL]");
 		currentIntCodeVector.add("rtn");
 	}
 break;
 case 40:
 //#line 269 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing left paretheses in function execution ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 41:
 //#line 273 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing right paretheses in function execution ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 42:
 //#line 278 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		String forConditionVar = ((Token)val_peek(1).obj).getLiteralValue();
 		parserUtils.forConditionVar = forConditionVar;
 		parserUtils.indexStack.add(currentIntCodeVector.size());
 	}
 break;
 case 43:
 //#line 284 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 
 		/*save the index where the false bifurcation will be stored*/
 		currentIntCodeVector.add("PLACEHOLDER");
 		parserUtils.indexStack.add(currentIntCodeVector.size()-1);
 		currentIntCodeVector.add("[BF]");
 	}
 break;
 case 44:
 //#line 292 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		Integer falseBifurcationIndex = parserUtils.indexStack.remove(parserUtils.indexStack.size()-1);
 		String forReevaluationIndex = parserUtils.indexStack.remove(parserUtils.indexStack.size()-1).toString();
 		String falseBifurcationDirection;
 		/*increment the condition variable*/
 		currentIntCodeVector.add(context+"_"+parserUtils.forConditionVar);
 		currentIntCodeVector.add(context+"_"+parserUtils.forConditionVar);
 		currentIntCodeVector.add("1");
 		currentIntCodeVector.add("+");
 		currentIntCodeVector.add("=");
 		/*Add inconditional jump to the FOR reevaluation index*/
 		currentIntCodeVector.add(forReevaluationIndex);
 		currentIntCodeVector.add("[JMP]");
 		/*Set the end of the FOR statement as the false bifurcation direction*/
 		falseBifurcationDirection = Integer.toString(currentIntCodeVector.size());
 		currentIntCodeVector.set(falseBifurcationIndex,falseBifurcationDirection);
 	}
 break;
 case 46:
 //#line 324 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing left paretheses in IF statement  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 47:
 //#line 328 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing right paretheses in IF statement  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 48:
 //#line 332 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing THEN in IF statement  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 50:
 //#line 337 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing left paretheses in IF statement  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 51:
 //#line 341 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing right paretheses in IF statement  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 52:
 //#line 345 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing THEN in IF statement  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 53:
 //#line 350 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		/*save the index where the false bifurcation will be stored*/
 		currentIntCodeVector.add("PLACEHOLDER");
 		parserUtils.indexStack.add(currentIntCodeVector.size()-1);
 		currentIntCodeVector.add("[BF]");
 	}
 break;
 case 54:
 //#line 359 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		String elseBlockDirection = Integer.toString(currentIntCodeVector.size()+2);
 		Integer elseBlockIndex = parserUtils.indexStack.remove(parserUtils.indexStack.size()-1);
 		currentIntCodeVector.set(elseBlockIndex,elseBlockDirection);
 		/*save the index where the end of else block will be stored*/
 		currentIntCodeVector.add("PLACEHOLDER");
 		parserUtils.indexStack.add(currentIntCodeVector.size()-1);
 		currentIntCodeVector.add("[JMP]");
 	}
 break;
 case 55:
 //#line 371 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		String endBlockDirection = Integer.toString(currentIntCodeVector.size());
 		Integer endBlockIndex = parserUtils.indexStack.remove(parserUtils.indexStack.size()-1);
 		currentIntCodeVector.set(endBlockIndex,endBlockDirection);
 	}
 break;
 case 56:
 //#line 379 "grammar.txt"
 {
 		String comparatorSymbol = ((Token)val_peek(1).obj).getLiteralValue();
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		currentIntCodeVector.add(comparatorSymbol);
 	}
 break;
 case 59:
 //#line 388 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing BEGIN in block  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 60:
 //#line 392 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing END in block  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 62:
 //#line 397 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing BEGIN in block  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 63:
 //#line 401 "grammar.txt"
 {
 		Error error = new Error(Error.TYPE_FATAL,"Missing END in block  ", parserUtils.lexical.getLine());
 		parserUtils.errorHandler.addError(error);
 	}
 break;
 case 67:
 //#line 409 "grammar.txt"
 {
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		String varName = ((Token)val_peek(1).obj).getLiteralValue();
 		currentIntCodeVector.add(context+"_"+varName);
 	}
 break;
 case 68:
 //#line 414 "grammar.txt"
 {
 		Error error;
 		String varName = ((Token)val_peek(3).obj).getLiteralValue();
 		String assignSymbol = ((Token)val_peek(2).obj).getLiteralValue();
 		String context = parserUtils.context;
 		String parameterNameWContext;
 		SymbolElement element = new SymbolElement();
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		HashSet<String> contextList = new HashSet<String>();
 		boolean found = false;
 		contextList.add(context);
 		contextList.add("MAIN");
 		/*check if identifier is declared*/
 		for(String cont: contextList) {
 			parameterNameWContext= cont+"_"+varName;
 			element = symbolTable.identify(parameterNameWContext);
 			if(element != null && element.getUse() != "FUNC") {
 				found = true;
 				break;
 			}
 		}
 		if(!found) {
 			error = new Error(Error.TYPE_FATAL,"Identifier "+varName+" not found.", parserUtils.lexical.getLine());
 			parserUtils.errorHandler.addError(error);
 		}
 		currentIntCodeVector.add(assignSymbol);
 
 	}
 break;
 case 69:
 //#line 443 "grammar.txt"
 {
 		String symbol = ((Token)val_peek(1).obj).getLiteralValue();
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		currentIntCodeVector.add(symbol);
 	}
 break;
 case 70:
 //#line 449 "grammar.txt"
 {
 		String symbol = ((Token)val_peek(1).obj).getLiteralValue();
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		currentIntCodeVector.add(symbol);
 	}
 break;
 case 72:
 //#line 458 "grammar.txt"
 {
 		String symbol = ((Token)val_peek(1).obj).getLiteralValue();
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		currentIntCodeVector.add(symbol);
 	}
 break;
 case 73:
 //#line 464 "grammar.txt"
 {
 		String symbol = ((Token)val_peek(1).obj).getLiteralValue();
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		currentIntCodeVector.add(symbol);
 	}
 break;
 case 75:
 //#line 473 "grammar.txt"
 {
 		String varName = ((Token)val_peek(0).obj).getLiteralValue();
 		String context = parserUtils.context;
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		SymbolElement element = new SymbolElement();
 
 		element = symbolTable.identify(varName);
 		/*The only allowed type for constants is ULONG*/
 		element.setVarType("ULONG");
 		currentIntCodeVector.add(varName);
 	}
 break;
 case 76:
 //#line 485 "grammar.txt"
 {
 		Error error;
 		String varName = ((Token)val_peek(0).obj).getLiteralValue();
 		String context = parserUtils.context;
 		HashSet<String> contextList = new HashSet<String>();
 		SymbolElement element = new SymbolElement();
 		SymbolTable symbolTable = SymbolTable.getInstance();
 		ArrayList<String> currentIntCodeVector = parserUtils.intermediateCode.get(context.toUpperCase());
 		String parameterNameWContext;
 		boolean found = false;
 		contextList.add(context);
 		contextList.add("MAIN");
 		for(String cont: contextList) {
 			parameterNameWContext= cont+"_"+varName;
 			element = symbolTable.identify(parameterNameWContext);
 			if(element != null && element.getUse() != "FUNC") {
 				found = true;
 				break;
 			}
 		}
 		/*check if parameter is declared*/
 		if(!found) {
 			error = new Error(Error.TYPE_FATAL,"Identifier "+varName+" not found.", parserUtils.lexical.getLine());
 			parserUtils.errorHandler.addError(error);
 		}
 		currentIntCodeVector.add(parserUtils.context+"_"+varName);
 
 	}
 break;
 //#line 1151 "Parser.java"
 //########## END OF USER-SUPPLIED ACTIONS ##########
     }//switch
     //#### Now let's reduce... ####
     if (yydebug) debug("reduce");
     state_drop(yym);             //we just reduced yylen states
     yystate = state_peek(0);     //get new state
     val_drop(yym);               //corresponding value drop
     yym = yylhs[yyn];            //select next TERMINAL(on lhs)
     if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
       {
       if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
       yystate = YYFINAL;         //explicitly say we're done
       state_push(YYFINAL);       //and save it
       val_push(yyval);           //also save the semantic value of parsing
       if (yychar < 0)            //we want another character?
         {
         yychar = yylex();        //get next character
         if (yychar<0) yychar=0;  //clean, if necessary
         if (yydebug)
           yylexdebug(yystate,yychar);
         }
       if (yychar == 0)          //Good exit (if lex returns 0 ;-)
          break;                 //quit the loop--all DONE
       }//if yystate
     else                        //else not done yet
       {                         //get next state and push, for next yydefred[]
       yyn = yygindex[yym];      //find out where to go
       if ((yyn != 0) && (yyn += yystate) >= 0 &&
             yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
         yystate = yytable[yyn]; //get new state
       else
         yystate = yydgoto[yym]; //else go to new defred
       if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
       state_push(yystate);     //going again, so push state & val...
       val_push(yyval);         //for next action
       }
     }//main loop
   return 0;//yyaccept!!
 }
 //## end of method parse() ######################################
 
 
 
 //## run() --- for Thread #######################################
 /**
  * A default run method, used for operating this parser
  * object in the background.  It is intended for extending Thread
  * or implementing Runnable.  Turn off with -Jnorun .
  */
 public void run()
 {
   yyparse();
 }
 //## end of method run() ########################################
 
 
 
 //## Constructors ###############################################
 /**
  * Default constructor.  Turn off with -Jnoconstruct .
 
  */
 public Parser()
 {
   //nothing to do
 }
 
 
 /**
  * Create a parser, setting the debug to true or false.
  * @param debugMe true for debugging, false for no debug.
  */
 public Parser(boolean debugMe)
 {
   yydebug=debugMe;
 }
 //###############################################################
 
 
 
 }
 //################### END OF CLASS ##############################
