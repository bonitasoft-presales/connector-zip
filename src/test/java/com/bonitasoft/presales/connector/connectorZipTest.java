package com.bonitasoft.presales.connector;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.ConnectorException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConnectorZipTest {

    private connectorZip connector;
    private ProcessAPI mockProcessAPI;
    private Document mockDocument;

    @Before
    public void setUp() throws Exception {
        // Initialize the connector and mock the dependencies
        connector = new connectorZip();
        mockProcessAPI = mock(ProcessAPI.class);
        mockDocument = mock(Document.class);
        
        // Set up mock for getAPIAccessor().getProcessAPI()
        connector.setAPIAccessor(() -> () -> mockProcessAPI);

        // Set up input parameters
        List<Document> documentsToZip = new ArrayList<>();
        documentsToZip.add(mockDocument);

        connector.setInputParameter("documentsToZip", documentsToZip);
        connector.setInputParameter("zipFileName", "testZip");
    }

    @Test
    public void testSuccessfulZipCreation() throws Exception {
        // Mock document behavior
        when(mockDocument.getContentStorageId()).thenReturn("documentStorageId");
        when(mockDocument.getContentFileName()).thenReturn("document.txt");
        when(mockProcessAPI.getDocumentContent("documentStorageId")).thenReturn("Sample content".getBytes());

        // Execute the connector
        connector.executeBusinessLogic();

        // Verify that the output is correctly set
        DocumentValue outputZip = (DocumentValue) connector.getOutputParameter("outputZipFile");
        assertNotNull(outputZip);
        assertEquals("application/zip", outputZip.getMimeType());
        assertTrue(outputZip.getFileName().endsWith(".zip"));

        // Further assertions can be added based on your expected zip file contents
    }

    @Test(expected = ConnectorException.class)
    public void testDocumentNotFound() throws Exception {
        // Simulate a DocumentNotFoundException
        when(mockProcessAPI.getDocumentContent(anyString())).thenThrow(new DocumentNotFoundException("Document not found"));

        connector.executeBusinessLogic();
    }

    @Test(expected = ConnectorValidationException.class)
    public void testInvalidInputParameters() throws Exception {
        // Set invalid input
        connector.setInputParameter("documentsToZip", null);

        connector.validateInputParameters();
    }

    @Test(expected = ConnectorException.class)
    public void testIOExceptionDuringZipCreation() throws Exception {
        // Mock an IOException during the zip creation
        when(mockProcessAPI.getDocumentContent(anyString())).thenThrow(new IOException("Simulated IOException"));

        connector.executeBusinessLogic();
    }
}
