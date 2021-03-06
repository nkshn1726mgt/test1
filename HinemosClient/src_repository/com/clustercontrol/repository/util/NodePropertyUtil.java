/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.repository.bean.DeviceTypeConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.NodeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeCpuInfo;
import com.clustercontrol.ws.repository.NodeCustomInfo;
import com.clustercontrol.ws.repository.NodeDeviceInfo;
import com.clustercontrol.ws.repository.NodeDiskInfo;
import com.clustercontrol.ws.repository.NodeFilesystemInfo;
import com.clustercontrol.ws.repository.NodeGeneralDeviceInfo;
import com.clustercontrol.ws.repository.NodeHostnameInfo;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.ws.repository.NodeLicenseInfo;
import com.clustercontrol.ws.repository.NodeMemoryInfo;
import com.clustercontrol.ws.repository.NodeProductInfo;
import com.clustercontrol.ws.repository.NodeNetstatInfo;
import com.clustercontrol.ws.repository.NodeNetworkInterfaceInfo;
import com.clustercontrol.ws.repository.NodeNoteInfo;
import com.clustercontrol.ws.repository.NodeOsInfo;
import com.clustercontrol.ws.repository.NodePackageInfo;
import com.clustercontrol.ws.repository.NodeProcessInfo;
import com.clustercontrol.ws.repository.NodeVariableInfo;
import com.clustercontrol.ws.repository.RepositoryTableInfo;

public class NodePropertyUtil {

	// ??????
	private static Log m_log = LogFactory.getLog( NodePropertyUtil.class );

	/** ----- ???????????????????????? ----- */
	private Object[][] platformCache = null;
	private Object[][] subPlatformCache = null;

	private static NodePropertyUtil getInstance() {
		return SingletonUtil.getSessionInstance(NodePropertyUtil.class);
	}
	
	/**
	 * ?????????[???????????????]??????????????????????????????????????????????????????????????????????????????????????????/????????????????????????
	 *
	 * @param property
	 * @param modifyNode
	 */
	public static void modifyPropertySetting (Property property, boolean modifyNode) {

		/** ?????????????????? */
		ArrayList<Property> propertyList = null;
		Property deviceProperty = null;
		ArrayList<Property> object1 = null;
		ArrayList<Property> object2 = null;
		ArrayList<Property> object3 = null;
		ArrayList<Property> object4 = null;
		ArrayList<Property> object5 = null;
		ArrayList<Property> object6 = null;
		ArrayList<Property> object7 = null;
		ArrayList<Property> object8 = null;
		ArrayList<Property> object9 = null;
		ArrayList<Property> object10 = null;

		int modify;//????????????
		if(modifyNode){
			modify = PropertyDefineConstant.MODIFY_OK;
		}else{
			modify = PropertyDefineConstant.MODIFY_NG;
		}

		//
		// ??????????????????(??????????????????)??????modify??????/???????????????????????????
		//

		// ----- ?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.HOST_NAME);
		if (propertyList != null && propertyList.size() != 0) {
			for (int i = 0; i < propertyList.size(); i++){
				(propertyList.get(i)).setModify(modify);
			}
		}

		// ----- ?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_DEVICE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
				}
			}
		}

		// ----- CPU?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CPU_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.CPU_CORE_COUNT);
			object9 = PropertyUtil.getProperty(deviceProperty, NodeConstant.CPU_THREAD_COUNT);
			object10 = PropertyUtil.getProperty(deviceProperty, NodeConstant.CPU_CLOCK_COUNT);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
					((Property)object9.get(i)).setModify(modify);
					((Property)object10.get(i)).setModify(modify);
				}
			}
		}

		// ----- MEM?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MEMORY_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
				}
			}
		}

		// ----- NIC?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETWORK_INTERFACE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NIC_IP_ADDRESS);
			object9 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NIC_MAC_ADDRESS);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
					((Property)object9.get(i)).setModify(modify);
				}
			}
		}

		// ----- DISK?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.DISK_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DISK_RPM);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
				}
			}
		}

		// ----- FS?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FILE_SYSTEM_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.FILE_SYSTEM_TYPE);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
				}
			}
		}

		// ----- ?????????????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETSTAT);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_PROTOCOL);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_LOCAL_IP_ADDRESS);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_LOCAL_PORT);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_FOREIGN_PORT);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_PROCESS_NAME);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_PID);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_STATUS);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
				}
			}
		}

		// ----- ?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PROCESS);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_PID);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_PATH);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_EXEC_USER);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_STARTUP_DATE_TIME);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
				}
			}
		}

		// ----- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PACKAGE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_ID);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_NAME);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_VERSION);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_RELEASE);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_INSTALL_DATE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_VENDOR);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_ARCHITECTURE);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
				}
			}
		}

		// ----- ???????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PRODUCT);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PRODUCT_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PRODUCT_VERSION);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PRODUCT_PATH);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
				}
			}
		}

		// ----- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.LICENSE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_PRODUCT_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_VENDOR);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_VENDOR_CONTACT);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_SERIAL_NUMBER);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_COUNT);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_EXPIRATION_DATE);
			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
				}
			}
		}
		
		// ----- ????????????????????? -----
		// ??????????????????.

		// ----- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_VARIABLE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NODE_VARIABLE_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NODE_VARIABLE_VALUE);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
				}
			}
		}

		// ----- ???????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NOTE);
		if (propertyList != null && propertyList.size() != 0) {
			for (int i = 0; i < propertyList.size(); i++){
				(propertyList.get(i)).setModify(modify);
			}
		}
	}

	/**
	 * Property????????????NodeInfo???????????????????????????????????????
	 * ?????????????????????????????????????????????????????????
	 */
	public static NodeInfo property2node (Property property) {
		NodeInfo nodeInfo = new NodeInfo();

		ArrayList<?> object1 = null;
		ArrayList<?> object2 = null;
		ArrayList<?> object3 = null;
		ArrayList<?> object4 = null;
		ArrayList<?> object5 = null;
		ArrayList<?> object6 = null;
		ArrayList<?> object7 = null;
		ArrayList<?> object8 = null;
		ArrayList<?> object9 = null;
		ArrayList<?> object10 = null;

		ArrayList<Property> propertyList = null;
		Property deviceProperty = null;

		// ----- ???????????????????????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.FACILITY_ID);
		if (object1.size() > 0) {
			nodeInfo.setFacilityId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.FACILITY_NAME);
		if (object1.size() > 0) {
			nodeInfo.setFacilityName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.DESCRIPTION);
		if (object1.size() > 0) {
			nodeInfo.setDescription((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.VALID);
		if (object1.size() > 0) {
			nodeInfo.setValid((Boolean)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.AUTO_DEVICE_SEARCH);
		if (object1.size() > 0) {
			nodeInfo.setAutoDeviceSearch((Boolean)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CREATOR_NAME);
		if (object1.size() > 0) {
			nodeInfo.setCreateUserId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CREATE_TIME);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setCreateDatetime(((Date)object1.get(0)).getTime());
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.MODIFIER_NAME);
		if (object1.size() > 0) {
			nodeInfo.setModifyUserId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.MODIFY_TIME);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setModifyDatetime(((Date)object1.get(0)).getTime());
		}

		// ----- ??????????????????????????? -----

		// ----- HW?????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.PLATFORM_FAMILY_NAME);
		if (object1.size() > 0) {
			nodeInfo.setPlatformFamily((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SUB_PLATFORM_FAMILY_NAME);
		if (object1.size() > 0) {
			nodeInfo.setSubPlatformFamily((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.HARDWARE_TYPE);
		if (object1.size() > 0) {
			nodeInfo.setHardwareType((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.NODE_NAME);
		if (object1.size() > 0) {
			nodeInfo.setNodeName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.ICONIMAGE);
		if (object1.size() > 0) {
			nodeInfo.setIconImage((String)object1.get(0));
		}


		// ----- IP?????????????????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_VERSION);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpAddressVersion((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_V4);
		if (object1.size() > 0) {
			nodeInfo.setIpAddressV4((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_V6);
		if (object1.size() > 0) {
			nodeInfo.setIpAddressV6((String)object1.get(0));
		}

		// ----- Hinemos????????????????????????  -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.AGENT_AWAKE_PORT);
		if (object1.size() > 0  && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setAgentAwakePort((Integer)object1.get(0));
		}

		// ----- ?????????????????? -----
		// ----- ???????????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.HOST_NAME);
		ArrayList<NodeHostnameInfo> nodeHostnameInfo = new ArrayList<NodeHostnameInfo>();
		for (Object o : object1) {
			NodeHostnameInfo item = new NodeHostnameInfo();
			item.setHostname((String)o);
			nodeHostnameInfo.add(item);
		}
		List<NodeHostnameInfo> nodeHostnameInfo_orig = nodeInfo.getNodeHostnameInfo();
		nodeHostnameInfo_orig.clear();
		nodeHostnameInfo_orig.addAll(nodeHostnameInfo);
		// ----- OS?????? -----
		if (nodeInfo.getNodeOsInfo() == null) {
			NodeOsInfo nodeOsInfo = new NodeOsInfo();
			nodeOsInfo.setFacilityId(nodeInfo.getFacilityId());
			nodeInfo.setNodeOsInfo(new NodeOsInfo());
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_NAME);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setOsName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_RELEASE);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setOsRelease((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_VERSION);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setOsVersion((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CHARACTER_SET);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setCharacterSet((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_STARTUP_DATE_TIME);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.getNodeOsInfo().setStartupDateTime(((Date)object1.get(0)).getTime());
		}

		// ----- ?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_DEVICE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			ArrayList<NodeGeneralDeviceInfo> nodeDeviceInfo = new ArrayList<NodeGeneralDeviceInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeGeneralDeviceInfo item = new NodeGeneralDeviceInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));

				if((item.getDeviceType() == null || "".equals(item.getDeviceType()))
						&& (item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("General Device is null");
				}else{
					nodeDeviceInfo.add(item);
				}
			}
			List<NodeGeneralDeviceInfo> nodeDeviceInfo_orig = nodeInfo.getNodeDeviceInfo();
			nodeDeviceInfo_orig.clear();
			nodeDeviceInfo_orig.addAll(nodeDeviceInfo);
		}

		// ----- CPU?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CPU_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.CPU_CORE_COUNT);
			object9 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.CPU_THREAD_COUNT);
			object10 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.CPU_CLOCK_COUNT);

			ArrayList<NodeCpuInfo> nodeCpuInfo = new ArrayList<NodeCpuInfo>();

			for (int i = 0; i < object1.size(); i++) {
				NodeCpuInfo item = new NodeCpuInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				item.setCoreCount((Integer)object8.get(i));
				item.setThreadCount((Integer)object9.get(i));
				item.setClockCount((Integer)object10.get(i));
				
				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("CPU Device is null");
				}else{
					nodeCpuInfo.add(item);
				}
			}
			List<NodeCpuInfo> nodeCpuInfo_orig = nodeInfo.getNodeCpuInfo();
			nodeCpuInfo_orig.clear();
			nodeCpuInfo_orig.addAll(nodeCpuInfo);
		}

		// ----- MEM?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MEMORY_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			ArrayList<NodeMemoryInfo> nodeMemoryInfo = new ArrayList<NodeMemoryInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeMemoryInfo item = new NodeMemoryInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("Memory Device is null");
				}else{
					nodeMemoryInfo.add(item);
				}
			}
			List<NodeMemoryInfo> nodeMemoryInfo_orig = nodeInfo.getNodeMemoryInfo();
			nodeMemoryInfo_orig.clear();
			nodeMemoryInfo_orig.addAll(nodeMemoryInfo);
		}

		// ----- NIC?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETWORK_INTERFACE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NIC_IP_ADDRESS);
			object9 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NIC_MAC_ADDRESS);

			ArrayList<NodeNetworkInterfaceInfo> nodeNetworkInterfaceInfo = new ArrayList<NodeNetworkInterfaceInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeNetworkInterfaceInfo item = new NodeNetworkInterfaceInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				item.setNicIpAddress((String)object8.get(i));
				item.setNicMacAddress((String)object9.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("NIC Device is null");
				}else{
					nodeNetworkInterfaceInfo.add(item);
				}
			}
			List<NodeNetworkInterfaceInfo> nodeNetworkInterfaceInfo_orig
			= nodeInfo.getNodeNetworkInterfaceInfo();
			nodeNetworkInterfaceInfo_orig.clear();
			nodeNetworkInterfaceInfo_orig.addAll(nodeNetworkInterfaceInfo);
		}

		// ----- DISK?????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.DISK_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DISK_RPM);

			ArrayList<NodeDiskInfo> nodeDiskInfo = new ArrayList<NodeDiskInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeDiskInfo item = new NodeDiskInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				if(object8.get(i) != null && !"".equals(object8.get(i)))
					item.setDiskRpm((Integer)object8.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("Disk Device is null");
				}else{
					nodeDiskInfo.add(item);
				}
			}
			List<NodeDiskInfo> nodeDiskInfo_orig = nodeInfo.getNodeDiskInfo();
			nodeDiskInfo_orig.clear();
			nodeDiskInfo_orig.addAll(nodeDiskInfo);
		}

		// ----- ?????????????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FILE_SYSTEM_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.FILE_SYSTEM_TYPE);

			ArrayList<NodeFilesystemInfo> nodeFilesystemInfo = new ArrayList<NodeFilesystemInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeFilesystemInfo item = new NodeFilesystemInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				item.setFilesystemType((String)object8.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("File System Device is null");
				}else{
					nodeFilesystemInfo.add(item);
				}
			}
			List<NodeFilesystemInfo> nodeFilesystemInfo_orig
			= nodeInfo.getNodeFilesystemInfo();
			nodeFilesystemInfo_orig.clear();
			nodeFilesystemInfo_orig.addAll(nodeFilesystemInfo);
		}

		// ----- ?????????????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETSTAT_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_PROTOCOL);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_LOCAL_IP_ADDRESS);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_LOCAL_PORT);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_FOREIGN_PORT);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_PROCESS_NAME);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_PID);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_STATUS);

			ArrayList<NodeNetstatInfo> nodeNetstatInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeNetstatInfo item = new NodeNetstatInfo();
				item.setProtocol((String)object1.get(i));
				item.setLocalIpAddress((String)object2.get(i));
				item.setLocalPort((String)object3.get(i));
				item.setForeignIpAddress((String)object4.get(i));
				item.setForeignPort((String)object5.get(i));
				item.setProcessName((String)object6.get(i));
				if(object7.get(i) != null && !"".equals(object7.get(i))) {
					item.setPid((Integer)object7.get(i));
				}
				item.setStatus((String)object8.get(i));
				if((item.getProtocol() == null || "".equals(item.getProtocol())
						&& (item.getLocalIpAddress() == null || "".equals(item.getLocalIpAddress()))
						&& (item.getLocalPort() == null || "".equals(item.getLocalPort())))){
					m_log.debug("Netstat is null");
				}else{
					nodeNetstatInfo.add(item);
				}
			}
			List<NodeNetstatInfo> nodeNetstatInfo_orig = nodeInfo.getNodeNetstatInfo();
			nodeNetstatInfo_orig.clear();
			nodeNetstatInfo_orig.addAll(nodeNetstatInfo);
		}

		// ----- ?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PROCESS_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_PID);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_PATH);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_EXEC_USER);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_STARTUP_DATE_TIME);

			ArrayList<NodeProcessInfo> nodeProcessInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeProcessInfo item = new NodeProcessInfo();
				item.setProcessName((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i))) {
					item.setPid((Integer)object2.get(i));
				}
				item.setPath((String)object3.get(i));
				item.setExecUser((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i))) {
					item.setStartupDateTime(((Date)object5.get(i)).getTime());
				}
				if((item.getProcessName() == null || "".equals(item.getProcessName())
						&& (item.getPid() == null || item.getPid() <= -1))){
					m_log.debug("Process is null");
				}else{
					nodeProcessInfo.add(item);
				}
			}
			List<NodeProcessInfo> nodeProcessInfo_orig = nodeInfo.getNodeProcessInfo();
			nodeProcessInfo_orig.clear();
			nodeProcessInfo_orig.addAll(nodeProcessInfo);
		}

		// ----- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PACKAGE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_ID);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_NAME);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_VERSION);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_RELEASE);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_INSTALL_DATE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_VENDOR);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_ARCHITECTURE);

			ArrayList<NodePackageInfo> nodePackageInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodePackageInfo item = new NodePackageInfo();
				item.setPackageId((String)object1.get(i));
				item.setPackageName((String)object2.get(i));
				item.setVersion((String)object3.get(i));
				item.setRelease((String)object4.get(i));
				if (object5.size() > 0 && object5.get(i) != null &&
						!object5.get(i).toString().equals("")) {
					item.setInstallDate(((Date)object5.get(i)).getTime());
				}
				item.setVendor((String)object6.get(i));
				item.setArchitecture((String)object7.get(i));

				if(item.getPackageId() == null || "".equals(item.getPackageId())){
					m_log.debug("Package is null");
				}else{
					nodePackageInfo.add(item);
				}
			}
			List<NodePackageInfo> nodePackageInfo_orig = nodeInfo.getNodePackageInfo();
			nodePackageInfo_orig.clear();
			nodePackageInfo_orig.addAll(nodePackageInfo);
		}

		// ----- ???????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PRODUCT_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PRODUCT_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PRODUCT_VERSION);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PRODUCT_PATH);

			ArrayList<NodeProductInfo> nodeProductInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeProductInfo item = new NodeProductInfo();
				item.setProductName((String)object1.get(i));
				item.setVersion((String)object2.get(i));
				item.setPath((String)object3.get(i));

				if(item.getProductName() == null || "".equals(item.getProductName())){
					m_log.debug("Product is null");
				}else{
					nodeProductInfo.add(item);
				}
			}
			List<NodeProductInfo> nodeProductInfo_orig = nodeInfo.getNodeProductInfo();
			nodeProductInfo_orig.clear();
			nodeProductInfo_orig.addAll(nodeProductInfo);
		}

		// ----- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.LICENSE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_PRODUCT_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_VENDOR);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_VENDOR_CONTACT);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_SERIAL_NUMBER);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_COUNT);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_EXPIRATION_DATE);

			ArrayList<NodeLicenseInfo> nodeLicenseInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeLicenseInfo item = new NodeLicenseInfo();
				item.setProductName((String)object1.get(i));
				item.setVendor((String)object2.get(i));
				item.setVendorContact((String)object3.get(i));
				item.setSerialNumber((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i))) {
					item.setCount((Integer)object5.get(i));
				}
				if(object6.get(i) != null && !"".equals(object6.get(i))) {
					item.setExpirationDate(((Date)object6.get(i)).getTime());
				}
				if(item.getProductName() == null || "".equals(item.getProductName())){
					m_log.debug("License is null");
				}else{
					nodeLicenseInfo.add(item);
				}

			}
			List<NodeLicenseInfo> nodeLicenseInfo_orig = nodeInfo.getNodeLicenseInfo();
			nodeLicenseInfo_orig.clear();
			nodeLicenseInfo_orig.addAll(nodeLicenseInfo);
		}

		// ----- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_VARIABLE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NODE_VARIABLE_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NODE_VARIABLE_VALUE);

			ArrayList<NodeVariableInfo> nodeVariableInfo = new ArrayList<NodeVariableInfo>();
			for (int i = 0; i < object1.size(); i++) {
				if ((object1.get(i) != null || ! object1.get(i).toString().equals(""))
						&& (object2.get(i) != null && ! object2.get(i).toString().equals(""))
						) {
					NodeVariableInfo item = new NodeVariableInfo();
					item.setNodeVariableName((String)object1.get(i));
					item.setNodeVariableValue((String)object2.get(i));
					nodeVariableInfo.add(item);
				}
			}
			List<NodeVariableInfo> nodeVariableInfo_orig = nodeInfo.getNodeVariableInfo();
			nodeVariableInfo_orig.clear();
			nodeVariableInfo_orig.addAll(nodeVariableInfo);
		}

		// ----- ????????????????????? -----
		// ????????????.

		// ----- ????????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.JOB_PRIORITY);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setJobPriority((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.JOB_MULTIPLICITY);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setJobMultiplicity((Integer)object1.get(0));
		}


		// ----- ???????????????????????? -----

		// ----- SNMP?????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_USER);
		if (object1.size() > 0) {
			nodeInfo.setSnmpUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_AUTH_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setSnmpAuthPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_PRIV_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setSnmpPrivPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSnmpPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_COMMUNITY);
		if (object1.size() > 0) {
			nodeInfo.setSnmpCommunity((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_VERSION);
		if (object1.size() > 0) {
			nodeInfo.setSnmpVersion(SnmpVersionConstant.stringToType((String)object1.get(0)));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_SECURITY_LEVEL);
		if (object1.size() > 0) {
			nodeInfo.setSnmpSecurityLevel((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_AUTH_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setSnmpAuthProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_PRIV_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setSnmpPrivProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMPTIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSnmpTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMPRETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSnmpRetryCount((Integer)object1.get(0));
		}


		// ----- WBEM?????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_USER);
		if (object1.size() > 0) {
			nodeInfo.setWbemUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setWbemUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWbemPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setWbemProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWbemTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_RETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWbemRetryCount((Integer)object1.get(0));
		}

		// ----- IPMI?????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_IP_ADDRESS);
		if (object1.size() > 0) {
			nodeInfo.setIpmiIpAddress((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpmiPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_USER);
		if (object1.size() > 0) {
			nodeInfo.setIpmiUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setIpmiUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpmiTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_RETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpmiRetries((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setIpmiProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_LEVEL);
		if (object1.size() > 0) {
			nodeInfo.setIpmiLevel((String)object1.get(0));
		}

		// ----- WinRM?????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_USER);
		if (object1.size() > 0) {
			nodeInfo.setWinrmUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setWinrmUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_VERSION);
		if (object1.size() > 0) {
			nodeInfo.setWinrmVersion((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWinrmPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setWinrmProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWinrmTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_RETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWinrmRetries((Integer)object1.get(0));
		}

		// ----- SSH?????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_USER);
		if (object1.size() > 0) {
			nodeInfo.setSshUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setSshUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_PRIVATE_KEY_FILEPATH);
		if (object1.size() > 0) {
			nodeInfo.setSshPrivateKeyFilepath((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_PRIVATE_KEY_PASSPHRASE);
		if (object1.size() > 0) {
			nodeInfo.setSshPrivateKeyPassphrase((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSshPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSshTimeout((Integer)object1.get(0));
		}

		// ----- ???????????????????????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDSERVICE);
		if (object1.size() > 0) {
			nodeInfo.setCloudService((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDSCOPE);
		if (object1.size() > 0) {
			nodeInfo.setCloudScope((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDRESOURCETYPE);
		if (object1.size() > 0) {
			nodeInfo.setCloudResourceType((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDRESOURCEID);
		if (object1.size() > 0) {
			nodeInfo.setCloudResourceId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDRESOURCENAME);
		if (object1.size() > 0) {
			nodeInfo.setCloudResourceName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDLOCATION);
		if (object1.size() > 0) {
			nodeInfo.setCloudLocation((String)object1.get(0));
		}

		// ----- ???????????? -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.ADMINISTRATOR);
		if (object1.size() > 0) {
			nodeInfo.setAdministrator((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CONTACT);
		if (object1.size() > 0) {
			nodeInfo.setContact((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.NOTE);
		ArrayList<NodeNoteInfo> nodeNoteInfo = new ArrayList<NodeNoteInfo>();
		for (int i = 0; i < object1.size(); i++) {
			NodeNoteInfo item = new NodeNoteInfo();
			item.setNoteId(i);
			item.setNote((String) object1.get(i));
			nodeNoteInfo.add(item);
		}
		List<NodeNoteInfo> nodeNoteInfo_orig = nodeInfo.getNodeNoteInfo();
		nodeNoteInfo_orig.clear();
		nodeNoteInfo_orig.addAll(nodeNoteInfo);

		return nodeInfo;
	}

	/**
	 * NodeInfo???Property?????????????????????
	 * @param managerName
	 * @param node
	 * @param mode
	 * @param locale
	 * @param isNodeMap true:????????????????????????????????????????????????
	 * @return
	 */
	public static Property node2property(String managerName, NodeInfo node, int mode, Locale locale, boolean isNodeMap) {
		/** ?????????????????? */
		Property property = null;
		ArrayList<Property> propertyList = null;
		Property childProperty = null;

		/** ??????????????? */
		property = getProperty(managerName, mode, locale, isNodeMap);
		if (node == null) {
			return property;
		}

		if (!isNodeMap) {
			// ----- ???????????????????????? -----
			// ??????????????????ID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.FACILITY_ID);
			((Property)propertyList.get(0)).setValue(node.getFacilityId());
			// ?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.FACILITY_NAME);
			((Property)propertyList.get(0)).setValue(node.getFacilityName());
			// ??????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.DESCRIPTION);
			((Property)propertyList.get(0)).setValue(node.getDescription());
			// ??????/??????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.VALID);
			((Property)propertyList.get(0)).setValue(node.isValid());
			// ???????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.AUTO_DEVICE_SEARCH);
			((Property)propertyList.get(0)).setValue(node.isAutoDeviceSearch());
			// ???????????????ID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CREATOR_NAME);
			((Property)propertyList.get(0)).setValue(node.getCreateUserId());
			// ????????????
			if (node.getCreateDatetime() != null && node.getCreateDatetime() != 0) {
				propertyList = PropertyUtil.getProperty(property, NodeConstant.CREATE_TIME);
				((Property)propertyList.get(0)).setValue(new Date(node.getCreateDatetime()));
			}
			// ?????????????????????ID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.MODIFIER_NAME);
			((Property)propertyList.get(0)).setValue(node.getModifyUserId());
			// ??????????????????
			if (node.getModifyDatetime() != null && node.getModifyDatetime() != 0) {
				propertyList = PropertyUtil.getProperty(property, NodeConstant.MODIFY_TIME);
				((Property)propertyList.get(0)).setValue(new Date(node.getModifyDatetime()));
			}
	
			// ----- ??????????????? -----
	
			// ----- HW?????? -----
			// ????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.PLATFORM_FAMILY_NAME);
			((Property)propertyList.get(0)).setValue(node.getPlatformFamily());
			// ??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SUB_PLATFORM_FAMILY_NAME);
			((Property)propertyList.get(0)).setValue(node.getSubPlatformFamily());
			// H/W?????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.HARDWARE_TYPE);
			((Property)propertyList.get(0)).setValue(node.getHardwareType());
			// ????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_NAME);
			((Property)propertyList.get(0)).setValue(node.getNodeName());
			// ??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.ICONIMAGE);
			((Property)propertyList.get(0)).setValue(node.getIconImage());
	
	
			// ----- IP?????????????????? -----
			// IP???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_VERSION);
			((Property)propertyList.get(0)).setValue(node.getIpAddressVersion());
			// IP????????????V4
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_V4);
			((Property)propertyList.get(0)).setValue(node.getIpAddressV4());
			// IP????????????V6
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_V6);
			((Property)propertyList.get(0)).setValue(node.getIpAddressV6());

			// ----- Hinemos?????????????????? -----
			// ??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.AGENT_AWAKE_PORT);
			((Property)propertyList.get(0)).setValue(node.getAgentAwakePort());
		}

		// ----- ?????????????????? -----
		Boolean isSearchTarget = null;
		// ????????????
		propertyList = PropertyUtil.getProperty(property, NodeConstant.HOST_NAME);
		Property parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeHostnameInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(parentProperty, childProperty);
			for (int i = 0; i < node.getNodeHostnameInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeHostnameInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeHostnameInfo hostname = node.getNodeHostnameInfo().get(i);

				if(hostname.isSearchTarget() == null){
					isSearchTarget = false;
				} else{
					isSearchTarget = hostname.isSearchTarget();
				}

				// ????????????
				target.setValue(hostname.getHostname());
				target.setStringHighlight(isSearchTarget);
			}
		}
		// ----- OS?????? -----
		String osName = null;
		String osRelease = null;
		String osVersion = null;
		String characterSet = null;
		Long startDateTime = null;
		if (node.getNodeOsInfo() != null) {
			osName = node.getNodeOsInfo().getOsName();
			osRelease = node.getNodeOsInfo().getOsRelease();
			osVersion = node.getNodeOsInfo().getOsVersion();
			characterSet = node.getNodeOsInfo().getCharacterSet();
			startDateTime = node.getNodeOsInfo().getStartupDateTime();
			if (node.getNodeOsInfo().isSearchTarget() == null) {
				isSearchTarget = false;
			} else {
				isSearchTarget = node.getNodeOsInfo().isSearchTarget();
			}
		}
		// OS???
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_NAME);
		((Property)propertyList.get(0)).setValue(osName);
		// ????????????
		((Property)propertyList.get(0)).setStringHighlight(isSearchTarget);
		// OS????????????
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_RELEASE);
		((Property)propertyList.get(0)).setValue(osRelease);
		// OS???????????????
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_VERSION);
		((Property)propertyList.get(0)).setValue(osVersion);
		// ???????????????
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CHARACTER_SET);
		((Property)propertyList.get(0)).setValue(characterSet);
		// ????????????
		if (startDateTime != null && startDateTime != 0) {
			propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_STARTUP_DATE_TIME);
			((Property)propertyList.get(0)).setValue(new Date(startDateTime));
		}

		// ----- ??????????????????-----
		// ----- ???????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_DEVICE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeDeviceInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeDeviceInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeDeviceInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeDeviceInfo device = node.getNodeDeviceInfo().get(i);

				// ????????????????????????????????????????????????
				target.setValue(device.getDeviceDisplayName());
				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(device.getDeviceType());
				// ????????????INDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(device.getDeviceIndex());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(device.getDeviceName());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(device.getDeviceDisplayName());
				// ?????????????????????
				if (device.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(device.getDeviceSize());
				}
				// ???????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(device.getDeviceSizeUnit());
				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(device.getDeviceDescription());
			}
		}

		// ----- CPU?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CPU);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeCpuInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeCpuInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeCpuInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeCpuInfo cpu = node.getNodeCpuInfo().get(i);

				// ????????????????????????????????????????????????
				target.setValue(cpu.getDeviceDisplayName());
				// ????????????
				target.setStringHighlight(cpu.isSearchTarget());

				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceType());
				// ????????????INDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceIndex());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceName());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceDisplayName());
				// ?????????????????????
				if (cpu.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(cpu.getDeviceSize());
				}
				// ???????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceSizeUnit());
				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceDescription());
				// ?????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.CPU_CORE_COUNT);
				((Property)propertyList.get(0)).setValue(cpu.getCoreCount());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.CPU_THREAD_COUNT);
				((Property)propertyList.get(0)).setValue(cpu.getThreadCount());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.CPU_CLOCK_COUNT);
				((Property)propertyList.get(0)).setValue(cpu.getClockCount());

			}
		}

		// ----- MEM?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MEMORY);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeMemoryInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeMemoryInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeMemoryInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeMemoryInfo memory = node.getNodeMemoryInfo().get(i);

				// ????????????????????????????????????????????????
				target.setValue(memory.getDeviceDisplayName());
				// ????????????
				target.setStringHighlight(memory.isSearchTarget());

				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(memory.getDeviceType());
				// ????????????INDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(memory.getDeviceIndex());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(memory.getDeviceName());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(memory.getDeviceDisplayName());
				// ?????????????????????
				if (memory.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(memory.getDeviceSize());
				}
				// ???????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(memory.getDeviceSizeUnit());
				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(memory.getDeviceDescription());
			}
		}

		// ----- NIC?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETWORK_INTERFACE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeNetworkInterfaceInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeNetworkInterfaceInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeNetworkInterfaceInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeNetworkInterfaceInfo nic = node.getNodeNetworkInterfaceInfo().get(i);

				// ????????????????????????????????????????????????
				target.setValue(nic.getDeviceDisplayName());
				// ????????????
				target.setStringHighlight(nic.isSearchTarget());

				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(nic.getDeviceType());
				// ????????????INDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(nic.getDeviceIndex());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(nic.getDeviceName());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(nic.getDeviceDisplayName());
				// ?????????????????????
				if (nic.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(nic.getDeviceSize());
				}
				// ???????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(nic.getDeviceSizeUnit());
				// NIC IP ????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NIC_IP_ADDRESS);
				((Property)propertyList.get(0)).setValue(nic.getNicIpAddress());
				// NIC MAC ????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NIC_MAC_ADDRESS);
				((Property)propertyList.get(0)).setValue(nic.getNicMacAddress());
				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(nic.getDeviceDescription());
			}
		}

		// ----- DISK?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.DISK);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeDiskInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeDiskInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeDiskInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeDiskInfo disk = node.getNodeDiskInfo().get(i);

				// ????????????????????????????????????????????????
				target.setValue(disk.getDeviceDisplayName());
				// ????????????
				target.setStringHighlight(disk.isSearchTarget());

				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(disk.getDeviceType());
				// ????????????INDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(disk.getDeviceIndex());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(disk.getDeviceName());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(disk.getDeviceDisplayName());
				// ?????????????????????
				if (disk.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(disk.getDeviceSize());
				}
				// ???????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(disk.getDeviceSizeUnit());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DISK_RPM);
				((Property)propertyList.get(0)).setValue(disk.getDiskRpm());
				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(disk.getDeviceDescription());
			}
		}

		// ---- ?????????????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FILE_SYSTEM);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeFilesystemInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeFilesystemInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeFilesystemInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeFilesystemInfo filesystem = node.getNodeFilesystemInfo().get(i);

				// ????????????????????????????????????????????????
				target.setValue(filesystem.getDeviceDisplayName());
				// ????????????
				target.setStringHighlight(filesystem.isSearchTarget());

				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceType());
				// ????????????INDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceIndex());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceName());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceDisplayName());
				// ?????????????????????
				if (filesystem.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(filesystem.getDeviceSize());
				}
				// ???????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceSizeUnit());
				// ??????????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.FILE_SYSTEM_TYPE);
				((Property)propertyList.get(0)).setValue(filesystem.getFilesystemType());
				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceDescription());
			}
		}

		// ---- ???????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETSTAT);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeNetstatInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeNetstatInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeNetstatInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeNetstatInfo netstatInfo = node.getNodeNetstatInfo().get(i);

				// ??????????????????????????????????????????????????????IP???????????????????????????????????????
				target.setValue(String.format(
						"%s %s:%s", netstatInfo.getProtocol(), netstatInfo.getLocalIpAddress(), netstatInfo.getLocalPort()));
				// ????????????
				target.setStringHighlight(netstatInfo.isSearchTarget());

				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_PROTOCOL);
				((Property)propertyList.get(0)).setValue(netstatInfo.getProtocol());
				// ????????????IP????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_LOCAL_IP_ADDRESS);
				((Property)propertyList.get(0)).setValue(netstatInfo.getLocalIpAddress());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_LOCAL_PORT);
				((Property)propertyList.get(0)).setValue(netstatInfo.getLocalPort());
				// ??????IP????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS);
				((Property)propertyList.get(0)).setValue(netstatInfo.getForeignIpAddress());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_FOREIGN_PORT);
				((Property)propertyList.get(0)).setValue(netstatInfo.getForeignPort());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_PROCESS_NAME);
				((Property)propertyList.get(0)).setValue(netstatInfo.getProcessName());
				// PID
				if (netstatInfo.getPid() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_PID);
					((Property)propertyList.get(0)).setValue(netstatInfo.getPid());
				}
				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_STATUS);
				((Property)propertyList.get(0)).setValue(netstatInfo.getStatus());
			}
		}

		// ---- ?????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PROCESS);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeProcessInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeProcessInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeProcessInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeProcessInfo processInfo = node.getNodeProcessInfo().get(i);

				// ??????????????????????????????????????????
				target.setValue(processInfo.getProcessName());
				// ????????????
				target.setStringHighlight(processInfo.isSearchTarget());

				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_NAME);
				((Property)propertyList.get(0)).setValue(processInfo.getProcessName());
				// PID
				if (processInfo.getPid() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_PID);
					((Property)propertyList.get(0)).setValue(processInfo.getPid());
				}
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_PATH);
				((Property)propertyList.get(0)).setValue(processInfo.getPath());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_EXEC_USER);
				((Property)propertyList.get(0)).setValue(processInfo.getExecUser());
				// ????????????
				if (processInfo.getStartupDateTime() != null && processInfo.getStartupDateTime() != 0) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_STARTUP_DATE_TIME);
					((Property)propertyList.get(0)).setValue(new Date(processInfo.getStartupDateTime()));
				}
			}
		}

		// ---- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PACKAGE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodePackageInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodePackageInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodePackageInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodePackageInfo packageInfo = node.getNodePackageInfo().get(i);

				// ???????????????????????????????????????ID???
				target.setValue(packageInfo.getPackageName());
				// ????????????
				target.setStringHighlight(packageInfo.isSearchTarget());

				// ???????????????ID
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_ID);
				((Property)propertyList.get(0)).setValue(packageInfo.getPackageId());
				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_NAME);
				((Property)propertyList.get(0)).setValue(packageInfo.getPackageName());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_VERSION);
				((Property)propertyList.get(0)).setValue(packageInfo.getVersion());
				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_RELEASE);
				((Property)propertyList.get(0)).setValue(packageInfo.getRelease());
				// ????????????????????????
				if (packageInfo.getInstallDate() != null && packageInfo.getInstallDate() != 0) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_INSTALL_DATE);
					((Property)propertyList.get(0)).setValue(new Date(packageInfo.getInstallDate()));
				}
				// ?????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_VENDOR);
				((Property)propertyList.get(0)).setValue(packageInfo.getVendor());
				// ?????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_ARCHITECTURE);
				((Property)propertyList.get(0)).setValue(packageInfo.getArchitecture());
			}
		}

		// ---- ???????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PRODUCT);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeProductInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeProductInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeProductInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeProductInfo productInfo = node.getNodeProductInfo().get(i);

				// ?????????????????????????????????
				target.setValue(productInfo.getProductName());
				// ????????????
				target.setStringHighlight(productInfo.isSearchTarget());

				// ??????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PRODUCT_NAME);
				((Property)propertyList.get(0)).setValue(productInfo.getProductName());
				// ???????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PRODUCT_VERSION);
				((Property)propertyList.get(0)).setValue(productInfo.getVersion());
				// ????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PRODUCT_PATH);
				((Property)propertyList.get(0)).setValue(productInfo.getPath());
			}
		}

		// ---- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.LICENSE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeLicenseInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeLicenseInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeLicenseInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeLicenseInfo licenseInfo = node.getNodeLicenseInfo().get(i);

				// ????????????????????????????????????
				target.setValue(licenseInfo.getProductName());
				// ????????????
				target.setStringHighlight(licenseInfo.isSearchTarget());

				// ?????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_PRODUCT_NAME);
				((Property)propertyList.get(0)).setValue(licenseInfo.getProductName());
				// ?????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_VENDOR);
				((Property)propertyList.get(0)).setValue(licenseInfo.getVendor());
				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_VENDOR_CONTACT);
				((Property)propertyList.get(0)).setValue(licenseInfo.getVendorContact());
				// ????????????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_SERIAL_NUMBER);
				((Property)propertyList.get(0)).setValue(licenseInfo.getSerialNumber());
				// ??????
				if (licenseInfo.getCount()!= null && licenseInfo.getCount() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_COUNT);
					((Property)propertyList.get(0)).setValue(licenseInfo.getCount());
				}
				// ????????????
				if (licenseInfo.getExpirationDate() != null && licenseInfo.getExpirationDate() != 0) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_EXPIRATION_DATE);
					((Property)propertyList.get(0)).setValue(new Date(licenseInfo.getExpirationDate()));
				}
			}
		}

		// ----- ??????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_NODE_VARIABLE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeVariableInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeVariableInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeVariableInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeVariableInfo variable = node.getNodeVariableInfo().get(i);

				// ?????????????????????????????????????????????
				target.setValue(variable.getNodeVariableName());
				// ????????????
				target.setStringHighlight(variable.isSearchTarget());

				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NODE_VARIABLE_NAME);
				((Property)propertyList.get(0)).setValue(variable.getNodeVariableName());
				// ??????????????????
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NODE_VARIABLE_VALUE);
				((Property)propertyList.get(0)).setValue(variable.getNodeVariableValue());
			}
		}

		// ---- ????????????????????? -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CUSTOM);
		Property parent = (Property)((Property)propertyList.get(0)).getParent();

		Property custom = (Property)propertyList.get(0);
		if (node.getNodeCustomInfo() != null) {
			parent.removeChildren();
			Property target = null;
			if(node.getNodeCustomInfo().size() <= 0){
				target = custom;
				parent.addChildren(target, 0);
				target.setName(Messages.getString("node.custom", locale));
				target.setValue("");
			}
			
			for (int i = 0; i < node.getNodeCustomInfo().size(); i++) {
				NodeCustomInfo customInfo = node.getNodeCustomInfo().get(i);
				target = PropertyUtil.copy(custom);
				parent.addChildren(target, i);
				// ??????
				target.setName(customInfo.getDisplayName());
				// ???(??????????????? \n ???????????????????????????
				target.setValue(customInfo.getValue());
				// ????????????
				target.setStringHighlight(customInfo.isSearchTarget());
			}
		}

		if (!isNodeMap) {
			// ----- ????????? -----
			// ??????????????????
			if (node.getJobPriority() != null) {
				propertyList = PropertyUtil.getProperty(property, NodeConstant.JOB_PRIORITY);
				((Property)propertyList.get(0)).setValue(node.getJobPriority());
			}
			// ??????????????????
			if (node.getJobMultiplicity() != null) {
				propertyList = PropertyUtil.getProperty(property, NodeConstant.JOB_MULTIPLICITY);
				((Property)propertyList.get(0)).setValue(node.getJobMultiplicity());
			}
	
	
			// ----- ?????????????????? -----
	
			// ----- SNMP?????? -----
			// SNMP???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_USER);
			((Property)propertyList.get(0)).setValue(node.getSnmpUser());
			// SNMP???????????????
			if (node.getSnmpPort() != null) {
				propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PORT);
				((Property)propertyList.get(0)).setValue(node.getSnmpPort());
			}
			// SNMP?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_COMMUNITY);
			((Property)propertyList.get(0)).setValue(node.getSnmpCommunity());
			// SNMP???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_VERSION);
			((Property)propertyList.get(0)).setValue(SnmpVersionConstant.typeToString(node.getSnmpVersion()));
			// SNMP???????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_SECURITY_LEVEL);
			((Property)propertyList.get(0)).setValue(node.getSnmpSecurityLevel());
			// SNMP???????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_AUTH_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getSnmpAuthPassword());
			// SNMP??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PRIV_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getSnmpPrivPassword());
			// SNMP?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_AUTH_PROTOCOL);
			((Property)propertyList.get(0)).setValue(node.getSnmpAuthProtocol());
			// SNMP????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PRIV_PROTOCOL);
			((Property)propertyList.get(0)).setValue(node.getSnmpPrivProtocol());
			// SNMP??????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMPTIMEOUT);
			((Property)propertyList.get(0)).setValue(node.getSnmpTimeout());
			// SNMP??????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMPRETRIES);
			((Property)propertyList.get(0)).setValue(node.getSnmpRetryCount());
	
			// ----- WBEM?????? -----
			// WBEM?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_PORT);
			((Property)propertyList.get(0)).setValue(node.getWbemPort());
			// WBEM???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_USER);
			((Property)propertyList.get(0)).setValue(node.getWbemUser());
			// WBEM??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_USER_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getWbemUserPassword());
			// WBEM?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_PROTOCOL);
			((Property)propertyList.get(0)).setValue(node.getWbemProtocol());
			// WBEM????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_TIMEOUT);
			((Property)propertyList.get(0)).setValue(node.getWbemTimeout());
			// WBEM????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_RETRIES);
			((Property)propertyList.get(0)).setValue(node.getWbemRetryCount());
	
			// ----- IPMI?????? -----
			// IPMI??????IP????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_IP_ADDRESS);
			((Property)propertyList.get(0)).setValue(node.getIpmiIpAddress());
			// IPMI?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_PORT);
			((Property)propertyList.get(0)).setValue(node.getIpmiPort());
			// IPMI???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_USER);
			((Property)propertyList.get(0)).setValue(node.getIpmiUser());
			// IPMI??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_USER_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getIpmiUserPassword());
			// IPMI????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_TIMEOUT);
			((Property)propertyList.get(0)).setValue(node.getIpmiTimeout());
			// IPMI????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_RETRIES);
			((Property)propertyList.get(0)).setValue(node.getIpmiRetries());
			// IPMI?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_PROTOCOL);
			((Property)propertyList.get(0)).setValue(node.getIpmiProtocol());
			// IPMI???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_LEVEL);
			((Property)propertyList.get(0)).setValue(node.getIpmiLevel());
	
			// ----- WinRM?????? -----
			// WinRM???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_USER);
			((Property)propertyList.get(0)).setValue(node.getWinrmUser());
			// WinRM??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_USER_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getWinrmUserPassword());
			// WinRM???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_VERSION);
			((Property)propertyList.get(0)).setValue(node.getWinrmVersion());
			// WinRM?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_PORT);
			((Property)propertyList.get(0)).setValue(node.getWinrmPort());
			// WinRM?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_PROTOCOL);
			((Property)propertyList.get(0)).setValue(node.getWinrmProtocol());
			// WinRM????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_TIMEOUT);
			((Property)propertyList.get(0)).setValue(node.getWinrmTimeout());
			// WinRM????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_RETRIES);
			((Property)propertyList.get(0)).setValue(node.getWinrmRetries());
	
			// ----- SSH?????? -----
			// SSH???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_USER);
			((Property)propertyList.get(0)).setValue(node.getSshUser());
			// SSH??????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_USER_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getSshUserPassword());
			// SSH????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_PRIVATE_KEY_FILEPATH);
			((Property)propertyList.get(0)).setValue(node.getSshPrivateKeyFilepath());
			// SSH???????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_PRIVATE_KEY_PASSPHRASE);
			((Property)propertyList.get(0)).setValue(node.getSshPrivateKeyPassphrase());
			// SSH???????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_PORT);
			((Property)propertyList.get(0)).setValue(node.getSshPort());
			// SSH??????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_TIMEOUT);
			((Property)propertyList.get(0)).setValue(node.getSshTimeout());
	
			// ----- ???????????????????????? -----
			// ????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDSERVICE);
			((Property)propertyList.get(0)).setValue(node.getCloudService());
			// ???????????????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDSCOPE);
			((Property)propertyList.get(0)).setValue(node.getCloudScope());
			// ?????????????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDRESOURCETYPE);
			((Property)propertyList.get(0)).setValue(node.getCloudResourceType());
			// ????????????????????????ID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDRESOURCEID);
			((Property)propertyList.get(0)).setValue(node.getCloudResourceId());
			// ???????????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDRESOURCENAME);
			((Property)propertyList.get(0)).setValue(node.getCloudResourceName());
			// ?????????????????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDLOCATION);
			((Property)propertyList.get(0)).setValue(node.getCloudLocation());
	
	
			// ----- ???????????? -----
			// ?????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CONTACT);
			((Property)propertyList.get(0)).setValue(node.getContact());
			// ?????????
			propertyList = PropertyUtil.getProperty(property, NodeConstant.ADMINISTRATOR);
			((Property)propertyList.get(0)).setValue(node.getAdministrator());
	
	
			// ----- ?????? -----
			propertyList = PropertyUtil.getProperty(property, NodeConstant.NOTE);
			Property noteProperty = (Property)propertyList.get(0);
			if (node.getNodeNoteInfo() != null) {
				int index = PropertyUtil.getPropertyIndex(property, noteProperty);
				int cnt = 0;
				for (NodeNoteInfo note : node.getNodeNoteInfo()) {
					Property target = null;
					if (cnt == 0) {
						target = noteProperty;
					} else {
						target = PropertyUtil.copy(noteProperty);
						property.addChildren(target, index + cnt);
					}
					// ??????
					target.setValue(note.getNote());
					cnt++;
				}
			}
		}

		return property;
	}


	/**
	 * ?????????????????????????????????????????????
	 *
	 * @param mode
	 * @param isNodeMap true:????????????????????????????????????????????????
	 * @return ???????????????????????????
	 */
	public static Property getProperty(String managerName, int mode, Locale locale, boolean isNodeMap) {

		// ------------------------
		// ---- ????????????-----
		// ------------------------

		// ---- ???????????????????????? -----
		//??????????????????ID
		Property facilityId =
				new Property(NodeConstant.FACILITY_ID, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_512);
		//?????????????????????
		Property facilityName =
				new Property(NodeConstant.FACILITY_NAME, Messages.getString("facility.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//??????
		Property description =
				new Property(NodeConstant.DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//??????/??????
		Property valid =
				new Property(NodeConstant.VALID, Messages.getString("management.object", locale), PropertyDefineConstant.EDITOR_BOOL);
		//???????????????????????????
		Property autoDeviceSearch =
				new Property(NodeConstant.AUTO_DEVICE_SEARCH, Messages.getString("auto.device.search", locale), PropertyDefineConstant.EDITOR_BOOL);
		//?????????
		Property createTime =
				new Property(NodeConstant.CREATE_TIME, Messages.getString("create.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//????????????
		Property creatorName =
				new Property(NodeConstant.CREATOR_NAME, Messages.getString("creator.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????
		Property modifyTime =
				new Property(NodeConstant.MODIFY_TIME, Messages.getString("update.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//????????????
		Property modifierName =
				new Property(NodeConstant.MODIFIER_NAME, Messages.getString("modifier.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- ???????????? -----
		//????????????
		Property basicInformation =
				new Property(NodeConstant.BASIC_INFORMATION, Messages.getString("basic.information", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- HW?????? -----
		//H/W
		Property hardware =
				new Property(NodeConstant.HARDWARE, Messages.getString("hardware", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//H/W?????????
		Property hardwareType =
				new Property(NodeConstant.HARDWARE_TYPE, Messages.getString("hardware.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//????????????????????????
		Property platformFamilyName =
				new Property(NodeConstant.PLATFORM_FAMILY_NAME, Messages.getString("platform.family.name", locale), PropertyDefineConstant.EDITOR_SELECT);
		//??????????????????????????????
		Property subPlatformFamilyName =
				new Property(NodeConstant.SUB_PLATFORM_FAMILY_NAME, Messages.getString("sub.platform.family.name", locale), PropertyDefineConstant.EDITOR_SELECT);
		//????????????
		Property nodeName =
				new Property(NodeConstant.NODE_NAME, Messages.getString("node.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//??????????????????????????????
		Property iconImage =
				new Property(NodeConstant.ICONIMAGE, Messages.getString("icon.image", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);


		// ---- IP?????????????????? -----
		//??????????????????
		Property network =
				new Property(NodeConstant.NETWORK, Messages.getString("network", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//IP???????????????
		Property ipAddressVersion =
				new Property(NodeConstant.IP_ADDRESS_VERSION, Messages.getString("ip.address.version", locale), PropertyDefineConstant.EDITOR_SELECT);
		//IP????????????V4
		Property ipAddressV4 =
				new Property(NodeConstant.IP_ADDRESS_V4, Messages.getString("ip.address.v4", locale), PropertyDefineConstant.EDITOR_IPV4);
		//IP????????????V6
		Property ipAddressV6 =
				new Property(NodeConstant.IP_ADDRESS_V6, Messages.getString("ip.address.v6", locale), PropertyDefineConstant.EDITOR_IPV6);

		// ----- Hinemos????????????????????????-----
		//Hinemos??????????????????
		Property agent =
				new Property(NodeConstant.AGENT, Messages.getString("agent", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//??????????????????????????????
		Property agentAwakePort =
				new Property(NodeConstant.AGENT_AWAKE_PORT, Messages.getString("agent.awake.port", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);

		// ---- ???????????? -----
		Property nodeConfigInformation =
				new Property(NodeConstant.NODE_CONFIG_INFORMATION, Messages.getString("node.config", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//????????????
		Property hostName =
				new Property(NodeConstant.HOST_NAME, Messages.getString("host.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		// ---- OS?????? -----
		//OS
		Property os =
				new Property(NodeConstant.OS, Messages.getString("os", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//OS???
		Property osName =
				new Property(NodeConstant.OS_NAME, Messages.getString("os.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//OS????????????
		Property osRelease =
				new Property(NodeConstant.OS_RELEASE, Messages.getString("os.release", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//OS???????????????
		Property osVersion =
				new Property(NodeConstant.OS_VERSION, Messages.getString("os.version", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//???????????????
		Property characterSet =
				new Property(NodeConstant.CHARACTER_SET, Messages.getString("character.set", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_16);
		//????????????
		Property osStartupDateTime =
				new Property(NodeConstant.OS_STARTUP_DATE_TIME, Messages.getString("os.startup.date.time", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// ---- ???????????????????????? -----
		//????????????
		Property device =
				new Property(NodeConstant.DEVICE, Messages.getString("device", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//??????????????????
		Property generalDevice =
				new Property(NodeConstant.GENERAL_DEVICE, Messages.getString("general.device", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//????????????????????????
		Property generalDeviceList =
				new Property(NodeConstant.GENERAL_DEVICE_LIST, Messages.getString("general.device.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????????????????
		Property deviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//???????????????
		Property deviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//????????????INDEX
		Property deviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//??????????????????
		Property deviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//?????????????????????
		Property deviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//???????????????????????????
		Property deviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//??????
		Property deviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- CPU?????????????????? -----
		//CPU
		Property cpu =
				new Property(NodeConstant.CPU, Messages.getString("cpu", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//CPU??????
		Property cpuList =
				new Property(NodeConstant.CPU_LIST, Messages.getString("cpu.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????????????????
		Property cpuDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//???????????????
		Property cpuDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//????????????INDEX
		Property cpuDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//??????????????????
		Property cpuDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//?????????????????????
		Property cpuDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//???????????????????????????
		Property cpuDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//??????
		Property cpuDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????
		Property cpuCoreCount =
				new Property(NodeConstant.CPU_CORE_COUNT, Messages.getString("cpu.core.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		//???????????????
		Property cpuThreadCount =
				new Property(NodeConstant.CPU_THREAD_COUNT, Messages.getString("cpu.thread.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		//???????????????
		Property cpuClockCount =
				new Property(NodeConstant.CPU_CLOCK_COUNT, Messages.getString("cpu.clock.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);


		// ---- MEM?????????????????? -----
		//MEM
		Property memory =
				new Property(NodeConstant.MEMORY, Messages.getString("memory", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//MEM
		Property memoryList =
				new Property(NodeConstant.MEMORY_LIST, Messages.getString("memory.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????????????????
		Property memoryDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//???????????????
		Property memoryDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//????????????INDEX
		Property memoryDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//??????????????????
		Property memoryDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//?????????????????????
		Property memoryDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//???????????????????????????
		Property memoryDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//??????
		Property memoryDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- NIC?????????????????? -----
		//NIC
		Property networkInterface =
				new Property(NodeConstant.NETWORK_INTERFACE, Messages.getString("network.interface", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//NIC??????
		Property networkInterfaceList =
				new Property(NodeConstant.NETWORK_INTERFACE_LIST, Messages.getString("network.interface.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????????????????
		Property networkInterfaceDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//???????????????
		Property networkInterfaceDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//????????????INDEX
		Property networkInterfaceDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//??????????????????
		Property networkInterfaceDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//?????????????????????
		Property networkInterfaceDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//???????????????????????????
		Property networkInterfaceDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//??????
		Property networkInterfaceDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//NIC IP????????????
		Property nicIpAddress =
				new Property(NodeConstant.NIC_IP_ADDRESS, Messages.getString("nic.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//NIC MAC????????????
		Property nicMacAddress =
				new Property(NodeConstant.NIC_MAC_ADDRESS, Messages.getString("nic.mac.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- DISK?????????????????? -----
		//DISK
		Property disk =
				new Property(NodeConstant.DISK, Messages.getString("disk", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//DISK??????
		Property diskList =
				new Property(NodeConstant.DISK_LIST, Messages.getString("disk.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????????????????
		Property diskDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//???????????????
		Property diskDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//????????????INDEX
		Property diskDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//??????????????????
		Property diskDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//?????????????????????
		Property diskDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//???????????????????????????
		Property diskDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//??????
		Property diskDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//DISK?????????
		Property diskRpm =
				new Property(NodeConstant.DISK_RPM, Messages.getString("disk.rpm", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);


		// ---- ?????????????????????????????????????????? -----
		//????????????????????????
		Property fileSystem =
				new Property(NodeConstant.FILE_SYSTEM, Messages.getString("file.system", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//????????????????????????
		Property fileSystemList =
				new Property(NodeConstant.FILE_SYSTEM_LIST, Messages.getString("file.system.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????????????????
		Property fileSystemDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//???????????????
		Property fileSystemDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//????????????INDEX
		Property fileSystemDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//??????????????????
		Property fileSystemDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//?????????????????????
		Property fileSystemDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//???????????????????????????
		Property fileSystemDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//??????
		Property fileSystemDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//??????????????????????????????
		Property fileSystemType =
				new Property(NodeConstant.FILE_SYSTEM_TYPE, Messages.getString("file.system.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// ---- ???????????????????????? -----
		Property nodeNetstat =
				new Property(NodeConstant.NETSTAT, Messages.getString("node.netstat", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeNetstatList =
				new Property(NodeConstant.NETSTAT_LIST, Messages.getString("node.netstat.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatProtocol =
				new Property(NodeConstant.NETSTAT_PROTOCOL, Messages.getString("node.netstat.protocol", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatLocalIpAddress =
				new Property(NodeConstant.NETSTAT_LOCAL_IP_ADDRESS, Messages.getString("node.netstat.local.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatLocalPort =
				new Property(NodeConstant.NETSTAT_LOCAL_PORT, Messages.getString("node.netstat.local.port", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatForeignIpAddress =
				new Property(NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS, Messages.getString("node.netstat.foreign.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatForeignPort =
				new Property(NodeConstant.NETSTAT_FOREIGN_PORT, Messages.getString("node.netstat.foreign.port", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatProcessName =
				new Property(NodeConstant.NETSTAT_PROCESS_NAME, Messages.getString("node.netstat.process.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatPid =
				new Property(NodeConstant.NETSTAT_PID, Messages.getString("node.netstat.pid", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		Property netstatStatus =
				new Property(NodeConstant.NETSTAT_STATUS, Messages.getString("node.netstat.status", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- ?????????????????? -----
		Property process =
				new Property(NodeConstant.PROCESS, Messages.getString("node.process", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processList =
				new Property(NodeConstant.PROCESS_LIST, Messages.getString("node.process.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processName =
				new Property(NodeConstant.PROCESS_NAME, Messages.getString("node.process.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processPid =
				new Property(NodeConstant.PROCESS_PID, Messages.getString("node.process.pid", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		Property processPath =
				new Property(NodeConstant.PROCESS_PATH, Messages.getString("node.process.path", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.TEXT);
		Property processExecUser =
				new Property(NodeConstant.PROCESS_EXEC_USER, Messages.getString("node.process.exec.user", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processStartupDateTime =
				new Property(NodeConstant.PROCESS_STARTUP_DATE_TIME, Messages.getString("node.process.startup.date.time", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// ---- ????????????????????? -----
		Property nodePackage =
				new Property(NodeConstant.PACKAGE, Messages.getString("node.package", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodePackageList =
				new Property(NodeConstant.PACKAGE_LIST, Messages.getString("node.package.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageId =
				new Property(NodeConstant.PACKAGE_ID, Messages.getString("node.package.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageName =
				new Property(NodeConstant.PACKAGE_NAME, Messages.getString("node.package.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageVersion =
				new Property(NodeConstant.PACKAGE_VERSION, Messages.getString("node.package.version", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageRelease =
				new Property(NodeConstant.PACKAGE_RELEASE, Messages.getString("node.package.release", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageInstallDate =
				new Property(NodeConstant.PACKAGE_INSTALL_DATE, Messages.getString("node.package.install.date", locale), PropertyDefineConstant.EDITOR_DATETIME);
		Property packageVendor =
				new Property(NodeConstant.PACKAGE_VENDOR, Messages.getString("node.package.vendor", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageArchitecture =
				new Property(NodeConstant.PACKAGE_ARCHITECTURE, Messages.getString("node.package.architecture", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- ???????????????????????? -----
		Property nodeProduct =
				new Property(NodeConstant.PRODUCT, Messages.getString("node.product", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeProductList =
				new Property(NodeConstant.PRODUCT_LIST, Messages.getString("node.product.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property productName =
				new Property(NodeConstant.PRODUCT_NAME, Messages.getString("node.product.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property productVersion =
				new Property(NodeConstant.PRODUCT_VERSION, Messages.getString("node.product.version", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property productPath =
				new Property(NodeConstant.PRODUCT_PATH, Messages.getString("node.product.path", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.TEXT);

		// ---- ??????????????? -----
		Property nodeLicense =
				new Property(NodeConstant.LICENSE, Messages.getString("node.license", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeLicenseList =
				new Property(NodeConstant.LICENSE_LIST, Messages.getString("node.license.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseProductName =
				new Property(NodeConstant.LICENSE_PRODUCT_NAME, Messages.getString("node.license.product.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseVendor =
				new Property(NodeConstant.LICENSE_VENDOR, Messages.getString("node.license.vendor", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseVendorContact =
				new Property(NodeConstant.LICENSE_VENDOR_CONTACT, Messages.getString("node.license.vendor.contact", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseSerialNumber =
				new Property(NodeConstant.LICENSE_SERIAL_NUMBER, Messages.getString("node.license.serial.number", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseCount =
				new Property(NodeConstant.LICENSE_COUNT, Messages.getString("node.license.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		Property licenseExpirationDate =
				new Property(NodeConstant.LICENSE_EXPIRATION_DATE, Messages.getString("node.license.expiration.date", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// ---- ??????????????? -----
		Property nodeVariable =
				new Property(NodeConstant.NODE_VARIABLE, Messages.getString("node.variable", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property generalNodeVariable =
				new Property(NodeConstant.GENERAL_NODE_VARIABLE, Messages.getString("node.variable", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property nodeVariableName =
				new Property(NodeConstant.NODE_VARIABLE_NAME, Messages.getString("node.variable.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		Property nodeVariableValues =
				new Property(NodeConstant.NODE_VARIABLE_VALUE, Messages.getString("node.variable.value", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- ????????????????????? -----
		Property nodeCustomList =
				new Property(NodeConstant.CUSTOM_LIST, Messages.getString("node.custom.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeCustom =
				new Property(NodeConstant.CUSTOM, Messages.getString("node.custom", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);


		//?????????
		Property job =
				new Property(NodeConstant.JOB, Messages.getString("job", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//??????????????????
		Property jobPriority =
				new Property(NodeConstant.JOB_PRIORITY, Messages.getString("job.priority", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//??????????????????
		Property jobMultiplicity =
				new Property(NodeConstant.JOB_MULTIPLICITY, Messages.getString("job.multiplicity", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);


		// ---- ???????????? -----
		//????????????
		Property service =
				new Property(NodeConstant.SERVICE, Messages.getString("service", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- SNMP?????? -----
		//SNMP
		Property snmp =
				new Property(NodeConstant.SNMP, Messages.getString("snmp", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//SNMP???????????????
		Property snmpUser =
				new Property(NodeConstant.SNMP_USER, Messages.getString("snmp.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SNMP???????????????
		Property snmpPort =
				new Property(NodeConstant.SNMP_PORT, Messages.getString("snmp.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//SNMP?????????????????????
		Property snmpCommunity =
				new Property(NodeConstant.SNMP_COMMUNITY, Messages.getString("community.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SNMP???????????????
		Property snmpVersion =
				new Property(NodeConstant.SNMP_VERSION, Messages.getString("snmp.version", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMP???????????????????????????
		Property snmpSecurityLevel =
				new Property(NodeConstant.SNMP_SECURITY_LEVEL, Messages.getString("snmp.security.level", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMP???????????????????????????
		Property snmpAuthPassword =
				new Property(NodeConstant.SNMP_AUTH_PASSWORD, Messages.getString("snmp.auth.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//SNMP??????????????????????????????
		Property snmpPrivPassword =
				new Property(NodeConstant.SNMP_PRIV_PASSWORD, Messages.getString("snmp.priv.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//SNMP?????????????????????
		Property snmpAuthProtocol =
				new Property(NodeConstant.SNMP_AUTH_PROTOCOL, Messages.getString("snmp.auth.protocol", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMP????????????????????????
		Property snmpPrivProtocol =
				new Property(NodeConstant.SNMP_PRIV_PROTOCOL, Messages.getString("snmp.priv.protocol", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMP??????????????????
		Property snmpTimeout =
				new Property(NodeConstant.SNMPTIMEOUT, Messages.getString("snmp.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//SNMP??????????????????
		Property snmpRetries =
				new Property(NodeConstant.SNMPRETRIES, Messages.getString("snmp.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);


		// ---- WBEM?????? -----
		//WBEM
		Property wbem =
				new Property(NodeConstant.WBEM, Messages.getString("wbem", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//WBEM???????????????
		Property wbemUser =
				new Property(NodeConstant.WBEM_USER, Messages.getString("wbem.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//WBEM??????????????????????????????
		Property wbemUserPassword =
				new Property(NodeConstant.WBEM_USER_PASSWORD, Messages.getString("wbem.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//WBEM?????????????????????
		Property wbemPort =
				new Property(NodeConstant.WBEM_PORT, Messages.getString("wbem.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//WBEM?????????????????????
		Property wbemProtocol =
				new Property(NodeConstant.WBEM_PROTOCOL, Messages.getString("wbem.protocol", locale),  PropertyDefineConstant.EDITOR_SELECT);
		//WBEM????????????????????????
		Property wbemTimeout =
				new Property(NodeConstant.WBEM_TIMEOUT, Messages.getString("wbem.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//WBEM????????????????????????
		Property wbemRetries =
				new Property(NodeConstant.WBEM_RETRIES, Messages.getString("wbem.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);


		// ---- IPMI?????? -----
		//IPMI
		Property ipmi =
				new Property(NodeConstant.IPMI, Messages.getString("ipmi", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//IPMI??????IP????????????
		Property ipmiIpAddress =
				new Property(NodeConstant.IPMI_IP_ADDRESS, Messages.getString("ipmi.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//IPMI?????????????????????
		Property ipmiPort =
				new Property(NodeConstant.IPMI_PORT, Messages.getString("ipmi.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//IPMI???????????????
		Property ipmiUser =
				new Property(NodeConstant.IPMI_USER, Messages.getString("ipmi.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//IPMI??????????????????????????????
		Property ipmiUserPassword =
				new Property(NodeConstant.IPMI_USER_PASSWORD, Messages.getString("ipmi.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//IPMI????????????????????????
		Property ipmiTimeout =
				new Property(NodeConstant.IPMI_TIMEOUT, Messages.getString("ipmi.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//IPMI????????????????????????
		Property ipmiRetries =
				new Property(NodeConstant.IPMI_RETRIES, Messages.getString("ipmi.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);
		//IPMI?????????????????????
		Property ipmiProtocol =
				new Property(NodeConstant.IPMI_PROTOCOL, Messages.getString("ipmi.protocol", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//IPMI???????????????
		Property ipmiLevel =
				new Property(NodeConstant.IPMI_LEVEL, Messages.getString("ipmi.level", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);

		// ---- WinRM?????? -----
		Property winrm =
				new Property(NodeConstant.WINRM, Messages.getString("winrm", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//WinRM???????????????
		Property winrmUser =
				new Property(NodeConstant.WINRM_USER, Messages.getString("winrm.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//WinRM??????????????????????????????
		Property winrmUserPassword =
				new Property(NodeConstant.WINRM_USER_PASSWORD, Messages.getString("winrm.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//WinRM???????????????
		Property winrmVersion =
				new Property(NodeConstant.WINRM_VERSION, Messages.getString("winrm.version", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//WinRM?????????????????????
		Property winrmPort =
				new Property(NodeConstant.WINRM_PORT, Messages.getString("winrm.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//WinRM?????????????????????
		Property winrmProtocol =
				new Property(NodeConstant.WINRM_PROTOCOL, Messages.getString("winrm.protocol", locale),  PropertyDefineConstant.EDITOR_SELECT);
		//WinRM????????????????????????
		Property winrmTimeout =
				new Property(NodeConstant.WINRM_TIMEOUT, Messages.getString("winrm.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//WinRM????????????????????????
		Property winrmRetries =
				new Property(NodeConstant.WINRM_RETRIES, Messages.getString("winrm.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);

		// ---- SSH?????? ----
		Property ssh =
				new Property(NodeConstant.SSH, Messages.getString("ssh", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SSH???????????????
		Property sshUser =
				new Property(NodeConstant.SSH_USER, Messages.getString("ssh.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SSH??????????????????????????????
		Property sshUserPassword =
				new Property(NodeConstant.SSH_USER_PASSWORD, Messages.getString("ssh.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//SSH????????????????????????
		Property sshPrivateKeyFilepath =
				new Property(NodeConstant.SSH_PRIVATE_KEY_FILEPATH, Messages.getString("ssh.private.key.filepath", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//SSH???????????????????????????
		Property sshPrivateKeyPassphrase =
				new Property(NodeConstant.SSH_PRIVATE_KEY_PASSPHRASE, Messages.getString("ssh.private.key.passphrase", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_1024);
		//SSH???????????????
		Property sshPort =
				new Property(NodeConstant.SSH_PORT, Messages.getString("ssh.port", locale),  PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//SSH??????????????????
		Property sshTimeout =
				new Property(NodeConstant.SSH_TIMEOUT, Messages.getString("ssh.timeout", locale),  PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		
		// ---- ???????????????????????????????????? -----
		//??????????????????
		Property cloudManagement =
				new Property(NodeConstant.CLOUD_MANAGEMENT, Messages.getString("cloud.management", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//????????????????????????
		Property cloudService =
				new Property(NodeConstant.CLOUDSERVICE, Messages.getString("cloud.service", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//????????????????????????
		Property cloudScope =
				new Property(NodeConstant.CLOUDSCOPE, Messages.getString("cloud.scope", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//?????????????????????????????????
		Property cloudResourceType =
				new Property(NodeConstant.CLOUDRESOURCETYPE, Messages.getString("cloud.resource.type", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//????????????????????????ID
		Property cloudResourceId =
				new Property(NodeConstant.CLOUDRESOURCEID, Messages.getString("cloud.resource.id", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//???????????????????????????
		Property cloudResourceName =
				new Property(NodeConstant.CLOUDRESOURCENAME, Messages.getString("cloud.resource.name", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//??????????????????????????????
		Property cloudLocation =
				new Property(NodeConstant.CLOUDLOCATION, Messages.getString("cloud.location", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// ---- ???????????? -----
		//??????
		Property maintenance =
				new Property(NodeConstant.MAINTENANCE, Messages.getString("maintenance", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//?????????
		Property administrator =
				new Property(NodeConstant.ADMINISTRATOR, Messages.getString("administrator", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//?????????
		Property contact =
				new Property(NodeConstant.CONTACT, Messages.getString("contact", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//??????
		Property note =
				new Property(NodeConstant.NOTE, Messages.getString("note", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.VARCHAR_1024);


		// ------------------------
		// ---- ????????? -----
		// ------------------------

		// ---- ???????????????????????? -----
		facilityId.setValue("");
		facilityName.setValue("");
		description.setValue("");
		valid.setValue(true);
		autoDeviceSearch.setValue(true);
		createTime.setValue("");
		creatorName.setValue("");
		modifyTime.setValue("");
		modifierName.setValue("");

		// ---- ???????????? -----
		basicInformation.setValue("");

		// ---- HW?????? -----
		hardware.setValue("");
		hardwareType.setValue("");
		platformFamilyName.setSelectValues(getPlatformNames(managerName));
		platformFamilyName.setValue("");
		subPlatformFamilyName.setSelectValues(getSubPlatformNames(managerName));
		subPlatformFamilyName.setValue("");
		nodeName.setValue("");
		iconImage.setValue("");

		// ---- IP?????????????????? -----
		network.setValue("");
		Object ipVersionValue[][] = {
				{ "4", "6" },
				{ 4, 6 }
		};
		ipAddressVersion.setSelectValues(ipVersionValue);
		ipAddressVersion.setValue(4);
		ipAddressV4.setValue("");
		ipAddressV6.setValue("");

		// ----- Hinemos???????????????????????? -----
		agent.setValue("");
		agentAwakePort.setValue("");

		// ---- ???????????? -----
		nodeConfigInformation.setValue("");
		
		// ---- ???????????? -----
		hostName.setValue("");

		// ---- OS?????? -----
		os.setValue("");
		osName.setValue("");
		osRelease.setValue("");
		osVersion.setValue("");
		characterSet.setValue("");
		osStartupDateTime.setValue("");
		
		// ---- ?????????????????? -----
		device.setValue("");

		// ---- ???????????????????????? -----
		generalDevice.setValue("");
		generalDeviceList.setValue("");
		deviceDisplayName.setValue("");
		deviceName.setValue("");
		deviceIndex.setValue("");
		deviceType.setValue("");
		deviceSize.setValue(0);
		deviceSizeUnit.setValue("");
		deviceDescription.setValue("");

		// ---- CPU?????? -----
		cpu.setValue("");
		cpuList.setValue("");
		cpuDeviceDisplayName.setValue("");
		cpuDeviceName.setValue("");
		cpuDeviceIndex.setValue("");
		cpuDeviceType.setValue(DeviceTypeConstant.DEVICE_CPU);
		cpuDeviceSize.setValue(0);
		cpuDeviceSizeUnit.setValue("");
		cpuDeviceDescription.setValue("");
		cpuClockCount.setValue(0);
		cpuThreadCount.setValue(0);
		cpuCoreCount.setValue(0);

		// ---- MEM?????? -----
		memory.setValue("");
		memoryList.setValue("");
		memoryDeviceDisplayName.setValue("");
		memoryDeviceName.setValue("");
		memoryDeviceIndex.setValue("");
		memoryDeviceType.setValue(DeviceTypeConstant.DEVICE_MEM);
		memoryDeviceSize.setValue(0);
		memoryDeviceSizeUnit.setValue("");
		memoryDeviceDescription.setValue("");

		// ---- NIC?????? -----
		networkInterface.setValue("");
		networkInterfaceList.setValue("");
		networkInterfaceDeviceDisplayName.setValue("");
		networkInterfaceDeviceName.setValue("");
		networkInterfaceDeviceIndex.setValue("");
		networkInterfaceDeviceType.setValue(DeviceTypeConstant.DEVICE_NIC);
		networkInterfaceDeviceSize.setValue(0);
		networkInterfaceDeviceSizeUnit.setValue("");
		networkInterfaceDeviceDescription.setValue("");
		nicIpAddress.setValue("");
		nicMacAddress.setValue("");

		// ---- DISK?????? -----
		disk.setValue("");
		diskList.setValue("");
		diskDeviceDisplayName.setValue("");
		diskDeviceName.setValue("");
		diskDeviceIndex.setValue("");
		diskDeviceType.setValue(DeviceTypeConstant.DEVICE_DISK);
		diskDeviceSize.setValue(0);
		diskDeviceSizeUnit.setValue("");
		diskDeviceDescription.setValue("");
		diskRpm.setValue("");

		// ---- ?????????????????????????????? -----
		fileSystem.setValue("");
		fileSystemList.setValue("");
		fileSystemDeviceDisplayName.setValue("");
		fileSystemDeviceName.setValue("");
		fileSystemDeviceIndex.setValue("");
		fileSystemDeviceType.setValue(DeviceTypeConstant.DEVICE_FILESYSTEM);
		fileSystemDeviceSize.setValue(0);
		fileSystemDeviceSizeUnit.setValue("");
		fileSystemDeviceDescription.setValue("");
		fileSystemType.setValue("");


		// ---- ???????????????????????? -----
		nodeNetstat.setValue("");
		nodeNetstatList.setValue("");
		netstatProtocol.setValue("");
		netstatLocalIpAddress.setValue("");
		netstatLocalPort.setValue("");
		netstatForeignIpAddress.setValue("");
		netstatForeignPort.setValue("");
		netstatProcessName.setValue("");
		netstatPid.setValue("");
		netstatStatus.setValue("");


		// ---- ?????????????????? -----
		process.setValue("");
		processList.setValue("");
		processName.setValue("");
		processPid.setValue("");
		processPath.setValue("");
		processExecUser.setValue("");
		processStartupDateTime.setValue("");


		// ---- ????????????????????? -----
		nodePackage.setValue("");
		nodePackageList.setValue("");
		packageId.setValue("");
		packageName.setValue("");
		packageVersion.setValue("");
		packageRelease.setValue("");
		packageInstallDate.setValue("");
		packageVendor.setValue("");
		packageArchitecture.setValue("");


		// ---- ???????????????????????? -----
		nodeProduct.setValue("");
		nodeProductList.setValue("");
		productName.setValue("");
		productVersion.setValue("");
		productPath.setValue("");

		// ---- ????????????????????? -----
		nodeLicense.setValue("");
		nodeLicenseList.setValue("");
		licenseProductName.setValue("");
		licenseVendor.setValue("");
		licenseVendorContact.setValue("");
		licenseSerialNumber.setValue("");
		licenseCount.setValue("");
		licenseExpirationDate.setValue("");
		
		// ---- ????????????????????? -----
		nodeVariable.setValue("");
		generalNodeVariable.setValue("");
		nodeVariableName.setValue("");
		nodeVariableValues.setValue("");

		// ---- ????????????????????? -----
		nodeCustomList.setValue("");
		nodeCustom.setValue("");

		// ---- ????????? -----
		job.setValue("");
		jobPriority.setValue(16);
		jobMultiplicity.setValue(0);

		// ---- ?????????????????? -----
		service.setValue("");

		// ---- SNMP?????? -----
		snmp.setValue("");
		Object snmpVersionValue[][] = {
				{ "", SnmpVersionConstant.STRING_V1, SnmpVersionConstant.STRING_V2, SnmpVersionConstant.STRING_V3 },
				{ "", SnmpVersionConstant.STRING_V1, SnmpVersionConstant.STRING_V2, SnmpVersionConstant.STRING_V3 }
		};
		Object snmpSecurityLevelValue[][] = {
				{ "", SnmpSecurityLevelConstant.NOAUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_PRIV },
				{ "", SnmpSecurityLevelConstant.NOAUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_PRIV },
		};
		Object snmpAuthProtocolValue[][] = {
				{ "", SnmpProtocolConstant.MD5, SnmpProtocolConstant.SHA },
				{ "", SnmpProtocolConstant.MD5, SnmpProtocolConstant.SHA },
		};
		Object snmpPrivProtocolValue[][] = {
				{ "", SnmpProtocolConstant.DES, SnmpProtocolConstant.AES },
				{ "", SnmpProtocolConstant.DES, SnmpProtocolConstant.AES },
		};
		snmpUser.setValue("");
		snmpPort.setValue("");
		snmpCommunity.setValue("");
		snmpVersion.setSelectValues(snmpVersionValue);
		snmpVersion.setValue("");
		snmpSecurityLevel.setSelectValues(snmpSecurityLevelValue);
		snmpSecurityLevel.setValue("");
		snmpAuthPassword.setValue("");
		snmpPrivPassword.setValue("");
		snmpAuthProtocol.setSelectValues(snmpAuthProtocolValue);
		snmpAuthProtocol.setValue("");
		snmpPrivProtocol.setSelectValues(snmpPrivProtocolValue);
		snmpPrivProtocol.setValue("");
		snmpTimeout.setValue("");
		snmpRetries.setValue("");
		snmpPort.setValue("");


		// ---- WBEM?????? -----
		wbem.setValue("");
		Object wbemProtocolValue[][] = {
				{"", "http", "https"},
				{"", "http", "https"}
		};
		wbemUser.setValue("");
		wbemUserPassword.setValue("");
		wbemPort.setValue("");
		wbemProtocol.setSelectValues(wbemProtocolValue);
		wbemProtocol.setValue("");
		wbemTimeout.setValue("");
		wbemRetries.setValue("");


		// ---- IPMI?????? -----
		ipmi.setValue("");
		ipmiIpAddress.setValue("");
		ipmiPort.setValue("");
		ipmiUser.setValue("");
		ipmiUserPassword.setValue("");
		ipmiTimeout.setValue("");
		ipmiRetries.setValue("");
		ipmiProtocol.setValue("");
		ipmiLevel.setValue("");

		// ---- WinRM?????? -----
		Object winrmProtocolValue[][] = {
				{"", "http", "https"},
				{"", "http", "https"}
		};
		winrm.setValue("");
		winrmUser.setValue("");
		winrmUserPassword.setValue("");
		winrmVersion.setValue("");
		winrmPort.setValue("");
		winrmProtocol.setSelectValues(winrmProtocolValue);
		winrmProtocol.setValue("");
		winrmTimeout.setValue("");
		winrmRetries.setValue("");

		// ---- SSH?????? -----
		ssh.setValue("");
		sshUser.setValue("");
		sshUserPassword.setValue("");
		sshPrivateKeyFilepath.setValue("");
		sshPrivateKeyPassphrase.setValue("");
		sshPort.setValue("");
		sshTimeout.setValue("");

		// ---- ???????????????????????????????????? -----
		cloudManagement.setValue("");
		cloudService.setValue("");
		cloudScope.setValue("");
		cloudResourceType.setValue("");
		cloudResourceId.setValue("");
		cloudResourceName.setValue("");
		cloudLocation.setValue("");


		// ---- ???????????? -----
		maintenance.setValue("");
		administrator.setValue("");
		contact.setValue("");
		note.setValue("");


		// ------------------------
		// ---- ???????????????????????????????????? -----
		// ------------------------
		//?????????????????????????????????????????????????????????
		if(mode == PropertyDefineConstant.MODE_ADD ||
				mode == PropertyDefineConstant.MODE_MODIFY){

			// ---- ???????????????????????? ----
			if(mode == PropertyDefineConstant.MODE_ADD){
				facilityId.setModify(PropertyDefineConstant.MODIFY_OK); // ??????????????????ID??????????????????
			}
			facilityName.setModify(PropertyDefineConstant.MODIFY_OK);
			description.setModify(PropertyDefineConstant.MODIFY_OK);
			valid.setModify(PropertyDefineConstant.MODIFY_OK);
			autoDeviceSearch.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ???????????? -----

			// ---- HW?????? -----
			hardwareType.setModify(PropertyDefineConstant.MODIFY_OK);
			platformFamilyName.setModify(PropertyDefineConstant.MODIFY_OK);
			subPlatformFamilyName.setModify(PropertyDefineConstant.MODIFY_OK);
			nodeName.setModify(PropertyDefineConstant.MODIFY_OK);
			iconImage.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- IP?????????????????? -----
			ipAddressVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			ipAddressV4.setModify(PropertyDefineConstant.MODIFY_OK);
			ipAddressV6.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ???????????? -----
			
			// ---- ???????????? -----
			hostName.setModify(PropertyDefineConstant.MODIFY_OK);
			hostName.setCopy(PropertyDefineConstant.COPY_OK);

			// ---- OS?????? -----
			osName.setModify(PropertyDefineConstant.MODIFY_OK);
			osRelease.setModify(PropertyDefineConstant.MODIFY_OK);
			osVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			characterSet.setModify(PropertyDefineConstant.MODIFY_OK);
			osStartupDateTime.setModify(PropertyDefineConstant.MODIFY_OK);
			
			// ---- ?????????????????? -----
			// ---- ???????????????????????? -----
			generalDevice.setCopy(PropertyDefineConstant.COPY_OK);
			deviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- CPU?????????????????? -----
			cpu.setCopy(PropertyDefineConstant.COPY_OK);
			cpuDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuCoreCount.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuThreadCount.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuClockCount.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- MEM?????????????????? -----
			memoryDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- NIC?????????????????? -----
			networkInterface.setCopy(PropertyDefineConstant.COPY_OK);
			networkInterfaceDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			nicIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			nicMacAddress.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- DISK?????????????????? -----
			disk.setCopy(PropertyDefineConstant.COPY_OK);
			diskDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			diskRpm.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ?????????????????????????????????????????? -----
			fileSystem.setCopy(PropertyDefineConstant.COPY_OK);
			fileSystemDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemType.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ???????????????????????? -----
			nodeNetstat.setCopy(PropertyDefineConstant.COPY_OK);
			netstatProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatLocalIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatLocalPort.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatForeignIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatForeignPort.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatProcessName.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatPid.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatStatus.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ?????????????????? -----
			process.setCopy(PropertyDefineConstant.COPY_OK);
			processName.setModify(PropertyDefineConstant.MODIFY_OK);
			processPid.setModify(PropertyDefineConstant.MODIFY_OK);
			processPath.setModify(PropertyDefineConstant.MODIFY_OK);
			processExecUser.setModify(PropertyDefineConstant.MODIFY_OK);
			processStartupDateTime.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ????????????????????? -----
			nodePackage.setCopy(PropertyDefineConstant.COPY_OK);
			packageId.setModify(PropertyDefineConstant.MODIFY_OK);
			packageName.setModify(PropertyDefineConstant.MODIFY_OK);
			packageVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			packageRelease.setModify(PropertyDefineConstant.MODIFY_OK);
			packageInstallDate.setModify(PropertyDefineConstant.MODIFY_OK);
			packageVendor.setModify(PropertyDefineConstant.MODIFY_OK);
			packageArchitecture.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ???????????????????????? -----
			nodeProduct.setCopy(PropertyDefineConstant.COPY_OK);
			productName.setModify(PropertyDefineConstant.MODIFY_OK);
			productVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			productPath.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ????????????????????? -----
			nodeLicense.setCopy(PropertyDefineConstant.COPY_OK);
			licenseProductName.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseVendor.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseVendorContact.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseSerialNumber.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseCount.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseExpirationDate.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ????????????????????? -----
			generalNodeVariable.setCopy(PropertyDefineConstant.COPY_OK);
			nodeVariableName.setModify(PropertyDefineConstant.MODIFY_OK);
			nodeVariableValues.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ????????????????????? -----
			nodeCustom.setModify(PropertyDefineConstant.MODIFY_NG);
			nodeCustom.setCopy(PropertyDefineConstant.COPY_NG);

			// ----- Hinemos???????????????????????? -----
			agentAwakePort.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ????????? -----
			jobPriority.setModify(PropertyDefineConstant.MODIFY_OK);
			jobMultiplicity.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ?????????????????? -----

			// ---- SNMP?????? -----
			snmpUser.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpPort.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpCommunity.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpSecurityLevel.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpAuthPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpPrivPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpAuthProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpPrivProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpRetries.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- WBEM?????? -----
			wbemUser.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemPort.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemRetries.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- IPMI?????? -----
			ipmiIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiPort.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiUser.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiRetries.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiLevel.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- WinRM?????? -----
			winrmUser.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmPort.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmRetries.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- SSH?????? -----
			sshUser.setModify(PropertyDefineConstant.MODIFY_OK);
			sshUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			sshPrivateKeyFilepath.setModify(PropertyDefineConstant.MODIFY_OK);
			sshPrivateKeyPassphrase.setModify(PropertyDefineConstant.MODIFY_OK);
			sshPort.setModify(PropertyDefineConstant.MODIFY_OK);
			sshTimeout.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ???????????????????????? -----
			cloudService.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudScope.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceType.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceId.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceName.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudLocation.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ???????????? -----
			administrator.setModify(PropertyDefineConstant.MODIFY_OK);
			contact.setModify(PropertyDefineConstant.MODIFY_OK);
			note.setCopy(PropertyDefineConstant.COPY_OK);
			note.setModify(PropertyDefineConstant.MODIFY_OK);
			note.setModify(PropertyDefineConstant.MODIFY_OK);

		}


		// ------------------------
		// ---- ?????????????????? -----
		// ------------------------

		// ---- ?????????????????????????????????
		Property property = new Property(null, null, "");
		property.removeChildren();

		if (!isNodeMap) {
			//????????????????????????
			property.addChildren(facilityId);
			property.addChildren(facilityName);
			property.addChildren(description);
			property.addChildren(valid);
			property.addChildren(autoDeviceSearch);
	
			//????????????
			property.addChildren(basicInformation);
		}

		if (!isNodeMap) {

			//????????????
			property.addChildren(nodeConfigInformation);

			//?????????
			property.addChildren(job);
			//????????????
			property.addChildren(service);
			//??????????????????????????????
			property.addChildren(cloudManagement);
			//??????
			property.addChildren(maintenance);
			//????????????????????????(??????)
			property.addChildren(createTime);
			property.addChildren(creatorName);
			property.addChildren(modifyTime);
			property.addChildren(modifierName);
			//????????????????????????(??????)
			property.addChildren(note);
	
			// ---- ?????????????????????
			basicInformation.removeChildren();
			basicInformation.addChildren(hardware);
			basicInformation.addChildren(network);
			basicInformation.addChildren(agent);
	
			// HW?????????
			hardware.removeChildren();
			hardware.addChildren(platformFamilyName);
			hardware.addChildren(subPlatformFamilyName);
			hardware.addChildren(hardwareType);
			hardware.addChildren(iconImage);
	
			// ???????????????????????????
			network.removeChildren();
			network.addChildren(ipAddressVersion);
			network.addChildren(ipAddressV4);
			network.addChildren(ipAddressV6);
			network.addChildren(nodeName);
	
			// Hinemos???????????????????????????
			agent.removeChildren();
			agent.addChildren(agentAwakePort);

			// ?????????????????????
			nodeConfigInformation.removeChildren();
			nodeConfigInformation.addChildren(hostName);
			nodeConfigInformation.addChildren(os);
			nodeConfigInformation.addChildren(device);
			nodeConfigInformation.addChildren(nodeNetstatList);
			nodeConfigInformation.addChildren(processList);
			nodeConfigInformation.addChildren(nodePackageList);
			nodeConfigInformation.addChildren(nodeProductList);
			nodeConfigInformation.addChildren(nodeLicenseList);
			nodeConfigInformation.addChildren(nodeCustomList);
			nodeConfigInformation.addChildren(nodeVariable);
		} else {

			// ?????????????????????
			property.addChildren(hostName);
			property.addChildren(os);
			property.addChildren(device);
			property.addChildren(nodeNetstatList);
			property.addChildren(processList);
			property.addChildren(nodePackageList);
			property.addChildren(nodeProductList);
			property.addChildren(nodeLicenseList);
			property.addChildren(nodeCustomList);
			property.addChildren(nodeVariable);			
		}

		// OS?????????
		os.removeChildren();
		os.addChildren(osName);
		os.addChildren(osRelease);
		os.addChildren(osVersion);
		os.addChildren(characterSet);
		os.addChildren(osStartupDateTime);

		// ---- ???????????????????????????
		device.removeChildren();
		device.addChildren(cpuList);
		device.addChildren(memoryList);
		device.addChildren(networkInterfaceList);
		device.addChildren(diskList);
		device.addChildren(fileSystemList);
		device.addChildren(generalDeviceList);

		// ???????????????????????????
		generalDeviceList.removeChildren();
		generalDeviceList.addChildren(generalDevice);

		generalDevice.removeChildren();
		generalDevice.addChildren(deviceDisplayName);
		generalDevice.addChildren(deviceName);
		generalDevice.addChildren(deviceIndex);
		generalDevice.addChildren(deviceType);
		generalDevice.addChildren(deviceSize);
		generalDevice.addChildren(deviceSizeUnit);
		generalDevice.addChildren(deviceDescription);

		// CPU?????????
		cpuList.removeChildren();
		cpuList.addChildren(cpu);

		cpu.removeChildren();
		cpu.addChildren(cpuDeviceDisplayName);
		cpu.addChildren(cpuDeviceName);
		cpu.addChildren(cpuDeviceIndex);
		cpu.addChildren(cpuDeviceType);
		cpu.addChildren(cpuDeviceSize);
		cpu.addChildren(cpuDeviceSizeUnit);
		cpu.addChildren(cpuDeviceDescription);
		cpu.addChildren(cpuCoreCount);
		cpu.addChildren(cpuThreadCount);
		cpu.addChildren(cpuClockCount);

		// MEM?????????
		memoryList.removeChildren();
		memoryList.addChildren(memory);

		memory.removeChildren();
		memory.addChildren(memoryDeviceDisplayName);
		memory.addChildren(memoryDeviceName);
		memory.addChildren(memoryDeviceIndex);
		memory.addChildren(memoryDeviceType);
		memory.addChildren(memoryDeviceSize);
		memory.addChildren(memoryDeviceSizeUnit);
		memory.addChildren(memoryDeviceDescription);

		// NIC?????????
		networkInterfaceList.removeChildren();
		networkInterfaceList.addChildren(networkInterface);

		networkInterface.removeChildren();
		networkInterface.addChildren(networkInterfaceDeviceDisplayName);
		networkInterface.addChildren(networkInterfaceDeviceName);
		networkInterface.addChildren(networkInterfaceDeviceIndex);
		networkInterface.addChildren(networkInterfaceDeviceType);
		networkInterface.addChildren(networkInterfaceDeviceSize);
		networkInterface.addChildren(networkInterfaceDeviceSizeUnit);
		networkInterface.addChildren(networkInterfaceDeviceDescription);
		networkInterface.addChildren(nicIpAddress);
		networkInterface.addChildren(nicMacAddress);

		// DISK?????????
		diskList.removeChildren();
		diskList.addChildren(disk);

		disk.removeChildren();
		disk.addChildren(diskDeviceDisplayName);
		disk.addChildren(diskDeviceName);
		disk.addChildren(diskDeviceIndex);
		disk.addChildren(diskDeviceType);
		disk.addChildren(diskDeviceSize);
		disk.addChildren(diskDeviceSizeUnit);
		disk.addChildren(diskDeviceDescription);
		disk.addChildren(diskRpm);

		// ?????????????????????????????????
		fileSystemList.removeChildren();
		fileSystemList.addChildren(fileSystem);

		fileSystem.removeChildren();
		fileSystem.addChildren(fileSystemDeviceDisplayName);
		fileSystem.addChildren(fileSystemDeviceName);
		fileSystem.addChildren(fileSystemDeviceIndex);
		fileSystem.addChildren(fileSystemDeviceType);
		fileSystem.addChildren(fileSystemDeviceSize);
		fileSystem.addChildren(fileSystemDeviceSizeUnit);
		fileSystem.addChildren(fileSystemDeviceDescription);
		fileSystem.addChildren(fileSystemType);

		// ---- ?????????????????????????????????
		nodeNetstatList.removeChildren();
		nodeNetstatList.addChildren(nodeNetstat);
		nodeNetstat.removeChildren();
		nodeNetstat.addChildren(netstatProtocol);
		nodeNetstat.addChildren(netstatLocalIpAddress);
		nodeNetstat.addChildren(netstatLocalPort);
		nodeNetstat.addChildren(netstatForeignIpAddress);
		nodeNetstat.addChildren(netstatForeignPort);
		nodeNetstat.addChildren(netstatProcessName);
		nodeNetstat.addChildren(netstatPid);
		nodeNetstat.addChildren(netstatStatus);

		// ---- ???????????????????????????
		processList.removeChildren();
		processList.addChildren(process);
		process.removeChildren();
		process.addChildren(processName);
		process.addChildren(processPid);
		process.addChildren(processPath);
		process.addChildren(processExecUser);
		process.addChildren(processStartupDateTime);

		// ---- ??????????????????????????????
		nodePackageList.removeChildren();
		nodePackageList.addChildren(nodePackage);
		nodePackage.removeChildren();
		nodePackage.addChildren(packageId);
		nodePackage.addChildren(packageName);
		nodePackage.addChildren(packageVersion);
		nodePackage.addChildren(packageRelease);
		nodePackage.addChildren(packageInstallDate);
		nodePackage.addChildren(packageVendor);
		nodePackage.addChildren(packageArchitecture);

		// ---- ?????????????????????????????????
		nodeProductList.removeChildren();
		nodeProductList.addChildren(nodeProduct);
		nodeProduct.removeChildren();
		nodeProduct.addChildren(productName);
		nodeProduct.addChildren(productVersion);
		nodeProduct.addChildren(productPath);

		// ---- ??????????????????????????????
		nodeLicenseList.removeChildren();
		nodeLicenseList.addChildren(nodeLicense);
		nodeLicense.removeChildren();
		nodeLicense.addChildren(licenseProductName);
		nodeLicense.addChildren(licenseVendor);
		nodeLicense.addChildren(licenseVendorContact);
		nodeLicense.addChildren(licenseSerialNumber);
		nodeLicense.addChildren(licenseCount);
		nodeLicense.addChildren(licenseExpirationDate);

		// ---- ??????????????????????????????
		nodeVariable.removeChildren();
		nodeVariable.addChildren(generalNodeVariable);
		generalNodeVariable.addChildren(nodeVariableName);
		generalNodeVariable.addChildren(nodeVariableValues);

		// ---- ??????????????????????????????
		nodeCustomList.removeChildren();
		nodeCustomList.addChildren(nodeCustom);

		if (!isNodeMap) {
			// ??????????????????
			job.removeChildren();
			job.addChildren(jobPriority);
			job.addChildren(jobMultiplicity);
	
			// ---- ???????????????????????????
			service.removeChildren();
			service.addChildren(snmp);
			service.addChildren(wbem);
			service.addChildren(ipmi);
			service.addChildren(winrm);
			service.addChildren(ssh);
	
			// SNMP?????????
			snmp.removeChildren();
			snmp.addChildren(snmpUser);
			snmp.addChildren(snmpPort);
			snmp.addChildren(snmpCommunity);
			snmp.addChildren(snmpVersion);
			snmp.addChildren(snmpSecurityLevel);
			snmp.addChildren(snmpAuthPassword);
			snmp.addChildren(snmpPrivPassword);
			snmp.addChildren(snmpAuthProtocol);
			snmp.addChildren(snmpPrivProtocol);
			snmp.addChildren(snmpTimeout);
			snmp.addChildren(snmpRetries);
	
			// WBEM?????????
			wbem.removeChildren();
			wbem.addChildren(wbemUser);
			wbem.addChildren(wbemUserPassword);
			wbem.addChildren(wbemPort);
			wbem.addChildren(wbemProtocol);
			wbem.addChildren(wbemTimeout);
			wbem.addChildren(wbemRetries);
	
			// IPMI?????????
			ipmi.removeChildren();
			ipmi.addChildren(ipmiIpAddress);
			ipmi.addChildren(ipmiPort);
			ipmi.addChildren(ipmiUser);
			ipmi.addChildren(ipmiUserPassword);
			ipmi.addChildren(ipmiTimeout);
			ipmi.addChildren(ipmiRetries);
			ipmi.addChildren(ipmiProtocol);
			ipmi.addChildren(ipmiLevel);
	
			// WinRM?????????
			winrm.removeChildren();
			winrm.addChildren(winrmUser);
			winrm.addChildren(winrmUserPassword);
			winrm.addChildren(winrmVersion);
			winrm.addChildren(winrmPort);
			winrm.addChildren(winrmProtocol);
			winrm.addChildren(winrmTimeout);
			winrm.addChildren(winrmRetries);
	
			// SSH?????????
			ssh.removeChildren();
			ssh.addChildren(sshUser);
			ssh.addChildren(sshUserPassword);
			ssh.addChildren(sshPrivateKeyFilepath);
			ssh.addChildren(sshPrivateKeyPassphrase);
			ssh.addChildren(sshPort);
			ssh.addChildren(sshTimeout);
	
			// ---- ?????????????????????????????????????????????
			cloudManagement.removeChildren();
			cloudManagement.addChildren(cloudService);
			cloudManagement.addChildren(cloudScope);
			cloudManagement.addChildren(cloudResourceType);
			cloudManagement.addChildren(cloudResourceId);
			cloudManagement.addChildren(cloudResourceName);
			cloudManagement.addChildren(cloudLocation);
	
			// ---- ?????????????????????
			maintenance.removeChildren();
			maintenance.addChildren(administrator);
			maintenance.addChildren(contact);
		}

		return property;
	}

	/**
	 * TODO HinemosManager???NodeInfo?????????????????????????????????!
	 * @param nodeInfo
	 */
	public static void setDefaultNode(NodeInfo nodeInfo) {
		nodeInfo.setFacilityType(FacilityConstant.TYPE_NODE);
		nodeInfo.setDisplaySortOrder(100);
		if (nodeInfo.getFacilityId() == null) {
			nodeInfo.setFacilityId("");
		}
		if (nodeInfo.getFacilityName() == null) {
			nodeInfo.setFacilityName("");
		}
		if (nodeInfo.getDescription() == null) {
			nodeInfo.setDescription("");
		}
		if (nodeInfo.isValid() == null) {
			nodeInfo.setValid(Boolean.TRUE);
		}
		if (nodeInfo.isAutoDeviceSearch() == null) {
			nodeInfo.setAutoDeviceSearch(Boolean.TRUE);
		}
		if (nodeInfo.getCreateUserId() == null) {
			nodeInfo.setCreateUserId("");
		}
		if (nodeInfo.getCreateDatetime() == null) {
			nodeInfo.setCreateDatetime(null);
		}
		if (nodeInfo.getModifyUserId() == null) {
			nodeInfo.setModifyUserId("");
		}
		if (nodeInfo.getModifyDatetime() == null) {
			nodeInfo.setModifyDatetime(null);
		}

		// HW
		if (nodeInfo.getPlatformFamily() == null) {
			nodeInfo.setPlatformFamily("");
		}
		if (nodeInfo.getSubPlatformFamily() == null) {
			nodeInfo.setSubPlatformFamily("");
		}
		if (nodeInfo.getHardwareType() == null) {
			nodeInfo.setHardwareType("");
		}
		if (nodeInfo.getIconImage() == null) {
			nodeInfo.setIconImage("");
		}

		// IP????????????
		if (nodeInfo.getIpAddressVersion() == null) {
			nodeInfo.setIpAddressVersion(4);
		}
		if (nodeInfo.getIpAddressV4() == null) {
			nodeInfo.setIpAddressV4("");
		}
		if (nodeInfo.getIpAddressV6() == null) {
			nodeInfo.setIpAddressV6("");
		}

		// OS
		if (nodeInfo.getNodeName() == null) {
			nodeInfo.setNodeName("");
		}
		if (nodeInfo.getNodeOsInfo() == null) {
			NodeOsInfo nodeOsInfo = new NodeOsInfo();
			nodeOsInfo.setFacilityId(nodeInfo.getFacilityId());
			nodeInfo.setNodeOsInfo(nodeOsInfo);
		}
		if (nodeInfo.getNodeOsInfo().getOsName() == null) {
			nodeInfo.getNodeOsInfo().setOsName("");
		}
		if (nodeInfo.getNodeOsInfo().getOsRelease() == null) {
			nodeInfo.getNodeOsInfo().setOsRelease("");
		}
		if (nodeInfo.getNodeOsInfo().getOsVersion() == null) {
			nodeInfo.getNodeOsInfo().setOsVersion("");
		}
		if (nodeInfo.getNodeOsInfo().getCharacterSet() == null) {
			nodeInfo.getNodeOsInfo().setCharacterSet("");
		}
		if (nodeInfo.getNodeOsInfo().getStartupDateTime() == null || nodeInfo.getNodeOsInfo().getStartupDateTime() <= 0) {
			nodeInfo.getNodeOsInfo().setStartupDateTime(null);
		}

		// Hinemos??????????????????
		if (nodeInfo.getAgentAwakePort() == null || nodeInfo.getAgentAwakePort() == -1) {
			nodeInfo.setAgentAwakePort(24005);
		}

		// JOB
		if (nodeInfo.getJobPriority() == null) {
			nodeInfo.setJobPriority(16);
		}
		if (nodeInfo.getJobMultiplicity() == null) {
			nodeInfo.setJobMultiplicity(0);
		}

		// SNMP
		if (nodeInfo.getSnmpUser() == null || "".equals(nodeInfo.getSnmpUser())) {
			nodeInfo.setSnmpUser("root");
		}
		if (nodeInfo.getSnmpAuthPassword() == null) {
			nodeInfo.setSnmpAuthPassword("");
		}
		if (nodeInfo.getSnmpPrivPassword() == null) {
			nodeInfo.setSnmpPrivPassword("");
		}
		if (nodeInfo.getSnmpPort() == null) {
			nodeInfo.setSnmpPort(161);
		}
		if (nodeInfo.getSnmpCommunity() == null) {
			nodeInfo.setSnmpCommunity("public");
		}
		if (nodeInfo.getSnmpVersion() == null) {
			nodeInfo.setSnmpVersion(SnmpVersionConstant.TYPE_V2);
		}
		if (nodeInfo.getSnmpSecurityLevel() == null) {
			nodeInfo.setSnmpSecurityLevel(SnmpSecurityLevelConstant.NOAUTH_NOPRIV);
		}
		if (nodeInfo.getSnmpAuthProtocol() == null) {
			nodeInfo.setSnmpAuthProtocol("");
		}
		if (nodeInfo.getSnmpPrivProtocol() == null) {
			nodeInfo.setSnmpPrivProtocol("");
		}
		if (nodeInfo.getSnmpTimeout() == null || nodeInfo.getSnmpTimeout() == -1) {
			nodeInfo.setSnmpTimeout(5000);
		}
		if (nodeInfo.getSnmpRetryCount() == null || nodeInfo.getSnmpRetryCount() == -1) {
			nodeInfo.setSnmpRetryCount(3);
		}

		// WBEM
		if (nodeInfo.getWbemUser() == null || "".equals(nodeInfo.getWbemUser())) {
			nodeInfo.setWbemUser("root");
		}
		if (nodeInfo.getWbemUserPassword() == null) {
			nodeInfo.setWbemUserPassword("");
		}
		if (nodeInfo.getWbemPort() == null || nodeInfo.getWbemPort() == -1) {
			nodeInfo.setWbemPort(5988);
		}
		if (nodeInfo.getWbemProtocol() == null || "".equals(nodeInfo.getWbemProtocol())) {
			nodeInfo.setWbemProtocol("http");
		}
		if (nodeInfo.getWbemTimeout() == null || nodeInfo.getWbemTimeout() == -1) {
			nodeInfo.setWbemTimeout(5000);
		}
		if (nodeInfo.getWbemRetryCount() == null || nodeInfo.getWbemRetryCount() == -1) {
			nodeInfo.setWbemRetryCount(3);
		}

		// IPMI
		if (nodeInfo.getIpmiIpAddress() == null) {
			nodeInfo.setIpmiIpAddress("");
		}
		if (nodeInfo.getIpmiPort() == null || nodeInfo.getIpmiPort() == -1) {
			nodeInfo.setIpmiPort(0);
		}
		if (nodeInfo.getIpmiUser() == null) {
			nodeInfo.setIpmiUser("");
		}
		if (nodeInfo.getIpmiUserPassword() == null) {
			nodeInfo.setIpmiUserPassword("");
		}
		if (nodeInfo.getIpmiTimeout() == null || nodeInfo.getIpmiTimeout() == -1) {
			nodeInfo.setIpmiTimeout(5000);
		}
		if (nodeInfo.getIpmiRetries() == null || nodeInfo.getIpmiRetries() == -1) {
			nodeInfo.setIpmiRetries(3);
		}
		if (nodeInfo.getIpmiProtocol() == null || "".equals(nodeInfo.getIpmiProtocol())) {
			nodeInfo.setIpmiProtocol("RMCP+");
		}
		if (nodeInfo.getIpmiLevel() == null) {
			nodeInfo.setIpmiLevel("");
		}

		// WinRM
		if (nodeInfo.getWinrmUser() == null) {
			nodeInfo.setWinrmUser("");
		}
		if (nodeInfo.getWinrmUserPassword() == null) {
			nodeInfo.setWinrmUserPassword("");
		}
		if (nodeInfo.getWinrmVersion() == null || "".equals(nodeInfo.getWinrmVersion())) {
			nodeInfo.setWinrmVersion("2.0");
		}
		if (nodeInfo.getWinrmPort() == null || nodeInfo.getWinrmPort() == -1) {
			nodeInfo.setWinrmPort(5985);
		}
		if (nodeInfo.getWinrmProtocol() == null || "".equals(nodeInfo.getWinrmProtocol())) {
			nodeInfo.setWinrmProtocol("http");
		}
		if (nodeInfo.getWinrmTimeout() == null || nodeInfo.getWinrmTimeout() == -1) {
			nodeInfo.setWinrmTimeout(5000);
		}
		if (nodeInfo.getWinrmRetries() == null || nodeInfo.getWinrmRetries() == -1) {
			nodeInfo.setWinrmRetries(3);
		}

		// SSH
		if (nodeInfo.getSshUser() == null || "".equals(nodeInfo.getSshUser())) {
			nodeInfo.setSshUser("root");
		}
		if (nodeInfo.getSshUserPassword() == null) {
			nodeInfo.setSshUserPassword("");
		}
		if (nodeInfo.getSshPrivateKeyFilepath() == null) {
			nodeInfo.setSshPrivateKeyFilepath("");
		}
		if (nodeInfo.getSshPrivateKeyPassphrase() == null) {
			nodeInfo.setSshPrivateKeyPassphrase("");
		}
		if (nodeInfo.getSshPort() == null) {
			nodeInfo.setSshPort(22);
		}
		if (nodeInfo.getSshTimeout() == null) {
			nodeInfo.setSshTimeout(50000);
		}

		// ??????????????????
		if (nodeInfo.getCloudService() == null) {
			nodeInfo.setCloudService("");
		}
		if (nodeInfo.getCloudScope() == null) {
			nodeInfo.setCloudScope("");
		}
		if (nodeInfo.getCloudResourceType() == null) {
			nodeInfo.setCloudResourceType("");
		}
		if (nodeInfo.getCloudResourceId() == null) {
			nodeInfo.setCloudResourceId("");
		}
		if (nodeInfo.getCloudResourceName() == null) {
			nodeInfo.setCloudResourceName("");
		}
		if (nodeInfo.getCloudLocation() == null) {
			nodeInfo.setCloudLocation("");
		}

		// ??????
		if (nodeInfo.getAdministrator() == null) {
			nodeInfo.setAdministrator("");
		}
		if (nodeInfo.getContact() == null) {
			nodeInfo.setContact("");
		}
	}

	/**
	 * ?????????????????????????????????????????????????????????ID???????????????????????????????????????????????????<BR>
	 * ?????????????????????????????????2???????????????Object[][]???????????????<BR>
	 * <PRE>
	 * {
	 *    {platformId1, platformId2, ...},
	 *    {platformName1, platformName2, ...}
	 * }
	 * </PRE>
	 *
	 * @return ?????????????????????????????????2????????????
	 */
	private static Object[][] getPlatformNames(String managerName) {
		// ???????????????????????????????????????????????????????????????
		if(getInstance().platformCache != null){
			return getInstance().platformCache;
		}
		/** ?????????????????? */
		Object[][] table = null;
		//Collection platforms = null;
		List<RepositoryTableInfo> platforms = null;
		ArrayList<String> platformIdList = null;
		ArrayList<String> platformNameList = null;

		/** ??????????????? */
		try {
			platformIdList = new ArrayList<String>();
			platformNameList = new ArrayList<String>();
			table = new Object[2][platformIdList.size()];
			if (managerName == null) {
				return table;
			}
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			platforms = wrapper.getPlatformList();

			if (platforms != null) {
				for (RepositoryTableInfo platform : platforms) {
					platformIdList.add(platform.getId());
					platformNameList.add(platform.getName() + "(" + platform.getId() + ")");
				}
			}

			table[PropertyDefineConstant.SELECT_VALUE] = platformIdList.toArray();
			table[PropertyDefineConstant.SELECT_DISP_TEXT] = platformNameList.toArray();
		} catch (InvalidRole_Exception e) {
			// ????????????????????????????????????????????????????????????????????????
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getPlatformNames(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		m_log.debug("getPlatformNames : cache created");
		getInstance().platformCache = table;
		return table;
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????ID?????????????????????????????????????????????????????????<BR>
	 * ?????????????????????????????????2???????????????Object[][]???????????????<BR>
	 * <PRE>
	 * {
	 *    {subPlatformId1, subPlatformId2, ...},
	 *    {subPlatformName1(subPlatformName), subPlatformName2(subPlatformId2), ...}
	 * }
	 * </PRE>
	 *
	 * @return ???????????????????????????????????????2????????????
	 */
	private static Object[][] getSubPlatformNames(String managerName) {
		// ???????????????????????????????????????????????????????????????
		if(getInstance().subPlatformCache != null){
			return getInstance().subPlatformCache;
		}
		/** ?????????????????? */
		Object[][] table = null;
		List<RepositoryTableInfo> subPlatforms = null;
		ArrayList<String> subPlatformIdList = new ArrayList<String>();
		ArrayList<String> subPlatformNameList = new ArrayList<String>();

		/** ??????????????? */
		try {
			table = new Object[2][subPlatformIdList.size()];
			if (managerName == null) {
				return table;
			}
			
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			subPlatforms = wrapper.getCollectorSubPlatformTableInfoList();

			subPlatformIdList.add("");
			subPlatformNameList.add("");
			if (subPlatforms != null) {
				for (RepositoryTableInfo subPlatform : subPlatforms) {
					subPlatformIdList.add(subPlatform.getId());
					subPlatformNameList.add(subPlatform.getName() + "(" + subPlatform.getId() + ")");
				}
			}

			table[PropertyDefineConstant.SELECT_VALUE] = subPlatformIdList.toArray();
			table[PropertyDefineConstant.SELECT_DISP_TEXT] = subPlatformNameList.toArray();
		} catch (InvalidRole_Exception e) {
			// ????????????????????????????????????????????????????????????????????????
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getSubPlatformNames(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		m_log.debug("getSubPlatformNames : cache created");
		getInstance().subPlatformCache = table;
		return table;
	}
}
