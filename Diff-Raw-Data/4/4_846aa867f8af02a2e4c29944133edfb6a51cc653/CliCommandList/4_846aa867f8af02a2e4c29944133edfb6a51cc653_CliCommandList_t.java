 /* Copyright (c) 2011 Danish Maritime Authority
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.dma.commons.app;
 
 import static java.util.Objects.requireNonNull;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * 
  * @author Kasper Nielsen
  */
 public class CliCommandList {
 
     private final String name;
     private final Map<String, String> helpText = new LinkedHashMap<>();// keep registration order
     private final Map<String, Command> command = new LinkedHashMap<>();
     static final ThreadLocal<String> CLI_APP_NAME = new ThreadLocal<>();
 
     public CliCommandList(String name) {
         this.name = requireNonNull(name);
     }
 
     public final void add(Class<?> main, String name, String description) {
         final Method m;
         try {
             m = main.getMethod("main", String[].class);
         } catch (NoSuchMethodException e) {
             throw new IllegalArgumentException("Could not find a main(String[] args) method");
         }
         helpText.put(requireNonNull(name), requireNonNull(description));
         command.put(name, new Command() {
             public void execute(String[] args) throws Exception {
                m.invoke(null, (Object) args);
             }
         });
     }
 
     public final void add(String name, String description, Command cmd) {
         helpText.put(requireNonNull(name), requireNonNull(description));
         command.put(name, requireNonNull(cmd));
     }
 
     public final void invoke(String[] args) throws Exception {
         // So we have to write some custom code.
         ArrayList<String> list = new ArrayList<>(Arrays.asList(args));
         int cmdIndex = 0;
         for (;; cmdIndex++) {
             if (cmdIndex == list.size()) {
                 printError("No command specified");
             } else if (!list.get(cmdIndex).startsWith("-")) {
                 break;
             }
         }
         String cmd = list.get(cmdIndex);
         Command c = command.get(cmd);
         if (c == null) {
             printError("Unknown command specified: " + cmd);
         } else {
             list.remove(cmdIndex);
             args = list.toArray(new String[list.size()]);
             CLI_APP_NAME.set(name); // makes sure we use the name of this list, and not the original app name
             try {
                 c.execute(args);
             } finally {
                 CLI_APP_NAME.remove();
             }
         }
     }
 
     private void printError(String errorMessage) {
         System.out.println(errorMessage);
         System.out.println("The available " + name + " commands are:");
         int longest = helpText.keySet().iterator().next().length();
         for (String s : helpText.keySet()) {
             longest = Math.max(longest, s.length());
         }
         for (Map.Entry<String, String> e : helpText.entrySet()) {
             System.out.printf("    %-" + longest + "s  %s\n", e.getKey(), e.getValue());
         }
         System.exit(1);
     }
 
     public abstract static class Command {
         public abstract void execute(String[] args) throws Exception;
     }
 }
