 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-08-17
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.io.FileWriter;
 import java.io.File;
 import org.deri.iris.api.IKnowledgeBase;
 import org.deri.iris.api.basics.IPredicate;
 import org.deri.iris.api.basics.IQuery;
 import org.deri.iris.api.basics.ITuple;
 import org.deri.iris.api.basics.ILiteral;
 import org.deri.iris.api.terms.ITerm;
 import org.deri.iris.storage.IRelation;
 import static org.deri.iris.factory.Factory.*;
 
 public class Graph {
 	static public void graph(IKnowledgeBase knowledgeBase, File outputPngFile) throws Exception {
 		ITuple xAndY = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"));
 
 		IPredicate graphNodePredicate = BASIC.createPredicate("visibleGraphNode", 2);
 		ILiteral graphNodeLiteral = BASIC.createLiteral(true, graphNodePredicate, xAndY);
 		IQuery graphNodeQuery = BASIC.createQuery(graphNodeLiteral);
 		IRelation graphNodeResults = knowledgeBase.execute(graphNodeQuery);
 
 		ITuple xAndYandAttr = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"), TERM.createVariable("Attr"));
 		IPredicate graphEdgePredicate = BASIC.createPredicate("visibleGraphEdge", 3);
 		ILiteral graphEdgeLiteral = BASIC.createLiteral(true, graphEdgePredicate, xAndYandAttr);
 		IQuery graphEdgeQuery = BASIC.createQuery(graphEdgeLiteral);
 		IRelation graphEdgeResults = knowledgeBase.execute(graphEdgeQuery);
 
 		ITuple xAndYandAttrAndLabel = BASIC.createTuple(TERM.createVariable("X"), TERM.createVariable("Y"),
 								TERM.createVariable("Attr"), TERM.createVariable("Label"));
 		IPredicate graphEdgeLabelPredicate = BASIC.createPredicate("visibleGraphEdge", 4);
 		ILiteral graphEdgeLabelLiteral = BASIC.createLiteral(true, graphEdgeLabelPredicate, xAndYandAttrAndLabel);
 		IQuery graphEdgeLabelQuery = BASIC.createQuery(graphEdgeLabelLiteral);
 		IRelation graphEdgeLabelResults = knowledgeBase.execute(graphEdgeLabelQuery);
 
 		IQuery ignoredForRankingQuery = BASIC.createQuery(BASIC.createLiteral(true, Constants.ignoreEdgeForRankingP, xAndY));
 		IRelation ignoredForRankingResults = knowledgeBase.execute(ignoredForRankingQuery);
 
 		graph(graphNodeResults, graphEdgeResults, graphEdgeLabelResults, ignoredForRankingResults, outputPngFile);
 	}
 
 	static private String format(ITerm term) {
 		return "\"" + term.getValue().toString() + "\"";
 	}
 
 	static private void graph(IRelation nodes, IRelation edges, IRelation labelledEdges, IRelation ignoredForRanking, File pngFile) throws Exception {
 		File dotFile = File.createTempFile("SAM-tmp", ".dot");
 
 		FileWriter writer = new FileWriter(dotFile);
 		writer.write("digraph a {\n");
 		//writer.write("  concentrate=true;\n");
 		//writer.write("  rankdir=LR;\n");
 		writer.write("  node[shape=plaintext];\n");
 
 		for (int t = 0; t < nodes.size(); t++) {
 			ITuple tuple = nodes.get(t);
 			ITerm nodeId = tuple.get(0);
 			String nodeAttrs = tuple.get(1).getValue().toString();
 			writer.write(format(nodeId) + " [" + nodeAttrs + "];\n");
 		}
 
 		Set<String> dotEdges = new HashSet<String>();
 		Set<String> doubles = new HashSet<String>();
 
 		for (int t = 0; t < edges.size(); t++) {
 			ITuple tuple = edges.get(t);
 			ITerm a = tuple.get(0);
 			ITerm b = tuple.get(1);
 			String edgeAttrs = tuple.get(2).getValue().toString();
 
 			ITuple pair = BASIC.createTuple(a, b);
 			if (ignoredForRanking.contains(pair)) {
 				edgeAttrs += ",constraint=false";
 			}
 
 			String reverse = format(b) + " -> " + format(a) + " [" + edgeAttrs;
 
 			if (dotEdges.contains(reverse)) {
 				doubles.add(reverse);
 			} else {
 				String line = format(a) + " -> " + format(b) + " [" + edgeAttrs;
 				dotEdges.add(line);
 			}
 		}
 
 		for (String edge : dotEdges) {
 			if (doubles.contains(edge)) {
 				if (!edge.endsWith("[")) {
 					edge += ",";
 				}
 				edge += "dir=both";
 			}
 			writer.write(edge + "];\n");
 		}
 
 		for (int t = 0; t < labelledEdges.size(); t++) {
 			ITuple tuple = labelledEdges.get(t);
 			ITerm a = tuple.get(0);
 			ITerm b = tuple.get(1);
 			String edgeAttrs = tuple.get(2).getValue().toString();
 			String labelAttr = "label=" + format(tuple.get(3));
 			if (edgeAttrs.equals("")) {
 				edgeAttrs = labelAttr;
 			} else {
 				edgeAttrs += "," + labelAttr;
 			}
 
 			writer.write(format(a) + " -> " + format(b) + " [" + edgeAttrs + "];\n");
 		}
 
 		writer.write("}\n");
 		writer.close();
 
 		String dotBinary = "dot";
 		String graphvizHome = System.getenv("GRAPHVIZ_HOME");
 		if (graphvizHome != null) {
 			File bin = new File(graphvizHome, "bin");
 			File dotBinaryFile = new File(bin, "dot.exe");
 			if (bin.exists()) {
 				dotBinary = dotBinaryFile.toString();
 			} else {
 				dotBinaryFile = new File(bin, "dot");
 				dotBinary = dotBinaryFile.toString();
 			}
 		}
 
 		Process proc = Runtime.getRuntime().exec(new String[] {dotBinary, "-o" + pngFile.getAbsolutePath(), "-Tpng", dotFile.getAbsolutePath()});
 		int result = proc.waitFor();
 		if (result != 0) {
			throw new RuntimeException("dot failed to run: exit status = " + result + "; see " + dotFile);
 		}

		dotFile.delete();
 	}
 }
