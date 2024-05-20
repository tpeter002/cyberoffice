// Agent mainframe in project cyberoffice

/* GENERIC 
 * used by any agent in the right circumstance, it is detailed in each agents' own section
 */

// Report the completion of a request from a requester
+done(Requester)[source(Source)]
	<-  
		.print("recieved 'done' from ", Source, ", forwarding to ", Requester);
		.send(Source, tell, report_location).
		// this will continue via +location(_,_,_)

// Report an error that happened during a request from a requester
+error(Requester)[source(Source)]
	<-  
		.print("recieved 'error' from ", Source, ", to task from ", Requester);
		//.send(Requester, tell, error(Source)); // we seem to not need this :D

		!private_fix_error(Source, Requester).

// Report an error that happened randomly
+error[source(Source)]
	<-	
		.findall(Human, human(Human), Humans);
		.length(Humans, Length);
		.random(R);
		RandomIndex = math.floor(R * Length);
		.nth(RandomIndex, Humans, SelectedHuman);

		!private_fix_error(Source, SelectedHuman).



/* HUMAN 
 * 
 */

// Please send this signal on startup
+human_ready[source(Human)] 
	<-	
		+human(Human);
		-human_ready.

+human_chilling[source(Human)]
	<- 
		-human_chilling[source(_)];
		reminder(Human);
		.send(Human, tell, working).

// Call if you want to print, you'll either get a done(Printer,X,Y) or a go_fix(Printer,X,Y) in return
+print[source(Requester)]
	:	printer(_)
	<-	
		.findall(Printer, printer(Printer), Printers);
		.length(Printers, Length);
		.random(R);
		RandomIndex = math.floor(R * Length);
		.nth(RandomIndex, Printers, SelectedPrinter);

		!private_print(SelectedPrinter, Requester);
		-print[source(Requester)].

// Wait for printers to boot up before sending print requests
+print[source(Requester)]
	:	not printer(_)
	<-	
		-print[source(Requester)];
		.wait(1000);
		+print[source(Requester)].



/* PRINTER
 * you get a print(Requester) and should return done(Requester) or error(Requester)
 * please respond to report_location via location(X,Y)
 */

// Please send this signal on startup, and any time when fixed afterwards
+printer_ready[source(Printer)]
	:	not error(Printer)
	<-	
		+printer(Printer);
		-printer_ready.

+printer_ready[source(Printer)]
	:	error(Printer)
	<-	
		-error(Printer);
		-printer_ready.



/* VACUUM 
 * please respond to report_location via location(X,Y)
 */

// Please send this signal on startup, and any time when fixed afterwards
+vacuum_ready[source(Vacuum)]
	:	not error(Vacuum)
	<-	
		+vacuum(Vacuum);
		-vacuum_ready.

+vacuum_ready[source(Vacuum)]
	:	error(Vacuum)
	<-	
		-error(Vacuum);
		-vacuum_ready.

// Sent every time to vacuum when there's an empty room
+room_empty(Room)
	:	vacuum(_)
	<-	
		.findall(Vacuum, vacuum(Vacuum), Vacuums);
		.length(Vacuums, Length);
		.random(R);
		RandomIndex = math.floor(R * Length);
		.nth(RandomIndex, Vacuums, SelectedVacuum);

		.send(SelectedVacuum, tell, room_empty(Room));

		-room_empty(Room).

// Sent every time to vacuum when a room is not empty
+room_not_empty(Room)
	:	vacuum(_)
	<-
		.findall(Vacuum, vacuum(Vacuum), Vacuums);
		for (.member(vacuum(Vacuum), Vacuums)) {
			.send(Vacuum, tell, room_not_empty(Room));
		};

    	-room_not_empty(Room).



/* LIGHT
 *
 */

// Please send this signal on startup, and any time when fixed afterwards
+light_ready[source(Light)]
	:	not error(Light)
	<-	
		+light(Light);
		-light_ready.

+light_ready[source(Light)]
	:	error(Light)
	<-	
		-error(Light);
		-light_ready.



/* private helper "functions" */

+!private_print(Printer, Requester)
	:	not error(Printer)
	<-	
		.print("recieved 'print' from ", Requester, ", forwarding to ", Printer);
		.send(Printer, tell, print(Requester)).

+!private_print(Printer, Requester)
	:	error(Printer)
	<-	
		.print("recieved 'print' from ", Requester, ", but ", Printer, " is non functional");
		!private_fix_error(Printer, Requester).

+!private_fix_error(Errorer, Requester)
	<-	
		+error_in_need_of_fixing(Errorer, Requester);
		.print("asking ", Errorer, " for their location ");
		.send(Errorer, tell, report_location).

// only here to help with the error fixing above
+location(X, Y)[source(Errorer)]
	:	error_in_need_of_fixing(Errorer, _)
	<-
		.findall(Requester, error_in_need_of_fixing(Errorer, Requester), Requesters);

		if (not .empty(Requesters)) {
			.nth(0, Requesters, Requester);
			.print("sending ", Requester, " to fix ", Errorer);
			.send(Requester, tell, go_fix(Errorer, X, Y));
		}.

// report location after done signal so requester can eg pick up the print
+location(X, Y)[source(Source)]
	:	done(_)[source(Source)]
	<-
		.findall(Requester, done(Requester)[source(Source)], Requesters);
		if (not .empty(Requesters)) {
			.nth(0, Requesters, Requester);
			.send(Requester, tell, done(Source, X, Y));
		};
		-done(Requester)[source(Source)].