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
import java.time.Instant;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	private ChoiceBox<String> liste_disques_choiceBox;
	
	@FXML
	private Button scanner_button;
	@FXML
	private Button rafraichir_button;
	
	@FXML
	private Label progress_label;
	@FXML
	private Label date_dernier_scan_label;
	@FXML
	private Label date_derniere_modif_label;
	@FXML
	private Label nombre_elements_label;
	
	
	
	@FXML
	private ProgressBar progress_progressBar;
	
	
	private ObservableList<String> collec_disques;
	
	private static int nombre_de_fichiers_vus;     
	private static int nombre_de_dossiers_vus;
	private static int nombre_elements_vus;
	private static int nombre_de_elements_erreur;
	private static Date date_dernier_scan;
	private static Instant date_derniere_modification_vue;
	private static Path chemin_du_disque;
	private static Path nom_du_disque;
	
	private static int nombre_de_fichiers_copies;     
	private static int nombre_de_dossiers_copies;
	private static int nombre_elements_copies; 
	
	private int index;
	
	

	
	private void refreshList(){
		
		collec_disques.clear();
		
		collec_disques.add("choisir un disque");
		
		if (new File("/Volumes").isDirectory()){
			collec_disques.addAll(new File("/Volumes").list());
			chemin_du_disque = Paths.get("/Volumes");
		}
		else {
			collec_disques.addAll(new File("/run/media/autor").list());
			chemin_du_disque = Paths.get("/run/media/autor");
		}
		
		
		liste_disques_choiceBox.getSelectionModel().select(0);
	}

	@FXML
	public void on_rafraichir_button(){
		refreshList();
		
		
	}
	@FXML
	public void on_scanner_button(){
		
	}
	
	public void afficher_infos_disque(){
		
		nombre_de_fichiers_vus = 0;     
        nombre_de_dossiers_vus = 0;
        nombre_elements_vus = 0; 
        nombre_de_elements_erreur = 0;
        
        date_derniere_modification_vue = Instant.EPOCH;

		FileVisitor<Path> fileVisitor = new Walk.FileSizeVisitor();
		try {
			Files.walkFileTree(chemin_du_disque.resolve(nom_du_disque), fileVisitor);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//date_dernier_scan_label.setText(date_dernier_scan.toInstant().toString());
		date_derniere_modif_label.setText(date_derniere_modification_vue.toString());
		nombre_elements_label.setText(String.format("%s / %s", nombre_de_dossiers_vus, nombre_de_fichiers_vus));

	}
	
	public static void dossiers_vus_plus_1(){
		nombre_de_dossiers_vus ++;
	}
	public static void fichiers_vus_plus_1(){
		nombre_de_fichiers_vus ++;
	}
	public static void elements_erreur_plus_1(){
		nombre_de_elements_erreur ++;
	}
		
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		
		collec_disques = FXCollections.observableArrayList();
		
		liste_disques_choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

		      @Override
		      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
  
		        if ((int) number2 != 0){
		        	nom_du_disque = Paths.get(collec_disques.get((int) number2)); 
					afficher_infos_disque();
				}
		      }
		    });
		
		liste_disques_choiceBox.setItems(collec_disques);
		refreshList();
	
	}

	public static Instant getDate_derniere_modification_vue() {
		return date_derniere_modification_vue;
	}

	public static void setDate_derniere_modification_vue(Instant date_derniere_modification_vue) {
		Gui_scan_controller.date_derniere_modification_vue = date_derniere_modification_vue;
	}

}
