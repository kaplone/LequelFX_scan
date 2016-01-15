package lequelFX_scan;
	
import javafx.application.Application;
import javafx.stage.Stage;
import utils.JfxUtils;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Scene scene = new Scene((Parent) JfxUtils.loadFxml("GUI_scan.fxml"), 1000, 600);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			
			primaryStage.getIcons().add(new Image(getClass().getResource("LequelFX_scan_01.png").toExternalForm()));
			primaryStage.setTitle("LequelFX_scan");
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
