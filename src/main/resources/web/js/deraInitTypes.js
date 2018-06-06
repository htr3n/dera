jQuery.noConflict();

(function ($) {
    "use strict";

    DERA._construct = function () {

        /* command element */
        function Command(cmd) {
            this.cmd = cmd;
            this.elementType = null;
            this.elementId = null;
        }

        /* model elements */
        function Actor(type, id) {
            this.type = type;
            this.id = id;
            this.input = [];
        }

        function Event(type) {
            this.type = type;
            this.attributes = {
                type: type,
                correlationId: null
            };
        }

        function Application(id) {
            this.id = id;
            this.start = [];
            this.end = [];
        }


        /* graphical elements */
        function ActorNode(actor, x, y) {
            this.node = VARS.deraGraph.insertVertex(VARS.deraGraph.getDefaultParent(), actor.id, actor, x, y,
                VARS.NODE_WIDTH, VARS.NODE_HEIGHT);
        }

        function BarrierNode(actor, x, y) {
            this.node = VARS.deraGraph.insertVertex(VARS.deraGraph.getDefaultParent(), actor.id, actor, x, y,
                VARS.NODE_WIDTH, VARS.NODE_HEIGHT,
                +mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_LABEL + ";"
                    + mxConstants.STYLE_IMAGE + "=img/Barrier.png;"
                    + mxConstants.STYLE_FILLCOLOR + "=" + VARS.FILL_COLOR_BARRIER + ";"
            );
        }

        function ConditionNode(actor, x, y) {
            var portSize = 6;
            this.node = VARS.deraGraph.insertVertex(VARS.deraGraph.getDefaultParent(), actor.id, actor, x, y,
                VARS.NODE_WIDTH, VARS.NODE_HEIGHT,
                +mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_LABEL + ";"
                    + mxConstants.STYLE_IMAGE + "=img/Condition.png;"
                    + mxConstants.STYLE_FILLCOLOR + "=" + VARS.FILL_COLOR_CONDITION + ";"
            );
            this.node.setConnectable(false);
            this.whenTrue = VARS.deraGraph.insertVertex(this.node, null, '', 1, 0, portSize, portSize);
            this.whenTrue.geometry.offset = new mxPoint(-4, 4);
            this.whenTrue.geometry.relative = true;
            this.whenFalse = VARS.deraGraph.insertVertex(this.node, null, '', 1, 1, portSize, portSize);
            this.whenFalse.geometry.offset = new mxPoint(-4, -10);
            this.whenFalse.geometry.relative = true;
            VARS.deraGraph.setCellStyle(mxConstants.STYLE_ROUNDED + '=false', [this.whenFalse, this.whenTrue]);
            VARS.deraGraph.setCellStyle(mxConstants.STYLE_FILLCOLOR + '=black', [this.whenTrue]);
            VARS.deraGraph.setCellStyle(mxConstants.STYLE_FILLCOLOR + '=white', [this.whenFalse]);
        }

        function EventNode(event, x, y) {
            this.node = VARS.deraGraph.insertVertex(VARS.deraGraph.getDefaultParent(), event.type, event, x, y,
                VARS.NODE_WIDTH, VARS.NODE_HEIGHT,
                mxConstants.STYLE_DASHED + ";"
                    + mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_DOUBLE_ELLIPSE + ";"
                    + mxConstants.STYLE_FILLCOLOR + "=" + VARS.FILL_COLOR_EVENT + ";"
                    + mxConstants.STYLE_FONTCOLOR + "=" + VARS.COLOR_EVENT + ";"
                    + mxConstants.STYLE_FONTSTYLE + "=" + mxConstants.FONT_BOLD + ";"
                    + mxConstants.STYLE_STROKEWIDTH + "=1" + ";"
            );
        }

        function PhantomNode(event, x, y) {
            this.node = VARS.deraGraph.insertVertex(VARS.deraGraph.getDefaultParent(), event.type, event, x, y,
                VARS.NODE_WIDTH, VARS.NODE_HEIGHT,
                mxConstants.STYLE_DASHED + ";"
                    + mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_ELLIPSE + ";"
                    + mxConstants.STYLE_FILLCOLOR + "=" + VARS.FILL_COLOR_PHANTOM + ";"
                    + mxConstants.STYLE_FONTCOLOR + "=" + VARS.COLOR_EVENT + ";"
                    + mxConstants.STYLE_FONTSTYLE + "=" + mxConstants.FONT_BOLD + ";"
            );
        }

        function EventLink(event, source, target) {
            this.link = VARS.deraGraph.insertEdge(VARS.deraGraph.getDefaultParent(), null, null, source, target);
        }

        function BridgeNode(actor, x, y) {
            this.node = VARS.deraGraph.insertVertex(VARS.deraGraph.getDefaultParent(), actor.id, actor, x, y,
                VARS.NODE_WIDTH, VARS.NODE_HEIGHT,
                +mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_LABEL + ";"
                    + mxConstants.STYLE_IMAGE + "=img/Bridge.png;"
            );
        }

        function TriggerNode(actor, x, y) {
            this.node = VARS.deraGraph.insertVertex(VARS.deraGraph.getDefaultParent(), actor.id, actor, x, y,
                VARS.NODE_WIDTH, VARS.NODE_HEIGHT,
                +mxConstants.STYLE_SHAPE + "=" + mxConstants.SHAPE_LABEL + ";"
                    + mxConstants.STYLE_IMAGE + "=img/Trigger.png;"
            );
        }

        /**
         * Creates an overlay object using the given tooltip and text for the alert window
         * which is being displayed on click.
         */
        function createOverlay(image, tooltip) {
            var overlay = new mxCellOverlay(image, tooltip);
            // Installs a handler for clicks on the overlay
            overlay.addListener(mxEvent.CLICK, function (sender, evt) {
                mxUtils.alert(tooltip + '\n' + 'Warning:');
            });

            return overlay;
        };

        this.createOverlay = createOverlay;

        // the graph elements
        this.Command = Command;
        this.ActorNode = ActorNode;
        this.BarrierNode = BarrierNode;
        this.ConditionNode = ConditionNode;
        this.EventLink = EventLink;
        this.EventNode = EventNode;
        this.BridgeNode = BridgeNode;
        this.TriggerNode = TriggerNode;
        this.PhantomNode = PhantomNode;

        // the model elements
        this.Event = Event;
        this.Actor = Actor;
        this.Application = Application;
    }

    DERA._construct();

})(jQuery);
