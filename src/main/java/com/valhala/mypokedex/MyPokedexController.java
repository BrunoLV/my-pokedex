package com.valhala.mypokedex;

import io.micronaut.http.annotation.*;

@Controller("/my-pokedex")
public class MyPokedexController {

    @Get(uri="/", produces="text/plain")
    public String index() {
        return "Example Response";
    }
}