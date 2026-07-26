package mod.pilot.birch_n_bees.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class HostileMeleeGoal extends Goal {
    public final Mob mob;
    public float damage;
    public double followRange;
    public Predicate<LivingEntity> targetPredicate;
    boolean requireLineOfSight(){
        return maxUnseenTime != -1;
    }

    public double bonusRange;
    public double speed;

    public int seenTick;
    public int maxUnseenTime;
    public boolean checkVisibility(Entity target){
        if (requireLineOfSight()) {
            boolean flag = mob.hasLineOfSight(target);
            if (flag) {
                seenTick = 0;
                return true;
            } else return ++seenTick <= maxUnseenTime;
        } else return true;
    }

    public HostileMeleeGoal(Mob mob, Predicate<LivingEntity> targetPredicate) {
        this(mob, -1f, -1d, 0d, 1d, -1, targetPredicate);
    }
    public HostileMeleeGoal(Mob mob, float damage, double followRange, double bonusRange, double speed, int maxTimeNotVisible, Class<? extends LivingEntity> targetEntity) {
        this(mob, damage, followRange, bonusRange, speed, maxTimeNotVisible, targetEntity::isInstance);
    }
    public HostileMeleeGoal(Mob mob, float damage, double followRange, double bonusRange, double speed, int maxTimeNotVisible, Predicate<LivingEntity> targetPredicate) {
        this.mob = mob;
        this.damage = damage;
        this.followRange = followRange;
        this.bonusRange = bonusRange;
        this.speed = speed;
        this.maxUnseenTime = maxTimeNotVisible;
        this.targetPredicate = targetPredicate;
    }

    public double getFollowRange(){
        if (followRange == -1) return mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        else return followRange;
    }
    public double getRangeSqr(){
        double width = mob.getBbWidth();
        return width * width + bonusRange * bonusRange;
    }

    @Override
    public boolean canUse() {
        return mob.isAlive() && (mob.tickCount % 40 == 0 || mob.getRandom().nextInt(20) == 0);
    }
    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && checkVisibility(target);
    }

    @Override
    public void start() {
        Level level = mob.level();
        LivingEntity target = null;
        double dist = Double.MAX_VALUE;
        for (Entity E : level.getEntities(mob, mob.getBoundingBox().inflate(getFollowRange()),
                (entity) -> entity instanceof LivingEntity le && targetPredicate.test(le))){
            LivingEntity LE = (LivingEntity)E;
            if (!requireLineOfSight() || mob.hasLineOfSight(LE)){
                double dist1 = mob.distanceTo(LE);
                if (dist1 < dist){
                    target = LE;
                    dist = dist1;
                }
            }
        }
        if (target == null) {
            stop();
            return;
        }
        mob.setTarget(target);
        mob.setAggressive(true);
        seenTick = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || target.isDeadOrDying()) { stop(); return; }

        boolean hit = attemptHit(target);
        if (!hit){
            PathNavigation nav = mob.getNavigation();
            if (nav.isStuck()) stop();
            else if (nav.isDone() || mob.tickCount % 40 == 0){
                nav.moveTo(target.getX(), target.getY(), target.getZ(), speed);
            }
        }
    }

    protected boolean attemptHit(LivingEntity target) {
        if (mob.level() instanceof ServerLevel server) {
            double dist = mob.distanceToSqr(target);
            if (dist < getRangeSqr() && mob.hasLineOfSight(target)) {
                if (damage == -1) mob.doHurtTarget(server, target);
                else if (target.hurtServer(server, mob.damageSources().mobAttack(mob), damage)) {
                    mob.setLastHurtMob(target);
                    target.setLastHurtByMob(mob);
                }
                mob.swing(InteractionHand.MAIN_HAND, true);
                return true;
            }
        }
        return false;
    }


    @Override
    public void stop() {
        mob.setTarget(null);
        mob.setAggressive(false);
        seenTick = 0;
    }

    @Override public boolean requiresUpdateEveryTick() {return true;}
}
