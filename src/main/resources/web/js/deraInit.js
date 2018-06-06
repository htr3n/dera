jQuery.noConflict();

var VARS = VARS || {};
var DERA = DERA || {};
var LISTENERS = LISTENERS || {};

(function ($) {

    $.fn.center = function () {
        this.css("position", "absolute");
        this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) +
            $(window).scrollTop()) + "px");
        this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) +
            $(window).scrollLeft()) + "px");
        return this;
    }

    VARS._construct = function () {

        this.elementTypes = ['Application', 'Barrier', 'Condition', 'Event', 'EventActor'];

        this.excludedEvents = { // special events to be excluded
            "EVENT_HIGH_PRIORITY": true,
            "EVENT_LOW_PRIORITY": true
        };

        this.DEBUG = false;
        this.LOG_TIMESTAMP = false;

        this.FILL_COLOR_ACTOR = '#a0c88f';
        this.FILL_COLOR_BARRIER = '#ffff10';
        this.FILL_COLOR_CONDITION = '#ffaf1b';
        this.FILL_COLOR_EVENT = '#daccbc';
        this.FILL_COLOR_PHANTOM = '#ffffff';
        this.COLOR_EVENT = '#0000e0';

        this.NODE_WIDTH = 24;
        this.NODE_HEIGHT = 24;

        this.LOG_WIDTH = 500;
        this.LOG_HEIGHT = 550;

        this.TERM_WIDTH = 600;
        this.TERM_HEIGHT = 600;
        this.TERM_WIDTH_MIN = 600;

        this.terminalOpened = true;
        this.logOpened = false;

        // manages DERA model elements
        this.applications = {};         // mimic a set: app id -> app
        this.actors = {};               // mimic a set: actor id -> actor
        this.events = {};               // mimic a set: event type -> event

        // manages graphical elements
        this.typeIdToActorNode = {};    // map: actor's type + id -> graph node
        this.eventToNode = {};          // map: event type -> graph node

        this.eventToConsumers = {};     // map: event type  -> actors who consume it
        this.eventToProducers = {};     // map: event type  -> actors who produce it

        // html elements
        this.header = document.getElementById('header');
        this.container = document.getElementById('container');
        this.deraCanvas = document.getElementById('canvas');
        this.deraTerm = document.getElementById('deraTerm');
        this.deraGraph = new mxGraph(this.deraCanvas);

        this.logWindow;

        this.logContent = document.createElement('div');

        // start
        initLogging();

        updateCanvasSize();

        initTerminalSize();

        initLayout();

        initGraph();

        initButtons();

        /* ========================================================================================================== */

        $(window).resize(function () {
            "use strict";
            updateCanvasSize();
        });

        this.showTerminal = function showTerminal() {
            if ($(this.deraTerm)) {
                $(this.deraTerm).show();
                $(this.deraTerm).focus();
                this.terminalOpened = true;
            }
            return false;
        }

        this.hideTerminal = function hideTerminal() {
            if ($(this.deraTerm)) {
                $(this.deraTerm).hide();
                this.terminalOpened = false;
            }
            return false;
        }

        this.showLog = function showLog() {
            if (this.logWindow) {
                this.logWindow.show();
                this.logOpened = true;
            }
            return false;
        }

        this.hideLog = function hideLog() {
            if (this.logWindow) {
                this.logWindow.hide();
                this.logOpened = false;
            }
            return false;
        }

        function initTerminalSize() {
            "use strict";
            $(VARS.deraTerm)
                .css('width', VARS.TERM_WIDTH + 'px')
                .css('height', VARS.TERM_HEIGHT + 'px')
                .css('min-width', VARS.TERM_WIDTH_MIN + 'px');
            return false;
        }

        function initLayout() {
            "use strict";
            var primaryLayout = new mxHierarchicalLayout(VARS.deraGraph, mxConstants.DIRECTION_WEST, true);
            var secondaryLayout = new mxParallelEdgeLayout(VARS.deraGraph);

            VARS.graphLayout = new mxCompositeLayout(VARS.deraGraph, [primaryLayout, secondaryLayout]);

            //The spacing buffer between unconnected hierarchies. Default is 60.
            primaryLayout.interHierarchySpacing = 30; // vertical

            //The spacing buffer added between cell on adjacent layers. Default is 50.
            primaryLayout.interRankCellSpacing = 20;  // horizontal

            //The spacing buffer added between cells on the same layer. Default is 30.
            primaryLayout.intraCellSpacing = 10;

            return false;
        }

        function updateCanvasSize() {
            "use strict";
            VARS.canvas_left = $(VARS.container).offset().left;
            VARS.canvas_top = $(VARS.header).height();
            VARS.canvas_width = $(VARS.container).width();
            VARS.canvas_height = $(VARS.container).height();
            $(VARS.deraCanvas).css('width', VARS.canvas_width).css('height', VARS.canvas_height);
            return false;
        }

        function initGraph() {
            "use strict";
            if (!mxClient.isBrowserSupported()) {
                mxUtils.error('Browser is not supported!', 200, false);
            }
            else {
                mxGraphHandler.prototype.guidesEnabled = true;
                mxEdgeHandler.prototype.isHandleVisible = function (index) {
                    return false;
                };
                VARS.deraGraph.htmlLabels = true;
                VARS.deraGraph.setTooltips(true);
                mxEvent.disableContextMenu(VARS.deraCanvas);
                VARS.deraGraph.setPanning(true); // for pop-up menu


                VARS.deraGraph.connectable = true;
                VARS.deraGraph.foldingEnabled = false;
                VARS.deraGraph.edgeLabelsMovable = true;
                VARS.deraGraph.resetEdgesOnResize = true;
                VARS.deraGraph.resetEdgesOnMove = true;
                VARS.deraGraph.allowDanglingEdges = false;
                VARS.deraGraph.disconnectOnMove = false;
                VARS.deraGraph.centerZoom = false;

                VARS.deraGraph.setBorder(10);

                VARS.deraGraph.maximumGraphBounds = new mxRectangle(
                    VARS.canvas_left,
                    VARS.canvas_top,
                    VARS.canvas_width,
                    VARS.canvas_height);

                new mxRubberband(VARS.deraGraph);
                new mxKeyHandler(VARS.deraGraph);

                var edgeStyle = VARS.deraGraph.getStylesheet().getDefaultEdgeStyle();
                edgeStyle[mxConstants.STYLE_ROUNDED] = true;
                edgeStyle[mxConstants.STYLE_ENDARROW] = mxConstants.ARROW_BLOCK;
                edgeStyle[mxConstants.STYLE_DASHED] = true;
                edgeStyle[mxConstants.STYLE_FONTCOLOR] = 'blue';
                edgeStyle[mxConstants.STYLE_FONTSIZE] = 9;
                edgeStyle[mxConstants.STYLE_LABEL_BACKGROUNDCOLOR] = 'white';
                //edgeStyle[mxConstants.STYLE_VERTICAL_ALIGN] = mxConstants.ALIGN_TOP;
                //edgeStyle[mxConstants.STYLE_LABEL_POSITION] = mxConstants.ALIGN_LEFT;
                /*
                 mxEdgeStyle.SegmentConnector || mxEdgeStyle.ElbowConnector
                 || mxEdgeStyle.SideToSide || mxEdgeStyle.TopToBottom
                 || mxEdgeStyle.EntityRelation ||  mxEdgeStyle.OrthConnector
                 */
                edgeStyle[mxConstants.STYLE_EDGE] = mxEdgeStyle.EntityRelation;
                edgeStyle[mxConstants.STYLE_ORTHOGONAL] = true;

                var vertexStyle = VARS.deraGraph.getStylesheet().getDefaultVertexStyle();
                vertexStyle[mxConstants.STYLE_RESIZABLE] = false;
                vertexStyle[mxConstants.STYLE_VERTICAL_ALIGN] = mxConstants.ALIGN_MIDDLE;
                vertexStyle[mxConstants.STYLE_VERTICAL_LABEL_POSITION] = mxConstants.ALIGN_BOTTOM;
                vertexStyle[mxConstants.STYLE_LABEL_POSITION] = mxConstants.ALIGN_CENTER;
                vertexStyle[mxConstants.STYLE_STROKECOLOR] = 'black';
                vertexStyle[mxConstants.STYLE_FILLCOLOR] = VARS.FILL_COLOR_ACTOR;
                vertexStyle[mxConstants.STYLE_GRADIENTCOLOR] = 'white';
                vertexStyle[mxConstants.STYLE_GRADIENT_DIRECTION] = mxConstants.DIRECTION_EAST;
                vertexStyle[mxConstants.STYLE_ROUNDED] = true;
                vertexStyle[mxConstants.STYLE_SHADOW] = false;
                vertexStyle[mxConstants.STYLE_FONTSTYLE] = mxConstants.FONT_ITALIC;
                vertexStyle[mxConstants.STYLE_FONTCOLOR] = 'black';

                // Overrides method to disallow edge label editing
                VARS.deraGraph.isCellEditable = function (cell) {
                    return !this.getModel().isEdge(cell);
                };

                // Overrides method to provide a cell label in the display
                VARS.deraGraph.convertValueToString = function (cell) {
                    if (VARS.DEBUG)
                        console.log(cell.value);
                    if (cell.value) {
                        if (typeof cell.value === "string") { // an event element
                            return cell.value;
                        } else { // an actor element
                            if (cell.value['id'])
                                return cell.value['id'];
                            else if (cell.value['type'])
                                return cell.value['type'];
                        }
                    }
                    return '';
                };

                // Overrides method to store a cell label in the model
                var cellLabelChanged = VARS.deraGraph.cellLabelChanged;

                VARS.deraGraph.cellLabelChanged = function (cell, newValue, autoSize) {
                    if (mxUtils.isNode(cell.value) &&
                        cell.value.nodeName.toLowerCase() == ('person')) {
                        var pos = newValue.indexOf(' ');

                        var firstName = (pos > 0) ? newValue.substring(0,
                            pos) : newValue;
                        var lastName = (pos > 0) ? newValue.substring(
                            pos + 1, newValue.length) : '';

                        // Clones the value for correct undo/redo
                        var elt = cell.value.cloneNode(true);

                        elt.setAttribute('firstName', firstName);
                        elt.setAttribute('lastName', lastName);

                        newValue = elt;
                        autoSize = true;
                    }

                    cellLabelChanged.apply(this, arguments);
                };

                //TODO override this to show nice tool tips
                mxGraph.getTooltipForCell = function (cell) {
                    var tip = null;
                    if (cell != null && cell.getTooltip != null) {
                        tip = cell.getTooltip();
                    }
                    else {
                        tip = this.convertValueToString(cell);
                    }
                    return tip;
                };
                ;
                //TODO : override ??
                // mxGraph.findTreeRoots();
                //var cell = graph.getSelectionCell();

                // Installs a popupmenu handler using local function (see below).
                /*
                 VARS.deraGraph.panningHandler.factoryMethod = function (menu, cell, evt) {
                 return createPopupMenu(menu, cell, evt);
                 };
                 */
            }
            // Function to create the entries in the popupmenu
            function createPopupMenu(menu, cell, evt) {
                if (cell != null) {
                    menu.addItem('Cell Item', 'editors/images/image.gif', function () {
                        mxUtils.alert('MenuItem1');
                    });
                }
                else {
                    menu.addItem('No-Cell Item', 'editors/images/image.gif', function () {
                        mxUtils.alert('MenuItem2');
                    });
                }
                menu.addSeparator();
                menu.addItem('MenuItem3', '../src/images/warning.gif', function () {
                    mxUtils.alert('MenuItem3: ' + VARS.deraGraph.getSelectionCount() + ' selected');
                });
            };
            return false;
        };

        function initButtons() {
            "use strict";

            $(document).tooltip();

            var termButton = document.getElementById('termButton');

            $(termButton).show();

            $(termButton).click(function (event) {
                    event.stopPropagation();
                    if ($(VARS.deraTerm)) {
                        if (!VARS.terminalOpened)
                            VARS.showTerminal();
                        else
                            VARS.hideTerminal();
                    }
                }
            );

            var logButton = document.getElementById('logButton');
            $(logButton).show();
            $(logButton).click(function (event) {
                    event.stopPropagation();
                    if (VARS.logWindow) {
                        if (!VARS.logOpened)
                            VARS.showLog();
                        else
                            VARS.hideLog();
                    }
                }
            );
            return false;
        }

        function initLogging() {
            "use strict";
            // logging window
            VARS.logWindow = new mxWindow('Log',
                VARS.logContent,
                Math.max(0, $(window).width() - VARS.LOG_WIDTH - 20) - 100,
                5,
                VARS.LOG_WIDTH,
                VARS.LOG_HEIGHT,
                true,
                true
            );
            VARS.logContent.style.fontFamily = "'Droid Sans Mono'; monospace";
            VARS.logContent.style.height = '100%';
            VARS.logContent.style.width = '100%'
            VARS.logContent.style.resize = 'none';
            VARS.logContent.style.backgroundColor = '#eeeeee';
            VARS.logWindow.setMaximizable(true);
            VARS.logWindow.setScrollable(true);
            VARS.logWindow.setResizable(true);
            VARS.logWindow.setVisible(VARS.logOpened);
            VARS.logWindow.setClosable(true);
            VARS.logWindow.destroyOnClose = false;
            return false;
        }
    };

    VARS._construct();

}(jQuery));
