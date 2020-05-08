 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.model;
 
 import com.trollsahead.qcumberless.engine.Engine;
 import com.trollsahead.qcumberless.util.Util;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Step {
     private static final String PARAMETER_TAG = "\\(\\.\\*\\)";
 
     private String definition;
     private boolean isMatched;
 
     private List<StepDefinitionHook> hooks = new ArrayList<StepDefinitionHook>();
 
     private List<CucumberStepPart> parts = null;
     private boolean renderKeyword = true;
 
     private boolean textDirty = true;
     private int renderWidth = 0;
 
     public Step(String definition) {
         this(definition, true);
     }
     
     public Step(StepDefinition stepDefinition) {
         this(stepDefinition.getStepDefinition(), true);
         for (StepDefinitionHook parameter : stepDefinition.getHooks()) {
             hooks.add(parameter);
         }
         resetParametersToDefault();
         parts = null;
     }
 
     public Step(String definition, boolean isMatched) {
         this.definition = Util.stripLeadingSpaces(definition);
         this.isMatched = isMatched;
     }
 
     public Step(Step step, String line) {
         this.definition = Util.stripLeadingSpaces(step.definition);
         this.hooks = step.hooks;
         this.parts = null;
         findParameters(Util.stripLeadingSpaces(line));
         findParts();
         isMatched = true;
     }
 
     public Step duplicate() {
         Step step = new Step(definition);
         step.renderKeyword = this.renderKeyword;
         step.isMatched = this.isMatched;
         step.textDirty = true;
         for (StepDefinitionHook hook : this.hooks) {
             step.hooks.add(hook.duplicate());
         }
         step.parts = new LinkedList<CucumberStepPart>();
         for (CucumberStepPart part : this.parts) {
             step.parts.add(part.duplicate(step));
         }
         return step;
     }
 
     public String getDefinition() {
         return definition;
     }
 
     public boolean isTextDirty() {
         return textDirty;
     }
 
     private void findParameters(String line) {
         Matcher matcher = Pattern.compile(definition).matcher(Util.stripLeadingSpaces(line));
         matcher.find();
         if (matcher.groupCount() > 0) {
             for (int i = 0; i < matcher.groupCount(); i++) {
                 hooks.get(i).setActualParameter(matcher.group(i + 1));
             }
         }
         textDirty = true;
     }
 
     public boolean matchedByStepDefinition() {
         return isMatched;
     }
 
     public void setMatchedByStepDefinition(boolean b) {
         isMatched = b;
     }
 
     public List<CucumberStepPart> getParts() {
         if (parts == null) {
             findParts();
         }
         return parts;
     }
 
     private void resetParametersToDefault() {
         for (StepDefinitionHook hook : hooks) {
             hook.setActualParameter(hook.getValidParameters()[0]);
         }
         textDirty = true;
         updateRenderKeyword();
     }
 
     private void findParts() {
         parts = new ArrayList<CucumberStepPart>();
         String[] strings = definition.split(PARAMETER_TAG);
         int parameterIdx = 0;
         boolean isLastPartAnArgument = false;
         for (String str : strings) {
             if (!Util.isEmpty(str)) {
                 parts.add(new CucumberStepPart(this, CucumberStepPart.PartType.TEXT, str));
                 isLastPartAnArgument = false;
             }
             if (parameterIdx < hooks.size()) {
                 if (isLastPartAnArgument) {
                     parts.add(new CucumberStepPart(this, CucumberStepPart.PartType.TEXT, " "));
                 }
                 parts.add(new CucumberStepPart(this, CucumberStepPart.PartType.ARGUMENT, hooks.get(parameterIdx)));
                 parameterIdx++;
                 isLastPartAnArgument = true;
             }
         }
         updateRenderKeyword();
     }
 
     public boolean matches(String line) {
         return line.matches(definition);
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder();
         for (CucumberStepPart part : getParts()) {
             sb.append(part.getText());
         }
         return sb.toString();
     }
 
     public CucumberStepPart getFirstPart() {
         return getParts().get(0);
     }
 
     public void setShouldRenderKeyword(boolean renderKeyword) {
         this.renderKeyword = renderKeyword;
         updateRenderKeyword();
     }
 
     private void updateRenderKeyword() {
         if (parts == null) {
             return;
         }
         if (!renderKeyword) {
             boolean shouldRender = false;
             int partCount = 0;
             for (CucumberStepPart part : parts) {
                 part.isFirstPart = partCount < 2;
                 if (part.type == CucumberStepPart.PartType.ARGUMENT) {
                     part.render = shouldRender;
                     shouldRender = true;
                 } else {
                     part.render = true;
                 }
                 partCount++;
             }
         } else {
             boolean firstPart = true;
             for (CucumberStepPart part : parts) {
                 part.render = true;
                 part.isFirstPart = firstPart;
                 firstPart = false;
             }
         }
         textDirty = true;
     }
 
     public void setRenderWidth(int width) {
         if (renderWidth != width) {
             renderWidth = width;
             textDirty = true;
         }
     }
 
     public int getLastPartBottom() {
         if (parts != null && parts.size() > 0) {
             return parts.get(parts.size() - 1).endY;
         } else {
             return 0;
         }
     }
 
     public void setTextDirty(boolean textDirty) {
         this.textDirty = textDirty;
     }
 
     public static class CucumberStepPart {
         public static enum PartType {TEXT, ARGUMENT}
 
         private Step parentStep;
         public PartType type = PartType.TEXT;
         private String text;
         public StepDefinitionHook hook = null;
         
         public boolean isTouched = false;
         public boolean isFirstPart = false;
 
         public List<String> wrappedText;
 
         public int startX = 0;
         public int startY = 0;
         public int endX = 0;
         public int endY = 0;
 
         public boolean render = true;
 
         public CucumberStepPart(Step parent, PartType type, String text) {
             parentStep = parent;
             this.type = type;
             this.text = text;
             this.hook = null;
         }
 
         public CucumberStepPart(Step parent, PartType type, StepDefinitionHook hook) {
             parentStep = parent;
             this.type = type;
             this.text = hook.getActualParameter();
             this.hook = hook;
         }
 
         public StepDefinitionHook getValidParameters() {
             return hook;
         }
 
         public void wrapText(int startX, int startY) {
             if (!parentStep.isTextDirty()) {
                 return;
             }
             this.startX = startX;
             this.startY = startY;
             this.endX = startX;
             this.endY = startY;
             wrappedText = new LinkedList<String>();
             wrappedText.add("");
             if (!render) {
                 return;
             }
             if (Util.isEmpty(text)) {
                 this.endX += Engine.fontMetrics.stringWidth(" ");
                 return;
             }
             String[] lines = getText().split("\n");
             boolean addNewLine = false;
             int offsetX = startX;
             for (String line : lines) {
                 if (addNewLine) {
                     this.endX = 0;
                     this.endY += Engine.fontMetrics.getHeight();
                     offsetX = 0;
                     wrappedText.add("");
                 }
                 String[] words = type == PartType.TEXT ? line.split(" ") : new String[] {line};
                 for (int i = 0; i < words.length; i++) {
                     String space = i < words.length - 1 || line.endsWith(" ") ? " " : "";
                     offsetX = addWordToWrappedText(words[i] + space, parentStep.renderWidth, offsetX);
                 }
                 if (Util.isEmpty(wrappedText.get(0))) {
                     this.startX = 0;
                     this.startY += Engine.fontMetrics.getHeight();
                     wrappedText.remove(0);
                 }
                 addNewLine = true;
             }
         }
         
         private int addWordToWrappedText(String word, int width, int offsetX) {
             int index = wrappedText.size() - 1;
             String currentLine = wrappedText.get(index);
             endX = offsetX + Engine.fontMetrics.stringWidth(currentLine + word);
             if (endX < width) {
                 wrappedText.set(index, currentLine + word);
                 return offsetX;
             } else {
                 endX = Engine.fontMetrics.stringWidth(word);
                 endY += Engine.fontMetrics.getHeight();
                 wrappedText.add(word);
                 return 0;
             }
         }
 
         public CucumberStepPart duplicate(Step parent) {
             CucumberStepPart part = this.type == PartType.TEXT ? new CucumberStepPart(parent, this.type, this.text) : new CucumberStepPart(parent, this.type, this.hook.duplicate());
             part.wrappedText = new LinkedList<String>();
            if (this.wrappedText != null) {
                part.wrappedText.addAll(this.wrappedText);
            }
             part.isFirstPart = this.isFirstPart;
             part.startX = this.startX;
             part.startY = this.startY;
             part.endX = this.endX;
             part.endY = this.endY;
             part.render = this.render;
             return part;
         }
 
         public String getText() {
             return isFirstPart ? Util.firstCharUpperCase(text) : text;
         }
 
         public void setText(String text) {
             this.text = text;
             if (hook != null) {
                 hook.setActualParameter(text);
             }
             parentStep.textDirty = true;
         }
 
         public boolean matchesRegExp() {
             try {
                 return type == PartType.ARGUMENT && Pattern.compile("(" + hook.getRegExp() + ")").matcher(text).matches();
             } catch (Exception e) {
                 return false;
             }
         }
 
         public String getRegExp() {
             return hook.getRegExp();
         }
     }
 }
