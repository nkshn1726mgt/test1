/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.system.action;

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

import com.clustercontrol.maintenance.util.MaintenanceEndpointWrapper;
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
import com.clustercontrol.utility.settings.maintenance.xml.Maintenance;
import com.clustercontrol.utility.settings.maintenance.xml.MaintenanceInfo;
import com.clustercontrol.utility.settings.maintenance.xml.MaintenanceType;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.platform.action.ObjectPrivilegeAction;
import com.clustercontrol.utility.settings.system.conv.MaintenanceConv;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.maintenance.HinemosUnknown_Exception;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;
import com.clustercontrol.ws.maintenance.InvalidSetting_Exception;
import com.clustercontrol.ws.maintenance.InvalidUserPass_Exception;
import com.clustercontrol.ws.maintenance.MaintenanceDuplicate_Exception;

/**
 * ????????????????????????????????????????????????????????????????????????????????????????????????????????????<br>
 * 
 * @version 6.1.0
 * @since 1.0.0
 * 
 */
public class MaintenanceAction {

	protected static Logger log = Logger.getLogger(MaintenanceAction.class);

	public MaintenanceAction() throws ConvertorException {
		super();
	}
	
	/**
	 * ????????????????????????????????????????????????<BR>
	 * 
	 * @return ???????????????
	 */
	@ClearMethod
	public int clearMaintenance() {

		log.debug("Start Clear PlatformMaintenance ");

		// ???????????????(??????????????????????????????
		int ret = 0;

		// ???????????????????????????????????????
		List<com.clustercontrol.ws.maintenance.MaintenanceInfo> maintenanceInfoList = null;
		try {
			maintenanceInfoList = MaintenanceEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMaintenanceList();
			Collections.sort(maintenanceInfoList, new Comparator<com.clustercontrol.ws.maintenance.MaintenanceInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.maintenance.MaintenanceInfo info1,
						com.clustercontrol.ws.maintenance.MaintenanceInfo info2) {
					return info1.getMaintenanceId().compareTo(info2.getMaintenanceId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Clear PlatformMaintenance (Error)");
			return ret;
		}

		// ?????????????????????????????????
		for (com.clustercontrol.ws.maintenance.MaintenanceInfo maintenanceInfo : maintenanceInfoList) {
			try {
				MaintenanceEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMaintenance(maintenanceInfo.getMaintenanceId());
				log.info(Messages.getString("SettingTools.ClearSucceeded") + " : " + maintenanceInfo.getMaintenanceId());
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ClearCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Clear PlatformMaintenance ");
		return ret;

	}

	/**
	 * ??????????????????????????????????????????XML?????????????????????<BR>
	 * 
	 * @param ????????????XML????????????
	 * @return ???????????????
	 */
	@ExportMethod
	public int exportMaintenance(String xmlFile) {

		log.debug("Start Export PlatformMaintenance ");

		// ???????????????(??????????????????????????????
		int ret = 0;

		// ???????????????????????????????????????
		List<com.clustercontrol.ws.maintenance.MaintenanceInfo> maintenanceInfoList = null;
		try {
			maintenanceInfoList = MaintenanceEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMaintenanceList();
			Collections.sort(maintenanceInfoList, new Comparator<com.clustercontrol.ws.maintenance.MaintenanceInfo>() {
				@Override
				public int compare(
						com.clustercontrol.ws.maintenance.MaintenanceInfo info1,
						com.clustercontrol.ws.maintenance.MaintenanceInfo info2) {
					return info1.getMaintenanceId().compareTo(info2.getMaintenanceId());
				}
			});
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Export PlatformMaintenance (Error)");
			return ret;
		}

		// ?????????????????????????????????
		Maintenance maintenance = new Maintenance();
		for (com.clustercontrol.ws.maintenance.MaintenanceInfo maintenanceInfo : maintenanceInfoList) {
			try {
				maintenance.addMaintenanceInfo(MaintenanceConv.getMaintenanceInfo(maintenanceInfo));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + maintenanceInfo.getMaintenanceId());
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		// XML?????????????????????
		try {
			maintenance.setCommon(com.clustercontrol.utility.settings.platform.conv.CommonConv.versionMaintenanceDto2Xml(Config.getVersion()));
			maintenance.setSchemaInfo(MaintenanceConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				maintenance.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export PlatformMaintenance ");
		return ret;
	}

	/**
	 * XML????????????????????????????????????????????????<BR>
	 * 
	 * @param ????????????XML????????????
	 * @return ???????????????
	 */
	@ImportMethod
	public int importMaintenance(String xmlFile) {

		log.debug("Start Import PlatformMaintenance ");
		
		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
	    	getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
	    	getLogger().debug("End Import PlatformMaintenance (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
	    }
		
		// ???????????????(??????????????????????????????
		int ret = 0;
		MaintenanceType maintenance = null;
		// XML?????????????????????????????????
		try {
			maintenance = Maintenance.unmarshal(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Inport PlatformMaintenance (Error)");
			return ret;
		}
		
		/*??????????????????????????????????????????*/
		if(!this.checkSchemaVersion(maintenance.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		
		// ?????????????????????????????????
		List<String> objectIdList = new ArrayList<String>();
		for (MaintenanceInfo info : maintenance.getMaintenanceInfo()) {
			com.clustercontrol.ws.maintenance.MaintenanceInfo dto = null;
			try {
				dto = MaintenanceConv.getMaintenanceInfoData(info);
				MaintenanceEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).addMaintenance(dto);
				objectIdList.add(info.getMaintenanceId());
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + info.getMaintenanceId());
			} catch (MaintenanceDuplicate_Exception e) {
				//??????????????????????????????????????????????????????
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {info.getMaintenanceId()};
					UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
				    ImportProcessMode.setProcesstype(dialog.open());
				    ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
			    	try {
			    		MaintenanceEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).modifyMaintenance(dto);
			    		objectIdList.add(info.getMaintenanceId());
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + info.getMaintenanceId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
			    } else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + info.getMaintenanceId());
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
		importObjectPrivilege(HinemosModuleConstant.SYSYTEM_MAINTENANCE, objectIdList);
		
		//????????????
		checkDelete(maintenance);
		
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Inport PlatformMaintenance ");
		return ret;
	}
	
	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????<BR>
	 * ???????????????????????????????????????????????????????????????logger????????????????????????????????????????????????
	 * 
	 * @param XML???????????????????????????
	 * @return ??????????????????
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo schmaversion) {
		/*??????????????????????????????????????????*/
		int res = MaintenanceConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.maintenance.xml.SchemaInfo sci = MaintenanceConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
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

		log.debug("Start Differrence PlatformMaintenance ");

		// ???????????????(??????????????????????????????
		int ret = 0;

		Maintenance maintenance1 = null;
		Maintenance maintenance2 = null;

		// XML?????????????????????????????????
		try {
			maintenance1 = (Maintenance) Maintenance.unmarshal(new InputStreamReader(new FileInputStream(filePath1), "UTF-8"));
			maintenance2 = (Maintenance) Maintenance.unmarshal(new InputStreamReader(new FileInputStream(filePath2), "UTF-8"));
			sort(maintenance1);
			sort(maintenance2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret=SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence PlatformMaintenance (Error)");
			return ret;
		}

		/*??????????????????????????????????????????*/
		if(!checkSchemaVersion(maintenance1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		if(!checkSchemaVersion(maintenance2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}
		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//?????????????????????
			boolean diff = DiffUtil.diffCheck2(maintenance1, maintenance2, Maintenance.class, resultA);
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
		getLogger().debug("End Differrence PlatformMaintenance");

		return ret;
	}
	
	private void sort(Maintenance maintenance) {
		MaintenanceInfo[] infoList = maintenance.getMaintenanceInfo();
		Arrays.sort(
			infoList,
			new Comparator<MaintenanceInfo>() {
				@Override
				public int compare(MaintenanceInfo info1, MaintenanceInfo info2) {
					return info1.getMaintenanceId().compareTo(info2.getMaintenanceId());
				}
			});
		 maintenance.setMaintenanceInfo(infoList);
	}

	public Logger getLogger() {
		return log;
	}

	protected void checkDelete(MaintenanceType xmlElements){
		List<com.clustercontrol.ws.maintenance.MaintenanceInfo> subList = null;
		try {
			subList = MaintenanceEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMaintenanceList();
		}
		catch (Exception e) {
			getLogger().error(Messages.getString("SettingTools.FailToGetList") + " : " + HinemosMessage.replace(e.getMessage()));
			getLogger().debug(e.getMessage(), e);
		}
		
		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<MaintenanceInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getMaintenanceInfo()));
		for(com.clustercontrol.ws.maintenance.MaintenanceInfo mgrInfo: new ArrayList<>(subList)){
			for(MaintenanceInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getMaintenanceId().equals(xmlElement.getMaintenanceId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}
		
		if(subList.size() > 0){
			for(com.clustercontrol.ws.maintenance.MaintenanceInfo info: subList){
				//?????????????????????????????????????????????????????????????????????????????????????????????
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getMaintenanceId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
			    
			    if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
			    	try {
			    		MaintenanceEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).deleteMaintenance(info.getMaintenanceId());
			    		getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getMaintenanceId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
			    } else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
			    	getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getMaintenanceId());
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
