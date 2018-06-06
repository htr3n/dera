jQuery.noConflict();

(function ($) {
    "use strict";

    function storeAndDrawEvents(jqxhr) {
        if (jqxhr) {
            var events = $.parseJSON(jqxhr.responseText);
            if (events) {
                $.each(events, function (i, e) {
                    if (e && e.type) {
                        info('Got Event {' + e.type + ' | ' + e.attributes + '}');
                        VARS.events[e.type] = e;
                        var eventNode = new DERA.EventNode(e, random_x(), random_y());
                        if (eventNode)
                            VARS.eventToNode[e.type] = eventNode.node;
                    }
                });
                DERA.resetDirtyEvents();
            }
        }
        return false;
    }

    function drawActor(actor) {
        var node;
        switch (actor.type) {
            case "Barrier":
                node = new DERA.BarrierNode(actor, random_x(), random_y());
                break;
            case "Condition":
                node = new DERA.ConditionNode(actor, random_x(), random_y());
                break;
            case "EventActor":
                node = new DERA.ActorNode(actor, random_x(), random_y());
                break;
            case "Trigger":
                node = new DERA.TriggerNode(actor, random_x(), random_y());
                break;
            default :
                if (VARS.DEBUG)
                    console.log('The actor ' + actor.id + ' of type ' + actor.type + ' is not supported');
                break;
        }
        return node;
    }

    function storeAndDrawActors(jqxhr) {
        if (jqxhr) {
            var actors = $.parseJSON(jqxhr.responseText);
            if (actors) {
                $.each(actors, function (i, actor) {
                    if (actor && actor.type && actor.id) {
                        info('Got ' + actor.type + '{' + actor.id + ', input = ' + actor.input + '}');
                        VARS.actors[actor.type + '::' + actor.id] = actor;
                        var actorNode = drawActor(actor);
                        if (actorNode) {
                            VARS.typeIdToActorNode[actor.type + '::' + actor.id] = actorNode.node;
                            // connect the actors with the input events
                            if (actor.input) {
                                $.each(actor.input, function (i, event) {
                                    if (VARS.DEBUG)
                                        info('... draw link ' + event + ' -> ' + actor.id);
                                    var eventNode = VARS.eventToNode[event];
                                    if (eventNode)
                                        new DERA.EventLink(event + '::' + actor.id, eventNode, actorNode.node);
                                });
                            }
                            // regarding most of actors except Condition
                            if (actor.output){
                                $.each(actor.output, function (i, event) {
                                    if (VARS.DEBUG)
                                        info('... draw link ' + event + ' -> ' + actor.id);
                                    var eventNode = VARS.eventToNode[event];
                                    if (eventNode)
                                        new DERA.EventLink(actor.id + '::' + event, actorNode.node, eventNode);
                                });
                            }
                            // regarding true events of a Condition
                            if (actor.trueEvent){
                                $.each(actor.trueEvent, function (i, event) {
                                    if (VARS.DEBUG)
                                        info('... draw link ' + event + ' -> ' + actor.id);
                                    var eventNode = VARS.eventToNode[event];
                                    if (eventNode)
                                        new DERA.EventLink(actor.id + '::' + event, actorNode.whenTrue, eventNode);
                                });
                            }
                            // regarding false events of a Condition
                            if (actor.falseEvent){
                                $.each(actor.falseEvent, function (i, event) {
                                    if (VARS.DEBUG)
                                        info('... draw link ' + event + ' -> ' + actor.id);
                                    var eventNode = VARS.eventToNode[event];
                                    if (eventNode)
                                        new DERA.EventLink(actor.id + '::' + event, actorNode.whenFalse, eventNode);
                                });
                            }
                        }
                    }
                });
                DERA.resetDirtyActors();
            }
        }
        return false;
    }

    function drawInitialGraph() {
        $.when($.ajax("/domain/events"), $.ajax("/domain/actors"))
            .done(function (arg1, arg2) {
                storeAndDrawEvents(arg1[2]); // arguments are [ "success", statusText, jqxhr ]
                storeAndDrawActors(arg2[2]);
                performLayout();
            });
        return false;
    }

    function initDeraVisualization() {
        // initially retrieve actors' information from DERA
        $.getJSON('domain/actors', function (data) {
            $.each(data, function (index, actor) {
                if (actor) {
                    info('Got ' + actor.type + '{' + actor.id + '}');
                    // add each input event to the map of consumers
                    if (actor.input) {
                        $.each(actor.input, function (i, e) {
                            if (!VARS.excludedEvents.hasOwnProperty(e)) { // do not add the excluded events
                                addToMap(e, actor, VARS.eventToConsumers);
                                addToSet(e, VARS.events);
                            }
                        });
                    }
                    // add each output event to the map of producers
                    if (actor.trueEvent) {
                        $.each(actor.trueEvent, function (index, e) {
                            addToMap(e, actor, VARS.eventToProducers);
                            addToSet(e, VARS.events);
                        });
                    }
                    if (actor.falseEvent) {
                        $.each(actor.falseEvent, function (index, e) {
                            addToMap(e, actor, VARS.eventToProducers);
                            addToSet(e, VARS.events);
                        });
                    }

                    if (actor.output) {
                        $.each(actor.output, function (index, e) {
                            addToMap(e, actor, VARS.eventToProducers);
                            addToSet(e, VARS.events);
                        });
                    }
                    if (!VARS.actors[actor.id])
                        VARS.actors[actor.id] = actor;
                }
            });
            drawActors();
            drawEvents();
            performLayout();
        });
        return false;
    }

    function _drawEvents() {
        var model = VARS.deraGraph.getModel();
        model.beginUpdate();
        try {
            $.each(Object.keys(VARS.events), function (i, e) {
                if (!VARS.excludedEvents.hasOwnProperty(e)) { //only draw events that are not excluded

                    var eventNode = new DERA.EventNode(e, random_x(), random_y());

                    VARS.eventToNode[e] = eventNode.node;

                    // connect to producers and consumers
                    var producers = VARS.eventToProducers[e];
                    var consumers = VARS.eventToConsumers[e];

                    if (!producers) { // no producers, set warning
                        VARS.deraGraph.setCellStyles(mxConstants.STYLE_FILLCOLOR, '#8B008B', [eventNode.node]);
                        //VARS.deraGraph.addCellOverlay(eventNode.node, DERA.createOverlay(VARS.deraGraph.warningImage, 'Warning: no producers'));
                    }

                    if (producers) {
                        $.each(producers, function (i, producer) {
                            if (producer) {
                                var source = VARS.typeIdToActorNode[producer.type + producer.id];
                                switch (producer.type) {
                                    case 'Condition':
                                        if (producer.trueEvent && $.inArray(e, producer.trueEvent) !== -1) {
                                            new DERA.EventLink(producer.id + e, source.whenTrue, eventNode.node);
                                        }
                                        if (producer.falseEvent && $.inArray(e, producer.falseEvent) !== -1) {
                                            new DERA.EventLink(producer.id + e, source.whenFalse, eventNode.node);
                                        }
                                        break;
                                    default :
                                        if (producer.input && $.inArray(e, producer.output) !== -1) {
                                            new DERA.EventLink(producer.id + e, source.node, eventNode.node);
                                        }
                                        break;
                                }
                            }
                        });
                    }
                    if (consumers) {
                        $.each(consumers, function (i, consumer) {
                            if (consumer) {
                                var target = VARS.typeIdToActorNode[consumer.type + consumer.id];
                                new DERA.EventLink(e + consumer.id, eventNode.node, target.node);
                            }
                        });
                    }
                }
            })
        }
        finally {
            model.endUpdate();
        }
        return false;
    }

    function performLayout() {
        if (VARS.graphLayout) {
            VARS.graphLayout.execute(VARS.deraGraph.getDefaultParent());
        }
        return false;
    }

    function random_x() {
        return randomRange(VARS.canvas_left, VARS.canvas_width);
    }

    function random_y() {
        return randomRange(VARS.canvas_top, VARS.canvas_height);
    }

    function randomRange(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    function handleApplications(jqxhr) {
        if (jqxhr) {
            var data = $.parseJSON(jqxhr.responseText);
            if (data) {
                $.each(data, function (i, a) {
                    if (a && a.id) {
                        info('Got Application {' + a.id + '}');
                        VARS.applications[a.id] = a;
                    }
                });
            }
        }
        return false;
    }


    /*================================================================================================================*/
    drawInitialGraph();

})(jQuery);