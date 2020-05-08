 package org.zkoss.todoZK.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import org.zkoss.todoZK.vo.Milestone;
 import org.zkoss.todoZK.vo.Task;
 import org.zkoss.todoZK.vo.Workspace;
 
 class MockDB extends AbstractDB {
 	//simulate table in Database
 	private static List<Workspace> workspaces = new ArrayList<Workspace>();
 	private static List<Milestone> milestones = new ArrayList<Milestone>();
 	private static List<Task> tasks = new ArrayList<Task>();
 	//
 	
 	private static Long longId = 0L;
 	private static final Random random = new Random();
 	private static final Date standardDate = new Date();
 
 	public MockDB() {
 		genMockData();
 	}
 
 	@Override
 	public List<Workspace> getWorkspaces() {
 		return workspaces;
 	}
 		
 	@Override
 	public Workspace getWorkspaceById(Long id) {
 		for (Workspace ws : workspaces) {
 			if (ws.getClass().equals(id)) {
 				return ws;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public List<Milestone> getMilestonesByWorkspace(Long workspaceId) {
 		ArrayList<Milestone> result = new ArrayList<Milestone>();
 		for (Milestone ms : milestones) {
			if (ms.getWorkspaceId().equals(workspaceId)) {
 				result.add(ms);
 			}
 		}
 		//TODO sort result
 		return result;
 	}
 
 	@Override
 	public List<Task> getTasksByMilestone(Long milestoneId) {
 		ArrayList<Task> result = new ArrayList<Task>();
 		for (Task task : tasks) {
			if (task.getMilestoneId().equals(milestoneId)) {
 				result.add(task);
 			}
 		}
 		//TODO sort result
 		return null;
 	}
 	
 	@Override
 	public void addWorkspace(Workspace ws) {
 		workspaces.add(ws);
 	}
 
 	@Override
 	public void addMilestone(Milestone ms) {
 		//XXX check workspace exist
 		milestones.add(ms);
 	}
 
 	@Override
 	public void addTask(Task task) {
 		//XXX check milestone exist
 		tasks.add(task);
 	}
 
 	////////////////////////////////////////////////////////////////////
 	private void genMockData() {
 		Workspace ws1 = new Workspace();
 		ws1.setId(genNextLong());
 		ws1.setTitle("ZK Workspace");
 		addWorkspace(ws1);
 		genMilestone(ws1, random.nextInt(3)+2);
 
 		Workspace ws2 = new Workspace();
 		ws2.setId(genNextLong());
 		ws2.setTitle("Personal");
 		addWorkspace(ws2);
 		genMilestone(ws2, random.nextInt(3)+2);
 	}
 
 	private void genMilestone(Workspace ws, int number) {
 		for (int i=0; i<number; i++) {
 			Milestone ms = new Milestone();
 			ms.setId(genNextLong());
 			ms.setTitle("Milestone "+ms.getId());
 			ms.setWorkspaceId(ws.getId());
 			addMilestone(ms);
 			genTask(ms, random.nextInt(8)+1);
 		}
 	}
 	
 	private void genTask(Milestone ms, int number) {
 		for (int i=0; i<number; i++) {
 			Task task = new Task();
 			task.setId(genNextLong());
 			task.setTitle("Task "+task.getId());
 			task.setPriority(random.nextInt(5));
 			task.setCreateDate(genDate(true));
 			task.setDueDate(genDate(false));
 			task.setMilestoneId(ms.getId());
 			addTask(task);
 		}
 	}
 
 	private Date genDate(boolean before) {
 		long diff = random.nextInt(10) * 86400L
 				+ random.nextInt(24) * 3600L 
 				+ random.nextInt(60) * 60L
 				+ random.nextInt(60) * 1L;
 		if (before) {
 			diff *= -1L;
 		}
 		return new Date(standardDate.getTime() + diff * 1000);
 	}
 
 	private static synchronized Long genNextLong() {
 		longId++;
 		return longId;
 	}
 }
