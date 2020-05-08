 package com.redhat.ecs.services.docbookcompiling.xmlprocessing;
 
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import com.redhat.contentspec.Level;
 import com.redhat.contentspec.SpecTopic;
 import com.redhat.contentspec.entities.TargetRelationship;
 import com.redhat.contentspec.entities.TopicRelationship;
 import com.redhat.ecs.commonstructures.Pair;
 import com.redhat.ecs.commonutils.CollectionUtilities;
 import com.redhat.ecs.commonutils.ExceptionUtilities;
 import com.redhat.ecs.commonutils.XMLUtilities;
 import com.redhat.ecs.constants.CommonConstants;
 import com.redhat.ecs.services.docbookcompiling.DocbookBuilderConstants;
 import com.redhat.ecs.services.docbookcompiling.DocbookBuildingOptions;
 import com.redhat.ecs.services.docbookcompiling.DocbookUtils;
 import com.redhat.ecs.services.docbookcompiling.xmlprocessing.structures.GenericInjectionPoint;
 import com.redhat.ecs.services.docbookcompiling.xmlprocessing.structures.GenericInjectionPointDatabase;
 import com.redhat.ecs.services.docbookcompiling.xmlprocessing.structures.InjectionListData;
 import com.redhat.ecs.services.docbookcompiling.xmlprocessing.structures.InjectionTopicData;
 import com.redhat.ecs.services.docbookcompiling.xmlprocessing.structures.TocTopicDatabase;
 import com.redhat.ecs.sort.ExternalListSort;
 
 import com.redhat.topicindex.rest.entities.ComponentBaseTopicV1;
 import com.redhat.topicindex.rest.entities.ComponentTagV1;
 import com.redhat.topicindex.rest.entities.ComponentTopicV1;
 import com.redhat.topicindex.rest.entities.ComponentTranslatedTopicV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTBaseTopicV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTPropertyTagV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTagV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTopicV1;
 import com.redhat.topicindex.rest.entities.interfaces.RESTTranslatedTopicV1;
 import com.redhat.topicindex.rest.sort.TopicTitleSorter;
 import com.redhat.topicindex.rest.sort.BaseTopicV1TitleComparator;
 
 /**
  * This class takes the XML from a topic and modifies it to include and injected content.
  */
 public class XMLPreProcessor<T extends RESTBaseTopicV1<T>>
 {
 	/**
 	 * Used to identify that an <orderedlist> should be generated for the injection point
 	 */
 	protected static final int ORDEREDLIST_INJECTION_POINT = 1;
 	/**
 	 * Used to identify that an <itemizedlist> should be generated for the injection point
 	 */
 	protected static final int ITEMIZEDLIST_INJECTION_POINT = 2;
 	/**
 	 * Used to identify that an <xref> should be generated for the injection point
 	 */
 	protected static final int XREF_INJECTION_POINT = 3;
 	/**
 	 * Used to identify that an <xref> should be generated for the injection point
 	 */
 	protected static final int LIST_INJECTION_POINT = 4;
 	/** Identifies a named regular expression group */
 	protected static final String TOPICIDS_RE_NAMED_GROUP = "TopicIDs";
 	/** This text identifies an option task in a list */
 	protected static final String OPTIONAL_MARKER = "OPT:";
 	/** The text to be prefixed to a list item if a topic is optional */
 	protected static final String OPTIONAL_LIST_PREFIX = "Optional: ";
 	/** A regular expression that identifies a topic id */
 	protected static final String OPTIONAL_TOPIC_ID_RE = "(" + OPTIONAL_MARKER + "\\s*)?\\d+";
 	/** A regular expression that identifies a topic id */
 	protected static final String TOPIC_ID_RE = "\\d+";
 
 	/**
 	 * A regular expression that matches an InjectSequence custom injection point
 	 */
 	public static final String CUSTOM_INJECTION_SEQUENCE_RE =
 	/*
 	 * start xml comment and 'InjectSequence:' surrounded by optional white space
 	 */
 	"\\s*InjectSequence:\\s*" +
 	/*
 	 * an optional comma separated list of digit blocks, and at least one digit block with an optional comma
 	 */
 	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
 	/* xml comment end */
 	"\\s*";
 
 	/** A regular expression that matches an InjectList custom injection point */
 	public static final String CUSTOM_INJECTION_LIST_RE =
 	/* start xml comment and 'InjectList:' surrounded by optional white space */
 	"\\s*InjectList:\\s*" +
 	/*
 	 * an optional comma separated list of digit blocks, and at least one digit block with an optional comma
 	 */
 	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
 	/* xml comment end */
 	"\\s*";
 
 	public static final String CUSTOM_INJECTION_LISTITEMS_RE =
 	/* start xml comment and 'InjectList:' surrounded by optional white space */
 	"\\s*InjectListItems:\\s*" +
 	/*
 	 * an optional comma separated list of digit blocks, and at least one digit block with an optional comma
 	 */
 	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
 	/* xml comment end */
 	"\\s*";
 
 	public static final String CUSTOM_ALPHA_SORT_INJECTION_LIST_RE =
 	/*
 	 * start xml comment and 'InjectListAlphaSort:' surrounded by optional white space
 	 */
 	"\\s*InjectListAlphaSort:\\s*" +
 	/*
 	 * an optional comma separated list of digit blocks, and at least one digit block with an optional comma
 	 */
 	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
 	/* xml comment end */
 	"\\s*";
 
 	/** A regular expression that matches an Inject custom injection point */
 	public static final String CUSTOM_INJECTION_SINGLE_RE =
 	/* start xml comment and 'Inject:' surrounded by optional white space */
 	"\\s*Inject:\\s*" +
 	/* one digit block */
 	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + OPTIONAL_TOPIC_ID_RE + "))" +
 	/* xml comment end */
 	"\\s*";
 
 	/** A regular expression that matches an Inject Content Fragment */
 	public static final String INJECT_CONTENT_FRAGMENT_RE =
 	/* start xml comment and 'Inject:' surrounded by optional white space */
 	"\\s*InjectText:\\s*" +
 	/* one digit block */
 	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + TOPIC_ID_RE + "))" +
 	/* xml comment end */
 	"\\s*";
 
 	/** A regular expression that matches an Inject Content Fragment */
 	public static final String INJECT_TITLE_FRAGMENT_RE =
 	/* start xml comment and 'Inject:' surrounded by optional white space */
 	"\\s*InjectTitle:\\s*" +
 	/* one digit block */
 	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + TOPIC_ID_RE + "))" +
 	/* xml comment end */
 	"\\s*";
 
 	/**
 	 * The noinject value for the role attribute indicates that an element should not be included in the Topic Fragment
 	 */
 	protected static final String NO_INJECT_ROLE = "noinject";
 
 	public void processTopicBugzillaLink(final SpecTopic specTopic, final Document document, final DocbookBuildingOptions docbookBuildingOptions, final String buildName, final String searchTagsUrl, final Date buildDate)
 	{
 		/* SIMPLESECT TO HOLD OTHER LINKS */
 		final Element bugzillaSection = document.createElement("simplesect");
 		document.getDocumentElement().appendChild(bugzillaSection);
 
 		final Element bugzillaSectionTitle = document.createElement("title");
 		bugzillaSectionTitle.setTextContent("");
 		bugzillaSection.appendChild(bugzillaSectionTitle);
 
 		/* BUGZILLA LINK */
 		try
 		{
 			final String instanceNameProperty = System.getProperty(CommonConstants.INSTANCE_NAME_PROPERTY);
 			final String fixedInstanceNameProperty = instanceNameProperty == null ? "Not Defined" : instanceNameProperty;
 
 			final Element bugzillaPara = document.createElement("para");
 			bugzillaPara.setAttribute("role", DocbookBuilderConstants.ROLE_CREATE_BUG_PARA);
 
 			final Element bugzillaULink = document.createElement("ulink");
 
 			bugzillaULink.setTextContent("Report a bug");
 
 			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
 
 			String specifiedBuildName = "";
 			if (docbookBuildingOptions != null && docbookBuildingOptions.getBuildName() != null)
 				specifiedBuildName = docbookBuildingOptions.getBuildName();
 
 			/* build up the elements that go into the bugzilla URL */
 			String bugzillaProduct = null;
 			String bugzillaComponent = null;
 			String bugzillaVersion = null;
 			String bugzillaKeywords = null;
 			String bugzillaAssignedTo = null;
 			final String bugzillaEnvironment = URLEncoder.encode("Instance Name: " + fixedInstanceNameProperty + "\nBuild: " + buildName + "\nBuild Filter: " + searchTagsUrl + "\nBuild Name: " + specifiedBuildName + "\nBuild Date: " + formatter.format(buildDate), "UTF-8");
 			
 			final RESTBaseTopicV1<? extends RESTBaseTopicV1<?>> topic = specTopic.getTopic();
 			final String bugzillaBuildId = topic instanceof RESTTopicV1 ? ComponentTopicV1.returnBugzillaBuildId((RESTTopicV1)topic) : ComponentTranslatedTopicV1.returnBugzillaBuildId((RESTTranslatedTopicV1)topic);			
 			final String bugzillaBuildID = URLEncoder.encode(bugzillaBuildId, "UTF-8");
 
 			/* look for the bugzilla options */
 			if (specTopic.getTopic().getTags() != null && specTopic.getTopic().getTags().getItems() != null)
 			{
 				for (final RESTTagV1 tag : specTopic.getTopic().getTags().getItems())
 				{
 					final RESTPropertyTagV1 bugzillaProductTag = ComponentTagV1.returnProperty(tag, CommonConstants.BUGZILLA_PRODUCT_PROP_TAG_ID);
 					final RESTPropertyTagV1 bugzillaComponentTag = ComponentTagV1.returnProperty(tag, CommonConstants.BUGZILLA_COMPONENT_PROP_TAG_ID);
 					final RESTPropertyTagV1 bugzillaKeywordsTag = ComponentTagV1.returnProperty(tag, CommonConstants.BUGZILLA_KEYWORDS_PROP_TAG_ID);
 					final RESTPropertyTagV1 bugzillaVersionTag = ComponentTagV1.returnProperty(tag, CommonConstants.BUGZILLA_VERSION_PROP_TAG_ID);
 					final RESTPropertyTagV1 bugzillaAssignedToTag = ComponentTagV1.returnProperty(tag, CommonConstants.BUGZILLA_PROFILE_PROPERTY);
 
 					if (bugzillaProduct == null && bugzillaProductTag != null)
 						bugzillaProduct = URLEncoder.encode(bugzillaProductTag.getValue(), "UTF-8");
 
 					if (bugzillaComponent == null && bugzillaComponentTag != null)
 						bugzillaComponent = URLEncoder.encode(bugzillaComponentTag.getValue(), "UTF-8");
 
 					if (bugzillaKeywords == null && bugzillaKeywordsTag != null)
 						bugzillaKeywords = URLEncoder.encode(bugzillaKeywordsTag.getValue(), "UTF-8");
 
 					if (bugzillaVersion == null && bugzillaVersionTag != null)
 						bugzillaVersion = URLEncoder.encode(bugzillaVersionTag.getValue(), "UTF-8");
 
 					if (bugzillaAssignedTo == null && bugzillaAssignedToTag != null)
 						bugzillaAssignedTo = URLEncoder.encode(bugzillaAssignedToTag.getValue(), "UTF-8");
 				}
 			}
 
 			/* build the bugzilla url options */
 			String bugzillaURLComponents = "";
 
 			/* we need at least a product */
 			if (bugzillaProduct != null)
 			{
 				bugzillaURLComponents += bugzillaURLComponents.isEmpty() ? "?" : "&amp;";
 				bugzillaURLComponents += "product=" + bugzillaProduct;
 
 				if (bugzillaComponent != null)
 				{
 					bugzillaURLComponents += bugzillaURLComponents.isEmpty() ? "?" : "&amp;";
 					bugzillaURLComponents += "component=" + bugzillaComponent;
 				}
 
 				if (bugzillaVersion != null)
 				{
 					bugzillaURLComponents += bugzillaURLComponents.isEmpty() ? "?" : "&amp;";
 					bugzillaURLComponents += "version=" + bugzillaVersion;
 				}
 
 				if (bugzillaKeywords != null)
 				{
 					bugzillaURLComponents += bugzillaURLComponents.isEmpty() ? "?" : "&amp;";
 					bugzillaURLComponents += "keywords=" + bugzillaKeywords;
 				}
 
 				if (bugzillaAssignedTo != null)
 				{
 					bugzillaURLComponents += bugzillaURLComponents.isEmpty() ? "?" : "&amp;";
 					bugzillaURLComponents += "assigned_to=" + bugzillaAssignedTo;
 				}
 
 				bugzillaURLComponents += bugzillaURLComponents.isEmpty() ? "?" : "&amp;";
 				bugzillaURLComponents += "cf_environment=" + bugzillaEnvironment;
 
 				bugzillaURLComponents += bugzillaURLComponents.isEmpty() ? "?" : "&amp;";
 				bugzillaURLComponents += "cf_build_id=" + bugzillaBuildID;
 			}
 
 			/* build the bugzilla url with the base components */
 			String bugZillaUrl = "https://bugzilla.redhat.com/enter_bug.cgi" + bugzillaURLComponents;
 
 			bugzillaULink.setAttribute("url", bugZillaUrl);
 
 			/*
 			 * only add the elements to the XML DOM if there was no exception (not that there should be one
 			 */
 			bugzillaSection.appendChild(bugzillaPara);
 			bugzillaPara.appendChild(bugzillaULink);
 		}
 		catch (final Exception ex)
 		{
 			ExceptionUtilities.handleException(ex);
 		}
 	}
 
 	/**
 	 * Adds some debug information and links to the end of the topic
 	 */
 	public void processTopicAdditionalInfo(final SpecTopic specTopic, final Document document, final DocbookBuildingOptions docbookBuildingOptions, final String buildName, final String searchTagsUrl, final Date buildDate)
 	{		
 		if ((docbookBuildingOptions != null && docbookBuildingOptions.getInsertSurveyLink()) || searchTagsUrl != null)
 		{
 			/* SIMPLESECT TO HOLD OTHER LINKS */
 			final Element bugzillaSection = document.createElement("simplesect");
 			document.getDocumentElement().appendChild(bugzillaSection);
 	
 			final Element bugzillaSectionTitle = document.createElement("title");
 			bugzillaSectionTitle.setTextContent("");
 			bugzillaSection.appendChild(bugzillaSectionTitle);
 	
 			// SURVEY LINK
 			if (docbookBuildingOptions != null && docbookBuildingOptions.getInsertSurveyLink())
 			{
 				final Element surveyPara = document.createElement("para");
 				surveyPara.setAttribute("role", DocbookBuilderConstants.ROLE_CREATE_BUG_PARA);
 				bugzillaSection.appendChild(surveyPara);
 	
 				final Text startSurveyText = document.createTextNode("Thank you for evaluating the new documentation format for JBoss Enterprise Application Platform. Let us know what you think by taking a short ");
 				surveyPara.appendChild(startSurveyText);
 	
 				final Element surveyULink = document.createElement("ulink");
 				surveyPara.appendChild(surveyULink);
 				surveyULink.setTextContent("survey");
 				surveyULink.setAttribute("url", "https://www.keysurvey.com/survey/380730/106f/");
 	
 				final Text endSurveyText = document.createTextNode(".");
 				surveyPara.appendChild(endSurveyText);
 			}
 	
 			/* searchTagsUrl will be null for internal (i.e. HTML rendering) builds */
 			if (searchTagsUrl != null)
 			{
 				// VIEW IN SKYNET
 	
 				final Element skynetElement = document.createElement("remark");
 				skynetElement.setAttribute("role", DocbookBuilderConstants.ROLE_VIEW_IN_SKYNET_PARA);
 				bugzillaSection.appendChild(skynetElement);
 	
 				final Element skynetLinkULink = document.createElement("ulink");				
 				skynetElement.appendChild(skynetLinkULink);
 				skynetLinkULink.setTextContent("View in Skynet");
 				
 				final RESTBaseTopicV1<? extends RESTBaseTopicV1<?>> topic = specTopic.getTopic();
 				final String url = topic instanceof RESTTopicV1 ? ComponentTopicV1.returnSkynetURL((RESTTopicV1)topic) : ComponentTranslatedTopicV1.returnSkynetURL((RESTTranslatedTopicV1)topic); 
 				skynetLinkULink.setAttribute("url", url);
 	
 				// SKYNET VERSION
 	
 				final Element buildVersionElement = document.createElement("remark");
 				buildVersionElement.setAttribute("role", DocbookBuilderConstants.ROLE_BUILD_VERSION_PARA);
 				bugzillaSection.appendChild(buildVersionElement);
 	
 				final Element skynetVersionElementULink = document.createElement("ulink");
 				buildVersionElement.appendChild(skynetVersionElementULink);
 				skynetVersionElementULink.setTextContent("Built with " + buildName);
 				skynetVersionElementULink.setAttribute("url", searchTagsUrl);
 			}
 		}
 		
 		// BUGZILLA LINK
 		if (docbookBuildingOptions != null && docbookBuildingOptions.getInsertBugzillaLinks()) {
 			processTopicBugzillaLink(specTopic, document, docbookBuildingOptions, buildName, searchTagsUrl, buildDate);
 		}
 	}
 
 	/**
 	 * Takes a comma separated list of ints, and returns an array of Integers. This is used when processing custom injection points.
 	 */
 	private static List<InjectionTopicData> processTopicIdList(final String list)
 	{
 		/* find the individual topic ids */
 		final String[] topicIDs = list.split(",");
 
 		List<InjectionTopicData> retValue = new ArrayList<InjectionTopicData>(topicIDs.length);
 
 		/* clean the topic ids */
 		for (int i = 0; i < topicIDs.length; ++i)
 		{
 			final String topicId = topicIDs[i].replaceAll(OPTIONAL_MARKER, "").trim();
 			final boolean optional = topicIDs[i].indexOf(OPTIONAL_MARKER) != -1;
 
 			try
 			{
 				final InjectionTopicData topicData = new InjectionTopicData(Integer.parseInt(topicId), optional);
 				retValue.add(topicData);
 			}
 			catch (final Exception ex)
 			{
 				/*
 				 * these lists are discovered by a regular expression so we shouldn't have any trouble here with Integer.parse
 				 */
 				ExceptionUtilities.handleException(ex);
 				retValue.add(new InjectionTopicData(-1, false));
 			}
 		}
 
 		return retValue;
 	}
 
 	public List<Integer> processInjections(final Level level, final SpecTopic topic, final ArrayList<Integer> customInjectionIds, final Document xmlDocument, final DocbookBuildingOptions docbookBuildingOptions, final boolean usedFixedUrls)
 	{
 		/*
 		 * this collection keeps a track of the injection point markers and the docbook lists that we will be replacing them with
 		 */
 		final HashMap<Node, InjectionListData> customInjections = new HashMap<Node, InjectionListData>();
 
 		final List<Integer> errorTopics = new ArrayList<Integer>();
 
 		errorTopics.addAll(processInjections(level, topic, customInjectionIds, customInjections, ORDEREDLIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_SEQUENCE_RE, null, docbookBuildingOptions, usedFixedUrls));
 		errorTopics.addAll(processInjections(level, topic, customInjectionIds, customInjections, XREF_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_SINGLE_RE, null, docbookBuildingOptions, usedFixedUrls));
 		errorTopics.addAll(processInjections(level, topic, customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_LIST_RE, null, docbookBuildingOptions, usedFixedUrls));
 		errorTopics.addAll(processInjections(level, topic, customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, xmlDocument, CUSTOM_ALPHA_SORT_INJECTION_LIST_RE, new TopicTitleSorter<T>(), docbookBuildingOptions, usedFixedUrls));
 		errorTopics.addAll(processInjections(level, topic, customInjectionIds, customInjections, LIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_LISTITEMS_RE, null, docbookBuildingOptions, usedFixedUrls));
 
 		/*
 		 * If we are not ignoring errors, return the list of topics that could not be injected
 		 */
 		if (errorTopics.size() != 0 && docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections())
 			return errorTopics;
 
 		/* now make the custom injection point substitutions */
 		for (final Node customInjectionCommentNode : customInjections.keySet())
 		{
 			final InjectionListData injectionListData = customInjections.get(customInjectionCommentNode);
 			List<Element> list = null;
 
 			/*
 			 * this may not be true if we are not building all related topics
 			 */
 			if (injectionListData.listItems.size() != 0)
 			{
 				if (injectionListData.listType == ORDEREDLIST_INJECTION_POINT)
 				{
 					list = DocbookUtils.wrapOrderedListItemsInPara(xmlDocument, injectionListData.listItems);
 				}
 				else if (injectionListData.listType == XREF_INJECTION_POINT)
 				{
 					list = injectionListData.listItems.get(0);
 				}
 				else if (injectionListData.listType == ITEMIZEDLIST_INJECTION_POINT)
 				{
 					list = DocbookUtils.wrapItemizedListItemsInPara(xmlDocument, injectionListData.listItems);
 				}
 				else if (injectionListData.listType == LIST_INJECTION_POINT)
 				{
 					list = DocbookUtils.wrapItemsInListItems(xmlDocument, injectionListData.listItems);
 				}
 			}
 
 			if (list != null)
 			{
 				for (final Element element : list)
 				{
 					customInjectionCommentNode.getParentNode().insertBefore(element, customInjectionCommentNode);
 				}
 
 				customInjectionCommentNode.getParentNode().removeChild(customInjectionCommentNode);
 			}
 		}
 
 		return errorTopics;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Integer> processInjections(final Level level, final SpecTopic topic, final ArrayList<Integer> customInjectionIds, final HashMap<Node, InjectionListData> customInjections, final int injectionPointType, final Document xmlDocument, final String regularExpression,
 			final ExternalListSort<Integer, T, InjectionTopicData> sortComparator, final DocbookBuildingOptions docbookBuildingOptions, final boolean usedFixedUrls)
 	{
 		final List<Integer> retValue = new ArrayList<Integer>();
 
 		if (xmlDocument == null)
 			return retValue;
 
 		/* loop over all of the comments in the document */
 		for (final Node comment : XMLUtilities.getComments(xmlDocument))
 		{
 			final String commentContent = comment.getNodeValue();
 
 			/* compile the regular expression */
 			final Pattern injectionSequencePattern = Pattern.compile(regularExpression);
 			/* find any matches */
 			final Matcher injectionSequencematcher = injectionSequencePattern.matcher(commentContent);
 
 			/* loop over the regular expression matches */
 			while (injectionSequencematcher.find())
 			{
 				/*
 				 * get the list of topics from the named group in the regular expression match
 				 */
 				final String reMatch = injectionSequencematcher.group(TOPICIDS_RE_NAMED_GROUP);
 
 				/* make sure we actually found a matching named group */
 				if (reMatch != null)
 				{
 					/* get the sequence of ids */
 					final List<InjectionTopicData> sequenceIDs = processTopicIdList(reMatch);
 
 					/*
 					 * get the outgoing relationships
 					 */
 					final List<T> relatedTopics = (List<T>) topic.getTopic().getOutgoingRelationships().getItems();
 
 					/*
 					 * Create a TocTopicDatabase to hold the related topics. The TocTopicDatabase provides a convenient way to access these topics
 					 */
 					TocTopicDatabase<T> relatedTopicsDatabase = new TocTopicDatabase<T>();
 					relatedTopicsDatabase.setTopics(relatedTopics);
 
 					/* sort the InjectionTopicData list if required */
 					if (sortComparator != null)
 					{
 						sortComparator.sort(relatedTopics, sequenceIDs);
 					}
 
 					/* loop over all the topic ids in the injection point */
 					for (final InjectionTopicData sequenceID : sequenceIDs)
 					{
 						/*
 						 * topics that are injected into custom injection points are excluded from the generic related topic lists at the beginning and end of a
 						 * topic. adding the topic id here means that when it comes time to generate the generic related topic lists, we can skip this topic
 						 */
 						customInjectionIds.add(sequenceID.topicId);
 
 						/*
 						 * Pull the topic out of the list of related topics
 						 */
 						final T relatedTopic = relatedTopicsDatabase.getTopic(sequenceID.topicId);
 
 						/*
 						 * See if the topic is also available in the main database (if the main database is available)
 						 */
 						final boolean isInDatabase = level == null ? true : level.isSpecTopicInLevelByTopicID(sequenceID.topicId);
 
 						/*
 						 * It is possible that the topic id referenced in the injection point has not been related, or has not been included in the list of
 						 * topics to process. This is a validity error
 						 */
 						if (relatedTopic != null && isInDatabase)
 						{
 							/*
 							 * build our list
 							 */
 							List<List<Element>> list = new ArrayList<List<Element>>();
 
 							/*
 							 * each related topic is added to a string, which is stored in the customInjections collection. the customInjections key is the
 							 * custom injection text from the source xml. this allows us to match the xrefs we are generating for the related topic with the
 							 * text in the xml file that these xrefs will eventually replace
 							 */
 							if (customInjections.containsKey(comment))
 								list = customInjections.get(comment).listItems;
 
 							/* if the toc is null, we are building an internal page */
 							if (level == null)
 							{
 								final String url = relatedTopic instanceof RESTTranslatedTopicV1 ? ComponentTranslatedTopicV1.returnInternalURL((RESTTranslatedTopicV1) relatedTopic) : ComponentTopicV1.returnInternalURL((RESTTopicV1) relatedTopic);
 								if (sequenceID.optional)
 								{
 									list.add(DocbookUtils.buildEmphasisPrefixedULink(xmlDocument, OPTIONAL_LIST_PREFIX, url, relatedTopic.getTitle()));
 								}
 								else
 								{
 									list.add(DocbookUtils.buildULink(xmlDocument, url, relatedTopic.getTitle()));
 								}
 							}
 							else
 							{
 								final Integer topicId;
 								if (relatedTopic instanceof RESTTranslatedTopicV1)
 								{
 									topicId = ((RESTTranslatedTopicV1) relatedTopic).getTopicId();
 								}
 								else
 								{
 									topicId = relatedTopic.getId();
 								}
 
 								final SpecTopic closestSpecTopic = topic.getClosestTopicByDBId(topicId, true);
 								if (sequenceID.optional)
 								{
 									list.add(DocbookUtils.buildEmphasisPrefixedXRef(xmlDocument, OPTIONAL_LIST_PREFIX, closestSpecTopic.getUniqueLinkId(usedFixedUrls)));
 								}
 								else
 								{
 									list.add(DocbookUtils.buildXRef(xmlDocument, closestSpecTopic.getUniqueLinkId(usedFixedUrls)));
 								}
 							}
 
 							/*
 							 * save the changes back into the customInjections collection
 							 */
 							customInjections.put(comment, new InjectionListData(list, injectionPointType));
 						}
 						else
 						{
 							retValue.add(sequenceID.topicId);
 						}
 					}
 				}
 			}
 		}
 
 		return retValue;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public List<Integer> processGenericInjections(final Level level, final SpecTopic topic, final Document xmlDocument, final ArrayList<Integer> customInjectionIds, final List<Pair<Integer, String>> topicTypeTagIDs, final DocbookBuildingOptions docbookBuildingOptions, final boolean usedFixedUrls)
 	{
 		final List<Integer> errors = new ArrayList<Integer>();
 
 		if (xmlDocument == null)
 			return errors;
 
 		/*
 		 * this collection will hold the lists of related topics
 		 */
 		final GenericInjectionPointDatabase<T> relatedLists = new GenericInjectionPointDatabase<T>();
 
 		/* wrap each related topic in a listitem tag */
 		if (topic.getTopic().getOutgoingRelationships() != null && topic.getTopic().getOutgoingRelationships().getItems() != null)
 		{
 			for (final RESTBaseTopicV1 relatedTopic : topic.getTopic().getOutgoingRelationships().getItems())
 			{
 
 				final Integer topicId;
 				if (relatedTopic instanceof RESTTranslatedTopicV1)
 				{
 					topicId = ((RESTTranslatedTopicV1) relatedTopic).getTopicId();
 				}
 				else
 				{
 					topicId = relatedTopic.getId();
 				}
 
 				/*
 				 * don't process those topics that were injected into custom injection points
 				 */
 				if (!customInjectionIds.contains(topicId))
 				{
 					/* make sure the topic is available to be linked to */
 					if (level != null && !level.isSpecTopicInLevelByTopicID(topicId))
 					{
 						if ((docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections()))
 							errors.add(relatedTopic.getId());
 					}
 					else
 					{
 						// loop through the topic type tags
 						for (final Pair<Integer, String> primaryTopicTypeTag : topicTypeTagIDs)
 						{
 							/*
 							 * see if we have processed a related topic with one of the topic type tags this may never be true if not processing all related
 							 * topics
 							 */
 							if (ComponentBaseTopicV1.hasTag(relatedTopic, primaryTopicTypeTag.getFirst()))
 							{
 								relatedLists.addInjectionTopic(primaryTopicTypeTag, (T) relatedTopic);
 
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		insertGenericInjectionLinks(level, topic, xmlDocument, relatedLists, docbookBuildingOptions, usedFixedUrls);
 
 		return errors;
 	}
 
 	/**
 	 * The generic injection points are placed in well defined locations within a topics xml structure. This function takes the list of related topics and the
 	 * topic type tags that are associated with them and injects them into the xml document.
 	 */
 	private void insertGenericInjectionLinks(final Level level, final SpecTopic topic, final Document xmlDoc, final GenericInjectionPointDatabase<T> relatedLists, final DocbookBuildingOptions docbookBuildingOptions, final boolean usedFixedUrls)
 	{
 		/* all related topics are placed before the first simplesect */
 		final NodeList nodes = xmlDoc.getDocumentElement().getChildNodes();
 		Node simplesectNode = null;
 		for (int i = 0; i < nodes.getLength(); ++i)
 		{
 			final Node node = nodes.item(i);
 			if (node.getNodeType() == 1 && node.getNodeName().equals("simplesect"))
 			{
 				simplesectNode = node;
 				break;
 			}
 		}
 
 		/*
 		 * place the topics at the end of the topic. They will appear in the reverse order as the call to toArrayList()
 		 */
 		for (final Integer topTag : CollectionUtilities.toArrayList(DocbookBuilderConstants.REFERENCE_TAG_ID, DocbookBuilderConstants.TASK_TAG_ID, DocbookBuilderConstants.CONCEPT_TAG_ID, DocbookBuilderConstants.CONCEPTUALOVERVIEW_TAG_ID))
 		{
 			for (final GenericInjectionPoint<T> genericInjectionPoint : relatedLists.getInjectionPoints())
 			{
 				if (genericInjectionPoint.getCategoryIDAndName().getFirst() == topTag)
 				{
 					final List<T> relatedTopics = genericInjectionPoint.getTopics();
 
 					/* don't add an empty list */
 					if (relatedTopics.size() != 0)
 					{
 						final Node itemizedlist = DocbookUtils.createRelatedTopicItemizedList(xmlDoc, "Related " + genericInjectionPoint.getCategoryIDAndName().getSecond() + "s");
 
 						Collections.sort(relatedTopics, new BaseTopicV1TitleComparator<T>());
 
 						for (final T relatedTopic : relatedTopics)
 						{
 							if (level == null)
 							{
 								final String internalURL = relatedTopic instanceof RESTTranslatedTopicV1 ? ComponentTranslatedTopicV1.returnInternalURL((RESTTranslatedTopicV1) relatedTopic) : ComponentTopicV1.returnInternalURL((RESTTopicV1) relatedTopic);
 								DocbookUtils.createRelatedTopicULink(xmlDoc, internalURL, relatedTopic.getTitle(), itemizedlist);
 							}
 							else
 							{
 								final Integer topicId;
 								if (relatedTopic instanceof RESTTranslatedTopicV1)
 								{
 									topicId = ((RESTTranslatedTopicV1) relatedTopic).getTopicId();
 								}
 								else
 								{
 									topicId = relatedTopic.getId();
 								}
 
 								final SpecTopic closestSpecTopic = topic.getClosestTopicByDBId(topicId, true);
 								DocbookUtils.createRelatedTopicXRef(xmlDoc, closestSpecTopic.getUniqueLinkId(usedFixedUrls), itemizedlist);
 							}
 
 						}
 
 						if (simplesectNode != null)
 							xmlDoc.getDocumentElement().insertBefore(itemizedlist, simplesectNode);
 						else
 							xmlDoc.getDocumentElement().appendChild(itemizedlist);
 					}
 				}
 			}
 		}
 	}
 
	public static void processInternalImageFiles(final Document xmlDoc)
 	{
 		if (xmlDoc == null)
 			return;
 
 		final List<Node> imageDataNodes = XMLUtilities.getNodes(xmlDoc.getDocumentElement(), "imagedata");
 		for (final Node imageDataNode : imageDataNodes)
 		{
 			final NamedNodeMap attributes = imageDataNode.getAttributes();
 			final Node filerefAttribute = attributes.getNamedItem("fileref");
 			if (filerefAttribute != null)
 			{
 				String imageId = filerefAttribute.getTextContent();
 				imageId = imageId.replace("images/", "");
 				final int periodIndex = imageId.lastIndexOf(".");
 				if (periodIndex != -1)
 					imageId = imageId.substring(0, periodIndex);
 
 				/*
 				 * at this point imageId should be an integer that is the id of the image uploaded in skynet. We will leave the validation of imageId to the
 				 * ImageFileDisplay class.
 				 */
 
				filerefAttribute.setTextContent("ImageFileDisplay.seam?imageFileId=" + imageId);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Integer> processTopicContentFragments(final SpecTopic specTopic, final Document xmlDocument, final DocbookBuildingOptions docbookBuildingOptions)
 	{
 		final T topic = (T) specTopic.getTopic();
 		
 		final List<Integer> retValue = new ArrayList<Integer>();
 
 		if (xmlDocument == null)
 			return retValue;
 
 		final Map<Node, ArrayList<Node>> replacements = new HashMap<Node, ArrayList<Node>>();
 
 		/* loop over all of the comments in the document */
 		for (final Node comment : XMLUtilities.getComments(xmlDocument))
 		{
 			final String commentContent = comment.getNodeValue();
 
 			/* compile the regular expression */
 			final Pattern injectionSequencePattern = Pattern.compile(INJECT_CONTENT_FRAGMENT_RE);
 			/* find any matches */
 			final Matcher injectionSequencematcher = injectionSequencePattern.matcher(commentContent);
 
 			/* loop over the regular expression matches */
 			while (injectionSequencematcher.find())
 			{
 				/*
 				 * get the list of topics from the named group in the regular expression match
 				 */
 				final String reMatch = injectionSequencematcher.group(TOPICIDS_RE_NAMED_GROUP);
 
 				/* make sure we actually found a matching named group */
 				if (reMatch != null)
 				{
 					try
 					{
 						if (!replacements.containsKey(comment))
 							replacements.put(comment, new ArrayList<Node>());
 
 						final Integer topicID = Integer.parseInt(reMatch);
 
 						/*
 						 * make sure the topic we are trying to inject has been related
 						 */
 						if (topic instanceof RESTTranslatedTopicV1 ? ComponentTranslatedTopicV1.hasRelationshipTo((RESTTranslatedTopicV1) topic, topicID) : ComponentTopicV1.hasRelationshipTo((RESTTopicV1) topic, topicID))
 						{
 							final T relatedTopic = (T) (topic instanceof RESTTranslatedTopicV1 ? ComponentTranslatedTopicV1.returnRelatedTopicByID((RESTTranslatedTopicV1) topic, topicID) : ComponentTopicV1.returnRelatedTopicByID((RESTTopicV1) topic, topicID));
 							final Document relatedTopicXML = XMLUtilities.convertStringToDocument(relatedTopic.getXml());
 							if (relatedTopicXML != null)
 							{
 								final Node relatedTopicDocumentElement = relatedTopicXML.getDocumentElement();
 								final Node importedXML = xmlDocument.importNode(relatedTopicDocumentElement, true);
 
 								/* ignore the section title */
 								final NodeList sectionChildren = importedXML.getChildNodes();
 								for (int i = 0; i < sectionChildren.getLength(); ++i)
 								{
 									final Node node = sectionChildren.item(i);
 									if (node.getNodeName().equals("title"))
 									{
 										importedXML.removeChild(node);
 										break;
 									}
 								}
 
 								/* remove all with a role="noinject" attribute */
 								removeNoInjectElements(importedXML);
 
 								/*
 								 * importedXML is a now section with no title, and no child elements with the noinject value on the role attribute. We now add
 								 * its children to the Array in the replacements Map.
 								 */
 
 								final NodeList remainingChildren = importedXML.getChildNodes();
 								for (int i = 0; i < remainingChildren.getLength(); ++i)
 								{
 									final Node child = remainingChildren.item(i);
 									replacements.get(comment).add(child);
 								}
 							}
 						}
 						else if (docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections())
 						{
 							retValue.add(Integer.parseInt(reMatch));
 						}
 					}
 					catch (final Exception ex)
 					{
 						ExceptionUtilities.handleException(ex);
 					}
 				}
 			}
 		}
 
 		/*
 		 * The replacements map now has a keyset of the comments mapped to a collection of nodes that the comment will be replaced with
 		 */
 
 		for (final Node comment : replacements.keySet())
 		{
 			final ArrayList<Node> replacementNodes = replacements.get(comment);
 			for (final Node replacementNode : replacementNodes)
 				comment.getParentNode().insertBefore(replacementNode, comment);
 			comment.getParentNode().removeChild(comment);
 		}
 
 		return retValue;
 	}
 
 	protected static void removeNoInjectElements(final Node parent)
 	{
 		final NodeList childrenNodes = parent.getChildNodes();
 		final ArrayList<Node> removeNodes = new ArrayList<Node>();
 
 		for (int i = 0; i < childrenNodes.getLength(); ++i)
 		{
 			final Node node = childrenNodes.item(i);
 			final NamedNodeMap attributes = node.getAttributes();
 			if (attributes != null)
 			{
 				final Node roleAttribute = attributes.getNamedItem("role");
 				if (roleAttribute != null)
 				{
 					final String[] roles = roleAttribute.getTextContent().split(",");
 					for (final String role : roles)
 					{
 						if (role.equals(NO_INJECT_ROLE))
 						{
 							removeNodes.add(node);
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		for (final Node removeNode : removeNodes)
 			parent.removeChild(removeNode);
 
 		final NodeList remainingChildrenNodes = parent.getChildNodes();
 
 		for (int i = 0; i < remainingChildrenNodes.getLength(); ++i)
 		{
 			final Node child = remainingChildrenNodes.item(i);
 			removeNoInjectElements(child);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Integer> processTopicTitleFragments(final SpecTopic specTopic, final Document xmlDocument, final DocbookBuildingOptions docbookBuildingOptions)
 	{
 		final T topic = (T) specTopic.getTopic();
 		
 		final List<Integer> retValue = new ArrayList<Integer>();
 
 		if (xmlDocument == null)
 			return retValue;
 
 		final Map<Node, Node> replacements = new HashMap<Node, Node>();
 
 		/* loop over all of the comments in the document */
 		for (final Node comment : XMLUtilities.getComments(xmlDocument))
 		{
 			final String commentContent = comment.getNodeValue();
 
 			/* compile the regular expression */
 			final Pattern injectionSequencePattern = Pattern.compile(INJECT_TITLE_FRAGMENT_RE);
 			/* find any matches */
 			final Matcher injectionSequencematcher = injectionSequencePattern.matcher(commentContent);
 
 			/* loop over the regular expression matches */
 			while (injectionSequencematcher.find())
 			{
 				/*
 				 * get the list of topics from the named group in the regular expression match
 				 */
 				final String reMatch = injectionSequencematcher.group(TOPICIDS_RE_NAMED_GROUP);
 
 				/* make sure we actually found a matching named group */
 				if (reMatch != null)
 				{
 					try
 					{
 						if (!replacements.containsKey(comment))
 							replacements.put(comment, null);
 
 						final Integer topicID = Integer.parseInt(reMatch);
 
 						/*
 						 * make sure the topic we are trying to inject has been related
 						 */
 						if (topic instanceof RESTTranslatedTopicV1 ? ComponentTranslatedTopicV1.hasRelationshipTo((RESTTranslatedTopicV1) topic, topicID) : ComponentTopicV1.hasRelationshipTo((RESTTopicV1) topic, topicID))
 						{
 							final T relatedTopic = (T) (topic instanceof RESTTranslatedTopicV1 ? ComponentTranslatedTopicV1.returnRelatedTopicByID((RESTTranslatedTopicV1) topic, topicID) : ComponentTopicV1.returnRelatedTopicByID((RESTTopicV1) topic, topicID));
 							final Element titleNode = xmlDocument.createElement("title");
 							titleNode.setTextContent(relatedTopic.getTitle());
 							replacements.put(comment, titleNode);
 						}
 						else if (docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections())
 						{
 							retValue.add(Integer.parseInt(reMatch));
 						}
 					}
 					catch (final Exception ex)
 					{
 						ExceptionUtilities.handleException(ex);
 					}
 				}
 			}
 		}
 
 		/* swap the comment nodes with the new title nodes */
 		for (final Node comment : replacements.keySet())
 		{
 			final Node title = replacements.get(comment);
 			comment.getParentNode().insertBefore(title, comment);
 			comment.getParentNode().removeChild(comment);
 		}
 
 		return retValue;
 	}
 
 	public void processPrevRelationshipInjections(final SpecTopic topic, final Document doc, final boolean useFixedUrls)
 	{
 		if (topic.getPrevTopicRelationships().isEmpty())
 			return;
 
 		// Get the title element so that it can be used later to add the prev topic node
 		Element titleEle = null;
 		NodeList titleList = doc.getDocumentElement().getElementsByTagName("title");
 		for (int i = 0; i < titleList.getLength(); i++)
 		{
 			if (titleList.item(i).getParentNode().equals(doc.getDocumentElement()))
 			{
 				titleEle = (Element) titleList.item(i);
 				break;
 			}
 		}
 		if (titleEle != null)
 		{
 			// Attempt to get the previous topic and process it
 			List<TopicRelationship> prevList = topic.getPrevTopicRelationships();
 			// Create the paragraph/itemizedlist and list of previous relationships.
 			Element rootEle = null;
 			rootEle = doc.createElement("itemizedlist");
 			// Create the title
 			Element linkTitleEle = doc.createElement("title");
 			linkTitleEle.setAttribute("role", "process-previous-title");
 			if (prevList.size() > 1)
 			{
 				linkTitleEle.setTextContent("Previous Steps in ");
 			}
 			else
 			{
 				linkTitleEle.setTextContent("Previous Step in ");
 			}
 			Element titleXrefItem = doc.createElement("link");
 			titleXrefItem.setTextContent(topic.getParent().getTitle());
 			titleXrefItem.setAttribute("linkend", topic.getParent().getUniqueLinkId(useFixedUrls));
 			linkTitleEle.appendChild(titleXrefItem);
 			rootEle.appendChild(linkTitleEle);
 
 			for (TopicRelationship prev : prevList)
 			{
 				Element prevEle = doc.createElement("para");
 				SpecTopic prevTopic = prev.getSecondaryRelationship();
 				prevEle.setAttribute("role", "process-previous-link");
 				// Add the previous element to either the list or paragraph
 				// Create the link element
 				Element xrefItem = doc.createElement("xref");
 				xrefItem.setAttribute("linkend", prevTopic.getUniqueLinkId(useFixedUrls));
 				prevEle.appendChild(xrefItem);
 				Element listitemEle = doc.createElement("listitem");
 				listitemEle.appendChild(prevEle);
 				rootEle.appendChild(listitemEle);
 			}
 			// Insert the node after the title node
 			Node nextNode = titleEle.getNextSibling();
 			while (nextNode.getNodeType() != Node.ELEMENT_NODE && nextNode.getNodeType() != Node.COMMENT_NODE && nextNode != null)
 			{
 				nextNode = nextNode.getNextSibling();
 			}
 			doc.getDocumentElement().insertBefore(rootEle, nextNode);
 		}
 	}
 
 	public void processNextRelationshipInjections(final SpecTopic topic, final Document doc, final boolean useFixedUrls)
 	{
 		if (topic.getNextTopicRelationships().isEmpty())
 			return;
 
 		// Attempt to get the previous topic and process it
 		List<TopicRelationship> nextList = topic.getNextTopicRelationships();
 		// Create the paragraph/itemizedlist and list of next relationships.
 		Element rootEle = null;
 		rootEle = doc.createElement("itemizedlist");
 
 		// Create the title
 		Element linkTitleEle = doc.createElement("title");
 		linkTitleEle.setAttribute("role", "process-next-title");
 		if (nextList.size() > 1)
 		{
 			linkTitleEle.setTextContent("Next Steps in ");
 		}
 		else
 		{
 			linkTitleEle.setTextContent("Next Step in ");
 		}
 		Element titleXrefItem = doc.createElement("link");
 		titleXrefItem.setTextContent(topic.getParent().getTitle());
 		titleXrefItem.setAttribute("linkend", topic.getParent().getUniqueLinkId(useFixedUrls));
 		linkTitleEle.appendChild(titleXrefItem);
 		rootEle.appendChild(linkTitleEle);
 
 		for (TopicRelationship next : nextList)
 		{
 			Element nextEle = doc.createElement("para");
 			SpecTopic nextTopic = next.getSecondaryRelationship();
 			nextEle.setAttribute("role", "process-next-link");
 			// Add the next element to either the list or paragraph
 			// Create the link element
 			Element xrefItem = doc.createElement("xref");
 			xrefItem.setAttribute("linkend", nextTopic.getUniqueLinkId(useFixedUrls));
 			nextEle.appendChild(xrefItem);
 			Element listitemEle = doc.createElement("listitem");
 			listitemEle.appendChild(nextEle);
 			rootEle.appendChild(listitemEle);
 		}
 		// Add the node to the end of the XML data
 		doc.getDocumentElement().appendChild(rootEle);
 	}
 
 	/*
 	 * Process's a Content Specs Topic and adds in the prerequisite topic links
 	 */
 	public void processPrerequisiteInjections(final SpecTopic topic, final Document doc, final boolean useFixedUrls)
 	{
 		if (topic.getPrerequisiteRelationships().isEmpty())
 			return;
 
 		// Get the title element so that it can be used later to add the prerequisite topic nodes
 		Element titleEle = null;
 		NodeList titleList = doc.getDocumentElement().getElementsByTagName("title");
 		for (int i = 0; i < titleList.getLength(); i++)
 		{
 			if (titleList.item(i).getParentNode().equals(doc.getDocumentElement()))
 			{
 				titleEle = (Element) titleList.item(i);
 				break;
 			}
 		}
 
 		if (titleEle != null)
 		{
 			// Create the paragraph and list of prerequisites.
 			Element formalParaEle = doc.createElement("formalpara");
 			formalParaEle.setAttribute("role", "prereqs-list");
 			Element formalParaTitleEle = doc.createElement("title");
 			formalParaTitleEle.setTextContent("Prerequisites:");
 			formalParaEle.appendChild(formalParaTitleEle);
 			List<List<Element>> list = new ArrayList<List<Element>>();
 
 			// Add the Topic Prerequisites
 			for (TopicRelationship prereq : topic.getPrerequisiteTopicRelationships())
 			{
 				SpecTopic relatedTopic = prereq.getSecondaryRelationship();
 				list.add(DocbookUtils.buildXRef(doc, relatedTopic.getUniqueLinkId(useFixedUrls)));
 			}
 
 			// Add the Level Prerequisites
 			for (TargetRelationship prereq : topic.getPrerequisiteLevelRelationships())
 			{
 				Level relatedLevel = (Level) prereq.getSecondaryElement();
 				list.add(DocbookUtils.buildXRef(doc, relatedLevel.getUniqueLinkId(useFixedUrls)));
 			}
 
 			// Wrap the items into an itemized list
 			List<Element> items = DocbookUtils.wrapItemizedListItemsInPara(doc, list);
 			for (Element ele : items)
 			{
 				formalParaEle.appendChild(ele);
 			}
 
 			// Add the paragraph and list after the title node
 			Node nextNode = titleEle.getNextSibling();
 			while (nextNode.getNodeType() != Node.ELEMENT_NODE && nextNode.getNodeType() != Node.COMMENT_NODE && nextNode != null)
 			{
 				nextNode = nextNode.getNextSibling();
 			}
 
 			doc.getDocumentElement().insertBefore(formalParaEle, nextNode);
 		}
 	}
 
 	public void processSeeAlsoInjections(final SpecTopic topic, final Document doc, final boolean useFixedUrls)
 	{
 		// Create the paragraph and list of prerequisites.
 		if (topic.getRelatedRelationships().isEmpty())
 			return;
 		Element formalParaEle = doc.createElement("formalpara");
 		formalParaEle.setAttribute("role", "refer-to-list");
 		Element formalParaTitleEle = doc.createElement("title");
 		formalParaTitleEle.setTextContent("See Also:");
 		formalParaEle.appendChild(formalParaTitleEle);
 		List<List<Element>> list = new ArrayList<List<Element>>();
 
 		// Add the Topic Relationships
 		for (TopicRelationship prereq : topic.getRelatedTopicRelationships())
 		{
 			SpecTopic relatedTopic = prereq.getSecondaryRelationship();
 
 			list.add(DocbookUtils.buildXRef(doc, relatedTopic.getUniqueLinkId(useFixedUrls)));
 		}
 
 		// Add the Level Relationships
 		for (TargetRelationship prereq : topic.getRelatedLevelRelationships())
 		{
 			Level relatedLevel = (Level) prereq.getSecondaryElement();
 			list.add(DocbookUtils.buildXRef(doc, relatedLevel.getUniqueLinkId(useFixedUrls)));
 		}
 
 		// Wrap the items into an itemized list
 		List<Element> items = DocbookUtils.wrapItemizedListItemsInPara(doc, list);
 		for (Element ele : items)
 		{
 			formalParaEle.appendChild(ele);
 		}
 
 		// Add the paragraph and list after at the end of the xml data
 		doc.getDocumentElement().appendChild(formalParaEle);
 	}
 
 	public static String processDocumentType(final String xml)
 	{
 		assert xml != null : "The xml parameter can not be null";
 
 		if (XMLUtilities.findDocumentType(xml) == null)
 		{
 			final String preamble = XMLUtilities.findPreamble(xml);
 			final String fixedPreamble = preamble == null ? "" : preamble + "\n";
 			final String fixedXML = preamble == null ? xml : xml.replace(preamble, "");
 
 			return fixedPreamble + "<!DOCTYPE section PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" \"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\" []>\n" + fixedXML;
 		}
 
 		return xml;
 	}
 }
