package hr.fer.patenti.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "resultTable")
public class Result {
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(name="valueGroup") 
	private String group;
	@Column(name="valueHash") 
	private String hash;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

}
