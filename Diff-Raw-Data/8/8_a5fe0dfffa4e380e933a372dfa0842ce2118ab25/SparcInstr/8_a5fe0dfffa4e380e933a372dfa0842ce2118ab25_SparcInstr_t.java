 //-----------------------------------------------------------------------------
 // Common SPARC Instructions
 // This static final singleton class provides string literals for
 // 131 programming assignment 2. Each is a SPARC instruction
 //-----------------------------------------------------------------------------
 
 
 // I recommend defining all operations as well as formats.  
 // Operations are things like add, mul, set, etc.
 // Formats are like {OPERATION} {REG_1}, {REG_2}, {REG_3} etc.
 
 class SparcInstr
 {
     //-----------------------------------------------------------------------------
     //      Directives
     //-----------------------------------------------------------------------------
     public static final String FILE_DIR    = ".file";
     public static final String IDENT_DIR   = ".ident";
     public static final String SECTION_DIR = ".section";
     public static final String GLOBAL_DIR  = ".global";
     public static final String ALIGN_DIR   = ".align";
     public static final String ASCIZ_DIR   = ".asciz";
     public static final String SKIP_DIR    = ".skip";
     public static final String WORD_DIR    = ".word";
 
     //-----------------------------------------------------------------------------
     //      Sections
     //-----------------------------------------------------------------------------
     public static final String TEXT_SEC   = "\".text\"";
     public static final String DATA_SEC   = "\".data\""; 
     public static final String BSS_SEC    = "\".bss\""; 
     public static final String RODATA_SEC = "\".rodata\""; 
 
     //-----------------------------------------------------------------------------
     //      Registers
     //-----------------------------------------------------------------------------
 
     public static final String REG_FRAME = "%fp";
     public static final String REG_STACK = "%sp";
 
     // Global
     public static final String REG_GLOBAL0 = "%g0";
     public static final String REG_GLOBAL1 = "%g1";
     public static final String REG_GLOBAL2 = "%g2";
     public static final String REG_GLOBAL3 = "%g3";
     public static final String REG_GLOBAL4 = "%g4";
     public static final String REG_GLOBAL5 = "%g5";
     public static final String REG_GLOBAL6 = "%g6";
     public static final String REG_GLOBAL7 = "%g7";
 
     // Local
     public static final String REG_LOCAL0 = "%l0";
     public static final String REG_LOCAL1 = "%l1";
     public static final String REG_LOCAL2 = "%l2";
     public static final String REG_LOCAL3 = "%l3";
     public static final String REG_LOCAL4 = "%l4";
     public static final String REG_LOCAL5 = "%l5";
     public static final String REG_LOCAL6 = "%l6";
     public static final String REG_LOCAL7 = "%l7";
 
     // Input
     public static final String REG_INPUT0 = "%i0";
     public static final String REG_INPUT1 = "%i1";
     public static final String REG_INPUT2 = "%i2";
     public static final String REG_INPUT3 = "%i3";
     public static final String REG_INPUT4 = "%i4";
     public static final String REG_INPUT5 = "%i5";
 
     // Output
     public static final String REG_OUTPUT0 = "%o0";
     public static final String REG_OUTPUT1 = "%o1";
     public static final String REG_OUTPUT2 = "%o2";
     public static final String REG_OUTPUT3 = "%o3";
     public static final String REG_OUTPUT4 = "%o4";
     public static final String REG_OUTPUT5 = "%o5";
 
     // Float
     public static final String REG_FLOAT0 = "%f0";
     public static final String REG_FLOAT1 = "%f1";
     public static final String REG_FLOAT2 = "%f2";
     public static final String REG_FLOAT3 = "%f3";
     public static final String REG_FLOAT4 = "%f4";
     public static final String REG_FLOAT5 = "%f5";
 
     // Pseudonyms - For ease of programming
     public static final String REG_PARAM0 = REG_INPUT0;
     public static final String REG_PARAM1 = REG_INPUT1;
     public static final String REG_PARAM2 = REG_INPUT2;
     public static final String REG_PARAM3 = REG_INPUT3;
     public static final String REG_PARAM4 = REG_INPUT4;
     public static final String REG_PARAM5 = REG_INPUT5;
 
     public static final String REG_ARG0 = REG_OUTPUT0;
     public static final String REG_ARG1 = REG_OUTPUT1;
     public static final String REG_ARG2 = REG_OUTPUT2;
     public static final String REG_ARG3 = REG_OUTPUT3;
     public static final String REG_ARG4 = REG_OUTPUT4;
     public static final String REG_ARG5 = REG_OUTPUT5;
 
     public static final String REG_SET_RETURN = REG_INPUT0;
     public static final String REG_GET_RETURN = REG_OUTPUT0;
 
     //-----------------------------------------------------------------------------
     //      Constants
     //-----------------------------------------------------------------------------
     public static final String ENDL    = "_endl";
     public static final String INTFMT  = "_intFmt";
     public static final String BOOLFMT = "_boolFmt";
     public static final String BOOLT   = "_boolT";
     public static final String BOOLF   = "_boolF";
     public static final String STRFMT  = "_strFmt";
 
     //-----------------------------------------------------------------------------
     //      Comment
     //-----------------------------------------------------------------------------
     public static final String COMMENT  = "!";
 
     //-----------------------------------------------------------------------------
     //      Separator
     //-----------------------------------------------------------------------------
     public static final String SEPARATOR = "\t";
     public static final String INDENTOR  = "\t";    // tabs might actually be better :(
     //public static final String INDENTOR  = "    ";
     
     //-----------------------------------------------------------------------------
     //      Save
     //-----------------------------------------------------------------------------
     public static final String SAVE_OP   = "save";
     public static final String SAVE_WORD = "SAVE";
 
     //-----------------------------------------------------------------------------
     //      Set
     //-----------------------------------------------------------------------------
     public static final String SET_OP = "set";
 
     //-----------------------------------------------------------------------------
     //      Ret
     //-----------------------------------------------------------------------------
     public static final String RET_OP = "ret";
 
     //-----------------------------------------------------------------------------
     //      Restore
     //-----------------------------------------------------------------------------
     public static final String RESTORE_OP  = "restore";
     
     //-----------------------------------------------------------------------------
     //      Move
     //-----------------------------------------------------------------------------
     public static final String MOV_OP = "mov";
 
     //-----------------------------------------------------------------------------
     //      Simple Arithmetic
     //-----------------------------------------------------------------------------
     public static final String ADD_OP = "add";
     public static final String ADDCC_OP = "addcc";
     public static final String SUB_OP = "sub";
     public static final String SUBCC_OP = "subcc";
     
     //-----------------------------------------------------------------------------
     //      Increment/Decrement
     //-----------------------------------------------------------------------------
     public static final String INC_OP  = "inc";
     public static final String INCCC_OP = "inccc";
     public static final String DEC_OP  = "dec";
     public static final String DECCC_OP = "deccc";
     
     //-----------------------------------------------------------------------------
     //      Fitos
     //-----------------------------------------------------------------------------
     public static final String FITOS_OP = "fitos";
 
     //-----------------------------------------------------------------------------
     //      Shifting
     //-----------------------------------------------------------------------------
     public static final String SLL_OP = "sll";
     public static final String SRL_OP = "srl";
     public static final String SRA_OP = "sra";
 
     //-----------------------------------------------------------------------------
     //      Load
     //-----------------------------------------------------------------------------
     public static final String LOAD_OP = "ld";
 
     //-----------------------------------------------------------------------------
     //      Store
     //-----------------------------------------------------------------------------
     public static final String STORE_OP = "st";
 
     //-----------------------------------------------------------------------------
     //      Compare
     //-----------------------------------------------------------------------------
     public static final String COMP_OP = "cmp";
 
     //-----------------------------------------------------------------------------
     //      Branch
     //-----------------------------------------------------------------------------
     public static final String BG_OP  = "bg";
     public static final String BGE_OP = "bge";
     public static final String BL_OP  = "bl";
     public static final String BE_OP  = "be";
     public static final String BNE_OP = "bne";
     public static final String BA_OP  = "ba";
     public static final String BN_OP  = "bn";
     
     //-----------------------------------------------------------------------------
     //      Call
     //-----------------------------------------------------------------------------
     public static final String CALL_OP = "call";
     public static final String NOP_OP  = "nop";
     
     //-----------------------------------------------------------------------------
     //      Multiplication/Division/Modulus Arithmetic
     //-----------------------------------------------------------------------------
     public static final String MUL_OP = "mul";
     public static final String DIV_OP = "div";
     public static final String REM_OP = "rem";
 
     //-----------------------------------------------------------------------------
     //      Negating/2's Complement
     //-----------------------------------------------------------------------------
     public static final String NEG_OP = "neg";
 
     //-----------------------------------------------------------------------------
     //      Clear Register
     //-----------------------------------------------------------------------------
     public static final String CLR_OP = "clr";
 
     //-----------------------------------------------------------------------------
     //      Bitwise Ops
     //-----------------------------------------------------------------------------
     public static final String AND_OP   = "and";
     public static final String ANDCC_OP = "andcc";
     public static final String OR_OP    = "or";
     public static final String ORCC     = "orcc";
     public static final String XOR_OP   = "xor";
     public static final String XORCC_OP = "xorcc";
 
     //-----------------------------------------------------------------------------
     //      Functions
     //-----------------------------------------------------------------------------
     public static final String PRINTF = "printf";
     public static final String PRINTFLOAT = "printFloat";
     
     //-----------------------------------------------------------------------------
     //      Precisions
     //-----------------------------------------------------------------------------
     public static final String SINGLEP = ".single";
     
     //-----------------------------------------------------------------------------
     //      Templates
     //-----------------------------------------------------------------------------
     public static final String LINE          = "%s\n";
     public static final String LABEL         = "%s:\n";
     public static final String BLANK_LINE    = "\n";
 
     public static final String NO_PARAM      = "%s\n";
     public static final String ONE_PARAM     = "%s" + SEPARATOR + "%s\n";
     public static final String TWO_PARAM     = "%s" + SEPARATOR + "%s, %s\n";
     public static final String THREE_PARAM   = "%s" + SEPARATOR + "%s, %s, %s\n";
 
    public static final String NO_PARAM_COMM    = "%s" + SEPARATOR + SEPARATOR + "! %s\n";
    public static final String ONE_PARAM_COMM   = "%s" + SEPARATOR + "%s" + SEPARATOR + SEPARATOR + " %s\n";
    public static final String TWO_PARAM_COMM   = "%s" + SEPARATOR + "%s, %s" + SEPARATOR + SEPARATOR + SEPARATOR + " %s\n";
    public static final String THREE_PARAM_COMM = "%s" + SEPARATOR + "%s, %s, %s" + SEPARATOR + SEPARATOR + " %s\n";
 
     public static final String SAVE_FUNC     = SAVE_WORD + ".%s = -(%s + %s) & -8\n";
     public static final String RO_DEFINE     = "%s:" + SEPARATOR + "%s" + SEPARATOR + "%s\n";
     public static final String GLOBAL_DEFINE = "%s:" + SEPARATOR + "%s" + SEPARATOR + "%s\n";
 
 
 }
