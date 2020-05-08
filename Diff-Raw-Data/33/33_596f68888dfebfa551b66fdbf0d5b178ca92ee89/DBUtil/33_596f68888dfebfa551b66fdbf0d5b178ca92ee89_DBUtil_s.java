 package com.quuzz.doors.server;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.FlushModeType;
 import javax.persistence.Persistence;
 import javax.persistence.TypedQuery;
 
 import com.quuzz.doors.client.DoorDTO;
 import com.quuzz.doors.client.UserDTO;
 
 public class DBUtil {
 
 	static EntityManagerFactory entityManagerFactory;
 	static EntityManager entityManager;
 	static Map<Long, Long> sessionsMap = new HashMap<Long, Long>();
 	
 	private static synchronized EntityManager getEntityManager() {
 		if (entityManager == null) {
 			entityManagerFactory = Persistence.createEntityManagerFactory("default");
 			entityManager = entityManagerFactory.createEntityManager();
 		}
 		if (!entityManager.isOpen()) {
 			entityManager = entityManagerFactory.createEntityManager();
 		}
 		entityManager.setFlushMode(FlushModeType.AUTO);
 		return entityManager;
 	}
 
 	public static synchronized List<DoorDTO> getDoors(String criteriaId) {
 		List<DoorDTO> doors = new LinkedList<DoorDTO>();
 		EntityManager em = getEntityManager();
 		try {
 			TypedQuery<Door> query = null;
 			if (criteriaId != null) {
 				query = em.createQuery("from Door d where d.id = :recId", Door.class);
 				query.setParameter("recId", Long.valueOf(criteriaId));
 			} else {
 				query = em.createQuery("from Door", Door.class);
 			}
 			for (Door door : query.getResultList()) {
 				doors.add(new DoorDTO(door.getId(), door.getQuestion(), door.getAnswer(), door.getCreatorId()));
 			}
 			return doors;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static synchronized List<UserDTO> getUsersByLogin(String loginValue) {
 		List<UserDTO> users = new LinkedList<UserDTO>();
 		EntityManager em = getEntityManager();
 		try {
 			TypedQuery<User> query = em.createQuery("from user_t u where u.login = :recId", User.class);
 			query.setParameter("recId", loginValue);
 			List<User> resultList = query.getResultList();
 			if(resultList != null &&  resultList.size() > 0)
 				users.add(new UserDTO(0L, "", "", "", null, null));
 			return users;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static synchronized List<UserDTO> getUsersByLoginPassword(String loginValue, String password) {
 		List<UserDTO> users = new LinkedList<UserDTO>();
 		EntityManager em = getEntityManager();
 		try {
 			TypedQuery<User> query = em.createQuery("from user_t u where u.login = :recId", User.class);
 			query.setParameter("recId", loginValue);
 			for (User user : query.getResultList()) {
 				if(!BCrypt.checkpw(password, user.getPassword()))
 					continue;
 				List<DoorDTO> userDoors = new LinkedList<DoorDTO>();
 				for (Door door : user.getDoors()) {
 					DoorDTO doorDTO = new DoorDTO(door.getId(), door.getQuestion(), door.getAnswer(), door.getCreatorId());
 					userDoors.add(doorDTO);
 				}
 				// TODO add contacts ?
 				long sessionId = Long.valueOf(System.currentTimeMillis()).longValue(); 
 				sessionsMap.put(sessionId, user.getId());
 				users.add(new UserDTO(sessionId, user.getContactLine(), user.getLogin(), 
 						user.getPassword(), userDoors, null));
 			}
 			return users;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static synchronized List<UserDTO> getUsers(String criteriaId) {
 		List<UserDTO> users = new LinkedList<UserDTO>();
 		EntityManager em = getEntityManager();
 		try {
 			TypedQuery<User> query = null;
 			if (criteriaId != null) {
 				query = em.createQuery("from user_t u where u.id = :recId", User.class);
 				query.setParameter("recId", Long.valueOf(criteriaId));
 			} else {
 				query = em.createQuery("from user_t", User.class);
 			}
 			for (User user : query.getResultList()) {
 				List<DoorDTO> userDoors = new LinkedList<DoorDTO>();
 				List<UserDTO> userContacts = new LinkedList<UserDTO>();
 				for (Door door : user.getDoors()) {
 					DoorDTO doorDTO = new DoorDTO(door.getId(), door.getQuestion(), door.getAnswer(), door.getCreatorId());
 					userDoors.add(doorDTO);
 				}
 				if (criteriaId != null) {
 					for (User contactUser : user.getContacts()) {
 						// контакты рекурсивно добавлять пока не нужно
 						UserDTO userDTO = new UserDTO(user.getId(), user.getContactLine(), user.getLogin(), user.getPassword(), userDoors, null);
 						userContacts.add(userDTO);
 					}
 				}
 				users.add(new UserDTO(user.getId(), user.getContactLine(), user.getLogin(), user.getPassword(), userDoors, userContacts));
 			}
 			return users;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static synchronized List<DoorDTO> getUserOpenedDoors(String userId) {
 		List<DoorDTO> doors = new LinkedList<DoorDTO>();
 		EntityManager em = getEntityManager();
 		try {
 			TypedQuery<User> query = null;
 			query = em.createQuery("from user_t d where d.id = :recId", User.class);
 			query.setParameter("recId", sessionsMap.get(Long.valueOf(userId)));
 			User user = query.getSingleResult();
 			for (Door door : user.getDoors()) {
 				if (sessionsMap.get(Long.valueOf(userId)) == door.getCreatorId())
 					continue;
 				doors.add(new DoorDTO(door.getId(), door.getQuestion(), door.getAnswer(), door.getCreatorId()));
 			}
 			return doors;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return doors;
 	}
 
 	public static synchronized List<DoorDTO> getUserOtherDoors(String userId, String filterValue) {
 		List<DoorDTO> doors = new LinkedList<DoorDTO>();
 		EntityManager em = getEntityManager();
 		try {
 			if (userId == null || "null".equals(userId)) {
 				for (Door door : em.createQuery("from Door", Door.class).getResultList()) {
 					doors.add(new DoorDTO(door.getId(), door.getQuestion(), door.getAnswer(), door.getCreatorId()));
 				}
 				return doors;
 			}
 			TypedQuery<Door> query = null; 
 			if(filterValue == null) {
 				query = em.createQuery("from Door", Door.class);
 			} else {
 				query = em.createQuery("from Door d where d.answer like :par1", Door.class);
 				query.setParameter("par1", filterValue);
 			}
 			List<Door> doorsList = query.getResultList();
 			TypedQuery<User> query2 = em.createQuery("from user_t u where u.id = :recId", User.class);
 			query2.setParameter("recId", sessionsMap.get(Long.valueOf(userId)));
 			User user = query2.getSingleResult();
 			Collection<Door> userDoorList = user.getDoors();
 			doorsList.removeAll(userDoorList);
 			for (Door door : doorsList) {
 				doors.add(new DoorDTO(door.getId(), door.getQuestion(), door.getAnswer(), door.getCreatorId()));
 			}
 			return doors;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static synchronized List<DoorDTO> getUserMyDoors(String userId) {
 		List<DoorDTO> doors = new LinkedList<DoorDTO>();
 		EntityManager em = getEntityManager();
 		if (userId == null || "null".equals(userId)) {
 			return doors;
 		}
 		try {
 			TypedQuery<User> query = em.createQuery("from user_t u where u.id = :recId", User.class);
 			query.setParameter("recId", sessionsMap.get(Long.valueOf(userId)));
 			User user = query.getSingleResult();
 			if (user == null) {
 				return doors;
 			}
 			for (Door door : user.getDoors()) {
 				if (sessionsMap.get(Long.valueOf(userId)) == door.getCreatorId())
 					doors.add(new DoorDTO(door.getId(), door.getQuestion(), door.getAnswer(), door.getCreatorId()));
 			}
 			return doors;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return Collections.emptyList();
 	}
 
 	public static synchronized void addOpenedDoor(UserDTO data) {
 		EntityManager em = getEntityManager();
 		User user = em.find(User.class, sessionsMap.get(data.getId()));
 		Door openedDoor = em.find(Door.class, data.getDoors().get(0).getId());
 		if (!user.getDoors().contains(openedDoor)) {
 			if(!em.getTransaction().isActive())
 				em.getTransaction().begin();
 			user.getDoors().add(openedDoor);
 			em.merge(user);
 			em.getTransaction().commit();
			em.close();
 		}
 	}
 
 	public static synchronized void remove(DoorDTO data) {
 		EntityManager em = getEntityManager();
 		Door door = em.find(Door.class, data.getId());
		if(!em.getTransaction().isActive())
			em.getTransaction().begin();
 		em.remove(door);
 		em.getTransaction().commit();
 	}
 
 	public static synchronized DoorDTO add(DoorDTO data) {
 		if (data.getCreatorId() == null)
 			return data;
 		EntityManager em = getEntityManager();
 		Door door = new Door();
 		door.setAnswer(data.getAnswer());
 		door.setQuestion(data.getQuestion());
 		door.setCreatorId(sessionsMap.get(data.getCreatorId()));
 		em.getTransaction().begin();
 		em.persist(door);
 		TypedQuery<User> query = em.createQuery("from user_t u where u.id = :recId", User.class);
 		query.setParameter("recId", sessionsMap.get(data.getCreatorId()));
 		User user = query.getSingleResult();
 		user.getDoors().add(door);
 		em.persist(user);
 		em.getTransaction().commit();
		em.close();
 		data.setId(door.getId());
 		return data;
 	}
 
 	public static synchronized UserDTO add(UserDTO data) {
 		EntityManager em = getEntityManager();
 		User user = new User();
 		user.setLogin(data.getLogin());
 		
 		String hashed = BCrypt.hashpw(data.getPassword(), BCrypt.gensalt());
 		
 //		user.setPassword(data.getPassword());
 		user.setPassword(hashed);
 		user.setContactLine(data.getContactLine());
 		user.setDoors(new LinkedList<Door>());
 		user.setContacts(new LinkedList<User>());
 		em.getTransaction().begin();
 		em.persist(user);
 		em.getTransaction().commit();
		em.close();
 		data.setId(user.getId());
 		return data;
 	}
 
 	public static synchronized List<UserDTO> getUsersByContacts(String criteriaId) {
 		List<UserDTO> users = new LinkedList<UserDTO>();
 		if (criteriaId == null)
 			return users;
 		EntityManager em = getEntityManager();
 		User user = em.find(User.class, sessionsMap.get(Long.valueOf(criteriaId)));
 		if (user.getContacts() == null)
 			return users;
 		for (User contactUser : user.getContacts()) {
 			UserDTO userDTO = new UserDTO(contactUser.getId(), contactUser.getContactLine(), "", "", null, null);
 			users.add(userDTO);
 		}
 		return users;
 	}
 
 	public static synchronized void addInterceptionUser(String criteriaId, String userId) {
 		// TODO here will regulate interception parameters
 		// перебираю собственные двери другого пользователя,
 		// если процент совпадения с открытыми дверями текущего пользователя
 		// более 10 %
 		// то возвращаю контакт (т.е. другого пользователя)
 		EntityManager em = getEntityManager();
 
 		TypedQuery<Door> query = em.createQuery("from Door d where d.creatorId = :recId", Door.class);
 		query.setParameter("recId", Long.valueOf(userId));
 		User currentUser = em.find(User.class, sessionsMap.get(Long.valueOf(criteriaId)));
 		double otherDoors = 0;
 		double interceptedDoors = 0;
 		for (Door otherDoor : query.getResultList()) {
 			otherDoors++;
 			if (currentUser.getDoors().contains(otherDoor)) {
 				interceptedDoors++;
 			}
 		}
 		// TODO 90%
 		if (interceptedDoors / otherDoors > 0.9) {
 			User otherUser = em.find(User.class, Long.valueOf(userId));
 			if (!currentUser.getContacts().contains(otherUser)) {
 				if(!em.getTransaction().isActive())
 					em.getTransaction().begin();
 				currentUser.getContacts().add(otherUser);
 				em.merge(currentUser);
 				em.getTransaction().commit();
				em.close();
 			}
 		}
 	}
 
 	public static synchronized void addContactUser(UserDTO data) {
 		EntityManager em = getEntityManager();
 		User user = em.find(User.class, data.getId());
 		User userToAdd = em.find(User.class, Long.valueOf(data.getContactLine()));
 		if (!user.getContacts().contains(userToAdd)) {
 			user.getContacts().add(userToAdd);
 			em.getTransaction().begin();
 			em.merge(user);
 			em.getTransaction().commit();
			em.close();
 		}
 	}
 
 	public static synchronized DoorDTO updateDoor(DoorDTO data, String operationType) {
 		if ("otherDoors".equals(operationType)) {
 			return data;
 		}
 		EntityManager em = getEntityManager();
 		Door door = em.find(Door.class, data.getId());
 		door.setAnswer(data.getAnswer());
 		door.setQuestion(data.getQuestion());
 		em.getTransaction().begin();
 		em.persist(door);
 		em.getTransaction().commit();
		em.close();
 		return data;
 	}
 
 	public static synchronized void updateContactUserData(UserDTO data) {
 		EntityManager em = getEntityManager();
 		User user = em.find(User.class, Long.valueOf(sessionsMap.get(data.getId())));
 		user.setContactLine(data.getContactLine());
 		em.getTransaction().begin();
 		em.persist(user);
 		em.getTransaction().commit();
		em.close();
 	}
 
 	public static synchronized List<UserDTO> getUserById(String criteriaId) {
 		List<UserDTO> users = new LinkedList<UserDTO>();
 		if(criteriaId == null) 
 			return users;
 		EntityManager em = getEntityManager();
 		User user = em.find(User.class, Long.valueOf(sessionsMap.get(Long.valueOf(criteriaId))));
 		users.add(new UserDTO(null, user.getContactLine(), user.getLogin(), 
 				user.getPassword(), null, null));
 		return users;
 	}
 }
