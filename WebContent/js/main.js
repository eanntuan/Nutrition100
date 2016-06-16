/*
  Main JS file
*/
$(document).ready(function(){
	"use strict";

	// If this code is running, no need to show javascript warning
	$("#jsWarning").hide(); 	
	
		// hide warnings until need to show them
		$("#similarWarning").hide();
 		//$("#incompleteWarning").hide();
 		$("#shortWarning").hide();
 		$("#spellWarning").hide();
 		$("#repeatWarning").hide();
 		$("#canSubmit").hide();
 		$("#radioBtnWarning").hide();

	// The following code is from MTurk's CLT example

	//Get the assignmentId
	var assignmentId = gup("assignmentId");
	// If there isn't an assignment Id, set noAssignmentId equal to true
	var noAssignmentId = (!assignmentId || assignmentId === "ASSIGNMENT_ID_NOT_AVAILABLE");

	$("#assignmentId").val(assignmentId);

	if(noAssignmentId) {
		// We are only previewing the HIT - display the message to explain to the Turker that we are
		$("#warning").show();
	} else {
		// If we aren't previewing the HIT, enable submission 
		$("#checkButton, input, textarea").prop("disabled",false);
		// If we aren't previewing the HIT, get the dataIndex value
		$("#dataIndex").val(gup("dataIndex"));
	}

	if (gup("turkSubmitTo").indexOf("workersandbox") !== -1) {
		// Sandbox mode
		// On submit, submit to sandbox mode, not actual Mturk
		$("#answerForm").attr("action","https://workersandbox.mturk.com/mturk/externalSubmit");
	}
	
	//$(".required").focus(function() {
		//$("#incompleteWarning").hide();
	//});

	var format = function(text) {
		text = text.replace(/\s+/g, " ");
    	text = text.replace(/^ | $/g, "");
    	return text;
 	};

 	$('.required').bind('input propertychange', function() {
 		// Disable submit button if something required isn't done
 		$("#submitButton").prop("disabled",true);
 	});

 	// Spell check the user's input and their feedback so they can correct it
 	//$Spelling.SpellCheckAsYouType('response1');
 	//$Spelling.SpellCheckAsYouType('feedback');
 	
 	function checkMistakes() {

	 	// check number misses per category (USDA, tags, quantities)
	 	var numUSDAMisses = 0;
	 	var numTagsMisses = 0;
	 	var numQuantMisses = 0;
	 	var numImageMisses = 0;
	 	var foodNum = parseInt($("#numFoods").val());
	 	var foodNumIncr = foodNum+1;
	 	console.log("num foods "+foodNum);
//	 	console.log("num foods+1 "+foodNumIncr);
	 	
	 	// check radio btns for each row in the table (i.e. each food item)
	 	for (var foodIndex = 1; foodIndex < foodNum+1; foodIndex++){
	 		console.log("foodIndex: "+foodIndex);
		 	if ( $("#radio_USDA"+foodIndex+"no:checked").length ) { 
		 		numUSDAMisses += 1;
		 		console.log("USDA misses: "+numUSDAMisses);
		 	} else if (! $("#radio_USDA"+foodIndex+"yes:checked").length) {
		 		// if neither radio button was checked, prevent submission
		 		console.log("USDA radio button wasn't checked");
		 		return false;
		 	}
		 	/*
		 	if ( $("#radio_tags"+foodIndex+"no:checked").length ) { 
		 		numTagsMisses += 1;
		 		console.log("tag misses "+numTagsMisses);
		 	} else if (! $("#radio_tags"+foodIndex+"yes:checked").length) {
		 		 //if neither radio button was checked, prevent submission
		 		console.log("tag radio button wasn't checked");
		 		return false;
		 	}
		 	*/
		 	if ( $("#radio_quantity"+foodIndex+"no:checked").length ) { 
		 		numQuantMisses += 1;
		 		console.log("quantity misses "+numQuantMisses);
		 	} else if (! $("#radio_quantity"+foodIndex+"yes:checked").length) {
		 		// if neither radio button was checked, prevent submission
		 		console.log("quantity radio button wasn't checked");
		 		return false;
		 	}
		 	
		 	if ( $("#radio_Image"+foodIndex+"no:checked").length ) { 
		 		numImageMisses += 1;
		 		console.log("image misses "+numImageMisses);
		 	} else if (! $("#radio_Image"+foodIndex+"yes:checked").length) {
		 		// if neither radio button was checked, prevent submission
		 		console.log("image radio button wasn't checked");
		 		return false;
		 	}
	 	}
	 	// set values for number of misses per category
	 	console.log("numUSDAMissed: "+numUSDAMisses);
	 	$("#numUSDAMissed").val(numUSDAMisses);
	 	//console.log("numTagsMissed "+numTagsMisses);
	 	//$("#numTagsMissed").val(numTagsMisses);
	 	console.log("numQuantMissed: "+numQuantMisses);
	 	$("#numQuantMissed").val(numQuantMisses);
	 	console.log("numImageMissed: "+numImageMisses);
	 	$("#numImageMissed").val(numImageMisses);
	 	return true;
 	}
 	
 	/*
 	function checkDelSubst(){
 		
 		var numSubstDels = ["0", "1", "2", "3", "4"];
 		var numDels = 0;
 		var numSubs = 0;
 		var subSelected = false;
 		var delSelected = false;
 		
 		for (i = 0; i < numSubstDels.length; i++) {
	 		if ( $("#radio_del"+i+":checked").length ) { 
		 		numDels = i;
		 		subSelected = true;
		 		console.log("num deleted "+numDels);
		 	}
	 		if ( $("#radio_subst"+i+":checked").length ) { 
		 		numSubs = i;
		 		delSelected = true;
		 		console.log("num substituted "+numSubs);
		 	}
 		}
	 	
	 	// assign values to form
	 	$("#numDeletions").val(numDels);
	 	$("#numSubs").val(numSubs);
	 	return (subSelected & delSelected);
 	}
 	*/
 	
	$("#submitButton").click(function() {
 		setTimeout(function() {
 				
 				// ensure radio buttons for all mistake categories were checked
 				if (!checkMistakes()){
 					console.log("checking for mistakes");
 			 		$("#radioBtnWarning").show();
 			 		$("#canSubmit").hide();
 					return false;
 				}
 				/*
 				// ensure substitution/deletion radio btns were checked
 				if (!checkDelSubst()){
 			 		$("#radioBtnWarning").show();
 			 		$("#canSubmit").hide();
 					return false;
 				}*/
			 	
	 			$("#answerForm").submit();
 		}, 500);
 	});

	/*
 	$("#checkButton").click(function() {
		$("#similarWarning").hide();
 		$("#incompleteWarning").hide();
 		$("#shortWarning").hide();
 		$("#spellWarning").hide();
 		$("#repeatWarning").hide();
 		$("#canSubmit").hide();
 		// Perform a check of the data and prevent accidental submission
 		var ok = true;
		
 		// check that none of the required inputs are blank
 		$(".required").each(function(index,elt) {
 			$(elt).val(format($(elt).val()));
 			if ($(elt).val() === "") {
 				$("#submitButton").prop("disabled",true);
 				ok = false;
 			}
 		});

 		if (!ok) {
 			console.log("incomplete!");
 			$("#incompleteWarning").show();
 			return false;
 		}

 		$(".required").each(function(index,elt) {
 			$.ajax({
 				url: "readPy.php",
 				data:{sent:$(elt).val() },
 				async: false}).done(function(data) {
 					var comp = data.localeCompare("yes");
 					if(comp==1 || comp==0){
 						ok = false;
 					}
 			});
 		});

 		// TODO: debug
 		if (!ok) {
 			console.log("too similar!");
 			$("#similarWarning").show();
 			$("#submitButton").prop("disabled",true);
 			return false;
 		}

 		$(".required").each(function(index,elt) {
 			var s = $(elt).val();
 			s = s.replace(/(^\s*)|(\s*$)/gi,"");
			s = s.replace(/[ ]{2,}/gi," ");
			s = s.replace(/\n /,"\n");
			var wordcount = s.split(' ').length;
			if (wordcount < 4) {
				ok = false;
			}

 		});

 		if (!ok) {
 			console.log("too few words");
 			$("#shortWarning").show();
 			$("#submitButton").prop("disabled",true);
 			return false;
 		}

		// Run a spell check when it is clicked
		//spellchecker.check();

 		$(".required").each(function(index,elt){
 			var repeat = 0;
 			var s = $(elt).val();
 			s = s.replace(/(^\s*)|(\s*$)/gi,"");
			s = s.replace(/[ ]{2,}/gi," ");
			s = s.replace(/\n /,"\n");
			var words = s.split(' ');
			var wordcount = s.split(' ').length;
			var result = [];
			for(var i = 0; i<words.length; i++){
				if(result.indexOf(words[i])==-1) {
					result.push(words[i]);
				}
				else {
					repeat += 1;
				}
			}

			if (repeat/wordcount>=0.5) {
				ok=false;
			}
 		});

 		if (!ok) {
 			console.log("more than 50% repeat words!");
 			$("#repeatWarning").show();
 			$("#submitButton").prop("disabled",true);
 			return false;
 		}



 		$(".required").each(function(index,elt) {
 			var count = 0;
 			var s = $(elt).val();
 			s = s.replace(/(^\s*)|(\s*$)/gi,"");
			s = s.replace(/[ ]{2,}/gi," ");
			s = s.replace(/\n /,"\n");
			var words = s.split(' ');
			var wordcount = s.split(' ').length;
			for (var i in words) {
				var valu = words[i];
				var value = valu.toLowerCase();;
				if (valu.length==1 && !(value==="a" || value==="i")) {
					count += 1;
				}
				//if (!$Spelling.BinSpellCheck(valu) && value!=="i") {
					//count += 1;
				//}
			}
			if (count/wordcount >= 0.5) {
				// They used more than 50% of words not in dictionary
				ok = false;
			}
 		});

 		// TODO: debug
 		if (!ok) {
 			console.log("more than 50% misspelled!");
 			$("#spellWarning").show();
 			$("#submitButton").prop("disabled",true);
 			return false;
 		}

 		// After running all checks, enable submit button
 		console.log("ready to submit!");
 		$("#canSubmit").show();
 		$("#submitButton").prop("disabled",false);

 	});
 	*/
 });
