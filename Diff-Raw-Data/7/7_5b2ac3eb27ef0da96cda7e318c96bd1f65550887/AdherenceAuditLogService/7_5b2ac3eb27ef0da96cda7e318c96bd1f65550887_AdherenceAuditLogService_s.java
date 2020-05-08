 package org.motechproject.whp.reports.service;
 
 import org.motechproject.whp.reports.contract.adherence.AdherenceAuditLogDTO;
 import org.motechproject.whp.reports.domain.adherence.AdherenceAuditLog;
 import org.motechproject.whp.reports.mapper.AdherenceAuditLogMapper;
 import org.motechproject.whp.reports.repository.AdherenceAuditLogRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 @Transactional
 public class AdherenceAuditLogService {
 
    private final AdherenceAuditLogMapper adherenceAuditLogMapper;
    private final AdherenceAuditLogRepository adherenceAuditLogRepository;
 
     @Autowired
     public AdherenceAuditLogService(AdherenceAuditLogMapper adherenceAuditLogMapper, AdherenceAuditLogRepository adherenceAuditLogRepository) {
         this.adherenceAuditLogMapper = adherenceAuditLogMapper;
         this.adherenceAuditLogRepository = adherenceAuditLogRepository;
     }
 
 
     public void save(AdherenceAuditLogDTO adherenceAuditLogDTO) {
         AdherenceAuditLog auditLog = adherenceAuditLogMapper.map(adherenceAuditLogDTO);
         adherenceAuditLogRepository.save(auditLog);
     }
 }
