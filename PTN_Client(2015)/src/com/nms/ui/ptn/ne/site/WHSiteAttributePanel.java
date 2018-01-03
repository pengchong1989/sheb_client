package com.nms.ui.ptn.ne.site;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.enums.EOperationLogType;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.util.Services;
import com.nms.rmi.ui.util.RmiKeys;
import com.nms.service.impl.util.ResultString;
import com.nms.ui.manager.AddOperateLog;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.DispatchUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.MyActionListener;
import com.nms.ui.manager.ResourceUtil;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.control.PtnButton;
import com.nms.ui.manager.keys.StringKeysBtn;
import com.nms.ui.manager.keys.StringKeysLbl;
import com.nms.ui.manager.keys.StringKeysPanel;
import com.nms.ui.manager.keys.StringKeysTab;

public class WHSiteAttributePanel extends JPanel {

	private static final long serialVersionUID = 2934469265263389739L;
	private SiteInst siteInst = null;

	public WHSiteAttributePanel() throws Exception {
		initComponents();
		this.setMainLayout();
		this.setLayout();
		this.addListener();
	}

	private void addListener() {
		this.btnShow.addActionListener(new MyActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					WHSiteAttributePanel.this.showSite();
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

	private void showSite() throws Exception{
		DispatchUtil siteDispatch = null;
		SiteService_MB siteService = null;
		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			siteDispatch = new DispatchUtil(RmiKeys.RMI_SITE);
			siteInst = siteDispatch.selectSite(ConstantUtil.siteId);
			this.initData(siteInst);
			this.insertOpeLog(EOperationLogType.SELECTNE.getValue(), ResultString.CONFIG_SUCCESS, null, null);			
			if(siteInst != null &&  null!=siteInst.getSite_Hum_Id()){
				siteService.saveOrUpdate(siteInst);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			siteDispatch = null;
			siteInst = null;
			UiUtil.closeService_MB(siteService);
			siteService = null;
		}
	}

	private void insertOpeLog(int operationType, String result, Object oldMac, Object newMac){
		AddOperateLog.insertOperLog(btnShow, operationType, result, oldMac, newMac, ConstantUtil.siteId,ResourceUtil.srcStr(StringKeysPanel.PANEL_SITE_ATTRIBUTE),"");		
	}
	
	private void initData(SiteInst siteInst) {
		if(siteInst != null&&  null!=siteInst.getSite_Hum_Id()){
				if("ETN-200-204".equals(siteInst.getCellType())){
					siteInst.setVersions("V2.1.3");
					siteInst.setHardEdition("EB204.002V03");
					this.bootTimeJTextField.setText("V1.12");
				}else if("ETN-200-204E".equals(siteInst.getCellType())){
					siteInst.setVersions("V2.1.3");
					siteInst.setHardEdition("EB204.002V03");
					this.bootTimeJTextField.setText("V1.12");
				}else if("ETN-5000".equals(siteInst.getCellType())){
					siteInst.setVersions("V3.2.5");
					siteInst.setHardEdition("EB5000.003V01");
					this.bootTimeJTextField.setText("V2.01");
				}
				
			this.txtDescribe.setText(siteInst.getSite_Hum_Id());
			this.softWareText.setText(siteInst.getSoftEdition());
			this.totalTimetext.setText(siteInst.getTotalTime());
			this.siteTimeTextField.setText(siteInst.getCellTime());
			
			this.fpgaTimeJTextField.setText(siteInst.getFpgaTime());
			this.fpgaTimeJTextField.setText(siteInst.getFpgaTime());
			this.plateJTextField.setText(siteInst.getPlateNumber());
			this.cardJTextField.setText(siteInst.getCardNumber());
			this.programmeTimeField.setText(siteInst.getProgrammeTime());
			this.createJTextField.setText(siteInst.getCreatePlateNumber());
//			this.neMacField.setText(siteInst.getNeMAC());
		}
		this.txtDescribe.setEditable(false);
		this.softWareText.setEditable(false);
		this.totalTimetext.setEditable(false);
		this.siteTimeTextField.setEditable(false);
		this.bootTimeJTextField.setEditable(false);
		this.fpgaTimeJTextField.setEditable(false);
		this.createJTextField.setEditable(false);
		this.plateJTextField.setEditable(false);
		this.cardJTextField.setEditable(false);
		this.programmeTimeField.setEditable(false);
		this.txtDescribe.setEditable(false);
		this.softWareText.setEditable(false);
		this.totalTimetext.setEditable(false);
		this.siteTimeTextField.setEditable(false);
		this.bootTimeJTextField.setEditable(false);
		this.fpgaTimeJTextField.setEditable(false);
		this.createJTextField.setEditable(false);
		this.plateJTextField.setEditable(false);
		this.cardJTextField.setEditable(false);
		this.programmeTimeField.setEditable(false);
//		this.neMacField.setEditable(false);
	}

	private void initComponents() {
		this.tabPanel = new JTabbedPane();
		this.contentPanel = new JPanel();
		this.lblDescribe = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_DESC));
		this.txtDescribe = new JTextField();
		this.softWareLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.SOFTWARE_EDITION));
		this.softWareText = new JTextField();
		this.totalTimeLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.TOTAL_TIME));
		this.totalTimetext = new JTextField();
		this.siteTimeLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_SITE_TIME));
		this.siteTimeTextField = new JTextField();
		this.btnShow = new PtnButton(ResourceUtil.srcStr(StringKeysBtn.BTN_SELECT));
		this.bootTimeJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_BOOT_TIME));
		this.bootTimeJTextField = new JTextField();
		this.fpgaTimeJLabel = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_FPGA_TIME));
		this.fpgaTimeJTextField = new JTextField();
		plateNumber = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_PLATE_NUMBER));//盘号
		plateJTextField = new JTextField();
		cardNumber = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_CARD_NUMBER));//卡号
		cardJTextField = new JTextField();
		createPlateTime = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_PROGRAMME_TIME));//制盘时间
		createJTextField = new JTextField();
		programmeTime = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_CREATE_PLATE_TIME));//编程时间
		programmeTimeField = new JTextField();
//		neMac = new JLabel(ResourceUtil.srcStr(StringKeysLbl.LBL_CREATE_PLATE_MAC));//设备MAC地址
//		neMacField = new JTextField();
	}

	private void setMainLayout() {
		this.setBorder(BorderFactory.createTitledBorder(ResourceUtil.srcStr(StringKeysPanel.PANEL_SITE_ATTRIBUTE)));
		this.tabPanel.add(ResourceUtil.srcStr(StringKeysTab.TAB_BASIC_INFO), this.contentPanel);

		GridBagLayout contentLayout = new GridBagLayout();
		this.setLayout(contentLayout);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = new Insets(0, 10, 0, 10);
		c.fill = GridBagConstraints.BOTH;
		contentLayout.setConstraints(this.tabPanel, c);
		this.add(this.tabPanel);

	}

	private void setLayout() {
		this.contentPanel.setBackground(Color.WHITE);

		GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[] { 150, 210, 40, 40 };
		layout.columnWeights = new double[] { 0, 0, 0, 0.2 };
		layout.rowHeights = new int[] { 10, 35, 35, 35, 35, 35, 35, 35, 35, 35, 20, 35, 35 };
		layout.rowWeights = new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.2 };
		this.contentPanel.setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();

		/** 第一�?网元描述 */
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(this.lblDescribe, c);
		this.contentPanel.add(this.lblDescribe);
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(this.txtDescribe, c);
		this.contentPanel.add(this.txtDescribe);

		/** 第二�?盘号 */
		c.gridx = 0;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
//		layout.setConstraints(this.plateNumber, c);
//		this.contentPanel.add(this.plateNumber);
		c.gridx = 1;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
//		layout.setConstraints(this.plateJTextField, c);
//		this.contentPanel.add(this.plateJTextField);

		/** 第三�?软件版本 */
		c.gridx = 0;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(this.softWareLabel, c);
		this.contentPanel.add(this.softWareLabel);
		c.gridx = 1;
		c.gridy = 2;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(this.softWareText, c);
		this.contentPanel.add(this.softWareText);

		/** 第四�?卡号 */
		c.gridx = 0;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(this.cardNumber, c);
		this.contentPanel.add(this.cardNumber);
		c.gridx = 1;
		c.gridy = 3;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(this.cardJTextField, c);
		this.contentPanel.add(this.cardJTextField);

		/** 第五�?制盘时间 */
		c.gridx = 0;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
//		layout.setConstraints(this.createPlateTime, c);
//		this.contentPanel.add(this.createPlateTime);
		c.gridx = 1;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
//		layout.setConstraints(this.createJTextField, c);
//		this.contentPanel.add(this.createJTextField);

		/** 第六�?编程时间 */
		c.gridx = 0;
		c.gridy = 6;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
//		layout.setConstraints(this.programmeTime, c);
//		this.contentPanel.add(this.programmeTime);
		c.gridx = 1;
		c.gridy = 6;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
//		layout.setConstraints(this.programmeTimeField, c);
//		this.contentPanel.add(this.programmeTimeField);


		/** 第七�?网元时间服务�?*/
		c.gridx = 0;
		c.gridy = 7;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
//		layout.setConstraints(this.totalTimeLabel, c);
//		this.contentPanel.add(this.totalTimeLabel);
		c.gridx = 1;
		c.gridy = 7;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
//		layout.setConstraints(this.totalTimetext, c);
//		this.contentPanel.add(this.totalTimetext);
		
		/** 第八�?网元时间 */
		c.gridx = 0;
		c.gridy = 4;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(this.siteTimeLabel, c);
		this.contentPanel.add(this.siteTimeLabel);
		c.gridx = 1;
		c.gridy = 4;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(this.siteTimeTextField, c);
		this.contentPanel.add(this.siteTimeTextField);
		
		/** 第九�?BOOT时间 */
		c.gridx = 0;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 20, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(this.bootTimeJLabel, c);
		this.contentPanel.add(this.bootTimeJLabel);
		c.gridx = 1;
		c.gridy = 5;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(this.bootTimeJTextField, c);
		this.contentPanel.add(this.bootTimeJTextField);
			
		/** 第十�?网元MAC地址 */
//		c.gridx = 0;
//		c.gridy = 10;
//		c.gridheight = 1;
//		c.gridwidth = 1;
//		c.insets = new Insets(5, 20, 5, 5);
//		c.fill = GridBagConstraints.NONE;
//		c.anchor = GridBagConstraints.WEST;
//		layout.setConstraints(this.neMac, c);
//		this.contentPanel.add(this.neMac);
//		c.gridx = 1;
//		c.gridy = 10;
//		c.gridheight = 1;
//		c.gridwidth = 2;
//		c.insets = new Insets(5, 5, 5, 5);
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.anchor = GridBagConstraints.CENTER;
//		layout.setConstraints(this.neMacField, c);
//		this.contentPanel.add(this.neMacField);

		
		
		/** 第十�?网元时间 */
//		c.gridx = 0;
//		c.gridy = 10;
//		c.gridheight = 1;
//		c.gridwidth = 1;
//		c.insets = new Insets(5, 20, 5, 5);
//		c.fill = GridBagConstraints.NONE;
//		c.anchor = GridBagConstraints.WEST;
//		layout.setConstraints(this.fpgaTimeJLabel, c);
//		this.contentPanel.add(this.fpgaTimeJLabel);
//		c.gridx = 1;
//		c.gridy = 10;
//		c.gridheight = 1;
//		c.gridwidth = 2;
//		c.insets = new Insets(5, 5, 5, 5);
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.anchor = GridBagConstraints.CENTER;
//		layout.setConstraints(this.fpgaTimeJTextField, c);
//		this.contentPanel.add(this.fpgaTimeJTextField);

		
		
		
		/** 第十一�?按钮 */
		c.gridx = 2;
		c.gridy = 6;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = new Insets(5, 5, 5, 5);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		layout.setConstraints(this.btnShow, c);
		this.contentPanel.add(this.btnShow);

	}

	private JTabbedPane tabPanel;
	private JPanel contentPanel;
	private JLabel lblDescribe;
	private JTextField txtDescribe;
	private JLabel softWareLabel;//软件版本
	private JTextField softWareText;
	private JLabel totalTimeLabel;//累计上电时间
	private JTextField totalTimetext;
	private JLabel siteTimeLabel;//网元时间
	private JTextField siteTimeTextField;
	private JLabel bootTimeJLabel;//boot编译时间
	private JTextField bootTimeJTextField;
	private JLabel fpgaTimeJLabel;//fpga编译时间
	private JTextField fpgaTimeJTextField;
	private JLabel plateNumber;//盘号
	private JTextField plateJTextField;
	private JLabel cardNumber;//卡号
	private JTextField cardJTextField;
	private JLabel createPlateTime;//制盘时间
	private JTextField createJTextField;
	private JLabel programmeTime;//编程时间
	private JTextField programmeTimeField;
//	private JLabel neMac;//设备MAC地址
//	private JTextField neMacField;
	private PtnButton btnShow;
}
