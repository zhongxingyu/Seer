 package net.link.oath;
 
 /**
  * This class represents an HOTP configuration. It can be used to generate OTP's or to validate them
  * whithin a defined lookahead.
  */
 public class HOTP {
 
     private byte[] secret;
     private int codeDigits;
     private boolean addChecksum;
     private int truncationOffset;
     private int lookahead;
 
     /**
      * Instantiates an HOTP.
      *
      * @param secret           the shared secret
      * @param codeDigits       the number of digits in the OTP, not
      *                         including the checksum, if any.
      * @param addChecksum      a flag that indicates if a checksum digit
      *                         should be appended to the OTP.
      * @param truncationOffset the offset into the MAC result to
      *                         begin truncation.  If this value is out of
      *                         the range of 0 ... 15, then dynamic
      *                         truncation  will be used.
      *                         Dynamic truncation is when the last 4
      *                         bits of the last byte of the MAC are
      *                         used to determine the start offset.
      * @param lookahead        how many additional OTP's can be generated before validation fails.
      *                         This parameter tries to deal with out of sync counters
      */
     public HOTP(byte[] secret,
                 int codeDigits,
                 boolean addChecksum,
                 int truncationOffset,
                 int lookahead) {
         this.secret = secret;
         this.codeDigits = codeDigits;
         this.addChecksum = addChecksum;
         this.truncationOffset = truncationOffset;
         this.lookahead = lookahead;
     }
 
     /**
      * This method generates an OTP value for the given counter
      *
      * @param counter the counter, time, or other value that
      *                changes on a per use basis.
      * @return A numeric String in base 10 that includes
      *         codeDigits digits plus the optional checksum
      *         digit if requested.
      */
     public String generateOTP(int counter) {
         // put movingFactor value into text byte array
         int digits = this.addChecksum ? (this.codeDigits + 1) : this.codeDigits;
         byte[] text = new byte[8];
         for (int i = text.length - 1; i >= 0; i--) {
             text[i] = (byte) (counter & 0xff);
             counter >>= 8;
         }
 
         // compute hmac hash
         byte[] hash = Util.hmac_sha("HmacSHA1", this.secret, text);
 
         // put selected bytes into result int
         int offset = hash[hash.length - 1] & 0xf;
         if ((0 <= this.truncationOffset) &&
                 (this.truncationOffset < (hash.length - 4))) {
             offset = truncationOffset;
         }
         int binary =
                 ((hash[offset] & 0x7f) << 24)
                         | ((hash[offset + 1] & 0xff) << 16)
                         | ((hash[offset + 2] & 0xff) << 8)
                         | (hash[offset + 3] & 0xff);
 
         int otp = binary % DIGITS_POWER[codeDigits];
         if (addChecksum) {
             otp = (otp * 10) + calcChecksum(otp, codeDigits);
         }
         String result = Integer.toString(otp);
         while (result.length() < digits) {
             result = "0" + result;
         }
         return result;
     }
 
     /**
      * @param counter the counter value that was saved by the service provider
      * @param otp     the otp that was provided by the subject
      * @return the new counter value to be stored by the service provider
      * @throws InvalidResponseException when the response was out of reach
      */
     public int validate(int counter, String otp) throws InvalidResponseException {
         int i = 0;
        while (i <= lookahead) {
             if (generateOTP(counter).equals(otp)) return counter + i;
            i++;
         }
         throw new InvalidResponseException("Provided HOTP response is outside the lookahead range");
     }
 
 
     // These are used to calculate the check-sum digits.
     //                                0  1  2  3  4  5  6  7  8  9
     private static final int[] doubleDigits =
             {0, 2, 4, 6, 8, 1, 3, 5, 7, 9};
 
     /**
      * Calculates the checksum using the credit card algorithm.
      * This algorithm has the advantage that it detects any single
      * mistyped digit and any single transposition of
      * adjacent digits.
      *
      * @param num    the number to calculate the checksum for
      * @param digits number of significant places in the number
      * @return the checksum of num
      */
     private static int calcChecksum(long num, int digits) {
         boolean doubleDigit = true;
         int total = 0;
         while (0 < digits--) {
             int digit = (int) (num % 10);
             num /= 10;
             if (doubleDigit) {
                 digit = doubleDigits[digit];
             }
             total += digit;
             doubleDigit = !doubleDigit;
         }
         int result = total % 10;
         if (result > 0) {
             result = 10 - result;
         }
         return result;
     }
 
     private static final int[] DIGITS_POWER
             // 0 1  2   3    4     5      6       7        8
             = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
 
 }
