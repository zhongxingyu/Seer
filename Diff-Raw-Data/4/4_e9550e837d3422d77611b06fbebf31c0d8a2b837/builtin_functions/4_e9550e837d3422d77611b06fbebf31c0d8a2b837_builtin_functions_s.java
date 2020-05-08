 // Copyright 2013 Lambert Boskamp
 //
 // Author: Lambert Boskamp <lambert@boskamp-consulting.com.nospam>
 //
 // This file is part of IDMacs.
 //
 // IDMacs is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // IDMacs is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with IDMacs.  If not, see <http://www.gnu.org/licenses/>.
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class builtin_functions {
 	// GLOBAL DATA ==================================================
 	private static Map Par = null;
 	/**
 	 * String returned by built-in function uHelp();
 	 */
 	private static String gv_help = null;
 
 	/**
 	 * Name of function currently being processed as string.
 	 */
 	private static String gv_func_name = null;
 
 	/**
 	 * Signature of function currently being processed as string. This is all
 	 * characters between opening and closing paren following a function name in
 	 * uHelp(), e.g.
 	 * 
 	 * "SQLStatement [RowSeparator], [ColumnSeparator]"
 	 * 
 	 * for the function name "uSelect".
 	 */
 	private static String gv_func_signature = null;
 
 	/**
 	 * Multi-line comment of function currently being processed as string. The
 	 * string includes begin and end of comment markers, and line breaks in case
 	 * the comment spans multiple lines. Due to the included begin and end of
 	 * comment markers, no 1:1 example can be included here; they would break
 	 * this comment.
 	 */
 	private static String gv_func_comment = null;
 
 	/**
 	 * java.util.ArrayList of all function names returned by uHelp(); required
 	 * to add them as symbols to js2-additional-externs
 	 */
 	private static ArrayList go_func_names = null;
 
 	/**
 	 * java.util.ArrayList of all argument names of the function currently being
 	 * processed; any argument names that were not plain words (i.e. didn't
 	 * match regex \\w+) have already been transformed into plain words.
 	 */
 	private static ArrayList go_func_arg_names = null;
 
 	/**
 	 * java.util.ArrayList of java.lang.Boolean, with index equality to
 	 * go_func_arg_names. Each Boolean indicates whether the corresponding
 	 * element of go_func_arg_names is an optional argument (true) or a
 	 * mandatory argument (false).
 	 */
 	private static ArrayList go_func_arg_opt = null;
 
 	// GLOBAL CONSTANTS: GENERAL ====================================
 	private final static String GC_SNIPPETS_DIR = "SNIPPETS_DIR";
 	private final static String GC_DICTIONARY_DIR = "DICTIONARY_DIR";
 	private final static String GC_HELP_FILE = "HELP_FILE";
 
 	// GLOBAL CONSTANTS: REGULAR EXPRESSIONS ========================
 	/**
 	 * A constant string containing the regular expression to match exactly one
 	 * function argument in the content returned by uHelp(). Uses additional
 	 * whitespace and comments for improved readability
 	 * 
 	 * ==> requires flag Pattern.COMMENT when compiled
 	 */
 	private static final String GC_REGEX_ONE_ARGUMENT = "" // workaround for
 															// Eclipse pretty
 															// printer
 			+ "(                          # begin: one argument \n"
 			+ "  ( \\w+ ( \\s+\\w+)?   )  # one or two regular argument words \n"
 			+ "| (   < ([^>]+)     >   )  # OR angle-bracketed optional arg \n"
 			+ "| ( \\[ ([^\\]]+) \\]   )  # OR square bracketed optional arg \n"
 			+ ")                          # end: one argument \n"
 			+ "                    \n"; // workaround for Eclipse pretty printer
 
 	/**
 	 * A constant string containing the regular expression to match exactly one
 	 * function, including its name, signature and comment in the content
 	 * returned by uHelp(). Uses additional whitespace and comments for improved
 	 * readability
 	 * 
 	 * ==> requires flag Pattern.COMMENT when compiled
 	 */
 	private static final String GC_REGEX_ONE_FUNCTION = "" // workaround for
 															// Eclipse pretty
 															// printer
 			+ "(\\w+)                    # function name \n"
 			+ "\\s* \\*? \\s*            # workaround for buggy uExtEncode \n"
 			+ "( \\(                     # begin: opt. signature incl. parens \n"
 			+ "(                         # begin: signature  excl. parens \n"
 			+ "(                         # begin: zero or more arguments  \n"
 			+ "\\s* ,? \\s*              # opt. comma surrounded by opt. whitspace \n"
 			+ GC_REGEX_ONE_ARGUMENT
 			// TODO: print() doesn't work yet
 			// +
 			// "\\s* ( \\.{3} )? \\s*     # optional ellipsis for variadic functions \n"
 			+ "\\s* ,? \\s*              # opt. comma surrounded by opt. whitspace \n"
 			+ ")*                        # end: zero or more arguments  \n"
 			+ ")                         # end: signature  excl. parens \n"
 			+ "  \\){1,2} )?             # end: opt. signature incl. parens, workaround uExpandString \n"
 			+ "\\s* ;?                   # opt. whitespace and semicolon \n"
 			+ "( ( \\s* /\\* .*? \\*/ \\s* )+ )? # optional multi-line comment (reluctant quantifier!) \n"
 			+ "                    \n"; // workaround for Eclipse pretty printer
 
 	private static void idmacs_trace(String m) {
 		System.err.println(m);
 	}
 
 	/**
 	 * Reads the whole content of file idmacs_uhelp.txt from the current working
 	 * directory into the global string variable gv_help.
 	 * 
 	 * Preconditions: File idmacs_uhelp.txt exists in current working directory
 	 * and contains the string returned by uHelp().
 	 * 
 	 * @throws Exception
 	 */
 	private static void idmacs_builtins_open_datasource() throws Exception {
 		List lo_help = new ArrayList();
 		StringBuffer lo_help_sb = new StringBuffer();
 		BufferedReader lo_help_reader = new BufferedReader(new FileReader(
 				"idmacs_uhelp.txt"));
 
 		String lv_line = null;
 
 		do {
 			lv_line = lo_help_reader.readLine();
 			if (lv_line == null) {
 				break; // ======================================= EXIT
 			}
 			lo_help.add(lv_line);
 			lo_help_sb.append(lv_line);
 		} while (true);
 
 		lo_help_reader.close();
 
 		gv_help = lo_help_sb.toString();
 
 	}// idmacs_builtins_open_datasource
 
 	/**
 	 * Creates snippet files for each built-in function, and one dictionary file
 	 * containing all built-in function names.
 	 * 
 	 * Process the content of global variable gv_help in one step, i.e. this
 	 * function is designed to be invoked ONLY ONCE, and it requires that
 	 * idmacs_builtins_open_datasource() must have been executed before, because
 	 * that initializes gv_help.
 	 * 
 	 * Preconditions: Global gv_help contains string returned by uHelp()
 	 * 
 	 * @throws Exception
 	 */
 	public static void idmacs_builtins_next_entry() throws Exception {
 		Pattern lo_help_pattern = Pattern.compile(GC_REGEX_ONE_FUNCTION,
 				Pattern.COMMENTS | Pattern.DOTALL);
 
 		Matcher lo_help_matcher = lo_help_pattern.matcher(gv_help);
 
 		int lv_match_number = 0;
 
 		// Initialize global list of function names.
 		// Used by idmacs_builtins_create_dictionary after loop.
 		go_func_names = new ArrayList();
 
 		// Process all functions in gv_help
 		while (lo_help_matcher.find()) {
 			String lv_whole_match = lo_help_matcher.group(0);
 			idmacs_trace("START PROCESSING \"" + lv_whole_match + "\"");
 
 			gv_func_name = lo_help_matcher.group(1);
 			gv_func_signature = lo_help_matcher.group(3);
 			gv_func_comment = lo_help_matcher.group(13);
 
 			if (lv_whole_match.trim().equals(gv_func_name)) {
 				idmacs_trace("Ignoring this match"
 						+ " (doesn't look like a function definition)");
 				continue; // ====================== with next function
 			}
 			// Keep track of number of real matches, ignoring odd ones
 			lv_match_number++;
 
 			idmacs_trace("gv_func_name      = \"" + gv_func_name + "\"");
 			idmacs_trace("gv_func_signature = \"" + gv_func_signature + "\"");
 			idmacs_trace("gv_func_comment   = \"" + gv_func_comment + "\"");
 
 			// Note that group 0 always exists, and is not included in the
 			// value returned by groupCount. Therefore, termination condition
 			// must be "less than or equal" (<=), not "less than" (<)
 			for (int i = 0; i <= lo_help_matcher.groupCount(); ++i) {
 				idmacs_trace("Match " + lv_match_number
 						+ ": lo_help_matcher.group(" + i + ")=\""
 						+ lo_help_matcher.group(i) + "\"");
 			}
 			// Cleaning up the global argument list objects is done inside
 			// ==> must always be invoked, even for empty (null) signatures
 			idmacs_builtins_parse_signature();
 
 			// Now that all information about one function has been collected
 			// into global variables, write the corresponding snippet file
 			idmacs_builtins_write_snippet();
 
 			// Collect function names for building dictionary out of loop
 			go_func_names.add(gv_func_name);
 
 		}// while (lo_help_matcher.find())
 
 		// Create dictionary file containing all function names
 		idmacs_builtins_create_dictionary();
 
 		idmacs_trace("Total number of functions successfully parsed: "
 				+ lv_match_number);
 
 	}// idmacs_create_builtin_functions_snippets
 
 	/**
 	 * Parses the signature string of the current function, and stores the
 	 * argument names found in the global list go_func_arg_names. For each
 	 * argument found, a flag will be stored in go_func_arg_opt, indicating
 	 * whether the argument is optional or not.
 	 * 
 	 * Preconditions: Global gv_func_signature contains that functions signature
 	 * string Global gv_func_name contains current function name (for tracing)
 	 * 
 	 * @throws Exception
 	 */
 	private static void idmacs_builtins_parse_signature() throws Exception {
 		// If current function doesn't have any arguments, we're done
 		if (gv_func_signature == null) {
 			go_func_arg_names = null;
 			go_func_arg_opt = null;
 			return; // ========================================== EXIT
 		}
 
 		// Reset global list of argument names to empty list
 		go_func_arg_names = new ArrayList();
 
 		// Reset global list of "is optional" flags to empty list
 		go_func_arg_opt = new ArrayList();
 
 		Pattern lo_one_arg_pattern = Pattern.compile(GC_REGEX_ONE_ARGUMENT,
 				Pattern.COMMENTS);
 		Matcher lo_one_arg_matcher = lo_one_arg_pattern
 				.matcher(gv_func_signature);
 
 		while (lo_one_arg_matcher.find()) {
 			for (int i = 0; i <= lo_one_arg_matcher.groupCount(); ++i) {
 				idmacs_trace(gv_func_name + ": lo_one_arg_matcher.group(" + i
 						+ ")=\"" + lo_one_arg_matcher.group(i) + "\"");
 			}
 
 			// A regular argument e.g. "arg_name" or "arg_type arg_name"
 			String lv_regular_arg_name = lo_one_arg_matcher.group(2);
 			idmacs_trace(gv_func_name + ": lv_regular_arg_name=\""
 					+ lv_regular_arg_name + "\"");
 
 			// An optional argument surrounded by angle brackets , e.g.
 			// <arg_name>
 			String lv_angle_arg_name = lo_one_arg_matcher.group(5);
 			idmacs_trace(gv_func_name + ": lv_angle_arg_name=\""
 					+ lv_angle_arg_name + "\"");
 
 			// An optional argument surrounded by square brackets , e.g.
 			// [arg_name]
 			String lv_square_arg_name = lo_one_arg_matcher.group(7);
 			idmacs_trace(gv_func_name + ": lv_square_arg_name=\""
 					+ lv_square_arg_name + "\"");
 
 			String lv_argument_name = lv_regular_arg_name != null ? lv_regular_arg_name
 					: lv_angle_arg_name != null ? lv_angle_arg_name
 							: lv_square_arg_name;
 
 			// Replace sequences of white space in argument names with one
 			// underscore
 			lv_argument_name = lv_argument_name.replaceAll("\\s+", "_");
 
 			// Remove any ill-positioned commas from argument names
 			lv_argument_name = lv_argument_name.replaceAll(",", "");
 
 			idmacs_trace(gv_func_name + ": lv_argument_name=\""
 					+ lv_argument_name + "\"");
 
 			boolean lv_argument_opt = lv_angle_arg_name != null
 					|| lv_square_arg_name != null;
 
 			idmacs_trace(gv_func_name + ": lv_argument_opt=\""
 					+ lv_argument_opt + "\"");
 
 			go_func_arg_names.add(lv_argument_name);
 			go_func_arg_opt.add(new Boolean(lv_argument_opt));
 		}// while(lo_one_arg_matcher.find())
 
 		idmacs_trace(gv_func_name + ": go_func_arg_names = "
 				+ go_func_arg_names);
 		idmacs_trace(gv_func_name + ": go_func_arg_opt   = " + go_func_arg_opt);
 	}
 
 	/**
 	 * Write the snippet file for function currently being processed.
 	 * 
 	 * Preconditions: Global gv_func_name contains function name
 	 * 
 	 * Global go_func_arg_names contains java.util.List of argument names, or is
 	 * null for functions with empty signature.
 	 * 
 	 * Global go_func_arg_opt contains java.util.List of java.lang.Boolean flags
 	 * indicating whether the index-corresponding element of go_func_arg_names
 	 * is optional (true) or mandatory (false).
 	 * 
 	 * @throws Exception
 	 */
 	private static void idmacs_builtins_write_snippet() throws Exception {
 		File lo_snippets_dir = mkdirs((String) Par.get(GC_SNIPPETS_DIR));
 		File lo_snippet_file = new File(lo_snippets_dir, gv_func_name);
 		FileOutputStream lo_snippet_fos = new FileOutputStream(lo_snippet_file);
 		PrintWriter lo_snippet_writer = new PrintWriter(lo_snippet_fos);
 
 		lo_snippet_writer.println("# name: " + gv_func_name);
 		lo_snippet_writer.println("# --");
 		lo_snippet_writer.print(gv_func_name + "(");
 
 		// Process arguments only if function really has arguments
 		if (go_func_arg_names != null) {
 			// Overall number of arguments in signature
 			int lv_args_count = go_func_arg_names.size();
 
 			for (int i = 0; i < lv_args_count; ++i) {
 				// The number of the current argument, starting with 1
 				int lv_func_arg_num = i + 1;
 				String lv_func_arg_name = (String) go_func_arg_names.get(i);
 				boolean lv_func_arg_opt = ((Boolean) go_func_arg_opt.get(i))
 						.booleanValue();
 
 				if (lv_args_count > 1) {
 					lo_snippet_writer.println();
 				}
 
 				if (lv_func_arg_opt) {
 					lo_snippet_writer.print("/*");
 				}
 				if (lv_func_arg_num > 1) {
 					lo_snippet_writer.print(",");
 				}
 				if (!lv_func_arg_opt) {
 					lo_snippet_writer.print("${" + lv_func_arg_num + ":");
 				}
 
 				lo_snippet_writer.print(lv_func_arg_name);
 
 				if (!lv_func_arg_opt) {
 					lo_snippet_writer.print("}");
 				} else {
 					lo_snippet_writer.print("*/");
 				}
 
 			}// for (int i = 0; i < lv_args_count; ++i)
 
 			// Put closing paren on a separate line,
 			// but only for multi-argument functions
 			if (lv_args_count > 1) {
 				lo_snippet_writer.println();
 			}
 		}// if(gt_func_arg_names != null) {
 
 		// Always add closing signature paren,
 		// followed by "end of snippet" marker
 		lo_snippet_writer.print(")$0");
 
 		// Finalize current snippet file
 		lo_snippet_writer.flush();
 		lo_snippet_writer.close();
 	}
 
 	/**
 	 * Create a dictionary file of all built-in function names, i.e. a file that
 	 * contains the name of each built-in function on a separate line.
 	 * 
 	 * This file will be used inside Emacs to populate the variable
 	 * js2-additional-externs, which will make js2-mode recognize these function
 	 * names as externally declared, and not produce any syntax warnings for
 	 * them.
 	 * 
 	 * @throws Exception
 	 */
 	private static void idmacs_builtins_create_dictionary() throws Exception {
 		File lo_dictionary_dir = mkdirs((String) Par.get(GC_DICTIONARY_DIR));
 		File lo_dictionary_file = new File(lo_dictionary_dir, "js2-mode");
 		FileOutputStream lo_dictionary_fos = new FileOutputStream(
 				lo_dictionary_file);
 		PrintWriter lo_dictionary_writer = new PrintWriter(lo_dictionary_fos);
 
 		for (int i = 0; i < go_func_names.size(); ++i) {
			String gv_func_name = (String) go_func_names.get(i);
			lo_dictionary_writer.println(gv_func_name);
 		}// for
 
 		lo_dictionary_writer.flush();
 		lo_dictionary_writer.close();
 	}
 
 	private static File mkdirs(String iv_dir_name) throws Exception {
 		File lo_dir = null;
 		lo_dir = new File(iv_dir_name);
 
 		if (lo_dir.exists()) {
 			if (!lo_dir.isDirectory()) {
 				idmacs_trace(lo_dir.getCanonicalPath() + ": not a directory");
 				System.exit(-1); // ============================= EXIT
 			}
 		} else {
 			lo_dir.mkdirs();
 		}
 
 		return lo_dir;
 	}
 
 	public static void main(String[] args) throws Exception {
 		Par = new HashMap();
 
 		String lv_snippets_dir = args.length > 0 ? args[0] : ".snippets";
 		Par.put(GC_SNIPPETS_DIR, lv_snippets_dir);
 
 		String lv_dictionary_dir = args.length > 1 ? args[1] : ".dictionary";
 		Par.put(GC_DICTIONARY_DIR, lv_dictionary_dir);
 
 		String lv_help_file = args.length > 2 ? args[2] : "idmacs_uhelp.txt";
 		Par.put(GC_HELP_FILE, lv_help_file);
 
 		idmacs_builtins_open_datasource();
 
 		idmacs_builtins_next_entry();
 
 	}// main
 
 }// Main
