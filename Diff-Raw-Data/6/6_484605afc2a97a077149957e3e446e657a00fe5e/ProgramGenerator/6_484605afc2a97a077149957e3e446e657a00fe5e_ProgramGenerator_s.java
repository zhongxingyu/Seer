 package jp.ac.ritsumei.is.hpcss.cellMLonGPU.generator;
 
 import java.io.PrintWriter;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.exception.CellMLException;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.exception.MathException;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.exception.RelMLException;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.exception.SyntaxException;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.exception.TableException;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.exception.TranslateException;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.MathExpression;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.MathFactor;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.MathFactory;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.MathMLDefinition.eMathOperand;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.MathMLDefinition.eMathOperator;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.MathOperand;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.MathOperator;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_assign;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_ci;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_cn;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_divide;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_exp;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_inc;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_leq;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_lt;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_minus;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_plus;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_remainder;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.mathML.Math_times;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.CellMLAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.RelMLAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.parser.TecMLAnalyzer;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxCallFunction;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxCondition;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxControl;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxControl.eControlKind;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxDataType;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxDataType.eDataType;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxDeclaration;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxExpression;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxFunction;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxPreprocessor;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxProgram;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.syntax.SyntaxStatement;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.table.ComponentTable;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.table.VariableTable;
 import jp.ac.ritsumei.is.hpcss.cellMLonGPU.utility.StringUtil;
 
 /**
  * プログラム構文生成クラス
  */
 public abstract class ProgramGenerator {
 
     public static final String PROG_FUNC_STR_MAIN = "main";
     public static final String PROG_FUNC_STR_MPIUT_MAIN = "mainCellMLCompiler";
     public static final String PROG_VAR_STR_ARGC = "argc";
     public static final String PROG_VAR_STR_ARGV = "argv";
 
     public static final String PROG_FUNC_STR_MEMSET = "memset";
     public static final String PROG_FUNC_STR_MALLOC = "malloc";
     public static final String PROG_FUNC_STR_FREE = "free";
 
     /* 各解析器インスタンス */
     protected CellMLAnalyzer m_pCellMLAnalyzer;
     protected RelMLAnalyzer m_pRelMLAnalyzer;
     protected TecMLAnalyzer m_pTecMLAnalyzer;
 
     /* プログラム生成用パラメータ */
     protected int m_unElementNum; // 計算要素数
     protected double m_dStartTime; // 実験開始時刻
     protected double m_dEndTime; // 実験終了時刻
     protected double m_dDeltaTime; // 時間増分
 
     /* テスト関数を生成するか */
     static protected Boolean m_isTestGenerate = false;
     static protected SyntaxFunction m_testInitFunc;
     static protected SyntaxFunction m_testFunc;
 
     /*-----コンストラクタ-----*/
     public ProgramGenerator(CellMLAnalyzer pCellMLAnalyzer,
             RelMLAnalyzer pRelMLAnalyzer, TecMLAnalyzer pTecMLAnalyzer) {
         m_pCellMLAnalyzer = pCellMLAnalyzer;
         m_pRelMLAnalyzer = pRelMLAnalyzer;
         m_pTecMLAnalyzer = pTecMLAnalyzer;
         m_unElementNum = 0;
         m_dStartTime = 0.0;
         m_dEndTime = 0.0;
         m_dDeltaTime = 0.0;
     }
 
     /*-----プログラム構文取得メソッド-----*/
     public abstract SyntaxProgram getSyntaxProgram() throws MathException,
             CellMLException, RelMLException, TranslateException,
             SyntaxException;
 
     /*-----プログラム生成用パラメータ設定メソッド-----*/
 
     // ========================================================
     // setElementNum
     // 要素数(配列サイズ)設定メソッド
     //
     // @arg
     // unsigned int unElementNum : 要素数
     //
     // ========================================================
     public void setElementNum(int unElementNum) {
         m_unElementNum = unElementNum;
     }
 
     // ========================================================
     // setTimeParam
     // 時間関係設定メソッド
     //
     // @arg
     // double dStartTime : 実験開始時刻
     // double dEndTime : 実験終了時刻
     // double dDeltaTime : 時間刻み幅
     //
     // ========================================================
     public void setTimeParam(double dStartTime, double dEndTime,
             double dDeltaTime) {
         m_dStartTime = dStartTime;
         m_dEndTime = dEndTime;
         m_dDeltaTime = dDeltaTime;
     }
 
 
     /**
      * set isTestGenerate
      * @param isTestGenerate
      *
      * @author Yuu Shigetani
      */
     public void setIsTestGenerate(Boolean isTestGenerate) {
         m_isTestGenerate = isTestGenerate;
        m_unElementNum = 32;
        m_dEndTime = 0.100000;
     }
     public void setTestInitFunc(SyntaxFunction testInitFunc) {
         m_testInitFunc = testInitFunc;
     }
     public void setTestFunc(SyntaxFunction testFunc) {
         m_testFunc = testFunc;
     }
 
     /*-----変数リスト出力メソッド-----*/
 
     // ========================================================
     // outputVarRelationList
     // 変数対応関係出力メソッド
     //
     // @arg
     // ofstream &ofs : 出力ストリームへの参照
     //
     // ========================================================
     public void outputVarRelationList(PrintWriter out) throws MathException {
         for (Math_ci it : m_pCellMLAnalyzer.getM_vecTimeVar()) {
             out.println(it.toLegalString() + "\t"
                     + m_pTecMLAnalyzer.getM_pTimeVar().toLegalString());
         }
         for (int i = 0; i < m_pCellMLAnalyzer.getM_vecDiffVar().size(); i++) {
             Math_ci it = m_pCellMLAnalyzer.getM_vecDiffVar().get(i);
             out.println(it.toLegalString() + "\t"
                     + m_pTecMLAnalyzer.getM_pInputVar().toLegalString() + "["
                     + i + "]");
         }
         for (int i = 0; i < m_pCellMLAnalyzer.getM_vecArithVar().size(); i++) {
             Math_ci it = m_pCellMLAnalyzer.getM_vecArithVar().get(i);
             out.println(it.toLegalString()
                     + "\t"
                     + m_pTecMLAnalyzer.getM_vecArithVar().get(0)
                             .toLegalString() + "[" + i + "]");
         }
         for (int i = 0; i < m_pCellMLAnalyzer.getM_vecConstVar().size(); i++) {
             Math_ci it = m_pCellMLAnalyzer.getM_vecConstVar().get(i);
             out.println(it.toLegalString()
                     + "\t"
                     + m_pTecMLAnalyzer.getM_vecConstVar().get(0)
                             .toLegalString() + "[" + i + "]");
         }
     }
 
     // ========================================================
     // outputInitializeList
     // 変数初期化式出力メソッド
     //
     // @arg
     // ofstream &ofs : 出力ストリームへの参照
     // ComponentTable* pComponentTable : 変数テーブル
     //
     // ========================================================
     public void outputInitializeList(PrintWriter out,
             ComponentTable pComponentTable) throws MathException {
         for (int i = 0; i < m_pCellMLAnalyzer.getM_vecDiffVar().size(); i++) {
             Math_ci it = m_pCellMLAnalyzer.getM_vecDiffVar().get(i);
 
             /* 名前の取得と分解 */
             String strVarName = it.toLegalString();
             int nDotPos = strVarName.indexOf(".");
             String strCompName = strVarName.substring(0, nDotPos);
             String strLocalName = strVarName.substring(nDotPos + 1);
             String strInitValue;
 
             /* テーブルの探索 */
             try {
                 VariableTable pVariableTable = pComponentTable
                         .searchTable(strCompName);
                 strInitValue = pVariableTable.getInitValue(strLocalName);
             } catch (TableException e) {
                 System.err.println(strVarName + " " + e.getMessage());
                 continue;
             }
 
             out.println(m_pTecMLAnalyzer.getM_pInputVar().toLegalString() + "["
                     + i + "] = " + strInitValue + ";");
         }
         for (int i = 0; i < m_pCellMLAnalyzer.getM_vecArithVar().size(); i++) {
             Math_ci it = m_pCellMLAnalyzer.getM_vecArithVar().get(i);
 
             /* 名前の取得と分解 */
             String strVarName = it.toLegalString();
             int nDotPos = strVarName.indexOf(".");
             String strCompName = strVarName.substring(0, nDotPos);
             String strLocalName = strVarName.substring(nDotPos + 1);
             String strInitValue;
 
             /* テーブルの探索 */
             try {
                 VariableTable pVariableTable = pComponentTable
                         .searchTable(strCompName);
                 strInitValue = pVariableTable.getInitValue(strLocalName);
             } catch (TableException e) {
                 continue;
             }
 
             out.println(m_pTecMLAnalyzer.getM_vecArithVar().get(0)
                     .toLegalString()
                     + "[" + i + "] = " + strInitValue + ";");
         }
         for (int i = 0; i < m_pCellMLAnalyzer.getM_vecConstVar().size(); i++) {
             Math_ci it = m_pCellMLAnalyzer.getM_vecConstVar().get(i);
 
             /* 名前の取得と分解 */
             String strVarName = it.toLegalString();
             int nDotPos = strVarName.indexOf(".");
             String strCompName = strVarName.substring(0, nDotPos);
             String strLocalName = strVarName.substring(nDotPos + 1);
             String strInitValue;
 
             /* テーブルの探索 */
             try {
                 VariableTable pVariableTable = pComponentTable
                         .searchTable(strCompName);
                 strInitValue = pVariableTable.getInitValue(strLocalName);
             } catch (TableException e) {
                 continue;
             }
 
             out.println(m_pTecMLAnalyzer.getM_vecConstVar().get(0)
                     .toLegalString()
                     + "[" + i + "] = " + strInitValue + ";");
         }
     }
 
     /*-----定型構文生成メソッド-----*/
 
     // ========================================================
     // createNewProgram
     // 新規プログラム構文インスタンスを生成
     //
     // @return
     // プログラム構文インスタンス : SyntaxProgram*
     //
     // ========================================================
     public SyntaxProgram createNewProgram() {
         return new SyntaxProgram();
     }
 
     // ========================================================
     // createMainFunction
     // メイン関数構文インスタンスを生成
     //
     // @return
     // 関数構文インスタンス : SyntaxFunction*
     //
     // ========================================================
     public SyntaxFunction createMainFunction() throws MathException {
         /* 関数本体の生成 */
         SyntaxDataType pSynIntType = new SyntaxDataType(eDataType.DT_INT, 0);
         SyntaxFunction pSynMainFunc = new SyntaxFunction(PROG_FUNC_STR_MAIN,
                 pSynIntType);
 
         /* 引数宣言の生成 */
         SyntaxDataType pSynPPCharType = new SyntaxDataType(eDataType.DT_CHAR, 2);
         Math_ci pArgcVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, PROG_VAR_STR_ARGC);
         Math_ci pArgvVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, PROG_VAR_STR_ARGV);
         SyntaxDeclaration pSynArgcDec = new SyntaxDeclaration(pSynIntType,
                 pArgcVar);
         SyntaxDeclaration pSynArgvDec = new SyntaxDeclaration(pSynPPCharType,
                 pArgvVar);
 
         /* 引数宣言の追加 */
         pSynMainFunc.addParam(pSynArgcDec);
         pSynMainFunc.addParam(pSynArgvDec);
 
         return pSynMainFunc;
     }
 
     /**
      * create arg
      * @param dataType
      * @param argName
      * @return pSynArgDec
      *
      * @author Yuu Shigetani
      */
     public SyntaxDeclaration createArg(eDataType dataType, int pointerNum, Math_ci pArgVar) throws MathException {
         SyntaxDataType pSynType = new SyntaxDataType(dataType, pointerNum);
         SyntaxDeclaration pSynArgDec = new SyntaxDeclaration(pSynType,
                 pArgVar);
         return pSynArgDec;
     }
 
     /**
      * create function
      * @param pSynReturnType
      * @param functionName
      * @param pSynArgs[]
      * @return pSynFunc
      *
      * @author Yuu Shigetani
      */
     public SyntaxFunction createFunction(SyntaxDataType pSynReturnType, String functionName, SyntaxDeclaration... pSynArgs)
             throws MathException {
         /* 関数本体の生成 */
         SyntaxFunction pSynFunc = new SyntaxFunction(functionName,
                 pSynReturnType);
 
         /* 引数宣言の追加 */
         for (int i=0; i < pSynArgs.length; i++){
             pSynFunc.addParam(pSynArgs[i]);
         }
 
         return pSynFunc;
     }
 
     /**
      * create while
      * @param pCondition
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public SyntaxControl createWhile(MathFactor pCondition)
             throws MathException {
         MathExpression pConditionExp = new MathExpression(pCondition);
         /* while条件生成 */
         SyntaxCondition pSynWhileCond = new SyntaxCondition(pConditionExp);
 
         /* whileを生成 */
         SyntaxControl pSynWhile = new SyntaxControl(eControlKind.CTRL_WHILE,
                 pSynWhileCond);
 
         /* while構文を返す */
         return pSynWhile;
     }
 
     /**
      * create if
      * @param pCondition
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public SyntaxControl createIf(MathFactor pCondition)
             throws MathException {
         MathExpression pConditionExp = new MathExpression(pCondition);
         /* if条件生成 */
         SyntaxCondition pSynIfCond = new SyntaxCondition(pConditionExp);
 
         /* ifを生成 */
         SyntaxControl pSynIf = new SyntaxControl(eControlKind.CTRL_IF,
                 pSynIfCond);
 
         /* if構文を返す */
         return pSynIf;
     }
 
     /**
      * create break
      * @param pSynControl
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public void createBreak(SyntaxControl pSynControl)
             throws MathException {
 
         /* breakを生成 */
         MathExpression pBreakExp = new MathExpression((Math_cn)MathFactory.createOperand(eMathOperand.MOPD_CN, "break"));
         SyntaxExpression pSynBreak = new SyntaxExpression(pBreakExp);
 
         /* breakを追加 */
         pSynControl.addStatement(pSynBreak);
 
     }
 
     /**
      * create continue
      * @param pControl
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public void createContinue(SyntaxControl pSynControl)
             throws MathException {
 
         /* continueを生成 */
         MathExpression pContinueExp = new MathExpression((Math_cn)MathFactory.createOperand(eMathOperand.MOPD_CN, "continue"));
         SyntaxExpression pSynContinue = new SyntaxExpression(pContinueExp);
 
         /* continueを追加 */
         pSynControl.addStatement(pSynContinue);
 
     }
 
     /**
      * create condition
      * @param pFirstVariable
      * @param pSecondVariable
      * @param pCondition
      * @return pConditionExp
      *
      * @author Yuu Shigetani
      */
     public MathFactor createCondition(MathFactor pFirstVariable,
             MathFactor pSecondVariable, MathOperator pCondition) {
         //add element
         pCondition.addFactor(pFirstVariable);
         pCondition.addFactor(pSecondVariable);
         //conect elements
         //MathExpression pConditionExp = new MathExpression(pCondition);
 
         return pCondition;
     }
 
     /**
      * create else if
      * @param pCondition
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public SyntaxControl createElseIf(MathFactor pCondition)
             throws MathException {
         MathExpression pConditionExp = new MathExpression(pCondition);
         /* if条件生成 */
         SyntaxCondition pSynIfCond = new SyntaxCondition(pConditionExp);
 
         /* ifを生成 */
         SyntaxControl pSynElseIf = new SyntaxControl(eControlKind.CTRL_ELSEIF,
                 pSynIfCond);
 
         /* if構文を返す */
         return pSynElseIf;
     }
 
     /**
      * create else
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public SyntaxControl createElse() throws MathException {
 
         SyntaxCondition pSynIfCond = null;
         /* ifを生成 */
         SyntaxControl pSynElseIf = new SyntaxControl(eControlKind.CTRL_ELSE,
                 pSynIfCond);
 
         /* if構文を返す */
         return pSynElseIf;
     }
 
     public SyntaxControl createLoop(Math_cn workerStartNum,
             Math_ci pEnd, MathOperand pLoopVariable) throws MathException {
 
         /* ループ条件式生成 */
         Math_lt pMathLt = (Math_lt) MathFactory
                 .createOperator(eMathOperator.MOP_LT);
 
         pMathLt.addFactor(pLoopVariable);
         pMathLt.addFactor(pEnd);
         MathExpression pConditionExp = new MathExpression(pMathLt);
 
         /* ループ中初期化式生成 */
         Math_assign pMathAssign = (Math_assign) MathFactory
                 .createOperator(eMathOperator.MOP_ASSIGN);
 
         pMathAssign.addFactor(pLoopVariable);
         pMathAssign.addFactor(workerStartNum);
         MathExpression pInitExp = new MathExpression(pMathAssign);
 
         /* ループ再初期化式生成 */
         Math_inc pMathInc = (Math_inc) MathFactory
                 .createOperator(eMathOperator.MOP_INC);
         pMathInc.addFactor(pLoopVariable);
         MathExpression pReInitExp = new MathExpression(pMathInc);
 
         /* ループ条件生成 */
         SyntaxCondition pSynLoopCond = new SyntaxCondition(pConditionExp);
         pSynLoopCond.setInitExpression(pInitExp, pReInitExp);
 
         /* ループを生成 */
         SyntaxControl pSynFor = new SyntaxControl(eControlKind.CTRL_FOR,
                 pSynLoopCond);
 
         /* ループ構文を返す */
         return pSynFor;
     }
 
     public SyntaxControl createLoop(Math_ci workerStartNum,
             Math_ci pEnd, MathOperand pLoopVariable) throws MathException {
 
         /* ループ条件式生成 */
         Math_lt pMathLt = (Math_lt) MathFactory
                 .createOperator(eMathOperator.MOP_LT);
 
         pMathLt.addFactor(pLoopVariable);
         pMathLt.addFactor(pEnd);
         MathExpression pConditionExp = new MathExpression(pMathLt);
 
         /* ループ中初期化式生成 */
         Math_assign pMathAssign = (Math_assign) MathFactory
                 .createOperator(eMathOperator.MOP_ASSIGN);
 
         pMathAssign.addFactor(pLoopVariable);
         pMathAssign.addFactor(workerStartNum);
         MathExpression pInitExp = new MathExpression(pMathAssign);
 
         /* ループ再初期化式生成 */
         Math_inc pMathInc = (Math_inc) MathFactory
                 .createOperator(eMathOperator.MOP_INC);
         pMathInc.addFactor(pLoopVariable);
         MathExpression pReInitExp = new MathExpression(pMathInc);
 
         /* ループ条件生成 */
         SyntaxCondition pSynLoopCond = new SyntaxCondition(pConditionExp);
         pSynLoopCond.setInitExpression(pInitExp, pReInitExp);
 
         /* ループを生成 */
         SyntaxControl pSynFor = new SyntaxControl(eControlKind.CTRL_FOR,
                 pSynLoopCond);
 
         /* ループ構文を返す */
         return pSynFor;
     }
     // ========================================================
     // createSyntaxDataNumLoop
     // データ数ループ構文インスタンスを生成
     //
     // @arg
     // MathOperand* pDataNumVariable: データ数変数インスタンス
     // MathOperand* pIndexVariable : インデックス変数インスタンス
     //
     // @return
     // 制御文構文インスタンス : SyntaxControl*
     //
     // ========================================================
     public SyntaxControl createSyntaxDataNumLoop(MathOperand pDataNumVariable,
             MathOperand pIndexVariable) throws MathException {
         /* ループ条件式生成 */
         Math_lt pMathLt = (Math_lt) MathFactory
                 .createOperator(eMathOperator.MOP_LT);
         pMathLt.addFactor(pIndexVariable);
         pMathLt.addFactor(pDataNumVariable);
         MathExpression pConditionExp = new MathExpression(pMathLt);
 
         /* ループ中初期化式生成 */
         Math_assign pMathAssign = (Math_assign) MathFactory
                 .createOperator(eMathOperator.MOP_ASSIGN);
         Math_cn pLoopInit = (Math_cn) MathFactory.createOperand(
                 eMathOperand.MOPD_CN, "0");
         pMathAssign.addFactor(pIndexVariable);
         pMathAssign.addFactor(pLoopInit);
         MathExpression pInitExp = new MathExpression(pMathAssign);
 
         /* ループ再初期化式生成 */
         Math_inc pMathInc = (Math_inc) MathFactory
                 .createOperator(eMathOperator.MOP_INC);
         pMathInc.addFactor(pIndexVariable);
         MathExpression pReInitExp = new MathExpression(pMathInc);
 
         /* ループ条件生成 */
         SyntaxCondition pSynLoopCond = new SyntaxCondition(pConditionExp);
         pSynLoopCond.setInitExpression(pInitExp, pReInitExp);
 
         /* ループを生成 */
         SyntaxControl pSynFor = new SyntaxControl(eControlKind.CTRL_FOR,
                 pSynLoopCond);
 
         /* ループ構文を返す */
         return pSynFor;
     }
 
     // ========================================================
     // createSyntaxTimeLoop
     // 時間によるループ構文生成メソッド
     //
     // @arg
     // double dStartTime : 開始時刻
     // double dEndTime : 終了時刻
     // MathOperand* pTimeVariable : 時間変数インスタンス
     // MathOperand* pDeltaOperand : デルタ変数インスタンス
     //
     // ========================================================
     public SyntaxControl createSyntaxTimeLoop(double dStartTime,
             double dEndTime, MathOperand pTimeVariable,
             MathOperand pDeltaOperand) throws MathException {
         /* 時間を文字列化 */
         String strStartTime = StringUtil.doubleToString(dStartTime);
         String strEndTime = StringUtil.doubleToString(dEndTime);
 
         /* ループ条件式生成 */
         Math_leq pMathLeq = (Math_leq) MathFactory
                 .createOperator(eMathOperator.MOP_LEQ);
 
         Math_cn pLoopEnd = (Math_cn) MathFactory.createOperand(
                 eMathOperand.MOPD_CN, strEndTime);
 
         pMathLeq.addFactor(pTimeVariable);
         pMathLeq.addFactor(pLoopEnd);
         MathExpression pConditionExp = new MathExpression(pMathLeq);
 
         /* ループ中初期化式生成 */
         Math_assign pMathAssign = (Math_assign) MathFactory
                 .createOperator(eMathOperator.MOP_ASSIGN);
         Math_cn pLoopInit = (Math_cn) MathFactory.createOperand(
                 eMathOperand.MOPD_CN, strStartTime);
         pMathAssign.addFactor(pTimeVariable);
         pMathAssign.addFactor(pLoopInit);
         MathExpression pInitExp = new MathExpression(pMathAssign);
 
         /* ループ再初期化式生成 */
         Math_assign pMathAssign2 = (Math_assign) MathFactory
                 .createOperator(eMathOperator.MOP_ASSIGN);
         Math_plus pMathPlus = (Math_plus) MathFactory
                 .createOperator(eMathOperator.MOP_PLUS);
         pMathPlus.addFactor(pTimeVariable);
         pMathPlus.addFactor(pDeltaOperand);
         pMathAssign2.addFactor(pTimeVariable);
         pMathAssign2.addFactor(pMathPlus);
         MathExpression pReInitExp = new MathExpression(pMathAssign2);
 
         /* ループ条件生成 */
         SyntaxCondition pSynLoopCond = new SyntaxCondition(pConditionExp);
         pSynLoopCond.setInitExpression(pInitExp, pReInitExp);
 
         /* ループを生成 */
         SyntaxControl pSynFor = new SyntaxControl(eControlKind.CTRL_FOR,
                 pSynLoopCond);
 
         /* ループ構文を返す */
         return pSynFor;
     }
 
     // ========================================================
     // createZeroMemset
     // 0初期化用memset関数呼び出し生成メソッド
     //
     // @arg
     // Math_ci* pDstVar : コピー先変数インスタンス
     // MathFactor* pDataNumFactor : データ数を表す数式ファクタインスタンス
     //
     // @return
     // 生成した関数呼び出しインスタンス : SyntaxCallFunction*
     //
     // ========================================================
     public SyntaxCallFunction createZeroMemset(Math_ci pDstVar,
             MathFactor pDataNumFactor) throws MathException {
         /* 関数呼び出しインスタンス生成 */
         SyntaxCallFunction pSynMemsetCall = new SyntaxCallFunction(
                 PROG_FUNC_STR_MEMSET);
 
         /* 第一引数追加 */
         pSynMemsetCall.addArgFactor(pDstVar);
 
         /* 第二引数追加 */
         Math_cn pZeroConst = (Math_cn) MathFactory.createOperand(
                 eMathOperand.MOPD_CN, "0");
         // Syntax構文群が完成するまでの暫定処置
         pSynMemsetCall.addArgFactor(pZeroConst);
 
         /* 第三引数の構築 */
         Math_ci pSizeofVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, "sizeof( double )");
         // Syntax構文群が完成するまでの暫定処置
         Math_times pMathTimes1 = (Math_times) MathFactory
                 .createOperator(eMathOperator.MOP_TIMES);
 
         pMathTimes1.addFactor(pSizeofVar);
         pMathTimes1.addFactor(pDataNumFactor);
 
         /* 第三引数の追加 */
         pSynMemsetCall.addArgFactor(pMathTimes1);
 
         /* 関数呼び出しインスタンスを戻す */
         return pSynMemsetCall;
     }
 
     // ========================================================
     // createMalloc
     // malloc関数呼び出し生成メソッド
     //
     // @arg
     // Math_ci* pDstVar : メモリ確保先変数インスタンス
     // MathFactor* pDataNumFactor : データ数を表す数式ファクタインスタンス
     //
     // @return
     // 生成した数式構文インスタンス : SyntaxExpression*
     //
     // ========================================================
     public SyntaxExpression createMalloc(Math_ci pDstVar,
             MathFactor pDataNumFactor) throws MathException {
         /* 関数呼び出しインスタンス生成 */
         SyntaxCallFunction pSynMallocCall = new SyntaxCallFunction(
                 PROG_FUNC_STR_MALLOC);
 
         /* 引数の構築 */
         Math_ci pSizeofVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, "sizeof( double )");
         // Syntax構文群が完成するまでの暫定処置
         Math_times pMathTimes1 = (Math_times) MathFactory
                 .createOperator(eMathOperator.MOP_TIMES);
 
         pMathTimes1.addFactor(pSizeofVar);
         pMathTimes1.addFactor(pDataNumFactor);
 
         /* 引数の追加 */
         pSynMallocCall.addArgFactor(pMathTimes1);
 
         /* 戻り値をキャスト */
         SyntaxDataType pSynPDoubleType = new SyntaxDataType(
                 eDataType.DT_DOUBLE, 1);
         pSynMallocCall.addCastDataType(pSynPDoubleType);
 
         /* 代入式を生成 */
         Math_assign pMathAssign = (Math_assign) MathFactory
                 .createOperator(eMathOperator.MOP_ASSIGN);
         Math_ci pMathTmpVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, pSynMallocCall.toLegalString());
         pMathAssign.addFactor(pDstVar);
         pMathAssign.addFactor(pMathTmpVar);
         MathExpression pNewExpression = new MathExpression(pMathAssign);
         SyntaxExpression pNewSynExpression = new SyntaxExpression(
                 pNewExpression);
 
         /* 関数呼び出しインスタンスを戻す */
         return pNewSynExpression;
     }
 
     public void createCharMalloc(SyntaxFunction pSynFunc, Math_ci pDstVar,
             MathFactor pDataNumFactor) throws MathException {
         /* 関数呼び出しインスタンス生成 */
         SyntaxCallFunction pSynMallocCall = new SyntaxCallFunction(
                 PROG_FUNC_STR_MALLOC);
 
         /* 引数の構築 */
         Math_ci pSizeofVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, "sizeof( char )");
 
         Math_times pMallocArg = createTimes(pSizeofVar, pDataNumFactor);
 
         /* 引数の追加 */
         pSynMallocCall.addArgFactor(pMallocArg);
 
         /* 戻り値をキャスト */
         SyntaxDataType pSynPCharType = new SyntaxDataType(
                 eDataType.DT_CHAR, 1);
         pSynMallocCall.addCastDataType(pSynPCharType);
         Math_ci pMathTmpVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, pSynMallocCall.toLegalStringWithNoSemicolon());
 
         /* 代入式を生成 */
         createAssign(pSynFunc, pDstVar, pMathTmpVar);
 
     }
 
     // ========================================================
     // createFree
     // free関数呼び出し生成メソッド
     //
     // @arg
     // Math_ci* pDstVar : メモリ解放対象変数インスタンス
     //
     // @return
     // 生成した関数呼び出し構文インスタンス : SyntaxCallFunction*
     //
     // ========================================================
     public SyntaxCallFunction createFree(Math_ci pDstVar) {
         /* 関数呼び出しインスタンス生成 */
         SyntaxCallFunction pSynFreeCall = new SyntaxCallFunction(
                 PROG_FUNC_STR_FREE);
 
         /* 第一引数追加 */
         pSynFreeCall.addArgFactor(pDstVar);
 
         /* 生成したインスタンスを返す */
         return pSynFreeCall;
     }
 
     /**
      * create int variable
      *
      * @param SyntaxFunction pSynMainFunc
      * @param String variableName
      * @param int pNum
      * @param int variableNum
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_ci createIntVal(SyntaxFunction pSynMainFunc,
             String variableName, int pNum, int variableNum, boolean initZero)
             throws MathException {
         Math_ci pMpiIndexVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, variableName);
 
         /* 変数を関数に宣言追加 */
         {
             SyntaxDataType pSynTypeInt = new SyntaxDataType(eDataType.DT_INT,
                     pNum);
             SyntaxDeclaration pDecVar = new SyntaxDeclaration(pSynTypeInt,
                     pMpiIndexVar);
 
             if (variableNum != 0 || (variableNum == 0 && initZero == true)) {
                 /* 初期化式の生成 */
                 Math_cn pConstDeltaVal = (Math_cn) MathFactory.createOperand(
                         eMathOperand.MOPD_CN, String.valueOf(variableNum));
                 MathExpression pInitExpression = new MathExpression(
                         pConstDeltaVal);
 
                 /* 初期化式の追加 */
                 pDecVar.addInitExpression(pInitExpression);
 
             }
 
             pSynMainFunc.addDeclaration(pDecVar);
         }
         return pMpiIndexVar;
     }
 
     /**
      * create double variable
      *
      * @param SyntaxFunction pSynMainFunc
      * @param String variableName
      * @param int pNum
      * @param int variableNum
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_ci createDoubleVal(SyntaxFunction pSynMainFunc,
             String variableName, int pNum, double variableNum, boolean initZero)
             throws MathException {
         Math_ci pMpiIndexVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, variableName);
 
         /* 変数を関数に宣言追加 */
         {
             SyntaxDataType pSynTypeInt = new SyntaxDataType(
                     eDataType.DT_DOUBLE, pNum);
             SyntaxDeclaration pDecVar = new SyntaxDeclaration(pSynTypeInt,
                     pMpiIndexVar);
 
             if (variableNum != 0 || (variableNum == 0 && initZero == true)) {
                 /* 初期化式の生成 */
                 Math_cn pConstDeltaVal = (Math_cn) MathFactory.createOperand(
                         eMathOperand.MOPD_CN,
                         StringUtil.doubleToString(variableNum));
                 MathExpression pInitExpression = new MathExpression(
                         pConstDeltaVal);
 
                 /* 初期化式の追加 */
                 pDecVar.addInitExpression(pInitExpression);
 
             }
 
             pSynMainFunc.addDeclaration(pDecVar);
         }
         return pMpiIndexVar;
     }
 
     /**
      * create char variable
      *
      * @param SyntaxFunction pSynMainFunc
      * @param String variableName
      * @param int pNum
      * @param int variableNum
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_ci createCharVal(SyntaxFunction pSynFunc,
             String variableName, int pointerNum, String defaultString)
             throws MathException {
         Math_ci pCharVar = (Math_ci) MathFactory.createOperand(
                 eMathOperand.MOPD_CI, variableName);
 
         /* 変数を関数に宣言追加 */
         {
             SyntaxDataType pSynTypeInt = new SyntaxDataType(
                     eDataType.DT_CHAR, pointerNum);
             SyntaxDeclaration pDecVar = new SyntaxDeclaration(pSynTypeInt,
                     pCharVar);
 
             if ( defaultString != null ) {
                 /* 初期化式の生成 */
                 Math_cn pConstDeltaVal = (Math_cn) MathFactory.createOperand(
                         eMathOperand.MOPD_CN,
                         defaultString);
                 MathExpression pInitExpression = new MathExpression(
                         pConstDeltaVal);
 
                 /* 初期化式の追加 */
                 pDecVar.addInitExpression(pInitExpression);
 
             }
 
             pSynFunc.addDeclaration(pDecVar);
         }
         return pCharVar;
     }
 
     /**
      * create assign and add func
      *
      * @param pSynFunc
      * @param pSynCont
      * @param pDstVar
      * @param pInsVar
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public void createAssign(SyntaxFunction pSynFunc,
             Math_ci pDstVar, MathFactor pInsVar) throws MathException {
         /* 代入式を生成 */
         Math_assign pMathAssign = (Math_assign) MathFactory.createOperator(eMathOperator.MOP_ASSIGN);
 
         pMathAssign.addFactor(pDstVar);
         pMathAssign.addFactor(pInsVar);
         MathExpression pNewExpression = new MathExpression(pMathAssign);
         SyntaxExpression pNewSynExpression = new SyntaxExpression(pNewExpression);
 
         // add func
         pSynFunc.addStatement(pNewSynExpression);
     }
 
     /**
      * create assign and add control
      *
      * @param pSynFunc
      * @param pSynCont
      * @param pDstVar
      * @param pInsVar
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public void createAssign(SyntaxControl pSynCont,
             Math_ci pDstVar, MathFactor pInsVar) throws MathException {
         /* 代入式を生成 */
         Math_assign pMathAssign = (Math_assign) MathFactory.createOperator(eMathOperator.MOP_ASSIGN);
 
         pMathAssign.addFactor(pDstVar);
         pMathAssign.addFactor(pInsVar);
         MathExpression pNewExpression = new MathExpression(pMathAssign);
         SyntaxExpression pNewSynExpression = new SyntaxExpression(pNewExpression);
 
         // add control
         pSynCont.addStatement(pNewSynExpression);
 
     }
 
     /**
      * create pFirstVar * pSecondVar
      * @param pFirstVar
      * @param pSecondVar
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_times createTimes(MathFactor pFirstVar, MathFactor pSecondVar)
             throws MathException {
 
         Math_times pMathTimes =
                 (Math_times)MathFactory.createOperator(eMathOperator.MOP_TIMES);
 
         pMathTimes.addFactor(pFirstVar);
         pMathTimes.addFactor(pSecondVar);
 
         return pMathTimes;
     }
 
     /**
      * create pFirstVar / pSecondVar
      * @param pFirstVar
      * @param pSecondVar
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_divide createDivide(MathFactor pFirstVar, MathFactor pSecondVar)
             throws MathException {
 
         Math_divide pMathDiv =
                 (Math_divide)MathFactory.createOperator(eMathOperator.MOP_DIVIDE);
 
         pMathDiv.addFactor(pFirstVar);
         pMathDiv.addFactor(pSecondVar);
 
         return pMathDiv;
     }
 
     /**
      * create pFirstVar - pSecondVar
      * @param pFirstVar
      * @param pSecondVar
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_minus createMinus(MathFactor pFirstVar, MathFactor pSecondVar)
             throws MathException {
 
         Math_minus pMathMin =
                 (Math_minus)MathFactory.createOperator(eMathOperator.MOP_MINUS);
 
         pMathMin.addFactor(pFirstVar);
         pMathMin.addFactor(pSecondVar);
 
         return pMathMin;
     }
 
     /**
      * create pFirstVar + pSecondVar
      * @param pFirstVar
      * @param pSecondVar
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_plus createPlus(MathFactor pFirstVar, MathFactor pSecondVar)
             throws MathException {
 
         Math_plus pMathPul =
                 (Math_plus)MathFactory.createOperator(eMathOperator.MOP_PLUS);
 
         pMathPul.addFactor(pFirstVar);
         pMathPul.addFactor(pSecondVar);
 
         return pMathPul;
     }
 
     /**
      * create pFirstVar % pSecondVar
      * @param pFirstVar
      * @param pSecondVar
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_remainder createRemainder(MathFactor pFirstVar, MathFactor pSecondVar)
             throws MathException {
 
         Math_remainder pMathRemainder =
                 (Math_remainder)MathFactory.createOperator(eMathOperator.MOP_REMAINDER);
 
         pMathRemainder.addFactor(pFirstVar);
         pMathRemainder.addFactor(pSecondVar);
 
         return pMathRemainder;
     }
 
     /**
      * create pFirstVar ++
      * @param pFirstVar
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public Math_inc createInc(MathFactor pIncVar)
             throws MathException {
 
         Math_inc pMathInc =
                 (Math_inc)MathFactory.createOperator(eMathOperator.MOP_INC);
 
         pMathInc.addFactor(pIncVar);
 
         return pMathInc;
     }
 
     /**
      * create pFirstVar ++
      * @param pFirstVar
      * @return
      * @throws MathException
      *
      * @author Yuu Shigetani
      */
     public void createIncExp(SyntaxControl pSynControl, MathFactor pIncVar)
             throws MathException {
 
         MathExpression pNewExpression = new MathExpression(createInc(pIncVar));
         SyntaxExpression pNewSynExpression = new SyntaxExpression(pNewExpression);
         pSynControl.addStatement(pNewSynExpression);
 
     }
 
 }
 
