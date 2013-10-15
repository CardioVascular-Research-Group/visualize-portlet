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


import org.json.JSONArray;
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

@ManagedBean(name = "visualizeGraphBacking")
@ViewScoped
public class VisualizeGraphBacking implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3657756514965814260L;
//	private ArrayList<StudyEntry> selectedNodes;

	@ManagedProperty("#{visualizeSharedBacking}")
	private VisualizeSharedBacking visualizeSharedBacking;         
	
	private String description;
	private ArrayList<StudyEntry> studyEntryList;
	private boolean geVisible = true;
	private FileTree fileTree;
	private boolean selectVisible = true, graphVisible = false, graphMultipleVisible=false;
	private int iCurrentVisualizationOffset=0; // 12 lead displays always start at zero seconds (0 ms).
	private String newMilliSec;
	private int iVisualizationWidthMS = 1200;
	private int iDurationMilliSeconds = 1200; // 1.2 second of data 
	private int iGraphWidthPixels = 1200; //width of the longest graph which will use this data. Sets the maximum amount of data compression allowable.
	private String[] saGraphTitle= {"I","II","III","aVR","aVL","aVF","V1","V2","V3","V4","V5","V6","VX","VY","VZ"}; // default values, should be replaced by the this.setGraphTitle() method, though usually the values are the same.
	private JSONObject dataJson = null;


	private User userModel;
	
	public void initialize(ComponentSystemEvent event) {
    	System.out.println("*************** VisualizeGraphBacking.java, initialize() **********************");
    	System.out.println("*************** selected record:" + visualizeSharedBacking.getSharedStudyEntry().getRecordName() + " in file:" + visualizeSharedBacking.getSharedStudyEntry().getDataFile());

    	if(dataJson == null){
			userModel = ResourceUtility.getCurrentUser();
			view12LeadsGraph();
    	}
//		if(selectVisible) {
//			fileTree = new FileTree();
//			fileTree.initialize(userModel.getScreenName());
//		}
	}
    
//    public void viewSelectTree(ActionEvent event){
//    	System.out.println("VisualizeGraphBacking.java, viewSelectTree()");
//    	System.out.println("= graphVisible = " + graphVisible);
//    	setVisibleFragment(0); // show list/tree page fragment.
//    }

    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSelectionTree(){
    	System.out.println("+++ VisualizeGraphBacking.java, viewSelectTree() +++ ");
//    	setVisibleFragment(2); // show 12 lead graph page fragment.
		return "viewA_SelectionTree";
    }
    
    
    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSingleGraph(){
    	System.out.println("+++ VisualizeGraphBacking.java, viewSingleGraph() +++ ");
//    	setVisibleFragment(2); // show 12 lead graph page fragment.
		return "viewD_SingleLead";
//		return "viewD_Test";
    }

    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSingleGraph2(){
    	FacesContext context = FacesContext.getCurrentInstance();
		Map map = context.getExternalContext().getRequestParameterMap();
		String passedLeadName = (String) map.get("sLeadName");
		String passedLeadNumber = (String) map.get("sLeadNumber");
		
		visualizeSharedBacking.setSelectedLeadName(passedLeadName);
		visualizeSharedBacking.setSelectedLeadNumber(passedLeadNumber);
//		visualizeSharedBacking.getSharedAnnotationBacking().showAnnotationForLead();
    	System.out.println("+++ VisualizeGraphBacking.java, viewSingleGraph2() passedLeadName: " + passedLeadName + " passedLeadNumber: " + passedLeadNumber + " +++ ");
		return "viewD_SingleLead";
    }

    /** Switches to the 12 lead graph panel.
     * Handles onclick event for the button "btnView12LeadECG" in the panelVisualizeSelect fragment (panel).
     * 
     * @param event
     */
//    public void viewLeads(ActionEvent event){
//    	System.out.println("VisualizeGraphBacking.java, viewLeads()");
//    	System.out.println("+ graphVisible = " + isGraphVisible());
//    	System.out.println("+ graphMultipleVisible = " + isGraphMultipleVisible());
//    	//setVisibleFragment(2); // show 12 lead graph page fragment.
//    	generic12leadOnloadCallback();
//    }
    
    /** Loads the data for the selected ecg file and switches to the 12 lead graph panel.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String view12LeadsGraph(){
    	System.out.println("+ VisualizeGraphBacking.java, view12LeadsGraph() +++ ");
//    	System.out.println("++ graphVisible = " + isGraphVisible());
//    	System.out.println("++ graphMultipleVisible = " + isGraphMultipleVisible());
//    	generic12leadOnloadCallback();
    	
		if(visualizeSharedBacking.getSharedStudyEntry() != null){
			int iaAnnCount[][] = fetchAnnotationArray();			
//			panZeroSec();
			iCurrentVisualizationOffset = 0;	
			int iLeadCount = fetchDisplayData();
			setGraphTitle(iaAnnCount, iLeadCount);
		}
    	System.out.println("+ Exiting view12LeadsGraph() +++ ");

		return "viewB_Display12Leads.xhtml";
    }


    /** Determines which files are selected in the fileTree and displays them in the StudyEntryList.
     * Handles the click event from the button "btnDisplay".
     * @param event
     */
//	public void displaySelectedMultiple(ActionEvent event) {
//		System.out.println("-VisualizeGraphBacking.displaySelectedMultiple() ");
//		selectedNodes = fileTree.getSelectedFileNodes();
//		setStudyEntryList(selectedNodes);
//		System.out.println("-VisualizeGraphBacking.displaySelectedMultiple() DONE");
//	}

//	public void onRowSelect(SelectEvent event) {
//		//selectedStudyObject = ((StudyEntry) event.getObject());
//		System.out.println(" onRowSelect() selectedStudyObject " + graphedStudyEntry.toString()  );
//		FacesMessage msg = new FacesMessage("Selected Row", ((StudyEntry) event.getObject()).getStudy());
//		FacesContext.getCurrentInstance().addMessage(null, msg);
//		System.out.println(" onRowSelect() selectedStudyObject DONE");
//	}
//
//	public void onRowUnselect(UnselectEvent event) {
//		System.out.println(" onRowUnSelect() selectedStudyObject " + graphedStudyEntry.toString()  );
//		StudyEntry studyentry = ((StudyEntry) event.getObject());
//		FacesMessage msg = new FacesMessage("Unselected Row",studyentry.getStudy());
//		FacesContext.getCurrentInstance().addMessage(null, msg);
//		System.out.println(" onRowUnSelect() selectedStudyObject DONE");
//	}
	
	public void hideGe(ActionEvent e){
		this.geVisible = false;
	}
	
	public void showGe(ActionEvent e){
		this.geVisible = true;
	}

    public String getDescription() {
    	description = "Subject:" + visualizeSharedBacking.getSharedStudyEntry().getRecordName() 
    			+ " / Lead count:" + visualizeSharedBacking.getSharedStudyEntry().getLeadCount() 
    			+ " / Sampling-rate:" + visualizeSharedBacking.getSharedStudyEntry().getSamplingRate() + "Hz";
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setgraphedStudyEntry(StudyEntry selectedStudyObject) {this.visualizeSharedBacking.setSharedStudyEntry(selectedStudyObject);}
	public StudyEntry getgraphedStudyEntry() {return visualizeSharedBacking.getSharedStudyEntry();}

	public boolean isGeVisible() {return geVisible;}
	public void setGeVisible(boolean geVisible) {this.geVisible = geVisible;}

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
	public void setCurrentVisualizationOffset(int currentVisualizationOffset) {
		//this.currentVisualizationOffset = currentVisualizationOffset;
		panToTime(currentVisualizationOffset);
	}

	public String getNewMilliSec() {
		return newMilliSec;
	}

	public void setNewMilliSec(String newMilliSec) {
		this.newMilliSec = newMilliSec;
	}

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
//		System.out.println("-Entering function generic12leadOnCallback graphedStudyEntry:" + visualizeSharedBacking.getSharedStudyEntry().getRecordName());
//		if(visualizeSharedBacking.getSharedStudyEntry() != null){
//			int iaAnnCount[][] = fetchAnnotationArray();			
////			panZeroSec();
//			iCurrentVisualizationOffset = 0;	
//			int iLeadCount = fetchDisplayData();
//			setGraphTitle(iaAnnCount, iLeadCount);
//		}
//		System.out.println("Exiting function generic12leadOnCallback");
//	}
	
	/** Creates the titles for the multi-lead graphs.
	 * 
	 * @param iaAnnCount
	 * @param iLeadCount
	 */
	public void setGraphTitle(int[][] iaAnnCount, int iLeadCount){
		System.out.print("--- Entering function setGraphTitle()");
		ServerUtility util = new ServerUtility(false);
//		int iLeadCount = iaAnnCount.length;
		saGraphTitle = new String[iLeadCount+1];
		for(int[] iaACnt: iaAnnCount){
			String sName = util.guessLeadName(iaACnt[0]-1, iLeadCount);
			if(iaACnt[1] == 0){
				saGraphTitle[iaACnt[0]-1] = sName; // don't mention count when there are zero annotations
			}else{
				saGraphTitle[iaACnt[0]-1] = sName + " (" +  iaACnt[1] + " annotations)";
			}
			System.out.print(iaACnt[0]-1 + ":" + iaACnt[1] + ",");
		}		
		System.out.println("--- Exiting function setGraphTitle()");
	}
	
	public void panZeroSec() {
		System.out.println("--Entering function panZeroSec()");
		iCurrentVisualizationOffset = 0;	
		fetchDisplayData();
		System.out.println("--Exiting function panZeroSec()");
	}
	public void panRight() {
		System.out.println("--Entering function panRight");
		iCurrentVisualizationOffset += iVisualizationWidthMS;	
		fetchDisplayData();
		System.out.println("--Exiting function panRight");
	}
	public void panLeft() {
		System.out.println("--Entering function panLeft");
		iCurrentVisualizationOffset -= iVisualizationWidthMS;	
		fetchDisplayData();
		System.out.println("--Exiting function panLeft");
	}
	public void panEnd() {
		System.out.println("--Entering function panRight");
		int msInFullECG = visualizeSharedBacking.getSharedStudyEntry().getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file. 
		int lastDataOffset = msInFullECG - iVisualizationWidthMS + 1; // one graph width before the end of the data.
		iCurrentVisualizationOffset = lastDataOffset; 
		fetchDisplayData();
		System.out.println("--Exiting function panRight");
	}

	public void panToMilliSec(){
		System.out.println("-Entering function panToMilliSec, newMilliSec: \"" + getNewMilliSec() + "\"");
		int iStartPoint = parseToMilliSec(getNewMilliSec());
		if(iStartPoint ==-1){
			System.err.println("Unable to parse requested new time: \""+ getNewMilliSec() + "\"");
		}else{
			if(iStartPoint <= visualizeSharedBacking.getSharedStudyEntry().getMsecDuration()){
				panToTime(iStartPoint);
			}else{
				String msg = iStartPoint + " is too large, this ECG contains " + (visualizeSharedBacking.getSharedStudyEntry().getMsecDuration()/1000.0) + " seconds";
				System.err.println(msg);
			}
		}
		System.out.println("-Exiting function panToMilliSec");
	}
	
	private int parseToMilliSec(String sNewMilliSec){
		int newScrollTimeMS=-1;
		Double newTime = 0.0;
		String sSec2="";
		boolean success =  false;
		try{
			newTime = Double.parseDouble(sNewMilliSec);
			success = true;
		}catch (NumberFormatException e){ // try to change it from "A.AAA x 10^BB" format to "A.AAAeBB" 
			String expr = "\\s*[xX\\*]\\s*10\\s*\\^\\s*";
			sSec2 = sNewMilliSec.replaceAll(expr, "e");
			try{
				newTime = Double.parseDouble(sSec2);
			}catch (NumberFormatException e2){
				String msg = "\"" + sNewMilliSec + "\" is not a recongnizable number. Please enter seconds in one of the following formats, \"123.45\", \"1.2345e2\" or \"1.2345 x 10^2\" ";
			    FacesContext fcMsg = FacesContext.getCurrentInstance();  
			    fcMsg.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Pan to Time.", msg));
			}
			success = true;
		}
		if(success){
			newScrollTimeMS = (int) (newTime*1000);
		}
		return newScrollTimeMS;
	}
	
	public void panToTime(int iStartPoint) {
		System.out.println("--Entering function panToTime, iStartPoint:" + iStartPoint);
		int msInFullECG = visualizeSharedBacking.getSharedStudyEntry().getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file. 
		int lastDataOffset = msInFullECG - iVisualizationWidthMS + 1; // one graph width before the end of the data.
		if(iStartPoint>lastDataOffset) {// don't allow view frame to pan past the end of the data.
			iStartPoint = lastDataOffset;
		}
		iCurrentVisualizationOffset = iStartPoint; 
		fetchDisplayData();
		System.out.println("--Exiting function panToTime");
	}

	
	public void g12leadLoadHiRez() {
		System.out.println("Entering function g12leadLoadHiRez");
		iGraphWidthPixels = 2500;
		fetchDisplayData();
//		iGraphWidthPixels = 250;
		System.out.println("Exiting function g12leadLoadHiRez");
	}
	
	/** Fetch an array of all annotations on this ECG.
	 * 
	 * @return
	 */
	private int[][] fetchAnnotationArray(){
		System.out.println("--- fetchAnnotationArray()----");
		int iaAnnCount[][] = null;
		if(visualizeSharedBacking.getSharedStudyEntry() != null){
			AnnotationUtility annUtil = new AnnotationUtility(com.liferay.util.portlet.PortletProps.get("dbUser"),
					com.liferay.util.portlet.PortletProps.get("dbPassword"), 
					com.liferay.util.portlet.PortletProps.get("dbURI"),	
					com.liferay.util.portlet.PortletProps.get("dbDriver"), 
					com.liferay.util.portlet.PortletProps.get("dbMainDatabase"));
//			System.out.println("----AnnotationUtility using URI: " + annUtil.getURI());
			
			userModel = ResourceUtility.getCurrentUser();
			String SN = userModel.getScreenName();
//			System.out.println("----userModel.getScreenName(): " + SN);
			String St = visualizeSharedBacking.getSharedStudyEntry().getStudy();
//			System.out.println("----visualizeSharedBacking.getSharedStudyEntry().getStudy(): " + St);
			String SID = visualizeSharedBacking.getSharedStudyEntry().getSubjectID();
//			System.out.println("----visualizeSharedBacking.getSharedStudyEntry().getSubjectID(): " + SID);
			String RN = visualizeSharedBacking.getSharedStudyEntry().getRecordName();
//			System.out.println("----visualizeSharedBacking.getSharedStudyEntry().getRecordName(): " + RN);
			
			iaAnnCount = annUtil.getAnnotationCountPerLead(SN, St, SID, RN);
		}else{
			System.err.println("--- fetchAnnotationArray() SharedStudyEntry not found.");
		}
//		System.out.println("--- annotations count, 1st lead: " + iaAnnCount[0][1]);
		System.out.println("--- exiting fetchAnnotationArray()");
		return iaAnnCount;
	}

	/** Fetch and display the ECG data for the current offset time.
	 * @return - lead count
	 */
	private int fetchDisplayData(){
		System.out.println("--- fetchDisplayData() with iCurrentVisualizationOffset:" + iCurrentVisualizationOffset + " and iDurationMilliSeconds:" + iDurationMilliSeconds);
		boolean verbose = false;
		boolean bTestPattern = false; // this will cause it to return 3 sine waves, and ignore all the other inputs.
		String userID = userModel.getScreenName();
		String subjectID = visualizeSharedBacking.getSharedStudyEntry().getSubjectID();
		String[] saFileNameList = visualizeSharedBacking.getSharedStudyEntry().getAllFilenames();
		long fileSize = visualizeSharedBacking.getSharedStudyEntry().getFileSize(); 

		//fetch data and print elapsed time.
		long startTime = System.currentTimeMillis();
		VisualizationManager visMan = new VisualizationManager(verbose);		
		VisualizationData VisData = visMan.fetchSubjectVisualizationData(userID, subjectID, saFileNameList, fileSize, 
				iCurrentVisualizationOffset, iDurationMilliSeconds, iGraphWidthPixels, bTestPattern);
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("--- - fetchSubjectVisualizationData() took " + estimatedTime +  " milliSeconds. Sample Count:" + VisData.getECGDataLength() + " Lead Count:" + VisData.getECGDataLeads() );
		
		//	Check to see is the The Web Service is returning Data for the User Display.
	    if (VisData == null) {
		    FacesContext msgs = FacesContext.getCurrentInstance();  
		    msgs.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "The ECG lookup Web Service.", "reports failure!"));
		    System.out.println("--- get12leadOnloadCallback WARNING: The  ECG lookup WebService failed! ");
	    } else { 
			String dataForJavaScript = VisData.getECGDataSingleString();
			dataForJavaScript = dataForJavaScript.replace("\n", "\\n");
			System.out.println("--- get12leadOnloadCallback INFO:  dataForJavaScript.length: [" + dataForJavaScript.length() + "]");
			try {
				dataJson = new JSONObject();
				dataJson.put("ECG", dataForJavaScript);
				dataJson.put("minTime", new Integer(iCurrentVisualizationOffset).toString());
				dataJson.put("maxTime", new Integer(iCurrentVisualizationOffset + iVisualizationWidthMS).toString());			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    System.out.println("---Exiting function fetchDisplayData()");
		return VisData.getECGDataLeads();
	}

	/** Set booleans so that only one page fragment is displayed.
	 * 
	 * @param fragmentID :<BR>
	 * 0 = selection tree/lists<BR>
	 * 1 = single lead graph<BR>
	 * 2 = multiple lead (e.g. 3, 12 or 15) graph<BR>
	 */
	private void setVisibleFragment(int fragmentID){
    	System.out.println("VisualizeGraphBacking.java, setVisibleFragment(" + fragmentID + ")");

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

	public JSONArray getSaGraphTitle() {
		JSONArray jsonArrayTitle = new JSONArray();
		for(String title: saGraphTitle){
			jsonArrayTitle.put(title);
		}

		return jsonArrayTitle;
	}

	public VisualizeSharedBacking getVisualizeSharedBacking() {
		return visualizeSharedBacking;
	}

	public void setVisualizeSharedBacking(
			VisualizeSharedBacking visualizeSharedBacking) {
		this.visualizeSharedBacking = visualizeSharedBacking;
	}


}