// Agent vacuumcleaner in project 

/* Initial beliefs and rules */
at(room(1)).
last_room(room(3)).

/* Initial goals */

!clean_rooms.

/* Plans */

+!clean_rooms : room_empty(R) & garbage(R)
   <- !clean_room(R);
      !move_to_next_room;
      !clean_rooms.

+!clean_rooms : not room_empty(_)
   <- !wait_for_empty_room;
      !clean_rooms.

+!clean_room(R) : garbage(R)
   <- !pick_garbage(R);
      .print("Cleaning in room ", R);
      !clean_room(R).
+!clean_room(_).

+!move_to_next_room : at(room(R1)) & not last_room(R1)
   <- ?next_room(R1,R2);
      .print("Moving to room ", R2);
      !move_to(R2).
+!move_to_next_room.

+!wait_for_empty_room : not room_empty(_)
   <- .wait(1000);
      .print("Waiting for room to be empty...");
      !wait_for_empty_room.
+!wait_for_empty_room.

+!pick_garbage(R) : garbage(R)
   <- pick(garb);
      !pick_garbage(R).
+!pick_garbage(_).

+!move_to(R) : not at(room(R))
   <- move_to_room(R);
      !move_to(R).
+!move_to(_).

+!move_to_next_room : at(room(R1)) & last_room(room(R1))
   <- .print("Finished cleaning all rooms.").
   // TODO:  move to charger room and recharge vacuum cleaner

+!repair : broken
   <- .print("Vacuum cleaner is broken. Repairing...");
      .wait(2000);
      -broken.
