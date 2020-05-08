 package jp.ac.osaka_u.ist.sel.metricstool.main;
 
 
 import java.util.StringTokenizer;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.UnavailableLanguageException;
 
 
 /**
  * 
  * @author higo
  * 
  * s̈i[邽߂̃NX
  * 
  */
 public class Settings {
 
     private static Settings INSTANCE = null;
 
     public static Settings getInstance() {
         if (null == INSTANCE) {
             INSTANCE = new Settings();
         }
         return INSTANCE;
     }
 
     private Settings() {
         this.verbose = false;
         this.targetDirectory = null;
         this.listFile = null;
         this.language = null;
         this.metrics = null;
         this.fileMetricsFile = null;
         this.classMetricsFile = null;
         this.methodMetricsFile = null;
         this.fieldMetricsFile = null;
     }
 
     /**
      * 璷o͂sǂԂ
      * 
      * @return sꍇ true, sȂꍇ false
      */
     public boolean isVerbose() {
         return this.verbose;
     }
 
     public void setVerbose(final boolean verbose) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         this.verbose = verbose;
     }
 
     /**
      * 
      * @return ͑ΏۃfBNg
      * 
      * ͑ΏۃfBNgԂD
      * 
      */
     public String getTargetDirectory() {
         return this.targetDirectory;
     }
 
     public void setTargetDirectory(final String targetDirectory) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == targetDirectory) {
             throw new IllegalArgumentException();
         }
         this.targetDirectory = targetDirectory;
     }
 
     /**
      * ͑Ώۃt@C̋LqԂ
      * 
      * @return ͑Ώۃt@C̋Lq
      * @throws UnavailableLanguageException ps\Ȍꂪw肳ĂꍇɃX[
      */
     public LANGUAGE getLanguage() throws UnavailableLanguageException {
         assert null != this.language : "\"language\" is not set";
         return this.language;
     }
 
     public void setLanguage(final String language) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == language) {
             throw new IllegalArgumentException();
         }
 
         if (language.equalsIgnoreCase("java") || language.equalsIgnoreCase("java15")) {
             this.language = LANGUAGE.JAVA15;
         } else if (language.equalsIgnoreCase("java14")) {
             this.language = LANGUAGE.JAVA14;
         } else if (language.equalsIgnoreCase("java13")) {
             this.language = LANGUAGE.JAVA13;
             // }else if (language.equalsIgnoreCase("cpp")) {
             // return LANGUAGE.C_PLUS_PLUS;
             // }else if (language.equalsIgnoreCase("csharp")) {
             // return LANGUAGE.C_SHARP
         } else if (language.equalsIgnoreCase("csharp")) {
             this.language = LANGUAGE.CSHARP;
         } else {
             throw new UnavailableLanguageException("\"" + language
                     + "\" is not an available programming language!");
         }
     }
 
     /**
      * 
      * @return ͑Ώۃt@C̃pXLqĂt@C
      * 
      * ͑Ώۃt@C̃pXLqĂt@C̃pXԂ
      * 
      */
     public String getListFile() {
         return listFile;
     }
 
     public void setListFile(final String listFile) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == listFile) {
             throw new IllegalArgumentException();
         }
         this.listFile = listFile;
     }
 
     /**
      * 
      * @return v郁gNX
      * 
      * v郁gNXꗗԂ
      * 
      */
     public String[] getMetrics() {
         return this.metrics;
     }
 
     public void setMetrics(final String metrics) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == metrics) {
             throw new IllegalArgumentException();
         }
 
         final StringTokenizer tokenizer = new StringTokenizer(metrics, ",", false);
         this.metrics = new String[tokenizer.countTokens()];
         for (int i = 0; i < this.metrics.length; i++) {
             this.metrics[i] = tokenizer.nextToken();
         }
     }
 
     /**
      * 
      * @return t@C^CṽgNXo͂t@C
      * 
      * t@C^CṽgNXo͂t@C̃pXԂ
      * 
      */
     public String getFileMetricsFile() {
        return this.fileMetricsFile;
     }
 
     public void setFileMetricsFile(final String fileMetricsFile) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == fileMetricsFile) {
             throw new IllegalArgumentException();
         }
        this.fileMetricsFile = fileMetricsFile;
     }
 
     /**
      * 
      * @return NX^CṽgNXo͂t@C
      * 
      * NX^CṽgNXo͂t@C̃pXԂ
      * 
      */
     public String getClassMetricsFile() {
         return classMetricsFile;
     }
 
     public void setClassMetricsFile(final String classMetricsFile) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classMetricsFile) {
             throw new IllegalArgumentException();
         }
         this.classMetricsFile = classMetricsFile;
     }
 
     /**
      * 
      * @return \bh^CṽgNXo͂t@C
      * 
      * \bh^CṽgNXo͂t@C̃pXԂ
      * 
      */
     public String getMethodMetricsFile() {
         return methodMetricsFile;
     }
 
     public void setMethodMetricsFile(final String methodMetricsFile) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == methodMetricsFile) {
             throw new IllegalArgumentException();
         }
         this.methodMetricsFile = methodMetricsFile;
     }
 
     /**
      * 
      * @return tB[h^CṽgNXo͂t@C
      */
     public String getFieldMetricsFile() {
         return fieldMetricsFile;
     }
 
     public void setFieldMetricsFile(final String fieldMetricsFile) {
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == fieldMetricsFile) {
             throw new IllegalArgumentException();
         }
        this.fieldMetricsFile = fieldMetricsFile;
     }
 
     /**
      * 璷o̓[hǂL^邽߂̕ϐ
      */
     private boolean verbose;
 
     /**
      * ͑ΏۃfBNgL^邽߂̕ϐ
      */
     private String targetDirectory;
 
     /**
      * ͑Ώۃt@C̃pXLqt@C̃pXL^邽߂̕ϐ
      */
     private String listFile;
 
     /**
      * ͑Ώۃt@C̋LqL^邽߂̕ϐ
      */
     private LANGUAGE language;
 
     /**
      * v郁gNXL^邽߂̕ϐ
      */
     private String[] metrics;
 
     /**
      * t@C^CṽgNXo͂t@C̃pXL^邽߂̕ϐ
      */
     private String fileMetricsFile;
 
     /**
      * NX^CṽgNXo͂t@C̃pXL^邽߂̕ϐ
      */
     private String classMetricsFile;
 
     /**
      * \bh^CṽgNXo͂t@C̃pXL^邽߂̕ϐ
      */
     private String methodMetricsFile;
 
     /**
      * tB[h^CṽgNXo͂t@C̃pXL^邽߂̕ϐ
      */
     private String fieldMetricsFile;
 }
