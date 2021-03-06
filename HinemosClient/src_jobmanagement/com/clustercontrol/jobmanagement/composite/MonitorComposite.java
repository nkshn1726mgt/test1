/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.PriorityMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.bean.MonitorJobConstant;
import com.clustercontrol.jobmanagement.bean.ProcessingMethodConstant;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.monitor.run.bean.MonitorTypeMessage;
import com.clustercontrol.monitor.view.action.MonitorModifyAction;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.MonitorJobInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * ????????????????????????????????????????????????
 *
 * @version 5.1.0
 */
public class MonitorComposite extends Composite {

	// ??????
	private static Log m_log = LogFactory.getLog( MonitorComposite.class );

	/** ??????????????????????????? */
	private Text m_scope = null;
	/** ???????????????????????????????????? */
	private Button m_scopeJobParam = null;
	/** ?????????????????????????????? */
	private Text m_scopeJobParamText = null;
	/** ?????????????????????????????? */
	private Button m_scopeFixedValue = null;
	/** ?????????????????????????????? */
	private Button m_scopeSelect = null;
	/** ???????????????????????????????????????????????? */
	private Button m_allNode = null;
	/** ??????????????????????????????????????????????????????????????????????????? */
	private Button m_retry = null;
	/** ???????????????????????????????????? */
	private Combo m_monitorIdCombo = null;
	/** ?????????????????????????????????????????????????????????????????? */
	private Map<String, MonitorInfo> m_monitorIdMap = null;
	/** ?????????????????????????????? */
	private Button m_monitorReferBtn = null;
	/** ???????????????????????????????????? */
	private Text m_infoEndValue = null;
	/** ???????????????????????????????????? */
	private Text m_warnEndValue = null;
	/** ???????????????????????????????????? */
	private Text m_criticalEndValue = null;
	/** ???????????????????????????????????? */
	private Text m_unknownEndValue = null;
	/** ???????????????????????????????????????????????????????????????????????????????????? */
	private Text m_waitTimeText = null;
	/** ??????????????????????????????????????????????????????????????????????????? */
	private Text m_waitEndValueText = null;

	/** ????????????????????? */
	private MonitorJobInfo m_monitor = null;
	/** ??????????????????ID */
	private String m_facilityId = null;
	/** ???????????? */
	private String m_facilityPath = null;
	/** ????????? */
	private Shell m_shell = null;
	/** ?????????????????????ID */
	private String m_ownerRoleId = null;
	/** ?????????????????? */
	private String m_managerName = null;

	/**
	 * ?????????????????????
	 *
	 * @param parent ?????????????????????
	 * @param style ????????????
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public MonitorComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
		m_shell = this.getShell();
	}

	/**
	 * ???????????????????????????????????????
	 */
	private void initialize() {

		Label label = null;

		this.setLayout(JobDialogUtil.getParentLayout());

		// ??????????????????????????????
		Group cmdScopeGroup = new Group(this, SWT.NONE);
		cmdScopeGroup.setText(Messages.getString("scope"));
		cmdScopeGroup.setLayout(new GridLayout(3, false));

		// ?????????????????????????????????????????????
		this.m_scopeJobParam = new Button(cmdScopeGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_scopeJobParam", this.m_scopeJobParam);
		this.m_scopeJobParam.setText(Messages.getString("job.parameter") + " : ");
		this.m_scopeJobParam.setLayoutData(
				new GridData(120, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeJobParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_scopeJobParamText.setEditable(true);
					m_scopeFixedValue.setSelection(false);
					m_scopeSelect.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		// ????????????????????????????????????????????????
		this.m_scopeJobParamText = new Text(cmdScopeGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_scopeJobParamText", this.m_scopeJobParamText);
		this.m_scopeJobParamText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scopeJobParamText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
				if (m_scopeJobParam.getSelection()) {
					m_facilityId = m_scopeJobParamText.getText();
				}
			}
		});

		//dummy
		new Label(cmdScopeGroup, SWT.LEFT);

		// ???????????????????????????????????????
		this.m_scopeFixedValue = new Button(cmdScopeGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_scopeFixedValue", this.m_scopeFixedValue);
		this.m_scopeFixedValue.setText(Messages.getString("fixed.value") + " : ");
		this.m_scopeFixedValue.setLayoutData(new GridData(120,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeFixedValue.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_scopeJobParam.setSelection(false);
					m_scopeSelect.setEnabled(true);
					m_scopeJobParamText.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		// ??????????????????????????????????????????
		this.m_scope = new Text(cmdScopeGroup, SWT.BORDER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_scope", this.m_scope);
		this.m_scope.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_scope.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ????????????????????????????????????
		this.m_scopeSelect = new Button(cmdScopeGroup, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_scopeSelect", this.m_scopeSelect);
		this.m_scopeSelect.setText(Messages.getString("refer"));
		this.m_scopeSelect.setLayoutData(new GridData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_scopeSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(m_shell, m_managerName, m_ownerRoleId);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem selectItem = dialog.getSelectItem();
					FacilityInfo info = selectItem.getData();
					FacilityPath path = new FacilityPath(
							ClusterControlPlugin.getDefault()
							.getSeparator());
					m_facilityPath = path.getPath(selectItem);
					m_facilityId = info.getFacilityId();
					m_scope.setText(m_facilityPath);
					update();
				}
			}
		});

		// ????????????????????????????????????
		Group cmdScopeProcGroup = new Group(this, SWT.NONE);
		cmdScopeProcGroup.setText(Messages.getString("scope.process"));
		cmdScopeProcGroup.setLayout(new RowLayout());

		// ??????????????????????????????????????????????????????
		this.m_allNode = new Button(cmdScopeProcGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_allNode", this.m_allNode);
		this.m_allNode.setText(Messages.getString("scope.process.all.nodes"));
		this.m_allNode.setLayoutData(
				new RowData(150,SizeConstant.SIZE_BUTTON_HEIGHT));
		
		// ????????????????????????????????????????????????????????????????????????
		this.m_retry = new Button(cmdScopeProcGroup, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_retry", this.m_retry);
		this.m_retry.setText(Messages.getString("scope.process.retry.nodes"));
		this.m_retry.setLayoutData(
				new RowData(250, SizeConstant.SIZE_BUTTON_HEIGHT));

		// ???????????????????????????????????????Composite
		JobDialogUtil.getComposite_Space(cmdScopeProcGroup, 100, SizeConstant.SIZE_BUTTON_HEIGHT);

		// ???????????????Composite???
		Composite monitorComposite = new Composite(this, SWT.NONE);
		monitorComposite.setLayout(new RowLayout());

		// ???????????????????????????
		label = new Label(monitorComposite, SWT.NONE);
		label.setText(Messages.getString("monitor.setting") + " : ");
		label.setLayoutData(new RowData(100,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// ???????????????????????????????????????
		this.m_monitorIdCombo = new Combo(monitorComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_monitorIdCombo", this.m_monitorIdCombo);
		this.m_monitorIdCombo.setLayoutData(new RowData(350,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_monitorIdCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo check = (Combo) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				String monitorTypeId = getMonitorTypeId(check.getText());
				if (monitorTypeId != null
					&& (monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S))) {
					m_waitTimeText.setEditable(true);
					m_waitEndValueText.setEditable(true);
				} else {
					m_waitTimeText.setEditable(false);
					m_waitEndValueText.setEditable(false);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// ????????????????????????????????????
		this.m_monitorReferBtn = new Button(monitorComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_monitorReferBtn", this.m_monitorReferBtn);
		this.m_monitorReferBtn.setText(Messages.getString("refer"));
		this.m_monitorReferBtn.setLayoutData(new RowData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_monitorReferBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_monitorIdCombo.getText() == null || m_monitorIdCombo.getText().isEmpty()) {
					return;
				}
				MonitorModifyAction action = new MonitorModifyAction();
				String monitorTypeId = getMonitorTypeId(m_monitorIdCombo.getText());
				String monitorId = getMonitorId(m_monitorIdCombo.getText());
				if (action.dialogOpen(m_shell, m_managerName, monitorTypeId, monitorId) 
						== IDialogConstants.OK_ID) {
					update();
				}
			}
		});

		// ???????????????????????????
		Group endValueGroup = new Group(this, SWT.NONE);
		endValueGroup.setText(Messages.getString("end.value"));
		endValueGroup.setLayout(new GridLayout(5, false));

		// ?????????????????????????????????
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("info") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_INFO);
		label.setLayoutData(new GridData(70, SizeConstant.SIZE_LABEL_HEIGHT));

		// ????????????????????????????????????
		this.m_infoEndValue = new Text(endValueGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_infoEndValue", this.m_infoEndValue);
		this.m_infoEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_infoEndValue.getLayoutData()).horizontalSpan = 2;
		this.m_infoEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_infoEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ?????????????????????????????????
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("warning") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_WARNING);
		label.setLayoutData(new GridData(70, SizeConstant.SIZE_LABEL_HEIGHT));

		// ????????????????????????????????????
		this.m_warnEndValue = new Text(endValueGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_warnEndValue", this.m_warnEndValue);
		this.m_warnEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_warnEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_warnEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ?????????????????????????????????
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("critical") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_CRITICAL);
		label.setLayoutData(new GridData(70, SizeConstant.SIZE_LABEL_HEIGHT));

		// ????????????????????????????????????
		this.m_criticalEndValue = new Text(endValueGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_criticalEndValue", this.m_criticalEndValue);
		this.m_criticalEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		((GridData)this.m_criticalEndValue.getLayoutData()).horizontalSpan = 2;
		this.m_criticalEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_criticalEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ?????????????????????????????????
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("unknown") + " : ");
		label.setBackground(PriorityColorConstant.COLOR_UNKNOWN);
		label.setLayoutData(new GridData(70, SizeConstant.SIZE_LABEL_HEIGHT));

		// ????????????????????????????????????
		this.m_unknownEndValue = new Text(endValueGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_unknownEndValue", this.m_unknownEndValue);
		this.m_unknownEndValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_unknownEndValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_unknownEndValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		JobDialogUtil.getGridSeparator(endValueGroup, 5);

		// ???????????????????????????????????????????????????????????????
		label = new Label(endValueGroup, SWT.NONE);
		label.setText(Messages.getString("job.monitorjob.result.end"));
		label.setLayoutData(new GridData());
		((GridData)label.getLayoutData()).horizontalSpan = 5;

		// ????????????????????????????????????????????????????????????
		label = new Label(endValueGroup, SWT.LEFT);
		label.setText(Messages.getString("time.out") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// ???????????????????????????????????????????????????????????????
		this.m_waitTimeText = new Text(endValueGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_waitTimeText", this.m_waitTimeText);
		this.m_waitTimeText.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_waitTimeText.addVerifyListener(
				new NumberVerifyListener(0, DataRangeConstant.SMALLINT_HIGH));
		this.m_waitTimeText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ??????????????????????????????????????????????????????????????????
		label = new Label(endValueGroup, SWT.LEFT);
		label.setText(Messages.getString("min"));
		label.setLayoutData(new GridData(30, SizeConstant.SIZE_LABEL_HEIGHT));

		// ???????????????????????????????????????????????????????????????
		label = new Label(endValueGroup, SWT.LEFT);
		label.setText("    " + Messages.getString("end.value") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// ??????????????????????????????????????????????????????????????????
		this.m_waitEndValueText = new Text(endValueGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_waitEndValueText", this.m_waitEndValueText);
		this.m_waitEndValueText.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_waitEndValueText.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_waitEndValueText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * ????????????
	 *
	 */
	@Override
	public void update(){
		// ?????????????????????
		if(this.m_scopeFixedValue.getSelection() && "".equals(this.m_scope.getText())){
			this.m_scope.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(this.m_scopeJobParam.getSelection() && "".equals(this.m_scopeJobParamText.getText())){
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scopeJobParamText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_infoEndValue.getText())){
			this.m_infoEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_infoEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_warnEndValue.getText())){
			this.m_warnEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_warnEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_criticalEndValue.getText())){
			this.m_criticalEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_criticalEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_unknownEndValue.getText())){
			this.m_unknownEndValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_unknownEndValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		String monitorTypeId = this.getMonitorTypeId(this.m_monitorIdCombo.getText());
		boolean isMonitorTrap = 
				monitorTypeId != null
				&& (monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S));
		if(isMonitorTrap && "".equals(this.m_waitEndValueText.getText())){
			this.m_waitEndValueText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_waitEndValueText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(isMonitorTrap && "".equals(this.m_waitTimeText.getText())){
			this.m_waitTimeText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_waitTimeText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ????????????????????????????????????????????????????????????
	 *
	 * @see com.clustercontrol.jobmanagement.bean.MonitorJobInfo
	 */
	public void reflectMonitorJobInfo() {

		// ?????????
		//????????????????????????????????????????????????"#[FACILITY_ID]"?????????
		this.m_scopeJobParam.setSelection(false);
		this.m_scopeJobParamText.setText(SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
		this.m_scopeFixedValue.setSelection(true);
		this.m_scope.setText("");
		this.m_allNode.setSelection(true);
		this.m_infoEndValue.setText(String.valueOf(MonitorJobConstant.INITIAL_END_VALUE_INFO));
		this.m_warnEndValue.setText(String.valueOf(MonitorJobConstant.INITIAL_END_VALUE_WARN));
		this.m_criticalEndValue.setText(String.valueOf(MonitorJobConstant.INITIAL_END_VALUE_CRITICAL));
		this.m_unknownEndValue.setText(String.valueOf(MonitorJobConstant.INITIAL_END_VALUE_UNKNOWN));
		this.m_waitEndValueText.setText(String.valueOf(MonitorJobConstant.INITIAL_END_VALUE_UNKNOWN));
		this.m_waitTimeText.setText(String.valueOf(MonitorJobConstant.INITIAL_WAIT_INTERVAL_MINUTE));

		// ????????????
		List<MonitorInfo> monitorInfoList = null;
		this.m_monitorIdMap = new ConcurrentHashMap<>();
		try {
			JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(this.m_managerName);
			monitorInfoList = wrapper.getMonitorListForJobMonitor(this.m_ownerRoleId);
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("reflectMonitorJobInfo() getMonitorListByMonitorTypeIds, " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		String selectMonitorIdLabel = "";
		if (monitorInfoList != null && monitorInfoList.size() > 0) {
			for (MonitorInfo monitorInfo : monitorInfoList) {
				String monitorIdLabel = getMonitorIdLabel(monitorInfo);
				if (m_monitor != null && m_monitor.getMonitorId().equals(monitorInfo.getMonitorId())) {
					selectMonitorIdLabel = monitorIdLabel;
				}
				this.m_monitorIdMap.put(monitorIdLabel, monitorInfo);
				this.m_monitorIdCombo.add(monitorIdLabel);
			}
		} else {
			this.m_monitorIdCombo.add("");
		}
		this.m_monitorIdCombo.setText(selectMonitorIdLabel);

		if (this.m_monitor != null) {
			//??????????????????
			this.m_facilityPath = HinemosMessage.replace(this.m_monitor.getScope());
			this.m_facilityId = this.m_monitor.getFacilityID();
			if (isParamFormat(this.m_facilityId)) {
				//??????????????????ID???????????????????????????
				this.m_facilityPath = "";
				this.m_scope.setText(this.m_facilityPath);
				this.m_scopeJobParam.setSelection(true);
				this.m_scopeJobParamText.setText(this.m_facilityId);
				this.m_scopeFixedValue.setSelection(false);
			} else{
				if (this.m_facilityPath != null && this.m_facilityPath.length() > 0) {
					this.m_scope.setText(this.m_facilityPath);
				}
				this.m_scopeJobParam.setSelection(false);
				this.m_scopeFixedValue.setSelection(true);
			}
			//??????????????????
			if (this.m_monitor.getProcessingMethod() == ProcessingMethodConstant.TYPE_ALL_NODE) {
				this.m_allNode.setSelection(true);
				this.m_retry.setSelection(false);
			} else {
				this.m_allNode.setSelection(false);
				this.m_retry.setSelection(true);
			}
	
			//????????????
			if (this.m_scopeJobParam.getSelection()) {
				this.m_scopeSelect.setEnabled(false);
			} else {
				this.m_scopeSelect.setEnabled(true);
			}

			// ?????????????????????
			if (this.m_monitor.getMonitorInfoEndValue() != null) {
				this.m_infoEndValue.setText(this.m_monitor.getMonitorInfoEndValue().toString());
			}
	
			// ?????????????????????
			if (this.m_monitor.getMonitorWarnEndValue() != null) {
				this.m_warnEndValue.setText(this.m_monitor.getMonitorWarnEndValue().toString());
			}
	
			// ?????????????????????
			if (this.m_monitor.getMonitorCriticalEndValue() != null) {
				this.m_criticalEndValue.setText(this.m_monitor.getMonitorCriticalEndValue().toString());
			}
	
			// ?????????????????????
			if (this.m_monitor.getMonitorUnknownEndValue() != null) {
				this.m_unknownEndValue.setText(this.m_monitor.getMonitorUnknownEndValue().toString());
			}
	
			// ?????????????????????????????????????????????????????????
			if (this.m_monitor.getMonitorWaitTime() != null) {
				this.m_waitTimeText.setText(this.m_monitor.getMonitorWaitTime().toString());
			}
	
			// ????????????????????????????????????????????????????????????
			if (this.m_monitor.getMonitorWaitEndValue() != null) {
				this.m_waitEndValueText.setText(this.m_monitor.getMonitorWaitEndValue().toString());
			}
		}
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param monitor ?????????????????????
	 */
	public void setMonitorJobInfo(MonitorJobInfo monitor) {
		m_monitor = monitor;
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @return ?????????????????????
	 */
	public MonitorJobInfo getMonitorJobInfo() {
		return m_monitor;
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????
	 *
	 * @return ????????????????????????
	 *
	 * @see com.clustercontrol.jobmanagement.bean.MonitorJobInfo
	 */
	public ValidateResult createMonitorJobInfo() {
		ValidateResult result = null;

		//????????????????????????????????????????????????????????????
		m_monitor = new MonitorJobInfo();

		//??????????????????
		if(m_scopeJobParam.getSelection()){
			//????????????????????????
			if (isParamFormat(m_scopeJobParamText.getText())) {
				//????????????????????????
				m_monitor.setFacilityID(m_scopeJobParamText.getText());
				m_monitor.setScope("");
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.4"));
				return result;
			}
		}
		else{
			//??????????????????
			if (m_facilityId != null && m_facilityId.length() > 0){
				m_monitor.setFacilityID(m_facilityId);
				m_monitor.setScope(m_facilityPath);
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.hinemos.3"));
				return result;
			}
		}

		//??????????????????
		if (m_allNode.getSelection()) {
			m_monitor
			.setProcessingMethod(ProcessingMethodConstant.TYPE_ALL_NODE);
		} else {
			m_monitor.setProcessingMethod(ProcessingMethodConstant.TYPE_RETRY);
		}

		// ????????????ID
		if (this.m_monitorIdCombo.getText().equals("")) {
			this.m_monitor.setMonitorId("");
		} else {
			this.m_monitor.setMonitorId(this.getMonitorId(this.m_monitorIdCombo.getText()));
		}
		if (this.m_monitor.getMonitorId().equals("")) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.monitor.57"));
			return result;
		}

		// ?????????????????????
		try {
			this.m_monitor.setMonitorInfoEndValue(Integer.parseInt(this.m_infoEndValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.153", 
					new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_INFO)}));
			return result;
		}

		// ?????????????????????
		try {
			this.m_monitor.setMonitorWarnEndValue(Integer.parseInt(this.m_warnEndValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.153", 
					new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_WARNING)}));
			return result;
		}

		// ?????????????????????
		try {
			this.m_monitor.setMonitorCriticalEndValue(Integer.parseInt(this.m_criticalEndValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.153", 
					new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_CRITICAL)}));
			return result;
		}

		// ?????????????????????
		try {
			this.m_monitor.setMonitorUnknownEndValue(Integer.parseInt(this.m_unknownEndValue.getText()));
		} catch (NumberFormatException e) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.153", 
					new String[]{PriorityMessage.typeToString(PriorityConstant.TYPE_UNKNOWN)}));
			return result;
		}

		// ???????????????????????????????????????????????????????????????????????????
		String monitorTypeId = this.getMonitorTypeId(this.m_monitorIdCombo.getText());
		if (monitorTypeId != null
			&& (monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S))) {
			try {
				this.m_monitor.setMonitorWaitTime(Integer.parseInt(this.m_waitTimeText.getText()));
			} catch (NumberFormatException e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.154"));
				return result;
			}
			try {
				this.m_monitor.setMonitorWaitEndValue(Integer.parseInt(this.m_waitEndValueText.getText()));
			} catch (NumberFormatException e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.155"));
				return result;
			}
		}

		return null;
	}

	/**
	 * @param ownerRoleId
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
		this.m_scope.setText("");
		this.m_facilityId = null;
	}

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	/**
	 * @param m_managerName the m_managerName to set
	 */
	public void setManagerName(String m_managerName) {
		this.m_managerName = m_managerName;
	}

	/**
	 * ????????????ID???????????????
	 * 
	 * @param monitorIdLabel ?????????????????????
	 * @return ????????????ID
	 */
	private String getMonitorTypeId(String monitorIdLabel) {
		if (monitorIdLabel == null || this.m_monitorIdMap == null) {
			return null;
		}
		MonitorInfo monitorInfo = this.m_monitorIdMap.get(monitorIdLabel);
		if (monitorInfo == null) {
			return null;
		} else {
			return monitorInfo.getMonitorTypeId();
		}
	}

	/**
	 * ????????????ID???????????????
	 * 
	 * @param monitorIdLabel ?????????????????????
	 * @return ????????????ID
	 */
	private String getMonitorId(String monitorIdLabel) {
		MonitorInfo monitorInfo = this.m_monitorIdMap.get(monitorIdLabel);
		if (monitorInfo == null) {
			return null;
		} else {
			return monitorInfo.getMonitorId();
		}
	}

	/**
	 * ????????????????????????????????????
	 * 
	 * @param monitorInfo ????????????
	 * @return ?????????????????????
	 */
	private String getMonitorIdLabel(MonitorInfo monitorInfo) {
		String pluginName = "";
		if (monitorInfo == null || monitorInfo.getMonitorTypeId() == null) {
			m_log.warn("monitorInfo=" + monitorInfo);
			pluginName = "";
			return null;
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_AGENT)) {
			pluginName = Messages.getString("agent.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_HTTP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_HTTP_S)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_HTTP_SCENARIO)) {
			pluginName = Messages.getString("http.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
			pluginName = Messages.getString("performance.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PING)) {
			pluginName = Messages.getString("ping.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PORT)) {
			pluginName = Messages.getString("port.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PROCESS)) {
			pluginName = Messages.getString("process.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMP_S)) {
			pluginName = Messages.getString("snmp.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SQL_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SQL_S)) {
			pluginName = Messages.getString("sql.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)) {
			pluginName = Messages.getString("systemlog.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			pluginName = Messages.getString("logfile.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGCOUNT)) {
			pluginName = Messages.getString("logcount.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)) {
			pluginName = Messages.getString("binary.file.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)) {
			pluginName = Messages.getString("packet.capture.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			pluginName = Messages.getString("custom.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
			pluginName = Messages.getString("snmptrap.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINSERVICE)) {
			pluginName = Messages.getString("winservice.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
			pluginName = Messages.getString("winevent.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_JMX)) {
			pluginName = Messages.getString("jmx.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
			pluginName = Messages.getString("customtrap.monitor");
		} else {
			pluginName = monitorInfo.getMonitorTypeId();
		}

		return String.format("%s (%s[%s])", 
				monitorInfo.getMonitorId(),
				pluginName, 
				MonitorTypeMessage.typeToString(monitorInfo.getMonitorType()));
	}
	/**
	 * ??????????????????????????????????????????????????????
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_scope.setEditable(false);
		this.m_scopeJobParam.setEnabled(enabled);
		this.m_scopeJobParamText.setEditable(m_scopeJobParam.getSelection() && enabled);
		this.m_scopeFixedValue.setEnabled(enabled);
		this.m_scopeSelect.setEnabled(enabled);
		this.m_allNode.setEnabled(enabled);
		this.m_retry.setEnabled(enabled);
		this.m_monitorIdCombo.setEnabled(enabled);
		this.m_infoEndValue.setEditable(enabled);
		this.m_warnEndValue.setEditable(enabled);
		this.m_criticalEndValue.setEditable(enabled);
		this.m_unknownEndValue.setEditable(enabled);
		String monitorTypeId = this.getMonitorTypeId(this.m_monitorIdCombo.getText());
		if (monitorTypeId != null
			&& (monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S))) {
			this.m_waitTimeText.setEditable(enabled);
			this.m_waitEndValueText.setEditable(enabled);
		} else {
			this.m_waitTimeText.setEditable(false);
			this.m_waitEndValueText.setEditable(false);
		}
	}

	/**
	 * str???????????????????????????(#[xxx])???????????????????????????
	 * 
	 * @param str
	 * @return
	 */
	private boolean isParamFormat(String str) {
		if (str == null) {
			return false;
		}
		return str.startsWith(SystemParameterConstant.PREFIX)
				&& str.endsWith(SystemParameterConstant.SUFFIX);
	}
}
