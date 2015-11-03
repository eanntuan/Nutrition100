var fs = require('fs');
var split = require('split');
var transform = require('csv').transform;
var combine = require('stream-combiner');
var _ = require('underscore');

var nutLogs = 'logs/all_nut_logs.txt';
var outputFile = 'logs/selected_nut_logs.txt';

var UNK = '<unk>';
var MAX_LENGTH = 10;


var numRecordsRead = 0;
var numRecordsKept = 0;
var transformReduce = function (record) {
    numRecordsRead++;
    var words = record.split(' ');
    var speakerId = words.shift();
    words.unshift(numRecordsRead); // add id as the line number from the big log file
    var containsUnk = _.contains(words, UNK);
    if (!containsUnk && words.length <= MAX_LENGTH) {
        numRecordsKept++;
        var sentence = words.join(' ') + '\n';
        return sentence;
    } else {
    }
};

var transformer = transform(transformReduce);

var readOptions = {
    encoding: 'utf8',
};
var inputStream = fs.createReadStream(nutLogs, readOptions);
var outputStream = fs.createWriteStream(outputFile);

inputStream
    .pipe(split())
    .pipe(transformer)
    .pipe(outputStream);
inputStream.on('end', function () {
    console.log('Finished reading in logs from', nutLogs);
    console.log('Read', numRecordsRead, 'records, but only kept', numRecordsKept);
});
