// Agent human in project

/* Initial beliefs and rules */
working.
/* Initial goals */
!csill.
!ready.
!loadinitialpos. 
!szemetel.

+!ready: true <- .send(mainframe, tell, human_ready).
+!loadinitialpos: true <-  loadpos; load.


+go_fix(Errorer, Xe, Ye) : true <-  .print("I got a fix"); !move.


+done(Source, Xd, Yd): true <- .print("I got a done"); !move.


+print: working <- 
    .send(mainframe, tell, print);

    .print("I hereby reserve the printer for myself");

    -print[source(_)].

+move(X, Y) : working & not target(X, Y) <-
    -move(X, Y)[source(_)];
    +target(X, Y);
    !move.


//elerte javitast
+!move: go_fix(Errorer,Xe,Ye) & working & (pos(Xe, Ye) | adjacent(Xe, Ye)) <-
    .print("I reached the fixing position: ", Xe,", ", Ye);
    clear(go_fix(Errorer,Xe,Ye)[source(_)]);
    .send(Errorer, tell, repair);
    -go_fix(Errorer, Xe, Ye)[source(_)];
    !move.

//javitani megy
+!move: go_fix(Errorer,Xe,Ye) & working & not pos(Xe, Ye) & not adjacent(Xe, Ye) <-
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
    load;
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
+!move : not go_fix(_,_,_) & not done(_,_,_) & target(Xt, Yt) & pos(Xt, Yt) <-
    .print("I have reached the target position (", Xt, ",", Yt, ")");
    -target(Xt, Yt)[source(_)];
    .wait(10000);
    load.

//A rutin céljához megy
+!move : not go_fix(_,_,_) & not done(_,_,_) & working & target(Xt, Yt) & not pos(Xt, Yt)<- 

    moveto(Xt, Yt);
    loadpos;
    .random(A);
    .wait(500 + A * 1000);
    !move.



+!move: not go_fix(_,_,_) & not target(_,_) & not done(_,_,_) <- 
    .wait(1000);
    !move.



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
    garbagedrop;
     !szemetel.

