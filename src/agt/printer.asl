// Initial beliefs
printer_ready(true).
error(false).


// Plans

+print[source(Agent)]
    : printer_ready(true)
   <-  -printer_ready(true);
    +printer_ready(false);
    .print("Printing for ", Agent, "...");
    print;
    !print_success(Agent);
    +printer_ready(true);
    -printer_ready(false).


+print[source(Agent)]
    : printer_ready(false)
    <- .print("Agent ", Agent, " is in the queue.");
    +printer_ready(false);
    -in_queue(Agent);
    .wait(6000);
    !in_queue(Agent).

+!in_queue(Agent)
    : true
    <-.print(Agent,"is trying to print again."); 
    -print[source(Agent)];
   +print[source(Agent)].

+!print_success(Agent)
    : error(true)
   <- .print("Printing failed for ", Agent);
      !notify_mainframe_error(Agent).

+!print_success(Agent)
    : error(false)
   <- .print("Printing successful for ", Agent).

+printer_error
    : true
   <- +error(true).

+!notify_mainframe_error(Agent)
    : true
   <- .send(Agent, tell, printer_error);
   .print("Notifying mainframe about error for ", Agent).

+!notify_mainframe_ready
    : true
   <- .send(mainframe, tell, printer_ready).