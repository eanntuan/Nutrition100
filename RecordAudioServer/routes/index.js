var express = require('express');
var _ = require('underscore');
var router = express.Router();
var logger = require('../lib/logger');
var uttDataGroups = require('../prepared_data/utteranceGroups.json');
var dataGroupIds = _.keys(uttDataGroups);
logger.info('Loaded data for groups:', dataGroupIds);

/* GET home page. */
/*
    The url query can include:
    dataIndex: the id of the sentence group to use
    assignmentId: the id of the assignment
    hitId: the id of the HIT
    turkSubmitTo: only defined in url if hit accepted, not in preview
*/
router.get('/', function(req, res, next) {
    logger.debug('Getting index page for query', req.query);
    logger.debug('Request referer:', req.headers.referer);

    var dataGroup = req.query.dataIndex || -1;
    var assignmentId = req.query.assignmentId;
    var hitId = req.query.hitId;
    var workerId = req.query.workerId;
    var turkSubmitTo = req.query.turkSubmitTo;

    if (!_.contains(dataGroupIds, dataGroup)) {
        logger.debug('Data group', dataGroup, 'not found');
        dataGroup = _.sample(dataGroupIds);
        logger.debug('Assigning new group id', dataGroup);
    }
    var data = uttDataGroups[dataGroup];
    
    
    data.pathPrefix = res.app.locals.pathPrefix;
    data.assignmentId = assignmentId;
    data.hitId = hitId;
    data.turkSubmitTo = turkSubmitTo;
    data.workerId = workerId;
    data.isPreview = isPreview(data.assignmentId);
    logger.info('Loaded data', data);
    data.acceptBrowser = true;

    res.render('index', data);
});

var isPreview = function (assignmentId) {
    var preview = true;
    if (assignmentId && assignmentId !== 'ASSIGNMENT_ID_NOT_AVAILABLE') {
        preview = false;
    }
    return preview;
};

module.exports = router;
