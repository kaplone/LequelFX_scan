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

import lequelFX_scan.Gui_scan_controller;
import models.Element;

public class Walk {

//	public static void main_(Path homeFolder) {
//		//Path  = Paths.get("/home/autor/Desktop/tests_select/soletanche");
//		FileVisitor<Path> fileVisitor = new FileSizeVisitor();
//		try {
//			Files.walkFileTree(homeFolder, fileVisitor);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
  
	
	public static class FileSizeVisitor implements FileVisitor<Path> {
		
		

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
	
			if (path.getFileName().toString().startsWith(".")){

			}
			else {
				Gui_scan_controller.fichiers_vus_plus_1();
				BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				 if (attr.lastModifiedTime().toInstant().compareTo(Gui_scan_controller.getDate_derniere_modification_vue()) > 0){
					 Gui_scan_controller.setDate_derniere_modification_vue(attr.lastModifiedTime().toInstant());
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
			Gui_scan_controller.elements_erreur_plus_1();
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
		     ||	path.getFileName().toString().startsWith("$")){
				return FileVisitResult.SKIP_SUBTREE;
			}
			else{
			    //Gui_scan_controller.dossiers_vus_plus_1();
				
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				
				Element element = new Element();
				element.setFichier(false);
				//element.setExtension(path.getFileName().toString().split("\\.").length > 1 ? path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length -1] : "");
				element.setNom(path.getFileName().toString().split("\\.")[0]);
				element.setScan(Date.from(Instant.now()));
				element.setDate(Date.from(attr.lastModifiedTime().toInstant()));
				element.setTaille(attr.size());
				element.setChemin(path.toString());
				
				System.out.println("path : " + path);
				System.out.println("path.getFileName() : " + path.getFileName());
				System.out.println("path.getParent() : " + path.getParent());
				
				if (! path.getFileName().equals(Gui_scan_controller.getNom_du_disque())
				 && ! path.getParent().getFileName().equals(Gui_scan_controller.getNom_du_disque())		
						){
					
					System.out.println(String.format("{\"%s\" : \"%s\", \"%s\" : \"%s\"}",
							                                                         "chemin",
							                                                         path.getParent().toString(),
							                                                         "scanned",
							                                                         Gui_scan_controller.getScanId()));
				
					element.setId_pere(MongoConn.getCollBase().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : #}",
							                                                         "chemin",
							                                                         path.getParent().toString(),
							                                                         "scanned"),
							                                                         Gui_scan_controller.getScanId())
							                                  .as(Element.class).get_id().toString());
				}
				element.setScanned(Gui_scan_controller.getScanId());
				
				MongoConn.collBase.save(element);
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
			||	path.getFileName().toString().startsWith("$")){

			}
			else {
//				Gui_scan_controller.fichiers_vus_plus_1();
				
				BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
				
				Element element = new Element();
				element.setFichier(true);
				element.setExtension(path.getFileName().toString().split("\\.").length > 1 ? path.getFileName().toString().split("\\.")[path.getFileName().toString().split("\\.").length -1] : "");
				element.setNom(path.getFileName().toString().split("\\.")[0]);
				element.setScan(Date.from(Instant.now()));
				element.setDate(Date.from(attr.lastModifiedTime().toInstant()));
				element.setTaille(attr.size());
				element.setChemin(path.toString());
				element.setId_pere(MongoConn.getCollBase().findOne(String.format("{\"%s\" : \"%s\", \"%s\" : #}",
						                                                         "chemin",
						                                                         path.getParent().toString(),
						                                                         "scanned"),
						                                                         Gui_scan_controller.getScanId())
						                                  .as(Element.class).get_id().toString());
				element.setScanned(Gui_scan_controller.getScanId());
				
				MongoConn.collBase.save(element);
				
				

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
