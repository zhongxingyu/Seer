 package org.sukrupa.student;
 
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.*;
 import org.joda.time.LocalDate;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository
 class StudentsSearchCriteriaGenerator {
     private static final String ID = "id";
     private final SessionFactory sessionFactory;
 
     private static final String STUDENT_CLASS = "studentClass";
     private static final String GENDER = "gender";
     private static final String CASTE = "caste";
     private static final String COMMUNITY_LOCATION = "communityLocation";
     private static final String NAME = "name";
     private static final String DATE_OF_BIRTH = "dateOfBirth";
 
     private static final String TALENTS = "talents";
     private static final String RELIGION = "religion";
     private static final String DESCRIPTION = "description";
     private static final String STATUS = "status";
     private static final String OCCUPATION = "occupation";
     private static final String FATHER = "father";
     private static final String MOTHER = "mother";
     private static final String GUARDIAN = "guardian";
     private static final String FAMILY_STATUS = "familyStatus";
     private static final String SPONSOR = "sponsor";
 
     @Autowired
     public StudentsSearchCriteriaGenerator(SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     public Criteria createOrderedCriteriaFrom(StudentSearchParameter searchParam) {
         return addOrderCriteria(generateSearchCriteria(searchParam));
     }
 
     public Criteria createCountCriteriaBasedOn(StudentSearchParameter searchParam) {
         return generateSearchCriteria(searchParam).setProjection(Projections.rowCount());
     }
 
     private Criteria generateSearchCriteria(StudentSearchParameter searchParam) {
         Conjunction conjunction = createConjunction(searchParam.getName(), searchParam.getStudentClass(), searchParam.getGender(),
                 searchParam.getCaste(), searchParam.getCommunityLocation(), searchParam.getReligion());
         if (!StudentSearchParameter.WILDCARD_CHARACTER.equals(searchParam.getAgeFrom())) {
             addAgeCriteria(Integer.parseInt(searchParam.getAgeFrom()), Integer.parseInt(searchParam.getAgeTo()), conjunction);
         }
 
         Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Student.class);
         criteria.add(conjunction);
 
         addTalentsSearchCriteria(criteria, searchParam.getTalents());
         addCaregiversOccupationSearchCriteria(criteria, searchParam.getCaregiversOccupation());
         addStudentStatusSearchCriteria(criteria, StudentStatus.fromString(searchParam.getStatus()));
         addStudentFamilyStatusSearchCriteria(criteria, searchParam.getFamilyStatus());
         addSponsoredSearchCriteria(criteria, searchParam.getSponsored(), searchParam.getSponsorName());
 
         return criteria;
     }
 
     private void addSponsoredSearchCriteria(Criteria criteria, String sponsored, String sponsorName) {
         if (!sponsored.equals(StudentSearchParameter.WILDCARD_CHARACTER)){
             if(sponsored.equals("Yes")){
                 if(sponsorName.equals("")){
                    criteria.add(Restrictions.and(Restrictions.isNotNull(SPONSOR),Restrictions.ne(SPONSOR,sponsorName)));
                 }
                 else {
                     criteria.add(Restrictions.eq(SPONSOR, sponsorName));
                 }
             }
             else {
                criteria.add(Restrictions.or(Restrictions.isNull(SPONSOR),Restrictions.eq(SPONSOR,"")));
             }
         }
     }
 
     private void addStudentFamilyStatusSearchCriteria(Criteria criteria, String studentFamilyStatus) {
         if (!studentFamilyStatus.equals(StudentSearchParameter.WILDCARD_CHARACTER)){
             if (studentFamilyStatus.isEmpty()) {
                 criteria.add(Restrictions.isNull(FAMILY_STATUS));
             } else {
                 criteria.add(Restrictions.eq(FAMILY_STATUS, StudentFamilyStatus.fromString(studentFamilyStatus)));
             }
         }
     }
 
     private void addAgeCriteria(int ageFrom, int ageTo, Conjunction conjunction) {
         LocalDate birthDateFrom = computeBirthDateFromAge(ageFrom);
         LocalDate birthDateTo = computeBirthDateFromAge(getInclusiveUpperBoundAge(ageTo));
         conjunction.add(Restrictions.between(DATE_OF_BIRTH, birthDateTo, birthDateFrom));
     }
 
     private int getInclusiveUpperBoundAge(int ageTo) {
         return ageTo + 1;
     }
 
     private void addTalentsSearchCriteria(Criteria criteria, List<Talent> talents) {
         if (talents.isEmpty()) {
             return;
         }
 
         List<String> descriptions = new ArrayList<String>();
         for (Talent talent : talents) {
             descriptions.add(talent.getDescription());
         }
         criteria.createCriteria(TALENTS).add(Restrictions.in(DESCRIPTION, descriptions)).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
     }
 
     private void addCaregiversOccupationSearchCriteria(Criteria criteria, String caregiversOccupation) {
         if (!caregiversOccupation.equals("*")) {
             criteria.createAlias("father","fa");
             criteria.createAlias("mother","ma");
             criteria.createAlias("guardian","ga");
 
             SimpleExpression fatherRestrictions = Restrictions.eq("fa.occupation",caregiversOccupation);
             SimpleExpression fatherNotDeceased = Restrictions.ne("fa.maritalStatus", "Deceased");
             LogicalExpression fatherIsAlive = Restrictions.and(fatherRestrictions, fatherNotDeceased);
 
             SimpleExpression motherRestrictions = Restrictions.eq("ma.occupation", caregiversOccupation);
             SimpleExpression motherNotDeceased = Restrictions.ne("ma.maritalStatus", "Deceased");
             LogicalExpression motherIsAlive = Restrictions.and(motherRestrictions, motherNotDeceased);
 
             SimpleExpression guardianRestrictions = Restrictions.eq("ga.occupation", caregiversOccupation);
             SimpleExpression guardianNotDeceased = Restrictions.ne("ga.maritalStatus", "Deceased");
             LogicalExpression guardianIsAlive = Restrictions.and(guardianRestrictions, guardianNotDeceased);
 
             criteria.add( Restrictions.or( Restrictions.or( fatherIsAlive , motherIsAlive ), guardianIsAlive));
         }
     }
 
     private void addStudentStatusSearchCriteria(Criteria criteria, StudentStatus status) {
         criteria.add(Restrictions.eq(STATUS, status));
     }
 
     private LocalDate computeBirthDateFromAge(int age) {
         return new LocalDate().minusYears(age);
     }
 
     private Criteria addOrderCriteria(Criteria criteria) {
         return criteria.addOrder(Order.asc(NAME).ignoreCase());
     }
 
     private Conjunction createConjunction(String name, String studentClass, String gender, String caste, String communityLocation, String religion) {
         Conjunction conjunction = Restrictions.conjunction();
 
         addContainsRestrictionIfNotWildcard(NAME, name, conjunction);
         addEqualsRestrictionIfNotWildcard(STUDENT_CLASS, studentClass, conjunction);
         addEqualsRestrictionIfNotWildcard(GENDER, gender, conjunction);
         addEqualsRestrictionIfNotWildcard(CASTE, caste, conjunction);
         addEqualsRestrictionIfNotWildcard(COMMUNITY_LOCATION, communityLocation, conjunction);
         addEqualsRestrictionIfNotWildcard(RELIGION, religion, conjunction);
         return conjunction;
     }
 
     private void addEqualsRestrictionIfNotWildcard(String field, String parameter, Conjunction conjunction) {
         if (!StudentSearchParameter.WILDCARD_CHARACTER.equals(parameter)) {
             SimpleExpression equalToParam = Restrictions.eq(field, parameter);
             if (parameter.isEmpty()) {
                 Disjunction disj = Restrictions.disjunction();
                 disj.add(Restrictions.isNull(field));
                 disj.add(equalToParam);
                 conjunction.add(disj);
             } else {
                 conjunction.add(equalToParam);
             }
         }
     }
 
     private void addEqualsRestrictionIfNotWildcardDisjunction(String field, String parameter, Disjunction disjunction) {
         if (!StudentSearchParameter.WILDCARD_CHARACTER.equals(parameter)) {
             SimpleExpression equalToParam = Restrictions.eq(field, parameter);
             if (parameter.isEmpty()) {
                 Disjunction disj = Restrictions.disjunction();
                 disj.add(Restrictions.isNull(field));
                 disj.add(equalToParam);
                 disjunction.add(disj);
             } else {
                 disjunction.add(equalToParam);
             }
         }
     }
 
 
     private void addContainsRestrictionIfNotWildcard(String field, String parameter, Conjunction conjunction) {
         if (!StudentSearchParameter.WILDCARD_CHARACTER.equals(parameter)) {
             SimpleExpression equalToParam = Restrictions.like(field, parameter + "%").ignoreCase();
             if (parameter.isEmpty()) {
                 Disjunction disj = Restrictions.disjunction();
                 disj.add(Restrictions.isNull(field));
                 disj.add(equalToParam);
                 conjunction.add(disj);
             } else {
                 conjunction.add(equalToParam);
             }
         }
     }
 }
