/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.utility.traputil.ui.views.commands.ImportCommand;
import com.clustercontrol.accesscontrol.util.ObjectBean;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.monitor.composite.MonitorListComposite;
import com.clustercontrol.monitor.composite.action.MonitorListSelectionChangedListener;
import com.clustercontrol.monitor.run.action.GetMonitorListTableDefine;
import com.clustercontrol.monitor.view.action.CollectorDisableAction;
import com.clustercontrol.monitor.view.action.CollectorEnableAction;
import com.clustercontrol.monitor.view.action.MonitorCopyAction;
import com.clustercontrol.monitor.view.action.MonitorDeleteAction;
import com.clustercontrol.monitor.view.action.MonitorDisableAction;
import com.clustercontrol.monitor.view.action.MonitorEnableAction;
import com.clustercontrol.monitor.view.action.MonitorFilterAction;
import com.clustercontrol.monitor.view.action.MonitorModifyAction;
import com.clustercontrol.monitor.view.action.MonitorRefreshAction;
import com.clustercontrol.monitor.view.action.MonitorSummaryAction;
import com.clustercontrol.monitor.view.action.ObjectPrivilegeMonitorListAction;
import com.clustercontrol.view.CommonViewPart;
import com.clustercontrol.view.ObjectPrivilegeTargetListView;

/**
 * ??????[??????]??????????????????<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MonitorListView extends CommonViewPart implements ObjectPrivilegeTargetListView {

	/** ??????[??????]?????????ID */
	public static final String ID = MonitorListView.class.getName();

	/** ???????????????????????????????????? */
	private MonitorListComposite composite = null;

	/** ???????????? */
	private Property condition = null;

	/** ????????????????????? */
	private int rowNum = 0;

	/** ??????????????????ID */
	private String selectMonitorTypeId = null;

	/**
	 * ?????????????????????
	 */
	public MonitorListView() {
		super();
	}

	protected String getViewName() {
		return this.getClass().getName();
	}

	/**
	 * ViewPart????????????????????????????????????<BR>
	 *
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		GridLayout layout = new GridLayout(1, true);
		parent.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		composite = new MonitorListComposite(parent, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, composite);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.composite.setLayoutData(gridData);

		//????????????????????????????????????
		createContextMenu();

		// ??????????????????????????????????????????????????????????????????
		this.composite.getTableViewer().addSelectionChangedListener(
				new MonitorListSelectionChangedListener());

		this.update();
	}

	/**
	 * ???????????????????????????????????????????????????
	 *
	 * @see org.eclipse.jface.action.MenuManager
	 * @see org.eclipse.swt.widgets.Menu
	 */
	private void createContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);

		Menu menu = menuManager.createContextMenu(composite.getTable());
		WidgetTestUtil.setTestId(this, null, menu);
		composite.getTable().setMenu(menu);
		getSite().registerContextMenu( menuManager, this.composite.getTableViewer() );
	}

	/**
	 * ??????????????????????????????????????????
	 *
	 * @return ????????????????????????
	 */
	public Composite getListComposite() {
		return this.composite;
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????
	 * <p>
	 *
	 * condition???null????????????????????????????????????????????????
	 *
	 * @param condition
	 *            ????????????
	 */
	public void update(Property condition) {
		this.condition = condition;

		this.update();
	}

	/**
	 * ??????????????????????????????
	 * <p>
	 *
	 * ???????????????????????????????????????????????????????????????????????????????????????????????????????????? ??????????????? <br>
	 * ???????????????????????????????????????????????????????????????????????????????????????
	 */
	@Override
	public void update() {
		this.composite.update(this.condition);
	}


	/**
	 * ???????????????????????????????????????
	 * @return rowNum
	 */
	public int getSelectedNum(){
		return this.rowNum;
	}

	/**
	 * ?????????????????????????????????ID??????????????????
	 * @return selectMonitorTypeId
	 */
	public String getSelectMonitorTypeId(){
		return this.selectMonitorTypeId;
	}

	/**
	 * ????????????????????????????????????/???????????????????????????
	 *
	 * @param num ?????????????????????
	 * @param selection ???????????????????????????????????????????????????????????????
	 */
	public void setEnabledAction(int num, String selectMonitorTypeId, ISelection selection) {
		this.rowNum = num;
		this.selectMonitorTypeId = selectMonitorTypeId;

		//????????????????????????????????????/???????????????
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService( ICommandService.class );
		if( null != service ){
			service.refreshElements(ObjectPrivilegeMonitorListAction.ID, null);
			service.refreshElements(MonitorCopyAction.ID, null);
			service.refreshElements(MonitorDeleteAction.ID, null);
			service.refreshElements(MonitorModifyAction.ID, null);
			service.refreshElements(MonitorDisableAction.ID, null);
			service.refreshElements(MonitorEnableAction.ID, null);
			service.refreshElements(CollectorDisableAction.ID, null);
			service.refreshElements(CollectorEnableAction.ID, null);
			service.refreshElements(MonitorRefreshAction.ID, null);
			service.refreshElements(MonitorFilterAction.ID, null);
			service.refreshElements(MonitorSummaryAction.ID, null);
			service.refreshElements(ImportCommand.ID, null);

			// Update ToolBar after elements refreshed
			// WARN : Both ToolBarManager must be updated after updateActionBars(), otherwise icon won't change.
			getViewSite().getActionBars().updateActionBars();
			getViewSite().getActionBars().getToolBarManager().update(false);
		}
	}

	@Override
	public List<ObjectBean> getSelectedObjectBeans() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();
		Object [] objs = selection.toArray();

		String managerName = null;
		String objectType = HinemosModuleConstant.MONITOR;
		String objectId = null;
		List<ObjectBean> objectBeans = new ArrayList<ObjectBean>();
		for (Object obj : objs) {
			managerName = (String) ((List<?>)obj).get(GetMonitorListTableDefine.MANAGER_NAME);
			objectId = (String) ((List<?>)obj).get(GetMonitorListTableDefine.MONITOR_ID);
			ObjectBean objectBean = new ObjectBean(managerName, objectType, objectId);
			objectBeans.add(objectBean);
		}
		return objectBeans;
	}

	@Override
	public String getSelectedOwnerRoleId() {
		StructuredSelection selection = (StructuredSelection) this.composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String id = null;
		if (list != null) {
			id = (String) list.get(GetMonitorListTableDefine.OWNER_ROLE);
		}
		return id;
	}
}
