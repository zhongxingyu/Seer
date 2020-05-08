 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.ithb.si.made.mtmgmt.core.util;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.MachineModelEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.MachineModelPartEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.MachineModelTotalizerEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.ServiceReportEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.ServiceReportSpbuMachineTotalizerEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.SpbuEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.SpbuMachineEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.SpbuMachinePartMttfEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.entity.SpbuMachineTotalizerEntity;
 import org.ithb.si.made.mtmgmt.core.persistence.repository.MachineModelPartRepository;
 import org.ithb.si.made.mtmgmt.core.persistence.repository.ServiceReportRepository;
 import org.ithb.si.made.mtmgmt.core.persistence.repository.ServiceReportSpbuMachineTotalizerRepository;
 import org.ithb.si.made.mtmgmt.core.persistence.repository.SpbuMachinePartMttfRepository;
 import org.ithb.si.made.mtmgmt.core.persistence.repository.SpbuMachineTotalizerRepository;
 import org.ithb.si.made.mtmgmt.core.persistence.repository.SpbuRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 /**
  *
  * @author Uyeee
  */
 @Component
 public class MaintenancePredictor {
 
 	private static final Logger LOG = LoggerFactory.getLogger(MaintenancePredictor.class);
 	private static final long UNIT_SECOND = 1000l;
 	private static final long UNIT_MINUTE = 60 * UNIT_SECOND;
 	private static final long UNIT_HOUR = 60 * UNIT_MINUTE;
 	private static final long UNIT_DAY = 24 * UNIT_HOUR;
 	private static final long TOTALIZER_PREDICT_PER_DAY = 17000;
 	@Autowired
 	private SpbuRepository spbuRepository;
 	@Autowired
 	private MachineModelPartRepository machineModelPartRepository;
 	@Autowired
 	private SpbuMachinePartMttfRepository machinePartMttfRepository;
 	@Autowired
 	private SpbuMachineTotalizerRepository spbuMachineTotalizerRepository;
 	@Autowired
 	private ServiceReportRepository serviceReportRepository;
 	@Autowired
 	private ServiceReportSpbuMachineTotalizerRepository serviceReportSpbuMachineTotalizerRepository;
 
 	@Transactional
 	public List<PredictionResult> getPredictions() {
 		final List<SpbuEntity> dbSpbuEntities = spbuRepository.findAll();
 		final List<PredictionResult> result = new LinkedList<>();
 
 		for (final SpbuEntity dbSpbuEntity : dbSpbuEntities) {
 			result.addAll(analyzeSpbu(dbSpbuEntity));
 		}
 		return result;
 	}
 
 	public List<PredictionResult> analyzeSpbu(SpbuEntity spbuEntity) {
 		final List<PredictionResult> result = new LinkedList<>();
 		for (final SpbuMachineEntity spbuMachineEntity : spbuEntity.getSpbuMachineEntityList()) {
 			analyzeSpbuMachine(result, spbuMachineEntity);
 		}
 		return result;
 	}
 
 	private void analyzeSpbuMachine(List<PredictionResult> result, SpbuMachineEntity spbuMachineEntity) {
 		final MachineModelEntity machineModelEntity = spbuMachineEntity.getMachineModelEntity();
 		for (final MachineModelPartEntity machineModelPartEntity : machineModelEntity.getMachineModelPartEntityList()) {
 			final ServiceReportEntity latestServiceReportEntity = getLatestServiceReport(spbuMachineEntity, machineModelPartEntity);
 			if (latestServiceReportEntity == null) {
 				final PredictionResult predictionResult = new PredictionResult(spbuMachineEntity, machineModelPartEntity, PredictionType.UNTRACKED, getMttf(spbuMachineEntity, machineModelPartEntity));
 				result.add(predictionResult);
 			} else {
 				final PredictionType predictionType = getPredictionType(machineModelPartEntity);
 				final PredictionResult predictionResult;
 				switch (predictionType) {
 					case TIME:
 						predictionResult = predictByTime(latestServiceReportEntity);
 						break;
 					case TOTALIZER:
 						predictionResult = predictByTotalizer(latestServiceReportEntity);
 						break;
 					default:
 						throw new RuntimeException("Unexpected prediction type: " + predictionType);
 				}
 				if (predictionResult != null) {
 					result.add(predictionResult);
 				}
 			}
 		}
 	}
 
 	private PredictionType getPredictionType(MachineModelPartEntity machineModelPartEntity) {
 		if (machineModelPartEntity.getMachineModelTotalizerEntityList().isEmpty()) {
 			return PredictionType.TIME;
 		} else {
 			return PredictionType.TOTALIZER;
 		}
 	}
 
 	private PredictionResult predictByTime(ServiceReportEntity latestServiceReportEntity) {
 		final Date latestServiceDate = latestServiceReportEntity.getDate();
 		final Date currentDate = new Date();
 		final long tickDiff = currentDate.getTime() - latestServiceDate.getTime();
 		final long tickDiffInDays = tickDiff / UNIT_DAY;
 		final MachinePartMttf machinePartMttf = getMttf(latestServiceReportEntity);
 		if (machinePartMttf.getMttf() < 0) {
 			return null;
 		}
 
 		//update: return all parts even it's TTF is not in the MTTF Threshold range
 		final PredictionResult predictionResult = new PredictionResult(latestServiceReportEntity.getSpbuMachineEntity(), latestServiceReportEntity.getMachineModelPartEntity(), PredictionType.TIME, machinePartMttf);
 		long deadLine = (long) (latestServiceDate.getTime() + (machinePartMttf.getMttf() * UNIT_DAY));
 		long tickToDeadLine = deadLine - System.currentTimeMillis();
 		long daysToDeadLine = tickToDeadLine / UNIT_DAY;
 		LOG.debug("Latest ServiceReportTime:[{}], MTTF:[{}], MTTF_Tick:[{}], DeadLine:[{}], TickToDeadLine:[{}], DaysToDeadLine:[{}]", latestServiceDate, machinePartMttf.getMttf(), ((long) machinePartMttf.getMttf() * UNIT_DAY), new Date(deadLine), tickToDeadLine, daysToDeadLine);
 		predictionResult.setTtf(daysToDeadLine);
 		return predictionResult;
 
 //		if (tickDiffInDays > machinePartMttf.getMttf() || tickDiffInDays > machinePartMttf.getMttfThreshold()) {
 //			final PredictionResult predictionResult = new PredictionResult(latestServiceReportEntity.getSpbuMachineEntity(), latestServiceReportEntity.getMachineModelPartEntity(), PredictionType.TIME, machinePartMttf);
 //			long deadLine = (long) (latestServiceDate.getTime() + (machinePartMttf.getMttf() * UNIT_DAY));
 //			long tickToDeadLine = deadLine - System.currentTimeMillis();
 //			long daysToDeadLine = tickToDeadLine / UNIT_DAY;
 //			LOG.debug("Latest ServiceReportTime:[{}], MTTF:[{}], MTTF_Tick:[{}], DeadLine:[{}], TickToDeadLine:[{}], DaysToDeadLine:[{}]", latestServiceDate, machinePartMttf.getMttf(), ((long) machinePartMttf.getMttf() * UNIT_DAY), new Date(deadLine), tickToDeadLine, daysToDeadLine);
 //			predictionResult.setTtf(daysToDeadLine);
 //			return predictionResult;
 //		}
 //		return null;
 	}
 
 	private PredictionResult predictByTotalizer(ServiceReportEntity latestServiceReportEntity) {
 		final double accumulatedTotalizerOnLastService = getTotalizerCounterOnLastService(latestServiceReportEntity);
 		final double accumulatedCurrentTotalizer = getCurrentTotalizerCounter(latestServiceReportEntity);
 		final double totalizerDiff = accumulatedCurrentTotalizer - accumulatedTotalizerOnLastService;
 		final MachinePartMttf machinePartMttf = getMttf(latestServiceReportEntity);
 		if (machinePartMttf.getMttf() < 0) {
 			return null;
 		}
 
 		//update: return all parts even it's TTF is not in the MTTF Threshold range
 		final PredictionResult predictionResult = new PredictionResult(latestServiceReportEntity.getSpbuMachineEntity(), latestServiceReportEntity.getMachineModelPartEntity(), PredictionType.TOTALIZER, machinePartMttf);
 
 		LOG.debug("accumulatedTotalizerOnLastService:[{}], MTTF:[{}], TTF:[{}]", accumulatedTotalizerOnLastService, machinePartMttf.getMttf(), ((accumulatedTotalizerOnLastService + machinePartMttf.getMttf()) - totalizerDiff));
 		predictionResult.setTtf(((accumulatedTotalizerOnLastService + machinePartMttf.getMttf()) - totalizerDiff) / TOTALIZER_PREDICT_PER_DAY);
 		return predictionResult;
 		
 //		if (totalizerDiff > machinePartMttf.getMttfThreshold()) {
 //			final PredictionResult predictionResult = new PredictionResult(latestServiceReportEntity.getSpbuMachineEntity(), latestServiceReportEntity.getMachineModelPartEntity(), PredictionType.TOTALIZER, machinePartMttf);
 //
 //			LOG.debug("accumulatedTotalizerOnLastService:[{}], MTTF:[{}], TTF:[{}]", accumulatedTotalizerOnLastService, machinePartMttf.getMttf(), ((accumulatedTotalizerOnLastService + machinePartMttf.getMttf()) - totalizerDiff));
 //			predictionResult.setTtf(((accumulatedTotalizerOnLastService + machinePartMttf.getMttf()) - totalizerDiff) / TOTALIZER_PREDICT_PER_DAY);
 //			return predictionResult;
 //		}
 //		return null;
 	}
 
 	private ServiceReportEntity getLatestServiceReport(SpbuMachineEntity spbuMachineEntity, MachineModelPartEntity machineModelPartEntity) {
		final List<ServiceReportEntity> latestServiceReports = serviceReportRepository.findBySpbuMachineEntityAndMachineModelPartEntity(spbuMachineEntity, machineModelPartEntity, new Sort(new Sort.Order(Sort.Direction.DESC, "date"), new Sort.Order(Sort.Direction.DESC, "id")), new PageRequest(0, 1));
 		assert (latestServiceReports.size() <= 1);
 		return latestServiceReports.isEmpty() ? null : latestServiceReports.get(0);
 	}
 
 	private double getTotalizerCounterOnLastService(ServiceReportEntity latestServiceReportEntity) {
 		double accumulatedTotalizerOnLastService = 0;
 		if (latestServiceReportEntity != null) {
 			final List<ServiceReportSpbuMachineTotalizerEntity> recordedTotalizers = new LinkedList<>();
 			for (final MachineModelTotalizerEntity machineModelTotalizerEntity : latestServiceReportEntity.getMachineModelPartEntity().getMachineModelTotalizerEntityList()) {
 				ServiceReportSpbuMachineTotalizerEntity recordedTotalizer = serviceReportSpbuMachineTotalizerRepository.findByServiceReportEntityAndMachineModelTotalizerEntity(latestServiceReportEntity, machineModelTotalizerEntity);
 				if (recordedTotalizer != null) {
 					recordedTotalizers.add(recordedTotalizer);
 				}
 			}
 			for (final ServiceReportSpbuMachineTotalizerEntity recordedTotalizer : recordedTotalizers) {
 				accumulatedTotalizerOnLastService += recordedTotalizer.getCounter();
 			}
 		}
 		return accumulatedTotalizerOnLastService;
 	}
 
 	private double getCurrentTotalizerCounter(ServiceReportEntity latestServiceReportEntity) {
 		double accumulatedCurrentTotalizer = 0;
 		final List<SpbuMachineTotalizerEntity> totalizers = new LinkedList<>();
 		for (final MachineModelTotalizerEntity machineModelTotalizerEntity : latestServiceReportEntity.getMachineModelPartEntity().getMachineModelTotalizerEntityList()) {
 			final SpbuMachineTotalizerEntity totalizer = spbuMachineTotalizerRepository.findBySpbuMachineEntityAndMachineModelTotalizerEntity(latestServiceReportEntity.getSpbuMachineEntity(), machineModelTotalizerEntity);
 			if (totalizer != null) {
 				totalizers.add(totalizer);
 			}
 		}
 		for (final SpbuMachineTotalizerEntity totalizer : totalizers) {
 			accumulatedCurrentTotalizer += totalizer.getCounter();
 		}
 		return accumulatedCurrentTotalizer;
 	}
 
 	private MachinePartMttf getMttf(ServiceReportEntity latestServiceReportEntity) {
 		return getMttf(latestServiceReportEntity.getSpbuMachineEntity(), latestServiceReportEntity.getMachineModelPartEntity());
 	}
 
 	private MachinePartMttf getMttf(SpbuMachineEntity spbuMachineEntity, MachineModelPartEntity machineModelPartEntity) {
 		final MachinePartMttf mttf = new MachinePartMttf();
 		final SpbuMachinePartMttfEntity spbuMachinePartMttfEntity = machinePartMttfRepository.findBySpbuMachineEntityAndMachineModelPartEntity(spbuMachineEntity, machineModelPartEntity);
 		if (spbuMachinePartMttfEntity == null) {
 			mttf.setMttf(machineModelPartEntity.getMachinePartTypeEntity().getDefaultMttf());
 			mttf.setMttfThreshold(machineModelPartEntity.getMachinePartTypeEntity().getDefaultMttfThreshold());
 		} else {
 			mttf.setMttf(spbuMachinePartMttfEntity.getMttf());
 			mttf.setMttfThreshold(spbuMachinePartMttfEntity.getMttfThreshold());
 		}
 		return mttf;
 	}
 
 	public static class PredictionResult {
 
 		private final SpbuMachineEntity spbuMachineEntity;
 		private final MachineModelPartEntity machineModelPartEntity;
 		private final PredictionType predictionType;
 		private final MachinePartMttf machinePartMttf;
 		private double ttf = 0d;
 
 		public PredictionResult(SpbuMachineEntity spbuMachineEntity, MachineModelPartEntity machineModelPartEntity, PredictionType predictionType, MachinePartMttf machinePartMttf) {
 			this.spbuMachineEntity = spbuMachineEntity;
 			this.machineModelPartEntity = machineModelPartEntity;
 			this.predictionType = predictionType;
 			this.machinePartMttf = machinePartMttf;
 		}
 
 		public SpbuMachineEntity getSpbuMachineEntity() {
 			return spbuMachineEntity;
 		}
 
 		public MachineModelPartEntity getMachineModelPartEntity() {
 			return machineModelPartEntity;
 		}
 
 		public PredictionType getPredictionType() {
 			return predictionType;
 		}
 
 		public MachinePartMttf getMachinePartMttf() {
 			return machinePartMttf;
 		}
 
 		public double getTtf() {
 			return ttf;
 		}
 
 		public void setTtf(double ttf) {
 			this.ttf = ttf;
 		}
 
 		@Override
 		public String toString() {
 			return "PredictionResult{" + "spbuMachineEntity=" + spbuMachineEntity + ", machineModelPartEntity=" + machineModelPartEntity + ", predictionType=" + predictionType + ", machinePartMttf=" + machinePartMttf + ", ttf=" + ttf + '}';
 		}
 	}
 
 	public static class MachinePartMttf {
 
 		private double mttf;
 		private double mttfThreshold;
 
 		public MachinePartMttf() {
 		}
 
 		public double getMttf() {
 			return mttf;
 		}
 
 		public void setMttf(double mttf) {
 			this.mttf = mttf;
 		}
 
 		public double getMttfThreshold() {
 			return mttfThreshold;
 		}
 
 		public void setMttfThreshold(double mttfThreshold) {
 			this.mttfThreshold = mttfThreshold;
 		}
 
 		@Override
 		public String toString() {
 			return "MachinePartMttf{" + "mttf=" + mttf + ", mttfThreshold=" + mttfThreshold + '}';
 		}
 	}
 
 	public static enum PredictionType {
 
 		TOTALIZER,
 		TIME,
 		UNTRACKED;
 	}
 }
