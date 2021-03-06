/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.fault.JobInvalid;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.composite.ApprovalComposite;
import com.clustercontrol.jobmanagement.composite.CommandComposite;
import com.clustercontrol.jobmanagement.composite.ControlComposite;
import com.clustercontrol.jobmanagement.composite.ControlNodeComposite;
import com.clustercontrol.jobmanagement.composite.EndDelayComposite;
import com.clustercontrol.jobmanagement.composite.EndStatusComposite;
import com.clustercontrol.jobmanagement.composite.FileComposite;
import com.clustercontrol.jobmanagement.composite.JobTreeComposite;
import com.clustercontrol.jobmanagement.composite.MonitorComposite;
import com.clustercontrol.jobmanagement.composite.NotificationsComposite;
import com.clustercontrol.jobmanagement.composite.ParameterComposite;
import com.clustercontrol.jobmanagement.composite.ReferComposite;
import com.clustercontrol.jobmanagement.composite.StartDelayComposite;
import com.clustercontrol.jobmanagement.composite.WaitRuleComposite;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.util.JobPropertyUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.JobEndStatusInfo;
import com.clustercontrol.ws.jobmanagement.JobInfo;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobParameterInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;
import com.clustercontrol.ws.jobmanagement.OtherUserGetLock_Exception;

/**
 * ?????????[???????????????????????????]?????????????????????????????????
 *
 * @version 4.0.0
 * @since 1.0.0
 */
public class JobDialog extends CommonDialog {
	// ??????
	private static Log m_log = LogFactory.getLog( JobDialog.class );

	/** ?????????ID??????????????? */
	private Text m_jobIdText = null;
	/** ??????????????????????????? */
	private Text m_jobNameText = null;
	/** ?????????????????? */
	private Text m_jobAnnotationText = null;
	/** ????????????ID????????????????????? */
	private Combo m_iconIdCombo = null;
	/** ??????????????????????????????????????? */
	private WaitRuleComposite m_startComposite = null;
	/** ????????????????????????????????? */
	private ControlComposite m_controlComposite = null;
	/** ??????????????????????????????????????? */
	private EndStatusComposite m_endComposite = null;
	/** ??????????????????????????????????????? */
	private CommandComposite m_executeComposite = null;
	/** ????????????????????????????????????????????? */
	private FileComposite m_fileComposite = null;
	/** ????????????????????????????????????????????? */
	private NotificationsComposite m_messageComposite = null;
	/** ??????????????????????????????????????? */
	private StartDelayComposite m_startDelayComposite = null;
	/** ??????????????????????????????????????? */
	private EndDelayComposite m_endDelayComposite = null;
	/** ??????(?????????)????????????????????? */
	private ControlNodeComposite m_controlNodeComposite = null;
	/** ?????????????????????????????????????????? */
	private ParameterComposite m_parameterComposite = null;
	/** ????????????????????????????????? */
	private ReferComposite m_referComposite = null;
	/** ????????????????????????????????? */
	private ApprovalComposite m_approvalComposite = null;
	/** ????????????????????????????????? */
	private MonitorComposite m_monitorComposite = null;

	/** ?????????????????????????????? */
	private JobTreeItem m_jobTreeItem = null;
	/** ????????????????????? */
	private TabFolder m_tabFolder = null;
	/** ????????? */
	private Shell m_shell = null;
	/** ??????????????????????????? */
	private boolean m_readOnly = false;
	/** ?????????????????????????????? */
	private boolean m_isCallJobHistory = false;

	/** ?????????????????????ID??????????????? */
	private RoleIdListComposite m_ownerRoleId = null;

	/** ?????????????????? */
	private String m_managerName = null;

	private Button m_editButton;

	private JobTreeComposite m_jobTreeComposite = null;

	/** ?????????????????????????????????*/
	private Button m_moduleRegisteredCondition = null;
	
	/**
	 * ?????????????????????
	 *
	 * @param parent ????????????
	 * @param readOnly ??????????????????????????? true??????????????????false????????????
	 */
	public JobDialog(Shell parent, String managerName, boolean readOnly) {
		super(parent);
		this.m_managerName = managerName;
		m_readOnly = readOnly;
		this.m_jobTreeComposite = null;
	}

	public JobDialog(JobTreeComposite jobTreeComposite, Shell parent, String managerName, boolean readOnly) {
		this(parent, managerName, readOnly);
		this.m_jobTreeComposite = jobTreeComposite;
	}

	public JobDialog(Shell parent, String managerName, boolean readOnly, boolean isCallJobHistory) {
		this(parent, managerName, readOnly);
		this.m_isCallJobHistory = isCallJobHistory;
	}
	
	/**
	 * ?????????????????????????????????????????????
	 * <P>
	 * ?????????????????????????????????????????????????????????????????????
	 *
	 * @param parent ?????????????????????
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 * @see com.clustercontrol.bean.JobConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		m_shell = this.getShell();

		Label label = null;

		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		JobInfo info = m_jobTreeItem.getData();
		if (info == null)
			throw new InternalError("info is null.");
		
		// ??????????????????????????????????????????????????????setJobFull??????
		JobPropertyUtil.setJobFull(m_managerName, info);

		if (info.getType() == JobConstant.TYPE_JOBUNIT) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.jobunit"));
		} else if (info.getType() == JobConstant.TYPE_JOBNET) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.jobnet"));
		} else if (info.getType() == JobConstant.TYPE_JOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.job"));
		} else if (info.getType() == JobConstant.TYPE_FILEJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.forward.file.job"));
		} else if (info.getType() == JobConstant.TYPE_REFERJOB || info.getType() == JobConstant.TYPE_REFERJOBNET){
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.refer.job"));
		} else if (info.getType() == JobConstant.TYPE_APPROVALJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.approval.job"));
		} else if (info.getType() == JobConstant.TYPE_MONITORJOB) {
			parent.getShell().setText(
					Messages.getString("dialog.job.create.modify.monitor.job"));
		}
		
		boolean initFlag = true;
		if (info.getId() != null && info.getId().length() > 0) {
			initFlag = false;
		}

		// Composite
		Composite jobInfoComposite = new Composite(parent, SWT.NONE);
		GridLayout jobInfoGridLayout = new GridLayout(4, false);
		jobInfoComposite.setLayout(jobInfoGridLayout);

		// ?????????ID???????????????
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("job.id") + " : ");
		label
			.setLayoutData(new GridData(120, SizeConstant.SIZE_LABEL_HEIGHT));
		
		// ?????????ID??????????????????
		this.m_jobIdText = new Text(jobInfoComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobIdText", m_jobIdText);
		this.m_jobIdText.setLayoutData(new GridData(200,
				SizeConstant.SIZE_TEXT_HEIGHT));
		
		if(m_isCallJobHistory){
			this.m_jobIdText.addVerifyListener(
					new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		}else{
			this.m_jobIdText.addVerifyListener(
					new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		}
		
		this.m_jobIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ?????????????????????
		this.m_editButton = new Button(jobInfoComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_editButton", m_editButton);
		m_editButton.setText(Messages.getString("edit"));
		m_editButton.setEnabled(false);
		this.m_editButton.setLayoutData(new GridData(40,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_editButton.getLayoutData()).horizontalSpan = 2;
		m_editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				super.widgetSelected(event);

				JobTreeItem jobunitItem = JobUtil.getTopJobUnitTreeItem(m_jobTreeItem);
				String jobunitId = jobunitItem.getData().getJobunitId();

				JobEditState JobEditState = JobEditStateUtil.getJobEditState( m_managerName );
				// ????????????????????????
				Long updateTime = JobEditState.getJobunitUpdateTime(jobunitId);
				Integer result = null;
				try {
					result =JobUtil.getEditLock(m_managerName, jobunitId, updateTime, false);
				} catch (OtherUserGetLock_Exception e) {
					// ????????????????????????????????????????????????
					String message = HinemosMessage.replace(e.getMessage());
					if (MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							message)) {
						try {
							result = JobUtil.getEditLock(m_managerName, jobunitId, updateTime, true);
						} catch (Exception e1) {
							// ????????????????????????????????????
							m_log.error("run() : logical error");
						}
					}
				}

				if (result != null) {
					// ????????????????????????
					m_log.debug("run() : get editLock(jobunitId="+jobunitId+")");
					JobEditState.addLockedJobunit(jobunitItem.getData(), JobTreeItemUtil.clone(jobunitItem, null), result);
					if (m_jobTreeComposite != null) {
						m_jobTreeComposite.refresh(jobunitItem.getParent());
					}

					//????????????????????????
					m_readOnly = false;
					updateWidgets();
				} else {
					// ?????????????????????????????????
					m_log.debug("run() : cannot get editLock(jobunitId="+jobunitId+")");
				}
			}
		});

		// ???????????????????????????
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("job.name") + " : ");
		label.setLayoutData(new GridData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// ??????????????????????????????
		this.m_jobNameText = new Text(jobInfoComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobNameText", m_jobNameText);
		this.m_jobNameText.setLayoutData(new GridData(200,
				SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_jobNameText.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_jobNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// ?????????????????????????????????
		this.m_moduleRegisteredCondition = new Button(jobInfoComposite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_moduleRegistCondition", this.m_moduleRegisteredCondition);
		this.m_moduleRegisteredCondition.setText(Messages.getString("job.module.registration"));
		this.m_moduleRegisteredCondition.setLayoutData(new GridData(150,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		((GridData)this.m_moduleRegisteredCondition.getLayoutData()).horizontalSpan = 2;
		this.m_moduleRegisteredCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		if (info.getType() == JobConstant.TYPE_JOBNET ||
				info.getType() == JobConstant.TYPE_APPROVALJOB ||
				info.getType() == JobConstant.TYPE_JOB ||
				info.getType() == JobConstant.TYPE_FILEJOB ||
				info.getType() == JobConstant.TYPE_MONITORJOB) {
			m_moduleRegisteredCondition.setEnabled(!m_readOnly);
		} else {
			m_moduleRegisteredCondition.setEnabled(false);
		}

		// ?????????????????????
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("description") + " : ");
		label.setLayoutData(new GridData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// ????????????????????????
		m_jobAnnotationText = new Text(jobInfoComposite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_jobAnnotationText", m_jobAnnotationText);
		m_jobAnnotationText.setLayoutData(new GridData(200,
				SizeConstant.SIZE_TEXT_HEIGHT));
		m_jobAnnotationText.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		// dummy
		new Label(jobInfoComposite, SWT.NONE);
		// dummy
		new Label(jobInfoComposite, SWT.NONE);

		// ?????????????????????ID???????????????
		label = new Label(jobInfoComposite, SWT.NONE);
		label.setText(Messages.getString("owner.role.id") + " : ");
		label.setLayoutData(new GridData(120,
				SizeConstant.SIZE_LABEL_HEIGHT));
		
		// ?????????????????????ID??????????????????
		// ?????????????????????????????????????????????
		// ????????????????????????????????????JobInfo.createTime????????????
		if (info.getType() == JobConstant.TYPE_JOBUNIT && info.getCreateTime() == null) {
			this.m_ownerRoleId = new RoleIdListComposite(jobInfoComposite,
					SWT.NONE, this.m_managerName, true, Mode.OWNER_ROLE);
			this.m_ownerRoleId.getComboRoleId().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					m_messageComposite.getNotifyId().setOwnerRoleId(m_ownerRoleId.getText(), false);
				}
			});
		} else {
			this.m_ownerRoleId = new RoleIdListComposite(jobInfoComposite,
					SWT.NONE, this.m_managerName, false, Mode.OWNER_ROLE);
		}
		GridData ownerRoleIdGridData = new GridData();
		ownerRoleIdGridData.widthHint = 207;
		this.m_ownerRoleId.setLayoutData(ownerRoleIdGridData);


		// ????????????ID
		if (info.getType() != JobConstant.TYPE_JOBUNIT) {

			// ????????????ID???????????????
			label = new Label(jobInfoComposite, SWT.NONE);
			label.setText(Messages.getString("icon.id") + " : ");
			label.setLayoutData(new GridData(70,
					SizeConstant.SIZE_LABEL_HEIGHT));
			
			// ????????????ID???????????????????????????
			m_iconIdCombo = new Combo(jobInfoComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			WidgetTestUtil.setTestId(this, "m_iconIdCombo", m_iconIdCombo);
			m_iconIdCombo.setLayoutData(new GridData(120,
					SizeConstant.SIZE_COMBO_HEIGHT));
		} else {
			// dummy
			new Label(jobInfoComposite, SWT.NONE);
			// dummy
			new Label(jobInfoComposite, SWT.NONE);
		}

		// ??????
		m_tabFolder = new TabFolder(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_tabFolder);

		if (info.getType() == JobConstant.TYPE_JOBNET) {
			//????????????
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//??????
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//????????????
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem3", tabItem3);
			tabItem3.setText(Messages.getString("start.delay"));
			tabItem3.setControl(m_startDelayComposite);

			//????????????
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem4", tabItem4);
			tabItem4.setText(Messages.getString("end.delay"));
			tabItem4.setControl(m_endDelayComposite);
		}
		else if (info.getType() == JobConstant.TYPE_JOB) {
			//????????????
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//??????(?????????)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//??????(?????????)
			m_controlNodeComposite = new ControlNodeComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlNodeComposite", m_controlNodeComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem3", tabItem3);
			tabItem3.setText(Messages.getString("control.node"));
			tabItem3.setControl(m_controlNodeComposite);

			//????????????
			m_executeComposite = new CommandComposite(m_tabFolder, SWT.NONE);
			m_executeComposite.setManagerName(m_managerName);
			WidgetTestUtil.setTestId(this, "m_executeComposite", m_executeComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem4", tabItem4);
			tabItem4.setText(Messages.getString("command"));
			tabItem4.setControl(m_executeComposite);

			//????????????
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem5", tabItem5);
			tabItem5.setText(Messages.getString("start.delay"));
			tabItem5.setControl(m_startDelayComposite);

			//????????????
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem6", tabItem6);
			tabItem6.setText(Messages.getString("end.delay"));
			tabItem6.setControl(m_endDelayComposite);

		}
		else if (info.getType() == JobConstant.TYPE_FILEJOB) {
			//????????????
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//?????????????????????
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);
			
			//??????(?????????)
			m_controlNodeComposite = new ControlNodeComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlNodeComposite", m_controlNodeComposite);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem6.setText(Messages.getString("control.node"));
			tabItem6.setControl(m_controlNodeComposite);

			//??????????????????
			m_fileComposite = new FileComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_fileComposite", m_fileComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem3.setText(Messages.getString("forward.file"));
			tabItem3.setControl(m_fileComposite);

			//????????????
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem4.setText(Messages.getString("start.delay"));
			tabItem4.setControl(m_startDelayComposite);

			//????????????
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, true);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem5.setText(Messages.getString("end.delay"));
			tabItem5.setControl(m_endDelayComposite);
		}
		//???????????????/????????????????????????
		else if(info.getType() == JobConstant.TYPE_REFERJOB || info.getType() == JobConstant.TYPE_REFERJOBNET){
			//????????????
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//??????
			m_referComposite = new ReferComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_referComposite", m_referComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabyitem2", tabItem2);
			tabItem2.setText(Messages.getString("refer"));
			tabItem2.setControl(m_referComposite);
		}
		else if (info.getType() == JobConstant.TYPE_APPROVALJOB) {
			//????????????
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//??????(?????????)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem2", tabItem2);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//??????
			m_approvalComposite = new ApprovalComposite(m_tabFolder, SWT.NONE, m_managerName);
			WidgetTestUtil.setTestId(this, "m_approvalComposite", m_approvalComposite);
			TabItem tabItem3 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem3", tabItem3);
			tabItem3.setText(Messages.getString("approval"));
			tabItem3.setControl(m_approvalComposite);

			//????????????
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem4", tabItem4);
			tabItem4.setText(Messages.getString("start.delay"));
			tabItem4.setControl(m_startDelayComposite);

			//????????????
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabItem5", tabItem5);
			tabItem5.setText(Messages.getString("end.delay"));
			tabItem5.setControl(m_endDelayComposite);
			
		}
		else if (info.getType() == JobConstant.TYPE_MONITORJOB) {
			//????????????
			m_startComposite = new WaitRuleComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_startComposite", m_startComposite);
			TabItem tabItem1 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem1", tabItem1);
			tabItem1.setText(Messages.getString("wait.rule"));
			tabItem1.setControl(m_startComposite);

			//??????(?????????)
			m_controlComposite = new ControlComposite(m_tabFolder, SWT.NONE, info.getType());
			WidgetTestUtil.setTestId(this, "m_controlComposite", m_controlComposite);
			TabItem tabItem2 = new TabItem(m_tabFolder, SWT.NONE);
			tabItem2.setText(Messages.getString("control.job"));
			tabItem2.setControl(m_controlComposite);

			//??????
			m_monitorComposite = new MonitorComposite(m_tabFolder, SWT.NONE);
			m_monitorComposite.setManagerName(m_managerName);
			WidgetTestUtil.setTestId(this, "m_monitorComposite", m_monitorComposite);
			TabItem tabItem4 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem4", tabItem4);
			tabItem4.setText(Messages.getString("monitor"));
			tabItem4.setControl(m_monitorComposite);

			//????????????
			m_startDelayComposite = new StartDelayComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_startDelayComposite", m_startDelayComposite);
			TabItem tabItem5 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem5", tabItem5);
			tabItem5.setText(Messages.getString("start.delay"));
			tabItem5.setControl(m_startDelayComposite);

			//????????????
			m_endDelayComposite = new EndDelayComposite(m_tabFolder, SWT.NONE, false);
			WidgetTestUtil.setTestId(this, "m_endDelayComposite", m_endDelayComposite);
			TabItem tabItem6 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem6", tabItem6);
			tabItem6.setText(Messages.getString("end.delay"));
			tabItem6.setControl(m_endDelayComposite);
		}
		//???????????????/????????????????????????????????????????????????
		if (info.getType() != JobConstant.TYPE_REFERJOB && info.getType() != JobConstant.TYPE_REFERJOBNET) {
			//????????????
			m_endComposite = new EndStatusComposite(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "m_endComposite", m_endComposite);
			TabItem tabItem7 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem7", tabItem7);
			tabItem7.setText(Messages.getString("end.status"));
			tabItem7.setControl(m_endComposite);

			//??????????????????
			m_messageComposite = new NotificationsComposite(m_tabFolder, SWT.NONE, m_managerName);
			WidgetTestUtil.setTestId(this, "m_messageComposite", m_messageComposite);
			TabItem tabItem8 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem8", tabItem8);
			tabItem8.setText(Messages.getString("notifications"));
			tabItem8.setControl(m_messageComposite);
		}

		if (info.getType() == JobConstant.TYPE_JOBUNIT ) {
			//????????????????????????
			m_parameterComposite = new ParameterComposite(m_tabFolder, SWT.NONE, initFlag);
			WidgetTestUtil.setTestId(this, "m_parameterComposite", m_parameterComposite);
			TabItem tabItem9 = new TabItem(m_tabFolder, SWT.NONE);
			WidgetTestUtil.setTestId(this, "tabitem9", tabItem9);
			tabItem9.setText(Messages.getString("job.parameter"));
			tabItem9.setControl(m_parameterComposite);
		}

		m_tabFolder.setSelection(0);

		// ???????????????
		Display display = m_shell.getDisplay();
		m_shell.setLocation(
				(display.getBounds().width - m_shell.getSize().x) / 2, (display
						.getBounds().height - m_shell.getSize().y) / 2);

		//?????????????????????
		reflectJobInfo(info);

		updateWidgets();
	}

	private void updateWidgets() {
		JobInfo info = m_jobTreeItem.getData();
		if (m_jobTreeItem.getParent() == null) {
			// ???????????????????????????parent???null???????????????????????????????????????????????????????????????
			m_editButton.setEnabled(false);
		} else {
			m_editButton.setEnabled(m_readOnly);
		}
		
		if (info.getType() == JobConstant.TYPE_JOBUNIT && info.getCreateTime() != null) {
			// ?????????????????????????????????????????????????????????????????????ID?????????????????????
			m_jobIdText.setEditable(false);
		} else {
			// ???????????????????????????????????????????????????????????????
			m_jobIdText.setEditable(!m_readOnly);
		}
		
		m_jobNameText.setEditable(!m_readOnly);
		
		if (info.getType() == JobConstant.TYPE_JOBNET ||
				info.getType() == JobConstant.TYPE_APPROVALJOB ||
				info.getType() == JobConstant.TYPE_JOB ||
				info.getType() == JobConstant.TYPE_FILEJOB ||
				info.getType() == JobConstant.TYPE_MONITORJOB) {
			m_moduleRegisteredCondition.setEnabled(!m_readOnly);
		}
		m_jobAnnotationText.setEditable(!m_readOnly);
		if (info.getType() != JobConstant.TYPE_JOBUNIT) {
			m_iconIdCombo.setEnabled(!m_readOnly);
		}
		if (m_startComposite != null)
			m_startComposite.setEnabled(!m_readOnly);
		if (m_controlComposite != null)
			m_controlComposite.setEnabled(!m_readOnly);
		if (m_executeComposite != null)
			m_executeComposite.setEnabled(!m_readOnly);
		if (m_fileComposite != null)
			m_fileComposite.setEnabled(!m_readOnly);
		if (m_startDelayComposite != null)
			m_startDelayComposite.setEnabled(!m_readOnly);
		if (m_endDelayComposite != null)
			m_endDelayComposite.setEnabled(!m_readOnly);
		if (m_controlNodeComposite != null)
			m_controlNodeComposite.setEnabled(!m_readOnly);
		if (m_approvalComposite != null)
			m_approvalComposite.setEnabled(!m_readOnly);
		if (m_monitorComposite != null)
			m_monitorComposite.setEnabled(!m_readOnly);

		if (info.getType() != JobConstant.TYPE_REFERJOB && info.getType() != JobConstant.TYPE_REFERJOBNET) {
			m_endComposite.setEnabled(!m_readOnly);
			m_messageComposite.setEnabled(!m_readOnly);
		} else {
			if (m_referComposite != null) {
				m_referComposite.setEnabled(!m_readOnly);
			}
		}

		if (m_parameterComposite != null)
			m_parameterComposite.setEnabled(!m_readOnly);
	}

	/**
	 * ????????????
	 *
	 */
	public void update(){
		// ?????????????????????
		if("".equals(this.m_jobIdText.getText())){
			this.m_jobIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_jobIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_jobNameText.getText())){
			this.m_jobNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_jobNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @return ??????????????????????????????
	 * @since 1.0.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @return ???????????????????????????????????????
	 * @since 1.0.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobInfo
	 * @see com.clustercontrol.bean.JobConstant
	 */
	private void reflectJobInfo(JobInfo info) {
		if (info != null) {
			//?????????ID??????
			String jobId = info.getId();
			if (jobId != null) {
				m_jobIdText.setText(jobId);
			} else {
				m_jobIdText.setText("");
			}

			//??????????????????
			if (info.getName() != null) {
				m_jobNameText.setText(info.getName());
			} else {
				m_jobNameText.setText("");
			}
			// ???????????????????????????????????????
			m_moduleRegisteredCondition.setSelection(info.isRegisteredModule());
			
			//????????????
			if (info.getDescription() != null) {
				m_jobAnnotationText.setText(info.getDescription());
			} else {
				m_jobAnnotationText.setText("");
			}

			// ???????????????????????????
			if (info.getType() == JobConstant.TYPE_JOBUNIT) {
				if (info.getOwnerRoleId() != null) {
					this.m_ownerRoleId.setText(info.getOwnerRoleId());
				} else {
					this.m_ownerRoleId.setText(RoleIdConstant.ALL_USERS);
				}
			} else {
				JobTreeItem parentItem = m_jobTreeItem.getParent();
				if (parentItem != null) {
					// ????????????????????????????????????????????????????????????????????????ID???????????????
					//FullJob API???????????????????????????JobTreeItem???JobInfo???OwnerRoleId???????????????????????????????????????????????????
					while(parentItem.getData().getType() != JobConstant.TYPE_JOBUNIT) {
						parentItem = parentItem.getParent();
					}
					JobInfo parentInfo = parentItem.getData();
					this.m_ownerRoleId.setText(parentInfo.getOwnerRoleId());
				} else {
					// ??????????????????????????????????????????????????????????????????(???????????????????????????????????????????????????)??????
					// ??????????????????????????????????????????????????????ID??????????????????
					m_ownerRoleId.setText(info.getOwnerRoleId() == null ? "": info.getOwnerRoleId());
				}
			}

			// ????????????ID
			if (info.getType() != JobConstant.TYPE_JOBUNIT) {
				setIconIdComboItem(this.m_iconIdCombo, 
						this.m_managerName, 
						this.m_ownerRoleId.getText());
				if (info.getIconId() != null) {
					this.m_iconIdCombo.setText(info.getIconId());
				}
			}

			//??????????????????
			if( info.getType() != JobConstant.TYPE_REFERJOB && info.getType() != JobConstant.TYPE_REFERJOBNET ){
				this.m_messageComposite.getNotifyId().setOwnerRoleId(m_ownerRoleId.getText(), false);
			}

			JobWaitRuleInfo jobWaitRuleInfo = info.getWaitRule();
			if (jobWaitRuleInfo == null) {
				jobWaitRuleInfo = JobTreeItemUtil.getNewJobWaitRuleInfo();
			}

			//?????????????????????????????????????????????????????????
			if (info.getType() == JobConstant.TYPE_JOBNET) {
				//??????????????????
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//??????
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//????????????
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//????????????
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobConstant.TYPE_JOB) {
				//??????????????????
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//??????
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//????????????
				m_executeComposite.setCommandInfo(info.getCommand());
				m_executeComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_executeComposite.reflectCommandInfo();

				//????????????
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//????????????
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();

				//??????(????????????
				m_controlNodeComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlNodeComposite.setCommandInfo(info.getCommand());
				m_controlNodeComposite.reflectControlNodeInfo();
			}
			else if (info.getType() == JobConstant.TYPE_FILEJOB) {
				//??????????????????
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//??????
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//??????????????????
				m_fileComposite.setFileInfo(info.getFile());
				m_fileComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_fileComposite.setManagerName(this.m_managerName);
				m_fileComposite.reflectFileInfo();

				//????????????
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//????????????
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();

				//??????(?????????)
				m_controlNodeComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlNodeComposite.setFileInfo(info.getFile());
				m_controlNodeComposite.reflectControlNodeInfo();
			}
			//???????????????
			else if(info.getType() == JobConstant.TYPE_REFERJOB || info.getType() == JobConstant.TYPE_REFERJOBNET){
				//??????????????????
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();
				//???????????????
				m_referComposite.setReferJobUnitId(info.getReferJobUnitId());
				m_referComposite.setReferJobId(info.getReferJobId());
				m_referComposite.setReferJobSelectType(info.getReferJobSelectType());
				m_referComposite.setReferJobType(info.getType());
				m_referComposite.setJobTreeItem(m_jobTreeItem);
				m_referComposite.setJobTreeComposite(m_jobTreeComposite);
				m_referComposite.reflectReferInfo();
			}
			//???????????????
			else if (info.getType() == JobConstant.TYPE_APPROVALJOB) {
				//??????????????????
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//??????
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//??????
				m_approvalComposite.setApprovalReqRoleId(info.getApprovalReqRoleId());
				m_approvalComposite.setApprovalReqUserId(info.getApprovalReqUserId());
				m_approvalComposite.setApprovalReqSentence(info.getApprovalReqSentence());
				m_approvalComposite.setApprovalReqMailTitle(info.getApprovalReqMailTitle());
				m_approvalComposite.setApprovalReqMailBody(info.getApprovalReqMailBody());
				m_approvalComposite.setUseApprovalReqSentence(info.isUseApprovalReqSentence());
				m_approvalComposite.setJobTreeItem(m_jobTreeItem);
				m_approvalComposite.reflectApprovalInfo();

				//????????????
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//????????????
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			else if (info.getType() == JobConstant.TYPE_MONITORJOB) {
				//??????????????????
				m_startComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startComposite.setJobTreeItem(m_jobTreeItem);
				m_startComposite.reflectWaitRuleInfo();

				//??????
				m_controlComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_controlComposite.setJobTreeItem(m_jobTreeItem);
				m_controlComposite.getCalendarId().createCalIdCombo(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.getJobQueueDropdown().refreshList(this.m_managerName, this.m_ownerRoleId.getText());
				m_controlComposite.reflectWaitRuleInfo();

				//??????
				m_monitorComposite.setMonitorJobInfo(info.getMonitor());
				m_monitorComposite.setOwnerRoleId(this.m_ownerRoleId.getText());
				m_monitorComposite.reflectMonitorJobInfo();

				//????????????
				m_startDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_startDelayComposite.reflectWaitRuleInfo();

				//????????????
				m_endDelayComposite.setWaitRuleInfo(jobWaitRuleInfo);
				m_endDelayComposite.reflectWaitRuleInfo();
			}
			//?????????????????????????????????
			if (info.getType() != JobConstant.TYPE_REFERJOB && info.getType() != JobConstant.TYPE_REFERJOBNET) {
				//?????????????????????
				m_endComposite.setEndInfo(info.getEndStatus());
				m_endComposite.reflectEndInfo();

				//????????????????????????
				m_messageComposite.setJobInfo(info);
				m_messageComposite.getNotifyId().setOwnerRoleId(this.m_ownerRoleId.getText(), false);
				m_messageComposite.reflectNotificationsInfo();
			}

			if (info.getType() == JobConstant.TYPE_JOBUNIT) {

				//????????????????????????
				m_parameterComposite.setParamInfo(info.getParam());
				m_parameterComposite.reflectParamInfo(m_isCallJobHistory);
			}
		}
	}

	/**
	 * ????????????????????????????????????
	 *
	 * @return ????????????
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		result = createJobInfo();
		if (result != null) {
			return result;
		}

		JobInfo info = m_jobTreeItem.getData();
		if (info != null) {
			if (info.getType() == JobConstant.TYPE_JOBNET) {
				//??????????????????
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//??????
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobConstant.TYPE_JOB) {
				//??????????????????
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//??????
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_executeComposite.createCommandInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//?????????
				result = m_controlNodeComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//??????????????????
				result = m_controlNodeComposite.createCommandInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobConstant.TYPE_FILEJOB) {
				//??????????????????
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//??????
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//??????????????????
				result = m_fileComposite.createFileInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//?????????
				result = m_controlNodeComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
				
				//??????????????????
				result = m_controlNodeComposite.createFileInfo();
				if (result != null) {
					return result;
				}
			}
			//???????????????/????????????????????????
			else if(info.getType() == JobConstant.TYPE_REFERJOB || info.getType() == JobConstant.TYPE_REFERJOBNET){
				//??????????????????
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
				//???????????????
				result = m_referComposite.createReferInfo();
				if(result != null){
					return result;
				}
			}
			//???????????????
			else if (info.getType() == JobConstant.TYPE_APPROVALJOB) {
				//??????????????????
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
	
				//??????
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
	
				//??????
				result = m_approvalComposite.createApprovalInfo();
				if (result != null) {
					return result;
				}
	
				//????????????
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
	
				//????????????
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			} else if (info.getType() == JobConstant.TYPE_MONITORJOB) {
				//??????????????????
				result = m_startComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//??????
				result = m_controlComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//??????
				result = m_monitorComposite.createMonitorJobInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_startDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}

				//????????????
				result = m_endDelayComposite.createWaitRuleInfo();
				if (result != null) {
					return result;
				}
			}
			//???????????????/?????????????????????????????????????????????
			if(info.getType() != JobConstant.TYPE_REFERJOB && info.getType() != JobConstant.TYPE_REFERJOBNET){
				//?????????????????????
				result = m_endComposite.createEndInfo();
				if (result != null) {
					return result;
				}

				//????????????????????????
				result = m_messageComposite.createNotificationsInfo();
				if (result != null) {
					return result;
				}
			}

			if (info.getType() == JobConstant.TYPE_JOBUNIT) {
				//????????????????????????
				result = m_parameterComposite.createParamInfo();
				if (result != null) {
					return result;
				}
			}

			if (m_startComposite != null)
				info.setWaitRule(
						m_startComposite.getWaitRuleInfo());
			if (m_controlComposite != null)
				info.setWaitRule(
						m_controlComposite.getWaitRuleInfo());
			if (m_executeComposite != null)
				info.setCommand(
						m_executeComposite.getCommandInfo());
			if (m_monitorComposite != null)
				info.setMonitor(
						m_monitorComposite.getMonitorJobInfo());
			if (m_fileComposite != null)
				info.setFile(
						m_fileComposite.getFileInfo());
			if (m_endComposite != null) {
				List<JobEndStatusInfo> jobEndStatusInfoList = info.getEndStatus();
				jobEndStatusInfoList.clear();
				if (m_endComposite.getEndInfo() != null) {
					jobEndStatusInfoList.addAll(m_endComposite.getEndInfo());
				}
			} if (m_startDelayComposite != null)
				info.setWaitRule(
						m_startDelayComposite.getWaitRuleInfo());
			if (m_endDelayComposite != null)
				info.setWaitRule(
						m_endDelayComposite.getWaitRuleInfo());
			if (m_controlNodeComposite != null) {
				info.setWaitRule(
						m_controlNodeComposite.getWaitRuleInfo());
				//??????????????????????????????
				if (m_controlNodeComposite.getCommandInfo() != null) {
					info.getCommand().setMessageRetryEndFlg(m_controlNodeComposite.getCommandInfo().isMessageRetryEndFlg());
					info.getCommand().setMessageRetryEndValue(m_controlNodeComposite.getCommandInfo().getMessageRetryEndValue());
					info.getCommand().setMessageRetry(m_controlNodeComposite.getCommandInfo().getMessageRetry());
					info.getCommand().setCommandRetryFlg(m_controlNodeComposite.getCommandInfo().isCommandRetryFlg());
					info.getCommand().setCommandRetry(m_controlNodeComposite.getCommandInfo().getCommandRetry());
					info.getCommand().setCommandRetryEndStatus(m_controlNodeComposite.getCommandInfo().getCommandRetryEndStatus());
				}
				if (m_controlNodeComposite.getFileInfo() != null) {
					info.getFile().setMessageRetryEndFlg(m_controlNodeComposite.getFileInfo().isMessageRetryEndFlg());
					info.getFile().setMessageRetryEndValue(m_controlNodeComposite.getFileInfo().getMessageRetryEndValue());
					info.getFile().setMessageRetry(m_controlNodeComposite.getFileInfo().getMessageRetry());
					info.getFile().setCommandRetryFlg(m_controlNodeComposite.getFileInfo().isCommandRetryFlg());
					info.getFile().setCommandRetry(m_controlNodeComposite.getFileInfo().getCommandRetry());
				}
			}
			if (m_messageComposite != null){
				JobInfo messageJobInfo = m_messageComposite.getJobInfo();
				info.setBeginPriority(messageJobInfo.getBeginPriority());
				info.setNormalPriority(messageJobInfo.getNormalPriority());
				info.setWarnPriority(messageJobInfo.getWarnPriority());
				info.setAbnormalPriority(messageJobInfo.getAbnormalPriority());

				if (messageJobInfo.getNotifyRelationInfos() != null) {
					info.getNotifyRelationInfos().clear();
					info.getNotifyRelationInfos().addAll(messageJobInfo.getNotifyRelationInfos());
				}
			}

			if (m_parameterComposite != null){
				List<JobParameterInfo> jobParameterInfoinfoList = info.getParam();
				jobParameterInfoinfoList.clear();
				if (m_parameterComposite.getParamInfo() != null) {
					jobParameterInfoinfoList.addAll(m_parameterComposite.getParamInfo());
				}
			}

			//???????????????
			if(m_referComposite != null){
				if(m_referComposite.getReferJobUnitId() != null){
					info.setReferJobUnitId(m_referComposite.getReferJobUnitId());
				}
				if(m_referComposite.getReferJobId() != null){
					info.setReferJobId(m_referComposite.getReferJobId());
				}
				if(m_referComposite.getReferJobSelectType() != null){
					info.setReferJobSelectType(m_referComposite.getReferJobSelectType());
				}
				info.setType(m_referComposite.getReferJobType());
			}
			
			//???????????????
			if(m_approvalComposite != null){
				if(m_approvalComposite.getApprovalReqRoleId() != null){
					info.setApprovalReqRoleId(m_approvalComposite.getApprovalReqRoleId());
				}
				if(m_approvalComposite.getApprovalReqUserId() != null){
					info.setApprovalReqUserId(m_approvalComposite.getApprovalReqUserId());
				}
				if(m_approvalComposite.getApprovalReqSentence() != null){
					info.setApprovalReqSentence(m_approvalComposite.getApprovalReqSentence());
				}
				if(m_approvalComposite.getApprovalReqMailTitle() != null){
					info.setApprovalReqMailTitle(m_approvalComposite.getApprovalReqMailTitle());
				}
				if(m_approvalComposite.getApprovalReqMailBody() != null){
					info.setApprovalReqMailBody(m_approvalComposite.getApprovalReqMailBody());
				}
				info.setUseApprovalReqSentence(m_approvalComposite.isUseApprovalReqSentence());
			}

			info.setPropertyFull(true);
		}

		return null;
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????
	 *
	 * @return ????????????????????????
	 */
	private ValidateResult createJobInfo() {
		ValidateResult result = null;

		JobInfo info = m_jobTreeItem.getData();
		String oldJobId = info.getId();
		String oldJobunitId;

		// ?????????????????????ID?????????????????????(???????????????????????????????????????????????????????????????
		if (!m_readOnly && info.getType() == JobConstant.TYPE_JOBUNIT) {
			// ?????????????????????ID?????????????????????
			oldJobunitId = info.getJobunitId();
			info.setJobunitId(m_jobIdText.getText());
			try {
				JobUtil.findDuplicateJobunitId(m_jobTreeItem.getParent().getParent());
			} catch (JobInvalid e) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText() };
				result.setMessage(Messages.getString("message.job.64", args1));
				return result;
			} finally {
				info.setJobunitId(oldJobunitId);
			}
			// ?????????????????????ID??????????????????????????????
			if(!m_jobIdText.getText().matches(PatternConstant.HINEMOS_ID_PATTERN)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText(), Messages.getString("job.id")};
				result.setMessage(Messages.getString("message.common.6", args1));

				info.setJobunitId(oldJobunitId);
				return result;
			}

			JobEditState JobEditState = JobEditStateUtil.getJobEditState( m_managerName );
			if( JobEditState.getEditSession(m_jobTreeItem.getData()) == null ){
				// ??????????????????????????????????????????
				Integer editSession = null;

				try {
					editSession =JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, false);
				} catch (OtherUserGetLock_Exception e) {
					// ????????????????????????????????????????????????
					String message = HinemosMessage.replace(e.getMessage());
					if (MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							message)) {
						try {
							editSession = JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, true);
						} catch (Exception e1) {
							// ????????????????????????????????????
							m_log.error("run() : logical error");
						}
					}
				}
				if (editSession == null) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					String[] args1 = { m_jobIdText.getText() };
					result.setMessage(Messages.getString("message.job.105", args1));
					return result;
				}
				JobEditState.addLockedJobunit(info, null, editSession);
			} else if (!m_jobIdText.getText().equals(oldJobunitId)) {
				// ?????????????????????ID???????????????
				Integer oldEditSession = JobEditState.getEditSession(info);
				Integer editSession = null;
				try {
					editSession =JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, false);
				} catch (OtherUserGetLock_Exception e) {
					// ????????????????????????????????????????????????
					String message = HinemosMessage.replace(e.getMessage());
					if (MessageDialog.openQuestion(
							null,
							Messages.getString("confirmed"),
							message)) {
						try {
							editSession = JobUtil.getEditLock(m_managerName, m_jobIdText.getText(), null, true);
						} catch (Exception e1) {
							// ????????????????????????????????????
							m_log.error("run() : logical error");
						}
					}
				}
				if (editSession == null) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					String[] args1 = { m_jobIdText.getText() };
					result.setMessage(Messages.getString("message.job.105", args1));
					return result;
				}
				JobEditState.addLockedJobunit(info, null, editSession);
				try {
					JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(m_managerName);
					wrapper.releaseEditLock(oldEditSession);
				} catch (Exception e) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					String[] args1 = { m_jobIdText.getText() };
					result.setMessage(Messages.getString("message.job.105", args1));
					return result;
				}
			}
		}

		//?????????ID??????
		if (m_jobIdText.getText().length() > 0) {
			String oldId = info.getId();
			info.setId("");
			//?????????ID????????????????????????????????????????????????????????????????????????
			JobTreeItem unit = JobUtil.getTopJobUnitTreeItem(m_jobTreeItem);
			if(unit != null && JobUtil.findJobId(m_jobIdText.getText(), unit)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText() };
				result.setMessage(Messages.getString("message.job.42", args1));

				info.setId(oldId);
				return result;
			}
			// ?????????ID??????????????????????????????
			if(!m_jobIdText.getText().matches(PatternConstant.HINEMOS_ID_PATTERN)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				String[] args1 = { m_jobIdText.getText(), Messages.getString("job.id")};
				result.setMessage(Messages.getString("message.common.6", args1));

				info.setId(oldId);
				return result;
			}
			info.setId(m_jobIdText.getText());

			// ??????????????????????????????????????????????????????ID?????????????????????
			if (info.getType() == JobConstant.TYPE_JOBUNIT) {
				info.setJobunitId(m_jobIdText.getText());
			}


		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.22"));
			return result;
		}

		//??????????????????
		if (m_jobNameText.getText().length() > 0) {
			info.setName(m_jobNameText.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.23"));
			return result;
		}

		//???????????????????????????????????????
		info.setRegisteredModule(m_moduleRegisteredCondition.getSelection());

		//????????????
		if (m_jobAnnotationText.getText().length() > 0) {
			info.setDescription(m_jobAnnotationText.getText());
		} else {
			info.setDescription("");
		}

		// ????????????ID
		if (info.getType() != JobConstant.TYPE_JOBUNIT) {
			if (this.m_iconIdCombo.getText() == null) {
				info.setIconId("");
			} else {
				info.setIconId(this.m_iconIdCombo.getText());
			}
		}

		//?????????????????????ID??????
		//???????????????????????????JobInfo???OwnerRoleId???????????????
		//????????????????????????????????????????????????????????????OwnerRoleId??????????????????
		if (info.getType() == JobConstant.TYPE_JOBUNIT) {
			String newOwnerRoleId = m_ownerRoleId.getText();
			if (newOwnerRoleId.length() > 0) {
				if (!newOwnerRoleId.equals(info.getOwnerRoleId())) {
					info.setOwnerRoleId(newOwnerRoleId);
				}
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("owner.role.id"));
				return result;
			}
		} else {
			info.setOwnerRoleId(null);
		}

		//????????????????????????????????????????????????????????????????????????
		//???????????????????????????????????????????????????ID??????????????????(????????????????????????????????????????????????????????????????????????)
		//???????????????????????????????????????????????????????????????????????????????????????????????????????????????
		if (!oldJobId.equals(info.getId()) && info.getType() != JobConstant.TYPE_JOBUNIT) {
			List<JobTreeItem> siblings = m_jobTreeItem.getParent().getChildren();
			for (JobTreeItem sibling : siblings) {
				if (sibling == m_jobTreeItem) {
					continue;
				}

				JobInfo siblingJobInfo = sibling.getData();
				if (siblingJobInfo.getWaitRule() == null) {
					continue;
				}
				
				for (JobObjectInfo siblingWaitJobObjectInfo : siblingJobInfo.getWaitRule().getObject()) {
					if (oldJobId.equals(siblingWaitJobObjectInfo.getJobId())) {
						siblingWaitJobObjectInfo.setJobId(info.getId());
					}
				}
			}
		}
		
		//?????????????????????????????????ID?????????
		if (!oldJobId.equals(info.getId()) && info.getType() != JobConstant.TYPE_JOBUNIT) {
			//????????????jobunit?????????
			JobTreeItem treeItem = m_jobTreeItem;
			while (treeItem.getData().getType() != JobConstant.TYPE_JOBUNIT) {
				treeItem = treeItem.getParent();
			}
			
			//????????????????????????????????????????????????????????????
			updateReferJob(treeItem, oldJobId, info.getId());
		}
		
		return null;
	}

	private void updateReferJob(JobTreeItem treeItem, String oldJobId, String newJobId) {
		JobInfo info = treeItem.getData();
		if (info.getType() == JobConstant.TYPE_REFERJOB || info.getType() == JobConstant.TYPE_REFERJOBNET) {
			if (oldJobId.equals(info.getReferJobId())) {
				info.setReferJobId(newJobId);
			}
		}
		
		for (JobTreeItem childTreeItem : treeItem.getChildren()) {
			updateReferJob(childTreeItem, oldJobId, newJobId);
		}
	}

	/**
	 * ????????????????????????????????????????????????
	 *
	 * @return ??????????????????????????????
	 */
	public JobTreeItem getJobTreeItem() {
		return m_jobTreeItem;
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @param jobTreeItem ??????????????????????????????
	 */
	public void setJobTreeItem(JobTreeItem jobTreeItem) {
		this.m_jobTreeItem = jobTreeItem;
	}

	/**
	 * ????????????ID??????????????????????????????????????????????????????
	 *
	 * @param iconIdCombo ????????????ID????????????????????????
	 * @param managerName ??????????????????
	 * @param ownerRoleId ?????????????????????ID
	 */
	private void setIconIdComboItem(Combo iconIdCombo, String managerName, String ownerRoleId){

		// ?????????
		iconIdCombo.removeAll();
		// ??????????????????
		iconIdCombo.add("");

		List<String> iconIdList = null;
		// ???????????????
		try {
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
				iconIdList = wrapper.getJobmapIconImageIdListForSelect(ownerRoleId);
			}
		} catch (InvalidRole_Exception e) {
			// ????????????
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			// ?????????????????????
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		if(iconIdList != null){
			for(String iconId : iconIdList){
				iconIdCombo.add(iconId);
			}
		}
	}

}
