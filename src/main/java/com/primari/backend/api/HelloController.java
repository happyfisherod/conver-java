package com.primari.backend.api;
import javax.validation.constraints.NotBlank;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.validation.Validated;
import io.reactivex.Single;

@Controller("/")
@Validated
public class HelloController {

	@Get(uri = "/hello", produces = MediaType.TEXT_PLAIN)
	public Single<String> hello(@NotBlank String name) {
		return Single.just("Hello " + name + "!");
	}
}