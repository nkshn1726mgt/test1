/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.composite.action.NumberVerifyListener;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.action.GetWaitRuleTableDefine;
import com.clustercontrol.jobmanagement.bean.ConditionTypeConstant;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.composite.action.WaitRuleSelectionChangedListener;
import com.clustercontrol.jobmanagement.dialog.WaitRuleDialog;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;

/**
 * ????????????????????????????????????????????????????????????
 *
 * @version 2.1.0
 * @since 1.0.0
 */
public class WaitRuleComposite extends Composite {
	/** ????????????????????????????????? */
	private static Log m_log = LogFactory.getLog( WaitRuleComposite.class );
	/** ???????????????????????? */
	private CommonTableViewer m_viewer = null;
	/** ??????????????????????????? AND????????????????????? */
	private Button m_andCondition = null;
	/** ??????????????????????????? OR????????????????????? */
	private Button m_orCondition = null;
	/** ?????????????????????????????????????????????????????????????????? */
	private Button m_endCondition = null;
	/** ????????????????????????????????????????????????????????? */
	private Combo m_endStatus = null;
	/** ?????????????????????????????????????????????????????? */
	private Text m_endValue = null;
	/** ?????????????????? */
	private Button m_createCondition = null;
	/** ?????????????????? */
	private Button m_modifyCondition = null;
	/** ?????????????????? */
	private Button m_deleteCondition = null;
	/** ??????????????????????????? */
	private JobWaitRuleInfo m_waitRule = null;
	/** ????????? */
	private Shell m_shell = null;
	/** ?????????????????? */
	private ArrayList<Object> m_selectItem = null;

	private JobTreeItem m_jobTreeItem = null;

	/**
	 * ?????????????????????
	 *
	 * @param parent ????????????????????????
	 * @param style ????????????
	 * @param jobType ??????????????????
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public WaitRuleComposite(Composite parent, int style, int jobType) {
		super(parent, style);
		initialize(jobType);
		m_shell = this.getShell();
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @param jobType ??????????????????
	 */
	private void initialize(int jobType) {

		this.setLayout(JobDialogUtil.getParentLayout());

		// ?????????????????????????????????
		Label tableTitle = new Label(this, SWT.NONE);
		tableTitle.setText(Messages.getString("object.list"));

		// ????????????????????????????????????
		int addTableWidth = 0;
		if (jobType == JobConstant.TYPE_JOBNET) {
			// ??????????????????
			addTableWidth = 0;
		} else if (jobType == JobConstant.TYPE_JOB) {
			// ?????????????????????
			addTableWidth = 170;
		} else if (jobType == JobConstant.TYPE_FILEJOB) {
			// ?????????????????????
			addTableWidth = 170;
		} else if (jobType == JobConstant.TYPE_REFERJOB || jobType == JobConstant.TYPE_REFERJOBNET) {
			// ???????????????????????????????????????????????????
			addTableWidth = 0;
		} else if (jobType == JobConstant.TYPE_APPROVALJOB) {
			// ???????????????
			addTableWidth = 170;
		} else if (jobType == JobConstant.TYPE_MONITORJOB) {
			// ???????????????
			addTableWidth = 170;
		}

		Table table = new Table(this, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE);
		WidgetTestUtil.setTestId(this, "table", table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new RowData(430 + addTableWidth, 100));

		// ????????????Composite???
		Composite buttonComposite = new Composite(this, SWT.NONE);
		buttonComposite.setLayout(new RowLayout());

		// dummy
		new Label(buttonComposite, SWT.NONE)
			.setLayoutData(new RowData(200 + addTableWidth, SizeConstant.SIZE_LABEL_HEIGHT));

		// ?????????????????????????????????
		this.m_createCondition = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_createCondition", this.m_createCondition);
		this.m_createCondition.setText(Messages.getString("add"));
		this.m_createCondition.setLayoutData(new RowData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_createCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				m_log.debug("widgetSelected");
				WaitRuleDialog dialog = new WaitRuleDialog(m_shell, m_jobTreeItem);
				if (dialog.open() == IDialogConstants.OK_ID) {

					// ?????????????????????????????????????????????????????????
					boolean addWaitRule = true;

					@SuppressWarnings("unchecked")
					ArrayList<Integer> info = (ArrayList<Integer>)dialog.getInputData();
					@SuppressWarnings("unchecked")
					ArrayList<ArrayList<Integer>> list = (ArrayList<ArrayList<Integer>>) m_viewer.getInput();
					if (list == null) {
						list = new ArrayList<ArrayList<Integer>>();

					} else {

						//????????????????????????????????????????????????????????????????????????????????????false?????????
						int newWaitRuleType = (Integer) info.get(GetWaitRuleTableDefine.JUDGMENT_OBJECT);
						if (newWaitRuleType == JudgmentObjectConstant.TYPE_TIME){

							for (ArrayList<Integer> waitRule : list) {
								m_log.debug("WaitRuleComposite_initialize_info = " + info);
								int rule = waitRule.get(GetWaitRuleTableDefine.JUDGMENT_OBJECT);
								m_log.debug("WaitRuleComposite_initialize_rule = " + rule);
								if (rule == JudgmentObjectConstant.TYPE_TIME) {
									addWaitRule = false;

									MessageDialog.openWarning(
											null,
											Messages.getString("warning"),
											Messages.getString("message.job.61"));
								}
							}
						} else if (newWaitRuleType == JudgmentObjectConstant.TYPE_START_MINUTE){

							for (ArrayList<Integer> waitRule : list) {
								m_log.debug("WaitRuleComposite_initialize_info = " + info);
								int rule = waitRule.get(GetWaitRuleTableDefine.JUDGMENT_OBJECT);
								m_log.debug("WaitRuleComposite_initialize_rule = " + rule);
								if (rule == JudgmentObjectConstant.TYPE_START_MINUTE) {
									addWaitRule = false;

									MessageDialog.openWarning(
											null,
											Messages.getString("warning"),
											Messages.getString("message.job.62"));
								}
							}
						}
					}

					if (addWaitRule) list.add(info);
					m_viewer.setInput(list);
				}
			}
		});

		// ?????????????????????????????????
		this.m_modifyCondition = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_modifyCondition", this.m_modifyCondition);
		this.m_modifyCondition.setText(Messages.getString("modify"));
		this.m_modifyCondition.setLayoutData(new RowData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_modifyCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WaitRuleDialog dialog = new WaitRuleDialog(m_shell, m_jobTreeItem);
				if (m_selectItem != null) {
					dialog.setInputData(m_selectItem);
					if (dialog.open() == IDialogConstants.OK_ID) {

						ArrayList<?> info = dialog.getInputData();
						@SuppressWarnings("unchecked")
						ArrayList<ArrayList<?>> list = (ArrayList<ArrayList<?>>) m_viewer.getInput();

						list.remove(m_selectItem);
						list.add(info);

						m_selectItem = null;
						m_viewer.setInput(list);
					}
				} else {

				}
			}
		});

		// ?????????????????????????????????
		this.m_deleteCondition = new Button(buttonComposite, SWT.NONE);
		WidgetTestUtil.setTestId(this, "m_deleteCondition", this.m_deleteCondition);
		this.m_deleteCondition.setText(Messages.getString("delete"));
		this.m_deleteCondition.setLayoutData(new RowData(80,
				SizeConstant.SIZE_BUTTON_HEIGHT));
		this.m_deleteCondition.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<?> list = (ArrayList<?>) m_viewer.getInput();
				list.remove(m_selectItem);
				m_selectItem = null;
				m_viewer.setInput(list);
			}
		});

		// separator
		JobDialogUtil.getSeparator(this);

		// ?????????????????????????????????????????????
		Group group = new Group(this, SWT.NONE);
		group.setText(Messages.getString("condition.between.objects"));
		group.setLayout(new RowLayout());

		// ??????????????????????????????AND???????????????
		this.m_andCondition = new Button(group, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_andCondition", this.m_andCondition);
		this.m_andCondition.setText(Messages.getString("and"));
		this.m_andCondition.setLayoutData(new RowData(100,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// ??????????????????????????????OR???????????????
		this.m_orCondition = new Button(group, SWT.RADIO);
		WidgetTestUtil.setTestId(this, "m_orCondition", this.m_orCondition);
		this.m_orCondition.setText(Messages.getString("or"));
		this.m_orCondition.setLayoutData(new RowData(100,
				SizeConstant.SIZE_BUTTON_HEIGHT));

		// separator
		JobDialogUtil.getSeparator(this);

		// ??????????????????????????????????????????????????????
		this.m_endCondition = new Button(JobDialogUtil.getComposite_MarginZero(this), SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_endCondition", this.m_endCondition);
		this.m_endCondition.setText(Messages.getString("end.if.condition.unmatched"));
		this.m_endCondition.setLayoutData(new RowData(220, SizeConstant.SIZE_BUTTON_HEIGHT + 5));
		this.m_endCondition.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_endStatus.setEnabled(true);
					m_endValue.setEditable(true);
				} else {
					m_endStatus.setEnabled(false);
					m_endValue.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// ???????????????????????????????????????Composite???
		Composite endConditionGroup = new Composite(this, SWT.BORDER);
		endConditionGroup.setLayout(new GridLayout(2, false));

		// ??????????????????????????????????????????????????????????????????
		Label endStatusTitle = new Label(endConditionGroup, SWT.LEFT);
		endStatusTitle.setText(Messages.getString("end.status") + " : ");
		endStatusTitle.setLayoutData(new GridData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// ??????????????????????????????????????????????????????????????????
		this.m_endStatus = new Combo(endConditionGroup, SWT.CENTER | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, "m_endStatus", this.m_endStatus);
		this.m_endStatus.setLayoutData(new GridData(100,
				SizeConstant.SIZE_COMBO_HEIGHT));
		this.m_endStatus.add(EndStatusMessage.STRING_NORMAL);
		this.m_endStatus.add(EndStatusMessage.STRING_WARNING);
		this.m_endStatus.add(EndStatusMessage.STRING_ABNORMAL);

		// ???????????????????????????????????????????????????????????????
		Label endValueTitle = new Label(endConditionGroup, SWT.LEFT);
		endValueTitle.setText(Messages.getString("end.value") + " : ");
		endValueTitle.setLayoutData(new GridData(80,
				SizeConstant.SIZE_LABEL_HEIGHT));

		// ??????????????????????????????????????????????????????????????????
		this.m_endValue = new Text(endConditionGroup, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "m_endValue", this.m_endValue);
		this.m_endValue.setLayoutData(new GridData(100, SizeConstant.SIZE_TEXT_HEIGHT));
		this.m_endValue.addVerifyListener(
				new NumberVerifyListener(DataRangeConstant.SMALLINT_LOW, DataRangeConstant.SMALLINT_HIGH));
		this.m_endValue.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		this.m_viewer = new CommonTableViewer(table);
		this.m_viewer.createTableColumn(GetWaitRuleTableDefine.get(),
				GetWaitRuleTableDefine.SORT_COLUMN_INDEX,
				GetWaitRuleTableDefine.SORT_ORDER);
		this.m_viewer
		.addSelectionChangedListener(new WaitRuleSelectionChangedListener(
				this));
	}

	@Override
	public void update() {
		if(m_endCondition.getSelection() && "".equals(this.m_endValue.getText())){
			this.m_endValue.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.m_endValue.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public void reflectWaitRuleInfo() {
		if (m_waitRule != null) {
			//????????????????????????????????????
			List<JobObjectInfo> list = m_waitRule.getObject();
			if(list != null){
				m_log.debug("reflectWaitRuleInfo_JobObjectInfo.size() = " + list.size());
				ArrayList<Object> tableData = new ArrayList<Object>();
				for (int i = 0; i < list.size(); i++) {
					m_log.debug("loop count = " + i);
					JobObjectInfo info = list.get(i);
					ArrayList<Object> tableLineData = new ArrayList<Object>();
					tableLineData.add(info.getType());
					if (info.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS) {
						tableLineData.add(info.getJobId());
						tableLineData.add(EndStatusMessage.typeToString(info.getValue()));
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add(info.getDescription());
						tableData.add(tableLineData);
					}
					else if (info.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE) {
						tableLineData.add(info.getJobId());
						tableLineData.add(info.getValue());
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add(info.getDescription());
						tableData.add(tableLineData);
					}
					else if (info.getType() == JudgmentObjectConstant.TYPE_TIME) {
						if (info.getTime() != null) {
							tableLineData.add("");
							tableLineData.add(new Date(info.getTime()));
							tableLineData.add("");
							tableLineData.add("");
							tableLineData.add("");
							tableLineData.add("");
							tableLineData.add(info.getDescription());
							tableData.add(tableLineData);
						} else {
						}
					}
					else if (info.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
						m_log.debug("reflectWaitRuleInfo_JobObjectInfo of line is  DELAY");
						if (info.getValue() != null) {
							tableLineData.add("");
							tableLineData.add(info.getStartMinute());
							tableLineData.add("");
							tableLineData.add("");
							tableLineData.add("");
							tableLineData.add("");
							tableLineData.add(info.getDescription());
							tableData.add(tableLineData);
						} else {
						}
					}
					else if (info.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
						if (info.getDecisionValue01() != null) {
							tableLineData.add("");
							tableLineData.add("");
							tableLineData.add(info.getDecisionValue01());
							tableLineData.add(info.getDecisionCondition());
							tableLineData.add(info.getDecisionValue02());
							tableLineData.add("");
							tableLineData.add(info.getDescription());
							tableData.add(tableLineData);
						} else {
						}
					}
					else if (info.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS) {
						tableLineData.add(info.getJobId());
						tableLineData.add(EndStatusMessage.typeToString(info.getValue()));
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add(info.getCrossSessionRange());
						tableLineData.add(info.getDescription());
						tableData.add(tableLineData);
					}
					else if (info.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
						tableLineData.add(info.getJobId());
						tableLineData.add(info.getValue());
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add("");
						tableLineData.add(info.getCrossSessionRange());
						tableLineData.add(info.getDescription());
						tableData.add(tableLineData);
					}
				}
				m_log.debug("reflectWaitRuleInfo_tableData.size() = " + tableData.size());
				m_viewer.setInput(tableData);
			}

			//??????????????????
			if (m_waitRule.getCondition() == ConditionTypeConstant.TYPE_AND) {
				m_andCondition.setSelection(true);
				m_orCondition.setSelection(false);
			} else {
				m_andCondition.setSelection(false);
				m_orCondition.setSelection(true);
			}

			//?????????????????????????????????????????? ??????
			m_endCondition.setSelection(m_waitRule.isEndCondition());

			//????????????
			setSelectEndStatus(m_endStatus, m_waitRule.getEndStatus());

			//?????????
			m_endValue.setText(String.valueOf(m_waitRule.getEndValue()));
		}

		//??????????????????????????????????????????
		if (m_endCondition.getSelection()) {
			m_endStatus.setEnabled(true);
			m_endValue.setEditable(true);
		} else {
			m_endStatus.setEnabled(false);
			m_endValue.setEditable(false);
		}
	}

	/**
	 * ????????????????????????????????????????????????
	 *
	 * @param start ???????????????????????????
	 */
	public void setWaitRuleInfo(JobWaitRuleInfo start) {
		m_waitRule = start;
	}

	/**
	 * ?????????????????????????????????????????????
	 *
	 * @return ???????????????????????????
	 */
	public JobWaitRuleInfo getWaitRuleInfo() {
		return m_waitRule;
	}

	public static JobObjectInfo array2JobObjectInfo(ArrayList<?> tableLineData) {
		Integer type = (Integer) tableLineData.get(GetWaitRuleTableDefine.JUDGMENT_OBJECT);
		JobObjectInfo info = new JobObjectInfo();
		info.setValue(0);
		info.setType(type);
		if (info.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS) {
			info.setJobId((String) tableLineData
					.get(GetWaitRuleTableDefine.JOB_ID));
			String value = (String) tableLineData
					.get(GetWaitRuleTableDefine.START_VALUE);
			info.setValue(EndStatusMessage.stringToType(value));
		}
		else if (info.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE) {
			info.setJobId((String) tableLineData
					.get(GetWaitRuleTableDefine.JOB_ID));
			Integer value = (Integer) tableLineData
					.get(GetWaitRuleTableDefine.START_VALUE);
			info.setValue(value);
		}
		else if (info.getType() == JudgmentObjectConstant.TYPE_TIME) {
			Date value = (Date) tableLineData
					.get(GetWaitRuleTableDefine.START_VALUE);
			info.setTime(value.getTime());
		}
		else if (info.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
			Integer startMinute = (Integer) tableLineData
					.get(GetWaitRuleTableDefine.START_VALUE);
			info.setStartMinute(startMinute);
		}
		else if (info.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
			info.setDecisionValue01((String) tableLineData
					.get(GetWaitRuleTableDefine.DECISION_VALUE_1));
			Integer condition = (Integer) tableLineData
					.get(GetWaitRuleTableDefine.DECISION_CONDITION);
			info.setDecisionCondition(condition);
			info.setDecisionValue02((String) tableLineData
					.get(GetWaitRuleTableDefine.DECISION_VALUE_2));
		}
		else if (info.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS) {
			info.setJobId((String) tableLineData
					.get(GetWaitRuleTableDefine.JOB_ID));
			String value = (String) tableLineData
					.get(GetWaitRuleTableDefine.START_VALUE);
			info.setValue(EndStatusMessage.stringToType(value));
			info.setCrossSessionRange((Integer)tableLineData.get(GetWaitRuleTableDefine.CROSS_SESSION_RANGE));
		}
		else if (info.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
			info.setJobId((String) tableLineData
					.get(GetWaitRuleTableDefine.JOB_ID));
			Integer value = (Integer) tableLineData
					.get(GetWaitRuleTableDefine.START_VALUE);
			info.setValue(value);
			info.setCrossSessionRange((Integer)tableLineData.get(GetWaitRuleTableDefine.CROSS_SESSION_RANGE));
		}
		String description = (String) tableLineData
				.get(GetWaitRuleTableDefine.DESCRIPTION);
		info.setDescription(description);
		return info;
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????????????????
	 *
	 * @return ????????????????????????
	 *
	 * @see com.clustercontrol.jobmanagement.bean.JobWaitRuleInfo
	 */
	public ValidateResult createWaitRuleInfo() {
		m_log.debug("createWaitRuleInfo");
		ValidateResult result = null;

		//????????????????????????????????????
		ArrayList<JobObjectInfo> list = new ArrayList<JobObjectInfo>();
		ArrayList<?> tableData = (ArrayList<?>) m_viewer.getInput();
		HashMap<String, Integer> map = new HashMap<String, Integer>();

		for (int i = 0; i < tableData.size(); i++) {
			ArrayList<?> tableLineData = (ArrayList<?>) tableData.get(i);
			JobObjectInfo info = array2JobObjectInfo(tableLineData);
			// ???????????????????????????????????????????????????????????????
			if (info.getType() == JudgmentObjectConstant.TYPE_JOB_END_STATUS || info.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS) {
				Integer checkValue = map.get(info.getJobId() + info.getType() + info.getValue());
				if (checkValue == null
						|| !checkValue.equals(info.getValue())) {
					list.add(info);
					map.put(info.getJobId() + info.getType() + info.getValue(), info.getValue());
				}
			}
			else if (info.getType() == JudgmentObjectConstant.TYPE_JOB_END_VALUE || info.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
				Integer checkValue = map.get(info.getJobId() + info.getType() + info.getValue());
				if (checkValue == null
						|| !checkValue.equals(info.getValue())) {
					list.add(info);
					map.put(info.getJobId() + info.getType() + info.getValue(), info.getValue());
				}
			}
			else if (info.getType() == JudgmentObjectConstant.TYPE_TIME) {
				if (map.get("TIME") == null) {
					list.add(info);
					map.put("TIME", 1);
				}
			}
			else if (info.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
				m_log.debug("info.getType="  + info.getType());
				m_log.debug("info.getStartMinute="  + info.getStartMinute());
				if (map.get(info.getType().toString()) == null) {
					list.add(info);
					map.put(info.getType().toString(), info.getValue());
				}
			}
			else if (info.getType() == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
				Integer checkValue = map.get(info.getType() + info.getDecisionValue01() + info.getDecisionCondition() + info.getDecisionValue02());
				if (checkValue == null
						|| !checkValue.equals(info.getValue())) {
					list.add(info);
					map.put(info.getType() + info.getDecisionValue01() + info.getDecisionCondition() + info.getDecisionValue02(), info.getValue());
				}
			}
		}
		List<JobObjectInfo> jobObjectInfoList = m_waitRule.getObject();
		jobObjectInfoList.clear();
		jobObjectInfoList.addAll(list);

		//??????????????????
		if (m_andCondition.getSelection()) {
			m_waitRule.setCondition(ConditionTypeConstant.TYPE_AND);
		} else {
			m_waitRule.setCondition(ConditionTypeConstant.TYPE_OR);
		}

		//?????????????????????????????????????????? ??????
		m_waitRule.setEndCondition(m_endCondition.getSelection());

		//????????????????????????
		try {
			m_waitRule.setEndStatus(getSelectEndStatus(m_endStatus));
			m_waitRule.setEndValue(Integer.parseInt(m_endValue.getText()));
		} catch (NumberFormatException e) {
			if (m_waitRule.isEndCondition().booleanValue()) {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.21"));
				return result;
			}
		}

		return null;
	}

	/**
	 * ???????????????????????????????????????
	 *
	 * @param selectItem ??????????????????
	 */
	public void setSelectItem(ArrayList<Object> selectItem) {
		m_selectItem = selectItem;
	}

	public void setJobTreeItem(JobTreeItem jobTreeItem) {
		m_jobTreeItem = jobTreeItem;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 *
	 */
	private void setSelectEndStatus(Combo combo, int status) {
		String select = "";

		select = EndStatusMessage.typeToString(status);

		combo.select(0);
		for (int i = 0; i < combo.getItemCount(); i++) {
			if (select.equals(combo.getItem(i))) {
				combo.select(i);
				break;
			}
		}
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????????????????
	 *
	 */
	private int getSelectEndStatus(Combo combo) {
		String select = combo.getText();
		return EndStatusMessage.stringToType(select);
	}

	/**
	 * ??????????????????????????????????????????????????????
	 */
	@Override
	public void setEnabled(boolean enabled) {
		m_andCondition.setEnabled(enabled);
		m_orCondition.setEnabled(enabled);
		m_endCondition.setEnabled(enabled);
		m_endStatus.setEnabled(m_endCondition.getSelection() && enabled);
		m_endValue.setEditable(m_endCondition.getSelection() && enabled);
		m_createCondition.setEnabled(enabled);
		m_modifyCondition.setEnabled(enabled);
		m_deleteCondition.setEnabled(enabled);
	}
}
