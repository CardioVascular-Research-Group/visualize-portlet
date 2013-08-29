package edu.jhu.cvrg.waveform.backing;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import edu.jhu.cvrg.waveform.model.StudyEntry;

@ManagedBean(name = "visualizeSharedBacking")
@SessionScoped
public class VisualizeSharedBacking {
	private StudyEntry sharedStudyEntry;

	public StudyEntry getSharedStudyEntry() {
		return sharedStudyEntry;
	}

	public void setSharedStudyEntry(StudyEntry sharedStudyEntry) {
		System.out.println("Set in shared object ========");
		this.sharedStudyEntry = sharedStudyEntry;
	}         
}
