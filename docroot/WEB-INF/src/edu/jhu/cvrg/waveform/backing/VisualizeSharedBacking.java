package edu.jhu.cvrg.waveform.backing;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import edu.jhu.cvrg.waveform.model.StudyEntry;

@ManagedBean(name = "visualizeSharedBacking")
@SessionScoped
public class VisualizeSharedBacking  implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3526964652221486865L;
	private StudyEntry sharedStudyEntry;

	public StudyEntry getSharedStudyEntry() {
		System.out.println("||||| VisualizeSharedBacking, getSharedStudyEntry |||||");
		return sharedStudyEntry;
	}

	public void setSharedStudyEntry(StudyEntry sharedStudyEntry) {
		System.out.println("===== VisualizeSharedBacking, setSharedStudyEntry ========");
		this.sharedStudyEntry = sharedStudyEntry;
	}         
}
