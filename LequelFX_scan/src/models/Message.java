package models;

import java.time.Instant;

public class Message {
	
	private String fichiers;
	private Instant date;
	
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
	
	

}
