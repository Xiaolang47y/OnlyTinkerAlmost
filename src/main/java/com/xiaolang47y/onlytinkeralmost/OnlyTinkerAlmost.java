package com.xiaolang47y.onlytinkeralmost;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OnlyTinkerAlmost — restricts the player to Tinkers' Construct (and addon)
 * tools, weapons and armor. Vanilla / other-mod equipment can only be used for
 * crafting unless whitelisted in the config.
 *
 * @author Xiaolang47y
 */
@Mod(OnlyTinkerAlmost.MODID)
public class OnlyTinkerAlmost {
    public static final String MODID = "onlytinkeralmost";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public OnlyTinkerAlmost() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC,
                "onlytinkeralmost-common.toml");
        LOGGER.info("OnlyTinkerAlmost initialised — only Tinkers' Construct gear is usable.");
    }
}
