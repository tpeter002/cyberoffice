+print[source(Agent)]
    : true
   <-  .print("szolok a nyomtatonak hogy printeljen");
        .send(printer, tell, print(Agent)).

+printer_done(Agent)
    : true
   <-  .print("a nyomtato jelezte hogy vegzett " , Agent ," szamara").

+printer_error(Agent)
    : true
   <-  .print("a nyomtato jelezte hogy hiba tortent " , Agent ," szamara");
        !printer_repair(Agent).

+!printer_repair(Agent)
    : true
   <-  .print("a nyomtato javitasra kerul " , Agent ," reszerol");
        .send(Agent, tell, printer_repair).
