
// Initial beliefs
printer_ready.
no_error.

// Plans
+print[source(proba)]
    : printer_ready & no_error
   <- .print("Printing...");
   print.


+printer_error
    : true
   <- .print("Printer encountered an error!");
      -printer_ready;
      -no_error;
      !notify_mainframe.

+!notify_mainframe
    : true
   <- .send(mainframe, tell, printer_error).

