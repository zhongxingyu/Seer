 /* Generated By:JavaCC: Do not edit this line. WikiParserTokenManager.java */
 /**
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Wiki.
  *
  * The Initial Developer of the Original Code is Technology Concepts
  * and Design, Inc.
  * Copyright (C) 2000 Technology Concepts and Design, Inc.  All
  * Rights Reserved.
  *
  * Contributor(s): Lane Sharman (OpenDoors Software)
  *                 Justin Wells (Semiotek Inc.)
  *                 Eric B. Ridge (Technology Concepts and Design, Inc.)
  *
  * Alternatively, the contents of this file may be used under the
  * terms of the GNU General Public License Version 2 or later (the
  * "GPL"), in which case the provisions of the GPL are applicable
  * instead of those above.  If you wish to allow use of your
  * version of this file only under the terms of the GPL and not to
  * allow others to use your version of this file under the MPL,
  * indicate your decision by deleting the provisions above and
  * replace them with the notice and other provisions required by
  * the GPL.  If you do not delete the provisions above, a recipient
  * may use your version of this file under either the MPL or the
  * GPL.
  *
  *
  * This product includes sofware developed by OpenDoors Software.
  *
  * This product includes software developed by Justin Wells and Semiotek Inc.
  * for use in the WebMacro ServletFramework (http://www.webmacro.org).
  */
 package org.tcdi.opensource.wiki.parser;
 import java.io.*;
 import org.tcdi.opensource.wiki.*;
 import org.tcdi.opensource.wiki.builder.*;
 
 public class WikiParserTokenManager implements WikiParserConstants
 {
   public  java.io.PrintStream debugStream = System.out;
   public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
 private final int jjStopStringLiteralDfa_0(int pos, long active0)
 {
    switch (pos)
    {
       case 0:
          if ((active0 & 0x40000L) != 0L)
          {
             jjmatchedKind = 32;
             return -1;
          }
          if ((active0 & 0x40020000L) != 0L)
             return 10;
          if ((active0 & 0x80000000L) != 0L)
          {
             jjmatchedKind = 32;
             return 0;
          }
          return -1;
       case 1:
          if ((active0 & 0x40000L) != 0L)
          {
             if (jjmatchedPos == 0)
             {
                jjmatchedKind = 32;
                jjmatchedPos = 0;
             }
             return -1;
          }
          if ((active0 & 0x80000000L) != 0L)
          {
             if (jjmatchedPos == 0)
             {
                jjmatchedKind = 32;
                jjmatchedPos = 0;
             }
             return 48;
          }
          return -1;
       case 2:
          if ((active0 & 0x80000000L) != 0L)
          {
             if (jjmatchedPos == 0)
             {
                jjmatchedKind = 32;
                jjmatchedPos = 0;
             }
             return 49;
          }
          if ((active0 & 0x40000L) != 0L)
          {
             if (jjmatchedPos == 0)
             {
                jjmatchedKind = 32;
                jjmatchedPos = 0;
             }
             return -1;
          }
          return -1;
       case 3:
          if ((active0 & 0x40000L) != 0L)
          {
             if (jjmatchedPos == 0)
             {
                jjmatchedKind = 32;
                jjmatchedPos = 0;
             }
             return -1;
          }
          if ((active0 & 0x80000000L) != 0L)
             return 49;
          return -1;
       default :
          return -1;
    }
 }
 private final int jjStartNfa_0(int pos, long active0)
 {
    return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0), pos + 1);
 }
 private final int jjStopAtPos(int pos, int kind)
 {
    jjmatchedKind = kind;
    jjmatchedPos = pos;
    return pos + 1;
 }
 private final int jjStartNfaWithStates_0(int pos, int kind, int state)
 {
    jjmatchedKind = kind;
    jjmatchedPos = pos;
    try { curChar = input_stream.readChar(); }
    catch(java.io.IOException e) { return pos + 1; }
    return jjMoveNfa_0(state, pos + 1);
 }
 private final int jjMoveStringLiteralDfa0_0()
 {
    switch(curChar)
    {
       case 42:
          jjmatchedKind = 10;
          return jjMoveStringLiteralDfa1_0(0x10000000L);
       case 45:
          return jjMoveStringLiteralDfa1_0(0x40000L);
       case 60:
          return jjStopAtPos(0, 13);
       case 62:
          return jjStopAtPos(0, 14);
       case 91:
          return jjMoveStringLiteralDfa1_0(0x80000000L);
       case 94:
          jjmatchedKind = 17;
          return jjMoveStringLiteralDfa1_0(0x40000000L);
       case 95:
          jjmatchedKind = 11;
          return jjMoveStringLiteralDfa1_0(0x20000000L);
       case 126:
          return jjStopAtPos(0, 12);
       default :
          return jjMoveNfa_0(7, 0);
    }
 }
 private final int jjMoveStringLiteralDfa1_0(long active0)
 {
    try { curChar = input_stream.readChar(); }
    catch(java.io.IOException e) {
       jjStopStringLiteralDfa_0(0, active0);
       return 1;
    }
    switch(curChar)
    {
       case 42:
          if ((active0 & 0x10000000L) != 0L)
             return jjStopAtPos(1, 28);
          break;
       case 45:
          return jjMoveStringLiteralDfa2_0(active0, 0x40000L);
       case 91:
          return jjMoveStringLiteralDfa2_0(active0, 0x80000000L);
       case 94:
          if ((active0 & 0x40000000L) != 0L)
             return jjStopAtPos(1, 30);
          break;
       case 95:
          if ((active0 & 0x20000000L) != 0L)
             return jjStopAtPos(1, 29);
          break;
       default :
          break;
    }
    return jjStartNfa_0(0, active0);
 }
 private final int jjMoveStringLiteralDfa2_0(long old0, long active0)
 {
    if (((active0 &= old0)) == 0L)
       return jjStartNfa_0(0, old0); 
    try { curChar = input_stream.readChar(); }
    catch(java.io.IOException e) {
       jjStopStringLiteralDfa_0(1, active0);
       return 2;
    }
    switch(curChar)
    {
       case 45:
          return jjMoveStringLiteralDfa3_0(active0, 0x40000L);
       case 91:
          return jjMoveStringLiteralDfa3_0(active0, 0x80000000L);
       default :
          break;
    }
    return jjStartNfa_0(1, active0);
 }
 private final int jjMoveStringLiteralDfa3_0(long old0, long active0)
 {
    if (((active0 &= old0)) == 0L)
       return jjStartNfa_0(1, old0); 
    try { curChar = input_stream.readChar(); }
    catch(java.io.IOException e) {
       jjStopStringLiteralDfa_0(2, active0);
       return 3;
    }
    switch(curChar)
    {
       case 45:
          return jjMoveStringLiteralDfa4_0(active0, 0x40000L);
       case 91:
          if ((active0 & 0x80000000L) != 0L)
             return jjStartNfaWithStates_0(3, 31, 49);
          break;
       default :
          break;
    }
    return jjStartNfa_0(2, active0);
 }
 private final int jjMoveStringLiteralDfa4_0(long old0, long active0)
 {
    if (((active0 &= old0)) == 0L)
       return jjStartNfa_0(2, old0); 
    try { curChar = input_stream.readChar(); }
    catch(java.io.IOException e) {
       jjStopStringLiteralDfa_0(3, active0);
       return 4;
    }
    switch(curChar)
    {
       case 45:
          if ((active0 & 0x40000L) != 0L)
             return jjStopAtPos(4, 18);
          break;
       default :
          break;
    }
    return jjStartNfa_0(3, active0);
 }
 private final void jjCheckNAdd(int state)
 {
    if (jjrounds[state] != jjround)
    {
       jjstateSet[jjnewStateCnt++] = state;
       jjrounds[state] = jjround;
    }
 }
 private final void jjAddStates(int start, int end)
 {
    do {
       jjstateSet[jjnewStateCnt++] = jjnextStates[start];
    } while (start++ != end);
 }
 private final void jjCheckNAddTwoStates(int state1, int state2)
 {
    jjCheckNAdd(state1);
    jjCheckNAdd(state2);
 }
 private final void jjCheckNAddStates(int start, int end)
 {
    do {
       jjCheckNAdd(jjnextStates[start]);
    } while (start++ != end);
 }
 private final void jjCheckNAddStates(int start)
 {
    jjCheckNAdd(jjnextStates[start]);
    jjCheckNAdd(jjnextStates[start + 1]);
 }
 static final long[] jjbitVec0 = {
    0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
 };
 private final int jjMoveNfa_0(int startState, int curPos)
 {
    int[] nextStates;
    int startsAt = 0;
    jjnewStateCnt = 48;
    int i = 1;
    jjstateSet[0] = startState;
    int j, kind = 0x7fffffff;
    for (;;)
    {
       if (++jjround == 0x7fffffff)
          ReInitRounds();
       if (curChar < 64)
       {
          long l = 1L << curChar;
          MatchLoop: do
          {
             switch(jjstateSet[--i])
             {
                case 10:
                case 9:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 15)
                      kind = 15;
                   jjCheckNAdd(9);
                   break;
                case 7:
                   if ((0xfc00ffffffffffffL & l) != 0L)
                   {
                      if (kind > 32)
                         kind = 32;
                   }
                   else if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddStates(0, 3);
                   if ((0x3ff000000000000L & l) != 0L)
                   {
                      if (kind > 23)
                         kind = 23;
                      jjCheckNAddStates(4, 8);
                   }
                   else if ((0x100000200L & l) != 0L)
                   {
                      if (kind > 27)
                         kind = 27;
                   }
                   else if (curChar == 13)
                      jjAddStates(9, 10);
                   else if (curChar == 10)
                   {
                      if (kind > 25)
                         kind = 25;
                      jjCheckNAddTwoStates(42, 44);
                   }
                   if (curChar == 32)
                      jjstateSet[jjnewStateCnt++] = 14;
                   break;
                case 49:
                case 1:
                   jjCheckNAddStates(11, 13);
                   break;
                case 48:
                   jjCheckNAddStates(11, 13);
                   break;
                case 3:
                   jjCheckNAddStates(14, 16);
                   break;
                case 6:
                   jjCheckNAddStates(17, 20);
                   break;
                case 11:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 16)
                      kind = 16;
                   jjstateSet[jjnewStateCnt++] = 11;
                   break;
                case 13:
                   if (curChar == 32)
                      jjstateSet[jjnewStateCnt++] = 14;
                   break;
                case 14:
                   if (curChar == 32 && kind > 26)
                      kind = 26;
                   break;
                case 15:
                   if ((0x100000200L & l) != 0L && kind > 27)
                      kind = 27;
                   break;
                case 16:
                   if ((0xfc00ffffffffffffL & l) != 0L && kind > 32)
                      kind = 32;
                   break;
                case 17:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 23)
                      kind = 23;
                   jjCheckNAddStates(4, 8);
                   break;
                case 18:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddTwoStates(18, 19);
                   break;
                case 20:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddTwoStates(20, 21);
                   break;
                case 21:
                   if (curChar == 46)
                      jjstateSet[jjnewStateCnt++] = 22;
                   break;
                case 22:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 19)
                      kind = 19;
                   jjCheckNAdd(23);
                   break;
                case 23:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 19)
                      kind = 19;
                   jjCheckNAddStates(21, 23);
                   break;
                case 24:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddTwoStates(24, 25);
                   break;
                case 25:
                   if (curChar == 58)
                      jjstateSet[jjnewStateCnt++] = 26;
                   break;
                case 26:
                  if ((0x87ffe06000000000L & l) == 0L)
                      break;
                   if (kind > 20)
                      kind = 20;
                   jjCheckNAddStates(24, 26);
                   break;
                case 27:
                  if ((0x87ffe06000000000L & l) == 0L)
                      break;
                   if (kind > 20)
                      kind = 20;
                   jjCheckNAdd(27);
                   break;
                case 28:
                  if ((0x87ffe06000000000L & l) != 0L)
                      jjCheckNAddTwoStates(28, 29);
                   break;
                case 29:
                   if (curChar == 40)
                      jjCheckNAdd(30);
                   break;
                case 30:
                   if ((0xfffffdffffffdbffL & l) != 0L)
                      jjCheckNAddTwoStates(30, 31);
                   break;
                case 31:
                   if (curChar == 41 && kind > 20)
                      kind = 20;
                   break;
                case 32:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 23)
                      kind = 23;
                   jjCheckNAdd(32);
                   break;
                case 33:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddStates(0, 3);
                   break;
                case 34:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddTwoStates(34, 35);
                   break;
                case 35:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 21)
                      kind = 21;
                   jjCheckNAddTwoStates(35, 36);
                   break;
                case 36:
                   if ((0x3ff000000000000L & l) == 0L)
                      break;
                   if (kind > 21)
                      kind = 21;
                   jjCheckNAddStates(27, 29);
                   break;
                case 37:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddTwoStates(37, 38);
                   break;
                case 38:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddStates(30, 32);
                   break;
                case 40:
                   if ((0x3ff000000000000L & l) != 0L)
                      jjCheckNAddStates(33, 36);
                   break;
                case 41:
                   if (curChar != 10)
                      break;
                   if (kind > 25)
                      kind = 25;
                   jjCheckNAddTwoStates(42, 44);
                   break;
                case 42:
                case 43:
                   if (curChar == 10 && kind > 24)
                      kind = 24;
                   break;
                case 44:
                   if (curChar == 13)
                      jjstateSet[jjnewStateCnt++] = 43;
                   break;
                case 45:
                   if (curChar == 13)
                      jjAddStates(9, 10);
                   break;
                case 46:
                   if (curChar == 10)
                      jjCheckNAddTwoStates(42, 44);
                   break;
                case 47:
                   if (curChar == 10 && kind > 25)
                      kind = 25;
                   break;
                default : break;
             }
          } while(i != startsAt);
       }
       else if (curChar < 128)
       {
          long l = 1L << (curChar & 077);
          MatchLoop: do
          {
             switch(jjstateSet[--i])
             {
                case 10:
                   if ((0x7fffffe07fffffeL & l) != 0L)
                   {
                      if (kind > 15)
                         kind = 15;
                      jjCheckNAdd(9);
                   }
                   else if (curChar == 64)
                   {
                      if (kind > 16)
                         kind = 16;
                      jjCheckNAdd(11);
                   }
                   break;
                case 7:
                   if ((0x7fffffe07fffffeL & l) != 0L)
                   {
                      if (kind > 23)
                         kind = 23;
                      jjCheckNAddStates(4, 8);
                   }
                   else if ((0xf8000001f8000001L & l) != 0L)
                   {
                      if (kind > 32)
                         kind = 32;
                   }
                   if ((0x7fffffeL & l) != 0L)
                      jjCheckNAddStates(0, 3);
                   else if (curChar == 94)
                      jjstateSet[jjnewStateCnt++] = 10;
                   else if (curChar == 91)
                      jjstateSet[jjnewStateCnt++] = 0;
                   if (curChar == 94)
                      jjCheckNAdd(9);
                   break;
                case 49:
                   if ((0xffffffffdfffffffL & l) != 0L)
                      jjCheckNAddStates(11, 13);
                   else if (curChar == 93)
                      jjstateSet[jjnewStateCnt++] = 4;
                   if (curChar == 93)
                      jjstateSet[jjnewStateCnt++] = 3;
                   break;
                case 48:
                   if ((0xffffffffdfffffffL & l) != 0L)
                      jjCheckNAddStates(11, 13);
                   else if (curChar == 93)
                      jjstateSet[jjnewStateCnt++] = 3;
                   break;
                case 0:
                   if (curChar == 91)
                      jjCheckNAddTwoStates(1, 2);
                   break;
                case 1:
                   if ((0xffffffffdfffffffL & l) != 0L)
                      jjCheckNAddStates(11, 13);
                   break;
                case 2:
                   if (curChar == 93)
                      jjstateSet[jjnewStateCnt++] = 3;
                   break;
                case 3:
                   if ((0xffffffffdfffffffL & l) != 0L)
                      jjCheckNAddStates(14, 16);
                   break;
                case 4:
                   if (curChar == 93 && kind > 9)
                      kind = 9;
                   break;
                case 5:
                   if (curChar == 93)
                      jjstateSet[jjnewStateCnt++] = 4;
                   break;
                case 6:
                   if ((0xffffffffdfffffffL & l) != 0L)
                      jjCheckNAddStates(17, 20);
                   break;
                case 8:
                   if (curChar == 94)
                      jjCheckNAdd(9);
                   break;
                case 9:
                   if ((0x7fffffe07fffffeL & l) == 0L)
                      break;
                   if (kind > 15)
                      kind = 15;
                   jjCheckNAdd(9);
                   break;
                case 11:
                   if ((0x7fffffe07fffffeL & l) == 0L)
                      break;
                   if (kind > 16)
                      kind = 16;
                   jjCheckNAdd(11);
                   break;
                case 12:
                   if (curChar == 94)
                      jjstateSet[jjnewStateCnt++] = 10;
                   break;
                case 16:
                   if ((0xf8000001f8000001L & l) != 0L && kind > 32)
                      kind = 32;
                   break;
                case 17:
                   if ((0x7fffffe07fffffeL & l) == 0L)
                      break;
                   if (kind > 23)
                      kind = 23;
                   jjCheckNAddStates(4, 8);
                   break;
                case 18:
                   if ((0x7fffffe07fffffeL & l) != 0L)
                      jjCheckNAddTwoStates(18, 19);
                   break;
                case 19:
                   if (curChar == 64)
                      jjCheckNAdd(20);
                   break;
                case 20:
                   if ((0x7fffffe07fffffeL & l) != 0L)
                      jjCheckNAddTwoStates(20, 21);
                   break;
                case 22:
                   if ((0x7fffffe07fffffeL & l) == 0L)
                      break;
                   if (kind > 19)
                      kind = 19;
                   jjCheckNAdd(23);
                   break;
                case 23:
                   if ((0x7fffffe07fffffeL & l) == 0L)
                      break;
                   if (kind > 19)
                      kind = 19;
                   jjCheckNAddStates(21, 23);
                   break;
                case 24:
                   if ((0x7fffffe07fffffeL & l) != 0L)
                      jjCheckNAddTwoStates(24, 25);
                   break;
                case 26:
                   if ((0x47fffffe87ffffffL & l) == 0L)
                      break;
                   if (kind > 20)
                      kind = 20;
                   jjCheckNAddStates(24, 26);
                   break;
                case 27:
                   if ((0x47fffffe87ffffffL & l) == 0L)
                      break;
                   if (kind > 20)
                      kind = 20;
                   jjCheckNAdd(27);
                   break;
                case 28:
                   if ((0x47fffffe87ffffffL & l) != 0L)
                      jjCheckNAddTwoStates(28, 29);
                   break;
                case 30:
                   jjAddStates(37, 38);
                   break;
                case 32:
                   if ((0x7fffffe07fffffeL & l) == 0L)
                      break;
                   if (kind > 23)
                      kind = 23;
                   jjCheckNAdd(32);
                   break;
                case 33:
                   if ((0x7fffffeL & l) != 0L)
                      jjCheckNAddStates(0, 3);
                   break;
                case 34:
                   if ((0x7fffffeL & l) != 0L)
                      jjCheckNAddTwoStates(34, 35);
                   break;
                case 35:
                   if ((0x7fffffe00000000L & l) == 0L)
                      break;
                   if (kind > 21)
                      kind = 21;
                   jjCheckNAddTwoStates(35, 36);
                   break;
                case 36:
                   if ((0x7fffffeL & l) == 0L)
                      break;
                   if (kind > 21)
                      kind = 21;
                   jjCheckNAddStates(27, 29);
                   break;
                case 37:
                   if ((0x7fffffeL & l) != 0L)
                      jjCheckNAddTwoStates(37, 38);
                   break;
                case 38:
                   if ((0x7fffffe00000000L & l) != 0L)
                      jjCheckNAddStates(30, 32);
                   break;
                case 39:
                   if (curChar == 96 && kind > 22)
                      kind = 22;
                   break;
                case 40:
                   if ((0x7fffffeL & l) != 0L)
                      jjCheckNAddStates(33, 36);
                   break;
                default : break;
             }
          } while(i != startsAt);
       }
       else
       {
          int i2 = (curChar & 0xff) >> 6;
          long l2 = 1L << (curChar & 077);
          MatchLoop: do
          {
             switch(jjstateSet[--i])
             {
                case 7:
                   if ((jjbitVec0[i2] & l2) != 0L && kind > 32)
                      kind = 32;
                   break;
                case 49:
                case 1:
                   if ((jjbitVec0[i2] & l2) != 0L)
                      jjCheckNAddStates(11, 13);
                   break;
                case 48:
                   if ((jjbitVec0[i2] & l2) != 0L)
                      jjCheckNAddStates(11, 13);
                   break;
                case 3:
                   if ((jjbitVec0[i2] & l2) != 0L)
                      jjCheckNAddStates(14, 16);
                   break;
                case 6:
                   if ((jjbitVec0[i2] & l2) != 0L)
                      jjCheckNAddStates(17, 20);
                   break;
                case 30:
                   if ((jjbitVec0[i2] & l2) != 0L)
                      jjAddStates(37, 38);
                   break;
                default : break;
             }
          } while(i != startsAt);
       }
       if (kind != 0x7fffffff)
       {
          jjmatchedKind = kind;
          jjmatchedPos = curPos;
          kind = 0x7fffffff;
       }
       ++curPos;
       if ((i = jjnewStateCnt) == (startsAt = 48 - (jjnewStateCnt = startsAt)))
          return curPos;
       try { curChar = input_stream.readChar(); }
       catch(java.io.IOException e) { return curPos; }
    }
 }
 static final int[] jjnextStates = {
    34, 35, 37, 38, 18, 19, 24, 25, 32, 46, 47, 1, 2, 5, 2, 5, 
    6, 1, 2, 5, 6, 20, 21, 23, 27, 28, 29, 34, 35, 36, 38, 39, 
    40, 37, 38, 39, 40, 30, 31, 
 };
 public static final String[] jjstrLiteralImages = {
 "", null, null, null, null, null, null, null, null, null, "\52", "\137", 
 "\176", "\74", "\76", null, null, "\136", "\55\55\55\55\55", null, null, null, null, 
 null, null, null, null, null, "\52\52", "\137\137", "\136\136", "\133\133\133\133", 
 null, };
 public static final String[] lexStateNames = {
    "DEFAULT", 
 };
 private SimpleCharStream input_stream;
 private final int[] jjrounds = new int[48];
 private final int[] jjstateSet = new int[96];
 protected char curChar;
 public WikiParserTokenManager(SimpleCharStream stream)
 {
    if (SimpleCharStream.staticFlag)
       throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
    input_stream = stream;
 }
 public WikiParserTokenManager(SimpleCharStream stream, int lexState)
 {
    this(stream);
    SwitchTo(lexState);
 }
 public void ReInit(SimpleCharStream stream)
 {
    jjmatchedPos = jjnewStateCnt = 0;
    curLexState = defaultLexState;
    input_stream = stream;
    ReInitRounds();
 }
 private final void ReInitRounds()
 {
    int i;
    jjround = 0x80000001;
    for (i = 48; i-- > 0;)
       jjrounds[i] = 0x80000000;
 }
 public void ReInit(SimpleCharStream stream, int lexState)
 {
    ReInit(stream);
    SwitchTo(lexState);
 }
 public void SwitchTo(int lexState)
 {
    if (lexState >= 1 || lexState < 0)
       throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
    else
       curLexState = lexState;
 }
 
 private final Token jjFillToken()
 {
    Token t = Token.newToken(jjmatchedKind);
    t.kind = jjmatchedKind;
    String im = jjstrLiteralImages[jjmatchedKind];
    t.image = (im == null) ? input_stream.GetImage() : im;
    t.beginLine = input_stream.getBeginLine();
    t.beginColumn = input_stream.getBeginColumn();
    t.endLine = input_stream.getEndLine();
    t.endColumn = input_stream.getEndColumn();
    return t;
 }
 
 int curLexState = 0;
 int defaultLexState = 0;
 int jjnewStateCnt;
 int jjround;
 int jjmatchedPos;
 int jjmatchedKind;
 
 public final Token getNextToken() 
 {
   int kind;
   Token specialToken = null;
   Token matchedToken;
   int curPos = 0;
 
   EOFLoop :
   for (;;)
   {   
    try   
    {     
       curChar = input_stream.BeginToken();
    }     
    catch(java.io.IOException e)
    {        
       jjmatchedKind = 0;
       matchedToken = jjFillToken();
       return matchedToken;
    }
 
    jjmatchedKind = 0x7fffffff;
    jjmatchedPos = 0;
    curPos = jjMoveStringLiteralDfa0_0();
    if (jjmatchedKind != 0x7fffffff)
    {
       if (jjmatchedPos + 1 < curPos)
          input_stream.backup(curPos - jjmatchedPos - 1);
          matchedToken = jjFillToken();
          return matchedToken;
    }
    int error_line = input_stream.getEndLine();
    int error_column = input_stream.getEndColumn();
    String error_after = null;
    boolean EOFSeen = false;
    try { input_stream.readChar(); input_stream.backup(1); }
    catch (java.io.IOException e1) {
       EOFSeen = true;
       error_after = curPos <= 1 ? "" : input_stream.GetImage();
       if (curChar == '\n' || curChar == '\r') {
          error_line++;
          error_column = 0;
       }
       else
          error_column++;
    }
    if (!EOFSeen) {
       input_stream.backup(1);
       error_after = curPos <= 1 ? "" : input_stream.GetImage();
    }
    throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
   }
 }
 
 }
