/**************************
Functions used by singleLead.xhtml
revision 0.1 : May 29, 2013 - initial version Michael Shipway

*************************/
var dataSingle = [];
var labelSingle = [];
var drawECGCallCount = 0.0;
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
		
		// force a 40msec gap at the start of the graph.
		var point = [];
		point[0] = minTime-440; // column zero is time in milliseconds 
		point[1] = null;
		point[2] = 1000; 

	
		for(var cal=0; cal<=300;cal++){
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


	/** Single Lead Dygraph Display Mike Shipway 6/4/2013.
	 * 
	 * @returns
	 */	
	var CVRG_drawECGgraphSingle = function(divName, namespace){
		singleLeadNamespace = namespace;
		//alert("running CVRG_drawECGgraphSingle("+ singleLeadNamespace + ":" + divName +")");
		if(drawECGCallCount == 0){
			drawECGCallCount++;
//			data = WAVEFORM_getSingleLeadData(parent.CVRG_getLeadNum(), parent.data, parent.labels);
			dataSingle = WAVEFORM_getSingleLeadData(CVRG_getLeadNum(), dataFull, labelFull);
			ecg_graph = new Dygraph( 
					WAVEFORM_getElementById(divName),
					//parent.dataFull, 
					dataSingle,
					{
						stepPlot: false,
						labels: labelSingle,
						labelsDiv: WAVEFORM_getElementById('status_div'),
						labelsDivStyles: { border: '1px solid black' },
						labelsSeparateLines: false,
						gridLineColor: '#FA8C8C',
						labelsKMB: true,
						axes: { 
							x: { 
								valueFormatter: CVRG_xValueFormatter2,
								axisLabelFormatter: CVRG_xAxisLabelFormatter2,
								ticker: CVRG_xTicker 
							}, 
							y: { 
								valueFormatter: CVRG_yValueFormatter2,
								axisLabelFormatter: CVRG_yAxisLabelFormatter2, 
								ticker: CVRG_yTicker 
							} 
						},
						annotationClickHandler:    CVRG_annotationClickHandler, 
						annotationDblClickHandler: CVRG_annotationDblClickHandler, 
						annotationMouseOverHandler:CVRG_annotationMouseOverHandler, 
						annotationMouseOutHandler: CVRG_annotationMouseOutHandler, 
						drawCallback:              CVRG_drawCallbackSingle, 
						pointClickCallback:        CVRG_pointClickCallbackSingle,
						zoomCallback:              CVRG_zoomCallback,
						
						highlightCallback: function(e, x, pts) {
							var x = WAVEFORM_getElementById(divName).xpos;
							var y = WAVEFORM_getElementById(divName).ypos;
							var yOffset = 209;
							var xOffset = 380;
							//var yOffset = 0;
							//var xOffset = 0;
							CVRG_highlightCallback(e, pts, yOffset, xOffset);
						},
						unhighlightCallback: function(e){
							CVRG_unhighlightCallback(e, ecg_graph.rawData_[0].length-1);
						},
//						visibility: [true, false, false, false, false, false, false, false, false, false, false, false, ],
						highlightCircleSize: 5,
						strokeWidth: 1,
						drawPoints: false,
						padding: {left: 1, right: 1, top: 5, bottom: 5},
			            showRangeSelector: true,
			            rangeSelectorPlotStrokeColor: 'black',
			            rangeSelectorPlotFillColor: 'lightblue',
			            connectSeparatedPoints: true,
						//dateWindow: [0,2500], // Start and End times in milliseconds
						interactionModel : {  // custom interation model definition parameter (Implemented in interval.js)
							'mousedown' : CVRG_mousedown2,
							'mousemove' : CVRG_mousemove2,
							'mouseup' : CVRG_mouseup2

				      }
						
				}
			);
		}
		var newWidth = 700; 
		var newHeight= 400;
		ecg_graph.resize(newWidth, newHeight);
		CVRG_setLabels(displayMinV2, displayMaxV2, yLabel);
		CVRG_InitHorizontalLines(1, divName, singleLeadNamespace);
		CVRG_InitVerticalLines(divName, namespace);
	};

	//CVRG_drawECGgraphSingle(); // update from Mike Shipway at 050213
	//event -  the event object for the click 
	// p  - a point on one of the graphs that was clicked.
	var CVRG_pointClickCallbackSingle = function(event, p) {
		CVRG_unhighlightCrosshairs(1);
		// Check if the point is already annotated.
		if (p.annotation){
			alert("CVRG Message: " + p.name + " already has an annotation at: " + p.xval + " seconds.");
		}
		annotationBar.show();  // Scott Alger Primefaces dropdown menu SA 1/17/2013
		num++;
	};
	
	function zoomTime() {
		CVRG_unhighlightCrosshairs(1);
		// the screen size in uses
		var zoomCoefficient = (2500 / 100);
		var startPositionLeft = 0;
		CVRG_zoomGraphX( timeMinInput.value*zoomCoefficient+startPositionLeft , timeMaxInput.value*zoomCoefficient+startPositionLeft  )
	};
	
    function zoomVoltageOld() {
    	CVRG_unhighlightCrosshairs(1);
        // the screen size in uses
        var voltCoeff = (displayMaxV2-displayMinV2)/100; // converts percentage scroll bar to Voltage scale
    
        CVRG_zoomGraphY( (voltMinInput.value-50)*voltCoeff, (voltMaxInput.value-50)*voltCoeff)
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

		dataCenter += voltCenterOffset;
		minVolt += dataCenter;
		maxVolt += dataCenter;

		ecg_graph.updateOptions({
			valueRange: [minVolt, maxVolt]
		});
    };
    
    var centerVoltage = function(){
    	CVRG_unhighlightCrosshairs(1);
    	var col = 1;
    	var dataMin = ecg_graph.getValue(0,col);
    	var dataMax = ecg_graph.getValue(0,col);
    	var val = 0;
    	for(row=0;row<ecg_graph.numRows();row++){
    		val = ecg_graph.getValue(row,col);
    		if(dataMax < val) dataMax = val;
    		if(dataMin > val) dataMin = val;
    	}
    	var deltaV = (dataMax-dataMin);
    	
		displayMinV2 = dataMin-(deltaV*.05); // leave a 5% space at the bottom
		displayMaxV2 = dataMax+(deltaV*.05); // leave a 5% space at the top
    	var dataCenter = deltaV/2 + displayMinV2;
    	
		ecg_graph.updateOptions({
			valueRange: [displayMinV2, displayMaxV2]
		});

		sliderVoltRangeSingle.minValue = displayMinV2;
		sliderVoltRangeSingle.maxValue = displayMaxV2;
		voltMinInput.value = displayMinV2;
		voltMaxInput.value = displayMaxV2;
		voltCenterInput.value = 00;
    };
    
    var centerTime = function(){
    	CVRG_unhighlightCrosshairs(1);
    	ecg_graph.updateOptions({
    		 dateWindow: null
    	});
    };
    
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
		
		var bDots=false;
		if(CVRG_MsPerPixel < 0.1){
			bDots = true;
		}
		ecg_graph.updateOptions({
			drawPoints: bDots
		});

	};

	/** Prepends the namespace from the portlet environment to the element's ID, then returns the element thus found.
	 *  @param  elementID
	 *  
	 *  @returns - the DOM element
	 */
	var WAVEFORM_getElementById = function(elementID){
		return document.getElementById(singleLeadNamespace + ":" + elementID);
	}