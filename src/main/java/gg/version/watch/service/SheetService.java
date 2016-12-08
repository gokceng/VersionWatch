package gg.version.watch.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import gg.version.watch.model.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gokcen Guner
 * 06.12.2016
 */
@Service
public class SheetService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SheetService.class);

  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "Version Watch Demo";

  /**
   * Directory to store user credentials for this application.
   */
  private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

  /**
   * Global instance of the {@link FileDataStoreFactory}.
   */
  private static FileDataStoreFactory DATA_STORE_FACTORY;

  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /**
   * Global instance of the HTTP transport.
   */
  private static HttpTransport HTTP_TRANSPORT;

  /**
   * Global instance of the scopes required by this quickstart.
   * <p/>
   * If modifying these scopes, delete your previously saved credentials
   * at ~/.credentials/sheets.googleapis.com-java-quickstart
   */
  private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
    } catch (Throwable t) {
      LOGGER.error(t.getMessage(), t);
      System.exit(1);
    }
  }

  /**
   * Creates an authorized Credential object.
   *
   * @return an authorized Credential object.
   */
  public Credential authorize() {
    try {
      // Load client secrets.
      InputStream in = SheetService.class.getResourceAsStream("/client_secret.json");
      GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

      // Build flow and trigger user authorization request.
      GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
          .setDataStoreFactory(DATA_STORE_FACTORY)
          .setAccessType("offline")
          .build();
      Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
      LOGGER.info("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
      return credential;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Build and return an authorized Sheets API client service.
   *
   * @return an authorized Sheets API client service
   */
  public Sheets getSheetsService() {
    Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  public Set<Dependency> loadData() {
    // Build a new authorized API client service.
    Sheets service = getSheetsService();

    // Prints the names and majors of students in a sample spreadsheet:
    // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
    String spreadsheetId = "";
    String range = "Sheet1!A2:C";
    ValueRange response;
    try {
      response = service.spreadsheets().values().get(spreadsheetId, range).execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    List<List<Object>> values = response.getValues();
    Set<Dependency> dependencySet = new HashSet<>();
    if (values == null || values.size() == 0) {
      LOGGER.error("No data found.");
      return dependencySet;
    }
    LOGGER.info("{} dependencies found", dependencySet.size());
    for (List row : values) {
      String groupId = (String) row.get(0);
      String artifactId = (String) row.get(1);
      String version = (String) row.get(2);
      Dependency dependency = new Dependency(groupId, artifactId);
      LOGGER.info(dependency.toString());
      dependencySet.add(dependency);
    }
    return dependencySet;
  }

  public void updateSheet(Set<Dependency> dependencies) {
    // Build a new authorized API client service.
    Sheets service = getSheetsService();

    // Prints the names and majors of students in a sample spreadsheet:
    // https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
    String spreadsheetId = "1KOLMeQvI8IIy42aCxdgJ7Nvrgm4dwu4SFdW9czr-Usw";
    String range = "Sheet2!A:F";
    BatchUpdateValuesResponse response;
    try {
      List<List<Object>> arrData1 = new ArrayList<>();
      for (Dependency dependency : dependencies) {
        List<Object> arrData = new ArrayList<>();
        arrData.add(dependency.getGroupId());
        arrData.add(dependency.getArtifactId());
        arrData.add(dependency.getCurrentVersion());
        arrData.add(dependency.getLatestVersion());
        arrData.add(dependency.getReleaseVersion());
        arrData.add(dependency.getVersioneState().name());
        arrData1.add(arrData);
      }
      ValueRange oRange = new ValueRange();
      oRange.setRange(range); // I NEED THE NUMBER OF THE LAST ROW
      oRange.setValues(arrData1);


      BatchUpdateValuesRequest request = new BatchUpdateValuesRequest();
      request.setValueInputOption("RAW");
      request.setData(Collections.singletonList(oRange));
      response = service.spreadsheets().values().batchUpdate(spreadsheetId, request).execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Integer totalUpdatedRows = response.getTotalUpdatedRows();
    LOGGER.info("{} dependencies inserted to the sheet", totalUpdatedRows);
  }
}
