/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;

/**
 * イベント履歴の削除処理
 *
 * @version 4.0.0
 * @since 3.1.0
 *
 */
public class MaintenanceEvent extends MaintenanceObject{

	private static Log m_log = LogFactory.getLog( MaintenanceEvent.class );

	private static final Object _deleteLock = new Object();

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId) {
		m_log.debug("_delete() start : status = " + status);
		int ret = 0;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			String ownerRoleId2 = null;

			synchronized (_deleteLock) {
				//オーナーロールIDがADMINISTRATORSの場合
				if(RoleIdConstant.isAdministratorRole(ownerRoleId)) {
					//SQL文の実行
					if(status){
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						QueryUtil.deleteEventLogOperationHistoryByGenerationDate(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
						ret = QueryUtil.deleteEventLogByGenerationDate(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
					} else {
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						//status=falseの場合は確認済みイベントのみを削除する
						QueryUtil.deleteEventLogOperationHistoryByGenerationDateConfigFlg(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
						ret = QueryUtil.deleteEventLogByGenerationDateConfigFlg(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue());
					}
				}
				//オーナーロールが一般ロールの場合
				else {
					ownerRoleId2 = ownerRoleId;
					//SQL文の実行
					if(status){
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						QueryUtil.deleteEventLogOperationHistoryByGenerationDateAndOwnerRoleId(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
						ret = QueryUtil.deleteEventLogByGenerationDateAndOwnerRoleId(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
					} else {
						// for HA (縮退判定時間を延ばすため)、シングルには影響なし(0) : タイムアウト値設定
						//status=falseの場合は確認済みイベントのみを削除する
						QueryUtil.deleteEventLogOperationHistoryByGenerationDateConfigFlgAndOwnerRoleId(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
						ret = QueryUtil.deleteEventLogByGenerationDateConfigFlgAndOwnerRoleId(boundary, HinemosPropertyCommon.maintenance_query_timeout.getIntegerValue(), ownerRoleId);
					}
				}
	
				// cache内も消す
				// status=trueは全削除、status=falseはConfirmFlgが1(確認)のものを削除
				jtm.addCallback(new EventCacheModifyCallback(boundary, status, ownerRoleId2));
			}

			//終了
			m_log.debug("_delete() count : " + ret);

		} catch (Exception e) {
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteCollectData() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
		}
		return ret;
	}

}
