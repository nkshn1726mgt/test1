/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobParameterInfo;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.MonitorJobInfo;
import com.clustercontrol.jobmanagement.model.JobCommandParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobEnvVariableMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobNextJobOrderMstEntity;
import com.clustercontrol.jobmanagement.model.JobParamMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartJobMstEntity;
import com.clustercontrol.jobmanagement.model.JobStartParamMstEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.util.HinemosTime;

/**
 * ????????????????????????????????????????????????
 *
 * @version 4.1.0
 * @since 1.0.0
 */
public class FullJob {
	/** ????????????????????????????????? */
	private static Log m_log = LogFactory.getLog( FullJob.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(FullJob.class.getName());
		
		try {
			init();
		} catch (Throwable t) {
			m_log.error("FullJob initialisation error. " + t.getMessage(), t);
		}
	}
	
	public static void init() {
		try {
			_lock.writeLock();
			
			Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
			if (jobMstCache == null) {	// not null if clustered
				initJobMstCache();
			}
		} finally {
			_lock.writeUnlock();
		}
		
		try {
			_lock.writeLock();
			
			Map<String, Map<String, JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache == null) {	// not null if clustered
				initJobInfoCache();
			}
		} finally {
			_lock.writeUnlock();
		}
	}
	
	public static void initJobMstCache() {
		long startTime = System.currentTimeMillis();
		HashMap<String, Map<String,JobMstEntity>> jobMstCache = new HashMap<String, Map<String,JobMstEntity>>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			EntityManager em = jtm.getEntityManager();
			em.clear();
			List<JobMstEntity> jobunits = ((HinemosEntityManager)em).createNamedQuery("JobMstEntity.findByParentJobunitIdAndJobId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
					.setParameter("parentJobunitId", CreateJobSession.TOP_JOBUNIT_ID)
					.setParameter("parentJobId", CreateJobSession.TOP_JOB_ID)
					.getResultList();
			
			for(JobMstEntity jobunit : jobunits) {
				String jobunitId = jobunit.getId().getJobunitId();
				List<JobMstEntity> jobs =
						((HinemosEntityManager)em).createNamedQuery("JobMstEntity.findByJobunitId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				Map<String, JobMstEntity> jobunitMap = new HashMap<String, JobMstEntity>();
				for(JobMstEntity job : jobs) {
					String jobId = job.getId().getJobId();
					jobunitMap.put(jobId, job);
				}
				jobMstCache.put(jobunitId, jobunitMap);
			}
			m_log.info("init jobMstCache " + (System.currentTimeMillis() - startTime) + "ms. size=" + jobMstCache.size());
			for(Map.Entry<String, Map<String, JobMstEntity>> entry : jobMstCache.entrySet()) {
				m_log.info("jobMstCache key(jobunitId)=" + entry.getKey() + " size=" + entry.getValue().size());
			}
			
			storeJobMstCache(jobMstCache);
		} finally {
			if(jtm != null) {
				jtm.close();
			}
		}
	}
	
	public static void initJobInfoCache() {
		long startTime = System.currentTimeMillis();
		HashMap<String, Map<String,JobInfo>> jobInfoCache = new HashMap<String, Map<String,JobInfo>>();
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			EntityManager em = jtm.getEntityManager();
			em.clear();
			
			List<JobParamMstEntity> paramList = ((HinemosEntityManager)em).createNamedQuery("JobParamMstEntity.findAll", 
					JobParamMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobStartJobMstEntity> startJobList = ((HinemosEntityManager)em).createNamedQuery("JobStartJobMstEntity.findAll", 
					JobStartJobMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobCommandParamMstEntity> commandParamList = ((HinemosEntityManager)em).createNamedQuery("JobCommandParamMstEntity.findAll", 
					JobCommandParamMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobEnvVariableMstEntity> envVariableList = ((HinemosEntityManager)em).createNamedQuery("JobEnvVariableMstEntity.findAll", 
					JobEnvVariableMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobStartParamMstEntity> startParamList = ((HinemosEntityManager)em).createNamedQuery("JobStartParamMstEntity.findAll", 
					JobStartParamMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			List<JobNextJobOrderMstEntity> nextJobOrderList = ((HinemosEntityManager)em).createNamedQuery("JobNextJobOrderMstEntity.findAll", 
					JobNextJobOrderMstEntity.class, ObjectPrivilegeMode.NONE).getResultList();
			
			// ??????????????????????????????????????? <jobunitId, <jobId, List<JobParamMstEntity>>>
			Map<String, Map<String, List<JobParamMstEntity>>> paramMap = new HashMap<String, Map<String, List<JobParamMstEntity>>>();
			// ???????????????????????????????????? <jobunitId, <jobId, List<JobStartJobMstEntity>>>
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobMap = new HashMap<String, Map<String, List<JobStartJobMstEntity>>>();
			// ????????????????????????????????????(?????????????????????????????????) <targetJobunitId, <targetJobId, List<JobStartJobMstEntity>>>
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobTargetJobMap = new HashMap<String, Map<String, List<JobStartJobMstEntity>>>();
			// ????????????????????????????????????????????????????????? <jobunitId, <jobId, List<JobCommandParamMstEntity>>>
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap = new HashMap<String, Map<String, List<JobCommandParamMstEntity>>>();
			// ???????????????????????????????????? <jobunitId, <jobId, List<JobEnvVariableMstEntity>>>
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap = new HashMap<String, Map<String, List<JobEnvVariableMstEntity>>>();
			// ????????????(???????????????)???????????????????????? <jobunitId, <jobId, List<JobStartParamMstEntity>>>
			Map<String, Map<String, List<JobStartParamMstEntity>>> startParamMap = new HashMap<String, Map<String, List<JobStartParamMstEntity>>>();
			// ??????????????????????????????????????????????????? <jobunitId, <jobId, List<JobNextJobOrderMstEntity>>>
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap = new HashMap<String, Map<String, List<JobNextJobOrderMstEntity>>>();
			// ?????????????????????????????????????????? <notifyGroupId, List<NotifyRelationInfo>>
			Map<String, List<NotifyRelationInfo>> notifyRelMap = new HashMap<String, List<NotifyRelationInfo>>();
			
			// ?????????????????????????????????
			createParamMap(paramList, paramMap);
			
			// ??????????????????????????????
			createStartJobMap(startJobList, startJobMap);
			
			// ??????????????????????????????(?????????????????????????????????)
			createStartJobTargetJobMap(startJobList, startJobTargetJobMap);
			
			// ???????????????????????????????????????????????????
			createCommandParamMap(commandParamList, commandParamMap);
			
			// ??????????????????????????????
			createEnvVariableMap(envVariableList, envVariableMap);
			
			// ????????????(???????????????)??????????????????
			createStartParamMap(startParamList, startParamMap);
			
			// ?????????????????????????????????????????????
			createNextJobOrderMap(nextJobOrderList, nextJobOrderMap);
			
			// ????????????????????????????????????
			createNotifyRelationMap(notifyRelMap, em);
			
			Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
			
			for(Map.Entry<String, Map<String, JobMstEntity>> jobunitEntry : jobMstCache.entrySet()) {
				String jobunitId = jobunitEntry.getKey();
				Map<String, JobInfo> jobunitMap = new HashMap<String, JobInfo>();
				for(Map.Entry<String, JobMstEntity> jobEntry : jobunitEntry.getValue().entrySet()) {
					String jobId = jobEntry.getKey();
					try {
						jobunitMap.put(jobId, createJobInfo(jobEntry.getValue(), paramMap, startJobMap, startJobTargetJobMap, commandParamMap, envVariableMap, startParamMap, nextJobOrderMap, notifyRelMap));
					} catch (InvalidRole | JobMasterNotFound | HinemosUnknown e) {
						m_log.warn("failed initCache jobunitId=" + jobunitId + " jobId=" + jobId + ". " 
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
					}
				}
				jobInfoCache.put(jobunitId, jobunitMap);
			}
		
			m_log.info("init jobInfoCache " + (System.currentTimeMillis() - startTime) + "ms. size=" + jobInfoCache.size());
			for(String jobunitId : jobMstCache.keySet()) {
				m_log.info("jobInfoCache key(jobunitId)=" + jobunitId + " size=" + jobInfoCache.get(jobunitId).size());
			}
		
			storeJobInfoCache(jobInfoCache);
		} finally {
			if(jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * ????????????????????????????????????
	 * <jobunitId, <jobId, jobInfo>>
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobInfo>> getJobInfoCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_INFO);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobInfo>>)cache;
	}
	
	private static void storeJobInfoCache(HashMap<String, Map<String, JobInfo>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_INFO + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_INFO, newCache);
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobMstEntity>> getJobMstCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_MST);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_JOB_MST + " : " + cache);
		return cache == null ? null : (HashMap<String, Map<String, JobMstEntity>>)cache;
	}
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Map<String, JobMstEntity>> getJobMstCacheWithoutDebugLog() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_JOB_MST);
		return cache == null ? null : (HashMap<String, Map<String, JobMstEntity>>)cache;
	}
	
	private static void storeJobMstCache(HashMap<String, Map<String, JobMstEntity>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_JOB_MST + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_JOB_MST, newCache);
	}

	// deleteJobunit??????????????????
	public static void removeCache(String jobunitId) {
		m_log.debug("removeCache " + jobunitId);
		
		try {
			_lock.writeLock();
			
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache.remove(jobunitId) != null) {
				storeJobInfoCache(jobInfoCache);
			}
			
			HashMap<String, Map<String,JobMstEntity>> jobMstCache = getJobMstCache();
			if (jobMstCache.remove(jobunitId) != null) {
				storeJobMstCache(jobMstCache);
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	// registerJobunit?????????????????????
	public static void updateCache(String jobunitId) {
		m_log.debug("updateCache " + jobunitId);
		long start = HinemosTime.currentTimeMillis();
		
		try {
			_lock.writeLock();

			try (JpaTransactionManager jtm = new JpaTransactionManager()) {
				HinemosEntityManager em = jtm.getEntityManager();

				HashMap<String, Map<String,JobMstEntity>> jobMstCache = getJobMstCache();
				HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
				
				Map<String,JobMstEntity> jobunitMstMap = new HashMap<String,JobMstEntity>();
				Map<String,JobInfo> jobunitInfoMap = new HashMap<String,JobInfo>();
				em.clear();
				
				List<JobParamMstEntity> paramList = ((HinemosEntityManager)em).createNamedQuery("JobParamMstEntity.findByJobunitId", 
						JobParamMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobStartJobMstEntity> startJobList = ((HinemosEntityManager)em).createNamedQuery("JobStartJobMstEntity.findByJobunitId", 
						JobStartJobMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobCommandParamMstEntity> commandParamList = ((HinemosEntityManager)em).createNamedQuery("JobCommandParamMstEntity.findByJobunitId", 
						JobCommandParamMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobEnvVariableMstEntity> envVariableList = ((HinemosEntityManager)em).createNamedQuery("JobEnvVariableMstEntity.findByJobunitId", 
						JobEnvVariableMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobStartParamMstEntity> startParamList = ((HinemosEntityManager)em).createNamedQuery("JobStartParamMstEntity.findByJobunitId", 
						JobStartParamMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				List<JobNextJobOrderMstEntity> nextJobOrderList = ((HinemosEntityManager)em).createNamedQuery("JobNextJobOrderMstEntity.findByJobunitId", 
						JobNextJobOrderMstEntity.class, ObjectPrivilegeMode.NONE)
						.setParameter("jobunitId", jobunitId)
						.getResultList();
				
				// ??????????????????????????????????????? <jobunitId, <jobId, List<JobParamMstEntity>>>
				Map<String, Map<String, List<JobParamMstEntity>>> paramMap = new HashMap<String, Map<String, List<JobParamMstEntity>>>();
				// ???????????????????????????????????? <jobunitId, <jobId, List<JobStartJobMstEntity>>>
				Map<String, Map<String, List<JobStartJobMstEntity>>> startJobMap = new HashMap<String, Map<String, List<JobStartJobMstEntity>>>();
				// ????????????????????????????????????(?????????????????????????????????) <targetJobunitId, <targetJobId, List<JobStartJobMstEntity>>>
				Map<String, Map<String, List<JobStartJobMstEntity>>> startJobTargetJobMap = new HashMap<String, Map<String, List<JobStartJobMstEntity>>>();
				// ????????????????????????????????????????????????????????? <jobunitId, <jobId, List<JobCommandParamMstEntity>>>
				Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap = new HashMap<String, Map<String, List<JobCommandParamMstEntity>>>();
				// ???????????????????????????????????? <jobunitId, <jobId, List<JobEnvVariableMstEntity>>>
				Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap = new HashMap<String, Map<String, List<JobEnvVariableMstEntity>>>();
				// ????????????(???????????????)???????????????????????? <jobunitId, <jobId, List<JobStartParamMstEntity>>>
				Map<String, Map<String, List<JobStartParamMstEntity>>> startParamMap = new HashMap<String, Map<String, List<JobStartParamMstEntity>>>();
				// ??????????????????????????????????????????????????? <jobunitId, <jobId, List<JobNextJobOrderMstEntity>>>
				Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap = new HashMap<String, Map<String, List<JobNextJobOrderMstEntity>>>();
				// ?????????????????????????????????????????? <notifyGroupId, List<NotifyRelationInfo>>
				Map<String, List<NotifyRelationInfo>> notifyRelMap = new HashMap<String, List<NotifyRelationInfo>>();
				
				// ?????????????????????????????????
				createParamMap(paramList, paramMap);
				
				// ??????????????????????????????
				createStartJobMap(startJobList, startJobMap);
				
				// ??????????????????????????????(?????????????????????????????????)
				createStartJobTargetJobMap(startJobList, startJobTargetJobMap);
				
				// ???????????????????????????????????????????????????
				createCommandParamMap(commandParamList, commandParamMap);
				
				// ??????????????????????????????
				createEnvVariableMap(envVariableList, envVariableMap);
				
				// ????????????(???????????????)??????????????????
				createStartParamMap(startParamList, startParamMap);
				
				// ?????????????????????????????????????????????
				createNextJobOrderMap(nextJobOrderList, nextJobOrderMap);
				
				// ????????????????????????????????????
				createNotifyRelationMap(notifyRelMap, em.getEntityManager());
				
				List<JobMstEntity> jobs = QueryUtil.getJobMstEnityFindByJobunitId(jobunitId);
				for(JobMstEntity job : jobs) {
					String jobId = job.getId().getJobId();
					try {
						jobunitMstMap.put(jobId, job);
						jobunitInfoMap.put(jobId, createJobInfo(job, paramMap, startJobMap, startJobTargetJobMap, commandParamMap, envVariableMap, startParamMap, nextJobOrderMap, notifyRelMap));
					} catch (InvalidRole | JobMasterNotFound | HinemosUnknown e) {
						m_log.warn("failed initCache jobunitId=" + jobunitId + " jobId=" + jobId + ". " 
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
					}
				}
				jobMstCache.put(jobunitId, jobunitMstMap);
				jobInfoCache.put(jobunitId, jobunitInfoMap);
				
				storeJobMstCache(jobMstCache);
				storeJobInfoCache(jobInfoCache);
			}
		} finally {
			_lock.writeUnlock();
		}
		m_log.info("updateCache " + (HinemosTime.currentTimeMillis() - start) + "ms");
	}

	/**
	* ????????????????????????????????????????????????(????????????)????????????????????????
	*/
	public static void updateScopesInCache() {
		long start_time = System.currentTimeMillis();

		_lock.writeLock();
		try {
			HashMap<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			if (jobInfoCache == null) {
				m_log.info("updateScopesInCache() : JobInfoCache is null.");
				return;
			}
			for (Map<String, JobInfo> cacheEnrty : jobInfoCache.values()) {
				for (JobInfo job : cacheEnrty.values()) {
					String facilityId = "";	 // for logging
					switch (job.getType()) {
					case JobConstant.TYPE_JOB:
						facilityId = job.getCommand().getFacilityID();
						job.getCommand().setScope(FacilitySelector.getNodeScopePath(null, facilityId));
						break;
					case JobConstant.TYPE_MONITORJOB:
						facilityId = job.getMonitor().getFacilityID();
						job.getMonitor().setScope(FacilitySelector.getNodeScopePath(null,facilityId));
						break;
					case JobConstant.TYPE_FILEJOB:
						facilityId = job.getFile().getSrcFacilityID();
						job.getFile().setSrcScope(FacilitySelector.getNodeScopePath(null,facilityId));
						facilityId = job.getFile().getDestFacilityID();
						job.getFile().setDestScope(FacilitySelector.getNodeScopePath(null,facilityId));
						break;
					default:
						/* NOP */
						break;
					}
				}
			}

		} finally {
			_lock.writeUnlock();
		}

		m_log.info("updateScopesInCache() : " + (System.currentTimeMillis() - start_time) + "ms.");
	}

	/**
	 * ???????????????{@link com.clustercontrol.jobmanagement.bean.JobInfo}?????????????????????<BR>
	 * ??????????????????????????????????????????????????????????????????
	 *
	 * @param job ??????????????????
	 * @param treeOnly treeOnly true=??????????????????????????????, false=?????????????????????
	 * @return ???????????????
	 * @throws HinemosUnknown
	 * @throws JobMasterNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 */
	public static JobInfo getJobFull(JobInfo jobInfo) throws HinemosUnknown, JobMasterNotFound, UserNotFound, InvalidRole {
		// TODO: UserNotFound ????????????????????????????????????
		// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		m_log.debug("createJobData() : " + jobInfo.getJobunitId() + ", " + jobInfo.getId() + "," + jobInfo.isPropertyFull());
		if (jobInfo.isPropertyFull()) {
			return jobInfo;
		}

		String jobunitId = jobInfo.getJobunitId();
		String jobId = jobInfo.getId();
		
		
		try {
			_lock.readLock();
			
			Map<String, Map<String,JobInfo>> jobInfoCache = getJobInfoCache();
			Map<String, JobInfo> jobInfoUnitCache = jobInfoCache.get(jobunitId);
			if (jobInfoUnitCache != null) {
				JobInfo ret = jobInfoUnitCache.get(jobId);
				if (ret != null) {
					m_log.debug("cache hit " + jobunitId + "," + jobId + ", hit=" + jobInfoUnitCache.size());
					return ret;
				}
			} else {
				m_log.debug("cache didn't hit " + jobunitId + "," + jobId);
			}
		} finally {
			_lock.readUnlock();
		}
		
		m_log.debug("createJobData() : " + jobunitId + ", " + jobId);
		JobMstEntity jobMstEntity = null;
		try {
			_lock.readLock();
			jobMstEntity = getJobMstEntityFromLocal(jobunitId, jobId);
			if (jobMstEntity == null) {
				jobMstEntity = QueryUtil.getJobMstPK(jobunitId, jobId);
			}
		} finally {
			_lock.readUnlock();
		}
		
		jobInfo = createJobInfo(jobMstEntity, null, null, null, null, null, null, null, null);
		
		return jobInfo;
	}

	/**
	 * ??????????????????????????????????????????????????????
	 * @param jobunitId
	 * @param jobId
	 * @return
	 */
	private static JobMstEntity getJobMstEntityFromLocal(String jobunitId,
			String jobId) {
		Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCache();
		Map<String, JobMstEntity> jobMstUnitCache = jobMstCache.get(jobunitId);
		if (jobMstUnitCache == null) {
			return null;
		}
		return jobMstUnitCache.get(jobId);
	}

	/**
	 * jobMstEntity????????????????????????Job?????????????????????????????????jobInfo??????????????????
	 * @param jobInfo
	 * @param jobMstEntity
	 * @param paramMap
	 */
	private static void setJobParam(JobInfo jobInfo, JobMstEntity jobMstEntity, Map<String, Map<String, List<JobParamMstEntity>>> paramMap) {
		ArrayList<JobParameterInfo> paramList = new ArrayList<JobParameterInfo>();
		
		Collection<JobParamMstEntity> params = null;
		if(paramMap == null) {
			m_log.debug("params get Entities");
			params = jobMstEntity.getJobParamMstEntities();
		} else {
			m_log.debug("params get map");
			if(paramMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				params = paramMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
		if(params != null){
			for (JobParamMstEntity param : params) {
				JobParameterInfo paramInfo = new JobParameterInfo();
				paramInfo.setParamId(param.getId().getParamId());
				paramInfo.setType(param.getParamType());
				paramInfo.setDescription(param.getDescription());
				paramInfo.setValue(param.getValue());
				paramList.add(paramInfo);
			}
			/*
			 * ???????????????
			 */
			Collections.sort(paramList);
		}
		jobInfo.setParam(paramList);
	}

	/**
	 * jobMstEntity????????????????????????JobNextJobOrder?????????????????????????????????jobInfo??????????????????
	 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * ???????????????????????????????????????????????????????????????ID????????????????????????????????????????????????????????????
	 * @param jobInfo
	 * @param jobMstEntit
	 * @param startJobTargetJobMap
	 * @param nextJobOrderMap
	 */
	private static void setJobNextJobOrder(JobInfo jobInfo, JobMstEntity jobMstEntity, 
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobTargetJobMap,
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			ArrayList<JobNextJobOrderInfo> nextJobOrderList = new ArrayList<JobNextJobOrderInfo>();	
			List<JobNextJobOrderMstEntity> orderMstList = null;
			if(nextJobOrderMap == null) {
				m_log.debug("orderMstList get Entities");
				orderMstList = jobMstEntity.getJobNextJobOrderMstEntities();
			} else {
				m_log.debug("orderMstList get map");
				if(nextJobOrderMap.get(jobMstEntity.getId().getJobunitId()) != null) {
					orderMstList = nextJobOrderMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
				}
			}
			
			if(orderMstList != null){
				//????????????????????????
				//nextJobOrderList??????????????????????????????
				orderMstList.sort(Comparator.comparing(orderMst -> orderMst.getOrder()));
				for (JobNextJobOrderMstEntity orderMst : orderMstList) {
					JobNextJobOrderInfo orderInfo = new JobNextJobOrderInfo();
					orderInfo.setJobunitId(orderMst.getId().getJobunitId());
					orderInfo.setJobId(orderMst.getId().getJobId());
					orderInfo.setNextJobId(orderMst.getId().getNextJobId());
					nextJobOrderList.add(orderInfo);
				}
			}

			//????????????????????????????????????????????????????????????????????????
			//???????????????????????????????????????????????????????????????????????????????????????
			List<JobStartJobMstEntity> nextJobList = null;
			if(startJobTargetJobMap != null) {
				m_log.debug("nextJobList get map");
				if(startJobTargetJobMap.get(jobMstEntity.getId().getJobunitId()) != null) {
					nextJobList = startJobTargetJobMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
				}
			}
			
			if (nextJobList != null) {
				//???????????????????????????????????????????????????????????????
				nextJobList.removeIf(
					startJobMst ->
					startJobMst.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS ||
					startJobMst.getId().getTargetJobType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE 
				);

				//??????????????????????????????????????????????????????
				List<String> nextJobIdList = nextJobList
					.stream().map(startJobMst -> startJobMst.getId().getJobId())
					.collect(Collectors.toList());
				nextJobOrderList.removeIf(orderInfo -> !nextJobIdList.contains(orderInfo.getNextJobId()));

				//??????????????????????????????????????????????????????????????????????????????????????????
				//??????????????????????????????????????????????????????????????????????????????ID????????????????????????
				//?????????????????????????????????????????????ID
				List<String> orderExistsJobIdList = nextJobOrderList.stream()
					.map(nextJobOrder -> nextJobOrder.getNextJobId()).collect(Collectors.toList());
				//???????????????????????????
				nextJobList.stream()
				//???????????????????????????????????????????????????????????????
				.filter(startJobMst -> !orderExistsJobIdList.contains(startJobMst.getId().getJobId()))
				//?????????ID?????????????????????
				.sorted(Comparator.comparing(startJobMst -> startJobMst.getId().getJobId()))
				//?????????????????????????????????????????????????????????????????????????????????
				.forEach(startJobMst -> {
					JobNextJobOrderInfo nextJobOrder = new JobNextJobOrderInfo();
					nextJobOrder.setJobunitId(startJobMst.getId().getJobunitId());
					nextJobOrder.setJobId(startJobMst.getId().getTargetJobId());
					nextJobOrder.setNextJobId(startJobMst.getId().getJobId());
					nextJobOrderList.add(nextJobOrder);
				});
			}
			jobInfo.getWaitRule().setExclusiveBranchNextJobOrderList(nextJobOrderList);
		}
	}
	
	/**
	 * jobMstEntity????????????????????????Job???????????????jobInfo??????????????????
	 * @param jobInfo
	 * @param jobMstEntity
	 */
	private static void setJobEndStatus(JobInfo jobInfo, JobMstEntity jobMstEntity) {
		//?????????????????????
		ArrayList<JobEndStatusInfo> endList = new ArrayList<JobEndStatusInfo>();
		// ??????
		JobEndStatusInfo endInfoNormal = new JobEndStatusInfo();
		endInfoNormal.setType(EndStatusConstant.TYPE_NORMAL);
		endInfoNormal.setValue(jobMstEntity.getNormalEndValue());
		endInfoNormal.setStartRangeValue(jobMstEntity.getNormalEndValueFrom());
		endInfoNormal.setEndRangeValue(jobMstEntity.getNormalEndValueTo());
		endList.add(endInfoNormal);
		// ??????
		JobEndStatusInfo endInfoWarn = new JobEndStatusInfo();
		endInfoWarn.setType(EndStatusConstant.TYPE_WARNING);
		endInfoWarn.setValue(jobMstEntity.getWarnEndValue());
		endInfoWarn.setStartRangeValue(jobMstEntity.getWarnEndValueFrom());
		endInfoWarn.setEndRangeValue(jobMstEntity.getWarnEndValueTo());
		endList.add(endInfoWarn);
		// ??????
		JobEndStatusInfo endInfoAbnormal = new JobEndStatusInfo();
		endInfoAbnormal.setType(EndStatusConstant.TYPE_ABNORMAL);
		endInfoAbnormal.setValue(jobMstEntity.getAbnormalEndValue());
		endInfoAbnormal.setStartRangeValue(jobMstEntity.getAbnormalEndValueFrom());
		endInfoAbnormal.setEndRangeValue(jobMstEntity.getAbnormalEndValueTo());
		endList.add(endInfoAbnormal);
		
		jobInfo.setEndStatus(endList);
	}

	/**
	 * jobMstEntity????????????????????????Job??????????????????jobInfo??????????????????
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	private static void setJobNotifications(JobInfo jobInfo, JobMstEntity jobMstEntity, Map<String, List<NotifyRelationInfo>> notifyRelMap)
			throws InvalidRole, HinemosUnknown {
		jobInfo.setBeginPriority(jobMstEntity.getBeginPriority());
		jobInfo.setNormalPriority(jobMstEntity.getNormalPriority());
		jobInfo.setWarnPriority(jobMstEntity.getWarnPriority());
		jobInfo.setAbnormalPriority(jobMstEntity.getAbnormalPriority());

		//?????????????????????
		List<NotifyRelationInfo> nriList = null;
		if(notifyRelMap == null) {
			m_log.debug("nriList get database");
			nriList = new NotifyControllerBean().getNotifyRelation(jobMstEntity.getNotifyGroupId());
		} else {
			m_log.debug("nriList get map");
			nriList = notifyRelMap.get(jobMstEntity.getNotifyGroupId());
		}
		if (nriList != null) {
			Collections.sort(nriList);
			jobInfo.setNotifyRelationInfos(new ArrayList<NotifyRelationInfo>(nriList));
		}
	}

	/**
	 * jobMstEntity????????????????????????Job????????????????????????jobInfo??????????????????
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setJobFile(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		JobFileInfo fileInfo = new JobFileInfo();
		fileInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		fileInfo.setSrcFacilityID(jobMstEntity.getSrcFacilityId());
		fileInfo.setDestFacilityID(jobMstEntity.getDestFacilityId());
		fileInfo.setSrcFile(jobMstEntity.getSrcFile());
		fileInfo.setSrcWorkDir(jobMstEntity.getSrcWorkDir());
		fileInfo.setDestDirectory(jobMstEntity.getDestDirectory());
		fileInfo.setDestWorkDir(jobMstEntity.getDestWorkDir());
		fileInfo.setCompressionFlg(jobMstEntity.getCompressionFlg());
		fileInfo.setCheckFlg(jobMstEntity.getCheckFlg());
		fileInfo.setSpecifyUser(jobMstEntity.getSpecifyUser());
		fileInfo.setUser(jobMstEntity.getEffectiveUser());
		fileInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		fileInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		fileInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		fileInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		fileInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		fileInfo.setCommandRetryEndStatus(jobMstEntity.getCommandRetryEndStatus());
		//?????????????????????????????????
		fileInfo.setSrcScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getSrcFacilityId()));
		fileInfo.setDestScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getDestFacilityId()));
		jobInfo.setFile(fileInfo);
	}

	/**
	 * jobMstEntity????????????????????????Job????????????????????????jobInfo??????????????????
	 * @param jobInfo
	 * @param jobMstEntity
	 * @param envVariableMap
	 * @param commandParamMap
	 * @throws HinemosUnknown
	 */
	private static void setJobCommand(JobInfo jobInfo, JobMstEntity jobMstEntity, 
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap,
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap)
			throws HinemosUnknown {
		JobCommandInfo commandInfo = new JobCommandInfo();
		commandInfo.setFacilityID(jobMstEntity.getFacilityId());
		commandInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		commandInfo.setStartCommand(jobMstEntity.getStartCommand());
		commandInfo.setStopType(jobMstEntity.getStopType());
		commandInfo.setStopCommand(jobMstEntity.getStopCommand());
		commandInfo.setSpecifyUser(jobMstEntity.getSpecifyUser());
		commandInfo.setUser(jobMstEntity.getEffectiveUser());
		commandInfo.setMessageRetry(jobMstEntity.getMessageRetry());
		commandInfo.setMessageRetryEndFlg(jobMstEntity.getMessageRetryEndFlg());
		commandInfo.setMessageRetryEndValue(jobMstEntity.getMessageRetryEndValue());
		commandInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		commandInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		commandInfo.setCommandRetryEndStatus(jobMstEntity.getCommandRetryEndStatus());
		// ????????????????????????????????????
		ArrayList<JobCommandParam> jobCommandParamList = new ArrayList<>();
		List<JobCommandParamMstEntity> commandParams = null;
		if(commandParamMap == null) {
			m_log.debug("commandParams get Entities");
			commandParams = jobMstEntity.getJobCommandParamEntities();
		} else {
			m_log.debug("commandParams get map");
			if(commandParamMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				commandParams = commandParamMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
		if (commandParams != null && commandParams.size() > 0) {
			for (JobCommandParamMstEntity jobCommandParamEntity : commandParams) {
				if (jobCommandParamEntity != null) {
					JobCommandParam jobCommandParam = new JobCommandParam();
					jobCommandParam.setJobStandardOutputFlg(jobCommandParamEntity.getJobStandardOutputFlg());
					jobCommandParam.setParamId(jobCommandParamEntity.getId().getParamId());
					jobCommandParam.setValue(jobCommandParamEntity.getValue());
					jobCommandParamList.add(jobCommandParam);
				}
			}
		}
		commandInfo.setJobCommandParamList(jobCommandParamList);
		commandInfo.setManagerDistribution(jobMstEntity.getManagerDistribution());
		commandInfo.setScriptName(jobMstEntity.getScriptName());
		commandInfo.setScriptEncoding(jobMstEntity.getScriptEncoding());
		commandInfo.setScriptContent(jobMstEntity.getScriptContent());
		
		List<JobEnvVariableInfo> envVariableList = new ArrayList<JobEnvVariableInfo>();
		List<JobEnvVariableMstEntity> envVariables = null;
		if(envVariableMap == null) {
			m_log.debug("envVariables get Entities");
			envVariables = jobMstEntity.getJobEnvVariableMstEntities();
		} else {
			m_log.debug("envVariables get map");
			if(envVariableMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				envVariables = envVariableMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
		if(envVariables != null){
			for (JobEnvVariableMstEntity envVariable : envVariables) {
				JobEnvVariableInfo envVariableInfo = new JobEnvVariableInfo();
				envVariableInfo.setEnvVariableId(envVariable.getId().getEnvVariableId());
				envVariableInfo.setDescription(envVariable.getDescription());
				envVariableInfo.setValue(envVariable.getValue());
				envVariableList.add(envVariableInfo);
			}
			//???????????????
			Collections.sort(envVariableList);
		}
		commandInfo.setEnvVariableInfo(envVariableList);
		
		//?????????????????????????????????
		commandInfo.setScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
		jobInfo.setCommand(commandInfo);
	}

	/**
	 * jobMstEntity????????????????????????Job???????????????????????????jobInfo??????????????????
	 * @param jobInfo
	 * @param jobMstEntity
	 * @throws HinemosUnknown
	 */
	private static void setMonitorJob(JobInfo jobInfo, JobMstEntity jobMstEntity)
			throws HinemosUnknown {
		MonitorJobInfo monitorJobInfo = new MonitorJobInfo();
		monitorJobInfo.setFacilityID(jobMstEntity.getFacilityId());
		//?????????????????????????????????
		monitorJobInfo.setScope(FacilitySelector.getNodeScopePath(null, jobMstEntity.getFacilityId()));
		monitorJobInfo.setProcessingMethod(jobMstEntity.getProcessMode());
		monitorJobInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		monitorJobInfo.setMonitorId(jobMstEntity.getMonitorId());
		monitorJobInfo.setMonitorInfoEndValue(jobMstEntity.getMonitorInfoEndValue());
		monitorJobInfo.setMonitorWarnEndValue(jobMstEntity.getMonitorWarnEndValue());
		monitorJobInfo.setMonitorCriticalEndValue(jobMstEntity.getMonitorCriticalEndValue());
		monitorJobInfo.setMonitorUnknownEndValue(jobMstEntity.getMonitorUnknownEndValue());
		monitorJobInfo.setMonitorWaitTime(jobMstEntity.getMonitorWaitTime());
		monitorJobInfo.setMonitorWaitEndValue(jobMstEntity.getMonitorWaitEndValue());
		monitorJobInfo.setCommandRetry(jobMstEntity.getCommandRetry());
		monitorJobInfo.setCommandRetryFlg(jobMstEntity.getCommandRetryFlg());
		jobInfo.setMonitor(monitorJobInfo);
	}

	/**
	 * jobMstEntity????????????????????????Job??????????????????jobInfo??????????????????
	 * @param jobInfo
	 * @param jobMstEntity
	 * @param startJobMap
	 * @param startParamMap
	 * @throws JobMasterNotFound
	 * @throws InvalidRole
	 */
	private static void setJobWaitRule(JobInfo jobInfo, JobMstEntity jobMstEntity, 
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobMap,
			Map<String, Map<String, List<JobStartParamMstEntity>>> startParamMap)
			throws JobMasterNotFound, InvalidRole {
		//?????????????????????
		JobWaitRuleInfo waitRule = null;
		//?????????????????????
		waitRule = new JobWaitRuleInfo();
		//?????????????????????????????????????????????????????????????????????
		//?????????????????????
		if(jobMstEntity.getJobType() == JobConstant.TYPE_JOBNET ||
				jobMstEntity.getJobType() == JobConstant.TYPE_JOB||
				jobMstEntity.getJobType() == JobConstant.TYPE_FILEJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_APPROVALJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_REFERJOBNET ||
				jobMstEntity.getJobType() == JobConstant.TYPE_REFERJOB ||
				jobMstEntity.getJobType() == JobConstant.TYPE_MONITORJOB){
			waitRule.setSuspend(jobMstEntity.getSuspend());
			waitRule.setCondition(jobMstEntity.getConditionType());
			waitRule.setEndCondition(jobMstEntity.getUnmatchEndFlg());
			waitRule.setEndStatus(jobMstEntity.getUnmatchEndStatus());
			waitRule.setEndValue(jobMstEntity.getUnmatchEndValue());
			waitRule.setSkip(jobMstEntity.getSkip());
			waitRule.setSkipEndStatus(jobMstEntity.getSkipEndStatus());
			waitRule.setSkipEndValue(jobMstEntity.getSkipEndValue());
			waitRule.setExclusiveBranch(jobMstEntity.getExclusiveBranchFlg());
			waitRule.setExclusiveBranchEndStatus(jobMstEntity.getExclusiveBranchEndStatus());
			waitRule.setExclusiveBranchEndValue(jobMstEntity.getExclusiveBranchEndValue());
			waitRule.setCalendar(jobMstEntity.getCalendar());
			waitRule.setCalendarId(jobMstEntity.getCalendarId());
			waitRule.setCalendarEndStatus(jobMstEntity.getCalendarEndStatus());
			waitRule.setCalendarEndValue(jobMstEntity.getCalendarEndValue());
			waitRule.setJobRetryFlg(jobMstEntity.getJobRetryFlg());
			waitRule.setJobRetry(jobMstEntity.getJobRetry());
			waitRule.setJobRetryEndStatus(jobMstEntity.getJobRetryEndStatus());

			waitRule.setStart_delay(jobMstEntity.getStartDelay());
			waitRule.setStart_delay_session(jobMstEntity.getStartDelaySession());
			waitRule.setStart_delay_session_value(jobMstEntity.getStartDelaySessionValue());
			waitRule.setStart_delay_time(jobMstEntity.getStartDelayTime());
			if (jobMstEntity.getStartDelayTimeValue() != null) {
				waitRule.setStart_delay_time_value(jobMstEntity.getStartDelayTimeValue());
			}
			waitRule.setStart_delay_condition_type(jobMstEntity.getStartDelayConditionType());
			waitRule.setStart_delay_notify(jobMstEntity.getStartDelayNotify());
			waitRule.setStart_delay_notify_priority(jobMstEntity.getStartDelayNotifyPriority());
			waitRule.setStart_delay_operation(jobMstEntity.getStartDelayOperation());
			waitRule.setStart_delay_operation_type(jobMstEntity.getStartDelayOperationType());
			waitRule.setStart_delay_operation_end_status(jobMstEntity.getStartDelayOperationEndStatus());
			waitRule.setStart_delay_operation_end_value(jobMstEntity.getStartDelayOperationEndValue());

			waitRule.setEnd_delay(jobMstEntity.getEndDelay());
			waitRule.setEnd_delay_session(jobMstEntity.getEndDelaySession());
			waitRule.setEnd_delay_session_value(jobMstEntity.getEndDelaySessionValue());
			waitRule.setEnd_delay_job(jobMstEntity.getEndDelayJob());
			waitRule.setEnd_delay_job_value(jobMstEntity.getEndDelayJobValue());
			waitRule.setEnd_delay_time(jobMstEntity.getEndDelayTime());
			if (jobMstEntity.getEndDelayTimeValue() != null) {
				waitRule.setEnd_delay_time_value(jobMstEntity.getEndDelayTimeValue());
			}
			waitRule.setEnd_delay_condition_type(jobMstEntity.getEndDelayConditionType());
			waitRule.setEnd_delay_notify(jobMstEntity.getEndDelayNotify());
			waitRule.setEnd_delay_notify_priority(jobMstEntity.getEndDelayNotifyPriority());
			waitRule.setEnd_delay_operation(jobMstEntity.getEndDelayOperation());
			waitRule.setEnd_delay_operation_type(jobMstEntity.getEndDelayOperationType());
			waitRule.setEnd_delay_operation_end_status(jobMstEntity.getEndDelayOperationEndStatus());
			waitRule.setEnd_delay_operation_end_value(jobMstEntity.getEndDelayOperationEndValue());
			waitRule.setEnd_delay_change_mount(jobMstEntity.getEndDelayChangeMount());
			waitRule.setEnd_delay_change_mount_value(jobMstEntity.getEndDelayChangeMountValue());
			waitRule.setMultiplicityNotify(jobMstEntity.getMultiplicityNotify());
			waitRule.setMultiplicityNotifyPriority(jobMstEntity.getMultiplicityNotifyPriority());
			waitRule.setMultiplicityOperation(jobMstEntity.getMultiplicityOperation());
			waitRule.setMultiplicityEndValue(jobMstEntity.getMultiplicityEndValue());
			waitRule.setQueueFlg(jobMstEntity.getQueueFlg());
			waitRule.setQueueId(jobMstEntity.getQueueId());
		}

		//????????????????????????????????????
		Collection<JobStartJobMstEntity> startJobList = null;
		if(startJobMap == null) {
			m_log.debug("startJobList get Entities");
			startJobList = jobMstEntity.getJobStartJobMstEntities();
		} else {
			m_log.debug("startJobList get map");
			if(startJobMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				startJobList = startJobMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
		ArrayList<JobObjectInfo> objectList = new ArrayList<JobObjectInfo>();
		if(startJobList != null && startJobList.size() > 0){
			for (JobStartJobMstEntity startJob : startJobList){
				if(startJob != null){
					JobObjectInfo objectInfo = new JobObjectInfo();
					objectInfo.setJobId(startJob.getId().getTargetJobId());
					// ?????????????????????
					String jobName = "";
					//????????????????????????
					Map<String, Map<String, JobMstEntity>> jobMstCache = getJobMstCacheWithoutDebugLog();
					if (jobMstCache.get(startJob.getId().getTargetJobunitId()) != null
							&& jobMstCache.get(startJob.getId().getTargetJobunitId()).get(startJob.getId().getTargetJobId()) != null) {
						// ?????????????????????????????????????????????????????????????????????????????????????????????
						JobMstEntity targetJob = jobMstCache.get(startJob.getId().getTargetJobunitId()).get(startJob.getId().getTargetJobId());
						jobName = targetJob.getJobName();
					} else {
						// ???????????????SQL???????????????
						try {
							JobMstEntity targetJob= QueryUtil.getJobMstPK(startJob.getId().getTargetJobunitId(), startJob.getId().getTargetJobId());
							jobName = targetJob.getJobName();
						} catch(JobMasterNotFound e) {
							m_log.error("targetJob Not Found : " + e.getMessage());
							jobName = "";
						}
					}
					objectInfo.setJobName(jobName);
					objectInfo.setType(startJob.getId().getTargetJobType());
					objectInfo.setValue(startJob.getId().getTargetJobEndValue());
					objectInfo.setCrossSessionRange(startJob.getTargetJobCrossSessionRange());
					objectInfo.setDescription(startJob.getTargetJobDescription());
					m_log.debug("getTargetJobType = " + startJob.getId().getTargetJobType());
					m_log.debug("getTargetJobId = " + startJob.getId().getTargetJobId());
					m_log.debug("getTargetJobEndValue = " + startJob.getId().getTargetJobEndValue());
					m_log.debug("getTargetJobCrossSessionRange = " + startJob.getTargetJobCrossSessionRange());
					m_log.debug("getTargetJobDescription = " + startJob.getTargetJobDescription());
					objectList.add(objectInfo);
				}
			}
		}

		//?????????????????????????????????
		if (jobMstEntity.getStartTime() != null) {
			JobObjectInfo objectInfo = new JobObjectInfo();
			objectInfo.setType(JudgmentObjectConstant.TYPE_TIME);
			objectInfo.setTime(jobMstEntity.getStartTime());
			objectInfo.setDescription(jobMstEntity.getStartTimeDescription());
			m_log.debug("getType = " + JudgmentObjectConstant.TYPE_TIME);
			m_log.debug("getTime = " + jobMstEntity.getStartTime());
			m_log.debug("getStartTimeDescription= " + jobMstEntity.getStartTimeDescription());
			objectList.add(objectInfo);
		}
		m_log.debug("job.getStartMinute() = " + jobMstEntity.getStartMinute());
		//?????????????????????????????????????????????????????????????????????
		if (jobMstEntity.getStartMinute() != null) {
			JobObjectInfo objectInfo = new JobObjectInfo();
			objectInfo.setType(JudgmentObjectConstant.TYPE_START_MINUTE);
			objectInfo.setStartMinute(jobMstEntity.getStartMinute());
			objectInfo.setDescription(jobMstEntity.getStartMinuteDescription());
			m_log.debug("getType = " + JudgmentObjectConstant.TYPE_START_MINUTE);
			m_log.debug("getStartMinute = " + jobMstEntity.getStartMinute());
			m_log.debug("getStartMinuteDescription= " + jobMstEntity.getStartMinuteDescription());
			objectList.add(objectInfo);
		}

		// ??????????????????????????????????????????
		List<JobStartParamMstEntity> decisionList = null;
		if(startParamMap == null) {
			m_log.debug("decisionList get Entities");
			decisionList = jobMstEntity.getJobStartParamMstEntities();
		} else {
			m_log.debug("decisionList get map");
			if(startParamMap.get(jobMstEntity.getId().getJobunitId()) != null) {
				decisionList = startParamMap.get(jobMstEntity.getId().getJobunitId()).get(jobMstEntity.getId().getJobId());
			}
		}
		if (decisionList != null && decisionList.size() != 0) {
			for (JobStartParamMstEntity decision : decisionList) {
				if (decision != null) {
					JobObjectInfo objectInfo = new JobObjectInfo();
					objectInfo.setType(decision.getId().getTargetJobType());
					objectInfo.setDecisionValue01(decision.getId().getStartDecisionValue01());
					objectInfo.setDecisionCondition(decision.getId().getStartDecisionCondition());
					objectInfo.setDecisionValue02(decision.getId().getStartDecisionValue02());
					objectInfo.setDescription(decision.getDecisionDescription());
					m_log.debug("getTargetJobType = " + decision.getId().getTargetJobType());
					m_log.debug("getStartDecisionValue01 = " + decision.getId().getStartDecisionValue01());
					m_log.debug("getStartDecisionCondition = " + decision.getId().getStartDecisionCondition());
					m_log.debug("getStartDecisionValue02 = " + decision.getId().getStartDecisionValue02());
					m_log.debug("getDecisionDescription = " + decision.getDecisionDescription());
					objectList.add(objectInfo);
				}
			}
		}
		/*
		 * ???????????????
		 */
		Collections.sort(objectList);
		waitRule.setObject(objectList);
		jobInfo.setWaitRule(waitRule);
	}

	/**
	 * ?????????????????????????????????????????????fullProperty=true????????????
	 * @param job
	 * @throws JobMasterNotFound
	 * @throws NotifyNotFound
	 * @throws UserNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void setJobTreeFull (JobTreeItem job)
			throws JobMasterNotFound, NotifyNotFound, UserNotFound, InvalidRole, HinemosUnknown {
		JobInfo jobInfo = job.getData();
		if (!jobInfo.isPropertyFull()) {
			job.setData(getJobFull(jobInfo));
		}
		if (job.getChildren() == null) {
			return;
		}
		for (JobTreeItem childJob : job.getChildren()) {
			setJobTreeFull(childJob);
		}
	}

	/**
	 * ????????????????????????????????????ID???????????????????????????????????????
	 * ????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param findNotifyIds ?????????????????????ID(??????)
	 */
	public static void updateCacheForNotifyId(String[] findNotifyIds) {
		final long start = HinemosTime.currentTimeMillis();
		if (m_log.isDebugEnabled()) {
			m_log.debug("updateCacheForNotifyId() id=" + String.join(",", findNotifyIds));
		}
		//?????????????????????????????????????????????
		//  ?????????????????????????????? ??????ID????????????????????????????????????????????????????????????????????????
		//  NotifyRelationInfo?????????????????????????????????????????????
		//  ????????????????????????????????????????????????????????????ID??????????????????ID???????????????????????????????????????????????????????????????????????????
		final Set<String> updateJobUnitIdSet = getContainJobUnitFromCache(findNotifyIds);
		
		//??????????????????????????????????????????????????????
		for (String unitId : updateJobUnitIdSet) {
			updateCache(unitId);
		}
		m_log.info("updateCacheForNotifyId() " + (HinemosTime.currentTimeMillis() - start) + "ms");
	}
	
	// ????????????null??????????????????????????????????????????????????????????????????????????????????????????????????????????????????(???????????????)???????????????????????????????????????
	private static JobInfo createJobInfo(JobMstEntity jobMstEntity,
			Map<String, Map<String, List<JobParamMstEntity>>> paramMap,
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobMap,
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobTargetJobMap,
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap,
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap,
			Map<String, Map<String, List<JobStartParamMstEntity>>> startParamMap,
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap,
			Map<String, List<NotifyRelationInfo>> notifyRelMap) 
					throws InvalidRole, JobMasterNotFound, HinemosUnknown {
		JobInfo jobInfo = new JobInfo(jobMstEntity.getId().getJobunitId(), jobMstEntity.getId().getJobId(), jobMstEntity.getJobName(), jobMstEntity.getJobType());
		
		jobInfo.setDescription(jobMstEntity.getDescription());
		jobInfo.setIconId(jobMstEntity.getIconId());
		if (jobMstEntity.getJobType() == JobConstant.TYPE_JOBUNIT) {
			jobInfo.setOwnerRoleId(jobMstEntity.getOwnerRoleId());
		} else {
			//???????????????????????????????????????????????????ID???null?????????
			jobInfo.setOwnerRoleId(null);
		}
			jobInfo.setRegisteredModule(jobMstEntity.isRegisteredModule());

		if (jobMstEntity.getRegDate() != null) {
			jobInfo.setCreateTime(jobMstEntity.getRegDate());
		}
		if (jobMstEntity.getUpdateDate() != null) {
			jobInfo.setUpdateTime(jobMstEntity.getUpdateDate());
		}
		jobInfo.setCreateUser(jobMstEntity.getRegUser());
		jobInfo.setUpdateUser(jobMstEntity.getUpdateUser());

		jobInfo.setIconId(jobMstEntity.getIconId());

		setJobWaitRule(jobInfo, jobMstEntity, startJobMap, startParamMap);
		setJobNextJobOrder(jobInfo, jobMstEntity, startJobTargetJobMap, nextJobOrderMap);

		switch (jobMstEntity.getJobType()) {
		case JobConstant.TYPE_JOB:
			setJobCommand(jobInfo, jobMstEntity, envVariableMap, commandParamMap);
			break;
		case JobConstant.TYPE_FILEJOB:
			setJobFile(jobInfo, jobMstEntity);
			break;
		case JobConstant.TYPE_JOBUNIT:
			setJobParam(jobInfo, jobMstEntity, paramMap);
			break;
		case JobConstant.TYPE_REFERJOB:
		case JobConstant.TYPE_REFERJOBNET:
			jobInfo.setReferJobUnitId(jobMstEntity.getReferJobUnitId());
			jobInfo.setReferJobId(jobMstEntity.getReferJobId());
			jobInfo.setReferJobSelectType(jobMstEntity.getReferJobSelectType());
			break;
		case JobConstant.TYPE_APPROVALJOB:
			jobInfo.setApprovalReqRoleId(jobMstEntity.getApprovalReqRoleId());
			jobInfo.setApprovalReqUserId(jobMstEntity.getApprovalReqUserId());
			jobInfo.setApprovalReqSentence(jobMstEntity.getApprovalReqSentence());
			jobInfo.setApprovalReqMailTitle(jobMstEntity.getApprovalReqMailTitle());
			jobInfo.setApprovalReqMailBody(jobMstEntity.getApprovalReqMailBody());
			jobInfo.setUseApprovalReqSentence(jobMstEntity.isUseApprovalReqSentence());
			break;
		case JobConstant.TYPE_MONITORJOB:
			setMonitorJob(jobInfo, jobMstEntity);
			break;
		default:
			break;
		}
		if (jobInfo.getType() != JobConstant.TYPE_REFERJOB && jobInfo.getType() != JobConstant.TYPE_REFERJOBNET) {
			setJobNotifications(jobInfo, jobMstEntity, notifyRelMap);
			setJobEndStatus(jobInfo, jobMstEntity);
		}

		jobInfo.setPropertyFull(true);
		return jobInfo;
	}
	
	private static void createParamMap(List<JobParamMstEntity> paramList, 
			Map<String, Map<String, List<JobParamMstEntity>>> paramMap) {
		
		for(JobParamMstEntity param : paramList) {
			String jobunitId = param.getId().getJobunitId();
			String jobId = param.getId().getJobId();
			
			Map<String, List<JobParamMstEntity>> map = paramMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobParamMstEntity>>();
			}
			
			List<JobParamMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobParamMstEntity>();
			}
		
			list.add(param);
			map.put(jobId, list);
			paramMap.put(jobunitId, map);
		}
		m_log.debug("paramMap size=" + paramMap.size());
	}
	
	private static void createStartJobMap(List<JobStartJobMstEntity> startJobList, 
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobMap) {
		
		for(JobStartJobMstEntity startJob : startJobList) {
			String jobunitId = startJob.getId().getJobunitId();
			String jobId = startJob.getId().getJobId();
			
			Map<String, List<JobStartJobMstEntity>> map = startJobMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobStartJobMstEntity>>();
			}
			
			List<JobStartJobMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobStartJobMstEntity>();
			}
		
			list.add(startJob);
			map.put(jobId, list);
			startJobMap.put(jobunitId, map);
		}
		m_log.debug("startJobMap size=" + startJobMap.size());
	}
	
	private static void createStartJobTargetJobMap(List<JobStartJobMstEntity> startJobList, 
			Map<String, Map<String, List<JobStartJobMstEntity>>> startJobTargetJobMap) {
		
		for(JobStartJobMstEntity startJob : startJobList) {
			String targetJobunitId = startJob.getId().getTargetJobunitId();
			String targetJobId = startJob.getId().getTargetJobId();
			
			Map<String, List<JobStartJobMstEntity>> map = startJobTargetJobMap.get(targetJobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobStartJobMstEntity>>();
			}
			
			List<JobStartJobMstEntity> list = map.get(targetJobId);
			if(list == null) {
				list = new ArrayList<JobStartJobMstEntity>();
			}
		
			list.add(startJob);
			map.put(targetJobId, list);
			startJobTargetJobMap.put(targetJobunitId, map);
		}
		m_log.debug("startJobTargetJobMap size=" + startJobTargetJobMap.size());
	}

	private static void createCommandParamMap(List<JobCommandParamMstEntity> commandParamList, 
			Map<String, Map<String, List<JobCommandParamMstEntity>>> commandParamMap) {
		
		for(JobCommandParamMstEntity commandParam : commandParamList) {
			String jobunitId = commandParam.getId().getJobunitId();
			String jobId = commandParam.getId().getJobId();
			
			Map<String, List<JobCommandParamMstEntity>> map = commandParamMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobCommandParamMstEntity>>();
			}
			
			List<JobCommandParamMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobCommandParamMstEntity>();
			}
		
			list.add(commandParam);
			map.put(jobId, list);
			commandParamMap.put(jobunitId, map);
		}
		m_log.debug("commandParamMap size=" + commandParamMap.size());
	}

	private static void createEnvVariableMap(List<JobEnvVariableMstEntity> envVariableList, 
			Map<String, Map<String, List<JobEnvVariableMstEntity>>> envVariableMap) {
		
		for(JobEnvVariableMstEntity envVariable : envVariableList) {
			String jobunitId = envVariable.getId().getJobunitId();
			String jobId = envVariable.getId().getJobId();
			
			Map<String, List<JobEnvVariableMstEntity>> map = envVariableMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobEnvVariableMstEntity>>();
			}
			
			List<JobEnvVariableMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobEnvVariableMstEntity>();
			}
		
			list.add(envVariable);
			map.put(jobId, list);
			envVariableMap.put(jobunitId, map);
		}
		m_log.debug("envVariableMap size=" + envVariableMap.size());
	}

	private static void createStartParamMap(List<JobStartParamMstEntity> startParamList, 
			Map<String, Map<String, List<JobStartParamMstEntity>>> startParamMap) {
		
		for(JobStartParamMstEntity startParam : startParamList) {
			String jobunitId = startParam.getId().getJobunitId();
			String jobId = startParam.getId().getJobId();
			
			Map<String, List<JobStartParamMstEntity>> map = startParamMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobStartParamMstEntity>>();
			}
			
			List<JobStartParamMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobStartParamMstEntity>();
			}
		
			list.add(startParam);
			map.put(jobId, list);
			startParamMap.put(jobunitId, map);
		}
		m_log.debug("startParamMap size=" + startParamMap.size());
	}
	
	private static void createNextJobOrderMap(List<JobNextJobOrderMstEntity> nextJobOrderList,
			Map<String, Map<String, List<JobNextJobOrderMstEntity>>> nextJobOrderMap) {
		
		for(JobNextJobOrderMstEntity nextJobOrder : nextJobOrderList) {
			String jobunitId = nextJobOrder.getId().getJobunitId();
			String jobId = nextJobOrder.getId().getJobId();
			
			Map<String, List<JobNextJobOrderMstEntity>> map = nextJobOrderMap.get(jobunitId);
			if(map == null) {
				map = new HashMap<String, List<JobNextJobOrderMstEntity>>();
			}
			
			List<JobNextJobOrderMstEntity> list = map.get(jobId);
			if(list == null) {
				list = new ArrayList<JobNextJobOrderMstEntity>();
			}
		
			list.add(nextJobOrder);
			map.put(jobId, list);
			nextJobOrderMap.put(jobunitId, map);
		}
		m_log.debug("nextJobOrderMap size=" + nextJobOrderMap.size());
	}
	
	private static void createNotifyRelationMap(Map<String, List<NotifyRelationInfo>> notifyRelMap, EntityManager em) {
		List<NotifyRelationInfo> notifyRelList = com.clustercontrol.notify.util.QueryUtil.getNotifyRelationInfoJob();
		if (notifyRelList != null) {
			for(NotifyRelationInfo info : notifyRelList) {
				// EntityManager?????????????????????????????????
				em.detach(info);
				String notifyGroupId = info.getNotifyGroupId();
				
				List<NotifyRelationInfo> list = notifyRelMap.get(notifyGroupId);
				if(list == null) {
					list = new ArrayList<NotifyRelationInfo>();
				}
				list.add(info);
				notifyRelMap.put(notifyGroupId, list);
			}
		}
		m_log.debug("notifyRelMap size=" + notifyRelMap.size());
	}
	
	/**
	 * JobInfo????????????????????????????????????ID???????????????????????????????????????
	 * ??????????????????????????????????????????????????????????????????ID?????????
	 * 
	 * @param findNotifyIds ?????????????????????ID(??????)
	 * @return ????????????????????????ID(?????????)
	 */
	private static Set<String> getContainJobUnitFromCache(String[] findNotifyIds) {
		
		final List<String> notifyIdList = Arrays.asList(findNotifyIds);
		final Set<String> updateIDSet = new HashSet<String>();
		
		try {
			_lock.readLock();
			//?????????????????????????????????????????????????????????
			Map<String, Map<String, JobInfo>> jobInfoCache = getJobInfoCache();
			for (String keyUnitId : jobInfoCache.keySet()) {
				//??????????????????????????????????????????????????????????????????ID?????????????????????????????????
				Map<String, JobInfo> unitInfoMap = jobInfoCache.get(keyUnitId);
				boolean isNeedUpdate = false;
				if (m_log.isDebugEnabled()) {
					m_log.debug("getContainJobUnitFromCache() searchUnitId=" + keyUnitId);
				}
				for (JobInfo jobInfo : unitInfoMap.values()) {
					if (jobInfo == null || jobInfo.getNotifyRelationInfos() == null) {
						continue;
					}
					for (NotifyRelationInfo relationInfo : jobInfo.getNotifyRelationInfos()) {
						if (relationInfo == null || relationInfo.getNotifyId() == null) {
							continue;
						}
						if (m_log.isTraceEnabled()) {
							m_log.trace("getContainJobUnitFromCache() jobInfo.getId=" + jobInfo.getId() + ", relationInfo.getNotifyId=" + relationInfo.getNotifyId());
						}
						if (notifyIdList.contains(relationInfo.getNotifyId())) {
							//???????????????ID???????????????????????? ???????????????????????????????????????
							isNeedUpdate = true;
							break;
						}
					}
					if (isNeedUpdate) {
						updateIDSet.add(keyUnitId);
						break;
					}
				}
			}
		} finally {
			_lock.readUnlock();
		}
		return updateIDSet;
		
	}
	
}
