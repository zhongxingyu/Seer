 package edu.wustl.cab2b.server.path.pathgen;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Set;
 
 import edu.wustl.cab2b.common.util.Constants;
 import edu.wustl.cab2b.server.path.PathConstants;
 
 /**
  * Util to write a set of paths to a file. Each path is written as one line. The
  * line is of the following form :<br>
  * srcNodeId###interNodeid1_interNodeid2_..._interNodeid2n###destNodeId<br>
  * e.g. 1###6_3_9_7###23; here 1 is srcNodeId, 23 is destNodeId and {6,3,9,7}
  * are the intermediate nodes' ids.<br>
  * The caller can specify an array of "nodeTags"; a node tag is something that
  * the caller want written instead of the nodeId. e.g. if each node represents a
  * city in the original graph, then corresponding city names can be present as
  * the nodeTag.
  * @author srinath_k
  */
 public class PathToFileWriter {
 
 	/**
 	 * Write a set of paths to a file
 	 * @param paths
 	 * @param nodeTags
 	 * @param append
 	 */
     public static void writePathsToFile(Set<Path> paths, Object[] nodeTags, boolean append) {
         try {
             FileWriter writer = new FileWriter(new File(PathConstants.PATH_FILE_NAME), append);
             String endNodesDelimiter = PathConstants.FIELD_SEPARATOR;
             String intermediateNodesDelimiter = Constants.CONNECTOR;
 
             for (Path path : paths) {
                 Node srcNode = path.fromNode();
                 Node destNode = path.toNode();
                 StringBuffer temp = new StringBuffer();
 
                 if (nodeTags != null) {
                     temp.append(nodeTags[srcNode.getId()]);
                 } else {
                     temp.append(srcNode.getId());
                 }
                 temp.append(endNodesDelimiter);
 
                 boolean isFirst = true;
                 for (Node interNode : path.getIntermediateNodes()) {
                     if (!isFirst) {
                         temp.append(intermediateNodesDelimiter);
                     }
                     if (nodeTags != null) {
                         temp.append(nodeTags[interNode.getId()]);
                     } else {
                         temp.append(interNode.getId());
                     }
                     isFirst = false;
                 }
                 temp.append(endNodesDelimiter);
                 if (nodeTags != null) {
                     temp.append(nodeTags[destNode.getId()]);
                 } else {
                     temp.append(destNode.getId());
                 }
                 temp.append(PathConstants.LINE_SEPERATOR);
                 writer.write(temp.toString());
                 writer.flush();
             }
             writer.close();
        } catch (IOException ioex) {
            ioex.printStackTrace();
         }
     }
 }
