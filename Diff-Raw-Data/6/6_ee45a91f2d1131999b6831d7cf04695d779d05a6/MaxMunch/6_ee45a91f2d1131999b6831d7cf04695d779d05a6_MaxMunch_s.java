 package tree;
 
 import assem.Instr;
 import assem.Targets;
 import util.List;
 import temp.Temp;
 import temp.Label;
 
 public class MaxMunch
 {
 	public List<Rest> info = null;
 	public List<Rest> last = null;
 
     public MaxMunch(Stm s) {
         if ( s == null )
             return;
         
         if ( s instanceof CJUMP )
             maxMunch( (CJUMP) s);
         else if ( s instanceof EXPSTM )
             maxMunch( (EXPSTM) s);
         else if ( s instanceof JUMP )
             maxMunch( (JUMP) s);
         else if ( s instanceof LABEL )
             maxMunch( (LABEL) s);
         else if ( s instanceof MOVE )
             maxMunch( (MOVE) s);
         else if ( s instanceof SEQ )
             maxMunch( (SEQ) s);
         else
             throw new Error("Unexpected: " + s.getClass());
 	}
 
 	private Rest maxMunch(Stm s)
 	{
 		return new Rest();
 	}
 
 	private Rest maxMunch(CJUMP s)
 	{
 		Rest r = new Rest();
 		Rest left = maxMunch(s.left);
 		Rest right = maxMunch(s.right);
 		r.addSrc(right.dst.head);
 		r.addDst(left.dst.head);
 		switch (s.op)
 		{
 			case CJUMP.EQ:
 				defineRest("eq `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.NE:
 				defineRest("ne `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.LT:
 				defineRest("lt `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.LE:
 				defineRest("le `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.GT:
 				defineRest("gt `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.GE:
 				defineRest("ge `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.ULT:
 				defineRest("ult `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.ULE:
 				defineRest("ule `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.UGT:
 				defineRest("ugt `d0, `s0", r, Rest.OPER);
 				break;
 			case CJUMP.UGE:
 				defineRest("uge `d0, `s0", r, Rest.OPER);
 				break;
 			default:
 				System.out.println("Wrong OP on CJUMP");
 				break;
 		}
 
 		r = new Rest();
 		defineRest("je " + s.ifTrue, r, Rest.OPER);
 
 		r = new Rest();
 		defineRest("jne " + s.ifFalse, r, Rest.OPER);
 	
 		return r;
 	}
 
 	private Rest maxMunch(EXPSTM s)
 	{
 		return maxMunch(s.exp);
 	}
 
 	private Rest maxMunch(JUMP s)
 	{
 		Rest r = new Rest();
 		r.targets = s.targets;
 		defineRest("jmp `j0", r, Rest.OPER_TARGET);
 		return r;
 	}
 
 	private Rest maxMunch(LABEL s){
 		Rest r = new Rest();
 		r.label = s.label;
 		System.out.println(" Label : " + s.getClass() + s.label);
 		defineRest(s.label + ":", r, Rest.LABEL);
 		return r;
 	}
 
 	private Rest maxMunch(SEQ s)
 	{
 		maxMunch(s.left);
 		maxMunch(s.right);
 		return new Rest();
 	}
 
     private Rest maxMunch(MOVE s) {
 		Rest r = new Rest();
 		if (s.dst instanceof TEMP)
 		{
 			TEMP t = (TEMP)(s.dst);
 			r.addDst(t.temp);
 		}
 		else
 		{
 			Rest rr = maxMunch(s.dst);
 			r.addDst(rr.dst.head);
 		}
 
 		if (s.src instanceof TEMP)
 		{
 			TEMP t = (TEMP)(s.src);
 			r.addSrc(t.temp);
 		}
 		else
 		{
 			Rest rr = maxMunch(s.src);
 			r.addSrc(rr.dst.head);
 		}
 
 		defineRest("mov `d0, `s0", r, Rest.MOVE);
 		return r;
 	}
 
 
 	private Rest maxMunch(Exp e){		
 		System.out.println(" Exp: " + e.getClass());
 		Rest r = null;
         if ( e == null )
             return r;
         
         if ( e instanceof BINOP )
             r = maxMunch( (BINOP) e);
         else if ( e instanceof CALL ){
             r = maxMunch( (CALL) e);
 		}
         else if ( e instanceof CONST )
             r = maxMunch( (CONST) e);
         else if ( e instanceof ESEQ )
             r = maxMunch( (ESEQ) e);
         else if ( e instanceof MEM )
             r = maxMunch( (MEM) e);
         else if ( e instanceof NAME )
             r = maxMunch( (NAME) e);
         else if ( e instanceof TEMP )
             r = maxMunch( (TEMP) e);
         else
             throw new Error("Unexpected: " + e.getClass());
 		return r;
 	}
 
 	private Rest maxMunch(BINOP e){		
 		System.out.println(" uppercase: " + e.getClass());
 		Rest r = new Rest();
 		r.addSrc(((Rest)maxMunch(e.right)).dst.head);
 		r.addDst(((Rest)maxMunch(e.left)).dst.head);
 		switch (e.binop)
 		{
 			case BINOP.PLUS:
 				defineRest("add `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.MINUS:
 				defineRest("sub `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.TIMES:
 				defineRest("imul `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.DIV:
 				defineRest("idiv `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.AND:
 				defineRest("and `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.OR:
 				defineRest("or `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.LSHIFT:
 				defineRest("shl `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.RSHIFT:
 				defineRest("shr `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.ARSHIFT:
 				defineRest("arsh `d0, `s0", r, Rest.OPER);
 				break;
 			case BINOP.XOR:
 				defineRest("xor `d0, `s0", r, Rest.OPER);
 				break;
 			default:
 				System.out.println("Wrong binop");
 		}
     
 		return r;
 	}
 
 	private Rest maxMunch(CALL e){		
 		Rest r = new Rest();
 		for (List<Exp> arg = e.args; arg != null; arg = arg.tail)
 		{
 			Rest ra = maxMunch(arg.head);
 			ra.addDst(ra.dst.head);
 			defineRest("push `d0", ra, Rest.OPER);
 		}
 		r.addDst(new Temp());
 
 		Rest rr = maxMunch(e.func);
 		defineRest("call " + rr.label, r, Rest.OPER);
 
 		rr = new Rest();
 		Temp t = new Temp();
 		rr.addDst(t);
 		defineRest("pop `d0", rr, Rest.OPER);
 		return rr;
 	}
 
 	private Rest maxMunch(CONST e){		
 		System.out.println(" uppercase: " + e.getClass());
 		
 		Rest r = new Rest();
 		Temp t = new Temp();
 		r.addDst(t);
 		defineRest("xor `d0, `d0", r, Rest.OPER);
 
 		r = new Rest();
 		r.addDst(t);
 		defineRest("add `d0, " + e.value, r, Rest.OPER);
 
 		r = new Rest();
 		r.addDst(t);
 		return r;
 	}
 
 	private Rest maxMunch(ESEQ e){		
 		System.out.println(" uppercase: " + e.getClass());
 		maxMunch(e.stm);
 		return maxMunch(e.exp);
 	}
 
 	private Rest maxMunch(MEM e){
 		System.out.println(" uppercase: " + e.getClass());
 		Rest r = maxMunch(e.exp);
 		r.addSrc(r.dst.head);
 		r.addDst(new Temp());
 		// FIXME
 		defineRest("mov `d0, [`s0]", r, Rest.MOVE);
 		return r;
 	}
 
 	private Rest maxMunch(NAME e){		
 		Rest r = new Rest();
 		r.label = e.label;
 		return r;
 	}
 
 	private Rest maxMunch(TEMP e){		
 		System.out.println(" uppercase: " + e.getClass());
 		return new Rest();}
 
 	private void defineRest(String cmd, Rest r, int type){
 		r.cmd = cmd;
 		r.type = type;
 		
 		if (info == null)
 			last = info = new List<Rest>(r, null);
 		else 
 			last = last.tail = new List<Rest>(r, null);
 	}
 }
     
