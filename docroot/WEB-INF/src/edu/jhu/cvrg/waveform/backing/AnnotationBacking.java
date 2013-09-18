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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
//import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

//import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;

import com.liferay.portal.model.User;

import edu.jhu.cvrg.waveform.utility.AnnotationUtility;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.model.AnnotationData;
import edu.jhu.cvrg.waveform.model.FileTree;
import edu.jhu.cvrg.waveform.model.StudyEntry;
//import edu.jhu.cvrg.waveform.model.UserModel;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

@SessionScoped
@ManagedBean(name="annotationBacking")
public class AnnotationBacking implements Serializable {

	private static final long serialVersionUID = -6719393176698520013L;

		public String nodeID;	
//    	public String dataSXstring; 
//    	public String dataSYstring;
		public double dataXChange; 
		public double dataYChange; 
		public double dataOnsetX; // X value (time) of a point or the Start of an interval
		public double dataOnsetY; // Y value (Voltage) of a point or the Start of an interval
		public double dataOffsetX;
		public double dataOffsetY;
		public double valueofSX;
		public double valueofSY;
		public String fullAnnotation;
		private String leadName;
		public int leadnum;
		public String firstname;
        public String lastname;
        public int lastnum;
        public String portalDefinitionName;
        public String termName;
        public Integer age;
        private String street;
        private String city;
        private String postalCode;
        private String info;
        private String email;
        private String phone;
        private User userLifeRayModel;
        private int annotationCount=0;
        private boolean newInstance=true;
        //        @ManagedProperty("#{userModel}")
//        private UserModel userModel;
        
//    	@ManagedProperty("#{visualizeBacking.selectedStudyObject}")
//    	private StudyEntry studyEntry;
    	@ManagedProperty("#{visualizeSharedBacking}")
    	private VisualizeSharedBacking visualizeSharedBacking;   

//   public void populateRythStrip(){
//	   Ajax.oncomplete("calledOnLoadData()");
//	   Ajax.oncomplete("alert('leadOnloadCallback: Hello from the Backing Bean')");
//
//	}   	

//    public String getLeadnameFromRender(){
//    		
//    		// Ajax.oncomplete("alert('leadOnloadCallback: Hello from the Backing Bean')");
//			   String value = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("leadNamesend");
//			   System.out.println("getLeadnameFromRender(), Onload Callback LeadName: " + getLeadName());
//			   System.out.println("getLeadnameFromRender(), Onload Callback lastnum: " + getLastnum());
//			   System.out.println(value);  
//			   return value;
//	}

//  RSA 04/26/13 this is called using a remoteCommand in  gerhythmstrip.xhtml - to initilaize the leadname and Leadnum in the Backing bean.
   
//	public void leadOnloadCallbackTemp() {
//        System.out.println("AnnotationBacking.leadOnloadCallbackTemp() LeadName: " + getLeadName());
//        System.out.println("AnnotationBacking.leadOnloadCallbackTemp() LeadNumber: " + getLeadnum());
////        RequestContext context = RequestContext.getCurrentInstance();
////       // showAnnotations(context, RetrieveECGDatabase);  
//    }


//	public void leadOnloadCallback() {
//		System.out.println("AnnotationBacking.leadOnloadCallback()");
////		Ajax.oncomplete("alert('AnnotationBacking.leadOnloadCallback: called by singleLead.xhtml line 41')");
//	}
	
    	public void initialize(ComponentSystemEvent event) {
        	System.out.println("*************** AnnotationBacking.java, initialize() **********************");
        	
    		
			if (newInstance) {
    			System.out.println("***  New instance ****");
    			showAnnotationForLead();
    		}else{
    			System.out.println("***  Existing instance, annotationCount:" + annotationCount + " Lead Name:" + leadName + " ****");
    		}
    		newInstance = false;
    	}
        
    	
    	
	/** sets up and calls showAnnotations() which retrieves annotations for the given lead, and executes the JavaScript functions which clear and then set the annotations on the Dygraph.<BR/>
	 * The executed JavaScript includes:  CVRG_resetAnnotations(), CVRG_addAnnotationHeight(), CVRG_addAnnotationInterval(), CVRG_showAnnotations().
	 **/
	public void showAnnotationForLead(){
		// Ajax.oncomplete("alert('AnnotationBacking.showAnnotationForLead: ')");
		RequestContext context = RequestContext.getCurrentInstance();
		//AnnotationUtility RetrieveECGDatabase = new AnnotationUtility();
		AnnotationUtility RetrieveECGDatabase = new AnnotationUtility(com.liferay.util.portlet.PortletProps.get("dbUser"),
                com.liferay.util.portlet.PortletProps.get("dbPassword"),
                com.liferay.util.portlet.PortletProps.get("dbURI"),     
                com.liferay.util.portlet.PortletProps.get("dbDriver"),
                com.liferay.util.portlet.PortletProps.get("dbMainDatabase"));
		
		setLeadName(visualizeSharedBacking.getSelectedLeadName());
		setLeadnum(Integer.parseInt(visualizeSharedBacking.getSelectedLeadNumber()));
		
        System.out.println("AnnotationBacking.showAnnotationForLead LeadName: " + getLeadName());
        System.out.println("AnnotationBacking.showAnnotationForLead Leadnum: " + getLeadnum());
        showAnnotations(context, RetrieveECGDatabase);  
	}
	
    /** Switches to the selection tree and list view.
     * Handles onclick event for the button "btnView12LeadECG" in the viewA_SelectionTree.xhtml view.
     * 
     */
    public String viewAnnotationPoint(){
    	System.out.println("+++ AnnotationBacking.java +++");
    	FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> map = context.getExternalContext().getRequestParameterMap();
		String passedDataOnsetX = (String) map.get("sDataOnsetX");
		String passedDataOnsetY = (String) map.get("sDataOnsetY");
		
		setDataSX(Double.parseDouble(passedDataOnsetX));
		setDataSY(Double.parseDouble(passedDataOnsetY));

		setDataOffsetX(0);
		setDataOffsetY(0);
		setDeltaX(0);
		setDeltaY(0);
		
		System.out.println("+++ AnnotationBacking.java, viewAnnotationPoint() passedDataOnsetX: " + passedDataOnsetX + " passedDataSY: " + passedDataOnsetY + " +++ ");
		return "viewE_Annotate";
    }
	
    
    public String viewAnnotationInterval(){
    	System.out.println("+++ AnnotationBacking.java, viewAnnotationIntervalEdit() +++");
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
		
		System.out.println("+++ AnnotationBacking.java, viewAnnotationIntervalEdit() passedDataOnsetX:  " + passedDataOnsetX + "   passedDataOnsetY: " + passedDataOnsetY + " +++ ");
		System.out.println("+++ ++++++++++++++++++++++++++++++++++++++++++++++++++++ passedDataOffsetX: " + passedDataOffsetX + " passedDataOffsetY: " + passedDataOffsetY + " +++ ");
		System.out.println("+++ ++++++++++++++++++++++++++++++++++++++++++++++++++++ dataSXDuration:    " + getDataSXDuration() + "  dataSYDuration: " + getDataSYDuration() + " +++ ");
		System.out.println("+++ ++++++++++++++++++++++++++++++++++++++++++++++++++++ getDataOffsetX: " + getDataOffsetX() + " getDataOffsetY: " + getDataOffsetY() + " +++ ");

		return "viewE_Annotate";
    }
    
    
	public void showNodeID(){
		String[] saOntDetail =  WebServiceUtility.lookupOntologyDefinition(this.getNodeID()); // ECGTermsv1:ECG_000000103 
		String sDefinition= saOntDetail[1];
		// String sDefinition=lookupOntologyDefinition("ECGTermsv1:ECG_000000103");
		
		setFullAnnotation(sDefinition);
		System.out.println("*** showNodeID(), nodeID: \"" + getNodeID() + "\"");
		System.out.println("*** showNodeID(), FullAnnotation: \"" + getFullAnnotation()  + "\"");
	     
	     	Map<String, Object> data = new HashMap<String, Object>();
	     	String dataFullAnnotation = getFullAnnotation();
	        
	     	data.put("*", dataFullAnnotation);
//	        Ajax.data(data);
//	        Ajax.oncomplete("showData()");
//	    //  Ajax.oncomplete("showDataVal()");
	        
	}
	
	
	
	/**Sets the dygraphs flag inplace carrys the x and y info and fills these values with the user input data for each lead annotation.
	 *   termName, getLeadName, dataSY, dataSX, getFullAnnotation
	 */
		public void saveAnnotationSetFlag(ActionEvent actionEvent) {
			RequestContext context = RequestContext.getCurrentInstance();
			userLifeRayModel = ResourceUtility.getCurrentUser();
			context.addCallbackParam("saved", true);    //basic parameter
	   //   context.addCallbackParam("annotationBackingBean", annotationBacking);    //pojo as json
			
		//  callBack working
			System.out.println("saveAnnotationSetFlag(), LeadName: " + getLeadName());
			System.out.println("saveAnnotationSetFlag(), leadnum: " + getLeadnum());
			System.out.println("saveAnnotationSetFlag(), dataSY: " + getDataSY());
			System.out.println("saveAnnotationSetFlag(), dataSX: " + getDataSX());
			System.out.println("saveAnnotationSetFlag(), dataSYDuration : " + getDataSYDuration());
			System.out.println("saveAnnotationSetFlag(), dataSXDuration : " + getDataSXDuration());
			System.out.println("saveAnnotationSetFlag(), termName: " + getTermName());
			System.out.println("saveAnnotationSetFlag(), FullAnnotation: " + getFullAnnotation());
			System.out.println("saveAnnotationSetFlag(), nodeID: " + getNodeID());

		    AnnotationUtility RetrieveECGDatabase = new AnnotationUtility();
			
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
			
			AnnotationData annotationToInsert = new AnnotationData();

			String ms = String.valueOf(java.lang.System.currentTimeMillis());  // used for GUID
	/*		String series = "6"; // lead name
			double x = 996; // "t-coodinate"
			double y = 95; // y-coordinate
			String flagLabel = "flagLabel"; // onset label???
			String ontologyId = "QRS Offset";
			String fullAnnotation = "This is only a test";	*/
			
			annotationToInsert.setUserID(userLifeRayModel.getScreenName());
			
			annotationToInsert.setSubjectID(visualizeSharedBacking.getSharedStudyEntry().getSubjectID());
			annotationToInsert.setLeadName(getLeadName());
			annotationToInsert.setLeadIndex(getLeadnum());
			annotationToInsert.setDatasetName(visualizeSharedBacking.getSharedStudyEntry().getRecordName());
			annotationToInsert.setStudyID(visualizeSharedBacking.getSharedStudyEntry().getStudy());
			
			 annotationToInsert.setCreator("manual");
			 annotationToInsert.setConceptLabel(getTermName());
			 annotationToInsert.setConceptRestURL("");
			 annotationToInsert.setUniqueID(ms);
			 annotationToInsert.setOnsetLabel("Onset");
			 annotationToInsert.setOnsetRestURL("");
			 annotationToInsert.setMicroVoltStart(getDataSY());
			 annotationToInsert.setMilliSecondStart(getDataSX());
			 
			 annotationToInsert.setMicroVoltEnd(getDataSYDuration());
			 annotationToInsert.setMilliSecondEnd(getDataSXDuration());
			 
//Check to make sure that there is no case where the duration/offset values can be legitimately 0 
			 
			 if (getDataSYDuration() == 0 && getDataSXDuration() == 0) {
				 annotationToInsert.setIsSinglePoint(true);
			 }
			 else {
				 annotationToInsert.setIsSinglePoint(false);
			 }
			 annotationToInsert.setOffsetLabel("Offset");
			 annotationToInsert.setOffsetRestURL("");
			 annotationToInsert.setAnnotation(getFullAnnotation());
			 annotationToInsert.setUnit("");
			
//Inserting save to XML database
			 
			 boolean insertionSuccess = RetrieveECGDatabase.storeLeadAnnotationNode(annotationToInsert);
			 
			 if(!insertionSuccess) {

//add facesmessage
				 
			 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Failure", "Annotation did not save properly"));
			        
			 }

			System.out.println("saveAnnotationSetFlag(), LeadName: " + getLeadName());
			System.out.println("saveAnnotationSetFlag(), dataSY: " + getDataSY());
			System.out.println("saveAnnotationSetFlag(), dataSX: " + getDataSX());
			System.out.println("saveAnnotationSetFlag(), termName: " + getTermName());
			System.out.println("saveAnnotationSetFlag(), FullAnnotation: " + getFullAnnotation());
			System.out.println("saveAnnotationSetFlag(), nodeID: " + getNodeID());
			
			showAnnotations(context, RetrieveECGDatabase); // clears current annotations and reloads all, including the new one.
			context.execute("WAVEFORM_ShowAnnotationSingle()"); // need to redisplay all the annotations.
		}
	

		/** Retrieves annotations for the given lead, and executes the JavaScript functions which clear and then set the annotations on the Dygraph.<BR/>
		 * The executed JavaScript includes:  CVRG_resetAnnotations(), CVRG_addAnnotationHeight(), CVRG_addAnnotationInterval(), CVRG_showAnnotations().
		 * @param context
		 * @param RetrieveECGDatabase
		 */
		private void showAnnotations(RequestContext context, AnnotationUtility RetrieveECGDatabase) {
			userLifeRayModel = ResourceUtility.getCurrentUser();
			String sErrorMess = "";
			//    		int leadIndex = AnnotationData.getLeadIndexStatic(getLeadName());
			int leadIndex = getLeadnum();
			if(leadIndex == -1) sErrorMess += "lead index is invalid | ";
			if(userLifeRayModel ==  null) sErrorMess += "userLifeRayModel is invalid | ";
			if(visualizeSharedBacking.getSharedStudyEntry() ==  null) sErrorMess += "visualizeSharedBacking.getSharedStudyEntry() is invalid | ";

			if(sErrorMess.length() > 0){
				System.err.println ("AnnotationBacking.java, showAnnotations() failed: " + sErrorMess);
			}else{
				AnnotationData[] retrievedAnnotationList = RetrieveECGDatabase.getLeadAnnotationNode(userLifeRayModel.getScreenName(), visualizeSharedBacking.getSharedStudyEntry().getStudy(), visualizeSharedBacking.getSharedStudyEntry().getSubjectID(), String.valueOf(leadIndex), leadIndex, visualizeSharedBacking.getSharedStudyEntry().getRecordName());
				annotationCount = retrievedAnnotationList.length;
				context.execute("CVRG_resetAnnotations()");

				Arrays.sort(retrievedAnnotationList);

				String series = getLeadName();
				long x = 0;
				double y = -999;
				String flagLabel; //e.g. = "1";
				String ontologyId; //e.g. = "Amplitude";
				String fullAnnotation;//


				HashMap<Double, Integer> duplicates = new HashMap<Double, Integer>();

				for( int i = 0; i < retrievedAnnotationList.length; i++ ){
					//Test JAVA data for CVRG_addAnnotation RSA 0117		
					// series = RetrievedAnnotations[i].getLeadName(1);
					x = (long) retrievedAnnotationList[i].getMilliSecondStart();     // time or "X" coordinate 
					y = retrievedAnnotationList[i].getMicroVoltStart();       //voltage or "Y" coordinate, not needed for dygraph annotation flag, but might be used by our code later.
					flagLabel = String.valueOf(i+1);   // label of Annotation
					ontologyId = retrievedAnnotationList[i].getConceptLabel();
					fullAnnotation = retrievedAnnotationList[i].getAnnotation();// "fullAnnotation needs to be setup in UserControloler and RetrieveAnnotation";
					System.out.println("RetrieveAnnotation loop x:" + x + " y:" + y + " flagLabel: " + flagLabel);

					Double xPosition = Double.valueOf(x);
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

					if(retrievedAnnotationList[i].getIsSinglePoint()) {
						int finalHeight = heightMultiplier * 15;
						System.out.println("-- Add single point, finalHeight:" + x);
						// add annotaion from JAVA to JavaScript Dygraph 
						context.execute("CVRG_addAnnotationHeight('" + series + "' , '" +  x + "', '" +  y + "','" +   flagLabel + "','" + ontologyId + "','" + fullAnnotation + "',' " + finalHeight + "')");
					}
					else {
						int finalHeight = heightMultiplier * -50;
						String flagLabelFirst = flagLabel + " >";
						String flagLabelLast = "< " + flagLabel;
						String flagLabelCenter  = flagLabel + " Interval";
						int width = 30;
						int widthInterval = 66;
						double secondX = retrievedAnnotationList[i].getMilliSecondEnd();
						double secondY = retrievedAnnotationList[i].getMicroVoltEnd();

						//TODO: corrected for Display at low resolution. RSA 04/15 works for 1000 samples per second.
						int centerX = (int) (( x + secondX ) / 2); 
						centerX = centerX / 10;
						centerX = centerX * 10;

						int YcenterY = (int) (( x + secondX ) / 2); 
						double centerArea = ( secondX - x );

						double toCenterWithArea = ( centerArea + x );

						System.out.println("x " + x);

						// START add annotaion from JAVA to JavaScript Dygraph 
						context.execute("CVRG_addAnnotationInterval('" + series + "' , '" +  x + "', '" +  y + "','" +   flagLabelFirst + "','" + ontologyId + "','" + fullAnnotation + "',' " + finalHeight + "','" + width + "')");
						System.out.println( "centerX" +  centerX);

						//  Sets the center flag
						context.execute("CVRG_addAnnotationInterval('" + series + "' , '" +  centerX + "', '" +  YcenterY + "','" +  flagLabelCenter  + "','" + ontologyId + "','" + fullAnnotation + "',' " + finalHeight + "','" + widthInterval + "')");
						System.out.println( "secondX" + secondX);

						// END X
						context.execute("CVRG_addAnnotationInterval('" + series + "' , '" +  secondX + "', '" +  secondY + "','" +   flagLabelLast + "','" + ontologyId + "','" + fullAnnotation + "',' " + finalHeight + "','" + width + "')");

						// sets the highlight
						context.execute("CVRG_setHightLightLocation('" +  x + "','" +  toCenterWithArea + "', '" +  secondX + "')");
					}

					//add facesmessage
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Success", "Success"));
				}

//				context.execute("CVRG_showAnnotations()");
//				context.execute("WAVEFORM_ShowAnnotationSingle()");
			}
		}

			
//    	public UserModel getUserModel() {
//			return userModel;
//		}
//
//		public void setUserModel(UserModel userModel) {
//			this.userModel = userModel;
//		}

		public User getUserLifeRayModel() {
			return userLifeRayModel;
		}

		public void setUserLifeRayModel(User userLifeRayModel) {
			this.userLifeRayModel = userLifeRayModel;
		}

		public StudyEntry getStudyEntry() {
			return visualizeSharedBacking.getSharedStudyEntry();
		}

		public void setStudyEntry(StudyEntry studyEntry) {
			this.visualizeSharedBacking.setSharedStudyEntry(studyEntry);
		}

        public VisualizeSharedBacking getVisualizeSharedBacking() {
			return visualizeSharedBacking;
		}

		public void setVisualizeSharedBacking(
				VisualizeSharedBacking visualizeSharedBacking) {
			this.visualizeSharedBacking = visualizeSharedBacking;
		}

		public String getFirstname() {
                return firstname;
        }

        public void setFirstname(String firstname) {
                this.firstname = firstname;
        }

        public String getLastname() {
                return lastname;
        }

        public void setLastname(String lastname) {
                this.lastname = lastname;
        }

        public Integer getAge() {
                return age;
        }

        public void setAge(Integer age) {
                this.age = age;
        }

        public String getStreet() {
                return street;
        }

        public void setStreet(String street) {
                this.street = street;
        }

        public String getCity() {
                return city;
        }

        public void setCity(String city) {
                this.city = city;
        }

        public String getPostalCode() {
                return postalCode;
        }

        public void setPostalCode(String postalCode) {
                this.postalCode = postalCode;
        }

        public String getInfo() {
                return info;
        }

        public void setInfo(String info) {
                this.info = info;
        }
        
        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getPhone() {
                return phone;
        }

        public void setPhone(String phone) {
                this.phone = phone;
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

		public void setLeadName(String leadName) {
			System.out.println( "AnnotationBacking.setLeadName() called with:" + leadName);
			this.leadName = leadName;
		}

		public String getLeadName() {
			//System.out.println( "AnnotationBacking.getLeadName() called with:" + leadName);
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

//		public String getDataSXstring() {
//			return dataSXstring;
//		}
//
//		public void setDataSXstring(String dataSXstring) {
//			this.dataSXstring = dataSXstring;
//		}

//		public String getDataSYstring() {
//			return dataSYstring;
//		}
//
//		public void setDataSYstring(String dataSYstring) {
//			this.dataSYstring = dataSYstring;
//		}

		public String getNodeID() {
			return nodeID;
		}

		public void setNodeID(String nodeID) {
			this.nodeID = nodeID;
//			this.fullAnnotation = this.termName + " | " + this.nodeID + " DUMMY Definition";
		}

		private int getLastnum() {
			return lastnum;
		}

		private void setLastnum(int lastnum) {
			this.lastnum = lastnum;
		}

		public int getLeadnum() {
			//System.out.println( "AnnotationBacking.getLeadnum() called with:" + leadnum);
			return leadnum;
		}

		public void setLeadnum(int leadnum) {
			System.out.println( "AnnotationBacking.setLeadnum() called with:" + leadnum);
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
//			double ret;
//			if(dataSXDuration==0)
//				ret = 0;
//			else 
//				ret = dataSX+dataSXDuration;
//			
//			return ret;
		}
		public double getDataOffsetY() {
			return dataOffsetY;
//			double ret;
//			if(dataSYDuration==0)
//				ret = 0;
//			else 
//				ret = dataSY+dataSYDuration;
//			
//			return ret;
		}

}