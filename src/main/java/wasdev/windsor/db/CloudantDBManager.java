package wasdev.windsor.db;

import java.util.Map.Entry;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.org.lightcouch.CouchDbException;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import newwindsorite.db.UserProfileAlreadyExistentException;
import newwindsorite.db.UserProfileNotFoundException;
import wasdev.windsor.resources.UserProfile;
import wasdev.windsor.resources.WindsoriteUtil;

public class CloudantDBManager {

	private CloudantClient cloudant = null;
	private Database db = null;
	private String databaseName = "newwindsorite";
	private static CloudantDBManager singletonCloudant = null;

	/**
	 *    Private constructor for Singleton Pattern
	 */
	private CloudantDBManager() {}
	
	
	/**
	 *     Method to retrieve the SINGLE instance of the Database Manager.
	 * 
	 * @return Returns the singleton reference to the CloudantDBManager  
	 */
	public static CloudantDBManager getInstance() {
		System.out.println("singletonCloudant" + singletonCloudant);
		synchronized (CloudantDBManager.class) {
			if (singletonCloudant == null) {
				singletonCloudant = new CloudantDBManager();
				singletonCloudant.cloudant = createClient();
				try {
					System.out.println("cgoing for creating db connections");
					singletonCloudant.db = singletonCloudant.cloudant.database(singletonCloudant.databaseName, true);
				} catch (Exception e) {
					System.out.println("inside exception of db not found");
					throw new RuntimeException("DB Not found", e);
				}
			}
		}
		
		return singletonCloudant;
	}
	
	
	/**
	 *  Creates CloudantClient to access Cloudant Database  
	 * 
	 * @return
	 */
	private static CloudantClient createClient() {
		// VCAP_SERVICES is a system environment variable
		// Parse it to obtain the NoSQL DB connection info
		String VCAP_SERVICES = System.getenv("VCAP_SERVICES");

		String serviceName = null;
		
		String user = "30c44038-8e2d-49dd-9798-f93d0a9e828d-bluemix";
		String password = "93e9e25f03d5b88071c8bce0c124852eb2c122de2644333ad4073a368c935e6a";


		//uncomment me
//		if (VCAP_SERVICES != null) {
//			// parse the VCAP JSON structure
//			JsonObject obj = (JsonObject) new JsonParser().parse(VCAP_SERVICES);
//			Entry<String, JsonElement> dbEntry = null;
//			Set<Entry<String, JsonElement>> entries = obj.entrySet();
//			// Look for the VCAP key that holds the cloudant no sql db information
//			for (Entry<String, JsonElement> eachEntry : entries) {
//				if (eachEntry.getKey().toLowerCase().contains("cloudant")) {
//					dbEntry = eachEntry;
//					break;
//				}
//			}
//			if (dbEntry == null) {
//				throw new RuntimeException("Could not find cloudantNoSQLDB key in VCAP_SERVICES env variable");
//			}
//
//			obj = (JsonObject) ((JsonArray) dbEntry.getValue()).get(0);
//			serviceName = (String) dbEntry.getKey();
//			System.out.println("Service Name - " + serviceName);
//
//			obj = (JsonObject) obj.get("credentials");
//
//			user = obj.get("username").getAsString();
//			password = obj.get("password").getAsString();
//
//		} else {
//			throw new RuntimeException("VCAP_SERVICES not found");
//		}

		try {
			CloudantClient client = ClientBuilder.account(user)
					.username(user)
					.password(password)
					.build();
			System.out.println("successfull");
			return client;
		} catch (CouchDbException e) {
			throw new RuntimeException("Unable to connect to repository", e);
		}
	}


	/**
	 * 	Searches for a User Profile in the Database based on the UserName
	 * 
	 * @param userName The User Name to search for its profile
	 * @return Returns the User profile if found and if not throws UserProfileNotFoundException 
	 * @throws UserProfileNotFoundException
	 */
	public UserProfile searchUserProfileByUserName(String userName) throws UserProfileNotFoundException {
		UserProfile retrievedUser = null;
		if (userName != null) {
			HashMap<String, Object> foundDocument = null;
			try {
				System.out.println("userName" + userName);
				System.out.println("db*****" + db);
				foundDocument = db.find(HashMap.class, userName);
			} catch (NoDocumentException ex) {
			}
			if (foundDocument != null) {
				retrievedUser = new UserProfile(userName);
				retrievedUser.setName((String)foundDocument.get("Name"));
				retrievedUser.setEmail((String)foundDocument.get("Email"));
				retrievedUser.setAddress((String)foundDocument.get("Address"));
				retrievedUser.setZipCOde((String)foundDocument.get("ZipCode"));
				retrievedUser.setRecomentation1((String)foundDocument.get("Recomentation1"));					
				retrievedUser.setRecomentation2((String)foundDocument.get("Recomentation2"));
				retrievedUser.setRecomentation3((String)foundDocument.get("Recomentation3"));
				retrievedUser.setPasswordHash((String)foundDocument.get("PasswordHash"));
			} else {
				throw new UserProfileNotFoundException("User profile not found");
			}
		}
		return retrievedUser;
	}
	
	
	/**
	 * 
	 *  Inserts a new UserProfile in Database
	 *  If UserName already existent in the Database throws UserProfileAlreadyExistentException
	 * 
	 * @param UserProfile
	 * @throws UserProfileAlreadyExistentException
	 */
	public void insertUserProfile(UserProfile newUserProfile) throws UserProfileAlreadyExistentException {
		HashMap<String, Object> foundDocument = null;
		try {
			foundDocument = db.find(HashMap.class, newUserProfile.getUserName());
		} catch (NoDocumentException ex) {
		}
		
		if (foundDocument == null) {
			Map<String, Object> cloudantDocument = new HashMap<String, Object>();
			cloudantDocument.put("_id", newUserProfile.get_id());
			cloudantDocument.put("UserName", newUserProfile.getUserName());
			cloudantDocument.put("Name", newUserProfile.getName());
			cloudantDocument.put("Email",newUserProfile.getEmail());
			cloudantDocument.put("Address",newUserProfile.getAddress());		
			cloudantDocument.put("ZipCode",newUserProfile.getZipCOde());
			cloudantDocument.put("Recomentation1",newUserProfile.getRecomentation1());
			cloudantDocument.put("Recomentation2",newUserProfile.getRecomentation2());
			cloudantDocument.put("Recomentation3",newUserProfile.getRecomentation3());
			cloudantDocument.put("PasswordHash",newUserProfile.getPasswordHash());
			db.save(cloudantDocument);
		} else {
			throw new UserProfileAlreadyExistentException("User name already used");
		}
	}
	
	/**
	 *  Removes a UserProfile from Database based on UserName
	 *  Throws UserProfileNotFoundException if UserName not found in the database
	 * 
	 * @param userName
	 * @throws UserProfileNotFoundException
	 */
	public void removeUserProfileByUserName(String userName) throws UserProfileNotFoundException {
		if (userName != null) {
			HashMap<String, Object> foundDocument = null;
			try {
				foundDocument = db.find(HashMap.class, userName);
			} catch (NoDocumentException ex) {
			}
			if (foundDocument != null) {
				db.remove(foundDocument);
			} else {
				throw new UserProfileNotFoundException("User not found");
			}
		}
	}
	
	/**
	 * 
	 *  Updates a UserProfile from Database
	 *  Throws UserProfileNotFoundException if UserName not found in the database
	 * 
	 * @param UserProfile User Profile to be Updated
	 * @throws UserProfileNotFoundException
	 */
	public void updateUserProfileByUserName(UserProfile updatedUser) throws UserProfileNotFoundException {
		if (updatedUser != null) {
			HashMap<String, Object> foundDocument = null;
			try {
				System.out.println("updatedUser.getUserName() " + updatedUser.getUserName());
				foundDocument = db.find(HashMap.class, updatedUser.getUserName());
			} catch (NoDocumentException ex) {
			}
			if (foundDocument != null) {
				foundDocument.put("Name", updatedUser.getName());
				foundDocument.put("Email",updatedUser.getEmail());
				foundDocument.put("Address",updatedUser.getAddress());		
				foundDocument.put("ZipCode",updatedUser.getZipCOde());
				foundDocument.put("Recomentation1",updatedUser.getRecomentation1());
				foundDocument.put("Recomentation2",updatedUser.getRecomentation2());
				foundDocument.put("Recomentation3",updatedUser.getRecomentation3());
				db.update(foundDocument);
			} else {
				throw new UserProfileNotFoundException("User profile not found");
			}
		}
	}	
	
	public static void main(String[] arr) throws UserProfileAlreadyExistentException {
		CloudantDBManager obj = CloudantDBManager.getInstance();
		UserProfile u1= new UserProfile("test2");
		u1.setEmail("test@test.cm");
		u1.setName("Clovis");
		u1.setAddress("test test test");
		u1.setPasswordHash(WindsoriteUtil.generateStringHash("test"));
		obj.insertUserProfile(u1);
		
	}
	
}

