package models;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Scan extends Commun{
	
	private String disque;
	private Date date;
	private int rang;
	private int next;
	
	private String tag;
	private String taille_disque;
	private String taille_restante;
	
	public String getDisque() {
		return disque;
	}
	public void setDisque(String disque) {
		this.disque = disque;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public int getRang() {
		return rang;
	}
	public void setRang(int rang) {
		this.rang = rang;
	}
	public int getNext() {
		return next;
	}
	public void setNext(int next) {
		this.next = next;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getTaille_disque() {
		return taille_disque;
	}
	public void setTaille_disque(String taille_disque) {
		this.taille_disque = taille_disque;
	}
	public String getTaille_restante() {
		return taille_restante;
	}
	public void setTaille_restante(String taille_restante) {
		this.taille_restante = taille_restante;
	}
    
	
}
