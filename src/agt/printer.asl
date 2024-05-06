// Initial beliefs
printer_ready(true).
error(false).


// Plans


// notify the mainframe that the printer is ready
+error(false)
    : true
    <- .send(mainframe, tell, printer_ready);
        .print("Printer ready.").



// print function for Agent sent by SenderAgent (mainframe)
+print(Agent)[source(SenderAgent)]
    : printer_ready(true)
   <-  -printer_ready(true);
    +printer_ready(false);
    .print("Printing for ", Agent, "...");
    print;
    !print_success(Agent, SenderAgent);
    +printer_ready(true);
    -printer_ready(false).


// print function after the error (printing for the human who repaired it, alias Agent)
+!print(Agent)[source(self)]
    : printer_ready(true)
   <-  -printer_ready(true);
    +printer_ready(false);
    .print("Printing again for ", Agent, "...");
    print;
    !print_success(Agent, SenderAgent);
    +printer_ready(true);
    -printer_ready(false).
    
// When the printer is not ready, Agent will go in the queue
+print(Agent)[source(SenderAgent)]
    : printer_ready(false)
    <- .print("Agent ", Agent, " is in the queue.");
    +printer_ready(false);
    -in_queue(Agent);
    .wait(5000);
    !in_queue(Agent, SenderAgent).


// In the queue, Agent will try to print again
+!in_queue(Agent, SenderAgent)
    : true
    <-.print(Agent,"is trying to print again."); 
    -print(Agent)[source(SenderAgent)];
   +print(Agent)[source(SenderAgent)].

// Successful printing
+!print_success(Agent, SenderAgent)
    : error(false)
   <- .print("Printing successful for ", Agent);
   .send(mainframe, tell, done(Agent)).


// Error while printing
+!print_success(Agent, SenderAgent)
    : error(true)
   <- .print("Printing failed for ", Agent);
      !notify_mainframe_error(Agent, SenderAgent).


// Notifying the mainframe about the error
+!notify_mainframe_error(Agent, SenderAgent)
    : true
   <- .print("Notifying mainframe about error for ", Agent);
   .send(SenderAgent, tell, error(Agent)).


// Notifying the mainframe that the printer is ready
+!notify_mainframe_ready(Agent, SenderAgent)
    : true
   <- .send(mainframe, tell, printer_ready).


// Repairing the printer
+repair[source(Agent)]
    : true
   <- -error(true);
   .print("Printer repaired.");
    !print(Agent);
   +error(false);
   .send(mainframe, tell, printer_ready).