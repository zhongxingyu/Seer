 /*
  * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 only, as
  * published by the Free Software Foundation.  
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 2 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 2 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
  * CA 95054 USA or visit www.sun.com if you need additional information or
  * have any questions.
  */
 
 package com.sun.tools.javafx.util;
 
 public class MsgSym {
     public static final String MESSAGE_JAVAFX_MSG_BUG = "javafx.msg.bug"; 
     public static final String MESSAGE_JAVAFX_OPT_ARG_NAME = "javafx.opt.arg.name"; 
     public static final String MESSAGE_JAVAFX_OPT_PLATFORM = "javafx.opt.platform"; 
 
     public static final String MESSAGEPREFIX_COMPILER_ERR = "compiler.err."; 
     public static final String MESSAGEPREFIX_COMPILER_WARN = "compiler.warn."; 
     public static final String MESSAGEPREFIX_COMPILER_MISC = "compiler.misc."; 
     public static final String MESSAGEPREFIX_JAVAC = "javac."; 
     public static final String MESSAGEPREFIX_VERBOSE = "verbose."; 
     public static final String MESSAGEPREFIX_COUNT = "count."; 
     public static final String MESSAGEPREFIX_ERROR = "error";
     public static final String MESSAGEPREFIX_WARN = "warn";
     public static final String MESSAGEPREFIX_DEPRECATED = "deprecated";
     public static final String MESSAGEPREFIX_UNCHECKED = "unchecked";
 
     public static final String MESSAGESUFFIX_PLURAL = ".plural"; 
     
     // javac messages
     public static final String MESSAGE_VERSION = "version";
     public static final String MESSAGE_FULLVERSION = "fullVersion";
     public static final String MESSAGE_MSG_USAGE_HEADER = "msg.usage.header";
     public static final String MESSAGE_MSG_USAGE_NONSTANDARD_FOOTER = "msg.usage.nonstandard.footer";
     public static final String MESSAGE_MSG_USAGE = "msg.usage";
     public static final String MESSAGE_WARN_TARGET_DEFAULT_SOURCE_CONFLICT = "warn.target.default.source.conflict";
     public static final String MESSAGE_WARN_SOURCE_TARGET_CONFLICT = "warn.source.target.conflict";
     public static final String MESSAGE_ERR_FILE_NOT_FOUND = "err.file.not.found";
     public static final String MESSAGE_MSG_IO = "msg.io";
     public static final String MESSAGE_MSG_RESOURCE = "msg.resource";
     public static final String MESSAGE_OPT_G = "opt.g";
     public static final String MESSAGE_OPT_G_NONE = "opt.g.none";
     public static final String MESSAGE_OPT_G_LINES_VARS_SOURCE = "opt.g.lines.vars.source";
     public static final String MESSAGE_OPT_XLINT = "opt.Xlint";
     public static final String MESSAGE_OPT_XLINT_SUBOPTLIST = "opt.Xlint.suboptlist";
     public static final String MESSAGE_OPT_NOWARN = "opt.nowarn";
     public static final String MESSAGE_OPT_VERBOSE = "opt.verbose";
     public static final String MESSAGE_OPT_DEPRECATION = "opt.deprecation";
     public static final String MESSAGE_OPT_ARG_PATH = "opt.arg.path";
     public static final String MESSAGE_OPT_CLASSPATH = "javafx.opt.classpath";
     public static final String MESSAGE_OPT_SOURCEPATH = "opt.sourcepath";
     public static final String MESSAGE_OPT_BOOTCLASSPATH = "opt.bootclasspath";
     public static final String MESSAGE_OPT_XBOOTCLASSPATH_P = "opt.Xbootclasspath.p";
     public static final String MESSAGE_OPT_XBOOTCLASSPATH_A = "opt.Xbootclasspath.a";
     public static final String MESSAGE_OPT_ARG_DIRS = "opt.arg.dirs";
     public static final String MESSAGE_OPT_EXTDIRS = "opt.extdirs";
     public static final String MESSAGE_OPT_ENDORSEDDIRS = "opt.endorseddirs";
     public static final String MESSAGE_OPT_PROC_NONE_ONLY = "opt.proc.none.only";
     public static final String MESSAGE_OPT_ARG_CLASS_LIST = "opt.arg.class.list";
     public static final String MESSAGE_OPT_PROCESSOR = "opt.processor";
     public static final String MESSAGE_OPT_PROCESSORPATH = "opt.processorpath";
     public static final String MESSAGE_OPT_ARG_DIRECTORY = "opt.arg.directory";
     public static final String MESSAGE_OPT_D = "opt.d";
     public static final String MESSAGE_OPT_SOURCE_DEST = "opt.sourceDest";
     public static final String MESSAGE_OPT_ARG_ENCODING = "opt.arg.encoding";
     public static final String MESSAGE_OPT_ENCODING = "opt.encoding";
     public static final String MESSAGE_OPT_ARG_RELEASE = "opt.arg.release";
     public static final String MESSAGE_OPT_SOURCE = "opt.source";
     public static final String MESSAGE_OPT_TARGET = "opt.target";
     public static final String MESSAGE_OPT_VERSION = "opt.version";
     public static final String MESSAGE_OPT_HELP = "opt.help";
     public static final String MESSAGE_OPT_X = "opt.X";
     public static final String MESSAGE_OPT_ARG_FLAG = "opt.arg.flag";
     public static final String MESSAGE_OPT_J = "opt.J";
     public static final String MESSAGE_OPT_ARG_NUMBER = "opt.arg.number";
     public static final String MESSAGE_OPT_MAXERRS = "opt.maxerrs";
     public static final String MESSAGE_OPT_MAXWARNS = "opt.maxwarns";
     public static final String MESSAGE_OPT_ARG_FILE = "opt.arg.file";
     public static final String MESSAGE_OPT_XSTDOUT = "opt.Xstdout";
     public static final String MESSAGE_OPT_PRINT = "opt.print";
     public static final String MESSAGE_OPT_PRINT_ROUNDS = "opt.printRounds";
     public static final String MESSAGE_OPT_PRINT_PROCESSOR_INFO = "opt.printProcessorInfo";
     public static final String MESSAGE_OPT_PREFER = "opt.prefer";
     public static final String MESSAGE_ERR_INVALID_SOURCE = "err.invalid.source";
     public static final String MESSAGE_ERR_INVALID_TARGET = "err.invalid.target";
     public static final String MESSAGE_ERR_ERROR_WRITING_FILE = "err.error.writing.file";
     public static final String MESSAGE_ERR_FILE_NOT_FILE = "err.file.not.file";
     public static final String MESSAGE_ERR_INVALID_FLAG = "err.invalid.flag";
     public static final String MESSAGE_ERR_REQ_ARG = "err.req.arg";
     public static final String MESSAGE_ERR_DIR_NOT_FOUND = "err.dir.not.found";
     public static final String MESSAGE_ERR_FILE_NOT_DIRECTORY = "err.file.not.directory";
     public static final String MESSAGE_ERR_NO_SOURCE_FILES = "err.no.source.files";
 
     // compiler messages
     public static final String MESSAGE_NEITHER_CONDITIONAL_SUBTYPE = "neither.conditional.subtype"; 
     public static final String MESSAGE_PKG_ANNOTATIONS_SB_IN_PACKAGE_INFO_JAVA = "pkg.annotations.sb.in.package-info.java";
     public static final String MESSAGE_PKG_INFO_ALREADY_SEEN = "pkg-info.already.seen";
     public static final String MESSAGE_DUPLICATE_CLASS = "duplicate.class";
     public static final String MESSAGE_VERSION_RESOURCE_MISSING = "version.resource.missing";
     public static final String MESSAGE_VERSION_UNKNOWN = "version.unknown";
     public static final String MESSAGE_CANNOT_ACCESS = "cant.access";
     public static final String MESSAGE_ERROR_READING_FILE = "error.reading.file";
     public static final String MESSAGE_PARSING_STARTED = "parsing.started";
     public static final String MESSAGE_PARSING_DONE = "parsing.done";
     public static final String MESSAGE_TOTAL = "total";
     public static final String MESSAGE_CHECKING_ATTRIBUTION = "checking.attribution";
     public static final String MESSAGE_FILE_DOES_NOT_CONTAIN_PACKAGE = "file.does.not.contain.package";
     public static final String MESSAGE_FILE_DOES_NOT_CONTAIN_CLASS = "file.doesnt.contain.class";
     public static final String MESSAGE_UNEXPECTED_TYPE = "unexpected.type";
     public static final String MESSAGE_ANNOTATION_VALUE_MUST_BE_NAME_VALUE = "annotation.value.must.be.name.value";
     public static final String MESSAGE_NO_ANNOTATION_MEMBER = "no.annotation.member";
     public static final String MESSAGE_ATTRIBUTE_VALUE_MUST_BE_CONSTANT = "attribute.value.must.be.constant";
     public static final String MESSAGE_ANNOTATION_VALUE_MUST_BE_CLASS_LITERAL = "annotation.value.must.be.class.literal";
     public static final String MESSAGE_ANNOTATION_VALUE_MUST_BE_ANNOTATION = "annotation.value.must.be.annotation";
     public static final String MESSAGE_NEW_NOT_ALLOWED_IN_ANNOTATION = "new.not.allowed.in.annotation";
     public static final String MESSAGE_ENUM_ANNOTATION_MUST_BE_ENUM_CONSTANT = "enum.annotation.must.be.enum.constant";
     public static final String MESSAGE_ANNOTATION_VALUE_NOT_ALLOWABLE_TYPE = "annotation.value.not.allowable.type";
     public static final String MESSAGE_TYPE_REQ_EXACT = "type.req.exact";
     public static final String MESSAGE_PROB_FOUND_REQ = "prob.found.req";
     public static final String MESSAGE_PROB_FOUND_REQ_1 = "prob.found.req.1";
     public static final String MESSAGE_TYPE_FOUND_REQ = "type.found.req";
     public static final String MESSAGE_CANNOT_REF_BEFORE_CTOR_CALLED = "cant.ref.before.ctor.called";
     public static final String MESSAGE_ALREADY_DEFINED = "already.defined";
     public static final String MESSAGE_ARRAY_AND_VARARGS = "array.and.varargs";
     public static final String MESSAGE_ASSIGNMENT_FROM_SUPER_BOUND = "assignment.from.super-bound";
     public static final String MESSAGE_ASSIGNMENT_TO_EXTENDS_BOUND = "assignment.to.extends-bound";
     public static final String MESSAGE_UNDETERMINDED_TYPE = "undetermined.type";
     public static final String MESSAGE_UNDETERMINDED_TYPE_1 = "undetermined.type.1";
     public static final String MESSAGE_INCOMPATIBLE_TYPES = "incompatible.types";
     public static final String MESSAGE_INCOMPATIBLE_TYPES_1 = "incompatible.types.1";
     public static final String MESSAGE_NOT_WITHIN_BOUNDS = "not.within.bounds";
     public static final String MESSAGE_VOID_NOT_ALLOWED_HERE = "void.not.allowed.here";
     public static final String MESSAGE_ILLEGAL_GENERIC_TYPE_FOR_INSTOF = "illegal.generic.type.for.instof";
     public static final String MESSAGE_ILLEGAL_COMBINATION_OF_MODIFIERS = "illegal.combination.of.modifiers";
     public static final String MESSAGE_ENUMS_MUST_BE_STATIC = "enums.must.be.static";
     public static final String MESSAGE_INTF_NOT_ALLOWED_HERE = "intf.not.allowed.here";
     public static final String MESSAGE_MOD_NOT_ALLOWED_HERE = "mod.not.allowed.here";
     public static final String MESSAGE_IMPROPERLY_FORMED_TYPE_INNER_RAW_PARAM = "improperly.formed.type.inner.raw.param";
     public static final String MESSAGE_IMPROPERLY_FORMED_TYPE_PARAM_MISSING = "improperly.formed.type.param.missing";
     public static final String MESSAGE_CANNOT_SELECT_STATIC_CLASS_FROM_PARAM_TYPE = "cant.select.static.class.from.param.type";
     public static final String MESSAGE_OVERRIDE_STATIC = "override.static";
     public static final String MESSAGE_OVERRIDE_METH = "override.meth";
     public static final String MESSAGE_OVERRIDE_METH_DOES_NOT_THROW = "override.meth.doesnt.throw";
     public static final String MESSAGE_OVERRIDE_VARARGS_MISSING = "override.varargs.missing";
     public static final String MESSAGE_OVERRIDE_VARARGS_EXTRA = "override.varargs.extra";
     public static final String MESSAGE_OVERRIDE_BRIDGE = "override.bridge";
     public static final String MESSAGE_CONCRETE_INHERITANCE_CONFLICT = "concrete.inheritance.conflict";
     public static final String MESSAGE_TYPES_INCOMPATIBLE_DIFF_RET = "types.incompatible.diff.ret";
     public static final String MESSAGE_ENUM_NO_FINALIZE = "enum.no.finalize";
     public static final String MESSAGE_DOES_NOT_OVERRIDE_ABSTRACT = "does.not.override.abstract";
     public static final String MESSAGE_CYCLIC_INHERITANCE = "cyclic.inheritance";
     public static final String MESSAGE_CANNOT_INHERIT_DIFF_ARG = "cant.inherit.diff.arg";
     public static final String MESSAGE_REPEATED_INTERFACE = "repeated.interface";
     public static final String MESSAGE_INVALID_ANNOTATION_MEMBER_TYPE = "invalid.annotation.member.type";
     public static final String MESSAGE_INTF_ANNOTATION_MEMBER_CLASH = "intf.annotation.member.clash";
     public static final String MESSAGE_ANNOTATION_TYPE_NOT_APPLICABLE = "annotation.type.not.applicable";
     public static final String MESSAGE_METHOD_DOES_NOT_OVERRIDE_SUPERCLASS = "method.does.not.override.superclass";
     public static final String MESSAGE_DUPLICATE_ANNOTATION_MEMBER_VALUE = "duplicate.annotation.member.value";
     public static final String MESSAGE_ANNOTATION_MISSING_DEFAULT_VALUE = "annotation.missing.default.value";
     public static final String MESSAGE_REPEATED_ANNOTATION_TARGET = "repeated.annotation.target";
     public static final String MESSAGE_MISSING_DEPRECATED_ANNOTATION = "missing.deprecated.annotation";
     public static final String MESSAGE_CYCLIC_ANNOTATION_ELEMENT = "cyclic.annotation.element";
     public static final String MESSAGE_RECURSIVE_CTOR_INVOCATION = "recursive.ctor.invocation";
     public static final String MESSAGE_OPERATOR_CANNOT_BE_APPLIED = "operator.cant.be.applied";
     public static final String MESSAGE_DIV_ZERO = "div.zero";
     public static final String MESSAGE_EMPTY_IF = "empty.if";
     public static final String MESSAGE_ALREADY_DEFINED_STATIC_SINGLE_IMPORT = "already.defined.static.single.import";
     public static final String MESSAGE_ALREADY_DEFINED_SINGLE_IMPORT = "already.defined.single.import";
     public static final String MESSAGE_ALREADY_DEFINED_THIS_UNIT = "already.defined.this.unit";
     public static final String MESSAGE_IMPORT_REQUIRES_CANONICAL = "import.requires.canonical";
     public static final String MESSAGE_UNCHECKED_CAST_TO_TYPE = "unchecked.cast.to.type";
     public static final String MESSAGE_UNCHECKED_ASSIGN = "unchecked.assign";
     public static final String MESSAGE_HAS_BEEN_DEPRECATED = "has.been.deprecated";
     public static final String MESSAGE_ABSTRACT_CANNOT_BE_ACCESSED_DIRECTLY = "abstract.cant.be.accessed.directly";
     public static final String MESSAGE_NOT_ENCL_CLASS = "not.encl.class";
     public static final String MESSAGE_ENCL_CLASS_REQUIRED = "encl.class.required";
     public static final String MESSAGE_PROC_MESSAGER = "proc.messager";
     public static final String MESSAGE_NOT_DEF_ACCESS_CLASS_INTF_CANNOT_ACCESS = "not.def.access.class.intf.cant.access";
     public static final String MESSAGE_REPORT_ACCESS = "report.access";
     public static final String MESSAGE_NOT_DEF_PUBLIC_CANNOT_ACCESS = "not.def.public.cant.access";
     public static final String MESSAGE_NON_STATIC_CANNOT_BE_REF = "non-static.cant.be.ref";
     public static final String MESSAGE_REF_AMBIGUOUS = "ref.ambiguous";
     public static final String MESSAGE_CANNOT_APPLY_SYMBOL = "cant.apply.symbol";
     public static final String MESSAGE_DOES_NOT_EXIST = "doesnt.exist";
     public static final String MESSAGE_CANNOT_RESOLVE_LOCATION = "cant.resolve.location";
     public static final String MESSAGE_CANNOT_RESOLVE = "cant.resolve";
     public static final String MESSAGE_ILLEGAL_NONASCII_DIGIT = "illegal.nonascii.digit";
     public static final String MESSAGE_ILLEGAL_UNICODE_ESC = "illegal.unicode.esc";
     public static final String MESSAGE_ILLEGAL_ESC_CHAR = "illegal.esc.char";
     public static final String MESSAGE_STATIC_IMP_ONLY_CLASSES_AND_INTERFACES = "static.imp.only.classes.and.interfaces";
     public static final String MESSAGE_PKG_CLASHES_WITH_CLASS_OF_SAME_NAME = "pkg.clashes.with.class.of.same.name";
     public static final String MESSAGE_ALREADY_ANNOTATED = "already.annotated";
     public static final String MESSAGE_DUPLICATE_ANNOTATION = "duplicate.annotation";
     public static final String MESSAGE_CLASH_WITH_PKG_OF_SAME_NAME = "clash.with.pkg.of.same.name";
     public static final String MESSAGE_ILLEGAL_FORWARD_REF = "illegal.forward.ref";
     public static final String MESSAGE_INTF_EXPECTED_HERE = "intf.expected.here";
     public static final String MESSAGE_NO_INTF_EXPECTED_HERE = "no.intf.expected.here";
     public static final String MESSAGE_CANNOT_INHERIT_FROM_FINAL = "cant.inherit.from.final";
     public static final String MESSAGE_TYPE_VAR_CANNOT_BE_DEREF = "type.var.cant.be.deref";
     public static final String MESSAGE_CANNOT_DEREF = "cant.deref";
     public static final String MESSAGE_ILLEGAL_START_OF_TYPE = "illegal.start.of.type";
     public static final String MESSAGE_FOREACH_NOT_APPLICABLE_TO_TYPE = "foreach.not.applicable.to.type";
     public static final String MESSAGE_UNREACHABLE_STMT = "unreachable.stmt";
     public static final String MESSAGE_ABSTRACT_CANNOT_BE_INSTANTIATED = "abstract.cant.be.instantiated";
     public static final String MESSAGE_ANON_CLASS_IMPL_INTF_NO_ARGS = "anon.class.impl.intf.no.args";
     public static final String MESSAGE_INTF_ANNOTATION_MEMBERS_CANNOT_HAVE_PARAMS = "intf.annotation.members.cant.have.params";
     public static final String MESSAGE_MISSING_METH_BODY_OR_DECL_ABSTRACT = "missing.meth.body.or.decl.abstract";
     public static final String MESSAGE_INTF_METH_CANNOT_HAVE_BODY = "intf.meth.cant.have.body";
     public static final String MESSAGE_ABSTRACT_METH_CANNOT_HAVE_BODY = "abstract.meth.cant.have.body";
     public static final String MESSAGE_NATIVE_METH_CANNOT_HAVE_BODY = "native.meth.cant.have.body";
     public static final String MESSAGE_NOT_LOOP_LABEL = "not.loop.label";
     public static final String MESSAGE_UNDEF_LABEL = "undef.label";
     public static final String MESSAGE_CONT_OUTSIDE_LOOP = "cont.outside.loop";
     public static final String MESSAGE_BREAK_OUTSIDE_SWITCH_LOOP = "break.outside.switch.loop";
     public static final String MESSAGE_RETURN_OUTSIDE_METH = "ret.outside.meth";
     public static final String MESSAGE_CANNOT_RET_VAL_FROM_METH_DECL_VOID = "cant.ret.val.from.meth.decl.void";
     public static final String MESSAGE_MISSING_RET_VAL = "missing.ret.val";
     public static final String MESSAGE_INCOMPARABLE_TYPES = "incomparable.types";
     public static final String MESSAGE_WRONG_NUMBER_TYPE_ARGS = "wrong.number.type.args";
     public static final String MESSAGE_TYPE_DOES_NOT_TAKE_PARAMS = "type.doesnt.take.params";
     public static final String MESSAGE_ANNOTATION_NOT_VALID_FOR_TYPE = "annotation.not.valid.for.type";
     public static final String MESSAGE_ENUM_NO_SUBCLASSING = "enum.no.subclassing";
     public static final String MESSAGE_ENUM_TYPES_NOT_EXTENSIBLE = "enum.types.not.extensible";
     public static final String MESSAGE_ARRAY_REQ_BUT_FOUND = "array.req.but.found";
     public static final String MESSAGE_UNCHECKED_ASSIGN_TO_VAR = "unchecked.assign.to.var";
     public static final String MESSAGE_UNCHECKED_CALL_MBR_OF_RAW_TYPE = "unchecked.call.mbr.of.raw.type";
     public static final String MESSAGE_UNCHECKED_METH_INVOCATION_APPLIED = "unchecked.meth.invocation.applied";
     public static final String MESSAGE_UNCHECKED_GENERIC_ARRAY_CREATION = "unchecked.generic.array.creation";
     public static final String MESSAGE_SUN_PROPRIETARY = "sun.proprietary";
     public static final String MESSAGE_CANNOT_ASSIGN_VAL_TO_FINAL_VAR = "cant.assign.val.to.final.var";
     public static final String MESSAGE_ILLEGAL_ENUM_STATIC_REF = "illegal.enum.static.ref";
     public static final String MESSAGE_INTERNAL_ERROR_CANNOT_INSTANTIATE = "internal.error.cant.instantiate";
     public static final String MESSAGE_INEXACT_NON_VARARGS_CALL = "inexact.non-varargs.call";
     public static final String MESSAGE_GENERIC_THROWABLE = "generic.throwable";
     public static final String MESSAGE_CANNOT_EXTEND_INTERFACE_ANNOTATION = "cant.extend.intf.annotation";
     public static final String MESSAGE_INTF_ANNOTATION_CANNOT_HAVE_TYPE_PARAMS = "intf.annotation.cant.have.type.params";
     public static final String MESSAGE_MISSING_SVUID = "missing.SVUID";
     public static final String MESSAGE_IMPROPER_SVUID = "improper.SVUID";
     public static final String MESSAGE_LONG_SVUID = "long.SVUID";
     public static final String MESSAGE_CONSTANT_SVUID = "constant.SVUID";
     public static final String MESSAGE_FATAL_ERR_CANNOT_LOCATE_FIELD = "fatal.err.cant.locate.field";
     public static final String MESSAGE_FATAL_ERR_CANNOT_LOCATE_METH = "fatal.err.cant.locate.meth";
     public static final String MESSAGE_FATAL_ERR_CANNOT_LOCATE_CTOR = "fatal.err.cant.locate.ctor";
     public static final String MESSAGE_POSSIBLE_LOSS_OF_PRECISION = "possible.loss.of.precision";
     public static final String MESSAGE_INCONVERTIBLE_TYPES = "inconvertible.types";
     public static final String MESSAGE_TYPE_REQ_CLASS = "type.req.class";
     public static final String MESSAGE_TYPE_PARAMETER = "type.parameter";
     public static final String MESSAGE_TYPE_REQ_CLASS_ARRAY = "type.req.class.array";
     public static final String MESSAGE_TYPE_REQ_REF = "type.req.ref";
     public static final String MESSAGE_CANNOT_OVERRIDE = "cant.override";
     public static final String MESSAGE_CANNOT_IMPLEMENT = "cant.implement";
     public static final String MESSAGE_CLASHES_WITH = "clashes.with";
     public static final String MESSAGE_UNCHECKED_OVERRIDE = "unchecked.override";
     public static final String MESSAGE_UNCHECKED_IMPLEMENT = "unchecked.implement";
     public static final String MESSAGE_UNCHECKED_CLASH_WITH = "unchecked.clash.with";
     public static final String MESSAGE_VARARGS_OVERRIDE = "varargs.override";
     public static final String MESSAGE_VARARGS_IMPLEMENT = "varargs.implement";
     public static final String MESSAGE_VARARGS_CLASH_WITH = "varargs.clash.with";
     public static final String MESSAGE_OVERRIDE_INCOMPATIBLE_RET = "override.incompatible.ret";
     public static final String MESSAGE_OVERRIDE_UNCHECKED_RET = "override.unchecked.ret";
     public static final String MESSAGE_FATAL_ERR_NO_JAVA_LANG = "fatal.err.no.java.lang";
     public static final String MESSAGE_NOT_ANNOTATION_TYPE = "not.annotation.type";
     public static final String MESSAGE_TOO_MANY_PARAMETERS = "javafx.too.many.parameters";
     
     // kindname
     public static final String KINDNAME = "kindname";
     public static final String KINDNAME_KEY_VARIABLE = ".variable";
     public static final String KINDNAME_KEY_VALUE = ".value";
     public static final String KINDNAME_KEY_METHOD = ".method";
     public static final String KINDNAME_KEY_CLASS = ".class";
     public static final String KINDNAME_KEY_PACKAGE = ".package";
     public static final String KINDNAME_PACKAGE = "kindname.package";
     public static final String KINDNAME_CLASS = "kindname.class";
     public static final String KINDNAME_VARIABLE = "kindname.variable";
     public static final String KINDNAME_VALUE = "kindname.value";
     public static final String KINDNAME_METHOD = "kindname.method";
     public static final String KINDNAME_TYPE_VARIABLE = "kindname.type.variable";
     public static final String KINDNAME_TYPE_VARIABLE_BOUND = "kindname.type.variable.bound";
     public static final String KINDNAME_ANNOTATION = "kindname.annotation";
     public static final String KINDNAME_INTERFACE = "kindname.interface";
     public static final String KINDNAME_CONSTRUCTOR = "kindname.constructor";
     public static final String KINDNAME_STATIC = "kindname.static";
 
     
     // jfx-compiler messages
     public static final String MESSAGE_JAVAFX_GENERALERROR = "javafx.generalerror"; 
     public static final String MESSAGE_JAVAFX_RANGE_START_INT_OR_NUMBER = "javafx.range.start.int.or.number"; 
     public static final String MESSAGE_JAVAFX_RANGE_END_INT_OR_NUMBER = "javafx.range.end.int.or.number"; 
     public static final String MESSAGE_JAVAFX_RANGE_STEP_INT_OR_NUMBER = "javafx.range.step.int.or.number"; 
     public static final String MESSAGE_JAVAFX_RANGE_LITERAL_EMPTY = "javafx.range.literal.empty"; 
     public static final String MESSAGE_JAVAFX_DUPLICATE_MODULE_MEMBER = "javafx.duplicate.module.member"; 
     public static final String MESSAGE_JAVAFX_INDEXOF_NOT_FOUND = "javafx.indexof.not.found"; 
     public static final String MESSAGE_JAVAFX_CANNOT_INFER_RETURN_TYPE = "javafx.cannot.infer.return.type"; 
     public static final String MESSAGE_JAVAFX_CANNOT_IMPORT_INTEGER_PRIMITIVE_TYPE = "javafx.can.not.import.integer.primitive.type"; 
     public static final String MESSAGE_JAVAFX_CANNOT_IMPORT_BOOLEAN_PRIMITIVE_TYPE = "javafx.can.not.import.boolean.primitive.type"; 
     public static final String MESSAGE_JAVAFX_CANNOT_IMPORT_NUMBER_PRIMITIVE_TYPE = "javafx.can.not.import.number.primitive.type"; 
     public static final String MESSAGE_JAVAFX_CANNOT_IMPORT_STRING_PRIMITIVE_TYPE = "javafx.can.not.import.string.primitive.type"; 
     public static final String MESSAGE_JAVAFX_TYPE_INFER_CYCLE_FUN_DECL = "javafx.type.infer.cycle.fun.decl";
     public static final String MESSAGE_JAVAFX_TYPE_INFER_CYCLE_VAR_DECL = "javafx.type.infer.cycle.var.decl";
     public static final String MESSAGE_JAVAFX_TYPE_INFER_CYCLE_VAR_REF = "javafx.type.infer.cycle.var.ref";
     public static final String MESSAGE_JAVAFX_FUNC_TYPE_INFER_CYCLE = "javafx.function.type.infer.cycle";
     public static final String MESSAGE_JAVAFX_VOID_SEQUENCE_NOT_ALLOWED = "javafx.void.sequence.not.allowed";
     public static final String MESSAGE_JAVAFX_ONLY_ONE_BASE_JAVA_CLASS_ALLOWED = "javafx.only.one.base.java.class.allowed"; 
     public static final String MESSAGE_JAVAFX_CANNOT_OVERRIDE_DEFAULT_INITIALIZER = "javafx.cannot.override.default.initializer"; 
     public static final String MESSAGE_JAVAFX_NOT_ALLOWED_IN_BIND_CONTEXT = "javafx.not.allowed.in.bind.context"; 
     public static final String MESSAGE_JAVAFX_BOUND_OVERRIDE_METH = "javafx.bound.override.meth"; 
     public static final String MESSAGE_JAVAFX_NON_BOUND_OVERRIDE_METH = "javafx.non.bound.override.meth"; 
     public static final String MESSAGE_JAVAFX_BASE_JAVA_CLASS_NON_PAPAR_CTOR = "javafx.base.java.class.non.papar.ctor"; 
     public static final String MESSAGE_JAVAFX_AMBIGUOUS_PARAM_TYPE_FROM_SUPER = "javafx.ambiguous.param.type.from.super"; 
     public static final String MESSAGE_JAVAFX_AMBIGUOUS_RETURN_TYPE_FROM_SUPER = "javafx.ambiguous.return.type.from.super"; 
     public static final String MESSAGE_JAVAFX_NOT_A_FUNC = "javafx.not.a.function";
     public static final String MESSAGE_JAVAFX_CANNOT_APPLY_FUNCTION = "javafx.cant.apply.function";
     public static final String MESSAGE_JAVAFX_MUST_BE_AN_ATTRIBUTE = "javafx.must.be.an.attribute"; 
     public static final String MESSAGE_JAVAFX_LITERAL_OUT_OF_RANGE = "javafx.lexer.literal.range";
    public static final String MESSAGE_JAVAFX_BOUND_FUNCTION_MUST_NOT_BE_VOID = "javafx.bound.function.must.not.be.void";
 
     public static final String MESSAGE_PLUGIN_CANNOT_FIND_PLUGIN = "plugin.cannot.find.plugin"; 
     public static final String MESSAGE_PLUGIN_CANNOT_LOAD_PLUGIN = "plugin.cannot.load.plugin"; 
 }
