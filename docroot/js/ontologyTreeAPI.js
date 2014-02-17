/* 
 * The following JavaScript functions allow you to interact with the Flash BioPortal Ontology Tree.  
 * You can load an ontology by id, and get the current ontology id or name.
 * Once an ontology is loaded then you can get the currently selected concept by id or name,
 * and you can also select a concept by id or name.  
 * You can also listen for one of these events by implementing the following functions (see the stubs below):
 * appComplete, treeSelectionChanged, treeNodeDoubleClicked, or errorLoadingOntology
 *
 * These are the parameters that you can pass into the application using the "flashVars" parameter:
 * (see the examples below):
 * - ontology: the id of the ontology (version or virtual)
 * - virtual: if false (default) then the ontology id above is the version id
 * 			  if true then the above ontology id is assumed to be the virtual id 
 * - alerterrors: (true/false) determines whether the application will display errors (default is false)
 * - server: defines the URL of the rest server (null by default)
 * - title: changes the default title for the page 
 * - canchangeontology: if true then the ontology can be changed
 * - rootconceptid: sets the optional root node of the tree
 * - canchangeroot: if false then the root of the tree cannot be changed by the user (using context menu items)
 */
var CVRG_conceptName = ""; //concept name temp variable so that it can be looked up once the app finishes loading.

// get a handle for the flash application
function getApp() {
	if (navigator.appName.indexOf ("Microsoft") != -1) {
		app = window["OntologyTree"];
	} else {
		app = document["OntologyTree"];
	}
	if (app == null) {
		app = document.getElementById("OntologyTree");
	}
	
	if (app == null) {
		app = WAVEFORM_getElementByIdEndsWith("div","OntologyTree");
	}
	if (app == null) {
		alert("Could not get Flash object, JavaScript/Flex communication failed.");
	}
	return app;
}

// these are the available functions that you can call ONCE the 
// flash SWF has finished loading

/** Loads a new ontology by id.  
 * e.g. "42932" */
function loadOntology(ontologyID) {
	var app = getApp();
	if (app && app.loadOntology) {
		app.loadOntology(ontologyID);
	}
}

/** Gets the id of the current ontology, will be null if no ontology is loaded. 
 * e.g. "42932" 
 */
function getOntologyID() {
	var ontologyID = null;
	var app = getApp();
	if (app && app.getOntologyID) {
		ontologyID = app.getOntologyID();
	}
	return ontologyID;
}

/** Gets the name of the current ontology, will be null if no ontology is loaded. 
 * e.g. "Electrocardiography Ontology" 
 */
function getOntologyName() {
	var ontologyName = null;
	var app = getApp();
	if (app && app.getOntologyName) {
		ontologyName = app.getOntologyName();
	}
	return ontologyName;
}

/** Gets the id of the currently selected concept, will be null if nothing is selected. 
 * e.g. "ECGOntologyv0:ECG000000318" 
 */
function getSelectedConceptID() {
	var conceptID = null;
	var app = getApp();
	if (app && app.getSelectedConceptID) {
		conceptID = app.getSelectedConceptID();
	}
	return conceptID;
}

/** 
 * Gets the full id (not all ontologies support this) of the currently selected concept. 
 * Will be null if nothing is selected, will be the same as the conceptID if no fullID exists.
 * e.g. "http://www.cvrgrid.org/files/ECGOntologyv0.1.7.owl#ECG000000318"
 */
function getSelectedConceptFullID() {
	var conceptID = null;
	var app = getApp();
	if (app && app.getSelectedConceptFullID) {
		conceptID = app.getSelectedConceptFullID();
	}
	return conceptID;
}

/** Gets the name of the currently selected concept, will be null if nothing is selected. 
 * e.g. "Negative_Electrode" 
 */
function getSelectedConceptName() {
	var conceptName = null;
	var app = getApp();
	if (app && app.getSelectedConceptName) {
		conceptName = app.getSelectedConceptName();
	}
	return conceptName;
}

/** Loads and selects a concept (by id) in the current ontology. */
function loadConceptByID(conceptID) {
	var app = getApp();
	if (app && app.loadConceptByID) {
		app.loadConceptByID(conceptID);
	}
}

/** Attempts to load and select a concept (by name) in the current ontology. */
function loadConceptByName(conceptName) {
	var app = getApp();
	if (app && app.loadConceptByName) {
		app.loadConceptByName(conceptName);
	}
}

/** This function gets call by flash when the swf has finished loading. */
function appComplete(swfID) {
	//loadConceptByName(CVRG_conceptName);
}

/** Implement this function to listen for tree selection changes 
 * nodeID - same as returned by getSelectedConceptID()
 * nodeName - same as returned by getSelectedConceptName()
 * swfID - id of the flash object, as set in HTML
 * e.g. "ECGOntologyv0:ECG000000318", "Negative_Electrode", "OntologyTree"
 */
function treeSelectionChanged(nodeID, nodeName, swfID) {
	//alert("tree selection: " + nodeID + " - " + nodeName + " - " + swfID);
    lookupAnnotationParam([{name:'nodeID', value:nodeID}, {name:'nodeName', value:nodeName}]);
    return false;
}

/** Implement this function to listen for tree double click events 
 * nodeID - same as returned by getSelectedConceptID()
 * nodeName - same as returned by getSelectedConceptName()
 * swfID - id of the flash object, as set in HTML
 * e.g. "ECGOntologyv0:ECG000000318", "Negative_Electrode", "OntologyTree"
 */
function treeNodeDoubleClicked(nodeID, nodeName, swfID) {
    lookupAnnotationParam([{name:'nodeID', value:nodeID}, {name:'nodeName', value:nodeName}]);
    return false;
}

/** Implement this function to listen for error messages when loading an ontology */
function errorLoadingOntology(errorMsg, swfID) {
	//alert("Error: " + errorMsg);
}



//  var CVRG_ontologyTreeSelectionChanged = function(nodeID, nodeName) {
//	CVRG_ontologyTreeSelectionChangedJSNI(nodeID, nodeName);
//}

//set the concept name temp variable so that it can be looked up once the app finishes loading.
var CVRG_setConceptName = function(conceptName){
	CVRG_conceptName = conceptName;
};