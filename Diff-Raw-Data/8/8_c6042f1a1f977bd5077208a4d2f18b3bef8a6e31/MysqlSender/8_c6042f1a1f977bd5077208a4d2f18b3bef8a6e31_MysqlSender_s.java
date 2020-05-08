 /**
  * 
  */
 package z.batchsender.extended;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.RandomAccessFile;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import z.batchsender.core.BatchSendException;
 import z.batchsender.core.MailEntity;
 import z.batchsender.core.ThreadSender;
 import z.batchsender.util.Util;
 import z.tool.util.PropertiesLoader;
 import z.tool.util.ZUtils;
 
 /**
  * 从mysql获取数据，组织调度批量发信
  * @author auzll@163.com
  * @since 2011-11-30 下午09:02:25
  */
 public class MysqlSender {
 	private static final Log LOG = LogFactory.getLog(MysqlSender.class);
 	
 	private static final String LOCK_FILE_NAME = "Lock" + MysqlSender.class.getSimpleName();
 	
 	private int threadSize; 
 	private int taskOfEachThread; 
 	private int transportUsingTimes;
 	
 	private String dbUrl;
 	private String dbUser;
 	private String dbPassword;
 	
 	private long taskId;
 	private String csvPath = null;
 	private long csvCurrentOffset = 0;
 	private int totalSize = 0;
 	private int currentSize = 0;
 	private String smtpHost = null;
 	private int smtpPort = 25;
 	private String charset = null;
 	private Date beginTime = null;
 	
 	private String fromEmail = null;
 	private String fromPassword = null;
 	private String fromPersonal = null;
 	private String subject = null;
 	private String content = null;
 	private String contentPath = null;
 	private String contentType = null;
 	
 	private List<MailEntity> entities;
 	private int realDataCount;
 	
 	public MysqlSender dbPassword(String dbPassword) {
 		this.dbPassword = dbPassword;
 		return this;
 	}
 
 	public MysqlSender dbUrl(String dbUrl) {
 		this.dbUrl = dbUrl;
 		return this;
 	}
 
 	public MysqlSender dbUser(String dbUser) {
 		this.dbUser = dbUser;
 		return this;
 	}
 
 	/**
 	 * 执行调度
 	 */
 	public void direct(String lockRecordFile) {
 		// 检查文件锁，无锁则加锁，有锁则退出并等待下次调度
 		checkAndLock(lockRecordFile);
 		
 		// 加载任务
 		loadTask();
 		
 		// 准备邮件实体数据
 		prepareData();
 		
 		// 发送邮件
 		sendMail();
 		
 		// 更新任务状态
 		updateTask();
 		
 		// 释放文件锁
 		releaseLock();
 	}
 
 	public MysqlSender taskOfEachThread(int taskOfEachThread) {
 		this.taskOfEachThread = taskOfEachThread;
 		return this;
 	}
 
 	public MysqlSender threadSize(int threadSize) {
 		this.threadSize = threadSize;
 		return this;
 	}
 
 	public MysqlSender transportUsingTimes(int transportUsingTimes) {
 		this.transportUsingTimes = transportUsingTimes;
 		return this;
 	}
 	
 	private String[] analyzeCsvData(String line) {
 		// TODO 暂时不能处理数据中含逗号等特殊字符的情况
 		return null != line ? line.split(",") : null;
 	}
 	
 	/**
 	 * 检查有无文件锁，有则表示上次发送任务尚未完成，暂不执行本次发送任务，无则立即加锁
 	 * @param lockRecordFile 若发现文件锁，累加次数，记录到指定文件中
 	 */
 	private void checkAndLock(String lockRecordFile) {
 		File file = new File(LOCK_FILE_NAME);
 		if (!file.exists()) {
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				throw new BatchSendException(e);
 			}
 		} else {
 			if (LOG.isInfoEnabled()) {
 				LOG.info("method:checkAndLock,desc:file[" + LOCK_FILE_NAME 
 						+ "] exist, try later");
 			}
 			
 			try {
 				String propKey = "Lock" + this.getClass().getSimpleName();
 				
 				// 文件不存在就创建
 				File lockWatchFile = new File(lockRecordFile);
 				if (!lockWatchFile.exists()) {
 					lockWatchFile.createNewFile();
 				}
 				
 				// 尝试加载已有记录
 				Properties props = PropertiesLoader.loadProperties(lockRecordFile);
 				Integer lockTimes = 0;
 				if (null != props.getProperty(propKey)) {
 					lockTimes = Integer.valueOf(props.getProperty(propKey));
 				}
 				lockTimes++;
 				
 				// 尝试报警
 				lockTimes = doAlarm(lockTimes);
 				
 				// 存入最新的锁定次数
 				props.put(propKey, lockTimes.toString());
 				OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(lockRecordFile));
 				props.store(out, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
 				out.close();
 			} catch (Exception e) {
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("method:checkAndLock", e);
 				}
 			}
 			
			System.exit(0);
 		}
 	}
 	
 	/**
 	 * 子类覆盖这个方法来控制报警策略
 	 */
 	protected Integer doAlarm(Integer lockTimes) {
 		LOG.info("method:doAlarm,lockTimes:" + lockTimes);
 		return lockTimes;
 	}
 	
 	private void close(Connection conn, Statement stmt, ResultSet rs) {
 		if (null != conn) {
 			try {
 				conn.close();
 			} catch (SQLException e) {
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("method:close", e);
 				}
 			}
 		}
 		
 		if (null != stmt) {
 			try {
 				stmt.close();
 			} catch (SQLException e) {
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("method:close", e);
 				}
 			}
 		}
 		
 		if (null != rs) {
 			try {
 				rs.close();
 			} catch (SQLException e) {
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("method:close", e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 加载任务
 	 */
 	private void loadTask() {
 		Connection conn = null;
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
 			conn.setAutoCommit(false); // 关闭自动提交
 			
 			pstmt = conn.prepareStatement("select * from tb_batchsender_task " +
 					" where status in('Waiting', 'Running') order by id asc limit 1");
 			rs = pstmt.executeQuery();
 			
 			if (rs.next()) {
 				taskId = rs.getLong("id");
 				csvPath = rs.getString("csv_path");
 				csvCurrentOffset = rs.getLong("csv_current_offset");
 				totalSize = rs.getInt("total_size");
 				currentSize = rs.getInt("current_size");
 				smtpHost = rs.getString("smtp_host");
 				smtpPort = rs.getInt("smtp_port");
 				charset = rs.getString("charset");
 				fromEmail = rs.getString("from_email");
 				fromPassword = rs.getString("from_password");
 				fromPersonal = rs.getString("from_personal");
 				subject = rs.getString("subject");
 				content = rs.getString("content");
 				contentPath = rs.getString("content_path");
 				contentType = rs.getString("content_type");
 				beginTime = rs.getTimestamp("begin_time");
 				
 				if (null == beginTime) {
 					// 表示尚未开始
 					beginTime = new Date();
 				}
 				
 				// 关闭连接
 				close(null, pstmt, rs);
 				pstmt = null;
 				rs = null;
 				
 				if (LOG.isInfoEnabled()) {
 					LOG.info("method:loadTask,desc:finish loading,taskId:" + taskId 
 							+ ",totalSize:" + totalSize 
 							+ ",currentSize:" + currentSize);
 				}
 				
 				if (currentSize >= totalSize) {
 					if (LOG.isInfoEnabled()) {
 						LOG.info("method:loadTask,totalSize:" + totalSize 
 								+ ",currentSize:" + currentSize + ",desc:treat as finish");
 					}
 					
 					pstmt = conn.prepareStatement("update tb_batchsender_task " +
 							" set status='Finish', finish_time=now() where id=? ");
 					pstmt.setLong(1, taskId);
 					pstmt.executeUpdate();
 					conn.commit();
 					
 					// 任务实际已完成，解锁，退出程序
 					releaseLock();
					System.exit(0);
 				}
 				
 			} else {
 				if (LOG.isInfoEnabled()) {
 					LOG.info("method:loadTask,desc:there is no any task");
 				}
 				
 				// 没有需要执行的任务，解锁，退出程序
 				releaseLock();
				System.exit(0);
 			}
 		} catch (Exception e) {
 			try {
 				if (null != conn && !conn.getAutoCommit()) {
 					conn.rollback();
 				}
 			} catch (SQLException e1) {
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("method:loadTask,desc:rollback", e1);
 				}
 			}
 			
 			throw new BatchSendException(e);
 			
 		} finally {
 			// 关闭连接
 			close(conn, pstmt, rs);
 		}
 		
 	}
 	
 	/**
 	 * 准备邮件实体数据
 	 */
 	private void prepareData() {
 		try {
 			int max = threadSize * taskOfEachThread;
 			
 			RandomAccessFile file = new RandomAccessFile(csvPath, "r");
 			String headLine = file.readLine();
 			if (null == headLine) {
 				throw new BatchSendException("headLine is null [" + csvPath + "]");
 			}
 			String[] headers = analyzeCsvData(headLine);
 			
 			if (0 != csvCurrentOffset) {
 				file.seek(csvCurrentOffset);
 			}
 			
 			String line = null;
 			Map<String, String> lineMap = new HashMap<String, String>();
 			entities = new ArrayList<MailEntity>();
 			while (realDataCount < max && null != (line = file.readLine())) {
 				line = ZUtils.trimStringNull(line);
 				if (null == line) {
 					continue;
 				}
 				
 				lineMap.clear();
 				String[] cols = analyzeCsvData(line);
 				for (int i = 0, headerLen = headers.length, colLen = cols.length; 
 					i < headerLen && i < colLen; i++) {
 					lineMap.put(headers[i], cols[i]);
 				}
 				
 				MailEntity entity = new MailEntity();
 				entity.to(lineMap.get("toAddress"), lineMap.get("toPersonal"));
 				entity.ccTo(lineMap.get("ccToAddress"), lineMap.get("ccToPersonal"));
 				entity.bccTo(lineMap.get("bccToAddress"), lineMap.get("bccToPersonal"));
 				
 				if ("Static".endsWith(contentType)) {
 					entity.content(content);
 				} else {
 					entity.content(Util.merge(null, contentPath, charset, lineMap));
 				}
 				
 				entities.add(entity);
 				realDataCount++;
 				currentSize++;
 				
 				if (currentSize >= totalSize) {
 					if (LOG.isInfoEnabled()) {
 						LOG.info("method:prepareData,currentSize:" + currentSize 
 								+ ",totalSize:" + totalSize 
 								+ ",desc:break for enough data");
 					}
 					break;
 				}
 			}
 			
 			csvCurrentOffset = file.getFilePointer();
 			
 		} catch (Exception e) {
 			throw new BatchSendException(e);
 		}
 	}
 	
 	/**
 	 * 释放文件锁
 	 */
 	private void releaseLock() {
 		File file = new File(LOCK_FILE_NAME);
 		if (file.exists()) {
 			file.delete();
 		} else {
 			if (LOG.isErrorEnabled()) {
 				LOG.error("method:releaseLock,desc:file[" + LOCK_FILE_NAME + "] missing");
 			}
 		}
 	}
 	
 	/**
 	 * 发送邮件
 	 */
 	private void sendMail() {
 		ThreadSender sender = new ThreadSender()
 			.threadSize(threadSize)
 			.taskOfEachThread(taskOfEachThread);
 		
 		sender.from(fromEmail, fromPersonal)
 			.password(fromPassword)
 			.subject(subject)
 			.content(content)
 			.transportUsingTimes(transportUsingTimes);
 		
 		if (null != charset) {
 			sender.charset(charset);
 		}
 		
 		if (null != smtpHost) {
 			sender.smtpHost(smtpHost);
 		}
 		
 		if (smtpPort > 0) {
 			sender.smtpPort(smtpPort);
 		}
 		
 		sender.send(entities);
 	}
 	
 	/**
 	 * 更新任务状态
 	 */
 	private void updateTask() {
 		Connection conn = null;
 		PreparedStatement pstmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
 			conn.setAutoCommit(false); // 关闭自动提交
 			
 			String status = "Running";
 			Date finishTime = null;
 			
 			if (currentSize >= totalSize || 0 == realDataCount) {
 				status = "Finish";
 				finishTime = new Date();
 			}
 			
 			pstmt = conn.prepareStatement("update tb_batchsender_task " +
 				" set csv_current_offset=?, status=?, finish_time=?, " +
 				" current_size=?, begin_time=? where id=? ");
 			pstmt.setLong(1, csvCurrentOffset);
 			pstmt.setString(2, status);
 			pstmt.setObject(3, finishTime);
 			pstmt.setInt(4, currentSize);
 			pstmt.setObject(5, beginTime);
 			pstmt.setLong(6, taskId);
 			int updateRet = pstmt.executeUpdate();
 			
 			conn.commit();
 			
 			if (LOG.isInfoEnabled()) {
 				LOG.info("method:updateTask,status:" + status 
 						+ ",updateRet:" + updateRet + ",taskId:" + taskId);
 			}
 			
 		} catch (Exception e) {
 			try {
 				if (null != conn && !conn.getAutoCommit()) {
 					conn.rollback();
 				}
 			} catch (SQLException e1) {
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("method:updateTask,desc:rollback", e1);
 				}
 			}
 			throw new BatchSendException(e);
 			
 		} finally {
 			// 关闭连接
 			close(conn, pstmt, rs);
 		}
 	}
 }
