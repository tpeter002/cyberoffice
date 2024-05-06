
!start.

+!start : true <- 
    .print("szolok mainframenek hogy nyomtatni akarok");
    .wait(1000);
    .send(mainframe, tell, print).
    

+printer_error[source(printer)] : true <- 
    .print("tudom, hogy a printer elromlott").


+printer_repair[source(Agent)] : true <- 
    .print("javitom a printert");
    .send(printer,tell,repair).

+done(Agent)[source(SenderAgent)] : true <- 
    .print("tudom hogy a ", Agent ,"printer nyomtatott nekem").


+error(Agent)[source(sAgent)] : true <- 
    .print("tudom hogy a ", Agent ,"printer nem tudott nyomtatni nekem").

+go_fix(Error)[source(Agent)] : true <- 
    .print("megkert", Agent, "hogy javitsak");
    .send(Error, tell, repair).