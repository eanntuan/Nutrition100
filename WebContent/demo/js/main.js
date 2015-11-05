require.config({
    paths: {
        'jquery': 'https://code.jquery.com/jquery-1.11.0.min',
        'ractive': 'https://cdnjs.cloudflare.com/ajax/libs/ractive/0.7.2/ractive.min',
        'spoke': 'spoke',
    },
});

require(['jquery', 'ractiveUi', 'spoke'], 
	function($, ui, spoke, mic) {
	
    console.log("Loaded requirements for main.js");
    
    //var socket = spoke.sharedSocket.getSocket({url: "http://localhost:8002", path: "/socket.io/"});
    var socket = spoke.sharedSocket.getSocket({url: "http://gentle-ben.csail.mit.edu:8002", path: "/socket.io/"});
    socket.on('connect', function () {
        ui.component.set('connected', true);
    });

    socket.on('disconnect', function () {
        ui.component.set('connected', false);
    });

    /* Set up microphone volume meter */
    var meter = $('#speech-mic');
    var volumeMeter = spoke.microphone.VolumeMeter(meter);
    volumeMeter.adjustVolumeMeter = function (volumeLevel) {
        var adjustedHeight = this._computeMeterHeight(volumeLevel);
        ui.component.set('meterMaxHeight', adjustedHeight + '%');
    };
    
    
    var recordButton = $('.record-btn');
    
    var expectedText = recordButton.data('text');
    var index = recordButton.data('uttindex');
    var uttId = recordButton.data('uttid');
    /* 
        Recorder setup
    */
    var recOptions = {
        audioMetadata: {
            fragment: uttId,
            index: index,
            text: expectedText,
        },
    };
    
    var recorder = spoke.Recorder(recordButton, recOptions);
    
    var onToggleData = {
            index: index,
        };
    
    recorder.on('start.spoke.recorder', onToggleData, function (event) {
        ui.component.set('recordingFragment', event.data.index);
        ui.component.set('recording', true);
    });

    recorder.on('stop.spoke.recorder', onToggleData, function (event) {
        var index = event.data.index;
        ui.component.set('recordingFragment', -1);
        ui.component.set('recording', false);
        //	ui.data.recognizedState[index] = ui.RecognizedStateEnum.PENDING;
        ui.component.set('recognizedState', ui.data.recognizedState);
    });
    
    var uttPath = $('#utt_' + index + '_path');
    recorder.on('result.spoke.recorder', onToggleData, function (event, result) {
        if (result.index === onToggleData.index) {
            console.log('Recorder result:', result);
            //	ui.data.savedState[result.index] = result.success;
            //	ui.component.set('savedState', ui.data.savedState);
            uttPath.val(result.path);
        }
    });
    
    /* Set up UI changes on record-btn click*/
    recordButton.toggle(
		function () { 
			ui.component.set('recording', true);
		},
		function () { 
			ui.component.set('recording', false);
	});
    
    socket.on('audioStreamResult', function (result) {
        console.log('Audio stream result for', result.index, ',', result.fragment,':', result.success);
        //	ui.data.savedState[result.index] = result.success;
        ui.component.set('savedState', ui.data.savedState);
    });

});