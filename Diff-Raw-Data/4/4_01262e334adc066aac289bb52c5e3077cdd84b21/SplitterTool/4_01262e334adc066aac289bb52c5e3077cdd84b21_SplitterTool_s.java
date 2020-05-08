 /**
  * SplitterTool.java
  * created: 29.11.2008
  * (c) 2008 by <a href="http://Wolschon.biz">Wolschon Softwaredesign und Beratung</a>
  * This file is part of jGnucashLib-private by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
  * You can purchase support for a sensible hourly rate or
  * a commercial license of this file (unless modified by others) by contacting him directly.
  *
  * ***********************************
  * Editing this file:
  *  -For consistent code-quality this file should be checked with the
  *   checkstyle-ruleset enclosed in this project.
  *  -After the design of this file has settled it should get it's own
  *   JUnit-Test that shall be executed regularly. It is best to write
  *   the test-case BEFORE writing this class and to run it on every build
  *   as a regression-test.
  */
 package biz.wolschon.finance.jgnucash.splitter;
 
 
 //automatically created logger for debug and error -output
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.logging.Logger;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 import javax.xml.bind.JAXBException;
 
 import biz.wolschon.fileformats.gnucash.GnucashAccount;
 import biz.wolschon.fileformats.gnucash.GnucashTransaction;
 import biz.wolschon.fileformats.gnucash.GnucashWritableAccount;
 import biz.wolschon.fileformats.gnucash.GnucashWritableFile;
 import biz.wolschon.fileformats.gnucash.GnucashWritableTransaction;
 import biz.wolschon.fileformats.gnucash.GnucashWritableTransactionSplit;
 import biz.wolschon.fileformats.gnucash.jwsdpimpl.GnucashFileWritingImpl;
 import biz.wolschon.finance.jgnucash.plugin.ToolPlugin;
 import biz.wolschon.numbers.FixedPointNumber;
 
 
 /**
  * (c) 2008 by <a href="http://Wolschon.biz>Wolschon Softwaredesign und Beratung</a>.<br/>
  * Project: jGnucashLib-private<br/>
  * SplitterTool.java<br/>
  * created: 29.11.2008<br/>
  *<br/><br/>
  * <b>Split a gnucash-file at a given data, arching old transactions into another file.</b>
  * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
  */
 public class SplitterTool implements ToolPlugin {
 
     /**
      * Automatically created logger for debug and error-output.
      */
     private static final Logger LOG = Logger.getLogger(SplitterTool.class
             .getName());
 
 
     /**
      * Just an overridden ToString to return this classe's name
      * and hashCode.
      * @return className and hashCode
      */
     @Override
     public String toString() {
         return "SplitterTool@" + hashCode();
     }
 
     /**
      * ${@inheritDoc}.
      */
     @Override
     public String runTool(final GnucashWritableFile aWritableModel,
                             final GnucashWritableAccount aCurrentAccount) throws Exception {
 
         JFileChooser fc = new JFileChooser();
         fc.setMultiSelectionEnabled(true);
         fc.setFileFilter(new FileFilter() {
             @Override
             public boolean accept(final File f) {
                 if (f.isDirectory()) {
                     return true;
                 }
                 return f.isFile();
             }
 
             @Override
             public String getDescription() {
                 return "OSCommerce-XML-file";
             }
         });
         int returnCode = fc.showSaveDialog(null);
         if (returnCode != JFileChooser.APPROVE_OPTION) {
             return "";
         }
 
         File newFile = fc.getSelectedFile();
         return runTool(aWritableModel, aCurrentAccount, newFile);
     }
 
     /**
      * @param aWritableModel
      * @param newFile
      * @return
      * @throws IOException
      * @throws JAXBException
      */
     private String runTool(final GnucashWritableFile aWritableModel,
                            final GnucashWritableAccount aCurrentAccount,
                            final File newFile) throws IOException, JAXBException {
         if (newFile.exists()) {
             JOptionPane.showMessageDialog(null, "File must not yet exist.");
             return "";
         }
         DateFormat localDateFormat = DateFormat.getDateInstance();
         GregorianCalendar lastFirstJanuary = new GregorianCalendar();
         lastFirstJanuary.set(Calendar.MONTH, Calendar.JANUARY);
         lastFirstJanuary.set(Calendar.DAY_OF_YEAR, 0);
         //lastFirstJanuary.add(Calendar.DAY_OF_YEAR, -1);
         Date splitDate = null;
         while (true) {
             try {
                 String inputDate = JOptionPane.showInputDialog(null, "Please enter the date to split at", localDateFormat.format(lastFirstJanuary.getTime()));
                 splitDate = localDateFormat.parse(inputDate);
                 if (splitDate != null) {
                     break;
                 }
             } catch (ParseException x) {
                 JOptionPane.showMessageDialog(null, "unparsable date.");
             }
         }
         // we cannot clone in memory, thus create a 1:1-copy first
         // and then remove transactions.
         File tempfile = File.createTempFile("jgnucasheditor_splittool", ".xml.gz");
         tempfile.deleteOnExit();
         tempfile.delete();
         aWritableModel.writeFile(tempfile);
         GnucashWritableFile newModel = new GnucashFileWritingImpl(tempfile);
         tempfile.delete();
         ArrayList<GnucashTransaction> allTransaction = new ArrayList<GnucashTransaction>(newModel.getTransactions());
         for (GnucashTransaction transaction : allTransaction) {
             if (transaction.getDatePosted().after(splitDate)) {
                 newModel.removeTransaction((GnucashWritableTransaction) transaction);
             }
         }
         newModel.writeFile(newFile);
 
         // get Balance for all accounts in newModel
         // and insert a split in the current model to get these balances
         GnucashWritableTransaction balanceTransaction = aWritableModel.createWritableTransaction();
         balanceTransaction.setDatePosted(splitDate);
         balanceTransaction.setDescription("Balance as of " + DateFormat.getDateInstance().format(splitDate));
         for (GnucashAccount newAccount : newModel.getAccounts()) {
             FixedPointNumber balance = newAccount.getBalance();
             GnucashAccount account = aWritableModel.getAccountByID(newAccount.getId());
             GnucashWritableTransactionSplit split = balanceTransaction.createWritingSplit(account);
             split.setQuantity(balance);
         }
         // balance the transaction
         FixedPointNumber balance = balanceTransaction.getNegatedBalance();
         GnucashWritableAccount balanceAccount = aWritableModel.getAccountByName("Ausgleichskonto");
         if (balanceAccount == null) {
             balanceAccount = aWritableModel.createWritableAccount();
             balanceAccount.setName("Ausgleichskonto");
         }
         GnucashWritableTransactionSplit split = balanceTransaction.createWritingSplit(balanceAccount);
         split.setValue(balance);
 
         // remove all old Transactions from the existing file
         for (GnucashTransaction transaction : allTransaction) {
             GnucashTransaction removeMe = aWritableModel.getTransactionByID(transaction.getId());
            aWritableModel.removeTransaction((GnucashWritableTransaction) removeMe);
         }
 
         return "";
     }
 
     /**
      * Run the tool manually.
      * @param args the arguments
      */
     public static void main(final String[] args) {
         final int argscount = 3;
         if (args.length != argscount) {
             System.out.println("usage: SplitterTool (infile) (outfile-older) (outfile-newer)\n");
             System.out.println("       This tool will ask graphically for a date and then split infile into\n");
             System.out.println("       This 2 new files that contain all transactions that are newer or older/as old as the date\n");
             return;
         }
         try {
             File file = new File(args[0]);
             File archiveFile = new File(args[1]);
             File newFile = new File(args[2]);
             if (archiveFile.exists()) {
                 archiveFile.delete();
             }
             if (newFile.exists()) {
                 newFile.delete();
             }
             GnucashWritableFile testdata = new GnucashFileWritingImpl(file);
             SplitterTool subject = new SplitterTool();
             subject.runTool(testdata, null, archiveFile);
             testdata.writeFile(newFile);
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
 
 
