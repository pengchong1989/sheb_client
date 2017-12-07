﻿package com.nms.ui.ptn.ne.eline.controller;

import java.util.ArrayList;
import java.util.List;

import com.nms.db.bean.ptn.path.ServiceInfo;
import com.nms.db.bean.ptn.path.eth.ElineInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.port.AcPortInfo;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EOperationLogType;
import com.nms.model.ptn.path.eth.ElineInfoService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.port.AcPortInfoService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.ResultString;
import com.nms.service.impl.util.SiteUtil;
import com.nms.service.impl.util.WhImplUtil;
import com.nms.ui.filter.impl.EthNeFilterDialog;
import com.nms.ui.frame.AbstractController;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.DateUtil;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ListingFilter;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.keys.StringKeysTab;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.ptn.ne.camporeData.CamporeDataDialog;
import com.nms.ui.ptn.ne.eline.view.ElineEditDialog;
import com.nms.ui.ptn.ne.eline.view.ElinePanel;

public class ElineController extends AbstractController {

	private ElinePanel elinePanel;
	private ElineInfo elineInfo=null;
	public ElineController(ElinePanel elinPanel) {
		this.setElinePanel(elinPanel);
	}

	@Override
	public void refresh() throws Exception {

		ElineInfoService_MB elineServiceMB = null;
		List<ElineInfo> infos = null;
		ListingFilter filter = null;
		try {
			if(null==this.elineInfo){
				this.elineInfo=new ElineInfo();
			}
			filter = new ListingFilter();
			elineServiceMB = (ElineInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Eline);
			this.elineInfo.setaSiteId(ConstantUtil.siteId);
			infos = (List<ElineInfo>) filter.filterList(elineServiceMB.selectElineByCondition(this.elineInfo));
			this.getElinePanel().clear();
			this.getElinePanel().getPwElinePanel().clear();
			this.getElinePanel().getAcElinePanel().clear();
			this.getElinePanel().initData(infos);
			this.getElinePanel().updateUI();
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(elineServiceMB);
			infos = null;
		}

	}
	
	@Override
	public void openFilterDialog() throws Exception {
		if(null==this.elineInfo){
			this.elineInfo=new ElineInfo();
		}
		new EthNeFilterDialog(this.elineInfo);
		this.refresh();
	}
	
	// 清除过滤
	public void clearFilter() throws Exception {
		this.elineInfo=null;
		this.refresh();
	}

	@Override
	public void delete() throws Exception {

		List<ElineInfo> elineInfoList = null;
		DispatchUtil elineDispatch = null;
		String resultStr = null;
		try {
			elineInfoList = this.getElinePanel().getAllSelect();
			elineDispatch = new DispatchUtil(RmiKeys.RMI_ELINE);
			resultStr = elineDispatch.excuteDelete(elineInfoList);
			DialogBoxUtil.succeedDialog(this.getElinePanel(), resultStr);
			//添加日志记录
			for (ElineInfo elineInfo : elineInfoList) {
				AddOperateLog.insertOperLog(null, EOperationLogType.ELINEDELETE.getValue(), resultStr,
						null, null, ConstantUtil.siteId, elineInfo.getName(), null);
			}
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			resultStr = null;
			elineInfoList = null;
			elineDispatch = null;
		}

	}
	
	@Override
	public boolean deleteChecking() {
		List<ElineInfo> elineInfoList = null;
		boolean flag = false;
		List<Integer> siteIds = null;
		try {
			elineInfoList = this.getElinePanel().getAllSelect();

			for (ElineInfo elineInfo : elineInfoList) {
				if (elineInfo.getIsSingle() == 0) {
					flag = true;
					break;
				}
			}
			if (flag) {
				DialogBoxUtil.errorDialog(this.getElinePanel(), ResourceUtil.srcStr(StringKeysTip.TIP_DELETE_NODE));
				return false;
			}else{
				SiteUtil siteUtil = new SiteUtil();
				if(1==siteUtil.SiteTypeOnlineUtil(ConstantUtil.siteId)){
					WhImplUtil wu = new WhImplUtil();
					siteIds = new ArrayList<Integer>();
					siteIds.add(ConstantUtil.siteId);
					String str=wu.getNeNames(siteIds);
					DialogBoxUtil.errorDialog(this.getElinePanel(), ResourceUtil.srcStr(StringKeysTip.TIP_NOT_DELETEONLINE)+""+str+ResourceUtil.srcStr(StringKeysTip.TIP_ONLINENOT_DELETEONLINE));
					return false;  		    		
					}
			}
			flag = true;
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
		return flag;
	}

	/**
	 * 激活处理事�?	 */
	public void doActive() {
		List<ElineInfo> infos = null;
		String result = null;
		DispatchUtil dispatch = null;
		try {
			infos = this.elinePanel.getAllSelect();
			int failCount = 0;
			if (infos != null && infos.size() > 0) {
				dispatch = new DispatchUtil(RmiKeys.RMI_ELINE);
				for (ElineInfo info : infos) {
					info.setActiveStatus(EActiveStatus.ACTIVITY.getValue());
					info.setActivatingTime(DateUtil.getDate(DateUtil.FULLTIME));
					result = dispatch.excuteUpdate(info);
					if(result == null || !result.contains(ResultString.CONFIG_SUCCESS)){
						failCount++;
					}
					//添加日志记录*************************/
					AddOperateLog.insertOperLog(null, EOperationLogType.ELINESINGACTIVE.getValue(), result, null, null, ConstantUtil.siteId, info.getName(), null);
					//************************************/
				}
				result = ResourceUtil.srcStr(StringKeysTip.TIP_BATCH_CREATE_RESULT);
				result = result.replace("{C}", (infos.size()-failCount) + "");
				result = result.replace("{S}", failCount + "");
			}
			String str = this.getOfflineSiteIdNames(infos);
			if(!str.equals("")){
				result += ","+str+ResultString.NOT_ONLINE_SUCCESS;
			}
			DialogBoxUtil.succeedDialog(this.elinePanel, result);  
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			infos = null;
			dispatch = null;
		}
	}
	
	private String getOfflineSiteIdNames(List<ElineInfo> elineList) throws Exception {
		List<Integer> siteIds = null;
		String str = "";
		try {
			siteIds = new ArrayList<Integer>();
			for (ElineInfo eline : elineList) {
				siteIds.add(eline.getaSiteId());
				siteIds.add(eline.getzSiteId());
			}
			str = new WhImplUtil().getNeNames(siteIds);
		} catch (Exception e) {
			throw e;
		}
		return str;
	}

	/**
	 * 去激活处理事�?	 */
	public void doUnActive() {
		List<ElineInfo> infos = null;
		String result = null;
		DispatchUtil dispatch = null;
		try {
			infos = this.elinePanel.getAllSelect();
			int failCount = 0;
			if (infos != null && infos.size() > 0) {
				dispatch = new DispatchUtil(RmiKeys.RMI_ELINE);
				for (ElineInfo info : infos) {
					info.setActiveStatus(EActiveStatus.UNACTIVITY.getValue());
					info.setActivatingTime(null);
					result = dispatch.excuteUpdate(info);
					if(result == null || !result.contains(ResultString.CONFIG_SUCCESS)){
						failCount++;
					}
					//添加日志记录*************************/
					AddOperateLog.insertOperLog(null, EOperationLogType.ELINESINGNOACTIVE.getValue(), result, null, null, ConstantUtil.siteId, info.getName(), null);
					//************************************/
				}
				result = ResourceUtil.srcStr(StringKeysTip.TIP_BATCH_CREATE_RESULT);
				result = result.replace("{C}", (infos.size()-failCount) + "");
				result = result.replace("{S}", failCount + "");
			}
			String str = this.getOfflineSiteIdNames(infos);
			if(!str.equals("")){
				result += ","+str+ResultString.NOT_ONLINE_SUCCESS;
			}
			DialogBoxUtil.succeedDialog(this.elinePanel, result);
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			infos = null;
			dispatch = null;
		}
	}

	/**
	 * 选中一条记录后，查看详细信�?	 */
	@Override
	public void initDetailInfo() {
		try {
			initInfoData();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}

	private void initInfoData() throws Exception {

		ElineInfo elineinfo = null;
		try {
			elineinfo = this.elinePanel.getSelect();

			this.initPwData(elineinfo);
			this.initAcData(elineinfo);
		} catch (Exception e) {
			throw e;
		}

	}

	private void initPwData(ElineInfo elineinfo) throws Exception {
		PwInfoService_MB pwInfoServiceMB = null;
		PwInfo pwinfo = null;
		List<PwInfo> pwinfoList = null;
		try {
			// 查询eline下的pw信息
			pwInfoServiceMB = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			pwinfo = new PwInfo();
			pwinfo.setPwId(elineinfo.getPwId());
			pwinfo = pwInfoServiceMB.selectBypwid_notjoin(pwinfo);
			pwinfoList = new ArrayList<PwInfo>();
			pwinfoList.add(pwinfo);

			this.getElinePanel().getPwElinePanel().initData(pwinfoList);
			this.getElinePanel().updateUI();
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(pwInfoServiceMB);
			pwinfo = null;
			pwinfoList = null;
		}
	}

	private void initAcData(ElineInfo elineinfo) throws Exception {

		AcPortInfoService_MB acInfoServiceMB = null;
		AcPortInfo acPortInfo = null;
		List<AcPortInfo> acportInfoList = null;
		try {
			acInfoServiceMB = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			acPortInfo = new AcPortInfo();
			if (elineinfo.getaSiteId() == ConstantUtil.siteId) {
				acPortInfo.setId(elineinfo.getaAcId());
			} else {
				acPortInfo.setId(elineinfo.getzAcId());
			}

			acportInfoList = acInfoServiceMB.queryByAcPortInfo(acPortInfo);

			this.getElinePanel().getAcElinePanel().initData(acportInfoList);
			this.getElinePanel().updateUI();
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(acInfoServiceMB);
		}

	}

	@Override
	public void synchro() {
		DispatchUtil elineDispatch = null;
		try {
			elineDispatch = new DispatchUtil(RmiKeys.RMI_ELINE);
			String result = elineDispatch.synchro(ConstantUtil.siteId);
			DialogBoxUtil.succeedDialog(null, result);
			//添加日志记录
			AddOperateLog.insertOperLog(null, EOperationLogType.ELINESYNCHRO.getValue(), result,
					null, null, ConstantUtil.siteId, ResourceUtil.srcStr(StringKeysTab.TAB_ELINEINFO), null);
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			elineDispatch = null;
		}
	}

	@Override
	public void openCreateDialog() throws Exception {
		new ElineEditDialog(null, this.getElinePanel());
	}
	// 修改
	@Override
	public void openUpdateDialog() throws Exception {
		//只能修改单网�?		
		if(this.elinePanel.getSelect().getIsSingle() == 0){
			DialogBoxUtil.succeedDialog(this.getElinePanel(), ResourceUtil.srcStr(StringKeysTip.TIP_UPDATE_NODE));
			return;
		}
		new ElineEditDialog(this.elinePanel.getSelect(), this.getElinePanel());
	};
	public ElinePanel getElinePanel() {
		return elinePanel;
	}

	public void setElinePanel(ElinePanel elinePanel) {
		this.elinePanel = elinePanel;
	}

	@SuppressWarnings("unchecked")
	public void consistence(){
		ElineInfoService_MB elineService = null;
		try {
			SiteUtil siteUtil=new SiteUtil();
			if (0 == siteUtil.SiteTypeUtil(ConstantUtil.siteId)) {
				DispatchUtil elineDispatch = new DispatchUtil(RmiKeys.RMI_ELINE);
				List<ServiceInfo> neList = new ArrayList<ServiceInfo>();
				try {
					neList = (List<ServiceInfo>) elineDispatch.consistence(ConstantUtil.siteId);
				} catch (Exception e) {
					ExceptionManage.dispose(e, this.getClass());
				}
				elineService = (ElineInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Eline);
				List<ElineInfo> emsList = new ArrayList<ElineInfo>();
				try {
					emsList = elineService.selectElineBySite(ConstantUtil.siteId);
				} catch (Exception e) {
					ExceptionManage.dispose(e, this.getClass());
				}
				this.filterElineList(neList);
				if(emsList.size() > 0 && neList.size() > 0){
					CamporeDataDialog dialog = new CamporeDataDialog(ResourceUtil.srcStr(StringKeysTip.TIP_ELINE),
							emsList, neList, this);
					UiUtil.showWindow(dialog, 700, 600);
				}else{
					DialogBoxUtil.errorDialog(this.elinePanel, ResourceUtil.srcStr(StringKeysTip.TIP_DATAISNULL));
				}
			}else{
				DialogBoxUtil.errorDialog(this.elinePanel, ResultString.QUERY_FAILED);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(elineService);
		}
	}
	
	/**
	 * 过滤出eline业务
	 */
	private void filterElineList(List<ServiceInfo> neList) {
		List<ElineInfo> elineList = new ArrayList<ElineInfo>();
		for (ServiceInfo elineInfo : neList) {
			if(elineInfo.getServiceType() == 1){
				elineList.add((ElineInfo)elineInfo);
			}
		}
		neList.clear();
		neList.addAll(elineList);
	}
}
