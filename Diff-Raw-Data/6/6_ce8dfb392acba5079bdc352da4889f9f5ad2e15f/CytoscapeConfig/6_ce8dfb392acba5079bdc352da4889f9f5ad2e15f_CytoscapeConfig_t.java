 package cytoscape;
 
 import java.io.*;
 import java.util.*;
 
 import java.beans.*;
 
 import cytoscape.data.readers.*;
 
 import gnu.getopt.Getopt;
 import gnu.getopt.LongOpt;
 
 /**
  * handles the parsing of, and access to, command line arguments for cytoscape, and
  * the control of various features and attributes via 'cytoscape.props' files
  */
 public class CytoscapeConfig 
   implements 
     PropertyChangeListener {
 
   protected String argSpecificationString = "n:j:g:b:i:y:he:vWs:l:p:xc:t:;";
 
   protected String [] commandLineArguments;
   protected String[] argsCopy;
   protected boolean canonicalize = true;
   protected boolean helpRequested = false;
   protected boolean inputsError = false;
   protected boolean displayVersion = false;
   protected List geometryFilenames = null;
   protected String bioDataDirectory = null;
   protected String expressionFilename = null;
   protected String projectFilename = null;
   protected List interactionsFilenames = null;
   protected Vector nodeAttributeFilenames = new Vector ();
   protected Vector edgeAttributeFilenames = new Vector ();
   protected String defaultSpeciesName = null;
   protected String projectPropsFileName = null;
   protected String projectVizmapPropsFileName = null;
   protected File projectFileDirectoryAbsolute;
   protected boolean enableUndo = false;
   protected boolean copyExpToAttribs = true;
   protected boolean yfiles = true;
   protected String graphLibrary = null;
   protected String viewType = "tabbed";
   //protected File projectPropsFile = null;
 
   protected String [] layoutStrategies = {"organic", "hierarchical", "embedded", "circular"};
   protected String defaultLayoutStrategy = layoutStrategies [0];
 
   protected StringBuffer errorMessages = new StringBuffer ();
   // system and user property files use the same name
   protected Properties props;
   protected Integer viewThreshold;
   
 
   static public StringBuffer debugLog = new StringBuffer ();
 
 
   public CytoscapeConfig ( String [] args ) {
     // make a copy of the args to parse here (getopt can mangle the array it parses)
     commandLineArguments = new String [args.length];
     System.arraycopy (args, 0, commandLineArguments, 0, args.length);
     // make a copy of the arguments for later use
     argsCopy = new String [args.length];
     System.arraycopy (args, 0, argsCopy, 0, args.length);
 
     props = readProperties ();
     loadProperties();
 
     parseArgs ();
     readProjectFile ();
    
     getConfigurationsFromProperties ();
 
     Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(Cytoscape.CYTOSCAPE_EXIT, this );
 
   } // ctor
  
   /**
    * @return a new array that is the copy of the args
    */
   public String [] getArgs () 
   {
     String [] returnVal = new String [argsCopy.length];
     System.arraycopy (argsCopy, 0, returnVal, 0, argsCopy.length);
     return returnVal;
   }
  
   public int getViewThreshold () {
     if ( viewThreshold == null )
       return 500;
     return viewThreshold.intValue();
   }
 
   public int getViewType() {
     
     if ( viewType.equals( "tabbed" ) ) 
       return cytoscape.view.CytoscapeDesktop.TABBED_VIEW;
     else if ( viewType.equals( "internal" ) ) 
       return cytoscape.view.CytoscapeDesktop.INTERNAL_VIEW;
     else if ( viewType.equals( "external" ) ) 
       return cytoscape.view.CytoscapeDesktop.EXTERNAL_VIEW;
     return cytoscape.view.CytoscapeDesktop.TABBED_VIEW;
   }
 
   /**
    * @return the given location of a passed GML file
    */
   public List getGeometryFilenames () {
     if ( geometryFilenames == null )
       geometryFilenames = new ArrayList(0);
     return geometryFilenames;
   }
 
   /**
    * @return the given location of a passed SIF file
    */
   public List getInteractionsFilenames () {
     if ( interactionsFilenames == null )
       interactionsFilenames = new ArrayList(0);
     return interactionsFilenames;
   }
 
   /**
    *  @return the given location of a passed expression
    */
   public String getExpressionFilename ()  {
     return expressionFilename;
   }
  
   /**
    *  @return the given location of a passed Project File
    */
   public String getProjectFilename () {
     return projectFilename;
   }
   
   /**
    *  @return the given location of a passed VizMap file
    */
   public String getProjectVizmapPropsFileName () {
     return projectVizmapPropsFileName;
   }
  
   /**
    *  @return the given directory for a BioData server
    */
   public String getBioDataDirectory () {
     return bioDataDirectory;
   }
   
  
   /**
    * @return the number of node attribute files
    */
   public int getNumberOfNodeAttributeFiles () {
     return nodeAttributeFilenames.size ();
   }
 
   /**
    * @return the number of edge attribute files
    */
   public int getNumberOfEdgeAttributeFiles () {
     return edgeAttributeFilenames.size ();
   }
  
   /**
    * @return the array of locations of node attribute files
    */
   public String [] getNodeAttributeFilenames ()  {
     return (String []) nodeAttributeFilenames.toArray (new String [0]);
   }
  
   /**
    * @return the array of locations of edge attribute files
    */
   public String [] getEdgeAttributeFilenames ()  {
     return (String []) edgeAttributeFilenames.toArray (new String [0]);
   }
 
   /**
    * If the -c flag is set in the command line, 
    * or if "canonicalizeNames = no" is in the project file, it returns
    * false. It returns true otherwise.
    * @return the default canonicalizization state
    */
   public boolean getCanonicalize () {
     return canonicalize;
   }
 
   /**
    * @deprecated
    * If the -y option specified and it is not "y-files"
    * then it returns false; otherwise true;
    * Added by Larissa Kamenkovich on September 26, 2003.
    *
    * This method now always returns false, because yFiles is
    * no longer supported. -AM 12-30-2003
    */
   public boolean isYFiles () {
     //return yfiles;
     return false;
   }
  
 
   public void addGeometryFilename ( String filename ) {
     if ( geometryFilenames == null )
       geometryFilenames = new ArrayList();
     geometryFilenames.add( filename );
   }
 
   public void addInteractionsFilename ( String filename ) {
     if ( interactionsFilenames == null )
       interactionsFilenames = new ArrayList();
     interactionsFilenames.add( filename );
   }
 
   /**
    * Add the given node attributes filename (as per opened in gui).
    *
    * added by dramage 2002-08-21
    */
   public void addNodeAttributeFilename (String filename) {
     if (!nodeAttributeFilenames.contains(filename))
       nodeAttributeFilenames.add(filename);
   }
   
   /**
    * Add the given edge attributes filename (as per opened in gui).
    *
    * added by dramage 2002-08-21
    */
   public void addEdgeAttributeFilename (String filename) {
     if (!edgeAttributeFilenames.contains(filename))
       edgeAttributeFilenames.add(filename);
   }
   
   public String [] getAllDataFileNames () {
     Vector allFileNames = new Vector ();
     String [] nodeAttributeFiles = getNodeAttributeFilenames ();
     String [] edgeAttributeFiles = getEdgeAttributeFilenames ();
 
     for (int n=0; n < nodeAttributeFiles.length; n++)
       allFileNames.add (nodeAttributeFiles [n]);
 
     for (int e=0; e < edgeAttributeFiles.length; e++)
       allFileNames.add (edgeAttributeFiles [e]);
 
     if (geometryFilenames != null)
       allFileNames.addAll( geometryFilenames );
 
     if (interactionsFilenames != null)
       allFileNames.addAll( interactionsFilenames );
 
     if (expressionFilename != null)
       allFileNames.add (expressionFilename);
 
     return (String []) allFileNames.toArray (new String [0]);
 
   } // getAllDataFileNames
  
   public String [] getAllDataFileExtensions () {
     String [] fullNames = getAllDataFileNames ();
     Vector allExtensions = new Vector ();
 
     for (int i=0; i < fullNames.length; i++) {
       String filename = fullNames [i];
       int positionOfLastDot = filename.lastIndexOf (".");
       if (positionOfLastDot > 0) {
         String extension = filename.substring (positionOfLastDot + 1);
         if (!allExtensions.contains (extension))
           allExtensions.add (extension);
       } // if
     } // for i
 
     return (String []) allExtensions.toArray (new String [0]);
 
   } // getAllDataFileExtensions
  
   public String getDefaultSpeciesName ()  {
     return defaultSpeciesName;
   }
 
   public String getDefaultLayoutStrategy ()  {
     return defaultLayoutStrategy;
   }
  
   public boolean helpRequested () {
     return helpRequested;
   }
  
   public boolean inputsError ()  {
     return inputsError;
   }
 
   public boolean displayVersion () {
     return displayVersion;
   }
  
   /**
    * @deprecated
    */
   public boolean enableUndo ()  {
     return enableUndo;
   }
 
   public Properties getProperties ()   {
     return props;
   }
 
   /** if the -x flag is set on the command line, returns false;
    *  otherwise, by default, returns true.                      */
   public boolean getWhetherToCopyExpToAttribs() {
     return copyExpToAttribs;
   }
 
   public void propertyChange ( PropertyChangeEvent e ) {
     if ( e.getPropertyName() == Cytoscape.CYTOSCAPE_EXIT )
       saveProperties();
   }
 
   protected void saveProperties () {
     
      if ( defaultSpeciesName != null )
        props.setProperty("defaultSpeciesName", defaultSpeciesName );
      if ( viewType != null )
        props.setProperty("viewType", viewType );
      if ( viewThreshold != null )
        props.setProperty("viewThreshold", viewThreshold.toString() );
      if ( bioDataDirectory != null )
        props.setProperty( "bioDataDirectory", bioDataDirectory );
 
     try {
       File file = Cytoscape.getCytoscapeObj().getConfigFile( "cytoscape.props" );
 
       FileOutputStream output = new FileOutputStream( file );
       props.store( output, "Cytoscape Property File" );
 
     } catch ( Exception ex ) {
       System.out.println( "Cytoscape.Props Write error" );
       ex.printStackTrace();
     }
   }
 
 
   protected void loadProperties () {
 
     System.out.println( "Loading Properties..." );
 
     Enumeration enum = props.propertyNames();
     while ( enum.hasMoreElements() ) {
       String property = ( String )enum.nextElement();
       System.out.println( property +" :" +props.getProperty( property ) );
     }
 
 
     viewThreshold = new Integer(props.getProperty( "viewThreshold", "500" ) );
     viewType = props.getProperty( "viewType", "tabbed" );
    defaultSpeciesName = props.getProperty("defaultSpeciesName", "Saccharomyces cerevisiae" );
     bioDataDirectory = props.getProperty( "bioDataDirectory", "testData/annotation/manifest");
   }
 
 
   /**
    * read (if possible) properties from CYTOSCAPE_HOME/cytoscape.props, 
    * from HOME/cytoscape.props, and  finally from PWD/cytoscape.props.
    * the latter properties (when they are duplicates) superceed previous ones
    * in the absence of any props, hard-coded defaults within various classes 
    * will be used.
    */
   protected Properties readProperties ()   {
    //  Properties systemProps = null;
 //     Properties userGeneralProps = null;
 //     Properties userSpecialProps = null;
 //     Properties projectProps = null;
     Properties defaultProps = null;
 
 
     // read all of the possible props files
 
     // 1. ~.cytoscape/cytoscape.props
     // 2. CYTOSCAPE_HOME/cytoscape.props
     // 3. ~/cytoscape.props
     // 4. `pwd`/cytoscape.props
 
     //    String propsFileName = "cytoscape.props"; // there may be 3 copies of this
 
     File defaultPropsFile = getConfigFile( "cytoscape.props" );//createFile ( System.getProperty ("user.dir"), ".cytoscape/"+propsFileName );
     //    if ( defaultPropsFile != null )
     //  defaultProps = readOnePropertyFile( null, defaultPropsFile );
     
       // System.out.println( "load properties from: "+defaultPropsFile );
 
     try {
       defaultProps = new Properties();
       defaultProps.load( new FileInputStream (defaultPropsFile) );
     } catch ( Exception e ) {}
       return defaultProps;
 
 
    //  File propsFile = createFile (System.getProperty ("CYTOSCAPE_HOME"), propsFileName);
 //     if (propsFile != null)
 //       systemProps = readOnePropertyFile (defaultProps, propsFile);
 
 //     File userGeneralPropsFile = createFile (System.getProperty ("user.home"), propsFileName);
 //     if (userGeneralPropsFile != null)
 //       userGeneralProps = readOnePropertyFile (systemProps, userGeneralPropsFile);
 
 //     File userSpecialPropsFile = createFile  (System.getProperty ("user.dir"), propsFileName);
 //     if (userSpecialPropsFile != null)
 //       userSpecialProps = readOnePropertyFile (userGeneralProps, userSpecialPropsFile);
 
    
 
 
 //     this.debugLog.append ("projectPropsFileName: " + projectPropsFileName);
 //     if (projectPropsFileName != null) {
 //       projectProps = readPropertyFileAsText (projectPropsFileName);
 //       // projectProps = readOnePropertyFile (projectProps, projectPropsFile);
 //     }
 
 //     /* we will return a valid Properties object; if any properties files
 //      * were found and read, we copy them in sequentially so that duplicate
 //      * keys in the users file overwrite the sytems defaults 
 //      */
 
 //     Properties fullProps = new Properties ();
 
 //     if ( defaultProps != null ) 
 //       fullProps.putAll( defaultProps );
 
 //     if (systemProps != null) 
 //       fullProps.putAll (systemProps);
 
 //     if (userGeneralProps != null) 
 //       fullProps.putAll (userGeneralProps);
 
 //     if (userSpecialProps != null)
 //       fullProps.putAll (userSpecialProps);
 
 
 //     if (projectProps != null) {
 //       this.debugLog.append ("--- about to add " + projectProps.size ()  +
 //                             " project Cyproperties");    fullProps.putAll (projectProps);
 //     }
 
 //     return fullProps;
 
   } // readProperties
  
 
   /**
    * @return the directory ".cytoscape" in the users home directory.
    */
   public File getConfigDirectoy () {
     File dir = null;
     try {
       File parent_dir = new File(System.getProperty ("user.home"), ".cytoscape" );
       if ( parent_dir.mkdir() ) 
         System.err.println( "Parent_Dir: "+parent_dir+ " created." );
 
       return parent_dir;
     } catch ( Exception e ) {
       System.err.println( "error getting config directory" );
     }
     return null;
   }
 
   public File getConfigFile ( String file_name ) {
     try {
       File parent_dir = getConfigDirectoy();
       File file = new File( parent_dir, file_name );
       if ( file.createNewFile() )
           System.err.println( "Config file: "+file+ " created." );
       return file;
 
     } catch ( Exception e ) {
       System.err.println( "error getting config file:"+file_name );
     }
     return null;
   }
 
 
 
 
   public Properties readPropertyFileAsText (String filename)  {
     String rawText = "";
     //String filename = projectPropsFile.getPath ();
     this.debugLog.append ("CC.readPropertyFileAsText, path: " + filename + "\n");
     try {
       if (filename.trim().startsWith ("jar://")) {
         this.debugLog.append ("CC.readPropertyFileAsText, starts with jar://\n");
         TextJarReader reader = new TextJarReader (filename);
         reader.read ();
         rawText = reader.getText ();
         this.debugLog.append ("from jar, rawText:\n" + rawText + "\n");
       }
       else {
         this.debugLog.append ("CC.readPropertyFileAsText, does not start with jar://\n");
         File projectPropsFile = new File (absolutizeFilename (projectFileDirectoryAbsolute, filename));
         TextFileReader reader = new TextFileReader (projectPropsFile.getPath ());
         reader.read ();
         rawText = reader.getText ();
         this.debugLog.append ("from file, rawText:\n" + rawText + "\n");
       }
     }
     catch (Exception e0) {
       System.err.println ("-- Exception while reading properties file " + filename);
       System.err.println (e0.getMessage ());
     }
 
     //the Properties class contains its own parser, so it makes the most sense
     //to massage our text into a form suitable for that loader
     byte[] byteText = rawText.getBytes();
     InputStream is = new ByteArrayInputStream(byteText);
     Properties newProps = new Properties ();
     try {
       newProps.load(is);
     } catch (IOException ioe) {//seems unlikely
       ioe.printStackTrace();
     }
 
     return newProps;
 
   } // readPropertyFileAsText
   //------------------------------------------------------------------------------------------
   /**
    * return a File which is known to exist, and is readable.
    */
   private File createFile (String directory, String filename)
   {
     if (directory != null) {
       File result = new File (directory, filename);
       if (result.canRead ())
         return result;
     }
 
     return null;
 
   } // createFile
   //------------------------------------------------------------------------------------------
   /**
    *  read properties from the named file, combining with previously read properties
    *  when possible.
    *  @param priorProps  previously-read properties of a more general status
    *  @param propsFile   a property file which is guaranteed to be readable
    */
   private Properties readOnePropertyFile (Properties priorProps, File propsFile)
   {
     Properties newProps = new Properties ();
 
     if (priorProps != null)
       newProps =  new Properties (priorProps);
 
     try {
       FileInputStream in = new FileInputStream (propsFile);
       newProps.load (in);
     } // try
     catch (FileNotFoundException ignore) {;}
     catch (IOException ignore) {;}
 
     return newProps;
 
   } // readOnePropertyFile
   //------------------------------------------------------------------------------------------
   protected void parseArgs ()
   {
 
    
 
     helpRequested = false;
     boolean argsError = false;
     String tmp;
 
     if (commandLineArguments == null || commandLineArguments.length == 0)
       return;
 
     LongOpt[] longopts = new LongOpt[] {
       new LongOpt ("VT",  LongOpt.REQUIRED_ARGUMENT, null, 0) 
     };
     Getopt g = new Getopt ("cytoscape", commandLineArguments, argSpecificationString, longopts);
     g.setOpterr(false); // We'll do our own error handling
 
     int c;
     while ( ( c = g.getopt() ) != -1 ) {
       switch (c) {
       case 0:
         viewThreshold = new Integer(g.getOptarg());
       case 't':
         viewType = g.getOptarg();
         break;
       case 'n':
         nodeAttributeFilenames.add ( g.getOptarg() );
         break;
       case 'j':
         edgeAttributeFilenames.add ( g.getOptarg () );
         break;
       case 'g':
         addGeometryFilename( g.getOptarg() );
         break;
       case 'b':
         bioDataDirectory = g.getOptarg();
         break;
       case 'i':
         addInteractionsFilename( g.getOptarg() );
         break;
       case 'l':
         defaultLayoutStrategy = g.getOptarg();
         break;
       case 'e':
         expressionFilename = g.getOptarg();
         break;
       case 'h':
         helpRequested = true;
         break;
       case 'p':
         projectFilename = g.getOptarg();
         break;
       case 's':
         defaultSpeciesName = g.getOptarg();
         break;
       case 'x':
         copyExpToAttribs = false;
         break;
       case 'v':
         displayVersion = true;
         break;
       case 'y':
         //y-files or giny switch
         graphLibrary = g.getOptarg();
         if (graphLibrary != "y-files")
           yfiles = false;
         break;
       case 'c':
         // This is the "canonicalization" switch
         canonicalize = false;
         break;
       case '?': // Optopt==0 indicates an unrecognized long option, which is reserved for plugins 
         int theOption = g.getOptopt();
         if (theOption != 0 )
           errorMessages.append ("The option '" + (char)theOption + "' is not valid\n");
         break;
       default:
         errorMessages.append ("unexpected argument: " + c + "\n");
         inputsError = true;
         break;
       } // switch on c
     }   
 
     if (!inputsError)
       inputsError = !legalArguments ();
 
   } // parseArgs
   //---------------------------------------------------------------------------------
   /**
    *  any values read from properties may be overridden at the command line; be sure
    *  that 'parseArgs' is called later than this method
    */
   protected void getConfigurationsFromProperties ()
   {
     defaultLayoutStrategy = props.getProperty ("defaultLayoutStrategy", defaultLayoutStrategy);
     String undoString = props.getProperty("enableUndo");
     if (undoString != null) {enableUndo=Boolean.valueOf(undoString).booleanValue();}
   }
   //---------------------------------------------------------------------------------
   /**
    *  a project file contains one or more lines, each of which is a key/value pair.
    *  by example (using every possible key): 
    * 
    *  <code>
    *   sif=galFiltered.sif
    *   gml=galFiltered.gml
    *   noa=nodeAttributes1.noa
    *   noa=nodeAttributes2.noa
    *   eda=edgeAttributes1.eda
    *   eda=edgeAttributes2.eda
    *   layout=hierarchical
    *   dataServer=rmi://hazel/yeast
    *   species=Saccharomyces cerevisiae
    *   props=/net/compbio/cytoscape/projects/galFiltered/cytoscape.props
    *   canonicalizeNames=yes|no
    *  </code>
    *
    * further information:
    *  <ul>
    *   <li> Most of the possible entries are filenames.  If a filename is not absolute,
    *        then it is assumed to be relative to the parent path of the project file itself
    *   <li> Any entries on the command line, or in the various props files, override any
    *        found in the project file.  This most obviously applies to the species value.
    *   <li> It makes no sense to specify <em> both </em> a sif and a gml file, but 
    *        there is nothing here (yet) which catches that error.
    *   <li> Values set by properties files will override any values set here.  In some 
    *        cases this may not be desirable.
    *  </ul>
    *
    */
   protected void readProjectFile ()
   {
     if (projectFilename == null) return;
     boolean readingFromJar = false;
     String rawText;
 
     try {
       if (projectFilename.trim().startsWith ("jar://")) {
         readingFromJar = true;
         TextJarReader reader = new TextJarReader (projectFilename);
         reader.read ();
         rawText = reader.getText ();
       }
       else {
         File projectFile = new File (projectFilename);
         TextFileReader reader = new TextFileReader (projectFile.getPath ());
         reader.read ();
         rawText = reader.getText ();
         projectFileDirectoryAbsolute = projectFile.getAbsoluteFile().getParentFile ();
       }
     }
     catch (Exception e0) {
       throw new IllegalArgumentException ("cannot read project file: " + projectFilename);
     }
 
     String [] lines = rawText.split ("\n");
 
     // most entities name in a project file are singular:  species, sif or gml,
     // props, dataServer.  edge & node attribute files, however, are frequently
     // plural.  to support them, the helper method 'parseProjectFileText' returns
     // an array of Strings; only the first element in that array is used for
     // most entitites below
 
     String [] sifFiles = parseProjectFileText (lines, "sif");
     String [] gmlFiles = parseProjectFileText (lines, "gml");
     String [] noaFiles = parseProjectFileText (lines, "noa");
     String [] edaFiles = parseProjectFileText (lines, "eda");
     String [] exprFiles = parseProjectFileText (lines, "expr");
     String [] dataServers = parseProjectFileText (lines, "dataServer");
     String [] speciesEntries = parseProjectFileText (lines, "species");
     String [] defaultLayouts = parseProjectFileText (lines, "layout");
     String [] propsFiles = parseProjectFileText (lines, "props");
     String [] vizmapPropsFiles = parseProjectFileText (lines, "vprops");
     String [] canonicalization = parseProjectFileText (lines, "canonicalizeNames"); // whether or not canonicalization should be done
     String [] otherArgs = parseProjectFileText (lines, "arg");
     String [] graphMode = parseProjectFileText ( lines, "graphMode");
 
 
     for ( int i = 0 ; i < sifFiles.length; ++i ) {
       if ( readingFromJar ) 
         addInteractionsFilename( sifFiles[i] );
       else 
         addInteractionsFilename( absolutizeFilename( projectFileDirectoryAbsolute, sifFiles[i] ) );
     }
 
     for ( int i = 0; i < gmlFiles.length; ++i ) {
       if ( readingFromJar )
         addGeometryFilename( gmlFiles[i] );
       else 
         addGeometryFilename( absolutizeFilename( projectFileDirectoryAbsolute, gmlFiles[i] ) );
     }
 
     if (exprFiles.length >= 1) {
       if (readingFromJar)
         expressionFilename = exprFiles [0];
       else
         expressionFilename = absolutizeFilename (projectFileDirectoryAbsolute, exprFiles [0]);
     }
 
     for (int i=0; i < noaFiles.length; i++) {
       if (readingFromJar)
         nodeAttributeFilenames.add (noaFiles[i]);
       else      
         nodeAttributeFilenames.add (absolutizeFilename (projectFileDirectoryAbsolute, noaFiles [i]));
     }
     
     for (int i=0; i < edaFiles.length; i++) {
       if (readingFromJar)
         edgeAttributeFilenames.add (edaFiles[i]);
       else      
         edgeAttributeFilenames.add (absolutizeFilename (projectFileDirectoryAbsolute, edaFiles [i]));
     }
     
     if (dataServers.length >= 1) {
       String tmp = dataServers [0];
       if ((!tmp.startsWith ("rmi://")) && 
          (!tmp.startsWith ("jar://")) &&
          (!tmp.startsWith ("http://"))) {
         bioDataDirectory = absolutizeFilename (projectFileDirectoryAbsolute, tmp);
       }
       else
         bioDataDirectory = tmp;
     } // if dataServers.length > 0
 
     if (speciesEntries.length > 0) {
       defaultSpeciesName = speciesEntries [0];
     }
 
     if (defaultLayouts.length > 0) {
       defaultLayoutStrategy = defaultLayouts [0];
     }
 
     this.debugLog.append ("config.readProjectFile, propsFile count: " + propsFiles.length + "\n");
     if (propsFiles.length >= 1) {
       projectPropsFileName = propsFiles [0];
       this.debugLog.append ("config.readProjectFile, propsPropsFileName: " + 
                             projectPropsFileName + "\n");
     }
 
     this.debugLog.append ("config.readProjectFile, vizmapPropsFile count: " + vizmapPropsFiles.length + "\n");
     if (vizmapPropsFiles.length >= 1) {
       projectVizmapPropsFileName = vizmapPropsFiles [0];
       this.debugLog.append ("config.readProjectFile, vizmapPropsFileName: " + 
                             projectVizmapPropsFileName + "\n");
     }
 
     // Whether or not names in input files should be canonicalized
     if (canonicalization.length > 0){
       String canValue = canonicalization[0];
       if(canValue.equals("yes")){
         canonicalize = true;
       }else if(canValue.equals("no")){
         canonicalize = false;
       }
     }
     // giny or y-files mode?
     if (graphMode.length > 0){
       String graph = graphMode[0];
       if(graph.equals("giny")){
         yfiles = false;
       }else if(graph.equals("y-files")){
         yfiles = true;
       }
     }
 
     // this is how to pass info on to a plugin.
     if (otherArgs.length > 0) {
       int lenOld = argsCopy.length;
       int lenOther = otherArgs.length;
       int lenNew = lenOld+lenOther;
       // first make a copy that is exactly what we had before.
       String[] argsCopy2;
       argsCopy2 = new String [lenOld];
       System.arraycopy (argsCopy, 0, argsCopy2, 0, lenOld);
       // then make the new one.
       argsCopy = new String[lenNew];
       // then copy everything in.
       System.arraycopy (argsCopy2, 0, argsCopy, 0, lenOld);
       System.arraycopy (otherArgs, 0, argsCopy, lenOld, lenOther);
       //
       // arraycopy arguments, fyi:
       //
       // (Obj source, int sourceStart, Obj target, int targetStart, int size)
       //
     }
 
   } // readProjectFile
   //---------------------------------------------------------------------------------
   protected String absolutizeFilename (File parentDirectory, String filename)
   {
     if (filename.trim().startsWith ("/"))
       return filename;
     else 
       return (new File (parentDirectory, filename)).getPath ();
 
   }
   //---------------------------------------------------------------------------------
   protected String [] parseProjectFileText (String [] lines, String key)
   {
     Vector list = new Vector ();
     for (int i=0; i < lines.length; i++) {
       String line = lines [i].trim ();
       if (line.startsWith (key)) {
         String fileToRead = line.substring (line.indexOf ("=") + 1);
         list.add (fileToRead.trim());
       } // if 
     } // for i
 
     return (String []) list.toArray (new String [0]);
   
   } // parseProjectFileText
   //----------------------------------------------------------------------------------------
   protected boolean legalArguments ()
   {
     boolean legal = true;
   
     // multiple networks now supported
     //   // make sure there is just one source for the graph
     //     if ( (geometryFilename != null) && (interactionsFilename != null)) {
     //  errorMessages.append (" -  geometry & interactions both specify a graph: use only one\n");
     //       legal = false;
     //     }
 
     boolean illegalLayoutSelected = true;
     for (int i=0; i < layoutStrategies.length; i++) {
       if (defaultLayoutStrategy.equals (layoutStrategies [i])) {
         illegalLayoutSelected = false;
         break;
       }
     } // for i
   
     legal = legal && (!illegalLayoutSelected);  
 
     return legal;
 
   } // legalArguments
   //---------------------------------------------------------------------------------
   public String getErrorMessages ()
   {
     return errorMessages.toString ();
 
   }
   //---------------------------------------------------------------------------------
   public String getUsage ()
   {
     StringBuffer sb = new StringBuffer ();
     String programName = "cytoscape";
     sb.append ("usage: ");
     sb.append (programName);
     sb.append (" [optional arguments]");
     sb.append ("\n\n");
 
     sb.append ("\n      optional arguments\n");
     sb.append ("      ------------------\n");
     sb.append (" -g  <geometry file name>          (xxxx.gml)\n");
     sb.append (" -b  <bioData directory>           (./biodata)\n");
     sb.append (" -i  <interactions filename>       (yyyy.intr)\n");
     sb.append (" -e  <expression filename>         (zzz.mrna)\n");
     sb.append (" -x  (causes Cytoscape not to copy expression to attribs)\n");
     sb.append (" -s  <default species name>        (\"Saccharomyces cerevisiae\")\n");
     sb.append (" -n  <nodeAttributes filename>     (zero or more)\n");
     sb.append (" -j  <edgeAttributes filename>     (zero or more)\n");
     sb.append (" -l  <layout strategy>             (organic|hierarchical|embedded|circular)\n");
     sb.append (" -c  (suppresses automatic canonicalization of node names in input graph files)\n");
     sb.append ("\n");
 
     sb.append (" -h  (display usage)\n");
     sb.append (" -v  (display version)\n");
     sb.append (" -y  <graph library>                (default: \"y-files\")\n");
 
 
     return sb.toString ();
 
   } // getUsage
   //---------------------------------------------------------------------------------
   public String toString ()
   {
     StringBuffer sb = new StringBuffer ();
     sb.append ("---------- requested options:\n");
     sb.append ("            geometry files: " + geometryFilenames + "\n");
     sb.append ("        interactions files: " + interactionsFilenames + "\n");
     sb.append ("          expression file: " + expressionFilename + "\n");
     sb.append ("         bioDataDirectory: " + bioDataDirectory + "\n");
     sb.append ("       defaultSpeciesName: " + defaultSpeciesName + "\n");
     sb.append ("    defaultLayoutStrategy: " + defaultLayoutStrategy + "\n");
     sb.append (" graphLibrary: " + graphLibrary +"\n");
  
     for (int i=0; i < nodeAttributeFilenames.size (); i++)
       sb.append ("        nodeAttributeFile: " + (String) nodeAttributeFilenames.get(i) + "\n");
 
     for (int i=0; i < edgeAttributeFilenames.size (); i++)
       sb.append ("        edgeAttributeFile: " + (String) edgeAttributeFilenames.get(i) + "\n");
    
     return sb.toString ();
 
   } // toString 
   //---------------------------------------------------------------------------------
 } // class CytoscapeConfig
 
 
