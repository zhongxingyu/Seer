 /*
  * Copyright (c) 2006-2014 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.debug;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.interfaces.CommandController;
 
 import com.google.common.collect.Sets;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import static com.dmdirc.harness.CommandArgsMatcher.eqLine;
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Matchers.same;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class DebugTest {
 
     @Mock private CommandArguments arguments;
     @Mock private FrameContainer container;
     @Mock private DebugPlugin plugin;
     @Mock private CommandController controller;
     @Mock private DebugCommand debugCommand;
     @Mock private CommandContext commandContext;
     private Debug debug;
 
     @Before
     public void setup() {
         when(controller.getCommandChar()).thenReturn('/');
         when(debugCommand.getName()).thenReturn("test");
     }
 
     /** Checks the debug command with no arguments shows usage. */
     @Test
     public void testNoArgs() {
        debug = new Debug(controller, Sets.<DebugCommand>newHashSet());
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[0]);
         debug.execute(container, arguments, null);
        verify(container).addLine(eq("commandUsage"), anyString(),
                 eq("debug"), anyObject());
     }
 
     /** Checks the debug command with an invalid subcommand shows an error. */
     @Test
     public void testInvalidArg() {
         debug = new Debug(controller, Sets.<DebugCommand>newHashSet());
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[]{"test"});
 
         debug.execute(container, arguments, null);
         verify(container).addLine(eq("commandError"), anyString());
     }
 
     /** Checks the debug command executes a subcommand with no args. */
     @Test
     public void testCommandNoArgs() {
         debug = new Debug(controller, Sets.newHashSet(debugCommand));
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[]{"test"});
         when(arguments.getArgumentsAsString(1)).thenReturn("");
         when(debugCommand.getName()).thenReturn("test");
 
         debug.execute(container, arguments, commandContext);
 
         verify(container, never()).addLine(anyString(), anyString());
         verify(debugCommand).execute(same(container), eqLine("/test"), same(commandContext));
     }
 
     /** Checks the debug command executes a subcommand with args. */
     @Test
     public void testCommandWithArgs() {
         debug = new Debug(controller, Sets.newHashSet(debugCommand));
         when(arguments.isCommand()).thenReturn(true);
         when(arguments.getArguments()).thenReturn(new String[]{"test", "1", "2", "3"});
         when(arguments.getArgumentsAsString(1)).thenReturn("1 2 3");
         when(debugCommand.getName()).thenReturn("test");
 
         debug.execute(container, arguments, commandContext);
 
         verify(container, never()).addLine(anyString(), anyString());
         verify(debugCommand).execute(same(container), eqLine("/test 1 2 3"), same(commandContext));
     }
 
 }
