package edu.jhu.cvrg.waveform.main;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.axiom.om.OMElement;

import edu.jhu.cvrg.waveform.callbacks.SvcAxisCallback;
import edu.jhu.cvrg.waveform.model.VisualizationData;
import edu.jhu.cvrg.waveform.utility.ResourceUtility;
import edu.jhu.cvrg.waveform.utility.WebServiceUtility;

/** Contains functions which support the graphing of ECGs.
 * 
 * @author Michael Shipway
 */
public class VisualizationManager {

	private boolean verbose = false;
//	private AnalysisInProgress aIP;
//	private AnalysisUtility anUtil = new AnalysisUtility();
	public boolean make12LeadTestPattern=false;
	public VisualizationManager(boolean verbose){		 
		this.verbose = verbose;
//		aIP = new AnalysisInProgress();
	}


	/** Calls the collectWFDBdataSegment web service which:<BR>
	 * Reads the file from the file repository (using ftp) and returns a short segment of it as VisualizationData.
	 *
	 * @param userID - needed to look up annotations.
	 * @param subjectID - needed to look up annotations.
	 * @param fileName - file containing the ECG data in RDT format.
	 * @param fileSize - used to size the file reading buffer.
	 * @param offsetMilliSeconds - number of milliseconds from the beginning of the ECG at which to start the graph.
	 * @param durationMilliSeconds - The requested length of the returned data subset, in milliseconds.
	 * @param graphWidthPixels - Width of the zoomed graph in pixels(zoom factor*unzoomed width), hence the maximum points needed in the returned VisualizationData.
	 * @param callback - call back handler class.
	 * 	 
	 * @return a populated VisualizationData object or <B>null</B> on any web service failure
	 * 
	 * @see org.cvrgrid.ecgrid.client.BrokerService#fetchSubjectVisualization(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, long, int, int)
	 */
	public VisualizationData fetchSubjectVisualizationData(String userID, String subjectID, 
			String[] saFileNameList, long fileSize, 
			int offsetMilliSeconds, int durationMilliSeconds, 
			int graphWidthPixels, boolean bTestPattern) {

		String ftpURL = ResourceUtility.getFtpHost(); // web address of the data file repository 
		String fUser = ResourceUtility.getFtpUser(); // ftp userID
		String fPassword = ResourceUtility.getFtpPassword(); // ftp password
//		String mySqlURL; 
//		String fileNameList = "";
		int iFileCount = saFileNameList.length;
		VisualizationData visualizationData = null;
		
		//**** create the file subnodes of the fileNameList node. ************
		LinkedHashMap<String, String> fileMap = new LinkedHashMap<String, String>();
		int f=0;
		for(String fn:saFileNameList){
//			fileNameList += "\t\t<filename>" + fn + "</filename>\n";
			fileMap.put("fileName_" + f, fn);
			f++;
		}
		//************************************
		
		LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<String, Object>();

//		String serviceMethod = "collectWFDBdataSegment";		
//		String serviceName = "nodeDataService"; 
		
		String serviceMethod = "fetchWFDBdataSegmentType2";
		String serviceName = "waveformDataService"; 

		parameterMap.put("service", serviceMethod); 
		
//		parameterMap.put("brokerURL", ftpURL); // 
		parameterMap.put("ftpHost", ftpURL);
		parameterMap.put("ftpUser", fUser); // 
		parameterMap.put("ftpPassword", fPassword);
//		parameterMap.put("fileName", fileName);
		parameterMap.put("fileNameList", fileMap);
		parameterMap.put("fileCount", String.valueOf(iFileCount));
		parameterMap.put("parameterCount", "0");
		
		parameterMap.put("fileSize", String.valueOf(fileSize));
		parameterMap.put("offsetMilliSeconds", String.valueOf(offsetMilliSeconds));
		parameterMap.put("durationMilliSeconds", String.valueOf(durationMilliSeconds));
		parameterMap.put("graphWidthPixels", String.valueOf(graphWidthPixels));
		parameterMap.put("testPattern", String.valueOf(bTestPattern));
		parameterMap.put("verbose", String.valueOf(verbose));

		String serviceURL = ResourceUtility.getAnalysisServiceURL();
		SvcAxisCallback callback = null; // forces callWebService to return webservice results directly.
		
		OMElement omeWSReturn = WebServiceUtility.callWebServiceComplexParam(parameterMap, 
				serviceMethod, // Method of the service which implements the copy. e.g. "copyDataFilesToAnalysis"
				serviceName, // Name of the web service. e.g. "dataTransferService"
				serviceURL, // URL of the Analysis Web Service to send data files to. e.g. "http://icmv058.icm.jhu.edu:8080/axis2/services/";
				callback);
		
		//***************************************************
		try{
			if(omeWSReturn != null){
				Map<String, Object> paramMap = WebServiceUtility.buildParamMap(omeWSReturn);
				boolean isGoodData = ((String) paramMap.get("Status")).equals("success");
				short siLeadCount=0;
				String[] saChannelName;
				if(isGoodData){
					int iSampleCount = Integer.parseInt((String) paramMap.get("SampleCount"));
					siLeadCount = new Short((String)paramMap.get("LeadCount"));
					if((make12LeadTestPattern && bTestPattern)){
						siLeadCount = 12;
					}else{
						siLeadCount = Short.parseShort((String)paramMap.get("LeadCount"));
					}
					int iSegmentOffset = new Integer( (String)paramMap.get("Offset") );
					int iSkippedSamples = new Integer( (String)paramMap.get("SkippedSamples") );
					int iSegmentDuration = new Integer( (String)paramMap.get("SegmentDuration") );
					String[] saTempDataIn = new String[siLeadCount+1]; //initialize the string array which will receive the CSV data for each channel
					if((make12LeadTestPattern && bTestPattern)){
						saChannelName = new String[13]; // array of names of each channel(lead) e.g. I, II, III, V1, V2...
					}else{
						saChannelName = new String[siLeadCount+1]; // array of names of each channel(lead) e.g. I, II, III, V1, V2...
					}
					
					for(int leadNum=0;leadNum < siLeadCount+1;leadNum++){
						String key = "lead_"+leadNum;
						saTempDataIn[leadNum] = (String)paramMap.get(key); 
						if(leadNum==0){
							saChannelName[0]="millisecond";
						}else{
							saChannelName[leadNum] = WebServiceUtility.guessLeadName(leadNum-1, siLeadCount);
						}
					}
		
					// Parse the data from the CSV strings, rotating the array in the process.
					int channelCount;
					double[][] tempData = new double[iSampleCount][siLeadCount+1];
					if((make12LeadTestPattern && bTestPattern)){
						tempData = new double[iSampleCount][13];
						channelCount = 3; // the test pattern only supplies 3 leads, which will be copied into 4 leads each for 12 lead test pattern.
					}else{
						channelCount = siLeadCount;
					}
					
					for(int ch = 0; ch < (channelCount+1); ch++) {
						if(saTempDataIn[ch] != null){
							String[] leadSamples = saTempDataIn[ch].split(",");
							for(int point=0;point<leadSamples.length;point++){
								tempData[point][ch] = Double.parseDouble(leadSamples[point]);
								if((make12LeadTestPattern && bTestPattern) && (ch>0)){
									tempData[point][ch+3] = tempData[point][ch]; // leads aVR, aVL and aVF are copies of lead I
									tempData[point][ch+6] = tempData[point][ch]; // leads V1, V2, and V3 are copies of lead II
									tempData[point][ch+9] = tempData[point][ch]; // leads V4, V5, and V6 are copies of lead III
								}
							}
						}else{
							System.err.println("ERROR VisualizationData.fetchSubjectVisualizationData() missing data for lead " + ch + " of " + siLeadCount);
						}
					}
					// populate the result object to be returned.   
					visualizationData = new VisualizationData();
					visualizationData.setECGDataLength(iSampleCount);
					visualizationData.setECGDataLeads(siLeadCount);
					visualizationData.setOffset(iSegmentOffset);
					visualizationData.setSkippedSamples(iSkippedSamples);
					visualizationData.setMsDuration(durationMilliSeconds); // msDuration);
					visualizationData.setSaLeadName(saChannelName);
					visualizationData.setECGData(tempData);
					visualizationData.setSubjectID(subjectID);
					visualizationData.setMsDuration(iSegmentDuration);
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return visualizationData;	
	}
}
