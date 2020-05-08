 /*
  * Copyright 2009 zaichu xiao
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package zcu.xutil.misc;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.shiro.session.Session;
 import org.apache.shiro.session.UnknownSessionException;
 import org.apache.shiro.session.mgt.ValidatingSession;
 import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
 
 import zcu.xutil.Logger;
 import zcu.xutil.Objutil;
 import zcu.xutil.XutilRuntimeException;
 import zcu.xutil.msg.ClusterCache;
 import zcu.xutil.sql.DBTool;
 import zcu.xutil.sql.Handler;
 import zcu.xutil.sql.handl.FirstField;
 import zcu.xutil.utils.ByteArray;
 import zcu.xutil.utils.Util;
 
 public class ShiroSessionDao extends AbstractSessionDAO implements Runnable {
 	private static final Logger logger = Logger.getLogger(ShiroSessionDao.class);
 	private static final String createTable = "CREATE TABLE SHIROSESSION (ID VARCHAR(50),DATAS VARBINARY(8190),EXPIRE TIMESTAMP,PRIMARY KEY(ID))";
 	private static final String insertSql = "INSERT INTO SHIROSESSION VALUES(?,?,DATEADD(HOUR,12,CURRENT_TIMESTAMP))";
 	private static final String deleteSql = "DELETE FROM SHIROSESSION WHERE ID=?";
 	private static final String updateSql = "UPDATE SHIROSESSION SET DATAS=? WHERE ID=?";
 	private static final String querySql = "SELECT DATAS FROM SHIROSESSION WHERE ID=?";
 	private static final String expireDelete = "DELETE FROM SHIROSESSION WHERE CURRENT_TIMESTAMP>EXPIRE";
 
 	private final Handler<byte[]> handle = FirstField.get(byte[].class);
 	private final DBTool dbtool;
 	private final ClusterCache<Serializable, Session> cache;
 	private final Set<Serializable> updatedKeys = new HashSet<Serializable>();
 
 	public ShiroSessionDao(DBTool tool) {
 		this(tool, 300);
 	}
 
 	public ShiroSessionDao(DBTool tool, int maximun) {
 		this.dbtool = tool;
 		try {
 			if (!tool.tableExist("SHIROSESSION"))
 				tool.update(createTable);
 			else
 				tool.update(expireDelete);
 		} catch (SQLException e) {
 			throw new XutilRuntimeException(e);
 		}
 		if (maximun < 200)
 			maximun = 200;
 		this.cache = new ClusterCache<Serializable, Session>(getClass().getName(), maximun);
 		Util.getScheduler().scheduleAtFixedRate(this, 5000, 2000, TimeUnit.MILLISECONDS);
 	}
 
 	@Override
 	public void run() {
 		List<Object> list = new ArrayList<Object>();
 		synchronized (updatedKeys) {
 			Iterator iter = updatedKeys.iterator();
 			while (iter.hasNext()) {
 				Object o = iter.next();
 				if ((o = cache.get(o)) != null)
 					list.add(o);
 			}
 			updatedKeys.clear();
 		}
 		int size = list.size();
 		if (size == 0)
 			return;
 		Object[][] params = new Object[size][2];
 		ByteArray out = new ByteArray(1024);
 		ObjectOutputStream oos = null;
 		try {
 			oos = new ObjectOutputStream(out);
 			while (--size >= 0) {
 				Session s = (Session) list.get(size);
 				list.set(size, params[size][1] = s.getId());
 				oos.writeObject(s);
 				params[size][0] = out.toByteArray();
 				out.reset();
 			}
 			dbtool.batch(updateSql, params);
 			cache.updateNotify(list.toArray());
 		} catch (Exception e) {
 			logger.warn("cluster session dao update fail", e);
 		} finally {
 			Objutil.closeQuietly(oos);
 		}
 	}
 
 	@Override
 	public void delete(Session session) {
 		Serializable id = session.getId();
 		cache.remove(id);
 		try {
 			dbtool.update(deleteSql, id);
 		} catch (SQLException e) {
 			logger.warn("delete session fail.", e);
 		}
 		cache.updateNotify(id);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public Collection<Session> getActiveSessions() {
 		try {
 			dbtool.update(expireDelete);
 		} catch (SQLException e) {
 			logger.warn("expire check error.", e);
 		}
 		return (List) Arrays.asList(cache.values());
 	}
 
 	@Override
 	public void update(Session session) throws UnknownSessionException {
 		Serializable id = session.getId();
 		synchronized (updatedKeys) {
 			updatedKeys.add(id);
 		}
 		if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid())
 			cache.remove(id);
 	}
 
 	@Override
 	protected Serializable doCreate(Session session) {
 		Serializable id = generateSessionId(session);
 		assignSessionId(session, id);
 		ObjectOutputStream oos = null;
 		try {
 			ByteArray out = new ByteArray(1024);
 			(oos = new ObjectOutputStream(out)).writeObject(session);
 			dbtool.update(insertSql, id, out.toByteArray());
 		} catch (Exception e) {
 			throw Objutil.rethrow(e);
 		} finally {
 			Objutil.closeQuietly(oos);
 		}
 		cache.put(id, session);
 		return id;
 	}
 
 	@Override
 	protected Session doReadSession(Serializable key) {
 		Session ret = cache.get(key);
 		if (ret == null) {
 			ObjectInputStream bis = null;
 			try {
 				byte[] data = dbtool.query(querySql, handle, key);
 				if (data != null) {
 					ret = (Session) (bis = new ObjectInputStream(ByteArray.toStream(data))).readObject();
 					ret = Objutil.ifNull(cache.putIfAbsent(key, ret), ret);
 				}
			} catch (RuntimeException e) {
				throw e;
 			} catch (Exception e) {
				logger.warn("read session fail.", e);
 			} finally {
 				Objutil.closeQuietly(bis);
 			}
 		}
 		return ret;
 	}
 }
