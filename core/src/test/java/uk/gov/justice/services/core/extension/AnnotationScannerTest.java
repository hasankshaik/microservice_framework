package uk.gov.justice.services.core.extension;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_CONTROLLER;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.ServiceComponent;

import java.util.HashSet;

import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AnnotationScannerTest {

    @Mock
    private AfterDeploymentValidation afterDeploymentValidation;

    @Mock
    private BeanManager beanManager;

    @Mock
    private ServiceComponentFoundEvent serviceComponentFoundEvent;

    @Mock
    private Bean<Object> beanMockCommandApiHandler;

    @Mock
    private Bean<Object> beanMockCommandController;

    @Mock
    private Bean<Object> beanMockCommandHandler;

    @Mock
    private Bean<Object> beanMockDummy;

    private AnnotationScanner annotationScanner;

    @Before
    public void setup() {
        annotationScanner = new AnnotationScanner();

        doReturn(TestCommandApiHandler.class).when(beanMockCommandApiHandler).getBeanClass();
        doReturn(TestCommandController.class).when(beanMockCommandController).getBeanClass();
        doReturn(TestCommandHandler.class).when(beanMockCommandHandler).getBeanClass();
        doReturn(Object.class).when(beanMockDummy).getBeanClass();
    }

    @Test
    public void shouldFireCommandApiFoundEventWithCommandApi() throws Exception {
        verifyIfEventFiredWith(beanMockCommandApiHandler);
    }

    @Test
    public void shouldFireCommandControllerFoundEventWithCommandController() throws Exception {
        verifyIfEventFiredWith(beanMockCommandController);
    }

    @Test
    public void shouldFireCommandHandlerFoundEventWithCommandHandler() throws Exception {
        verifyIfEventFiredWith(beanMockCommandHandler);
    }

    @Test
    public void shouldNotFireAnyEventWithNoHandler() throws Exception {
        mockBeanManagerGetBeansWith(beanMockDummy);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager, never()).fireEvent(any());
    }

    @SuppressWarnings("serial")
    private void mockBeanManagerGetBeansWith(Bean<Object> handler) {
        doReturn(new HashSet<Bean<Object>>() {
            {
                add(handler);
            }
        }).when(beanManager).getBeans(any(), any());
    }

    private void verifyIfEventFiredWith(Bean<Object> handler) {
        ArgumentCaptor<ServiceComponentFoundEvent> captor = ArgumentCaptor.forClass(ServiceComponentFoundEvent.class);
        mockBeanManagerGetBeansWith(handler);

        annotationScanner.afterDeploymentValidation(afterDeploymentValidation, beanManager);

        verify(beanManager).fireEvent(captor.capture());
        assertThat(captor.getValue(), CoreMatchers.instanceOf(ServiceComponentFoundEvent.class));
    }

    @ServiceComponent(COMMAND_API)
    public static class TestCommandApiHandler {
    }

    @ServiceComponent(COMMAND_CONTROLLER)
    public static class TestCommandController {
    }

    @ServiceComponent(COMMAND_HANDLER)
    public static class TestCommandHandler {
    }
}
