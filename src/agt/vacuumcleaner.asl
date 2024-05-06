// Agent vacuumcleaner in project 

/* Initial beliefs */
//at(P) :- pos(P,X,Y) & pos(vc,X,Y).
//home(P) :- pos(P,X,Y) & pos(vc,X,Y).

/* Initial goals */
!check.

/* Plans */

+should_go_home
   <- 
      !go_home.

+!go_home
   :  not at_home & not error
   <- 
      move_home;
      .wait(1000);
      !go_home.

+!go_home
   :  at_home & not error
   <- 
      empty_garbage;
      recharge_battery;
      .wait(5000);
      !check.

+!check
   :  not slot_has_garbage & not should_go_home & current_room_empty & not error
   <- 
      next_slot;
      .wait(100);
      !check.

+!check
   :  not slot_has_garbage & not should_go_home & not current_room_empty & not error
   <- 
      .print("Going home because they see me rollin'...");
      +should_go_home.

+slot_has_garbage
   :  not should_go_home
   <- 
      pick_garbage;
      !check.

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
      .send(mainframe, tell, location(X, Y);
      -location(_,_).

+fixed
   <- 
      -fixed;
      fix;
      .print("I'm ready to go once again!");
      .send(mainframe, tell, vacuum_ready);
      +should_go_home.

// TODO: error sometimes
