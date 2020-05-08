 package engine.usecases;
 
 import engine.calculation.*;
 import engine.calculation.evaluator.FunctionEvaluator;
 import engine.calculation.evaluator.ImmediateFunctionEvaluator;
 import engine.calculation.vector.VectorEvaluator;
 import engine.calculation.vector.VectorMachineEvaluator;
 import engine.calculation.vector.implementations.VectorMachineBuilder;
 import engine.expressions.Equation;
 import engine.expressions.parser.ClauseType;
 import engine.expressions.parser.ExpressionParser;
 import engine.expressions.parser.parboiled.ParboiledExpressionParser;
 import engine.expressions.parser.ParsingException;
 import engine.calculation.drawables.DrawToImage;
 import engine.calculation.drawables.Drawable;
 import engine.calculation.drawables.RectRange;
 import org.junit.BeforeClass;
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theories;
 import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 
 import java.io.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static org.junit.Assert.fail;
 
 /**
  * Look use_cases.txt in src/test/resources
  *
  * User: Oleksiy Pylypenko
  * Date: 3/5/13
  * Time: 9:01 AM
  */
 @RunWith(Theories.class)
 public class UseCasesTest {
     private final static int MAX_CONCURRENCY = Runtime.getRuntime().availableProcessors();
     private static final File DIR = new File("test_images");
     static {
         DIR.mkdirs();
     }
     private ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENCY);
     private ViewportSize size = new ViewportSize(1000, 1000);
 
     static class Header {
         static {
             System.out.println("Look '" + DIR + "' directory for image results");
             System.out.printf("%20s ", "EQUATION");
             System.out.printf("%-8s", "BASIC");
             System.out.printf("%-8s", "VECTOR");
             System.out.printf("%-8s", "VECTOR2");
             System.out.println();
         }
     }
 
     @Theory
     public void testUseCase(EqualUseCase useCase) throws Exception {
         new Header();
         StringTokenizer tokenizer = new StringTokenizer(useCase.bounds);
         ViewportBounds bounds = new ViewportBounds(
                 Double.parseDouble(tokenizer.nextToken()),
                 Double.parseDouble(tokenizer.nextToken()),
                 Double.parseDouble(tokenizer.nextToken()),
                 Double.parseDouble(tokenizer.nextToken()));
 
         String eqs = useCase.equations.trim().replaceAll("\n\r?", ",");
 
         System.out.printf("%20s ", eqs);
 
         ExpressionParser parser = new ParboiledExpressionParser();
         Equation []equations;
         try {
             equations = (Equation[]) parser.parse(ClauseType.EQUATIONS, eqs);
         } catch (ParsingException ex) {
             fail("syntax error on '" + useCase.equations + "', exception: " + ex);
             return;
         }
 
         CalculationParameters params = new CalculationParameters(bounds, size, 0.0,
                 equations);
 
         long time;
         time = calculateAndWrite(params,
                 createBasicEngine(),
                 String.format("test%03db.png",
                 useCase.number));
         System.out.printf("%8s", time + " ms");
 
         time = calculateAndWrite(params,
                 createVectorEngine(),
                 String.format("test%03dv.png",
                 useCase.number));
         System.out.printf("%8s", time + " ms");
 
         time = calculateAndWrite(params,
                 createVectorEngine2(),
                 String.format("test%03dv2.png",
                 useCase.number));
         System.out.printf("%8s", time + " ms");
         System.out.println("");
     }
 
     private long calculateAndWrite(CalculationParameters params, CalculationEngine engine, String filename) throws IOException {
         engine.calculate(params);
         long time = System.currentTimeMillis();
         CalculationResults results = engine.calculate(params);
         time = System.currentTimeMillis() - time;
         RectRange range = RectRange.fromViewportSize(size);
         DrawToImage drawToImage = new DrawToImage(range);
         for (Drawable drawable : results.getDrawables()) {
             drawToImage.draw(range, drawable);
         }
         drawToImage.writePng(new File(DIR, filename), range);
         return time;
     }
 
     private CalculationEngine createBasicEngine() {
         FunctionEvaluator evaluator = new ImmediateFunctionEvaluator();
         return new BasicCalculationEngine(evaluator);
     }
 
     private CalculationEngine createVectorEngine() {
         VectorMachineBuilder builder = new VectorMachineBuilder();
         builder.setConcurrency(1, executor);
         VectorEvaluator evaluator = new VectorMachineEvaluator(builder);
         return new VectorCalculationEngine(evaluator);
     }
 
     private CalculationEngine createVectorEngine2() {
         VectorMachineBuilder builder = new VectorMachineBuilder();
         builder.setConcurrency(1, executor);
         VectorEvaluator evaluator = new VectorMachineEvaluator(builder);
         return new VectorCalculationEngine2(evaluator);
     }
 
     private static EqualUseCase []useCases;
 
     @DataPoints
     public static EqualUseCase[] getUseCases() {
         return useCases;
     }
 
     @BeforeClass
     public static void initUseCases() {
        try {
            useCases = EqualUseCase.read(UseCasesTest.class.getResource("use_cases.txt"));
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            throw new AssertionError("loading problem", ex);
        }
     }
 
     private static class EqualUseCase {
         private final int number;
         private final String description;
         private final String equations;
         private final String bounds;
 
         private EqualUseCase(int number, String description, String equations, String bounds) {
             this.number = number;
             this.description = description;
             this.equations = equations;
             this.bounds = bounds;
         }
 
         public static EqualUseCase[] read(URL resource) {
             Scanner scanner;
             try {
                 if (resource == null) {
                     throw new FileNotFoundException("resource not found");
                 }
                 scanner = new Scanner(resource.openStream());
             } catch (IOException e) {
                 throw new RuntimeException("failed to read use-cases", e);
             }
             try {
                 while (scanner.hasNextLine()) {
                     String line = scanner.nextLine();
                     if (line.trim().equals("------------------------")) {
                         break;
                     }
                 }
                 if (!scanner.hasNextLine()) {
                     return new EqualUseCase[0];
                 }
 
                 List<EqualUseCase> useCaseList = new ArrayList<EqualUseCase>();
                 for (int i = 1; scanner.hasNext(); i++) {
                     String str = scanner.next("\\d+[!\\.]");
                     boolean skip = false;
                     if (str.substring(str.length() - 1).equals("!")) {
                         skip = true;
                     }
                     str = str.substring(0, str.length() - 1);
                     int t = Integer.parseInt(str);
                     if (i != t) {
                         System.err.println("Mismatch in use-case numbering " + i + " " + t);
                     }
 
                     String line = scanner.nextLine();
                     line = line.replaceAll("^\\.", "");
                     line = line.trim();
                     String description = line;
                     if (skip) {
                         System.err.println("Skipping use case: " + line);
                     }
 
                     String bounds = scanner.nextLine().trim();
 
                     StringBuilder builder = new StringBuilder();
                     while (scanner.hasNext()) {
                         line = scanner.nextLine();
                         if (line.trim().isEmpty()) {
                             break;
                         }
 
                         builder.append(line);
                         builder.append("\n");
                     }
                     String equations = builder.toString();
 
                     if (!skip) {
                         useCaseList.add(new EqualUseCase(i, description, equations, bounds));
                     }
                 }
 
                 return useCaseList.toArray(new EqualUseCase[useCaseList.size()]);
             } finally {
                 scanner.close();
             }
         }
     }
 }
