package com.bonitasoft.presales.connector;

import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class connectorZipTest {

    @InjectMocks
    private connectorZip filter;

    @Mock(lenient = true)
    private APIAccessor apiAccessor;
    @Mock(lenient = true)
    private ProcessAPI processApi;

    @Mock(lenient = true)
    private EngineExecutionContext executionContext;

    @BeforeEach
    void setUp() {
        when(apiAccessor.getProcessAPI()).thenReturn(processApi);
        when(executionContext.getProcessDefinitionId()).thenReturn(1L);
    }

    @Test
    public void should_throw_exception_if_mandatory_input_is_missing() {
        assertThrows(ConnectorValidationException.class, () ->
                filter.validateInputParameters()
        );
    }

    @Test
    public void should_throw_exception_if_mandatory_input_is_not_positive_integer() {
        Map<String, Object> parameters = new HashMap<>();
        filter.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                filter.validateInputParameters()
        );
    }

    @Test
    public void should_throw_exception_if_mandatory_input_is_not_an_integer() {
        Map<String, Object> parameters = new HashMap<>();
        filter.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                filter.validateInputParameters()
        );
    }

}
