package com.bolsadeideas.springboot.webflux.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.bolsadeideas.springboot.webflux.app.models.documents.Categoria;
import com.bolsadeideas.springboot.webflux.app.models.documents.Producto;
import com.bolsadeideas.springboot.webflux.app.models.services.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

//@AutoConfigureWebClient
@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)//puerto simulado
class SpringBootWebfluxApirestApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductoService service;
	
	@Value("${config.base.endpoint}")
	private String url;

	@Test
	void listarTest() {
		/*
		 * client.get().uri("/api/productos").accept(MediaType.APPLICATION_JSON).
		 * exchange().expectStatus().isOk()
		 * .expectHeader().contentType(MediaType.APPLICATION_JSON).expectBodyList(
		 * Producto.class).hasSize(9);
		 */

		//client.get().uri("/api/v2/productos").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
		client.get().uri(url).accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON).expectBodyList(Producto.class)
				.consumeWith(response -> {
					List<Producto> productos = response.getResponseBody();
					productos.forEach(p -> {
						System.out.println(p.getNombre());
					});
					Assertions.assertThat(productos.size() > 0).isTrue();
				});
	}

	@Test
	void verTest() {
		Producto producto = service.findByNombre("Tv Panasonic Pantalla LCD").block();

		/*
		 * client.get().uri("/api/productos/{id}", Collections.singletonMap("id",
		 * producto.getId())).accept(MediaType.APPLICATION_JSON)
		 * .exchange().expectStatus().isOk().expectHeader().contentType(MediaType.
		 * APPLICATION_JSON).expectBody()
		 * .jsonPath("$.id").isNotEmpty().jsonPath("$.nombre").
		 * isEqualTo("Tv Panasonic Pantalla LCD");
		 */

		//client.get().uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		client.get().uri(url +"/{id}", Collections.singletonMap("id", producto.getId()))
				.accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody(Producto.class).consumeWith(response -> {
					Producto productos = response.getResponseBody();
					Assertions.assertThat(productos.getId()).isNotEmpty();
					Assertions.assertThat(productos.getNombre()).isEqualTo("Tv Panasonic Pantalla LCD");
					Assertions.assertThat(productos.getId().length() > 0).isTrue();
				});
	}

	@Test
	void crearTest() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		Assertions.assertThat(categoria).isNotNull();

		Producto producto = new Producto("Mesa Comedor", 100.00, categoria);

		//client.post().uri("/api/v2/productos").contentType(MediaType.APPLICATION_JSON)
		client.post().uri(url).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).body(Mono.just(producto), Producto.class).exchange()
				.expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON).expectBody()
				.jsonPath("$.producto.id").isNotEmpty().jsonPath("$.producto.nombre").isEqualTo("Mesa Comedor")
				.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
		//para version v2
		//.jsonPath("$.id").isNotEmpty().jsonPath("$.nombre").isEqualTo("Mesa Comedor")
		//.jsonPath("$.categoria.nombre").isEqualTo("Muebles");
	}

	@Test
	void crear2Test() {
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		Assertions.assertThat(categoria).isNotNull();

		Producto producto = new Producto("Mesa Comedor", 100.00, categoria);

		//client.post().uri("/api/v2/productos").contentType(MediaType.APPLICATION_JSON)
		client.post().uri(url).contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON).body(Mono.just(producto), Producto.class).exchange()
				.expectStatus().isCreated().expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>(){}).consumeWith(response -> {
					Object o = response.getResponseBody().get("producto");
					Producto p = new ObjectMapper().convertValue(o, Producto.class);
					Assertions.assertThat(p).isNotNull();
					Assertions.assertThat(p.getId()).isNotEmpty();
				});
				//para version v2
				/*.expectBody(Producto.class).consumeWith(response -> {
					Producto p = response.getResponseBody();
					Assertions.assertThat(p).isNotNull();
					Assertions.assertThat(p.getId()).isNotEmpty();
				});*/
	}

	@Test
	void editarTest() {
		Producto producto = service.findByNombre("Sony NoteBook").block();

		Categoria categoria = service.findCategoriaByNombre("Electronico").block();

		Producto productoEditado = new Producto("Asus Notebook", 700.00, categoria);

		//client.put().uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId()))
		client.put().uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(Mono.just(productoEditado), Producto.class).exchange().expectStatus().isOk().expectHeader()
				.contentType(MediaType.APPLICATION_JSON).expectBody().jsonPath("$.id").isNotEmpty().jsonPath("$.nombre")
				.isEqualTo("Asus Notebook").jsonPath("$.categoria.nombre").isEqualTo("Electronico");
	}

	@Test
	void eliminarTest() {
		Producto producto = service.findByNombre("Mica Comoda 5 Cajones").block();

		//client.delete().uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId())).exchange()
		client.delete().uri(url + "/{id}", Collections.singletonMap("id", producto.getId())).exchange()
				.expectStatus().isNoContent().expectBody().isEmpty();
		
		//client.delete().uri("/api/v2/productos/{id}", Collections.singletonMap("id", producto.getId())).exchange()
		client.delete().uri(url + "/{id}", Collections.singletonMap("id", producto.getId())).exchange()
		.expectStatus().isNotFound().expectBody().isEmpty();
	}

}
