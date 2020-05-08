 package com.redhat.qe.pulp.v2_cli.tests;
 
 import java.util.Hashtable;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Enumeration;
 
 import org.testng.annotations.Test;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import com.redhat.qe.auto.testng.TestNGUtils;
 import com.redhat.qe.Assert;
 import org.testng.annotations.DataProvider;
 
 import com.redhat.qe.pulp.v2_cli.tasks.PulpTasks;
 import com.redhat.qe.pulp.v2_cli.base.PulpTestScript;
 
 import com.redhat.qe.pulp.abstraction.PulpAbstraction;
 import com.redhat.qe.tools.SSHCommandRunner;
 
 public class RepoTest extends PulpTestScript {
 	public RepoTest() {
 		super();
 		Hashtable<String, String> rules = new Hashtable<String, String>();
 		
 		rules.put("repoStatusName", "^Id:[\\s]+([a-zA-Z0-9\\_]+)[\\s]*$");
 		rules.put("repoStatusPkgCount", "^Content Unit Count:[\\s]+([0-9]+)[\\s]*$");
 		//rules.put("repoStatusSyncing", "^Currently syncing:[\\s]+([a-zA-Z0-9\\_\\(\\)\\%\\s\\.]+)[\\s]*$");
 
 		rules.put("repoScheduleHeader", "^[\\s]*Available Repository Schedules[\\s]*$");
 		rules.put("repoScheduleLabel","^Label[\\s]+([a-zA-Z0-9\\_]+)[\\s]*$");
 		rules.put("repoSchedule", "^Schedule[\\s]+([0-9\\*\\-\\s\\,]+)[\\s]*$");
 		
 		pulpAbs.appendRegexCriterion(rules);
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="localRepoData")
 	public void createTestRepo(ArrayList<String> fields, boolean cancelable) {
 		servertasks.createTestRepo(fields);
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="localRepoData", dependsOnMethods="createTestRepo")
 	public void verifyCancelSync(ArrayList<Object> fields, boolean cancelable) {
 		if (cancelable) {
 			String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 			servertasks.syncTestRepo(repoId, false);
 			String taskId = findSyncTask(repoId).replace("Task Id:", "").trim();
 
 			try {
 				Thread.currentThread();
 				Thread.sleep(10000);
 				servertasks.cancelSyncTestRepo(taskId);
 				Thread.currentThread();
 				Thread.sleep(10000);
 			}
 			catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 			Assert.assertEquals(findSyncTask(repoId), "", "Attempt to cancel sync");
 		}
 	}
 
 	public String findSyncTask(String repoId) {
 		String out = servertasks.getTaskList();
 		String[] diced = out.split("\n");
 		try {	
 			for (int i=1; i< diced.length; i++) {
 				String prev = diced[i-1];
 				String line = diced[i];
 				String next = diced[i+1];
 				if (line.contains("Resources:   " + repoId + " (repository)") && prev.contains("Operations:  sync") && next.contains("State:       Running")) {
 					return diced[i+5].replace("Task Id:", "");
 				}
 			}
 		} catch (ArrayIndexOutOfBoundsException iobe) { return ""; }
 		return "";
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="localRepoData", dependsOnMethods="verifyCancelSync", alwaysRun=true)
 	public void cleanupPartialSyncRepo(ArrayList<Object> fields, boolean cancelable) {
 		if (cancelable) {
 			String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 			servertasks.deleteTestRepo(repoId);
 		}
 	}
 
 	// TODO: verify wipe
 	@Test (groups={"testRepo"}, dataProvider="localRepoData", dependsOnMethods="cleanupPartialSyncRepo")
 	public void verifyCleanup(ArrayList<Object> fields, boolean cancelable) {
 		if (cancelable) {
 			String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 		}
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="localRepoData", dependsOnMethods="verifyCleanup")
 	public void syncLocalRepo(ArrayList<Object> fields, boolean cancelable) {
 		String repoFeed = ((String)fields.get(1)).replace("--feed=", "");
 		if (!cancelable && !repoFeed.equals("")) {
 			String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 			servertasks.syncTestRepo(repoId, true);
 		}
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="localRepoData", dependsOnMethods="syncLocalRepo")
 	public void verifySync(ArrayList<Object> fields, boolean cancelable) {
 		if (!cancelable) {
 			String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 			String repoPath = ((String)fields.get(2)).replace("--relative-url=", "");
 
 			String folderPath = "";
 			if (repoPath.equals("")) {
 				// TODO: The update part of the path is hardcoded...I'm taking advantage of the
 				// fact that I know all my test repos are synced to the same source.
 				folderPath = "/var/lib/pulp/repos/pub/updates";
 			}
 			else {
 				folderPath = "/var/lib/pulp/repos" + repoPath;
 			}	
 
 			//ArrayList<Hashtable<String, String>> filteredResult = pulpAbs.match(servertasks.listRepo(true));
 			ArrayList<Hashtable<String, ArrayList<String>>> filteredResult = this.match(servertasks.listRepo(true), ":");
 			int statusResult = 0;
 			Hashtable<String, ArrayList<String>> r = pulpAbs.findRepo(filteredResult, repoId, "Repo Id");
 			if (r.containsKey("Content Unit Count")) {
 				statusResult = Integer.parseInt(pulpAbs.findRepo(filteredResult, repoId, "Repo Id").get("Content Unit Count").get(0));
 			}
 			String[] intermediateResult = servertasks.getRepoPackageList(repoId).split("\n");
 			// filter the output so it only has the pkg 
 			ArrayList<String> pkgResult = new ArrayList<String>();
 			for (String s : intermediateResult) {
 				if (s.contains("Filename:")) {
 					pkgResult.add(s);
 				}
 			}
 
 			intermediateResult = servertasks.listPkgGroup(repoId, true).split("\n");
 			// filter the output so it only has the pkg group
 			ArrayList<String> pkgGroupResult = new ArrayList<String>();
 			for (String s : intermediateResult) {
 				if (s.contains("Repo Id:")) {
 					pkgGroupResult.add(s);
 				}
 			}
 
 			intermediateResult = servertasks.listCategory(repoId).split("\n");
 			// filter the output so it only has the pkg category
 			ArrayList<String> categoryResult = new ArrayList<String>();
 			for (String s : intermediateResult) {
 				if (s.contains("Repo Id:")) {
 					categoryResult.add(s);
 				}
 			}
 
 			intermediateResult = servertasks.listDistribution(repoId).split("\n");
 			// filter the output so it only has the dist
 			ArrayList<String> distResult = new ArrayList<String>();
 			for (String s : intermediateResult) {
 				if (s.contains("Id:")) {
 					distResult.add(s);
 				}
 			}
 
 			// filter the output so it only has the errata 
 			intermediateResult = servertasks.listErrata(repoId).split("\n");
 			ArrayList<String> errataResult = new ArrayList<String>();
 			for (String s : intermediateResult) {
 				if (s.contains("Id:")) {
 					errataResult.add(s);
 				}
 			}
 
 			int expectedResultNum = 0;
 			//if (pkgResult.size() > 0) {
 			expectedResultNum = pkgResult.size() + errataResult.size() + pkgGroupResult.size() + categoryResult.size() + distResult.size();
 			//}
 
 			Assert.assertEquals(statusResult, expectedResultNum, "Determing if the reported number of pkg available is equivalent.");
 
 			// bind the repo and test from yum
 			for (List<Object> consumer : getConsumerData()) {
 				String consumerId = (String)consumer.get(0);
 				PulpTasks task = (PulpTasks)consumer.get(1);
 
 				task.bindConsumer(consumerId, repoId, true);
 			}
 			int pkg_count = getYumPkgCount(servertasks.yumRepoList(), repoId);
 			Assert.assertEquals(pkg_count, pkgResult.size(), "Determining if the reported number of pkg equals yum output.");
 		}
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="localRepoData", dependsOnMethods="verifySync")
 	public void uploadPkg(ArrayList<Object> fields, boolean cancelable) {
 		String repoFeed = ((String)fields.get(1)).replace("--feed=", "");
 		if (repoFeed.equals("")) {
 			String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 			for (List<Object> pkg : getPkgData()) {
 				String rpmName = (String)pkg.get(0);
 				String rpmURL = (String)pkg.get(1);
 				String rpmDir = (String)pkg.get(2);
 				servertasks.uploadContent(repoId, rpmName, rpmURL, rpmDir, true);
 /*
 				// Temporary workaround for content bz 752098
 				while (!servertasks.getMetadataStatus(repoId).contains("finished")) {
 					try {
 						Thread.currentThread().sleep(10000);
 					} catch (Exception e) {}
 				}
 */
 				try { 
 					Thread.currentThread().sleep(5000);
 				} catch (Exception e) {}
 
 				verifyUpload(repoId, rpmName);
 			}
 		}
 	}
 /*
 	@Test (groups={"testRepo"}, dataProvider="localRepoData", dependsOnMethods="uploadPkg")
 	public void uploadFile(ArrayList<Object> fields, boolean cancelable) {
 		String repoFeed = ((String)fields.get(1)).replace("--feed=", "");
 		if (repoFeed.equals("")) {
 			String repoId = ((String)fields.get(0)).replace("--id=", "");
 		}
 	}
 */
 
 	@Test (groups={"testRepo"}, dataProvider="modRepoData")
 	public void createModRepo(ArrayList<String> fields) {
 		servertasks.createTestRepo(fields);
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="modRepoData", dependsOnMethods="createModRepo")
 	public void updateRepo(ArrayList<Object> fields) {
 		// TODO: change everything including feed url
 		String repoId = ((String)fields.get(0));
 		ArrayList<String> repoOpts = new ArrayList<String>();
 		repoOpts.add(repoId);
 		repoOpts.add("--display-name=mod_repo_name");
 		repoOpts.add("--feed=http://qeblade20.rhq.lab.eng.bos.redhat.com/pub/updates");
 		servertasks.updateTestRepo(repoOpts, true);
 	}
 
 	@Test (groups={"testRepo"}, dataProvider="modRepoData", dependsOnMethods={"updateRepo"})
 	public void verifyRepoUpdate(ArrayList<Object> fields) {
 		String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 		String repoLocation = ((String)fields.get(2)).replace("--feed=", "");
 		ArrayList<Hashtable<String, ArrayList<String>>> filteredResult = this.match(servertasks.listRepo(true), ":");
 		Hashtable<String, ArrayList<String>> result = pulpAbs.findRepo(filteredResult, repoId);
 		
 		Assert.assertEquals(result.get("Id").get(0), repoId, "comparing repoId to " + repoId);
 		Assert.assertEquals(result.get("Display Name").get(0), "mod_repo_name", "comparing repoName to " + repoId);
 		Assert.assertEquals(result.get("Feed URL").get(0), "http://qeblade20.rhq.lab.eng.bos.redhat.com/pub/updates", "comparing repoLocation to " + repoLocation);
 	}
 
 	//@Test (groups={"testRepo"}, dataProvider="modRepoData", dependsOnMethods={"verifyRepoUpdate"})
 	@Test (groups={"testRepo"}, dataProvider="modRepoData", dependsOnMethods={"createModRepo"})
 	public void syncModRepo(ArrayList<Object> fields) {
 		String repoId = ((String)fields.get(0)).replace("--repo-id=", "");
 		//servertasks.syncTestRepo(repoId, true);
 	}
 
 	// Negative
 	@Test (groups={"testRepo"}, dataProvider="modRepoData", dependsOnMethods="syncModRepo")
 	public void updateSyncedRepo(ArrayList<Object> fields) {
 		// update feed and assert for failure but not traceback
 		String repoId = ((String)fields.get(0));
 		ArrayList<String> repoOpts = new ArrayList<String>();
 		repoOpts.add(repoId);
 		repoOpts.add("--feed=http://qeblade20.rhq.lab.eng.bos.redhat.com/pub/updates");
 		servertasks.updateTestRepo(repoOpts, false);
 		// verify that the feed didn't change
 	}
 
 	//@Test (groups={"testRepo"}, dataProvider="chunkyUploadData")
 	//public void uploadAndVerifyChunkData(String fileName, String fileURL, String tmpDir, String chunkSize) {
 		/* *Note* 
 		 * Mechanically, I'm not sure if spawning upload in background is equivalent/worse/better than setting up another
 		 * PulpTasks obj., have servertasks run upload & while(sshCommandRunner.wait()) {
 		 * obj.getSegments();
 		 * }
 		 * 
 		 * The main concern is that the upload task is actually not complete until the CLI call returns. 
 		 * The test can verify the chunk result and try to cleanup prior the the initial CLI call returns. 
 		 */
 		
 		//try {
 		//	SSHCommandRunner subRunner = new SSHCommandRunner(serverHostname, sshUser, sshPassphrase, sshKeyPrivate, sshKeyPassphrase, null);	
 		//	String actualChunkSize = servertasks.uploadChunkedContent(fileName, fileURL, tmpDir, chunkSize, subRunner);
 		//	Assert.assertEquals(chunkSize, actualChunkSize, "Comparing actual chunk size w/ passed in chunk size.");
 		//}
 		//catch (Exception e) {
 			//e.printStackTrace();
 		//}
 	//}
 
 	@Test (groups={"testRepo"}, dataProvider="repoFilterData")
 	public void testFilter(Hashtable<String, String> opts, Hashtable<String, String> expectedResults) {
 		Assert.assertTrue(this.testThroughOptions("pulp-admin rpm repo search --help", opts, expectedResults));
 	}
 
 	// NEG Tests
 	public void createRepoCert() {
 	}
 
 	@Test (groups={"testRepo"})
	public void dupeRepoId() {
		servertasks.createTestRepo("dupe_test_repo", true);
		servertasks.createTestRepo("dupe_test_repo", false);
 	}
 	
 	@Test (groups={"testRepo"}, dataProvider="negRepoData")
 	public void badRepoCreate(ArrayList<String> fields) {
 		// special char names
 		// invalid feed
 		servertasks.createTestRepo(fields, false);
 	}
 /*
 	@AfterClass(groups={"testRepo"}, alwaysRun=true)
 	public void waitForUploadFinish() {
 		// just in case
 		try {
 			while (true) {
 				if (servertasks.getSegments().equals("")) {
 					break;
 				}
 				Thread.currentThread();
 				Thread.sleep(5000);
 			}
 			// apparently, there's a time period where chunks disappear from dir and the file shows up on content list
 			Thread.currentThread();
 			Thread.sleep(10000);
 		}
 		catch (Exception e) {
 		}
 	}
 */
 	//@AfterClass(groups={"testRepo"}, alwaysRun=true, dependsOnMethods="waitForUploadFinish")
 	@AfterClass(groups={"testRepo"}, alwaysRun=true)
 	public void deleteLocalRepo() {
 		String result = "";
 		for (List<Object> repo : getLocalRepoData()) {
 			ArrayList repoData = (ArrayList)repo.get(0);
 			String repoId = ((String)repoData.get(0)).replace("--repo-id=", "");
 			servertasks.deleteTestRepo(repoId);
 		}
 		for (List<Object> repo : getModRepoData()) {
 			ArrayList repoData = (ArrayList)repo.get(0);
 			String repoId = ((String)repoData.get(0)).replace("--repo-id=", "");
 			servertasks.deleteTestRepo(repoId);
 		}
		servertasks.deleteTestRepo("dupe_test_repo");
 		servertasks.deleteTestRepo("bad_test_repo");
 	}
 
 	@AfterClass (groups={"testRepo"}, alwaysRun=true, dependsOnMethods={"deleteLocalRepo"})
 	public void verifyDeleteTestRepo() {
 		for (List<Object> repo : getLocalRepoData()) {
 			ArrayList repoData = (ArrayList)repo.get(0);
 			String repoId = (String)repoData.get(0);
 			ArrayList<Hashtable<String, ArrayList<String>>> filteredResult = this.match(servertasks.listRepo(true), ":");
 			Hashtable<String, ArrayList<String>> result = pulpAbs.findRepo(filteredResult, repoId);
 		}
 	}
 
 	//@AfterClass(groups={"testRepo"}, alwaysRun=true, dependsOnMethods="waitForUploadFinish") 
 	/*
 	@AfterClass(groups={"testRepo"}, alwaysRun=true)
 	public void deleteChunkUploadData() {
 		for (List<Object> file : getCampbellChunkyIttyBittyPieces()) {
 			servertasks.deleteOrphanedContent((String)file.get(0));
 		}
 	}
 	*/
 
 	// Utility
 	// ###########################################################################
 	public void verifyUpload(String repoId, String rpmFile) {
 		String[] expectedResult = servertasks.getRepoPackageList(repoId).split("\n");
 		ArrayList<String> rpmsFound = new ArrayList<String>();
 		for (String s : expectedResult) {
 			String[] rpmLine = s.split("Filename: ");
 			if (rpmLine.length > 1) {
 				rpmsFound.add(rpmLine[1].trim());
 			}
 		}
 
 		Assert.assertTrue(rpmsFound.contains(rpmFile), "Verifying " + rpmFile + " is in the repo."); }
 	
 	// ###########################################################################
 	@DataProvider(name = "localRepoData")
 	public Object[][] localRepoData() {
 		return TestNGUtils.convertListOfListsTo2dArray(getLocalRepoData());
 	}
 	public List<List<Object>> getLocalRepoData() {
 		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
 
 		ArrayList<String> repoOpts = new ArrayList<String>();
 		repoOpts.add("--repo-id=faux_test_repo");
 		repoOpts.add("--feed=http://download.devel.redhat.com/released/F-13/GOLD/Fedora/x86_64/os");
 		repoOpts.add("");
 		data.add(Arrays.asList(new Object[]{repoOpts, true}));
 
 		repoOpts = new ArrayList<String>();
 		repoOpts.add("--repo-id=relative_test_repo");
 		repoOpts.add("--feed=http://qeblade20.rhq.lab.eng.bos.redhat.com/pub/updates/");
 		repoOpts.add("--relative-url=/pulp_relative_repo/");
 		data.add(Arrays.asList(new Object[]{repoOpts, false}));
 
 		repoOpts = new ArrayList<String>();
 		repoOpts.add("--repo-id=upload_test_repo");
 		repoOpts.add(""); // don't remove this as we're using this field to determine whether we're syncing the repo or not
 		repoOpts.add(""); // ^^^
 		data.add(Arrays.asList(new Object[]{repoOpts, false}));
 
 		repoOpts = new ArrayList<String>();
 		repoOpts.add("--repo-id=large_test_repo");
 		repoOpts.add("--feed=http://download.devel.redhat.com/released/F-17/GOLD/Fedora/x86_64/os");
 		repoOpts.add(""); // ^^^
 		data.add(Arrays.asList(new Object[]{repoOpts, false}));
 
 		return data;
 	}
 
 	@DataProvider(name = "modRepoData")
 	public Object[][] modRepoData() {
 		return TestNGUtils.convertListOfListsTo2dArray(getModRepoData());
 	}
 	public List<List<Object>> getModRepoData() {
 		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
 
 		ArrayList<String> repoOpts = new ArrayList<String>();
 		repoOpts.add("--repo-id=mod_test_repo");
 		repoOpts.add("--display-name=mod_test_repo");
 		repoOpts.add("--feed=http://download.devel.redhat.com/released/F-14/GOLD/Fedora/x86_64/os");
 		data.add(Arrays.asList(new Object[]{repoOpts}));
 
 		return data;
 	}
 
 	@DataProvider(name = "chunkyUploadData")
 	public Object[][] chunkyUploadData() {
 		return TestNGUtils.convertListOfListsTo2dArray(getCampbellChunkyIttyBittyPieces());
 	}
 	public List<List<Object>> getCampbellChunkyIttyBittyPieces() {
 		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
 
 		data.add(Arrays.asList(new Object[]{"Fedora-14-x86_64-netinst.iso", "http://qeblade20.rhq.lab.eng.bos.redhat.com/pub/Fedora-14-x86_64-netinst.iso", tmpRpmDir, "20971520"}));
 
 		return data;
 	}
 
 	@DataProvider(name = "repoFilterData")
 	public Object[][] repoFilterData() {
 		return TestNGUtils.convertListOfListsTo2dArray(getRepoFilterData());
 	}
 	public List<List<Object>> getRepoFilterData() {
 		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
 
 		Hashtable<String, String> opts = new Hashtable<String, String>();
 		opts.put("--str-eq", "id=mod_test_repo");
 		opts.put("--lt", "content_unit_count=10");
 		opts.put("--lte", "content_unit_count=10");
 		opts.put("--gt", "content_unit_count=0");
 		opts.put("--gte", "content_unit_count=8");
 
 		Hashtable<String, String> expectedResults = new Hashtable<String, String>();
 		opts.put("--str-eq", "mod_test_repo");
 		opts.put("--lt", "relative_test_repo");
 		opts.put("--lte", "upload_test_repo");
 		opts.put("--gt", "relative_test_repo");
 		opts.put("--gte", "relative_test_repo");
 
 		data.add(Arrays.asList(new Object[]{opts, expectedResults}));
 
 		return data;
 	}
 
 	@DataProvider(name = "negRepoData")
 	public Object[][] negRepoData() {
 		return TestNGUtils.convertListOfListsTo2dArray(getNegRepoData());
 	}
 	public List<List<Object>> getNegRepoData() {
 		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
 
 		ArrayList<String> repoOpts = new ArrayList<String>();
 		repoOpts.add("--repo-id=bad_test_repo");
 		repoOpts.add("--display-name=bad_test_repo");
 		repoOpts.add("--feed=http://download.devel.redhat.com/released/F-14/OLD/Fedora/x86_64/os");
 		data.add(Arrays.asList(new Object[]{repoOpts}));
 
 		repoOpts = new ArrayList<String>();
 		repoOpts.add("--repo-id=$%^&*!@#~+_()");
 		repoOpts.add("--display-name=bad_test_repo");
 		repoOpts.add("--feed=http://download.devel.redhat.com/released/F-14/GOLD/Fedora/x86_64/os");
 		data.add(Arrays.asList(new Object[]{repoOpts}));
 
 		return data;
 	}
 }
