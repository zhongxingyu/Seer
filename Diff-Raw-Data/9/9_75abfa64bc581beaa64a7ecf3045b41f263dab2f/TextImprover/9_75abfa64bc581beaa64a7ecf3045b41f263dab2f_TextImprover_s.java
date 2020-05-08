 package vlt.text.textimprover;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class TextImprover {
     private final String REGEXP_WORD = "[--0-9]+";
     private List<LexicalUnit> lexicalUnitList;
     private Map<String, Rule> ruleMap;
 
     public TextImprover(String configText) throws ParserConfigurationException,
 	    SAXException, IOException {
 	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
 	TextImproverPreferencesHandler handler = new TextImproverPreferencesHandler();
 	saxParser.parse(configText, handler);
 	lexicalUnitList = handler.getLexicalUnitList();
 	ruleMap = handler.getRuleMap();
     }
 
     public TextImprover(InputStream in) throws ParserConfigurationException,
 	    SAXException, IOException {
 	SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
 	TextImproverPreferencesHandler handler = new TextImproverPreferencesHandler();
 	saxParser.parse(in, handler);
 	lexicalUnitList = handler.getLexicalUnitList();
 	ruleMap = handler.getRuleMap();
 	in.close();
     }
 
     public String improveText(String text) {
 	StringBuffer textis = new StringBuffer();
 	Pattern pattern = Pattern.compile(REGEXP_WORD);
 	Matcher matcher = pattern.matcher(text);
 	while (matcher.find()) {
 	    matcher.appendReplacement(textis, improveWord(matcher.group()));
 	}
 	matcher.appendTail(textis);
 	return textis.toString();
     }
 
     private String improveWord(String word) {
 	LexicalUnit lexicalUnit = chooseLexicalUnit(word);
 	if (lexicalUnit == null) {
 	    return word;
 	}
 	return lexicalUnit.improveWord(word);
     }
 
     private LexicalUnit chooseLexicalUnit(String word) {
 	// TODO based on occurence chance
 	List<LexicalUnit> goodList = new ArrayList<LexicalUnit>();
 	for (int i = 0; i < lexicalUnitList.size(); i++) {
 	    if (lexicalUnitList.get(i).checkAgainstRules(word) != null) {
 		goodList.add(lexicalUnitList.get(i));
 	    }
 	}
 	if (goodList.isEmpty()) {
 	    return null;
 	}
 	Random r = new Random();
 	LexicalUnit lexicalUnit;
 	do {
 	    lexicalUnit = lexicalUnitList.get(r.nextInt(goodList.size()));
 	} while (lexicalUnit.checkAgainstRules(word) == null);
 	return lexicalUnit;
     }
 }
 
 class TextImproverPreferencesHandler extends DefaultHandler {
     private List<LexicalUnit> lexicalUnitList;
     private Map<String, Rule> ruleMap;
     private LexicalUnit currentLexicalUnit;
     private Rule currentRule;
     private String currentElementName;
 
     @Override
     public void startElement(String uri, String localName, String qName,
 	    Attributes attributes) throws SAXException {
 	if (qName.equalsIgnoreCase("LexicalUnits")) {
 	    lexicalUnitList = new ArrayList<LexicalUnit>();
 	}
 	if (qName.equalsIgnoreCase("LexicalUnit")) {
 	    currentLexicalUnit = new LexicalUnit();
 	}
 	if (qName.equalsIgnoreCase("Rules")) {
 	    ruleMap = new HashMap<String, Rule>();
 	}
 	if (qName.equalsIgnoreCase("Rule-Description")) {
 	    currentRule = new Rule();
 	}
 	currentElementName = qName.toLowerCase(Locale.getDefault());
     }
 
     @Override
     public void endElement(String uri, String localName, String qName)
 	    throws SAXException {
 	if (qName.equalsIgnoreCase("LexicalUnit")) {
 	    lexicalUnitList.add(currentLexicalUnit);
 	    currentLexicalUnit = null;
 	}
 	if (qName.equalsIgnoreCase("Rule-Description")) {
 	    ruleMap.put(currentRule.getName(), currentRule);
 	    currentRule = null;
 	}
     }
 
     @Override
     public void characters(char[] ch, int start, int length)
 	    throws SAXException {
 	String value = new String(ch, start, length).trim();
 	if (value.equals("")) {
 	    return;
 	}
 	if (currentElementName.equalsIgnoreCase("prefix")) {
 	    currentLexicalUnit.setPrefix(value);
 	    return;
 	}
 	if (currentElementName.equalsIgnoreCase("regexp")) {
 	    currentRule.setRegexp(value);
 	    return;
 	}
 	if (currentElementName.equalsIgnoreCase("chance")) {
 	    currentLexicalUnit.setOccurenceChance(Integer.parseInt(value));
 	    return;
 	}
 	if (currentElementName.equalsIgnoreCase("throwaway")) {
 	    currentRule.setThrowAwayRegex(value);
 	    return;
 	}
 	if (currentElementName.equalsIgnoreCase("outcomeLetter")) {
 	    currentRule.setOutcomeLetter(value);
 	    return;
 	}
 	if (currentElementName.equalsIgnoreCase("allowedlength")) {
 	    currentRule.setAllowedLength(Integer.parseInt(value));
 	    return;
 	}
 	if (currentElementName.equalsIgnoreCase("rule-name")) {
 	    if (currentRule == null) {
 		currentLexicalUnit.addToRuleSet(ruleMap.get(value));
 	    } else {
 		currentRule.setName(value);
 	    }
 	}
     }
 
     public List<LexicalUnit> getLexicalUnitList() {
 	return lexicalUnitList;
     }
 
     public Map<String, Rule> getRuleMap() {
 	return ruleMap;
     }
 }
