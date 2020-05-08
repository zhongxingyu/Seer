 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jledit.command.editor;
 
 import org.jledit.ConsoleEditor;
 import org.jledit.command.Command;
 
 import java.io.IOException;
 
 public class GoToCommand implements Command {
 
     private final ConsoleEditor editor;
     private final int line;
     private final int column;
 
     public GoToCommand(ConsoleEditor editor) {
         this(editor, 0, 0);
     }
 
     public GoToCommand(ConsoleEditor editor, int line, int column) {
         this.editor = editor;
         this.line = line;
         this.column = column;
     }
 
     @Override
     public void execute() {
         if (line == 0 || column == 0) {
             try {
                 int targetLine = 1;
                 int targetColumn = 1;
                 String[] coords = editor.readLine("Go to:").split(",");
                 if (coords.length == 1) {
                     targetLine = Integer.parseInt(coords[0]);
                }
                if (coords.length == 2) {
                     targetLine = Integer.parseInt(coords[0]);
                     targetColumn = Integer.parseInt(coords[1]);
                    editor.move(targetLine, targetColumn);
                 }
             } catch (Exception e) {
                 //noop
             }
         } else {
             editor.move(line, column);
         }
     }
 }
