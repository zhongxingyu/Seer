 package elw.dp.mips;
 
 import elw.dp.mips.asm.Data;
 import elw.dp.mips.asm.MipsAssembler;
 import org.akraievoy.gear.G;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TaskBean {
     protected static final Pattern PATTERN_TEST_COMMENT =
             Pattern.compile("#.*$");
     protected static final Pattern PATTERN_TEST_SPEC =
             Pattern.compile(
                     "^" +
                         "([^:\\s]+)" +
                         ":" +
                         "([^=>:\\s]*)" +
                         "(=>|:)" +
                         "([^\\s]*)" +
                     "$"
             );
     protected static final Pattern PATTERN_CONST_SPEC =
             Pattern.compile(
                     "^" +
                         "([^:\\s]+)" +
                         ":" +
                         "([^\\s]*)" +
                     "$"
             );
 
     private String statement;
     private java.util.List<String> tests = new ArrayList<String>();
     private String solution;
 
     public TaskBean(String statement, List<String> tests, String solution) {
         this.solution = solution;
         this.statement = statement;
         this.tests = tests;
     }
 
     public String getSolution() {
         return solution;
     }
 
     public String getStatement() {
         return statement;
     }
 
     public List<String> getTests() {
         return tests;
     }
 
     public static Test parseTest(String test) {
         final Map<Reg, TestSpecReg> regsText = new HashMap<Reg, TestSpecReg>();
         final Map<Integer, TestSpecMem> memText = new HashMap<Integer, TestSpecMem>();
         final ParseFeedback feedback = new ParseFeedback();
         final String[] testLines = test.split("\r?\n|\r");
         for (int i = 0; i < testLines.length; i++) {
             final String testLineCompact =
                     testLines[i]
                             .trim()
                             .replaceAll("\\s*:\\s*", ":")
                             .replaceAll("\\s*=>\\s*", "=>")
                             .replaceAll("\\s+", " ");
 
             final String testLineNoComm =
                     PATTERN_TEST_COMMENT
                             .matcher(testLineCompact)
                             .replaceAll("");
             if (testLineNoComm.length() == 0) {
                 continue;
             }
 
             final ParseFeedback.LineContext line =
                     feedback.forLine(i, testLines[i]);
             final Matcher matcherTestSpec =
                     PATTERN_TEST_SPEC.matcher(testLineNoComm);
             final Matcher matcherConstSpec =
                     PATTERN_CONST_SPEC.matcher(testLineNoComm);
 
             final String what;
             final String beforeStr;
             final String afterStr;
             if (matcherTestSpec.matches()) {
                 what = matcherTestSpec.group(1);
                 beforeStr = matcherTestSpec.group(2);
                 afterStr = matcherTestSpec.group(4);
             } else if (matcherConstSpec.matches()) {
                 what = matcherConstSpec.group(1);
                 afterStr = beforeStr = matcherConstSpec.group(2);
             } else {
                 line.addError("general format eror");
                 continue;
             }
 
             final Integer before = parseBeforeAfter("before", beforeStr, line);
             final Integer after = parseBeforeAfter("after", afterStr, line);
            if (before == null && after == null) {
                line.addError("both before and after are not specified");
             }
 
             if (what.startsWith("$")) {
                 final Reg reg = MipsAssembler.parseReg(what, null, null, "");
                 if (reg == null) {
                     line.addError("register not recognized");
                     continue;
                 }
                 if (regsText.containsKey(reg)) {
                     line.addError("multiple occurence, ignored");
                 }
                 if (!G.contains(Reg.publicRegs, reg)) {
                     line.addError(
                             "register $" + reg.toString() + " is reserved"
                     );
                 }
                 if (G.contains(Reg.roRegs, reg)) {
                     line.addError(
                             "register $" + reg.toString() + " is read-only"
                     );
                 }
                 if (G.contains(Reg.autoRegs, reg)) {
                     line.addError(
                             "register $" + reg.toString() +
                                     " is set/verified automatically"
                     );
                 }
                 if (G.contains(Reg.tempRegs, reg)) {
                     line.addError(
                             "register $" + reg.toString() + " is temporary"
                     );
                 }
                 if (!line.hasErrors()) {
                     regsText.put(reg, new TestSpecReg(reg, before, after));
                 }
             } else {
                 if (!Data.isNum(what, 32)) {
                     line.addError("not an address");
                     continue;
                 }
                 final int address = (int) Data.parse(what);
                 if (address < 0) {
                     line.addError("negative address");
                 }
                 if (address % 4 > 0) {
                     line.addError("address must be word-aligned");
                 }
                 if (!line.hasErrors()) {
                     memText.put(
                             address,
                             new TestSpecMem(address, before, after)
                     );
                 }
             }
         }
 
         return new Test(regsText, memText, feedback);
     }
 
     protected static Integer parseBeforeAfter(
             String fieldName, String fieldValue,
             ParseFeedback.LineContext line
     ) {
         Integer beforeVal = null;
         if (fieldValue.length() > 0) {
             if (!Data.isNum(fieldValue, 32)) {
                 line.addError(fieldName + ": '" + fieldValue + "' is not a number");
             } else {
                 beforeVal = (int) Data.parse(fieldValue);
             }
         }
         return beforeVal;
     }
 
     public static class TestSpec {
         public final Integer before;
         public final Integer after;
         public TestSpec(Integer before, Integer after) {
             this.after = after;
             this.before = before;
         }
     }
     public static class TestSpecReg extends TestSpec {
         public final Reg reg;
         public TestSpecReg(Reg reg, Integer before, Integer after) {
             super(before, after);
             this.reg = reg;
         }
     }
     public static class TestSpecMem extends TestSpec {
         public final int address;
         public TestSpecMem(int address, Integer before, Integer after) {
             super(before, after);
             this.address = address;
         }
     }
 
     public static class Test {
         public final Map<Reg, TestSpecReg> regs;
         public final Map<Integer, TestSpecMem> mem;
         public final ParseFeedback parseErrors;
 
         public Test(
                 Map<Reg, TestSpecReg> regs,
                 Map<Integer, TestSpecMem> mem,
                 ParseFeedback parseErrors
         ) {
             this.mem = Collections.unmodifiableMap(mem);
             this.regs = Collections.unmodifiableMap(regs);
             this.parseErrors = parseErrors;
         }
 
         public String errors(Map<Integer, List<String>> lineToErrors) {
             final Map<Integer, String> lineToText = parseErrors.getLineToText();
             final StringBuilder report = new StringBuilder("test syntax broken:");
             for (Map.Entry<Integer, List<String>> errEntry : lineToErrors.entrySet()) {
                 for (String err : errEntry.getValue()) {
                     report.append("\n\t")
                             .append(errEntry.getKey())
                             .append(" : ")
                             .append(err)
                             .append(" : ")
                             .append(lineToText.get(errEntry.getKey()));
                 }
             }
             return report.toString();
         }
     }
 
     public static class ParseFeedback {
         final Map<Integer, String> lineToText = new TreeMap<Integer, String>();
         final Map<Integer, List<String>> lineToErrors = new TreeMap<Integer, List<String>>();
         final Map<Integer, List<String>> lineToWarns = new TreeMap<Integer, List<String>>();
 
         public LineContext forLine(final int lineNo, final String lineText) {
             lineToText.put(lineNo, lineText);
             return new LineContext(lineNo);
         }
 
         public Map<Integer, List<String>> getLineToErrors() {
             return Collections.unmodifiableMap(lineToErrors);
         }
 
         public Map<Integer, String> getLineToText() {
             return Collections.unmodifiableMap(lineToText);
         }
 
         public Map<Integer, List<String>> getLineToWarns() {
             return Collections.unmodifiableMap(lineToWarns);
         }
 
         public boolean hasErrors(final int lineNo) {
             return lineToErrors.containsKey(lineNo) && !lineToErrors.get(lineNo).isEmpty();
         }
 
         public boolean hasWarns(final int lineNo) {
             return lineToWarns.containsKey(lineNo) && !lineToWarns.get(lineNo).isEmpty();
         }
 
         public class LineContext {
             private final int lineNo;
 
             public LineContext(int lineNo) {
                 this.lineNo = lineNo;
             }
 
             public void addError(final String errorText) {
                 ParseFeedback.this.addError(lineNo, errorText);
             }
 
             public void addWarn(final String warnText) {
                 ParseFeedback.this.addWarn(lineNo, warnText);
             }
 
             public boolean hasErrors() {
                 return ParseFeedback.this.hasErrors(lineNo);
             }
 
             public boolean hasWarns() {
                 return ParseFeedback.this.hasWarns(lineNo);
             }
         }
 
         private static void add(Map<Integer, List<String>> dest, int lineNo, String errorText) {
             if (!dest.containsKey(lineNo)) {
                 dest.put(lineNo, new ArrayList<String>(1));
             }
             dest.get(lineNo).add(errorText);
         }
 
         protected void addError(
                 final int lineNo,
                 final String errorText
         ) {
             add(lineToErrors, lineNo, errorText);
         }
 
         protected void addWarn(
                 final int lineNo,
                 final String warnText
         ) {
             add(lineToWarns, lineNo, warnText);
         }
     }
 }
