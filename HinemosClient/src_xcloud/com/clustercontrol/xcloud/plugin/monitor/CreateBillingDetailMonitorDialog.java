/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.monitor;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.dialog.ScopeTreeDialog;
import com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.monitor.run.dialog.CommonMonitorNumericDialog;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.repository.FacilityPath;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.MonitorDuplicate_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorPluginStringInfo;
import com.clustercontrol.ws.monitor.PluginCheckInfo;
import com.clustercontrol.ws.repository.FacilityInfo;
import com.clustercontrol.ws.repository.FacilityTreeItem;
import com.clustercontrol.xcloud.common.CloudConstants;
import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.common.MessageManager;
import com.clustercontrol.xcloud.util.ControlUtil;

/**
 * ?????????????????????????????????????????????????????????????????????<BR>
 * 
 * @version 6.0.0
 * @since 2.0.0
 */
public class CreateBillingDetailMonitorDialog extends CommonMonitorNumericDialog {
	
	public enum MonitorKind{
		sum,delta;
	}
	
	MessageManager messages = CloudConstants.bundle_messages;
	
	String strFee = messages.getString("word.fee");
	String strTarget = messages.getString("word.target");
	String strSeparator = messages.getString("caption.title_separator");
	String msgSelectTarget = messages.getString("message.select_subject", new Object[]{"word.target"});
	
	String strBillingDetailMonitorDialog = messages.getString("caption.create_billing_detail_monitor_dialog");
	
	private static final String key_facilityType = "FacilityType";
	private static final String key_monitorKind = "MonitorKind";
	
	private static final String type_scope = FacilityConstant.TYPE_SCOPE_STRING;
	private static final String type_node = FacilityConstant.TYPE_NODE_STRING;
	
	MonitorBasicScopeComposite m_monitorBasic_;

	// ??????
	private static final Log logger = LogFactory.getLog(CreateBillingDetailMonitorDialog.class);

	/** ??????????????? */
	private Combo cmbTarget = null;

	// ----- ????????????????????? ----- //

	/**
	 * ???????????????????????????????????????????????????????????????
	 * 
	 * @param parent
	 *            ?????????????????????????????????
	 */
	public CreateBillingDetailMonitorDialog(Shell parent, String managerName) {
		super(parent, managerName);
	}

	/**
	 * ???????????????????????????????????????????????????????????????
	 * 
	 * @param parent
	 *            ?????????????????????????????????
	 * @param monitorId
	 *            ??????ID
	 * @param updateFlg
	 *            ????????????????????????true:?????????false:???????????????
	 * @wbp.parser.constructor
	 */
	public CreateBillingDetailMonitorDialog(Shell parent, String managerName, String monitorId, boolean updateFlg) {
		super(parent, managerName);

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
		item1 = Messages.getString("select.value");
		item2 = Messages.getString("select.value");

		super.customizeDialog(parent);

		// ????????????
		shell.setText(strBillingDetailMonitorDialog);

		// ???????????????????????????????????????
		Label label = null;
		// ???????????????????????????????????????????????????
		GridData gridData = null;

		/*
		 * ????????????????????????????????????????????????????????????????????????
		 */
		Group groupCheckRule = new Group(groupRule, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 15;
		groupCheckRule.setLayout(layout);
		groupCheckRule.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 40, 1));
		groupCheckRule.setText(Messages.getString("check.rule"));

		/*
		 * ???????????????
		 */
		// ?????????
		label = new Label(groupCheckRule, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = WIDTH_TITLE;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText(strTarget + strSeparator);
		// ?????????????????????
		this.cmbTarget = new Combo(groupCheckRule, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.cmbTarget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 10, 1));
		ControlUtil.setRequired(this.cmbTarget);
		
		//??????????????????[?????????????????????]
		this.cmbTarget.add(createType(type_scope,MonitorKind.sum.name()));
		this.cmbTarget.setData(createType(type_scope,MonitorKind.sum.name()), new String[]{type_scope,MonitorKind.sum.name()});
		//??????????????????[???????????????????????????]
		this.cmbTarget.add(createType(type_scope,MonitorKind.delta.name()));
		this.cmbTarget.setData(createType(type_scope,MonitorKind.delta.name()), new String[]{type_scope,MonitorKind.delta.name().toString()});
		//????????????[?????????????????????]
		this.cmbTarget.add(createType(type_node,MonitorKind.sum.name()));
		this.cmbTarget.setData(createType(type_node,MonitorKind.sum.name()), new String[]{type_node,MonitorKind.sum.name().toString()});
		//????????????[???????????????????????????]
		this.cmbTarget.add(createType(type_node,MonitorKind.delta.name()));
		this.cmbTarget.setData(createType(type_node,MonitorKind.delta.name()), new String[]{type_node,MonitorKind.delta.name().toString()});
		
		// ????????????????????????????????????????????????
		this.itemName.setText(strFee);

		// ?????????????????????????????????????????????
//		this.measure.setText(Messages.getString("time.msec"));

		// ????????????????????????
		this.adjustDialog();

		// ????????????
		MonitorInfo info = null;
		if(this.monitorId == null){
			// ???????????????
			info = new MonitorInfo();
			this.setInfoInitialValue(info);
		} else {
			// ??????????????????????????????
			try {
				info = MonitorSettingEndpointWrapper.getWrapper(getManagerName()).getMonitor(this.monitorId);
			} catch (InvalidRole_Exception e) {
				// ????????????????????????????????????????????????????????????????????????
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));

			} catch (Exception e) {
				// ?????????????????????
				logger.warn("customizeDialog(), " + e.getMessage(), e);
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage());

			}
		}
		
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = CommonMonitorDialog.class.getDeclaredField("m_monitorBasic");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_monitorBasic_ = (MonitorBasicScopeComposite)field.get(this);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		RoleIdListComposite m_ownerRoleId;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = com.clustercontrol.monitor.run.composite.MonitorBasicComposite.class.getDeclaredField("m_ownerRoleId");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_ownerRoleId = (RoleIdListComposite)field.get(m_monitorBasic_);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		Combo comboRoleId;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = com.clustercontrol.composite.RoleIdListComposite.class.getDeclaredField("comboRoleId");
				accesible = field.isAccessible();
				field.setAccessible(true);
				comboRoleId = (Combo)field.get(m_ownerRoleId);
				
				if (comboRoleId != null) {
					comboRoleId.addModifyListener(new ModifyListener() {
						@Override
						public void modifyText(ModifyEvent e) {
							//cmbTarget.removeAll();
						}
					});
				}
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		final Text m_textScope;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = MonitorBasicScopeComposite.class.getDeclaredField("m_textScope");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_textScope = (Text)field.get(m_monitorBasic_);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}

		Button m_buttonScope = null;
		{
			Field field = null;
			Boolean accesible = null;
			try {
				field = com.clustercontrol.monitor.run.composite.MonitorBasicScopeComposite.class.getDeclaredField("m_buttonScope");
				accesible = field.isAccessible();
				field.setAccessible(true);
				m_buttonScope = (Button)field.get(m_monitorBasic_);
			} catch(Exception e) {
				throw new IllegalStateException(e);
			} finally {
				if (accesible != null) {
					field.setAccessible(accesible);
				}
			}
		}
		
		for (Listener listener: m_buttonScope.getListeners(SWT.Selection)) {
			m_buttonScope.removeListener(SWT.Selection, listener);
			m_buttonScope.removeListener(SWT.DefaultSelection,listener);	
		}
		
		m_buttonScope.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ScopeTreeDialog dialog = new ScopeTreeDialog(
						CreateBillingDetailMonitorDialog.this.getShell(), 
						m_monitorBasic_.getManagerListComposite().getText(), 
						m_monitorBasic_.getOwnerRoleId(), false, false);

				if (dialog.open() == IDialogConstants.OK_ID) {
					FacilityTreeItem item = dialog.getSelectItem();
					FacilityInfo info = item.getData();
					m_textScope.setData(info.getFacilityId());
					
					Field field = null;
					Boolean accesible = null;
					try {
						field = MonitorBasicScopeComposite.class.getDeclaredField("m_facilityId");
						accesible = field.isAccessible();
						field.setAccessible(true);
						field.set(m_monitorBasic_, info.getFacilityId());
					} catch(Exception ex) {
						throw new IllegalStateException(ex);
					} finally {
						if (accesible != null) {
							field.setAccessible(accesible);
						}
					}
					
					if (info.getFacilityType() == FacilityConstant.TYPE_NODE) {
						m_textScope.setText(info.getFacilityName());
						m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
					}
					else {
						FacilityPath path = new FacilityPath(ClusterControlPlugin.getDefault().getSeparator());
						m_textScope.setText(path.getPath(item));
						m_textScope.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
					}
				}
			}
		});
		
		if (info != null)
			this.setInputData(info);
	}


	/**
	 * ????????????
	 * 
	 */
	@Override
	public void update(){
		super.update();

//		// ?????????????????????
//		if(this.m_textTimeout.getEnabled() && "".equals(this.m_textTimeout.getText())){
//			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
//		}else{
//			this.m_textTimeout.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
//		}
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
		
		// ??????????????????????????????????????????
		PluginCheckInfo info = monitor.getPluginCheckInfo();
		if (info != null && info.getMonitorPluginStringInfoList() != null && !info.getMonitorPluginStringInfoList().isEmpty()) {
			String facilityType = null;
			String monitorKind = null;
			for (MonitorPluginStringInfo stringInfo : info.getMonitorPluginStringInfoList()) {
				switch(stringInfo.getKey()){
					case key_facilityType:
						facilityType = stringInfo.getValue();
						break;
					case key_monitorKind:
						monitorKind = stringInfo.getValue();
						break;
					default:
						break;
				}
			}
			info.getMonitorPluginNumericInfoList();
			int selectService = cmbTarget.indexOf(createType(facilityType,monitorKind));
			if(selectService != -1){
				this.cmbTarget.select(selectService);
			}
		}
		
		m_numericValueInfo.setInputData(monitor);
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

		if(cmbTarget.getText() == null || cmbTarget.getText().isEmpty()){
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					msgSelectTarget);
			return null;
		}
		
		// ?????????????????????????????????????????????
		monitorInfo.setMonitorTypeId(PlatformServiceBillingDetailMonitorPlugin.monitorPluginId);

		// ???????????? ??????????????????????????????
		PluginCheckInfo pluginInfo = new PluginCheckInfo();
		pluginInfo.setMonitorId(monitorInfo.getMonitorId());

		pluginInfo.setMonitorTypeId(PlatformServiceBillingDetailMonitorPlugin.monitorPluginId);

		String[] objects = (String[]) cmbTarget.getData(cmbTarget.getText());
		
		MonitorPluginStringInfo facilityTypeInfo = new MonitorPluginStringInfo();
		facilityTypeInfo.setMonitorId(monitorInfo.getMonitorId());
		facilityTypeInfo.setKey(key_facilityType);
		facilityTypeInfo.setValue(objects[0]);
		pluginInfo.getMonitorPluginStringInfoList().add(facilityTypeInfo);
		
		MonitorPluginStringInfo monitorKindInfo = new MonitorPluginStringInfo();
		monitorKindInfo.setMonitorId(monitorInfo.getMonitorId());
		monitorKindInfo.setKey(key_monitorKind);
		monitorKindInfo.setValue(objects[1]);
		pluginInfo.getMonitorPluginStringInfoList().add(monitorKindInfo);
		
		monitorInfo.setPluginCheckInfo(pluginInfo);

		// ?????????????????????
		validateResult = m_numericValueInfo.createInputData(monitorInfo);
		if(validateResult != null){
			return null;
		}

		// ??????????????????????????????????????????????????????
		// ??????????????????ID?????????
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
		
		managerName = m_monitorBasic_.getManagerListComposite().getText();
		MonitorInfo info = this.inputData;
		if(info != null){
			String[] args = { info.getMonitorId(), managerName };
			if(!this.updateFlg){
				// ???????????????
				try {
					result = MonitorSettingEndpointWrapper.getWrapper(getManagerName()).addMonitor(info);

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
						errMessage = ", " + e.getMessage();
					}

					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.34", args) + errMessage);

				}

			} else{
				// ???????????????
				String errMessage = "";
				try {
					result = MonitorSettingEndpointWrapper.getWrapper(getManagerName()).modifyMonitor(info);
				} catch (InvalidRole_Exception e) {
					// ????????????????????????????????????????????????????????????????????????
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					errMessage = ", " + e.getMessage();
				}

				if(result){
					MessageDialog.openInformation(
							null,
							Messages.getString("successful"),
							Messages.getString("message.monitor.35", args));
				} else{
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.monitor.36", args) + errMessage);
				}
			}
		}

		return result;
	}

	/**
	 * MonitorInfo??????????????????????????????
	 * 
	 * @see com.clustercontrol.dialog.CommonMonitorDialog#setInfoInitialValue()
	 */
	@Override
	protected void setInfoInitialValue(MonitorInfo monitor) {
		super.setInfoInitialValue(monitor);
	}
	
	private static String createType(String facilityType, String monitorKind){
		String strType;
		if (FacilityConstant.TYPE_SCOPE_STRING.equals(facilityType)) {
			strType = CloudStringConstants.strDetailTypeScope;
		} else /*if (FacilityConstant.TYPE_NODE_STRING.equals(facilityType))*/ {
			strType = CloudStringConstants.strDetailTypeNode;
		}
		
		String strKind;
		if (MonitorKind.sum.name().equals(monitorKind)) {
			strKind = CloudStringConstants.strDetailKindSum;
		} else /*if (MonitorKind.delta.name().equals(facilityType))*/ {
			strKind = CloudStringConstants.strDetailKindDelta;
		}
		return String.format("%s [%s]", strType, strKind);
	}
	
	public static void main (String[] args) {
		System.out.println(createType(null,null));
	}
}
