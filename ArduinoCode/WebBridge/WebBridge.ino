
void setup() {
  Serial.begin(9600);
}

void loop() {
  
   digitalWrite(LED_BUILTIN, HIGH);   
   delay(1000);                       
   digitalWrite(LED_BUILTIN, LOW);    
   delay(1000);   

   // URL urlid;ID 23;LEN 3434;payload buffer\n
   const char* msg = "URL 1;ID 5456;LEN 22;SOME PAYLOAD GOES HERE\n";
   Serial.print(msg);
  
}
