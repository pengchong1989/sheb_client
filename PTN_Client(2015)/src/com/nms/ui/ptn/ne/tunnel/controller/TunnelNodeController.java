﻿package com.nms.ui.ptn.ne.tunnel.controller;

import java.util.ArrayList;
import java.util.List;

import com.nms.db.bean.ptn.oam.OamMepInfo;
import com.nms.db.bean.ptn.path.ces.CesInfo;
import com.nms.db.bean.ptn.path.eth.DualInfo;
import com.nms.db.bean.ptn.path.eth.ElanInfo;
import com.nms.db.bean.ptn.path.eth.ElineInfo;
import com.nms.db.bean.ptn.path.eth.EtreeInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.tunnel.Lsp;
import com.nms.db.bean.ptn.path.tunnel.Tunnel;
import com.nms.db.bean.ptn.qos.QosInfo;
import com.nms.db.bean.report.SSProfess;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EServiceType;
import com.nms.model.equipment.port.PortService_MB;
import com.nms.model.ptn.BfdInfoService_MB;
import com.nms.model.ptn.oam.OamInfoService_MB;
import com.nms.model.ptn.path.ces.CesInfoService_MB;
import com.nms.model.ptn.path.eth.DualInfoService_MB;
import com.nms.model.ptn.path.eth.ElanInfoService_MB;
import com.nms.model.ptn.path.eth.ElineInfoService_MB;
import com.nms.model.ptn.path.eth.EtreeInfoService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.path.tunnel.TunnelService_MB;
import com.nms.model.ptn.qos.QosInfoService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.ResultString;
import com.nms.service.impl.util.SiteUtil;
import com.nms.service.impl.util.WhImplUtil;
import com.nms.ui.filter.impl.TunnelNEFilterDialog;
import com.nms.ui.frame.AbstractController;
import com.nms.ui.manager.CheckingUtil;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.keys.StringKeysLbl;
import com.nms.ui.manager.keys.StringKeysObj;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.ptn.ne.camporeData.CamporeDataDialog;
import com.nms.ui.ptn.ne.tunnel.view.TunnelAddDialog;
import com.nms.ui.ptn.ne.tunnel.view.TunnelPanel;
import com.nms.ui.ptn.systemconfig.dialog.qos.ComparableSort;

public class TunnelNodeController extends AbstractController {

	private TunnelPanel view;
	private Tunnel filterCondition = null;// tunnel的过滤条�?	
	private List<Tunnel> infos = null;
	private int total;
	private int now = 1;
	
	public TunnelNodeController(TunnelPanel tunnelPanel) {
		this.setView(tunnelPanel);
	}

	@Override
	public void refresh() throws Exception {
		this.searchAndrefreshdata();
	}

	// 创建
	@Override
	public void openCreateDialog() throws Exception {
		 new TunnelAddDialog(null, view);
	};

	@Override
	public boolean deleteChecking() {
		List<Tunnel> tunnelList = null;
		boolean flag = false;
		List<Integer> siteIds = null;
		try {
			tunnelList = this.getView().getAllSelect();
			for (Tunnel tunnel : tunnelList) {
				if (tunnel.getIsSingle() == 0) {
					flag = true;
					break;
				}
			}
			if (flag) {
				DialogBoxUtil.errorDialog(this.getView(), ResourceUtil.srcStr(StringKeysTip.TIP_DELETE_NODE));
				return false;
			}else{
				//判断是否为在线托管网�?				
				SiteUtil siteUtil = new SiteUtil();
				if(1==siteUtil.SiteTypeOnlineUtil(ConstantUtil.siteId)){
					WhImplUtil wu = new WhImplUtil();
					siteIds = new ArrayList<Integer>();
					siteIds.add(ConstantUtil.siteId);
					String str=wu.getNeNames(siteIds);
					DialogBoxUtil.errorDialog(this.getView(), ResourceUtil.srcStr(StringKeysTip.TIP_NOT_DELETEONLINE)+""+str+ResourceUtil.srcStr(StringKeysTip.TIP_ONLINENOT_DELETEONLINE));
					return false;  		    		
					}
			}
			
			// 删除tunnel之前先验证该tunnel是否有按需oam，没有才可删除，否则提示不能删除
			for (Tunnel tunnel : tunnelList) {
				// 如果为true，说明该条tunnel有按需oam，不能删�?				
				if (checkIsOam(tunnel)) {
					flag = true;
					break;
				}
			}
			if (flag) {
				DialogBoxUtil.errorDialog(this.getView(), ResourceUtil.srcStr(StringKeysTip.TIP_CLEAN_OAM));
				return false;
			}

			// 删除tunnel之前先验证该tunnel是否有bfd，没有才可删除，否则提示不能删除
			for (Tunnel tunnel : tunnelList) {
				// 如果为true，说明该条tunnel有bfd，不能删�?				
				if (checkIsBfd(tunnel)) {
					flag = true;
					break;
				}
			}
			if (flag) {
				DialogBoxUtil.errorDialog(this.view, ResourceUtil.srcStr(StringKeysTip.TIP_CLEAN_BFD));
				return false;
			}
			
			
			flag = true;
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			tunnelList = null;
			siteIds = null;
		}
		return flag;
	}

	private boolean checkIsBfd(Tunnel tunnel) {
		boolean flag=false;
		BfdInfoService_MB bfdService = null;
		int aSiteId=0;
		int lspId=0;
		List<Integer> lspIds=null;
		List<Integer> lspIds2=null;
		try {			
			bfdService = (BfdInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.BFDMANAGEMENT);
			lspIds=new ArrayList<Integer>();
			lspIds2=new ArrayList<Integer>();
			//a�?			aSiteId=tunnel.getaSiteId();
			lspIds=bfdService.queryLspIds(aSiteId, 1);
			lspIds2=bfdService.queryLspIds(aSiteId,2);
			for(int i=0;i<tunnel.getLspParticularList().size();i++){
				if(aSiteId==tunnel.getLspParticularList().get(i).getASiteId()){
					lspId=tunnel.getLspParticularList().get(i).getAtunnelbusinessid();
					if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
						flag=true;
					    return flag;
					}					   
				}
				if(aSiteId==tunnel.getLspParticularList().get(i).getZSiteId()){
					lspId=tunnel.getLspParticularList().get(i).getZtunnelbusinessid();
					if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
						flag=true;
					    return flag;
					}
				}
			}			
		//z�?
			aSiteId=tunnel.getzSiteId();
			lspIds=bfdService.queryLspIds(aSiteId, 1);
			lspIds2=bfdService.queryLspIds(aSiteId,2);
			for(int i=0;i<tunnel.getLspParticularList().size();i++){
				if(aSiteId==tunnel.getLspParticularList().get(i).getASiteId()){
					lspId=tunnel.getLspParticularList().get(i).getAtunnelbusinessid();
					if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
						flag=true;
					    return flag;
					}					   
				}
				if(aSiteId==tunnel.getLspParticularList().get(i).getZSiteId()){
					lspId=tunnel.getLspParticularList().get(i).getZtunnelbusinessid();
					if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
						flag=true;
					    return flag;
					}
				}
			}
			//保护
			if(tunnel.getProtectTunnel()!=null){
				//a�?				aSiteId=tunnel.getProtectTunnel().getaSiteId();
				lspIds=bfdService.queryLspIds(aSiteId, 1);
				lspIds2=bfdService.queryLspIds(aSiteId,2);
				for(int i=0;i<tunnel.getProtectTunnel().getLspParticularList().size();i++){
					if(aSiteId==tunnel.getProtectTunnel().getLspParticularList().get(i).getASiteId()){
						lspId=tunnel.getProtectTunnel().getLspParticularList().get(i).getAtunnelbusinessid();
						if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
							flag=true;
						    return flag;
						}					   
					}
					if(aSiteId==tunnel.getProtectTunnel().getLspParticularList().get(i).getZSiteId()){
						lspId=tunnel.getProtectTunnel().getLspParticularList().get(i).getZtunnelbusinessid();
						if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
							flag=true;
						    return flag;
						}
					}
				}			
			//z�?
				aSiteId=tunnel.getProtectTunnel().getzSiteId();
				lspIds=bfdService.queryLspIds(aSiteId, 1);
				lspIds2=bfdService.queryLspIds(aSiteId,2);
				for(int i=0;i<tunnel.getProtectTunnel().getLspParticularList().size();i++){
					if(aSiteId==tunnel.getProtectTunnel().getLspParticularList().get(i).getASiteId()){
						lspId=tunnel.getProtectTunnel().getLspParticularList().get(i).getAtunnelbusinessid();
						if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
							flag=true;
						    return flag;
						}					   
					}
					if(aSiteId==tunnel.getProtectTunnel().getLspParticularList().get(i).getZSiteId()){
						lspId=tunnel.getProtectTunnel().getLspParticularList().get(i).getZtunnelbusinessid();
						if(lspIds.contains(lspId)||lspIds2.contains(lspId)){
							flag=true;
						    return flag;
						}
					}
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
		finally
		{
			lspIds=null;
			lspIds2=null;
			UiUtil.closeService_MB(bfdService);
		}
		return flag;
	}
	
	private boolean checkIsOam(Tunnel tunnel) {
		OamMepInfo mep = null;
		OamInfoService_MB service = null;
		boolean flag = false;
		try {
			mep = new OamMepInfo();
			mep.setObjId(tunnel.getTunnelId());
			mep.setObjType("TUNNEL_TEST");
			service = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo);
			flag = service.queryByObjIdAndType(mep);
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(service);
		}
		return flag;
	}

	// 删除
	@Override
	public void delete() throws Exception {

		List<Tunnel> tunnelList = null;

		DispatchUtil tunnelDispatch = null;
		String resultStr = null;
		try {
			tunnelList = this.getView().getAllSelect();
			tunnelDispatch = new DispatchUtil(RmiKeys.RMI_TUNNEL);
			resultStr = tunnelDispatch.excuteDelete(tunnelList);
			DialogBoxUtil.succeedDialog(this.getView(), resultStr);
			// 添加日志记录
			PtnButton deleteButton = (PtnButton) this.view.getDeleteButton();
			deleteButton.setOperateKey(EOperationLogType.TUNNELDELETE.getValue());
			int operationResult = 0;
			if (ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS).equals(resultStr)) {
				operationResult = 1;
			} else {
				operationResult = 2;
			}
			deleteButton.setResult(operationResult);
			this.view.getRefreshButton().doClick();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			tunnelDispatch = null;
			resultStr = null;
			tunnelList = null;
		}

	};

	private void searchAndrefreshdata() {
		
		TunnelService_MB tunnelServiceMB = null;
		List<Tunnel> needs= null;
		try {
			tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			// �?filterCondition清空，则进入原有的为过滤的方�?			
			if (this.filterCondition == null) {
				this.filterCondition=new Tunnel();
			}
			infos=tunnelServiceMB.filterSelectNE(ConstantUtil.siteId,this.filterCondition);
			if(infos.size() ==0){
				now = 0;
				view.getNextPageBtn().setEnabled(false);
				view.getGoToJButton().setEnabled(false);
			}else{
				tunnelServiceMB.setOnlyTunnelLsp(infos, true);
				now =1;
				if (infos.size() % ConstantUtil.flipNumber == 0) {
					total = infos.size() / ConstantUtil.flipNumber;
				} else {
					total = infos.size() / ConstantUtil.flipNumber + 1;
				}
				if (total == 1) {
					view.getNextPageBtn().setEnabled(false);
					view.getGoToJButton().setEnabled(false);
				}else{
					view.getNextPageBtn().setEnabled(true);
					view.getGoToJButton().setEnabled(true);
				}
				if (infos.size() - (now - 1) * ConstantUtil.flipNumber > ConstantUtil.flipNumber) {
					needs = infos.subList((now - 1) * ConstantUtil.flipNumber, ConstantUtil.flipNumber);
				} else {
					needs = infos.subList((now - 1) * ConstantUtil.flipNumber, infos.size() - (now - 1) * ConstantUtil.flipNumber);
				}
			}
			view.getCurrPageLabel().setText(now+"");
			view.getTotalPageLabel().setText(total + "");
			view.getPrevPageBtn().setEnabled(false);
			this.view.clear();
			this.view.initData(needs);
			this.view.getLspPanel().clear();
			this.view.getQosPanel().clear();
			this.view.updateUI();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(tunnelServiceMB);
		}
	}

	// 修改
	@Override
	public void openUpdateDialog() throws Exception {
		if (this.getView().getAllSelect().size() == 0) {
			DialogBoxUtil.errorDialog(this.getView(), ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_DATA_ONE));
		} else {
			Tunnel tunnel = this.getView().getAllSelect().get(0);
			if (tunnel.getIsSingle() == 0) {
				DialogBoxUtil.errorDialog(this.view, ResourceUtil.srcStr(StringKeysTip.TIP_UPDATE_NODE));
				return;
			}
			new TunnelAddDialog(tunnel, this.view);
		}
	}

	/**
	 * 选中一条记录后，查看详细信�?	 */
	@Override
	public void initDetailInfo() {
		try {
			this.initQosInfos();
			this.initLspData();
			this.initPwNetworkTablePanel();
			this.initBusinessPanel();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}
	}
	
	private void initBusinessPanel() {
		PwInfoService_MB pwInfoServiceMB = null;
		ElineInfoService_MB elineInfoServiceMB = null;
		EtreeInfoService_MB etreeInfoServiceMB = null;
		ElanInfoService_MB elanInfoServiceMB = null;
		CesInfoService_MB cesInfoServiceMB = null;
		DualInfoService_MB dualInfoServiceMB = null;
		List<Integer> pwList = new ArrayList<Integer>();
		SSProfess ss=null;
		List<SSProfess> ssList = null;
		try {
			Tunnel tunnel = this.view.getSelect();
			List<Integer> tunnelIdList = new ArrayList<Integer>();
			tunnelIdList.add(tunnel.getTunnelId());
			pwInfoServiceMB = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);			
			List<PwInfo> pwInfoList = new ArrayList<PwInfo>();
			pwInfoList = (List<PwInfo>) pwInfoServiceMB.selectPwInfoByTunnelId(tunnelIdList);
			if(pwInfoList == null){
				pwInfoList = new ArrayList<PwInfo>();
			}
			for(PwInfo pw : pwInfoList){
				pwList.add(pw.getPwId());
			}
			if(!pwList.isEmpty()){
				elineInfoServiceMB = (ElineInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Eline);	
				etreeInfoServiceMB = (EtreeInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.EtreeInfo);			
				elanInfoServiceMB = (ElanInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.ElanInfo);	
				cesInfoServiceMB = (CesInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.CesInfo);			
				dualInfoServiceMB = (DualInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.DUALINFO);			
				List<ElineInfo> elineList = elineInfoServiceMB.selectElineByPwId(pwList);
				ssList = new ArrayList<SSProfess>();		
				if(elineList!=null && elineList.size()!=0){
					for(ElineInfo eline : elineList){
						ss=new SSProfess();
						ss.setName(eline.getName());
						ss.setServiceType(eline.getServiceType());
						ss.setCreateTime(eline.getCreateTime());
						ss.setActiveStatus(eline.getActiveStatus());
						ss.setClientName(eline.getClientName());
						ssList.add(ss);
					}
					this.view.getBusinessNetworkTablePanel().clear();
					this.view.getBusinessNetworkTablePanel().initData(ssList);
				}else{
					List<EtreeInfo> etreeList = etreeInfoServiceMB.selectEtreeByPwId(pwList);
					if(etreeList!=null && etreeList.size()!=0){
						for(EtreeInfo etree : etreeList){
						ss=new SSProfess();
							ss.setName(etree.getName());
							ss.setServiceType(etree.getServiceType());
							ss.setCreateTime(etree.getCreateTime());
							ss.setActiveStatus(etree.getActiveStatus());
							ss.setClientName(etree.getClientName());
						ssList.add(ss);
						}
						this.view.getBusinessNetworkTablePanel().clear();
						this.view.getBusinessNetworkTablePanel().initData(ssList);
					}else{
						List<ElanInfo> elanList = elanInfoServiceMB.selectElanbypwid(pwList);
						if(elanList!=null && elanList.size()!=0){
							for(ElanInfo elan : elanList){
							ss=new SSProfess();
								ss.setName(elan.getName());
								ss.setServiceType(elan.getServiceType());
								ss.setCreateTime(elan.getCreateTime());
								ss.setActiveStatus(elan.getActiveStatus());
								ss.setClientName(elan.getClientName());
							ssList.add(ss);
							}
							this.view.getBusinessNetworkTablePanel().clear();
							this.view.getBusinessNetworkTablePanel().initData(ssList);					
						}else{
							List<CesInfo> cesList = cesInfoServiceMB.selectCesByPwId(pwList);
							if(cesList!=null && cesList.size()!=0){
								for(CesInfo ces : cesList){
								ss=new SSProfess();
									ss.setName(ces.getName());
									ss.setServiceType(ces.getServiceType());
									ss.setCreateTime(ces.getCreateTime());
									ss.setActiveStatus(ces.getActiveStatus());
									ss.setClientName(ces.getClientName());
								ssList.add(ss);
								}
								this.view.getBusinessNetworkTablePanel().clear();
								this.view.getBusinessNetworkTablePanel().initData(ssList);	
							}else{
//								List<DualInfo> dualList = dualInfoServiceMB.queryByPwId(pwInfo.getPwId());
//								if(dualList!=null && dualList.size()!=0){
//									ss=new SSProfess();
//									ss.setName(dualList.get(0).getName());
//									ss.setServiceType(dualList.get(0).getServiceType());
//									ss.setCreateTime(dualList.get(0).getCreateTime());
//									ss.setActiveStatus(dualList.get(0).getActiveStatus());
//									ss.setClientName(dualList.get(0).getClientName());
//									ssList = new ArrayList<SSProfess>();
//									ssList.add(ss);
//									this.view.getBusinessNetworkTablePanel().clear();
//									this.view.getBusinessNetworkTablePanel().initData(ssList);
//								}else{
//									ssList = new ArrayList<SSProfess>();
//									this.view.getBusinessNetworkTablePanel().clear();
//									this.view.getBusinessNetworkTablePanel().initData(ssList);								
//								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(pwInfoServiceMB);
			UiUtil.closeService_MB(elineInfoServiceMB);
			UiUtil.closeService_MB(etreeInfoServiceMB);
			UiUtil.closeService_MB(elanInfoServiceMB);
			UiUtil.closeService_MB(cesInfoServiceMB);
			UiUtil.closeService_MB(dualInfoServiceMB);
		}
	}
	
	private void initPwNetworkTablePanel() {
		PwInfoService_MB pwServiceMB = null;
		Tunnel tunnel = null;
//		PwInfo pwInfo =null;
		try {
			tunnel = this.view.getSelect();
//			pwInfo=new PwInfo();
//			pwInfo.setTunnelId(tunnel.getTunnelId());
			List<Integer> tunnelIdList = new ArrayList<Integer>();
			tunnelIdList.add(tunnel.getTunnelId());
			pwServiceMB = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);			
			List<PwInfo> pwList = new ArrayList<PwInfo>();
			pwList=(List<PwInfo>) pwServiceMB.selectPwInfoByTunnelId(tunnelIdList);
			if(pwList ==null){
				pwList = new ArrayList<PwInfo>();
			}
			this.view.getPwNetworkTablePanel().clear();
			this.view.getPwNetworkTablePanel().initData(pwList);
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(pwServiceMB);
		}
	}	

	@SuppressWarnings("unchecked")
	private void initQosInfos() throws Exception {
		QosInfoService_MB qosInfoServiceMB = null;
		List<QosInfo> qosList = null;
		Tunnel tunnel = null;
		try {
			tunnel = this.view.getSelect();
			qosList = new ArrayList<QosInfo>();
			qosInfoServiceMB = (QosInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.QosInfo);
			qosList = qosInfoServiceMB.getQosByObj(EServiceType.TUNNEL.toString(), tunnel.getTunnelId());
			ComparableSort sort = new ComparableSort();
			qosList = (List<QosInfo>) sort.compare(qosList);
			this.view.getQosPanel().clear();
			this.view.getQosPanel().initData(qosList);
			this.view.updateUI();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			UiUtil.closeService_MB(qosInfoServiceMB);
		}
	}

	private void initLspData() throws Exception {
		TunnelService_MB tunnelServiceMB = null;
		try {
			Tunnel tunnel = this.view.getSelect();
			tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			List<Tunnel> tunnelList = new ArrayList<Tunnel>();
			tunnelList.add(tunnel);
//			tunnelServiceMB.setOnlyTunnelLsp(tunnelList, true);
			List<Lsp> lspList = this.convertLspData(tunnel);
			this.view.getLspPanel();
			this.view.getLspPanel().initData(lspList);
			this.view.updateUI();

		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(tunnelServiceMB);
		}
	}

	/**
	 * 转换lsp对象 给lsp所需要的列赋�?	 * 
	 * @param tunnel
	 * @throws Exception
	 */
	private List<Lsp> convertLspData(Tunnel tunnel) throws Exception {
		PortService_MB portServiceMB = null;
		List<Lsp> lspList = null;
		try {
			portServiceMB = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);
			lspList = new ArrayList<Lsp>();
			for (Lsp lsp : tunnel.getLspParticularList()) {
				if (tunnel.getASiteId() == ConstantUtil.siteId) {
					if (lsp.getASiteId() == tunnel.getASiteId()) {
						lsp.putClientProperty("id", lsp.getId());
						lsp.putClientProperty("lspname", tunnel.getTunnelName() + "/1");
						lsp.putClientProperty("inlabel", lsp.getBackLabelValue());
						lsp.putClientProperty("outlabel", lsp.getFrontLabelValue());
						lsp.putClientProperty("type", ResourceUtil.srcStr(StringKeysObj.LSP_TYPE_JOB));
						lsp.putClientProperty("portname", portServiceMB.getPortname(lsp.getAPortId()));
						lsp.putClientProperty("sitename", lsp.getZoppositeId());
						lsp.putClientProperty("position", tunnel.getPosition() == 1 ? true : false);
						lspList.add(lsp);
						break;
					}
				} else if (tunnel.getZSiteId() == ConstantUtil.siteId) {
					if (lsp.getZSiteId() == tunnel.getZSiteId()) {
						lsp.putClientProperty("id", lsp.getId());
						lsp.putClientProperty("lspname", tunnel.getTunnelName() + "/1");
						lsp.putClientProperty("inlabel", lsp.getFrontLabelValue());
						lsp.putClientProperty("outlabel", lsp.getBackLabelValue());
						lsp.putClientProperty("type", ResourceUtil.srcStr(StringKeysObj.LSP_TYPE_JOB));
						lsp.putClientProperty("portname", portServiceMB.getPortname(lsp.getZPortId()));
						lsp.putClientProperty("sitename", lsp.getAoppositeId());
						lsp.putClientProperty("position", tunnel.getPosition() == 1 ? true : false);
						lspList.add(lsp);
						break;
					}
				} else {
					if (lsp.getZSiteId() == ConstantUtil.siteId) {
						lsp.putClientProperty("id", lsp.getId());
						lsp.putClientProperty("lspname", tunnel.getTunnelName() + "/1");
						lsp.putClientProperty("inlabel", lsp.getFrontLabelValue());
						lsp.putClientProperty("outlabel", lsp.getBackLabelValue());
						lsp.putClientProperty("type", ResourceUtil.srcStr(StringKeysObj.LSP_TYPE_JOB));
						lsp.putClientProperty("portname", portServiceMB.getPortname(lsp.getZPortId()));
						lsp.putClientProperty("sitename", lsp.getAoppositeId());
						lsp.putClientProperty("position", tunnel.getPosition() == 1 ? true : false);
						// if (lsp.getASiteId() != 0) {
						// lsp.putClientProperty("sitename", UiUtil.getSiteName(lsp.getASiteId()));
						// }
						lspList.add(lsp);
					} else if (lsp.getASiteId() == ConstantUtil.siteId) {
						lsp.putClientProperty("id", lsp.getId());
						lsp.putClientProperty("lspname", tunnel.getTunnelName() + "/2");
						lsp.putClientProperty("inlabel", lsp.getBackLabelValue());
						lsp.putClientProperty("outlabel", lsp.getFrontLabelValue());
						lsp.putClientProperty("type", ResourceUtil.srcStr(StringKeysObj.LSP_TYPE_JOB));
						lsp.putClientProperty("portname", portServiceMB.getPortname(lsp.getAPortId()));
						lsp.putClientProperty("sitename", lsp.getZoppositeId());
						lsp.putClientProperty("position", tunnel.getPosition() == 1 ? true : false);
						// if (lsp.getZSiteId() != 0) {
						// lsp.putClientProperty("sitename", UiUtil.getSiteName(lsp.getZSiteId()));
						// }
						lspList.add(lsp);
					}

				}
			}

			// 类型�?:1保护 把保护的lsp信息绑定到列表中
			if (!"0".equals(tunnel.getTunnelType()) && "2".equals(UiUtil.getCodeById(Integer.parseInt(tunnel.getTunnelType())).getCodeValue())) {
				Tunnel protectTunnel = tunnel.getProtectTunnel();

				for (Lsp lsp : protectTunnel.getLspParticularList()) {
					if (lsp.getASiteId() == ConstantUtil.siteId) {
						lsp.putClientProperty("id", lsp.getId());
						lsp.putClientProperty("lspname", tunnel.getTunnelName() + "/2");
						lsp.putClientProperty("inlabel", lsp.getBackLabelValue());
						lsp.putClientProperty("outlabel", lsp.getFrontLabelValue());
						lsp.putClientProperty("type", ResourceUtil.srcStr(StringKeysLbl.LBL_PROTECT));
						lsp.putClientProperty("portname", portServiceMB.getPortname(lsp.getAPortId()));
						lsp.putClientProperty("sitename", lsp.getAoppositeId());
						lsp.putClientProperty("position", protectTunnel.getPosition() == 1 ? true : false);
						// if (lsp.getZSiteId() != 0) {
						// lsp.putClientProperty("sitename", UiUtil.getSiteName(lsp.getZSiteId()));
						// }
						lspList.add(lsp);
						break;
					} else if (lsp.getZSiteId() == ConstantUtil.siteId) {
						lsp.putClientProperty("id", lsp.getId());
						lsp.putClientProperty("lspname", tunnel.getTunnelName() + "/2");
						lsp.putClientProperty("inlabel", lsp.getFrontLabelValue());
						lsp.putClientProperty("outlabel", lsp.getBackLabelValue());
						lsp.putClientProperty("type", ResourceUtil.srcStr(StringKeysLbl.LBL_PROTECT));
						lsp.putClientProperty("portname", portServiceMB.getPortname(lsp.getZPortId()));
						lsp.putClientProperty("sitename", lsp.getZoppositeId());
						lsp.putClientProperty("position", protectTunnel.getPosition() == 1 ? true : false);
						// if (lsp.getASiteId() != 0) {
						// lsp.putClientProperty("sitename", UiUtil.getSiteName(lsp.getASiteId()));
						// }
						lspList.add(lsp);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			UiUtil.closeService_MB(portServiceMB);
		}
		return lspList;
	}

	@Override
	public void synchro() {
		DispatchUtil tunnelDispatch = null;
		try {
			tunnelDispatch = new DispatchUtil(RmiKeys.RMI_TUNNEL);
			String result = tunnelDispatch.synchro(ConstantUtil.siteId);
			DialogBoxUtil.succeedDialog(null, result);
			// 添加日志记录
			PtnButton deleteButton = (PtnButton) this.view.getSynchroButton();
			deleteButton.setOperateKey(EOperationLogType.TUNNELSYNCHRO.getValue());
			deleteButton.setResult(1);
			this.refresh();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		} finally {
			tunnelDispatch = null;
		}
	}

	public void setView(TunnelPanel view) {
		this.view = view;
	}

	public TunnelPanel getView() {
		return view;
	};

	/**
	 * 对单网元侧的tunnel进行过滤查询 添加tunnel过滤
	 */
	public void openFilterDialog() throws Exception {
//		if (null == this.filterCondition) {
//			this.filterCondition = new Tunnel();// 若filterCondition已经清空 ，重新创建实例，以供下次是用
//		}
//		new AddTunnelFilterDialog(2, this.filterCondition, this.view);
		new TunnelNEFilterDialog(this.filterCondition);
		this.refresh();
	}

	/**
	 * 一致性检�?	 */
	@Override
	public void consistence(){
		List<Tunnel> tunnelEMS = null;
		TunnelService_MB tunnelServiceMB = null;
		DispatchUtil dispatchUtil = null;
		List<Tunnel> tunnelsNE = null;  
		try {
			SiteUtil siteUtil=new SiteUtil();
			if (0 == siteUtil.SiteTypeUtil(ConstantUtil.siteId)) {
				tunnelServiceMB = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
				tunnelEMS = tunnelServiceMB.selectWHNodesBySiteId(ConstantUtil.siteId);
				dispatchUtil = new  DispatchUtil(RmiKeys.RMI_TUNNEL);
				tunnelsNE = (List<Tunnel>) dispatchUtil.consistence(ConstantUtil.siteId);
				CamporeDataDialog camporeDataDialog = new CamporeDataDialog(ResourceUtil.srcStr(StringKeysTip.TIP_TUNNEL_TABLE), tunnelEMS, tunnelsNE, this);
				UiUtil.showWindow(camporeDataDialog, 700, 600);
			}else{
				DialogBoxUtil.errorDialog(this.view, ResultString.QUERY_FAILED);
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}finally{
			UiUtil.closeService_MB(tunnelServiceMB);
		}
	}
	
	/**
	 * 清除tunnel过滤
	 */
	public void clearFilter() throws Exception {
		this.filterCondition = null;
		this.refresh();
	}

	public void setFilterCondition(Tunnel filterCondition) {
		this.filterCondition = filterCondition;
	}

	public Tunnel getFilterCondition() {
		return filterCondition;
	}
	
	private void flipRefresh(){
		view.getCurrPageLabel().setText(now+"");
    	try {
    		List<Tunnel> needTunnels = null;
    		if(now*ConstantUtil.flipNumber>infos.size()){
    			needTunnels = infos.subList((now-1)*ConstantUtil.flipNumber, infos.size());
    		}else{
    			needTunnels = infos.subList((now-1)*ConstantUtil.flipNumber, now*ConstantUtil.flipNumber);
    		}
    		this.view.clear();
			this.view.getLspPanel().clear();
			this.view.getQosPanel().clear();
			this.view.initData(needTunnels);
			this.view.updateUI();
		} catch (Exception e) {
			ExceptionManage.dispose(e, this.getClass());
		}finally{
		}
	}
	
	@Override
    public void prevPage()throws Exception{
    	now = now-1;
    	if(now == 1){
    		view.getPrevPageBtn().setEnabled(false);
    	}
    	view.getNextPageBtn().setEnabled(true);
    	
    	flipRefresh();
    }
	
	@Override
	public void goToAction() throws Exception {
		if (CheckingUtil.checking(view.getGoToTextField().getText(), CheckingUtil.NUM1_9)) {// 判断填写是否为数�?			
			Integer goi = Integer.parseInt(view.getGoToTextField().getText());
			if(goi>= total){
				goi = total;
				view.getNextPageBtn().setEnabled(false);
			}
			if(goi == 1){
				view.getPrevPageBtn().setEnabled(false);
			}
			if(goi > 1){
				view.getPrevPageBtn().setEnabled(true);
			}
			if(goi<total){
				view.getNextPageBtn().setEnabled(true);
			}
			now = goi;
			flipRefresh();
		}else{
			DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.MESSAGE_NUMBER));
		}
	}
	
	@Override
	public void nextPage() throws Exception {
		now = now+1;
		if(now == total){
			view.getNextPageBtn().setEnabled(false);
		}
		view.getPrevPageBtn().setEnabled(true);
		flipRefresh();
	}
}
