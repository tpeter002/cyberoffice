// Agent vacuumcleaner in project 

/* Initial beliefs */
at(P) :- pos(P,X,Y) & pos(vc,X,Y).
gohome(P) :- pos(P,X,Y) & pos(vc,X,Y).

/* Initial goals */
!check(slots).
!ensure_pick(G).

/* Plans */

// Going right till its possible to go right, then goes down, and starts going left, REPEATS
// Precondition: - battery not dead
//               - no one is in the room
+!check(slots): not garbage(vc) & not recharge & curr_room_empty(true)
   <- next(slot);
      .wait(100);
      .print("Checking next slot...");
      !check(slots).
+!check(slots).

// Goes home if someone is in the room
// Precondition: - battery not dead
//               - human is in the room
+!check(slots): not garbage(vc) & not recharge & curr_room_empty(false)
   <- !gohome(home);
      .print("Going home because they see me rollin'...");
      !check(slots).
+!check(slots).

// If we detect that the battery is low, then we should desire to recharge it
+recharge: not .desire(home)
   <- !home.

// If we detect that the garbage tank is full we head home
+vacuumFull(vc) : not .desire(home)
   <- !home.

// Goes home to recharge the battery, then goes back to checking slots
+!home
   <- -+pos(home,0,0);
      !gohome(home);
      .print("Recharging the vacuum cleaner");
      !check(slots).

// If we detect a garbage, then we should desire to destroy it
+garbage(vc) : not .desire(destroy(garb))
   <- !destroy(garb).

// Destroys G garbage            
+!destroy(G)
   <- !ensure_pick(G);
      .print("Dostroy the world");
      !check(slots).

// Pick up G garbage, helper for destroy
+!ensure_pick(G) : garbage(vc)
   <- pick(garb);
      !ensure_pick(G).
+!ensure_pick(_).

// When at home, recharge the battery and empty the garbage
+!gohome(L) : gohome(L)
   <- .print("I'm at ",L);
      .wait(1000);
      emptyGarbage;
      recharge.

// Recursive call to go home
+!gohome(L)
   <- ?pos(L,X,Y);
      .wait(1000);     
      recharge_route;
      !gohome(L).

// When getting fixed by human, and goes back to work
+fixed <- 
   .send(mainframe, tell, ready);
   .print("I'm ready to go!");
   !check(slots).
