define(['jquery', 'ractive'], function($, Ractive) {

    var data = {
        meterMaxHeight: '35%',
        recording: false,
    };

    var element = $("#speech-mic");
    var template = element.html();
    console.log("Template", template);

    /* Initialize the Ractive component */
    var component = new Ractive({
        el: element,
        template: template,
        data: data,
    });

    return {
        data: data,
        component: component,
    };

});