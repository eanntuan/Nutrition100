var fs = require('fs');
var util = require('util');
var _ = require('underscore');

var DATA_DIR = 'logs/hitGroups/';
var DATA_FILE_FORMAT = DATA_DIR + '%s';
var DATA_GROUP_ID_PATTERN = /group_(\d+).txt/;

var DATA_READ_OPTIONS = {
    encoding: 'utf8',
};

var exportAllToJSON = function (exportFile) {
    var dataFiles = fs.readdirSync(DATA_DIR);
    console.log('Preparing data from', DATA_DIR);
    console.log(dataFiles);
    console.log('------------');
    var groups = {};
    // var groupIds = [];
    _.forEach(dataFiles, function iteratee (filename, index, list) {
        var groupId = filename.match(DATA_GROUP_ID_PATTERN)[1];
        // groupIds.push(groupId);
        filename = util.format(DATA_FILE_FORMAT, filename);
        console.log('Preparing data group id', groupId, 'from file', filename);
        var groupData = fs.readFileSync(filename, DATA_READ_OPTIONS);
        var groupDataObj = extractGroupData(groupId, groupData);
        groups[groupId] = groupDataObj;
    });

    console.log('\n\nPrepared groups object:', JSON.stringify(groups));
    console.log('------------');
    console.log('Exporting data to', exportFile);
    fs.writeFileSync(exportFile, JSON.stringify(groups));
    console.log('Export complete.');
};

var extractGroupData = function (groupId, groupData) {
    var lines = groupData.split('\n');
    var utterancesList = [];
    _.each(lines, function iteratee (line, index, list) {
        var words = line.split(' ');
        // split the line up by spaces, and the first item is the utterance id
        // take the id out of the list, and join the rest back together into the utterance
        var id = words.shift();
        var text = words.join(' ');
        var utteranceData = createUtterance(text, id);
        utterancesList.push(utteranceData);
    });
    return {
        groupId: groupId,
        utterances: utterancesList,
    };
};

var createUtterance = function (text, id) {
    return {
        uttId: id,
        uttText: text,
    };
}; 

exportAllToJSON('prepared_data/utteranceGroups.json');

/* JSON structure of groups:
var groups = {
    1: {
        groupId: 1,
        utterances: [
            {
                uttId: 104,
                uttText: 'I had a handful of trail mix.',
            },
            {
                uttId: 73,
                uttText: 'I had waffles.',
            },
        ],
    },

    3: {...},

    15: {...},

    minGroupId: 1,
    maxGroupId: 15,
}
*/