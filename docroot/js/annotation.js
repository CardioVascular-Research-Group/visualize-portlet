/**
 * Adds appropriate ticks on the y-axis.
 * @param {Number} minY The minimum Y value in the data set
 * @param {Number} maxY The maximum Y value in the data set
 * @private
 */

//Dygraph.prototype.addYTicks_ = function(minY, maxY) {
//  // Set the number of ticks so that the labels are human-friendly.
//  // TODO(danvk): make this an attribute as well.
//  var formatter = this.attr_('yAxisLabelFormatter') ? this.attr_('yAxisLabelFormatter') : this.attr_('yValueFormatter');
////  var ticks = Dygraph.numericTicks(minY, maxY, this, formatter);
//  var ticks = CVRG_yTicker(minY, maxY, this, formatter);
//  
//  this.layout_.updateOptions( { yAxis: [minY, maxY],
//                                yTicks: ticks } );
//};


// Event handlers and callbacks for annotation demo, derived from http://danvk.org/dygraphs/tests/annotation.html
// Dygraph Annotations documentation: http://danvk.org/dygraphs/annotations.html      
//******************************************************
	var data=[];
	var CVRG_last_ann = 0; // counter: Last annotation added to this ecg chart.
	var CVRG_ecg_div_width = 30; // width of the 
	var ecg_graph = "not a graph";
	var CVRG_timeLabelPrefix = "Time";
	var CVRG_sSecondSuffix = "Sec";
	var CVRG_sampleRate = 1000; // samples per second, used to decide at which zoom level to draw dots
	var CVRG_isIE=false;
	var namespaceLocal = namespaceGlobal;
	
	var testJSNI = function(){
		alert('Testing JSNI with an alert box annotation.js');
	};

	var drawECGCallCount = 0;
	var leadVisibility = "";
	/** Creates a dygraph inside the div "ecg_div", with labels in the div "status_div" showing the data in the array "data".
	 * 
	 * @returns
	 */	
	var CVRG_drawECGgraph = function(){
		if(drawECGCallCount == 0){
			drawECGCallCount++;
			ecg_graph = new Dygraph( 
					document.getElementById("ecg_div"),
					data, 
					{
						stepPlot: false,
						labelsDiv: document.getElementById('status_div'),
						labelsDivStyles: { border: '1px solid black' },
						labelsSeparateLines: false,
						gridLineColor: '#FA8C8C',
						labelsKMB: true,
						axes: { 
							x: { 
								valueFormatter: CVRG_xValueFormatter2,
								ticker: CVRG_xTicker,
								axisLabelFormatter: CVRG_xAxisLabelFormatter2								
							}, 
							y: { 
								valueFormatter: CVRG_yValueFormatter2, 
								ticker: CVRG_yTicker, 
								axisLabelFormatter: CVRG_yAxisLabelFormatter2
							} 
						},
						annotationClickHandler:    CVRG_annotationClickHandler, 
						annotationDblClickHandler: CVRG_annotationDblClickHandler, 
						annotationMouseOverHandler:CVRG_annotationMouseOverHandler, 
						annotationMouseOutHandler: CVRG_annotationMouseOutHandler, 
						drawCallback:              CVRG_drawCallback, 
						pointClickCallback:        CVRG_pointClickCallback,
						zoomCallback:              CVRG_zoomCallback,
						
						highlightCallback: function(e, x, pts) {
							var x = document.getElementById("ecg_div").xpos;
							var y = document.getElementById("ecg_div").ypos;
							//var yOffset = 209;
							//var xOffset = 380;
							var yOffset = 0;
							var xOffset = 0;
							CVRG_highlightCallback(e, pts, yOffset, xOffset);
						},
						unhighlightCallback: function(e){
							CVRG_unhighlightCallback(e, ecg_graph.rawData_[0].length-1);
						},

						highlightCircleSize: 5,
						strokeWidth: 1,
						drawPoints: false,
						width: 640,
						height: 480,
						padding: {left: 1, right: 1, top: 5, bottom: 5}
						,
						interactionModel : { // custom interation model definition parameter
							'mousedown' : CVRG_mousedown,
							'mousemove' : CVRG_mousemove,
							'mouseup' : CVRG_mouseup
							//'click' : CVRG_mouseclick,
							//'dblclick' : CVRG_mousedblClick,
							//'mousewheel' : CVRG_mousescroll
						}
						
					}
			);
			
//			var leadCount = ecg_graph.rawData_[0].length-1;
//			CVRG_InitHorizontalLines(leadCount, "ecg_div");
//			CVRG_InitVerticalLines("ecg_div");
		}
	};
	

     // Ver 2 
    
    
    // Original Dygraph Single Lead Display by Mike Below.
    
	var CVRG_drawECGgraph2 = function(){
		if(drawECGCallCount == 0){
			drawECGCallCount++;
			ecg_graph = new Dygraph( 
					document.getElementById("ecg_div"),
					data, 
					{
						stepPlot: false,
						labelsDiv: document.getElementById('status_div'),
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
						drawCallback:              CVRG_drawCallback, 
						pointClickCallback:        CVRG_pointClickCallback2,
						zoomCallback:              CVRG_zoomCallback,
						highlightCallback: function(e, x, pts) {
							var x = document.getElementById("ecg_div").xpos;
							var y = document.getElementById("ecg_div").ypos;
							// var yOffset = 209;
							// var xOffset = 380;
							var yOffset = 0;
							var xOffset = 0;
							CVRG_highlightCallback(e, pts, yOffset, xOffset);
						},
						unhighlightCallback: function(e){
							CVRG_unhighlightCallback(e, ecg_graph.rawData_[0].length-1);
						},
						visibility: [true, false, false, false, false, false, false, false, false, false, false, false, ],
						highlightCircleSize: 5,
						strokeWidth: 1,
						drawPoints: false,
						width: 640,
						height: 480,
						padding: {left: 1, right: 1, top: 5, bottom: 5},
						
						dateWindow: [0,2500], // Start and End times in milliseconds
						interactionModel : { // custom interation model definition parameter
							'mousedown' : CVRG_mousedown2,
							'mousemove' : CVRG_mousemove2,
							'mouseup' : CVRG_mouseup2

						}
						
					}
			);
		}
	};

	
// Ver 3
	

// RSA highlight Area:
	

// Ver 3
  

//// This is the Single TEST Lead Dygraph Display RSA 04/10/2013 
//	
//	var CVRG_drawECGgraphSingle = function(){
//		if(drawECGCallCount == 0){
//			drawECGCallCount++;
//			ecg_graph = new Dygraph( 
//					document.getElementById("ecg_div"),
//					parent.data, 
//					{
//						stepPlot: false,
//						labelsDiv: document.getElementById('status_div'),
//						labelsDivStyles: { border: '1px solid black' },
//						labelsSeparateLines: false,
//						gridLineColor: '#FA8C8C',
//						labelsKMB: true,
//						axes: { 
//							x: { 
//								valueFormatter: CVRG_xValueFormatter2,
//								axisLabelFormatter: CVRG_xAxisLabelFormatter2,
//								ticker: CVRG_xTicker 
//							}, 
//							y: { 
//								valueFormatter: CVRG_yValueFormatter2,
//								axisLabelFormatter: CVRG_yAxisLabelFormatter2, 
//								ticker: CVRG_yTicker 
//							} 
//						},
//						annotationClickHandler:    CVRG_annotationClickHandler, 
//						annotationDblClickHandler: CVRG_annotationDblClickHandler, 
//						annotationMouseOverHandler:CVRG_annotationMouseOverHandler, 
//						annotationMouseOutHandler: CVRG_annotationMouseOutHandler, 
//						drawCallback:              CVRG_drawCallback, 
//						pointClickCallback:        CVRG_pointClickCallback2,
//						zoomCallback:              CVRG_zoomCallback,
//						
//						highlightCallback: function(e, x, pts) {
//							var x = document.getElementById("ecg_div").xpos;
//							var y = document.getElementById("ecg_div").ypos;
//							var yOffset = 209;
//							var xOffset = 380;
//							//var yOffset = 0;
//							//var xOffset = 0;
//							CVRG_highlightCallback(e, pts, yOffset, xOffset);
//						},
//						unhighlightCallback: function(e){
//							CVRG_unhighlightCallback(e, ecg_graph.rawData_[0].length-1);
//						},
//						visibility: [true, false, false, false, false, false, false, false, false, false, false, false, ],
//						highlightCircleSize: 5,
//						strokeWidth: 1,
//						drawPoints: false,
//						padding: {left: 1, right: 1, top: 5, bottom: 5},
//			            showRangeSelector: true,
//			            rangeSelectorHeight: 50,
//			            rangeSelectorPlotStrokeColor: 'black',
//			            rangeSelectorPlotFillColor: 'lightblue',
//						//dateWindow: [0,2500], // Start and End times in milliseconds
//						interactionModel : {  // custom interation model definition parameter
//							'mousedown' : CVRG_mousedown2,
//							'mousemove' : CVRG_mousemove2,
//							'mouseup' : CVRG_mouseup2
//
//				      }
//						
//				}
//			);
//		}
//	};
//	
	// Highlight one portion out of line. RSA 041113
    var highlight_start = 0;          // 480 dataSX now - set to the Current X Start javascript Var
    var highlight_end = 0; 			  // 710  dataECoords[0] - now set to the Current X End javascript Var dataECoords[0] 
    var CenterArea = 0;
    var CVRG_setHightLightLocation = function(X1, XC ,X2){
    	highlight_start = X1;
    	toCenterWithArea = XC;
    	highlight_end = X2;
    	CVRG_underlayCallbackClicked();  // gathers the call to set the color on the interval
    };
	

    // Set the Highlight on the interval
    function CVRG_underlayCallbackClicked(highlight_start, toCenterWithArea, highlight_end){
    	ecg_graph.updateOptions({
    		underlayCallback:   CVRG_underlayCallback
    	});
    };
	
	
 // Set the Highlight on the interval
	var CVRG_highlightStartEnd = function(){
    	alert(highlight_end - highlight_start / 2 );
    };
	    
	    // Highlight one portion out of line. RSA 041113
		var CVRG_underlayCallback = function(canvas, area, g) {
        var bottom_left = g.toDomCoords(highlight_start, -20);
        var top_right = g.toDomCoords(highlight_end, +20);

        var left = bottom_left[0];
        var right = top_right[0];
        var centerLength = highlight_start;
        var toLineAreaFill = toCenterWithArea;
        
        canvas.fillStyle = "rgba(111, 242, 254, 0.8)";
        canvas.fillRect(left, area.y, right - left, area.h);

//  ON HOLD  // Blue Highlight  canvas.fillStyle = "rgba(137, 245, 255, 1.0)";
        	// canvas.fillStyle = "rgba(111, 242, 254, 1.0)";
//			//  Yellow Highligh     canvas.fillStyle = "rgba(255, 255, 102, 1.0)";        
//        canvas.beginPath();
//        canvas.strokeStyle = "red";
//        canvas.lineWidth = 1;
//        canvas.moveTo(centerLength, 200);
//        canvas.lineTo(toLineAreaFill  , 200);
//        canvas.stroke();
//
//        canvas.beginPath();
//        canvas.strokeStyle = "rgb(204,0,0)";
//        canvas.lineWidth = 1;
//        canvas.moveTo(420, 200);
//        canvas.lineTo(550, 200);
//        canvas.stroke();

// Area of interval selection

//       alert("highlight_start: " + highlight_start);
//       alert("toCenterWithArea: " + toCenterWithArea);

    

      };


	
	var CVRG_clearECGgraph = function(){
		drawECGCallCount = 0;
		ecg_graph = "not a graph";
	};

//	var getecg_graph = function(dMVRange, pixelsPerXLabel,  pixelsPerYLabel){
//	ecg_graph = new Dygraph( 
//	document.getElementById("ecg_div"),
//	data, 
//	{
//	labelsDiv: document.getElementById('status'),
//	labelsDivStyles: { border: '1px solid black' },
//	labelsSeparateLines: true,
//	visibility: [true, true, true, false, false, false, false, false, false, false, false, false],
//	gridLineColor: '#FF0000',
//	pixelsPerXLabel: pixelsPerXLabel,
//	pixelsPerYLabel: pixelsPerYLabel,
//	xValueFormatter: function(x) {
//	var shift = Math.pow(10, 5)
//	return   Math.round(x * shift) / shift + "(Seconds)"
//	},
//	xAxisLabelFormatter: function(x) {
//	var shift = Math.pow(10, 5)
//	return   Math.round(x * shift) / shift 
//	},
//	yValueFormatter: function(x) {
//	var shift = Math.pow(10, 5)
//	return   Math.round(x * shift) / shift +  "(uV)"
//	},
//	yAxisLabelFormatter: function(x) {
//	var shift = Math.pow(10, 5)
//	return  Math.round(x * shift) / shift 
//	},
//	valueRange: [-dMVRange,dMVRange],
//	pointClickCallback:        CVRG_pointClickCallback,			         
//	annotationClickHandler:    CVRG_annotationClickHandler,
//	annotationDblClickHandler: CVRG_annotationDblClickHandler,
//	annotationMouseOverHandler:CVRG_annotationMouseOverHandler,
//	annotationMouseOutHandler: CVRG_annotationMouseOutHandler,         
//	drawCallback: CVRG_drawCallback 
//	}
//	);

//	return g;
//	};

	var saveBg = '';
	var num = 0;
//	ecg_graph.updateOptions( {
//	annotationClickHandler:    CVRG_annotationClickHandler,
//	annotationDblClickHandler: CVRG_annotationDblClickHandler,
//	annotationMouseOverHandler:CVRG_annotationMouseOverHandler,
//	annotationMouseOutHandler: CVRG_annotationMouseOutHandler,         
//	pointClickCallback:        CVRG_pointClickCallback,
//	drawCallback: CVRG_drawCallback 
//	});

	// creates a unique name of a single annotation to be used as an Element Id.
	var nameAnnotation = function(ann) {
		return  ann.series + "_" + ann.x + "_" + ann.y;
	};

	var CVRG_setAxisFormatters = function(){
		ecg_graph.updateOptions({
			axes: { 
				x: { 
					valueFormatter: CVRG_xValueFormatter2, // function(val, opts, series_name, dygraph) { ... }, 
					axisLabelFormatter: CVRG_xAxisLabelFormatter2, // function(val, granularity, opts, dygraph) { ... } 
					ticker: CVRG_xTicker  // function(minTms, maxTms, pixels, axis_props, self, forced_vals)  
				}, 
				y: { 
					valueFormatter: CVRG_yValueFormatter2, // function(val, opts, series_name, dygraph) { ... }, 
					axisLabelFormatter: CVRG_yAxisLabelFormatter2, // function(val, granularity, opts, dygraph) { ... } 
					ticker: CVRG_yTicker // function(minTms, maxTms, pixels, axis_props, self, forced_vals) 
				} 
			}  			
		});
	};
	
	// Scott Alger Mike Shipway 030713
	var CVRG_ontologyTreeSelectionChanged = function(nodeID, nodeName) {

		// MOVED TO ontologyTreeAPI
		
		//	alert("ID " + nodeID + "Name" + nodeName);
		// CVRG_ontologyTreeSelectionChangedJSF(nodeID, nodeName);

	};
	
	
	// called by treeSelectionChanged in ontologyTreeAPI.js when the tree selection changes.
	var CVRG_ontologyTreeSelectionChanged_Old = function(nodeID, nodeName) {
		
	
		
		CVRG_ontologyTreeSelectionChangedJSNI(nodeID, nodeName);
//		CVRG_ontologyTreeSelectionChangedJSNI("80","89");
		//alert("CVRG Message: Java code CVRG_ontologyTreeSelectionChangedJSNI called.");
	};

	var CVRG_annotationClickHandler = function(ann, point, dg, event) {
		CVRG_annotationClickHandlerJSNI(point.xval, point.yval);
		//eventDiv.innerHTML += "click: " + nameAnnotation(ann)  + "<br/>";
	};
	var CVRG_annotationDblClickHandler = function(ann, point, dg, event) {
		CVRG_annotationDblClickHandlerJSNI(point.xval, point.yval);
		//eventDiv.innerHTML += "dblclick: " + nameAnnotation(ann) + "<br/>";
    };
    

    //Sends a selected interval to the Java code for creating an annotation.
	var CVRG_annotationIntervalSelected = function(mSecStart, mVoltStart, mSecEnd, mVoltEnd) {
		CVRG_annotationIntervalSelectedJSNI(mSecStart, mVoltStart, mSecEnd, mVoltEnd);
		//eventDiv.innerHTML += "click: " + nameAnnotation(ann)  + "<br/>";
	};
    
    
	// Highlight the annotation in the list when the square is moused over.
	var CVRG_annotationMouseOverHandler = function(ann, point, dg, event) {
		CVRG_annotationMouseOverHandlerJSNI(nameAnnotation(ann), point.xval, point.yval);
		document.getElementById(nameAnnotation(ann)).style.fontWeight = 'bold';
		saveBg = ann.div.style.backgroundColor;
		ann.div.style.backgroundColor = '#ddd';
	};
	// un-Highlight the annotation in the list when the square is moused out.
	var CVRG_annotationMouseOutHandler = function(ann, point, dg, event) {
		//CVRG_annotationMouseOutHandlerJSNI(point.xval, point.yval);
		document.getElementById(nameAnnotation(ann)).style.fontWeight = 'normal';
		ann.div.style.backgroundColor = saveBg;
	};
		
	//event -  the event object for the click 
	// p  - a point on one of the graphs that was clicked.
	var CVRG_pointClickCallback = function(event, p) {
		// Check if the point is already annotated.
		//alert(p.name + " annotation at: " + p.xval + " seconds," + p.yval + " milliVolts.");
		//testJSNI(p.name, p.xval, p.yval);
		//openAnnotationPopup(p.name, p.xval, p.yval);
		if (p.annotation){
			alert("CVRG Message: " + p.name + " already has an annotation at: " + p.xval + " seconds.");
//			return;
		}	
		CVRG_pointClickHandlerJSNI(p.xval, p.yval);
		// If not, add one.
//		CVRG_addAnnotation (p.name, p.xval, p.yval, num, "Fake Annotation at: " + p.xval + ", " + p.yval, "fake fullAnnotation")
//		ecg_graph.setAnnotations(tempAnnotations);
//		var ann = {
//			series: p.name,
//			x: p.xval,
//			y: p.yval,
//			tickHeight: 10,
//			shortText: num,
//			text: p.xval + ", " + p.yval
//		};

//		var anns = ecg_graph.annotations();
//		// alert(point_info(p));
//		anns.push(ann);
//		ecg_graph.setAnnotations(anns);

		num++;
	};
	
	//event -  the event object for the click 
	// p  - a point on one of the graphs that was clicked.
	var CVRG_pointClickCallback2 = function(event, p) {
		CVRG_unhighlightCrosshairs(12);
		// Check if the point is already annotated.
		// RSA 4/10/13/ 3:58pm 
		// alert(p.name + " annotation at: " + p.xval + " seconds," + p.yval + " milliVolts.");
		if (p.annotation){
			alert("CVRG Message: " + p.name + " already has an annotation at: " + p.xval + " seconds.");
		}
		
		bar.show();  // Scott Alger Primefaces dropdown menu SA 1/17/2013
		
		// off for testing SA 012213
		// alert(" annotation.js - openAnnotationPopup(  " + p.xval  +", " + p.yval  +") ");
		// CVRG_pointClickHandlerJSNI(p.xval, p.yval);

		num++;
	
	};
	
	
	var tempAnnotations=[]; // temporarily holds the annotation data between CVRG_addAnnotation and CVRG_setLeadVisibility calls.
	var CVRG_step = true;
	
	// Adds the annotation's details to the list.  Also generates a unique ID for each annotation so that Javascript can bold/unbold. 
	var CVRG_drawCallback = function(ecg_graph) {
		var leadCount = ecg_graph.rawData_[0].length-1;
		//CVRG_InitHorizontalLines(leadCount, "ecg_div");
		//CVRG_InitVerticalLines("ecg_div", namespace);

		/*
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
		// document.getElementById("list_div").innerHTML = html;
		*/
//		var bDots=false;
//		if(CVRG_MsPerPixel < 0.1){
//			bDots = true;
//		}
//		ecg_graph.updateOptions({
//			drawPoints: bDots
//		});

	};
//-------------------------
	var CVRG_resetAnnotations = function(){
	// alert("Received resetAnnotations command - Mike Shipway - Scott Alger 011713 ");
		//var annnotations = [];
		tempAnnotations=[];
		CVRG_last_ann = 0;
		//ecg_graph.setAnnotations([]);
	};
	

	var CVRG_addAnnotationHeight = function(series, x, y, flagLabel, ontologyId, fullAnnotation, height){
		// alert("Received addAnnotation command series: " + series + " x: " + x  + " y: " + y  + " flagLabel: " + flagLabel + " ontologyId: " + ontologyId + " fullAnnotation: " + fullAnnotation);
		//var anns = ecg_graph.annotations();
		//alert("ecg_graph:" + ecg_graph + " CVRG_last_ann: " + CVRG_last_ann + " anns.length: " + anns.length);
		//alert("before: " + anns);
		var ann = {
			series: series,  // series, // lead name
			x: x, // milliseconds
			y: y, // millivolts,  CVRG extra data, not used by dygraphs, displayed when mousing over annotation list.
			shortText: flagLabel, // text to show in the flag
			text: ontologyId, // will appear when mouse hovers over flag
			fullAnnotation: fullAnnotation, // CVRG extra data, not used by dygraphs, displayed when mousing over annotation list.
			tickHeight: height
		};
		
		//anns.push(ann);
		tempAnnotations.push(ann);
		// alert(tempAnnotations.length + ") series: |" + tempAnnotations[tempAnnotations.length-1].series + "| text: |" + tempAnnotations[tempAnnotations.length-1].text + "|");
		
//		alert(anns.length + ") series: |" + anns[anns.length-1].series + "| text: |" + anns[anns.length-1].text + "|");
		// ecg_graph.setAnnotations(anns);
		CVRG_last_ann++; // redundent counter

		return CVRG_last_ann;
	};


	// Marking the Middle of The Interval Annotation
	
	var CVRG_addAnnotationInterval = function(series, x, y, flagLabel, ontologyId, fullAnnotation, height, width){
		// alert("Received addAnnotation command series: " + series + " x: " + x  + " y: " + y  + " flagLabel: " + flagLabel + " ontologyId: " + ontologyId + " fullAnnotation: " + fullAnnotation);
		//var anns = ecg_graph.annotations();
		//alert("ecg_graph:" + ecg_graph + " CVRG_last_ann: " + CVRG_last_ann + " anns.length: " + anns.length);
		//alert("before: " + anns);
		var ann = {
			series: series,  // series, // lead name
			x: x, // milliseconds
			y: y, // millivolts,  CVRG extra data, not used by dygraphs, displayed when mousing over annotation list.
			shortText: flagLabel, // text to show in the flag
			text: ontologyId, // will appear when mouse hovers over flag
			fullAnnotation: fullAnnotation, // CVRG extra data, not used by dygraphs, displayed when mousing over annotation list.
			tickHeight: height,
			width: width  // CVRG extra width to the Flag - RSA 
		};
	
		//anns.push(ann);
		tempAnnotations.push(ann);
		// alert(tempAnnotations.length + ") series: |" + tempAnnotations[tempAnnotations.length-1].series + "| text: |" + tempAnnotations[tempAnnotations.length-1].text + "|");
		
//		alert(anns.length + ") series: |" + anns[anns.length-1].series + "| text: |" + anns[anns.length-1].text + "|");
		// ecg_graph.setAnnotations(anns);
		CVRG_last_ann++; // redundent counter

		return CVRG_last_ann;
	};

	
	
	var CVRG_addAnnotation = function(series, x, y, flagLabel, ontologyId, fullAnnotation){
		return CVRG_addAnnotationHeight(series, x, y, flagLabel, ontologyId, fullAnnotation, 10);
	};

	// lead - integer 
//	var CVRG_setLeadVisibilityOLD = function(lead, bVisible){
//		//set all 3 leads invisible
//		ecg_graph.setVisibility(0, false);
//		ecg_graph.setVisibility(1, false);
//		ecg_graph.setVisibility(2, false);
//		if((lead==3) & (bVisible)){
//			ecg_graph.setVisibility(0, true);
//			ecg_graph.setVisibility(1, true);
//			ecg_graph.setVisibility(2, true);
//		}else{
//			ecg_graph.setVisibility(lead, bVisible);
//		}
//		ecg_graph.setAnnotations(tempAnnotations);
//	};
	
	// set the visibility of one lead and show the associated annotations.
	var CVRG_setLeadVisibility = function(lead, bVisible){
//		 alert("Setting Lead:" + lead + " to " + bVisible);
		if(ecg_graph != "not a graph"){
			ecg_graph.setVisibility(lead, bVisible);
			ecg_graph.setAnnotations(tempAnnotations);
		}
	};
	
	// show the associated annotations  01 17 13 MS SA
	var CVRG_showAnnotations = function() {
		
		ecg_graph.setAnnotations(tempAnnotations);
		
	//	alert("Setting 1 Scott Alger 1/17/13");
		
	};

	//------------------------ formatting --------------------
    
	/** formats a number to be the coefficient(mantissa) for Exponential notation.
	 * @param num - number to format
	 * @param exponent - power of 10 which the return value will be multiplied by.
	 * @param digitsAfterDecimal - number of digits to preserve after the decimal point, the last digit being rounded.
	 */ 
	var CVRG_formatExponential = function(num, exponent, digitsAfterDecimal){
		var label = "foo";
		var coefficient = num/Math.pow(10, exponent);
		
		// Round up to the requested number of decimal places.
		label = Dygraph.round_(coefficient, digitsAfterDecimal);
		
		return label;
	};

	// formats a number in powers of 1000
//	var CVRG_formatKMG = function(num){
//		var label = "foo";
//		var k = 1000;
//		var k_labels = [ "k", "m", "B", "T" ];
//		var absnum = Math.abs(num);
//		// Round up to an appropriate unit.
//		var n = k*k*k*k;
//		for (var j = 3; j >= 0; j--, n /= k) {
//			if (absnum >= n) {
//				label = Dygraph.round_(num / n, 1) + k_labels[j];
//				break;
//			}
//		}
//
//		return label;
//	};
//	
	
	
	/** controls the display of numbers on the axes (i.e. tick marks).
	 * 
	 * @param val = the value
	 * @param granularity = How fine- or coarse-grained the axis is (i.e. hourly, daily, weekly, ...) 
	 * @param opts = a function which maps options to their values
	 * @param dygraph = the dygraph object
	 */
	var CVRG_xAxisLabelFormatter2 = function(val, granularity, opts, dygraph) {
		return CVRG_xAxisLabelFormatter(val);  // calling the function written for the older dygraphs library.
	};
		
			
	// Function to call to format values along the x axis. 
	// parameter:
	//     ms - time in milliseconds.
	var CVRG_xAxisLabelFormatter = function(ms) {
		var shift = Math.pow(10, 5);
		var intMS = Math.round(ms * shift) / shift;
		var dSec = intMS/1000;
		var label = CVRG_formatExponential(dSec,CVRG_TimeExponent,3);
		
		return label;
	};

	/** Controls the display of numbers in the legend 
	 * (i.e. the text that appears when you hover on the chart).
	 * 
	 * @param val = the value
	 * @param opts = a function which maps options to their values
	 * @param series_name = the name of the relevant series
	 * @param dygraph = the dygraph object
	 */ 
	var CVRG_xValueFormatter2 = function(val, opts, series_name, dygraph){
		return CVRG_xValueFormatter(val); // calling the function written for the older dygraphs library.
	};
	    
    /** Function to provide a custom display format the X value for mouseover. 
	 * @param x - time in milliseconds.
	 * 
	 * @returns - time in HH:MM:SS.sss format.
	 */
	var CVRG_xValueFormatter = function(x) {
		var result = "";
//		var shift = Math.pow(10, 5);
//		var msDuration = Math.round(x * shift) / shift;
//		var msRemainder = 0;
//		
//		var hours = Math.floor(msDuration/3600000); // ms/hour
//		msRemainder = msDuration-(hours*3600000);
//		var minutes = Math.floor(msRemainder/60000); // ms/minute
//		msRemainder = msRemainder-(minutes*60000);
//		var seconds =  Math.floor(msRemainder/1000); // ms/second
//		var milliseconds = msRemainder - (seconds*1000);
//		
//		if(hours>0){
//			result = hours + ":";
//			if (minutes<10) result += "0";
//			if (minutes==0) result += "0:";
//		}
//		if(minutes>0){
//			result += minutes + ":";
//			if(seconds<10) result += "0";
//			if(seconds==0) result += "0.";
//		}
//		if(seconds>0){
//			result += seconds + ".";			
//			if(milliseconds<100) result += "0";
//			if(milliseconds<10) result += "0";
//			if(milliseconds==0) result += "0";
//		}
//		if(milliseconds>0) result += milliseconds;
		
//		result = x/1000; // Rai wanted to see it as raw seconds rather than HH:MM:SS.sss format.
		result = formatMsecToExponent(x);
// this is the LEAD		
		return "<br>" + result + "<br>Lead";
		
	
		
	// RSA 012813	return "[Time (seconds)]:</BR> Lead : </BR> microVolts</BR>[" + result + "]";
		
	};
	
	/** controls the display of numbers on the axes (i.e. tick marks).
	 * 
	 * @param val = the value
	 * @param granularity = How fine- or coarse-grained the axis is (i.e. hourly, daily, weekly, ...) 
	 * @param opts = a function which maps options to their values
	 * @param dygraph = the dygraph object
	 */
	var CVRG_yValueFormatter2 = function(val, granularity, opts, dygraph) {
		return CVRG_yValueFormatter(val);  // calling the function written for the older dygraphs library.
	};

	// Function to provide a custom display format for the Y value for mouseover. 
	// parameter:
	//     y - value of the sample in millivolts.
	var CVRG_yValueFormatter = function(y) {
		var shift = Math.pow(10, 5);
		return "<br> Amplitude (μV)  " +  Math.round(y * shift) / shift;
	};
	
	
	
	/** controls the display of numbers on the axes (i.e. tick marks).
	 * 
	 * @param val = the value
	 * @param granularity = How fine- or coarse-grained the axis is (i.e. hourly, daily, weekly, ...) 
	 * @param opts = a function which maps options to their values
	 * @param dygraph = the dygraph object
	 */
	var CVRG_yAxisLabelFormatter2 = function(val, granularity, opts, dygraph) {
		return CVRG_yAxisLabelFormatter(val);  // calling the function written for the older dygraphs library.
	};

	// Function used to format values along the Y axis. By default it uses the same as the yValueFormatter unless specified.
	// parameter:
	//     y - value of the sample in millivolts.
	var CVRG_yAxisLabelFormatter = function(y) {
		var mvRemainder = 0;

		var shift = Math.pow(10, 5);
		var mv = Math.round(y * shift) / shift;

		if ( (Math.floor(mv/500)*500) == mv ){ // Large GridSquare
			result = mv/1000;
		}else
		{
			var volts = Math.floor(mv/1000);
			mvRemainder = mv - volts*1000;
			
			result = mvRemainder/1000;
		}		
		
		return mv;
	};
	
	
	var CVRG_TimeExponent = 0;
	/** Sets the global variable CVRG_TimeExponent for the exponent used on the time axis labels.
	 * Will be used to modify the time axis label.
	 * 
	 * @param msMinTime - time, in milliseconds to calculate the Seconds' exponent from.
	 * @param chart - the dygraph chart object which the value refers to.
	 * @returns void
	 */
	var CVRG_setTimeExponent = function(msMinTime, chart){
		var secMinTime = msMinTime/1000;
		var temp_TimeExponent=-1;
		for (var i = 0; i < 10; i++) {
			if(secMinTime/Math.pow(10, i) < 10){
				temp_TimeExponent = i;
				break;
			}
		}
		
		// runs only once per scale change to avoid infinate recursion loop.
		if(temp_TimeExponent != CVRG_TimeExponent){
			CVRG_TimeExponent = temp_TimeExponent;
		}
	};
	
	var CVRG_MsPerPixel = 1 ;
	/** Sets the global valiable CVRG_MsPerPixel for the number of millisecond between one pixel and the next.
	 * Will be used to decide when to display the data points as dots on the line (e.g. when zoomed in far enough)
	 * 
	 * @param msPerPixel - value to be persisted.
	 * @param chart - the dygraph chart object which the value refers to.
	 * @returns void
	 */
	var CVRG_setMsPerPixel = function(msPerPixel, chart){
		//TODO: call Java code to adjust the display
		if (CVRG_MsPerPixel != msPerPixel){
			CVRG_MsPerPixel = msPerPixel;
		}
	};
	

	/**
	 * Add ticks when the x axis has numbers on it (instead of dates)
	 * @param minTms - {Number} startDate Start of the date window (millis since epoch)
	 * @param  maxTms -{Number} endDate End of the date window (millis since epoch)
	 * @param pixels - the length of the axis in pixels.
	 * @param axis_props - provides access to chart- and axis-specific options. 
	 *       It can be used to access number/date formatting code/options, check for a log scale, etc.
	 * @param self - the Dygraph object for which an axis is being constructed.
	 * @param forced_vals - used for secondary y-axes.
	 * @return "tick list" {Array.<Object>} Array of {label, value} tuples.
	 * @public
	 */
	var CVRG_xTicker = function(minTms, maxTms, pixels, axis_props, self, forced_vals) {
		var pixelsPerTick = 60; // axis_props('x:pixelsPerLabel');
		return CVRG_TickerCommon(minTms, maxTms, pixels, axis_props, self, forced_vals, pixelsPerTick, 200, 40);
	};
	
	var CVRG_yTicker = function(minV, maxV, pixels, axis_props, self, forced_vals) {
		var pixelsPerTick = 30; // axis_props('y:pixelsPerLabel');
		return CVRG_TickerCommon(minV, maxV, pixels, axis_props, self, forced_vals, pixelsPerTick, 500, 100);
	};	

//	var CVRG_xTicker = function(minTms, maxTms, pixels, axis_props, self, forced_vals) {
//		return CVRG_TickerCommon(minTms, maxTms, pixels, axis_props, self, forced_vals);
//	};
//	
//	var CVRG_yTicker = function(minV, maxV, pixels, axis_props, self, forced_vals) {
//		return CVRG_TickerCommon(minV, maxV, pixels, axis_props, self, forced_vals);
//	};	

	/**
	 * Add ticks when the x axis has numbers on it (instead of dates)
	 * @param minV - minimum value of the window on this axis
	 * @param maxV - maximum value of the window on this axis
	 * @param self
	 * @param axis_props - properties array of this axis, if any.
	 * @param vals - Array of {label, value} tuples.
	 * @param pixelsPerLabelAttribute - either "pixelsPerXLabel" or "pixelsPerYLabel"
	 * 
	 * @return - Array of {label, value} tuples.
	 * @public
	 */
	
	var CVRG_TickerCommon_OLD = function(minV, maxV, pixels, axis_props, self, forced_vals, pixelsPerTick) {
		var msecWidth = maxV-minV; // values spanned by the graph area (voltage or time).
		var msecLargeTime=200; // time span between the dark grid lines 1/5 second (5 millimeter)on paper ECG, equal to 5 small time blocks
		var msecSmallTime=40;  // time span between the light grid lines 1/25 second (1 mm) on paper ECG
		var pixLargeTime = (pixels*msecLargeTime)/msecWidth; // pixels spanned by the dark grid lines (1/5 second)
		var msPerPixel = msecWidth/pixels; // milliseconds between one pixel and then next
		var pixSmallTime = (pixels*msecSmallTime)/msecWidth; // pixels spanned by the light grid lines (1/25 second)
		var pixMinSmallTime = 1;  // small time blocks must be at least this many pixels wide.
		
//		var bLabelsKMB = attr("labelsKMB");
		var ticks = [];
		
		CVRG_setTimeExponent(minV,self);
		CVRG_setMsPerPixel(msPerPixel, self);
		
		if (forced_vals) {
			for (var i = 0; i < forced_vals.length; i++) {
				ticks.push({v: forced_vals[i]});
			}
		} else {
			// ticks.length won't be 0 if the log scale function finds values to insert.
			if (ticks.length == 0) {
				// Basic idea:
				// Try labels every 1, 2, 5, 10, 20, 50, 100, etc.
				// Calculate the resulting tick spacing (i.e. this.height_ / nTicks).
				// The first spacing greater than pixelsPerYLabel is what we use.
				var mults = [1, 2, 5];
				var scale, low_val, high_val, nTicks;
				
				if (pixLargeTime < pixelsPerTick){	
					// default tick spacing
					for (var i = -10; i < 50; i++) {
						var base_scale = Math.pow(10, i);
						for (var j = 0; j < mults.length; j++) {
							scale = base_scale * mults[j];
							low_val = Math.floor(minV / scale) * scale;
							high_val = Math.ceil(maxV / scale) * scale;
							nTicks = Math.abs(high_val - low_val) / scale;
							var spacing = self.height_ / nTicks;
							// wish I could break out of both loops at once...
							if (spacing > pixelsPerTick) break;
						}
						if (spacing > pixelsPerTick) break;
					}
					for (var i = 0; i < nTicks; i++) {
						var tickV = low_val + i * scale;
						ticks.push( {v: tickV} );
					}

				}else{
					// draw thick and thin grid line as are seen in paper ECGs.
					if(pixSmallTime >= pixMinSmallTime){
						scale = 40;
					}else {
						scale = 200;
					}
					low_val = Math.floor(minV / scale) * scale;				
					high_val = Math.ceil(maxV / scale) * scale;
					nTicks = Math.abs(high_val - low_val) / scale;

					spacing = self.height_ / nTicks;
					
					// Construct the set of ticks.
					for (var i = 0; i < nTicks; i++) {
						var tickV = low_val + i * scale;
						var sec = Math.floor(tickV/1000); // integer seconds portion of tick time
						var msSubSec= tickV-(sec*1000); // milliseconds portion of tick time
						
						// dark grid lines are made up of a label on the value plus an unlabeled "darkening line" one pixel to either side.
						if(msSubSec==0){ // dark grid lines on whole seconds
							ticks.push( {label: "", v: tickV-msPerPixel} ); // don't label darking lines.
							ticks.push( {v: tickV} ); //  use default label on whole seconds
							ticks.push( {label: "", v: tickV+msPerPixel} ); // don't label darking lines.
						}else{
							if(msSubSec%200 > 1){// light grid lines on 25ths of a second, if between 5ths of a second lines.
								if (pixSmallTime > pixelsPerTick){
									ticks.push( {v: tickV} ); // use default label on fractional seconds
//									if(tickV > 0){
//										ticks.push( {label: msSubSec/1000.0, v: tickV} ); // only show the milliseconds
//									}else{
//										ticks.push( {label: (msSubSec-1000)/1000.0, v: tickV} ); // only show the milliseconds					
//									}
								}else{
									ticks.push( {label: "", v: tickV} ); // don't label small squares, overrides default label.
								}
							}else{ // dark grid lines on whole 5ths of a second
								ticks.push( {label: "", v: tickV-msPerPixel} );// don't label darking lines.
								ticks.push( {v: tickV} ); // use default label on fractional seconds
								ticks.push( {label: "", v: tickV+msPerPixel} );	// don't label darking lines.		
							}
						}
					}
				}
			}
		}

		// Get the formatter's name from properties.
		var formatter = axis_props('axisLabelFormatter');

		// Add default labels to the ticks which don't yet have labels defined.
		for (var i = 0; i < ticks.length; i++) {
			// create label if no text is found.
			if (ticks[i].label !== undefined)
				continue;  // Use current label.
			else{
				var tickV = ticks[i].v;
				var label = formatter(tickV, self);
				ticks[i].label = label;
			}
		}

		return ticks;
	};
	

	/**
	 * Add ticks when the x axis has numbers on it (instead of dates)
	 * @param minV - minimum value of the window on this axis
	 * @param maxV - maximum value of the window on this axis
	 * @param self
	 * @param axis_props - properties array of this axis, if any.
	 * @param vals - Array of {label, value} tuples.
	 * @param pixelsPerLabelAttribute - either "pixelsPerXLabel" or "pixelsPerYLabel"
	 * @param msecLargeTime=500; // time span between the dark grid lines 1/5 second (5 millimeter)on paper ECG, equal to 5 small time blocks
	 * @param msecSmallTime=100; // time span between the light grid lines 1/25 second (1 mm) on paper ECG
	 * 
	 * @return - Array of {label, value} tuples.
	 * @public
	 */
	var CVRG_TickerCommon = function(minV, maxV, pixels, axis_props, self, forced_vals, pixelsPerTick, msecLargeTime, msecSmallTime) {
		var msecWidth = maxV-minV; // values spanned by the graph area (voltage or time).
//		var msecLargeTime=200; // time span between the dark grid lines 1/5 second (5 millimeter)on paper ECG, equal to 5 small time blocks
//		var msecSmallTime=40;  // time span between the light grid lines 1/25 second (1 mm) on paper ECG
		var pixLargeTime = (pixels*msecLargeTime)/msecWidth; // pixels spanned by the dark grid lines (1/5 second)
		var msPerPixel = msecWidth/pixels; // milliseconds between one pixel and then next
		var pixSmallTime = (pixels*msecSmallTime)/msecWidth; // pixels spanned by the light grid lines (1/25 second)
		var pixMinSmallTime = 1;  // small time blocks must be at least this many pixels wide.
		
//		var bLabelsKMB = attr("labelsKMB");
		var ticks = [];
		
		CVRG_setTimeExponent(minV,self);
		CVRG_setMsPerPixel(msPerPixel, self);
		
		if (forced_vals) {
			for (var i = 0; i < forced_vals.length; i++) {
				ticks.push({v: forced_vals[i]});
			}
		} else {
			// ticks.length won't be 0 if the log scale function finds values to insert.
			if (ticks.length == 0) {
				// Basic idea:
				// Try labels every 1, 2, 5, 10, 20, 50, 100, etc.
				// Calculate the resulting tick spacing (i.e. this.height_ / nTicks).
				// The first spacing greater than pixelsPerYLabel is what we use.
				var mults = [1, 2, 5];
				var scale, low_val, high_val, nTicks;
				
				if (pixLargeTime < pixelsPerTick){	
					// default tick spacing
					for (var i = -10; i < 50; i++) {
						var base_scale = Math.pow(10, i);
						for (var j = 0; j < mults.length; j++) {
							scale = base_scale * mults[j];
							low_val = Math.floor(minV / scale) * scale;
							high_val = Math.ceil(maxV / scale) * scale;
							nTicks = Math.abs(high_val - low_val) / scale;
							var spacing = self.height_ / nTicks;
							// wish I could break out of both loops at once...
							if (spacing > pixelsPerTick) break;
						}
						if (spacing > pixelsPerTick) break;
					}
					for (var i = 0; i < nTicks; i++) {
						var tickV = low_val + i * scale;
						ticks.push( {v: tickV} );
					}

				}else{
					// draw thick and thin grid line as are seen in paper ECGs.
					if(pixSmallTime >= pixMinSmallTime){
						scale = msecSmallTime;
					}else {
						scale = msecLargeTime;
					}
					low_val = Math.floor(minV / scale) * scale;				
					high_val = Math.ceil(maxV / scale) * scale;
					nTicks = Math.abs(high_val - low_val) / scale;

					spacing = self.height_ / nTicks;
					
					// Construct the set of ticks.
					for (var i = 0; i < nTicks; i++) {
						var tickV = low_val + i * scale;
						var sec = Math.floor(tickV/1000); // integer seconds portion of tick time
						var msSubSec= tickV-(sec*1000); // milliseconds portion of tick time
						
						// dark grid lines are made up of a label on the value plus an unlabeled "darkening line" one pixel to either side.
						if(msSubSec==0){ // dark grid lines on whole seconds
							ticks.push( {label: "", v: tickV-msPerPixel} ); // don't label darking lines.
							ticks.push( {v: tickV} ); //  use default label on whole seconds
							ticks.push( {label: "", v: tickV+msPerPixel} ); // don't label darking lines.
						}else{
							if(msSubSec%msecLargeTime > 1){// light grid lines on 25ths of a second, if between 5ths of a second lines.
								if (pixSmallTime > pixelsPerTick){
									ticks.push( {v: tickV} ); // use default label on fractional seconds
//									if(tickV > 0){
//										ticks.push( {label: msSubSec/1000.0, v: tickV} ); // only show the milliseconds
//									}else{
//										ticks.push( {label: (msSubSec-1000)/1000.0, v: tickV} ); // only show the milliseconds					
//									}
								}else{
									ticks.push( {label: "", v: tickV} ); // don't label small squares, overrides default label.
								}
							}else{ // dark grid lines on whole 5ths of a second
								ticks.push( {label: "", v: tickV-msPerPixel} );// don't label darking lines.
								ticks.push( {v: tickV} ); // use default label on fractional seconds
								ticks.push( {label: "", v: tickV+msPerPixel} );	// don't label darking lines.		
							}
						}
					}
				}
			}
		}

		// Get the formatter's name from properties.
		var formatter = axis_props('axisLabelFormatter');

		// Add default labels to the ticks which don't yet have labels defined.
		for (var i = 0; i < ticks.length; i++) {
			// create label if no text is found.
			if (ticks[i].label !== undefined)
				continue;  // Use current label.
			else{
				var tickV = ticks[i].v;
				var label = formatter(tickV, self);
				ticks[i].label = label;
			}
		}

		return ticks;
	};
	/*
*/
	/**
	 * Add ticks when the x axis has numbers on it (instead of dates)
	 * @param minV - minimum value of the window on this axis
	 * @param maxV - maximum value of the window on this axis
	 * @param self
	 * @param axis_props - properties array of this axis, if any.
	 * @param vals - Array of {label, value} tuples.
	 * @param pixelsPerLabelAttribute - either "pixelsPerXLabel" or "pixelsPerYLabel"
	 * 
	 * @return - Array of {label, value} tuples.
	 * @public
	 */
	/*
	var CVRG_TickerCommon = function(minV, maxV, pixels, axis_props, self, forced_vals, pixelsPerTick, msecLargeTime, msecSmallTime) {
//		var pixelsPerTick = axis_props('pixelsPerLabel');
		var msecWidth = maxV-minV; // values spanned by the graph area (voltage or time).
//		var msecLargeTime=200; // time span between the dark grid lines 1/5 second (5 millimeter)on paper ECG, equal to 5 small time blocks
//		var msecSmallTime=40;  // time span between the light grid lines 1/25 second (1 mm) on paper ECG
		var pixLargeTime = (pixels*msecLargeTime)/msecWidth; // pixels spanned by the dark grid lines (1/5 second)
		var msPerPixel = msecWidth/pixels; // milliseconds between one pixel and then next
		var pixSmallTime = (pixels*msecSmallTime)/msecWidth; // pixels spanned by the light grid lines (1/25 second)
		var pixMinSmallTime = 1;  // small time blocks must be at least this many pixels wide.
		
//		var bLabelsKMB = attr("labelsKMB");
		var ticks = [];
		
		CVRG_setTimeExponent(minV,self);
		CVRG_setMsPerPixel(msPerPixel, self);
		
		if (forced_vals) {
			for (var i = 0; i < forced_vals.length; i++) {
				ticks.push({v: forced_vals[i]});
			}
		} else {
			// ticks.length won't be 0 if the log scale function finds values to insert.
			if (ticks.length == 0) {
				// Basic idea:
				// Try labels every 1, 2, 5, 10, 20, 50, 100, etc.
				// Calculate the resulting tick spacing (i.e. this.height_ / nTicks).
				// The first spacing greater than pixelsPerYLabel is what we use.
				var mults = [1, 2, 5];
				var scale, low_val, high_val, nTicks;
				
				if (pixLargeTime < pixelsPerTick){	
					// default tick spacing
					for (var i = -10; i < 50; i++) {
						var base_scale = Math.pow(10, i);
						for (var j = 0; j < mults.length; j++) {
							scale = base_scale * mults[j];
							low_val = Math.floor(minV / scale) * scale;
							high_val = Math.ceil(maxV / scale) * scale;
							nTicks = Math.abs(high_val - low_val) / scale;
							var spacing = self.height_ / nTicks;
							// wish I could break out of both loops at once...
							if (spacing > pixelsPerTick) break;
						}
						if (spacing > pixelsPerTick) break;
					}
					for (var i = 0; i < nTicks; i++) {
						var tickV = low_val + i * scale;
						ticks.push( {v: tickV} );
					}

				}else{
					// draw thick and thin grid line as are seen in paper ECGs.
					if(pixSmallTime >= pixMinSmallTime){
						scale = 40;
					}else {
						scale = 200;
					}
					low_val = Math.floor(minV / scale) * scale;				
					high_val = Math.ceil(maxV / scale) * scale;
					nTicks = Math.abs(high_val - low_val) / scale;

					spacing = self.height_ / nTicks;
					
					// Construct the set of ticks.
					for (var i = 0; i < nTicks; i++) {
						var tickV = low_val + i * scale;
						var sec = Math.floor(tickV/1000); // integer seconds portion of tick time
						var msSubSec= tickV-(sec*1000); // milliseconds portion of tick time
						
						// dark grid lines are made up of a label on the value plus an unlabeled "darkening line" one pixel to either side.
						if(msSubSec==0){ // dark grid lines on whole seconds
							ticks.push( {label: "", v: tickV-msPerPixel} ); // don't label darking lines.
							ticks.push( {v: tickV} ); //  use default label on whole seconds
							ticks.push( {label: "", v: tickV+msPerPixel} ); // don't label darking lines.
						}else{
							if(msSubSec%200 > 1){// light grid lines on 25ths of a second, if between 5ths of a second lines.
								ticks.push( {label: "", v: tickV} ); // don't label small squares, overrides default label.
							}else{ // dark grid lines on whole 5ths of a second
								ticks.push( {label: "", v: tickV-msPerPixel} );// don't label darking lines.
								ticks.push( {v: tickV} ); // use default label on fractional seconds
//								if(tickV > 0){
//									ticks.push( {label: msSubSec, v: tickV} ); // only show the milliseconds
//								}else{
//									ticks.push( {label: (msSubSec-1000), v: tickV} ); // only show the milliseconds					
//								}
//								ticks.push( {label: "", v: tickV+msPerPixel} );	// don't label darking lines.		
							}
						}
					}
				}
			}
		}

		// Get the formatter's name from properties.
		var formatter = axis_props('axisLabelFormatter');

		// Add default labels to the ticks which don't yet have labels defined.
		for (var i = 0; i < ticks.length; i++) {
			// create label if no text is found.
			if (ticks[i].label !== undefined)
				continue;  // Use current label.
			else{
				var tickV = ticks[i].v;
				var label = formatter(tickV, self);
				ticks[i].label = label;
			}
		}

		return ticks;
	};
	
	*/
	
/*
	var CVRG_TickerGeneratorExponential = function(minV, maxV, self, formatter, pixelsPerTick, ecg_div_width, bLabelsKMB) {
//		var pxClipWidth = self.clippingArea_.width;
		var msecWidth = maxV-minV;
		var msecLargeTime=200
		var msecSmallTime=40
		var pixLargeTime = (pxClipWidth*msecLargeTime)/msecWidth;
		var msPerPixel = msecWidth/pxClipWidth;
		var pixSmallTime = (pxClipWidth*msecSmallTime)/msecWidth;
		var pixMinSmallTime = 1;  // small squares must be at least this many pixels wide.
		var spacing=0;
		// Basic idea:
		// Try labels every 1, 2, 5, 10, 20, 50, 100, etc.
		// Calculate the resulting tick spacing (i.e. this.height_ / nTicks).
		// The first spacing greater than pixelsPerXLabel is what we use.
		// TODO(danvk): version that works on a log scale.
		//if (self.attr_("labelsKMG2")) {
		//if(bLabelsKMB){
	//		var mults = [1, 2, 4, 8];
		//} else {
			var mults = [1, 2, 5];
		//}
		
		var scale, low_val, high_val, nTicks;
		// TODO(danvk): make it possible to set this for x- and y-axes independently.
		if (pixLargeTime < pixelsPerTick){	
			for (var i = -10; i < 50; i++) {
				var base_scale = Math.pow(10, i);

				for (var j = 0; j < mults.length; j++) {
					scale = base_scale * mults[j];
					low_val = Math.floor(minV / scale) * scale;
					high_val = Math.ceil(maxV / scale) * scale;
					nTicks = Math.abs(high_val - low_val) / scale;
					spacing = self.height_ / nTicks;
					// wish I could break out of both loops at once...
					if (spacing > pixelsPerTick) break;
				}
				if (spacing > pixelsPerTick) break;
			}
		}else{
			//if (pixLargeTime >= pixelsPerTick){
				if(pixSmallTime >= pixMinSmallTime){
					scale = 40;
				}else {
					scale = 200;
				}
				low_val = Math.floor(minV / scale) * scale;				
				high_val = Math.ceil(maxV / scale) * scale;
				nTicks = Math.abs(high_val - low_val) / scale;

//				spacing = self.height_ / nTicks;
				spacing = pxGraphSpan/nTicks;
				
			//}
		}
		// Construct labels for the ticks
		var ticks = [];
		var k;
		var k_labels = [];
		if (bLabelsKMB) {
			k = 10;
			k_labels = [ "e1", "e2", "e3", "e4" ];
		}
		
//		if (self.attr_("labelsKMG2")) {
//			if (k) self.warn("Setting both labelsKMB and labelsKMG2. Pick one!");
//			k = 1024;
//			k_labels = [ "k", "M", "G", "T" ];
//		}

		// Allow reverse y-axis if it's explicitly requested.
		if (low_val > high_val) scale *= -1;

		for (var i = 0; i < nTicks; i++) {
			var tickV = low_val + i * scale;
			var absTickV = Math.abs(tickV);
			var label;
			if (formatter != undefined) {
			  label = formatter(tickV);
			} else {
			  label = Dygraph.round_(tickV, 2);
			}
		
			if (bLabelsKMB) {
			  // Round up to an appropriate unit.
			  var n = k*k*k*k;
			  RndtickV = 1;
			  for (var j = 3; j >= 0; j--, n /= k) {
				if (absTickV >= n) {
					RndtickV = Dygraph.round_(tickV / n, 1);
					label = RndtickV + k_labels[j];
					break;
				}
			  }
			}		
			var sec = Math.floor(tickV/1000); // integer seconds portion of tick time
			var msSubSec=0;
//			if(tickV > 0){
				msSubSec = tickV-(sec*1000); // milliseconds portion of tick time
//			}else{
//				msSubSec = (sec*1000) - tickV; // milliseconds portion of tick time for negative numbers.
//			}		

			if(msSubSec==0){
				ticks.push( {label: "", v: tickV-msPerPixel} );
				ticks.push( {label: label, v: tickV} ); // on even seconds, show "k", "M", "G", "T" version
				ticks.push( {label: "", v: tickV+msPerPixel} );
			}else{
				if(msSubSec%200 > 1){
					ticks.push( {label: "", v: tickV} ); // don't label small squares
				}else{
					ticks.push( {label: "", v: tickV-msPerPixel} );
					if(tickV > 0){
						ticks.push( {label: msSubSec, v: tickV} ); // only show the milliseconds
					}else{
						ticks.push( {label: (msSubSec-1000), v: tickV} ); // only show the milliseconds					
					}
					ticks.push( {label: "", v: tickV+msPerPixel} );			
				}
			}
		}
		return ticks;
	};
*/
	
	
	// centers the graph on specified coordinates, leaving scales the same.
	// x - milliseconds
	// y - microvolts,
	var CVRG_CenterGraph = function(x, y){
		var extremes = ecg_graph.xAxisExtremes();
		var min = extremes[0];
		var max = extremes[1];
		if( (x > min) & (x < max) ){ // new center is within currently loaded data range.			
		      // Pull an initial values for calculating ranges.
			var minX = ecg_graph.xAxisRange()[0];
			var maxX = ecg_graph.xAxisRange()[1];
			var minY = ecg_graph.yAxisRange()[0];
			var maxY = ecg_graph.yAxisRange()[1];
	
			var xWidth =  (maxX - minX)/2;
			var yHeight = (maxY - minY)/2;
			
			// calculate new ranges
			minX = x - xWidth;
			maxX = x + xWidth;
			minY = y - yHeight;
			maxY = y + yHeight;
	
			CVRG_zoomGraphX(minX, maxX);
			CVRG_zoomGraphY(minY, maxY);
		}else{
//			alert("Annotation is at " + x + " milliseconds, which is outside of the currently loaded data range. (" + min + " - " + max + ")");
//			CVRG_annotationDblClickHandlerJSNI(x,y);
			CVRG_centerDygraphJSNI(x,y);
		}
	};

	function CVRG_zoomGraphX(minTime, maxTime) {
		var bDots = CVRG_bShowDots();
		var newTimeLabel = CVRG_getnewTimeLabel();
		
		ecg_graph.updateOptions({
			dateWindow: [minTime, maxTime],
			drawPoints: bDots,
			xlabel: newTimeLabel
		});
	};

	function CVRG_zoomGraphY(minValue, maxValue) {
		ecg_graph.updateOptions({
			valueRange: [minValue, maxValue]
		});
	};



	/** A function to call when the zoom window is changed (either by zooming in or out). minDate and maxDate are milliseconds since epoch. yRanges is an array of [bottom, top] pairs, one for each y-axis.
	 * 
	 * @param minDate
	 * @param maxDate
	 * @param yRanges
	 * @returns
	 */
	var CVRG_zoomCallback = function (minDate, maxDate, yRanges) {
		var bDots = CVRG_bShowDots();
		var newTimeLabel = CVRG_getnewTimeLabel();
		ecg_graph.updateOptions({
//			isZoomedIgnoreProgrammaticZoom: true,
//			dateWindow: [minTime, maxTime],
			valueRange: [yRanges[0][0], yRanges[0][1]],
			drawPoints: bDots,
			xlabel: newTimeLabel
		});
	};

	/** Initializes the axis labels, y-axis range and dot visibility.
	 * x-label is constructed from previously set values of CVRG_timeLabelPrefix, CVRG_TimeExponent and CVRG_sSecondSuffix.
	 * 
	 * @param dMinV - smallest voltage to display
	 * @param dMaxV - largest voltage to display
	 * @param yLabel - voltage label
	 * @returns void
	 */
	var CVRG_setLabels = function (dMinV, dMaxV, yLabel){
		var bDots = CVRG_bShowDots();
		var newTimeLabel = CVRG_getnewTimeLabel();
		ecg_graph.updateOptions({
			valueRange: [dMinV, dMaxV],
			drawPoints: bDots,
			xlabel: newTimeLabel,
			ylabel: yLabel
		});
	};
	
	
	function CVRG_bShowDots(){
		var bDots=false;
		var msPerSample = 1000/CVRG_sampleRate;
		var pixelsPerSample = msPerSample/CVRG_MsPerPixel;
//		if(CVRG_MsPerPixel < 0.1{
		if(pixelsPerSample > 10){
			bDots = true;
		}

		return bDots;
	}
	
	/** Formats the label for the time axis based on the current decade of the data being displayed.
	 * 
	 * @returns {String}
	 */
	function CVRG_getnewTimeLabel(){
		return CVRG_timeLabelPrefix + " ( * 10^" + CVRG_TimeExponent + " Lead " + CVRG_sSecondSuffix +")";
	};
	
	function formatMsecToExponent(x){
		var decimalMult = ( Math.pow(10,(CVRG_TimeExponent)) ); // e.g. 1, 10, 100, 1000 or 10000 ect.
		var s = x/decimalMult;
		return "Time (Sec) " + s + " * 10^0";
		
		
		// RSA 012813 - return "Time: Seconds " + s + " X 10^" + CVRG_TimeExponent;
	};
	
//	var CVRG_SetStepPlot = function(step) {
//		CVRG_step = step;
//		if(CVRG_step){
//			ecg_graph.updateOptions( {
//				stepPlot: true,
//				fillGraph: true
//			});
//		}else{
//			ecg_graph.updateOptions( {
//				stepPlot: false
//			});		
//		}
//
//	};
