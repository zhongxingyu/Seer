 package com.schedushare.core.schedule.service.impl;
 
 import java.sql.Connection;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.List;
 
 import org.jooq.Field;
 import org.jooq.Record;
 import org.jooq.Result;
 import org.jooq.impl.Factory;
 
 import com.google.inject.Inject;
 import com.schedushare.common.domain.dto.ScheduleEntity;
 import com.schedushare.common.domain.dto.ScheduleListEntity;
 import com.schedushare.common.domain.dto.TimeBlockEntity;
 import com.schedushare.common.domain.dto.TimeBlocksEntity;
 import com.schedushare.common.domain.exception.SchedushareException;
 import com.schedushare.common.domain.exception.SchedushareExceptionFactory;
 import com.schedushare.core.database.SchedushareFactory;
 import com.schedushare.core.database.Tables;
 import com.schedushare.core.schedule.service.ScheduleService;
 import com.schedushare.core.timeblocks.service.TimeBlocksService;
 
 /**
  * Implements {@link ScheduleService}.
  */
 public class ScheduleServiceImpl implements ScheduleService {
 
 	private final SchedushareExceptionFactory schedushareExceptionFactory;
 	private final TimeBlocksService timeBlocksService;
 	
 	@Inject
 	public ScheduleServiceImpl(final SchedushareExceptionFactory schedushareExceptionFactory,
 							   final TimeBlocksService timeBlocksService) {
 		this.schedushareExceptionFactory = schedushareExceptionFactory;
 		this.timeBlocksService = timeBlocksService;
 	}
 	
 	@Override
 	public ScheduleEntity getSchedule(Connection connection, int scheduleId)
 			throws SchedushareException {
 		
 		SchedushareFactory getScheduleQuery = new SchedushareFactory(connection);
 		
 		List<ScheduleEntity> queryResult = getScheduleQuery.select()
 						.from(Tables.SCHEDULE)
 						.where(Tables.SCHEDULE.ID.equal(scheduleId))
 						.fetchInto(ScheduleEntity.class);
 		
 		if (queryResult.isEmpty()) {
 			throw schedushareExceptionFactory.createSchedushareException("Schedule does not exist");
 		} else {
 			ScheduleEntity retrievedSchedule = queryResult.get(0);
 			Collection<TimeBlockEntity> timeBlockEntities = new ArrayList<TimeBlockEntity>();
 			
 			Result<Record> timeBlocksRecords = getScheduleQuery.select()
 					.from(Tables.TIMEBLOCK)
 					.where(Tables.TIMEBLOCK.SCHEDULE_ID.equal(scheduleId)).fetch();
 			
 			for (Record timeBlockRecord : timeBlocksRecords) {
 				timeBlockEntities.add(new TimeBlockEntity(timeBlockRecord.getValue(Tables.TIMEBLOCK.ID),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.START_TIME),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.END_TIME),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.DAY).toString(),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.NAME),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.TYPE),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.LATITUDE),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.LONGITUDE),
 														  timeBlockRecord.getValue(Tables.TIMEBLOCK.SCHEDULE_ID)));
 			}
 			ScheduleEntity getScheduleResult = new ScheduleEntity(retrievedSchedule.getScheduleId(), retrievedSchedule.getScheduleName(), 
 					retrievedSchedule.isScheduleActive(), retrievedSchedule.getUserId(), retrievedSchedule.getT_lastModified().toString(),
 					timeBlockEntities);
 			return getScheduleResult;
 		}
 	}
 
 	@Override
 	public ScheduleEntity getActiveScheduleForUser(Connection connection,
 			String userId) throws SchedushareException {
 		
 		try {
 			SchedushareFactory getActiveScheduleQuery = new SchedushareFactory(connection);
 			
 			List<ScheduleEntity> scheduleEntitiesResult = getActiveScheduleQuery.select().from(Tables.SCHEDULE)
 					.where(Tables.SCHEDULE.ACTIVE.equal(Boolean.TRUE))
 					.and(Tables.SCHEDULE.USER_ID.equal(userId))
 					.fetchInto(ScheduleEntity.class);
 			
 			if (scheduleEntitiesResult.isEmpty()) {
 				return null;
 			} else {
 				ScheduleEntity scheduleEntity = scheduleEntitiesResult.get(0);
				TimeBlocksEntity timeBlocksForSchedule = timeBlocksService.getTimeBlocksForSchedule(connection, scheduleEntity.getScheduleId());
				
 				return new ScheduleEntity(scheduleEntity.getScheduleId(), 
 						scheduleEntity.getScheduleName(), 
 						scheduleEntity.isScheduleActive(), 
 						scheduleEntity.getUserId(), 
 						scheduleEntity.getT_lastModified().toString(), 
						timeBlocksForSchedule.getTimeBlocks());
 			}
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 	@Override
 	public ScheduleListEntity getSchedulesForUser(
 			Connection connection, String userId) throws SchedushareException {
 		SchedushareFactory getSchedulesQuery = new SchedushareFactory(connection);
 		
 		try {
 			List<ScheduleEntity> queryResult = getSchedulesQuery.select()
 					.from(Tables.SCHEDULE)
 					.where(Tables.SCHEDULE.USER_ID.equal(userId))
 					.fetchInto(ScheduleEntity.class);
 			return new ScheduleListEntity(queryResult);
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 
 	@Override
 	public ScheduleEntity createScheduleForUser(Connection connection, ScheduleEntity scheduleEntity) throws SchedushareException {
 
 		SchedushareFactory createScheduleQuery = new SchedushareFactory(connection);
 		Collection<TimeBlockEntity> timeBlocks = scheduleEntity.getTimeBlocks();
 		
 		try {
 			Boolean isScheduleActive = scheduleEntity.isScheduleActive();
 			if (isScheduleActive) {
 				createScheduleQuery.update(Tables.SCHEDULE)
 								   .set(Tables.SCHEDULE.ACTIVE, Boolean.FALSE)
 								   .where(Tables.SCHEDULE.ACTIVE.equal(Boolean.TRUE)
 										   .and(Tables.SCHEDULE.USER_ID.equal(scheduleEntity.getUserId())))
 								   .execute();
 			}
 			createScheduleQuery.insertInto(Tables.SCHEDULE, 
 										   Tables.SCHEDULE.NAME, 
 										   Tables.SCHEDULE.LAST_MODIFIED, 
 										   Tables.SCHEDULE.ACTIVE, 
 										   Tables.SCHEDULE.USER_ID)
 							    .values(scheduleEntity.getScheduleName(), 
 							    		new Time(Calendar.getInstance().getTimeInMillis()).getTime(), 
 							    		isScheduleActive, 
 							    		scheduleEntity.getUserId())
 							    		.execute();
 			//Look for a better way to do this.
 			Field<?> identity = Factory.field("@@IDENTITY");
 			Integer createdScheduleId = createScheduleQuery.select(identity)
 																	  .from(Tables.SCHEDULE)
 																	  .fetchInto(Integer.class)
 																	  .get(0);
 			timeBlocksService.createTimeBlocks(connection, new TimeBlocksEntity(createdScheduleId, timeBlocks));
 
 			return getSchedule(connection, createdScheduleId);
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 	@Override
 	public ScheduleEntity deleteSchedule(Connection connection, int scheduleId) throws SchedushareException {
 		SchedushareFactory deleteScheduleQuery = new SchedushareFactory(connection);
 		
 		try {
 			ScheduleEntity scheduleEntity = getSchedule(connection, scheduleId);
 			deleteScheduleQuery.delete(Tables.SCHEDULE)
 							   .where(Tables.SCHEDULE.ID.equal(scheduleId))
 							   .execute();
 			return scheduleEntity;
 		} catch (SchedushareException e) {
 			throw e;
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 	@Override
 	public ScheduleEntity updateSchedule(Connection connection,
 			ScheduleEntity scheduleEntity) throws SchedushareException {
 
 		SchedushareFactory updateScheduleQuery = new SchedushareFactory(connection);
 		try {
 			//harden this to check that a user has active schedules before updating.
 			int scheduleId = scheduleEntity.getScheduleId();
 			Boolean isScheduleActive = scheduleEntity.isScheduleActive();
 			if (isScheduleActive) {
 				updateScheduleQuery.update(Tables.SCHEDULE)
 								   .set(Tables.SCHEDULE.ACTIVE, Boolean.FALSE)
 								   .where(Tables.SCHEDULE.ACTIVE.equal(Boolean.TRUE)
 										   .and(Tables.SCHEDULE.USER_ID.equal(scheduleEntity.getUserId())))
 								   .execute();
 			}
 			updateScheduleQuery.update(Tables.SCHEDULE)
 							   .set(Tables.SCHEDULE.ACTIVE, isScheduleActive)
 							   .set(Tables.SCHEDULE.LAST_MODIFIED, new Time(Calendar.getInstance().getTimeInMillis()))
 							   .set(Tables.SCHEDULE.NAME, scheduleEntity.getScheduleName())
 							   .where(Tables.SCHEDULE.ID.equal(scheduleId))
 							   .execute();
 			return getSchedule(connection, scheduleId);
 		} catch (SchedushareException e) {
 			throw e;
 		} catch (Exception e) {
 			throw schedushareExceptionFactory.createSchedushareException(e.getMessage());
 		}
 	}
 
 	
 }
