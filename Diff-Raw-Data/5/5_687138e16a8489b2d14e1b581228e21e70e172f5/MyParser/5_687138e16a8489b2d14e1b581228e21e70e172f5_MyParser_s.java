 
 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 import java_cup.runtime.*;
 import java.util.Vector;
 
 
 
 class MyParser extends parser
 {
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public 
     MyParser (Lexer lexer, ErrorPrinter errors)
     {
         m_lexer = lexer;
         m_symtab = new SymbolTable ();
         m_errors = errors;
         m_nNumErrors = 0;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public boolean
     Ok ()
     {
         return (m_nNumErrors == 0);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public Symbol
     scan ()
     {
         Token        t = m_lexer.GetToken ();
 
         //    We'll save the last token read for error messages.
         //    Sometimes, the token is lost reading for the next
         //    token which can be null.
         m_strLastLexeme = t.GetLexeme ();
 
         switch (t.GetCode ())
         {
             case sym.T_ID:
             case sym.T_ID_U:
             case sym.T_STR_LITERAL:
             case sym.T_FLOAT_LITERAL:
             case sym.T_INT_LITERAL:
             case sym.T_CHAR_LITERAL:
                 return (new Symbol (t.GetCode (), t.GetLexeme ()));
             default:
                 return (new Symbol (t.GetCode ()));
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void
     syntax_error (Symbol s)
     {
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void
     report_fatal_error (Symbol s)
     {
         m_nNumErrors++;
         if (m_bSyntaxError)
         {
             m_nNumErrors++;
 
             //    It is possible that the error was detected
             //    at the end of a line - in which case, s will
             //    be null.  Instead, we saved the last token
             //    read in to give a more meaningful error 
             //    message.
             m_errors.print (Formatter.toString (ErrorMsg.syntax_error, m_strLastLexeme));
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void
     unrecovered_syntax_error (Symbol s)
     {
         report_fatal_error (s);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public void
     DisableSyntaxError ()
     {
         m_bSyntaxError = false;
     }
 
     public void
     EnableSyntaxError ()
     {
         m_bSyntaxError = true;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     public String 
     GetFile ()
     {
         return (m_lexer.getEPFilename ());
     }
 
     public int
     GetLineNum ()
     {
         return (m_lexer.getLineNumber ());
     }
 
     public void
     SaveLineNum ()
     {
         m_nSavedLineNum = m_lexer.getLineNumber ();
     }
 
     public int
     GetSavedLineNum ()
     {
         return (m_nSavedLineNum);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoProgramStart()
     {
         // Opens the global scope.
         m_symtab.openScope ();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoProgramEnd()
     {
         m_symtab.closeScope ();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoVarDecl (Vector<String> lstIDs)
     {
         for (int i = 0; i < lstIDs.size (); i++)
         {
             String id = lstIDs.elementAt (i);
         
             if (m_symtab.accessLocal (id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
             }
 
             VarSTO sto = new VarSTO (id);
             m_symtab.insert (sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoExternDecl (Vector<String> lstIDs)
     {
         for (int i = 0; i < lstIDs.size (); i++)
         {
             String id = lstIDs.elementAt (i);
 
             if (m_symtab.accessLocal (id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
             }
 
             VarSTO sto = new VarSTO (id);
             m_symtab.insert (sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoConstDecl (Vector<String> lstIDs)
     {
         for (int i = 0; i < lstIDs.size (); i++)
         {
             String id = lstIDs.elementAt (i);
 
             if (m_symtab.accessLocal (id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print (Formatter.toString (ErrorMsg.redeclared_id, id));
             }
         
             ConstSTO sto = new ConstSTO (id);
             m_symtab.insert (sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoTypedefDecl (Vector<String> lstIDs)
     {
         for (int i = 0; i < lstIDs.size (); i++)
         {
             String id = lstIDs.elementAt (i);
 
             if (m_symtab.accessLocal (id) != null)
             {
                 m_nNumErrors++;
                 m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
             }
         
             TypedefSTO sto = new TypedefSTO (id);
             m_symtab.insert (sto);
         }
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoStructdefDecl (String id)
     {
         if (m_symtab.accessLocal (id) != null)
         {
             m_nNumErrors++;
             m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
         }
         
         TypedefSTO sto = new TypedefSTO (id);
         m_symtab.insert (sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoFuncDecl_1 (String id)
     {
         if (m_symtab.accessLocal (id) != null)
         {
             m_nNumErrors++;
             m_errors.print (Formatter.toString(ErrorMsg.redeclared_id, id));
         }
     
         FuncSTO sto = new FuncSTO (id);
         m_symtab.insert (sto);
 
         m_symtab.openScope ();
         m_symtab.setFunc (sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoFuncDecl_2 ()
     {
         m_symtab.closeScope ();
         m_symtab.setFunc (null);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoFormalParams (Vector<String> params)
     {
         if (m_symtab.getFunc () == null)
         {
             m_nNumErrors++;
             m_errors.print ("internal: DoFormalParams says no proc!");
         }
 
         // insert parameters here
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoBlockOpen ()
     {
         // Open a scope.
         m_symtab.openScope ();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     void
     DoBlockClose()
     {
         m_symtab.closeScope ();
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoAssignExpr (STO stoDes)
     {
         if (!stoDes.isModLValue())
         {
             // Good place to do the assign checks
         }
         
         return stoDes;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoFuncCall (STO sto)
     {
         if (!sto.isFunc())
         {
             m_nNumErrors++;
             m_errors.print (Formatter.toString(ErrorMsg.not_function, sto.getName()));
             return (new ErrorSTO (sto.getName ()));
         }
 
         return (sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator2_Dot (STO sto, String strID)
     {
         // Good place to do the struct checks
 
         return sto;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator2_Array (STO sto)
     {
         // Good place to do the array checks
 
         return sto;
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator3_GlobalID (String strID)
     {
         STO sto;
 
         if ((sto = m_symtab.accessGlobal (strID)) == null)
         {
             m_nNumErrors++;
              m_errors.print (Formatter.toString(ErrorMsg.error0g_Scope, strID));    
             sto = new ErrorSTO (strID);
         }
         return (sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoDesignator3_ID (String strID)
     {
         STO sto;
 
         if ((sto = m_symtab.access (strID)) == null)
         {
             m_nNumErrors++;
              m_errors.print (Formatter.toString(ErrorMsg.undeclared_id, strID));    
             sto = new ErrorSTO (strID);
         }
         return (sto);
     }
 
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoQualIdent (String strID)
     {
         STO sto;
 
         if ((sto = m_symtab.access (strID)) == null)
         {
             m_nNumErrors++;
              m_errors.print (Formatter.toString(ErrorMsg.undeclared_id, strID));    
             return (new ErrorSTO (strID));
         }
 
         if (!sto.isTypedef())
         {
             m_nNumErrors++;
             m_errors.print (Formatter.toString(ErrorMsg.not_type, sto.getName ()));
             return (new ErrorSTO (sto.getName ()));
         }
 
         return (sto);
     }
 
     //----------------------------------------------------------------
     //
     //----------------------------------------------------------------
     STO
     DoArithOp (String op, STO operand1, STO operand2)
     {
         STO sto;
         // Check #1 - Modulus - both int
         if(op.equal("%"))
         {
             // Check left operand to be int
             if((!operand1.getType().isInt()))
             {
                 m_nNumErrors++;
                 m_errors.print (Formatter.toString(ErrorMsg.error1w_Expr, operand1.getType().getName(), op, "int"));    
                 return (new ErrorSTO (operand1.getName()));
             }
             // Check right operand to be int
             else if((!operand2.getType().isInt()))
             {
                 m_nNumErrors++;
                 m_errors.print (Formatter.toString(ErrorMsg.error1w_Expr, operand2.getType().getName(), op, "int"));    
                 return (new ErrorSTO (operand2.getName()));
             }
 
         }
 
         // Check #1 - Plus, Minus, Star, Slash - Both operands numeric
         // Check left operand to be numeric
         if((!operand1.getType().isNumeric()))
         {
             m_nNumErrors++;
            m_errors.print (Formatter.toString(ErrorMsg.error1n_Expr, operand1.getName()));    
             return (new ErrorSTO (operand1.getName()));
         }
         // Check right operand to be numeric
         else if((!operand2.getType().isNumeric()))
         {
             m_nNumErrors++;
            m_errors.print (Formatter.toString(ErrorMsg.error1n_Expr, operand2.getName()));    
             return (new ErrorSTO (operand2.getName()));
         }
         
         // Check successful, determine result type
         if(operand1.getType().isInt() && operand2.getType().isInt())
         {
             sto = new ExprSTO("DoAddOp Result", new IntType());
         }
         else
         {
             sto = new ExprSTO("DoAddOp Result", new FloatType());
         }
 
 
         return (sto);
     }
 
 
 //----------------------------------------------------------------
 //    Instance variables
 //----------------------------------------------------------------
     private Lexer            m_lexer;
     private ErrorPrinter        m_errors;
     private int             m_nNumErrors;
     private String            m_strLastLexeme;
     private boolean            m_bSyntaxError = true;
     private int            m_nSavedLineNum;
 
     private SymbolTable        m_symtab;
 }
