 package com.github.signed.log.ui;
 
 import com.github.signed.log.core.LogEntry;
 import com.github.signed.log.list.SimpleLogModel;
 import com.github.signed.log.thread.LoggedThread;
 import lang.ArgumentClosure;
 import org.hamcrest.Matcher;
 import org.hamcrest.Matchers;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static com.github.signed.log.LogEntryBuilder.ofParts;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class SimpleLogModel_Test {
     private final SimpleLogModel logModel = new SimpleLogModel();
 
     @Test
     public void retrieveTheAvailableThreads() throws Exception {
         logEntryOnThread("one");
 
        assertThat(theThreadsKownByTheModel(), containsThreadsWithName("one"));
     }
 
     @Test
     public void removeDuplicatedThreadsWhileRetrieving() throws Exception {
         logEntryOnThread("one");
         logEntryOnThread("one");
        assertThat(theThreadsKownByTheModel(), containsThreadsWithName("one"));
     }
 
     private void logOnThread(LoggedThread thread) {
         LogEntry logEntry = ofParts(thread).build();
         logModel.addEntriesFrom(Collections.singletonList(logEntry));
     }
 
     private Matcher<Iterable<? extends LoggedThread>> containsThreadsWithName(String... names) {
         List<LoggedThread> threads = new ArrayList<>();
         for (String name : names) {
             threads.add(new LoggedThread(name));
         }
         return Matchers.contains(threads.toArray(new LoggedThread[threads.size()]));
     }
 
     @SuppressWarnings("unchecked")
    private List<LoggedThread> theThreadsKownByTheModel() {
         ArgumentClosure<List<LoggedThread>> closure = mock(ArgumentClosure.class);
         logModel.provideThreadChoicesTo(closure);
 
         return availableThreads(closure);
     }
 
     @SuppressWarnings("unchecked")
     private List<LoggedThread> availableThreads(ArgumentClosure<List<LoggedThread>> closure) {
         ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
         verify(closure).excecute(captor.capture());
         return captor.getValue();
     }
 
     private void logEntryOnThread(String thread) {
         logOnThread(new LoggedThread(thread));
     }
 }
