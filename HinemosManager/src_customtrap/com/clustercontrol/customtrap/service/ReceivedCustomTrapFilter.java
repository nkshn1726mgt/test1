/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.calendar.factory.SelectCalendar;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.customtrap.bean.CustomTrap;
import com.clustercontrol.customtrap.bean.CustomTrap.Type;
import com.clustercontrol.customtrap.bean.CustomTraps;
import com.clustercontrol.customtrap.util.CustomTrapNotifier;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.hub.bean.CollectStringTag;
import com.clustercontrol.hub.bean.StringSample;
import com.clustercontrol.hub.bean.StringSampleTag;
import com.clustercontrol.hub.util.CollectStringDataUtil;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil;
import com.clustercontrol.monitor.run.util.MonitorJudgementInfoCache;
import com.clustercontrol.monitor.run.util.CollectMonitorManagerUtil.CollectMonitorDataInfo;
import com.clustercontrol.monitor.run.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.notify.util.NotifyRelationCache;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.bean.FacilityTreeAttributeConstant;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ???????????????????????????????????????????????????
 * 
 * @version 6.0.0
 * @since 6.0.0
 */
public class ReceivedCustomTrapFilter {

	private Logger logger = Logger.getLogger(this.getClass());
	private static Map<String, Double> resentDataMap = new HashMap<String, Double>();

	CustomTraps receivedCustomTraps;
	private CustomTrapNotifier notifier;
	private long notifiedCount = 0;
	private int resentDataMapMaxSize = 0;

	/**
	 * ?????????????????????
	 * 
	 * @param receivedCustomTraps	???????????????
	 * @param notifier				CustomTrapNotifier
	 * @param defaultCharset		????????????????????????
	 */
	public ReceivedCustomTrapFilter(CustomTraps receivedCustomTraps, CustomTrapNotifier notifier,
			Charset defaultCharset) {
		this.receivedCustomTraps = receivedCustomTraps;
		this.notifier = notifier;
		resentDataMapMaxSize = HinemosPropertyCommon.monitor_Customtrap_RecentData_Map_size.getIntegerValue();
		logger.info("monitor.Customtrap.RecentData.Map.initialCapacity=" + resentDataMapMaxSize);
	}

	/**
	 * ???????????????????????????????????????
	 */
	public void work() {
		logger.info("ReceivedCustomTrapFilter work");
		JpaTransactionManager tm = null;
		List<OutputBasicInfo> notifyInfoList = new ArrayList<>();

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			RepositoryControllerBean repositoryCtrl = new RepositoryControllerBean();
			List<String> matchedFacilityIdList = new ArrayList<String>();
			String agentAddr = receivedCustomTraps.getAgentAddr();
			String facilityId = receivedCustomTraps.getFacilityId();

			// ?????????????????????????????????
			if ((null == facilityId) || (facilityId.isEmpty())) {
				// IP????????????????????????????????????ID????????????????????????
				matchedFacilityIdList = repositoryCtrl.getFacilityIdByIpAddress(InetAddress.getByName(agentAddr));
				if (matchedFacilityIdList.size() == 0) {
					// FacilityNotFound
					matchedFacilityIdList.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
					logger.info("work() : UNREGISTERED_SCOPE agentAddr =" + agentAddr);
				}
			} else {
				try {
					if (repositoryCtrl.isNode(facilityId)) {
						matchedFacilityIdList.add(facilityId);
					} else {
						// ???????????????????????????
						logger.warn("work() : Scope is set to FacilityID [FacilityID=" + facilityId + "]");
						return;
					}
				} catch (FacilityNotFound e) {
					matchedFacilityIdList.add(FacilityTreeAttributeConstant.UNREGISTERED_SCOPE);
				}
			}

			List<Sample> collectedSamples = new ArrayList<>();
			/* ????????????????????? */
			// ?????????????????????????????????
			for (CustomTrap receivedCustomTrap : receivedCustomTraps.getCustomTraps()) {
				// ?????????????????????
				List<MonitorInfo> monitorList = null;
				switch (receivedCustomTrap.getType()) {
				case STRING: {
						monitorList = QueryUtil.getMonitorInfoByMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
					}
					break;
				case NUM: {
						monitorList = QueryUtil.getMonitorInfoByMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
					}
					break;
				}

				if (monitorList == null) {
					// ??????????????????????????????
					if (logger.isDebugEnabled()) {
						logger.info("work() : customtrap monitor not found. skip filtering. [" + receivedCustomTrap.toString()
								+ "]");
					}
					continue;
				}
				double value = 0;// ????????????
				String key = "";
				List<StringSample> collectedStringSamples = new ArrayList<>();
				Sample sample = null;
				StringSample stringSample = null;
				for (MonitorInfo monitor : monitorList) {
					// ???????????????????????????
					if (isNotInCalendar(monitor, receivedCustomTrap)) {
						logger.debug("work() : NotInCalender");
						continue;
					}

					// ??????????????????
					Pattern keyPattern = Pattern.compile(monitor.getCustomTrapCheckInfo().getTargetKey(),
							Pattern.DOTALL);
					Matcher matcherKeyPattern = keyPattern.matcher(receivedCustomTrap.getKey());
					if (!matcherKeyPattern.matches()) {
						logger.info("work() : KeyPattern Unmatched");
						continue;
					}

					// ????????????
					// ?????????????????????????????????Value??????sample/notify??????????????????
					if (receivedCustomTrap.getType() == Type.NUM) {
						value = Double.parseDouble(receivedCustomTrap.getMsg());
						key = monitor.getMonitorId() + ":" + receivedCustomTrap.getKey();
						sample = new Sample(new Date(receivedCustomTrap.getSampledTime()), monitor.getMonitorId());
						if (monitor.getCustomTrapCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
							Double oldData = null;
							logger.debug("work() : monitor.Customtrap.RecentData.Map.size=" + resentDataMap.size());
							synchronized (resentDataMap) {
								// ???????????????????????????????????????????????????????????????????????????
								oldData = resentDataMap.putIfAbsent(key, value);
								if (null == oldData) {
									// ??????????????????
									// ?????????????????????????????????????????????????????????[??????????????????????????????Custom??????????????????]
									logger.info("work() : No previous information No Monitoring and sampling!!");
									continue;
								}
								// ?????????????????????????????????????????????
								resentDataMap.replace(key, value);
							}
							if (resentDataMapMaxSize < resentDataMap.size()) {
								logger.warn("work() : CustomTrap Specified max size(" + resentDataMapMaxSize + ") < cache size("
										+ resentDataMap.size() + ")  ");
								// Internal Event
								String[] args = { String.valueOf(resentDataMap.size()),
										String.valueOf(resentDataMapMaxSize) };
								AplLogger.put(PriorityConstant.TYPE_CRITICAL,
										HinemosModuleConstant.MONITOR_CUSTOMTRAP_N,
										MessageConstant.MESSAGE_SYS_022_CUSTOM_TRAP_NUM_OVER, args);
							}
							// ??????????????????
							double prevValue = oldData.doubleValue();
							logger.info("work() : CustomTrapNum prev=" + prevValue + " value = " + value + " new value="
									+ (value - prevValue));
							value -= prevValue;
						}
					}

					List<String> validFacilityIdList = getValidFacilityIdList(matchedFacilityIdList, monitor);

					// ??????????????????????????????????????????????????????????????????????????????????????????
					if (monitor.getCollectorFlg()
							|| monitor.getPredictionFlg() 
							|| monitor.getChangeFlg()) {
						List<CustomTrap> customtrapListBuffer = new ArrayList<CustomTrap>();
						List<MonitorRunResultInfo> collectResultBuffer = new ArrayList<>();
						for (String facilityIdElement : validFacilityIdList) {
							switch (receivedCustomTrap.getType()) {
							case STRING: {
								stringSample = new StringSample(new Date(receivedCustomTrap.getSampledTime()), monitor.getMonitorId());

								// ??????????????????
								List<StringSampleTag> tags = new ArrayList<>();
								if (receivedCustomTrap.getDate() != null) {
									StringSampleTag tagDate = new StringSampleTag(
											CollectStringTag.TIMESTAMP_IN_LOG, Long.toString(receivedCustomTrap.getSampledTime()));
									tags.add(tagDate);
								}
								StringSampleTag tagType = new StringSampleTag(CollectStringTag.TYPE, receivedCustomTrap.getType().name());
								tags.add(tagType);
								StringSampleTag tagKey = new StringSampleTag(CollectStringTag.KEY, receivedCustomTrap.getKey());
								tags.add(tagKey);
								StringSampleTag tagMsg = new StringSampleTag(CollectStringTag.MSG, receivedCustomTrap.getMsg());
								tags.add(tagMsg);
								StringSampleTag tagFacility = new StringSampleTag(CollectStringTag.FacilityID, facilityIdElement);
								tags.add(tagFacility);

								// ?????????????????????
								stringSample.set(facilityIdElement, "customtrap", receivedCustomTrap.getOrgMsg(), tags);

								collectedStringSamples.add(stringSample);
								break;
							}
							case NUM: {
								boolean overlapCheck = false;
								// key?????????????????????
								for (Sample cSample : collectedSamples){
									// ????????????????????????????????????collectedSamples???1??????????????????perfData???1??????????????????????????????
									if (cSample.getMonitorId().equals(monitor.getMonitorId())
											&& cSample.getDateTime().getTime() == receivedCustomTrap.getSampledTime()
											&& cSample.getPerfDataList().get(0).getFacilityId().equals(facilityIdElement)
											&& cSample.getPerfDataList().get(0).getDisplayName().equals(key)
											&& cSample.getPerfDataList().get(0).getItemName().equals(monitor.getItemName())) {
										overlapCheck = true;
										break;
									}
								}
								if (!overlapCheck) {
									String displayName = null;
									if (receivedCustomTrap.getKey() != null) {
										displayName = receivedCustomTrap.getKey();
									}

									// ??????????????????????????????????????????????????????
									CollectMonitorDataInfo collectMonitorDataInfo 
										= CollectMonitorManagerUtil.calculateChangePredict(
											null, monitor, facilityIdElement, displayName, monitor.getItemName(), 
											receivedCustomTrap.getSampledTime(), value);

									// ???????????????????????????????????????????????????????????????????????????
									Double average = null;
									Double standardDeviation = null;
									if (collectMonitorDataInfo != null) {
										if (collectMonitorDataInfo.getChangeMonitorRunResultInfo() != null) {
											// ????????????????????????
											MonitorRunResultInfo collectResult = collectMonitorDataInfo.getChangeMonitorRunResultInfo();
											customtrapListBuffer.add(receivedCustomTrap);
											collectResultBuffer.add(collectResult);
											countupNotified();
										}
										if (collectMonitorDataInfo.getPredictionMonitorRunResultInfo() != null) {
											// ???????????????????????????
											MonitorRunResultInfo collectResult = collectMonitorDataInfo.getPredictionMonitorRunResultInfo();
											customtrapListBuffer.add(receivedCustomTrap);
											collectResultBuffer.add(collectResult);
											countupNotified();
										}
										average = collectMonitorDataInfo.getAverage();
										standardDeviation = collectMonitorDataInfo.getStandardDeviation();
									}
									notifyInfoList.addAll(notifier.createPredictionOutputBasicInfoList(
											customtrapListBuffer, monitor, agentAddr, collectResultBuffer));
									
									if (monitor.getCollectorFlg()) {
										if (receivedCustomTrap.getKey() != null) {
											sample.set(facilityIdElement, monitor.getItemName(), value,
													average, standardDeviation,
													CollectedDataErrorTypeConstant.NOT_ERROR, receivedCustomTrap.getKey());
										} else {
											sample.set(facilityIdElement, monitor.getItemName(), value,
													average, standardDeviation,
													CollectedDataErrorTypeConstant.NOT_ERROR);
										}
										collectedSamples.add(sample);
									}
								}
								break;
							}
							}
						}
					} else {
						logger.debug("work() : CustomTrap CollectorFlg==false");
					}

					// ????????????
					if (monitor.getMonitorFlg()) {
						// ???????????????????????????????????????????????????
						List<NotifyRelationInfo> notifyRelationList 
							= NotifyRelationCache.getNotifyList(monitor.getNotifyGroupId());
						if (notifyRelationList == null || notifyRelationList.size() == 0) {
							logger.info("work() : notifyRelationList.size() == 0");
							continue;
						}

						List<CustomTrap> customtrapListBuffer = new ArrayList<CustomTrap>();
						List<String> facilityIdListBuffer = new ArrayList<String>();

						List<MonitorStringValueInfo> ruleListBuffer = new ArrayList<MonitorStringValueInfo>();
						List<Integer> priorityBuffer = new ArrayList<Integer>();
						int orderNo = 0;

						switch (receivedCustomTrap.getType()) {
						case STRING: {
							// ???????????????????????????
							for (MonitorStringValueInfo rule : monitor.getStringValueInfo()) {
								++orderNo;
								if (logger.isDebugEnabled()) {
									logger.info(String.format(
											"work() : monitoring (monitorId = %s, orderNo = %d, patten = %s, enabled = %s, casesensitive = %s)",
											monitor.getMonitorId(), orderNo, rule.getPattern(), rule.getValidFlg(),
											rule.getCaseSensitivityFlg()));
								}
								if (!rule.getValidFlg()) {
									// ??????????????????????????????????????????????????????
									logger.debug("work() : CustomTrap !rule.getValidFlg()");
									continue;
								}
								// ??????????????????????????????
								if (logger.isDebugEnabled()) {
									logger.debug(String.format("work() : filtering customtrap (regex = %s, customtrap = %s",
											rule.getPattern(), receivedCustomTrap));
								}
								try {
									Pattern pattern = null;
									if (rule.getCaseSensitivityFlg()) {
										// ?????????????????????????????????????????????
										pattern = Pattern.compile(rule.getPattern(),
												Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
									} else {
										// ??????????????????????????????????????????
										pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL);
									}

									Matcher matcher = pattern.matcher(receivedCustomTrap.getMsg());
									if (matcher.matches()) {
										if (rule.getProcessType()) {
											logger.debug(String.format("work() : matched (regex = %s, CustomTrap = %s",
													rule.getPattern(), receivedCustomTrap));
											for (String facilityIdElement : validFacilityIdList) {
												customtrapListBuffer.add(receivedCustomTrap);
												ruleListBuffer.add(rule);
												priorityBuffer.add(rule.getPriority());
												facilityIdListBuffer.add(facilityIdElement);
												countupNotified();
											}
										} else {
											logger.debug(String.format("work() : CustomTrap not ProcessType (regex = %s, CustomTrap = %s",
													rule.getPattern(), receivedCustomTrap));
										}
										break;
									} else {
										logger.debug("work() : CustomTrap rule not match rule = " + rule.getPattern());
									}
								} catch (Exception e) {
									logger.warn("work() : filtering failure. (regex = " + rule.getPattern() + ") . "
											+ e.getMessage(), e);
								}
							}

							logger.info("work() : CustomTrap Notify ValueType.string " + customtrapListBuffer.size() + "data");
							notifyInfoList.addAll(notifier.createStringOutputBasicInfoList(
									customtrapListBuffer, monitor, priorityBuffer, ruleListBuffer,
									facilityIdListBuffer, agentAddr, null));
						}
							break;
						case NUM: {
							// ????????????????????????
							List<Double> valueBuffer = new ArrayList<Double>();
							TreeMap<Integer, MonitorJudgementInfo> thresholds = 
									MonitorJudgementInfoCache.getMonitorJudgementMap(
											monitor.getMonitorId(), MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_BASIC.getType());
							int priority = judgePriority(value, thresholds, receivedCustomTrap);
							for (String facilityIdElement : validFacilityIdList) {
								customtrapListBuffer.add(receivedCustomTrap);
								facilityIdListBuffer.add(facilityIdElement);
								priorityBuffer.add(priority);
								valueBuffer.add(value);
								countupNotified();
							}
							logger.info("work() : CustomTrap Notify ValueType.num " + customtrapListBuffer.size() + "data");
							notifyInfoList.addAll(notifier.createNumOutputBasicInfoList(customtrapListBuffer, monitor, priorityBuffer, facilityIdListBuffer,
									agentAddr, valueBuffer, null));
						}
							break;
						}
					}
				}
				// DB??????
				if (!collectedStringSamples.isEmpty()) {
					logger.debug("work() : CustomTrap collectedStringSamples " + collectedStringSamples.size() + "data");
					CollectStringDataUtil.store(collectedStringSamples);
				}
			}

			// DB??????(??????)
			if (!collectedSamples.isEmpty()) {
				logger.debug("work() : CustomTrap collectedSamples " + collectedSamples.size() + "data");
				CollectDataUtil.put(collectedSamples);
			}

			/* ??????????????? */
			// ?????????????????????????????????
			for (CustomTrap receivedCustomTrap : receivedCustomTraps.getCustomTraps()) {
				// ?????????????????????
				Map<RunInstructionInfo, MonitorInfo> monitorJobMap = null;
				switch (receivedCustomTrap.getType()) {
				case STRING: {
					monitorJobMap = MonitorJobWorker.getMonitorJobMap(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
				}
					break;
				case NUM: {
					monitorJobMap = MonitorJobWorker.getMonitorJobMap(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
				}
					break;
				}

				if (monitorJobMap == null) {
					// ??????????????????????????????
					if (logger.isDebugEnabled()) {
						logger.info("customtrap job monitor not found. skip filtering. [" + receivedCustomTrap.toString()
								+ "]");
					}
					continue;
				}
				double value = 0;// ????????????
				for (Map.Entry<RunInstructionInfo, MonitorInfo> entry : monitorJobMap.entrySet()) {
					if (!isMatchFacilityIdList(matchedFacilityIdList, entry.getKey().getFacilityId())) {
						// ????????????????????????????????????
						continue;
					}
					// ??????????????????
					Pattern keyPattern = Pattern.compile(entry.getValue().getCustomTrapCheckInfo().getTargetKey(),
							Pattern.DOTALL);
					Matcher matcherKeyPattern = keyPattern.matcher(receivedCustomTrap.getKey());
					if (!matcherKeyPattern.matches()) {
						logger.info("KeyPattern Unmatched");
						continue;
					}

					// ????????????
					// ?????????????????????????????????Value??????sample/notify??????????????????
					if (receivedCustomTrap.getType() == Type.NUM) {
						value = Double.parseDouble(receivedCustomTrap.getMsg());
						String key = receivedCustomTrap.getKey();
						if (entry.getValue().getCustomTrapCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
							// ???????????????????????????????????????????????????????????????????????????
							// ??????????????????
							Object oldData = MonitorJobWorker.getPrevMonitorValue(entry.getKey());
							if (oldData == null) {
								// ??????????????????
								Map<String, Double> map = new ConcurrentHashMap<>();
								map.put(key, value);
								MonitorJobWorker.addPrevMonitorValue(entry.getKey(), map);
								// ?????????????????????????????????????????????????????????
								logger.info("No previous information No Monitoring and sampling!!");
								continue;
							} else {
								@SuppressWarnings("unchecked")
								Map<String, Double> map = (Map<String, Double>)oldData;
								if (map.get(key) == null) {
									// ????????????????????????????????????????????????????????????
									map.put(key, value);
									MonitorJobWorker.addPrevMonitorValue(entry.getKey(), map);
									// ?????????????????????????????????????????????????????????
									logger.info("No previous information No Monitoring and sampling!!");
									continue;
								} else {
									// ??????????????????
									double prevValue = ((Double)map.get(key)).doubleValue();
									logger.info("CustomTrapNum prev=" + prevValue + " value = " + value + " new value="
											+ (value - prevValue));
									value -= prevValue;
								}
							}
						}
					}

					// ????????????
					List<CustomTrap> customtrapListBuffer = new ArrayList<CustomTrap>();
					List<String> facilityIdListBuffer = new ArrayList<String>();

					List<MonitorStringValueInfo> ruleListBuffer = new ArrayList<MonitorStringValueInfo>();
					List<Integer> priorityBuffer = new ArrayList<Integer>();

					switch (receivedCustomTrap.getType()) {
					case STRING: {
						// ???????????????????????????
						for (MonitorStringValueInfo rule : entry.getValue().getStringValueInfo()) {
							if (!rule.getValidFlg()) {
								// ??????????????????????????????????????????????????????
								logger.debug("CustomTrap !rule.getValidFlg()");
								continue;
							}
							// ??????????????????????????????
							if (logger.isDebugEnabled()) {
								logger.debug(String.format("filtering customtrap (regex = %s, customtrap = %s",
										rule.getPattern(), receivedCustomTrap));
							}
							try {
								Pattern pattern = null;
								if (rule.getCaseSensitivityFlg()) {
									// ?????????????????????????????????????????????
									pattern = Pattern.compile(rule.getPattern(),
											Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
								} else {
									// ??????????????????????????????????????????
									pattern = Pattern.compile(rule.getPattern(), Pattern.DOTALL);
								}

								Matcher matcher = pattern.matcher(receivedCustomTrap.getMsg());
								if (matcher.matches()) {
									if (rule.getProcessType()) {
										logger.debug(String.format("matched (regex = %s, CustomTrap = %s",
												rule.getPattern(), receivedCustomTrap));
										customtrapListBuffer.add(receivedCustomTrap);
										ruleListBuffer.add(rule);
										priorityBuffer.add(rule.getPriority());
										facilityIdListBuffer.add(entry.getKey().getFacilityId());
										countupNotified();
									} else {
										logger.debug(String.format("CustomTrap not ProcessType (regex = %s, CustomTrap = %s",
												rule.getPattern(), receivedCustomTrap));
									}
									break;
								} else {
									logger.debug("CustomTrap rule not match rule = " + rule.getPattern());
								}
							} catch (Exception e) {
								logger.warn("filtering failure. (regex = " + rule.getPattern() + ") . "
										+ e.getMessage(), e);
							}
						}

						logger.info("CustomTrap Notify ValueType.string " + customtrapListBuffer.size() + "data");
						notifyInfoList.addAll(notifier.createStringOutputBasicInfoList(
								customtrapListBuffer, entry.getValue(), priorityBuffer, ruleListBuffer,
								facilityIdListBuffer, agentAddr, entry.getKey()));
					}
						break;
					case NUM: {
						// ????????????????????????
						List<Double> valueBuffer = new ArrayList<Double>();
						TreeMap<Integer, MonitorJudgementInfo> thresholds 
							 = MonitorJudgementInfoCache.getMonitorJudgementMap(
							 entry.getValue().getMonitorId(), MonitorTypeConstant.TYPE_NUMERIC, MonitorNumericType.TYPE_BASIC.getType());
						int priority = judgePriority(value, thresholds, receivedCustomTrap);
						customtrapListBuffer.add(receivedCustomTrap);
						facilityIdListBuffer.add(entry.getKey().getFacilityId());
						priorityBuffer.add(priority);
						valueBuffer.add(value);

						logger.info("CustomTrap Notify ValueType.num " + customtrapListBuffer.size() + "data");
						notifyInfoList.addAll(notifier.createNumOutputBasicInfoList(
								customtrapListBuffer, entry.getValue(), priorityBuffer, facilityIdListBuffer,
								agentAddr, valueBuffer, entry.getKey()));
					}
						break;
					}
				}
			}

			// ????????????
			tm.addCallback(new NotifyCallback(notifyInfoList));

			tm.commit();
		} catch (HinemosUnknown | UnknownHostException e) {
			e.printStackTrace();
			logger.warn("work() : unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// HA????????????????????????????????????????????????????????????
			throw new RuntimeException(
					"unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("work() : CustomTrap data error " + e.getMessage());
			// HA????????????????????????????????????????????????????????????
			throw new RuntimeException(
					"unexpected internal error. : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		} finally {
			if (tm != null) {
				tm.close();
			}
		}
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param value
	 *            ?????????
	 * @param thresholds
	 *            ??????????????????
	 * @param customTrap
	 *            ????????????????????????
	 * @return ?????????????????????
	 * @throws CustomInvalid
	 */
	private int judgePriority(Double value, TreeMap<Integer, MonitorJudgementInfo> thresholds, CustomTrap customTrap)
			throws CustomInvalid {
		// Local Variables
		int priority = PriorityConstant.TYPE_UNKNOWN;

		// MAIN
		if (Double.isNaN(value)) {
			// if user defined not a number
			priority = PriorityConstant.TYPE_UNKNOWN;
		} else {
			// if numeric value is defined
			if (thresholds.containsKey(PriorityConstant.TYPE_INFO)
					&& thresholds.containsKey(PriorityConstant.TYPE_WARNING)) {
				if (value >= thresholds.get(PriorityConstant.TYPE_INFO).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_INFO).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_INFO;
				} else if (value >= thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdLowerLimit()
						&& value < thresholds.get(PriorityConstant.TYPE_WARNING).getThresholdUpperLimit()) {
					return PriorityConstant.TYPE_WARNING;
				} else {
					priority = PriorityConstant.TYPE_CRITICAL;
				}
			} else {
				// if threshold is not defined
				CustomInvalid e = new CustomInvalid(
						"configuration of CustomTrap monitor is not valid. [" + customTrap + "]");
				logger.info("judgePriority() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		return priority;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param facilityIdList
	 *            ????????????????????????
	 * @param monitor
	 *            ????????????????????????
	 * @return ???????????????????????????????????????????????????????????????
	 */
	private List<String> getValidFacilityIdList(List<String> facilityIdList, MonitorInfo monitor) {
		List<String> validFacilityIdList = new ArrayList<String>();
		for (String facilityId : facilityIdList) {

			if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
				if (!FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(monitor.getFacilityId())) {
					// ???????????????????????????????????????CustomTrap?????????????????????????????????????????????????????????????????????????????????
					continue;
				}
			} else {
				if (!new RepositoryControllerBean().containsFaciliyId(monitor.getFacilityId(), facilityId,
						monitor.getOwnerRoleId())) {
					// CustomTrap????????????????????????????????????????????????????????????????????????????????????????????????
					continue;
				}
			}

			validFacilityIdList.add(facilityId);

		}
		return validFacilityIdList;
	}


	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param facilityIdList
	 *            ????????????????????????
	 * @param facilityId ?????????????????????????????????ID
	 *            ????????????????????????
	 * @return true????????????false????????????
	 */
	private boolean isMatchFacilityIdList(List<String> facilityIdList, String monitorJobFacilityId) {
		for (String facilityId : facilityIdList) {
			if (FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(facilityId)) {
				if (!FacilityTreeAttributeConstant.UNREGISTERED_SCOPE.equals(monitorJobFacilityId)) {
					// ???????????????????????????????????????CustomTrap?????????????????????????????????????????????????????????????????????????????????
					continue;
				}
			} else {
				if (facilityId.equals(monitorJobFacilityId)) {
					// ????????????
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param monitor
	 *            ????????????????????????
	 * @param recievedCustomTrap
	 *            ????????????????????????????????????
	 * @return ??????????????????????????????????????????true
	 */
	private boolean isNotInCalendar(MonitorInfo monitor, CustomTrap recievedCustomTrap) {
		boolean notInCalendar = false;

		// ????????????????????????????????????????????????
		if (monitor.getCalendarId() != null && monitor.getCalendarId().length() > 0) {
			try {
				boolean run = new SelectCalendar().isRun(monitor.getCalendarId(), recievedCustomTrap.getSampledTime());
				notInCalendar = !run;
			} catch (CalendarNotFound e) {
				logger.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ")");
			} catch (InvalidRole e) {
				logger.warn("calendar not found (calendarId = " + monitor.getCalendarId() + ") ," + e.getMessage());
			}

			// ???????????????????????????????????????
			if (notInCalendar) {
				if (logger.isDebugEnabled()) {
					logger.debug("skip monitoring because of calendar. (monitorId = " + monitor.getMonitorId()
							+ ", calendarId = " + monitor.getCalendarId() + ")");
				}
			}
		}
		return notInCalendar;
	}

	private synchronized void countupNotified() {
		notifiedCount = notifiedCount >= Long.MAX_VALUE ? 0 : notifiedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_customtrap_stats_interval.getIntegerValue();
		logger.info("monitor.customtrap.stats.interval = " + _statsInterval);
		if (notifiedCount % _statsInterval == 0) {
			logger.info("The number of CustomTrap (notified) : " + notifiedCount);
		}
	}
}
