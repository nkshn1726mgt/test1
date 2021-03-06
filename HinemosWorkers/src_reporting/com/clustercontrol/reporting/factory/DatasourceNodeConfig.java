package com.clustercontrol.reporting.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.bean.ReportingConstant;
import com.clustercontrol.reporting.fault.ReportingPropertyNotFound;
import com.clustercontrol.reporting.session.ReportingNodeConfigControllerBean;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeDeviceHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.util.Messages;

import net.sf.jasperreports.engine.data.JRCsvDataSource;

public class DatasourceNodeConfig extends DatasourceBase {

	private static Log m_log = LogFactory.getLog(DatasourceNodeConfig.class);
	private List<String[]> m_targetNodes;
	private int m_maxNodeHisorySize;
	private String m_addCategory;
	private String m_updateCategory;
	private String m_deleteCategory;

	private class NodeConfigHistoryCSV {
		List<NodeConfigHistoryCsvRow> nodeConfigCSVRows = new ArrayList<>();
		String[] columns = { "node", "update_date", "change_category", "name", "details" };
		String columnsStr;
		BufferedWriter bw;

		public NodeConfigHistoryCSV(BufferedWriter bw) {
			columnsStr = ReportUtil.joinStrings(columns, ",");
			this.bw = bw;
		}

		void add(NodeConfigHistoryCsvRow row) {
			nodeConfigCSVRows.add(row);
		}

		void writeHeader() throws IOException {
			bw.write(columnsStr);
			bw.newLine();
		}

		void writeRows() throws IOException {
			Collections.sort(nodeConfigCSVRows, Collections.reverseOrder());
			if (nodeConfigCSVRows.size() > m_maxNodeHisorySize) {
				nodeConfigCSVRows = nodeConfigCSVRows.subList(0, m_maxNodeHisorySize);
			}
			// CSV???????????????
			for (NodeConfigHistoryCsvRow row : nodeConfigCSVRows) {
				bw.write(row.getCSVLine());
				bw.newLine();
			}
			nodeConfigCSVRows.clear();
		}
	}

	private class NodeConfigHistoryCsvRow implements Comparable<NodeConfigHistoryCsvRow> {
		String facilityId;
		String facilityName;
		Timestamp updateDateTime;
		String changeCategory;
		String name;
		String details;

		NodeConfigHistoryCsvRow(String facilityId, String facilityName, Long updateDate, String changeCategory,
				String name, String details) {
			this.facilityId = facilityId;
			this.facilityName = facilityName;
			if (updateDate != null) {
				updateDateTime = new Timestamp(updateDate);
				updateDateTime.setNanos(0);
			}
			this.changeCategory = changeCategory;
			this.name = name;
			this.details = details;
		}

		public Timestamp getUpdateDateTime() {
			return updateDateTime;
		}

		String getCSVLine() {
			// NodeConfigHistoryCSV???columns????????????????????????
			return ((facilityName == null ? "" : facilityName) + "(" + (facilityId == null ? "" : facilityId) + ")"
					+ "," + (updateDateTime == null ? "" : updateDateTime) + ","
					+ (changeCategory == null ? "" : changeCategory) + "," + (name == null ? "" : name) + ","
					+ (details == null ? "" : details));
		}

		@Override
		public int compareTo(NodeConfigHistoryCsvRow o) {
			return this.getUpdateDateTime().compareTo(o.getUpdateDateTime());
		}
	}

	private boolean isOsDeleted(List<NodeOsHistoryDetail> newNodeOsHistoryList, long regDateTo) {
		if (regDateTo > m_endDate.getTime()) {
			return false;
		}
		for (NodeOsHistoryDetail osDetails : newNodeOsHistoryList) {
			if (osDetails.getRegDate() == regDateTo) {
				return false;
			}
		}
		return true;
	}

	private <T extends NodeDeviceHistoryDetail> boolean isDeviceDeleted(List<T> newNodeDeviceHistoryList,
			String deviceName, long regDateTo) {
		if (regDateTo > m_endDate.getTime()) {
			return false;
		}
		for (T t : newNodeDeviceHistoryList) {
			if (t.getDeviceName().equals(deviceName) && t.getRegDate() == regDateTo) {
				return false;
			}
		}
		return true;
	}

	private boolean isPackageDeleted(List<NodePackageHistoryDetail> newNodePackageHistoryList, String packageId,
			long regDateTo) {
		if (regDateTo > m_endDate.getTime()) {
			return false;
		}
		for (NodePackageHistoryDetail packageDetails : newNodePackageHistoryList) {
			if (packageDetails.getPackageId().equals(packageId) && packageDetails.getRegDate() == regDateTo) {
				return false;
			}
		}
		return true;
	}

	private boolean isCustomDataDeleted(List<NodeCustomHistoryDetail> newNodeCustomHistoryList, String settingId,
			String settingCustomId, long regDateTo) {
		if (regDateTo > m_endDate.getTime()) {
			return false;
		}
		for (NodeCustomHistoryDetail customDetails : newNodeCustomHistoryList) {
			if (customDetails.getSettingId().equals(settingId)
					&& customDetails.getSettingCustomId().equals(settingCustomId)
					&& customDetails.getRegDate() == regDateTo) {
				return false;
			}
		}
		return true;
	}

	private boolean isRegDateValid(long regDate) {
		if (regDate >= m_startDate.getTime() && regDate <= m_endDate.getTime()) {
			return true;
		}
		return false;
	}

	private void writeOsHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodeOsHistoryDetail> nodeOsHistoryList) throws IOException {
		for (int index = 0; index < nodeOsHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodeOsHistoryDetail nodeOsHistoryDetail = nodeOsHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isOsDeleted(nodeOsHistoryList.subList(0, index), nodeOsHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodeOsHistoryDetail.getRegDate())) {
				if (controller.hasOldOSNodeConfigHistory(facilityId, nodeOsHistoryDetail.getRegDate())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("OS_RELEASE") + "=" + nodeOsHistoryDetail.getOsRelease());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("OS_VERSION") + "=" + nodeOsHistoryDetail.getOsVersion());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("CHARACTER_SET") + "=" + nodeOsHistoryDetail.getCharacterSet());
			historyDtlStr.append("<br/>");
			Timestamp startupDateTime = null;
			if (nodeOsHistoryDetail.getStartupDateTime() != null) {
				startupDateTime = new Timestamp(nodeOsHistoryDetail.getStartupDateTime());
				startupDateTime.setNanos(0);
			}
			historyDtlStr.append(Messages.getString("NODE_OS_STARTUP_DATE_TIME") + "=" + startupDateTime);
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow osHistoryRow = null;
				if (changeCategory.equals(m_deleteCategory)) {
					osHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeOsHistoryDetail.getRegDateTo(), changeCategory, nodeOsHistoryDetail.getOsName(),
							historyDtlStr.toString());
				} else {
					osHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeOsHistoryDetail.getRegDate(), changeCategory, nodeOsHistoryDetail.getOsName(),
							historyDtlStr.toString());
				}
				csv.add(osHistoryRow);
			}
		}
		csv.writeRows();
	}

	private void writeCpuHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodeCpuHistoryDetail> nodeCpuHistoryList) throws IOException {
		for (int index = 0; index < nodeCpuHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodeCpuHistoryDetail nodeCpuHistoryDetail = nodeCpuHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isDeviceDeleted(nodeCpuHistoryList.subList(0, index), nodeCpuHistoryDetail.getDeviceName(),
					nodeCpuHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodeCpuHistoryDetail.getRegDate())) {
				if (controller.hasOldCPUNodeConfigHistory(facilityId, nodeCpuHistoryDetail.getRegDate(),
						nodeCpuHistoryDetail.getDeviceName())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("DEVICE_NAME") + "=" + nodeCpuHistoryDetail.getDeviceName());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_INDEX") + "=" + nodeCpuHistoryDetail.getDeviceIndex());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_TYPE") + "=" + nodeCpuHistoryDetail.getDeviceType());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_SIZE") + "=" + nodeCpuHistoryDetail.getDeviceSize());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("DEVICE_SIZE_UNIT") + "=" + nodeCpuHistoryDetail.getDeviceSizeUnit());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DESCRIPTION") + "=" + nodeCpuHistoryDetail.getDeviceDescription());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("CPU_CORE_COUNT") + "=" + nodeCpuHistoryDetail.getCoreCount());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("CPU_THREAD_COUNT") + "=" + nodeCpuHistoryDetail.getThreadCount());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("CPU_CLOCK_COUNT") + "=" + nodeCpuHistoryDetail.getClockCount());
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow cpuHistoryRow = null;
				if (changeCategory.equals(m_deleteCategory)) {
					cpuHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeCpuHistoryDetail.getRegDateTo(), changeCategory,
							nodeCpuHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				} else {
					cpuHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeCpuHistoryDetail.getRegDate(), changeCategory,
							nodeCpuHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				}
				csv.add(cpuHistoryRow);
			}
		}
		csv.writeRows();
	}

	private void writeMemoryHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodeMemoryHistoryDetail> nodeMemoryHistoryList) throws IOException {
		for (int index = 0; index < nodeMemoryHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodeMemoryHistoryDetail nodeMemoryHistoryDetail = nodeMemoryHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isDeviceDeleted(nodeMemoryHistoryList.subList(0, index), nodeMemoryHistoryDetail.getDeviceName(),
					nodeMemoryHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodeMemoryHistoryDetail.getRegDate())) {
				if (controller.hasOldMemoryNodeConfigHistory(facilityId, nodeMemoryHistoryDetail.getRegDate(),
						nodeMemoryHistoryDetail.getDeviceName())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("DEVICE_NAME") + "=" + nodeMemoryHistoryDetail.getDeviceName());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_INDEX") + "=" + nodeMemoryHistoryDetail.getDeviceIndex());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_TYPE") + "=" + nodeMemoryHistoryDetail.getDeviceType());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_SIZE") + "=" + nodeMemoryHistoryDetail.getDeviceSize());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("DEVICE_SIZE_UNIT") + "=" + nodeMemoryHistoryDetail.getDeviceSizeUnit());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("DESCRIPTION") + "=" + nodeMemoryHistoryDetail.getDeviceDescription());
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow memoryHistoryRow = null;
				if (changeCategory.equals(m_deleteCategory)) {
					memoryHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeMemoryHistoryDetail.getRegDateTo(), changeCategory,
							nodeMemoryHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				} else {
					memoryHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeMemoryHistoryDetail.getRegDate(), changeCategory,
							nodeMemoryHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				}
				csv.add(memoryHistoryRow);
			}
		}
		csv.writeRows();
	}

	private void writeNicHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodeNetworkInterfaceHistoryDetail> nodeNicHistoryList) throws IOException {
		for (int index = 0; index < nodeNicHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodeNetworkInterfaceHistoryDetail nodeNicHistoryDetail = nodeNicHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isDeviceDeleted(nodeNicHistoryList.subList(0, index), nodeNicHistoryDetail.getDeviceName(),
					nodeNicHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodeNicHistoryDetail.getRegDate())) {
				if (controller.hasOldNICNodeConfigHistory(facilityId, nodeNicHistoryDetail.getRegDate(),
						nodeNicHistoryDetail.getDeviceName())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("DEVICE_NAME") + "=" + nodeNicHistoryDetail.getDeviceName());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_INDEX") + "=" + nodeNicHistoryDetail.getDeviceIndex());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_TYPE") + "=" + nodeNicHistoryDetail.getDeviceType());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_SIZE") + "=" + nodeNicHistoryDetail.getDeviceSize());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("DEVICE_SIZE_UNIT") + "=" + nodeNicHistoryDetail.getDeviceSizeUnit());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DESCRIPTION") + "=" + nodeNicHistoryDetail.getDeviceDescription());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("NIC_IP_ADDRESS") + "=" + nodeNicHistoryDetail.getNicIpAddress());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("NIC_MAC_ADDRESS") + "=" + nodeNicHistoryDetail.getNicMacAddress());
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow nicHistoryRow = null;
				if (changeCategory.equals(m_deleteCategory)) {
					nicHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeNicHistoryDetail.getRegDateTo(), changeCategory,
							nodeNicHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				} else {
					nicHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeNicHistoryDetail.getRegDate(), changeCategory,
							nodeNicHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				}
				csv.add(nicHistoryRow);
			}
		}
		csv.writeRows();
	}

	private void writeDiskHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodeDiskHistoryDetail> nodeDiskHistoryList) throws IOException {
		for (int index = 0; index < nodeDiskHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodeDiskHistoryDetail nodeDiskHistoryDetail = nodeDiskHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isDeviceDeleted(nodeDiskHistoryList.subList(0, index), nodeDiskHistoryDetail.getDeviceName(),
					nodeDiskHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodeDiskHistoryDetail.getRegDate())) {
				if (controller.hasOldDiskNodeConfigHistory(facilityId, nodeDiskHistoryDetail.getRegDate(),
						nodeDiskHistoryDetail.getDeviceName())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("DEVICE_NAME") + "=" + nodeDiskHistoryDetail.getDeviceName());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_INDEX") + "=" + nodeDiskHistoryDetail.getDeviceIndex());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_TYPE") + "=" + nodeDiskHistoryDetail.getDeviceType());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_SIZE") + "=" + nodeDiskHistoryDetail.getDeviceSize());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("DEVICE_SIZE_UNIT") + "=" + nodeDiskHistoryDetail.getDeviceSizeUnit());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("DESCRIPTION") + "=" + nodeDiskHistoryDetail.getDeviceDescription());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DISK_RPM") + "=" + nodeDiskHistoryDetail.getDiskRpm());
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow diskHistoryRow = null;
				if (changeCategory.equals(m_deleteCategory)) {
					diskHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeDiskHistoryDetail.getRegDateTo(), changeCategory,
							nodeDiskHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				} else {
					diskHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeDiskHistoryDetail.getRegDate(), changeCategory,
							nodeDiskHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				}
				csv.add(diskHistoryRow);
			}
		}
		csv.writeRows();
	}

	private void writeFileSystemHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodeFilesystemHistoryDetail> nodeFileSystemHistoryList) throws IOException {
		for (int index = 0; index < nodeFileSystemHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodeFilesystemHistoryDetail nodeFileSystemHistoryDetail = nodeFileSystemHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isDeviceDeleted(nodeFileSystemHistoryList.subList(0, index),
					nodeFileSystemHistoryDetail.getDeviceName(), nodeFileSystemHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodeFileSystemHistoryDetail.getRegDate())) {
				if (controller.hasOldFileSystemNodeConfigHistory(facilityId, nodeFileSystemHistoryDetail.getRegDate(),
						nodeFileSystemHistoryDetail.getDeviceName())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("DEVICE_NAME") + "=" + nodeFileSystemHistoryDetail.getDeviceName());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("DEVICE_INDEX") + "=" + nodeFileSystemHistoryDetail.getDeviceIndex());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_TYPE") + "=" + nodeFileSystemHistoryDetail.getDeviceType());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(Messages.getString("DEVICE_SIZE") + "=" + nodeFileSystemHistoryDetail.getDeviceSize());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(
					Messages.getString("DEVICE_SIZE_UNIT") + "=" + nodeFileSystemHistoryDetail.getDeviceSizeUnit());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(
					Messages.getString("FILE_SYSTEM_TYPE") + "=" + nodeFileSystemHistoryDetail.getFilesystemType());
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow fileSystemHistoryRow = null;
				if (changeCategory.equals(m_deleteCategory)) {
					fileSystemHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeFileSystemHistoryDetail.getRegDateTo(), changeCategory,
							nodeFileSystemHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				} else {
					fileSystemHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeFileSystemHistoryDetail.getRegDate(), changeCategory,
							nodeFileSystemHistoryDetail.getDeviceDisplayName(), historyDtlStr.toString());
				}
				csv.add(fileSystemHistoryRow);
			}
		}
		csv.writeRows();
	}

	private void writePackageHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodePackageHistoryDetail> nodePackageHistoryList) throws IOException {
		for (int index = 0; index < nodePackageHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodePackageHistoryDetail nodePackageHistoryDetail = nodePackageHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isPackageDeleted(nodePackageHistoryList.subList(0, index), nodePackageHistoryDetail.getPackageId(),
					nodePackageHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodePackageHistoryDetail.getRegDate())) {
				if (controller.hasOldPackageNodeConfigHistory(facilityId, nodePackageHistoryDetail.getRegDate(),
						nodePackageHistoryDetail.getPackageId())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("NODE_PACKAGE_ID") + "=" + nodePackageHistoryDetail.getPackageId());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("NODE_PACKAGE_VERSION") + "=" + nodePackageHistoryDetail.getVersion());
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("NODE_PACKAGE_RELEASE") + "=" + nodePackageHistoryDetail.getRelease());
			historyDtlStr.append("<br/>");
			Timestamp installDateTime = null;
			if (nodePackageHistoryDetail.getInstallDate() != null) {
				installDateTime = new Timestamp(nodePackageHistoryDetail.getInstallDate());
				installDateTime.setNanos(0);
			}
			historyDtlStr.append(Messages.getString("NODE_PACKAGE_INSTALL_DATE") + "=" + installDateTime);
			historyDtlStr.append("<br/>");
			historyDtlStr
					.append(Messages.getString("NODE_PACKAGE_VENDOR") + "=" + nodePackageHistoryDetail.getVendor());
			historyDtlStr.append("<br/>");
			historyDtlStr.append(
					Messages.getString("NODE_PACKAGE_ARCHITECTURE") + "=" + nodePackageHistoryDetail.getArchitecture());
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow packageHistoryRow = null;
				if (changeCategory.equals(m_deleteCategory)) {
					packageHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodePackageHistoryDetail.getRegDateTo(), changeCategory,
							nodePackageHistoryDetail.getPackageName(), historyDtlStr.toString());
				} else {
					packageHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodePackageHistoryDetail.getRegDate(), changeCategory,
							nodePackageHistoryDetail.getPackageName(), historyDtlStr.toString());
				}
				csv.add(packageHistoryRow);
			}
		}
		csv.writeRows();
	}

	private void writeCustomHistoryToCSV(NodeConfigHistoryCSV csv, String facilityId, String facilityName,
			List<NodeCustomHistoryDetail> nodeCustomHistoryList) throws IOException {
		for (int index = 0; index < nodeCustomHistoryList.size(); index++) {
			List<String> changeCategoryList = new ArrayList<String>();
			NodeCustomHistoryDetail nodeCustomHistoryDetail = nodeCustomHistoryList.get(index);
			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			// ??????????????????
			if (isCustomDataDeleted(nodeCustomHistoryList.subList(0, index), nodeCustomHistoryDetail.getSettingId(),
					nodeCustomHistoryDetail.getSettingCustomId(), nodeCustomHistoryDetail.getRegDateTo())) {
				changeCategoryList.add(m_deleteCategory);
			}
			if (isRegDateValid(nodeCustomHistoryDetail.getRegDate())) {
				if (controller.hasOldCustomNodeConfigHistory(facilityId, nodeCustomHistoryDetail.getRegDate(),
						nodeCustomHistoryDetail.getSettingId(), nodeCustomHistoryDetail.getSettingCustomId())) {
					changeCategoryList.add(m_updateCategory);
				} else {
					changeCategoryList.add(m_addCategory);
				}
			}
			// ??????????????????
			StringBuilder historyDtlStr = new StringBuilder();
			historyDtlStr.append("\"");
			historyDtlStr.append(Messages.getString("VALUE") + "=" + nodeCustomHistoryDetail.getValue());
			historyDtlStr.append("\"");
			//CSV????????????
			for (String changeCategory : changeCategoryList) {
				NodeConfigHistoryCsvRow customHistoryRow = null;
				String displayName = nodeCustomHistoryDetail.getSettingId() + " / "
						+ nodeCustomHistoryDetail.getDisplayName();
				if (changeCategory.equals(m_deleteCategory)) {
					customHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeCustomHistoryDetail.getRegDateTo(), changeCategory, displayName,
							historyDtlStr.toString());
				} else {
					customHistoryRow = new NodeConfigHistoryCsvRow(facilityId, facilityName,
							nodeCustomHistoryDetail.getRegDate(), changeCategory, displayName,
							historyDtlStr.toString());
				}
				csv.add(customHistoryRow);
			}
		}
		csv.writeRows();
	}

	/**
	 * ?????????????????????CSV??????????????????Hinemos DB??????????????????
	 */
	@Override
	public HashMap<String, Object> createDataSource(int num) throws ReportingPropertyNotFound {

		if (m_propertiesMap.get(SUFFIX_KEY_VALUE + "." + num).isEmpty()) {
			throw new ReportingPropertyNotFound(SUFFIX_KEY_VALUE + "." + num + " is not defined.");
		}

		String suffix = m_propertiesMap.get(SUFFIX_KEY_VALUE + "." + num);
		String dayString = new SimpleDateFormat("MMdd").format(m_startDate);

		String csvFileName = ReportUtil.getCsvFileNameForTemplateType(m_templateId, suffix + "_" + dayString);
		HashMap<String, Object> retMap = new HashMap<String, Object>();

		m_maxNodeHisorySize = Integer.parseInt(isDefine("node.config.history.max", "30"));
		m_addCategory = isDefine("node.config.add", "ADD");
		m_updateCategory = isDefine("node.config.update", "UPDATE");
		m_deleteCategory = isDefine("node.config.delete", "DELETE");

		m_log.debug("createDataSource: facilityId=" + ReportUtil.getFacilityId() + ", startDate=" + m_startDate
				+ ", endDate=" + m_endDate + ", csvFileName=" + csvFileName);

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(csvFileName), false))) {
			// ????????????????????????????????????
			m_targetNodes = ReportUtil.getNodesInScope(ReportUtil.getFacilityId());

			ReportingNodeConfigControllerBean controller = new ReportingNodeConfigControllerBean();
			NodeConfigHistoryCSV csv = new NodeConfigHistoryCSV(bw);
			csv.writeHeader();
			for (String[] nodeInfo : m_targetNodes) {
				String facilityId = nodeInfo[0];
				String facilityName = nodeInfo[1];
				switch (m_nodeConfigId) {
				case "OS":
					List<NodeOsHistoryDetail> nodeOsHistoryList = controller.getNodeOsHistoryDetail(facilityId,
							m_startDate.getTime(), m_endDate.getTime(), m_maxNodeHisorySize);
					writeOsHistoryToCSV(csv, facilityId, facilityName, nodeOsHistoryList);
					break;
				case "CPU":
					List<NodeCpuHistoryDetail> nodeCpuHistoryList = controller.getNodeCpuHistoryDetail(facilityId,
							m_startDate.getTime(), m_endDate.getTime(), m_maxNodeHisorySize);
					writeCpuHistoryToCSV(csv, facilityId, facilityName, nodeCpuHistoryList);
					break;
				case "MEMORY":
					List<NodeMemoryHistoryDetail> nodeMemoryHistoryList = controller.getNodeMemoryHistoryDetail(
							facilityId, m_startDate.getTime(), m_endDate.getTime(), m_maxNodeHisorySize);
					writeMemoryHistoryToCSV(csv, facilityId, facilityName, nodeMemoryHistoryList);
					break;
				case "NIC":
					List<NodeNetworkInterfaceHistoryDetail> nodeNicHistoryList = controller
							.getNodeNetworkInterfaceHistoryDetail(facilityId, m_startDate.getTime(),
									m_endDate.getTime(), m_maxNodeHisorySize);
					writeNicHistoryToCSV(csv, facilityId, facilityName, nodeNicHistoryList);
					break;
				case "DISK":
					List<NodeDiskHistoryDetail> nodeDiskHistoryList = controller.getNodeDiskHistoryDetail(facilityId,
							m_startDate.getTime(), m_endDate.getTime(), m_maxNodeHisorySize);
					writeDiskHistoryToCSV(csv, facilityId, facilityName, nodeDiskHistoryList);
					break;
				case "FILESYSTEM":
					List<NodeFilesystemHistoryDetail> nodeFileSystemHistoryList = controller
							.getNodeFilesystemHistoryDetail(facilityId, m_startDate.getTime(), m_endDate.getTime(),
									m_maxNodeHisorySize);
					writeFileSystemHistoryToCSV(csv, facilityId, facilityName, nodeFileSystemHistoryList);
					break;
				case "PACKAGE":
					List<NodePackageHistoryDetail> nodePackageHistoryList = controller.getNodePackageHistoryDetail(
							facilityId, m_startDate.getTime(), m_endDate.getTime(), m_maxNodeHisorySize);
					writePackageHistoryToCSV(csv, facilityId, facilityName, nodePackageHistoryList);
					break;
				case "CUSTOM":
					List<NodeCustomHistoryDetail> nodeCustomHistoryList = controller.getNodeCustomHistoryDetail(
							facilityId, m_startDate.getTime(), m_endDate.getTime(), m_maxNodeHisorySize);
					writeCustomHistoryToCSV(csv, facilityId, facilityName, nodeCustomHistoryList);
					break;
				default:
					break;
				}
			}

			JRCsvDataSource ds = new JRCsvDataSource(csvFileName);
			ds.setUseFirstRowAsHeader(true);
			retMap.put(ReportingConstant.STR_DS + "_" + num, ds);

		} catch (IOException e) {
			m_log.error(e, e);
		} catch (Exception e) {
			m_log.error(e, e);
		}

		return retMap;
	}
}
