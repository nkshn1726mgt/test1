/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.extensions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.clustercontrol.ClusterControlPlugin;

public class CloudOptionExtension {
	public static final String pointId = "cloudOption";
	public static final String elementName = "cloudOption";
	
	public static final String platformIdAttributeName = "platformId";
	public static final String nameAttributeName = "name";
	
	private Map<String, String> options = new HashMap<>();
	
	private static CloudOptionExtension singleton;
	
	private static final Log logger = LogFactory.getLog(CloudOptionExtension.class);
	
	private CloudOptionExtension() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();

		// 拡張ポイントを取得
		IExtensionPoint point = registry.getExtensionPoint(ClusterControlPlugin.getDefault().getBundle().getSymbolicName() + "." + pointId);
		for (IExtension ex: point.getExtensions()) {
			for (IConfigurationElement element: ex.getConfigurationElements()) {
				// 要素名が該当するpluginInfoのIdだった場合、ExtensionTypeの情報を取得
				if(element.getName().equals(elementName)){
					try {
						String platformId = element.getAttribute(platformIdAttributeName);
						String name = element.getAttribute(nameAttributeName);
						options.put(platformId, name);
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		}
	}
	
	public static Map<String, String> getOptions() {
		if (singleton == null)
			singleton = new CloudOptionExtension();
		return Collections.unmodifiableMap(singleton.options);
	}
}
