package com.majruszsdifficulty.items;

import com.majruszsdifficulty.MajruszsDifficulty;
import com.majruszsdifficulty.MajruszsHelper;
import com.majruszsdifficulty.Registries;
import com.majruszsdifficulty.events.TreasureBagOpenedEvent;
import com.majruszsdifficulty.features.treasure_bag.LootProgress;
import com.majruszsdifficulty.features.treasure_bag.LootProgressClient;
import com.mlib.config.AvailabilityConfig;
import com.mlib.config.ConfigGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.majruszsdifficulty.MajruszsDifficulty.FEATURES_GROUP;

/** Class representing treasure bag. */
@Mod.EventBusSubscriber
public class TreasureBagItem extends Item {
	public final static List< TreasureBagItem > TREASURE_BAGS = new ArrayList<>();
	protected final static ConfigGroup CONFIG_GROUP;
	private final static String ITEM_TOOLTIP_TRANSLATION_KEY = "majruszsdifficulty.treasure_bag.item_tooltip";

	static {
		CONFIG_GROUP = new ConfigGroup( "TreasureBag", "Configuration for treasure bags." );
		FEATURES_GROUP.addGroup( CONFIG_GROUP );
	}

	private final ResourceLocation lootTableLocation;
	private final String id;
	private final AvailabilityConfig availability;

	public TreasureBagItem( String id, String entityNameForConfiguration ) {
		super( ( new Item.Properties() ).stacksTo( 16 ).tab( Registries.ITEM_GROUP ).rarity( Rarity.UNCOMMON ) );

		this.lootTableLocation = new ResourceLocation( MajruszsDifficulty.MOD_ID, "gameplay/" + id + "_treasure_loot" );
		this.id = id;
		this.availability = new AvailabilityConfig( id, getComment( entityNameForConfiguration ), false, true );
		CONFIG_GROUP.addConfig( this.availability );

		TREASURE_BAGS.add( this );
	}

	/** Opening treasure bag on right click. */
	@Override
	public InteractionResultHolder< ItemStack > use( Level world, Player player, InteractionHand hand ) {
		ItemStack itemStack = player.getItemInHand( hand );

		if( !world.isClientSide ) {
			if( !player.getAbilities().instabuild )
				itemStack.shrink( 1 );
			if( player instanceof ServerPlayer )
				handleStatistics( ( ServerPlayer )player );

			world.playSound( null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.AMBIENT, 1.0f, 0.9f );

			if( this.availability.isEnabled() ) {
				List< ItemStack > loot = generateLoot( player );
				MinecraftForge.EVENT_BUS.post( new TreasureBagOpenedEvent( player, this, loot ) );
				LootProgress.updateProgress( this, player, loot );
				for( ItemStack reward : loot ) {
					if( player.canTakeItem( reward ) )
						player.getInventory().add( reward );
					else
						world.addFreshEntity( new ItemEntity( world, player.getX(), player.getY() + 1, player.getZ(), reward ) );
				}
			}
		}

		return InteractionResultHolder.sidedSuccess( itemStack, world.isClientSide() );
	}

	/** Adding simple tooltip to treasure bag. */
	@Override
	@OnlyIn( Dist.CLIENT )
	public void appendHoverText( ItemStack itemStack, @Nullable Level world, List< Component > tooltip, TooltipFlag flag ) {
		MajruszsHelper.addAdvancedTranslatableTexts( tooltip, flag, ITEM_TOOLTIP_TRANSLATION_KEY, " " );

		LootProgressClient.addDropList( this, tooltip );
	}

	/** Generating loot context of current treasure bag. (who opened the bag, where, etc.) */
	public static LootContext generateLootContext( Player player ) {
		LootContext.Builder lootContextBuilder = new LootContext.Builder( ( ServerLevel )player.level );
		lootContextBuilder.withParameter( LootContextParams.ORIGIN, player.position() );
		lootContextBuilder.withParameter( LootContextParams.THIS_ENTITY, player );

		return lootContextBuilder.create( LootContextParamSets.GIFT );
	}

	/** Creates comment for configuration. */
	private static String getComment( String treasureBagSourceName ) {
		return "Is treasure bag from " + treasureBagSourceName + " available in survival mode?";
	}

	/** Checks whether treasure bag is not disabled in configuration file? */
	public boolean isAvailable() {
		return this.availability.isEnabled();
	}

	/**
	 Generating random loot from loot table.

	 @param player Player required to generate loot context.
	 */
	protected List< ItemStack > generateLoot( Player player ) {
		LootTable lootTable = getLootTable();

		return lootTable.getRandomItems( generateLootContext( player ) );
	}

	/** Returning loot table for current treasure bag. (possible loot) */
	public LootTable getLootTable() {
		return ServerLifecycleHooks.getCurrentServer().getLootTables().get( this.lootTableLocation );
	}

	/** Handles statistics and advancements for a Treasure Bag. */
	protected void handleStatistics( ServerPlayer player ) {
		Stat< ? > statistic = Stats.ITEM_USED.get( this );
		ServerStatsCounter statisticsManager = player.getStats();

		player.awardStat( statistic );
		Registries.TREASURE_BAG_TRIGGER.trigger( player, this, statisticsManager.getValue( statistic ) );
	}

	public static class UndeadArmy extends TreasureBagItem {
		public UndeadArmy() {
			super( "undead_army", "Undead Army" );
		}
	}

	public static class ElderGuardian extends TreasureBagItem {
		public ElderGuardian() {
			super( "elder_guardian", "Elder Guardian" );
		}
	}

	public static class Wither extends TreasureBagItem {
		public Wither() {
			super( "wither", "Wither" );
		}
	}

	public static class EnderDragon extends TreasureBagItem {
		public EnderDragon() {
			super( "ender_dragon", "Ender Dragon" );
		}
	}

	public static class Fishing extends TreasureBagItem {
		public Fishing() {
			super( "fishing", "Fishing" );
		}
	}

	public static class Pillager extends TreasureBagItem {
		public Pillager() {
			super( "pillager", "Pillager Raid" );
		}
	}
}