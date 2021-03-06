/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;


import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.util.HinemosTime;


public class HinemosSchedulerTest {

	private static Log m_log = LogFactory.getLog( HinemosSchedulerTest.class );
	
	private final AtomicInteger testno = new AtomicInteger(0);
	private final AtomicInteger jobid = new AtomicInteger(0);
	
	public ScheduledExecutorService srv0 = null;
	public ScheduledExecutorService srv1 = null;

	private final SchedulerType[]  ramTypes = {
			SchedulerType.RAM_JOB
		,	SchedulerType.RAM_MONITOR
	};

	private final String[] dbmsGroups  = {
			com.clustercontrol.jobmanagement.bean.QuartzConstant.GROUP_NAME
		,	com.clustercontrol.maintenance.bean.QuartzConstant.GROUP_NAME
		,	com.clustercontrol.hub.bean.QuartzConstant.GROUP_NAME
		,	com.clustercontrol.reporting.bean.QuartzConstant.GROUP_NAME
	};
	
	public static void main(String[] args) {
		ScheduledExecutorService exe = Executors.newScheduledThreadPool(10);
		
		exe.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				System.out.println("start");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
		
	}
	
	public HinemosSchedulerTest() {
	}
	
	public void activate() {
		m_log.debug("activate():start");
		SchedulerTestEnd();
		if (srv0 == null){
			srv0 = Executors.newSingleThreadScheduledExecutor();
		}
		srv0.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					int no = testno.getAndIncrement();
					m_log.debug("SchedulerTest() testno=" + no);
					SchedulerTest(no);
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
		m_log.debug("activate():end");
	}
	
	public void deactivate() {
		m_log.debug("deactivate():start");
		SchedulerTestEnd();
		SchedulerStressTestEnd();
		m_log.debug("deactivate():end");
	}
	
	public void SchedulerTest(int no) {
		
		switch (no) {
		case 0  : SchedulerTest00(); break;
		case 1  : SchedulerTest01(); break;
		case 2  : SchedulerTest02(); break;
		case 3  : SchedulerTest03(); break;
		case 4  : SchedulerTest04(); break;
		case 5  : SchedulerTest05(); break;
		case 6  : SchedulerTest06(); break;
		case 7  : SchedulerTest07(); break;
		case 8  : SchedulerTest08(); break;
		case 9  : SchedulerTest09(); break;
		case 10 : SchedulerTest10(); break;
		case 11 : SchedulerTest11(); break;
		case 12 : SchedulerTest12(); break;
		case 13 : SchedulerTest13(); break;
		case 14 : SchedulerTest14(); break;
		case 15 : SchedulerTest15(); break;
		case 16 : SchedulerTest16(); break;
		case 17 : SchedulerTest17(); break;
		case 18 : SchedulerTest18(); break;
		default : break;
		}
		
	}
	
	public void SchedulerTestEnd() {
		m_log.info("SchedulerTestEnd()");
		testno.set(0);
		SchedulerTestDel();
		if (srv0 != null)
			srv0.shutdown();
		srv0 = null;
	}
	
	public void SchedulerTestDel() {
		m_log.info("SchedulerTestDel start.");
		try {
			SchedulerTest00();
			SchedulerTest03();
			SchedulerTest06();
			SchedulerTest09();
			for( String type : dbmsGroups){
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(type), "ClassTypeTestOK", type);
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(type), "ParamNum", type);
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(type), "DbmsSimpleJobErr", type);
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(type), "DbmsCronJobSuccess", type);
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(type), "DbmsCronJobError", type);
			}
		} catch (Exception e) {
			m_log.info("SchedulerTestDel:Exception:" + e);
		}
		m_log.debug("SchedulerTestDel end.");
	}
	
	public void activateStressTest() {
		m_log.debug("activateStressTest():start");
		SchedulerStressTestEnd();
		if (srv1 == null){
			srv1 = Executors.newSingleThreadScheduledExecutor();
		}
		srv1.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					if (jobid.get() < 1000){
						int id = jobid.getAndIncrement();
						m_log.debug("SchedulerStressTest() id=" + id);
						SchedulerStressTest(id);
						Thread.sleep(1);
					} else {
						srv1.shutdown();
					}
						
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
		m_log.debug("activateStressTest():end");
	}
	
	
	public void SchedulerStressTestEnd() {
		m_log.info("SchedulerTestEnd:" + HinemosTime.currentTimeMillis());
		jobid.set(0);
		if (srv1 != null)
			srv1.shutdown();
		srv1 = null;
	}
	
	public void SchedulerStressTest(int id) {
		m_log.info("SchedulerStressTest():" + id);
		SchedulerTestRamCronAdd(id, "SchedulerStressTestRam", "scheduleRunJobRamStressTest", "10 0/5 * * * ? *");
		SchedulerTestDbmsCronAdd(id, "SchedulerStressTestDbms", "scheduleRunJobDbmsStressTest", "10 0/5 * * * ? *");
	}
	
	// RAM???CRON????????????????????????(??????????????????)
	public void SchedulerTestRamCronAdd(int id, String group, String methodName, String cron) {
		
		Serializable[] jdArgs = new Serializable[3];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[3];
		
		//ID?????????
		String ramkey ="StressTestRamCron-" + Integer.toString(id);
		jdArgs[0] = ramkey;
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = group;
		jdArgsType[1] = String.class;
		
		jdArgs[2] = id;
		jdArgsType[2] = Integer.class;
		
		try {
			m_log.debug("SchedulerTestRamCronAdd:scheduleCronJob():" + ramkey);
			for( SchedulerType type : ramTypes){
				SchedulerPlugin.scheduleCronJob(type, ramkey, group, HinemosTime.currentTimeMillis() + 2 * 1000,
						cron, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), methodName, jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTestRamCronAdd:Exception:" + e);
		}
	}
	
	// DBMS???CRON????????????????????????(??????????????????)
	public void SchedulerTestDbmsCronAdd(int id, String group, String methodName, String cron) {
		
		Serializable[] jdArgs = new Serializable[3];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[3];

		//ID?????????
		String dbmskey ="StressTestDbmsCron-" + Integer.toString(id);
		jdArgs[0] = dbmskey;
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = group;
		jdArgsType[1] = String.class;
		
		jdArgs[2] = id;
		jdArgsType[2] = Integer.class;
		
		try {
			m_log.debug("SchedulerTestDbmsCronAdd:scheduleCronJob():" + dbmskey);
			SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), dbmskey, group, HinemosTime.currentTimeMillis() + 2 * 1000,
					cron, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), methodName, jdArgsType, jdArgs);
			
		} catch (Exception e) {
			m_log.error("SchedulerTestDbmsCronAdd:Exception:" + e);
		}
	}
	
	// RAM???CRON????????????????????????(??????????????????????????????)
	public void SchedulerTest00() {
		
		m_log.debug("SchedulerTest00 start.");
		try {
			m_log.debug("SchedulerTest00:deleteJob():RamCronDel");
			for( SchedulerType type : ramTypes){
				SchedulerPlugin.deleteJob(type, "RamCron", "SchedulerTest");
			}
		} catch (Exception e) {
			m_log.debug("SchedulerTest00:Exception:" + e);
		}
		m_log.debug("SchedulerTest00 end.");
	}
	
	// RAM???CRON????????????????????????
	public void SchedulerTest01() {
		
		m_log.debug("SchedulerTest01 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "RamCronAdd";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest01:scheduleCronJob():RamCronAdd");
			for( SchedulerType type : ramTypes){
				SchedulerPlugin.scheduleCronJob(type, "RamCron", "SchedulerTest", HinemosTime.currentTimeMillis() + 2 * 1000,
						"0/5 * * * * ? *", true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobRamCron", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest01:Exception:" + e);
		}
		m_log.debug("SchedulerTest01 end.");
	}
	
	// RAM???CRON????????????????????????
	public void SchedulerTest02() {
		
		m_log.debug("SchedulerTest02 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "RamCronMod";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest02:scheduleCronJob():RamCronMod");
			// ID????????????????????????????????????
			for( SchedulerType type : ramTypes){
				SchedulerPlugin.scheduleCronJob(type, "RamCron", "SchedulerTest", HinemosTime.currentTimeMillis() + 2 * 1000,
						"0 0/1 * * * ? *", true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobRamCron", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest02:Exception:" + e);
		}
		m_log.debug("SchedulerTest02 end.");
	}
	
	// DBMS???CRON????????????????????????(??????????????????????????????)
	public void SchedulerTest03() {
		
		m_log.info("SchedulerTest03 start.");
		try {
			m_log.info("SchedulerTest03:deleteJob():DbmsCronDel");
			for( String group : dbmsGroups){
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsCron",group);
			}
		} catch (Exception e) {
			m_log.info("SchedulerTest03:Exception:" + e);
		}
		m_log.debug("SchedulerTest03 end.");
	}
	
	// DBMS???CRON????????????????????????
	public void SchedulerTest04() {
		
		m_log.debug("SchedulerTest04 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "DbmsCronAdd";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest04:scheduleCronJob():DbmsCronAdd");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsCron", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						"0/5 * * * * ? *", true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobDbmsCron", jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTest04:Exception:" + e);
		}
		m_log.debug("SchedulerTest04 end.");
	}
	
	// DBMS???CRON????????????????????????
	public void SchedulerTest05() {
		
		m_log.debug("SchedulerTest05 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "DbmsCronMod";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest05:scheduleCronJob():DbmsCronMod");
			// ID????????????????????????????????????
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsCron", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						"0 0/1 * * * ? *", true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobDbmsCron", jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTest05:Exception:" + e);
		}
		m_log.debug("SchedulerTest05 end.");
	}
	
	// RAM???SIMPLE????????????????????????(??????????????????????????????)
	public void SchedulerTest06() {
		
		m_log.debug("SchedulerTest06 start.");
		try {
			m_log.debug("SchedulerTest06:deleteJob():RamSimpleDel");
			for( SchedulerType type : ramTypes){
				SchedulerPlugin.deleteJob(type, "RamSimple", "SchedulerTest");
			}
		} catch (Exception e) {
			m_log.debug("SchedulerTest06:Exception:" + e);
		}
		m_log.debug("SchedulerTest06 end.");
	}
	
	// RAM???SIMPLE????????????????????????
	public void SchedulerTest07() {
		
		m_log.debug("SchedulerTest07 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "RamSimpleAdd";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest07:scheduleCronJob():RamSimpleAdd");
			for( SchedulerType type : ramTypes){
				SchedulerPlugin.scheduleSimpleJob(type, "RamSimple", "SchedulerTest", HinemosTime.currentTimeMillis() + 2 * 1000,
						5, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobRamSimple", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest07:Exception:" + e);
		}
		m_log.debug("SchedulerTest07 end.");
	}
	
	// RAM???SIMPLE????????????????????????
	public void SchedulerTest08() {
		
		m_log.debug("SchedulerTest08 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "RamSimpleMod";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest08:scheduleCronJob():RamSimpleMod");
			// ID????????????????????????????????????
			for( SchedulerType type : ramTypes){
				SchedulerPlugin.scheduleSimpleJob(type, "RamSimple", "SchedulerTest", HinemosTime.currentTimeMillis() + 2 * 1000,
						20, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobRamSimple", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest08:Exception:" + e);
		}
		m_log.debug("SchedulerTest08 end.");
	}
	
	// DBMS???SIMPLE????????????????????????(??????????????????????????????)
	public void SchedulerTest09() {
		
		m_log.info("SchedulerTest09 start.");
		try {
			m_log.info("SchedulerTest09:deleteJob():DbmsSimpleDel");
			for( String group : dbmsGroups){
				SchedulerPlugin.deleteJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsSimple", group);
			}
		} catch (Exception e) {
			m_log.info("SchedulerTest09:Exception:" + e);
		}
		m_log.debug("SchedulerTest09 end.");
	}
	
	// DBMS???SIMPLE????????????????????????
	public void SchedulerTest10() {
		
		m_log.debug("SchedulerTest10 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "DbmsSimpleAdd";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest10:scheduleCronJob():DbmsSimpleAdd");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleSimpleJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsSimple", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						5, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobDbmsSimple", jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTest10:Exception:" + e);
		}
		m_log.debug("SchedulerTest10 end.");
	}
	
	// DBMS???SIMPLE????????????????????????
	public void SchedulerTest11() {
		
		m_log.debug("SchedulerTest11 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "DbmsSimpleMod";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest11:scheduleCronJob():DbmsSimpleMod");
			// ID????????????????????????????????????
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleSimpleJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsSimple", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						20, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobDbmsSimple", jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTest11:Exception:" + e);
		}
		m_log.debug("SchedulerTest11 end.");
	}
	
	// DBMS???SIMPLE?????????????????? ??????????????????????????????????????????????????????1(?????????)
	public void SchedulerTest12() {
		
		m_log.debug("SchedulerTest12 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[9];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[9];
		// String
		jdArgs[0] = "ClassTypeTestOK";
		jdArgsType[0] = String.class;
		// String
		jdArgs[1] = "";
		jdArgsType[1] = String.class;
		// null String
		jdArgs[2] = null;
		jdArgsType[2] = String.class;
		//Boolean 
		jdArgs[3] = true;
		jdArgsType[3] = Boolean.class;
		//Integer 
		jdArgs[4] = 12;
		jdArgsType[4] = Integer.class;
		//Long 
		jdArgs[5] = (long)24;
		jdArgsType[5] = Long.class;
		//Short 
		jdArgs[6] = (short)6;
		jdArgsType[6] = Short.class;
		//Float 
		jdArgs[7] = (float)0.83f;
		jdArgsType[7] = Float.class;
		//Double 
		jdArgs[8] = (double)1.223;
		jdArgsType[8] = Double.class;
		
		try {
			m_log.debug("SchedulerTest12:scheduleCronJob():ClassTypeTestOK");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleSimpleJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "ClassTypeTestOK", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						20, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobClassTypeTest", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest12:Exception:" + e);
		}
		m_log.debug("SchedulerTest12 end.");
	}
	
	// DBMS???SIMPLE?????????????????? ??????????????????????????????????????????????????????2(?????????)
	public void SchedulerTest13() {
		
		m_log.debug("SchedulerTest13 start.");
		
		Serializable[] jdArgs = new Serializable[9];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[9];
		// String
		jdArgs[0] = "ClassTypeTestNG";
		jdArgsType[0] = String.class;
		// Date
		jdArgs[1] = HinemosTime.getDateInstance();
		jdArgsType[1] = Date.class;
		// null String
		jdArgs[2] = null;
		jdArgsType[2] = String.class;
		//Boolean 
		jdArgs[3] = true;
		jdArgsType[3] = Boolean.class;
		//Integer 
		jdArgs[4] = 12;
		jdArgsType[4] = Integer.class;
		//Long 
		jdArgs[5] = (long)24;
		jdArgsType[5] = Long.class;
		//Short 
		jdArgs[6] = (short)6;
		jdArgsType[6] = Short.class;
		//Float 
		jdArgs[7] = 0.83f;
		jdArgsType[7] = Float.class;
		//Double 
		jdArgs[8] = 1.223;
		jdArgsType[8] = Double.class;
		
		try {
			m_log.debug("SchedulerTest13:scheduleCronJob():ClassTypeTestNG");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleSimpleJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "ClassTypeTestNG", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						20, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobClassTypeTest", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest13:Exception:" + e);
		}
		m_log.debug("SchedulerTest13 end.");
	}
	
	// DBMS???SIMPLE?????????????????? ????????????????????????????????????????????????(?????????)
	public void SchedulerTest14() {
		
		m_log.debug("SchedulerTest14 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[15];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[15];
		// String
		jdArgs[0] = "ParamNum15";
		jdArgsType[0] = String.class;
		// String
		jdArgs[1] = "1";
		jdArgsType[1] = String.class;
		// String
		jdArgs[2] = "2";
		jdArgsType[2] = String.class;
		// String 
		jdArgs[3] = "3";
		jdArgsType[3] = String.class;
		// String 
		jdArgs[4] = "4";
		jdArgsType[4] = String.class;
		// String 
		jdArgs[5] = "5";
		jdArgsType[5] = String.class;
		// String 
		jdArgs[6] = "6";
		jdArgsType[6] = String.class;
		// String 
		jdArgs[7] = "7";
		jdArgsType[7] = String.class;
		// String 
		jdArgs[8] = "8";
		jdArgsType[8] = String.class;
		// String 
		jdArgs[9] = "9";
		jdArgsType[9] = String.class;
		// String 
		jdArgs[10] = "10";
		jdArgsType[10] = String.class;
		// String 
		jdArgs[11] = "11";
		jdArgsType[11] = String.class;
		// String 
		jdArgs[12] = "12";
		jdArgsType[12] = String.class;
		// String 
		jdArgs[13] = "13";
		jdArgsType[13] = String.class;
		// String 
		jdArgs[14] = "14";
		jdArgsType[14] = String.class;
		
		try {
			m_log.debug("SchedulerTest14:scheduleCronJob():ParamNum");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleSimpleJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "ParamNum",group, HinemosTime.currentTimeMillis() + 2 * 1000,
						20, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobParamNumTest", jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTest14:Exception:" + e);
		}
		m_log.debug("SchedulerTest14 end.");
	}
	
	// DBMS???SIMPLE?????????????????? ????????????????????????????????????????????????(????????????)
	public void SchedulerTest15() {
		
		m_log.debug("SchedulerTest15 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[16];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[16];
		// String
		jdArgs[0] = "ParamNum16";
		jdArgsType[0] = String.class;
		// String
		jdArgs[1] = "1";
		jdArgsType[1] = String.class;
		// String
		jdArgs[2] = "2";
		jdArgsType[2] = String.class;
		// String 
		jdArgs[3] = "3";
		jdArgsType[3] = String.class;
		// String 
		jdArgs[4] = "4";
		jdArgsType[4] = String.class;
		// String 
		jdArgs[5] = "5";
		jdArgsType[5] = String.class;
		// String 
		jdArgs[6] = "6";
		jdArgsType[6] = String.class;
		// String 
		jdArgs[7] = "7";
		jdArgsType[7] = String.class;
		// String 
		jdArgs[8] = "8";
		jdArgsType[8] = String.class;
		// String 
		jdArgs[9] = "9";
		jdArgsType[9] = String.class;
		// String 
		jdArgs[10] = "10";
		jdArgsType[10] = String.class;
		// String 
		jdArgs[11] = "11";
		jdArgsType[11] = String.class;
		// String 
		jdArgs[12] = "12";
		jdArgsType[12] = String.class;
		// String 
		jdArgs[13] = "13";
		jdArgsType[13] = String.class;
		// String 
		jdArgs[14] = "14";
		jdArgsType[14] = String.class;
		// String 
		jdArgs[15] = "15";
		jdArgsType[15] = String.class;
		
		try {
			m_log.debug("SchedulerTest15:scheduleCronJob():ParamNum");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleSimpleJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "ParamNum", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						30, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobParamNumTest", jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTest15:Exception:" + e);
		}
		m_log.debug("SchedulerTest15 end.");
	}
	
	
	// DBMS???SIMPLE??????????????????(Job?????????)
	public void SchedulerTest16() {
		
		m_log.debug("SchedulerTest16 start.");
		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "DbmsSimpleJobErr";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest16:scheduleCronJob():DbmsSimpleJobErr");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleSimpleJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsSimpleJobErr", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						20, true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobDbmsJobErr", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest16:Exception:" + e);
		}
		m_log.debug("SchedulerTest16 end.");
	}

	// DBMS???CRON????????????????????????(???????????????Success)
	public void SchedulerTest17() {
		m_log.debug("SchedulerTest17 start.");

		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "DbmsCronJobSuccess";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest17:scheduleCronJob():DbmsCronJobSuccess");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsCronJobSuccess", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						"30 20 14 4 4 ? 2016", true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobDbmsEndStatus", jdArgsType, jdArgs);
			}
			
		} catch (Exception e) {
			m_log.error("SchedulerTest17:Exception:" + e);
		}
		m_log.debug("SchedulerTest17 end.");
	}
	
	// DBMS???CRON????????????????????????(???????????????Error)
	public void SchedulerTest18() {
		m_log.debug("SchedulerTest18 start.");

		//ID?????????
		Serializable[] jdArgs = new Serializable[2];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[2];
		jdArgs[0] = "DbmsCronJobError";
		jdArgsType[0] = String.class;
		//?????????????????????
		jdArgs[1] = "SchedulerTest";
		jdArgsType[1] = String.class;
		
		try {
			m_log.debug("SchedulerTest18:scheduleCronJob():DbmsCronJobError");
			for( String group : dbmsGroups){
				SchedulerPlugin.scheduleCronJob(SchedulerPlugin.toSchedulerTypeForDBMS(group), "DbmsCronJobError", group, HinemosTime.currentTimeMillis() + 2 * 1000,
						"30 25 14 4 4 ? 2016", true, HinemosSchedulerTest.SchedulerTestCallback.class.getName(), "scheduleRunJobDbmsEndStatus", jdArgsType, jdArgs);
			}
		} catch (Exception e) {
			m_log.error("SchedulerTest19:Exception:" + e);
		}
		m_log.debug("SchedulerTest18 end.");
	}
	
	public static class SchedulerTestCallback {
		
		public void scheduleRunJobRamCron(String arg1, String arg2) {
			m_log.debug("scheduleRunJobRamCron() arg1:" + arg1 + ", arg2:" +  arg2);
		}
		public void scheduleRunJobRamSimple(String arg1, String arg2) {
			m_log.debug("scheduleRunJobRamSimple() arg1:" + arg1 + ", arg2:" +  arg2);
		}
		public void scheduleRunJobDbmsCron(String arg1, String arg2) {
			m_log.debug("scheduleRunJobDbmsCron() arg1:" + arg1 + ", arg2:" +  arg2);
		}
		public void scheduleRunJobDbmsSimple(String arg1, String arg2) {
			m_log.debug("scheduleRunJobDbmsSimple() arg1:" + arg1 + ", arg2:" +  arg2);
		}
		public void scheduleRunJobRamStressTest(String arg1, String arg2, Integer arg3) {
			
			m_log.debug("scheduleRunJobRamStressTest() arg1:" + arg1 + ", arg2:" +  arg2 + ", arg3:" +  arg3);
			int num = (arg3 % 5) * 20;
			try {
				Thread.sleep(num);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		public void scheduleRunJobDbmsStressTest(String arg1, String arg2, Integer arg3) {
			m_log.debug("scheduleRunJobDbmsStressTest() arg1:" + arg1 + ", arg2:" +  arg2 + ", arg3:" +  arg3);
			int num = (arg3 % 5) * 20;
			try {
				Thread.sleep(num);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void scheduleRunJobClassTypeTest(String arg1, String arg2, String arg3, Boolean arg4
								, Integer arg5, Long arg6, Short arg7, Float arg8, Double arg9) {
			m_log.debug("scheduleRunJobClassTypeTest() arg1:" + arg1);
			m_log.debug("scheduleRunJobClassTypeTest() arg2:" + arg2);
			m_log.debug("scheduleRunJobClassTypeTest() arg3:" + arg3);
			m_log.debug("scheduleRunJobClassTypeTest() arg4:" + arg4);
			m_log.debug("scheduleRunJobClassTypeTest() arg5:" + arg5);
			m_log.debug("scheduleRunJobClassTypeTest() arg6:" + arg6);
			m_log.debug("scheduleRunJobClassTypeTest() arg7:" + arg7);
			m_log.debug("scheduleRunJobClassTypeTest() arg8:" + arg8);
			m_log.debug("scheduleRunJobClassTypeTest() arg9:" + arg9);
		}
		
		public void scheduleRunJobParamNumTest(String arg1, String arg2, String arg3, String arg4
				, String arg5, String arg6, String arg7, String arg8, String arg9, String arg10
				, String arg11, String arg12, String arg13, String arg14, String arg15) {
			m_log.debug("scheduleRunJobParamNumTest() arg1:" + arg1);
			m_log.debug("scheduleRunJobParamNumTest() arg2:" + arg2);
			m_log.debug("scheduleRunJobParamNumTest() arg3:" + arg3);
			m_log.debug("scheduleRunJobParamNumTest() arg4:" + arg4);
			m_log.debug("scheduleRunJobParamNumTest() arg5:" + arg5);
			m_log.debug("scheduleRunJobParamNumTest() arg6:" + arg6);
			m_log.debug("scheduleRunJobParamNumTest() arg7:" + arg7);
			m_log.debug("scheduleRunJobParamNumTest() arg8:" + arg8);
			m_log.debug("scheduleRunJobParamNumTest() arg9:" + arg9);
			m_log.debug("scheduleRunJobParamNumTest() arg10:" + arg10);
			m_log.debug("scheduleRunJobParamNumTest() arg11:" + arg11);
			m_log.debug("scheduleRunJobParamNumTest() arg12:" + arg12);
			m_log.debug("scheduleRunJobParamNumTest() arg13:" + arg13);
			m_log.debug("scheduleRunJobParamNumTest() arg14:" + arg14);
			m_log.debug("scheduleRunJobParamNumTest() arg15:" + arg15);
			
		}
		
		public void scheduleRunJobDbmsJobErr(String arg1, String arg2) throws Exception {
			m_log.debug("scheduleRunJobDbmsJobErr() arg1:" + arg1 + ", arg2:" +  arg2);
			throw new Exception();
		}
		
		public void scheduleRunJobDbmsEndStatus(String arg1, String arg2) throws Exception {
			m_log.debug("scheduleRunJobDbmsEndStatus() arg1:" + arg1 + ", arg2:" +  arg2);
			throw new Exception();
		}
		
	}

}
