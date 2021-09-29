package p2pApp.p2pUi.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import p2pApp.p2pDownloader.DownloadEngine;
import p2pApp.p2pDownloader.DownloadNodes;
import p2pApp.p2pQueries.GetDirQuery;

import static p2pApp.p2pUi.AppUi.FXML_PATH;
import static p2pApp.p2pUi.AppUi.IMG_PATH;

public class DirDownloadController implements Initializable {

	@FXML private Label speedLabel, titleLabel, minLabel, closeLabel;
	@FXML private Label dirsize;
	@FXML private Label dirname;
	@FXML private ProgressBar progressBar;
	@FXML private Label progressLabel, filesCount;
	@FXML public ListView<DownloadNodes> listView;
	@FXML private Button openButton, pauseButton, stopButton;
	@FXML private ImageView image;
	
	private Stage stage;
	public long totalSize=0;
	private boolean isComplete;
	private long speed;
	private double percent=0;
	private long prevDone=0;
	public Image runImage;
	public Image pauseImage;
	public Image cancelImage;
	boolean stopUpdates = false;
	
	private final ObservableList<DownloadNodes> files= 
			FXCollections.observableArrayList();

	@FXML protected void onOpenPressed(ActionEvent ae){
		openFolderLocation(utility.Utilities.outputFolder + dirname.getText());
	}

	@FXML protected void minClicked(MouseEvent ae){
		stage.setIconified(true);
	}
	
	@FXML protected void closeClicked(MouseEvent ae){
		if(isComplete)
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
	
	public void stopAllDownloads(){
		if(stopButton.getText().equals("Stop")){
			stopUpdates= true;
			for(int i=0;i<files.size();i++){
				files.get(i).stopDownload();
			}
		}
		stage.hide();
	}
	
	@FXML protected void onPausePressed(ActionEvent ae){
		if(pauseButton.getText().equals("Pause")){
			pauseButton.setText("Resume");
			titleLabel.setText("Download Paused");
			stopUpdates= true;
			for(int i=0; i< files.size();i++){
				files.get(i).pauseDownload();
			}
			return;
		}

		if(pauseButton.getText().equals("Resume")){
			pauseButton.setText("Pause");
			setUpdates();
			titleLabel.setText("Downloading directory");
			stopUpdates= false;
			for(int i=0; i< files.size();i++){
				files.get(i).resumeDownload();
			}
			return;
		}
	}

	@FXML protected void onStopPressed(ActionEvent ae){
		if(isComplete)
			stage.hide();
		else
			showCloseConfirm();
	}

	@Override 
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		image.setImage(new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "img_dir.png")));
		listView.setItems(files);
		listView.setCellFactory(new Callback<ListView<DownloadNodes>, ListCell<DownloadNodes>>()
		{
			@Override
			public ListCell<DownloadNodes> call(ListView<DownloadNodes> listView)
			{
				return new ListViewCell();
			}
		});

		runImage= new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "image_pl.png"));
		pauseImage= new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "images_ps.png"));
		cancelImage= new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "images_cn.png"));
	}

	public void setupStage(Stage stage){
		this.stage= stage;
	}

	public void setDetails(String name, String size){
		dirname.setText(name);
		dirsize.setText(utility.Utilities.humanReadableByteCount(size, false)+"\nSize");
		totalSize= Long.parseLong(size);
	}

	//callback functions
	public void fileList(String action, Object obj){
		if(action.equals("p2p-app-dir-listfiles")){
			GetDirQuery gdq= (GetDirQuery)obj;
			files.addAll(DownloadEngine.getInstance().addMultiple(gdq.name, gdq.files, false));
			Platform.runLater(new Runnable(){
				public void run(){		
					filesCount.setText("Total files: "+ files.size());
				}
			});
			DownloadEngine.getInstance().startDownloading();
			setUpdates();
		}
	}

	private class ListViewCell extends ListCell<DownloadNodes>{
		private ListRow listRow= null;
		ListViewCell(){
			listRow= new ListRow(DirDownloadController.this);
		}
		@Override
		public void updateItem(DownloadNodes node, boolean empty){
			super.updateItem(node, empty);
			if(node!=null && node.getSearchResults().getType().equals("1")){
				listRow.setDetails(node);
				setGraphic(listRow.getRow());
			}
		}
	}

	private void setUpdates(){
		Thread timer= new Thread(){
			public void run(){
				try{
					while(true){
						Thread.sleep(500);
						computeUpdates();
						Platform.runLater(new Runnable(){
							public void run(){	
								listView.refresh();
								speedLabel.setText(""+speed+ " Mbps");
								progressBar.setProgress(percent);
								progressLabel.setText((long)(percent*100)+"%");
								if(isComplete){
									stage.setTitle("Download Completed");
									pauseButton.setDisable(true);
									stopButton.setText("Close");
								}
							}
						});
						if(isComplete || stopUpdates)
							break;
						else
							Thread.sleep(500);
					}
				}
				catch(Exception e){
				}
			}
		};
		timer.setDaemon(true);
		timer.start();
	}

	public void computeUpdates(){
		speed= 0;
		isComplete = true;
		long sizeDone = 0;
		for(int i=0; i<files.size();i++){
			DownloadNodes node= files.get(i);
			if(!node.isStopped && !node.isComplete)
				isComplete= false;
			sizeDone= sizeDone + node.getSizeDone();
		}
		speed= sizeDone- prevDone;
		speed= speed * 8 / (1024 * 1024);
		if(speed<0)
			speed= 0;
		prevDone= sizeDone;
		if(isComplete)
			percent= 1;
		else
			percent= (double)sizeDone/totalSize;
	}

	public void openFolderLocation(String path) {
		try{
			Desktop.getDesktop().open(new File(path.replace("/", "\\")));
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
					"exit-confirm", DirDownloadController.this,
					"Stop downloading all files?", "Closing this window will stop download of all the files.", "Yes", "No");
			
			Scene scene = new Scene(root);
			dialog.setScene(scene);
			dialog.initStyle(StageStyle.UNDECORATED);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.show();
		} catch (IOException e) {
		}

	}

}

