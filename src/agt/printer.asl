// Initial beliefs
printer_ready.
no_error.

// Plans
+!print
    : printer_ready & no_error
   <- .print("Printing...");
      .send(mainframe, tell, printing);
      print;
      .send(mainframe, tell, printed).

+printer_error
    : true
   <- .print("Printer encountered an error!");
      -printer_ready;
      -no_error;
      !notify_mainframe.

+!notify_mainframe
    : true
   <- .send(mainframe, tell, printer_error).

// Actions
+!print <- true.