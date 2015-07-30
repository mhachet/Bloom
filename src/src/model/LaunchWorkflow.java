/**
 * src.model
 * LaunchWorkflow
 * TODO
 */
package src.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import src.beans.Finalisation;
import src.beans.Initialise;
import src.beans.Step1_MappingDwc;
import src.beans.Step2_ReconciliationService;
import src.beans.Step3_CheckCoordinates;
import src.beans.Step4_CheckGeoIssue;
import src.beans.Step5_CheckTaxonomy;
import src.beans.Step6_IncludeSynonym;
import src.beans.Step7_CheckTDWG;
import src.beans.Step8_CheckISo2Coordinates;
import src.beans.Step9_CheckCoordinatesRaster;

/**
 * src.model
 * 
 * LaunchWorkflow.java
 */
public class LaunchWorkflow {

    private Treatment dataTreatment;
    private Initialise initialisation;
    private Finalisation finalisation;
    //private String DIRECTORY_PATH = "/home/mhachet/workspace/WebWorkflowCleanData/";
    
    private Step1_MappingDwc step1;
    private Step2_ReconciliationService step2;
    private Step3_CheckCoordinates step3;
    private Step4_CheckGeoIssue step4;
    private Step5_CheckTaxonomy step5;
    private Step6_IncludeSynonym step6;
    private Step7_CheckTDWG step7;
    private Step8_CheckISo2Coordinates step8;
    private Step9_CheckCoordinatesRaster step9;
    
    /**
     * 
     * src.model
     * LaunchWorkflow
     */
    public LaunchWorkflow(Initialise initialise){
	this.initialisation = initialise;
    }

    /**
     * Call steps of the workflow
     * 
     * @throws IOException
     * @return void
     */
    public void initialiseLaunchWorkflow() throws IOException{
	this.dataTreatment = new Treatment();
	this.dataTreatment.setNbSessionRandom(initialisation.getNbSessionRandom());
	this.dataTreatment.setDIRECTORY_PATH(initialisation.getDIRECTORY_PATH());
	this.dataTreatment.setRESSOURCES_PATH(initialisation.getRESSOURCES_PATH());
	
	finalisation = new Finalisation();
	step1 = new Step1_MappingDwc();
	step2 = new Step2_ReconciliationService();
	step3 = new Step3_CheckCoordinates();
	step4 = new Step4_CheckGeoIssue();
	step5 = new Step5_CheckTaxonomy();
	step6 = new Step6_IncludeSynonym();
	step7 = new Step7_CheckTDWG();
	step8 = new Step8_CheckISo2Coordinates();
	step9 = new Step9_CheckCoordinatesRaster();
	
	boolean inputFilesIsValid = this.isValidInputFiles();
	
	if(inputFilesIsValid){
	    
	    this.launchWorkflow();
	}

	if(this.initialisation.isSynonym()){

	    boolean synonymFileIsValid = this.isValidSynonymFile();
	    this.launchSynonymOption(synonymFileIsValid);
	    step6.setInvolved(true);
	}

	if(this.initialisation.isTdwg4Code()){
	    dataTreatment.tdwgCodeOption();
	    step7.setInvolved(true);
	}

	if(this.initialisation.isRaster()){
	    step9.setInvolved(true);
	    boolean rasterFilesIsValid = this.isValidRasterFiles();
	    if(rasterFilesIsValid){
		this.launchRasterOption();	
		step9.setStep9_ok(true);
	    }
	    else{
		step9.setStep9_ok(false);
	    }
	}

	//System.out.println("establishment : " + this.initialisation.getEstablishmentList());
	//keep introduced data
	if(this.initialisation.isEstablishment()){
	    this.launchEstablishmentMeansOption();
	}
	
	this.writeFinalOutput();
	
	this.dataTreatment.deleteTables();
    }
    
    /**
     * Call main steps of the workflow
     * 
     * @throws IOException
     * @return void
     */
    public void launchWorkflow() throws IOException{
	
	ArrayList<MappingDwC> listMappingDWC = this.initialisation.getListDwcFiles();
	HashMap<MappingDwC, String> mappingPath = step1.getMappedFilesAssociatedPath();
	
	for(int i = 0 ; i < listMappingDWC.size() ; i++){
	    MappingDwC mappingDwc = listMappingDWC.get(i);
	    boolean mapping = mappingDwc.isMapping();
	    if(mapping){
		step1.setInvolved(mapping);
		
		this.dataTreatment.mappingDwC(mappingDwc);
		String pathMappedFile = mappingDwc.getMappedFile().getAbsolutePath().replace(initialisation.getDIRECTORY_PATH(),"");
		mappingPath.put(mappingDwc, pathMappedFile);
		
		
	    }
	}
		
	for(int i = 0 ; i < this.initialisation.getListDwcFiles().size() ; i++){
	    MappingDwC fileInput = this.initialisation.getListDwcFiles().get(i);
	    int idFile = fileInput.getCounterID();
	    List<String> linesInputFile = null;
	    
	    if(fileInput.isMapping()){
		linesInputFile = this.dataTreatment.initialiseFile(fileInput.getMappedFile(), idFile);
	    }
	    else{
		linesInputFile = this.dataTreatment.initialiseFile(fileInput.getNoMappedFile().getCsvFile(), idFile);
	    }
	   
	    File inputFileModified = this.dataTreatment.createTemporaryFile(linesInputFile, idFile);
	    String sqlInsert = this.dataTreatment.createSQLInsert(inputFileModified, linesInputFile);
	    
	    this.dataTreatment.createTableDarwinCoreInput(sqlInsert);
	}	

	GeographicTreatment geoTreatment = this.dataTreatment.checkGeographicOption();
	
	File wrongCoordinatesFile = geoTreatment.getWrongCoordinatesFile();
	finalisation.setWrongCoordinatesFile(wrongCoordinatesFile);
	finalisation.setPathWrongCoordinatesFile(wrongCoordinatesFile.getAbsolutePath().replace(initialisation.getDIRECTORY_PATH(), ""));	
	step3.setNbFound(geoTreatment.getNbWrongCoordinates());
	
	File wrongGeospatial = geoTreatment.getWrongGeoFile();
	finalisation.setWrongGeospatial(wrongGeospatial);
	finalisation.setPathWrongGeospatial(wrongGeospatial.getAbsolutePath().replace(initialisation.getDIRECTORY_PATH(), ""));
	step4.setNbFound(geoTreatment.getNbWrongGeospatialIssues());
	
	File wrongPolygon = geoTreatment.getWrongPolygonFile();
	finalisation.setWrongPolygon(wrongPolygon);
	finalisation.setPathWrongPolygon(wrongPolygon.getAbsolutePath().replace(initialisation.getDIRECTORY_PATH(), ""));
	step8.setNbFound(geoTreatment.getNbWrongIso2());
	
    }

    /**
     * Check if data (from input files) are valid 
     * 
     * @return boolean
     */
    public boolean isValidInputFiles(){

	if(this.initialisation.getListDwcFiles().size() != 0){
	    System.out.println("Your data are valid");
	    return true;
	}
	else{
	    System.out.println("Your data aren't valid");
	    return false;
	}

    }

    /**
     * Check if raster files are valid
     * 
     * @return boolean
     */
    public boolean isValidRasterFiles(){
	//System.out.println("size raster : " + this.initialisation.getInputRastersList().size());
	//System.out.println("size header : " + this.initialisation.getHeaderRasterList().size());
	boolean isValid = true;
	if(this.initialisation.getInputRastersList().size() == this.initialisation.getHeaderRasterList().size()){
	    if(this.initialisation.getInputRastersList().size() == 0){
		System.err.println("You have to put a raster file (format : bil, ...) if you desire to match your point and cells data.");
		isValid = false;
	    }
	}
	else{
	    System.err.println("You have to put a raster file AND its header file (format : hdr).");
	    isValid = false;
	}
	
	for(int i = 0 ; i < this.initialisation.getInputRastersList().size() ; i++){
	    File raster = this.initialisation.getInputRastersList().get(i);
	    String extensionRaster = raster.getName().substring(raster.getName().lastIndexOf("."));
	    String [] extensionsRaster = {".bil", ".grd", ".asc", ".sdat", ".rsc", ".nc", ".cdf", ".bsq", ".bip", ".adf"};
	    ArrayList<String> extensionsRasterList = new ArrayList(Arrays.asList(extensionsRaster));
	    
	    if(!extensionsRasterList.contains(extensionRaster)){
		isValid = false;
	    }
	    
	}
	for(int i = 0 ; i < this.initialisation.getHeaderRasterList().size() ; i++){
	    File header = this.initialisation.getHeaderRasterList().get(i);
	    String extensionHeader = header.getName().substring(header.getName().lastIndexOf("."));
	    String headerName = header.getName();
	    
	    /*if(!headerName.contains("hdr")){
		System.out.println("false 2 " + headerName);
		isValid = false;
	    }
	    else if(!extensionHeader.equals(".hdr")){
		System.out.println("false 3");
		isValid = false;
	    }*/
	    
	    
	    
	}
	
	//System.out.println("raster valid : " + isValid);
	return isValid;
    }

    /**
     * Check if synonym file is valid
     * 
     * @return boolean
     */
    public boolean isValidSynonymFile(){
	System.out.println("size synonym : " + this.initialisation.getInputSynonymsList());
	if(this.initialisation.getInputSynonymsList().size() != 0){
	    return true;
	}
	else{
	    return false;
	}
    }

    /**
     * Launch synonym option
     * 
     * @param isValidSynonymFile
     * @return void
     */
    public void launchSynonymOption(boolean isValidSynonymFile){
	if(isValidSynonymFile){
	    this.dataTreatment.includeSynonyms(this.initialisation.getInputSynonymsList().get(0));
	}
	else{
	    this.dataTreatment.includeSynonyms(null);
	}
	
	step6.setNbFound(this.dataTreatment.getNbSynonymInvolved());
    }

    /**
     * Launch raster option
     * 
     * @return void
     */
    public void launchRasterOption(){

	RasterTreatment rasterTreatment = this.dataTreatment.checkWorldClimCell(this.initialisation.getInputRastersList());
	finalisation.setMatrixFileValidCells(rasterTreatment.getMatrixFileValidCells());
	finalisation.setPathMatrixFile(rasterTreatment.getMatrixFileValidCells().getAbsolutePath().replace(initialisation.getDIRECTORY_PATH(), ""));
	step9.setNbFound(rasterTreatment.getNbWrongOccurrences());
    }

    /**
     * Launch establishmentMeans option
     * 
     * @return void
     */
    public void launchEstablishmentMeansOption(){
	if(this.initialisation.getEstablishmentList().size() != 0){
	    File wrongEstablishmentMeans = this.dataTreatment.establishmentMeansOption(this.initialisation.getEstablishmentList());
	    finalisation.setWrongEstablishmentMeans(wrongEstablishmentMeans);
	    finalisation.setPathWrongEstablishmentMeans(wrongEstablishmentMeans.getAbsolutePath().replace(initialisation.getDIRECTORY_PATH(), ""));
	}
    }

   
    public void writeFinalOutput(){
	ArrayList<File> listFinalOutput = new ArrayList<>();
	ArrayList<String> listPathsOutput = new ArrayList<>();
	
	if(!new File(initialisation.getDIRECTORY_PATH() + "temp/final_results/").exists()){
	    new File(initialisation.getDIRECTORY_PATH() + "temp/final_results/").mkdir();
	}
	
	int nbFiles = this.initialisation.getNbFiles();
	for(int i = 0 ; i < nbFiles ; i++){
	    int idFile = this.initialisation.getListDwcFiles().get(i).getCounterID();
	    String originalName = this.initialisation.getListDwcFiles().get(i).getOriginalName();
	    String originalExtension = this.initialisation.getListDwcFiles().get(i).getOriginalExtension();
	    
	    ConnectionDatabase newConnection = new ConnectionDatabase();
	    ArrayList<String > resultCleanTable = newConnection.getCleanTableFromIdFile(idFile, initialisation.getNbSessionRandom());
	    String nameFile = originalName.replace("." + originalExtension, "") + "_" + initialisation.getNbSessionRandom() + "_clean.csv";
	    File cleanOutput = this.dataTreatment.createFileCsv(resultCleanTable, nameFile);

	    listFinalOutput.add(cleanOutput);
	    String pathFile = cleanOutput.getAbsolutePath().replace(initialisation.getDIRECTORY_PATH(),"");
	    listPathsOutput.add(pathFile);
	}
	
	finalisation.setListPathsOutputFiles(listPathsOutput);
	finalisation.setFinalOutputFiles(listFinalOutput);
    }
    
    /**
     *  
     * @return TreatmentData
     */
    public Treatment getDataTreatment() {
	return dataTreatment;
    }

    /**
     * 
     * @param dataTreatment
     * @return void
     */
    public void setDataTreatment(Treatment dataTreatment) {
	this.dataTreatment = dataTreatment;
    }

    public Initialise getInitialisation() {
        return initialisation;
    }

    public void setInitialisation(Initialise initialisation) {
        this.initialisation = initialisation;
    }

    public Finalisation getFinalisation() {
        return finalisation;
    }

    public void setFinalisation(Finalisation finalisation) {
        this.finalisation = finalisation;
    }

    /*
    public String getDIRECTORY_PATH() {
        return DIRECTORY_PATH;
    }

    public void setDIRECTORY_PATH(String dIRECTORY_PATH) {
        DIRECTORY_PATH = dIRECTORY_PATH;
    }
     */
    public Step1_MappingDwc getStep1() {
        return step1;
    }

    public void setStep1(Step1_MappingDwc step1) {
        this.step1 = step1;
    }
    
    public Step2_ReconciliationService getStep2() {
        return step2;
    }

    public void setStep2(Step2_ReconciliationService step2) {
        this.step2 = step2;
    }

    public Step3_CheckCoordinates getStep3() {
        return step3;
    }

    public void setStep2(Step3_CheckCoordinates step3) {
        this.step3 = step3;
    }

    public Step4_CheckGeoIssue getStep4() {
        return step4;
    }

    public void setStep3(Step4_CheckGeoIssue step3) {
        this.step4 = step3;
    }

    public Step5_CheckTaxonomy getStep5() {
        return step5;
    }

    public void setStep4(Step5_CheckTaxonomy step4) {
        this.step5 = step4;
    }

    public Step6_IncludeSynonym getStep6() {
        return step6;
    }

    public void setStep5(Step6_IncludeSynonym step5) {
        this.step6 = step5;
    }

    public Step7_CheckTDWG getStep7() {
        return step7;
    }

    public void setStep6(Step7_CheckTDWG step6) {
        this.step7 = step6;
    }

    public Step8_CheckISo2Coordinates getStep8() {
        return step8;
    }

    public void setStep7(Step8_CheckISo2Coordinates step7) {
        this.step8 = step7;
    }

    public Step9_CheckCoordinatesRaster getStep9() {
        return step9;
    }

    public void setStep8(Step9_CheckCoordinatesRaster step8) {
        this.step9 = step8;
    }  
    
}
