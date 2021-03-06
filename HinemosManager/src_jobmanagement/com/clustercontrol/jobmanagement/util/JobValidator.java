/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.ScheduleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.IconFileNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobCommandInfo;
import com.clustercontrol.jobmanagement.bean.JobCommandParam;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JobEndStatusInfo;
import com.clustercontrol.jobmanagement.bean.JobEnvVariableInfo;
import com.clustercontrol.jobmanagement.bean.JobFileCheck;
import com.clustercontrol.jobmanagement.bean.JobFileInfo;
import com.clustercontrol.jobmanagement.bean.JobInfo;
import com.clustercontrol.jobmanagement.bean.JobKick;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.bean.JobNextJobOrderInfo;
import com.clustercontrol.jobmanagement.bean.JobObjectInfo;
import com.clustercontrol.jobmanagement.bean.JobQueueConstant;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParam;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamDetail;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobSchedule;
import com.clustercontrol.jobmanagement.bean.JobTreeItem;
import com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo;
import com.clustercontrol.jobmanagement.bean.JobmapIconImage;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.MonitorJobInfo;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.factory.SelectJobmap;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.queue.JobQueueNotFoundException;
import com.clustercontrol.jobmanagement.session.JobControllerBean;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;

/**
 * ?????????????????????????????????????????????
 * 
 * @since 4.0
 */
public class JobValidator {
	private static Log m_log = LogFactory.getLog( JobValidator.class );

	// ????????????,????????????,??????????????????????????????????????????,?????????
	private static final long DATETIME_VALUE_MIN = -392399000L; //???-99:59:59?????????????????????
	private static final long DATETIME_VALUE_MAX = 3567599000L; //???999:59:59?????????????????????
	private static final String DATETIME_STRING_MIN = "-99:59:59"; //?????????????????????????????????????????????
	private static final String DATETIME_STRING_MAX = "999:59:59"; //?????????????????????????????????????????????
	
	/**
	 * ???????????????validate
	 * @param JobKick
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobKick(JobKick jobKick) throws InvalidSetting, HinemosUnknown, InvalidRole {
		String id = jobKick.getId();
		// jobkickId
		CommonValidator.validateId(MessageConstant.JOBKICK_ID.getMessage(), id, 64);
		// jobkickName
		CommonValidator.validateString(MessageConstant.JOBKICK_NAME.getMessage(), jobKick.getName(), true, 1, 64);
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobKick.getOwnerRoleId(), true, jobKick.getId(), HinemosModuleConstant.JOB_KICK);
		// jobid
		validateJobId(jobKick.getJobunitId(),jobKick.getJobId(), jobKick.getOwnerRoleId());

		if (jobKick.getType() != JobKickConstant.TYPE_MANUAL) {
			// calenderId
			CommonValidator.validateCalenderId(jobKick.getCalendarId(), false, jobKick.getOwnerRoleId());
		}

		// jobRuntimeParamList
		if (jobKick.getJobRuntimeParamList() != null) {
			for (JobRuntimeParam jobRuntimeParam : jobKick.getJobRuntimeParamList()) {
				// paramId
				CommonValidator.validateId(MessageConstant.JOBKICK_PARAM_ID.getMessage(), jobRuntimeParam.getParamId(), 64);
				//paramType
				if (jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_INPUT
						&& jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_RADIO
						&& jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_COMBO
						&& jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_FIXED) {
					InvalidSetting e = new InvalidSetting("unknown jobkick type");
					m_log.info("validateJobKick() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				// defaultValue
				if (jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_FIXED
						|| jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_RADIO) {
					CommonValidator.validateString(MessageConstant.JOBKICK_DEFAULT_VALUE.getMessage(), jobRuntimeParam.getValue(), true, 1, 1024);
				}
				// description
				CommonValidator.validateString(MessageConstant.JOBKICK_DESCRIPTION.getMessage(), jobRuntimeParam.getDescription(), true, 1, 256);
				// requiredFlg
				if (jobRuntimeParam.getRequiredFlg() == null) {
					InvalidSetting e = new InvalidSetting("required flag is null");
					m_log.info("validateJobKick() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				// jobRuntimeParamDetailList
				if (jobRuntimeParam.getJobRuntimeParamDetailList() == null
						|| jobRuntimeParam.getJobRuntimeParamDetailList().size() == 0) {
					if (jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_RADIO
							|| jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_COMBO) {
						InvalidSetting e = new InvalidSetting("select item is null");
						m_log.info("validateJobKick() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				} else {
					for (JobRuntimeParamDetail jobRuntimeParamDetail : jobRuntimeParam.getJobRuntimeParamDetailList()) {
						// paramValue
						CommonValidator.validateString(MessageConstant.JOBKICK_DETAIL_PARAM_VALUE.getMessage(), jobRuntimeParamDetail.getParamValue(), true, 1, 1024);
						// description
						CommonValidator.validateString(MessageConstant.JOBKICK_DETAIL_DESCRIPTION.getMessage(), jobRuntimeParamDetail.getDescription(), true, 1, 1024);
					}
				}
			}
		}
	}

	/**
	 * ??????????????????????????????validate
	 * 
	 * @param jobSchedule
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobSchedule(JobSchedule jobSchedule) throws InvalidSetting, HinemosUnknown, InvalidRole {

		// jobkick
		validateJobKick(jobSchedule);

		/**
		 * ????????????????????????
		 */
		//p??????q????????????????????????????????????
		if(jobSchedule.getScheduleType() == ScheduleConstant.TYPE_REPEAT){
			//???p??????????????????????????????
			Integer pMinute = jobSchedule.getFromXminutes();
			if (pMinute != null) {
				if (pMinute < 0 || 60 <= pMinute) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FROM_MIN.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// ???????????????
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FROM_MIN.getMessage());
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//???q??????????????????????????????
			Integer qMinute = jobSchedule.getEveryXminutes();
			if (qMinute != null) {
				if (qMinute <= 0 || 60 < qMinute || qMinute <= pMinute ||
						!(qMinute == 1 || qMinute == 2 || qMinute == 3 || qMinute == 5 || qMinute == 10 || qMinute == 15 ||
						qMinute == 20 || qMinute == 30 || qMinute == 60)) { 
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN_EACH.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// ???????????????
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN_EACH.getMessage());
				m_log.info("validateSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage() + "null");
				throw e;
			}
		}
		//?????????????????????
		else {
			//3?????????????????????????????????
			if (jobSchedule.getScheduleType() != ScheduleConstant.TYPE_DAY
					&& jobSchedule.getScheduleType() != ScheduleConstant.TYPE_WEEK) {
				InvalidSetting e = new InvalidSetting("unknown schedule type");
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//???????????????
			if (jobSchedule.getScheduleType() == ScheduleConstant.TYPE_WEEK) {
				if (jobSchedule.getWeek() == null ||
						jobSchedule.getWeek() < 0 || 7 < jobSchedule.getWeek()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_WEEK.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			/*
			 * ?????????????????????
			 * ???????????????
			 * ???*??????????????????00??? - ???48??????????????????
			 * ???*?????????null????????????DB??????????????????
			 */
			if (jobSchedule.getHour() != null) {
				if (jobSchedule.getHour() < 0 || 48 < jobSchedule.getHour()) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_HOUR.getMessage());
					m_log.info("validateJobSchedule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			//?????????????????????
			if (jobSchedule.getMinute() != null) {
				if (jobSchedule.getMinute() < 0 || 60 < jobSchedule.getMinute()) {
					String[] args = {"0","59"};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER.getMessage(args));
					m_log.info("validateJobSchedule()  "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else {
				// ???????????????
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MIN.getMessage());
				m_log.info("validateJobSchedule() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			//48:01???????????????????????????
			if (jobSchedule.getHour() != null && jobSchedule.getMinute() != null) {
				if (jobSchedule.getHour() == 48) {
					if (jobSchedule.getMinute() != 0) {
						String[] args = {"00:00","48:00"};
						InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER.getMessage(args));
						m_log.info("validateJobSchedule()  "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
			}
		}
	}

	/**
	 * ????????????[????????????????????????]???validate
	 * @param jobFileCheck
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobFileCheck(JobFileCheck jobFileCheck) throws InvalidSetting, HinemosUnknown, InvalidRole {
		// jobkick
		validateJobKick(jobFileCheck);

		// ??????????????????????????????ID???????????????
		if(jobFileCheck.getFacilityId() == null || "".equals(jobFileCheck.getFacilityId())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			//??????????????????ID?????????????????????????????????
			if(!SystemParameterConstant.isParam(
					jobFileCheck.getFacilityId(),
					SystemParameterConstant.FACILITY_ID)){
				try {
					FacilityTreeCache.validateFacilityId(jobFileCheck.getFacilityId(), jobFileCheck.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + jobFileCheck.getFacilityId()
							+ ", JobFileCheck  = " + jobFileCheck.getId());
					m_log.info("validateJobFileCheck() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobFileCheck() add job unknown error. FacilityId = " + jobFileCheck.getFacilityId()
							+ ", JobFileCheck  = " + jobFileCheck.getId()  + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + jobFileCheck.getFacilityId() + ", JobFileCheck  = " + jobFileCheck.getId(), e);
				}
			}
		}

		//??????????????????
		if(jobFileCheck.getDirectory() == null || jobFileCheck.getDirectory().equals("")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DIR_NAME.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		//???????????????
		if(jobFileCheck.getFileName() == null || jobFileCheck.getFileName().equals("")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FILE_NAME.getMessage());
			m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.DIRECTORY.getMessage(), jobFileCheck.getDirectory(), true, 1, 1024);
		CommonValidator.validateString(MessageConstant.FILE_NAME.getMessage(), jobFileCheck.getFileName(), true, 1, 64);
	}

	/**
	 * ?????????ID?????????????????????
	 * @param jobunitId
	 * @param jobId
	 * @param isFlag true:??????????????????????????????????????? false : ?????????
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateJobId (String jobunitId, String jobId,Boolean isFlag) throws InvalidSetting, InvalidRole {

		try {
			//???????????????????????????????????????????????????
			if (isFlag) {
				QueryUtil.getJobMstPK_NONE(new JobMstEntityPK(jobunitId, jobId));
			}
			//??????????????????
			else {
				QueryUtil.getJobMstPK(jobunitId, jobId);
			}
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateJobId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB.getMessage() +
					" Target job is not exist! jobunitId = " + jobunitId +
					", jobId = " + jobId);
			throw e1;
		}
	}

	/**
	 * ?????????ID?????????????????????(?????????????????????ID????????????????????????
	 * @param jobunitId
	 * @param jobId
	 * @param ownerRoleId
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateJobId (String jobunitId, String jobId, String ownerRoleId) throws InvalidSetting, InvalidRole {

		try {
			QueryUtil.getJobMstPK_OR(jobunitId, jobId, ownerRoleId);
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateJobId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB.getMessage() +
					" Target job is not exist! jobunitId = " + jobunitId +
					", jobId = " + jobId);
			throw e1;
		}
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????????????????
	 * INSERT, UPDATE, DELETE????????????????????????
	 * 
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws JobInfoNotFound
	 * @throws InvalidRole
	 */
	public static void validateJobMaster() throws InvalidSetting, HinemosUnknown, JobInfoNotFound, InvalidRole {

		m_log.debug("validateJobMaster()");

		// ?????????????????????
		m_log.debug("validateJobMaster() jobschedule check start");
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Collection<JobKickEntity> jobKickList =
					em.createNamedQuery("JobKickEntity.findAll",
							JobKickEntity.class, ObjectPrivilegeMode.NONE).getResultList();

			if (jobKickList == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobKickEntity.findAll");
				m_log.info("validateJobMaster() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				throw je;
			}
			for(JobKickEntity jobKick : jobKickList){
				String jobunitId = jobKick.getJobunitId();
				String jobId = jobKick.getJobId();

				m_log.debug("validateJobMaster() target jobkick " + jobKick.getJobkickId() +
						", jobunitId = " + jobunitId + ", jobId = " + jobId);
				try{
					// jobunitId,jobid?????????????????????
					//true : ???????????????????????????????????????????????????
					validateJobId(jobunitId,jobId,true);

					String[] args = {jobKick.getJobkickId()};
					m_log.debug(MessageConstant.MESSAGE_JOBTRIGGERTYPE_NOT_EXIST_REFERENCE.getMessage(args));
				} catch (InvalidSetting e) {
					// ???????????????????????????????????????????????????????????????????????????????????????
					String[] args = {jobKick.getJobkickId(), jobunitId, jobId};
					m_log.info("validateJobMaster() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOBTRIGGERTYPE_REFERENCE.getMessage(args));
				}
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (JobInfoNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("validateJobMaster() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	/**
	 * ??????????????????validate
	 * INSERT, UPDATE?????????????????????
	 * 
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 * @throws JobInvalid
	 */
	public static void validateJobUnit(JobTreeItem item) throws InvalidSetting, InvalidRole, HinemosUnknown, JobInvalid {
		validateJobInfo(item);
		validateDuplicateJobId(item);
		validateWaitRule(item);
		validateReferJob(item);
		validateReferJobNet(item);
	}


	/**
	 * ????????????????????????????????????????????????validate
	 * @param JobKick
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateJobmapIconImage(JobmapIconImage jobmapIconImage) throws InvalidSetting, HinemosUnknown, InvalidRole {
		// iconID
		CommonValidator.validateString(MessageConstant.FILE_NAME.getMessage(), jobmapIconImage.getIconId(), true, 1, 64);
		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), jobmapIconImage.getDescription(), true, 1, 256);
		// filedata
		if (jobmapIconImage.getFiledata() == null) {
			InvalidSetting e = new InvalidSetting("filedata is not defined.");
			m_log.info("validateJobmapIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobmapIconImage.getOwnerRoleId(), true, jobmapIconImage.getIconId(), HinemosModuleConstant.JOBMAP_IMAGE_FILE);
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????
	 * ???????????????????????????????????????????????????????????????????????????
	 * @param iconId ????????????ID
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void valideDeleteJobmapIconImage(String iconId) throws InvalidSetting, HinemosUnknown{
		try{
			if (iconId == null || iconId.equals("")) {
				InvalidSetting e = new InvalidSetting("iconId is not defined.");
				m_log.info("valideDeleteJobmapIconImage() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ??????????????????????????????????????????????????????????????????????????????
			String defaultJobIconId =  new JobControllerBean().getJobmapIconIdJobDefault();
			String defaultJobnetIconId =  new JobControllerBean().getJobmapIconIdJobnetDefault();
			String defaultApprovalIconId = new JobControllerBean().getJobmapIconIdApprovalDefault();
			String defaultMonitorIconId = new JobControllerBean().getJobmapIconIdMonitorDefault();
			String defaultFileIconId = new JobControllerBean().getJobmapIconIdFileDefault();
			if (iconId.equals(defaultJobIconId) || iconId.equals(defaultJobnetIconId) 
					|| iconId.equals(defaultApprovalIconId) || iconId.equals(defaultMonitorIconId) 
					|| iconId.equals(defaultFileIconId)) {
				String[] args = {iconId};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_ICONID_DEFAULT.getMessage(args));
				m_log.info("valideDeleteJobmapIconImage() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			//?????????
			List<JobMstEntity> jobMstList =
					QueryUtil.getJobMstEnityFindByIconId(iconId);
			if (jobMstList != null && jobMstList.size() > 0) {
				for(JobMstEntity jobMst : jobMstList){
					m_log.debug("valideDeleteJobmapIconImage() target JobMaster " + jobMst.getId().getJobId() + ", iconId = " + iconId);
					if(jobMst.getIconId() != null){
						String[] args = {jobMst.getId().getJobId(), iconId};
						throw new InvalidSetting(MessageConstant.MESSAGE_DELETE_NG_JOB_REFERENCE_TO_ICONFILE.getMessage(args));
					}
				}
			}
			
			// log.cc_job_info????????????????????????????????????????????????????????????????????????????????????????????????
			// ???????????????log.cc_job_info???iconId?????????????????????????????????????????????????????????????????????????????????????????????

		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			HinemosUnknown e1 = new HinemosUnknown(e.getMessage(), e);
			m_log.warn("valideDeleteJobmapIconImage() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e1;
		}
	}

	private static void validateJobInfo(JobTreeItem item) throws InvalidSetting, InvalidRole, HinemosUnknown{

		if(item == null || item.getData() == null){
			m_log.warn("validateJobInfo is null");
			return ;
		}

		JobInfo jobInfo = item.getData();

		// ?????????ID
		String jobId = jobInfo.getId();
		CommonValidator.validateId(MessageConstant.JOB_ID.getMessage(), jobId, 64);

		// ?????????????????????ID
		String jobunitId = jobInfo.getJobunitId();
		CommonValidator.validateId(MessageConstant.JOBUNIT_ID.getMessage(), jobunitId, 64);

		// ????????????
		String jobName = jobInfo.getName();
		CommonValidator.validateString(MessageConstant.JOB_NAME.getMessage(), jobName, true, 1, 64);

		// ??????
		String description = jobInfo.getDescription();
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), description, true, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(jobInfo.getOwnerRoleId(), true,
				new JobMstEntityPK(jobunitId, jobId), HinemosModuleConstant.JOB);

		// ????????????????????????????????????jobId???jobunitId????????????
		if (jobInfo.getType() == JobConstant.TYPE_JOBUNIT) {
			if (!jobId.equals(jobunitId)) {
				InvalidSetting e = new InvalidSetting("jobType is TYPE_JOBUNIT, but jobId != jobunitId");
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		if (jobInfo.getType() == JobConstant.TYPE_JOB) {
			JobCommandInfo command = jobInfo.getCommand();

			// ????????????????????????????????????????????????
			if (command.getStartCommand() == null || "".equals(command.getStartCommand())) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND.getMessage());
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
			}
			// ?????????????????????????????????????????????????????????????????????????????????)
			if (command.getStopType() == CommandStopTypeConstant.EXECUTE_COMMAND) {
				if (command.getStopCommand() == null || "".equals(command.getStopCommand())) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND.getMessage());
					m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			// ??????????????????????????????
			if (command.getSpecifyUser()) {
				CommonValidator.validateString(MessageConstant.EFFECTIVE_USER.getMessage(), command.getUser(), true, 1, DataRangeConstant.VARCHAR_64);
			}
			
			// ??????????????????????????????ID???????????????
			if(command.getFacilityID() == null || "".equals(command.getFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ??????????????????????????????????????????????????????ID????????????????????????
			if(!ParameterUtil.isParamFormat(command.getFacilityID())){
				try {
					FacilityTreeCache.validateFacilityId(command.getFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add job unknown error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + command.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			} else {
				CommonValidator.validateString(MessageConstant.JOB_PARAM_ID.getMessage(), command.getFacilityID(), true, 1, 512);
			}

			// ???????????????????????????(???????????????????????????)
			if (command.getMessageRetry() == null || command.getCommandRetry() == null) {
				String message = "validateJobUnit() messageRetry or commandRetry is null(job). messageRetry =" + command.getMessageRetry()
						+ ", commandRetry =" + command.getCommandRetry();
				m_log.info(message);
				throw new InvalidSetting(message);
			}

			// ???????????????????????????
			CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), command.getMessageRetry(), 1, DataRangeConstant.SMALLINT_HIGH);

			if (command.getCommandRetryFlg()) {
				CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), command.getCommandRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
			}

			// ????????????????????????????????????
			// ????????????????????????????????????Hinemos???????????????????????????
			int scriptMaxSize = HinemosPropertyCommon.job_script_maxsize.getIntegerValue();
			if(command.getManagerDistribution()) {
				// ??????????????????
				String scriptName = command.getScriptName();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT_NAME.getMessage(), scriptName, true, 1, 256);
				// ????????????????????????
				String scriptEncoding = command.getScriptEncoding();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT_ENCODING.getMessage(), scriptEncoding, true, 1, 32);
				// ???????????????
				String scriptContent = command.getScriptContent();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT.getMessage(), scriptContent, true, 1, scriptMaxSize);
			} else {
				// ??????????????????
				String scriptName = command.getScriptName();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT_NAME.getMessage(), scriptName, false, 0, 256);
				// ????????????????????????
				String scriptEncoding = command.getScriptEncoding();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT_ENCODING.getMessage(), scriptEncoding, false, 0, 32);
				// ???????????????
				String scriptContent = command.getScriptContent();
				CommonValidator.validateString(MessageConstant.JOB_SCRIPT.getMessage(), scriptContent, false, 0, scriptMaxSize);
			}
			
			// ??????????????????????????????
			ArrayList<JobCommandParam> jobCommandParamList = jobInfo.getCommand().getJobCommandParamList();
			if(jobCommandParamList != null && jobCommandParamList.size() > 0) {
				for (JobCommandParam jobCommandParam : jobCommandParamList) {
					// ???????????????ID
					String paramId = jobCommandParam.getParamId();
					CommonValidator.validateId(MessageConstant.JOB_PARAM_ID.getMessage(), paramId, 64);
					// ???
					String jobCommandParamValue = jobCommandParam.getValue();
					CommonValidator.validateString(MessageConstant.JOB_PARAM_VALUE.getMessage(), jobCommandParamValue, true, 1, 256);
				}
			}
			
			// ???????????????????????????
			List<JobEnvVariableInfo> envInfoList = command.getEnvVariableInfo();
			if(envInfoList != null && envInfoList.size() > 0) {
				for (JobEnvVariableInfo envInfo : envInfoList) {
					// ??????
					String envId = envInfo.getEnvVariableId();
					CommonValidator.validateId(MessageConstant.JOB_ENV_ID.getMessage(), envId, 64);
					// ???
					String envValue = envInfo.getValue();
					CommonValidator.validateString(MessageConstant.JOB_ENV_VALUE.getMessage(), envValue, true, 1, 256);
					// ??????
					String envDescription = envInfo.getDescription();
					CommonValidator.validateString(MessageConstant.JOB_ENV_DESCRIPTION.getMessage(), envDescription, false, 0, 256);
				}
			}
		} else if (jobInfo.getType() == JobConstant.TYPE_FILEJOB) {
			JobFileInfo file = jobInfo.getFile();
			
			// ?????????????????????????????????????????????????????????
			if (file.getSrcFile() == null || "".equals(file.getSrcFile())) {
				 InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_FILE_NOT_FOUND.getMessage(jobInfo.getId()));
				 m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				 throw e;
			}
			// ??????????????????????????????????????????????????????????????????
			if (file.getDestDirectory() == null || "".equals(file.getDestDirectory())) {
				 InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_DIR.getMessage());
				 m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				 throw e;
			}
			// ???????????????????????????ID(?????????)
			if(file.getSrcFacilityID() == null || "".equals(file.getSrcFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_NODE.getMessage());
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			try {
				FacilityTreeCache.validateFacilityId(file.getSrcFacilityID(), jobInfo.getOwnerRoleId(), true);
			} catch (FacilityNotFound e) {
				InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
				m_log.info("validateJobUnit() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			} catch (InvalidRole e) {
				throw e;
			} catch (InvalidSetting e) {
				InvalidSetting e1 = new InvalidSetting("Src FacilityId is not node. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
				m_log.info("validateJobUnit() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw new HinemosUnknown("add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
			} catch (Exception e) {
				m_log.warn("validateJobUnit() add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw new HinemosUnknown("add file transfer job unknown error. Src FacilityId = " + file.getSrcFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
			}

			// ???????????????????????????ID(?????????/????????????)
			if(file.getDestFacilityID() == null || "".equals(file.getDestFacilityID())){
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			}else{
				try {
					FacilityTreeCache.validateFacilityId(file.getDestFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add file transfer job unknown error. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add file transfer job unknown error. Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}

			// ??????[????????????]??????????????????????????????
			if(jobInfo.getWaitRule().isEnd_delay_operation() && jobInfo.getWaitRule().getEnd_delay_operation_type() == OperationConstant.TYPE_STOP_AT_ONCE){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_STOPCOMMAND_NG_IN_FILE_TRANSFER.getMessage());
				m_log.info("validateJobUnit() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			// ??????????????????
			if (file.isSpecifyUser()) {
				CommonValidator.validateString(MessageConstant.EFFECTIVE_USER.getMessage(), file.getUser(), true, 1, DataRangeConstant.VARCHAR_64);
			}
			
			try {
				// ???????????????????????????(???????????????????????????)
				if (file.getMessageRetry() == null || file.getCommandRetry() == null) {
					String message = "validateJobUnit() messageRetry or commandRetry is null(file transfer job). messageRetry =" + file.getMessageRetry()
							+ ", commandRetry =" + file.getCommandRetry();
					m_log.info(message);
					throw new InvalidSetting(message);
				}

				// ???????????????????????????
				CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), file.getMessageRetry(), 1, DataRangeConstant.SMALLINT_HIGH);

				if (file.isCommandRetryFlg()) {
					CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), file.getCommandRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
				}
			} catch (Exception e) {
				m_log.info("validateJobUnit() add file transfer job retry error.Dest FacilityId = " + file.getDestFacilityID() + ", jobunitId = " + jobunitId
						+ ", jobId = " + jobId + ",messageRetry =" + file.getMessageRetry() + ",commandRetry =" + file.getCommandRetry() + ",commandRetryFlg ="
						+ file.isCommandRetryFlg() + " : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_REFERJOB || jobInfo.getType() == JobConstant.TYPE_REFERJOBNET ) {
			if ( jobInfo.getReferJobId() == null || jobInfo.getReferJobId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REFERENCE_JOBID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if ( jobInfo.getReferJobUnitId() == null || jobInfo.getReferJobUnitId().equals("") || !jobInfo.getReferJobUnitId().equals(jobInfo.getJobunitId())) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REFERENCE_JOBUNITID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_APPROVALJOB ) {
			if (jobInfo.getApprovalReqRoleId() == null || jobInfo.getApprovalReqRoleId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_ROLEID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ????????????????????????
			try {
				com.clustercontrol.accesscontrol.util.QueryUtil.getRolePK(jobInfo.getApprovalReqRoleId());
			} catch (RoleNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
			if (jobInfo.getApprovalReqUserId() == null || jobInfo.getApprovalReqUserId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_USERID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ????????????????????????
			if (!jobInfo.getApprovalReqUserId().equals("*")) {
				try {
					com.clustercontrol.accesscontrol.util.QueryUtil.getUserPK(jobInfo.getApprovalReqUserId());
				} catch (UserNotFound e) {
					throw new InvalidSetting(e.getMessage(), e);
				}
				
			}
			if (jobInfo.getApprovalReqSentence() == null || jobInfo.getApprovalReqSentence().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_SENTENCE.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if (jobInfo.getApprovalReqMailTitle() == null || jobInfo.getApprovalReqMailTitle().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_MAIL_TITLE.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			if(!jobInfo.isUseApprovalReqSentence()){
				if (jobInfo.getApprovalReqMailBody() == null || jobInfo.getApprovalReqMailBody().equals("")) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPROVAL_REQ_MAIL_BODY.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
		}else if (jobInfo.getType() == JobConstant.TYPE_MONITORJOB ) {
			MonitorJobInfo monitor = jobInfo.getMonitor();
			if (monitor.getMonitorId() == null || monitor.getMonitorId().equals("")) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MONITOR_ID.getMessage());
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			MonitorInfo monitorInfo = null;
			try {
				monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
					monitor.getMonitorId(), jobInfo.getOwnerRoleId());
				
			} catch (InvalidRole e) {
				throw e;
			} catch (Exception e) {
				String[] args = {monitor.getMonitorId()};
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_JOB_MONITOR_NOT_FOUND.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e1;
			}

			if (monitor.getMonitorInfoEndValue() == null) {
				String[] args = { MessageConstant.INFO.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_INFO.getMessage(), monitor.getMonitorInfoEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitor.getMonitorWarnEndValue() == null) {
				String[] args = { MessageConstant.WARNING.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_WARNING.getMessage(), monitor.getMonitorWarnEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitor.getMonitorCriticalEndValue() == null) {
				String[] args = { MessageConstant.CRITICAL.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_CRITICAL.getMessage(), monitor.getMonitorCriticalEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitor.getMonitorUnknownEndValue() == null) {
				String[] args = { MessageConstant.UNKNOWN.getMessage()};
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_END_VALUE.getMessage(args));
				m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_UNKNOWN.getMessage(), monitor.getMonitorUnknownEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
				if (monitor.getMonitorWaitTime() == null) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_WAIT_TIME.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				CommonValidator.validateInt(MessageConstant.MONITORJOB_MINUTE_WAIT.getMessage(), monitor.getMonitorWaitTime(), 0, DataRangeConstant.SMALLINT_HIGH);
				if (monitor.getMonitorWaitEndValue() == null) {
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB_MONITOR_WAIT_END_VALUE.getMessage());
					m_log.info("validateJobUnit() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				CommonValidator.validateInt(MessageConstant.MONITORJOB_RETURNVALUE_WAIT.getMessage(), monitor.getMonitorWaitEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
			}
			
			// ??????????????????????????????ID???????????????
			if(monitor.getFacilityID() == null || "".equals(monitor.getFacilityID())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// ??????????????????????????????????????????????????????ID????????????????????????
			if(!ParameterUtil.isParamFormat(monitor.getFacilityID())){
				try {
					FacilityTreeCache.validateFacilityId(monitor.getFacilityID(), jobInfo.getOwnerRoleId(), false);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("FacilityId is not exist in repository. FacilityId = " + monitor.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId);
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add job unknown error. FacilityId = " + monitor.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. FacilityId = " + monitor.getFacilityID() + ", jobunitId = " + jobunitId + ", jobId = " + jobId, e);
				}
			}
		}

		int type = jobInfo.getType();
		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_JOBNET ||
				type == JobConstant.TYPE_JOBUNIT ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_FILEJOB) {
			ArrayList<JobEndStatusInfo> endStatusList = item.getData().getEndStatus();
			if (endStatusList == null) {
				String message = "JobEndStatus is null [" + item.getData().getId() + "]";
				m_log.info(message);
				throw new InvalidSetting(message);
			}
			if (endStatusList.size() != 3) {
				String message = "the number of JobEndStatus is too few [" + item.getData().getId() + "] " +
						endStatusList.size();
				m_log.info(message);
				throw new InvalidSetting(message);
			}
			for (JobEndStatusInfo endStatus : endStatusList) {
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), endStatus.getValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				CommonValidator.validateInt(MessageConstant.RANGE_END_VALUE.getMessage(), endStatus.getStartRangeValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				CommonValidator.validateInt(MessageConstant.RANGE_END_VALUE.getMessage(), endStatus.getEndRangeValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				if (endStatus.getStartRangeValue() > endStatus.getEndRangeValue()) {
					String message = "startRangeValue > endRangeValue. start="+ endStatus.getStartRangeValue()+", end="+endStatus.getEndRangeValue()+ ", jobunitId = " + jobunitId + ", jobId = " + jobId;
					m_log.info(message);
					throw new InvalidSetting(message);
				}
			}
			

			if (jobInfo.getBeginPriority() == null
					|| jobInfo.getNormalPriority() == null
					|| jobInfo.getWarnPriority() == null
					|| jobInfo.getAbnormalPriority() == null) {
				String message = "the priorities of JobInfo less than 4 [" + jobInfo.getId() + "]";
				m_log.info(message);
				throw new InvalidSetting(message);
			}

			// ???????????????????????????
			if (jobInfo.getNotifyRelationInfos() != null) {
				for(NotifyRelationInfo notifyInfo : jobInfo.getNotifyRelationInfos()){
					CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, jobInfo.getOwnerRoleId());
				}
			}
		}

		if (type == JobConstant.TYPE_JOB ||
				type == JobConstant.TYPE_FILEJOB ||
				type == JobConstant.TYPE_APPROVALJOB ||
				type == JobConstant.TYPE_MONITORJOB ||
				type == JobConstant.TYPE_REFERJOBNET ||
				type == JobConstant.TYPE_REFERJOB) {

			// ????????????ID?????????????????????
			if (jobInfo.getIconId() != null && !"".equals(jobInfo.getIconId())) {
				try {
					new SelectJobmap().getJobmapIconImage(jobInfo.getIconId());
				} catch (IconFileNotFound e) {
					InvalidSetting e1 = new InvalidSetting("Icon Image is not exist in repository. Icon Id = " + jobInfo.getIconId());
					m_log.info("validateJobUnit() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (ObjectPrivilege_InvalidRole e) {
					throw new InvalidRole(e.getMessage(), e);
				} catch (Exception e) {
					m_log.warn("validateJobUnit() add job unknown error. Icon Id = " + jobInfo.getIconId() + " : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					throw new HinemosUnknown("add job unknown error. Icon Id = " + jobInfo.getIconId(), e);
				}
			}
		}
			
		//???JobTreeItem?????????
		for(JobTreeItem child : item.getChildren()){
			validateJobInfo(child);
		}
	}

	/**
	 * ??????????????????ID???????????????
	 * @param item
	 * @return
	 * @throws JobInvalid
	 */
	private static void validateDuplicateJobId(JobTreeItem item) throws JobInvalid {
		if(item == null || item.getData() == null) {
			return;
		}

		ArrayList<String> jobList = getJobIdList(item);
		Collections.sort(jobList);
		for (int i = 0; i < jobList.size() - 1; i++) {
			if (jobList.get(i).equals(jobList.get(i + 1))) {
				JobInvalid e = new JobInvalid(MessageConstant.MESSAGE_JOBUNIT_NG_DUPLICATE_JOB.getMessage(item.getData().getJobunitId(), jobList.get(i)));
				m_log.info("findDuplicateJobId() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
	}

	private static ArrayList<String> getJobIdList(JobTreeItem item) {
		if(item == null || item.getData() == null) {
			return new ArrayList<String>();
		}
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(item.getData().getId());
		for (JobTreeItem child : item.getChildren()) {
			ret.addAll(getJobIdList(child));
		}
		return ret;
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param item ??????????????????????????????????????????????????????????????????????????????
	 * @throws InvalidRole 
	 */
	private static void validateWaitRule(JobTreeItem item) throws InvalidSetting, JobInvalid, InvalidRole{
		if(item == null || item.getData() == null) {
			return;
		}
		//?????????ID??????
		String jobId = item.getData().getId();
		//?????????????????????????????????
		JobWaitRuleInfo waitRule = item.getData().getWaitRule();
		if(waitRule != null) {
			// ???????????????????????????????????????
			if (waitRule.getObject() != null) {
				for (JobObjectInfo objectInfo : waitRule.getObject()) {
					m_log.debug("objectInfo=" + objectInfo);
	
					if(objectInfo.getType() != JudgmentObjectConstant.TYPE_TIME
							&& objectInfo.getType() != JudgmentObjectConstant.TYPE_START_MINUTE
							&& objectInfo.getType() != JudgmentObjectConstant.TYPE_JOB_PARAMETER
							&& objectInfo.getType() != JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS
							&& objectInfo.getType() != JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE){
						m_log.debug("Not Time and Not Delay");
						//????????????????????????ID?????????????????????????????????????????????
						boolean find = false;
						String targetJobId = objectInfo.getJobId();
						for(JobTreeItem child : item.getParent().getChildren()){
							//?????????ID???????????????
							JobInfo childInfo = child.getData();
							if(childInfo != null && !jobId.equals(childInfo.getId())){
								if(targetJobId.equals(childInfo.getId())){
									find = true;
									break;
								}
							}
						}
						if(!find){
							String args[] = {jobId, targetJobId};
							JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_JOBID_NG_INVALID_JOBID.getMessage(args));
							m_log.info("checkWaitRule() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
							throw ji;
						}
					} else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS
								|| objectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE){
						//??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
						JobTreeItem jobunitItem = item;
						while (jobunitItem.getData().getType() != JobConstant.TYPE_JOBUNIT) {
							jobunitItem = jobunitItem.getParent();
						}
						List<String> jobIdList = getJobIdList(jobunitItem);
						String targetJobId = objectInfo.getJobId();
						if (!jobIdList.contains(targetJobId)) {
							String args[] = {jobId, targetJobId};
							JobInvalid ji = new JobInvalid(MessageConstant.MESSAGE_WAIT_CROSS_SESSION_JOBID_NG_INVALID_JOBID.getMessage(args));
							m_log.info("checkWaitRule() : " + ji.getClass().getSimpleName() + ", " + ji.getMessage());
							throw ji;
						}
						
					}else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
						// ??????????????????????????????????????????????????????????????????
						CommonValidator.validateString(MessageConstant.WAIT_RULE_DECISION_VALUE_1.getMessage(), objectInfo.getDecisionValue01(), true, 1, 128);
						CommonValidator.validateString(MessageConstant.WAIT_RULE_DECISION_VALUE_2.getMessage(), objectInfo.getDecisionValue02(), true, 1, 128);
						CommonValidator.validateInt(MessageConstant.WAIT_RULE_DECISION_CONDITION.getMessage(), objectInfo.getDecisionCondition(), 0, 7);
					}else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_TIME) {
						if(objectInfo.getTime() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < objectInfo.getTime()){
							String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
							InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
							m_log.info("validateWaitRule() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}else if (objectInfo.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
						CommonValidator.validateInt(MessageConstant.TIME_AFTER_SESSION_START.getMessage(), objectInfo.getStartMinute(), 0, DataRangeConstant.SMALLINT_HIGH);
					}
				}
			}
			// ?????????????????????????????????????????????
			if (waitRule.isEndCondition()) {
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				if (waitRule.getEndStatus() == null) {
					String message = "validateWaitRule() : endStatus(endCondition) is null";
					m_log.info(message);
					throw new InvalidSetting(message);
				}
			}
			// ???????????????????????????
			if (waitRule.isCalendar()) {
				CommonValidator.validateCalenderId(waitRule.getCalendarId(), true, item.getData().getOwnerRoleId());
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getCalendarEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				if (waitRule.getSkipEndStatus() == null) {
					String message = "validateWaitRule() : endStatus(calendar) is null";
					throw new InvalidSetting(message);
				}
			}
			
			// ???????????????????????????
			if (waitRule.isSkip()) {
				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getSkipEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				if (waitRule.getSkipEndStatus() == null) {
					String message = "validateWaitRule() : endStatus(Skip) is null";
					m_log.info(message);
					throw new InvalidSetting(message);
				}
			}
			if(waitRule.isStart_delay_time()){
				if(waitRule.getStart_delay_time_value() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < waitRule.getStart_delay_time_value()){
					String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if(waitRule.isStart_delay_session()) {
				CommonValidator.validateInt(MessageConstant.TIME_AFTER_SESSION_START.getMessage(), waitRule.getStart_delay_session_value(), 1, DataRangeConstant.SMALLINT_HIGH);
			}
			if (waitRule.isStart_delay_notify()) {
				CommonValidator.validatePriority(MessageConstant.START_DELAY.getMessage(), waitRule.getStart_delay_notify_priority(), false);
			}
			if(waitRule.isEnd_delay_time()){
				if(waitRule.getEnd_delay_time_value() < DATETIME_VALUE_MIN || DATETIME_VALUE_MAX < waitRule.getEnd_delay_time_value()){
					String[] args = {DATETIME_STRING_MIN, DATETIME_STRING_MAX, item.getData().getJobunitId(), jobId};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_RANGE_OVER_JOB_SETTINGTIME.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if (waitRule.isEnd_delay_session()) {
				CommonValidator.validateInt(MessageConstant.TIME_AFTER_SESSION_START.getMessage(), waitRule.getEnd_delay_session_value(), 1, DataRangeConstant.SMALLINT_HIGH);
			}
			if (waitRule.isEnd_delay_job()) {
				CommonValidator.validateInt(MessageConstant.TIME_AFTER_JOB_START.getMessage(), waitRule.getEnd_delay_job_value(), 1, DataRangeConstant.SMALLINT_HIGH);
			}
			if(waitRule.isEnd_delay_change_mount()){
				// 0???????????????????????????????????????????????????
				Double minSize = 0D;
				Double maxSize = 100D;
				if (waitRule.getEnd_delay_change_mount_value() == null 
						|| waitRule.getEnd_delay_change_mount_value() <= minSize 
						|| waitRule.getEnd_delay_change_mount_value() > maxSize) {
					String[] args = {MessageConstant.JOB_CHANGE_MOUNT.getMessage(),
							((new BigDecimal(minSize)).toBigInteger()).toString(),
							((new BigDecimal(maxSize)).toBigInteger()).toString()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_BETWEEN_EXCLUDE_MINSIZE.getMessage(args));
					m_log.info("validateWaitRule() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}
			if (waitRule.isEnd_delay_notify()) {
				CommonValidator.validatePriority(MessageConstant.END_DELAY.getMessage(), waitRule.getEnd_delay_notify_priority(), false);
			}
			//??????????????????????????????????????????
			if (waitRule.isExclusiveBranch()) {
				//????????????????????????????????????????????????
				//???????????????????????????????????????????????????????????????????????????
				//??????????????????????????????????????????????????????ID?????????
				List<String> nextJobIdList = new ArrayList<>();
				JobTreeItem parent= item.getParent();
				if (parent != null) {
					//?????????????????????????????????
					List<JobTreeItem> siblingJobList = parent.getChildren();
					//???????????????????????????????????????????????????????????????????????????
					for (JobTreeItem sibling : siblingJobList) {
						if (sibling == item) {
							continue;
						}
						JobInfo siblingJobInfo = sibling.getData();
						if (siblingJobInfo.getWaitRule() == null) {
							continue;
						}
						List<JobObjectInfo> siblingWaitJobObjectInfoList = siblingJobInfo.getWaitRule().getObject();
						if (siblingWaitJobObjectInfoList != null) {
							for (JobObjectInfo siblingWaitJobObjectInfo : siblingWaitJobObjectInfoList) {
								//????????????????????????????????????????????????????????????????????????????????????
								if ((siblingWaitJobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS ||
									siblingWaitJobObjectInfo.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE) &&
									siblingWaitJobObjectInfo.getJobId().equals(item.getData().getId())) {
									nextJobIdList.add(sibling.getData().getId());
									break;
								} 
							}
						}
					}
				}

				List<JobNextJobOrderInfo> nextJobOrderList = waitRule.getExclusiveBranchNextJobOrderList();
				if (nextJobOrderList != null) {
					for (JobNextJobOrderInfo nextJobOrder: nextJobOrderList){
						String targetJobId = nextJobOrder.getNextJobId();
						//???????????????????????????ID??????????????????????????????????????????
						if (!nextJobIdList.contains(targetJobId)) {
							String[] args = {jobId, targetJobId};
							InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_NEXT_JOB_ORDER_JOBID_NG_INVALID_JOBID.getMessage(args));
							m_log.info("validateWaitRule() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}
					}
				}

				CommonValidator.validateInt(MessageConstant.END_VALUE.getMessage(), waitRule.getExclusiveBranchEndValue(), DataRangeConstant.INTEGER_LOW, DataRangeConstant.INTEGER_HIGH);
				if (waitRule.getExclusiveBranchEndStatus() == null) {
					String message = "validateWaitRule() : endStatus(exclusiveBranch) is null";
					throw new InvalidSetting(message);
				}
			}
			//?????????????????????????????????
			if (waitRule.getJobRetryFlg()) {
				if (waitRule.getJobRetry() == null) {
					String message = "validateJobUnit() jobRetry is null(job). jobRetry =" + waitRule.getJobRetry();
					m_log.info(message);
					throw new InvalidSetting(message);
				}
				// ???????????????????????????
				CommonValidator.validateInt(MessageConstant.JOB_RETRIES.getMessage(), waitRule.getJobRetry(), 1, DataRangeConstant.SMALLINT_HIGH);
			}
			// ????????????????????????????????????????????????
			if (waitRule.getQueueFlg() != null && waitRule.getQueueFlg().booleanValue()) {
				String queueId = waitRule.getQueueId();
				// ??????????????????
				CommonValidator.validateId(MessageConstant.JOB_QUEUE.getMessage(), queueId, JobQueueConstant.ID_MAXLEN);
				// ???????????????????????????
				try {
					Singletons.get(JobQueueContainer.class).get(queueId);
				} catch (JobQueueNotFoundException e) {
					throw new InvalidSetting(
							MessageConstant.MESSAGE_JOBQUEUE_NOT_FOUND.getMessage(new String[] { queueId }), e);
				}
			}
		} // waitRule != null
		//???JobTreeItem?????????
		for(JobTreeItem child : item.getChildren()){
			validateWaitRule(child);
		}
		return;
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param item ??????????????????????????????????????????????????????????????????????????????
	 */
	private static void validateReferJob(JobTreeItem item) throws JobInvalid{
		if(item == null || item.getData() == null) {
			return;
		}

		//?????????????????????????????????????????????????????????
		ArrayList<JobInfo> referJobList = JobUtil.findReferJob(item);
		m_log.trace("ReferJob count : " + referJobList.size());
		for (JobInfo referJob : referJobList) {
			String referJobId = referJob.getReferJobId();
			m_log.trace("ReferJobID : " + referJobId);
			//???????????????????????????????????????????????????????????????
			int ret = JobUtil.checkValidJob(item, referJobId, referJob.getReferJobSelectType());
			if(ret != 0) {
				//??????????????????????????????????????????????????????????????????
				String args[] = {referJob.getId(), referJobId};
				if(ret == 1) {
					// ????????????????????????????????????
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SETTING.getMessage(args));
				}else{
					// ???????????????????????????
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_JOBID.getMessage(args));
				}
			}
		}
		return;
	}
	
	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param item ???????????????????????????????????????????????????????????????????????????????????????
	 */
	private static void validateReferJobNet(JobTreeItem item) throws JobInvalid{
		if(item == null || item.getData() == null) {
			return;
		}

		//??????????????????????????????????????????????????????????????????
		ArrayList<JobInfo> referJobNetList = JobUtil.findReferJobNet(item);
		m_log.trace("ReferJobNet count : " + referJobNetList.size());
		for (JobInfo referJobNet : referJobNetList) {
			String referJobNetId = referJobNet.getReferJobId();
			m_log.trace("ReferJobNetID : " + referJobNetId);
			//????????????????????????????????????????????????????????????????????????
			int ret = JobUtil.checkValidJobNet(item, referJobNetId, referJobNet);
			if(ret != 0) {
				//???????????????????????????????????????????????????????????????????????????
				String args[] = {referJobNet.getId(), referJobNetId};
				if(ret == 1) {
					// ????????????????????????????????????
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SETTING.getMessage(args));
				} else if(ret == 2) {
					// ???????????????????????????????????????????????????
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_SUBORDINATE_JOB.getMessage(args));
				}else{
					// ???????????????????????????
					throw new JobInvalid(MessageConstant.MESSAGE_REFERENCE_JOBID_NG_INVALID_JOBID.getMessage(args));
				}
				
			}
		}
		return;
	}
}
