package edu.jhu.cvrg.waveform.backing;
/*
Copyright 2013 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
/**
* @author Chris Jurado, Scott Alger, Mike Shipway
* 
*/
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;


import org.json.JSONException;
import org.json.JSONObject;
//import org.omnifaces.util.Ajax;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.waveform.utility.AnnotationUtility;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.main.VisualizationManager;
import edu.jhu.cvrg.waveform.model.FileTree;
import edu.jhu.cvrg.waveform.model.StudyEntry;
import edu.jhu.cvrg.waveform.model.VisualizationData;
import edu.jhu.cvrg.waveform.utility.ServerUtility;

@ManagedBean(name = "visualizeBacking")
@ViewScoped
public class VisualizeBacking implements Serializable {

	@ManagedProperty("#{visualizeSharedBacking}")
	private VisualizeSharedBacking visualizeSharedBacking;  
	
	private static final long serialVersionUID = -4006126553152259063L;

	private ArrayList<StudyEntry> selectedNodes;
	

	private StudyEntry selectedStudyObject;  
	
	private ArrayList<StudyEntry> studyEntryList;
	private boolean geVisible = true;
	private FileTree fileTree;
	private boolean selectVisible = true, graphVisible = false, graphMultipleVisible=false;
	private int iCurrentVisualizationOffset=0; // 12 lead displays always start at zero seconds (0 ms).
	private int iVisualizationWidthMS = 2500;
	private int iDurationMilliSeconds = 2500; // 2.5 second of data is needed for rhythm strip(s) at the bottom of the page. 
	private int iGraphWidthPixels = 2500; //width of the longest graph which will use this data. Sets the maximum amount of data compression allowable.
	private String[] saGraphTitle= {"I","II","III","aVR","aVL","aVF","V1","V2","V3","V4","V5","V6","VX","VY","VZ"}; // default values, should be replaced by the this.setGraphTitle() method, though usually the values are the same.
	private JSONObject dataJson;
	private boolean newInstance = true;

	private User userModel;
	
	public void initialize(ComponentSystemEvent event) {
    	System.out.println("*************** VisualizeBacking.java, initialize() **********************");
    	
		if (newInstance) {
			System.out.println("***  New instance ****");
			userModel = ResourceUtility.getCurrentUser();
			if (selectVisible) {
				fileTree = new FileTree();
				fileTree.initialize(userModel.getScreenName());
			}
		}
		newInstance = false;
	}
    
    public void viewSelectTree(ActionEvent event){
    	System.out.println("VisualizeBacking.java, viewSelectTree()");
    	System.out.println("= graphVisible = " + graphVisible);
    	setVisibleFragment(0); // show list/tree page fragment.
    }

    /** Switches to the 12 lead graph panel.
     * Handles onclick event for the button "btnView12LeadECG" in the panelVisualizeSelect fragment (panel).
     * 
     * @param event
     */
//    public void viewLeads(ActionEvent event){
//    	System.out.println("VisualizeBacking.java, viewLeads()");
//    	System.out.println("+ graphVisible = " + isGraphVisible());
//    	System.out.println("+ graphMultipleVisible = " + isGraphMultipleVisible());
//    	//setVisibleFragment(2); // show 12 lead graph page fragment.
//    	generic12leadOnloadCallback();
//    }
//    
    /** Loads the data for the selected ecg file and switches to the 12 lead graph panel.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String graphSelectedECG(){
    	String nextView="";
    	System.out.println("+++ VisualizeBacking.java, graphSelectedECG() +++ ");
    	System.out.println("+ selected record:" + selectedStudyObject.getRecordName() + " in file:" + selectedStudyObject.getDataFile() + " lead count:" + selectedStudyObject.getLeadCount());
    	setVisibleFragment(2); // show 12 lead graph page fragment.
    	if(selectedStudyObject.getLeadCount()==12){
    		nextView = "viewB_Display12Leads";
    	}else{
    		if(selectedStudyObject.getLeadCount()==15){
	    		nextView = "viewD_SingleLead";
	    	}else{
	    		nextView = "viewC_Display3Leads";
	    	}
    	}
    	
    	System.out.println("+ nextView:" + nextView); 
    	//generic12leadOnloadCallback();
		return nextView;
    }
//
//    public void viewSingleLead(ActionEvent event){
//    	System.out.println("VisualizeBacking.java, viewSingleLead()");
//    	System.out.println("- graphVisible = " + isGraphVisible());
//    	System.out.println("- graphMultipleVisible = " + isGraphMultipleVisible());
//    	//setVisibleFragment(1); // show single lead graph page fragment.
////    	generic12leadOnloadCallback();
//    }


    /** Determines which files are selected in the fileTree and displays them in the StudyEntryList.
     * Handles the click event from the button "btnDisplay".
     * @param event
     */
	public void displaySelectedMultiple(ActionEvent event) {
		System.out.println("-VisualizeBacking.displaySelectedMultiple() ");
		selectedNodes = fileTree.getSelectedFileNodes();
		setStudyEntryList(selectedNodes);
		System.out.println("-VisualizeBacking.displaySelectedMultiple() DONE");
	}

	public void onRowSelect(SelectEvent event) {
		//selectedStudyObject = ((StudyEntry) event.getObject());
		System.out.println(" onRowSelect() selectedStudyObject " + selectedStudyObject.toString()  );
		FacesMessage msg = new FacesMessage("Selected Row", ((StudyEntry) event.getObject()).getStudy());
		FacesContext.getCurrentInstance().addMessage(null, msg);
		System.out.println(" onRowSelect() selectedStudyObject DONE");
	}

	public void onRowUnselect(UnselectEvent event) {
		System.out.println(" onRowUnSelect() selectedStudyObject " + selectedStudyObject.toString()  );
		StudyEntry studyentry = ((StudyEntry) event.getObject());
		FacesMessage msg = new FacesMessage("Unselected Row",studyentry.getStudy());
		FacesContext.getCurrentInstance().addMessage(null, msg);
		System.out.println(" onRowUnSelect() selectedStudyObject DONE");
	}
	
	public void hideGe(ActionEvent e){
		this.geVisible = false;
	}
	
	public void showGe(ActionEvent e){
		this.geVisible = true;
	}

	public void setSelectedStudyObject(StudyEntry selectedStudyObject) {
		this.selectedStudyObject = selectedStudyObject;
		visualizeSharedBacking.setSharedStudyEntry(selectedStudyObject);
		System.out.println("Graphed Study Object Set");
	}

	public StudyEntry getSelectedStudyObject() {
		return selectedStudyObject;
	}

	/**
	 * @return
	 */
	public boolean isGeVisible() {
		return geVisible;
	}

	public void setGeVisible(boolean geVisible) {
		this.geVisible = geVisible;
	}

	public FileTree getFileTree() {
		return fileTree;
	}

	public void setFileTree(FileTree fileTree) {
		this.fileTree = fileTree;
	}


	public boolean isSelectVisible() {
		return selectVisible;
	}
	public void setSelectVisible(boolean selectVisible) {
		this.selectVisible = selectVisible;
	}

	public boolean isGraphVisible() {
		return graphVisible;
	}
	public void setGraphVisible(boolean graphVisible) {
		this.graphVisible = graphVisible;
	}

	public boolean isGraphMultipleVisible() {
		return graphMultipleVisible;
	}
	public void setGraphMultipleVisible(boolean graphMultipleVisible) {
		this.graphMultipleVisible = graphMultipleVisible;
	}

	public ArrayList<StudyEntry> getStudyEntryList() {
		return studyEntryList;
	}

	public void setStudyEntryList(ArrayList<StudyEntry> studyEntryList) {
		this.studyEntryList = studyEntryList;
	}

	public int getCurrentVisualizationOffset() {
		return iCurrentVisualizationOffset;
	}

	/** When this variable is changed, then the data will be fetched and the viewing window will be reloaded.
	 * 
	 * @param currentVisualizationOffset
	 */
//	public void setCurrentVisualizationOffset(int currentVisualizationOffset) {
//		//this.currentVisualizationOffset = currentVisualizationOffset;
//		g12leadPanToTime(currentVisualizationOffset);
//	}

	public int getVisualizationWidthMS() {
		return iVisualizationWidthMS;
	}

	public void setVisualizationWidthMS(int visualizationWidthMS) {
		this.iVisualizationWidthMS = visualizationWidthMS;
	}

	public int getDurationMilliSeconds() {
		return iDurationMilliSeconds;
	}

	public void setDurationMilliSeconds(int durationMilliSeconds) {
		this.iDurationMilliSeconds = durationMilliSeconds;
	}

	public int getGraphWidthPixels() {
		return iGraphWidthPixels;
	}

	public void setGraphWidthPixels(int graphWidthPixels) {
		this.iGraphWidthPixels = graphWidthPixels;
	}


	
	/** This function runs when the 12 lead page finishes loading.<BR>
	 * - It should be kept generic so that it can be used by different xhtml pages which display the same data in different layouts.<BR>
	 * - It first loads the first 10 seconds of the requested ECG file into the JavaScript "data[][]" variable.<BR>
	 * - Then it calls the Javascript function "WAVEFORM_showGraphs()" on the xhtml page to create all of the instances of dygraph and assign them to the correct div tags.<BR>
	 * - "WAVEFORM_showGraphs()" is kept on the .xhtml page, so that it can contain code specific to that layout.
	 * 
	 * @param event
	 */
//	public void generic12leadOnloadCallback() {
//		System.out.println("-Entering function generic12leadOnCallback");
//		if(selectedStudyObject != null){
////			AnnotationUtility annUtil = new AnnotationUtility(com.liferay.util.portlet.PortletProps.get("dbUser"),
////					com.liferay.util.portlet.PortletProps.get("dbPassword"), 
////					com.liferay.util.portlet.PortletProps.get("dbURI"),	
////					com.liferay.util.portlet.PortletProps.get("dbDriver"), 
////					com.liferay.util.portlet.PortletProps.get("dbMainDatabase"));
////			int iaAnnCount[][] = annUtil.getAnnotationCountPerLead(userModel.getScreenName(), 
////					selectedStudyObject.getStudy(),
////					selectedStudyObject.getSubjectID(),
////					selectedStudyObject.getRecordName());
//	
//			int iaAnnCount[][] = fetchAnnotationArray();			
////			g12leadPanZeroSec();
//			iCurrentVisualizationOffset = 0;	
//			int iLeadCount = fetchDisplayData();
//			setGraphTitle(iaAnnCount, iLeadCount);
//
//		}
//		System.out.println("Exiting function generic12leadOnCallback");
////		}else{
////		    FacesContext msgs = FacesContext.getCurrentInstance();  
////		    msgs.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Study not found.", "The selected study was not found or else no study was selected."));
////
////		}
//	}
//	
//	
//	public void setGraphTitle(int[][] iaAnnCount, int iLeadCount){
//		System.out.println("Entering function setGraphTitle()");
//		ServerUtility util = new ServerUtility(false);
////		int iLeadCount = iaAnnCount.length;
//		saGraphTitle = new String[iLeadCount+1];
//		for(int[] iaACnt: iaAnnCount){
//			String sName = util.guessLeadName(iaACnt[0]-1, iLeadCount);
//			saGraphTitle[iaACnt[0]-1] = sName + " (" +  iaACnt[1] + " annotations)";
//		}		
//	}
//	
//	public void g12leadPanZeroSec() {
//		System.out.println("--Entering function g12leadPanZeroSec()");
//		iCurrentVisualizationOffset = 0;	
//		fetchDisplayData();
//		System.out.println("--Exiting function g12leadPanZeroSec()");
//	}
//	public void g12leadPanRight() {
//		System.out.println("Entering function g12leadPanRight");
//		iCurrentVisualizationOffset += iVisualizationWidthMS;	
//		fetchDisplayData();
//	}
//	public void g12leadPanLeft() {
//		System.out.println("Entering function g12leadPanLeft");
//		iCurrentVisualizationOffset -= iVisualizationWidthMS;	
//		fetchDisplayData();
//
//	}
//	public void g12leadPanEnd() {
//		System.out.println("Entering function g12leadPanRight");
//		int msInFullECG = selectedStudyObject.getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file. 
//		int lastDataOffset = msInFullECG - iVisualizationWidthMS + 1; // one graph width before the end of the data.
//		iCurrentVisualizationOffset = lastDataOffset; 
//		fetchDisplayData();
//	}
//
//	public void g12leadPanToTime(int iStartPoint) {
//		System.out.println("Entering function g12leadPanToTime");
//		int msInFullECG = selectedStudyObject.getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file. 
//		int lastDataOffset = msInFullECG - iVisualizationWidthMS + 1; // one graph width before the end of the data.
//		if(iStartPoint>lastDataOffset) {// don't allow view frame to pan past the end of the data.
//			iStartPoint = lastDataOffset;
//		}
//		iCurrentVisualizationOffset = iStartPoint; 
//		fetchDisplayData();
//	}
//
//	
//	public void g12leadLoadHiRez() {
//		System.out.println("Entering function g12leadLoadHiRez");
//		iGraphWidthPixels = 2500;
//		fetchDisplayData();
////		iGraphWidthPixels = 250;
//	}
//	
//	/** Fetch an array of all annotations on this ECG.
//	 * 
//	 * @return
//	 */
//	private int[][] fetchAnnotationArray(){
//		int iaAnnCount[][] = null;
//		if(selectedStudyObject != null){
//			AnnotationUtility annUtil = new AnnotationUtility(com.liferay.util.portlet.PortletProps.get("dbUser"),
//					com.liferay.util.portlet.PortletProps.get("dbPassword"), 
//					com.liferay.util.portlet.PortletProps.get("dbURI"),	
//					com.liferay.util.portlet.PortletProps.get("dbDriver"), 
//					com.liferay.util.portlet.PortletProps.get("dbMainDatabase"));
//			iaAnnCount = annUtil.getAnnotationCountPerLead(userModel.getScreenName(), 
//					selectedStudyObject.getStudy(),
//					selectedStudyObject.getSubjectID(),
//					selectedStudyObject.getRecordName());
//		}
//		return iaAnnCount;
//	}
//
//	/** Fetch and display the ECG data for the current offset time.
//	 * @return - lead count
//	 */
//	private int fetchDisplayData(){
//		System.out.println("---Entering function fetchDisplayData() with iCurrentVisualizationOffset:" + iCurrentVisualizationOffset);
//		boolean verbose = false;
//
//		String userID="";
//		String subjectID ="";
//		String[] saFileNameList;
//		boolean bTestPattern = false; // this will cause it to return 3 sine waves, and ignore all the other inputs.
//
//		VisualizationManager visMan = new VisualizationManager(verbose);		
//		userID = userModel.getScreenName();
//		subjectID = selectedStudyObject.getSubjectID();
//		saFileNameList = selectedStudyObject.getAllFilenames();
//	    		
//		long fileSize = selectedStudyObject.getFileSize(); 
//		// These variables are probably fine with these values.
//
//		long startTime = System.currentTimeMillis();
//		VisualizationData VisData = visMan.fetchSubjectVisualizationData(userID, subjectID, saFileNameList, fileSize, 
//				iCurrentVisualizationOffset, iDurationMilliSeconds, iGraphWidthPixels, bTestPattern);
//		long estimatedTime = System.currentTimeMillis() - startTime;
//		System.out.println("--- - fetchSubjectVisualizationData() took " + estimatedTime +  " milliSeconds. Sample Count:" + VisData.getECGDataLength() + " Lead Count:" + VisData.getECGDataLeads() );
//		
//		//	Check to see is the The Web Service is returning Data for the User Display.
//	    if (VisData == null) {
//		    FacesContext msgs = FacesContext.getCurrentInstance();  
//		    msgs.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "The Web Service.", "reports failure!"));
//		    System.out.println("--- get12leadOnloadCallback WARNING: The WebService failed! ");
//	    } else { 
//			String dataForJavaScript = VisData.getECGDataSingleString();
//			dataForJavaScript = dataForJavaScript.replace("\n", "\\n");
//			System.out.println("--- get12leadOnloadCallback INFO:  dataForJavaScript.length: [" + dataForJavaScript.length() + "]");
//			try {
//				dataJson = new JSONObject();
//				dataJson.put("ECG", dataForJavaScript);
//				dataJson.put("minTime", new Integer(iCurrentVisualizationOffset).toString());
//				dataJson.put("maxTime", new Integer(iCurrentVisualizationOffset + iVisualizationWidthMS).toString());			
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	    }
//	    System.out.println("---Exiting function fetchDisplayData()");
//		return VisData.getECGDataLeads();
//	}

	/** Set booleans so that only one page fragment is displayed.
	 * 
	 * @param fragmentID :<BR>
	 * 0 = selection tree/lists<BR>
	 * 1 = single lead graph<BR>
	 * 2 = multiple lead (e.g. 3, 12 or 15) graph<BR>
	 */
	private void setVisibleFragment(int fragmentID){
    	System.out.println("VisualizeBacking.java, setVisibleFragment(" + fragmentID + ")");

		// reset all
		setSelectVisible(false);
		setGraphVisible(false);
		setGraphMultipleVisible(false);
		
		// set specified fragment
		switch(fragmentID){
			case 0: // show only selection tree/lists page.
				setSelectVisible(true);
				break;
			case 1: // show only single lead graph page.
				setGraphVisible(true);
				break;
			case 2: // show only multiple lead (e.g. 3, 12 or 15) graph page.
				setGraphMultipleVisible(true);
				break;
			default: 
				setSelectVisible(true);
				break;
		}
	}

	public JSONObject getData() {
		return dataJson;
	}

	public void setData(JSONObject dataJson) {
		this.dataJson = dataJson;
	}

	public VisualizeSharedBacking getVisualizeSharedBacking() {
		return visualizeSharedBacking;
	}

	public void setVisualizeSharedBacking(
			VisualizeSharedBacking visualizeSharedBacking) {
		this.visualizeSharedBacking = visualizeSharedBacking;
	}


}