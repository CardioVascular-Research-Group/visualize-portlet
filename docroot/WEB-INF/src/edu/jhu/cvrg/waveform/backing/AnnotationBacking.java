package edu.jhu.cvrg.waveform.backing;
/**
 * CONTROLLER of the MVC for the LogonScreen Migration SA 10/25.
 * 
 * @author salger2 aka Scott Alger 
 * 
 */
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
* @author Scott Alger 2013, Chris Jurado, Brandon Bennetz, Mike Shipway
* 
*/
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.dbapi.dto.AnnotationDTO;
import edu.jhu.cvrg.dbapi.dto.DocumentRecordDTO;
import edu.jhu.cvrg.dbapi.factory.ConnectionFactory;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

@ViewScoped
@ManagedBean(name="annotationBacking")
public class AnnotationBacking implements Serializable {

	private static final long serialVersionUID = -6719393176698520013L;

	public String nodeID; // equivalent to Concept ID (AnnotationData.ConceptID) e.g. "ECGOntology:ECG_000000243".
	public double dataXChange; 
	public double dataYChange; 
	public double dataOnsetX; // X value (time) of a point or the Start of an interval
	public double dataOnsetY; // Y value (Voltage) of a point or the Start of an interval
	public double dataOffsetX;
	public double dataOffsetY;
	public double valueofSX;
	public double valueofSY;
	public String fullAnnotation="full annotation";
	public String comment="";
	public int leadnum;
    public int lastnum;
    public String portalDefinitionName;
    public String termName;
    
    private String leadName;
    private User userLifeRayModel;
    private int annotationCount=0;
	private boolean singlePoint=true;
    private boolean previousAnnotation=false;
	private int flagCount = 0;
	private char cIntervalLabel = 'A';
	private boolean showFineGraph=false;

	@ManagedProperty("#{visualizeSharedBacking}")
	private VisualizeSharedBacking visualizeSharedBacking;   
	
	private static final Logger log = Logger.getLogger(AnnotationBacking.class);
	
	public void initialize(ComponentSystemEvent event) {
    	log.info("*************** AnnotationBacking.java, initialize() **********************");
    	showAnnotationForLead();
	}
        
    	
    	
	/** sets up and calls showAnnotations() which retrieves annotations for the given lead, and executes the JavaScript functions which clear and then set the annotations on the Dygraph.<BR/>
	 * The executed JavaScript includes:  CVRG_resetAnnotations(), CVRG_addAnnotationHeight(), CVRG_addAnnotationInterval().
	 **/
	@PostConstruct
	public void showAnnotationForLead(){
		log.info("***  New instance ****");
		RequestContext context = RequestContext.getCurrentInstance();
		
		setLeadName(visualizeSharedBacking.getSelectedLeadName());
		setLeadnum(Integer.parseInt(visualizeSharedBacking.getSelectedLeadNumber()));
		setComment(""); // make sure it is blank
		
        showAnnotations(context);  
	}
	
    /** Switches to the single annotation creation/editing view.
     * Handles interactionModel.mouseup event (via WAVEFORM3_mouseup() in JavaScript) for the single dygraph in the viewD_SingleLead.xhtml view.
     */
	   public String viewAnnotationPoint(){

	    	FacesContext context = FacesContext.getCurrentInstance();
			Map<String, String> map = context.getExternalContext().getRequestParameterMap();
			
			String passedDataOnsetX = (String) map.get("DataOnsetX");
			String passedDataOnsetY = (String) map.get("DataOnsetY");

			setDataSX(Double.parseDouble(passedDataOnsetX));
			setDataSY(Double.parseDouble(passedDataOnsetY));

			setDataOffsetX(0);
			setDataOffsetY(0);
			
			setDeltaX(0);
			setDeltaY(0);
			
			setSinglePoint(true);

			showNewAnnotationForLead();
			log.info("+++ AnnotationBacking.java, viewAnnotationPoint() passedDataOnsetX: " + passedDataOnsetX + " passedDataSY: " + passedDataOnsetY + " +++ ");
			setTermName("");
			setFullAnnotation("");
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
		
		setDataSX(Double.parseDouble(passedDataOnsetX));
		setDataSY(Double.parseDouble(passedDataOnsetY));
		
		setDataOffsetX(Double.parseDouble(passedDataOffsetX));
		setDataOffsetY(Double.parseDouble(passedDataOffsetY));

		setDeltaX(Double.parseDouble(passedDeltaX));
		setDeltaY(Double.parseDouble(passedDeltaY));
		
		setSinglePoint(false);
		
		log.info("+++ AnnotationBacking.java, viewAnnotationInterval() passedDataOnsetX:  " + passedDataOnsetX + "   passedDataOnsetY: " + passedDataOnsetY + " +++ ");
		log.info("+++ ++++++++++++++++++++++++++++++++++++++++++++++++ passedDataOffsetX: " + passedDataOffsetX + " passedDataOffsetY: " + passedDataOffsetY + " +++ ");
		log.info("+++ ++++++++++++++++++++++++++++++++++++++++++++++++ dataSXDuration:    " + getDataSXDuration() + "  dataSYDuration: " + getDataSYDuration() + " +++ ");
		
		setTermName("");
		setFullAnnotation("");
		showNewAnnotationForLead();
		setPreviousAnnotation(false);// this is an new annotation, allow editing.
		return "viewE_Annotate";
    }

    public void updateAnnotationInterval(){

    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		String passedDataOnsetX = (String) map.get("DataOnsetX");
		String passedDataOnsetY = (String) map.get("DataOnsetY");
		String passedDataOffsetX = (String) map.get("DataOffsetX");
		String passedDataOffsetY = (String) map.get("DataOffsetY");
		String passedDeltaX = (String) map.get("DeltaX");
		String passedDeltaY = (String) map.get("DeltaY");
		
		setDataSX(Double.parseDouble(passedDataOnsetX));
		setDataSY(Double.parseDouble(passedDataOnsetY));
		
		setDataOffsetX(Double.parseDouble(passedDataOffsetX));
		setDataOffsetY(Double.parseDouble(passedDataOffsetY));

		setDeltaX(Double.parseDouble(passedDeltaX));
		setDeltaY(Double.parseDouble(passedDeltaY));
		
		setSinglePoint(false);
		
		log.info("+++ AnnotationBacking.java, viewAnnotationInterval() passedDataOnsetX:  " + passedDataOnsetX + "   passedDataOnsetY: " + passedDataOnsetY + " +++ ");
		log.info("+++ ++++++++++++++++++++++++++++++++++++++++++++++++ passedDataOffsetX: " + passedDataOffsetX + " passedDataOffsetY: " + passedDataOffsetY + " +++ ");
		log.info("+++ ++++++++++++++++++++++++++++++++++++++++++++++++ dataSXDuration:    " + getDataSXDuration() + "  dataSYDuration: " + getDataSYDuration() + " +++ ");
		
		showNewAnnotationForLead();
    }
    
    public String viewCurrentAnnotation(){
    	log.info("+++ AnnotationBacking.java, viewCurrentAnnotation() +++");
    	double DeltaX, DeltaY;
    	
    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		
		String passedAnnotationID = (String) map.get("annotationID");
		
		AnnotationDTO retrievedAnnotation = ConnectionFactory.createConnection().getAnnotationById(userLifeRayModel.getUserId(), Long.valueOf(passedAnnotationID));
		
		setDataSX(retrievedAnnotation.getStartXcoord());
		setDataSY(retrievedAnnotation.getStartYcoord());

		setDataOffsetX(retrievedAnnotation.getEndXcoord());
		setDataOffsetY(retrievedAnnotation.getEndYcoord());
		
		if(retrievedAnnotation.isSinglePoint()){
			DeltaX=0;
			DeltaY=0;
		}else{
			DeltaX=getDataOffsetX()-getDataSX();
			DeltaY=getDataOffsetY()-getDataSY();
		}

		setDeltaX(DeltaX);
		setDeltaY(DeltaY);
		
		setSinglePoint(retrievedAnnotation.isSinglePoint());
		setTermName(retrievedAnnotation.getName());
		setFullAnnotation(retrievedAnnotation.getValue());
		setComment(retrievedAnnotation.getDescription());
		setNodeID(retrievedAnnotation.getBioportalID());
		setPreviousAnnotation(true);// this is an existing annotation, do not allow editing.

		return "viewE_Annotate";
    }
	
    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewSingleGraph(){
    	log.info("*** AnnotationBack.java, viewSingleGraph() ***");
		return "viewD_SingleLead";
    }


	public void showNodeID(){
		String[] saOntDetail =  WebServiceUtility.lookupOntologyDefinition(this.getNodeID()); // ECGTermsv1:ECG_000000103 
		String sDefinition= saOntDetail[1];
		
		setFullAnnotation(sDefinition);
		log.info("*** showNodeID(), nodeID: \"" + getNodeID() + "\"");
		log.info("*** showNodeID(), FullAnnotation: \"" + getFullAnnotation()  + "\"");
	     
     	Map<String, Object> data = new HashMap<String, Object>();
     	String dataFullAnnotation = getFullAnnotation();
        
     	data.put("*", dataFullAnnotation);
	}
	
	public void lookupDefinition(){
		log.info("*** AnnotationBackup.lookupDefinition() ***");
    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		
		String passedNodeID = (String) map.get("nodeID");
		String passedNodeName = (String) map.get("nodeName");

		setNodeID(passedNodeID); // e.g. "ECGTermsv1:ECG_000000460"
		setTermName(passedNodeName); // e.g. "R_Peak"

		
		String[] saOntDetail =  WebServiceUtility.lookupOntologyDefinition(this.getNodeID()); // ECGTermsv1:ECG_000000103 
		String sDefinition= saOntDetail[1];
		
		setFullAnnotation(sDefinition); // e.g. "The peak of the R Wave."
		log.info("*** -- nodeID: \"" + getNodeID() + "\"");
		log.info("*** -- FullAnnotation: \"" + getFullAnnotation()  + "\"");
	     
	}
	
	
	/** Sets the dygraphs flag inplace carrys the x and y info and fills these values with the user input data for each lead annotation.
	 *   termName, getLeadName, dataSY, dataSX, getFullAnnotation
	 */
	public void saveAnnotationSetFlag(ActionEvent actionEvent) {
		RequestContext context = RequestContext.getCurrentInstance();
		userLifeRayModel = ResourceUtility.getCurrentUser();
		context.addCallbackParam("saved", true);    //basic parameter
		
		//  callBack working
		log.debug("saveAnnotationSetFlag(), SinglePoint: " + isSinglePoint());
		log.debug("saveAnnotationSetFlag(), LeadName: " + getLeadName());
		log.debug("saveAnnotationSetFlag(), leadnum: " + getLeadnum());
		log.debug("saveAnnotationSetFlag(), dataSY: " + getDataSY());
		log.debug("saveAnnotationSetFlag(), dataSX: " + getDataSX());
		log.debug("saveAnnotationSetFlag(), DataOffsetX: " + getDataOffsetX());
		log.debug("saveAnnotationSetFlag(), DataOffsetY: " + getDataOffsetY());			
		log.debug("saveAnnotationSetFlag(), dataSYDuration : " + getDataSYDuration());
		log.debug("saveAnnotationSetFlag(), dataSXDuration : " + getDataSXDuration());
		log.debug("saveAnnotationSetFlag(), termName: " + getTermName());
		log.debug("saveAnnotationSetFlag(), FullAnnotation: " + getFullAnnotation());
		log.debug("saveAnnotationSetFlag(), nodeID: " + getNodeID());

//			 * Required values that need to be filled in are:
//				 * 
//				 * created by (x) - the source of this annotation (whether it came from an algorithm or was entered manually)
//				 * concept label - the type of annotation as defined in the annotation's bioportal reference term
//				 * annotation ID - a unique ID used for easy retrieval of the annotation in the database
//				 * onset label - the bioportal reference term for the onset position.  This indicates the start point of an interval
//				 * 					or the location of a single point
//				 * onset y-coordinate - the y coordinate for that point on the ECG wave
//				 * onset t-coordinate - the t coordinate for that point on the ECG wave.
//				 * an "isInterval" boolean - for determining whether this is an interval (and thus needs an offset tag)
//		    	 * Full text description - This is the "value" so to speak, and contains the full definition of the annotation type being used
//				 * 
//				 * Note:  If this is an interval, then an offset label, y-coordinate, and t-coordinate are required for that as well.
		
					
		AnnotationDTO ann = new AnnotationDTO(ResourceUtility.getCurrentUserId(), ResourceUtility.getCurrentGroupId(), ResourceUtility.getCurrentCompanyId(),
				 							   visualizeSharedBacking.getSharedStudyEntry().getDocumentRecordId(), "manual", "ANNOTATION", getTermName(), null /*bioportalID*/, "",
				 							   getLeadnum(), "", getComment(), getFullAnnotation(), Calendar.getInstance(),null, null, null, null, 
				 							   null, visualizeSharedBacking.getSharedStudyEntry().getRecordName(), visualizeSharedBacking.getSharedStudyEntry().getSubjectId());
		 
		 if(isSinglePoint()){
			 ann.setStartXcoord(getDataSX());
			 ann.setStartYcoord(getDataSY());
			 ann.setEndXcoord(getDataSX());
			 ann.setEndYcoord(getDataSY());
		 }else{
			 ann.setStartXcoord(getDataSX());
			 ann.setStartYcoord(getDataSY());
			 ann.setEndXcoord(getDataOffsetX());
			 ann.setEndYcoord(getDataOffsetY());
		 }
		 
		 
		 Long annotationId = ConnectionFactory.createConnection().storeAnnotation(ann);
		 
		 if(annotationId == null) {
			 //add facesmessage
			 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failure", "Annotation did not save properly"));
		 }

		 log.info("saveAnnotationSetFlag(), Complete.");
		
		showAnnotations(context); // clears current annotations and reloads all, including the new one.
		context.execute("SINGLELEAD_ShowAnnotationSingle()"); // need to redisplay all the annotations.
	}


	/** Retrieves annotations for the given lead, and executes the JavaScript functions which clear and then set the annotations on the Dygraph.<BR/>
	 * The executed JavaScript includes:  CVRG_resetAnnotations(), CVRG_addAnnotationHeight(), CVRG_addAnnotationInterval().
	 * @param context
	 * @param RetrieveECGDatabase
	 */
	private void showAnnotations(RequestContext context) {
		userLifeRayModel = ResourceUtility.getCurrentUser();
		String sErrorMess = "";

		int leadIndex = getLeadnum();
		if(leadIndex == -1) sErrorMess += "lead index is invalid | ";
		if(userLifeRayModel ==  null) sErrorMess += "userLifeRayModel is invalid | ";
		if(visualizeSharedBacking.getSharedStudyEntry() ==  null) sErrorMess += "visualizeSharedBacking.getSharedStudyEntry() is invalid | ";

		if(sErrorMess.length() > 0){
			log.error("AnnotationBacking.java, showAnnotations() failed: " + sErrorMess);
		}else{
			List<AnnotationDTO> retrievedAnnotationList = ConnectionFactory.createConnection().getLeadAnnotationNode(userLifeRayModel.getUserId(), visualizeSharedBacking.getSharedStudyEntry().getDocumentRecordId(), leadIndex);

			if(retrievedAnnotationList != null){
				annotationCount = retrievedAnnotationList.size();
			}else{
				annotationCount = 0;
			}
			context.execute("CVRG_resetAnnotations()");
			context.execute("WAVEFORM_clearHighLightQueue()");
			
			flagCount = 0;
			cIntervalLabel = 'A';
			
			if(retrievedAnnotationList != null){
				String series = getLeadName();
				HashMap<Double, Integer> duplicates = new HashMap<Double, Integer>();
				
				for (AnnotationDTO annotationDTO : retrievedAnnotationList) {
					if(!annotationDTO.isWholeLead()){
						duplicates = addAnnotation(context, series, annotationDTO, duplicates);	
					}
				}
			}
		}
	}

	
	/** Retrieves annotations for the given lead, and executes the JavaScript functions which clear and then set the annotations on the Dygraph.<BR/>
	 * The executed JavaScript includes:  CVRG_resetAnnotations(), CVRG_addAnnotationHeight(), CVRG_addAnnotationInterval().
	 * @param context
	 * @param RetrieveECGDatabase
	 */
	public void showNewAnnotationForLead(){
		log.info("### showNewAnnotationForLead ###");
		RequestContext context = RequestContext.getCurrentInstance();
		userLifeRayModel = ResourceUtility.getCurrentUser();
		String sErrorMess = "";

		int leadIndex = getLeadnum();
		if(leadIndex == -1) sErrorMess += "lead index is invalid | ";
		if(userLifeRayModel ==  null) sErrorMess += "userLifeRayModel is invalid | ";
		if(visualizeSharedBacking.getSharedStudyEntry() ==  null) sErrorMess += "visualizeSharedBacking.getSharedStudyEntry() is invalid | ";

		if(sErrorMess.length() > 0){
			log.error("AnnotationBacking.java, showNewAnnotation() failed: " + sErrorMess);
		}else{
			AnnotationDTO newAnnotation = new AnnotationDTO();
			newAnnotation.setStartXcoord(getDataSX());
			newAnnotation.setStartYcoord(getDataSY());
			newAnnotation.setValue("Fine-tuning Annotation");
			newAnnotation.setName("Editing");

			if(!isSinglePoint()){
				newAnnotation.setEndXcoord(getDataOffsetX());
				newAnnotation.setEndYcoord(getDataOffsetY());
			}else{
				newAnnotation.setEndXcoord(getDataSX());
				newAnnotation.setEndYcoord(getDataSY());
			}
			log.debug("### ### isSinglePoint: " + newAnnotation.isComment() 
						+ " getDataOffsetX(): " + newAnnotation.getEndXcoord()
						+ " getDataOffsetY(): " + newAnnotation.getEndYcoord());
			

			context.execute("CVRG_resetAnnotations()");
			context.execute("WAVEFORM_clearHighLightQueue()");

			String series = getLeadName();


			HashMap<Double, Integer> duplicates = new HashMap<Double, Integer>();
			flagCount = 0;
			cIntervalLabel = 'A';

			duplicates = addAnnotation(context, series, newAnnotation, duplicates);
		}
	}

	public void showNewAnnotationGraph(){
		log.info("*** showNewAnnotationGraph()");
		setShowFineGraph(true);
	}
	public void showNewAnnotationDetails(){
		log.info("*** showNewAnnotationDetails()");
		setShowFineGraph(false);
	}

	/** Adds a single annotation to the javascript array "tempAnnotations", to be added to the single lead graph after the view page finishes loading.
	 *  The executed JavaScript functions include: CVRG_addAnnotationHeight(), CVRG_addAnnotationInterval().

	 * @param context
	 * @param series
	 * @param singleAnnotation
	 * @param duplicates
	 * @return
	 */
	private HashMap<Double, Integer>  addAnnotation(RequestContext context, String series, AnnotationDTO singleAnnotation, 
													HashMap<Double, Integer> duplicates){
		long firstX;
		long firstY = -999;
		String flagLabel; //e.g. = "1";
		String ontologyId; //e.g. = "Amplitude";
		String fullAnnotation;//
		Long annotationID;
		log.debug("### ### addAnnotation() -- isSinglePoint: " + singleAnnotation.isSinglePoint() 
				+ " isComment: " + singleAnnotation.isComment()
				+ " getDataOffsetX(): " + singleAnnotation.getEndXcoord()
				+ " getDataOffsetY(): " + singleAnnotation.getEndYcoord());

		firstX = (long) singleAnnotation.getStartXcoord().longValue();	// time or "X" coordinate 
		firstY = (long) singleAnnotation.getStartYcoord().longValue();	//voltage or "Y" coordinate, not needed for dygraph annotation flag, but might be used by our code later.

		ontologyId = singleAnnotation.getName();
		fullAnnotation = singleAnnotation.getValue();
		setComment(singleAnnotation.getDescription());
		annotationID = singleAnnotation.getAnnotationId();

		Double xPosition = Double.valueOf(firstX);
		Integer numOccurances = Integer.valueOf(1);

		boolean duplicateCheck = duplicates.containsKey(xPosition);
		int heightMultiplier = 1;

		if(duplicateCheck) {
			numOccurances = duplicates.get(xPosition);
			heightMultiplier = (numOccurances.intValue()) + 1;
			duplicates.put(xPosition, Integer.valueOf(heightMultiplier));
		}
		else {
			duplicates.put(xPosition, Integer.valueOf(heightMultiplier));
		}

		if (fullAnnotation.length()>50){
			String truncatedFull = fullAnnotation.substring(0, 50);
			truncatedFull += "...";
			ontologyId += " - " + truncatedFull;
		}else{
			ontologyId += " - " + fullAnnotation;
		}
	
		if(singleAnnotation.isSinglePoint()) {
			if(firstX != (-999999999)){
				flagLabel = String.valueOf(flagCount+1);   // label of Annotation

				log.debug("Single point: " + flagLabel + " x: " + firstX);
				int finalHeight = heightMultiplier * 15;
				// 	add annotaion from JAVA to JavaScript Dygraph 
				context.execute("CVRG_addAnnotationHeight('" + series + "' , '" +  firstX + "', '" +  firstY + "','" 
					+ flagLabel + "','" + ontologyId + "','" + fullAnnotation + "/" + getComment() + "',' " 
					+ finalHeight + "','" + annotationID + "')");
				flagCount++;
			}
		} else {
			// get Alphabetic flag label, then increment letter.
			String sIntervallabel = String.valueOf(cIntervalLabel);
			cIntervalLabel =  (char) (cIntervalLabel+1);

			int finalHeight = heightMultiplier * -50;
			String flagLabelFirst = sIntervallabel + ">";
			String flagLabelLast = "<" + sIntervallabel;
			
			int width = 30;
			
			long secondX = (long) singleAnnotation.getEndXcoord().longValue();
			long secondY = (long) singleAnnotation.getEndYcoord().longValue();

			long centerX = (long) (( firstX + secondX ) / 2);
			long msSample = (long)(1000/visualizeSharedBacking.getSharedStudyEntry().getSamplingRate()); // milliseconds per sample
			long remainder = centerX % msSample;
			centerX = centerX - remainder;

			// START add annotaion from JAVA to JavaScript Dygraph 
			context.execute("CVRG_addAnnotationInterval('" + series + "' , '" +  firstX + "', '" +  firstY + "','" 
					+ flagLabelFirst + "','" + ontologyId + "','" + fullAnnotation + "',' " 
					+ finalHeight + "','" + width + "','" + annotationID + "')");

			// END X
			context.execute("CVRG_addAnnotationInterval('" + series + "' , '" +  secondX + "', '" +  secondY + "','" 
					+ flagLabelLast + "','" + ontologyId + "','" + fullAnnotation + "',' " 
					+ finalHeight + "','" + width + "','" + annotationID + "')");

			// sets the highlight
			context.execute("WAVEFORM_queueHighLightLocation('" +  firstX + "','" +  firstY + "','" +  finalHeight + "','" +  centerX + "', '" +  secondX + "')");
		}

		//add facesmessage
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Success"));
		
		return duplicates;
	}
	
	
	public User getUserLifeRayModel() {
		return userLifeRayModel;
	}

	public void setUserLifeRayModel(User userLifeRayModel) {
		this.userLifeRayModel = userLifeRayModel;
	}

	public DocumentRecordDTO getStudyEntry() {
		return visualizeSharedBacking.getSharedStudyEntry();
	}

	public void setStudyEntry(DocumentRecordDTO studyEntry) {
		this.visualizeSharedBacking.setSharedStudyEntry(studyEntry);
	}

    public VisualizeSharedBacking getVisualizeSharedBacking() {
		return visualizeSharedBacking;
	}

	public void setVisualizeSharedBacking(
			VisualizeSharedBacking visualizeSharedBacking) {
		this.visualizeSharedBacking = visualizeSharedBacking;
	}

	public String getLeadDescription() {
		String leadDescription = "Subject: "+ visualizeSharedBacking.getSharedStudyEntry().getRecordName()  
						+ " / Lead: \"" + leadName 
						+ "\" " + (leadnum+1) + 
						" of " + visualizeSharedBacking.getSharedStudyEntry().getLeadCount()
						+ " / Sampling-rate: " + visualizeSharedBacking.getSharedStudyEntry().getSamplingRate()
						+ "Hz / " + annotationCount + " total annotations." ;

		return leadDescription;
	}


	public String getPortalDefinitionName() {
		return portalDefinitionName;
	}

	public void setPortalDefinitionName(String portalDefinitionName) {
		this.portalDefinitionName = portalDefinitionName;
	}

	public String getTermName() {
		return termName;
	}

	public void setTermName(String termName) {
		this.termName = termName;
	}

	public void setFullAnnotation(String fullAnnotation) {
		this.fullAnnotation = fullAnnotation;
	}
	public String getFullAnnotation() {
		return fullAnnotation;
	}

	/** Comment on annotation, separate and in addition to the full annotation.
	 * Optional */
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}



	public void setLeadName(String leadName) {
		this.leadName = leadName;
	}

	public String getLeadName() {
		return leadName;
	}

	public double getValueofSX() {
		return valueofSX;
	}

	public void setValueofSX(double valueofSX) {
		this.valueofSX = valueofSX;
	}

	public double getValueofSY() {
		return valueofSY;
	}

	public void setValueofSY(double valueofSY) {
		this.valueofSY = valueofSY;
	}

	public double getDataSX() {
		return dataOnsetX;
	}

	public void setDataSX(double dataSX) {
		this.dataOnsetX = dataSX;
	}

	public double getDataSY() {
		return dataOnsetY;
	}

	public void setDataSY(double dataSY) {
		this.dataOnsetY = dataSY;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public int getLeadnum() {
		return leadnum;
	}

	public void setLeadnum(int leadnum) {
		this.leadnum = leadnum;
	}

	public void setDeltaX(double deltaX) {
		this.dataXChange = deltaX;
	}

	public void setDeltaY(double deltaY) {
		this.dataYChange = deltaY;
	}

	public double getDataSXDuration() {
		return dataXChange;
	}

	public double getDataSYDuration() {
		return dataYChange;
	}


	public void setDataOffsetX(double dataOffsetX) {
		this.dataOffsetX = dataOffsetX;
	}

	public void setDataOffsetY(double dataOffsetY) {
		this.dataOffsetY = dataOffsetY;
	}
	
	public double getDataOffsetX() {
		return dataOffsetX;
	}
	
	public double getDataOffsetY() {
		return dataOffsetY;
	}

	public boolean isSinglePoint() {
		return singlePoint;
	}
	public void setSinglePoint(boolean singlePoint) {
		this.singlePoint = singlePoint;
	}

    public int getAnnotationCount() {
		return annotationCount;
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

}