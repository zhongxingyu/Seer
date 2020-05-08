 
 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 import java.util.*;
 
 
 class SymbolTable
 {
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public 
 	SymbolTable ()
 	{
 		m_nLevel = 0;
 		m_stkScopes = new Stack<Scope> ();
 		m_scopeGlobal = null;
 	}
 
 
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public void
 	insert (STO sto)
 	{
 		Scope		scope = m_stkScopes.peek ();
 
 		scope.InsertLocal (sto);
 	}
 
 
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public STO
 	accessGlobal (String strName)
 	{
 		return (m_scopeGlobal.access (strName));
 	}
 
 
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public STO
 	accessLocal (String strName)
 	{
 		Scope		scope = m_stkScopes.peek ();
 
 		return (scope.accessLocal (strName));
 	}
 
 
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public STO
 	access (String strName)
 	{
 		Stack		stk = new Stack ();
 		Scope		scope;
 		STO		stoReturn = null;	
 
		for (Enumeration<Scope> e = m_stkScopes.elements(); e.hasMoreElements(); )
 		{
 			scope = e.nextElement();
 			if ((stoReturn = scope.access (strName)) != null)
 				return	stoReturn;
 		}
 
 		return (null);
 	}
 
 
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public void
 	openScope ()
 	{
 		Scope		scope = new Scope();
 
 		//	The first scope created will be the global scope.
 		if (m_scopeGlobal == null)
 			m_scopeGlobal = scope;
 
 		m_stkScopes.push (scope);
 		m_nLevel++;
 	}
 
 
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public void
 	closeScope ()
 	{
 		m_stkScopes.pop ();
 		m_nLevel--;
 	}
 
 
 	//----------------------------------------------------------------
 	//
 	//----------------------------------------------------------------
 	public int
 	getLevel ()
 	{
 		return m_nLevel;
 	}
 
 
 	//----------------------------------------------------------------
 	//	This is the function currently being parsed.
 	//----------------------------------------------------------------
 	public FuncSTO		getFunc () { return m_func; }
 	public void		setFunc (FuncSTO sto) { m_func = sto; }
 
 
 //----------------------------------------------------------------
 //	Instance variables.
 //----------------------------------------------------------------
 	private Stack<Scope>  		m_stkScopes;
 	private int		m_nLevel;
 	private Scope		m_scopeGlobal;
 	private FuncSTO	        m_func = null;
 }
