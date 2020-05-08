 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.service;
 
 import static com.google.common.collect.Iterables.limit;
 import static com.google.common.collect.Iterables.skip;
 import static com.google.common.collect.Lists.newArrayList;
 import static java.lang.String.format;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.repo.PhysicalPortRepo;
 import nl.surfnet.bod.web.security.RichUserDetails;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
 import org.springframework.stereotype.Service;
 import org.springframework.util.StringUtils;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 
 /**
  * Service implementation which combines {@link PhysicalPort}s.
  *
  * The {@link PhysicalPort}s found in the {@link NbiPortService} are leading and
  * when more data is available in our repository they will be enriched.
  *
  * Since {@link PhysicalPort}s from the {@link NbiPortService} are considered
  * readonly, the methods that change data are performed using the
  * {@link PhysicalPortRepo}.
  *
  *
  * @author Frank Mölder ($Author$)
  * @version $Revision$ $Date$
  */
 @Service
 public class PhysicalPortServiceImpl implements PhysicalPortService {
 
   @Autowired
   private PhysicalPortRepo physicalPortRepo;
 
   @Autowired
   @Qualifier("nbiClient")
   private NbiPortService nbiPortService;
 
   /**
    * Finds all ports using the North Bound Interface and enhances these ports
    * with data found in our own database.
    */
   @Override
   public List<PhysicalPort> findAll() {
     List<PhysicalPort> nbiPorts = nbiPortService.findAll();
     List<PhysicalPort> repoPorts = physicalPortRepo.findAll();
 
     enrichPorts(nbiPorts, repoPorts);
 
     return nbiPorts;
   }
 
   @Override
   public List<PhysicalPort> findEntries(int firstResult, int sizeNo) {
     return limitPorts(findAll(), firstResult, sizeNo);
   }
 
   @Override
   public Collection<PhysicalPort> findUnallocatedEntries(int firstResult, int sizeNo) {
     return limitPorts(findUnallocated(), firstResult, sizeNo);
   }
 
   private List<PhysicalPort> limitPorts(Collection<PhysicalPort> ports, int firstResult, int sizeNo) {
     return newArrayList(limit(skip(ports, firstResult), sizeNo));
   }
 
   @Override
   public Collection<PhysicalPort> findUnallocated() {
     List<PhysicalPort> allPorts = findAll();
 
     return Collections2.filter(allPorts, new Predicate<PhysicalPort>() {
       @Override
       public boolean apply(PhysicalPort input) {
         return input.getId() == null;
       }
     });
   }
 
   @Override
   public long count() {
     return nbiPortService.count();
   }
 
   @Override
   public long countUnallocated() {
     return nbiPortService.count() - physicalPortRepo.count();
   }
 
   @Override
   public PhysicalPort findByName(final String name) {
     PhysicalPort nbiPort = nbiPortService.findByName(name);
     PhysicalPort repoPort = physicalPortRepo.findByName(name);
 
     enrichPortWithPort(nbiPort, repoPort);
 
     return nbiPort;
   }
 
   @Override
   public void delete(final PhysicalPort physicalPort) {
     physicalPortRepo.delete(physicalPort);
   }
 
   @Override
   public PhysicalPort find(final Long id) {
     return physicalPortRepo.findOne(id);
   }
 
   @Override
   public void save(final PhysicalPort physicalPort) {
     physicalPortRepo.save(physicalPort);
 
   }
 
   @Override
   public PhysicalPort update(final PhysicalPort physicalPort) {
     return physicalPortRepo.save(physicalPort);
   }
 
   /**
    * Adds data found in given ports to the specified ports, enriches them.
    *
    * @param portsToEnrich
    *          {@link PhysicalPort}s to add the data to
    * @param dataPorts
    *          {@link PhysicalPort}s containing additional data
    */
   private void enrichPorts(final List<PhysicalPort> portsToEnrich, final List<PhysicalPort> dataPorts) {
     // Iterate of the nbiPorts and find matching repoPort
     for (final PhysicalPort portToEnrich : portsToEnrich) {
 
       Collection<PhysicalPort> matchingPorts = Collections2.filter(dataPorts, new Predicate<PhysicalPort>() {
         @Override
         public boolean apply(final PhysicalPort port) {
           return StringUtils.hasText(portToEnrich.getName()) && portToEnrich.getName().equals(port.getName());
         };
       });
 
       if (!matchingPorts.isEmpty()) {
         if (matchingPorts.size() == 1) {
           enrichPortWithPort(portToEnrich, matchingPorts.iterator().next());
         }
         else {
           throw new IllegalStateException(format("Name is not unique. Found [%s] ports with name: %s",
               matchingPorts.size(), portToEnrich.getName()));
         }
       }
     }
   }
 
   @Override
   public Collection<PhysicalPort> findAllocatedForUser(final RichUserDetails user) {
     if (user.getUserGroups().isEmpty()) {
       return Collections.emptyList();
     }
 
     Specification<PhysicalPort> forUserSpecification = new Specification<PhysicalPort>() {
       @Override
      public javax.persistence.criteria.Predicate toPredicate(
          Root<PhysicalPort> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
         return cb.and(root.get("physicalResourceGroup").get("adminGroup").in(user.getUserGroupIds()));
       }
     };
 
     return physicalPortRepo.findAll(forUserSpecification);
   }
 
   /**
    * Enriches the port with additional data.
    *
    * Clones JPA attributes (id and version), so a find will return these
    * preventing a additional save instead of an update.
    *
    * @param portToEnrich
    *          The port to enrich
    * @param dataPort
    *          The data to enrich with.
    */
   private void enrichPortWithPort(final PhysicalPort portToEnrich, final PhysicalPort dataPort) {
     if (portToEnrich == null || dataPort == null) {
       return;
     }
     portToEnrich.setPhysicalResourceGroup(dataPort.getPhysicalResourceGroup());
 
     portToEnrich.setId(dataPort.getId());
     portToEnrich.setVersion(dataPort.getVersion());
   }
 
 }
