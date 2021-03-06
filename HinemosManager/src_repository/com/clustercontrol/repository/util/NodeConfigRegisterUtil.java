/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.bean.NodeConfigSettingConstant;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeCustomInfoPK;
import com.clustercontrol.repository.model.NodeDeviceInfoPK;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameHistoryDetail;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeHostnameInfoPK;
import com.clustercontrol.repository.model.NodeLicenseHistoryDetail;
import com.clustercontrol.repository.model.NodeLicenseInfo;
import com.clustercontrol.repository.model.NodeLicenseInfoPK;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeProductHistoryDetail;
import com.clustercontrol.repository.model.NodeProductInfo;
import com.clustercontrol.repository.model.NodeProductInfoPK;
import com.clustercontrol.repository.model.NodeNetstatHistoryDetail;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetstatInfoPK;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodePackageInfoPK;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.model.NodeProcessInfoPK;
import com.clustercontrol.repository.model.NodeVariableHistoryDetail;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.NodeVariableInfoPK;
import com.clustercontrol.util.HinemosTime;

/**
 * ????????????????????????
 *
 * @version 6.2.0
 */
public class NodeConfigRegisterUtil {

	private static Log m_log = LogFactory.getLog( NodeConfigRegisterUtil.class );
	/**
	 * OS?????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param info OS??????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeOsInfo(
			Long registerDatetime, String modifyUserId, String facilityId, NodeOsInfo info, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeOsInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.OS, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeOsInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.OS.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			if (info != null) {
				/** ??????????????????(?????????????????????) */
				NodeOsInfo entity = null;
				boolean isUpdate = false;
				try {
					entity = QueryUtil.getNodeOsEntityPkForNodeConfigSetting(facilityId);

					if (isCollect) {
						// ????????????????????????????????????
						info.setCharacterSet(entity.getCharacterSet());
					}

					// ????????????
					if (!entity.getOsName().equals(info.getOsName())
						|| !entity.getOsRelease().equals(info.getOsRelease())
						|| !entity.getOsVersion().equals(info.getOsVersion())
						|| !entity.getCharacterSet().equals(info.getCharacterSet())
						|| !entity.getStartupDateTime().equals(info.getStartupDateTime())) {

						// ?????????????????????
						isUpdate = true;

						// NodeOsHistoryDetail??????
						NodeOsHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeOsHistoryDetailByRegDateTo(facilityId, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// ????????????
						diffInfo.addModObj(new NodeOsInfo[]{entity.clone(), info.clone()});
					}
				} catch (FacilityNotFound e) {

					// ?????????????????????
					isUpdate = true;

					// ??????????????????
					jtm.checkEntityExists(NodeOsInfo.class, facilityId);
					// ????????????
					entity = new NodeOsInfo(facilityId);
					entity.setRegDate(registerDatetime);
					entity.setRegUser(modifyUserId);
					em.persist(entity);

					// ????????????
					diffInfo.addAddObj(info.clone());
				}

				if (isUpdate) {
					// ????????????????????????????????????

					// ????????????
					entity.setOsName(info.getOsName());
					entity.setOsRelease(info.getOsRelease());
					entity.setOsVersion(info.getOsVersion());
					entity.setCharacterSet(info.getCharacterSet());
					entity.setStartupDateTime(info.getStartupDateTime());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeOsHistoryDetail??????
					NodeOsHistoryDetail historyDetail = new NodeOsHistoryDetail(
							facilityId, registerDatetime);
					historyDetail.setOsName(info.getOsName());
					historyDetail.setOsRelease(info.getOsRelease());
					historyDetail.setOsVersion(info.getOsVersion());
					historyDetail.setCharacterSet(info.getCharacterSet());
					historyDetail.setStartupDateTime(info.getStartupDateTime());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			} else {
				/** ??????????????????(??????) */

				NodeOsInfo entity = null;
				try {
					entity = QueryUtil.getNodeOsEntityPkForNodeConfigSetting(facilityId);

					// NodeOsHistoryDetail??????
					NodeOsHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeOsHistoryDetailByRegDateTo(
								facilityId, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}
					// ????????????
					diffInfo.addDelObj(entity.clone());
				} catch (FacilityNotFound e) {
					// ???????????????
				}
			}

			if (diffInfo.getAddObj().size() == 0
					&& diffInfo.getModObj().size() == 0
					&& diffInfo.getDelObj().size() == 0) {
				diffInfo = null;
			}

			long end = HinemosTime.currentTimeMillis() - start;
			m_log.debug("registerNodeOsInfo : end (" + end + "ms)");

			return diffInfo;
		}
	}

	/**
	 * CPU?????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list CPU??????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeCpuInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeCpuInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeCpuInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_CPU, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeCpuInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_CPU.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeCpuInfo info : list) {
					NodeCpuInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeCpuEntityPkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& entity.getDeviceDescription().equals(info.getDeviceDescription())
								&& entity.getCoreCount().equals(info.getCoreCount())
								&& entity.getThreadCount().equals(info.getThreadCount())
								&& entity.getClockCount().equals(info.getClockCount())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeCpuHistoryDetail??????
						NodeCpuHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeCpuHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// ????????????
						diffInfo.addModObj(new NodeCpuInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeCpuInfo.class, entityPk);
						// ????????????
						entity = new NodeCpuInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeCpuInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setCoreCount(info.getCoreCount());
					entity.setThreadCount(info.getThreadCount());
					entity.setClockCount(info.getClockCount());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeCpuHistoryDetail??????
					NodeCpuHistoryDetail historyDetail = new NodeCpuHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setCoreCount(info.getCoreCount());
					historyDetail.setThreadCount(info.getThreadCount());
					historyDetail.setClockCount(info.getClockCount());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}
			
			/** ??????????????????(??????) */
			List<NodeCpuInfo> entityList = QueryUtil.getNodeCpuInfoByFacilityId(facilityId);
			for (NodeCpuInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_cpu_info????????????
					em.remove(entity);
					// NodeCpuHistoryDetail????????????
					NodeCpuHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeCpuHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeCpuInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ??????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ???????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeMemoryInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeMemoryInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeMemoryInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_MEMORY, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeMemoryInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_MEMORY.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeMemoryInfo info : list) {
					NodeMemoryInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeMemoryEntityPkForNodeConfigSetting(entityPk);

						if (isCollect) {
							// ????????????????????????????????????
							info.setDeviceDescription(entity.getDeviceDescription());
						}

						// ????????????
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& entity.getDeviceDescription().equals(info.getDeviceDescription())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeMemoryHistoryDetail??????
						NodeMemoryHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeMemoryHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// ????????????
						diffInfo.addModObj(new NodeMemoryInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeMemoryInfo.class, entityPk);
						// ????????????
						entity = new NodeMemoryInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeMemoryInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeMemoryHistoryDetail??????
					NodeMemoryHistoryDetail historyDetail = new NodeMemoryHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeMemoryInfo> entityList = QueryUtil.getNodeMemoryInfoByFacilityId(facilityId);
			for (NodeMemoryInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_memory_info????????????
					em.remove(entity);
					// NodeMemoryHistoryDetail????????????
					NodeMemoryHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeMemoryHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeMemoryInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * NIC?????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list NIC??????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeNetworkInterfaceInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeNetworkInterfaceInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeNetworkInterfaceInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_NIC, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeNetworkInterfaceInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_NIC.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeNetworkInterfaceInfo info : list) {
					NodeNetworkInterfaceInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeNetworkInterfaceEntityPkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& entity.getDeviceDescription().equals(info.getDeviceDescription())
								&& entity.getNicIpAddress().equals(info.getNicIpAddress())
								&& entity.getNicMacAddress().equals(info.getNicMacAddress())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeNetworkInterfaceHistoryDetail??????
						NodeNetworkInterfaceHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeNetworkInterfaceHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// ????????????
						diffInfo.addModObj(new NodeNetworkInterfaceInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeNetworkInterfaceInfo.class, entityPk);
						// ????????????
						entity = new NodeNetworkInterfaceInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeNetworkInterfaceInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setNicIpAddress(info.getNicIpAddress());
					entity.setNicMacAddress(info.getNicMacAddress());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeNetworkInterfaceHistoryDetail??????
					NodeNetworkInterfaceHistoryDetail historyDetail = new NodeNetworkInterfaceHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setNicIpAddress(info.getNicIpAddress());
					historyDetail.setNicMacAddress(info.getNicMacAddress());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeNetworkInterfaceInfo> entityList = QueryUtil.getNodeNetworkInterfaceInfoByFacilityId(facilityId);
			for (NodeNetworkInterfaceInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					//??????????????????????????????????????????NIC??????????????????
					if(isCollect && entity.getDeviceType().equals("vnic")){
						m_log.debug("registerNodeNetworkInterfaceInfo(): "+entity.getDeviceName()+" is vnic. Do not delete");
						continue;
					}
					// cc_cfg_node_network_interface_info????????????
					em.remove(entity);
					// NodeNetworkInterfaceHistoryDetail??????
					NodeNetworkInterfaceHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeNetworkInterfaceHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeNetworkInterfaceInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ??????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeDiskInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeDiskInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeDiskInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_DISK, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeDiskInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_DISK.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeDiskInfo info : list) {
					NodeDiskInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeDiskEntityPkForNodeConfigSetting(entityPk);

						if (isCollect) {
							// ????????????????????????????????????
							info.setDiskRpm(entity.getDiskRpm());
						}

						// ????????????
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& entity.getDeviceDescription().equals(info.getDeviceDescription())
								&& entity.getDiskRpm().equals(info.getDiskRpm())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeDiskHistoryDetail??????
						NodeDiskHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeDiskHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// ????????????
						diffInfo.addModObj(new NodeDiskInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeDiskInfo.class, entityPk);
						// ????????????
						entity = new NodeDiskInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeDiskInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setDiskRpm(info.getDiskRpm());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeDiskHistoryDetail??????
					NodeDiskHistoryDetail historyDetail = new NodeDiskHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setDiskRpm(info.getDiskRpm());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeDiskInfo> entityList = QueryUtil.getNodeDiskInfoByFacilityId(facilityId);
			for (NodeDiskInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					//????????????????????????????????????????????????????????????????????????
					if(isCollect && entity.getDeviceType().equals("vdisk")){
						m_log.debug("registerNodeDiskInfo(): "+entity.getDeviceName()+" is vdisk. Do not delete");
						continue;
					}
					// cc_cfg_node_disk_info????????????
					em.remove(entity);
					// NodeDiskHistoryDetail??????
					NodeDiskHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeDiskHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeDiskInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ?????????????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ??????????????????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeFilesystemInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeFilesystemInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeFilesystemInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_FILESYSTEM, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeFilesystemInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_FILESYSTEM.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeFilesystemInfo info : list) {
					NodeFilesystemInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeFilesystemEntityPkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& entity.getDeviceDescription().equals(info.getDeviceDescription())
								&& entity.getFilesystemType().equals(info.getFilesystemType())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeFilesystemHistoryDetail??????
						NodeFilesystemHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeFilesystemHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// ????????????
						diffInfo.addModObj(new NodeFilesystemInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeFilesystemInfo.class, entityPk);
						// ????????????
						entity = new NodeFilesystemInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeFilesystemInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setFilesystemType(info.getFilesystemType());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeFilesystemHistoryDetail??????
					NodeFilesystemHistoryDetail historyDetail = new NodeFilesystemHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setFilesystemType(info.getFilesystemType());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeFilesystemInfo> entityList = QueryUtil.getNodeFilesystemInfoByFacilityId(facilityId);
			for (NodeFilesystemInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_filesystem_info????????????
					em.remove(entity);
					// NodeFilesystemHistoryDetail??????
					NodeFilesystemHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeFilesystemHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeFilesystemInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ?????????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeVariableInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeVariableInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeVariableInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.NODE_VARIABLE, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeVariableInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.NODE_VARIABLE.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeVariableInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeVariableInfo info : list) {
					NodeVariableInfo entity = null;
					NodeVariableInfoPK entityPk = new NodeVariableInfoPK(facilityId, info.getNodeVariableName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeVariablePkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getNodeVariableValue().equals(info.getNodeVariableValue())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeVariableHistoryDetail??????
						NodeVariableHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeVariableHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// ????????????
						diffInfo.addModObj(new NodeVariableInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeVariableInfo.class, entityPk);
						// ????????????
						entity = new NodeVariableInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeVariableInfo
					entity.setNodeVariableValue(info.getNodeVariableValue());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeVariableHistoryDetail??????
					NodeVariableHistoryDetail historyDetail = new NodeVariableHistoryDetail(
							facilityId, info.getNodeVariableName(), registerDatetime);
					historyDetail.setNodeVariableValue(info.getNodeVariableValue());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeVariableInfo> entityList = QueryUtil.getNodeVariableInfoByFacilityId(facilityId);
			for (NodeVariableInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_variable_info????????????
					em.remove(entity);
					// NodeVariableHistoryDetail??????
					NodeVariableHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeVariableHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeVariableInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ??????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeHostnameInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeHostnameInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeHostnameInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HOSTNAME, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeHostnameInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HOSTNAME.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeHostnameInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeHostnameInfo info : list) {
					if (info.getHostname() == null || info.getHostname().equals("")) {
						continue;
					}
					NodeHostnameInfo entity = null;
					NodeHostnameInfoPK entityPk = new NodeHostnameInfoPK(facilityId, info.getHostname());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeHostnamePkForNodeConfigSetting(entityPk);
						// ????????????????????????????????????
						continue;
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeHostnameInfo.class, entityPk);
						// ????????????
						entity = new NodeHostnameInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeHostnameHistoryDetail??????
					NodeHostnameHistoryDetail historyDetail = new NodeHostnameHistoryDetail(
							facilityId, info.getHostname(), registerDatetime);
					historyDetail.setHostnameItem(info.getHostname());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeHostnameInfo> entityList = QueryUtil.getNodeHostnameInfoByFacilityId(facilityId);
			for (NodeHostnameInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_hostname_info????????????
					em.remove(entity);
					// NodeHostnameHistoryDetail??????
					NodeHostnameHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeHostnameHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeHostnameInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ?????????????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ??????????????????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeNetstatInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodeNetstatInfo> list, boolean isCollect) {

		List<String> listenString = new ArrayList<>();
		listenString.add("LISTEN");
		listenString.add("LISTENING");
		listenString.add("UNCONN");

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeNetstatInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.NETSTAT, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeNetstatInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.NETSTAT.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeNetstatInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeNetstatInfo info : list) {
					boolean isNotListen = false;
					NodeNetstatInfo entity = null;
					NodeNetstatInfoPK entityPk = new NodeNetstatInfoPK(
							facilityId, info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort(), info.getForeignIpAddress(), info.getForeignPort(),
							info.getProcessName(), info.getPid());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeNetstatEntityPkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getStatus().equals(info.getStatus())) {
							// ????????????????????????????????????
							continue;
						}
						if (listenString.contains(entity.getStatus())) {
							NodeNetstatHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeNetstatHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
							if (oldHistoryDetail != null) {
								oldHistoryDetail.setRegDateTo(registerDatetime);
							}
							if (listenString.contains(info.getStatus())) {
								// ????????????????????????LISTEN -> ????????????
								// ????????????
								diffInfo.addModObj(new NodeNetstatInfo[]{entity.clone(), info.clone()});
							} else {
								// ???????????????LISTEN -> ????????????
								// ????????????
								diffInfo.addDelObj(entity.clone());
								isNotListen = true;
							}
						} else {
							if (listenString.contains(info.getStatus())) {
								// ????????????LISTEN???????????????LISTEN -> ??????????????????
								// ????????????
								diffInfo.addAddObj(info.clone());
							} else {
								// ????????????????????????LISTEN?????? -> ???????????????
								isNotListen = true;
							}
						}
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeNetstatInfo.class, entityPk);
						// ????????????
						entity = new NodeNetstatInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						if (listenString.contains(info.getStatus())) {
							// LISTEN -> ??????????????????
							// ????????????
							diffInfo.addAddObj(info.clone());
						} else {
							// LISTEN?????? -> ???????????????
							isNotListen = true;
						}
					}

					// NodeNetstatInfo
					entity.setStatus(info.getStatus());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					if (!isNotListen) {
						// NodeNetstatHistoryDetail??????
						NodeNetstatHistoryDetail historyDetail = new NodeNetstatHistoryDetail(
								facilityId, info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort(), info.getForeignIpAddress(), info.getForeignPort(), 
								info.getProcessName(), info.getPid(), registerDatetime);
						historyDetail.setStatus(info.getStatus());
						historyDetail.setRegUser(modifyUserId);
						em.persist(historyDetail);
					}
				}
			}

			/** ??????????????????(??????) */
			List<NodeNetstatInfo> entityList = QueryUtil.getNodeNetstatInfoByFacilityId(facilityId);
			for (NodeNetstatInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					if (listenString.contains(entity.getStatus())) { 
						// NodeNetstatHistoryDetail????????????
						NodeNetstatHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeNetstatHistoryDetailByRegDateTo(
									entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// ????????????
						diffInfo.addDelObj(entity.clone());
					}
					// cc_cfg_node_pacakge_info????????????
					em.remove(entity);
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeNetstatInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ??????????????????
	 * @return true:?????????????????????
	 */
	public static boolean registerNodeProcessInfo(Long registerDatetime, String modifyUserId, String facilityId, List<NodeProcessInfo> list) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeProcessInfo() : start");

		boolean isUpdate = false;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			/** cc_cfg_node_process_info???????????????????????? */ 
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeProcessInfo.class, facilityId);

			em.flush();

			/** cc_cfg_node_process_info????????????????????? */ 
			/** ??????????????????(?????????????????????) */
			if (list != null) {
				for (NodeProcessInfo info : list) {
					NodeProcessInfoPK entityPk = new NodeProcessInfoPK(facilityId, info.getProcessName(), info.getPid());
					// ??????????????????
					jtm.checkEntityExists(NodeProcessInfo.class, entityPk);
					// ????????????
					NodeProcessInfo entity = new NodeProcessInfo(entityPk);
					entity.setPath(info.getPath());
					entity.setExecUser(info.getExecUser());
					entity.setStartupDateTime(info.getStartupDateTime());
					entity.setRegDate(registerDatetime);
					entity.setRegUser(modifyUserId);
					em.persist(entity);

					if (!isUpdate) {
						isUpdate = true;
					}
				}
			}
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeProcessInfo : end (" + end + "ms)");

		return isUpdate;
	}

	/**
	 * ????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ?????????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodePackageInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodePackageInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodePackageInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.PACKAGE, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodePackageInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.PACKAGE.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodePackageInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodePackageInfo info : list) {
					NodePackageInfo entity = null;
					NodePackageInfoPK entityPk = new NodePackageInfoPK(facilityId, info.getPackageId());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodePackageEntityPkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getPackageName().equals(info.getPackageName())
								&& entity.getVersion().equals(info.getVersion())
								&& entity.getRelease().equals(info.getRelease())
								&& entity.getInstallDate().equals(info.getInstallDate())
								&& entity.getVendor().equals(info.getVendor())
								&& entity.getArchitecture().equals(info.getArchitecture())) {
							// ????????????????????????????????????
							continue;
						}

						// NodePackageHistoryDetail??????
						NodePackageHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodePackageHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// ????????????
						diffInfo.addModObj(new NodePackageInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodePackageInfo.class, entityPk);
						// ????????????
						entity = new NodePackageInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodePackageInfo
					entity.setPackageName(info.getPackageName());
					entity.setVersion(info.getVersion());
					entity.setRelease(info.getRelease());
					entity.setInstallDate(info.getInstallDate());
					entity.setVendor(info.getVendor());
					entity.setArchitecture(info.getArchitecture());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodePackageHistoryDetail??????
					NodePackageHistoryDetail historyDetail = new NodePackageHistoryDetail(
							facilityId, info.getPackageId(), registerDatetime);
					historyDetail.setPackageName(info.getPackageName());
					historyDetail.setVersion(info.getVersion());
					historyDetail.setRelease(info.getRelease());
					historyDetail.setInstallDate(info.getInstallDate());
					historyDetail.setVendor(info.getVendor());
					historyDetail.setArchitecture(info.getArchitecture());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodePackageInfo> entityList = QueryUtil.getNodePackageInfoByFacilityId(facilityId);
			for (NodePackageInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_pacakge_info????????????
					em.remove(entity);
					// NodePackageHistoryDetail????????????
					NodePackageHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodePackageHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodePackageInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ???????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ????????????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeProductInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodeProductInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeProductInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.PRODUCT, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeProductInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.PRODUCT.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeProductInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeProductInfo info : list) {
					NodeProductInfo entity = null;
					NodeProductInfoPK entityPk = new NodeProductInfoPK(facilityId, info.getProductName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeProductEntityPkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getVersion().equals(info.getVersion())
								&& entity.getPath().equals(info.getPath())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeProductHistoryDetail??????
						NodeProductHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeProductHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// ????????????
						diffInfo.addModObj(new NodeProductInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeProductInfo.class, entityPk);
						// ????????????
						entity = new NodeProductInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeProductInfo
					entity.setVersion(info.getVersion());
					entity.setPath(info.getPath());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeProductHistoryDetail??????
					NodeProductHistoryDetail historyDetail = new NodeProductHistoryDetail(
							facilityId, info.getProductName(), registerDatetime);
					historyDetail.setVersion(info.getVersion());
					historyDetail.setPath(info.getPath());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeProductInfo> entityList = QueryUtil.getNodeProductInfoByFacilityId(facilityId);
			for (NodeProductInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_product_info????????????
					em.remove(entity);
					// NodeProductHistoryDetail????????????
					NodeProductHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeProductHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeProductInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ?????????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeLicenseInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodeLicenseInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeLicenseInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.LICENSE, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeLicenseInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.LICENSE.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeLicenseInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeLicenseInfo info : list) {
					NodeLicenseInfo entity = null;
					NodeLicenseInfoPK entityPk = new NodeLicenseInfoPK(facilityId, info.getProductName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeLicenseEntityPkForNodeConfigSetting(entityPk);

						// ????????????
						if (entity.getVendor().equals(info.getVendor())
								&& entity.getVendorContact().equals(info.getVendorContact())
								&& entity.getSerialNumber().equals(info.getSerialNumber())
								&& entity.getCount().equals(info.getCount())
								&& entity.getExpirationDate().equals(info.getExpirationDate())) {
							// ????????????????????????????????????
							continue;
						}

						// NodeLicenseHistoryDetail??????
						NodeLicenseHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeLicenseHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// ????????????
						diffInfo.addModObj(new NodeLicenseInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeLicenseInfo.class, entityPk);
						// ????????????
						entity = new NodeLicenseInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeLicenseInfo
					entity.setVendor(info.getVendor());
					entity.setVendorContact(info.getVendorContact());
					entity.setSerialNumber(info.getSerialNumber());
					entity.setCount(info.getCount());
					entity.setExpirationDate(info.getExpirationDate());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeLicenseHistoryDetail??????
					NodeLicenseHistoryDetail historyDetail = new NodeLicenseHistoryDetail(
							facilityId, info.getProductName(), registerDatetime);
					historyDetail.setVendor(info.getVendor());
					historyDetail.setVendorContact(info.getVendorContact());
					historyDetail.setSerialNumber(info.getSerialNumber());
					historyDetail.setCount(info.getCount());
					historyDetail.setExpirationDate(info.getExpirationDate());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeLicenseInfo> entityList = QueryUtil.getNodeLicenseInfoByFacilityId(facilityId);
			for (NodeLicenseInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_license_info????????????
					em.remove(entity);
					// NodeLicenseHistoryDetail????????????
					NodeLicenseHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeLicenseHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeLicenseInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 * @param list ?????????????????????
	 * @param isCollect true:???????????????????????????, false:??????????????????????????????????????????
	 * @return ???????????? (????????????????????????null)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeCustomInfo(
			Long registerDatetime, String modifyUserId, String facilityId, String parentSettingId, List<NodeCustomInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeCustomInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.CUSTOM, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// ???????????????????????????????????????????????????????????????
				m_log.warn(String.format("registerNodeCustomInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.CUSTOM.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** ??????????????????(?????????????????????) */
			List<NodeCustomInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeCustomInfo info : list) {
					NodeCustomInfo entity = null;
					NodeCustomInfoPK entityPk = new NodeCustomInfoPK(facilityId, parentSettingId, info.getSettingCustomId());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeCustomEntityPkForNodeConfigSetting(entityPk);
						
						//??????????????????????????????DB?????????????????????????????????????????????????????????
						if (info.getRegisterFlag().equals(NodeRegisterFlagConstant.NOT_GET)){
							m_log.debug("Invalid flag detected. Remove entity from DB");
							notDelPkList.remove(entityPk);
							continue;
						}
						
						// ????????????
						if (entity.getDisplayName().equals(info.getDisplayName())
								&& entity.getCommand().equals(info.getCommand())
								&& entity.getValue().equals(info.getValue())) {
							// ????????????????????????????????????
							continue;
						}

						// HistoryDetail??????
						NodeCustomHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeCustomHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// ????????????
						diffInfo.addModObj(new NodeCustomInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// ??????????????????
						jtm.checkEntityExists(NodeCustomInfo.class, entityPk);
						//?????????????????????????????????
						if (info.getRegisterFlag().equals(NodeRegisterFlagConstant.NOT_GET)){
							m_log.debug("Invalid flag detected. Do not add to DB");
							continue;
						}
						// ????????????
						entity = new NodeCustomInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// ????????????
						diffInfo.addAddObj(info.clone());
					}

					// NodeCustomInfo
					entity.setDisplayName(info.getDisplayName());
					entity.setCommand(info.getCommand());
					entity.setValue(info.getValue());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodePackageHistoryDetail??????
					NodeCustomHistoryDetail historyDetail = new NodeCustomHistoryDetail(
							facilityId, registerDatetime, parentSettingId, info.getSettingCustomId());
					historyDetail.setDisplayName(info.getDisplayName());
					historyDetail.setCommand(info.getCommand());
					historyDetail.setValue(info.getValue());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** ??????????????????(??????) */
			List<NodeCustomInfo> entityList = QueryUtil.getNodeCustomByFacilityId(facilityId);
			for (NodeCustomInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// ????????????
					em.remove(entity);
					// HistoryDetail????????????
					NodeCustomHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeCustomHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// ????????????
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeCustomInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param registerDatetime ????????????
	 * @param modifyUserId ???????????????ID
	 * @param facilityId ??????????????????ID
	 */
	public static void deleteNodeHistoryDetailInfo(Long registerDatetime, String modifyUserId, String facilityId) {

		long start = HinemosTime.currentTimeMillis();
		long end = 0L;

		m_log.debug("deleteNodeHistoryDetailInfo() : start");

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			HinemosEntityManager em = jtm.getEntityManager();

			// OS??????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeOsHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeOsInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : os end (" + (end - start) + "ms)");
				start = end;
			}

			// CPU??????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeCpuHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeCpuInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : cpu end (" + (end - start) + "ms)");
				start = end;
			}

			// ???????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeMemoryHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeMemoryInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : memory end (" + (end - start) + "ms)");
				start = end;
			}

			// NIC??????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeNetworkInterfaceHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeNetworkInterfaceInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : nic end (" + (end - start) + "ms)");
				start = end;
			}

			// ??????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeDiskHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeDiskInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : disk end (" + (end - start) + "ms)");
				start = end;
			}

			// ??????????????????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeFilesystemHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeFilesystemInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : filesystem end (" + (end - start) + "ms)");
				start = end;
			}

			// ?????????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeVariableHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeVariableInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : variable end (" + (end - start) + "ms)");
				start = end;
			}

			// ??????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeHostnameHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeHostnameInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : hostname end (" + (end - start) + "ms)");
				start = end;
			}

			// ??????????????????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeNetstatHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeNetstatInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : netstat end (" + (end - start) + "ms)");
				start = end;
			}

			// ??????????????????
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeProcessInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : process end (" + (end - start) + "ms)");
				start = end;
			}

			// ?????????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodePackageHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodePackageInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : package end (" + (end - start) + "ms)");
				start = end;
			}

			// ????????????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeProductHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeProductInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : product end (" + (end - start) + "ms)");
				start = end;
			}

			// ?????????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeLicenseHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeLicenseInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : license end (" + (end - start) + "ms)");
				start = end;
			}

			// ?????????????????????
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeCustomHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeCustomInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : custom end (" + (end - start) + "ms)");
				start = end;
			}

			em.flush();
		} catch (RuntimeException e) {
			m_log.warn("deleteNodeHistoryDetailInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		m_log.debug("deleteNodeHistoryDetailInfo() : end");
	}

	public static class NodeConfigRegisterDiffInfo {
		// ??????????????????
		private List<Object> addObj = new ArrayList<>();
		// ????????????
		private List<Object[]> modObj = new ArrayList<>();
		// ????????????
		private List<Object> delObj = new ArrayList<>();
		public List<Object> getAddObj() {
			return addObj;
		}
		public void addAddObj(Object obj) {
			this.addObj.add(obj);
		}
		public List<Object[]> getModObj() {
			return modObj;
		}
		public void addModObj(Object[] objs) {
			this.modObj.add(objs);
		}
		public List<Object> getDelObj() {
			return delObj;
		}
		public void addDelObj(Object obj) {
			this.delObj.add(obj);
		}
	}
}