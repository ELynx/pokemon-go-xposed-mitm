package com.elynx.pogoxmitm.modules;

import android.text.TextUtils;

import com.elynx.pogoxmitm.Injector;
import com.github.aeonlucid.pogoprotos.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DataExporter extends ModuleBase {
    protected ArrayList<PokemonExportData> pokemonData = new ArrayList<PokemonExportData>();
    protected HashMap<Integer, Integer> candyData = new HashMap<Integer, Integer>();

    public void addCandyData(int familyId, int candies) {
        candyData.put(familyId, candies);
    }

    public void addPokemonData(Data.PokemonData.Builder pokemonBuilder) {
        PokemonExportData pokemon = new PokemonExportData();
        pokemon.setId(pokemonBuilder.getPokemonIdValue());
        pokemon.setFamilyFromPokemonId(pokemonBuilder.getPokemonIdValue());
        pokemon.setCp(pokemonBuilder.getCp());
        pokemon.setAttack(pokemonBuilder.getIndividualAttack());
        pokemon.setDefence(pokemonBuilder.getIndividualDefense());
        pokemon.setStamina(pokemonBuilder.getIndividualStamina());
        pokemon.setMoveQuick(pokemonBuilder.getMove1().name());
        pokemon.setMoveCharge(pokemonBuilder.getMove2().name());
        pokemon.setFavourite(pokemonBuilder.getFavorite() == 1);
        pokemon.setCandies(0);
        pokemonData.add(pokemon);
    }

    public void run() {
        if (!Injector.doExportHack) {
            return;
        }

        try {
            exportPokemonData();
        } catch (Exception ex) {
            log("[ERROR] " + ex.getMessage());
        }

    }

    protected void exportPokemonData() throws Exception {
        // don't export when viewing single pokemon or there are 0 pokemon
        if (this.pokemonData.size() < 2) {
            return;
        }

        exportCsv(this.pokemonData);
    }

    protected void prepareData() {
        Collections.sort(this.pokemonData);

        for (PokemonExportData pokemon : pokemonData) {
            pokemon.setCandies(candyData.get(pokemon.getFamily()));
        }
    }

    protected String getColumnsString() {
        String[] columns = {
                "ID", "Family", "Candies", "Favourite",
                "CP", "IV", "Attack", "Defence",
                "Stamina", "Move Quick", "Move Charge"
        };

        return TextUtils.join(",", columns);
    }

    protected String getPokemonDataString(PokemonExportData pokemon) {
        String[] data = {
                pokemon.getId() + "", pokemon.getFamily() + "", pokemon.getCandies() + "",
                pokemon.isFavourite() ? "1" : "0", pokemon.getCp() + "", pokemon.getIv() + "",
                pokemon.getAttack() + "", pokemon.getDefence() + "", pokemon.getStamina() + "",
                pokemon.getMoveQuick(), pokemon.getMoveCharge()
        };
        return TextUtils.join(",", data);
    }

    protected void exportCsv(ArrayList<PokemonExportData> pokemonData) {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "PokemonData.csv";
        File folder = new File(baseDir + File.separator + "Pokemon");
        File file = new File(folder.getAbsolutePath() + File.separator + fileName);

        if (!folder.exists()) {
            folder.mkdir();
        }

        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();

            FileWriter outputStream = new FileWriter(file);
            PrintWriter out = new PrintWriter(outputStream);

            out.println(getColumnsString());

            prepareData();

            for (PokemonExportData pokemon : pokemonData) {
                out.println(getPokemonDataString(pokemon));
            }

            out.flush();
            out.close();
            outputStream.close();
        } catch (IOException ex) {
            log("[ERROR] " + ex.getMessage());
            showToast("Error when saving CSV!");
        }

        showToast("Pokemon.csv saved!");
    }
}