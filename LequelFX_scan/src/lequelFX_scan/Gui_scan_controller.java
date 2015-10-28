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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

import org.bson.types.ObjectId;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import models.Scan;
import utils.MongoConn;
import utils.Walk;

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
	private Label nb_scans_en_base_label;
	@FXML
	private Label trace_label;
	
	
	
	@FXML
	private ProgressBar progress_progressBar;
	
	
	private ObservableList<String> collec_disques;
	
	private static int nombre_de_fichiers_vus;     
	private static int nombre_de_dossiers_vus;
	private static int nombre_de_erreurs_vues;
	private static int nombre_total_elements_vus;
	private static Date date_dernier_scan;
	private static Instant date_derniere_modification_vue;
	private static Path chemin_du_disque;
	private static Path nom_du_disque;

	private static StringProperty info_progress;
	
	
	private int index;
	
	private static Scan scan;
	private static ObjectId scanId;
	
	private FileVisitor<Path> fileVisitor ;
	private FileVisitor<Path> scanVisitor ;
	
	
	private Task<?> readWorker;
	private Task<?> scanWorker;
		
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

		Scan precedent = MongoConn.getCollScans().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : 0}", "disque", nom_du_disque.toString(), "rang")).as(Scan.class);

		scan = new Scan();
		scan.setDate(Date.from(Instant.now()));
		scan.setDisque(nom_du_disque.toString());
		
		if (precedent != null){
			scan.setNext(precedent.getNext() + 1);
			precedent.setRang(precedent.getNext());
			MongoConn.getCollScans().update("{_id : #}", precedent.get_id()).with(precedent);
		}
		else {
			scan.setNext(1);	
		}
		scan.setRang(0);
		
		MongoConn.getCollScans().save(scan);
		
		scanId = scan.get_id();
		
		scanWorker = createWorker();
		

		progress_label.textProperty().unbind();
        progress_label.textProperty().bind(scanWorker.messageProperty());
        
        progress_progressBar.progressProperty().unbind();
        progress_progressBar.progressProperty().bind(scanWorker.progressProperty());
        
		new Thread(scanWorker).start();
		
		
	}
	
	public void afficher_infos_disque(){
		
		nombre_de_fichiers_vus = 0;     
        nombre_de_dossiers_vus = 0;
        nombre_total_elements_vus = 0; 
        nombre_de_erreurs_vues = 0;
        
        date_derniere_modification_vue = Instant.EPOCH;
        Scan scan = MongoConn.getCollScans().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : 0}", "disque", nom_du_disque.toString(), "rang")).as(Scan.class);
        date_dernier_scan = scan != null ? scan.getDate() : null;

		
		try {
			Files.walkFileTree(chemin_du_disque.resolve(nom_du_disque), fileVisitor);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		date_dernier_scan_label.setText(date_dernier_scan != null ? date_dernier_scan.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() : "Jamais");
		date_derniere_modif_label.setText(date_derniere_modification_vue.atZone(ZoneId.systemDefault()).toLocalDate().toString());
		nombre_elements_label.setText(String.format("%s / %s / %s", nombre_de_dossiers_vus, nombre_de_fichiers_vus, nombre_de_erreurs_vues));
		nb_scans_en_base_label.setText(scan != null ? scan.getNext() + " scans dans la base." : "0");
		
		nombre_total_elements_vus = nombre_de_fichiers_vus + nombre_de_dossiers_vus + nombre_de_erreurs_vues; 
		
		progress_label.textProperty().unbind();
        progress_label.setText(" 0.0%");
        
        progress_progressBar.progressProperty().unbind();
        progress_progressBar.setProgress(0.0);
		
	}
	
	public static void dossiers_vus_plus_1(){
		nombre_de_dossiers_vus ++;
	}
	public static void fichiers_vus_plus_1(){
		nombre_de_fichiers_vus ++;
	}
	public static void erreurs_vues_plus_1(){
		nombre_de_erreurs_vues ++;
	}

	
	
	public void maj_progres(){

	}
	
	 public Task<?> createWorker() {
        return new Task<Object>() {
            @Override
            protected Object call() throws Exception {
            	
            	Walk.init(nombre_total_elements_vus);
            	
            	Walk.pourcentageProperty().addListener(
    		            (ObservableValue<? extends Number> ov, Number old_val, 
    				            Number new_val) -> {
    				            	updateMessage(Walk.getMessage());
    				    			updateProgress(Double.parseDouble(new_val + "") * 100 , 100);
    				            }
    			);
            	
            	updateMessage(Walk.getMessage());
    			updateProgress(Walk.getPourcentage_copie() * 100 , 100);
                
                try {

        			Files.walkFileTree(chemin_du_disque.resolve(nom_du_disque), scanVisitor);
		
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
                return true;
            }
        };
	 }
		
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		
		fileVisitor = new Walk.FileSizeVisitor();
		scanVisitor = new Walk.FileScanVisitor();
		
		collec_disques = FXCollections.observableArrayList();
		
		info_progress = new SimpleStringProperty();

		liste_disques_choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

		      @Override
		      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
  
		        if ((int) number2 != 0){
		        	nom_du_disque = Paths.get(collec_disques.get((int) number2)); 
					afficher_infos_disque();
				}
		      }
		 });
		
		MongoConn .connecter("Lequel_new_test", "Lequel_scans_new_test");
		
		liste_disques_choiceBox.setItems(collec_disques);
		refreshList();
	
	}
	 
	public static Instant getDate_derniere_modification_vue() {
		return date_derniere_modification_vue;
	}

	public static void setDate_derniere_modification_vue(Instant date_derniere_modification_vue) {
		Gui_scan_controller.date_derniere_modification_vue = date_derniere_modification_vue;
	}

	public static ObjectId getScanId() {
		return scanId;
	}

	public static void setScanId(ObjectId scanId) {
		Gui_scan_controller.scanId = scanId;
	}

	public static Path getNom_du_disque() {
		return nom_du_disque;
	}

	public static void setNom_du_disque(Path nom_du_disque) {
		Gui_scan_controller.nom_du_disque = nom_du_disque;
	}

	public static StringProperty getInfo_progress() {
		return info_progress;
	}

	public static void setInfo_progress(StringProperty info_progress) {
		Gui_scan_controller.info_progress = info_progress;
	}
	
	
	

}
