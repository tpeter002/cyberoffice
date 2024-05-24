// Agent human in project

/* Initial beliefs and rules */
working.
/* Initial goals */
!csill.
!ready.
!loadinitialpos. 
!szemetel.

+!ready: true <- .send(mainframe, tell, human_ready).
+!loadinitialpos: true <-  load; !nextroutine.

+!nextroutine: true <- load.

+go_fix(Errorer, Xe, Ye) : true <-  .print("kaptam go fixet"); !move.


+done(Source, Xd, Yd): true <- .print("kaptam hogy done"); !move.


+print: working <- 
    .send(mainframe, tell, print);
    .print("EZENNEL EZT A NYOMDAT LEFOGLALOM");
    -print[source(_)].

+move(X, Y) : working & not target(X, Y) <-
    -move(X, Y)[source(_)];
    +target(X, Y);
    !move.


//elerte javitast
+!move: go_fix(Errorer,Xe,Ye) & working & (pos(Xe, Ye) | adjacent(Xe, Ye)) <-
    .print("elertem fixeles pozit: ", Xe,", ", Ye);
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
    .print("!!!!!!!!!!!!!!!!!!I have reached the printing position (", Xd, ",", Yd, ")");
    clear(done(Source, Xd, Yd)[source(_)]);
    -done(Source, Xd, Yd)[source(_)];
    load;
    !move.


//A nyomtató céljához megy
+!move : not go_fix(_,_,_) & working & done(Source, Xd, Yd) & not pos(Xd, Yd) & not adjacent(Xd, Yd) <- 
    //?pos(Xc, Yc);
    .print("megyek a nyomtato fele");
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
    //?pos(Xc, Yc);
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
     .print("fu nagyon be csilleztem elfelejtettem mit kene csinalni :((");
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



