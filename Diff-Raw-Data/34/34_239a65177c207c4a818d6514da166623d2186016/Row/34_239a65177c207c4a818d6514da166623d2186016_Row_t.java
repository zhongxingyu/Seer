 package com.attask.descriptiondashboard;
 
 import hudson.Util;
 import hudson.model.Project;
 import hudson.model.Run;
 import org.kohsuke.stapler.export.Exported;
 import org.kohsuke.stapler.export.ExportedBean;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * User: Joel Johnson
  * Date: 9/7/12
  * Time: 8:22 PM
  */
 @ExportedBean
 public class Row implements Serializable {
 	private final String id;
 	private final String description;
 	private final Map<String, Cell> cells;
 	private final transient List<Header> headers;
 	private final transient CustomColumn customColumn;
 	private final int totalFailures;
	private final boolean criticalRequired;
 	private final boolean criticalAll;
 	private final boolean running;
 
 	public Row(String id, String description, Map<String, Cell> cells, List<Header> headers, CustomColumn customColumn) {
 		if(id == null) {
 			throw new IllegalArgumentException("id cannot be null");
 		}
 		if(description == null) {
 			throw new IllegalArgumentException("description cannot be null");
 		}
 		if (cells == null || cells.isEmpty()) {
 			throw new IllegalArgumentException("cells cannot be null or empty");
 		}
 		if (headers == null || headers.isEmpty()) {
 			throw new IllegalArgumentException("headers cannot be null or empty");
 		}
 
 		this.id = id;
 		this.description = description;
 		this.cells = cells;
 		this.headers = headers;
 		this.customColumn = customColumn;
 
 		boolean running = false;
 		int totalFailures = 0;
		boolean criticalRequired = false;
 		boolean criticalAll = false;
 		for (Header header : headers) {
 			Cell cell = cells.get(header.getName());
 			if(cell != null) {
 				int failures = cell.getFailures();
 				totalFailures += failures > 0 ? failures : 0;
 				if(cell.getRunning()) {
 					running = true;
 				} else {
 					if(!"SUCCESS".equals(cell.getResult()) && !"UNSTABLE".equals(cell.getResult())) {
						if(header.getRequirement() == Header.Requirement.Required) {
							criticalRequired = true;
 						}
 						criticalAll = true;
 					}
 				}
 			} else {
				if(header.getRequirement() != Header.Requirement.NotRequired) {
					criticalRequired = true;
 				}
 				criticalAll = true;
 			}
 		}
		this.criticalRequired = !running && criticalRequired;
 		this.criticalAll = !running && criticalAll;
 		this.running = running;
 		this.totalFailures = totalFailures;
 	}
 
 	@Exported
 	public String getId() {
 		return id;
 	}
 
 	@Exported
 	public String getDescription() {
 		return description;
 	}
 
 	@Exported
 	public Map<String, Cell> getCells() {
 		return cells;
 	}
 
 	@Exported
 	public int getTotalFailures() {
 		return totalFailures;
 	}
 
 	@Exported
	public boolean getCriticalRequired() {
		return criticalRequired;
 	}
 
 	@Exported
 	public boolean getCriticalAll() {
 		return criticalAll;
 	}
 
 	@Exported
 	public boolean getRunning() {
 		return running;
 	}
 
 	public CustomColumn getCustomColumn() {
 		return customColumn;
 	}
 
 	public Cell getCell(String name) {
 		if (cells.containsKey(name)) {
 			return cells.get(name);
 		}
 		return null;
 	}
 
 	public Date findDate() {
 		for (Header header : headers) {
 			Cell cell = cells.get(header.getName());
 			if (cell != null) {
 				return cell.getDate();
 			}
 		}
 		return new Date(0);
 	}
 
 	public String findDateFormatted() {
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm z", Locale.US);
 		return simpleDateFormat.format(findDate());
 	}
 
 	public String findDatePretty() {
 		long duration = new GregorianCalendar().getTimeInMillis()-findDate().getTime();
 		return Util.getPastTimeString(duration);
 	}
 
 	public List<Header> getHeaders() {
 		return headers;
 	}
 
 	public boolean getPassed() {
 		for (Header header : headers) {
 			Cell cell = cells.get(header.getName());
 			if(cell == null) {
 				if(header.getRequirement() != Header.Requirement.NotRequired && header.getRequirement() != Header.Requirement.RequiredWhenPresent) {
 					return false;
 				}
 			} else {
 				String result = cell.getResult();
				if(!cell.getRunning()  && "SUCCESS".equals(result)) {
 					continue;
 				}
 
 				if(header.getRequirement() != Header.Requirement.NotRequired) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public int findNumberCommitters() {
 		Iterator<Header> iterator = headers.iterator();
		while(iterator.hasNext()) {
 			Cell cell = cells.get(iterator.next().getName());
			if(cell != null) {
				return cell.getNumberCommitters();
			}
 		}
 		return 0;
 	}
 
 	/**
 	 * Used from Row/render.jelly. Added to the class so we can style rows based on committer.
 	 * @return HTML-safe set of the names and IDs of everyone who contributed to all the builds in the entire row.
 	 */
 	public String findCommitters() {
 		Set<String> result = new HashSet<String>();
 		for (Cell cell : cells.values()) {
 			for (SimpleUser committer : cell.getCommitters()) {
 				String cleanCommitterId = cleanString(committer.getId());
 				result.add(cleanCommitterId);
 				String cleanCommitterName = cleanString(committer.getName());
 				result.add(cleanCommitterName);
 			}
 		}
 		return Util.join(result, " ");
 	}
 
 	private String cleanString(String cleanCommitter) {
 		cleanCommitter = cleanCommitter.replaceAll("\\s+", ""); //no spaces in the class name
 		cleanCommitter = Util.escape(cleanCommitter); //no XSS attacks, please
 		return cleanCommitter;
 	}
 
 	public List<Change> findChangeSet() {
 		List<Change> allChanges = new LinkedList<Change>();
 		for (Cell cell : cells.values()) {
 			String projectName = cell.getProjectName();
 			int buildNumber = cell.getBuildNumber();
 			Project project = ProjectUtils.findProject(projectName);
 			if(project != null) {
 				Run buildByNumber = project.getBuildByNumber(buildNumber);
 				if(buildByNumber != null) {
 					List<Change> changeSet = ProjectUtils.findChangeSet(buildByNumber);
 					allChanges.addAll(changeSet);
 				}
 			}
 		}
 		return Collections.unmodifiableList(allChanges);
 	}
 
 	public boolean idEquals(String[] ids) {
 		if(ids != null) {
 			for (String id : ids) {
 				if(this.id != null && ("row-" + this.id).equals(id)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public int findNumberVisibleColumns() {
 		int total = 0;
 		for (Header header : headers) {
 			if(header.getVisible()) {
 				total++;
 			}
 		}
 		return total;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if(o == null || !(o instanceof Row)) {
 			return false;
 		}
 		Row row = (Row) o;
 		return this.getId().equals(row.getId());
 	}
 
 	@Override
 	public int hashCode() {
 		return this.getId() != null ? this.getId().hashCode() : 0;
 	}
 }
