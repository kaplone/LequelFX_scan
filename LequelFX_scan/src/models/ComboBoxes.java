package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComboBoxes extends Commun {
	
	private Set<String> tags;
	private Set<String> taille_disques;
	private Set<String> taille_restantes;
	
	
	
	public ComboBoxes() {
		super();
		this.tags = new HashSet<String>();
		this.taille_disques = new HashSet<String>();
		this.taille_restantes = new HashSet<String>();
	}
	public Set<String> getTags() {
		return tags;
	}
	public void setTags(Set<String> tags) {
		this.tags = tags;
	}
	public Set<String> getTaille_disques() {
		return taille_disques;
	}
	public void setTaille_disques(Set<String> taille_disques) {
		this.taille_disques = taille_disques;
	}
	public Set<String> getTaille_restantes() {
		return taille_restantes;
	}
	public void setTaille_restantes(Set<String> taille_restantes) {
		this.taille_restantes = taille_restantes;
	}
	
	public Set<String> addTag(String s){
		tags.add(s);
		return tags;
	}
	
	public Set<String> addTaille_disque(String s){
		taille_disques.add(s);
		return taille_disques;
	}
	
	public Set<String> addTaille_restante(String s){
		taille_restantes.add(s);
		return taille_restantes;
	}

}
