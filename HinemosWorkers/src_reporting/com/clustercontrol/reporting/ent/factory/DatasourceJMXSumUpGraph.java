/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.ent.bean.DataKey;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo.OutputNodeInfo;
import com.clustercontrol.reporting.ent.bean.OutputFacilityInfo.OutputScopeInfo;
import com.clustercontrol.reporting.ent.bean.ResourceChart;
import com.clustercontrol.reporting.ent.session.ReportingJmxControllerBean;
import com.clustercontrol.reporting.ent.session.ReportingPerformanceControllerBean;
import com.clustercontrol.reporting.ent.util.PropertiesConstant;
import com.clustercontrol.reporting.ent.util.ReportingUtil;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.repository.model.FacilityInfo;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRCsvDataSource;

/**
 * The DatasourceJMXLineGraph fetch JMX monitoring related data from database,
 * and generate data source for report output.
 * 
 * @version 5.0.b
 * @since 5.0.b
 */
public class DatasourceJMXSumUpGraph extends DatasourceSamePattern {
	private static Log m_log = LogFactory.getLog(DatasourceJMXSumUpGraph.class);

	private Map<String, Map<String, List<String[]>>> m_databaseMap = new HashMap<>();

	private String m_title;
	private String m_suffix;
	private String m_label;
	private boolean m_adjustMax = false;
	private boolean m_adjustMin = false;
	private Double maxval = 0.0;
	private Double minval = 0.0;

	private Integer count = 0;

	/**
	 * csv?????????????????? item_code : ??????????????? item_name : ????????? date_time : ??????(????????????????????????)
	 * legend_name : ??????????????????????????????????????????????????? facilityid : ??????????????????ID value : ?????????
	 * 
	 */
	private final String[] COLUMNS = { "item_code", "date_time", "legend_name", "facilityid", "value" };

	/**
	 * ??????????????????????????????????????????CSV???????????????????????????
	 * 
	 * @param csvFileName
	 * @param facilityId
	 * @param columns
	 * @return
	 */
	protected void getCsvFromDB(String csvFileName, List<DataKey> datakeys) {
		m_log.info("create csv file. file name : " + csvFileName);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(csvFileName), false))) {
			bw.write(ReportUtil.joinStrings(COLUMNS, ","));
			bw.newLine();

			for (DataKey dataKey : datakeys) {
				List<String[]> dataLines = m_databaseMap.get(dataKey.getFacilityId()).get(dataKey.getDisplayName());
				if (dataLines != null && !dataLines.isEmpty()) {
					for (String[] line : dataLines) {
						bw.write(ReportUtil.joinStrings(line, ","));
						bw.newLine();
					}
				}
			}
		} catch (Exception e) {
			m_log.error(e, e);
		}
	}

	private List<String> createNodeIdList(OutputFacilityInfo rootFacility) {
		List<String> facilityIdList = new ArrayList<>();
		if (rootFacility instanceof OutputScopeInfo) {
			OutputScopeInfo rootScope = (OutputScopeInfo) rootFacility;
			for (OutputScopeInfo scope : rootScope.getScopes()) {
				for (OutputNodeInfo node : scope.getNodes()) {
					facilityIdList.add(node.getFacilityId());
				}
			}
			for (OutputNodeInfo node : rootScope.getNodes()) {
				facilityIdList.add(node.getFacilityId());
			}
		} else if (rootFacility instanceof OutputNodeInfo) {
			facilityIdList.add(rootFacility.getFacilityId());
		}
		return facilityIdList;
	}

	private Map<String, List<String[]>> getItemDataMap(String facilityId, String itemCode, int divider, Boolean trimFlg, String legendType, String facilityName, Integer first,
			Integer last) {
		List<String[]> rawData = new ArrayList<>();
		Map<String, List<String[]>> itemDataMap = new TreeMap<>();
		
		ReportingJmxControllerBean controller = new ReportingJmxControllerBean();
		// ?????????????????????????????????????????????(raw)???????????????????????????
		int summaryType = ReportUtil.getSummaryType(m_startDate.getTime(), m_endDate.getTime());
		switch (summaryType) {
		case SummaryTypeConstant.TYPE_AVG_HOUR:
			List<Object[]> summaryHList = null;
			try {
				summaryHList = controller.getSummarySumAvgHour(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryHList == null || summaryHList.isEmpty())
				break;
			for (Object[] summaryHour : summaryHList) {
				itemDataMap.put(itemCode, rawData);
				String nextItemCode = summaryHour[0].toString();
				if (!itemDataMap.containsKey(nextItemCode)) {
					itemDataMap.put(nextItemCode, new ArrayList<String[]>());
				}
				String[] result = new String[COLUMNS.length];
				result[0] = nextItemCode;
				result[1] = new Timestamp((Long) summaryHour[1]).toString();

				// ??????????????????????????????????????????????????????????????????
				String legendValue;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendType)) {
					legendValue = facilityName;
				} else {
					legendValue = summaryHour[2].toString();
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				String legendName;
				if (trimFlg) {
					legendName = ReportingUtil.trimLegend(legendValue, first, last,
							isDefine(PropertiesConstant.TRIM_STR_KEY, PropertiesConstant.TRIM_STR_DEFAULT));
				} else {
					legendName = legendValue;
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName);
				}
				result[2] = legendName;
				result[3] = summaryHour[2].toString();

				Double value = Double.NaN;
				if (summaryHour[3] != null) {
					value = Double.valueOf(summaryHour[3].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[4] = Double.toString(value / divider);
					} else {
						result[4] = value.toString();
					}

					if (this.maxval < Double.valueOf(result[4])) {
						this.maxval = Double.valueOf(result[4]) + 1;
					} else if (this.minval > Double.valueOf(result[4])) {
						this.minval = Double.valueOf(result[4]);
					}
					itemDataMap.get(nextItemCode).add(result);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_DAY:
			List<Object[]> summaryDList = null;
			try {
				summaryDList = controller.getSummarySumAvgDay(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryDList == null || summaryDList.isEmpty())
				break;
			for (Object[] summaryDay : summaryDList) {
				itemDataMap.put(itemCode, rawData);
				String nextItemCode = summaryDay[0].toString();
				if (!itemDataMap.containsKey(nextItemCode)) {
					itemDataMap.put(nextItemCode, new ArrayList<String[]>());
				}
				String[] result = new String[COLUMNS.length];
				result[0] = nextItemCode;
				result[1] = new Timestamp((Long) summaryDay[1]).toString();

				// ??????????????????????????????????????????????????????????????????
				String legendValue;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendType)) {
					legendValue = facilityName;
				} else {
					legendValue = summaryDay[2].toString();
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				String legendName;
				if (trimFlg) {
					legendName = ReportingUtil.trimLegend(legendValue, first, last,
							isDefine(PropertiesConstant.TRIM_STR_KEY, PropertiesConstant.TRIM_STR_DEFAULT));
				} else {
					legendName = legendValue;
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName);
				}
				result[2] = legendName;
				result[3] = summaryDay[2].toString();

				Double value = Double.NaN;
				if (summaryDay[3] != null) {
					value = Double.valueOf(summaryDay[3].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[4] = Double.toString(value / divider);
					} else {
						result[4] = value.toString();
					}

					if (this.maxval < Double.valueOf(result[4])) {
						this.maxval = Double.valueOf(result[4]) + 1;
					} else if (this.minval > Double.valueOf(result[4])) {
						this.minval = Double.valueOf(result[4]);
					}
					itemDataMap.get(nextItemCode).add(result);
				}
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_MONTH:
			List<Object[]> summaryMList = null;
			try {
				summaryMList = controller.getSummaryJmxAvgMonth(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryMList == null || summaryMList.isEmpty())
				break;
			for (Object[] summaryMonth : summaryMList) {
				itemDataMap.put(itemCode, rawData);
				String nextItemCode = summaryMonth[0].toString();
				if (!itemDataMap.containsKey(nextItemCode)) {
					itemDataMap.put(nextItemCode, new ArrayList<String[]>());
				}
				String[] result = new String[COLUMNS.length];
				result[0] = nextItemCode;
				result[1] = new Timestamp((Long) summaryMonth[1]).toString();

				// ??????????????????????????????????????????????????????????????????
				String legendValue;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendType)) {
					legendValue = facilityName;
				} else {
					legendValue = summaryMonth[2].toString();
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				String legendName;
				if (trimFlg) {
					legendName = ReportingUtil.trimLegend(legendValue, first, last,
							isDefine(PropertiesConstant.TRIM_STR_KEY, PropertiesConstant.TRIM_STR_DEFAULT));
				} else {
					legendName = legendValue;
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName);
				}
				result[2] = legendName;
				result[3] = summaryMonth[2].toString();

				Double value = Double.NaN;
				if (summaryMonth[3] != null) {
					value = Double.valueOf(summaryMonth[3].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[4] = Double.toString(value / divider);
					} else {
						result[4] = value.toString();
					}

					if (this.maxval < Double.valueOf(result[4])) {
						this.maxval = Double.valueOf(result[4]) + 1;
					} else if (this.minval > Double.valueOf(result[4])) {
						this.minval = Double.valueOf(result[4]);
					}
					itemDataMap.get(nextItemCode).add(result);
				}
			}
			break;
		default: // default???RAW?????????
			List<Object[]> summaryList = null;
			try {
				summaryList = controller.getSummarySumAvgData(facilityId, m_startDate.getTime(),
						m_endDate.getTime(), itemCode);
			} catch (HinemosDbTimeout e) {
				m_log.warn("getItemDataMap() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			if (summaryList == null || summaryList.isEmpty())
				break;
			for (Object[] data : summaryList) {
				itemDataMap.put(itemCode, rawData);
				String nextItemCode = data[0].toString();
				if (!itemDataMap.containsKey(nextItemCode)) {
					itemDataMap.put(nextItemCode, new ArrayList<String[]>());
				}
				String[] result = new String[COLUMNS.length];
				result[0] = nextItemCode;
				result[1] = new Timestamp((Long) data[1]).toString();

				// ??????????????????????????????????????????????????????????????????
				String legendValue;
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendType)) {
					legendValue = facilityName;
				} else {
					legendValue = data[2].toString();
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendValue : " + legendValue);
				}

				String legendName;
				if (trimFlg) {
					legendName = ReportingUtil.trimLegend(legendValue, first, last,
							isDefine(PropertiesConstant.TRIM_STR_KEY, PropertiesConstant.TRIM_STR_DEFAULT));
				} else {
					legendName = legendValue;
				}
				if (m_log.isDebugEnabled()) {
					m_log.debug("legendName : " + legendName);
				}
				result[2] = legendName;
				result[3] = data[2].toString();

				Double value = Double.NaN;
				if (data[3] != null) {
					value = Double.valueOf(data[3].toString());
				}
				if (!value.isNaN()) {
					if (divider > 1) {
						result[4] = Double.toString(value / divider);
					} else {
						result[4] = value.toString();
					}

					if (this.maxval < Double.valueOf(result[4])) {
						this.maxval = Double.valueOf(result[4]) + 1;
					} else if (this.minval > Double.valueOf(result[4])) {
						this.minval = Double.valueOf(result[4]);
					}
					itemDataMap.get(nextItemCode).add(result);
				}
			}
			break;
		}
		return itemDataMap;
	}

	public void collectDataSource(OutputFacilityInfo rootFacility, int chartType) throws ReportingPropertyNotFound {
		List<String> facilityIds = createNodeIdList(rootFacility);

		// ??????????????????????????????????????????????????????????????????
		String itemCode = m_propertiesMap.get(PropertiesConstant.ITEM_CODE_KEY + "." + chartType);
		if (itemCode == null || itemCode.isEmpty()) {
			throw new ReportingPropertyNotFound(
					PropertiesConstant.ITEM_CODE_KEY + "." + chartType + " is not defined.");
		} else {
			itemCode = itemCode.trim();
		}
		m_log.info("collect item " + PropertiesConstant.ITEM_CODE_KEY + " : " + itemCode);

		int divider = Integer.parseInt(isDefine(PropertiesConstant.DIVIDER_KEY + "." + chartType, "1"));
		m_title = m_propertiesMap.get(PropertiesConstant.CHART_TITLE_KEY + "." + chartType);
		m_suffix = isDefine(SUFFIX_KEY_VALUE + "." + chartType, itemCode.toLowerCase());
		m_label = m_propertiesMap.get(PropertiesConstant.LABEL_KEY + "." + chartType);

		m_adjustMax = Boolean.valueOf(m_propertiesMap.get(PropertiesConstant.ADJUST_MAX_VALUE_KEY + "." + chartType));
		m_adjustMin = Boolean.valueOf(m_propertiesMap.get(PropertiesConstant.ADJUST_MIN_VALUE_KEY + "." + chartType));

		Boolean trimFlg = false;
		Integer first = 0;
		if ((m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY + "." + chartType) != null)
				&& !m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY + "." + chartType).isEmpty()) {
			try {
				first = Integer
						.valueOf(m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY + "." + chartType));
				if (first > 0)
					trimFlg = true;
			} catch (NumberFormatException e) {
				m_log.info("check your property. key: " + PropertiesConstant.LEGEND_TRIM_PREFIX_KEY + "." + chartType
						+ " value : "
						+ m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_PREFIX_KEY + "." + chartType));
			}
		}
		Integer last = 0;
		if ((m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY + "." + chartType) != null)
				&& !m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY + "." + chartType).isEmpty()) {
			try {
				last = Integer
						.valueOf(m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY + "." + chartType));
				if (last > 0)
					trimFlg = true;
				else
					trimFlg = false;
			} catch (NumberFormatException e) {
				m_log.info("check your property. key: " + PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY + "." + chartType
						+ " value : "
						+ m_propertiesMap.get(PropertiesConstant.LEGEND_TRIM_SUFFIX_KEY + "." + chartType));
				trimFlg = false;
			}
		}
		String legendType = isDefine(PropertiesConstant.LEGEND_TYPE_KEY,
				PropertiesConstant.LEGEND_TYPE_DEFAULT);
		m_log.info("legend.type : " + legendType);

		// ???????????????????????????????????????????????????????????????????????????????????????????????????
		String outputMode = isDefine(PropertiesConstant.OUTPUT_MODE_KEY, PropertiesConstant.OUTPUT_MODE_DEFAULT);
		if (!outputMode.equals(PropertiesConstant.MODE_AUTO)) {
			if (m_propertiesMap.get(PropertiesConstant.GRAPH_OUTPUT_ID_KEY + "." + chartType).isEmpty()) {
				throw new ReportingPropertyNotFound(
						PropertiesConstant.GRAPH_OUTPUT_ID_KEY + "." + chartType + " is not defined.");
			}
		} 

		m_databaseMap = new HashMap<>();
		String facilityName = null;
		// DB??????ID????????????????????????????????????
		for (String id : facilityIds) {
			// ????????????CSV???????????????????????????
			try {
				// ??????????????????????????????????????????????????????????????????
				if (PropertiesConstant.TYPE_FACILITY_NAME.equals(legendType)) {
					ReportingPerformanceControllerBean performanceController = new ReportingPerformanceControllerBean();
					FacilityInfo facilityInfo = performanceController.getFacilityInfo(id);

					if (facilityInfo != null) {
						facilityName = facilityInfo.getFacilityName();
					}
				}
			} catch (FacilityNotFound e) {
				m_log.error(e, e);
			}
			Map<String, List<String[]>> itemDataMap = this.getItemDataMap(id, itemCode, divider, trimFlg,
					legendType, facilityName, first, last);
			if (!itemDataMap.isEmpty()) {
				// DB???????????????????????????????????????
				m_databaseMap.put(id, itemDataMap);
			}
		}
	}

	@Override
	public List<DataKey> getKeys(String facilityId) {
		List<DataKey> keys = new ArrayList<>();
		Map<String, List<String[]>> dataMap = m_databaseMap.get(facilityId);
		if (dataMap == null)
			return new ArrayList<DataKey>();

		for (String displaname : m_databaseMap.get(facilityId).keySet()) {
			DataKey key = new DataKey(facilityId, displaname);
			keys.add(key);
		}
		return keys;
	}

	@Override
	public Map<String, Object> createDataSource(ResourceChart chart, int chartNum) throws ReportingPropertyNotFound {
		count++;
		m_retMap = new HashMap<String, Object>();
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);
		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId,
				m_suffix + count + "_" + chartNum + "_" + dayString);

		getCsvFromDB(csvFileName, chart.getItems());

		try {
			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			m_retMap.put(ReportingConstant.STR_DS + "_" + chartNum, ds);
		} catch (JRException e) {
			m_log.error(e, e);
		}

		String chartTitle = m_title;
		String subTitle = chart.getSubTitle();
		if (null != subTitle) {
			chartTitle += " - " + subTitle;
		}

		m_retMap.put(PropertiesConstant.CHART_TITLE_KEY + "." + chartNum, chartTitle);
		m_retMap.put(PropertiesConstant.LABEL_KEY + "." + chartNum, m_label);

		if (m_label.equals("%")) {
			m_retMap.put(PropertiesConstant.FIXVAL_KEY + "." + chartNum, 100.0);
		}
		if (m_label.equals("%")) {
			m_retMap.put(PropertiesConstant.MINVAL_KEY + "." + chartNum, 0.0);
		}

		if (m_adjustMax) {
			// Set max except min == max (properly == 0)
			if (! this.minval.equals(this.maxval)) {
				m_retMap.put(PropertiesConstant.FIXVAL_KEY + "." + chartNum, this.maxval);
			}
		}
		if (m_adjustMin) {
			m_retMap.put(PropertiesConstant.MINVAL_KEY + "." + chartNum, this.minval);
		}
		return m_retMap;
	}
}