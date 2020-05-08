 /*
  * Copyright (C) 2012 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.server.libglom;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang3.StringUtils;
 import org.glom.web.shared.libglom.Field;
 import org.glom.web.shared.libglom.NumericFormat;
 import org.glom.web.shared.libglom.Relationship;
 import org.glom.web.shared.libglom.Report;
 import org.glom.web.shared.libglom.Translatable;
 import org.glom.web.shared.libglom.layout.Formatting;
 import org.glom.web.shared.libglom.layout.LayoutGroup;
 import org.glom.web.shared.libglom.layout.LayoutItem;
 import org.glom.web.shared.libglom.layout.LayoutItemField;
 import org.glom.web.shared.libglom.layout.LayoutItemNotebook;
 import org.glom.web.shared.libglom.layout.LayoutItemPortal;
 import org.glom.web.shared.libglom.layout.LayoutItemPortal.NavigationType;
 import org.glom.web.shared.libglom.layout.UsesRelationship;
 import org.glom.web.shared.libglom.layout.UsesRelationshipImpl;
 import org.glom.web.shared.libglom.layout.reportparts.LayoutItemGroupBy;
 import org.jfree.util.Log;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * @author Murray Cumming <murrayc@openismus.com>
  * 
  */
 public class Document {
 
 	@SuppressWarnings("serial")
 	private static class TableInfo extends Translatable {
 		public boolean isDefault;
 		public boolean isHidden;
 
 		private final Hashtable<String, Field> fieldsMap = new Hashtable<String, Field>();
 		private final Hashtable<String, Relationship> relationshipsMap = new Hashtable<String, Relationship>();
 		private final Hashtable<String, Report> reportsMap = new Hashtable<String, Report>();
 
 		private List<LayoutGroup> layoutGroupsList = new ArrayList<LayoutGroup>();
 		private List<LayoutGroup> layoutGroupsDetails = new ArrayList<LayoutGroup>();
 	}
 
 	private String fileURI = "";
 	private org.w3c.dom.Document xmlDocument = null;
 
 	private Translatable databaseTitle = new Translatable();
 	private List<String> translationAvailableLocales = new ArrayList<String>(); // TODO
 	private String connectionServer = "";
 	private String connectionDatabase = "";
 	private int connectionPort = 0;
 	private final Hashtable<String, TableInfo> tablesMap = new Hashtable<String, TableInfo>();
 
 	private static final String NODE_CONNECTION = "connection";
 	private static final String ATTRIBUTE_CONNECTION_SERVER = "server";
 	private static final String ATTRIBUTE_CONNECTION_DATABASE = "database";
 	private static final String ATTRIBUTE_CONNECTION_PORT = "port";
 	private static final String NODE_TABLE = "table";
 	private static final String ATTRIBUTE_NAME = "name";
 	private static final String ATTRIBUTE_TITLE = "title";
 	private static final String ATTRIBUTE_DEFAULT = "default";
 	private static final String ATTRIBUTE_HIDDEN = "hidden";
 	private static final String NODE_TRANSLATIONS_SET = "trans_set";
 	private static final String NODE_TRANSLATIONS = "trans";
 	private static final String ATTRIBUTE_TRANSLATION_LOCALE = "loc";
 	private static final String ATTRIBUTE_TRANSLATION_TITLE = "val";
 	private static final String NODE_REPORTS = "reports";
 	private static final String NODE_REPORT = "report";
 	private static final String NODE_FIELDS = "fields";
 	private static final String NODE_FIELD = "field";
 	private static final String ATTRIBUTE_PRIMARY_KEY = "primary_key";
 	private static final String ATTRIBUTE_FIELD_TYPE = "type";
 	private static final String NODE_FORMATTING = "formatting";
 	//private static final String ATTRIBUTE_TEXT_FORMAT_MULTILINE = "format_text_multiline";
 	private static final String ATTRIBUTE_USE_THOUSANDS_SEPARATOR = "format_thousands_separator";
 	private static final String ATTRIBUTE_DECIMAL_PLACES = "format_decimal_places";
 	private static final String NODE_RELATIONSHIPS = "relationships";
 	private static final String NODE_RELATIONSHIP = "relationship";
 	private static final String ATTRIBUTE_RELATIONSHIP_FROM_FIELD = "key";
 	private static final String ATTRIBUTE_RELATIONSHIP_TO_TABLE = "other_table";
 	private static final String ATTRIBUTE_RELATIONSHIP_TO_FIELD = "other_key";
 	private static final String NODE_DATA_LAYOUTS = "data_layouts";
 	private static final String NODE_DATA_LAYOUT = "data_layout";
 	private static final String NODE_DATA_LAYOUT_GROUPS = "data_layout_groups";
 	private static final String NODE_DATA_LAYOUT_GROUP = "data_layout_group";
 	private static final String ATTRIBUTE_LAYOUT_GROUP_COLUMNS_COUNT = "columns_count";
 	private static final String NODE_DATA_LAYOUT_NOTEBOOK = "data_layout_notebook";
 	private static final String NODE_DATA_LAYOUT_PORTAL = "data_layout_portal";
 	private static final String NODE_DATA_LAYOUT_PORTAL_NAVIGATIONRELATIONSHIP = "portal_navigation_relationship";
 	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE = "navigation_type";
 	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE_AUTOMATIC = "automatic";
 	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE_SPECIFIC = "specific";
 	private static final String ATTRIBUTE_PORTAL_NAVIGATION_TYPE_NONE = "none";
 	private static final String ATTRIBUTE_RELATIONSHIP_NAME = "relationship";
 	private static final String ATTRIBUTE_RELATED_RELATIONSHIP_NAME = "related_relationship";
 	private static final String NODE_DATA_LAYOUT_ITEM = "data_layout_item";
 	private static final String NODE_DATA_LAYOUT_ITEM_GROUPBY = "data_layout_item_groupby";
 	private static final String NODE_GROUPBY = "groupby";
 	private static final String NODE_SECONDARY_FIELDS = "secondary_fields";
 	private static final String ATTRIBUTE_USE_DEFAULT_FORMATTING = "use_default_formatting";
 	private static final String LAYOUT_NAME_DETAILS = "details";
 	private static final String LAYOUT_NAME_LIST = "list";
 
 	public void setFileURI(final String fileURI) {
 		this.fileURI = fileURI;
 	}
 
 	public String getFileURI() {
 		return fileURI;
 	}
 
 	// TODO: Make sure these have the correct values.
 	public enum LoadFailureCodes {
 		LOAD_FAILURE_CODE_NONE, LOAD_FAILURE_CODE_NOT_FOUND, LOAD_FAILURE_CODE_FILE_VERSION_TOO_NEW
 	};
 
	public boolean load(final int failure_code) {
 		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder documentBuilder;
 		try {
 			documentBuilder = dbf.newDocumentBuilder();
 		} catch (final ParserConfigurationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 
 		try {
 			xmlDocument = documentBuilder.parse(fileURI);
 		} catch (final SAXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (final IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 
 		final Element rootNode = xmlDocument.getDocumentElement();
 		if (rootNode.getNodeName() != "glom_document") {
 			Log.error("Unexpected XML root node name found: " + rootNode.getNodeName());
 			return false;
 		}
 
 		databaseTitle.setTitleOriginal(rootNode.getAttribute(ATTRIBUTE_TITLE));
 
 		// We first load the fields, relationships, etc,
 		// for all tables:
 		final List<Node> listTableNodes = getChildrenByTagName(rootNode, NODE_TABLE);
 		for (final Node node : listTableNodes) {
 			if (!(node instanceof Element))
 				continue;
 
 			final Element element = (Element) node;
 			final TableInfo info = loadTableNodeBasic(element);
 			tablesMap.put(info.getName(), info);
 		}
 
 		// We then load the layouts for all tables, because they
 		// need the fields and relationships for all tables:
 		for (final Node node : listTableNodes) {
 			if (!(node instanceof Element))
 				continue;
 
 			final Element element = (Element) node;
 			final String tableName = element.getAttribute(ATTRIBUTE_NAME);
 
 			// We first load the fields, relationships, etc:
 			final TableInfo info = getTableInfo(tableName);
 			if (info == null) {
 				continue;
 			}
 
 			// We then load the layouts afterwards, because they
 			// need the fields and relationships:
 			loadTableLayouts(element, info);
 
 			tablesMap.put(info.getName(), info);
 		}
 
 		final Element nodeConnection = getElementByName(rootNode, NODE_CONNECTION);
 		if (nodeConnection != null) {
 			connectionServer = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_SERVER);
 			connectionDatabase = nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_DATABASE);
 			connectionPort = getAttributeAsDecimal(nodeConnection,
 					nodeConnection.getAttribute(ATTRIBUTE_CONNECTION_PORT));
 		}
 
 		return true;
 	};
 
 	private Element getElementByName(final Element parentElement, final String tagName) {
 		final List<Node> listNodes = getChildrenByTagName(parentElement, tagName);
 		if (listNodes == null)
 			return null;
 
 		if (listNodes.size() == 0)
 			return null;
 
 		return (Element) listNodes.get(0);
 	}
 
 	private boolean getAttributeAsBoolean(final Element node, final String attributeName) {
 		final String str = node.getAttribute(attributeName);
 		if (str == null)
 			return false;
 
 		return str.equals("true");
 	}
 
 	/**
 	 * @param elementFormatting
 	 * @param aTTRIBUTE_DECIMAL_PLACES2
 	 * @return
 	 */
 	private int getAttributeAsDecimal(final Element node, final String attributeName) {
 		final String str = node.getAttribute(attributeName);
 		if (StringUtils.isEmpty(str))
 			return 0;
 
 		return Integer.valueOf(str);
 	}
 
 	/**
 	 * Load a title and its translations.
 	 * 
 	 * @param node
 	 *            The XML Element that may contain a title attribute and a trans_set of translations of the title.
 	 * @param title
 	 */
 	private void loadTitle(final Element node, final Translatable title) {
 		title.setName(node.getAttribute(ATTRIBUTE_NAME));
 
 		title.setTitleOriginal(node.getAttribute(ATTRIBUTE_TITLE));
 
 		final Element nodeSet = getElementByName(node, NODE_TRANSLATIONS_SET);
 		if (nodeSet == null) {
 			return;
 		}
 
 		final List<Node> listNodes = getChildrenByTagName(nodeSet, NODE_TRANSLATIONS);
 		if (listNodes == null)
 			return;
 
 		for (final Node transNode : listNodes) {
 			if (!(transNode instanceof Element)) {
 				continue;
 			}
 
 			final Element element = (Element) transNode;
 
 			final String locale = element.getAttribute(ATTRIBUTE_TRANSLATION_LOCALE);
 			final String translatedTitle = element.getAttribute(ATTRIBUTE_TRANSLATION_TITLE);
 			if (!StringUtils.isEmpty(locale) && !StringUtils.isEmpty(translatedTitle)) {
 				title.setTitle(translatedTitle, locale);
 			}
 		}
 	}
 
 	/**
 	 * @param tableNode
 	 * @return
 	 */
 	private TableInfo loadTableNodeBasic(final Element tableNode) {
 		final TableInfo info = new TableInfo();
 		loadTitle(tableNode, info);
 		final String tableName = info.getName();
 
 		info.isDefault = getAttributeAsBoolean(tableNode, ATTRIBUTE_DEFAULT);
 		info.isHidden = getAttributeAsBoolean(tableNode, ATTRIBUTE_HIDDEN);
 
 		// These should be loaded before the fields, because the fields use them.
 		final Element relationshipsNode = getElementByName(tableNode, NODE_RELATIONSHIPS);
 		if (relationshipsNode != null) {
 			final List<Node> listNodes = getChildrenByTagName(relationshipsNode, NODE_RELATIONSHIP);
 			for (final Node node : listNodes) {
 				if (!(node instanceof Element)) {
 					continue;
 				}
 
 				final Element element = (Element) node;
 				final Relationship relationship = new Relationship();
 				loadTitle(element, relationship);
 				relationship.setFromTable(tableName);
 				relationship.setFromField(element.getAttribute(ATTRIBUTE_RELATIONSHIP_FROM_FIELD));
 				relationship.setToTable(element.getAttribute(ATTRIBUTE_RELATIONSHIP_TO_TABLE));
 				relationship.setToField(element.getAttribute(ATTRIBUTE_RELATIONSHIP_TO_FIELD));
 
 				info.relationshipsMap.put(relationship.getName(), relationship);
 			}
 		}
 
 		final Element fieldsNode = getElementByName(tableNode, NODE_FIELDS);
 		if (fieldsNode != null) {
 			final List<Node> listNodes = getChildrenByTagName(fieldsNode, NODE_FIELD);
 			for (final Node node : listNodes) {
 				if (!(node instanceof Element)) {
 					continue;
 				}
 
 				final Element element = (Element) node;
 				final Field field = new Field();
 				loadField(element, field);
 
 				info.fieldsMap.put(field.getName(), field);
 			}
 		}
 
 		return info;
 	}
 
 	/**
 	 * @param tableNode
 	 * @param info
 	 */
 	private void loadTableLayouts(final Element tableNode, final TableInfo info) {
 		final String tableName = info.getName();
 
 		final Element layoutsNode = getElementByName(tableNode, NODE_DATA_LAYOUTS);
 		if (layoutsNode != null) {
 			final List<Node> listNodes = getChildrenByTagName(layoutsNode, NODE_DATA_LAYOUT);
 			for (final Node node : listNodes) {
 				if (!(node instanceof Element)) {
 					continue;
 				}
 
 				final Element element = (Element) node;
 				final String name = element.getAttribute("name");
 				final List<LayoutGroup> listLayoutGroups = loadLayoutNode(element, tableName);
 				if (name.equals(LAYOUT_NAME_DETAILS)) {
 					info.layoutGroupsDetails = listLayoutGroups;
 				} else if (name.equals(LAYOUT_NAME_LIST)) {
 					info.layoutGroupsList = listLayoutGroups;
 				} else {
 					Log.error("loadTableNode(): unexpected layout name: " + name);
 				}
 			}
 		}
 
 		final Element reportsNode = getElementByName(tableNode, NODE_REPORTS);
 		if (reportsNode != null) {
 			final List<Node> listNodes = getChildrenByTagName(reportsNode, NODE_REPORT);
 			for (final Node node : listNodes) {
 				if (!(node instanceof Element)) {
 					continue;
 				}
 
 				final Element element = (Element) node;
 				final Report report = new Report();
 				loadReport(element, report, tableName);
 
 				info.reportsMap.put(report.getName(), report);
 			}
 		}
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	private List<LayoutGroup> loadLayoutNode(final Element node, final String tableName) {
 		if (node == null) {
 			return null;
 		}
 
 		final List<LayoutGroup> result = new ArrayList<LayoutGroup>();
 
 		final List<Node> listNodes = getChildrenByTagName(node, NODE_DATA_LAYOUT_GROUPS);
 		for (final Node nodeGroups : listNodes) {
 			if (!(nodeGroups instanceof Element)) {
 				continue;
 			}
 
 			final Element elementGroups = (Element) nodeGroups;
 
 			final NodeList list = elementGroups.getChildNodes();
 			final int num = list.getLength();
 			for (int i = 0; i < num; i++) {
 				final Node nodeLayoutGroup = list.item(i);
 				if (nodeLayoutGroup == null) {
 					continue;
 				}
 
 				if (!(nodeLayoutGroup instanceof Element)) {
 					continue;
 				}
 
 				final Element element = (Element) nodeLayoutGroup;
 				final String tagName = element.getTagName();
 				if (tagName == NODE_DATA_LAYOUT_GROUP) {
 					final LayoutGroup group = new LayoutGroup();
 					loadDataLayoutGroup(element, group, tableName);
 					result.add(group);
 				} else if (tagName == NODE_DATA_LAYOUT_NOTEBOOK) {
 					final LayoutItemNotebook group = new LayoutItemNotebook();
 					loadDataLayoutGroup(element, group, tableName);
 					result.add(group);
 				} else if (tagName == NODE_DATA_LAYOUT_PORTAL) {
 					final LayoutItemPortal portal = new LayoutItemPortal();
 					loadDataLayoutPortal(element, portal, tableName);
 					result.add(portal);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param element
 	 * @param tableName
 	 * @param portal
 	 */
 	private void loadUsesRelationship(final Element element, final String tableName, final UsesRelationship item) {
 		if (element == null) {
 			return;
 		}
 
 		if (item == null) {
 			return;
 		}
 
 		final String relationshipName = element.getAttribute(ATTRIBUTE_RELATIONSHIP_NAME);
 		Relationship relationship = null;
 		if (!StringUtils.isEmpty(relationshipName)) {
 			// std::cout << "  debug in : tableName=" << tableName << ", relationshipName=" << relationship_name <<
 			// std::endl;
 			relationship = getRelationship(tableName, relationshipName);
 			item.setRelationship(relationship);
 
 			if (relationship == null) {
 				Log.error("relationship not found: " + relationshipName + ", in table: " + tableName);
 			}
 		}
 
 		final String relatedRelationshipName = element.getAttribute(ATTRIBUTE_RELATED_RELATIONSHIP_NAME);
 		if (!StringUtils.isEmpty(relatedRelationshipName) && (relationship != null)) {
 			final Relationship relatedRelationship = getRelationship(relationship.getToTable(), relatedRelationshipName);
 			if (relatedRelationship == null) {
 				Log.error("related relationship not found in table=" + relationship.getToTable() + ",  name="
 						+ relatedRelationshipName);
 
 				item.setRelatedRelationship(relatedRelationship);
 			}
 		}
 	}
 
 	/**
 	 * getElementsByTagName() is recursive, but we do not want that.
 	 * 
 	 * @param node
 	 * @param
 	 * @return
 	 */
 	private List<Node> getChildrenByTagName(final Element parentNode, final String tagName) {
 		final List<Node> result = new ArrayList<Node>();
 
 		final NodeList list = parentNode.getElementsByTagName(tagName);
 		final int num = list.getLength();
 		for (int i = 0; i < num; i++) {
 			final Node node = list.item(i);
 			if (node == null) {
 				continue;
 			}
 
 			final Node itemParentNode = node.getParentNode();
 			if (itemParentNode.equals(parentNode)) {
 				result.add(node);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param element
 	 * @param group
 	 */
 	private void loadDataLayoutGroup(final Element nodeGroup, final LayoutGroup group, final String tableName) {
 		loadTitle(nodeGroup, group);
 		
 		group.setColumnCount( getAttributeAsDecimal(nodeGroup, ATTRIBUTE_LAYOUT_GROUP_COLUMNS_COUNT));
 
 		final NodeList listNodes = nodeGroup.getChildNodes();
 		final int num = listNodes.getLength();
 		for (int i = 0; i < num; i++) {
 			final Node node = listNodes.item(i);
 			if (!(node instanceof Element))
 				continue;
 
 			final Element element = (Element) node;
 			final String tagName = element.getTagName();
 			if (tagName == NODE_DATA_LAYOUT_GROUP) {
 				final LayoutGroup childGroup = new LayoutGroup();
 				loadDataLayoutGroup(element, childGroup, tableName);
 				group.addItem(childGroup);
 			} else if (tagName == NODE_DATA_LAYOUT_NOTEBOOK) {
 				final LayoutItemNotebook childGroup = new LayoutItemNotebook();
 				loadDataLayoutGroup(element, childGroup, tableName);
 				group.addItem(childGroup);
 			} else if (tagName == NODE_DATA_LAYOUT_PORTAL) {
 				final LayoutItemPortal childGroup = new LayoutItemPortal();
 				loadDataLayoutPortal(element, childGroup, tableName);
 				group.addItem(childGroup);
 			} else if (element.getTagName() == NODE_DATA_LAYOUT_ITEM) {
 				final LayoutItemField item = new LayoutItemField();
 				loadDataLayoutItemField(element, item, tableName);
 				group.addItem(item);
 			} else if (element.getTagName() == NODE_DATA_LAYOUT_ITEM_GROUPBY) {
 				final LayoutItemGroupBy item = new LayoutItemGroupBy();
 				loadDataLayoutItemGroupBy(element, item, tableName);
 				group.addItem(item);
 			}
 		}
 	}
 
 	/**
 	 * @param element
 	 * @param item
 	 * @param tableName
 	 */
 	private void loadDataLayoutItemGroupBy(final Element element, final LayoutItemGroupBy item, final String tableName) {
 		loadDataLayoutGroup(element, item, tableName);
 
 		final Element elementGroupBy = getElementByName(element, NODE_GROUPBY);
 		if (elementGroupBy == null) {
 			return;
 		}
 
 		final LayoutItemField fieldGroupBy = new LayoutItemField();
 		loadDataLayoutItemField(elementGroupBy, fieldGroupBy, tableName);
 		item.setFieldGroupBy(fieldGroupBy);
 
 		final Element elementSecondaryFields = getElementByName(element, NODE_SECONDARY_FIELDS);
 		if (elementSecondaryFields == null) {
 			return;
 		}
 
 		final Element elementLayoutGroup = getElementByName(elementSecondaryFields, NODE_DATA_LAYOUT_GROUP);
 		if (elementLayoutGroup != null) {
 			final LayoutGroup secondaryLayoutGroup = new LayoutGroup();
 			loadDataLayoutGroup(elementLayoutGroup, secondaryLayoutGroup, tableName);
 			item.setSecondaryFields(secondaryLayoutGroup);
 		}
 	}
 
 	/**
 	 * @param element
 	 * @param item
 	 */
 	private void loadDataLayoutItemField(final Element element, final LayoutItemField item, final String tableName) {
 		loadTitle(element, item);
 		loadUsesRelationship(element, tableName, item);
 
 		// Get the actual field:
 		final String fieldName = item.getName();
 		final String inTableName = item.getTableUsed(tableName);
 		final Field field = getField(inTableName, fieldName);
 		item.setFullFieldDetails(field);
 
 		item.setUseDefaultFormatting(getAttributeAsBoolean(element, ATTRIBUTE_USE_DEFAULT_FORMATTING));
 
 		final Element elementFormatting = getElementByName(element, NODE_FORMATTING);
 		if (elementFormatting != null) {
 			loadFormatting(elementFormatting, item.getFormatting());
 		}
 	}
 
 	/**
 	 * @param element
 	 * @param childGroup
 	 */
 	private void loadDataLayoutPortal(final Element element, final LayoutItemPortal portal, final String tableName) {
 		loadUsesRelationship(element, tableName, portal);
 		final String relatedTableName = portal.getTableUsed(tableName);
 		loadDataLayoutGroup(element, portal, relatedTableName);
 
 		final Element elementNavigation = getElementByName(element, NODE_DATA_LAYOUT_PORTAL_NAVIGATIONRELATIONSHIP);
 		if (elementNavigation != null) {
 			final String navigationTypeAsString = elementNavigation.getAttribute(ATTRIBUTE_PORTAL_NAVIGATION_TYPE);
 			if (StringUtils.isEmpty(navigationTypeAsString)
 					|| navigationTypeAsString == ATTRIBUTE_PORTAL_NAVIGATION_TYPE_AUTOMATIC) {
 				portal.setNavigationType(LayoutItemPortal.NavigationType.NAVIGATION_AUTOMATIC);
 			} else if (navigationTypeAsString == ATTRIBUTE_PORTAL_NAVIGATION_TYPE_NONE) {
 				portal.setNavigationType(LayoutItemPortal.NavigationType.NAVIGATION_NONE);
 			} else if (navigationTypeAsString == ATTRIBUTE_PORTAL_NAVIGATION_TYPE_SPECIFIC) {
 				// Read the specified relationship name:
 				final UsesRelationship relationshipNavigationSpecific = new UsesRelationshipImpl();
 				loadUsesRelationship(elementNavigation, relatedTableName, relationshipNavigationSpecific);
 				portal.setNavigationRelationshipSpecific(relationshipNavigationSpecific);
 			}
 		}
 
 	}
 
 	/**
 	 * @param element
 	 * @param field
 	 */
 	private void loadField(final Element element, final Field field) {
 		loadTitle(element, field);
 
 		Field.GlomFieldType fieldType = Field.GlomFieldType.TYPE_INVALID;
 		final String fieldTypeStr = element.getAttribute(ATTRIBUTE_FIELD_TYPE);
 		if (!StringUtils.isEmpty(fieldTypeStr)) {
 			if (fieldTypeStr.equals("Boolean")) {
 				fieldType = Field.GlomFieldType.TYPE_BOOLEAN;
 			} else if (fieldTypeStr.equals("Date")) {
 				fieldType = Field.GlomFieldType.TYPE_DATE;
 			} else if (fieldTypeStr.equals("Image")) {
 				fieldType = Field.GlomFieldType.TYPE_IMAGE;
 			} else if (fieldTypeStr.equals("Number")) {
 				fieldType = Field.GlomFieldType.TYPE_NUMERIC;
 			} else if (fieldTypeStr.equals("Text")) {
 				fieldType = Field.GlomFieldType.TYPE_TEXT;
 			} else if (fieldTypeStr.equals("Time")) {
 				fieldType = Field.GlomFieldType.TYPE_TIME;
 			}
 		}
 
 		field.setGlomFieldType(fieldType);
 
 		field.setPrimaryKey(getAttributeAsBoolean(element, ATTRIBUTE_PRIMARY_KEY));
 		loadTitle(element, field);
 
 		final Element elementFormatting = getElementByName(element, NODE_FORMATTING);
 		if (elementFormatting != null) {
 			loadFormatting(elementFormatting, field.getFormatting());
 		}
 	}
 
 	/**
 	 * @param elementFormatting
 	 * @param formatting
 	 */
 	private void loadFormatting(final Element elementFormatting, final Formatting formatting) {
 		if (elementFormatting == null)
 			return;
 
 		if (formatting == null)
 			return;
 
 		// formatting.setTextFormatMultiline(getAttributeAsBoolean(elementFormatting, ATTRIBUTE_TEXT_FORMAT_MULTILINE));
 
 		final NumericFormat numericFormatting = formatting.getNumericFormat();
 		if (numericFormatting != null) {
 			numericFormatting.setUseThousandsSeparator(getAttributeAsBoolean(elementFormatting,
 					ATTRIBUTE_USE_THOUSANDS_SEPARATOR));
 			numericFormatting.setDecimalPlaces(getAttributeAsDecimal(elementFormatting, ATTRIBUTE_DECIMAL_PLACES));
 		}
 
 	}
 
 	/**
 	 * @param element
 	 * @param reportNode
 	 */
 	private void loadReport(final Element element, final Report report, final String tableName) {
 		report.setName(element.getAttribute(ATTRIBUTE_NAME));
 		loadTitle(element, report);
 
 		final List<LayoutGroup> listLayoutGroups = loadLayoutNode(element, tableName);
 
 		// A report can actually only have one LayoutGroup,
 		// though it uses the same XML structure as List and Details layouts,
 		// which (wrongly) suggests that it can have more than one group.
 		LayoutGroup layoutGroup = null;
 		if (!listLayoutGroups.isEmpty()) {
 			layoutGroup = listLayoutGroups.get(0);
 		}
 
 		report.setLayoutGroup(layoutGroup);
 	}
 
 	private TableInfo getTableInfo(final String tableName) {
 		return tablesMap.get(tableName);
 	}
 
 	public enum HostingMode {
 		HOSTING_MODE_POSTGRES_CENTRAL, HOSTING_MODE_POSTGRES_SELF, HOSTING_MODE_SQLITE
 	};
 
 	public String getDatabaseTitle(final String locale) {
 		return databaseTitle.getTitle(locale);
 	}
 
 	public String getDatabaseTitleOriginal() {
 		return databaseTitle.getTitleOriginal();
 	}
 
 	public List<String> getTranslationAvailableLocales() {
 		return translationAvailableLocales;
 	}
 
 	public Document.HostingMode getHostingMode() {
 		return HostingMode.HOSTING_MODE_POSTGRES_CENTRAL; // TODO
 	}
 
 	public String getConnectionServer() {
 		return connectionServer;
 	}
 
 	public long getConnectionPort() {
 		return connectionPort;
 	}
 
 	public String getConnectionDatabase() {
 		return connectionDatabase;
 	}
 
 	public List<String> getTableNames() {
 		// TODO: Return a Set?
 		return new ArrayList<String>(tablesMap.keySet());
 	}
 
 	public boolean getTableIsHidden(final String tableName) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null) {
 			return false;
 		}
 
 		return info.isHidden;
 	}
 
 	public String getTableTitle(final String tableName, final String locale) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null) {
 			return "";
 		}
 
 		return info.getTitle(locale);
 	}
 
 	public String getDefaultTable() {
 		for (final TableInfo info : tablesMap.values()) {
 			if (info.isDefault) {
 				return info.getName();
 			}
 		}
 
 		return "";
 	}
 
 	public boolean getTableIsKnown(final String tableName) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public List<Field> getTableFields(final String tableName) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null)
 			return null;
 
 		return new ArrayList<Field>(info.fieldsMap.values());
 	}
 
 	public Field getField(final String tableName, final String strFieldName) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null)
 			return null;
 
 		return info.fieldsMap.get(strFieldName);
 	}
 
 	public List<LayoutGroup> getDataLayoutGroups(final String layoutName, final String parentTableName) {
 		final TableInfo info = getTableInfo(parentTableName);
 		if (info == null)
 			return new ArrayList<LayoutGroup>();
 
 		if (layoutName == LAYOUT_NAME_DETAILS) {
 			return info.layoutGroupsDetails;
 		} else if (layoutName == LAYOUT_NAME_LIST) {
 			return info.layoutGroupsList;
 		} else {
 			return new ArrayList<LayoutGroup>();
 		}
 	}
 
 	public List<String> getReportNames(final String tableName) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null)
 			return new ArrayList<String>();
 
 		return new ArrayList<String>(info.reportsMap.keySet());
 	}
 
 	public Report getReport(final String tableName, final String reportName) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null)
 			return null;
 
 		return info.reportsMap.get(reportName);
 	}
 
 	/**
 	 * @param tableName
 	 * @param field
 	 * @return
 	 */
 	public Relationship getFieldUsedInRelationshipToOne(final String tableName, final LayoutItemField layoutField) {
 
 		if (layoutField == null) {
 			Log.error("layoutField was null");
 			return null;
 		}
 
 		Relationship result = null;
 
 		final String tableUsed = layoutField.getTableUsed(tableName);
 		final TableInfo info = getTableInfo(tableUsed);
 		if (info == null) {
 			// This table is special. We would not create a relationship to it using a field:
 			// if(tableUsed == GLOM_STANDARD_TABLE_PREFS_TABLE_NAME)
 			// return result;
 
 			Log.error("table not found: " + tableUsed);
 			return null;
 		}
 
 		// Look at each relationship:
 		final String fieldName = layoutField.getName();
 		for (final Relationship relationship : info.relationshipsMap.values()) {
 			if (relationship != null) {
 				// If the relationship uses the field
 				if (relationship.getFromField() == fieldName) {
 					// if the to_table is not hidden:
 					if (!getTableIsHidden(relationship.getToTable())) {
 						// TODO_Performance: The use of this convenience method means we get the full relationship
 						// information again:
 						if (getRelationshipIsToOne(tableName, relationship.getName())) {
 							result = relationship;
 						}
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param tableName
 	 * @param relationshipName
 	 * @return
 	 */
 	private boolean getRelationshipIsToOne(final String tableName, final String relationshipName) {
 		final Relationship relationship = getRelationship(tableName, relationshipName);
 		if (relationship != null) {
 			final Field fieldTo = getField(relationship.getToTable(), relationship.getToField());
 			if (fieldTo != null) {
 				return (fieldTo.getPrimaryKey() || fieldTo.getUniqueKey());
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * @param tableName
 	 * @param relationshipName
 	 * @return
 	 */
 	private Relationship getRelationship(final String tableName, final String relationshipName) {
 		final TableInfo info = getTableInfo(tableName);
 		if (info == null) {
 			Log.error("table not found: " + tableName);
 			return null;
 		}
 
 		return info.relationshipsMap.get(relationshipName);
 	}
 
 	public class TableToViewDetails {
 		public String tableName;
 		public UsesRelationship usesRelationship;
 	}
 
 	private static class FieldIdentifies {
 		public LayoutItemField field;
 		public Relationship usedInRelationShip;
 	}
 
 	/**
 	 * @param tableName
 	 *            Output parameter
 	 * @param relationship
 	 * @param portal
 	 *            TODO
 	 */
 	public TableToViewDetails getPortalSuitableTableToViewDetails(LayoutItemPortal portal) {
 		UsesRelationship navigationRelationship = null;
 
 		// Check whether a relationship was specified:
 		if (portal.getNavigationType() == NavigationType.NAVIGATION_AUTOMATIC) {
 			navigationRelationship = getPortalNavigationRelationshipAutomatic(portal);
 		} else {
 			navigationRelationship = portal.getNavigationRelationshipSpecific();
 		}
 
 		// Get the navigation table name from the chosen relationship:
 		String directlyRelatedTableName = portal.getTableUsed("" /* not relevant */);
 
 		// The navigation_table_name (and therefore, the table_name output parameter,
 		// as well) stays empty if the navrel type was set to none.
 		String navigationTableName = null;
 		if (navigationRelationship != null) {
 			navigationTableName = navigationRelationship.getTableUsed(directlyRelatedTableName);
 		} else if (portal.getNavigationType() != NavigationType.NAVIGATION_NONE) {
 			// An empty result from get_portal_navigation_relationship_automatic() or
 			// get_navigation_relationship_specific() means we should use the directly related table:
 			navigationTableName = directlyRelatedTableName;
 		}
 
 		if (StringUtils.isEmpty(navigationTableName)) {
 			return null;
 		}
 
 		if (this == null) {
 			Log.error("document is null.");
 			return null;
 		}
 
 		if (getTableIsHidden(navigationTableName)) {
 			Log.error("navigation_table_name indicates a hidden table: " + navigationTableName);
 			return null;
 		}
 
 		TableToViewDetails result = new TableToViewDetails();
 		result.tableName = navigationTableName;
 		result.usesRelationship = navigationRelationship;
 		return result;
 	}
 
 	/**
 	 * @param portal
 	 *            TODO
 	 * @return
 	 */
 	private UsesRelationship getPortalNavigationRelationshipAutomatic(LayoutItemPortal portal) {
 		if (this == null) {
 			return null;
 		}
 
 		// If the related table is not hidden then we can just navigate to that:
 		final String direct_related_table_name = portal.getTableUsed("" /* parent table - not relevant */);
 		if (!getTableIsHidden(direct_related_table_name)) {
 			// Non-hidden tables can just be shown directly. Navigate to it:
 			return null;
 		} else {
 			// If the related table is hidden,
 			// then find a suitable related non-hidden table by finding the first layout field that mentions one:
 			final LayoutItemField field = getPortalFieldIsFromNonHiddenRelatedRecord(portal);
 			if (field != null) {
 				return field; // Returns the UsesRelationship base part. (A relationship belonging to the portal's
 								// related table.)
 			} else {
 				// Instead, find a key field that's used in a relationship,
 				// and pretend that we are showing the to field as a related field:
 				final FieldIdentifies fieldIndentifies = getPortalFieldIdentifiesNonHiddenRelatedRecord(portal);
 				if (fieldIndentifies != null) {
 					if (fieldIndentifies.usedInRelationShip != null) {
 						UsesRelationship result = new UsesRelationshipImpl();
 						result.setRelationship(fieldIndentifies.usedInRelationShip);
 						return result;
 					}
 				}
 			}
 		}
 
 		// There was no suitable related table to show:
 		return null;
 	}
 
 	/**
 	 * @param portal
 	 *            TODO
 	 * @return
 	 */
 	private LayoutItemField getPortalFieldIsFromNonHiddenRelatedRecord(LayoutItemPortal portal) {
 		// Find the first field that is from a non-hidden related table.
 
 		if (this == null) {
 			return null;
 		}
 
 		LayoutItemField result = null;
 
 		final String parent_table_name = portal.getTableUsed("" /* parent table - not relevant */);
 
 		final List<LayoutItem> items = portal.getItems();
 		for (LayoutItem item : items) {
 			if (item instanceof LayoutItemField) {
 				LayoutItemField field = (LayoutItemField) item;
 				if (field.getHasRelationshipName()) {
 					final String table_name = field.getTableUsed(parent_table_name);
 					if (!(getTableIsHidden(table_name)))
 						return field;
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param used_in_relationship
 	 * @param portal
 	 *            TODO
 	 * @return
 	 */
 	private FieldIdentifies getPortalFieldIdentifiesNonHiddenRelatedRecord(LayoutItemPortal portal) {
 		// Find the first field that is from a non-hidden related table.
 
 		if (this == null) {
 			Log.error("document is null");
 			return null;
 		}
 
 		final String parent_table_name = portal.getTableUsed("" /* parent table - not relevant */);
 
 		List<LayoutItem> items = portal.getItems();
 		for (LayoutItem item : items) {
 			if (item instanceof LayoutItemField) {
 				LayoutItemField field = (LayoutItemField) item;
 				if (field.getHasRelationshipName()) {
 					final Relationship relationship = getFieldUsedInRelationshipToOne(parent_table_name, field);
 					if (relationship != null) {
 						final String table_name = relationship.getToTable();
 						if (!StringUtils.isEmpty(table_name)) {
 							if (!(getTableIsHidden(table_name))) {
 
 								FieldIdentifies result = new FieldIdentifies();
 								result.field = field;
 								result.usedInRelationShip = relationship;
 								return result;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 }
