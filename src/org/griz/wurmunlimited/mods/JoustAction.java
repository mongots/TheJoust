package org.griz.wurmunlimited.mods;

import com.sun.javafx.geom.Point2D;
import com.wurmonline.math.Vector2f;
import com.wurmonline.server.Items;

import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.*;
import com.wurmonline.server.bodys.*;
import com.wurmonline.server.combat.ArmourTypes;
import com.wurmonline.server.creatures.*;
import com.wurmonline.server.items.Item;

import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.sounds.SoundPlayer;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by griz on 10/22/2016.
 */

public class JoustAction implements ModAction {

    private Logger logger = Logger.getLogger( JoustAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;
    private int LanceID;
    private int LanceDamage;
    private int BaseHitChance;
    private int SpearSkillRange;
    private int BonusLanceDamage;
    private boolean Debug;
    private String debugstr;
    private int broadcastrange = 60;
    private int LoseHelmetChance;
    private float PerKMDamageBoost;
    private boolean AllowSkillGain;
    private float LanceRange;

    //private float OldDistance;
    private float Speed;

    public JoustAction(int id, int damage, int basehitchance, int spearskillrange, int bonuslancedamage, boolean debug, int losehemletchance, float perkmdamageboost, boolean allowskillgain, float lancerange){
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry( actionId, "Joust", "jousting", new int[] { 23, 36});
        LanceID = id;
        LanceDamage = damage;
        BaseHitChance = basehitchance;
        SpearSkillRange = spearskillrange;
        BonusLanceDamage = bonuslancedamage;
        Debug = debug;
        LoseHelmetChance = losehemletchance;
        PerKMDamageBoost = perkmdamageboost;
        AllowSkillGain = allowskillgain;
        LanceRange = lancerange;

        ModActions.registerAction( actionEntry);
    }

    public Creature FindMount( Creature rider){
        return FindMount( (Player)rider);
    }

    /* I would like to find a better way to reference the animal a player is currently riding.*/
    public Creature FindMount( Player rider){
        Creature[] creatures = Creatures.getInstance().getCreatures();
        Creature mount = null;

        for( int idx = 0; idx < creatures.length; ++idx){
            if( creatures[idx].isRiddenBy( rider.getWurmId())){
                mount = creatures[idx];
                break;
            }
        }
        return mount;
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return new BehaviourProvider() {
            @Override
            public List<ActionEntry> getBehavioursFor( Creature performer, Item subject, Creature target){
                try {
                    if ((performer.getRighthandItem().getTemplateId() == LanceID && target.getRighthandItem().getTemplateId() == LanceID) && (performer.isPlayer() && target.isPlayer()) && FindMount( performer) != null && FindMount( target) != null) {
                        return Collections.singletonList(actionEntry);
                    } else {
                        return null;
                    }
                } catch (Exception e) {}
                return null;
            }
        };
    }

    @Override
    public ActionPerformer getActionPerformer() {

        return new ActionPerformer() {

            @Override
            public short getActionId() {
                return actionId;
            }

            public void FixCorpse( Creature deadguy){
                FixCorpse( (Player)deadguy);
            }

            public void FixCorpse( Player deadguy){
                Item[] items = Items.getAllItems();
                Item corpse = null;

                for(int idx = 0; idx < items.length; ++idx) {
                    if( items[idx].getZoneId() > -1 && items[idx].getTemplateId() == 272 && items[idx].getName().equals("corpse of " + deadguy.getName())) {
                        items[idx].setProtected( false);
                    }
                }
            }

            public byte GetJoustingHitLocation(){
                byte ret = BodyHuman.leftArm;

                switch((int)(Server.rand.nextFloat() * 100)){
                    case 1:case 2:case 3: ret = BodyHuman.face; break; //3% chance
                    case 4:case 5:case 6: ret = BodyHuman.head; break; //3% chance
                    case 7:case 8:case 9:case 10:case 11:case 12:case 13:case 14:case 15:case 16:
                    case 17:case 18:case 19:case 20:case 21:case 22:case 23:case 24:case 25:case 26:
                    case 27:case 28:case 29:case 30:case 31:case 32:case 33:case 34:case 35:case 36:
                    case 37:case 38:case 39:case 40:case 41:case 42:case 43:case 44:case 45:case 46: ret = BodyHuman.torso; break; //40% torso
                    case 47:case 48:case 49:case 50:case 51:case 52:case 53:case 54:case 55:case 56:
                    case 57:case 58:case 59:case 60:case 61:case 62:case 63:case 64:case 65:case 66:
                    case 67:case 68:case 69:case 70:case 71:case 72:case 73:case 74:case 75:case 76:
                    case 77:case 78:case 79:case 80:case 81:case 82:case 83:case 84:case 85:case 86: ret = BodyHuman.leftArm; break; //40% left arm
                    case 87:case 88:case 89:case 90:case 91:case 92:case 93:case 94:case 95:case 96: ret = BodyHuman.leftLeg; break; //10% left leg
                    case 97:case 98:case 99:case 100: ret = BodyHuman.leftFoot; break; //4% left foot
                }
                return ret;
            }

            public boolean DidJoustingHit( Creature player){
                return DidJoustingHit( (Player) player);
            }

            public boolean DidJoustingHit(Player player){
                float hitroll = Server.rand.nextFloat() * 100;
                double HitChance = 0;
                try {
                    HitChance = BaseHitChance + (SpearSkillRange * (player.getSkills().getSkillOrLearn(10088).getRealKnowledge() / 100));

                    if( AllowSkillGain) {
                        Skill lanceskill = player.getSkills().getSkillOrLearn( player.getRighthandItem().getPrimarySkill());
                        lanceskill.skillCheck( HitChance / 100, 2000.0D, false, 1.0f, true, 1.0d);
                    }

                } catch (Exception e){}

                return (hitroll < HitChance);
            }

            public double GetJoustingDamage( Creature player){
                return GetJoustingDamage( (Player)player);
            }

            public double GetJoustingDamage( Player player){
                double ret = 0;

                ret = (player.getRighthandItem().getCurrentQualityLevel() / 100) * (LanceDamage + ( Server.rand.nextInt( BonusLanceDamage + 1)));
                debugstr = debugstr.concat("with a " + player.getRighthandItem().getTemplate().getName() + " ");

                Speed = player.getMovementScheme().getMountSpeed();
                debugstr = debugstr.concat(" at the speed of: " + Speed + " ");

                // I can't for the life of me figure out how to get a Players speed..... I'm just missing something obvious I think.

                double tmp = ret * (Speed * PerKMDamageBoost);
                ret = tmp + ret;
                ret *= 1000;
                return ret;
            }

            public double AdjustJoustingDamage( Creature target, double damage, byte location){
                return AdjustJoustingDamage( (Player) target, damage, location);
            }

            public double AdjustJoustingDamage( Player target, double damage, byte location){
                float damageMod = 1.0f;
                try {
                    Item armour = target.getArmour(location);
                    float qualitymod = 0;
                    if( armour != null) {
                        int armourType = armour.getArmourType();
                        qualitymod = 0.1f * (armour.getCurrentQualityLevel() / 100);

                        switch (armourType) {
                            case ArmourTypes.ARMOUR_NONE: damageMod = 1.0f; break;
                            case ArmourTypes.ARMOUR_CLOTH: damageMod = 0.9f - qualitymod; break;
                            case ArmourTypes.ARMOUR_LEATHER: damageMod = 0.8f - qualitymod; break;
                            case ArmourTypes.ARMOUR_STUDDED: damageMod = 0.7f - qualitymod; break;
                            case ArmourTypes.ARMOUR_CHAIN: damageMod = 0.6f - qualitymod; break;
                            case ArmourTypes.ARMOUR_PLATE: damageMod = 0.5f - qualitymod; break;
                            case ArmourTypes.ARMOUR_LEATHER_DRAGON:damageMod = 0.4f - qualitymod; break;
                            case ArmourTypes.ARMOUR_SCALE_DRAGON: damageMod = 0.3f - qualitymod; break;
                        }
                    }

                    debugstr = debugstr.concat( "but the " + armour.getName() + "(" + damageMod + "/" + qualitymod) + ") ";

                } catch (Exception e) {}

                return damage * damageMod;
            }

            public DbWound RunJoust(Player attacker, Player defender){
                debugstr = "";
                DbWound wound = null;

                if ( DidJoustingHit( attacker)) {
                    debugstr = debugstr.concat( attacker.getName() + " hit " + defender.getName() + " ");
                    boolean blocked = false;

                    //shield effects.
                    try {
                        Item Shield = defender.getShield();
                        if (Shield != null) {
                            Skill shieldskill = defender.getSkills().getSkillOrLearn(Shield.getPrimarySkill());
                            float blockroll = Server.rand.nextFloat() * 100;

                            if( AllowSkillGain) {
                                shieldskill.skillCheck( blockroll / 100, 2000.0D, false, 1.0f, true, 1.0d);
                            }

                            if( blockroll < (shieldskill.getRealKnowledge() / 3)){
                                blocked = true;
                            }
                        }
                    } catch( Exception e) {}

                    byte hitlocation = GetJoustingHitLocation();

                    if( blocked) {
                        hitlocation = BodyHuman.leftArm;
                        Server.getInstance().broadCastMessage(attacker.getName() + " strikes " + defender.getName() + " but " + defender.getHeSheItString() + " blocked with their shield!", defender.getTileX(), defender.getTileY(), true, broadcastrange);
                        debugstr = debugstr.concat(" but " + defender.getHeSheItString() + " blocked with their shield! ");
                    } else {
                        debugstr = debugstr.concat("in the " + defender.getBody().getWoundLocationString(hitlocation) + " ");
                        Server.getInstance().broadCastMessage(attacker.getName() + " strikes " + defender.getName() + " in the " + defender.getBody().getWoundLocationString(hitlocation) + "!", defender.getTileX(), defender.getTileY(), true, broadcastrange);
                    }
                    double p1Damage = GetJoustingDamage(attacker);

                    if( blocked){
                        debugstr = debugstr.concat(" for " + p1Damage + " damage ");
                        p1Damage -= 1000;
                        debugstr = debugstr.concat(" the shield deflected some damage ");
                    } else {
                        debugstr = debugstr.concat("for " + p1Damage + " damage ");
                    }

                    p1Damage = AdjustJoustingDamage(defender, p1Damage, hitlocation);
                    debugstr = debugstr.concat("adjusted the damage to " + p1Damage);

                    if (p1Damage > 0) {

                        //do they keep their helmet?
                        if (hitlocation == BodyHuman.head || hitlocation == BodyHuman.face) {
                            if ((Server.rand.nextFloat() * 100) < LoseHelmetChance) {
                                try {
                                    Item helmet = defender.getArmour(BodyHuman.head);
                                    if (helmet != null) {
                                        defender.dropItem(helmet);
                                        Server.getInstance().broadCastMessage(defender.getName() + " had their helmet torn off!", defender.getTileX(), defender.getTileY(), true, broadcastrange);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }

                        //Does the lance break?
                        boolean broken = false;
                        try {
                            Item lance = attacker.getRighthandItem();
                            if (lance != null) {
                                if( lance.getDamage() + 30 < 100) {
                                    if ((Server.rand.nextFloat() * 100) < (lance.getDamage() + 30)) {
                                        broken = true;
                                        Server.getInstance().broadCastMessage(attacker.getName() + " has broken their lance!", attacker.getTileX(), attacker.getTileY(), true, broadcastrange);
                                        Items.destroyItem(lance.getWurmId());
                                        SoundPlayer.playSound("sound.destroyobject.wood.axe", attacker.getTileX(), attacker.getTileY(), true, 1.0f);
                                    }
                                } else {
                                    broken = true;
                                    Server.getInstance().broadCastMessage(attacker.getName() + " has broken their lance!", attacker.getTileX(), attacker.getTileY(), true, broadcastrange);
                                    Items.destroyItem(lance.getWurmId());
                                    SoundPlayer.playSound("sound.destroyobject.wood.axe", attacker.getTileX(), attacker.getTileY(), true, 1.0f);
                                }
                            }

                        } catch (Exception e) {
                        }

                        //Did they get knocked off their horse?
                        double bodyaverage = (defender.getSkills().getSkillOrLearn(102).getRealKnowledge() + defender.getSkills().getSkillOrLearn(103).getRealKnowledge() +defender.getSkills().getSkillOrLearn(104).getRealKnowledge()) / 3;
                        if( Server.rand.nextFloat() * 100 > bodyaverage){
                            Server.getInstance().broadCastMessage(defender.getName() + " has been dismounted!", defender.getTileX(), defender.getTileY(), true, broadcastrange);
                            defender.disembark( true);
                        }

                        if( !broken){
                            Item lance = attacker.getRighthandItem();
                            lance.setDamage(30.0f + lance.getDamage());
                        }

                        wound = new DbWound(Wound.TYPE_CRUSH, hitlocation, (float) p1Damage, defender.getWurmId(), 0.0f, 0.0f, false);
                        SoundPlayer.playSound("sound.combat.fleshmetal1", attacker.getTileX(), attacker.getTileY(), true, 1.0f);

                    } else {
                        Server.getInstance().broadCastMessage(attacker.getName() + " missed.", attacker.getTileX(), attacker.getTileY(), true, broadcastrange);
                        SoundPlayer.playSound("sound.combat.miss.heavy", attacker.getTileX(), attacker.getTileY(), true, 1.0f);
                    }
                } else {
                    debugstr =  debugstr.concat( attacker.getName() + " missed " + defender.getName());
                    Server.getInstance().broadCastMessage( attacker.getName() + " missed!", attacker.getTileX(), attacker.getTileY(), true, broadcastrange);
                }

                if( Debug) {
                    attacker.getCommunicator().sendToGlobalKingdomChat(debugstr);
                    logger.log(Level.INFO, debugstr);
                }

                return wound;
            }

            @Override
            public boolean action(Action action, Creature performer, Item source, Creature target, short num, float counter) {
                try {
                    if (counter == 1.0f) {
                        Server.getInstance().broadCastMessage( "Let the joust begin, CHARGE!", target.getTileX(), target.getTileY(), true, broadcastrange);

                        int time = 150;

                        performer.getCurrentAction().setTimeLeft(time);
                        performer.sendActionControl("Jousting", true, time);

                        target.stopCurrentAction();
                        return false;

                    }else{

                        if (performer.getPos2f().distance(target.getPos2f()) < LanceRange) {

                            DbWound targetwound = RunJoust( (Player)performer, (Player)target);
                            DbWound performerwound = RunJoust( (Player)target, (Player)performer);

                            performer.getStatus().modifyStamina( -150.0f);
                            target.getStatus().modifyStamina( -150.0f);

                            if( targetwound != null){
                                target.getBody().addWound((Wound) targetwound);
                            }

                            if( performerwound != null){
                                performer.getBody().addWound(performerwound);
                            }

                            return true;
                        }

                        int time = performer.getCurrentAction().getTimeLeft();
                        if( counter * 10 > time){
                            performer.getCommunicator().sendNormalServerMessage("You took too long to joust, start again.");
                            target.getCommunicator().sendNormalServerMessage("You took too long to joust, start again.");

                            performer.stopCurrentAction();
                            target.stopCurrentAction();
                            return true;
                        }
                        return false;
                    }
                } catch (NoSuchActionException e) {}
                return false;
            }
        };
    }
}