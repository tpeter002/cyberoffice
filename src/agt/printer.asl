// Initial beliefs
printer_ready(true).
error(false).


// Plans

// print function when the printer is ready
+print(Agent)[source(SenderAgent)]
    : printer_ready(true)
   <-  -printer_ready(true);
    +printer_ready(false);
    .print("Printing for ", Agent, "...");
    print;
    !print_success(Agent, SenderAgent);
    +printer_ready(true);
    -printer_ready(false).
    
// print function when the printer is not ready
+print(Agent)[source(SenderAgent)]
    : printer_ready(false)
    <- .print("Agent ", Agent, " is in the queue.");
    +printer_ready(false);
    -in_queue(Agent);
    .wait(5000);
    !in_queue(Agent, SenderAgent).

// plan for being in the queue
+!in_queue(Agent, SenderAgent)
    : true
    <-.print(Agent,"is trying to print again."); 
    -print(Agent)[source(SenderAgent)];
   +print(Agent)[source(SenderAgent)].

// plan for "print success" if some error occurs
+!print_success(Agent, SenderAgent)
    : error(true)
   <- .print("Printing failed for ", Agent);
      !notify_mainframe_error(Agent, SenderAgent).

// plan for "print success" if no error occurs
+!print_success(Agent, SenderAgent)
    : error(false)
   <- .print("Printing successful for ", Agent);
   .send(dummymainframe, tell, printer_done(Agent)).

// plan if error occurs
+printer_error
    : true
   <- +error(true).

// plan to notify mainframe about error
+!notify_mainframe_error(Agent, SenderAgent)
    : true
   <- .print("Notifying mainframe about error for ", Agent);
   .send(SenderAgent, tell, printer_error(Agent)).

// plan to notify mainframe about printer ready
+!notify_mainframe_ready(Agent, SenderAgent)
    : true
   <- .send(mainframe, tell, printer_ready(SenderAgent)).

// plan to repair the printer
+repair[source(Agent)]
    : true
   <- -error(true);
   .print("Printer repaired.");
   +error(false).