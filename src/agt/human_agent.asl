// Agent human in project

/* Initial beliefs and rules */
working.
//pos(0,0).
/* Initial goals */

!csill.
!ready.
!loadinitialpos. // initial goal to rutin
//!pause. // initial goal to break
!szemetel.
//go_fix(printer, 19, 0).


+!ready: true <- .send(mainframe, tell, human_ready).
+!loadinitialpos: true <-  load; !nextroutine.

+!nextroutine: true <- load.

+go_fix(Errorer, Xe, Ye) : true <- !fixtarget.

+!fixtarget: working & go_fix(Errorer,Xe,Ye) & not pos(Xe, Ye) & not adjacent(Xe, Ye) <- 
    .suspend(move);
    //?pos(Xc, Yc);
    moveto(Xe, Ye);
    loadpos;
    .random(A);
    .wait(A * 1000);
    !fixtarget.

+!fixtarget: go_fix(Errorer, Xe, Ye) & pos(Xe, Ye) | adjacent(Xe, Ye) <-
    .print("elertem fixeles pozit");
    -go_fix(Errorer, Xe, Ye)[source(_)];
    !move;
    .send(Errorer, tell, repair).

+print: working <- 
.send(mainframe, tell, print);
.print("EZENNEL EZT A NYOMDAT LEFOGLALOM");
-print[source(_)].

+move(X, Y) : working & not target(X, Y) <-
    -move(X, Y)[source(_)];
    +target(X, Y);
    !move.

+!move : not go_fix(_,_,_) & working & target(Xt, Yt) & not pos(Xt, Yt) & not adjacent(Xt, Yt) <- //ez itt szar
    //?pos(Xc, Yc);
    moveto(Xt, Yt);
    loadpos;
    .random(A);
    .wait(A * 1000);
    !move.

+!move : not go_fix(_,_,_) & target(Xt, Yt) & (pos(Xt, Yt) | adjacent(Xt, Yt)) <-
    .print("I have reached the target position (", Xt, ",", Yt, ")");
    -target(Xt, Yt)[source(_)];
    load.


+pos(X, Y) : pos(Xc, Yc) & not (X == Xc & Y == Yc) <-
    -pos(Xc, Yc)[source(_)];
    -adjacent(Xc+1, Yc)[source(_)];
    -adjacent(Xc-1, Yc)[source(_)];
    -adjacent(Xc, Yc+1)[source(_)];
    -adjacent(Xc, Yc-1)[source(_)];
    +adjacent(X+1, Y);
    +adjacent(X-1, Y);
    +adjacent(X, Y+1);
    +adjacent(X, Y-1).
    

+!csill :
     true <-
     .random(Felejtesifaktor);
     .wait(Felejtesifaktor*10000+5000);
     .print("fu nagyon be csilleztem elfelejtettem mit kene csinalni :((");
     -move[source(_)];
     -working;
     -target[source(_)];
     .send(mainframe, tell, human_chilling).
     

+!szemetel : 
     true <- 
    .random(A);
    //.wait(10000+A*5000);    
    .wait(2000); 

    //.print("szemeteltem oriasit :)");

    garbagedrop;
     !szemetel.


//ez az hogy megall neha barhol
+!pause: true <- 
     .random(B);
     .wait(B*10000);    
     .print("alldigalok");
     .suspend(move);
     .wait(B*10000);     
     .resume(move);
     .print("csak megyek megyek");
     !pause.


     /*    
+adjacent(Xt, Yt) : pos(Xc, Yc) & ((math.abs(Xc - Xt) == 1 & math.abs(Yc - Yt) == 0) | (math.abs(Xc - Xt) == 0 & math.abs(Yc - Yt) == 1)) <-
    true.
*/

/*
+printelj: working <- .print("sikeresen beolvastam egy dolgot AAAAAAAAAAAAAAAAAAAAAAAAAAAA"); .random(A); .wait(A*5000); load.

+move(X, Y): not pos(P, X, Y) <- -move(X, Y)[source(percept)]; !move(X, Y).

+!move(X,Y): not pos(P, X, Y) <- moveto(X, Y); .random(A); .wait(A*1000); !move(X, Y).



+!move(X,Y): pos(P, X, Y) <- .print("tortenik itt vagyok sztem"); load.
*/



//+!doneprinting(X, Y): true <- !move(X, Y).
//+!move(X, Y): not pos(P,X,Y) <- moveto(X, Y); !move(X,Y).
//+!move(X,Y): pos(P,X,Y) <- wait(5000); !randommove.


//+!randommove: true <- 
//!interact(X).
