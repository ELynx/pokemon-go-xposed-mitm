package com.elynx.pogoxmitm.module;

import com.github.aeonlucid.pogoprotos.networking.Requests;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class that supervises several ModuleManagers
 * Provide single entry point for MitmProvider into installed hacks
 *
 * TODO How to inregrate this into GUI - list of modules that are OK or not
 */
public class ModuleSupervisor {
    List<ModuleManager> modules;
    Set<Requests.RequestType> packageTypes;

    /**
     * Private constructor because Singleton
     */
    private ModuleSupervisor() {
        modules = new ArrayList<>();

        packageTypes = new HashSet<>();
    }

    /**
     * Holder for Singleton, see MitmProvider for "inspiration" sources
     */
    private static class Holder {
        private static final ModuleSupervisor INSTANCE = new ModuleSupervisor();
    }

    /**
     * Get instance of ModuleSupervisor
     *
     * @return Singleton of ModuleSupervisor
     */
    public static ModuleSupervisor getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Add module to active modules
     *
     * Module will be checked against already added modules
     * Module will be init()-ialized
     *
     * @param module Module to be added
     * @return True if module was added, False otherwise
     */
    public boolean addModule(ModuleManager module) {
        for (ModuleManager m : modules) {
            if (module.moduleId() == m.moduleId()) {
                return false;
            }
        }

        if (!module.init())
            return false;

        modules.add(module);

        updatePackageTypes();

        return true;
    }

    public ByteBuffer processRequest(Requests.Request request, int exchangeId, boolean connectionOk) {
        //TODO
        return null;
    }

    public ByteBuffer processResponse(){
        //TODO
        return null;
    }

    /**
     * Update package types that needs to be monitored
     */
    protected void updatePackageTypes() {
        packageTypes.clear();

        for (ModuleManager m : modules) {
            packageTypes.addAll(m.requestTypes());
            packageTypes.addAll(m.responseTypes());
        }
    }
}
