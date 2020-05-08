 package Application;
 
 import UI.CommandLine;
 import UI.UIException;
 
 public class Interaction {
     private final CommandLine commandLine = new CommandLine();
     private Controller controller = null;
 
     public void run() {
         initializeControllerUntilSucceed();
         handleCommandUntilQuit();
     }
 
     private void initializeControllerUntilSucceed() {
        while (null == controller && initializeController()) ;
         while (!initializeFund()) ;
     }
 
     private boolean initializeController() {
         try {
             commandLine.outputInNewline(
                     "请选择2~4位不重复玩家，输入编号即可。(1.钱夫人; 2.阿土伯; 3.孙小美; 4.金贝贝):");
             controller = new Controller(commandLine.waitForInput());
             return true;
         } catch (UIException e) {
             commandLine.outputInNewline(e.toString());
            return e.isNeedRetry();
         }
     }
 
     private boolean initializeFund() {
         try {
             commandLine.outputInNewline("请输入玩家初始资金，范围1000～50000（默认10000）");
             controller.initialFund(commandLine.waitForInput());
             return true;
         } catch (UIException e) {
             commandLine.outputInNewline(e.toString());
             return !e.isNeedRetry();
         }
     }
 
     private void handleCommandUntilQuit() {
         while (handleCommand()) ;
     }
 
     private boolean handleCommand() {
         try {
             commandLine.output(controller.getPromptMessageForCurrentPlayer());
             controller.handleCommand(commandLine.waitForInput());
             return true;
         } catch (UIException e) {
             commandLine.outputInNewline(e.toString());
             return e.isNeedRetry();
         }
     }
 }
