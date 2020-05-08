 package xi.optimizer;
 
import static xi.ast.BuiltIn.B;
import static xi.ast.BuiltIn.C;
import static xi.ast.BuiltIn.I;

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 
 import stefan.Cout;
 import xi.ast.App;
 import xi.ast.Expr;
 import xi.ast.Module;
 import xi.ast.Name;
 import xi.ast.parser.AstSKParser;
 import xi.ast.stefan.LazyTree;
 import xi.util.Logging;
 
 import com.martiansoftware.jsap.FlaggedOption;
 import com.martiansoftware.jsap.JSAPResult;
 import com.martiansoftware.jsap.SimpleJSAP;
 import com.martiansoftware.jsap.Switch;
 import com.martiansoftware.jsap.UnflaggedOption;
 import com.martiansoftware.jsap.stringparsers.FileStringParser;
 
 /**
  * A linker for the SK output.
  * 
  * @author Leo
  */
 public class Optimizer extends AstSKParser {
 
     /**
      * Hidden default constructor.
      * 
      * @param map
      *            definition map
      */
     private Optimizer(final Map<String, Expr> map) {
         super(map);
     }
 
     public static void main(final String[] args) throws Exception {
 
         final SimpleJSAP parser = new SimpleJSAP("saslo");
         // verbose flag
         parser.registerParameter(new Switch("verbose", 'v', "verbose",
                 "if set, info messages are printed to STDERR"));
         // output filename
         final FlaggedOption out = new FlaggedOption("out", FileStringParser
                 .getParser(), null, false, 'o', "out",
                 "output file, default is STDOUT");
         parser.registerParameter(out);
         // input file
         final FileStringParser fsp = FileStringParser.getParser().setMustExist(
                 true).setMustBeFile(true);
         final UnflaggedOption in = new UnflaggedOption("in", fsp, null, false,
                 false, "input file, default is STDIN");
         parser.registerParameter(in);
 
         final JSAPResult opts = parser.parse(args);
         if (parser.messagePrinted()) {
             return;
         }
 
         // set verbose
         if (opts.getBoolean("verbose")) {
             Logging.setLevel(Level.ALL);
         }
 
         final File inFile = opts.getFile("in"), outFile = opts.getFile("out");
         final Reader r = new InputStreamReader(inFile == null ? System.in
                 : new FileInputStream(inFile), "UTF-8");
         final Writer w = new OutputStreamWriter(outFile == null ? System.out
                 : new FileOutputStream(outFile), "UTF-8");
 
         Cout.module(LazyTree.create(optimize(r)), w);
     }
 
     /**
      * Optimizes the SK code provided by the given reader.
      * 
      * @param r
      *            SK code reader
      * @return AST of the optimized SK module
      */
     private static Module optimize(final Reader r) {
         final Map<String, Expr> map = new HashMap<String, Expr>();
         final Optimizer opt = new Optimizer(map);
         opt.read(r);
 
         final Module mod = new Module(false);
         for (final Entry<String, Expr> e : map.entrySet()) {
             mod.addDefinition(Name.valueOf(e.getKey()), e.getValue());
         }
         return mod;
     }
 
     @Override
     protected Expr app(final Expr f, final Expr x) {
         // optimize here
        final Expr[] ci = { C, I, null };
        if (f.match(ci)) {
            return App.create(x, ci[2]);
        }

        final Expr[] b = { B, null };
        if (f.match(b)) {
            System.out.println(f + " " + x);
        }
         return App.create(f, x);
     }
 }
