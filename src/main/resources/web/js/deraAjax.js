jQuery.noConflict();

var DERA = DERA || {};

(function ($) {
    "use strict";

    DERA.resetDirtyActors = function () {
        var _uri = document.location.toString()
            + 'domain/actors/nodirty';
        $.get(_uri, function (data) {
        });
    }

    DERA.resetDirtyEvents = function () {
        var _uri = document.location.toString()
            + 'domain/events/nodirty';
        $.get(_uri, function (data) {
        });
    }

    DERA.resetDirtyApplications = function () {
        var _uri = document.location.toString()
            + 'domain/applications/nodirty';
        $.get(_uri, function (data) {
        });
    }

})(jQuery);