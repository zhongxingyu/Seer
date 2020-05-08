 package com.redshape.generators.jpa;
 
 import com.redshape.generators.jpa.utils.Commons;
 import com.redshape.generators.jpa.utils.StringUtils;
 import com.thoughtworks.qdox.JavaDocBuilder;
 import com.thoughtworks.qdox.model.*;
 import com.thoughtworks.qdox.model.annotation.AnnotationValue;
 import freemarker.cache.ClassTemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.DirectoryScanner;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.nio.charset.Charset;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * @goal generate
  * @phase generate-source
  * @requiresDependencyResolution compile
  */
 public class GeneratorMojo extends AbstractMojo {
     private static final String ENTITY_ANNOTATION_CLASS_NAME = "javax.persistence.Entity";
     private static final String COLLECTION_CLASS_NAME = "java.util.Collection";
     private static final String LIST_CLASS_NAME = "java.util.List";
     private static final String SET_CLASS_NAME = "java.util.Set";
     private static final String GENERIC_LIST_TYPE = "java.util.List<%s>";
     private static final String CONVENTION_QUERY_CLASS_NAME = "com.redshape.generators.annotations.ConventionalQuery";
     private static final String CONVENTION_QUERIES_CLASS_NAME = "com.redshape.generators.annotations.ConventionalQueries";
     private static final String NATIVE_QUERY_CLASS_NAME = "com.redshape.generators.annotations.NativeQuery";
     private static final String NATIVE_QUERIES_CLASS_NAME = "com.redshape.generators.annotations.NativeQueries";
     private static final String DTO_GROUPS_CLASS_NAME = "com.redshape.generators.annotations.dto.DtoGroups";
     private static final String DTO_GROUP_CLASS_NAME = "com.redshape.generators.annotations.dto.DtoGroup";
     private static final String TARGET_GROUP_CLASS_NAME = "com.redshape.generators.annotations.dto.TargetGroup";
     private static final String TARGET_GROUPS_CLASS_NAME = "com.redshape.generators.annotations.dto.TargetGroup";
     private static final String DTO_INCLUDE_CLASS_NAME = "com.redshape.generators.annotations.dto.DtoInclude";
     private static final String DTO_EXTEND_CLASS_NAME = "com.redshape.generators.annotations.dto.DtoExtend";
     private static final String DTO_EXCLUDE_CLASS_NAME = "com.redshape.generators.annotations.dto.DtoExclude";
     private static final String DTO_TRANSIENT_CLASS_NAME = "com.redshape.generators.annotations.dto.DtoTransient";
 
     private static final String SKIP_DAO_CLASS_NAME = "com.redshape.generators.annotations.dto.SkipDao";
     private static final String DTO_FIELD_OVERRIDE_CLASS_NAME = "com.redshape.generators.annotations.dto.DtoFieldOverride";
     
     private static final String MANY_TO_ONE_CLASS_NAME = "javax.persistence.ManyToOne";
     private static final String MANY_TO_MANY_CLASS_NAME = "javax.persistence.ManyToMany";
     private static final String ONE_TO_MANY_CLASS_NAME = "javax.persistence.OneToMany";
     private static final String ONE_TO_ONE_CLASS_NAME = "javax.persistence.OneToOne";
     
     private static final String[] JPA_RELATIONS_CLASS_LIST = new String[] {
             MANY_TO_MANY_CLASS_NAME,
             MANY_TO_ONE_CLASS_NAME,
             ONE_TO_MANY_CLASS_NAME,
             ONE_TO_ONE_CLASS_NAME
     };
 
 
     /**
      * Target entities base package path pattern
      *
      * @parameter expression="${plugin.entityPattern}" default-value="**\/entities\/**\/*.java"
      */
     private String entityPattern;
 
     /**
      * Project module where generated contents must be placed on
      *
      * @parameter expression="${plugin.targetModule}" required="true"
      */
     private String targetModule;
 
 
     /**
      * Base package where plugin would be looking for a source entities to process
      *
      * @parameter expression="${plugin.basePackagePath}" required="true"
      */
     private String basePackagePath;
 
     /**
      * Base package where plugin would be looking for a source entities to process
      *
      * @parameter expression="${plugin.basePackagePaths}" required="false"
      */
     private List<String> basePackagePaths = new ArrayList<String>();
 
     /**
      * Skip generation of DAO classes from source entities
      * @parameter expression="${plugin.skipDaoGeneration}" required="false"  default-value="false"
      */
     private boolean skipDaoGeneration;
 
     /**
      * Skip generation of DTO classes from source entities
      * @parameter expression="${plugin.skipDtoGeneration}" required="false" default-value="false"
      */
     private boolean skipDtoGeneration;
 
     /**
      * Skip generation of a Spring service classes from source entities
      * @parameter expression="${plugin.skipServicesGeneration}" required="false" default-value="false"
      */
     private boolean skipServicesGeneration;
 
     /**
      * @parameter expression="${plugin.dtoPackagePath}"  required="true"
      */
     private String dtoPackagePath;
 
     /**
      * @parameter expression="${plugin.conversionServicePackagePath}" required="true"
      */
     private String conversationServicePackagePath;
 
     /**
      * @parameter expression="${plugin.daoPackagePath}" required="true"
      */
     private String daoPackagePath;
 
     /**
      * @parameter expression="${plugin.conversionServiceClass}" default-value="DtoConversionService"
      */
     private String conversationServiceClass;
 
     /**
      * @parameter expression="${project.build.sourceEncoding}"
      */
     private String encoding;
 
     private GenerationProfile profile = new GenerationProfile();
 
     protected Template daoTemplate;
     protected Template dtoTemplate;
     protected Template conversionServiceTemplate;
 
     protected void initTemplateEngine() throws IOException {
         Configuration cfg = new Configuration();
         cfg.setTemplateLoader( new ClassTemplateLoader());
         cfg.setObjectWrapper(new DefaultObjectWrapper());
 
         this.conversionServiceTemplate = cfg.getTemplate("templates/ConversionService.template");
         this.daoTemplate = cfg.getTemplate("templates/DAO.template");
         this.dtoTemplate = cfg.getTemplate("templates/DTO.template");
     }
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         if ( "pom".equals( getProject().getPackaging() ) ) {
             return;
         }
 
         if ( encoding == null ) {
             getLog().warn( "Encoding is not set, your build will be platform dependent" );
             encoding = Charset.defaultCharset().name();
         }
 
         try {
             this.initTemplateEngine();
         } catch ( Throwable e ) {
             throw new MojoFailureException( e.getMessage(), e );
         }
 
         JavaDocBuilder builder = createJavaDocBuilder();
 
         List<String> sourceRoots = getProject().getCompileSourceRoots();
         boolean generated = false;
         for ( String sourceRoot : sourceRoots ) {
             getLog().info("Source root: " + sourceRoot );
             try {
                 generated |= scanAndGenerate( new File( sourceRoot ), builder );
             } catch ( Throwable e ) {
                 getLog().error( e.getMessage(), e );
             }
         }
 
         if ( generated ) {
             getLog().debug( "add compile source root " + getDaoGenerateDirectory() + " for DAO" );
             addCompileSourceRoot( getDaoGenerateDirectory() );
 
             getLog().debug( "add compile source root " + getDaoGenerateDirectory() + " for DTO" );
             addCompileSourceRoot( getDtoGenerateDirectory() );
         }
     }
 
     /**
      * @param sourceRoot the base directory to scan for RPC services
      * @return true if some file have been generated
      * @throws Exception generation failure
      */
     private boolean scanAndGenerate( File sourceRoot, JavaDocBuilder builder )
             throws Exception
     {
         getLog().debug("Entity pattern: " + entityPattern);
 
         try {
             DirectoryScanner scanner = new DirectoryScanner();
             scanner.setBasedir( sourceRoot );
             scanner.setIncludes( new String[] { entityPattern } );
             scanner.scan();
             String[] sources = scanner.getIncludedFiles();
             if ( sources.length == 0 )
             {
                 getLog().info("No source entities is suitable to be processed");
                 return false;
             }
 
             boolean fileGenerated = false;
             for ( String source : sources ) {
                 String className = getTopLevelClassName( source );
                 JavaClass clazz = builder.getClassByName( className );
                 if ( isEligibleForGeneration( clazz ) )
                 {
                     process(clazz);
                     fileGenerated = true;
                 }
             }
 
             return fileGenerated;
         } finally {
             generate();
         }
     }
 
     protected String getDtoClassName( String name ) {
         return name + "DTO";
     }
 
     protected String replacePaths( List<String> paths, String replacement, String value ) {
         for ( String path : paths ) {
             value = value.replace(path, replacement);
         }
 
         return value;
     }
 
     protected String getDtoPackagePath( String name ) {
         if ( basePackagePaths != null && !basePackagePaths.isEmpty() ) {
             return replacePaths(basePackagePaths, dtoPackagePath, name);
         } else {
             return replacePaths( Commons.list(basePackagePath), dtoPackagePath, name );
         }
     }
 
     protected String getDaoPackagePath( String name ) {
         if ( basePackagePaths != null && !basePackagePaths.isEmpty() ) {
             return replacePaths(basePackagePaths, daoPackagePath, name );
         } else {
             return replacePaths( Commons.list(basePackagePath), daoPackagePath, name );
         }
     }
 
     protected String getDtoPath( String packagePath, String className ) {
         return getDtoPackagePath(packagePath).replaceAll(Pattern.quote("\\"), "." )
                 + "." + this.getDtoClassName(className);
     }
 
     protected String getDtoPath( DtoGroup group ) {
         return getDtoPackagePath(group.packagePath == null ? group.parent.packagePath : group.packagePath)
                 .replaceAll(Pattern.quote("."), "\\" + File.separator)
                 + File.separator + this.getDtoClassName(group.className) + ".java";
     }
 
     protected String getDaoClassName( String path ) {
         return "I" + path + "DAO";
     }
 
     protected String getDAOPath( DtoGroup group ) {
         return getDaoPackagePath(group.packagePath == null ? group.parent.packagePath : group.packagePath)
                 .replaceAll(Pattern.quote("."), "\\" + File.separator)
                 + File.separator + this.getDaoClassName(group.className) + ".java";
     }
 
     public boolean isSkipDaoGeneration() {
         return skipDaoGeneration;
     }
 
     public boolean isSkipDtoGeneration() {
         return skipDtoGeneration;
     }
 
     public boolean isSkipServicesGeneration() {
         return skipServicesGeneration;
     }
 
     protected File getDaoOutputTarget( DtoGroup profile ) {
         File file =  new File( this.getDaoGenerateDirectory(), this.getDAOPath( profile ) );
         file.getParentFile().mkdirs();
         return file;
     }
 
     protected File getServiceOutputTarget( String packagePath, String path ) {
         File file = new File( this.getServicesGenerateDirectory(), packagePath + "/" + path );
         file.getParentFile().mkdirs();
         return file;
     }
 
     protected File getOutputTarget( String classPath ) {
         File file =  new File( this.getDtoGenerateDirectory(), classPath );
         file.getParentFile().mkdirs();
         return file;
     }
 
     protected File getOutputTarget( String packagePath, String className ) {
         return getOutputTarget( packagePath + "/" + className );
     }
 
     protected File getOutputTarget( DtoGroup profile ) {
         return getOutputTarget( this.getDtoPath(profile) );
     }
 
     protected void generate() throws IOException, TemplateException {
         this.getDtoGenerateDirectory().delete();
         this.getDaoGenerateDirectory().delete();
 
         if ( !this.isSkipDtoGeneration() ) {
             for ( DtoGenerationProfile profile : this.profile.dtoProfiles ) {
                 generateDto(profile);
             }
         } else {
             getLog().info("DTO entities generation has been skipped by the configuration setting...");
         }
 
         if ( !this.isSkipDaoGeneration() ) {
             for ( DaoGenerationProfile profile : this.profile.daoProfiles ) {
                 generateDao( profile );
             }
         } else {
             getLog().info("DAO services generation has been skipped by the configuration setting...");
         }
 
         if ( !this.isSkipServicesGeneration() ) {
             generateConversionService( this.profile.dtoProfiles );
         } else {
             getLog().info("Conversation service generation has been skipped by the configuration setting...");
         }
     }
 
     protected Map<String, Object> convertDtoGroupToMap( DtoGroup group ) {
         Map<String, Object> parameters = new HashMap<String, Object>();
 
         parameters.put("className", group.className == null ? group.parent.className : group.className );
         if ( group.parentClass != null && !group.parentClass.isEmpty() ) {
             parameters.put("parentClass", this.getDtoPackagePath( this.getDtoClassName(group.parentClass) ) );
         } else {
             parameters.put("parentClass", "");
         }
 
         parameters.put("isAbstract", group.isAbstract );
         parameters.put("package", group.packagePath == null ? group.parent.packagePath :  this.getDtoPackagePath(group.packagePath));
         parameters.put("originalPackage", group.packagePath == null ? group.parent.packagePath : group.packagePath );
 
         if ( group.parent != null ) {
             parameters.put( "parent", group.parent.name );
         } else {
             parameters.put( "parent", "java.io.Serializable" );
         }
 
         Collection<Map<String, Object>> properties = new ArrayList<Map<String, Object>>();
         for ( DtoProperty property : group.properties ) {
             if ( property.isExcluded ) {
                 continue;
             }
 
             HashMap<String, Object> propertyData = new HashMap<String, Object>();
             propertyData.put("name", property.name );
             propertyData.put("accessModifier", property.accessModifier );
             propertyData.put("isFinal", property.isFinal );
             propertyData.put("isTransient", property.isTransient );
             propertyData.put("accessorName", property.accessorName );
             propertyData.put("type", property.propertyType );
             propertyData.put("hasMutator", property.hasMutator );
             propertyData.put("hasAccessor", property.hasAccessor );
             propertyData.put("isSynthetic", property.isSynthetic );
             propertyData.put("isAggregated", property.isAggregation );
             propertyData.put("isInherited", property.isInherited );
             if ( property.aggregationType != null ) {
                 propertyData.put("aggregationType", property.aggregationType.name() );
             }
             properties.add( propertyData );
         }
 
         parameters.put("fields", properties);
 
         return parameters;
     }
 
     protected boolean hasAncestors( DtoGroup group ) {
         boolean result = false;
         for ( DtoGenerationProfile profile : this.profile.dtoProfiles ) {
             if ( profile.defaultGroup.parentClass.equals( group.packagePath + "." + group.className ) ) {
                 result = true;
                 break;
             }
         }
 
         return result;
     }
 
     protected void generateConversionService( Collection<DtoGenerationProfile> profiles )
         throws IOException, TemplateException {
         Map<String, Object> templateData = new HashMap<String, Object>();
         templateData.put("package", this.conversationServicePackagePath );
         templateData.put("className", this.conversationServiceClass );
 
         Collection<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
         for ( DtoGenerationProfile profile : profiles ) {
             if ( profile.defaultGroup == null ) {
                 continue;
             }
 
             Map<String, Object> entityData = new HashMap<String, Object>();
             Map<String, Object> originalData = convertDtoGroupToMap(profile.defaultGroup);
             originalData.put("packagePath", profile.defaultGroup.packagePath );
             originalData.put("className", profile.defaultGroup.className );
             entityData.put("original", originalData );
             entityData.put("isAbstract", profile.defaultGroup.isAbstract );
 
             entityData.put("dtoClassName",
                 this.getDtoPath( profile.defaultGroup.packagePath, profile.defaultGroup.className )
             );
             entities.add(entityData);
         }
         templateData.put("entities", entities);
 
         FileWriter writer = new FileWriter(
             this.getServiceOutputTarget(this.conversationServicePackagePath.replaceAll("\\.", "\\/"),
                     this.conversationServiceClass + ".java")
         );
 
         this.conversionServiceTemplate.process(templateData, writer );
     }
 
     protected void generateDtoGroup( DtoGroup group ) throws IOException, TemplateException  {
         Map<String, Object> parameters = convertDtoGroupToMap( group );
 
         FileWriter writer = new FileWriter( this.getOutputTarget(group) );
 
         this.dtoTemplate.process(parameters, writer );
     }
 
     protected void generateDto( DtoGenerationProfile profile ) throws IOException, TemplateException {
         this.generateDtoGroup(profile.defaultGroup);
 
 //        for ( DtoGroup group : profile.groups ) {
 //            this.generateDtoGroup(group);
 //        }
     }
 
     protected Map<String, Object> generateQueryProfile( QueryGenerationProfile query ) {
         return generateQueryProfile( query, new ArrayList<QueryParameter>() );
     }
 
     protected Map<String, Object> generateQueryProfile( QueryGenerationProfile query,
                                                 List<QueryParameter> additionalParameters ) {
         Map<String, Object> queryProfile = new HashMap<String, Object>();
         queryProfile.put( "name", query.name );
 
         String resultType;
         if ( query.isPageable ) {
             if ( !additionalParameters.isEmpty() ) {
                 resultType = "org.springframework.data.domain.Page<" + query.resultType + ">";
             } else {
                 resultType = "java.util.Collection<" + query.resultType + ">";
             }
         } else if ( query.isCollection ) {
             resultType = "java.util.Collection<" + query.resultType + ">";
         } else {
             resultType = query.resultType;
         }
 
         queryProfile.put( "resultType", resultType );
         queryProfile.put( "isCollection", query.isCollection );
         queryProfile.put( "isPageable", query.isPageable );
         queryProfile.put( "isTransactional", query.isTransactional);
 
         Collection<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
         List<QueryParameter> parameters = new ArrayList<QueryParameter>(query.parameters);
         parameters.addAll(additionalParameters);
         for ( QueryParameter param : parameters ) {
             Map<String, Object> paramData = new HashMap<String, Object>();
             paramData.put("name", param.name);
             paramData.put("type", param.type );
             params.add(paramData);
         }
 
         queryProfile.put( "parameters", params  );
 
         boolean isNative = query instanceof NativeQueryGenerationProfile;
         queryProfile.put( "isNative", isNative );
 
         if ( isNative ) {
             queryProfile.put( "value", ( (NativeQueryGenerationProfile) query).query );
             queryProfile.put( "isModifying", ( (NativeQueryGenerationProfile) query ).isModifying );
         }
 
         return queryProfile;
     }
 
     protected QueryParameter createPageableQueryParameter( QueryGenerationProfile query ) {
         QueryParameter pageable = new QueryParameter();
         pageable.name = "\"pageRequestParam\"";
         pageable.type = "org.springframework.data.domain.Pageable";
         return pageable;
     }
 
     protected void generateDao( DaoGenerationProfile profile ) throws TemplateException, IOException {
         if ( profile.entity.skipDao ) {
             getLog().info("Skipping DAO generation for entity: " + profile.entity.className );
             return;
         }
 
         Map<String, Object> parameters = new HashMap<String, Object>();
 
         List<Map<String, Object>> queries = new ArrayList<Map<String, Object>>();
         for ( QueryGenerationProfile query : profile.queries ) {
             queries.add(generateQueryProfile(query));
 
             if ( query.isPageable ) {
                 queries.add(generateQueryProfile(query, Commons.list(
                     createPageableQueryParameter(query)
                 )));
             }
         }
 
         if ( profile.entity.parentClass != null && !profile.entity.parentClass.isEmpty() ) {
             int lastSeparatorPosition =  profile.entity.parentClass.lastIndexOf(".");
 
             String entityName = profile.entity.parentClass.substring( lastSeparatorPosition + 1);
             String packagePath = profile.entity.parentClass.substring(0, lastSeparatorPosition);
             parameters.put("parentClass", this.getDaoPackagePath(packagePath) + "."
                     + this.getDaoClassName(entityName) );
         } else {
             parameters.put("parentClass", "");
         }
 
         parameters.put("hasAncestors", hasAncestors(profile.entity) );
         parameters.put("isAbstract", profile.entity.isAbstract );
         parameters.put("entityName", profile.entity.className );
         parameters.put("package", this.getDaoPackagePath(profile.entity.packagePath) );
         parameters.put("entityPackage", profile.entity.packagePath );
         parameters.put("className", this.getDaoClassName( profile.entity.className )  );
         parameters.put("queries", queries );
         parameters.put("isTransactional", profile.isTransactional );
 
         FileWriter writer = new FileWriter( this.getDaoOutputTarget(profile.entity) );
 
         this.daoTemplate.process(parameters, writer);
     }
 
     protected boolean isSimpleType( JavaClass clazz ) {
         return clazz.getFullyQualifiedName().startsWith("java.lang");
     }
 
     protected boolean isExcludedField( JavaClass clazz ) {
         return !isSimpleType(clazz) && this.isEligibleForGeneration(clazz);
     }
 
     protected boolean isJPARelationType( JavaClass clazz ) {
         for ( String relationClass : JPA_RELATIONS_CLASS_LIST ) {
             if ( clazz.isA(relationClass) ) {
                 return true;
             }
         }
         
         return false;
     }
     
     protected String getEntityClassName( DtoGroup group ) {
         StringBuilder builder = new StringBuilder();
         builder.append( group.packagePath == null ? group.parent.packagePath : group.packagePath ).append(".");
         builder.append( group.className == null ? group.parent.className : group.className );
         return builder.toString();
     }
 
     protected Collection<JavaField> getAllClassFields(JavaClass clazz) {
         Set<JavaField> fields = new HashSet<JavaField>();
 
         fields.addAll( Arrays.asList( clazz.getFields() ) );
 
         JavaClass parent = clazz.getSuperJavaClass();
         while ( parent != null ) {
             fields.addAll( Arrays.asList( parent.getFields() ) );
             parent = parent.getSuperJavaClass();
         }
 
         return fields;
     }
 
     protected void process(JavaClass clazz)
             throws IOException
     {
         getLog().info("Processing class which seems to be applicable as an entity class: "
                 + clazz.getFullyQualifiedName() );
 
         DaoGenerationProfile daoProfile = new DaoGenerationProfile();
         DtoGenerationProfile dtoProfile = new DtoGenerationProfile();
 
         dtoProfile.defaultGroup.packagePath = clazz.getPackage().getName();
         dtoProfile.defaultGroup.className = clazz.getName();
 
         String superClass = "";
         if ( clazz.getSuperJavaClass() != null ) {
             if ( this.isEligibleForGeneration( clazz.getSuperJavaClass() ) ) {
                 superClass = clazz.getSuperClass().getFullQualifiedName();
             }
         }
 
         dtoProfile.defaultGroup.parentClass = superClass;
         dtoProfile.defaultGroup.isAbstract = clazz.isAbstract();
 
         daoProfile.entity = dtoProfile.defaultGroup;
 
         for ( JavaField field : getAllClassFields(clazz) ) {
             if ( field.isStatic() ) {
                 continue;
             }
 
             DtoProperty property = this.processProperty(clazz, field, dtoProfile);
             property.isInherited = !field.getParentClass().equals( clazz );
 
             boolean forcedInclude = false;
             for ( Annotation annotation : field.getAnnotations() ) {
                 if ( annotation.getType().getJavaClass().isA(TARGET_GROUP_CLASS_NAME) ) {
                     this.processTargetGroup( annotation, property );
                 } else if ( annotation.getType().getJavaClass().isA(TARGET_GROUPS_CLASS_NAME) ) {
                     this.processTargetGroups(annotation, property);
                 } else if ( annotation.getType().getJavaClass().isA(DTO_INCLUDE_CLASS_NAME) ) {
                     forcedInclude = true;
                     this.processDtoInclude(annotation, dtoProfile.defaultGroup, property);
                 } else if ( annotation.getType().getJavaClass().isA(DTO_EXCLUDE_CLASS_NAME) ) {
                     property.isExcluded = true;
                 } else if ( annotation.getType().getJavaClass().isA(DTO_TRANSIENT_CLASS_NAME) ) {
                     property.isTransient = true;
                 } else if ( annotation.getType().getJavaClass().isA(DTO_FIELD_OVERRIDE_CLASS_NAME) ) {
                     this.processDtoFieldOverride( annotation, property );
                 } else if ( isJPARelationType(annotation.getType().getJavaClass() ) ) {
                     AnnotationValue targetEntityParameter = annotation.getProperty("targetEntity");
                     if ( targetEntityParameter != null ) {
                         property.realPropertyType = String.valueOf(targetEntityParameter.getParameterValue())
                                 .replace(".class", "");
                     }
                 }
             }
 
            if ( !isExcludedField(field.getType().getJavaClass()) || forcedInclude ) {
                 dtoProfile.defaultGroup.properties.add( property );
             }
         }
 
         for ( Annotation annotation : clazz.getAnnotations() ) {
             if ( annotation.getType().getJavaClass().isA(CONVENTION_QUERY_CLASS_NAME ) ) {
                 this.processConventionQuery(annotation, dtoProfile.defaultGroup, daoProfile);
             } else if ( annotation.getType().getJavaClass().isA(SKIP_DAO_CLASS_NAME) ) {
                 dtoProfile.defaultGroup.skipDao = true;
             } else if ( annotation.getType().getJavaClass().isA(NATIVE_QUERY_CLASS_NAME) ) {
                 this.processNativeQuery(annotation, dtoProfile.defaultGroup, daoProfile);
             } else if ( annotation.getType().getJavaClass().isA(DTO_GROUP_CLASS_NAME ) ) {
                 this.processDtoGroups(annotation, dtoProfile);
             } else if ( annotation.getType().getJavaClass().isA(DTO_GROUPS_CLASS_NAME) ) {
                 this.processDtoGroup(annotation, dtoProfile);
             } else if ( annotation.getType().getJavaClass().isA(NATIVE_QUERIES_CLASS_NAME) ) {
                 this.processNativeQueries(annotation, dtoProfile.defaultGroup, daoProfile );
             } else if ( annotation.getType().getJavaClass().isA(CONVENTION_QUERIES_CLASS_NAME) ) {
                 this.processConventionQueries(annotation, dtoProfile.defaultGroup, daoProfile );
             } else if ( annotation.getType().getJavaClass().isA(DTO_EXTEND_CLASS_NAME) ) {
                 this.processDtoExtend( annotation, dtoProfile );
             }
         }
 
         profile.daoProfiles.add( daoProfile );
         profile.dtoProfiles.add( dtoProfile );
     }
 
     private void processDtoFieldOverride(Annotation annotation, DtoProperty property) {
         Object typeOverride = annotation.getNamedParameter("type");
         if ( typeOverride != null ) {
             property.propertyType = String.valueOf( typeOverride ).replace(".class", "");
         }
 
         Object nameOverride = annotation.getNamedParameter("name");
         if ( nameOverride != null ) {
             property.name = normalizeAnnotationValue( String.valueOf( nameOverride ) );
         }
     }
 
     protected void processDtoExtend( Annotation annotation, DtoGenerationProfile profile ) {
         int i = 0;
         for ( Annotation extension : ( (List<Annotation>) annotation.getNamedParameter("value") ) ) {
             DtoProperty property = new DtoProperty();
             property.hasAccessor = true;
             property.hasMutator = true;
             property.name = this.normalizeAnnotationValue(
                 String.valueOf( extension.getNamedParameter("value") )
             );
 
             property.isSynthetic = true;
 
             if ( property.name == null ) {
                 property.name = "param" + i++;
             }
             property.propertyType = String.valueOf( extension.getNamedParameter("type") ).replace(".class", "");
             if ( extension.getNamedParameter("isArray") != null ) {
                 boolean isArray = Boolean.valueOf( String.valueOf( extension.getNamedParameter("isArray") ) );
                 if ( isArray ) {
                     property.propertyType += "[]";
                 }
             }
 
             profile.defaultGroup.properties.add(property);
         }
     }
 
     protected void processDtoInclude( Annotation annotation, DtoGroup group, DtoProperty field ) {
         if ( annotation.getNamedParameter("name") != null ) {
             field.name = this.normalizeAnnotationValue(
                 String.valueOf( annotation.getNamedParameter("name") )
             );
         }
 
         String aggregationType = String.valueOf(annotation.getNamedParameter("value"));
         if ( aggregationType.equals("AggregationType.ID") ) {
             field.propertyType = "java.lang.Long";
             field.isAggregation = true;
             field.aggregationType = AggregationType.ID;
             field.name += "Id";
         } else {
             field.isAggregation = true;
             field.aggregationType = AggregationType.DTO;
 
             String listTypeName = getListType( field.propertyType );
             if ( listTypeName == null ) {
                 field.propertyType = this.getDtoPath(
                     field.propertyType.substring(0, field.propertyType.lastIndexOf(".") ),
                     field.propertyType.substring( field.propertyType.lastIndexOf(".") + 1 ) );
             } else {
                 field.propertyType = this.generifyType(listTypeName, this.getDtoPath(
                         field.realPropertyType.substring(0, field.realPropertyType.lastIndexOf(".")),
                         field.realPropertyType.substring(field.realPropertyType.lastIndexOf(".") + 1)));
             }
         }
     }
 
     protected String generifyType( String collectionTypeName, String rawType ) {
         return collectionTypeName + "<" + rawType + ">";
     }
 
     protected String getListType( String typeName ) {
         if ( typeName.startsWith(LIST_CLASS_NAME) ) {
             return LIST_CLASS_NAME;
         }
 
         if ( typeName.startsWith(COLLECTION_CLASS_NAME) ) {
             return COLLECTION_CLASS_NAME;
         }
 
         if ( typeName.startsWith(SET_CLASS_NAME) ) {
             return SET_CLASS_NAME;
         }
 
         return null;
     }
 
     protected boolean isMethodExists( JavaClass clazz, String methodName, Type[] methodParameters ) {
         JavaMethod accessorMethod = null;
         while ( accessorMethod == null && clazz != null ) {
             accessorMethod = clazz.getMethodBySignature( methodName,
                     new Type[] {}  );
             clazz = clazz.getSuperJavaClass();
         }
         return accessorMethod != null;
     }
 
     protected DtoProperty processProperty( JavaClass clazz, JavaField field, DtoGenerationProfile profile  ) {
         DtoProperty property = new DtoProperty();
         property.name = field.getName();
         property.accessorName = "get";
         property.isFinal = field.isFinal();
         property.accessModifier = "public"; //field.isPrivate() ? "private" : ( field.isProtected() ? "protected" : "public" );
 
         if ( field.getType().getFullQualifiedName().contains("boolean")
                 || field.getType().getFullQualifiedName().equals("java.lang.Boolean") ) {
             if ( isMethodExists(clazz, "is" + StringUtils.ucfirst(property.name), new Type[] {} ) ) {
                 property.accessorName = "is";
             }
         }
 
         property.hasAccessor = property.accessorName == "is"
                 || isMethodExists(clazz, "get" + StringUtils.ucfirst(property.name), new Type[] {} ) ;
         property.hasMutator = isMethodExists(clazz, "set" + StringUtils.ucfirst(property.name), new Type[] {} );
         property.propertyType = field.getType().getFullQualifiedName();
         return property;
     }
 
     protected void processTargetGroups( Annotation annotation, DtoProperty property ) {
         for ( Annotation group : ( (List<Annotation>) annotation.getNamedParameter("value") ) ) {
             TargetGroup groupItem = new TargetGroup();
             groupItem.groupName = String.valueOf(group.getNamedParameter("value"));
             groupItem.isExluded = Boolean.valueOf( String.valueOf(group.getNamedParameter("isExcluded")) );
             property.groups.add( groupItem );
         }
     }
 
     protected void processTargetGroup( Annotation annotation, DtoProperty property ) {
         TargetGroup group = new TargetGroup();
         group.groupName = String.valueOf( annotation.getNamedParameter("value") );
         group.isExluded = Boolean.valueOf( String.valueOf( annotation.getNamedParameter("isExcluded") ) );
         property.groups.add( group );
     }
 
     protected void processQueryParameters( DtoGroup group, QueryGenerationProfile queryProfile, Annotation annotation ) {
         List<Annotation> parameters = (List<Annotation>) annotation.getNamedParameter("parameters");
         if ( parameters == null ) {
             return;
         }
 
         int i = 0 ;
         for ( Annotation parameterMeta : parameters ) {
             QueryParameter parameter = new QueryParameter();
             parameter.name = String.valueOf( parameterMeta.getNamedParameter("value") );
             if ( parameter.name == null ) {
                 parameter.name = "param" + i++;
             }
             parameter.type = String.valueOf( parameterMeta.getNamedParameter("type") ).replace(".class", "");
             if ( parameterMeta.getNamedParameter("isArray") != null ) {
                 boolean isArray = Boolean.valueOf( String.valueOf( parameterMeta.getNamedParameter("isArray") ) );
                 if ( isArray ) {
                     parameter.type += "[]";
                 }
             }
 
             if (parameter.type == null) {
                 for ( DtoProperty property : group.properties ) {
                     if ( property.name.equals( parameter.name ) ) {
                         parameter.type = property.propertyType;
                         break;
                     }
                 }
 
                 if ( group.parent != null && parameter.type == null ) {
                     for ( DtoProperty property : group.parent.properties ) {
                         if ( property.name.equals( parameter.name ) ) {
                             parameter.type = property.propertyType;
                             break;
                         }
                     }
                 }
             }
 
             queryProfile.parameters.add(parameter);
         }
     }
 
     protected void processConventionQuery( Annotation annotation, DtoGroup dtoGroup, DaoGenerationProfile profile ) {
         ConventionQueryGenerationProfile query = new ConventionQueryGenerationProfile();
 
         query.name = String.valueOf( annotation.getNamedParameter("name") ).replace("\"", "");
         if ( annotation.getNamedParameter("isCollection") != null ) {
             query.isCollection = Boolean.valueOf(String.valueOf(annotation.getNamedParameter("isCollection")));
         } else {
             query.isCollection = true;
         }
 
         if ( annotation.getNamedParameter("isTransactional") != null ) {
             query.isTransactional  = Boolean.valueOf( String.valueOf( annotation.getNamedParameter("isTransactional") ) );
         } else {
             query.isTransactional = false;
         }
 
         if ( annotation.getNamedParameter("isPageable") != null ) {
             query.isPageable = Boolean.valueOf(String.valueOf(annotation.getNamedParameter("isPageable")));
         } else {
             query.isPageable = false;
         }
 
         query.resultType = annotation.getNamedParameter("resultType") != null ?
                 String.valueOf(annotation.getNamedParameter("resultType")) :
                 this.getEntityClassName(dtoGroup);
         this.processQueryParameters( dtoGroup, query,  annotation );
         profile.queries.add(query);
     }
 
     protected void processConventionQueries( Annotation annotation, DtoGroup dtoGroup, DaoGenerationProfile profile ) {
         for ( Annotation queryMeta : ( (List<Annotation>) annotation.getNamedParameter("value") ) ) {
             this.processConventionQuery( queryMeta, dtoGroup, profile );
         }
 
         if ( annotation.getNamedParameter("isTransactional") != null ) {
             profile.isTransactional = Boolean.valueOf( String.valueOf( annotation.getNamedParameter("isTransactional") ) );
         } else {
             profile.isTransactional = false;
         }
     }
 
     protected void processNativeQueries( Annotation annotation, DtoGroup dtoGroup, DaoGenerationProfile profile ) {
         for ( Annotation queryMeta : ( (List<Annotation>) annotation.getNamedParameter("value") ) ) {
             this.processNativeQuery( queryMeta, dtoGroup, profile );
         }
 
         if ( annotation.getNamedParameter("isTransactional") != null ) {
             profile.isTransactional = Boolean.valueOf( String.valueOf( annotation.getNamedParameter("isTransactional") ) );
         } else {
             profile.isTransactional = false;
         }
     }
 
     protected void processNativeQuery( Annotation annotation, DtoGroup dtoGroup, DaoGenerationProfile profile ) {
         NativeQueryGenerationProfile nativeQuery = new NativeQueryGenerationProfile();
         nativeQuery.name = String.valueOf( annotation.getNamedParameter("name") ).replace("\"", "");
         nativeQuery.isModifying = Boolean.valueOf(String.valueOf(annotation.getNamedParameter("isModifying")));
         nativeQuery.query = String.valueOf(annotation.getNamedParameter("value"));
 
         if ( annotation.getNamedParameter("isTransactional") != null ) {
             nativeQuery.isTransactional = Boolean.valueOf( String.valueOf( annotation.getNamedParameter("isTransactional") ) );
         } else {
             nativeQuery.isTransactional = false;
         }
 
         if ( annotation.getNamedParameter("isCollection") != null ) {
             nativeQuery.isCollection = Boolean.valueOf(String.valueOf(annotation.getNamedParameter("isCollection")));
         } else {
             nativeQuery.isCollection = true;
         }
 
         if ( annotation.getNamedParameter("isPageable") != null ) {
             nativeQuery.isPageable = Boolean.valueOf(String.valueOf(annotation.getNamedParameter("isPageable")));
         } else {
             nativeQuery.isPageable = false;
         }
 
         nativeQuery.resultType = annotation.getNamedParameter("resultType") != null ?
                 String.valueOf(annotation.getNamedParameter("resultType")).replace(".class", "") :
                 this.getEntityClassName(dtoGroup);
         this.processQueryParameters( dtoGroup, nativeQuery,  annotation );
         profile.queries.add( nativeQuery );
     }
 
     protected void processDtoGroup( Annotation annotation, DtoGenerationProfile profile ) {
         DtoGroup group = new DtoGroup();
         group.name = String.valueOf( annotation.getNamedParameter("name") );
         group.parent = profile.defaultGroup;
         profile.groups.add( group );
     }
 
     protected void processDtoGroups( Annotation annotation, DtoGenerationProfile profile ) {
         for ( Annotation annotationGroup : ( (List<Annotation>) annotation.getNamedParameter("value") ) ) {
             DtoGroup group = new DtoGroup();
             group.name = String.valueOf( annotationGroup.getNamedParameter("name") );
             group.parent = profile.defaultGroup;
             profile.groups.add( group );
         }
     }
 
     protected Annotation getDeclaredAnnotation( JavaClass clazz, String annotationName ) {
         for ( Annotation annotation : clazz.getAnnotations() ) {
             if ( annotation.getType().getJavaClass().isA(annotationName) ) {
                 return annotation;
             }
         }
 
         return null;
     }
 
     private boolean isEligibleForGeneration( JavaClass javaClass )
     {
         return !javaClass.isInterface() && !javaClass.isEnum()
                 && javaClass.isPublic()
                 && this.getDeclaredAnnotation(javaClass, ENTITY_ANNOTATION_CLASS_NAME) != null;
     }
 
     @SuppressWarnings("unchecked")
     private JavaDocBuilder createJavaDocBuilder()
             throws MojoExecutionException
     {
         try
         {
             JavaDocBuilder builder = new JavaDocBuilder();
             builder.setEncoding( encoding );
             builder.getClassLibrary().addClassLoader( getProjectClassLoader() );
             for ( String sourceRoot : ( List < String > ) getProject().getCompileSourceRoots() )
             {
                 builder.addSourceTree( new File( sourceRoot ) );
             }
             return builder;
         }
         catch ( MalformedURLException e )
         {
             throw new MojoExecutionException( "Failed to resolve project classpath", e );
         }
         catch ( DependencyResolutionRequiredException e )
         {
             throw new MojoExecutionException( "Failed to resolve project classpath", e );
         }
     }
 
     private String getTopLevelClassName( String sourceFile )
     {
         String className = sourceFile.substring( 0, sourceFile.length() - 5 ); // strip ".java"
         return className.replace( File.separatorChar, '.' );
     }
 
     /**
      * @return the project classloader
      * @throws DependencyResolutionRequiredException failed to resolve project dependencies
      * @throws java.net.MalformedURLException configuration issue ?
      */
     protected ClassLoader getProjectClassLoader()
             throws DependencyResolutionRequiredException, MalformedURLException
     {
         List<?> compile = getProject().getCompileClasspathElements();
         URL[] urls = new URL[compile.size()];
         int i = 0;
         for ( Object object : compile )
         {
             if ( object instanceof Artifact )
             {
                 urls[i] = ( (Artifact) object ).getFile().toURI().toURL();
             }
             else
             {
                 urls[i] = new File( (String) object ).toURI().toURL();
             }
             i++;
         }
         return new URLClassLoader( urls, ClassLoader.getSystemClassLoader() );
     }
 
     public class ConventionQueryGenerationProfile extends QueryGenerationProfile {
 
     }
 
     public class NativeQueryGenerationProfile extends QueryGenerationProfile {
         public String query;
         public boolean isModifying;
     }
 
     public class QueryParameter {
         public String name;
         public String type;
     }
 
     public abstract class QueryGenerationProfile {
         public String name;
         public String  resultType;
         public boolean isCollection;
         public boolean isPageable;
         public boolean isTransactional;
         public List<QueryParameter> parameters = new ArrayList<QueryParameter>();
     }
 
     public class DaoGenerationProfile {
         public String name;
         public DtoGroup entity;
         public boolean isTransactional;
         public Collection<QueryGenerationProfile> queries = new ArrayList<QueryGenerationProfile>();
     }
 
     public enum AggregationType {
         ID,
         DTO;
     }
 
     public class TargetGroup {
         public String groupName;
         public boolean isExluded;
     }
 
     public class DtoProperty {
         public String name;
         public boolean isTransient;
         public boolean isFinal;
         public String accessModifier;
         public String propertyType;
         public String realPropertyType;
         public String accessorName;
         public Collection<TargetGroup> groups = new ArrayList<TargetGroup>();
         public boolean isSynthetic;
         public boolean isExcluded;
         public boolean hasAccessor;
         public boolean hasMutator;
         public boolean isAggregation;
         public AggregationType aggregationType;
         public boolean isInherited;
     }
 
     public class DtoGroup {
         public String name;
         public boolean skipDao;
         public String packagePath;
         public DtoGroup parent;
         public String className;
         public String parentClass;
         public boolean isAbstract;
         public Collection<DtoProperty> properties = new ArrayList<DtoProperty>();
     }
 
     public class DtoGenerationProfile {
         public Collection<DtoGroup> groups = new ArrayList<DtoGroup>();
         public DtoGroup defaultGroup = new DtoGroup();
     }
 
 
     public class GenerationProfile {
         public Collection<DaoGenerationProfile> daoProfiles = new ArrayList<DaoGenerationProfile>();
         public Collection<DtoGenerationProfile> dtoProfiles = new ArrayList<DtoGenerationProfile>();
     }
 
 }
