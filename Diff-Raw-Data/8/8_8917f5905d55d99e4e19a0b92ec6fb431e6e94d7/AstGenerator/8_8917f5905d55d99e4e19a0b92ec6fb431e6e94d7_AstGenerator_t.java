 package cn.shenyanchao.ut;
 
 import cn.shenyanchao.ut.builder.CompilationUnitBuilder;
 import cn.shenyanchao.ut.command.ExistTestCommand;
 import cn.shenyanchao.ut.command.NewTestCommand;
 import cn.shenyanchao.ut.command.invoker.CommandInvoker;
 import cn.shenyanchao.ut.common.Consts;
 import cn.shenyanchao.ut.filter.JavaFileFilter;
 import cn.shenyanchao.ut.generator.TestWriter;
 import cn.shenyanchao.ut.receiver.ExistTestReceiver;
 import cn.shenyanchao.ut.receiver.NewTestReceiver;
 import cn.shenyanchao.ut.utils.FileChecker;
 import cn.shenyanchao.ut.utils.JavaParserFactory;
 import cn.shenyanchao.ut.utils.JavaParserUtils;
 import japa.parser.ast.CompilationUnit;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 import java.io.File;
 import java.util.Iterator;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author shenyanchao
  *         Date:  6/13/13
  *         Time:  5:13 PM
  */
 @Mojo(name = "source2test", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
 public class AstGenerator extends AbstractMojo {
 
     @Parameter(defaultValue = Consts.DEFAULT_ENCODE, property = "sourceEncode", required = false)
     private String sourceEncode;
 
     @Parameter(defaultValue = "${project.build.sourceDirectory}", property = "sourceDir", required = false)
     private String sourceDir;
 
     @Parameter(defaultValue = "${project.build.testSourceDirectory}", property = "testDir", required = false)
     private String testDir;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
 
         getLog().info(sourceDir);
         getLog().info(testDir);
 
         File sourceDirectory = new File(sourceDir);
         makeDirIfNotExist(sourceDirectory);
         File testDirectory = new File(testDir);
         makeDirIfNotExist(testDirectory);
 
         Iterator<File> fileItr = FileUtils.iterateFiles(sourceDirectory, new JavaFileFilter(), TrueFileFilter.INSTANCE);
         while (fileItr.hasNext()) {
             File javaFile = fileItr.next();
             getLog().info("start process file:" + javaFile.getAbsolutePath());
             //process java file
             convertJavaFile2Test(javaFile);
         }
     }
 
     /**
      * if dir not exist,create it
      *
      * @param dir
      */
     private void makeDirIfNotExist(File dir) {
         if (!dir.isDirectory()) {
             getLog().error(dir.getAbsolutePath() + "is not a directory!");
         }
         if (!dir.exists()) {
             boolean success = false;
             while (!success) {
                 success = dir.mkdirs();
             }
         }
     }
 
 
     /**
      * @param javaFile
      */
     private void convertJavaFile2Test(File javaFile) {
 
         CompilationUnit sourceCU = JavaParserFactory.getCompilationUnit(javaFile, sourceEncode);
 
         CompilationUnitBuilder compilationUnitBuilder = null;
         String testJavaFileName = JavaParserUtils.findTestJavaFileName(sourceCU, javaFile, testDir);
         boolean testExist = FileChecker.isTestJavaClassExist(new File(testJavaFileName));
         if (!testExist) {
             CommandInvoker invoker = new CommandInvoker(new NewTestCommand(new NewTestReceiver(sourceCU, javaFile)));
             compilationUnitBuilder = invoker.action();
         } else if (testExist) {
             CompilationUnit testCU = JavaParserFactory.getCompilationUnit(new File(testJavaFileName), sourceEncode);
             CommandInvoker invoker = new CommandInvoker(new ExistTestCommand(new ExistTestReceiver(sourceCU, javaFile,
                     testCU)));
             compilationUnitBuilder = invoker.action();
         }
 
        if (null != compilationUnitBuilder){
            CompilationUnit testCU = compilationUnitBuilder.build();
            //写入测试代码文件
            TestWriter.writeJavaTest(testJavaFileName, testCU.toString(), sourceEncode);
        }
     }
 
 }
