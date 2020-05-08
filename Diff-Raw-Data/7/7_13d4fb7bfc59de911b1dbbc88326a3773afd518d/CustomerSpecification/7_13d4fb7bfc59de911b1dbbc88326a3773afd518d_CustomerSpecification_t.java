 package jp.rejaxon.spring.domain.business.customer;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import jp.rejaxon.spring.domain.entity.Customer;
 import jp.rejaxon.spring.domain_framework.jpa.criteria.WhereBuilder;
 import jp.rejaxon.spring.domain_framework.jpa.criteria.WhereBuilder.LikeType;
 import jp.rejaxon.spring.lib.util.ObjectUtl;
 import jp.rejaxon.spring.lib.util.StringUtl;
 
 import org.springframework.data.jpa.domain.Specification;
 
 public class CustomerSpecification {
 	
 	public static Specification<Customer> search(final CustomerCriteria criteria) {
 		return new Specification<Customer>() {
 			@Override
 			public Predicate toPredicate(Root<Customer> root,
 					CriteriaQuery<?> query, CriteriaBuilder cb) {
 				
 				WhereBuilder<Customer> predicates = new WhereBuilder<>(cb, root);
				predicates.addEqual("mailAddress", criteria.getMailAddress())
 					.addEqual("birthday", criteria.getBirthday())
 					.addLike("firstName", criteria.getFirstName(), LikeType.PART)
 					.addLike("lastName", criteria.getLastName(), LikeType.PART)
 					.addBetweenDate("createDatetime", criteria.getCreateDatetimeFrom(), criteria.getCreateDatetimeTo());
 					
 				
 				if (ObjectUtl.isFill(criteria.getTel1(), criteria.getTel2(), criteria.getTel3())) {
 					String str = StringUtl.concatWithSep("-", criteria.getTel1(), criteria.getTel2(), criteria.getTel3());
 					predicates.add(
 							cb.equal(root.get("tel").as(String.class), str));
 				}
 				
				return predicates.build();
 			}
 		};
 	}
 }
