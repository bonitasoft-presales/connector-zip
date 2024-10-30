package com.bonitasoft.presales.connector;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;
//
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ConnectorZipTest {

    private ConnectorZip connector;

    @Mock
    ProcessAPI processAPI;
    @Mock
    Document mockDocument;
    @Mock
    APIAccessor apiAccessor;

    @BeforeEach
    public void before() throws Exception {
        // Initialize the mocks
        MockitoAnnotations.initMocks(this);

        // Initialize the connector and mock the dependencies
        connector = new ConnectorZip();
        processAPI = mock(ProcessAPI.class);
        mockDocument = mock(Document.class);
        doReturn(apiAccessor).when(connector).getAPIAccessor();

    }

    @Test
    public void testSuccessfulZipCreation() throws Exception {
        //Mock the document
        connector = Mockito.spy(new ConnectorZip());
        mockDocument = mock(Document.class);
        processAPI = mock(ProcessAPI.class);
        apiAccessor = mock(APIAccessor.class);

        doReturn(apiAccessor).when(connector).getAPIAccessor();
        doReturn(processAPI).when(apiAccessor).getProcessAPI();

        // Mock document behavior
        when(mockDocument.getContentStorageId()).thenReturn("documentStorageId");
        when(mockDocument.getContentFileName()).thenReturn("document.txt");
        when(processAPI.getDocumentContent("documentStorageId")).thenReturn("Sample content".getBytes());

        // Set up input parameters
        List<Document> documentsToZip = new ArrayList<>();
        documentsToZip.add(mockDocument);
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ConnectorZip.SINGLE_DOCUMENTS_INPUT, documentsToZip);
        parameters.put(ConnectorZip.ZIP_FILE_NAME, "testZip");
        connector.setInputParameters(parameters);

        //Validate Inputs
        connector.validateInputParameters();
        // Execute the connector
        final Map<String, Object> execute = connector.execute();

        // Verify that the output is correctly set
        DocumentValue outputZip = (DocumentValue) execute.get(ConnectorZip.OUTPUT_ZIP);
        assertNotNull(outputZip);
        assertEquals("application/zip", outputZip.getMimeType());
        assertTrue(outputZip.getFileName().endsWith(".zip"));

        // Further assertions can be added based on your expected zip file contents
    }

    @Test(expected = ConnectorException.class)
    public void testDocumentNotFound() throws Exception {
        connector = Mockito.spy(new ConnectorZip());
        processAPI = mock(ProcessAPI.class);
        apiAccessor = mock(APIAccessor.class);

        doReturn(apiAccessor).when(connector).getAPIAccessor();
        doReturn(processAPI).when(apiAccessor).getProcessAPI();


        // Simulate a DocumentNotFoundException
        when(processAPI.getDocumentContent(anyString())).thenThrow(new DocumentNotFoundException("Document not found"));

        connector.executeBusinessLogic();
    }

    @Test(expected = ConnectorValidationException.class)
    public void testInvalidInputParameters() throws Exception {
        connector = Mockito.spy(new ConnectorZip());
        processAPI = mock(ProcessAPI.class);
        apiAccessor = mock(APIAccessor.class);

        doReturn(apiAccessor).when(connector).getAPIAccessor();
        doReturn(processAPI).when(apiAccessor).getProcessAPI();

        // Set invalid input
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(ConnectorZip.SINGLE_DOCUMENTS_INPUT, null);
        parameters.put(ConnectorZip.MULTIPLE_DOCUMENTS_INPUT, null);
        parameters.put(ConnectorZip.ZIP_FILE_NAME, "testZip");
        connector.setInputParameters(parameters);

        connector.validateInputParameters();
    }

    @Test
    public void testSingleInputParameters() throws Exception {
        connector = Mockito.spy(new ConnectorZip());
        processAPI = mock(ProcessAPI.class);
        apiAccessor = mock(APIAccessor.class);

        doReturn(apiAccessor).when(connector).getAPIAccessor();
        doReturn(processAPI).when(apiAccessor).getProcessAPI();

        // Set invalid input
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        List<Document> singleDocs = aListOfDocuments();
        parameters.put(ConnectorZip.SINGLE_DOCUMENTS_INPUT, singleDocs);
        parameters.put(ConnectorZip.MULTIPLE_DOCUMENTS_INPUT, null);
        parameters.put(ConnectorZip.ZIP_FILE_NAME, "testZip");
        connector.setInputParameters(parameters);

        connector.validateInputParameters();
    }

    @Test
    public void testMultipleInputParameters() throws Exception {
        connector = Mockito.spy(new ConnectorZip());
        processAPI = mock(ProcessAPI.class);
        apiAccessor = mock(APIAccessor.class);

        doReturn(apiAccessor).when(connector).getAPIAccessor();
        doReturn(processAPI).when(apiAccessor).getProcessAPI();

        // Set invalid input
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        List<Document> singleDocs = aListOfDocuments();

        parameters.put(ConnectorZip.SINGLE_DOCUMENTS_INPUT, null);
        parameters.put(ConnectorZip.MULTIPLE_DOCUMENTS_INPUT, List.of(singleDocs));
        parameters.put(ConnectorZip.ZIP_FILE_NAME, "testZip");
        connector.setInputParameters(parameters);

        connector.validateInputParameters();
    }

    private List<Document> aListOfDocuments() {
        List<Document> docs = new ArrayList<>();
        DocumentImpl d = new DocumentImpl();
        d.setContentMimeType("text");
        d.setName(UUID.randomUUID().toString() + ".txt");
        docs.add(d);
        return docs;
    }


}
