/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.analytics.factory.RunMonitorLogcount;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.custom.bean.CommandExecuteDTO;
import com.clustercontrol.custom.factory.SelectCustom;
import com.clustercontrol.fault.CustomInvalid;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.factory.RunMonitorAgent;
import com.clustercontrol.http.factory.RunMonitorHttp;
import com.clustercontrol.http.factory.RunMonitorHttpScenario;
import com.clustercontrol.http.factory.RunMonitorHttpString;
import com.clustercontrol.jmx.factory.RunMonitorJmx;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobOperationInfo;
import com.clustercontrol.jobmanagement.bean.JobSessionRequestMessage;
import com.clustercontrol.jobmanagement.bean.JobTriggerInfo;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.factory.CreateJobSession;
import com.clustercontrol.jobmanagement.factory.JobOperationJudgment;
import com.clustercontrol.jobmanagement.factory.JobSessionImpl;
import com.clustercontrol.jobmanagement.factory.JobSessionJobImpl;
import com.clustercontrol.jobmanagement.factory.JobSessionNodeImpl;
import com.clustercontrol.jobmanagement.factory.OperateForceRunOfJob;
import com.clustercontrol.jobmanagement.factory.OperateForceStopOfJob;
import com.clustercontrol.jobmanagement.factory.OperateMaintenanceOfJob;
import com.clustercontrol.jobmanagement.factory.OperateSkipOfJob;
import com.clustercontrol.jobmanagement.factory.OperateStartOfJob;
import com.clustercontrol.jobmanagement.factory.OperateStopOfJob;
import com.clustercontrol.jobmanagement.factory.OperateSuspendOfJob;
import com.clustercontrol.jobmanagement.factory.OperateWaitOfJob;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.jobmanagement.util.MonitorJobWorker;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.performance.monitor.factory.RunMonitorPerformance;
import com.clustercontrol.ping.factory.RunMonitorPing;
import com.clustercontrol.port.factory.RunMonitorPort;
import com.clustercontrol.process.factory.RunMonitorProcess;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmp.factory.RunMonitorSnmp;
import com.clustercontrol.snmp.factory.RunMonitorSnmpString;
import com.clustercontrol.sql.factory.RunMonitorSql;
import com.clustercontrol.sql.factory.RunMonitorSqlString;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.util.apllog.AplLogger;
import com.clustercontrol.winservice.factory.RunMonitorWinService;


/**
 * ????????????????????????????????????????????? Session Bean ?????????<BR>
 * 
 */
public class JobRunManagementBean {
	/** ?????????????????????????????????<BR> */
	private static Log m_log = LogFactory.getLog( JobRunManagementBean.class );

	/** Quartz??????????????????????????????<BR> */
	public static final String GROUP_NAME = "JOB_MANAGEMENT";

	// ??????????????????????????????????????????
	private static final Semaphore duplicateExec = new Semaphore(1);

	/**
	 * Quartz???????????????????????????????????????<BR>
	 * <P>
	 * Quartz??????????????????????????????????????????<BR>
	 * <BR>
	 * ?????????????????????????????????????????????????????????????????????????????????????????????????????????<BR>
	 * ???????????????????????????????????????????????????????????????????????????????????????<BR>
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * 
	 * ????????????????????????????????????????????????????????????
	 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????Never??????????????????
	 * 
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#runJob()
	 * @see com.clustercontrol.jobmanagement.session.JobRunManagementBean#runWaitJob()
	 */
	public void run() throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		if (duplicateExec.tryAcquire()) {
			try {
				runSub();
			} finally {
				duplicateExec.release();
			}
		} else {
			m_log.warn("runningCheck is busy !!");
		}
	}
	
	public static ILock getLock (String sessionId) {
		ILockManager lm = LockManagerFactory.instance().create();
		return lm.create(JobSessionImpl.class.getName() + "-" + sessionId);
	}

	private void runSub() throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.trace("run() start");
		JpaTransactionManager jtm = null;
		List<String> unendSessionIdList = null;
		HashMap<String, List<JobSessionNodeEntityPK>> sessionNodeMap = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
			unendSessionIdList = new JobSessionImpl().getRunUnendSession();

			// ???????????????????????????????????????????????????
			sessionNodeMap = new JobSessionNodeImpl().checkTimeoutAll();
			jtm.commit();
		} catch (JobInfoNotFound e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("run() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		long from = HinemosTime.currentTimeMillis();
		for (String sessionId : unendSessionIdList) {
			m_log.trace("run() unendSessionId=" + sessionId);
			Boolean doRunningCheck = true;
			if (!JobSessionJobImpl.checkRemoveForceCheck(sessionId)) {
				if (JobSessionJobImpl.isSkipCheck(sessionId)) {
					doRunningCheck = false;
				}
			}

			ILock lock = getLock(sessionId);
			try {
				lock.writeLock();
				
				try{
					jtm = new JpaTransactionManager();
					jtm.begin();
					if (doRunningCheck) {
						//????????????????????????????????????????????????
						new JobSessionImpl().runningCheck(sessionId);
					} else {
						//????????????????????????????????????
						//???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
						new JobSessionImpl().waitingCheck(sessionId);
					}
					jtm.commit();
				} catch (JobInfoNotFound | HinemosUnknown | InvalidRole e){
					jtm.rollback();
					throw e;
				} catch (Exception e) {
					m_log.warn("run() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					jtm.close();
				}
			} finally {
				lock.writeUnlock();
			}
		}
		long to = HinemosTime.currentTimeMillis();
		long time1 = to - from;

		// ???????????????????????????????????????????????????
		from = HinemosTime.currentTimeMillis();
		int nodeCount = 0;
		for (Map.Entry<String, List<JobSessionNodeEntityPK>> entry : sessionNodeMap.entrySet()) {

			ILock lock = getLock(entry.getKey());
			try {
				lock.writeLock();
				
				try{
					jtm = new JpaTransactionManager();
					jtm.begin();
					for (JobSessionNodeEntityPK pk : entry.getValue()) {
						new JobSessionNodeImpl().checkTimeout(pk);
						nodeCount ++;
					}
					jtm.commit();
				} catch (JobInfoNotFound e) {
					jtm.rollback();
					throw e;
				} catch (Exception e) {
					m_log.warn("run() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					jtm.rollback();
					throw new HinemosUnknown(e.getMessage(), e);
				} finally {
					jtm.close();
				}
			} finally {
				lock.writeUnlock();
			}
		}
		to = HinemosTime.currentTimeMillis();
		long time2 = to - from;

		String message = "runningCheck(" + unendSessionIdList.size() + "): " + time1 + "ms" +
				", checkTimeout(" + sessionNodeMap.size() + "," + nodeCount + "): " + time2 + "ms";
		long total = time1 + time2;
		if (total > 30 * 1000) {
			m_log.warn(message + "!");
		} else if (total > 10 * 1000){
			m_log.info(message);
		} else {
			m_log.debug(message);
		}

		// ??????????????????????????????????????????????????????????????????
		Map<JobSessionNodeEntity, String> jobSessionNodeMonitorTrapMap = new HashMap<>();
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			List<JobSessionNodeEntity> jobSessionNodeList 
				= QueryUtil.getJobSessionNodeEntityFindByJobTypeEndIsNull_NONE(JobConstant.TYPE_MONITORJOB);
			for (JobSessionNodeEntity jobSessionNodeEntity : jobSessionNodeList) {
				JobInfoEntity jobInfoEntity = jobSessionNodeEntity.getJobSessionJobEntity().getJobInfoEntity();
				try {
					MonitorInfo monitorInfo 
						= com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(jobInfoEntity.getMonitorId());
					// ?????????????????????????????????
					if (!monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
							&& !monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
							&& !monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
							&& !monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
							&& !monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
							&& !monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
							&& !monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
							&& !monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
						continue;
					}
					jobSessionNodeMonitorTrapMap.put(jobSessionNodeEntity, monitorInfo.getMonitorTypeId());
				} catch (MonitorNotFound e) {
					m_log.debug("runSub() : monitorInfo is null. monitorId=" + jobInfoEntity.getMonitorId());
					continue;
				}
			}
		} catch (Exception e) {
			m_log.warn("runSub() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		if (jobSessionNodeMonitorTrapMap != null) {
			for (Map.Entry<JobSessionNodeEntity, String> entry : jobSessionNodeMonitorTrapMap.entrySet()) {
				ILock lock = getLock(entry.getKey().getId().getSessionId());
				try {
					lock.writeLock();
					
					new JobSessionNodeImpl().checkMonitorJobTimeout(entry.getKey(), entry.getValue());
				} finally {
					lock.writeUnlock();
				}
			}
		}
		
		// ???????????????????????????????????????
		// - ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		//   ??????????????????????????????????????????????????????????????????????????????????????????????????????
		//   ????????????????????????????????????
		Singletons.get(JobQueueContainer.class).activateJobs();
	}

	/**
	 * ??????????????????????????????<BR>
	/**
	 * CreateJobSessionTaskFactory??????????????????
	 * 
	 */
	public static void makeSession(JobSessionRequestMessage message) {
		String sessionId = message.getSessionId();
		String jobunitId = message.getJobunitId();
		String jobId = message.getJobId();
		OutputBasicInfo info = message.getOutputBasicInfo();
		JobTriggerInfo triggerInfo = message.getTriggerInfo();
		
		JpaTransactionManager jtm = null;

		ILock lock = getLock(sessionId);
		try {
			lock.writeLock();
			try {
				jtm = new JpaTransactionManager();
				HinemosEntityManager em = jtm.getEntityManager();
				jtm.begin();
	
				JobMstEntity job = QueryUtil.getJobMstPK(jobunitId, jobId);
				//JobSession?????????
				JobSessionEntity jobSessionEntity = new JobSessionEntity(sessionId);
				// ??????????????????
				jtm.checkEntityExists(JobSessionEntity.class, jobSessionEntity.getSessionId());
				jobSessionEntity.setJobunitId(jobunitId);
				jobSessionEntity.setJobId(job.getId().getJobId());
				jobSessionEntity.setScheduleDate(HinemosTime.currentTimeMillis());
				jobSessionEntity.setOperationFlg(0);
				jobSessionEntity.setTriggerType(triggerInfo.getTrigger_type());
				jobSessionEntity.setTriggerInfo(triggerInfo.getTrigger_info());
				// ??????
				jtm.getEntityManager().persist(jobSessionEntity);
	
				m_log.trace("jobSessionEntity SessionId : " + jobSessionEntity.getSessionId());
				m_log.trace("jobSessionEntity JobUnitId : " + jobSessionEntity.getJobunitId());
				m_log.trace("jobSessionEntity JobId : " + jobSessionEntity.getJobId());
	
				// ??????????????????????????????????????????
				// ????????????????????????
				JobSessionJobEntity jobSessionJobEntity
				= new JobSessionJobEntity(jobSessionEntity, CreateJobSession.TOP_JOBUNIT_ID, CreateJobSession.TOP_JOB_ID);
				// ??????????????????
				jtm.checkEntityExists(JobSessionJobEntity.class, jobSessionJobEntity.getId());
				jobSessionJobEntity.setStatus(StatusConstant.TYPE_WAIT);
				jobSessionJobEntity.setOwnerRoleId(JobUtil.createSessioniOwnerRoleId(CreateJobSession.TOP_JOBUNIT_ID));
				// ??????
				em.persist(jobSessionJobEntity);
				jobSessionJobEntity.relateToJobSessionEntity(jobSessionEntity);

				// ??????????????????????????????(????????????????????????????????????????????????)
				CreateJobSession.createJobSessionJob(job, sessionId, info, true, triggerInfo, null, null);
	
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("makeSession() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				String[] args = {jobId};
				AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_003_JOB, args);
				if (jtm != null) {
					jtm.rollback();
				}
				return;
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
			
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				//??????
				new JobSessionJobImpl().startJob(sessionId, jobunitId, jobId);
				jtm.commit();
			} catch (Exception e) {
				m_log.warn("makeSession() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				String[] args = {jobId};
				AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_003_JOB, args);
				if (jtm != null) {
					jtm.rollback();
				}
				return;
			} finally {
				if (jtm != null) {
					jtm.close();
				}
			}
		} finally {
			lock.writeUnlock();
		}
	}

	public void operation(JobOperationInfo property) throws HinemosUnknown, JobInfoNotFound, InvalidRole, IllegalStateException {
		String sessionId = property.getSessionId();
		String jobunitId = property.getJobunitId();
		String jobId = property.getJobId();
		String facilityId = property.getFacilityId();
		Integer control = property.getControl();
		Integer endStatus = property.getEndStatus();
		Integer endValue = property.getEndValue();

		m_log.info("operationJob() " + "jobId=" + jobId + ", facilityId= " + facilityId +
				", control=" + control + ", endStatus=" + endStatus + ", endValue=" + endValue);

		JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);
		if (sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILEJOB) {
			if (facilityId != null && facilityId.length() > 0) {
				// ?????????????????????????????????
				operationJob(sessionId, jobunitId, jobId + "_" + facilityId, control, endStatus, endValue);
			} else {
				// ?????????????????????????????????
				operationJob(sessionId, jobunitId, jobId, control, endStatus, endValue);
			}
		} else if (facilityId == null || facilityId.length() == 0) {
			// ?????????????????????????????????
			operationJob(sessionId, jobunitId, jobId, control, endStatus, endValue);
		} else {
			// ?????????????????????????????????
			operationNode(sessionId, jobunitId, jobId, facilityId, control, endValue);
		}
	}

	/**
	 * ????????????????????????????????????<BR>
	 * 
	 * @param property ?????????????????????????????????
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws IllegalStateException
	 * @throws InvalidRole
	 */
	private void operationNode(String sessionId, String jobunitId, String jobId, String facilityId, Integer control, Integer endValue)
			throws HinemosUnknown, JobInfoNotFound, IllegalStateException, InvalidRole {
		m_log.info("operationJob() " + "jobId=" + jobId + ", control=" + control + ", endValue=" + endValue);
		JpaTransactionManager jtm = null;

		int status = 0;
		int jobType = 0;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			try {
				//???????????????ID????????????ID??????????????????????????????????????????
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

				//???????????????????????????
				if(sessionJob.getJobInfoEntity().getJobType() == JobConstant.TYPE_FILEJOB){
					jobId = jobId + "_" + facilityId;

					//???????????????ID????????????ID??????????????????????????????????????????
					JobSessionJobEntity childSessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

					//?????????????????????
					status = childSessionJob.getStatus();

					//???????????????????????????
					jobType = JobOperationJudgment.TYPE_JOBNET;
				} else {
					//???????????????ID????????????ID??????????????????????????????????????????
					JobSessionNodeEntity sessionNode = QueryUtil.getJobSessionNodePK(sessionId, jobunitId, jobId, facilityId);

					//?????????????????????
					status = sessionNode.getStatus();

					//???????????????????????????
					jobType = JobOperationJudgment.TYPE_NODE;
				}
			} catch (Exception e) {
				m_log.warn("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			//??????????????????????????????????????????????????????????????????????????????
			if(JobOperationJudgment.judgment(control, jobType, status)){
				if(control == OperationConstant.TYPE_START_AT_ONCE){
					//??????[??????]
					try {
						new OperateStartOfJob().startNode(sessionId, jobunitId, jobId, facilityId);
					} catch (JobInfoNotFound | InvalidRole e) {
						String[] args = {sessionId, jobId, facilityId};
						AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_007_JOB, args);
						throw e;
					}
				} else if(control == OperationConstant.TYPE_STOP_AT_ONCE){
					try {
						//??????[????????????]
						new OperateStopOfJob().stopNode(sessionId, jobunitId, jobId, facilityId);
					} catch (JobInfoNotFound | InvalidRole | HinemosUnknown e) {
						String[] args = {sessionId, jobId, facilityId};
						AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_011_JOB, args);
						throw e;
					}
				} else if(control == OperationConstant.TYPE_STOP_MAINTENANCE){
					//??????[????????????]
					if(endValue == null){
						throw new NullPointerException();
					}
					try {
						new OperateMaintenanceOfJob().maintenanceNode(sessionId, jobunitId, jobId, facilityId, endValue);
					} catch (JobInfoNotFound | HinemosUnknown | InvalidRole e) {
						String[] args = {sessionId, jobId, facilityId};
						AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_015_JOB, args);
						throw e;
					} catch (NullPointerException e) {
						String[] args = {sessionId, jobId, facilityId};
						AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_015_JOB, args);
						m_log.warn("operationJob() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				} else if(control == OperationConstant.TYPE_STOP_FORCE){
					//??????[??????]
					if(endValue == null){
						throw new NullPointerException();
					}
					try {
						new OperateForceStopOfJob().forceStopNode(sessionId, jobunitId, jobId, facilityId, endValue);//????????????
					} catch (JobInfoNotFound | HinemosUnknown | InvalidRole e) {
						String[] args = {sessionId, jobId, facilityId};
						AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_018_JOB, args);
						throw e;
					} catch (NullPointerException e) {
						String[] args = {sessionId, jobId, facilityId};
						AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_018_JOB, args);
						m_log.warn("operationJob() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						throw new HinemosUnknown(e.getMessage(), e);
					}
				} else {
					m_log.warn("operationNode() : unknown control. " + control);
				}
			} else {
				IllegalStateException e = new IllegalStateException();
				m_log.info("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}

			jtm.commit();
		} catch (HinemosUnknown | JobInfoNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (IllegalStateException e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("operationJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * ????????????????????????????????????<BR>
	 * 
	 * @param property ?????????????????????????????????
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws IllegalStateException
	 * @throws InvalidRole
	 */
	private void operationJob(String sessionId, String jobunitId, String jobId, Integer control,
			Integer endStatus, Integer endValue)
					throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.info("operationJob() " + "jobId=" + jobId + ", control=" + control +
				", endStatus=" + endStatus + ", endValue=" + endValue);
		JpaTransactionManager jtm = null;

		int status = 0;
		int jobType = 0;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			try {
				//???????????????ID????????????ID??????????????????????????????????????????
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(sessionId, jobunitId, jobId);

				//?????????????????????
				status = sessionJob.getStatus();

				if(sessionJob.hasSessionNode()){
					jobType = JobOperationJudgment.TYPE_JOB;
				} else{
					jobType = JobOperationJudgment.TYPE_JOBNET;
				}
			} catch (Exception e) {
				m_log.warn("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			//??????????????????????????????????????????????????????????????????????????????
			if(!JobOperationJudgment.judgment(control, jobType, status)){
				IllegalStateException e = new IllegalStateException("illegal status " + status);
				m_log.info("operationJob() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}

			if(control == OperationConstant.TYPE_START_AT_ONCE){
				//??????[??????]
				try {
					new OperateStartOfJob().startJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound | InvalidRole e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_007_JOB, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_START_SUSPEND){
				//??????[????????????]
				try {
					new OperateSuspendOfJob().releaseSuspendJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound | InvalidRole e) {
					String[] args = {sessionId, jobId};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_008_JOB, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_START_WAIT){
				//??????[????????????]
				try {
					new OperateWaitOfJob().releaseWaitJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound | InvalidRole e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_009_JOB, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_START_SKIP){
				//??????[??????????????????]
				try {
					new OperateSkipOfJob().releaseSkipJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound | InvalidRole e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_010_JOB, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_START_FORCE_RUN){
				//??????[????????????]
				try {
					new OperateForceRunOfJob().forceRunJob(sessionId, jobunitId, jobId);
				} catch (Throwable e) {
					String[] args = { sessionId, jobId };
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB,
							MessageConstant.MESSAGE_SYS_JOB_OPERATION_FAILED_FORCE_RUN, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_AT_ONCE){
				try {
					//??????[????????????]
					new OperateStopOfJob().stopJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound | HinemosUnknown e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_011_JOB, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_SUSPEND){
				try {
					//??????[??????]
					new OperateSuspendOfJob().suspendJob(sessionId, jobunitId, jobId);
				} catch (InvalidRole | JobInfoNotFound | HinemosUnknown e) {
					String[] args = {sessionId, jobId};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_012_JOB, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_WAIT){
				try {
					//??????[??????]
					new OperateWaitOfJob().waitJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound | InvalidRole e) {
					String[] args = {sessionId, jobId};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_012_JOB, args);
					throw e;
				}
			} else if(control == OperationConstant.TYPE_STOP_SKIP){
				//??????[????????????]
				try {
					new OperateSkipOfJob().skipJob(sessionId, jobunitId, jobId);
				} catch (JobInfoNotFound | InvalidRole e) {
					String[] args = {sessionId, jobId};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_013_JOB, args);
					throw e;
				} catch (NullPointerException e) {
					String[] args = {sessionId, jobId};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_013_JOB, args);
					m_log.warn("operationJob() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			} else if(control == OperationConstant.TYPE_STOP_MAINTENANCE){
				//??????[????????????]
				if(endValue == null){
					throw new NullPointerException();
				}
				try {
					new OperateMaintenanceOfJob().maintenanceJob(sessionId, jobunitId, jobId,
							StatusConstant.TYPE_MODIFIED, endStatus, endValue);
				} catch (JobInfoNotFound | HinemosUnknown | InvalidRole e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_015_JOB, args);
					throw e;
				} catch (NullPointerException e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_015_JOB, args);
					m_log.warn("operationJob() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			} else if(control == OperationConstant.TYPE_STOP_FORCE){
				//??????[??????]
				if(endValue == null){
					throw new NullPointerException();
				}
				try {
					new OperateForceStopOfJob().forceStopJob(sessionId, jobunitId, jobId, endStatus, endValue);
				} catch (JobInfoNotFound | HinemosUnknown | InvalidRole e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_018_JOB, args);
					throw e;
				} catch (NullPointerException e) {
					String[] args = {sessionId, jobId, null};
					AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.JOB, MessageConstant.MESSAGE_SYS_018_JOB, args);
					m_log.warn("operationJob() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown(e.getMessage(), e);
				}
			} else {
				m_log.warn("operationJob() : unknown control. " + control);
			}

			jtm.commit();
		} catch (HinemosUnknown | JobInfoNotFound | InvalidRole e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (IllegalStateException e) {
			if (jtm != null)
				jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("operationJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * ???????????????????????????????????????<BR>
	 * 
	 * @param info ??????????????????
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 * 
	 * @see com.clustercontrol.jobmanagement.factory.OperationJob#endNode(RunResultInfo)
	 */
	public boolean endNode(RunResultInfo info) throws HinemosUnknown, JobInfoNotFound, InvalidRole {
		m_log.trace("endNode() : sessionId=" + info.getSessionId() + ", jobId=" + info.getJobId() + ", facilityId=" + info.getFacilityId());
		JpaTransactionManager jtm = null;

		boolean result = false;

		ILock lock = getLock(info.getSessionId());
		try {
			lock.writeLock();
			try {
				jtm = new JpaTransactionManager();
				JobSessionNodeImpl nodeImpl = new JobSessionNodeImpl();
				// ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????begin(true)?????????
				try {
					jtm.begin(true);
				} catch (Exception e) {
					m_log.error("endNode() ", e);
				}
				result = nodeImpl.endNode(info);
				jtm.commit();
			} catch (JobInfoNotFound | HinemosUnknown | InvalidRole e) {
				if (jtm != null){
					jtm.rollback();
				}
				throw e;
			} catch (EntityExistsException e) {
				if (jtm != null)
					jtm.rollback();
				m_log.warn("endNode() : " + e.getMessage(), e);
				throw new HinemosUnknown(e.getMessage(), e);
			} catch (Exception e) {
				m_log.warn("endNode() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				if (jtm != null)
					jtm.rollback();
				throw new HinemosUnknown(e.getMessage(), e);
			} finally {
				if (jtm != null)
					jtm.close();
			}
		} finally {
			lock.writeUnlock();
		}
		return result;
	}


	/**
	 * ???????????????????????????????????????
	 * 
	 * @param runInstructionInfo ????????????
	 * @throws FacilityNotFound
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 */
	public void runMonitorJob(RunInstructionInfo runInstructionInfo) throws FacilityNotFound, MonitorNotFound, HinemosUnknown {
		m_log.debug("runMonitorJob()");

		MonitorRunResultInfo monitorRunResultInfo = null;
		JpaTransactionManager jtm = null;
		MonitorInfo monitorInfo = null;
		RunMonitor runMonitor = null;
		JobInfoEntity jobInfoEntity = null; 

		// null????????????
		if (runInstructionInfo == null) {
			HinemosUnknown e = new HinemosUnknown("runInstructionInfo is null.");
			m_log.warn("runMonitorJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		try {
			// ??????????????????????????????
			jtm = new JpaTransactionManager();
			jtm.begin();

			// ????????????????????????
			JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(
					runInstructionInfo.getSessionId(), 
					runInstructionInfo.getJobunitId(), 
					runInstructionInfo.getJobId());
			jobInfoEntity = sessionJob.getJobInfoEntity();

			// ?????????????????????
			monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
					jobInfoEntity.getMonitorId(), sessionJob.getOwnerRoleId());

			switch (monitorInfo.getMonitorTypeId()) {
			case HinemosModuleConstant.MONITOR_AGENT:
				runMonitor = new RunMonitorAgent();
				break;
				
			case HinemosModuleConstant.MONITOR_HTTP_N:
				runMonitor = new RunMonitorHttp();
				break;
				
			case HinemosModuleConstant.MONITOR_HTTP_S:
				runMonitor = new RunMonitorHttpString();
				break;
				
			case HinemosModuleConstant.MONITOR_HTTP_SCENARIO:
				runMonitor = new RunMonitorHttpScenario();
				break;
				
			case HinemosModuleConstant.MONITOR_JMX:
				runMonitor = new RunMonitorJmx();
				break;
				
			case HinemosModuleConstant.MONITOR_PING:
				runMonitor = new RunMonitorPing();
				break;
				
			case HinemosModuleConstant.MONITOR_PORT:
				runMonitor = new RunMonitorPort();
				break;
				
			case HinemosModuleConstant.MONITOR_SQL_N:
				runMonitor = new RunMonitorSql();
				break;
				
			case HinemosModuleConstant.MONITOR_SQL_S:
				runMonitor = new RunMonitorSqlString();
				break;
				
			case HinemosModuleConstant.MONITOR_SNMP_S:
				runMonitor = new RunMonitorSnmpString();
				break;
				
			case HinemosModuleConstant.MONITOR_SNMP_N:
				runMonitor = new RunMonitorSnmp();
				break;
				
			case HinemosModuleConstant.MONITOR_WINSERVICE:
				runMonitor = new RunMonitorWinService();
				break;
				
			case HinemosModuleConstant.MONITOR_PERFORMANCE:
				runMonitor = new RunMonitorPerformance();
				break;

			case HinemosModuleConstant.MONITOR_PROCESS:
				runMonitor = new RunMonitorProcess();
				break;

			case HinemosModuleConstant.MONITOR_LOGCOUNT:
				runMonitor = new RunMonitorLogcount();
				break;

			case HinemosModuleConstant.MONITOR_INTEGRATION:
			case HinemosModuleConstant.MONITOR_CORRELATION:
			case HinemosModuleConstant.MONITOR_LOGFILE:
			case HinemosModuleConstant.MONITOR_BINARYFILE_BIN:
			case HinemosModuleConstant.MONITOR_PCAP_BIN:
			case HinemosModuleConstant.MONITOR_SNMPTRAP:
			case HinemosModuleConstant.MONITOR_SYSTEMLOG:
			case HinemosModuleConstant.MONITOR_CUSTOM_N:
			case HinemosModuleConstant.MONITOR_CUSTOM_S:
			case HinemosModuleConstant.MONITOR_CUSTOMTRAP_N:
			case HinemosModuleConstant.MONITOR_CUSTOMTRAP_S:
			case HinemosModuleConstant.MONITOR_WINEVENT:
				// ???????????????
				break;
			default:
				m_log.warn("runMonitorJob() : monitorTypeId is incorrect.");
			}

			if (runMonitor == null) {
				// ???????????????????????????
				String message = "runMonitorJob : runMonitor is null";
				m_log.warn(message);
				throw new HinemosUnknown(message);
			}

			// ????????????
			int convertFlg = ConvertValueConstant.TYPE_NO;
			if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMP_N)) {
				// SNMP??????
				convertFlg = com.clustercontrol.snmp.util.QueryUtil.
						getMonitorSnmpInfoPK(jobInfoEntity.getMonitorId()).getConvertFlg();
			} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_JMX)) {
				// JMX??????
				convertFlg = com.clustercontrol.jmx.util.QueryUtil.
						getMonitorJmxInfoPK(jobInfoEntity.getMonitorId()).getConvertFlg();
			}

			Object prevData = null;
			boolean isSecond = false;
			if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PERFORMANCE)
					|| ((monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMP_N) 
							|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_JMX))
							&& convertFlg == ConvertValueConstant.TYPE_DELTA)) {
				// ???????????????
				isSecond = (prevData = MonitorJobWorker.getPrevMonitorValue(runInstructionInfo)) != null;
			}
			if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PERFORMANCE)
					|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PROCESS)) {
				// ????????????
				monitorRunResultInfo = runMonitor.runMonitorAggregateByNode(
						monitorInfo.getMonitorTypeId(), 
						monitorInfo.getMonitorId(), 
						runInstructionInfo.getFacilityId(), 
						prevData);
				if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
					if (!isSecond) {
						// ???????????????
						// ???????????????
						MonitorJobWorker.addPrevMonitorValue(runInstructionInfo, monitorRunResultInfo.getCurData());
						// ???????????????????????????
						MonitorJobWorker.updateSchedule(runInstructionInfo);
					} else {
						// 2???????????????
						// ?????????????????????
						if (monitorRunResultInfo != null) {
							MonitorJobWorker.endMonitorJob(runInstructionInfo, monitorInfo.getMonitorTypeId(), 
									monitorRunResultInfo.getMessageOrg(), "", RunStatusConstant.END,
									MonitorJobWorker.getReturnValue(runInstructionInfo, monitorRunResultInfo.getPriority()));
						} else {
							// ?????????????????????????????????
							MonitorJobWorker.endMonitorJob(runInstructionInfo, monitorInfo.getMonitorTypeId(), "", "second response is null.", 
									RunStatusConstant.ERROR, MonitorJobWorker.getReturnValue(runInstructionInfo, PriorityConstant.TYPE_UNKNOWN));
						}
						// ??????????????????????????????
						MonitorJobWorker.deleteSchedule(runInstructionInfo);
					}
				} else {
					// ?????????????????????
					MonitorJobWorker.endMonitorJob(runInstructionInfo, monitorInfo.getMonitorTypeId(),
							monitorRunResultInfo.getMessageOrg(), "", RunStatusConstant.END,
							MonitorJobWorker.getReturnValue(runInstructionInfo, monitorRunResultInfo.getPriority()));
				}
			} else {
				monitorRunResultInfo = runMonitor.runMonitor(
						monitorInfo.getMonitorTypeId(), 
						monitorInfo.getMonitorId(), 
						runInstructionInfo.getFacilityId(), 
						prevData);
				if ((monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMP_N) 
						|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_JMX))
						&& convertFlg == ConvertValueConstant.TYPE_DELTA) {
					if (!isSecond) {
						// ???????????????
						// ????????????null???????????????????????????????????????????????????
						if (monitorRunResultInfo.getCurData() == null) {
							MonitorJobWorker.endMonitorJob(runInstructionInfo, monitorInfo.getMonitorTypeId(),
									monitorRunResultInfo.getMessageOrg(), "", RunStatusConstant.ERROR,
									MonitorJobWorker.getReturnValue(runInstructionInfo, monitorRunResultInfo.getPriority()));
						} else {
							// ???????????????
							MonitorJobWorker.addPrevMonitorValue(runInstructionInfo, monitorRunResultInfo.getCurData());
							// ???????????????????????????
							MonitorJobWorker.updateSchedule(runInstructionInfo);
						}
					} else {
						// 2???????????????
						// ?????????????????????
						if (monitorRunResultInfo != null) {
							MonitorJobWorker.endMonitorJob(runInstructionInfo, monitorInfo.getMonitorTypeId(),
									monitorRunResultInfo.getMessageOrg(), "", RunStatusConstant.END,
									MonitorJobWorker.getReturnValue(runInstructionInfo, monitorRunResultInfo.getPriority()));
						} else {
							// ?????????????????????????????????
							MonitorJobWorker.endMonitorJob(runInstructionInfo, monitorInfo.getMonitorTypeId(), "", "second response is null.", 
									RunStatusConstant.ERROR, MonitorJobWorker.getReturnValue(runInstructionInfo, PriorityConstant.TYPE_UNKNOWN));
						}
						// ??????????????????????????????
						MonitorJobWorker.deleteSchedule(runInstructionInfo);
					}
				} else {
					// ?????????????????????
					MonitorJobWorker.endMonitorJob(runInstructionInfo, monitorInfo.getMonitorTypeId(),
							monitorRunResultInfo.getMessageOrg(), "", RunStatusConstant.END, 
							MonitorJobWorker.getReturnValue(runInstructionInfo, monitorRunResultInfo.getPriority()));
				}
			}
			jtm.commit();
		}catch(FacilityNotFound | MonitorNotFound | HinemosUnknown e){
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		}catch(Exception e){
			m_log.warn("runMonitorJob() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 
	 * facilityID??????????????????????????????????????????????????????????????????
	 * 
	 * @param monitorTypeId
	 * @param facilityId
	 * @return ???????????????????????????????????????
	 * @throws HinemosUnknown
	 * @throws MonitorNotFound
	 * 
	 */
	public HashMap<RunInstructionInfo, MonitorInfo> getMonitorJobMap (
			String monitorTypeId, String facilityId) throws HinemosUnknown {
		HashMap<RunInstructionInfo, MonitorInfo> ret = new HashMap<>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			Map<RunInstructionInfo, MonitorInfo> monitorJobMap 
				= MonitorJobWorker.getMonitorJobMap(monitorTypeId);
			for (Map.Entry<RunInstructionInfo, MonitorInfo> entry : monitorJobMap.entrySet()) {
				// ??????????????????????????????????????????????????????ID??????
				JobSessionJobEntity sessionJob = QueryUtil.getJobSessionJobPK(
						entry.getKey().getSessionId(), 
						entry.getKey().getJobunitId(), 
						entry.getKey().getJobId());
				// ?????????????????????????????????ID?????????????????????????????????????????????
				ArrayList<String> facilityIdList
					= new RepositoryControllerBean().getExecTargetFacilityIdList(
							entry.getKey().getFacilityId(), sessionJob.getOwnerRoleId());
				if (facilityIdList != null && facilityIdList.contains(facilityId)) {
					ret.put(entry.getKey(), entry.getValue());
				}
			}
			
			jtm.commit();
		} catch (HinemosUnknown e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("getMonitorJobMap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}

		return ret;
	}

	/**
	 * <?????????> ?????????????????????Agent?????????????????????????????????????????????
	 * <?????????> ??????????????????????????????Agent?????????????????????????????????????????????????????????????????????
	 * ?????????????????????????????????????????????
	 * 
	 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * @param requestedFacilityId ?????????????????????????????????????????????facilityId
	 * @return ?????????????????????????????????
	 * @throws CustomInvalid ????????????????????????????????????????????????????????????
	 * @throws InvalidRole
	 * @throws HinemosUnknown ??????????????????????????????????????????
	 * 
	 */
	public ArrayList<CommandExecuteDTO> getCommandExecuteDTOForMonitorJob(String requestedFacilityId) throws CustomInvalid, InvalidRole, HinemosUnknown {
		JpaTransactionManager jtm = null;
		ArrayList<CommandExecuteDTO> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectCustom selector = new SelectCustom();
			list = selector.getCommandExecuteDTOForMonitorJob(requestedFacilityId);
			jtm.commit();
		} catch (CustomInvalid | InvalidRole | HinemosUnknown e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.warn("getCommandExecuteDTOForMonitorJob() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return list;
	}

}
