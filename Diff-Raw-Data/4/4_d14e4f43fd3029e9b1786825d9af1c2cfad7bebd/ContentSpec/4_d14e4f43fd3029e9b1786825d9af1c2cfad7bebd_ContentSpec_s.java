 package com.redhat.contentspec;
 
 /**
  * A class that is used to hold the data for a Content Specification. It holds a basic data(Title, Product, Version, etc...) and
  * the original pre processed text of the Content Specification and all of the levels (Appendixes, Chapters, Sections, etc...) inside the Content Spec.
  */
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.redhat.contentspec.constants.CSConstants;
 import com.redhat.contentspec.entities.BugzillaOptions;
 import com.redhat.contentspec.entities.InjectionOptions;
 import com.redhat.contentspec.enums.LevelType;
 import com.redhat.ecs.commonutils.DocBookUtilities;
 import com.redhat.ecs.commonutils.HashUtilities;
import com.redhat.ecs.commonutils.StringUtilities;
 
 public class ContentSpec extends Node
 {
 	private KeyValueNode<Integer> id = null;
 	private KeyValueNode<String> title = null;
 	private KeyValueNode<String> product = null;
 	private KeyValueNode<String> version = null;
 	private KeyValueNode<String> brand = null;
 	private KeyValueNode<String> subtitle = null;
 	private KeyValueNode<String> edition = null;
 	private KeyValueNode<Integer> pubsNumber = null;
 	private KeyValueNode<String> publicanCfg = null;
 	private ArrayList<String> text = new ArrayList<String>();
 	private KeyValueNode<String> dtd = null;
 	private String createdBy = null;
 	private KeyValueNode<Integer> revision = null;
 	private KeyValueNode<String> checksum = null;
 	private KeyValueNode<String> copyrightHolder = null;
 	private KeyValueNode<String> description = null;
 	private KeyValueNode<InjectionOptions> injectionOptions = null;
 	private KeyValueNode<String> bugzillaProduct = null;
 	private KeyValueNode<String> bugzillaComponent = null;
 	private KeyValueNode<String> bugzillaVersion = null;
 	private KeyValueNode<String> bugzillaURL = null;
 	private KeyValueNode<Boolean> injectBugLinks = null;
 	private KeyValueNode<Boolean> injectSurveyLinks = null;
 	private KeyValueNode<String> locale = null;
 	private KeyValueNode<String> outputStyle = null;
 	private KeyValueNode<Boolean> allowDuplicateTopics = null; 
 	private KeyValueNode<Boolean> allowEmptyLevels = null; 
 	
 	private final LinkedList<Node> nodes = new LinkedList<Node>();
 	private final Level level = new Level("Initial Level", 0, null, LevelType.BASE);
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param title
 	 *            The Title of the Content Specification.
 	 * @param product
 	 *            The Product that the Content Specification documents.
 	 * @param version
 	 *            The Version of the Product that the Content Specification documents.
 	 * @param copyrightHolder
 	 *            The Copyright Holder of the Content Specification and the book it creates.
 	 */
 	public ContentSpec(final String title, final String product, final String version, final String copyrightHolder)
 	{
 		setTitle(title);
 		setProduct(product);
 		setVersion(version);
 		setCopyrightHolder(copyrightHolder);
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param title
 	 *            The title of the Content Specification.
 	 */
 	public ContentSpec(final String title)
 	{
 		setTitle(title);
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param id
 	 *            The Database ID of the Content Specification.
 	 * @param title
 	 *            The title of the Content Specification.
 	 */
 	public ContentSpec(final int id, final String title)
 	{
 		this(title);
 		setId(id);
 	}
 
 	public ContentSpec()
 	{
 	}
 
 	// Start of the basic getter/setter methods for this Scope.
 
 	/**
 	 * Get the base Level of the Content Specification. This level will contain all the other levels in the content specification.
 	 * 
 	 * @return The Base Level object of a Content Specification.
 	 */
 	public Level getBaseLevel()
 	{
 		return level;
 	}
 
 	/**
 	 * Gets the Product that the Content Specification documents.
 	 * 
 	 * @return The name of the product.
 	 */
 	public String getProduct()
 	{
 		return product == null ? "" : product.getValue();
 	}
 
 	/**
 	 * Sets the name of the product that the Content Specification documents.
 	 * 
 	 * @param product
 	 *            The name of the Product.
 	 */
 	public void setProduct(final String product)
 	{
 		if (this.product == null)
 		{
 			this.product = new KeyValueNode<String>("Product", product);
 			nodes.add(this.product);
 		}
 		else
 		{
 			this.product.setValue(product);
 		}
 	}
 
 	/**
 	 * Get the version of the product that the Content Specification documents.
 	 * 
 	 * @return The version of the product or an empty string if the version is null.
 	 */
 	public String getVersion()
 	{
 		return version == null ? "" : version.getValue();
 	}
 
 	/**
 	 * Set the version of the product that the Content Specification documents.
 	 * 
 	 * @param version
 	 *            The product version.
 	 */
 	public void setVersion(final String version)
 	{
 		if (this.version == null)
 		{
 			this.version = new KeyValueNode<String>("Version", version);
 			nodes.add(this.version);
 		}
 		else
 		{
 			this.version.setValue(version);
 		}
 	}
 
 	/**
 	 * Gets the brand of the product that the Content Specification documents.
 	 * 
 	 * @return The brand of the product or null if one doesn't exist.
 	 */
 	public String getBrand()
 	{
 		return brand == null ? null : brand.getValue();
 	}
 
 	/**
 	 * Set the brand of the product that the Content Specification documents.
 	 * 
 	 * @param brand
 	 *            The brand of the product.
 	 */
 	public void setBrand(final String brand)
 	{
 		if (this.brand == null)
 		{
 			this.brand = new KeyValueNode<String>("Brand", brand);
 			appendChild(this.brand);
 		}
 		else
 		{
 			this.brand.setValue(brand);
 		}
 	}
 
 	/**
 	 * Sets the ID of the Content Specification.
 	 * 
 	 * @param id
 	 *            The database ID of the content specification.
 	 */
 	public void setId(final int id)
 	{
 		if (this.id == null)
 		{
 			this.id = new KeyValueNode<Integer>("ID", id);
 			appendChild(this.id);
 			nodes.addFirst(this.id);
 			if (this.id.getParent() != null)
 			{
 				this.id.removeParent();
 			}
 			this.id.setParent(this);
 		}
 		else
 		{
 			this.id.setValue(id);
 		}
 	}
 
 	/**
 	 * Gets the ID of the Content Specification
 	 * 
 	 * @return The Content Specification database ID or 0 if one hasn't been set. 
 	 */
 	public int getId()
 	{
 		return (Integer) (id == null ? 0 : id.getValue());
 	}
 
 	/**
 	 * Gets the title of the Content Specification.
 	 * 
 	 * @return The Content Specification title or an empty string if it is null.
 	 */
 	public String getTitle()
 	{
 		return title == null ? "" : title.getValue();
 	}
 
 	/**
 	 * Gets the escaped version of the title for the Content Specification.
 	 * 
 	 * @return The Content Specification title.
 	 */
 	public String getEscapedTitle()
 	{
 		return DocBookUtilities.escapeTitle(getTitle());
 	}
 
 	/**
 	 * Sets the Content Specifications title.
 	 * 
 	 * @param title
 	 *            The title for the content specification
 	 */
 	public void setTitle(final String title)
 	{
 		if (this.title == null)
 		{
 			this.title = new KeyValueNode<String>("Title", title);
 			appendChild(this.title);
 		}
 		else
 		{
 			this.title.setValue(title);
 		}
 	}
 
 	/**
 	 * Gets the Subtitle for the Content Specification.
 	 * 
 	 * @return The Subtitle of the Content Specification or null if one doesn't exist.
 	 */
 	public String getSubtitle()
 	{
 		return subtitle == null ? null : subtitle.getValue();
 	}
 
 	/**
 	 * Sets the Subtitle for the Content Specification
 	 * 
 	 * @param subtitle
 	 */
 	public void setSubtitle(final String subtitle)
 	{
 		if (this.subtitle == null)
 		{
 			this.subtitle = new KeyValueNode<String>("Subtitle", subtitle);
 			appendChild(this.subtitle);
 		}
 		else
 		{
 			this.subtitle.setValue(subtitle);
 		}
 	}
 
 	/**
 	 * Gets the Edition of the book the Content Specification will create.
 	 * 
 	 * @return The Content Specifications Book Edition or null if one doesn't exist.
 	 */
 	public String getEdition()
 	{
 		return edition == null ? null : edition.getValue();
 	}
 
 	/**
 	 * Set the Edition of the Book the Content Specification represents.
 	 * 
 	 * @param edition
 	 *            The Book Edition.
 	 */
 	public void setEdition(final String edition)
 	{
 		if (this.edition == null)
 		{
 			this.edition = new KeyValueNode<String>("Edition", edition);
 			appendChild(this.edition);
 		}
 		else
 		{
 			this.edition.setValue(edition);
 		}
 	}
 
 	/**
 	 * Get the publication number used by publican
 	 * 
 	 * @return The publication number or null if one doesn't exist.
 	 */
 	public Integer getPubsNumber()
 	{
 		return (Integer) (pubsNumber == null ? null : pubsNumber.getValue());
 	}
 
 	/**
 	 * Set the publication number for the Content Specification.
 	 * 
 	 * @param pubsNumber
 	 *            The publication number.
 	 */
 	public void setPubsNumber(final Integer pubsNumber)
 	{
 		if (this.pubsNumber == null)
 		{
 			this.pubsNumber = new KeyValueNode<Integer>("Pubsnumber", pubsNumber);
 			appendChild(this.pubsNumber);
 		}
 		else
 		{
 			this.pubsNumber.setValue(pubsNumber);
 		}
 	}
 
 	/**
 	 * Gets the data what will be appended to the publican.cfg file when built.
 	 * 
 	 * @return The data to be appended or null if none exist.
 	 */
 	public String getPublicanCfg()
 	{
 		return publicanCfg == null ? null : publicanCfg.getValue();
 	}
 
 	/**
 	 * Set the data that will be appended to the publican.cfg file when built.
 	 * 
 	 * @param publicanCfg
 	 *            The data to be appended.
 	 */
 	public void setPublicanCfg(final String publicanCfg)
 	{
 		if (this.publicanCfg == null)
 		{
 			this.publicanCfg = new KeyValueNode<String>("publican.cfg", publicanCfg);
 			appendChild(this.publicanCfg);
 		}
 		else
 		{
 			this.publicanCfg.setValue(publicanCfg);
 		}
 	}
 
 	/**
 	 * Get the pre processed text of a specific line in the Content Specification.
 	 * 
 	 * @param line
 	 *            The line number of the text.
 	 * @return The text at "line" in the Content Specification or null if the line doesn't exist.
 	 */
 	public String getPreProcessedTextForLine(int line)
 	{
 		return text.size() >= line ? text.get(line - 1) : null;
 	}
 
 	/**
 	 * Gets a list of all the pre processed lines in the Content Specification.
 	 * 
 	 * @return A List of pre processed Content Specification lines.
 	 */
 	public List<String> getPreProcessedText()
 	{
 		return text;
 	}
 
 	/**
 	 * Sets the pre processed text of a content specification for a specific line. If the line doesn't
 	 * exist then a IndexOutOfBoundsException will be thrown.
 	 * 
 	 * @param text
 	 *            The text to be set at "line".
 	 * @param line
 	 *            The line number in the content specification to set the text for.
 	 */
 	public void setPreProcessedTextForLine (final String text, final int line)
 	{
 		this.text.set(line - 1, text);
 	}
 
 	/**
 	 * Get the DTD of the Content Specification. The default value is "Docbook 4.5".
 	 * 
 	 * @return The DTD of the Content Specification or the default value if one isn't set.
 	 */
 	public String getDtd()
 	{
 		return dtd == null ? "Docbook 4.5" : dtd.getValue();
 	}
 
 	/**
 	 * Sets the DTD for a Content Specification.
 	 * 
 	 * @param dtd
 	 *            The DTD of the Content Specification.
 	 */
 	public void setDtd (final String dtd)
 	{
 		if (this.dtd == null)
 		{
 			this.dtd = new KeyValueNode<String>("DTD", dtd);
 			appendChild(this.dtd);
 		}
 		else
 		{
 			this.dtd.setValue(dtd);
 		}
 	}
 
 	/**
 	 * Sets the created by Username for the author who created/uploaded the Content Specification.
 	 * 
 	 * @param username
 	 *            The Username of the User who created/uploaded the Content Specification or null if one doesn't exist.
 	 */
 	public void setCreatedBy(final String username)
 	{
 		this.createdBy = username;
 	}
 
 	/**
 	 * Get the Username of the user who created/uploaded the Content Specification.
 	 * 
 	 * @return The Username of the Content Specification creator.
 	 */
 	public String getCreatedBy()
 	{
 		return createdBy;
 	}
 
 	/**
 	 * Gets the SpecRevision number for the Content Specification. This number is used to validate 
 	 * that the Content Specification hasn't been modified since the last upload.
 	 * 
 	 * @return The SpecRevision number or null if one doesn't exist.
 	 */
 	@Deprecated
 	public Integer getRevision()
 	{
 		return (Integer) (revision == null ? null : revision.getValue());
 	}
 
 	/**
 	 * Sets the SpecRevision number for a ContentSpecification.
 	 * 
 	 * @param revision
 	 *            The SpecRevision number.
 	 */
 	@Deprecated
 	public void setRevision(final int revision)
 	{
 		if (this.revision == null)
 		{
 			this.revision = new KeyValueNode<Integer>("SpecRevision", revision);
 			appendChild(this.revision);
 		}
 		else
 		{
 			this.revision.setValue(revision);
 		}
 	}
 
 	/**
 	 * Get the set Checksum value for a Content Specification.
 	 * 
 	 * Note: This function doesn't calculate the Checksum of a Content Specification.
 	 * 
 	 * @return The set value of the Content Specifications Checksum or null if one doesn't exist.
 	 */
 	public String getChecksum()
 	{
 		return checksum == null ? null : checksum.getValue();
 	}
 
 	/**
 	 * Set the Checksum value for a Content Specification.
 	 * 
 	 * Note: This value isn't used by the toString() function as it re calculates the Checksum.
 	 * 
 	 * @param checksum
 	 *            The Checksum of the Content Specification.
 	 */
 	public void setChecksum(final String checksum)
 	{
 		if (this.checksum == null)
 		{
 			this.checksum = new KeyValueNode<String>("CHECKSUM", checksum);
 			appendChild(this.checksum);
 		}
 		else
 		{
 			this.checksum.setValue(checksum);
 		}
 	}
 
 	/**
 	 * Gets the Abstract (or Description) for a Content Specification that will be added to a book when built.
 	 * 
 	 * @return The abstract for the Content Specification or null if one doesn't exist.
 	 */
 	public String getAbstract()
 	{
 		return description == null ? null : description.getValue();
 	}
 
 	/**
 	 * Sets the Abstract (or Description) for a Content Specification.
 	 * 
 	 * @param description
 	 *            The Abstract for the Content Specifications book.
 	 */
 	public void setAbstract(final String description)
 	{
 		if (this.description == null)
 		{
 			this.description = new KeyValueNode<String>("Abstract", description);
 			appendChild(this.description);
 		}
 		else
 		{
 			this.description.setValue(description);
 		}
 	}
 
 	/**
 	 * Get the Copyright Holder of the Content Specification and the book it creates.
 	 * 
 	 * @return The name of the Copyright Holder.
 	 */
 	public String getCopyrightHolder()
 	{
 		return copyrightHolder == null ? "" : copyrightHolder.getValue();
 	}
 
 	/**
 	 * Set the Copyright Holder of the Content Specification and the book it creates.
 	 * 
 	 * @param copyrightHolder
 	 *            The name of the Copyright Holder.
 	 */
 	public void setCopyrightHolder(final String copyrightHolder)
 	{
 		if (this.copyrightHolder == null)
 		{
 			this.copyrightHolder = new KeyValueNode<String>("Copyright Holder", copyrightHolder);
 			appendChild(this.copyrightHolder);
 		}
 		else
 		{
 			this.copyrightHolder.setValue(copyrightHolder);
 		}
 	}
 
 	/**
 	 * Gets the injection Options for the Content Specification that will be used when building a book.
 	 * 
 	 * @return A InjectionOptions object containing the Injection Options to be used when building.
 	 */
 	public InjectionOptions getInjectionOptions()
 	{
 		return injectionOptions == null ? null : injectionOptions.getValue();
 	}
 
 	/**
 	 * Sets the InjectionOptions that will be used by the Builder when building a book.
 	 * 
 	 * @param injectionOptions
 	 *            The InjectionOptions to be used when building a book.
 	 */
 	public void setInjectionOptions(final InjectionOptions injectionOptions)
 	{
 		if (this.injectionOptions == null)
 		{
 			this.injectionOptions = new KeyValueNode<InjectionOptions>("Inline Injection", injectionOptions);
 			appendChild(this.injectionOptions);
 		}
 		else
 		{
 			this.injectionOptions.setValue(injectionOptions);
 		}
 	}
 
 	/**
 	 * Sets the description for the global tags.
 	 * 
 	 * @param desc
 	 *            The description.
 	 */
 	public void setDescription(final String desc)
 	{
 		level.setDescription(desc);
 	}
 
 	/**
 	 * Get the description that will be applied globally.
 	 * 
 	 * @return The description as a String
 	 */
 	public String getDescription()
 	{
 		return level.getDescription(false);
 	}
 
 	/**
 	 * Gets the locale of the Content Specification.
 	 * 
 	 * @return The Content Specification locale.
 	 */
 	public String getLocale()
 	{
 		return locale == null ? null : locale.getValue();
 	}
 
 	/**
 	 * Sets the Content Specifications locale.
 	 * 
 	 * @param locale
 	 *            The locale for the content specification
 	 */
 	public void setLocale(final String locale)
 	{
 		if (this.locale == null)
 		{
 			this.locale = new KeyValueNode<String>("Translation Locale", locale);
 			appendChild(this.locale);
 		}
 		else
 		{
 			this.locale.setValue(locale);
 		}
 	}
 
 	/**
 	 * Gets the output style for the Content Specification. The default is CSP.
 	 * 
 	 * @return The Content Specification output style.
 	 */
 	public String getOutputStyle()
 	{
 		return outputStyle == null ? CSConstants.CSP_OUTOUT_FORMAT : outputStyle.getValue();
 	}
 
 	/**
 	 * Sets the Content Specifications output style.
 	 * 
 	 * @param outputStyle
 	 *            The output style for the content specification
 	 */
 	public void setOutputStyle(final String outputStyle)
 	{
 		if (this.outputStyle == null)
 		{
 			this.outputStyle = new KeyValueNode<String>("Output Style", outputStyle);
 			appendChild(this.outputStyle);
 		}
 		else
 		{
 			this.outputStyle.setValue(outputStyle);
 		}
 	}
 
 	/**
 	 * Sets the Assigned Writer for the global tags.
 	 * 
 	 * @param writer
 	 *            The writers name that matches to the assigned writer tag in the database
 	 */
 	public void setAssignedWriter(final String writer)
 	{
 		level.setAssignedWriter(writer);
 	}
 
 	/**
 	 * Gets the Assigned Writer that will be applied globally.
 	 * 
 	 * @return The Assigned Writers name as a String
 	 */
 	public String getAssignedWriter()
 	{
 		return level.getAssignedWriter(false);
 	}
 
 	/**
 	 * Sets the set of tags for the global tags
 	 * 
 	 * @param tags
 	 *            A List of tags by their name.
 	 */
 	public void setTags(final List<String> tags)
 	{
 		level.setTags(tags);
 	}
 
 	/**
 	 * Gets the set of tags for the global tags.
 	 * 
 	 * @return A list of tag names
 	 */
 	public List<String> getTags()
 	{
 		return level.getTags(false);
 	}
 
 	/**
 	 * Sets the list of tags that are to be removed in the global options.
 	 * 
 	 * @param tags
 	 *            An ArrayList of tags to be removed
 	 */
 	public void setRemoveTags(List<String> tags)
 	{
 		level.setRemoveTags(tags);
 	}
 
 	/**
 	 * Gets an ArrayList of tags that are to be removed globally.
 	 * 
 	 * @return An ArrayList of tags
 	 */
 	public List<String> getRemoveTags()
 	{
 		return level.getRemoveTags(false);
 	}
 
 	/**
 	 * Sets the list of source urls to be applied globally
 	 * 
 	 * @param sourceUrls
 	 *            A List of urls
 	 */
 	public void setSourceUrls(final List<String> sourceUrls)
 	{
 		level.setSourceUrls(sourceUrls);
 	}
 
 	/**
 	 * Get the Source Urls that are to be applied globally.
 	 * 
 	 * @return A List of Strings that represent the source urls
 	 */
 	public List<String> getSourceUrls()
 	{
 		return level.getSourceUrls();
 	}
 
 	public Boolean getAllowDuplicateTopics()
 	{
 		return (Boolean) (allowDuplicateTopics == null ? false : allowDuplicateTopics.getValue());
 	}
 
 	public void setAllowDuplicateTopics(final Boolean allowDuplicateTopics)
 	{
 		if (this.allowDuplicateTopics == null)
 		{
 			this.allowDuplicateTopics = new KeyValueNode<Boolean>("Duplicate Topics", allowDuplicateTopics);
 			appendChild(this.allowDuplicateTopics);
 		}
 		else
 		{
 			this.allowDuplicateTopics.setValue(allowDuplicateTopics);
 		}
 	}
 
 	public Boolean getAllowEmptyLevels()
 	{
 		return (Boolean) (allowEmptyLevels == null ? false : allowEmptyLevels.getValue());
 	}
 
 	public void setAllowEmptyLevels(final Boolean allowEmptyLevels)
 	{
 		if (this.allowEmptyLevels == null)
 		{
 			this.allowEmptyLevels = new KeyValueNode<Boolean>("Allow Empty Levels", allowEmptyLevels);
 			appendChild(this.allowEmptyLevels);
 		}
 		else
 		{
 			this.allowEmptyLevels.setValue(allowEmptyLevels);
 		}
 	}
 
 	/**
 	 * Adds a tag to the global list of tags. If the tag starts with a - then its added to the remove tag list otherwise its added
 	 * to the normal tag mapping. Also strips off + & - from the start of tags.
 	 * 
 	 * @param tagName
 	 *            The name of the Tag to be added.
 	 * @return True if the tag was added successfully otherwise false.
 	 */
 	public boolean addTag(final String tagName)
 	{
 		return level.addTag(tagName);
 	}
 
 	/**
 	 * Adds a list of tags to the global list of tags
 	 * 
 	 * @param tagArray
 	 *            A list of tags by name that are to be added.
 	 * @return True if all the tags were added successfully otherwise false.
 	 */
 	public boolean addTags(final List<String> tagArray)
 	{
 		return level.addTags(tagArray);
 	}
 
 	/**
 	 * Adds a source URL to the list of global URL's
 	 * 
 	 * @param url
 	 *            The URL to be added
 	 */
 	public void addSourceUrl(final String url)
 	{
 		level.addSourceUrl(url);
 	}
 
 	/**
 	 * Removes a specific Source URL from the list of global URL's
 	 * 
 	 * @param url
 	 *            The URL to be removed.
 	 */
 	public void removeSourceUrl(final String url)
 	{
 		level.removeSourceUrl(url);
 	}
 
 	/**
 	 * Adds a Chapter to the Content Specification. If the Chapter already has a parent, then it is removed from that parent
 	 * and added to this level.
 	 * 
 	 * @param chapter
 	 *            A Chapter to be added to the Content Specification.
 	 */
 	public void appendChapter(final Chapter chapter)
 	{
 		level.appendChild(chapter);
 	}
 
 	/**
 	 * Adds a Part to the Content Specification. If the Part already has a parent, then it is removed from that parent
 	 * and added to this level.
 	 * 
 	 * @param chapter
 	 *            The Chapter to be removed from the Content Specification.
 	 */
 	public void appendPart(final Part part)
 	{
 		level.appendChild(part);
 	}
 
 	/**
 	 * Removes a Chapter from the Content Specification and removes the Content Specification as the Chapters parent.
 	 * 
 	 * @param chapter The Chapter to be removed from the Content Specification.
 	 */
 	public void removeChapter(final Chapter chapter)
 	{
 		level.appendChild(chapter);
 	}
 
 	/**
 	 * Gets a ordered linked list of the child nodes within the Content Specification. This includes comments and chapters.
 	 * 
 	 * @return The ordered list of nodes for the Content Specification.
 	 */
 	public LinkedList<Node> getChildNodes()
 	{
 		return level.getChildNodes();
 	}
 
 	/**
 	 * Gets the number of Chapters in the Content Specification.
 	 * 
 	 * @return The number of Child Levels
 	 */
 	public int getNumberOfChapters()
 	{
 		return level.getNumberOfChildLevels();
 	}
 
 	/**
 	 * Gets a List of all the chapters in this level.
 	 * 
 	 * Note: The Chapters may not be in order.
 	 * 
 	 * @return A List of Chapters.
 	 */
 	public List<Level> getChapters()
 	{
 		return level.getChildLevels();
 	}
 
 	/**
 	 * Appends a Comment to the Content Specification.
 	 * 
 	 * @param comment
 	 *            The comment node to be appended to the Content Specification.
 	 */
 	public void appendComment(final Comment comment)
 	{
 		nodes.add(comment);
 		if (comment.getParent() != null)
 		{
 			comment.removeParent();
 		}
 		comment.setParent(this);
 	}
 
 	/**
 	 * Creates and appends a Comment node to the Content Specification.
 	 * 
 	 * @param comment
 	 *            The Comment to be appended.
 	 */
 	public void appendComment(final String comment)
 	{
 		appendComment(new Comment(comment));
 	}
 
 	/**
 	 * Removes a Comment from the Content Specification.
 	 * 
 	 * @param comment
 	 *            The Comment node to be removed.
 	 */
 	public void removeComment(final Comment comment)
 	{
 		nodes.remove(comment);
 		comment.removeParent();
 	}
 
 	// End of the basic getter/setter methods for this ContentSpec.
 
 	/**
 	 * Appends a pre processed line to the list of lines in the Content Specification.
 	 * 
 	 * @param line
 	 *            The Line to be added.
 	 */
 	public void appendPreProcessedLine(final String line)
 	{
 		this.text.add(line);
 	}
 
 	/**
 	 * Appends a String to a pre processed line that has already been added to the Content Specification.
 	 * Throws a IndexOutOfBoundsException if the line doesn't exist.
 	 * 
 	 * @param text
 	 *            The text to be appended.
 	 * @param line
 	 *            The line number of the original text that the new text should be appended to.
 	 */
 	public void appendPreProcessedLineText (final String text, final int line)
 	{
 		String temp = this.text.get(line-1) + text;
 		this.text.set(line-1, temp);
 	}
 
 	public List<SpecTopic> getSpecTopics()
 	{
 		return getLevelSpecTopics(level);
 	}
 
 	private List<SpecTopic> getLevelSpecTopics(final Level level)
 	{
 		final List<SpecTopic> specTopics = level.getSpecTopics();
 		for (final Level childLevel: level.getChildLevels())
 		{
 			specTopics.addAll(getLevelSpecTopics(childLevel));
 		}
 		return specTopics;
 	}
 
 	public String getBugzillaProduct()
 	{
 		return bugzillaProduct == null ? null : bugzillaProduct.getValue().toString();
 	}
 
 	public void setBugzillaProduct(final String bugzillaProduct)
 	{
 		if (this.bugzillaProduct == null)
 		{
 			this.bugzillaProduct = new KeyValueNode<String>("BZProduct", bugzillaProduct);
 			appendChild(this.bugzillaProduct);
 		}
 		else
 		{
 			this.bugzillaProduct.setValue(bugzillaProduct);
 		}
 	}
 
 	public String getBugzillaComponent()
 	{
 		return bugzillaComponent == null ? null : bugzillaComponent.getValue().toString();
 	}
 
 	public void setBugzillaComponent(final String bugzillaComponent)
 	{
 		if (this.bugzillaComponent == null)
 		{
 			this.bugzillaComponent = new KeyValueNode<String>("BZComponent", bugzillaComponent);
 			appendChild(this.bugzillaComponent);
 		}
 		else
 		{
 			this.bugzillaComponent.setValue(bugzillaComponent);
 		}
 	}
 
 	public String getBugzillaVersion()
 	{
 		return bugzillaVersion == null ? null : bugzillaVersion.getValue().toString();
 	}
 
 	public void setBugzillaVersion(final String bugzillaVersion)
 	{
 		if (this.bugzillaVersion == null)
 		{
 			this.bugzillaVersion = new KeyValueNode<String>("BZVersion", bugzillaVersion);
 			appendChild(this.bugzillaVersion);
 		}
 		else
 		{
 			this.bugzillaVersion.setValue(bugzillaVersion);
 		}
 	}
 
 	/**
 	 * Get the URL component that is used in the .ent file when
 	 * building the Docbook files.
 	 * 
 	 * @return The BZURL component for the content specification.
 	 */
 	public String getBugzillaURL()
 	{
 		return bugzillaURL == null ? null : bugzillaURL.getValue().toString();
 	}
 
 	/**
 	 * Set the URL component that is used in the .ent file when
 	 * building the Docbook files.
 	 * 
 	 * @param bugzillaURL
 	 *            The BZURL component to be used when building.
 	 */
 	public void setBugzillaURL(final String bugzillaURL)
 	{
 		if (this.bugzillaURL == null)
 		{
 			this.bugzillaURL = new KeyValueNode<String>("BZURL", bugzillaURL);
 			appendChild(this.bugzillaURL);
 		}
 		else
 		{
 			this.bugzillaURL.setValue(bugzillaURL);
 		}
 	}
 
 	public boolean isInjectBugLinks()
 	{
 		return (Boolean) (injectBugLinks == null ? true : injectBugLinks.getValue());
 	}
 
 	public void setInjectBugLinks(boolean injectBugLinks)
 	{
 		if (this.injectBugLinks == null)
 		{
 			this.injectBugLinks = new KeyValueNode<Boolean>("Bug Links", injectBugLinks);
 			appendChild(this.injectBugLinks);
 		}
 		else
 		{
 			this.injectBugLinks.setValue(injectBugLinks);
 		}
 	}
 
 	public boolean isInjectSurveyLinks()
 	{
 		return (Boolean) (injectSurveyLinks == null ? false : injectSurveyLinks.getValue());
 	}
 
 	public void setInjectSurveyLinks(boolean injectSurveyLinks)
 	{
 		if (this.injectSurveyLinks == null)
 		{
 			this.injectSurveyLinks = new KeyValueNode<Boolean>("Survey Links", injectSurveyLinks);
 			appendChild(this.injectSurveyLinks);
 		}
 		else
 		{
 			this.injectSurveyLinks.setValue(injectSurveyLinks);
 		}
 	}
 
 	public BugzillaOptions getBugzillaOptions()
 	{
 		final BugzillaOptions bzOption = new BugzillaOptions();
 		bzOption.setProduct(getBugzillaProduct());
 		bzOption.setComponent(getBugzillaComponent());
 		bzOption.setVersion(getBugzillaVersion());
 		bzOption.setUrlComponent(getBugzillaURL());
 		bzOption.setBugzillaLinksEnabled(isInjectBugLinks());
 		return bzOption;
 	}
 	
 	/**
 	 * Adds a Child Level to the Level. If the Child Level already has a parent, then it is removed from that parent
 	 * and added to this level.
 	 * 
 	 * @param childLevel A Child Level to be added to the Level.
 	 */
 	protected void appendChild(final Node child)
 	{
 		nodes.add(child);
 		if (child.getParent() != null)
 		{
 			child.removeParent();
 		}
 		child.setParent(this);
 	}
 
 	/**
 	 * Removes a child node from the content spec and removes the content as the childs parent.
 	 * 
 	 * @param child The Child Node to be removed from the Content Spec.
 	 */
 	protected void removeChild(final Node child)
 	{
 		nodes.remove(child);
 		child.setParent(null);
 	}
 
 	/**
 	 * Returns a String representation of the Content Specification.
 	 */
 	@SuppressWarnings("rawtypes")
 	@Override
 	public String toString()
 	{
 		final StringBuilder output = new StringBuilder();
 		for (final Node node : nodes)
 		{
 			if (node instanceof KeyValueNode)
 			{
 				final KeyValueNode keyValueNode = (KeyValueNode) node;
 				if (!keyValueNode.getKey().equals("CHECKSUM"))
 				{
 					output.append(node.toString() + "\n");
 				}
 			}
 			else
 			{
 				output.append(node.toString() + "\n");
 			}
 		}
 		
 		/*for (Comment baseComment: baseComments) {
 			output += baseComment.toString();
 		}
 		String bugzillaDetails = "Bug Links = " + (injectBugLinks ? "On" : "Off") + "\n";
 		bugzillaDetails += (bugzillaProduct == null ? "" : ("BZPRODUCT = " + bugzillaProduct + "\n")) +
 				(bugzillaComponent == null ? "" : ("BZCOMPONENT = " + bugzillaComponent + "\n")) +
 				(bugzillaVersion == null ? "" : ("BZVERSION = " + bugzillaVersion + "\n")) +
 				(bugzillaURL == null ? "" : ("BZURL = " + bugzillaURL + "\n"));
 		
 		String allowDetails = (allowDuplicateTopics == null || allowDuplicateTopics ? "" : ("Duplicate Topics = Off\n")) +
 				(allowEmptyLevels == null  || !allowEmptyLevels ? "" : ("Allow Empty Levels = " + allowEmptyLevels.toString() + "\n"));
 		
 		output += "Title = " + (title == null ? "" : title) + "\n" + 
 				"Product = " + (product == null ? "" : product) + "\n" + 
 				"Version = " + (version == null ? "" : version) + "\n" + 
 				"Copyright Holder = " + (copyrightHolder == null ? "" : copyrightHolder) + "\n" +
 				"DTD = " + (dtd == null ? "" : dtd) + "\n" +
 				(subtitle == null ? "" : ("Subtitle = " + subtitle + "\n")) +
 				(edition == null ? "" : ("Edition = " + edition + "\n")) +
 				(brand == null ? "" : ("Brand = " + brand + "\n")) +
 				(pubsNumber == null ? "" : ("Pubsnumber = " + pubsNumber + "\n")) +
 				(description == null ? "" : ("Abstract = " + description + "\n")) +
 				(allowDetails.isEmpty() ? "" : allowDetails) +
 				(outputStyle == null ? "" : ("Output Style = " + outputStyle + "\n")) +
 				(locale == null ? "" : ("Translation Locale = " + locale + "\n")) +
 				"\n" + bugzillaDetails +
 				(injectSurveyLinks ? "Survey Links = On\n" : "") + 
 				(publicanCfg == null ? "" : ("\npublican.cfg = [" + publicanCfg + "]\n"));
 		
 		// Add the injection options if they exist
 		if (injectionOptions != null && injectionOptions.getStrictTopicTypes() != null 
 				&& !injectionOptions.getStrictTopicTypes().isEmpty() && injectionOptions.getContentSpecType() != null) {
 			output += "Inline Injection = ";
 			if (injectionOptions.getContentSpecType() == InjectionOptions.UserType.STRICT) {
 				output += "on [" + StringUtilities.buildString(injectionOptions.getStrictTopicTypes().toArray(new String[0]), ", ") + "]";
 			} else if (injectionOptions.getContentSpecType() == InjectionOptions.UserType.ON) {
 				output += "on";
 			} else if (injectionOptions.getContentSpecType() == InjectionOptions.UserType.OFF) {
 				output += "off";
 			}
 			output += "\n";
 		}*/
 		
 		// Append a new line to separate the metadata from content
 		output.append("\n");
 
 		// Add any global options
 		String options = level.getOptionsString();
 		if (!options.equals(""))
 		{
 			output.append("[" + options + "]\n");
 		}
 
 		// Append the String representation of each level
 		output.append(level.toString());
 
 		// If the id isn't null then add the id and checksum
 		if (getId() != 0)
 		{
 			output.insert(0, "CHECKSUM=" + HashUtilities.generateMD5("ID = " + id + "\n" + output) + "\n" + "ID = " + id + "\n");
 		}
		return output.roString();
 	}
 
 	@Override
 	public Integer getStep()
 	{
 		return null;
 	}
 	
 	@Override
 	protected void removeParent()
 	{
 		
 	}
 }
