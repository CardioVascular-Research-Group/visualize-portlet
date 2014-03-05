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

@ViewScoped
@ManagedBean(name = "visualizeBacking")
public class VisualizeBacking extends BackingBean implements Serializable {

	@ManagedProperty("#{visualizeSharedBacking}")
	private VisualizeSharedBacking visualizeSharedBacking;  
	
	private static final long serialVersionUID = -4006126553152259063L;

	private ArrayList<FileTreeNode> selectedNodes;
	
	private DocumentRecordDTO selectedStudyObject;  
	
	private ArrayList<DocumentRecordDTO> studyEntryList;
	private LocalFileTree fileTree;
	
	private User userModel;
	
	@PostConstruct
	public void init() { 
		this.getLog().info("*************** VisualizeBacking.java, init() copied from analyze to replace initialize() **********************");
		userModel = ResourceUtility.getCurrentUser();
		if(userModel != null){
			visualizeSharedBacking.reset();
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
	
	public void setSelectedStudyObject(DocumentRecordDTO selectedStudyObject) {
		this.selectedStudyObject = selectedStudyObject;
		visualizeSharedBacking.setSharedStudyEntry(selectedStudyObject);
		this.getLog().info("Graphed Study Object Set");
	}

	public DocumentRecordDTO getSelectedStudyObject() {
		return selectedStudyObject;
	}

	public LocalFileTree getFileTree() {
		return fileTree;
	}

	public void setFileTree(LocalFileTree fileTree) {
		this.fileTree = fileTree;
	}

	public ArrayList<DocumentRecordDTO> getStudyEntryList() {
		return studyEntryList;
	}

	public void setStudyEntryList(ArrayList<DocumentRecordDTO> studyEntryList) {
		this.studyEntryList = studyEntryList;
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
