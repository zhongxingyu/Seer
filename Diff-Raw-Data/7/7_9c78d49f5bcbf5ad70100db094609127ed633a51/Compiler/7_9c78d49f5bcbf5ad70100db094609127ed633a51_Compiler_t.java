 
 import java.util.ArrayList;
 
 /**
  *
  * @author Steven Neisius
  */
 public class Compiler {
 
     private static Scanner scn;
     private static ArrayList<Integer> vars;
 //    private static Queue<Integer> inputs;
     private static int pc;
     private static ArrayList<Integer> buf;
     private static boolean[] R = new boolean[32];
     private static final int FP = 28;
     private static final int SP = 29;
     private static final int GV = 30;
     private static final int RA = 31;
     // Mnemonic-to-Opcode mapping
     static final String mnemo[] = {
         "ADD", "SUB", "MUL", "DIV", "MOD", "CMP", "ERR", "ERR", "OR", "AND", "BIC", "XOR", "LSH", "ASH", "CHK", "ERR",
         "ADDI", "SUBI", "MULI", "DIVI", "MODI", "CMPI", "ERRI", "ERRI", "ORI", "ANDI", "BICI", "XORI", "LSHI", "ASHI", "CHKI", "ERR",
         "LDW", "LDX", "POP", "ERR", "STW", "STX", "PSH", "ERR", "BEQ", "BNE", "BLT", "BGE", "BLE", "BGT", "BSR", "ERR",
         "JSR", "RET", "RDI", "WRD", "WRH", "WRL", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR",
         "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR", "ERR"};
     static final int ADD = 0;
     static final int SUB = 1;
     static final int MUL = 2;
     static final int DIV = 3;
     static final int MOD = 4;
     static final int CMP = 5;
     static final int OR = 8;
     static final int AND = 9;
     static final int BIC = 10;
     static final int XOR = 11;
     static final int LSH = 12;
     static final int ASH = 13;
     static final int CHK = 14;
     static final int ADDI = 16;
     static final int SUBI = 17;
     static final int MULI = 18;
     static final int DIVI = 19;
     static final int MODI = 20;
     static final int CMPI = 21;
     static final int ORI = 24;
     static final int ANDI = 25;
     static final int BICI = 26;
     static final int XORI = 27;
     static final int LSHI = 28;
     static final int ASHI = 29;
     static final int CHKI = 30;
     static final int LDW = 32;
     static final int LDX = 33;
     static final int POP = 34;
     static final int STW = 36;
     static final int STX = 37;
     static final int PSH = 38;
     static final int BEQ = 40;
     static final int BNE = 41;
     static final int BLT = 42;
     static final int BGE = 43;
     static final int BLE = 44;
     static final int BGT = 45;
     static final int BSR = 46;
     static final int JSR = 48;
     static final int RET = 49;
     static final int RDI = 50;
     static final int WRD = 51;
     static final int WRH = 52;
     static final int WRL = 53;
     static final int ERR = 63; // error opcode which is insertered by loader
 
     public Compiler(String filename) {
         if (filename != null && filename.isEmpty()) {
             Error("Usage: java Compiler <file>");
             return;
         }
 
         scn = new Scanner(filename);
         vars = new ArrayList<Integer>();
 
         pc = 0;
         buf = new ArrayList<Integer>();//FIXME size of memory buffer
 
         computation();//compile program
     }
 
     public static void Error(String errorMsg) {
         System.err.println("Compiler error: " + errorMsg);
     }
 
     private static boolean computation() {
         //computation = “main” [ varDecl ] “{” statSequence “}” “.” .
         boolean rtn = true;
 
         for (int i = 0; i < R.length; i++) {
             R[i] = true;
         }
         R[0] = false;//block special registers
         R[FP] = false;
         R[SP] = false;
         R[GV] = false;
         R[RA] = false;
 
         CheckFor(Scanner.mainToken);// "main"
 
         scn.Next();
 
         if (scn.sym == Scanner.varToken) {// "var" [VarDecl]
             rtn = rtn & varDecl();
         }
 
         CheckFor(Scanner.beginToken);// "{"
 
         scn.Next();
         rtn = rtn & statSequence();
 
         CheckFor(Scanner.endToken);// "}"
 
         scn.Next();
         CheckFor(Scanner.periodToken);// "."
 
         PutF2(RET, 0, 0, 0);//END PROGRAM!
 
         if (!rtn) {
             Error("computation");
         }
         return rtn;
     }
 
     private static boolean varDecl() {
         //varDecl = “var” ident { “,” ident } “;” .
         boolean rtn = true;
 
         CheckFor(Scanner.varToken); // var
 
         scn.Next();
         CheckFor(Scanner.identToken); // ident
         GiveAddress(scn.id);
 
         scn.Next();
         while (scn.sym == Scanner.commaToken) {// ","
             scn.Next();
             CheckFor(Scanner.identToken); // ident
             GiveAddress(scn.id);
 
             scn.Next();
         }
 
         CheckFor(Scanner.semiToken);// ";"
 
         scn.Next();
 
         if (!rtn) {
             Error("varDecl");
         }
         return rtn;
     }
 
     private static boolean statSequence() {
         //statSequence = statement { “;” statement }.
         boolean rtn = true;
 
         rtn = rtn & statement();
 
         while (scn.sym == Scanner.semiToken) {// ";"
             scn.Next();
             rtn = rtn & statement();
         }
 
         if (!rtn) {
             Error("statSequence");
         }
         return rtn;
     }
 
     private static boolean statement() {
         //statement = assignment | funcCall | ifStatement | whileStatement .
         boolean rtn = true;
 
         Result x;
 
         if (scn.sym == Scanner.ifToken) {// if
             scn.Next();
             Result follow = new Result();
             follow.fixuplocation = 0;
 
             x = relation();
             CondNegBraFwd(x);
             Deallocate(x);
             CheckFor(Scanner.thenToken);
             scn.Next();
 
             statSequence();
 
 //            while (scn.sym == Scanner.elseToken) {//elsif??
 //                scn.Next();
 //                UnCondBraFwd(follow);
 //                Fixup(x.fixuplocation);
 //                x = relation();
 //                CondNegBraFwd(x);
 //                CheckFor(Scanner.thenToken);
 //                statSequence();
 //            }
 
             if (scn.sym == Scanner.elseToken) {
                 scn.Next();
 
                 UnCondBraFwd(follow);
                 Fixup(x.fixuplocation);
                 statSequence();
             } else {
                 Fixup(x.fixuplocation);
             }
 
 
             CheckFor(Scanner.fiToken);
             scn.Next();
 
             FixAll(follow.fixuplocation);//FIXME Works?
 
         } else if (scn.sym == Scanner.whileToken) {
             scn.Next();
             int looplocation = pc;
 
             x = relation();
 
             CondNegBraFwd(x);
             CheckFor(Scanner.doToken);
             scn.Next();
 
             statSequence();
 
             PutF1(BEQ, 0, 0, looplocation - pc);// Why minus PC? isnt this the default loop // -pc
 
             Fixup(x.fixuplocation);
 
             Deallocate(x);
 
             CheckFor(Scanner.odToken);
             scn.Next();
 
         } else if (scn.sym == Scanner.callToken) { // call
             x = funcCall();
         } else if (scn.sym == Scanner.identToken) { // ident
             x = assignment();
         } else {// empty statement invalid
             rtn = false;
         }
 
         if (!rtn) {
             Error("statement");
         }
         return rtn;
     }
 
     private static Result funcCall() {
         //funcCall = “call” ident [ “(“ [expression { “,” expression } ] “)” ].
         boolean rtn = true;
 
         Result x;
 
         ArrayList<Result> funcArgs = new ArrayList<Result>();
 
         CheckFor(Scanner.callToken); // call
 
         scn.Next();
         CheckFor(Scanner.identToken); // ident
         int func = scn.id;
 
         scn.Next();
         if (scn.sym == Scanner.openparenToken) { // "("
             scn.Next();
 
             if (!(scn.sym == Scanner.closeparenToken)) { // ")"
 
                 funcArgs.add(expression());
 
                 while (scn.sym == Scanner.commaToken) {// ","
                     scn.Next();
 
                     funcArgs.add(expression());//TODO load variables to memory
                 }
             }
 
             CheckFor(Scanner.closeparenToken); // ")"
             scn.Next();
         }
 
         //TODO Jump to subroutine
         //TODO return path
 
         if (!rtn) {
             Error("funcCall");
         }
 
         x = execFunc(func, funcArgs);
 
         return x;
     }
 
     private static Result assignment() {
         //assignment = ident “<-” expression.
         Result x = new Result();
 
         CheckFor(Scanner.identToken); // ident
         x.address = LookupAddress(scn.id);
         x.setVar();
 
         if (x.address == -1) {
             Error("unknown identifier: " + scn.Id2String(scn.id));
         }
 
         scn.Next();
         CheckFor(Scanner.becomesToken); // "<-"
 
         scn.Next();
         Result y = expression();
 
         if (!y.isReg()) {
             load(y);
         }
         PutF1(STW, y.regno, GV, -x.address);
         Deallocate(y);
 
         return x;
     }
 
     private static Result expression() {
         //expression = term {(“+” | “-”) term}.
         Result x, y;
         int op;
 
         x = term();
 
         while (scn.sym == Scanner.plusToken || scn.sym == Scanner.minusToken) { // "+" or "-"
             op = scn.sym;
             scn.Next();
 
             y = term();
             Compute(op, x, y);
         }
         return x;
     }
 
     private static Result relation() {
         //relation = expression relOp expression .
         Result x, y;
         int op;
 
         x = expression();
 
         switch (scn.sym) {
             case Scanner.eqlToken://==
             case Scanner.neqToken://!=
             case Scanner.lssToken://<
             case Scanner.geqToken://>=
             case Scanner.leqToken://<=
             case Scanner.gtrToken://>
                 op = scn.sym;
                 scn.Next();
 
                 y = expression();
                 Compute(CMP, x, y);
 
                 x.setCond();
                 x.cond = op;
                 x.fixuplocation = 0;
                 break;
             default:
                 Error("Relation: Syntax Error");
         }
         return x;
     }
 
     private static Result term() {
         //term = factor { (“*” | “/”) factor}.
         Result x, y;
         int op;
 
         x = factor();
 
         while (scn.sym == Scanner.timesToken || scn.sym == Scanner.divToken) { // "*" or "/"
             op = scn.sym;
             scn.Next();
 
             y = factor();
             Compute(op, x, y);
         }
 
         return x;
     }
 
     private static Result factor() {
         //factor = ident | number | “(“ expression “)” | funcCall .
         Result x = new Result();
         boolean rtn = true;
 
         if (scn.sym == Scanner.numberToken) { // number
             x.value = scn.val;
             x.setConst();
             scn.Next();
         } else if (scn.sym == Scanner.identToken) { // ident
             x.setVar();
             x.address = LookupAddress(scn.id);
             scn.Next();
         } else if (scn.sym == Scanner.openparenToken) { // "("
             scn.Next();
 
             x = expression();
 
             CheckFor(Scanner.closeparenToken); // ")"
             scn.Next();
 
         } else if (scn.sym == Scanner.callToken) { // call
             x = funcCall();
         } else {
             rtn = false;
         }
 
         if (!rtn) {
             Error("factor");
         }
         return x;
     }
 
     private static Result execFunc(Integer func, ArrayList<Result> funcArgs) {
         String funcName = scn.Id2String(func);
 
         /* RDI a R.a := read a decimal number from the input F2 50
          * WRD b write the contents of R.b to the output in decimal F2 51
          * WRL start a new line on the output F1 53
          */
         Result x = new Result();
 
         if (funcName.equals("outputnum")) {
             if (!funcArgs.get(0).isReg()) {
                 load(funcArgs.get(0));
             }
             PutF2(WRD, 0, funcArgs.get(0).regno, 0);
 //            Deallocate(funcArgs.get(0));
         } else if (funcName.equals("outputnewline")) {
             PutF1(WRL, 0, 0, 0);
         } else if (funcName.equals("inputnum")) {
             //TODO get mem location, read number to it as var
             x.regno = AllocateReg();
             load(x);
             PutF2(RDI, x.regno, 0, 0);
 //            Deallocate(x);
         }
         return x;
     }
 
     private static void Compute(int scnOp, Result x, Result y) {
         if (x.isConst() && y.isConst()) {
             switch (scnOp) {
                 case Scanner.plusToken:
                     x.value += y.value;
                     break;
                 case Scanner.minusToken:
                     x.value -= y.value;
                     break;
                 case Scanner.timesToken:
                     x.value *= y.value;
                     break;
                 case Scanner.divToken:
                     x.value /= y.value;
                     break;
             }
         } else {
             load(x);
             if (x.regno == 0) {
                 x.regno = AllocateReg();
                 PutF1(ADD, x.regno, 0, 0);
             }
             if (y.isConst()) {
                 PutF1(opCodeImm(scnOp), x.regno, x.regno, y.value);
             } else {
                 load(y);
                 PutF1(opCode(scnOp), x.regno, x.regno, y.regno);
                 Deallocate(y);
             }
         }
     }
 
     private static void PutF1(int op, int a, int b, int c) {
         buf.add(pc++, op << 26 | a << 21 | b << 16 | c & 0xffff);
     }
 
     private static void PutF2(int op, int a, int b, int c) {
         buf.add(pc++, op << 26 | a << 21 | b << 16 | c & 0x1f);
     }
 
     private static void PutF3(int op, int c) {
         buf.add(pc++, op << 26 | c & 0xffffff);
     }
 
     private static void CheckFor(int token) {
         if (scn.sym != token) {
             Error("CheckFor: Syntax Error: " + scn.Id2String(token));
         }
     }
 
     private static void load(Result x) {
         if (x.isVar()) {
             x.regno = AllocateReg();
             PutF1(LDW, x.regno, GV, -x.address);
             x.setReg();
         } else if (x.isConst()) {
             if (x.value == 0) {
                 x.regno = 0;
             } else {
                 x.regno = AllocateReg();
                 PutF1(ADDI, x.regno, 0, x.value);
             }
             x.setReg();
         }
     }
 
     private static void CondNegBraFwd(Result x) {
         x.fixuplocation = pc;
        PutF1(negatedBranchOp(x.cond), x.regno, 0, 0);
     }
 
     private static void UnCondBraFwd(Result x) {
         PutF1(BEQ, 0, 0, x.fixuplocation);//Build linked list by storing previous value
         x.fixuplocation = pc - 1;
     }
 
     private static void Fixup(int loc) {
         int part = (0xffff0000 + (pc - loc));
        int fixed = (buf.get(loc) | 0x0000ffff) & part;
         buf.set(loc, fixed );
     }
 
     private static void FixAll(int loc) {
         int next;
         while (loc != 0) {
             next = buf.get(loc) & 0x0000ffff; //extract next element of linked list
             Fixup(loc);
             loc = next;
         }
     }
 
     private static void Deallocate(Result y) {
         R[y.regno] = true;
     }
 
     private static int AllocateReg() {
 //        R[30] = MemSize - 1;
         for (int i = 1; i < R.length; i++) {
             if (R[i] == true) {
                 R[i] = false;
                 return i;
             }
         }
         return 0;
     }
 
     private static int opCodeImm(int op) {
         int regCode = opCode(op);
         if (regCode != ERR) {
             return regCode + 16;
         }
         return ERR;
     }
 
     private static int opCode(int op) {
         switch (op) {
             case Scanner.plusToken:
                 return ADD;
             case Scanner.minusToken:
                 return SUB;
             case Scanner.timesToken:
                 return MUL;
             case Scanner.divToken:
                 return DIV;
             case CMP:
                 return CMP;
         }
         return ERR;
     }
 
     private static int negatedBranchOp(int cond) {
         switch (cond) {
             case Scanner.eqlToken:
                 return BNE;
             case Scanner.neqToken:
                 return BEQ;
             case Scanner.lssToken:
                 return BGE;
             case Scanner.leqToken:
                 return BGT;
             case Scanner.geqToken:
                 return BLT;
             case Scanner.gtrToken:
                 return BLE;
         }
         return ERR;
     }
 
     private static int LookupAddress(int id) {
         if (!vars.contains(id)) {
             return -1;
         }
         return vars.indexOf(id) * 4;
     }
 
     private static void GiveAddress(int id) {
         if (!vars.contains(id)) {
             vars.add(id);
         }
     }
 
     public int[] getProgram() {
         int[] ret = new int[buf.size()];
         for (int i = 0; i < buf.size(); i++) {
             ret[i] = buf.get(i);
         }
         return ret;
     }
 }
