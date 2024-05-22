// Initial beliefs
printer_ready(true).
error(false).


// Plans

// Notify mainframe that printer is ready
+error(false)
    : true
    <- .send(mainframe, tell, printer_ready);
        .print("Printer ready.").


// Print function when printer is ready
+print(Agent)[source(SenderAgent)]
    : printer_ready(true)
   <-  -printer_ready(true);
    +printer_ready(false);
    .print("Printing for ", Agent, "...");
    print;
    !print_success(Agent);
    +printer_ready(true);
    -printer_ready(false).


// Print function after repairing the printer (for Agent)
+!print(Agent)[source(self)]
    : printer_ready(true)
   <-  -printer_ready(true);
    +printer_ready(false);
    .print("Printing again for ", Agent, "...");
    print;
    !print_success(Agent);
    +printer_ready(true);
    -printer_ready(false).


// Print function when the printer is not ready - add to queue
+print(Agent)[source(SenderAgent)]
    : printer_ready(false)
    <- .print("Agent ", Agent, " is in the queue.");
    +printer_ready(false);
    -in_queue(Agent);
    .wait(5000);
    !in_queue(Agent, SenderAgent).


// Being in the queue: trying to print again
+!in_queue(Agent, SenderAgent)
    : true
    <-.print(Agent,"is trying to print again."); 
    -print(Agent)[source(SenderAgent)];
   +print(Agent)[source(SenderAgent)].


// When error occurs
+!print_success(Agent)
    : error(true)
   <- .print("Printing failed for ", Agent);
      !notify_mainframe_error(Agent).


// When printing is successful
+!print_success(Agent)
    : error(false)
   <- .print("Printing successful for ", Agent);
   .send(mainframe, tell, done(Agent)).

// From the java file if error occurs
+printer_error
    : true
   <- +error(true).

// Notifying the mainframe about the error
+!notify_mainframe_error(Agent)
    : true
   <- .print("Notifying mainframe about error for ", Agent);
   .send(mainframe, tell, error(Agent)).


// Repairing the printer
+repair[source(Agent)]
    : true
   <- -error(true);
    .print("Javítanak:)");
   .print("Printer repaired.");
    !print(Agent);
   +error(false);
   gotrepaired;
    .send(mainframe, tell, printer_ready).


// Reporting location
+report_location
   <- 
       get_location;
      -report_location.

// Sending the printer's location to the mainframe
+location(X, Y)
   <- 
      .send(mainframe, tell, location(X, Y));
      .print("Location sent to mainframe: ", X, ", ", Y);
      -location(_,_).