// Agent mainframe in project cyberoffice

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

// Everyone reports in when they're ready TODO: move these to their respective slots

+light_ready[source(Light)] 
	<-	
		+light(Light);
		-light_ready.



/* GENERIC 
 * used by any agent in the right circumstance, it is detailed in each agents' own section
 */

// Report the completion of a request from a requester
+done(Requester)[source(Source)]
	<-  
		.print("recieved 'done' from ", Source, ", forwarding to ", Requester);
		.send(Requester, tell, done(Source));

		-done[source(Source)].

// Report an error that happened during a request from a requester
+error(Requester)[source(Source)]
	<-  
		.print("recieved 'error' from ", Source, ", forwarding to ", Requester);
		.send(Requester, tell, error(Source));
		
		!private_fix_error(Source, Requester).

// Report an error that happened randomly
+error[source(Source)]
    <-	
		findall(Human, human(Human), Humans);
        .length(Humans, Length);
        .random(R);
        RandomIndex = math.floor(R * Length) + 1;
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

// Call if you want to print
+print[source(Requester)]
	<-	
		.findall(Printer, printer(Printer), Printers);
	    .length(Printers, Length);
        .random(R);
        RandomIndex = math.floor(R * Length) + 1;
        .nth(RandomIndex, Printers, SelectedPrinter);

		!private_print(SelectedPrinter, Requester);

		-print[source(Requester)].



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

+room_empty(Room)
	<-	
		.findall(Vacuum, vacuum(Vacuum), Vacuums);
	    .length(Vacuums, Length);
        .random(R);
        RandomIndex = math.floor(R * Length) + 1;
        .nth(RandomIndex, Vacuums, SelectedVacuum).

		// TODO: add this to java




/* LIGHT
 *
 */









/* private helper functions */

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
		.print("asking ", Requester, " to fix ", Errorer);
		.send(Requester, tell, go_fix(Errorer)).



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