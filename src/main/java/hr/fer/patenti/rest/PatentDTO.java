package hr.fer.patenti.rest;

public class PatentDTO {
	private String patentText;
	private String modelName;

	public String getPatentText() {
		return patentText;
	}

	public void setPatentText(String patentText) {
		this.patentText = patentText;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelNumber) {
		this.modelName = modelNumber;
	}
}
