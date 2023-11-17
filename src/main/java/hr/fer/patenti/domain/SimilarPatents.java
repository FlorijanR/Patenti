package hr.fer.patenti.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "similarPatents")
public class SimilarPatents {
	
	@Id
	private Long id;
	
	@Column(name="patentId")
	private String patentId;
	@Column(name="valueText") 
	private String text;

	public String getPatentId() {
		return patentId;
	}

	public void setPatentId(String patentId) {
		this.patentId = patentId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
