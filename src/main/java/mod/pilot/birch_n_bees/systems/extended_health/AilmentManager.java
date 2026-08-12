package mod.pilot.birch_n_bees.systems.extended_health;

import mod.pilot.birch_n_bees.systems.extended_health.types.OhOuchMyBones;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class AilmentManager {
    private static final int NATIVE_AILMENT_COUNT = 0;
    public static void init(IEventBus bus){
        registered = new Ailment[NATIVE_AILMENT_COUNT];
        bus.addListener(AilmentManager::registerNativeAilments);
        bus.post(new RegisterAilmentsEvent());
    }
    private static Ailment[] registered;
    private static int count = 0;
    @SuppressWarnings("unchecked")
    private static <A extends Ailment> A register_INTERNAL(Supplier<A> supplier){
        if (count >= registered.length){
            Ailment[] newArray = new Ailment[count + 1];
            System.arraycopy(registered, 0, newArray, 0, count);
            registered = newArray;
        }
        return (A)(registered[count++] = supplier.get());
    }

    public static Ailment[] allAilments(){
        return registered;
    }
    public static @Nullable Ailment byIdentifier(Identifier id){
        for (Ailment a : registered){
            if (a.ID.equals(id)) return a;
        }
        return null;
    }

    public static class RegisterAilmentsEvent extends Event implements IModBusEvent {
        public Ailment[] allRegisteredAilments() {
            return allAilments();
        }
        public <A extends Ailment> A registerAilment(Supplier<A> ailment){
            return AilmentManager.register_INTERNAL(ailment);
        }
    }

    private static void registerNativeAilments(RegisterAilmentsEvent event){
        System.out.println("Hi from [registerNativeAilments] !!!!");

        OH_OUCH_MY_BONES = event.registerAilment(OhOuchMyBones::new);
    }

    public static OhOuchMyBones OH_OUCH_MY_BONES;
}
