/**************************
Functions used by 12Lead_D.html and 12Lead_GE.html
revision 0.1 : April 6, 2011 - initial version Michael Shipway
Revision 1.0 : August 19, 2013 - Updated for use in Waveform 3. .
*************************/
	var namespaceGlobal = "";
	var isMultigraph=true;
//	var data = [];
	var graphSet = [];
	var graphCalSet = [];
	var xLineSet = [];
	var blockRedraw = false; // prevents WAVEFORM_drawCallback from looping on events from other graphs.
	var initialized = false;
	var leadDurationMS = 1200;
    var displayMinV = -2000;
    var displayMaxV = +2000;
    var StartmSec = 0; // Starting time in the data, e.g. first point to display.
    var bInvertVoltageSlider=true; // quick fix to invert the values from the slider.

    var graphDivCalPrefix 	= "graphDivCal";
	var labelCalPrefix 		= "labelCal";
	var graphDivPrefix 		= "graphDiv";
	var labelDivPrefix 		= "labelDiv";
	var verticalXHairPrefix = "lineDiv";
	
//	var callbackSet = [];
//
//	callbackSet[0] = CVRG_clickCallback0;
//	callbackSet[1] = CVRG_clickCallback1;
//	callbackSet[2] = CVRG_clickCallback2;
//	callbackSet[3] = CVRG_clickCallback3;
//	callbackSet[4] = CVRG_clickCallback4;
//	callbackSet[5] = CVRG_clickCallback5;
//	callbackSet[6] = CVRG_clickCallback6;
//	callbackSet[7] = CVRG_clickCallback7;
//	callbackSet[8] = CVRG_clickCallback8;
//	callbackSet[9] = CVRG_clickCallback9;
//	callbackSet[10] = CVRG_clickCallback10;
//	callbackSet[11] = CVRG_clickCallback11;


//	var lines = [];

	 // I  // 
	 // II  // 
	 // III  // 
	 // aVR  // 
	 // aVL  // 
	 // aVF  // 
	 // V1  // 
	 // V2  // 
	 // V3  // 
	 // V4  // 
	 // V5  // 
	 // V6  // 
	
	var WF_minTime = 1, WF_maxTime = 5000;
	var dataJsonParse=[];
	var data =[];
	var saGraphTitle = [];
	var saGraphTitleArray=[];
	
	
//	function show12LeadData(minTime, maxTime, ECG) {
	function show12LeadData() {
		//alert("running show12LeadData()");
		var startTimeNormal = (new Date).getTime();
//			var namespace = '#{facesContext.externalContext.encodeNamespace('')}';
//			var namespace = "n/a";
//			WF_minTime = minTime;
//			WF_maxTime = maxTime;
//			dataFull = WAVEFORM_parseCSV(ECG, namespace);
			
			// Turning on the Dygrahs Display
			WAVEFORM_showGraphs();
			setGraphLabel(12);
		// debugPrintTime("show12LeadData ", startTimeNormal);
	};

	// This function is kept on the .xhtml page, because it contains code specific to this layout. 
     var WAVEFORM_showGraphs = function(){
         //alert("running WAVEFORM_showGraphs() in panelDisplayLeads.xhtml data.length:" + dataFull.length);
         var graphDurationMS = 1200;
         var graphWidthPx = 250;
         var graphHeightPx = 150;
         var calibrationCount = 3;
         populateGraphsCalibrations(graphDurationMS, graphWidthPx, graphHeightPx, dataFull, calibrationCount);
     };
	
	var parseJSONdata = function(){					
		var startTimeParseData = (new Date).getTime();
//			var data = '#{visualizeGraphBacking.data}';
		if(data.length>0){
			dataJsonParse = JSON.parse(data);
			WF_minTime = dataJsonParse.minTime;
			WF_maxTime = dataJsonParse.maxTime;
			dataFull = WAVEFORM_parseCSV(dataJsonParse.ECG);
	//		data = null;
		// debugPrintTime("JSON.parse(data) ", startTimeParseData);
		
//		alert("parseJSONdata() dataJsonParse.minTime, dataJsonParse.maxTime: " + dataJsonParse.minTime + ", " + dataJsonParse.maxTime);
			//show12LeadData(dataJsonParse.minTime, dataJsonParse.maxTime, dataJsonParse.ECG);
		var startTimeParseTitle = (new Date).getTime();
//			var saGraphTitle = '#{visualizeGraphBacking.saGraphTitle}'
			saGraphTitleArray = JSON.parse(saGraphTitle);
//			alert("saGraphTitleArray[0]: " + saGraphTitleArray[0] + "  saGraphTitleArray[1]: " + saGraphTitleArray[1] );
		// debugPrintTime("JSON.parse(saGraphTitle) ", startTimeParseTitle);
		}else{
			alert("parseJSONdata() data variable is empty.");
		}
	};

	var renderData =  function(){
//		saGraphTitle = '#{visualizeGraphBacking.saGraphTitle}'
//	  alert("renderData() saGraphTitle: " + saGraphTitle);
		// startDebugTable();
		parseJSONdata();
		if(isMultigraph){
//			show12LeadData(dataJsonParse.minTime, dataJsonParse.maxTime, dataJsonParse.ECG);
			show12LeadData();
		}else{
			renderSingleGraphAndAnnotations();
		}
		// endDebugTable();
	};

    /** Event handler for when the mouse is over any graph. 
	 * this callback gets called every time a new point is highlighted.
	 * 
	 * http://dygraphs.com/options.html#Callbacks
	 * 
	 * @param e - event: the JavaScript mousemove event
	 * @param x - the x-coordinate of the highlighted points
	 * @param pts - points: an array of highlighted points: [ {name: 'series', yval: y-value}, … ]
	 * @returns
	 */
    var CVRG_highlightCallbackAll = function(e, x, points){
//		CVRG_highlightCallbackCommon(pts, 1);
		for (var i = 0; i < points.length; i++) {
			ptsName = points[i].name;
			var x=Math.floor(points[i].canvasx) + "px";
			for(var xL=0;xL < xLineSet.length;xL++){	
				var xLineTemp = xLineSet[xL];
				var xLineID = xLineTemp.id; // should be the same as array index "xL"
				
				xLineTemp.style.left = x;
				//xLineTemp.style.top = "-150px";
				for(var lab=0;lab<labelFull.length;lab++){
					if(ptsName == labelFull[lab]){
						lineID = verticalXHairPrefix +  (lab-1);
					};
				}

				// don't show the line on the graph the mouse is over.
				if(xLineID == lineID){
					xLineTemp.style.width = "0px";
				}else{  
					xLineTemp.style.width = "3px";
				};
			};
		};
	};


	var CVRG_unhighlightCallback = function(e) {
		for (var xL = 0; xL < xLineSet.length; xL++) {
			xLineSet[xL].style.display = "none";
		};
	};

	
	/** When set, this callback gets called every time the dygraph is drawn. 
	 * This includes the initial draw, after zooming and repeatedly while panning.
	 * http://dygraphs.com/options.html#Callbacks
	 *  
	 * Type: function(dygraph, is_initial)
	 * @param   me - dygraph: The graph being drawn
	 * @param   initial - is_initial: True if this is the initial draw, false for subsequent draws.
	 * 
	 * Default: null
	 */
	var WAVEFORM_drawCallback = function(me, initial) {
//		return;
//		var dummy=0;
		if (blockRedraw || initial) return;  // "blockRedraw" prevents WAVEFORM_drawCallback from looping on events from other graphs.
		blockRedraw = true;
		var range = me.xAxisRange();
		var yrange = me.yAxisRange();
		for (var j = 0; j < graphSet.length; j++) {
			if (graphSet[j] == me) continue;
			graphSet[j].updateOptions( {
				dateWindow: range,
				valueRange: yrange
			} );
		}
		blockRedraw = false;
	}; 
	
	/** Changes the dataWindow on all 12 leads. 
	 * @param   minTime - the earliest time displayed on the single lead graph
	 * @param   maxTime - The latest time displayed on the  single lead graphs.
	 * 
	 * Default: null
	 */
	var WAVEFORM_zoomGraphX = function(minTime, maxTime) {
		var startTimeZoomGraphX = (new Date).getTime();
		var range = [minTime,maxTime];
		for (var j = 0; j < graphSet.length; j++) {
			graphSet[j].updateOptions( {
				dateWindow: range
			} );
		}
		// debugPrintTime("ZoomGraphX", startTimeZoomGraphX);
	}; 

	var createXLine = function(lineIdName){
		var xlineX = document.createElement("div");
		xlineX.id = lineIdName;
		xlineX.styleClass="crossHairVert";
		xlineX.style.display = ""; // "none";
		xlineX.style.width = "1px";
		xlineX.style.height = "100%";
		xlineX.style.top = "-150px";
		xlineX.style.left = "0px";
		xlineX.style.backgroundColor = "purple";
		xlineX.style.position = "relative";

		return xlineX;
	};

	
	/** Creates and populates all the data graphs(one lead per) and all the calibration graphs.
	 * 
	 */
	var populateGraphsCalibrations = function(graphDurationMS, graphWidthPx, graphHeightPx, 
											dataFull, calibrationCount){
		
//	var startTime1 = (new Date).getTime();
		populate12Graphs(graphDurationMS, graphWidthPx, graphHeightPx, 
						dataFull, namespaceGlobal);
	// debugPrintTime("Current", startTime1);
		
//		var graphDivName = namespaceGlobal + ":" + graphDivCalPrefix;
//		var labelDivName = namespaceGlobal + ":" + labelCalPrefix;
		
//	var startTimeCal = (new Date).getTime();
        makeCalibrationMarks(calibrationCount, graphHeightPx, graphDivCalPrefix, labelCalPrefix);
	// debugPrintTime("Calibration", startTimeCal);
	};
	
	
	var populate12Graphs = function(graphDurationMS, graphWidthPx, graphHeightPx, 
									dataFull, namespace){
		
		StartmSec = dataFull[1][0];  // Starting time in the data, e.g. first point to display.
//		var fields = [];
		//var divTag = "";
		var labelDivName ="";
		//data = new Array(dataFull.length);
//		data = dataFull;
//		data = [];
//		var data20thSize = dataFull.length/20;
//		for(var samp=0; samp < data20thSize;samp++){
//			fields = dataFull[samp];
//			data.push(fields);
////			data[samp] = fields;
//		}

		leadDurationMS = graphDurationMS;
		graphSet = null;
		graphSet = [];
		xLineSet = null;
		xLineSet = [];
//		var headerEnd = data.indexOf("\n");
//		var header = data.substring(0,headerEnd);
//		var columnNames = header.split(",");
		//var highlightCB = CVRG_hlCB1;
		
		// labelFull.length is one greater than the number of data columns 
		// because it includes the Timestamp column label "msec"
		for(var col=0;col<(labelFull.length-1); col++){ 
			lineIdName   = verticalXHairPrefix + col;
			graphDivName = graphDivPrefix + col;
			labelDivName = labelDivPrefix + col;
			
			blockRedraw=true;
//			var cback = eval("CVRG_clickCallback"+ col);
			graphSet.push(getGraphCommon(labelFull[col+1],
										col, 
										graphDivName, 
										labelDivName,
										graphWidthPx, graphHeightPx));

			var xLine = createXLine(lineIdName); // document.getElementById(lineIdName); // 
			WAVEFORM_getElementByIdEndsWith("div", graphDivName).appendChild(xLine);
//			xLineSet.push(xLine);
			xLineSet[col] = xLine;
		}
//		blockRedraw=false;
	};

	var getGraphCommon  = function (lead, leadNumber, graphDivName, labelDivName,
									graphWidthPx, graphHeightPx){
		var graphDiv = WAVEFORM_getElementByIdEndsWith("div", graphDivName);
		var labelDiv = WAVEFORM_getElementByIdEndsWith("div", labelDivName);

		var vis = Array(12);
		for(var i=0;i<12;i++){
			vis[i] = ((i)==leadNumber);
		}
		
		var graph = new Dygraph(
			graphDiv,
			dataFull,
			{
				clickCallback: function(e, x, points){
					CVRG_clickCallCommon(leadNumber);
				},
				highlightCallback: CVRG_highlightCallbackAll,
				unhighlightCallback: CVRG_unhighlightCallback,
				drawCallback: WAVEFORM_drawCallback, //called every time the dygraph is drawn. 			
				visibility: vis,
				dateWindow: [ WF_minTime, WF_maxTime],
				valueRange: [displayMinV, displayMaxV],
				labels: labelFull,
				labelsDiv: labelDiv,
				axes: { 
					x: { 
						valueFormatter: CVRG_xValueFormatter2,
						axisLabelFormatter: CVRG_xAxisLabelFormatter2,
						ticker: CVRG_xTickerMultiLead 
					}, 
					y: { 
						valueFormatter: CVRG_yValueFormatter2,
						axisLabelFormatter: CVRG_yAxisLabelFormatter2, 
						ticker: CVRG_yTickerMultiLead 
					} 
				},
				xAxisLabelWidth:0,
				yAxisLabelWidth:0,
				rollPeriod: 0,
				showRoller: false,
				errorBars: false,
				stepPlot: true,
				padding: {left: 0, right: 0, top: 0, bottom: 0},
				axisLabelFontSize: 0,
				gridLineColor: '#FF0000'
			}
		);
//		graph.resize(graphWidthPx, graphHeightPx);
		return graph;
	};

//	var WAVEFORM_backfillDataArray = function (dataFull){
//		var fields = [];
//		for(samp=0; samp < dataFull.length ;samp++){
//			fields= dataFull[samp];
//			data.push(fields);
//		}
//	};
	
//	var WAVEFORM_replaceDataArray = function (dataFull){
//		data = dataFull;
//		//dataFull = null;
////		for (var j = 0; j < graphSet.length; j++) {
////			graphSet[j].updateOptions( {
////				dateWindow: range
////			} );
////		}
//		graphSet[0].updateOptions( {
//			dateWindow: null
//		} );
//	};
	
	var makeCalibrationMarks = function(markCount, graphHeightPx, graphDivName, labelDivName){
		//graphCalSet=[];
		for (var i = 0; i < markCount; i++) {
			blockRedraw=true;
			var graphDiv = WAVEFORM_getElementByIdEndsWith("div", graphDivName + i);
			var labelDiv = WAVEFORM_getElementByIdEndsWith("div", labelDivName + i);
			var calibrationData = "mS,\n" +
			"0,0\n" +
			"100,0\n" +
			"100,1000\n" +
			"300,1000\n" +
			"300,0\n" +
			"400,0\n";
			
			var graphCal = new Dygraph(
				graphDiv,
				calibrationData,
				{
					dateWindow: [ 0, 400],
					colors: ["#000000"],
					labelsDiv: labelDiv,
					rollPeriod: 0,
					showRoller: false,
					errorBars: false,
					axes: { 
						x: { 
//							valueFormatter: CVRG_xValueFormatterCalibration,
							axisLabelFormatter: CVRG_xAxisLabelFormatter2,
							ticker: CVRG_xTickerMultiLead 
						}, 
						y: { 
							valueFormatter: CVRG_yValueFormatter3,
							axisLabelFormatter: CVRG_yAxisLabelFormatter2, 
							ticker: CVRG_yTickerMultiLead 
						} 
					},
					xAxisLabelWidth:0,
					yAxisLabelWidth:0,
					padding: {left: 0, right: 0, top: 0, bottom: 0},
					axisLabelFontSize: 0,
					valueRange: [displayMinV, displayMaxV],
					gridLineColor: '#FF0000',
					unhighlightCallback: CVRG_unhighlightCallback,
					drawCallback: WAVEFORM_drawCallback // called every time the dygraph is drawn. 			 	
				}
			);
			graphCal.resize(40, graphHeightPx);
			graphCalSet[i] = graphCal;
		}
	};
	
	/** Calls Dygraph.prototype.resize() for each of the 12 graphs.
	 * Resizes the dygraph. If no parameters are specified, resizes to fill the
	 * containing div (which has presumably changed size since the dygraph was
	 * instantiated. If the width/height are specified, the div will be resized.
	 *
	 * This is far more efficient than destroying and re-instantiating a
	 * Dygraph, since it doesn't have to reparse the underlying data.
	 *
	 * @param {Number} [width] Width (in pixels)
	 * @param {Number} [height] Height (in pixels)
	 */
	var set12GraphSize = function(newWidth, newHeight){
		for(var i=0;i<12;i++){
			graphSet[i].resize(newWidth, newHeight);
		}
		for(var i=0;i<3;i++){
			graphCalSet[i].resize(40, newHeight);
		}
	};
	
	 var zoomTime = function() {
		// the screen size in uses
		var zoomCoefficient = (2500 / 100);
		var startPositionLeft = 0;
		CVRG_zoomGraphX( timeMinInput.value*zoomCoefficient+startPositionLeft , timeMaxInput.value*zoomCoefficient+startPositionLeft  );
	};
	
    var zoomVoltage = function() {
        // the screen size in use
        var voltCoeff = (displayMaxV-displayMinV)/100; // converts percentage scroll bar to Voltage scale
    
        CVRG_zoomGraphY( (voltMinInput.value-50)*voltCoeff, (voltMaxInput.value-50)*voltCoeff);
    };
    
    var panVoltage = function(graphNumber, bPanSingle, namespace) {
        // the screen size in use
    	var voltCenterInputName = namespace + ":voltCenterInput" + graphNumber;
        var voltCoeff = (displayMaxV-displayMinV)/100; // converts percentage scroll bar to Voltage scale
		var voltCenterValue = (WAVEFORM_getElementByIdEndsWith("input", voltCenterInputName).value-50)*voltCoeff;

		// quick fix to invert the values from the slider.
		if(bInvertVoltageSlider){
			voltCenterValue = -voltCenterValue;
		}
		var minVolt = displayMinV+(voltCenterValue);
		var maxVolt = displayMaxV+(voltCenterValue);
		
		blockRedraw=bPanSingle;
		if(graphNumber=="ALL") {
			graphNumber = 0;
			for(var gs=0;gs<graphSet.length;gs++){
				graphSet[gs].updateOptions({
					valueRange: [minVolt, maxVolt]
				});
			}
		}else{
			graphSet[graphNumber].updateOptions({
				valueRange: [minVolt, maxVolt]
			});
		}
		blockRedraw=false;
		
    };
    
//    var panVoltageSingle = function(namespace){
//        // the screen size in use
//    	var voltCenterInputName = namespace + ":voltCenterInput" + graphNumber;
//        var voltCoeff = (displayMaxV-displayMinV)/100; // converts percentage scroll bar to Voltage scale
//		var voltCenterValue = (document.getElementById(voltCenterInputName).value-50)*voltCoeff;
//		var minVolt = displayMinV+(voltCenterValue);
//		var maxVolt = displayMaxV+(voltCenterValue);
//
//		ecg_graph.updateOptions({
//			valueRange: [minVolt, maxVolt]
//		});
//    };
    
    /** Center and Scale the voltage axis of the Multi-lead view
     * 
     */
    var centerScaleVoltageMulti = function(){
		for(var gs=0;gs<graphSet.length;gs++){
	    	var ext = getDataMinMaxMulti(gs);
	    	var deltaV = (ext.dataMax-ext.dataMin);
	    	
			displayMinV2 = ext.dataMin-(deltaV*.05); // leave a 5% space at the bottom
			displayMaxV2 = ext.dataMax+(deltaV*.05); // leave a 5% space at the top
	    	
			graphSet[gs].updateOptions({
				valueRange: [displayMinV2, displayMaxV2]
			});
	
//			sliderVoltRangeSingle.minValue = displayMinV2;
//			sliderVoltRangeSingle.maxValue = displayMaxV2;
//			voltMinInput.value = displayMinV2;
//			voltMaxInput.value = displayMaxV2;
//			voltCenterInput.value = 00;
		}
    };
    
    /** Center the voltage axis on the average, set voltage scale to default +/- 2000uV.
     * 
     */
    var centerVoltageMulti = function(){
		for(var gs=0;gs<graphSet.length;gs++){
	    	var ext = getDataMinMaxMulti(gs);
	    	var centerV = (ext.dataMax+ext.dataMin)/2;
	    	
			displayMinV2 = centerV-2000; // same scale as default graph
			displayMaxV2 = centerV+2000; // same scale as default graph
	    	
			graphSet[gs].updateOptions({
				valueRange: [displayMinV2, displayMaxV2]
			});
	
//			sliderVoltRangeSingle.minValue = displayMinV2;
//			sliderVoltRangeSingle.maxValue = displayMaxV2;
//			voltMinInput.value = displayMinV2;
//			voltMaxInput.value = displayMaxV2;
//			voltCenterInput.value = 00;
    	}
    };
    
    /** Center the voltage axis on the Mode (not average) value, set voltage scale to default +/- 2000uV.
     * 
     */
    var centerVoltageMultiMode = function(){
		for(var gs=0;gs<graphSet.length;gs++){
	    	var ext = getDataMinMaxMulti(gs);
	    	var centerV = ext.mode;
	    	
			displayMinV2 = centerV-2000; // same scale as default graph
			displayMaxV2 = centerV+2000; // same scale as default graph
	    	
			graphSet[gs].updateOptions({
				valueRange: [displayMinV2, displayMaxV2]
			});
	
//			sliderVoltRangeSingle.minValue = displayMinV2;
//			sliderVoltRangeSingle.maxValue = displayMaxV2;
//			voltMinInput.value = displayMinV2;
//			voltMaxInput.value = displayMaxV2;
//			voltCenterInput.value = 00;
    	}
    };
    
    /** Find the minimum and maximum Voltage values in all of the samples for the specified graph.
     * Assumes that the graph number and lead number are the same value.
     */ 
    var getDataMinMaxMulti = function (graphNumber){
    	var ret = [];
        var modeMap = {};
        var maxEl = graphSet[graphNumber][0];
        var maxCount = 1;
        var modeRounder = 100;

    	var dataMin = graphSet[graphNumber].getValue(calPointCount+1,graphNumber+1);
    	var dataMax = graphSet[graphNumber].getValue(calPointCount+1,graphNumber+1);
    	var val = 0;
    	for(var row=calPointCount+2;row<(graphSet[graphNumber].numRows()/2);row++){
    		val = graphSet[graphNumber].getValue(row,graphNumber+1); // column zero is time, so lead zero's data is in column one.
    		if(dataMax < val) dataMax = val;
    		if(dataMin > val) dataMin = val;
    		
    		val = (val/modeRounder)|0; // bitwise OR to truncate floating point figures 
    		if(modeMap[val] == null)
        		modeMap[val] = 1;
        	else
        		modeMap[val]++;	
        	if(modeMap[val] > maxCount)
        	{
        		maxEl = val;
        		maxCount = modeMap[val];
        	}
    	}
    	ret = {
			dataMin: dataMin, // microVolts
			dataMax: dataMax, // microVolts
			mode: maxEl*modeRounder // mode (mode commonly occuring value
		};
    	
    	return ret;
    };
    
    function mode(array)
    {
        if(array.length == 0)
        	return null;
        var modeMap = {};
        var maxEl = array[0], maxCount = 1;
        for(var i = 0; i < array.length; i++)
        {
        	var val = array[i];
        	if(modeMap[val] == null)
        		modeMap[val] = 1;
        	else
        		modeMap[val]++;	
        	if(modeMap[val] > maxCount)
        	{
        		maxEl = val;
        		maxCount = modeMap[val];
        	}
        }
        return maxEl;
    }
    
    var setVoltageZoomMulti = function(newDisplayMinV, newDisplayMaxV){
		for(var gs=0;gs<graphSet.length;gs++){
			graphSet[gs].updateOptions({
				valueRange: [newDisplayMinV, newDisplayMaxV]
			});
		}
    };
    
    var resetMultiGraphDimensions = function(){
    	setVoltageZoomMulti(displayMinV, displayMaxV);
		resizeMultiGraphParts(120, 150, 40, 150);
    };
    
    var dataFull = [];
    var labelFull =[];
    /** Derived from the Dygraph method "Dygraph.prototype.parseCSV_(data)"
     * Also populates the "labelFull[]" array with the headers from the ecg CSV file.
     * The following is the documentation for Dygraphs:
     * @private
     * Parses a string in a special csv format.  We expect a csv file where each
     * line is a date point, and the first field in each line is the date string.
     * We also expect that all remaining fields represent series.
     * if the errorBars attribute is set, then interpret the fields as:
     * date, series1, stddev1, series2, stddev2, ...
     * @param {[Object]} data See above.
     *
     * @return [Object] An array with one entry for each row. These entries
     * are an array of cells in that row. The first entry is the parsed x-value for
     * the row. The second, third, etc. are the y-values. These can take on one of
     * three forms, depending on the CSV and constructor parameters:
     * 1. numeric value
     * 2. [ value, stddev ]
     * 3. [ low value, center value, high value ]
     */
    var WAVEFORM_parseCSV = function(data) {
//    	var ret = [];
    	//dataFull = ""; // clear data variable.
//    	namespaceGlobal = namespace;
    	var startTimeParseCSV = (new Date).getTime();
    	dataFull = []; // clear data variable.
    	var lines = data.split("\n");
//    	var statusPrefix = "Loading ECG data ";
//    	var statusSuffix = " 0% complete";
//  	Use the default delimiter or fall back to a tab if that makes sense.
    	var delim = ',';
    	var start = 0;
    	start = 1;
    	labelFull = lines[0].split(delim);  // NOTE: _not_ user_attrs_.
//    	var line_no = 0;

    	var expectedCols = labelFull.length;
    	var outOfOrder = false;
    	for (var i = start; i < lines.length; i++) {
    		var line = lines[i];
    		line_no = i;
    		if (line.length == 0) continue;  // skip blank lines
    		if (line[0] == '#') continue;    // skip comment lines
    		var inFields = line.split(delim);
    		if (inFields.length < 2) continue;

    		var fields = [];
    		// Time is always in  milliSeconds
    		fields[0] = parseFloat(inFields[0]);
    		// Values are just numbers
    		for (var j = 1; j < inFields.length; j++) {
    			fields[j] = parseFloat(inFields[j]);
    		}
    		if (dataFull.length > 0 && fields[0] < dataFull[dataFull.length - 1][0]) {
    			outOfOrder = true;
    		}

    		if (fields.length != expectedCols) {
    			alert("Number of columns in line " + i + " (" + fields.length +
    					") does not agree with number of labels (" + expectedCols +
    					") " + line);
    		}

    		dataFull.push(fields);
    		statusSuffix = ((i*100)/lines.length) + "% complete";
//    		var instructionName = namespace + ":instruction";
//        	var instruction = document.getElementById(instructionName);
//        	instruction.innerHTML = statusPrefix + statusSuffix;
    	}

    	if (outOfOrder) {
    		this.warn("CSV is out of order; order it correctly to speed loading.");
    		dataFull.sort(function(a,b) { return a[0] - b[0]; });
    	}
//    	var instructionName = namespace + ":instruction";
//    	var instruction = document.getElementById(instructionName);
//    	instruction.innerHTML = "Done";

    	// debugPrintTime("ParseCSV", startTimeParseCSV);
    	
    	return dataFull;
    };
    
	
	// [[containerWidth, containerHeight], [graphWidth,graphHeight,mSecWidth,uVoltLow,uVoltHigh],
	//  [container0Left,container0Top],  [container1Left,container1Top], [container2Left,container2Top] ... ]
	var LO_Traditional = [[250,170], [240,150,2500,-2000,2000],
	                      [45,0],[45,175],[45,350],
	                      [290,0],[290,175],[290,350],
	                      [535,0],[535,175],[535,350],
	                      [780,0],[780,175],[780,350]];

	var LO_GE = [[120,100], [115,100,1200,-2000,2000],
	             [45,0],  [160,  0],[275,  0],[390,  0],[505,  0],[620,  0],
	             [45,175],[160,175],[275,175],[390,175],[505,175],[620,175]];

	var layOutList = [LO_Traditional,LO_GE];

	// rearrange the graphs into a different layout, not currently used.
	var moveGraphDivs = function(layoutNumber, namespace){
		var stepCount = 1;
		// all graph containers are the same size.
		var newContainerWidth = layOutList[layoutNumber][0][0];
		var newContainerHeight= layOutList[layoutNumber][0][1];
		var newGraphWidth = layOutList[layoutNumber][1][0];
		var newGraphHeight= layOutList[layoutNumber][1][1];
		var newGraphmSecWidth= layOutList[layoutNumber][1][2];
		var newGraphuVoltLow = layOutList[layoutNumber][1][3];
		var newGraphuVoltHigh= layOutList[layoutNumber][1][4];
		set12GraphSize(newGraphWidth, newGraphHeight);			
		WAVEFORM_zoomGraphX(StartmSec, StartmSec+newGraphmSecWidth);
		
		for(g=0;g<12;g++){
			var graphContainerDivName = "ContainerDiv" + g;
			var graphContainerDiv = WAVEFORM_getElementByIdEndsWith("div", graphContainerDivName);
			var currentLeft = graphContainerDiv.style.left;
			var currentTop = graphContainerDiv.style.top;
			
			currentLeft = currentLeft.substr(0,currentLeft.indexOf("px"));
			currentTop = currentTop.substr(0,currentTop.indexOf("px"));

			// graph container positions start with the 3rd element of the layout array
			var newLeft=layOutList[layoutNumber][g+2][0];
			var newTop=layOutList[layoutNumber][g+2][1];
			
			//set graph container's sizes and positions
			graphContainerDiv.style.width = newContainerWidth+"px";
			graphContainerDiv.style.height = newContainerHeight+"px";
			moveGraphNow(g, newLeft, newTop);
			
//			var incLeft = (currentLeft - newLeft)/stepCount;
//			var incTop = (currentTop - newTop)/stepCount;
//			moveSlowly(g, newLeft, newTop, incLeft, incTop);
		}
	};

	var moveGraphNow = function(graphNumber, newLeft, newTop, namespace){
		var graphContainerDivName = namespace + ":ContainerDiv" + graphNumber;
		var graphContainerDiv = document.getElementById(graphContainerDivName);
		graphContainerDiv.style.left=newLeft + "px";
		graphContainerDiv.style.top=newTop + "px";		
	};
	
	var moveSlowly = function(graphNumber, newLeft, newTop, incLeft, incTop, namespace){
		var graphContainerDivName = namespace + ":ContainerDiv" + graphNumber;
		var graphContainerDiv = document.getElementById(graphContainerDivName);
		var currentLeft = graphContainerDiv.style.left;
		var currentTop = graphContainerDiv.style.top;
		
		currentLeft = currentLeft.substr(0,currentLeft.indexOf("px"));
		currentTop = currentTop.substr(0,currentTop.indexOf("px"));

		if(currentLeft < newLeft) {
			currentLeft = currentLeft-incLeft;
			graphContainerDiv.style.left=currentLeft + "px";
		}
			
		if(currentTop < newTop){ 
			currentTop = currentTop-incTop;
			graphContainerDiv.style.top=currentTop + "px";		
		}
		
		if((currentLeft != newLeft) | (currentLeft != newTop)){
			setTimeout("moveSlowly(" + g + "," + newLeft + "," + newTop + "," + incLeft + "," + incTop + ")",5);
		}
	};	

//	var animatedChangeLayout = function(droplist, namespace){
//		var x=droplist.selectedIndex;
//		var optionList=droplist.options;
//		var newLayout = optionList[x];
//		var newLayoutElementName = namespace + ":" + optionList[x];
//		var newLayoutElement = document.getElementById(newLayoutElementName);
//		alert("Phenotype - name:" + newLayout.label + " value:" + newLayout.value);
//	};

	var setGraphLabel = function(graphCount){		
		for(var i=0;i<12;i++){
//	       	var title = document.getElementById(namespaceGlobal + ":TitleDiv"+i);
	       	var title = WAVEFORM_getElementByIdEndsWith("span", "TitleDiv"+i);
//	       	alert("saGraphTitleArray[" + i + "]: " + saGraphTitleArray[i]);
        	title.innerHTML = saGraphTitleArray[i];
		}
	};
	
	var stretchToContentMulti = function(){
		var extraSpaceForVScroll = 25; 
		// lookup all the relevent widths
		var contentArea = WAVEFORM_getElementByIdEndsWith("div", "ecgGraphLayout");
		var contentWidth = parseInt(contentArea.clientWidth) - extraSpaceForVScroll;  // removes the "px" at the end
		
		var graphContainer = WAVEFORM_getElementByIdEndsWith("table", "Container_12LeadDivOutside");
		var gcWidth = parseInt(graphContainer.clientWidth);  // removes the "px" at the end
		
		
		var graphZero = WAVEFORM_getElementByIdEndsWith("div", "graphDiv0");
		var g0Width = parseInt(graphZero.clientWidth);  // removes the "px" at the end
		var g0Height = parseInt(graphZero.clientHeight);  // removes the "px" at the end

		var calZero = WAVEFORM_getElementByIdEndsWith("div", "graphDivCal0");
		var c0Width = parseInt(calZero.clientWidth);  // removes the "px" at the end
		var c0Height = parseInt(calZero.clientHeight);  // removes the "px" at the end

		// calculations
		var nonGraphWidth =(gcWidth -(g0Width*4) - c0Width);
		var AvailableGraphWidth = contentWidth - nonGraphWidth;
		var zoomRatio = AvailableGraphWidth/(4*g0Width + c0Width); // Largest graph width : current graph width
		
		// Set graph sizes
		resizeMultiGraphParts(g0Width*zoomRatio, g0Height*zoomRatio, c0Width*zoomRatio, c0Height*zoomRatio);
		
//		resizeAllGraphs(g0Width*zoomRatio, g0Height*zoomRatio);
//		resizeAllCallibrations(c0Width*zoomRatio, c0Height*zoomRatio);
//		resizeAllSliders(g0Height*zoomRatio);
//		
//		var gcHeight = parseInt(graphContainer.clientHeight);  // uses the new height after graphs have been resized.
//		document.getElementById(namespaceGlobal + ":voltCenterALL").style.height = parseInt(gcHeight) + "px";
	};
	
	var resizeMultiGraphParts = function (graphWidth, graphHeight, calWidth, calHeight){
		// Set graph sizes
		resizeAllGraphs(graphWidth, graphHeight);
		resizeAllCallibrations(calWidth, calHeight);
//		resizeAllSliders(graphHeight);
		repositionAllCrosshairs();
		
		// uses the new graph container's height after graphs have been resized.
//		var gcHeight = parseInt(document.getElementById(namespaceGlobal + ":Container_12LeadDivOutside").clientHeight); 
		var gcHeight = graphHeight * 3;
		WAVEFORM_getElementByIdEndsWith("div", "voltCenterALL").style.height = parseInt(gcHeight) + "px";
	};
	
	
//**************************** utility functions *******************************
	//** Resizing functions ********************
	/** Change the size of all graphs to the specified width & height. */
	var resizeAllGraphs = function(graphWidthPx, graphHeightPx){
		for(var gs=0;gs<graphSet.length;gs++){
			graphSet[gs].resize(graphWidthPx, graphHeightPx);
		}
	};
	
	/** Change the size of all Calibration graphs to the specified width & height. */
	var resizeAllCallibrations = function(graphWidthPx, graphHeightPx){
		for(var gcs=0;gcs<graphCalSet.length;gcs++){
			graphCalSet[gcs].resize(graphWidthPx, graphHeightPx);
		}
	};
	
	/** Change the height of all voltage position sliders to the specified height. */
	var resizeAllSliders = function(graphHeightPx){
		for (var s=0;s<graphSet.length;s++){
//			var sliderName = namespaceGlobal + ":slider" + s;
			var oSlider = WAVEFORM_getElementByIdEndsWith("div", slider + s);
			oSlider.style.height = parseInt(graphHeightPx) + "px";
		}
	};
	
	/** Change the vertical position of the cross-hairs relative to the graph containiner. 
	 * The crosshair's top starts at the bottom of the graphDiv, so it must be moved up by the height of the graphDiv **/
	var repositionAllCrosshairs = function(){
		var graphZero = WAVEFORM_getElementByIdEndsWith("div", "graphDiv0");
		var g0Height = parseInt(graphZero.clientHeight);  // removes the "px" at the end

		for(var xL=0;xL < xLineSet.length;xL++){	
			var xLineTemp = xLineSet[xL];
			var xLineID = xLineTemp.id; // should be the same as array index "xL"
			
			xLineTemp.style.top = "-" + g0Height + "px";
		}
	}
	//** end of Resizing functions ********************