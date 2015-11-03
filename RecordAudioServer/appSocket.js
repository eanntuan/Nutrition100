var socketIO = require('socket.io');
var ss = require('socket.io-stream');
var util = require('util');
var fs = require('fs');
var tmp = require('tmp');
var extfs = require('extfs');
var Recorder = require('./lib/recorder');
var logger = require('./lib/logger');

// should require (./app) here and get locals from that

// %d is for the recording timestamp
//  var RECORDINGS_DIRECTORY_PROD = '/data/sls/scratch/psaylor/amt_nutrecordings';
//var RECORDINGS_DIRECTORY_PROD = '/data/sls/scratch/dharwath/nutrition_test/datasets/amt_nutrecordings';
var RECORDINGS_DIRECTORY_PROD = '/s/nutrition/recordings/';
//var RECORDINGS_DIRECTORY_DEBUG = '/data/sls/scratch/dharwath/debug_recordings';
var RECORDINGS_DIRECTORY_DEBUG = '/s/nutrition/recordings/debug_recordings/';
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
        var recordingsDirOptions = {
            mode: 0755,
            prefix: 'r_',
            postfix: '',
            dir: recordToDir,
        };
        var recordingsDirObj = tmp.dirSync(recordingsDirOptions);
        var recordingsDir = recordingsDirObj.name;
        logger.debug('Created new recordings dir at', recordingsDir);
        
        var streamId = 0;

        ss(socket).on('audioStream', function(stream, data) {
            logger.info('Receiving stream audio for data', data);

            //var streamId = data.fragment;
            //	var streamText = data.text.toLowerCase().replace('.', '') + '\n';
            var clientSampleRate = data.sampleRate;

            var rawFileName = util.format(RAW_FILE_NAME_FORMAT, recordingsDir, streamId);
            var wavFileName = util.format(WAV_FILE_NAME_FORMAT, recordingsDir, streamId);
            //	var txtFileName = util.format(TXT_FILE_NAME_FORMAT, recordingsDir, streamId);
            
           
            logger.debug('Saving raw audio to file ' + rawFileName);
            console.log('Saving raw audio to file ' + rawFileName);
            
            logger.debug('Saving converted wav audio to file ' + wavFileName);
            console.log('Saving converted wav audio to file ' + wavFileName);
            
            var rawFileWriter = fs.createWriteStream(rawFileName, {encoding: 'binary'});
            //	fs.writeFile(txtFileName, streamText);

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
                    fs.rmdir(recordingsDir, function (err) {
                        logger.error('Error removing recording dir:', err);
                    });
                }
            });
        });

    });

};


module.exports = AppSocket;
