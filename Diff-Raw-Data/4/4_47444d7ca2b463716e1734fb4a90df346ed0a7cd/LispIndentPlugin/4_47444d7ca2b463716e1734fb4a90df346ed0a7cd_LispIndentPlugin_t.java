 
 import org.gjt.sp.jedit.buffer.BufferAdapter;
 import org.gjt.sp.jedit.buffer.BufferListener;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.buffer.JEditBuffer;
 import org.gjt.sp.jedit.Buffer;
 import org.gjt.sp.jedit.textarea.TextArea;
 
 public class LispIndentPlugin extends org.gjt.sp.jedit.EditPlugin {
 	//static boolean is_clojure_buffer(Buffer buffer) {
 	//	String p = buffer.getPath();
 	//	return p.endsWith(".clj") || p.endsWith(".cljs");
 	//}
 	
 	static int get_line_offset(JEditBuffer buffer, int index) {
 		int line = buffer.getLineOfOffset(index);
 		return index - buffer.getLineStartOffset(line);
 	}
 	
 	static int get_bracket_indent(JEditBuffer buffer, int index) { return get_line_offset(buffer, index) + 1; }
 	static int get_parenthesis_indent(JEditBuffer buffer, int index) { return get_line_offset(buffer, index) + 2; }
 	
 	static int get_indent_of_line(JEditBuffer buffer, int line) {
 		int i = buffer.getLineStartOffset(line);
 		int sum = 0;
 		while(buffer.getText(i, 1).equals(" ")) {
 			sum += 1;
 			i += 1;
 		}
 		return sum;
 	}
 	
 	static int get_indent(int start, JEditBuffer buffer) {
 		if(start < 0) { return 0; }
 		int start_line = buffer.getLineOfOffset(start);
 		int current_line_start = buffer.getLineStartOffset(start_line);
 		int br = 0;       // bracket count
 		int cbr = 0;      // curly bracket count
 		int pa = 0;       // parenthesis count
 		String c;
 		for(int i = start; i >= 0; i--) {
 			c = buffer.getText(i, 1);
 			// update counts
 			if(c.equals("("))      { pa += 1; }
 			else if(c.equals(")")) { pa -= 1; }
 			else if(c.equals("[")) { br += 1; }
 			else if(c.equals("]")) { br -= 1; }
 			else if(c.equals("{")) { cbr += 1; }
 			else if(c.equals("}")) { cbr -= 1; }
 			// check counts for possible indenting
 			if(br > 0 || cbr > 0) { return get_bracket_indent(buffer, i); }
 			else if(pa > 0) {
 				if(i != 0 && buffer.getText(i - 1, 1).equals("'")) { // indent as list
 					return get_bracket_indent(buffer, i);
 				}
				else { return get_parenthesis_indent(buffer, i); }   // indent as function call
 			}
 			else if(c.equals("\n") && br == 0 && cbr == 0 && pa == 0) {
 				return get_indent_of_line(buffer, buffer.getLineOfOffset(i + 1));
 			}
 		}
 		return 0;
 	}
 	
 	static String build_indent_string(int offset) {
 		String str = "";
 		for(int i = 0; i < offset; i++) { str = str.concat(" "); }
 		return str;
 	}
 	
 	static void indent_line(JEditBuffer buffer, int line) {
 		int line_start = buffer.getLineStartOffset(line);
 		int current_indent = get_indent_of_line(buffer, line);
 		buffer.remove(line_start, current_indent);
 		// we need "- 2" here to "skip" the newline
 		buffer.insert(line_start, build_indent_string(get_indent(line_start - 2, buffer)));
 	}
 	
 	public static void indent(JEditBuffer buffer, TextArea textArea) {
 		int[] lines = textArea.getSelectedLines();
 		for(int i = 0; i < lines.length; i++) {
 			int line = lines[i];
 			indent_line(buffer, line);
 		}
 	}
 	
 	public static void insert_enter_and_indent(JEditBuffer buffer, TextArea textArea) {
		// we need "- 1" here to skip the newline (if the caret is at the end of a line)
 		int indent = get_indent(textArea.getCaretPosition() - 1, buffer);
 		buffer.insert(textArea.getCaretPosition(), "\n");
 		buffer.insert(textArea.getCaretPosition(), build_indent_string(indent));
 	}
 }
