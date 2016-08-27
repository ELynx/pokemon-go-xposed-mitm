package com.elynx.pogoxmitm;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.github.aeonlucid.pogoprotos.Data;
import com.github.aeonlucid.pogoprotos.Enums;
import com.github.aeonlucid.pogoprotos.Inventory;
import com.github.aeonlucid.pogoprotos.data.Capture;
import com.github.aeonlucid.pogoprotos.inventory.Item;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that shows IVs for pokemon in nickname
 */
public class IvHack {
    public static boolean isMonitoredType(Requests.RequestType type) {
        if (type == Requests.RequestType.GET_INVENTORY) {
            return true;
        }

        if (type == Requests.RequestType.UPGRADE_POKEMON) {
            return true;
        }

        if (type == Requests.RequestType.EVOLVE_POKEMON) {
            return true;
        }

        return false;
    }

    public static ByteString hack(ByteString response, Requests.RequestType type) throws InvalidProtocolBufferException {
        if (type == Requests.RequestType.GET_INVENTORY) {
            return hackGetInventory(response);
        }

        if (type == Requests.RequestType.UPGRADE_POKEMON) {
            return hackUpgradePokemon(response);
        }

        if (type == Requests.RequestType.EVOLVE_POKEMON) {
            return hackEvolvePokemon(response);
        }

        return null;
    }

    protected static ByteString hackUpgradePokemon(ByteString response) throws InvalidProtocolBufferException {
        Responses.UpgradePokemonResponse.Builder upgradeBuilder = Responses.UpgradePokemonResponse.parseFrom(response).toBuilder();

        if (upgradeBuilder.getResult() == Responses.UpgradePokemonResponse.Result.SUCCESS) {
            if (upgradeBuilder.hasUpgradedPokemon()) {
                Data.PokemonData.Builder pokeBuilder = upgradeBuilder.getUpgradedPokemon().toBuilder();

                if (!pokeBuilder.getIsEgg()) {
                    pokeBuilder = makeIvNickname(pokeBuilder);

                    upgradeBuilder.setUpgradedPokemon(pokeBuilder.build());

                    return upgradeBuilder.build().toByteString();
                }
            }
        }

        return null;
    }

    protected static ByteString hackEvolvePokemon(ByteString response) throws InvalidProtocolBufferException {
        Responses.EvolvePokemonResponse.Builder evolveBuilder = Responses.EvolvePokemonResponse.parseFrom(response).toBuilder();

        if (evolveBuilder.getResult() == Responses.EvolvePokemonResponse.Result.SUCCESS) {
            if (evolveBuilder.hasEvolvedPokemonData()) {
                Data.PokemonData.Builder pokeBuilder = evolveBuilder.getEvolvedPokemonData().toBuilder();

                if (!pokeBuilder.getIsEgg()) {
                    pokeBuilder = makeIvNickname(pokeBuilder);

                    evolveBuilder.setEvolvedPokemonData(pokeBuilder.build());

                    return evolveBuilder.build().toByteString();
                }
            }
        }

        return null;
    }

    protected static ByteString hackGetInventory(ByteString response) throws InvalidProtocolBufferException {
        Responses.GetInventoryResponse.Builder inventoryResponseBuilder = Responses.GetInventoryResponse.parseFrom(response).toBuilder();

        if (inventoryResponseBuilder.hasInventoryDelta()) {
            boolean deltaChanged = false;
            ArrayList<HashMap<String, String>> pokemonData = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> candyData = new HashMap<String, String>();

            Inventory.InventoryDelta.Builder inventoryDeltaBuilder = inventoryResponseBuilder.getInventoryDelta().toBuilder();

            for (int invItemNo = 0; invItemNo < inventoryDeltaBuilder.getInventoryItemsCount(); ++invItemNo) {
                Inventory.InventoryItem.Builder invItemBuilder = inventoryDeltaBuilder.getInventoryItems(invItemNo).toBuilder();

                if (invItemBuilder.hasInventoryItemData()) {
                    Inventory.InventoryItemData.Builder invItemDataBuilder = invItemBuilder.getInventoryItemData().toBuilder();

                    if (Injector.doCsvHack) {
                        if (invItemDataBuilder.hasCandy()) {
                            String candies = invItemDataBuilder.getCandy().toBuilder().getCandy() + "";
                            String family = invItemDataBuilder.getCandy().toBuilder().getFamilyIdValue() + "";
                            candyData.put(family, candies);
                        }
                    }

                    if (invItemDataBuilder.hasPokemonData()) {
                        Data.PokemonData.Builder pokeBuilder = invItemDataBuilder.getPokemonData().toBuilder();

                        if (pokeBuilder.getIsEgg())
                            continue; // with another item

                        int atk = pokeBuilder.getIndividualAttack();
                        int def = pokeBuilder.getIndividualDefense();
                        int sta = pokeBuilder.getIndividualStamina();
                        int total = (atk + def + sta) * 100 / 45;

                        if (Injector.doCsvHack) {
                            HashMap<String, String> data = new HashMap<String, String>();
                            data.put("id", pokeBuilder.getPokemonIdValue() + "");
                            data.put("cp", pokeBuilder.getCp() + "");
                            data.put("family", "");
                            data.put("candies", "");
                            data.put("favourite", pokeBuilder.getFavorite() + "");
                            data.put("iv", total + "");
                            data.put("move1", pokeBuilder.getMove1().name() + "");
                            data.put("move2", pokeBuilder.getMove2().name() + "");
                            pokemonData.add(data);
                        }

                        pokeBuilder = makeIvNickname(pokeBuilder);

                        invItemDataBuilder.setPokemonData(pokeBuilder.build());
                        invItemBuilder.setInventoryItemData(invItemDataBuilder.build());
                        inventoryDeltaBuilder.setInventoryItems(invItemNo, invItemBuilder.build());

                        deltaChanged = true;
                    }
                }
            } //end of delta->items

            if (deltaChanged) {

                if (Injector.doCsvHack) {
                    try {
                        exportPokemonData(pokemonData, candyData);
                    } catch (Exception ex) {
                        XposedBridge.log("[ERROR] " + ex.getMessage());
                    }
                }

                inventoryResponseBuilder.setInventoryDelta(inventoryDeltaBuilder.build());
                return inventoryResponseBuilder.build().toByteString();
            }
        } // end of delta

        return null;
    }

    protected static String getFamilyId(String id) {
        int pokeId = Integer.parseInt(id);
        int familyId = 0;

        // fix for incorrect hypno family
        if (pokeId == Enums.PokemonId.HYPNO_VALUE) {
            return Enums.PokemonFamilyId.FAMILY_DROWZEE_VALUE + "";
        }

        // this might be stupid but it works
        if (Enums.PokemonFamilyId.valueOf(pokeId) != null) {
            familyId = Enums.PokemonFamilyId.valueOf(pokeId).getNumber();
        } else if (Enums.PokemonFamilyId.valueOf(pokeId - 1) != null) {
            familyId = Enums.PokemonFamilyId.valueOf(pokeId - 1).getNumber();
        } else if (Enums.PokemonFamilyId.valueOf(pokeId - 2) != null) {
            familyId = Enums.PokemonFamilyId.valueOf(pokeId - 2).getNumber();
        }
        return familyId + "";
    }

    protected static void exportPokemonData(ArrayList<HashMap<String, String>> pokemonData, HashMap<String, String> candyData) throws Exception {
        if (!Injector.doCsvHack || pokemonData.size() < 2) {
            return;
        }

        Collections.sort(pokemonData, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> map1, HashMap<String, String> map2) {
                return Double.compare(Integer.parseInt(map1.get("id")), Integer.parseInt(map2.get("id")));
            }
        });

        for (HashMap<String, String> map : pokemonData) {
            String family = getFamilyId(map.get("id"));
            String candies = candyData.get(family);
            //XposedBridge.log("[family] : " + map.get("id") + " - " + family + " [candies] : " + candies);
            map.put("candies", candies);
            map.put("family", family);
        }

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

        file.createNewFile();
        file.setWritable(true);

        FileWriter outputStream = new FileWriter(file);
        PrintWriter out = new PrintWriter(outputStream);

        out.println("ID,Family,Candy,Favourite,CP,IV,Move1,Move2");

        for (HashMap<String, String> map : pokemonData) {
            String text = map.get("id") + "," + map.get("family") + "," + map.get("candies") + "," + map.get("favourite") + "," + map.get("cp") +
                    "," + map.get("iv") + "," + map.get("move1") + "," + map.get("move2");

            out.println(text);
        }

        out.flush();
        out.close();
        outputStream.close();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AndroidAppHelper.currentApplication().getBaseContext(), "Pokemon.csv saved!", Toast.LENGTH_SHORT).show();

                XposedBridge.log("### [PoGo-MITM] Pokemon.csv saved!");
            }
        });
    }

    protected static Data.PokemonData.Builder makeIvNickname(Data.PokemonData.Builder pokeBuilder) {
        int atk = pokeBuilder.getIndividualAttack();
        int def = pokeBuilder.getIndividualDefense();
        int sta = pokeBuilder.getIndividualStamina();

        int total = (atk + def + sta) * 100 / 45;

        /*Item.ItemId ballId = pokeBuilder.getPokeball();
        String ballStr = "";

        switch (ballId)
        {
            case ITEM_GREAT_BALL:
                ballStr += " G";
                break;

            case ITEM_MASTER_BALL:
                ballStr += " M";
                break;

            case ITEM_POKE_BALL:
                //ballStr += " P";
                break;

            case ITEM_ULTRA_BALL:
                ballStr += " U";
                break;
        }

        int battlesAttacked = pokeBuilder.getBattlesAttacked();
        int battlesDefended = pokeBuilder.getBattlesDefended();
        String battleString = "";

        if (battlesAttacked > 0)
            battleString += " a" + Integer.toString(battlesAttacked);

        if (battlesDefended > 0)
            battleString += " d" + Integer.toString(battlesDefended);*/

        // For A-Z sorting to see best ones at glance
        String prefix;
        if (total > 89)
            prefix = "A";
        else if (total > 79)
            prefix = "B";
        else if (total > 69)
            prefix = "C";
        else if (total > 59)
            prefix = "D";
        else if (total > 49)
            prefix = "E";
        else if (total > 39)
            prefix = "F";
        else if (total > 29)
            prefix = "G";
        else if (total > 19)
            prefix = "H";
        else if (total > 9)
            prefix = "I";
        else
            prefix = "J";

        String nickname = prefix + " " +
                Integer.toString(total) + "%" +
                " A" + Integer.toString(atk) +
                " D" + Integer.toString(def) +
                " S" + Integer.toString(sta);/* +
                battleString +
                ballStr;*/

        pokeBuilder.setNickname(nickname);

        return pokeBuilder;
    }
}
