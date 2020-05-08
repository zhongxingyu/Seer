 /*模块名称: Java代码分析
  *主要功能：
  *1，划分单词以及符号
  *2，确定每个关键字颜色
  *3，确定注释块并上色
  *		包括行注释，块注释，文档注释
  *4，确定字符串块并上色
  *5，提供颜色设置功能
  */
 
 package CodeTransform;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import javax.xml.parsers.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class JavaParser extends CodeParser {
 	enum CodeBlock {
 		CodeBlockKeyWord, // 关键字
 		CodeBlockLineComment, // 单行注释
 		CodeBlockMultiLineComment, // 多行注释
 		CodeBlockDocumentComment, // 文档注释，类似 /** 注释内容 */
 		CodeBlockString, // 字符串内容，就是 "这种"
 		CodeBlockChar, // 字符内容，就是 '这种'
 		CodeBlockDefault // 默认块
 	}
 
 	private File sourceFile_ = null;
 	private ArrayList<ParsedCode> parsedResult_ = null;
 	private Node keyWordNode_ = null;
 	private Node colorNode_ = null;
 	private Node fontNode_ = null;
 	private ArrayList<String> keyWordList_ = null;
 	private HashMap<String, Color> colorMap_ = null;
 	private HashMap<String, String> fontMap_ = null;
 
 	// xml读取部分
 	private DocumentBuilderFactory documentBuilderFactory_ = null;
 	private DocumentBuilder documentBuilder_ = null;
 	private Document document_ = null;
 	private Element rootNode_ = null;
 
 	@Override
 	void init(File sourceFile) {
 		if (sourceFile.exists() == false) {
 			System.err.println("找不到文件 " + sourceFile.getAbsolutePath());
 			return;
 		}
 
 		try {
 			sourceFile_ = sourceFile;
 			keyWordList_ = new ArrayList<String>();
 			colorMap_ = new HashMap<String, Color>();
 			fontMap_ = new HashMap<String, String>();
 
 			initXmlConfig();
 			// 必须先初始化xml
 			initKeyWordList();
 			initColorHashMap();
 			initFontHashMap();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	void parse() {
 		try {
 			parsedResult_ = new ArrayList<ParsedCode>();
 			InputStream inputStream = new FileInputStream(sourceFile_);
 			byte[] buffer = new byte[(int) sourceFile_.length()];
 			inputStream.read(buffer);
 			ArrayList<String> wordArrayList = parsePrintableWords(buffer);
 
 			for (int i = 0; i < wordArrayList.size(); i++) {
 				String wordString = wordArrayList.get(i);
 
 				if (hasKeyWord(wordString)) {
 					// 关键字块
 					i = parseKeyWord(wordArrayList, i);
 
 				} else if (wordString.indexOf("//") >= 0) {
 					i = parseLineComment(wordArrayList, i);
 
 				} else if (wordString.indexOf("/**") >= 0) {
 					i = parseDocumentComment(wordArrayList, i);
 
 				} else if (wordString.indexOf("/*") >= 0) {
 					i = parseMultiLineComment(wordArrayList, i);
 
 				} else if (wordString.indexOf("\"") >= 0) {
 					i = parseString(wordArrayList, i);
 
 				} else if (wordString.indexOf('\'') >= 0) {
 					// 单个字符
 					i = parseChar(wordArrayList, i);
 
 				} else {
 					i = parseDefault(wordArrayList, i);
 				}
 			}
 
 			inputStream.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private int parseChar(ArrayList<String> wordArrayList, int i) {
 		Color color = this.colorMap_.get("Char");
 
 		String wordString = wordArrayList.get(i);
 		int beginIndex = wordString.indexOf('\'');
 		int endIndex = -1;
 		String commentString = wordString.substring(beginIndex);
 		if (beginIndex > 0) {
 			// 不是在单词第一个字母
 			String leftString = wordString.substring(0, beginIndex);
 			wordArrayList.set(i, leftString);
 			wordArrayList.add(i + 1, commentString);
 			// 简单处理下就好了，返回去重新分析，没准刚刚分出来的是关键字
 			i--;
 		} else {
 			// 接下来都是字符串内容
 
 			// 屏蔽第一个"字符
 			endIndex = 1;
 			do {
 				endIndex = wordString.indexOf('\'', endIndex);
 				if (endIndex > 0 && wordString.charAt(endIndex - 1) == '\\') {
 					endIndex++;
 					// 不是结束符，转义字符
 					continue;
 				} else if (endIndex > 0
 						&& wordString.charAt(endIndex - 1) != '\\') {
 					break;
 				} else if (endIndex == 0) {
 					break;
 				}
 
 				ParsedCode parsedCode = new ParsedCode();
 				parsedCode.codeString_ = wordString;
 				parsedCode.codeColor_ = color;
 				parsedCode.codeFont_ = fontMap_.get("Char");
 				parsedResult_.add(parsedCode);
 
 				if (wordArrayList.size() > i + 1) {
 					wordString = wordArrayList.get(++i);
 				} else {
 					break;
 				}
 			} while (true);
 
 			// 到这里字符串块结束，但是结束符还没弄进去
 			endIndex++;
 			String leftString = wordString.substring(0, endIndex);
 			wordArrayList.set(i, leftString);
 			ParsedCode parsedCode = new ParsedCode();
 
 			parsedCode.codeString_ = leftString;
 			parsedCode.codeColor_ = color;
 			parsedCode.codeFont_ = fontMap_.get("Char");
 			parsedResult_.add(parsedCode);
 
 			if (wordString.length() > beginIndex) {
 				String remainString = wordString.substring(endIndex);
 				wordArrayList.add(i + 1, remainString);
 			}
 		}
 
 		return i;
 	}
 
 	private int parseString(ArrayList<String> wordArrayList, int i) {
 		Color color = this.colorMap_.get("String");
 		String wordString = wordArrayList.get(i);
 		int beginIndex = wordString.indexOf("\"");
 		int endIndex = -1;
 		String commentString = wordString.substring(beginIndex);
 		if (beginIndex > 0) {
 			// 不是在单词第一个字母
 			String leftString = wordString.substring(0, beginIndex);
 			wordArrayList.set(i, leftString);
 			wordArrayList.add(i + 1, commentString);
 			// 简单处理下就好了，返回去重新分析，没准刚刚分出来的是关键字
 			i--;
 		} else {
 			// 接下来都是字符串内容
 
 			// 屏蔽第一个"字符
 			endIndex = 1;
 			do {
 
 				endIndex = wordString.indexOf("\"", endIndex);
 				if (endIndex > 0 && wordString.charAt(endIndex - 1) == '\\') {
 					endIndex++;
 					// 不是结束符，转义字符
 					continue;
 				} else if (endIndex > 0
 						&& wordString.charAt(endIndex - 1) != '\\') {
 					break;
 				} else if (endIndex == 0) {
 					break;
 				}
 
 				ParsedCode parsedCode = new ParsedCode();
 				parsedCode.codeString_ = wordString;
 				parsedCode.codeColor_ = color;
 				parsedCode.codeFont_ = fontMap_.get("String");
 				parsedResult_.add(parsedCode);
 
 				if (wordArrayList.size() > i + 1) {
 					wordString = wordArrayList.get(++i);
 				} else {
 					break;
 				}
 			} while (true);
 
 			// 到这里字符串块结束，但是结束符还没弄进去
 			endIndex++;
 			String leftString = wordString.substring(0, endIndex);
 			wordArrayList.set(i, leftString);
 			ParsedCode parsedCode = new ParsedCode();
 
 			parsedCode.codeString_ = leftString;
 			parsedCode.codeColor_ = color;
 			parsedCode.codeFont_ = fontMap_.get("String");
 			parsedResult_.add(parsedCode);
 
 			if (wordString.length() > beginIndex) {
 				String remainString = wordString.substring(endIndex);
 				wordArrayList.add(i + 1, remainString);
 			}
 		}
 
 		return i;
 	}
 
 	private int parseMultiLineComment(ArrayList<String> wordArrayList, int i) {
 		Color color = this.colorMap_.get("MultiLineComment");
 		String wordString = wordArrayList.get(i);
 		int beginIndex = wordString.indexOf("/*");
 		String commentString = wordString.substring(beginIndex);
 		if (beginIndex > 0) {
 			// 注释不是在单词第一个字母
 			String leftString = wordString.substring(0, beginIndex);
 			wordArrayList.set(i, leftString);
 			wordArrayList.add(i + 1, commentString);
 			// 简单处理下就好了，返回去重新分析，没准刚刚分出来的是关键字
 			i--;
 		} else {
 			// 接下来都是注释内容
 			while (wordString.indexOf("*/") < 0) {
 				ParsedCode parsedCode = new ParsedCode();
 				parsedCode.codeString_ = wordString;
 				parsedCode.codeColor_ = color;
 				parsedCode.codeFont_ = fontMap_.get("MultiLineComment");
 				parsedResult_.add(parsedCode);
 
 				wordString = wordArrayList.get(++i);
 			}
 
 			// 到这里注释块结束，但是结束符还没弄进去
 			beginIndex = wordString.indexOf("*/") + 2;
 			String leftString = wordString.substring(0, beginIndex);
 			wordArrayList.set(i, leftString);
 			ParsedCode parsedCode = new ParsedCode();
 			parsedCode.codeString_ = leftString;
 			parsedCode.codeColor_ = color;
 			parsedCode.codeFont_ = fontMap_.get("MultiLineComment");
 			parsedResult_.add(parsedCode);
 			if (wordString.length() > beginIndex) {
 				String remainString = wordString.substring(beginIndex);
 				wordArrayList.add(i + 1, remainString);
 			}
 		}
 
 		return i;
 	}
 
 	private int parseDocumentComment(ArrayList<String> wordArrayList, int i) {
 		Color color = this.colorMap_.get("DocumentComment");
 		String wordString = wordArrayList.get(i);
 		int beginIndex = wordString.indexOf("/**");
 		String commentString = wordString.substring(beginIndex);
 		if (beginIndex > 0) {
 			// 注释不是在单词第一个字母
 			String leftString = wordString.substring(0, beginIndex);
 			wordArrayList.set(i, leftString);
 			wordArrayList.add(i + 1, commentString);
 			// 简单处理下就好了，返回去重新分析，没准刚刚分出来的是关键字
 			i--;
 		} else {
 			// 接下来都是注释内容
 			while (wordString.indexOf("*/") < 0) {
 				ParsedCode parsedCode = new ParsedCode();
 				parsedCode.codeString_ = wordString;
 				parsedCode.codeColor_ = color;
 				parsedCode.codeFont_ = fontMap_.get("DocumentComment");
 				parsedResult_.add(parsedCode);
 
 				wordString = wordArrayList.get(++i);
 			}
 
 			// 到这里注释块结束，但是结束符还没弄进去
 			beginIndex = wordString.indexOf("*/") + 2;
 			String leftString = wordString.substring(0, beginIndex);
 			wordArrayList.set(i, leftString);
 			ParsedCode parsedCode = new ParsedCode();
 			parsedCode.codeString_ = leftString;
 			parsedCode.codeColor_ = color;
 			parsedCode.codeFont_ = fontMap_.get("DocumentComment");
 			parsedResult_.add(parsedCode);
 			if (wordString.length() > beginIndex) {
 				String remainString = wordString.substring(beginIndex);
 				wordArrayList.add(i + 1, remainString);
 			}
 
 		}
 
 		return i;
 	}
 
 	private int parseKeyWord(ArrayList<String> wordArrayList, int i) {
 		// 关键字
 		String wordString = wordArrayList.get(i);
 		ParsedCode parsedCode = new ParsedCode();
 		parsedCode.codeString_ = wordString;
 		parsedCode.codeColor_ = colorMap_.get("KeyWord");
 		parsedCode.codeFont_ = fontMap_.get("KeyWord");
 		parsedResult_.add(parsedCode);
 		return i;
 	}
 
 	private int parseLineComment(ArrayList<String> wordArrayList, int i) {
 		// 注释块1
 		String wordString = wordArrayList.get(i);
 		ParsedCode parsedCode = new ParsedCode();
 		parsedCode.codeString_ = wordString;
 
 		int beginIndex = wordString.indexOf("//");
 		String commentString = wordString.substring(beginIndex);
 		if (beginIndex > 0) {
 			// 注释不是在单词第一个字母
 			String leftString = wordString.substring(0, beginIndex);
 			wordArrayList.set(i, leftString);
 			wordArrayList.add(i + 1, commentString);
 
 			// 简单处理下就好了，返回去重新分析，没准刚刚分出来的是关键字
 			i--;
 		} else {
 			parsedCode.codeColor_ = colorMap_.get("LineComment");
 			parsedCode.codeFont_ = fontMap_.get("LineComment");
 			parsedResult_.add(parsedCode);
 			// 接下来都是注释内容
 			while (wordString.indexOf('\n') < 0) {
 				if (i < wordArrayList.size() - 1) {
 					wordString = wordArrayList.get(++i);
 				} else {
 					break;
 				}
 
 				parsedCode = new ParsedCode();
 				parsedCode.codeString_ = wordString;
 				parsedCode.codeColor_ = colorMap_.get("LineComment");
 				parsedCode.codeFont_ = fontMap_.get("LineComment");
 				parsedResult_.add(parsedCode);
 			}
 		}
 
 		return i;
 	}
 
 	private int parseDefault(ArrayList<String> wordArrayList, int i) {
 		// 普通块
 		String wordString = wordArrayList.get(i);
 		ParsedCode parsedCode = new ParsedCode();
 		parsedCode.codeString_ = wordString;
 		parsedCode.codeColor_ = colorMap_.get("Default");
 		parsedCode.codeFont_ = fontMap_.get("Default");
 		parsedResult_.add(parsedCode);
 		return i;
 	}
 
 	@Override
 	ArrayList<ParsedCode> getParserResult() {
 		return parsedResult_;
 	}
 
 	void initKeyWordList() {
 		// 这里初始化关键字列表
 		NodeList keyWordNodeList = keyWordNode_.getChildNodes();
 		int length = keyWordNodeList.getLength();
 		for (int i = 0; i < length; i++) {
 			Node node = keyWordNodeList.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				NamedNodeMap nodeMap = node.getAttributes();
 				Node tmpNode = nodeMap.getNamedItem("name");
 				String keyWord = tmpNode.getTextContent();
 				if (keyWord.length() > 0) {
 					this.keyWordList_.add(keyWord);
 				}
 			}
 		}
 	}
 
 	void initColorHashMap() {
 		// 这里初始化颜色哈希表和字体哈希表
 		NodeList colorNodeList = colorNode_.getChildNodes();
 		int colorListLength = colorNodeList.getLength();
 		for (int i = 0; i < colorListLength; i++) {
 			Node node = colorNodeList.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				NamedNodeMap nodeMap = node.getAttributes();
 
 				Node tmpNode = nodeMap.getNamedItem("name");
 				String colorKey = tmpNode.getTextContent();
 
 				tmpNode = nodeMap.getNamedItem("value");
 				String colorValueString = tmpNode.getTextContent();
 				Color color = ColorConverter.ColorFromString(colorValueString);
 				colorMap_.put(colorKey, color);
 			}
 		}
 	}
 
 	void initFontHashMap() {
 		// 这里初始化颜色哈希表和字体哈希表
 		NodeList colorNodeList = fontNode_.getChildNodes();
 		int colorListLength = colorNodeList.getLength();
 		for (int i = 0; i < colorListLength; i++) {
 			Node node = colorNodeList.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				NamedNodeMap nodeMap = node.getAttributes();
 
 				Node tmpNode = nodeMap.getNamedItem("name");
 				String fontKey = tmpNode.getTextContent();
 
 				tmpNode = nodeMap.getNamedItem("value");
 				String valueString = tmpNode.getTextContent();
 				fontMap_.put(fontKey, valueString);
 			}
 		}
 	}
 
 	void initXmlConfig() throws ParserConfigurationException, SAXException,
 			IOException {
 		// 下面是初始化xml文件
 		InputStream inputStream = JavaParser.class
 				.getResourceAsStream("/res/JavaParserConfig.xml");
 		documentBuilderFactory_ = DocumentBuilderFactory.newInstance();
 		documentBuilder_ = documentBuilderFactory_.newDocumentBuilder();
 		document_ = documentBuilder_.parse(inputStream);
 		rootNode_ = document_.getDocumentElement();// 获取根节点
 
 		NodeList nodeList = rootNode_.getChildNodes();
 
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node node = nodeList.item(i);
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 				if (node.getNodeName() == "KeyWordSet") {
 					keyWordNode_ = node;
 				} else if (node.getNodeName() == "ColorSet") {
 					colorNode_ = node;
 				} else if (node.getNodeName() == "FontSet") {
 					fontNode_ = node;
 				} else {
 					System.out.println("---" + node.getNodeName());
 				}
 			}
 		}
 
 		if (keyWordNode_ == null || colorNode_ == null || fontNode_ == null) {
 			throw new IllegalArgumentException("JavaParserConfig.xml 文件非法");
 		}
 	}
 
 	boolean hasKeyWord(String string) {
 		for (String keyWorkString : this.keyWordList_) {
 			int index = string.indexOf(keyWorkString);
 
 			if (index >= 0) {
 				Character beforeChar = '\0';
 				if (index > 0) {
 					beforeChar = string.charAt(index - 1);
 				}
 
 				Character nextChar = '\0';
 				if (index + keyWorkString.length() < string.length() - 1) {
					nextChar = string.charAt(index + keyWorkString.length());
 				}
 
 				if (Character.isLetterOrDigit(beforeChar)
 						|| Character.isLetterOrDigit(nextChar)) {
 					return false;
 				}
 
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public static void main(String[] args) {
 		try {
 			JavaParser parser = new JavaParser();
 			parser.init(new File("temp/javatest.java"));
 			parser.parse();
 			ArrayList<ParsedCode> resultArrayList = parser.getParserResult();
 			for (int i = 0; i < resultArrayList.size(); i++) {
 				System.out.println("i = " + i + ": ---------------------【"
 						+ resultArrayList.get(i) + "】----------------");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void setColor(String item, String value) {
 		String key = null;
 		if (item.equals("字符")) {
 			key = "Char";
 		} else if (item.equals("文档注释")) {
 			key = "DocumentComment";
 		} else if (item.equals("单行注释")) {
 			key = "LineComment";
 		} else if (item.equals("多行注释")) {
 			key = "MultiLineComment";
 		} else if (item.equals("关键字")) {
 			key = "KeyWord";
 		} else if (item.equals("其他")) {
 			key = "Default";
 		} else {
 			throw new IllegalArgumentException();
 		}
 		
 		Color color = ColorConverter.ColorFromString(value);
 		colorMap_.put(key, color);
 	}
 
 	@Override
 	public void setFontName(String text) {
 		for (String key : fontMap_.keySet()) {
 			String valueString = fontMap_.get(key);
 			String[] valuesString = valueString.split(" ");
 			valuesString[4] = text;
 			StringBuffer stringBuffer = new StringBuffer();
 
 			for (int i = 0; i < valuesString.length; i++) {
 				stringBuffer.append(valuesString[i] + " ");
 			}
 
 			valueString = stringBuffer.toString();
 			fontMap_.put(key, valueString);
 		}
 	}
 
 	@Override
 	public void setFontSize(String text) {
 		for (String key : fontMap_.keySet()) {
 			String valueString = fontMap_.get(key);
 			String[] valuesString = valueString.split(" ");
 			valuesString[3] = text;
 			StringBuffer stringBuffer = new StringBuffer();
 
 			for (int i = 0; i < valuesString.length; i++) {
 				stringBuffer.append(valuesString[i] + " ");
 			}
 
 			valueString = stringBuffer.toString();
 			fontMap_.put(key, valueString);
 		}
 	}
 
 	@Override
 	String[] getOptionItems() {
 		String[] itemStrings = { "字符", "文档注释", "单行注释", "多行注释", "关键字", "字符串",
 				"其他" };
 		return itemStrings;
 	}
 
 	@Override
 	Color getItemColor(String item) {
 		if (item.equals("字符")) {
 			return colorMap_.get("Char");
 		} else if (item.equals("文档注释")) {
 			return colorMap_.get("DocumentComment");
 		} else if (item.equals("单行注释")) {
 			return colorMap_.get("LineComment");
 		} else if (item.equals("多行注释")) {
 			return colorMap_.get("MultiLineComment");
 		} else if (item.equals("关键字")) {
 			return colorMap_.get("KeyWord");
 		} else if (item.equals("其他")) {
 			return colorMap_.get("Default");
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	@Override
 	String getItemFontName(String item) {
 		if (item.equals("字符")) {
 			return fontMap_.get("Char").split(" ")[4];
 		} else if (item.equals("文档注释")) {
 			return fontMap_.get("DocumentComment").split(" ")[4];
 		} else if (item.equals("单行注释")) {
 			return fontMap_.get("LineComment").split(" ")[4];
 		} else if (item.equals("多行注释")) {
 			return fontMap_.get("MultiLineComment").split(" ")[4];
 		} else if (item.equals("关键字")) {
 			return fontMap_.get("KeyWord").split(" ")[4];
 		} else if (item.equals("其他")) {
 			return fontMap_.get("Default").split(" ")[4];
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	@Override
 	String getItemFontSize(String item) {
 		if (item.equals("字符")) {
 			return fontMap_.get("Char").split(" ")[3];
 		} else if (item.equals("文档注释")) {
 			return fontMap_.get("DocumentComment").split(" ")[3];
 		} else if (item.equals("单行注释")) {
 			return fontMap_.get("LineComment").split(" ")[3];
 		} else if (item.equals("多行注释")) {
 			return fontMap_.get("MultiLineComment").split(" ")[3];
 		} else if (item.equals("关键字")) {
 			return fontMap_.get("KeyWord").split(" ")[3];
 		} else if (item.equals("其他")) {
 			return fontMap_.get("Default").split(" ")[3];
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	@Override
 	public void setFontName(String item, String text) {
 		String key = null;
 		if (item.equals("字符")) {
 			key = "Char";
 		} else if (item.equals("文档注释")) {
 			key = "DocumentComment";
 		} else if (item.equals("单行注释")) {
 			key = "LineComment";
 		} else if (item.equals("多行注释")) {
 			key = "MultiLineComment";
 		} else if (item.equals("关键字")) {
 			key = "KeyWord";
 		} else if (item.equals("其他")) {
 			key = "Default";
 		} else {
 			throw new IllegalArgumentException();
 		}
 		
 		String valueString = fontMap_.get(key);
 		String[] valuesString = valueString.split(" ");
 		valuesString[4] = text;
 		StringBuffer stringBuffer = new StringBuffer();
 
 		for (int i = 0; i < valuesString.length; i++) {
 			stringBuffer.append(valuesString[i] + " ");
 		}
 
 		valueString = stringBuffer.toString();
 		fontMap_.put(key, valueString);
 	}
 
 	@Override
 	public void setFontSize(String item, String text) {
 		String key = null;
 		if (item.equals("字符")) {
 			key = "Char";
 		} else if (item.equals("文档注释")) {
 			key = "DocumentComment";
 		} else if (item.equals("单行注释")) {
 			key = "LineComment";
 		} else if (item.equals("多行注释")) {
 			key = "MultiLineComment";
 		} else if (item.equals("关键字")) {
 			key = "KeyWord";
 		} else if (item.equals("其他")) {
 			key = "Default";
 		} else {
 			throw new IllegalArgumentException();
 		}
 		
 		String valueString = fontMap_.get(key);
 		String[] valuesString = valueString.split(" ");
 		valuesString[3] = text;
 		StringBuffer stringBuffer = new StringBuffer();
 
 		for (int i = 0; i < valuesString.length; i++) {
 			stringBuffer.append(valuesString[i] + " ");
 		}
 
 		valueString = stringBuffer.toString();
 		fontMap_.put(key, valueString);
 	}
 }
