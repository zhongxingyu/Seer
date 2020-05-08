 /*===========================================================================
   Copyright (C) 2009-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.html;
 
 import java.io.File;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.StartTagType;
 import net.htmlparser.jericho.Tag;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.encoder.HtmlEncoder;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderAccessType;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.filters.abstractmarkup.AbstractMarkupFilter;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration;
 import net.sf.okapi.filters.yaml.TaggedFilterConfiguration.RULE_TYPE;
 
 @UsingParameters(Parameters.class)
 public class HtmlFilter extends AbstractMarkupFilter {
 
 	private static final Logger LOGGER = Logger.getLogger(HtmlFilter.class.getName());
 
 	private Parameters parameters;
 	private HtmlEventBuilder eventBuilder;
 
 	public HtmlFilter() {
 		super(new HtmlEventBuilder());
 		eventBuilder = (HtmlEventBuilder) getEventBuilder();
 		setMimeType(MimeTypeMapper.HTML_MIME_TYPE);
 		setFilterWriter(createFilterWriter());
 		setParameters(new Parameters());
 		setName("okf_html"); //$NON-NLS-1$
 		setDisplayName("HTML/XHTML Filter"); //$NON-NLS-1$
 		addConfiguration(new FilterConfiguration(getName(), MimeTypeMapper.HTML_MIME_TYPE, getClass().getName(),
 				"HTML", "HTML or XHTML documents", //$NON-NLS-1$
 				Parameters.NONWELLFORMED_PARAMETERS));
 		addConfiguration(new FilterConfiguration(getName()+"-wellFormed",
 				MimeTypeMapper.XHTML_MIME_TYPE, getClass().getName(),
 				"HTML (Well-Formed)", "XHTML and well-formed HTML documents", //$NON-NLS-1$
 				Parameters.WELLFORMED_PARAMETERS));
 	}
 
 	/**
 	 * Initialize rule state and parser. Called before processing of each input.
 	 */
 	@Override
 	protected void startFilter() {
 		super.startFilter();
 		eventBuilder.setCollapseWhitespace(!isPreserveWhitespace() && getConfig().collapseWhitespace());
 		if (getConfig().collapseWhitespace()) {
 			LOGGER.log(Level.FINE,
 					"By default the HTML filter will collapse whitespace unless overridden in the configuration"); //$NON-NLS-1$
 		}
 		eventBuilder.initializeCodeFinder(getConfig().getUseCodeFinder(), getConfig().getCodeFinderRules());
 	}
 
 	@Override
 	protected void preProcess(Segment segment) {
 		super.preProcess(segment);
 		
 		// let the handlers deal with wellformed content
 		if (getConfig().isWellformed()) {
 			return;
 		}
 
 		// otherwise we can't assume a valid end tag and we must close any TextUnits when we see a non inline tag
 		if (segment instanceof Tag) {
 			// We just hit a tag that could close the current TextUnit
 			final Tag tag = (Tag) segment;
 			boolean inlineTag = false;
 			if (getConfig().getMainRuleType(tag.getName()) == RULE_TYPE.INLINE_ELEMENT
 					|| (getEventBuilder().isInsideTextRun() && (tag.getTagType() == StartTagType.COMMENT || tag
 							.getTagType() == StartTagType.XML_PROCESSING_INSTRUCTION)))
 				inlineTag = true;
 
 			// if its an inline code let the handlers deal with it
 			if (getEventBuilder().isCurrentTextUnit() && !inlineTag) {
 				getEventBuilder().endTextUnit();
 			}
 		}
 	}
 
 	@Override
 	protected void handleStartTag(StartTag startTag) {
 		super.handleStartTag(startTag);
 		eventBuilder.setCollapseWhitespace(!isPreserveWhitespace() && getConfig().collapseWhitespace());
 	}
 	
 	@Override
 	protected void handleEndTag(EndTag endTag) {
 		super.handleEndTag(endTag);
 		eventBuilder.setCollapseWhitespace(!isPreserveWhitespace() && getConfig().collapseWhitespace());
 	}
 
 	@Override
 	protected PropertyTextUnitPlaceholder createPropertyTextUnitPlaceholder(PlaceholderAccessType type, String name,
 			String value, Tag tag, Attribute attribute) {
 
 		String normalizeAttributeName = normalizeAttributeName(name, value, tag);
 
 		// Test for charset in meta tag - we need to isolate the position of
 		// charset within the attribute value
 		// i.e., content="text/html; charset=ISO-2022-JP"
 		if (isMetaCharset(name, value, tag) && value.toLowerCase().indexOf("charset=") != -1) { //$NON-NLS-1$
 			// offset of attribute
 			int mainStartPos = attribute.getBegin() - tag.getBegin();
 			int mainEndPos = attribute.getEnd() - tag.getBegin();
 
 			// adjust offset of value of the attribute
 			int charsetValueOffset = value.toLowerCase().lastIndexOf("charset=") + "charset=".length(); //$NON-NLS-1$
 
 			int valueStartPos = (attribute.getValueSegment().getBegin() + charsetValueOffset) - tag.getBegin();
 			int valueEndPos = attribute.getValueSegment().getEnd() - tag.getBegin();
 
 			// get the charset value (encoding)
 			String v = tag.toString().substring(valueStartPos, valueEndPos);
 			return new PropertyTextUnitPlaceholder(type, normalizeAttributeName, v, mainStartPos, mainEndPos,
 					valueStartPos, valueEndPos);
 		}
 
 		// name is normalized in super-class
 		return super.createPropertyTextUnitPlaceholder(type, name, eventBuilder.normalizeHtmlText(value, true,
 				!isPreserveWhitespace() && getConfig().collapseWhitespace()), tag, attribute);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.okapi.common.markupfilter.BaseMarkupFilter#normalizeName(java. lang.String)
 	 */
 	@Override
 	protected String normalizeAttributeName(String attrName, String attrValue, Tag tag) {
 		// normalize values for HTML
 		String normalizedName = attrName;
 
 		// <meta http-equiv="Content-Type"
 		// content="text/html; charset=ISO-2022-JP">
 		if (isMetaCharset(attrName, attrValue, tag)) {
 			normalizedName = Property.ENCODING;
 			return normalizedName;
 		}
 
 		// <meta http-equiv="Content-Language" content="en"
 		if (tag.getName().equalsIgnoreCase("meta") && attrName.equalsIgnoreCase(HtmlEncoder.CONTENT)) {
 			StartTag st = (StartTag) tag;
 			if (st.getAttributeValue("http-equiv") != null) {
 				if (st.getAttributeValue("http-equiv").equalsIgnoreCase("Content-Language")) {
 					normalizedName = Property.LANGUAGE;
 					return normalizedName;
 				}
 			}
 		}
 
 		// <x lang="en"> or <x xml:lang="en">
 		if (attrName.equalsIgnoreCase("lang") || attrName.equalsIgnoreCase("xml:lang")) {
 			normalizedName = Property.LANGUAGE;
 		}
 
 		return normalizedName;
 	}
 
 	private boolean isMetaCharset(String attrName, String attrValue, Tag tag) {
 		if (tag.getName().equalsIgnoreCase("meta") && attrName.equalsIgnoreCase(HtmlEncoder.CONTENT)) {
 			StartTag st = (StartTag) tag;
 			if (st.getAttributeValue("http-equiv") != null && st.getAttributeValue("content") != null) {
 				if (st.getAttributeValue("http-equiv").equalsIgnoreCase("Content-Type")
 						&& st.getAttributeValue("content").toLowerCase().contains("charset=")) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected TaggedFilterConfiguration getConfig() {
 		return parameters.getTaggedConfig();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.okapi.common.filters.IFilter#setParameters(net.sf.okapi.common .IParameters)
 	 */
 	public void setParameters(IParameters params) {
 		this.parameters = (Parameters) params;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see net.sf.okapi.common.filters.IFilter#getParameters()
 	 */
 	public IParameters getParameters() {
 		return parameters;
 	}
 
 	/**
 	 * Initialize filter parameters from a URL.
 	 * 
 	 * @param config
 	 */
 	public void setParametersFromURL(URL config) {
 		parameters = new Parameters(config);
 	}
 
 	/**
 	 * Initialize filter parameters from a Java File.
 	 * 
 	 * @param config
 	 */
 	public void setParametersFromFile(File config) {
 		parameters = new Parameters(config);
 	}
 
 	/**
 	 * Initialize filter parameters from a String.
 	 * 
 	 * @param config
 	 */
 	public void setParametersFromString(String config) {
 		parameters = new Parameters(config);
 	}
 }
