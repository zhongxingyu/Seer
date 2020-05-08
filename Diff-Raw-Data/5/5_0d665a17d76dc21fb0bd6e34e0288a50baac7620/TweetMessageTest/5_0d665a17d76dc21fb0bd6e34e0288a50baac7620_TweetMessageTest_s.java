 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.komusubi.feeder.sns.twitter;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.rules.ExpectedException;
 import org.junit.runner.RunWith;
 import org.komusubi.feeder.model.ScriptLine;
 import org.komusubi.feeder.sns.twitter.TweetMessage.TweetScript;
 
 /**
  * @author jun.ozeki
  */
 @RunWith(Enclosed.class)
 public class TweetMessageTest {
 
     public static class TweetScriptTest {
         
         private TweetScript target;
         
         @Test
         public void trimedEndOfWhite() {
             // setup
             String expected = "sample text for trimed";
             String text = expected + " \n";
             target = new TweetScript(text);
             
             // exercise and verify
             assertThat(target.trimedLine(), is(expected));
         }
         
         @Test
         public void trimedStartOfWhite() {
             // setup
             String expected = "this is expected text buffer.";
             String text = "  \n\r" + expected;
             target = new TweetScript(text);
             
             // exercise and verify
             assertThat(target.trimedLine(), is(expected));
         }
         
     }
 
     public static class TweetMessageParentTest {
         @Rule public ExpectedException exception = ExpectedException.none();
         
         private TweetMessage target;
     
         @Before
         public void before() {
             target = new TweetMessage();
         }
     
         @Test
         public void null文字の追加時に例外が発生する事() {
             exception.expect(Twitter4jException.class);
             exception.expectMessage("line must NOT be null");
             // exercise
             target.append((String) null);
         }
         
         @Test
         public void 文字列追加時に分割して追加される事() {
             // setup
             String line = "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと";
             String expected1 = 
                           "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ";
             String expected2 = 
                           "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと";
             // exercise
             target.append(line);
             // verify
             assertThat(target.size(), is(2));
             assertThat(target.get(0).line(), is(expected1));
             assertThat(target.get(1).line(), is(expected2));
         }
         
         @Test
         public void chunkMaxLength() {
             // setup
             String line1 = "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ";
     
             String line2 = "アイウエオカキクケコサシスセソタチツテト";
             
             String expected1 = 
                           "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "アイウエオカキクケコサシスセソタチツテト";
             String expected2 =
                           "アイウエオカキクケコサシスセソタチツテト"
                         + "アイウエオカキクケコサシスセソタチツテト";
     
             target.append(line1);
             
             // exercise for chunk max size
             target.append(line2);
             target.append(line2);
             target.append(line2);
             
             // verify
             assertThat(target.size(), is(2));
             assertThat(target.get(0).line(), is(expected1));
             assertThat(target.get(1).line(), is(expected2));
         }
         
         @Test
         public void trimedWhite() {
             // setup 
             String text = "this message dose not have carrige return.";
             String expected = "\n" + text + "\n";
             target.append("\n");
             target.append(text);
             target.append("\n");
             
             // verify
             assertThat(target.text(), is(expected));
         }
         
         // add method chunk TweetScritp.MESSAGE_LENGTH_MAX and add list.
         @Test
         public void chunkAdd() {
             // setup
             String line = "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと";
 
             String expected1 = "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ"
                         + "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと"
                         + "なにぬねのはひふへほまみむめもやいゆえよ"
                         + "らりるれろアイウエオカキクケコサシスセソ";
 
             String expected2 = "タチツテトナニヌネノハヒフヘホマミムメモ"
                         + "あいうえおかきくけこさしすせそたちつてと";
             // exercise
             target.add(new ScriptLine(line));
             
             // verify
             assertThat(target.size(), is(2));
             assertThat(target.get(0).line(), is(expected1));
             assertThat(target.get(1).line(), is(expected2));
         }
         
         @Test
         public void splitOfLineFeedInScript() {
             // setup
             String expected1 = "パウダースノーの北海道。おトクがいっぱいのJAL SKI発売中！\n"
                             + "北海道のスキーツアーをご紹介。JALSKIならではのサービス・サポートでどなたでも安心快適なスキーツアーをお楽しみいただけます。国内ツアー・旅行ならJALパック。\n"
                            + "http://bit.ly/1gyKAed \n";
             String expected2 = "#jal";
             
             // exercise
            target.add(new ScriptLine(expected1 + expected2));
 
             // verify
             assertThat(target.size(), is(2));
             assertThat(target.get(0).line(), is(expected1)); 
             assertThat(target.get(1).line(), is(expected2)); 
         }
     }
 }
