package com.bonitasoft.presales.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConnectorZip extends AbstractConnector {

    static final String SINGLE_DOCUMENTS_INPUT = "documentsToZip";
    static final String MULTIPLE_DOCUMENTS_INPUT = "multipleDocumentsToZip";
    static final String ZIP_FILE_NAME = "zipFileName";
    static final String OUTPUT_ZIP = "outputZipFile";

    Logger logger = Logger.getLogger("org.bonitasoft");

    List<Document> allDocs;

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        if (getInputParameter(ZIP_FILE_NAME) == null || !(getInputParameter(ZIP_FILE_NAME) instanceof String)) {
            throw new ConnectorValidationException("You must provide a name for the output ZIP file.");
        }
        allDocs = new ArrayList<>();
        List<Document> allSingleDocs = (List<Document>) getInputParameter(SINGLE_DOCUMENTS_INPUT);
        List<List<Document>> allMultipleDocs = (List<List<Document>>) getInputParameter(MULTIPLE_DOCUMENTS_INPUT);
        if (allSingleDocs != null && !allSingleDocs.isEmpty()) {
            allDocs.addAll(allSingleDocs);
        }
        if (allMultipleDocs != null && !allMultipleDocs.isEmpty()) {
            for (List<Document> allMultipleDoc : allMultipleDocs) {
                allDocs.addAll(allMultipleDoc);
            }
        }
        if (allDocs.isEmpty()) {
            throw new ConnectorValidationException("Should provide a list one Document to zip.");
        }

    }

    @Override
    public void executeBusinessLogic() throws ConnectorException {
        String zipFileName = (String) getInputParameter(ZIP_FILE_NAME);
        logger.info("Input validated");

        // Create a temporary zip file
        File zipFile;

        try {
            zipFile = createZipFile(allDocs, zipFileName);
            DocumentValue documentValue;
            try {
                documentValue = convertFileToDocumentValue(zipFile, zipFileName);
                // Set the DocumentValue as the output
                setOutputParameter(OUTPUT_ZIP, documentValue);
            } catch (IOException e) {
                logger.severe("Failed to convert ZIP file to DocumentValue: " + e.getMessage());
                throw new ConnectorException("Error during ZIP creation", e);
            }
            if (documentValue == null) {
                throw new ConnectorException("Failed to generate ZIP file for outputZipFile");
            }
        } catch (RuntimeException | DocumentNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            throw new ConnectorException(e);
        }
    }

    // Helper method to create the zip file from Bonita documents
    private File createZipFile(List<Document> documentsToZip, String zipFileName)
            throws IOException, DocumentNotFoundException {
        File tempZipFile = File.createTempFile(zipFileName, ".zip");
        ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
        try (FileOutputStream fos = new FileOutputStream(tempZipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (Document document : documentsToZip) {
                byte[] documentContent = processAPI.getDocumentContent(document.getContentStorageId());
                if (documentContent != null && documentContent.length > 0) {
                    try (ByteArrayInputStream is = new ByteArrayInputStream(documentContent)) {
                        ZipEntry zipEntry = new ZipEntry(document.getContentFileName());
                        zos.putNextEntry(zipEntry);

                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = is.read(bytes)) >= 0) {
                            zos.write(bytes, 0, length);
                        }

                        zos.closeEntry();
                    }
                }
            }
        }
        logger.info("Zip file exists " + tempZipFile.exists());
        logger.info(tempZipFile.getAbsolutePath());

        return tempZipFile;
    }

    // Helper method to convert File to DocumentValue
    private DocumentValue convertFileToDocumentValue(File file, String fileName) throws IOException {
        // Read the file into a byte array
        byte[] fileContent = Files.readAllBytes(file.toPath());

        // Create the DocumentValue with file content, MIME type and filename
        return new DocumentValue(fileContent, "application/zip", fileName + ".zip");
    }

}
