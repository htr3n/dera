jQuery.noConflict();

var MONITOR = MONITOR || {};

(function ($) {
    "use strict";

    function handleDirtyActors(elements) {
        // parse the received data
        $.each(elements, function (i, e) {
            info('... updating ' + e.type + '{id=' + e.id + '}');
        })
        // update the graph

        // reset the dirty flags
        DERA.resetDirtyActors();
        return false;
    }

    function handleDirtyApplications(elements) {
        // parse the received data
        $.each(elements, function (i, e) {
            info('... updating Application {' + e.id + '}');
        })
        // update the graph

        // reset the dirty flags
        DERA.resetDirtyApplications();
        return false;
    }

    function handleDirtyEvents(elements) {
        // parse the received data
        $.each(elements, function (i, e) {
            info('... updating Event{type=' + e.type + ', attributes=[' + e.attributes + ']}');
        })
        // update the graph

        // reset the dirty flags
        DERA.resetDirtyEvents();
        return false;
    }

    MONITOR._construct = function () {
        this.uri = document.location.toString()
            .replace('http://', 'ws://')
            .replace('https://', 'wss://') + 'monitor';

        this.ws = new WebSocket(this.uri, "htr3n.dera.monitor");

        this.ws.onopen = function (e) {
            info('The WS connection is opened!');
            return false;
        }

        this.ws.onmessage = function (e) {
            debug('Got a message from the monitoring WebSocket' + e);
            var parsedObj = $.parseJSON(e.data);
            if (parsedObj) {
                info('Receive updates of ' + parsedObj.data.length + ' ' + parsedObj.type + (parsedObj.data.length > 1 ? '(s)' : ''));
                if (parsedObj.type) {
                    switch (parsedObj.type) {
                        case 'Event':
                            if (parsedObj.data)
                                handleDirtyEvents(parsedObj.data);
                            return;
                        case 'EventActor':
                            if (parsedObj.data)
                                handleDirtyActors(parsedObj.data);
                            return;
                        case 'Application':
                            if (parsedObj.data)
                                handleDirtyApplications(parsedObj.data);
                            return;
                        default:
                            info('The updating type ' + parsedObj.type + ' is not supported in this version!');
                            return;
                    }
                }
            }
            return false;
        }

        this.ws.onclose = function (e) {
            warn('The monitoring WebSocket is closed');
            MONITOR.ws = null;
            return false;
        }

        this.ws.onerror = function (e) {
            error('Error: ' + e);
            console.log($.parseJSON(e.data));
            return false;
        }
    };

    if (!window.WebSocket)
        error("WebSocket not supported by this browser");
    else {
        MONITOR._construct();
    }

})(jQuery);