package ch.so.agi.ilivalidator.nplso;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.restassured.response.Response;

/*
 * This class can be inherited by other classes to run 
 * integration tests:
 * - Spring Boot
 * - Docker Image
 */
public abstract class IntegrationTests {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/*
	 * Simple index.html test.
	 */
	@Test
	public void indexPageTest() {				
		given().
		when().
        	get("/ilivalidator-nplso/").
        then().
            statusCode(200).
            body("html.head.title", equalTo("ilivalidator web service • nplso"));
	}
	
    /*
     * Test if version.txt is available.
     */
    @Test
    public void versionPageTest() {               
        given().
        when().
            get("/ilivalidator-nplso/version.txt").
        then().
            statusCode(200).
            body(containsString("Revision")).
            body(containsString("Application-name"));
    }

	/*
	 * We push the upload button but without sending a file
	 * to validate. It should redirect to the starting page.
	 * Not sure about how to implement this in rest assured:
	 * Now I create an empty file and checking the file size
	 * in the relevant if-clause. Testing the body seems
	 * not work though...
	 */
	@Test
	public void noFileUploadTest() throws IOException {
		File file = tempFolder.newFile("tempFile.txt");

		given().
			multiPart("file", file).
		when().
			post("/ilivalidator-nplso/").
		then().
	    	statusCode(302);//.
	    	//body("html.head.title", equalTo("ilivalidator web service")).log().all();
	}	
	
	/*
	 * Upload a text file with nonsense content and
	 * provoke a iox exception.
	 */
	@Test
	public void uploadNonsenseFileTest() throws IOException {
		File file = new File("src/test/data/nonsense.txt");

		given().
			multiPart("file", file).
		when().
			post("/ilivalidator-nplso/").
		then().
	    	statusCode(400).
	    	body(containsString("could not parse file: nonsense.txt"));
	}	
	
//	@Test
//	public void successfulValidationTest() {
//		File file = new File("src/test/data/ch_254900.itf");
//		
//		given().
//			multiPart("file", file).
//		when().
//			post("/ilivalidator/").
//		then().
//			statusCode(200).
//			body(containsString("...validation done"));
//	}
	
	@Test
	public void unsuccessfulValidationTest() {
		File file = new File("src/test/data/2405.xtf");
		
		given().
			multiPart("file", file).
		when().
			post("/ilivalidator-nplso/").
		then().
			statusCode(200).
            body(containsString("Error: line 451: SO_Nutzungsplanung_20171118.Rechtsvorschriften.HinweisWeitereDokumente: object null (325CA1F1-17F5-4F19-A2E4-ECD942DB6DCA <-> E9597D3A-90CD-4175-97B5-CFEAE56CB7BE) is part of a cycle: E9597D3A-90CD-4175-97B5-CFEAE56CB7BE,325CA1F1-17F5-4F19-A2E4-ECD942DB6DCA.")).
            body(containsString("Error: line 178586: SO_Nutzungsplanung_20171118.Nutzungsplanung.Ueberlagernd_Flaeche: tid 59BDF2E0-E5E7-49B9-B6BA-583BE13152C7: Set Constraint SO_Nutzungsplanung_20171118.Nutzungsplanung.Ueberlagernd_Flaeche.laermempfindlichkeitsAreaCheck is not true.")).
            body(containsString("Error: line 46065: SO_Nutzungsplanung_20171118.Nutzungsplanung.Typ_Grundnutzung: tid F8DE04B4-2E51-4B53-97DD-959A2B47242C: Typ 'N169_weitere_eingeschraenkte_Bauzonen' (Typ_Grundnutzung) ist mit keinem Dokument verkn")).
            body(containsString("Error: line 192: SO_Nutzungsplanung_20171118.Rechtsvorschriften.Dokument: tid 9C185FF7-B78F-445D-8868-905A569BA16C: Dokument 'https://geo.so.ch/docs/ch.so.arp.zonenplaene/Zonenplaene_pdf/78-Niederbuchsiten/Entscheide/78-10-E.pdf' wurde nicht gefunden.")).
            body(containsString("...validation failed"));
	}
}
