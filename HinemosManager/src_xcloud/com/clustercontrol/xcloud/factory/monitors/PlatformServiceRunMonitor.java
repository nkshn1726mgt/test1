/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.monitors;

import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorExceptionMessage;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorExceptionMessageOrg;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorExceptionMessageOrg2;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorMessage;
import static com.clustercontrol.xcloud.util.CloudMessageUtil.createCloudServiceMonitorMessageOrg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.EntityExistsException;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.plugin.model.MonitorPluginStringInfo;
import com.clustercontrol.monitor.plugin.model.PluginCheckInfo;
import com.clustercontrol.monitor.plugin.util.QueryUtil;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.util.NotifyCallback;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.bean.PlatformServiceCondition;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.util.RepositoryControllerBeanWrapper;

/**
* ??????????????????????????????????????????
*
* @version 5.0.0
* @since 2.0.0
*/
public class PlatformServiceRunMonitor extends RunMonitorTruthValueType {
	public static final String monitorTypeId = "MON_CLOUD_SERVICE_CONDITION";
	public static final int monitorType = MonitorTypeConstant.TYPE_TRUTH;
	public static final String STRING_CLOUD_SERVICE_DONDITION = CloudMessageConstant.CLOUDSERVICE_CONDITION_MONITOR.getMessage();
	
	public static final String key_targets = "targets";
	
	/** ???????????? */
	private PluginCheckInfo m_plugin = null;

	/** ???????????????????????? */
	private List<MonitorPluginStringInfo> m_monitorPluginStringInfoList;
	
	private static Logger logger = Logger.getLogger(PlatformServiceRunMonitor.class);

	/**
	 * ?????????????????????
	 * 
	 */
	public PlatformServiceRunMonitor() {
		super();
	}

	/**
	 * ????????????????????????????????????CallableTask???????????????????????????????????????????????????????????????
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.CallableTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new PlatformServiceRunMonitor();
	}

	/**
	 * [?????????????????????]?????????????????????????????????????????????
	 * 
	 * ????????????????????????1????????????????????????ID????????????????????????????????????ID???????????????????????????????????????????????????????????????????????????
	 * ?????????????????????????????????????????????runMonitorInfo?????????????????????
	 * 
	 */
	@Override
	protected List<OutputBasicInfo> runMonitorInfo() throws FacilityNotFound, MonitorNotFound, EntityExistsException, InvalidRole, HinemosUnknown {
		logger.debug("runMonitorInfo()");

		// XXX
		// ?????????????????????deadlock????????????????????????
		// collect??????????????????notify???ret.add????????????????????????
		List<OutputBasicInfo> ret = new ArrayList<>();
		m_now = new Date(System.currentTimeMillis());

		// ???????????????????????????
		if (!setMonitorInfo(m_monitorTypeId, m_monitorId)) {
			// ????????????
			return ret;
		}

		// ?????????????????????
		setJudgementInfo();

		// ?????????????????????????????????
		setCheckInfo();

		// ??????????????????????????????
		m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

		logger.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);
		
		// ?????????????????????
		List<OutputBasicInfo> outputs = collectTargets(m_facilityId);

		// ??????????????????
		if (m_monitor.getMonitorFlg()) {
			for (OutputBasicInfo output: outputs) {
				output.setNotifyGroupId(m_monitor.getNotifyGroupId());
				// ????????????
				new JpaTransactionManager().addCallback(new NotifyCallback(output));
			}
		}

		return ret;
	}

	/**
	 * ?????????????????????collectList()???????????????????????????????????????????????????????????????
	 */
	@Override
	public boolean collect(String facilityId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMessageOrg(int key) {
		throw new UnsupportedOperationException();
	}
	
	private static List<String> recursiveCollectScopes(FacilityTreeItem treeItem, String targetId) {
		if (targetId.equals(treeItem.getData().getFacilityId()) && treeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			return Arrays.asList(targetId);
		} else {
			for (FacilityTreeItem fti: treeItem.getChildren()) {
				List<String> result = recursiveCollectScopes(fti, targetId);
				if (!result.isEmpty()) {
					List<String> list = new ArrayList<>();
					list.add(fti.getData().getFacilityId());
					list.addAll(result);
					return list;
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * ????????????????????????????????????facilityId??????????????????
	 * @throws HinemosUnknown 
	 * 
	 */
	public List<OutputBasicInfo> collectTargets(String facilityId) throws FacilityNotFound, HinemosUnknown {
		try (SessionScope sessionScope = SessionScope.open()) {
			if (m_isNode)
				return Arrays.asList(createFailureOutputBasicInfo(
						facilityId,
						ErrorCode.MONITOR_CLOUDSERVICE_ONLY_NODE.getMessage(),
						ErrorCode.MONITOR_CLOUDSERVICE_ONLY_NODE.getMessage(),
						new Date().getTime()));
			
			FacilityTreeItem treeItem = RepositoryControllerBeanWrapper.bean().getFacilityTree(m_monitor.getOwnerRoleId(), Locale.getDefault());
			List<String> results = recursiveCollectScopes(treeItem, facilityId);
			
			if (results.isEmpty())
				throw new InternalManagerError(String.format("No found Scope. facilityId=%s", facilityId));
			
			// ???????????????????????????????????????????????????????????????????????????
			Object rootId = results.get(0);
			if (!CloudConstants.privateRootId.equals(rootId) && !CloudConstants.publicRootId.equals(rootId))
				throw new InternalManagerError(String.format("Invalid facilityId. facilityId = %s", facilityId));
			
			// ??????????????????????????????????????? Id ????????????
			Object cloudScopeScopeId = facilityId;
			if (results.size() > 1)
				cloudScopeScopeId = results.get(1); 
			
			// ???????????? Id ???????????????????????????????????????????????????
			CloudScopeEntity cloudScopeEntity = null;
			for (CloudScopeEntity cs: CloudManager.singleton().getCloudScopes().getAllCloudScopes()) {
				if (FacilityIdUtil.getCloudScopeScopeId(cs).equals(cloudScopeScopeId)) {
					cloudScopeEntity = cs;
					break;
				}
			}
			
			List<OutputBasicInfo> outputs = new ArrayList<>();
			if (cloudScopeEntity == null) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("scope in not cloudScope. facilityId = %s", facilityId));
				}
				return outputs;
			}
			
			// ?????????????????????????????????
			List<PlatformServiceCondition> conditions = Collections.emptyList();
			if (FacilityIdUtil.getCloudScopeScopeId(cloudScopeEntity).equals(facilityId)) {
				conditions = CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeEntity.getId());
			} else {
				for (LocationEntity location: cloudScopeEntity.getLocations()) {
					if (FacilityIdUtil.getLocationScopeId(cloudScopeEntity.getId(), location).equals(facilityId)) {
						conditions = CloudManager.singleton().getCloudScopes().getPlatformServiceConditions(cloudScopeEntity.getId(), location.getLocationId());
						break;
					}
				}
			}
			if (conditions.isEmpty()) {
				return Arrays.asList(createFailureOutputBasicInfo(
						facilityId,
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						new Date().getTime()));
			}
		
			// ?????????????????????
			Set<String> serviceIds = new TreeSet<>();
			for (MonitorPluginStringInfo entry: m_monitorPluginStringInfoList) {
				if (key_targets.equals(entry.getId().getKey())) {
					String[] targets = entry.getValue().split(",");
					serviceIds.addAll(Arrays.asList(targets));
					break;
				}
			}
	
			if (serviceIds.isEmpty()) {
				return Arrays.asList(createFailureOutputBasicInfo(
						facilityId,
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						ErrorCode.MONITOR_CLOUDSERVICE_FOUND_NO_SERVICEID.getMessage(),
						new Date().getTime()));
			}
			
			for (PlatformServiceCondition condition: conditions) {
				if (!serviceIds.contains(condition.getId()))
					continue;
				
				serviceIds.remove(condition.getId());
				
				switch (condition.getStatus()) {
				case normal:
					outputs.add(createAvailableOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						(condition.getDetail() == null && condition.getDetail().isEmpty()) ? condition.getMessage(): condition.getMessage() + ": " + condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				case warn:
				case abnormal:
					outputs.add(createUnavailableOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						(condition.getDetail() == null && condition.getDetail().isEmpty()) ? condition.getMessage(): condition.getMessage() + ": " + condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				case unknown:
					outputs.add(createUnknownOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						(condition.getDetail() == null && condition.getDetail().isEmpty()) ? condition.getMessage(): condition.getMessage() + ": " + condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				case exception:
					outputs.add(createExceptionOutputBasicInfo(
						facilityId,
						cloudScopeEntity.getPlatform().getName(),
						condition.getId(),
						condition.getServiceName(),
						condition.getMessage(),
						condition.getDetail(),
						condition.getRecordDate()
						));
					break;
				}
			}
	
			return outputs;
		} catch (CloudManagerException e) {
			return Arrays.asList(createExceptionOutputBasicInfo2(facilityId, e, new Date().getTime()));
		}
	}

	/**
	 * ?????????????????????????????????????????????
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// ?????????????????????
		m_plugin = QueryUtil.getMonitorPluginInfoPK(m_monitorId);
		// ?????????????????????
		m_monitorPluginStringInfoList = m_plugin.getMonitorPluginStringInfoList();
	}
	
	protected OutputBasicInfo createOutputBasicInfo(CloudUtil.Priority priority, String facilityId, String subKey, String message, String messageOrg, Long generationDate) {
		return CloudUtil.createOutputBasicInfoEx(priority, monitorTypeId, m_monitorId, subKey, m_monitor.getApplication(), facilityId, message, messageOrg, generationDate);
	}
	
	protected OutputBasicInfo createFailureOutputBasicInfo(String facilityId, String message, String messageOrg, Long generationDate) {
		// ???????????????????????? ID ?????????????????????????????????????????????????????????
		return createOutputBasicInfo(
				CloudUtil.Priority.priority(m_failurePriority),
				facilityId,
				"",
				message,
				messageOrg,
				generationDate
				);
	}

	protected OutputBasicInfo createAvailableOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		MonitorJudgementInfo info = m_judgementInfoList.get(TruthConstant.TYPE_TRUE);
		return createFormattedOutputBasicInfo(CloudUtil.Priority.priority(info.getPriority()), CloudMessageConstant.CLOUDSERVICE_AVAILABLE.getMessage(), facilityId, cloudPlatformName, targetId, targetName, message, messageOrg, generationDate);
	}

	protected OutputBasicInfo createUnavailableOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		MonitorJudgementInfo info = m_judgementInfoList.get(TruthConstant.TYPE_FALSE);
		return createFormattedOutputBasicInfo(CloudUtil.Priority.priority(info.getPriority()), CloudMessageConstant.CLOUDSERVICE_UNAVAILABLE.getMessage(), facilityId, cloudPlatformName, targetId, targetName, message, messageOrg, generationDate);
	}

	protected OutputBasicInfo createUnknownOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		return createFormattedOutputBasicInfo(CloudUtil.Priority.UNKNOWN, CloudMessageConstant.CLOUDSERVICE_UNKNOWN.getMessage(), facilityId, cloudPlatformName, targetId, targetName, message, messageOrg, generationDate);
	}

	protected OutputBasicInfo createFormattedOutputBasicInfo(CloudUtil.Priority priority, String result, String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		// ???????????????????????? ID ?????????????????????????????????????????????????????????
		return createOutputBasicInfo(
				priority,
				facilityId,
				targetId,
				createCloudServiceMonitorMessage(result, cloudPlatformName, targetName, message),
				createCloudServiceMonitorMessageOrg(result, cloudPlatformName, targetName, messageOrg),
				generationDate
				);
	}
	
	protected OutputBasicInfo createExceptionOutputBasicInfo(String facilityId, String cloudPlatformName, String targetId, String targetName, String message, String messageOrg, Long generationDate) {
		// ???????????????????????? ID ?????????????????????????????????????????????????????????
		return createFailureOutputBasicInfo(
				facilityId,
				createCloudServiceMonitorExceptionMessage(cloudPlatformName, targetName),
				createCloudServiceMonitorExceptionMessageOrg(cloudPlatformName, targetName, message, messageOrg),
				generationDate
				);
	}

	protected OutputBasicInfo createExceptionOutputBasicInfo2(String facilityId, Exception exception, Long generationDate) {
		// ???????????????????????? ID ?????????????????????????????????????????????????????????
		return createFailureOutputBasicInfo(
				facilityId,
				CloudMessageConstant.CLOUDSERVICE_EXCEPTION.getMessage(),
				createCloudServiceMonitorExceptionMessageOrg2(exception),
				generationDate
				);
	}
}
