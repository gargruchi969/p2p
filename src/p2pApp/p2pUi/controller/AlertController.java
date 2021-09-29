package p2pApp.p2pUi.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import p2pApp.SearchResults;
import p2pApp.p2pQueries.GetDirQuery;

public class AlertController {
	
	@FXML private Label infoLabel, titleLabel;
	@FXML private Button okBtn, cancelBtn;
	@FXML private VBox vBox;
	@FXML private AnchorPane anchorPane;
	@FXML private TextArea textArea;
	
	private Stage stage;
	private Object obj;
	private String action;
	
	public void setupDetails(Stage stage, String action, Object obj, String title, String info, String bt1, String bt2){
		this.stage= stage;
		this.obj= obj;
		this.action= action;
		
		if(title==null)
			titleLabel.setVisible(false);
		else
			titleLabel.setText(title);
		
		if(info==null)
			infoLabel.setVisible(false);
		else
			infoLabel.setText(info);
		
		if(bt1==null){
			okBtn.setVisible(false);
			cancelBtn.setDefaultButton(true);
		}
		else
			okBtn.setText(bt1);
		
		cancelBtn.setText(bt2);
	}
	
	@FXML public void okClicked(ActionEvent ae){
		stage.hide();
		if(action.equals("exit-confirm")){
			if(obj instanceof UIController){
				((UIController)obj).exitApp();
			}
			if(obj instanceof DirDownloadController){
				((DirDownloadController)obj).stopAllDownloads();
			}
			if(obj instanceof FileDownloadController){
				((FileDownloadController)obj).endDownload();
			}
		}
	}
	
	@FXML public void cancelClicked(ActionEvent ae){
		stage.hide();
	}
	
	//callback
	public void fileList(String act, final Object obj){
		action="";
		if(act.equals("p2p-app-dir-listfiles-only")){
			GetDirQuery gdq= (GetDirQuery)obj;
			for(int i=0;i<gdq.files.size();i++){
				SearchResults sr= gdq.files.get(i);
				action= action+ " -> "+sr.getFilename()+"\n";
			}
			Platform.runLater(new Runnable(){
				public void run(){
					GetDirQuery gdq= (GetDirQuery)obj;
					titleLabel.setText("List of files in "+gdq.name+". <Total: "+gdq.files.size()+">");
					infoLabel.setVisible(false);
					textArea.setVisible(true);
					textArea.setText(action);
					double h= 20.0* gdq.files.size();
					if(h>300)
						h=300;
					cancelBtn.setTranslateY(h-30);
					textArea.setPrefHeight(h);
					vBox.setPrefHeight(h+100);
					stage.sizeToScene();
				}
			});
		}
	}
}
