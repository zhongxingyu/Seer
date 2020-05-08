 package com.twistlet.falcon.model.repository;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.apache.commons.lang3.time.DateUtils;
 import org.springframework.stereotype.Repository;
 
 import com.mysema.query.jpa.JPQLQuery;
 import com.mysema.query.jpa.impl.JPAQuery;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.twistlet.falcon.model.entity.FalconAppointment;
 import com.twistlet.falcon.model.entity.QFalconAppointment;
 import com.twistlet.falcon.model.entity.QFalconAppointmentPatron;
 import com.twistlet.falcon.model.entity.QFalconLocation;
 import com.twistlet.falcon.model.entity.QFalconPatron;
 import com.twistlet.falcon.model.entity.QFalconService;
 import com.twistlet.falcon.model.entity.QFalconStaff;
 
 @Repository
 public class FalconAppointmentRepositoryImpl implements
 		FalconAppointmentRepositoryCustom {
 
 	@PersistenceContext
 	EntityManager entityManager;
 
 	@Override
 	public List<FalconAppointment> listFullByAppointmentDateBetween(Date start,
 			Date end) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconAppointment falconAppointment = QFalconAppointment.falconAppointment;
 		final QFalconLocation falconLocation = QFalconLocation.falconLocation;
 		final QFalconService falconService = QFalconService.falconService;
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		query.from(falconAppointment);
 		query.join(falconAppointment.falconStaff, falconStaff);
 		query.join(falconAppointment.falconLocation, falconLocation);
 		query.join(falconAppointment.falconService, falconService);
 		query.orderBy(falconAppointment.appointmentDate.desc());
 		query.where(falconAppointment.appointmentDate.between(start, end));
 		return query.list(falconAppointment);
 	}
 
 	@Override
 	public List<FalconAppointment> listAppointmentsByParam(Integer staffId,
 			String patronId, Integer serviceId, Integer locationId,
 			Date searchDate) {
 		Date maxDate = null;
 		if(searchDate != null){
 			maxDate = DateUtils.addSeconds(DateUtils.addDays(searchDate, 1), -1);
 		}
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconAppointment falconAppointment = QFalconAppointment.falconAppointment;
 		final QFalconLocation falconLocation = QFalconLocation.falconLocation;
 		final QFalconService falconService = QFalconService.falconService;
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		final QFalconAppointmentPatron falconAppointmentPatron = QFalconAppointmentPatron.falconAppointmentPatron;
 		final QFalconPatron falconPatron = QFalconPatron.falconPatron;
 		query.from(falconAppointment);
 		List<BooleanExpression> expressions = new ArrayList<>();
		query.join(falconAppointment.falconAppointmentPatrons, falconAppointmentPatron).fetch();
		query.join(falconAppointmentPatron.falconPatron, falconPatron).fetch();
		query.orderBy(falconPatron.falconUserByPatron.name.asc());
 		if (searchDate != null) {
 			BooleanExpression x = falconAppointment.appointmentDate.between(searchDate, maxDate);
 			expressions.add(x);
 		}
 		if (staffId != null) {
 			query.join(falconAppointment.falconStaff, falconStaff).fetch();
 			BooleanExpression x = falconStaff.id.eq(staffId);
 			expressions.add(x);
 		}
 		if (patronId != null) {
 			BooleanExpression x = falconPatron.falconUserByPatron.username.eq(patronId);
 			expressions.add(x);
 		}
 		if (locationId != null) {
 			query.join(falconAppointment.falconLocation, falconLocation).fetch();
 			BooleanExpression x = falconAppointment.falconLocation.id.eq(locationId);
 			expressions.add(x);
 		}
 		if (serviceId != null) {
 			query.join(falconAppointment.falconService, falconService).fetch();
 			BooleanExpression x = falconService.id.eq(serviceId);
 			expressions.add(x);
 		}
 		query.where(expressions.toArray(new BooleanExpression[] {}));
 		return query.list(falconAppointment);
 	}
 }
