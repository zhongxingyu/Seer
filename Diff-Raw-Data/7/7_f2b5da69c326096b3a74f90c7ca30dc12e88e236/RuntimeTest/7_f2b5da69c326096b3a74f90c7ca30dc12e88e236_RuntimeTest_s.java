 package org.jasmine;
 
 import org.dynjs.runtime.modules.ModuleProvider;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InOrder;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.internal.debugging.MockitoDebuggerImpl;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.util.Set;
 
 import static com.google.common.collect.Lists.newArrayList;
 import static org.jasmine.Failure.Stack.stack;
 import static org.jasmine.Failure.failure;
 import static org.jasmine.Identifier.identifier;
 import static org.javafunk.funk.Lazily.repeat;
 import static org.javafunk.funk.Literals.setWith;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 
 @RunWith(MockitoJUnitRunner.class)
 public class RuntimeTest {
     @Mock
     Notifier notifier;
 
     @Before
     public void resetThreadLocalState() throws Throwable {
         ModuleProvider.clearCache();
     }
 
     @Test
     public void shouldExecuteTests() {
         final String expectedStack = "Error: Expected 'foo' to be 'bar'.\n" +
                "  at <native function: BuiltinError> (org/dynjs/runtime/builtins/types/BuiltinError.java:0)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:116)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:1252)\n" +
                 "  at <anonymous> (org/jasmine/failingSpec.js:3)\n" +
                 "  at <native function: Apply> (org/dynjs/runtime/builtins/types/function/prototype/Apply.java:0)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:1075)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2118)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2071)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2398)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2118)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2071)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2543)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2118)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2071)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:2165)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:806)\n" +
                 "  at <anonymous> (jasmine-jvm/executor.js:59)\n" +
                 "  at Object.run (jasmine-jvm/executor.js:7)\n" +
                 "  at <anonymous> (jasmine-jvm/executor.js:4)\n" +
                 "  at Object.setTimeout (jasmine-jvm/executor.js:20)\n" +
                 "  at <native function: Apply> (org/dynjs/runtime/builtins/types/function/prototype/Apply.java:0)\n" +
                 "  at <anonymous> (jasmine-1.3.1/jasmine.js:1750)\n" +
                 "  at Object.execute (jasmine-jvm/executor.js:63)\n" +
                "  at <native function: org.dynjs.runtime.modules.ClasspathModuleProvider> (jasmine-jvm/executor.js:1)\n" +
                "  at <native function: org.dynjs.runtime.builtins.Require> (org/dynjs/runtime/builtins/Require.java:0)\n" +
                 "  at <eval> (<eval>:1)\n" +
                 "  at <eval> (null:0)\n";
 
         Runtime runtime = new Runtime.Builder().specs("org/jasmine/failingSpec.js", "org/jasmine/fooSpec.js").build();
 
         runtime.execute(notifier);
 
         InOrder inOrder = Mockito.inOrder(notifier);
         inOrder.verify(notifier).started();
         inOrder.verify(notifier).fail(
                 identifier(0, 0), "should be failing", setWith(failure(identifier(0, 0), stack(expectedStack))));
         inOrder.verify(notifier).pass(identifier(1, 1), "should be foo");
         inOrder.verify(notifier).finished();
     }
 
     @Test
     public void shouldRunEachTestOnlyOnce() {
         Runtime runtime = new Runtime.Builder().specs("org/jasmine/fooSpec.js", "org/jasmine/fooSpec.js").build();
 
         runtime.execute(notifier);
 
         InOrder inOrder = Mockito.inOrder(notifier);
         inOrder.verify(notifier).started();
         inOrder.verify(notifier).pass(any(Identifier.class), anyString());
         inOrder.verify(notifier).finished();
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void shouldRunTestsMatchedByPattern() {
         Runtime runtime = new Runtime.Builder().scan("src/test/javascript/**/*Spec.js").build();
 
         runtime.execute(notifier);
 
         new MockitoDebuggerImpl().printInvocations(notifier);
         InOrder inOrder = Mockito.inOrder(notifier);
         inOrder.verify(notifier).started();
         inOrder.verify(notifier).fail(any(Identifier.class), anyString(), any(Set.class));
         inOrder.verify(notifier).pass(any(Identifier.class), anyString());
         inOrder.verify(notifier).finished();
     }
 
     @Test
     @Ignore
     public void shouldRunTestsFast() {
         Runtime runtime = new Runtime.Builder().specs(repeat(newArrayList("org/jasmine/fooSpec.js"), 1000000)).build();
 
         runtime.execute(notifier);
     }
 }
