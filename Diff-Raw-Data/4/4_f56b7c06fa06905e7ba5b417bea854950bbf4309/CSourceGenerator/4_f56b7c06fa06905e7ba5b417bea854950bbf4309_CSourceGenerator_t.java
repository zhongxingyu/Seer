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
 
 //ifdef  JAVA
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class CSourceGenerator extends SourceGenerator {
 	///*field*/ConstantFolder Opt;
 	CSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		//this.Opt = new ConstantFolder(TargetCode, OutputFile, GeneratorFlag);
 		this.TrueLiteral  = "1";
 		this.FalseLiteral = "0";
 		this.NullLiteral = "NULL";
 		this.MemberAccessOperator = "->";
 	}
 	@Override public void InitContext(GtClassContext Context) {
 		super.InitContext(Context);
 		//this.Opt.InitContext(Context);
 	}
 
 	private String GetLocalType(GtType Type, boolean IsPointer) {
 		if(Type.IsDynamic() || Type.IsNative()) {
 			return Type.ShortClassName;
 		}
 		/*local*/String TypeName = "struct " + Type.ShortClassName;
 		if(IsPointer) {
 			TypeName += "*";
 		}
 		return TypeName;
 
 	}
 	public String NativeTypeName(GtType Type) {
 		return this.GetLocalType(Type, false);
 	}
 
 	public String LocalTypeName(GtType Type) {
 		return this.GetLocalType(Type, true);
 	}
 
 	public String GreenTeaTypeName(GtType Type) {
 		return Type.ShortClassName;
 	}
 
 	@Override protected String GetNewOperator(GtType Type) {
 		/*local*/String TypeName = this.GreenTeaTypeName(Type);
 		return "NEW_" + TypeName + "()";
 	}
 
 	public void VisitBlockEachStatementWithIndent(GtNode Node, boolean NeedBlock) {
 		/*local*/String Code = "";
 		if(NeedBlock) {
 			Code += "{" + this.LineFeed;
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			if(!this.IsEmptyBlock(CurrentNode)) {
 				/*local*/String Stmt = this.VisitNode(CurrentNode);
 				/*local*/String SemiColon = "";
 				/*local*/String LineFeed = "";
 				if(!Stmt.endsWith(";")) {
 					SemiColon = ";";
 				}
 				if(!Stmt.endsWith(this.LineFeed)) {
 					LineFeed = this.LineFeed;
 				}
 				Code += this.GetIndentString() + Stmt + SemiColon + LineFeed;
 			}
 			CurrentNode = CurrentNode.NextNode;
 		}
 		if(NeedBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString() + "}";
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while(" + this.VisitNode(Node.CondExpr) + ")";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody, true);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		/*local*/String Program = "do";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody, true);
 		Program += " while(" + this.VisitNode(Node.CondExpr) + ")";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/*local*/String Cond = this.VisitNode(Node.CondExpr);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for(; " + Cond  + "; " + Iter + ")";
 		Program += this.VisitNode(Node.LoopBody);
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		/*local*/String Program = this.VisitNode(Node.Expr);
 		/*local*/String FieldName = Node.Func.FuncName;
 		/*local*/GtType RecvType = Node.Func.GetRecvType();
 		if(Node.Expr.Type == RecvType) {
 			Program = Program + "->" + FieldName;
 		}
 		else {
 			Program = "GT_GetField(" + this.LocalTypeName(RecvType) + ", " + Program + ", " + FieldName + ")";
 		}
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		/*local*/String Type = this.LocalTypeName(Node.DeclType);
 		/*local*/String VarName = Node.VariableName;
 		/*local*/String Code = Type + " " + VarName;
 		/*local*/boolean CreateNewScope = true;
 		if(Node.InitNode != null) {
 			Code += " = " + this.VisitNode(Node.InitNode);
 		}
 		Code += ";" + this.LineFeed;
 		if(CreateNewScope) {
 			Code += this.GetIndentString();
 		}
 		this.VisitBlockEachStatementWithIndent(Node.BlockNode, CreateNewScope);
 		this.PushSourceCode(Code + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		this.VisitBlockEachStatementWithIndent(Node.ThenNode, true);
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String Code = "if(" + CondExpr + ") " + ThenBlock;
 		if(Node.ElseNode != null) {
 			this.VisitBlockEachStatementWithIndent(Node.ElseNode, true);
 			Code += " else " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		/*local*/String Code = "switch (" + this.VisitNode(Node.MatchNode) + ") {" + this.LineFeed;
 		/*local*/int i = 0;
 		while(i < Node.CaseList.size()) {
			/*local*/GtNode Case  = Node.CaseList.get(i);
			/*local*/GtNode Block = Node.CaseList.get(i+1);
 			Code += this.GetIndentString() + "case " + this.VisitNode(Case) + ":";
 			if(this.IsEmptyBlock(Block)) {
 				this.Indent();
 				Code += this.LineFeed + this.GetIndentString() + "/* fall-through */" + this.LineFeed;
 				this.UnIndent();
 			}
 			else {
 				this.VisitBlockEachStatementWithIndent(Block, true);
 				Code += this.PopSourceCode() + this.LineFeed;
 			}
 			i = i + 2;
 		}
 		if(Node.DefaultBlock != null) {
 			Code += this.GetIndentString() + "default:" + this.LineFeed;
 			this.VisitBlockEachStatementWithIndent(Node.DefaultBlock, true);
 			Code += this.PopSourceCode();
 		}
 		Code += this.GetIndentString() + "}";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try ";
 		this.VisitBlockEachStatementWithIndent(Node.TryBlock, true);
 		Code += this.PopSourceCode();
 		/*local*/LetNode Val = (/*cast*/LetNode) Node.CatchExpr;
 		Code += " catch (" + Val.Type.toString() + " " + Val.VariableName + ") ";
 		this.VisitBlockEachStatementWithIndent(Node.CatchBlock, true);
 		Code += this.PopSourceCode();
 		if(Node.FinallyBlock != null) {
 			this.VisitBlockEachStatementWithIndent(Node.FinallyBlock, true);
 			Code += " finally " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		/*local*/String Code = "throw " + this.VisitNode(Node.Expr);
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "system(";
 		/*local*/int i = 0;
 		/*local*/String Command = "String __Command = ";
 		while(i < GtStatic.ListSize(Node.Params)) {
 			/*local*/GtNode Param = Node.Params.get(i);
 			if(i != 0) {
 				Command += " + ";
 			}
 			Param.Evaluate(this);
 			Command += "(" + this.PopSourceCode() + ")";
 			i = i + 1;
 		}
 		Code = Command + ";" + this.LineFeed + this.GetIndentString() + Code + "__Command)";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Code = "";
 		if(!Func.Is(ExportFunc)) {
 			Code = "static ";
 		}
 		//Body = this.Opt.Fold(Body);
 		/*local*/String RetTy = this.LocalTypeName(Func.GetReturnType());
 		Code += RetTy + " " + Func.GetNativeFuncName() + "(";
 		/*local*/int i = 0;
 		while(i < ParamNameList.size()) {
 			/*local*/String ParamTy = this.LocalTypeName(Func.GetFuncParamType(i));
 			if(i > 0) {
 				Code += ", ";
 			}
 			Code += ParamTy + " " + ParamNameList.get(i);
 			i = i + 1;
 		}
 		Code += ")";
 		this.VisitBlockEachStatementWithIndent(Body, true);
 		Code += this.PopSourceCode();
 		this.WriteLineCode(Code);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		//Node = this.Opt.Fold(Node);
 		this.VisitBlockEachStatementWithIndent(Node, false);
 		/*local*/String Code = this.PopSourceCode();
 		if(LibGreenTea.EqualsString(Code, ";" + this.LineFeed)) {
 			return "";
 		}
 		this.WriteLineCode(Code);
 		return Code;
 	}
 
 	@Override public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		/*local*/String TypeName = Type.ShortClassName;
 		/*local*/String LocalType = this.LocalTypeName(Type);
 		/*local*/String Program = this.GetIndentString() + "struct " + TypeName + " {" + this.LineFeed;
 		this.Indent();
 		if(Type.SuperType != null) {
 			Program += this.GetIndentString() + "// " + this.LocalTypeName(Type.SuperType) + " __base;" + this.LineFeed;
 		}
 		/*local*/int i = 0;
 		while(i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/GtType VarType = FieldInfo.Type;
 			/*local*/String VarName = FieldInfo.NativeName;
 			Program += this.GetIndentString() + this.LocalTypeName(VarType) + " " + VarName + ";" + this.LineFeed;
 			i = i + 1;
 		}
 		this.UnIndent();
 		Program += this.GetIndentString() + "};" + this.LineFeed;
 		Program += this.GetIndentString() + LocalType + " NEW_" + TypeName + "() {" + this.LineFeed;
 		this.Indent();
 		i = 0;
 		Program +=  this.GetIndentString() + LocalType + " " + this.GetRecvName() + " = " + "GT_New("+LocalType+");" + this.LineFeed;
 		while(i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String VarName = FieldInfo.NativeName;
 			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = this.NullLiteral;
 			}
 			Program += this.GetIndentString() + this.GetRecvName() + "->" + VarName + " = " + InitValue + ";" + this.LineFeed;
 			i = i + 1;
 		}
 		Program += this.GetIndentString() + "return " + this.GetRecvName() + ";" + this.LineFeed;
 		this.UnIndent();
 		Program += this.GetIndentString() + "};";
 		
 		this.WriteLineCode(Program);
 	}
 
 	@Override public void StartCompilationUnit() {
 		this.WriteLineCode("#include \"GreenTeaPlus.h\"");
 	}
 
 	@Override public String GetRecvName() {
 		return "self";
 	}
 }
