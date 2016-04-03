// get the necessary java-types from java.util.logging-package
var Logger = Java.type("java.util.logging.Logger");
var FileHandler = Java.type("java.util.logging.FileHandler");
var SimpleFormatter = Java.type("java.util.logging.SimpleFormatter");

// EventLogger-class
function EventLogger(logger) {
	this.logger = logger
	this.eventHandler = function(event) {
		var logMessage = event.getTopic();
		var propertyNames = event.getPropertyNames();
		for each (var propertyName in propertyNames) {
		}
		this.logger.info(logMessage);
	}
}

// remove the per-default installed console-handler
var rootLogger = Logger.getLogger("");
var handlers = rootLogger.getHandlers();
rootLogger.removeHandler(handlers[0]);
// install a new file-handler with simple-formatting
var logger = Logger.getLogger("org.modulaplayer.EventLogger");
var logHandler = new FileHandler("/tmp/events.log");
var formatter = new SimpleFormatter();
logHandler.setFormatter(formatter);
logger.addHandler(logHandler);
// instantiate EventLogger, register it as EventHandler for all org/bragi/*-events and return the EventLogger-object
var eventLogger=new EventLogger(logger);
engine.registerEventHandler("org/bragi/*", eventLogger, eventLogger.eventHandler);
eventLogger;
