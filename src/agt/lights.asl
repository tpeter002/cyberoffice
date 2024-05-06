!initialize.

+!initialize : not initialized 
  <-  .print("Initializing Lights Agent...");
      +initialized.

+person_in_room 
  <-  .print("Person detected in room");
      !turn_on_lights().

-person_in_room 
  <-  .print("No Person detected in room: ");
      !turn_off_lights.

+!turn_on_lights : not is_light_on
  <-  turn_light_on;
      .print("Turning on lights in ").
      
//      .send(mainframe, tell, lights_on(location)).

+!turn_off_lights : is_light_on
  <-  turn_light_off;
      .print("Turning off lights in ").
//      .send(mainframe, tell, lights_off(location)).