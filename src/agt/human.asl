// Agent human in project 

/* Initial beliefs and rules */

at(P): - pos(P,Xo,Yo).
/* Initial goals */

!start. // initial goal to rutin
!pause. // initial goal to break
!szemetel.


+!start: true <- !move(0, 2).
//-!interact <- !interact.

+!doneprinting(X, Y): true <- !move(X, Y).
+!move(X, Y): not pos(P,X,Y) <- moveto(X, Y); !move(X,Y).
+!move(X,Y): pos(P,X,Y) <- wait(5000); !randommove().

+!randommove(): true <- ra
//!interact(X).





+!csill: true <-
.suspend(interact);
.wait(10000);
.resume(interact).


+!szemetel: true <- 
    .random(A);
    .wait(10000+A*5000);     
    .print("szemeteltem oriasit :)");
    garbagedrop();
     !szemetel.



//ez az hogy megall neha barhol
+!pause: true <- .wait(2000);     // suspend this intention (the pause) for 2 seconds
     .print("alldigalok");
     .suspend(interact);
     .wait(1000);     // suspend this intention again for 1 second
     .resume(interact);
     .print("csak megyek megyek");
     !pause.