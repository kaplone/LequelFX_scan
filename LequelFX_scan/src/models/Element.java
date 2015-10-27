package models;

import java.util.Date;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Element extends Commun{
	
	
	private boolean fichier;
	private String extension;
	private String nom;
	private Date scan;
	private Date date;
	private long taille;
	private String chemin;
	private String id_pere;
	private ObjectId scanned;
	public boolean isFichier() {
		return fichier;
	}
	public void setFichier(boolean fichier) {
		this.fichier = fichier;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public String getNom() {
		return nom;
	}
	public void setNom(String nom) {
		this.nom = nom;
	}
	public Date getScan() {
		return scan;
	}
	public void setScan(Date scan) {
		this.scan = scan;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public long getTaille() {
		return taille;
	}
	public void setTaille(long taille) {
		this.taille = taille;
	}
	public String getChemin() {
		return chemin;
	}
	public void setChemin(String chemin) {
		this.chemin = chemin;
	}
	public String getId_pere() {
		return id_pere;
	}
	public void setId_pere(String id_pere) {
		this.id_pere = id_pere;
	}
	public ObjectId getScanned() {
		return scanned;
	}
	public void setScanned(ObjectId string) {
		this.scanned = string;
	}
	
	

}
