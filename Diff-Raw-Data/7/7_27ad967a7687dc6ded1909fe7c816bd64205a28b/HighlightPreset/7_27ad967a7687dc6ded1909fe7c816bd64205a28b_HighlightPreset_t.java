 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.stormfront.settings.server;
 
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import cc.warlock.core.client.settings.IClientSettingProvider;
 import cc.warlock.core.client.settings.IHighlightString;
 import cc.warlock.core.stormfront.xml.StormFrontElement;
 
 @Deprecated
 public class HighlightPreset extends Preset implements IHighlightString {
 
 	public static final String KEY_TEXT = "text";
 	
 	public static final String STRINGS_PREFIX = "<strings>";
 	public static final String STRINGS_SUFFIX = "</strings>";
 	public static final String NAMES_PREFIX = "<names>";
 	public static final String NAMES_SUFFIX = "</names>";
 	
 	protected String text;
 	protected boolean isName, isNew = false;
 	protected HighlightPreset originalString;
 	protected Pattern pattern;
 	protected int index;
 	protected String sound;
 	
 	protected HighlightPreset (ServerSettings serverSettings, Palette palette, int index)
 	{
 		super(serverSettings, palette);
 		
 		this.index = index;
 	}
 	
 	public HighlightPreset (HighlightPreset other)
 	{
 		super(other);
 		
 		this.text = other.text == null ? null : new String(other.text);
 		this.isName = other.isName;
 		this.isNew = other.isNew;
 		this.originalString = other;
 		this.index = other.index;
 		this.sound = other.sound;
 		
 	}
 	
 	public HighlightPreset (ServerSettings serverSettings, StormFrontElement highlightElement, Palette palette)
 	{
 		super(serverSettings, highlightElement, palette);
 		
 		this.text = highlightElement.attributeValue("text");
		this.sound = highlightElement.attributeValue("sound");
 	}
 	
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		if (!text.equals(this.text))
 			needsUpdate = true;
 		
 		this.text = text;
 		this.pattern = null;
 	}
 	
 	public boolean isName() {
 		return isName;
 	}
 
 	public void setIsName(boolean isName) {
 		this.isName = isName;
 	}
 
 	protected void saveToDOM ()
 	{
 		setAttribute(KEY_FGCOLOR, foregroundColor);
 		setAttribute(KEY_BGCOLOR, backgroundColor == null ? "" : backgroundColor);
 		setAttribute(KEY_TEXT, getText());
 		
 		if (fillEntireLine)
 			setAttribute(KEY_FILL_ENTIRE_LINE, "y");
 	}
 	
 	protected String toStormfrontMarkup ()
 	{
 		return "<h text=\"" + getText() + "\" " +
 			KEY_FGCOLOR + "=\"" + foregroundColor + "\" " +
 			KEY_BGCOLOR + "=\"" + (backgroundColor == null ? "" : backgroundColor) + "\"" +
 			(fillEntireLine ? (" " + KEY_FILL_ENTIRE_LINE + "=\"y\"/>") : "/>");
 	}
 	
 	protected String toStormfrontAddMarkup ()
 	{
 		String aPrefix = (isName ? NAMES_PREFIX : STRINGS_PREFIX) + ADD_PREFIX;
 		String aSuffix = ADD_SUFFIX + (isName ? NAMES_SUFFIX : STRINGS_SUFFIX);	
 		String mPrefix = (isName ? NAMES_PREFIX : STRINGS_PREFIX) + UPDATE_PREFIX;
 		String mSuffix = UPDATE_SUFFIX + (isName ? NAMES_SUFFIX : STRINGS_SUFFIX);
 		
 		String emptyTag = "<h";
 		String justTextTag = emptyTag + " text=\"" + getText() + "\"";
 		String textAndColorTag = justTextTag + " " + KEY_FGCOLOR + "=\"" + foregroundColor + "\"";
 		String textAndColorsTag = textAndColorTag + " " + KEY_BGCOLOR + "=\"" + (backgroundColor == null ? "" : backgroundColor) + "\"";
 		String allAttribsTag = textAndColorsTag + (fillEntireLine ? (" " + KEY_FILL_ENTIRE_LINE + "=\"y\"/>") : "/>");
 		
 		emptyTag += "/>"; justTextTag += "/>"; textAndColorTag += "/>"; textAndColorsTag += "/>";
 		
 		return 
 			aPrefix + emptyTag + aSuffix +
 			mPrefix + emptyTag + justTextTag + mSuffix +
 			mPrefix + justTextTag + textAndColorTag + mSuffix +
 			mPrefix + textAndColorTag + textAndColorsTag + mSuffix +
 			mPrefix + textAndColorsTag + allAttribsTag + mSuffix;
 	}
 	
 	public static HighlightPreset createHighlightStringFromParent (ServerSettings serverSettings, StormFrontElement parent)
 	{
 		StormFrontElement element = new StormFrontElement("h");
 		parent.addElement(element);
 		
 		HighlightPreset string = new HighlightPreset(serverSettings, element, serverSettings.getPalette());
 		string.needsUpdate = true;
 		return string;
 	}
 	
 	@Override
 	public int compareTo(ColorSetting o) {
 		if (o instanceof HighlightPreset)
 		{
 			HighlightPreset other = (HighlightPreset) o;
 			return text.compareTo(other.text);
 		}
 		else return -1;
 	}
 	
 	@Override
 	public void setNeedsUpdate(boolean needsUpdate) {
 		super.setNeedsUpdate(needsUpdate);
 		if (!needsUpdate)
 			this.originalString = null;
 	}
 	
 	public HighlightPreset getOriginalHighlightString ()
 	{
 		return originalString;
 	}
 	
 	@Override
 	public String toString() {
 		return (isName ? "name: " : "string: ") + text;
 	}
 
 	public boolean isNew() {
 		return isNew;
 	}
 
 	public void setNew(boolean isNew) {
 		this.isNew = isNew;
 	}
 	
 	public Pattern getPattern() {
 		if(pattern == null) {
 			if(text == null)
 				return null;
 			// TODO test if we should compile this as a regex
 			try {
 				pattern = Pattern.compile(text, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
 			} catch(PatternSyntaxException e) {
 				System.err.println("Pattern error: " + e.getMessage());
 			}
 		}
 		
 		return pattern;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof HighlightPreset)
 		{
 			HighlightPreset other =(HighlightPreset) obj;
 			if (other.getText().equals(getText()) && other.getIndex() == getIndex())
 			{
 				return true;
 			}
 		}
 		return super.equals(obj);
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public void setIndex(int index) {
 		this.index = index;
 	}
 	
 	public String surroundMarkup(String childMarkup)
 	{
 		String tagPrefix = isName ? HighlightPreset.NAMES_PREFIX : HighlightPreset.STRINGS_PREFIX;
 		String tagSuffix = isName ? HighlightPreset.NAMES_SUFFIX : HighlightPreset.STRINGS_SUFFIX;
 		
 		return tagPrefix + childMarkup + tagSuffix;
 	}
 	
 	public boolean isCaseSensitive() {
 		return false;
 	}
 	
 	public void setCaseSensitive(boolean caseSensitive) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public boolean isLiteral() {
 		return true;
 	}
 	
 	public void setLiteral(boolean literal) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public boolean isFullWordMatch() {
 		return true;
 	}
 	
 	public void setFullWordMatch(boolean fullWordMatch) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setPattern(Pattern pattern) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public String getFullWordPattern() {
 		return pattern.pattern();
 	}
 	
 	public IClientSettingProvider getProvider() {
 		return null;
 	}
 	
 	public String getSound(){
 		return sound;
 	}
 	
 	public void setSound(String sound){
 		this.sound = sound;
 	}
 }
