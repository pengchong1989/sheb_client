package com.nms.ui.ptn.statistics.pathstatics;

import java.util.List;

import com.nms.db.bean.report.SSPath;
import com.nms.db.enums.EOperationLogType;
import com.nms.model.report.StaticsticsService_MB;
import com.nms.model.util.ExportExcel;
import com.nms.model.util.Services;
import com.nms.service.impl.util.ResultString;
import com.nms.ui.frame.AbstractController;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.ListingFilter;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.keys.StringKeysTitle;

/**
 * 端到端路径数量统计控制类
 * @author xuxx
 */
public class PathNumStatisticsController extends AbstractController{

	private PathNumStatisticsPanel view;
	
	PathNumStatisticsController(PathNumStatisticsPanel pathNumStatisticsPanel){
		this.setView(pathNumStatisticsPanel);
	}
	@Override
	public void refresh() throws Exception {
		this.searchAndrefreshdata();
	}

	public PathNumStatisticsPanel getView() {
		return view;
	}

	public void setView(PathNumStatisticsPanel view) {
		this.view = view;
	}
	/**
	 * 导出统计数据保存到excel
	 */
	@Override
	public void export() throws Exception {
		List<SSPath> infos = null;
		String result;
		ExportExcel export=null;
		// 得到页面信息
		try {
			infos =  this.view.getTable().getAllElement();
			export=new ExportExcel();
			//得到bean的集合转为  String[]的List
			List<String[]> beanData=export.tranListString(infos,"PathNumStatisticsPanel");
			//导出页面的信息-Excel
			result=export.exportExcel(beanData, "PathNumStatisticsPanel");
			//添加操作日志记录
			this.insertOpeLog(EOperationLogType.PATHNUM.getValue(),ResultString.CONFIG_SUCCESS, null, null);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			infos = null;
			result=null;
			export=null;
		}
	}
	
	private void insertOpeLog(int operationType, String result, Object oldMac, Object newMac){
		AddOperateLog.insertOperLog(null, operationType, result, oldMac, newMac, 0,ResourceUtil.srcStr(StringKeysTitle.TIT_PATHNUM),"");		
	}
	
	/**
	 * 页面初始化数据、点击刷新按钮刷新数据
	 */
	private void searchAndrefreshdata() {
		List<SSPath> infos = null;
		StaticsticsService_MB service = null;
		ListingFilter filter=null;
		try {
			service = (StaticsticsService_MB) ConstantUtil.serviceFactory.newService_MB(Services.STATISTICS);
			filter=new ListingFilter();
			infos = (List<SSPath>) filter.filterList(service.SSPathSelect());
			this.view.clear();
			this.view.initData(infos);
			this.view.updateUI();
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		}finally {
			UiUtil.closeService_MB(service);
		}
	}
}
