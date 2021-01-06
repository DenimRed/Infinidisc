package denimred.infinidisc.config;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

import static net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import static net.minecraftforge.common.ForgeConfigSpec.Builder;

@SuppressWarnings("StaticInitializerReferencesSubClass")
// This stuff *should* only classload on one thread, so deadlocks shouldn't ever be a concern
public abstract class InfinidiscConfig {
    public static final Client CLIENT = configure(Client::new);
    protected final ModConfig.Type type;
    protected ForgeConfigSpec spec;

    InfinidiscConfig(ModConfig.Type type) {
        this.type = type;
    }

    private static <T extends InfinidiscConfig> T configure(Function<Builder, T> mapper) {
        final Pair<T, ForgeConfigSpec> pair = new Builder().configure(mapper);
        final T config = pair.getLeft();
        config.spec = pair.getRight();
        return config;
    }

    public void register(ModLoadingContext context) {
        Preconditions.checkNotNull(spec, "Spec not set; config not done building");
        context.registerConfig(type, spec);
    }

    public static final class Client extends InfinidiscConfig {
        public final BooleanValue global;

        public Client(Builder builder) {
            super(ModConfig.Type.CLIENT);
            builder.push("client");
            global = builder.comment("If true, Infinidisc music always plays globally").define("global", false);
            builder.pop();
        }
    }
}

