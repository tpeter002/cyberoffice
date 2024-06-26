
!initialize.
!operating.

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
  <-  .print("Turning on light");
      .send(mainframe, tell, lights_on).

+light_off : true
  <-  .print("Turning off light");
      .send(mainframe, tell, lights_off).

+light_broken : true
  <-  .print("Light is broken");
      .send(mainframe, tell, error).

+repair : true
  <-  .print("Light is repaired");
      repair_light;
      .send(mainframe, tell, light_ready).

+location(X, Y) 
  <-  .send(mainframe, tell, location(X, Y));
      .print("Location has been sent to mainframe");
      -location(_, _).

+report_location 
  <-  get_location;
      .print("Location is reported");
      -report_location.