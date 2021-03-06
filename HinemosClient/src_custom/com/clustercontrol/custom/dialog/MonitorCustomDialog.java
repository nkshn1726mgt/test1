/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.custom.dialog;

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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.TextWithParameterComposite;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.bean.ConvertValueMessage;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.monitor.CommandExecType;
import com.clustercontrol.ws.monitor.CustomCheckInfo;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * ???????????????????????????????????????????????????<br/>
 *
 * @version 4.0.0
 * @since 2.4.0
 */
public class MonitorCustomDialog extends CommonMonitorNumericDialog {

	// ??????
	private static Log m_log = LogFactory.getLog( MonitorCustomDialog.class );

	// ----- instance ??????????????? ----- //
	/** ????????????????????????????????????????????? */
	private Text m_textTimeout = null;
	private Button checkSelected = null;	// checkbox(??????????????????????????????????????????????????????)
	private Text textNode = null;			// ??????????????????????????????????????????????????????????????????????????????????????????
	private Button buttonNode = null;		// ?????????????????????(???????????????????????????????????????????????????)
	private Button buttonAgentUser = null; //????????????????????????????????????????????????????????????
	private Button buttonSpecifyUser = null; //???????????????????????????????????????????????????
	private Text textEffectiveUser = null;	// ??????????????????????????????????????????????????????
	private TextWithParameterComposite textCommand = null;		//  ????????????????????????????????????????????????????????????

	/** ?????????????????? */
	private Combo m_comboConvertValue = null;

	private String nodeFacilityId = null;	// ????????????????????????????????????????????????ID

	// command??????????????????????????????????????????
	public static final int TIMEOUT_SEC_COMMAND = 15000;
	/**
	 * ?????????????????????(?????????)<br/>
	 *
	 * @param parent ?????????????????????????????????
	 */
	public MonitorCustomDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * ?????????????????????(?????????)<br/>
	 *
	 * @param parent ???????????????????????????????????????
	 * @param managerName ??????????????????
	 * @param monitorId ??????????????????????????????????????????????????????ID
	 * @param updateFlg ????????????????????????true:?????????false:???????????????
	 */
	public MonitorCustomDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	/**
	 * ???????????????????????????????????????????????????<br/>
	 *
	 * @param parent ??????????????????????????????
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// Local Variables
		Label label = null;		// ???????????????????????????????????????
		GridData gridData = null;	// ???????????????????????????????????????????????????

		// MAIN

		// ????????????????????????
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");

		super.customizeDialog(parent);

		// ?????????????????????
		shell.setText(Messages.getString("dialog.monitor.custom.edit"));

		// ??????????????????????????????????????????
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		groupCheckRule.setText(Messages.getString("check.rule"));
		GridLayout layout = new GridLayout(15, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		groupCheckRule.setLayout(layout);

		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = BASIC_UNIT;
		groupCheckRule.setLayoutData(gridData);

		// checkbox?????????(?????????????????????????????????????????????)
		this.checkSelected = new Button(groupCheckRule, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "selectedCheck", checkSelected);
		this.checkSelected.setText(Messages.getString("monitor.custom.type.selected"));
		this.checkSelected.setToolTipText(Messages.getString("monitor.custom.type.selected.tips"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 7;
		checkSelected.setLayoutData(gridData);
		checkSelected.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				if (button.getSelection()) {
					textNode.setEnabled(true);
					buttonNode.setEnabled(true);
				} else {
					textNode.setEnabled(false);
					buttonNode.setEnabled(false);
				}
				update();
			}
		});

		// ?????????????????????????????????????????????????????????????????????
		this.textNode = new Text(groupCheckRule, SWT.BORDER | SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "nodetext", textNode);
		this.textNode.setText("");
		this.textNode.setMessage(Messages.getString("monitor.custom.node.selected"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 6;
		this.textNode.setLayoutData(gridData);
		this.textNode.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});;

		this.buttonNode = new Button(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "node", buttonNode);
		this.buttonNode.setText(Messages.getString("refer"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 2;
		this.buttonNode.setLayoutData(gridData);
		this.buttonNode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// ??????????????????
				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

				String managerName = getMonitorBasicScope().getManagerListComposite().getText();
				ScopeTreeDialog dialog = new ScopeTreeDialog(shell, managerName, getMonitorBasicScope().getOwnerRoleId(), false, false);
				dialog.setSelectNodeOnly(true);
				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem item = dialog.getSelectItem();
					FacilityInfo info = item.getData();
					nodeFacilityId = info.getFacilityId();
					if (info.getFacilityType() == FacilityConstant.TYPE_NODE) {
						textNode.setText(info.getFacilityName());
					} else {
						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						textNode.setText(path.getPath(item));
					}
				}
			}
		});

		// ??????????????????????????????????????????
		Group groupEffectiveUser = new Group(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "effectiveuser", groupEffectiveUser);
		groupEffectiveUser.setText(Messages.getString("effective.user"));
		layout = new GridLayout(15, true);
		layout.marginWidth = HALF_MARGIN;
		layout.marginHeight = HALF_MARGIN;
		groupEffectiveUser.setLayout(layout);

		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 15;
		groupEffectiveUser.setLayoutData(gridData);

		this.buttonAgentUser = new Button(groupEffectiveUser, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "agentuser", buttonAgentUser);
		this.buttonAgentUser.setText(Messages.getString("agent.user"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = SMALL_UNIT;
		this.buttonAgentUser.setLayoutData(gridData);
		this.buttonAgentUser.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					buttonSpecifyUser.setSelection(false);
					textEffectiveUser.setEnabled(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		this.buttonSpecifyUser = new Button(groupEffectiveUser, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "specifyuser", buttonSpecifyUser);
		this.buttonSpecifyUser.setText(Messages.getString("specified.user"));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = WIDTH_TEXT_SHORT;
		this.buttonSpecifyUser.setLayoutData(gridData);
		this.buttonSpecifyUser.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					buttonAgentUser.setSelection(false);
					textEffectiveUser.setEnabled(true);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		this.textEffectiveUser = new Text(groupEffectiveUser, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, "effectiveuser", textEffectiveUser);
		this.textEffectiveUser.setText("");
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = HALF_UNIT - (SMALL_UNIT + WIDTH_TEXT_SHORT);
		this.textEffectiveUser.setLayoutData(gridData);
		this.textEffectiveUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "customcommand", label);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		label.setText(Messages.getString("monitor.custom.command") + " : ");
		gridData.horizontalSpan = WIDTH_TITLE;
		label.setLayoutData(gridData);

		this.textCommand = new TextWithParameterComposite(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "commnad", textCommand);
		this.textCommand.setText("");
		String tooltipText = Messages.getString("monitor.custom.commandline.tips") + Messages.getString("replace.parameter.node");
		this.textCommand.setToolTipText(tooltipText);
		this.textCommand.setColor(new Color(parent.getDisplay(), new RGB(0, 0, 255)));
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gridData.horizontalSpan = 11;
		this.textCommand.setLayoutData(gridData);
		this.textCommand.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});;

		/*
		 * ??????????????????
		 */
		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "timeout", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("time.out") + " : ");

		// ????????????
		this.m_textTimeout = new Text(groupCheckRule, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "timeout", m_textTimeout);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textTimeout.setLayoutData(gridData);
		this.m_textTimeout.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ?????????????????????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "millisec", label);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_VALUE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("milli.sec"));

		// ??????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank", label);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * ??????????????????
		 */
		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("convert.value") + " : ");
		// ?????????????????????
		this.m_comboConvertValue = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "convertvalue", m_comboConvertValue);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT_SHORT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboConvertValue.setLayoutData(gridData);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_NO);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_DELTA);

		// ???????????????????????????????????????
		this.adjustDialog();

		// ????????????
		MonitorInfo info = null;
		if (this.monitorId == null) {
			// ?????????????????????
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
		} else {
			// ???????????????
			try {
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(this.monitorId);
			} catch (InvalidRole_Exception e) {
				// ????????????????????????????????????????????????????????????????????????
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
				return;
			} catch (Exception e) {
				// ?????????????????????
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				return;
			}
		}
		this.setInputData(info);
		update();
	}

	/**
	 * ????????????
	 */
	@Override
	public void update(){
		super.update();

		// ??????????????????????????????????????????????????????????????????
		if (checkSelected.getSelection() && "".equals(this.textNode.getText())) {
			this.textNode.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.textNode.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ??????????????????????????????????????????????????????????????????
		if (buttonSpecifyUser.getSelection() && "".equals(this.textEffectiveUser.getText())) {
			this.textEffectiveUser.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.textEffectiveUser.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ????????????????????????????????????????????????????????????
		if ("".equals(this.textCommand.getText())) {
			this.textCommand.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.textCommand.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ?????????????????????????????????????????????????????????
		if(this.m_textTimeout.getEnabled() && "".equals(this.m_textTimeout.getText())){
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @param monitor
	 *			???????????????????????????????????????
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		// ????????????????????????????????????
		CustomCheckInfo customInfo = monitor.getCustomCheckInfo();
		if (customInfo == null) {
			customInfo = new CustomCheckInfo();
			customInfo.setTimeout(TIMEOUT_SEC_COMMAND);
			this.checkSelected.setSelection(false);
			this.textNode.setEnabled(false);
			this.buttonNode.setEnabled(false);
			this.buttonAgentUser.setSelection(true);
			this.buttonSpecifyUser.setSelection(false);
			this.textEffectiveUser.setEnabled(false);
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(ConvertValueConstant.TYPE_NO));
		} else {
			if (customInfo.getCommandExecType() == CommandExecType.INDIVIDUAL) {
				this.checkSelected.setSelection(false);
			} else {
				this.checkSelected.setSelection(true);
				this.nodeFacilityId = customInfo.getSelectedFacilityId();

				String facilityPath = null;
				String managerName = this.getManagerName();
				try {
					RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
					facilityPath = wrapper.getFacilityPath(this.nodeFacilityId, null);
				} catch (com.clustercontrol.ws.repository.InvalidRole_Exception e) {
					// ???????????????????????????????????????????????????????????????
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					// ?????????????????????
					m_log.warn("setInputData() getFacilityPath, " + HinemosMessage.replace(e.getMessage()), e);
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
				}
				this.textNode.setText(facilityPath);
			}
			if (customInfo.isSpecifyUser().booleanValue()) {
				this.buttonAgentUser.setSelection(false);
				this.buttonSpecifyUser.setSelection(true);
				this.textEffectiveUser.setEnabled(true);
			} else {
				this.buttonAgentUser.setSelection(true);
				this.buttonSpecifyUser.setSelection(false);
				this.textEffectiveUser.setEnabled(false);
			}
			this.textEffectiveUser.setText(customInfo.getEffectiveUser());
			this.textCommand.setText(customInfo.getCommand());
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(customInfo.getConvertFlg()));
		}
		this.m_textTimeout.setText(Integer.toString(customInfo.getTimeout()));

		m_numericValueInfo.setInputData(monitor);
	}

	/**
	 * ??????????????????????????????????????????????????????
	 *
	 * @return ????????????????????????????????????
	 */
	@Override
	protected MonitorInfo createInputData() {
		// Local Variables
		CustomCheckInfo customInfo = null;

		// MAIN
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// ????????????????????????
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOM_N);

		// ????????????????????????????????????
		customInfo = new CustomCheckInfo();
		customInfo.setTimeout(TIMEOUT_SEC_COMMAND);
		customInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_CUSTOM_N);
		customInfo.setMonitorId(monitorInfo.getMonitorId());
		customInfo.setConvertFlg(ConvertValueConstant.TYPE_NO);
		monitorInfo.setCustomCheckInfo(customInfo);

		// ?????????????????????????????????
		if (! this.checkSelected.getSelection()) {
			customInfo.setCommandExecType(CommandExecType.INDIVIDUAL);
		} else {
			customInfo.setCommandExecType(CommandExecType.SELECTED);
			customInfo.setSelectedFacilityId(nodeFacilityId);
		}

		// ????????????????????????
		if (this.buttonSpecifyUser.getSelection()) {
			customInfo.setSpecifyUser(true);
		} else {
			customInfo.setSpecifyUser(false);
		}
		customInfo.setEffectiveUser(this.textEffectiveUser.getText());

		// ??????????????????????????????
		customInfo.setCommand(this.textCommand.getText());

		// ???????????????????????????
		try {
			customInfo.setTimeout(Integer.parseInt(this.m_textTimeout.getText()));
		} catch (NumberFormatException e) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.custom.msg.timeout.invalid"));
			return null;
		}

		// ?????????????????????
		if (!"".equals(this.m_comboConvertValue.getText().trim())) {
			customInfo.setConvertFlg(Integer.valueOf(ConvertValueMessage.stringToType(this.m_comboConvertValue.getText())));
		}
		// ?????????????????????
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			return null;
		}

		// ?????????????????????
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if (validateResult.getID() == null) {
				if (! displayQuestion(validateResult)) {	// ??????ID?????????????????????????????????
					validateResult = null;
					return null;
				}
			} else {
				return null;	// ?????????????????????????????????????????????
			}
		}

		return monitorInfo;
	}

	/**
	 * ?????????????????????????????????????????????<br/>
	 *
	 * @return ????????????????????????true, ????????????false
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		MonitorInfo info = this.inputData;
		String managerName = this.getManagerName();
		if (info != null) {
			String[] args = { info.getMonitorId(), managerName };
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			if (!this.updateFlg) {
				// ?????????????????????
				try {
					result = wrapper.addMonitor(info);

					if (result) {
						// ??????????????????????????????????????????
						MessageDialog.openInformation(
								null,
								Messages.getString("successful"),
								Messages.getString("message.monitor.33", args));
					} else {
						// ??????????????????????????????????????????
						MessageDialog.openError(
								null,
								Messages.getString("failed"),
								Messages.getString("message.monitor.34", args));
					}
				} catch (MonitorDuplicate_Exception e) {
					// ????????????????????????ID????????????????????????????????????
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.monitor.53", args));
				} catch (Exception e) {
					String errMessage = "";
					if (e instanceof InvalidRole_Exception) {
						// ???????????????????????????????????????????????????????????????
						MessageDialog.openInformation(
								null,
								Messages.getString("message"),
								Messages.getString("message.accesscontrol.16"));
					} else {
						errMessage = ", " + HinemosMessage.replace(e.getMessage());
					}

					// ??????????????????????????????????????????
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);
				}
			} else {
				// ???????????????
				String errMessage = "";
				try {
					result = wrapper.modifyMonitor(info);
				} catch (InvalidRole_Exception e) {
					// ???????????????????????????????????????????????????????????????
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}

				if (result) {
					// ??????????????????????????????????????????
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
				} else {
					// ??????????????????????????????????????????
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}

			}
		}

		return result;
	}
}
