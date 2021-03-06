/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.PatternConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.StringVerifyListener;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetRuntimeParameterTableDefine;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobRuntimeParamTypeMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.JobRuntimeParam;
import com.clustercontrol.ws.jobmanagement.JobRuntimeParamDetail;

/**
 * ????????????????????? ???????????????????????????????????????????????????????????????
 *
 * @version 5.1.0
 */
public class RuntimeParameterDialog extends CommonDialog {

	/** ????????? */
	private Shell m_shell = null;

	/** ???????????????????????? */
	private Text m_txtParamId = null;
	/** ????????????????????? */
	private Text m_txtDescription = null;
	/** ?????????????????????????????? */
	private Combo m_cmbType = null;
	/** ????????????????????????????????? */
	private Text m_txtDefaultValue = null;
	/** ??????????????????????????????????????? */
	private CommonTableViewer m_viewer = null;
	/** ???????????? ?????????????????? */
	private Button m_btnSelectAdd = null;
	/** ???????????? ?????????????????? */
	private Button m_btnSelectModify = null;
	/** ???????????? ?????????????????? */
	private Button m_btnSelectDelete = null;
	/** ???????????? ????????????????????? */
	private Button m_btnSelectCopy = null;
	/** ???????????? ?????????????????? */
	private Button m_btnSelectUp = null;
	/** ???????????? ?????????????????? */
	private Button m_btnSelectDown = null;
	/** ??????????????????????????????????????? */
	private Button m_chkRequiredFlg = null;

	/** ???????????????????????????????????? */
	private JobRuntimeParam m_jobRuntimeParam = null;

	/** ??????????????????????????????????????? */
	private Map<String, JobRuntimeParam> m_parentJobRuntimeParamMap = new HashMap<>();
	
	/**
	 * ?????????????????????
	 * ?????????
	 * @param parent
	 * @param paramInfo
	 * @param mode
	 */
	public RuntimeParameterDialog(Shell parent, Map<String, JobRuntimeParam> parentJobRuntimeParamMap,
		JobRuntimeParam jobRuntimeParam){
		super(parent);
		this.m_jobRuntimeParam = jobRuntimeParam;
		this.m_parentJobRuntimeParamMap = parentJobRuntimeParamMap;
	}

	/**
	 * ?????????????????????
	 * ???????????????
	 * @param parent
	 */
	public RuntimeParameterDialog(Shell parent,
			Map<String, JobRuntimeParam> parentJobRuntimeParamMap){
		super(parent);
		this.m_parentJobRuntimeParamMap = parentJobRuntimeParamMap;
		this.m_jobRuntimeParam = new JobRuntimeParam();
	}

	/**
	 * ?????????????????????????????????????????????
	 *
	 * @param parent ?????????????????????
	 */
	@Override
	protected void customizeDialog(Composite parent) {

		m_shell = this.getShell();
		parent.getShell().setText(Messages.getString("dialog.job.add.modify.manual.param"));

		Label label = null;
		
		/**
		 * ?????????????????????
		 * ????????????????????????????????????????????????????????????????????????
		 */
		RowLayout layout = new RowLayout();
		layout.type = SWT.VERTICAL;
		layout.spacing = 0;
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.marginBottom = 0;
		layout.fill = true;
		parent.setLayout(layout);

		// Composite
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		// ?????????????????????
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("name") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// ????????????????????????
		this.m_txtParamId = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtParamId", this.m_txtParamId);
		this.m_txtParamId.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtParamId.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_64));
		this.m_txtParamId.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		
		// dummy
		new Label(composite, SWT.NONE);

		// ?????????????????????
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("description") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// ????????????????????????
		this.m_txtDescription = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtDescription", this.m_txtDescription);
		this.m_txtDescription.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtDescription.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_256));
		this.m_txtDescription.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// ?????????????????????
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("type") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// ?????????????????????????????????
		this.m_cmbType = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_cmbType", this.m_cmbType);
		this.m_cmbType.setLayoutData(new GridData(200,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_cmbType.add(Messages.getString("job.manual.type.input"));
		this.m_cmbType.add(Messages.getString("job.manual.type.radio"));
		this.m_cmbType.add(Messages.getString("job.manual.type.combo"));
		this.m_cmbType.add(Messages.getString("job.manual.type.fixed"));
		this.m_cmbType.select(0);
		this.m_cmbType.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo combo = (Combo) e.getSource();
				WidgetTestUtil.setTestId(this, null, combo);
				Integer type = JobRuntimeParamTypeMessage.stringToType(combo.getText());
				if (type == JobRuntimeParamTypeConstant.TYPE_INPUT) {
					m_txtDefaultValue.setEditable(true);
				} else if (type == JobRuntimeParamTypeConstant.TYPE_RADIO) {
					m_txtDefaultValue.setEditable(false);
				} else if (type == JobRuntimeParamTypeConstant.TYPE_COMBO) {
					m_txtDefaultValue.setEditable(false);
				} else if (type == JobRuntimeParamTypeConstant.TYPE_FIXED) {
					m_txtDefaultValue.setEditable(true);
				}
				update();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// ?????????????????????????????????
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("default.value") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));

		// ????????????????????????????????????
		this.m_txtDefaultValue = new Text(composite, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_txtDefaultValue", this.m_txtDefaultValue);
		this.m_txtDefaultValue.setLayoutData(new GridData(220, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_txtDefaultValue.addVerifyListener(
				new StringVerifyListener(DataRangeConstant.VARCHAR_1024));
		this.m_txtDefaultValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// dummy
		new Label(composite, SWT.NONE);

		// ???????????????????????????
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("job.manual.select.item") + " : ");
		label.setLayoutData(new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT));
		((GridData)label.getLayoutData()).verticalSpan = 6;
		((GridData)label.getLayoutData()).verticalAlignment = SWT.BEGINNING;

		// ??????????????????????????????
		Table table = new Table(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
		WidgetTestUtil.setTestId(this, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(210, 135));
		((GridData)table.getLayoutData()).verticalSpan = 6;

		// ????????????????????????????????????
		this.m_btnSelectAdd = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectAdd", this.m_btnSelectAdd);
		this.m_btnSelectAdd.setText(Messages.getString("add"));
		this.m_btnSelectAdd.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RuntimeParameterSelectionDialog dialog 
					= new RuntimeParameterSelectionDialog(
							m_shell,
							m_jobRuntimeParam.getJobRuntimeParamDetailList(),
							new JobRuntimeParamDetail());
				if (dialog.open() == IDialogConstants.OK_ID) {
					m_jobRuntimeParam.getJobRuntimeParamDetailList().add(dialog.getInputData());
					if (dialog.getDefaultValueSelection()) {
						m_jobRuntimeParam.setValue(
							dialog.getInputData().getParamValue());
					}
					reflectParamDetailInfo();
				}
			}
		});

		// ????????????????????????????????????
		this.m_btnSelectModify = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectModify", this.m_btnSelectModify);
		this.m_btnSelectModify.setText(Messages.getString("modify"));
		this.m_btnSelectModify.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectModify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					RuntimeParameterSelectionDialog dialog 
						= new RuntimeParameterSelectionDialog(
							m_shell,
							m_jobRuntimeParam.getJobRuntimeParamDetailList(),
							m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo),
							(m_jobRuntimeParam.getValue() == null ? false : 
								m_jobRuntimeParam.getValue().equals(
								m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue())));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo, dialog.getInputData());
						if (dialog.getDefaultValueSelection()) {
							m_jobRuntimeParam.setValue(
								dialog.getInputData().getParamValue());
						} else {
							if (m_jobRuntimeParam.getValue() != null
									&& m_jobRuntimeParam.getValue().equals(
									dialog.getInputData().getParamValue())) {
								m_jobRuntimeParam.setValue(null);
							}
						}
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// ????????????????????????????????????
		this.m_btnSelectDelete = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectDelete", this.m_btnSelectDelete);
		this.m_btnSelectDelete.setText(Messages.getString("delete"));
		this.m_btnSelectDelete.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					String detail = m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getDescription();
					if (detail == null) {
						detail = "";
					}

					String[] args = { detail };
					if (MessageDialog.openConfirm(
							null,
							Messages.getString("confirmed"),
							Messages.getString("message.job.130", args))) {
						if (m_jobRuntimeParam.getValue() != null
								&& m_jobRuntimeParam.getValue().equals(
										m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue())) {
							m_jobRuntimeParam.setValue(null);
						}
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// ???????????????????????????????????????
		this.m_btnSelectCopy = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectCopy", this.m_btnSelectCopy);
		this.m_btnSelectCopy.setText(Messages.getString("copy"));
		this.m_btnSelectCopy.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					// ??????????????????
					JobRuntimeParamDetail paramDetail = new JobRuntimeParamDetail();
					paramDetail.setDescription(m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getDescription());
					paramDetail.setParamValue(m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue());
					RuntimeParameterSelectionDialog dialog 
						= new RuntimeParameterSelectionDialog(
								m_shell,
								m_jobRuntimeParam.getJobRuntimeParamDetailList(),
								paramDetail);
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(dialog.getInputData());
						if (dialog.getDefaultValueSelection()) {
							m_jobRuntimeParam.setValue(
								dialog.getInputData().getParamValue());
						}
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// ????????????????????????????????????
		this.m_btnSelectUp = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectUp", this.m_btnSelectUp);
		this.m_btnSelectUp.setText(Messages.getString("up"));
		this.m_btnSelectUp.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					if (orderNo > 0) {
						JobRuntimeParamDetail jobRuntimeParamDetail 
							= m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo-1, jobRuntimeParamDetail);
						reflectParamDetailInfo();
						m_viewer.getTable().setSelection(orderNo-1);
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// ????????????????????????????????????
		this.m_btnSelectDown = new Button(composite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_btnSelectDown", this.m_btnSelectDown);
		this.m_btnSelectDown.setText(Messages.getString("down"));
		this.m_btnSelectDown.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_btnSelectDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					if (orderNo < m_jobRuntimeParam.getJobRuntimeParamDetailList().size() - 1) {
						JobRuntimeParamDetail jobRuntimeParamDetail 
							= m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo+1, jobRuntimeParamDetail);
						reflectParamDetailInfo();
						m_viewer.getTable().setSelection(orderNo+1);
					}
				}
				else{
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});
		
		// ???????????????????????????
		label = new Label(composite, SWT.LEFT);
		label.setText(Messages.getString("job.manual.required") + " : ");
		label.setLayoutData(new GridData(60, SizeConstant.SIZE_LABEL_HEIGHT));

		// ??????????????????????????????????????????
		this.m_chkRequiredFlg = new Button(composite, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_chkRequiredFlg", this.m_chkRequiredFlg);
		this.m_btnSelectDown.setLayoutData(new GridData(50,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// dummy
		new Label(composite, SWT.NONE);

		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetRuntimeParameterTableDefine.get(),
				GetRuntimeParameterTableDefine.SORT_COLUMN_INDEX,
				GetRuntimeParameterTableDefine.SORT_ORDER);
		this.m_viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (m_viewer.getTable().getSelectionIndex() >= 0) {
					int orderNo = (Integer)((ArrayList<?>)m_viewer.getTable()
							.getSelection()[0].getData()).get(1) - 1;
					RuntimeParameterSelectionDialog dialog
						= new RuntimeParameterSelectionDialog(
							m_shell,
							m_jobRuntimeParam.getJobRuntimeParamDetailList(),
							m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo),
							(m_jobRuntimeParam.getValue() == null ? false : 
								m_jobRuntimeParam.getValue().equals(
								m_jobRuntimeParam.getJobRuntimeParamDetailList().get(orderNo).getParamValue())));
					if (dialog.open() == IDialogConstants.OK_ID) {
						m_jobRuntimeParam.getJobRuntimeParamDetailList().remove(orderNo);
						m_jobRuntimeParam.getJobRuntimeParamDetailList().add(orderNo, dialog.getInputData());
						if (dialog.getDefaultValueSelection()) {
							m_jobRuntimeParam.setValue(
									dialog.getInputData().getParamValue());
						} else {
							if (m_jobRuntimeParam.getValue() == null || m_jobRuntimeParam.getValue().equals(
									dialog.getInputData().getParamValue())) {
								m_jobRuntimeParam.setValue(null);
							}
						}
						reflectParamDetailInfo();
					}
				} else {
					MessageDialog.openWarning(
							null,
							Messages.getString("warning"),
							Messages.getString("message.job.129"));
				}
			}
		});

		// ?????????????????????????????????
		reflectParamInfo();

		// ????????????
		update();
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @return ???????????????
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 400);
	}


	/**
	 * ??????????????????????????????????????????????????????????????????????????????
	 *
	 */
	public void reflectParamInfo() {
		if (this.m_jobRuntimeParam != null) {
			// ??????
			if (this.m_jobRuntimeParam.getParamId() != null) {
				this.m_txtParamId.setText(this.m_jobRuntimeParam.getParamId());
			}
			// ??????
			if (this.m_jobRuntimeParam.getDescription() != null) {
				this.m_txtDescription.setText(this.m_jobRuntimeParam.getDescription());
			}
			// ??????
			if (this.m_jobRuntimeParam.getParamType() != null) {
				this.m_cmbType.setText(JobRuntimeParamTypeMessage.typeToString(this.m_jobRuntimeParam.getParamType()));
			}
			// ??????????????????
			if (this.m_jobRuntimeParam.getParamType() != null
					&& this.m_jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_RADIO
					&& this.m_jobRuntimeParam.getParamType() != JobRuntimeParamTypeConstant.TYPE_COMBO
					&& this.m_jobRuntimeParam.getValue() != null) {
				this.m_txtDefaultValue.setText(this.m_jobRuntimeParam.getValue());
			}
			// ????????????
			if (this.m_jobRuntimeParam.getParamType() != null
					&& (this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_INPUT
					|| this.m_jobRuntimeParam.getParamType() == JobRuntimeParamTypeConstant.TYPE_COMBO)
				&& this.m_jobRuntimeParam.isRequiredFlg() != null) {
				this.m_chkRequiredFlg.setSelection(this.m_jobRuntimeParam.isRequiredFlg());
			}
			// ????????????
			reflectParamDetailInfo();
		}
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????????????????
	 *
	 */
	public void reflectParamDetailInfo() {

		// ????????????
		ArrayList<ArrayList<?>> tableData = new ArrayList<ArrayList<?>>();
		if ((this.m_cmbType.getText().equals(JobRuntimeParamTypeMessage.STRING_RADIO)
				|| this.m_cmbType.getText().equals(JobRuntimeParamTypeMessage.STRING_COMBO))
				&& this.m_jobRuntimeParam.getJobRuntimeParamDetailList() != null
				&& this.m_jobRuntimeParam.getJobRuntimeParamDetailList().size() > 0) {
			int detailIdx = 1;
			if (this.m_cmbType.getText().equals(JobRuntimeParamTypeMessage.STRING_RADIO)) {
				// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
				if (this.m_jobRuntimeParam.getValue() == null
						|| "".equals(this.m_jobRuntimeParam.getValue())) {
					this.m_jobRuntimeParam.setValue(
						this.m_jobRuntimeParam.getJobRuntimeParamDetailList()
						.get(0).getParamValue());
				}
			}
			for (JobRuntimeParamDetail jobRuntimeParamDetail
					: this.m_jobRuntimeParam.getJobRuntimeParamDetailList()) {
				ArrayList<Object> tableLineData = new ArrayList<Object>();
				if (this.m_jobRuntimeParam.getValue() != null
						&& this.m_jobRuntimeParam.getValue().equals(
								jobRuntimeParamDetail.getParamValue())) {
					tableLineData.add("*");
				} else {
					tableLineData.add("");
				}
				tableLineData.add(detailIdx);
				tableLineData.add(jobRuntimeParamDetail.getParamValue());
				tableLineData.add(jobRuntimeParamDetail.getDescription());
				tableData.add(tableLineData);
				detailIdx++;
			}
		}
		this.m_viewer.setInput(tableData);
	}

	/**
	 * ??????????????????????????? ????????????
	 *
	 */
	public void update(){
		// ??????
		Integer type = JobRuntimeParamTypeMessage.stringToType(this.m_cmbType.getText());

		// ?????????????????????
		if("".equals(this.m_txtParamId.getText())){
			this.m_txtParamId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtParamId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if("".equals(this.m_txtDescription.getText())){
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_txtDescription.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if (type == JobRuntimeParamTypeConstant.TYPE_FIXED 
				&& "".equals(this.m_txtDefaultValue.getText())){
			this.m_txtDefaultValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_txtDefaultValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}

		// ??????????????????
		boolean selectFlg = type == JobRuntimeParamTypeConstant.TYPE_RADIO 
				|| type == JobRuntimeParamTypeConstant.TYPE_COMBO;
		this.m_btnSelectAdd.setEnabled(selectFlg);
		this.m_btnSelectModify.setEnabled(selectFlg);
		this.m_btnSelectDelete.setEnabled(selectFlg);
		this.m_btnSelectCopy.setEnabled(selectFlg);
		this.m_btnSelectUp.setEnabled(selectFlg);
		this.m_btnSelectDown.setEnabled(selectFlg);
		this.m_chkRequiredFlg.setEnabled((type == JobRuntimeParamTypeConstant.TYPE_INPUT
				|| type == JobRuntimeParamTypeConstant.TYPE_COMBO));
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @return ??????????????????????????????
	 * @since 2.1.0
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @return ???????????????????????????????????????
	 * @since 2.1.0
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
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

		if (this.m_jobRuntimeParam == null) {
			// ????????????
			this.m_jobRuntimeParam = new JobRuntimeParam();
		}

		// ?????????
		if (this.m_txtParamId.getText() != null
				&& !this.m_txtParamId.getText().equals("")) {
			//?????????????????????????????????'_'???'-'????????????????????????
			if(!m_txtParamId.getText().matches(PatternConstant.HINEMOS_ID_PATTERN)){
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.60"));
				return result;
			}
			// ??????????????????
			if (isParameterDuplicate(this.m_txtParamId.getText(), this.m_jobRuntimeParam.getParamId())) {
				// ???????????????????????????
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.134"));
				return result;
			}
			this.m_jobRuntimeParam.setParamId(this.m_txtParamId.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.19"));
			return result;
		}
		// ??????
		if (this.m_txtDescription.getText() != null
				&& !this.m_txtDescription.getText().equals("")) {
			this.m_jobRuntimeParam.setDescription(this.m_txtDescription.getText());
		} else {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.128"));
			return result;
		}
		// ??????
		Integer type = JobRuntimeParamTypeMessage.stringToType(this.m_cmbType.getText());
		this.m_jobRuntimeParam.setParamType(type);
		// ??????????????????
		if (type == JobRuntimeParamTypeConstant.TYPE_FIXED
				|| type == JobRuntimeParamTypeConstant.TYPE_INPUT) {
			if (this.m_txtDefaultValue.getText() != null
					&& !this.m_txtDefaultValue.getText().equals("")) {
				this.m_jobRuntimeParam.setValue(this.m_txtDefaultValue.getText());
			} else {
				if (type == JobRuntimeParamTypeConstant.TYPE_FIXED) {
					result = new ValidateResult();
					result.setValid(false);
					result.setID(Messages.getString("message.hinemos.1"));
					result.setMessage(Messages.getString("message.job.132"));
					return result;
				}
			}
		}
		// ????????????
		if (type == JobRuntimeParamTypeConstant.TYPE_RADIO
				|| type == JobRuntimeParamTypeConstant.TYPE_COMBO) {
			if (this.m_jobRuntimeParam.getJobRuntimeParamDetailList() == null
				|| this.m_jobRuntimeParam.getJobRuntimeParamDetailList().size() == 0) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.133"));
				return result;
			}
		}

		// ????????????
		this.m_jobRuntimeParam.setRequiredFlg(this.m_chkRequiredFlg.getSelection());

		return null;
	}

	/**
	 * ??????????????????????????????????????????????????????
	 *
	 * @return ?????????????????????
	 */
	public JobRuntimeParam getInputData() {
		return this.m_jobRuntimeParam;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????
	 * 
	 * @param newParamId ???????????????????????????????????????
	 * @param oldParamId ???????????????????????????????????????
	 * @return true:????????????, false:????????????
	 */
	private boolean isParameterDuplicate(String newParamId, String oldParamId) {
		boolean result = false;
		if (m_parentJobRuntimeParamMap == null) {
			// ???????????????????????????????????????
			return result;
		}
		if (oldParamId != null && oldParamId.equals(newParamId)) {
			// ?????????????????????????????????????????????
			return result;
		}
		for (Map.Entry<String, JobRuntimeParam> entry : m_parentJobRuntimeParamMap.entrySet()) {
			if (oldParamId != null && entry.getKey().equals(oldParamId)) {
				continue;
			}
			if (entry.getKey().equals(newParamId)) {
				result = true;
				break;
			}
		}
		return result;
	}
}
