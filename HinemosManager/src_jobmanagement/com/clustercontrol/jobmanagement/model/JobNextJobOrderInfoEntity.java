/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;



/**
 * The persistent class for the cc_job_next_job_order_info database table.
 *
 */
@Entity
@Table(name="cc_job_next_job_order_info", schema="log")
public class JobNextJobOrderInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private JobNextJobOrderInfoEntityPK id;
	private Integer order; 
	private JobInfoEntity jobInfoEntity;
	
	public JobNextJobOrderInfoEntity() {
	}

	public JobNextJobOrderInfoEntity(JobNextJobOrderInfoEntityPK pk) {
		this.setId(pk);
	}

	@EmbeddedId
	public JobNextJobOrderInfoEntityPK getId() {
		return this.id;
	}

	public void setId(JobNextJobOrderInfoEntityPK id) {
		this.id = id;
	}
	//bi-directional many-to-one association to JobInfoEntity
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(name="job_id", referencedColumnName="job_id", insertable=false, updatable=false),
		@JoinColumn(name="jobunit_id", referencedColumnName="jobunit_id", insertable=false, updatable=false),
		@JoinColumn(name="session_id", referencedColumnName="session_id", insertable=false, updatable=false)
	})
	public JobInfoEntity getJobInfoEntity() {
		return this.jobInfoEntity;
	}

	@Deprecated
	public void setJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.jobInfoEntity = jobInfoEntity;
	}

	@Column(name="order_no")
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * JobInfoEntityćŖććøć§ćÆćåē§čØ­å®<BR>
	 * 
	 * JobInfoEntityčØ­å®ęćÆSetterć«ä»£ćććć”ććä½æēØććććØć
	 * 
	 * JPAć®ä»ę§(JSR 220)ć§ćÆććć¼ćæę“ę°ć«ä¼“ćrelationshipć®ē®”ēćÆć¦ć¼ć¶ć«å§ć­ććć¦ććć
	 * INSERTćDELETEęć«ććć®ćŖććøć§ćÆćć«åÆ¾ććåē§ćć”ć³ććć³ć¹ććå¦ēćå®č£ććć
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developerās responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void relateToJobInfoEntity(JobInfoEntity jobInfoEntity) {
		this.setJobInfoEntity(jobInfoEntity);
		if (jobInfoEntity != null) {
			List<JobNextJobOrderInfoEntity> list = jobInfoEntity.getJobNextJobOrderInfoEntities();
			if (list == null) {
				list = new ArrayList<JobNextJobOrderInfoEntity>();
			} else {
				for (JobNextJobOrderInfoEntity entity : list) {
					if (entity.getId().equals(this.getId())) {
						return;
					}
				}
			}
			list.add(this);
			jobInfoEntity.setJobNextJobOrderInfoEntities(list);
		}
	}

	/**
	 * åé¤åå¦ē<BR>
	 * 
	 * JPAć®ä»ę§(JSR 220)ć§ćÆććć¼ćæę“ę°ć«ä¼“ćrelationshipć®ē®”ēćÆć¦ć¼ć¶ć«å§ć­ććć¦ććć
	 * INSERTćDELETEęć«ććć®ćŖććøć§ćÆćć«åÆ¾ććåē§ćć”ć³ććć³ć¹ććå¦ēćå®č£ććć
	 * 
	 * JSR 220 3.2.3 Synchronization to the Database
	 * 
	 * Bidirectional relationships between managed entities will be persisted
	 * based on references held by the owning side of the relationship.
	 * It is the developerās responsibility to keep the in-memory references
	 * held on the owning side and those held on the inverse side consistent
	 * with each other when they change.
	 */
	public void unchain() {

		// JobInfoEntity
		if (this.jobInfoEntity != null) {
			List<JobNextJobOrderInfoEntity> list = this.jobInfoEntity.getJobNextJobOrderInfoEntities();
			if (list != null) {
				Iterator<JobNextJobOrderInfoEntity> iter = list.iterator();
				while(iter.hasNext()) {
					JobNextJobOrderInfoEntity entity = iter.next();
					if (entity.getId().equals(this.getId())){
						iter.remove();
						break;
					}
				}
			}
		}
	}
}
