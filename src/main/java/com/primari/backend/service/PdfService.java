package com.primari.backend.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudmersive.client.invoker.ApiException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import io.micronaut.context.annotation.Primary;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.http.server.types.files.SystemFile;
import io.reactivex.Single;

@Primary
public class PdfService {

	private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

	public Single<HttpResponse<SystemFile>> splitPdfFile(StreamingFileUpload file) {
		logger.info("splitting PDF file");

		File tempFile = new File("/tmp", file.getFilename());
		Publisher<Boolean> uploadPublisher = file.transferTo(tempFile);

		return Single.fromPublisher(uploadPublisher).map(success -> tryToSplitPdf(success, tempFile));
	}

	private HttpResponse<SystemFile> tryToSplitPdf(boolean success, File tempFile) throws ApiException, IOException {
		if (success) {

			String fileName = tempFile.getName().replace(".pdf", "");

			File zipFile = new File("/tmp", fileName + "_pages.zip");

			try (PDDocument document = PDDocument.load(tempFile);
					ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {

				Splitter splitter = new Splitter();

				List<PDDocument> pages = splitter.split(document);

				Iterator<PDDocument> it = pages.listIterator();
				int pageNumber = 1;

				while (it.hasNext()) {
					PDDocument page = it.next();

					FileOutputStream fo = new FileOutputStream("/tmp/" + fileName + "_" + pageNumber + ".pdf");

					page.save(fo);
					fo.close();

					pageNumber++;
				}

				while (--pageNumber > 0) {
					File file = new File("/tmp", fileName + "_" + pageNumber + ".pdf");
					byte[] array = FileUtils.readFileToByteArray(file);

					ZipEntry entry = new ZipEntry(fileName + "_" + pageNumber + ".pdf");
					out.putNextEntry(entry);
					out.write(array);
					out.closeEntry();

					Files.deleteIfExists(file.toPath());
				}

				out.close();

			} catch (Exception e) {

				System.err.println(e.getMessage());
				return HttpResponse.serverError();

			}

			Files.deleteIfExists(tempFile.toPath());

			return HttpResponse.ok(new SystemFile(zipFile));
		}
		return HttpResponse.serverError();
	}

	public HttpResponse<SystemFile> mergePdfFiles(List<String> gsKeys) {
		logger.info("mergind PDF files");

		Storage storage = StorageOptions.getDefaultInstance().getService();

		try {
			PDFMergerUtility merger = new PDFMergerUtility();

			File resultFile = new File("/tmp", "merged_file.pdf");

			merger.setDestinationFileName(resultFile.getPath());

			for (String gsKey : gsKeys) {

				File tempFile = new File("/tmp", gsKey.replaceAll("/", "_"));
				tempFile.createNewFile();
				Blob blob = storage.get(BlobId.of("primari.appspot.com", gsKey));
				blob.downloadTo(tempFile.toPath());

				merger.addSource(tempFile);
			}

			// TODO: Set a limit on the total size of files to be merged
			merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

			// TODO: Do we need to delete files from /temp, so that they are not stuck in
			// memory?

			return HttpResponse.ok(new SystemFile(resultFile));

		} catch (IOException e) {
			e.printStackTrace();
		}
		return HttpResponse.serverError();
	}

}