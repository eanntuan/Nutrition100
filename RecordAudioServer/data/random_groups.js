var fs = require('fs');
var split = require('split');
var through = require('through');
var _ = require('underscore');
var util = require('util');

var logFile = 'logs/Flickr8k_numbered_shuffled_recollect_noquotes.txt';
var outputGroupFormat = 'logs/hitGroups/group_%d.txt';

var GROUP_SIZE = 10;
var groupNumber

var gen_transformReduce = function () {
    var groupNumber = 1;
    var sentenceGroup = [];

    var transformReduce = through(
        function write (line) {
            sentenceGroup.push(line);
            if (sentenceGroup.length === GROUP_SIZE) {
                var groupFile = util.format(outputGroupFormat, groupNumber);
                console.log('Creating group at', groupFile);
                var contents = sentenceGroup.join('\n');
                fs.writeFile(groupFile, contents);
                groupNumber++;
                sentenceGroup = [];
            }
        },
        function end () {
            if (sentenceGroup.length > 0) {
                console.log('Sentences could not be evenly divided into groups of', GROUP_SIZE);
                console.log(sentenceGroup.length, 'sentences omitted');
            }
        }
    );

    return transformReduce;
};

var readOptions = {
    encoding: 'utf8',
};
var inputStream = fs.createReadStream(logFile, readOptions);

inputStream
    .pipe(split())
    .pipe(gen_transformReduce());

inputStream.on('end', function () {
    console.log('Finished reading in logs from', logFile);
});
