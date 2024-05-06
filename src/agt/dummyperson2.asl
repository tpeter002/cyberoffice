
!start.

+!start : true <- 
    .send(dummymainframe, tell, print);
    .print("szolok mainframenek hogy nyomtatni akarok").

+printer_error[source(printer)] : true <- 
    .print("tudom, hogy a printer elromlott").

+printer_repair[source(Agent)] : true <- 
    .print("javitom a printert");
    .send(printer,tell,repair).