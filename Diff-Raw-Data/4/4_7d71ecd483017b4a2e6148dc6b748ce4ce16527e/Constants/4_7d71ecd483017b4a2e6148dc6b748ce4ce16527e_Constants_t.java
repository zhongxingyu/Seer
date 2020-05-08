 package it.wolfed.util;
 
 public class Constants 
 {
     // About Message
    public static final String EDITOR_ABOUT = 
     Constants.EDITOR_NAME + " Workflow Light Fast Editor.\n"
     + "Version:" + Constants.EDITOR_VERSION +"\n\n"
     +  "Authors: \n"
        + "- Said Daoudagh\n"
        + "- Fabio Piro\n\n"
     + "Supervisor:\n"
            + "- Prof. Roberto Bruni";
     
     // Editor
     public static final String EDITOR_VERSION                       = "1.0.0.0";
     public static final String EDITOR_NAME                          = "WoLFEd";
     public static final String EDITOR_EXPORT_DOT                    = ".dot";
     public static final String EDITOR_EXPORT_PNML                   = ".pnml";
     
     // Styles
     public static final String STYLE_TRANSITION                     = "fillColor=white;";
     public static final String STYLE_TRANSITION_VALID               = "fillColor=white;strokeColor=green;";
     public static final String STYLE_PLACE                          = "shape=ellipse;fillColor=white;";
     public static final String STYLE_PLACE_VALID                    = "shape=ellipse;fillColor=white;strokeColor=green;";
     public static final String STYLE_PLACE_SPECIAL_VALID            = "shape=doubleEllipse;fillColor=white;strokeColor=#31B404";
     public static final String STYLE_PLACE_SPECIAL_INVALID          = "shape=doubleEllipse;fillColor=white;strokeColor=#FF0000";
     public static final String STYLE_ARC                            = "defaultEdge";
     public static final String STYLE_ARC_FLOW_CONNECTED             = "strokeColor=green;";
     public static final String STYLE_ARC_FLOW_UNCONNECTED           = "strokeColor=red;dashed=1;";
     public static final String STYLE_ARC_WITH_INTERFACE             = "strokeColor=orange;";
     public static final String STYLE_INTERFACE                      = "shape=doubleEllipse;fillColor=white;strokeColor=orange;";
     
     // Operations
     public static final String OPERATION_PREFIX                     = "n";
     public static final String OPERATION_SEQUENCING                 = "Sequencing";
     public static final String OPERATION_ALTERNATION                = "Alternation";
     public static final String OPERATION_CLONEGRAPH                 = "CloneGraph";
     public static final String OPERATION_DEFFEREDCHOICE             = "DefferedChoice";
     public static final String OPERATION_EXPLICITCHOICE             = "ExplicitChoice";
     public static final String OPERATION_ITERATIONONEORMORE         = "IterationOneOrMore";
     public static final String OPERATION_ITERATIONONESERVEPERTIME   = "IterationOneServePerTime";
     public static final String OPERATION_ITERATIONZEROORMORE        = "IterationZeroOrMore";
     public static final String OPERATION_FULLMERGE                  = "FullMerge";
     public static final String OPERATION_MUTUALEXCLUSION            = "MutualExclusion";
     public static final String OPERATION_PARALLELISM                = "Parallelism";
     
     // Layouts
     public static final String LAYOUT_VERTICALTREE                  = "VerticalTree";
     public static final String LAYOUT_HORIZONTALTREE                = "HorizontalTree";
     public static final String LAYOUT_HIERARCHICAL                  = "Hierarchical";
     public static final String LAYOUT_ORGANIC                       = "Organic";
 
     // Pnml
     public static final String PNML_TAG                             = "pnml";
     public static final String PNML_PLACE                           = "place";
     public static final String PNML_TRANSITION                      = "transition";
     public static final String PNML_ARC                             = "arc";
     public static final String PNML_TARGET                          = "target";
     public static final String PNML_SOURCE                          = "source";
     public static final String PNML_NET                             = "net";
     public static final String PNML_ID                              = "id";
     public static final String PNML_INITIALMARKING                  = "initialMarking";
     public static final String PNML_NAME                            = "name";
     public static final String PNML_TYPE                            = "type";
     public static final String PNML_GRAPHICS                        = "graphics";
     public static final String PNML_GRAPHICS_POSITION               = "position";
     public static final String PNML_GRAPHICS_POSITION_X             = "x";
     public static final String PNML_GRAPHICS_POSITION_Y             = "y";
     public static final String PNML_TEXT                            = "text";
     public static final String PNML_INTERFACE                       = "interface";
     public static final String PNML_INTERFACES                      = "interfaces"; 
     public static final String PNML_TOOL_SPECIFIC                   = "toolspecific";
     public static final String PNML_TOOL                            = "tool";
     public static final String PNML_TOOL_VERSION                    = "version";
    
 //    public static final String NAMED_TARGET = "namedTarget";
 //    public static final String POSITION = "position";
 //    public static final String RESOURCES = "resources";
 //    public static final String TEXT = "text";
 //    public static final String INITIAL_MARKING = "initialMarking";
 //    public static final String OPERATOR = "operator";
 //    public static final String BOUNDS = "bounds";
 //    public static final String DIMENSION = "dimension";
 //    public static final String GRAPHICS = "graphics";
 //    public static final String INSCRIPTION = "inscription";
 //    public static final String OFFSET = "offset";
 //    public static final String TOOL_SPECIFIC = "toolspecific";
 //    public static final String VERTICALLAYOUT = "verticalLayout";
 //    public static final String TREEWIDTHRIGHT = "treeWidthRight";
 //    public static final String OVERVIEWPANELVISIBLE = "overviewPanelVisible";
 //    public static final String TIME = "time";
 //    public static final String DISPLAYPROBABILITYON = "displayProbabilityOn";
 //    public static final String TIMEUNIT = "timeUnit";
 //    public static final String SIMULATIONS = "simulations";
 //    public static final String PROBABILITY = "probability";
 //    public static final String ORIENTATION = "orientation";
 //    public static final String DISPLAYPROBABILITYPOSITION = "displayProbabilityPosition";
 //    public static final String TREEHEIGHTOVERVIEW = "treeHeightOverview";
 //    public static final String VARIABLES = "variables";
 //    public static final String TREEPANELVISIBLE = "treePanelVisible";
 //    public static final String PARTNERLINKS = "partnerLinks";
 //    public static final String TOOLSPECIFIC = "toolspecific";
 //    public static final String PNML = "pnml";
 }
