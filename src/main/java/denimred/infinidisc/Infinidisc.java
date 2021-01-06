package denimred.infinidisc;

import denimred.infinidisc.config.InfinidiscConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(Infinidisc.MOD_ID)
public class Infinidisc {
    public static final String MOD_ID = "infinidisc";

    public Infinidisc() {
        InfinidiscConfig.CLIENT.register(ModLoadingContext.get());
    }
}
