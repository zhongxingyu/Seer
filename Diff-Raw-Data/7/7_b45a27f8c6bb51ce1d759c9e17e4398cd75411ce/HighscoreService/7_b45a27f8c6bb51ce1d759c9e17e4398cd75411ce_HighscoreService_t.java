 package no.kantega.frisktblodtilhodet.service;
 
 import no.kantega.frisktblodtilhodet.model.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Component;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.*;
 import java.util.*;
 
 @Component
 public class HighscoreService {
 
     @Autowired
     private AktivitetRepository aktivitetRepository;
     
     @Autowired
     private GruppeRepository gruppeRepository;
 
     @Autowired
     private PeriodeRepository periodeRepository;
 
     @PersistenceContext
     private EntityManager entityManager;
 
     public Map<Aktivitet, Integer> getAktivitetAndCountForPerson(Person person) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> cq = cb.createQuery();
         Root<UtfortAktivitet> from = cq.from(UtfortAktivitet.class);
 
         Expression<Integer> sum = cb.sum(from.get(UtfortAktivitet_.poeng));
         Path<Person> p = from.get(UtfortAktivitet_.person);
         Path<Aktivitet> aktivitet = from.get(UtfortAktivitet_.aktivitet);
 
         Predicate utfortPersonEqPerson = cb.equal(p, person);
         Predicate and = getlatestPeriodPredicate(cb, from, utfortPersonEqPerson);
 
         CriteriaQuery<Object> multiselect = cq.multiselect(sum, aktivitet).where(and).orderBy(cb.desc(sum), cb.asc(aktivitet.get(Aktivitet_.name))).groupBy(aktivitet);
 
         TypedQuery<Object> query = entityManager.createQuery(multiselect);
         List<Object> resultList = query.getResultList();
 
         Map<Aktivitet, Integer> aktivitetAndCountByPerson = new LinkedHashMap<Aktivitet, Integer>();
         for (Object o : resultList) {
             Object[] result = (Object[]) o;
             aktivitetAndCountByPerson.put((Aktivitet)result[1], (Integer)result[0]);
         }
 
         addNotPresentAktivitetAndZeroCount(aktivitetAndCountByPerson);
         return aktivitetAndCountByPerson;
     }
 
     private Predicate getlatestPeriodPredicate(CriteriaBuilder cb, Root<UtfortAktivitet> from, Predicate additionalPredicate) {
         Periode sistePeriode = periodeRepository.findLatestPeriode();
 
         Calendar start = new GregorianCalendar();
         start.setTime(sistePeriode.getStartdato());
         Calendar end = new GregorianCalendar();
         end.setTime(sistePeriode.getStopdato());
         Predicate timeGtStart = cb.greaterThan(from.get(UtfortAktivitet_.time), start);
         Predicate timeLtStio= cb.lessThan(from.get(UtfortAktivitet_.time), end);
 
         if (additionalPredicate != null) {
             return cb.and(additionalPredicate, timeGtStart, timeLtStio);
         } else {
             return cb.and(timeGtStart, timeLtStio);
         }
     }
 
     private void addNotPresentAktivitetAndZeroCount(Map<Aktivitet, Integer> aktivitetAndCountByPerson) {
         for(Aktivitet aktivitet : aktivitetRepository.findAll(new Sort(new Sort.Order(Aktivitet_.name.getName())))){
             boolean notPresent = !aktivitetAndCountByPerson.containsKey(aktivitet);
             if(notPresent){
                 aktivitetAndCountByPerson.put(aktivitet, 0);
             }
         }
     }
 
     public SortedMap<Person, Integer> getPersonAndScore() {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> query = cb.createQuery();
         Root<UtfortAktivitet> from = query.from(UtfortAktivitet.class);
         Expression<Integer> sum = cb.sum(from.get(UtfortAktivitet_.poeng));
         Path<Person> person = from.get(UtfortAktivitet_.person);
 
         Predicate and = getlatestPeriodPredicate(cb, from, null);
         CriteriaQuery<Object> select = query.multiselect(sum, person).where(and).orderBy(cb.desc(sum)).groupBy(person);
 
         return addResultToMap(select);
     }
 
     public SortedMap<Person, Integer> getPersonsAndScoreForAktivitet(Aktivitet aktivitet) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> query = cb.createQuery();
         Root<UtfortAktivitet> from = query.from(UtfortAktivitet.class);
         Expression<Integer> sum = cb.sum(from.get(UtfortAktivitet_.poeng));
         Path<Person> person = from.get(UtfortAktivitet_.person);
         Path<Aktivitet> aktivitetPath = from.get(UtfortAktivitet_.aktivitet);
 
         Predicate utfortAktivitetEquals = cb.equal(aktivitetPath, aktivitet);
         Predicate predicate = getlatestPeriodPredicate(cb, from, utfortAktivitetEquals);
         CriteriaQuery<Object> q = query.multiselect(sum, person).where(predicate).orderBy(cb.desc(sum)).groupBy(person);
         return addResultToMap(q);
     }
 
     private SortedMap<Person, Integer> addResultToMap(CriteriaQuery<Object> q) {
         TypedQuery<Object> query1 = entityManager.createQuery(q);
         List<Object> resultList = query1.getResultList();
         Map<Person, Integer> personAndCount = new LinkedHashMap<Person, Integer>();
         for (Object o : resultList) {
             Object[] result = (Object[]) o;
             Integer count = (Integer) result[0];
             Person p = (Person) result[1];
             personAndCount.put(p, count);
         }
         return getSortedPersonMap(personAndCount);
     }
 
     public SortedMap<Person, Integer> getPersonsAndScoreForGruppe(Gruppe gruppe) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> query = cb.createQuery();
         Root<UtfortAktivitet> from = query.from(UtfortAktivitet.class);
         Root<Person> personfrom = query.from(Person.class);
         Expression<Integer> sum = cb.sum(from.get(UtfortAktivitet_.poeng));
         Path<Person> person = from.get(UtfortAktivitet_.person);
 
         Predicate utfortPersonGruppeEqGruppe = cb.equal(personfrom.get(Person_.gruppe), gruppe);
         Predicate predicate = getlatestPeriodPredicate(cb, from, utfortPersonGruppeEqGruppe);
         CriteriaQuery<Object> q = query.multiselect(sum, person).where(predicate).orderBy(cb.desc(sum)).groupBy(person);
 
         return addResultToMap(q);
     }
 
     public SortedMap<Person, Integer> getPersonsAndScoreForGruppeAndAktivitet(Gruppe gruppe, Aktivitet aktivitet) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> query = cb.createQuery();
         Root<UtfortAktivitet> from = query.from(UtfortAktivitet.class);
         Root<Person> personfrom = query.from(Person.class);
         Expression<Integer> sum = cb.sum(from.get(UtfortAktivitet_.poeng));
         Path<Person> person = from.get(UtfortAktivitet_.person);
 
         Predicate and = cb.and(cb.equal(personfrom.get(Person_.gruppe), gruppe), cb.equal(from.get(UtfortAktivitet_.aktivitet), aktivitet));
         Predicate predicate = getlatestPeriodPredicate(cb, from, and);
         CriteriaQuery<Object> q = query.multiselect(sum, person).where(predicate).orderBy(cb.desc(sum)).groupBy(person);
 
         return addResultToMap(q);
     }
 
     public SortedMap<Gruppe, Double> getGrupperAndScoreForAktivitet(Aktivitet aktivitet) {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> query = cb.createQuery();
 
         Root<UtfortAktivitet> from = query.from(UtfortAktivitet.class);
         final Map<Gruppe, Double> avg = new HashMap<Gruppe, Double>();
         for (Gruppe gruppe : gruppeRepository.findAllLeafGrupper()){
             int groupSum = 0;
             for (Person person : gruppe.getPersons()) {
                 Predicate predicate = getlatestPeriodPredicate(cb, from, cb.and(cb.equal(from.get(UtfortAktivitet_.person), person), cb.equal(from.get(UtfortAktivitet_.aktivitet), aktivitet)));
 
                 CriteriaQuery<Object> selectSum = query.select(cb.sum(from.get(UtfortAktivitet_.poeng))).where(predicate);
 
                 TypedQuery<Object> query1 = entityManager.createQuery(selectSum);
                 Integer personSum = (Integer) query1.getSingleResult();
                 if (personSum != null) {
                     groupSum += personSum;
                 }
             }
             avg.put(gruppe, (double)groupSum / (double)gruppe.getPersons().size());
         }
 
         return getSortedGruppeMap(avg);
     }
 
     public SortedMap<Gruppe, Double> getGrupperAndScoreForAlle() {
         CriteriaBuilder cb = entityManager.getCriteriaBuilder();
         CriteriaQuery<Object> query = cb.createQuery();
 
         Root<UtfortAktivitet> from = query.from(UtfortAktivitet.class);
         final Map<Gruppe, Double> avg = new HashMap<Gruppe, Double>();
         for (Gruppe gruppe : gruppeRepository.findAllLeafGrupper()){
             int groupSum = 0;
             for (Person person : gruppe.getPersons()) {
                 Predicate equal = cb.equal(from.get(UtfortAktivitet_.person), person);
                 Predicate predicate = getlatestPeriodPredicate(cb, from, equal);
 
                 CriteriaQuery<Object> selectSum = query.select(cb.sum(from.get(UtfortAktivitet_.poeng))).where(predicate);
                 TypedQuery<Object> query1 = entityManager.createQuery(selectSum);
                 Integer personSum = (Integer) query1.getSingleResult();
                 if (personSum != null) {
                     groupSum += personSum;
                 }
             }
             avg.put(gruppe, (double)groupSum / (double)gruppe.getPersons().size());
         }
 
         return getSortedGruppeMap(avg);
     }
 
     private TreeMap<Gruppe, Double> getSortedGruppeMap(final Map<Gruppe, Double> avg) {
         TreeMap<Gruppe, Double> gruppeDoubleTreeMap = new TreeMap<Gruppe, Double>(new Comparator<Gruppe>() {
             @Override
             public int compare(Gruppe o1, Gruppe o2) {
                 Double v1 = avg.get(o1);
                 Double v2 = avg.get(o2);
                 return v2.compareTo(v1);
             }
         });
         gruppeDoubleTreeMap.putAll(avg);
         return gruppeDoubleTreeMap;
     }
 
     private SortedMap<Person, Integer> getSortedPersonMap(final Map<Person, Integer> personAndCount) {
         SortedMap<Person, Integer> sortedMap = new TreeMap<Person, Integer>(new Comparator<Person>() {
             @Override
             public int compare(Person o1, Person o2) {
                Integer integer = personAndCount.get(o2);
                int i = integer.compareTo(personAndCount.get(o1));
                if(i == 0){ // If 0 is returned, only the first is in the sorted map.
                    i = -1;
                }
                return i;
             }
         });
         sortedMap.putAll(personAndCount);
         return sortedMap;
     }
 }
