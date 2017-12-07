/*
 * AddCESPanel.java
 *
 * Created on __DATE__, __TIME__
 */

package com.nms.ui.ptn.business.dialog.cespath;

import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import twaver.Element;
import twaver.Node;
import twaver.PopupMenuGenerator;
import twaver.TView;
import twaver.network.TNetwork;

import com.nms.db.bean.equipment.port.PortInst;
import com.nms.db.bean.equipment.shelf.SiteInst;
import com.nms.db.bean.ptn.path.ces.CesInfo;
import com.nms.db.bean.ptn.path.pw.PwInfo;
import com.nms.db.bean.ptn.path.tunnel.Tunnel;
import com.nms.db.bean.system.Field;
import com.nms.db.enums.EActiveStatus;
import com.nms.db.enums.EServiceType;
import com.nms.model.equipment.port.PortService_MB;
import com.nms.model.equipment.shlef.SiteService_MB;
import com.nms.model.ptn.path.pw.PwInfoService_MB;
import com.nms.model.ptn.path.tunnel.TunnelService_MB;
import com.nms.model.system.FieldService_MB;
import com.nms.model.util.Services;
import com.nms.ui.manager.ConstantUtil;
import com.nms.ui.manager.ControlKeyValue;
import com.nms.ui.manager.DateUtil;
import com.nms.ui.manager.ExceptionManage;
import com.nms.ui.manager.UiUtil;
import com.nms.ui.manager.control.PtnDialog;
import com.nms.ui.ptn.business.ces.CesBusinessPanel;
import com.nms.ui.ptn.business.dialog.tunnel.TunnelTopoPanel;

/**
 * 
 * @author __USER__
 */
public class AddCESPanel extends PtnDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private CesBusinessPanel cesPathPanel;
	private PortInst portInst_a = null;
	private PortInst portInst_z = null;
	private CesInfo cesInfo;
	private AddCesDialog dialog=null;


	/** Creates new form AddCESPanel */
	public AddCESPanel(java.awt.Frame parent, boolean modal) {
		intialComponent();

	}

	public AddCESPanel(CesBusinessPanel cesPathPanel, boolean modal, CesInfo ces) {
		this.setModal(modal);
		intialComponent();
		this.cesInfo = ces;
		dialog=cesPathPanel.getDialog();
		this.cesPathPanel = cesPathPanel;
		this.bindPwComboxData(jComboBox1);

		if (cesInfo != null) {
			this.jTextField1.setText(cesInfo.getName());
			this.comboBoxSelect(jComboBox1, this.cesInfo.getPwId() + "");
			initialAZPortText(cesInfo);
			this.BtnIsactive.setSelected(ces.getActiveStatus() == EActiveStatus.ACTIVITY.getValue() ? false : true);
		}
	}

	private void intialComponent() {
		createComponents();
		addMyListener();

	}

	private void createComponents() {

		jLabel6 = new JLabel();
		BtnConfirm = new JButton();
		BtnCancel = new JButton();
		jTabbedPane1 = new JTabbedPane();
		jSplitPane1 = new JSplitPane();
		jPanel1 = new JPanel();
		nameL = new JLabel();
		pwNameL = new JLabel();
		leftL = new JLabel();
		rightL = new JLabel();
		activeL = new JLabel();
		jTextField1 = new JTextField();
		jComboBox1 = new JComboBox();
		BtnIsactive = new JCheckBox();
		jTextField2 = new JTextField();
		jTextField3 = new JTextField();

		jTextField2.setEditable(false);
		jTextField3.setEditable(false);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		jLabel6.setFont(new java.awt.Font("宋体", 0, 16));
		jLabel6.setText("\u521b\u5efaCES");

		BtnConfirm.setText("\u786e\u5b9a");

		BtnCancel.setText("\u53d6\u6d88");

		nameL.setText("\u540d  \u79f0");

		pwNameL.setText("PW\u540d\u79f0");

		leftL.setText("A\u7aef\u7aef\u53e3");

		rightL.setText("Z\u7aef\u7aef\u53e3");

		activeL.setText("\u6fc0\u6d3b\u72b6\u6001");

		BtnIsactive.setText("\u662f\u5426\u6fc0\u6d3b");

		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGap(16, 16, 16).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(nameL).addComponent(pwNameL).addComponent(leftL).addComponent(rightL).addComponent(activeL)).addGap(22, 22, 22).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addComponent(BtnIsactive).addComponent(jTextField1).addComponent(jComboBox1, 0, 101, Short.MAX_VALUE).addComponent(jTextField3).addComponent(jTextField2)).addGap(19, 19, 19)));
		jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				jPanel1Layout.createSequentialGroup().addGap(19, 19, 19).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(nameL).addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(pwNameL).addComponent(jComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(leftL).addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(
						jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(rightL).addComponent(jTextField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18, 18, 18).addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(activeL).addComponent(BtnIsactive)).addContainerGap(158, Short.MAX_VALUE)));

		jSplitPane1.setRightComponent(new TunnelTopoPanel());
		jSplitPane1.setLeftComponent(jPanel1);

		jTabbedPane1.addTab("\u57fa\u672c\u4fe1\u606f", jSplitPane1);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(21, 21, 21).addComponent(jLabel6).addContainerGap(621, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 680, GroupLayout.PREFERRED_SIZE).addContainerGap(18, Short.MAX_VALUE)).addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap(549, Short.MAX_VALUE).addComponent(BtnConfirm).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(BtnCancel).addGap(29, 29, 29)));
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel6).addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 387, GroupLayout.PREFERRED_SIZE).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE).addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(BtnConfirm).addComponent(BtnCancel)).addContainerGap()));

		pack();
	}

	public void bindPwComboxData(JComboBox jComboBox1) {

		PwInfoService_MB pwservice = null;
		TunnelService_MB tunnelservice = null;
		List<PwInfo> pwList = null;
		Tunnel tunnel = null;
		DefaultComboBoxModel pwModel = null;

		try {
			pwModel = (DefaultComboBoxModel) jComboBox1.getModel();
			tunnelservice = (TunnelService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Tunnel);
			pwservice = (PwInfoService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PwInfo);
			pwList = pwservice.select();

			for (PwInfo pw : pwList) {
				tunnel = new Tunnel();
				tunnel.setTunnelId(pw.getTunnelId());
				tunnel = tunnelservice.select(tunnel).get(0);
				//显示pw,但保存tunnel对象,为后面显示拓扑图
				pwModel.addElement(new ControlKeyValue(pw.getPwId() + "", pw.getPwName(), tunnel));
			}
			jComboBox1.setModel(pwModel);
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(pwservice);
			UiUtil.closeService_MB(tunnelservice);
			pwList = null;
			tunnel = null;
			pwModel = null;
		}
	}

	private void comboBoxSelect(JComboBox jComboBox, String string) {
		for (int i = 0; i < jComboBox.getItemCount(); i++) {
			if (((ControlKeyValue) jComboBox.getItemAt(i)).getId().equals(string)) {
				jComboBox.setSelectedIndex(i);
				return;
			}
		}
	}

	private void initialAZPortText(CesInfo cesInfo) {
		SiteService_MB siteService = null;
		PortService_MB portService = null;
		SiteInst siteInst = null;
		PortInst portInst = null;

		try {
			siteService = (SiteService_MB) ConstantUtil.serviceFactory.newService_MB(Services.SITE);
			portService = (PortService_MB) ConstantUtil.serviceFactory.newService_MB(Services.PORT);

			siteInst = siteService.select(cesInfo.getaSiteId());
			portInst = new PortInst();
			portInst.setPortId(cesInfo.getAportId());
			portInst = portService.select(portInst).get(0);
			setPortInfo(siteInst, portInst, "A");

			siteInst = siteService.select(cesInfo.getzSiteId());
			portInst = new PortInst();
			portInst.setPortId(cesInfo.getZportId());
			portInst = portService.select(portInst).get(0);
			setPortInfo(siteInst, portInst, "Z");

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(portService);
			UiUtil.closeService_MB(siteService);
		}
	}

	/**
	 * 给tunnel面板上的控件赋�?	 * 
	 * @param siteInst
	 * @param portInst
	 * @param type
	 */
	private void setPortInfo(SiteInst siteInst, PortInst portInst, String type) {
		Field field = null;
		FieldService_MB fieldService = null;
		List<Field> fieldList = null;
		String text = null;
		try {
			field = new Field();
			field.setId(siteInst.getFieldID());
			fieldService = (FieldService_MB) ConstantUtil.serviceFactory.newService_MB(Services.Field);
			fieldList = fieldService.select(field);
			if (fieldList == null || fieldList.size() != 1) {
				throw new Exception("查询field出错");
			}
			text = fieldList.get(0).getFieldName() + "/" + siteInst.getCellId() + "/" + portInst.getPortName();
			if (type.equals("A")) {
				this.setPortInst_a(portInst);
				this.jTextField2.setText(text);
			} else if (type.equals("Z")) {
				this.setPortInst_z(portInst);
				this.jTextField3.setText(text);
			}

		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			UiUtil.closeService_MB(fieldService);
		}
	}

	private void addMyListener() {

		BtnConfirm.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				BtnConfirmActionPerformed(evt);
			}
		});

		BtnCancel.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				BtnCancelActionPerformed(evt);
			}
		});

		jComboBox1.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				pwItemStateChanged(evt);
			}
		});
	}

	private void BtnCancelActionPerformed(java.awt.event.ActionEvent evt) {
		this.dispose();
	}

	private void BtnConfirmActionPerformed(java.awt.event.ActionEvent evt) {

		ControlKeyValue obj = null;
		try {
			obj = (ControlKeyValue) jComboBox1.getSelectedItem();
			if (jTextField1.getText().trim().length() == 0 || jTextField2.getText().trim().length() == 0 || jTextField3.getText().trim().length() == 0 || obj == null) {
				return;
			}

			if (cesInfo == null)
				cesInfo = new CesInfo();

			this.cesInfo.setName(jTextField1.getText());
			this.cesInfo.setPwId(Integer.valueOf(obj.getId()));
			this.cesInfo.setaAcId(portInst_a.getPortId());
			this.cesInfo.setAportId(((Tunnel) obj.getObject()).getAPortId());
			this.cesInfo.setaSiteId(portInst_a.getSiteId());

			this.cesInfo.setaAcId(portInst_z.getPortId());
			this.cesInfo.setZportId(((Tunnel) obj.getObject()).getZPortId());
			this.cesInfo.setzSiteId(portInst_z.getSiteId());

			this.cesInfo.setActiveStatus(BtnIsactive.isSelected() ? 1 : 0);
			this.cesInfo.setServiceType(EServiceType.CES.getValue());
			this.cesInfo.setCreateTime(DateUtil.getDate(DateUtil.FULLTIME));
			if(this.BtnIsactive.isSelected()){
				cesInfo.setActivatingTime(DateUtil.getDate(DateUtil.FULLTIME));
			}else{
				cesInfo.setActivatingTime(null);
			}
			this.cesInfo.setCreateUser(ConstantUtil.user.getUser_Name());

			this.dispose();
			if (null != this.cesPathPanel) {
				cesPathPanel.getController().refresh();
			}
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			obj=null;
		}

	}

	protected void pwItemStateChanged(ItemEvent evt) {
		ControlKeyValue controlKeyValue = (ControlKeyValue) evt.getItem();
		TNetwork network = null;
		try {
			if (null != cesInfo) {
				dialog.getTunnelTopoPanel().boxData(((Tunnel) controlKeyValue.getObject()).getTunnelId());
				network = dialog.getTunnelTopoPanel().getNetWork();
			} else {
				
				dialog.getTunnelTopoPanel().boxData(((Tunnel) controlKeyValue.getObject()).getTunnelId());
				network =dialog.getTunnelTopoPanel().getNetWork();
				network.setPopupMenuGenerator(new PopupMenuGenerator() {
					@Override
					public JPopupMenu generate(TView tview, MouseEvent mouseEvent) {

						JPopupMenu menu = new JPopupMenu();

						if (!tview.getDataBox().getSelectionModel().isEmpty()) {
							final Element element = tview.getDataBox().getLastSelectedElement();

							if (element instanceof Node) {
								if (element.getBusinessObject() != null) {
									JMenuItem jMenuItem = new JMenuItem("选择端口");
									jMenuItem.addActionListener(new java.awt.event.ActionListener() {
										@Override
										public void actionPerformed(java.awt.event.ActionEvent evt) {
											
										}
									});
									menu.add(jMenuItem);
								} else {
									return menu;
								}
							}
						}
						return menu;
					}

				});
			}
		} catch (NumberFormatException e) {
			ExceptionManage.dispose(e,this.getClass());
		} catch (Exception e) {
			ExceptionManage.dispose(e,this.getClass());
		} finally {
			controlKeyValue = null;
			network = null;
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				AddCESPanel dialog = new AddCESPanel(new JFrame(), true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					@Override
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

	public PortInst getPortInst_a() {
		return portInst_a;
	}

	public void setPortInst_a(PortInst portInst_a) {
		this.portInst_a = portInst_a;
	}

	public PortInst getPortInst_z() {
		return portInst_z;
	}

	public void setPortInst_z(PortInst portInst_z) {
		this.portInst_z = portInst_z;
	}

	private JButton BtnCancel;
	private JButton BtnConfirm;
	private JCheckBox BtnIsactive;
	private JLabel activeL;
	private JComboBox jComboBox1;
	private JLabel jLabel6;
	private JPanel jPanel1;
	private JSplitPane jSplitPane1;
	private JTabbedPane jTabbedPane1;
	private JTextField jTextField1;
	private JTextField jTextField2;
	private JTextField jTextField3;
	private JLabel leftL;
	private JLabel nameL;
	private JLabel pwNameL;
	private JLabel rightL;

}