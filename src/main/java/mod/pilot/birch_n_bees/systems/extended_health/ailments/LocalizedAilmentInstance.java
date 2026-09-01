package mod.pilot.birch_n_bees.systems.extended_health.ailments;

import net.minecraft.resources.Identifier;

/**
 * Ignore this whole thing, it was originally going to be used as an interface to localizing ailments
 * to a specific limb, but I wanted to actually rewrite the limbs to store their own ailments
 * instead of using a master list
 *
 * <p>the entire system got scrapped anyway so its not like it matters. If you want to steal stuff from this
 * system, dont bother with this.</p>
 */
@Deprecated
public interface LocalizedAilmentInstance {
    Identifier limbID();

    class Client extends AilmentInstance.Client implements LocalizedAilmentInstance{
        public Identifier ID;
        public Client(Ailment parentType, Identifier identifier,  byte severity) {
            super(parentType, severity);
            this.ID = identifier;
        }
        @Override
        public Identifier limbID() {
            return ID;
        }
    }
    class Server extends AilmentInstance.Server implements LocalizedAilmentInstance{
        public Identifier ID;
        public Server(Ailment parentType, Identifier identifier,  byte severity, int timeUntilCured) {
            super(parentType, severity, timeUntilCured);
            this.ID = identifier;
        }
        @Override
        public Identifier limbID() {
            return ID;
        }
    }
}
