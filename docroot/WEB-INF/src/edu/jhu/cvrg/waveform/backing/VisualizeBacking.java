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

import javax.annotation.PostConstruct;


import java.io.Serializable;
import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;

//import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
//import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;


//import org.json.JSONException;
import org.json.JSONObject;
//import org.omnifaces.util.Ajax;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.TreeNode;

import com.liferay.portal.model.User;

//import edu.jhu.cvrg.waveform.utility.AnnotationUtility;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
//import edu.jhu.cvrg.waveform.main.VisualizationManager;
import edu.jhu.cvrg.waveform.model.FileTree;
import edu.jhu.cvrg.waveform.model.StudyEntry;
//import edu.jhu.cvrg.waveform.model.VisualizationData;
//import edu.jhu.cvrg.waveform.utility.ServerUtility;

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
	private int iSingleLeadWidthMS = 2500;
	private int iGraphWidthPixels = 2500; //width of the longest graph which will use this data. Sets the maximum amount of data compression allowable.
	private String[] saGraphTitle= {"I","II","III","aVR","aVL","aVF","V1","V2","V3","V4","V5","V6","VX","VY","VZ"}; // default values, should be replaced by the this.setGraphTitle() method, though usually the values are the same.
	private JSONObject dataJson;
	private boolean newInstance = true;

	private User userModel;
	
	
	public void init() { // copied from analyze to replace initialize()
		System.out.println("*************** VisualizeBacking.java, init() copied from analyze to replace initialize() **********************");
		userModel = ResourceUtility.getCurrentUser();
		if(fileTree == null){
			System.out.println("*** creating new FileTree for user:" + userModel.getScreenName());
			fileTree = new FileTree();
			fileTree.initialize(userModel.getScreenName());
			System.out.println("*** fileTree == null :" + (fileTree == null));
		}else{
			System.out.println("*** fileTree already exists *** ");
		}
			
		System.out.println("*************** VisualizeBacking.java, init() finished **********************");
	}

	public void initialize(ComponentSystemEvent event) {
    	System.out.println("*************** VisualizeBacking.java, initialize() **********************");
    	
		if (newInstance) {
			System.out.println("***  New instance ****");
			userModel = ResourceUtility.getCurrentUser();
			if (selectVisible) {
				fileTree = new FileTree(userModel.getScreenName());
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
//    	setVisibleFragment(2); // show 12 lead graph page fragment.
    	if(selectedStudyObject.getLeadCount()==3){
    		nextView = "viewC_Display3Leads";
//    	setVisibleFragment(2); // show 12 lead graph page fragment.
//    	if(selectedStudyObject.getLeadCount()==12){
//    		nextView = "viewB_Display12Leads";
    	}else{
//    		if(selectedStudyObject.getLeadCount()==15){
//	    		nextView = "viewD_SingleLead";
//	    	}else{
	    		nextView = "viewB_Display12Leads";
	   // 	}
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
		System.out.println("--selectedNodes.size(): " + selectedNodes.size());
		setStudyEntryList(selectedNodes);
		System.out.println("-VisualizeBacking.displaySelectedMultiple() DONE");
	}
	
	public void folderSelect(NodeSelectEvent event){
		TreeNode node = event.getTreeNode();
		if(!node.getType().equals("document")){
			fileTree.selectAllChildNodes(node);
		}
	}
	
	public void folderUnSelect(NodeUnselectEvent event){
		TreeNode node = event.getTreeNode();
		node.setSelected(false);
		if(!node.getType().equals("document")){
			fileTree.unSelectAllChildNodes(node);
		}
	}

	public void onRowSelect(SelectEvent event) {
		selectedStudyObject = ((StudyEntry) event.getObject());
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

	public int getSingleLeadWidthMS() {
		return iSingleLeadWidthMS;
	}

	public void setSingleLeadWidthMS(int iSingleLeadWidthMS) {
		this.iSingleLeadWidthMS = iSingleLeadWidthMS;
		this.setDurationMilliSeconds(iSingleLeadWidthMS);
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
