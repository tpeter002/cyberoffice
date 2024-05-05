// Agent mainframe in project cyberoffice

/* Initial beliefs and rules */

/* Initial goals */

!inform_room_status.

/* Plans */

+report_room_change[source(S), from(F), to(T)] : true
	<- 
	// TODO: here we need some kind of memory of who is where, 
	// I need to know the syntax better for this, but if "isin(R, A)" works then that's great 
	   

+!inform_room_status : room_empty(R)
	<- .send(vacuum_cleaner, tell, room_empty(R)).
	// TODO: tell everyone else who needs this info too



MAS vacuum_cleaner_system {

	environment:
		VacuumCleanerEnv

	agents:
		vacuum_cleaner;
		mainframe;

}



// human should fix printer and printer should tell mainframe

+printer_error : true
	<- .send(human, tell, go_fix_printer).

+fixed_printer : true
	<- .-printer_error;
	   -fixed_printer.



/* TODO: get occupancy of rooms (all agents tell mainframe? mainframe asks? idk how this works as of now)
 * TODO: interop with other agents
 * 
 * also, wtf is MAS
 */