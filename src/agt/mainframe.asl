// Agent mainframe in project cyberoffice

/* GENERIC 
 * used by any agent in the right circumstance, it is detailed in each agents' own section
 */

// Report the completion of a request from a requester
+done(Requester)[source(Source)]
	<-  
		.print("recieved 'done' from ", Source, ", forwarding to ", Requester);
		.send(Requester, tell, done(Source));
		.print("Done for ", Requester);
		-done[source(Source)].

// Report an error that happened during a request from a requester
+error(Requester)[source(Source)]
	<-  
		.print("recieved 'error' from ", Source, ", forwarding to ", Requester);
		.send(Requester, tell, error(Source));
		.print("Error for ", Requester);
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



// Call if you want to print, you'll either get a done(Printer) or an error(Printer) in return
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

+print[source(Requester)]
	:	not printer(_)
	<-	
		-print[source(Requester)];
		.wait(1000);
		+print[source(Requester)].



/* PRINTER
 * you get a print(Requester) and should return done(Requester) or error(Requester)
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
 *
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

// Sent every time to 
+empty(Room)
	:	vacuum(_)
	<-	
		.findall(Vacuum, vacuum(Vacuum), Vacuums);
		.length(Vacuums, Length);
		.random(R);
		RandomIndex = math.floor(R * Length);
		.nth(RandomIndex, Vacuums, SelectedVacuum);

		.send(SelectedVacuum, tell, empty(Room));

		-empty(Room).



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



/*
+!find_everyone
	<-
		.findall(Agent, .my_name(Agent), Agents);
		.send(Agents, askOne, location(Agent, Room), Locations);
		
		.abolish(occupied(_));
		.abolish(location(_,_));

		for (.member(location(Agent, Room), Locations)) {
			.count(location(_, Room, Locations, Count);
			if (Count > 0) {
				+occupied(Room);    
			}
			+location(Agent, Room);
		}.

+!inform_room_status[source(S)]
	<-
		.findall(Room, occupied(Room), OccupiedRooms);
		.findall(Room, room(Room) & not occupied(R), EmptyRooms);
		
		.send(S, tell, occupied_rooms(OccupiedRooms));
		.send(S, tell, empty_rooms(EmptyRooms)).
*/