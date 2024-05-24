/* Initial goals */
!start.
searching_for_empty_room.
at_room(2).
at_room_start(2).

first_belief.

// When starting send a message to the mainframe
+!start
   <-
      .print("I'm ready to go!");
      .send(mainframe, tell, vacuum_ready).

+!check(Room)
    : not error & not slot_has_garbage & not at_room_end(Room) & not people_in_current_room
   <- 
      next_slot;
      .wait(100);
      !check(Room).

+!check(Room)
    : not error & slot_has_garbage & not at_room_end(Room) & not people_in_current_room
   <- 
      pick_garbage;
      .print("Trash detected, picking it up.");
      .wait(500);
      !check(Room).

// LETS GO TO OTHER ROOM
+!check(Room)
    : not error & at_room_end(Room)
   <-  
      -room_empty(_)[source(Mainframe)];
      .send(mainframe, tell, vacuum_finished_room(Room));
      +searching_for_empty_room.

/* people in the room while checking */
+!check(Room)
    : not error & people_in_current_room & not at_room_start(Room)
   <- 
      go_to_start(Room);
      .print("Human in room, cleaning permission temporary removed.");
      .wait(100);
      !check(Room).

+!check(Room)
    : not error & people_in_current_room & at_room_start(Room)
   <- 
      .wait(1000);
      check_room_empty;
      !check(Room).

/* error while checking */
+!check(Room)
    : error
   <- 
      .print("I'm broken:(");
      .wait(1000);
      !check(Room).

// Maybe I should ask the mainframe for an empty room not him informing me of an empty room
+room_empty(Room)[source(Mainframe)]
   : searching_for_empty_room // if i dont have any other percept of a room being empty
   <- -searching_for_empty_room;
      .print("I recevied an empty room to clean: " , Room);
      +should_clean_room(Room).

+should_clean_room(SelectedRoom)
   : not at_room(SelectedRoom)
   <- 
      -should_clean_room(SelectedRoom);
      !first_round;
      !go_to_other_room(SelectedRoom).

+should_clean_room(SelectedRoom)
   : at_room(SelectedRoom)
   <- 
      -should_clean_room(SelectedRoom);
      !first_round;
      !check(SelectedRoom).

+!go_to_other_room(SelectedRoom)
    : not error & not at_room(SelectedRoom)
   <-    go_to(SelectedRoom);
        .wait(100);
        !go_to_other_room(SelectedRoom).

+!go_to_other_room(SelectedRoom)
    : not error & at_room(SelectedRoom) & not at_room_start(SelectedRoom)
   <-   go_to_start(SelectedRoom);
        .wait(100);
        !go_to_other_room(SelectedRoom).

+!go_to_other_room(SelectedRoom)
    : not error & at_room(SelectedRoom) & at_room_start(SelectedRoom)
   <-   .print("I just arrived at the starting postion, starting to clean");
        .wait(1000);
        +should_clean_room(SelectedRoom).


+!first_round
   : first_belief
   <- 
      -first_belief;
      -at_room(2);
      -at_room_start(2);
      .print("I just cleared my inital beliefs").

+!first_round
   : not first_belief
   <- 
      .print("").

/* Error */
+error
   <-
      .send(mainframe, tell, error).

+report_location
   <- 
      get_location; // java code
      -report_location[source(mainframe)].

+location(X, Y)
   <- 
      .send(mainframe, tell, location(X, Y));
      -location(_,_)[source(percept)].

+repair[source(Human)]
   <- 
      -error;
      fix; // java code
      .print("I'm ready to go once again!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      .send(mainframe, tell, vacuum_ready);
      -repair[source(Human)].