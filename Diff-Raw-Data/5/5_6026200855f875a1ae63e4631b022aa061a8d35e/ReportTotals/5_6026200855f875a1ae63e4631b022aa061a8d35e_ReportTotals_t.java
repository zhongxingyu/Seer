 package br.com.bluesoft.report.components;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.List;
 import java.util.Map;
 
 import br.com.bluesoft.commons.lang.NumberUtil;
 
 import com.google.common.collect.Maps;
 
 public class ReportTotals {
 
 	private final ReportDataGrid grid;
 
 	private boolean calculated = false;
 
 	private int numberOfRows = 0;
 
 	private final Map<Integer, BigDecimal> sumMap = Maps.newHashMap();
 	private final Map<Integer, BigDecimal> sumWeightMap = Maps.newHashMap();
 	private final Map<Integer, BigDecimal> avgMap = Maps.newHashMap();
 	private final Map<Integer, BigDecimal> totalsMap = Maps.newHashMap();
 	private final Map<Integer, Integer> countGreaterThanZero = Maps.newHashMap();
 
 	public ReportTotals(final ReportDataGrid grid) {
 		this.grid = grid;
 	}
 
 	public void calculate() {
 
 		calculated = true;
 
 		final List<ReportDataGridColumn> columns = grid.getColumns();
 
 		sum(columns);
 		weightedAverage(columns);
 		fillTotalsMap(columns);
 
 	}
 
 	public boolean isCalculated() {
 		return calculated;
 	}
 
 	private void fillTotalsMap(final List<ReportDataGridColumn> columns) {
 
 		for (final ReportDataGridColumn column : columns) {
 			if (column.isTotalized()) {
 				BigDecimal sumValue = sumMap.get(column.getIndex());
 				switch (column.getTotalizationType()) {
 					case SUM:
 						totalsMap.put(column.getIndex(), sumValue);
 						break;
 					case AVERAGE:
 					case WEIGHTED_AVERAGE:
 						switch (column.getCalculationType()) {
 							case WEIGHTED_AVERAGE:
 								calculateWeightAndPutItInTheTotalsMap(column);
 								break;
 							default:
 								totalsMap.put(column.getIndex(), avgMap.get(column.getIndex()));
 								break;
 						}
 
 						break;
 				}
 			}
 		}
 
 	}
 
 	private void calculateWeightAndPutItInTheTotalsMap(final ReportDataGridColumn column) {
 		BigDecimal sumValue = sumMap.get(column.getColumnNotCalculated().getIndex());
 		BigDecimal sumWeight = sumMap.get(column.getAverageWeightColumn().getIndex());
 		if (sumWeight != null && sumWeight.compareTo(BigDecimal.ZERO) > 0) {
 			totalsMap.put(column.getIndex(), sumValue.divide(sumWeight, RoundingMode.HALF_UP));
 		} else {
 			totalsMap.put(column.getIndex(), BigDecimal.ZERO);
 		}
 	}
 
 	private void sum(final List<ReportDataGridColumn> columns) {
 
 		if (grid.isTotalized()) {
 
 			for (final Object bean : grid.getData()) {
 				numberOfRows++;
 
 				for (final ReportDataGridColumn column : columns) {
 
 					if (column.isTotalized()) {
 						BigDecimal previousTotal = NumberUtil.toBigDecimal(sumMap.get(column.getIndex()));
 						BigDecimal currentValue = NumberUtil.toBigDecimal(column.getPropertyValue(bean));
 						BigDecimal newTotal = previousTotal.add(currentValue);
 						sumMap.put(column.getIndex(), newTotal);
 
 						if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
 							countGreaterThanZero.put(column.getIndex(), NumberUtil.toInt(countGreaterThanZero.get(column.getIndex())) + 1);
 						}
 
 						if (column.getTotalizationType() == TotalizationType.WEIGHTED_AVERAGE) {
 
 							final BigDecimal previousTotalWeight = NumberUtil.toBigDecimal(sumWeightMap.get(column.getIndex()));
 							final BigDecimal currentWeight = NumberUtil.toBigDecimal(column.getAverageWeightColumn().getPropertyValue(bean)).multiply(currentValue);
 							final BigDecimal newTotalWeight = previousTotalWeight.add(currentWeight);
 
 							sumWeightMap.put(column.getIndex(), newTotalWeight);
 						}
 
 						if (column.getAverageWeightColumn() != null && !column.getAverageWeightColumn().isTotalized()) {
 							previousTotal = NumberUtil.toBigDecimal(sumMap.get(column.getAverageWeightColumn().getIndex()));
 							currentValue = NumberUtil.toBigDecimal(column.getAverageWeightColumn().getPropertyValue(bean));
 							newTotal = previousTotal.add(currentValue);
 							sumMap.put(column.getAverageWeightColumn().getIndex(), newTotal);
 						}
 
 					}
 
 				}
 
 			}
 		}
 	}
 
 	private void weightedAverage(final List<ReportDataGridColumn> columns) {
 		if (grid.isTotalized()) {
 
 			for (final ReportDataGridColumn column : columns) {
 				if (column.isTotalized()) {
 					if (column.getTotalizationType() == TotalizationType.WEIGHTED_AVERAGE) {
 						final BigDecimal f1 = sumWeightMap.get(column.getIndex());
 						final BigDecimal f2 = sumMap.get(column.getAverageWeightColumn().getIndex());
 						BigDecimal wavg = BigDecimal.ZERO;
 						if (f1 != null && f2 != null && f2.doubleValue() > 0) {
 							wavg = f1.divide(f2, RoundingMode.HALF_UP);
 						}
 						avgMap.put(column.getIndex(), wavg);
 					} else if (column.getTotalizationType() == TotalizationType.AVERAGE) {
						BigDecimal avg = BigDecimal.ZERO;
						if (sumMap.get(column.getIndex()) != null && numberOfRows > 0) {
							avg = sumMap.get(column.getIndex()).divide(BigDecimal.valueOf(numberOfRows), RoundingMode.HALF_UP);
						}
 						avgMap.put(column.getIndex(), avg);
 					} else if (column.getTotalizationType() == TotalizationType.AVERAGE_IGNORING_LESS_THAN_ONE) {
 						long numberOfRowsGreaterThanZero = countGreaterThanZero.get(column.getIndex()) != null && countGreaterThanZero.get(column.getIndex()) != 0 ? countGreaterThanZero.get(column.getIndex()) : 1;
 						final BigDecimal avg = sumMap.get(column.getIndex()).divide(BigDecimal.valueOf(numberOfRowsGreaterThanZero), RoundingMode.HALF_UP);
 						avgMap.put(column.getIndex(), avg);
 					}
 				}
 
 			}
 		}
 	}
 
 	public Map<Integer, BigDecimal> getTotals() {
 		return totalsMap;
 	}
 
 }
