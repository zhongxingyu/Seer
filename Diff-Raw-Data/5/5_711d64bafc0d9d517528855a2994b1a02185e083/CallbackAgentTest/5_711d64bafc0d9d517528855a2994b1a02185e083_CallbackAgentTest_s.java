 package org.thobe.agent;
 
 import java.lang.instrument.Instrumentation;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.concurrent.Exchanger;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import com.sun.tools.attach.VirtualMachineDescriptor;
 import org.junit.Rule;
 import org.junit.Test;
 import org.thobe.testing.subprocess.Subprocess;
 import org.thobe.testing.subprocess.TestProcesses;
 import sun.misc.Unsafe;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
import static org.thobe.testing.subprocess.TestProcesses.Option.AWAIT_STDERR_OUTPUT;
 
 public class CallbackAgentTest
 {
     @Rule
    public final TestProcesses subprocess = new TestProcesses( AWAIT_STDERR_OUTPUT );
 
     @Test
     public void shouldLoadAgentAndInvokeCallback() throws Exception
     {
         // given
         Callback callback = new Callback();
         CallbackAgent agent = new CallbackAgent( callback, Invoker.LOG_PARAMETERS );
         Subprocess process = subprocess.starter( LoopingProcess.class ).stdOut( null ).start();
 
         // when
         agent.injectInto( process.pid() );
 
         // then
         String call = callback.awaitCall( 5, SECONDS );
         assertFalse( "result should not be empty", call.isEmpty() );
         assertFalse( call, call.contains( "null" ) );
     }
 
     private enum Invoker implements CallbackInvoker<RemoteCallback>
     {
         LOG_PARAMETERS;
 
         @Override
         public void invokeCallback( RemoteCallback callback, Instrumentation instrumentation, Unsafe unsafe )
                 throws RemoteException
         {
             callback.invoke( String.valueOf( instrumentation ), String.valueOf( unsafe ) );
         }
 
         @Override
         public void attachFailed( RemoteCallback callback, VirtualMachineDescriptor descriptor, Throwable failure )
                 throws RemoteException
         {
             fail( failure.getMessage() );
         }
     }
 
     private static class Callback extends UnicastRemoteObject implements RemoteCallback
     {
         private final Exchanger<String> exchanger = new Exchanger<String>();
 
         Callback() throws RemoteException
         {
         }
 
         @Override
         public void invoke( String inst, String unsafe ) throws RemoteException
         {
             try
             {
                 exchanger.exchange( String.format( "invoke(%s,%s)", inst, unsafe ) );
             }
             catch ( InterruptedException e )
             {
                 throw new RemoteException( "Interrupted", e );
             }
         }
 
         String awaitCall( long time, TimeUnit unit ) throws InterruptedException, TimeoutException
         {
             return exchanger.exchange( null, time, unit );
         }
     }
 }
