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
 
 package org.vial.terminal;
 
 
 import jline.UnixTerminal;
 
 public class WrappedUnixTerminal extends UnixTerminal {
     private final UnixTerminal delegate;
 
     WrappedUnixTerminal(UnixTerminal delegate) throws Exception {
         this.delegate = delegate;
     }
 
     @Override
     public void init() throws Exception {
         getSettings().set("intr undef");
         //We want to be able to use CTRL-Z for undo
         getSettings().set("susp undef");
         //We want to be able to use CTRL-S for save
         getSettings().set("stop undef");
         //We want to be able to use CTRL-O for open
         getSettings().set("discard undef");
         //We want to be able to use CTRL-V for open
         getSettings().set("lnext undef");
     }
 
 
     @Override
     public void restore() throws Exception {
         delegate.reset();
     }

    @Override
    public void reset() throws Exception {
        init();
    }
 }
