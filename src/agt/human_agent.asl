// Agent human in project 

/* Initial beliefs and rules */

at(P) :- pos(P,Xo,Yo).
/* Initial goals */

!start. // initial goal to rutin
!pause. // initial goal to break
!szemetel.
working.


+!start :
      true 
      <- loadnextroutine.

+printelj : 
     working 
     <- .print("sikeresen beolvastam egy dolgot AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
     .random(A);
      .wait(A*5000);
     loadnextroutine.

+move(X,Y) : 
     working 
     <- .print("tegyuk fel elmentem", X, Y);
      .random(A);
       .wait(A*5000);
     loadnextroutine.

+!doneprinting(X, Y) : 
     true 
     <- !move(X, Y).
+!move(X, Y) : 
     not pos(P,X,Y) 
     <- moveto(X, Y); 
     !move(X,Y).

+!move(X,Y) : 
     pos(P,X,Y) 
     <- wait(5000);
     !randommove.

//+!randommove: true <- 
//!interact(X).

+!csill :
     true <-
     .suspend(working);
     .wait(10000);
     .resume(working).


+!szemetel : 
     true <- 
    .random(A);
    .wait(10000+A*5000);     
    .print("szemeteltem oriasit :)");
//    garbagedrop;
     !szemetel.



//ez az hogy megall neha barhol
+!pause: true <- 
     .random(B);
     .wait(B*5000);     // suspend this intention (the pause) for 2 seconds
     .print("alldigalok");
     .suspend(working);
     .wait(B*7000);     // suspend this intention again for 1 second
     .resume(working);
     .print("csak megyek megyek");
     !pause.