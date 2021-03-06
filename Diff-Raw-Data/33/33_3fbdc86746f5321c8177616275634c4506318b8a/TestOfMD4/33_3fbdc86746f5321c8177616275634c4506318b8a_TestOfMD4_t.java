 /* vim:set softtabstop=3 shiftwidth=3 tabstop=3 expandtab tw=72:
    $Id$
   
    based on TestOfMD4 from GNU Crypto.
 
    TestOfMD4: Conformance test of MD4 implementation.
    Copyright (C) 2001, 2002, Free Software Foundation, Inc.
    Copyright (C) 2003  Casey Marshall <rsdio@metastatic.org>
   
    This file is a part of Jarsync.
   
    Jarsync is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the
    Free Software Foundation; either version 2 of the License, or (at
    your option) any later version.
   
    Jarsync is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.
   
    You should have received a copy of the GNU General Public License
    along with Jarsync; if not, write to the
   
       Free Software Foundation, Inc.,
       59 Temple Place, Suite 330,
       Boston, MA  02111-1307
       USA
   
    Linking Jarsync statically or dynamically with other modules is
    making a combined work based on Jarsync.  Thus, the terms and
    conditions of the GNU General Public License cover the whole
    combination.
   
    As a special exception, the copyright holders of Jarsync give you
    permission to link Jarsync with independent modules to produce an
    executable, regardless of the license terms of these independent
    modules, and to copy and distribute the resulting executable under
    terms of your choice, provided that you also meet, for each linked
    independent module, the terms and conditions of the license of that
    module.  An independent module is a module which is not derived from
    or based on Jarsync.  If you modify Jarsync, you may extend this
    exception to your version of it, but you are not obligated to do so.
    If you do not wish to do so, delete this exception statement from
    your version.  */
 
 package gnu.testlet.org.metastatic.rsync;
 
 // Tags: JARSYNC
 
 import java.security.MessageDigest;
 import java.security.Security;
 
 import junit.framework.Assert;
 
 import org.apache.log4j.Logger;
import org.junit.Test;
 import org.metastatic.rsync.JarsyncProvider;
 import org.metastatic.rsync.Util;
 
 /**
  * <p>
  * Conformance tests for the MD4 implementation.
  * </p>
  * 
  * @version $Revision$
  */
 public class TestOfMD4
 {
   private static final Logger log = Logger.getLogger(TestOfMD4.class);
 
   // Constants and variables
   // -------------------------------------------------------------------------
 
   private MessageDigest algorithm, clone;
 
   // Constructor(s)
   // -------------------------------------------------------------------------
 
   // default 0-arguments constructor
 
   // Class methods
   // -------------------------------------------------------------------------
 
   // Instance methods
   // -------------------------------------------------------------------------
 
  @Test
   public void test()
   {
     checkPoint("TestOfMD4");
 
     try
       {
         Security.addProvider(new JarsyncProvider());
         algorithm = MessageDigest.getInstance("MD4", "JARSYNC");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.provider");
         throw new Error(x);
       }
 
     // The next two vectors were generated with OpenSSL 0.9.6g.
 
     // Correct padding when input is a multiple of the block size?
     try
       {
         for (int i = 0; i < 64; i++)
           algorithm.update((byte) 'a');
         byte[] md = algorithm.digest();
         String exp = "52f5076fabd22680234a3fa9f9dc5732";
         check(exp.equals(Util.toHexString(md)), "testSixtyFourA");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.provider");
       }
 
     // Correct padding for input larger that 2^32 bits?
     try
       {
         verbose("NOTE: This test may take a while.");
         for (int i = 0; i < 536870913; i++)
           algorithm.update((byte) 'a');
         byte[] md = algorithm.digest();
         String exp = "47d01fa6657f903280232d30b98da482";
         check(exp.equals(Util.toHexString(md)), "test536870913A");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.provider");
       }
 
     try
       {
         byte[] md = algorithm.digest("a".getBytes());
         String exp = "bde52cb31de33e46245e05fbdbd6fb24";
         check(exp.equals(Util.toHexString(md)), "testA");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.testA");
       }
 
     try
       {
         byte[] md = algorithm.digest("abc".getBytes());
         String exp = "a448017aaf21d8525fc10ae87aa6729d";
         check(exp.equals(Util.toHexString(md)), "testABC");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.testABC");
       }
 
     try
       {
         byte[] md = algorithm.digest("message digest".getBytes());
         String exp = "d9130a8164549fe818874806e1c7014b";
         check(exp.equals(Util.toHexString(md)), "testMessageDigest");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.testMessageDigest");
       }
 
     try
       {
         byte[] md = algorithm.digest("abcdefghijklmnopqrstuvwxyz".getBytes());
         String exp = "d79e1c308aa5bbcdeea8ed63df412da9";
         check(exp.equals(Util.toHexString(md)), "testAlphabet");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.testAlphabet");
       }
 
     try
       {
         byte[] md = algorithm
             .digest("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                 .getBytes());
         String exp = "043f8582f241db351ce627e153e7f0e4";
         check(exp.equals(Util.toHexString(md)), "testAsciiSubset");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.testAsciiSubset");
       }
 
     try
       {
         byte[] md = algorithm
             .digest("12345678901234567890123456789012345678901234567890123456789012345678901234567890"
                 .getBytes());
         String exp = "e33b4ddc9c38f2199c3e7b164fcc0536";
         check(exp.equals(Util.toHexString(md)), "testEightyNumerics");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.testEightyNumerics");
       }
 
     try
       {
         algorithm.update("a".getBytes(), 0, 1);
         clone = (MessageDigest) algorithm.clone();
         byte[] md = algorithm.digest();
         String exp = "bde52cb31de33e46245e05fbdbd6fb24";
         check(exp.equals(Util.toHexString(md)), "testCloning #1");
 
         clone.update("bc".getBytes(), 0, 2);
         md = clone.digest();
         exp = "a448017aaf21d8525fc10ae87aa6729d";
         check(exp.equals(Util.toHexString(md)), "testCloning #2");
       } catch (Exception x)
       {
         debug(x);
         fail("TestOfMD4.testCloning");
       }
   }
 
   private void verbose(String string)
   {
     log.debug(string);
   }
 
   private void fail(String string)
   {
     Assert.fail(string);    
   }
 
   private void debug(Exception x)
   {
     log.debug(x);
   }
 
   private void check(boolean b, String string)
   {
     Assert.assertTrue(string, b);
   }
 
   private void checkPoint(String string)
   {
     log.debug(string);
   }
 
 }
