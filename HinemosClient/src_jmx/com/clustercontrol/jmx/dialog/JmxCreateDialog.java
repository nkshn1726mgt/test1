/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.dialog;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.bean.ConvertValueMessage;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jmxmaster.JmxMasterEndpoint;
import com.clustercontrol.ws.jmxmaster.JmxMasterEndpointService;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.JmxCheckInfo;
import com.clustercontrol.ws.monitor.JmxMasterInfo;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * JMX?????????????????????????????????????????????
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class JmxCreateDialog extends CommonMonitorNumericDialog {

	// ??????
	private static Log m_log = LogFactory.getLog( JmxCreateDialog.class );

	// ----- instance ??????????????? ----- //

	/** ???????????? */
	private Combo m_comboCollectorItem = null;

	/** JMX?????????????????? */
	private List<JmxMasterInfo> m_master = null;

	/** ????????? */
	private Text m_textPort = null;

	/** ????????? */
	private Text m_textUser = null;

	/** ??????????????? */
	private Text m_textPassword = null;

	/** ?????????????????? */
	private Combo m_comboConvertValue = null;

	// ----- ????????????????????? ----- //

	/**
	 * ???????????????????????????????????????????????????????????????
	 *
	 * @param parent
	 *            ?????????????????????????????????
	 */
	public JmxCreateDialog(Shell parent) {
		super(parent, null);
	}

	/**
	 * ???????????????????????????????????????????????????????????????
	 *
	 * @param parent
	 *            ?????????????????????????????????
	 * @param managerName
	 *            ??????????????????
	 * @param notifyId
	 *            ??????????????????ID
	 * @param updateFlg
	 *            ????????????????????????true:?????????false:???????????????
	 */
	public JmxCreateDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);
		this.managerName = managerName;
		this.monitorId = monitorId;
		this.updateFlg = updateFlg;
	}

	// ----- instance ???????????? ----- //

	/**
	 * ?????????????????????????????????????????????
	 *
	 * @param parent
	 *            ????????????????????????
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		// ????????????????????????????????????
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");

		super.customizeDialog(parent);
		itemName.setEditable(false);
		measure.setEditable(false);

		// ????????????
		shell.setText(Messages.getString("dialog.monitor.jmx.create.modify"));

		// ???????????????????????????????????????
		Label label = null;
		// ???????????????????????????????????????????????????
		GridData gridData = null;

		/*
		 * ????????????????????????????????????????????????????????????????????????
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "checkrule", groupCheckRule);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = BASIC_UNIT;
		groupCheckRule.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = BASIC_UNIT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		groupCheckRule.setLayoutData(gridData);
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * ????????????
		 */
		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "monitoritem", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("monitor.item") + " : ");
		// ?????????????????????
		this.m_comboCollectorItem =	new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "collectoritem", m_comboCollectorItem);
		gridData = new GridData();
		gridData.horizontalSpan = 22;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboCollectorItem.setLayoutData(gridData);

		createComboCollectorItem();

		// ?????????????????????????????????????????????
		m_comboCollectorItem.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0){
				if (m_comboCollectorItem.getSelectionIndex() != -1) {
					itemName.setText(HinemosMessage.replace(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getName()));
					measure.setText(HinemosMessage.replace(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getMeasure()));
					update();
				}
			}
		});

		//????????????????????????????????????
		if(!updateFlg) {
			this.getMonitorBasicScope().getManagerListComposite()
			.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					itemName.setText(Messages.getString("select.value"));
					measure.setText(Messages.getString("collection.unit"));
					createComboCollectorItem();
				}
			});
		}

		/*
		 * ????????????
		 */
		// ????????????????????????
		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "port", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("port") + " : ");

		// ????????????
		this.m_textPort = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textPort);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPort.setLayoutData(gridData);
		this.m_textPort.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ??????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank1", label);
		gridData = new GridData();
		gridData.horizontalSpan = 16;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ???????????????????????????
		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "user", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("user") + " : ");

		// ????????????
		this.m_textUser = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, null, m_textUser);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textUser.setLayoutData(gridData);
		this.m_textUser.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ??????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank2", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		// ??????????????????????????????

		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "password", label);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("password") + " : ");

		// ????????????
		this.m_textPassword = new Text(groupCheckRule, SWT.BORDER | SWT.LEFT | SWT.SINGLE | SWT.PASSWORD);
		WidgetTestUtil.setTestId(this, null, m_textPassword);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_textPassword.setLayoutData(gridData);
		this.m_textPassword.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ??????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank3", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);

		/*
		 * ??????????????????
		 */
		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE_LONG;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(Messages.getString("convert.value") + " : ");
		// ?????????????????????
		this.m_comboConvertValue = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "convertvalue", m_comboConvertValue);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TEXT;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboConvertValue.setLayoutData(gridData);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_NO);
		this.m_comboConvertValue.add(ConvertValueMessage.STRING_DELTA);

		// ??????
		label = new Label(groupCheckRule, SWT.NONE);
		WidgetTestUtil.setTestId(this, "blank4", label);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);


		// ????????????????????????
		this.adjustDialog();

		// ????????????
		MonitorInfo info = null;
		if(this.monitorId == null){
			// ???????????????
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
			this.setInputData(info);
		} else {
			// ??????????????????????????????
			try {
				MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
				info = wrapper.getMonitor(this.monitorId);
				this.setInputData(info);
			} catch (InvalidRole_Exception e) {
				// ????????????????????????????????????????????????????????????????????????
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));

			} catch (Exception e) {
				// ?????????????????????
				m_log.warn("customizeDialog() getMonitor, " + HinemosMessage.replace(e.getMessage()), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
	}

	/**
	 * ????????????
	 *
	 */
	@Override
	protected void update() {
		super.update();

		if("".equals(this.m_textPort.getText().trim())){
			this.m_textPort.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_textPort.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		try{
			// ????????????????????????????????????????????????????????????????????????
			item1 = Messages.getString("select.value") 
					+ "(" + HinemosMessage.replace(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getMeasure()) 
					+ ")";
			item2 = item1;

			this.m_numericValueInfo.setTextItem1(item1);
			this.m_numericValueInfo.setTextItem2(item2);

		}catch(NullPointerException e){
			// ?????????????????????????????????????????????
		}

	}
	/**
	 * ??????????????????????????????????????????
	 *
	 * @param monitor
	 *            ???????????????????????????????????????
	 */
	@Override
	protected void setInputData(MonitorInfo monitor) {
		super.setInputData(monitor);

		this.inputData = monitor;

		JmxCheckInfo info = monitor.getJmxCheckInfo();
		if(info == null){
			info = new JmxCheckInfo();
		}

		if(info.getMasterId() != null){
			for(JmxMasterInfo master :this.m_master){
				if(master.getId().equals(info.getMasterId())){
					this.m_comboCollectorItem.select(this.m_comboCollectorItem.indexOf(HinemosMessage.replace(master.getName())));
				}
			}
		}

		if(info.getPort() != null){
			this.m_textPort.setText(String.valueOf(info.getPort()));
		}

		if(info.getAuthUser() != null){
			this.m_textUser.setText(info.getAuthUser());
		}

		if(info.getAuthPassword() != null){
			this.m_textPassword.setText(info.getAuthPassword());
		}

		if (info.getConvertFlg() != null) {
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(info.getConvertFlg()));
		} else {
			this.m_comboConvertValue.setText(ConvertValueMessage.typeToString(ConvertValueConstant.TYPE_NO));
		}
		m_numericValueInfo.setInputData(monitor);

		this.update();
	}

	/**
	 * ??????????????????????????????????????????????????????
	 *
	 * @return ????????????????????????????????????
	 */
	@Override
	protected MonitorInfo createInputData() {
		super.createInputData();
		if(validateResult != null){
			return null;
		}

		// JMX???????????????????????????
		monitorInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_JMX);
		monitorInfo.setMonitorType(MonitorTypeConstant.TYPE_NUMERIC);

		// JMX?????????????????????
		JmxCheckInfo jmxCheckInfo = new JmxCheckInfo();
		jmxCheckInfo.setMonitorTypeId(HinemosModuleConstant.MONITOR_JMX);
		jmxCheckInfo.setMonitorId(monitorInfo.getMonitorId());
		jmxCheckInfo.setMasterId(((JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText())).getId());
		jmxCheckInfo.setPort(Integer.valueOf(this.m_textPort.getText()));
		jmxCheckInfo.setConvertFlg(ConvertValueConstant.TYPE_NO);
		if(!"".equals(this.m_textUser.getText().trim())){
			jmxCheckInfo.setAuthUser(this.m_textUser.getText());
		}
		if(!"".equals(this.m_textPassword.getText().trim())){
			jmxCheckInfo.setAuthPassword(this.m_textPassword.getText());
		}
		if (!"".equals(this.m_comboConvertValue.getText().trim())) {
			jmxCheckInfo.setConvertFlg(Integer.valueOf(ConvertValueMessage.stringToType(this.m_comboConvertValue.getText())));
		}

		monitorInfo.setJmxCheckInfo(jmxCheckInfo);

		// ?????????????????????
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// ??????????????????????????????????????????????????????
		validateResult = m_notifyInfo.createInputData(monitorInfo);
		if (validateResult != null) {
			if(validateResult.getID() == null){	// ??????ID???????????????
				if(!displayQuestion(validateResult)){
					validateResult = null;
					return null;
				}
			}
			else{	// ?????????????????????????????????????????????
				return null;
			}
		}
		return monitorInfo;
	}

	/**
	 * ????????????????????????????????????????????????
	 *
	 * @return true????????????false?????????
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#action()
	 */
	@Override
	protected boolean action() {
		boolean result = false;

		MonitorInfo info = this.inputData;
		JmxMasterInfo selectJmxMasterInfo = (JmxMasterInfo)m_comboCollectorItem.getData(m_comboCollectorItem.getText());
		info.setItemName(selectJmxMasterInfo.getName());
		info.setMeasure(selectJmxMasterInfo.getMeasure());
		String managerName = this.getManagerName();
		MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
		String[] args = { info.getMonitorId(), managerName };
		if(!this.updateFlg){
			// ???????????????
			try {
				result = wrapper.addMonitor(info);

				if(result){
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.33", args));
				} else {
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args));
				}
			} catch (MonitorDuplicate_Exception e) {
				// ????????????ID?????????????????????????????????????????????????????????????????????
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.monitor.53", args));

			} catch (Exception e) {
				String errMessage = "";
				if (e instanceof InvalidRole_Exception) {
					// ????????????????????????????????????????????????????????????????????????
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} else {
					errMessage = ", " + HinemosMessage.replace(e.getMessage());
				}

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
				// ????????????????????????????????????????????????????????????????????????
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (Exception e) {
				errMessage = ", " + HinemosMessage.replace(e.getMessage());
			}

			if(result){
				MessageDialog.openInformation(
						null,
						Messages.getString("successful"),
						Messages.getString("message.monitor.35", args));
			} else {
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.monitor.36", args) + errMessage);
			}
		}
		return result;
	}


	/**
	 * ?????????????????????????????????????????????????????????
	 * <p>
	 *
	 * @return ValidateResult??????????????????
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;


		if ("".equals((this.m_comboCollectorItem.getText()).trim())) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required", new Object[]{Messages.getString("monitor.item")}));
			return this.validateResult;
		}
		if ("".equals((this.m_textPort.getText()).trim())) {
			this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("port")}));
			return this.validateResult;
		} else {
			try{
				Integer.valueOf(this.m_textPort.getText().trim());
			}
			catch(NumberFormatException e){
				this.setValidateResult(Messages.getString("message.hinemos.1"),Messages.getString("message.monitor.http.scenario.required.numeric", new Object[]{Messages.getString("port")}));
				return this.validateResult;
			}
		}

		result = super.validate();
		return result;
	}

	private List<JmxMasterInfo> getJmxMasterInfoList(String managerName) throws com.clustercontrol.ws.jmxmaster.HinemosUnknown_Exception, com.clustercontrol.ws.jmxmaster.InvalidRole_Exception, com.clustercontrol.ws.jmxmaster.InvalidUserPass_Exception {
		WebServiceException wse = null;
		EndpointUnit endpointUnit = EndpointManager.get(managerName);
		for (EndpointSetting<JmxMasterEndpoint> endpointSetting : endpointUnit.getEndpoint(JmxMasterEndpointService.class, JmxMasterEndpoint.class)) {
			try {
				JmxMasterEndpoint endpoint = endpointSetting.getEndpoint();
				return endpoint.getJmxMasterInfoList();
			} catch (WebServiceException e) {
				wse = e;
				m_log.warn("getJmxMasterInfoList(), " + e.getMessage(), e);
				endpointUnit.changeEndpoint();
			}
		}
		throw wse;
	}

	private void createComboCollectorItem() {
		try {
			if (this.m_master != null) {
				this.m_master.clear();
			}
			this.m_master = getJmxMasterInfoList(this.getManagerName());
		} catch (com.clustercontrol.ws.jmxmaster.HinemosUnknown_Exception
				| com.clustercontrol.ws.jmxmaster.InvalidRole_Exception
				| com.clustercontrol.ws.jmxmaster.InvalidUserPass_Exception e1) {
			m_log.warn(e1.getMessage(), e1);
		}
		if(this.m_master != null){
			this.m_comboCollectorItem.removeAll();

			List<JmxMasterInfo> cpMasterList = new ArrayList<>();
			List<JmxMasterInfo> cpMasterListHnms = new ArrayList<>();

			for(JmxMasterInfo info : this.m_master){
				if(HinemosMessage.replace(info.getName()).startsWith("[Hinemos]")){
					cpMasterListHnms.add(info);
				}else {
					cpMasterList.add(info);
				}
			}

			cpMasterList.addAll(cpMasterListHnms);
			this.m_master = cpMasterList;
			for(JmxMasterInfo info : this.m_master){
				this.m_comboCollectorItem.add(HinemosMessage.replace(info.getName()));
				this.m_comboCollectorItem.setData(HinemosMessage.replace(info.getName()), info);
			}
		}
	}
}
