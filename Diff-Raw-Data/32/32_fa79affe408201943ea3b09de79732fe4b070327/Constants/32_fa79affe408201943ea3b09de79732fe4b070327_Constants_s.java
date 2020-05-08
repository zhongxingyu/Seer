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
 //	Created Date :			2011-04-09
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam;
 
 import eu.serscis.sam.node.TRefLiteral;
 import eu.serscis.sam.node.TStringLiteral;
 import java.util.List;
 import java.util.Arrays;
 import org.deri.iris.api.basics.IPredicate;
 import static org.deri.iris.factory.Factory.*;
 
 public class Constants {
 	static public IPredicate ASSIGNP = BASIC.createPredicate("ASSIGN", 3);
 	static public IPredicate accessControlOnP = BASIC.createPredicate("accessControlOn", 0);
 	static public IPredicate expectFailureP = BASIC.createPredicate("expectFailure", 0);
 	static public IPredicate importP = BASIC.createPredicate("import", 2);
 	static public IPredicate definedTypeP = BASIC.createPredicate("definedType", 1);
 	static public IPredicate isRefP = BASIC.createPredicate("isRef", 1);
 	static public IPredicate isAP = BASIC.createPredicate("isA", 2);
 	static public IPredicate liveMethodP = BASIC.createPredicate("liveMethod", 3);
 	static public IPredicate hasCallSiteP = BASIC.createPredicate("hasCallSite", 2);
 	static public IPredicate catchesAllExceptionsP = BASIC.createPredicate("catchesAllExceptions", 1);
 	static public IPredicate didCall3P = BASIC.createPredicate("didCall", 3);
 	static public IPredicate didCall5P = BASIC.createPredicate("didCall", 5);
 	static public IPredicate didCallP = BASIC.createPredicate("didCall", 6);
 	static public IPredicate didGetP = BASIC.createPredicate("didGet", 4);
 	static public IPredicate didGetExceptionP = BASIC.createPredicate("didGetException", 4);
 	static public IPredicate accessAllowedP = BASIC.createPredicate("accessAllowed", 3);
 	static public IPredicate didCreateP = BASIC.createPredicate("didCreate", 4);
 	static public IPredicate mayCallObjectP = BASIC.createPredicate("mayCallObject", 4);
 	static public IPredicate callsMethodP = BASIC.createPredicate("callsMethod", 2);
 	static public IPredicate callsMethodInLocalP = BASIC.createPredicate("callsMethodInLocal", 2);
 	static public IPredicate maySendFromAnyContextP = BASIC.createPredicate("maySendFromAnyContext", 4);
 	static public IPredicate maySend5P = BASIC.createPredicate("maySend", 5);
 	static public IPredicate didReceiveP = BASIC.createPredicate("didReceive", 5);
 	static public IPredicate mayStoreP = BASIC.createPredicate("mayStore", 2);
 	static public IPredicate mayCreateP = BASIC.createPredicate("mayCreate", 3);
 	static public IPredicate mayAccept3P = BASIC.createPredicate("mayAccept", 3);
 	static public IPredicate hasParamP = BASIC.createPredicate("hasParam", 4);
 	static public IPredicate mayReturnP = BASIC.createPredicate("mayReturn", 4);
 	static public IPredicate mayThrowP = BASIC.createPredicate("mayThrow", 4);
 	static public IPredicate hasFieldP = BASIC.createPredicate("hasField", 2);
 	static public IPredicate hasConstructorP = BASIC.createPredicate("hasConstructor", 2);
 	static public IPredicate hasMethodP = BASIC.createPredicate("hasMethod", 2);
 	static public IPredicate methodNameP = BASIC.createPredicate("methodName", 2);	/* Class.method -> "method" */
 	static public IPredicate localP = BASIC.createPredicate("local", 4);
 	static public IPredicate fieldP = BASIC.createPredicate("field", 3);
 	static public IPredicate debugEdgeP = BASIC.createPredicate("debugEdge", 5);
 	static public IPredicate initialObjectP = BASIC.createPredicate("initialObject", 2);
 	static public IPredicate initialInvocation2P = BASIC.createPredicate("initialInvocation", 2);
 	static public IPredicate initialInvocationP = BASIC.createPredicate("initialInvocation", 3);
 	static public IPredicate phaseP = BASIC.createPredicate("phase", 1);
 	static public IPredicate assertionMessageP = BASIC.createPredicate("assertionMessage", 2);
 	static public IPredicate failedAssertionP = BASIC.createPredicate("failedAssertion", 1);
 	static public IPredicate assertionArrowP = BASIC.createPredicate("assertionArrow", 4);
 	static public IPredicate isInvocationP = BASIC.createPredicate("isInvocation", 1);
 	static public IPredicate ignoreEdgeForRankingP = BASIC.createPredicate("ignoreEdgeForRanking", 2);
 	static public IPredicate grantsRoleP = BASIC.createPredicate("grantsRole", 3);
 	static public IPredicate hasIdentityP = BASIC.createPredicate("hasIdentity", 2);
 	static public IPredicate guiObjectTabP = BASIC.createPredicate("guiObjectTab", 4);
 	static public IPredicate graphClusterP = BASIC.createPredicate("graphCluster", 2);
 	static public IPredicate graphClusterColourP = BASIC.createPredicate("graphClusterColour", 2);
 	static public IPredicate graphClusterLabelP = BASIC.createPredicate("graphClusterLabel", 2);
 
 	static <X> List<X> makeList(X... items) {
 		return Arrays.asList(items);
 	}
 
 	static String getString(TStringLiteral literal) {
 		String str = literal.getText();
 		String innerStr = str.substring(1, str.length() - 1);
 		return innerStr.replaceAll("\\\\", "");
 	}
 
 	static String getRef(TRefLiteral literal) {
 		String str = literal.getText();
 		String innerStr = str.substring(1, str.length() - 1);
 		return innerStr.replaceAll("\\\\", "");
 	}
 }
