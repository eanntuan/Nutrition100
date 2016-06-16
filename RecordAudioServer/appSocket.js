var socketIO = require('socket.io');
var ss = require('socket.io-stream');
var util = require('util');
var fs = require('fs');
var tmp = require('tmp');
var extfs = require('extfs');
var Recorder = require('./lib/recorder');
var logger = require('./lib/logger');
//var mkpath = require('mkpath');

// should require (./app) here and get locals from that

// %d is for the recording timestamp
//  var RECORDINGS_DIRECTORY_PROD = '/data/sls/scratch/psaylor/amt_nutrecordings';
//var RECORDINGS_DIRECTORY_PROD = '/s/nutrition/recordings/';
var RECORDINGS_DIRECTORY_PROD = '/afs/csail.mit.edu/u/e/eanntuan/Thesis/recordings/';
//var RECORDINGS_DIRECTORY_DEBUG = '/data/sls/scratch/dharwath/debug_recordings';
//var RECORDINGS_DIRECTORY_DEBUG = '/s/nutrition/recordings/debug_recordings/';
var RECORDINGS_DIRECTORY_DEV = __dirname + '/recordings'

// %d is for a number unique to each utterance within the same session
var RAW_FILE_NAME_FORMAT = '%s/utterance_%d.raw';
var WAV_FILE_NAME_FORMAT = '%s/utterance_%d.wav';
var TXT_FILE_NAME_FORMAT = '%s/utterance_%d.txt';

var AppSocket = function(server, appLocals) {

    var io = socketIO(server, {
        resource: appLocals.pathPrefix + 'socket.io',
        //  transports: ['websocket'],
    });

    logger.info('Created new io:', io);

    io.on('connection', function(socket) {
        logger.info('Connected to client socket', {
            // client: socket.client,
            conn: socket.conn.id,
            connMore: socket.conn.server.pingTimeout,
            request: socket.request,
            id: socket.id,
        }); 

        var recordToDir;
        if (appLocals.production) {
            recordToDir = RECORDINGS_DIRECTORY_PROD;
        } else {
            recordToDir = RECORDINGS_DIRECTORY_DEV;
        }
        
        logger.debug('Recording to dir:', recordToDir);
        
        //Getting timestamp for the file names
        //var dateObj = new Date(year, month, day, hours, minutes);
        /*var month = dateObj.getUCMonth() + 1; //months from 1-12
        var day = dateObj.getUTCDate();
        var year = dateObj.getUTCFullYear();
        
        var hr = d.getHours();
        var min = d.getMinutes();
        if (min < 10) {
            min = "0" + min;
        }
        var ampm = hr < 12 ? "am" : "pm";
        
        var newdate = year + "/" + month + "/" + day + " - " + hr + ":" + min + ampm;
        */
        
        var currentdate = new Date(); 
        var date = (currentdate.getMonth()+1) + "-" 
	        + currentdate.getDate() + "-"
	        + currentdate.getFullYear();
        
        var time = currentdate.getHours() + ":" + currentdate.getMinutes() + "_";
        var month = currentdate.getMonth()+1;
        var year = currentdate.getFullYear();
        var day = currentdate.getDate();

        var date = (currentdate.getMonth()+1) + "-" + currentdate.getDate() + "-" + currentdate.getFullYear() + "-";
        var time = currentdate.getHours() + ":" + currentdate.getMinutes() + "_";
        
        logger.debug('Timestamp: ', date + "-" + time);

        if(!fs.existsSync(RECORDINGS_DIRECTORY_PROD + year)){
        	logger.debug(RECORDINGS_DIRECTORY_PROD + year + " directory doesn't exist");
        	fs.mkdirSync(RECORDINGS_DIRECTORY_PROD + year);
        	logger.debug('1.Created new recordings dir at', RECORDINGS_DIRECTORY_PROD + year);
        }
       
        if(!fs.existsSync(RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month)){
        	logger.debug(RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month + " directory doesn't exist");
        	fs.mkdirSync(RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month);
        	logger.debug('2.Created new recordings dir at', RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month);
        }
        
        if(!fs.existsSync(RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month + "/Day-" + day)){
        	logger.debug(RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month + "/Day-" + day + " directory doesn't exist");
        	fs.mkdirSync(RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month + "/Day-" + day);
        	logger.debug('3.Created new recordings dir at', RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month + "/Day-" + day);
        }
        
        var recordToDir = RECORDINGS_DIRECTORY_PROD + year + "/Month-" + month + "/Day-" + day;
        
        var recordingsDirOptions = {
                mode: 0755,
                prefix: date + "-" + time,
                postfix: '',
                dir: recordToDir,
            };
        
        var recordingsDirObj = tmp.dirSync(recordingsDirOptions);
        var recordingsDir = recordingsDirObj.name;
        logger.debug('4.Created new recordings dir at', recordingsDir);
        console.log('Created new recordings dir at', recordingsDir);
        var streamId = 0;

        ss(socket).on('audioStream', function(stream, data) {
            logger.info('Receiving stream audio for data', data);
            

            //var streamId = data.fragment;
            //var streamText = data.text.toLowerCase().replace('.', '') + '\n';
            
          
            
            var clientSampleRate = data.sampleRate;

            var rawFileName = util.format(RAW_FILE_NAME_FORMAT, recordingsDir, streamId);
            var wavFileName = util.format(WAV_FILE_NAME_FORMAT, recordingsDir, streamId);
            var txtFileName = util.format(TXT_FILE_NAME_FORMAT, recordingsDir, streamId);
            
            if(data.text){
            	console.log('data.text: ' + data.text);
            	var streamText = data.text.toLowerCase().replace('.', '') + '\n';
            	logger.debug('Saving converted txt to file ' + txtFileName);
                console.log('Saving converted txt to file ' + txtFileName);
                
                fs.writeFile(txtFileName, streamText);
            }
            
           
            //logger.debug('Saving raw audio to file ' + rawFileName);
            //console.log('Saving raw audio to file ' + rawFileName);
            
            logger.debug('Saving converted wav audio to file ' + wavFileName);
            console.log('Saving converted wav audio to file ' + wavFileName);
            
            var rawFileWriter = fs.createWriteStream(rawFileName, {encoding: 'binary'});

            streamId+=1;
            
            stream.on('end', function(e) {
                logger.info('audio stream ended', {
                    streamId: streamId,
                    socketId: socket.id,
                });
            });

            var onWavConversion = function (err, result) {
                if (err) {
                    logger.error('Error converting wav file', err);
                    socket.emit('audioStreamResult', {
                        success: false,
                        fragment: streamId,
                        index: data.index,
                    });
                    return;
                }
                logger.debug('Success converting wav file', {streamId: streamId, result: result});
                socket.emit('audioStreamResult', {
                    success: true,
                    path: wavFileName,
                    fragment: streamId,
                    index: data.index,
                });
            };

            
            stream.pipe(rawFileWriter);
            Recorder.convertToWav(stream, clientSampleRate, wavFileName, onWavConversion);
            
        });

        socket.on('disconnect', function () {
            logger.info('Socket disconnect', {
                id: socket.id, 
                recordingDir: recordingsDir
            });
            extfs.isEmpty(recordingsDir, function(empty) {
                if (empty) {
                    logger.debug('Removing recording dir', recordingsDir);
                    recordingsDirNoTime = recordingsDir.split('_')[1];
                    //console.log("recordingsDirNoTime: " + recordingsDirNoTime);
                    fs.rmdir(recordingsDir, function (err) {
                        logger.error('Error removing recording dir:', err);
                    });
                }
            });
        });

    });

};


module.exports = AppSocket;
