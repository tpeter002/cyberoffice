
!initialize.
!operating.

//light_ready -> message to mainframe that light is ready (Csuti kéri) + hol van
//light_ready -> message to mainframe that light has been repaired (Csuti kéri)

//light_on
//light_off 
//light_repaired -> message to mainframe that light is repaired ()
//light_broken -> message to mainframe that light is broken (Csuti error-t kér és lekezeli)


//Check room if there is a person in the room POLLING?

+!initialize : not initialized 
  <-  .print("Initializing Lights Agent...");
      .send(mainframe, tell, light_ready);
      +initialized.

+!operating : true 
  <-  operate;
      .wait(1000);
      !operating.

+light_on : true
  <-  .print("Turning on lights---------------------------------------------------------yeee mukodik az ASL");
      .send(mainframe, tell, lights_on).

+light_off : true
  <-  .print("Turning off lights---------------------------------------------------------yeee mukodik az ASL");
      .send(mainframe, tell, lights_off).

+light_broken : true
  <-  .print("Light is broken---------------------------------------------------------yeee mukodik az ASL");
      .send(mainframe, tell, error).

+light_repaired : true
  <-  .print("Light is repaired ---------------------------------------------------------yeee mukodik az ASL");
      .send(mainframe, tell, light_ready).

+location(X, Y) 
  <-  .send(mainframe, tell, location(X, Y));
      .print("Location has been sent to mainframe---------------------------------------------------------yeee mukodik az ASL");
      -location(_, _).

+report_location 
  <-  get_location;
      .print("Location is reported---------------------------------------------------------yeee mukodik az ASL");
      -report_location.