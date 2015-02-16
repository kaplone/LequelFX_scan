package lequelFX_scan;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
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
    private static int nombre_f_p = 0; 
    private static int nombre_d_p = 0; 
	
	private void refreshList(){
		
		collec_disques.clear();
		
		collec_disques.add("choisir un disque");
		collec_disques.addAll(new File("/").list());
		
		liste_disques.getSelectionModel().select(0);
	}
	
	
	private void recurse(Path p_){
		try {
			DirectoryStream<Path> directory = Files.newDirectoryStream(p_);
			for (Path pp : directory){
				//nombre_f++;
				if (pp.toFile().isDirectory()){
					recurse(pp);
				}
			}
		}
		catch (IOException a) {
			System.out.println(p_ + " est inaccessible");
			System.out.println("R : " + p_.toFile().canRead() + ", W : " + p_.toFile().canWrite() + ", X : " + p_.toFile().canExecute());
			System.out.println("Is directory : "+Files.isDirectory(FileSystems.getDefault().getPath(p_.toFile().getAbsolutePath())));
			System.out.println("Is readable : "+Files.isReadable(FileSystems.getDefault().getPath(p_.toFile().getAbsolutePath())));
			System.out.println("Is executable : "+Files.isExecutable(FileSystems.getDefault().getPath(p_.toFile().getAbsolutePath())));
			System.out.println(p_.toFile().listFiles());
			//a.printStackTrace();
		} 
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
		        nombre_f_p = 0;
		        nombre_d_p = 0;
				
				homeFolder = Paths.get("/" + liste_disques.getSelectionModel().getSelectedItem());
				
				//recurse(homeFolder);
		
				FileVisitor<Path> fileVisitor = new FileSizeVisitor();
				try {
					Files.walkFileTree(homeFolder, fileVisitor);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println(nombre_f + " fichiers");
				System.out.println(nombre_d + " dossiers");
				System.out.println(nombre_f_e + " fichiers erreurs");
				System.out.println(nombre_f_p + " fichiers cachés");
				System.out.println(nombre_d_p + " dossiers cachés");
				
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
			System.out.println("vpdp " + path);
            
			if (path.getFileName().startsWith(".")){
				
				System.out.println("vpdp+ " + path.getFileName());
				nombre_d_p ++;
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
			System.out.println("vfp " + path);
			System.out.println("vfp? " + path.getFileName());
			
			if (path.getFileName().startsWith(".")){
				System.out.println("vfp+ " + path.getFileName());
				nombre_f_p ++;
			}
			else {
                nombre_f ++;
			}
            //System.out.println(path  + " ----> " + attrs.size()  + " ----> " + attrs.creationTime()  + " ----> " + attrs.isSymbolicLink());
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
			nombre_f_e++;
			System.err.println("ERROR: Cannot visit path: " + path);
			// We continue the folder walk
			return FileVisitResult.CONTINUE;
		}

		/**
		 * This is triggered after we finish visiting a specified folder.
		 */
		@Override
		public FileVisitResult postVisitDirectory(Path path, IOException exc)
				throws IOException {
			// We continue the folder walk
			//System.out.println("\n");
			return FileVisitResult.CONTINUE;
		}
		
		String base = "/run/media/autor/QUAI_H/LAFARGE/2014-12-10/DCIM_copy";
		

	}

	

}
