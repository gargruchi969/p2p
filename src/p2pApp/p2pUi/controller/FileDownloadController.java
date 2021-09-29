package p2pApp.p2pUi.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import p2pApp.SearchResults;
import p2pApp.p2pDownloader.DownloadNodes;

import static p2pApp.p2pUi.AppUi.FXML_PATH;
import static p2pApp.p2pUi.AppUi.IMG_PATH;

public class FileDownloadController implements Initializable {

	@FXML private Label title, titleLabel, closeLabel;
	@FXML private Button pauseButton, closeButton, openExplorer;
	@FXML private ProgressBar progressBar;
	@FXML private ImageView image;
	@FXML private AnchorPane anchorPane;
	@FXML private Label speedLabel;

	private Desktop desktop = Desktop.getDesktop();
	private SearchResults sr;
	private DownloadNodes node;
	private Stage stage;
	private boolean reported = false;

	@Override 
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		image.setImage(new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "Downloads.png")));
	}

	@FXML protected void closeClicked(MouseEvent ae){
		if(node.isComplete)
			stage.hide();
		else
			showCloseConfirm();
	}

	@FXML protected void mouseEntered(MouseEvent ae){
		closeLabel.setStyle("-fx-background-color: red");
	}

	@FXML protected void mouseExited(MouseEvent ae){
		closeLabel.setStyle("-fx-background-color:  #9dd2d3");
	}
	
	@FXML protected void minClicked(MouseEvent ae){
		stage.setIconified(true);
	}

	@FXML protected void openInExplorer(ActionEvent ae){
		openFileLocation(utility.Utilities.outputFolder+ utility.Utilities.parseInvalidFilenames(sr.getFilename()));
	}

	@FXML protected void controlAction(ActionEvent ae){
		if(pauseButton.getText().contains("Open")){
			openFile(new File(utility.Utilities.outputFolder+ utility.Utilities.parseInvalidFilenames(sr.getFilename())));
		}

		if(pauseButton.getText().equals("Pause")){
			pauseButton.setText("Resume");
			titleLabel.setText("Download Paused");
			node.pauseDownload();
			return;
		}

		if(pauseButton.getText().equals("Resume")){
			node.resumeDownload();
			pauseButton.setText("Pause");
			titleLabel.setText("Downloading file");
			setProgress();
			return;
		}
	}

	@FXML protected void stopAction(ActionEvent ae){
		node.stopDownload();
		stage.close();
	}

	public void endDownload(){
		node.stopDownload();
		stage.close();
	}
	
	public void setDownloadNode(final DownloadNodes node){
		this.node= node;
		this.sr= node.getSearchResults();
		title.setText(sr.getFilename());
		setProgress();
	}

	public void setProgress(){
		Thread timer= new Thread(){
			public void run(){
				try{
					while(true){
						Thread.sleep(500);
						Platform.runLater(new Runnable(){
							public void run(){	
								try{	
									
									if(node.isComplete){
										progressBar.setProgress(1);
										pauseButton.setText("Open file");
										titleLabel.setText("Download Completed");
										closeButton.setText("Close");
										openExplorer.setVisible(true);
										reported= true;
									}
									else
										progressBar.setProgress(node.getPercent()/100.0);
									
									speedLabel.setText(node.getSpeed(8)+"\nMbps");
								}
								catch(Exception e){
									System.out.println("Error from file download controller #1: " + e.getMessage());
								}
							}
						});
						
						if(node.isPaused || node.isStopped)
							break;
						if(node.isComplete && reported)
							break;
					}	
				}
				catch(Exception e){
					System.out.println("Error #1" + e.getMessage());
				}
			}
		};
		timer.setDaemon(true);
		timer.start();
	}

	public void setupStage(Stage stage){
		this.stage= stage;
	}

	private void openFile(File file) {
		try {
			desktop.open(file);
		} catch (IOException ex) {  
		}
	}

	public static void openFileLocation(String path) {
		try{
			Runtime.getRuntime().exec("explorer.exe /select," + path.replace("/", "\\"));
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}

	public void showCloseConfirm(){
		
		try {
			Stage dialog = new Stage();
			dialog.initOwner(stage);
			
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "alert.fxml"));
			Parent root = (Parent)loader.load();
			AlertController controller = (AlertController)loader.getController();
			controller.setupDetails(dialog,
					"exit-confirm", FileDownloadController.this,
					"Stop downloading "+sr.getFilename()+"?", "Closing this window will stop download of the file.", "Yes", "No");
			
			Scene scene = new Scene(root);
			dialog.setScene(scene);
			dialog.initStyle(StageStyle.UNDECORATED);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.show();
		} catch (IOException e) {
		}

	}

	//callback functions

	public void completeDownload(String action, Object o){
		SearchResults sr= (SearchResults)o;

		try{
			if(this.sr.getFileId().equals(sr.getFileId())&&
					this.sr.getFileSize().equals(sr.getFileSize())){

				Platform.runLater(new Runnable(){
					public void run(){		
						pauseButton.setText("Open file");
						titleLabel.setText("Download Completed");
						closeButton.setText("Close");
						openExplorer.setVisible(true);
					}
				});

			}
		}
		catch(Exception e){
		}
	}
}
