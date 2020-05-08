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
 
 public class GtType extends GreenTeaUtils {
 	/*field*/public final GtParserContext	Context;
 	/*field*/public GtNameSpace     PackageNameSpace;
 	/*field*/public int					TypeFlag;
 	/*field*/public int                    TypeId;
 	/*field*/public String			ShortName;
 	/*field*/public GtType					SuperType;
 	/*field*/public GtType			ParentMethodSearch;
 	/*field*/public GtType					BaseType;
 	/*field*/public GtType[]				TypeParams;
 	/*field*/public Object          TypeBody;
 	/*field*/public Object			DefaultNullValue;
 
 	GtType/*constructor*/(GtParserContext Context, int TypeFlag, String ShortName, Object DefaultNullValue, Object TypeBody) {
 		this.Context = Context;
 		this.TypeFlag = TypeFlag;
 		this.ShortName = ShortName;
 		this.SuperType = null;
 		this.BaseType = this;
 		this.ParentMethodSearch = Context.TopType;
 		this.DefaultNullValue = DefaultNullValue;
 		this.TypeBody = TypeBody;
 		if(!IsFlag(TypeFlag, TypeVariable)) {
 			this.TypeId = Context.TypePools.size();
 			Context.TypePools.add(this);
 		}
 		this.TypeParams = null;
 	}
 
 	public GtType CreateSubType(int ClassFlag, String ClassName, Object DefaultNullValue, Object NativeSpec) {
 		/*local*/GtType SubType = new GtType(this.Context, ClassFlag, ClassName, DefaultNullValue, NativeSpec);
 		SubType.SuperType = this;
 		SubType.ParentMethodSearch = this;
 		return SubType;
 	}
 
 	// Note Don't call this directly. Use Context.GetGenericType instead.
 	public GtType CreateGenericType(int BaseIndex, ArrayList<GtType> TypeList, String ShortName) {
 		/*local*/int i = BaseIndex;
 		/*local*/int TypeVariableFlag = 0;
 		while(i < TypeList.size()) {
 			if(TypeList.get(i).HasTypeVariable()) {
 				TypeVariableFlag = GenericVariable;
 				break;
 			}
 			i = i + 1;
 		}
 		/*local*/GtType GenericType = new GtType(this.Context, this.TypeFlag | TypeVariableFlag, ShortName, null, null);
 		GenericType.BaseType = this.BaseType;
 		GenericType.ParentMethodSearch = this.BaseType;
 		GenericType.SuperType = this.SuperType;
 		GenericType.TypeParams = LibGreenTea.CompactTypeList(BaseIndex, TypeList);
 		LibGreenTea.VerboseLog(VerboseType, "new generic type: " + GenericType.ShortName + ", ClassId=" + GenericType.TypeId);
 		return GenericType;
 	}
 
 	public final boolean IsAbstract() {
 		return (this.TypeBody == null && this.SuperType == this.Context.StructType/*default*/);
 	}
 
 	public final boolean IsNative() {
 		return IsFlag(this.TypeFlag, NativeType);
 	}
 
 	public final boolean IsDynamic() {
 		return IsFlag(this.TypeFlag, DynamicType);
 	}
 
 	public final boolean IsGenericType() {
 		return (this.TypeParams != null);
 	}
 
 	public final boolean IsFuncType() {
 		return (this.BaseType == this.Context.FuncType);
 	}
 
 	public final boolean IsTopType() {
 		return (this == this.Context.TopType);
 	}
 
 	public final boolean IsVoidType() {
 		return (this == this.Context.VoidType);
 	}
 
 	public final boolean IsVarType() {
 		return (this == this.Context.VarType);
 	}
 
 	public final boolean IsAnyType() {
 		return (this == this.Context.AnyType);
 	}
 
 	public final boolean IsTypeType() {
 		return (this == this.Context.TypeType);
 	}
 
 	public final boolean IsBooleanType() {
 		return (this == this.Context.BooleanType);
 	}
 
 	public final boolean IsIntType() {
 		return (this == this.Context.IntType);
 	}
 
 	public final boolean IsStringType() {
 		return (this == this.Context.StringType);
 	}
 
 	public final boolean IsArrayType() {
 		return (this == this.Context.ArrayType);
 	}
 
 	public final boolean IsEnumType() {
 		return IsFlag(this.TypeFlag, EnumType);
 	}
 	public final void SetUnrevealedType(GtType StrongType) {
 		this.BaseType = StrongType;
 		this.TypeFlag |= UnrevealedType;
 		this.ShortName = "_" + this.ShortName + "_";
 	}
 	public final boolean IsUnrevealedType() {
 		return IsFlag(this.TypeFlag, UnrevealedType);
 	}
 	@Override public String toString() {
 		return this.ShortName;
 	}
 
 	public final String GetNativeName() {
 		if(IsFlag(this.TypeFlag, ExportType)) {
 			return this.ShortName;
 		}
 		else {
 			return this.BaseType.ShortName + NativeNameSuffix + this.TypeId;
 		}
 	}
 
 	public final String GetUniqueName() {
 		if(IsFlag(this.TypeFlag, TypeVariable)) {
 			return this.ShortName;
 		}
 		else {
 			if(LibGreenTea.DebugMode) {
 				return this.BaseType.ShortName + NativeNameSuffix + this.TypeId;
 			}
 			else {
 				return NativeNameSuffix + this.TypeId;
 			}
 		}
 	}
 
 	public final boolean Accept(GtType Type) {
 		if(this == Type || this == this.Context.AnyType) {
 			return true;
 		}
 		/*local*/GtType SuperClass = this.SuperType;
 		while(SuperClass != null) {
 			if(SuperClass == Type) {
 				return true;
 			}
 			SuperClass = SuperClass.SuperType;
 		}
 		return this.Context.CheckSubType(Type, this);
 	}
 
//	public final boolean Accept(GtType Type) {
//		boolean b = this.Accept_(Type);
//		System.err.println("" + this + " accepts " + Type + " ? " + b);
//		return b;
//	}

	
 	public final boolean AcceptValue(Object Value) {
 		return (Value != null) ? this.Accept(this.Context.GuessType(Value)) : true;
 	}
 
 	public void SetClassField(GtClassField ClassField) {
 		this.TypeBody = ClassField;
 	}
 
 	public boolean IsDynamicNaitiveLoading() {
 		return this.IsNative() /*&& !IsFlag(this.TypeFlag, CommonType)*/;
 	}
 
 	public final boolean IsTypeVariable() {   // T
 		return IsFlag(this.TypeFlag, TypeVariable);
 	}
 
 	public final boolean HasTypeVariable() {
 		return IsFlag(this.TypeFlag, TypeVariable) || IsFlag(this.TypeFlag, GenericVariable);
 	}
 
 	public int AppendTypeVariable(GtNameSpace GenericNameSpace, int Count) {
 		if(IsFlag(this.TypeFlag, TypeVariable)) {
 			/*local*/GtType TypeVar = GenericNameSpace.GetType(this.ShortName);
 			if(TypeVar != null && TypeVar.IsTypeVariable()) {
 				return Count;
 			}
 			GenericNameSpace.SetSymbol(this.ShortName, this, null);
 			return Count + 1;
 		}
 		if(IsFlag(this.TypeFlag, GenericVariable)) {
 			/*local*/int i = 0;
 			while(i < this.TypeParams.length) {
 				Count = this.TypeParams[i].AppendTypeVariable(GenericNameSpace, Count);
 				i += 1;
 			}
 		}
 		return Count;
 	}
 
 	private GtType GivenParamType(GtType GivenType, int ParamIndex) {
 		if(GivenType.BaseType == this.BaseType && GivenType.TypeParams.length == this.TypeParams.length) {
 			return GivenType.TypeParams[ParamIndex];
 		}
 		return GivenType;
 	}
 	
 	public GtType RealType(GtNameSpace GenericNameSpace, GtType GivenType) {
 		if(IsFlag(this.TypeFlag, TypeVariable)) {
 			GtType TypeVar = GenericNameSpace.GetType(this.ShortName);
 			//System.err.println("TypeVar="+this.ShortName + ", " + TypeVar);
 			if(TypeVar != null && TypeVar.IsTypeVariable()) {
 				GenericNameSpace.SetSymbol(this.ShortName, GivenType, null);
 				return GivenType;
 			}
 			else {
 				return TypeVar;
 			}
 		}
 		if(IsFlag(this.TypeFlag, GenericVariable)) {
 			/*local*/int i = 0;
 			/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 			while(i < this.TypeParams.length) {
 				GtType RealParamType = this.TypeParams[i].RealType(GenericNameSpace, GivenParamType(GivenType, i));
 				TypeList.add(RealParamType);
 				i += 1;
 			}
 			return this.Context.GetGenericType(this.BaseType, 0, TypeList, true);
 		}
 		return this;
 	}
 
 	public boolean Match(GtNameSpace GenericNameSpace, GtType GivenType) {
		
 		if(IsFlag(this.TypeFlag, TypeVariable)) {
 			GtType TypeVar = GenericNameSpace.GetType(this.ShortName);
 			if(TypeVar.IsTypeVariable()) {
 				//System.err.println("updating "+ this.ShortName + " " + GivenType);
 				GenericNameSpace.SetSymbol(this.ShortName, GivenType, null);
 				return true;
 			}
 			return TypeVar.Accept(GivenType);
 		}
 		if(IsFlag(this.TypeFlag, GenericVariable)) {
 			if(GivenType.BaseType == this.BaseType && GivenType.TypeParams.length == this.TypeParams.length) {
 				/*local*/int i = 0;
 				while(i < this.TypeParams.length) {
 					if(!this.TypeParams[i].Match(GenericNameSpace, GivenType.TypeParams[i])) {
 						return false;
 					}
 					i += 1;
 				}
 				return true;
 			}
 			return false;
 		}
 		return this.Accept(GivenType);
 	}
 
 
 //	public boolean Match(GtNameSpace GenericNameSpace, GtType GivenType) {
 //		boolean b = this.Match_(GenericNameSpace, GivenType);
 //		System.err.println("matching.. " + this + ", given = " + GivenType + ", results=" + b);
 //		return b;
 //	}
 
 }
