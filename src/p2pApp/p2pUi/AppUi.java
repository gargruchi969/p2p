package p2pApp.p2pUi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import p2pApp.p2pUi.controller.UIController;

public class AppUi extends Application{

	public static void main(String []args){
		launch(args);	
	}

	private static String APP_NAME = "P2P App";

	public static String getAppName() {
		return APP_NAME;
	}

	public static String FXML_PATH = "resources/fxml/";
	public static String IMG_PATH = "resources/img/";

	@Override
	public void start(Stage primaryStage) {

		try{
			FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(FXML_PATH + "main.fxml"));
			Parent root = loader.load();
			UIController controller = loader.getController();
			controller.setupStage(primaryStage);
			Scene scene = new Scene(root);
			primaryStage.setTitle(getAppName());
			primaryStage.setScene(scene);
			primaryStage.getIcons().add(new Image(getClass().getClassLoader()
					.getResourceAsStream(IMG_PATH + "share.png")));
			primaryStage.initStyle(StageStyle.UNDECORATED);
			UIController.makeDraggable(primaryStage);
			primaryStage.show();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
		
	}	
}
