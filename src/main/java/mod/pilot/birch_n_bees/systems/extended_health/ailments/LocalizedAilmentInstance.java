package mod.pilot.birch_n_bees.systems.extended_health.ailments;

import net.minecraft.resources.Identifier;

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
