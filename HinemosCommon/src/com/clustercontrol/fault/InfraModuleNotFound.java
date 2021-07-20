/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

public class InfraModuleNotFound extends HinemosException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String managementId;
	
	private String moduleId;

	/**
	 * InfraCheckResultNotFound コンストラクタ
	 */
	public InfraModuleNotFound(String managementId, String moduleId) {
		super(String.format("managementId = %s, moduleId = %s", managementId, moduleId));
		setManagementId(managementId);
		setModuleId(moduleId);
	}

	/**
	 * InfraCheckResultNotFound コンストラクタ
	 * @param messages
	 */
	public InfraModuleNotFound(String managementId, String moduleId, String messages) {
		super(messages);
		setManagementId(managementId);
		setModuleId(moduleId);
	}

	/**
	 * InfraCheckResultNotFound コンストラクタ
	 * @param e
	 */
	public InfraModuleNotFound(String managementId, String moduleId, Throwable e) {
		super(e);
		setManagementId(managementId);
		setModuleId(moduleId);
	}

	/**
	 * InfraCheckResultNotFound コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InfraModuleNotFound(String managementId, String moduleId, String messages, Throwable e) {
		super(messages, e);
		setManagementId(managementId);
		setModuleId(moduleId);
	}

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
}
