 package jp.ac.ritsumei.is.hpcss.cellMLonGPU.app;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.generator.CommonProgramGenerator;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.generator.CudaProgramGenerator;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.generator.MpiProgramGenerator;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.generator.ProgramGenerator;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.generator.SimpleProgramGenerator;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.CellMLAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.CellMLVariableAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.RelMLAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.TecMLAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.XMLAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.XMLHandler;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxProgram;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 public class CellMLonGPUMain {
 
     // ========================================================
     // DEFINE
     // ========================================================
     private static final String MAIN_VAR_RELATION_FILENAME = "relation.txt";
     private static final String MAIN_VAR_INITIALIZE_FILENAME = "initialize.txt";
    private static final String MPI_FILENAME = "mpi.c";
 
     /** Default parser name. */
     protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
 
     private static final String GENERATOR_CUDA = "cuda";
     private static final String GENERATOR_COMMON = "common";
     private static final String GENERATOR_SIMPLE = "simple";
     private static final String GENERATOR_MPI = "mpi";
     private static final String DEFALUT_GENERATOR = GENERATOR_CUDA;
 
     protected static String generatorName = DEFALUT_GENERATOR;
     protected static Boolean isTestGenerate = false;
 
     // ===================================================
     // main
     // エントリポイント関数
     //
     // @arg
     // int nArgc : コマンドライン文字列数
     // char **pszArgv : コマンドライン文字列
     //
     // @return
     // 終了コード : int
     // ===================================================
     public static void main(String[] args) {
 
         // ---------------------------------------------------
         // 実行開始時処理
         // ---------------------------------------------------
         /* 引数チェック */
         int n = 0;
         if (args.length > n && args[n].startsWith("-")) {
             if (args[n].equals("-g") || args[n].equals("-gt")) {
                 if (args[n].equals("-gt"))
                     isTestGenerate = true;
                 n++;
                 if (args.length > n) {
                     generatorName = args[n];
                     n++;
                 } else {
                     System.err.println("error: Missing argument to -g option.");
                     printUsage();
                     System.exit(1);
                 }
             } else {
                 System.err.println("error: unknown option (" + args[n] + ").");
                 printUsage();
                 System.exit(1);
             }
         }
         if (args.length != n + 1) {
             System.err.println("error: Missing file name of RelML");
             printUsage();
             System.exit(1);
         }
         // String xml = "";
         String xml = args[n];
 
         // ---------------------------------------------------
         // XMLパーサ初期化
         // ---------------------------------------------------
         // create parser
         XMLReader parser = null;
         try {
             parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
             // バッファサイズが小さいと ハンドラの characters() が
             // 文字列を途中で切った値を返す。バッファサイズを大きくする
             // デフォルトは2k
             parser.setProperty(
                     "http://apache.org/xml/properties/input-buffer-size",
                     new Integer(16 * 0x1000));
         } catch (Exception e) {
             System.err.println("error: Unable to instantiate parser ("
                     + DEFAULT_PARSER_NAME + ")");
             System.exit(1);
         }
 
         // ---------------------------------------------------
         // 解析処理
         // ---------------------------------------------------
         /* 解析器インスタンス生成 */
         CellMLVariableAnalyzer pCellMLVariableAnalyzer = new CellMLVariableAnalyzer();
         CellMLAnalyzer pCellMLAnalyzer = new CellMLAnalyzer();
         RelMLAnalyzer pRelMLAnalyzer = new RelMLAnalyzer();
         TecMLAnalyzer pTecMLAnalyzer = new TecMLAnalyzer();
 
         /* 各ファイル名初期化 */
         String strRelMLFileName = xml;
         String strTecMLFileName;
         String strCellMLFileName;
 
         /* RelMLの解析 */
         if (!parseXMLFile(strRelMLFileName, parser, pRelMLAnalyzer)) {
             System.exit(1);
         }
         // System.out.println("******* RelML parse end ");
 
         /* 読み込みファイル名取得 */
         strTecMLFileName = pRelMLAnalyzer.getFileNameTecML();
         strCellMLFileName = pRelMLAnalyzer.getFileNameCellML();
 
         /* CellML変数部分の解析 */
         if (!parseXMLFile(strCellMLFileName, parser, pCellMLVariableAnalyzer)) {
             System.exit(1);
         }
         // System.out.println("******* CellML変数 parse end");
 
         /* 変数テーブルをCellML解析器に渡す */
         pCellMLAnalyzer.setComponentTable(pCellMLVariableAnalyzer
                 .getComponentTable());
 
         /* CellMLの解析 */
         if (!parseXMLFile(strCellMLFileName, parser, pCellMLAnalyzer)) {
             System.exit(1);
         }
         // System.out.println("******* CellML parse end");
 
         /* TecMLの解析 */
         if (!parseXMLFile(strTecMLFileName, parser, pTecMLAnalyzer)) {
             System.exit(1);
         }
         // System.out.println("******* TecML parse end");
 
         // ---------------------------------------------------
         // 目的プログラム生成
         // ---------------------------------------------------
         /* プログラム生成器インスタンス生成 */
         ProgramGenerator pProgramGenerator = null;
         SyntaxProgram pSynProgram = null;
         ProgramGenerator pSerialProgramGenerator = null;
         SyntaxProgram pSynSerialProgram = null;
 
         try {
             if (generatorName.equals(GENERATOR_CUDA)) {
                 pProgramGenerator = new CudaProgramGenerator(pCellMLAnalyzer,
                         pRelMLAnalyzer, pTecMLAnalyzer);
             } else if ((generatorName.equals(GENERATOR_COMMON))) {
                 pProgramGenerator = new CommonProgramGenerator(pCellMLAnalyzer,
                         pRelMLAnalyzer, pTecMLAnalyzer);
             } else if ((generatorName.equals(GENERATOR_SIMPLE))) {
                 pProgramGenerator = new SimpleProgramGenerator(pCellMLAnalyzer,
                         pRelMLAnalyzer, pTecMLAnalyzer);
             } else if ((generatorName.equals(GENERATOR_MPI))) {
                 pProgramGenerator = new MpiProgramGenerator(pCellMLAnalyzer,
                         pRelMLAnalyzer, pTecMLAnalyzer);
             } else {
                 System.err.println("error: invalid Generator name ("
                         + generatorName + ").");
                 printUsage();
                 System.exit(1);
             }
 
             /* パラメータをハードコードで設定 */
             pProgramGenerator.setElementNum(1024);
             pProgramGenerator.setTimeParam(0.0, 400.0, 0.01);
             pProgramGenerator.setIsTestGenerate(isTestGenerate);
 
             /* プログラム構文出力 */
             pSynProgram = pProgramGenerator.getSyntaxProgram();
 
         } catch (Exception e) {
             /* エラー出力 */
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
             System.err.println("failed to translate program");
             System.exit(1);
         }
 
         // ---------------------------------------------------
         // 出力
         // ---------------------------------------------------
         try {
             /* RelML内容出力 */
             // pRelMLAnalyzer.printContents();
 
             /* CellML内容出力 */
             // pCellMLAnalyzer.printContents();
 
             /* TecML内容出力 */
             // pTecMLAnalyzer.printContents();
 
             /* 目的プログラム出力 */
             if (pSynProgram != null) {
                 /* 出力開始線 */
                 // System.out.println("[output]------------------------------------");
 
                 /* プログラム出力 */
                 // console output
                 // System.out.println(pSynProgram.toLegalString());
 
                 // file output
                 String srcDir = "";
                 String programCode = pSynProgram.toLegalString();
 
                 PrintWriter out = null;
                 out = new PrintWriter(new BufferedWriter(new FileWriter(srcDir
                        + MPI_FILENAME)));
                 out.println(programCode);
                 out.close();
             }
             // System.exit(1); // *ML内容出力確認時に有効にする
         } catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
             System.exit(1);
         }
 
         try {
             PrintWriter out = null;
             /* 変数関係の出力 */
             out = new PrintWriter(new BufferedWriter(new FileWriter(
                     MAIN_VAR_RELATION_FILENAME)));
             pProgramGenerator.outputVarRelationList(out);
             out.close();
 
             /* 初期化式の出力 */
             out = new PrintWriter(new BufferedWriter(new FileWriter(
                     MAIN_VAR_INITIALIZE_FILENAME)));
             pProgramGenerator.outputInitializeList(out,
                     pCellMLVariableAnalyzer.getComponentTable());
             out.close();
         } catch (Exception e) {
             System.err.println(e.getMessage());
             e.printStackTrace(System.err);
             System.exit(1);
         }
     }
 
     // =============================================================
     // parseXMLFile
     // XMLファイル解析関数
     //
     // @arg
     // string strXMLFileName : 読み込みファイル名
     // SAX2XMLReader* pParser : XMLパーサインスタンス
     // XMLAnalyzer* pXMLAnalyzer : RelML解析器インスタンス
     //
     // @return
     // 成否判定 : bool
     //
     // =============================================================
     static boolean parseXMLFile(String strXMLFileName, XMLReader pParser,
             XMLAnalyzer pXMLAnalyzer) {
 
         try {
 
             /* ファイルの存在を確認 */
             if (!new File(strXMLFileName).canRead()) {
                 System.err.println("file can't open : " + strXMLFileName);
                 return false;
             }
 
             /* パース処理 */
             XMLHandler handler = new XMLHandler(pXMLAnalyzer);
             pParser.setContentHandler(handler);
             pParser.parse(strXMLFileName);
 
         } catch (SAXParseException e) {
             /* エラー出力 */
             System.err.println("failed to parse file : " + strXMLFileName);
             return false;
         } catch (Exception e) {
 
             /* 例外メッセージ出力 */
             System.err.println("error: Parse error occurred - "
                     + e.getMessage());
             Exception se = e;
             if (e instanceof SAXException) {
                 se = ((SAXException) e).getException();
             }
             if (se != null) {
                 se.printStackTrace(System.err);
             } else {
                 e.printStackTrace(System.err);
             }
 
             /* エラー出力 */
             System.err.println("failed to parse file : " + strXMLFileName);
             return false;
         }
 
         return true;
     }
 
     private static void printUsage() {
         System.err.println("usage: ./parser [option] \"filename of RelML\"");
         System.err.println("option:");
         System.err
                 .println("  -g name     Select Generator by name. {cuda|common|simple}");
         System.err.println("default:");
         System.err.println("  Generator:  " + DEFALUT_GENERATOR);
     }
 }
