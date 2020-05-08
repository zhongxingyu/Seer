 /*********************************************************************************
  * Copyright (c) 2011, Monnet Project All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Monnet Project nor the names
  * of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *******************************************************************************
  */
 package eu.monnetproject.translation.topics;
 
 import eu.monnetproject.lang.Script;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Penn TreeBank Tokenization, based on original sed file
  * 
  * http://www.cis.upenn.edu/~treebank/tokenizer.sed
  * 
  * @author John McCrae
  */
 public class PTBTokenizer implements Tokenizer {
 
     private final void replaceAll(StringBuilder sb, String match, String replace) {
         final Pattern pattern = Pattern.compile(match);
         Matcher matcher = pattern.matcher(sb);
         int start = 0;
         while(matcher.find(start)) {
             final String replace2 = matcher.groupCount() > 0 ? 
                     matcher.groupCount() > 1 ?
                   //  matcher.groupCount() > 2 ?
                     //  replace.replaceAll("\\\\1", matcher.group(1)).replaceAll("\\\\2", matcher.group(2)).replaceAll("\\\\3", matcher.group(3)) :
                      replace.replace("\\1", matcher.group(1)).replace("\\2", matcher.group(2)) :
                      replace.replace("\\1", matcher.group(1)) :
                       replace;
             sb.replace(matcher.start(), matcher.end(), replace2);
             start = matcher.start() + replace.length();
             if(start >= sb.length()) {
                 break;
             } else {
                 matcher = pattern.matcher(sb);
             }
         }
     }
     
     @Override
     public List<String> tokenize(String input) {
         final StringBuilder sb = new StringBuilder(input);
         // Generic Latin Charset rules
         replaceAll(sb,"^\"","``");
         replaceAll(sb,"(?<=[ \\(\\[\\{\\<])\"","`` ");
         replaceAll(sb,"\\.\\.\\."," ... ");
         replaceAll(sb,"([,;:@#$%&])"," \\1 ");
         replaceAll(sb,"([^\\.])\\.([\\]\\)\\}\\>\"'\\s])","\\1 . \\2 ");
         replaceAll(sb,"([\\?\\!])"," \\1 ");
         replaceAll(sb,"([\\]\\[\\(\\)\\{\\}\\<\\>])"," \\1 ");
         replaceAll(sb,"--"," -- ");
         replaceAll(sb,"\""," '' ");
         replaceAll(sb,"([^'])' ","\\1 ' ");
         
         // English specific rules
         replaceAll(sb,"'([sSmMdD]) "," '\\1 ");
         replaceAll(sb,"'ll "," 'll ");
         replaceAll(sb,"'re "," 're ");
         replaceAll(sb,"'ve "," 've ");
         replaceAll(sb,"n't "," n't ");
         replaceAll(sb,"'LL "," 'LL ");
         replaceAll(sb,"'RE "," 'RE ");
         replaceAll(sb,"'VE "," 'VE ");
         replaceAll(sb,"N'T "," N'T ");
         replaceAll(sb,"([Cc])annot ","\\1an not ");
         replaceAll(sb,"([Dd])'ye ","\\' ye ");
         replaceAll(sb,"([Gg])imme ","\\1im me ");
         replaceAll(sb,"([Gg])onna ","\\1on na ");
         replaceAll(sb,"([Gg])otta ","\\1ot ta ");
         replaceAll(sb,"([Ll])emma ","\\1em me ");
         replaceAll(sb,"([Mm])ore'n ","\\1ore 'n ");
         replaceAll(sb,"'([Tt])is ","'\\1 is ");
         replaceAll(sb,"'([Tt])was ","'\\1 was ");
         replaceAll(sb,"([Ww])anna ","\\1an na ");
         replaceAll(sb,"([Ww])haddya ","\\1ha dd ya ");
         replaceAll(sb,"([Ww])hatcha ","\\1ha t cha ");
         
         // Clean up extra
         replaceAll(sb,"\\s{2,}"," ");
         replaceAll(sb,"^ +","");
         replaceAll(sb," +$","");
         
         // Format into a token list
         return Arrays.asList(sb.toString().split("\\s+"));
     }
 
     @Override
     public Script getScript() {
         return Script.LATIN;
     }
 
 }
