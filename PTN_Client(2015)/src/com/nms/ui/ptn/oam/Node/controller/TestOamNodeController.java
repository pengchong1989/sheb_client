package com.nms.ui.ptn.oam.Node.controller;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import com.nms.db.bean.ptn.oam.OamInfo;
import com.nms.db.bean.ptn.oam.OamLinkInfo;
import com.nms.db.bean.ptn.oam.OamMepInfo;
import com.nms.db.enums.EOperationLogType;
import com.nms.db.enums.EServiceType;
import com.nms.model.ptn.oam.OamInfoService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.SiteUtil;
import com.nms.service.impl.util.WhImplUtil;
import com.nms.ui.frame.AbstractController;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.ptn.oam.Node.view.PwOamNodeDialog;
import com.nms.ui.ptn.oam.Node.view.SectionOamNodeDialog;
import com.nms.ui.ptn.oam.Node.view.TestOamNodePanel;
import com.nms.ui.ptn.oam.Node.view.TunnelOamNodeDialog;

	public class TestOamNodeController extends AbstractController {
		private TestOamNodePanel view;
		private List<OamInfo> oamInfoList = new ArrayList<OamInfo>();
		
		public TestOamNodeController(TestOamNodePanel testOamNodePanel) {
			this.view = testOamNodePanel;
			addListener();
		}

		@Override
		public void refresh() throws Exception {
			this.view.clear();
			this.view.initData(this.searchAndrefreshdata());
			this.view.updateUI();
		}

	/**
	 * 新建
	 */
	public void openCreateDialog() throws Exception {
		if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == 0) {
			DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.TIP_CHOOSE_TYPE));
		}
		if(oamInfoList != null && oamInfoList.size() > 9){
			DialogBoxUtil.confirmDialog(view, ResourceUtil.srcStr(StringKeysTip.TIP_OAM_LIMIT_10));
		}else{
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.SECTION.getValue()) {
				SectionOamNodeDialog dialog = new SectionOamNodeDialog(null, "Section OAM");
				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
				if(ResourceUtil.language.equals("zh_CN")){
					dialog.setSize(new Dimension(500, 450));
				}else{
					dialog.setSize(new Dimension(780, 450));
				}
				dialog.setVisible(true);
			}
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.TUNNEL.getValue()) {
				TunnelOamNodeDialog dialog = new TunnelOamNodeDialog(null);
				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
//				dialog.setSize(new Dimension(600, 450));
				if(ResourceUtil.language.equals("zh_CN")){
					dialog.setSize(new Dimension(750, 450));
				}else{
					dialog.setSize(new Dimension(980, 450));
				}
				
				dialog.setVisible(true);
			}
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.PW.getValue()) {
				PwOamNodeDialog dialog = new PwOamNodeDialog(null);
				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
				
				if(ResourceUtil.language.equals("zh_CN")){
					dialog.setSize(new Dimension(850, 450));
				}else{
					dialog.setSize(new Dimension(1100, 450));
				}
//				dialog.setSize(new Dimension(600, 450));
				dialog.setVisible(true);
			}
	//		if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.LINKOAM.getValue()) {
	//			NodeOAMTestDialog dialog = new NodeOAMTestDialog(view.getSelect(), "ETH OAM");
	//			dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
	//			dialog.setSize(new Dimension(500, 400));
	//			dialog.setVisible(true);
	////			view.getRefreshButton().doClick();
	//		}
	//		if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.ELINE.getValue()) {
	//			NodeOAMTestDialog dialog = new NodeOAMTestDialog(null, "ELine OAM");
	//			dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
	//			dialog.setSize(new Dimension(500, 400));
	//			dialog.setVisible(true);
	////			view.getRefreshButton().doClick();
	//		}
		}
		this.refresh();
	}
		
	// 删除
	@Override
	public void delete() throws Exception {
		OamInfoService_MB oamInfoService = null;
		List<OamInfo> oamInfoList = null;
		DispatchUtil tmsDispatch = null;
		DispatchUtil tmpDispatch = null;
		DispatchUtil tmcDispatch = null;
		String message = "";
		int operateKey = 0;
		List<Integer> siteIds = null;
		try {
			//判断是否为在线脱管网元
  			SiteUtil siteUtil = new SiteUtil();
  			if(1==siteUtil.SiteTypeOnlineUtil(ConstantUtil.siteId)){
  				WhImplUtil wu = new WhImplUtil();
  				siteIds = new ArrayList<Integer>();
  				siteIds.add(ConstantUtil.siteId);
  				String str=wu.getNeNames(siteIds);
  				DialogBoxUtil.errorDialog(this.view, ResourceUtil.srcStr(StringKeysTip.TIP_NOT_DELETEONLINE)+""+str+ResourceUtil.srcStr(StringKeysTip.TIP_ONLINENOT_DELETEONLINE));
  				return;
  			}
  			
			oamInfoService = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo);
			oamInfoList = this.getView().getAllSelect();
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.SECTION.getValue()) {
				tmsDispatch = new DispatchUtil(RmiKeys.RMI_TMSOAMCONFIG);
				message = tmsDispatch.excuteDelete(oamInfoList);
				operateKey = EOperationLogType.SEGMENTOAMTESTSDEL.getValue();
			} else if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.TUNNEL.getValue()) {
				tmpDispatch = new DispatchUtil(RmiKeys.RMI_TMPOAMCONFIG);
				message = tmpDispatch.excuteDelete(oamInfoList);
				operateKey = EOperationLogType.TUNNELOAMTESTSDEL.getValue();
			} else if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.PW.getValue()) {
				tmcDispatch = new DispatchUtil(RmiKeys.RMI_TMCOAMCONFIG);
				message = tmcDispatch.excuteDelete(oamInfoList);
				operateKey = EOperationLogType.PWOAMTESTSDEL.getValue();
			} else if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.ELINE.getValue()) {
//				oamDispatch = new TmsOamDispatch();
//				message = oamDispatch.excutionDelete(oamInfoList);
			} else {
				for (OamInfo oamInfo : oamInfoList) {
					oamInfoService.delete(oamInfo);
				}
			}
			// DialogBoxUtil.succeedDialog(this.getView(), ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS));
			refresh();
			DialogBoxUtil.succeedDialog(this.getView(), message);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(oamInfoService);
			oamInfoList = null;
		}

	};

	// 修改
	@Override
	public void openUpdateDialog() throws Exception {
		if (this.getView().getAllSelect().size() == 0) {
			DialogBoxUtil.errorDialog(this.getView(), ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_DATA_ONE));
		} else {
			OamInfo oamInfo = this.getView().getAllSelect().get(0);
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == 0) {
				DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.TIP_CHOOSE_TYPE));
//				view.getRefreshButton().doClick();
			}
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.SECTION.getValue()) {
				SectionOamNodeDialog dialog = new SectionOamNodeDialog(view.getSelect(), "Section OAM");
				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
//				dialog.setSize(new Dimension(600, 450));
				if(ResourceUtil.language.equals("zh_CN")){
					dialog.setSize(new Dimension(500, 450));
				}else{
					dialog.setSize(new Dimension(780, 450));
				}
				dialog.setVisible(true);
//				view.getRefreshButton().doClick();
			}
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.TUNNEL.getValue()) {
				TunnelOamNodeDialog dialog = new TunnelOamNodeDialog(view.getSelect());
				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
				if(ResourceUtil.language.equals("zh_CN")){
					dialog.setSize(new Dimension(750, 450));
				}else{
					dialog.setSize(new Dimension(980, 450));
				}
				dialog.setVisible(true);
//				view.getRefreshButton().doClick();
			}
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.PW.getValue()) {
				PwOamNodeDialog dialog = new PwOamNodeDialog(view.getSelect());
				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
				if(ResourceUtil.language.equals("zh_CN")){
					dialog.setSize(new Dimension(750, 450));
				}else{
					dialog.setSize(new Dimension(980, 450));
				}
				dialog.setVisible(true);
//				view.getRefreshButton().doClick();
			}
//			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.LINKOAM.getValue()) {
//				NodeOAMTestDialog dialog = new NodeOAMTestDialog(view.getSelect(), "ETH OAM");
//				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
//				dialog.setSize(new Dimension(500, 400));
//				dialog.setVisible(true);
////				view.getRefreshButton().doClick();
//			}
//			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.ELINE.getValue()) {
//				NodeOAMTestDialog dialog = new NodeOAMTestDialog(null, "ELine OAM");
//				dialog.setLocation(UiUtil.getWindowWidth(dialog.getWidth()) - 200, UiUtil.getWindowHeight(dialog.getHeight()) / 2 - 8);
//				dialog.setSize(new Dimension(500, 400));
//				dialog.setVisible(true);
////				view.getRefreshButton().doClick();
//			}
		}
		
		this.refresh();
	}

	private List<OamInfo> searchAndrefreshdata() throws Exception {
		OamInfoService_MB oamInfoService = null;
		OamInfo oamInfo = null;
		OamMepInfo oamMepInfo = null;
		OamLinkInfo oamLinkInfo = null;
		try {
			oamInfoService = (OamInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.OamInfo);
			oamInfo = new OamInfo();
			oamMepInfo = new OamMepInfo();
			oamLinkInfo = new OamLinkInfo();
			oamInfo.setOamMep(oamMepInfo);
			oamInfo.setOamLinkInfo(oamLinkInfo);
			if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.LINKOAM.getValue()) {
				oamInfo.getOamLinkInfo().setSiteId(ConstantUtil.siteId);
				oamLinkInfo.setObjType(EServiceType.LINKOAM.toString());
				oamInfoList = oamInfoService.queryLinkOAMBySiteId(oamInfo);
			} else {
				if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == 0) {
					oamInfoList = new ArrayList<OamInfo>();
				}else{
					if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.SECTION.getValue()) {
						oamMepInfo.setObjType(EServiceType.SECTION.toString()+"_TEST");
					}
					if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.TUNNEL.getValue()) {
						oamMepInfo.setObjType(EServiceType.TUNNEL.toString()+"_TEST");
					}
					if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.PW.getValue()) {
						oamMepInfo.setObjType(EServiceType.PW.toString()+"_TEST");
					}
//				if (Integer.parseInt(((ControlKeyValue) (view.getOamTypeComboBox().getSelectedItem())).getId()) == EServiceType.ELINE.getValue()) {
//					oamMepInfo.setObjType(EServiceType.ELINE.toString());
//				}
					oamInfo.getOamMep().setSiteId(ConstantUtil.siteId);
					oamInfo.setOamMep(oamMepInfo);
					oamInfoList = oamInfoService.queryBySiteIdAndType(oamInfo);
				}
			}
			return oamInfoList;
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
			throw e;
		} finally {
			UiUtil.closeService_MB(oamInfoService);
			oamInfo = null;
			oamMepInfo = null;
		}
	}

	private void addListener() {
//		this.view.getTable().addElementClickedActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				if (view.getSelect() == null) {
//					return;
//				} else {
//					initDetailInfo();
//				}
//
//			}
//		});

	};

//	@Override
//	public void initDetailInfo() {
//		OamInfo oamInfo = null;
//
//		try {
//			oamInfo = view.getSelect();
//			initBasicInfo(oamInfo);
//
//			view.updateUI();
//		} catch (Exception e) {
//			ExceptionManage.dispose(e,this.getClass());
//		} finally {
//			oamInfo = null;
//		}
//	}

//	private void initBasicInfo(OamInfo oamInfo) {
//		if (oamInfo.getOamMep() != null) {
//			this.view.getMelField().setText(oamInfo.getOamMep().getMel() + "");
//
//			this.view.getLckCheckBox().setSelected(oamInfo.getOamMep().isLck());
//			this.view.getLoopCheckBox().setSelected(oamInfo.getOamMep().isRingEnable());
//			this.view.getTstCheckBox().setSelected(oamInfo.getOamMep().isTstEnable());
//		}
//	}

	public TestOamNodePanel getView() {
		return view;
	}

	public void setView(TestOamNodePanel view) {
		this.view = view;
	}

}