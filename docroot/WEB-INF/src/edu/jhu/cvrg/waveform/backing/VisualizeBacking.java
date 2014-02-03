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

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.json.JSONObject;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.TreeNode;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.dbapi.dto.DocumentRecordDTO;
import edu.jhu.cvrg.dbapi.factory.Connection;
import edu.jhu.cvrg.dbapi.factory.ConnectionFactory;
import edu.jhu.cvrg.waveform.model.FileTreeNode;
import edu.jhu.cvrg.waveform.model.LocalFileTree;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;

@ManagedBean(name = "visualizeBacking")
@ViewScoped
public class VisualizeBacking extends BackingBean implements Serializable {

	@ManagedProperty("#{visualizeSharedBacking}")
	private VisualizeSharedBacking visualizeSharedBacking;  
	
	private static final long serialVersionUID = -4006126553152259063L;

	private ArrayList<FileTreeNode> selectedNodes;
	
	private DocumentRecordDTO selectedStudyObject;  
	
	private ArrayList<DocumentRecordDTO> studyEntryList;
	private boolean geVisible = true;
	private LocalFileTree fileTree;
	private boolean selectVisible = true, graphVisible = false, graphMultipleVisible=false;
	private int iCurrentVisualizationOffset=0; // 12 lead displays always start at zero seconds (0 ms).
	private int iVisualizationWidthMS = 2500;
	private int iDurationMilliSeconds = 2500; // 2.5 second of data is needed for rhythm strip(s) at the bottom of the page. 
	private int iSingleLeadWidthMS = 2500;
	private int iGraphWidthPixels = 2500; //width of the longest graph which will use this data. Sets the maximum amount of data compression allowable.
	private JSONObject dataJson;
	
	private User userModel;
	
	@PostConstruct
	public void init() { 
		this.getLog().info("*************** VisualizeBacking.java, init() copied from analyze to replace initialize() **********************");
		userModel = ResourceUtility.getCurrentUser();
		if(userModel != null){
			if(fileTree == null){
				this.getLog().info("*** creating new FileTree for user:" + userModel.getScreenName());
				fileTree = new LocalFileTree(userModel.getUserId(), "hea");
				this.getLog().info("*** fileTree == null :" + (fileTree == null));
			}else{
				this.getLog().info("*** fileTree already exists *** ");
			}
		}
			
		this.getLog().info("*************** VisualizeBacking.java, init() finished **********************");
	}
    
    public void viewSelectTree(ActionEvent event){
    	this.getLog().info("VisualizeBacking.java, viewSelectTree()");
    	this.getLog().info("= graphVisible = " + graphVisible);
    	setVisibleFragment(0); // show list/tree page fragment.
    }

    /** Loads the data for the selected ecg file and switches to the 12 lead graph panel.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String graphSelectedECG(){
    	String nextView= null;
    	this.getLog().info("+++ VisualizeBacking.java, graphSelectedECG() +++ ");
    	
    	if(selectedStudyObject != null){
    		this.getLog().info("+ selected record:" + selectedStudyObject.getRecordName() + " lead count:" + selectedStudyObject.getLeadCount());
	   		nextView = "viewB_DisplayMultiLeads";
    	}else{
    		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Validation Error" , "<br />Please select a file."));	
    	}
    	
    	this.getLog().info("+ nextView:" + nextView); 
		return nextView;
    }

    /** Determines which files are selected in the fileTree and displays them in the StudyEntryList.
     * Handles the click event from the button "btnDisplay".
     * @param event
     */
	public void displaySelectedMultiple(ActionEvent event) {
		this.getLog().info("-VisualizeBacking.displaySelectedMultiple() ");
		selectedNodes = fileTree.getSelectedFileNodes();
		
		if(selectedNodes != null && !selectedNodes.isEmpty()){
			this.getLog().info("--selectedNodes.size(): " + selectedNodes.size());
			Connection database = ConnectionFactory.createConnection();
		
			studyEntryList = new ArrayList<DocumentRecordDTO>();
			for (FileTreeNode node : selectedNodes) {
				studyEntryList.add(database.getDocumentRecordById(node.getDocumentRecordId()));
			}
		}else{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "<br />Please select a file."));
		}
		
		this.getLog().info("-VisualizeBacking.displaySelectedMultiple() DONE");
	}
	
	public void folderSelect(NodeSelectEvent event){
		TreeNode node = event.getTreeNode();
		if(!node.getType().equals(FileTreeNode.DEFAULT_TYPE)){
			fileTree.selectAllChildNodes(node);
		}
	}
	
	public void folderUnSelect(NodeUnselectEvent event){
		TreeNode node = event.getTreeNode();
		node.setSelected(false);
		if(!node.getType().equals(FileTreeNode.DEFAULT_TYPE)){
			fileTree.unSelectAllChildNodes(node);
		}
	}

	public void onRowSelect(SelectEvent event) {
		setSelectedStudyObject((DocumentRecordDTO) event.getObject());
	}

	public void onRowUnselect(UnselectEvent event) {
		setSelectedStudyObject(null);
	}
	
	public void hideGe(ActionEvent e){
		this.geVisible = false;
	}
	
	public void showGe(ActionEvent e){
		this.geVisible = true;
	}

	public void setSelectedStudyObject(DocumentRecordDTO selectedStudyObject) {
		this.selectedStudyObject = selectedStudyObject;
		visualizeSharedBacking.setSharedStudyEntry(selectedStudyObject);
		this.getLog().info("Graphed Study Object Set");
	}

	public DocumentRecordDTO getSelectedStudyObject() {
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

	public LocalFileTree getFileTree() {
		return fileTree;
	}

	public void setFileTree(LocalFileTree fileTree) {
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

	public ArrayList<DocumentRecordDTO> getStudyEntryList() {
		return studyEntryList;
	}

	public void setStudyEntryList(ArrayList<DocumentRecordDTO> studyEntryList) {
		this.studyEntryList = studyEntryList;
	}

	public int getCurrentVisualizationOffset() {
		return iCurrentVisualizationOffset;
	}

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
		this.getLog().info("VisualizeBacking.java, setVisibleFragment(" + fragmentID + ")");

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

	public void setVisualizeSharedBacking(VisualizeSharedBacking visualizeSharedBacking) {
		this.visualizeSharedBacking = visualizeSharedBacking;
	}

	public User getUser(){
		return userModel;
	}
}
