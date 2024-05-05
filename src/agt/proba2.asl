
!start.

+!start : true <- 
    .send(printer, tell, print);
    .print("szolok neki h printeljen2").

+printer_error[source(printer)] : true <- 
    .print("tudom, hogy a printer elromlott2").