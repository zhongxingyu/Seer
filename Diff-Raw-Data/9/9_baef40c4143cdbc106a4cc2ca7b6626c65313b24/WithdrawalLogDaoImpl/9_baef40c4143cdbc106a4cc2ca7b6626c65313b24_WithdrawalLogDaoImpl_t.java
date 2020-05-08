 package com.smes.dao.hibernate;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import com.smes.dao.WithdrawalLogDao;
 import com.smes.domain.hibernate.Bank;
 import com.smes.domain.hibernate.WithdrawalLog;
 
 public class WithdrawalLogDaoImpl extends BaseDao<WithdrawalLog> implements WithdrawalLogDao{
 
 	@Override
 	protected Class<WithdrawalLog> getDomainClass() {
 		return WithdrawalLog.class;
 	}
 
 	@Override
 	public void deleteWithdrawal(int withdrawalLogID) {
 		delete(withdrawalLogID);
 	}
 
 	@Override
 	public WithdrawalLog getWithdrawal(int withdrawalLogId) {
 		return get(withdrawalLogId);
 	}
 
 	@Override
 	public Collection<WithdrawalLog> getWithdrawals(Date frm, Date to) {
 		DetachedCriteria criteria = 
 			DetachedCriteria.forClass(getDomainClass());
 		criteria.add(Restrictions.between("withdrawalDate", frm, to));
 		criteria.addOrder(Order.asc("withdrawalDate"));
 		return getAll(criteria);
 	}
 
 	@Override
 	public Collection<WithdrawalLog> getWithdrawals(Bank bank) {
 		return getWithdrawals(bank.getBankId());
 	}
 
 	@Override
 	public Collection<WithdrawalLog> getWithdrawals(int bankId) {
 		DetachedCriteria criteria = 
 			DetachedCriteria.forClass(getDomainClass());
 		criteria.add(Restrictions.eq("bankId", bankId));
		criteria.addOrder(Order.asc("withdrawalDate"));
 		return getAll(criteria);
 	}
 
 	@Override
 	public void persistObject(WithdrawalLog withdrawalLog) {
 		persist(withdrawalLog);
 	}
 
 	@Override
 	public void saveWithdrawal(WithdrawalLog withdrawalLog) {
 		save(withdrawalLog);
 	}
 	
 }
