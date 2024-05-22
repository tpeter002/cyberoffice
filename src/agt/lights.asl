
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
  <-  .print("Lights Agent is operating...");
      operate;
      .wait(1000);
      !operating.

+light_broken 
  <-  .print("Light is broken in room: ");
      .send(mainframe, tell, error).

+light_repaired 
  <-  .print("Light is repaired in room: ");
      .send(mainframe, tell, light_ready).

+person_in_room 
  <-  .print("Person detected in room");
      turn_light_on.

+location(X,Y) 
  <-  .send(mainframe, tell, location(X,Y));
      //.print("Location is: " + X + " " + Y);
      -location(_,_).

+report_location 
  <-  get_location.


//empty room be és fel kapcsolas pls
+empty_room 
  <-  .print("No Person detected in room: ");
      turn_light_off.





//+person_in_room 
//  <-  .print("Person detected in room");
//      !turn_on_lights().
//
//-person_in_room 
//  <-  .print("No Person detected in room: ");
//      !turn_off_lights.
//
//+!turn_on_lights : not is_light_on
//  <-  turn_light_on;
//      .print("Turning on lights in ").
//      
//      .send(mainframe, tell, lights_on(location)).
//
//+!turn_off_lights : is_light_on
//  <-  turn_light_off;
//      .print("Turning off lights in ").
//      .send(mainframe, tell, lights_off(location)).

