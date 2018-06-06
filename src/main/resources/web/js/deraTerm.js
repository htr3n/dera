jQuery.noConflict();

(function ($) {
    "use strict";

    var listenerCommands = {
        'add': [
            'Add a new element: event, actor, or application.',
            '',
            'General usage:',
            '  \tbadd\tb \tutype\tu arguments',
            '',
            '  \tu\titype\ti\tu can be: Event, Barrier, Condition, EventActor, or Application.',
            '',
            'For examples:',
            '  \tbadd\tb Event name [att1=value1, att2=value2, ...]',
            '  \tbadd\tb Barrier name [input={e1,e2}] [output={e3,e4}]',
            '  \tbadd\tb Condition name [input={e1,e2}] [when-true={e3}] [when-false={e4}]',
            '  \tbadd\tb EventActor name [input={e1,e2}] [output={e3,e4}]',
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
            'Show information of DERA elements: events, actors, or applications.',
            '',
            'Usage:',
            '  \tbls\tb \tuoptions\tu [\tuelement\tu]',
            '',
            '\tuoption\tu can be one of the following: ',
            '',
            '  \ti-e\ti \tt show a list of all events',
            '  \ti-a\ti \tt show a list of all actors',
            '  \ti-app\ti \tt show a list of all applications',
            '  \ti-e\ti \tuname\tu \tt show information of the corresponding event',
            '  \ti-a\ti \tuname\tu \tt show information of the corresponding actor',
            '  \ti-app\ti \tuname\tu \tt show information of the corresponding application'
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
    };

    var deraListener = {
        commands: listenerCommands,
        complete: completeHandler,
        execute: executeHandler

    };

    function sendCommand(cmdUrl, cmd, successCallback, failCallback) {
        $.ajax({
            async: false, /* otherwise, the terminal will proceed with invalid results */
            type: "POST",
            url: cmdUrl,
            contentType: 'application/json; charset=UTF-8',
            dataType: 'json',
            accepts: {json: 'application/json'},
            data: JSON.stringify(cmd)
        })
            .done(successCallback)
            .fail(failCallback);
    }

    function sendEventCommand(cmd, successCallback, failCallback) {
        sendCommand('domain/events/cmd', cmd, successCallback, failCallback)
    }

    function sendActorCommand(cmd, successCallback, failCallback) {
        sendCommand('domain/actors/cmd', cmd, successCallback, failCallback)
    }

    function sendApplicationCommand(cmd, successCallback, failCallback) {
        sendCommand('domain/applications/cmd', cmd, successCallback, failCallback)
    }

    function completeHandler(args) {
        switch (args[0]) {
            case 'add':
                if (args.length > 4) {
                    return;
                } else if (args.length > 3) {
                    switch (args[1]) {
                        case 'Application':
                            return $.terminal.among(args[3], ['start={e} end={e}']);
                        case 'Barrier':
                        case 'EventActor':
                            return $.terminal.among(args[3], ['input={e} output={e}']);
                        case 'Condition':
                            return $.terminal.among(args[3], ['input={e} when-true={e} when-false={e}']);
                        case 'Event':
                            return $.terminal.among(args[3], ['prop1 prop2 ...']);
                    }
                } else if (args.length > 2) {
                    return $.terminal.among(args[2], ['name']);
                } else if (args.length > 1) {
                    return $.terminal.among(args[1], VARS.elementTypes);
                }
                return;

            case 'update':
                if (args.length > 3) {
                    return;
                } else if (args.length > 2) {
                    var possibilities = [];
                    $.each(Object.keys(VARS.events), function (i, event) {
                        if (event)
                            possibilities.push(event);
                    });
                    $.each(VARS.actors, function (j, actor) {
                        if (actor) {
                            if (actor.type === 'Barrier'
                                || actor.type === 'EventActor'
                                || actor.type === 'Condition'
                                || actor.type === 'Trigger')
                                possibilities.push(actor.id);
                        }
                    });
                    $.each(VARS.applications, function (j, app) {
                        if (actor)
                            possibilities.push(app.id);
                    });
                    return $.terminal.among(args[2], possibilities);
                } else if (args.length > 1) {
                    return $.terminal.among(args[1], VARS.elementTypes);
                }
                return;
            case  'enable':
            case 'disable':
            case 'remove':
                if (args.length > 2) {
                    return;
                }
                return $.terminal.among(args[1], [ 'name' ]);
            case 'ls':
                if (args.length > 2) {
                    return;
                }
                return $.terminal.among(args[1], [ '-e', '-a', '-app' ]);
            case  'zoom':
                if (args.length > 2) {
                    return;
                }
                return $.terminal.among(args[1], [ 'in', 'out', 'none' ]);
        }
        return;
    }

    function executeList(args) {
        var list_actor = function () {
            var list = [];
            $.each(VARS.actors, function (j, actor) {
                if (actor) {
                    list.push(actor.id);
                }
            });
            return list;
        }
        var list_event = function () {
            var list = [];
            $.each(Object.keys(VARS.events), function (j, e) {
                if (e) {
                    list.push(e);
                }
            });
            return list;
        }
        var list_app = function () {
            var list = [];
            $.each(VARS.applications, function (j, a) {
                if (a) {
                    list.push(a);
                }
            });
            return list;
        }
        if (args.length > 1) {
            switch (args[1]) {
                case '-a':
                    return list_actor();
                case '-e':
                    return list_event();
                case '-app':
                    return list_app();
            }
        } else {
            return list_actor();
        }
        return [];
    }

    function executeAdd(args) {
        var result = [];

        if (args.length > 2) {

            var cmd = new DERA.Command('add');
            cmd.elementId = args[2];
            var cmdDoneHandler = function (data, status, jqxhr) {
                if (data.message) {
                    result.push(data.message);
                }
            };
            var cmdFailHandler = function (jqxhr, status, error) {
                console.log(this);
                if (jqxhr.status) {
                    result.push(this.url + " : " + jqxhr.status + ' -- ' + error);
                }
            };

            switch (args[1]) {
                case 'Event':
                    cmd["attributes"] = [];
                    for (var i = 3; i < args.length; i++) {
                        cmd["attributes"].push(args[i]);
                    }
                    sendEventCommand(cmd, cmdDoneHandler, cmdFailHandler);
                    return result;
                case 'Barrier':
                    cmd.elementType = args[1];
                    sendActorCommand(cmd, cmdDoneHandler, cmdFailHandler);
                    return result;
                case 'Condition':
                    cmd.elementType = args[1];
                    sendActorCommand(cmd, cmdDoneHandler, cmdFailHandler);
                    return result;
                case 'EventActor':
                    cmd.elementType = args[1];
                    sendActorCommand(cmd, cmdDoneHandler, cmdFailHandler);
                    return result;
            }
        } else {
            result.push("Not enough arguments!");
        }
        return result;
    }

    function executeUpdate(args) {
        var result = [];
        if (args.length > 2) {
            result.push("Updated '" + args[1] + "' ");
        } else {
            result.push('Not enough arguments!');
        }
        return result;
    }

    function executeEnable(args) {
        var result = [];
        if (args.length >= 2) {
            result.push('Enabled ' + args[1]);
        }
        else {
            result.push('Not enough arguments!');
        }
        return result;
    }

    function executeDisable(args) {
        var result = [];
        if (args.length >= 2) {
            result.push('Disabled ' + args[1]);
        }
        else {
            result.push('Not enough arguments!');
        }
        return result;
    }

    function executeRemove(args) {
        var result = [];
        if (args.length >= 2) {
            result.push('Removed ' + args[1]);
        }
        else {
            result.push('Not enough arguments!');
        }
        return result;
    }

    function executeZoom(args) {
        if (args[1]) {
            switch (args[1]) {
                case 'in':
                    VARS.deraGraph.zoomIn();
                    return [];
                case 'out':
                    VARS.deraGraph.zoomOut();
                    return [];
                case 'none':
                    VARS.deraGraph.zoomActual();
                    return [];
            }
        } else { // default zoom in
            VARS.deraGraph.zoomIn();
        }
        return [];
    }

    function executeRefresh() {
        VARS.deraGraph.refresh();
        if (VARS.graphLayout)
            VARS.graphLayout.execute(VARS.deraGraph.getDefaultParent());
        return [];
    }

    function executeHandler(args) {
        switch (args[0]) {
            case 'add':
                return executeAdd(args);
            case 'update':
                return executeUpdate(args);
            case 'ls':
                return executeList(args);
            case 'enable':
                return executeEnable(args);
            case 'disable':
                return executeDisable(args);
            case 'remove':
                return executeRemove(args);
            case 'zoom':
                return executeZoom(args);
            case 'refresh':
                return executeRefresh();
            case 'exit':
                VARS.hideTerminal();
                return [];
        }
    }

    function initTerm() {
        $(VARS.deraTerm).terminal({
            listeners: [deraListener],
            tab: 4
        });

        $(VARS.deraTerm).center().resizable().draggable();

        if ($(VARS.terminalOpened))
            VARS.showTerminal();
        else
            VARS.hideTerminal();

        // Close on Escape
        $(document).on('keydown', function (e) {
            if (e.keyCode === 27) { // ESC
                VARS.hideTerminal();
            }
        });
    }

    /* ===============================================================================================================*/
    initTerm();

})(jQuery);

