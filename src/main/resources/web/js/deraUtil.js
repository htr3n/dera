function formatTimestamp(d) {
    function pad(n) {
        return n < 10 ? '0' + n : n
    }

    return d.getFullYear() + '-'
        + pad(d.getMonth() + 1) + '-'
        + pad(d.getDate()) + ' '
        + pad(d.getHours()) + ':'
        + pad(d.getMinutes());
}

function formatLog(type, msg, color) {
    var s = '';
    if (color)
        s += '<span style=\'color:' + color + ';\'>';
    else
        s += '<span>';
    s += type;
    if (VARS.LOG_TIMESTAMP)
        s += ' - ' + formatTimestamp(new Date());
    s += ' - ' + msg;
    s += '</span><br>';
    return s;
}

function debug(msg) {
    "use strict";
    if (VARS.DEBUG) {
        jQuery(VARS.logContent).append(formatLog('D', msg));
    }
    return false;
}

function info(msg) {
    "use strict";
    jQuery(VARS.logContent).append(formatLog('I', msg));
    return false;
}

function warn(msg) {
    "use strict";
    jQuery(VARS.logContent).append(formatLog('W', msg, '#a52a2a'));
    return false;
}

function error(msg) {
    "use strict";
    jQuery(VARS.logContent).append(formatLog('E', msg, '#ff0000'));
    return false;
}

function addToMap(key, value, map) {
    if (!map[key]) { // not yet, create a new list
        map[key] = [];
        map[key].push(value);
    } else { // existed, just add to the list
        map[key].push(value);
    }
    return false;
}

function addToSet(obj, set) {
    set[obj] = true;
    return false;
}

function stringifyActor(actor) {
    if (actor) {
        var result = actor.type + '[' + actor.id + ']';
        if (actor.input)
            result += ' input={' + actor.input + '}';
        if (actor.type === "Condition") {
            if (actor.trueEvents)
                result += ' when-true={' + actor.trueEvent + '}';
            if (actor.falseEvents)
                result += ' when-false={' + actor.falseEvent + '}';
        } else {
            if (actor.output)
                result += ' output={' + actor.output + '}';
        }
        return result;
    }
    return '';
}
