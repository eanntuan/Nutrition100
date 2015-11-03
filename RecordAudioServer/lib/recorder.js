var fs = require('fs');
var SoxCommand = require('sox-audio');
var exec = require('child_process').exec;
var execFile = require('child_process').execFile;
var spawn = require('child_process').spawn;
var logger = require('../lib/logger');

var Recorder = function () {

};

var RECOGNIZER_SAMPLE_RATE = 16000;

Recorder.convertToWav = function (rawInputFile, sampleRate, wavOutputFile, cb) {
    var command = SoxCommand(rawInputFile)
        .inputSampleRate(sampleRate)
        .inputEncoding('signed')
        .inputBits(16)
        .inputChannels(1)
        .inputFileType('raw')
        .output(wavOutputFile)
        .outputSampleRate(RECOGNIZER_SAMPLE_RATE);
    
    command.on('error', function (err, stdout, stderr) {
        logger.error('Cannot process audio' + err.message);
        logger.debug('Sox Command Stdout', stdout);
        logger.debug('Sox Command Stderr', stderr)
        cb(err, null);
    });
    command.on('end', function (stdout, stderr) {
        logger.debug('Sox command ended successfully.');
        logger.debug('Sox Command Stdout', stdout);
        logger.debug('Sox Command Stderr', stderr);
        cb(null, wavOutputFile);
    });
    command.run();
};

module.exports = Recorder;