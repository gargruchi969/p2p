package p2pApp.p2pUi.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import baseServer.BaseNetworkEngine;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import modules.InitModule;
import p2pApp.SearchResults;
import p2pApp.SearchTable;
import p2pApp.StreamServer;
import p2pApp.p2pDownloader.DownloadEngine;
import p2pApp.p2pQueries.GetDirQuery;
import p2pApp.p2pQueries.SearchQuery;
import tcpQueries.PingQuery;
import tcpServer.BaseController;
import tcpUtilities.CallbackRegister;
import tcpUtilities.PeersTable;
import utility.Utilities;

import static p2pApp.p2pUi.AppUi.FXML_PATH;
import static p2pApp.p2pUi.AppUi.IMG_PATH;

public class UIController implements Initializable{

	@FXML private TextField searchField;
	@FXML private Button searchButton, clrBtn, sendBtn;
	@FXML public ListView<SearchResults> resultList;
	@FXML private Label startLabel, settingsLabel, minLabel, closeLabel;
	@FXML private AnchorPane anchorPane;
	@FXML private Label totalResults;
	@FXML private TextArea consoleArea;
	@FXML private TextField textIp;
	@FXML private ListView<String> peersList;
	@FXML private TextField textMessage;
	@FXML private TextArea chatArea;
	
	private boolean streamStarted= false;
	private Stage stage;
	private PrintStream ps ;

	private final ObservableList<SearchResults> results =
			FXCollections.observableArrayList();   
	private final ObservableList<String> peers= 
			FXCollections.observableArrayList();
	
	public void setupStage(Stage stage){
		this.stage= stage;
	}

	@FXML protected void startClicked(MouseEvent ae){
		if(startLabel.getText().equals("Stop")){
			try{
				BaseController.getInstance().stopServer();
				startLabel.setText("Start");
				totalResults.setText("Server Offline!");
			}
			catch(Exception e){

			}
			return;
		}
		if(startLabel.getText().equals("Start")){
			startServer();
			startLabel.setText("Stop");
			totalResults.setText("Server Online!");
		}
	}

	@FXML protected void settingsClicked(MouseEvent ae){
		showPreferencesDialog();
	}

	@FXML protected void minClicked(MouseEvent ae){
		stage.setIconified(true);
	}

	@FXML protected void closeClicked(MouseEvent ae){
		if(BaseController.getInstance().isServerRunning())
			showCloseConfirm();
		else
			exitApp();
	}

	@FXML protected void mouseEntered(MouseEvent ae){
		closeLabel.setStyle("-fx-background-color: red");
	}

	@FXML protected void mouseExited(MouseEvent ae){
		closeLabel.setStyle("-fx-background-color:  #9dd2d3");
	}

	@FXML protected void enterOnMessage(KeyEvent ke){
		if(ke.getCode()== KeyCode.ENTER){
			sendBtn.fire();
			ke.consume();
		}
		
	}
	
	@FXML protected void sendMessage(ActionEvent ae){
		String text= textMessage.getText();
		if(text== null || text.length()==0)
			return;
		
		if(peers.size()==0){
			System.out.println("\n**Can't send the message. Not connected to anyone!");
			return;
		}
		textMessage.setText("");
		BaseNetworkEngine.getInstance().sendMultipleRequests(new PingQuery("ping-message-all", utility.Utilities.userName, null, text), "tcp-server", "PingQuery", false);
	}
	
	@FXML protected void pingIp(ActionEvent ae){
		String text= textIp.getText();
		textIp.setText("");
		if(utility.Utilities.validateIp(text))
			BaseController.getInstance().sendRequest(new PingQuery(), "tcp-server", "PingQuery", true, "", text);
		else
			System.out.println("\n** Error while pinging. Enter valid ip address!");
	}

	@FXML protected void handleClickOnSearch(ActionEvent ae){
		performSearch();
	}

	@FXML protected void handleKeyOnSearch(KeyEvent ke){
		if(ke.getCode()== KeyCode.ENTER){
			performSearch();
			ke.consume();
		}
	}

	private class ResultRowCell extends ListCell<SearchResults>{
		private ResultRow listRow= null;
		ResultRowCell(){
			listRow= new ResultRow(UIController.this);
		}
		@Override
		public void updateItem(SearchResults sr, boolean empty){
			super.updateItem(sr, empty);
			if(sr!=null){
				listRow.setDetails(sr);
				setGraphic(listRow.getRow());
			}
			else
				setGraphic(null);
		}
	}

	@FXML public void clearConsole(ActionEvent ae){
		consoleArea.clear();
	}

	private void performSearch(){
		if(!BaseController.getInstance().isServerRunning()){
			showAlert("Can't do the search!", "Server not started!", null, "OK");
			return;
		}

		if(peers.size()>0){
			String str= searchField.getText();
			totalResults.setText("Searching...");
			int id= SearchTable.getInstance().getNewSearchId(true);
			BaseNetworkEngine.getInstance().sendMultipleRequests(new SearchQuery(id, "search", str, null), "p2p-app", "SearchQuery", false);
		}
		else{
			showAlert("Can't do the search!", "Not connected to any user to perform the search!", null, "OK");
			textIp.requestFocus();
		}
	}

	@Override 
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		//        assert myButton != null : "fx:id=\"myButton\" was not injected: check your FXML file 'simple.fxml'.";

		// initialize your logic here: all @FXML variables will have been injected

		//totalResults.setText("hello");
		//makeDraggable();
		Image clr = new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "clear.png"));
		clrBtn.setGraphic(new ImageView(clr));
		clrBtn.setTooltip(new Tooltip("Clear the console area. Should be cleared to free up some memory!"));
		ps = new PrintStream(new Console(consoleArea)) ;

		if (!Utilities.debugMode) {
            System.setOut(ps);
            System.setErr(ps);
            System.out.println("System status area");
        }

		peersList.setItems(peers);
		peersList.setTooltip(new Tooltip("Shows the list of peers with which you are connected"));
		consoleArea.setTooltip(new Tooltip("Shows the status of the system"));

		resultList.setItems(results);
		resultList.setCellFactory(new Callback<ListView<SearchResults>, ListCell<SearchResults>>()
		{
			@Override
			public ListCell<SearchResults> call(ListView<SearchResults> listView)
			{
				return new ResultRowCell();
			}
		});
	}

	synchronized public void addResults(final ArrayList<SearchResults> sr,final int size){
		Platform.runLater(new Runnable(){
			public void run(){	
				results.clear();
				for(int i=0; i < size;i++){
					results.add(sr.get(i));
				}
				totalResults.setText("Total Results: " + sr.size());
			}
		});

	}

	public static void appendResults(ArrayList<SearchResults> sr){

	}

	private void startServer(){

		Thread serv= new Thread(){
			public void run(){
				BaseController.getInstance().startServer();
				try{
					new InitModule().initSystem();
					CallbackRegister.getInstance().registerForCallback("p2p-app-results", "p2pApp.p2pUi.controller.UIController", "handleResults", false, UIController.this);
					CallbackRegister.getInstance().registerForCallback("tcp-server-neighbours", "p2pApp.p2pUi.controller.UIController", "modifyPeersList", false, UIController.this);
					CallbackRegister.getInstance().registerForCallback("tcp-server-ping-message-all", "p2pApp.p2pUi.controller.UIController", "handleChatMessage", false, UIController.this);
					
					//returns true if peers table has some entries (some possibility for connections).
					//returns false if peers table is empty.
					if(!BaseNetworkEngine.getInstance().connectToNetwork()){
						Platform.runLater(new Runnable(){
							public void run(){		
								showAlert("Failed to locate the users.", "Ping an active user to connect.", null, "OK");
							}
						});
					}
				}
				catch(final Exception e){
					Platform.runLater(new Runnable(){
						public void run(){		
							startLabel.setText("Start");
							showAlert("Failed to start Share It!", e.getMessage(), null, "OK");
							totalResults.setText("Server Offline!");
						}
					});

					try{
						BaseController.getInstance().stopServer();

					}
					catch(Exception t){	}
				}
			}
		};
		serv.setDaemon(true);
		serv.start();

		Thread stream = new Thread(){
			public void run(){
				try{
					new StreamServer();
				}
				catch(Exception e){
					System.out.println("Streaming Server #1: "+e.getMessage());
				}
			}
		};

		if(!streamStarted){
			streamStarted= true;
			stream.setDaemon(true);
			stream.start();
		}
	}

	public void showPreferencesDialog(){
		try{

			Stage dialog = new Stage();
			dialog.setTitle("Preferences");
			dialog.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "File.png")));
			dialog.initOwner(anchorPane.getScene().getWindow());
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "settings.fxml"));
			Parent root = (Parent)loader.load();
			PreferencesDialogController controller = (PreferencesDialogController)loader.getController();
			controller.setupStage(dialog);
			Scene scene = new Scene(root);
			dialog.setScene(scene);
			makeDraggable(dialog);
			dialog.initStyle(StageStyle.UNDECORATED);
			dialog.show();
		}
		catch(Exception e){
			System.out.println("Unable to open the preferences "+e.getMessage());
		}
	}

	public void showFileDownloadDialog(SearchResults sr){
		try{
			Stage dialog = new Stage();
			dialog.setTitle("Downloading: "+sr.getFilename());
			dialog.setX(400+ 40*Math.random());
			dialog.setY(200+ 20*Math.random());
			dialog.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "File.png")));

			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "download_file.fxml"));
			Parent root = (Parent)loader.load();
			FileDownloadController controller = (FileDownloadController)loader.getController();
			controller.setupStage(dialog);
			Scene scene = new Scene(root);
			dialog.setScene(scene);
			makeDraggable(dialog);
			dialog.initStyle(StageStyle.UNDECORATED);
			dialog.show();

			controller.setDownloadNode(DownloadEngine.getInstance().addDownload(sr));
			//			CallbackRegister.getInstance().registerForCallback(
			//					"p2p-app-download-file-"+sr.getIp()+"-"+sr.getFileId(), "p2pApp.p2pUi.controller.FileDownloadController", "completeDownload", true, controller);
		}

		catch(Exception e){
			System.out.println("Unable to open the file download: "+e.getMessage());
		}
	}

	public void showDirDownloadDialog(SearchResults sr){
		try{

			Stage dialog = new Stage();
			dialog.setTitle("Downloading: "+sr.getFilename());
			dialog.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(IMG_PATH + "File.png")));

			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "download_dir.fxml"));
			Parent root = (Parent)loader.load();
			DirDownloadController controller = (DirDownloadController)loader.getController();
			controller.setupStage(dialog);
			Scene scene = new Scene(root);
			dialog.setScene(scene);
			makeDraggable(dialog);
			dialog.initStyle(StageStyle.UNDECORATED);
			dialog.show();

			controller.setDetails(sr.getFilename(), sr.getFileSize());

			GetDirQuery.sendDirQuery(sr);
			CallbackRegister.getInstance().registerForCallback(
					"p2p-app-dir-listfiles", "p2pApp.p2pUi.controller.DirDownloadController", "fileList", true, controller);
		}

		catch(Exception e){
			System.out.println("Unable to open the file download: "+e.getMessage());
		}
	}

	public void showCloseConfirm(){

		try {
			Stage dialog = new Stage();
			dialog.initOwner(anchorPane.getScene().getWindow());

			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "alert.fxml"));
			Parent root = (Parent)loader.load();
			AlertController controller = (AlertController)loader.getController();
			controller.setupDetails(dialog,
					"exit-confirm", UIController.this,
					"Exit Share It ?", "Are you sure to quit the application?", "Yes", "No");

			Scene scene = new Scene(root);
			dialog.setScene(scene);
			dialog.initStyle(StageStyle.UNDECORATED);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.show();
		} catch (IOException e) {
		}

	}

	public void showAlert(String title, String msg, String bt1, String bt2){

		try {
			Stage dialog = new Stage();
			dialog.initOwner(anchorPane.getScene().getWindow());

			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "alert.fxml"));
			Parent root = (Parent)loader.load();
			AlertController controller = (AlertController)loader.getController();
			controller.setupDetails(dialog,
					"alert", UIController.this,
					title, msg, bt1, bt2);

			Scene scene = new Scene(root);
			dialog.setScene(scene);
			dialog.initStyle(StageStyle.UNDECORATED);
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.show();
		} catch (IOException e) {
		}

	}

	public void showFileListDialog(int index){

		SearchResults sr= resultList.getItems().get(index);
		try {
			Stage dialog = new Stage();
			dialog.initOwner(anchorPane.getScene().getWindow());
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "alert.fxml"));
			Parent root = (Parent)loader.load();
			AlertController controller = (AlertController)loader.getController();
			controller.setupDetails(dialog,
					"file-list", UIController.this,
					"List of files in "+sr.getFilename()+".", "Fetching the files...", null, "OK");

			Scene scene = new Scene(root);
			dialog.setScene(scene);
			dialog.initStyle(StageStyle.UNDECORATED);
			makeDraggable(dialog);
			dialog.show();

			GetDirQuery.sendDirQuery(sr);
			CallbackRegister.getInstance().registerForCallback(
					"p2p-app-dir-listfiles-only", "p2pApp.p2pUi.controller.AlertController", "fileList", true, controller);
		} catch (IOException e) {
		}

	}

	public class Console extends OutputStream {
		private TextArea console;
		private String consoleText;

		public Console(TextArea console) {
			this.console = console;
			consoleText="";
		}

		public void appendText(final String valueOf) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					console.appendText(valueOf);
				}
			});
		}

		public void write(int b) throws IOException {
			consoleText= consoleText + (char)b;
			if(consoleText.contains("\n")){
				String text= consoleText;
				consoleText="";
				if(text.contains("NanoHTTPD") || text.contains("SocketOutputStream"))
					return;
				appendText(text);
			}
		}

		public TextArea getConsole(){
			return console;
		}
	}

	public static void makeDraggable(final Stage stage){
		final Node root = stage.getScene().getRoot();
		final Delta dragDelta = new Delta();
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				// record a delta distance for the drag and drop operation.
				dragDelta.x = stage.getX() - mouseEvent.getScreenX();
				dragDelta.y = stage.getY() - mouseEvent.getScreenY();
			}
		});
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override public void handle(MouseEvent mouseEvent) {
				stage.setX(mouseEvent.getScreenX() + dragDelta.x);
				stage.setY(mouseEvent.getScreenY() + dragDelta.y);
			}
		});
	}

	static class Delta {
		double x;
		double y;
	}

	public void exitApp(){
		try {
			BaseController.getInstance().stopServer();
		} catch (IOException e) {
			System.out.println("**Unable to stop the server.**");
		}
		catch(Exception e){

		}
		stage.hide();
		System.exit(0);
	}
	// Callback functions

	public void handleResults(String action, Object obj){
		if(action.equals("p2p-app-results")){
			addResults(SearchTable.getInstance().getSearchTable(), SearchTable.getInstance().getSize());
		}
	}

	public synchronized void modifyPeersList(String action, Object obj){
		if(action.equals("tcp-server-neighbours")){
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					peers.clear();
					peers.addAll(PeersTable.getInstance().getNeighbourIps());
				}
			});
		}
	}
	
	public void handleChatMessage(String action, Object obj){
		if(obj instanceof PingQuery){
			final PingQuery pq= (PingQuery) obj;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					String mes= "\n"+pq.result+" :: "+pq.getExtraData();
					chatArea.appendText(mes);
				}
			});
		}
	}
}