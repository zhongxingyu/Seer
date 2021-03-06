 /**
  * This file is part of the Harmony package.
  *
  * (c) Mickael Gaillard <mickael.gaillard@tactfactory.com>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 package com.tactfactory.mda.template;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.Namespace;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import com.tactfactory.mda.annotation.Column.Type;
 import com.tactfactory.mda.meta.ApplicationMetadata;
 import com.tactfactory.mda.meta.ClassMetadata;
 import com.tactfactory.mda.meta.FieldMetadata;
 import com.tactfactory.mda.meta.TranslationMetadata;
 import com.tactfactory.mda.meta.TranslationMetadata.Group;
 import com.tactfactory.mda.plateforme.BaseAdapter;
 import com.tactfactory.mda.utils.ConsoleUtils;
 import com.tactfactory.mda.utils.TactFileUtils;
 import com.tactfactory.mda.utils.PackageUtils;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * Generate the CRUD activities.
  */
 public class ActivityGenerator extends BaseGenerator {
 	/** "template". */
 	private static final String LOWER_TEMPLATE = "template";
 	/** "Template". */
 	private static final String TEMPLATE = "Template";
 	/** "name". */
 	private static final String NAME = "name";
 	
 	/** The local namespace. */
 	private String localNameSpace;
 	
 	/** Are the entities writable ? */
 	private boolean isWritable = true;
 	
 	/** Has the project a date ? */ 
 	private boolean isDate;
 	
 	/** Has the project a time ? */
 	private boolean isTime;
 	
 	/**
 	 * Constructor.
 	 * @param adapter The adapter to use
 	 * @throws Exception 
 	 */
 	public ActivityGenerator(final BaseAdapter adapter) throws Exception {
 		this(adapter, true);
 		
 		// Make entities
 		for (final ClassMetadata meta 
 				: this.getAppMetas().getEntities().values()) {
 			if (!meta.getFields().isEmpty() && !meta.isInternal()) {
 				// copy Widget
 				if (this.isDate && this.isTime) {
 					break;
 				} else {
 					for (final FieldMetadata field 
 							: meta.getFields().values()) {
 						final String type = field.getType();
 						if (!this.isDate && (
 								type.equals(Type.DATE.getValue())  
 								|| type.equals(Type.DATETIME.getValue()))) {
 							this.isDate = true;
 						}
 						
 						if (!this.isTime && (
 								type.equals(Type.TIME.getValue())  
 								|| type.equals(Type.DATETIME.getValue()))) {
 							this.isTime = true;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Constructor.
 	 * @param adapter The adapter to use.
 	 * @param writable Are the entities writable ? (default to true)
 	 * @throws Exception 
 	 */
 	public ActivityGenerator(final BaseAdapter adapter,
 			final Boolean writable) throws Exception {
 		super(adapter);
 
 		this.isWritable = writable;
 		this.setDatamodel(
 				ApplicationMetadata.INSTANCE.toMap(this.getAdapter()));
 	}
 	
 	/**
 	 * Generate all activities for every entity.
 	 */
 	public final void generateAll() {
 		ConsoleUtils.display(">> Generate CRUD view...");
 
 		for (final ClassMetadata cm 
 				: this.getAppMetas().getEntities().values()) {
 			if (!cm.isInternal() && !cm.getFields().isEmpty()) {
 				cm.makeString("label");
 				this.getDatamodel().put(
 						TagConstant.CURRENT_ENTITY, cm.getName());
 				this.localNameSpace = 
 						this.getAdapter().getNameSpace(cm, 
 								this.getAdapter().getController())
 						+ "." 
 						+ cm.getName().toLowerCase(Locale.ENGLISH);
 				this.generateAllAction(cm.getName());
 			}
 		}
 		
 		if (this.isDate) {
 			this.updateWidget("CustomDatePickerDialog.java", 
 					"dialog_date_picker.xml");
 		}
 		
 		if (this.isTime) {
 			this.updateWidget("CustomTimePickerDialog.java",
 					"dialog_time_picker.xml");
 		}
 	}
 	
 	/** 
 	 * Generate all actions (List, Show, Edit, Create). 
 	 * @param entityName The entity for which to generate the crud. 
 	 */
 	public final void generateAllAction(final String entityName) {
 		ConsoleUtils.display(">>> Generate CRUD view for " +  entityName);
 		
 		try {
 			if (this.isWritable) {
 				ConsoleUtils.display("   with write actions");
 	
 				this.generateCreateAction(entityName);
 				this.generateEditAction(entityName);
 				
 				TranslationMetadata.addDefaultTranslation(
 						"common_create",
 						"Create",
 						Group.COMMON);
 				
 				TranslationMetadata.addDefaultTranslation(
 						"common_edit",
 						"Edit",
 						Group.COMMON);
 				
 				TranslationMetadata.addDefaultTranslation(
 						entityName.toLowerCase(Locale.ENGLISH) 
 							+ "_progress_save_title", 
 						entityName + " save progress",
 						Group.MODEL);
 				TranslationMetadata.addDefaultTranslation(
 						entityName.toLowerCase(Locale.ENGLISH) 
 							+ "_progress_save_message", 
 						entityName + " is saving to database…",
 						Group.MODEL);
 			}
 	
 			this.generateShowAction(entityName);
 			this.generateListAction(entityName);
 			
 			TranslationMetadata.addDefaultTranslation(
 					entityName.toLowerCase(Locale.ENGLISH)
 						+ "_progress_load_title", 
 					entityName + " Loading progress",
 					Group.MODEL);
 			TranslationMetadata.addDefaultTranslation(
 					entityName.toLowerCase(Locale.ENGLISH) 
 						+ "_progress_load_message", 
 					entityName + " is loading…",
 					Group.MODEL);
 		
 			new TranslationGenerator(this.getAdapter()).generateStringsXml();
 		} catch (final Exception e) {
 			ConsoleUtils.displayError(e);
 		}
 	}
 	
 	/** List Action.
 	 * @param entityName The entity to generate
 	 * @throws IOException
 	 * @throws TemplateException
 	 */
 	protected final void generateListAction(final String entityName) {
 		final ArrayList<String> javas = new ArrayList<String>();
 		javas.add("%sListActivity.java");
 		javas.add("%sListFragment.java");
 		javas.add("%sListAdapter.java");
 		javas.add("%sListLoader.java");
 		
 		final ArrayList<String> xmls = new ArrayList<String>();
 		xmls.add("activity_%s_list.xml");
 		xmls.add("fragment_%s_list.xml");		
 		xmls.add("row_%s.xml");
 		
 		for (final String java : javas) {
 			this.makeSourceControler(
 					String.format(java, TEMPLATE),
 					String.format(java, entityName));
 		}
 		
 		for (final String xml : xmls) {
 			this.makeResourceLayout(
 					String.format(xml, LOWER_TEMPLATE),
 					String.format(xml, entityName.toLowerCase(Locale.ENGLISH)));
 		}
 		
 		this.updateManifest("ListActivity", entityName);
 		
 		TranslationMetadata.addDefaultTranslation(
 				entityName.toLowerCase(Locale.ENGLISH) + "_empty_list", 
 				entityName + " list is empty !",
 				Group.MODEL);
 	}
 
 	/** Show Action.
 	 * @param entityName The entity to generate
 	 * @throws IOException
 	 * @throws TemplateException
 	 */
 	protected final void generateShowAction(final String entityName) {
 
 		final ArrayList<String> javas = new ArrayList<String>();
 		javas.add("%sShowActivity.java");
 		javas.add("%sShowFragment.java");
 		
 		final ArrayList<String> xmls = new ArrayList<String>();
 		xmls.add("activity_%s_show.xml");
 		xmls.add("fragment_%s_show.xml");
 		
 
 		for (final String java : javas) {
 			this.makeSourceControler(
 					String.format(java, TEMPLATE),
 					String.format(java, entityName));
 		}
 		
 		for (final String xml : xmls) {
 			this.makeResourceLayout(
 					String.format(xml, LOWER_TEMPLATE),
 					String.format(xml, entityName.toLowerCase(Locale.ENGLISH)));
 		}
 
 		this.updateManifest("ShowActivity", entityName);
 		
 		TranslationMetadata.addDefaultTranslation(
 				entityName.toLowerCase(Locale.ENGLISH) + "_error_load", 
 				entityName + " loading error…",
 				Group.MODEL);
 	}
 
 	/** Edit Action.
 	 * @param entityName The entity to generate
 	 * @throws IOException
 	 * @throws TemplateException
 	 */
 	protected final void generateEditAction(final String entityName) {
 		
 		final ArrayList<String> javas = new ArrayList<String>();
 		javas.add("%sEditActivity.java");
 		javas.add("%sEditFragment.java");
 		
 		final ArrayList<String> xmls = new ArrayList<String>();
 		xmls.add("activity_%s_edit.xml");
 		xmls.add("fragment_%s_edit.xml");
 		
 
 		for (final String java : javas) {
 			this.makeSourceControler(
 					String.format(java, TEMPLATE),
 					String.format(java, entityName));
 		}
 		
 		for (final String xml : xmls) {
 			this.makeResourceLayout(
 					String.format(xml, LOWER_TEMPLATE),
 					String.format(xml, entityName.toLowerCase(Locale.ENGLISH)));
 		}
 
 		this.updateManifest("EditActivity", entityName);
 		
 		TranslationMetadata.addDefaultTranslation(
 				entityName.toLowerCase(Locale.ENGLISH) + "_error_edit", 
 				entityName + " edition error…;",
 				Group.MODEL);
 				
 	}
 
 	/** Create Action.
 	 * @param entityName The entity to generate
 	 * @throws IOException
 	 * @throws TemplateException
 	 */
 	protected final void generateCreateAction(final String entityName) {
 		
 		final ArrayList<String> javas = new ArrayList<String>();
 		javas.add("%sCreateActivity.java");
 		javas.add("%sCreateFragment.java");
 		
 		final ArrayList<String> xmls = new ArrayList<String>();
 		xmls.add("activity_%s_create.xml");
 		xmls.add("fragment_%s_create.xml");
 		
 
 		for (final String java : javas) {
 			this.makeSourceControler(
 					String.format(java, TEMPLATE),
 					String.format(java, entityName));
 		}
 		
 		for (final String xml : xmls) {
 			this.makeResourceLayout(
 					String.format(xml, LOWER_TEMPLATE),
 					String.format(xml, entityName.toLowerCase(Locale.ENGLISH)));
 		}
 
 
 		this.updateManifest("CreateActivity", entityName);
 		
 		TranslationMetadata.addDefaultTranslation(
 				entityName.toLowerCase(Locale.ENGLISH) + "_error_create", 
 				entityName + " creation error…;",
 				Group.MODEL);
 	}
 
 	/** Make Java Source Code.
 	 * 
 	 * @param template Template path file. 
 	 *		For list activity is "TemplateListActivity.java"
 	 * @param filename The destination file name.
 	 */
 	private void makeSourceControler(final String template,
 			final String filename) {
 		final String fullFilePath = String.format("%s%s/%s",
 						this.getAdapter().getSourcePath(),
 						PackageUtils.extractPath(this.localNameSpace)
 							.toLowerCase(Locale.ENGLISH),
 						filename);
 		final String fullTemplatePath = String.format("%s%s",
 				this.getAdapter().getTemplateSourceControlerPath(),
 				template);
 		
 		super.makeSource(fullTemplatePath, fullFilePath, false);
 	}
 
 	/** 
 	 * Make Resource file.
 	 * 
 	 * @param template Template path file.
 	 * @param filename Resource file. 
 	 * 	prefix is type of view "row_" or "activity_" or "fragment_" with 
 	 *	postfix is type of action and extension file : 
 	 *		"_list.xml" or "_edit.xml".
 	 */
 	private void makeResourceLayout(final String template, 
 			final String filename) {
 		final String fullFilePath = String.format("%s/%s", 
 									this.getAdapter().getRessourceLayoutPath(),
 									filename);
 		final String fullTemplatePath = String.format("%s/%s",
 				this.getAdapter().getTemplateRessourceLayoutPath(),
 				template);
 		
 		super.makeSource(fullTemplatePath, fullFilePath, false);
 	}
 
 	/** Make Manifest file.
 	 * 
 	 * @param cfg Template engine
 	 * @throws IOException 
 	 * @throws TemplateException 
 	 */
 	public final void makeManifest(final Configuration cfg) 
 			throws IOException, TemplateException {
 		final File file = 
 				TactFileUtils.makeFile(this.getAdapter().getManifestPathFile());
 
 		// Debug Log
 		ConsoleUtils.displayDebug(
 				"Generate Manifest : " + file.getAbsoluteFile());
 
 		// Create
 		final Template tpl = cfg.getTemplate(
 				this.getAdapter().getTemplateManifestPathFile());
 		final OutputStreamWriter output = 
 				new OutputStreamWriter(
 						new FileOutputStream(file), 
 						TactFileUtils.DEFAULT_ENCODING);
 		tpl.process(this.getDatamodel(), output);
 		output.flush();
 		output.close();
 	}
 
 	/**  
 	 * Update Android Manifest.
 	 * @param classF The class file name
 	 * @param entityName the entity for which to update the manifest for.
 	 */
 	private void updateManifest(final String classF, final String entityName) {
 		String classFile = entityName + classF;
 		final String pathRelatif = String.format(".%s.%s.%s",
 				this.getAdapter().getController(), 
 				entityName.toLowerCase(Locale.ENGLISH), 
 				classFile);
 
 		// Debug Log
 		ConsoleUtils.displayDebug("Update Manifest : " + pathRelatif);
 
 		try {
 			// Make engine
 			final SAXBuilder builder = new SAXBuilder();		
 			final File xmlFile = TactFileUtils.makeFile(
 					this.getAdapter().getManifestPathFile());
 			
 			// Load XML File
 			final Document doc = builder.build(xmlFile); 	
 			
 			// Load Root element
 			final Element rootNode = doc.getRootElement(); 			
 			
 			// Load Name space (required for manipulate attributes)
 			final Namespace ns = rootNode.getNamespace("android");	
 
 			// Find Application Node
 			Element findActivity = null;
 			
 			// Find a element
 			final Element applicationNode = rootNode.getChild("application"); 	
 			if (applicationNode != null) {
 
 				// Find Activity Node
 				final List<Element> activities = 
 						applicationNode.getChildren("activity");
 				
 				// Find many elements
 				for (final Element activity : activities) {
 					// Load attribute value
 					if (activity.hasAttributes() 
 							&& activity.getAttributeValue(NAME, ns)
 								.equals(pathRelatif)) {	
 						findActivity = activity;
 						break;
 					}
 				}
 
 				// If not found Node, create it
 				if (findActivity == null) {
 					// Create new element
 					findActivity = new Element("activity");				
 					
 					// Add Attributes to element
 					findActivity.setAttribute(NAME, pathRelatif, ns);	
 					final Element findFilter = new Element("intent-filter");
 					final Element findAction = new Element("action");
 					final Element findCategory = new Element("category");
 					final Element findData = new Element("data");
 
 					// Add Child element
 					findFilter.addContent(findAction);					
 					findFilter.addContent(findCategory);
 					findFilter.addContent(findData);
 					findActivity.addContent(findFilter);
 					applicationNode.addContent(findActivity);
 				}
 
 				// Set values
 				findActivity.setAttribute("label", "@string/app_name", ns);
 				findActivity.setAttribute("exported", "false", ns);
 				final Element filterActivity = 
 						findActivity.getChild("intent-filter");
 				if (filterActivity != null) {
 					StringBuffer data = new StringBuffer();
 					String action = "VIEW";
 
 					if (pathRelatif.matches(".*List.*")) {
 						data.append("vnd.android.cursor.collection/");
 					} else {
 						data.append("vnd.android.cursor.item/");
 
 						if (pathRelatif.matches(".*Edit.*")) {
 							action = "EDIT";
 						} else 		
 
 						if (pathRelatif.matches(".*Create.*")) {
 							action = "INSERT";
 						}
 					}
 
 					
 					data.append(
 							this.getAppMetas().getProjectNameSpace()
 								.replace('/', '.'));
 					data.append('.');
 					data.append(entityName);
 					
 					filterActivity.getChild("action").setAttribute(
 							NAME, "android.intent.action." + action, ns);
 					filterActivity.getChild("category").setAttribute(
 							NAME, "android.intent.category.DEFAULT", ns);
 					filterActivity.getChild("data").setAttribute(
 							"mimeType", data.toString(), ns);
 				}
 				
 				// Clean code
 				applicationNode.sortChildren(new Comparator<Element>() {
 					@Override
 					public int compare(final Element o1, final Element o2) {
 						return o1.getName().compareToIgnoreCase(o2.getName());
 					}
 				});
 			}
 
 			// Write to File
 			final XMLOutputter xmlOutput = new XMLOutputter();
 
 			// Make beautiful file with indent !!!
 			xmlOutput.setFormat(Format.getPrettyFormat());				
 			xmlOutput.output(doc,
 							new OutputStreamWriter(
 								new FileOutputStream(xmlFile.getAbsoluteFile()),
 								TactFileUtils.DEFAULT_ENCODING));
 		} catch (final IOException io) {
 			ConsoleUtils.displayError(io);
 		} catch (final JDOMException e) {
 			ConsoleUtils.displayError(e);
 		}
 	}
 	
 	/**
 	 * Update Widget.
 	 * @param widgetName The widget name.
 	 * @param layoutName The layout name.
 	 */
 	protected final void updateWidget(final String widgetName, 
 			final String layoutName) {
 		super.makeSource(
 				String.format("%s%s", 
 						this.getAdapter().getTemplateWidgetPath(), 
 						widgetName), 
 						
 				String.format("%s%s", 
 						this.getAdapter().getWidgetPath(),
 						widgetName), 
 				false);
 		this.makeResourceLayout(layoutName, layoutName);
 	}
 }
