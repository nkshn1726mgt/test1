/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.model.CorrelationCheckInfo;
import com.clustercontrol.analytics.util.AnalyticsUtil;
import com.clustercontrol.analytics.util.OperatorAnalyticsUtil;
import com.clustercontrol.analytics.util.OperatorCommonUtil;
import com.clustercontrol.analytics.util.QueryUtil;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache;
import com.clustercontrol.monitor.run.util.MonitorMultipleExecuteTask;
import com.clustercontrol.monitor.run.util.ParallelExecution;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache.MonitorCollectData;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache.MonitorCollectDataPK;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ?????????????????? ????????????????????????????????????????????????????????????<BR>
 *
 * @version 6.1.0
 */
public class RunMonitorCorrelation extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorCorrelation.class );

	/** ???????????????????????? */
	private CorrelationCheckInfo m_correlation = null;

	/** ??????????????????ID(???????????????) */
	private List<String> m_referFacilityIdList = null;

	/** ????????????????????? */
	private String m_unKnownMessage = null;

	/** ??????????????? **/
	private String m_message = null;

	/** ????????????????????????????????? */
	private String m_collectDataListMessage = null;

	/**
	 * ?????????????????????
	 * 
	 */
	public RunMonitorCorrelation() {
		super();
	}

	/**
	 * ????????????????????????????????????CallableTask???????????????????????????????????????????????????????????????
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitorCorrelation createMonitorInstance() {
		return new RunMonitorCorrelation();
	}

	/**
	 * ?????????????????????
	 * 
	 * @param facilityId ??????????????????ID?????????????????????????????????
	 * @return ????????????????????????
	 */
	@Override
	public List<MonitorRunResultInfo> collectMultiple(String facilityId) {

		List<MonitorRunResultInfo> list = new ArrayList<>();
		// set Generation Date
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		m_message = "";
		// ?????????????????????????????????
		MonitorCollectDataPK targetPK 
			= new MonitorCollectDataPK(m_correlation.getTargetMonitorId(), facilityId, 
					m_correlation.getTargetDisplayName(), m_correlation.getTargetItemName());
		for (String referFacilityId : m_referFacilityIdList) {
			// ?????????????????????????????????
			MonitorCollectDataPK referPK 
			= new MonitorCollectDataPK(m_correlation.getReferMonitorId(), referFacilityId, 
					m_correlation.getReferDisplayName(), m_correlation.getReferItemName());
			TreeMap<Long, Double[]> dataMap = getCollectDataAverageList(
					targetPK, referPK, m_correlation.getAnalysysRange().doubleValue(), m_nodeDate, m_monitor.getRunInterval());

			// ???????????????????????????????????????????????????????????????????????? 
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
			sdf.setTimeZone(HinemosTime.getTimeZone());
			StringBuilder sbMonitorCollectDataList = new StringBuilder();
			if (dataMap != null) {
				for (Map.Entry<Long, Double[]> entry : dataMap.entrySet()) {
					String strTargetValue = "-";
					String strReferValue = "-";
					if (entry.getValue()[0] != null && !entry.getValue()[0].isNaN()) {
						strTargetValue = entry.getValue()[0].toString();
					}
					if (entry.getValue()[1] != null && !entry.getValue()[1].isNaN()) {
						strReferValue = entry.getValue()[1].toString();
					}
					sbMonitorCollectDataList.append(String.format("(%s, %s, %s)%n", 
					sdf.format(new Date(entry.getKey())), strTargetValue, strReferValue));
				}
			}
			m_collectDataListMessage = sbMonitorCollectDataList.toString();

			if (m_log.isDebugEnabled()) {
				StringBuilder sb = new StringBuilder();
				sb.append("\n");
				sb.append("dataList = " + sbMonitorCollectDataList.toString());
				sb.toString();
				m_log.info("collectMultiple(): dataOutputput"
					+ " monitorId=" + m_monitor.getMonitorId()
					+ ", facilityId=" + facilityId
					+ ", displayName=" + referFacilityId
					+ ", itemName=" + m_monitor.getItemName()
					+ ", targetDate=" + new Date(m_nodeDate)
					+ "\n" + m_collectDataListMessage
					+ "\n" + sb.toString());
			}
			// ????????????
			Double tmpValue = null;
			// ??????????????????
			Long dataCount = HinemosPropertyCommon.monitor_correlation_lower_limit.getNumericValue();
			try {
				tmpValue = OperatorAnalyticsUtil.getCorrelationCoefficient(dataMap, dataCount.intValue());
			} catch (HinemosArithmeticException e) {
				m_log.warn("collectMultiple():"
						+ " monitorId=" + m_monitor.getMonitorId()
						+ ", facilityId=" + facilityId
						+ ", displayName=" + referFacilityId
						+ ", itemName=" + m_monitor.getItemName()
						+ ", targetDate=" + new Date(m_nodeDate)
						+ "\n" + e.getMessage());
			} catch (HinemosIllegalArgumentException e) {
				m_log.info("collectMultiple():"
						+ " monitorId=" + m_monitor.getMonitorId()
						+ ", facilityId=" + facilityId
						+ ", displayName=" + referFacilityId
						+ ", itemName=" + m_monitor.getItemName()
						+ ", targetDate=" + new Date(m_nodeDate)
						+ "\n" + e.getMessage());
				// ??????????????????????????????????????????????????????
				continue;
			}

			// ?????????????????????null????????????
			if (tmpValue != null && (tmpValue.isNaN() || tmpValue.isInfinite())) {
				tmpValue = null;
			}

			// ?????????????????????????????????????????????
			m_value = tmpValue;
			m_curData = tmpValue;
			m_message = String.format("%s : %s", m_monitor.getItemName(),
					Objects.toString(tmpValue, MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_ANALYTICS.getMessage()));
			boolean success = (tmpValue != null);
			Integer checkResult = getCheckResult(success);

			// ?????????????????????
			MonitorRunResultInfo info = new MonitorRunResultInfo();
			info.setValue(m_value);
			info.setCurData(m_curData);
			info.setFacilityId(facilityId);
			info.setMonitorFlg(success);
			info.setCollectorResult(success);
			info.setCheckResult(checkResult);
			info.setMessage(getMessage(checkResult));
			info.setMessageOrg(getMessageOrg(checkResult));
			if (checkResult == -2) {
				info.setPriority(PriorityConstant.TYPE_NONE);
				info.setProcessType(false);
			} else {
				info.setPriority(getPriority(checkResult));
				info.setProcessType(true);
			}
			info.setNodeDate(m_nodeDate);
			info.setItemName(m_monitor.getItemName());
			info.setDisplayName(referFacilityId);
			info.setCollectorFlg(m_monitor.getCollectorFlg());
			info.setNotifyGroupId(getNotifyGroupId());
			info.setApplication(m_monitor.getApplication());
			list.add(info);
		}
		return list;
	}

	/**
	 * ????????????????????????????????????
	 * ??????????????????????????????[????????????]?????????[??????]???????????????????????????
	 * 
	 * @param targetPk ?????????????????????
	 * @param referPk ????????????????????????????????????
	 * @param analysysRange ????????????
	 * @param targetDate ????????????
	 * @param runInterval ??????
	 * @return ??????????????????????????????
	 */
	private TreeMap<Long, Double[]> getCollectDataAverageList(
			MonitorCollectDataPK targetPk,
			MonitorCollectDataPK referPk,
			Double analysysRange,
			Long targetDate,
			Integer runInterval) {
		TreeMap<Long, Double[]> map = new TreeMap<>(new Comparator<Long>() {
			public int compare(Long m, Long n) {
				return ((Long)m).compareTo(n) * -1;
			}
		});

		// ????????????????????????
		// ??????????????????????????????(??????????????????????????????update()????????????????????????)
		MonitorCollectDataCache.update(
				targetPk.getMonitorId(), 
				targetPk.getFacilityId(), 
				targetPk.getDisplayName(), 
				targetPk.getItemName(), 
				targetDate);
		List<MonitorCollectData> targetMonitorCollectDataList = MonitorCollectDataCache.getMonitorCollectDataList(
				targetPk.getMonitorId(), targetPk.getFacilityId(), targetPk.getDisplayName(), targetPk.getItemName(), targetDate, analysysRange);
		int targetIdx = 0;

		// ????????????????????????
		// ??????????????????????????????(??????????????????????????????update()????????????????????????)
		MonitorCollectDataCache.update(
				referPk.getMonitorId(), 
				referPk.getFacilityId(), 
				referPk.getDisplayName(), 
				referPk.getItemName(), 
				targetDate);
		List<MonitorCollectData> referMonitorCollectDataList = MonitorCollectDataCache.getMonitorCollectDataList(
				referPk.getMonitorId(), referPk.getFacilityId(), referPk.getDisplayName(), referPk.getItemName(), targetDate, analysysRange);
		int referIdx = 0;

		for (long toTime = targetDate; toTime >= targetDate - analysysRange * 60D * 1000D; toTime -= (runInterval * 1000)) {
			long fromTime = toTime - runInterval * 1000;
			if (targetMonitorCollectDataList.size() < targetIdx + 1
					&& referMonitorCollectDataList.size() < referIdx + 1) {
				break;
			}
			if ((targetMonitorCollectDataList.size() < targetIdx + 1 
					|| targetMonitorCollectDataList.get(targetIdx).getTime().longValue() <= fromTime)
					&& (referMonitorCollectDataList.size() < referIdx + 1 
					|| referMonitorCollectDataList.get(referIdx).getTime().longValue() <= fromTime)) {
				// ???????????????????????????????????????????????????????????????????????????
				continue;
			}
			Double targetAverage = null;
			Double referAverage = null;
			List<Double> targetValueList = new ArrayList<>();
			List<Double> referValueList = new ArrayList<>();
			if (targetMonitorCollectDataList.size() > targetIdx
					&& targetMonitorCollectDataList.get(targetIdx).getTime().longValue() > fromTime) {
				// ????????????????????????
				// ???????????????fromTime < ???????????????????????? <= toTime???
				while (targetMonitorCollectDataList.size() > targetIdx
						&& targetMonitorCollectDataList.get(targetIdx).getTime().longValue() > fromTime
						&& targetMonitorCollectDataList.get(targetIdx).getTime().longValue() <= toTime) {
					// ?????????????????????????????????List?????????
					if (targetMonitorCollectDataList.get(targetIdx).getValue() != null
							&& !targetMonitorCollectDataList.get(targetIdx).getValue().isNaN()) {
						targetValueList.add(targetMonitorCollectDataList.get(targetIdx).getValue());
					}
					targetIdx++;
				}
				try {
					targetAverage = OperatorCommonUtil.getAverage(targetValueList);
					m_log.debug("getCollectDataList(): average target monitorId=" + targetPk.getMonitorId() + ", facilityId=" + targetPk.getFacilityId() + ", displayName=" + targetPk.getDisplayName() 
					+ ", itemName=" + targetPk.getItemName() + ", targetDate=" + new Date(targetDate) + ", from=" + new Date(fromTime) + ", to=" + new Date(toTime) + ", list=" + Arrays.toString(targetValueList.toArray()) 
					+ ", average=" + targetAverage);
				} catch (HinemosArithmeticException e) {
					m_log.warn("getCollectDataList(): monitorId=" + targetPk.getMonitorId() + ", facilityId=" + targetPk.getFacilityId() + ", displayName=" + targetPk.getDisplayName() 
						+ ", itemName=" + targetPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				} catch (HinemosIllegalArgumentException e) {
					m_log.debug("getCollectDataList(): monitorId=" + targetPk.getMonitorId() + ", facilityId=" + targetPk.getFacilityId() + ", displayName=" + targetPk.getDisplayName()
						+ ", itemName=" + targetPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				}
			}
			if (referMonitorCollectDataList.size() > referIdx
					&& referMonitorCollectDataList.get(referIdx).getTime().longValue() > fromTime) {
				// ????????????????????????
				// ???????????????fromTime < ???????????????????????? <= toTime???
				while (referMonitorCollectDataList.size() > referIdx
						&& referMonitorCollectDataList.get(referIdx).getTime().longValue() > fromTime
						&& referMonitorCollectDataList.get(referIdx).getTime().longValue() <= toTime) {
					// ?????????????????????????????????List?????????
					if (referMonitorCollectDataList.get(referIdx).getValue() != null
							&& !referMonitorCollectDataList.get(referIdx).getValue().isNaN()) {
						referValueList.add(referMonitorCollectDataList.get(referIdx).getValue());
					}
					referIdx++;
				}
				try {
					referAverage = OperatorCommonUtil.getAverage(referValueList);
					m_log.debug("getCollectDataList(): average target monitorId=" + referPk.getMonitorId() + ", facilityId=" + referPk.getFacilityId() + ", displayName=" + referPk.getDisplayName() 
					+ ", itemName=" + referPk.getItemName() + ", targetDate=" + new Date(targetDate) + ", from=" + new Date(fromTime) + ", to=" + new Date(toTime) + ", list=" + Arrays.toString(referValueList.toArray()) 
					+ ", average=" + referAverage);
				} catch (HinemosArithmeticException e) {
					m_log.warn("getCollectDataList(): monitorId=" + referPk.getMonitorId() + ", facilityId=" + referPk.getFacilityId() + ", displayName=" + referPk.getDisplayName() 
						+ ", itemName=" + referPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				} catch (HinemosIllegalArgumentException e) {
					m_log.debug("getCollectDataList(): monitorId=" + referPk.getMonitorId() + ", facilityId=" + referPk.getFacilityId() + ", displayName=" + referPk.getDisplayName()
						+ ", itemName=" + referPk.getItemName() + ", targetDate=" + new Date(targetDate) + "\n" + e.getMessage());
				}
			}
			if (targetAverage != null || referAverage != null) {
				map.put(toTime, new Double[]{targetAverage, referAverage});
			}
		}

		return map;
	}

	/**
	 * ??????????????????????????????collect???????????????collectList?????????????????????
	 */
	@Override
	public boolean collect(String facilityId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * ?????????????????????????????????????????????
	 * <p>
	 * <ol>
	 * <li>?????????????????????????????????????????????{@link #setMonitorInfo(String, String)}??????</li>
	 * <li>?????????????????????????????????????????????????????????????????????{@link #setJudgementInfo()}??????</li>
	 * <li>?????????????????????????????????????????????????????????{@link #setCheckInfo()}??????</li>
	 * <li>?????????????????????????????????????????????????????????????????????????????? ???{@link #collect(String)}??????</li>
	 * <li>?????????????????????????????????????????????????????? ???{@link #getCheckResult(boolean)}??????</li>
	 * <li>???????????????????????????????????????????????????{@link #getPriority(int)}??????</li>
	 * <li>?????????????????????????????????{@link #notify(boolean, String, int, Date)}??????</li>
	 * </ol>
	 *
	 * @return ??????????????????????????????</code> true </code>
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 *
	 * @see #setMonitorInfo(String, String)
	 * @see #setJudgementInfo()
	 * @see #setCheckInfo()
	 * @see #collect(String)
	 * @see #getCheckResult(boolean)
	 * @see #getPriority(int)
	 * @see #notify(boolean, String, int, Date)
	 */
	@Override
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, InvalidRole, EntityExistsException, HinemosUnknown {

		List<OutputBasicInfo> ret = new ArrayList<>();
		m_now = HinemosTime.getDateInstance();

		m_priorityMap = new HashMap<Integer, ArrayList<String>>();
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_INFO),		new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_WARNING),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_CRITICAL),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN),	new ArrayList<String>());
		List<Sample> sampleList = new ArrayList<Sample>();
		
		try
		{
			// ???????????????????????????
			boolean run = this.setMonitorInfo(m_monitorTypeId, m_monitorId);
			if(!run){
				// ????????????
				return ret;
			}

			// ?????????????????????
			setJudgementInfo();

			// ?????????????????????????????????
			setCheckInfo();

			// ????????????????????????????????????????????????Read?????????????????????????????????????????????
			try {
				com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(m_correlation.getTargetMonitorId(), m_monitor.getOwnerRoleId());
			} catch (InvalidRole | MonitorNotFound e) {
				throw new HinemosUnknown("It does not have access authority to target monitor info. : monitorId=" + m_correlation.getTargetMonitorId());
			}
			try {
				com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(m_correlation.getReferMonitorId(), m_monitor.getOwnerRoleId());
			} catch (InvalidRole | MonitorNotFound e) {
				throw new HinemosUnknown("It does not have access authority to target monitor info. : monitorId=" + m_correlation.getReferMonitorId());
			}

			ArrayList<String> facilityList = null;
			ExecutorCompletionService<List<MonitorRunResultInfo>> ecs 
				= new ExecutorCompletionService<List<MonitorRunResultInfo>>(ParallelExecution.instance().getExecutorService());
			int taskCount = 0;

			if (!m_isMonitorJob) {
				// ??????????????????????????????
				// ??????????????????ID?????????????????????????????????
				// ??????/??????????????????true????????????????????????????????????ID???????????????
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() == 0) {
					return ret;
				}

				m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

				// ?????????????????????????????????????????????????????????
				nodeInfo = new HashMap<String, NodeInfo>();
				for (String facilityId : facilityList) {
					try {
						synchronized (this) {
							nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(facilityId));
						}
					} catch (FacilityNotFound e) {
						// ???????????????
					}
				}

				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				/**
				 * ???????????????
				 */
				// ??????????????????ID?????????????????????????????????????????????
				Iterator<String> itr = facilityList.iterator();
				while(itr.hasNext()){
					String facilityId = itr.next();
					if(facilityId != null && !"".equals(facilityId)){

						// ????????????????????????????????????RunMonitor??????????????????????????????????????????
						// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
						RunMonitorCorrelation runMonitor = this.createMonitorInstance();

						// ??????????????????????????????????????????????????????
						runMonitor.m_monitorTypeId = this.m_monitorTypeId;
						runMonitor.m_monitorId = this.m_monitorId;
						runMonitor.m_now = this.m_now;
						runMonitor.m_priorityMap = this.m_priorityMap;
						runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
						runMonitor.setJudgementInfo();
						runMonitor.setCheckInfo();
						runMonitor.nodeInfo = this.nodeInfo;

						ecs.submit(new MonitorMultipleExecuteTask(runMonitor, facilityId));
						taskCount++;
						
						if (m_log.isDebugEnabled()) {
							m_log.debug("starting monitor result : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
						}
					}
					else {
						facilityList.remove(facilityId);
					}
				}

			} else {
				// ????????????????????????
				// ??????????????????ID?????????????????????????????????
				// ??????/??????????????????true????????????????????????????????????ID???????????????
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() != 1
						|| !facilityList.get(0).equals(m_facilityId) ) {
					return ret;
				}

				m_isNode = true;

				// ?????????????????????????????????????????????????????????
				nodeInfo = new HashMap<String, NodeInfo>();
				try {
					synchronized (this) {
						nodeInfo.put(m_facilityId, new RepositoryControllerBean().getNode(m_facilityId));
					}
				} catch (FacilityNotFound e) {
					// ???????????????
				}
				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				/**
				 * ???????????????
				 */
				// ????????????????????????????????????RunMonitor??????????????????????????????????????????
				// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
				RunMonitorCorrelation runMonitor = this.createMonitorInstance();

				// ??????????????????????????????????????????????????????
				runMonitor.m_isMonitorJob = this.m_isMonitorJob;
				runMonitor.m_monitorTypeId = this.m_monitorTypeId;
				runMonitor.m_monitorId = this.m_monitorId;
				runMonitor.m_now = this.m_now;
				runMonitor.m_priorityMap = this.m_priorityMap;
				runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
				runMonitor.setJudgementInfo();
				runMonitor.setCheckInfo();
				runMonitor.nodeInfo = this.nodeInfo;
				runMonitor.m_prvData = this.m_prvData;

				ecs.submit(new MonitorMultipleExecuteTask(runMonitor, m_facilityId));
				taskCount++;

				if (m_log.isDebugEnabled()) {
					m_log.debug("starting monitor result : monitorId = " + m_monitorId + ", facilityId = " + m_facilityId);
				}
			}

			/**
			 * ?????????????????????
			 */
			List<MonitorRunResultInfo> resultList = new ArrayList<>();	// ?????????????????????

			m_log.debug("total start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			// ??????????????????????????????
			Sample sample = null;
			Date sampleTime = HinemosTime.getDateInstance();
			
			for (int i = 0; i < taskCount; i++) {
				Future<List<MonitorRunResultInfo>> future = ecs.take();
				resultList = future.get();	// ?????????????????????

				if (resultList == null || resultList.size() <= 0) {
					continue;
				}
				if (!m_isMonitorJob) {
					for (MonitorRunResultInfo result : resultList) {
						String facilityId = result.getFacilityId();
						m_nodeDate = result.getNodeDate();
						
						if (m_log.isDebugEnabled()) {
							m_log.debug("finished monitor : monitorId = " + m_monitorId + ", facilityId = " + facilityId);
						}
					
						// ??????????????????
						if(result.getProcessType().booleanValue()){
							if (m_monitor.getMonitorFlg()) {
								// ?????????????????????
								ret.add(createOutputBasicInfo(true, facilityId, result.getCheckResult(), new Date(m_nodeDate), result, m_monitor));
							}
							// ???????????????????????????
							if (m_monitor.getCollectorFlg()
									|| m_monitor.getPredictionFlg()
									|| m_monitor.getChangeFlg()) {

								// ??????????????????????????????????????????????????????
								CollectMonitorDataInfo collectMonitorDataInfo 
								= CollectMonitorManagerUtil.calculateChangePredict(
									this, 
									m_monitor, 
									facilityId, 
									result.getDisplayName(),
									m_monitor.getItemName(),
									sampleTime.getTime(),
									result.getValue());

								// ???????????????????????????????????????????????????????????????????????????
								Double average = null;
								Double standardDeviation = null;
								if (collectMonitorDataInfo != null) {
									if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
										// ????????????????????????
										MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
										ret.add(createOutputBasicInfo(true, facilityId, collectResult.getCheckResult(), 
												new Date(collectResult.getNodeDate()), collectResult, m_monitor));
									}
									if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
										// ???????????????????????????
										MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
										ret.add(createOutputBasicInfo(true, facilityId, collectResult.getCheckResult(), 
												new Date(collectResult.getNodeDate()), collectResult, m_monitor));
									}
									average = collectMonitorDataInfo.getAverage();
									standardDeviation = collectMonitorDataInfo.getStandardDeviation();
								}

								if (m_monitor.getCollectorFlg().booleanValue()) {
									sample = new Sample(sampleTime, m_monitor.getMonitorId());
									int errorType = -1;
									if(result.isCollectorResult()){
										errorType = CollectedDataErrorTypeConstant.NOT_ERROR;
									}else{
										errorType = CollectedDataErrorTypeConstant.UNKNOWN;
									}
									sample.set(facilityId, m_monitor.getItemName(), result.getValue(), 
											average, standardDeviation, errorType, result.getDisplayName());
									sampleList.add(sample);
								}
							}
						}
					}
				} else {
					m_monitorRunResultInfo = new MonitorRunResultInfo();
					m_monitorRunResultInfo.setPriority(resultList.get(0).getPriority());
					m_monitorRunResultInfo.setCheckResult(resultList.get(0).getCheckResult());
					m_monitorRunResultInfo.setNodeDate(m_nodeDate);
					m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(resultList.get(0).getMessageOrg(), resultList.get(0).getMessage()));
					m_monitorRunResultInfo.setCurData(resultList.get(0).getCurData());
				}
			}

			// ??????????????????????????????
			if(!sampleList.isEmpty()){
				CollectDataUtil.put(sampleList);
			}
			
			m_log.debug("monitor end : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			return ret;

		} catch (FacilityNotFound e) {
			throw e;
		} catch (InterruptedException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());

			throw new HinemosUnknown(e);
		} catch (ExecutionException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId  = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new HinemosUnknown(e);
		}
	}

	/* (non-Javadoc)
	 * ?????????????????????????????????
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// ?????????????????????????????????
		if (!m_isMonitorJob) {
			// ??????????????????????????????
			m_correlation = QueryUtil.getMonitorCorrelationInfoPK(m_monitorId);
		} else {
			// ????????????????????????
			m_correlation = QueryUtil.getMonitorCorrelationInfoPK(m_monitor.getMonitorId());
		}
		try {
			m_referFacilityIdList = new RepositoryControllerBean().getExecTargetFacilityIdList(
					m_correlation.getReferFacilityId(), m_monitor.getOwnerRoleId());
		} catch (HinemosUnknown e) {
			// ???????????????
		}
	}

	/* (??? Javadoc)
	 * ????????????????????????????????????
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		if(m_message == null || "".equals(m_message)){
			return m_unKnownMessage;
		}
		return m_message;
	}

	/* (??? Javadoc)
	 * ???????????????????????????????????????????????????
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		if (m_monitor == null || m_monitor.getCorrelationCheckInfo() == null) {
			return "";
		}
		String msgTargetItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getTargetItemName(), 
				m_monitor.getCorrelationCheckInfo().getTargetDisplayName(),
				m_monitor.getCorrelationCheckInfo().getTargetMonitorId());
		String msgReferItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getReferItemName(), 
				m_monitor.getCorrelationCheckInfo().getReferDisplayName(),
				m_monitor.getCorrelationCheckInfo().getReferMonitorId());
		String[] args = {
				msgTargetItemName,
				m_monitor.getCorrelationCheckInfo().getAnalysysRange().toString(),
				m_monitor.getCorrelationCheckInfo().getReferFacilityId(),
				msgReferItemName};
		/** ????????????????????? */
		return m_message + "\n" + MessageConstant.MESSAGE_MONITOR_ORGMSG_CORRELATION.getMessage(args) + "\n" + m_collectDataListMessage;
	}

	/* (??? Javadoc)
	 * ???????????????????????????????????????????????????(???????????????)
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#makeJobOrgMessage(java.lang.String, java.lang.String)
	 */
	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		String rtn = "";
		if (m_monitor == null || m_monitor.getCorrelationCheckInfo() == null) {
			return rtn;
		}
		String msgTargetItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getTargetItemName(), 
				m_monitor.getCorrelationCheckInfo().getTargetDisplayName(),
				m_monitor.getCorrelationCheckInfo().getTargetMonitorId());
		String msgReferItemName = AnalyticsUtil.getMsgItemName(
				m_monitor.getCorrelationCheckInfo().getReferItemName(), 
				m_monitor.getCorrelationCheckInfo().getReferDisplayName(),
				m_monitor.getCorrelationCheckInfo().getReferMonitorId());
		String[] args = {
				msgTargetItemName,
				m_monitor.getCorrelationCheckInfo().getAnalysysRange().toString(),
				m_monitor.getCorrelationCheckInfo().getReferFacilityId(),
				msgReferItemName};
		/** ????????????????????? */
		if (msg != null) {
			rtn = msg + "\n";
		}
		rtn += MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_CORRELATION.getMessage(args);
		return rtn;
	}
}
