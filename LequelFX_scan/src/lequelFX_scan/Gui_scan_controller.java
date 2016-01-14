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
import java.util.Comparator;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.jongo.MongoCursor;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import models.ComboBoxes;
import models.Message;
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
	private ComboBox<String> tag_combobox;
	@FXML
	private ComboBox<String> taille_disque_combobox;
	@FXML
	private ComboBox<String> taille_restante_combobox;
		
	@FXML
	private ProgressBar progress_progressBar;
	
	private ObservableList<String> collec_tags;
	private ObservableList<String> collec_tailles_disques;
	private ObservableList<String> collec_tailles_restantes;
	
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
	
	private ComboBoxes boxes;
	
	private int index;
	
	private static Scan scan;
	private static ObjectId scanId;
	
	private FileVisitor<Path> fileVisitor ;
	private FileVisitor<Path> dateVisitor ;
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
		else if(new File("/run/media/autor").isDirectory() && new File("/run/media/autor").list().length > 0) {
			collec_disques.addAll(new File("/run/media/autor").list());
			chemin_du_disque = Paths.get("/run/media/autor");
		}
		else{
			collec_disques.addAll(new File("/mnt").list());
			chemin_du_disque = Paths.get("/mnt");
		}
		
		
		liste_disques_choiceBox.getSelectionModel().select(0);
	}

	@FXML
	public void on_rafraichir_button(){
		refreshList();
		
		
	}
	@FXML
	public void on_scanner_button(){
		
		nombre_total_elements_vus = Walk.nombre_total_elementsProperty().get(); 
		Walk.init(nombre_total_elements_vus);

		Scan precedent = MongoConn.getCollScans().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : 0}", "disque", nom_du_disque.toString(), "rang")).as(Scan.class);
		
		scanner_button.setVisible(false);

		scan = new Scan();
		scan.setDate(Date.from(Instant.now()));
		scan.setDisque(nom_du_disque.toString());
		
		if (precedent != null){
			scan.setNext(precedent.getNext() + 1);
			precedent.setRang(precedent.getNext());
			
			System.out.println(precedent.get_id());
			
			MongoConn.getCollBase().update("{'scanned._id' : #}", precedent.get_id()).multi().with("{'$set' : {'scanned.rang' : #}}",precedent.getNext());
			MongoConn.getCollScans().update("{_id : #}", precedent.get_id()).with(precedent);
			
		}
		else {
			scan.setNext(1);	
			scan.setRang(0);
		}
		
		String tag_ = tag_combobox.getSelectionModel().getSelectedItem();
		String taille_disque_ = taille_disque_combobox.getSelectionModel().getSelectedItem();
		String taille_restante_ = taille_restante_combobox.getSelectionModel().getSelectedItem();
		
		System.out.println("tag_ : " + tag_);
		System.out.println("taille_disque_ : " + taille_disque_);
		System.out.println("taille_restante_ : " + taille_restante_);
		
		
		scan.setTag(tag_);
		scan.setTaille_disque(taille_disque_);
		scan.setTaille_restante(taille_restante_);
		
		MongoConn.getCollScans().save(scan);

  	    boxes.addTag(tag_combobox.getValue());
  	    boxes.addTaille_disque(taille_disque_combobox.getValue());
  	    boxes.addTaille_restante(taille_restante_combobox.getValue());
  	    System.out.println("box : " + boxes.getTaille_restantes());
  	    
        MongoConn.getCollBoxes().save(boxes);
        
        collec_tags.clear();
        collec_tailles_disques.clear();
        collec_tailles_restantes.clear();
        
        collec_tags.addAll(boxes.getTags()
                   .stream()
                   .filter(a -> a != null)
                   .sorted()
                   .collect(Collectors.toList()));

        collec_tailles_disques.addAll(boxes.getTaille_disques()
                              .stream()
                              .filter(a -> a != null)
                              .sorted()
                              .collect(Collectors.toList()));

        collec_tailles_restantes.addAll(boxes.getTaille_restantes()
                                .stream()
                                .filter(a -> a != null)
                                .sorted()
                                .collect(Collectors.toList()));
 
tag_combobox.setItems(collec_tags);
taille_disque_combobox.setItems(collec_tailles_disques);
taille_restante_combobox.setItems(collec_tailles_restantes);
		
		scanId = scan.get_id();
	
		scanWorker = createScanWorker();
		

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
        		
		progress_label.textProperty().unbind();
        progress_label.setText(" 0.0%");
        
        progress_progressBar.progressProperty().unbind();
        progress_progressBar.setProgress(0.0);
        
        date_derniere_modification_vue = Instant.EPOCH;
        Scan scan = MongoConn.getCollScans().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : 0}", "disque", nom_du_disque.toString(), "rang")).as(Scan.class);
        date_dernier_scan = scan != null ? scan.getDate() : null;

		date_dernier_scan_label.setText(date_dernier_scan != null ? date_dernier_scan.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() : "Jamais");
		
		nb_scans_en_base_label.setText(scan != null ? scan.getNext() + " scans dans la base." : "0");
		
		if (scan != null){
			tag_combobox.getSelectionModel().select(scan.getTag());
			taille_disque_combobox.getSelectionModel().select(scan.getTaille_disque());
			taille_restante_combobox.getSelectionModel().select(scan.getTaille_restante());
		}
		
        
        readWorker = createCheckWorker();
        
        nombre_elements_label.textProperty().unbind();
        nombre_elements_label.textProperty().bind(readWorker.messageProperty());
        
        date_derniere_modif_label.textProperty().unbind();
        date_derniere_modif_label.textProperty().bind(readWorker.titleProperty());
		
		// parcours pour le nombre de fichiers /dossiers /erreurs
        new Thread(readWorker).start();
         
        
        // parcours pour la date du dernier scan
//		ExecutorService executor = Executors.newFixedThreadPool(1);
//        Future<Instant> future_date_derniere_modification_vue = executor.submit(readTask);
//        
//        try {
//          // appel à .get() bloquant   (!!!)     
//			date_derniere_modif_label.setText(future_date_derniere_modification_vue.get().atZone(ZoneId.systemDefault()).toLocalDate().toString());
//			
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ExecutionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
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
	
	Callable<Instant> readTask = () -> {
		Walk.init(this, chemin_du_disque.resolve(nom_du_disque));

    	try {
    		
			Files.walkFileTree(chemin_du_disque.resolve(nom_du_disque), dateVisitor);
		} catch (IOException e) {
			e.printStackTrace();
		}	
    	return date_derniere_modification_vue;
		
	};
	
	public Task<?> createCheckWorker() {

        return new Task<Object>() {
            @Override
            protected Object call() throws Exception {
            	
            	Walk.init(Message.getController(), chemin_du_disque.resolve(nom_du_disque));
            	
            	Walk.nombre_total_elementsProperty().addListener(
    		            (ObservableValue<? extends Number> ov, Number old_val, 
    				            Number new_val) -> {
    				            	updateMessage(Walk.getMessageCheck());
    				            }
    			);
            	
            	Walk.date_derniere_modification_vueProperty().addListener(
    		            (ObservableValue<? extends Number> ov, Number old_val, 
    				            Number new_val) -> {
    				            	updateTitle(Walk.date_derniere_modification_vue_string());
    				            }
    			);
            	
            	updateValue(Walk.getMessageCheck());
            	try {
            		
        			Files.walkFileTree(chemin_du_disque.resolve(nom_du_disque), fileVisitor);
        		} catch (IOException e) {
        			e.printStackTrace();
        		}	
            	return true;
            }
        };
	 }
	
	 public Task<?> createScanWorker() {
        return new Task<Object>() {
            @Override
            protected Object call() throws Exception {
            	
            	Walk.init(nombre_total_elements_vus);
            	
            	Walk.pourcentageProperty().addListener(
    		            (ObservableValue<? extends Number> ov, Number old_val, 
    				            Number new_val) -> {
    				            	updateMessage(Walk.getMessage());
    				    			updateProgress(Double.parseDouble(new_val + "") * 100 , 100);
    				    			
    				    			if(Double.parseDouble(new_val + "") == 1){
    				    				
    				    				System.out.println("complet");
    				    				
    				    				Scan scan = MongoConn.getCollScans().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : 0}", "disque", nom_du_disque.toString(), "rang")).as(Scan.class);
    				    		        date_dernier_scan = scan != null ? scan.getDate() : null;
    				    				date_dernier_scan_label.setText(date_dernier_scan != null ? date_dernier_scan.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString() : "Jamais");
    				    				
    				    				// pas thread-safe : Exception in thread "Thread-5" java.lang.IllegalStateException: Not on FX application thread; currentThread = Thread-5
    				    				//nb_scans_en_base_label.setText(scan.getNext() + "");
    				    			}
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
	
//		Platform.setImplicitExit(false);
		
		collec_tags = FXCollections.observableArrayList();
		collec_tailles_disques = FXCollections.observableArrayList();
		collec_tailles_restantes = FXCollections.observableArrayList();

		Message.setController(this);
		
		fileVisitor = new Walk.FileSizeVisitor();
		scanVisitor = new Walk.FileScanVisitor();
		dateVisitor = new Walk.FileDateVisitor();
		
		scanner_button.setVisible(false);
		Message.setBouton(scanner_button);
		
		collec_disques = FXCollections.observableArrayList();
		
		info_progress = new SimpleStringProperty();

		liste_disques_choiceBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

		      @Override
		      public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
		    	  
		    	scanner_button.setVisible(false);
		    	
		        if ((int) number2 != 0){
		        	
		        	nom_du_disque = Paths.get(collec_disques.get((int) number2)); 
					afficher_infos_disque();
				}
		      }
		 });
		
		MongoConn .connecter("Lequel_V04", "Lequel_V04_scans", "boxes_V04");
		
		boxes = MongoConn.getCollBoxes().findOne().as(ComboBoxes.class);
		
		if (boxes == null){
			boxes = new ComboBoxes();
		}
		
		collec_tags.addAll(boxes.getTags()
				                .stream()
				                .filter(a -> a != null)
				                .sorted()
				                .collect(Collectors.toList()));
		
		collec_tailles_disques.addAll(boxes.getTaille_disques()
                                           .stream()
                                           .filter(a -> a != null)
                                           .sorted()
                                           .collect(Collectors.toList()));
		
		collec_tailles_restantes.addAll(boxes.getTaille_restantes()
                                             .stream()
                                             .filter(a -> a != null)
                                             .sorted()
                                             .collect(Collectors.toList()));
		
		tag_combobox.setItems(collec_tags);
		taille_disque_combobox.setItems(collec_tailles_disques);
		taille_restante_combobox.setItems(collec_tailles_restantes);

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

	public static Path getChemin_du_disque() {
		return chemin_du_disque;
	}

	public static void setChemin_du_disque(Path chemin_du_disque) {
		Gui_scan_controller.chemin_du_disque = chemin_du_disque;
	}

	public static Scan getScan() {
		return scan;
	}

	public static void setScan(Scan scan) {
		Gui_scan_controller.scan = scan;
	}

	public Button getScanner_button() {
		return scanner_button;
	}

	public void setScanner_button(Button scanner_button) {
		this.scanner_button = scanner_button;
	}

}
