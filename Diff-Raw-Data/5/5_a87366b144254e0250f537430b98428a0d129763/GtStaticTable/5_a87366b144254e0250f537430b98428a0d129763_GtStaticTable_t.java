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
 import java.util.Iterator;
 
 public class GtStaticTable implements GreenTeaConsts {
 
 	/*field*/public final static GtMap               SourceMap = new GtMap();
 	/*field*/public final static ArrayList<String>   SourceList = new ArrayList<String>();
 
 	/*field*/public final static GtMap			    ClassNameMap = new GtMap();
 	/*field*/public final static ArrayList<GtType>  TypePools = new ArrayList<GtType>();
 	/*field*/public final static ArrayList<GtFunc>  FuncPools = new ArrayList<GtFunc>();
 
 	/*field*/public final static GtType		TopType = new GtType(0, "Top", null, GreenTeaTopObject.class);
 	/*field*/public final static GtType		VoidType = new GtType(NativeType, "void", null, void.class);
 	/*field*/public final static GtType		BooleanType = new GtType(NativeType|UnboxType, "boolean", false, boolean.class);
 	/*field*/public final static GtType		IntType = new GtType(NativeType|UnboxType, "int", 0L, long.class);
 	/*field*/public final static GtType     FloatType = new GtType(NativeType|UnboxType, "float", 0.0, double.class);
 	/*field*/public final static GtType		StringType = new GtType(NativeType, "String", null, String.class);
 	/*field*/public final static GtType		AnyType = new GtType(DynamicType, "any", null, Object.class);
 	/*field*/public final static GtType		ArrayType = GtStaticTable.TopType.CreateSubType(GenericVariable, "Array", null, GreenTeaArray.class);
 	/*field*/public final static GtType		FuncType  = GtStaticTable.TopType.CreateSubType(GenericVariable, "Func", null, GtFunc.class);
 
 	/*field*/public final static GtType		EnumBaseType = GtStaticTable.TopType.CreateSubType(EnumType, "enum", null, GreenTeaEnum.class);
 //	/*field*/public final static GtType		StructType;
 	/*field*/public final static GtType		VarType = new GtType(0, "var", null, null);
 	/*field*/public final static GtType		TypeType = GtStaticTable.TopType.CreateSubType(0, "Type", null, GtType.class);
 	/*field*/public final static GtType     IteratorType = new GtType(GenericVariable, "Iterator", null, Iterator.class);
 	
 	public final static long GetFileLine(String FileName, int Line) {
		/*local*/Object IdOrNull = GtStaticTable.SourceMap.GetOrNull(FileName);
		/*local*/Integer Id = IdOrNull == null ? -1 : (Integer)IdOrNull;
		if(IdOrNull == null) {
 			GtStaticTable.SourceList.add(FileName);
 			Id = GtStaticTable.SourceList.size();
 			GtStaticTable.SourceMap.put(FileName, Id);
 		}
 		return LibGreenTea.JoinIntId(Id, Line);
 	}
 
 
 	public final static String GetSourceFileName(long FileLine) {
 		/*local*/int FileId = LibGreenTea.UpperId(FileLine);
 		return (FileId == 0) ? null : GtStaticTable.SourceList.get(FileId - 1);
 	}
 
 	public final static int GetFileLineNumber(long FileLine) {
 		return LibGreenTea.LowerId(FileLine);
 	}
 	
 	public final static String FormatFileLineNumber(long FileLine) {
 		/*local*/int FileId = LibGreenTea.UpperId(FileLine);
 		/*local*/int Line = LibGreenTea.LowerId(FileLine);
 		/*local*/String FileName = (FileId == 0) ? "eval" : GtStaticTable.SourceList.get(FileId - 1);
 		return "(" + FileName + ":" + Line + ")";
 	}
 
 	private static boolean IsInit = false;
 	
 	public final static void InitParserContext(GtParserContext Context) {
 		if(!IsInit) {
 //ifdef JAVA
 			ArrayType.TypeParams = new GtType[1];
 			ArrayType.TypeParams[0] = GtStaticTable.VarType;
 			FuncType.TypeParams = new GtType[1];
 			FuncType.TypeParams[0] = GtStaticTable.VarType;  // for PolyFunc
 			IteratorType.TypeParams = new GtType[1];
 			IteratorType.TypeParams[0] = GtStaticTable.VarType;
 			
 			GtStaticTable.SetNativeTypeName("org.GreenTeaScript.GreenTeaTopObject", GtStaticTable.TopType);
 			GtStaticTable.SetNativeTypeName("void",    GtStaticTable.VoidType);
 			GtStaticTable.SetNativeTypeName("java.lang.Object",  GtStaticTable.AnyType);
 			GtStaticTable.SetNativeTypeName("boolean", GtStaticTable.BooleanType);
 			GtStaticTable.SetNativeTypeName("java.lang.Boolean", GtStaticTable.BooleanType);
 			GtStaticTable.SetNativeTypeName("long",    GtStaticTable.IntType);
 			GtStaticTable.SetNativeTypeName("java.lang.Long",    GtStaticTable.IntType);
 			GtStaticTable.SetNativeTypeName("java.lang.String",  GtStaticTable.StringType);
 			GtStaticTable.SetNativeTypeName("org.GreenTeaScript.GtType", GtStaticTable.TypeType);
 			GtStaticTable.SetNativeTypeName("org.GreenTeaScript.GreenTeaEnum", GtStaticTable.EnumBaseType);
 			GtStaticTable.SetNativeTypeName("org.GreenTeaScript.GreenTeaArray", GtStaticTable.ArrayType);
 			GtStaticTable.SetNativeTypeName("org.GreenTeaScript.Konoha.GreenTeaIntArray", GtStaticTable.GetGenericType1(GtStaticTable.ArrayType, GtStaticTable.IntType, true));
 			GtStaticTable.SetNativeTypeName("double",    GtStaticTable.FloatType);
 			GtStaticTable.SetNativeTypeName("java.lang.Double",  GtStaticTable.FloatType);
 			GtStaticTable.SetNativeTypeName("java.util.Iterator",  GtStaticTable.IteratorType);
 //endif VAJA
 			IsInit = true;
 		}
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.TopType,  null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.VoidType,  null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.BooleanType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.IntType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.FloatType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.StringType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.VarType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.AnyType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.TypeType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.ArrayType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.FuncType, null);
 		Context.RootNameSpace.AppendTypeName(GtStaticTable.IteratorType, null);
 	}
 
 	public static int IssueTypeId(GtType Type) {
 		/*local*/int TypeId = GtStaticTable.TypePools.size();
 		GtStaticTable.TypePools.add(Type);
 		return TypeId;
 	}
 
 	public final static GtType GetTypeById(int TypeId) {
 		return GtStaticTable.TypePools.get(TypeId);
 	}
 
 	public final static void SetNativeTypeName(String Name, GtType Type) {
 		GtStaticTable.ClassNameMap.put(Name, Type);
 		LibGreenTea.VerboseLog(VerboseSymbol, "global type name: " + Name + ", " + Type);
 	}
 
 	public final static GtType GetNativeTypeOfValue(Object Value) {
 		return LibNative.GetNativeType(LibNative.GetClassOfValue(Value));
 	}
 	
 	public final static GtType GuessType (Object Value) {
 		if(Value instanceof GtFunc) {
 			return ((/*cast*/GtFunc)Value).GetFuncType();
 		}
 		else if(Value instanceof GtPolyFunc) {
 			return GtStaticTable.FuncType;
 		}
 		else if(Value instanceof GreenTeaObject) {
 			// FIXME In typescript, we cannot use GreenTeaObject
 			return ((/*cast*/GreenTeaObject)Value).GetGreenType();
 		}
 		else {
 			return GtStaticTable.GetNativeTypeOfValue(Value);
 		}
 	}
 
 	public final static GtType GetGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList, boolean IsCreation) {
 		LibGreenTea.Assert(BaseType.IsGenericType());
 		/*local*/String MangleName = GreenTeaUtils.MangleGenericType(BaseType, BaseIdx, TypeList);
 		/*local*/GtType GenericType = (/*cast*/GtType)GtStaticTable.ClassNameMap.GetOrNull(MangleName);
 		if(GenericType == null && IsCreation) {
 			/*local*/int i = BaseIdx;
 			/*local*/String s = BaseType.ShortName + "<";
 			while(i < LibGreenTea.ListSize(TypeList)) {
 				s = s + TypeList.get(i).ShortName;
 				i += 1;
 				if(i == LibGreenTea.ListSize(TypeList)) {
 					s = s + ">";
 				}
 				else {
 					s = s + ",";
 				}
 			}
 			GenericType = BaseType.CreateGenericType(BaseIdx, TypeList, s);
 			GtStaticTable.SetNativeTypeName(MangleName, GenericType);
 		}
 		return GenericType;
 	}
 
 	public final static GtType GetGenericType1(GtType BaseType, GtType ParamType, boolean IsCreation) {
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(ParamType);
 		return GtStaticTable.GetGenericType(BaseType, 0, TypeList, IsCreation);
 	}
 	
 //	private final String SubtypeKey(GtType FromType, GtType ToType) {
 //		return FromType.GetUniqueName() + "<" + ToType.GetUniqueName();
 //	}
 
 	public final static boolean CheckSubType(GtType SubType, GtType SuperType) {
 		// TODO: Structual Typing database
 		return false;
 	}
 
 	public final static GtFunc GetFuncById(int FuncId) {
 		return FuncPools.get(FuncId);
 	}
 
 	public static GtFunc GetConverterFunc(GtType ValueType, GtType CastType, boolean SearchRecursive) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 	// ConstPool
 	private static final ArrayList<Object> ConstPoolList = new ArrayList<Object>();
 
 	public static int AddConstPool(Object o) {
 		/*local*/int PooledId = ConstPoolList.indexOf(o);
 		if(PooledId != -1) {
 			return PooledId;
 		}
 		else {
 			ConstPoolList.add(o);
 			return ConstPoolList.size() - 1;
 		}
 	}
 
 	public static Object GetConstPool(int PooledId) {
 		return ConstPoolList.get(PooledId);
 	}
 
 
 }
