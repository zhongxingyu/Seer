 /**
  * Copyright (C) 2008 Ovea <dev@testatoo.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import com.thoughtworks.selenium.DefaultSelenium;
 import com.thoughtworks.selenium.Selenium;
 import org.testatoo.selenium.server.SeleniumServer;
 import org.testatoo.selenium.server.SeleniumServerFactory;
 
 final class OurRunnerFluentAPI {
     private static SeleniumServer seleniumServer = null;
 
     public static void main(final String... args) throws Exception {
         Thread t = new Thread(new Runnable() {
             public void run() {
                seleniumServer = SeleniumServerFactory.configure()
                    .setPort(5555)
                    .setSingleWindow(true)
                    .create();
                 System.out.println("isRunning:" + seleniumServer.isRunning());
                 seleniumServer.start();
                 System.out.println("isRunning:" + seleniumServer.isRunning());
                 System.out.println("port:" + seleniumServer.getPort());
             }
         });
         t.start();
         Thread.sleep(5000);
         Selenium s = new DefaultSelenium("localhost", 5555, "*firefox", "http://www.amazon.ca/");
         s.start();
         s.open("/");
         Thread.sleep(5000);
         s.close();
         seleniumServer.stop();
         System.out.println("isRunning:" + seleniumServer.isRunning());
     }
 }
