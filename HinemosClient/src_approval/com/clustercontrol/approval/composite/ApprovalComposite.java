/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.approval.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.approval.action.GetApprovalTableDefine;
import com.clustercontrol.approval.bean.ApprovalFilterPropertyConstant;
import com.clustercontrol.approval.dialog.ApprovalDetailDialog;
import com.clustercontrol.approval.preference.ApprovalPreferencePage;
import com.clustercontrol.approval.view.ApprovalView;
import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.jobmanagement.bean.JobApprovalResultMessage;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobApprovalFilter;
import com.clustercontrol.ws.jobmanagement.JobApprovalInfo;

/**
 * ???????????????????????????????????????<BR>
 *
 * @version 5.1.0
 * @since 5.1.0
 */
public class ApprovalComposite extends Composite implements ISelectionChangedListener, IDoubleClickListener {
	
	// ??????
	private static Log m_log = LogFactory.getLog( ApprovalComposite.class );
	
	/** ?????????????????????????????? */
	private CommonTableViewer tableViewer = null;
	
	/** ????????????????????? */
	private Label labelType = null;
	
	/** ???????????????????????? */
	private Label labelCount = null;
	
	/** ???????????????ID */
	private String selectedSessionId = null;
	
	/** ?????????????????? */
	private Property condition = null;

	public ApprovalComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}
	/**
	 * ???????????????????????????????????????
	 *
	 * @see com.clustercontrol.notify.action.GetNotifyTableDefineCheckBox#get()
	 * @see #update()
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		
		labelType = new Label(this, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "labelType", labelType);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		labelType.setLayoutData(gridData);
		
		final Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);
		
		labelCount = new Label(this, SWT.RIGHT);
		WidgetTestUtil.setTestId(this, "labelcount", labelCount);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		labelCount.setLayoutData(gridData);
		
		tableViewer = new CommonTableViewer(table);
		tableViewer.createTableColumn(GetApprovalTableDefine.get(),
				GetApprovalTableDefine.SORT_COLUMN_INDEX1,
				GetApprovalTableDefine.SORT_COLUMN_INDEX2,
				GetApprovalTableDefine.SORT_ORDER);
		
		// ???????????????????????????
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumn(i).setMoveable(true);
		}
		
		tableViewer.addSelectionChangedListener(this);
		tableViewer.addDoubleClickListener(this);
		
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????
	 *
	 * @return ????????????
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????????????????
	 *
	 * @return ???????????????????????????
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * ????????????????????????????????????????????????<BR>
	 * ?????????????????????????????????????????????????????????
	 *
	 */
	@Override
	public void update() {
			condition = null;
			update(condition);
		}
	
	public void update(Property property) {
		condition = property;
		
		List<JobApprovalInfo> infoList = null;
		Map<String, List<JobApprovalInfo>> dispDataMap= new ConcurrentHashMap<String, List<JobApprovalInfo>>();
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		
		String conditionManager = null;
		JobApprovalFilter filter = null;
		
		int total = 0;
		int limit = ClusterControlPlugin.getDefault().getPreferenceStore().getInt(
				ApprovalPreferencePage.P_APPROVAL_MAX_LIST);
		
		if(condition != null) {
			conditionManager = getManagerName(condition);
			filter = property2jobApprovalFilter(condition);
		} else {
			// ???????????????????????????????????????????????????????????????????????????????????????????????????
			filter = new JobApprovalFilter();
			filter.getStatusList().add(JobApprovalStatusConstant.TYPE_PENDING);
		}
		
		// ????????????????????????
		for(String managerName : EndpointManager.getActiveManagerSet()) {
			try {
				if(conditionManager != null && !conditionManager.equals(managerName)){
					continue;
				}
				
				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				infoList = wrapper.getApprovalJobList(filter, limit);
			} catch (InvalidRole_Exception e) {
				// ????????????
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );

			} catch (Exception e) {
				// ?????????????????????
				m_log.warn("update(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			
			if(infoList != null){
				dispDataMap.put(managerName, infoList);
				total += infoList.size();
			}
		}
		
		//?????????????????????
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		
		if(ClusterControlPlugin.getDefault().getPreferenceStore().getBoolean(
				ApprovalPreferencePage.P_APPROVAL_MESSAGE_FLG)){
			if(total > limit){
				if(ClientSession.isDialogFree()){
					ClientSession.occupyDialog();
					// ??????????????????????????????????????????????????????????????????????????????
					MessageDialogWithToggle.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.approval.7"),
							Messages.getString("message.will.not.be.displayed"),
							false,
							ClusterControlPlugin.getDefault().getPreferenceStore(),
							ApprovalPreferencePage.P_APPROVAL_MESSAGE_FLG);
					ClientSession.freeDialog();
				}
			}
		}
		
		//???????????????????????????????????????????????????
		List<JobApprovalInfo> infolist = dispDataMap2SortedList(dispDataMap, limit);
		
		// JobApprovalInfo ??? tableViewer ???????????????????????????????????????
		ArrayList<Object> listInput = new ArrayList<Object>();
		for (JobApprovalInfo info : infolist) {
			ArrayList<Object> obj = new ArrayList<Object>();
			obj.add(info.getMangerName()); 
			obj.add(info.getStatus());
			obj.add(info.getResult());
			obj.add(info.getSessionId());
			obj.add(info.getJobunitId());
			obj.add(info.getJobId());
			obj.add(info.getJobName());
			obj.add(info.getRequestUser());
			obj.add(info.getApprovalUser());
			obj.add(info.getStartDate() == null ? "": new Date(info.getStartDate()));
			obj.add(info.getEndDate() == null ? "": new Date(info.getEndDate()));
			obj.add(info.getRequestSentence());
			obj.add(info.getComment());
			obj.add(null);
			listInput.add(obj);
		}
		tableViewer.setInput(listInput);
		
		selectList(listInput);
		
		Integer count = listInput.size();
		Object[] args = null;
		args = new Object[]{ count.toString() };
		
		if (condition != null) {
			labelType.setText(Messages.getString("filtered.list"));
			labelCount.setText(Messages.getString("filtered.records", args));
		} else {
			labelType.setText("");
			labelCount.setText(Messages.getString("records", args));
		}
	}

	private List<JobApprovalInfo> dispDataMap2SortedList(Map<String, List<JobApprovalInfo>> dispDataMap, int limit) {
		List<JobApprovalInfo> ret = new ArrayList<JobApprovalInfo>();
		
		for(Map.Entry<String, List<JobApprovalInfo>> map : dispDataMap.entrySet()){
			for (JobApprovalInfo info : map.getValue()) {
				info.setMangerName(map.getKey());
				ret.add(info);
			}
		}
		
		// Sort - approvalStatus, ?????????????????????
		Collections.sort(ret, new Comparator<JobApprovalInfo>() {
			@Override
			public int compare(JobApprovalInfo o1, JobApprovalInfo o2) {
				return o2.getSessionId().compareTo(o1.getSessionId());
			}
		});
		
		// Slice array
		int len = ret.size();
		if( len > limit ){
			ret.subList(limit, len).clear();
			m_log.debug("limit over:" + len);
		}
		return ret;
	}

	private String getManagerName (Property property) {
		ArrayList<?> values = null;

		String managerName = null;
		//????????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.MANAGER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			managerName = (String)values.get(0);
		}

		return managerName;
	}

	private JobApprovalFilter property2jobApprovalFilter (Property property) {
		JobApprovalFilter filter = new JobApprovalFilter();
		ArrayList<?> values = null;

		//????????????????????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.START_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setStartFromDate(((Date)values.get(0)).getTime());
		}
		//????????????????????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.START_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setStartToDate(((Date)values.get(0)).getTime());
		}
		//????????????????????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.END_FROM_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setEndFromDate(((Date)values.get(0)).getTime());
		}
		//????????????????????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.END_TO_DATE);
		if(values.get(0) != null && values.get(0) instanceof Date){
			filter.setEndToDate(((Date)values.get(0)).getTime());
		}

		//??????????????????
		//?????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_PENDING);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getStatusList().add(JobApprovalStatusConstant.TYPE_PENDING);
			}
		}
		//?????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_STILL);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getStatusList().add(JobApprovalStatusConstant.TYPE_STILL);
			}
		}
		//?????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_SUSPEND);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getStatusList().add(JobApprovalStatusConstant.TYPE_SUSPEND);
			}
		}
		//??????(????????????)
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_STOP);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getStatusList().add(JobApprovalStatusConstant.TYPE_STOP);
			}
		}
		//?????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_STATUS_FINISHED);
		if (!"".equals(values.get(0))) {
			if ((Boolean)values.get(0)) {
				filter.getStatusList().add(JobApprovalStatusConstant.TYPE_FINISHED);
			}
		}
		
		//??????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_RESULT);
		Integer result = null;
		if(values.get(0) instanceof String){
			String resultString = (String)values.get(0);
			result = Integer.valueOf(JobApprovalResultMessage.stringToType(resultString));
			filter.setResult(result);
		}

		//???????????????ID??????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.SESSION_ID);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setSessionId((String)values.get(0));
		}

		//?????????????????????ID??????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.JOBUNIT_ID);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setJobunitId((String)values.get(0));
		}

		//?????????ID??????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.JOB_ID);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setJobId((String)values.get(0));
		}

		//??????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.JOB_NAME);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setJobName((String)values.get(0));
		}

		//?????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.RQUEST_USER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setRequestUser((String)values.get(0));
		}

		//?????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.APPROVAL_USER);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setApprovalUser((String)values.get(0));
		}

		//?????????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.RQUEST_SENTENCE);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setRequestSentence((String)values.get(0));
		}

		//??????????????????
		values = PropertyUtil.getPropertyValue(property, ApprovalFilterPropertyConstant.COMMENT);
		if(values.get(0) instanceof String && ((String)values.get(0)).length() > 0){
			filter.setComment((String)values.get(0));
		}

		return filter;
	}
	
	/**
	 * ?????????????????????????????????ID???????????????????????????????????????
	 *
	 * @param lsit ???????????????????????????
	 */
	private void selectList(ArrayList<Object> lsit) {
		if (selectedSessionId != null && selectedSessionId.length() > 0) {
			int index = -1;
			for (int i = 0; i < lsit.size(); i++) {
				ArrayList<?> line = (ArrayList<?>) lsit.get(i);
				String sessionId = (String)line.get(GetApprovalTableDefine.SESSION_ID);

				if (selectedSessionId.compareTo(sessionId) == 0) {
					index = i;
					break;
				}
			}
			if (index == -1) {
				selectedSessionId = null;
			} else {
				tableViewer.setSelection(new StructuredSelection(lsit.get(index)), true);
			}
		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		IViewPart viewPart = page.findView(ApprovalView.ID);
		if ( viewPart != null && selection != null ) {
			ApprovalView view = (ApprovalView) viewPart.getAdapter(ApprovalView.class);
			if (view == null) {
				m_log.info("selection changed: view is null");
			} else {
				view.setEnabledAction(selection.size(), selection);
			}
		}
		//???????????????ID?????????
		if (selection != null&& selection.getFirstElement() != null) {
			ArrayList<?> info = (ArrayList<?>) selection.getFirstElement();
			selectedSessionId = (String) info.get(GetApprovalTableDefine.SESSION_ID);
		}
	
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		StructuredSelection selection = (StructuredSelection) event.getSelection();
		IViewPart viewPart = page.findView(ApprovalView.ID);
		if (viewPart == null || selection == null) {
			return;
		}
		
		//??????????????????????????????
		ApprovalDetailDialog dialog = new ApprovalDetailDialog(viewPart.getSite().getShell(), getSelectedApprovalInfo());
		dialog.open();

		// ??????
		this.update(condition);
	}
	
	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @return ?????????????????????
	 */
	public JobApprovalInfo getSelectedApprovalInfo() {
		JobApprovalInfo info = null;
		
		StructuredSelection selection = (StructuredSelection) tableViewer.getSelection();
		ArrayList<?> itemList = (ArrayList<?>) selection.getFirstElement();
		if(itemList != null){
			info = new JobApprovalInfo();
			info.setMangerName((String) itemList.get(GetApprovalTableDefine.MANAGER_NAME));
			info.setStatus((Integer) itemList.get(GetApprovalTableDefine.APPROVAL_STATUS));
			info.setResult((Integer) itemList.get(GetApprovalTableDefine.APPROVAL_RESULT));
			info.setSessionId((String) itemList.get(GetApprovalTableDefine.SESSION_ID));
			info.setJobunitId((String) itemList.get(GetApprovalTableDefine.JOBUNIT_ID));
			info.setJobId((String) itemList.get(GetApprovalTableDefine.JOB_ID));
			info.setJobName((String) itemList.get(GetApprovalTableDefine.JOB_NAME));
			info.setRequestUser((String) itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_USER));
			info.setApprovalUser((String) itemList.get(GetApprovalTableDefine.APPROVAL_USER));
			if(itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_TIME) instanceof Date){
				Date start = (Date) itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_TIME);
				info.setStartDate(start.getTime());
			}
			if(itemList.get(GetApprovalTableDefine.APPROVAL_COMPLETION_TIME) instanceof Date){
				Date end = (Date) itemList.get(GetApprovalTableDefine.APPROVAL_COMPLETION_TIME);
				info.setEndDate(end.getTime());
			}
			info.setRequestSentence((String) itemList.get(GetApprovalTableDefine.APPROVAL_REQUEST_SENTENCE));
			info.setComment((String) itemList.get(GetApprovalTableDefine.COMMENT));
		}
		
		return info;
	}
	
}
