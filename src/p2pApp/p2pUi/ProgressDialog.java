package p2pApp.p2pUi;

import java.awt.event.WindowEvent;

import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import utility.PercentKeeper;

public class ProgressDialog extends javax.swing.JDialog implements Runnable{

	static final long serialVersionUID= 2;
	JScrollPane jp;
	String filename;
    
    public ProgressDialog(java.awt.Frame parent, boolean modal, String filename) {
        this(parent, modal, filename, null);
    }

    public ProgressDialog(java.awt.Frame parent, boolean modal, String filename, PercentKeeper pk) {
        super(parent, modal);
        this.pk= pk;
        this.filename= filename;
        new Thread(this).start();
    }
    
    public void run(){
    	initComponents();
    	this.setVisible(true);
//    	jp.requestFocus();
    }
    
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar1.setValue(0);
        jProgressBar1.setStringPainted(true);
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText(filename);

        jButton1.setText("Stop");
        
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        //jProgressBar1.setValue(10);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(76, 76, 76)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(200, 200, 200)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(73, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(jButton1)
                .addGap(35, 35, 35))
        );

        pack();
        new Task(this).start();
    }
    
    
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
//    	task = new Task();                
//        task.start();
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
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }
    
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JProgressBar jProgressBar1; 
    PercentKeeper pk;
    
    private class Task extends Thread { 
    	ProgressDialog pd;
        public Task(ProgressDialog pd){
        	this.pd= pd;
        }

        public void run(){
           for(int i =0; i<= 100; i+=1){
        	  i= pk.getVal();
              final int progress = i;
              SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                    jProgressBar1.setValue(progress);
                 }
              });
              try {
            	  if(progress>=100)
            		  Thread.sleep(700);
                 Thread.sleep(50);
              } catch (InterruptedException e) {}
           }
           
           
           pd.dispatchEvent(new WindowEvent(pd, WindowEvent.WINDOW_CLOSING));
}
    }
}