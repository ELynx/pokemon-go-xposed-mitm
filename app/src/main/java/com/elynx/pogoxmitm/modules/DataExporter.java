package com.elynx.pogoxmitm.modules;

import android.text.TextUtils;

import com.github.aeonlucid.pogoprotos.Data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.robv.android.xposed.XposedBridge;

public class DataExporter extends ModuleBase {
    protected ArrayList<PokemonExportData> pokemonData = new ArrayList<PokemonExportData>();
    protected HashMap<Integer, Integer> candyData = new HashMap<Integer, Integer>();

    public void addCandyData(int familyId, int candies) {
        candyData.put(familyId, candies);
    }

    public void addPokemonData(Data.PokemonData pokemonData1) {
        PokemonExportData pokemon = new PokemonExportData();
        pokemon.setId(pokemonData1.getPokemonIdValue());
        pokemon.setFamilyFromPokemonId(pokemonData1.getPokemonIdValue());
        pokemon.setCp(pokemonData1.getCp());
        pokemon.setAttack(pokemonData1.getIndividualAttack());
        pokemon.setDefence(pokemonData1.getIndividualDefense());
        pokemon.setStamina(pokemonData1.getIndividualStamina());
        pokemon.setMoveQuick(pokemonData1.getMove1().name());
        pokemon.setMoveCharge(pokemonData1.getMove2().name());
        pokemon.setFavourite(pokemonData1.getFavorite() == 1);
        pokemon.setCandies(0);
        pokemon.setFromEgg(pokemonData1.getFromFort() == 1);
        pokemon.setNumUpgrades(pokemonData1.getNumUpgrades());

        pokemonData.add(pokemon);
    }

    public void run() {
        try {
            exportPokemonData();
        } catch (Exception ex) {
            XposedBridge.log(ex);
        }

    }

    protected void exportPokemonData() throws Exception {
        // don't export when viewing single pokemon or there are 0 pokemon
        if (this.pokemonData.size() < 2) {
            return;
        }

        exportTsv(this.pokemonData);
    }

    protected void prepareData() {
        Collections.sort(this.pokemonData);

        for (PokemonExportData pokemon : pokemonData) {
            int family = pokemon.getFamily();

            if (candyData.containsKey(family)) {
                pokemon.setCandies(candyData.get(family));
            }
            else
            {
                pokemon.setCandies(0); // :'(
            }
        }
    }

    protected String getColumnsString() {
        String[] columns = {
                "ID", "Family", "Candies", "Favourite",
                "CP", "IV", "Attack", "Defence",
                "Stamina", "Move Quick", "Move Charge",
                "From egg", "Number of upgrades"
        };

        return TextUtils.join("\t", columns);
    }

    protected String getPokemonDataString(PokemonExportData pokemon) {
        String[] data = {
                pokemon.getId() + "", pokemon.getFamily() + "", pokemon.getCandies() + "",
                pokemon.isFavourite() ? "1" : "0", pokemon.getCp() + "", pokemon.getIv() + "",
                pokemon.getAttack() + "", pokemon.getDefence() + "", pokemon.getStamina() + "",
                pokemon.getMoveQuick(), pokemon.getMoveCharge(), pokemon.isFromEgg() ? "1" : "0",
                pokemon.getNumUpgrades() + ""
        };
        return TextUtils.join("\t", data);
    }

    protected void exportTsv(ArrayList<PokemonExportData> pokemonData) {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "PokemonData.tsv";
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
            XposedBridge.log(ex);
            showToast("Error when saving TSV!");
        }

        showToast(fileName + " saved!");
    }
}
