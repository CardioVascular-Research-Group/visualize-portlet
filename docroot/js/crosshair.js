/**************************
Functions used by crosshair.html
revision 0.1 : 9/7/2012 - initial version Michael Shipway

 *************************/

var ylinesLeft = [];
var ylinesRight = [];
var yPadding = 32;
var xPadding = 30;
//var leadCount = 2;
var xlineTop, xlineBottom; //, xlineB;


var CVRG_InitHorizontalLines = function(iLeadCount, divID, namespace){
	for (var i = 0; i < iLeadCount; i++) {
		var divFullID = namespace + ":" + divID;
		//alert("divFullID =" + divFullID);
//		initialize horizontal ylinesLeft
		var lineL = document.createElement("div");
		lineL.style.display = "none";
		lineL.style.width = "20px";
		lineL.style.height = "1px";
//		lineL.style.left = "25px";
		lineL.style.left = (yPadding + 25) + "px";
		lineL.style.backgroundColor = "black";// red
		lineL.style.position = "absolute";
		document.getElementById(divFullID).appendChild(lineL);
		ylinesLeft.push(lineL);

//		initialize horizontal ylinesRight
		var lineR = document.createElement("div");
		lineR.style.display = "none";
		lineR.style.width = "20px";
		lineR.style.height = "1px";
//		lineR.style.right = "25px";
		lineR.style.right = "0px";
		lineR.style.backgroundColor = "black";//magenta
		lineR.style.position = "absolute";
		document.getElementById(divFullID).appendChild(lineR);
		ylinesRight.push(lineR);

	}
};
/*
        for (var i = 0; i < 2; i++) {
          var line = document.createElement("div");
          line.style.display = "none";
          line.style.width = "100%";
          line.style.height = "1px";
          line.style.backgroundColor = "black";
          line.style.position = "absolute";
          document.getElementById("div_gB").appendChild(line);
          ylinesLeft.push(line);
        }
 */
var CVRG_InitVerticalLines = function(divID, namespace){
	var divFullID = namespace + ":" + divID;
	xlineTop = document.createElement("div");
	xlineTop.style.display = "none";
	xlineTop.style.width = "1px";
	xlineTop.style.height = "50%";
	xlineTop.style.top = yPadding + "px";
	xlineTop.style.backgroundColor = "black"; // blue
	xlineTop.style.position = "absolute";
	document.getElementById(divFullID).appendChild(xlineTop);

	xlineBottom = document.createElement("div");
	xlineBottom.style.display = "none";
	xlineBottom.style.width = "1px";
	xlineBottom.style.height = "10px";
//	xlineBottom.style.bottom = "700px";
	xlineBottom.style.backgroundColor = "black"; // green
	xlineBottom.style.position = "absolute";
	document.getElementById(divFullID).appendChild(xlineBottom);
};
/*
  	  	xlineB = document.createElement("div");
        xlineB.style.display = "none";
        xlineB.style.width = "1px";
        xlineB.style.height = "100%";
        xlineB.style.top = "0px";
        xlineB.style.backgroundColor = "black";
        xlineB.style.position = "absolute";
        document.getElementById("div_gB").appendChild(xlineB);
 */
//var CVRG_highlightCallback = function(e, pts, yOffset, xOffset) {
var WAVEFORM3_highlightCrosshairs = function(e, pts, bJustFirst) {
	var y=0, x=0, lineGap=10;
	var pointLen = 0;
	var parentCanvas = e.currentTarget;
//	var canvasHeight = parentCanvas.height;
//	var canvasWidth = parentCanvas.width;
	var canvasHeight = parentCanvas.height;
	var canvasWidth = parentCanvas.width;

	var rangeSelect = parentCanvas.nextElementSibling;
	var rangeHeight = rangeSelect.height;
	var rangeWidth = rangeSelect.width;
	var graphHeight = canvasHeight-rangeHeight-20; // minus twenty to account for padding
	
	if(bJustFirst){ // how many horizontal lines to draw.
		pointLen = 1;
	}else{
		pointLen = pts.length;
	}
	for (var i = 0; i < pointLen; i++) {
//		y = pts[i].canvasy;
//		x = pts[i].canvasx;
		y = pts[i].canvasy+yPadding;
		x = pts[i].canvasx+xPadding;
		
		//red
		ylinesLeft[i].style.display = "";
		ylinesLeft[i].style.top = y + "px";
		ylinesLeft[i].style.width = (x-lineGap-60) + "px";
//		ylinesLeft[i].style.width = (x-lineGap) + "px";
		

		//magenta
		ylinesRight[i].style.display = "";
		ylinesRight[i].style.top = y + "px";
		ylinesRight[i].style.left = (x+lineGap+10) + "px";
		
		ylinesRight[i].style.width = (canvasWidth-x) + "px";

		if (i == 0){
			xlineTop.style.left = x + "px";
			xlineBottom.style.left = x + "px";
		}
	}

	/*
	var xc = e.clientX; // Returns the horizontal coordinate of the mouse pointer, 
						// relative to the current window, when an event was triggered
	var xp = e.pageX;   //  gives the mouse coordinates relative to the entire document. On some browsers.
	var xs = e.screenX; // Returns the horizontal coordinate of the mouse pointer, 
						// relative to the screen, when an event was triggered
						// *** The only one that is completely cross�browser compatible. *** //
						
	var xl = e.layerX;  // Returns the horizontal (x) coordinate of the mouse pointer.
						// These coordinates are relative to the containing layer. 
						// If no layers or positionable elements have been defined, 
						// the default layer of the base document is used as a reference point, 
						// thus equivalent to the pageX and pageY properties. 
						
	var yc = e.clientY;
	var yp = e.pageY;
	var ys = e.screenY;
	*/
	var yl=0;
	if(pts.length > 1){
		yl = e.layerY;
	}else{
		yl = y;
	}
	
	xlineTop.style.height = (yl-lineGap) +"px";  // blue
	xlineTop.style.display = "";// blue
	
	xlineBottom.style.top = (yl+lineGap+yPadding) +"px"; // green
//	xlineBottom.style.bottom = (graphHeight + yPadding+20) +"px"; // green
	xlineBottom.style.height = (graphHeight-yl) + "px";// green
	xlineBottom.style.display = "";
};

var CVRG_unhighlightCrosshairs = function(iLeadCount) {
	for (var i = 0; i < iLeadCount; i++) {
		ylinesLeft[i].style.display = "none";
		ylinesRight[i].style.display = "none";
	}
//	xlineTop.style.display = "none";
//	xlineBottom.style.display = "none";
};

var CVRG_unhighlightCallback = function(e, iLeadCount) {
	CVRG_unhighlightCrosshairs(iLeadCount);
};

