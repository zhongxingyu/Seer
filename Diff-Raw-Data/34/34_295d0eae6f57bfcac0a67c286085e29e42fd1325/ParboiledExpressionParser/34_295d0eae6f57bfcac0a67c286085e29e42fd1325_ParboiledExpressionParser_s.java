 package engine.expressions.parser.parboiled;
 
 import engine.expressions.parser.ClauseType;
 import engine.expressions.parser.ExpressionParser;
 import engine.expressions.parser.ParsingException;
 import engine.expressions.parser.SyntaxError;
 import engine.expressions.parser.auto_complete.AutocompletionParser;
 import org.parboiled.Rule;
 import org.parboiled.buffers.InputBuffer;
 import org.parboiled.errors.BasicParseError;
 import org.parboiled.errors.InvalidInputError;
 import org.parboiled.errors.ParseError;
 import org.parboiled.parserunners.RecoveringParseRunner;
 import org.parboiled.support.*;
 
 import java.util.*;
 
 import static engine.expressions.parser.parboiled.HumanReadable.hr;
 
 /**
  * User: Oleksiy Pylypenko
  * Date: 2/9/13
  * Time: 11:24 AM
  */
 public class ParboiledExpressionParser implements ExpressionParser {
     private Map<String, Double> knownConstants = new HashMap<String, Double>();
     private List<String> varList = null;
 
     @Override
     public Map<String, Double> getKnownConstants() {
         return knownConstants;
     }
 
     @Override
     public void setKnownConstants(Map<String, Double> knownConstants) {
         if (knownConstants == null) {
             throw new IllegalArgumentException("knownConstants");
         }
         this.knownConstants = knownConstants;
     }
 
     @Override
     public List<String> getVarList() {
         return varList;
     }
 
     @Override
     public void setVarList(List<String> varList) {
         this.varList = varList;
     }
 
     @Override
     public Object parse(ClauseType clause,
                         String expression) throws ParsingException {
         if (clause == null) {
             throw new IllegalArgumentException("bad clause clause(null)");
         }
         if (expression == null) {
             throw new IllegalArgumentException("expression");
         }
 
         EqualParboiledParser epp = EqualParboiledParser.INSTANCE;
 
         Rule rule = epp.getRuleMap().get(clause);
         if (rule == null) {
             throw new UnsupportedOperationException("clause '" + clause + "' is not handled");
         }
 
         rule = epp.WholeSentence(rule);
 
         RecoveringParseRunner<Object> runner;
         runner = new RecoveringParseRunner<Object>(rule);
 
         ParsingResult<Object> result = runner.run(expression);
 
         ArrayList<ParseError> list = new ArrayList<ParseError>(result.parseErrors);
 
         EqualExpressionBuilder builder = new EqualExpressionBuilder(result.inputBuffer);
 
         builder.setKnownConstants(knownConstants);
         builder.setVarList(varList);
 
         Object res = null;
         try {
             res = builder.build(clause, result.parseTreeRoot);
         } catch (ParsingFailureException e) {
             // skip
         } catch (RuntimeException e) {
             if (result.parseErrors.isEmpty()) {
                 throw e;
             }
         }
 
         list.addAll(builder.getErrors());
         checkForErrors(list);
         return res;
     }
 
     private void checkForErrors(List<ParseError> errorList) throws ParsingException {
         if (!errorList.isEmpty()) {
             List<SyntaxError> strErrors;
             strErrors = new ArrayList<SyntaxError>();
 
             for (ParseError error : errorList) {
                 strErrors.add(createSyntaxError(error));
             }
             throw new ParsingException(strErrors);
         }
     }
 
     private SyntaxError createSyntaxError(ParseError error) {
         String message = error.getErrorMessage();
         InputBuffer buf = error.getInputBuffer();
         Position pos = buf.getPosition(error.getStartIndex());
         int line = pos.line;
         int col = pos.column;
         if (error instanceof InvalidInputError) {
             InvalidInputError iiError = (InvalidInputError) error;
             List<MatcherPath> matchers = iiError.getFailedMatchers();
            Collection<String> nonTerms = scanMatchers(matchers,
                    "Variable",
                    "Constant",
                    "Parentheses",
                    "MathFunc",
                    "Expression");
            message = "Insert " + hr(nonTerms);
         } else if (message == null) {
             String badExpr = nearChars(error, buf);
             message = "syntax error near \"" + badExpr + "\"";
         }
 
         boolean oneLiner = buf.getLineCount() <= 1;
 
 
         int delta = 0;
         if (error instanceof BasicParseError) {
             delta = ((BasicParseError)error).getIndexDelta();
         }
 
         return new SyntaxError(
                 oneLiner,
                 line,
                 col,
                 error.getStartIndex() - delta,
                 error.getEndIndex() - delta,
                 message);
     }
 
    private Collection<String> scanMatchers(List<MatcherPath> matcherPaths,
                                      String ...terminals) {
        Set<String> result = new TreeSet<String>();
        for (MatcherPath path : matcherPaths) {
            while (path != null) {
                if (path.element != null
                        && path.element.matcher != null) {
                    int idx = Arrays.asList(terminals).indexOf(path.element.matcher.getLabel());
                    if (idx != -1) {
                        result.add(path.element.matcher.getLabel());
                    }
                }
                path = path.parent;
            }
        }
        return result;
    }

     private String nearChars(ParseError error, InputBuffer buf) {
         int around = 5;
         String sLeft = buf.extract(error.getStartIndex() - around,
                 error.getStartIndex());
         if (error.getStartIndex() > around) {
             sLeft = "..." + sLeft;
         }
         String sCenter = buf.extract(error.getStartIndex(), error.getEndIndex());
         String sRight = buf.extract(error.getEndIndex(), error.getEndIndex() + around + 1);
         if (sRight.length() == around + 1) {
             sRight = sRight.substring(0, around);
             sRight = sRight + "...";
         }
         return sLeft + ">>>" + sCenter + "<<<" + sRight;
     }
 
     @Override
     public AutocompletionParser createAutocompletionParser() throws UnsupportedOperationException {
         return new ParboiledAutocompletionParser();
     }
 }
