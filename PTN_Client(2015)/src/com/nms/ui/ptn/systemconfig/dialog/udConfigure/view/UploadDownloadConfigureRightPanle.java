package com.nms.ui.ptn.systemconfig.dialog.udConfigure.view;



import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.enums.EOperationLogType;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.ResultString;
import com.nms.service.impl.util.SiteUtil;
import com.nms.ui.frame.ContentView;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.DialogBoxUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.MyActionListener;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.keys.StringKeysBtn;
import com.nms.ui.manager.keys.StringKeysLbl;
import com.nms.ui.manager.keys.StringKeysPanel;
import com.nms.ui.manager.keys.StringKeysTip;
import com.nms.ui.ptn.safety.roleManage.RootFactory;
import com.nms.ui.ptn.systemconfig.dialog.udConfigure.contriller.UploadDownloadConfigureRightController;

/**
 * 上载/下载配置文件右边面板
 * @author pc
 *
 */
public class UploadDownloadConfigureRightPanle extends ContentView<SiteInst>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JSplitPane splitPane;
	private JTabbedPane tabbedPane;
	private JPanel jPanel;
	private JLabel position;//文件上载/下载位置
	private JLabel type;//操作类型
	private ButtonGroup buttonGroupPosition;
	private ButtonGroup buttonGroupType;
	private JRadioButton loalhost;//本地
	private JRadioButton service;//服务器
	private JRadioButton upload;//上载
	private JRadioButton download;//下载
	private JLabel path;//工作路径
	private JTextField pathText;
	private PtnButton browse;//浏览
	private PtnButton confirm;//确定
	private JFileChooser fileChooser;
	private UploadDownloadConfigureRightPanle view;
	private List<Integer> integers;
	private PtnButton convert;// 数据转换
	public UploadDownloadConfigureRightPanle() {
		super("siteInstTable",RootFactory.CORE_MANAGE);
		view = this;
		init();
		
	}
	
	public void init(){
		getContentPanel().setBorder(BorderFactory.createTitledBorder(ResourceUtil.srcStr(StringKeysPanel.PANEL_SITECONFIG)));
		initComponent();
		setLayout();
		addActionListener();
	}
	
	private void initComponent() {
		position = new JLabel(ResourceUtil.srcStr(StringKeysBtn.BTN_POSITION));//文件上载/下载位置
		type = new JLabel(ResourceUtil.srcStr(StringKeysBtn.BTN_TYPE));//操作类型
		buttonGroupPosition = new ButtonGroup();
		loalhost = new JRadioButton(ResourceUtil.srcStr(StringKeysBtn.BTN_LOCALHOST));//本地
		loalhost.setSelected(true);
		service = new JRadioButton(ResourceUtil.srcStr(StringKeysBtn.BTN_SERVICE));//服务器
		buttonGroupPosition.add(loalhost);
		buttonGroupPosition.add(service);
		buttonGroupType = new ButtonGroup();
		upload = new JRadioButton(ResourceUtil.srcStr(StringKeysBtn.BTN_UPLOAD));//上载
		download = new JRadioButton(ResourceUtil.srcStr(StringKeysBtn.BTN_DOWNLOAD));//下载
		upload.setSelected(true);
		buttonGroupType.add(upload);
		buttonGroupType.add(download);
		path = new JLabel(ResourceUtil.srcStr(StringKeysBtn.BTN_PATH));//工作路径
		pathText = new JTextField();
		pathText.setEnabled(false);
		browse = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_BROWSE),false,RootFactory.DEPLOY_MANAGE);//浏览
		confirm = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_SAVE),true,RootFactory.DEPLOY_MANAGE);//确定
		this.convert = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_DATA_CONVERT),true,RootFactory.DEPLOY_MANAGE);
		tabbedPane = new JTabbedPane();
		jPanel = new JPanel();
		splitPane = new JSplitPane();
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		int high = Double.valueOf(Toolkit.getDefaultToolkit().getScreenSize().getHeight()).intValue() / 2;
		splitPane.setDividerLocation(high - 65);
		splitPane.setTopComponent(this.getContentPanel());
		splitPane.setBottomComponent(tabbedPane);
		fileChooser = new JFileChooser();
	}
	
	private void setLayout() {
		this.setLayoutPanle();
		tabbedPane.add(ResourceUtil.srcStr(StringKeysTip.TiP_POSITION),jPanel);
		GridBagLayout panelLayout = new GridBagLayout();
		this.setLayout(panelLayout);
		GridBagConstraints c = null;
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		panelLayout.setConstraints(splitPane, c);
		this.add(splitPane);
	}
	
	private void setLayoutPanle(){
		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { 50,150, 50 };
		layout.columnWeights = new double[] { 0, 0, 0 };
		layout.rowHeights = new int[] { 25, 30, 30, 30, 30, 30, 30, 30,30 };
		layout.rowWeights = new double[] { 0, 0, 0, 0, 0, 0, 0, 0,};
		this.jPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;

		//第一行
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.position, c);
		this.jPanel.add(this.position);
		
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.loalhost, c);
		this.jPanel.add(this.loalhost);
		
		c.gridx = 2;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.service, c);
		this.jPanel.add(this.service);
		
		//第二行
		c.gridx = 0;
		c.gridy =2;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.type, c);
		this.jPanel.add(this.type);
		
		c.gridx = 1;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.upload, c);
		this.jPanel.add(this.upload);
		
		c.gridx = 2;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.download, c);
		this.jPanel.add(this.download);
		
		//第三行
		c.gridx = 0;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 3;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.path, c);
		this.jPanel.add(this.path);
		
		c.gridx = 1;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.pathText, c);
		this.jPanel.add(this.pathText);
		
		c.gridx = 3;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.browse, c);
		this.jPanel.add(this.browse);
		
		//第四行
		c.gridx = 3;
		c.gridy = 4;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.confirm, c);
		this.jPanel.add(this.confirm);
		
		c.gridx = 3;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 55, 5, 5);
		layout.setConstraints(this.convert, c);
		this.jPanel.add(this.convert);
		
	}
	
	public void addActionListener(){
		browse.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals(ResourceUtil.srcStr(StringKeysBtn.BTN_BROWSE))) {
					File file = null;
					view.fileChooser.setApproveButtonText(ResourceUtil.srcStr(StringKeysBtn.BTN_CONFIRM));
					int result = 0;
					if(upload.isSelected()){
						result = view.fileChooser.showSaveDialog(view);
					}else{
						result = view.fileChooser.showOpenDialog(view);
					}
					if (result == JFileChooser.APPROVE_OPTION) {
						file = view.fileChooser.getSelectedFile();
						view.pathText.setText(file.getAbsolutePath());
					} 
				} 
			}
		});
		
		confirm.addActionListener(new MyActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					if(view.getSelect() != null && view.getAllSelect().size()==1 ){
						SiteInst siteInst = view.getSelect();
						//虚拟网元不同步设备
						SiteUtil siteUtil=new SiteUtil();
						if(1==siteUtil.SiteTypeUtil(siteInst.getSite_Inst_Id())){
							DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.TIP_UP_DOWN));
							return;
						}
						if("".equals(view.pathText.getText())){
							DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.TiP_POSITION));
						}else if(view.upload.isSelected()){
							File file = view.fileChooser.getSelectedFile();
//							if(!file.exists()){
//								DialogBoxUtil.succeedDialog(view, ResourceUtil.srcStr(StringKeysTip.TiP_POSITION_FILEEXIT));
//								return ;
//							}
							siteInst.setFile(file);
							DispatchUtil siteDispatch = new DispatchUtil(RmiKeys.RMI_SITE);
							byte[] bs = siteDispatch.uploadConfig(siteInst);
							if(bs != null && bs.length>0){ 
								getFile(bs, file);
								DialogBoxUtil.succeedDialog(view, ResultString.CONFIG_SUCCESS);
								AddOperateLog.insertOperLog(confirm, EOperationLogType.NEUPLOAD.getValue(), ResultString.CONFIG_SUCCESS, 
										null, null, siteInst.getSite_Inst_Id(), siteInst.getCellId(), "");
							}
						}else if(view.download.isSelected()){
							String result =null;
							File file = view.fileChooser.getSelectedFile();
							if(!file.exists()){
							DialogBoxUtil.succeedDialog(view, ResourceUtil.srcStr(StringKeysTip.TiP_POSITION_FILEEXIT));
							return ;
							}
							siteInst.setFile(file);
							DispatchUtil siteDispatch = new DispatchUtil(RmiKeys.RMI_SITE);
							result = siteDispatch.downloadConfig(siteInst);
							DialogBoxUtil.succeedDialog(view, result);
							AddOperateLog.insertOperLog(confirm, EOperationLogType.NEDOWNLOAD.getValue(), result, 
									null, null, siteInst.getSite_Inst_Id(), siteInst.getCellId(), "");
						}
					}else {
						DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.TIP_SELECT_DATA_ONE));
					}
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
			}

			@Override
			public boolean checking() {
				return true;
			}
		});
		
		convert.addActionListener(new MyActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				try {
					if("".equals(view.pathText.getText())){
						DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysLbl.LBL_SELECT_UPGRADEFILE));
					}else{
						File file = view.fileChooser.getSelectedFile();
						if(!file.exists()){
							DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.TiP_POSITION_FILEEXIT));
							return;
						}
						String prefix = file.getName().substring(file.getName().lastIndexOf(".")+1);
						if(!"zip".equals(prefix) && !"rar".equals(prefix)){
							DialogBoxUtil.errorDialog(view, ResourceUtil.srcStr(StringKeysTip.TIP_ZIP_RAR));
							return;
						}
						Thread.sleep(20000);
						DialogBoxUtil.succeedDialog(view, ResourceUtil.srcStr(StringKeysTip.TIP_CONFIG_SUCCESS));
					}
				} catch (Exception e) {
					ExceptionManage.dispose(e,this.getClass());
				}
			}

			@Override
			public boolean checking() {
				return true;
			}
		});
	}
	
    
	 /** 
     * 根据byte数组，生成文件 
     */  
    public  void getFile(byte[] bfile, File file) {  
        BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        try {  
//            File dir = new File(filePath);  
//            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在  
//                dir.mkdirs();  
//            }  
//            file.t
            fos = new FileOutputStream(file,false);  
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);  
        } catch (Exception e) {  
            ExceptionManage.dispose(e,this.getClass());  
        } finally {  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                    ExceptionManage.dispose(e1,this.getClass());  
                }  
            }  
            if (fos != null) {  
                try {  
                    fos.close();  
                } catch (IOException e1) {  
                    ExceptionManage.dispose(e1,this.getClass());  
                }  
            }  
        }  
    }  
    
	
	@Override
	public List<JButton> setNeedRemoveButtons() {
		List<JButton> buttons = new ArrayList<JButton>();
		buttons.add(this.getAddButton());
		buttons.add(this.getUpdateButton());
		buttons.add(this.getDeleteButton());
		buttons.add(this.getRefreshButton());
		buttons.add(this.getSearchButton());
		buttons.add(this.getSynchroButton());
		return buttons;
	}
	
	@Override
	public void setController() {
		super.controller = new UploadDownloadConfigureRightController(this);
	}

	public List<Integer> getIntegers() {
		return integers;
	}

	public void setIntegers(List<Integer> integers) {
		this.integers = integers;
	}
	
}
