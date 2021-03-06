/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.AbstractCacheManager;
import com.clustercontrol.commons.util.CacheManagerFactory;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.ICacheManager;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.nodemap.bean.ReservedFacilityIdConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityTreeItem;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * ???????????????????????????????????????????????????????????????????????????????????????????????????
 */
public class FacilityTreeCache {
	private static Log m_log = LogFactory.getLog( FacilityTreeCache.class );

	private static final ILock _lock;
	
	static {
		ILockManager lockManager = LockManagerFactory.instance().create();
		_lock = lockManager.create(FacilityTreeCache.class.getName());
		
		try {
			_lock.writeLock();
			
			HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
			FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
			HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
			
			if (facilityCache == null || facilityTreeRootCache == null || facilityTreeItemCache == null) {	// not null when clustered
				refresh();
			}
		} finally {
			_lock.writeUnlock();
		}
	}

	// ????????????????????????key: facilityId???
	@SuppressWarnings("unchecked")
	private static HashMap<String, FacilityInfo> getFacilityCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_FACILITY);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_FACILITY + " : " + cache);
		return cache == null ? null : (HashMap<String, FacilityInfo>)cache;
	}
	
	private static void storeFacilityCache(HashMap<String, FacilityInfo> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_FACILITY + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_FACILITY, newCache);
	}
	
	// ???????????????????????????
	private static FacilityTreeItem getFacilityTreeRootCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT + " : " + cache);
		return cache == null ? null : (FacilityTreeItem)cache;
	}
	
	private static void storeFacilityTreeRootCache(FacilityTreeItem newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_TREE_ROOT, newCache);
	}
	
	// ????????????????????????????????????(key: facilityId, value: facilityId???????????????facilityTreeItem??????)
	@SuppressWarnings("unchecked")
	private static HashMap<String, ArrayList<FacilityTreeItem>> getFacilityTreeItemCache() {
		ICacheManager cm = CacheManagerFactory.instance().create();
		Serializable cache = cm.get(AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM);
		if (m_log.isDebugEnabled()) m_log.debug("get cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM + " : " + cache);
		return cache == null ? null : (HashMap<String, ArrayList<FacilityTreeItem>>)cache;
	}
	
	private static void storeFacilityTreeItemCache(HashMap<String, ArrayList<FacilityTreeItem>> newCache) {
		ICacheManager cm = CacheManagerFactory.instance().create();
		if (m_log.isDebugEnabled()) m_log.debug("store cache " + AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM + " : " + newCache);
		cm.store(AbstractCacheManager.KEY_REPOSITORY_TREE_ITEM, newCache);
	}
	
	/**
	 * ??????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param facilityId ???????????????????????????????????????ID
	 * @param roleId ??????????????????????????????ID
	 * @param isNode ??????????????????????????????????????????true
	 * @throws FacilityNotFound ????????????????????????????????????
	 * @throws InvalidRole ?????????????????????????????????
	 * @throws InvalidSetting ????????????????????????????????????
	 */
	public static void validateFacilityId(String facilityId, String roleId, boolean isNode) throws FacilityNotFound, InvalidRole, InvalidSetting {
		m_log.debug("validateFacilityId() : facilityId = " + facilityId
					+ ", roleId = " + roleId);

		// ????????????
		FacilityInfo facilityInfo = getFacilityInfo(facilityId);
		if (facilityInfo == null) {
			throw new FacilityNotFound("FacilityId is not exist in repository. : facilityId = " + facilityId);
		}

		if (isNode && facilityInfo.getFacilityType() != FacilityConstant.TYPE_NODE) {
			throw new InvalidSetting("Src FacilityId is not node. : facilityId = " + facilityId);
		}

		if (!isFacilityReadable(facilityId, roleId)) {
			throw new InvalidRole(MessageConstant.MESSAGE_DO_NOT_HAVE_ENOUGH_PERMISSION.getMessage() + ", facilityId = " + facilityId);
		}
	}

	private static boolean isFacilityReadable(String facilityId, String roleId) {
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
		
		List<FacilityTreeItem> treeItemList = facilityTreeItemCache.get(facilityId);
		if (treeItemList != null && !treeItemList.isEmpty()) {
			for (FacilityTreeItem item : treeItemList) {
				m_log.debug("item=" + item.getData().getFacilityId());
				if (item.getAuthorizedRoleIdSet().contains(roleId)) {
					return true;
				}
			}
		}
	
		return false;
	}

	/**
	 * ????????????????????????????????????????????????
	 * 
	 * @param userId ?????????ID
	 * @return ??????????????????????????????????????????????????????
	 */
	public static List<FacilityInfo> getNodeFacilityInfoListByUserId(String userId){
		m_log.debug("getNodeListByUserId() : userId " + userId);
		return getNodeFacilityList(getFacilityTreeByUserId(userId));
	}

	/**
	 * ????????????????????????????????????????????????
	 * 
	 * @param roleId ?????????ID
	 * @return ??????????????????????????????????????????????????????
	 */
	public static List<FacilityInfo> getNodeFacilityInfoListByRoleId(String roleId){
		m_log.debug("getNodeListByRoleId() : roleId " + roleId);
		FacilityTreeItem facilityTreeItem = getFacilityTreeByRoleId(roleId);
		return getNodeFacilityList(facilityTreeItem);
	}


	/**
	 * ??????????????????????????????????????????
	 * 
	 * @param facilityTreePrivilege ?????????????????????????????????????????????
	 * @return ??????????????????????????????
	 */
	private static List<FacilityInfo> getNodeFacilityList(FacilityTreeItem facilityTreeItem){
		m_log.debug("getNodeList() ");
		
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
		
		List<FacilityInfo> facilityInfoList = new ArrayList<FacilityInfo>();
		Set<String> facilityIdSet = new HashSet<String>();
		FacilityTreeItem rootItem = null;
		rootItem = facilityTreeItem.clone();

		if (rootItem.getChildrenArray() != null) {
			for (FacilityTreeItem childItem : rootItem.getChildren()) {
				getNodeFacilityListRecursive(childItem, facilityIdSet);
			}
		}

		// ???????????????????????????????????????????????????
		for (String facilityId : facilityIdSet) {
			FacilityInfo facilityInfo = facilityCache.get(facilityId);
			facilityInfo.setNotReferFlg(false);
			facilityInfoList.add(facilityInfo);
		}
		return facilityInfoList;
	}

	/**
	 * ??????????????????????????????????????????
	 * 
	 */
	private static void getNodeFacilityListRecursive(FacilityTreeItem facilityTreeItem, Set<String> facilityIdSet){
		// ?????????????????????????????????????????????
		if (facilityTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_NODE) {
			String facilityId = facilityTreeItem.getData().getFacilityId();
			if (!facilityIdSet.contains(facilityId)) {
				facilityIdSet.add(facilityId);
			}
			return;
		}
		// ????????????????????????????????????
		if (facilityTreeItem.getChildrenArray() != null) {
			for (FacilityTreeItem childItem : facilityTreeItem.getChildrenArray()) {
				getNodeFacilityListRecursive(childItem, facilityIdSet);
			}
		}
	}

	/**
	 * ???????????????????????????????????????
	 * 
	 * @return ???????????????????????????????????????
	 */
	public static FacilityTreeItem getAllFacilityTree(){
		m_log.debug("getAllFacilityTree()");
		
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
		return facilityTreeRootCache;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????
	 * 
	 * @param userId ?????????ID
	 * @return ??????????????????????????????????????????????????????????????????
	 */
	public static FacilityTreeItem getFacilityTreeByUserId(String userId){
		m_log.debug("getFacilityTreeByUserId() : userId " + userId);
		return getFacilityTree(UserRoleCache.getRoleIdList(userId));
	}


	/**
	 * ??????????????????????????????????????????????????????????????????
	 * 
	 * @param roleId ?????????ID
	 * @return ??????????????????????????????????????????????????????????????????
	 */
	public static FacilityTreeItem getFacilityTreeByRoleId(String roleId){
		m_log.debug("getFacilityTreeByRoleId() : roleId " + roleId);
		return getFacilityTree(roleId);
	}

	private static FacilityTreeItem getFacilityTree(List<String> roleIdList) {
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
		
		FacilityTreeItem rootItem = null;
		rootItem = facilityTreeRootCache.clone();
		getFacilityTreeRecursive(rootItem, roleIdList);

		return rootItem;
	}

	private static FacilityTreeItem getFacilityTree(String roleId){
		ArrayList<String> roleIdList = new ArrayList<String>();
		roleIdList.add(roleId);

		return getFacilityTree(roleIdList);
	}

	/**
	 * ??????????????????????????????????????????????????????
	 * 
	 */
	private static void getFacilityTreeRecursive(FacilityTreeItem facilityTreeItem, List<String> roleIdList){
		Iterator<FacilityTreeItem>iter = facilityTreeItem.getChildren().iterator();
		while (iter.hasNext()) {
			FacilityTreeItem childItem = iter.next();
			HashSet<String> roleIdSet = childItem.getAuthorizedRoleIdSet();
			if (roleIdSet == null || !hasAnyCommonRoleId(roleIdSet, roleIdList)) {
				childItem.getData().setNotReferFlg(true);
			} else {
				childItem.getData().setNotReferFlg(false);
			}

			getFacilityTreeRecursive(childItem, roleIdList);
			
			// ?????????????????????????????????????????????????????????????????????????????????????????????
			if (childItem.getData().isNotReferFlg() && childItem.getChildren().size() == 0) {
				iter.remove();
			}
		}
	}

	private static boolean hasAnyCommonRoleId(HashSet<String> roleIdSet,
			List<String> roleIdList) {
		for (String roleId : roleIdList) {
			if (roleIdSet.contains(roleId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ??????????????????ID????????????FacilityInfo?????????
	 */
	public static FacilityInfo getFacilityInfo(String facilityId) {
		m_log.debug("getFacilityInfo() : facilityId " + facilityId);

		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
		
		return facilityCache.get(facilityId);
	}

	/**
	 * ???????????????????????????FacilityInfo?????????????????????
	 * 
	 * @param facilityId
	 * @return list
	 */
	public static List<FacilityInfo> getParentFacilityInfo(String facilityId) {
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
		
		List<FacilityInfo> list = new ArrayList<FacilityInfo>();
		List<FacilityTreeItem> treeItems = facilityTreeItemCache.get(facilityId);
		if (treeItems == null) {
			return list;
		}
		for (FacilityTreeItem treeItem : treeItems) {
			FacilityTreeItem parentTreeItem = treeItem.getParent();
			if (parentTreeItem != null) {
				list.add(parentTreeItem.getData());
			}
		}
		return list;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	 * @param facilityId
	 * @return list
	 */
	public static List<FacilityInfo> getChildFacilityInfoList(String facilityId) {
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		HashMap<String, FacilityInfo> facilityCache = getFacilityCache();
		
		List<FacilityInfo>childFacilityInfoList = new ArrayList<FacilityInfo>();
		Set<String> childFacilityIdSet = getChildFacilityIdSet(facilityId);
		for (String childFacilityId : childFacilityIdSet) {
			childFacilityInfoList.add(facilityCache.get(childFacilityId));
		}
		return childFacilityInfoList;
	}

	public static Set<String> getChildFacilityIdSet(String facilityId) {
		// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		// (?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????)
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemCache = getFacilityTreeItemCache();
		
		HashSet<String> childFacilityIdSet = new HashSet<String>();
		List<FacilityTreeItem> treeItems = facilityTreeItemCache.get(facilityId);
		if (treeItems == null) {
			return childFacilityIdSet;
		}

		for (FacilityTreeItem treeItem : treeItems) {
			for (FacilityTreeItem childTreeItem : treeItem.getChildren()) {
				FacilityInfo childFacilityInfo = childTreeItem.getData();
				childFacilityIdSet.add(childFacilityInfo.getFacilityId());
			}
		}
		return childFacilityIdSet;
	}

	/** ????????????????????????????????????????????????????????? */
	public static synchronized void refresh() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transactioin has not been begined.");
				return;
			}

			try {
				_lock.writeLock();
				
				/*
				 * FacilityInfoMap ?????????????????????
				 */
				long startTime = HinemosTime.currentTimeMillis();
				em.clear();
				
				// FacilityInfo???FacilityTreeItem????????????????????????????????????????????????????????????
				HashMap<String, FacilityInfo>facilityInfoMap = createFacilityInfoMap();
				if (facilityInfoMap == null) {
					return;
				}
				long infoMapRefreshTime = HinemosTime.currentTimeMillis() - startTime;
				m_log.info("refresh() : FacilityInfoMap(Cache) " + infoMapRefreshTime + "ms. size=" + facilityInfoMap.size());

				/*
				 * FacilityTreeItem ?????????????????????
				 */
				startTime = HinemosTime.currentTimeMillis();
				FacilityTreeItem facilityTreeItem = createFacilityTreeItem(facilityInfoMap);
				if (facilityTreeItem == null) {
					return;
				}
				long treeItemRefreshTime = HinemosTime.currentTimeMillis() - startTime;
				m_log.info("refresh() : FacilityTreeItem(Cache) " + treeItemRefreshTime + "ms");

				//FacilityTreeItemMap?????????????????????
				startTime = HinemosTime.currentTimeMillis();
				HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemMap = createFacilityTreeItemMap(
						facilityInfoMap, facilityTreeItem);
				long treeItemMapRefreshTime = HinemosTime.currentTimeMillis() - startTime;
				m_log.info("refresh() : FacilityTreeItemMap(Cache) " + treeItemMapRefreshTime + "ms");

				storeFacilityCache(facilityInfoMap);
				storeFacilityTreeRootCache(facilityTreeItem);
				storeFacilityTreeItemCache(facilityTreeItemMap);
			} finally {
				_lock.writeUnlock();
			}
		}
	}

	private static HashMap<String, ArrayList<FacilityTreeItem>> createFacilityTreeItemMap(
			HashMap<String, FacilityInfo> facilityInfoMap,
			FacilityTreeItem facilityTreeItem) {
		HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemMap = new HashMap<String, ArrayList<FacilityTreeItem>>();

		for (FacilityTreeItem childTreeItem : facilityTreeItem.getChildren()) {
			createFacilityTreeItemMapRecursive(childTreeItem, facilityTreeItemMap);
		}

		return facilityTreeItemMap;
	}

	private static void createFacilityTreeItemMapRecursive(
			FacilityTreeItem treeItem,
			HashMap<String, ArrayList<FacilityTreeItem>> facilityTreeItemMap) {

		String facilityId = treeItem.getData().getFacilityId();
		ArrayList<FacilityTreeItem> facilityTreeItemList = facilityTreeItemMap.get(facilityId);
		if (facilityTreeItemList == null) {
			facilityTreeItemList = new ArrayList<FacilityTreeItem>();
		}

		facilityTreeItemList.add(treeItem);
		facilityTreeItemMap.put(facilityId, facilityTreeItemList);

		for (FacilityTreeItem childTreeItem : treeItem.getChildren()) {
			createFacilityTreeItemMapRecursive(childTreeItem, facilityTreeItemMap);
		}
	}

	/** ??????????????????????????????????????? **/
	public static void printCache() {
		try {
			_lock.readLock();
			
			Map<String, FacilityInfo> facilityCache = getFacilityCache();
			FacilityTreeItem facilityTreeRootCache = getFacilityTreeRootCache();
			
			/*
			 * m_facilityInfoMap ?????????
			 */
			m_log.info("printCache() : FacilityInfo start");
			for(FacilityInfo info: facilityCache.values()) {
				m_log.info("facility id = " + info.getFacilityId() +
						", facility name = " + info.getFacilityName());

			}
			m_log.info("printCache() : FacilityInfo end");

			/*
			 * m_facilityTreeItem ?????????
			 */
			m_log.info("printCache() : FacilityTreeItem start");
			String brank = "  ";
			FacilityTreeItem treeItem = facilityTreeRootCache.clone();
			if (treeItem != null) {
				m_log.info("facility id = " + treeItem.getData().getFacilityId());
				for (FacilityTreeItem tree : treeItem.getChildrenArray()) {
					m_log.info(brank + "facility id = " + tree.getData().getFacilityId());
					printFacilityTreeItemRecursive(tree, RepositoryControllerBean.ALL, true, brank);
				}
			}
			m_log.info("printCache() : FacilityTreeItem end");
		} finally {
			_lock.readUnlock();
		}
	}

	/**
	 * ????????????????????????????????????????????????????????????????????????<BR>
	 * 
	 * @param parentFacilityTreeItem ???????????????????????????????????????????????????
	 * @param level ?????????????????????
	 * @param facilityList ?????????????????????????????????????????????
	 * @param scopeFlag ?????????????????????????????????????????????:true ????????????:false)
	 * @param brank ??????????????????
	 */
	private static void printFacilityTreeItemRecursive(FacilityTreeItem parentFacilityTreeItem,
			int level, boolean scopeFlag, String brank) {
		/** ?????????????????? */
		boolean recursive = false;
		int nextLevel = 0;

		// ??????????????????
		brank = brank + "  ";

		/** ??????????????? */
		// ??????????????????????????????????????????????????????
		if (level == RepositoryControllerBean.ALL) {
			recursive = true;
			nextLevel = RepositoryControllerBean.ALL;
		} else if (level > 1) {
			recursive = true;
			nextLevel = level - 1;
		}

		// ??????????????????????????????????????????????????????
		FacilityTreeItem[] childFacilityTreeItems = parentFacilityTreeItem.getChildrenArray();
		if (childFacilityTreeItems != null) {
			for (FacilityTreeItem childFacilityTreeItem : childFacilityTreeItems) {
				if (childFacilityTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE) {
					if (scopeFlag) {
						m_log.info(brank + "facility id = " + childFacilityTreeItem.getData().getFacilityId());
					}
				} else {
					m_log.info(brank + "facility id = " + childFacilityTreeItem.getData().getFacilityId());
				}
				if (recursive) {
					printFacilityTreeItemRecursive(childFacilityTreeItem, nextLevel, scopeFlag, brank);
				}
			}
		}
	}


	/**
	 * ??????????????????????????????????????????????????????????????????<BR>
	 * 
	 * @return ConcurrentHashMap<String, FacilityInfo>
	 */
	private static HashMap<String, FacilityInfo> createFacilityInfoMap() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// ????????????????????????????????????????????????????????????????????????????????????
			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transactioin has not been begined.");
				return null;
			}

			HashMap<String, FacilityInfo> facilityInfoMap = new HashMap<String, FacilityInfo>();

			// ????????????????????????????????????
			List<NodeInfo> nodeEntities = NodeProperty.getAllList();
			for (NodeInfo nodeEntity : nodeEntities) {
				// ???????????????????????????
				FacilityInfo facilityInfo = new FacilityInfo();
				facilityInfo.setFacilityId(nodeEntity.getFacilityId());
				facilityInfo.setFacilityName(nodeEntity.getFacilityName());
				facilityInfo.setFacilityType(nodeEntity.getFacilityType());
				facilityInfo.setDisplaySortOrder(nodeEntity.getDisplaySortOrder());
				facilityInfo.setIconImage(nodeEntity.getIconImage());
				facilityInfo.setBuiltInFlg(false);
				facilityInfo.setValid(FacilityUtil.isValid(nodeEntity));
				facilityInfo.setOwnerRoleId(nodeEntity.getOwnerRoleId());
				facilityInfo.setDescription(nodeEntity.getDescription());

				facilityInfoMap.put(nodeEntity.getFacilityId(), facilityInfo);
			}

			// ???????????????????????????????????????
			List<ScopeInfo> scopeEntities = QueryUtil.getAllScope_NONE();
			for (ScopeInfo scopeEntity : scopeEntities) {
				// ???????????????????????????
				FacilityInfo facilityInfo = new FacilityInfo();
				facilityInfo.setFacilityId(scopeEntity.getFacilityId());
				facilityInfo.setFacilityName(scopeEntity.getFacilityName());
				facilityInfo.setFacilityType(scopeEntity.getFacilityType());
				facilityInfo.setDisplaySortOrder(scopeEntity.getDisplaySortOrder());
				facilityInfo.setIconImage(scopeEntity.getIconImage());
				facilityInfo.setBuiltInFlg(FacilitySelector.isBuildinScope(scopeEntity));
				facilityInfo.setValid(FacilityUtil.isValid(scopeEntity));
				facilityInfo.setOwnerRoleId(scopeEntity.getOwnerRoleId());
				facilityInfo.setDescription(scopeEntity.getDescription());

				facilityInfoMap.put(scopeEntity.getFacilityId(), facilityInfo);
			}

			return facilityInfoMap;
		}
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????<BR>
	 * 
	 * @return FacilityTreeItem
	 */
	private static FacilityTreeItem createFacilityTreeItem(Map<String, FacilityInfo> facilityInfoMap) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			// ????????????????????????????????????????????????????????????????????????????????????
			if (!jtm.isNestedEm()) {
				m_log.warn("refresh() : transactioin has not been begined.");
				return null;
			}

			m_log.debug("getting tree data of facilities...");

			//Object???????????????????????????????????????????????????
			HashMap<String, ArrayList<String>> objectRoleMap = getObjectRoleMap();

			// ?????????????????????????????????????????????
			FacilityInfo rootFacilityInfo = new FacilityInfo();
			rootFacilityInfo.setFacilityId(ReservedFacilityIdConstant.ROOT_SCOPE);
			rootFacilityInfo.setFacilityName(MessageConstant.ROOT.getMessage());
			rootFacilityInfo.setFacilityType(FacilityConstant.TYPE_COMPOSITE);
			FacilityTreeItem rootTreeItem = new FacilityTreeItem(null, rootFacilityInfo);

			// ???????????????Facility???ID?????????????????????
			List<FacilityRelationEntity> facilityRelationList = QueryUtil.getAllFacilityRelations_NONE();
			Map<String, ArrayList<String>> facilityRelationMap = new HashMap<String, ArrayList<String>>();
			for (FacilityRelationEntity facilityRelationEntity : facilityRelationList) {
				String parentFacilityId = facilityRelationEntity.getParentFacilityId();
				String childFacilityId = facilityRelationEntity.getChildFacilityId();
				ArrayList<String> childFacilityIdList = facilityRelationMap.get(parentFacilityId);
				if (childFacilityIdList == null) {
					childFacilityIdList = new ArrayList<String>();
				}
				childFacilityIdList.add(childFacilityId);
				facilityRelationMap.put(parentFacilityId, childFacilityIdList);
			}

			try {
				for (FacilityInfo facilityEntity : FacilitySelector.getRootScopeList()) {
					createFacilityTreeItemRecursive(rootTreeItem,
							facilityEntity.getFacilityId(), facilityInfoMap,
							facilityRelationMap, objectRoleMap);
				}
				FacilityTreeItem.completeParent(rootTreeItem); // createFacilityTreeItemRecursive???????????????????????????????????????
			} catch (FacilityNotFound e) {
			} catch (Exception e) {
				m_log.warn("createFacilityTreeItem() failure to get a tree data of facilities. : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			m_log.debug("successful in getting tree data of facilities.");
			return rootTreeItem;
		}
	}

	private static HashMap<String, ArrayList<String>> getObjectRoleMap() {
		List<ObjectPrivilegeInfo> objectPrivilegeEntities = com.clustercontrol.accesscontrol.util.QueryUtil
				.getAllObjectPrivilegeByFilter(
						HinemosModuleConstant.PLATFORM_REPOSITORY,
						null,
						null,
						PrivilegeConstant.ObjectPrivilegeMode.READ.toString());

		HashMap<String, ArrayList<String>> objectRoleMap = new HashMap<String, ArrayList<String>>();
		for (ObjectPrivilegeInfo objectPrivilegeEntity : objectPrivilegeEntities) {
			String objectId = objectPrivilegeEntity.getId().getObjectId();
			ArrayList<String> roleIdList = objectRoleMap.get(objectId);
			if (roleIdList == null) {
				roleIdList = new ArrayList<String>();
			}
			roleIdList.add(objectPrivilegeEntity.getId().getRoleId());
			objectRoleMap.put(objectId, roleIdList);
		}
		return objectRoleMap;
	}

	/**
	 * ????????????????????????????????????????????????????????????<BR>
	 * 
	 * @param parentTreeItem ??????????????????????????????????????????
	 * @param facilityId
	 * @param facilityInfoMap
	 * @param facilityRelationMap
	 * @param objectRoleMap
	 * @param roleId ?????????ID
	 */
	private static void createFacilityTreeItemRecursive(
			FacilityTreeItem parentTreeItem,
			String facilityId,
			Map<String, FacilityInfo> facilityInfoMap,
			Map<String, ArrayList<String>> facilityRelationMap,
			HashMap<String, ArrayList<String>> objectRoleMap) {

		// ???????????????????????????
		FacilityInfo facilityInfo = facilityInfoMap.get(facilityId);
		if (facilityInfo == null) {
			// ??????????????????DB?????????commit??????????????????????????????????????????????????????????????????????????????????????????????????????skip?????????.
			m_log.info("createFacilityTreeItemRecursive : facilityInfo is null. " + facilityId);
			return ;
		}
		FacilityTreeItem treeItem = new FacilityTreeItem(parentTreeItem, facilityInfo);
		treeItem.setAuthorizedRoleIdSet(getAuthorizedRoleIdSet(facilityInfo,
				parentTreeItem, objectRoleMap));

		// ?????????????????????????????????????????????
		if (FacilityUtil.isScope_FacilityInfo(facilityInfo)) {
			List<String> childFacilityIdList = facilityRelationMap.get(facilityId);
			if (childFacilityIdList != null) {
				for (String childFacilityId : childFacilityIdList) {
					createFacilityTreeItemRecursive(treeItem, childFacilityId,
							facilityInfoMap, facilityRelationMap, objectRoleMap);
				}
			}
		}
	}

	private static HashSet<String> getAuthorizedRoleIdSet(
			FacilityInfo facilityInfo, FacilityTreeItem parentTreeItem, HashMap<String, ArrayList<String>>objectRoleMap) {

		HashSet<String> roleIdSet = new HashSet<String>();
		// ???????????????
		roleIdSet.add(RoleIdConstant.ADMINISTRATORS);
		roleIdSet.add(RoleIdConstant.HINEMOS_MODULE);

		// ?????????????????????
		// ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
		if(facilityInfo.getFacilityType() == FacilityConstant.TYPE_SCOPE) {
			roleIdSet.add(facilityInfo.getOwnerRoleId());
		}

		// ??????????????????????????????????????????????????????
		ArrayList<String>roleIdList = objectRoleMap.get(facilityInfo.getFacilityId());
		if (roleIdList != null) {
			roleIdSet.addAll(roleIdList);
		}

		//??????????????????????????????
		if (parentTreeItem != null
				&& parentTreeItem.getData().getFacilityType() == FacilityConstant.TYPE_SCOPE
				&& parentTreeItem.getAuthorizedRoleIdSet() != null) {
			roleIdSet.addAll(parentTreeItem.getAuthorizedRoleIdSet());
		}

		return roleIdSet;
	}
}