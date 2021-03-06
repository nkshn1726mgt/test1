/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.session.AccessControllerBean;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.infra.util.InfraManagementValidator;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.monitor.util.EventHinemosPropertyUtil;
import com.clustercontrol.notify.bean.EventNotifyInfo;
import com.clustercontrol.notify.bean.ExecFacilityConstant;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;

/**
 * ????????????????????????????????????
 * 
 * @since 4.0
 */
public class NotifyValidator {

	private static Log m_log = LogFactory.getLog(NotifyValidator.class);

	private static int getPriorityCode(int i) {
		Integer[] priorities = new Integer[] { PriorityConstant.TYPE_INFO,
				PriorityConstant.TYPE_WARNING,
				PriorityConstant.TYPE_CRITICAL,
				PriorityConstant.TYPE_UNKNOWN };
		return priorities[i];
	}

	private static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}

	private static void throwInvalidSetting(HinemosException e)
			throws InvalidSetting {
		m_log.info("validateNotifyInfo() : " + e.getClass().getSimpleName()
				+ ", " + e.getMessage());
		throw new InvalidSetting(e.getMessage(), e);
	}

	private static void throwInvalidSetting(String messageId)
			throws InvalidSetting {
		InvalidSetting e = new InvalidSetting(messageId);
		m_log.info("validateNotifyInfo() : " + e.getClass().getSimpleName()
				+ ", " + e.getMessage());
		throw e;
	}

	private static boolean validateCommandInfo(NotifyCommandInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		// ???????????????
		String[] effectiveUsers = new String[] {
				info.getInfoEffectiveUser(),
				info.getWarnEffectiveUser(),
				info.getCriticalEffectiveUser(),
				info.getUnknownEffectiveUser()
		};
		// ??????????????????
		String[] commands = new String[] { info.getInfoCommand(),
				info.getWarnCommand(), info.getCriticalCommand(),
				info.getUnknownCommand() };

		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (effectiveUsers[validFlgIndex] == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EFFECTIVEUSER.getMessage());
			}
			CommonValidator.validateString("effective.user", effectiveUsers[validFlgIndex],
					true, 0, 64);

			if (isNullOrEmpty(commands[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_COMMAND_NOTIFY.getMessage());
			}
			CommonValidator.validateString("command", commands[validFlgIndex], true, 1, 1024);
		}
		
		// ??????????????????
		if(info.getTimeout() == null) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT_TIMEOUT.getMessage());
			m_log.info("validateCustom() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.TIME_OUT.getMessage(),
				info.getTimeout(), 1, 60 * 60 * 1000);

		return true;
	}

	private static boolean validateEventInfo(NotifyEventInfo info,
			NotifyInfo notifyInfo) {
		return !NotifyUtil.getValidFlgIndexes(info).isEmpty();
	}

	private static boolean validateJobInfo(NotifyJobInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (info.getJobExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			// ??????????????????????????????????????????????????????
			if (info.getJobExecFacility() == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
			}
			try {
				FacilityTreeCache.validateFacilityId(info.getJobExecFacility(),
						notifyInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throwInvalidSetting(e);
			}
		}

		String[] jobIds = new String[] {
				info.getInfoJobId(),
				info.getWarnJobId(),
				info.getCriticalJobId(),
				info.getUnknownJobId()
		};
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (isNullOrEmpty(jobIds[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOBID.getMessage());
			}
		}

		return true;
	}

	private static boolean validateLogInfo(NotifyLogEscalateInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (info.getEscalateFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			// ??????????????????????????????????????????????????????
			if (info.getEscalateFacility() == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
			}

			try {
				FacilityTreeCache.validateFacilityId(
						info.getEscalateFacility(),
						notifyInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throwInvalidSetting(e);
			}
		}

		Integer[] syslogFacilities = new Integer[] {
				info.getInfoSyslogFacility(), info.getWarnSyslogFacility(),
				info.getCriticalSyslogFacility(),
				info.getUnknownSyslogFacility() };
		Integer[] syslogPriorities = new Integer[] {
				info.getInfoSyslogPriority(), info.getWarnSyslogPriority(),
				info.getCriticalSyslogPriority(),
				info.getUnknownSyslogPriority() };
		String[] escalateMessages = new String[] {
				info.getInfoEscalateMessage(), info.getWarnEscalateMessage(),
				info.getCriticalEscalateMessage(),
				info.getUnknownEscalateMessage() };

		Locale locale = NotifyUtil.getNotifyLocale();
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (syslogFacilities[validFlgIndex] == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_FACILIY.getMessage(Messages.getString(PriorityConstant.typeToMessageCode(getPriorityCode(validFlgIndex)), locale)));
			}
			if (syslogPriorities[validFlgIndex] == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_PRIORITY.getMessage(Messages.getString(PriorityConstant.typeToMessageCode(getPriorityCode(validFlgIndex)), locale)));
			}
			if (isNullOrEmpty(escalateMessages[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MESSAGE.getMessage(Messages.getString(PriorityConstant.typeToMessageCode(getPriorityCode(validFlgIndex)), locale)));
			}
			CommonValidator.validateString(MessageConstant.MESSAGE.getMessage(),
					escalateMessages[validFlgIndex], true, 1, 1024);

		}

		if (info.getEscalatePort() == null) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_PORT_NUM.getMessage());
		}
		CommonValidator.validateInt(MessageConstant.PORT_NUMBER.getMessage(),
				info.getEscalatePort(), 1, DataRangeConstant.PORT_NUMBER_MAX);

		return true;
	}

	private static boolean validateMailInfo(NotifyMailInfo info,
			NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		// ??????????????????????????????????????????
		if (info.getMailTemplateId() != null) {
			try {
				// ????????????
				com.clustercontrol.notify.mail.util.QueryUtil
						.getMailTemplateInfoPK(info.getMailTemplateId());
				// ????????????
				com.clustercontrol.notify.mail.util.QueryUtil
						.getMailTemplateInfoPK_OR(info.getMailTemplateId(),
								notifyInfo.getOwnerRoleId());
			} catch (MailTemplateNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			} catch (InvalidRole e) {
				m_log.warn("validateNotifyInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		String[] mailAddresses = new String[] { info.getInfoMailAddress(),
				info.getWarnMailAddress(), info.getCriticalMailAddress(),
				info.getUnknownMailAddress() };
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);

			if (isNullOrEmpty(mailAddresses[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MAILADDR_CORRECT_FORMAT.getMessage());
			}

			CommonValidator.validateString(
					MessageConstant.EMAIL_ADDRESS_SSV.getMessage(), mailAddresses[validFlgIndex], true,
					1, 1024);
		}

		return true;
	}

	/**
	 * ?????????????????????????????????????????????????????????
	 * 
	 * @param mailTemplateInfo
	 * @throws InvalidSetting
	 */
	public static void validateMailTemplateInfo(
			MailTemplateInfo mailTemplateInfo) throws InvalidSetting {
		// mailTemplateId
		CommonValidator.validateId(MessageConstant.MAIL_TEMPLATE_ID.getMessage(),
				mailTemplateInfo.getMailTemplateId(), 64);

		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				mailTemplateInfo.getDescription(), false, 0, 256);

		CommonValidator.validateString(MessageConstant.SUBJECT.getMessage(),
				mailTemplateInfo.getSubject(), true, 1, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(mailTemplateInfo.getOwnerRoleId(),
				true, mailTemplateInfo.getMailTemplateId(),
				HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE);

	}

	public static void validateNotifyInfo(NotifyInfo notifyInfo)
			throws InvalidSetting, InvalidRole  {
		// notifyId
		CommonValidator.validateId(MessageConstant.NOTIFY_ID.getMessage(),
				notifyInfo.getNotifyId(), 64);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				notifyInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator
				.validateOwnerRoleId(notifyInfo.getOwnerRoleId(), true,
						notifyInfo.getNotifyId(),
						HinemosModuleConstant.PLATFORM_NOTIFY);
		
		// calendarId
		CommonValidator.validateCalenderId(notifyInfo.getCalendarId(), false, notifyInfo.getOwnerRoleId());
		
		// ?????????????????????
		if (notifyInfo.getRenotifyPeriod() != null) {
			CommonValidator.validateInt(
					MessageConstant.SUPPRESS_BY_TIME_INTERVAL.getMessage(),
					notifyInfo.getRenotifyPeriod(), 1,
					DataRangeConstant.SMALLINT_HIGH);
		}
		// ???????????????????????????????????????
		int maxInitialCount = HinemosPropertyCommon.notify_initial_count_max.getIntegerValue();
		CommonValidator.validateInt(MessageConstant.NOTIFY_INITIAL.getMessage(),
				notifyInfo.getInitialCount(), 1, maxInitialCount - 1);

		// ??????????????????
		boolean result = true;
		switch (notifyInfo.getNotifyType()) {
		case NotifyTypeConstant.TYPE_COMMAND:
			NotifyCommandInfo command = notifyInfo.getNotifyCommandInfo();
			result = validateCommandInfo(command, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_EVENT:
			NotifyEventInfo event = notifyInfo.getNotifyEventInfo();
			result = validateEventInfo(event, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_JOB:
			NotifyJobInfo job = notifyInfo.getNotifyJobInfo();
			result = validateJobInfo(job, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_LOG_ESCALATE:
			NotifyLogEscalateInfo log = notifyInfo.getNotifyLogEscalateInfo();
			result = validateLogInfo(log, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_MAIL:
			NotifyMailInfo mail = notifyInfo.getNotifyMailInfo();
			result = validateMailInfo(mail, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_STATUS:
			NotifyStatusInfo status = notifyInfo.getNotifyStatusInfo();
			result = validateStatusInfo(status, notifyInfo);
			break;

		case NotifyTypeConstant.TYPE_INFRA:
			NotifyInfraInfo infra = notifyInfo.getNotifyInfraInfo();
			result = validateInfraInfo(infra, notifyInfo);
			break;

		default:
			break;
		}
		if (!result) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SELECT_INFO_ONE_OR_MORE.getMessage());
		}
	}

	public static boolean validateEventNotify(EventNotifyInfo eventNotifyInfo, Map<Integer, EventUserExtensionItemInfo> userExtenstionItemInfoMap) throws InvalidSetting, InvalidRole, HinemosUnknown {
		
		CommonValidator.validateNull("eventNotifyInfo", eventNotifyInfo);
		CommonValidator.validateString(MessageConstant.MONITOR_ID.getMessage(), 
				eventNotifyInfo.getMonitorId(), true, 0, 64);
		CommonValidator.validateString(MessageConstant.MONITOR_DETAIL_ID.getMessage(), 
				eventNotifyInfo.getMonitorDetail(), true, 0, 1024);
		CommonValidator.validateString(MessageConstant.PLUGIN_ID.getMessage(), 
				eventNotifyInfo.getPluginId(), true, 0, 64);
		CommonValidator.validateString(MessageConstant.FACILITY_ID.getMessage(), 
				eventNotifyInfo.getFacilityId(), true, 0, 512);
		CommonValidator.validateString(MessageConstant.SCOPE.getMessage(), 
				eventNotifyInfo.getScopeText(), true, 0, 512);
		CommonValidator.validateString(MessageConstant.APPLICATION.getMessage(), 
				eventNotifyInfo.getApplication(), true, 0, 64);
		CommonValidator.validateLong(MessageConstant.GENERATION_TIME.getMessage(), 
				eventNotifyInfo.getGenerationDate(), 0L, Long.MAX_VALUE);
		CommonValidator.validatePriority(MessageConstant.PRIORITY.getMessage(), 
				eventNotifyInfo.getPriority(), false);
		CommonValidator.validateConfirm(MessageConstant.CONFIRMED.getMessage(),
				eventNotifyInfo.getConfirmFlg());
		if (eventNotifyInfo.getOwnerRoleId() != null) {
			
			ArrayList<String> ownerRoleIdList = new AccessControllerBean().getOwnerRoleIdList();
			
			if (ownerRoleIdList == null || !ownerRoleIdList.contains(eventNotifyInfo.getOwnerRoleId())) {
				throw new InvalidSetting("invalid ownerRole.");
			}
		}
		
		for (int i = 1; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			validateUserExtensionItem(
					NotifyUtil.getUserItemValue(eventNotifyInfo, i), 
					userExtenstionItemInfoMap.get(i), i, false);
		}
		
		return true;
	}
	
	/**
	 * 
	 * @param value ????????????????????????
	 * @param itemInfo ??????????????????????????????????????????
	 * @param i ????????????????????????????????????index
	 * @param isValidate ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * @return
	 * @throws InvalidSetting
	 */
	public static boolean validateUserExtensionItem(String value, EventUserExtensionItemInfo itemInfo, Integer i, boolean isValidate) throws InvalidSetting {
		final int eventItemLength = 128;
		
		if (value == null) {
			//null???????????????????????????????????????????????????????????????????????????????????????????????????
			return true;
		}
		
		String itemName = EventHinemosPropertyUtil.getDisplayName(itemInfo.getDisplayName(), i);
		
		//??????????????????
		if (value.length() > eventItemLength) {
			throwInvalidSetting(MessageConstant.MESSAGE_INPUT_OVER_LIMIT.getMessage(itemName, String.valueOf(eventItemLength)));
		}
		
		if (!isValidate) {
			//??????????????????????????????????????????????????????
			return true;
		}
		
		//??????????????????
		if (itemInfo.getModifyRequired() && "".equals(value)) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(itemName));
		}
		
		if ("".equals(value)) {
			//???????????????????????????????????????????????????
			return true;
		}
		
		String validationType = itemInfo.getModifyValidation();
		String validateFormat = itemInfo.getModifyFormat();
		
		if (validationType == null || "".equals(validationType)) {
			//???????????????????????????????????????
			return true;
		}
		
		if (!EventHinemosPropertyConstant.isValidatonType(validationType)) {
			//???????????????????????????????????????????????????
			//????????????????????????????????????
			m_log.info(String.format(
					"invalid validation type. monitor.event.useritem.item%02d.modify.validation=%s",
					i, validationType));
			return true;
		}
		
		if (validateFormat == null || "".equals(validateFormat)) {
			//???????????????????????????????????????????????????????????????????????????
			m_log.info(String.format(
					"validation format not input. monitor.event.useritem.item%02d.modify.format=%s",
					i, validationType));
			return true;
		}
		
		if (EventHinemosPropertyConstant.USER_ITEM_VALIDATION_TYPE_REGEXP.equals(validationType)) {
			//?????????????????????
			validteUserItemRegExp(validateFormat, value, i, itemName);
			
		} else if (EventHinemosPropertyConstant.USER_ITEM_VALIDATION_TYPE_DATEFORMAT.equals(validationType)) {
			//?????????????????????
			validateUserItemDateFormat(validateFormat, value, i, itemName);
		}
		return true;
	}
	
	private static void validteUserItemRegExp(String validateFormat, String value, int index, String itemName) throws InvalidSetting {
		Pattern pattern = null;
		
		try {
			pattern = Pattern.compile(validateFormat);
		} catch (PatternSyntaxException e) {
		}
		
		if (pattern == null) {
			//????????????????????????????????????????????????????????????????????????????????????
			m_log.info(String.format(
					"not regexp format. monitor.event.useritem.item%02d.modify.format=%s",
					index, validateFormat));
			return;
		}
		
		//???????????????????????????
		if (!pattern.matcher(value).matches()) {
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EVENT_USER_EXNTENSION_ITEM_CORRECT_FORMAT.getMessage(itemName, validateFormat));
		}
	}
	
	private static void validateUserItemDateFormat(String validateFormat, String value, int index, String itemName) throws InvalidSetting {
		SimpleDateFormat sdf = null;
		
		try {
			sdf = new SimpleDateFormat(validateFormat);
		} catch (IllegalArgumentException e) {
		}
		
		if (sdf == null) {
			//????????????????????????????????????????????????????????????????????????????????????
			m_log.info(String.format(
					"not date format. monitor.event.useritem.item%02d.modify.format=%s",
					index, validateFormat));
			return;
		}
		
		try { 
			sdf.parse(value);
		} catch (ParseException e) {
			//??????????????????????????????????????????
			throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_EVENT_USER_EXNTENSION_ITEM_CORRECT_FORMAT.getMessage(itemName, validateFormat));
		}
	}

	
	private static boolean validateInfraInfo(NotifyInfraInfo info, NotifyInfo notifyInfo) throws InvalidSetting, InvalidRole {
		ArrayList<Integer> validFlgIndexes = NotifyUtil.getValidFlgIndexes(info);
		if (validFlgIndexes.isEmpty()) {
			return false;
		}

		if (info.getInfraExecFacilityFlg() == ExecFacilityConstant.TYPE_FIX) {
			// ??????????????????????????????????????????????????????
			if (info.getInfraExecFacility() == null) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE_NOTIFY.getMessage());
			}
			try {
				FacilityTreeCache.validateFacilityId(info.getInfraExecFacility(),
						notifyInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throwInvalidSetting(e);
			}
		}

		String[] infraIds = new String[] {
				info.getInfoInfraId(),
				info.getWarnInfraId(),
				info.getCriticalInfraId(),
				info.getUnknownInfraId()
		};
		for (int i = 0; i < validFlgIndexes.size(); i++) {
			int validFlgIndex = validFlgIndexes.get(i);
			if (isNullOrEmpty(infraIds[validFlgIndex])) {
				throwInvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFRA_MANAGEMENT_ID.getMessage());
			}

			// ??????ID????????????????????????????????????????????????????????????????????????
			InfraManagementValidator.validateInfraManagementId(infraIds[validFlgIndex], notifyInfo.getOwnerRoleId());
		}

		return true;
	}

	private static boolean validateStatusInfo(NotifyStatusInfo info,
			NotifyInfo notifyInfo) {
		return !NotifyUtil.getValidFlgIndexes(info).isEmpty();
	}
}
