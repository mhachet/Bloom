/**
 * src.test
 * TestConnectionDatabase
 */
package src.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import src.model.ConnectionDatabase;

/**
 * src.test
 * 
 * TestConnectionDatabase.java
 * TestConnectionDatabase
 */
public class TestConnectionDatabase {


    /**
     * Test method for {@link src.model.ConnectionDatabase#newConnection(java.lang.String, java.lang.String)}.
     * Test database connection
     */
    @Test
    public void testNewConnection() {
	
	
	ConnectionDatabase connection = new ConnectionDatabase();
	String choiceStatement = "", sql = "";
	choiceStatement = "execute";
	sql = "SHOW COLUMNS FROM Workflow.IsoCode;";
	ArrayList<String> messages = connection.newConnection(choiceStatement, sql);
	if(messages.contains("Connection error")){
	    fail("Connection to database failed");
	}
	
    }
    
}
