/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.conv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.platform.xml.CPUInfo;
import com.clustercontrol.utility.settings.platform.xml.DeviceInfo;
import com.clustercontrol.utility.settings.platform.xml.DiskInfo;
import com.clustercontrol.utility.settings.platform.xml.FSInfo;
import com.clustercontrol.utility.settings.platform.xml.HostnameInfo;
import com.clustercontrol.utility.settings.platform.xml.LicenseInfo;
import com.clustercontrol.utility.settings.platform.xml.MemoryInfo;
import com.clustercontrol.utility.settings.platform.xml.NetstatInfo;
import com.clustercontrol.utility.settings.platform.xml.NetworkInterfaceInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeInfo;
import com.clustercontrol.utility.settings.platform.xml.NodeVariableInfo;
import com.clustercontrol.utility.settings.platform.xml.NoteInfo;
import com.clustercontrol.utility.settings.platform.xml.PackageInfo;
import com.clustercontrol.utility.settings.platform.xml.ProcessInfo;
import com.clustercontrol.utility.settings.platform.xml.ProductInfo;
import com.clustercontrol.ws.repository.NodeCpuInfo;
import com.clustercontrol.ws.repository.NodeDeviceInfo;
import com.clustercontrol.ws.repository.NodeDiskInfo;
import com.clustercontrol.ws.repository.NodeFilesystemInfo;
import com.clustercontrol.ws.repository.NodeGeneralDeviceInfo;
import com.clustercontrol.ws.repository.NodeHostnameInfo;
import com.clustercontrol.ws.repository.NodeLicenseInfo;
import com.clustercontrol.ws.repository.NodeMemoryInfo;
import com.clustercontrol.ws.repository.NodeNetstatInfo;
import com.clustercontrol.ws.repository.NodeNetworkInterfaceInfo;
import com.clustercontrol.ws.repository.NodeNoteInfo;
import com.clustercontrol.ws.repository.NodeOsInfo;
import com.clustercontrol.ws.repository.NodePackageInfo;
import com.clustercontrol.ws.repository.NodeProcessInfo;
import com.clustercontrol.ws.repository.NodeProductInfo;

/**
 * ????????????????????????JavaBean???XML(Bean)???binding?????????????????????
 * ???????????????<BR>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class RepositoryConv {
	
	static final private String scopeSchemaType="E";
	static final private String scopeSchemaVersion="1";
	static final private String scopeSchemaRevision="2" ;
	
	static final private String nodeSchemaType="I";
	static final private String nodeSchemaVersion="1";
	static final private String nodeSchemaRevision="1" ;
	static private String schemaType="";
	static private String schemaVersion="";
	static private String schemaRevision="" ;
	
	/* ????????? */
	private  static Log log = LogFactory.getLog(RepositoryConv.class);

	static public int checkSchemaVersionScope(String type, String version ,String revision){
		
		schemaType=scopeSchemaType;
		schemaVersion=scopeSchemaVersion;
		schemaRevision=scopeSchemaRevision;
		
		return checkSchemaVersion(type, version ,revision);
	}
	
	static public int checkSchemaVersionNode(String type, String version ,String revision){
		
		schemaType=nodeSchemaType;
		schemaVersion=nodeSchemaVersion;
		schemaRevision=nodeSchemaRevision;
		
		return checkSchemaVersion(type, version ,revision);
	}
	
	
	/**
	 * XML??????????????????????????????????????????????????? */
	static private int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(schemaType, schemaVersion, schemaRevision,
				type, version, revision);
	}

	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersionScope(){

		schemaType=scopeSchemaType;
		schemaVersion=scopeSchemaVersion;
		schemaRevision=scopeSchemaRevision;
		
		return getSchemaVersionSub();
	}
	
	static public com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersionNode(){

		schemaType=nodeSchemaType;
		schemaVersion=nodeSchemaVersion;
		schemaRevision=nodeSchemaRevision;
		
		return getSchemaVersionSub();
	}
	
	/**
	 * ????????????????????????????????????????????????
	 * @return
	 */
	static private com.clustercontrol.utility.settings.platform.xml.SchemaInfo getSchemaVersionSub(){
	
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo schema =
			new com.clustercontrol.utility.settings.platform.xml.SchemaInfo();
		
		schema.setSchemaType(schemaType);
		schema.setSchemaVersion(schemaVersion);
		schema.setSchemaRevision(schemaRevision);
		
		return schema;
	}
	
	
	/**
	 * XML??????????????????????????????????????????????????????????????????????????????CPU???????????????????????????
	 * ??????????????????????????????????????????????????????????????????????????????????????????????????????
	 * ????????????????????????????????????????????????????????????????????????????????????
	 * ???????????????????????????????????????????????????????????????????????????
	 * ????????????????????????????????????????????????????????????<br>
	 * 
	 * @param xmlNodeInfo
	 *            ?????????????????????????????????
	 * @param hostnameList
	 *            ????????????????????????????????????
	 * @param cpuList
	 *            CPU????????????????????????
	 * @param memoryList
	 *            ?????????????????????????????????
	 * @param networkInterfaceList
	 *            ???????????????????????????????????????????????????????????????
	 * @param diskList
	 *            ????????????????????????????????????
	 * @param fsList
	 *            ????????????????????????????????????????????????
	 * @param deviceList
	 *            ??????????????????????????????????????????
	* @param netstatList
	 *            ????????????????????????????????????????????????
	 * @param licenseList
	 *            ???????????????????????????????????????
	 * @param productList
	 *            ??????????????????????????????????????????
	 * @param variableList
	 *            ???????????????????????????????????????
	 * @param noteList
	 *            ??????????????????????????????
	 * @return ?????????????????????????????????????????????
	 */
	public static com.clustercontrol.ws.repository.NodeInfo convNodeXml2Dto(
		NodeInfo xmlNodeInfo, HostnameInfo[] hostnameList, CPUInfo[] cpuList, MemoryInfo[] memoryList,
		NetworkInterfaceInfo[] networkInterfaceList, DiskInfo[] diskList, FSInfo[] fsList, DeviceInfo[] deviceList,
		NodeVariableInfo[] variableList, NoteInfo[] noteList) {
	
		com.clustercontrol.ws.repository.NodeInfo dto = new com.clustercontrol.ws.repository.NodeInfo();
		
		// ??????????????????????????????
		
		// ????????????????????????
		if(xmlNodeInfo.getFacilityId() != null
				&& !"".equals(xmlNodeInfo.getFacilityId())){
			dto.setFacilityId(xmlNodeInfo.getFacilityId());
			log.debug("(FaclityId) : " + xmlNodeInfo.toString());
			
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(FaclityId) : " + xmlNodeInfo.toString());
			
			//?????????????????????????????????????????????FacilityId???""????????????
			dto.setFacilityId("");
			return dto;
		}
		
		if(xmlNodeInfo.getFacilityName() != null
				&& !"".equals(xmlNodeInfo.getFacilityName())){
			dto.setFacilityName(xmlNodeInfo.getFacilityName());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(FaclityName) : " + xmlNodeInfo.getFacilityId());
			
			//?????????????????????????????????????????????FacilityId???""????????????
			dto.setFacilityId("");
			return dto;
		}
		
		dto.setOwnerRoleId(xmlNodeInfo.getOwnerRoleId());
		dto.setValid(xmlNodeInfo.getValidFlg());
		
		if(xmlNodeInfo.getDescription() != null){
			dto.setDescription(xmlNodeInfo.getDescription());
		}
		
		if(xmlNodeInfo.getPlatformFamily() != null
				&& !"".equals(xmlNodeInfo.getPlatformFamily())){
			dto.setPlatformFamily(xmlNodeInfo.getPlatformFamily());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(PlatformFamily) : " + xmlNodeInfo.getFacilityId());
			//?????????????????????????????????????????????PlatformFamily???""????????????
			dto.setPlatformFamily("");
			return dto;
		}
		
		if(xmlNodeInfo.getSubPlatformFamily() != null
				&& !"".equals(xmlNodeInfo.getSubPlatformFamily())){
			dto.setSubPlatformFamily(xmlNodeInfo.getSubPlatformFamily());
		}
		
		if(xmlNodeInfo.getHardwareType() != null){
			dto.setHardwareType(xmlNodeInfo.getHardwareType());
		}
		
		if(xmlNodeInfo.getIconImage() != null){
			dto.setIconImage(xmlNodeInfo.getIconImage());
		}

		// SNMP????????????????????????
		if(xmlNodeInfo.getSnmpPort() >= 1 && xmlNodeInfo.getSnmpPort() <=65767){
			dto.setSnmpPort(xmlNodeInfo.getSnmpPort());
		}

		if(xmlNodeInfo.getSnmpCommunity() != null){
			dto.setSnmpCommunity(xmlNodeInfo.getSnmpCommunity());
		}
		dto.setSnmpVersion(xmlNodeInfo.getSnmpVersion());

		if(xmlNodeInfo.getSnmpTimeout() >=1 ){
			dto.setSnmpTimeout(xmlNodeInfo.getSnmpTimeout());
		}

		if(xmlNodeInfo.getSnmpRetryCount() >=1){
			dto.setSnmpRetryCount(xmlNodeInfo.getSnmpRetryCount());
		}

		// WBEM????????????????????????
		if(xmlNodeInfo.getWbemUser() != null && !"".equals(xmlNodeInfo.getWbemUser())){
			dto.setWbemUser(xmlNodeInfo.getWbemUser());
		}

		if(xmlNodeInfo.getWbemUserPassword() != null ){
			dto.setWbemUserPassword(xmlNodeInfo.getWbemUserPassword());
		}

		if(xmlNodeInfo.getWbemPort() >= 1 && xmlNodeInfo.getWbemPort() <=65767){
			dto.setWbemPort(xmlNodeInfo.getWbemPort());
		}

		if(xmlNodeInfo.getWbemProtocol() != null){
			dto.setWbemProtocol(xmlNodeInfo.getWbemProtocol());
		}
		
		if(xmlNodeInfo.getWbemTimeout() >=1 ){
			dto.setWbemTimeout(xmlNodeInfo.getWbemTimeout());
		}
		
		if(xmlNodeInfo.getWbemRetryCount() >=1){
			dto.setWbemRetryCount(xmlNodeInfo.getWbemRetryCount());
		}
		
		if (xmlNodeInfo.getIpAddressVersion() == 4
				|| xmlNodeInfo.getIpAddressVersion() == 6) {
			dto.setIpAddressVersion(xmlNodeInfo.getIpAddressVersion());
		}
		
		if(xmlNodeInfo.getIpAddressV4() != null
				&& !"".equals(xmlNodeInfo.getIpAddressV4()) ){
			dto.setIpAddressV4(xmlNodeInfo.getIpAddressV4());
		}else{
			//IP?????????????????????????????????????????????4???????????????V4?????????????????????
			if(xmlNodeInfo.getIpAddressVersion() != 6){
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
						+ "(IPv4) : " + xmlNodeInfo.getFacilityId());
				//?????????????????????????????????????????????FacilityId???""????????????
				dto.setFacilityId("");
				return dto;
			}
		}
		
		if(xmlNodeInfo.getIpAddressV6() !=null
				&& !"".equals(xmlNodeInfo.getIpAddressV6())){
			dto.setIpAddressV6(xmlNodeInfo.getIpAddressV6());
		}else{
			//IP??????????????????????????????6???????????????V6?????????????????????
			if(xmlNodeInfo.getIpAddressVersion() == 6){
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
						+ "(IPv6) : " + xmlNodeInfo.getFacilityId());
				//?????????????????????????????????????????????FacilityId???""????????????
				dto.setFacilityId("");
				return dto;
			}
			
		}
		
		if(xmlNodeInfo.getNodeName() != null
				&& !"".equals(xmlNodeInfo.getNodeName())){
			dto.setNodeName(xmlNodeInfo.getNodeName());
		}else{
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
						+ "(NodeName) : " + xmlNodeInfo.getFacilityId());
				//?????????????????????????????????????????????FacilityId???""????????????
				dto.setFacilityId("");
				return dto;
		}

		if (dto.getNodeOsInfo() == null) {
			dto.setNodeOsInfo(new NodeOsInfo());
			dto.getNodeOsInfo().setFacilityId(dto.getFacilityId());
		}
		if(xmlNodeInfo.getOsName() != null
				&& !"".equals(xmlNodeInfo.getOsName())){
			dto.getNodeOsInfo().setOsName(xmlNodeInfo.getOsName());
		}
		
		if(xmlNodeInfo.getOsRelease() != null
				&& !"".equals(xmlNodeInfo.getOsRelease())){
			dto.getNodeOsInfo().setOsRelease(xmlNodeInfo.getOsRelease());
		}
		
		if(xmlNodeInfo.getOsVersion() != null
				&& !"".equals(xmlNodeInfo.getOsVersion())){
			dto.getNodeOsInfo().setOsVersion(xmlNodeInfo.getOsVersion());
		}
		
		if(xmlNodeInfo.getCharacterSet() != null
				&& !"".equals(xmlNodeInfo.getCharacterSet())){
			dto.getNodeOsInfo().setCharacterSet(xmlNodeInfo.getCharacterSet());
		}
		
		if(xmlNodeInfo.getStartupDateTime() != null
				&& !"".equals(xmlNodeInfo.getStartupDateTime())){
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				dto.getNodeOsInfo().setStartupDateTime(dateFormat.parse(xmlNodeInfo.getStartupDateTime()).getTime());
			} catch (NullPointerException | ParseException e) {
				log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
						+ "(StartupDateTime) : " + xmlNodeInfo.getFacilityId());
				dto.setFacilityId("");
				return dto;
			}
		}
		
		if(xmlNodeInfo.getAdministrator() != null
				&& !"".equals(xmlNodeInfo.getAdministrator())){
			dto.setAdministrator(xmlNodeInfo.getAdministrator());
		}
		
		if(xmlNodeInfo.getContact() != null
				&& !"".equals(xmlNodeInfo.getContact())){
			dto.setContact(xmlNodeInfo.getContact());
		}
		
		//Hinemos???????????????????????????????????????
		dto.setAgentAwakePort(xmlNodeInfo.getAgentAwakePort());
		
		//?????????????????????????????????
		dto.setJobMultiplicity(xmlNodeInfo.getJobMultiplicity());
		dto.setJobPriority(xmlNodeInfo.getJobPriority());
		
		/*???????????????*/
		
		dto.setAutoDeviceSearch(xmlNodeInfo.getAutoDeviceSearch());
		
		/*IPMI????????????????????????*/
		if(xmlNodeInfo.getIpmiIpAddress() != null && !"".equals(xmlNodeInfo.getIpmiIpAddress())){
			dto.setIpmiIpAddress(xmlNodeInfo.getIpmiIpAddress());
		}
		
		if(xmlNodeInfo.getIpmiPort() >= 1 && xmlNodeInfo.getIpmiPort() <=65767){
			dto.setIpmiPort(xmlNodeInfo.getIpmiPort());
		}
		
		if(xmlNodeInfo.getIpmiUser() != null && !"".equals(xmlNodeInfo.getIpmiUser())){
			dto.setIpmiUser(xmlNodeInfo.getIpmiUser());
		}
		
		if(xmlNodeInfo.getIpmiUserPassword() != null && !"".equals(xmlNodeInfo.getIpmiUserPassword())){
			dto.setIpmiUserPassword(xmlNodeInfo.getIpmiUserPassword());
		}
		
		if(xmlNodeInfo.getIpmiTimeout() >=1 ){
			dto.setIpmiTimeout(xmlNodeInfo.getIpmiTimeout());
		}
		
		if(xmlNodeInfo.getIpmiRetryCount() >=1){
			dto.setIpmiRetries(xmlNodeInfo.getIpmiRetryCount());
		}
		
		if(xmlNodeInfo.getIpmiProtocol() != null){
			dto.setIpmiProtocol(xmlNodeInfo.getIpmiProtocol());
		}
		
		if(xmlNodeInfo.getIpmiLevel() != null){
			dto.setIpmiLevel(xmlNodeInfo.getIpmiLevel());
		}
		
		/*WinRM????????????????????????*/
		if(xmlNodeInfo.getWinrmUser() != null && !"".equals(xmlNodeInfo.getWinrmUser())){
			dto.setWinrmUser(xmlNodeInfo.getWinrmUser());
		}
		
		if(xmlNodeInfo.getWinrmUserPassword() != null && !"".equals(xmlNodeInfo.getWinrmUserPassword())){
			dto.setWinrmUserPassword(xmlNodeInfo.getWinrmUserPassword());
		}
		
		if(xmlNodeInfo.getWinrmVersion() != null){
			dto.setWinrmVersion(xmlNodeInfo.getWinrmVersion());
		}

		if(xmlNodeInfo.getWinrmPort() >= 1 && xmlNodeInfo.getWinrmPort() <=65767){
			dto.setWinrmPort(xmlNodeInfo.getWinrmPort());
		}

		if(xmlNodeInfo.getWinrmProtocol() != null){
			dto.setWinrmProtocol(xmlNodeInfo.getWinrmProtocol());
		}
		

		if(xmlNodeInfo.getWinrmTimeout() >=1 ){
			dto.setWinrmTimeout(xmlNodeInfo.getWinrmTimeout());
		}
		
		if(xmlNodeInfo.getWinrmRetryCount() >=1){
			dto.setWinrmRetries(xmlNodeInfo.getWinrmRetryCount());
		}
		
		/*SNMP??????????????????????????????*/
		if(xmlNodeInfo.getSnmpAuthPassword() != null && !"".equals(xmlNodeInfo.getSnmpAuthPassword())){
			dto.setSnmpAuthPassword(xmlNodeInfo.getSnmpAuthPassword());
		}

		if(null != xmlNodeInfo.getSnmpAuthProtocol()){
			dto.setSnmpAuthProtocol(xmlNodeInfo.getSnmpAuthProtocol());
		}

		if(xmlNodeInfo.getSnmpCommunity() != null && !"".equals(xmlNodeInfo.getSnmpAuthProtocol())){
			dto.setSnmpAuthProtocol(xmlNodeInfo.getSnmpAuthProtocol());
		}
		
		dto.setSnmpPort(xmlNodeInfo.getSnmpPort());
		dto.setSnmpRetryCount(xmlNodeInfo.getSnmpRetryCount());
		dto.setSnmpTimeout(xmlNodeInfo.getSnmpTimeout());
		
		if(xmlNodeInfo.getSnmpPrivPassword() != null && !"".equals(xmlNodeInfo.getSnmpPrivPassword())){
			dto.setSnmpPrivPassword(xmlNodeInfo.getSnmpPrivPassword());
		}
		
		if(null != xmlNodeInfo.getSnmpPrivProtocol()){
			dto.setSnmpPrivProtocol(xmlNodeInfo.getSnmpPrivProtocol());
		}

		if(null != xmlNodeInfo.getSnmpSecurityLevel()){
			dto.setSnmpSecurityLevel(xmlNodeInfo.getSnmpSecurityLevel());
		}

		if(xmlNodeInfo.getSnmpUser() != null && !"".equals(xmlNodeInfo.getSnmpUser())){
			dto.setSnmpUser(xmlNodeInfo.getSnmpUser());
		}
		
		dto.setSnmpVersion(xmlNodeInfo.getSnmpVersion());
		

		/*SSH??????????????????????????????*/
		if(xmlNodeInfo.getSshUserPassword() != null && !"".equals(xmlNodeInfo.getSshUserPassword())){
			dto.setSshUserPassword(xmlNodeInfo.getSshUserPassword());
		}

		if(xmlNodeInfo.getSshPrivateKeyFilename() != null && !"".equals(xmlNodeInfo.getSshPrivateKeyFilename())){
			dto.setSshPrivateKeyFilepath(xmlNodeInfo.getSshPrivateKeyFilename());
		}
		
		if(xmlNodeInfo.getSshPrivateKeyPassphrase() != null && !"".equals(xmlNodeInfo.getSshPrivateKeyPassphrase())){
			dto.setSshPrivateKeyPassphrase(xmlNodeInfo.getSshPrivateKeyPassphrase());
		}
		
		dto.setSshPort(xmlNodeInfo.getSshPort());
		dto.setSshTimeout(xmlNodeInfo.getSshTimeout());

		if(xmlNodeInfo.getSshUser() != null && !"".equals(xmlNodeInfo.getSshUser())){
			dto.setSshUser(xmlNodeInfo.getSshUser());
		}

		/*WBEM??????????????????????????????*/
		if(xmlNodeInfo.getWbemUserPassword() != null && !"".equals(xmlNodeInfo.getWbemUserPassword())){
			dto.setWbemUserPassword(xmlNodeInfo.getWbemUserPassword());
		}

		if(xmlNodeInfo.getWbemProtocol() != null && !"".equals(xmlNodeInfo.getWbemProtocol())){
			dto.setWbemProtocol(xmlNodeInfo.getWbemProtocol());
		}
		
		dto.setWbemPort(xmlNodeInfo.getWbemPort());
		dto.setWbemTimeout(xmlNodeInfo.getWbemTimeout());
		dto.setWbemRetryCount(xmlNodeInfo.getWbemRetryCount());

		if(xmlNodeInfo.getWbemUser() != null && !"".equals(xmlNodeInfo.getWbemUser())){
			dto.setWbemUser(xmlNodeInfo.getWbemUser());
		}
		
		/*??????????????????????????????????????????*/
		if(xmlNodeInfo.getCloudService() != null && !"".equals(xmlNodeInfo.getCloudService())){
			dto.setCloudService(xmlNodeInfo.getCloudService());
		}
		
		if(xmlNodeInfo.getCloudScope() != null && !"".equals(xmlNodeInfo.getCloudScope())){
			dto.setCloudScope(xmlNodeInfo.getCloudScope());
		}
		
		if(xmlNodeInfo.getCloudResourceType() != null && !"".equals(xmlNodeInfo.getCloudResourceType())){
			dto.setCloudResourceType(xmlNodeInfo.getCloudResourceType());
		}
		
		if(xmlNodeInfo.getCloudResourceId() != null && !"".equals(xmlNodeInfo.getCloudResourceId())){
			dto.setCloudResourceId(xmlNodeInfo.getCloudResourceId());
		}
		
		if(xmlNodeInfo.getCloudLocation() != null && !"".equals(xmlNodeInfo.getCloudLocation())){
			dto.setCloudLocation(xmlNodeInfo.getCloudLocation());
		}
		
		if(xmlNodeInfo.getCloudResourceName() != null && !"".equals(xmlNodeInfo.getCloudResourceName())){
			dto.setCloudResourceName(xmlNodeInfo.getCloudResourceName());
		}
		
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		dto.setNodePackageRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
		dto.setNodeProcessRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
		dto.setNodeProductRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
		dto.setNodeLicenseRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
		dto.setNodeNetstatRegisterFlag(NodeRegisterFlagConstant.NOT_GET);
		dto.setNodeCpuRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		dto.setNodeDiskRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		dto.setNodeFilesystemRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		dto.setNodeHostnameRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		dto.setNodeMemoryRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		dto.setNodeNetworkInterfaceRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		dto.setNodeOsRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		dto.setNodeVariableRegisterFlag(NodeRegisterFlagConstant.GET_SUCCESS);
		
		// ???????????????????????????
		List<NodeHostnameInfo> nodeHostnameList = dto.getNodeHostnameInfo();
		for (int i = 0; i < hostnameList.length; i++) {
			
			NodeHostnameInfo nodeHostnameInfo = new NodeHostnameInfo();
			
			if (hostnameList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(hostnameList[i].getHostname() != null
						&& !"".equals(hostnameList[i].getHostname())){
					
					nodeHostnameInfo.setHostname(hostnameList[i].getHostname());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(Hostname) : " + xmlNodeInfo.getFacilityId());
						continue;
				}
				
				nodeHostnameList.add(nodeHostnameInfo);
				
			}
		}
		

		// CPU???????????????
		List<NodeCpuInfo> nodeCPUList = dto.getNodeCpuInfo();
		for (int i = 0; i < cpuList.length; i++) {
			
			NodeCpuInfo nodeCpuInfo = new NodeCpuInfo();
			
			if (cpuList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(cpuList[i].getDeviceDisplayName() != null
						&& !"".equals(cpuList[i].getDeviceDisplayName())){
					
					nodeCpuInfo.setDeviceDisplayName(cpuList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceName() != null
					&& !"".equals(cpuList[i].getDeviceName())){
					
					nodeCpuInfo.setDeviceName(cpuList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceIndex() >= 0){
					nodeCpuInfo.setDeviceIndex(cpuList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceType() != null
						&& !"".equals(cpuList[i].getDeviceType())){
					nodeCpuInfo.setDeviceType(cpuList[i].getDeviceType());
				}
				
				if(cpuList[i].getDeviceSize() >= 0 ){
					
					nodeCpuInfo.setDeviceSize(cpuList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(cpuList[i].getDeviceSizeUnit() != null
						&& !"".equals(cpuList[i].getDeviceSizeUnit())){
					
					nodeCpuInfo.setDeviceSizeUnit(cpuList[i].getDeviceSizeUnit());
					
				}
				
				if(cpuList[i].getDeviceDescription() != null
						&& !"".equals(cpuList[i].getDeviceDescription())){
					
					nodeCpuInfo.setDeviceDescription(cpuList[i].getDeviceDescription());
					
				}
				
				if(cpuList[i].getCoreCount() >= 0) {
					nodeCpuInfo.setCoreCount(cpuList[i].getCoreCount());
				}
				
				if(cpuList[i].getThreadCount() >= 0) {
					nodeCpuInfo.setThreadCount(cpuList[i].getThreadCount());
				}
		
				if(cpuList[i].getClockCount() >= 0) {
					nodeCpuInfo.setClockCount(cpuList[i].getClockCount());
				}
				nodeCPUList.add(nodeCpuInfo);
				
			}
		}
		

		// ????????????????????????
		List<NodeMemoryInfo> nodeMemoryList = dto.getNodeMemoryInfo();
		for (int i = 0; i < memoryList.length; i++) {
			
			NodeMemoryInfo nodeMemoryInfo = new NodeMemoryInfo();
			
			if (memoryList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(memoryList[i].getDeviceDisplayName() != null
						&& !"".equals(memoryList[i].getDeviceDisplayName())){
					
					nodeMemoryInfo.setDeviceDisplayName(memoryList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceName() != null
					&& !"".equals(memoryList[i].getDeviceName())){
					
					nodeMemoryInfo.setDeviceName(memoryList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceIndex() >= 0){
					nodeMemoryInfo.setDeviceIndex(memoryList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceType() != null
						&& !"".equals(memoryList[i].getDeviceType())){
					nodeMemoryInfo.setDeviceType(memoryList[i].getDeviceType());
				}
				
				if(memoryList[i].getDeviceSize() >= 0 ){
					
					nodeMemoryInfo.setDeviceSize(memoryList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(memoryList[i].getDeviceSizeUnit() != null
						&& !"".equals(memoryList[i].getDeviceSizeUnit())){
					
					nodeMemoryInfo.setDeviceSizeUnit(memoryList[i].getDeviceSizeUnit());
					
				}
				
				if(memoryList[i].getDeviceDescription() != null
						&& !"".equals(memoryList[i].getDeviceDescription())){
					
					nodeMemoryInfo.setDeviceDescription(memoryList[i].getDeviceDescription());
					
				}
				
				nodeMemoryList.add(nodeMemoryInfo);
				
			}
		}
		

		// ??????????????????????????????????????????????????????
		List<NodeNetworkInterfaceInfo> nodeNetworkInterfaceList = dto.getNodeNetworkInterfaceInfo();
		for (int i = 0; i < networkInterfaceList.length; i++) {
			
			NodeNetworkInterfaceInfo nodeNetworkInterfaceInfo = new NodeNetworkInterfaceInfo();
			
			if (networkInterfaceList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(networkInterfaceList[i].getDeviceDisplayName() != null
						&& !"".equals(networkInterfaceList[i].getDeviceDisplayName())){
					
					nodeNetworkInterfaceInfo.setDeviceDisplayName(networkInterfaceList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceName() != null
					&& !"".equals(networkInterfaceList[i].getDeviceName())){
					
					nodeNetworkInterfaceInfo.setDeviceName(networkInterfaceList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceIndex() >= 0){
					nodeNetworkInterfaceInfo.setDeviceIndex(networkInterfaceList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceType() != null
						&& !"".equals(networkInterfaceList[i].getDeviceType())){
					nodeNetworkInterfaceInfo.setDeviceType(networkInterfaceList[i].getDeviceType());
				}
				
				if(networkInterfaceList[i].getDeviceSize() >= 0 ){
					
					nodeNetworkInterfaceInfo.setDeviceSize(networkInterfaceList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(networkInterfaceList[i].getDeviceSizeUnit() != null
						&& !"".equals(networkInterfaceList[i].getDeviceSizeUnit())){
					
					nodeNetworkInterfaceInfo.setDeviceSizeUnit(networkInterfaceList[i].getDeviceSizeUnit());
					
				}
				
				if(networkInterfaceList[i].getDeviceDescription() != null
						&& !"".equals(networkInterfaceList[i].getDeviceDescription())){
					
					nodeNetworkInterfaceInfo.setDeviceDescription(networkInterfaceList[i].getDeviceDescription());
					
				}
				
				if(networkInterfaceList[i].getDeviceNicIpAddress() != null
						&& !"".equals(networkInterfaceList[i].getDeviceNicIpAddress())){
					
					nodeNetworkInterfaceInfo.setNicIpAddress(networkInterfaceList[i].getDeviceNicIpAddress());
					
				}
				
				if(networkInterfaceList[i].getDeviceNicMacAddress() != null
						&& !"".equals(networkInterfaceList[i].getDeviceNicMacAddress())){
					
					nodeNetworkInterfaceInfo.setNicMacAddress(networkInterfaceList[i].getDeviceNicMacAddress());
					
				}
				
				nodeNetworkInterfaceList.add(nodeNetworkInterfaceInfo);
				
			}
		}
		
		// ???????????????????????????
		List<NodeDiskInfo> nodeDiskList = dto.getNodeDiskInfo();
		for (int i = 0; i < diskList.length; i++) {
			
			NodeDiskInfo nodeDiskInfo = new NodeDiskInfo();
			
			if (diskList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(diskList[i].getDeviceDisplayName() != null
						&& !"".equals(diskList[i].getDeviceDisplayName())){
					
					nodeDiskInfo.setDeviceDisplayName(diskList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceName() != null
					&& !"".equals(diskList[i].getDeviceName())){
					
					nodeDiskInfo.setDeviceName(diskList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceIndex() >= 0){
					nodeDiskInfo.setDeviceIndex(diskList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceType() != null
						&& !"".equals(diskList[i].getDeviceType())){
					nodeDiskInfo.setDeviceType(diskList[i].getDeviceType());
				}
				
				if(diskList[i].getDeviceSize() >= 0 ){
					
					nodeDiskInfo.setDeviceSize(diskList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(diskList[i].getDeviceSizeUnit() != null
						&& !"".equals(diskList[i].getDeviceSizeUnit())){
					
					nodeDiskInfo.setDeviceSizeUnit(diskList[i].getDeviceSizeUnit());
					
				}
				
				if(diskList[i].getDeviceDescription() != null
						&& !"".equals(diskList[i].getDeviceDescription())){
					
					nodeDiskInfo.setDeviceDescription(diskList[i].getDeviceDescription());
					
				}
				
				if(diskList[i].getDeviceDiskRpm() >= 0){
					
					nodeDiskInfo.setDiskRpm(diskList[i].getDeviceDiskRpm());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceDiskRpm) : " + xmlNodeInfo.getFacilityId());
				}

				nodeDiskList.add(nodeDiskInfo);
				
			}
		}
		

		// ???????????????????????????????????????
		List<NodeFilesystemInfo> nodeFilesystemList = dto.getNodeFilesystemInfo();
		for (int i = 0; i < fsList.length; i++) {
			
			NodeFilesystemInfo nodeFilesystemInfo = new NodeFilesystemInfo();
			
			if (fsList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				if(fsList[i].getDeviceIndex() >= 0){
					nodeFilesystemInfo.setDeviceIndex(fsList[i].getDeviceIndex());
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
					continue;
				}
				
				if(fsList[i].getDeviceType() != null
						&& !"".equals(fsList[i].getDeviceType())){
					nodeFilesystemInfo.setDeviceType(fsList[i].getDeviceType());
				}
				
				if(fsList[i].getDeviceName() != null
					&& !"".equals(fsList[i].getDeviceName())){
					
					nodeFilesystemInfo.setDeviceName(fsList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(fsList[i].getDeviceDisplayName() != null
						&& !"".equals(fsList[i].getDeviceDisplayName())){
					
					nodeFilesystemInfo.setDeviceDisplayName(fsList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(fsList[i].getDeviceSize() >= 0 ){
					
					nodeFilesystemInfo.setDeviceSize(fsList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(fsList[i].getDeviceSizeUnit() != null
						&& !"".equals(fsList[i].getDeviceSizeUnit())){
					
					nodeFilesystemInfo.setDeviceSizeUnit(fsList[i].getDeviceSizeUnit());
					
				}
				
				if(fsList[i].getDeviceDescription() != null
						&& !"".equals(fsList[i].getDeviceDescription())){
					
					nodeFilesystemInfo.setDeviceDescription(fsList[i].getDeviceDescription());
					
				}
				
				if(fsList[i].getDeviceFSType() != null
						&& !"".equals(fsList[i].getDeviceFSType())){
					nodeFilesystemInfo.setFilesystemType(fsList[i].getDeviceFSType());
				}
				
				nodeFilesystemList.add(nodeFilesystemInfo);
				
			}
		}
		
		// ?????????????????????????????????
		List<NodeGeneralDeviceInfo> nodeDeviceList = dto.getNodeDeviceInfo();
		for (int i = 0; i < deviceList.length; i++) {
			
			NodeGeneralDeviceInfo nodeDeviceInfo = new NodeGeneralDeviceInfo();
			
			if (deviceList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(deviceList[i].getDeviceDisplayName() != null
						&& !"".equals(deviceList[i].getDeviceDisplayName())){
					
					nodeDeviceInfo.setDeviceDisplayName(deviceList[i].getDeviceDisplayName());
					
				}else{
						log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
								+ "(DeviceDisplayName) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(deviceList[i].getDeviceName() != null
					&& !"".equals(deviceList[i].getDeviceName())){
					
					nodeDeviceInfo.setDeviceName(deviceList[i].getDeviceName());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceName) : " + xmlNodeInfo.getFacilityId());
				}
				

				if(deviceList[i].getDeviceIndex() >= 0 ){
					
					nodeDeviceInfo.setDeviceIndex(deviceList[i].getDeviceIndex());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceIndex) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(deviceList[i].getDeviceType() != null
						&& !"".equals(deviceList[i].getDeviceType())){
					
					nodeDeviceInfo.setDeviceType(deviceList[i].getDeviceType());
					
				}
				
				if(deviceList[i].getDeviceSize() >= 0 ){
					
					nodeDeviceInfo.setDeviceSize(deviceList[i].getDeviceSize());
					
				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(DeviceSize) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(deviceList[i].getDeviceSizeUnit() != null
						&& !"".equals(deviceList[i].getDeviceSizeUnit())){
					
					nodeDeviceInfo.setDeviceSizeUnit(deviceList[i].getDeviceSizeUnit());
					
				}
				
				if(deviceList[i].getDeviceDescription() != null
						&& !"".equals(deviceList[i].getDeviceDescription())){
					
					nodeDeviceInfo.setDeviceDescription(deviceList[i].getDeviceDescription());
					
				}
				
				nodeDeviceList.add(nodeDeviceInfo);
				
			}
		}
		
		// ??????????????????????????????
		List<com.clustercontrol.ws.repository.NodeVariableInfo> nodeVariableList = dto.getNodeVariableInfo();
		for (int i = 0; i < variableList.length; i++) {
			
			com.clustercontrol.ws.repository.NodeVariableInfo nodeVariableInfo = new com.clustercontrol.ws.repository.NodeVariableInfo();
			
			if (variableList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(variableList[i].getNodeVariableName() != null
						&& !"".equals(variableList[i].getNodeVariableName())){
					
					nodeVariableInfo.setNodeVariableName(variableList[i].getNodeVariableName());
				}
				
				if(variableList[i].getNodeVariableValue() != null
						&& !"".equals(variableList[i].getNodeVariableValue())){
					
					nodeVariableInfo.setNodeVariableValue(variableList[i].getNodeVariableValue());
				}
				
				nodeVariableList.add(nodeVariableInfo);
				
			}
		}
		
		// ?????????????????????
		List<NodeNoteInfo> nodeNoteList = dto.getNodeNoteInfo();
		for (int i = 0; i < noteList.length; i++) {
			
			NodeNoteInfo nodeNoteInfo = new NodeNoteInfo();
			
			if (noteList[i].getFacilityId().equals(xmlNodeInfo.getFacilityId())) {
				
				if(noteList[i].getNoteId() >= 0){
					
					nodeNoteInfo.setNoteId(noteList[i].getNoteId());

				}else{
					log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
							+ "(NoteId) : " + xmlNodeInfo.getFacilityId());
				}
				
				if(noteList[i].getNote() != null){
					
					nodeNoteInfo.setNote(noteList[i].getNote());
				}
				
				nodeNoteList.add(nodeNoteInfo);
				
			}
		}
		
		return dto;
		
	}

	/**
	 * XML????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????<br>
	 * 
	 * @param xmlScopeInfo
	 *            ????????????????????????????????????
	 * @return ????????????????????????????????????????????????
	 */
	/*
	public static com.clustercontrol.ws.repository.ScopeInfo convScopeXml2Dto(ScopeInfo xmlScopeInfo) {

		com.clustercontrol.ws.repository.ScopeInfo dto = new com.clustercontrol.ws.repository.ScopeInfo();
		
		if(xmlScopeInfo.getFacilityId() != null
				&& !"".equals(xmlScopeInfo.getFacilityId())){
			
			dto.setFacilityId(xmlScopeInfo.getFacilityId());
			
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(FileSystemMountPoint) : " + xmlScopeInfo.toString());
			
			return null;
		}
		
		if(xmlScopeInfo.getFacilityName() != null
				&& !"".equals(xmlScopeInfo.getFacilityName())){
			dto.setFacilityName(xmlScopeInfo.getFacilityName());
		}else{
			log.warn(Messages.getString("SettingTools.EssentialValueInvalid")
					+ "(FileSystemMountPoint) : " + xmlScopeInfo.toString());
			return null;
		}
		
		if(xmlScopeInfo.getDescription() != null
				&& !"".equals(xmlScopeInfo.getDescription())){
			dto.setDescription(xmlScopeInfo.getFacilityName());
		}
		
		dto.setOwnerRoleId(xmlScopeInfo.getOwnerRoleId());

		return dto;
	}
*/
	/**
	 * ?????????????????????Hinemos???????????????????????????????????????????????????????????????????????????<br>
	 * 
	 * @param scopeInfo
	 *            ????????????????????????????????????
	 * @return ????????????????????????????????????true, ???????????????false?????????
	 */
	public static boolean checkInternalScope(String facilityId) {

		if (facilityId.equals("INTERNAL") ||
				facilityId.equals("REGISTERED") ||
				facilityId.equals("UNREGISTERED") ||
				facilityId.equals("OS") ||
				facilityId.equals("OWNER") ||
				facilityId.equals("_PRIVATE_CLOUD") ||
				facilityId.equals("_PUBLIC_CLOUD") ||
				facilityId.equals("NODE_CONFIGURATION")) {
			return true;
		} else {
			return false;
		}

	}
	
	
	public static void convDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto , NodeInfo nodeInfo){

		nodeInfo.setFacilityId(dto.getFacilityId());
		nodeInfo.setFacilityName(dto.getFacilityName());
		
		nodeInfo.setDescription(dto.getDescription());
		nodeInfo.setOwnerRoleId(dto.getOwnerRoleId());
		nodeInfo.setPlatformFamily(dto.getPlatformFamily());
		nodeInfo.setSubPlatformFamily(dto.getSubPlatformFamily());
		nodeInfo.setValidFlg(dto.isValid());
		
		if (dto.getIconImage()!= null) {
			nodeInfo.setIconImage(dto.getIconImage());
		}
		
		//SNMP??????
		if (dto.getSnmpPort() != null && checkInteger(dto.getSnmpPort()) != null) {
			nodeInfo.setSnmpPort(checkInteger(dto.getSnmpPort()));
		}

		if (dto.getSnmpCommunity() != null) {
			nodeInfo.setSnmpCommunity(dto.getSnmpCommunity());
		}

		nodeInfo.setSnmpVersion(dto.getSnmpVersion());

		if (dto.getSnmpRetryCount() != null && checkInteger(dto.getSnmpRetryCount()) != null) {
			nodeInfo.setSnmpRetryCount(checkInteger(dto.getSnmpRetryCount()));
		}

		if (dto.getSnmpTimeout() != null && checkInteger(dto.getSnmpTimeout()) != null) {
			nodeInfo.setSnmpTimeout(checkInteger(dto.getSnmpTimeout()));
		}

		//WBEM??????
		if (dto.getWbemUser() != null) {
			nodeInfo.setWbemUser(dto.getWbemUser());
		}
		
		if (dto.getWbemUserPassword() != null) {
			nodeInfo.setWbemUserPassword(dto.getWbemUserPassword());
		}

		if (dto.getWbemPort() != null && checkInteger(dto.getWbemPort()) != null) {
			nodeInfo.setWbemPort(checkInteger(dto.getWbemPort()));
		}
		if (dto.getWbemProtocol() != null) {
			nodeInfo.setWbemProtocol(dto.getWbemProtocol());
		}
		if (dto.getWbemTimeout() != null && checkInteger(dto.getWbemTimeout()) != null) {
			nodeInfo.setWbemTimeout(checkInteger(dto.getWbemTimeout()));
		}
		if (dto.getWbemRetryCount() != null && checkInteger(dto.getWbemRetryCount()) != null) {
			nodeInfo.setWbemRetryCount(checkInteger(dto.getWbemRetryCount()));
		}
		
		nodeInfo.setIpAddressVersion(dto.getIpAddressVersion());
		nodeInfo.setIpAddressV4(dto.getIpAddressV4());
		nodeInfo.setIpAddressV6(dto.getIpAddressV6());
		nodeInfo.setNodeName(dto.getNodeName());
		nodeInfo.setHardwareType(dto.getHardwareType());
		nodeInfo.setAdministrator(dto.getAdministrator());
		nodeInfo.setContact(dto.getContact());

		String osName = null;
		String osRelease = null;
		String osVersion = null;
		String characterSet = null;
		Long startupDateTime = 0L;
		if (dto.getNodeOsInfo() != null) {
			osName = dto.getNodeOsInfo().getOsName();
			osRelease = dto.getNodeOsInfo().getOsRelease();
			osVersion = dto.getNodeOsInfo().getOsVersion();
			characterSet = dto.getNodeOsInfo().getCharacterSet();
			startupDateTime = dto.getNodeOsInfo().getStartupDateTime();
		}
		nodeInfo.setOsName(osName);
		nodeInfo.setOsRelease(osRelease);
		nodeInfo.setOsVersion(osVersion);
		nodeInfo.setCharacterSet(characterSet);
		if (startupDateTime > 0) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			nodeInfo.setStartupDateTime(dateFormat.format(startupDateTime));
		}
		
		//Hinemos???????????????????????????????????????
		if (dto.getAgentAwakePort() != null && checkInteger(dto.getAgentAwakePort()) != null) {
			nodeInfo.setAgentAwakePort(checkInteger(dto.getAgentAwakePort()));
		}
		
		//???????????????
		if (dto.getJobMultiplicity() != null && checkInteger(dto.getJobMultiplicity()) != null) {
			nodeInfo.setJobMultiplicity(checkInteger(dto.getJobMultiplicity()));
		}
		if (dto.getJobPriority() != null && checkInteger(dto.getJobPriority()) != null) {
			nodeInfo.setJobPriority(checkInteger(dto.getJobPriority()));
		}

		// IPMI??????
		if (dto.getIpmiIpAddress() != null) {
			nodeInfo.setIpmiIpAddress(dto.getIpmiIpAddress());
		}
		Integer ipmiPort = dto.getIpmiPort();
		if (checkInteger(ipmiPort) != null) {
			nodeInfo.setIpmiPort(ipmiPort.intValue());
		}

		if (dto.getIpmiUser() != null) {
			nodeInfo.setIpmiUser(dto.getIpmiUser());
		}
		
		if (dto.getIpmiUserPassword() != null) {
			nodeInfo.setIpmiUserPassword(dto.getIpmiUserPassword());
		}
		Integer ipmiTimeout = dto.getIpmiTimeout();
		if (checkInteger(ipmiTimeout) != null) {
			nodeInfo.setIpmiTimeout(ipmiTimeout.intValue());
		}
		Integer ipmiRetryCount = dto.getIpmiRetries();
		if (checkInteger(ipmiRetryCount) != null) {
			nodeInfo.setIpmiRetryCount(ipmiRetryCount.intValue());
		}

		if (dto.getIpmiProtocol() != null) {
			nodeInfo.setIpmiProtocol(dto.getIpmiProtocol());
		}

		if (dto.getIpmiLevel() != null) {
			nodeInfo.setIpmiLevel(dto.getIpmiLevel());
		}

		// WinRM??????
		if (dto.getWinrmUser() != null) {
			nodeInfo.setWinrmUser(dto.getWinrmUser());
		}
		
		if (dto.getWinrmUserPassword() != null) {
			nodeInfo.setWinrmUserPassword(dto.getWinrmUserPassword());
		}

		if (dto.getWinrmVersion() != null) {
			nodeInfo.setWinrmVersion(dto.getWinrmVersion());
		}

		Integer winrmPort = dto.getWinrmPort();
		if (checkInteger(winrmPort) != null) {
			nodeInfo.setWinrmPort(winrmPort.intValue());
		}

		if (dto.getWinrmProtocol() != null) {
			nodeInfo.setWinrmProtocol(dto.getWinrmProtocol());
		}
		Integer winrmTimeout = dto.getWinrmTimeout();
		if (checkInteger(winrmTimeout) != null) {
			nodeInfo.setWinrmTimeout(winrmTimeout.intValue());
		}

		Integer winrmRetryCount = dto.getWinrmRetries();
		if (checkInteger(winrmRetryCount) != null) {
			nodeInfo.setWinrmRetryCount(winrmRetryCount.intValue());
		}

		nodeInfo.setAutoDeviceSearch(dto.isAutoDeviceSearch());
		
		/*SNMP??????????????????????????????*/
		if(dto.getSnmpAuthPassword() != null){
			nodeInfo.setSnmpAuthPassword(dto.getSnmpAuthPassword());
		}

		if(dto.getSnmpAuthProtocol() != null){
			nodeInfo.setSnmpAuthProtocol(dto.getSnmpAuthProtocol());
		}
		
		if(dto.getSnmpCommunity() != null){
			nodeInfo.setSnmpAuthProtocol(dto.getSnmpAuthProtocol());
		}
		
		nodeInfo.setSnmpPort(dto.getSnmpPort());
		nodeInfo.setSnmpRetryCount(dto.getSnmpRetryCount());
		nodeInfo.setSnmpTimeout(dto.getSnmpTimeout());
		
		if(dto.getSnmpPrivPassword() != null){
			nodeInfo.setSnmpPrivPassword(dto.getSnmpPrivPassword());
		}
		
		if(dto.getSnmpPrivProtocol() != null){
			nodeInfo.setSnmpPrivProtocol(dto.getSnmpPrivProtocol());
		}
		
		if(dto.getSnmpSecurityLevel() != null){
			nodeInfo.setSnmpSecurityLevel(dto.getSnmpSecurityLevel());
		}

		if(dto.getSnmpUser() != null){
			nodeInfo.setSnmpUser(dto.getSnmpUser());
		}
		
		if(dto.getSnmpVersion() != null){
			nodeInfo.setSnmpVersion(dto.getSnmpVersion());
		}
		

		/*SSH??????????????????????????????*/
		if(dto.getSshUserPassword() != null){
			nodeInfo.setSshUserPassword(dto.getSshUserPassword());
		}

		if(dto.getSshPrivateKeyFilepath() != null){
			nodeInfo.setSshPrivateKeyFilename(dto.getSshPrivateKeyFilepath());
		}
		
		if(dto.getSshPrivateKeyPassphrase() != null){
			nodeInfo.setSshPrivateKeyPassphrase(dto.getSshPrivateKeyPassphrase());
		}
		
		nodeInfo.setSshPort(dto.getSshPort());
		nodeInfo.setSshTimeout(dto.getSshTimeout());

		if(dto.getSshUser() != null){
			nodeInfo.setSshUser(dto.getSshUser());
		}

		/*WBEM??????????????????????????????*/
		if(dto.getWbemUserPassword() != null){
			nodeInfo.setWbemUserPassword(dto.getWbemUserPassword());
		}

		if(dto.getWbemProtocol() != null){
			nodeInfo.setWbemProtocol(dto.getWbemProtocol());
		}
		
		nodeInfo.setWbemPort(dto.getWbemPort());
		nodeInfo.setWbemTimeout(dto.getWbemTimeout());
		nodeInfo.setWbemRetryCount(dto.getWbemRetryCount());

		if(dto.getWbemUser() != null){
			nodeInfo.setWbemUser(dto.getWbemUser());
		}
		
		/*??????????????????????????????????????????*/
		if(dto.getCloudService() != null){
			nodeInfo.setCloudService(dto.getCloudService());
		}
		
		if(dto.getCloudScope() != null){
			nodeInfo.setCloudScope(dto.getCloudScope());
		}
		
		if(dto.getCloudResourceType() != null){
			nodeInfo.setCloudResourceType(dto.getCloudResourceType());
		}
		
		if(dto.getCloudResourceId() != null){
			nodeInfo.setCloudResourceId(dto.getCloudResourceId());
		}
		
		if(dto.getCloudLocation() != null){
			nodeInfo.setCloudLocation(dto.getCloudLocation());
		}
		
		if(dto.getCloudResourceName() != null){
			nodeInfo.setCloudResourceName(dto.getCloudResourceName());
		}
	}

	public static Collection<HostnameInfo> convHostnameDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<HostnameInfo> hostnameList = new ArrayList<HostnameInfo>();
		
		HostnameInfo xmlHostnameInfo = null;
		
		// ???????????????????????????????????????
		List<NodeHostnameInfo> nodeHostnameInfoList = dto.getNodeHostnameInfo();
		Iterator<NodeHostnameInfo> itrNodeHostnameInfoList = nodeHostnameInfoList.iterator();
		while (itrNodeHostnameInfoList.hasNext()) {

			NodeHostnameInfo nodeHostnameInfo = itrNodeHostnameInfoList.next();
			
			if (nodeHostnameInfo.getHostname() != null && !nodeHostnameInfo.getHostname().equals("")) {

				xmlHostnameInfo = new HostnameInfo();

				xmlHostnameInfo.setFacilityId(dto.getFacilityId());

				xmlHostnameInfo.setHostname(nodeHostnameInfo.getHostname());

				hostnameList.add(xmlHostnameInfo);
			}

		}

		return hostnameList;
	}

	public static Collection<CPUInfo> convCPUDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<CPUInfo> cpuList = new ArrayList<CPUInfo>();
		
		CPUInfo xmlCPUInfo = null;
		
		// ????????????CPU???????????????
		List<NodeCpuInfo> nodeCpuInfoList = dto.getNodeCpuInfo();
		Iterator<NodeCpuInfo> itrNodeCpuInfoList = nodeCpuInfoList.iterator();
		while (itrNodeCpuInfoList.hasNext()) {

			NodeCpuInfo nodeCpuInfo = itrNodeCpuInfoList.next();
			
			if (nodeCpuInfo.getDeviceName() != null && !nodeCpuInfo.getDeviceName().equals("")) {
				xmlCPUInfo = new CPUInfo();
				xmlCPUInfo.setFacilityId(dto.getFacilityId());
				xmlCPUInfo.setDeviceDisplayName(nodeCpuInfo.getDeviceDisplayName());
				xmlCPUInfo.setDeviceName(nodeCpuInfo.getDeviceName());
				Integer deviceIndex = nodeCpuInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlCPUInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlCPUInfo.setDeviceType(nodeCpuInfo.getDeviceType());
				Integer deviceSize = nodeCpuInfo.getDeviceSize();
				if (checkInteger(deviceSize) != null) {
					xmlCPUInfo.setDeviceSize(deviceSize.intValue());
				}
				xmlCPUInfo.setDeviceSizeUnit(nodeCpuInfo.getDeviceSizeUnit());
				xmlCPUInfo.setDeviceDescription(nodeCpuInfo.getDeviceDescription());
				xmlCPUInfo.setCoreCount(nodeCpuInfo.getCoreCount());
				xmlCPUInfo.setThreadCount(nodeCpuInfo.getThreadCount());
				xmlCPUInfo.setClockCount(nodeCpuInfo.getClockCount());
				cpuList.add(xmlCPUInfo);
			}

		}

		return cpuList;
	}

	public static Collection<MemoryInfo> convMemoryDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<MemoryInfo> memoryList = new ArrayList<MemoryInfo>();
		
		MemoryInfo xmlMemoryInfo = null;
		
		// ????????????????????????????????????
		List<NodeMemoryInfo> nodeMemoryInfoList = dto.getNodeMemoryInfo();
		Iterator<NodeMemoryInfo> itrNodeMemoryInfoList = nodeMemoryInfoList.iterator();
		while (itrNodeMemoryInfoList.hasNext()) {

			NodeMemoryInfo nodeMemoryInfo = itrNodeMemoryInfoList.next();
			
			if (nodeMemoryInfo.getDeviceName() != null && !nodeMemoryInfo.getDeviceName().equals("")) {
				xmlMemoryInfo = new MemoryInfo();
				xmlMemoryInfo.setFacilityId(dto.getFacilityId());
				xmlMemoryInfo.setDeviceDisplayName(nodeMemoryInfo.getDeviceDisplayName());
				xmlMemoryInfo.setDeviceName(nodeMemoryInfo.getDeviceName());

				Integer deviceIndex = nodeMemoryInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlMemoryInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlMemoryInfo.setDeviceType(nodeMemoryInfo.getDeviceType());
				Integer deviceSize = nodeMemoryInfo.getDeviceSize();
				if (checkInteger(deviceSize) != null) {
					xmlMemoryInfo.setDeviceSize(deviceSize.intValue());
				}
				xmlMemoryInfo.setDeviceSizeUnit(nodeMemoryInfo.getDeviceSizeUnit());
				xmlMemoryInfo.setDeviceDescription(nodeMemoryInfo.getDeviceDescription());
				memoryList.add(xmlMemoryInfo);
			}

		}

		return memoryList;
	}

	public static Collection<NetworkInterfaceInfo> convNetworkInterfaceDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<NetworkInterfaceInfo> networkInterfaceList = new ArrayList<NetworkInterfaceInfo>();
		
		NetworkInterfaceInfo xmlNetworkInterfaceInfo = null;
		
		// ??????????????????????????????????????????????????????????????????
		List<NodeNetworkInterfaceInfo> nodeNetworkInterfaceInfoList = dto.getNodeNetworkInterfaceInfo();
		Iterator<NodeNetworkInterfaceInfo> itrNodeNetworkInterfaceInfoList = nodeNetworkInterfaceInfoList.iterator();
		while (itrNodeNetworkInterfaceInfoList.hasNext()) {

			NodeNetworkInterfaceInfo nodeNetworkInterfaceInfo = itrNodeNetworkInterfaceInfoList.next();
			
			if (nodeNetworkInterfaceInfo.getDeviceName() != null && !nodeNetworkInterfaceInfo.getDeviceName().equals("")) {
				xmlNetworkInterfaceInfo = new NetworkInterfaceInfo();
				xmlNetworkInterfaceInfo.setFacilityId(dto.getFacilityId());
				xmlNetworkInterfaceInfo.setDeviceDisplayName(nodeNetworkInterfaceInfo.getDeviceDisplayName());
				xmlNetworkInterfaceInfo.setDeviceName(nodeNetworkInterfaceInfo.getDeviceName());
				Integer deviceIndex = nodeNetworkInterfaceInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlNetworkInterfaceInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlNetworkInterfaceInfo.setDeviceType(nodeNetworkInterfaceInfo.getDeviceType());
				Integer deviceSize = nodeNetworkInterfaceInfo.getDeviceSize();
				if (checkInteger(deviceSize) != null) {
					xmlNetworkInterfaceInfo.setDeviceSize(deviceSize.intValue());
				}
				xmlNetworkInterfaceInfo.setDeviceSizeUnit(nodeNetworkInterfaceInfo.getDeviceSizeUnit());
				xmlNetworkInterfaceInfo.setDeviceDescription(nodeNetworkInterfaceInfo.getDeviceDescription());
				xmlNetworkInterfaceInfo.setDeviceNicIpAddress(nodeNetworkInterfaceInfo.getNicIpAddress());
				xmlNetworkInterfaceInfo.setDeviceNicMacAddress(nodeNetworkInterfaceInfo.getNicMacAddress());
				networkInterfaceList.add(xmlNetworkInterfaceInfo);
			}

		}

		return networkInterfaceList;
	}

	public static Collection<DiskInfo> convDiskDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<DiskInfo> diskList = new ArrayList<DiskInfo>();
		
		DiskInfo xmlDiskInfo = null;
		
		// ???????????????????????????????????????
		List<NodeDiskInfo> nodeDiskInfoList = dto.getNodeDiskInfo();
		Iterator<NodeDiskInfo> itrNodeDiskInfoList = nodeDiskInfoList.iterator();
		while (itrNodeDiskInfoList.hasNext()) {

			NodeDiskInfo nodeDiskInfo = itrNodeDiskInfoList.next();
			
			if (nodeDiskInfo.getDeviceName() != null && !nodeDiskInfo.getDeviceName().equals("")) {
				xmlDiskInfo = new DiskInfo();
				xmlDiskInfo.setFacilityId(dto.getFacilityId());
				xmlDiskInfo.setDeviceDisplayName(nodeDiskInfo.getDeviceDisplayName());
				xmlDiskInfo.setDeviceName(nodeDiskInfo.getDeviceName());
				xmlDiskInfo.setDeviceIndex(nodeDiskInfo.getDeviceIndex());
				xmlDiskInfo.setDeviceType(nodeDiskInfo.getDeviceType());
				Integer deviceSize = nodeDiskInfo.getDeviceSize();
				if (checkInteger(deviceSize) != null) {
					xmlDiskInfo.setDeviceSize(deviceSize.intValue());
				}
				xmlDiskInfo.setDeviceSizeUnit(nodeDiskInfo.getDeviceSizeUnit());
				xmlDiskInfo.setDeviceDescription(nodeDiskInfo.getDeviceDescription());
				Integer deviceDiskRpm = nodeDiskInfo.getDiskRpm();
				if (checkInteger(deviceDiskRpm) != null) {
					xmlDiskInfo.setDeviceDiskRpm(deviceDiskRpm.intValue());
				}

				diskList.add(xmlDiskInfo);
			}

		}

		return diskList;
	}

	public static Collection<FSInfo> convFSDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<FSInfo> fsList = new ArrayList<FSInfo>();
		
		FSInfo xmlFilesystemInfo = null;
		
		// ???????????????????????????????????????????????????
		List<NodeFilesystemInfo> nodeFilesystemInfoList = dto.getNodeFilesystemInfo();
		Iterator<NodeFilesystemInfo> itrNodeFilesystemInfoList = nodeFilesystemInfoList.iterator();
		while (itrNodeFilesystemInfoList.hasNext()) {
			NodeFilesystemInfo nodeFilesystemInfo = itrNodeFilesystemInfoList.next();
			
			if (nodeFilesystemInfo.getDeviceName() != null && !nodeFilesystemInfo.getDeviceName().equals("")) {
				xmlFilesystemInfo = new FSInfo();
				xmlFilesystemInfo.setFacilityId(dto.getFacilityId());
				Integer deviceIndex = nodeFilesystemInfo.getDeviceIndex();
				if (checkInteger(deviceIndex) != null) {
					xmlFilesystemInfo.setDeviceIndex(deviceIndex.intValue());
				}
				xmlFilesystemInfo.setDeviceType(nodeFilesystemInfo.getDeviceType());
				xmlFilesystemInfo.setDeviceName(nodeFilesystemInfo.getDeviceName());
				xmlFilesystemInfo.setDeviceDisplayName(nodeFilesystemInfo.getDeviceDisplayName());

				Integer deviceSize = nodeFilesystemInfo.getDeviceSize();
				if (checkInteger(deviceSize) != null) {
					xmlFilesystemInfo.setDeviceSize(deviceSize.intValue());
				}
				xmlFilesystemInfo.setDeviceSizeUnit(nodeFilesystemInfo.getDeviceSizeUnit());
				xmlFilesystemInfo.setDeviceDescription(nodeFilesystemInfo.getDeviceDescription());
				xmlFilesystemInfo.setDeviceFSType(nodeFilesystemInfo.getFilesystemType());
				fsList.add(xmlFilesystemInfo);
			}

		}

		return fsList;
	}
	

	public static Collection<DeviceInfo> convDeviceDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto ){
		
		ArrayList<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
		
		DeviceInfo xmlDeviceInfo = null;
		
		// ?????????????????????????????????????????????
		List<NodeGeneralDeviceInfo> nodeDeviceInfoList = dto.getNodeDeviceInfo();
		Iterator<NodeGeneralDeviceInfo> itrNodeDeviceInfoList = nodeDeviceInfoList.iterator();
		while (itrNodeDeviceInfoList.hasNext()) {
			
			NodeDeviceInfo nodeDeviceInfo = itrNodeDeviceInfoList.next();
			
			if (nodeDeviceInfo.getDeviceName() != null && !nodeDeviceInfo.getDeviceName().equals("")) {
				xmlDeviceInfo = new DeviceInfo();
				xmlDeviceInfo.setFacilityId(dto.getFacilityId());
				xmlDeviceInfo.setDeviceDisplayName(nodeDeviceInfo.getDeviceDisplayName());
				xmlDeviceInfo.setDeviceName(nodeDeviceInfo.getDeviceName());
				xmlDeviceInfo.setDeviceDescription(nodeDeviceInfo.getDeviceDescription());
				xmlDeviceInfo.setDeviceIndex(nodeDeviceInfo.getDeviceIndex());

				xmlDeviceInfo.setDeviceType(nodeDeviceInfo.getDeviceType());
				Integer deviceSize = nodeDeviceInfo.getDeviceSize();
				if (checkInteger(deviceSize) != null) {
					xmlDeviceInfo.setDeviceSize(deviceSize.intValue());
				}
				xmlDeviceInfo.setDeviceSizeUnit(nodeDeviceInfo.getDeviceSizeUnit());
				xmlDeviceInfo.setDeviceType(nodeDeviceInfo.getDeviceType());
				deviceList.add(xmlDeviceInfo);
			}
		}

		return deviceList;
	}
	
	public static Collection<NodeVariableInfo> convVariableDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<NodeVariableInfo> variableList = new ArrayList<NodeVariableInfo>();
		
		NodeVariableInfo xmlVariableInfo = null;
		
		// ??????????????????????????????????????????
		List<com.clustercontrol.ws.repository.NodeVariableInfo> nodeVariableInfoList = dto.getNodeVariableInfo();
		Iterator<com.clustercontrol.ws.repository.NodeVariableInfo> itrNodeVariableInfoList = nodeVariableInfoList.iterator();
		while (itrNodeVariableInfoList.hasNext()) {

			com.clustercontrol.ws.repository.NodeVariableInfo nodeVariableInfo = itrNodeVariableInfoList.next();
			
			if (nodeVariableInfo.getNodeVariableName() != null && !nodeVariableInfo.getNodeVariableName().equals("")) {
				xmlVariableInfo = new NodeVariableInfo();
				xmlVariableInfo.setFacilityId(dto.getFacilityId());
				xmlVariableInfo.setNodeVariableName(nodeVariableInfo.getNodeVariableName());
				xmlVariableInfo.setNodeVariableValue(nodeVariableInfo.getNodeVariableValue());
				variableList.add(xmlVariableInfo);
			}

		}

		return variableList;
	}

	public static Collection<NoteInfo> convNoteDto2Xml(com.clustercontrol.ws.repository.NodeInfo dto){
		
		ArrayList<NoteInfo> noteList = new ArrayList<NoteInfo>();
		
		NoteInfo xmlNoteInfo = null;
		
		// ?????????????????????????????????
		List<NodeNoteInfo> nodeNoteInfoList = dto.getNodeNoteInfo();
		Iterator<NodeNoteInfo> itrNodeNoteInfoList = nodeNoteInfoList.iterator();
		while (itrNodeNoteInfoList.hasNext()) {

			NodeNoteInfo nodeNoteInfo = itrNodeNoteInfoList.next();

			xmlNoteInfo = new NoteInfo();
			xmlNoteInfo.setFacilityId(dto.getFacilityId());
			xmlNoteInfo.setNoteId(nodeNoteInfo.getNoteId());
			xmlNoteInfo.setNote(nodeNoteInfo.getNote());
			noteList.add(xmlNoteInfo);
		}

		return noteList;
	}

	public static List<NetstatInfo> convNetstatDto2Xml(
			com.clustercontrol.ws.repository.NodeInfo dto) {
		List<NetstatInfo> netstatList = new ArrayList<NetstatInfo>();
		
		// ???????????????????????????????????????????????????
		for (NodeNetstatInfo nodeNetstatInfo : dto.getNodeNetstatInfo()) {
			NetstatInfo xmlNetstatInfo = new NetstatInfo();
			xmlNetstatInfo.setFacilityId(dto.getFacilityId());
			xmlNetstatInfo.setProtocol(nodeNetstatInfo.getProtocol());
			xmlNetstatInfo.setLocalIpAddress(nodeNetstatInfo.getLocalIpAddress());
			xmlNetstatInfo.setLocalPort(nodeNetstatInfo.getLocalPort());
			xmlNetstatInfo.setForeignIpAddress(nodeNetstatInfo.getForeignIpAddress());
			xmlNetstatInfo.setForeignPort(nodeNetstatInfo.getForeignPort());
			xmlNetstatInfo.setProcessName(nodeNetstatInfo.getProcessName());
			if (nodeNetstatInfo.getPid() != null) {
				xmlNetstatInfo.setPid(nodeNetstatInfo.getPid());
			}
			xmlNetstatInfo.setStatus(nodeNetstatInfo.getStatus());
			netstatList.add(xmlNetstatInfo);
		}

		return netstatList;
	}

	public static List<PackageInfo> convPackageDto2Xml(
			com.clustercontrol.ws.repository.NodeInfo dto) {
		List<PackageInfo> packageList = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// ??????????????????????????????????????????
		for (NodePackageInfo nodePackageInfo : dto.getNodePackageInfo()) {
			PackageInfo xmlPakageInfo = new PackageInfo();
			xmlPakageInfo.setFacilityId(dto.getFacilityId());
			xmlPakageInfo.setPackageId(nodePackageInfo.getPackageId());
			xmlPakageInfo.setPackageName(nodePackageInfo.getPackageName());
			xmlPakageInfo.setVersion(nodePackageInfo.getVersion());
			xmlPakageInfo.setRelease(nodePackageInfo.getRelease());
			xmlPakageInfo.setInstallDate(dateFormat.format(nodePackageInfo.getInstallDate()));
			xmlPakageInfo.setVendor(nodePackageInfo.getVendor());
			xmlPakageInfo.setArchitecture(nodePackageInfo.getArchitecture());
			packageList.add(xmlPakageInfo);
		}
		return packageList;
	}

	public static List<ProductInfo> convProductDto2Xml(
			com.clustercontrol.ws.repository.NodeInfo dto) {
		List<ProductInfo> productList = new ArrayList<>();
		// ?????????????????????????????????????????????
		for (NodeProductInfo nodeProductInfo : dto.getNodeProductInfo()) {
			ProductInfo xmlProductInfo = new ProductInfo();
			xmlProductInfo.setFacilityId(dto.getFacilityId());
			xmlProductInfo.setProductName(nodeProductInfo.getProductName());
			xmlProductInfo.setVersion(nodeProductInfo.getVersion());
			xmlProductInfo.setPath(nodeProductInfo.getPath());
			productList.add(xmlProductInfo);
		}
		return productList;
	}

	public static List<LicenseInfo> convLicenseDto2Xml(
			com.clustercontrol.ws.repository.NodeInfo dto) {
		List<LicenseInfo> lisenceList = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// ??????????????????????????????????????????
		for (NodeLicenseInfo nodeProductInfo : dto.getNodeLicenseInfo()) {
			LicenseInfo xmlProductInfo = new LicenseInfo();
			xmlProductInfo.setFacilityId(dto.getFacilityId());
			xmlProductInfo.setProductName(nodeProductInfo.getProductName());
			xmlProductInfo.setVendor(nodeProductInfo.getVendor());
			xmlProductInfo.setVendorContact(nodeProductInfo.getVendorContact());
			xmlProductInfo.setSerialNumber(nodeProductInfo.getSerialNumber());
			xmlProductInfo.setCount(nodeProductInfo.getCount());
			xmlProductInfo.setExpirationDate(dateFormat.format(nodeProductInfo.getExpirationDate()));
			lisenceList.add(xmlProductInfo);
		}
		return lisenceList;
	}

	public static List<ProcessInfo> convProcessDto2Xml(
			com.clustercontrol.ws.repository.NodeInfo dto) {
		List<ProcessInfo> processList = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// ???????????????????????????????????????
		for (NodeProcessInfo nodeProcessInfo : dto.getNodeProcessInfo()) {
			ProcessInfo xmlProcessInfo = new ProcessInfo();
			xmlProcessInfo.setFacilityId(dto.getFacilityId());
			xmlProcessInfo.setProcessName(nodeProcessInfo.getProcessName());
			xmlProcessInfo.setPid(nodeProcessInfo.getPid());
			xmlProcessInfo.setExecUser(nodeProcessInfo.getExecUser());
			xmlProcessInfo.setPath(nodeProcessInfo.getPath());
			xmlProcessInfo.setStartupDateTime(dateFormat.format(nodeProcessInfo.getStartupDateTime()));
			processList.add(xmlProcessInfo);
		}
		return processList;
	}

	/**
	 * Integer????????????????????????????????????????????????????????????null?????????
	 * 
	 * @param value
	 * @return
	 */
	protected static Integer checkInteger(Object value) {
		if (value != null && value instanceof Integer) {
			return (Integer) value;
		} else {
			return null;
		}
	}
}
