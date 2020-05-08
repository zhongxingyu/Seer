 package com.appspot.manup.autograph;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.InterruptedIOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 
 import android.os.Process;
 import android.util.Log;
 
 /**
  *
  * Opens socket server.
  *
  * Can be killed by an interrupt
  *
  */
 public class SwipeServerThread extends Thread
 {
     private static final String TAG = SwipeServerThread.class.getSimpleName();
 
     private static final int PORT = 12345;
     private static final int BACKLOG = 10;
 
     private static final int READ_TIMEOUT_MILLISECONDS = 10000;
     private static final int SERVER_SOCKET_TIMEOUT_MILLISECONDS = 10000;
     private static final int SOCKET_TIMEOUT_MILLISECONDS = 1000;
 
     private final DataManager mSignatureDatabase;
 
     public SwipeServerThread(DataManager signatureDatabase)
     {
         super(TAG);
        mSignatureDatabase = signatureDatabase;
     } // SwipeServerThread
 
     @Override
     public void run()
     {
         Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
 
         try
         {
             handleRequests();
         } // try
         catch (final InterruptedException e)
         {
             Log.v(TAG, "Interrupted, now stopping.", e);
         } // catch
         catch (final IOException e)
         {
             Log.d(TAG, "An error occured while handling requests.", e);
         } // catch
     } // run
 
     private void handleRequests() throws IOException, InterruptedException
     {
         InetAddress hostInetAddress = NetworkUtils.getLocalIpAddress();
         if (hostInetAddress == null)
         {
             Log.e(TAG, "Could not get host inet address, aborting running server");
             return;
         } // if
         Log.d(TAG, "Host inet: " + hostInetAddress);
 
         ServerSocket serverSocket = null;
         try
         {
             serverSocket = new ServerSocket(PORT, BACKLOG, hostInetAddress);
             serverSocket.setSoTimeout(SERVER_SOCKET_TIMEOUT_MILLISECONDS);
 
             Log.d(TAG, "Server socket: " + serverSocket.getInetAddress() + ":"
                     + serverSocket.getLocalPort());
 
             // Terminates when handleRequest() is interrupted.
             for(;;)
             {
                 try
                 {
                     handleRequest(serverSocket);
                 } // try
                 catch (final IOException e)
                 {
                     Log.d(TAG, "Failed to handle request.", e);
                 } // catch
             } // while
         } // try
         finally
         {
             if (serverSocket != null)
             {
                 serverSocket.close();
             } // if
         } // finally
     } // handleRequests
 
     private void handleRequest(final ServerSocket serverSocket) throws InterruptedException,
             IOException
     {
         Socket socket = null;
         try
         {
             socket = waitForIncomingConnection(serverSocket);
             final String magStripeNumber = readMagStripeNumber(socket);
 
             if (magStripeNumber == null)
             {
                 Log.w(TAG, "Mag stripe number could not be read.");
                 return;
             } // if
 
             Log.d(TAG, "Mag stripe number: " + magStripeNumber);
 
             final boolean inserted = insertMagStripeNumber(magStripeNumber);
 
             if (!inserted)
             {
                 Log.e(TAG, "Failed to insert mag stripe number into database.");
             } // if
 
             writeResponse(socket, inserted);
         } // try
         finally
         {
             if (socket != null)
             {
                 socket.close();
             } // if
         } // finally
     } // handleRequest
 
     private Socket waitForIncomingConnection(final ServerSocket serverSocket)
             throws InterruptedException, IOException
     {
         Log.v(TAG, "Waiting for incoming connection...");
 
         Socket socket = null;
         do
         {
             try
             {
                 socket = serverSocket.accept();
             } // try
             catch (final InterruptedIOException e)
             {
                 // Timed out, loop.
                 Log.v(TAG, "Timed out while waiting for incoming connection.", e);
             } // catch
 
             if (Thread.interrupted())
             {
                 throw new InterruptedException(
                         "Interrupted while waiting for incoming connection.");
             } // if
         } while (socket == null);
 
         socket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
         return socket;
     } // read
 
     private String readMagStripeNumber(final Socket socket) throws IOException,
             InterruptedException
     {
         try
         {
             final BufferedReader input =
                     new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final long timeBeforeRead = System.currentTimeMillis();
             while (System.currentTimeMillis() - timeBeforeRead < READ_TIMEOUT_MILLISECONDS)
             {
                 try
                 {
                     return input.readLine();
                 } // try
                 catch (final SocketTimeoutException e)
                 {
                     Log.v(TAG, "Timed out before a line was read.", e);
                 } // catch
 
                 if (Thread.interrupted())
                 {
                     throw new InterruptedException("Interrupted while reading from socket.");
                 } // if
             } // while
             throw new SocketTimeoutException("Timed out before mag stripe was read.");
         } // try
         finally
         {
             socket.shutdownInput();
         } // finally
     } // readMagStripe
 
     private boolean insertMagStripeNumber(final String magStripeNumber)
     {
         return mSignatureDatabase.addMember(magStripeNumber) != -1;
     } // insertMagStripeNumber
 
     private void writeResponse(final Socket socket, final boolean inserted) throws IOException
     {
         try
         {
             final PrintWriter output = new PrintWriter(socket.getOutputStream());
             output.print("Ciao buddy");
             output.flush();
         } // try
         finally
         {
             socket.shutdownOutput();
         } // finally
     } // writeResponse
 
 } // SwipeServerThread
