 package CodeTransform;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 public class HTMLWriter {
 
 	private CodeParser parser_;
 
 	public HTMLWriter(CodeParser parser) {
 		parser_ = parser;
 	}
 
 	void write(File file) {
 		try {
 			if (file.exists() == false) {
 				file.createNewFile();
 			}
 
 			FileOutputStream fileOutputStream = new FileOutputStream(file);
 			writeHeader(fileOutputStream, file.getName());
 			writeBody(fileOutputStream);
 			writeFooter(fileOutputStream);
 			fileOutputStream.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void writeFooter(FileOutputStream fileOutputStream) {
 		String footerString = "</body>\r\n</html>";
 		try {
 			fileOutputStream.write(footerString.getBytes());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private String replaceEscapeChars(String src) {
 		src = src.replace("&", "&amp;");
 		src = src.replace(" ", "&nbsp;");
 		src = src.replace("\r\n", "&nbsp;\r\n");
 		src = src.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
 		src = src.replace("<", "&lt;");
 		src = src.replace(">", "&gt;");
 
 		return src;
 	}
 
 	private void writeBody(FileOutputStream fileOutputStream) {
 		ArrayList<ParsedCode> parsedCodes = parser_.getParserResult();
 		try {
 			fileOutputStream
 					.write("	<div class=\"code\" style=\"background-color:#E7E5DC\">\r\n"
 							.getBytes());
 			fileOutputStream
 					.write("		<ol start=\"1\" style=\"margin:0 0 1px 45px !important;\">\r\n"
 							.getBytes());
 
 			for (int i = 0; i < parsedCodes.size(); i++) {
 				ParsedCode parsedCode = parsedCodes.get(i);
 				fileOutputStream
 						.write("			<li style=\"border-left: 3px solid #999;\">\r\n"
 								.getBytes());
 				String colorString = ColorConverter
 						.Color2String(parsedCode.codeColor_);
 				String fontString = parsedCode.codeFont_;
 				String spanString = "				<span style=\"color:" + colorString
 						+ "; font:" + fontString + "\" >";
 				fileOutputStream.write(spanString.getBytes());
 				do {
 					String codeString = parsedCode.codeString_;
 
 					// 检测换行个数，两个以上的话拆分成开
 					int index = codeString.indexOf("\n");
 					if (index >= 0) {
 						index++;
 						if (index < codeString.length()) {
 							// 至少有两个换行
 							String rightString = codeString.substring(index);
 							ParsedCode parsedCodeNew = new ParsedCode();
 							parsedCodeNew.codeColor_ = parsedCode.codeColor_;
 							parsedCodeNew.codeString_ = rightString;
 							parsedCodeNew.codeFont_ = parsedCode.codeFont_;
 							parsedCodes.add(i + 1, parsedCodeNew);
 
 							String leftString = codeString.substring(0, index);
 							parsedCode.codeString_ = leftString;
 							parsedCodes.set(i, parsedCode);
 
 							codeString = leftString;
 						}
 					}
 
 					codeString = replaceEscapeChars(codeString);
 					fileOutputStream.write(codeString.getBytes());
 
 					if (codeString.indexOf("\n") > 0) {
 						break;
 					}
 
 					if (i < parsedCodes.size() - 1) {
 						ParsedCode nextCode = parsedCodes.get(i + 1);
 						if ((nextCode.codeColor_.equals(parsedCode.codeColor_) == false || nextCode.codeFont_
 								.equals(parsedCode.codeFont_))
 								&& nextCode.codeString_.indexOf('\n') < 0) {
 							// 颜色不一样，又不是回车
 							colorString = ColorConverter
 									.Color2String(nextCode.codeColor_);
 							fontString = nextCode.codeFont_;
 							spanString = "</span><span style=\"color:"
 									+ colorString + "; font:" + fontString
 									+ "\" >";
 							fileOutputStream.write(spanString.getBytes());
 
 							parsedCode = nextCode;
 							++i;
 						} else if (nextCode.codeColor_
 								.equals(parsedCode.codeColor_)) {
 							parsedCode = nextCode;
 							++i;
 						} else {
 							parsedCode = nextCode;
 							++i;
 						}
 					} else {
 						break;
 					}
 				} while (i < parsedCodes.size());
 				spanString = "				</span>\r\n";
 				fileOutputStream.write(spanString.getBytes());
 
 				fileOutputStream.write("			</li>\r\n".getBytes());
 			}
 
 			fileOutputStream.write("		</ol>\r\n".getBytes());
 			fileOutputStream.write("	</div>\r\n".getBytes());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeHeader(FileOutputStream fileOutputStream, String title) {
 		String headerString = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" "
 				+ "\"http://www.w3.org/TR/html4/loose.dtd\">\r\n"
 				+ "<html>\r\n"
 				+ "<head>\r\n"
 				+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n"
 				+ "<title>"
 				+ title
 				+ "</title>\r\n"
 				+ "</head>\r\n"
 				+ "<body>\r\n";
 		try {
 			fileOutputStream.write(headerString.getBytes());
 			System.out.println("HTMLWriter.writeHeader()");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param args
 	 */
//	 public static void main(String[] args) {
//	 // CodeParser parser = new CppParser();
//	 // parser.init(new File("temp/cpptest.cpp"));
//	 // parser.parse();
//	 //
//	 // HTMLWriter writer = new HTMLWriter(parser);
//	 // writer.write(new File("CppTest.html"));
//	
 //	 CodeParser parser = new JavaParser();
 //	 parser.init(new File("temp/javatest.java"));
 //	 parser.parse();
 //	
 //	 HTMLWriter writer = new HTMLWriter(parser);
 //	 writer.write(new File("JavaTest.html"));
//	
//	 }
 
 }
