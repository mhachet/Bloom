package src.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * 
 * model
 * 
 * ConnectionDatabase.java
 */
public class ConnectionDatabase {
    private String url = "jdbc:mysql://localhost:3306/Workflow";
    private String utilisateur = "mhachet";
    private String motDePasse = "ledzeppelin";
    private Connection connexion;
    private Statement statement;
    private ResultSet resultSet;
    private ArrayList<String> resultatSelect;
    private boolean resultat;
    private int i;

    /**
     * Constructor
     */
    public ConnectionDatabase(){

    }
    /**
     * Create a new connection to the database
     * @param String choiceStatement : execute, executeQuery or executeUpdate
     * @param String sql : request
     * @return ArrayList<String>
     */
    public ArrayList<String> newConnection(String choiceStatement, String sql){

	ArrayList<String> messages = new ArrayList<String>();

	try {
	    messages.add( "\nChargement du driver..." );
	    Class.forName( "com.mysql.jdbc.Driver" );
	    messages.add( "Driver chargé !" );
	} catch ( ClassNotFoundException e ) {
	    messages.add( "Erreur lors du chargement : le driver n'a pas été trouvé dans le classpath ! <br/>"
		    + e.getMessage() );
	}

	try {
	    messages.add("Connexion à la base de données ...");
	    connexion = DriverManager.getConnection( url, utilisateur, motDePasse );
	    messages.add("Connexion réussie !");

	    /* Création de l'objet gérant les requêtes */
	    statement = connexion.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

	    messages.add( "Objet requête créé !" );

	    //méthodes génériques pour n'importe quelle expression SQL - retourne un boolean : valant true si l'instruction renvoie un ResultSet, false sinon
	    if(choiceStatement == "execute"){
		setResultat(statement.execute(sql));
		messages.add(sql);
		//messages.add(resultat.toString());
	    }
	    // SELECT - retourne ResultSet : contenant les résultats 
	    else if(choiceStatement == "executeQuery"){
		resultSet = statement.executeQuery(sql);
		messages.add(sql);
		ResultSetMetaData resultMeta = resultSet.getMetaData();

		this.setResultatSelect(resultMeta);
		resultSet.close();
	    }
	    //écriture ou suppression sur la BD (requêtes de type INSERT, UPDATE, DELETE, ...)
	    //indiquant le nombre de tuples (lignes) modifiés pour un INSERT, UPDATE et DELETE, 
	    //ou alors 0 pour les instructions ne retournant rien (CREATE par exemple)
	    else if(choiceStatement == "executeUpdate"){
		i = statement.executeUpdate(sql);
		messages.add(sql);
		messages.add("nb lignes affectées : " + Integer.toString(i));
	    }

	    statement.close();

	} catch ( SQLException e ) {
	    messages.add( "Erreur lors de la connexion : " + e.getMessage() );
	}

	return messages;
    }

    /**
     * 
     * @return Statement
     */
    public Statement getStatement() {
	return statement;
    }

    /**
     * 
     * @param statement
     */
    public void setStatement(Statement statement) {
	this.statement = statement;
    }

    /**
     * 
     * @return ResultSet
     */
    public ResultSet getResultSet() {
	return resultSet;
    }

    /**
     * 
     * @param resultSet
     */
    public void setResultSet(ResultSet resultSet) {
	this.resultSet = resultSet;
    }

    /**
     * Delete table of the database
     * @param String tableName
     * @return ArrayList<String> messages list
     */
    public ArrayList<String> deleteTable(String tableName){
	ArrayList<String> messages = this.newConnection("executeUpdate", "DELETE FROM " + tableName + ";");
	return messages;
    }

    /**
     * Drop table of the database
     * @param String tableName
     * @return ArrayList<String> messages list
     */
    public ArrayList<String> dropTable(String tableName){
	ArrayList<String> messages = this.newConnection("executeUpdate", "DROP TABLE IF EXISTS " + tableName + ";");
	return messages;
    }

    /**
     * Get all result come from request
     * @return  ArrayList<String>
     */
    public ArrayList<String> getResultatSelect() {
	return resultatSelect;
    }

    /**
     * Format request resultat
     * @param resultMeta
     * @throws SQLException
     */
    public void setResultatSelect(ResultSetMetaData resultMeta) throws SQLException{
	resultatSelect = new ArrayList<String>();
	String line = "";
	//On affiche le nom des colonnes
	for(int i = 1; i <= resultMeta.getColumnCount(); i++){
	    line += resultMeta.getColumnName(i) + ",";
	}
	line = line.substring(0, line.length()-1);
	resultatSelect.add(line);
	line = "";
	while(resultSet.next()){         
	    for(int i = 1; i <= resultMeta.getColumnCount(); i++){

		try{
		    line += "\"" + resultSet.getObject(i) + "\"" + ",";
		}
		catch (Exception e) {
		    line += "NULL,";
		}

	    }
	    line = line.substring(0, line.length()-1);
	    resultatSelect.add(line);  
	    line = "";

	}
    }
    public boolean isResultat() {
	return resultat;
    }
    public void setResultat(boolean resultat) {
	this.resultat = resultat;
    }

}