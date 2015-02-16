package lequelFX_scan;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class Gui_scan_controller implements Initializable {
	
	@FXML
	private ChoiceBox<String> liste_disques;
	
	@FXML
	private Button bouton_scanner;
	
	@FXML
	private Label label_progress_;
	
	@FXML
	private ProgressBar progress_;
	
	
	private ObservableList<String> collec_disques = FXCollections.observableArrayList();
	
	private Path homeFolder;
	
    private static int nombre_f = 0; 
    private static int nombre_d = 0; 
    private static int nombre_f_e = 0; 

	
	private void refreshList(){
		
		collec_disques.clear();
		
		collec_disques.add("choisir un disque");
		collec_disques.addAll(new File("/").list());
		
		liste_disques.getSelectionModel().select(0);
	}
		
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		
		liste_disques.setItems(collec_disques);
		refreshList();
		
		bouton_scanner.setOnAction(new EventHandler<ActionEvent>() {
			
			
        
			
			@Override
			public void handle(ActionEvent event) {
				
				nombre_f = 0;     
		        nombre_d = 0;
		        nombre_f_e = 0;     
				
				homeFolder = Paths.get("/" + liste_disques.getSelectionModel().getSelectedItem());
		
				FileVisitor<Path> fileVisitor = new FileSizeVisitor();
				try {
					Files.walkFileTree(homeFolder, fileVisitor);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println((nombre_f + nombre_d ) + " total");

				
			}
		});
		
	}
	
	static class FileSizeVisitor implements FileVisitor<Path> {

		/**
		 * This is triggered before visiting a directory.
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path path,
				BasicFileAttributes attrs) throws IOException {

			if (path.getFileName().toString().startsWith(".")){
				return FileVisitResult.SKIP_SUBTREE;
			}
			else{
			    nombre_d ++;
			}
			return FileVisitResult.CONTINUE;
		}

		/**
		 * This is triggered when we visit a file.
		 */
		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
				throws IOException {
	
			if (path.getFileName().toString().startsWith(".")){

			}
			else {
                nombre_f ++;
			}
			return FileVisitResult.CONTINUE;
		}

		/**
		 * This is triggered if we cannot visit a Path We log the fact we cannot
		 * visit a specified path .
		 */
		@Override
		public FileVisitResult visitFileFailed(Path path, IOException exc)
				throws IOException {
			// We print the error
			nombre_f_e ++;
			System.err.println("ERROR: Cannot visit path: " + path);
			return FileVisitResult.CONTINUE;
		}

		/**
		 * This is triggered after we finish visiting a specified folder.
		 */
		@Override
		public FileVisitResult postVisitDirectory(Path path, IOException exc)
				throws IOException {
			return FileVisitResult.CONTINUE;
		}

		

	}

	

}
