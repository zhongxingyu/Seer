 /*
 
     ATerm -- The ATerm (Annotated Term) library
     Copyright (C) 1998-2000  Stichting Mathematisch Centrum, Amsterdam, 
                              The  Netherlands.
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 
 */
 
 package aterm.pure;
 
 import aterm.*;
 
 import java.lang.ref.*;
 import java.io.*;
 import java.util.*;
 
 public class PureFactory
   implements ATermFactory
 {
   private static int DEFAULT_TERM_TABLE_SIZE = 43117;
   private static int DEFAULT_AFUN_TABLE_SIZE = 2003;;
   private static int DEFAULT_PATTERN_CACHE_SIZE = 4321;
 
   private int term_table_size;
   private HashedWeakRef[] term_table;
 
   private int afun_table_size;
   private HashedWeakRef[] afun_table;
 
   static protected ATermList empty;
 
   //{{{ public PureFactory()
 
   public PureFactory()
   {
     this(DEFAULT_TERM_TABLE_SIZE, DEFAULT_AFUN_TABLE_SIZE);
   }
 
   //}}}
   //{{{ public PureFactory(int term_table_size, int afun_table_size)
 
   public PureFactory(int term_table_size, int afun_table_size)
   {
     this.term_table_size = term_table_size;
     this.term_table = new HashedWeakRef[term_table_size];
 
     this.afun_table_size = afun_table_size;
     this.afun_table = new HashedWeakRef[afun_table_size];
 
     empty = new ATermListImpl(this, null, null);
     term_table[empty.hashCode() % this.term_table_size] = new HashedWeakRef(empty, null);
   }
 
   //}}}
   
   //{{{ public synchronized ATermInt makeInt(int val)
 
   public synchronized ATermInt makeInt(int val)
   {
     ATerm term;
     int hnr = ATermIntImpl.hashFunction(val);
     int idx = hnr % term_table_size;
 
     HashedWeakRef prev, cur;
     prev = null;
     cur  = term_table[idx];
     while (cur != null) {
       term = (ATerm)cur.get();
       if (term == null) {
 	// Found a reference to a garbage collected term, remove it to speed up lookups.
 	if (prev == null) {
 	  term_table[idx] = cur.next;
 	} else {
 	  prev.next = cur.next;
 	}
       } else {
 	if (term.getType() == ATerm.INT) {
 	  if (((ATermInt)term).getInt() == val) {
 	    return (ATermInt)term;
 	  }
 	}
       }
       cur = cur.next;
     }
     
     // No integer term with 'val' found, so let's create one!
     term = new ATermIntImpl(this, val);
     cur = new HashedWeakRef(term, term_table[idx]);
     term_table[idx] = cur;
 
     return (ATermInt)term;
   }
 
   //}}}
   //{{{ public synchronized ATermReal makeReal(double val)
 
   public synchronized ATermReal makeReal(double val)
   {
     ATerm term;
     int hnr = ATermRealImpl.hashFunction(val);
     int idx = hnr % term_table_size;
 
     HashedWeakRef prev, cur;
     prev = null;
     cur  = term_table[idx];
     while (cur != null) {
       term = (ATerm)cur.get();
       if (term == null) {
 	// Found a reference to a garbage collected term, remove it to speed up lookups.
 	if (prev == null) {
 	  term_table[idx] = cur.next;
 	} else {
 	  prev.next = cur.next;
 	}
       } else {
 	if (term.getType() == ATerm.REAL) {
 	  if (((ATermReal)term).getReal() == val) {
 	    return (ATermReal)term;
 	  }
 	}
       }
       cur = cur.next;
     }
     
     // No real term with 'val' found, so let's create one!
     term = new ATermRealImpl(this, val);
     cur = new HashedWeakRef(term, term_table[idx]);
     term_table[idx] = cur;
 
     return (ATermReal)term;
   }
 
   //}}}
   //{{{ public ATermList makeList()
 
   public ATermList makeList()
   {
     return empty;
   }
 
   //}}}
   //{{{ public ATermList makeList(ATerm singleton)
 
   public ATermList makeList(ATerm singleton)
   {
     return makeList(singleton, empty);
   }
 
   //}}}
   //{{{ public synchronized ATermList makeList(ATerm first, ATermList next)
 
   public synchronized ATermList makeList(ATerm first, ATermList next)
   {
     ATerm term;
     int hnr = ATermListImpl.hashFunction(first, next);
     int idx = hnr % term_table_size;
 
     HashedWeakRef prev, cur;
     prev = null;
     cur  = term_table[idx];
     while (cur != null) {
       term = (ATerm)cur.get();
       if (term == null) {
 	// Found a reference to a garbage collected term, remove it to speed up lookups.
 	if (prev == null) {
 	  term_table[idx] = cur.next;
 	} else {
 	  prev.next = cur.next;
 	}
       } else {
 	if (term.getType() == ATerm.LIST) {
 	  ATermList list = (ATermList)term;
 	  if (list.getFirst() == first && list.getNext() == next) {
 	    return list;
 	  }
 	}
       }
       cur = cur.next;
     }
     
     // No existing term found, so let's create one!
     term = new ATermListImpl(this, first, next);
     cur = new HashedWeakRef(term, term_table[idx]);
     term_table[idx] = cur;
 
     return (ATermList)term;
   }
 
   //}}}
   //{{{ public synchronized ATermPlaceholder makePlaceholder(ATerm type)
 
   public synchronized ATermPlaceholder makePlaceholder(ATerm type)
   {
     ATerm term;
     int hnr = ATermPlaceholderImpl.hashFunction(type);
     int idx = hnr % term_table_size;
 
     HashedWeakRef prev, cur;
     prev = null;
     cur  = term_table[idx];
     while (cur != null) {
       term = (ATerm)cur.get();
       if (term == null) {
 	// Found a reference to a garbage collected term, remove it to speed up lookups.
 	if (prev == null) {
 	  term_table[idx] = cur.next;
 	} else {
 	  prev.next = cur.next;
 	}
       } else {
 	if (term.getType() == ATerm.PLACEHOLDER) {
 	  ATermPlaceholder ph = (ATermPlaceholder)term;
 	  if (ph.getPlaceholder() == type) {
 	    return ph;
 	  }
 	}
       }
       cur = cur.next;
     }
     
     // No existing term found, so let's create one!
     term = new ATermPlaceholderImpl(this, type);
     cur = new HashedWeakRef(term, term_table[idx]);
     term_table[idx] = cur;
 
     return (ATermPlaceholder)term;
   }
 
   //}}}
   //{{{ public synchronized ATermBlob makeBlob(byte[] data)
 
   public synchronized ATermBlob makeBlob(byte[] data)
   {
     ATerm term;
     int hnr = ATermBlobImpl.hashFunction(data);
     int idx = hnr % term_table_size;
 
     HashedWeakRef prev, cur;
     prev = null;
     cur  = term_table[idx];
     while (cur != null) {
       term = (ATerm)cur.get();
       if (term == null) {
 	// Found a reference to a garbage collected term, remove it to speed up lookups.
 	if (prev == null) {
 	  term_table[idx] = cur.next;
 	} else {
 	  prev.next = cur.next;
 	}
       } else {
 	if (term.getType() == ATerm.BLOB) {
 	  ATermBlob blob = (ATermBlob)term;
 	  if (blob.getBlobData() == data) {
 	    return blob;
 	  }
 	}
       }
       cur = cur.next;
     }
     
     // No existing term found, so let's create one!
     term = new ATermBlobImpl(this, data);
     cur = new HashedWeakRef(term, term_table[idx]);
     term_table[idx] = cur;
 
     return (ATermBlob)term;
   }
 
   //}}}
 
   //{{{ public synchronized AFun makeAFun(String name, int arity, boolean isQuoted)
 
   public synchronized AFun makeAFun(String name, int arity, boolean isQuoted)
   {
     AFun fun;
     int hnr = AFunImpl.hashFunction(name, arity, isQuoted);
     int idx = hnr % afun_table_size;
 
     name = name.intern();
     HashedWeakRef prev, cur;
     prev = null;
     cur  = afun_table[idx];
     while (cur != null) {
       fun = (AFun)cur.get();
       if (fun == null) {
 	// Found a reference to a garbage collected term, remove it to speed up lookups.
 	if (prev == null) {
 	  afun_table[idx] = cur.next;
 	} else {
 	  prev.next = cur.next;
 	}
       } else {
 	// use == because name is interned.
 	if (fun.getName() == name && fun.getArity() == arity &&
 	    fun.isQuoted() == isQuoted) {
 	  return fun;
 	}
       }
       cur = cur.next;
     }
     
     // No similar AFun found, so build a new one
     fun = new AFunImpl(name, arity, isQuoted);
     cur = new HashedWeakRef(fun, afun_table[idx]);
     afun_table[idx] = cur;
 
     return fun;
   }
 
   //}}}
   //{{{ public synchronized ATermAppl makeAppl(AFun fun, ATerm[] args)
 
   public synchronized ATermAppl makeAppl(AFun fun, ATerm[] args)
   {
     ATerm term;
     int hnr;
     int idx;
     HashedWeakRef prev, cur;
 
     if (fun.getArity() != args.length) {
       throw new IllegalArgumentException("arity does not match argument count: " +
 					 fun.getArity() + " != " + args.length);
     }
 
     hnr = ATermApplImpl.hashFunction(fun, args);
     idx = hnr % term_table_size;
 
     prev = null;
     cur  = term_table[idx];
     while (cur != null) {
       term = (ATerm)cur.get();
       if (term == null) {
 	// Found a reference to a garbage collected term, remove it to speed up lookups.
 	if (prev == null) {
 	  term_table[idx] = cur.next;
 	} else {
 	  prev.next = cur.next;
 	}
       } else {
 	if (term.getType() == ATerm.APPL) {
 	  ATermAppl appl = (ATermAppl)term;
 	  if (appl.getAFun() == fun) {
 	    ATerm[] appl_args = appl.getArgumentArray();
 	    if (appl_args.length == args.length) {
 	      boolean found = true;
 	      for (int i=0; i<args.length; i++) {
 		if(appl_args[i] != args[i]){
 		  found = false;
 		  break;
 		}
 	      }
 	      if (found) {
 		return appl;
 	      }
 	    }
 	  }
 	}
       }
       cur = cur.next;
     }
     
     // No existing term found, so let's create one!
     term = new ATermApplImpl(this, fun, args);
     cur = new HashedWeakRef(term, term_table[idx]);
     term_table[idx] = cur;
 
     return (ATermAppl)term;
   }
 
   //}}}
   //{{{ public synchronized ATermAppl makeAppl(AFun fun, ATermList args)
 
   public synchronized ATermAppl makeAppl(AFun fun, ATermList args)
   {
     ATerm[] arg_array;
 
     arg_array = new ATerm[args.getLength()];
 
     int i = 0;
     while (!args.isEmpty()) {
       arg_array[i++] = args.getFirst();
       args = args.getNext();
     }
 
     return makeAppl(fun, arg_array);
   }
 
   //}}}
   //{{{ public ATermAppl makeAppl(AFun fun)
 
   public ATermAppl makeAppl(AFun fun)
   {
     return makeAppl(fun, new ATerm[0]);
   }
 
   //}}}
   //{{{ public ATermAppl makeAppl(AFun fun, arg1)
 
   public ATermAppl makeAppl(AFun fun, ATerm arg)
   {
     ATerm[] args = { arg };
     return makeAppl(fun, args);
   }
 
   //}}}
   //{{{ public ATermAppl makeAppl(AFun fun, arg1, arg2)
 
   public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2)
   {
     ATerm[] args = { arg1, arg2 };
     return makeAppl(fun, args);
   }
 
   //}}}
   //{{{ public ATermAppl makeAppl(AFun fun, arg1, arg2, arg3)
 
   public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3)
   {
     ATerm[] args = { arg1, arg2, arg3 };
     return makeAppl(fun, args);
   }
 
   //}}}
   //{{{ public ATermAppl makeAppl(AFun fun, arg1, arg2, arg3, arg4)
 
   public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3, ATerm arg4)
   {
     ATerm[] args = { arg1, arg2, arg3, arg4 };
     return makeAppl(fun, args);
   }
 
   //}}}
   //{{{ public ATermAppl makeAppl(AFun fun, arg1, arg2, arg3, arg4, arg5)
 
   public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3,
 			    ATerm arg4, ATerm arg5)
   {
     ATerm[] args = { arg1, arg2, arg3, arg4, arg5};
     return makeAppl(fun, args);
   }
 
   //}}}
   //{{{ public ATermAppl makeAppl(AFun fun, arg1, arg2, arg3, arg4, arg5, arg6)
 
   public ATermAppl makeAppl(AFun fun, ATerm arg1, ATerm arg2, ATerm arg3,
 			    ATerm arg4, ATerm arg5, ATerm arg6)
   {
     ATerm[] args = { arg1, arg2, arg3, arg4, arg5, arg6 };
     return makeAppl(fun, args);
   }
 
   //}}}
 
   //{{{ public ATermList getEmpty()
 
   public ATermList getEmpty()
   {
     return empty;
   }
 
   //}}}
 
   //{{{ private ATerm parseNumber(ATermReader reader)
 
   private ATerm parseNumber(ATermReader reader)
     throws IOException
   {
     StringBuffer str = new StringBuffer();
     ATerm result;
 
     do {
       str.append((char)reader.getLastChar());
     } while(Character.isDigit((char)reader.read()));
 
     if(reader.getLastChar() != '.' && 
        reader.getLastChar() != 'e' && reader.getLastChar() != 'E') {
       int val;
       try {
 	val = Integer.parseInt(str.toString());
       } catch (NumberFormatException e) {
 	throw new ParseError("malformed int");
       }
       result = makeInt(val);
     } else {
       if(reader.getLastChar() == '.') {
 	str.append('.');
 	reader.read();
 	if(!Character.isDigit((char)reader.getLastChar()))
 	  throw new ParseError("digit expected");
 	do {
 	  str.append((char)reader.getLastChar());
 	} while(Character.isDigit((char)reader.read()));
       }
       if(reader.getLastChar() == 'e' || reader.getLastChar() == 'E') {
 	str.append((char)reader.getLastChar());
 	reader.read();
 	if(reader.getLastChar() == '-' || reader.getLastChar() == '+') {
 	  str.append((char)reader.getLastChar());
 	  reader.read();
 	}
 	if(!Character.isDigit((char)reader.getLastChar()))
 	  throw new ParseError("digit expected!");
 	do {
 	  str.append((char)reader.getLastChar());
 	} while(Character.isDigit((char)reader.read()));
       }
       double val;
       try {
 	val = Double.valueOf(str.toString()).doubleValue();
       } catch (NumberFormatException e) {
 	throw new ParseError("malformed real");
       }
       result = makeReal(val);    
     }
     return result;
   }
 
   //}}}
   //{{{ private String parseId(ATermReader reader)
 
   private String parseId(ATermReader reader)
     throws IOException
   {
     int c = reader.getLastChar();
     StringBuffer buf = new StringBuffer(32);
 
     do {
       buf.append((char)c);
       c = reader.read();
    } while (Character.isLetter((char)c) || c == '_' || c == '-');
 
     return buf.toString();
   }
 
   //}}}
   //{{{ private String parseString(ATermReader reader)
 
   private String parseString(ATermReader reader)
     throws IOException
   {
     boolean escaped;
     StringBuffer str = new StringBuffer();
     
     do {
       escaped = false;
       if(reader.read() == '\\') {
         reader.read();
 	escaped = true;
       }
 
       if(escaped) {
 	switch(reader.getLastChar()) {
 	case 'n':	str.append('\n');	break;
 	case 't':	str.append('\t');	break;
 	case 'b':	str.append('\b');	break;
 	case 'r':	str.append('\r');	break;
 	case 'f':	str.append('\f');	break;
 	case '\\':	str.append('\\');	break;
 	case '\'':	str.append('\'');	break;
 	case '\"':	str.append('\"');	break;
 	case '0':	case '1':	case '2':	case '3':
 	case '4':	case '5':	case '6':	case '7':
 	  str.append(reader.readOct());
 	  break;
 	default:	str.append('\\').append((char)reader.getLastChar());
 	} 
       } else if(reader.getLastChar() != '\"')
 	str.append((char)reader.getLastChar());
     } while(escaped || reader.getLastChar() != '"');
 
     return str.toString();
   }
 
   //}}}
   //{{{ private ATermList parseATerms(ATermReader reader)
 
   private ATermList parseATerms(ATermReader reader)
     throws IOException
   {
     ATerm[] terms = parseATermsArray(reader);
     ATermList result = empty;
     for (int i=terms.length-1; i>=0; i--) {
       result = makeList(terms[i], result);
     }
 
     return result;
   }
 
   //}}}
   //{{{ private ATerm[] parseATermsArray(ATermReader reader)
 
   private ATerm[] parseATermsArray(ATermReader reader)
     throws IOException
   {
     List list = new Vector();
     ATerm term;
 
     term = parseFromReader(reader);
     list.add(term);
     while (reader.getLastChar() == ',') {
       reader.readSkippingWS();
       term = parseFromReader(reader);
       list.add(term);
     } 
 
     ATerm[] array = new ATerm[list.size()];
     ListIterator iter = list.listIterator();
     int index = 0;
     while (iter.hasNext()) {
       array[index++] = (ATerm)iter.next();
     }
     return array;
   }
 
   //}}}
   //{{{ private ATerm parseFromReader(ATermReader reader)
 
   synchronized private ATerm parseFromReader(ATermReader reader)
     throws IOException
   {    
     ATerm result;
     int c;
     String funname;
 
     switch(reader.getLastChar()) {
       case -1:
 	throw new ParseError("premature EOF encountered.");
 
       case '[':
 	//{{{ Read a list
 
 	c = reader.readSkippingWS();
 	if (c == -1) {
 	  throw new ParseError("premature EOF encountered.");
 	}
 	
 	if(c == ']') {
 	  c = reader.readSkippingWS();
 	  result = (ATerm)empty;
 	} else {
 	  result = parseATerms(reader);
 	  if(reader.getLastChar() != ']') {
 	    throw new ParseError("expected ']' but got '" + (char)reader.getLastChar() + "'");
 	  }
 	  c = reader.readSkippingWS();
 	}
 
 	//}}}
 	break;
 
       case '<':
 	//{{{ Read a placeholder
 
 	c = reader.readSkippingWS();
 	ATerm ph = parseFromReader(reader);
 	
 	if (reader.getLastChar() != '>') {
 	  throw new ParseError("expected '>' but got '" + (char)reader.getLastChar() + "'");
 	}
 
 	c = reader.readSkippingWS();
 
 	result = makePlaceholder(ph);
 
 	//}}}
 	break;
 
       case '"':
 	//{{{ Read a quoted function
 
 	funname = parseString(reader);
 	
 	c = reader.readSkippingWS();
 	if (reader.getLastChar() == '(') {
 	  c = reader.readSkippingWS();
 	  if (c == -1) {
 	    throw new ParseError("premature EOF encountered.");
 	  }
 	  if (reader.getLastChar() == ')') {
 	    result = makeAppl(makeAFun(funname, 0, true));
 	  } else {
 	    ATerm[] list = parseATermsArray(reader);
 
 	    if(reader.getLastChar() != ')') {
 	      throw new ParseError("expected ')' but got '" + reader.getLastChar() + "'");
 	    }
 	    result = makeAppl(makeAFun(funname, list.length, true), list);
 	  }
 	  c = reader.readSkippingWS();
 	  if (c == -1) {
 	    throw new ParseError("premature EOF encountered.");
 	  }
 	} else {
 	  result = makeAppl(makeAFun(funname, 0, true));
 	}
 
 
 	//}}}
 	break;
 
       case '-':
       case '0':	case '1': case '2': case '3': case '4':
       case '5':	case '6': case '7': case '8': case '9':
         result = parseNumber(reader);
 	c = reader.skipWS();
 	break;
 
       default:
 	c = reader.getLastChar();
 	if (Character.isLetter((char)c)) {
 	  //{{{ Parse an unquoted function
 					 
 	  funname = parseId(reader);
 	  c = reader.skipWS();
 	  if (reader.getLastChar() == '(') {
 	    c = reader.readSkippingWS();
 	    if (c == -1) {
 	      throw new ParseError("premature EOF encountered.");
 	    }
 	    if (reader.getLastChar() == ')') {
 	      result = makeAppl(makeAFun(funname, 0, false));
 	    } else {
 	      ATerm[] list = parseATermsArray(reader);
 
 	      if(reader.getLastChar() != ')') {
 		throw new ParseError("expected ')' but got '" + reader.getLastChar() + "'");
 	      }
 	      result = makeAppl(makeAFun(funname, list.length, false), list);
 	    }
 	    c = reader.readSkippingWS();
 	  } else {
 	    result = makeAppl(makeAFun(funname, 0, false));
 	  }
 	  
 	  //}}}
 	} else {
 	  throw new ParseError("illegal character: " + reader.getLastChar());
 	}
     }
 	
     if(reader.getLastChar() == '{') {
       //{{{ Parse annotation
 
       ATermList annos;
       // Parse annotation
       if(reader.readSkippingWS() == '}') {
 	reader.readSkippingWS();
 	annos = empty;
       } else {
 	annos = parseATerms(reader);
 	if(reader.getLastChar() != '}') {
 	  throw new ParseError("'}' expected");
 	}
 	reader.readSkippingWS();
       }
       result = result.setAnnotations(annos);	
 
       //}}}
     }
 
     /* Parse some ToolBus anomalies for backwards compatibility */
     if(reader.getLastChar() == ':') {
       reader.read();
       ATerm anno = parseFromReader(reader);
       result = result.setAnnotation(parse("type"), anno);
     }
 
     if(reader.getLastChar() == '?') {
       reader.readSkippingWS();
       result = result.setAnnotation(parse("result"), parse("true"));
     }
 
     return result;    
   }
 
   //}}}
 
   //{{{ public ATerm parse(String trm)
 
   public ATerm parse(String trm)
   {
     try {
       ATermReader reader = new ATermReader(new StringReader(trm));
       reader.readSkippingWS();
       ATerm result = parseFromReader(reader);
       //System.out.println("parsing " + trm + " yields " + result);
       return result;
     } catch (IOException e) {
       throw new ParseError("premature end of string");
     }
   }
 
   //}}}
   //{{{ public ATerm make(String pattern, List args)
 
   public ATerm make(String pattern, List args)
   {
     return make(parse(pattern), args);
   }
 
   //}}}
   //{{{ public ATerm make(String pattern, arg1)
 
   public ATerm make(String pattern, Object arg1)
   {
     List args = new LinkedList();
     args.add(arg1);
     return make(pattern, args);
   }
 
   //}}}
   //{{{ public ATerm make(String pattern, arg1, arg2)
 
   public ATerm make(String pattern, Object arg1, Object arg2)
   {
     List args = new LinkedList();
     args.add(arg1);
     args.add(arg2);
     return make(pattern, args);
   }
 
   //}}}
   //{{{ public ATerm make(String pattern, arg1, arg2, arg3)
 
   public ATerm make(String pattern, Object arg1, Object arg2, Object arg3)
   {
     List args = new LinkedList();
     args.add(arg1);
     args.add(arg2);
     args.add(arg3);
     return make(pattern, args);
   }
 
   //}}}
   //{{{ public ATerm make(String pattern, arg1, arg2, arg3, arg4)
 
   public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
 		    Object arg4)
   {
     List args = new LinkedList();
     args.add(arg1);
     args.add(arg2);
     args.add(arg3);
     args.add(arg4);
     return make(pattern, args);
   }
 
   //}}}
   //{{{ public ATerm make(String pattern, arg1, arg2, arg3, arg4, arg5)
 
   public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
 		    Object arg4, Object arg5)
   {
     List args = new LinkedList();
     args.add(arg1);
     args.add(arg2);
     args.add(arg3);
     args.add(arg4);
     args.add(arg5);
     return make(pattern, args);
   }
 
   //}}}
   //{{{ public ATerm make(String pattern, arg1, arg2, arg3, arg4, arg5, arg6)
 
   public ATerm make(String pattern, Object arg1, Object arg2, Object arg3,
 		    Object arg4, Object arg5, Object arg6)
   {
     List args = new LinkedList();
     args.add(arg1);
     args.add(arg2);
     args.add(arg3);
     args.add(arg4);
     args.add(arg5);
     args.add(arg6);
     return make(pattern, args);
   }
 
   //}}}
   //{{{ public ATerm make(ATerm pattern, List args)
 
   public ATerm make(ATerm pattern, List args)
   {
     return pattern.make(args);
   }
 
   //}}}
 
   //{{{ public ATerm setAnnotation(ATerm term, ATerm label, ATerm anno)
 
   public ATerm setAnnotation(ATerm term, ATerm label, ATerm anno)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
   //{{{ public ATerm removeAnnotation(ATerm term, ATerm label)
 
   public ATerm removeAnnotation(ATerm term, ATerm label)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
   //{{{ public ATerm getAnnotation(ATerm term, ATerm label)
 
   public ATerm getAnnotation(ATerm term, ATerm label)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
 
   //{{{ public ATerm setAnnotations(ATerm term, ATerm annos)
 
   public ATerm setAnnotations(ATerm term, ATerm annos)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
   //{{{ public ATerm removeAnnotations(ATerm term)
 
   public ATerm removeAnnotations(ATerm term)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
 
   //{{{ ATerm parsePattern(String pattern)
 
   ATerm parsePattern(String pattern)
     throws ParseError
   {
     // <TODO>: cache patterns
     return parse(pattern);
   }
 
   //}}}
 
   //{{{ protected boolean isDeepEqual(ATermImpl t1, ATerm t2)
 
   protected boolean isDeepEqual(ATermImpl t1, ATerm t2)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
 
   //{{{ public ATerm readFromTextFile(InputStream stream)
 
   public ATerm readFromTextFile(InputStream stream)
     throws IOException
   {
     ATermReader reader = new ATermReader(new InputStreamReader(stream));
     reader.readSkippingWS();
     return parseFromReader(reader);
   }
 
   //}}}
   //{{{ public ATerm readFromBinaryFile(InputStream stream)
 
   public ATerm readFromBinaryFile(InputStream stream)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
   //{{{ public ATerm readFromFile(InputStream stream)
 
   public ATerm readFromFile(InputStream stream)
     throws IOException
   {
     return readFromTextFile(stream);
   }
 
   //}}}
 
   //{{{ public ATerm importTerm(ATerm term)
 
   public ATerm importTerm(ATerm term)
   {
     throw new RuntimeException("not yet implemented!");
   }
 
   //}}}
 
 }
 
 //{{{ class HashedWeakRef
 
 class HashedWeakRef extends WeakReference
 {
   protected HashedWeakRef next;
 
   public HashedWeakRef(Object object, HashedWeakRef next)
   {
     super(object);
     this.next = next;
   }
 }
 
 //}}}
 //{{{ class ATermReader
 
 class ATermReader
 {
   private Reader reader;
   private int last_char;
 
   public ATermReader(Reader reader)
   {
     this.reader = reader;
     last_char   = -1;
   }
 
   public int read()
     throws IOException
   {
     last_char = reader.read();
     return last_char;
   }
 
   public int readSkippingWS()
     throws IOException
   {
     do {
       last_char = reader.read();
     } while (Character.isWhitespace((char)last_char));
 
     return last_char;
 
   }
 
   public int skipWS()
     throws IOException
   {
     while (Character.isWhitespace((char)last_char)) {
       last_char = reader.read();
     }
 
     return last_char;
   }
 
   public int readOct()
     throws IOException
   {
     int val = Character.digit((char)last_char, 8);
     val += Character.digit((char)read(), 8);
 
     if(val < 0) {
       throw new ParseError("octal must have 3 octdigits.");
     }
 
     val += Character.digit((char)read(), 8);
 
     if(val < 0) {
       throw new ParseError("octal must have 3 octdigits");
     }
 
     return val;
   }
   
   public int getLastChar()
   {
     return last_char;
   }
 }
 
 //}}}
