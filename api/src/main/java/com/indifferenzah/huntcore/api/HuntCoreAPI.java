package com.indifferenzah.huntcore.api;

import com.indifferenzah.huntcore.api.service.IGameManager;
import com.indifferenzah.huntcore.api.service.IPlayerDataLoader;

public abstract class HuntCoreAPI {

    private static HuntCoreAPI instance;

    public static HuntCoreAPI get() {
        if (instance == null) {
            throw new IllegalStateException("HuntCoreAPI has not been initialized yet.");
        }
        return instance;
    }

    protected static void set(HuntCoreAPI api) {
        instance = api;
    }

    public abstract IGameManager getGameManager();

    public abstract IPlayerDataLoader getDataLoader();
}
