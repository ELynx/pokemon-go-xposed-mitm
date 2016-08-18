package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.Data;
import com.github.aeonlucid.pogoprotos.Inventory;
import com.github.aeonlucid.pogoprotos.Settings;
import com.github.aeonlucid.pogoprotos.networking.Envelopes.RequestEnvelope;
import com.github.aeonlucid.pogoprotos.networking.Envelopes.ResponseEnvelope;
import com.github.aeonlucid.pogoprotos.networking.Requests;
import com.github.aeonlucid.pogoprotos.networking.Responses;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

import de.robv.android.xposed.XposedBridge;

/**
 * Class that does actual manipulations on data
 * Should be made reentrant and synchronized, since it is called from threads
 */
public class DataHandler {
    // TODO actually connect to settings...
    public static boolean doIvHack = true;
    public static boolean doSpeedHack = false;

    /**
     * Processes single package going from client to server
     * data is created by allocate and had to have array
     * data is given here completely for destruction, it will be thrown away if false is returned
     * parseFrom seems to consume content of data, so unless it is reassembled DO return false,
     * or nothing will reach actual net code
     *
     * @param dataIn Data buffer to be processed
     * @return True if data was changed, false otherwise
     */
    public static ByteBuffer processOutboundPackage(ByteBuffer dataIn) throws Throwable {
        RpcContext context = Injector.rpcContext.get();
        context.serverRequestTypes.clear();

        try {
            RequestEnvelope request = RequestEnvelope.parseFrom(dataIn.array());

            for (Requests.Request singleRequest : request.getRequestsList()) {
                context.serverRequestTypes.add(singleRequest.getRequestType());
            }
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        }

        return null;
    }

    /**
     * Processes single package going from server to client
     * data is created by allocate and had to have array
     * data is given here completely for destruction, it will be thrown away if false is returned
     * parseFrom seems to consume content of data, so unless it is reassembled DO return false,
     * or nothing will reach actual client code
     *
     * @param dataIn Data buffer to be processed
     * @return True if data was changed, false otherwise
     */
    public static ByteBuffer processInboundPackage(ByteBuffer dataIn) throws Throwable {
        if (!doIvHack && !doSpeedHack)
            return null;

        RpcContext context = Injector.rpcContext.get();

        //<< this is only for performance, if parsing is slow
        boolean canBeModified = false;

        for (Requests.RequestType type : context.serverRequestTypes) {
            if (type == Requests.RequestType.GET_INVENTORY) {
                if (doIvHack) {
                    canBeModified = true;
                    break;
                }
            }

            if (type == Requests.RequestType.DOWNLOAD_SETTINGS) {
                if (doSpeedHack) {
                    canBeModified = true;
                    break;
                }
            }
        }

        if (!canBeModified)
            return null;
        //>>

        boolean wasModified = false;

        try {
            ResponseEnvelope.Builder response = ResponseEnvelope.parseFrom(dataIn.array()).toBuilder();

            // TODO why this is happening? some requests don't end in returns?
            if (response.getReturnsCount() != context.serverRequestTypes.size()) {

                if (BuildConfig.DEBUG) {
                    String infoDump = "[PoGo-MITM ERROR] Request for [" + Integer.toString(context.serverRequestTypes.size()) +
                            "] items but response is [" + Integer.toString(response.getReturnsCount()) + "] items\n";

                    infoDump += "Requested";

                    for (Requests.RequestType type : context.serverRequestTypes) {
                        infoDump += " " + type.toString();
                    }

                    infoDump += "\nResponded\n" + response.toString();

                    XposedBridge.log(infoDump);
                }

                return null;
            }

            if (doIvHack) {
                for (int i = 0; i < context.serverRequestTypes.size(); ++i) {
                    if (context.serverRequestTypes.get(i) == Requests.RequestType.GET_INVENTORY) {
                        ByteString buf = response.getReturns(i);

                        // OMG...
                        boolean invResponseModified = false;
                        Responses.GetInventoryResponse.Builder inventoryResponseBuilder = Responses.GetInventoryResponse.parseFrom(buf).toBuilder();

                        if (inventoryResponseBuilder.hasInventoryDelta()) {
                            boolean deltaHasPokemon = false;
                            Inventory.InventoryDelta.Builder inventoryDeltaBuilder = inventoryResponseBuilder.getInventoryDelta().toBuilder();

                            for (int j = 0; j < inventoryDeltaBuilder.getInventoryItemsCount(); ++j) {
                                Inventory.InventoryItem.Builder invItemBuilder = inventoryDeltaBuilder.getInventoryItems(j).toBuilder();

                                if (invItemBuilder.hasInventoryItemData()) {
                                    Inventory.InventoryItemData.Builder invItemDataBuilder = invItemBuilder.getInventoryItemData().toBuilder();

                                    if (invItemDataBuilder.hasPokemonData()) {
                                        Data.PokemonData.Builder pokeBuilder = invItemDataBuilder.getPokemonData().toBuilder();

                                        int atk = pokeBuilder.getIndividualAttack();
                                        int def = pokeBuilder.getIndividualDefense();
                                        int sta = pokeBuilder.getIndividualStamina();

                                        int total = (atk + def + sta) * 100 / 45;

                                        String oldNickname = pokeBuilder.getNickname();

                                        String nickname = Integer.toString(total) + "%" +
                                                " A" + Integer.toString(atk) +
                                                " D" + Integer.toString(def) +
                                                " S" + Integer.toString(sta);

                                        pokeBuilder.setNickname(nickname);

                                        invItemDataBuilder.setPokemonData(pokeBuilder.build());
                                        invItemBuilder.setInventoryItemData(invItemDataBuilder.build());
                                        inventoryDeltaBuilder.setInventoryItems(j, invItemBuilder.build());

                                        deltaHasPokemon = true;

                                        if (BuildConfig.DEBUG) {
                                            XposedBridge.log("[PoGo-MITM] INFO <" + oldNickname + "> " + nickname);
                                        }
                                    }
                                }
                            } //end of delta->items

                            if (deltaHasPokemon) {
                                inventoryResponseBuilder.setInventoryDelta(inventoryDeltaBuilder.build());
                                invResponseModified = true;
                            }
                        } //end of delta

                        if (invResponseModified) {
                            buf = inventoryResponseBuilder.build().toByteString();
                            response.setReturns(i, buf);

                            wasModified = true;
                        }
                    }
                }
            }

            if (doSpeedHack) {
                for (int i = 0; i < context.serverRequestTypes.size(); ++i) {
                    if (context.serverRequestTypes.get(i) == Requests.RequestType.DOWNLOAD_SETTINGS) {
                        ByteString buf = response.getReturns(i);

                        // there
                        Responses.DownloadSettingsResponse.Builder settingsBuilder = Responses.DownloadSettingsResponse.parseFrom(buf).toBuilder();
                        Settings.GlobalSettings.Builder globalSettingsBuilder = settingsBuilder.getSettings().toBuilder();
                        Settings.GpsSettings.Builder gpsSettingsBuilder = globalSettingsBuilder.getGpsSettings().toBuilder();

                        // make hack
                        float oldLimit = gpsSettingsBuilder.getDrivingWarningSpeedMetersPerSecond();
                        gpsSettingsBuilder.setDrivingWarningSpeedMetersPerSecond(340.0f); // warn on one mach
                        float newLimit = gpsSettingsBuilder.getDrivingWarningSpeedMetersPerSecond();

                        if (BuildConfig.DEBUG) {
                            XposedBridge.log("[PoGo-MITM INFO] Change driving from " + Float.toString(oldLimit) + " to " + Float.toString(newLimit));
                        }

                        // and back
                        globalSettingsBuilder.setGpsSettings(gpsSettingsBuilder.build());
                        settingsBuilder.setSettings(globalSettingsBuilder.build());

                        buf = settingsBuilder.build().toByteString();
                        response.setReturns(i, buf);

                        wasModified = true;
                    }
                }
            }

            if (wasModified) {
                return ByteBuffer.wrap(response.build().toByteArray());
            }
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        }

        return null;
    }
}
