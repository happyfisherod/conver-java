package com.primari.backend.api;

import java.util.List;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;

import com.cloudmersive.client.invoker.ApiClient;
import com.cloudmersive.client.invoker.Configuration;
import com.cloudmersive.client.invoker.auth.ApiKeyAuth;
import com.primari.backend.service.PdfService;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.http.server.types.files.SystemFile;
import io.reactivex.Single;

@Controller("/pdf")
public class PdfController {

	@Inject
	PdfService pdfService;

	static {
		ApiClient defaultClient = Configuration.getDefaultApiClient();
		ApiKeyAuth apikey = (ApiKeyAuth) defaultClient.getAuthentication("Apikey");
		apikey.setApiKey("5e715f7c-460e-4444-96c5-e71c237a8827");
	}

	@Post(uri = "/split", consumes = MediaType.MULTIPART_FORM_DATA)
	public Single<HttpResponse<SystemFile>> splitPdfFile(StreamingFileUpload file) {
		return pdfService.splitPdfFile(file);
	}

	@Post(uri = "/merge", consumes = MediaType.TEXT_HTML)
	public HttpResponse<SystemFile> mergePdfFiles(@Body @NotBlank List<String> gsKeys) {
		return pdfService.mergePdfFiles(gsKeys);
	}

}