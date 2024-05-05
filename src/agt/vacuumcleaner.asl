// Agent vacuumcleaner in project 

/* Initial beliefs */
at(P) :- pos(P,X,Y) & pos(vc,X,Y).
at(home):- pos(home,0,0) & pos(vc,0,0).

/* Initial goals */
!check(slots).
!ensure_pick(G).

/* Plans */

+!check(slots): not garbage(vc) & not recharge(vc)
   <- next(slot);
      .wait(1000);
      .print("Checking next slot...");
      !check(slots).
+!check(slots).

+garbage(vc) : not .desire(destroy(garb))
   <- !destroy(garb).

+recharge(vc): not .desire(recharge)
   <- !recharge.

+!recharge
   <- -+pos(home,0,0);
      !gohome(home);
      .print("Recharging the vacuum cleaner").
      
+!destroy(G)
   <- !ensure_pick(G);
      .print("Dostroy the world");
      !check(slots).

+!ensure_pick(G) : garbage(vc)
   <- pick(garb);
      !ensure_pick(G).
+!ensure_pick(_).

+!at(L) : at(L)
   <- .print("I'm at ",L).

+!gohome(L) 
   <- 
      ?pos(L,X,Y);
      .wait(1000);     
      recharge_route;
      !gohome(L).


/* Initial beliefs and rules 
at(room(1)).
last_room(room(3)).
battery_level(100).
low_battery_threshold(20).
charger_location(room(0)).
garbage_bin_location(room(0)).
*/


/* Initial beliefs and rules 
at(P) :- pos(P,X,Y) & pos(vc,X,Y).  // Initial position of the vacuum cleaner
last_pos(X, Y).  // Last position visited
battery_level(100).
low_battery_threshold(20).
charger_location(CX, CY).
garbage_bin_location(BX, BY).
current_room(R).  // Current room the vacuum cleaner is in

*/
/* Initial goals 

!clean_rooms.
*/
/* Plans 

+!clean_rooms : current_room(R) & room_empty(R) & garbage_in_room(R) & battery_level(B) & B >= low_battery_threshold(T)
   <- !clean_room(R);
      !move_to_next_room;
      !clean_rooms.

+!clean_rooms : current_room(,R) & room_empty(R) & garbage_in_room(R) & battery_level(B) & B < low_battery_threshold(T)
   <- !recharge;
      !clean_rooms.

+!clean_rooms : not room_empty(_)
   <- !wait_for_empty_room;
      !clean_rooms.

+!clean_room(R) : garbage_in_room(R)
   <- !pick_garbage(R);
      .print("Cleaning in room ", R);
      !clean_room(R).
+!clean_room(_).

//TODO: CSutitol kerni infot h menjek masik szobaba
+!move_to_next_room : at(room(R1)) & not last_room(R1)
   <- ?next_room(R1,R2);
      .print("Moving to room ", R2);
      !move_to(R2).
+!move_to_next_room.

*/

/*

+!wait_for_empty_room(R) : not room_empty(R) & current_room(R)
   <- .wait(1000);
      .print("Waiting for room to be empty...", R);
      !wait_for_empty_room.
+!wait_for_empty_room.

+!pick_garbage(R) : garbage(R) & current_room(R)
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

+!recharge : at(room(R)) & charger_location(C) & R \== C
   <- .print("Low battery. Moving to charger room ", C);
      !move_to(C);
      !recharge.

+!recharge : at(room(R)) & charger_location(R)
   <- .print("Recharging battery...");
      .wait(5000);
      +battery_level(100);
      .print("Battery fully charged.").

+!pick_garbage(R) : garbage(R) & not vacuum_full
   <- pick(garb);
      !pick_garbage(R).

+!pick_garbage(R) : garbage(R) & vacuum_full
   <- .print("Vacuum cleaner is full. Emptying...");
      !empty_vacuum;
      !pick_garbage(R).

+!empty_vacuum : at(room(R)) & garbage_bin_location(B) & R \== B
   <- !move_to(B);
      !empty_vacuum.

+!empty_vacuum : at(room(R)) & garbage_bin_location(R)
   <- .print("Emptying vacuum cleaner...");
      .wait(2000);
      -vacuum_full.

*/