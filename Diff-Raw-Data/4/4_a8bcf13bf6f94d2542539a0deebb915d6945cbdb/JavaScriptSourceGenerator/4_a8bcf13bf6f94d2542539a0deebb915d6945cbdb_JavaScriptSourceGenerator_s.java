 // ***************************************************************************
 // Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // *  Redistributions of source code must retain the above copyright notice,
 //    this list of conditions and the following disclaimer.
 // *  Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the distribution.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 // TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 // CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 // EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 // PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 // OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 // OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 // ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 // **************************************************************************
 
 //ifdef JAVA
 package org.GreenTeaScript;
 import java.util.ArrayList;
 //endif VAJA
 
 public class JavaScriptSourceGenerator extends SourceGenerator {
 	/*field*/private boolean UseLetKeyword;
 	/*field*/private boolean IsNodeJS;
 
 	JavaScriptSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.IsNodeJS = LibGreenTea.EqualsString(TargetCode, "nodejs");
 	}
 
 	public  String VisitBlockJS(GtNode Node) {
 		/*local*/String Code = "";
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			/*local*/String Statement = this.VisitNode(CurrentNode);
 			if(Statement.trim().length() >0) {
 				Code += this.GetIndentString() + Statement + ";" + this.LineFeed;
 			}
 			CurrentNode = CurrentNode.NextNode;
 		}
 		return Code;
 	}
 
 	public  String VisitBlockJSWithIndent(GtNode Node) {
 		/*local*/String Code = "";
 		Code += "{" + this.LineFeed;
 		this.Indent();
 		Code += this.VisitBlockJS(Node);
 		this.UnIndent();
 		Code += this.GetIndentString() + "}";
 		return Code;
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Left = this.VisitNode(Node.LeftNode);
 		/*local*/String Right = this.VisitNode(Node.RightNode);
 		/*local*/String Source = "(" + SourceGenerator.GenerateApplyFunc2(Node.Func, FuncName, Left, Right) + ")";
 		/*local*/String operator = Node.Token.ParsedText;
 		if(LibGreenTea.EqualsString(operator, "/") /*&& Node.Type == Context.IntType*/ ) {
 			Source = "(" + Source + " | 0)";
 		}
 		this.PushSourceCode(Source);
 	}
 
 	@Override public void VisitVarNode(VarNode Node) {
 		/*local*/String VarName = Node.NativeName;
 		/*local*/String Source = (this.UseLetKeyword ? "let " : "var ") + " " + VarName;
 		if(Node.InitNode != null) {
 			Node.InitNode.Evaluate(this);
 			Source += " = " + this.PopSourceCode();
 		}
 		Source +=  ";";
		this.VisitBlockJSWithIndent(Node.BlockNode);
		this.PushSourceCode(Source + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String ThenBlock = this.VisitBlockJSWithIndent(Node.ThenNode);
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		/*local*/String Source = "if(" + CondExpr + ") " + ThenBlock;
 		if(Node.ElseNode != null) {
 			Source = Source + " else " + this.VisitBlockJSWithIndent(Node.ElseNode);
 		}
 		this.PushSourceCode(Source);
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String LoopBody = this.VisitBlockJSWithIndent(Node.LoopBody);
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		this.PushSourceCode("while(" + CondExpr + ") {" + LoopBody + "}");
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/*local*/String LoopBody = this.VisitBlockJSWithIndent(Node.LoopBody);
 		/*local*/String IterExpr = this.VisitNode(Node.IterExpr);
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		this.PushSourceCode("for(;" + CondExpr + "; " + IterExpr + ") {" + LoopBody + "}");
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		/*local*/String LoopBody = this.VisitBlockJSWithIndent(Node.LoopBody);
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		this.PushSourceCode("do {" + LoopBody + "}while(" + CondExpr + ");");
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try ";
 		Code += this.VisitBlockJSWithIndent(Node.TryBlock);
 		/*local*/VarNode Val = (/*cast*/VarNode) Node.CatchExpr;
 		Code += " catch (" + Val.Type.toString() + " " + Val.NativeName + ") ";
 		Code += this.VisitBlockJSWithIndent(Node.CatchBlock);
 		if(Node.FinallyBlock != null) {
 			Code += " finally " + this.VisitBlockJSWithIndent(Node.FinallyBlock);
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		/*local*/String Expr = this.VisitNode(Node.Expr);
 		this.PushSourceCode("throw " + Expr);
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Expr = Node.Token.ParsedText;
 		this.PushSourceCode("(function() {throw new Error(\"" + Expr + "\") })()");
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> NameList, GtNode Body) {
 		this.FlushErrorReport();
 		/*local*/int ArgCount = Func.Types.length - 1;
 		/*local*/String Code = "var " + Func.GetNativeFuncName() + " = (function(";
 		/*local*/int i = 0;
 		while(i < ArgCount) {
 			if(i > 0) {
 				Code = Code + ", ";
 			}
 			Code = Code + NameList.get(i);
 			i = i + 1;
 		}
 		Code = Code + ") " + this.VisitBlockJSWithIndent(Body) + ");";
 		this.WriteLineCode(Code);
 	}
 
 /**
 JavaScript code to be generated:
 
 var CLASS = (function (_super) {
     __extends(CLASS, _super);                                // Derived class only.
     function CLASS(param) {                                   // Constructor.
         _super.call(this, param);
         this.FIELD = param;                                      // Field definition and initialization.
     };
     CLASS.STATIC_FIELD = "value";                      // Static fields
     
     CLASS.prototype.METHOD = function () {    // Methods.
     };
     CLASS.STATIC_METHOD = function () {         // Static methods.
     };
     return CLASS;
 })(SUPERCLASS);
  */
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		/*local*/String TypeName = Type.ShortClassName;
 		/*local*/String Program = this.GetIndentString() + "var " + TypeName + " = (function() {" + this.LineFeed;
 //		if(Type.SuperType != null) {
 //			Program += "(" + Type.SuperType.ShortClassName + ")";
 //		}
 		this.Indent();
 		Program += this.GetIndentString() + "function " + TypeName + "() {" + this.LineFeed;
 		this.Indent();
 		/*local*/int i = 0;
 		while(i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = this.NullLiteral;
 			}
 			Program += this.GetIndentString() + this.GetRecvName() + "." + FieldInfo.NativeName + " = " + InitValue + ";" + this.LineFeed;
 			i = i + 1;
 		}
 		this.UnIndent();
 		Program += this.GetIndentString() + "};" + this.LineFeed;
 		Program += this.GetIndentString() + "return " + TypeName + ";" + this.LineFeed;
 		this.UnIndent();
 		Program += this.GetIndentString() + "})();" + this.LineFeed;
 		this.WriteLineCode(Program);
 	}
 	@Override public Object Eval(GtNode Node) {
 		/*local*/String ret = this.VisitBlockJS(Node);
 		this.WriteLineCode(ret);
 		return ret;
 	}
 
 	@Override public void StartCompilationUnit() {
 		if(this.IsNodeJS) {
 			this.WriteLineCode("var assert = require('assert');");
 		}
 		else {			
 			this.WriteLineCode("var assert = console.assert;");
 		}
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode(MainFuncName + "();");
 	}
 }
