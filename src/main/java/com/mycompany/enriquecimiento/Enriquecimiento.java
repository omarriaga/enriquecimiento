/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.enriquecimiento;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;

/**
 *
 * @author juan
 */
public class Enriquecimiento {

    public static void main(String[] args) {
        Enriquecimiento objeto = new Enriquecimiento();
        objeto.run();

    }

    public void run() {
        try {
            PrintWriter tripletas = new PrintWriter(new File("/Users/juan/data/album.ttl"));
            List<Same> resources = queryMusica("musica:Album");
            resources.forEach((same) -> {
                llenarDatosAlbum(same).forEach((data) -> {
                    tripletas.println(data);
                });
            });
        } catch (FileNotFoundException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

    }

    public List<Same> queryMusica(String type) {
        String query = "prefix musica: <http://www.uniandes.web.semantica.example.org/201528630/etapa2ontology#>\n"
                + "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                + "prefix owl: <http://www.w3.org/2002/07/owl#> \n"
                + "SELECT ?sujeto ?same \n"
                + "WHERE {\n"
                + "    ?sujeto rdf:type " + type + ". \n"
                + "    ?sujeto owl:sameAs ?same . \n"
                + "}";
        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        System.out.println(qs);

        List<Same> data;
        // Normally you'd just do results = exec.execSelect(), but I want to
        // use this ResultSet twice, so I'm making a copy of it.
        try (QueryExecution exec = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", qs.asQuery())) {
            // Normally you'd just do results = exec.execSelect(), but I want to
            // use this ResultSet twice, so I'm making a copy of it.
            ResultSet results = ResultSetFactory.copyResults(exec.execSelect());
            data = new LinkedList<>();
            while (results.hasNext()) {
                QuerySolution result = results.next();
                Same same = new Same();
                same.setSame(result.getResource("same").getURI());
                same.setSujeto(result.getResource("sujeto").getURI());
                data.add(same);
            }   // A simpler way of printing the results.
            ResultSetFormatter.out(results);
        }

        return data;
    }

    public List<String> llenarDatosAlbum(Same sujeto) {
        //http://dbpedia.org/ontology/
        String template = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
                + "prefix dbo: <http://dbpedia.org/ontology/> \n"
                + "SELECT ?date \n"
                + "WHERE {\n"
                + "   <*sujeto*> rdf:type dbo:Album . \n"
                + "   <*sujeto*> dbo:releaseDate ?date . \n"
                + "}";
        String query = template.replace("*sujeto*", sujeto.getSame());
        ParameterizedSparqlString qs = new ParameterizedSparqlString(query);

        System.out.println(qs);

        List<String> data;
        // Normally you'd just do results = exec.execSelect(), but I want to
        // use this ResultSet twice, so I'm making a copy of it.
        try (QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery())) {
            // Normally you'd just do results = exec.execSelect(), but I want to
            // use this ResultSet twice, so I'm making a copy of it.
            ResultSet results = ResultSetFactory.copyResults(exec.execSelect());
            data = new LinkedList<>();
            while (results.hasNext()) {
                // As RobV pointed out, don't use the `?` in the variable
                // name here. Use *just* the name of the variable.
                QuerySolution result = results.next();
                if (!result.getLiteral("date").getString().isEmpty()) {
                    String release = "<"+sujeto.getSujeto()+">" + " <http://www.uniandes.web.semantica.example.org/201528630/etapa2ontology#Anio_album> "
                            + "\""+result.getLiteral("date").getString()+"\"^^<http://www.w3.org/2001/XMLSchema#string> .";
                    data.add(release);
                }
                
                
                

            }   // A simpler way of printing the results.
            ResultSetFormatter.out(results);
        }

        return data;

    }

}
