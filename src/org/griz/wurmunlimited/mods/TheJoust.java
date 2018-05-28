package org.griz.wurmunlimited.mods;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.items.*;
import com.wurmonline.server.skills.SkillList;

import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Griz on 10/22/2016.
 */

public class TheJoust implements WurmServerMod, ItemTemplatesCreatedListener, ServerStartedListener, ItemTypes, MiscConstants, Configurable{

    private int LanceID;
    private int LanceDamage;
    private int BaseHitChance;
    private int SpearSkillRange;
    private int BonusLanceDamage;
    private boolean Debug;
    private int LoseHelmetChance;
    private float PerKMDamageBoost;
    private boolean AllowSkillGain;
    private boolean AllowCraftingLance;
    private float LanceRange;

    private static Logger logger = Logger.getLogger(TheJoust.class.getName());

    @Override
    public void configure(Properties properties){
        LanceDamage = Integer.valueOf( properties.getProperty("LanceDamage"));
        BonusLanceDamage = Integer.valueOf( properties.getProperty("BonusLanceDamage"));
        BaseHitChance = Integer.valueOf( properties.getProperty("BaseHitChance"));
        SpearSkillRange = Integer.valueOf( properties.getProperty("SkillRange"));
        Debug = Boolean.valueOf( properties.getProperty("Debug"));
        LoseHelmetChance = Integer.valueOf( properties.getProperty("LoseHelmetChance"));
        PerKMDamageBoost = Float.valueOf( properties.getProperty("PerKMDamageBonus"));
        AllowSkillGain = Boolean.valueOf( properties.getProperty("AllowSkillGain"));
        AllowCraftingLance = Boolean.valueOf( properties.getProperty("AllowCraftingLance"));
        LanceRange = Float.valueOf( properties.getProperty( "LanceRange"));
    }

    @Override
    public void onItemTemplatesCreated() {

        try{
            ItemTemplateBuilder itemTemplateBuilder = new ItemTemplateBuilder("griz.joustlance");
            itemTemplateBuilder.name("jousting lance", "jousting lances", "A long spear used in the sport of jousting.");
            itemTemplateBuilder.descriptions("excellent", "good", "ok", "poor");
            itemTemplateBuilder.itemTypes(new short[] {
                    ITEM_TYPE_WEAPON,
                    ITEM_TYPE_WEAPON_PIERCE,
                    ITEM_TYPE_WOOD,
                    ITEM_TYPE_REPAIRABLE,
                    ITEM_TYPE_DESTROYABLE,
                    ITEM_TYPE_EQUIPMENTSLOT,
                    ITEM_TYPE_DECAYDESTROYS
            });

            itemTemplateBuilder.imageNumber((short) 1221);
            itemTemplateBuilder.behaviourType((short) 1);
            itemTemplateBuilder.combatDamage(30);
            itemTemplateBuilder.decayTime(3024000L);
            itemTemplateBuilder.dimensions(3, 5, 205);
            itemTemplateBuilder.primarySkill((int) 10088);
            itemTemplateBuilder.bodySpaces(EMPTY_BYTE_PRIMITIVE_ARRAY);
            itemTemplateBuilder.modelName("model.weapon.polearm.spear.long");
            itemTemplateBuilder.difficulty(20.0f);
            itemTemplateBuilder.weightGrams(2700);
            itemTemplateBuilder.material((byte) 14);
            ItemTemplate LanceTemplate = itemTemplateBuilder.build();
            this.LanceID = LanceTemplate.getTemplateId();

        } catch (Exception e){}
    }

    @Override
    public void onServerStarted() {

        logger.log(Level.INFO, "Registering jousting action");

        ModActions.registerAction( new JoustAction( LanceID, LanceDamage, BaseHitChance, SpearSkillRange, BonusLanceDamage, Debug, LoseHelmetChance, PerKMDamageBoost, AllowSkillGain, LanceRange));

        if (LanceID > 0 && AllowCraftingLance) {
            final AdvancedCreationEntry creationEntry = CreationEntryCreator.createAdvancedEntry(SkillList.CARPENTRY, ItemList.knifeCarving, ItemList.log, LanceID, false, true, 50.0f, false, false, CreationCategories.WEAPONS);
            creationEntry.addRequirement(new CreationRequirement(1, ItemList.sheetIron, 1, true));
        }

        logger.log(Level.INFO, "Finished registering jousting action");

    }
}