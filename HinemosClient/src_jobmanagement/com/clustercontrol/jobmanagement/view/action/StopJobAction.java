/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.OperationMessage;
import com.clustercontrol.jobmanagement.action.OperationJob;
import com.clustercontrol.jobmanagement.bean.JobOperationConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;
import com.clustercontrol.jobmanagement.composite.DetailComposite;
import com.clustercontrol.jobmanagement.composite.HistoryComposite;
import com.clustercontrol.jobmanagement.composite.NodeDetailComposite;
import com.clustercontrol.jobmanagement.dialog.JobOperationDialog;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.view.JobDetailView;
import com.clustercontrol.jobmanagement.view.JobHistoryView;
import com.clustercontrol.jobmanagement.view.JobNodeDetailView;
import com.clustercontrol.jobmanagement.view.JobQueueContentsView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;

/**
 * ?????????[??????]????????????[???????????????]????????????[???????????????]????????????[????????????????????????]????????????????????????????????????????????????????????????????????????<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class StopJobAction extends AbstractHandler implements IElementUpdater {

	// ??????
	protected static Log m_log = LogFactory.getLog( StartJobAction.class );

	/** ???????????????ID */
	public static final String ID = StopJobAction.class.getName();

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

	/**
	 * ??????????????????????????????
	 *
	 * @param managerName ??????????????????
	 * @param sessionId ???????????????ID
	 * @param jobunitId ???????????????????????????????????????ID
	 * @param jobId ?????????ID
	 * @param facilityId ??????????????????ID
	 * @return ???????????????????????????????????????
	 *
	 */
	private Property getStopProperty(String managerName, String sessionId, String jobunitId, String jobId, String facilityId) {
		Locale locale = Locale.getDefault();

		//???????????????ID
		Property session =
				new Property(JobOperationConstant.SESSION, Messages.getString("session.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		session.setValue(sessionId);

		//?????????????????????ID
		Property jobUnit =
				new Property(JobOperationConstant.JOB_UNIT, Messages.getString("jobunit.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		jobUnit.setValue(jobunitId);

		//?????????ID
		Property job =
				new Property(JobOperationConstant.JOB, Messages.getString("job.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		job.setValue(jobId);

		//??????????????????ID
		Property facility =
				new Property(JobOperationConstant.FACILITY, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT);
		ArrayList<Property> endList = new ArrayList<Property>();
		if(facilityId != null && facilityId.length() > 0){
			facility.setValue(facilityId);
		}else{
			facility.setValue("");
		}

		//????????????
		Property endStatus = null;
		if (facilityId == null) {
			endStatus = new Property(JobOperationConstant.END_STATUS, Messages.getString("end.status", locale), PropertyDefineConstant.EDITOR_SELECT);
			Object endStatusList[][] = {
					{"", EndStatusMessage.STRING_NORMAL, EndStatusMessage.STRING_WARNING, EndStatusMessage.STRING_ABNORMAL},
					{"", EndStatusConstant.TYPE_NORMAL, EndStatusConstant.TYPE_WARNING, EndStatusConstant.TYPE_ABNORMAL}
			};
			endStatus.setSelectValues(endStatusList);
			endStatus.setValue("");
			endList.add(endStatus);
		}

		//?????????
		Property endValue =
				new Property(JobOperationConstant.END_VALUE, Messages.getString("end.value", locale), PropertyDefineConstant.EDITOR_NUM,
						DataRangeConstant.SMALLINT_HIGH, DataRangeConstant.SMALLINT_LOW);
		endValue.setValue("");
		endList.add(endValue);

		//??????
		Property control =
				new Property(JobOperationConstant.CONTROL, Messages.getString("control", locale), PropertyDefineConstant.EDITOR_SELECT);
		HashMap<String, Object> mainteEndMap = new HashMap<String, Object>();
		mainteEndMap.put("value", OperationMessage.STRING_STOP_MAINTENANCE);
		mainteEndMap.put("property", endList);
		HashMap<String, Object> forceEndMap = new HashMap<String, Object>();
		forceEndMap.put("value", OperationMessage.STRING_STOP_FORCE);
		forceEndMap.put("property", endList);
		List<Integer> values1 = null;
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
			values1 = wrapper.getAvailableStopOperation(sessionId, jobunitId, jobId, facilityId);
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			throw new InternalError("values1 is null");
		} catch (Exception e) {
			m_log.warn("getStopProperty(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			throw new InternalError("values1 is null");
		}
		ArrayList<Object> values2 = new ArrayList<Object>();
		if(values1.contains(OperationConstant.TYPE_STOP_AT_ONCE)) {
			values2.add(OperationMessage.STRING_STOP_AT_ONCE);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_SUSPEND)){
			values2.add(OperationMessage.STRING_STOP_SUSPEND);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_WAIT)){
			values2.add(OperationMessage.STRING_STOP_WAIT);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_SKIP)) {
			values2.add(OperationMessage.STRING_STOP_SKIP);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_MAINTENANCE)) {
			values2.add(mainteEndMap);
		}
		if(values1.contains(OperationConstant.TYPE_STOP_FORCE)) {
			values2.add(forceEndMap);
		}
		
		List<String> stringValues = new ArrayList<String>();
		for (Integer type : values1) {
			stringValues.add(OperationMessage.typeToString(type));
		}
		Object controlValues[][] = {stringValues.toArray(), values2.toArray()};
		control.setSelectValues(controlValues);
		if(stringValues.size() >= 1){
			control.setValue(stringValues.get(0));
		}else{
			control.setValue("");
		}

		//????????????/???????????????
		session.setModify(PropertyDefineConstant.MODIFY_NG);
		jobUnit.setModify(PropertyDefineConstant.MODIFY_NG);
		job.setModify(PropertyDefineConstant.MODIFY_NG);
		facility.setModify(PropertyDefineConstant.MODIFY_NG);
		control.setModify(PropertyDefineConstant.MODIFY_OK);
		if (endStatus != null) {
			endStatus.setModify(PropertyDefineConstant.MODIFY_OK);
		}
		endValue.setModify(PropertyDefineConstant.MODIFY_OK);

		// ?????????????????????????????????
		Property property = new Property(null, null, "");
		property.removeChildren();
		property.addChildren(session);
		property.addChildren(jobUnit);
		property.addChildren(job);
		if(facilityId != null){
			property.addChildren(facility);
		}
		property.addChildren(control);

		String controlStr = (String)control.getValue();
		if(	OperationMessage.STRING_STOP_MAINTENANCE.equals(controlStr) ||
				OperationMessage.STRING_STOP_FORCE.equals(controlStr)){
			if (endStatus != null) {
				control.addChildren(endStatus);
			}
			control.addChildren(endValue);
		}

		return property;
	}

	/**
	 * ?????????[??????]????????????[???????????????]????????????[???????????????]????????????[????????????????????????]?????????????????????????????????????????????????????????????????????????????????????????????
	 * <p>
	 * <ol>
	 * <li>?????????[??????]????????????[???????????????]????????????[????????????????????????]???????????????????????????????????????????????????ID????????????ID?????????????????????</li>
	 * <li>?????????[???????????????]??????????????????????????????????????????????????????ID????????????ID?????????????????????ID?????????????????????</li>
	 * <li>????????????????????????????????????????????????????????????</li>
	 * <li>?????????[??????]????????????????????????????????????</li>
	 * <li>?????????[??????]?????????????????????????????????????????????????????????????????????????????????</li>
	 * <li>???????????????????????????????????????????????????????????????????????????????????????</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobHistoryView
	 * @see com.clustercontrol.jobmanagement.view.JobDetailView
	 * @see com.clustercontrol.jobmanagement.view.JobNodeDetailView
	 * @see com.clustercontrol.jobmanagement.dialog.JobOperationDialog
	 * @see com.clustercontrol.jobmanagement.action.OperationJob#operationJob(Property)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String managerName = null;
		String sessionId = null;
		String jobunitId = null;
		String jobId = null;
		String facilityId = null;

		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// ???????????????????????????
		this.viewPart = HandlerUtil.getActivePart(event);

		if (viewPart instanceof JobHistoryView) { // ??????????????????????????????
			JobHistoryView jobHistoryView = null;
			try {
				jobHistoryView = (JobHistoryView) viewPart
						.getAdapter(JobHistoryView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}
			
			if (jobHistoryView == null) {
				m_log.info("execute: job history view is null"); 
			} else {
				HistoryComposite historyComposite = jobHistoryView.getComposite();
				//????????????????????????
				managerName = historyComposite.getManagerName();
				//???????????????ID??????
				sessionId = historyComposite.getSessionId();
				//?????????????????????ID??????
				jobunitId = historyComposite.getJobunitId();
				//?????????ID??????
				jobId = historyComposite.getJobId();
			}
		} else if (viewPart instanceof JobDetailView) { // ??????????????????????????????
			JobDetailView JobDetailView = null;
			try {
				JobDetailView = (JobDetailView) viewPart
						.getAdapter(JobDetailView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}
			
			if (JobDetailView == null) {
				m_log.info("execute: job detail view is null"); 
			} else {
				DetailComposite detailComposite = JobDetailView.getComposite();
				//????????????????????????
				managerName = detailComposite.getManagerName();
				//???????????????ID??????
				sessionId = detailComposite.getSessionId();
				//?????????????????????ID??????
				jobunitId = detailComposite.getJobunitId();
				//?????????ID??????
				jobId = detailComposite.getJobId();
			}
		} else if (viewPart instanceof JobNodeDetailView) { // ??????????????????????????????
			JobNodeDetailView jobNodeDetailView = null;
			try {
				jobNodeDetailView = (JobNodeDetailView) viewPart
						.getAdapter(JobNodeDetailView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (jobNodeDetailView == null) {
				m_log.info("execute: job node detail view is null"); 
			} else {
				NodeDetailComposite nodeDetailComposite = jobNodeDetailView.getComposite();
				//????????????????????????
				managerName = nodeDetailComposite.getManagerName();
				//???????????????ID??????
				sessionId = nodeDetailComposite.getSessionId();
				//?????????????????????ID??????
				jobunitId = nodeDetailComposite.getJobunitId();
				//?????????ID??????
				jobId = nodeDetailComposite.getJobId();
				//??????????????????ID??????
				facilityId = nodeDetailComposite.getFacilityId();
				if (facilityId == null) {
					sessionId = null;
					jobunitId = null;
					jobId = null;
				}
			}
		} else if (viewPart instanceof JobQueueContentsView) {
			JobQueueContentsView view = null;
			try {
				view = (JobQueueContentsView) viewPart
						.getAdapter(JobQueueContentsView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (view == null) {
				m_log.info("execute: JobQueueContentsView is null"); 
			} else {
				managerName = view.getManagerName();
				sessionId = view.getSelectedSessionId();
				jobunitId = view.getSelectedJobunitId();
				jobId = view.getSelectedJobId();
			}
		}

		if (sessionId != null && sessionId.length() > 0 && jobunitId != null
				&& jobunitId.length() > 0 && jobId != null
				&& jobId.length() > 0) {

			JobOperationDialog dialog = new JobOperationDialog(this.window.getShell());

			//?????????????????????
			dialog.setProperty(getStopProperty(managerName, sessionId, jobunitId, jobId, facilityId));
			dialog.setTitleText(Messages.getString("job") + "["
					+ Messages.getString("stop") + "]");

			//?????????????????????
			if (dialog.open() == IDialogConstants.OK_ID) {
				//???????????????
				OperationJob operation = new OperationJob();
				operation.operationJob(managerName, dialog.getProperty());
			}
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
				if(part instanceof JobHistoryView){
					// Enable button when 1 item is selected
					JobHistoryView view = (JobHistoryView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				} else if(part instanceof JobDetailView) {
					JobDetailView view = (JobDetailView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				} else if(part instanceof JobNodeDetailView) {
					JobNodeDetailView view = (JobNodeDetailView)part;
					if(view.getSelectedNum() > 0) {
						editEnable = true;
					}
				} else if(part instanceof JobQueueContentsView) {
					JobQueueContentsView view = (JobQueueContentsView)part;
					if(view.getSelectedCount() > 0) {
						editEnable = true;
					}
				}
				this.setBaseEnabled( editEnable );
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
