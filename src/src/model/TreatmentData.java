package src.model;

import src.servlets.Controler;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.opengis.feature.simple.SimpleFeature;

import src.servlets.Controler;
import ucar.nc2.util.xml.Parse;

import com.sun.corba.se.impl.orb.ParserTable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;



public class TreatmentData {

    private DarwinCore fileDarwinCore;
    private ArrayList<File> rasterFiles;
    private HashMap<Integer, HashMap<String, Boolean>> hashMapValidOrNot;
    private int nbFileRandom;
    private String DIRECTORY_PATH = "/home/mhachet/workspace/WebWorkflowCleanData/";

    /**
     * 
     * package model
     * TreatmentData
     */
    public TreatmentData(){

    }

    /**
     * Drop Clean and temp tables. 
     * Delete DarwinCoreInput table.
     */
    public void deleteTables(){
	ConnectionDatabase newConnectionDeleteClean = new ConnectionDatabase();
	ArrayList<String> messagesClean = new ArrayList<String>();
	messagesClean.add("\n--- Delete Clean table ---");
	messagesClean.addAll(newConnectionDeleteClean.dropTable("Workflow.Clean"));

	for(int i = 0 ; i < messagesClean.size() ; i++){
	    System.out.println(messagesClean.get(i));
	}

	ConnectionDatabase newConnectionDeleteDarwin = new ConnectionDatabase();
	ArrayList<String> messagesDarwin = new ArrayList<String>();
	messagesDarwin.add("\n--- Delete DarwinCoreInput table ---");
	messagesDarwin.addAll(newConnectionDeleteDarwin.deleteTable("Workflow.DarwinCoreInput"));

	for(int i = 0 ; i < messagesDarwin.size() ; i++){
	    System.out.println(messagesDarwin.get(i));
	}

	ConnectionDatabase newConnectionDeleteTemp = new ConnectionDatabase();
	ArrayList<String> messagesTemp = new ArrayList<String>();
	messagesTemp.add("\n--- Delete temp table ---");
	messagesTemp.addAll(newConnectionDeleteTemp.dropTable("Workflow.temp"));

	for(int i = 0 ; i < messagesTemp.size() ; i++){
	    System.out.println(messagesTemp.get(i));
	}


    }

    public int generateRandomKey(){
	Random random = new Random();
	nbFileRandom = random.nextInt();

	return nbFileRandom;
    }
    /**
     * Create a DarwinCore class for each file.
     * Initial file will be modified thanks to readFile()
     * @param File inputFile
     * @param int nbFile
     * @return ArrayList<String> lines of the file
     */ 
    public List <String> initialiseFile(File inputFile, int nbFile) throws IOException{
	fileDarwinCore = new DarwinCore(inputFile, nbFile);
	List<String> listLinesDarwinCore = fileDarwinCore.readFile();

	return listLinesDarwinCore;
    }

    /**
     * Modified initial files in order to fill in table DarwinCoreInput
     * @param List<String> linesInputModified
     * @param int nbFile
     * @return File temporary
     */
    public File createTemporaryFile(List<String> linesInputModified, int nbFile) throws IOException{

	if(!new File(DIRECTORY_PATH + "temp/").exists()){
	    new File(DIRECTORY_PATH + "temp/").mkdirs();
	}
	File tempFile = new File(DIRECTORY_PATH + "temp/inputFile_" + Integer.toString(nbFile) + ".csv");
	FileWriter writer = null;
	try{
	    writer = new FileWriter(tempFile);
	    for(int i = 0 ; i < linesInputModified.size() ; i++){
		writer.write(linesInputModified.get(i) + "\n");
	    }
	}catch(IOException ex){
	    ex.printStackTrace();
	}finally{
	    if(writer != null){
		writer.close();
	    }
	}
	return tempFile;
    }

    /**
     * Create sql request to insert input file modified (temporary) in DarwinCoreInput table
     * 
     * @param File inputDarwinCoreModified
     * @param List<String> linesInputFile
     * @return String sql request
     */
    public String createSQLInsert(File inputDarwinCoreModified, List<String> linesInputFile){
	String sql = "";		
	String firstLine = linesInputFile.get(0).replace("\t", "_,");
	sql = "LOAD DATA LOCAL INFILE '" + inputDarwinCoreModified.getAbsolutePath() + "' INTO TABLE Workflow.DarwinCoreInput FIELDS TERMINATED BY ',' ENCLOSED BY '\"' IGNORE 1 LINES (" + firstLine + ");";
	return sql;
    }

    /**
     * Create DarwinCoreInput table in the database from input file(s)
     * 
     * @param String insertFileSQL
     */
    public void createTableDarwinCoreInput(String insertFileSQL){
	ConnectionDatabase newConnection = new ConnectionDatabase();
	String choiceStatement = "execute";
	ArrayList<String> messages = new ArrayList<String>();
	messages.add("\n--- Create DarwinCoreInput table ---");
	messages.addAll(newConnection.newConnection(choiceStatement, insertFileSQL));

	for(int i = 0 ; i < messages.size() ; i++){
	    System.out.println(messages.get(i));
	}
    }


    //Vérifier que le code iso2 existe et qu'il est bien inscrit dans la table IsoCode !!!
    /**
     * Create temporary table "temp" with only correct iso2 code in DarwinCoreInput table.
     * Iso2 code (countryCode_) is correct if it's contained in IsoCode table (iso2_).
     */
    public void deleteWrongIso2() {
	ConnectionDatabase newConnectionTemp = new ConnectionDatabase();
	ArrayList<String> messages = new ArrayList<String>();
	String choiceStatement = "executeUpdate";
	messages.add("\n--- Create temporary table with correct ISO2 ---");
	String sqlCreateTemp = "CREATE TABLE Workflow.temp AS SELECT DarwinCoreInput.* FROM Workflow.DarwinCoreInput,Workflow.IsoCode WHERE countryCode_=IsoCode.iso2_;";

	messages.addAll(newConnectionTemp.newConnection(choiceStatement, sqlCreateTemp));

	for(int i = 0 ; i < messages.size() ; i++){
	    System.out.println(messages.get(i));
	}
    }

    /**
     * From temp table, create a Clean table with correct geospatial coordinates :
     * -90 >= latitude > 0
     *  0 < latitude <= 90
     *  
     *  -180 >= longitude > 0
     *   0 < longitude <= 180
     *   
     *   tag "hasGeospatialIssues" = false
     *   
     */
    public void createTableClean(){
	ConnectionDatabase newConnectionClean = new ConnectionDatabase();
	ArrayList<String> messages = new ArrayList<String>();
	String choiceStatement = "executeUpdate";
	messages.add("\n--- Create Table Clean from temporary table ---");
	String sqlCreateClean = "CREATE TABLE Workflow.Clean AS SELECT * FROM Workflow.temp WHERE " +
		"(decimalLatitude_!=0 AND decimalLatitude_<90 AND decimalLatitude_>-90 AND decimalLongitude_!=0 " +
		"AND decimalLongitude_>-180 AND decimalLongitude_<180) AND (hasGeospatialIssues_='false');";
	messages.addAll(newConnectionClean.newConnection(choiceStatement, sqlCreateClean));

	for(int i = 0 ; i < messages.size() ; i++){
	    System.out.println(messages.get(i));
	}
    }

    /**
     * Select wrong coordinates and write in a file:
     * latitude = 0 ; <-90 ; >90
     * longitude = 0 ; <-180 ; >180
     * 
     * @return File wrong coordinates
     */
    public File deleteWrongCoordinates(){
	ConnectionDatabase newConnection = new ConnectionDatabase();

	ArrayList<String> messages = new ArrayList<String>();
	messages.add("\n--- Select wrong coordinates ---");

	String sqlRetrieveWrongCoord = "SELECT * FROM Workflow.DarwinCoreInput WHERE decimalLatitude_=0 OR decimalLatitude_>90 OR decimalLatitude_<-90 OR decimalLongitude_=0 OR decimalLongitude_>180 OR decimalLongitude_<-180;";
	messages.addAll(newConnection.newConnection("executeQuery", sqlRetrieveWrongCoord));
	ArrayList<String> resultatSelect = newConnection.getResultatSelect();
	messages.add("nb lignes affectées : " + Integer.toString(resultatSelect.size() - 1));

	if(!new File(DIRECTORY_PATH + "temp/wrong/").exists())
	{
	    new File(DIRECTORY_PATH + "temp/wrong/").mkdirs();
	}

	File wrongCoor = this.createFileCsv(resultatSelect, "wrong/wrong_coordinates");


	for(int j = 0 ; j < messages.size() ; j++){
	    System.out.println(messages.get(j));
	}

	return wrongCoor;
    }

    /**
     * Select wrong geospatial and write in a file :
     * tag "hasGeospatialIssues_" = true
     * 
     * @return File wrong geospatial
     */
    public File deleteWrongGeospatial(){
	ConnectionDatabase newConnection = new ConnectionDatabase();

	ArrayList<String> messages = new ArrayList<String>();
	messages.add("\n--- Select wrong geospatialIssues ---");

	String sqlRetrieveWrongGeo = "SELECT * FROM Workflow.DarwinCoreInput WHERE hasGeospatialIssues_='true' AND !(decimalLatitude_=0 OR decimalLatitude_>90 OR decimalLatitude_<-90 OR decimalLongitude_=0 OR decimalLongitude_>180 OR decimalLongitude_<-180);";
	messages.addAll(newConnection.newConnection("executeQuery", sqlRetrieveWrongGeo));

	ArrayList<String> resultatSelect = newConnection.getResultatSelect();

	messages.add("nb lignes affectées : " + Integer.toString(resultatSelect.size() - 1));

	if(!new File(DIRECTORY_PATH + "temp/wrong/").exists())
	{
	    new File(DIRECTORY_PATH + "temp/wrong/").mkdirs();
	}

	File wrongGeo = this.createFileCsv(resultatSelect, "wrong/wrong_geospatialIssues");

	for(int j = 0 ; j < messages.size() ; j++){
	    System.out.println(messages.get(j));
	}

	return wrongGeo;
    }


    /**
     * 
     * Add synonyms for each taxon (if exist)
     * 
     * @return void
     */
    public void includeSynonyms(File includeSynonyms){

	if(includeSynonyms != null){
	    SynonymsTreatment treatmentSynonyms = new SynonymsTreatment(includeSynonyms);
	    //treatmentSynonyms.getTagsSynonymsTempTable();
	    treatmentSynonyms.createSynonymTempTable();
	    treatmentSynonyms.updateCleanFromSynonymTemp();
	}
	else{
	    SynonymsTreatment treatmentSynonymsDefault = new SynonymsTreatment();
	    treatmentSynonymsDefault.updateClean();

	}

    }

    /**
     * Check if coordinates (latitude and longitude) are included in the country indicated by the iso2 code
     * 
     * @return void
     */
    public void getPolygonTreatment(){
	PolygonTreatment polygone = new PolygonTreatment();

	ArrayList<String> decimalLatitude = fileDarwinCore.getDecimalLatitudeClean();
	ArrayList<String> decimalLongitude = fileDarwinCore.getDecimalLongitudeClean();
	ArrayList<String> iso2Codes = fileDarwinCore.getIso2Clean();
	ArrayList<String> gbifIdList = fileDarwinCore.getGbifIDClean();

	for(int i = 1 ; i < decimalLatitude.size() ; i++){
	    float latitude = 0;
	    float longitude = 0;
	    String iso2 = "";
	    String iso3 = "";
	    String gbifId_ = "";

	    latitude = Float.parseFloat(decimalLatitude.get(i).replace("\"", ""));
	    longitude = Float.parseFloat(decimalLongitude.get(i).replace("\"", ""));
	    iso2 = iso2Codes.get(i);
	    iso3 = this.convertIso2ToIso3(iso2);
	    gbifId_ = gbifIdList.get(i);

	    File geoJsonFile = new File(DIRECTORY_PATH + "src/ressources/gadm_json/" + iso3.toUpperCase() + "_adm0.json");
	    GeometryFactory geometryFactory = new GeometryFactory();
	    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
	    System.out.println("--------------------------------------------------------------");
	    System.out.println("------------------ Check point in polygon --------------------");
	    System.out.println("Lat : " + latitude + "\tLong : " +  longitude);
	    System.out.println("gbifID : " + gbifId_ + "\tIso3 : " + iso3 + "\tiso2 : " + iso2);
	    boolean isContained = polygone.polygonContainedPoint(point, geoJsonFile);
	    System.out.println(isContained);
	    System.out.println("--------------------------------------------------------------");
	}
    }

    /**
     * Create a new csv file from lines 
     * 
     * @param ArrayList<String> linesFile
     * @param String fileName
     *  
     * @return File 
     */
    public File createFileCsv(ArrayList<String> linesFile, String fileName){
	if(!new File(DIRECTORY_PATH + "temp/").exists())
	{
	    new File(DIRECTORY_PATH + "temp/").mkdirs();
	}

	String fileRename = fileName + "_" + nbFileRandom + ".csv";
	File newFile = new File(DIRECTORY_PATH + "temp/" + fileRename);
	FileWriter writer = null;
	try {
	    writer = new FileWriter(newFile);
	    for(int i = 0 ; i < linesFile.size() ; i++){
		writer.write(linesFile.get(i) + "\n");
	    }
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	try {
	    writer.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	System.out.println(fileRename + " written !!!");
	return newFile;
    }


    /**
     * Convert the iso2 code (2 letters) to iso3 code (3 letters)
     * 
     * @param String iso2
     * @return String iso3
     */
    public String convertIso2ToIso3(String iso2){
	String iso3 = "";

	ConnectionDatabase newConnection = new ConnectionDatabase();

	ArrayList<String> messages = new ArrayList<String>();
	messages.add("\n--- Convert iso2 code to iso3 code ---");
	String sqlConvertIso2Iso3 = "SELECT iso3_ FROM Workflow.IsoCode WHERE iso2_ = \"" + iso2.replaceAll("\"", "") + "\";";
	messages.addAll(newConnection.newConnection("executeQuery", sqlConvertIso2Iso3));

	ArrayList<String> resultatConvert = newConnection.getResultatSelect();
	if(resultatConvert.size() != 2){
	    System.err.println("Several iso2");
	}
	else{
	    iso3 = resultatConvert.get(1).replaceAll("\"", "");
	}
	return iso3;
    }

    /**
     * 
     * Tdwg4 code is retrieved for each coordinates 
     * 
     * @return void
     */
    public void checkIsoTdwgCode(){

	PolygonTreatment tdwg4 = new PolygonTreatment();
	ArrayList<String> decimalLatitude = fileDarwinCore.getDecimalLatitudeClean();
	ArrayList<String> decimalLongitude = fileDarwinCore.getDecimalLongitudeClean();
	ArrayList<String> iso2Codes = fileDarwinCore.getIso2Clean();

	for(int i = 1 ; i < decimalLatitude.size() ; i++){
	    float latitude = 0;
	    float longitude = 0;
	    String iso2 = "";
	    latitude = Float.parseFloat(decimalLatitude.get(i).replace("\"", ""));
	    longitude = Float.parseFloat(decimalLongitude.get(i).replace("\"", ""));
	    iso2 = iso2Codes.get(i).replace("\"", "");
	    GeometryFactory geometryFactory = new GeometryFactory();
	    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
	    System.out.println("--------------------------------------------------------------");
	    System.out.println("---------------- Check point in TDWG4 code -------------------");
	    System.out.println("Lat : " + latitude + "\tLong : " + longitude);
	    System.out.print("iso2 : " + iso2);
	    String tdwg4Code = "";
	    try {
		tdwg4Code = tdwg4.tdwg4ContainedPoint(point, iso2);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    System.out.println("\ttdwg4 : " + tdwg4Code);
	    System.out.println("--------------------------------------------------------------");
	}
    }

    /**
     * 
     * Check if coordinates are included in raster cells
     * 
     * @param ArrayList<File> raster file
     * @return void
     */
    public File checkWorldClimCell(ArrayList<File> rasterFiles) {

	RasterTreatment rasterTreatment = new RasterTreatment(rasterFiles, this);

	File matrixFileValidCells = rasterTreatment.treatmentRaster();

	return matrixFileValidCells;
    }

    public boolean deleteDirectory(File path){

	if(path.isDirectory()){

	    String [] filesChild = path.list();

	    for(int i = 0 ; i < filesChild.length ; i ++){
		System.out.println("delete : " + filesChild[i]);
		boolean succes = this.deleteDirectory(new File(path + "/" + filesChild[i]));
		if(!succes){
		    return false;
		}
	    }
	}

	return path.delete();

    }

    public DarwinCore getFileDarwinCore() {
	return fileDarwinCore;
    }

    public void setFileDarwinCore(DarwinCore fileDarwinCore) {
	this.fileDarwinCore = fileDarwinCore;
    }

    public ArrayList<File> getRasterFiles() {
	return rasterFiles;
    }

    public void setRasterFiles(ArrayList<File> rasterFiles) {
	this.rasterFiles = rasterFiles;
    }

    public HashMap<Integer, HashMap<String, Boolean>> getHashMapValidOrNot() {
	return hashMapValidOrNot;
    }

    public void setHashMapValidOrNot(
	    HashMap<Integer, HashMap<String, Boolean>> hashMapValidOrNot) {
	this.hashMapValidOrNot = hashMapValidOrNot;
    }

    public int getNbFileRandom() {
	return nbFileRandom;
    }

    public void setNbFileRandom(int nbFileRandom) {
	this.nbFileRandom = nbFileRandom;
    }

}
