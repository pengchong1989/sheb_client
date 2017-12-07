package com.nms.ui.frame;

public abstract class AbstractController {
	// 创建
	public void openCreateDialog() throws Exception {
	};

	// 刷新
	public abstract void refresh() throws Exception;

	// 删除
	public void delete() throws Exception {
	};

	// 修改
	public void openUpdateDialog() throws Exception {
	};

	// 设置过滤
	public void openFilterDialog() throws Exception {
	};

	// 清除过滤
	public void clearFilter() throws Exception {
	};

	// 导入
	public void inport() throws Exception {
	};

	// 界面零值过�?	
	public void filterZero() throws Exception {
		
	};
	// 选中一条记录后，查看详细信�?	
	public void initDetailInfo() {
	};

	// 搜索
	public void search() throws Exception {
	};

	// 同步
	public void synchro() throws Exception {
	};

	/**
	 * 导出
	 * 
	 * @author kk
	 * 
	 * @Exception 异常对象
	 */
	public void export() throws Exception {
	};

	/**
	 * 导出所有的
	 * 
	 * @throws Exception
	 */
	public void allExport() throws Exception {
	};

	/**
	 * 删除前的验证
	 * 
	 * @return true 验证成功�?false 验证失败
	 * @throws Exception
	 */
	public boolean deleteChecking() throws Exception {
		return true;
	}
	
	/**
	 * 一致性检�?	 */
	public void consistence() throws Exception{
		
	}

	/**
	 * 上一�?	 */
	public void prevPage() throws Exception {
		
	}

	/**
	 * 下一�?	 */
	public void nextPage() throws Exception {
		
	}
	
	public void goToAction()throws Exception {
		
	}
	
	public void query()throws Exception {
		
	}
}
