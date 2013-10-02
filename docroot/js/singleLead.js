/**************************
Functions used by singleLead.xhtml
revision 0.1 : May 29, 2013 - initial version Michael Shipway

*************************/
var dataSingle = [];
var labelSingle = [];
//var drawECGCallCount = 0.0;
//var leadNum = 1; // parent.CVRG_getLeadNum();
//var leadName = "DUMMY I"; // parent.CVRG_getLeadName();
var sSecondSuffix       = leadName;
var timeLabelPrefix     = "Time (Sec) 2.5 * 10^0";
var yLabel              = "Amplitude (μV)";
var minTime = parent.WF_minTime;
var maxTime = parent.WF_maxTime;
var displayMinV2 = parent.displayMinV;
var displayMaxV2 = parent.displayMaxV;
var singleLeadNamespace = "";
var calPointCount = 300;
CVRG_sSecondSuffix = sSecondSuffix;
CVRG_timeLabelPrefix = timeLabelPrefix; 

//CVRG_sSecondSuffix = sSecondSuffix;
//CVRG_timeLabelPrefix = timeLabelPrefix; 

	var WAVEFORM_getSingleLeadData = function(leadNum2, dataFull2, labelsFull2){
		var singleDataCol = [];
		var fields = [];
		
		labelSingle[0] = labelsFull2[0];
		labelSingle[1] = labelsFull2[leadNum2+1];
		labelSingle[2] = "";
		
		var minTime = dataFull2[0][0];
		
//		var point = [];
//		point[0] = labelsFull2[0];
//		point[1] = labelsFull2[leadNum2+1];
//		point[2] = ""; 
//		singleDataCol.push(point);
	
		for(var cal=0; cal<=calPointCount;cal++){
			var point = [];
			
			// calibration mark is offset -400 msec from the start of data
			point[0] = minTime+cal-400; // column zero is time in milliseconds 
			point[1] = null;

			if((cal >= 50) & (cal <= 250)){ // 200 msec wide plus, 1000uV high.
				point[2] = 1000; // 1000 micro-Volts (uV).
			}else{
				point[2] = 0;
			}

			singleDataCol.push(point);
		}
		
		for(var samp=0; samp < dataFull2.length ;samp++){
			var point = [];
		
			fields = dataFull2[samp];
			point[0] = fields[0]; // column zero is time in milliseconds
			point[1] = fields[leadNum2+1]; // add 1 because column zero is time
			point[2] = null;
			singleDataCol.push(point);
		}
		return singleDataCol;
	};


//	WAVEFORM_getElementById(divName),
//	WAVEFORM_getIdwNamespace(divName),
	//parent.dataFull, 
	/** Single Lead Dygraph Display Mike Shipway 6/4/2013.
	 * 
	 * @returns
	 */	
	var SINGLELEAD_drawECGgraph = function(divName, namespace){
		singleLeadNamespace = namespace;
//		alert("running CVRG_drawECGgraphSingle("+ singleLeadNamespace + ":" + divName +")");
//		if(drawECGCallCount == 0){
//			drawECGCallCount++;
			dataSingle = WAVEFORM_getSingleLeadData(CVRG_getLeadNum(), dataFull, labelFull);
			ecg_graph = new Dygraph( 
					document.getElementById(singleLeadNamespace + ":" + divName),
					dataSingle,
					{
						stepPlot: false,
						labels: labelSingle,
						labelsDiv: document.getElementById(singleLeadNamespace + ":status_div"),
						labelsDivStyles: { border: '1px solid black' },
						labelsSeparateLines: false,
						gridLineColor: '#FA8C8C',
						labelsKMB: true,
						axes: { 
							x: { 
								valueFormatter: CVRG_xValueFormatter2, //format the text that appears when you hover on the chart
								axisLabelFormatter: CVRG_xAxisLabelFormatter2, // format the numbers on the axes (i.e. tick marks)
								ticker: CVRG_xTickerSingle // Draws grid lines and draws numbers on the axis
							}, 
							y: { 
								valueFormatter: CVRG_yValueFormatter2, //format the text that appears when you hover on the chart
								axisLabelFormatter: CVRG_yAxisLabelFormatter2, // format the numbers on the axes (i.e. tick marks)
								ticker: CVRG_yTickerSingle  // Draws grid lines and draws numbers on the axis
							} 
						},
						annotationClickHandler:    SINGLELEAD_annotationClickHandler, 
						annotationDblClickHandler: CVRG_annotationDblClickHandler, 
						annotationMouseOverHandler:CVRG_annotationMouseOverHandler, 
						annotationMouseOutHandler: CVRG_annotationMouseOutHandler, 
						drawCallback:              CVRG_drawCallbackSingle, 
						pointClickCallback:        CVRG_pointClickCallbackSingle,
						zoomCallback:              WAVEFORM_zoomCallbackSingle,
						underlayCallback:          SINGLELEAD_underlayCallback,

						highlightCallback: CVRG_highlightCallbackSingle,
						unhighlightCallback: function(e){
							CVRG_unhighlightCrosshairs(1);
						},
						highlightCircleSize: 5,
						strokeWidth: 1,
//						
						drawPoints: false,
						padding: {left: 1, right: 1, top: 5, bottom: 5},
			            showRangeSelector: true,
			            rangeSelectorPlotStrokeColor: 'black',
			            rangeSelectorPlotFillColor: 'lightblue',
			            connectSeparatedPoints: false,
			            drawGapEdgePoints: true,
						//dateWindow: [0,2500], // Start and End times in milliseconds
						interactionModel : {  // custom interation model definition parameter (Implemented in interval.js)
							'mousedown' : CVRG_mousedown2,
							'mousemove' : CVRG_mousemove2,
							'mouseup' : WAVEFORM3_mouseup
						}
					}
			);
		//}
		var newWidth = 700; 
		var newHeight= 400;
		ecg_graph.resize(newWidth, newHeight);
		CVRG_setLabels(displayMinV2, displayMaxV2, yLabel);
		CVRG_InitHorizontalLines(1, divName, singleLeadNamespace);
		CVRG_InitVerticalLines(divName, namespace);
	};

	var WAVEFORM_ShowAnnotationSingle = function() {
		ecg_graph.setAnnotations(tempAnnotations);
	};

	/** CVRG_pointClickCallbackSingle(); 
	//event -  the event object for the click 
	// p  - a point on one of the graphs that was clicked. 
	**/
	var CVRG_pointClickCallbackSingle = function(event, p) {
		CVRG_unhighlightCrosshairs(1);
		// Check if the point is already annotated.
		if (p.annotation){
			alert("CVRG Message: " + p.name + " already has an annotation at: " + p.xval + " seconds.");
		}
//		alert("CVRG Message: Point on " + p.name + " clicked at: " + p.xval + " seconds, " + p.yval + "yVolts,  open annotation popup.");
//		viewAnnotationPointEdit([{name:'sDataSX', value:p.xval},{name:'sDataSY', value:p.yval}]);

//		annotationBar.show();  // Scott Alger Primefaces dropdown menu SA 1/17/2013
		num++;
	};
	
	function zoomTime() {
		CVRG_unhighlightCrosshairs(1);
		// the screen size in uses
		var zoomCoefficient = (2500 / 100);
		var startPositionLeft = 0;
		CVRG_zoomGraphX( timeMinInput.value*zoomCoefficient+startPositionLeft , timeMaxInput.value*zoomCoefficient+startPositionLeft  );
	};
	
    function zoomVoltageOld() {
    	CVRG_unhighlightCrosshairs(1);
        // the screen size in uses
        var voltCoeff = (displayMaxV2-displayMinV2)/100; // converts percentage scroll bar to Voltage scale
    
        CVRG_zoomGraphY( (voltMinInput.value-50)*voltCoeff, (voltMaxInput.value-50)*voltCoeff);
    };
    
    var zoomVoltage = function(){
    	CVRG_unhighlightCrosshairs(1);
// the screen size in use
		var deltaV = (displayMaxV2-displayMinV2);
		var voltCoeff = deltaV/100; // converts percentage scroll bar to Voltage scale
		var dataCenter = deltaV/2 + displayMinV2;
		var voltCenterOffset = (WAVEFORM_getElementById('voltCenterInput').value)*voltCoeff;
		var minVolt = ((WAVEFORM_getElementById('voltMinInput').value-50)*voltCoeff);
		var maxVolt = ((WAVEFORM_getElementById('voltMaxInput').value-50)*voltCoeff);

		dataCenter -= voltCenterOffset;
		minVolt += dataCenter;
		maxVolt += dataCenter;

		ecg_graph.updateOptions({
			valueRange: [minVolt, maxVolt]
		});
    };
    
    var centerVoltage = function(){
    	CVRG_unhighlightCrosshairs(1);
    	var ext = getDataMinMax();
    	var centerV = (ext.dataMax+ext.dataMin)/2;
    	
		displayMinV2 = centerV-1000; // leave a 5% space at the bottom
		displayMaxV2 = centerV+1000
//    	var dataCenter = deltaV/2 + displayMinV2;
    	
		ecg_graph.updateOptions({
			valueRange: [displayMinV2, displayMaxV2]
		});

		sliderVoltRangeSingle.minValue = displayMinV2;
		sliderVoltRangeSingle.maxValue = displayMaxV2;
		voltMinInput.value = displayMinV2;
		voltMaxInput.value = displayMaxV2;
		voltCenterInput.value = 00;
    };
    
    var centerScaleVoltage = function(){
    	CVRG_unhighlightCrosshairs(1);
//    	var col = 1; // column zero is time, 1 is data, 2 is calibration.
//    	var dataMin = ecg_graph.getValue(calPointCount+1,col);
//    	var dataMax = ecg_graph.getValue(calPointCount+1,col);
//    	var val = 0;
//    	for(var row=calPointCount+2;row<ecg_graph.numRows();row++){
//    		val = ecg_graph.getValue(row,col);
//    		if(dataMax < val) dataMax = val;
//    		if(dataMin > val) dataMin = val;
//    	}
    	var ext = getDataMinMax();
    	var deltaV = (ext.dataMax-ext.dataMin);
    	
		displayMinV2 = ext.dataMin-(deltaV*.05); // leave a 5% space at the bottom
		displayMaxV2 = ext.dataMax+(deltaV*.05); // leave a 5% space at the top
//    	var dataCenter = deltaV/2 + displayMinV2;
    	
		ecg_graph.updateOptions({
			valueRange: [displayMinV2, displayMaxV2]
		});

		sliderVoltRangeSingle.minValue = displayMinV2;
		sliderVoltRangeSingle.maxValue = displayMaxV2;
		voltMinInput.value = displayMinV2;
		voltMaxInput.value = displayMaxV2;
		voltCenterInput.value = 00;
    };
    
    var getDataMinMax = function (){
    	var col = 1; // column zero is time, 1 is data, 2 is calibration.
    	var dataMin = ecg_graph.getValue(calPointCount+1,col);
    	var dataMax = ecg_graph.getValue(calPointCount+1,col);
    	var val = 0;
    	for(var row=calPointCount+2;row<ecg_graph.numRows();row++){
    		val = ecg_graph.getValue(row,col);
    		if(dataMax < val) dataMax = val;
    		if(dataMin > val) dataMin = val;
    	}
    	var ret = {
    				dataMin: dataMin, // microVolts
		    		dataMax: dataMax // microVolts
    			};
    	
    	return ret;
    };
    
    var centerTime = function(){
    	CVRG_unhighlightCrosshairs(1);
    	ecg_graph.updateOptions({
    		 dateWindow: null
    	});
    };
    
    
    var setVoltageZoom = function(newDisplayMinV, newDisplayMaxV){
		ecg_graph.updateOptions({
			valueRange: [newDisplayMinV, newDisplayMaxV]
		});
    }
	/**Overrides version in annotation.js 
	 * A function to call when the zoom window is changed (either by zooming in or out). 
	 * minDate and maxDate are milliseconds since epoch. 
	 * yRanges is an array of [bottom, top] pairs, one for each y-axis.
	 * 
	 * @param minDate
	 * @param maxDate
	 * @param yRanges
	 * @returns
	 */
	var CVRG_zoomCallback2 = function (minDate, maxDate, yRanges) {
		CVRG_unhighlightCrosshairs(1);
		var bDots = CVRG_bShowDots();
		var newTimeLabel = CVRG_getnewTimeLabel();
		ecg_graph.updateOptions({
//			isZoomedIgnoreProgrammaticZoom: true,
//			dateWindow: [minTime, maxTime],
			valueRange: [yRanges[0][0], yRanges[0][1]],
			drawPoints: bDots,
			xlabel: newTimeLabel
		});
		alert("displayMinV2:" + displayMinV2 + "VoltageMin:" + yRanges[0][0] );
		displayMinV2 = yRanges[0][0];
		displayMaxV2 = yRanges[0][1];
		voltMinInput.value = 0;
		voltMinInput.value = 100;
		voltCenterInput.value = 0;
	};

	// Adds the annotation's details to the list.  Also generates a unique ID for each annotation so that Javascript can bold/unbold. 
	var CVRG_drawCallbackSingle = function(ecg_graph) {
		//var leadCount = ecg_graph.rawData_[0].length-1;
		//CVRG_InitHorizontalLines(leadCount, "ecg_div");
		//CVRG_InitVerticalLines("ecg_div", namespace);

		
		var ann = ecg_graph.annotations(); 


		var html = "";
		for (var i = 0; i < ann.length; i++) {
			var name = nameAnnotation(ann[i]); // formats the summary of a single annotation for display.
			html += "<span id='" + name + "' title='" + ann[i].fullAnnotation  + "'>";
			html += "<a href='javascript:CVRG_CenterGraph(" + ann[i].x + "," + ann[i].y + ");'>"; // center graph on click
			html += "["   + ann[i].shortText       + "]";    // text to show in the flag
			html += "</a>";    // end of hyperlink
			html += " "   + ann[i].text            + ""; // will appear when mouse hovers over flag
			// html += "<i>" + ann[i].fullAnnotation  + "</i>"; // CVRG extra data, not used by dygraphs
			html += "</span><br/>";
		}
		WAVEFORM_getElementById("list_div").innerHTML = html;
		
//		var bDots=false;
//		if(CVRG_MsPerPixel < 0.1){
//			bDots = true;
//		}
//		ecg_graph.updateOptions({
//			drawPoints: bDots
//		});

	};

	/** Prepends the namespace from the portlet environment to the element's ID, then returns the element thus found.
	 *  @param  elementID
	 *  
	 *  @returns - the DOM element
	 */
	var WAVEFORM_getElementById = function(elementID){
		return document.getElementById(singleLeadNamespace + ":" + elementID);
	};
	
	var WAVEFORM_getIdwNamespace = function(elementID){
		return singleLeadNamespace + ":" + elementID;
	};
	
	/** A function to call when the zoom window is changed (either by zooming in or out). minDate and maxDate are milliseconds since epoch. yRanges is an array of [bottom, top] pairs, one for each y-axis.
	 * 
	 * @param minDate
	 * @param maxDate
	 * @param yRanges
	 * @returns
	 */
	var WAVEFORM_zoomCallbackSingle = function (minDate, maxDate, yRanges) {
		var bDots = CVRG_bShowDots();
//		var newTimeLabel = CVRG_getnewTimeLabel();
		ecg_graph.updateOptions({
//			valueRange: [yRanges[0][0], yRanges[0][1]],
//			xlabel: newTimeLabel,
			drawPoints: bDots
		});
	};
	
	/** Called whenever the user clicks on an annotation.
	 * 
	 * Type: function(annotation, point, dygraph, event)
	 * annotation: the annotation left
	 * point: the point associated with the annotation
	 * dygraph: the reference graph
	 * event: the mouse event
	 * Default: null
	**/
	var SINGLELEAD_annotationClickHandler = function(ann, point, dg, event) {
//		CVRG_annotationClickHandlerJSNI(point.xval, point.yval);
		alert("SINGLELEAD_annotationClickHandler() called.  series: " + ann.series + " x: " + ann.x  + " y: " + ann.y  
				+ " flagLabel: " + ann.flagLabel + " ontologyId: " + ann.ontologyId 
				+ " fullAnnotation: " + ann.fullAnnotation + " height: " + ann.height + " annotationID: " + ann.annotationID);
		
		viewCurrentAnnotationNoEdit([{name:'annotationID', value:ann.annotationID}]);

		
		//eventDiv.innerHTML += "click: " + nameAnnotation(ann)  + "<br/>";
		
	};
	
	var SINGLELEAD_underlayCallback = function(canvas, area, g){
		WAVEFORM_showHighLightQueue(canvas, area, g);		
	}
