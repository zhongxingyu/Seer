 package helpers;
  
  import java.security.MessageDigest;
  import java.security.NoSuchAlgorithmException;
  import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
  import sun.misc.BASE64Decoder;
  import sun.misc.BASE64Encoder;
  import java.sql.*;
  import java.util.Arrays;
 import java.security.SecureRandom;
 import model.User;
  
  public class OwaspAuthentication {
    private final static int ITERATION_NUMBER = 1000;
  
    public OwaspAuthentication() {
    }
  
    /**
     * Authenticates the user with a given login and password
     * If password and/or login is null then always returns false.
     * If the user does not exist in the database returns false.
     * @param con Connection An open connection to a database
     * @param login String The login of the user
     * @param password String The password of the user
     * @return boolean Returns true if the user is authenticated, false otherwise
     * @throws SQLException If the database is inconsistent or unavailable (
     *           (Two users with the same login, salt or digested password altered etc.)
     * @throws NoSuchAlgorithmException If the algorithm SHA-1 is not supported by the JVM
     */
    public static boolean authenticate(String login, String password, User user){
 	   
 	   String digest, salt;
        salt = user.getSalt();
        digest = user.getPassword();
        
       if(!user.isActive()){
    	   return false;
       }
       
        try
        {
            byte[] bDigest = base64ToByte(digest);
            byte[] bSalt = base64ToByte(salt);
 
            // Compute the new DIGEST
            byte[] proposedDigest = getHash(ITERATION_NUMBER, password, bSalt);
 
            return Arrays.equals(proposedDigest, bDigest);
        }
        catch (Exception e)
        {
     	   //TODO: log error
     	   return false;
        }
    }
  
  
  
    /**
     * Inserts a new user in the database
     * @param con Connection An open connection to a databse
     * @param login String The login of the user
     * @param password String The password of the user
     * @return boolean Returns true if the login and password are ok (not null and length(login)<=100
  * @throws Exception 
     */
    public static String getUserPassword(String login, String password, byte[] bSalt)
            throws Exception
    {
        if (login!=null && password!=null && login.length()<=100){
            byte[] bDigest = getHash(ITERATION_NUMBER, password, bSalt);
            String sDigest = byteToBase64(bDigest);
            return sDigest;
        } else {
            throw new Exception("get password failed");
        }
    }
  
    public static byte[] getBsalt() throws NoSuchAlgorithmException
    {
 	   //Uses a secure Random not a simple Random
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        // Salt generation 64 bits long
        byte[] bSalt = new byte[8];
        random.nextBytes(bSalt);
        return bSalt;
    }
    /**
     * From a password, a number of iterations and a salt,
     * returns the corresponding digest
     * @param iterationNb int The number of iterations of the algorithm
     * @param password String The password to encrypt
     * @param salt byte[] The salt
     * @return byte[] The digested password
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist
  * @throws UnsupportedEncodingException 
     */
    public static byte[] getHash(int iterationNb, String password, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(salt);
        byte[] input = digest.digest(password.getBytes("UTF-8"));
        for (int i = 0; i < iterationNb; i++) {
            digest.reset();
            input = digest.digest(input);
        }
        return input;
    }
  
  
    public static void creerTable(Connection con) throws SQLException{
        Statement st = null;
        try {
            st = con.createStatement();
            st.execute("CREATE TABLE CREDENTIAL (LOGIN VARCHAR(100) PRIMARY KEY, PASSWORD VARCHAR(32) NOT NULL, SALT VARCHAR(32) NOT NULL)");
        } finally {
            close(st);
        }
    }
  
  
  
    /**
     * Closes the current statement
     * @param ps Statement
     */
    public static void close(Statement ps) {
        if (ps!=null){
            try {
                ps.close();
            } catch (SQLException ignore) {
            }
        }
    }
  
    /**
     * Closes the current resultset
     * @param ps Statement
     */
    public void close(ResultSet rs) {
        if (rs!=null){
            try {
                rs.close();
            } catch (SQLException ignore) {
            }
        }
    }
  
  
    /**
     * From a base 64 representation, returns the corresponding byte[] 
     * @param data String The base64 representation
     * @return byte[]
     * @throws IOException
     */
    public static byte[] base64ToByte(String data) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        return decoder.decodeBuffer(data);
    }
  
    /**
     * From a byte[] returns a base 64 representation
     * @param data byte[]
     * @return String
     * @throws IOException
     */
    public static String byteToBase64(byte[] data){
        BASE64Encoder endecoder = new BASE64Encoder();
        return endecoder.encode(data);
    }
  }
