package p2pApp.p2pUi;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import baseServer.BaseNetworkEngine;
import modules.InitModule;
import p2pApp.SearchResults;
import p2pApp.SearchTable;
import p2pApp.p2pDownloader.DownloadEngine;
import p2pApp.p2pDownloader.DownloadRequest;
import p2pApp.p2pQueries.GetDirQuery;
import p2pApp.p2pQueries.SearchQuery;
import tcpQueries.PingQuery;
import tcpServer.BaseController;
import tcpUtilities.PeersTable;
import utility.PercentKeeper;


public class UISearch extends javax.swing.JFrame {

	static DefaultTableModel model;

	public UISearch() {
		initComponents(null);
	}

	static final long serialVersionUID=1;

	private void initComponents(ArrayList<SearchResults> al) {

		jTextField1 = new javax.swing.JTextField();
		jButton1 = new javax.swing.JButton();
		jScrollPane2 = new javax.swing.JScrollPane();
		jTable1 = new javax.swing.JTable();
		pingTextField = new javax.swing.JTextField();
		pingButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		getRootPane().setDefaultButton(jButton1);

		jTextField1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jTextField1ActionPerformed(evt);
			}
		});

		jButton1.setText("Search");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jButton1.setEnabled(false);

		pingButton.setText("Ping");
		pingButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pingButtonActionPerformed(evt);
			}
		});
		
		pingTextField.addActionListener(new java.awt.event.ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				pingButtonActionPerformed(evt);
			}
		});
		
		model = new DefaultTableModel(new String [] {
				"Source", "Name", "Size", "Download"
		},0);
		jTable1.setAutoCreateRowSorter(true);
		jTable1.setModel(model);

		//        model.addRow(new Object [] {null, null, null, "Download"});
		//        jTable1.setModel(new javax.swing.table.DefaultTableModel(
		//            new Object [][] {
		//                {null, null, null, "Download"},
		//        		{null, null, null, "Download"},
		//                {null, null, null, "Download"},
		//            },
		//            new String [] {
		//                "Result", "Name", "Size", "Button"
		//            }
		//        ));
		jScrollPane2.setViewportView(jTable1);
		jTable1.getColumn("Download").setCellRenderer(new ButtonRenderer());
		jTable1.getColumn("Download").setCellEditor(new ButtonEditor(new JCheckBox()));


		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		//      layout.setHorizontalGroup(
		//      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		//      .addGroup(layout.createSequentialGroup()
		//          .addContainerGap()
		//          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		//              .addGroup(layout.createSequentialGroup()
		//                  .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
		//                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		//                  .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
		//              .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE))
		//          .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		//  );

		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(layout.createSequentialGroup()
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
												.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addGroup(layout.createSequentialGroup()
														.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(18, 18, 18)
														.addComponent(jButton1)))
														.addGap(0, 8, Short.MAX_VALUE))
														.addGroup(layout.createSequentialGroup()
																.addComponent(pingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
																//.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																.addGap(18, 18, 18)
																.addComponent(pingButton)))
																.addContainerGap())
				);
		//  layout.setVerticalGroup(
		//      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		//      .addGroup(layout.createSequentialGroup()
		//          .addContainerGap()
		//          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
		//              .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
		//              .addComponent(jTextField1))
		//          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		//          .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
		//          .addContainerGap())
		//  );
		layout.setVerticalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButton1))
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 402, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
										.addComponent(pingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(pingButton))
										.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);



		pack();
	}                       

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
		String str= jTextField1.getText().toString();
		BaseNetworkEngine.getInstance().sendMultipleRequests(new SearchQuery(SearchTable.getInstance().getNewSearchId(), "search", str, null), "p2p-app", "SearchQuery", false);
	}                                        

	private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {                                            
		jButton1.doClick();
	}       

	private void pingButtonActionPerformed(java.awt.event.ActionEvent evt) {                                         
		String str= pingTextField.getText().toString();
		BaseController.getInstance().sendRequest(new PingQuery("ping",null,null), "tcp-server", "PingQuery", true, "", str);
		pingTextField.setText("");
	} 
	
	public static void updateTable(ArrayList<SearchResults> al){
		if(al!=null){
			int len= model.getRowCount();
			for(int i=0;i<len;i++){
				model.removeRow(0);
			}
			for(int i=0;i<al.size();i++){
				SearchResults sr= al.get(i);
				model.addRow(new Object [] {sr.getIp(), sr.getFilename() , utility.Utilities.humanReadableByteCount(Long.parseLong(sr.getFileSize()), false) , "Download"});
			}
		}

	}

	public JRootPane createRootPane() {
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
		Action action = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				System.out.println("escaping..");
				setVisible(false);
				dispose();
			}
		};
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", action);
		return rootPane;
	}



	public static void main(String args[]) {

		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(UISearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(UISearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(UISearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(UISearch.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		Thread t= new Thread(){
			public void run(){
				UISearch ui = new UISearch();
				ui.setVisible(true);

			}
		};
		t.start();

		//        java.awt.EventQueue.invokeLater(new Runnable() {
			//            public void run() {
		//            	UISearch ui = new UISearch();
		//            	ui.setVisible(true);
		//                
		//            }
		//        });


		BaseController baseController= BaseController.getInstance();
		baseController.startServer();
		new InitModule();


		if(BaseNetworkEngine.getInstance().connectToNetwork()){
			jButton1.setEnabled(true);
		}

		String input="";
		Scanner sc=new Scanner(System.in);

		while(true){

			input= sc.nextLine();
			if(input.contains("close")){

				try{
					baseController.stopServer();
					sc.close();
					break;
				}
				catch(Exception e){
					System.out.println("Failed to stop the server: "+e.getMessage());
				}

			}
			else if(input.contains("ping-message-all")){
				String data= input.substring(17);
				BaseNetworkEngine.getInstance().sendMultipleRequests(new PingQuery("ping-message-all", null, null, data), "tcp-server", "PingQuery", false);
			}
			else if(input.contains("ping-message")){
				String data= input.substring(12);
				baseController.sendRequest(new PingQuery("ping-message", null, null, data), "tcp-server", "PingQuery", false, "", utility.Utilities.getIpAddress());
			}
			else if(input.contains("ping")){
				String data= input.substring(input.indexOf("1"));
				baseController.sendRequest(new PingQuery(input.substring(0, input.indexOf("1")-1),null,null), "tcp-server", "PingQuery", true, "", data);
				//baseController.sendRequest(new PingQuery("ping",null,null), "tcp-server","PingQuery", true, "", ip));
			}
			else if(input.contains("show-peers")){
				PeersTable.getInstance().echoEntries();
			}
			else if(input.contains("show-npeers")){
				PeersTable.getInstance().echoNeighbours();
			}
			else if(input.contains("query")){
				BaseNetworkEngine.getInstance().sendMultipleRequests(new SearchQuery(SearchTable.getInstance().getNewSearchId(), "search", input.substring(6, input.length()), null), "p2p-app", "SearchQuery", false);
				//baseController.sendRequest(input.substring(6, input.length()), "p2p-app", "string", false, "", utility.Utilities.getIpAddress());
			}
			else if(input.contains("download")){
				String []arr= input.split(" ");
				new DownloadRequest(arr[2],arr[1],arr[3]);
			}
			else if(input.contains("getfile")){
				String []arr= input.split(" ");
				int index= Integer.parseInt(arr[1]);
				SearchResults sr= SearchTable.getInstance().getFromSearchTable(index);
				new DownloadRequest(sr.getFileId(), sr.getIp(), sr.getFilename(), sr.getUserid());
			}
			else
				baseController.sendRequest(input, "tcp-server", "string", true, "", utility.Utilities.getIpAddress());
		}
	}


	private static javax.swing.JButton jButton1;
	private static javax.swing.JScrollPane jScrollPane2;
	private static javax.swing.JTable jTable1;
	private javax.swing.JTextField jTextField1;
	private javax.swing.JButton pingButton;
	private javax.swing.JTextField pingTextField;

	public static void enableSearchButon(boolean enable){
		jButton1.setEnabled(enable);
	}


}

//
//class ButtonEditor extends DefaultCellEditor {
//
//    protected JButton button;
//    private String label;
//    private boolean isPushed;
//
//    public ButtonEditor(JCheckBox checkBox) {
//        super(checkBox);
//        button = new JButton();
//        button.setOpaque(true);
//        button.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                fireEditingStopped();
//                System.out.println("clicked");
//            }
//        });
//    }
//}
//
//class ButtonRenderer extends JButton implements TableCellRenderer {
//
//    public ButtonRenderer() {
//        setOpaque(true);
//    }
//
//    @Override
//    public Component getTableCellRendererComponent(JTable table, Object value,
//            boolean isSelected, boolean hasFocus, int row, int column) {
//        if (isSelected) {
//            setForeground(table.getSelectionForeground());
//            setBackground(table.getSelectionBackground());
//        } else {
//            setForeground(table.getForeground());
//            setBackground(UIManager.getColor("Button.background"));
//        }
//        setText((value == null) ? "" : value.toString());
//        return this;
//    }
//
//
//}


class ButtonEditor extends DefaultCellEditor {

	static final long serialVersionUID= 4;
	protected JButton button;
	private String label;
	private boolean isPushed;

	public ButtonEditor(JCheckBox checkBox) {
		super(checkBox);
		button = new JButton();
		button.setOpaque(true);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (isSelected) {
			button.setForeground(table.getSelectionForeground());
			button.setBackground(table.getSelectionBackground());
		} else {
			button.setForeground(table.getForeground());
			button.setBackground(table.getBackground());
		}
		label = (value == null) ? "" : value.toString();
		button.setText(label);
		startDownload(row);
		isPushed = true;
		return button;
	}

	public void startDownload(int row){

		final SearchResults sr= SearchTable.getInstance().getFromSearchTable(row);
		final PercentKeeper pk= new PercentKeeper();
		new ProgressDialog(new javax.swing.JFrame(), false, sr.getFilename(), pk);

		//    	java.awt.EventQueue.invokeLater(new Runnable() {
		//            public void run() {
		//                ProgressDialog dialog = new ProgressDialog(new javax.swing.JFrame(), true, sr.getFilename(), pk);
		//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
		//                    @Override
		//                    public void windowClosing(java.awt.event.WindowEvent e) {
		//                      //  System.exit(0);
		//                    }
		//                });
		//                dialog.setVisible(true);
		//                
		//            }
		//        });


		//new DownloadRequest(sr.getFileId(), sr.getIp(), sr.getFilename(), sr.getUserid(), pk);
		if(sr.getType().equals("1"))
		DownloadEngine.getInstance().addDownload(sr);
		if(sr.getType().equals("2")){
			GetDirQuery.sendDirQuery(sr);
		}
	}
	@Override
	public Object getCellEditorValue() {
		if (isPushed) {
			//JOptionPane.showMessageDialog(button, label + ": Ouch!");
			System.out.println("hello");
		}
		isPushed = false;
		return label;
	}

	@Override
	public boolean stopCellEditing() {
		isPushed = false;
		return super.stopCellEditing();
	}

	@Override
	protected void fireEditingStopped() {
		super.fireEditingStopped();
	}
}

class ButtonRenderer extends JButton implements TableCellRenderer {

	static final long serialVersionUID= 5;
	public ButtonRenderer() {
		//setOpaque(true);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if(isSelected){
			JButton button = new JButton(value.toString());
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					System.out.println("Clicked !");
				}
			});
		}
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(UIManager.getColor("Button.background"));
		}
		setText((value == null) ? "" : value.toString());
		return this;
	}
}

