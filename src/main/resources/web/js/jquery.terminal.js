/**
 * @file jquery.terminal.js
 * @brief Embedded terminal
 * @author Jonathan Giroux (Bloutiouf)
 * @site https://github.com/Bloutiouf/jquery.terminal
 * @version 2.5
 * @license MIT license <http://www.opensource.org/licenses/MIT>
 *
 * jquery.terminal is a jQuery plugin which displays an interactive terminal in
 * your pages.
 *
 * Features:
 * <ul>
 * <li>customizable</li>
 * <li>command pipelining and other operators</li>
 * <li>context-aware completion</li>
 * <li>handles asynchronous requests</li>
 * <li>interfaceable with other jQuery plugins</li>
 * </ul>
 */

(function ($) {

    // key for .data(), change it if it conflicts
    var dataName = 'terminal';

    function among(command, possibilities) {
        return $.map(possibilities, function (value) {
            if (value.substr(0, command.length) == command) {
                return value;
            }
        });
    };

    function escape(str) {
        return new String(str).replace('\\', '\\\\').replace(' ', '\\ ');
    };

    var listeners = {
        command: function (name, help, execute, complete) {
            var commands = {};
            commands[name] = help;

            return {
                commands: commands,
                complete: function (args) {
                    if (args[0] == name && complete) {
                        return complete(args);
                    }
                },
                execute: function (args) {
                    if (args[0] == name) {
                        return execute(args);
                    }
                }
            };
        },
        firebug: {
            commands: {},
            complete: function (args) {
                console.log('complete:', args);
            },
            execute: function (args) {
                console.log('execute:', args);
            }
        }
    };

    $.terminal = {
        among: among,
        escape: escape,
        listeners: listeners
    };

    var defaults = {
        history: 20,
        popup: 1000,
        prompt: '> ',
        tab: 4,
        listeners: [],
        variables: {}
    };

    var htmlEntities = {
        ' ': '&nbsp;',
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;'
    };

    var operators = [ ';;', ';', '~', '||', '&&', '|', ':', '&' ];

    var states = {
        beginning: 0,
        command: 1,
        double: 2,
        single: 3,
        operator: 4,
        space: 5,
        argument: 6
    };

    // add quote around arguments if they are not simple
    var notSimpleRegExp = /[^a-zA-Z0-9_]/;

    // allowed characters with eval and test
    var evalRegExp = /^[0-9+\-*\/%(). =!<>]*$/;

    function strip(arg) {
        return arg && arg.replace(/\t(a|b|c|i|m|u|x)(?:.*?\t\-)?(.*?)\t\1/g,function (match, $1, $2) {
            return strip($2);
        }).replace(/\t(t|(p|h).*?\t\2)/g, ' ');
    }

    // from https://gist.github.com/82181
    if (!$.getJSONP) {
        $.getJSONP = function (options) {
            options.dataType = 'jsonp';
            $.ajax(options);

            var script = $(document.getElementsByTagName('head')[0].firstChild);
            script[0].onerror = function (error) {
                script.remove();
                if (options.error) {
                    options.error({}, 'error', error);
                }
            };
        };
    }

    $.fn.terminal = function (globalOptions) {

        if (typeof globalOptions == 'string') {
            var args = $.makeArray(arguments);

            switch (globalOptions) {
                case 'execute':
                    return this.each(function () {
                        var input = $(this).children('input');
                        input.val(args[1]);
                        input.keydown();
                    });

                case 'addListeners':
                    var newListeners = (Object.prototype.toString.call(args[1]) === '[object Array]') ? args[1] : args.slice(1);
                    return this.each(function () {
                        var terminal = $(this);
                        var options = terminal.data(dataName);
                        options.listeners = newListeners.concat(options.listeners);
                        terminal.data(dataName, options);
                    });
                    break;

                case 'removeListeners':
                    var oldListeners = (Object.prototype.toString.call(args[1]) === '[object Array]') ? args[1] : args.slice(1);
                    return this.each(function () {
                        var terminal = $(this);
                        var options = terminal.data(dataName);
                        for (var listener in oldListeners) {
                            for (var i in options.listeners) {
                                if (oldListeners[listeners] === options.listeners[i]) {
                                    delete options.listeners[i];
                                    break;
                                }
                            }
                        }
                        terminal.data(dataName, options);
                    });
                    break;

                case 'options':
                    if (args[1] !== undefined) {
                        return this.data(dataName, args[1]);
                    }
                    return this.data(dataName);

                default:
                    throw 'Unknown action.';
            }
        }

        globalOptions = $.extend(defaults, globalOptions);

        return this.each(function () {

            var terminal = $(this);

            var options = {};

            var character;
            var size;

            var content;
            var bar;
            var input;
            var popup;

            var popupTimeout;

            var history = [];
            var currentHistory;
            var currentCommand;

            var executionTree;
            var asyncResult;

            var cookieName = (options.cookie != null) ? options.cookie : terminal.attr('id');

            if ($.cookie) {
                var cookie = $.cookie(cookieName);
                if (cookie) {
                    cookie = JSON.parse(cookie);
                    history = cookie.history;
                    options.variables = cookie.variables;
                }
            }

            var defaultListener = {
                commands: {
                    'help': [ 'Show help.', '', 'Usage: \tbhelp\tb [\tucommand\tu]', '', 'If \tucommand\tu is not given,shows the available commands.', '', 'If \tucommand\tu is given,shows help of this command.' ],
                    'clear': [ 'Clear medium.', '', 'Usage: \tbclear\tb [\tumedium\tu ...]', '', '\tumedium\tu can be', ' \tia\ti, \tiall\ti: all media listed below', ' \tih\ti, \tihistory\ti: command history', ' \tis\ti, \tiscreen\ti: previous commands and their output', '', 'Several mediums may be used. Default is \tiscreen\ti.' ]
                    /*
                     'alert': [ 'Pops up an alert box.', '', 'Usage: \tbalert\tb [\tumessage\tu]' ],
                     'clear': [ 'Clear medium.', '', 'Usage: \tbclear\tb [\tumedium\tu ...]', '', '\tumedium\tu can be', ' \tia\ti, \tiall\ti: all media listed below', ' \tih\ti, \tihistory\ti: command history', ' \tis\ti, \tiscreen\ti: previous commands and their output', ' \tiv\ti, \tivariables\ti: variables set by \tb set\tb ', '', 'Several mediums may be used. Default is \tiscreen\ti.' ],
                     'confirm': [ 'Pops up a confirm box.', '', 'Usage: \tbconfirm\tb [\tumessage\tu]' ],
                     'count': [ 'Counts arguments.', '', 'Usage: \tbcount\tb ...' ],
                     'echo': [ 'Displays arguments.', '', 'Usage: \tbecho\tb \tuargument\tu ...' ],
                     'eval': [ 'Evaluates an expression.', '', 'Usage: \tbeval\tb \tuexpression\tu' ],
                     'exec': [ 'Executes a command.', '', 'Usage: \tbexec\tb \tucommand\tu [\tuargument\tu ...]' ],
                     'filter': [ 'Filters arguments.', '', 'Usage: \tbfilter\tb \tupattern\tu [\tuargument\tu ...]', '', 'Keeps only the \tuargument\tus matching with \tupattern\tu.', '', 'Uses the Javascript RegExp rules.' ],
                     'foreach': [ 'Iterates over arguments.', '', 'Usage: \tbforeach\tb \tuvariable\tu', '', 'Iterates the following command over arguments. In the next command,the current iterated argument is available through \tuvariable\tu.', '', 'Example: \txseq 42 | foreach i : eval $i * 2\t-seq 42 | foreach i : eval $i * 2\tx' ],
                     'get': [ 'Gets the value of a variable.', '', 'Usage: \tbget\tb \tuname\tu' ],
                     'help': [ 'Show help.', '', 'Usage: \tbhelp\tb [\tucommand\tu]', '', 'If \tucommand\tu is not given,shows the available commands.', '', 'If \tucommand\tu is given,shows help of this command.' ],
                     'join': [ 'Joins arguments.', '', 'Usage: \tbjoin\tb \tuglue\tu \tuargument\tu ...', '', '\tuglue\tu is inserted between each \tuargument\tu,but not at the beginning or the end.' ],
                     'length': [ 'Gives length of arguments.', '', 'Usage: \tblength\tb \tuargument\tu ...' ],
                     'list': [ 'Lists arguments.', '', 'Usage: \tblist\tb \tuargument\tu ...' ],
                     'prompt': [ 'Pops up a prompt box.', '', 'Usage: \tbprompt\tb [\tumessage\tu [\tudefault\tu]]' ],
                     'replace': [ 'Replaces parts of arguments.', '', 'Usage: \tbreplace\tb \tupattern\tu \tureplacement\tu \tuargument\tu ...', '', 'Uses the Javascript RegExp rules.' ],
                     'reverse': [ 'Reverses the order of the arguments.', '', 'Usage: \tbreverse\tb \tuargument\tu ...' ],
                     'select': [ 'Select an argument.', '', 'Usage: \tbselect\tb \tuindex\tu \tuargument\tu ...', '', 'Gives only the \tuindex\tu-th \tuargument\tu (starting from \ti1\ti).' ],
                     'seq': [ 'Gives a sequence of number.', '', 'Usage: \tbseq\tb [\tufirst\tu [\tuincrement\tu]] \tulast\tu', '', 'Gives a sequence of number from \tufirst\tu (default \ti1\ti) to \tulast\tu in steps of \tuincrement\tu (default \ti1\ti).' ],
                     'set': [ 'Sets a variable.', '', 'Usage: \tbset\tb \tuname\tu \tuvalue\tu [...]', '', 'Sets the variable \tuname\tu to \tuvalue\tu (if there are more than one value,they are kept splited).', '', 'Variables may be used later by invoking \tb$\tuname\tu\tb or calling \tbget\tb.' ],
                     'slice': [ 'Select a part of the arguments.', '', 'Usage: \tbselect\tb \tustart\tu \tuend\tu \tuargument\tu ...', '', 'Selects a part of the \tuargument\tus in range [\tustart\tu,\tuend\tu[ (starting from \ti1\ti). \tuend\tu may be set to \ti0\ti to select arguments from \tustart\tu to the end.' ],
                     'sort': [ 'Sorts arguments alphabetically and ascending.', '', 'Usage: \tbsort\tb \tuargument\tu [...]' ],
                     'split': [ 'Splits arguments.', '', 'Usage: \tbsplit\tb \tuseparator\tu \tuargument\tu [...]' ],
                     'sum': [ 'Sums arguments.', '', 'Usage: \tbsum\tb \tuargument\tu ...' ],
                     'test': [ 'Tests an expression.', '', 'Usage: \tbtest\tb \tuexpression\tu', '', 'Example: \txtest 1 + 1 == 2 && echo ok || echo problem!\t-test 1 + 1 == 2 && echo ok || echo problem!\tx' ],
                     'unset': [ 'Removes a variable.', '', 'Usage: \tbunset\tb \tuname\tu' ]
                     */
                },

                complete: function (args) {
                    switch (args[0]) {

                        case 'clear':
                            return among(args[args.length - 1], [ 'all', 'history', 'screen']);
                        case 'help':
                            if (args.length > 2) {
                                return;
                            }
                            var possibilities = [];
                            $.each(options.listeners, function (i, listener) {
                                for (var command in listener.commands) {
                                    if ($.inArray(command, possibilities) < 0) {
                                        possibilities.push(command);
                                    }
                                }
                                ;
                            });
                            return among(args[1], possibilities);
                    }
                },

                execute: function (args) {
                    switch (args[0]) {

                        case 'clear':
                            var clearScreen = function () {
                                content.empty();
                            };
                            var clearHistory = function () {
                                history = [];
                            };
                            var clearVariables = function () {
                                options.variables = {};
                            };
                            if (args.length == 1) {
                                clearScreen();
                            } else {
                                for (var i = 1; i < args.length; ++i) {
                                    switch (args[i]) {
                                        case 'a':
                                        case 'all':
                                            clearHistory();
                                            clearScreen();
                                            clearVariables();
                                            break;

                                        case 'h':
                                        case 'history':
                                            clearHistory();
                                            break;

                                        case 's':
                                        case 'screen':
                                            clearScreen();
                                            break;
                                    }
                                }
                            }
                            return [];
                        case 'help':
                            if (args.length > 1) {
                                var ret;
                                $.each(options.listeners, function (i, listener) {
                                    if (listener.commands[args[1]]) {
                                        ret = listener.commands[args[1]];
                                    }
                                });
                                if (ret) {
                                    return ret;
                                }
                                throw 'Unknown command \tb' + args[1] + '\tb.';
                            } else {
                                var ret = [];
                                var max = 0;
                                $.each(options.listeners, function (i, listener) {
                                    for (var command in listener.commands) {
                                        if (command.length > max) {
                                            max = command.length;
                                        }
                                    }
                                });
                                var separator = '\tx\tp' + (max + 2) + '\tp';
                                $.each(options.listeners, function (i, listener) {
                                    $.merge(ret, $.map(listener.commands, function (value, key) {
                                        return '\txhelp ' + key + '\t-' + key + separator + value[0];
                                    }));
                                });
                                ret.sort();
                                return [ 'Type \tbhelp \tucommand\tu\tb to show information about \tucommand\tu.\n\nAvailable commands:' ].concat(ret);
                            }
                    }
                }
            };

            $.extend(options, globalOptions);

            $.merge(options.listeners, [ defaultListener ]);

            function loadOptions() {
                options = terminal.data(dataName);
            }

            function saveOptions() {
                terminal.data(dataName, options);
            }

            saveOptions();

            // transform a command line to an execution tree
            function parse(command) {

                // split commands
                function firstPass(node, i) {
                    var state = states.beginning;
                    var newCommand;
                    var start;

                    outerwhile: while (i < command.length) {
                        var c = command.charAt(i);

                        switch (state) {
                            case states.beginning:
                                if (c != ' ') {
                                    if ($.inArray(command.substr(i, 2), operators) >= 0) {
                                        throw 'command expected before operator.';
                                    } else if ($.inArray(c, operators) >= 0) {
                                        throw 'command expected before operator.';
                                    } else if (c == '(') {
                                        var subnode = [];
                                        i = firstPass(subnode, i + 1);
                                        node.push(subnode);
                                        state = states.operator;
                                    } else if (c == '"') {
                                        state = states.double;
                                        newCommand = c;
                                    } else if (c == "'") {
                                        state = states.single;
                                        newCommand = c;
                                    } else if (c == ')') {
                                        throw 'unexpected closing parenthesis.';
                                    } else if (c == '\\') {
                                        ++i;
                                        if (i >= command.length) {
                                            throw 'character expected after \\.';
                                        }
                                        c = command.charAt(i);
                                        newCommand = '\\' + c;
                                        state = states.command;
                                        start = i;
                                    } else {
                                        newCommand = c;
                                        state = states.command;
                                        start = i;
                                    }
                                }
                                break;

                            case states.command:
                                if ($.inArray(command.substr(i, 2), operators) >= 0) {
                                    node.push({
                                        command: newCommand,
                                        start: start,
                                        end: i
                                    });
                                    node.push({
                                        operator: command.substr(i, 2)
                                    });
                                    newCommand = null;
                                    start = null;
                                    state = states.beginning;
                                    ++i;
                                } else if ($.inArray(c, operators) >= 0) {
                                    node.push({
                                        command: newCommand,
                                        start: start,
                                        end: i
                                    });
                                    node.push({
                                        operator: c
                                    });
                                    newCommand = null;
                                    start = null;
                                    state = states.beginning;
                                } else if (c == '(') {
                                    throw 'unexpected opening parenthesis.';
                                } else if (c == '"') {
                                    state = states.double;
                                    newCommand += c;
                                } else if (c == "'") {
                                    state = states.single;
                                    newCommand += c;
                                } else if (c == ')') {
                                    break outerwhile;
                                } else if (c == '\\') {
                                    ++i;
                                    if (i >= command.length) {
                                        throw 'character expected after \\.';
                                    }
                                    c = command.charAt(i);
                                    newCommand += '\\' + c;
                                } else {
                                    newCommand += c;
                                }
                                break;

                            case states.double:
                                if (c == '"') {
                                    newCommand += c;
                                    state = states.command;
                                } else if (c == '\\') {
                                    ++i;
                                    if (i >= command.length) {
                                        throw 'character expected after \\.';
                                    }
                                    c = command.charAt(i);
                                    newCommand += '\\' + c;
                                } else {
                                    newCommand += c;
                                }
                                break;

                            case states.single:
                                if (c == "'") {
                                    newCommand += c;
                                    state = states.command;
                                } else {
                                    newCommand += c;
                                }
                                break;

                            case states.operator:
                                if ($.inArray(command.substr(i, 2), operators) >= 0) {
                                    node.push({
                                        operator: command.substr(i, 2)
                                    });
                                    newCommand = null;
                                    start = null;
                                    state = states.beginning;
                                    ++i;
                                } else if ($.inArray(c, operators) >= 0) {
                                    node.push({
                                        operator: c
                                    });
                                    newCommand = null;
                                    start = null;
                                    state = states.beginning;
                                } else if (c == ')') {
                                    break outerwhile;
                                } else if (c != ' ') {
                                    throw 'unexpected character after parenthesis.';
                                }
                                break;
                        }

                        ++i;
                    }

                    switch (state) {
                        case states.double:
                        case states.single:
                            throw 'quote expected.';
                    }

                    if (newCommand != null) {
                        node.push({
                            command: newCommand,
                            start: start,
                            end: i
                        });
                    }

                    if (node.length > 0 && node[node.length - 1].operator != null) {
                        throw 'command expected after operator.';
                    }

                    return i;
                }

                // build a tree
                function secondPass(nodeIn, nodeOut) {
                    if (nodeIn.length == 1) {
                        if (nodeIn[0].length) {
                            return secondPass(nodeIn[0], nodeOut);
                        }
                        nodeOut.command = nodeIn[0].command;
                        nodeOut.start = nodeIn[0].start;
                        nodeOut.end = nodeIn[0].end;
                    }

                    $.each(operators, function (priority, operator) {
                        var index;

                        $.each(nodeIn, function (i, subnode) {
                            if (subnode.operator == operator) {
                                index = i;
                                return false;
                            }
                        });

                        if (index) {
                            nodeOut.operator = nodeIn[index].operator;

                            var first = [ nodeIn.slice(0, index) ];
                            nodeOut.first = {};
                            secondPass(first, nodeOut.first);

                            var second = [ nodeIn.slice(index + 1) ];
                            nodeOut.second = {};
                            secondPass(second, nodeOut.second);

                            return false;
                        }
                    });
                }

                var firstTree = [];

                firstPass(firstTree, 0);

                var secondTree = {};

                secondPass(firstTree, secondTree);

                return secondTree;
            }

            function showPopup(text) {
                if (options.popup) {
                    if (popupTimeout) {
                        clearTimeout(popupTimeout);
                    }

                    popup.text(text);
                    popup.css('left', ((terminal.width() - popup.width()) / 2) + 'px');
                    popup.css('top', ((terminal.height() - popup.height()) / 2) + 'px');
                    popup.show();

                    popupTimeout = setTimeout(function () {
                        popup.hide();
                        popupTimeout = null;
                    }, options.popup);
                }
            }

            // parse magic sequences
            function toHtml(s) {
                var r = [ '<span>' ];
                var bold = false, colored = false, execute = false, italic = false, link = false, underlined = false;

                for (var i = 0, l = 0, o = 0, n = s.length; i < n; ++i) {
                    var c = s.charAt(i);
                    switch (c) {
                        case '\t':
                            c = s.charAt(++i);
                            switch (c) {
                                case 'a':
                                    if (link)
                                        r[++o] = '</a>';
                                    else {
                                        ++i;
                                        var space = s.indexOf('\t-', i);
                                        if (space < 0) {
                                            space = s.length;
                                        }
                                        r[++o] = '<a href="' + s.substring(i, space) + '">';
                                        i = space + 1;
                                    }
                                    link = !link;
                                    break;

                                case 'b':
                                    if (bold)
                                        r[++o] = '</span>';
                                    else
                                        r[++o] = '<span style="font-weight: bold">';
                                    bold = !bold;
                                    break;

                                case 'c':
                                    if (colored)
                                        r[++o] = '</span>';
                                    else {
                                        ++i;
                                        var space = s.indexOf('\t-', i);
                                        if (space < 0) {
                                            space = s.length;
                                        }
                                        r[++o] = '<span style="color:' + s.substring(i, space) + '">';
                                        i = space + 1;
                                    }
                                    colored = !colored;
                                    break;

                                case 'h':
                                    ++i;
                                    var space = s.indexOf('\th', i);
                                    if (space < 0) {
                                        space = s.length;
                                    }
                                    r[++o] = s.substring(i, space);
                                    i = space + 1;
                                    break;

                                case 'i':
                                    if (italic)
                                        r[++o] = '</span>';
                                    else
                                        r[++o] = '<span style="font-style: italic">';
                                    italic = !italic;
                                    break;

                                case 'm':
                                    ++i;
                                    var space = s.indexOf('\tm', i);
                                    if (space < 0) {
                                        space = s.length;
                                    }
                                    r[++o] = '<img src="' + s.substring(i, space) + '"/>';
                                    i = space + 1;
                                    break;

                                case 'p':
                                    ++i;
                                    var space = s.indexOf('\tp', i);
                                    if (space < 0) {
                                        space = s.length;
                                    }
                                    var pos = parseInt(s.substring(i, space)) - l;
                                    if (pos > 0) {
                                        r[++o] = new Array(pos + 1).join('&nbsp;');
                                    }
                                    l += pos;
                                    i = space + 1;
                                    break;

                                case 't':
                                    var d = options.tab - (l % options.tab);
                                    if (d < options.tab) {
                                        r[++o] = new Array(d + 1).join('&nbsp;');
                                        l += d;
                                    }
                                    break;

                                case 'u':
                                    if (underlined)
                                        r[++o] = '</span>';
                                    else
                                        r[++o] = '<span style="text-decoration: underline">';
                                    underlined = !underlined;
                                    break;

                                case 'x':
                                    if (execute)
                                        r[++o] = '</a>';
                                    else {
                                        ++i;
                                        var space = s.indexOf('\t-', i);
                                        if (space < 0) {
                                            space = s.length;
                                        }
                                        r[++o] = '<a href="#' + s.substring(i, space) + '">';
                                        i = space + 1;
                                    }
                                    execute = !execute;
                                    break;

                                default:
                                    throw 'Unexpected magic sequence ' + c + '.';
                            }
                            break;

                        case '\n':
                            r[++o] = '<br/>';
                            l = 0;
                            break;

                        default:
                            if (l >= size.columns) {
                                r[++o] = '<br/>';
                                l = 0;
                            }
                            r[++o] = htmlEntities[c] || c;
                            ++l;
                            break;
                    }
                }

                if (bold)
                    r[++o] = '</span>';

                if (colored)
                    r[++o] = '</span>';

                if (execute)
                    r[++o] = '</a>';

                if (italic)
                    r[++o] = '</span>';

                if (link)
                    r[++o] = '</a>';

                if (underlined)
                    r[++o] = '</span>';

                r[++o] = '</span>';
                return r.join('');
            }

            function attachHandlers(html) {
                html.find('img').load(updateInput);
                html.find('a[href^="#"]').click(function () {
                    execute($(this).attr('href').substr(1));
                    return false;
                });
            }

            // resize the content, to be called when terminal is resized
            function resize() {
                size = {
                    columns: Math.floor(terminal.width() / character.width) - 1,
                    rows: Math.floor(terminal.height() / character.height)
                };

                content.width(terminal.width() - character.width);
                content.height(terminal.height());

                if (input) {
                    input.width(content.width() - input.position().left);
                }

                updateScrollbar();

                setTimeout(function () {
                    showPopup('Size: ' + size.columns + 'x' + size.rows);
                }, 0);
            }

            function updateScrollbar() {
                var scrollHeight = content[0].scrollHeight;
                if (scrollHeight > content.height()) {
                    bar.show();
                    var h = terminal.height() - 2 * character.height;
                    var height = (h * content.height() / scrollHeight);
                    if (height < character.height) {
                        height = character.height;
                    }
                    bar.css('height', height + 'px');
                    h -= height - 1;
                    bar.css('top', (character.height + content.scrollTop() * h / (scrollHeight - content.height())) + 'px');
                } else {
                    bar.hide();
                }
            }

            function focusInput() {
                content.scrollTop(content[0].scrollHeight);

                input.css('top', content.children(":last").position().top);
                input.focus();

                updateScrollbar();
            }

            function updateInput() {
                var last = content.children(":last");
                var offset = last.position();
                var top = offset.top;
                var left = offset.left + last.width() - 1;

                input.css({
                    top: top,
                    left: left
                }).width(content.width() - left - 1);
            }

            function showInput(val) {
                var html = $(toHtml(options.prompt));
                attachHandlers(html);
                content.append(html);
                content.scrollTop(content[0].scrollHeight);

                terminal.append(input);

                updateInput();

                input.focus();
                input.select();
                input.val(val);

                updateScrollbar();
            }

            // split a command string into an array of arguments
            function split(command) {
                // expand variables
                var newCommand;
                var state = states.beginning;
                for (var i = 0; i < command.length; ++i) {
                    var c = command.charAt(i);

                    switch (state) {
                        case states.beginning:
                            if (c == '"') {
                                state = states.double;
                                newCommand = c;
                            } else if (c == "'") {
                                state = states.single;
                                newCommand = c;
                            } else if (c != ' ') {
                                newCommand = '';
                                state = states.command;
                                --i;
                            }
                            break;

                        case states.command:
                            if (c == '"') {
                                state = states.double;
                                newCommand += c;
                            } else if (c == "'") {
                                state = states.single;
                                newCommand += c;
                            } else if (c == '\\') {
                                ++i;
                                c = command.charAt(i);
                                newCommand += '\\' + c;
                            } else if (c == '$') {
                                var dollar = i;
                                ++i;
                                var bracket = (command.charAt(i) == '{');
                                var space;
                                if (bracket) {
                                    ++i;
                                    space = command.indexOf('}', i);
                                    if (space < 0) {
                                        throw 'closing bracket expected.';
                                    }
                                } else {
                                    space = i;
                                    while (space < command.length) {
                                        c = command.charAt(space);
                                        if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_') {
                                            break;
                                        }
                                        ++space;
                                    }
                                }
                                var name = command.substring(i, space);
                                var value = options.variables[name];
                                i = space;
                                if (!bracket) {
                                    --i;
                                }
                                if (value != null) {
                                    command = command.substr(0, dollar) + value.join(' ') + command.substr(i + 1);
                                    i = dollar - 1;
                                }
                            } else {
                                newCommand += c;
                            }
                            break;

                        case states.double:
                            if (c == '"') {
                                newCommand += c;
                                state = states.command;
                            } else if (c == '\\') {
                                ++i;
                                c = command.charAt(i);
                                newCommand += '\\' + c;
                            } else if (c == '$') {
                                var dollar = i;
                                ++i;
                                var bracket = (command.charAt(i) == '{');
                                var space;
                                if (bracket) {
                                    ++i;
                                    space = command.indexOf('}', i);
                                    if (space < 0) {
                                        throw 'closing bracket expected.';
                                    }
                                } else {
                                    space = i;
                                    while (space < command.length) {
                                        c = command.charAt(space);
                                        if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && (c < '0' || c > '9') && c != '_') {
                                            break;
                                        }
                                        ++space;
                                    }
                                }
                                var name = command.substring(i, space);
                                var value = options.variables[name];
                                i = space;
                                if (!bracket) {
                                    --i;
                                }
                                if (value != null) {
                                    command = command.substr(0, dollar) + value.join(' ') + command.substr(i + 1);
                                    i = dollar - 1;
                                }
                            } else {
                                newCommand += c;
                            }
                            break;

                        case states.single:
                            if (c == "'") {
                                newCommand += c;
                                state = states.command;
                            } else {
                                newCommand += c;
                            }
                            break;

                    }

                }

                // split
                var command = [];
                var arg;
                state = states.space;

                for (var i = 0; i < newCommand.length; ++i) {
                    var c = newCommand.charAt(i);

                    switch (state) {
                        case states.space:
                            if (c == '"') {
                                state = states.double;
                                arg = '';
                            } else if (c == "'") {
                                state = states.single;
                                arg = '';
                            } else if (c != ' ') {
                                arg = '';
                                state = states.argument;
                                --i;
                            }
                            break;

                        case states.argument:
                            if (c == ' ') {
                                command.push(arg);
                                arg = null;
                                state = states.space;
                            } else if (c == '"') {
                                state = states.double;
                            } else if (c == "'") {
                                state = states.single;
                            } else if (c == '\\') {
                                ++i;
                                c = newCommand.charAt(i);
                                arg += c;
                            } else {
                                arg += c;
                            }
                            break;

                        case states.double:
                            if (c == '"') {
                                state = states.argument;
                            } else if (c == '\\') {
                                ++i;
                                c = newCommand.charAt(i);
                                arg += c;
                            } else {
                                arg += c;
                            }
                            break;

                        case states.single:
                            if (c == "'") {
                                state = states.argument;
                            } else {
                                arg += c;
                            }
                            break;
                    }
                }

                if (arg != null) {
                    command.push(arg);
                }

                return command;
            }

            // at the end of execute
            function endExecution(result) {
                if (!result) {
                    result = [];
                } else if (typeof result != 'object') {
                    result = [ result.toString() ];
                }

                options.variables.LAST = $.map(result, strip);
                saveOptions();

                if (result.length) {
                    var data = result.join('\n');
                    var html = $(toHtml(data));
                    attachHandlers(html);
                    content.append(html);
                    content.append('<br/>');
                }

                if ($.cookie) {
                    $.cookie(cookieName, JSON.stringify({
                        history: history,
                        variables: options.variables
                    }));
                }

                showInput();
            }

            // remove result cache
            function deleteResult(node) {
                delete node.result;

                if (node.operator) {
                    deleteResult(node.first);
                    deleteResult(node.second);
                }
            }

            // execute tree
            function exec(node, args) {
                // THIS node is waiting for the result!
                if (node.result === true) {
                    if (!asyncResult) {
                        var command = split(node.command);
                        throw 'Unknown command \tb' + command[0] + '\tb.';
                    }
                    node.result = asyncResult;
                }

                // if result already computed
                if (node.result) {
                    return node.result;
                }

                // node is a command
                if (node.command) {
                    var result;

                    var command = split(node.command);

                    if (command.length == 0) {
                        return 'No command.';
                    }

                    $.merge(command, $.map(args, strip));

                    while (command.length > 0 && command[0] == 'exec') {
                        command = split(command.slice(1).join(' '));
                    }

                    if (command.length > 0) {
                        $.each(options.listeners, function (i, listener) {
                            result = listener.execute(command, asyncExecute);
                            if (result) {
                                return false;
                            }
                        });

                        if (result == null) {
                            throw 'Unknown command \tb' + command[0] + '\tb.';
                        }
                    } else {
                        result = [];
                    }

                    node.result = result;

                    return result;
                }

                // node is an operator
                switch (node.operator) {
                    case ';;':
                        if (exec(node.first, args) === true) {
                            return true;
                        }
                        return exec(node.second, []);

                    case ';':
                        var ret = exec(node.first, args);
                        if (ret === true) {
                            return true;
                        }
                        var ret2 = exec(node.second, []);
                        if (ret2 === true) {
                            return true;
                        }
                        return $.merge($.merge([], ret), ret2);

                    case '~':
                        var ret = exec(node.first, args);
                        if (ret === true) {
                            return true;
                        }
                        var ret2 = exec(node.second, ret);
                        if (ret2 === true) {
                            return true;
                        }
                        return $.merge($.merge([], ret), ret2);

                    case '||':
                        var ret = exec(node.first, args);
                        if (ret === true) {
                            return true;
                        }
                        return (ret.length > 0) ? ret : exec(node.second, []);

                    case '&&':
                        var ret = exec(node.first, args);
                        if (ret === true) {
                            return true;
                        }
                        return (ret.length > 0) ? exec(node.second, []) : ret;

                    case '|':
                        var result = exec(node.first, args);
                        if (result === true) {
                            return true;
                        }
                        return exec(node.second, result);

                    case ':':
                        var command = split(node.first.command);
                        switch (command[0]) {

                            case 'foreach':
                                if (command.length > 1) {
                                    var value = options.variables[command[1]];

                                    if (node.args == null) {
                                        $.merge(command, args);
                                        node.args = command.slice(2);
                                        node.resulting = [];
                                    }

                                    while (node.args.length > 0) {
                                        var arg = node.args.shift();
                                        options.variables[command[1]] = [ arg ];
                                        var result = exec(node.second, []);
                                        if (result === true) {
                                            node.args.unshift(arg);
                                            if (value == null) {
                                                delete options.variables[command[1]];
                                            } else {
                                                options.variables[command[1]] = value;
                                            }
                                            return true;
                                        }
                                        deleteResult(node.second);
                                        $.merge(node.resulting, result);
                                    }

                                    if (value == null) {
                                        delete options.variables[command[1]];
                                    } else {
                                        options.variables[command[1]] = value;
                                    }

                                    delete node.args;

                                    node.result = node.resulting;
                                    return node.result;
                                } else {
                                    throw 'foreach: variable name required.';
                                }
                                break;

                            default:
                                throw 'Unknown command \tb' + command[0] + '\tb.';
                        }

                    case '&':
                        var ret = exec(node.first, args);
                        if (ret === true) {
                            return true;
                        }
                        var ret2 = exec(node.second, args);
                        if (ret2 === true) {
                            return true;
                        }
                        return $.merge($.merge([], ret), ret2);
                }
            }

            function asyncExecute(result) {
                asyncResult = result;

                try {
                    result = exec(executionTree, []);
                } catch (e) {
                    result = e;
                }

                if (result !== true) {
                    endExecution(result);
                }
            }

            function complete() {
                loadOptions();

                var caret = input.caret();

                var originalCommand = input.val().replace('\t', ' ');

                // indicate caret position
                command = originalCommand.substr(0, caret.start) + '\b' + originalCommand.substr(caret.end);

                try {
                    var tree = parse(command);

                    // find node where the caret is
                    function traverse(node) {
                        if (node.command) {
                            if (caret.start >= node.start && caret.start <= node.end) {
                                return node;
                            }
                        } else {
                            return traverse(node.second) || traverse(node.first);
                        }
                    }

                    var currentNode = traverse(tree);

                    if (currentNode) {
                        currentNodeCommand = split(currentNode.command);

                        var index;
                        var pos;

                        // get caret position
                        $.each(currentNodeCommand, function (i, arg) {
                            pos = arg.indexOf('\b');
                            if (pos >= 0) {
                                index = i;
                                return false;
                            }
                        });

                        if (index != null) {
                            var search = currentNodeCommand[index].substr(0, pos);
                            var result;
                            var asyncResult;

                            var copyListeners = $.merge([], options.listeners);

                            function asyncComplete(asyncResult) {
                                result = asyncResult;
                                compl();
                            }

                            function compl() {
                                // complete command
                                if (index == 0) {
                                    result = [];
                                    $.each(options.listeners, function (i, listener) {
                                        $.merge(result, $.map(listener.commands, function (value, key) {
                                            if (key.substr(0, pos) == search) {
                                                return key;
                                            }
                                        }));
                                    });

                                    // complete command
                                } else if (!result) {
                                    var args = currentNodeCommand.slice(0, index);
                                    args.push(search);

                                    while (copyListeners.length > 0) {
                                        listener = copyListeners.shift();
                                        result = listener.complete(args, asyncComplete);
                                        if (result === true) {
                                            return;
                                        }
                                        if (result) {
                                            break;
                                        }
                                    }
                                }

                                if (result) {
                                    // only one sugugestion
                                    if (result.length == 1) {
                                        var args = currentNodeCommand.slice(0, index).concat(result, [ '' ], currentNodeCommand.slice(index + 1));
                                        var offset = 0;
                                        var currentCommand = $.map(args,function (arg, i) {
                                            /* TODO: this code returns the double quotes
                                             if (notSimpleRegExp.test(arg)) {
                                             if (i <= index) {
                                             offset += 2;
                                             }
                                             return '"' + arg.replace('"', '\"') + '"';
                                             } else {
                                             return arg;
                                             }
                                             */
                                            return arg;
                                        }).join(' ');
                                        command = command.substr(0, currentNode.start) + currentCommand + command.substr(currentNode.end);
                                        input.val(command);
                                        var caretPosition = caret.start - search.length + result[0].length + 1 + offset;
                                        input.caret({
                                            start: caretPosition,
                                            end: caretPosition
                                        });

                                        // several suggestion
                                    } else if (result.length > 1) {
                                        result.sort();

                                        input.detach();

                                        var span = $('<span></span>');
                                        span.text(originalCommand);
                                        content.append(span);
                                        content.append('<br/>');

                                        // display suggestions in columns
                                        var max = 0;
                                        $.each(result, function (i, command) {
                                            if (command.length > max)
                                                max = command.length;
                                        });
                                        max += 2;
                                        var mult;
                                        var cols = Math.floor(size.columns / max);
                                        if (result.length <= cols) {
                                            mult = max;
                                        } else {
                                            mult = Math.floor(size.columns / cols);
                                        }
                                        var out = [], o = -1;
                                        for (var i = 0, n = result.length; i < n; ++i) {
                                            if (i) {
                                                if (i % cols == 0)
                                                    out[++o] = '\n';
                                                else
                                                    out[++o] = '\tp' + (i % cols) * mult + '\tp';
                                            }
                                            out[++o] = result[i];
                                        }
                                        data = out.join('');
                                        var html = $(toHtml(data));
                                        attachHandlers(html);
                                        content.append(html);
                                        content.append('<br/>');

                                        // extend command line if possible
                                        var newArg = '';
                                        similoop: for (var i = 0; ; ++i) {
                                            var c = null;
                                            for (var com in result) {
                                                if (i >= result[com].length) {
                                                    break similoop;
                                                } else if (c == null) {
                                                    c = result[com].charAt(i);
                                                } else if (result[com].charAt(i) != c) {
                                                    break similoop;
                                                }
                                            }
                                            newArg += c;
                                        }

                                        var args = currentNodeCommand.slice(0, index).concat([ newArg ], currentNodeCommand.slice(index + 1));
                                        var offset = 0;

                                        var currentCommand = $.map(args,function (arg, i) {
                                            /*
                                             if (notSimpleRegExp.test(arg)) {
                                             if (i <= index) {
                                             offset += 2;
                                             }
                                             return '"' + arg.replace('"', '\"') + '"';
                                             } else {
                                             return arg;
                                             }
                                             */
                                            return arg;
                                        }).join(' ');
                                        command = command.substr(0, currentNode.start) + currentCommand + command.substr(currentNode.end);
                                        var caretPosition = caret.start - search.length + newArg.length + offset;
                                        showInput(command);
                                        input.caret({
                                            start: caretPosition,
                                            end: caretPosition
                                        });
                                    }
                                }
                            }

                            compl();
                        }
                    }
                } catch (e) {
                }

                updateScrollbar();
            }

            function execute(inputString) {
                loadOptions();

                var command = inputString || input.val().replace('\t', ' ');

                if (command.length > 0) {

                    // put in history
                    history.push(command);
                    if (history.length > options.history) {
                        history.shift();
                    }
                    currentHistory = null;

                    // hide input
                    input.detach();

                    // show command
                    var commandSpan = $('<span></span>');
                    commandSpan.text(command);
                    content.append(commandSpan);
                    content.append('<br/>');

                    var result;

                    // get execution tree
                    var tree;
                    try {
                        tree = parse(command);
                    } catch (e) {
                        result = 'Parse error: ' + e;
                    }

                    // execute tree
                    if (tree) {
                        if (tree.command || tree.operator) {
                            executionTree = tree;
                            try {
                                result = exec(tree, []);
                            } catch (e) {
                                result = e;
                            }
                        } else {
                            result = [];
                        }
                    }

                    // if synchronous result
                    if (result !== true) {
                        endExecution(result);
                    }
                } else {
                    // no command given
                    content.append('<br/>');
                    showInput();
                }
            }

            terminal.addClass('jqueryTerminal');

            // get the dimension of characters
            var space = $('<span>&nbsp;</span>');
            terminal.append(space);
            var character = {
                width: space.width() + 0.4,
                height: space.height()
            };
            space.remove();

            content = $('<div class="content"></div>');
            terminal.append(content);

            var arrowUp = $('<div class="scrollarrow" style="top:0">&#9650;</div>');
            terminal.append(arrowUp);
            arrowUp.click(function () {
                content.scrollTop(content.scrollTop() - character.height);
                terminal.children('input').css('top', content.children(':last').position().top);
                updateScrollbar();
                return false;
            });

            var arrowDown = $('<div class="scrollarrow" style="bottom:0">&#9660;</div>');
            terminal.append(arrowDown);
            arrowDown.click(function () {
                content.scrollTop(content.scrollTop() + character.height);
                terminal.children('input').css('top', content.children(':last').position().top);
                updateScrollbar();
                return false;
            });

            bar = $('<div class="scrollbar">&nbsp;</div>');
            terminal.append(bar);

            var barOffset = null;

            bar.mousedown(function (e) {
                barOffset = {
                    mouse: e.pageY || event.clientY + document.body.scrollTop,
                    top: parseInt(bar.css('top'))
                };
                return false;
            });

            input = $('<input type="text"/>');

            input.keydown(function (e) {
                switch (e.which) {
                    // tab
                    case 9:
                        complete();
                        break;

                    // enter
                    case 13:
                    case undefined:
                        execute();
                        break;

                    // up
                    case 38:
                        if (currentHistory == null) {
                            currentCommand = $(this).val();
                            currentHistory = history.length;
                        }
                        --currentHistory;
                        if (currentHistory < 0)
                            currentHistory = 0;
                        $(this).val(history[currentHistory]);
                        break;

                    // down
                    case 40:
                        if (currentHistory != null) {
                            ++currentHistory;
                            if (currentHistory >= history.length) {
                                currentHistory = null;
                                $(this).val(currentCommand);
                            } else
                                $(this).val(history[currentHistory]);
                        }
                        break;
                    default:
                        currentHistory = null;
                        return true;
                }

                focusInput();
                return false;
            });

            popup = $('<div class="popup"></div>');
            terminal.append(popup);

            resize();

            $(document).mousemove(function (e) {
                if (barOffset) {
                    var mouse = e.pageY || event.clientY + document.body.scrollTop;
                    var delta = mouse - barOffset.mouse;
                    var top = delta + barOffset.top;
                    if (top < character.height)
                        top = character.height;
                    var maxtop = terminal.height() - bar.height() - character.height;
                    if (top >= maxtop)
                        top = maxtop;
                    content.scrollTop((top - character.height) / (maxtop - character.height) * (content[0].scrollHeight - content.height()));
                    terminal.children("input").css('top', content.children(":last").position().top);
                    bar.css('top', top + 'px');
                    return false;
                }
            });

            $(document).mouseup(function () {
                barOffset = null;
            });

            // allow text selection without focusing on input
            var moved;

            content.mousedown(function () {
                moved = false;
            });

            content.mousemove(function () {
                moved = true;
            });

            content.mouseup(function () {
                if (!moved) {
                    focusInput();
                    bar.css('top', (terminal.height() - bar.height() - character.height) + 'px');
                    //TODO returns false here will make the 'draggable window' sticks to the mouse
                    //return false;
                }
            });

            function mousewheel(e) {
                if (!e) {
                    e = window.event;
                }

                var delta = 0;
                if (e.wheelDelta) {
                    delta = e.wheelDelta / 120;
                } else if (e.detail) {
                    delta = -e.detail / 3;
                }

                content.scrollTop(content.scrollTop() - delta * character.height);
                terminal.children("input").css('top', content.children(":last").position().top);
                updateScrollbar();

                e.preventDefault();
                e.stopPropagation();
            };

            var mousewheelName = $.browser.mozilla ? 'DOMMouseScroll' : 'mousewheel';

            if (terminal[0].attachEvent) {
                terminal[0].attachEvent('on' + mousewheelName, mousewheel);
            } else if (terminal[0].addEventListener) {
                terminal[0].addEventListener(mousewheelName, mousewheel, false);
            }

            $(window).resize(resize);

            if (options.welcome != null) {
                var html = $(toHtml(options.welcome));
                attachHandlers(html);
                content.append(html);
                content.append('<br/>');
            }

            showInput();
        });
    };
})(jQuery);
