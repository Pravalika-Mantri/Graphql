package com.spring.graphql.api.controller;

import com.spring.graphql.api.entity.Person;
import com.spring.graphql.api.repository.PersonRepository;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("person")
public class PersonController {
    @Autowired
    private PersonRepository personRepository;

    @Value("classpath:person.graphqls")
    private Resource schemaResource;

    private GraphQL graphQL;

    @PostConstruct
    public void loadSchema() throws IOException {
        File schemaFile = schemaResource.getFile();
        TypeDefinitionRegistry registry = new SchemaParser().parse(schemaFile);
        RuntimeWiring wiring = buildWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(registry,wiring);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    private RuntimeWiring buildWiring() {
        DataFetcher<List<Person>> fetcher1=data->{
            return personRepository.findAll();
        };
        DataFetcher<Person> fetcher2=data->{
            return personRepository.findByName(data.getArgument("name"));
        };
        return RuntimeWiring.newRuntimeWiring().type("Query",
                typeWriting -> typeWriting.dataFetcher("getAllPerson",fetcher1).dataFetcher("findPerson", fetcher2)).build();


    }

    @PostMapping("/getAll")
    public ResponseEntity<Object> getAll(@RequestBody String query){
        ExecutionResult result = graphQL.execute(query);
        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }

    @PostMapping("/persons")
    public void persons(@RequestBody List<Person> persons){
        personRepository.saveAll(persons);
    }

    @PostMapping
    public void saveperson(@RequestBody  Person person){
        personRepository.save(person);
    }

    @PostMapping("savepersons")
    public void savepersons(@RequestBody  List<Person> persons){
        personRepository.saveAll(persons);
    }

    @GetMapping
    public List<Person> findPersons(){
        return personRepository.findAll();
    }
}
