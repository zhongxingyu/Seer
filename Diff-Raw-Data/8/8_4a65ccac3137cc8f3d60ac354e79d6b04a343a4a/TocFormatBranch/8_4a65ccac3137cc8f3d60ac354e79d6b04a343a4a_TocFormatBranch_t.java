 package com.redhat.topicindex.component.docbookrenderer.structures.tocformat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.redhat.ecs.commonutils.XMLUtilities;
 import com.redhat.ecs.constants.CommonConstants;
 import com.redhat.topicindex.component.docbookrenderer.structures.TopicErrorDatabase;
 import com.redhat.topicindex.rest.entities.TagV1;
 import com.redhat.topicindex.rest.entities.TopicV1;
 
 public class TocFormatBranch
 {
 	/** defines the parent of this branch */
 	private final TocFormatBranch parent;
 
 	/**
 	 * Defines the tag that this branch represents. Will be null for a top level
 	 * branch
 	 */
 	private final TagV1 tag;
 
 	/** Defines the tags that a child must have to exist as a subbranch */
 	private final TagRequirements childTags;
 
 	/**
 	 * Defines the tags that a topic must have to be listed under this branch.
 	 * This is null if no topics are to be listed under this branch.
 	 */
 	private final TagRequirements displayTags;
 
 	/** Holds any sub branches */
 	private final List<TocFormatBranch> children = new ArrayList<TocFormatBranch>();
 
 	/** Holds any topics that should be listed under this branch */
 	private final Map<TopicV1, Document> topics = new HashMap<TopicV1, Document>();
 
 	public Map<TopicV1, Document> getTopics()
 	{
 		return topics;
 	}
 
 	public TocFormatBranch getParent()
 	{
 		return parent;
 	}
 
 	public List<TocFormatBranch> getChildren()
 	{
 		return children;
 	}
 
 	public TagRequirements getDisplayTags()
 	{
 		return displayTags;
 	}
 
 	public TagRequirements getTagCategoryId()
 	{
 		return childTags;
 	}
 
 	public TagV1 getTag()
 	{
 		return tag;
 	}
 
 	public TocFormatBranch()
 	{
 		this.parent = null;
 		this.tag = null;
 		this.childTags = new TagRequirements();
 		this.displayTags = new TagRequirements();
 	}
 
 	public TocFormatBranch(final TagV1 tag, final TocFormatBranch parent, final TagRequirements childTags, final TagRequirements displayTags)
 	{
 		this.tag = tag;
 		this.childTags = childTags == null ? new TagRequirements() : childTags;
 		this.displayTags = displayTags == null ? new TagRequirements() : displayTags;
 		this.parent = parent;
 	}
 
 	public void getDisplayTagsWithParent(final TagRequirements requirements)
 	{
 		requirements.merge(displayTags);
 		requirements.merge(childTags);
 		if (parent != null)
 			parent.getDisplayTagsWithParent(requirements);
 	}
 
 	public String getTOCBranchID()
 	{
 		final StringBuilder retValue = new StringBuilder();
 		
 		if (parent != null)
 			retValue.append(parent.getTOCBranchID());
 		
 		if (this.tag != null)
 		{
 			retValue.append("-");
 			retValue.append(this.tag.getId());
 		}
 		
 		return retValue.toString();
 	}
 
 	public String buildDocbook(final boolean useFixedUrls, final TopicErrorDatabase errorDatabase)
 	{
 		final StringBuilder docbook = new StringBuilder();
 		
 		if (this.parent == null)
 		{
 			if (this.getTopicCount() != 0)
 			{
 				buildDocbookContents(docbook, useFixedUrls, errorDatabase);
 			}
 			else
 			{
 				docbook.append("<chapter>");
 				docbook.append("<title>");
 				docbook.append("Error");
 				docbook.append("</title>");
 				docbook.append("<para>");
 				docbook.append("No Content");
 				docbook.append("</para>");
 				docbook.append("</chapter>");
 			}
 		}
 		else if (this.getTopicCount() != 0)
 		{
 			/* If this is a top level toc element (i.e. its parent has no parent) then build it as a chapter */
 			if (this.parent.parent == null)
 			{
 				docbook.append("<chapter>");
 				docbook.append("<title>");
 				docbook.append(this.getTag() == null ? "" : this.getTag().getName());
 				docbook.append("</title>");
 				
 				buildDocbookContents(docbook, useFixedUrls, errorDatabase);
 				
 				docbook.append("</chapter>");
 			}
 			else
 			{
 				docbook.append("<section>");
 				docbook.append("<title>");
 				docbook.append(this.getTag() == null ? "" : this.getTag().getName());
 				docbook.append("</title>");
 				
 				buildDocbookContents(docbook, useFixedUrls, errorDatabase);
 				
 				docbook.append("</section>");
 			}
 		}
 
 		return docbook.toString();
 	}
 
 	private void buildDocbookContents(final StringBuilder docbook, final boolean useFixedUrls, final TopicErrorDatabase errorDatabase)
 	{
 		/* append any child branches */
 		for (final TocFormatBranch child : children)
 			docbook.append(child.buildDocbook(useFixedUrls, errorDatabase));
 	
 		/* Add an xref to each topic that appears under this branch */
 		for (final TopicV1 topic : topics.keySet())
 		{
 			String fileName = "";
 			if (useFixedUrls)
 			{
 				fileName = topic.getXrefPropertyOrId(CommonConstants.FIXED_URL_PROP_TAG_ID) + this.getTOCBranchID() + ".xml";
 			}
 			else
 			{
 				fileName = "Topic" + topic.getId() + this.getTOCBranchID() + ".xml";
 			}
 	
 			docbook.append("<xi:include href=\"" + fileName + "\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n");	
 		}
 	}
 
 	public int getTopicCount()
 	{
 		int retValue = this.topics.size();
 
 		for (final TocFormatBranch child : children)
 			retValue += child.getTopicCount();
 
 		return retValue;
 	}
 
 	public List<TopicV1> getAllTopics()
 	{
 		final List<TopicV1> retValue = new ArrayList<TopicV1>();
 
 		retValue.addAll(this.topics.keySet());
 
 		for (final TocFormatBranch child : children)
 			retValue.addAll(child.getAllTopics());
 
 		return retValue;
 	}
 
 	public boolean isInToc(final Integer topicId)
 	{
 		final List<TopicV1> topics = getAllTopics();
 		for (final TopicV1 topic : topics)
 			if (topic.getId().equals(topicId))
 				return true;
 		return false;
 	}
 
 	/**
 	 * When inserting an xref to a topic, we need to find the closest topic to
 	 * link to. This is because a topic can appear multiple times in a TOC, and
 	 * therefore multiple times in a document.
 	 * 
 	 * @param topicId
 	 *            The topic to find
 	 * @param referenceTopic
 	 *            The topic to use a search reference point
 	 * @return the xref postfix of the toc that is applied to the topic
 	 */
 	public String getClosestTopicXrefPostfix(final Integer topicId, final TopicV1 referenceTopic)
 	{
 		TocFormatBranch branch = this.getBranchThatContainsTopic(referenceTopic);
 
 		while (branch != null)
 		{
 			/* Search that branch first */
 			final TopicV1 topicInBranch = branch.getTopicInBranchAndChildren(topicId);
 			if (topicInBranch != null)
			{
				final TocFormatBranch branchThatContainsTopic = branch.getBranchThatContainsTopic(referenceTopic);
				if (branchThatContainsTopic != null)
					return branchThatContainsTopic.getTOCBranchID();
				else
					return null;
			}
 
 			/* go up to the parent and try again */
 			branch = branch.getParent();
 		}
 
 		return null;
 	}
 
 	public TocFormatBranch getBranchThatContainsTopic(final TopicV1 topic)
 	{
 		if (this.children.contains(topic))
 			return this;
 		
 		for (final TocFormatBranch child : children)
 		{
 			if (child.getBranchThatContainsTopic(topic) != null)
 			{
 				return child;
 			}
 		}
 		
 		return null;
 	}
 
 	public TopicV1 getTopicInBranchAndChildren(final Integer topicId)
 	{
 		for (final TopicV1 topic : this.topics.keySet())
 			if (topicId.equals(topic.getId()))
 				return topic;
 
 		for (final TocFormatBranch child : children)
 		{
 			final TopicV1 retValue = child.getTopicInBranchAndChildren(topicId);
 			if (retValue != null)
 				return retValue;
 		}
 
 		return null;
 	}
 
 	public void addTopicsToZIPFile(final HashMap<String, byte[]> files, final boolean useFixedUrls)
 	{
 		for (final TopicV1 topic : this.topics.keySet())
 		{
 			if (useFixedUrls)
 			{				
 				files.put("Book/en-US/" + topic.getXrefPropertyOrId(CommonConstants.FIXED_URL_PROP_TAG_ID) + this.getTOCBranchID() + ".xml", XMLUtilities.convertDocumentToString(this.topics.get(topic), "UTF-8").getBytes());
 			}
 		}
 		
 		for (final TocFormatBranch child : children)
 			child.addTopicsToZIPFile(files, useFixedUrls);
 	}
 	
 	public Document getXMLDocument(final TopicV1 topic)
 	{
 		if (this.topics.containsKey(topic))
 			return this.topics.get(topic);
 		
 		for (final TocFormatBranch child : children)
 		{
 			final Document doc = child.getXMLDocument(topic);
 			if (doc != null)
 				return doc;
 		}
 		
 		return null;
 	}
 	
 	public void setUniqueIds()
 	{
 		for (final Document doc : this.topics.values())
 			fixNodeId(doc);
 		
 		for (final TocFormatBranch child : children)
 			child.setUniqueIds();
 	}
 	
 	private void fixNodeId(final Node node)
 	{
 		final NamedNodeMap attributes = node.getAttributes();
 		if (attributes != null)
 		{
 			final Node idAttribute = attributes.getNamedItem("id");
 			if (idAttribute != null)
 			{
 				final String idAttibuteValue = idAttribute.getNodeValue();
 				final String fixedIdAttribute = idAttibuteValue + this.getTOCBranchID();
 				idAttribute.setNodeValue(fixedIdAttribute);
 			}
 		}
 
 		final NodeList elements = node.getChildNodes();
 		for (int i = 0; i < elements.getLength(); ++i)
 			fixNodeId(elements.item(i));
 	}
 }
