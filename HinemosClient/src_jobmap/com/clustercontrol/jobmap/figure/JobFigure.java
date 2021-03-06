/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.figure;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ScalableLayeredPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.bean.EndStatusConstant;
import com.clustercontrol.bean.EndStatusImageConstant;
import com.clustercontrol.bean.EndStatusMessage;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.bean.StatusMessage;
import com.clustercontrol.jobmanagement.bean.JobConstant;
import com.clustercontrol.jobmanagement.bean.JudgmentObjectConstant;
import com.clustercontrol.jobmanagement.bean.StatusImageConstant;
import com.clustercontrol.jobmanagement.util.JobEditState;
import com.clustercontrol.jobmanagement.util.JobEditStateUtil;
import com.clustercontrol.jobmanagement.util.JobTreeItemUtil;
import com.clustercontrol.jobmanagement.util.JobmapIconImageUtil;
import com.clustercontrol.jobmap.composite.JobMapComposite;
import com.clustercontrol.jobmap.editpart.MapViewController;
import com.clustercontrol.jobmap.util.JobmapImageCacheUtil;
import com.clustercontrol.jobmap.view.JobMapEditorView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.ws.jobmanagement.IconFileNotFound_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidRole_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidSetting_Exception;
import com.clustercontrol.ws.jobmanagement.InvalidUserPass_Exception;
import com.clustercontrol.ws.jobmanagement.JobDetailInfo;
import com.clustercontrol.ws.jobmanagement.JobObjectInfo;
import com.clustercontrol.ws.jobmanagement.JobTreeItem;
import com.clustercontrol.ws.jobmanagement.JobWaitRuleInfo;
import com.clustercontrol.ws.jobmanagement.JobmapIconImage;

/**
 * ????????????(?????????)??????????????????
 * @since 1.0.0
 */
public class JobFigure extends Figure implements ISelection {

	// ??????
	private static Log m_log = LogFactory.getLog( JobFigure.class );

	private JobTreeItem m_jobTreeItem;
	public static final int textHeight = 35;
	public static final int jobnetBorder = 10;
	public static final int lineWidth = 2;
	private final static String fontStr = "MS UI Gothic";
	// ????????????
	private final static Font jobNetFont = new Font(
			Display.getCurrent(), fontStr, 10,  SWT.BOLD);

	// 3????????????????????????????????????
	// layerStack?????????layerToolbar???????????????layerXY???
	private ScalableLayeredPane m_layerXY; //??????(???????????????????????????)

	private GRoundedRectangle m_background;

	private Layer m_baseLayer = null;

	private final Rectangle zeroRectangle;

	private final JobMapEditorView m_editorView;

	// ?????????????????????????????????????????????
	private static Image waitImage;
	// ???????????????????????????????????????????????????????????????
	private static Image waitDoubleImage;
	// ??????????????????????????????????????????????????????
	private static Image waitCrossingJobImage;

	private ImageFigure m_collapseExpandImageFigure;

	private String m_collapseExpandImageId;

	private Point m_position;

	private boolean m_collapse;

	private Dimension m_size = new Dimension();

	private JobMapComposite m_jobMapComposite;

	private MapViewController m_controller;

	private String m_managerName;

	private ImageFigure m_iconImageFigure;

	private JobmapImageCacheUtil m_iconCache;

	public JobFigure(String managerName, JobTreeItem item, JobMapEditorView editorView, JobMapComposite jobMapComposite, boolean collapse){
		this.setFocusTraversable(true);
		this.setRequestFocusEnabled(true);
		// ??????????????????????????????
		this.setBackgroundColor(ColorConstantsWrapper.white());
		this.m_jobTreeItem = item;
		this.m_editorView = editorView;
		this.m_jobMapComposite = jobMapComposite;
		this.m_controller = new MapViewController(jobMapComposite);
		this.m_collapse = collapse;
		this.zeroRectangle = new Rectangle(new Point(0, 0), new Dimension(-1, -1));
		this.m_managerName = managerName;

		// ?????????????????????
		this.m_controller.applySetting();
		
		//????????????????????????????????????
		m_iconCache = JobmapImageCacheUtil.getInstance();

		// ?????????????????????????????????
		updateIconImage();
	}

	public void setJob(JobTreeItem item){
		m_jobTreeItem = item;
	}

	/**
	 * ?????????????????????????????????
	 */
	public void updateIconImage() {

		if (isIconImageJob()) {
			JobmapIconImage jobmapIconImage = null;
			if (this.m_jobTreeItem.getData().getIconId() != null
					&& !this.m_jobTreeItem.getData().getIconId().equals("")) {
				try {
					jobmapIconImage
						= m_iconCache.getJobmapIconImage(this.m_managerName, this.m_jobTreeItem.getData().getIconId());
				} catch (IconFileNotFound_Exception e) {
					jobmapIconImage = null;
				} catch (InvalidRole_Exception e) {
					// ????????????????????????????????????????????????????????????????????????
					MessageDialog.openInformation(
							null,
							Messages.getString("message"),
							Messages.getString("message.accesscontrol.16"));
					return;
				} catch (InvalidUserPass_Exception e) {
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
					return;
				} catch (InvalidSetting_Exception e) {
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.job.140") + " " + HinemosMessage.replace(e.getMessage()));
					return;
				} catch (Exception e) {
					m_log.warn("action(), " + HinemosMessage.replace(e.getMessage()), e);
					MessageDialog.openError(
							null,
							Messages.getString("failed"),
							Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
					return;
				}
			}
			
			// iconId????????????????????????????????????iconId????????????????????????????????????????????????????????????????????????????????????
			if (jobmapIconImage == null) {
				if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_JOBNET
						|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_REFERJOBNET) {
					jobmapIconImage 
						= m_iconCache.getJobmapIconImageDefaultJobnet(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_APPROVALJOB) {
					jobmapIconImage = m_iconCache.getJobmapIconImageDefaultApproval(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_MONITORJOB) {
					jobmapIconImage = m_iconCache.getJobmapIconImageDefaultMonitor(this.m_managerName);
				} else if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_FILEJOB) {
					jobmapIconImage = m_iconCache.getJobmapIconImageDefaultFile(this.m_managerName);
				} else {
					jobmapIconImage 
						= m_iconCache.getJobmapIconImageDefaultJob(this.m_managerName);
				}
			}
			if (jobmapIconImage == null) {
				String iconId = "";
				if (this.m_jobTreeItem.getData().getIconId() == null
						|| this.m_jobTreeItem.getData().getIconId().equals("")) {
					if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_JOBNET
							|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_REFERJOBNET) {
						iconId = m_iconCache.getJobmapIconIdDefaultJobnet(this.m_managerName);
					} else if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_APPROVALJOB) {
						iconId = m_iconCache.getJobmapIconIdDefaultApproval(this.m_managerName);
					} else if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_MONITORJOB) {
						iconId = m_iconCache.getJobmapIconIdDefaultMonitor(this.m_managerName);
					} else if (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_FILEJOB) {
						iconId = m_iconCache.getJobmapIconIdDefaultFile(this.m_managerName);
					} else {
						iconId
							= m_iconCache.getJobmapIconIdDefaultJob(this.m_managerName);
					}
				} else {
					iconId = this.m_jobTreeItem.getData().getIconId();
				}
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.job.148", new String[]{iconId}));
				return;
			}
			m_iconImageFigure = new ImageFigure(m_iconCache.loadGraphicImage(jobmapIconImage));
		}
	}

	/**
	 * ??????????????????????????????
	 */
	public static int getDepth(JobTreeItem item) {
		int ret = 0;
		if (item.getChildren() == null || item.getChildren().size() == 0) {
			return ret;
		}
		for (JobTreeItem child : item.getChildren()) {
			int childDepth = getDepth(child);
			if (ret < childDepth) {
				ret = childDepth;
			}
		}
		ret ++;
		return ret;
	}

	/**
	 * ????????????????????????
	 */
	public void draw() {
		// ??????????????????????????????????????????
		this.removeAll();

		// ??????????????????????????????
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		this.setLayoutManager(layout);

		// ?????????????????????
		if (isIconImageJob()) {
			// ?????????????????????
			m_baseLayer = new Layer();
			m_baseLayer.setLayoutManager(new FlowLayout(false));
			m_baseLayer.setSize(JobmapIconImageUtil.ICON_WIDTH + 8, JobmapIconImageUtil.ICON_HEIGHT + SizeConstant.SIZE_TEXT_HEIGHT + 8);

			//layerStack??????????????????????????????????????????????????????
			Layer layerStack = new Layer();
			layerStack.setLayoutManager(new StackLayout());
			layerStack.setPreferredSize(JobmapIconImageUtil.ICON_WIDTH + 8, JobmapIconImageUtil.ICON_HEIGHT + 8);

			m_background = new GRoundedRectangle();
			m_background.setSize(layerStack.getSize());
			m_size.setSize(layerStack.getSize());
			layerStack.add(m_background);
			// ?????????????????????
			layerStack.add(m_iconImageFigure);
			// ??????????????????????????????????????????
			if (m_jobTreeItem.getData().getType() == JobConstant.TYPE_REFERJOB
					|| m_jobTreeItem.getData().getType() == JobConstant.TYPE_REFERJOBNET) {
				ImageFigure referImageFigure = new ImageFigure(ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_REFER)
						, PositionConstants.SOUTH_WEST);
				layerStack.add(referImageFigure);
			}
			// ???????????????????????????
			else {
				ImageFigure queueImage = getQueueIcon();
				if (queueImage != null) {
					queueImage.setAlignment(PositionConstants.SOUTH_WEST);
					layerStack.add(queueImage);
				}
			}
			// ??????????????????
			if (m_jobTreeItem.getData().getType() == JobConstant.TYPE_JOBNET) {
				Layer layerTitle = new Layer();
				layerTitle.setLayoutManager(new BorderLayout());

				m_collapseExpandImageFigure = new ImageFigure(ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_EXPAND), 
						PositionConstants.NORTH_EAST);
				layerTitle.add(m_collapseExpandImageFigure, BorderLayout.RIGHT);
				layerStack.add(layerTitle);
				m_collapseExpandImageFigure.addMouseListener(new MouseListener() {
					@Override
					public void mouseDoubleClicked(MouseEvent arg0) {
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
						collapseOrExpand(ClusterControlPlugin.IMG_COLLAPSE.equals(m_collapseExpandImageId));
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {
					}
				});
			}
			m_baseLayer.add(layerStack);

			// ???????????????????????????
			ImageFigure imageFigure = getWaitingIcon();
			if (imageFigure != null) {
				m_layerXY = new ScalableLayeredPane();
				m_layerXY.setLayoutManager(new XYLayout());
				m_layerXY.add(imageFigure);
				m_layerXY.setConstraint(imageFigure, zeroRectangle);
				layerStack.add(m_layerXY);
			}

			// ?????????ID???????????????
			Label label = new Label();
			if (this.m_controller.isLabelingId()) {
				label.setText(m_jobTreeItem.getData().getId());
			} else {
				label.setText(m_jobTreeItem.getData().getName());
			}
			label.setLabelAlignment(Label.CENTER);
			label.setSize(JobmapIconImageUtil.ICON_WIDTH, SizeConstant.SIZE_TEXT_HEIGHT);
			label.setBorder(new MarginBorder(0, 4, 0, 4));
			
			m_baseLayer.add(label);


			// ????????????
			setBgColor();

			// ???????????????????????????
			this.setToolTip(getTooltip());

			this.add(m_baseLayer);
			this.setConstraint(m_baseLayer, zeroRectangle);
			this.setMaximumSize(m_baseLayer.getPreferredSize());
		} else {
			// ?????????????????????????????????????????????????????????1??????
			m_layerXY = new ScalableLayeredPane();
			m_layerXY.setLayoutManager(new XYLayout());

			// ???????????????
			m_background = new GRoundedRectangle();
			m_layerXY.add(m_background);
			
			Layer layerTitle = new Layer();
			BorderLayout borderLayout = new BorderLayout();
			layerTitle.setLayoutManager(borderLayout);
			
			// ?????????ID
			Label label = new Label();
			if (this.m_controller.isLabelingId()) {
				label.setText(m_jobTreeItem.getData().getId());
			} else {
				label.setText(m_jobTreeItem.getData().getName());
			}
			label.setForegroundColor(JobMapColor.darkgray);
			label.setFont(jobNetFont);
			label.setSize(this.m_controller.getTextWidth() - 8, textHeight + 8);
			label.setBorder(new MarginBorder(0, 4, 0, 4));
			
			layerTitle.add(label, BorderLayout.CENTER);
			
			if (m_collapse) {
				m_collapseExpandImageId = ClusterControlPlugin.IMG_EXPAND;
			} else {
				m_collapseExpandImageId = ClusterControlPlugin.IMG_COLLAPSE;
			}
			m_collapseExpandImageFigure = new ImageFigure(ClusterControlPlugin.getDefault().getImageRegistry().get(m_collapseExpandImageId));
			layerTitle.add(m_collapseExpandImageFigure, BorderLayout.RIGHT);
			m_collapseExpandImageFigure.addMouseListener(new MouseListener() {

				@Override
				public void mouseDoubleClicked(MouseEvent arg0) {
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					collapseOrExpand(ClusterControlPlugin.IMG_COLLAPSE.equals(m_collapseExpandImageId));
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
				}
			});

			m_background.add(layerTitle);

			// ???????????????????????????
			ImageFigure waitingIcon = getWaitingIcon();
			if (waitingIcon != null) {
				m_log.debug("wait icon");
				m_layerXY.add(waitingIcon);
				m_layerXY.setConstraint(waitingIcon, zeroRectangle);
			}

			// ???????????????????????????????????????
			ImageFigure queueIcon = getQueueIcon();
			if (queueIcon != null) {
				// ??????????????????????????????????????????????????????
				Point leftTop = (waitingIcon == null) ? new Point(0, 0) : new Point(16, 0);
				m_layerXY.add(queueIcon);
				m_layerXY.setConstraint(queueIcon, new Rectangle(leftTop, new Dimension(-1, -1)));
			}
			
			// ????????????
			setBgColor();

			// ???????????????????????????
			this.setToolTip(getTooltip());

			this.add(m_layerXY);
		}
	}

	private void collapseOrExpand(boolean collapse) {
		if (collapse) {
			m_jobMapComposite.addCollapseItem(m_jobTreeItem);
		} else {
			m_jobMapComposite.removeCollapseItem(m_jobTreeItem);
		}
		m_jobMapComposite.update();
	}

	private boolean isIconImageJob() {
		return (this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_JOBNET
				&& this.m_collapse)
				|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_REFERJOBNET
				|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_JOB
				|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_FILEJOB
				|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_REFERJOB
				|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_APPROVALJOB
				|| this.m_jobTreeItem.getData().getType() == JobConstant.TYPE_MONITORJOB;
	}

	private ImageFigure getWaitingIcon() {
		JobWaitRuleInfo waitRule = m_jobTreeItem.getData().getWaitRule();
		if (waitRule == null) {
			return null;
		}
		List<JobObjectInfo> list = waitRule.getObject();
		if (list == null) {
			return null;
		}
		boolean isTimeWaiting =false;
		boolean isCrossingJobWaiting =false;
		Date date = null;
		Integer minute = null;
		ArrayList<JobObjectInfo> crossingJobList = new ArrayList<JobObjectInfo>();
		//???????????? ??????
		for (JobObjectInfo jobObjectInfo : list) {
			if (jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_TIME) {
				if (jobObjectInfo.getTime() != null) {
					date = new Date(jobObjectInfo.getTime());
					isTimeWaiting = true;
				}
			} else if (jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_START_MINUTE) {
				if (jobObjectInfo.getStartMinute() != null) {
					minute = jobObjectInfo.getStartMinute();
					isTimeWaiting = true;
				}
			} else if (jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS) {
				if (jobObjectInfo.getValue() != null) {
					crossingJobList.add(jobObjectInfo);
					isCrossingJobWaiting = true;
				}
			} else if (jobObjectInfo.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_VALUE) {
				if (jobObjectInfo.getValue() != null) {
					crossingJobList.add(jobObjectInfo);
					isCrossingJobWaiting = true;
				}
			}
		}
		if (isTimeWaiting ==false  && isCrossingJobWaiting == false) {
			return null;
		}
		ImageFigure waitImageFigure ;
		//??????????????????
		if( isTimeWaiting && isCrossingJobWaiting){
			// ??????
			if (waitDoubleImage == null) {
				waitDoubleImage = ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_WAIT_DOUBLE);
			}
			waitImageFigure = new ImageFigure(waitDoubleImage);
		}else if(isTimeWaiting) {
			// ????????????
			if (waitImage == null) {
				waitImage = ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_WAIT);
			}
			waitImageFigure = new ImageFigure(waitImage);
		}else{
			// ?????????????????????
			if (waitCrossingJobImage == null) {
				waitCrossingJobImage = ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_WAIT_CROSS_JOB);
			}
			waitImageFigure = new ImageFigure(waitCrossingJobImage);
		}
		//??????????????????????????????????????????RAP???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		ArrayList<String> messageArray = new ArrayList<String>();
		if (date != null) {
			messageArray.add(Messages.getString("timestamp") + ":" + TimeStringConverter.formatTime(date) + "   ");
		}
		if (minute != null) {
			messageArray.add((Messages.getString("time.after.session.start") + ":" + minute) + "   ");
		}
		for ( JobObjectInfo targetWait :  crossingJobList){
			StringBuilder message = new StringBuilder() ;
			//?????????????????????????????????
			message.append(Messages.getString("wait.rule.cross.session")+ " " + Messages.getString("wait.rule") +":"+ "   ");
			//??????????????????????????????????????????
			message.append( "\n" + targetWait.getJobId() +"->" + m_jobTreeItem.getData().getId());
			//??????????????????????????????
			if (targetWait.getType() == JudgmentObjectConstant.TYPE_CROSS_SESSION_JOB_END_STATUS) {
				message.append("\n" + Messages.getString("end.status")  + "," + EndStatusMessage.typeToString(targetWait.getValue()));
			}
			else {
				message.append("\n" + Messages.getString("end.value")  + "," + targetWait.getValue());
			}
			//?????????????????????????????????
			message.append("\n" +  Messages.getString("wait.rule.cross.session.range") +","+ targetWait.getCrossSessionRange()+ "   ");
			messageArray.add(message.toString());
		}
		//???????????????????????????????????????
		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));
		for (String addMessage : messageArray){
			Panel subPanel = new Panel();
			subPanel.setLayoutManager(new FlowLayout(true));
			subPanel.add(new Label(addMessage));
			tooltip.add(subPanel);
		}
		waitImageFigure.setToolTip(tooltip);
		return waitImageFigure;
	}

	private ImageFigure getQueueIcon() {
		JobWaitRuleInfo waitRule = m_jobTreeItem.getData().getWaitRule();
		if (waitRule == null) return null;

		Boolean queueFlg = waitRule.isQueueFlg();
		if (queueFlg == null || !queueFlg.booleanValue()) return null;

		// ????????????????????????
		ImageFigure figure = new ImageFigure(
				ClusterControlPlugin.getDefault().getImageRegistry().get(ClusterControlPlugin.IMG_QUEUE));

		// ??????????????????
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(Messages.get("jobqueue.id") + ":" + waitRule.getQueueId());

		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));
		for (String message : messages){
			Panel subPanel = new Panel();
			subPanel.setLayoutManager(new FlowLayout(true));
			subPanel.add(new Label(message));
			tooltip.add(subPanel);
		}
		figure.setToolTip(tooltip);

		return figure;
	}
	
	public Dimension getBackgroundSize() {
		if (isIconImageJob()) {
			return m_baseLayer.getSize();
		} else {
			return m_background.getSize();
		}
	}
	
	public void adjustBackgroundSize() {
		if (isIconImageJob()) {
			return;
		}

		m_layerXY.setConstraint(m_background, zeroRectangle);
		calculateSize();
		m_background.setSize(m_size.width, m_size.height);
		if (!m_background.getChildren().isEmpty()) {
			//??????????????????????????????
			((Layer)m_background.getChildren().get(0)).setSize(m_size.width - 6, textHeight);
		}
	}
	
	private void calculateSize() {
		if (m_collapse) {
			m_size.setSize(this.m_controller.getTextWidth(), textHeight);
			return;
		}
		
		int width = 0;
		int height = 0;
		for (Object child : m_layerXY.getChildren()) {
			if (!(child instanceof JobFigure)) {
				continue;
			}

			JobFigure childJobFigure = (JobFigure) child;
			Dimension childJobFigureSize = childJobFigure.getBackgroundSize();
			Point childJobFigurePos = childJobFigure.getPosition();
			width = Math.max(width, childJobFigurePos.x + childJobFigureSize.width);
			height = Math.max(height, childJobFigurePos.y + childJobFigureSize.height);
		}
		
		if (height == 0) {
			height = textHeight;
		} else {
			height +=  jobnetBorder;
		}

		if (width < this.m_controller.getTextWidth()) {
			width = this.m_controller.getTextWidth();
		} else {
			width +=  jobnetBorder;
		}
		
		m_size.setSize(width, height);
	}
	
	public Point getPosition() {
		return m_position;
	}

	public void setPosition(Point position) {
		m_position = position;
	}
	
	public void setBgColor(){
		if(m_background == null) {
			m_log.debug("setPriority : m_backGround is null");
			return;
		}

		Color backColor = null;
		Color foreColor = null;
		JobDetailInfo detail = m_jobTreeItem.getDetail();
		Integer endStatus = null;
		Integer status = null;
		if (detail != null) {
			endStatus = detail.getEndStatus();
			status = detail.getStatus();
		}
		backColor = JobMapColor.lightgray;
		if (endStatus != null) {
			switch (endStatus) {
			case EndStatusConstant.TYPE_NORMAL:
				backColor = JobMapColor.green;
				break;
			case EndStatusConstant.TYPE_ABNORMAL:
				backColor = JobMapColor.red;
				break;
			case EndStatusConstant.TYPE_WARNING:
				backColor = JobMapColor.yellow;
				break;
			default:
				break;
			}
		} else if (status != null) {
			switch (status) {
			case StatusConstant.TYPE_RESERVING: // ??????
				backColor = JobMapColor.yellow;
				break;
			case StatusConstant.TYPE_SKIP: // ????????????
				backColor = JobMapColor.yellow;
				break;
			case StatusConstant.TYPE_RUNNING: // ?????????
				backColor = JobMapColor.blue;
				break;
			case StatusConstant.TYPE_STOPPING: // ???????????????
				backColor = JobMapColor.blue;
				break;
			case StatusConstant.TYPE_SUSPEND: // ??????
				backColor = JobMapColor.yellow;
				break;
			case StatusConstant.TYPE_STOP: // ??????????????????
				backColor = JobMapColor.red;
				break;
			case StatusConstant.TYPE_MODIFIED: // ????????????
				backColor = JobMapColor.green;
				break;
			case StatusConstant.TYPE_END: // ??????
				backColor = JobMapColor.green;
				break;
			case StatusConstant.TYPE_ERROR:
				backColor = JobMapColor.red;
				break;
			case StatusConstant.TYPE_WAIT:
				backColor = JobMapColor.lightgray;
				break;
			case StatusConstant.TYPE_RUNNING_QUEUE: // ?????????(???????????????)
				backColor = JobMapColor.blue;
				break;
			case StatusConstant.TYPE_SUSPEND_QUEUE: // ??????(???????????????)
				backColor = JobMapColor.yellow;
				break;
			default:
				break;
			}
		}

		// ????????????????????????????????????????????????????????????
		foreColor = new Color(null,
				backColor.getRed() * 3 / 4,
				backColor.getGreen() * 3 / 4,
				backColor.getBlue() * 3 / 4);

		/*
		 * ???????????????
		 * ?????????priority??????
		 */
		m_background.setLineWidth(lineWidth);
		m_background.setForegroundColor(foreColor); // ????????????????????????????????????????????????????????????
		m_background.setDownColor(foreColor);
		m_background.setBackgroundColor(backColor); // ????????????
	}

	@Override
	public void repaint(){
		super.repaint();
	}

	@Override
	public void handleFocusGained(FocusEvent fe){
		m_log.debug("handleFocusGained " + fe +", jobId="+m_jobTreeItem.getData().getId());

		if (m_editorView != null) {
			m_log.debug("handleFocusGained Call setEnabledAction ");
			//????????????????????????????????????/??????????????? 
			m_editorView.setEnabledAction(m_jobTreeItem);
		}

		// ??????????????????
		setFocus(true);
	}

	@Override
	public void handleFocusLost(FocusEvent fe){
		m_log.debug("handleFocusLost " + fe +", jobId="+m_jobTreeItem.getData().getId());
		if (m_editorView != null) {
			m_editorView.setEnabledActionAll(false);
		}
		setFocus(false);
	}

	/**
	 *  ????????????????????????????????????
	 * @return
	 */
	private Panel getTooltip(){
		Panel tooltip = new Panel();
		tooltip.setLayoutManager(new FlowLayout(false));

		tooltip.add(new Label(m_jobTreeItem.getData().getName() + " (" +
				m_jobTreeItem.getData().getId() + ")"));
		if (m_jobTreeItem.getData().getDescription() != null
				&& !m_jobTreeItem.getData().getDescription().equals("")) {
			tooltip.add(new Label(m_jobTreeItem.getData().getDescription()));
		}

		JobDetailInfo detail = m_jobTreeItem.getDetail();
		// ??????????????????[??????]???????????????????????????????????????
		if (detail == null) {
			if( m_jobTreeItem.getData().getWaitRule() != null ){
				//???????????????????????????????????????????????????????????????
				if(m_jobTreeItem.getData().getWaitRule().isJobRetryFlg()){
					StringBuilder messageBuilder = new StringBuilder();
					messageBuilder.append (Messages.getString("job.retry.count") + ":" + m_jobTreeItem.getData().getWaitRule().getJobRetry());
					if( m_jobTreeItem.getData().getWaitRule().getJobRetryEndStatus() != null ){
						messageBuilder.append("\n");
						messageBuilder.append(Messages.getString("job.retry.end.status")  + ":" +  EndStatusMessage.typeToString(m_jobTreeItem.getData().getWaitRule().getJobRetryEndStatus()));
					}
					tooltip.add(new Label(messageBuilder.toString()));
				}
				//???????????????????????????????????????????????????
				if(m_jobTreeItem.getData().getWaitRule().isSuspend()){
					tooltip.add(new Label(Messages.getString("reserve") ));
				}
				//?????????????????????????????????????????????????????????
				if(m_jobTreeItem.getData().getWaitRule().isSkip()){
					StringBuilder messageBuilder = new StringBuilder();
					messageBuilder.append(Messages.getString("skip"));
					if( m_jobTreeItem.getData().getWaitRule().getSkipEndStatus() != null ){
						messageBuilder.append("\n");
						messageBuilder.append(Messages.getString("end.status")  + ":" +  EndStatusMessage.typeToString(m_jobTreeItem.getData().getWaitRule().getSkipEndStatus()));
					}
					if( m_jobTreeItem.getData().getWaitRule().getSkipEndValue() != null ){
						messageBuilder.append("\n");
						messageBuilder.append(Messages.getString("end.value")  + ":" +  m_jobTreeItem.getData().getWaitRule().getSkipEndValue());
					}
					tooltip.add(new Label( messageBuilder.toString()));
				}
			}
			return tooltip;
		}

		// ????????????
		Panel subPanel = null;
		subPanel = new Panel();
		subPanel.setLayoutManager(new FlowLayout(true));
		subPanel.add(new Label(Messages.getString("run.status") + " : "));
		Integer status = null;
		status = m_jobTreeItem.getDetail().getStatus();
		if (status != null) {
			subPanel.add(new ImageFigure(StatusImageConstant.typeToImage(status)));
			subPanel.add(new Label(StatusMessage.typeToString(status)));
		}
		tooltip.add(subPanel);

		// ????????????
		subPanel = new Panel();
		subPanel.setLayoutManager(new FlowLayout(true));
		subPanel.add(new Label(Messages.getString("end.status") + " : "));
		Integer endStatus = null;
		endStatus = m_jobTreeItem.getDetail().getEndStatus();
		if (endStatus != null) {
			subPanel.add(new ImageFigure(EndStatusImageConstant.typeToImage(endStatus)));
			subPanel.add(new Label(EndStatusMessage.typeToString(endStatus)));
		}
		tooltip.add(subPanel);

		// ?????????
		subPanel = new Panel();
		subPanel.setLayoutManager(new FlowLayout(true));
		subPanel.add(new Label(Messages.getString("end.value") + " : "));
		Integer endValue = null;
		endValue = m_jobTreeItem.getDetail().getEndValue();
		if (endValue != null) {
			subPanel.add(new Label(endValue.toString()));
		}
		tooltip.add(subPanel);

		// ????????????????????????
		String dateStr = "";
		Date date = null;
		if (m_jobTreeItem.getDetail().getStartDate() != null) {
			date = new Date(m_jobTreeItem.getDetail().getStartDate());
		}
		if (date != null) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			dateStr = df.format(date);
		}
		tooltip.add(new Label(Messages.getString("start.rerun.time") + " : " + dateStr));

		// ?????????????????????
		dateStr = "";
		date = null;
		if (m_jobTreeItem.getDetail().getEndDate() != null) {
			date = new Date(m_jobTreeItem.getDetail().getEndDate());
		}
		if (date != null) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			dateStr = df.format(date);
		}
		tooltip.add(new Label(Messages.getString("end.suspend.time") + " : " + dateStr));

		//??????????????????????????????????????????????????????????????????????????????
		if( m_jobTreeItem.getData().getWaitRule() != null && m_jobTreeItem.getData().getWaitRule().isJobRetryFlg() && m_jobTreeItem.getDetail().getRunCount() != null ){
			tooltip.add(new Label(Messages.getString("job.run.count") + " : " + m_jobTreeItem.getDetail().getRunCount()));
		}

		return tooltip;
	}

	/**
	 * ????????????????????????????????????????????????
	 */
	private void setFocus(boolean focus){

		JobFigure focusFigure = m_jobMapComposite.getFocusFigure();
		if(m_log.isDebugEnabled()){
			m_log.debug("setFocus : focus=" +focus + " focusFigure="+focusFigure.getJobTreeItem().getData().getId());
		}
		// ????????????????????????????????????
		if(focus == true && this.equals(focusFigure)){
			m_background.setLineWidth(lineWidth * 2);
			m_jobMapComposite.emphasisConnection(focusFigure.getJobTreeItem().getData().getId());
		} else {
			m_background.setLineWidth(lineWidth);
			m_jobMapComposite.emphasisConnection("");
		}
		
	}

	public GRoundedRectangle getBackground() {
		return m_background;
	}

	public ScalableLayeredPane getLayer() {
		return m_layerXY;
	}

	public JobTreeItem getJobTreeItem() {
		return m_jobTreeItem;
	}
	
	public boolean isLockedJob() {
		JobEditState editState = JobEditStateUtil.getJobEditState(JobTreeItemUtil.getManagerName(m_jobTreeItem));
		return editState.isLockedJobunitId(m_jobTreeItem.getData().getJobunitId());
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}
