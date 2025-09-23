package com.valhala.mypokedex.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokemonDTO {
    public int id;
    public String name;
    public List<String> types = new ArrayList<>();
    public Map<String, Integer> base_stats = new HashMap<>();
    public Map<String, String> sprites = new HashMap<>();
    public List<String> abilities = new ArrayList<>();
    public String source_url;

    public PokemonDTO() {}
}
