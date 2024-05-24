// Agent human in project

/* Initial beliefs and rules */
working.
/* Initial goals */

!csill.
!ready.
!loadinitialpos. // initial goal to rutin
//!pause. // initial goal to break
!szemetel.


+!ready: true <- .send(mainframe, tell, human_ready).
+!loadinitialpos: true <-  load; !nextroutine.

+!nextroutine: true <- load.

+go_fix(Errorer, Xe, Ye) : true <-  .print("kaptam go fixet"); !move.


+done(Source, Xd, Yd): true <- .print("kaptam hogy done"); !move.

+print: working <- 
.send(mainframe, tell, print);
.print("I hereby reserve the printer for myself");
-print[source(_)];
load.

+move(X, Y) : working & not target(X, Y) <-
    -move(X, Y)[source(_)];
    +target(X, Y);
    !move.

+stay: true <-
.suspend(move);
loadpos;
.print("I'm staying here to work");
.wait(10000);
-stay[source(_)];
.resume(move);
load.

//elerte javitast
+!move: go_fix(Errorer,Xe,Ye) & working & (pos(Xe, Ye) | adjacent(Xe, Ye)) <-
    .print("I reached the fixing position: ", Xe,", ", Ye);
    clear(go_fix(Errorer,Xe,Ye)[source(_)]);
    .send(Errorer, tell, repair);
    -go_fix(Errorer, Xe, Ye)[source(_)];
    !move.

//javitani megy
+!move: go_fix(Errorer,Xe,Ye) & working & not pos(Xe, Ye) & not adjacent(Xe, Ye) <-

    //?pos(Xc, Yc);
    moveto(Xe, Ye);
    loadpos;
    .random(A);
    .wait(500 + A * 1000);
    !move.

//A nyomtató célját elérte
+!move : not go_fix(_,_,_) & done(Source, Xd, Yd) & (pos(Xd, Yd) | adjacent(Xd, Yd)) <-
    .print("I have reached the printing position (", Xd, ",", Yd, ")");
    clear(done(Source, Xd, Yd)[source(_)]);
    -done(Source, Xd, Yd)[source(_)];
    !move.


//A nyomtató céljához megy
+!move : not go_fix(_,_,_) & working & done(Source, Xd, Yd) & not pos(Xd, Yd) & not adjacent(Xd, Yd) <- 
    //?pos(Xc, Yc);
    .print("I'm going toward the printer right now");
    moveto(Xd, Yd);
    loadpos;
    .random(A);
    .wait(500 + A * 1000);
    !move.

 //A rutin célját elérte
+!move : not go_fix(_,_,_) & not done(_,_,_) & target(Xt, Yt) & (pos(Xt, Yt) | adjacent(Xt, Yt)) <-
    .print("I have reached the target position (", Xt, ",", Yt, ")");
    -target(Xt, Yt)[source(_)];
    .wait(10000);
    load.

//A rutin céljához megy
+!move : not go_fix(_,_,_) & not done(_,_,_) & working & target(Xt, Yt) & not pos(Xt, Yt) & not adjacent(Xt, Yt) <- 
    //?pos(Xc, Yc);
    moveto(Xt, Yt);
    loadpos;
    .random(A);
    .wait(500 + A * 1000);
    !move.

+!move : go_fix(_,_,_) <- .print("I don't care").

+!move: not go_fix(_,_,_) & not target(_,_) & not done(_,_,_)  & not stay<- 
.print("I finished my routine :)").

+!csill :
     true <-
     .random(Felejtesifaktor);
     .wait(Felejtesifaktor*10000+5000);
     .print("I'm chilling so hard that I forgot what to do :((");
     -move[source(_)];
     -working;
     -target[source(_)];
     .send(mainframe, tell, human_chilling).
     
+!szemetel : 
     true <- 
    .random(B);
    .wait(10000+B*5000);    
    //.print("szemeteltem oriasit :)");
    garbagedrop;
     !szemetel.


//ez az hogy megall neha barhol
+!pause: true <- 
     .random(B);
     .wait(B*10000);    
     .print("just standing...");
     .suspend(move);
     .wait(B*10000);     
     .resume(move);
     .print("...and going on");
     !pause.

