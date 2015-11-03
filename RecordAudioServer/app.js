var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var morgan = require('morgan');


var rr = require('ractive-render');
var layout = require('express-layout');

var routes = require('./routes/index');
var logger = require('./lib/logger');

var app = express();

// view engine setup
app.engine('html', rr.renderFile);
app.set('view engine', 'html');
app.set('views', path.join(__dirname, 'views'));
rr.config({autoloadPartials: true});
rr.clearCache();
// use the delimiters normally for static binding for server side rendering
rr.config({
    escapeDelimiters: ['{{', '}}'],
});
app.use(layout());
app.use(express.static(path.join(__dirname, 'public')));

app.use(morgan('dev'));

app.use('/', routes);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
    logger.error("404 Error");
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

// error handlers


if (app.get('env') === 'development') {
    logger.info('Configuring app for development:', app.get('env'));
    // development error handler
    // will print stacktrace
    app.use(function(err, req, res, next) {
        logger.error("Dev error", err);
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
    app.locals.pathPrefix = '';
    app.locals.production = false;
} else {
    // configure production settings
    logger.info('Configuring app for production:', app.get('env'));
    // production error handler
    // no stacktraces leaked to user
    app.use(function(err, req, res, next) {
        logger.error("Prod err", err);
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: {}
        });
    });

    app.enable('trust proxy');
    app.locals.pathPrefix = '/'; // 'nuttask/';
    app.locals.production = true;
}




module.exports = app;
