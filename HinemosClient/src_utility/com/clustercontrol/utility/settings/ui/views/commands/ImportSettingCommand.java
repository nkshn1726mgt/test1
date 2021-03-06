/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.ui.views.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.client.ui.util.FileDownloader;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.Messages;
import com.clustercontrol.utility.constant.HinemosModuleConstant;
import com.clustercontrol.utility.settings.ui.action.CommandAction;
import com.clustercontrol.utility.settings.ui.action.ReadXMLAction;
import com.clustercontrol.utility.settings.ui.bean.FuncInfo;
import com.clustercontrol.utility.settings.ui.dialog.JobunitListDialog;
import com.clustercontrol.utility.settings.ui.preference.SettingToolsXMLPreferencePage;
import com.clustercontrol.utility.settings.ui.util.BackupUtil;
import com.clustercontrol.utility.settings.ui.util.DeleteProcessMode;
import com.clustercontrol.utility.settings.ui.util.ImportProcessMode;
import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;
import com.clustercontrol.utility.ui.dialog.MessageDialogWithCheckbox;
import com.clustercontrol.utility.ui.settings.dialog.UtilityImportCommandDialog;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.FileUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
import com.clustercontrol.utility.util.UtilityDialogConstant;
import com.clustercontrol.utility.util.UtilityEndpointWrapper;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.utility.util.ZipUtil;
import com.clustercontrol.ws.utility.HinemosUnknown_Exception;
import com.clustercontrol.ws.utility.InvalidRole_Exception;
import com.clustercontrol.ws.utility.InvalidUserPass_Exception;

/**
 * ??????????????????????????????????????????????????????????????????<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 */
public class ImportSettingCommand extends AbstractHandler implements IElementUpdater {
	
	private Log log = LogFactory.getLog(getClass());
	/** ???????????????ID */
	public static final String ID = ImportSettingCommand.class.getName();
	private IWorkbenchWindow window;
	/** ????????? */
	private IWorkbenchPart viewPart;

	private final int CANCEL = -1;
	private final int OK_WITHOUT_OBJECT_PRIVILEGE = 1;
	private final int OK_WITH_OBJECT_PRIVILEGE = 2;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/** ???????????????????????? */
	private List<FuncInfo> m_funcList = null;
	
	private String m_successList = "";
	private String m_failureList = "";
	private CommandAction m_action = null;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// key????????????
		try {
			UtilityEndpointWrapper wrapper = UtilityEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName());
			String version = wrapper.getVersion();
			if (version.length() > 7) {
				boolean result = Boolean.valueOf(version.substring(7, version.length()));
				if (!result) {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.expiration.term.invalid"));
				}
			}
		} catch (HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
			return null;
		} catch (Exception e) {
			// ?????????????????????????????????????????????????????????????????????????????????
			// Key file not found. This process will be terminated.
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.expiration.term"));
			return null;
		}
		
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// ???????????????????????????
		this.viewPart = HandlerUtil.getActivePart(event);
		ImportExportExecView listView = (ImportExportExecView) viewPart.getSite().getPage().findView(ImportExportExecView.ID);
		if (null == listView){
			return null;
		}
		
		// ????????????????????????????????????????????????????????????
		List<FuncInfo> checkedList = listView.getCheckedFunc();
		
		// ??????????????????????????????
		// ???????????????????????????????????????????????????????????????????????????????????????
		m_funcList = new ArrayList<FuncInfo>();

		String parentPath = MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML);
		String tmpPath = null;

		ClientPathUtil pathUtil = ClientPathUtil.getInstance();
		String filePath = null;
		
		if(pathUtil.lock(parentPath)){
			filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
		} else {
			//#3660
			//??????????????????????????????OOME???????????????unlock????????????????????????????????????????????????????????????????????????
			//????????????????????????????????????????????????????????????
			pathUtil.unlock(parentPath);
			if(pathUtil.lock(parentPath)) {
				filePath = MultiManagerPathUtil.getDirectoryPath(SettingToolsXMLPreferencePage.KEY_XML);
			}
		}
		
		if (checkedList.size() == 0) {
			 MessageDialog.openError(null,
					 Messages.getString("message.error"),
					 Messages.getString("message.import.error1"));
		} else {
			
			UtilityImportCommandDialog dialog = new UtilityImportCommandDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			if(dialog.open() != Window.OK){
				return null;
			} else {
				tmpPath = dialog.getFilePath();
			}
			
			// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
			Iterator<FuncInfo> it = checkedList.iterator();
			boolean masterFlg = false;
			boolean otherFlg = false;
			boolean withObjectprivilege = false;
			final String objectPrivilegeXml = ReadXMLAction.getXMLFile(listView.getObjectPrivilegeFunc().getDefaultXML()).get(0);
			// boolean includedObjectPrivilage = false;
			while(it.hasNext()) {
				FuncInfo info = it.next();
				if(info.getId().matches("^"+HinemosModuleConstant.MASTER+".*")){
					masterFlg = true;
				}
				else {
					otherFlg = true;
				}
				
				// ???????????????????????????????????????????????????????????????????????????
				if(masterFlg && otherFlg) {
					
					MessageDialog.openWarning(
							null,
							Messages.getString("message.warning"),
							Messages.getString("message.import.error3"));
					if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
						ClientPathUtil.getInstance().unlock(parentPath);
					}
					return null;
				}
				
				// ???????????????????????????????????????????????????????????????????????????????????????
				if(!info.getObjectType().isEmpty()){
					withObjectprivilege = true;
				}
			}
			
			
			// ??????????????????????????????????????????????????????????????????
			List<String> xmlFiles = null;
			it = checkedList.iterator();
			StringBuffer funcNameList = new StringBuffer();
			while(it.hasNext()) {
				
				// ????????????????????????????????????OK?????????????????????????????????????????????
				// ver.3.2, ver4.0??????????????????????????????????????????????????????
				boolean isTargetSelected = true;
				
				FuncInfo info = it.next();
				
				//???????????????????????????????????????
				if (info.getId().equals(HinemosModuleConstant.JOB_MST)) {
					String fileName = ReadXMLAction.getXMLFile(info.getDefaultXML()).get(0);
					File xmlFile = new File(fileName);
					if(!xmlFile.exists()) {
						// ??????????????????????????????????????????
						String[] args = { xmlFile.getName(), info.getName()};
						MessageDialog.openError(
								null,
								Messages.getString("message.confirm"),
								Messages.getString("message.import.error2",args));
						if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
							ClientPathUtil.getInstance().unlock(parentPath);
						}
						return null;
					}
					// ???????????????????????????????????????????????????
					JobunitListDialog jobunitListDialog = new JobunitListDialog(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"import", fileName);
					
					// OK?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????ID????????????
					if (jobunitListDialog.open() == IDialogConstants.OK_ID) {
						if (jobunitListDialog.getSelectionData() != null && jobunitListDialog.getSelectionData().size() != 0) {
							info.setTargetIdList(jobunitListDialog.getSelectionData());
						} else {
							isTargetSelected = false;
						}
						
					// ????????????????????????????????????????????????????????????????????????????????????????????????ID?????????????????????
					} else {
						info.setTargetIdList(null);
						isTargetSelected = false;
					}
				}
				
				// ??????????????????????????????????????????XML???????????????
				if (isTargetSelected) {
					funcNameList.append(" ");
					funcNameList.append(info.getName());
					funcNameList.append("\n");
					
					// ???????????????????????????????????????
					m_funcList.add(info);
					
					xmlFiles = ReadXMLAction.getXMLFile(info.getDefaultXML());
					
					Iterator<String> itrFilePath = xmlFiles.iterator();
					while(itrFilePath.hasNext()) {
						
						File xmlFile = new File(itrFilePath.next());
						if(!xmlFile.exists()) {
							// ?????????????????????????????????????????????????????????????????????????????????
							if (!ReadXMLAction.isRequiredImportFile(xmlFile.getName())) {
								log.debug(xmlFile.getName() + " is not required when import.");
								continue;
							}
							String[] args = { xmlFile.getName(), info.getName()};
							MessageDialog.openError(
									null,
									Messages.getString("message.confirm"),
									Messages.getString("message.import.error2",args));
							if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
								ClientPathUtil.getInstance().unlock(parentPath);
							}
							return null;
						}
					}
				}
			}
			
			ImportProcessMode.setProcesstype(0);
			ImportProcessMode.setSameprocess(false);
			ImportProcessMode.setObjectPrivilege(false);
			ImportProcessMode.setXmlObjectPrivilege(objectPrivilegeXml);
			DeleteProcessMode.setProcesstype(0);
			DeleteProcessMode.setSameprocess(false);
			IMPORT:if(m_funcList.size() != 0){
				if (withObjectprivilege ){
					int status = showComfirmWithObjectPrivilege(funcNameList.toString());
					switch(status){
					case CANCEL:
						break IMPORT;
					case OK_WITH_OBJECT_PRIVILEGE:
						ImportProcessMode.setObjectPrivilege(true);
						File xmlFile = new File(objectPrivilegeXml);
						if(!xmlFile.exists()) {
							String[] args = { xmlFile.getName(), listView.getObjectPrivilegeFunc().getName()};
							MessageDialog.openError(
									null,
									Messages.getString("message.confirm"),
									Messages.getString("message.import.error2",args));
							if(tmpPath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
								ClientPathUtil.getInstance().unlock(parentPath);
							}
							return null;
						}
						break;
					}
				}else if (!showComfirmDialog(funcNameList.toString())){
					break IMPORT;
				}
				// ??????????????????????????????
				try {
					IRunnableWithProgress op = new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
				
							List<String> defaultFileList = null;
							List<String> fileList = null;

							// ??????????????????????????????
							FuncInfo[] obj = m_funcList.toArray(new FuncInfo[0]);
							Arrays.sort(obj, new Comparator<FuncInfo>() {
										/**
										 * ???????????????????????????????????????????????????????????????Comparator?????????
										 * 
										 */
										@Override
										public int compare(FuncInfo o1, FuncInfo o2){
											return o1.getWeight() - o2.getWeight();
										}
									});

							List<FuncInfo> importOrderList = new ArrayList<FuncInfo>();
							for(int i=0; i<obj.length; i++){
								importOrderList.add((FuncInfo)obj[i]);
							}
				
							// ?????????????????????????????????????????????
							monitor.beginTask(Messages.getString("string.import"), 100);
							// ????????????????????????????????????
							m_successList = "";
							m_failureList = "";
							m_action = new CommandAction();
							
							boolean doNotify = false;
							boolean doJob = false;
							FuncInfo notifyFuncInfo = null;
							List<String> notifyFileList = null;
							for (int i = 0; i < importOrderList.size(); i++) {
								if (monitor.isCanceled())
									throw new InterruptedException();
								
								FuncInfo info = importOrderList.get(i);
								defaultFileList = info.getDefaultXML();
								
								// XML??????????????????????????????
								fileList = ReadXMLAction.getXMLFile(defaultFileList);
								
								String[] args = { info.getName() };
								
								monitor.subTask(Messages.getString("message.import.running", args));

								if(info.getId().equals(HinemosModuleConstant.PLATFORM_NOTIFY)){
									doNotify = true;
									notifyFuncInfo = info;
									notifyFileList = fileList;
								}

								if(info.getId().equals(HinemosModuleConstant.JOB_MST)){
									doJob = true;
								}
								int result = 0;
								
								if (BackupUtil.isBackup(BackupUtil.WHEN_IMPORT)){
									result = m_action.backupCommand(info, fileList, info.getTargetIdList());
									if (result != 0){
										log.error(Messages.getString("message.backup.fail", args));
										result = -1;
										boolean continueFlg = MessageDialog.openQuestion(
												null,
												Messages.getString("message.error"),
												Messages.getString("ContinueQuestion.WhenExportError") + "\n\n" + info.getName()
												);
										if (continueFlg){
											result = 0;
										}
									}
								}
								
								result = m_action.importCommand(info, fileList, info.getTargetIdList());
								
								if(result == 0) {
									m_successList = m_successList + " " + info.getName() + "\n";
								}
								else {
									m_failureList = m_failureList + " " + info.getName() + "\n";
								}
								
								monitor.worked(100/importOrderList.size());
							}
							if(doJob && doNotify){
								ImportProcessMode.setProcesstype(UtilityDialogConstant.SKIP);
								ImportProcessMode.setSameprocess(true);
								m_action.importCommand(notifyFuncInfo, notifyFileList, notifyFuncInfo.getTargetIdList());
							}
							
							monitor.done();
						}
					};
					// ????????????????????????
					new ProgressMonitorDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell()).run(false, true, op);

				} catch (InvocationTargetException e) {
					log.error(e);
				} catch (InterruptedException e) {
					// ???????????????????????????
					MessageDialog.openInformation(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							Messages.getString("string.cancel"),
							Messages.getString("message.cancel"));
				}
			
				// ???????????????????????????
				String message = "";
				// ??????????????????
				if(!m_successList.equals("")){
					message = Messages.getString("message.import.result1") + "\n\n" + m_successList;
					MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
					
					String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n");
					String[] resultMessages = resultMessage.split("\n");
					for(int i = 0; i < resultMessages.length; i++){
						mStatus.add(new Status(IStatus.INFO, this.toString(), IStatus.OK, resultMessages[i], null));
					}
					ErrorDialog.openError(null, Messages.getString("message.confirm"), null, mStatus);
				}
				
				// ??????????????????
				if(!m_failureList.equals("")){
					message = Messages.getString("message.import.result2") + "\n\n" + m_failureList;
					
					MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
					
					String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n") + m_action.getErrOut().replaceAll("\r\n", "\n");
					String[] resultMessages = resultMessage.split("\n");
					for(int i = 0; i < resultMessages.length; i++){
						mStatus.add(new Status(IStatus.WARNING, this.toString(), IStatus.OK, resultMessages[i], null));
					}
					
					// ????????????????????????
					ErrorDialog.openError(
							null, 
							Messages.getString("message.confirm"), 
							null, 
							mStatus);
				}
				
			}
		}
		// Web????????????????????????????????????zip??????
		if((filePath != null) && (BackupUtil.isBackup(BackupUtil.WHEN_IMPORT))){
			String backupTmpPath = BackupUtil.getBackupPath();
			backupTmpPath=backupTmpPath.replaceAll("\\\\", "/");
			List<String> inputFileNameList = new ArrayList<>();
			File dir = new File(backupTmpPath);
			FileUtil.addFiles2List(dir, inputFileNameList);
			File exportArchive = null;
			try {
				exportArchive = File.createTempFile("backup_", ".zip", new File(ClientPathUtil.getDefaultXMLPath()));
				log.info("create export file : " + exportArchive.getAbsolutePath());
			} catch (IOException e) {
				log.error("fail to createTempFile : " + e.getMessage());
				MessageDialog.openError(null,
						 Messages.getString("message.error"),
						 Messages.getString("message.export.error2"));
			}
			
			if(inputFileNameList.size() > 0 && exportArchive != null){
				try {
					ZipUtil.archive(inputFileNameList, exportArchive.getAbsolutePath(), new File(filePath).getAbsolutePath());
					FileDownloader.openBrowser(window.getShell(), exportArchive.getAbsolutePath(), "backup.zip");
				} catch (Exception e) {
					MessageDialog.openError(null,
							 Messages.getString("message.error"),
							 Messages.getString("message.export.error2"));
				}
			}
				
			ClientPathUtil.getInstance().unlock(parentPath);
			
			if(exportArchive != null) {
				if(exportArchive.delete()) {
					log.info("delete  exportArchive : " + exportArchive.getAbsolutePath());
				} else {
					log.error("fail to delete  exportArchive : " + exportArchive.getAbsolutePath());
				}
			}
		}
		
		if(filePath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
			ClientPathUtil.getInstance().unlock(parentPath);
		}
		
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if(part instanceof ImportExportExecView){
					// Enable button when 1 item is selected
//					ImportExportExecView view = (ImportExportExecView)part;
				}
				this.setBaseEnabled(editEnable);
				this.setBaseEnabled(true);
			}
		}
	}

	/**
	 * @param funcNameList
	 * @return ????????????????????????(-1)???OK????????????????????????OFF???1??????OK????????????????????????OFF???2??????
	 */
	private int showComfirmWithObjectPrivilege(String funcNameList){
		String msg = Messages.getString("string.manager") + " :  " +
				UtilityManagerUtil.getCurrentManagerName() +
				" ( " +
				EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr() +
				" )\n\n" +
				Messages.getString("message.import.confirm1") +
				"\n\n" +
				funcNameList;
		MessageDialogWithCheckbox messageDialogWithCheckbox = new MessageDialogWithCheckbox(
				msg,
				Messages.getString("message.import.confirm.object.privilege")
				);
		boolean status = messageDialogWithCheckbox.openQuestion();
		if (status){
			if (messageDialogWithCheckbox.isChecked()){
				return OK_WITH_OBJECT_PRIVILEGE;
			}else{
				return OK_WITHOUT_OBJECT_PRIVILEGE;
			}
		}else{
			return CANCEL;
		}
	}
	
	private boolean showComfirmDialog(String funcNameList){
		return MessageDialog.openQuestion(
				null,
				Messages.getString("message.confirm"),
				Messages.getString("string.manager") + " :  " + UtilityManagerUtil.getCurrentManagerName() + " ( " + EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr() + " )\n\n" +
				Messages.getString("message.import.confirm1") + "\n\n" + funcNameList);
	}
}
