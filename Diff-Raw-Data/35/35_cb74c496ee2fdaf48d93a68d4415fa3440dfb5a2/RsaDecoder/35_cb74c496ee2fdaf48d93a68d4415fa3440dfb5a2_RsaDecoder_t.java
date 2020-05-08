 package com.signr.rsa.core;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import static com.signr.rsa.core.Rsa.DECIMAL_FORMATTER;
 
 import javax.annotation.concurrent.Immutable;
 
 /**
  * The Rsa Decoder is used to decrypt a encoded messages.
  * 
  * This is a thread-safe immutable class.
  * 
  * @author Marcello de Sales (marcello.desales@gmail.com)
  * 
  */
 @Immutable
 public final class RsaDecoder {
 
   /**
    * The original message received
    */
   private final String decryptedMessage;
   /**
    * The encrypted message as a result of the rsa algorithm
    * */
   private final String encryptedMessage;
   /**
    * The privateKey to decode the encrypted message
    * */
   private final RsaPrivateKey privateKey;
   /**
    * Log used to output the messages
    */
   private final List<String> log = new ArrayList<>();
 
   /**
    * Constructs a new RsaSender with the originalMessage along with the Rsa structure
    * 
    * @param inputEncryptedMessage is the message encrypted in blocks.
    * @param privateKey is the private key to decode the given message
    */
   private RsaDecoder(String inputEncryptedMessage, RsaPrivateKey givenPrivateKey) {
     privateKey = givenPrivateKey;
     encryptedMessage = inputEncryptedMessage;
     decryptedMessage = getDecryptedMessage(encryptedMessage);
     log.add("");
     log.add(Rsa.LOG_ARROW + "Original Message");
     log.add(decryptedMessage);
   }
 
   /**
    * @param encryptedMessage
    * @param privateKey is the instance of a private key
    * @return a new instance of the RsaReceiver based on the rsa.
    */
   public static RsaDecoder newInstance(String encryptedMessage, RsaPrivateKey privateKey) {
     return new RsaDecoder(encryptedMessage, privateKey);
   }
 
   /**
    * @return The original message decoded from the RSA process.
    */
   public String getOriginalMessage() {
     return this.decryptedMessage;
   }
 
   /**
    * @return The encrypted message to be decoded.
    */
   public String getEncryptedMessage() {
     return this.encryptedMessage;
   }
 
   /**
    * @param encryptedMessage is the encrypted message
    * @return the original message sent
    */
   private String getDecryptedMessage(String encryptedMessage) {
     log.add("");
     log.add("#### Receiving the message ####");
     log.add("");
     log.add(Rsa.LOG_ARROW + "Encrypted Message");
     log.add(encryptedMessage);
     log.add("");
     log.add(Rsa.LOG_ARROW + "Setting the private key");
     log.add("(N , D) = ("
         + DECIMAL_FORMATTER.format(this.privateKey.getRSAPublicKey().getKeyN()) + " , "
         + DECIMAL_FORMATTER.format(this.privateKey.getKeyD()) + ")");
 
     List<Integer> chriptedBlocks = this.getCryptedBlocks(encryptedMessage);
     log.add("");
     log.add(Rsa.LOG_ARROW + "Decripting each block");
     String dechriptedBlocks = this.decodeBlocksToAscii(chriptedBlocks);
     log.add("");
     log.add(Rsa.LOG_ARROW + "Complete message in ASCII");
     log.add(dechriptedBlocks);
     return this.decodeAsciiMessage(dechriptedBlocks);
   }
 
   /**
    * @param encryptedMessage is the encryption used
    * @return the the encrypted blocks from given encrypted message
    */
   private List<Integer> getCryptedBlocks(String encryptedMessage) {
     List<Integer> chriptedBlocks = new ArrayList<>();
     StringTokenizer tokenizer = new StringTokenizer(encryptedMessage, Rsa.DELIMITER);
     while (tokenizer.hasMoreTokens()) {
       String chriptedBlock = tokenizer.nextToken();
       chriptedBlocks.add(new Integer(chriptedBlock));
     }
     return chriptedBlocks;
   }
 
   /**
    * @param encryptedBlock decrypts a given encrypted block
    * @return the decrypted value for the given encrypted block
    */
   private double decodeEncryptedBlock(double encryptedBlock) {
     double dechriptedb =
         Algebra.SINGLETON.getPowerModuleN(encryptedBlock, this.privateKey.getKeyD(),
             this.privateKey.getRSAPublicKey().getKeyN());
     this.log.add("Ascii(" + DECIMAL_FORMATTER.format(encryptedBlock) + ") = "
         + DECIMAL_FORMATTER.format(encryptedBlock) + " ^ "
         + DECIMAL_FORMATTER.format(this.privateKey.getKeyD()) + " mod "
         + DECIMAL_FORMATTER.format(this.privateKey.getRSAPublicKey().getKeyN()) + " = "
         + DECIMAL_FORMATTER.format(dechriptedb));
     return dechriptedb;
   }
 
   /**
    * @param encryptedBlocks a collection of encrypted blocks
    * @return the decrypted blocks in single ASCII string
    */
   private String decodeBlocksToAscii(List<Integer> encryptedBlocks) {
     log.add("Ascii(x) = x ^ D mod N");
     log.add("");
 
     StringBuilder asciiMessage = new StringBuilder();
     int dechriptedValue;
     for (int blockValue : encryptedBlocks) {
       Double blockValeu = new Double(String.valueOf(blockValue));
       dechriptedValue = (int) this.decodeEncryptedBlock(blockValeu.doubleValue());
       asciiMessage.append(dechriptedValue);
     }
     return asciiMessage.toString();
   }
 
   /**
    * @param asciiMessage is the message in the ascii format, containing only digits
    * @return the original message based on the ascii format of the message
    */
   private String decodeAsciiMessage(String asciiMessage) {
     StringBuilder origem = new StringBuilder();
     while (asciiMessage.length() > 0) {
       String valueStr = asciiMessage.substring(0, 3);
       asciiMessage = asciiMessage.substring(3, asciiMessage.length());
       int asciiValue = Integer.parseInt(valueStr) - 100;
       String charValue = new Character((char) asciiValue).toString();
       origem.append(charValue);
     }
     return origem.toString();
   }
 
   /**
    * Prints the log into the file-system
    * 
    * @param stream is the print stream to print the message. System.out, Servlet.getOutputStream,
    *        etc.
    */
   public void printLog(PrintStream stream) {
     for (String logEntry : this.log) {
       stream.println(logEntry);
     }
   }

  /**
   * @return the log entries for the creation of the Decoding process.
   */
  public String[] getLog() {
    List<String> logEntries = new ArrayList<>(log.size());
    for (String factorEntry : log) {
      logEntries.add(factorEntry);
    }
    return logEntries.toArray(new String[logEntries.size()]);
  }
 }
