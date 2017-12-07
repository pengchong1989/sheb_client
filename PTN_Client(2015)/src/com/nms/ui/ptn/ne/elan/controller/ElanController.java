package com.nms.ui.ptn.ne.elan.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nms.db.bean.ptn.path.StaticUnicastInfo;
import com.nms.db.bean.ptn.path.eth.ElanInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.pw.PwNniInfo;
import com.nms.db.bean.ptn.port.AcPortInfo;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EOperationLogType;
import com.nms.model.ptn.path.SingleSpreadService_MB;
import com.nms.model.ptn.path.eth.ElanInfoService_MB;
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
import com.nms.ui.ptn.ne.elan.view.ElanEditDialog;
import com.nms.ui.ptn.ne.elan.view.ElanPanel;

public class ElanController extends AbstractController {

	private ElanPanel elanPanel;
	private Map<Integer, List<ElanInfo>> elanMap = null;
	private ElanInfo elanInfo=null;

	public ElanController(ElanPanel elanPanel) {
		this.setElanPanel(elanPanel);
	}

	@Override
	public void refresh() throws Exception {
		Map<Integer, List<ElanInfo>> elanFilterMap = null;
		ElanInfoService_MB elanInfoService = null;
		List<ElanInfo> infos = null;
		List<ElanInfo> elanInfo = null;
		ListingFilter filter = null;
		if(null==this.elanInfo){
			this.elanInfo=new ElanInfo();
		}
		try {
			infos = new ArrayList<ElanInfo>();
			filter = new ListingFilter();
			elanInfoService = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);
			this.elanInfo.setaSiteId(ConstantUtil.siteId);
			elanMap = elanInfoService.filterSelect(this.elanInfo);
			Iterator iter = elanMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iter.next();
				infos.addAll((Collection<? extends ElanInfo>) entry2.getValue());
			}
			//删除serviceID重复�?重复的算一�?			elanInfo = removeRepeatedElan(infos);
			this.getElanPanel().clear();
			this.getElanPanel().getPwElinePanel().clear();
			this.getElanPanel().getAcElinePanel().clear();
			this.getElanPanel().initData(elanInfo);
			this.getElanPanel().updateUI();
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(elanInfoService);
			infos = null;
			elanInfo = null;
		}
	}

	/**
	 * 删除业务ID重复的ELAN
	 * @param ElanList
	 * @return
	 */
	private List<ElanInfo> removeRepeatedElan(List<ElanInfo> ElanList) {
		List<ElanInfo> NorepeatedElan = ElanList;
		for (int i = 0; i < NorepeatedElan.size() - 1; i++) {
			for (int j = NorepeatedElan.size() - 1; j > i; j--) {
				if (NorepeatedElan.get(j).getServiceId() == NorepeatedElan.get(i).getServiceId()) {
					NorepeatedElan.remove(j);
				}
			}
		}

		return NorepeatedElan;
	}
	
	/**
	 * 选中一条记录后，查看详细信�?	 * 
	 * @throws Exception
	 */
	@Override
	public void initDetailInfo() {
		ElanInfo elanInfo = null;
		try {
			elanInfo = this.getElanPanel().getSelect();

			this.initPwData(elanInfo);
			this.initAcData(elanInfo);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}
	}

	/**
	 * 初始化pw数据
	 * 
	 * @param etreeInfo
	 * @throws Exception
	 */
	private void initPwData(ElanInfo elanInfo) throws Exception {
		PwInfoService_MB pwInfoService = null;
		PwInfo pwinfo = null;
		List<PwInfo> pwinfoList = null;
		ElanInfoService_MB elanInfoService = null;
		List<ElanInfo> elanInfoList = null;
		try {
			pwinfoList = new ArrayList<PwInfo>();
			elanInfoService = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);
			elanInfoList = elanInfoService.select(elanInfo);

			pwInfoService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			for (int i = 0; i < elanInfoList.size(); i++) {
				if (elanInfoList.get(i).getaSiteId() == ConstantUtil.siteId || elanInfoList.get(i).getzSiteId() == ConstantUtil.siteId) {
					pwinfo = new PwInfo();
					pwinfo.setPwId(elanInfoList.get(i).getPwId());
					pwinfo = pwInfoService.selectBypwid_notjoin(pwinfo);
					pwinfoList.add(pwinfo);
				}
			}

			this.getElanPanel().getPwElinePanel().initData(pwinfoList);
			this.getElanPanel().updateUI();
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(elanInfoService);
			UiUtil.closeService_MB(pwInfoService);
			pwinfo = null;
			pwinfoList = null;
			elanInfoList = null;
		}
	}

	/**
	 * 初始化ac数据
	 * 
	 * @param etreeInfo
	 * @throws Exception
	 */
	private void initAcData(ElanInfo elanInfo) throws Exception {

		AcPortInfoService_MB acInfoService = null;
		List<AcPortInfo> acportInfoList = null;
		Set<Integer> acIdSet = null;
		List<Integer> acIdList = null;
		UiUtil uiUtil = null;
		try {
			acportInfoList = new ArrayList<AcPortInfo>();
			acInfoService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			acIdSet = new HashSet<Integer>();
			acIdList = new ArrayList<Integer>();
			uiUtil = new UiUtil();
			if (elanInfo.getaSiteId() == ConstantUtil.siteId) {
				acIdSet.addAll(uiUtil.getAcIdSets(elanInfo.getAmostAcId()));
			} else {
				acIdSet.addAll(uiUtil.getAcIdSets(elanInfo.getZmostAcId()));
			}
			if(acIdSet.size() >0)
			{
				acIdList.addAll(acIdSet);
				acportInfoList = acInfoService.select(acIdList);
			}
			this.getElanPanel().getAcElinePanel().initData(acportInfoList);
			this.getElanPanel().updateUI();
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(acInfoService);
			acportInfoList = null;
			acIdSet = null;
			acIdList = null;
		}

	}

	@Override
	public void delete() throws Exception {

		List<ElanInfo> elanInfoList = null;
		boolean flag = true;
		String resultStr = null;
		List<ElanInfo> elanInfoList_delete = null;
		DispatchUtil elanDispatch = null;
		try {
			elanInfoList = this.getElanPanel().getAllSelect();
			elanInfoList_delete = new ArrayList<ElanInfo>();
			for (ElanInfo elanInfo : elanInfoList) {
				elanInfoList_delete.addAll(this.elanMap.get(elanInfo.getServiceId()));
			}

			elanDispatch = new DispatchUtil(RmiKeys.RMI_ELAN);
			resultStr = elanDispatch.excuteDelete(elanInfoList_delete);
			DialogBoxUtil.succeedDialog(this.getElanPanel(), resultStr);
			//添加日志记录
			for (ElanInfo elanInfo : elanInfoList_delete) {
				AddOperateLog.insertOperLog(null, EOperationLogType.ELANDELETE.getValue(), resultStr,
						null, null, ConstantUtil.siteId, elanInfo.getName(), null);
			}
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			elanInfoList = null;
			resultStr = null;
			elanInfoList_delete = null;
			elanDispatch = null;
		}

	}
	
	@Override
	public boolean deleteChecking() {
		List<ElanInfo> elanInfoList = null;
		boolean flag = false;
		List<Integer> siteIds = null;
		try {
			elanInfoList = this.getElanPanel().getAllSelect();
			for (ElanInfo elanInfo : elanInfoList) {
				if (elanInfo.getIsSingle() == 0) {
					flag = true;
					break;
				}
			}
			if (flag) {
				DialogBoxUtil.errorDialog(this.getElanPanel(), ResourceUtil.srcStr(StringKeysTip.TIP_DELETE_NODE));
				return false;
			}else{
				SiteUtil siteUtil = new SiteUtil();
				if(1==siteUtil.SiteTypeOnlineUtil(ConstantUtil.siteId)){
					WhImplUtil wu = new WhImplUtil();
					siteIds = new ArrayList<Integer>();
					siteIds.add(ConstantUtil.siteId);
					String str=wu.getNeNames(siteIds);
					DialogBoxUtil.errorDialog(this.getElanPanel(), ResourceUtil.srcStr(StringKeysTip.TIP_NOT_DELETEONLINE)+""+str+ResourceUtil.srcStr(StringKeysTip.TIP_ONLINENOT_DELETEONLINE));
					return false;  		    		
					}else{
						SingleSpreadService_MB uniService = null;
						StaticUnicastInfo staticUni =null;
						StaticUnicastInfo staticUni1 =null;
						List<StaticUnicastInfo> staticUniList = null;
						List<StaticUnicastInfo> staticUniInfo = null;
						try {
						     uniService = (SingleSpreadService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SINGELSPREAD);					   
						     staticUniList = new ArrayList<StaticUnicastInfo>();
								for(int i=0;i<elanInfoList.size();i++){
									staticUni = new StaticUnicastInfo();
									staticUni1 = new StaticUnicastInfo();
									if(elanInfoList.get(i).getaSiteId()>0){
									   staticUni.setSiteId(elanInfoList.get(i).getaSiteId());
									   staticUni.setVplsVs(elanInfoList.get(i).getAxcId());
									}
									if(elanInfoList.get(i).getzSiteId()>0){
									  staticUni1.setSiteId(elanInfoList.get(i).getzSiteId());
									  staticUni1.setVplsVs(elanInfoList.get(i).getZxcId());
									}
									staticUniList.add(staticUni);
									staticUniList.add(staticUni1);
								}
								for(int i=0;i<staticUniList.size();i++){
								    for(int j=staticUniList.size()-1;j>i;j--){
								    	if(staticUniList.get(j).getSiteId() == staticUniList.get(i).getSiteId() && 
								    			staticUniList.get(j).getVplsVs() == staticUniList.get(i).getVplsVs()){
								    		staticUniList.remove(j);
								    	}
								    }				    	
								}	
								int count=0;
								for(int i=0;i<staticUniList.size();i++){
									staticUniInfo = uniService.selectByStaticUniInfo(staticUniList.get(i));
								     if(staticUniInfo.size()>0 && staticUniInfo !=null){				   
									    count++;
								     }
								}
								if(count!=0){
								   DialogBoxUtil.succeedDialog(this.getElanPanel(), ResourceUtil.srcStr(StringKeysTip.TIP_DELETE_NOT));
								   return false;
								}
						} catch (Exception e) {
							ExceptionManage.dispose(e, this.getClass());
						} finally {
							UiUtil.closeService_MB(uniService);
							staticUni=null;
							staticUni1=null;
							staticUniList=null;
							staticUniInfo=null;
						}
					}
			}
			flag = true;
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally{
			elanInfoList =null;
			siteIds = null ;
		}
		return flag;
	}

	/**
	 * 激活处理事�?	 */
	public void doActive() {
		List<ElanInfo> infos = null;
		String result = null;
		DispatchUtil dispatch = null;
		List<ElanInfo> elanInfos = null;
		ElanInfoService_MB elanInfoService = null;
		List<ElanInfo> elanInfo2 = null;
		try {
			infos = this.elanPanel.getAllSelect();
			if (infos != null && infos.size() > 0) {
				elanInfos = new ArrayList<ElanInfo>();
				elanInfoService = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);
				elanInfo2 = new ArrayList<ElanInfo>();
				for (ElanInfo info : infos) {
					elanInfos = elanInfoService.selectByServiceId(info.getServiceId());
					for(ElanInfo elanInfo : elanInfos){
						elanInfo.setActiveStatus(EActiveStatus.ACTIVITY.getValue());
						elanInfo.setActivatingTime(DateUtil.getDate(DateUtil.FULLTIME));
						elanInfo2.add(elanInfo);
					}
				}
			}
			dispatch = new DispatchUtil(RmiKeys.RMI_ELAN);
			result = dispatch.excuteUpdate(elanInfo2);
			DialogBoxUtil.succeedDialog(this.elanPanel, result);
			//添加日志记录*************************/
			if (infos != null && infos.size() > 0) {
				for (ElanInfo info : infos) {
					AddOperateLog.insertOperLog(null, EOperationLogType.ELANSINGACTIVE.getValue(), result, null, null, ConstantUtil.siteId, info.getName(), null);
				}
			}
			//************************************/
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			infos = null;
			dispatch = null;
			elanInfos = null;
			UiUtil.closeService_MB(elanInfoService);
			elanInfo2 = null;
		}
	}

	/**
	 * 去激活处理事�?	 */
	public void doUnActive() {
		List<ElanInfo> infos = null;
		String result = null;
		DispatchUtil dispatch = null;
		List<ElanInfo> elanInfos = null;
		ElanInfoService_MB elanInfoService = null;
		List<ElanInfo> elanInfo2 = null;
		try {
			infos = this.elanPanel.getAllSelect();
			if (infos != null && infos.size() > 0) {
				elanInfos = new ArrayList<ElanInfo>();
				elanInfoService = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);
				elanInfo2 = new ArrayList<ElanInfo>();
				for (ElanInfo info : infos) {
					elanInfos = elanInfoService.selectByServiceId(info.getServiceId());
					for(ElanInfo elanInfo : elanInfos){
						elanInfo.setActiveStatus(EActiveStatus.UNACTIVITY.getValue());
						elanInfo.setActivatingTime(null);
						elanInfo2.add(elanInfo);
					}
				}
			}
			dispatch = new DispatchUtil(RmiKeys.RMI_ELAN);
			result = dispatch.excuteUpdate(elanInfo2);
			DialogBoxUtil.succeedDialog(this.elanPanel, result);
			//添加日志记录*************************/
			if (infos != null && infos.size() > 0) {
				for (ElanInfo info : infos) {
					AddOperateLog.insertOperLog(null, EOperationLogType.ELANSINGNOACTIVE.getValue(), result, null, null, ConstantUtil.siteId, info.getName(), null);
				}
			}
			//************************************/
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			infos = null;
			dispatch = null;
			elanInfos = null;
			UiUtil.closeService_MB(elanInfoService);
			elanInfo2 = null;
		}
	}

	@Override
	public void openCreateDialog() throws Exception {
		new ElanEditDialog(null, this.getElanPanel());
	}

	@Override
	public void openUpdateDialog() throws Exception {
		//只能修改单网�?		
		if(this.elanPanel.getSelect().getIsSingle() == 0){
			DialogBoxUtil.succeedDialog(this.getElanPanel(), ResourceUtil.srcStr(StringKeysTip.TIP_UPDATE_NODE));
			return;
		}
		SingleSpreadService_MB uniService = null;
		StaticUnicastInfo staticUni =null;
		List<StaticUnicastInfo> staticUniList = null;
		try {
		     uniService = (SingleSpreadService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SINGELSPREAD);
		     staticUni = new StaticUnicastInfo();
		     if(this.elanPanel.getSelect().getaSiteId() !=0){
			    staticUni.setSiteId(this.elanPanel.getSelect().getaSiteId());
			    staticUni.setVplsVs(this.elanPanel.getSelect().getAxcId());
		     }else{
			    staticUni.setSiteId(this.elanPanel.getSelect().getzSiteId());
			    staticUni.setVplsVs(this.elanPanel.getSelect().getZxcId());
		     }		     
		     staticUniList = uniService.selectByStaticUniInfo(staticUni);
		     if(staticUniList.size()>0 && staticUniList !=null){
			    DialogBoxUtil.succeedDialog(this.getElanPanel(), ResourceUtil.srcStr(StringKeysTip.TIP_UPDATE_NOT));
			    return;
		     }else{
		    	 new ElanEditDialog(this.elanPanel.getSelect(), this.getElanPanel());
		     }
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(uniService);
			staticUni=null;
			staticUniList=null;
		}
		
	}
	
	public ElanPanel getElanPanel() {
		return elanPanel;
	}

	public void setElanPanel(ElanPanel elanPanel) {
		this.elanPanel = elanPanel;
	}

	@Override
	public void synchro() {
		DispatchUtil elanDispatch = null;
		try {
			elanDispatch = new DispatchUtil(RmiKeys.RMI_ELAN);
			String result = elanDispatch.synchro(ConstantUtil.siteId);
			DialogBoxUtil.succeedDialog(null, result);
			//添加日志记录
			AddOperateLog.insertOperLog(null, EOperationLogType.ELANSYNCHRO.getValue(), result,
					null, null, ConstantUtil.siteId, ResourceUtil.srcStr(StringKeysTab.TAB_ELANINFO), null);
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			elanDispatch = null;
		}
	}
	
	@Override
	public void openFilterDialog() throws Exception {
		if(null==this.elanInfo){
			this.elanInfo=new ElanInfo();
		}
		new EthNeFilterDialog(this.elanInfo);
		this.refresh();
	}
	
	// 清除过滤
	public void clearFilter() throws Exception {
		this.elanInfo=null;
		this.refresh();
	}

	@SuppressWarnings("unchecked")
	public void consistence(){
		ElanInfoService_MB elanService = null;
		try {
			SiteUtil siteUtil=new SiteUtil();
			if (0 == siteUtil.SiteTypeUtil(ConstantUtil.siteId)) {
				DispatchUtil elanDispatch = new DispatchUtil(RmiKeys.RMI_ELAN);
				List<ElanInfo> neList = new ArrayList<ElanInfo>();
				try {
					neList = (List<ElanInfo>)elanDispatch.consistence(ConstantUtil.siteId);
				} catch (Exception e) {
					ExceptionManage.dispose(e, this.getClass());
				}
				elanService = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);
				Map<Integer, List<ElanInfo>> emsMap = elanService.selectBySiteId(ConstantUtil.siteId);
				List<ElanInfo> emsList = this.getEmsList(emsMap);
				if(emsList.size() > 0 && neList.size() > 0){
					CamporeDataDialog dialog = new CamporeDataDialog(ResourceUtil.srcStr(StringKeysTip.TIP_ELAN),emsList, neList, this);
					UiUtil.showWindow(dialog, 700, 600);
				}else{
					DialogBoxUtil.errorDialog(this.elanPanel, ResourceUtil.srcStr(StringKeysTip.TIP_DATAISNULL));
				}
			}else{
				DialogBoxUtil.errorDialog(this.elanPanel, ResultString.QUERY_FAILED);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(elanService);
		}
	}

	private List<ElanInfo> getEmsList(Map<Integer, List<ElanInfo>> emsMap) {
		AcPortInfoService_MB acService = null;
		PwInfoService_MB pwService = null;
		List<ElanInfo> elanList = new ArrayList<ElanInfo>();
		try {
			acService = (AcPortInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.AcInfo);
			pwService = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			for (int serviceId : emsMap.keySet()) {
				List<ElanInfo> elanInfoList = emsMap.get(serviceId);
				ElanInfo elan = elanInfoList.get(0);
				elan.getAcPortList().addAll(this.getAcInfo(ConstantUtil.siteId, elanInfoList.get(0), acService));
				for (ElanInfo elanInfo : elanInfoList) {
					elan.getPwNniList().add(this.getPwNniInfo(ConstantUtil.siteId, elanInfo, pwService));
				}
				elanList.add(elan);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(acService);
			UiUtil.closeService_MB(pwService);
		}
		return elanList;
	}

	private List<AcPortInfo> getAcInfo(int siteId, ElanInfo elanInfo, AcPortInfoService_MB acService) throws Exception {
		int id = 0;
		UiUtil uiutil = null;
		Set<Integer> acIds = null;
		List<Integer> acIdList = null;
		try {
			acIds = new HashSet<Integer>();
			uiutil = new UiUtil();
			if(elanInfo.getaSiteId() == siteId){
//				id = elanInfo.getaAcId();
				acIds.addAll(uiutil.getAcIdSets(elanInfo.getAmostAcId()));
			}else{
//				id = elanInfo.getzAcId();
				acIds.addAll(uiutil.getAcIdSets(elanInfo.getZmostAcId()));
			}
			if(acIds.size() > 0)
			{
				acIdList = new ArrayList<Integer>(acIds);
				return  acService.select(acIdList);
			}
		} catch (Exception e)
		{
			ExceptionManage.dispose(e, getClass());
		}finally
		{
			 uiutil = null;
			 acIds = null;
			 acIdList = null;
		}
		return null;
	}

	private PwNniInfo getPwNniInfo(int siteId, ElanInfo elanInfo, PwInfoService_MB pwService) throws Exception {
		PwInfo pw = new PwInfo();
		pw.setPwId(elanInfo.getPwId());
		pw = pwService.selectBypwid_notjoin(pw);
		if(pw != null){
			if(pw.getASiteId() == siteId){
				return pw.getaPwNniInfo();
			}else if(pw.getZSiteId() == siteId){
				return pw.getzPwNniInfo();
			}
		}
		return null;
	}
}
