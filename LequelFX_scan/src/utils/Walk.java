package utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Date;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import lequelFX_scan.Gui_scan_controller;
import models.Element;

public class Walk {

	private static IntegerProperty nombre_de_fichiers_copies;     
	private static IntegerProperty nombre_de_dossiers_copies;
	private static IntegerProperty nombre_de_erreurs_copies; 
	private static DoubleProperty nombre_total_elements_copies; 
	private static DoubleProperty pourcentage_copie;
	
	private static String message; 
	
	public static void dossiers_copies_plus_1(){
		nombre_de_dossiers_copies.set(nombre_de_dossiers_copies.get() + 1);
	}
	public static void fichiers_copies_plus_1(){
		nombre_de_fichiers_copies.set(nombre_de_fichiers_copies.get() + 1);
	}
	public static void erreurs_copies_plus_1(){
		nombre_de_erreurs_copies.set(nombre_de_erreurs_copies.get() + 1);
	}
	
	public static void init(int nombre_total_elements_vus){
		
		message = "0.0 %";
		
		nombre_de_fichiers_copies = new SimpleIntegerProperty(1);
		nombre_de_dossiers_copies = new SimpleIntegerProperty(0);
		nombre_de_erreurs_copies = new SimpleIntegerProperty(0);
		nombre_total_elements_copies = new SimpleDoubleProperty(0);
		pourcentage_copie = new SimpleDoubleProperty(15);
	
		nombre_de_fichiers_copies.addListener(
		            (ObservableValue<? extends Number> ov, Number old_val, 
		            Number new_val) -> {
		            	nombre_total_elements_copies.set(Double.parseDouble(new_val.toString()) + nombre_de_dossiers_copies.get() + nombre_de_erreurs_copies.get());
		        });
		nombre_de_dossiers_copies.addListener(
	            (ObservableValue<? extends Number> ov, Number old_val, 
	            Number new_val) -> {
	            	nombre_total_elements_copies.set(Double.parseDouble(new_val.toString()) + nombre_de_fichiers_copies.get() + nombre_de_erreurs_copies.get());
	        });
		nombre_de_erreurs_copies.addListener(
	            (ObservableValue<? extends Number> ov, Number old_val, 
	            Number new_val) -> {
	            	nombre_total_elements_copies.set(Double.parseDouble(new_val.toString()) + nombre_de_dossiers_copies.get() + nombre_de_fichiers_copies.get());  
	        });
		nombre_total_elements_copies.addListener(
	            (ObservableValue<? extends Number> ov, Number old_val, 
	            Number new_val) -> {
	            	pourcentage_copie.set(Double.parseDouble(new_val.toString()) / nombre_total_elements_vus);  
	            	message = String.format("% 6d / % 6d / % 6d     % 3.2f",
																	          nombre_de_dossiers_copies.get(),
																	          nombre_de_fichiers_copies.get(),
																	          nombre_de_erreurs_copies.get(),
																	          pourcentage_copie.get() * 100 
	      		          ) + "%";
	        });
	}
  
	
	public static class FileSizeVisitor implements FileVisitor<Path> {

		/**
		 * This is triggered before visiting a directory.
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path path,
				BasicFileAttributes attrs) throws IOException {

			if (path.getFileName().toString().startsWith(".")
			 || path.getFileName().toString().startsWith("@")
			 || path.getFileName().toString().startsWith("$")){
				return FileVisitResult.SKIP_SUBTREE;
			}
			else{
			    Gui_scan_controller.dossiers_vus_plus_1();
			}
			return FileVisitResult.CONTINUE;
		}

		/**
		 * This is triggered when we visit a file.
		 */
		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
				throws IOException {
	
			if (path.getFileName().toString().startsWith(".")
			 || path.getFileName().toString().startsWith("@")
			 || path.getFileName().toString().startsWith("$")){

			}
			else {
				Gui_scan_controller.fichiers_vus_plus_1();
				try {
					BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
					 if (attr.lastModifiedTime().toInstant().compareTo(Gui_scan_controller.getDate_derniere_modification_vue()) > 0){
						 Gui_scan_controller.setDate_derniere_modification_vue(attr.lastModifiedTime().toInstant());
					 }
				}
				catch (java.nio.file.NoSuchFileException nsfe){
					System.out.println("erreur broken link :\n" + path);
				}
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
			Gui_scan_controller.erreurs_vues_plus_1();
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
	
    public static class FileScanVisitor implements FileVisitor<Path> {

		/**
		 * This is triggered before visiting a directory.
		 */
		@Override
		public FileVisitResult preVisitDirectory(Path path,
				BasicFileAttributes attrs) throws IOException {

			if (path.getFileName().toString().startsWith(".")
		     ||	path.getFileName().toString().startsWith("$")
		     || path.getFileName().toString().startsWith("@")){
				return FileVisitResult.SKIP_SUBTREE;
			}
			else{
				
				try{
				
                    BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				
				
					Element element = new Element();
					element.setFichier(false);
					element.setNom(path.getFileName().toString().split("\\.")[0]);
					element.setScan(Date.from(Instant.now()));
					element.setDate(Date.from(attr.lastModifiedTime().toInstant()));
					element.setTaille(attr.size());
					element.setChemin("/" + Gui_scan_controller.getChemin_du_disque().relativize(path).toString());
					
					if (! path.getFileName().equals(Gui_scan_controller.getNom_du_disque())
					 && ! path.getParent().getFileName().equals(Gui_scan_controller.getNom_du_disque())		
							){
					
						element.setId_pere(MongoConn.getCollBase().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : #}",
								                                                         "chemin",
								                                                         "/" + Gui_scan_controller.getChemin_du_disque().relativize(path.getParent()).toString(),
								                                                         "scanned._id"),
								                                                         Gui_scan_controller.getScanId())
								                                  .as(Element.class).get_id().toString());
					}
					//element.setScanned_id(Gui_scan_controller.getScanId());
					element.setScanned(Gui_scan_controller.getScan());
					
					MongoConn.collBase.save(element);
					
					dossiers_copies_plus_1();
				}
				catch (java.nio.file.NoSuchFileException nsfe){
					System.out.println("erreur broken link :\n" + path);
				}
			}
			return FileVisitResult.CONTINUE;
		}

		/**
		 * This is triggered when we visit a file.
		 */
		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
				throws IOException {
	
			if (path.getFileName().toString().startsWith(".")
			 || path.getFileName().toString().startsWith("@")
			 || path.getFileName().toString().startsWith("$")){

			}
			else {
				
				try {
					BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
					
					Element element = new Element();
					element.setFichier(true);
					element.setExtension(path.getFileName().toString().split("\\.").length > 1 ? path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length -1] : "");
					element.setNom(path.getFileName().toString().split("\\.")[0]);
					element.setScan(Date.from(Instant.now()));
					element.setDate(Date.from(attr.lastModifiedTime().toInstant()));
					element.setTaille(attr.size());
					element.setChemin("/" + Gui_scan_controller.getChemin_du_disque().relativize(path).toString());
					element.setId_pere(MongoConn.getCollBase().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : #}",
							                                                         "chemin",
							                                                         "/" + Gui_scan_controller.getChemin_du_disque().relativize(path.getParent()).toString(),
							                                                         "scanned._id"),
							                                                         Gui_scan_controller.getScanId())
							                                  .as(Element.class).get_id().toString());
					//element.setScanned_id(Gui_scan_controller.getScanId());
					element.setScanned(Gui_scan_controller.getScan());
					
					MongoConn.collBase.save(element);
					
					fichiers_copies_plus_1();
				}
				catch (java.nio.file.NoSuchFileException nsfe){
					System.out.println("erreur broken link :\n" + path);
				}

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
			//Gui_scan_controller.elements_erreur_plus_1();
			System.err.println("ERROR: Cannot visit path: " + path);
			erreurs_copies_plus_1();
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

	public static String getMessage() {
		return message;
	}


	public static void setMessage(String message) {
		Walk.message = message;
	}


	public static double getPourcentage_copie() {
		return pourcentage_copie.get();
	}


	public static void setPourcentage_copie(double pourcentage_copie) {
		Walk.pourcentage_copie.set(pourcentage_copie);
	}
	
	public static DoubleProperty pourcentageProperty(){
		return pourcentage_copie;
	}
    
    

}
