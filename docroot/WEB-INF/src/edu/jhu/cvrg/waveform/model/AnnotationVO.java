package edu.jhu.cvrg.waveform.model;

import java.io.Serializable;

public class AnnotationVO implements Serializable, Cloneable{

	private static final long serialVersionUID = -709730496507285517L;
	
	private double dataOnsetX; // X value (time) of a point or the Start of an interval
	private double dataOnsetY; // Y value (Voltage) of a point or the Start of an interval
	private double dataOffsetX;
	private double dataOffsetY;
	private double dataXChange; 
	private double dataYChange; 
	private String nodeID; // equivalent to Concept ID (AnnotationData.ConceptID) e.g. "ECGOntology:ECG_000000243".
	private String termName;
	private String fullAnnotation;
	private String comment;
	private boolean singlePoint=true;

	public AnnotationVO(double dataOnsetX, double dataOnsetY,
			double dataOffsetX, double dataOffsetY, String nodeID, String termName,
			String fullAnnotation, String comment, boolean singlePoint) {
		super();
		this.dataOnsetX = dataOnsetX;
		this.dataOnsetY = dataOnsetY;
		this.dataOffsetX = dataOffsetX;
		this.dataOffsetY = dataOffsetY;
		this.singlePoint = singlePoint;
		
		this.nodeID = nodeID;
		this.termName = termName;
		this.fullAnnotation = fullAnnotation;
		this.comment = comment;
		
	}

	
	public double getDataOnsetX() {
		return dataOnsetX;
	}

	public void setDataOnsetX(double dataSX) {
		this.dataOnsetX = dataSX;
	}

	public double getDataOnsetY() {
		return dataOnsetY;
	}

	public void setDataOnsetY(double dataSY) {
		this.dataOnsetY = dataSY;
	}
	
	public void setDataOffsetX(double dataOffsetX) {
		this.dataOffsetX = dataOffsetX;
	}

	public double getDataOffsetX() {
		return dataOffsetX;
	}
	
	public double getDataOffsetY() {
		return dataOffsetY;
	}
	public void setDataOffsetY(double dataOffsetY) {
		this.dataOffsetY = dataOffsetY;
	}
	
	public double getDataYChange() {
		if(singlePoint){
			this.dataYChange = 0;
		}else{
			this.dataYChange = getDataOffsetY()-getDataOnsetY();
		}
		return dataYChange;
	}
	
	public double getDataXChange() {
		if(singlePoint){
			this.dataXChange = 0;
		}else{
			this.dataXChange = getDataOffsetX()-getDataOnsetX();
		}
		return dataXChange;
	}
	
	public void setFullAnnotation(String fullAnnotation) {
		this.fullAnnotation = fullAnnotation;
	}
	
	public String getFullAnnotation() {
		return fullAnnotation;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}
	
	public boolean isSinglePoint() {
		return singlePoint;
	}
	
	public void setSinglePoint(boolean singlePoint) {
		this.singlePoint = singlePoint;
	}
	
	public String getTermName() {
		return termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}
	
	/** Comment on annotation, separate and in addition to the full annotation.
	 * Optional */
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public AnnotationVO clone(){
		
		try {
			return (AnnotationVO) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
