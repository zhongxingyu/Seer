 /*
  *	http://www.jrecruiter.org
  *
  *	Disclaimer of Warranty.
  *
  *	Unless required by applicable law or agreed to in writing, Licensor provides
  *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  *	including, without limitation, any warranties or conditions of TITLE,
  *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
  *	solely responsible for determining the appropriateness of using or
  *	redistributing the Work and assume any risks associated with Your exercise of
  *	permissions under this License.
  *
  */
 package org.jrecruiter.service.migration.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.sql.DataSource;
 
 import org.jasypt.digest.StringDigester;
 import org.jrecruiter.common.Constants.JobStatus;
 import org.jrecruiter.dao.IndustryDao;
 import org.jrecruiter.dao.JobDao;
 import org.jrecruiter.dao.RegionDao;
 import org.jrecruiter.dao.UserDao;
 import org.jrecruiter.model.Industry;
 import org.jrecruiter.model.Job;
 import org.jrecruiter.model.Region;
 import org.jrecruiter.model.Statistic;
 import org.jrecruiter.model.User;
 import org.jrecruiter.service.JobService;
 import org.jrecruiter.service.migration.MigrationService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * @author Gunnar Hillert
 * @version $Id$
  */
 @Service("migrationService")
 @Transactional
 public class MigrationServiceImpl implements MigrationService {
 
 	private SimpleJdbcTemplate jdbcTemplateV1;
 	private SimpleJdbcTemplate jdbcTemplate;
 
 	private @Autowired StringDigester stringDigester;
 	private @Autowired UserDao        userDao;
 	private @Autowired JobDao         jobDao;
 	private @Autowired IndustryDao    industryDao;
 	private @Autowired RegionDao      regionDao;
 	private @Autowired JobService     jobService;
 	
 	protected @PersistenceContext(unitName="base") EntityManager entityManager;
 	
 	@Autowired
 	public void setDataSourceV1(@Qualifier("dataSourceV1") final DataSource dataSourceV1) {
 		this.jdbcTemplateV1 = new SimpleJdbcTemplate(dataSourceV1);
 	}
 
 	@Autowired
 	public void setDataSource(@Qualifier("dataSource") final DataSource dataSource) {
 		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
 	}
 
 	/**
 	 * Logger Declaration.
 	 */
 	private final static Logger LOGGER = LoggerFactory.getLogger(MigrationServiceImpl.class);
 
 	@Override
 	public void migrateIndustries() {
 		// TODO Auto-generated method stub	
 	}
 
 	@Override
 	public void migrateRegions() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void migrateSettings() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	@Transactional
 	public void migrateUserData(final Boolean digestPasswords) {
 		String sql = "select first_name, last_name, user_name, user_passwd, " +
 		"phone, fax, email, register_date, expire_date, update_date, company from users where users.user_name <> 'admin'";
 
 		ParameterizedRowMapper<User> mapper = new ParameterizedRowMapper<User>() {
 
 			// notice the return type with respect to Java 5 covariant return types
 			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
 				User user = new User();
 				user.setFirstName(rs.getString("first_name"));
 				user.setLastName(rs.getString("last_name"));
 
 				user.setUsername(rs.getString("user_name"));
 				user.setPassword(rs.getString("user_passwd"));
 
 				user.setPhone(rs.getString("phone"));
 				user.setFax(rs.getString("fax"));
 				user.setEmail(rs.getString("email"));
 
 				user.setRegistrationDate(rs.getTimestamp("register_date"));
 				user.setUpdateDate(rs.getTimestamp("update_date"));
 				user.setCompany(rs.getString("company"));
 
 				return user;
 			}
 		};
 
 		final List<User> users = this.jdbcTemplateV1.query(sql, mapper);
 
 		if (digestPasswords) {
 		
 			int counter = 0;
 			for (User user : users) {
 				LOGGER.info("[" +  ++counter  +"/" + users.size() + "] User: " + user + "...digesting password.");
 				user.setPassword(stringDigester.digest(user.getPassword()));
 			}
 
 		}	
 
 		int counter = 0;
 		for (User user : users) {
 			LOGGER.info("[" +  ++counter  +"/" + users.size() + "] User: " + user + "...saving.");
 		  //  if ( counter % 20 == 0 ) { //20, same as the JDBC batch size
 		        //flush a batch of inserts and release memory:
 		    	userDao.save(user);
 		        entityManager.flush();
 		    //    entityManager.clear();
 		   // }
 		}
 		
 		LOGGER.info("Total number of records saved: " + users.size());
 
 	}
 
 	@Override
 	@Transactional
 	public void migrateJobData() {
 		
 		jobService.updateJobCountPerDays(); 
 		
 		LOGGER.info("Migrating jobs...");
 
 		userDao.getAll();
 		
 		String sql = "SELECT job_id, business_name, business_location, job_title, salary, " +
 		"description, web_site, business_address1, business_address2, " +
 		"business_city, business_state, business_zip, business_phone, " +
 		"business_email, industry, job_restrictions, register_date, expire_date, " +
 		"update_date, status, user_name " +
 		"FROM jobs order by register_date asc;";
 
 		ParameterizedRowMapper<Job> mapper = new ParameterizedRowMapper<Job>() {
 
 			// notice the return type with respect to Java 5 covariant return types
 			public Job mapRow(ResultSet rs, int rowNum) throws SQLException {
 				Job job = new Job();
 				
 				job.setId(rs.getLong("job_id"));
 				job.setBusinessName(rs.getString("business_name"));
 				job.setBusinessCity(rs.getString("business_location"));
 
 				job.setJobTitle(rs.getString("job_title"));
 				job.setSalary(rs.getBigDecimal("salary"));
 
 				job.setDescription(rs.getString("description"));
 				job.setWebsite(rs.getString("web_site"));
 				job.setBusinessAddress1(rs.getString("business_address1"));
 				job.setBusinessAddress2(rs.getString("business_address2"));
 
 				job.setBusinessCity(rs.getString("business_city"));
 				job.setBusinessState(rs.getString("business_state"));
 				job.setBusinessZip(rs.getString("business_zip"));
 				job.setBusinessPhone(rs.getString("business_phone"));
 				job.setBusinessEmail(rs.getString("business_email"));
 				job.setIndustryOther(rs.getString("industry"));
 				job.setJobRestrictions(rs.getString("job_restrictions"));
 				job.setRegistrationDate(rs.getDate("register_date"));
 				job.setUpdateDate(rs.getDate("update_date"));
 				job.setStatus(JobStatus.ACTIVE);
 				job.setUsesMap(Boolean.FALSE);
 
 				final User user = userDao.getUser(rs.getString("user_name"));
 				LOGGER.info("Fetching user: " + rs.getString("user_name") + ".");
 				if (user == null) {
 					throw new IllegalStateException("No user found for user name: " + rs.getString("user_name"));
 				}
 
 				job.setUser(user);
 
 				return job;
 			}
 		};
 
 		final List<Job> jobs = this.jdbcTemplateV1.query(sql, mapper);
 
 		
 		//Populating statistics
 		
 		int counterForStatistics = 0;
 		for (Job job : jobs) {
 			Statistic statistic = this.getStatistic(job.getId());
 			LOGGER.info("[" +  ++counterForStatistics  +"/" + jobs.size() + "] Old  Id: " + job.getId() + "...assigning statistics: " + statistic);
 			if (statistic == null) {
 				throw new IllegalStateException("Statistic should not be null.");
 			}
 			statistic.setJob(job);
 			job.setStatistic(statistic);
 		}	
 		
 		int counter = 0;
 		
 		Industry otherIndustry = industryDao.get(-1L);
 		Region   otherRegion   = regionDao.get(-1L);
 		
 		for (Job job : jobs) {
 			LOGGER.info("[" +  ++counter  +"/" + jobs.size() + "] Job: " + job + ".");
 			
 			final Job newJob = new Job();
 			newJob.setBusinessAddress1(job.getBusinessAddress1());
 			newJob.setBusinessAddress2(job.getBusinessAddress2());
 			newJob.setBusinessCity(job.getBusinessCity());
 			newJob.setBusinessEmail(job.getBusinessEmail());
 			newJob.setBusinessName(job.getBusinessName());
 			newJob.setBusinessPhone(job.getBusinessPhone());
 			newJob.setBusinessState(job.getBusinessState());
 			newJob.setBusinessZip(job.getBusinessZip());
 			newJob.setDescription(job.getDescription());
 			newJob.setIndustry(otherIndustry);
 			newJob.setIndustryOther(job.getIndustryOther());
 			newJob.setJobRestrictions(job.getJobRestrictions());
 			newJob.setJobTitle(job.getJobTitle());
 			newJob.setSalary(job.getSalary());
 
 			
 			newJob.setStatus(JobStatus.ACTIVE);
 			
 			if (job.getRegistrationDate() != null) {
 				newJob.setRegistrationDate(job.getRegistrationDate());
 			} else if (job.getUpdateDate() != null) {
 				newJob.setRegistrationDate(job.getUpdateDate());
 		    } else {
 		    	throw new IllegalStateException("Both Registration Date and Update Date are null for Job: " + job);
 		    }
 		
 			newJob.setUpdateDate(job.getUpdateDate());
 			newJob.setUser(job.getUser());
 			newJob.setUsesMap(Boolean.FALSE);
 			newJob.setWebsite(job.getWebsite());
 			
 			Statistic newStatistic = new Statistic();
 			newStatistic.setJob(newJob);
 			newStatistic.setCounter(job.getStatistic().getCounter());
 			newStatistic.setLastAccess(job.getStatistic().getLastAccess());
 			newStatistic.setUniqueVisits(job.getStatistic().getUniqueVisits());
 			
 			newJob.setStatistic(newStatistic);
 			
 			jobService.addJob(newJob);
 			
 		}
 		
 		entityManager.flush();
 		LOGGER.info("Total number of jobs migrated: " + jobs.size());
 
 	}
 
 	
 	private Statistic getStatistic(Long oldJobId) {
 		
 		String sql = "select counter, last_access, unique_visits " +
 		"from statistics where statistics.job_id = ?";
 
 		ParameterizedRowMapper<Statistic> mapper = new ParameterizedRowMapper<Statistic>() {
 
 			// notice the return type with respect to Java 5 covariant return types
 			public Statistic mapRow(ResultSet rs, int rowNum) throws SQLException {
 				Statistic statistic = new Statistic();
 				statistic.setCounter(rs.getLong("counter"));
 				statistic.setLastAccess(rs.getTimestamp("last_access"));
 				statistic.setUniqueVisits(rs.getLong("unique_visits"));
 
 				return statistic;
 			}
 		};
 
 		return this.jdbcTemplateV1.queryForObject(sql, mapper, oldJobId);
 
 		
 	}
 	
 }
