 /**
  * Copyright (c) 2011 Jan Ehrhardt.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Jan Ehrhardt
  */
 package org.ducktools.eclipse.tycho.samples.ui.views;
 
 import static org.junit.Assert.assertEquals;
 
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
 import org.junit.Test;
 
 /**
  * @author <a href="https://github.com/derjan1982">Jan Ehrhardt</a>
  */
 public class HelloWorldViewTest {
 
   SWTWorkbenchBot bot = new SWTWorkbenchBot();
 
   @Test
   public void it_should_have_hello_world_as_title() {
     // given: a hello world view in the Java perspective
     bot.perspectiveById("org.eclipse.jdt.ui.JavaPerspective").activate();
    SWTBotView helloWorldView = bot.viewById("org.ducktools.eclipse.tycho.samples.ui.views.HelloWorldView");
 
     // expected: the title is 'Hello World"
    assertEquals("Hello World", helloWorldView.getTitle());
   }
 
 }
