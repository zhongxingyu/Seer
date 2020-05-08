 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com>
  */
 package de.weltraumschaf.commons;
 
 import de.weltraumschaf.commons.system.ExitCode;
 import de.weltraumschaf.commons.system.Exitable;
 import java.io.InputStream;
 import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 /**
  * Test cases for {@link InvokableAdapter}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class InvokableAdapterTest {
 
     private final String[] args = new String[] {"foo", "bar", "baz"};
 
     private final InvokableAdapter sut = new InvokableAdapter(args) {
 
         @Override
         public void execute() throws Exception {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
     };
 
     @Test public void mainWithCustomDefaultIo() throws Exception {
         final IOStreams io = IOStreams.newDefault();
         final Invokable app = mock(Invokable.class);
 
         InvokableAdapter.main(app, io);
         verify(app, times(1)).setIoStreams(io);
         verify(app, times(1)).init();
         verify(app, times(1)).execute();
         verify(app, times(1)).exit(0);
     }
 
     @Test public void mainWithDefaultIo() throws Exception {
         final Invokable app = mock(Invokable.class);
 
         InvokableAdapter.main(app);
         verify(app, times(1)).setIoStreams(any(IOStreams.class));
         verify(app, times(1)).init();
         verify(app, times(1)).execute();
         verify(app, times(1)).exit(0);
     }
 
     @Test public void mainWithExceptionInInit() throws Exception {
         final Invokable app = mock(Invokable.class);
         final String msg = "Error message";
         doThrow(new RuntimeException(msg)).when(app).init();
         final IOStreams io = new IOStreams(mock(InputStream.class),
                                            mock(PrintStream.class),
                                            mock(PrintStream.class));
 
         InvokableAdapter.main(app, io);
         verify(app, times(1)).setIoStreams(io);
         verify(app, times(1)).init();
         verify(io.getStderr(), times(1)).println(msg);
         verify(app, never()).execute();
         verify(app, times(1)).exit(-1);
     }
 
     @Test public void mainWithExceptionInExecute() throws Exception {
         final Invokable app = mock(Invokable.class);
         final String msg = "Error message";
         doThrow(new RuntimeException(msg)).when(app).execute();
         final IOStreams io = new IOStreams(mock(InputStream.class),
                                            mock(PrintStream.class),
                                            mock(PrintStream.class));
 
         InvokableAdapter.main(app, io);
         verify(app, times(1)).setIoStreams(io);
         verify(app, times(1)).init();
         verify(app, times(1)).execute();
         verify(io.getStderr(), times(1)).println(msg);
         verify(app, times(1)).exit(-1);
     }
 
     @Test public void getArgs() {
         assertArrayEquals(args, sut.getArgs());
         assertNotSame(args, sut.getArgs());
     }
 
    @Test public void getAndSetIo() throws UnsupportedEncodingException {
         assertNull(sut.getIoStreams());
         final IOStreams io = IOStreams.newDefault();
         sut.setIoStreams(io);
         assertSame(io, sut.getIoStreams());
     }
 
     @Test public void callExiter() {
         final Exitable exiter = mock(Exitable.class);
         sut.setExiter(exiter);
         InvokableAdapter.main(sut);
         verify(exiter, times(1)).exit(0);
 
         final ExitCode code = mock(ExitCode.class);
         sut.exit(code);
         verify(exiter, times(1)).exit(code);
     }
 
     @Test public void registerShutdownHooksOnInit() {
         final Runtime runtime = mock(Runtime.class);
         final ShutDownHook hook = mock(ShutDownHook.class);
         final InvokableAdapter _sut = new InvokableAdapter(args, runtime, hook) {
 
             @Override
             public void execute() throws Exception {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
 
         };
 
         _sut.init();
         verify(runtime, times(1)).addShutdownHook(hook);
     }
 
 
     @Test public void registerShutDownHook() {
         final Runtime runtime = mock(Runtime.class);
         final ShutDownHook hook = mock(ShutDownHook.class);
         final Runnable callback = mock(Runnable.class);
         final InvokableAdapter _sut = new InvokableAdapter(args, runtime, hook) {
 
             @Override
             public void execute() throws Exception {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
 
         };
 
         _sut.registerShutdownHook(callback);
         verify(hook, times(1)).register(callback);
     }
 
 }
