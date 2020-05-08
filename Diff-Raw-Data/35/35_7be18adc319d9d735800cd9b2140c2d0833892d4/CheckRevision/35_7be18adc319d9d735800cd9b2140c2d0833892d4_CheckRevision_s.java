 /*
  * CheckRevision.java
  * 
  * Created on March 10, 2004, 9:05 AM
  */
 package com.javaop.BNetLogin.versioning;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.FileInputStream;
 import java.io.InputStream;
 
 import java.nio.ByteOrder;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 
 import java.util.StringTokenizer;
 
 import com.javaop.exceptions.LoginException;
 
 /**
  * This takes care of the CheckRevision() for the main game files of any
  * program. This is done to prevent tampering and to make sure the version is
  * correct. This class is generally slow because it has to read through the
  * entire files. The majority of the time is spent in i/o, but I've tried to
  * optimize this as much as possible.
  * 
  * @author iago, wjlafance
  */
 public class CheckRevision {
     
     /**
      * Hashcodes for each MPQ file, for old-style CheckRevisions
      */
     private static final int hashcodes[] = {
             0xE7F4CB62, 0xF6A14FFC, 0xAA5504AF, 0x871FCDC2,
             0x11BF6A18, 0xC57292E6, 0x7927D27E, 0x2FEC8733
     };
 
     /**
      * This is the main entry point for doing CheckRevision. This sorts out
      * what kind of CheckRevision we're doing and then calls another function.
      * @param mpqName MPQ name specified in SID_AUTH_INFO response
      * @param files Files to run CheckRevision on
      * @param formula Version check formula specified in SID_AUTH_INFO response
      * @return
      */
     public static int doCheckRevision(String mpqName, String[] files,
             byte[] formula) throws LoginException, IOException
     {
         //System.out.println("Entered doCheckRevision for MPQ: " + mpqName);
         //for (int i = 0; i < files.length; i++)
             //System.out.println("File " + i + ": " + files[i]);
             
         String mpq = mpqName.toLowerCase();
         
         // Windows (IX86)
         if(mpq.matches("ix86ver[0-7].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(7, 8));
             return checkRevisionOld("IX86", mpqNum, files, new String(formula));
         } else if (mpq.matches("ver-ix86-[0-7].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(9, 10));
             return checkRevisionOld("IX86", mpqNum, files, new String(formula));
         } else if (mpq.matches("lockdown-ix86-[0-1][0-9].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(14, 16));
             return checkRevisionLockdown("IX86", mpqNum, files, formula);
         }
         
         // Power Macintosh
         if (mpq.matches("pmacver[0-7].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(7, 8));
             return checkRevisionOld("PMAC", mpqNum, files, new String(formula));
         } else if (mpq.matches("ver-pmac-[0-7].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(9, 10));
             return checkRevisionOld("PMAC", mpqNum, files, new String(formula));
         } else if (mpq.matches("lockdown-pmac-[0-1][0-9].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(14, 16));
             return checkRevisionLockdown("PMAC", mpqNum, files, formula);
         }
         
         // Mac OS X
         if (mpq.matches("xmacver[0-7].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(7, 8));
             return checkRevisionOld("XMAC", mpqNum, files, new String(formula));
         } else if (mpq.matches("ver-xmac-[0-7].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(9, 10));
             return checkRevisionOld("XMAC", mpqNum, files, new String(formula));
         } else if (mpq.matches("lockdown-xmac-[0-1][0-9].mpq")) {
             int mpqNum = Integer.parseInt(mpq.substring(14, 16));
             return checkRevisionLockdown("XMAC", mpqNum, files, formula);
         }
         
         throw new LoginException("MPQ filename (" + mpqName + ") doesn't match "
                 + "any known pattern.");
     }
 
     /**
      * Performs an old-style CheckRevision.
      * 
      * @param platform IX86, PMAC, XMAC
      * @param mpqNumber MPQ number specified in SID_AUTH_INFO response
      * @param files The array of files we're checking. Generally the main game
      * files, like Starcraft.exe, Storm.dll, and Battle.snp.
      * @param formula Version check formula from SID_AUTH_INFO response
      * @throws FileNotFoundException If the datafiles aren't found.
      * @throws IOException If there is an error reading from one of the datafiles.
      * @return The 32-bit CheckRevision hash.
      */
     public static int checkRevisionOld(String platform, int mpqNumber,
             String []files, String formula) throws FileNotFoundException,
             IOException, LoginException
     {
 
         int checksum = 0;
 
         StringTokenizer tok = new StringTokenizer(formula, " ");
 
         long a=0,b=0,c=0;
 
         for(int x = 0; x<3; x++){
           String seed = tok.nextToken();
           if(seed.toLowerCase().startsWith("a=") == true) a = Long.parseLong(seed.substring(2));
           if(seed.toLowerCase().startsWith("b=") == true) b = Long.parseLong(seed.substring(2));
           if(seed.toLowerCase().startsWith("c=") == true) c = Long.parseLong(seed.substring(2));
         }
         tok.nextToken();
         if (a == 0 || b == 0 || c == 0)
                 return 0;
         String formulaChunk;
 
         formulaChunk = tok.nextToken();
         if(formulaChunk.matches("A=A.S") == false) checksum
                 = checkRevisionOldSlow(platform, mpqNumber, files, formula);
         char op1 = formulaChunk.charAt(3);
 
         formulaChunk = tok.nextToken();
         if(formulaChunk.matches("B=B.C") == false && checksum == 0) checksum
                 = checkRevisionOldSlow(platform, mpqNumber, files, formula);
         char op2 = formulaChunk.charAt(3);
 
         formulaChunk = tok.nextToken();
         if(formulaChunk.matches("C=C.A") == false && checksum == 0) checksum
                 = checkRevisionOldSlow(platform, mpqNumber, files, formula);
         char op3 = formulaChunk.charAt(3);
 
         formulaChunk = tok.nextToken();
         if(formulaChunk.matches("A=A.B") == false && checksum == 0) checksum
                 = checkRevisionOldSlow(platform, mpqNumber, files, formula);
         char op4 = formulaChunk.charAt(3);
 
         if(checksum == 0) {
           // Now we actually do the hashing for each file
           // Start by hashing A by the hashcode
 
           a ^= hashcodes[mpqNumber];
 
           for(int i = 0; i < files.length; i++)
           {
             File currentFile = new File(files[i]);
 
             byte []data = readFile(currentFile);
             for(int j = 0; j < data.length; j += 4)
             {
                 int s = 0;
                 s |= ((data[j+0] << 0) & 0x000000ff);
                 s |= ((data[j+1] << 8) & 0x0000ff00);
                 s |= ((data[j+2] << 16) & 0x00ff0000);
                 s |= ((data[j+3] << 24) & 0xff000000);
 
                 switch (op1) {
                     case '^': a ^= s; break;
                     case '+': a += s; break;
                     case '-': a -= s; break;
                     case '*': a *= s; break;
                     case '/': a /= s; break;
                 }
                 switch (op2) {
                     case '^': b ^= c; break;
                     case '+': b += c; break;
                     case '-': b -= c; break;
                     case '*': b *= c; break;
                     case '/': b /= c; break;
                 }
                 switch (op3) {
                     case '^': c ^= a; break;
                     case '+': c += a; break;
                     case '-': c -= a; break;
                     case '*': c *= a; break;
                     case '/': c /= a; break;
                 }
                 switch (op4) {
                     case '^': a ^= b; break;
                     case '+': a += b; break;
                     case '-': a -= b; break;
                     case '*': a *= b; break;
                     case '/': a /= b; break;
                 }
             }
           }
           checksum = (int)c;
         }
         return checksum;
     }
 
     public static int checkRevisionOldSlow(String platform, int mpqNumber,
             String []files, String formula) throws FileNotFoundException,
             IOException, LoginException
     {
 
         // First, parse the versionString to name=value pairs and put them
         // in the appropriate place
         long[] values = new long[4];
         int[] opValueDest = new int[4];
         int[] opValueSrc1 = new int[4];
         char[] operation = new char[4];
         int[] opValueSrc2 = new int[4];
 
         // Break this apart at the spaces
         StringTokenizer s = new StringTokenizer(formula, " ");
         int currentFormula = 0;
        while(s.hasMoreTokens()){
             String thisToken = s.nextToken();
             // As long as there is an '=' in the string
            if(thisToken.indexOf('=') > 0){
                 // Break it apart at the '='
                 StringTokenizer nameValue = new StringTokenizer(thisToken, "=");
                 if(nameValue.countTokens() != 2) return 0;
 
                 int variable = getNum(nameValue.nextToken().charAt(0));
 
                 String value = nameValue.nextToken();
 
                 // If it starts with a number, assign that number to the appropriate variable
                if(Character.isDigit(value.charAt(0))){
                     values[variable] = Long.parseLong(value);
                }else{
                     opValueDest[currentFormula] = variable;
 
                     opValueSrc1[currentFormula] = getNum(value.charAt(0));
                     operation[currentFormula] = value.charAt(1);
                     opValueSrc2[currentFormula] = getNum(value.charAt(2));
 
                     currentFormula++;
                 }
             }
         }
 
         // Now we actually do the hashing for each file
         // Start by hashing A by the hashcode
           
         values[0] ^= hashcodes[mpqNumber];
         
        for(int i = 0; i < files.length; i++)
        {
             File currentFile = new File(files[i]);
 
             byte []data = readFile(currentFile);
 
            for(int j = 0; j < data.length; j += 4)
            {
                 values[3] = 0;
                 values[3] |= ((data[j+0] << 0) & 0x000000FF);
                values[3] |= ((data[j+1] << 8) & 0x0000ff00);
                values[3] |= ((data[j+2] << 16) & 0x00ff0000);
                values[3] |= ((data[j+3] << 24) & 0xff000000);
 
                 for(int k = 0; k < currentFormula; k++){
                     switch(operation[k]){
                         case '^': values[opValueDest[k]] = values[opValueSrc1[k]] ^ values[opValueSrc2[k]]; break;
                         case '+': values[opValueDest[k]] = values[opValueSrc1[k]] + values[opValueSrc2[k]]; break;
                         case '-': values[opValueDest[k]] = values[opValueSrc1[k]] - values[opValueSrc2[k]]; break;
                         case '*': values[opValueDest[k]] = values[opValueSrc1[k]] * values[opValueSrc2[k]]; break;
                         case '/': values[opValueDest[k]] = values[opValueSrc1[k]] / values[opValueSrc2[k]]; break;
                     }
                 }
              }
         }
         return (int)values[2];
     }
     
     
     /**
      * Performs an lockdown CheckRevision.
      * 
      * @param platform IX86, PMAC, XMAC
      * @param mpqNumber MPQ number specified in SID_AUTH_INFO response
      * @param files The array of files we're checking. Generally the main game
      * files, like Starcraft.exe, Storm.dll, and Battle.snp.
      * @param formula Version check formula from SID_AUTH_INFO response
      * @throws FileNotFoundException If the datafiles aren't found.
      * @throws IOException If there is an error reading from one of the datafiles.
      * @return The 32-bit CheckRevision hash.
      */
     public static int checkRevisionLockdown(String platform, int mpqNumber,
             String[] files, byte[] formula) throws LoginException, IOException
     {
         throw new LoginException("Lockdown CheckRevision not supported for "
                 + platform);
     }
 
     
     /**
      * Reads a file and returns a byte array.
      */
     private static byte []readFile(File file) throws IOException {
       int length = (int) file.length();
       byte []ret = new byte[(length % 1024) == 0 ? length : (length / 1024 * 1024) + 1024];
 
       InputStream in = new FileInputStream(file);
       in.read(ret);
       in.close();
 
       int value = 0xFF;
       for(int i = (int) file.length(); i < ret.length; i++)
         ret[i] = (byte) value--;
 
       return ret;
     }
 
     /**
      * Utility method for old slow CheckRevision
      */
     private static int getNum(char c) {
         c = Character.toUpperCase(c);
         if(c == 'S')
             return 3;
 
         return c - 'A';
     }
 
 }
