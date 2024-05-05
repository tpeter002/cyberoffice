// Agent mainframe in project cyberoffice

/* Initial beliefs and rules */

room(room_hall).
room(room_printer).
room(room_vacuum).

/* Initial goals */

/* Plans */

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


+!inform_room_status[source(Source)]
    <-
        .findall(Room, occupied(Room), OccupiedRooms);
        .findall(Room, room(Room) & not occupied(Room), EmptyRooms);
        
        .send(Source, tell, occupied_rooms(OccupiedRooms));
        .send(Source, tell, empty_rooms(EmptyRooms)).


MAS vacuum_cleaner_system {

	environment:
		VacuumCleanerEnv

	agents:
		vacuum_cleaner;
		mainframe;

}



// human should fix printer and printer should tell mainframe

+printer_error : true
	<-
		.send(human, tell, go_fix_printer).

+fixed_printer : true
	<- 
		-printer_error;
		-fixed_printer.