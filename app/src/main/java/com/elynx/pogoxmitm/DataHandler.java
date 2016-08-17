package com.elynx.pogoxmitm;

import com.github.aeonlucid.pogoprotos.Data;
import com.github.aeonlucid.pogoprotos.Inventory;
import com.github.aeonlucid.pogoprotos.Settings;
import com.github.aeonlucid.pogoprotos.networking.Envelopes;
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
    public static boolean doSpeedHack = true;

    /**
     * Processes single package going from client to server
     * data is created by allocate and had to have array
     * data is given here completely for destruction, it will be thrown away if false is returned
     * parseFrom seems to consume content of data, so unless it is reassembled DO return false,
     * or nothing will reach actual net code
     *
     * @param data Data buffer to be processed
     * @return True if data was changed, false otherwise
     */
    public static boolean processOutboundPackage(ByteBuffer data) throws Throwable {
        Envelopes.RequestEnvelope request = Envelopes.RequestEnvelope.parseFrom(data.array());

        RpcContext context = Injector.rpcContext.get();

        String dump = "Request types";

        context.serverRequestTypes.clear();
        for (Requests.Request singleRequest : request.getRequestsList()) {
            context.serverRequestTypes.add(singleRequest.getRequestType());
            dump += " " + Integer.toString(singleRequest.getRequestTypeValue());
        }

        XposedBridge.log("[PoGo-MITM INFO] " + dump);

        return false;
    }

    /**
     * Processes single package going from server to client
     * data is created by allocate and had to have array
     * data is given here completely for destruction, it will be thrown away if false is returned
     * parseFrom seems to consume content of data, so unless it is reassembled DO return false,
     * or nothing will reach actual client code
     *
     * @param data Data buffer to be processed
     * @return True if data was changed, false otherwise
     */
    public static boolean processInboundPackage(ByteBuffer data) throws Throwable {
        if (!doIvHack && !doSpeedHack)
            return false;

        RpcContext context = Injector.rpcContext.get();

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

        XposedBridge.log("[PoGo-MITM INFO] Attempt to modify " + Boolean.toString(canBeModified));

        if (!canBeModified)
            return false;

        boolean wasModified = false;

        try {
            Envelopes.ResponseEnvelope.Builder response = Envelopes.ResponseEnvelope.parseFrom(data.array()).toBuilder();

            if (response.getReturnsCount() != context.serverRequestTypes.size()) {
                XposedBridge.log("[PoGo-MITM ERROR] Request was " + Integer.toString(context.serverRequestTypes.size()) +
                        " but response is " + Integer.toString(response.getReturnsCount()));

                return false;
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

                                        String nickname = Integer.toString(total) + "%" +
                                                " A" + Integer.toString(atk) +
                                                " D" + Integer.toString(def) +
                                                " S" + Integer.toString(sta);

                                        pokeBuilder.setNickname(nickname);

                                        invItemDataBuilder.setPokemonData(pokeBuilder.build());
                                        invItemBuilder.setInventoryItemData(invItemDataBuilder.build());
                                        inventoryDeltaBuilder.setInventoryItems(j, invItemBuilder.build());

                                        deltaHasPokemon = true;

                                        XposedBridge.log("[PoGo-MITM] INFO " + nickname);
                                    }
                                }
                            }

                            if (deltaHasPokemon)
                            {
                                inventoryResponseBuilder.setInventoryDelta(inventoryDeltaBuilder.build());
                                invResponseModified = true;
                            }
                        }

                        if (invResponseModified)
                        {
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

                        XposedBridge.log("[PoGo-MITM INFO] Change driving from " + Float.toString(oldLimit) + " to " + Float.toString(newLimit));

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
                byte[] modified = response.build().toByteArray();
                data = ByteBuffer.wrap(modified);
                return true;
            }
        } catch (InvalidProtocolBufferException e) {
            XposedBridge.log(e);
        }

        return false;
    }
}
