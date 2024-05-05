// Agent mainframe in project cyberoffice

/* Initial beliefs and rules */

room(room_hall).
room(room_printer).
room(room_vacuum).

human(alice).
human(bob).

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


+!inform_room_status[source(S)]
    <-
        .findall(Room, occupied(Room), OccupiedRooms);
        .findall(Room, room(Room) & not occupied(R), EmptyRooms);
        
        .send(S, tell, occupied_rooms(OccupiedRooms));
        .send(S, tell, empty_rooms(EmptyRooms)).



+error[source(S)]
    <-
        -ready(S);
        
        .findall(Human, human(Human), Humans);
        .length(Humans, Length);
        .random(R);
        RandomIndex = math.floor(R * Length) + 1;
        .nth(RandomIndex, Humans, SelectedHuman);
        
        .send(SelectedHuman, tell, fix(S));
        .print("dispatched ", SelectedHuman, " to fix ", S).

+ready[source(S)] : error(S)
    <- 
        -error(S).



// something with the humans' daily tasks

+ready[source(S)] : not error(S) & human(S)
	<-
		.send(S, tell, send_routine_to_mainframe).