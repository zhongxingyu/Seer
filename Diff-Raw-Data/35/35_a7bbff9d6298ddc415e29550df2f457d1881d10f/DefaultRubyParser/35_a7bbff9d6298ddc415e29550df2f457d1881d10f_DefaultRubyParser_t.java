 // created by jay 1.0.2 (c) 2002-2004 ats@cs.rit.edu
 // skeleton Java 1.0 (c) 2002 ats@cs.rit.edu
 
 					// line 2 "DefaultRubyParser.y"
 /***** BEGIN LICENSE BLOCK *****
  * Version: CPL 1.0/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Common Public
  * License Version 1.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.eclipse.org/legal/cpl-v10.html
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
  * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
  * Copyright (C) 2001-2004 Stefan Matthias Aust <sma@3plus4.de>
  * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
  * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
  * Copyright (C) 2004-2006 Thomas E Enebo <enebo@acm.org>
  * Copyright (C) 2004 Charles O Nutter <headius@headius.com>
  * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
  * 
  * Alternatively, the contents of this file may be used under the terms of
  * either of the GNU General Public License Version 2 or later (the "GPL"),
  * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the CPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the CPL, the GPL or the LGPL.
  ***** END LICENSE BLOCK *****/
 package org.jruby.parser;
 
 import java.io.IOException;
 
 import org.jruby.ast.AliasNode;
 import org.jruby.ast.ArgsNode;
 import org.jruby.ast.ArgumentNode;
 import org.jruby.ast.ArrayNode;
 import org.jruby.ast.AssignableNode;
 import org.jruby.ast.BackRefNode;
 import org.jruby.ast.BeginNode;
 import org.jruby.ast.BlockAcceptingNode;
 import org.jruby.ast.BlockArgNode;
 import org.jruby.ast.BlockNode;
 import org.jruby.ast.BlockPassNode;
 import org.jruby.ast.BreakNode;
 import org.jruby.ast.CallNode;
 import org.jruby.ast.CaseNode;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.ClassVarNode;
 import org.jruby.ast.Colon2Node;
 import org.jruby.ast.Colon3Node;
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.ast.DRegexpNode;
 import org.jruby.ast.DStrNode;
 import org.jruby.ast.DSymbolNode;
 import org.jruby.ast.DXStrNode;
 import org.jruby.ast.DefinedNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.DotNode;
 import org.jruby.ast.EnsureNode;
 import org.jruby.ast.EvStrNode;
 import org.jruby.ast.FCallNode;
 import org.jruby.ast.FloatNode;
 import org.jruby.ast.ForNode;
 import org.jruby.ast.GlobalVarNode;
 import org.jruby.ast.HashNode;
 import org.jruby.ast.IfNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.IterNode;
 import org.jruby.ast.ListNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.MultipleAsgnNode;
 import org.jruby.ast.NewlineNode;
 import org.jruby.ast.NextNode;
 import org.jruby.ast.NilNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.NotNode;
 import org.jruby.ast.OpAsgnAndNode;
 import org.jruby.ast.OpAsgnNode;
 import org.jruby.ast.OpAsgnOrNode;
 import org.jruby.ast.OpElementAsgnNode;
 import org.jruby.ast.PostExeNode;
 import org.jruby.ast.RedoNode;
 import org.jruby.ast.RegexpNode;
 import org.jruby.ast.RescueBodyNode;
 import org.jruby.ast.RescueNode;
 import org.jruby.ast.RetryNode;
 import org.jruby.ast.ReturnNode;
 import org.jruby.ast.SClassNode;
 import org.jruby.ast.SValueNode;
 import org.jruby.ast.SelfNode;
 import org.jruby.ast.SplatNode;
 import org.jruby.ast.StarNode;
 import org.jruby.ast.StrNode;
 import org.jruby.ast.SymbolNode;
 import org.jruby.ast.ToAryNode;
 import org.jruby.ast.UndefNode;
 import org.jruby.ast.UntilNode;
 import org.jruby.ast.VAliasNode;
 import org.jruby.ast.WhenNode;
 import org.jruby.ast.WhileNode;
 import org.jruby.ast.XStrNode;
 import org.jruby.ast.YieldNode;
 import org.jruby.ast.ZArrayNode;
 import org.jruby.ast.ZSuperNode;
 import org.jruby.ast.ZeroArgNode;
 import org.jruby.ast.ext.ElseNode;
 import org.jruby.ast.ext.PreExeNode;
 import org.jruby.ast.types.ILiteralNode;
 import org.jruby.ast.types.INameNode;
 import org.jruby.common.IRubyWarnings;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.lexer.yacc.ISourcePositionHolder;
 import org.jruby.lexer.yacc.LexState;
 import org.jruby.lexer.yacc.LexerSource;
 import org.jruby.lexer.yacc.RubyYaccLexer;
 import org.jruby.lexer.yacc.StrTerm;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.jruby.lexer.yacc.Token;
 import org.jruby.runtime.Visibility;
 import org.jruby.util.ByteList;
 import org.jruby.util.IdUtil;
 
 public class DefaultRubyParser {
     private ParserSupport support;
     private RubyYaccLexer lexer;
     private IRubyWarnings warnings;
 
     public DefaultRubyParser() {
         support = new ParserSupport();
         lexer = new RubyYaccLexer();
         lexer.setParserSupport(support);
     }
 
     public void setWarnings(IRubyWarnings warnings) {
         this.warnings = warnings;
 
         support.setWarnings(warnings);
         lexer.setWarnings(warnings);
     }
 					// line 152 "-"
   // %token constants
   public static final int kCLASS = 257;
   public static final int kMODULE = 258;
   public static final int kDEF = 259;
   public static final int kUNDEF = 260;
   public static final int kBEGIN = 261;
   public static final int kRESCUE = 262;
   public static final int kENSURE = 263;
   public static final int kEND = 264;
   public static final int kIF = 265;
   public static final int kUNLESS = 266;
   public static final int kTHEN = 267;
   public static final int kELSIF = 268;
   public static final int kELSE = 269;
   public static final int kCASE = 270;
   public static final int kWHEN = 271;
   public static final int kWHILE = 272;
   public static final int kUNTIL = 273;
   public static final int kFOR = 274;
   public static final int kBREAK = 275;
   public static final int kNEXT = 276;
   public static final int kREDO = 277;
   public static final int kRETRY = 278;
   public static final int kIN = 279;
   public static final int kDO = 280;
   public static final int kDO_COND = 281;
   public static final int kDO_BLOCK = 282;
   public static final int kRETURN = 283;
   public static final int kYIELD = 284;
   public static final int kSUPER = 285;
   public static final int kSELF = 286;
   public static final int kNIL = 287;
   public static final int kTRUE = 288;
   public static final int kFALSE = 289;
   public static final int kAND = 290;
   public static final int kOR = 291;
   public static final int kNOT = 292;
   public static final int kIF_MOD = 293;
   public static final int kUNLESS_MOD = 294;
   public static final int kWHILE_MOD = 295;
   public static final int kUNTIL_MOD = 296;
   public static final int kRESCUE_MOD = 297;
   public static final int kALIAS = 298;
   public static final int kDEFINED = 299;
   public static final int klBEGIN = 300;
   public static final int klEND = 301;
   public static final int k__LINE__ = 302;
   public static final int k__FILE__ = 303;
   public static final int tIDENTIFIER = 304;
   public static final int tFID = 305;
   public static final int tGVAR = 306;
   public static final int tIVAR = 307;
   public static final int tCONSTANT = 308;
   public static final int tCVAR = 309;
   public static final int tNTH_REF = 310;
   public static final int tBACK_REF = 311;
   public static final int tSTRING_CONTENT = 312;
   public static final int tINTEGER = 313;
   public static final int tFLOAT = 314;
   public static final int tREGEXP_END = 315;
   public static final int tUPLUS = 316;
   public static final int tUMINUS = 317;
   public static final int tUMINUS_NUM = 318;
   public static final int tPOW = 319;
   public static final int tCMP = 320;
   public static final int tEQ = 321;
   public static final int tEQQ = 322;
   public static final int tNEQ = 323;
   public static final int tGEQ = 324;
   public static final int tLEQ = 325;
   public static final int tANDOP = 326;
   public static final int tOROP = 327;
   public static final int tMATCH = 328;
   public static final int tNMATCH = 329;
   public static final int tDOT = 330;
   public static final int tDOT2 = 331;
   public static final int tDOT3 = 332;
   public static final int tAREF = 333;
   public static final int tASET = 334;
   public static final int tLSHFT = 335;
   public static final int tRSHFT = 336;
   public static final int tCOLON2 = 337;
   public static final int tCOLON3 = 338;
   public static final int tOP_ASGN = 339;
   public static final int tASSOC = 340;
   public static final int tLPAREN = 341;
   public static final int tLPAREN2 = 342;
   public static final int tRPAREN = 343;
   public static final int tLPAREN_ARG = 344;
   public static final int tLBRACK = 345;
   public static final int tRBRACK = 346;
   public static final int tLBRACE = 347;
   public static final int tLBRACE_ARG = 348;
   public static final int tSTAR = 349;
   public static final int tSTAR2 = 350;
   public static final int tAMPER = 351;
   public static final int tAMPER2 = 352;
   public static final int tTILDE = 353;
   public static final int tPERCENT = 354;
   public static final int tDIVIDE = 355;
   public static final int tPLUS = 356;
   public static final int tMINUS = 357;
   public static final int tLT = 358;
   public static final int tGT = 359;
   public static final int tPIPE = 360;
   public static final int tBANG = 361;
   public static final int tCARET = 362;
   public static final int tLCURLY = 363;
   public static final int tRCURLY = 364;
   public static final int tBACK_REF2 = 365;
   public static final int tSYMBEG = 366;
   public static final int tSTRING_BEG = 367;
   public static final int tXSTRING_BEG = 368;
   public static final int tREGEXP_BEG = 369;
   public static final int tWORDS_BEG = 370;
   public static final int tQWORDS_BEG = 371;
   public static final int tSTRING_DBEG = 372;
   public static final int tSTRING_DVAR = 373;
   public static final int tSTRING_END = 374;
   public static final int tRDOC = 375;
   public static final int tLOWEST = 376;
   public static final int tLAST_TOKEN = 377;
   public static final int yyErrorCode = 256;
 
   /** number of final state.
     */
   protected static final int yyFinal = 1;
 
   /** parser tables.
       Order is mandated by <i>jay</i>.
     */
   protected static final short[] yyLhs = {
 //yyLhs 494
     -1,   101,     0,    20,    19,    21,    21,    21,    21,   104,
     22,    22,    22,    22,    22,    22,    22,    22,    22,    22,
     22,   105,    22,    22,    22,    22,    22,    22,    22,    22,
     22,    22,    22,    22,    22,    22,    23,    23,    23,    23,
     23,    23,    27,    18,    18,    18,    18,    18,    43,    43,
     43,   106,    78,    26,    26,    26,    26,    26,    26,    26,
     26,    79,    79,    81,    81,    80,    80,    80,    80,    80,
     80,    51,    51,    67,    67,    52,    52,    52,    52,    52,
     52,    52,    52,    59,    59,    59,    59,    59,    59,    59,
     59,    91,    91,    17,    17,    17,    92,    92,    92,    92,
     92,    85,    85,    47,   108,    47,    93,    93,    93,    93,
     93,    93,    93,    93,    93,    93,    93,    93,    93,    93,
     93,    93,    93,    93,    93,    93,    93,    93,    93,    93,
     93,    93,   107,   107,   107,   107,   107,   107,   107,   107,
    107,   107,   107,   107,   107,   107,   107,   107,   107,   107,
    107,   107,   107,   107,   107,   107,   107,   107,   107,   107,
    107,   107,   107,   107,   107,   107,   107,   107,   107,   107,
    107,   107,   107,    24,    24,    24,    24,    24,    24,    24,
     24,    24,    24,    24,    24,    24,    24,    24,    24,    24,
     24,    24,    24,    24,    24,    24,    24,    24,    24,    24,
     24,    24,    24,    24,    24,    24,    24,    24,    24,    24,
     24,    24,    24,    24,    24,    24,    24,    53,    56,    56,
     56,    56,    56,    56,    36,    36,    36,    36,    41,    41,
     33,    33,    33,    33,    33,    33,    33,    33,    33,    34,
     34,    34,    34,    34,    34,    34,    34,    34,    34,    34,
     34,   111,    39,    35,   112,    35,   113,    35,    72,    71,
     71,    65,    65,    50,    50,    50,    25,    25,    25,    25,
     25,    25,    25,    25,    25,    25,   114,    25,    25,    25,
     25,    25,    25,    25,    25,    25,    25,    25,    25,    25,
     25,    25,    25,    25,   115,   117,    25,   118,   119,    25,
     25,    25,    25,   120,   121,    25,   122,    25,   123,   124,
     25,   125,    25,   126,    25,   127,   128,    25,    25,    25,
     25,    25,    28,    99,    99,    99,    99,   116,   116,   116,
     31,    31,    29,    29,    57,    57,    58,    58,    58,    58,
    129,    77,    42,    42,    42,    10,    10,    10,    10,    10,
     10,   130,    76,   131,    76,    54,    66,    66,    66,    30,
     30,    82,    82,    55,    55,    55,    32,    32,    38,    38,
     14,    14,    14,     2,     3,     3,     4,     5,     6,    11,
     11,    62,    62,    13,    13,    12,    12,    61,    61,     7,
      7,     8,     8,     9,   132,     9,   133,     9,    48,    48,
     48,    48,    87,    86,    86,    86,    86,    16,    15,    15,
     15,    15,    84,    84,    84,    84,    84,    84,    84,    84,
     84,    84,    84,    40,    83,    49,    49,    37,   134,    37,
     37,    44,    44,    45,    45,    45,    45,    45,    45,    45,
     45,    45,    94,    94,    94,    94,    63,    63,    46,    64,
     64,    97,    97,    95,    95,    98,    98,    75,    74,    74,
      1,   135,     1,    70,    70,    70,    68,    68,    69,    88,
     88,    88,    89,    89,    89,    89,    90,    90,    90,    96,
     96,   102,   102,   109,   109,   110,   110,   110,   100,   100,
    103,   103,    60,    73,
     }, yyLen = {
 //yyLen 494
      2,     0,     2,     4,     2,     1,     1,     3,     2,     0,
      4,     3,     3,     3,     2,     1,     3,     3,     3,     3,
      3,     0,     5,     4,     3,     3,     3,     6,     5,     5,
      5,     3,     3,     3,     3,     1,     1,     3,     3,     2,
      2,     1,     1,     1,     1,     2,     2,     2,     1,     4,
      4,     0,     5,     2,     3,     4,     5,     4,     5,     2,
      2,     1,     3,     1,     3,     1,     2,     3,     2,     2,
      1,     1,     3,     2,     3,     1,     4,     3,     3,     3,
      3,     2,     1,     1,     4,     3,     3,     3,     3,     2,
      1,     1,     1,     2,     1,     3,     1,     1,     1,     1,
      1,     1,     1,     1,     0,     4,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     1,     1,     3,     5,     3,     6,     5,     5,     5,
      5,     4,     3,     3,     3,     3,     3,     3,     3,     3,
      3,     4,     4,     2,     2,     3,     3,     3,     3,     3,
      3,     3,     3,     3,     3,     3,     3,     3,     2,     2,
      3,     3,     3,     3,     3,     5,     1,     1,     1,     2,
      2,     5,     2,     3,     3,     4,     4,     6,     1,     1,
      1,     2,     5,     2,     5,     4,     7,     3,     1,     4,
      3,     5,     7,     2,     5,     4,     6,     7,     9,     3,
      1,     0,     2,     1,     0,     3,     0,     4,     2,     2,
      1,     1,     3,     3,     4,     2,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     3,     0,     5,     3,     3,
      2,     4,     3,     3,     1,     4,     3,     1,     5,     2,
      1,     2,     6,     6,     0,     0,     7,     0,     0,     7,
      5,     4,     5,     0,     0,     9,     0,     6,     0,     0,
      8,     0,     5,     0,     6,     0,     0,     9,     1,     1,
      1,     1,     1,     1,     1,     1,     2,     1,     1,     1,
      1,     5,     1,     2,     1,     1,     1,     2,     1,     3,
      0,     5,     2,     4,     4,     2,     4,     4,     3,     2,
      1,     0,     5,     0,     5,     5,     1,     4,     2,     1,
      1,     6,     0,     1,     1,     1,     2,     1,     2,     1,
      1,     1,     1,     1,     1,     2,     3,     3,     3,     3,
      3,     0,     3,     1,     2,     3,     3,     0,     3,     0,
      2,     0,     2,     1,     0,     3,     0,     4,     1,     1,
      1,     1,     2,     1,     1,     1,     1,     3,     1,     1,
      2,     2,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     0,     4,
      2,     4,     2,     6,     4,     4,     2,     4,     2,     2,
      1,     0,     1,     1,     1,     1,     1,     3,     3,     1,
      3,     1,     1,     2,     1,     1,     1,     2,     2,     0,
      1,     0,     5,     1,     2,     2,     1,     3,     3,     1,
      1,     1,     1,     1,     1,     1,     1,     1,     1,     1,
      1,     0,     1,     0,     1,     0,     1,     1,     1,     1,
      1,     2,     0,     0,
     }, yyDefRed = {
 //yyDefRed 885
      1,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,   294,   297,     0,     0,     0,   320,   321,     0,
      0,     0,   418,   417,   419,   420,     0,     0,     0,    21,
      0,   422,   421,     0,     0,   414,   413,     0,   416,   425,
    426,   408,   409,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,   389,   391,   391,     0,     0,
     15,   267,     0,   374,   268,   269,     0,   270,   271,   266,
    370,   372,    36,     2,     0,     0,     0,     0,     0,     0,
      0,   272,     0,    44,     0,     0,    71,     0,     5,     0,
      0,    61,     0,     0,   371,     0,     0,   318,   319,   284,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    322,     0,   273,   423,     0,    94,   311,   141,   152,   142,
    165,   138,   158,   148,   147,   163,   146,   145,   140,   166,
    150,   139,   153,   157,   159,   151,   144,   160,   167,   162,
      0,     0,     0,     0,   137,   156,   155,   168,   169,   170,
    171,   172,   136,   143,   134,   135,     0,     0,     0,    98,
      0,   127,   128,   125,   109,   110,   111,   114,   116,   112,
    129,   130,   117,   118,   461,   122,   121,   108,   126,   124,
    123,   119,   120,   115,   113,   106,   107,   131,     0,   460,
    313,    99,   100,   161,   154,   164,   149,   132,   133,    96,
     97,     0,     0,   103,   102,   101,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,   489,   488,     0,
    490,     0,     0,     0,     0,     0,     0,     0,     0,   334,
    335,     0,     0,     0,     0,     0,   230,    46,     0,     0,
      0,   466,   238,    47,    45,     0,    60,     0,     0,   349,
     59,    39,     0,     9,   484,     0,     0,     0,   193,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,   218,     0,     0,   463,     0,     0,     0,     0,
      0,     0,    69,     0,   209,    40,   208,   405,   404,   406,
      0,   402,   403,     0,     0,     0,     0,     0,     0,     0,
    375,   353,   351,   291,     4,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,   340,
    342,     0,     0,     0,     0,     0,     0,    73,     0,     0,
      0,     0,     0,     0,   345,     0,   289,     0,   410,   411,
      0,    91,     0,    93,     0,   428,   306,   427,     0,     0,
      0,     0,     0,   479,   480,   315,     0,   104,     0,     0,
    275,     0,   325,   324,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,   491,     0,     0,
      0,     0,     0,     0,   303,     0,   258,     0,     0,   231,
    260,     0,   233,   286,     0,     0,   253,   252,     0,     0,
      0,     0,     0,    11,    13,    12,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,   278,     0,     0,
      0,   219,   282,     0,   486,   220,     0,   222,     0,   465,
    464,   283,     0,     0,     0,     0,   393,   396,   394,   407,
    392,   376,   390,   377,   378,   379,   380,   383,     0,   385,
      0,   386,     0,     0,     0,    16,    17,    18,    19,    20,
     37,    38,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    474,     0,     0,   475,     0,     0,     0,     0,   348,     0,
      0,   472,   473,     0,     0,    31,     0,     0,    24,     0,
     32,   261,     0,     0,    67,    74,    25,    34,     0,    26,
      0,    51,    54,     0,   430,     0,     0,     0,     0,     0,
      0,    95,     0,     0,     0,     0,     0,   443,   442,   444,
      0,   452,   451,   456,   455,     0,     0,   449,     0,     0,
    440,   446,     0,     0,     0,     0,   364,     0,     0,   365,
      0,     0,   332,     0,   326,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,   301,   329,   328,
    327,   295,   298,     0,     0,     0,     0,     0,     0,     0,
    237,   468,     0,     0,     0,   259,     0,     0,   467,   285,
      0,     0,   256,     0,     0,   250,     0,     0,     0,     0,
      0,   224,     0,    10,     0,     0,    23,     0,     0,     0,
      0,     0,   223,     0,   262,     0,     0,     0,     0,     0,
      0,     0,   382,   384,   388,   338,     0,     0,   336,     0,
      0,     0,     0,     0,   229,     0,   346,   228,     0,     0,
    347,     0,     0,    49,   343,    50,   344,   265,     0,     0,
     72,     0,   309,     0,     0,   281,   312,     0,   316,     0,
      0,     0,   432,     0,   436,     0,   438,     0,   439,   453,
    457,   105,     0,     0,   367,   333,     0,     3,   369,     0,
    330,     0,     0,     0,     0,     0,     0,   300,   302,   358,
      0,     0,     0,     0,     0,     0,     0,     0,   235,     0,
      0,     0,     0,     0,   243,   255,   225,     0,     0,   226,
      0,     0,   288,    22,   277,     0,     0,     0,   398,   399,
    400,   395,   401,   337,     0,     0,     0,     0,     0,    28,
      0,    29,     0,    56,    30,     0,     0,    58,     0,     0,
      0,     0,     0,     0,   429,   307,   462,     0,   448,     0,
    314,     0,   458,   447,     0,     0,   450,     0,     0,     0,
      0,   366,     0,     0,   368,     0,   292,     0,   293,     0,
      0,     0,     0,   304,   232,     0,   234,   249,   257,     0,
      0,     0,   240,     0,     0,   221,   397,   339,   354,   352,
    341,    27,     0,   264,     0,     0,     0,   431,     0,   434,
    435,   437,     0,     0,     0,     0,     0,     0,   357,   359,
    355,   360,   296,   299,     0,     0,     0,     0,   239,     0,
    245,     0,   227,    52,   310,     0,     0,     0,     0,     0,
      0,     0,   361,     0,     0,   236,   241,     0,     0,     0,
    244,   317,   433,     0,   331,   305,     0,     0,   246,     0,
    242,     0,   247,     0,   248,
     }, yyDgoto = {
 //yyDgoto 136
      1,   188,    61,    62,    63,    64,    65,   293,   290,   460,
     66,    67,    68,   468,    69,    70,    71,   109,    72,   206,
    207,    74,    75,    76,    77,    78,    79,   210,   259,   710,
    840,   711,   703,   237,   622,   417,   664,   366,   707,   246,
     81,   666,    82,    83,   565,   566,   567,   202,   751,   212,
    530,    85,    86,   238,   396,   578,   271,   228,   657,   213,
     88,   299,   297,   568,   569,   273,   596,    89,   274,   241,
    278,   409,   615,   410,   694,   782,   356,   340,   542,    90,
     91,   267,   379,   214,   215,   203,   291,    94,   114,   547,
    518,   115,   205,   513,   571,   572,   375,   573,   574,   384,
    385,     2,   221,   222,   426,   256,   681,   192,   575,   255,
    445,   247,   626,   731,   439,   223,   601,   722,   224,   723,
    608,   844,   546,   543,   773,   371,   376,   555,   777,   508,
    473,   472,   651,   650,   545,   372,
     }, yySindex = {
 //yySindex 885
      0,     0,  3093,  5011, 16123, 16468, 17033, 16923,  3093, 14743,
  14743,  4450,     0,     0, 16238,  5529,  5529,     0,     0,  5529,
   -279,  -256,     0,     0,     0,     0, 14743, 16813,   140,     0,
   -242,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0, 15893, 15893,   225,  -155,  4053, 14743, 14858,
  15893, 16583, 15893, 16008, 17142,     0,     0,     0,   133,   197,
      0,     0,  -205,     0,     0,     0,  -219,     0,     0,     0,
      0,     0,     0,     0,   126,   928,    81,  2036,     0,   -59,
    -67,     0,  -125,     0,  -102,   198,     0,   186,     0, 16353,
    205,     0,   -68,     0,     0,  -176,   928,     0,     0,     0,
   -279,  -256,   140,     0,     0,   252, 14743,  -170,  3093,    37,
      0,    32,     0,     0,  -176,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,   -96,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0, 17142,   242,     0,     0,     0,    34,    27,    70,    81,
    100,   188,    90,   283,   122,     0,   100,     0,     0,   126,
      0,  -108,   346, 14743, 14743,   131,   206,     0,   187,     0,
      0,     0, 15893, 15893, 15893,  2036,     0,     0,   134,   441,
    443,     0,     0,     0,     0,  3607,     0, 13478,  5529,     0,
      0,     0,   -85,     0,     0, 14973,   120,  3093,     0,   335,
    195,   207,   212,   167,  4053,   190,     0,   192,    81, 15893,
    140,   164,     0,    85,    95,     0,   101,    95,   177,   246,
    350,     0,     0,     0,     0,     0,     0,     0,     0,     0,
   -204,     0,     0,  -196,  -193,   319,   189,  -175,   193,  -231,
      0,     0,     0,     0,     0, 13311, 14743, 14743, 14743, 14743,
   5011, 14743, 14743, 15893, 15893, 15893, 15893, 15893, 15893, 15893,
  15893, 15893, 15893, 15893, 15893, 15893, 15893, 15893, 15893, 15893,
  15893, 15893, 15893, 15893, 15893, 15893, 15893, 15893, 15893,     0,
      0,  2533,  3481, 14858,  3926,  3926, 16008,     0, 15088,  4053,
  16583,   517, 15088, 16008,     0,   233,     0,   243,     0,     0,
     81,     0,     0,     0,   126,     0,     0,     0,  3926,  4884,
  14858,  3093, 14743,     0,     0,     0,  1480,     0, 15203,   314,
      0,   167,     0,     0,  3093,   325, 17310, 17369, 14858, 15893,
  15893, 15893,  3093,   337,  3093, 15318,   342,     0,   125,   125,
      0, 17428, 17487, 14858,     0,   566,     0, 15893, 13593,     0,
      0, 13708,     0,     0,   287,  4565,     0,     0,   -59,   140,
     11,   289,   598,     0,     0,     0, 16923, 14743,  2036,  3093,
    279, 17310, 17369, 15893, 15893, 15893,   303,     0,     0,   140,
   1982,     0,     0, 15433,     0,     0, 15893,     0, 15893,     0,
      0,     0,     0, 17546, 17605, 14858,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,    14,     0,
    619,     0,  -218,  -218,   928,     0,     0,     0,     0,     0,
      0,     0,   195,  1698,  1698,  1698,  1698,  2648,  2648,  9414,
   9293,  1698,  1698,  2097,  2097,  1646,  1646,   195,  1524,   195,
    195,  -146,  -146,  2648,  2648,  1397,  1397, 13372,  -218,   316,
      0,   318,  -256,     0,   323,     0,   332,  -256,     0,     0,
    312,     0,     0,  -256,  -256,     0,  2036, 15893,     0,  2153,
      0,     0,   629,   336,     0,     0,     0,     0,     0,     0,
   2036,     0,     0,   126,     0, 14743,  3093,  -256,     0,     0,
   -256,     0,   329,   414,   201, 17251,   627,     0,     0,     0,
   1538,     0,     0,     0,     0,  3093,   126,     0,   646,   649,
      0,     0,   659,   400,   406, 16923,     0,     0,   371,     0,
   3093,   460,     0,   331,     0,   390,   392,   394,   332,   388,
   2153,   314,   474,   475, 15893,   691,   100,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,   396, 14743,   405,
      0,     0, 15893,   134,   707,     0, 15893,   134,     0,     0,
  15893,  2036,     0,    -1,   718,     0,   422,   428,  3926,  3926,
    432,     0, 13823,     0,  -191,   415,     0,   195,   195,  2036,
      0,   434,     0, 15893,     0,     0,     0,     0,     0,   447,
   3093,   330,     0,     0,     0,     0,  2647,  3093,     0,  3093,
  15893,  3093, 16008, 16008,     0,   233,     0,     0, 16008, 15893,
      0,   233,   458,     0,     0,     0,     0,     0, 15893, 15548,
      0,  -218,     0,   126,   514,     0,     0,   456,     0, 15893,
    140,   536,     0,  1538,     0,   602,     0,   264,     0,     0,
      0,     0, 16698,   100,     0,     0,  3093,     0,     0, 14743,
      0,   537, 15893, 15893, 15893,   464,   541,     0,     0,     0,
  15663,  3093,  3093,  3093,     0,   125,   566, 13938,     0,   566,
    566,   468, 14053, 14168,     0,     0,     0,  -256,  -256,     0,
    -59,    11,     0,     0,     0,  1982,     0,   448,     0,     0,
      0,     0,     0,     0,   453,   551,   452,  2036,   558,     0,
   2036,     0,  2036,     0,     0,  2036,  2036,     0, 16008,  2036,
  15893,     0,  3093,  3093,     0,     0,     0,  1480,     0,   481,
      0,   783,     0,     0,   659,   627,     0,   659,   525,   439,
      0,     0,     0,  3093,     0,   100,     0, 15893,     0, 15893,
    -79,   579,   583,     0,     0, 15893,     0,     0,     0, 15893,
    804,   807,     0, 15893,   516,     0,     0,     0,     0,     0,
      0,     0,  2036,     0,   493,   596,  3093,     0,   602,     0,
      0,     0,     0, 17664, 17723, 14858,    34,  3093,     0,     0,
      0,     0,     0,     0,  3093,  3422,   566, 14283,     0, 14398,
      0,   566,     0,     0,     0,   599,   659,     0,     0,     0,
      0,   521,     0,   331,   604,     0,     0, 15893,   826, 15893,
      0,     0,     0,     0,     0,     0,   566, 14513,     0,   566,
      0, 15893,     0,   566,     0,
     }, yyRindex = {
 //yyRindex 885
      0,     0,   112,     0,     0,     0,     0,     0,  1159,     0,
      0,   -56,     0,     0,     0,  8162,  8285,     0,     0,  8404,
   4324,  3729,     0,     0,     0,     0,     0,     0, 15778,     0,
      0,     0,     0,  1807,  2884,     0,     0,  1924,     0,     0,
      0,     0,     0,     0,     0,     0,     0,    58,     0,   534,
    507,    87,     0,     0,   199,     0,     0,     0,   204,  -223,
      0,     0,  7436,     0,     0,     0,  7559,     0,     0,     0,
      0,     0,     0,     0,   191,   438,   919, 13054,  7678, 13091,
      0,     0, 13131,     0,  9489,     0,     0,     0,     0,   266,
      0,     0,     0,  9368,     0, 14628,   931,     0,     0,     0,
   7801,  6704,   540,  5650,  5771,     0,     0,     0,    58,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    901,  1219,  1297,  1303,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,  1669,  2473,  2484,     0,
   2953,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,  1541,     0,     0,     0,   258,     0,     0, 11801,
      0,     0,  7075,     0,     0,  6833,     0,     0,     0,   613,
      0,     0,     9,     0,     0,     0,     0,   956,     0,     0,
      0,  1405,     0,     0,     0, 11888,     0,     0,  1477, 12866,
  12866,     0,     0,     0,     0,     0,     0,     0,   543,     0,
      0,     0,     0,     0,     0,     0,     0,    21,     0,     0,
   8525,  7920,  8043,  9610,    58,     0,   -11,     0,     8,     0,
    544,     0,     0,   546,   546,     0,   530,   530,     0,     0,
      0,   774,     0,  1235,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,   231,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,   534,     0,     0,     0,     0,     0,    58,
    309,   471,     0,     0,     0, 12903,     0,     0,     0,     0,
    130,     0,  6172,     0,     0,     0,     0,     0,     0,     0,
    534,  1159,     0,     0,     0,     0,   157,     0,   200,   340,
      0,  7194,     0,     0,   527,  6293,     0,     0,   534,     0,
      0,     0,   413,     0,    31,     0,     0,     0,     0,     0,
   1425,     0,     0,   534,     0, 12866,     0,     0,     0,     0,
      0,     0,     0,     0,     0,   556,     0,     0,    15,   562,
    562,     0,    39,     0,     0,     0,     0,     0, 11980,    21,
      0,     0,     0,     0,     0,     0,     0,     0,   111,   562,
    544,     0,     0,   561,     0,     0,  -171,     0,   548,     0,
      0,     0,  3898,     0,     0,   534,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,  6433,  6562,  1084,     0,     0,     0,     0,     0,
      0,     0,  8646, 11108, 11216, 11303, 11406, 10666, 10774, 11494,
  11759, 11581, 11671,  1664, 11846, 10092, 10211,  8765, 10332,  8886,
   9007,  9850,  9971, 10882, 11000, 10437, 10548,     0,  6433,  4687,
      0,  4802,  3844,     0,  5167,  3249,  5282, 14628,     0,  3364,
      0,     0,     0,  5409,  5409,     0, 12067,     0,     0, 11124,
      0,     0,     0,     0,     0,     0,     0,     0, 13237,     0,
  12154,     0,     0,     0,     0,     0,  1159,  6946,  5911,  6032,
      0,     0,     0,     0,   562,     0,    48,     0,     0,     0,
     66,     0,     0,     0,     0,  1159,     0,     0,    61,    61,
      0,     0,    61,    54,     0,     0,     0,   144,   210,     0,
    265,   650,     0,   650,     0,  2289,  2404,  2769,  4209,     0,
  12742,   650,     0,     0,     0,   214,     0,     0,     0,     0,
      0,     0,     0,   817,  1105,  1352,   238,     0,     0,     0,
      0,     0,     0, 12779, 12866,     0,     0,     0,     0,     0,
      0,    74,     0,     0,   567,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,  9126,  9247, 12195,
     41,     0,     0,     0,     0,  1577,  1606,  2938,   719,     0,
     21,     0,     0,     0,     0,     0,     0,    31,     0,    21,
      0,    31,     0,     0,     0, 12960,     0,     0,     0,     0,
      0, 12997,  9731,     0,     0,     0,     0,     0,     0,     0,
      0,  6562,     0,     0,     0,     0,     0,     0,     0,     0,
    562,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,    31,     0,     0,     0,
      0,     0,     0,     0,     0,  7317,     0,     0,     0,     0,
      0,   559,    31,    31,  1426,     0, 12866,     0,     0, 12866,
    567,     0,     0,     0,     0,     0,     0,   139,   139,     0,
      0,   562,     0,     0,     0,   544,  4856,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0, 12258,     0,     0,
  12345,     0, 12382,     0,     0, 12472, 12513,     0,     0, 12611,
      0, 13176,    21,  1159,     0,     0,     0,   157,     0,     0,
      0,    61,     0,     0,    61,     0,     0,    61,     0,     0,
    657,     0,   778,  1159,     0,     0,     0,     0,     0,     0,
    650,     0,     0,     0,     0,     0,     0,     0,     0,     0,
    567,   567,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0, 12648,     0,     0,     0,  1159,     0,     0,     0,
      0,     0,   978,     0,     0,   534,   258,   527,     0,     0,
      0,     0,     0,     0,    31, 12866,   567,     0,     0,     0,
      0,   567,     0,     0,     0,     0,    61,    73,   666,   967,
    277,     0,     0,   650,     0,     0,     0,     0,   567,     0,
      0,     0,     0,  1061,     0,     0,   567,     0,     0,   567,
      0,     0,     0,   567,     0,
     }, yyGindex = {
 //yyGindex 136
      0,     0,     0,     0,   851,     0,     0,     0,   564,  -215,
      0,     0,     0,     0,     0,     0,     0,   911,   104,    86,
   -351,     0,   153,   867,   -15,    52,    -9,   136,   241,  -363,
      0,    55,     0,   608,     0,     0,    -8,     0,     0,    -4,
    914,   137,  -234,     0,   145,   357,  -601,     0,     0,   681,
   -178,   832,   -25,  1182,  -388,     0,  -328,   270,  -446,   738,
   1112,     0,     0,     0,   234,    29,     0,     0,    17,  -381,
      0,   159,    36,     0,   552,  -364,   862,     0,  -464,    -6,
     78,  -225,    96,   893,   836,   -18,     0,    -5,   711,  -272,
      0,   -84,     5,    47,   244,  -567,     0,     0,     0,  -213,
    585,     0,     3,   865,     0,     0,     0,     0,     0,    84,
    282,     0,     0,     0,     0,     0,  -380,     0,     0,     0,
      0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
      0,     0,     0,     0,     0,     0,
     };
     protected static final short[] yyTable = YyTables.yyTable();
     protected static final short[] yyCheck = YyTables.yyCheck();
 
   /** maps symbol value to printable name.
       @see #yyExpecting
     */
   protected static final String[] yyNames = {
     "end-of-file",null,null,null,null,null,null,null,null,null,"'\\n'",
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,"' '",null,null,null,null,null,
     null,null,null,null,null,null,"','",null,null,null,null,null,null,
     null,null,null,null,null,null,null,"':'","';'",null,"'='",null,"'?'",
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,
     "'['",null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,null,
     null,null,null,null,null,null,null,null,null,null,null,null,null,
     "kCLASS","kMODULE","kDEF","kUNDEF","kBEGIN","kRESCUE","kENSURE",
     "kEND","kIF","kUNLESS","kTHEN","kELSIF","kELSE","kCASE","kWHEN",
     "kWHILE","kUNTIL","kFOR","kBREAK","kNEXT","kREDO","kRETRY","kIN",
     "kDO","kDO_COND","kDO_BLOCK","kRETURN","kYIELD","kSUPER","kSELF",
     "kNIL","kTRUE","kFALSE","kAND","kOR","kNOT","kIF_MOD","kUNLESS_MOD",
     "kWHILE_MOD","kUNTIL_MOD","kRESCUE_MOD","kALIAS","kDEFINED","klBEGIN",
     "klEND","k__LINE__","k__FILE__","tIDENTIFIER","tFID","tGVAR","tIVAR",
     "tCONSTANT","tCVAR","tNTH_REF","tBACK_REF","tSTRING_CONTENT",
     "tINTEGER","tFLOAT","tREGEXP_END","tUPLUS","tUMINUS","tUMINUS_NUM",
     "tPOW","tCMP","tEQ","tEQQ","tNEQ","tGEQ","tLEQ","tANDOP","tOROP",
     "tMATCH","tNMATCH","tDOT","tDOT2","tDOT3","tAREF","tASET","tLSHFT",
     "tRSHFT","tCOLON2","tCOLON3","tOP_ASGN","tASSOC","tLPAREN","tLPAREN2",
     "tRPAREN","tLPAREN_ARG","tLBRACK","tRBRACK","tLBRACE","tLBRACE_ARG",
     "tSTAR","tSTAR2","tAMPER","tAMPER2","tTILDE","tPERCENT","tDIVIDE",
     "tPLUS","tMINUS","tLT","tGT","tPIPE","tBANG","tCARET","tLCURLY",
     "tRCURLY","tBACK_REF2","tSYMBEG","tSTRING_BEG","tXSTRING_BEG",
     "tREGEXP_BEG","tWORDS_BEG","tQWORDS_BEG","tSTRING_DBEG",
     "tSTRING_DVAR","tSTRING_END","tRDOC","tLOWEST","tLAST_TOKEN",
     };
 
   /** thrown for irrecoverable syntax errors and stack overflow.
       Nested for convenience, does not depend on parser class.
     */
   public static class yyException extends java.lang.Exception {
     private static final long serialVersionUID = 1L;
     public yyException (String message) {
       super(message);
     }
   }
 
   /** must be implemented by a scanner object to supply input to the parser.
       Nested for convenience, does not depend on parser class.
     */
   public interface yyInput {
 
     /** move on to next token.
         @return <tt>false</tt> if positioned beyond tokens.
         @throws IOException on input error.
       */
     boolean advance () throws java.io.IOException;
 
     /** classifies current token.
         Should not be called if {@link #advance()} returned <tt>false</tt>.
         @return current <tt>%token</tt> or single character.
       */
     int token ();
 
     /** associated with current token.
         Should not be called if {@link #advance()} returned <tt>false</tt>.
         @return value for {@link #token()}.
       */
     Object value ();
   }
 
   /** simplified error message.
       @see #yyerror(java.lang.String, java.lang.String[])
     */
   public void yyerror (String message) {
     yyerror(message, null, null);
   }
 
   /** (syntax) error message.
       Can be overwritten to control message format.
       @param message text to be displayed.
       @param expected list of acceptable tokens, if available.
     */
   public void yyerror(String message, String[] expected, String found) {
 		if (found != null) {
 			throw new SyntaxException(getPosition(null), message
 					+ ", unexpected " + found);
 		} else {
 			throw new SyntaxException(getPosition(null), message);
 		}
 	}
 
   /** computes list of expected tokens on error by tracing the tables.
       @param state for which to compute the list.
       @return list of token names.
     */
   protected String[] yyExpecting (int state) {
     int token, n, len = 0;
     boolean[] ok = new boolean[yyNames.length];
 
     if ((n = yySindex[state]) != 0)
       for (token = n < 0 ? -n : 0;
            token < yyNames.length && n+token < yyTable.length; ++ token)
         if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
           ++ len;
           ok[token] = true;
         }
     if ((n = yyRindex[state]) != 0)
       for (token = n < 0 ? -n : 0;
            token < yyNames.length && n+token < yyTable.length; ++ token)
         if (yyCheck[n+token] == token && !ok[token] && yyNames[token] != null) {
           ++ len;
           ok[token] = true;
         }
 
     String result[] = new String[len];
     for (n = token = 0; n < len;  ++ token)
       if (ok[token]) result[n++] = yyNames[token];
     return result;
   }
 
   /** the generated parser, with debugging messages.
       Maintains a dynamic state and value stack.
       @param yyLex scanner.
       @param yydebug debug message writer implementing <tt>yyDebug</tt>, or <tt>null</tt>.
       @return result of the last reduction, if any.
       @throws yyException on irrecoverable parse error.
     */
   public Object yyparse (RubyYaccLexer yyLex, Object ayydebug)
 				throws java.io.IOException, yyException {
     return yyparse(yyLex);
   }
 
   /** initial size and increment of the state/value stack [default 256].
       This is not final so that it can be overwritten outside of invocations
       of {@link #yyparse}.
     */
   protected int yyMax;
 
   /** executed at the beginning of a reduce action.
       Used as <tt>$$ = yyDefault($1)</tt>, prior to the user-specified action, if any.
       Can be overwritten to provide deep copy, etc.
       @param first value for <tt>$1</tt>, or <tt>null</tt>.
       @return first.
     */
   protected Object yyDefault (Object first) {
     return first;
   }
 
   /** the generated parser.
       Maintains a dynamic state and value stack.
       @param yyLex scanner.
       @return result of the last reduction, if any.
       @throws yyException on irrecoverable parse error.
     */
   public Object yyparse (RubyYaccLexer yyLex) throws java.io.IOException, yyException {
     if (yyMax <= 0) yyMax = 256;			// initial size
     int yyState = 0, yyStates[] = new int[yyMax];	// state stack
     Object yyVal = null, yyVals[] = new Object[yyMax];	// value stack
     int yyToken = -1;					// current input
     int yyErrorFlag = 0;				// #tokens to shift
 
     yyLoop: for (int yyTop = 0;; ++ yyTop) {
       if (yyTop >= yyStates.length) {			// dynamically increase
         int[] i = new int[yyStates.length+yyMax];
         System.arraycopy(yyStates, 0, i, 0, yyStates.length);
         yyStates = i;
         Object[] o = new Object[yyVals.length+yyMax];
         System.arraycopy(yyVals, 0, o, 0, yyVals.length);
         yyVals = o;
       }
       yyStates[yyTop] = yyState;
       yyVals[yyTop] = yyVal;
 
       yyDiscarded: for (;;) {	// discarding a token does not change stack
         int yyN;
         if ((yyN = yyDefRed[yyState]) == 0) {	// else [default] reduce (yyN)
           if (yyToken < 0) {
             yyToken = yyLex.advance() ? yyLex.token() : 0;
           }
           if ((yyN = yySindex[yyState]) != 0 && (yyN += yyToken) >= 0
               && yyN < yyTable.length && yyCheck[yyN] == yyToken) {
             yyState = yyTable[yyN];		// shift to yyN
             yyVal = yyLex.value();
             yyToken = -1;
             if (yyErrorFlag > 0) -- yyErrorFlag;
             continue yyLoop;
           }
           if ((yyN = yyRindex[yyState]) != 0 && (yyN += yyToken) >= 0
               && yyN < yyTable.length && yyCheck[yyN] == yyToken)
             yyN = yyTable[yyN];			// reduce (yyN)
           else
             switch (yyErrorFlag) {
   
             case 0:
               yyerror("syntax error", yyExpecting(yyState), yyNames[yyToken]);
   
             case 1: case 2:
               yyErrorFlag = 3;
               do {
                 if ((yyN = yySindex[yyStates[yyTop]]) != 0
                     && (yyN += yyErrorCode) >= 0 && yyN < yyTable.length
                     && yyCheck[yyN] == yyErrorCode) {
                   yyState = yyTable[yyN];
                   yyVal = yyLex.value();
                   continue yyLoop;
                 }
               } while (-- yyTop >= 0);
               throw new yyException("irrecoverable syntax error");
   
             case 3:
               if (yyToken == 0) {
                 throw new yyException("irrecoverable syntax error at end-of-file");
               }
               yyToken = -1;
               continue yyDiscarded;		// leave stack alone
             }
         }
         int yyV = yyTop + 1-yyLen[yyN];
         yyVal = yyDefault(yyV > yyTop ? null : yyVals[yyV]);
         switch (yyN) {
 case 1:
 					// line 265 "DefaultRubyParser.y"
   {
                   lexer.setState(LexState.EXPR_BEG);
                   support.initTopLocalVariables();
               }
   break;
 case 2:
 					// line 268 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[0+yyTop]) != null) {
                       /* last expression should not be void */
                       if (((Node)yyVals[0+yyTop]) instanceof BlockNode) {
                           support.checkUselessStatement(((BlockNode)yyVals[0+yyTop]).getLast());
                       } else {
                           support.checkUselessStatement(((Node)yyVals[0+yyTop]));
                       }
                   }
                   support.getResult().setAST(support.addRootNode(((Node)yyVals[0+yyTop]), getPosition(((Node)yyVals[0+yyTop]))));
               }
   break;
 case 3:
 					// line 280 "DefaultRubyParser.y"
   {
                   Node node = ((Node)yyVals[-3+yyTop]);
 
 		  if (((RescueBodyNode)yyVals[-2+yyTop]) != null) {
 		      node = new RescueNode(getPosition(((Node)yyVals[-3+yyTop]), true), ((Node)yyVals[-3+yyTop]), ((RescueBodyNode)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]));
 		  } else if (((Node)yyVals[-1+yyTop]) != null) {
 		      warnings.warn(getPosition(((Node)yyVals[-3+yyTop])), "else without rescue is useless");
 		      node = new RescueNode(getPosition(((Node)yyVals[-3+yyTop]), true), ((Node)yyVals[-3+yyTop]), null, ((Node)yyVals[-1+yyTop]));
                       /* node = support.appendToBlock($1, $3);*/
 		  }
 		  if (((EnsureNode.Keyword)yyVals[0+yyTop]) != null) {
 		      node = new EnsureNode(getPosition(((Node)yyVals[-3+yyTop])), node, ((EnsureNode.Keyword)yyVals[0+yyTop]));
 		  }
 
 	          yyVal = node;
               }
   break;
 case 4:
 					// line 297 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[-1+yyTop]) instanceof BlockNode) {
                       support.checkUselessStatements(((BlockNode)yyVals[-1+yyTop]));
 		  }
                   yyVal = ((Node)yyVals[-1+yyTop]);
               }
   break;
 case 6:
 					// line 305 "DefaultRubyParser.y"
   {
                   yyVal = support.newline_node(((Node)yyVals[0+yyTop]), getPosition(((Node)yyVals[0+yyTop]), true));
               }
   break;
 case 7:
 					// line 308 "DefaultRubyParser.y"
   {
 	          yyVal = support.appendToBlock(((Node)yyVals[-2+yyTop]), support.newline_node(((Node)yyVals[0+yyTop]), getPosition(((Node)yyVals[0+yyTop]), true)));
               }
   break;
 case 8:
 					// line 311 "DefaultRubyParser.y"
   {
                   yyVal = ((Node)yyVals[0+yyTop]);
               }
   break;
 case 9:
 					// line 315 "DefaultRubyParser.y"
   {
                   lexer.setState(LexState.EXPR_FNAME);
               }
   break;
 case 10:
 					// line 317 "DefaultRubyParser.y"
   {
                   yyVal = new AliasNode(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 11:
 					// line 320 "DefaultRubyParser.y"
   {
                   yyVal = new VAliasNode(getPosition(((Token)yyVals[-2+yyTop])), (String) ((Token)yyVals[-1+yyTop]).getValue(), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 12:
 					// line 323 "DefaultRubyParser.y"
   {
                   yyVal = new VAliasNode(getPosition(((Token)yyVals[-2+yyTop])), (String) ((Token)yyVals[-1+yyTop]).getValue(), "$" + ((BackRefNode)yyVals[0+yyTop]).getType()); /* XXX*/
               }
   break;
 case 13:
 					// line 326 "DefaultRubyParser.y"
   {
                   yyerror("can't make alias for the number variables");
               }
   break;
 case 14:
 					// line 329 "DefaultRubyParser.y"
   {
                   yyVal = ((Node)yyVals[0+yyTop]);
               }
   break;
 case 15:
 					// line 332 "DefaultRubyParser.y"
   {
                   yyVal = lexer.comment();
               }
   break;
 case 16:
 					// line 335 "DefaultRubyParser.y"
   {
                   yyVal = new IfNode.Inline(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), null);
               }
   break;
 case 17:
 					// line 338 "DefaultRubyParser.y"
   {
                   yyVal = new IfNode.InlineUnless(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), null, ((Node)yyVals[-2+yyTop]));
               }
   break;
 case 18:
 					// line 341 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[-2+yyTop]) != null && ((Node)yyVals[-2+yyTop]) instanceof BeginNode) {
                       yyVal = new WhileNode.Inline(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), false);
                   } else {
                       yyVal = new WhileNode.Inline(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), true);
                   }
               }
   break;
 case 19:
 					// line 348 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[-2+yyTop]) != null && ((Node)yyVals[-2+yyTop]) instanceof BeginNode) {
                       yyVal = new UntilNode.Inline(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]));
                   } else {
                       yyVal = new UntilNode.Inline(getPosition(((Node)yyVals[-2+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]));
                   }
               }
   break;
 case 20:
 					// line 355 "DefaultRubyParser.y"
   {
 	          yyVal = new RescueNode.Inline(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[-2+yyTop]), new RescueBodyNode(getPosition(((Node)yyVals[-2+yyTop])), null,((Node)yyVals[0+yyTop]), null, null), null);
               }
   break;
 case 21:
 					// line 358 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) {
                       yyerror("BEGIN in method");
                   }
 		  support.pushLocalScope();
               }
   break;
 case 22:
 					// line 363 "DefaultRubyParser.y"
   {
                   support.popCurrentScope();
                   yyVal = new PreExeNode(((Token)yyVals[-4+yyTop]), ((Token)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 23:
 					// line 367 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) {
                       yyerror("END in method; use at_exit");
                   }
 
                   yyVal = new PostExeNode(getPosition(((Token)yyVals[-3+yyTop])), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-3+yyTop]), ((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 24:
 					// line 374 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[0+yyTop]));
                   yyVal = support.node_assign(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 25:
 					// line 378 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[0+yyTop]));
 		  if (((MultipleAsgnNode)yyVals[-2+yyTop]).getHeadNode() != null) {
 		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ToAryNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
 		  } else {
 		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ArrayNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
 		  }
 		  yyVal = ((MultipleAsgnNode)yyVals[-2+yyTop]);
               }
   break;
 case 26:
 					// line 387 "DefaultRubyParser.y"
   {
  	          support.checkExpression(((Node)yyVals[0+yyTop]));
 
 	          String name = ((INameNode)yyVals[-2+yyTop]).getName();
 		  String asgnOp = (String) ((Token)yyVals[-1+yyTop]).getValue();
 		  if (asgnOp.equals("||")) {
 	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
 	              yyVal = new OpAsgnOrNode(support.union(((AssignableNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.gettable2(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
 		  } else if (asgnOp.equals("&&")) {
 	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
                       yyVal = new OpAsgnAndNode(support.union(((AssignableNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.gettable2(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
 		  } else {
                       ((AssignableNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable2(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), asgnOp, ((Node)yyVals[0+yyTop])));
                       ((AssignableNode)yyVals[-2+yyTop]).setPosition(support.union(((AssignableNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])));
 		      yyVal = ((AssignableNode)yyVals[-2+yyTop]);
 		  }
 	      }
   break;
 case 27:
 					// line 404 "DefaultRubyParser.y"
   {
                   yyVal = new OpElementAsgnNode(getPosition(((Node)yyVals[-5+yyTop])), ((Node)yyVals[-5+yyTop]), (String) ((Token)yyVals[-1+yyTop]).getValue(), ((Node)yyVals[-3+yyTop]), ((Node)yyVals[0+yyTop]));
 
               }
   break;
 case 28:
 					// line 408 "DefaultRubyParser.y"
   {
                   yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
               }
   break;
 case 29:
 					// line 411 "DefaultRubyParser.y"
   {
                   yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
               }
   break;
 case 30:
 					// line 414 "DefaultRubyParser.y"
   {
                   yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
               }
   break;
 case 31:
 					// line 417 "DefaultRubyParser.y"
   {
                   support.backrefAssignError(((Node)yyVals[-2+yyTop]));
               }
   break;
 case 32:
 					// line 420 "DefaultRubyParser.y"
   {
                   yyVal = support.node_assign(((Node)yyVals[-2+yyTop]), new SValueNode(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
               }
   break;
 case 33:
 					// line 423 "DefaultRubyParser.y"
   {
                   if (((MultipleAsgnNode)yyVals[-2+yyTop]).getHeadNode() != null) {
 		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ToAryNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
 		  } else {
 		      ((MultipleAsgnNode)yyVals[-2+yyTop]).setValueNode(new ArrayNode(getPosition(((MultipleAsgnNode)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop])));
 		  }
 		  yyVal = ((MultipleAsgnNode)yyVals[-2+yyTop]);
 	      }
   break;
 case 34:
 					// line 431 "DefaultRubyParser.y"
   {
                   ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
 		  yyVal = ((MultipleAsgnNode)yyVals[-2+yyTop]);
                   ((MultipleAsgnNode)yyVals[-2+yyTop]).setPosition(support.union(((MultipleAsgnNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])));
 	      }
   break;
 case 37:
 					// line 439 "DefaultRubyParser.y"
   {
                   yyVal = support.newAndNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 38:
 					// line 442 "DefaultRubyParser.y"
   {
                   yyVal = support.newOrNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 39:
 					// line 445 "DefaultRubyParser.y"
   {
                   yyVal = new NotNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])));
               }
   break;
 case 40:
 					// line 448 "DefaultRubyParser.y"
   {
                   yyVal = new NotNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])));
               }
   break;
 case 42:
 					// line 453 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[0+yyTop]));
 	      }
   break;
 case 45:
 					// line 459 "DefaultRubyParser.y"
   {
                   yyVal = new ReturnNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.ret_args(((Node)yyVals[0+yyTop]), getPosition(((Token)yyVals[-1+yyTop]))));
               }
   break;
 case 46:
 					// line 462 "DefaultRubyParser.y"
   {
                   yyVal = new BreakNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.ret_args(((Node)yyVals[0+yyTop]), getPosition(((Token)yyVals[-1+yyTop]))));
               }
   break;
 case 47:
 					// line 465 "DefaultRubyParser.y"
   {
                   yyVal = new NextNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.ret_args(((Node)yyVals[0+yyTop]), getPosition(((Token)yyVals[-1+yyTop]))));
               }
   break;
 case 49:
 					// line 470 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 50:
 					// line 473 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 51:
 					// line 477 "DefaultRubyParser.y"
   {
                     support.pushBlockScope();
 		}
   break;
 case 52:
 					// line 479 "DefaultRubyParser.y"
   {
                     yyVal = new IterNode(getPosition(((Token)yyVals[-4+yyTop])), ((Node)yyVals[-2+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
                     support.popCurrentScope();
 		}
   break;
 case 53:
 					// line 484 "DefaultRubyParser.y"
   {
                   yyVal = support.new_fcall(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 54:
 					// line 487 "DefaultRubyParser.y"
   {
                   yyVal = support.new_fcall(((Token)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])); 
               }
   break;
 case 55:
 					// line 490 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 56:
 					// line 493 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-4+yyTop]), ((Token)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])); 
 	      }
   break;
 case 57:
 					// line 496 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 58:
 					// line 499 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-4+yyTop]), ((Token)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])); 
 	      }
   break;
 case 59:
 					// line 502 "DefaultRubyParser.y"
   {
 		  yyVal = support.new_super(((Node)yyVals[0+yyTop]), ((Token)yyVals[-1+yyTop])); /* .setPosFrom($2);*/
 	      }
   break;
 case 60:
 					// line 505 "DefaultRubyParser.y"
   {
                   yyVal = support.new_yield(getPosition(((Token)yyVals[-1+yyTop])), ((Node)yyVals[0+yyTop]));
 	      }
   break;
 case 62:
 					// line 510 "DefaultRubyParser.y"
   {
                   yyVal = ((MultipleAsgnNode)yyVals[-1+yyTop]);
 	      }
   break;
 case 64:
 					// line 515 "DefaultRubyParser.y"
   {
                   yyVal = new MultipleAsgnNode(getPosition(((Token)yyVals[-2+yyTop])), new ArrayNode(getPosition(((Token)yyVals[-2+yyTop])), ((MultipleAsgnNode)yyVals[-1+yyTop])), null);
               }
   break;
 case 65:
 					// line 519 "DefaultRubyParser.y"
   {
                   yyVal = new MultipleAsgnNode(getPosition(((ListNode)yyVals[0+yyTop])), ((ListNode)yyVals[0+yyTop]), null);
               }
   break;
 case 66:
 					// line 522 "DefaultRubyParser.y"
   {
 /*mirko: check*/
                   yyVal = new MultipleAsgnNode(support.union(((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), ((ListNode)yyVals[-1+yyTop]).add(((Node)yyVals[0+yyTop])), null);
                   ((Node)yyVals[-1+yyTop]).setPosition(support.union(((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])));
               }
   break;
 case 67:
 					// line 527 "DefaultRubyParser.y"
   {
                   yyVal = new MultipleAsgnNode(getPosition(((ListNode)yyVals[-2+yyTop])), ((ListNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 68:
 					// line 530 "DefaultRubyParser.y"
   {
                   yyVal = new MultipleAsgnNode(getPosition(((ListNode)yyVals[-1+yyTop])), ((ListNode)yyVals[-1+yyTop]), new StarNode(getPosition(null)));
               }
   break;
 case 69:
 					// line 533 "DefaultRubyParser.y"
   {
                   yyVal = new MultipleAsgnNode(getPosition(((Token)yyVals[-1+yyTop])), null, ((Node)yyVals[0+yyTop]));
               }
   break;
 case 70:
 					// line 536 "DefaultRubyParser.y"
   {
                   yyVal = new MultipleAsgnNode(getPosition(((Token)yyVals[0+yyTop])), null, new StarNode(getPosition(null)));
               }
   break;
 case 72:
 					// line 541 "DefaultRubyParser.y"
   {
                   yyVal = ((MultipleAsgnNode)yyVals[-1+yyTop]);
               }
   break;
 case 73:
 					// line 545 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(((Node)yyVals[-1+yyTop]).getPosition(), ((Node)yyVals[-1+yyTop]));
               }
   break;
 case 74:
 					// line 548 "DefaultRubyParser.y"
   {
                   yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[-1+yyTop]));
               }
   break;
 case 75:
 					// line 552 "DefaultRubyParser.y"
   {
                   yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
               }
   break;
 case 76:
 					// line 555 "DefaultRubyParser.y"
   {
                   yyVal = support.aryset(((Node)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]));
               }
   break;
 case 77:
 					// line 558 "DefaultRubyParser.y"
   {
                   yyVal = support.attrset(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 78:
 					// line 561 "DefaultRubyParser.y"
   {
                   yyVal = support.attrset(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 79:
 					// line 564 "DefaultRubyParser.y"
   {
                   yyVal = support.attrset(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 80:
 					// line 567 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) {
 		      yyerror("dynamic constant assignment");
 		  }
 
 		  ISourcePosition position = support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));
 
                   yyVal = new ConstDeclNode(position, null, new Colon2Node(position, ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue()), null);
 	      }
   break;
 case 81:
 					// line 576 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) {
 		      yyerror("dynamic constant assignment");
 		  }
 
                   ISourcePosition position = support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop]));
 
                   yyVal = new ConstDeclNode(position, null, new Colon3Node(position, (String) ((Token)yyVals[0+yyTop]).getValue()), null);
 	      }
   break;
 case 82:
 					// line 585 "DefaultRubyParser.y"
   {
 	          support.backrefAssignError(((Node)yyVals[0+yyTop]));
               }
   break;
 case 83:
 					// line 589 "DefaultRubyParser.y"
   {
                   yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
               }
   break;
 case 84:
 					// line 592 "DefaultRubyParser.y"
   {
                   yyVal = support.aryset(((Node)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]));
               }
   break;
 case 85:
 					// line 595 "DefaultRubyParser.y"
   {
                   yyVal = support.attrset(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 86:
 					// line 598 "DefaultRubyParser.y"
   {
                   yyVal = support.attrset(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
  	      }
   break;
 case 87:
 					// line 601 "DefaultRubyParser.y"
   {
                   yyVal = support.attrset(((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 88:
 					// line 604 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) {
 		      yyerror("dynamic constant assignment");
 		  }
 			
 		  ISourcePosition position = support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));
 
                   yyVal = new ConstDeclNode(position, null, new Colon2Node(position, ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue()), null);
               }
   break;
 case 89:
 					// line 613 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) {
 		      yyerror("dynamic constant assignment");
 		  }
 
                   ISourcePosition position = support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop]));
 
                   yyVal = new ConstDeclNode(position, null, new Colon3Node(position, (String) ((Token)yyVals[0+yyTop]).getValue()), null);
 	      }
   break;
 case 90:
 					// line 622 "DefaultRubyParser.y"
   {
                    support.backrefAssignError(((Node)yyVals[0+yyTop]));
 	      }
   break;
 case 91:
 					// line 626 "DefaultRubyParser.y"
   {
                   yyerror("class/module name must be CONSTANT");
               }
   break;
 case 93:
 					// line 631 "DefaultRubyParser.y"
   {
                   yyVal = new Colon3Node(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
 	      }
   break;
 case 94:
 					// line 634 "DefaultRubyParser.y"
   {
                   yyVal = new Colon2Node(((Token)yyVals[0+yyTop]).getPosition(), null, (String) ((Token)yyVals[0+yyTop]).getValue());
  	      }
   break;
 case 95:
 					// line 637 "DefaultRubyParser.y"
   {
                   yyVal = new Colon2Node(support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
 	      }
   break;
 case 99:
 					// line 643 "DefaultRubyParser.y"
   {
                   lexer.setState(LexState.EXPR_END);
                   yyVal = ((Token)yyVals[0+yyTop]);
               }
   break;
 case 100:
 					// line 648 "DefaultRubyParser.y"
   {
                   lexer.setState(LexState.EXPR_END);
                   yyVal = yyVals[0+yyTop];
               }
   break;
 case 103:
 					// line 655 "DefaultRubyParser.y"
   {
                   yyVal = new UndefNode(getPosition(((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 104:
 					// line 658 "DefaultRubyParser.y"
   {
                   lexer.setState(LexState.EXPR_FNAME);
 	      }
   break;
 case 105:
 					// line 660 "DefaultRubyParser.y"
   {
                   yyVal = support.appendToBlock(((Node)yyVals[-3+yyTop]), new UndefNode(getPosition(((Node)yyVals[-3+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue()));
               }
   break;
 case 173:
 					// line 679 "DefaultRubyParser.y"
   {
                   yyVal = support.node_assign(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
 		  /* FIXME: Consider fixing node_assign itself rather than single case*/
 		  ((Node)yyVal).setPosition(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])));
               }
   break;
 case 174:
 					// line 684 "DefaultRubyParser.y"
   {
                   ISourcePosition position = support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
                   yyVal = support.node_assign(((Node)yyVals[-4+yyTop]), new RescueNode.Inline(position, ((Node)yyVals[-2+yyTop]), new RescueBodyNode(position, null, ((Node)yyVals[0+yyTop]), null, null), null));
 	      }
   break;
 case 175:
 					// line 688 "DefaultRubyParser.y"
   {
 		  support.checkExpression(((Node)yyVals[0+yyTop]));
 	          String name = ((INameNode)yyVals[-2+yyTop]).getName();
 		  String asgnOp = (String) ((Token)yyVals[-1+yyTop]).getValue();
 
 		  if (asgnOp.equals("||")) {
 	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
 	              yyVal = new OpAsgnOrNode(support.union(((AssignableNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.gettable2(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
 		  } else if (asgnOp.equals("&&")) {
 	              ((AssignableNode)yyVals[-2+yyTop]).setValueNode(((Node)yyVals[0+yyTop]));
                       yyVal = new OpAsgnAndNode(support.union(((AssignableNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.gettable2(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), ((AssignableNode)yyVals[-2+yyTop]));
 		  } else {
 		      ((AssignableNode)yyVals[-2+yyTop]).setValueNode(support.getOperatorCallNode(support.gettable2(name, ((AssignableNode)yyVals[-2+yyTop]).getPosition()), asgnOp, ((Node)yyVals[0+yyTop])));
                       ((AssignableNode)yyVals[-2+yyTop]).setPosition(support.union(((AssignableNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])));
 		      yyVal = ((AssignableNode)yyVals[-2+yyTop]);
 		  }
               }
   break;
 case 176:
 					// line 705 "DefaultRubyParser.y"
   {
                   yyVal = new OpElementAsgnNode(getPosition(((Node)yyVals[-5+yyTop])), ((Node)yyVals[-5+yyTop]), (String) ((Token)yyVals[-1+yyTop]).getValue(), ((Node)yyVals[-3+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 177:
 					// line 708 "DefaultRubyParser.y"
   {
                   yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
               }
   break;
 case 178:
 					// line 711 "DefaultRubyParser.y"
   {
                   yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
               }
   break;
 case 179:
 					// line 714 "DefaultRubyParser.y"
   {
                   yyVal = new OpAsgnNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop]), ((Node)yyVals[0+yyTop]), (String) ((Token)yyVals[-2+yyTop]).getValue(), (String) ((Token)yyVals[-1+yyTop]).getValue());
               }
   break;
 case 180:
 					// line 717 "DefaultRubyParser.y"
   {
 	          yyerror("constant re-assignment");
 	      }
   break;
 case 181:
 					// line 720 "DefaultRubyParser.y"
   {
 		  yyerror("constant re-assignment");
 	      }
   break;
 case 182:
 					// line 723 "DefaultRubyParser.y"
   {
                   support.backrefAssignError(((Node)yyVals[-2+yyTop]));
               }
   break;
 case 183:
 					// line 726 "DefaultRubyParser.y"
   {
 		  support.checkExpression(((Node)yyVals[-2+yyTop]));
 		  support.checkExpression(((Node)yyVals[0+yyTop]));
                   yyVal = new DotNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]), false);
               }
   break;
 case 184:
 					// line 731 "DefaultRubyParser.y"
   {
 		  support.checkExpression(((Node)yyVals[-2+yyTop]));
 		  support.checkExpression(((Node)yyVals[0+yyTop]));
                   yyVal = new DotNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]), true);
               }
   break;
 case 185:
 					// line 736 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "+", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 186:
 					// line 739 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "-", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 187:
 					// line 742 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "*", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 188:
 					// line 745 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "/", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 189:
 					// line 748 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "%", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 190:
 					// line 751 "DefaultRubyParser.y"
   {
 		  yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "**", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 191:
 					// line 754 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "**", ((Node)yyVals[0+yyTop]), getPosition(null)), "-@");
               }
   break;
 case 192:
 					// line 757 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(support.getOperatorCallNode(((FloatNode)yyVals[-2+yyTop]), "**", ((Node)yyVals[0+yyTop]), getPosition(null)), "-@");
               }
   break;
 case 193:
 					// line 760 "DefaultRubyParser.y"
   {
  	          if (((Node)yyVals[0+yyTop]) != null && ((Node)yyVals[0+yyTop]) instanceof ILiteralNode) {
 		      yyVal = ((Node)yyVals[0+yyTop]);
 		  } else {
                       yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "+@");
 		  }
               }
   break;
 case 194:
 					// line 767 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "-@");
 	      }
   break;
 case 195:
 					// line 770 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "|", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 196:
 					// line 773 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "^", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 197:
 					// line 776 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "&", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 198:
 					// line 779 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<=>", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 199:
 					// line 782 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), ">", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 200:
 					// line 785 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), ">=", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 201:
 					// line 788 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 202:
 					// line 791 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<=", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 203:
 					// line 794 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "==", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 204:
 					// line 797 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "===", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 205:
 					// line 800 "DefaultRubyParser.y"
   {
                   yyVal = new NotNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "==", ((Node)yyVals[0+yyTop]), getPosition(null)));
               }
   break;
 case 206:
 					// line 803 "DefaultRubyParser.y"
   {
                   yyVal = support.getMatchNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 207:
 					// line 806 "DefaultRubyParser.y"
   {
                   yyVal = new NotNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), support.getMatchNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])));
               }
   break;
 case 208:
 					// line 809 "DefaultRubyParser.y"
   {
                   yyVal = new NotNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[0+yyTop])));
               }
   break;
 case 209:
 					// line 812 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[0+yyTop]), "~");
               }
   break;
 case 210:
 					// line 815 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), "<<", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 211:
 					// line 818 "DefaultRubyParser.y"
   {
                   yyVal = support.getOperatorCallNode(((Node)yyVals[-2+yyTop]), ">>", ((Node)yyVals[0+yyTop]), getPosition(null));
               }
   break;
 case 212:
 					// line 821 "DefaultRubyParser.y"
   {
                   yyVal = support.newAndNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 213:
 					// line 824 "DefaultRubyParser.y"
   {
                   yyVal = support.newOrNode(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 214:
 					// line 827 "DefaultRubyParser.y"
   {
                   yyVal = new DefinedNode(getPosition(((Token)yyVals[-2+yyTop])), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 215:
 					// line 830 "DefaultRubyParser.y"
   {
                   yyVal = new IfNode.Inline(getPosition(((Node)yyVals[-4+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 216:
 					// line 833 "DefaultRubyParser.y"
   {
                   yyVal = ((Node)yyVals[0+yyTop]);
               }
   break;
 case 217:
 					// line 837 "DefaultRubyParser.y"
   {
 	          support.checkExpression(((Node)yyVals[0+yyTop]));
 	          yyVal = ((Node)yyVals[0+yyTop]);   
 	      }
   break;
 case 219:
 					// line 843 "DefaultRubyParser.y"
   {
                   warnings.warn(getPosition(((Node)yyVals[-1+yyTop])), "parenthesize argument(s) for future version");
                   yyVal = new ArrayNode(getPosition(((Node)yyVals[-1+yyTop])), ((Node)yyVals[-1+yyTop]));
               }
   break;
 case 220:
 					// line 847 "DefaultRubyParser.y"
   {
                   yyVal = ((ListNode)yyVals[-1+yyTop]);
               }
   break;
 case 221:
 					// line 850 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), ((ListNode)yyVals[-4+yyTop]), ((Node)yyVals[-1+yyTop]));
               }
   break;
 case 222:
 					// line 854 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(getPosition(((ListNode)yyVals[-1+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
               }
   break;
 case 223:
 					// line 857 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[-1+yyTop]));
 		  yyVal = new NewlineNode(getPosition(((Token)yyVals[-2+yyTop])), new SplatNode(getPosition(((Token)yyVals[-2+yyTop])), ((Node)yyVals[-1+yyTop])));
               }
   break;
 case 224:
 					// line 862 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
               }
   break;
 case 225:
 					// line 865 "DefaultRubyParser.y"
   {
                   yyVal = ((Node)yyVals[-2+yyTop]);
 		  ((Node)yyVal).setPosition(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])));
               }
   break;
 case 226:
 					// line 869 "DefaultRubyParser.y"
   {
                   warnings.warn(getPosition(((Token)yyVals[-3+yyTop])), "parenthesize argument(s) for future version");
                   yyVal = new ArrayNode(getPosition(((Token)yyVals[-3+yyTop])), ((Node)yyVals[-2+yyTop]));
               }
   break;
 case 227:
 					// line 873 "DefaultRubyParser.y"
   {
                   warnings.warn(getPosition(((Token)yyVals[-5+yyTop])), "parenthesize argument(s) for future version");
                   yyVal = ((ListNode)yyVals[-4+yyTop]).add(((Node)yyVals[-2+yyTop]));
               }
   break;
 case 230:
 					// line 881 "DefaultRubyParser.y"
   {
                   warnings.warn(((Node)yyVals[0+yyTop]).getPosition(), "parenthesize argument(s) for future version");
                   yyVal = new ArrayNode(getPosition(((Node)yyVals[0+yyTop])), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 231:
 					// line 885 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_blk_pass(((ListNode)yyVals[-1+yyTop]), ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 232:
 					// line 888 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), ((ListNode)yyVals[-4+yyTop]), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass(((Node)yyVal), ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 233:
 					// line 892 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(getPosition(((ListNode)yyVals[-1+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 234:
 					// line 896 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), new ArrayNode(getPosition(((ListNode)yyVals[-4+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 235:
 					// line 900 "DefaultRubyParser.y"
   {
                   yyVal = ((ListNode)yyVals[-3+yyTop]).add(new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 236:
 					// line 904 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[-1+yyTop]));
 		  yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-6+yyTop])), ((ListNode)yyVals[-6+yyTop]).add(new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 237:
 					// line 909 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_blk_pass(new SplatNode(getPosition(((Token)yyVals[-2+yyTop])), ((Node)yyVals[-1+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 238:
 					// line 912 "DefaultRubyParser.y"
   {}
   break;
 case 239:
 					// line 914 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_blk_pass(new ArrayNode(getPosition(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-3+yyTop])).addAll(((ListNode)yyVals[-1+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 240:
 					// line 917 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_blk_pass(new ArrayNode(getPosition(((Node)yyVals[-2+yyTop])), ((Node)yyVals[-2+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
               }
   break;
 case 241:
 					// line 920 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((Node)yyVals[-4+yyTop])), new ArrayNode(getPosition(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 242:
 					// line 924 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((Node)yyVals[-6+yyTop])), new ArrayNode(getPosition(((Node)yyVals[-6+yyTop])), ((Node)yyVals[-6+yyTop])).addAll(new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 243:
 					// line 928 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(getPosition(((ListNode)yyVals[-1+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 244:
 					// line 932 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-4+yyTop])), new ArrayNode(getPosition(((ListNode)yyVals[-4+yyTop])), new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 245:
 					// line 936 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(getPosition(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-3+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 246:
 					// line 940 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(getPosition(((Node)yyVals[-5+yyTop])), ((Node)yyVals[-5+yyTop])).addAll(((ListNode)yyVals[-3+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-1+yyTop])));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 247:
 					// line 944 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((Node)yyVals[-6+yyTop])), new ArrayNode(getPosition(((Node)yyVals[-6+yyTop])), ((Node)yyVals[-6+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 248:
 					// line 948 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((Node)yyVals[-8+yyTop])), new ArrayNode(getPosition(((Node)yyVals[-8+yyTop])), ((Node)yyVals[-8+yyTop])).addAll(((ListNode)yyVals[-6+yyTop])).add(new HashNode(getPosition(null), ((ListNode)yyVals[-4+yyTop]))), ((Node)yyVals[-1+yyTop]));
                   yyVal = support.arg_blk_pass((Node)yyVal, ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 249:
 					// line 952 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_blk_pass(new SplatNode(getPosition(((Token)yyVals[-2+yyTop])), ((Node)yyVals[-1+yyTop])), ((BlockPassNode)yyVals[0+yyTop]));
 	      }
   break;
 case 250:
 					// line 955 "DefaultRubyParser.y"
   {}
   break;
 case 251:
 					// line 957 "DefaultRubyParser.y"
   { 
 	          yyVal = new Long(lexer.getCmdArgumentState().begin());
 	      }
   break;
 case 252:
 					// line 959 "DefaultRubyParser.y"
   {
                   lexer.getCmdArgumentState().reset(((Long)yyVals[-1+yyTop]).longValue());
                   yyVal = ((Node)yyVals[0+yyTop]);
               }
   break;
 case 254:
 					// line 965 "DefaultRubyParser.y"
   {                    
 		  lexer.setState(LexState.EXPR_ENDARG);
 	      }
   break;
 case 255:
 					// line 967 "DefaultRubyParser.y"
   {
                   warnings.warn(getPosition(((Token)yyVals[-2+yyTop])), "don't put space before argument parentheses");
 	          yyVal = null;
 	      }
   break;
 case 256:
 					// line 971 "DefaultRubyParser.y"
   {
 		  lexer.setState(LexState.EXPR_ENDARG);
 	      }
   break;
 case 257:
 					// line 973 "DefaultRubyParser.y"
   {
                   warnings.warn(getPosition(((Token)yyVals[-3+yyTop])), "don't put space before argument parentheses");
 		  yyVal = ((Node)yyVals[-2+yyTop]);
 	      }
   break;
 case 258:
 					// line 978 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[0+yyTop]));
                   yyVal = new BlockPassNode(support.union(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop])), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 259:
 					// line 983 "DefaultRubyParser.y"
   {
                   yyVal = ((BlockPassNode)yyVals[0+yyTop]);
               }
   break;
 case 261:
 					// line 988 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(getPosition2(((Node)yyVals[0+yyTop])), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 262:
 					// line 991 "DefaultRubyParser.y"
   {
                   yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[0+yyTop]));
               }
   break;
 case 263:
 					// line 995 "DefaultRubyParser.y"
   {
 		  yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[0+yyTop]));
               }
   break;
 case 264:
 					// line 998 "DefaultRubyParser.y"
   {
                   yyVal = support.arg_concat(getPosition(((ListNode)yyVals[-3+yyTop])), ((ListNode)yyVals[-3+yyTop]), ((Node)yyVals[0+yyTop]));
 	      }
   break;
 case 265:
 					// line 1001 "DefaultRubyParser.y"
   {  
                   yyVal = new SplatNode(getPosition(((Token)yyVals[-1+yyTop])), ((Node)yyVals[0+yyTop]));
 	      }
   break;
 case 274:
 					// line 1013 "DefaultRubyParser.y"
   {
                   yyVal = new FCallNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue(), null);
 	      }
   break;
 case 275:
 					// line 1016 "DefaultRubyParser.y"
   {
                   yyVal = new BeginNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));
 	      }
   break;
 case 276:
 					// line 1019 "DefaultRubyParser.y"
   { 
                   lexer.setState(LexState.EXPR_ENDARG); 
               }
   break;
 case 277:
 					// line 1021 "DefaultRubyParser.y"
   {
 		  warnings.warning(getPosition(((Token)yyVals[-4+yyTop])), "(...) interpreted as grouped expression");
                   yyVal = ((Node)yyVals[-3+yyTop]);
 	      }
   break;
 case 278:
 					// line 1025 "DefaultRubyParser.y"
   {
 		  yyVal = ((Node)yyVals[-1+yyTop]);
               }
   break;
 case 279:
 					// line 1028 "DefaultRubyParser.y"
   {
                   yyVal = new Colon2Node(support.union(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 280:
 					// line 1031 "DefaultRubyParser.y"
   {
                   yyVal = new Colon3Node(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 281:
 					// line 1034 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[-3+yyTop]) instanceof SelfNode) {
                       yyVal = new FCallNode(getPosition(((Node)yyVals[-3+yyTop])), "[]", ((Node)yyVals[-1+yyTop]));
                   } else {
                       yyVal = new CallNode(getPosition(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-3+yyTop]), "[]", ((Node)yyVals[-1+yyTop]));
                   }
               }
   break;
 case 282:
 					// line 1041 "DefaultRubyParser.y"
   {
                   ISourcePosition position = support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));
                   if (((Node)yyVals[-1+yyTop]) == null) {
                       yyVal = new ZArrayNode(position); /* zero length array */
                   } else {
                       yyVal = ((Node)yyVals[-1+yyTop]);
                       ((ISourcePositionHolder)yyVal).setPosition(position);
                   }
               }
   break;
 case 283:
 					// line 1050 "DefaultRubyParser.y"
   {
                   yyVal = new HashNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((ListNode)yyVals[-1+yyTop]));
               }
   break;
 case 284:
 					// line 1053 "DefaultRubyParser.y"
   {
 		  yyVal = new ReturnNode(((Token)yyVals[0+yyTop]).getPosition(), null);
               }
   break;
 case 285:
 					// line 1056 "DefaultRubyParser.y"
   {
                   yyVal = support.new_yield(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-1+yyTop]));
               }
   break;
 case 286:
 					// line 1059 "DefaultRubyParser.y"
   {
                   yyVal = new YieldNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), null, false);
               }
   break;
 case 287:
 					// line 1062 "DefaultRubyParser.y"
   {
                   yyVal = new YieldNode(((Token)yyVals[0+yyTop]).getPosition(), null, false);
               }
   break;
 case 288:
 					// line 1065 "DefaultRubyParser.y"
   {
                   yyVal = new DefinedNode(getPosition(((Token)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]));
               }
   break;
 case 289:
 					// line 1068 "DefaultRubyParser.y"
   {
                   yyVal = new FCallNode(support.union(((Token)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])), (String) ((Token)yyVals[-1+yyTop]).getValue(), null, ((IterNode)yyVals[0+yyTop]));
               }
   break;
 case 291:
 					// line 1072 "DefaultRubyParser.y"
   {
 	          if (((Node)yyVals[-1+yyTop]) != null && 
                       ((BlockAcceptingNode)yyVals[-1+yyTop]).getIterNode() instanceof BlockPassNode) {
                       throw new SyntaxException(getPosition(((Node)yyVals[-1+yyTop])), "Both block arg and actual block given.");
 		  }
 		  ((BlockAcceptingNode)yyVals[-1+yyTop]).setIterNode(((IterNode)yyVals[0+yyTop]));
 		  ((Node)yyVals[-1+yyTop]).setPosition(support.union(((Node)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])));
               }
   break;
 case 292:
 					// line 1080 "DefaultRubyParser.y"
   {
                   yyVal = new IfNode(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-2+yyTop]), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 293:
 					// line 1083 "DefaultRubyParser.y"
   {
                   yyVal = new IfNode.Unless(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[-2+yyTop]), ((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 294:
 					// line 1086 "DefaultRubyParser.y"
   { 
                   lexer.getConditionState().begin();
 	      }
   break;
 case 295:
 					// line 1088 "DefaultRubyParser.y"
   {
 		  lexer.getConditionState().end();
 	      }
   break;
 case 296:
 					// line 1090 "DefaultRubyParser.y"
   {
                   yyVal = new WhileNode(support.union(((Token)yyVals[-6+yyTop]), ((Token)yyVals[0+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-6+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 297:
 					// line 1093 "DefaultRubyParser.y"
   {
                   lexer.getConditionState().begin();
               }
   break;
 case 298:
 					// line 1095 "DefaultRubyParser.y"
   {
                   lexer.getConditionState().end();
               }
   break;
 case 299:
 					// line 1097 "DefaultRubyParser.y"
   {
                  yyVal = new UntilNode(getPosition(((Token)yyVals[-6+yyTop])), support.getConditionNode(((Node)yyVals[-4+yyTop])), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-6+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 300:
 					// line 1100 "DefaultRubyParser.y"
   {
                   yyVal = new CaseNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 301:
 					// line 1103 "DefaultRubyParser.y"
   {
                   yyVal = new CaseNode(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])), null, ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 302:
 					// line 1106 "DefaultRubyParser.y"
   {
                   yyVal = new CaseNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), null, new ElseNode(((Node)yyVals[-1+yyTop]), ((Token)yyVals[-2+yyTop])), ((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 303:
 					// line 1109 "DefaultRubyParser.y"
   {
                   lexer.getConditionState().begin();
               }
   break;
 case 304:
 					// line 1111 "DefaultRubyParser.y"
   {
                   lexer.getConditionState().end();
               }
   break;
 case 305:
 					// line 1113 "DefaultRubyParser.y"
   {
                   yyVal = new ForNode(support.union(((Token)yyVals[-8+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-7+yyTop]), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[-4+yyTop]), ((Token)yyVals[-8+yyTop]), ((Token)yyVals[0+yyTop]));
               }
   break;
 case 306:
 					// line 1116 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) {
                       yyerror("class definition in method body");
                   }
 		  support.pushLocalScope();
               }
   break;
 case 307:
 					// line 1121 "DefaultRubyParser.y"
   {
                   yyVal = new ClassNode(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), ((Colon3Node)yyVals[-4+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[-3+yyTop]), ((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop]));
                   support.popCurrentScope();
               }
   break;
 case 308:
 					// line 1125 "DefaultRubyParser.y"
   {
                   yyVal = new Boolean(support.isInDef());
                   support.setInDef(false);
               }
   break;
 case 309:
 					// line 1128 "DefaultRubyParser.y"
   {
                   yyVal = new Integer(support.getInSingle());
                   support.setInSingle(0);
 		  support.pushLocalScope();
               }
   break;
 case 310:
 					// line 1132 "DefaultRubyParser.y"
   {
                   yyVal = new SClassNode(support.union(((Token)yyVals[-7+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-5+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-7+yyTop]), ((Token)yyVals[0+yyTop]));
                   support.popCurrentScope();
                   support.setInDef(((Boolean)yyVals[-4+yyTop]).booleanValue());
                   support.setInSingle(((Integer)yyVals[-2+yyTop]).intValue());
               }
   break;
 case 311:
 					// line 1138 "DefaultRubyParser.y"
   {
                   if (support.isInDef() || support.isInSingle()) { 
                       yyerror("module definition in method body");
                   }
 		  support.pushLocalScope();
               }
   break;
 case 312:
 					// line 1143 "DefaultRubyParser.y"
   {
                   yyVal = new ModuleNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Colon3Node)yyVals[-3+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop]));
                   support.popCurrentScope();
               }
   break;
 case 313:
 					// line 1147 "DefaultRubyParser.y"
   {
                   support.setInDef(true);
 		  support.pushLocalScope();
               }
   break;
 case 314:
 					// line 1150 "DefaultRubyParser.y"
   {
                     /* NOEX_PRIVATE for toplevel */
                   yyVal = new DefnNode(support.union(((Token)yyVals[-5+yyTop]), ((Token)yyVals[0+yyTop])), new ArgumentNode(((Token)yyVals[-4+yyTop]).getPosition(), (String) ((Token)yyVals[-4+yyTop]).getValue()), ((ArgsNode)yyVals[-2+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), Visibility.PRIVATE, ((Token)yyVals[0+yyTop]));
                   support.popCurrentScope();
                   support.setInDef(false);
               }
   break;
 case 315:
 					// line 1156 "DefaultRubyParser.y"
   {
                   lexer.setState(LexState.EXPR_FNAME);
               }
   break;
 case 316:
 					// line 1158 "DefaultRubyParser.y"
   {
                   support.setInSingle(support.getInSingle() + 1);
 		  support.pushLocalScope();
                   lexer.setState(LexState.EXPR_END); /* force for args */
               }
   break;
 case 317:
 					// line 1162 "DefaultRubyParser.y"
   {
                   yyVal = new DefsNode(support.union(((Token)yyVals[-8+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-7+yyTop]), new ArgumentNode(((Token)yyVals[-4+yyTop]).getPosition(), (String) ((Token)yyVals[-4+yyTop]).getValue()), ((ArgsNode)yyVals[-2+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop]));
                   support.popCurrentScope();
                   support.setInSingle(support.getInSingle() - 1);
               }
   break;
 case 318:
 					// line 1167 "DefaultRubyParser.y"
   {
                   yyVal = new BreakNode(((Token)yyVals[0+yyTop]).getPosition());
               }
   break;
 case 319:
 					// line 1170 "DefaultRubyParser.y"
   {
                   yyVal = new NextNode(((Token)yyVals[0+yyTop]).getPosition());
               }
   break;
 case 320:
 					// line 1173 "DefaultRubyParser.y"
   {
                   yyVal = new RedoNode(((Token)yyVals[0+yyTop]).getPosition());
               }
   break;
 case 321:
 					// line 1176 "DefaultRubyParser.y"
   {
                   yyVal = new RetryNode(((Token)yyVals[0+yyTop]).getPosition());
               }
   break;
 case 322:
 					// line 1180 "DefaultRubyParser.y"
   {
                   support.checkExpression(((Node)yyVals[0+yyTop]));
 		  yyVal = ((Node)yyVals[0+yyTop]);
 	      }
   break;
 case 324:
 					// line 1186 "DefaultRubyParser.y"
   {
                 yyVal = new Token(":", lexer.getPosition());
               }
   break;
 case 325:
 					// line 1189 "DefaultRubyParser.y"
   {
                 yyVal = ((Token)yyVals[0+yyTop]);
               }
   break;
 case 326:
 					// line 1192 "DefaultRubyParser.y"
   {
                 yyVal = ((Token)yyVals[-1+yyTop]);
               }
   break;
 case 331:
 					// line 1201 "DefaultRubyParser.y"
   {
 /*mirko: support.union($<ISourcePositionHolder>1.getPosition(), getPosition($<ISourcePositionHolder>1)) ?*/
                   yyVal = new IfNode.ElseIf(((Token)yyVals[-4+yyTop]).getPosition(), support.getConditionNode(((Node)yyVals[-3+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), ((Token)yyVals[-2+yyTop]));
               }
   break;
 case 333:
 					// line 1207 "DefaultRubyParser.y"
   {
                   yyVal = new ElseNode(((Node)yyVals[0+yyTop]), ((Token)yyVals[-1+yyTop]));
               }
   break;
 case 335:
 					// line 1212 "DefaultRubyParser.y"
   {}
   break;
 case 337:
 					// line 1215 "DefaultRubyParser.y"
   {
                   yyVal = new ZeroArgNode(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])));
               }
   break;
 case 338:
 					// line 1218 "DefaultRubyParser.y"
   {
                   yyVal = new ZeroArgNode(((Token)yyVals[0+yyTop]).getPosition());
 	      }
   break;
 case 339:
 					// line 1221 "DefaultRubyParser.y"
   {
                   yyVal = ((Node)yyVals[-1+yyTop]);
 
 		  /* Include pipes on multiple arg type*/
                   if (((Node)yyVals[-1+yyTop]) instanceof MultipleAsgnNode) {
 		      ((Node)yyVals[-1+yyTop]).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
 		  } 
               }
   break;
 case 340:
 					// line 1230 "DefaultRubyParser.y"
   {
                   support.pushBlockScope();
 	      }
   break;
 case 341:
 					// line 1232 "DefaultRubyParser.y"
   {
                   yyVal = new IterNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
                   support.popCurrentScope();
               }
   break;
 case 342:
 					// line 1237 "DefaultRubyParser.y"
   {
 	          if (((Node)yyVals[-1+yyTop]) != null && 
                       ((BlockAcceptingNode)yyVals[-1+yyTop]).getIterNode() instanceof BlockPassNode) {
                       throw new SyntaxException(getPosition(((Node)yyVals[-1+yyTop])), "Both block arg and actual block given.");
                   }
 		  ((BlockAcceptingNode)yyVals[-1+yyTop]).setIterNode(((IterNode)yyVals[0+yyTop]));
 		  ((Node)yyVals[-1+yyTop]).setPosition(support.union(((Node)yyVals[-1+yyTop]), ((IterNode)yyVals[0+yyTop])));
               }
   break;
 case 343:
 					// line 1245 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 344:
 					// line 1248 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 345:
 					// line 1252 "DefaultRubyParser.y"
   {
                   yyVal = support.new_fcall(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 346:
 					// line 1255 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 347:
 					// line 1258 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-3+yyTop]), ((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]), null);
               }
   break;
 case 348:
 					// line 1261 "DefaultRubyParser.y"
   {
                   yyVal = support.new_call(((Node)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]), null, null);
               }
   break;
 case 349:
 					// line 1264 "DefaultRubyParser.y"
   {
                   yyVal = support.new_super(((Node)yyVals[0+yyTop]), ((Token)yyVals[-1+yyTop]));
               }
   break;
 case 350:
 					// line 1267 "DefaultRubyParser.y"
   {
                   yyVal = new ZSuperNode(((Token)yyVals[0+yyTop]).getPosition());
               }
   break;
 case 351:
 					// line 1272 "DefaultRubyParser.y"
   {
                   support.pushBlockScope();
 	      }
   break;
 case 352:
 					// line 1274 "DefaultRubyParser.y"
   {
                   yyVal = new IterNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
                   support.popCurrentScope();
               }
   break;
 case 353:
 					// line 1278 "DefaultRubyParser.y"
   {
                   support.pushBlockScope();
 	      }
   break;
 case 354:
 					// line 1280 "DefaultRubyParser.y"
   {
                   yyVal = new IterNode(support.union(((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop]), support.getCurrentScope(), ((Node)yyVals[-1+yyTop]), ((Token)yyVals[-4+yyTop]), ((Token)yyVals[0+yyTop]));
                   ((ISourcePositionHolder)yyVals[-5+yyTop]).setPosition(support.union(((ISourcePositionHolder)yyVals[-5+yyTop]), ((ISourcePositionHolder)yyVal)));
                   support.popCurrentScope();
               }
   break;
 case 355:
 					// line 1286 "DefaultRubyParser.y"
   {
                   yyVal = new WhenNode(support.union(((Token)yyVals[-4+yyTop]), support.unwrapNewlineNode(((Node)yyVals[-1+yyTop]))), ((ListNode)yyVals[-3+yyTop]), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 357:
 					// line 1291 "DefaultRubyParser.y"
   {
                   yyVal = ((ListNode)yyVals[-3+yyTop]).add(new WhenNode(getPosition(((ListNode)yyVals[-3+yyTop])), ((Node)yyVals[0+yyTop]), null, null));
               }
   break;
 case 358:
 					// line 1294 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(getPosition(((Token)yyVals[-1+yyTop])), new WhenNode(getPosition(((Token)yyVals[-1+yyTop])), ((Node)yyVals[0+yyTop]), null, null));
               }
   break;
 case 361:
 					// line 1301 "DefaultRubyParser.y"
   {
                   Node node;
                   if (((Node)yyVals[-3+yyTop]) != null) {
                      node = support.appendToBlock(support.node_assign(((Node)yyVals[-3+yyTop]), new GlobalVarNode(getPosition(((Token)yyVals[-5+yyTop])), "$!")), ((Node)yyVals[-1+yyTop]));
                      if(((Node)yyVals[-1+yyTop]) != null) {
                         node.setPosition(support.unwrapNewlineNode(((Node)yyVals[-1+yyTop])).getPosition());
                      }
 		  } else {
 		     node = ((Node)yyVals[-1+yyTop]);
                   }
                   yyVal = new RescueBodyNode(getPosition(((Token)yyVals[-5+yyTop]), true), ((Node)yyVals[-4+yyTop]), node, ((RescueBodyNode)yyVals[0+yyTop]), ((Token)yyVals[-5+yyTop]));
 	      }
   break;
 case 362:
 					// line 1313 "DefaultRubyParser.y"
   {yyVal = null;}
   break;
 case 363:
 					// line 1315 "DefaultRubyParser.y"
   {
                   yyVal = new ArrayNode(((Node)yyVals[0+yyTop]).getPosition(), ((Node)yyVals[0+yyTop]));
 	      }
   break;
 case 366:
 					// line 1321 "DefaultRubyParser.y"
   {
                   yyVal = ((Node)yyVals[0+yyTop]);
               }
   break;
 case 368:
 					// line 1326 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[0+yyTop]) != null) {
                       yyVal = new EnsureNode.Keyword(((Token)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
                   } else {
                       yyVal = new EnsureNode.Keyword(((Token)yyVals[-1+yyTop]), new NilNode(getPosition(null)));
                   }
               }
   break;
 case 369:
 					// line 1333 "DefaultRubyParser.y"
   {
                   yyVal = null;
               }
   break;
 case 371:
 					// line 1338 "DefaultRubyParser.y"
   {
                   yyVal = new SymbolNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
               }
   break;
 case 373:
 					// line 1343 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[0+yyTop]) instanceof EvStrNode) {
                       yyVal = new DStrNode(getPosition(((Node)yyVals[0+yyTop]))).add(((Node)yyVals[0+yyTop]));
                   } else {
                       yyVal = ((Node)yyVals[0+yyTop]);
                   }
 	      }
   break;
 case 375:
 					// line 1352 "DefaultRubyParser.y"
   {
                   yyVal = support.literal_concat(getPosition(((Node)yyVals[-1+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 376:
 					// line 1356 "DefaultRubyParser.y"
   {
                   yyVal = ((Node)yyVals[-1+yyTop]);
                   ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
 		  int extraLength = ((String) ((Token)yyVals[-2+yyTop]).getValue()).length() - 1;
 
                   /* We may need to subtract addition offset off of first */
 		  /* string fragment (we optimistically take one off in*/
 		  /* ParserSupport.literal_concat).  Check token length*/
 		  /* and subtract as neeeded.*/
 		  if ((((Node)yyVals[-1+yyTop]) instanceof DStrNode) && extraLength > 0) {
 		     Node strNode = ((DStrNode)((Node)yyVals[-1+yyTop])).get(0);
 		     assert strNode != null;
 		     strNode.getPosition().adjustStartOffset(-extraLength);
 		  }
               }
   break;
 case 377:
 					// line 1372 "DefaultRubyParser.y"
   {
                   ISourcePosition position = support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop]));
 
 		  if (((Node)yyVals[-1+yyTop]) == null) {
 		      yyVal = new XStrNode(position, null);
 		  } else if (((Node)yyVals[-1+yyTop]) instanceof StrNode) {
                       yyVal = new XStrNode(position, (ByteList) ((StrNode)yyVals[-1+yyTop]).getValue().clone());
 		  } else if (((Node)yyVals[-1+yyTop]) instanceof DStrNode) {
                       yyVal = new DXStrNode(position, ((DStrNode)yyVals[-1+yyTop]));
 
                       ((Node)yyVal).setPosition(position);
                   } else {
                       yyVal = new DXStrNode(position).add(((Node)yyVals[-1+yyTop]));
 		  }
               }
   break;
 case 378:
 					// line 1388 "DefaultRubyParser.y"
   {
 		  int options = ((RegexpNode)yyVals[0+yyTop]).getOptions();
 		  Node node = ((Node)yyVals[-1+yyTop]);
 
 		  if (node == null) {
                       yyVal = new RegexpNode(getPosition(((Token)yyVals[-2+yyTop])), ByteList.create(""), options & ~ReOptions.RE_OPTION_ONCE);
 		  } else if (node instanceof StrNode) {
                       yyVal = new RegexpNode(((Node)yyVals[-1+yyTop]).getPosition(), (ByteList) ((StrNode) node).getValue().clone(), options & ~ReOptions.RE_OPTION_ONCE);
 		  } else if (node instanceof DStrNode) {
                       yyVal = new DRegexpNode(getPosition(((Token)yyVals[-2+yyTop])), (DStrNode) node, options, (options & ReOptions.RE_OPTION_ONCE) != 0);
 		  } else {
 		      yyVal = new DRegexpNode(getPosition(((Token)yyVals[-2+yyTop])), options, (options & ReOptions.RE_OPTION_ONCE) != 0).add(node);
                   }
 	       }
   break;
 case 379:
 					// line 1403 "DefaultRubyParser.y"
   {
                    yyVal = new ZArrayNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
 	       }
   break;
 case 380:
 					// line 1406 "DefaultRubyParser.y"
   {
 		   yyVal = ((ListNode)yyVals[-1+yyTop]);
                    ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
 	       }
   break;
 case 381:
 					// line 1411 "DefaultRubyParser.y"
   {
                    yyVal = new ArrayNode(getPosition(null));
 	       }
   break;
 case 382:
 					// line 1414 "DefaultRubyParser.y"
   {
                    yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[-1+yyTop]) instanceof EvStrNode ? new DStrNode(getPosition(((ListNode)yyVals[-2+yyTop]))).add(((Node)yyVals[-1+yyTop])) : ((Node)yyVals[-1+yyTop]));
 	       }
   break;
 case 384:
 					// line 1419 "DefaultRubyParser.y"
   {
                    yyVal = support.literal_concat(getPosition(((Node)yyVals[-1+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
 	       }
   break;
 case 385:
 					// line 1423 "DefaultRubyParser.y"
   {
                    yyVal = new ZArrayNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
 	       }
   break;
 case 386:
 					// line 1426 "DefaultRubyParser.y"
   {
 		   yyVal = ((ListNode)yyVals[-1+yyTop]);
                    ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
 	       }
   break;
 case 387:
 					// line 1431 "DefaultRubyParser.y"
   {
                    yyVal = new ArrayNode(getPosition(null));
 	       }
   break;
 case 388:
 					// line 1434 "DefaultRubyParser.y"
   {
                    yyVal = ((ListNode)yyVals[-2+yyTop]).add(((Node)yyVals[-1+yyTop]));
 	       }
   break;
 case 389:
 					// line 1438 "DefaultRubyParser.y"
   {
                    yyVal = new StrNode(((Token)yyVals[0+yyTop]).getPosition(), ByteList.create(""));
 	       }
   break;
 case 390:
 					// line 1441 "DefaultRubyParser.y"
   {
                    yyVal = support.literal_concat(getPosition(((Node)yyVals[-1+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
 	       }
   break;
 case 391:
 					// line 1445 "DefaultRubyParser.y"
   {
 		   yyVal = null;
 	       }
   break;
 case 392:
 					// line 1448 "DefaultRubyParser.y"
   {
                    yyVal = support.literal_concat(getPosition(((Node)yyVals[-1+yyTop])), ((Node)yyVals[-1+yyTop]), ((Node)yyVals[0+yyTop]));
 	       }
   break;
 case 393:
 					// line 1452 "DefaultRubyParser.y"
   {
                    yyVal = ((Node)yyVals[0+yyTop]);
                }
   break;
 case 394:
 					// line 1455 "DefaultRubyParser.y"
   {
                    yyVal = lexer.getStrTerm();
 		   lexer.setStrTerm(null);
 		   lexer.setState(LexState.EXPR_BEG);
 	       }
   break;
 case 395:
 					// line 1459 "DefaultRubyParser.y"
   {
 		   lexer.setStrTerm(((StrTerm)yyVals[-1+yyTop]));
 	           yyVal = new EvStrNode(support.union(((Token)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), ((Node)yyVals[0+yyTop]));
 	       }
   break;
 case 396:
 					// line 1463 "DefaultRubyParser.y"
   {
 		   yyVal = lexer.getStrTerm();
 		   lexer.setStrTerm(null);
 		   lexer.setState(LexState.EXPR_BEG);
 	       }
   break;
 case 397:
 					// line 1467 "DefaultRubyParser.y"
   {
 		   lexer.setStrTerm(((StrTerm)yyVals[-2+yyTop]));
 
 		   yyVal = support.newEvStrNode(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])), ((Node)yyVals[-1+yyTop]));
 	       }
   break;
 case 398:
 					// line 1473 "DefaultRubyParser.y"
   {
                    yyVal = new GlobalVarNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
                }
   break;
 case 399:
 					// line 1476 "DefaultRubyParser.y"
   {
                    yyVal = new InstVarNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
                }
   break;
 case 400:
 					// line 1479 "DefaultRubyParser.y"
   {
                    yyVal = new ClassVarNode(((Token)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue());
                }
   break;
 case 402:
 					// line 1485 "DefaultRubyParser.y"
   {
                    lexer.setState(LexState.EXPR_END);
                    yyVal = ((Token)yyVals[0+yyTop]);
 		   ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])));
                }
   break;
 case 407:
 					// line 1493 "DefaultRubyParser.y"
   {
                    lexer.setState(LexState.EXPR_END);
 
 		   /* DStrNode: :"some text #{some expression}"*/
                    /* StrNode: :"some text"*/
 		   /* EvStrNode :"#{some expression}"*/
                    if (((Node)yyVals[-1+yyTop]) == null) {
                        yyerror("empty symbol literal");
                    }
 
 		   if (((Node)yyVals[-1+yyTop]) instanceof DStrNode) {
 		       yyVal = new DSymbolNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])), ((DStrNode)yyVals[-1+yyTop]));
 		   } else {
                        ISourcePosition position = support.union(((Node)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop]));
 
                        /* We substract one since tsymbeg is longer than one*/
 		       /* and we cannot union it directly so we assume quote*/
                        /* is one character long and subtract for it.*/
 		       position.adjustStartOffset(-1);
                        ((Node)yyVals[-1+yyTop]).setPosition(position);
 		       
 		       yyVal = new DSymbolNode(support.union(((Token)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
                        ((DSymbolNode)yyVal).add(((Node)yyVals[-1+yyTop]));
                    }
 	       }
   break;
 case 409:
 					// line 1520 "DefaultRubyParser.y"
   {
                    yyVal = ((FloatNode)yyVals[0+yyTop]);
                }
   break;
 case 410:
 					// line 1523 "DefaultRubyParser.y"
   {
                    yyVal = support.negateInteger(((Node)yyVals[0+yyTop]));
 	       }
   break;
 case 411:
 					// line 1526 "DefaultRubyParser.y"
   {
                    yyVal = support.negateFloat(((FloatNode)yyVals[0+yyTop]));
 	       }
   break;
 case 417:
 					// line 1532 "DefaultRubyParser.y"
   { 
 		   yyVal = new Token("nil", ((Token)yyVals[0+yyTop]).getPosition());
                }
   break;
 case 418:
 					// line 1535 "DefaultRubyParser.y"
   {
 		   yyVal = new Token("self", ((Token)yyVals[0+yyTop]).getPosition());
                }
   break;
 case 419:
 					// line 1538 "DefaultRubyParser.y"
   { 
 		   yyVal = new Token("true", ((Token)yyVals[0+yyTop]).getPosition());
                }
   break;
 case 420:
 					// line 1541 "DefaultRubyParser.y"
   {
 		   yyVal = new Token("false", ((Token)yyVals[0+yyTop]).getPosition());
                }
   break;
 case 421:
 					// line 1544 "DefaultRubyParser.y"
   {
 		   yyVal = new Token("__FILE__", ((Token)yyVals[0+yyTop]).getPosition());
                }
   break;
 case 422:
 					// line 1547 "DefaultRubyParser.y"
   {
 		   yyVal = new Token("__LINE__", ((Token)yyVals[0+yyTop]).getPosition());
                }
   break;
 case 423:
 					// line 1551 "DefaultRubyParser.y"
   {
 		   yyVal = support.gettable((String) ((Token)yyVals[0+yyTop]).getValue(), ((Token)yyVals[0+yyTop]).getPosition());
                }
   break;
 case 424:
 					// line 1555 "DefaultRubyParser.y"
   {
                    yyVal = support.assignable(((Token)yyVals[0+yyTop]), null);
                }
   break;
 case 427:
 					// line 1561 "DefaultRubyParser.y"
   {
                    yyVal = null;
                }
   break;
 case 428:
 					// line 1564 "DefaultRubyParser.y"
   {
                    lexer.setState(LexState.EXPR_BEG);
                }
   break;
 case 429:
 					// line 1566 "DefaultRubyParser.y"
   {
                    yyVal = ((Node)yyVals[-1+yyTop]);
                }
   break;
 case 430:
 					// line 1569 "DefaultRubyParser.y"
   {
                    yyerrok();
                    yyVal = null;
                }
   break;
 case 431:
 					// line 1575 "DefaultRubyParser.y"
   {
                    yyVal = ((Node)yyVals[-2+yyTop]);
                    ((ISourcePositionHolder)yyVal).setPosition(support.union(((Token)yyVals[-3+yyTop]), ((Token)yyVals[0+yyTop])));
                    lexer.setState(LexState.EXPR_BEG);
                }
   break;
 case 432:
 					// line 1580 "DefaultRubyParser.y"
   {
                    yyVal = ((Node)yyVals[-1+yyTop]);
                }
   break;
 case 433:
 					// line 1584 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(support.union(((ListNode)yyVals[-5+yyTop]), ((BlockArgNode)yyVals[0+yyTop])), ((ListNode)yyVals[-5+yyTop]), ((ListNode)yyVals[-3+yyTop]), ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 434:
 					// line 1587 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-3+yyTop])), ((ListNode)yyVals[-3+yyTop]), ((ListNode)yyVals[-1+yyTop]), -1, ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 435:
 					// line 1590 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(support.union(((ListNode)yyVals[-3+yyTop]), ((BlockArgNode)yyVals[0+yyTop])), ((ListNode)yyVals[-3+yyTop]), null, ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 436:
 					// line 1593 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(((ISourcePositionHolder)yyVals[-1+yyTop]).getPosition(), ((ListNode)yyVals[-1+yyTop]), null, -1, ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 437:
 					// line 1596 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-3+yyTop])), null, ((ListNode)yyVals[-3+yyTop]), ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 438:
 					// line 1599 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(getPosition(((ListNode)yyVals[-1+yyTop])), null, ((ListNode)yyVals[-1+yyTop]), -1, ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 439:
 					// line 1602 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(getPosition(((Token)yyVals[-1+yyTop])), null, null, ((Integer) ((Token)yyVals[-1+yyTop]).getValue()).intValue(), ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 440:
 					// line 1605 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(getPosition(((BlockArgNode)yyVals[0+yyTop])), null, null, -1, ((BlockArgNode)yyVals[0+yyTop]));
                }
   break;
 case 441:
 					// line 1608 "DefaultRubyParser.y"
   {
                    yyVal = new ArgsNode(support.createEmptyArgsNodePosition(getPosition(null)), null, null, -1, null);
                }
   break;
 case 442:
 					// line 1612 "DefaultRubyParser.y"
   {
                    yyerror("formal argument cannot be a constant");
                }
   break;
 case 443:
 					// line 1615 "DefaultRubyParser.y"
   {
                    yyerror("formal argument cannot be an instance variable");
                }
   break;
 case 444:
 					// line 1618 "DefaultRubyParser.y"
   {
                    yyerror("formal argument cannot be a class variable");
                }
   break;
 case 445:
 					// line 1621 "DefaultRubyParser.y"
   {
                    String identifier = (String) ((Token)yyVals[0+yyTop]).getValue();
                    if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                        yyerror("formal argument must be local variable");
                    } else if (support.getCurrentScope().getLocalScope().isDefined(identifier) >= 0) {
                        yyerror("duplicate argument name");
                    }
 
 		   support.getCurrentScope().getLocalScope().addVariable(identifier);
                    yyVal = ((Token)yyVals[0+yyTop]);
                }
   break;
 case 446:
 					// line 1633 "DefaultRubyParser.y"
   {
                     yyVal = new ListNode(((ISourcePositionHolder)yyVals[0+yyTop]).getPosition());
                     ((ListNode) yyVal).add(new ArgumentNode(((ISourcePositionHolder)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue()));
                }
   break;
 case 447:
 					// line 1637 "DefaultRubyParser.y"
   {
                    ((ListNode)yyVals[-2+yyTop]).add(new ArgumentNode(((ISourcePositionHolder)yyVals[0+yyTop]).getPosition(), (String) ((Token)yyVals[0+yyTop]).getValue()));
                    ((ListNode)yyVals[-2+yyTop]).setPosition(support.union(((ListNode)yyVals[-2+yyTop]), ((Token)yyVals[0+yyTop])));
 		   yyVal = ((ListNode)yyVals[-2+yyTop]);
                }
   break;
 case 448:
 					// line 1643 "DefaultRubyParser.y"
   {
                    String identifier = (String) ((Token)yyVals[-2+yyTop]).getValue();
 
                    if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                        yyerror("formal argument must be local variable");
                    } else if (support.getCurrentScope().getLocalScope().isDefined(identifier) >= 0) {
                        yyerror("duplicate optional argument name");
                    }
 		   support.getCurrentScope().getLocalScope().addVariable(identifier);
                    yyVal = support.assignable(((Token)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 449:
 					// line 1655 "DefaultRubyParser.y"
   {
                   yyVal = new BlockNode(getPosition(((Node)yyVals[0+yyTop]))).add(((Node)yyVals[0+yyTop]));
               }
   break;
 case 450:
 					// line 1658 "DefaultRubyParser.y"
   {
                   yyVal = support.appendToBlock(((ListNode)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop]));
               }
   break;
 case 453:
 					// line 1664 "DefaultRubyParser.y"
   {
                   String identifier = (String) ((Token)yyVals[0+yyTop]).getValue();
 
                   if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                       yyerror("rest argument must be local variable");
                    } else if (support.getCurrentScope().getLocalScope().isDefined(identifier) >= 0) {
                       yyerror("duplicate rest argument name");
                   }
 		  ((Token)yyVals[-1+yyTop]).setValue(new Integer(support.getCurrentScope().getLocalScope().addVariable(identifier)));
                   yyVal = ((Token)yyVals[-1+yyTop]);
               }
   break;
 case 454:
 					// line 1675 "DefaultRubyParser.y"
   {
                   ((Token)yyVals[0+yyTop]).setValue(new Integer(-2));
                   yyVal = ((Token)yyVals[0+yyTop]);
               }
   break;
 case 457:
 					// line 1682 "DefaultRubyParser.y"
   {
                   String identifier = (String) ((Token)yyVals[0+yyTop]).getValue();
 
                   if (IdUtil.getVarType(identifier) != IdUtil.LOCAL_VAR) {
                       yyerror("block argument must be local variable");
 		  } else if (support.getCurrentScope().getLocalScope().isDefined(identifier) >= 0) {
                       yyerror("duplicate block argument name");
                   }
                   yyVal = new BlockArgNode(support.union(((Token)yyVals[-1+yyTop]), ((Token)yyVals[0+yyTop])), support.getCurrentScope().getLocalScope().addVariable(identifier), identifier);
               }
   break;
 case 458:
 					// line 1693 "DefaultRubyParser.y"
   {
                   yyVal = ((BlockArgNode)yyVals[0+yyTop]);
               }
   break;
 case 459:
 					// line 1696 "DefaultRubyParser.y"
   {
 	          yyVal = null;
 	      }
   break;
 case 460:
 					// line 1700 "DefaultRubyParser.y"
   {
                   if (!(((Node)yyVals[0+yyTop]) instanceof SelfNode)) {
 		      support.checkExpression(((Node)yyVals[0+yyTop]));
 		  }
 		  yyVal = ((Node)yyVals[0+yyTop]);
               }
   break;
 case 461:
 					// line 1706 "DefaultRubyParser.y"
   {
                   lexer.setState(LexState.EXPR_BEG);
               }
   break;
 case 462:
 					// line 1708 "DefaultRubyParser.y"
   {
                   if (((Node)yyVals[-2+yyTop]) instanceof ILiteralNode) {
                       yyerror("Can't define single method for literals.");
                   }
 		  support.checkExpression(((Node)yyVals[-2+yyTop]));
                   yyVal = ((Node)yyVals[-2+yyTop]);
               }
   break;
 case 463:
 					// line 1718 "DefaultRubyParser.y"
   { /* [!null]*/
                   yyVal = new ArrayNode(getPosition(null));
               }
   break;
 case 464:
 					// line 1721 "DefaultRubyParser.y"
   { /* [!null]*/
                   yyVal = ((ListNode)yyVals[-1+yyTop]);
               }
   break;
 case 465:
 					// line 1724 "DefaultRubyParser.y"
   {
                   if (((ListNode)yyVals[-1+yyTop]).size() % 2 != 0) {
                       yyerror("Odd number list for Hash.");
                   }
                   yyVal = ((ListNode)yyVals[-1+yyTop]);
               }
   break;
 case 467:
 					// line 1733 "DefaultRubyParser.y"
   { /* [!null]*/
                   yyVal = ((ListNode)yyVals[-2+yyTop]).addAll(((ListNode)yyVals[0+yyTop]));
               }
   break;
 case 468:
 					// line 1738 "DefaultRubyParser.y"
   { /* [!null]*/
                   yyVal = new ArrayNode(support.union(((Node)yyVals[-2+yyTop]), ((Node)yyVals[0+yyTop])), ((Node)yyVals[-2+yyTop])).add(((Node)yyVals[0+yyTop]));
               }
   break;
 case 488:
 					// line 1750 "DefaultRubyParser.y"
   {
                   yyerrok();
                   yyVal = new Token(";", lexer.getPosition());
               }
   break;
 case 489:
 					// line 1754 "DefaultRubyParser.y"
   {
                   yyVal = new Token(";", lexer.getPosition());
               }
   break;
 case 491:
 					// line 1759 "DefaultRubyParser.y"
   {
                   yyerrok();
               }
   break;
 case 492:
 					// line 1763 "DefaultRubyParser.y"
   {
                   yyVal = null;
               }
   break;
 case 493:
 					// line 1767 "DefaultRubyParser.y"
   {  
                   yyVal = null;
 	      }
   break;
 					// line 7449 "-"
         }
         yyTop -= yyLen[yyN];
         yyState = yyStates[yyTop];
         int yyM = yyLhs[yyN];
         if (yyState == 0 && yyM == 0) {
           yyState = yyFinal;
           if (yyToken < 0) {
             yyToken = yyLex.advance() ? yyLex.token() : 0;
           }
           if (yyToken == 0) {
             return yyVal;
           }
           continue yyLoop;
         }
         if ((yyN = yyGindex[yyM]) != 0 && (yyN += yyState) >= 0
             && yyN < yyTable.length && yyCheck[yyN] == yyState)
           yyState = yyTable[yyN];
         else
           yyState = yyDgoto[yyM];
         continue yyLoop;
       }
     }
   }
 
 					// line 1772 "DefaultRubyParser.y"
 
     /** The parse method use an lexer stream and parse it to an AST node 
      * structure
      */
     public RubyParserResult parse(RubyParserConfiguration configuration, LexerSource source) {
         support.reset();
         support.setConfiguration(configuration);
         support.setResult(new RubyParserResult());
         
         lexer.reset();
         lexer.setSource(source);
         try {
 	    //yyparse(lexer, new jay.yydebug.yyAnim("JRuby", 9));
 	    //yyparse(lexer, new jay.yydebug.yyDebugAdapter());
 	    yyparse(lexer, null);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (yyException e) {
             e.printStackTrace();
         }
         
         return support.getResult();
     }
 
     // +++
     // Helper Methods
     
     void yyerrok() {}
 
     /**
      * Since we can recieve positions at times we know can be null we
      * need an extra safety net here.
      */
     private ISourcePosition getPosition2(ISourcePositionHolder pos) {
         return pos == null ? lexer.getPosition(null, false) : pos.getPosition();
     }
 
     private ISourcePosition getPosition(ISourcePositionHolder start) {
         return getPosition(start, false);
     }
 
     private ISourcePosition getPosition(ISourcePositionHolder start, boolean inclusive) {
         if (start != null) {
 	    return lexer.getPosition(start.getPosition(), inclusive);
 	} 
 	
 	return lexer.getPosition(null, inclusive);
     }
 }
 					// line 7529 "-"
