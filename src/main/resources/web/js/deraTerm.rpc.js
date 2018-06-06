jQuery.noConflict();
/* https://raw.github.com/datagraph/jquery-jsonrpc/master/jquery.jsonrpc.js */
(function ($) {
    "use strict";
    var rpcListener =
        $.terminal.listeners.jsonrpc(
            'rpc',
            {
                'add': [
                    'Add a new element: event, actor, or application.',
                    '',
                    'General usage:',
                    '  \tbadd\tb \tutype\tu arguments',
                    '  \tutype\tu can be: Event, Barrier, Condition, EventActor, or Application.',
                    '',
                    'For examples:',
                    '  \tbadd\tb Event name [prop=value, prop=value, ...]',
                    '  \tbadd\tb Barrier name [input={e1,e2}] [output={e3,e4}]',
                    '  \tbadd\tb Condition name [input={e1,e2}] [when-true={e3}] [when-false={e4}]',
                    '  \tbadd\tb EventActor name name [input={e1,e2}] [output={e3,e4}]',
                    '  \tbadd\tb Application name [start={e1,e2}] [end={e3,e4}]',
                    '',
                    'The parts inside the square brackets are optional.'
                ],
                'update': [
                    'Update an existing element: event, actor, or application.',
                    '',
                    'General usage:',
                    '  \tbupdate\tb \tutype\tu arguments',
                    '  \tutype\tu can be: Event, Barrier, Condition, EventActor, or Application.',
                    '',
                    'For examples:',
                    '  \tbupdate\tb Event name [prop=value, prop=value, ...]',
                    '  \tbupdate\tb Barrier name [input={e1,e2}] [output={e3,e4}]',
                    '  \tbupdate\tb Condition name [input={e1,e2}] [when-true={e3}] [when-false={e4}]',
                    '  \tbupdate\tb EventActor name [input={e1,e2}] [output={e3,e4}]',
                    '  \tbupdate\tb Application name [start={e1,e2}] [end={e3,e4}]',
                    '',
                    'The parts inside the square brackets are optional.'
                ],
                'ls': [
                    'Lists existing elements: events, actors, or applications.',
                    '',
                    'Usage:',
                    '  \tbls\tb \tuoptions\tu',
                    '\tuoptions\tu can be: ',
                    '  \ti-e\ti -- list all events ',
                    '  \ti-a\ti -- list all actors',
                    '  \ti-app\ti -- list all applications',
                    'The default case is \'-a\', i.e., list all actors, when no option is given.'
                ],
                'enable': [
                    'Enable a DERA actor or application.',
                    '',
                    'Usage: \tbenable\tb \tuname\tu',
                    ''
                ],
                'disable': [
                    'Disable a DERA actor or application.',
                    '',
                    'Usage: \tbdisable\tb \tuname\tu',
                    ''
                ],
                'remove': [
                    'Remove a DERA element: event, actor, or application.',
                    '',
                    'Usage: \tbremove\tb \tuname\tu',
                    ''
                ],
                'zoom': ['Zooms in, out, or reset zooming of the DEREA system visualization.'],
                'refresh': ['Refresh the DEREA system visualization.'],
                'exit': ['Hide the console.']
            });
    function initTerm() {
        $(VARS.deraTerm).terminal({
            listeners: [rpcListener],
            tab: 4
        });
    }
    initTerm();
})(jQuery);

