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
import com.clustercontrol.utility.settings.ui.views.ImportExportExecView;
import com.clustercontrol.utility.util.ClientPathUtil;
import com.clustercontrol.utility.util.FileUtil;
import com.clustercontrol.utility.util.MultiManagerPathUtil;
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
public class DeleteSettingCommand extends AbstractHandler implements IElementUpdater {
	/*?????????*/
	private Log log = LogFactory.getLog(getClass());
	
	/** ???????????????ID */
	public static final String ID = DeleteSettingCommand.class.getName();
	private IWorkbenchWindow window;
	/** ????????? */
	private IWorkbenchPart viewPart;

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
		} catch (HinemosUnknown_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.version.error"));
			return null;
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.expiration.term"));
			return null;
		} catch (InvalidUserPass_Exception e) {
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
        List<FuncInfo> checkList = listView.getCheckedFunc();
		
		String parentPath = MultiManagerPathUtil.getDirectoryPathTemporary(SettingToolsXMLPreferencePage.KEY_XML);

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
        // ??????????????????????????????????????????????????????
        m_funcList = new ArrayList<FuncInfo>();
		
		if(checkList.size() == 0){
			MessageDialog.openError(null,
				Messages.getString("message.error"),
				Messages.getString("message.delete.error1"));
		}else{
			// ????????????????????????????????????????????????????????????????????????????????????????????????
			// ???????????????????????????????????????
			// ??????????????????????????????
			Iterator<FuncInfo> it = checkList.iterator();
			String funcs = "";
			boolean masterFlg = false;
			boolean otherFlg = false;
			while(it.hasNext()) {
				FuncInfo info = it.next();
				
				// ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
				if(info.getId().equals(HinemosModuleConstant.PLATFORM_PRIORITY_JUDGMENT) ||
						info.getId().equals(HinemosModuleConstant.PERFORMANCE_RECORD) ||
						info.getId().equals(HinemosModuleConstant.MASTER_PLATFORM) ||
						info.getId().equals(HinemosModuleConstant.NODE_MAP_SETTING) ||
						info.getId().equals(HinemosModuleConstant.NODE_MAP_IMAGE)) {
					
					String[] args = { info.getName() };
					
					if(info.getId().equals(HinemosModuleConstant.PERFORMANCE_RECORD)) {
						
						if(! MessageDialog.openQuestion(
								null,
								Messages.getString("message.confirm"),
								Messages.getString("message.delete.confirm3", args)))
						{
							// ??????????????????????????????
							if(filePath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
								ClientPathUtil.getInstance().unlock(parentPath);
							}
							return null;
						}
						
					}
					else {
						
						if(! MessageDialog.openQuestion(
								null,
								Messages.getString("message.confirm"),
								Messages.getString("message.delete.confirm2", args)))
						{
							// ??????????????????????????????
							if(filePath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
								ClientPathUtil.getInstance().unlock(parentPath);
							}
							return null;
						}
						
					}
					
				}
				else {
					
					// ???????????????????????????OK?????????????????????????????????????????????
					// ver.3.2, ver4.0??????????????????????????????????????????????????????
					boolean isTargetSelected = true;
					
					// ???????????????????????????????????????????????????
					if (info.getId().equals(HinemosModuleConstant.JOB_MST)) {
						JobunitListDialog jobunitListDialog = new JobunitListDialog(
								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								"clear", info.getDefaultXML().get(0));
						
						// OK?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????ID????????????
						if (jobunitListDialog.open() == IDialogConstants.OK_ID) {
							if (jobunitListDialog.getSelectionData() != null && jobunitListDialog.getSelectionData().size() != 0) {
								info.setTargetIdList(jobunitListDialog.getSelectionData());
							} else {
								isTargetSelected = false;
							}
							
						} else {
							info.setTargetIdList(null);
							isTargetSelected = false;
						}
					}

					if (isTargetSelected) {
						m_funcList.add(info);
						funcs = funcs + " " + info.getName() + "\n";
					}
					
					if(info.getId().matches("^"+HinemosModuleConstant.MASTER+".*")){
						masterFlg = true;
					}
					else {
						otherFlg = true;
					}
					
				}
				
				// ???????????????????????????????????????????????????????????????????????????
				if(masterFlg && otherFlg) {
					
					MessageDialog.openWarning(
							null,
							Messages.getString("message.priority.warning"),
							Messages.getString("message.delete.error2"));
					
					if(filePath != null || ClientPathUtil.getInstance().isBussy(parentPath)){
						ClientPathUtil.getInstance().unlock(parentPath);
					}
					return null;
				}
			}
			
			if(m_funcList.size() != 0 &&
					MessageDialog.openQuestion(
							null,
							Messages.getString("message.confirm"),
							Messages.getString("string.manager") + " :  " + UtilityManagerUtil.getCurrentManagerName() + " ( " + EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getUrlListStr() + " )\n\n" +
							Messages.getString("message.delete.confirm1") + "\n\n" +funcs ))
			{
				
				// ??????????????????????????????
				try {
					IRunnableWithProgress op = new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {

							
							// ?????????????????????
							Object[] obj = m_funcList.toArray();
							Arrays.sort(obj, new Comparator<Object>() {
								@Override
								public int compare(Object o1, Object o2){
									return ((FuncInfo)o2).getWeight() - ((FuncInfo)o1).getWeight();
								}
							});

							ArrayList<FuncInfo> deleteOrderList = new ArrayList<FuncInfo>();
							for(int i=0; i<obj.length; i++){
								deleteOrderList.add((FuncInfo)obj[i]);
							}

							// ?????????????????????????????????????????????
							monitor.beginTask(Messages.getString("string.delete"), 100);
							// ????????????????????????????????????
							m_successList = "";
							m_failureList = "";
							
							m_action = new CommandAction();
							for (int i = 0; i < deleteOrderList.size(); i++) {
								if (monitor.isCanceled())
									throw new InterruptedException();

								// ???????????????????????????
								FuncInfo info = deleteOrderList.get(i);
								
								String[] args = { info.getName() };
								
								monitor.subTask(Messages.getString("message.delete.running", args));
								
								int result = 0;
								if (BackupUtil.isBackup(BackupUtil.WHEN_CLEAR)){
									result = m_action.backupCommand(info, ReadXMLAction.getXMLFile(info.getDefaultXML()), info.getTargetIdList());
									if (result!=0){
										log.warn(Messages.getString("message.backup.fail", args));
									}
								}
								
								if (result == 0){
									result = m_action.deleteCommand(info, info.getTargetIdList());
								}
								
								if(result == 0) {
									m_successList = m_successList + " " + info.getName() + "\n";
								}
								else {
									m_failureList = m_failureList + " " + info.getName() + "\n";
								}
								
								monitor.worked(100/deleteOrderList.size());
								
							}
							
							monitor.done();
							

						}
					};
					// ????????????????????????
					new ProgressMonitorDialog(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getShell()).run(false, true, op);

				} catch (InvocationTargetException e) {
					Throwable throwable = e.getCause();
					log.error(throwable);
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
					message = Messages.getString("message.delete.result1") + "\n\n" + m_successList;
					
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
					message = Messages.getString("message.delete.result2") + "\n\n" + m_failureList;
					
					MultiStatus mStatus = new MultiStatus(this.toString(), IStatus.OK, message, null);
					String resultMessage = m_action.getStdOut().replaceAll("\r\n", "\n") + m_action.getErrOut().replaceAll("\r\n", "\n");
					String[] resultMessages = resultMessage.split("\n");
					for(int i = 0; i < resultMessages.length; i++){
						mStatus.add(new Status(IStatus.WARNING, this.toString(), IStatus.OK, resultMessages[i], null));
					}
					
					// ????????????????????????
					ErrorDialog.openError(null, Messages.getString("message.confirm"), null, mStatus);
				}
			}
		}
		
		
		// Web????????????????????????????????????zip??????
		if((filePath != null) && (BackupUtil.isBackup(BackupUtil.WHEN_CLEAR))){
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
			// Backup????????????????????????????????????
			File folder = new File(backupTmpPath);
			BackupUtil.deleteFiles(folder);
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
				}
				this.setBaseEnabled(editEnable);
				this.setBaseEnabled(true);
			}
		}
	}
}
