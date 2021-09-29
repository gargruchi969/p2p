package p2pApp.p2pUi.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PreferencesDialogController  implements Initializable {
	@FXML private Button saveBtn;
	@FXML private Button addPathBtn;
	@FXML private TextField outputFolder;
	@FXML private TextField ipAddress;
	@FXML private TextField inputPath, playerLocation;
	@FXML private ComboBox<String> inputDepth;
	@FXML private TextArea addedPaths;
	@FXML private Label statusLabel, closeLabel;
	@FXML private Button fileBtn;
	@FXML private Button folderBtn;

	private String systemId;
	private String userName;
	
	Stage stage=null;
	private final ObservableList<String> depths =
			FXCollections.observableArrayList();   


	@FXML protected void closeClicked(MouseEvent ae){
		stage.close();
	}
	
	@FXML protected void mouseEntered(MouseEvent ae){
		closeLabel.setStyle("-fx-background-color: red");
	}

	@FXML protected void mouseExited(MouseEvent ae){
		closeLabel.setStyle("-fx-background-color:  #9dd2d3");
	}

	@FXML protected void addNewPath(ActionEvent ae){
		if(inputPath.getText().length()>2){
			String str= inputPath.getText();
			if(inputDepth.getSelectionModel().getSelectedItem().equals("Default")){
				str= str+"::"+128;
			}
			else
				str= str+"::"+inputDepth.getSelectionModel().getSelectedItem();

			addedPaths.setText(addedPaths.getText()+"\n"+str);
			inputPath.setText("");
		}
		statusLabel.setText("");
	}

	@FXML protected void saveChanges(ActionEvent ae){
		FileOutputStream fos = null;

		try{

			Properties props = new Properties();

			props.setProperty("p2p.baseIp", ipAddress.getText());
			if(systemId!=null)
				props.setProperty("p2p.systemId", systemId);
			if(userName==null){
				userName="";
				props.setProperty("p2p.userName", userName);
			}
			
			if(outputFolder.getText().length()<3){
				throw new Exception();
			}

			props.setProperty("p2p.outputFolder", outputFolder.getText());
			props.setProperty("p2p.streamPlayer", playerLocation.getText());
			
			if(addedPaths.getText().length()<3){
				throw new Exception();
			}

			props.setProperty("p2p.inputFolder", addedPaths.getText().replace("\n", ", "));
			fos = new FileOutputStream("./data/config.properties");
			props.store(fos, "p2p properties");
			fos.close();
			statusLabel.setText("Saved: \u2713");
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			statusLabel.setText("\u2718 Failed");
		}
		finally{
		}
	}

	@FXML protected void openFileList(ActionEvent ae){
		statusLabel.setText("");
		final FileChooser fileChooser =
				new FileChooser();
		final File selectedFile =
				fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			inputPath.setText(selectedFile.getAbsolutePath());
		}
	}

	@FXML protected void openDirList(ActionEvent ae){
		statusLabel.setText("");
		final DirectoryChooser directoryChooser =
				new DirectoryChooser();
		final File selectedDirectory =
				directoryChooser.showDialog(stage);
		if (selectedDirectory != null) {
			inputPath.setText(selectedDirectory.getAbsolutePath());
		}
	}

	@FXML protected void outputDir(ActionEvent ae){
		statusLabel.setText("");
		final DirectoryChooser directoryChooser =
				new DirectoryChooser();
		final File selectedDirectory =
				directoryChooser.showDialog(stage);
		if (selectedDirectory != null) {
			outputFolder.setText(selectedDirectory.getAbsolutePath().replace("\\", "/")+"/");
		}
	}

	@FXML protected void locatePlayer(ActionEvent ae){
		statusLabel.setText("");
		final FileChooser fileChooser =
				new FileChooser();
		final File selectedFile =
				fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			String loc= selectedFile.getAbsolutePath();
			if(loc.contains(".exe") && loc.contains("mpc"))
				playerLocation.setText(loc);
		}
	}
	
	@FXML protected void notifyChanges1(ActionEvent ae){
		statusLabel.setText("");
	}

	@FXML protected void notifyChanges2(ActionEvent ae){
		statusLabel.setText("");
	}

	@FXML protected void notifyChanges3(KeyEvent ae){
		statusLabel.setText("");
	}

	public void setupStage(Stage stage){
		this.stage= stage;
	}

	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		try{
			setValues();
		}
		catch(Exception e){

		}
	}

	private void setValues() throws Exception{

		ipAddress.setTooltip(new Tooltip("Set the default ip address of the network"));
		outputFolder.setTooltip(new Tooltip("Set the location to save the files downloaded"));
		depths.add("Default");
		depths.add("0");
		depths.add("1");
		depths.add("2");
		depths.add("3");
		depths.add("4");
		inputDepth.setItems(depths);
		inputDepth.getSelectionModel().select(0);
		inputDepth.setTooltip(new Tooltip("Define the depth upto which a folder is explored"));

		inputPath.setTooltip(new Tooltip("Specify a new location to enable it for sharing"));
		addPathBtn.setTooltip(new Tooltip("Add the new location"));
		saveBtn.setTooltip(new Tooltip("Save the changes. Restart the server to enable the changes"));
		playerLocation.setTooltip(new Tooltip("Specify the location of the player for streaming"));
		fileBtn.setTooltip(new Tooltip("Specify the file you want to share"));
		folderBtn.setTooltip(new Tooltip("Specify the folder you want to share"));

		Properties props = new Properties();
		FileInputStream fis = null;

		fis = new FileInputStream("./data/config.properties");
		props.load(fis);
		fis.close();

		systemId= props.getProperty("p2p.systemId");
		userName= props.getProperty("p2p.userName");
		
		if(systemId==null){
			systemId= utility.Utilities.getSystemId();
		}

		ipAddress.setText(props.getProperty("p2p.baseIp"));
		outputFolder.setText(props.getProperty("p2p.outputFolder"));
		playerLocation.setText(props.getProperty("p2p.streamPlayer"));
		String str= props.getProperty("p2p.inputFolder");
		addedPaths.setText(str.replace(", ", "\n").replace(",", "\n"));
	}
}
