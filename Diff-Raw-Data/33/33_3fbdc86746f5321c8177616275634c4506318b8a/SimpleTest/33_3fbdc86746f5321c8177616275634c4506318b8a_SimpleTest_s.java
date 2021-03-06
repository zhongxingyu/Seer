 /* vim:set softtabstop=3 shiftwidth=3 tabstop=3 expandtab tw=72:
    $Id$
   
    SimpleTest: real test of the algorithm.
    Copyright (C) 2003  Casey Marshall <rsdio@metastatic.org>
   
    This file is a part of Jarsync
   
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
 
 // Tags: JARSYNC
 
 package gnu.testlet.org.metastatic.rsync;
 
 import java.security.MessageDigest;
 import java.security.Security;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 import org.metastatic.rsync.Checksum32;
 import org.metastatic.rsync.Configuration;
 import org.metastatic.rsync.DataBlock;
 import org.metastatic.rsync.Generator;
 import org.metastatic.rsync.JarsyncProvider;
 import org.metastatic.rsync.ListenerException;
 import org.metastatic.rsync.MatcherEvent;
 import org.metastatic.rsync.MatcherListener;
 import org.metastatic.rsync.MatcherStream;
 import org.metastatic.rsync.Rebuilder;
 import org.metastatic.rsync.Util;
 
 /**
  * Exercise the rsync algorithm 50 times, each time with randomly generated data
  * sets and differences, and with a random message digest algorithm.
  * 
  * @version $Revision $
  */
 public class SimpleTest
   implements MatcherListener
 {
   private static final Logger log = Logger.getLogger(SimpleTest.class);
 
   // Fields.
   // -----------------------------------------------------------------------
 
   Random rand;
 
  List deltas;
 
   // Constructor.
   // -----------------------------------------------------------------------
 
   public SimpleTest() {
     rand = new Random();
   }
 
   // Instance methods.
   // -----------------------------------------------------------------------
 
   public void update(MatcherEvent event)
   {
     deltas.add(event.getDelta());
   }
 
   @Test
   public void test()
   {
     String[] mds = getMessageDigests();
     Security.addProvider(new JarsyncProvider());
     Configuration conf = new Configuration();
 
     // Make sure we use our MD4 at least once!
     try
       {
         conf.strongSum = MessageDigest.getInstance("MD4", "JARSYNC");
       } catch (Exception x)
       {
         throw new Error(x);
       }
     conf.strongSumLength = conf.strongSum.getDigestLength();
 
     for (int i = 0; i < 50; i++)
       {
         conf.blockLength = rand.nextInt(1400) + 250;
         conf.weakSum = new Checksum32();
         byte[] n3w = new byte[rand.nextInt(1000000) + 500];
         rand.nextBytes(n3w);
         byte[] old = null;
         try
           {
             old = mutate(n3w);
           } catch (Exception x)
           {
             throw new Error(x);
           }
         log.debug("TEST #" + (i + 1) + ": old data=" + old.length
             + " bytes, target data=" + n3w.length + " bytes, blocks="
             + conf.blockLength + " bytes, digest="
             + conf.strongSum.getAlgorithm());
         Generator gen = new Generator(conf);
         List sums = gen.generateSums(old);
         log.debug("\tGenerated " + sums.size() + " checksums.");
        deltas = new LinkedList();
         MatcherStream mat = new MatcherStream(conf);
         mat.addListener(this);
         mat.setChecksums(sums);
         try
           {
             mat.update(n3w);
             mat.doFinal();
           } catch (ListenerException wontHappen)
           {
           }
         int copies = 0, inserts = 0;
        for (Iterator it = deltas.iterator(); it.hasNext();)
           {
             if (it.next() instanceof DataBlock)
               inserts++;
             else
               copies++;
           }
         log.debug("\tDeltas: " + copies + " copy commands, " + inserts
             + " insert commands.");
         byte[] reconst = Rebuilder.rebuild(old, deltas);
         Assert.assertTrue(Arrays.equals(n3w, reconst));
         if (!Arrays.equals(n3w, reconst))
           {
             // Try to see why.
             if (n3w.length != reconst.length)
               {
                 log.debug("\tThe arrays are not of equal length!");
                 log.debug("\t    n3w=" + n3w.length);
                 log.debug("\treconst=" + reconst.length);
                 log.debug("\tThe two differ in size by "
                     + (n3w.length - reconst.length) + " bytes.");
               } else
               {
                 for (int x = 0; x < n3w.length; x++)
                   {
                     if (n3w[x] != reconst[x])
                       {
                         log.debug("diff: index=" + x + " n3w="
                             + Util.toHexString(n3w, x, 1) + " reconst="
                             + Util.toHexString(reconst, x, 1)
                             // + " " + y +
                             // " contiguous mismatched blocks so far."
                             + " blockLength=" + conf.blockLength);
                       }
                   }
               }
           }
         try
           {
             conf.strongSum = MessageDigest.getInstance(mds[rand
                 .nextInt(mds.length)]);
             conf.strongSumLength = conf.strongSum.getDigestLength();
           } catch (Exception x)
           {
             throw new Error(x);
           }
       }
   }
 
   // Own methods.
   // -----------------------------------------------------------------------
 
   /**
    * Derived from `mutate.pl' from librsync, (C) 1999, 2000 by Martin Pool and
    * (C) 1999 by Andrew Tridgell.
    */
   private byte[] mutate(byte[] b) throws Exception
   {
     StringBuffer corpus = new StringBuffer(new String(b, "ISO-8859-1"));
     int nmuts = 1 + rand.nextInt(30);
     log.debug("\t" + nmuts + " mutations");
     while (--nmuts > 0)
       {
         int from_off = rand.nextInt(corpus.length());
         int from_len = (int) (rand.nextDouble() * rand.nextInt(corpus.length()
             - from_off));
         int to_off = rand.nextInt(corpus.length());
         int to_len = (int) (rand.nextDouble() * rand.nextInt(corpus.length()
             - to_off));
         switch (rand.nextInt(3))
           {
           case 0:
             log.debug("\tcopy and overwrite (" + from_off + ", " + from_len
                 + ") -> (" + to_off + ", " + to_len + ")");
             corpus.replace(to_off, to_off + to_len,
                 corpus.substring(from_off, from_off + from_len));
             break;
           case 1:
             log.debug("\tcopy and insert (" + from_off + ", " + from_len
                 + ") -> (" + to_off + ", " + to_len + ")");
             corpus.insert(to_off,
                 corpus.substring(from_off, from_off + from_len));
             break;
           case 2:
             log.debug("\tdelete (" + from_off + ", " + from_len + ")");
             corpus.delete(from_off, from_off + from_len);
             break;
           }
       }
     return corpus.toString().getBytes("ISO-8859-1");
   }
 
   private String[] getMessageDigests()
   {
     Set<String> algs = new HashSet<String>();
     String[] tries = { "md2", "md4", "md5", "sha-1", "ripemd128", "ripemd160",
         "tiger", "whirlpool", "brokenmd4" };
     for (int i = 0; i < tries.length; i++)
       {
         try
           {
             MessageDigest.getInstance(tries[i]);
             algs.add(tries[i]);
           } catch (Exception x)
           {
           }
       }
     return algs.toArray(new String[algs.size()]);
   }
 }
