 package terminator;
 
 import java.awt.Component;
 import java.awt.event.*;
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 import terminator.util.*;
 
 public class TerminatorServer {
   public void parseCommandLine(PrintWriter out, String line) {
     List<String> arguments = new ArrayList<String>();
     for (String encoded : line.split(" "))
       arguments.add(decode(encoded));
     arguments.remove(0);
     TerminatorOpener opener = new TerminatorOpener(arguments, out);
     if (opener.showUsageIfRequested(out))
       return;
     TerminatorFrame window = opener.openFromBackgroundThread();
     if (window == null)
       return;
     waitForWindowToBeClosed(window);
   }
 
   private String decode(String encoded) {
     try {
       return URLDecoder.decode(encoded, "UTF-8");
     } catch (UnsupportedEncodingException e) {
       throw new RuntimeException(e);
     }
   }
 
  private static void waitForWindowToBeClosed(final Component window) {
     final CountDownLatch done = new CountDownLatch(1);
     // FIXME: Can this be simplified?
     window.addHierarchyListener(new HierarchyListener() {
      public void hierarchyChanged(HierarchyEvent e) {
         if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 &&
             !window.isShowing())
           done.countDown();
       }
     });
     try {
      done.await();
     } catch (InterruptedException e) {
       Log.warn("Waiting for window to be closed was interrupted", e);
     }
   }
 }
