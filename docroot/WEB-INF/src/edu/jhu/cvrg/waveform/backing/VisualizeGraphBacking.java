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
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;

import edu.jhu.cvrg.dbapi.dto.DocumentRecordDTO;
import edu.jhu.cvrg.dbapi.dto.FileInfoDTO;
import edu.jhu.cvrg.dbapi.factory.ConnectionFactory;
import edu.jhu.cvrg.waveform.main.VisualizationManager;
import edu.jhu.cvrg.waveform.model.LocalFileTree;
import edu.jhu.cvrg.waveform.model.MultiLeadLayout;
import edu.jhu.cvrg.waveform.model.VisualizationData;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.ServerUtility;

@ManagedBean(name = "visualizeGraphBacking")
@ViewScoped
public class VisualizeGraphBacking implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3657756514965814260L;


	@ManagedProperty("#{visualizeSharedBacking}")
	private VisualizeSharedBacking visualizeSharedBacking;         
	
	private String description;
	private boolean geVisible = true;
	private LocalFileTree fileTree;
	private boolean selectVisible = true, graphVisible = false, graphMultipleVisible=false;
	private int iCurrentVisualizationOffset=0; // 12 lead displays always start at zero seconds (0 ms).
	private String newMilliSec;
	private int iVisualizationWidthMS = 1200;
	private int iDurationMilliSeconds = 2500; // 2.5 second of data 
	private int iGraphWidthPixels = 1200; //width of the longest graph which will use this data. Sets the maximum amount of data compression allowable.
	private String[] saGraphTitle= {"I","II","III","aVR","aVL","aVF","V1","V2","V3","V4","V5","V6","VX","VY","VZ"}; // default values, should be replaced by the this.setGraphTitle() method, though usually the values are the same.
	private JSONObject dataJson = null;
	private ArrayList<MultiLeadLayout> multiLeadLayoutList;
	private int multiLeadColumnCount = 5;
	

	//private User userModel;
	
	public void initialize(ComponentSystemEvent event) {
    	System.out.println("*************** VisualizeGraphBacking.java, initialize() **********************");
    	//TODO[VILARDO] DATAFILE = .DAT FILE
    	/*System.out.println("*************** selected record:" + visualizeSharedBacking.getSharedStudyEntry().getRecordName() + " in file:" + visualizeSharedBacking.getSharedStudyEntry().getDataFile());*/
    	
    	if(dataJson == null){
			view12LeadsGraph();
    	}
    	System.out.println("*************** DONE, initialize() **********************");
	}

    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSelectionTree(){
    	System.out.println("+++ VisualizeGraphBacking.java, viewSelectTree() +++ ");
		return "viewA_SelectionTree";
    }
    
    
    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSingleGraph(){
    	System.out.println("+++ VisualizeGraphBacking.java, viewSingleGraph() +++ ");
    	setGraphMultipleVisible(false);
		return "viewD_SingleLead";
    }

    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSingleGraph2(){
    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		String passedLeadName = (String) map.get("sLeadName");
		String passedLeadNumber = (String) map.get("sLeadNumber");
		
		visualizeSharedBacking.setSelectedLeadName(passedLeadName);
		visualizeSharedBacking.setSelectedLeadNumber(passedLeadNumber);

		System.out.println("+++ VisualizeGraphBacking.java, viewSingleGraph2() passedLeadName: " + passedLeadName + " passedLeadNumber: " + passedLeadNumber + " +++ ");
    	setGraphMultipleVisible(false);
		return "viewD_SingleLead";
    }

    /** Loads the data for the selected ecg file and switches to the 12 lead graph panel.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String view12LeadsGraph(){
    	System.out.println("+ VisualizeGraphBacking.java, view12LeadsGraph() +++ ");
    	
		if(visualizeSharedBacking.getSharedStudyEntry() != null){
			int iaAnnCount[][] = fetchAnnotationArray();			
			iCurrentVisualizationOffset = 0;	
			int iLeadCount = fetchDisplayData();
			setGraphTitle(iaAnnCount, iLeadCount);
		}
    	System.out.println("+ Exiting view12LeadsGraph() +++ ");
    	setGraphMultipleVisible(true);
		return null;
    }

    /** Loads the data for the selected ecg file and switches to the 12 lead graph panel.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewMultiLeadGraph(){
    	System.out.println("+ VisualizeGraphBacking.java, viewMultiLeadGraph() +++ ");
    	
		if(visualizeSharedBacking.getSharedStudyEntry() != null){
			int iaAnnCount[][] = fetchAnnotationArray();			
			iCurrentVisualizationOffset = 0;	
			int iLeadCount = fetchDisplayData();
			setGraphTitle(iaAnnCount, iLeadCount);
		}
    	System.out.println("+ Exiting viewMultiLeadGraph() +++ ");
    	setGraphMultipleVisible(true);
		return "viewB_DisplayMultiLeads";
    }
	public void hideGe(ActionEvent e){
		this.geVisible = false;
	}
	
	public void showGe(ActionEvent e){
		this.geVisible = true;
	}

    public String getDescription() {
    	description = "Subject:" + visualizeSharedBacking.getSharedStudyEntry().getRecordName() 
    			+ " / Lead count:" + visualizeSharedBacking.getSharedStudyEntry().getLeadCount() 
    			+ " / Sampling-rate:" + visualizeSharedBacking.getSharedStudyEntry().getSamplingRate() + "Hz"
    			+ " / ECG duration:" + visualizeSharedBacking.getSharedStudyEntry().getDurationSec();
		return description;
	}
    
	public void setDescription(String description) {
		this.description = description;
	}

	public void setgraphedStudyEntry(DocumentRecordDTO selectedStudyObject) {this.visualizeSharedBacking.setSharedStudyEntry(selectedStudyObject);}
	public DocumentRecordDTO getgraphedStudyEntry() {return visualizeSharedBacking.getSharedStudyEntry();}

	public boolean isGeVisible() {return geVisible;}
	public void setGeVisible(boolean geVisible) {this.geVisible = geVisible;}

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

	public int getCurrentVisualizationOffset() {
		return iCurrentVisualizationOffset;
	}

	/** When this variable is changed, then the data will be fetched and the viewing window will be reloaded.
	 * 
	 * @param currentVisualizationOffset
	 */
	public void setCurrentVisualizationOffset(int currentVisualizationOffset) {
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
		System.out.println("visualizationWidthMS set to " + visualizationWidthMS);
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

	/** Creates the titles for the multi-lead graphs.
	 * 
	 * @param iaAnnCount
	 * @param iLeadCount
	 */
	public void setGraphTitle(int[][] iaAnnCount, int iLeadCount){
		System.out.print("--- Entering function setGraphTitle()");
		ServerUtility util = new ServerUtility(false);

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
	
	/** refetches the displayed data.  Called if there is a change to the durationMilliSeconds.	 */
	public void reloadData() {
		System.out.println("--Entering function reloadData()");
		fetchDisplayData();
		System.out.println("--Exiting function reloadData()");
	}
	public void panZeroSec() {
		System.out.println("--Entering function panZeroSec()");
		panToTime(0);
		System.out.println("--Exiting function panZeroSec()");
	}
	public void panRight() {
		System.out.println("--Entering function panRight");
		if(isGraphMultipleVisible()){
			panToTime(iCurrentVisualizationOffset + iVisualizationWidthMS);
		}else{
			panToTime(iCurrentVisualizationOffset + iDurationMilliSeconds);
		}
		System.out.println("--Exiting function panRight");
	}
	public void panLeft() {
		System.out.println("--Entering function panLeft");
		if(isGraphMultipleVisible()){
			panToTime(iCurrentVisualizationOffset - iVisualizationWidthMS);	
		}else{
			panToTime(iCurrentVisualizationOffset - iDurationMilliSeconds);
		}
		System.out.println("--Exiting function panLeft");
	}
	public void panEnd() {
		System.out.println("--Entering function panRight");
		int msInFullECG = visualizeSharedBacking.getSharedStudyEntry().getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file.
		panToTime(msInFullECG);
		System.out.println("--Exiting function panRight");
	}

	public void panToMilliSec(){
		System.out.println("-Entering function panToMilliSec, newMilliSec: \"" + getNewMilliSec() + "\"");
		int iStartPoint = parseToMilliSec(getNewMilliSec());
		if(iStartPoint ==-1){
			String message = "\"" + getNewMilliSec()  + "\" is not a recongnizable number. Please enter seconds in one of the following formats, \"123.45\", \"1.2345e2\" or \"1.2345 x 10^2\" ";
			System.err.println("Unable to parse requested new time: "+ message);
		}else{
			if(iStartPoint <= visualizeSharedBacking.getSharedStudyEntry().getMsecDuration()){
				panToTime(iStartPoint);
			}else{
				String message = iStartPoint + " is too large, this ECG contains " + (visualizeSharedBacking.getSharedStudyEntry().getMsecDuration()/1000.0) + " seconds";
				System.err.println(message);
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
				success = true;
			}catch (NumberFormatException e2){
				newScrollTimeMS=-1;
				success = false;
				String message = "\"" + sNewMilliSec + "\" is not a recongnizable number. Please enter seconds in one of the following formats, \"123.45\", \"1.2345e2\" or \"1.2345 x 10^2\" ";
				System.err.println(message);
			}
		}
		if(success){
			newScrollTimeMS = (int) (newTime*1000);
		}
		return newScrollTimeMS;
	}
	
	/** Pans display data to the StartPoint specified (in milliseconds).
	 * Ajusts for out-of-range values, then calls fetchDisplayData.
	 * 
	 * @param iStartPoint - new start time of graph data in milliseconds.
	 */
	public void panToTime(int iStartPoint) {
		System.out.println("--Entering function panToTime, iStartPoint:" + iStartPoint);
		int msInFullECG = visualizeSharedBacking.getSharedStudyEntry().getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file. 
		int maxAllowableOffset=0;
		
		// don't allow view frame to pan past the end of the data.
		// calculate largest allowable start point.
		if(isGraphMultipleVisible()){
			maxAllowableOffset = msInFullECG - iVisualizationWidthMS + 1; // one graph width before the end of the data.
		}else{
			maxAllowableOffset = msInFullECG - iDurationMilliSeconds + 1; // one graph width before the end of the data.			
		}
		// check if starting point is too large.
		if(iStartPoint < maxAllowableOffset){
			iCurrentVisualizationOffset = iStartPoint;
		}else{
			iCurrentVisualizationOffset = maxAllowableOffset; 
		}
		
		//check if starting point is too small.
		if(iCurrentVisualizationOffset<0) iCurrentVisualizationOffset = 0;
		
		fetchDisplayData();
		System.out.println("--Exiting function panToTime");
	}

	/** Fetch an array of all annotations on this ECG.
	 * 
	 * @return
	 */
	private int[][] fetchAnnotationArray(){
		System.out.println("--- fetchAnnotationArray()----");
		int iaAnnCount[][] = null;
		try {
			if(visualizeSharedBacking.getSharedStudyEntry() != null){
				
				Long docId = visualizeSharedBacking.getSharedStudyEntry().getDocumentRecordId();
				Integer leadCount = visualizeSharedBacking.getSharedStudyEntry().getLeadCount();
				
				iaAnnCount = ConnectionFactory.createConnection().getAnnotationCountPerLead(docId, leadCount);
				
			}else{
				System.err.println("--- fetchAnnotationArray() SharedStudyEntry not found.");
			}
			System.out.println("--- exiting fetchAnnotationArray()");
		} catch (Exception e) {
			System.err.println("Localized message: " + e.getLocalizedMessage());
			System.err.println("Detailed error message: " + e.getMessage());
			e.printStackTrace();
		}
		return iaAnnCount;
	}

	/** Fetch and display the ECG data for the current offset time.
	 * @return - lead count
	 */
	private int fetchDisplayData(){
		System.out.println("--- fetchDisplayData() with iCurrentVisualizationOffset:" + iCurrentVisualizationOffset + " and iDurationMilliSeconds:" + iDurationMilliSeconds);
		boolean verbose = false;
		boolean bTestPattern = false; // this will cause it to return 3 sine waves, and ignore all the other inputs.

		Long userID = ResourceUtility.getCurrentUserId();
		String subjectID = visualizeSharedBacking.getSharedStudyEntry().getSubjectId();
		
		long fileSize = 0l; //FIXME [VILARDO] visualizeSharedBacking.getSharedStudyEntry().getFileSize();

		//fetch data and print elapsed time.
		long startTime = System.currentTimeMillis();
		
		Map<String, FileEntry> files = this.getFileEntriesDocId(visualizeSharedBacking.getSharedStudyEntry().getDocumentRecordId());
		
		VisualizationManager visMan = new VisualizationManager(verbose);	
		VisualizationData VisData = visMan.fetchSubjectVisualizationData(userID, subjectID, files, fileSize, iCurrentVisualizationOffset, iDurationMilliSeconds, iGraphWidthPixels, bTestPattern);
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("--- - fetchSubjectVisualizationData() took " + estimatedTime +  " milliSeconds total. Sample Count:" + VisData.getECGDataLength() + " Lead Count:" + VisData.getECGDataLeads() );
		
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
	    
	    setMultiLeadLayoutList( MultiLeadLayout(VisData.getECGDataLeads()) );
	    
	    System.out.println("---Exiting function fetchDisplayData()");
		return VisData.getECGDataLeads();
	}

	private Map<String, FileEntry> getFileEntriesDocId(Long documentRecordId) {
		
		Map<String, FileEntry> ret = null;
		
		List<FileInfoDTO> fileInfoList = ConnectionFactory.createConnection().getFileListByDocumentRecordId(documentRecordId);
		
		if(fileInfoList!=null){
			ret = new HashMap<String, FileEntry>();
			for (FileInfoDTO fileDTO : fileInfoList) {
				try {
					
					FileEntry liferayFile = DLAppLocalServiceUtil.getFileEntry(fileDTO.getFileEntryId());
					ret.put(liferayFile.getTitle(), liferayFile);
					
				} catch (PortalException e) {
					e.printStackTrace();
				} catch (SystemException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
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

	public String[] getArrayGraphTitle() {
		return saGraphTitle;
	}
	
	public int getMultiLeadColumnCount(){
		return multiLeadColumnCount;
	}
	public void setMultiLeadColumnCount(int count){
		this.multiLeadColumnCount = count;
	}
	
	public ArrayList<MultiLeadLayout> getMultiLeadLayoutList() {
		return multiLeadLayoutList;
	}
	public void setMultiLeadLayoutList(ArrayList<MultiLeadLayout> multiLeadLayoutList) {
		this.multiLeadLayoutList = multiLeadLayoutList;
	}

	private ArrayList<MultiLeadLayout> MultiLeadLayout(int leadCount){
		switch (leadCount){
			case 3:
				setMultiLeadColumnCount(4);
				break;
			case 12:
				setMultiLeadColumnCount(5);
				break;
			default:
				setMultiLeadColumnCount(5);
				break;
		}
		
		ArrayList<MultiLeadLayout> alLayoutList = new ArrayList<MultiLeadLayout>();
		int iRowCount = (int) ((leadCount/(getMultiLeadColumnCount()-1))+0.5);  // rows always start with a calibration column, so data column count is one less.
		for(int row=0;row<iRowCount;row++){
			String debug = "layout row: " + row;
			for(int col=0;col<getMultiLeadColumnCount();col++){
				MultiLeadLayout layout = new MultiLeadLayout();
				if(col ==  0){
					// this is a calibration column
					layout.setLead(false);
					layout.setLeadNumber(row);
					debug += " cal"+ row;
				}else{
					// this is a lead data column
					layout.setLead(true);
					layout.setLeadNumber(row + ((col-1)*iRowCount));
					debug += ", L#"+ layout.getLeadNumber();
				}
				alLayoutList.add(layout);
			}
			System.out.println(debug);
		}
		return alLayoutList;
	}
	
	
	
	public VisualizeSharedBacking getVisualizeSharedBacking() {
		return visualizeSharedBacking;
	}

	public void setVisualizeSharedBacking(
			VisualizeSharedBacking visualizeSharedBacking) {
		this.visualizeSharedBacking = visualizeSharedBacking;
	}
}