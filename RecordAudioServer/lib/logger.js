var winston = require('winston');
var path = require('path');

console.log('Setting up Winston logger');

var NODE_ENV = process.env.NODE_ENV;
var env = (/prod/.test(NODE_ENV)) ? 'prod' : 'dev';
console.log('Using environment:', env);

var dailyRotateDebugLogs = path.join(__dirname, '..', 'logs', env, 'nuttask.debug.log');
var dailyRotateExceptions = path.join(__dirname, '..', 'logs', env, 'nuttask.exceptions.log');
var dailyRotateAll = path.join(__dirname, '..', 'logs', env, 'nuttask.all.log');

var logger = new (winston.Logger)({
    transports: [
        new (winston.transports.Console)({
            level: 'debug',
            prettyPrint: true,
        }),
        new (winston.transports.DailyRotateFile)({
            name: 'debug-file',
            level: 'debug',
            filename: dailyRotateDebugLogs,
        }),
        new (winston.transports.DailyRotateFile)({
            name: 'all-file',
            level: 'verbose',
            filename: dailyRotateAll,
        }),
    ],
    exceptionHandlers: [
        new (winston.transports.Console)({
            prettyPrint: true,
        }),
        new (winston.transports.DailyRotateFile)({
            name: 'exception-file',
            filename: dailyRotateExceptions,
        }),
        new (winston.transports.DailyRotateFile)({ 
            name: 'all-file',
            filename: dailyRotateAll,
        }),
    ],
    exitOnError: false,
}); 

logger.info('Logging all to', dailyRotateAll);

module.exports = logger;