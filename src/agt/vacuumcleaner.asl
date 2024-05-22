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
      .print("chekkolom a slotokat faszaság");
      .wait(100);
      !check(Room).

+!check(Room)
    : not error & slot_has_garbage & not at_room_end(Room) & not people_in_current_room
   <- 
      pick_garbage;
      .print("hopp egy szemét, felveszem");
      .wait(500);
      !check(Room).

// LETS GO TO OTHER ROOM
// ide kéne egy while amíg nincs üres szoba
+!check(Room)
    : not error & at_room_end(Room)
   <-  // elmegyünk másik szobába
      .print("elértem a szoba végét megyek máshova");
      -room_empty(_)[source(Mainframe)];
      .send(mainframe, tell, vacuum_finished_room(Room));
      +searching_for_empty_room.

/* people in the room while checking */
+!check(Room)
    : not error & people_in_current_room & not at_room_start(Room)
   <- 
      go_to_start(Room);
      .print("ember van a szobában visszamegyek a start pozira várakozni");
      .wait(100);
      !check(Room).

+!check(Room)
    : not error & people_in_current_room & at_room_start(Room)
   <- 
      .print("ember van a szobában várok");
      .wait(1000);
      check_room_empty;
      !check(Room).

/* error while checking */
+!check(Room)
    : error
   <- 
      .print("elromlottam, varom az embert hogy megjavitson");
      .wait(1000);
      !check(Room).

// Maybe I should ask the mainframe for an empty room not him informing me of an empty room
+room_empty(Room)[source(Mainframe)]
   : searching_for_empty_room // if i dont have any other percept of a room being empty
   <- -searching_for_empty_room;
      .print("Megkaptam: " , Room);
      +should_clean_room(Room).

+should_clean_room(SelectedRoom)
   : not at_room(SelectedRoom)
   <- .print("SHOULD CLEAN OTHER ROOM_: ", SelectedRoom);
      -should_clean_room(SelectedRoom);
      !first_round;
      !go_to_other_room(SelectedRoom).

+should_clean_room(SelectedRoom)
   : at_room(SelectedRoom)
   <- .print("SHOULD CLEAN THIS FUCKING ROOM_: ", SelectedRoom);
      -should_clean_room(SelectedRoom);
      !first_round;
      !check(SelectedRoom).

+!go_to_other_room(SelectedRoom)
    : not error & not at_room(SelectedRoom)
   <-    go_to(SelectedRoom);
        .print("Megyek a másik szobába takkerolni");
        .wait(100);
        !go_to_other_room(SelectedRoom).

+!go_to_other_room(SelectedRoom)
    : not error & at_room(SelectedRoom) & not at_room_start(SelectedRoom)
   <-   go_to_start(SelectedRoom);
        .print("Megyek a szobában a kezdő pozira");
        .wait(100);
        !go_to_other_room(SelectedRoom).

+!go_to_other_room(SelectedRoom)
    : not error & at_room(SelectedRoom) & at_room_start(SelectedRoom)
   <-   .print("Itt vagyok a másik szoba startjában és nincs itt senki, akkor nyomom");
        .wait(1000);
        +should_clean_room(SelectedRoom).


+!first_round
   : first_belief
   <- 
      -first_belief;
      -at_room(2);
      -at_room_start(2);
      .print("cleareltem a beliefjeim").

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
      -report_location.

+location(X, Y)
   <- 
      .send(mainframe, tell, location(X, Y));
      -location(_,_).

+repair
   <- 
      -repair;
      fix; // java code
      .print("I'm ready to go once again!");
      .send(mainframe, tell, vacuum_ready).