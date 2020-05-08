 package com.baoxue.action;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.exception.ConstraintViolationException;
 
 import com.baoxue.common.ActionBase;
 import com.baoxue.common.ApkInfo;
 import com.baoxue.common.Helper;
 import com.baoxue.db.TPackageUpdate;
 import com.baoxue.db.TTask;
 import com.baoxue.db.TTaskItem;
 
 public class Task extends ActionBase {
 
 	public final static String CMD_UPDATE_PACKAGE = "updatePackage";
 	public final static String CMD_DELETE_PACKAGE = "deletePackage";
 	public final static String CMD_LINK = "link";
 	public final static String CMD_SHELL = "shell";
 	private String[] cmdS = new String[] { CMD_UPDATE_PACKAGE,
 			CMD_DELETE_PACKAGE, CMD_LINK, CMD_SHELL };
 	private String command;
 	private boolean showMsg;
 	private String msgTitle;
 	private List<String> msgItem = new ArrayList<String>();
 	private String taskID;
 	private String itemID;
 	private String taskName;
 	private String versionRegex;
 	private String deviceId;
 	private boolean waitResult;
 
 	private boolean addUpdateView;
 	private boolean editUpdateView;
 	private boolean addDeleteView;
 	private boolean editDeleteView;
 	private boolean editTaskView;
 	private boolean addLinkView;
 	private boolean editLinkView;
 
 	private boolean addShellView;
 	private boolean editShellView;
 
 	private boolean forcesUpdate;
 
 	private File file;
 	private String fileContentType;
 	private String fileFileName;
 	private boolean publish;
 	private String packageName;
 	private List<TTaskItem> taskItems;
 	private List<TTask> tasks;
 	private String linkMessage;
 	private String linkURL;
 	private boolean linkBackground;
 	private boolean linkAutoOpen;
 
 	private String shell;
 
 	public String getDeviceId() {
 		return deviceId;
 	}
 
 	public void setDeviceId(String deviceId) {
 		this.deviceId = deviceId;
 	}
 
 	public boolean isWaitResult() {
 		return waitResult;
 	}
 
 	public void setWaitResult(boolean waitResult) {
 		this.waitResult = waitResult;
 	}
 
 	public String getShell() {
 		return shell;
 	}
 
 	public void setShell(String shell) {
 		this.shell = shell;
 	}
 
 	public boolean isAddShellView() {
 		return addShellView;
 	}
 
 	public void setAddShellView(boolean addShellView) {
 		this.addShellView = addShellView;
 	}
 
 	public boolean isEditShellView() {
 		return editShellView;
 	}
 
 	public void setEditShellView(boolean editShellView) {
 		this.editShellView = editShellView;
 	}
 
 	public String getLinkMessage() {
 		return linkMessage;
 	}
 
 	public void setLinkMessage(String linkMessage) {
 		this.linkMessage = linkMessage;
 	}
 
 	public String getLinkURL() {
 		return linkURL;
 	}
 
 	public void setLinkURL(String linkURL) {
 		this.linkURL = linkURL;
 	}
 
 	public boolean isLinkBackground() {
 		return linkBackground;
 	}
 
 	public void setLinkBackground(boolean linkBackground) {
 		this.linkBackground = linkBackground;
 	}
 
 	public boolean isLinkAutoOpen() {
 		return linkAutoOpen;
 	}
 
 	public void setLinkAutoOpen(boolean linkAutoOpen) {
 		this.linkAutoOpen = linkAutoOpen;
 	}
 
 	public boolean isEditTaskView() {
 		return editTaskView;
 	}
 
 	public void setEditTaskView(boolean editTaskView) {
 		this.editTaskView = editTaskView;
 	}
 
 	public List<TTask> getTasks() {
 		return tasks;
 	}
 
 	public void setTasks(List<TTask> tasks) {
 		this.tasks = tasks;
 	}
 
 	public String getPackageName() {
 		return packageName;
 	}
 
 	public void setPackageName(String packageName) {
 		this.packageName = packageName;
 	}
 
 	public String getCommand() {
 		return command;
 	}
 
 	public void setCommand(String command) {
 		this.command = command;
 	}
 
 	public String[] getCmdS() {
 		return cmdS;
 	}
 
 	public void setCmdS(String[] cmdS) {
 		this.cmdS = cmdS;
 	}
 
 	public List<TTaskItem> getTaskItems() {
 		return taskItems;
 	}
 
 	public void setTaskItems(List<TTaskItem> taskItems) {
 		this.taskItems = taskItems;
 	}
 
 	public String getVersionRegex() {
 		return versionRegex;
 	}
 
 	public void setVersionRegex(String versionRegex) {
 		this.versionRegex = versionRegex;
 	}
 
 	public String getTaskName() {
 		return taskName;
 	}
 
 	public void setTaskName(String taskName) {
 		this.taskName = taskName;
 	}
 
 	public boolean isShowMsg() {
 		return showMsg;
 	}
 
 	public void setShowMsg(boolean showMsg) {
 		this.showMsg = showMsg;
 	}
 
 	public String getMsgTitle() {
 		return msgTitle;
 	}
 
 	public void setMsgTitle(String msgTitle) {
 		this.msgTitle = msgTitle;
 	}
 
 	public List<String> getMsgItem() {
 		return msgItem;
 	}
 
 	public void setMsgItem(List<String> msgItem) {
 		this.msgItem = msgItem;
 	}
 
 	public String getTaskID() {
 		return taskID;
 	}
 
 	public void setTaskID(String taskID) {
 		this.taskID = taskID;
 	}
 
 	public String getItemID() {
 		return itemID;
 	}
 
 	public void setItemID(String itemID) {
 		this.itemID = itemID;
 	}
 
 	public boolean isAddUpdateView() {
 		return addUpdateView;
 	}
 
 	public void setAddUpdateView(boolean addUpdateView) {
 		this.addUpdateView = addUpdateView;
 	}
 
 	public boolean isEditUpdateView() {
 		return editUpdateView;
 	}
 
 	public void setEditUpdateView(boolean editUpdateView) {
 		this.editUpdateView = editUpdateView;
 	}
 
 	public boolean isAddDeleteView() {
 		return addDeleteView;
 	}
 
 	public void setAddDeleteView(boolean addDeleteView) {
 		this.addDeleteView = addDeleteView;
 	}
 
 	public boolean isEditDeleteView() {
 		return editDeleteView;
 	}
 
 	public void setEditDeleteView(boolean editDeleteView) {
 		this.editDeleteView = editDeleteView;
 	}
 
 	public boolean isForcesUpdate() {
 		return forcesUpdate;
 	}
 
 	public void setForcesUpdate(boolean forcesUpdate) {
 		this.forcesUpdate = forcesUpdate;
 	}
 
 	public File getFile() {
 		return file;
 	}
 
 	public void setFile(File file) {
 		this.file = file;
 	}
 
 	public String getFileContentType() {
 		return fileContentType;
 	}
 
 	public void setFileContentType(String fileContentType) {
 		this.fileContentType = fileContentType;
 	}
 
 	public String getFileFileName() {
 		return fileFileName;
 	}
 
 	public void setFileFileName(String fileFileName) {
 		this.fileFileName = fileFileName;
 	}
 
 	public boolean isPublish() {
 		return publish;
 	}
 
 	public void setPublish(boolean publish) {
 		this.publish = publish;
 	}
 
 	public boolean isShowAddView() {
 		return !addUpdateView && !editUpdateView && !addDeleteView
 				&& !editDeleteView && !addLinkView && !editLinkView
 				&& !addShellView && !editShellView;
 	}
 
 	public boolean isAddLinkView() {
 		return addLinkView;
 	}
 
 	public void setAddLinkView(boolean addLinkView) {
 		this.addLinkView = addLinkView;
 	}
 
 	public boolean isEditLinkView() {
 		return editLinkView;
 	}
 
 	public void setEditLinkView(boolean editLinkView) {
 		this.editLinkView = editLinkView;
 	}
 
 	@Override
 	public String execute() throws Exception {
 		queryTask();
 		taskID = null;
 		publish = false;
 		return "task";
 	}
 
 	public String taskEditEnd() {
 
 		Session session = getDBSession();
 		try {
 			setTaskEditState(taskID, false, session);
 			taskID = null;
 			publish = false;
 		} finally {
 			session.close();
 		}
 		queryTask();
 		return "task";
 	}
 
 	public String taskAdd() throws Exception {
 
 		TTask task = new TTask();
 		taskID = UUID.randomUUID().toString();
 		task.setCId(taskID);
 		task.setCName(taskName);
 		task.setCPublish(publish);
 		task.setCCreateTime(new Date());
 		task.setCDelete(false);
 		task.setCEdit(true);
 		if (deviceId != null && !"".equals(deviceId)) {
 			task.setCTaskDeviceId(deviceId);
 		}
 		task.setCWaiteResult(waitResult);
 		task.setCVersionRegex(versionRegex);
 		Session session = getDBSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			session.save(task);
 			tx.commit();
 
 		} catch (ConstraintViolationException ex) {
 			tx.rollback();
 			showMsg = true;
 			msgTitle = "添加失败";
 			msgItem.add("名称重复");
 			return "task";
 		} catch (Exception ex) {
 			tx.rollback();
 			showMsg = true;
 			msgTitle = "添加失败";
 			msgItem.add(ex.getMessage());
 			return "task";
 		} finally {
 			session.close();
 		}
 
 		queryItem();
 		return "item";
 
 	}
 
 	public String taskEditItem() {
 		Session session = getDBSession();
 		try {
 			setTaskEditState(taskID, true, session);
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	private boolean setTaskEditState(String id, boolean edit, Session session) {
 		Transaction tx = session.beginTransaction();
 		try {
 			String hql = "from TTask t where t.CId=:id";
 			Query query = session.createQuery(hql);
 			query.setString("id", taskID);
 			List<TTask> tasks = query.list();
 			if (tasks != null && tasks.size() > 0) {
 
 				TTask task = tasks.get(0);
 				task.setCEdit(edit);
 				session.update(task);
 				tx.commit();
 				return true;
 			}
 		} catch (Exception ex) {
 			tx.rollback();
 
 		}
 		return false;
 
 	}
 
 	private void deleteItem(Session session, String id) throws Exception {
 		TTaskItem item = queryItemById(id, session);
 		if (item != null) {
 			if (CMD_UPDATE_PACKAGE.equals(item.getCCommand())) {
 				String hql = "from TPackageUpdate p where p.CId=:id";
 				Query query = session.createQuery(hql);
 				query.setParameter("id", item.getCP1());
 				@SuppressWarnings("unchecked")
 				List<TPackageUpdate> res = query.list();
 				if (res != null && res.size() > 0) {
 					String fileName = res.get(0).getCFileName();
 					String fullName = getRealPath("/apk/" + fileName);
 					File file = new File(fullName);
 					if (file.delete()) {
 						System.out.println("success delete file");
 					}
 					session.delete(res.get(0));
 				}
 
 			}
 			session.delete(item);
 		}
 	}
 
 	public String deleteItem() throws Exception {
 
 		Session session = getDBSession();
 		Transaction tx = session.beginTransaction();
 		showMsg = true;
 		try {
 			deleteItem(session, itemID);
 			tx.commit();
 			setMsgTitle("删除成功");
 		} catch (Exception ex) {
 			tx.rollback();
 			setShowMsg(true);
 			setMsgTitle("删除失败");
 			msgItem.clear();
 			msgItem.add(ex.getMessage());
 		} finally {
 			session.close();
 		}
 
 		queryItem();
 		return "item";
 	}
 
 	public String editItem() throws Exception {
 		Session session = getDBSession();
 		try {
 			TTaskItem item = queryItemById(itemID, session);
 			if (item != null) {
 				if (CMD_UPDATE_PACKAGE.equals(item.getCCommand())) {
 					editUpdateView = true;
 
 					String hql = "from TPackageUpdate p where p.CId=:id";
 					Query query = session.createQuery(hql);
 					query.setParameter("id", item.getCP1());
 					@SuppressWarnings("unchecked")
 					List<TPackageUpdate> res = query.list();
 					if (res != null && res.size() > 0) {
 						forcesUpdate = res.get(0).isCForcesUpdate();
 					}
 				} else if (CMD_DELETE_PACKAGE.equals(item.getCCommand())) {
 					editDeleteView = true;
 					packageName = item.getCP1();
 				} else if (CMD_LINK.equals(item.getCCommand())) {
 					editLinkView = true;
 					linkMessage = item.getCP1();
 					linkURL = item.getCP2();
 					if ("1".equals(item.getCP3())) {
 						linkBackground = true;
 					} else {
 						linkBackground = false;
 					}
 					if ("1".equals(item.getCP4())) {
 						linkAutoOpen = true;
 					} else {
 						linkAutoOpen = false;
 					}
 
 				} else if (CMD_SHELL.equals(item.getCCommand())) {
 
 					editShellView = true;
 					shell = item.getCP1();
 				}
 
 			}
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String addItem() throws Exception {
 
 		if (CMD_UPDATE_PACKAGE.equals(command)) {
 			addUpdateView = true;
 		} else if (CMD_DELETE_PACKAGE.equals(command)) {
 			addDeleteView = true;
 		} else if (CMD_LINK.equals(command)) {
 			addLinkView = true;
 		} else if (CMD_SHELL.equals(command)) {
 			addShellView = true;
 		}
 		itemID = null;
 		queryItem();
 		return "item";
 	}
 
 	public String UpdateAdd() throws Exception {
 		if (isPost()) {
 			if (file == null) {
 				setShowMsg(true);
 				setMsgTitle("文件不能为空");
 				getMsgItem().clear();
 				addUpdateView = true;
 			} else {
 				String fileName = UUID.randomUUID().toString() + ".apk";
 				String fullName = getRealPath("/apk/" + fileName);
 				File desFile = new File(fullName);
 
 				if (Helper.Copy(file, desFile)) {
 					ApkInfo info = Helper.getApkInfo(desFile.getAbsolutePath());
 					if (info != null && info.enable()) {
 						Session session = getDBSession();
 						Transaction tx = session.beginTransaction();
 						try {
 
 							TPackageUpdate pu = new TPackageUpdate();
 							pu.setCId(UUID.randomUUID().toString());
 							pu.setCFileName(fileName);
 							pu.setCForcesUpdate(forcesUpdate);
 
 							pu.setCPackageName(info.getPackageName());
 							pu.setCUploadTime(new Date());
 							pu.setCVersionCode(info.getVersionCode());
 							pu.setCVersionName(info.getVersionName());
 
 							session.save(pu);
 
 							TTaskItem taskitem = new TTaskItem();
 							itemID = UUID.randomUUID().toString();
 							taskitem.setCId(itemID);
 							taskitem.setCCommand(CMD_UPDATE_PACKAGE);
 							taskitem.setCEnable(true);
 							taskitem.setCIndex(getMaxIndex(session) + 1);
 							String des = "更新包：" + pu.getCPackageName() + ",";
 							if (forcesUpdate) {
 								des += "强制更新.";
 							} else {
 								des += "非强制更新.";
 							}
 							taskitem.setCDescription(des);
 							taskitem.setCTaskId(taskID);
 							taskitem.setCP1(pu.getCId());
 							session.save(taskitem);
 							tx.commit();
 							setShowMsg(true);
 							setMsgTitle("添加成功");
 							msgItem.add("文件大小:"
 									+ Helper.formatLength(file.length()));
 							msgItem.add("packageName:" + info.getPackageName());
 							msgItem.add("versionCode:" + info.getVersionCode());
 							msgItem.add("versionName:" + info.getVersionName());
 
 						} catch (Exception ex) {
 							tx.rollback();
 							setShowMsg(true);
 							setMsgTitle("添加失败");
 							msgItem.clear();
 							msgItem.add(ex.getMessage());
 						} finally {
 							session.close();
 						}
 
 					} else {
 						desFile.deleteOnExit();
 						setShowMsg(true);
 						setMsgTitle("文件格式错误");
 						getMsgItem().clear();
 						addUpdateView = true;
 					}
 				} else {
 					setShowMsg(true);
 					setMsgTitle("上传失败");
 					getMsgItem().clear();
 					addUpdateView = true;
 				}
 			}
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String UpdateEdit() throws Exception {
 		Session session = getDBSession();
 		try {
 			Transaction tx = session.beginTransaction();
 			TTaskItem item = queryItemById(itemID, session);
 			showMsg = true;
 			if (item != null) {
 				try {
 					if (CMD_UPDATE_PACKAGE.equals(item.getCCommand())) {
 						String hql = "from TPackageUpdate p where p.CId=:id";
 						Query query = session.createQuery(hql);
 						query.setParameter("id", item.getCP1());
 						@SuppressWarnings("unchecked")
 						List<TPackageUpdate> res = query.list();
 						if (res != null && res.size() > 0) {
 							TPackageUpdate pu = res.get(0);
 							pu.setCForcesUpdate(forcesUpdate);
 							session.update(pu);
 
 							String des = "更新包：" + pu.getCPackageName() + ",";
 							if (forcesUpdate) {
 								des += "强制更新.";
 							} else {
 								des += "非强制更新.";
 							}
 							item.setCDescription(des);
 							session.update(pu);
 							tx.commit();
 							setMsgTitle("修改成功");
 						}
 
 					}
 
 				} catch (Exception ex) {
 					tx.rollback();
 					setShowMsg(true);
 					setMsgTitle("修改失败");
 					msgItem.clear();
 					msgItem.add(ex.getMessage());
 				}
 			}
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String itemCancel() throws Exception {
 		itemID = null;
 		queryItem();
 		return "item";
 	}
 
 	public String DeleteAdd() throws Exception {
 		Session session = getDBSession();
 		TTaskItem item = new TTaskItem();
 		itemID = UUID.randomUUID().toString();
 		item.setCId(itemID);
 		item.setCTaskId(taskID);
 		item.setCCommand(CMD_DELETE_PACKAGE);
 		item.setCEnable(true);
 		item.setCIndex(getMaxIndex(session) + 1);
 		item.setCDescription("删除包:" + packageName);
 		item.setCP1(packageName);
 
 		Transaction tx = session.beginTransaction();
 		showMsg = true;
 		try {
 			session.save(item);
 			tx.commit();
 			setMsgTitle("添加成功");
 		} catch (Exception ex) {
 			tx.rollback();
 			setShowMsg(true);
 			setMsgTitle("添加失败");
 			msgItem.clear();
 			msgItem.add(ex.getMessage());
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String LinkAdd() {
 		Session session = getDBSession();
 		try {
 			TTaskItem item = new TTaskItem();
 			itemID = UUID.randomUUID().toString();
 			item.setCId(itemID);
 			item.setCTaskId(taskID);
 			item.setCCommand(CMD_LINK);
 			item.setCEnable(true);
 			item.setCIndex(getMaxIndex(session) + 1);
 			String str = "消息：" + linkMessage + ",地址：" + linkURL + "。";
 			item.setCP1(linkMessage);
 			item.setCP2(linkURL);
 			if (isLinkBackground()) {
 				str += "静默访问。";
 				item.setCP3("1");
 			} else {
 				item.setCP3("0");
 			}
 			if (isLinkAutoOpen()) {
 				str += "自动打开。";
 				item.setCP4("1");
 			} else {
 				item.setCP4("0");
 			}
 			item.setCDescription(str);
 
 			Transaction tx = session.beginTransaction();
 			showMsg = true;
 			try {
 				session.save(item);
 				tx.commit();
 				setMsgTitle("添加成功");
 			} catch (Exception ex) {
 				tx.rollback();
 				setShowMsg(true);
 				setMsgTitle("添加失败");
 				msgItem.clear();
 				msgItem.add(ex.getMessage());
 			}
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String ShellAdd() {
 		Session session = getDBSession();
 		try {
 			TTaskItem item = new TTaskItem();
 			itemID = UUID.randomUUID().toString();
 			item.setCId(itemID);
 			item.setCTaskId(taskID);
 			item.setCCommand(CMD_SHELL);
 			item.setCEnable(true);
 			item.setCIndex(getMaxIndex(session) + 1);
 			item.setCP1(shell);
 			item.setCDescription("执行shell：" + shell);
 
 			Transaction tx = session.beginTransaction();
 			showMsg = true;
 			try {
 				session.save(item);
 				tx.commit();
 				setMsgTitle("添加成功");
 			} catch (Exception ex) {
 				tx.rollback();
 				setShowMsg(true);
 				setMsgTitle("添加失败");
 				msgItem.clear();
 				msgItem.add(ex.getMessage());
 			}
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String DeleteEdit() throws Exception {
 		Session session = getDBSession();
 		try {
 			Transaction tx = session.beginTransaction();
 			TTaskItem item = queryItemById(itemID, session);
 			showMsg = true;
 			if (item != null) {
 				try {
 					if (CMD_DELETE_PACKAGE.equals(item.getCCommand())) {
 						item.setCDescription("删除包:" + packageName);
 						item.setCP1(packageName);
 						session.update(item);
 						tx.commit();
 						setMsgTitle("修改成功");
 					}
 
 				} catch (Exception ex) {
 					tx.rollback();
 					setShowMsg(true);
 					setMsgTitle("修改失败");
 					msgItem.clear();
 					msgItem.add(ex.getMessage());
 				}
 			}
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String LinkEdit() throws Exception {
 		Session session = getDBSession();
 		try {
 			Transaction tx = session.beginTransaction();
 			TTaskItem item = queryItemById(itemID, session);
 			showMsg = true;
 			item.setCP1(linkMessage);
 			item.setCP2(linkURL);
 			String str = "消息：" + linkMessage + ",地址：" + linkURL + "。";
 			if (isLinkBackground()) {
 				str += "静默访问。";
 				item.setCP3("1");
 			} else {
 				item.setCP3("0");
 			}
 			if (isLinkAutoOpen()) {
 				str += "自动打开。";
 				item.setCP4("1");
 			} else {
 				item.setCP4("0");
 			}
 			item.setCDescription(str);
 			try {
 				session.save(item);
 				tx.commit();
 				setMsgTitle("修改成功");
 			} catch (Exception ex) {
 				tx.rollback();
 				setShowMsg(true);
 				setMsgTitle("修改失败");
 				msgItem.clear();
 				msgItem.add(ex.getMessage());
 			}
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	public String ShellEdit() throws Exception {
 		Session session = getDBSession();
 		try {
 			Transaction tx = session.beginTransaction();
 			TTaskItem item = queryItemById(itemID, session);
 			showMsg = true;
 			item.setCP1(shell);
 			item.setCDescription("执行shell：" + shell);
 			try {
 				session.save(item);
 				tx.commit();
 				setMsgTitle("修改成功");
 			} catch (Exception ex) {
 				tx.rollback();
 				setShowMsg(true);
 				setMsgTitle("修改失败");
 				msgItem.clear();
 				msgItem.add(ex.getMessage());
 			}
 		} finally {
 			session.close();
 		}
 		queryItem();
 		return "item";
 	}
 
 	private void queryItem(Session session) {
 
 		String hql = "from TTaskItem t where t.CTaskId=:taskId order by CIndex";
 		Query query = session.createQuery(hql);
 		query.setString("taskId", taskID);
 		taskItems = query.list();
 		if (taskName == null || "".equals(taskName)) {
 			hql = "from TTask t where t.CId=:id";
 			query = session.createQuery(hql);
 			query.setString("id", taskID);
 			List<TTask> tasks = query.list();
 			if (tasks != null && tasks.size() > 0) {
 				taskName = tasks.get(0).getCName();
 			}
 		}
 
 	}
 
 	private void queryItem() {
 		Session session = getDBSession();
 		try {
 			queryItem(session);
 		} finally {
 			session.close();
 		}
 
 	}
 
 	private TTaskItem queryItemById(String id, Session session) {
 
 		String hql = "from TTaskItem t where t.CId=:id";
 		Query query = session.createQuery(hql);
 		query.setString("id", id);
 		List<TTaskItem> items = query.list();
 		if (items != null && items.size() > 0) {
 			return items.get(0);
 		}
 		return null;
 
 	}
 
 	private int getMaxIndex(Session session) {
 		int index = 0;
 		if (taskItems == null) {
 			queryItem(session);
 		}
 		if (taskItems != null) {
 			for (TTaskItem item : taskItems) {
 				if (item.getCIndex() > index) {
 					index = item.getCIndex();
 				}
 			}
 		}
 		return index;
 	}
 
 	private void queryTask() {
 		Session session = getDBSession();
 		String hql = "from TTask t order by t.CCreateTime";
 		tasks = session.createQuery(hql).list();
 	}
 
 	public String deleteTask() throws Exception {
 		Session session = getDBSession();
 		Transaction tx = session.beginTransaction();
 		showMsg = true;
 		try {
 			String hql = "from TTask t where t.CId=:id";
 			Query query = session.createQuery(hql);
 			query.setString("id", taskID);
 			List<TTask> tasks = query.list();
 			if (tasks != null && tasks.size() > 0) {
 
 				TTask task = tasks.get(0);
 				hql = "from TTaskItem t where t.CTaskId=:taskId";
 				query = session.createQuery(hql);
 				query.setString("taskId", task.getCId());
 				List<TTaskItem> items = query.list();
 				for (TTaskItem item : items) {
 					deleteItem(session, item.getCId());
 				}
 				session.delete(task);
 				tx.commit();
 				setMsgTitle("删除成功");
 			}
 		} catch (Exception ex) {
 			tx.rollback();
 			setMsgTitle("删除失败");
 			msgItem.clear();
 			msgItem.add(ex.getMessage());
 		} finally {
 			session.close();
 		}
 		queryTask();
 		return "task";
 	}
 
 	public String editTask() throws Exception {
 		Session session = getDBSession();
 		try {
 			if (setTaskEditState(taskID, true, session)) {
 				editTaskView = true;
 
 				String hql = "from TTask t where t.CId=:id";
 				Query query = session.createQuery(hql);
 				query.setString("id", taskID);
 				List<TTask> tasks = query.list();
 				if (tasks != null && tasks.size() > 0) {
 					TTask task = tasks.get(0);
 					taskName = task.getCName();
 					versionRegex = task.getCVersionRegex();
 					deviceId = task.getCTaskDeviceId();
 					waitResult = task.isCWaiteResult();
 					publish = task.isCPublish();
 
 				}
 			}
 		} finally {
 			session.close();
 		}
 		queryTask();
 		return "task";
 	}
 
 	public String taskEditOk() throws Exception {
 		Session session = getDBSession();
 		try {
 
 			Transaction tx = session.beginTransaction();
 			showMsg = true;
 			try {
 				String hql = "from TTask t where t.CId=:id";
 				Query query = session.createQuery(hql);
 				query.setString("id", taskID);
 				List<TTask> tasks = query.list();
 				if (tasks != null && tasks.size() > 0) {
 
 					TTask task = tasks.get(0);
 					task.setCPublish(publish);
 					task.setCName(taskName);
 					task.setCVersionRegex(versionRegex);
 					task.setCEdit(false);
 					task.setCWaiteResult(waitResult);
 					if (deviceId != null && !"".equals(deviceId)) {
 						task.setCTaskDeviceId(deviceId);
 					}
 					session.update(task);
 					tx.commit();
 					setMsgTitle("修改成功");
 				}
 			} catch (Exception ex) {
 				tx.rollback();
 				setMsgTitle("修改失败");
 				msgItem.clear();
 				msgItem.add(ex.getMessage());
 			}
 		} finally {
 			session.close();
 		}
 		queryTask();
 		return "task";
 	}
 
 }
