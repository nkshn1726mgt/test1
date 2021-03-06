/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.composite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.nodemap.util.RelationViewController;
import com.clustercontrol.nodemap.view.NodeMapView;
import com.clustercontrol.nodemap.view.ScopeTreeView;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.composite.ScopeTreeSearchBarComposite;
import com.clustercontrol.repository.util.ScopePropertyUtil;
import com.clustercontrol.util.FacilityTreeCache;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.FacilityTreeContentProvider;
import com.clustercontrol.viewer.FacilityTreeLabelProvider;
import com.clustercontrol.viewer.FacilityTreeViewerSorter;
import com.clustercontrol.ws.repository.FacilityTreeItem;

/**
 * ??????????????????????????????????????????????????????
 * @since 1.0.0
 */
public class ScopeComposite extends FacilityTreeComposite {

	// ??????
	private static Log m_log = LogFactory.getLog( ScopeComposite.class );

	/**
	 * ??????5?????????????????????????????????????????????????????????????????????
	 * ????????????????????????????????????????????????
	 * 
	 * @param parent
	 * @param style
	 * @param scopeOnly
	 * @param unregistered
	 * @param internal
	 */
	public ScopeComposite(Composite parent, int style,
			boolean scopeOnly ,
			boolean unregistered,
			boolean internal) {
		super(parent,style, null, null,scopeOnly,unregistered,internal);
	}

	@Override
	/**
	 * ???????????????????????????????????????
	 */
	protected void createContents() {
		FacilityTreeCache.addComposite(this);
		
		// ??????????????????????????????????????????
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);
		
		// Add search bar
		Composite compSearch = new ScopeTreeSearchBarComposite(this, SWT.NONE, true);
		WidgetTestUtil.setTestId(this, "search", compSearch);
		compSearch.setLayoutData( new GridData(GridData.GRAB_HORIZONTAL) );
		
		// ???????????????
		Tree tree = new Tree(this, SWT.SINGLE | SWT.BORDER);

		// ??????????????????????????????????????????
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.FILL;
		layoutData.verticalAlignment = GridData.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		tree.setLayoutData(layoutData);

		// ???????????????????????????
		this.treeViewer = new TreeViewer(tree);

		// ???????????????????????????
		this.treeViewer.setContentProvider(new FacilityTreeContentProvider());
		this.treeViewer.setLabelProvider(new FacilityTreeLabelProvider());
		this.treeViewer.setSorter(new FacilityTreeViewerSorter());

		// ??????????????????????????????????????????
		this.treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();

				/*
				 * topic??????????????????????????????????????????null check???????????????
				 */
				selectItem = (FacilityTreeItem) selection.getFirstElement();
				if (selectItem == null) {
					m_log.warn("selectionChanged(), selectionChanged selectItem is null");
					return;
				}

				subScopeNumber = selectItem.getChildren().size();

				try{
					// ????????????????????????????????????????????????????????????
					IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
					if (page == null) {
						m_log.debug("ScopeComposite page is null");
						return;
					}
					IViewReference viewReference = page.findViewReference(ScopeTreeView.ID);
					if (viewReference == null) {
						m_log.debug("ScopeComposite viewReference is null");
						return;
					}
					IViewPart viewPart = viewReference.getView(false);
					if (viewPart == null) {
						m_log.debug("ScopeComposite viewPart is null");
						return;
					}
					if (viewPart instanceof ScopeTreeView) {
						ScopeTreeView view = (ScopeTreeView) viewPart;
						// ????????????????????????????????????/???????????????
						view.setEnabledAction();
					}
				} catch (Exception e) {
					m_log.warn("createContents(), " + e.getMessage(), e);
				}
			}
		});

		// ?????????????????????????????????????????????????????????????????????????????????
		this.treeViewer.addDoubleClickListener(
				new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						if (event.getSelection() instanceof TreeSelection) {
							TreeSelection treeSelection = (TreeSelection)event.getSelection();
							m_log.debug("first " + treeSelection.getFirstElement());
							if (treeSelection.getFirstElement() instanceof FacilityTreeItem) {
								FacilityTreeItem item = (FacilityTreeItem) treeSelection.getFirstElement();
								if (FacilityConstant.TYPE_NODE != item.getData().getFacilityType() &&
										FacilityConstant.TYPE_COMPOSITE != item.getData().getFacilityType()) {
									String facilityId = item.getData().getFacilityId();
									if (item.getData().getFacilityType() == FacilityConstant.TYPE_MANAGER) {
										facilityId = ReservedFacilityIdConstant.ROOT_SCOPE;
									}
									String managerName = ScopePropertyUtil.getManager(item).getData().getFacilityId();
									RelationViewController.createNewView(managerName, facilityId, NodeMapView.class);
								}
							}
						}
					}
				});

		//Drag&Drop
		final ScopeComposite scopeComposite = this;
		Transfer[] transferTypes = new Transfer[] {TextTransfer.getInstance()};
		treeViewer.addDragSupport(DND.DROP_MOVE, transferTypes, new DragSourceListener() {
			@Override
			public void dragStart(DragSourceEvent event) {
				if (selectItem.getData().getFacilityType() != FacilityConstant.TYPE_NODE) {
					event.doit = false;
				}
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = selectItem.getData().getFacilityId();
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				m_log.debug("Drag finished.");
				m_log.debug("Drag operation: " + event.detail);
				if (event.detail == DND.DROP_MOVE) {
					scopeComposite.update();
				}
			}
		});
		
		
		// ??????????????????
		this.update();
	}
}
