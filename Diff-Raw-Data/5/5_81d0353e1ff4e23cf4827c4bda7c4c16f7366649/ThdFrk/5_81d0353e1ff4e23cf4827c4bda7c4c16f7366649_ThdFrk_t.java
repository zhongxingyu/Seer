 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 //    mp_bench - multiprocessing benchmarks for string handling
 //
 //    Copyright (C) 2009, 2010, Robin R Anderson
 //    roboprog@yahoo.com
 //    PO 1608
 //    Shingle Springs, CA 95682
 //
 //    This program is free software: you can redistribute it and/or modify
 //    it under the terms of the GNU Lesser General Public License as published by
 //    the Free Software Foundation, either version 3 of the License, or
 //    (at your option) any later version.
 //
 //    This program is distributed in the hope that it will be useful,
 //    but WITHOUT ANY WARRANTY; without even the implied warranty of
 //    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //    GNU General Public License for more details.
 //
 //    You should have received a copy of the GNU Lesser General Public License
 //    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 /**
  * Test java thread (someday: fork) performance relative to
  *  Perl / Python.
  * No, it's not particularly OOP-ish, just a "static" recreation of the scripting.
  */
 public
 class                   ThdFrk
     {
 
 	/** non-reentrant data/code */
     private static final
     SimpleDateFormat    threadDangerousVar = new SimpleDateFormat();
 
     /**
      * main program logic:  spawn crud to see what happens
      */
     public static
     void                main
         (
         String []       argv
         )
         {
         String mode = argv[ 0 ];
         String cnt = argv[ 1 ];
         if ( mode.toUpperCase().equals( "T") )
             {
             doThreads( Integer.parseInt( cnt) );
             }  // threads?
         else if ( mode.toUpperCase().equals( "F") )
             {
             // TODO:  doForks( Integer.parseInt( cnt) );
             throw new RuntimeException( "TODO");
             }  // forks?
         else if ( mode.toUpperCase().equals( "S") )
             {
             doSequence( Integer.parseInt( cnt) );
             }  // sequential processing?
         else
             {
             throw new RuntimeException(
                     "Arg 1 must be T (thread), F (fork), or S (sequential)");
             }
         }
 
     /** test thread based concurrency */
     private static
     void                doThreads
         (
         int             cnt
         )
         {
         while ( ( cnt--) > 0)
 
             {
             Thread t = new Thread( new Runnable()
                 {
                 public void run()
                     {
                     serviceThread();
                     }
                 });
             t.run();
             }  // lob off each slave to process "request"
 
         }
 
     /** pretend to provide a useful service for thread testing */
     private static
     void                serviceThread()
         {
		String timestamp;

         // force a shared data situation, however contrived
         synchronized( threadDangerousVar)
             {
             // contrived?  not so much: date formatter is not thread-safe
            timestamp = threadDangerousVar.format( new Date() );
             }
 
 		String buf = timestamp + " " + genPgTemplate() + "\n";
 
         System.out.print( buf);
 		// do not flush
         }
 
     /** test sequential processing for timing baseline */
     private static
     void                doSequence
         (
         int             cnt
         )
         {
         while ( ( cnt--) > 0)
 
             {
             serviceSequence();
             }  // lob off each slave to process "request"
 
         }
 
     /** pretend to provide a useful service for sequential testing */
     private static
     void                serviceSequence()
         {
         String timestamp = threadDangerousVar.format( new Date() );
 
 		String buf = timestamp + " " + genPgTemplate() + "\n";
 
         System.out.print( buf);
 		// do not flush
         }
 
     /** pretend to do something that would generate some CPU work */
     private static
     String              genPgTemplate()
         {
         String text = "<blah/>";
         for ( int cnt = 1; cnt <= 6; cnt++)
 
             {
             // deliberately *not* using StringBuffer / StringBuilder,
             //  as a real app would have many transient string variables
             text += text;
             }  // cat some crud up to thrash on cache
 
         return text;
         }
 
     }
 
 // *** EOF ***
