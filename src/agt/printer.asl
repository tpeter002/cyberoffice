// Initial beliefs
printer_ready(true).
error(false).


// Plans
+print(Agent)[source(SenderAgent)]
    : printer_ready(true)
   <-  -printer_ready(true);
    +printer_ready(false);
    .print("Printing for ", Agent, "...");
    print;
    !print_success(Agent, SenderAgent);
    +printer_ready(true);
    -printer_ready(false).
    

+print(Agent)[source(SenderAgent)]
    : printer_ready(false)
    <- .print("Agent ", Agent, " is in the queue.");
    +printer_ready(false);
    -in_queue(Agent);
    .wait(6000);
    !in_queue(Agent, SenderAgent).

+!in_queue(Agent, SenderAgent)
    : true
    <-.print(Agent,"is trying to print again."); 
    -print(Agent)[source(SenderAgent)];
   +print(Agent)[source(SenderAgent)].

+!print_success(Agent, SenderAgent)
    : error(true)
   <- .print("Printing failed for ", Agent);
      !notify_mainframe_error(Agent, SenderAgent).

+!print_success(Agent, SenderAgent)
    : error(false)
   <- .print("Printing successful for ", Agent);
   .send(dummymainframe, tell, printer_done(Agent)).

+printer_error
    : true
   <- +error(true).

+!notify_mainframe_error(Agent, SenderAgent)
    : true
   <- .print("Notifying mainframe about error for ", Agent);
   .send(SenderAgent, tell, printer_error(Agent)).

+!notify_mainframe_ready(Agent, SenderAgent)
    : true
   <- .send(mainframe, tell, printer_ready(SenderAgent)).

+repair[source(Agent)]
    : true
   <- -error(true);
   .print("Printer repaired.");
   +error(false).