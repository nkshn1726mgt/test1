/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.collect;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.collect.session.CollectControllerBean;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.session.PerformanceCollectMasterControllerBean;
import com.clustercontrol.platform.HinemosPropertyDefault;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.util.ArrayListInfo;
import com.clustercontrol.ws.util.HashMapInfo;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * ????????????WebAPI?????????????????????
 */
@MTOM
@javax.jws.WebService(targetNamespace = "http://collect.ws.clustercontrol.com")
public class CollectEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog( CollectEndpoint.class );
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	
	private static boolean debug = false;//debug??????????????????true???????????????????????????false????????????????????????
	
	/**
	 * echo(Web????????????API?????????)
	 *
	 * ????????????????????????????????????????????????????????????
	 *
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}
	
	
	/**
	 * 
	 * ??????????????????ID????????????????????????ID????????????????????????HashMap??????????????????
	 *
	 * CollectRead???????????????
	 *
	 * @param itemCode ?????????????????????
	 * @param displayName ?????????(??????????????????)
	 * @param facilityIdList ??????????????????ID????????????
	 * @return ??????????????????ID????????????????????????ID????????????????????????HashMap
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public HashMapInfo getCollectId(String itemName, String displayName, String monitorId, List<String> facilityIdList) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getCollectId");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// ????????????????????????
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getCollectId, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		HashMap<String, Integer> map = new HashMap<>();
		//???????????????
		if(debug){
			for (String facilityId : facilityIdList) {
				int id = 0;
				id += itemName.hashCode();
				id *= 37;
				if (displayName != null) {
					id += displayName.hashCode();
				}
				id *= 37;
				id += facilityId.hashCode();
				map.put(facilityId, id); 
			}
		}else{
			for(String facilityId : facilityIdList){
				m_log.debug("itemName:" + itemName + ", displayName:"+displayName + ", monitorId:" + monitorId + ", facilityId:" + facilityId);
				Integer id = null;
				try {
					id = new CollectControllerBean().getCollectId(itemName, displayName, monitorId, facilityId);
				} catch (Exception e) {
					m_log.debug(e.getClass().getName() + ", itemName:" + itemName + ", displayName:"+displayName + 
							", monitorId:" + monitorId + ", facilityId:" + facilityId);
				}
				map.put(facilityId, id);
			}
		}
		HashMapInfo ret = new HashMapInfo();
		ret.setMap4(map);
		
		return ret;
	}
	
	
	/**
	 * 
	 * ??????ID????????????????????????ID?????????????????????????????????????????????HashMap??????????????????
	 *
	 * CollectRead???????????????
	 *
	 * @param idList ??????ID????????????
	 * @param summaryType ??????????????????
	 * @param fromTime ??????????????????????????????(??????)
	 * @param toTime ??????????????????????????????(??????)
	 * @return ??????ID????????????????????????ID?????????????????????????????????????????????HashMap
	 * @throws HinemosDbTimeout
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public HashMapInfo getCollectData (List<Integer> idList, Integer summaryType, Long fromTime, Long toTime) throws HinemosDbTimeout, InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getCollectData");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// ????????????????????????
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getCollectData, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		HashMap<Integer, ArrayListInfo> map = new HashMap<>();

		m_log.debug("getCollectData start"); // debug
		long start = HinemosTime.currentTimeMillis();
		//???????????????
		if(debug){
			int count = 0;
			int span = 60 * 1000;
				switch (summaryType) {
				case SummaryTypeConstant.TYPE_RAW :
					span *= 5; break; // 5???
				case SummaryTypeConstant.TYPE_AVG_HOUR: 
					span *= 12; break; // 1??????
				case SummaryTypeConstant.TYPE_MIN_HOUR: 
					span *= 12; break; // 1??????
				case SummaryTypeConstant.TYPE_MAX_HOUR: 
					span *= 12; break; // 1??????
				case SummaryTypeConstant.TYPE_AVG_DAY : 
					span *= 12 * 24; break; // 1???
				case SummaryTypeConstant.TYPE_MIN_DAY : 
					span *= 12 * 24; break; // 1???
				case SummaryTypeConstant.TYPE_MAX_DAY : 
					span *= 12 * 24; break; // 1???
				case SummaryTypeConstant.TYPE_AVG_MONTH : 
					span *= 12 * 24 * 30; break; // 1??????
				case SummaryTypeConstant.TYPE_MIN_MONTH : 
					span *= 12 * 24 * 30; break; // 1??????
				case SummaryTypeConstant.TYPE_MAX_MONTH : 
					span *= 12 * 24 * 30; break; // 1??????
				default :
					break;
				}
			for (Integer id : idList) {
				ArrayListInfo list = new ArrayListInfo();
				// span???????????????????????????????????????????????????
				long time = fromTime / span * span; // ????????????????????????
				while (true) {
					time += span;
					if (toTime < time) {
						break;
					}
					double tmp = (Math.random()*10);
					CollectData data = new CollectData();
					data.setTime(time);
					data.setValue((float)tmp);
					list.getList().add(data);
					count ++;
					m_log.debug("id:" + id + ", time:" + time + ", value:" + tmp);
				}
				map.put(id, list);
			}
			try {
				Thread.sleep(count / 10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			//??????????????????
			CollectControllerBean controller = new CollectControllerBean();
			
			//??????????????????????????????????????????????????????(raw)???????????????????????????
			switch(summaryType){
				case SummaryTypeConstant.TYPE_AVG_HOUR: {	
					ArrayListInfo list;
					List<SummaryHour> summaryList = controller.getSummaryHourList(idList, fromTime, toTime);
					for (SummaryHour summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getAvg());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
						}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_HOUR: {	
					ArrayListInfo list;
					List<SummaryHour> summaryList = controller.getSummaryHourList(idList, fromTime, toTime);
					for (SummaryHour summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMin());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_HOUR: {	
					ArrayListInfo list;
					List<SummaryHour> summaryHourList = controller.getSummaryHourList(idList, fromTime, toTime);
					for (SummaryHour summary : summaryHourList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMax());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_AVG_DAY: {
					ArrayListInfo list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(idList, fromTime, toTime);
					for (SummaryDay summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getAvg());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_DAY: {
					ArrayListInfo list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(idList, fromTime, toTime);
					for (SummaryDay summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMin());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_DAY: {
					ArrayListInfo list;
					List<SummaryDay> summaryList = controller.getSummaryDayList(idList, fromTime, toTime);
					for (SummaryDay summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMax());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_AVG_MONTH: {
					ArrayListInfo list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(idList, fromTime, toTime);
					for (SummaryMonth summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getAvg());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MIN_MONTH: {
					ArrayListInfo list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(idList, fromTime, toTime);
					for (SummaryMonth summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMin());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				case SummaryTypeConstant.TYPE_MAX_MONTH: {
					ArrayListInfo list;
					List<SummaryMonth> summaryList = controller.getSummaryMonthList(idList, fromTime, toTime);
					for (SummaryMonth summary : summaryList) {
						CollectData data = new CollectData();
						data.setId(summary.getId());
						data.setTime(summary.getTime());
						data.setValue(summary.getMax());
						data.setAverage(summary.getAverageAvg());
						data.setStandardDeviation(summary.getStandardDeviationAvg());
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
				default: { // default???RAW?????????
					ArrayListInfo list;
					List<CollectData> dataList = controller.getCollectDataList(idList, fromTime, toTime);
					for(CollectData data : dataList){
						if (map.get(data.getId().getCollectorid()) == null) {
							list = new ArrayListInfo();
							map.put(data.getId().getCollectorid(), list);
						}
						map.get(data.getId().getCollectorid()).getList().add(data);
					}
					break;
				}
			}
		}
		
		if (m_log.isInfoEnabled()) { // debug
			int size = 0;
			for (Map.Entry<Integer, ArrayListInfo> entry : map.entrySet()) {
				size += entry.getValue().size();
			}
			long difftime = HinemosTime.currentTimeMillis() - start;
			if (difftime > 5 * 1000) {
				m_log.info("getCollectData end   size=" + size + ", " + difftime + "ms"); // debug
			}
		}
		HashMapInfo ret = new HashMapInfo();
		ret.setMap3(map);
		return ret;
	}

	/**
	 * 
	 * ???????????????????????????????????????????????????
	 *
	 * CollectRead???????????????
	 *
	 * @param facilityIdList ??????????????????ID?????????
	 * @return ?????????????????????????????????
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	public List<CollectKeyInfoPK> getItemCodeList (List<String> facilityIdList) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		m_log.debug("getItemCodeList");

		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		
		// ????????????????????????
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get, Method=getItemCodeList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		List<CollectKeyInfoPK>ret = new CollectControllerBean().getItemCode(facilityIdList);
		return ret;
	}
	
	/**
	 * ???????????????DL???????????????????????????
	 * ??????????????????????????????ID?????????????????????????????????????????????????????????????????????1??????????????????CSV??????????????????????????????
	 * ??????????????????????????????????????????Hinemos??????????????????????????????????????????????????????????????????????????????????????????????????????
	 *
	 * CollectRead???????????????
	 *
	 *
	 * @param facilityidList
	 * @param summaryType
	 * @param item_codeList
	 * @param header
	 * @param archive
	 * @return
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	
	public List<String> createPerfFile(HashMapInfo map1,
			List<CollectKeyInfoPK> collectKeyInfoList,
			List<String> facilityList,
			Integer summaryType,
			String localeStr,
			boolean header,
			String defaultDateStr) throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("createPerfFile()");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		List<String> ret = null;
		
		TreeMap<String, String>facilityIdNameMap = map1.getMap6();
		
		// ????????????????????????
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityIdNameMap=");
		msg.append(facilityIdNameMap);
		msg.append(", collectKeyInfoList=");
		msg.append(collectKeyInfoList.toString());
		msg.append(", facilityList=");
		msg.append(facilityList.toString());
		msg.append(", SummaryType=");
		msg.append(summaryType);
		msg.append(", LocaleStr=");
		msg.append(localeStr);
		msg.append(", Header=");
		msg.append(header);
		msg.append(", defaultDateStr=");
		msg.append(defaultDateStr);

		try {
			ret = new CollectControllerBean().createPerfFile(facilityIdNameMap, facilityList, collectKeyInfoList, 
					summaryType, localeStr, header, defaultDateStr);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=createPerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=createPerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		return ret;
	}

	/**
	 * ?????????????????????????????????DL??????
	 *
	 * CollectRead???????????????
	 *
	 * @param filepath
	 * @return
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 * @throws HinemosUnknown
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadPerfFile(String fileName) throws InvalidUserPass, InvalidRole, HinemosUnknown
	{
		m_log.debug("downloadPerfFile() fileName = " + fileName);
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ????????????????????????
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(fileName);

		String exportDirectory = HinemosPropertyDefault.performance_export_dir.getStringValue();
		File file = new File(exportDirectory + fileName);
		if(!file.exists()) {
			m_log.info("file is not found : " + exportDirectory + fileName);
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=downloadPerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			return null;
		}
		m_log.info("file is found : " + exportDirectory + fileName);
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=downloadPerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());

		FileDataSource source = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}

	/**
	 * ?????????????????????????????????????????????
	 *
	 * CollectREAD???????????????
	 *
	 * @param filepathList
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public void deletePerfFile(ArrayList<String> fileNameList) throws HinemosUnknown, InvalidUserPass, InvalidRole
	{
		m_log.debug("deletePerfFile() fileNameList.size = " + fileNameList.size());
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// ????????????????????????
		StringBuffer msg = new StringBuffer();
		msg.append(", FileName=");
		msg.append(Arrays.toString(fileNameList.toArray()));

		try {
			new CollectControllerBean().deletePerfFile(fileNameList);
		} catch (Exception e) {
			m_opelog.warn(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download Failed, Method=deletePerfFile, User="
					+ HttpAuthenticator.getUserAccountString(wsctx)
					+ msg.toString());
			throw e;
		}
		m_opelog.info(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Download, Method=deletePerfFile, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
	}
	
	public HashMapInfo getEventDataMap (ArrayList<String> facilityIdList) throws HinemosUnknown {
		HashMapInfo ret = new HashMapInfo();
		HashMap<String, ArrayListInfo> map = new HashMap<>();
		HashMap<String, ArrayList<EventDataInfo>> map1 = new CollectControllerBean().getEventDataMap(facilityIdList);
		for (Entry<String, ArrayList<EventDataInfo>> e : map1.entrySet()) {
			ArrayListInfo list = new ArrayListInfo();
			list.setList3(e.getValue());
			map.put(e.getKey(), list);
		}
		ret.setMap7(map);
		return ret;
	}
	
	/**
	 * ??????????????????????????????????????????
	 * 
	 * @return ??????????????????????????????
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public ArrayList<CollectorItemCodeMstData> getCollectItemCodeMasterList() throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCollectItemCodeMasterList");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.COLLECT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		// ????????????????????????
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get Master, Method=getCollectItemCodeMasterList, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));
		return new PerformanceCollectMasterControllerBean().getCollectItemCodeMasterList();
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????
	 *???????????????????????????ID???????????????
	 *??????????????????
	 *???????????????????????????????????????ID???????????????????????????????????????????????????
	 *?????????????????????????????????????????????
	 *???????????????MonitorSetting???READ??????
	 * 
	 * @param facilityId?????????????????????ID
	 * @param ownerRoleId????????????????????????ID
	 * @return Map(??????, ????????????????????????)
	 * @throws HinemosUnknown
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 */
	public HashMapInfo getCollectKeyMapForAnalytics(String facilityId, String ownerRoleId)
			throws HinemosUnknown, InvalidUserPass, InvalidRole {
		m_log.debug("getCollectKeyMapForAnalytics");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.MONITOR_SETTING, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);
		// ????????????????????????
		StringBuffer msg = new StringBuffer();
		msg.append(", FacilityID=");
		msg.append(facilityId);
		msg.append(", OwnerRoleID=");
		msg.append(ownerRoleId);
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_PERFORMANCE + " Get Master, Method=getCollectKeyMapForAnalytics, User="
				+ HttpAuthenticator.getUserAccountString(wsctx)
				+ msg.toString());
		HashMapInfo info = new HashMapInfo();
		info.getMap9().putAll(new CollectControllerBean().getCollectKeyMapForAnalytics(facilityId, ownerRoleId));
		return info;
	}
	
	public Double[] getCoefficients(String monitorId, String facilityId, String displayName, String itemName) {
		return MonitorCollectDataCache.getCoefficients(monitorId, facilityId, displayName, itemName);
	}
}
