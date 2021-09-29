package p2pApp.p2pUi.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import p2pApp.SearchResults;

import static p2pApp.p2pUi.AppUi.FXML_PATH;
import static p2pApp.p2pUi.AppUi.IMG_PATH;

public class ResultRow {

	@FXML private VBox vBox;
	@FXML private Label filename;
	@FXML private Label filesize;
	@FXML private ImageView image;
	@FXML private Label sourceip;
	@FXML private Label moreip;
	@FXML private Label dirlabel;
	@FXML private Button downloadbtn;
	@FXML private Button streambtn;
	
	private Image fileImage, dirImage;
	UIController controller;
		
	public ResultRow(UIController controller){
		this.controller= controller;
		
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "results_row.fxml"));
		fxmlLoader.setController(this);
		
		try{
			fxmlLoader.load();
			fileImage= new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "file_r.png"));
			dirImage= new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "folder.png"));
		}
		catch(Exception e){
			System.out.println("Failed to load the list row: "+e.getMessage());
		}
	}
	
	@FXML public void dirClicked(MouseEvent me){
		controller.showFileListDialog(controller.resultList.getSelectionModel().getSelectedIndex());
	}
	
	public void setDetails(final SearchResults sr){
		filename.setText(sr.getFilename());
		filesize.setText(utility.Utilities.humanReadableByteCount(sr.getFileSize(), false));
		filename.setTooltip(new Tooltip(sr.getFilename()));
		
		if(sr.getType().equals("1")){
			dirlabel.setVisible(false);
			image.setImage(fileImage);
		}
		else{
			dirlabel.setVisible(true);
			dirlabel.setTooltip(new Tooltip("CLICK to get list of files."));
			image.setImage(dirImage);
		}
		
		if(sr.getStream()==2)
			streambtn.setVisible(true);
		else
			streambtn.setVisible(false);
		
		if(sr.getIp().equals(utility.Utilities.getIpAddress()))
			sourceip.setText("me");
		else 
			sourceip.setText(sr.getIp());
		
		if(sr.getAlternateIps().size()>0){
			moreip.setVisible(true);
			moreip.setText(""+sr.getAlternateIps().size());
		}
		else
			moreip.setVisible(false);
		
		downloadbtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(sr.getType().equals("1")){
                	controller.showFileDownloadDialog(sr);
                }
                if(sr.getType().equals("2")){
                	controller.showDirDownloadDialog(sr);
                }
            }
        });
		
		streambtn.setOnAction(new EventHandler<ActionEvent>(){
			
			@Override
			public void handle(ActionEvent event){
				utility.Utilities.streamOnCommandLine(sr.getIp(), sr.getStream(), sr.getFileId(), sr.getFilename());
			}
		});
	}
	
	public VBox getRow(){
		return vBox;
	}
}
