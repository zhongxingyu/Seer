 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.tnavigator;
 
 import java.util.HashMap;
 import java.util.Map;
 
import om.axis.qengine.Resource;
 import om.tnavigator.auth.UserDetails;
 
 /** Data stored about particular user */
 public class UserSession
 {
 	/** Cookie (key of map too, but duplicated here) */
 	String sCookie;
 	
 	/** Question session ID for question engine */
 	OmServiceBalancer.OmServiceSession oss=null;
 	
 	/** Sequence used to check you don't do things out of order */
 	String sSequence;
 	
 	/** Random seed for session */
 	long lRandomSeed;
 	
 	/** Time of session start */
 	long lSessionStart=System.currentTimeMillis();
 	
 	/** Time of last action in session */
 	long lLastAction=System.currentTimeMillis();
 	
 	/** ID of in-progress test deployment */
 	public String sTestID=null;
 	
 	/** Test version for this user (null for single question) */
 	TestGroup tg;
 	
 	/** Test items, linearised */
 	TestLeaf[] atl;
 	
 	/** Index within test items */
 	int iIndex;
 	
 	/** Current test definition (null for single question) */
 	TestDefinition tdDefinition=null;
 	
 	/** Current test deployment */
 	public TestDeployment tdDeployment=null;
 	
 	/** User login details */
 	UserDetails ud=null;
 	
 	/** OUCU (real or fake) */
 	String sOUCU;
 	
 	/** 
 	 * Hash of cookies that determine ud and without which the session 
 	 * should be dumped
 	 */
 	int iAuthHash=0;
 	
 	/** Whether they have admin access */
 	boolean bAdmin=false;
 	
 	/** Whether they can also view reports */
 	boolean bAllowReports=false;
 	
 	/** Map of String (filename) -> Resource */
	Map<String,Resource> mResources=new HashMap<String,Resource>();
 	
 	/** CSS */
 	String sCSS="";
 	
 	/** Progress info */
 	String sProgressInfo="";
 	
 	/** Database ID for test, question, sequence */
 	int iDBti,iDBqi,iDBseq;
 	
 	/** True if they have finished the test */
 	boolean bFinished;
 	
 	/** 
 	 * Set true only for specific requests that are permitted after the forbid
 	 * date (up to the forbid-extension date)
 	 */
 	boolean bAllowAfterForbid;
 	
 	/** 
 	 * True if their browser has been checked and found OK, or they have
 	 * decided to ignore the warning. 
 	 */
 	boolean bCheckedBrowser;
 
 	/**
 	 * Once we've seen their OUCU and checked that we only hold one session
 	 * for them, this is set to OUCU-testID.
 	 */
 	String sCheckedOUCUKey;
 	
 	/** If not -1, this indicates a fixed variant has been selected */
 	int iFixedVariant=-1;
 	
 	/** Index increments whenever there is a new CSS version */
 	int iCSSIndex=0;
 	
 	/** 
 	 * Very hacky way to store the situation when confirm emails are sent
 	 * or not. 1=sent, -1=error
 	 */
 	int iEmailSent=0;
 }
