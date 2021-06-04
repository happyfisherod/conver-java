package com.primari.backend.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.inject.Inject;
import com.cloudmersive.client.ConvertDocumentApi;
import com.cloudmersive.client.invoker.ApiException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.reactivex.Single;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Primary
public class ConvertService {

    private static final Logger logger = LoggerFactory.getLogger(ConvertService.class);
    private final ConvertDocumentApi convertDocumentApi;

    @Inject
    public ConvertService(ConvertDocumentApi convertDocumentApi) {
        this.convertDocumentApi = convertDocumentApi;
    }

    public Single<HttpResponse<StreamedFile>> convertFileToPdf(StreamingFileUpload file){
        logger.info("converting the file to PDF (File autodetect)");

        File tempFile = new File("/tmp", file.getFilename());
        Publisher<Boolean> uploadPublisher = file.transferTo(tempFile);

        return Single.fromPublisher(uploadPublisher).map(success -> convertFileToPdf(success, tempFile));
    }

    public HttpResponse<StreamedFile> getFileByGsKey(String gsKey) {
        logger.info("Converting gs object");
        try {
            return tryToGetFileByGsKey(gsKey);
        }
        catch (IOException e) {
            logger.error("IOException: " + e.getMessage());
            return HttpResponse.serverError();
        } catch (ApiException e) {
            logger.error("API exception: " + e.getMessage());
            return HttpResponse.serverError();
        }
    }

    public HttpResponse<StreamedFile> convertHtmlToPdf(String html){
        logger.info("Converting to html");
        try {
            return tryConvertToHtml(html);
        }
        catch (IOException e) {
            logger.error("IOException with message: " +e.getMessage());
            return HttpResponse.serverError();
        } catch (ApiException e) {
            logger.error("ApiException with message: " +e.getMessage());
            return HttpResponse.serverError();
        }
    }

    private MutableHttpResponse<StreamedFile> tryConvertToHtml(String html) throws IOException, ApiException {
        File tempFile = new File("/tmp", System.currentTimeMillis() + ".html");
        Files.write(tempFile.toPath(), html.getBytes());

        byte[] converted = convertDocumentApi.convertDocumentAutodetectToPdf(tempFile);
        InputStream inputStream = new ByteArrayInputStream(converted);
        Files.deleteIfExists(tempFile.toPath());

        return HttpResponse.ok(new StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
    }

    private MutableHttpResponse<StreamedFile> tryToGetFileByGsKey(String gsKey) throws ApiException, IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        File tempFile = new File("/tmp", gsKey.replaceAll("/", "_"));
        tempFile.createNewFile();
        Blob blob = storage.get(BlobId.of("primari.appspot.com", gsKey));
        blob.downloadTo(tempFile.toPath());
        byte[] converted = convertDocumentApi.convertDocumentAutodetectToPdf(tempFile);
        InputStream inputStream = new ByteArrayInputStream(converted);
        Files.deleteIfExists(tempFile.toPath());

        return HttpResponse.ok(new StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
    }

    private HttpResponse<StreamedFile> convertFileToPdf(boolean success, File tempFile) throws ApiException, IOException {
        if (success) {
            byte[] converted = convertDocumentApi.convertDocumentAutodetectToPdf(tempFile);

            InputStream inputStream = new ByteArrayInputStream(converted);
            Files.deleteIfExists(tempFile.toPath());

            return HttpResponse.ok(new StreamedFile(inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
        }
        return HttpResponse.serverError();
    }

}
