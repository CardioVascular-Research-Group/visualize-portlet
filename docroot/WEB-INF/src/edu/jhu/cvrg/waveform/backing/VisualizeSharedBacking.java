package edu.jhu.cvrg.waveform.backing;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import edu.jhu.cvrg.dbapi.dto.DocumentRecordDTO;

@ManagedBean(name = "visualizeSharedBacking")
@SessionScoped
public class VisualizeSharedBacking  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3526964652221486865L;
	private String selectedLeadName="";
	private String selectedLeadNumber="";

	private DocumentRecordDTO sharedStudyEntry;

	public String getSelectedLeadName() {
		return selectedLeadName;
	}
	public void setSelectedLeadName(String selectedLeadName) {
		this.selectedLeadName = selectedLeadName;
	}


	public String getSelectedLeadNumber() {
		return selectedLeadNumber;
	}
	public void setSelectedLeadNumber(String selectedLeadNumber) {
		this.selectedLeadNumber = selectedLeadNumber;
	}

	public DocumentRecordDTO getSharedStudyEntry() {
		return sharedStudyEntry;
	}
	public void setSharedStudyEntry(DocumentRecordDTO sharedStudyEntry) {
		System.out.println("===== VisualizeSharedBacking, setSharedStudyEntry ========");
		this.sharedStudyEntry = sharedStudyEntry;
	}
	
    
    public String getLeadCount(){
    	return String.valueOf(getSharedStudyEntry().getLeadCount());
    }

    public String getSamplingRate(){
    	return String.valueOf(getSharedStudyEntry().getSamplingRate()) + "Hz";
    }

    public String getDurationSec(){
    	return String.valueOf(getSharedStudyEntry().getDurationSec()) + " seconds";
    }
 
}
