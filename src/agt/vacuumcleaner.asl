// Agent vacuumcleaner in project 

/* Initial beliefs */
//at(P) :- pos(P,X,Y) & pos(vc,X,Y).
//home(P) :- pos(P,X,Y) & pos(vc,X,Y).

/* Initial goals */
!start.

/* Plans */

+!start
   <-
      .print("I'm ready to go!");
      .send(mainframe, tell, vacuum_ready);
      !check.

+should_go_home
   <- 
      !go_home.

+!go_home
   :  not at_home & not error & not fast_go_home
   <- 
      move_home;
      .wait(1000);
      !go_home.

+!go_home
   :  not at_home & not error & fast_go_home
   <- 
      move_home;
      .wait(100);
      !go_home.

+!go_home
   :  at_home & not error
   <- 
      empty_garbage;
      recharge_battery;
      .print("Recharging battery...");
      .wait(2500);
      .print("Emptying garbage...");
      .wait(2500);
      .print("Cleaning again!");
      -should_go_home;
      -fast_go_home;
      !check.

+!check
   :  not slot_has_garbage & not should_go_home & not current_room_has_people & not error
   <- 
      next_slot;
      .wait(100);
      !check.

+!check
   :  not slot_has_garbage & not should_go_home & current_room_has_people & not error
   <- 
      .print("Going home because they see me rollin'...");
      +fast_go_home;
      +should_go_home.

+!check
   :  slot_has_garbage & not should_go_home & not current_room_has_people & not error
   <- 
      pick_garbage;
      .print("Removed garbage");
      .wait(500);
      !check.
      
+!check.

/* Error */
+error
   <-
      .send(mainframe, tell, error).

+report_location
   <- 
      get_location;
      -report_location.

+location(X, Y)
   <- 
      .send(mainframe, tell, location(X, Y));
      -location(_,_).

+repair
   <- 
      -repair;
      fix;
      .print("I'm ready to go once again!");
      .send(mainframe, tell, vacuum_ready);
      +should_go_home.

// TODO: error sometimes
