 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.model;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JTextPane;
 
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolMacroContext;
 import net.rptools.maptool.client.ui.MacroButtonHotKeyManager;
 import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
 import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
 
 /**
  * This (data)class is used by all Macro Buttons, including campaign, global and token macro buttons.
  * @see net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton
  */
 public class MacroButtonProperties implements Comparable<Object> {
 	private transient static final List<String> HTMLColors = Arrays.asList("aqua", "black", "blue", "fuchsia", "gray", "green", "lime", "maroon", "navy", "olive", "purple", "red", "silver", "teal", "white", "yellow");
 	private transient MacroButton button;
 	private transient GUID tokenId;
 	private String saveLocation;
 	private int index;
 	private String colorKey;
 	private String hotKey;
 	private String command;
 	private String label;
 	private String group;
 	private String sortby;
 	private boolean autoExecute;
 	private boolean includeLabel;  //include the macro label when printing output?
 	private boolean applyToTokens; //when the button is clicked it will impersonate every selected token when executing the macro
 	private String fontColorKey;
 	private String fontSize;
 	private String minWidth;
 	private String maxWidth;
	private Boolean allowPlayerEdits = true;
 
 	// constructor that creates a new instance, doesn't auto-save
 	public MacroButtonProperties(int index, String colorKey, String hotKey, String command, 
 			String label, String group, String sortby, boolean autoExecute, boolean includeLabel, 
 			boolean applyToTokens, String fontColorKey, String fontSize, String minWidth, String maxWidth) {
 		setIndex(index);
 		setColorKey(colorKey);
 		setHotKey(hotKey);
 		setCommand(command);
 		setLabel(label);
 		setGroup(group);
 		setSortby(sortby);
 		setAutoExecute(autoExecute);
 		setIncludeLabel(includeLabel);
 		setApplyToTokens(applyToTokens);
 		setFontColorKey(fontColorKey);
 		setFontSize(fontSize);
 		setMinWidth(minWidth);
 		setMaxWidth(maxWidth);
 		setButton(null);
 		setTokenId((GUID)null);
 		setSaveLocation("");
 		setAllowPlayerEdits(true);
 		setCompareGroup(true);
 		setCompareSortPrefix(true);
 		setCompareCommand(true);
 		setCompareIncludeLabel(true);
 		setCompareAutoExecute(true);
 		setCompareApplyToSelectedTokens(true);
 	}
 
 	// constructor that creates a new instance, doesn't auto save
 	public MacroButtonProperties(int index)	{
 		setIndex(index);
 		setColorKey("");
 		setHotKey(MacroButtonHotKeyManager.HOTKEYS[0]);
 		setCommand("");
 		setLabel("(new)");
 		setGroup("");
 		setSortby("");
 		setAutoExecute(true);
 		setIncludeLabel(false);
 		setApplyToTokens(false);
 		setFontColorKey("");
 		setFontSize("");
 		setMinWidth("");
 		setMaxWidth("");
 		setButton(null);
 		setTokenId((GUID)null);
 		setSaveLocation("");
 		setAllowPlayerEdits(true);
 		setCompareGroup(true);
 		setCompareSortPrefix(true);
 		setCompareCommand(true);
 		setCompareIncludeLabel(true);
 		setCompareAutoExecute(true);
 		setCompareApplyToSelectedTokens(true);
 	}
 
 	// constructor for creating a new button in a specific button group, auto-saves
 	public MacroButtonProperties(String panelClass, int index, String group) {
 		this(index);
 		setSaveLocation(panelClass);
 		setGroup(group);
 		setAllowPlayerEdits(true);
 		setCompareGroup(true);
 		setCompareSortPrefix(true);
 		setCompareCommand(true);
 		setCompareIncludeLabel(true);
 		setCompareAutoExecute(true);
 		setCompareApplyToSelectedTokens(true);
 		save();
 	}
 
 	// constructor for creating a new token button in a specific button group, auto-saves
 	public MacroButtonProperties(Token token, int index, String group) {
 		this(index);
 		setSaveLocation("Token");
 		setTokenId(token);
 		setGroup(group);
 		setAllowPlayerEdits(true);
 		setCompareGroup(true);
 		setCompareSortPrefix(true);
 		setCompareCommand(true);
 		setCompareIncludeLabel(true);
 		setCompareAutoExecute(true);
 		setCompareApplyToSelectedTokens(true);
 		save();
 	}
 
 	// constructor for creating a new copy of an existing button, auto-saves
 	public MacroButtonProperties(String panelClass, int index, MacroButtonProperties properties) {
 		this(index);
 		setSaveLocation(panelClass);
 		setColorKey(properties.getColorKey());
 		// use the default hot key
 		setCommand(properties.getCommand());
 		setLabel(properties.getLabel());
 		setGroup(properties.getGroup());
 		setSortby(properties.getSortby());
 		setAutoExecute(properties.getAutoExecute());
 		setIncludeLabel(properties.getIncludeLabel());
 		setApplyToTokens(properties.getApplyToTokens());
 		setFontColorKey(properties.getFontColorKey());
 		setFontSize(properties.getFontSize());
 		setMinWidth(properties.getMinWidth());
 		setMaxWidth(properties.getMaxWidth());
 		setAllowPlayerEdits(properties.getAllowPlayerEdits());
 		setCompareIncludeLabel(properties.getCompareIncludeLabel());
 		setCompareAutoExecute(properties.getCompareAutoExecute());
 		setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
 		setCompareGroup(properties.getCompareGroup());
 		setCompareSortPrefix(properties.getCompareSortPrefix());
 		setCompareCommand(properties.getCompareCommand());
 		save();
 	}
 	
 	// constructor for creating a new copy of an existing token button, auto-saves
 	public MacroButtonProperties(Token token, int index, MacroButtonProperties properties) {
 		this(index);
 		setSaveLocation("Token");
 		setTokenId(token);
 		setColorKey(properties.getColorKey());
 		// use the default hot key
 		setCommand(properties.getCommand());
 		setLabel(properties.getLabel());
 		setGroup(properties.getGroup());
 		setSortby(properties.getSortby());
 		setAutoExecute(properties.getAutoExecute());
 		setIncludeLabel(properties.getIncludeLabel());
 		setApplyToTokens(properties.getApplyToTokens());
 		setFontColorKey(properties.getFontColorKey());
 		setFontSize(properties.getFontSize());
 		setMinWidth(properties.getMinWidth());
 		setMaxWidth(properties.getMaxWidth());
 		setAllowPlayerEdits(properties.getAllowPlayerEdits());
 		setCompareIncludeLabel(properties.getCompareIncludeLabel());
 		setCompareAutoExecute(properties.getCompareAutoExecute());
 		setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
 		setCompareGroup(properties.getCompareGroup());
 		setCompareSortPrefix(properties.getCompareSortPrefix());
 		setCompareCommand(properties.getCompareCommand());
 		save();
 	}
 	
 	// constructor for creating common macro buttons on selection panel
 	public MacroButtonProperties(int index, MacroButtonProperties properties) {
 		this(index);
 		setTokenId((Token) null);
 		setColorKey(properties.getColorKey());
 		// use the default hot key
 		setCommand(properties.getCommand());
 		setLabel(properties.getLabel());
 		setGroup(properties.getGroup());
 		setSortby(properties.getSortby());
 		setAutoExecute(properties.getAutoExecute());
 		setIncludeLabel(properties.getIncludeLabel());
 		setApplyToTokens(properties.getApplyToTokens());
 		setFontColorKey(properties.getFontColorKey());
 		setFontSize(properties.getFontSize());
 		setMinWidth(properties.getMinWidth());
 		setMaxWidth(properties.getMaxWidth());
 		setAllowPlayerEdits(properties.getAllowPlayerEdits());
 		setCompareIncludeLabel(properties.getCompareIncludeLabel());
 		setCompareAutoExecute(properties.getCompareAutoExecute());
 		setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
 		setCompareGroup(properties.getCompareGroup());
 		setCompareSortPrefix(properties.getCompareSortPrefix());
 		setCompareCommand(properties.getCompareCommand());
 		commonMacro = true;
 	}
 
 	public void save (){
 		if (saveLocation.equals("Token") && tokenId != null) {
 			getToken().saveMacroButtonProperty(this);
 		} else if (saveLocation.equals("GlobalPanel")) {
 			MacroButtonPrefs.savePreferences(this);
 		} else if (saveLocation.equals("CampaignPanel")) {
 			MapTool.getCampaign().saveMacroButtonProperty(this);
 		}
 	}
 
  	public void executeMacro() {
 		executeMacro(false);
 	}
 		 
 	public void executeMacro(Boolean runOnSelected) {
 		List<Token> selectedTokens = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList();
 		if(button != null) {
 			if(button.getPanelClass().equals("ImpersonatePanel")){
 				if(runOnSelected || applyToTokens) {
 					for(Token nextToken : selectedTokens) {
 						executeCommand(nextToken.getId());
 					}
 				} else {
 					executeCommand(null);
 				}
 			} else if(button.getPanelClass().equals("SelectionPanel")) {
 				if(MapTool.getFrame().getSelectionPanel().getCommonMacros().contains(this)) {
 					if(runOnSelected || applyToTokens) {
 						if(compareCommand) {
 							for(Token nextToken : selectedTokens) {
 								executeCommand(nextToken.getId());
 							}
 						} else {
 							MapTool.showError("Commonality of this macro is not based on the command field.  The macro cannot be applied to the entire selection set.");
 						}
 					} else {
 						for(Token nextToken : selectedTokens) {
 							for(MacroButtonProperties nextMacro : nextToken.getMacroList(true)) {
 								if(nextMacro.hashCodeForComparison() == hashCodeForComparison()) {
 									nextMacro.executeMacro(nextToken.getId());
 								}
 							}
 						}
 					}
 				} else {
 					if(runOnSelected || applyToTokens) {
 						for(Token nextToken : selectedTokens) {
 							executeCommand(nextToken.getId());
 						}
 					} else {
 						executeCommand(tokenId);
 					}
 				}
 			} else if(button.getPanelClass().equals("CampaignPanel")) {
 				if(runOnSelected || applyToTokens) {
 					for(Token nextToken : selectedTokens) {
 						executeCommand(nextToken.getId());
 					}
 				} else {
 					executeCommand(null);
 				}
 			} else if(button.getPanelClass().equals("GlobalPanel")) {
 				if(runOnSelected || applyToTokens) {
 					for(Token nextToken : selectedTokens) {
 						executeCommand(nextToken.getId());
 					}
 				} else {
 					executeCommand(null);
 				}
 			} else {
 				executeCommand(null);
 			}
 		}
 	}
 	
 	public void executeMacro(GUID tokenId) {
 		executeCommand(tokenId);
 	}
 	
 	private void executeCommand(GUID tokenId) {
 		if (getCommand() != null) {
 			
 			String impersonatePrefix = "";
 			if (tokenId != null){
 				impersonatePrefix = "/im "+tokenId+":";
 			}
 
 			JTextPane commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();
 			String oldText = commandArea.getText();
 
 			if (getIncludeLabel()) {
 				String commandToExecute = getLabel();
 				commandArea.setText(impersonatePrefix + commandToExecute);
 
 				MapTool.getFrame().getCommandPanel().commitCommand();
 			}
 
 			String commandsToExecute[] = parseMultiLineCommand(getCommand());
 
 			for (String command : commandsToExecute) {
 				// If we aren't auto execute, then append the text instead of replace it
 				commandArea.setText(impersonatePrefix + (!getAutoExecute() ? oldText + " " : "") + command);
 				if (getAutoExecute()) {
 					Token contextToken = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
 					boolean trusted = false;
 					if(allowPlayerEdits == null) {
 						allowPlayerEdits = false;
 					}
 					if(saveLocation.equals("Campaign") || !allowPlayerEdits) {
 						trusted = true;
 					}
 					MapToolMacroContext newMacroContext = new MapToolMacroContext(label, contextToken.getName(), trusted, index);
 					MapTool.getFrame().getCommandPanel().commitCommand(newMacroContext);
 				}
 			}
 
 			commandArea.requestFocusInWindow();
 		}
 	}
 
 	private String[] parseMultiLineCommand(String multiLineCommand) {
 
 		// lookahead for new macro "/" after "\n" to prevent unnecessary splitting.
 		String pattern = "\n(?=/)";
 		String[] parsedCommand = multiLineCommand.split(pattern);
 
 		return parsedCommand;
 	}
 
 	public Token getToken() {
 		return MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(this.tokenId);
 	}
 
 	public void setTokenId(Token token){
 		if (token==null){
 			this.tokenId=null;
 		}else{
 			this.tokenId = token.getId();
 		}
 	}
 	
 	public void setTokenId(GUID tokenId){
 		this.tokenId = tokenId;
 	}
 
 	public void setSaveLocation(String saveLocation){
 		if (saveLocation.equals("ImpersonatePanel") || saveLocation.equals("SelectionPanel")) {
 			this.saveLocation = "Token";
 		} else {
 			this.saveLocation = saveLocation;
 		}
 	}
 	
 	public void setButton (MacroButton button){
 		this.button = button;
 	}
 	
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index)	{
 		this.index = index;
 	}
 
 	public String getColorKey()	{
 		if (colorKey==null || colorKey.equals("")){
 			return "default";
 		}
 		return colorKey;
 	}
 
 	public void setColorKey(String colorKey) {
 		this.colorKey = colorKey;
 	}
 
 	public String getHotKey() {
 		return hotKey;
 	}
 
 	public void setHotKey(String hotKey) {
 		this.hotKey = hotKey;
 	}
 
 	public String getCommand() {
 		return command;
 	}
 
 	public void setCommand(String command) {
 		this.command = command;
 	}
 
 	public String getLabel() {
 		return ( label == null ? "" : label );
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public String getGroup() {
 		return ( group == null ? "" : group );			
 	}
 
 	public String getGroupForDisplay() {
 		return this.group;			
 	}
 
 	public void setGroup(String group) {
 		this.group = group;
 	}
 
 	public String getSortby() {
 		return ( sortby == null ? "" : sortby );
 	}
 
 	public void setSortby(String sortby) {
 		this.sortby = sortby;
 	}
 
 	public boolean getAutoExecute()	{
 		return autoExecute;
 	}
 
 	public void setAutoExecute(boolean autoExecute)	{
 		this.autoExecute = autoExecute;
 	}
 
 	public boolean getIncludeLabel() {
 		return includeLabel;
 	}
 
 	public void setIncludeLabel(boolean includeLabel) {
 		this.includeLabel = includeLabel;
 	}
 
 	public boolean getApplyToTokens() {
 		return applyToTokens;
 	}
 
 	public void setApplyToTokens(boolean applyToTokens) {
 		this.applyToTokens = applyToTokens;
 	}
 
 	public static String[] getFontColors() {
 		return (String[])HTMLColors.toArray();
 	}
 	public String getFontColorKey()	{
 		if (!HTMLColors.contains(fontColorKey))
 			return "black";
 		else
 			return fontColorKey;
 	}
 
 	public void setFontColorKey(String fontColorKey) {
 		if (!HTMLColors.contains(fontColorKey))
 			this.fontColorKey = "black";
 		else
 			this.fontColorKey = fontColorKey;
 	}
 
 	public String getFontSize() {
 		return ( fontSize == null || fontSize.equals("") ? "1.00em" : fontSize );
 	}
 
 	public void setFontSize(String fontSize) {
 		this.fontSize = ( fontSize == null || fontSize.equals("") ? "1.00em" : fontSize );
 	}
 
 	public String getMinWidth() {
 		return ( minWidth == null ? "" : minWidth );
 	}
 
 	public void setMinWidth(String minWidth) {
 		this.minWidth = minWidth;
 	}
 
 	public String getMaxWidth() {
 		return ( maxWidth == null ? "" : maxWidth );
 	}
 
 	public void setMaxWidth(String maxWidth) {
 		this.maxWidth = maxWidth;
 	}
 	
 	public Boolean getAllowPlayerEdits() {
 		return allowPlayerEdits;
 	}
 	
 	public void setAllowPlayerEdits(Boolean value) {
 		allowPlayerEdits = value;
 	}
 	
 	public String getSaveLocation() {
 		return saveLocation;
 	}
 
 	public boolean isDuplicateMacro(String source, Token token) {
 		int macroHashCode = hashCodeForComparison();
 		List<MacroButtonProperties> existingMacroList = null;
 		if (source.equalsIgnoreCase("CampaignPanel")){
 			existingMacroList = MapTool.getCampaign().getMacroButtonPropertiesArray();
 		} else if (source.equalsIgnoreCase("GlobalPanel")){
 			existingMacroList = MacroButtonPrefs.getButtonProperties();
 		} else if (token != null){
 			existingMacroList = token.getMacroList(false);
 		} else {
 			return false;
 		}
 		for (MacroButtonProperties existingMacro : existingMacroList){
 			if (existingMacro.hashCodeForComparison() == macroHashCode){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void reset() {
 		colorKey = "";
 		hotKey = MacroButtonHotKeyManager.HOTKEYS[0];
 		command = "";
 		label = String.valueOf(index);
 		group = "";
 		sortby = "";
 		autoExecute = true;
 		includeLabel = false;	
 		applyToTokens = false;
 		fontColorKey = "";
 		fontSize = "";
 		minWidth = "";
 		maxWidth = "";
 	}
 
 	//TODO: may have to rewrite hashcode and equals to only take index into account
 	public boolean equals(Object o) {
 		if (this == o) {
 			return true;
 		}
 		if (o == null || getClass() != o.getClass()) {
 			return false;
 		}
 
 		MacroButtonProperties that = (MacroButtonProperties) o;
 
 		if (autoExecute != that.autoExecute) {
 			return false;
 		}
 		if (includeLabel != that.includeLabel) {
 			return false;
 		}
 		if (applyToTokens != that.applyToTokens) {
 			return false;
 		}
 		if (index != that.index) {
 			return false;
 		}
 		if (colorKey != null ? !colorKey.equals(that.colorKey) : that.colorKey != null) {
 			return false;
 		}
 		if (command != null ? !command.equals(that.command) : that.command != null) {
 			return false;
 		}
 		if (hotKey != null ? !hotKey.equals(that.hotKey) : that.hotKey != null) {
 			return false;
 		}
 		if (label != null ? !label.equals(that.label) : that.label != null) {
 			return false;
 		}
 		if (group != null ? !group.equals(that.group) : that.group != null) {
 			return false;
 		}
 		if (sortby != null ? !sortby.equals(that.sortby) : that.sortby != null) {
 			return false;
 		}
 		if (fontColorKey != null ? !fontColorKey.equals(that.fontColorKey) : that.fontColorKey != null) {
 			return false;
 		}
 		if (fontSize != null ? !fontSize.equals(that.fontSize) : that.fontSize != null) {
 			return false;
 		}
 		if (minWidth != null ? !minWidth.equals(that.minWidth) : that.minWidth != null) {
 			return false;
 		}
 		if (maxWidth != null ? !maxWidth.equals(that.maxWidth) : that.maxWidth != null) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public int hashCode() {   // modified so longest strings are at the end
 		int result;
 		result = index;
 		result = 31 * result + (autoExecute ? 1 : 0);
 		result = 31 * result + (includeLabel ? 1 : 0);
 		result = 31 * result + (applyToTokens ? 1 : 0);
 		result = 31 * result + (minWidth != null ? minWidth.hashCode() : 0);
 		result = 31 * result + (maxWidth != null ? maxWidth.hashCode() : 0);
 		result = 31 * result + (fontSize != null ? fontSize.hashCode() : 0);
 		result = 31 * result + (fontColorKey != null ? fontColorKey.hashCode() : 0);
 		result = 31 * result + (colorKey != null ? colorKey.hashCode() : 0);
 		result = 31 * result + (hotKey != null ? hotKey.hashCode() : 0);
 		result = 31 * result + (label != null ? label.hashCode() : 0);
 		result = 31 * result + (group != null ? group.hashCode() : 0);
 		result = 31 * result + (sortby != null ? sortby.hashCode() : 0);
 		result = 31 * result + (command != null ? command.hashCode() : 0);
 		return result;
 	}
 	
 	// Don't include the index, so you can compare all the other properties between two macros
 	// Also don't include hot key since they can't be the same anyway, or cosmetic fields
 	public int hashCodeForComparison() {
 		int result;
 		result = 0;
 		result = 31 * result + (getCompareAutoExecute() && autoExecute ? 1 : 0);
 		result = 31 * result + (getCompareIncludeLabel() && includeLabel ? 1 : 0);
 		result = 31 * result + (getCompareApplyToSelectedTokens() && applyToTokens ? 1 : 0);
 		result = 31 * result + (getLabel() != null ? label.hashCode() : 0);
 		result = 31 * result + (getCompareGroup() && group != null ? group.hashCode() : 0);
 		result = 31 * result + (getCompareSortPrefix() && sortby != null ? sortby.hashCode() : 0);
 		result = 31 * result + (getCompareCommand() && command != null ? command.hashCode() : 0);
 		return result;
 	}
 
 	// function to enable sorting of buttons; uses the group first, then sortby field
 	// concatenated with the label field.  Case Insensitive
 	public int compareTo(Object b2) throws ClassCastException {
 	    if (!(b2 instanceof MacroButtonProperties))
 	      throw new ClassCastException("A MacroButtonProperties object expected.");
 	    String b1group = getGroup();
 	    if (b1group == null) b1group="";
 	    String b1sortby = getSortby();
 	    if (b1sortby == null) b1sortby="";
 	    String b1label = getLabel();
 	    if (b1label == null) b1label="";
 	    String b2group = ((MacroButtonProperties) b2).getGroup();
 	    if (b2group == null) b2group="";
 	    String b2sortby = ((MacroButtonProperties) b2).getSortby();
 	    if (b2sortby == null) b2sortby="";
 	    String b2label = ((MacroButtonProperties) b2).getLabel();
 	    if (b2label == null) b2label="";
 	    // now parse the sort strings to help dice codes sort properly, use space as a separator
 	    String b1string = modifySortString(" "+b1group+" "+b1sortby+" "+b1label);
 	    String b2string = modifySortString(" "+b2group+" "+b2sortby+" "+b2label);
 	    return b1string.compareToIgnoreCase(b2string);
 	}
 
 	// function to pad numbers with leading zeroes to help sort them appropriately.  
 	// So this will turn a 2d6 into 0002d0006, and 10d6 into 0010d0006, so the 2d6
 	// will sort as lower.
 	private static final Pattern sortStringPattern = Pattern.compile("(\\d+)");
 	private static String modifySortString(String str){
 	    StringBuffer result = new StringBuffer();
 	    Matcher matcher = sortStringPattern.matcher(str);
 	    while ( matcher.find() ) {
 	      matcher.appendReplacement(result, paddingString(matcher.group(1), 4, '0', true));
 	    }
 	    matcher.appendTail(result);
 	    return result.toString();
 	}
 	
 	// function found at http://www.rgagnon.com/javadetails/java-0448.html
 	// to pad a string by inserting additional characters
 	public static String paddingString ( String s, int n, char c , boolean paddingLeft  ) {
 	    StringBuffer str = new StringBuffer(s);
 	    int strLength  = str.length();
 	    if ( n > 0 && n > strLength ) {
 	      for ( int i = 0; i <= n ; i ++ ) {
 	            if ( paddingLeft ) {
 	              if ( i < n - strLength ) str.insert( 0, c );
 	            }
 	            else {
 	              if ( i > strLength ) str.append( c );
 	            }
 	      	}
 	    }
 	    return str.toString();
 	}
 	
 	// Begin comparison customization
 	
 	private Boolean commonMacro = false;
 	private Boolean compareGroup = true;	
 	private Boolean compareSortPrefix = true;
 	private Boolean compareCommand = true;
 	private Boolean compareIncludeLabel = true;
 	private Boolean compareAutoExecute = true;
 	private Boolean compareApplyToSelectedTokens = true;
 	
 	public Boolean getCommonMacro() {
 		return commonMacro;
 	}
 	public void setCommonMacro(Boolean value) {
 		commonMacro = value;
 	}
 	public Boolean getCompareGroup() {
 		return compareGroup;
 	}
 	public void setCompareGroup(Boolean value) {
 		compareGroup = value;
 	}
 	public Boolean getCompareSortPrefix() {
 		return compareSortPrefix;
 	}
 	public void setCompareSortPrefix(Boolean value) {
 		compareSortPrefix = value;
 	}
 	public Boolean getCompareCommand() {
 		return compareCommand;
 	}
 	public void setCompareCommand(Boolean value) {
 		compareCommand = value;
 	}
 	public Boolean getCompareIncludeLabel() {
 		return compareIncludeLabel;
 	}
 	public void setCompareIncludeLabel(Boolean value) {
 		compareIncludeLabel = value;
 	}
 	public Boolean getCompareAutoExecute() {
 		return compareAutoExecute;
 	}
 	public void setCompareAutoExecute(Boolean value) {
 		compareAutoExecute = value;
 	}
 	public Boolean getCompareApplyToSelectedTokens() {
 		return compareApplyToSelectedTokens;
 	}
 	public void setCompareApplyToSelectedTokens(Boolean value) {
 		compareApplyToSelectedTokens = value;
 	}
 	
 	public static void fixOldMacroCompare(MacroButtonProperties oldMacro) {
 		if(oldMacro.getCommonMacro() == null) {
 			oldMacro.setCommonMacro(new Boolean(true));
 		}
 		if(oldMacro.getAllowPlayerEdits() == null) {
 			oldMacro.setAllowPlayerEdits(new Boolean(true));
 		}
 		if(oldMacro.getCompareApplyToSelectedTokens() == null) {
 			oldMacro.setCompareApplyToSelectedTokens(new Boolean(true));
 		}
 		if(oldMacro.getCompareAutoExecute() == null) {
 			oldMacro.setCompareAutoExecute(new Boolean(true));
 		}
 		if(oldMacro.getCompareCommand() == null) {
 			oldMacro.setCompareCommand(new Boolean(true));
 		}
 		if(oldMacro.getCompareGroup() == null) {
 			oldMacro.setCompareGroup(new Boolean(true));
 		}
 		if(oldMacro.getCompareIncludeLabel() == null) {
 			oldMacro.setCompareIncludeLabel(new Boolean(true));
 		}
 		if(oldMacro.getCompareSortPrefix() == null) {
 			oldMacro.setCompareSortPrefix(new Boolean(true));
 		}
 	}
 	
 	public static void fixOldMacroSetCompare(List<MacroButtonProperties> oldMacros) {
 		for(MacroButtonProperties nextMacro : oldMacros) {
 			fixOldMacroCompare(nextMacro);
 		}
 	}
 
 	public Object readResolve() {
 
 		if (commonMacro == null) commonMacro = false;
 		if (compareGroup == null) compareGroup = true;	
 		if (compareSortPrefix == null) compareSortPrefix = true;
 		if (compareCommand == null) compareCommand = true;
 		if (compareIncludeLabel == null) compareIncludeLabel = true;
 		if (compareAutoExecute == null) compareAutoExecute = true;
 		if (compareApplyToSelectedTokens == null) compareApplyToSelectedTokens = true;
		if (allowPlayerEdits == null) allowPlayerEdits = true;
		
 		return this;
 	}
 }
