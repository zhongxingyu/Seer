 package com.signr.rsa.core;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.concurrent.Immutable;
 
 import static com.signr.rsa.core.Rsa.DECIMAL_FORMATTER;
 
 /**
  * The Rsa Encoder implements the encryption machine.
  * 
  * This is a thread-safe immutable class.
  * 
  * @author Marcello de Sales (marcello.desales@gmail.com)
  * 
  */
 @Immutable
 public final class RsaEncoder {
 
   /**
    * The original message to be sent
    */
   private final String originalMessage;
   /**
    * The encrypted message as a result of the rsa algorithm
    */
   private final String encryptedMessage;
   /**
    * The execution log
    */
   private final List<String> log = new ArrayList<>();
   /**
    * The RSAPublic key used to encrypt the original message
    */
   private final RsaPublicKey publicKey;
 
   /**
    * Constructs a new RsaSender with the originalMessage along with the Rsa structure
    * 
    * @param originalMessage is the original human-readable message
    * @param publicKey is the RSA object holding the public and private keys
    */
   private RsaEncoder(String givenOriginalMessage, RsaPublicKey givenPublicKey) {
     publicKey = givenPublicKey;
     originalMessage = givenOriginalMessage;
     encryptedMessage = encryptMessage(originalMessage);
 
     log.add("");
     log.add(Rsa.LOG_ARROW + "Encrypted Message");
     log.add(encryptedMessage);
   }
 
   /**
    * @param originalMessage is the original message to be encoded
    * @param publicKey is the key used to encrypt the given original message
    * @return a new instance of RsaSender
    */
   public static RsaEncoder newInstance(String originalMessage, RsaPublicKey publicKey) {
     return new RsaEncoder(originalMessage, publicKey);
   }
 
   /**
    * @return The original message
    */
   public String getOriginalMessage() {
     return this.originalMessage;
   }
 
   /**
    * @return the encrypted message
    */
   public String getEncryptedMessage() {
     return this.encryptedMessage;
   }
 
   /**
    * @param originalMessage is the human-readable message
    * @return the encrypted message based on the original human-readable message and the RSA private
    *         and public keys
    */
   private String encryptMessage(String originalMessage) {
     log.add("");
     log.add("#### Sending a message ####");
     log.add("");
     log.add(Rsa.LOG_ARROW + "Original Message");
     log.add(originalMessage);
     log.add("");
     log.add(Rsa.LOG_ARROW + "Setting the receiver's public key");
     log.add("(N , E) = (" + DECIMAL_FORMATTER.format(this.publicKey.getKeyN()) + " , "
         + DECIMAL_FORMATTER.format(this.publicKey.getKeyE()) + ")");
     log.add("");
     log.add(Rsa.LOG_ARROW + "Transforming the message to ASCII code");
     String asciiMessage = this.encodeStringToAscii(originalMessage);
     log.add(asciiMessage);
     log.add("");
     log.add(Rsa.LOG_ARROW + "Configuring randomly selected blocks from the ASCII message");
     List<Integer> asciiBlocks = this.getAsciiBlocks(asciiMessage);
     return this.encryptBlocks(asciiBlocks);
   }
 
   /**
    * @param widestring is the string before encryption
    * @return the ASCII representation a string
    */
   private String encodeStringToAscii(String widestring) {
     StringBuffer ascii = new StringBuffer();
     for (int i = 0; i < widestring.length(); i++) {
       ascii.append((int) widestring.charAt(i) + 100);
     }
     return ascii.toString();
   }
 
   /**
    * Returns the set of blocks of the ASCII representation in a random way 1241355334 =>
    * [1241][355][3][34]. It can't begin with 0's.
    */
   private List<Integer> getAsciiBlocks(String asciiM) {
     int i, messageLength;
     String asciiMessage = asciiM;
     List<Integer> blocks = new ArrayList<>();
     String stringN = new String(new Double(this.publicKey.getKeyN()).toString());
     int blockLength = stringN.length() - 3;
 
     while (asciiMessage.length() > 0) {
       messageLength = asciiMessage.length();
 
       i = Algebra.SINGLETON.getARandomInteger(blockLength); // uses n +1
 
       if (i > messageLength) {
         i = messageLength;
       }
       if (messageLength != i) {
         while (asciiMessage.charAt(i) == '0')
           i++;
       }
 
       String toBeCopied = asciiMessage.substring(0, i);
       if (Integer.parseInt(toBeCopied) > this.publicKey.getKeyN()) { // use n
         i--;
         while (asciiMessage.charAt(i) == '0') {
           i--;
         }
       }
 
       String block = asciiMessage.substring(0, i);
       asciiMessage = asciiMessage.substring(i, asciiMessage.length());
       blocks.add(new Integer(block));
     }
     return blocks;
   }
 
   /**
    * Encrypts a block with the Power Module N algorithm [1241] => 343434
    * 
    * @param x is the number representing a block
    * @return the result of the Power Module N algorithm
    */
   private double encryptBlock(int x) {
     double encryptedBlock =
         Algebra.SINGLETON.getPowerModuleN(x, this.publicKey.getKeyE(), this.publicKey.getKeyN());
     this.log.add("Block(" + DECIMAL_FORMATTER.format(x) + ") = " + DECIMAL_FORMATTER.format(x)
         + " ^ " + DECIMAL_FORMATTER.format(this.publicKey.getKeyE()) + " mod "
         + DECIMAL_FORMATTER.format(this.publicKey.getKeyN()) + " = "
         + DECIMAL_FORMATTER.format(encryptedBlock));
     return encryptedBlock;
   }
 
   /**
    * Returns the set of encrypted blocks of the ascii representation applying the Power Module N
    * algorithm Coded blocks: [1241][355][3][34] ==> Encrypted Final message: 343434-43552-43544-3435
    * 
    * @param codedBlocks is the list of coded blocks to be encrypted
    * @return the encrypted message based on the Power Module N algorithm based on the coded blocks
    */
   private String encryptBlocks(List<Integer> codedBlocks) {
     log.add("Bloco(x) = x ^ E mod N");
     log.add("");
 
     double encryptedBlock;
     StringBuilder code = new StringBuilder();
 
     for (int blockValue : codedBlocks) {
       encryptedBlock = this.encryptBlock(blockValue);
       code.append(DECIMAL_FORMATTER.format(encryptedBlock) + Rsa.DELIMITER);
     }
     return code.toString().substring(0, code.toString().length() - 1);
   }
 
   /**
    * Prints the log into a given print stream
    * 
    * @param printStream is a stream where the log will be printed. System.out,
    *        Servlet.getPrintWriter
    */
   public void printLog(PrintStream printStream) {
     for (String logEntry : this.log) {
       printStream.println(logEntry);
     }
   }
 }
