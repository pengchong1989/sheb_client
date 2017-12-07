﻿package com.nms.ui.ptn.alarm.controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nms.db.bean.alarm.CurrentAlarmInfo;
import com.nms.db.bean.alarm.WarningLevel;
import com.nms.db.bean.equipment.port.E1Info;
import com.nms.db.bean.equipment.port.PortInst;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.bean.path.Segment;
import com.nms.db.bean.ptn.path.ServiceInfo;
import com.nms.db.bean.ptn.path.ces.CesInfo;
import com.nms.db.bean.ptn.path.eth.ElanInfo;
import com.nms.db.bean.ptn.path.eth.ElineInfo;
import com.nms.db.bean.ptn.path.eth.EtreeInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.tunnel.Lsp;
import com.nms.db.bean.ptn.path.tunnel.Tunnel;
import com.nms.db.enums.EObjectType;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EServiceType;
import com.nms.model.alarm.CurAlarmService_MB;
import com.nms.model.alarm.WarningLevelService_MB;
import com.nms.model.equipment.port.E1InfoService_MB;
import com.nms.model.equipment.port.PortService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.ResultString;
import com.nms.service.impl.util.WhImplUtil;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.DateUtil;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ListingFilter;
import com.nms.ui.manager.MyActionListener;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.ptn.alarm.model.CurrentAlarmFilter;
import com.nms.ui.ptn.alarm.view.CurrentAlarmFilterDialog;
import com.nms.ui.ptn.alarm.view.CurrentAlarmPanel;

/**
 * 当前告警事件处理类
 * 
 * @author lp
 * 
 */
public class CurrentAlarmController {
	private CurrentAlarmPanel view = null;
	private CurrentAlarmFilter filter = null;
	private List<CurrentAlarmInfo> currAlarmList = new ArrayList<CurrentAlarmInfo>();
	private int direction = 1;//0/1 = 上一页/下一页
	private int totalPage = 1;//总页数
	private int currPage = 1;//当前页数
	private int minId = 0;//当前页面告警数据的最小id
	private int maxId = 0;//当前页面告警数据的最大id
	private int pageCount = 3000;//每页大小
	public CurrentAlarmController(CurrentAlarmPanel view) {
		this.view = view;
		this.init();
		this.refresh();
	}
	
	private void init() {
		this.direction = 1;
		this.totalPage = 1;
		this.currPage = 1;
		this.maxId = 0;
		this.minId = 0;
		this.view.getTotalPageLabel().setText(this.totalPage + "");
		this.view.getCurrPageLabel().setText(this.currPage + "");
	}
			
	/**
	 * 同步设备当前告警
	 * 只有在线的网元才进行同步操作
	 */
	public void synchro(){
		CurAlarmService_MB service = null;
		SiteService_MB siteService = null;
		try {
			DispatchUtil alarmOperationImpl = new DispatchUtil(RmiKeys.RMI_ALARM);
			service = (CurAlarmService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CurrentAlarm);
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			List<Integer> siteIdList = service.querySiteIdList(); 
			for(int i=0;i<siteIdList.size();i++){	
				int neId = siteIdList.get(i);
				if(siteService.queryNeStatus(neId) == 1)
				{
					alarmOperationImpl.synchroCurrentAlarm(neId);	
					UiUtil.insertOperationLog(EOperationLogType.ALARMSYSCHRO.getValue(),ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS));  
				}
			}	
			WhImplUtil util = new WhImplUtil();
			String str = util.getNeNames(siteIdList);
			if(str.equals("")){
				DialogBoxUtil.succeedDialog(null, ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS));
			}else{
				DialogBoxUtil.succeedDialog(null, ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS)+","+str+ResultString.NOT_ONLINE_SUCCESS);
			}
			this.init();
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally{
			UiUtil.closeService_MB(service);
			UiUtil.closeService_MB(siteService);
		}
	}
		
	/**
	 * 清空过滤条件
	 */
	public void clearFilter() {
		this.filter = null;
		this.view.clear();
		this.init();
		this.refresh();
	}
	
	/**
	 * 打开设置过滤条件对话框
	 */
	public void openFilterDialog() {
		final CurrentAlarmFilterDialog filterDialog = new CurrentAlarmFilterDialog(1);
		if(ResourceUtil.language.equals("zh_CN")){
			filterDialog.setSize(new Dimension(550, 620));
		}else{
			filterDialog.setSize(new Dimension(570, 620));
		}
		
		filterDialog.setLocation(UiUtil.getWindowWidth(filterDialog.getWidth()), UiUtil.getWindowHeight(filterDialog.getHeight()));
		filterDialog.getConfirm().addActionListener(new MyActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent evt) {
				if (filterDialog.validateParams()) {
					CurrentAlarmController.this.setFilter(filterDialog);
				}
			}
			@Override
			public boolean checking() {
				return true;
			}
		});
//		filterDialog.addWindowListener(new WindowAdapter() {
//			@Override
//			public void windowClosed(WindowEvent e) {
//				filterDialog.dispose();
//			}
//		});
		filterDialog.setVisible(true);
	}
	
	/**
	 * 设置过滤条件，并显示查询结果
	 */
	private void setFilter(CurrentAlarmFilterDialog dialog) {
		Map<Integer, List<Integer>> siteId2SlotIds = null;
		try {
			filter = dialog.get();
			if (filter.getObjectType() != null && filter.getObjectType() == EObjectType.SITEINST) {
//				for (SiteInst siteInst : filter.getSiteInsts()) {//查询某网元当前告警
//				}
			}else if(filter.getObjectType() != null && filter.getObjectType() == EObjectType.SLOTINST){
				siteId2SlotIds = new HashMap<Integer, List<Integer>>();
				// 封装网元与槽位的映射关系
				for (int i = 0; i < filter.getSlotInsts().size(); i++) {
					int siteId = filter.getSiteInsts().get(i).getSite_Inst_Id();
					if (siteId2SlotIds.get(siteId) == null) {
						siteId2SlotIds.put(siteId, new ArrayList<Integer>());
					}
					siteId2SlotIds.get(siteId).add(filter.getSlotInsts().get(i).getId());
				}
				for (Integer id : siteId2SlotIds.keySet()) {
					if (siteId2SlotIds.get(id) != null && siteId2SlotIds.get(id).size() > 0) {
						for(Integer slotId :siteId2SlotIds.get(id)){//查询某网元某盘当前告警
						}
					}
				}
			}
			this.view.setFilterInfos(dialog.getFilterInfo());
			this.getAlarmCode();
			this.init();
			this.refresh();
//			currAlarmList = getCurrentAlarmByFilter();
//			
//			this.view.getBox().clear();
//			this.view.initData(currAlarmList);
//			this.view.updateUI();
			//添加日志记录
			AddOperateLog.insertOperLog(dialog.getConfirm(), EOperationLogType.CURRENTALARMFILTERSELECT.getValue(), ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS));
			DialogBoxUtil.succeedDialog(dialog, ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS));
			dialog.dispose();
		} catch (Exception e) {
			dialog.dispose();
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	
	private void getAlarmCode() {
		WarningLevelService_MB service = null;
		try {
			service = (WarningLevelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.WarningLevel);
			List<WarningLevel> levelList = service.select();
			if(levelList != null){
				levelList = this.filterOAMEvent(levelList);
				for (String type : this.filter.getAlarmTypeList()) {
					for (WarningLevel level : levelList) {
						if(type.equals(level.getWarningnote())){ 
							if(this.filter.getWarningtype() > 0){
								if(level.getWarningtype() == this.filter.getWarningtype())
									this.filter.getAlarmCodeList().add(level.getWarningcode());
							}else{
								this.filter.getAlarmCodeList().add(level.getWarningcode());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
	}
	
	/**
	 * 过滤掉OAM事件
	 */
	private List<WarningLevel> filterOAMEvent(List<WarningLevel> levelList) {
		List<WarningLevel> warningList = new ArrayList<WarningLevel>();
		List<Integer> oamEventCodeList = new ArrayList<Integer>();
		oamEventCodeList.add(35);
		oamEventCodeList.add(68);
		oamEventCodeList.add(69);
		oamEventCodeList.add(70);
		oamEventCodeList.add(71);
		oamEventCodeList.add(211);
		oamEventCodeList.add(212);
		for (WarningLevel level : levelList) {
			if(!oamEventCodeList.contains(level.getWarningcode())){
				warningList.add(level);
			}
		}
		return warningList;
	}

	
	/**
	 * 刷新按钮事件处理方法 先设置过滤条件后，才能显示刷新结果
	 */
	public void refresh() {
		CurAlarmService_MB service = null;
		try {
			service = (CurAlarmService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CurrentAlarm);
			if(this.maxId == 0){
				this.updateUI(service.selectMaxId()+1, 1);
			}else{
				this.updateUI(this.maxId+1, 3);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
//		CurAlarmService service = null;
//		ListingFilter filter=null;
//		List<CurrentAlarmInfo> alarmInfos = new ArrayList<CurrentAlarmInfo>();
//		List<CurrentAlarmInfo> currentAlarmInfoList = new ArrayList<CurrentAlarmInfo>();
		try {
//			if (filter == null) {
//				filter=new ListingFilter();
//				service = (CurAlarmService) ConstantUtil.serviceFactory.newService(Services.CurrentAlarm);
//				currentAlarmInfoList.addAll(service.select());
//				currentAlarmInfoList.addAll(service.alarmByAlarmLevel());
//				currAlarmList =(List<CurrentAlarmInfo>) filter.filterList(currentAlarmInfoList);
//				
//			} else {
//				// 根据过滤条件查询
//				currAlarmList = getCurrentAlarmByFilter();
//			}
//			//无oam事件上报，暂时这样过滤
//			for(CurrentAlarmInfo alarmInfo : currAlarmList){
//				if(!("OAM_PEER_ERR_SYMBOL".equals(alarmInfo.getWarningLevel().getWarningname())
//						||"OAM_PEER_ERR_FRAME".equals(alarmInfo.getWarningLevel().getWarningname())
//						||"OAM_PEER_ERR_FRAME_PERIOD".equals(alarmInfo.getWarningLevel().getWarningname())
//						||"OAM_PEER_ERR_FRAME_SECOND".equals(alarmInfo.getWarningLevel().getWarningname())
//						||"OAM_PEER_DISCOVERY".equals(alarmInfo.getWarningLevel().getWarningname())
//						||"OAM_LOOPBACK_TIMEOUT".equals(alarmInfo.getWarningLevel().getWarningname()))){
//						alarmInfos.add(alarmInfo);
//				}
//			}
//			//是否需要告警反转
//			this.view.getBox().clear();
//			this.view.initData(alarmInfos);
//			this.view.updateUI();
//			AlarmSeverity.CLEARED.setColor(Color.green);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
//			UiUtil.closeService(service);
//			filter=null;
		}
	}
	
//	private boolean isLevel(int mylevel, List<Integer> levelList)
//	{
//		boolean islevel = false;
//		for(int level: levelList)
//		{
//			if(level == mylevel)
//			{
//				islevel = true;
//				break;
//			}
//		}
//		return islevel;
//	}
	
//	private List<CurrentAlarmInfo> getCurrentAlarmByFilter() throws Exception {
//		// key为网元数据库id，value为槽位的集合
//		Map<Integer, List<Integer>> siteId2SlotIds = null;
//		CurAlarmService_MB service = null;
//		List<CurrentAlarmInfo> curAlarmInfos = new ArrayList<CurrentAlarmInfo>();
//		List<CurrentAlarmInfo> levAlarmInfos = new ArrayList<CurrentAlarmInfo>();
//		List<Integer> levelList = new ArrayList<Integer>();
//		DateUtil dataUtil = new DateUtil();
//		try {
//			service = (CurAlarmService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CurrentAlarm);
//			if (filter.getObjectType() != null && filter.getObjectType() == EObjectType.SITEINST) {
//				List<Integer> siteIdList = new ArrayList<Integer>();
//				
//				for (SiteInst site : filter.getSiteInsts()) {
//					siteIdList.add(Integer.valueOf(site.getSite_Inst_Id()));
//				}
//				
//			curAlarmInfos = service.queryCurBySites(siteIdList);
//			} else if (filter.getObjectType() != null && filter.getObjectType() == EObjectType.SLOTINST) {
//				siteId2SlotIds = new HashMap<Integer, List<Integer>>();
//				// 封装网元与槽位的映射关系
//				for (int i = 0; i < filter.getSlotInsts().size(); i++) {
//					int siteId = filter.getSiteInsts().get(i).getSite_Inst_Id();
//					if (siteId2SlotIds.get(siteId) == null) {
//						siteId2SlotIds.put(siteId, new ArrayList<Integer>());
//					}
//					siteId2SlotIds.get(siteId).add(filter.getSlotInsts().get(i).getId());
//				}
//				
//				for (Integer id : siteId2SlotIds.keySet()) {
//					if (siteId2SlotIds.get(id) != null&& siteId2SlotIds.get(id).size() > 0) {
//						List<CurrentAlarmInfo> curAlarmInfoLists = service.queryCurBySlots(id, siteId2SlotIds.get(id));
//						if (curAlarmInfoLists != null &&  curAlarmInfoLists.size() > 0) {
//							curAlarmInfos.addAll(curAlarmInfoLists);
//						}
//					}
//				}
//			}else{
//				//单独查询客户端 用户输入错误而产生的告警
//				curAlarmInfos.addAll(service.alarmByAlarmLevel());
//			}
//			
//			//设置级别过滤
//			levelList = filter.getAlarmLevel();
//			
//			for(CurrentAlarmInfo info : curAlarmInfos){
//				if(filter.getAlarmState().equals("1") && info.getWarningLevel()!= null){
//					for(String aramTypeName: filter.getAlarmTypeList()){
//						if(info.getWarningLevel().getWarningnote().equalsIgnoreCase(aramTypeName)&& info.getAckUser()!= null 
//								&& isLevel(info.getWarningLevel_temp(),levelList)){
//							
//							checkUpTime(levAlarmInfos,info,dataUtil);
//							
//						}
//					}
//				}else if(filter.getAlarmState().equals("2") && info.getWarningLevel()!= null){
//					for(String aramTypeName: filter.getAlarmTypeList()){
//						if(info.getWarningLevel().getWarningnote().equalsIgnoreCase(aramTypeName)&& info.getAckUser()== null
//								&& isLevel(info.getWarningLevel_temp(),levelList)){
//							
//							   checkUpTime(levAlarmInfos,info,dataUtil);
//						}
//					}	
//				}
//			}
//			
//		} catch (Exception e) {
//			ExceptionManage.dispose(e,this.getClass());
//			throw e;
//		} finally {
//			UiUtil.closeService_MB(service);
//			siteId2SlotIds = null;
//		}
//		
//		if(levelList.size() == 0)
//		{
//			return curAlarmInfos;
//		}
//		else
//		{
//			return levAlarmInfos;
//		}
//	}

	public CurrentAlarmPanel getView() {
		return view;
	}

	public List<CurrentAlarmInfo> getCurrInfos() {
		return currAlarmList;
	}

	public void setCurrInfos(List<CurrentAlarmInfo> currInfos) {
		this.currAlarmList = currInfos;
	}
	
	private void checkUpTime(List<CurrentAlarmInfo> levAlarmInfos,CurrentAlarmInfo info,DateUtil dataUtil){
		
		try {
			
             if(validateParamsAll(info,dataUtil)){
            	 levAlarmInfos.add(info);
             }	
			
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}
	}
	
	private boolean validateParamsAll(CurrentAlarmInfo info,DateUtil dataUtil){
		boolean flag = false;
		try {
			if(filter != null){
				//发生时间
                 if(!validateParamsTime(info.getRaisedTime(),dataUtil,filter.getHappenTime(),filter.getHappenEndTime())){
                	 return false;
                 }				
                //确定时间
                 if(!validateParamsTime(info.getAckTime(),dataUtil,filter.getEnsureTime(),filter.getEnsureEndTime())){
                	 return false;
                 }
               //清除时间
                 if(!validateParamsTime(info.getClearedTime(),dataUtil,filter.getClearTime(),filter.getClearEndTime())){
                	 return false;
                 }
                 //告警类型
                 if(filter.getWarningtype() > 0 && (info.getWarningLevel().getWarningtype() != filter.getWarningtype())){
                	 return false;
                 }
                 
                 if(filter.getEnsureUser() != null){
                	 if(!filter.getEnsureUser().equals(info.getAckUser())){
                		 return false;
                	 }
                 }
			}
			flag = true;
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}
		return flag ; 
	}
	
	private boolean validateParamsTime(Date neTime,DateUtil dataUtil,String timeString,String timeEndString){
		boolean flga = false;
		long startTime = 0l;
		long startEndTime = 0l;
		try {
			if(timeString != null && !"".equals(timeString)){
				 startTime = dataUtil.updateTimeToLong(timeString, DateUtil.FULLTIME);
				 startEndTime = dataUtil.updateTimeToLong(timeEndString, DateUtil.FULLTIME);
				 if(neTime == null|| (startTime > neTime.getTime() || neTime.getTime() > startEndTime)){
					 return false;
				 }
			}
			flga = true;
		} catch (Exception e) {
			ExceptionManage.dispose(e, getClass());
		}
		return flga;
	}

	public void prevPage() {
		this.direction = 0;
		this.pageTurning();
		if(this.currPage > 1){
			this.currPage -= 1;
		}
		if(this.currPage == 1){
			this.view.getPrevPageBtn().setEnabled(false);
		}
		this.view.getNextPageBtn().setEnabled(true);
		this.view.getCurrPageLabel().setText(this.currPage + "");
	}
	
	public void nextPage() {
		this.direction = 1;
		this.pageTurning();
		if(this.currPage < this.totalPage){
			this.currPage += 1;
		}
		this.view.getPrevPageBtn().setEnabled(true);
		if(this.currPage == this.totalPage){
			this.view.getNextPageBtn().setEnabled(false);
		}
		this.view.getCurrPageLabel().setText(this.currPage + "");
	}

	/**
	 * 翻页
	 */
	private void pageTurning(){
		try {
			int id = 0;
			if(this.direction == 0){
				id = this.maxId;
			}else{
				id = this.minId;
			}
			this.updateUI(id, this.direction);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}
	
	private void updateUI(int id, int type){
		CurAlarmService_MB service = null;
		ListingFilter listFilter = null;
		try {
			this.currAlarmList.clear();
			listFilter = new ListingFilter(); 
			service = (CurAlarmService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CurrentAlarm);
			List<Integer> siteIdList = null;
			if (filter == null) {
				siteIdList = listFilter.getSiteIdListAll();
			}else{
				siteIdList = this.getSiteIdList();
			}
			List<Integer> idList = service.selectCurrAlarmId(this.filter, siteIdList);
			id = this.getCurrAlarmId(idList, this.maxId == 0 ? 0 : this.currPage, type);
			if(id != -1){
				this.currAlarmList.addAll(service.selectByPage(type == 3?1:this.direction, id, this.filter, siteIdList, this.pageCount));
			}
			int count = idList.size();
			this.totalPage = count%this.pageCount == 0 ? count/this.pageCount : (count/this.pageCount+1);
			//如果总页数小于当前页数，说明数据被转储，要重新查询
			if(this.totalPage == 0){
				this.totalPage = 1;
			}
			if(this.totalPage > 0 && this.totalPage < this.currPage){
				this.init();
				this.refresh();
			}
			if(this.totalPage == 0){
				this.totalPage = 1;
			}
			this.view.getTotalPageLabel().setText(this.totalPage + "");
			this.view.getCurrPageLabel().setText(this.currPage + "");
			this.view.getPrevPageBtn().setEnabled(true);
			this.view.getNextPageBtn().setEnabled(true);
			if(this.currPage == 1){
				this.view.getPrevPageBtn().setEnabled(false);
			}
			if(this.currPage == this.totalPage){
				this.view.getNextPageBtn().setEnabled(false);
			}
			if(this.currAlarmList.size() > 0){
				int minid = this.currAlarmList.get(0).getId();
				int maxid = this.currAlarmList.get(this.currAlarmList.size() - 1).getId();
				if(minid < maxid){
					this.minId = minid;
					this.maxId = maxid;
				}else{
					List<CurrentAlarmInfo> alarmList = new ArrayList<CurrentAlarmInfo>();
					for (int i = (this.currAlarmList.size()-1); i >= 0; i--) {
						alarmList.add(this.currAlarmList.get(i));
					}
					this.currAlarmList.clear();
					this.currAlarmList.addAll(alarmList);
					this.minId = maxid;
					this.maxId = minid;
				}
			}
			this.view.getBox().clear();
			List<CurrentAlarmInfo> cList = new ArrayList<CurrentAlarmInfo>();
			for (int i = this.currAlarmList.size()-1; i >=0; i--) {
				cList.add(this.currAlarmList.get(i));
			}
			//根据告警源过滤
			if(filter != null && filter.getAlarmSrc() != 0){
				List<CurrentAlarmInfo> list = new ArrayList<CurrentAlarmInfo>();
				int alarmSrc = filter.getAlarmSrc();
				Object alarmBusi = filter.getAlarmBusiness();
				E1InfoService_MB e1Service = null;
				PortService_MB portService = null;
				try {
					portService = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
					e1Service = (E1InfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.E1Info);
					
					for(CurrentAlarmInfo alarm : cList){
						if(alarmSrc == 1){// 端口
							if((alarm.getObjectType().getValue() == EObjectType.PORT.getValue())){
								if(alarmBusi != null){
									PortInst port = (PortInst)alarmBusi;
									if(alarm.getObjectId() == port.getNumber()){
										list.add(alarm);
									}
								}else{
									list.add(alarm);
								}
							}else if(alarm.getObjectType().getValue() == EObjectType.E1.getValue()){
								if(alarmBusi != null){
									PortInst port = (PortInst)alarmBusi;
									E1Info e1Con = new E1Info();
									e1Con.setPortId(port.getPortId());
									try {
										e1Con = e1Service.selectByCondition(e1Con).get(0);
									} catch (Exception e) {
										ExceptionManage.dispose(e, this.getClass());
									}
									if(("E1线路"+e1Con.getLegId()).equals(alarm.getObjectName())){
										list.add(alarm);
									}
								}else{
									list.add(alarm);
								}
							}
						}else if(alarmSrc == 2){// 段
							if((alarm.getObjectType().getValue() == EObjectType.TMS_OAM.getValue())){
								if(alarmBusi != null){
									PortInst portCon = new PortInst();
									portCon.setSiteId(alarm.getSiteId());
									portCon.setNumber(alarm.getObjectId());
									List<PortInst> portList = portService.select(portCon);
									if(portList != null && portList.size() == 1){
										Segment segment = (Segment)alarmBusi;
										portCon = portList.get(0);
										if((segment.getASITEID() == portCon.getSiteId() && segment.getAPORTID() == portCon.getPortId()) || 
												segment.getZSITEID() == portCon.getSiteId() && segment.getZPORTID() == portCon.getPortId()){
											list.add(alarm);
										}
									}
								}else{
									list.add(alarm);
								}
							}
						}else if(alarmSrc == 3){// Tunnel
							if((alarm.getObjectType().getValue() == EObjectType.TUNNEL.getValue())){
								if(alarmBusi != null){
									Tunnel tunnel = (Tunnel) alarmBusi;
									for(Lsp lsp : tunnel.getLspParticularList()){
										if((lsp.getASiteId() == alarm.getSiteId() && lsp.getAtunnelbusinessid() == alarm.getObjectId()) ||
												(lsp.getZSiteId() == alarm.getSiteId() && lsp.getZtunnelbusinessid() == alarm.getObjectId())){
											list.add(alarm);
										}
									}
								}else{
									list.add(alarm);
								}
							}
							if(alarm.getObjectType().getValue() == EObjectType.LSP.getValue()){
								if(alarmBusi != null){
									Tunnel tunnel = (Tunnel) alarmBusi;
									if(tunnel.getProtectTunnelId() > 0){
										Tunnel pTunnel = tunnel.getProtectTunnel();
										if((pTunnel.getASiteId() == alarm.getSiteId() && pTunnel.getAprotectId() == alarm.getObjectId()) ||
												(pTunnel.getZSiteId() == alarm.getSiteId() && pTunnel.getZprotectId() == alarm.getObjectId())){
											list.add(alarm);
										}
									}
								}else{
									list.add(alarm);
								}
							}
						}else if(alarmSrc == 4){// Pw
							if((alarm.getObjectType().getValue() == EObjectType.PW.getValue())){
								if(alarmBusi != null){
									PwInfo pw = (PwInfo) alarmBusi;
									if((pw.getASiteId() == alarm.getSiteId() && pw.getApwServiceId() == alarm.getObjectId()) ||
											(pw.getZSiteId() == alarm.getSiteId() && pw.getZpwServiceId() == alarm.getObjectId())){
										
									}
								}else{
									list.add(alarm);
								}
							}
						}else if(alarmSrc == 5){// vpws业务
							if(alarm.getObjectType().getValue() == EObjectType.VPWS.getValue()){
								if(alarmBusi != null){
									if(alarmBusi instanceof ElineInfo){
										ElineInfo eline = (ElineInfo) alarmBusi;
										if((eline.getaSiteId() == alarm.getSiteId() && eline.getaXcId() == alarm.getObjectId()) ||
										(eline.getzSiteId() == alarm.getSiteId() && eline.getzXcId() == alarm.getObjectId())){
											list.add(alarm);
										}
									}else if(alarmBusi instanceof CesInfo){
										CesInfo ces = (CesInfo) alarmBusi;
										if((ces.getaSiteId() == alarm.getSiteId() && ces.getAxcId() == alarm.getObjectId()) ||
										(ces.getzSiteId() == alarm.getSiteId() && ces.getZxcId() == alarm.getObjectId())){
											list.add(alarm);
										}
									}
								}else{
									list.add(alarm);
								}
							}
						}else if(alarmSrc == 6){// vpls业务
							if((alarm.getObjectType().getValue() == EObjectType.VPLS.getValue())){
								if(alarmBusi != null){
									List<ServiceInfo> serviceList = (List<ServiceInfo>) alarmBusi;
									for(ServiceInfo serviceInfo : serviceList){
										if(serviceInfo.getServiceType() == EServiceType.ETREE.getValue()){
											EtreeInfo etree = (EtreeInfo) serviceInfo;
											if((etree.getRootSite() == alarm.getSiteId() && etree.getaXcId() == alarm.getObjectId()) ||
													(etree.getBranchSite() == alarm.getSiteId() && etree.getzXcId() == alarm.getObjectId())){
												list.add(alarm);
											}
										}else if(serviceInfo.getServiceType() == EServiceType.ELAN.getValue()){
											ElanInfo elan = (ElanInfo) serviceInfo;
											if((elan.getaSiteId() == alarm.getSiteId() && elan.getAxcId() == alarm.getObjectId()) ||
													(elan.getzSiteId() == alarm.getSiteId() && elan.getZxcId() == alarm.getObjectId())){
												list.add(alarm);
											}
										}
									}
								}else{
									list.add(alarm);
								}
							}
						}
					}
				} catch (Exception e) {
					ExceptionManage.dispose(e, this.getClass());
				} finally {
					UiUtil.closeService_MB(e1Service);
					UiUtil.closeService_MB(portService);
				}
				cList.clear();
				cList.addAll(list);
			}
			
			this.currAlarmList.clear();
			this.currAlarmList.addAll(cList);
			
			//以时间排序
			this.sortListByTime(cList);
			this.view.initData(cList);
			this.view.updateUI();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void sortListByTime(List<CurrentAlarmInfo> cList) {
		Collections.sort(cList, new SortClass());
	}
	
	@SuppressWarnings("unchecked")
	private class SortClass implements Comparator{
		public int compare(Object arg0,Object arg1){
			CurrentAlarmInfo c0 = (CurrentAlarmInfo)arg0;
			CurrentAlarmInfo c1 = (CurrentAlarmInfo)arg1;
		    int flag = c1.getRaisedTime().compareTo(c0.getRaisedTime());
		    return flag;
		}
	}

	private int getCurrAlarmId(List<Integer> idList, int currentPage, int type){
		if(!idList.isEmpty()){
			if(currentPage == 0){
				return idList.get(0)+1;
			}else{
				//下一页,要取区间的最小值
				if(type == 1){
					if(idList.size() > pageCount*currentPage){
						return idList.get(pageCount*currentPage-1);
					}else if(idList.size() > pageCount*(currentPage-1)){
						return idList.get(pageCount*(currentPage-1));
					}else{
						return -1;
					}
				}else if(type == 0){
					//上一页，要取区间的最大值
					if(idList.size() > pageCount*(currentPage-1)){
						return idList.get(pageCount*(currentPage-1));
					}else if(currentPage > 1 && idList.size() > pageCount*(currentPage-2)){
						return idList.get(idList.size()-1)-1;
					}else{
						return -1;
					}
				}else{
					//当前页刷新
					if(idList.size() > pageCount*(currentPage-1)){
						if(currentPage == 1){
							return idList.get(0)+1;
						}else{
							return idList.get(pageCount*(currentPage-1)-1);
						}
					}else{
						return -1;
					}
				}
			}
		}else{
			return 0;
		}
	}
	
	private List<Integer> getSiteIdList() {
		List<Integer> siteIdList = new ArrayList<Integer>();
		for (SiteInst site : this.filter.getSiteInsts()) {
			siteIdList.add(Integer.valueOf(site.getSite_Inst_Id()));
		}
		return siteIdList;
	}
}
