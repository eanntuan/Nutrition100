//var tableWeights={};
//var finalHits={};
//var currentLevels={};
var rowsToFoodItems={};
var foodItemsToRows={};
(function($) {

    var nuturl;
    var nutname;

    // type of approach for associating foods w/ attrs: FST|Simple|Dependency
    var segment_type = "CRF";
    var labelRep = "IOE";
    var tag_type = "mallet"; // semantic tagging type: semicrf|mallet
    var responseNum = 1;
    var numSpoken = 0;
    var numTyped = 0;
    var foodNum = 0;

    (function(){	
    	var scripts = document.getElementsByTagName("script");
    	var src = scripts[scripts.length-1].src;
    	var pos = src.lastIndexOf('/');
    	nuturl = src.substring(0, pos+1);
    	nuturlNLP = "http://localhost:8080/Nutrition103-NLP/";
    	nutname = src.substring(pos+1, src.indexOf(".", pos+1));
    	
    	$('<link rel="stylesheet" type="text/css" href="'+nuturl+nutname+'.css'+'">').appendTo("head");
    	$('<script src="http://code.google.com/p/jquery-json/"></script>').appendTo("head");
    }());

    $(document).ready(function() {
    	// check if Nutrition or Nutrition_AMT (disable AMT stuff if Nutrition)
    	if (nuturl.indexOf("AMT")==-1 & nuturl.indexOf("Eval")==-1) {
    		$("#introDiv").hide();
    		$("#finalDiv").hide();
    		$("#speech-mic").prop("disabled",false);
		 	$("#textEntry").prop("disabled",false);
    	} 
    	else if (!(nuturl.indexOf("Eval")==-1)){
    		console.log("Nutrition_Eval");
    		//$("#textEntry").prop("disabled",false);
    		$("#innerInstructionDiv").html("<p>Please record <b>two</b> meals (e.g. what you ate for breakfast, lunch, dinner, or snack today or yesterday) using as much detail and accuracy as possible. Be creative--we will not accept repeat answers. Try to include as much additional information as you remember, such as brand names and quantities. Please press the microphone icon to describe your meal orally (note that it requires using Chrome).</p><p>Please interact with the system to narrow down the USDA hits to one food. Then <b>check the boxes</b> in the right-most column if the labels are correct, if the quantity is correct, and if the final USDA hit correctly matches the food you actually ate.</p><p>If you encounter any errors or have feedback from your experience using the system, please let us know!</p><h3>Examples</h3><p>I had a boiled egg, a Thomas's english muffin, and an ounce of organic butter.</p><p>For lunch I ate a plate of spaghetti with a spinach salad and feta cheese.</p>");
    	} 
    	
    	//supposed to have the cursor focus on the textbox at the start of the page load
    	$("textEntry").focus();

    	$("#textEntrySubmit").click (function () {
    		tag($("#textEntry").val());
    	});
    	
    	$("#textEntry").keypress(function(event) {
    	    if (event.which == 13) {
    	        event.preventDefault();
    	        var sentence = $("#textEntry").val();
    	        numTyped += 1;
    	        tagAndWriteResponse(sentence);
    	        
    	        // clear the input textbox
    	        $("#textEntry").val("");
    	        
    	    }
    	});
    	
		// prevent submission upon entering feedback
    	$("#feedback").keypress(function(event) {
    	    if (event.which == 13) {
    	        event.preventDefault();
    	        var feedback = $("#feedback").val();
    	        //$("#feedback").val("");
    	    }
    	});
    	
        try {
            var SpeechRecognition = SpeechRecognition || webkitSpeechRecognition;
            var recognition = new SpeechRecognition();
            //$("#speech-page-content").text("Got recognition");
            console.log("Got recognition");
        } catch(e) {
            var recognition = Object;
            //$("#speech-page-content").text(e);
            console.log(e);
        }
        recognition.continuous = true;
        recognition.interimResults = true;

        var interimResult = '';
        var textAreaID = 'tagged-query';
        var textArea = $('#tagged-query');
        var table = $('#dependencies');
        var resultsArea = $('#database-results');
        var table = $('<table class=center></table>')
        	.attr('border', '1')
        	.css({textAlign: 'center'})
        	.addClass('table table-bordered');
	    $('#dependencies').append(table); 
	    //$('#dependencies').addClass('table');
	    
	    //table.append("<td><strong>Image</strong></td>");
	    if (!(nuturl.indexOf("Eval")==-1)){
		    var thead = $('<thead><tr class="active"><th>Food</th><th>Quantity</th><th>USDA Hits</th><th>Are the color-coded labels for this food (shown above) correct?</th><th>Is the quantity correct?</th><th>Is the USDA hit correct?</th></tr></thead>');	
	    } else {
		    var thead = $('<thead><tr class="active"><th>Food</th><th>Quantity</th><th>USDA Hits</th></tr></thead>');
	    }
	    table.append(thead);
	    
	    //table.append("<th>Food</th>");
	    //table.append("<th>Quantity</th>");
	    //table.append("<td><strong>Brand</strong></td>");
	    //table.append("<td><strong>Description</strong></td>");
	    //table.append("<th class=columnHeader><strong>USDA Hits</strong></th>");
//	    if (!(nuturl.indexOf("Eval")==-1)){
//	    	table.append("<td class=columnHeader><strong>Are the color-coded labels for this food (shown above) correct?</strong></td>");
//	    	table.append("<td class=columnHeader><strong>Is the quantity correct?</strong></td>");
//	    	table.append("<td class=columnHeader><strong>Is the USDA hit correct?</strong></td>");
//	    }
	    //table.append("<td><strong>Semantic3 Hits</strong></td>");
	    table.hide(); 
	    var tableRow=0;

        $('#speech-mic').click(function(){
            toggleRecognition();
        });

        var listening = false;
        var toggleRecognition = function() {
        	//changes the color of the button to red to indicate recording
        	$('#speech-mic').toggleClass('ButtonClicked');
        	console.log("buttton clicked");
        	if (listening){
        		recognition.stop();
        	} else {
        		textArea.focus();
        		recognition.start();
        		$("#speech-mic").prop('value', 'Stop Recording');
        		listening = true;
        	}
        };
        
	function tagAndWriteResponse(sentence){
		$("#similarWarning").hide();
 		$("#incompleteWarning").hide();
 		$("#shortWarning").hide();
 		$("#spellWarning").hide();
 		$("#repeatWarning").hide();
 		$("#canSubmit").hide();
 		$("#radioBtnWarning").hide();
        
		// skip empty entry
        if (sentence===""){
        	return;
        }
        
        // assign entry to input response
        $("#response"+responseNum).val(sentence);
        console.log("response"+responseNum+": "+$("#response"+responseNum).val());
 			
		// tag the sentence 
        tag(sentence);
        
        // if AMT, don't check the diary
        if (nuturl.indexOf("AMT")==-1 & nuturl.indexOf("Eval")==-1) {
        	return;
        }
        
        // Perform a check of the data and prevent accidental submission
        // check that there are > 4 words in the diary
        sentence = sentence.replace(/(^\s*)|(\s*$)/gi,"");
        sentence = sentence.replace(/[ ]{2,}/gi," ");
        sentence = sentence.replace(/\n /,"\n");
		var wordcount = sentence.split(' ').length;
		if (wordcount < 4) {
			console.log("too short!");
			$("#shortWarning").show();
 			$("#submitButton").prop("disabled",true);
 			return false;
		} 
		
		// check that there's not > 50% repeat words
		var repeat = 0;
		sentence = sentence.replace(/(^\s*)|(\s*$)/gi,"");
		sentence = sentence.replace(/[ ]{2,}/gi," ");
		sentence = sentence.replace(/\n /,"\n");
		var words = sentence.split(' ');
		var wordcount = sentence.split(' ').length;
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
			console.log("more than 50% repeat words!");
 			$("#repeatWarning").show();
 			$("#submitButton").prop("disabled",true);
 			return false;
 		}
			
		// check that < 50% of words are misspelled
 		var count = 0;
 		sentence = sentence.replace(/(^\s*)|(\s*$)/gi,"");
 		sentence = sentence.replace(/[ ]{2,}/gi," ");
 		sentence = sentence.replace(/\n /,"\n");
		var words = sentence.split(' ');
		var wordcount = sentence.split(' ').length;
		for (var i in words) {
			var valu = words[i];
			var value = valu.toLowerCase();;
			if (valu.length==1 && !(value==="a" || value==="i")) {
				count += 1;
			}
			if (!$Spelling.BinSpellCheck(valu) && value!=="i") {
				count += 1;
			}
		}
		
		if (count/wordcount >= 0.5) {
			// They used more than 50% of words not in dictionary
			console.log("more than 50% misspelled!");
	 		$("#spellWarning").show();
	 		$("#submitButton").prop("disabled",true);
	 		return false;
		}

		// check that diary is not a repeat
		$.ajax({
 			url: "https://projects.csail.mit.edu/slu/readPy.php",
 			data:{sent:sentence},
 			async: false}).done(function(data) {
 				console.log("reading data: "+data);
 				var comp = data.localeCompare("yes");
 				if(comp==1 || comp==0){
 					console.log("too similar!");
 		 			$("#similarWarning").show();
 		 			$("#submitButton").prop("disabled",true);
 		 			return false;
 				} else {
 			        
 			        // write the value in the text box to the corpus
 					// files located in /afs/csail.mit.edu/proj/slu/www/data
 			 		$.ajax({
 			 			url: "https://projects.csail.mit.edu/slu/writePy.php",
 			 			data: {sent:sentence},
 			 			async: false}).done(function(data){
 			 	 			console.log("writing data: "+data);
 			 		});
 					
 					// only increment responseNum if passes all the checks
 					responseNum+=1;
 					console.log("increment response num");
 					
 					// enable submit if all checks pass and responseNum > 4
 					// only one required for evaluating the system
 					// ensure at least one food
 					if (((responseNum > 4) || (!(nuturl.indexOf("Eval")==-1) & responseNum > 2)) & foodNum > 0){
 						console.log("ready to submit!");
 						
 						// when ready to submit, add table for substitutions and deletions
 						if (!(nuturl.indexOf("Eval")==-1)){
 							var table2 = $('<table class=center></table>')
 				        		.attr('border', '1')
 				        		.css({textAlign: 'center'})
 				        		.addClass('table table-bordered');
 	 				        $('#deletions').append(table2); 
 	 					    var thead2 = $('<thead><tr class="active"><th>Were any foods missed?</th><th>Were any non-food words labeled as foods?</th></tr></thead>');
 	 					    table2.append(thead2);
 	 				        //table2.append("<td class=columnHeader><strong>Were any foods missed?</strong></td>");
 	 					    //table2.append("<td class=columnHeader><strong>Were any non-food words labeled as foods?</strong></td>");
 	 					    var row = $('<tr id="checkboxes-delete-subst"></tr>'); 
 							row.append("<td></td>");
 							row.append("<td></td>");
 							table2.append(row);

 							// deletion radio buttons
 							substDelRadioBtns("radio_del", 1);
 							
 							// substitution radio buttons
 							substDelRadioBtns("radio_subst", 2);
 						}
 						
 				 		$("#canSubmit").show();
 				 		$("#submitButton").prop("disabled",false);
 				 		console.log("numSpoken "+numSpoken);
 				 		console.log("numTyped "+numTyped);
 				 		$("#numSpoken").val(numSpoken);
 				 		$("#numTyped").val(numTyped);

 				 		// disable record button and text input
 			 			$("#incompleteWarning").hide();
 				 		$("#speech-mic").prop("disabled",true);
 				 		$("#textEntry").prop("disabled",true);
 					} else {
 						console.log("not complete yet!");
 						console.log($("#submitButton").is(':disabled'));
 						if (!(nuturl.indexOf("Eval")==-1)){
 							$("#incompleteWarning").html("You'll need to record two valid meals with foods before you can submit. Keep going!");
 						}
 			 			$("#incompleteWarning").show();
 					}
 				}
 		});

	}
	
        
    function displayTaggedResult(data){
	    var text = data.text;
	    var tokens = data.tokens;
	    var segments = data.segments;
	    var dependencies = data.attributes;
	    var databaseResults = data.results;
	    
	    // leave previous meal descriptions visible when evaluating the system
	    if (nuturl.indexOf("Eval")==-1) {
	    	textArea.html("");
	    }
	    for(var i=0; i<segments.length; i++){
	    	var segment = segments[i];

	    	var stext = tokens.slice(segment.start, segment.end).join('&nbsp');
	    	
	    	textArea.append("<div class='wordDiv "+segment.label+"'><p class='wordSpan'>"+stext+"<p class='wordCat'>"+segment.label+"</div>");
	    	textArea.append("<div style:'clear:left'></div>");
	    }
	    textArea.append("<div class='eolDiv'>");
	    textArea.append("<br/>");
	}
	
	function displayTable(data){
		var text = data.text;
	    var tokens = data.tokens;
	    var segments = data.segments;
	    var dependencies = data.attributes;
	    var databaseResults = data.results;
	    var semantic3Results = data.semantic3results;
	    var images = data.images;
	    var foodId = data.foodID;
	    
	    console.log("dependencies:");
	    console.log(dependencies);
	    console.log("data.images");
	    console.log(images);
	    
	    $(".changedComponent").removeClass("changedComponent");
	    //Allow modifications without food items 
	    if (jQuery.isEmptyObject(dependencies)&& !jQuery.isEmptyObject(rowsToFoodItems)) {
	    	console.log(segments);
	    	var changed=false;
	    	var cellsToUpdate=[];
	    	var descrReset=false;
	    	for(var i=0; i<segments.length; i++) {
	    		var segment = segments[i];
	    		var stext = tokens.slice(segment.start, segment.end).join(' ');
	    		if (segment.label=="Description"){
	    			entry=rowsToFoodItems[tableRow];
	    			if (!descrReset) {
	    				entry.description=[];
	    				descrReset=true;
	    			}
	    			
	    			entry.addDescription(stext);
	    			entry.resetLevel();
	    			changed=true;
	    			cellsToUpdate.push("3");
	    		}
	    		if (segment.label=="Quantity"){
	    			entry=rowsToFoodItems[tableRow];
	    			entry.changeQuantity(stext);
	    			changed=true;
	    			cellsToUpdate.push("2");
	    		}
	    	}
	    	if (changed) {
	    		updateResults(tableRow, rowsToFoodItems[tableRow].levelUsed,"");
	    		console.log("cells to update: " + cellsToUpdate);
	    		for (var j=0;j<cellsToUpdate.length; j++) {
	    			$("#dependencies tr:nth-child("+foodItemsToRows[foodNoNums]+") td:nth-child("+cellsToUpdate[j]+")").addClass("changedComponent");
	    		}
	    		updatePhoto(tableRow);
	    	}
	    }
	    foodItemsThisEntry={};
	    for(var food in dependencies){
	    	// remove numeric values (i.e. indices) from food name
	    	foodNoNums = food.replace(/[0-9]/g, '');
	    	//Check if this is an update to an existing item
	    	if (foodItemsToRows[foodNoNums] !=null && foodItemsThisEntry[foodNoNums]==null) {
	    		var attributes = dependencies[food];
	    		var entry=rowsToFoodItems[foodItemsToRows[foodNoNums]];
	    		var currentRow=foodItemsToRows[foodNoNums];
	    		console.log(entry);
	    		var changed=false;
		    	var cellsToUpdate=[];
		    	var descrReset=false;
		    	// change the attributes for this food
		    	for(var i=0; i<attributes.length; i++) {
		    		var segment = attributes[i];
		    		var stext = tokens.slice(segment.start, segment.end).join(' ');
		    		if (segment.label=="Description"){
		    			if (!descrReset) {
		    				entry.description=[];
		    				descrReset=true;
		    			}
		    			entry.addDescription(stext);
		    			console.log(entry);
		    			entry.resetLevel();
		    			changed=true;
		    			cellsToUpdate.push("3");
		    		}
		    		if (segment.label=="Quantity"){
		    			entry.changeQuantity(stext);
		    			changed=true;
		    			cellsToUpdate.push("2");
		    		}
		    	}
		    	if (changed) {
		    		console.log(currentRow);
		    		updateResults(currentRow, rowsToFoodItems[currentRow].levelUsed,"");
		    		console.log(cellsToUpdate);
		    		for (var j=0;j<cellsToUpdate.length; j++) {
		    			console.log($("#dependencies tr:nth-child("+foodItemsToRows[foodNoNums]+") td:nth-child("+cellsToUpdate[j]+")"));
		    			$("#dependencies tr:nth-child("+foodItemsToRows[foodNoNums]+") td:nth-child("+cellsToUpdate[j]+")").addClass("changedComponent");
		    		}
		    		updatePhoto(currentRow);
		    	}
	    	}  else {
	    	foodItemsThisEntry[foodNoNums]=1;
	    	table.show();
			var row = $('<tr id="food"'+tableRow+'"></tr>'); 
			var img = $('<img id="dynamic'+(tableRow+1)+'">'); 
			row.append('<td id="foodCol">'+foodNoNums+"<br></td>");
			for(var i=0;i<2;i++){
				row.append("<td></td>");
			}
			// add three extra columns for AMT system evaluation task
			if (!(nuturl.indexOf("Eval")==-1)){
				row.append("<td></td>");
				row.append("<td></td>");
				row.append("<td></td>");
			}
			
			table.append(row);
			tableRow++;
			
			var currentDatabaseResults=databaseResults[food];
			 currentFoodEntry= new foodEntry(foodNoNums, currentDatabaseResults.originalDescription,
					currentDatabaseResults.originalBrand, currentDatabaseResults.quantity, currentDatabaseResults.levelUsed,
					tableRow, [], databaseResults[food].results, databaseResults[food].weights);
			 currentFoodEntry.adjectivesRelevant = currentDatabaseResults.adjectiveRelevant;
			 rowsToFoodItems[tableRow]= currentFoodEntry;
			 foodItemsToRows[foodNoNums]=tableRow;
	    	var attributes = dependencies[food];
	    	
	    	// add the attributes for this food to its row in the table
	    	for(var i=0; i<attributes.length; i++) {
	    		var segment = attributes[i];
	    		var stext = tokens.slice(segment.start, segment.end).join(' ');
	    		if (segment.label=="Quantity"){
	    			$("#dependencies tr:nth-child("+tableRow+") td:nth-child(2)").text(stext);
	    		} 
	    	}
	    	
	    	// add the database hits for this food to last column of its row
	    	var features = databaseResults[food].features;
	    	var hits = databaseResults[food].results;
	    	var weights = databaseResults[food].weights;
	    	var currentLevel = currentFoodEntry.levelUsed;
	    	console.log(databaseResults[food]);
	    	stext = "";
	    	if (hits.length == 1) {
	    		//quantitySelectionText=quantitySelectionTextGeneration(weights, tableRow, databaseResults[food].quantityAmount);
	    		//console.log("quantity selection text: " + quantitySelectionText);
	    		$("#dependencies tr:nth-child("+tableRow+") td:nth-child(2)").html("<ul>"+quantitySelectionText+"</ul>").css('width', '480px').css('text-align', 'left');
	    		
	    		stext+=hits[0].longDesc+"<br>Calories: " + "<div class='calories' id='calories"+tableRow+"'>"+Math.round(hits[0].calories)+"</div>";
	    		console.log("food id: " + hits[0].foodID);
	    		if (hits[0].foodID=="-1") {
	    			stext+="<br><a class='sourceLink hover' target='_blank' href=http://www.nutritionix.com/search/item/"+hits[0].nutID+">Source: Nutritionix </a>";
	    		}else {
	    			stext+="<br><a class='sourceLink hover' target='_blank' href=http://ndb.nal.usda.gov/ndb/search/list?qlookup="+hits[0].foodID+">Source: USDA </a>";
	    		}

	    		stext += "</br><li id='refine"+tableRow+"'><a class='hover'>"+ "See more options" +"</a></li>";

	    	} else {
	    		if (hits.length==0) {
	    			stext+= "No USDA Results found."
	    		} else {
	    			console.log(currentDatabaseResults);
	    			if (currentDatabaseResults.adjectivesRelevant) {
	    				stext +="Select further adjectives:";
	    				for (var i=0;i<features.length;i++){
	    					stext += "<li><input class='feature"+tableRow+"' type='button' value='"+features[i]+"'>"+"</input></li>";
	    				}	
	    			} else {
	    				stext +="Select the best match:";
	    				for (var i=0;i<hits.length;i++){
	    					stext += '<li><input class="hitoption'+tableRow+'" type="button" value="'+hits[i].longDesc+'">'+'</input></li>';
	    				}	
	    			}
	    		
			stext += "<li class='hover' id='refine"+tableRow+"'><a class='hover'>"+ "See more options" +"</a></li>";
	       }}
	    	
	    	var foodID = hits[0].foodID;
	    	if (foodID.charAt(0) == '0'){
	    		foodID = foodID.substring(1, foodID.length);
	    	}
	    	
	    	console.log("hits[0].foodID: " + foodID);
	    	console.log(images);
	    	
	    	var path = "/scratch/images/";
	    	img.attr("src", images[foodID]);
			img.attr("border", '1');
			img.hide();
			img.on('load', setImgSize); 
			
			// add image and db results to row
			$("#dependencies tr:nth-child("+tableRow+") td:nth-child(1)").append(img);			
			$("#dependencies tr:nth-child("+tableRow+") td:nth-child(3)").append("<ul>"+stext+"</ul>").css('width', '480px').css('text-align', 'left');
			
			// add radio buttons for system evaluation task
			foodNum += 1;
			$("#numFoods").val(foodNum);
			console.log("foodNum "+foodNum);
			console.log("numfoods val: " + $("#numFoods").val());
			if (!(nuturl.indexOf("Eval")==-1)){
		         
		        // create semantic tag radio buttons
				yesNoRadioBtns("radio_tags", 4, foodNum);
				
				// create quantity radio buttons
				yesNoRadioBtns("radio_quantity", 5, foodNum);
				
				// create USDA hit radio buttons
				yesNoRadioBtns("radio_USDA", 6, foodNum);

			}
			console.log($("#dependencies tr:nth-child("+tableRow+") td:nth-child(3)"));
			if (hits.length==1) {
				console.log("table row: " + tableRow);
				updateCalories(tableRow);
			}
			
			makeUSDAListeners(tableRow);
	    	}}
	}
	
	/*Creates yes/no radio buttons for the given name in the specified column. */
	function yesNoRadioBtns(name, col, foodNum){
		var radioItem1 = document.createElement("input");
        radioItem1.type = "radio";
        radioItem1.name = "radioGrp"+name+foodNum;
        radioItem1.id = name+foodNum+"yes";
 
        var radioItem2 = document.createElement("input");
        radioItem2.type = "radio";
        radioItem2.name = "radioGrp"+name+foodNum;
        radioItem2.id = name+foodNum+"no";
 
        var objTextNode1 = document.createTextNode("Yes");
        var objTextNode2 = document.createTextNode("No");
 
        var objLabel = document.createElement("label");
        objLabel.htmlFor = radioItem1.id;
        objLabel.appendChild(radioItem1);
        objLabel.appendChild(objTextNode1);
 
        var objLabel2 = document.createElement("label");
        objLabel2.htmlFor = radioItem2.id;
        objLabel2.appendChild(radioItem2);
        objLabel2.appendChild(objTextNode2);
        
		$("#dependencies tr:nth-child("+tableRow+") td:nth-child("+col+")").append(objLabel);
		$("#dependencies tr:nth-child("+tableRow+") td:nth-child("+col+")").append(objLabel2);

	}
	
	/*Creates substitution/deletion radio buttons. */
	function substDelRadioBtns(name, col){

			// radio button labels (i.e. number of substitutions/deletions)
			numSubstDels = ["0", "1", "2", "3", "4"];
			
			// create radio button group (i.e., all share same name)
			for (i = 0; i < numSubstDels.length; i++) { 
				// create radio button
				var radioItem = document.createElement("input");
				radioItem.type = "radio";
				radioItem.name = "radioGrp"+name;
				radioItem.id = name+numSubstDels[i];
				
				var objTextNode = document.createTextNode(numSubstDels[i]);
				
				// create label, add radio button to it
				var objLabel = document.createElement("label");
		        objLabel.htmlFor = radioItem.id;
		        objLabel.appendChild(radioItem);
		        objLabel.appendChild(objTextNode);
				
				$("#deletions tr:nth-child(1) td:nth-child("+col+")").append(objLabel);

			}			
	}
	
	function makeUSDAListeners(tableRow) {
		
		console.log("tableRow make usda listeners: " + tableRow);
		var food = rowsToFoodItems[tableRow].itemName;
		$(".feature"+tableRow).click(function (event) {
		updateResults(tableRow, rowsToFoodItems[tableRow].levelUsed, event.target.value);
		
	}) ;
		
		$(".hitoption"+tableRow).click(function (event) {
			var entry = rowsToFoodItems[tableRow];
			var hits = entry.hits;
			console.log("Looking for "+event.target.value)
			//Move current to be the first hit
			for(var i=0; i<hits.length; i++) {
				
				if (hits[i].longDesc==event.target.value) {
					hits[0]=hits[i];
					console.log("found "+hits[i].longDesc)
					changePhoto(hits[i].foodID);
					break;
				}
			}
			//Filter weights to only include relevant ones
			var relevantIDs=hits[0].allFoodIDs;
			var newWeights=[];
			console.log(relevantIDs);
			console.log(entry.weights);
			for (var j=0; j<entry.weights.length; j++) {
				var found=false;
				for (var k=0; k<relevantIDs.length; k++) {
					for (var h=0; h<entry.weights[j].foodIDs.length;h++) {
						if (relevantIDs[k]==entry.weights[j].foodIDs[h]) {
							
							found=true;
							break;
						}
					}
					if (found) {
						newWeights.push(entry.weights[j]);
					}
					}
			}
			entry.weights=newWeights;
//			console.log(event.target.value+item+tableRow);
			quantitySelectionText=quantitySelectionTextGeneration(entry.weights, tableRow, entry.quantityAmount);
	    	$("#dependencies tr:nth-child("+tableRow+") td:nth-child(2)").html("<ul>"+quantitySelectionText+"</ul>").css('width', '480px').css('text-align', 'left');
	    		//updatedText+="Selected features: "+featureChosen+"</br>";
	    		updatedText="<ul>"+hits[0].longDesc+"<br>Calories: " + "<div class='calories' id='calories"+tableRow+"'>"+Math.round(hits[0].calories)+"<br></div>";
	    		updatedText+=" <a style='font-size: 8pt;' target='_blank' href=http://ndb.nal.usda.gov/ndb/search/list?qlookup="+hits[0].foodID+">Source: USDA </a>";
	    		updatedText += "</br><li id='refine"+tableRow+"'><a class='hover'>"+ "See more options" +"</a></li>";
  		    	updatedText += "</br><li id='back"+tableRow+"'><a class='hover'>"+ "Back" +"</a></li></ul>";
			$("#dependencies tr:nth-child("+tableRow+") td:nth-child(3)").html(updatedText).css('width', '480px').css('text-align', 'left');
			makeUSDAListeners(tableRow);
			window.updateCalories(tableRow);
		}) ;

		$("#refine"+tableRow).click(function (event) {
//			console.log(event.target.value+food);
			updateResults(tableRow, rowsToFoodItems[tableRow].levelUsed+1, "");
		}) ;
		$("#back"+tableRow).click(function (event) {
			rowsToFoodItems[tableRow].backUpLevel();
			updateResults(tableRow, rowsToFoodItems[tableRow].levelUsed, "");
		}) ;
	}
	
	function quantitySelectionTextGeneration (weights, tableRow, quantityAmount) {
		var stext="Quantity: <input type='text' id='quantityInput"+tableRow+
		"' class='quantityInput' value='"+quantityAmount+"' onchange='updateCalories("+tableRow+");'> ";
		//stext="<table class='quantitySelectionTable'><tr> <td>Quantity:</td> <td><input type='text' class='quantityInput' value='1'></td><td>";
		stext+="<select id='quantitySelect"+tableRow+"' onchange='updateCalories("+tableRow+");''>";
		for (var i=0; i<weights.length;i++) {
			stext+="<option>"+weights[i].msre_Desc+"</option>"
		}
		stext+="</select></br>";
		return stext;
	};
	
	window.updateCalories= function (tableRow) {
		console.log("table row in update calories: " + tableRow);
		var amount=$('#quantityInput'+tableRow).val();
		var currentRate =  rowsToFoodItems[tableRow].weights[$('#quantitySelect'+tableRow).prop("selectedIndex")];
		if (currentRate.gmwgt==-1) {
			$("#calories"+tableRow).html(rowsToFoodItems[tableRow].hits[0].calories*amount/currentRate.amount);
		}else {
			var conversionRate = currentRate.gmwgt*1.0/currentRate.amount;
			$("#calories"+tableRow).html(Math.round(rowsToFoodItems[tableRow].hits[0].calories*amount*conversionRate/100));
		}
	};
	
	// resize the image, but keep aspect ratio
	function setImgSize() {
		var maxWidth = 150; // Max width for the image
        var maxHeight = 150;    // Max height for the image
        var ratio = 0;  // Used for aspect ratio
        var width = $(this).width();    // Current image width
        var height = $(this).height();  // Current image height

        // Check if the current width is larger than the max
        if(width > height && width > maxWidth){
            ratio = maxWidth / width;   // get ratio for scaling image
			$(this).css("width", maxWidth); // Set new width
            $(this).css("height", height * ratio);  // Scale height based on ratio
        }

        // Check if current height is larger than max
        else if(height > maxHeight){
            ratio = maxHeight / height; // get ratio for scaling image
			$(this).css("height", maxHeight);   // Set new height
			$(this).css("width", width * ratio);    // Scale width based on ratio
        }
        $(this).show();
	}

	//Updates results for new adjectives
	function updateResults (tableRow, level, adjectiveAdded) {
		console.log("Updating row "+tableRow);
		var entry = rowsToFoodItems[tableRow];
		if (!adjectiveAdded=="") {
			entry.addExtraAdjective(adjectiveAdded);
		}
		$.getJSON(nuturl+'FeatureLookup'+'?jsonp=?', {'item' : entry.itemName, 'description': JSON.stringify(entry.description),
			'featureChosen':JSON.stringify(entry.addedAdjectives), 'brand' : entry.brand, 'level': level, 'quantity': entry.quantity},
			     function(updatedResult){
	  				console.log(updatedResult);
	  	
	  				var features = updatedResult.features;
	  		    	var hits = updatedResult.results;
	  		    	entry.weights=updatedResult.weights;
	  		    	entry.levelUsed=updatedResult.levelUsed;
	  		    	entry.adjectivesRelevant=updatedResult.adjectivesRelevant
	  		    	
	  		    	console.log("hits in update results: ");
	  		    	console.log(hits);
	  		    	if (entry.previouslevels[entry.previouslevels.length-1]!=entry.levelUsed) {
	  		    		entry.previouslevels.push(entry.levelUsed);
	  		    	}
	  		    	
	  		    	entry.hits=hits;
	  		    	updatedText = "";
	  		    	if (hits.length==0) {
	  		    		updatedText="No USDA Hits found";
	  		    	} else {if (hits.length == 1) {
	  		    		quantitySelectionText=quantitySelectionTextGeneration(entry.weights, tableRow, updatedResult.quantityAmount);
	  		    		$("#dependencies tr:nth-child("+tableRow+") td:nth-child(2)").html("<ul>"+quantitySelectionText+"</ul>").css('width', '480px').css('text-align', 'left');
	  		    		
	  		    		//updatedText+="Selected features: "+featureChosen+"</br>";
	  		    		updatedText+=hits[0].longDesc+"<br>Calories: " + "<div class='calories' id='calories"+tableRow+"'>"+Math.round(hits[0].calories)+"</div><br>";
	  		    		updatedText+=" <a style='font-size: 8pt;' target='_blank' href=http://ndb.nal.usda.gov/ndb/search/list?qlookup="+hits[0].foodID+">Source: USDA </a>";
	  		    	} else {
	  		    		console.log(currentFoodEntry.adjectivesRelevant);
	  		    		console.log(currentFoodEntry);
	  		    		if (currentFoodEntry.adjectivesRelevant) {
		    				updatedText +="Select further adjectives:";
		    				for (var i=0;i<features.length;i++){
		    					updatedText += "<li><input class='feature"+tableRow+"' type='button' value='"+features[i]+"'>"+"</input></li>";
		    				}	
		    			} else {
		    				updatedText +="Select the best match:";
		    				for (var i=0;i<hits.length;i++){
		    					updatedText += '<li><input class="hitoption'+tableRow+'" type="button" value="'+hits[i].longDesc+'">'+'</input></li>';
		    				}	
		    			}	  				
	  		       }
	  		    	updatedText += "</br><li id='refine"+tableRow+"'><a class='hover'>"+ "See more options" +"</a></li>";}
	  		    	updatedText += "<li id='back"+tableRow+"'><a class='hover'>"+ "Back" +"</a></li>";
	  				//console.log(updatedText);
//	  				$("#dependencies tr:nth-child("+tableRow+") td:nth-child(5)").text("");
	  				$("#dependencies tr:nth-child("+tableRow+") td:nth-child(3)").html("<ul>"+updatedText+"</ul>");
//	  				makeSubsequentUSDAListeners (item, tableRow, hits, featureChosen, updatedResult.weights,brand);
	  				if (hits.length==1){
	  					window.updateCalories(tableRow);
	  				}
	  				makeUSDAListeners(tableRow);
			      });
	}
	
	function changePhoto(foodID){
		console.log("change photo food ID: " + foodID);
		var src;
		if (foodID.charAt(0) == '0'){
    		foodID = foodID.substring(1, foodID.length);
    	}
		
		encodeImageFileAsURL(foodID);
		//$("#dynamic"+tableRow).attr("src", src);
		//console.log($("#dynamic"+tableRow).attr("src"));
	}
	
	function encodeImageFileAsURL(foodID){
		//encodes image file as url
		var path = "/scratch/images/";
		var filePath = path + foodID + ".png";
		//var filePath = new File([blob], path + foodID + ".png");
		/*
		var reader = new FileReader();

		if(file){
			reader.readAsDataURL(file);
		}
        reader.onloadend = function(e){
        	$("#dynamic"+tableRow).attr("src", reader.result);
        }
        */
        
        var xhr = new XMLHttpRequest();       
        xhr.open("GET", filePath, true); 
        xhr.responseType = "blob";
        xhr.onload = function (e) {
        	console.log(this.response);
            var reader = new FileReader();
            reader.onload = function(event) {
            var res = event.target.result;
            console.log(res)
            $("#dynamic"+tableRow).attr("src", res);
         }
            var file = this.response;
            reader.readAsDataURL(file)
            console.log(file);
            $("#dynamic"+tableRow).attr("src", reader.result);
        };
        xhr.send()

	}
	
	function updatePhoto (tableRow) {
		console.log("update photo tableRow " + tableRow);
		var entry = rowsToFoodItems[tableRow];
		
		$.getJSON(nuturl+'UpdatePhoto'+'?jsonp=?', {'item' : entry.itemName, 'description': JSON.stringify(entry.description),
			'brand' : entry.brand},
			     function(updatedResult){
//				var img = $("#dynamic"+tableRow); 
				console.log($("#dynamic"+tableRow));
				//console.log($("#dynamic"+tableRow).attr("src"));
				if (updatedResult.image !="") {
					$("#dynamic"+tableRow).attr("src", updatedResult.image);
					console.log($("#dynamic"+tableRow).attr( "src" ));
				}
	  				console.log(updatedResult);
			});
	}
	
	function tag(text){
	    // first display the recognized speech with CRF labels
	    $.getJSON(nuturlNLP+nutname+'?jsonp=?', {'text' : text, 'segment_type' : segment_type, 'labelRep' : labelRep, 'tag_type' : tag_type},
		      function(data){
	    		console.log("went to Nutrition103-NLP and got tagged results");
	    	  	displayTaggedResult(data);
	    	  	
	    	  	console.log("data");
	    	  	console.log(data);
	    	  	//var stringifyData = $.toJSON(data);
	    	  	//console.log("stringify data");
	    	  	//console.log(stringifydata);

	    	  	// serialize received data before sending to Images
	    	  	var serializedData = $.param(data);
	    	  	//console.log("serializedData");
	    	  	//console.log(serializedData);
	    	  	
	    	  	// get images after table with db info
	    	  	// then display table with images and db info
	    	  	console.log("nuturl: " + nuturl);
	    	  	$.getJSON(nuturl+'Images'+'?jsonp=?', {'text' : text, 'segment_type' : segment_type, 'labelRep' : labelRep, 'tag_type' : tag_type},
	    			     function(dataWithImages){
	    	  				console.log("data with images data coming");
	    	  				console.log(dataWithImages);
	    		    	  	displayTable(dataWithImages);
	    			      });
	    });
	}
//	tag("I had cereal and milk");
        recognition.onresult = function (event) {
        	console.log("on result")
            for (var i = event.resultIndex; i < event.results.length; ++i) {
                if (event.results[i].isFinal) {
                	var text = event.results[i][0].transcript.trim();
                	numSpoken += 1;
        	        tagAndWriteResponse(text);
                } else {

                }
            }
        };

        recognition.onend = function() {
	    $("#speech-mic").prop('value', 'Start Recording');
	    listening = false;
        };
    });
})(jQuery);

//Adding definition of food item entry
function foodEntry (itemName, description, brand, quantity, levelUsed, rowNumber, addedAdjectives, hits, weights) {
	this.itemName=itemName;
	this.description=description;
	this.brand=brand;
	this.levelUsed=levelUsed;
	this.rowNumber=rowNumber;
	this.quantity=quantity;
	this.weights=weights;
	this.addedAdjectives=addedAdjectives;
	this.hits=hits;
	this.previouslevels=[0,levelUsed];
	this.adjectivesRelevant=true;
	this.quantityAmount=1;
	this.addExtraAdjective=function(adj) {
		this.addedAdjectives.push(adj);
	}
	this.addDescription=function(adj) {
		//this.description=[];
		this.description.push(adj);
	}
	this.resetLevel=function() {
		this.levelUsed=0;
		this.previousLevels=[0];
	}
	this.changeQuantity=function(quant) {
		this.quantity=quant;
	}
	this.addWeights=function(weight) {
		this.weights=weight;
	}
	this.backUpLevel= function () {
		console.log("back up level");
		if (this.previouslevels.length>1) {
		this.previouslevels.pop();
		this.levelUsed=this.previouslevels[this.previouslevels.length-1];
		}
	}
}
