package utils;

import java.net.UnknownHostException;

import org.bson.Document;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import com.mongodb.client.MongoDatabase;


public class MongoConn {
	
	static MongoClient mongoclient;

	static DB db;
	static MongoCollection collBase;
	static MongoCollection collScans;
	static MongoCollection collBoxes;
	
	private static Jongo jongo;
	
	public static void connecter(String base, String scans, String boxes){
		
		

		mongoclient = new MongoClient( "192.168.0.201" , 27017 );
		
	
		db = mongoclient.getDB("LequelFX");
		jongo = new Jongo(db);
		collBase = jongo.getCollection(base);
		collScans = jongo.getCollection(scans);
		collBoxes = jongo.getCollection(boxes);
		
	}

	public static MongoCollection getCollBase() {
		return collBase;
	}

	public static void setCollBase(MongoCollection collBase) {
		MongoConn.collBase = collBase;
	}

	public static MongoCollection getCollScans() {
		return collScans;
	}

	public static void setCollScans(MongoCollection collScans) {
		MongoConn.collScans = collScans;
	}

	public static MongoCollection getCollBoxes() {
		return collBoxes;
	}

	public static void setCollBoxes(MongoCollection collBoxes) {
		MongoConn.collBoxes = collBoxes;
	}
	
	

}
