package ru.vk.competition.minbenchmark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.vk.competition.minbenchmark.entity.SingleQuery;
import ru.vk.competition.minbenchmark.repository.SingleQueryRepository;


@Service
@Slf4j
@RequiredArgsConstructor
public class SingleQueryService {

    private final SingleQueryRepository queryRepository;

    public Flux<SingleQuery> getAllQueries() {
        return Mono.fromCallable(queryRepository::findAll)
                .publishOn(Schedulers.boundedElastic())
                .flatMapIterable(x -> x);
    }

    public Mono<SingleQuery> getQueryById(Integer id) {
        return Mono.fromCallable(() -> queryRepository.findByQueryId(id).orElseThrow(() -> new RuntimeException(
                String.format("Cannot find tableQuery by Id %s", id.toString())
        ))).publishOn(Schedulers.boundedElastic());
    }

    public Mono<ResponseEntity<Void>> deleteQueryById(Integer id) {
        return Mono.fromCallable(() -> {
            try {
                if(queryRepository.findByQueryId(id).map(SingleQuery::getQueryId).isEmpty()) {
                    return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
                } else {
                    queryRepository.deleteByQueryId(id);
                    return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
                }
            } catch (Exception e) {
                return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
            }
        }).publishOn(Schedulers.boundedElastic());
    }

    public Mono<ResponseEntity<Void>> addQueryWithQueryId(SingleQuery singleQuery) {
        return Mono.fromCallable(() -> {
            queryRepository.save(singleQuery);
            return new ResponseEntity<Void>(HttpStatus.CREATED);
        }).publishOn(Schedulers.boundedElastic());
    }

    public Mono<ResponseEntity<Void>> updateQueryWithQueryId(SingleQuery singleQuery) {
        return Mono.fromCallable(() -> {
            queryRepository.findByQueryId(singleQuery.getQueryId())
                    .orElseThrow(() -> new RuntimeException(
                            String.format("Cannot find tableQuery by ID %s", singleQuery.getQueryId())
                    ));
            queryRepository.save(singleQuery);
            return ResponseEntity.<Void>ok(null);
        }).publishOn(Schedulers.boundedElastic());
    }

}