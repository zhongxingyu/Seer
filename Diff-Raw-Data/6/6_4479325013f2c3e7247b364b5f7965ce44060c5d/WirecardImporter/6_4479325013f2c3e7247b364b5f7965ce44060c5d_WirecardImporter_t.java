 /**
  *
  */
 package biz.wolschon.finance.jgnucash.wirecard.importer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.xml.bind.JAXBException;
 
 import biz.wolschon.fileformats.gnucash.GnucashWritableAccount;
 import biz.wolschon.fileformats.gnucash.GnucashWritableFile;
 import biz.wolschon.fileformats.gnucash.GnucashWritableTransaction;
 import biz.wolschon.fileformats.gnucash.GnucashWritableTransactionSplit;
 import biz.wolschon.finance.jgnucash.plugin.PluginConfigHelper;
 import biz.wolschon.numbers.FixedPointNumber;
 
 /**
  * This class parses Wirecard-reports (clearing and account-statement)
  * and inserts transactions for them.<br/>
  */
 public class WirecardImporter {
 
 
     /**
      * Automatically created logger for debug and error-output.
      */
     private static final Logger LOG = Logger.getLogger(WirecardImporter.class
             .getName());
 
     /**
      * The date-format used in the pago-files.
      */
     private static final SimpleDateFormat UOS_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
 
     /**
      * Buffersize when reading streams.
      */
     private static final int BUFFERSIZE = 255;
 
     /**
      * The book we operate on.
      */
     private GnucashWritableFile myBook;
 
     /**
      * @param aBook The book we operate on
      */
     public WirecardImporter(final GnucashWritableFile aBook) {
         myBook = aBook;
     }
 
     /**
      * @param aBook The book we operate on
      * @param pdfFile can be a PDF-file or a ZIP-file containing pdfs.
      * @throws IOException errors reading or writing
      * @throws ParseException errors parsing the pdf-file
      * @throws JAXBException errors reading or writing the xml-datastructures of Gnucash
      */
     public static void importFile(final GnucashWritableFile aBook, final File pdfFile) throws IOException, ParseException, JAXBException {
         if (pdfFile.getName().toLowerCase().endsWith(".zip")) {
             InputStream in = new FileInputStream(pdfFile);
             importZIPStream(aBook, in, false);
             return;
         }
         // for debugging. Work without pdftotext
         if (pdfFile.getName().toLowerCase().endsWith(".txt")) {
             Reader reader = new FileReader(pdfFile);
             BufferedReader buffer = new BufferedReader(reader);
             if (pdfFile.getName().contains("SettlementNote")) {
                 importClearingStatement(aBook, buffer);
             } else if (pdfFile.getName().contains("ReserveNote")) {
                 importReserveStatement(aBook, buffer);
             } else {
                 importAccountStatement(aBook, buffer);
             }
             return;
         }
 
         Process proc = Runtime.getRuntime()
                 .exec(new String[] {"/usr/bin/pdftotext", pdfFile.getAbsolutePath(), "-"});
         Reader reader = new InputStreamReader(proc.getInputStream());
         BufferedReader buffer = new BufferedReader(reader);
 
         if (pdfFile.getName().toLowerCase().endsWith(".p7s")) {
             return; //skip cryptographic signatures
         }
         if (pdfFile.getName().contains("SettlementNote")) {
             importClearingStatement(aBook, buffer);
         } else if (pdfFile.getName().contains("ReserveNote")) {
             importReserveStatement(aBook, buffer);
         } else {
             importAccountStatement(aBook, buffer);
         }
     }
     /**
      * @param aBook the book to import into
      * @param aBuffer where to read from
      * @throws IOException if we annot read parts
      * @throws JAXBException issues with the XML-backend
      * @throws ParseException if we cannot parse parts
      */
     private static void importReserveStatement(final GnucashWritableFile aBook,
                                                final BufferedReader aBuffer) throws IOException, JAXBException, ParseException {
         if (aBuffer == null) {
             throw new IllegalArgumentException("null buffer given");
         }
         String line = aBuffer.readLine(); // line 1
         if (line == null) {
             throw new IllegalArgumentException(
                     "cannot read a single line from buffer");
         }
         aBuffer.readLine(); // line 2
         aBuffer.readLine();
         aBuffer.readLine();
         aBuffer.readLine();
         aBuffer.readLine();
         line = aBuffer.readLine(); // line 7 = date
         String[] splits = line.split(" ");
         Date date = UOS_DATEFORMAT.parse(splits[0]);
         String currency = splits[splits.length - 1];
         /*aBuffer.readLine(); // line 8
         aBuffer.readLine();
         aBuffer.readLine();
         aBuffer.readLine(); // line 11
         aBuffer.readLine();*/
         line = aBuffer.readLine(); // 2010-05-30 made adaptive due to new PDF-layout
         while (!line.startsWith("EUR EUR")) {
             line = aBuffer.readLine();
         }
         //aBuffer.readLine();
         //aBuffer.readLine();
         aBuffer.readLine();
         line = aBuffer.readLine(); // line 17 (old) line 29 (new)
         splits = line.split(" ");
         FixedPointNumber total;
         try {
             total = new FixedPointNumber(splits[splits.length - 1]);
         } catch (Exception e) {
             throw new IllegalArgumentException("Cannot parse total from \"" + splits[splits.length - 1] + "\" in line \"" + line  + "\"", e);
         }
 
         GnucashWritableAccount sicherheitseinbehalt = PluginConfigHelper.getOrConfigureAccountWithKey(aBook, "wirecard.sicherheitseinbehalt." + currency, "thisAccount",
                 "Please select the account for the accumulated 'Sicherheits-Einbehalt' for currency '" + currency + "'");
 //        String sicherheitseinbehalt = "f982b88d02990008053d54d06ebd7049";
 //        if (currency.equals("USD")) {
 //            sicherheitseinbehalt = "259c916fcf62fd414d2b0ec27f252544";
 //        }
 
 
         GnucashWritableTransaction trans = aBook.createWritableTransaction();
         trans.setDatePosted(date);
         trans.setDescription("TEST Saldo = " + total + " eur"); // total is given in euro in the document even if for other currencies :/
         GnucashWritableAccount account = sicherheitseinbehalt; //aBook.getAccountByID(sicherheitseinbehalt);
         if (!account.getBalance(date).equals(total)) {
             trans.setDescription(trans.getDescription() + " NAK");
         } else {
             trans.setDescription(trans.getDescription() + " OK");
         }
        trans.createWritingSplit(account);
 
     }
 
     /**
      * Import clearing-statements and account-statements compressed
      * in a zip-file.
      * @param aBook the model to import to.
      * @param in the zip-file or zip-stream
      * @param saveExractedFiles save the extracted files to ~/.jGnucash/imported
      * @throws IOException errors reading or writing
      * @throws ParseException errors parsing the pdf-file
      * @throws JAXBException errors reading or writing the xml-datastructures of Gnucash
      */
     public static void importZIPStream(final GnucashWritableFile aBook,
                                         final InputStream in,
                                         final boolean saveExractedFiles) throws IOException,
                                                        ParseException,
                                                        JAXBException {
         ZipInputStream zipIn = new ZipInputStream(in);
         ZipEntry entry = null;
         while ((entry = zipIn.getNextEntry()) != null) {
             String fileName = entry.getName();
             File tempFile = null;
             if (saveExractedFiles) {
                 File dir = new File(new File(System.getProperty("user.home"), ".jgnucash"), "imported");
                 dir.mkdirs();
                 tempFile = new File(dir, entry.getName());
             } else {
                 tempFile = File.createTempFile("wirecardImporter_", fileName);
                 tempFile.deleteOnExit();
             }
             try {
                 FileOutputStream tempOut = new FileOutputStream(tempFile);
                byte[] buffer = new byte[BUFFERSIZE];
                int len = 0;
                while ((len = zipIn.read(buffer)) > 0) {
                    tempOut.write(buffer, 0, len);
                }
                tempOut.close();
                importFile(aBook, tempFile);
             } finally {
                 if (!saveExractedFiles) {
                     tempFile.delete();
                 }
             }
         }
     }
 
     /**
      * Import a *_CLE*.pdf -file converted to text using pdftotext containing a clearing.
      * @param aBook The Book we operate on
      * @param aBuffer the import-statement converted to plaintext.
      * @throws IOException errors reading or writing
      * @throws ParseException errors parsing the pdf-file
      * @throws JAXBException errors reading or writing the xml-datastructures of Gnucash
      */
     public static void importClearingStatement(final GnucashWritableFile aBook, final BufferedReader aBuffer) throws IOException, ParseException, JAXBException {
         if (aBuffer == null) {
             throw new IllegalArgumentException("null buffer given");
         }
         String line = aBuffer.readLine();
         if (line == null) {
             throw new IllegalArgumentException(
                     "cannot read a single line from buffer");
         }
         lineMustBeNull(aBuffer);
         lineMustEqual(aBuffer, "Settlement Note");
         lineMustBeNull(aBuffer);
         lineMustEqual(aBuffer, "Settlement ID: Settlement Date: Settlement Period: Merchant Name:");
         lineMustBeNull(aBuffer);
         line = aBuffer.readLine();
         String[] splits = line.split(" ");
         String settlementID = splits[0];
         Date date = UOS_DATEFORMAT.parse(splits[1]);
 
         lineMustBeNull(aBuffer);
         line = aBuffer.readLine();
         if (!line.startsWith("Business Case ")) {
             throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
         }
         lineMustBeNull(aBuffer);
         lineMustEqual(aBuffer, "Invoice No.");
         lineMustBeNull(aBuffer);
         lineMustEqual(aBuffer, "Billing Period");
         lineMustBeNull(aBuffer);
         lineMustEqual(aBuffer, "Comment");
         lineMustBeNull(aBuffer);
         line = aBuffer.readLine();
         if (!line.startsWith("Invoice Amount Exchange Rate Settlement Amount ")) {
             throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
         }
         splits = line.split(" ");
         //Invoice Amount Exchange Rate Settlement Amount
         final int first = 6;
 
         //EUR     EUR    USD         EUR    USD         EUR
         int count = 0;
         List<String> currencies = new LinkedList<String>();
         for (count = 0; splits[count + first].equals("EUR")
                     || splits[count + first].equals("USD"); count++) {
             currencies.add(splits[count + first]);
         }
 
         //999.18 -984.14 0.00        59.17  0.00 3      81.66
         List<FixedPointNumber> invoiceAmount = new ArrayList<FixedPointNumber>(count);
         int i = first + count;
         for (int j = 0; j < count; j++) {
             invoiceAmount.add(new FixedPointNumber(splits[i++]));
         }
 
         //EUR      EUR   0.7336 EUR  EUR    0.7336 EUR  EUR     EUR
         List<FixedPointNumber> exchangeRates = new ArrayList<FixedPointNumber>(count);
         for (int j = 0; j < count; j++) {
             if (currencies.get(j).equals("EUR")) {
                 exchangeRates.add(new FixedPointNumber(1));
             } else {
                 exchangeRates.add(new FixedPointNumber(splits[i++]));
             }
             i++;
         }
         i++; // currency of total
 
     //999.18 -984.14 0.00        59.17  0.00        381.66  455.87
         List<FixedPointNumber> settlementAmount = new ArrayList<FixedPointNumber>(count);
         for (int j = 0; j < count; j++) {
             settlementAmount.add(new FixedPointNumber(splits[i++]));
         }
         FixedPointNumber total = new FixedPointNumber(splits[i++]);
 
         lineMustBeNull(aBuffer);
         line = aBuffer.readLine();
         splits = line.split(" ");
         List<String> invoiceNumbers = new ArrayList<String>(count);
         List<String> invoiceFrom = new ArrayList<String>(count);
         List<String> invoiceTo = new ArrayList<String>(count);
         try {
             i = 0;
             for (int j = 0; j < count; j++) {
                 invoiceNumbers.add(splits[i++]);
                 invoiceFrom.add(splits[i++]);
                 i++;
                 invoiceTo.add(splits[i++]);
                 while (i < splits.length && !splits[i].startsWith(invoiceNumbers.get(0).substring(0, 2))) {
                     i++;  // skip invoice-nodes. We know all invoice-numbers start with the same 3 characters
                 }
             }
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "Cannot parse invoice-number", e);
         }
 
 
 //        String auszahlungAccount = "0682d0d40fefc9af74cae75372b0159d";
 //        String auszahlungAccountUSD = "eeda2c814e012144d7e0c240a6a0a3e8";
 ////        String zahlungenUSDAccount = "3ae589ac215cfdba454569ed652e5113";
 ////        String Sicherheitseinbehalt = "f982b88d02990008053d54d06ebd7049";
 ////        String SicherheitseinbehaltUSD = "259c916fcf62fd414d2b0ec27f252544";
 ////        String disagio = "035c0d6f1d0f19e5b94a1fe532084e95";
 ////        String disagioUSD = "28f9ed9929fc83e59082ceed5b77e596";
 ////        String disagioVorsteuerUSD = "e6e5f9c028769215c42a55a3fe616842";
 //        String targetAccount = "e1a8001a2a189134b6fda79647471f1c";
 
       GnucashWritableTransaction auszahlung = aBook.createWritableTransaction();
       auszahlung.setCurrencyID("EUR");
       auszahlung.setCurrencyNameSpace("ISO4217");
       auszahlung.setDatePosted(date);
       auszahlung.setTransactionNumber("Settlement " + settlementID);
       auszahlung.setDescription("Wirecard - Auszahlung Kreditkartenakzeptanz");
       //targetAccount 192,20eur "Auszahlung"
       //auszahlungAccount -57,62 "UEBERTRAG/UEBERWEISUNG..."
       //auszahlungAccountUSD -179,20 -134,58 "179,20USD*0,7510=134,58EUR"
 
     //TODO: test this
       GnucashWritableAccount targetAccount = PluginConfigHelper.getOrConfigureAccountWithKey(aBook, "wirecard.targetAccount", "thisAccount",
               "Please select the bank-account 'Auszahlung' is transfered to");
       GnucashWritableTransactionSplit targetSplit = auszahlung.createWritingSplit(targetAccount);
       targetSplit.setDescription("Auszahlung");
       targetSplit.setValue(total);
       targetSplit.setQuantity(total);
 
       for (int j = 0; j < count; j++) {
 //          String accountID = auszahlungAccount;
 //          if (!currencies.get(j).equals("EUR")) {
 //              accountID = auszahlungAccountUSD;
 //          }
 //          GnucashWritableAccount account = aBook.getAccountByID(accountID);
           //TODO: test this
           GnucashWritableAccount account = PluginConfigHelper.getOrConfigureAccountWithKey(aBook, "wirecard.auszahlung." + currencies.get(j), "thisAccount",
                   "Please select the account to accumulate 'Auszahlung' in before transfer to the bank-account for currency '" + currencies.get(j) + "'");
 
           GnucashWritableTransactionSplit split = auszahlung.createWritingSplit(account);
           split.setDescription(invoiceAmount.get(j) + currencies.get(j)
                   + "*" + exchangeRates.get(j) + "=" + settlementAmount.get(j) + "EUR");
           split.setSplitAction("");
           if (invoiceNumbers.size() > j) {
               split.setSplitAction(invoiceNumbers.get(j));
               split.setDescription(split.getDescription() + " invoice " + invoiceNumbers.get(j) + " ");
           }
           if (invoiceFrom.size() > j) {
               split.setDescription(split.getDescription() + " : " + invoiceFrom.get(j));
           }
           if (invoiceTo.size() > j) {
               split.setDescription(split.getDescription() + " - " + invoiceTo.get(j));
           }
           auszahlung.setCurrencyID(currencies.get(j));
           auszahlung.setCurrencyNameSpace("ISO4217");
           split.setValue(settlementAmount.get(j).negate());
           split.setQuantity(invoiceAmount.get(j).negate());
       }
 
     }
 
     /**
      * Throws an IllegalArgumentException if the next line reat is
      * not equal to the given one.
      * @param aBuffer the reader to read the line from
      * @param aString the string the line must be equal to
      * @return the given line
      * @throws IOException if we cannot read
      */
     private static String lineMustEqual(final BufferedReader aBuffer, final String aString) throws IOException {
         String line = aBuffer.readLine();
         if (!line.equals(aString)) {
             throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
         }
         return line;
     }
 
     /**
      * @param aBuffer the reader to read from
      * @throws IOException may happen
      */
     private static void lineMustBeNull(final BufferedReader aBuffer)
                                                                     throws IOException {
         String line;
         line = aBuffer.readLine();
         if (line.length() != 0) {
             throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
         }
     }
 
     /**
      * Import a *_PAVI*.pdf -file converted to text using pdftotext containing an account-statement.
      * @param aBook our data-model
      * @param aBuffer the file to import conveted to plaintext
      * @throws IOException if we cannot read parts
      * @throws ParseException if we cannot parse parts of the account-statement
      * @throws JAXBException issues with the XML-backend
      */
     public static void importAccountStatement(final GnucashWritableFile aBook, final BufferedReader aBuffer) throws IOException, ParseException, JAXBException {
 
         if (aBuffer == null) {
             throw new IllegalArgumentException("null buffer given");
         }
         String line = aBuffer.readLine();
         if (line == null) {
             throw new IllegalArgumentException(
                     "cannot read a single line from buffer");
         }
        if (line.trim().length() == 0) {
            LOG.info("first line is empty, skipping.");
            line = aBuffer.readLine();
        }
         if (!line.equals("Wirecard Technologies AG | Bretonischer Ring 4 | 85630 Grasbrunn")) {
            LOG.severe("first line=\"" + line + "\" != \"Wirecard Technologies...\"");
             throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
         }
         lineMustBeNull(aBuffer);
         line = aBuffer.readLine();
         String[] splits = line.split(" ");
         Date date = UOS_DATEFORMAT.parse(splits[splits.length - 1]);
         lineMustBeNull(aBuffer);
 
         lineMustEqual(aBuffer, "Rechnung");
         // Rechnungsnummer: Haendler: Haendlerkennung: Rechnungsperiode: Waehrung: Produkt: Acquirer: Brand:
         // 0913XA794429    Wolschon Import WDB 0000003161ED9CA8 2009-03-16 - 2009-03-22 USD Credit Card Wirecard Bank Master Card, Visa
         line = aBuffer.readLine();
         if (!line.startsWith("Rechnungsnummer: ")) {
                throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
         }
        splits = line.split(" ");
        final int invoiceNrplace = 8;
        String invoiceNr = splits[invoiceNrplace];
        String currency = "EUR";
        int i = 0;
        for (int j = 0; j < splits.length; j++) {
            if (splits[j].equals("USD")) {
                currency = "USD";
                i = j;
                break;
            }
            if (splits[j].equals("EUR")) {
                currency = "EUR";
                i = j;
                break;
            }
        }
        String from = splits[(i - 2) - 1];
        String to = splits[i - 1];
 
        lineMustBeNull(aBuffer);
        lineMustEqual(aBuffer, "Anzahl Netto Transaktionsumsatz Kreditkarteneinzuege "
             + "Gutschriften Chargebacks Erfolgreiche Rueckweisung von Chargebacks Summe Netto "
             + "Transaktionsumsatz Gebuehren Disagio Chargeback Gebuehr Summe Gebuehren ohne MwSt. "
             + "MwSt. auf Gebuehren Summe Gebuehren Sicherheitseinbehalt Auszahlungsbetrag");
        lineMustBeNull(aBuffer);
        line = aBuffer.readLine();
        if (!line.equals("Volumen")) {
               throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
        }
 
        lineMustBeNull(aBuffer);
        line = aBuffer.readLine();
        if (!line.equals("Tarif")) {
               throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
        }
        lineMustBeNull(aBuffer);
        line = aBuffer.readLine();
        if (!line.startsWith("Betrag")) {
               throw new IllegalArgumentException("wrong input-format. Aborting for safety reasons.");
        }
        splits = line.split(" ");
        FixedPointNumber unsatz = new FixedPointNumber(splits[1]);
 
        line = aBuffer.readLine();
        line = aBuffer.readLine();
        // 0 0
        line = aBuffer.readLine();
        line = aBuffer.readLine();
        // 31,18 0 1,15 31,18
        line = aBuffer.readLine();
        line = aBuffer.readLine();
        // 3,70% 52,00 19,00% 5,00%
        line = aBuffer.readLine();
        line = aBuffer.readLine();
        // 1,15 0,00 1,15 0,22 1,37 1,56 28,25
        splits = line.split(" ");
        // 0 = disagio
        // 1 = chargeback
        int p = 2;
        FixedPointNumber fees = new FixedPointNumber(splits[p++]);
        FixedPointNumber feesTax = new FixedPointNumber(splits[p++]);
        FixedPointNumber feesWithTax = new FixedPointNumber(splits[p++]);
        FixedPointNumber security = new FixedPointNumber(splits[p++]);
        FixedPointNumber sum = new FixedPointNumber(splits[p++]);
 
        String auszahlungAccount = "0682d0d40fefc9af74cae75372b0159d";
        if (currency.equals("USD")) {
            auszahlungAccount = "eeda2c814e012144d7e0c240a6a0a3e8";
        }
 //       String zahlungenUSDAccount = "3ae589ac215cfdba454569ed652e5113";
        String sicherheitseinbehalt = "f982b88d02990008053d54d06ebd7049";
        if (currency.equals("USD")) {
            sicherheitseinbehalt = "259c916fcf62fd414d2b0ec27f252544";
        }
        String disagio = "035c0d6f1d0f19e5b94a1fe532084e95";
        if (currency.equals("USD")) {
            disagio = "28f9ed9929fc83e59082ceed5b77e596";
        }
        String zahlungen = "3ae589ac215cfdba454569ed652e5113";
        if (currency.equals("USD")) {
            zahlungen = "3dbb5458285627254e8ec3c7f2ede36e";
        }
 
 //       String disagioVorsteuer =
 //       if (currency.equals("USD")) {
 //           disagioVorsteuer = "e6e5f9c028769215c42a55a3fe616842";
 //       }
 //       String targetAccount = "e1a8001a2a189134b6fda79647471f1c";
 
      GnucashWritableTransaction auszahlung = aBook.createWritableTransaction();
      auszahlung.setCurrencyID(currency);
      auszahlung.setCurrencyNameSpace("ISO4217");
      auszahlung.setDatePosted(date);
      auszahlung.setTransactionNumber("invoice " + invoiceNr);
      auszahlung.setDescription("Settlement " + from + " - " + to + " " + currency + " invoice="
              + invoiceNr);
 
      //auszahlungAccountUSD 179,2
      GnucashWritableTransactionSplit auszahlungSplit = auszahlung.createWritingSplit(aBook.getAccountByID(auszahlungAccount));
      auszahlungSplit.setValue(sum);
      auszahlungSplit.setQuantity(sum);
      auszahlungSplit.setDescription("Auszahlungen");
 
      // "Sicherheitseinebhalt" 9,89 SicherheitseinbehaltUSD
      GnucashWritableTransactionSplit securitySplit = auszahlung.createWritingSplit(aBook.getAccountByID(sicherheitseinbehalt));
      securitySplit.setValue(security);
      securitySplit.setQuantity(security);
      securitySplit.setDescription("Sicherheitseinbehalt");
 
      // "197,80eur*3,7%=7,32=8,71eur Brutto" 8,71  disagioUSD
      GnucashWritableTransactionSplit disagioSplit = auszahlung.createWritingSplit(aBook.getAccountByID(disagio));
      disagioSplit.setValue(feesWithTax);
      disagioSplit.setQuantity(feesWithTax);
      disagioSplit.setDescription("Disagio + Chargeback " + unsatz + "*3.7%="
              + fees + " Netto + " + feesTax + " =" + feesWithTax + " Brutto");
 
      // sum Zahlungen
      GnucashWritableTransactionSplit zahlungenSplit = auszahlung.createWritingSplit(aBook.getAccountByID(zahlungen));
      zahlungenSplit.setValue(unsatz.negate());
      zahlungenSplit.setDescription("Zahlungen");
 
     }
 
     /**
      * @return the book
      */
     public GnucashWritableFile getBook() {
         return myBook;
     }
 
     /**
      * @param aBook the book to set
      */
     public void setBook(final GnucashWritableFile aBook) {
         myBook = aBook;
     }
 
 }
