 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.inject.util.Providers;
 import com.google.jstestdriver.guice.DefaultThreadedActionProvider;
 import com.google.jstestdriver.hooks.ActionListProcessor;
 import com.google.jstestdriver.hooks.TestsPreProcessor;
 import com.google.jstestdriver.output.PrintXmlTestResultsAction;
 import com.google.jstestdriver.output.XmlPrinterImpl;
 import com.google.jstestdriver.output.XmlPrinter;
 
 import junit.framework.TestCase;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  * @author corysmith@google.com (Cory Smith)
  */
 public class DefaultActionListProviderTest extends TestCase {
 
   public void testParseFlagsAndCreateActionQueue() throws Exception {
     List<String> browsers = Arrays.asList("browser");
     DefaultActionListProvider parser =
         createProvider(browsers, 9876, null, false, Collections.<String> emptyList(), Collections
             .<ActionListProcessor> emptySet(), "", null);
     List<Action> actions = parser.get();
 
     ArrayList<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
     expectedActions.add(ServerStartupAction.class);
     expectedActions.add(BrowserStartupAction.class);
     expectedActions.add(FailureCheckerAction.class);
     assertSequence(expectedActions, actions);
     assertEquals(browsers, findAction(actions, BrowserStartupAction.class).getBrowserPath());
   }
   
   public void testProcessCreateActionQueue() throws Exception {
     final List<String> browsers = Arrays.asList("browser");
     final List<Action> expectedActions = Lists.<Action>newArrayList(
         new BrowserShutdownAction(null));
 
     final Set<ActionListProcessor> processors =
         Sets.<ActionListProcessor>newHashSet(new ActionListProcessor() {
       public List<Action> process(List<Action> actions) {
         assertEquals(browsers, findAction(actions, BrowserStartupAction.class).getBrowserPath());
         return expectedActions;
       }
     });
 
     DefaultActionListProvider parser =
         createProvider(browsers, 9876, null, false, Collections.<String> emptyList(), processors, "", null);
     assertSame(expectedActions,parser.get());
   }
 
   private DefaultActionListProvider createProvider(List<String> browsers,
                                                    int port,
                                                    String serverAddress,
                                                    boolean reset,
                                                    List<String> tests,
                                                    Set<ActionListProcessor> processors,
                                                    String testOutput, XmlPrinter xmlPrinter) {
     ActionFactory actionFactory =
         new ActionFactory(null, Collections.<TestsPreProcessor>emptySet());
     return new DefaultActionListProvider(
         actionFactory,
         null,
         tests,
         Collections.<String>emptyList(),
         browsers,
         reset,
         Collections.<String>emptyList(),
         false,
         port,
         Collections.<FileInfo>emptySet(),
         serverAddress,
         testOutput,
         null,
         new DefaultThreadedActionProvider(actionFactory, null, reset, Collections
             .<String> emptyList(), false, tests, Collections.<String> emptyList()),
         Providers.<JsTestDriverClient>of(null),
         Providers.<URLTranslator>of(null), Providers.<URLRewriter>of(null),
         new FailureAccumulator(),
         processors,
         xmlPrinter);
   }
 
   public void testParseWithServerAndReset() throws Exception {
     String serverAddress = "http://otherserver:8989";
     DefaultActionListProvider parser =
         createProvider(Arrays.asList("browser1"), -1, serverAddress, true, Collections
             .<String> emptyList(), Collections.<ActionListProcessor>emptySet(), "", null);
 
     FlagsImpl flags = new FlagsImpl();
     flags.setServer(serverAddress);
     flags.setBrowser(Arrays.asList("browser1"));
     flags.setReset(true);
 
     List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
     expectedActions.add(BrowserStartupAction.class);
     expectedActions.add(ThreadedActionsRunner.class);
     expectedActions.add(BrowserShutdownAction.class);
     expectedActions.add(FailureCheckerAction.class);
 
     List<Action> actions = parser.get();
     assertSequence(expectedActions, actions);
 
     ThreadedActionsRunner action = findAction(actions, ThreadedActionsRunner.class);
     assertEquals(1, action.getActions().size());
     ThreadedAction threadedAction = action.getActions().get(0);
     assertTrue("Expected ResetAction, found " + threadedAction,
         threadedAction instanceof ResetAction);
   }
 
   public void testParseFlagsWithServer() throws Exception {
     List<String> browserPaths = new ArrayList<String>();
     browserPaths.add("browser");
     browserPaths.add("browser2");
     String serverAddress = "http://otherserver:8989";
     DefaultActionListProvider parser =
         createProvider(browserPaths, -1, serverAddress, false, Collections.<String> emptyList(),
             Collections.<ActionListProcessor> emptySet(), "", null);
 
     List<Action> actions = parser.get();
 
     BrowserStartupAction action = findAction(actions, BrowserStartupAction.class);
     assertNotNull("Server action not created", action);
     assertEquals(serverAddress, action.getServerAddress());
   }
 
   public void testParseFlagsAndCreateTestActions() throws Exception {
     List<String> tests = Arrays.asList("foo.testBar");
     DefaultActionListProvider parser =
         createProvider(Arrays.asList("browser"), 9876, null, false, tests, Collections
             .<ActionListProcessor> emptySet(), "", null);
 
     List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
     expectedActions.add(ServerStartupAction.class);
     expectedActions.add(BrowserStartupAction.class);
     expectedActions.add(ThreadedActionsRunner.class);
     expectedActions.add(ServerShutdownAction.class);
     expectedActions.add(FailureCheckerAction.class);
 
     List<Action> actions = parser.get();
     assertSequence(expectedActions, actions);
 
     ThreadedActionsRunner testRunner = findAction(actions, ThreadedActionsRunner.class);
     assertNotNull("Test action not found", testRunner);
     assertEquals(1, testRunner.getActions().size());
     assertTrue(testRunner.getActions().get(0) instanceof RunTestsAction);
     assertEquals(tests, ((RunTestsAction) testRunner.getActions().get(0)).getTests());
   }
 
   public void testXmlTestResultsActionIsAddedIfTestOutputFolderIsSet() throws Exception {
     List<String> tests = Arrays.asList("foo.testBar");
     DefaultActionListProvider parser =
         createProvider(Arrays.asList("browser"), 9876, null, false, tests, Collections
             .<ActionListProcessor> emptySet(), ".", new XmlPrinterImpl(null, null));
 
     List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
     expectedActions.add(ServerStartupAction.class);
     expectedActions.add(BrowserStartupAction.class);
     expectedActions.add(ThreadedActionsRunner.class);
     expectedActions.add(PrintXmlTestResultsAction.class);
     expectedActions.add(ServerShutdownAction.class);
     expectedActions.add(FailureCheckerAction.class);
 
     List<Action> actions = parser.get();
     assertSequence(expectedActions, actions);
   }
 
   private void assertSequence(List<Class<? extends Action>> expectedActions,
       List<Action> actions) {
     List<Class<? extends Action>> actual = new ArrayList<Class<? extends Action>>();
     for (Action action : actions) {
       actual.add(action.getClass());
     }
     assertEquals(expectedActions, actual);
   }
 
   private <T extends Action> T findAction(List<Action> actions, Class<T> type) {
     for (Action action: actions) {
       if (type.isInstance(action)) {
         return type.cast(action);
       }
     }
     return null;
   }
 }
