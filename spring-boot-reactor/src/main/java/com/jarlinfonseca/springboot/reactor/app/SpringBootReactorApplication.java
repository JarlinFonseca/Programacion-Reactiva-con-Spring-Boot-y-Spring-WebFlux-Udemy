package com.jarlinfonseca.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.jarlinfonseca.springboot.reactor.app.models.Comentarios;
import com.jarlinfonseca.springboot.reactor.app.models.Usuario;
import com.jarlinfonseca.springboot.reactor.app.models.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
	
	private static final Logger log = LoggerFactory.getLogger(SpringApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		ejemploIntervalInfinito();
		
	}
	
	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(latch::countDown)
		.flatMap(i ->{
			if(i>=5) {
				return Flux.error(new InterruptedException("Solo hasta 5!"));
			}
			return Flux.just(i);
		})
		.map(i->"Hola "+i)
		.retry(2)
		.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));
		
		latch.await();
	}
	
	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));
		
		rango.subscribe();
		
		Thread.sleep(13000);

	}
	
	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith(retraso, (ra, re)-> ra)
		.doOnNext(i -> log.info(i.toString()))
		.blockLast();
	}
	
	public void ejemploZipWithRangos() {
		Flux<Integer> rangos = Flux.range(0, 4);
		Flux.just(1,2,3,4)
		  .map(i -> (i*2))
		  .zipWith(rangos, (uno, dos)-> String.format("Primer Flux: %d, Segundo Flux: %d ", uno, dos))
		  .subscribe(texto -> log.info(texto));
		
	}
	
	public void ejemploUsuarioComentariosZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()-> new Usuario("John", "Doe"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple ->{
					Usuario u = tuple.getT1();
					Comentarios c = tuple.getT2();
					return new UsuarioComentarios(u, c);
				});
		
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()-> new Usuario("John", "Doe"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono
				.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario)->new UsuarioComentarios(usuario, comentariosUsuario));
		
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	
	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()-> new Usuario("John", "Doe"));
		
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola pepe, qué tal!");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy tomando el curso de spring con reactor");
			return comentarios;
		});
		
		Mono<UsuarioComentarios> usuarioConComentarios = usuarioMono.flatMap(u -> comentariosUsuarioMono
				.map(c-> new UsuarioComentarios(u, c)));
		usuarioConComentarios.subscribe(uc -> log.info(uc.toString()));
	}
	
	
	public void ejemploCollectList() throws Exception {
			
			List<Usuario> usuariosList = new ArrayList<>();
			usuariosList.add(new Usuario("Andres", "Fonseca"));
			usuariosList.add(new Usuario("Pedro", "Fulano"));
			usuariosList.add(new Usuario("Maria", "Fulana"));
			usuariosList.add(new Usuario("Diego", "Sultano"));
			usuariosList.add(new Usuario("Juan", "Mengano"));
			usuariosList.add(new Usuario("Bruce", "Lee"));
			usuariosList.add(new Usuario("Bruce", "Willis"));
			
			Flux.fromIterable(usuariosList)
			.collectList()
			.subscribe(lista -> {
				lista.forEach(item-> log.info(item.toString()));
				});
		}
	
	public void ejemploToString() throws Exception {
		
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Fonseca"));
		usuariosList.add(new Usuario("Pedro", "Fulano"));
		usuariosList.add(new Usuario("Maria", "Fulana"));
		usuariosList.add(new Usuario("Diego", "Sultano"));
		usuariosList.add(new Usuario("Juan", "Mengano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));
		
				
		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre ->{
					if(nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					}else {
						return Mono.empty();
					}
				})
				.map(nombre ->{
					return nombre.toLowerCase();
				})
				.subscribe(u -> log.info(u.toString()));
		
		
	}
	
	public void ejemploFlatMap() throws Exception {
			
			List<String> usuariosList = new ArrayList<>();
			usuariosList.add("Andres Fonseca");
			usuariosList.add("Pedro Fulano");
			usuariosList.add("Maria Fulana");
			usuariosList.add("Diego Sultano");
			usuariosList.add("Juan Mengano");
			usuariosList.add("Bruce Lee");
			usuariosList.add("Bruce Willis");
			
					
			Flux.fromIterable(usuariosList).map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
					.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
					.flatMap(usuario ->{
						if(usuario.getNombre().equalsIgnoreCase("bruce")) {
							return Mono.just(usuario);
						}else {
							return Mono.empty();
						}
						
					}).map(usuario ->{
						String nombre = usuario.getNombre().toLowerCase();
						usuario.setNombre(nombre);
						return usuario;
					}).subscribe(u ->log.info(u.toString()));
			
			
		}
	
	public void ejemploIterable() throws Exception {
			
			List<String> usuariosList = new ArrayList<>();
			usuariosList.add("Andres Fonseca");
			usuariosList.add("Pedro Fulano");
			usuariosList.add("Maria Fulana");
			usuariosList.add("Diego Sultano");
			usuariosList.add("Juan Mengano");
			usuariosList.add("Bruce Lee");
			usuariosList.add("Bruce Willis");
			
					
			Flux<String> nombres = Flux.fromIterable(usuariosList);
			
			Flux<Usuario> usuarios= nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
					.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
					.doOnNext(usuario ->{ 
						if(usuario== null) {
							throw new RuntimeException("Nombres no pueden ser vacíos");
						}
						System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
						})
					.map(usuario ->{
						String nombre = usuario.getNombre().toLowerCase();
						usuario.setNombre(nombre);
						return usuario;
					});
					
			
			usuarios.subscribe(e ->log.info(e.toString()),
					error -> log.error(error.getMessage()),
					new Runnable() {
						
						@Override
						public void run() {
							log.info("Ha finalizado la ejecución del obvservable con éxito!");
							
						}
					});
		}
	
	


}
