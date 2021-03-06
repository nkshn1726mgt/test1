/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.infra.bean.SendMethodConstant;
import com.clustercontrol.infra.composite.FileReplaceSettingComposite;
import com.clustercontrol.infra.util.InfraEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.infra.FileTransferModuleInfo;
import com.clustercontrol.ws.infra.HinemosUnknown_Exception;
import com.clustercontrol.ws.infra.InfraFileInfo;
import com.clustercontrol.ws.infra.InfraManagementDuplicate_Exception;
import com.clustercontrol.ws.infra.InfraManagementInfo;
import com.clustercontrol.ws.infra.InfraManagementNotFound_Exception;
import com.clustercontrol.ws.infra.InfraModuleInfo;
import com.clustercontrol.ws.infra.InvalidRole_Exception;
import com.clustercontrol.ws.infra.InvalidSetting_Exception;
import com.clustercontrol.ws.infra.InvalidUserPass_Exception;
import com.clustercontrol.ws.infra.NotifyDuplicate_Exception;
import com.clustercontrol.ws.infra.NotifyNotFound_Exception;

/**
 * ????????????[???????????????????????????????????????????????????]?????????????????????????????????
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class FileTransferModuleDialog extends CommonDialog {
	// ??????
	private static Log m_log = LogFactory.getLog( FileTransferModuleDialog.class );

	// CONSTANT
	private static String DEFAULT_SCP_OWNER = Messages.getString("infra.module.transfer.default.owner");
	private static String DEFAULT_SCP_ATTRIBUTE = Messages.getString("infra.module.transfer.default.file.attibute");

	/** ????????????[??????]??????*/
	private InfraManagementInfo infraInfo ;

	private FileTransferModuleInfo moduleInfo;

	/**
	 * ???????????????????????????????????????????????????8ijm
	 * ???????????????????????????????????????????????????????????????????????????????????????????????????
	 * ?????????????????????????????????????????????????????????????????????
	 */
	private final int DIALOG_WIDTH = 12;
	/*
	 * ??????????????????
	 */
	/** ?????????????????? */
	private String m_managerName = null;
	/** ????????????ID???????????? */
	private String m_managementId = null;
	/** ???????????????ID???????????? */
	private String m_strModuleId = null;
	/** ???????????????ID??????????????? */
	private Text m_moduleId = null;
	/** ????????????????????????????????? */
	private Text m_moduleName = null;
	/** ??????????????????ID????????????????????? */
	private Combo m_comboFileId = null;
	/** ??????????????????????????? */
	private Text m_placementPath = null;
	/** SCP??????????????????????????????????????? */
	private Button m_scp = null;
	/** WinRM???????????????????????????????????????*/
	private Button m_winRm = null;

	/** ????????????????????????*/
	private Button m_valid = null;

	/** SCP??????????????????????????????*/
	private Text m_scpOwner = null;
	/** SCP????????????????????????????????????*/
	private Text m_scpFileAttribute = null;
	/** ?????????????????????*/
	private Label dumyLabel = null;

	/** ???????????????????????????????????????????????????????????? */
	private Button m_rename = null;
	/** ????????????????????????*/
	private Button m_check = null;
	/** ???????????????????????????????????? */
	private Text m_execReturnParamName = null;

	private FileReplaceSettingComposite m_chenge;
	/** ????????? */
	private Shell m_shell = null;

	/**
	 * ?????????MODE_ADD = 0;
	 * ?????????MODE_MODIFY = 1;
	 * ?????????MODE_COPY = 3;
	 * */
	private int mode;

	private Button m_md5Check = null;


	public FileTransferModuleDialog(Shell parent, String managerName, String managementId) {
		super(parent);
		this.mode = PropertyDefineConstant.MODE_ADD;
		this.m_managerName = managerName;
		this.m_managementId = managementId;
	}

	public FileTransferModuleDialog(Shell parent, String managerName, String managementId, String moduleId, int mode) {
		super(parent);
		this.mode = mode;
		this.m_managerName = managerName;
		this.m_managementId = managementId;
		this.m_strModuleId = moduleId;
	}

	/**
	 * ?????????????????????????????????????????????
	 *
	 * @param parent ?????????????????????
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();
		parent.getShell().setText(
				Messages.getString("dialog.infra.module.transfer"));
		/**
		 * ?????????????????????
		 * ????????????????????????????????????????????????????????????????????????
		 */
		GridLayout baseLayout = new GridLayout(1, true);
		baseLayout.marginWidth = 10;
		baseLayout.marginHeight = 10;
		baseLayout.numColumns = DIALOG_WIDTH;
		//????????????????????????
		parent.setLayout(baseLayout);

		GridData gridData= null;
		/*
		 * ???????????????ID
		 */
		Composite fileCheckComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 12;
		fileCheckComposite.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileCheckComposite.setLayoutData(gridData);
		//?????????
		Label labelModuleId = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelModuleId.setText(Messages.getString("infra.module.id") + " : ");
		labelModuleId.setLayoutData(gridData);

		//????????????
		m_moduleId = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_moduleId.setLayoutData(gridData);
		this.m_moduleId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * ??????????????????
		 */
		//?????????
		Label labelModuleName = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelModuleName.setText(Messages.getString("infra.module.name") + " : ");
		labelModuleName.setLayoutData(gridData);
		//????????????
		this.m_moduleName = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_moduleName.setLayoutData(gridData);
		this.m_moduleName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * ??????????????????ID
		 */
		//?????????
		Label labelFileId = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelFileId.setText(Messages.getString("infra.module.placement.file") + " : ");
		labelFileId.setLayoutData(gridData);

		//?????????????????????
		// ??????????????????????????????????????????
		this.m_comboFileId = new Combo(fileCheckComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboFileId.setLayoutData(gridData);
		this.m_comboFileId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		/*
		 * ????????????
		 */
		//?????????
		Label labelPlacementPath = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelPlacementPath.setText(Messages.getString("infra.module.placement.path") + " : ");
		labelPlacementPath.setLayoutData(gridData);
		//????????????
		this.m_placementPath = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 6;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_placementPath.setLayoutData(gridData);
		this.m_placementPath.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ??????
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);


		/*
		 * ???????????????????????????
		 */
		Group fileDistributeMethod = new Group(fileCheckComposite, SWT.NONE);
		fileDistributeMethod.setText(Messages.getString("infra.module.transfer.method"));
		fileDistributeMethod.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		fileDistributeMethod.setLayoutData(gridData);


		//SCP????????????????????????????????????
		m_scp = new Button(fileDistributeMethod, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_scp.setText(Messages.getString("infra.module.transfer.method.scp"));
		m_scp.setLayoutData(gridData);
		m_scp.setSelection(true);
		m_scp.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});


		//?????????
		Label labelSelectMethod = new Label(fileDistributeMethod, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		labelSelectMethod.setText(Messages.getString("infra.module.transfer.method.owner") + " : ");
		labelSelectMethod.setLayoutData(gridData);
		//????????????
		this.m_scpOwner = new Text(fileDistributeMethod, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		this.m_scpOwner.setLayoutData(gridData);
		this.m_scpOwner.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		//?????????
		Label labelSelectMetho = new Label(fileDistributeMethod, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		labelSelectMetho.setText(Messages.getString("infra.module.transfer.method.scp.file.attribute") + " : ");
		labelSelectMetho.setLayoutData(gridData);

		//????????????
		m_scpFileAttribute = new Text(fileDistributeMethod, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 20;
		m_scpFileAttribute.setLayoutData(gridData);
		this.m_scpFileAttribute.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});


		//WinRM????????????????????????????????????
		m_winRm = new Button(fileDistributeMethod, SWT.RADIO);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_winRm.setText(Messages.getString("infra.module.transfer.method.winrm"));
		m_winRm.setLayoutData(gridData);
		m_winRm.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// ??????
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);
		//??????
		m_check = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_check.setText(Messages.getString("infra.module.inexec.after.transfer.error"));
		m_check.setLayoutData(gridData);


		//????????????
		m_rename = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_rename.setText(Messages.getString("infra.module.same.file.maintain"));
		m_rename.setLayoutData(gridData);


		//MD5????????????
		m_md5Check = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_md5Check.setText(Messages.getString("infra.module.md5.check"));
		m_md5Check.setLayoutData(gridData);
		m_md5Check.setSelection(true);

		/*
		 * ???????????????
		 */
		Label label = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setText(Messages.getString("infra.module.exec.return.param.name") + " : ");
		label.setLayoutData(gridData);
		m_execReturnParamName = new Text(fileCheckComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalIndent = 25;
		m_execReturnParamName.setLayoutData(gridData);
		m_execReturnParamName.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ??????
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);
		/*
		 * ?????????????????????????????????
		 */

		//?????????
		Label labelCommandCheck = new Label(fileCheckComposite, SWT.LEFT);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelCommandCheck.setText(Messages.getString("infra.module.transfer.chenge.variable") + " : ");
		labelCommandCheck.setLayoutData(gridData);

		//?????????????????????????????????
		m_chenge = new FileReplaceSettingComposite(fileCheckComposite, SWT.NONE);
		m_chenge.setLayout(layout);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		m_chenge.setLayoutData(gridData);

		// ??????
		dumyLabel = new Label(fileCheckComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dumyLabel.setLayoutData(gridData);

		// ??????????????????
		Label line = new Label(fileCheckComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 12;
		line.setLayoutData(gridData);

		//???????????????
		m_valid = new Button(fileCheckComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 12;
		gridData.horizontalAlignment = SWT.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		m_valid.setText(Messages.getString("setting.valid.confirmed"));
		m_valid.setLayoutData(gridData);



		// ????????????????????????
		this.adjustDialog();
		//??????????????????????????????
		setInputData();
		update();
	}

	/**
	 * ?????????????????????????????????????????????
	 *
	 */
	private void adjustDialog(){
		// ?????????????????????
		// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
		m_shell.pack();
		m_shell.setSize(new Point(550, m_shell.getSize().y));

		// ?????????????????????
		Display display = m_shell.getDisplay();
		m_shell.setLocation((display.getBounds().width - m_shell.getSize().x) / 2,
				(display.getBounds().height - m_shell.getSize().y) / 2);
	}
	/**
	 * ????????????
	 *
	 */
	public void update(){
		m_scpOwner.setEnabled(m_scp.getSelection());
		m_scpFileAttribute.setEnabled(m_scp.getSelection());

		/*
		 *  ?????????????????????
		 */
		//???????????????ID
		if("".equals(this.m_moduleId.getText())){
			this.m_moduleId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_moduleId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//??????????????????
		if("".equals(this.m_moduleName.getText())){
			this.m_moduleName.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_moduleName.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//??????????????????
		if("".equals(this.m_comboFileId.getText())){
			this.m_comboFileId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_comboFileId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//????????????
		if("".equals(this.m_placementPath.getText())){
			this.m_placementPath.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_placementPath.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//????????????
		if(m_scpOwner.isEnabled() && "".equals(this.m_scpOwner.getText())){
			this.m_scpOwner.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scpOwner.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//??????????????????
		if(m_scpFileAttribute.isEnabled() && "".equals(this.m_scpFileAttribute.getText())){
			this.m_scpFileAttribute.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_scpFileAttribute.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @return ??????????????????????????????
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("register");
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @return ???????????????????????????????????????
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}


	/**
	 * ????????????????????????????????????????????????????????????
	 *
	 * @see com.clustercontrol.infra.bean.InfraManagementInfo
	 */
	private void setInputData() {
		InfraManagementInfo info = null;
		FileTransferModuleInfo module = null;
		try {
			InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.m_managerName);
			info = wrapper.getInfraManagement(m_managementId);
		} catch (InfraManagementNotFound_Exception | HinemosUnknown_Exception | InvalidRole_Exception | InvalidUserPass_Exception | NotifyNotFound_Exception e) {
			m_log.error(m_managementId + " InfraManagerInfo is null");
			return;
		}

		//????????????????????????????????????
		List<InfraFileInfo> infraFileInfoList = null;
		InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(m_managerName);
		try {
			infraFileInfoList = wrapper.getInfraFileListByOwnerRoleId(info.getOwnerRoleId());
		} catch (Exception e) {
			m_log.warn("setInputData() getInfraFileList, " + e.getMessage());
		}
		if (infraFileInfoList != null) {
			for (InfraFileInfo infraFileInfo : infraFileInfoList) {
				m_comboFileId.add(infraFileInfo.getFileId());
			}
		}
		
		if (mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY) {
			// ??????????????????????????????????????????????????????????????????
			moduleInfo = new FileTransferModuleInfo();
			List<InfraModuleInfo> modules = info.getModuleList();
			modules.add(moduleInfo);
		} else if (mode == PropertyDefineConstant.MODE_MODIFY){
			// ???????????????????????????????????????
			for(InfraModuleInfo tmpModule: info.getModuleList()){
				if(tmpModule.getModuleId().equals(m_strModuleId)){
					moduleInfo = (FileTransferModuleInfo) tmpModule;
				}
			}
		}
		
		// ??????????????????????????????????????????
		if (m_strModuleId != null && info != null) {
			for (InfraModuleInfo tmpModule : info.getModuleList()) {
				if (tmpModule.getModuleId().equals(m_strModuleId)) {
					module = (FileTransferModuleInfo) tmpModule;
					break;
				}
			}
			if (module == null) {
				m_log.error("setInputData() module does not find, " + m_strModuleId);
				return;
			}
			//	???????????????ID
			m_moduleId.setText(module.getModuleId());
			if (mode == PropertyDefineConstant.MODE_MODIFY) {
				m_moduleId.setEnabled(false);
			}
			//	??????????????????
			m_moduleName.setText(module.getName());

			m_comboFileId.setText(module.getFileId());

			m_placementPath.setText(module.getDestPath());

			//	????????????
			if (module.getSendMethodType() == SendMethodConstant.TYPE_WINRM) {
				m_scp.setSelection(false);
				m_winRm.setSelection(true);
			}
			else {
				m_scp.setSelection(true);
				m_winRm.setSelection(false);
				m_scpOwner.setText(module.getDestOwner());
				m_scpFileAttribute.setText(module.getDestAttribute());
			}

			//	??????
			m_check.setSelection(module.isStopIfFailFlg());
			//	????????????
			m_rename.setSelection(module.isBackupIfExistFlg());
			// MD5
			m_md5Check.setSelection(module.isPrecheckFlg());
			// ?????????????????????
			if (module.getExecReturnParamName() != null) {
				m_execReturnParamName.setText(module.getExecReturnParamName());
			}
			//	????????????????????????
			m_valid.setSelection(module.isValidFlg());

			m_chenge.setInputData(module.getFileTransferVariableList());

		} else {
			// ???????????????(default??????)
			m_valid.setSelection(true);
			m_scp.setSelection(true);
			m_scpOwner.setText(DEFAULT_SCP_OWNER);
			m_scpFileAttribute.setText(DEFAULT_SCP_ATTRIBUTE);
		}
		//	?????????????????????????????????
		infraInfo = info;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????
	 *
	 * @return ??????????????????????????????????????????
	 *
	 *
	 */
	private void createInputData() {
		//???????????????ID??????
		moduleInfo.setModuleId(m_moduleId.getText());

		//????????????????????????
		moduleInfo.setName(m_moduleName.getText());

		moduleInfo.setFileId(m_comboFileId.getText());

		moduleInfo.setDestPath(m_placementPath.getText());

		//?????????????????????
		if (m_scp.getSelection()) {
			moduleInfo.setSendMethodType(SendMethodConstant.TYPE_SCP);
			moduleInfo.setDestOwner(m_scpOwner.getText());
			moduleInfo.setDestAttribute(m_scpFileAttribute.getText());
		} else {
			moduleInfo.setSendMethodType(SendMethodConstant.TYPE_WINRM);
		}

		//????????????????????????????????????????????????????????????????????????????????????????????????
		moduleInfo.setStopIfFailFlg(m_check.getSelection());

		//????????????????????????????????????????????????????????????????????????????????????????????????
		moduleInfo.setBackupIfExistFlg(m_rename.getSelection());

		moduleInfo.setPrecheckFlg(m_md5Check.getSelection());

		// ?????????????????????
		if (m_execReturnParamName.getText() != null) {
			moduleInfo.setExecReturnParamName(m_execReturnParamName.getText());
		}
		moduleInfo.getFileTransferVariableList().clear();
		moduleInfo.getFileTransferVariableList().addAll(m_chenge.getInputData());

		//???????????????
		moduleInfo.setValidFlg(m_valid.getSelection());
	}

	@Override
	protected ValidateResult validate() {
		return super.validate();
	}

	/**
	 * ValidateResult??????????????????
	 * @id messageBox??????????????????????????????
	 * @message messageBox?????????????????????????????????
	 */
	protected ValidateResult createValidateResult(String id, String message) {
		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}

	@Override
	protected boolean action() {
		boolean result = false;
		createInputData();
		String action = null;
		if(infraInfo != null){
			if(mode == PropertyDefineConstant.MODE_ADD | mode == PropertyDefineConstant.MODE_COPY){
				// ???????????????
				action = Messages.getString("add");
			} else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// ???????????????
				action = Messages.getString("modify");
			}

			try {
				InfraEndpointWrapper wrapper = InfraEndpointWrapper.getWrapper(this.m_managerName);
				wrapper.modifyInfraManagement(infraInfo);
				action += "(" + this.m_managerName + ")";
				result = true;
				MessageDialog.openInformation(null, Messages
						.getString("successful"), Messages.getString(
						"message.infra.action.result",
						new Object[] { Messages.getString("infra.module"),
								action, Messages.getString("successful"),
								m_moduleId.getText() }));
			} catch (InfraManagementDuplicate_Exception e) {
				// ID??????
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.infra.module.duplicate", new String[]{m_moduleId.getText()}));
			} catch (InvalidRole_Exception e) {
				// ????????????
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.accesscontrol.16"));
			} catch (InfraManagementNotFound_Exception | NotifyDuplicate_Exception | NotifyNotFound_Exception | HinemosUnknown_Exception | InvalidUserPass_Exception | InvalidSetting_Exception e) {
				m_log.info("action() modifyInfraManagement : " + e.getMessage() + " (" + e.getClass().getName() + ")");
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), 
								action, Messages.getString("failed"), m_moduleId.getText() + "\n" + HinemosMessage.replace(e.getMessage())}));
			} catch (Exception e) {
				m_log.info("action() modifyInfraManagement : " + e.getMessage() + " (" + e.getClass().getName() + ")");
				MessageDialog.openError(
						null,
						Messages.getString("failed"),
						Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), 
								action, Messages.getString("failed"), m_moduleId.getText() + "\n" + HinemosMessage.replace(e.getMessage())}));
			}
		} else {
			m_log.error("inputData InfraManagerInfo is null");
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.infra.action.result", new Object[]{Messages.getString("infra.module"), action, Messages.getString("failed"), m_moduleId.getText()}));
		}
		return result;
	}
}
