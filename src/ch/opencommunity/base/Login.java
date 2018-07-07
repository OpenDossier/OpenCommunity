package ch.opencommunity.base;

public class Login extends BasicOCObject{

	public Login(){
		setTablename("Login");
		addProperty("Username", "String", "", false, "Benutzername", 100);
		addProperty("Password", "String", "", false, "Passwort", 12);
		addProperty("RecoveryPassword", "String", "", false, "Passwort", 12);
		addProperty("RegistrationCode", "String", "", false, "Registrationscode", 20);
	}

}
