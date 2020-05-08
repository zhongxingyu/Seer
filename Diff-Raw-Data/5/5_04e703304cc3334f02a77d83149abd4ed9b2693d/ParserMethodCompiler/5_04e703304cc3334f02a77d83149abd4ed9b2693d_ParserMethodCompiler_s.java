 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.parser;
 
 import org.vesalainen.bcc.LookupList;
 import org.vesalainen.bcc.MethodCompiler;
 import org.vesalainen.bcc.type.MethodWrapper;
 import org.vesalainen.bcc.ObjectType;
 import org.vesalainen.lpg.Act;
 import org.vesalainen.lpg.Action;
 import org.vesalainen.lpg.LALRKParserGenerator;
 import org.vesalainen.grammar.Nonterminal;
 import org.vesalainen.grammar.Symbol;
 import org.vesalainen.grammar.GRule;
 import org.vesalainen.grammar.GTerminal;
 import org.vesalainen.lpg.Goto;
 import org.vesalainen.lpg.LaReduce;
 import org.vesalainen.lpg.LaShift;
 import org.vesalainen.lpg.LaState;
 import org.vesalainen.lpg.Lr0State;
 import org.vesalainen.lpg.Reduce;
 import org.vesalainen.lpg.ReduceAct;
 import org.vesalainen.lpg.Shift;
 import org.vesalainen.lpg.ShiftReduceAct;
 import org.vesalainen.lpg.State;
 import org.vesalainen.lpg.TerminalAction;
 import org.vesalainen.parser.annotation.ParserContext;
 import org.vesalainen.parser.util.InputReader;
 import java.io.File;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import org.vesalainen.bcc.Block;
 import org.vesalainen.bcc.MethodImplementor;
 import org.vesalainen.bcc.type.Generics;
 import org.vesalainen.grammar.Grammar;
 import org.vesalainen.grammar.GrammarException;
 import org.vesalainen.parser.util.HtmlPrinter;
 import org.vesalainen.parser.util.NumSet;
 import org.vesalainen.parser.util.Reducers;
 import org.vesalainen.parser.util.SystemErrPrinter;
 
 /**
  * ParserMethodCompiler class compiles Grammar into a Parser subclass.
  * @author tkv
  */
 public class ParserMethodCompiler implements MethodImplementor, ParserConstants
 {
     // ParserInfo methods
 
     private ParserCompiler parserCompiler;
 
     private Grammar g;
     private LALRKParserGenerator lrk;
     private MethodCompiler c;
     private List<Lr0State> lr0StateList;
     private List<LaState> laStateList;
     private Type parseReturnType;
     private Deque<SubCompiler> compileQueue = new ArrayDeque<>();
     private Set<String> compiledSet = new HashSet<>();
     private List<String> contextList;
     private String eof;
     private boolean syntaxOnly;
     private String[] whiteSpace;
     private Set<GTerminal> whiteSpaceSet = new NumSet<>();
 
     private String start;
     private boolean lineLocatorSupported;
     private boolean offsetLocatorSupported;
 
     public ParserMethodCompiler(ParserCompiler pc)
     {
         this.parserCompiler = pc;
     }
 
     public ParserMethodCompiler(String start, String eof, boolean syntaxOnly, String... whiteSpace)
     {
         this.start = start;
         this.eof = eof;
         this.syntaxOnly = syntaxOnly;
         this.whiteSpace = whiteSpace;
     }
 
     public void setParserCompiler(ParserCompiler parserCompiler)
     {
         this.parserCompiler = parserCompiler;
     }
 
     public void setStart(String start)
     {
         this.start = start;
     }
 
     String addCompilerRequest(SubCompiler comp)
     {
         if (!compiledSet.contains(comp.getLabel()))
         {
             compileQueue.add(comp);
             compiledSet.add(comp.getLabel());
         }
         return comp.getLabel();
     }
 
     @Override
     public void implement(MethodCompiler mc, Member m) throws IOException
     {
         try
         {
             doImplement(mc, m);
         }
 
         catch (NoSuchMethodException | NoSuchFieldException ex)
         {
             throw new IOException(ex);
         }
     }
     private void doImplement(MethodCompiler mc, Member m) throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         c = mc;
         g = parserCompiler.getGrammar();
         parseReturnType = Generics.getReturnType(m);
         try
         {
             lrk = g.getParserGenerator(start, eof, syntaxOnly, whiteSpace);
             File srcDir = parserCompiler.getSrcDir();
             if (srcDir != null)
             {
                 Type thisClass = parserCompiler.getSubClass().getThisClass();
                 Type superClass = parserCompiler.getSubClass().getSuperClass();
                 String simpleName = Generics.getSimpleName(superClass);
                 try (HtmlPrinter printer = new HtmlPrinter(srcDir, thisClass, simpleName+"-"+start+".html"))
                 {
                     lrk.printAll(printer);
                 }
             }
         }
         catch (GrammarException ex)
         {
             g.print(System.err);
             throw ex;
         }
         for (GTerminal terminal : lrk.getTerminals())
         {
             if (terminal.isWhiteSpace())
             {
                 whiteSpaceSet.add(terminal);
             }
             checkLocator(terminal.getReducerType());
         }
         for (Nonterminal nonterminal : lrk.getNonterminals())
         {
             checkLocator(nonterminal.getReducerType());
         }
         if (!whiteSpaceSet.isEmpty())
         {
             for (State state : lrk.getStateList())
             {
                 state.getInputSet().addAll(whiteSpaceSet);
             }
         }
         
         for (int ii=0;ii<contextList.size();ii++)
         {
             c.nameArgument(contextList.get(ii), ii+2);
         }
         init();
         c.fixAddress("reset");
         reset();
 
 
         lr0StateList = lrk.getLr0StateList();
         laStateList = lrk.getLaStateList();
         // ----------- start --------------
         Block mainBlock = c.startBlock();
         c.fixAddress("start");
 
         c.tload(TOKEN);
         c.ifge("afterShift");
 
         c.jsr("shiftSubroutine");
         c.fixAddress("afterShift");
         c.jsr("updateValueStack");
 
         compileStates();
 
         c.endBlock(mainBlock);
         c.addExceptionHandler(mainBlock, "ioExceptionHandler", IOException.class);
         // after this point program control doesn't flow free. It is allowed to compile
         // independent subroutines after this
         // ----------- syntaxError --------------
         c.fixAddress("assert");
         c.fixAddress("syntaxError");
         if (parserCompiler.getRecoverMethod() == null)
         {
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("throwSyntaxErrorException"));
         }
         else
         {
             c.tload(THIS);
             loadContextParameters(parserCompiler.getRecoverMethod(), 0);
             c.invokevirtual(parserCompiler.getRecoverMethod());
         }
         c.goto_n("reset");
         c.fixAddress("ioExceptionHandler");
         c.tstore(EXCEPTION);
         c.tload(INPUTREADER);
         c.tload(EXCEPTION);
        c.invokevirtual(InputReader.class.getMethod("throwSyntaxErrorException", IOException.class));
         c.goto_n("reset");
 
         // LA Start
         if (lrk.isLrk())
         {
             compileLaStates();
             compileUnread();
             compileLaReadInput();
         }
 
         //compileShift();
         compileShift();
         compileUpdateValueStack();
         compileProcessInput();
         compileSetCurrent();
 
         while (!compileQueue.isEmpty())
         {
             SubCompiler comp = compileQueue.pollFirst();
             comp.compile();
         }
 
         c.end();
 
     }
     private void init() throws IOException, NoSuchMethodException
     {
         c.nameArgument(INPUTREADER, 1);
         // curTok
         c.addVariable(SP, int.class);
         c.addVariable(TOKEN, int.class);
         c.addVariable(CURTOK, int.class);
         c.addVariable(CURTYPE, int.class);
 
         int stackSize = Math.min(g.getMaxStack(), lrk.getStackSize()+lrk.getLrkLevel());
         assert stackSize > 0;
         c.addNewArray(STATESTACK, int[].class, stackSize);
         c.addNewArray(TYPESTACK, int[].class, stackSize);
         // value stack
         c.addNewArray(VALUESTACK, Object[].class, ObjectType.values().length);
 
         for (ObjectType ot : lrk.getUsedTypes())
         {
             // value stack
             c.tload(VALUESTACK);  // array
             c.iconst(ot.ordinal());   // index
             c.newarray(ot.getRelatedClass(), stackSize);
             c.aastore();
             // curValue
             c.addVariable(CUR+ot.name(), ot.getObjectClass());
             c.assignDefault(CUR+ot.name());
         }
         // LA init
         if (lrk.isLrk())
         {
             c.addVariable(LASTATE, int.class);
             c.addVariable(LATOKEN, int.class);
             c.addVariable(LALENGTH, int.class);
         }
         // locator stacks
         if (lineLocatorSupported || offsetLocatorSupported)
         {
             c.addNewArray(SOURCESTACK, String[].class, stackSize);
         }
         if (lineLocatorSupported)
         {
             c.addNewArray(LINESTACK, int[].class, stackSize);
             c.addNewArray(COLUMNSTACK, int[].class, stackSize);
         }
         if (offsetLocatorSupported)
         {
             c.addNewArray(OFFSETSTACK, int[].class, stackSize);
         }
        c.addVariable(EXCEPTION, Exception.class);
     }
     private void reset() throws IOException, NoSuchMethodException
     {
         c.iconst(-1);
         c.tstore(TOKEN);
         c.iconst(-1);
         c.tstore(CURTOK);
         c.iconst(ObjectType.VOID.ordinal());
         c.tstore(CURTYPE);
         if (lrk.isLrk())
         {
             c.iconst(0);
             c.tstore(LASTATE);
             c.iconst(-1);
             c.tstore(LATOKEN);
             c.iconst(0);
             c.tstore(LALENGTH);
         }
         c.iconst(-1);
         c.tstore(SP);
 
         push(1);
     }
     private void compileShift() throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         String bend = c.createBranch();
         c.startSubroutine("shiftSubroutine");
         c.fixAddress("shiftStart");
         LookupList inputAddresses = new LookupList();
         Set<Integer> targetSet = new LinkedHashSet<>();
         for (State state : lr0StateList)
         {
             Set<GTerminal> inputSet = state.getInputSet();
             assert !inputSet.isEmpty();
             int inputNumber = parserCompiler.getInputNumber(inputSet, state);
             String target = INPUT+inputNumber;
             inputAddresses.addLookup(state.getNumber(), target);
             targetSet.add(inputNumber);
         }
         trace(Trace.STATE, -1);
         c.tload(STATESTACK);
         c.tload(SP);
         c.iaload();
         c.optimizedSwitch(inputAddresses);
         for (Integer target : targetSet)
         {
         // ----------- input999 --------------
             c.fixAddress(INPUT+target);
             setLocation();
             c.tload(THIS);
             c.tload(INPUTREADER);
             MethodWrapper mw = new MethodWrapper(
                     0,
                     parserCompiler.getThisClass(),
                     INPUT+target,
                     int.class,
                     InputReader.class);
             c.invokespecial(mw);
             c.tstore(TOKEN);
             trace(Trace.INPUT, target);
             c.tload(TOKEN);
             c.ifge(bend);
             if (parserCompiler.getRecoverMethod() == null)
             {
                 if (parserCompiler.implementsParserInfo())
                 {
                     c.tload(INPUTREADER);
                     c.ldc(parserCompiler.getExpected(target));
                     c.tload(THIS);
                     c.tload(TOKEN);
                     MethodWrapper mw2 = new MethodWrapper(
                             0,
                             parserCompiler.getThisClass(),
                             GETTOKEN,
                             String.class,
                             int.class);
                     c.invokespecial(mw2);
                     c.invokevirtual(InputReader.class.getMethod("throwSyntaxErrorException", String.class, String.class));
                 }
                 else
                 {
                     c.goto_n("syntaxError");
                 }
             }
             else
             {
                 c.tload(THIS);
                 loadContextParameters(parserCompiler.getRecoverMethod(), 0);
                 c.invokevirtual(parserCompiler.getRecoverMethod());
                 c.goto_n("reset");
             }
         }
         c.fixAddress(bend);
 
         c.tload(TOKEN);
         c.tstore(CURTOK);
 
         bend = c.createBranch();
         LookupList terminalNumbers = new LookupList();
         terminalNumbers.addLookup(0, bend); // Eof
         for (GTerminal t : lrk.getTerminals())
         {
             if (t.getExpression() != null)
             {
                 terminalNumbers.addLookup(t.getNumber(), "$term-"+t.toString());
             }
         }
         c.tload(CURTOK);
         c.optimizedSwitch(terminalNumbers);
         for (GTerminal t : lrk.getTerminals())
         {
         // ----------- terminal --------------
             if (t.getExpression() != null)
             {
                 c.fixAddress("$term-"+t.toString());
                 Member reducer = t.getReducer();
                 if (reducer != null)
                 {
                     parserCompiler.addReducer(reducer);
                     // if terminal reducer was abstract, there is no need to call
                     // it. These methods have no effect on stack
                     if (!parserCompiler.implementedAbstract(reducer))
                     {
                         c.tload(THIS);
                     }
                     // anyway we have to do the type conversion on stack
                     Type[] params = Generics.getParameterTypes(reducer);
                     Type returnType = Generics.getReturnType(reducer);
                     if (params.length > 0)
                     {
                         if (isAnnotationPresent(reducer, 0, ParserContext.class))
                         {
                             // Terminal value is omitted but @ParserContext is present
                             loadContextParameters(reducer, 0);
                         }
                         else
                         {
                             c.tload(INPUTREADER);
                             // if param[0] is not InputReader -> convert
                             if (!(Generics.isAssignableFrom(InputReader.class, params[0])))
                             {
                                 c.invokevirtual(InputReader.getParseMethod(params[0], t));
                             }
                             loadContextParameters(reducer, 1);
                         }
                     }
                     if (!parserCompiler.implementedAbstract(reducer))
                     {
                         c.invokevirtual(reducer);
                     }
                     // if type was other than void we are storing the result in
                     // local variable CURxxx
                     ObjectType ot = ObjectType.valueOf(returnType);
                     if (!void.class.equals(returnType))
                     {
                         callSetLocation(returnType);
                         c.tstore(CUR+ot.name());
                     }
                     setCurrentType(ot.ordinal());
                 }
                 c.goto_n(bend);
             }
         }
         c.fixAddress(bend);
         c.tload(INPUTREADER);
         c.invokevirtual(InputReader.class.getMethod("clear"));
         if (!whiteSpaceSet.isEmpty())
         {
             LookupList wspList = new LookupList();
             for (GTerminal wsp : whiteSpaceSet)
             {
                 Member reducer = wsp.getReducer();
                 if (reducer != null && !void.class.equals(Generics.getReturnType(reducer)))
                 {
                     wspList.addLookup(wsp.getNumber(), wsp+"-shiftInsert");
                 }
                 else
                 {
                     wspList.addLookup(wsp.getNumber(), "shiftStart");
                 }
             }
             c.tload(TOKEN);
             c.optimizedSwitch("wspContinue", wspList);
             for (GTerminal wsp : whiteSpaceSet)
             {
                 Member reducer = wsp.getReducer();
                 
                 if (reducer != null)
                 {
                     Type returnType = Generics.getReturnType(reducer);
                     if (!void.class.equals(returnType))
                     {
                         c.fixAddress(wsp+"-shiftInsert");
                         ObjectType ot = ObjectType.valueOf(returnType);
                         c.tload(INPUTREADER);
                         c.tload(CUR+ot.name());
                         c.checkcast(returnType);
                         c.invokevirtual(InputReader.class, "insert", returnType);
                         c.goto_n("shiftStart");
                     }
                 }
             }
             c.fixAddress("wspContinue");
         }
         c.endSubroutine();
     }
 
     private boolean isAnnotationPresent(Member reducer, int paramIndex, Class<?> annotation)
     {
         Annotation[][] parameterAnnotations = Generics.getParameterAnnotations(reducer);
         for (Annotation a : parameterAnnotations[paramIndex])
         {
             if (a.annotationType().equals(annotation))
             {
                 return true;
             }
         }
         return false;
     }
     private void loadContextParameters(Member reducer, int start) throws IOException
     {
         Annotation[][] parameterAnnotations = Generics.getParameterAnnotations(reducer);
         Type[] parameters = Generics.getParameterTypes(reducer);
         for (int ii=start;ii < parameters.length;ii++)
         {
             boolean parserContextFound = false;
             for (Annotation a : parameterAnnotations[ii])
             {
                 if (a.annotationType().equals(ParserContext.class))
                 {
                     ParserContext parserContext = (ParserContext) a;
                     c.tload(parserContext.value());
                     parserContextFound = true;
                     break;
                 }
             }
             if (!parserContextFound)
             {
                 if (InputReader.class.equals(parameters[ii]))
                 {
                     c.tload(INPUTREADER);
                 }
                 else
                 {
                     throw new IllegalArgumentException("reducer "+reducer+" has extra parameters which are not @ParserContext");
                 }
             }
         }
     }
     private void compileUpdateValueStack() throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         c.startSubroutine("updateValueStack");
         LookupList ll = new LookupList();
         ll.addLookup(ObjectType.VOID.ordinal(), "setCurrent-Void");
         for (ObjectType ot : lrk.getUsedTypes())
         {
             // value stack
             ll.addLookup(ot.ordinal(), ot+"-cur");
         }
         getCurrentType();
         c.optimizedSwitch(ll);
         for (ObjectType ot : lrk.getUsedTypes())
         {
             // value stack
             c.fixAddress(ot+"-cur");
 
             c.tload(TYPESTACK);
             c.tload(SP);
             c.tload(CURTYPE);
             c.iastore();
 
             c.tload(VALUESTACK);  // valueStack
             getCurrentType();       // valueStack curType
             c.aaload();             // stackXXX
             c.checkcast(ot.getRelatedClass());
             c.tload(SP);          // stackXXX spXXX 
             c.tload(CUR+ot.name());   // stackXXX spXXX curXXX
             c.tastore(ot.getObjectClass());
             c.goto_n("setCurrent-Exit");
         }
         c.fixAddress("setCurrent-Void");
 
         c.tload(TYPESTACK);
         c.tload(SP);
         c.iconst(ObjectType.VOID.ordinal());
         c.iastore();
 
         c.fixAddress("setCurrent-Exit");
         trace(Trace.PUSHVALUE, -1);
 
         c.endSubroutine();
     }
 
     private void compileLaReadInput() throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         String bend = c.createBranch();
         c.startSubroutine("readLaInputSubroutine");
         c.fixAddress("laReadStart");
         LookupList inputAddresses = new LookupList();
         Set<Integer> targetSet = new LinkedHashSet<>();
         for (State state : laStateList)
         {
             Set<GTerminal> inputList = state.getInputSet();
             assert !inputList.isEmpty();
             int inputNumber = parserCompiler.getInputNumber(inputList, state);
             String target = LAINPUT+inputNumber;
             inputAddresses.addLookup(state.getNumber(), target);
             targetSet.add(inputNumber);
         }
         c.tload(LASTATE);
         c.optimizedSwitch(inputAddresses);
         for (Integer target : targetSet)
         {
         // ----------- input999 --------------
             c.fixAddress(LAINPUT+target);
             setLocation();
             c.tload(THIS);
             c.tload(INPUTREADER);
             MethodWrapper mw = new MethodWrapper(
                     0,
                     parserCompiler.getThisClass(),
                     INPUT+target,
                     int.class,
                     InputReader.class);
             c.invokevirtual(mw);
             c.tstore(LATOKEN);
             // La token buffer
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("getLength"));
             c.tload(LALENGTH);
             c.iadd();
             c.tstore(LALENGTH);
             trace(Trace.LAINPUT, target);
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("clear"));
 
             c.tload(LATOKEN);
             c.ifge(bend);
             if (parserCompiler.getRecoverMethod() == null)
             {
                 if (parserCompiler.implementsParserInfo())
                 {
                     c.tload(INPUTREADER);
                     c.ldc(parserCompiler.getExpected(target));
                     c.tload(THIS);
                     c.tload(LATOKEN);
                     MethodWrapper mw2 = new MethodWrapper(
                             0,
                             parserCompiler.getThisClass(),
                             GETTOKEN,
                             String.class,
                             int.class);
                     c.invokespecial(mw2);
                     c.invokevirtual(InputReader.class.getMethod("throwSyntaxErrorException", String.class, String.class));
                 }
                 else
                 {
                     c.goto_n("syntaxError");
                 }
             }
             else
             {
                 c.tload(THIS);
                 loadContextParameters(parserCompiler.getRecoverMethod(), 0);
                 c.invokevirtual(parserCompiler.getRecoverMethod());
                 c.goto_n("reset");
             }
             // till here
         }
         c.fixAddress(bend);
         if (!whiteSpaceSet.isEmpty())
         {
             LookupList wspList = new LookupList();
             for (GTerminal wsp : whiteSpaceSet)
             {
                 Member reducer = wsp.getReducer();
                 if (reducer != null && !void.class.equals(Generics.getReturnType(reducer)))
                 {
                     wspList.addLookup(wsp.getNumber(), wsp+"-laReadInsert");
                 }
                 else
                 {
                     wspList.addLookup(wsp.getNumber(), "laReadStart");
                 }
             }
             c.tload(LATOKEN);
             c.optimizedSwitch("laWspContinue", wspList);
             for (GTerminal wsp : whiteSpaceSet)
             {
                 Member reducer = wsp.getReducer();
                 if (reducer != null)
                 {
                     Type returnType = Generics.getReturnType(reducer);
                     if (!void.class.equals(returnType))
                     {
                         c.fixAddress(wsp+"-laReadInsert");
                         ObjectType ot = ObjectType.valueOf(returnType);
                         c.tload(INPUTREADER);
                         c.tload(CUR+ot.name());
                         c.checkcast(returnType);
                         c.invokevirtual(InputReader.class, "insert", returnType);
                         c.goto_n("laReadStart");
                     }
                 }
             }
             c.fixAddress("laWspContinue");
         }
         c.endSubroutine();
     }
 
     private void compileShiftAction(Shift shift) throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         GTerminal symbol = shift.getSymbol();
         Action action = shift.getAction();
         if (action instanceof Lr0State)
         {
             Lr0State lr0State = (Lr0State) action;
             push(lr0State.getNumber());
             c.iconst(-1);
             c.tstore(TOKEN);
             trace(Trace.SHIFT, lr0State.getNumber());
             c.goto_n("start");    // shift to state
         }
         else
         {
             if (action instanceof GRule)
             {
                 // Shift/Reduce
                 GRule rule = (GRule) action;
                 trace(Trace.SHRD, rule.getNumber());
                 trace(Trace.BEFOREREDUCE, rule.getOriginalNumber());
                 c.tinc(SP, 1);
                 String t = addCompilerRequest(new ReductSubCompiler(rule));
                 c.jsr(t);   // shift/reduce
                 trace(Trace.AFTERREDUCE, rule.getOriginalNumber());
                 c.iconst(-1);
                 c.tstore(TOKEN);
                 Nonterminal nt = rule.getLeft();
                 if (!nt.isStart())
                 {
                     String t2 = addCompilerRequest(new GotoCompiler(nt));
                     c.goto_n(t2);
                 }
                 else
                 {
                     assert false;   //
                 }
             }
             else
             {
                 if (action instanceof LaState)
                 {
                     LaState state = (LaState) action;
                     trace(Trace.LASHIFT, state.getNumber());
                     c.iconst(state.getNumber());
                     c.tstore(LASTATE);
                     c.jsr("readLaInputSubroutine");
                     c.goto_n("laStateStart");    // shift to state
                 }
                 else
                 {
                     assert false;
                 }
             }
         }
     }
 
     private void compileReduceAction(Reduce reduce) throws IOException
     {
         GRule rule = reduce.getRule();
         String target = addCompilerRequest(new ReductCompiler(rule));
         c.goto_n(target);
     }
 
     private void compileLaShiftAction(LaShift laShift) throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         Act act = laShift.getAct();
         if (act instanceof LaState)
         {
             // La Shift
             LaState state = (LaState) act;
             trace(Trace.LASHIFT, state.getNumber());
             c.iconst(state.getNumber());
             c.tstore(LASTATE);
             c.jsr("readLaInputSubroutine");
             c.goto_n("laStateStart");    // shift to state
         }
         else
         {
             if (act instanceof ShiftReduceAct)
             {
                 // La Sh/Rd
                 ShiftReduceAct ract = (ShiftReduceAct) act;
                 GRule rule = ract;
                 trace(Trace.LASHRD, rule.getNumber());
                 c.jsr("updateValueStack");
                 trace(Trace.BEFOREREDUCE, rule.getOriginalNumber());
                 c.tinc(SP, 1);
                 String target = addCompilerRequest(new ReductSubCompiler(rule));
                 c.jsr(target);   // shift/reduce
                 //c.tinc(SP, 1);
                 trace(Trace.AFTERREDUCE, rule.getOriginalNumber());
                 c.jsr("unreadSubroutine");
                 c.tload(INPUTREADER);
                 c.invokevirtual(InputReader.class.getMethod("clear"));
                 c.iconst(-1);
                 c.tstore(TOKEN);
                 Nonterminal nt = rule.getLeft();
                 if (!nt.isStart())
                 {
                     String t2 = addCompilerRequest(new GotoCompiler(nt));
                     c.goto_n(t2);
                 }
                 else
                 {
                     assert false;   //
                 }
             }
             else
             {
                 if (act instanceof Lr0State)
                 {
                     // Shift
                     Lr0State lr0State = (Lr0State) act;
                     trace(Trace.GOTOLA2LR, lr0State.getNumber());
                     c.jsr("unreadSubroutine");
                     c.tload(INPUTREADER);
                     c.invokevirtual(InputReader.class.getMethod("clear"));
                     push(lr0State.getNumber());
                     c.iconst(-1);
                     c.tstore(TOKEN);
                     c.goto_n("start");    // shift to state
                 }
                 else
                 {
                     assert false;
                 }
             }
         }
     }
 
     private void compileLaReduceAction(LaReduce laReduce) throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         Act act = laReduce.getAct();
         if (act instanceof ReduceAct)
         {
             GRule rule = (GRule) act;
             exitLa();
             String target = addCompilerRequest(new ReductCompiler(rule));
             c.goto_n(target);   // reduce
         }
         else
         {
             throw new UnsupportedOperationException("not supported yet");
         }
     }
 
     private void compileStates() throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         trace(Trace.STATE, -1);
         c.tload(STATESTACK);
         c.tload(SP);
         c.iaload();
         List<String> stateTargets = new ArrayList<>();
         for (State state : lr0StateList)
         {
             stateTargets.add(state.toString());
         }
         c.tableswitch(1, lr0StateList.size(), stateTargets);
 
         for (Lr0State state : lr0StateList)
         {
             c.fixAddress(state.toString());
             Set<GTerminal> inputList = state.getInputSet();
             assert !inputList.isEmpty();
             LookupList lookupList = new LookupList();
             StringBuilder expected = new StringBuilder();
 
             //assert state.getDefaultReduce() == null;
 
             for (Shift shift : state.getShiftList())
             {
                 String target = addCompilerRequest(new TerminalActionCompiler(shift));
                 lookupList.addLookup(shift.getSymbol().getNumber(), target);
                 //terminalActionSet.add(shift);
                 if (expected.length() == 0)
                 {
                     expected.append("\n  "+shift.getSymbol());
                 }
                 else
                 {
                     expected.append("\n| "+shift.getSymbol());
                 }
             }
             for (Reduce reduce : state.getReduceList())
             {
                 String target = addCompilerRequest(new TerminalActionCompiler(reduce));
                 lookupList.addLookup(reduce.getSymbol().getNumber(), target);
                 //terminalActionSet.add(reduce);
                 if (expected.length() == 0)
                 {
                     expected.append("\n  "+reduce.getSymbol());
                 }
                 else
                 {
                     expected.append("\n| "+reduce.getSymbol());
                 }
             }
             c.tload(CURTOK);
             c.optimizedSwitch(state+"syntaxError", lookupList);
             c.fixAddress(state+"syntaxError");
             if (parserCompiler.implementsParserInfo())
             {
                 c.tload(INPUTREADER);
                 c.ldc(expected.toString());
                 c.tload(THIS);
                 c.tload(TOKEN);
                 MethodWrapper mw = new MethodWrapper(
                         0,
                         parserCompiler.getThisClass(),
                         GETTOKEN,
                         String.class,
                         int.class);
                 c.invokespecial(mw);
                 c.invokevirtual(InputReader.class.getMethod("throwSyntaxErrorException", String.class, String.class));
             }
             else
             {
                 c.goto_n("syntaxError");
             }
         }
     }
 
     private void compileLaStates() throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         c.fixAddress("laStateStart");
         c.tload(LASTATE);
         List<String> stateTargets = new ArrayList<>();
         for (State state : laStateList)
         {
             stateTargets.add(state.toString());
         }
         int first = laStateList.get(0).getNumber();
         c.tableswitch(first, first+laStateList.size()-1, stateTargets);
 
         for (LaState state : laStateList)
         {
             c.fixAddress(state.toString());
             Set<GTerminal> inputSet = state.getInputSet();
             if (!inputSet.isEmpty())
             {
                 c.tload(LATOKEN);
                 LookupList lookupList = new LookupList();
 
                 assert state.getDefaultRule() == null;
                 for (LaShift shift : state.getShiftList())
                 {
                     String target = addCompilerRequest(new TerminalActionCompiler(shift));
                     lookupList.addLookup(shift.getSymbol().getNumber(), target);
                     //terminalActionSet.add(shift);
                 }
                 for (LaReduce reduce : state.getReduceList())
                 {
                     String target = addCompilerRequest(new TerminalActionCompiler(reduce));
                     lookupList.addLookup(reduce.getSymbol().getNumber(), target);
                     //terminalActionSet.add(reduce);
                 }
                 c.optimizedSwitch("syntaxError", lookupList);
             }
             else
             {
                 exitLa();
                 //c.tinc(SP, -1);
                 c.goto_n("start");
             }
         }
     }
 
     private void setCurrentType(int type) throws IOException
     {
         c.iconst(type);
         c.tstore(CURTYPE);
     }
 
     private void getCurrentType() throws IOException
     {
         c.tload(CURTYPE);
     }
 
     private void compileSetCurrent() throws IOException, NoSuchMethodException, NoSuchFieldException
     {
     }
 
 
     private void trace(Trace action, int ctx) throws NoSuchFieldException, IOException, NoSuchMethodException
     {
         if (parserCompiler.getTraceMethod() != null)
         {
             Type[] parameterTypes = parserCompiler.getTraceMethod().getParameterTypes();
             if (
                     parameterTypes.length >= 2 &&
                     int.class.equals(parameterTypes[0]) &&
                     int.class.equals(parameterTypes[1]) &&
                     void.class.equals(parserCompiler.getTraceMethod().getReturnType())
                     )
             {
                 c.tload(THIS);
                 c.iconst(action.ordinal());
                 c.iconst(ctx);
                 loadContextParameters(parserCompiler.getTraceMethod(), 2);
                 c.invokevirtual(parserCompiler.getTraceMethod());
             }
             else
             {
                 throw new IllegalArgumentException(parserCompiler.getTraceMethod()+" signature is not xxx(int trace, int ctx, ...");
             }
         }
     }
 
     public LALRKParserGenerator getLrk()
     {
         return lrk;
     }
 
     private void push(int state) throws IOException
     {
         c.tinc(SP, 1);
         c.tload(STATESTACK);
         c.tload(SP);
         c.iconst(state);
         c.iastore();
     }
 
     private void exitLa() throws IOException, NoSuchMethodException, NoSuchFieldException
     {
         c.iconst(0);
         c.tstore(LASTATE);
 
         trace(Trace.EXITLA, -1);
         c.jsr("unreadSubroutine");
 
         c.tload(INPUTREADER);
         c.invokevirtual(InputReader.class.getMethod("clear"));
     }
 
     private void compileReset() throws NoSuchMethodException, IOException
     {
         c.startSubroutine("resetSubroutine");
         c.tload(INPUTREADER);
         c.invokevirtual(InputReader.class.getMethod("clear"));
         c.endSubroutine();
     }
 
     private void compileUnread() throws NoSuchMethodException, IOException
     {
         c.startSubroutine("unreadSubroutine");
         c.tload(INPUTREADER);
         c.tload(LALENGTH);
         c.invokevirtual(InputReader.class.getMethod("unreadLa", int.class));
         c.iconst(0);
         c.tstore(LALENGTH);
         c.endSubroutine();
     }
 
     private void compileProcessInput() throws IOException, NoSuchMethodException
     {
     }
 
     void setContextList(List<String> contextList)
     {
         this.contextList = contextList;
     }
 
     private void checkLocator(Type rt)
     {
         if (rt instanceof Class)
         {
             Class<?> cls = (Class<?>) rt;
             if (ParserLineLocator.class.isAssignableFrom(cls))
             {
                 lineLocatorSupported = true;
             }
             if (ParserOffsetLocator.class.isAssignableFrom(cls))
             {
                 offsetLocatorSupported = true;
             }
         }
     }
     private void setLocation() throws IOException, NoSuchMethodException
     {
         if (lineLocatorSupported)
         {
             c.tload(SOURCESTACK);
             c.tload(SP);
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("getSource"));
             c.aastore();
             
             c.tload(LINESTACK);
             c.tload(SP);
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("getLineNumber"));
             c.iastore();
             
             c.tload(COLUMNSTACK);
             c.tload(SP);
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("getColumnNumber"));
             c.iastore();
         }
         if (offsetLocatorSupported)
         {
             c.tload(SOURCESTACK);
             c.tload(SP);
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("getSource"));
             c.aastore();
             
             c.tload(OFFSETSTACK);
             c.tload(SP);
             c.tload(INPUTREADER);
             c.invokevirtual(InputReader.class.getMethod("getStart"));
             c.iastore();
             
         }
     }
 
     private void callSetLocation(Type returnType) throws IOException, NoSuchMethodException
     {
         if (lineLocatorSupported)
         {
             if (returnType instanceof Class)
             {
                 Class<?> cls = (Class<?>) returnType;
                 if (ParserLineLocator.class.isAssignableFrom(cls))
                 {
                     c.dup();
                     String branch = c.createBranch();
                     c.ifnull(branch);
                     c.dup();
                     c.tload(SOURCESTACK);
                     c.tload(SP);
                     c.aaload();
                     c.tload(LINESTACK);
                     c.tload(SP);
                     c.iaload();
                     c.tload(COLUMNSTACK);
                     c.tload(SP);
                     c.iaload();
                     c.tload(INPUTREADER);
                     c.invokevirtual(InputReader.class.getMethod("getLineNumber"));
                     c.tload(INPUTREADER);
                     c.invokevirtual(InputReader.class.getMethod("getColumnNumber"));
                     c.invokevirtual(ParserLineLocator.class.getDeclaredMethod("setLocation", String.class, int.class, int.class, int.class, int.class));
                     c.fixAddress(branch);
                 }
             }
         }
         if (offsetLocatorSupported)
         {
             if (returnType instanceof Class)
             {
                 Class<?> cls = (Class<?>) returnType;
                 if (ParserOffsetLocator.class.isAssignableFrom(cls))
                 {
                     c.dup();
                     String branch = c.createBranch();
                     c.ifnull(branch);
                     c.dup();
                     c.tload(SOURCESTACK);
                     c.tload(SP);
                     c.aaload();
                     c.tload(OFFSETSTACK);
                     c.tload(SP);
                     c.iaload();
                     c.tload(INPUTREADER);
                     c.invokevirtual(InputReader.class.getMethod("getStart"));
                     c.invokevirtual(ParserOffsetLocator.class.getDeclaredMethod("setLocation", String.class, int.class, int.class));
                     c.fixAddress(branch);
                 }
             }
         }
     }
 
     interface SubCompiler
     {
         void compile() throws ParserCompilerException;
         String getLabel();
     }
     private class TerminalActionCompiler implements SubCompiler
     {
         private TerminalAction action;
 
         public TerminalActionCompiler(TerminalAction action)
         {
             this.action = action;
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj == null)
             {
                 return false;
             }
             if (getClass() != obj.getClass())
             {
                 return false;
             }
             final TerminalActionCompiler other = (TerminalActionCompiler) obj;
             if (this.action != other.action && (this.action == null || !this.action.equals(other.action)))
             {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode()
         {
             int hash = 3;
             hash = 43 * hash + (this.action != null ? this.action.hashCode() : 0);
             return hash;
         }
 
         public void compile() throws ParserCompilerException
         {
             try
             {
                 c.fixAddress(getLabel());
                 if (action instanceof Shift)
                 {
                     compileShiftAction((Shift)action);
                 }
                 else
                 {
                     if (action instanceof Reduce)
                     {
                         compileReduceAction((Reduce)action);
                     }
                     else
                     {
                         if (action instanceof LaShift)
                         {
                             compileLaShiftAction((LaShift)action);
                         }
                         else
                         {
                             if (action instanceof LaReduce)
                             {
                                 compileLaReduceAction((LaReduce)action);
                             }
                             else
                             {
                                 assert false;
                             }
                         }
                     }
                 }
             }
             catch (Exception ex)
             {
                 throw new ParserCompilerException(ex);
             }
         }
 
         public String getLabel()
         {
             return action+"-action";
         }
     }
 
     private class GotoCompiler implements SubCompiler
     {
         private Nonterminal nt;
 
         public GotoCompiler(Nonterminal nt)
         {
             this.nt = nt;
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj == null)
             {
                 return false;
             }
             if (getClass() != obj.getClass())
             {
                 return false;
             }
             final GotoCompiler other = (GotoCompiler) obj;
             if (this.nt != other.nt && (this.nt == null || !this.nt.equals(other.nt)))
             {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode()
         {
             int hash = 3;
             hash = 97 * hash + (this.nt != null ? this.nt.hashCode() : 0);
             return hash;
         }
 
         public void compile() throws ParserCompilerException
         {
             try
             {
                 if (!nt.isStart())
                 {
                     String label = getLabel();
                     LookupList list = new LookupList();
                     c.fixAddress(label);
                     c.tload(STATESTACK);
                     c.tload(SP);
                     c.iaload();
                     for (Lr0State lr0State : lr0StateList)
                     {
                         for (Goto go : lr0State.getGotoList())
                         {
                             if (nt.equals(go.getSymbol()))
                             {
                                 String target = addCompilerRequest(new GotoActionCompiler(go.getAction()));
                                 list.addLookup(lr0State.getNumber(), target);
                                 //actionSet.add(go.getAction());
                             }
                         }
                     }
                     if (list.isEmpty())
                     {
                         lrk.printAll(new SystemErrPrinter());
                         throw new IllegalArgumentException(nt+" has empty goto table. Meaning propably that it is not referenced outside it's own declaration");
                     }
                     c.optimizedSwitch(list);
                 }
             }
             catch (Exception ex)
             {
                 throw new ParserCompilerException(ex);
             }
         }
 
         public String getLabel()
         {
             return nt+"-goto";
         }
     }
 
     private class GotoActionCompiler implements SubCompiler
     {
         private Action action;
 
         public GotoActionCompiler(Action action)
         {
             this.action = action;
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj == null)
             {
                 return false;
             }
             if (getClass() != obj.getClass())
             {
                 return false;
             }
             final GotoActionCompiler other = (GotoActionCompiler) obj;
             if (this.action != other.action && (this.action == null || !this.action.equals(other.action)))
             {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode()
         {
             int hash = 5;
             hash = 53 * hash + (this.action != null ? this.action.hashCode() : 0);
             return hash;
         }
 
         public void compile() throws ParserCompilerException
         {
             try
             {
                 c.fixAddress(getLabel());
                 if (action instanceof Lr0State)
                 {
                     Lr0State lr0State = (Lr0State) action;
                     trace(Trace.GOTO, lr0State.getNumber());
                     push(lr0State.getNumber());
                     c.goto_n("start");
                 }
                 else
                 {
                     if (action instanceof LaState)
                     {
                         throw new UnsupportedOperationException("LaState-goto-action");
                     }
                     else
                     {
                         GRule rule = (GRule) action;
                         trace(Trace.GTRD, rule.getNumber());
                         trace(Trace.BEFOREREDUCE, rule.getOriginalNumber());
                         c.tinc(SP, 1);
                         String target = addCompilerRequest(new ReductSubCompiler(rule));
                         c.jsr(target);   // shift/reduce
                         trace(Trace.AFTERREDUCE, rule.getOriginalNumber());
                         Nonterminal nt = rule.getLeft();
                         if (!nt.isStart())
                         {
                             String t = addCompilerRequest(new GotoCompiler(nt));
                             c.goto_n(t);
                         }
                     }
                 }
             }
             catch (Exception ex)
             {
                 throw new ParserCompilerException(ex);
             }
         }
 
         public String getLabel()
         {
             return action+"-goto-action";
         }
     }
 
     private class ReductCompiler implements SubCompiler
     {
         private GRule rule;
 
         public ReductCompiler(GRule rule)
         {
             this.rule = rule;
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj == null)
             {
                 return false;
             }
             if (getClass() != obj.getClass())
             {
                 return false;
             }
             final ReductCompiler other = (ReductCompiler) obj;
             if (this.rule != other.rule && (this.rule == null || !this.rule.equals(other.rule)))
             {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode()
         {
             int hash = 7;
             hash = 47 * hash + (this.rule != null ? this.rule.hashCode() : 0);
             return hash;
         }
 
         @Override
         public String toString()
         {
             return "ReductCompiler{" + "rule=" + rule + '}';
         }
 
         public void compile() throws ParserCompilerException
         {
             try
             {
                 c.fixAddress(getLabel());
                 String target = addCompilerRequest(new ReductSubCompiler(rule));
                 c.jsr(target);   // shift/reduce
                 Nonterminal nt = rule.getLeft();
                 if (!nt.isStart())
                 {
                     String t = addCompilerRequest(new GotoCompiler(nt));
                     c.goto_n(t);
                 }
                 else
                 {
                     c.goto_n("assert");
                 }
             }
             catch (Exception ex)
             {
                 throw new ParserCompilerException(ex);
             }
         }
 
         public String getLabel()
         {
             return rule.toString();
         }
     }
 
     private class ReductSubCompiler implements SubCompiler
     {
         private GRule rule;
 
         public ReductSubCompiler(GRule rule)
         {
             this.rule = rule;
         }
 
         @Override
         public boolean equals(Object obj)
         {
             if (obj == null)
             {
                 return false;
             }
             if (getClass() != obj.getClass())
             {
                 return false;
             }
             final ReductSubCompiler other = (ReductSubCompiler) obj;
             if (this.rule != other.rule && (this.rule == null || !this.rule.equals(other.rule)))
             {
                 return false;
             }
             return true;
         }
 
         @Override
         public String toString()
         {
             return "ReductSubCompiler{" + "rule=" + rule + '}';
         }
 
         @Override
         public int hashCode()
         {
             int hash = 7;
             hash = 47 * hash + (this.rule != null ? this.rule.hashCode() : 0);
             return hash;
         }
 
         public void compile() throws ParserCompilerException
         {
             try
             {
                 c.startSubroutine(getLabel());
                 // state stack
                 int rhs = rule.getRight().size();
                 if (rhs > 0)
                 {
                     c.tinc(SP, -rhs);
                 }
 
                 Member reducer = rule.getReducer();
                 if (reducer != null)
                 {
                     parserCompiler.addReducer(reducer);
                     Type[] parameters = Generics.getParameterTypes(reducer);
                     Type returnType = Generics.getReturnType(reducer);
                     ObjectType rot = ObjectType.valueOf(returnType);
 
                     if (!void.class.equals(returnType))
                     {
                         c.tload(VALUESTACK);                // valueStack
                         c.iconst(rot.ordinal());  // valueStack class
                         c.aaload();                         // stackXXX
                         c.checkcast(rot.getRelatedClass());
                         c.tload(SP);                      // stackXXX sp
                     }
                     if (
                             !parserCompiler.implementedAbstract(reducer) &&
                             !Modifier.isAbstract(Generics.getModifiers(reducer)) &&
                             !Modifier.isStatic(Generics.getModifiers(reducer))
                             )
                     {
                         c.tload(THIS);                            // this
                     }
                     int paramIndex = 0;
                     int symbolIndex = 0;
                     for (Symbol symbol : rule.getRight())
                     {
                         Type rt = symbol.getReducerType();
                         if (!void.class.equals(rt))
                         {
                             Type param = parameters[paramIndex];
                             if (!Generics.isAssignableFrom(param, rt) && !Reducers.isGet(symbol.getReducer()))
                             {
                                 throw new IllegalArgumentException(symbol+" type="+Generics.getName(rt)+" reducer "+reducer.getName()+" expects "+Generics.getName(param));
                             }
                             c.tload(VALUESTACK);              // this valueStack
                             ObjectType pot = ObjectType.valueOf(param);
                             c.iconst(pot.ordinal());  // this valueStack indexXXX
                             c.aaload();                         // this stackXXX
                             c.checkcast(pot.getRelatedClass());
                             c.tload(SP);          // sp
                             c.iconst(symbolIndex);
                             c.iadd();
                             c.taload(param);                    // this paramx
                             if (!Generics.isPrimitive(param))
                             {
                                 c.checkcast(param);
                             }
                             paramIndex++;
                         }
                         symbolIndex++;
                     }
                     loadContextParameters(reducer, paramIndex);
                     if (!parserCompiler.implementedAbstract(reducer))
                     {
                         c.invoke(reducer);               // result
                     }
 
                     if (!void.class.equals(returnType))
                     {
                         callSetLocation(returnType);
                                                             // stackXXX spXXX result
                         c.tastore(returnType);              //
                         c.tload(TYPESTACK);
                         c.tload(SP);
                         c.iconst(rot.ordinal());
                         c.iastore();
                     }
                 }
                 if (rule.isAccepting())
                 {
                     Nonterminal s = (Nonterminal) rule.getRight().get(0);
                     GRule sr = s.getLhsRule().get(0);
                     Member r = sr.getReducer();
                     if (r != null)
                     {
                         Type type = Generics.getReturnType(r);
                         if (!void.class.equals(type))
                         {
                             ObjectType rot = ObjectType.valueOf(type);
                             c.tload(VALUESTACK);              // valueStack
                             c.iconst(rot.ordinal());  // valueStack indexXXX
                             c.aaload();                         // stackXXX
                             c.checkcast(rot.getRelatedClass());
                             c.iconst(0);                        // stackXXX 0
                             c.taload(type);                     // valueXXX
                             if (!Generics.isPrimitive(parseReturnType))
                             {
                                 c.checkcast(parseReturnType);
                             }
                             /*
                             c.tstore(CUR+rot.name());
                             try
                             {
                                 c.convert(CUR + rot.name(), parseReturnType); // refXXX
                             }
                             catch (IllegalConversionException ex)
                             {
                                 throw new IOException("Conversion problem with "+r, ex);
                             }
                              */
                         }
                         else
                         {
                             c.loadDefault(parseReturnType);
                         }
                     }
                     else
                     {
                         c.loadDefault(parseReturnType);
                     }
                     c.treturn();
                     c.resetSubroutine();
                 }
                 else
                 {
                     c.endSubroutine();
                 }
             }
             catch (Exception ex)
             {
                 throw new ParserCompilerException(ex);
             }
         }
 
         public String getLabel()
         {
             return rule.toString()+"subroutine";
         }
     }
 
     private class Element implements Comparable<Element>
     {
         private int order;
         private String left;
         private Annotation annotation;
         private Method reducer;
         private boolean terminal;
 
         public Element(int order, String left, Annotation annotation, Method reducer, boolean terminal)
         {
             this.order = order;
             this.left = left;
             this.annotation = annotation;
             this.reducer = reducer;
             this.terminal = terminal;
         }
 
         public int compareTo(Element o)
         {
             return order - o.order;
         }
 
         public boolean isTerminal()
         {
             return terminal;
         }
 
         private String getLeft()
         {
             if (left.isEmpty())
             {
                 return reducer.getName();
             }
             else
             {
                 return left;
             }
         }
     }
 }
