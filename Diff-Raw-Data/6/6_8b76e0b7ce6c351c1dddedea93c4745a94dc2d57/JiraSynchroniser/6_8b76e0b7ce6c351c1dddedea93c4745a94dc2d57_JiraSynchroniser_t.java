 /*******************************************************************************
  * Copyright 2012 Brendan Gordon
  * 	
  * 	This file is part of Jira2Db.
  * 	
  * 	Jira2Db is free software: you can redistribute it and/or modify
  * 	it under the terms of the GNU General Public License as published by
  * 	the Free Software Foundation, either version 3 of the License, or
  * 	(at your option) any later version.
  * 	
  * 	Jira2Db is distributed in the hope that it will be useful,
  * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
  * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * 	GNU General Public License for more details.
  * 	
  * 	You should have received a copy of the GNU General Public License
  * 	along with Jira2Db.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package com.gordcorp.jira2db;
 
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.gordcorp.jira2db.jira.Jira;
 import com.gordcorp.jira2db.persistence.JiraCustomFieldDao;
 import com.gordcorp.jira2db.persistence.JiraIssueDao;
 import com.gordcorp.jira2db.persistence.SqlSessionFactorySingleton;
 import com.gordcorp.jira2db.persistence.dto.JiraCustomFieldDto;
 import com.gordcorp.jira2db.persistence.dto.JiraIssueDto;
 import com.gordcorp.jira2db.util.JiraCustomFieldDtoIdComparator;
 import com.gordcorp.jira2db.util.PropertiesWrapper;
 
 public class JiraSynchroniser {
 
 	protected final static Logger log = LoggerFactory
 			.getLogger(JiraSynchroniser.class);
 
 	List<String> projectNames = null;
 
 	boolean includeDeletions = false;
 
 	Date lastSyncDate = null;
 
 	JiraIssueDao jiraIssueDao = null;
 
 	JiraCustomFieldDao jiraCustomFieldDao = null;
 
 	public JiraSynchroniser() {
 
 		this.jiraIssueDao = new JiraIssueDao(JiraIssueDto.class,
 				SqlSessionFactorySingleton.instance());
 
 		this.jiraCustomFieldDao = new JiraCustomFieldDao(
 				JiraCustomFieldDto.class, SqlSessionFactorySingleton.instance());
 
 		this.includeDeletions = PropertiesWrapper.get("jira.sync-deletions")
 				.equals("1");
 	}
 
 	/**
 	 * Limits the issues returned from Jira to these projects when it is
 	 * searched. An exception is thrown if a given project name is not found in
 	 * Jira.
 	 * 
 	 * @param projectsNames
 	 */
 	public void setProjects(List<String> projectsNames) {
 
 		List<String> allProjects = Jira.getAllProjects();
 		for (String project : projectsNames) {
 			if (!allProjects.contains(project)) {
 				throw new RuntimeException("Project not found in Jira: "
 						+ project);
 			}
 		}
 
 		this.projectNames = projectsNames;
 	}
 
 	/**
 	 * If this issue's Custom Fields already exists in the database, it is
 	 * updated if not equal. Otherwise they are created.
 	 * 
 	 * @param jiraCustomFieldDtos
 	 *            Dtos from Jira
 	 * @param readJiraCustomFieldDtos
 	 *            Dtos from the Database
 	 */
 	protected void updateOrCreateCustomFields(
 			List<JiraCustomFieldDto> jiraCustomFieldDtos,
 			List<JiraCustomFieldDto> readJiraCustomFieldDtos) {
 
 		if (jiraCustomFieldDtos.size() != readJiraCustomFieldDtos.size()) {
 			if (readJiraCustomFieldDtos.size() > 0) {
 				log.info("Number of custom fields has changed, deleting all from DB and reinserting");
 				jiraCustomFieldDao.deleteAllByJiraKey(jiraCustomFieldDtos
 						.get(0).getJiraKey());
 			}
 
 			for (JiraCustomFieldDto dto : jiraCustomFieldDtos) {
 				int rows = jiraCustomFieldDao.create(dto);
 				if (rows != 1) {
 					throw new RuntimeException(
 							"Problem inserting custom field " + dto);
 				}
 			}
 		} else {
 			Collections.sort(readJiraCustomFieldDtos,
 					new JiraCustomFieldDtoIdComparator());
 			Collections.sort(jiraCustomFieldDtos,
 					new JiraCustomFieldDtoIdComparator());
 
 			for (int i = 0; i < jiraCustomFieldDtos.size(); i++) {
 				JiraCustomFieldDto dtoFromJira = jiraCustomFieldDtos.get(i);
 				JiraCustomFieldDto dtoFromDb = readJiraCustomFieldDtos.get(i);
 				if (!dtoFromJira.equals(dtoFromDb)) {
 					log.info("Updating field in Db: " + dtoFromJira);
 					int rows = jiraCustomFieldDao.update(dtoFromJira);
 					if (rows != 1) {
 						throw new RuntimeException(
 								"Problem updating custom field " + dtoFromJira);
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * If this issue already exists in the database, it is updated. Otherwise it
 	 * is created.
 	 * 
 	 * @param jiraIssueDto
 	 */
 	protected void updateOrCreateIssue(JiraIssueDto jiraIssueDto) {
 		log.info("Checking if issue already exists: "
 				+ jiraIssueDto.getJiraKey());
 		JiraIssueDto readJiraIssueDto = jiraIssueDao.getByJiraKey(jiraIssueDto
 				.getJiraKey());
		if (readJiraIssueDto != null) {
			readJiraIssueDto.setCustomFields(jiraCustomFieldDao
					.getAllByJiraKey(jiraIssueDto.getJiraKey()));
		}
 
 		// todo use transaction
 
 		if (readJiraIssueDto == null) {
 			log.info("Creating " + jiraIssueDto.getJiraKey());
 			int rows = jiraIssueDao.create(jiraIssueDto);
 			log.info("Create returned " + rows);
 			if (rows != 1) {
 				throw new RuntimeException("Problem inserting " + jiraIssueDto);
 			}
 			for (JiraCustomFieldDto dto : jiraIssueDto.getCustomFields()) {
 				rows = jiraCustomFieldDao.create(dto);
 				if (rows != 1) {
 					throw new RuntimeException(
 							"Problem inserting custom field " + dto);
 				}
 			}
 
 		} else {
 
 			readJiraIssueDto.setCustomFields(jiraCustomFieldDao
 					.getAllByJiraKey(jiraIssueDto.getJiraKey()));
 
 			if (readJiraIssueDto.equals(jiraIssueDto)) {
 				log.info("Updated issue from Jira matches the database, so skipping update");
 			} else {
 				log.info("Updating " + jiraIssueDto.getJiraKey());
 
 				int rows = jiraIssueDao.updateByJiraKey(jiraIssueDto);
 				log.info("Update returned " + rows);
 				if (rows != 1) {
 					throw new RuntimeException("Problem updating "
 							+ jiraIssueDto);
 				}
 
 				updateOrCreateCustomFields(jiraIssueDto.getCustomFields(),
 						readJiraIssueDto.getCustomFields());
 
 			}
 		}
 	}
 
 	/**
 	 * Get list of issues from Jira and the DB. Delete those that are not in
 	 * Jira but are in the DB.
 	 */
 	protected void syncDeletions(String projectName,
 			List<JiraIssueDto> issuesInJira) {
 
 		// todo limit getAll by projectName
 
 		List<JiraIssueDto> issuesInDb = jiraIssueDao.getAll();
 		for (JiraIssueDto issueInDb : issuesInDb) {
 			if (issueInDb.getProject().equalsIgnoreCase(projectName)
 					&& !issuesInJira.contains(issueInDb)) {
 				log.info("Found issue in DB that was not in Jira. Deleting from DB: "
 						+ issueInDb);
 
 				int rows = jiraIssueDao.deleteByJiraKey(issueInDb.getJiraKey());
 				log.info("Delete returned " + rows);
 				if (rows != 1) {
 					throw new RuntimeException("Problem deleting " + issueInDb);
 				}
 
 				log.info("Deleting custom fields " + issueInDb.getJiraKey());
 				jiraCustomFieldDao.deleteAllByJiraKey(issueInDb.getJiraKey());
 
 			}
 		}
 	}
 
 	/**
 	 * Sync deletions for all currently selected projects
 	 */
 	protected void syncDeletions() {
 		log.info("Syncing deletions for projects " + projectNames);
 		for (String projectName : projectNames) {
 
 			List<JiraIssueDto> dtos = Jira.getAllIssuesInProject(projectName);
 			syncDeletions(projectName, dtos);
 		}
 
 	}
 
 	/**
 	 * Sync issues updated since the last sync occurred.
 	 */
 	protected void syncIssuesUpdatedSinceLastSync() {
 
 		int minutesSinceLastSync = minutesSinceLastSync();
 
 		// Ensure the minutes to search Jira is at least 1 minute.
 		minutesSinceLastSync = Math.max(1, minutesSinceLastSync);
 
 		lastSyncDate = Calendar.getInstance().getTime();
 		for (String projectName : projectNames) {
 			List<JiraIssueDto> dtos = Jira.getIssuesUpdatedWithin(projectName,
 					minutesSinceLastSync);
 			for (JiraIssueDto jiraIssueDto : dtos) {
 				updateOrCreateIssue(jiraIssueDto);
 			}
 		}
 	}
 
 	/**
 	 * Calculates the number of minutes elapsed since the last sync
 	 * 
 	 * @return the number of minutes elapsed since the last sync
 	 */
 	protected int minutesSinceLastSync() {
 		if (lastSyncDate == null) {
 			return Integer.MAX_VALUE;
 		}
 
 		Date now = Calendar.getInstance().getTime();
 
 		return (int) ((now.getTime() / 60000) - (lastSyncDate.getTime() / 60000));
 
 	}
 
 	/**
 	 * Sync all issues from Jira, regardless of when it was updated
 	 */
 	public void syncAll() {
 		if (projectNames == null) {
 			projectNames = Jira.getAllProjects();
 		}
 
 		log.info("Syncing projects: " + projectNames);
 		lastSyncDate = Calendar.getInstance().getTime();
 		for (String projectName : projectNames) {
 			log.info("Syncing project " + projectName);
 			List<JiraIssueDto> dtos = Jira.getAllIssuesInProject(projectName);
 
 			log.info("Number of issues found: " + dtos.size());
 			for (JiraIssueDto jiraIssueDto : dtos) {
 				updateOrCreateIssue(jiraIssueDto);
 			}
 			if (includeDeletions) {
 				syncDeletions(projectName, dtos);
 			}
 			log.info("Finished syncing project " + projectName);
 		}
 	}
 
 	/**
 	 * Never returns. Sync and keep syncing in an infinite loop. Jira is polled
 	 * at a rate as given in the properties.
 	 */
 	public void syncForever() {
 
 		int pollRateInMillis = 1000 * Integer.parseInt(PropertiesWrapper
 				.get("jira.pollrate.seconds"));
 		if (pollRateInMillis <= 0) {
 			throw new RuntimeException(
 					"jira.pollrate.seconds must be greater than 0");
 		}
 
 		log.info("Jira will be polled every " + pollRateInMillis / 1000
 				+ " seconds");
 
 		if (projectNames == null) {
 			projectNames = Jira.getAllProjects();
 		}
 
 		boolean continueOnException = (PropertiesWrapper
 				.get("continue-on-exception").equalsIgnoreCase("1"));
 
 		try {
 
 			// If continueOnException is true, we keep trying until the sync
 			// finishes without error
 			while (true) {
 				try {
 					syncAll();
 					break;
 				} catch (Exception e) {
 					if (continueOnException) {
 						log.error(
 								"Problem syncing, continuing: "
 										+ e.getMessage(), e);
 					} else {
 						throw new RuntimeException(e);
 					}
 				}
 				Thread.sleep(pollRateInMillis);
 			}
 
 			log.info("Forever syncing projects: " + projectNames);
 
 			while (true) {
 				try {
 					syncIssuesUpdatedSinceLastSync();
 					if (includeDeletions) {
 						syncDeletions();
 					}
 				} catch (Exception e) {
 					if (continueOnException) {
 						log.error(
 								"Problem syncing, continuing: "
 										+ e.getMessage(), e);
 					} else {
 						throw new RuntimeException(e);
 					}
 				}
 
 				Thread.sleep(pollRateInMillis);
 			}
 		} catch (InterruptedException e) {
 			log.info("Stopped infinte loop syncing");
 		}
 	}
 
 }
