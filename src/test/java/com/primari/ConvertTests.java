package com.primari;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import com.cloudmersive.client.ConvertDocumentApi;
import com.cloudmersive.client.invoker.ApiException;
import com.primari.backend.service.ConvertService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
@MicronautTest
public class ConvertTests {

    @Mock
    private ConvertDocumentApi convertDocumentApi;

    @Captor
    private ArgumentCaptor<File> inputFileCaptor;

    @InjectMocks
    private ConvertService convertService;

    private static final String TEST_HTML = "testHtml";
    private static final byte[] CONVERTED_FILE = TEST_HTML.getBytes();

    @Test
	public void testConvertHtmlToPdf() throws ApiException, IOException {
        mockConvertDocumentAutodetectToPdf();
        HttpResponse<StreamedFile> streamedFileHttpResponse = convertService.convertHtmlToPdf(TEST_HTML);
        StreamedFile streamedFile = new StreamedFile(new ByteArrayInputStream(CONVERTED_FILE), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        verifyNoMoreInteractions(convertDocumentApi);
        verify(convertDocumentApi, times(1)).convertDocumentAutodetectToPdf(inputFileCaptor.capture());
        Assert.assertEquals(streamedFile.getInputStream().read(), Objects.requireNonNull(streamedFileHttpResponse.body()).getInputStream().read());
    }

	private void mockConvertDocumentAutodetectToPdf() throws ApiException {
        when(convertDocumentApi.convertDocumentAutodetectToPdf(any())).thenReturn(CONVERTED_FILE);
    }

}
