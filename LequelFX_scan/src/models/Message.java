package models;

import java.time.Instant;

import javafx.scene.control.Button;
import lequelFX_scan.Gui_scan_controller;

public class Message {
	
	private String fichiers;
	private Instant date;
	private static Gui_scan_controller controller;
	private static Button bouton;
	
	public String getFichiers() {
		return fichiers;
	}
	public void setFichiers(String fichiers) {
		this.fichiers = fichiers;
	}
	public Instant getDate() {
		return date;
	}
	public void setDate(Instant date) {
		this.date = date;
	}
	public static Gui_scan_controller getController() {
		return controller;
	}
	public static void setController(Gui_scan_controller controller) {
		Message.controller = controller;
	}
	public static Button getBouton() {
		return bouton;
	}
	public static void setBouton(Button bouton) {
		Message.bouton = bouton;
	}
    
}
