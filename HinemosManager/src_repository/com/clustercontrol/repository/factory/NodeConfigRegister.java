/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeCustomInfoPK;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHistory;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.NodeConfigRegisterUtil;
import com.clustercontrol.repository.util.NodeConfigRegisterUtil.NodeConfigRegisterDiffInfo;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.repository.util.RepositoryValidator;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ?????????????????????????????????????????????
 *
 */
public class NodeConfigRegister {

	private static Log m_log = LogFactory.getLog(NodeConfigRegister.class);

	// ?????????????????????????????????
	private Long m_registerDatetime = 0L;
	// ???????????????ID
	private String m_modifyUserId = "";
	// ????????????????????????
	private NodeInfo m_nodeInfo = null;
	// ??????????????????
	private NodeConfigSettingInfo m_settingInfo = null;

	/**
	 * ?????????????????????
	 * 
	 * @param registerDatetime ?????????????????????????????????
	 * @param modifyUserId ???????????????ID
	 * @param nodeInfo ????????????????????????
	 * @param nodeConfigSetting ??????????????????
	 */
	public NodeConfigRegister (Long registerDatetime, String modifyUserId, NodeInfo nodeInfo, NodeConfigSettingInfo settingInfo) {
		m_registerDatetime = registerDatetime;
		m_modifyUserId = modifyUserId;
		m_nodeInfo = nodeInfo;
		m_settingInfo = settingInfo;
	}

	/**
	 * 
	 * ????????????????????????
	 * 
	 * @return ?????????????????????
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<OutputBasicInfo> exec() throws FacilityNotFound, InvalidRole, HinemosUnknown {

		/** ??????????????????????????????????????????????????????(SettingItemId?????????) */
		List<String> settingItemInfoList = createSettingItemInfoList(m_settingInfo);

		// ???????????????????????????
		List<OutputBasicInfo> outputBasicInfoList = new ArrayList<>();

		// ?????????????????????
		HashMap<NodeConfigSettingItem, NodeConfigRegisterDiffInfo> diffMap = new HashMap<>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// cc_node_history???????????????????????? 
			NodeHistory history = new NodeHistory(m_nodeInfo.getFacilityId(), m_registerDatetime);

			/** ???????????? (OS??????) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.OS.name())) {
				if (m_nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeOsConfigInfo(m_nodeInfo.getNodeOsInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.OS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()),
								msg, 
								m_settingInfo));
					}
					// ????????????
					if (msgList.size() <= 0) {
						diffMap.put(NodeConfigSettingItem.OS, 
								NodeConfigRegisterUtil.registerNodeOsInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeOsInfo(), true));
						// cc_node_history???????????????????????? 
						history.setOsFlag(true);
					}
				} else if (m_nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
						createOutputBasicList(
							NodeConfigSettingItem.OS.name(),
							PriorityConstant.TYPE_WARNING, 
							MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
									MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()),
							MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
									MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()), 
							m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.OS.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
						createOutputBasicList(
							NodeConfigSettingItem.OS.name(),
							PriorityConstant.TYPE_WARNING, 
							MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
									MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()),
							e.getMessage(), 
							m_settingInfo));
				}
			}
	
			/** ???????????? (CPU??????) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_CPU.name())) {
				if (m_nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeCpuConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeCpuInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_CPU.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.HW_CPU,
								NodeConfigRegisterUtil.registerNodeCpuInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeCpuInfo(), true));
						// cc_node_history???????????????????????? 
						history.setCpuFlag(true);
					}
				} else if (m_nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_CPU.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.CPU_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_CPU.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** ???????????? (Disk??????) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_DISK.name())) {
				if (m_nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeDiskConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeDiskInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_DISK.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.HW_DISK,
								NodeConfigRegisterUtil.registerNodeDiskInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeDiskInfo(), true));
						// cc_node_history???????????????????????? 
						history.setDiskFlag(true);
					}
				} else if (m_nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_DISK.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()), 
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.DISK_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_DISK.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** ???????????? (Filesystem??????) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_FILESYSTEM.name())) {
				if (m_nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeFilesystemConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeFilesystemInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_FILESYSTEM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.HW_FILESYSTEM,
								NodeConfigRegisterUtil.registerNodeFilesystemInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeFilesystemInfo(), true));
						// cc_node_history???????????????????????? 
						history.setFilesystemFlag(true);
					}
				} else if (m_nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_FILESYSTEM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.FILE_SYSTEM_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_FILESYSTEM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** ???????????? (Hostname??????) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HOSTNAME.name())) {
				if (m_nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeHostnameConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeHostnameInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HOSTNAME.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.HOSTNAME,
								NodeConfigRegisterUtil.registerNodeHostnameInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeHostnameInfo(), true));
						// cc_node_history???????????????????????? 
						history.setHostnameFlag(true);
					}
				} else if (m_nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HOSTNAME.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()), 
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.HOST_NAME.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HOSTNAME.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** ???????????? (???????????????) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_MEMORY.name())) {
				if (m_nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeMemoryConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeMemoryInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_MEMORY.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.HW_MEMORY,
								NodeConfigRegisterUtil.registerNodeMemoryInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeMemoryInfo(), true));
						// cc_node_history???????????????????????? 
						history.setMemoryFlag(true);
					}
				} else if (m_nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_MEMORY.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()), 
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.MEMORY_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_MEMORY.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** ???????????? (NIC??????) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_NIC.name())) {
				if (m_nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeNicConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetworkInterfaceInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_NIC.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.HW_NIC,
								NodeConfigRegisterUtil.registerNodeNetworkInterfaceInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetworkInterfaceInfo(), true));
						// cc_node_history???????????????????????? 
						history.setNetworkInterfaceFlag(true);
					}
				} else if (m_nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_NIC.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()), 
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NETWORK_INTERFACE_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_NIC.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
			
			/** ?????????????????????????????? */
			if (settingItemInfoList.contains(NodeConfigSettingItem.NETSTAT.name())) {
				/** ???????????? (??????????????????????????????) */
				if (m_nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeNetstatConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetstatInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.NETSTAT.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.NETSTAT,
								NodeConfigRegisterUtil.registerNodeNetstatInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetstatInfo(), true));
						// cc_node_history???????????????????????? 
						history.setNetstatFlag(true);
					}
	
				} else if (m_nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.NETSTAT.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()), 
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_NETSTAT.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.NETSTAT.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
	
			/** ?????????????????? */
			if (settingItemInfoList.contains(NodeConfigSettingItem.PROCESS.name())) {
				/** ???????????? (??????????????????) */
				if (m_nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodeProcessConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeProcessInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PROCESS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						boolean isUpdate = NodeConfigRegisterUtil.registerNodeProcessInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeProcessInfo());
						if (isUpdate) {
							diffMap.put(NodeConfigSettingItem.PROCESS, new NodeConfigRegisterDiffInfo());
						}
					}
	
				} else if (m_nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PROCESS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()), 
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_PROCESS.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PROCESS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
	
			/** ????????????????????? */
			if (settingItemInfoList.contains(NodeConfigSettingItem.PACKAGE.name())) {
				/** ???????????? (?????????????????????) */
				if (m_nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// ??????????????????
					List<String> msgList = RepositoryValidator.validateNodePackageConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodePackageInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PACKAGE.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// ????????????
						diffMap.put(NodeConfigSettingItem.PACKAGE,
								NodeConfigRegisterUtil.registerNodePackageInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodePackageInfo(), true));
						// cc_node_history???????????????????????? 
						history.setPackageFlag(true);
					}
	
				} else if (m_nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// ?????????
				} else if (m_nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// ????????????
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PACKAGE.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()), 
								m_settingInfo));
				} else {
					// ????????????????????????????????????
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_PACKAGE.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PACKAGE.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
			
			/** ????????????????????? */
			/** ???????????? (?????????????????????) */
			
			//????????????????????????????????????????????????????????????True?????????
			boolean isCustomValid = false;
			
			List<NodeCustomInfo> customList = m_nodeInfo.getNodeCustomInfo();
			if(customList == null || customList.isEmpty()){
				m_log.debug("There is no custom list");
				//????????????????????????????????????????????????????????????????????????????????????????????????
				if (settingItemInfoList.contains(NodeConfigSettingItem.CUSTOM.name())) {
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.CUSTOM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
								MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_CUSTOM_ALL.getMessage(m_nodeInfo.getFacilityId()), 
								m_settingInfo));	
				}
				// ???????????????????????????????????????????????????
				
			} else{
				// ???????????????????????????????????????????????????.
				List<NodeCustomInfo> registerList = new ArrayList<NodeCustomInfo>();
				//????????????????????????????????????
				List<NodeCustomInfo> notGetList = new ArrayList<NodeCustomInfo>();
				//????????????????????????????????????????????????
				String notifyOrgMessage = null;
				for(NodeCustomInfo customResult : customList){
					m_log.debug("Flag num is "+customResult.getRegisterFlag().intValue());
					
					if (customResult.getRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
						try {
							//?????????????????????????????????????????????true
							isCustomValid = true;
							RepositoryValidator.validateNodeCustomInfo(customResult);
							registerList.add(customResult);
						} catch(InvalidSetting e){
							String stackTrace = Arrays.toString( e.getStackTrace());
							String[] args = { customResult.getSettingCustomId(), e.getMessage() + "\n" + stackTrace};
							//??????????????????????????????1024?????????????????????????????????????????????
							if(args[1].length() >= HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue()){
								m_log.debug("[InvalidSetting]Original message to be inserted into Event log exceeded notify.event.messageorg.max.length. Message truncated.");
								notifyOrgMessage = "Custom Info Setting ID: "+args[0]+"\n\nError Details:\n"+args[1];
								notifyOrgMessage = notifyOrgMessage.substring(0,HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue() -1);
							}else{
								notifyOrgMessage =MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_CUSTOM_BY_SETTING.getMessage(args);
							}
							outputBasicInfoList.add(
									createOutputBasicList(
										NodeConfigSettingItem.CUSTOM.name(),
										PriorityConstant.TYPE_WARNING, 
										MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
												MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
										notifyOrgMessage, 
										m_settingInfo));
						}
					} else if(customResult.getRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET){
						// ?????????
						m_log.debug("Got custom info that is not supposed to be exec");
						//???????????????????????????notGetList?????????
						notGetList.add(customResult);
					} else if(customResult.getRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE){
						// ????????????
						String[] args = { customResult.getSettingCustomId(), customResult.getValue()};
						//??????????????????????????????1024?????????????????????????????????????????????
						if(args[1].length() >= HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue()){
							m_log.debug("[Failed Command]Original message to be inserted into Event log exceeded notify.event.messageorg.max.length. Message truncated.");
							notifyOrgMessage = "Custom Info Setting ID: "+args[0]+"\n\nError Details:\n"+args[1];
							notifyOrgMessage = notifyOrgMessage.substring(0,HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue() -1);
						}else{
							notifyOrgMessage =MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_CUSTOM_BY_SETTING.getMessage(args);
						}
						outputBasicInfoList.add(
								createOutputBasicList(
									NodeConfigSettingItem.CUSTOM.name(),
									PriorityConstant.TYPE_WARNING, 
									MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
											MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
									notifyOrgMessage, 
									m_settingInfo));
					} else{
						// ????????????????????????????????????
						InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
								MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()));
						m_log.info("exec() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
							outputBasicInfoList.add(
								createOutputBasicList(
									NodeConfigSettingItem.CUSTOM.name(),
									PriorityConstant.TYPE_WARNING, 
									MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
											MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage() + customResult.getSettingId()),
									e.getMessage(), 
									m_settingInfo));
					}
				}
				
				// ?????????????????????????????????????????????????????????.
				if(!registerList.isEmpty()){
					Iterator<NodeCustomInfo> iter = registerList.iterator();
					List<NodeCustomInfoPK> pkList = new ArrayList<>();
					while(iter.hasNext()) {
						NodeCustomInfo info = iter.next();
						NodeCustomInfoPK entityPk = new NodeCustomInfoPK(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeConfigSettingId(), info.getSettingCustomId());
						if (pkList.contains(entityPk)) {
							String[] args = { MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage(), info.getSettingCustomId()};
							String msg = MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args);
							outputBasicInfoList.add(
									createOutputBasicList(
										NodeConfigSettingItem.CUSTOM.name(),
										PriorityConstant.TYPE_WARNING, 
										MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
												MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
										msg, 
										m_settingInfo));
							iter.remove();
							continue;
						}
						pkList.add(entityPk);
					}
					
					// ????????????.
					if(!registerList.isEmpty()){
						//?????????????????????????????????????????????????????????????????????????????????
						if(!notGetList.isEmpty()){
							//????????????????????????????????????????????????
							m_log.debug("There is NOT_GET setting to delete entity from DB");
							registerList.addAll(notGetList);
						}
						m_log.debug("Finally ready to add to DB");
						diffMap.put(NodeConfigSettingItem.CUSTOM,
								NodeConfigRegisterUtil.registerNodeCustomInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeConfigSettingId(), registerList, true));
						// cc_node_history???????????????????????? 
						if(isCustomValid){
							//??????????????????????????????????????????????????????????????????flag???true?????????
							history.setCustomFlag(true);
							m_log.debug("Valid custom info setting exist. Set Custom Flag to True");
						}else{
							//???????????????????????????????????????????????????????????????????????????
							m_log.debug("No Valid custom info setting exist. Custom Flag is False");
						}
					}
				}
				
			}
				
				
			// cc_node_history???????????????????????? 
			history.setRegUser(m_modifyUserId);
			em.persist(history);

			if (diffMap.size() > 0) {
				// ?????????????????????
				String orgMessage = createDiffMessage(diffMap);
				//orgMessage????????????????????????????????????????????????
				if(orgMessage.equals("")){
					m_log.info("exec(): No NodeConfigInfo Updated");
				}else{
					// FacilityInfo??????
					FacilityInfo facilityInfo = QueryUtil.getFacilityPK(m_nodeInfo.getFacilityId(), ObjectPrivilegeMode.MODIFY);
					facilityInfo.setModifyDatetime(m_registerDatetime);
					facilityInfo.setModifyUserId(m_modifyUserId);
					// ?????????????????????
					outputBasicInfoList.add(
							createOutputBasicList(
									null,
									PriorityConstant.TYPE_INFO, 
									MessageConstant.MESSAGE_NODE_CONFIG_SETTING_SUCCESS.getMessage(),
									orgMessage,
									this.m_settingInfo,
									this.m_registerDatetime));
				}
			}

			// ?????????????????????????????????INTERNAL???????????????
			if (((diffMap.containsKey(NodeConfigSettingItem.OS) && diffMap.get(NodeConfigSettingItem.OS) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_CPU) && diffMap.get(NodeConfigSettingItem.HW_CPU) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_MEMORY) && diffMap.get(NodeConfigSettingItem.HW_MEMORY) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_NIC) && diffMap.get(NodeConfigSettingItem.HW_NIC) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_DISK) && diffMap.get(NodeConfigSettingItem.HW_DISK) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_FILESYSTEM) && diffMap.get(NodeConfigSettingItem.HW_FILESYSTEM) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HOSTNAME) && diffMap.get(NodeConfigSettingItem.HOSTNAME) != null))
				&& HinemosPropertyCommon.repository_device_search_interval.getIntegerValue() > 0) {
				String facilityId = m_nodeInfo.getFacilityId();
				NodeInfo nodeInfo = NodeProperty.getProperty(facilityId);
				if (nodeInfo.getAutoDeviceSearch()) {
					// INTERNAL??????????????????
					try {
						AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.NODE_CONFIG_SETTING,
								MessageConstant.MESSAGE_PLEASE_SET_NODE_CONFIG_AUTO_DEVICE_OFF, new String[] { facilityId });
					} catch (Exception e) {
						// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
						m_log.warn("exec(): Failed to notify InternalEvent.", e);
					}
				}
			}
			
		}

		return outputBasicInfoList;
	}

	/**
	 * ?????????????????????????????????ID????????????????????????????????????
	 * 
	 * @param settingInfo ??????????????????
	 * @return ?????????????????????????????????ID?????????
	 */
	private List<String> createSettingItemInfoList(NodeConfigSettingInfo settingInfo) {
		List<String> settingItemInfoList = new ArrayList<>();
		if (settingInfo == null || settingInfo.getNodeConfigSettingItemList() == null) {
			return settingItemInfoList;
		}
		for (NodeConfigSettingItemInfo itemInfo : settingInfo.getNodeConfigSettingItemList()) {
			settingItemInfoList.add(itemInfo.getSettingItemId());
		}
		return settingItemInfoList;
	}

	/**
	 * 
	 * ??????????????????
	 * 
	 * @param settingItemId ?????????????????????????????????????????????
	 * @param priority ????????????
	 * @param message ???????????????
	 * @param messageOrg ??????????????????????????????
	 * @param settingInfo ????????????
	 * @return ?????????????????????
	 * @throws HinemosUnknown
	 */
	private OutputBasicInfo createOutputBasicList(
			String settingItemId, 
			Integer priority,
			String message,
			String messageOrg, 
			NodeConfigSettingInfo settingInfo
			) throws HinemosUnknown {
		
		return  createOutputBasicList(settingItemId, priority, message, messageOrg, settingInfo, HinemosTime.getDateInstance().getTime());
	}

	/**
	 * 
	 * ??????????????????
	 * 
	 * @param settingItemId ?????????????????????????????????????????????
	 * @param priority ????????????
	 * @param message ???????????????
	 * @param messageOrg ??????????????????????????????
	 * @param settingInfo ????????????
	 * @param outputDate ????????????
	 * @return ?????????????????????
	 * @throws HinemosUnknown
	 */
	private OutputBasicInfo createOutputBasicList(
			String settingItemId, 
			Integer priority,
			String message,
			String messageOrg, 
			NodeConfigSettingInfo settingInfo,
			Long outputDate
			) throws HinemosUnknown {

		// ??????????????????
		OutputBasicInfo outputBasicInfo = new OutputBasicInfo();
		// ??????????????????ID
		outputBasicInfo.setNotifyGroupId(settingInfo.getNotifyGroupId());
		// ???????????????ID
		outputBasicInfo.setPluginId(HinemosModuleConstant.NODE_CONFIG_SETTING);
		// ????????????????????????
		outputBasicInfo.setApplication("");
		// ????????????ID
		outputBasicInfo.setMonitorId(settingInfo.getSettingId());
		// ??????????????????ID
		outputBasicInfo.setFacilityId(m_nodeInfo.getFacilityId());
		// ????????????
		String facilityPath = new RepositoryControllerBean().getFacilityPath(m_nodeInfo.getFacilityId(), null);
		outputBasicInfo.setScopeText(facilityPath);
		// ???????????????
		outputBasicInfo.setMessage(message);
		// ??????????????????????????????
		outputBasicInfo.setMessageOrg(messageOrg);
		//?????????
		outputBasicInfo.setPriority(priority);
		//????????????
		outputBasicInfo.setGenerationDate(outputDate);

		return outputBasicInfo;
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param diffMap ????????????(???????????????????????????)
	 * @return ??????????????????????????????
	 */
	public String createDiffMessage(Map<NodeConfigSettingItem, NodeConfigRegisterDiffInfo> diffMap) {

		if (diffMap.keySet().size() == 0) {
			// ??????????????????
			return "";
		}

		StringBuilder sbMessage = new StringBuilder();
		StringBuilder sbDetail = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

		// ????????????
		// OS??????
		NodeConfigRegisterDiffInfo diffInfo = diffMap.get(NodeConfigSettingItem.OS);
		if (diffInfo != null) {
			String title = MessageConstant.OS.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			if (diffInfo.getAddObj().size() > 0) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.FACILITY_ID.getMessage() + "=" + m_nodeInfo.getFacilityId())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeOsInfo beforeObj = (NodeOsInfo)objs[0];
				NodeOsInfo afterObj = (NodeOsInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.FACILITY_ID.getMessage() + "=" + m_nodeInfo.getFacilityId())
						+ "\n");
				if (!beforeObj.getOsName().equals(afterObj.getOsName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.OS_NAME.getMessage(), beforeObj.getOsName(), afterObj.getOsName()) + "\n");
				}
				if (beforeObj.getOsRelease() != afterObj.getOsRelease()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.OS_RELEASE.getMessage(), beforeObj.getOsRelease(), afterObj.getOsRelease()) + "\n");
				}
				if (!beforeObj.getOsVersion().equals(afterObj.getOsVersion())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.OS_VERSION.getMessage(), beforeObj.getOsVersion(), afterObj.getOsVersion()) + "\n");
				}
				if (!beforeObj.getCharacterSet().equals(afterObj.getCharacterSet())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CHARACTER_SET.getMessage(), beforeObj.getCharacterSet(), afterObj.getCharacterSet()) + "\n");
				}
				if (beforeObj.getStartupDateTime() != afterObj.getStartupDateTime()) {
					String beforeDate = "";
					String afterDate = "";
					if (beforeDate != null) {
						beforeDate = sdf.format(new Date(beforeObj.getStartupDateTime()));
					}
					if (afterDate != null) {
						afterDate = sdf.format(new Date(afterObj.getStartupDateTime()));
					}
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_OS_STARTUP_DATE_TIME.getMessage(), beforeDate, afterDate) + "\n");
				}
			}
			if (diffInfo.getDelObj().size() > 0) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.FACILITY_ID.getMessage() + "=" + m_nodeInfo.getFacilityId())
						+ "\n");
			}
		}
		// HW?????? - CPU??????
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_CPU);
		if (diffInfo != null) {
			String title = MessageConstant.CPU.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeCpuInfo beforeObj = (NodeCpuInfo)objs[0];
				NodeCpuInfo afterObj = (NodeCpuInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getCoreCount().equals(afterObj.getCoreCount())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CPU_CORE_COUNT.getMessage(), beforeObj.getCoreCount().toString(), afterObj.getCoreCount().toString()) + "\n");
				}
				if (!beforeObj.getThreadCount().equals(afterObj.getThreadCount())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CPU_THREAD_COUNT.getMessage(), beforeObj.getThreadCount().toString(), afterObj.getThreadCount().toString()) + "\n");
				}
				if (!beforeObj.getClockCount().equals(afterObj.getClockCount())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CPU_CLOCK_COUNT.getMessage(), beforeObj.getClockCount().toString(), afterObj.getClockCount().toString()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW?????? - ???????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_MEMORY);
		if (diffInfo != null) {
			String title = MessageConstant.MEMORY.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeMemoryInfo beforeObj = (NodeMemoryInfo)objs[0];
				NodeMemoryInfo afterObj = (NodeMemoryInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW?????? - NIC??????
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_NIC);
		if (diffInfo != null) {
			String title = MessageConstant.NETWORK_INTERFACE.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeNetworkInterfaceInfo beforeObj = (NodeNetworkInterfaceInfo)objs[0];
				NodeNetworkInterfaceInfo afterObj = (NodeNetworkInterfaceInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getNicIpAddress().equals(afterObj.getNicIpAddress())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NIC_IP_ADDRESS.getMessage(), beforeObj.getNicIpAddress(), afterObj.getNicIpAddress()) + "\n");
				}
				if (!beforeObj.getNicMacAddress().equals(afterObj.getNicMacAddress())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NIC_MAC_ADDRESS.getMessage(), beforeObj.getNicMacAddress(), afterObj.getNicMacAddress()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW?????? - ??????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_DISK);
		if (diffInfo != null) {
			String title = MessageConstant.DISK.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeDiskInfo beforeObj = (NodeDiskInfo)objs[0];
				NodeDiskInfo afterObj = (NodeDiskInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getDiskRpm().equals(afterObj.getDiskRpm())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DISK_RPM.getMessage(), beforeObj.getDiskRpm().toString(), afterObj.getDiskRpm().toString()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW?????? - ??????????????????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_FILESYSTEM);
		if (diffInfo != null) {
			String title = MessageConstant.FILE_SYSTEM.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeFilesystemInfo beforeObj = (NodeFilesystemInfo)objs[0];
				NodeFilesystemInfo afterObj = (NodeFilesystemInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getFilesystemType().equals(afterObj.getFilesystemType())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.FILE_SYSTEM_TYPE.getMessage(), beforeObj.getFilesystemType().toString(), afterObj.getFilesystemType().toString()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW?????? - ??????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.HOSTNAME);
		if (diffInfo != null) {
			String title = MessageConstant.HOST_NAME.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.HOST_NAME.getMessage() + "=" + ((NodeHostnameInfo)obj).getHostname())
						+ "\n");
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.HOST_NAME.getMessage() + "=" + ((NodeHostnameInfo)obj).getHostname())
						+ "\n");
			}
		}
		// ?????????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.NODE_VARIABLE);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_VARIABLE.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.NODE_VARIABLE_NAME + "=" + ((NodeVariableInfo)obj).getNodeVariableName())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeVariableInfo beforeObj = (NodeVariableInfo)objs[0];
				NodeVariableInfo afterObj = (NodeVariableInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.NODE_VARIABLE_NAME.getMessage() + "=" + beforeObj.getNodeVariableName())
						+ "\n");
				sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_VARIABLE_VALUE.getMessage(), beforeObj.getNodeVariableValue(), afterObj.getNodeVariableValue()) + "\n");
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.NODE_VARIABLE_NAME.getMessage() + "=" + ((NodeVariableInfo)obj).getNodeVariableName())
						+ "\n");
			}
		}
		// ????????????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.NETSTAT);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_NETSTAT.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.NODE_NETSTAT_PROTOCOL.getMessage() + "=" + ((NodeNetstatInfo)obj).getProtocol()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalPort()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignPort()
						+ ", " + MessageConstant.NODE_NETSTAT_PROCESS_NAME.getMessage() + "=" + ((NodeNetstatInfo)obj).getProcessName()
						+ ", " + MessageConstant.NODE_NETSTAT_PID.getMessage() + "=" + ((NodeNetstatInfo)obj).getPid().toString()
						)
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeNetstatInfo beforeObj = (NodeNetstatInfo)objs[0];
				NodeNetstatInfo afterObj = (NodeNetstatInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.NODE_NETSTAT_PROTOCOL.getMessage() + "=" +  beforeObj.getProtocol()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.getMessage() + "=" + beforeObj.getLocalIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_PORT.getMessage() + "=" +  beforeObj.getLocalPort()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.getMessage() + "=" + beforeObj.getForeignIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_PORT.getMessage() + "=" +  beforeObj.getForeignPort()
						+ ", " + MessageConstant.NODE_NETSTAT_PROCESS_NAME.getMessage() + "=" + beforeObj.getProcessName()
						+ ", " + MessageConstant.NODE_NETSTAT_PID.getMessage() + "=" +  beforeObj.getPid().toString()
						)
						+ "\n");
				if (!beforeObj.getStatus().equals(afterObj.getStatus())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_NETSTAT_STATUS.getMessage(), beforeObj.getStatus(), afterObj.getStatus()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.NODE_NETSTAT_PROTOCOL.getMessage() + "=" + ((NodeNetstatInfo)obj).getProtocol()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalPort()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignPort()
						+ ", " + MessageConstant.NODE_NETSTAT_PROCESS_NAME.getMessage() + "=" + ((NodeNetstatInfo)obj).getProcessName()
						+ ", " + MessageConstant.NODE_NETSTAT_PID.getMessage() + "=" + ((NodeNetstatInfo)obj).getPid().toString()
						)
						+ "\n");
			}
		}
		// ??????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.PROCESS);
		if (diffInfo != null) {
			// ??????
			sbMessage.append(MessageConstant.NODE_PROCESS.getMessage() + " : " + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_IS_MODIFY.getMessage() + "\n");
		}
		// ?????????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.PACKAGE);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_PACKAGE.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.NODE_PACKAGE_ID.getMessage() + "=" + ((NodePackageInfo)obj).getPackageId())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodePackageInfo beforeObj = (NodePackageInfo)objs[0];
				NodePackageInfo afterObj = (NodePackageInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.NODE_PACKAGE_ID.getMessage() + "=" + beforeObj.getPackageId())
						+ "\n");
				if (!beforeObj.getPackageName().equals(afterObj.getPackageName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.NODE_PACKAGE_NAME.getMessage(), beforeObj.getPackageName(), afterObj.getPackageName()) + "\n");
				}
				if (!beforeObj.getVersion().equals(afterObj.getVersion())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.NODE_PACKAGE_VERSION.getMessage(), beforeObj.getVersion(), afterObj.getVersion()) + "\n");
				}
				if (!beforeObj.getRelease().equals(afterObj.getRelease())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_RELEASE.getMessage(), beforeObj.getRelease(), afterObj.getRelease()) + "\n");
				}
				if (beforeObj.getInstallDate() != afterObj.getInstallDate()) {
					String beforeDate = "";
					String afterDate = "";
					if (beforeDate != null) {
						beforeDate = sdf.format(new Date(beforeObj.getInstallDate()));
					}
					if (afterDate != null) {
						afterDate = sdf.format(new Date(afterObj.getInstallDate()));
					}
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_INSTALL_DATE.getMessage(), beforeDate, afterDate) + "\n");
				}
				if (!beforeObj.getVendor().equals(afterObj.getVendor())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_VENDOR.getMessage(), beforeObj.getVendor(), afterObj.getVendor()) + "\n");
				}
				if (!beforeObj.getArchitecture().equals(afterObj.getArchitecture())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_ARCHITECTURE.getMessage(), beforeObj.getArchitecture(), afterObj.getArchitecture()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.NODE_PACKAGE_ID.getMessage() + "=" + ((NodePackageInfo)obj).getPackageId())
						+ "\n");
			}
		}

		// ?????????????????????
		diffInfo = diffMap.get(NodeConfigSettingItem.CUSTOM);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage();
			// ??????
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// ??????
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.SETTING_CUSTOM_ID.getMessage() + "=" + ((NodeCustomInfo)obj).getSettingCustomId())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeCustomInfo beforeObj = (NodeCustomInfo)objs[0];
				NodeCustomInfo afterObj = (NodeCustomInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.SETTING_CUSTOM_ID.getMessage() + "=" + beforeObj.getSettingCustomId())
						+ "\n");
				if (!beforeObj.getDisplayName().equals(afterObj.getDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.NODE_CUSTOM_DISPLAY_NAME.getMessage(), beforeObj.getDisplayName(), afterObj.getDisplayName()) + "\n");
				}
				if (!beforeObj.getCommand().equals(afterObj.getCommand())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.COMMAND.getMessage(), beforeObj.getCommand(), afterObj.getCommand()) + "\n");
				}
				if (!beforeObj.getValue().equals(afterObj.getValue())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_CUSTOM_RESULT.getMessage(), beforeObj.getValue(), afterObj.getValue()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.SETTING_CUSTOM_ID.getMessage() + "=" + ((NodeCustomInfo)obj).getSettingCustomId())
						+ "\n");
			}
		}

		// ????????????
		if (sbDetail.length() > 0) {
			sbMessage.append("\n");
			sbMessage.append("<" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DETAIL.getMessage() + ">\n");
			sbMessage.append(sbDetail);
		}
		return sbMessage.toString();
	}
}
