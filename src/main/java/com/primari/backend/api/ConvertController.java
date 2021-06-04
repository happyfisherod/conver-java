package com.primari.backend.api;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

import com.primari.backend.service.ConvertService;
import com.cloudmersive.client.invoker.ApiClient;
import com.cloudmersive.client.invoker.Configuration;
import com.cloudmersive.client.invoker.auth.ApiKeyAuth;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.reactivex.Single;

@Controller("/convert")
public class ConvertController {

    @Inject
    ConvertService convertService;

	static {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		ApiKeyAuth apikey = (ApiKeyAuth) defaultClient.getAuthentication("Apikey");
		apikey.setApiKey("5e715f7c-460e-4444-96c5-e71c237a8827");
	}

	@Post(uri = "/file/to/pdf", consumes = MediaType.MULTIPART_FORM_DATA)
	public Single<HttpResponse<StreamedFile>> convertFileToPdf(StreamingFileUpload file) {
        return convertService.convertFileToPdf(file);
	}

	@Post(uri = "/gskey", consumes = MediaType.TEXT_HTML)
	public HttpResponse<StreamedFile> getFileByGsKey(@Body @NotBlank String gsKey) {
        return convertService.getFileByGsKey(gsKey);
	}

	@Post(uri = "/html/to/pdf", consumes = MediaType.TEXT_HTML)
	public HttpResponse<StreamedFile> convertHtmlToPdf(@Body @NotBlank String html) {
        return convertService.convertHtmlToPdf(html);
	}

}
