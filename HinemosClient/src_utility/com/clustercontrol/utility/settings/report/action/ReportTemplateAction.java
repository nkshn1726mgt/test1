/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.report.action;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
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
import com.clustercontrol.utility.settings.report.conv.ReportTemplateConv;
import com.clustercontrol.utility.settings.report.xml.ReportTemplate;
import com.clustercontrol.utility.settings.report.xml.ReportTemplateType;
import com.clustercontrol.utility.settings.report.xml.TemplateInfo;
import com.clustercontrol.utility.settings.ui.dialog.DeleteProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityProcessDialog;
import com.clustercontrol.utility.settings.ui.dialog.UtilityDialogInjector;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.util.Config;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.reporting.HinemosUnknown_Exception;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;
import com.clustercontrol.ws.reporting.InvalidUserPass_Exception;
import com.clustercontrol.ws.reporting.ReportingDuplicate_Exception;
import com.clustercontrol.ws.reporting.ReportingNotFound_Exception;


/**
 * 
 * @param action ??????
 * @param XML?????????????????????????????????????????????????????????
 * 
 * @version 6.1.0
 * @since 6.0.0
 * 
 * 
 */
public class ReportTemplateAction {

	/* ????????? */
	protected static Logger log = Logger.getLogger(ReportTemplateAction.class);

	public ReportTemplateAction() throws ConvertorException {
		super();
	}
	
	/**
	 * ??????????????????????????????????????????????????????????????????????????????<BR>
	 * 
	 * @since 6.0
	 * @return ???????????????
	 */
	@ClearMethod
	public int clearReportTemplate(){
		
		log.debug("Start Clear ReportTemplate ");
		int ret = 0;

		List<com.clustercontrol.ws.reporting.TemplateSetInfo> templateSetInfoList =null;
		
		try {
			templateSetInfoList = ReportingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getTemplateSetInfoList(null);
		} catch (com.clustercontrol.ws.reporting.HinemosUnknown_Exception
				| com.clustercontrol.ws.reporting.InvalidRole_Exception
				| com.clustercontrol.ws.reporting.InvalidUserPass_Exception | ReportingNotFound_Exception e) {
			log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		for (com.clustercontrol.ws.reporting.TemplateSetInfo templateSetInfo:templateSetInfoList){
			try {
				ReportingEndpointWrapper
				.getWrapper(UtilityManagerUtil.getCurrentManagerName())
				.deleteTemplateSet(templateSetInfo.getTemplateSetId());
			} catch (com.clustercontrol.ws.reporting.HinemosUnknown_Exception
					| com.clustercontrol.ws.reporting.InvalidRole_Exception
					| com.clustercontrol.ws.reporting.InvalidUserPass_Exception | ReportingNotFound_Exception e) {
				log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				return ret;
			}
		}
		
		// ???????????????
		log.info(Messages.getString("ReportTemplate.ClearCompleted"));
		
		log.debug("End Clear ReportTemplate");
		return ret;
	}
	/**
	 *	?????????????????????????????????????????????????????????
	 * @return
	 */
	@ExportMethod
	public int exportReportTemplate(String xmlFile) {

		log.debug("Start Export Reporting Template ");

		int ret = 0;
		ReportingEndpointWrapper wrapper =
				ReportingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
		
		List<com.clustercontrol.ws.reporting.TemplateSetInfo> templateSetInfoList =null;
		
		try {
			templateSetInfoList = wrapper.getTemplateSetInfoList(null);
		} catch (com.clustercontrol.ws.reporting.HinemosUnknown_Exception
				| com.clustercontrol.ws.reporting.InvalidRole_Exception
				| com.clustercontrol.ws.reporting.InvalidUserPass_Exception | ReportingNotFound_Exception e) {
			log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			ret = SettingConstants.ERROR_INPROCESS;
			return ret;
		}
		
		ReportTemplate reportTemplate = new ReportTemplate();
		
		for (com.clustercontrol.ws.reporting.TemplateSetInfo info : templateSetInfoList) {
			try{
				info = wrapper.getTemplateSetInfo(info.getTemplateSetId());
				reportTemplate.addTemplateInfo(ReportTemplateConv.getTemplate(info));
				log.info(Messages.getString("SettingTools.ExportSucceeded") + " : " + info.getTemplateSetId());
			} catch (WebServiceException e) {
				log.error(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
				break;
			} catch (Exception e) {
				log.warn(Messages.getString("SettingTools.ExportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		// XML?????????????????????
		try {
			reportTemplate.setCommon(ReportTemplateConv.versionReportDto2Xml(Config.getVersion()));
			// ??????????????????????????????
			reportTemplate.setSchemaInfo(ReportTemplateConv.getSchemaVersion());
			try(FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");){
				reportTemplate.marshal(osw);
			}
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.MarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
		}
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ExportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Export Report Template ");
		return ret;
	}
	
	/**
	 * ???????????????????????????????????????????????????????????????
	 * 
	 * @return
	 */
	@ImportMethod
	public int importReportTemplate(String xmlFile){
		log.debug("Start Import Report Template ");

		if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
			getLogger().info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
			getLogger().debug("End Import Report.Template (Cancel)");
			return SettingConstants.ERROR_INPROCESS;
		}
		
		int ret = 0;
		
		// XML?????????????????????????????????
		ReportTemplateType template = null;
		try {
			template = ReportTemplate.unmarshal(new InputStreamReader(new FileInputStream(xmlFile), "UTF-8"));
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"),e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Import Report.Template (Error)");
			return ret;
		}
		/*??????????????????????????????????????????*/
		if(!checkSchemaVersion(template.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		for (TemplateInfo info : template.getTemplateInfo()) {
			com.clustercontrol.ws.reporting.TemplateSetInfo templateSetInfo = null;
			try {
				templateSetInfo = ReportTemplateConv.getTemplateInfoDto(info);
				ReportingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.addTemplateSet(templateSetInfo);
				log.info(Messages.getString("SettingTools.ImportSucceeded") + " : " + info.getTemplateSetId());
			} catch (ReportingDuplicate_Exception e) {
				//??????????????????????????????????????????????????????
				if(!ImportProcessMode.isSameprocess()){
					String[] args = {info.getTemplateSetId()};
					UtilityProcessDialog dialog = UtilityDialogInjector.createImportProcessDialog(
							null, Messages.getString("message.import.confirm2", args));
					ImportProcessMode.setProcesstype(dialog.open());
					ImportProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.UPDATE){
					try {
						ReportingEndpointWrapper
							.getWrapper(UtilityManagerUtil.getCurrentManagerName())
							.modifyTemplateSet(templateSetInfo);
						log.info(Messages.getString("SettingTools.ImportSucceeded.Update") + " : " + info.getTemplateSetId());
					} catch (Exception e1) {
						log.warn(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
						ret = SettingConstants.ERROR_INPROCESS;
					}
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Skip") + " : " + info.getTemplateSetId());
				} else if(ImportProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					log.info(Messages.getString("SettingTools.ImportSucceeded.Cancel"));
					ret = SettingConstants.ERROR_INPROCESS;
					return ret;
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
			} catch (javax.xml.ws.WebServiceException e){
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				throw e;//????????????
			} catch (Exception e) {
				log.error(Messages.getString("SettingTools.ImportFailed") + " : " + HinemosMessage.replace(e.getMessage()));
				ret = SettingConstants.ERROR_INPROCESS;
			}
		}
		
		checkDelete(template);
		
		// ???????????????
		if (ret == 0) {
			log.info(Messages.getString("SettingTools.ImportCompleted"));
		} else {
			log.error(Messages.getString("SettingTools.EndWithErrorCode"));
		}
		log.debug("End Import Report.Template ");
		
		return ret;
	}
	
	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????<BR>
	 * ???????????????????????????????????????????????????????????????logger????????????????????????????????????????????????
	 * 
	 * @param XML???????????????????????????
	 * @return ??????????????????
	 */
	private boolean checkSchemaVersion(com.clustercontrol.utility.settings.report.xml.SchemaInfo schmaversion) {
		/*??????????????????????????????????????????*/
		int res = ReportTemplateConv.checkSchemaVersion(schmaversion.getSchemaType(),
					schmaversion.getSchemaVersion(),
					schmaversion.getSchemaRevision());
		com.clustercontrol.utility.settings.report.xml.SchemaInfo sci = ReportTemplateConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(log, res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}
	
	protected void checkDelete(ReportTemplateType xmlElements){
		List<com.clustercontrol.ws.reporting.TemplateSetInfo> subList =null;

		try {
			subList = ReportingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getTemplateSetInfoList(null);
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception
				| ReportingNotFound_Exception e) {
			log.error(Messages.getString("ReportTemplate.ClearFailed") + " : " + HinemosMessage.replace(e.getMessage()));
			return ;
		}

		if(subList == null || subList.size() <= 0){
			return;
		}
		
		List<TemplateInfo> xmlElementList = new ArrayList<>(Arrays.asList(xmlElements.getTemplateInfo()));
		for(com.clustercontrol.ws.reporting.TemplateSetInfo mgrInfo: new ArrayList<>(subList)){
			for(TemplateInfo xmlElement: new ArrayList<>(xmlElementList)){
				if(mgrInfo.getTemplateSetId().equals(xmlElement.getTemplateSetId())){
					subList.remove(mgrInfo);
					xmlElementList.remove(xmlElement);
					break;
				}
			}
		}

		if(subList.size() > 0){
			for(com.clustercontrol.ws.reporting.TemplateSetInfo info: subList){
				//?????????????????????????????????????????????????????????????????????????????????????????????
				if(!DeleteProcessMode.isSameprocess()){
					String[] args = {info.getTemplateSetId()};
					DeleteProcessDialog dialog = UtilityDialogInjector.createDeleteProcessDialog(
							null, Messages.getString("message.delete.confirm4", args));
					DeleteProcessMode.setProcesstype(dialog.open());
					DeleteProcessMode.setSameprocess(dialog.getToggleState());
				}
				
				if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.DELETE){
					try {
						ReportingEndpointWrapper
							.getWrapper(UtilityManagerUtil.getCurrentManagerName())
							.deleteTemplateSet(info.getTemplateSetId());
						getLogger().info(Messages.getString("SettingTools.SubSucceeded.Delete") + " : " + info.getTemplateSetId());
					} catch (Exception e1) {
						getLogger().warn(Messages.getString("SettingTools.ClearFailed") + " : " + HinemosMessage.replace(e1.getMessage()));
					}
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.SKIP){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Skip") + " : " + info.getTemplateSetId());
				} else if(DeleteProcessMode.getProcesstype() == UtilityDialogConstant.CANCEL){
					getLogger().info(Messages.getString("SettingTools.SubSucceeded.Cancel"));
					return;
				}
			}
		}
	}

	/**
	 * ????????????????????????????????????
	 * XML?????????????????????filePath1,filePath2?????????????????????
	 * ????????????????????????????????????????????????????????????????????????
	 * ??????????????????????????????????????????
	 * 				   ??????????????????????????????????????????????????????????????????????????????????????????
	 *
	 * @param xmlFile1 XML???????????????
	 * @param xmlFile2 XML???????????????
	 * @return ???????????????
	 * @throws ConvertorException
	 */
	@DiffMethod
	public int diffXml(String xmlFile1, String xmlFile2) throws ConvertorException {
		log.debug("Start Differrence Report Template ");

		int ret = 0;
		// XML?????????????????????????????????
		ReportTemplateType report1 = null;
		ReportTemplateType report2 = null;
		try {
			report1 = ReportTemplateType.unmarshal(new InputStreamReader(new FileInputStream(xmlFile1), "UTF-8"));
			report2 = ReportTemplateType.unmarshal(new InputStreamReader(new FileInputStream(xmlFile2), "UTF-8"));
			sort(report1);
			sort(report2);
		} catch (Exception e) {
			log.error(Messages.getString("SettingTools.UnmarshalXmlFailed"), e);
			ret = SettingConstants.ERROR_INPROCESS;
			log.debug("End Differrence Report Template (Error)");
			return ret;
		}

		/*??????????????????????????????????????????*/
		if(!checkSchemaVersion(report1.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		if(!checkSchemaVersion(report2.getSchemaInfo())){
			ret=SettingConstants.ERROR_SCHEMA_VERSION;
			return ret;
		}

		FileOutputStream fos = null;
		try {
			ResultA resultA = new ResultA();
			//?????????????????????
			boolean diff = DiffUtil.diffCheck2(report1, report2, ReportTemplateType.class, resultA);
			assert resultA.getResultBs().size() == 1;
			
			if(diff){
				ret += SettingConstants.SUCCESS_DIFF_1;
			}
			//???????????????????????????????????????????????????
			if (diff || DiffUtil.isAll()) {
				CSVUtil.CSVSerializer csvSerializer = CSVUtil.createCSVSerializer();
				fos = new FileOutputStream(xmlFile2 + ".csv");
				csvSerializer.write(fos, resultA.getResultBs().values().iterator().next());
			}
			//??????????????????????????????????????????????????????????????????????????????????????????
			else {
				File f = new File(xmlFile2 + ".csv");
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
		getLogger().debug("End Differrence Report Template");

		return ret;
	}
	
	private void sort(ReportTemplateType template) {
		TemplateInfo[] infoList = template.getTemplateInfo();
		Arrays.sort(infoList,
			new Comparator<TemplateInfo>() {
				@Override
				public int compare(TemplateInfo info1, TemplateInfo info2) {
					return info1.getTemplateSetId().compareTo(info2.getTemplateSetId());
				}
			});
		template.setTemplateInfo(infoList);
	}
	
	public Logger getLogger() {
		return log;
	}
}
