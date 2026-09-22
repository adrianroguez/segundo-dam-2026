package com.codelearn.tareas;

import com.google.gson.Gson;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        var datos = Map.of("titulo", "Aprender Maven", "completada", false);
        System.out.println(new Gson().toJson(datos));
    }
}