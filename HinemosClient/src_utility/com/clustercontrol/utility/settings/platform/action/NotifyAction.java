/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.platform.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.notify.util.NotifyEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.difference.CSVUtil;
import com.clustercontrol.utility.difference.DiffUtil;
import com.clustercontrol.utility.difference.ResultA;
import com.clustercontrol.utility.settings.ClearMethod;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.DiffMethod;
import com.clustercontrol.utility.settings.ExportMethod;
import com.clustercontrol.utility.settings.ImportMethod;
import com.clustercontrol.utility.settings.SettingConstants;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.conv.CommonConv;
import com.clustercontrol.utility.settings.platform.conv.NotifyInfoConv;
import com.clustercontrol.utility.settings.platform.xml.Notify;
import com.clustercontrol.utility.settings.platform.xml.NotifyInfo;
import com.clustercontrol.utility.settings.platform.xml.NotifyType;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.notify.HinemosUnknown_Exception;
import com.clustercontrol.ws.notify.InvalidRole_Exception;
import com.clustercontrol.ws.notify.InvalidSetting_Exception;
import com.clustercontrol.ws.notify.InvalidUserPass_Exception;
import com.clustercontrol.ws.notify.NotifyDuplicate_Exception;

/**
 * ????????????????????????????????????????????????????????????????????????????????????????????????<br>
 *
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class NotifyAction {

	protected static Logger log = Logger.getLogger(NotifyAction.class);

	public NotifyAction() throws ConvertorException {
		super();
	}

	/**
	 * ????????????????????????????????????????????????<BR>
	 *
	 * @return ???????????????
	 */
	@ClearMethod
	public int clearNotify() {

		log.debug("Start Clear PlatformNotify ");

		int ret = 0;
		// ???????????????????????????
		List<com.clustercontrol.ws.notify.NotifyInfo> notifyInfoList = null;
		try {
			notifyInfoList = NotifyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNotifyList();
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformNotify (Error)");
			return ret;
		}

		// ?????????????????????????????????
		List<String> ids = new ArrayList<>();
		for (com.clustercontrol.ws.notify.NotifyInfo notifyInfo : notifyInfoList) {
			ids.add(notifyInfo.getNotifyId());
		}

		try {
			NotifyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteNotify(ids);
			log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + ids.toString());
		} catch (WebServiceException e) {
			ret = SettingConstants.ERROR_INPROCESS;
		} catch (Exception e) {
			log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear PlatformNotify ");
		return ret;
	}

	/**
	 * ??????????????????????????????????????????XML?????????????????????<BR>
	 *
	 * @param ????????????XML????????????
	 * @return ???????????????
	 */
	@ExportMethod
	public int exportNotify(String xmlFile) {

		log.debug("Start Export PlatformNotify ");

		// ???????????????(??????????????????????????????
		int ret = 0;

		// ???????????????????????????
		List<com.clustercontrol.ws.notify.NotifyInfo> notifyInfoList = null;
		try {
			notifyInfoList = NotifyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNotifyList();
			Collections.sort(notifyInfoList, new Comparator<com.clustercontrol.ws.notify.NotifyInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.notify.NotifyInfo info1,
						com.clustercontrol.ws.notify.NotifyInfo info2) {
					return info1.getNotifyId().compareTo(info2.getNotifyId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformNotify (Error)");
			return ret;
		}
		
		// ?????????????????????
		Notify notify = new Notify();
		for (com.clustercontrol.ws.notify.NotifyInfo notifyInfo : notifyInfoList) {
			try {
				notify.addNotifyInfo(NotifyInfoConv.convDto2XmlNotify(notifyInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + notifyInfo.getNotifyId());
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
			
		// XML?????????????????????
		try {
			notify.setCommon(CommonConv.versionPlatformDto2Xml(Config.getVersion()));
			// ??????????????????????????????
			notify.setSchemaInfo(NotifyInfoConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				notify.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Export PlatformNotify ");
		return ret;
	}

	/**
	 * XML????????????????????????????????????????????????<BR>
	 *
	 * @param ????????????XML????????????
	 * @return ???????????????
	 * @throws ConvertorException 
	 */
	@ImportMethod
	public int importNotify(String xmlNotify) throws ConvertorException {

		log.debug("Start Import PlatformNotify ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import PlatformNotify (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		// ???????????????(??????????????????????????????
		int ret = 0;
		NotifyType notifyInfoList = null;
		com.clustercontrol.ws.notify.NotifyInfo notifyInfo = null;

		// XML?????????????????????????????????
		try {
			notifyInfoList = Notify.unmarshal(new InputStreamReader(new FileInputStream(xmlNotify), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import PlatformNotify (Error)");
			return ret;
		}

		/*??????????????????????????????????????????*/
		if(!checkSchemaVersion(notifyInfoList.getSchemaInfo())){
			ret = SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		// ?????????????????????
		List<String> objectIdList = new ArrayList<String>();
		for (int i = 0; i < notifyInfoList.getNotifyInfoCount(); i++) {
			notifyInfo = NotifyInfoConv.convXml2DtoNotify(notifyInfoList.getNotifyInfo(i));
			try {
				NotifyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).addNotify(notifyInfo);
				objectIdList.add(notifyInfo.getNotifyId());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + notifyInfo.getNotifyId());
			} catch (NotifyDuplicate_Exception e) {
				//??????????????????????????????????????????????????????
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {notifyInfo.getNotifyId()};
					UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
				    ImportProcessMode.setProcesstype(dialog.open());
				    ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
			    	try {
						NotifyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).modifyNotify(notifyInfo);
						objectIdList.add(notifyInfo.getNotifyId());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + notifyInfo.getNotifyId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
			    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + notifyInfo.getNotifyId());
			    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			    	ret = SettingConstants.ERROR_INPROCESS;
			    	break;
			    }
			} catch (HinemosUnknown_Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidRole_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidRole") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidUserPass_Exception e) {
				log.error(Messages.getString("SettingTools.InvalidUserPass") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (InvalidSetting_Exception e) {
				log.warn(Messages.getString("SettingTools.InvalidSetting") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		//?????????????????????????????????????????????
		importObjectPrivilege(HinemosModuleConstant.PLATFORM_NOTIFY, objectIdList);
		
		//????????????
		checkDelete(notifyInfoList);
		
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		
		log.debug("End Import PlatformNotify ");
		return ret;
	}
	
	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????<BR>
	 * ???????????????????????????????????????????????????????????????logger????????????????????????????????????????????????
	 * 
	 * @param XML???????????????????????????
	 * @return ??????????????????
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.platform.xml.SchemaInfo schmaversion) {
		/*??????????????????????????????????????????*/
		int res = NotifyInfoConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.platform.xml.SchemaInfo sci = NotifyInfoConv.getSchemaVersion();
		
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	/**
	 * ????????????????????????????????????
	 * XML?????????????????????filePath1,filePath2?????????????????????
	 * ????????????????????????????????????????????????????????????????????????
	 * ??????????????????????????????????????????
	 * 				   ??????????????????????????????????????????????????????????????????????????????????????????
	 *
	 * @param filePath1 XML???????????????
	 * @param filePath2 XML???????????????
	 * @return ???????????????
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String filePath1, String filePath2) throws ConvertorException {
		log.debug("Start Differrence PlatformNotify ");

		// ???????????????(??????????????????????????????
		int ret = 0;

		Notify notify1 = null;
		Notify notify2 = null;

		// XML?????????????????????????????????
		try {
			notify1 = (Notify) Notify.unmarshal(new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			notify2 = (Notify) Notify.unmarshal(new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(notify1);
			sort(notify2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformNotify (Error)");
			return ret;
		}
		/*??????????????????????????????????????????*/
		if(!checkSchemaVersion(notify1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(notify2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//?????????????????????
			boolean diff = DiffUtil.diffCheck2(notify1, notify2, Notify.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//???????????????????????????????????????????????????
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(filePath2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//??????????????????????????????????????????????????????????????????????????????????????????
			else {
				File f = new File(filePath2 + ".csv");
				if (f.exists()) {
					if (!f.delete())
						log.warn(String.format("Fail to delete file. %s", f.getAbsolutePath()));
				}
			}
		}
		catch (Exception e) {
			getLogger().error("unexpected: ", e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		
		// ???????????????
		if ((ret >= SettingConstants.SUCCESS) && (ret<=SettingConstants.SUCCESS_MAX)){
			log.info(Messages.getString("SettingTools.DiffCompleted"));
		}else{
			log.error(Messages.getString("SettingTools.EndWithErrorCode") );
		}

		getLogger().debug("End Differrence PlatformNotify");

		return ret;
	}
	
	private void sort(Notify notify) {
		NotifyInfo[] infoList = notify.getNotifyInfo();
		Arrays.sort(
			infoList,
			new Comparator<NotifyInfo>() {
				@Override
				public int compare(NotifyInfo info1, NotifyInfo info2) {
					return info1.getNotifyId().compareTo(info2.getNotifyId());
				}
			});
		 notify.setNotifyInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}
	

	protected void checkDelete(NotifyType xmlElements){
		List<com.clustercontrol.ws.notify.NotifyInfo> subList = null;
		try {
			subList = NotifyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getNotifyList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<NotifyInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getNotifyInfo()));
		for(com.clustercontrol.ws.notify.NotifyInfo mgrInfo: new ArrayList<>(subList)){
			for(NotifyInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getNotifyId().equals(xmlElement.getNotifyId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.notify.NotifyInfo info: subList){
				//?????????????????????????????????????????????????????????????????????????????????????????????
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getNotifyId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		List<String> args = new ArrayList<>();
			    		args.add(info.getNotifyId());
			    		NotifyEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteNotify(args);
			    		getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getNotifyId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getNotifyId());
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
			    	return;
			    }
			}
		}
	}
	
	/**
	 * ?????????????????????????????????????????????
	 * 
	 * @param objectType
	 * @param objectIdList
	 */
	/**
	 * @param objectType
	 * @param objectIdList
	 */
	protected void importObjectPrivilege(String objectType, List<String> objectIdList){
		if(ImportProcessMode.isSameObjectPrivilege()){
			ObjectPrivilegeAction.importAccessExtraction(
					ImportProcessMode.getXmlObjectPrivilege(),
					objectType,
					objectIdList,
					getLogger());
		}
	}
}
