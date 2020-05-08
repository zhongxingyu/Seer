 public class Nurse extends User
 {
 
     public Nurse(String setName, int setPin)
     {
         super(setName, setPin);
        userType = "Nurse";
 
     }
 
     public Visit newVisit(int patientID)
     {
         Visit _newVisit = new Visit();
         return _newVisit;
 
     }
 
     public User createNewAccount(String _userName, String userType, int setPin)
     {
         if (userType == "Nurse")
         {
             Nurse newNurse = new Nurse(_userName, setPin);
             return newNurse;
 
         }
         if (userType == "Doctor")
         {
            Doctor newDoctor = new Doctor(_userName, setPin);
             return newDoctor;
         }
         if (userType == "Patient")
         {
             Patient newPatient= new Patient(_userName, setPin);
             return newPatient;
         }
         return null;
     }
 
 
 }
