 package com.raxacoricofallapatorius.service;
 
 public enum TokenType {
 	TK_ID, // identifier token
 	TK_STRING, // string token
 	TK_INT, // int num token
 	TK_FLOAT, // float num token
 
 	// -------Keywords--------
 
 	TK_K_INT {
 		@Override
 		public String getName() {
 			return "int";
 		}
 	}, // int
 	TK_K_FLOAT {
 		@Override
 		public String getName() {
 			return "float";
 		}
 	}, // float
 	TK_K_STR {
 		@Override
 		public String getName() {
 			return "str";
 		}
 	}, // str
 	TK_K_VOID {
 		@Override
 		public String getName() {
 			return "void";
 		}
 	}, // void
 	TK_K_BOOL {
 		@Override
 		public String getName() {
 			return "bool";
 		}
 	}, // bool
 	TK_K_TRUE {
 		@Override
 		public String getName() {
 			return "true";
 		}
 	}, // true
 	TK_K_FALSE {
 		@Override
 		public String getName() {
 			return "false";
 		}
 	}, // false
 	TK_K_FUNC {
 		@Override
 		public String getName() {
 			return "func";
 		}
 	}, // func
 	TK_K_FOR {
 		@Override
 		public String getName() {
 			return "for";
 		}
 	}, // for
 	TK_K_WHILE {
 		@Override
 		public String getName() {
			return "white";
 		}
 	}, // while
 	TK_K_IF {
 		@Override
 		public String getName() {
 			return "if";
 		}
 	}, // if
 	TK_K_ELSE {
 		@Override
 		public String getName() {
 			return "else";
 		}
 	}, // else
 	TK_K_CONTINUE {
 		@Override
 		public String getName() {
 			return "continue";
 		}
 	}, // continue
 	TK_K_BREAK {
 		@Override
 		public String getName() {
 			return "break";
 		}
 	}, // break
 	TK_K_CONST {
 		@Override
 		public String getName() {
 			return "const";
 		}
 	}, // const
 	TK_K_DEF {
 		@Override
 		public String getName() {
 			return "def";
 		}
 	}, // def
 	TK_K_DIV {
 		@Override
 		public String getName() {
 			return "div";
 		}
 	}, // div
 
 	// --------Separators-----------
 
 	TK_S_QUOT {
 		@Override
 		public String getName() {
 			return "\"";
 		}
 	}, // "
 	TK_S_LEFT_PARENT {
 		public String getName() {
 			return "(";
 		}
 	}, // (
 		// }
 	TK_S_RIGHT_PARENT {
 		@Override
 		public String getName() {
 			return ")";
 		}
 	}, // )
 	TK_S_LEFT_BRACE {
 		@Override
 		public String getName() {
 			return "{";
 		}
 	}, // {
 	TK_S_RIGHT_BRACE {
 		@Override
 		public String getName() {
 			return "}";
 		}
 	}, // }
 	TK_S_AND {
 		@Override
 		public String getName() {
 			return "&";
 		}
 	}, // &
 	TK_S_OR {
 		@Override
 		public String getName() {
 			return "|";
 		}
 	}, // |
 	TK_S_MULTI {
 		@Override
 		public String getName() {
 			return "*";
 		}
 	}, // *
 	TK_S_DIVIDE {
 		@Override
 		public String getName() {
 			return "/";
 		}
 	}, // /
 	TK_S_ADD {
 		@Override
 		public String getName() {
 			return "+";
 		}
 	}, // +
 	TK_S_SUBTRACT {
 		@Override
 		public String getName() {
 			return "-";
 		}
 	}, // -
 	TK_S_GREATER {
 		@Override
 		public String getName() {
 			return ">";
 		}
 	}, // >
 	TK_S_LESS {
 		@Override
 		public String getName() {
 			return "<";
 		}
 	}, // <
 
 	// --------Separator chx2--------
 
 	TK_S_NOT {
 		@Override
 		public String getName() {
 			return "!=";
 		}
 	}, // !=
 	TK_S_EQUAL {
 		@Override
 		public String getName() {
 			return "==";
 		}
 	}, // ==
 	TK_S_EQUAL_OR_GREATER {
 		@Override
 		public String getName() {
 			return ">=";
 		}
 	}, // >=
 	TK_S_EQUAL_OR_LESS {
 		@Override
 		public String getName() {
 			return "<=";
 		}
 	}, // <=
 
 	TK_S_INIT {
 		@Override
 		public String getName() {
 			return "=";
 		}
 	}, // =
 
 	TK_S_COMMA {
 		@Override
 		public String getName() {
 			return ",";
 		}
 	}, // ,
 	TK_EOP { // end of program token
 
 		@Override
 		public String getName() {
 			return "";
 		}
 
 	};
 
 	public String getName() {
 		return null;
 	}
 }
