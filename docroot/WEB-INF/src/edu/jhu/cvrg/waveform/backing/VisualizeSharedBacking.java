package edu.jhu.cvrg.waveform.backing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portlet.documentlibrary.service.DLAppLocalServiceUtil;

import edu.jhu.cvrg.dbapi.dto.AnnotationDTO;
import edu.jhu.cvrg.dbapi.dto.DocumentRecordDTO;
import edu.jhu.cvrg.dbapi.dto.FileInfoDTO;
import edu.jhu.cvrg.dbapi.factory.ConnectionFactory;
import edu.jhu.cvrg.waveform.main.VisualizationManager;
import edu.jhu.cvrg.waveform.model.AnnotationVO;
import edu.jhu.cvrg.waveform.model.VisualizationData;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.ServerUtility;

@ManagedBean(name = "visualizeSharedBacking")
@SessionScoped
public class VisualizeSharedBacking extends BackingBean implements Serializable {
	
	private static final long serialVersionUID = -3526964652221486865L;
	
	private String selectedLeadName="";
	private String selectedLeadNumber="";
	private int currentVisualizationOffset=0;
	private int durationMilliSeconds = 2500; // 2.5 second of data
	private int visualizationWidthMS = 1200;
	private int graphWidthPixels = 1200; //width of the longest graph which will use this data. Sets the maximum amount of data compression allowable.
	private String newMilliSec;
	private JSONObject data = null;
	private boolean graphMultipleVisible = false;

	private String[] saGraphTitle= {"I","II","III","aVR","aVL","aVF","V1","V2","V3","V4","V5","V6","VX","VY","VZ"}; // default values, should be replaced by the this.setGraphTitle() method, though usually the values are the same.
	private DocumentRecordDTO sharedStudyEntry;

	private AnnotationVO sessionAnn;
	
	private boolean previousAnnotation=false;
    private boolean showFineGraph=false;
	public int annotationCount;
	
	/** 
	 * Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSelectionTree(){
    	return "viewA_SelectionTree";
    }
 
    public String viewMultiLeadGraph(){
    	return "viewB_DisplayMultiLeads";
    }
    
    /** 
     * Refetches the displayed data.  Called if there is a change to the durationMilliSeconds.	 
     * 
     * */
	public void reloadData() {
		this.getLog().info("--Entering function reloadData()");
		fetchDisplayData();
		this.getLog().info("--Exiting function reloadData()");
	}
	public void panZeroSec() {
		this.getLog().info("--Entering function panZeroSec()");
		panToTime(0);
		this.getLog().info("--Exiting function panZeroSec()");
	}
	public void panRight() {
		this.getLog().info("--Entering function panRight");
		if(isGraphMultipleVisible()){
			panToTime(currentVisualizationOffset + visualizationWidthMS);
		}else{
			panToTime(currentVisualizationOffset + durationMilliSeconds);
		}
		this.getLog().info("--Exiting function panRight");
	}
	public void panLeft() {
		this.getLog().info("--Entering function panLeft");
		if(isGraphMultipleVisible()){
			panToTime(currentVisualizationOffset - visualizationWidthMS);	
		}else{
			panToTime(currentVisualizationOffset - durationMilliSeconds);
		}
		this.getLog().info("--Exiting function panLeft");
	}
	public void panEnd() {
		this.getLog().info("--Entering function panRight");
		int msInFullECG = getSharedStudyEntry().getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file.
		panToTime(msInFullECG);
		this.getLog().info("--Exiting function panRight");
	}

	public void panToMilliSec(){
		this.getLog().info("-Entering function panToMilliSec, newMilliSec: \"" + getNewMilliSec() + "\"");
		int iStartPoint = parseToMilliSec(getNewMilliSec());
		if(iStartPoint ==-1){
			String message = "\"" + getNewMilliSec()  + "\" is not a recongnizable number. Please enter seconds in one of the following formats, \"123.45\", \"1.2345e2\" or \"1.2345 x 10^2\" ";
			this.getLog().error("Unable to parse requested new time: "+ message);
		}else{
			if(iStartPoint <= getSharedStudyEntry().getMsecDuration()){
				panToTime(iStartPoint);
			}else{
				String message = iStartPoint + " is too large, this ECG contains " + (getSharedStudyEntry().getMsecDuration()/1000.0) + " seconds";
				this.getLog().error(message);
			}
		}
		this.getLog().info("-Exiting function panToMilliSec");
	}
	
	private int parseToMilliSec(String newMilliSec){
		int newScrollTimeMS=-1;
		Double newTime = 0.0;
		String sSec2="";
		boolean success =  false;
		try{
			newTime = Double.parseDouble(newMilliSec);
			success = true;
		}catch (NumberFormatException e){ // try to change it from "A.AAA x 10^BB" format to "A.AAAeBB" 
			String expr = "\\s*[xX\\*]\\s*10\\s*\\^\\s*";
			sSec2 = newMilliSec.replaceAll(expr, "e");
			try{
				newTime = Double.parseDouble(sSec2);
				success = true;
			}catch (NumberFormatException e2){
				newScrollTimeMS=-1;
				success = false;
				String message = "\"" + newMilliSec + "\" is not a recongnizable number. Please enter seconds in one of the following formats, \"123.45\", \"1.2345e2\" or \"1.2345 x 10^2\" ";
				this.getLog().error(message);
			}
		}
		if(success){
			newScrollTimeMS = (int) (newTime*1000);
		}
		return newScrollTimeMS;
	}
	
	/** 
	 * Pans display data to the StartPoint specified (in milliseconds).
	 * Adjusts for out-of-range values, then calls fetchDisplayData.
	 * 
	 * @param iStartPoint - new start time of graph data in milliseconds.
	 */
	public void panToTime(int iStartPoint) {
		this.getLog().info("--Entering function panToTime, iStartPoint:" + iStartPoint);
		int msInFullECG = getSharedStudyEntry().getMsecDuration();  //(int)((NumPts/sampRate)*1000.0); // number of milliseconds in full ECG file. 
		int maxAllowableOffset=0;
		
		// don't allow view frame to pan past the end of the data.
		// calculate largest allowable start point.
		if(isGraphMultipleVisible()){
			maxAllowableOffset = msInFullECG - visualizationWidthMS + 1; // one graph width before the end of the data.
		}else{
			maxAllowableOffset = msInFullECG - durationMilliSeconds + 1; // one graph width before the end of the data.			
		}
		// check if starting point is too large.
		if(iStartPoint < maxAllowableOffset){
			currentVisualizationOffset = iStartPoint;
		}else{
			currentVisualizationOffset = maxAllowableOffset; 
		}
		
		//check if starting point is too small.
		if(currentVisualizationOffset<0) currentVisualizationOffset = 0;
		
		fetchDisplayData();
		this.getLog().info("--Exiting function panToTime");
	}
	
	
	/** Fetch and display the ECG data for the current offset time.
	 * @return - lead count
	 */
	protected int fetchDisplayData(){
		this.getLog().info("--- fetchDisplayData() with iCurrentVisualizationOffset:" + currentVisualizationOffset + " and iDurationMilliSeconds:" + durationMilliSeconds);
		boolean bTestPattern = false; // this will cause it to return 3 sine waves, and ignore all the other inputs.

		Long userID = ResourceUtility.getCurrentUserId();
		String subjectID = getSharedStudyEntry().getSubjectId();
		double samplingRate = getSharedStudyEntry().getSamplingRate();
		int leadCount = getSharedStudyEntry().getLeadCount();
		int samplesPerChannel = getSharedStudyEntry().getSamplesPerChannel();
		
		//fetch data and print elapsed time.
		long startTime = System.currentTimeMillis();
		
		Map<String, FileEntry> files = this.getFileEntriesDocId(getSharedStudyEntry().getDocumentRecordId());
		
		VisualizationData visData = VisualizationManager.fetchSubjectVisualizationData(userID, subjectID, files, currentVisualizationOffset, durationMilliSeconds, graphWidthPixels, bTestPattern, samplingRate, leadCount, samplesPerChannel);
		
		long estimatedTime = System.currentTimeMillis() - startTime;
		this.getLog().info("--- - fetchSubjectVisualizationData() took " + estimatedTime +  " milliSeconds total. Sample Count:" + visData.getECGDataLength() + " Lead Count:" + visData.getECGDataLeads() );
		
		//	Check to see is the The Web Service is returning Data for the User Display.
	    if (visData == null) {
		    FacesContext msgs = FacesContext.getCurrentInstance();  
		    msgs.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "The ECG lookup Web Service.", "reports failure!"));
		    this.getLog().error("--- get12leadOnloadCallback WARNING: The  ECG lookup WebService failed! ");
	    } else { 
			String dataForJavaScript = visData.getECGDataSingleString();
			dataForJavaScript = dataForJavaScript.replace("\n", "\\n");
			this.getLog().info("--- get12leadOnloadCallback INFO:  dataForJavaScript.length: [" + dataForJavaScript.length() + "]");
			try {
				data = new JSONObject();
				data.put("ECG", dataForJavaScript);
				data.put("minTime", new Integer(currentVisualizationOffset).toString());
				data.put("maxTime", new Integer(currentVisualizationOffset + visualizationWidthMS).toString());			
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }
	    
	    this.getLog().info("---Exiting function fetchDisplayData()");
		return visData.getECGDataLeads();
	}

	private Map<String, FileEntry> getFileEntriesDocId(Long documentRecordId) {
		
		Map<String, FileEntry> ret = null;
		
		List<FileInfoDTO> fileInfoList = ConnectionFactory.createConnection().getFileListByDocumentRecordId(documentRecordId);
		
		if(fileInfoList!=null){
			ret = new HashMap<String, FileEntry>();
			for (FileInfoDTO fileDTO : fileInfoList) {
				try {
					FileEntry liferayFile = DLAppLocalServiceUtil.getFileEntry(fileDTO.getFileEntryId());
					if(".hea.dat".contains(liferayFile.getExtension())){
						ret.put(liferayFile.getTitle(), liferayFile);	
					}
					
				} catch (PortalException e) {
					e.printStackTrace();
				} catch (SystemException e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
	
	
	/* - START - Methods moved from AnnotationBacking, to have only one session bean.*/
	
	/** 
	 * Switches to the single annotation creation/editing view.
     * Handles interactionModel.mouseup event (via WAVEFORM3_mouseup() in JavaScript) for the single dygraph in the viewD_SingleLead.xhtml view.
     */
	public String viewAnnotationPoint(){

    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		
		String passedDataOnsetX = (String) map.get("DataOnsetX");
		String passedDataOnsetY = (String) map.get("DataOnsetY");

		
		sessionAnn = new AnnotationVO(Double.parseDouble(passedDataOnsetX), Double.parseDouble(passedDataOnsetY),
									  0, 0, null, "", "", "", true);
		
		this.getLog().info("+++ AnnotationBacking.java, viewAnnotationPoint() passedDataOnsetX: " + passedDataOnsetX + " passedDataSY: " + passedDataOnsetY + " +++ ");
		
		setShowFineGraph(false);
		setPreviousAnnotation(false);// this is an new annotation, allow editing.
		
		return "viewE_Annotate";
    }
	
    public String viewAnnotationInterval(){

    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		
		String passedDataOnsetX = (String) map.get("DataOnsetX");
		String passedDataOnsetY = (String) map.get("DataOnsetY");
		String passedDataOffsetX = (String) map.get("DataOffsetX");
		String passedDataOffsetY = (String) map.get("DataOffsetY");
		String passedDeltaX = (String) map.get("DeltaX");
		String passedDeltaY = (String) map.get("DeltaY");
		
		sessionAnn = new AnnotationVO(Double.parseDouble(passedDataOnsetX), Double.parseDouble(passedDataOnsetY),
									  Double.parseDouble(passedDataOffsetX), Double.parseDouble(passedDataOffsetY), 
									  null, "", "", "", false);

		this.getLog().info("+++ AnnotationBacking.java, viewAnnotationInterval() passedDataOnsetX:  " + passedDataOnsetX + "   passedDataOnsetY: " + passedDataOnsetY + " +++ ");
		this.getLog().info("+++ ++++++++++++++++++++++++++++++++++++++++++++++++ passedDataOffsetX: " + passedDataOffsetX + " passedDataOffsetY: " + passedDataOffsetY + " +++ ");
		this.getLog().info("+++ ++++++++++++++++++++++++++++++++++++++++++++++++ dataSXDuration:    " + sessionAnn.getDataXChange() + "  dataSYDuration: " + sessionAnn.getDataYChange() + " +++ ");
		
		setPreviousAnnotation(false);// this is an new annotation, allow editing.
		
		return "viewE_Annotate";
    }
    
    public String viewCurrentAnnotation(){
    	this.getLog().info("+++ AnnotationBacking.java, viewCurrentAnnotation() +++");
    	
    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		
		String passedAnnotationID = (String) map.get("annotationID");
		
		AnnotationDTO retrievedAnnotation = ConnectionFactory.createConnection().getAnnotationById(ResourceUtility.getCurrentUserId(), Long.valueOf(passedAnnotationID));
		
		sessionAnn = new AnnotationVO(retrievedAnnotation.getStartXcoord(), retrievedAnnotation.getStartYcoord(),
									  retrievedAnnotation.getEndXcoord(), retrievedAnnotation.getEndYcoord(), 
									  retrievedAnnotation.getBioportalID(), retrievedAnnotation.getName(), retrievedAnnotation.getValue(), 
									  retrievedAnnotation.getDescription(), retrievedAnnotation.isSinglePoint());
		
		setPreviousAnnotation(true);// this is an existing annotation, do not allow editing.

		return "viewE_Annotate";
    }
	   
    

	

	public boolean isShowFineGraph() {
		return showFineGraph;
	}

	/** Set to true to show fine tuning graph, to false for text details of new annotation.
	 *  
	 * @param showFineGraph
	 */
	public void setShowFineGraph(boolean showFineGraph) {
		this.showFineGraph = showFineGraph;
	}

	public boolean isPreviousAnnotation() {
		return previousAnnotation;
	}

	public void setPreviousAnnotation(boolean previousAnnotation) {
		this.previousAnnotation = previousAnnotation;
	}
	
	/* - END - Methods moved from AnnotationBacking, to have only one session bean.*/
    
    
	/** Creates the titles for the multi-lead graphs.
	 * 
	 * @param iaAnnCount
	 * @param iLeadCount
	 */
	public void setGraphTitle(int[][] iaAnnCount, int iLeadCount){
		this.getLog().info("--- Entering function setGraphTitle()");
		ServerUtility util = new ServerUtility(false);

		saGraphTitle = new String[iLeadCount+1];
		for(int[] iaACnt: iaAnnCount){
			String sName = util.guessLeadName(iaACnt[0]-1, iLeadCount);
			if(iaACnt[1] == 0){
				saGraphTitle[iaACnt[0]-1] = sName; // don't mention count when there are zero annotations
			}else{
				saGraphTitle[iaACnt[0]-1] = sName + " (" +  iaACnt[1] + " annotations)";
			}
			this.getLog().debug(iaACnt[0]-1 + ":" + iaACnt[1] + ",");
		}		
		this.getLog().info("--- Exiting function setGraphTitle()");
	}
	
	public JSONArray getGraphTitle() {
		JSONArray jsonArrayTitle = new JSONArray();
		for(String title: saGraphTitle){
			jsonArrayTitle.put(title);
		}
		return jsonArrayTitle;
	}

	public String[] getArrayGraphTitle() {
		return saGraphTitle;
	}

	
	public String getSelectedLeadName() {
		return selectedLeadName;
	}
	public void setSelectedLeadName(String selectedLeadName) {
		this.selectedLeadName = selectedLeadName;
	}
	public String getNewMilliSec() {
		return newMilliSec;
	}

	public void setNewMilliSec(String newMilliSec) {
		this.newMilliSec = newMilliSec;
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
		this.sharedStudyEntry = sharedStudyEntry;
	}
	public boolean isGraphMultipleVisible() {
		return graphMultipleVisible;
	}
	public void setGraphMultipleVisible(boolean graphMultipleVisible) {
		this.graphMultipleVisible = graphMultipleVisible;
	}
    public String getLeadCount(){
    	return String.valueOf(getSharedStudyEntry().getLeadCount());
    }
    public String getSamplingRate(){
    	return String.valueOf(getSharedStudyEntry().getSamplingRate()) + "Hz";
    }
    public String getDurationSec(){
    	return (getSharedStudyEntry().getDurationSec() + " seconds");
    }
	public int getCurrentVisualizationOffset() {
		return currentVisualizationOffset;
	}
	public void setCurrentVisualizationOffset(int currentVisualizationOffset) {
		this.currentVisualizationOffset = currentVisualizationOffset;
	}
	public int getDurationMilliSeconds() {
		return durationMilliSeconds;
	}
	public void setDurationMilliSeconds(int durationMilliSeconds) {
		this.durationMilliSeconds = durationMilliSeconds;
	}
	public int getGraphWidthPixels() {
		return graphWidthPixels;
	}
	public void setGraphWidthPixels(int graphWidthPixels) {
		this.graphWidthPixels = graphWidthPixels;
	}
	public JSONObject getData() {
		return data;
	}
	public void setData(JSONObject dataJson) {
		this.data = dataJson;
	}
	public int getVisualizationWidthMS() {
		return visualizationWidthMS;
	}
	public void setVisualizationWidthMS(int visualizationWidthMS) {
		this.visualizationWidthMS = visualizationWidthMS;
	}

	public int getAnnotationCount() {
		return annotationCount;
	}

	public void setAnnotationCount(int annotationCount) {
		this.annotationCount = annotationCount;
	}

	public AnnotationVO getSessionAnn() {
		return sessionAnn;
	}

}
