package com.majruszsdifficulty.gamemodifiers.list;

import com.majruszsdifficulty.gamestage.GameStage;
import com.majruszsdifficulty.Registries;
import com.majruszsdifficulty.gamemodifiers.CustomConditions;
import com.majruszsdifficulty.gamemodifiers.configs.ProgressiveEffectConfig;
import com.mlib.annotations.AutoInstance;
import com.mlib.config.ConfigGroup;
import com.mlib.gamemodifiers.Condition;
import com.mlib.gamemodifiers.ModConfigs;
import com.mlib.gamemodifiers.contexts.OnDamaged;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Slime;

@AutoInstance
public class SlimeSlownessAttack {
	final ProgressiveEffectConfig slowness = new ProgressiveEffectConfig( MobEffects.MOVEMENT_SLOWDOWN, 0, 6.0 );

	public SlimeSlownessAttack() {
		ConfigGroup group = ModConfigs.registerSubgroup( Registries.Groups.DEFAULT )
			.name( "SlimeSlownessAttack" )
			.comment( "Slime attack may inflict slowness effect." );

		OnDamaged.listen( this::applyEffect )
			.addCondition( CustomConditions.gameStageAtLeast( GameStage.EXPERT ) )
			.addCondition( Condition.chanceCRD( 0.5, true ) )
			.addCondition( Condition.excludable() )
			.addCondition( Condition.predicate( data->data.attacker instanceof Slime ) )
			.addCondition( Condition.predicate( data->data.source.getDirectEntity() == data.attacker ) )
			.addConfig( this.slowness )
			.insertTo( group );
	}

	private void applyEffect( OnDamaged.Data data ) {
		this.slowness.apply( data.target );
	}
}
